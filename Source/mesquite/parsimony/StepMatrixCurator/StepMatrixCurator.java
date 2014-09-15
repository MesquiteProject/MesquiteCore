/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.parsimony.StepMatrixCurator;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.parsimony.lib.*;

/* ======================================================================== */
public class StepMatrixCurator extends WholeCharModelCurator implements EditingCurator {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed

		EmployeeNeed e = registerEmployeeNeed(WindowHolder.class, getName() + " need assistance to hold a window" ,
		"This is arranged automatically");
	}
	public String getName() {
		return "Stepmatrix";
	}
	public String getExplanation() {
		return "Supplies editor for and manages stepmatrices (cost matrices).";
	}

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Step (Cost) Matrix";
	}

	/*.................................................................................................................*/
	/** passes which object changed, along with optional code number (type of change) and integers (e.g. which character)*/
	public void changed(Object caller, Object obj, Notification notification){
		if (obj instanceof CharacterModel){
			int i = getModelNumber((CharacterModel)obj);
			if (i>=0) {
				CategTModelEditWindow window = (CategTModelEditWindow)getWindow(i);
				if (window!=null)
					window.setModel((CategTModel)obj);
			}
		}
		super.changed(caller, obj, notification);
	}
	/*.................................................................................................................*/
	public MesquiteModule editModelNonModal(CharacterModel model, ObjectContainer w){
		if (model!=null && model instanceof StepMatrixModel) {
			StepMatrixModel modelToEdit = (StepMatrixModel)model;
			MesquiteModule windowServer = hireNamedEmployee(WindowHolder.class, "#WindowBabysitter");
			if (windowServer == null)
				return null;

			CategTModelEditWindow window = new CategTModelEditWindow(this, windowServer);
			windowServer.makeMenu("Step_Matrix");
			windowServer.addMenuItem("Set maximum state...", makeCommand("setMaxState", modelToEdit));
			if (modelToEdit.isDNA()) {
				windowServer.addMenuItem("Select Transversions", makeCommand("selectTransversions", modelToEdit));
				windowServer.addMenuItem("Select Transitions", makeCommand("selectTransitions", modelToEdit));
			}
			modelToEdit.setWindow(window);

			window.setDefaultAnnotatable(model);
			window.setModel((StepMatrixModel)model);

			if (w!=null)
				w.setObject(window);
			return windowServer;
		}
		return this;
	}
	public boolean curatesModelClass(Class modelClass){
		return StepMatrixModel.class.isAssignableFrom(modelClass);
	}
	/*.................................................................................................................*/
	public String getNameOfModelClass() {
		return "Stepmatrix";
	}
	/*.................................................................................................................*/
	public String getNEXUSNameOfModelClass() {
		return "Stepmatrix";
	}
	/*.................................................................................................................*/
	public Class getModelClass() {
		return StepMatrixModel.class;
	}
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public CharacterModel makeNewModel(String name) {
		StepMatrixModel model = new StepMatrixModel(name, getProject());
		return model;
	}
	/*.................................................................................................................*/
	public CharacterModel readCharacterModel(String name, MesquiteInteger stringPos, String description, int format) {
		StepMatrixModel model = new StepMatrixModel(name, getProject());
		model.fromString(description, stringPos, format);
		return model;
	}
	/*.................................................................................................................*/

	public String getNexusCommands(MesquiteFile file, String blockName){ return "";}
}

/*=======================================================*/
class StepmatrixNexusCmdTest  extends NexusCommandTest{
	public boolean readsWritesCommand(String blockName, String commandName, String command){  //returns whether or not can deal with command
		return (blockName.equalsIgnoreCase("ASSUMPTIONS") && commandName.equalsIgnoreCase("stepmatrix"));
	}
}

/* ======================================================================== */
/** A character model for categorical characters to be used in parsimony calculations*/
class StepMatrixModel  extends CostMatrixModel implements CategTModel {
	int[] states;
	NumberArray costs;
	int numStates;
	MesquiteNumber utilityNumber;
	MesquiteProject project;
	int maxNumStates = CategoricalState.maxCategoricalState+1;
	boolean isDNA=false;

	CategTModelEditWindow window = null;

	public StepMatrixModel (String name, MesquiteProject project) {
		super(name, CategoricalState.class);
		this.project = project;
		costs = new NumberArray(maxNumStates*maxNumStates);
		states = new int[maxNumStates];
		for (int i=0; i< maxNumStates*maxNumStates; i++) {
			costs.setValue(i, 1);
		}
		for (int i=0; i< maxNumStates; i++) {
			costs.setValue(i*maxNumStates + i, 0);
		}
		numStates = 10;
		if (project != null && project.getNumberCharMatrices(CategoricalState.class) ==1) {
			CategoricalData data = (CategoricalData)project.getCharacterMatrix(0, CategoricalState.class);
			if (data!=null) {
				numStates = data.getMaxPossibleState()+1;
				isDNA = data instanceof DNAData;
			}

		}
		for (int i=0; i< maxNumStates; i++) {
			states[i]=i;
		}
		utilityNumber = new MesquiteNumber();
	}

	/** returns whether or not size can be changed externally*/
	public boolean canChangeSize() {
		return true;
	}
	/** returns whether or not model is default (unordered, initial Jukes-Cantor; if so, then doesn't need to be written to file*/
	public boolean isBuiltIn() {
		return false;
	}
	/** returns name of model class (e.g. "stepmatrix")*/
	public String getNEXUSClassName() {
		return "Stepmatrix";
	}
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the maximum state value handled by the step matrix", "[state number]", commandName, "setMaxState")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int maxState = MesquiteInteger.fromString(arguments, io);
			int previous = getMaxStateDefined();

			if (!MesquiteInteger.isCombinable(maxState) || maxState<0 || maxState> CategoricalState.maxCategoricalState){
				maxState = MesquiteInteger.queryInteger(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Set Maximum state", "Maximum state value handled by the step matrix", getMaxStateDefined());
			}
			if (maxState !=getMaxStateDefined() && !(!MesquiteInteger.isCombinable(maxState) || maxState<0 || maxState> CategoricalState.maxCategoricalState)){
				if (previous< maxState){  //fill in with 1's
					for (int beginState= previous+1; beginState <= maxState; beginState++) {
						for (int endState = 0; endState <= maxState; endState++)
							costs.setValue(beginState*maxNumStates+endState, 1);
					}
					for (int beginState= 0; beginState <= maxState; beginState++) {
						for (int endState = previous+1; endState <= maxState; endState++)
							costs.setValue(beginState*maxNumStates+endState, 1);
					}
					for (int beginState= previous+1; beginState <= maxState; beginState++) {
						costs.setValue(beginState*maxNumStates+beginState, 1);
					}
				}
				setMaxStateDefined(maxState);
			}
		} else if (checker.compare(this.getClass(), "Selects the cells corresponding to tranversions", "[]", commandName, "selectTransversions")) {
			if (window!=null) {
				window.getTable().selectAndRedrawCell(0,1);
				window.getTable().selectAndRedrawCell(0,3);
				window.getTable().selectAndRedrawCell(1,0);
				window.getTable().selectAndRedrawCell(1,2);
				window.getTable().selectAndRedrawCell(2,1);
				window.getTable().selectAndRedrawCell(2,3);
				window.getTable().selectAndRedrawCell(3,0);
				window.getTable().selectAndRedrawCell(3,2);

			}
		} else if (checker.compare(this.getClass(), "Selects the cells corresponding to transitions", "[]", commandName, "selectTransitions")) {
			if (window!=null) {
				window.getTable().selectAndRedrawCell(0,2);
				window.getTable().selectAndRedrawCell(1,3);
				window.getTable().selectAndRedrawCell(2,0);
				window.getTable().selectAndRedrawCell(3,1);

			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	public void setMaxState(int maxState){
		if (maxState>0 && maxState<=CategoricalState.maxCategoricalState) {
			numStates = maxState+1;
			notifyListeners(this, new Notification(MesquiteListener.UNKNOWN),CharacterModel.class, true);
			notifyListeners(this, new Notification(MesquiteListener.UNKNOWN),CharacterModel.class, false);
		}
	}
	public void setMaxStateDefined(int maxState){
		setMaxState(maxState);
	}
	public boolean isDNA(){
		return isDNA;
	}
	public int getMaxStateDefined(){
		return numStates -1;
	}

	public String getStateSymbol(int state){
		if (symbols != null) {
			if (state >= 0 && state < symbols.length && !StringUtil.blank(symbols[state]))
				return symbols[state];
			if (state < 0)
				return CategoricalData.getDefaultStateSymbol(state);

			return Integer.toString(state);
		}

		if (project != null && project.getNumberCharMatrices(CategoricalState.class) ==1) {
			CategoricalData data = (CategoricalData)project.getCharacterMatrix(0, CategoricalState.class);
			String s = getDataSymbol(data, state);
			if (StringUtil.blank(s))
				return Integer.toString(state);
			return s;
		}
		return CategoricalData.getDefaultStateSymbol(state);
	}

	private String getDataSymbol(CategoricalData data, int state){
		try{
			return "" + data.getSymbol(state);
		}
		catch(Exception e){
		}
		return "";
	}

	String[] symbols;
	private boolean symbolsMatch(String[] symbolsFound, CategoricalData data){
		if (symbolsFound == null || data == null)
			return false;
		for (int i = 0; i< symbolsFound.length; i++)
			if (symbolsFound[i]!= null && !symbolsFound[i].equals(getDataSymbol(data, i)))
				return false;
		return true;
	}
	private boolean symbolsProtein(String[] symbolsFound){
		if (symbolsFound == null)
			return false;
		for (int i = 0; i< symbolsFound.length; i++) {
			if (symbolsFound[i]!= null && !symbolsFound[i].equals(ProteinData.getDefaultStateSymbol(i)))
				return false;
		}
		return true;
	}
	private boolean symbolsDNA(String[] symbolsFound){
		if (symbolsFound == null)
			return false;
		for (int i = 0; i< symbolsFound.length; i++)
			if (symbolsFound[i]!= null && !symbolsFound[i].equals(DNAData.getDefaultStateSymbol(i)))
				return false;
		return true;
	}
	private boolean symbolsRNA(String[] symbolsFound){
		if (symbolsFound == null)
			return false;
		for (int i = 0; i< symbolsFound.length; i++)
			if (symbolsFound[i]!= null && !symbolsFound[i].equals(RNAData.getDefaultStateSymbol(i)))
				return false;
		return true;
	}

	//this is a workaround of NEXUS difficulty of stepmatrix using symbols but not referring to which matrix!  This will not work at times with multiple matrix files
	private void determineSymbols(String description, int currentPos, int matrixSize){
		if (matrixSize > CategoricalState.maxCategoricalState+1)
			matrixSize = CategoricalState.maxCategoricalState + 1;
		symbols = new String[CategoricalState.maxCategoricalState+1];
		MesquiteInteger stringPos = new MesquiteInteger(currentPos);
		for (int i=0; i<=CategoricalState.maxCategoricalState; i++)
			symbols[i] = null;
		boolean multiFound = false;
		String[] symbolsFound = new String[CategoricalState.maxCategoricalState+1];
		for (int i=0; i<matrixSize; i++) {
			symbolsFound[i] =  ParseUtil.getToken(description, stringPos);
			if (symbolsFound[i]  != null && symbolsFound[i].length() >1)
				multiFound = true; //multichar symbol; must be state; can't use matrix symbol
		}

		if (project != null && project.getNumberCharMatrices(CategoricalState.class) >=1) {
			//check last read matrix
			CategoricalData data = (CategoricalData)project.getCharacterMatrix(project.getNumberCharMatrices(CategoricalState.class)-1, CategoricalState.class);
			if (symbolsMatch(symbolsFound, data)) {
				for (int i=0; i<=CategoricalState.maxCategoricalState; i++)
					symbols[i] = getDataSymbol(data, i);
				return;
			}
			//check other matrices for symbols match
			for (int iD = 0; iD< project.getNumberCharMatrices(CategoricalState.class); iD++){
				CategoricalData data2 = (CategoricalData)project.getCharacterMatrix(iD, CategoricalState.class);
				if (symbolsMatch(symbolsFound, data2)) {
					for (int i=0; i<=CategoricalState.maxCategoricalState; i++)
						symbols[i] = getDataSymbol(data2, i);
					return;
				}
			}
		}
		if (symbolsProtein(symbolsFound)) {
			for (int i=0; i<=CategoricalState.maxCategoricalState; i++)
				symbols[i] = ProteinData.getDefaultStateSymbol(i);
		}
		else if (symbolsRNA(symbolsFound)){
			for (int i=0; i<=CategoricalState.maxCategoricalState; i++)
				symbols[i] = RNAData.getDefaultStateSymbol(i);
		}
		else if (symbolsDNA(symbolsFound)){
			for (int i=0; i<=CategoricalState.maxCategoricalState; i++)
				symbols[i] = DNAData.getDefaultStateSymbol(i);
		}
		else if (!multiFound && project != null && project.getNumberCharMatrices(CategoricalState.class) >=1) {
			//use last read matrix
			CategoricalData data = (CategoricalData)project.getCharacterMatrix(project.getNumberCharMatrices(CategoricalState.class)-1, CategoricalState.class);
			for (int i=0; i<=CategoricalState.maxCategoricalState; i++)
				symbols[i] = getDataSymbol(data, i);
		}
		else {
			for (int i=0; i<=CategoricalState.maxCategoricalState && i<symbolsFound.length; i++){
				if (symbolsFound[i] != null && symbolsFound[i].equals("2147483646")){  //workaround for file improperly written because of an old bug...
					if (project.getNumberCharMatrices(CategoricalState.class) >0){
						CategoricalData data = (CategoricalData)project.getCharacterMatrix(project.getNumberCharMatrices(CategoricalState.class)-1, CategoricalState.class);
						symbolsFound[i] = data.getStateSymbol(0, i);
					}
					else
						symbolsFound[i] = CategoricalData.getDefaultStateSymbol(CategoricalData.class, i);
				}
				symbols[i] = symbolsFound[i];  
			}
		}

	}

	public void fromString (String description, MesquiteInteger stringPos, int format) {
		costs.deassignArray();
		for (int i=0; i<states.length; i++)
			states[i]=-1;
		int matrixSize = MesquiteInteger.fromString(description, stringPos);
		int maxS = matrixSize-1;
		determineSymbols(description, stringPos.getValue(), matrixSize);
		CategoricalState cs = new CategoricalState();
		for (int i=0; i<matrixSize; i++) {
			String s = ParseUtil.getToken(description, stringPos);
			int state = -1;
			if (s !=null && s.length()==1) {  //single state; symbol
				state = StringArray.indexOf(symbols, s);
				if (state<0)
					cs.whichState(s.charAt(0));
			}
			else
				state = MesquiteInteger.fromString(s);
			if (MesquiteInteger.isCombinable(state) && state>=0 && state<=CategoricalState.maxCategoricalState)
				states[i] = state;
			else
				states[i] = i;

			if (states[i]>maxS)
				maxS = states[i];
		}
		for (int i=0; i<matrixSize; i++) {
			if (states[i]>=0){
				for (int j=0; j<matrixSize; j++) {
					if (states[j]>=0){
						String costString = ParseUtil.getToken(description, stringPos);
						if (costString != null){
							if (costString.equals("."))
								costs.setValue(states[i]*maxNumStates + states[j], 0);
							else {
								utilityNumber.setValue(costString);
								costs.setValue(states[i]*maxNumStates + states[j], utilityNumber);
							}
						}
					}
				}
			}
		}
		numStates = maxS+1;
	}

	//	HERE use saved symbols!  NOTE that if symbols change of matrix, this will cause problems, but so far Mesquite doesn't allow changing of symbols!
	public String getNexusSpecification () {
		String s= " ";
		if (!StringUtil.blank(getAnnotation()))
			s +=" [!" + getAnnotation() + "] ";

		s += numStates + StringUtil.lineEnding();
		for (int i=0; i<numStates; i++) 
			s += "\t" + getStateSymbol(states[i]);

		s+= StringUtil.lineEnding();
		for (int i=0; i<numStates; i++) {
			for (int j=0; j<numStates; j++) {
				int loc = i*maxNumStates + j;
				if (costs.isInfinite(loc))
					s+= "\ti";
				else
					s+= "\t" + costs.toString(loc);
			}
			s+=  StringUtil.lineEnding();
		}
		return s;
	}
	/** Returns cost to change from beginning to ending state.  Returned value may be undefined
	if either the begin or end states are not in list of states for which matrix defined*/
	public MesquiteNumber getTransitionValue (int beginState, int endState, MesquiteNumber result){
		if (result ==null)
			result = new MesquiteNumber();
		if (beginState>=numStates || endState>=numStates)
			result.setToInfinite();
		else
			costs.placeValue(beginState*maxNumStates+endState, result);
		return result;
	}

	public boolean isSymmetrical(){
		for (int i=0; i< numStates; i++){
			for (int j=i+1; j< numStates; j++)
				if (!costs.equal(i*maxNumStates+j, j*maxNumStates+i))
					return false;
		}
		return true;
	}

	/** Returns cost to change from beginning to ending state.  Returned value may be undefined
	if either the begin or end states are not in list of states for which matrix defined*/
	public void setTransitionValue (int beginState, int endState, MesquiteNumber result, boolean notify){
		if (beginState>=numStates || endState>=numStates)
			return;
		costs.setValue(beginState*maxNumStates+endState, result);
		if (notify){
			notifyListeners(this, new Notification(MesquiteListener.UNKNOWN),CharacterModel.class, true);
			notifyListeners(this, new Notification(MesquiteListener.UNKNOWN),CharacterModel.class, false);
		}
	}
	/** To set what are the states available for transitions*/
	public long getStatesDefined () {
		long sd = CategoricalState.compressFromList(states);
		return sd & (CategoricalState.span(0, numStates-1));
	}

	public void setWindow(CategTModelEditWindow window){
		this.window = window;
	}

	/** returns the highest state for which the values are defined*/
	public int getMaxState (){
		return CategoricalState.maximum(getStatesDefined());
	}

	public String getExplanation(){	
		return "Cost of change from state to state is given by a matrix";
	}
}

