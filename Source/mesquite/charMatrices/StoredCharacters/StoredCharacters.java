/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.StoredCharacters;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/** Supplies characters from character matrices stored in the project.*/
public class StoredCharacters extends CharacterSource implements MesquiteListener, Selectionable {
	int currentChar=0;
	Taxa taxa=null;
	CharacterData data;
	CharacterDistribution states;
	Class dataClass = null;
	MesquiteString dataName;
	MesquiteSubmenuSpec mss;
	boolean autoBail=false;
	Object condition;
	Vector indirectListeners;
	long cmID = -1;
	/*.................................................................................................................*/
	/** condition passed to this module must be subclass of CharacterState */
	public boolean startJob(String arguments, Object condition, boolean hiredByName) { 
		dataName = new MesquiteString();
		mss = addSubmenu(null, "Stored Matrix", makeCommand("setDataSet",  this), (ListableVector)getProject().datas);
		mss.setSelected(dataName);
		//can leave a hint in terms of an id of a matrix to use
		String whichBlock = MesquiteThread.retrieveAndDeleteHint(this);
		long wB = MesquiteLong.fromString(whichBlock);
		if (MesquiteLong.isCombinable(wB)){
			cmID = wB;
		}
		if (condition!=null ) {
			setHiringCondition(condition);
			if (getProject().getNumberCharMatricesVisible(dataClass)<=0 && !MesquiteThread.isScripting()) {
				return sorry("There are no stored data matrices of the requested type available (for use by " + getEmployer().getName() + ")");
			}


		}
		else {
			if (getProject().getNumberCharMatricesVisible()<=0 && !MesquiteThread.isScripting()) {
				return sorry("There are no stored data matrices available (for use by " + getEmployer().getName() + ")");
			}
		}
		indirectListeners = new Vector();
		return true;
	}
	public void setHiringCondition(Object condition){
		data = null;
		this.condition = condition;
		if (condition instanceof Class)
			dataClass = (Class)condition;
		else if (condition instanceof CompatibilityTest)
			dataClass = ((CompatibilityTest)condition).getAcceptedClass();
		boolean filter = false;
		CharacterState cs=null;
		try {
			cs = (CharacterState)dataClass.newInstance();
			filter = true;
		}
		catch (IllegalAccessException e){alert("iae 17"); }
		catch (InstantiationException e){alert("ie 17"); }
		if (filter)
			mss.setListableFilter(cs.getCharacterDataClass());
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public CompatibilityTest getCompatibilityTest() {
		return new CSMCompatibilityTest();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		if (getProject() == null)
			return null;
		Snapshot temp = new Snapshot();
		if (taxa!=null && getProject().getNumberTaxas()>1) {
			temp.addLine("setTaxa " + getProject().getTaxaReferenceExternal(taxa));
		}
		if (data != null)
			temp.addLine("setDataSet " + getProject().getCharMatrixReferenceExternal(data));
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets which stored data matrix to use", "[matrix reference]", commandName, "setDataSet")) { //list doesn't filter by taxa but this does!!
			String dataReference =parser.getFirstToken(arguments);
			CharacterData d = getProject().getCharacterMatrixByReference(checker.getFile(), taxa, dataClass, dataReference, true);
			if (d == null)
				d = getProject().getCharacterMatrixByReference(checker.getFile(), taxa, dataClass, dataReference);
			if (d==null && CommandRecord.macro()){ //macro; at this point ask for user to choose
				int numMatrices = getNumberOfMatrices(taxa);
				if (numMatrices>1) {
					int which = queryUser(taxa, numMatrices);
					if (MesquiteInteger.isCombinable(which)) {
						d = getProject().getCharacterMatrixVisible(which);
						useMatrix(taxa, d);
						dataName.setValue(d.getName());
						currentChar = 0;
						parametersChanged();
					}
					else
						return null;
				}
				else {
					useFirstMatrix(taxa);
					if (d == null)
						return null;
					dataName.setValue(d.getName());
					currentChar = 0;
					parametersChanged();
				}
			}
			else if (d!=null && d!= data) {
				if (taxa == null)
					taxa = d.getTaxa();
				mss.setCompatibilityCheck(taxa);
				useMatrix(taxa, d);
				dataName.setValue(d.getName());
				currentChar = 0;
				parametersChanged();
				resetContainingMenuBar();
			}
			return d;
		}
		else if (checker.compare(this.getClass(), "Sets which taxa block to use", "[block reference, number, or name]", commandName, "setTaxa")) { 
			Taxa t = getProject().getTaxa(checker.getFile(), parser.getFirstToken(arguments));
			if (t!=null){
				if (taxa!=null)
					taxa.removeListener(this);
				taxa = t;
				if (taxa!=null)
					taxa.addListener(this);
				mss.setCompatibilityCheck(taxa);
				parametersChanged();
				resetContainingMenuBar();
				return taxa;
			}
			else
				MesquiteMessage.warnProgrammer("Stored characters error: taxa reference not found");
		}
		else if (checker.compare(this.getClass(), "Returns number of characters available", null, commandName, "getNumberOfCharacters")) {
			MesquiteInteger n = new MesquiteInteger();
			n.setValue(getNumberOfCharacters(taxa));
			return n;
		}
		else if (checker.compare(this.getClass(), "Sets autoBail", "[on or off]", commandName, "autoBail")) {
			autoBail = ("on".equalsIgnoreCase(arguments));
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		if (obj == data && !doomed) {
			data.removeListener(this);
			if (taxa !=null && taxa.isDoomed()) {
				iQuit();
				return;
			}
			data = null;
			dataName.setValue("No matrix is currently in use");
		/*
			discreetAlert("A character data matrix in use (for " + getEmployer().getName() + " used by \"" + getEmployer().getEmployer().getName() + "\") has been deleted.  Another matrix will be sought.");

			if (dataClass!=null) {
				if (getProject().getNumberCharMatricesVisible(taxa, dataClass)<=0) {
					iQuit();
					return;
				}
				data = getProject().getCharacterMatrixVisible(taxa, 0, dataClass);
			}
			else if (getProject().getNumberCharMatricesVisible(taxa)>0) {
				data = getProject().getCharacterMatrixVisible(taxa, 0);
			}
			else data = null;
			if (data==null){
				iQuit();
				return;
			}
			data.addListener(this);
			dataName.setValue(data.getName());
			*/
			parametersChanged();
		}
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;  //Disposal prohibition not used in Mesquite yet; once used then this should return false if currently in use
	}
	/*.................................................................................................................*/
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification);
		if (obj==data && (code == AssociableWithSpecs.SPECSSET_CHANGED || code == MesquiteListener.VALUE_CHANGED ||code == MesquiteListener.DATA_CHANGED || code == MesquiteListener.PARTS_CHANGED || code == MesquiteListener.PARTS_ADDED || code == MesquiteListener.PARTS_DELETED || code == MesquiteListener.PARTS_MOVED)){
			parametersChanged(notification);
		}
	}
	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa){
		checkMatrix(taxa, false);
	}


	private int queryUser(Taxa taxa, int numMatrices){
		if (cmID >=0){
			for (int i=0; i<numMatrices; i++) {
				CharacterData d = retrieveDataSet(taxa, i);
				if (d.getID() == cmID)
					return i;
			}
		}
		String[] names = new String[numMatrices];
		for (int i=0; i<numMatrices; i++) {
			CharacterData d = retrieveDataSet(taxa, i);
			if (d!=null)
				names[i]=d.getName() + " (" + d.getDataTypeName() + ")";
		}
		String forWhom = whatIsMyPurpose();

		if (isInStartup()){
			MesquiteInteger io = new MesquiteInteger(0);
			ListDialog id = new ListDialog(containerOfModule(), "Select data matrix", "Use characters from which matrix? \n(" + forWhom + ")", false, MesquiteString.helpString, names, io, null,true, false);
			id.completeDialog("OK",null, true,id);
			id.setVisible(true);
			id.dispose();
			return io.getValue();
		}
		else
			return ListDialog.queryList(containerOfModule(), "Select data matrix", "Use characters from which matrix? \n(" + forWhom + ")", MesquiteString.helpString,names, 0);
	}

	boolean warned = false;
	boolean compDone = false;

	private void setTaxa(Taxa taxa){
		if (this.taxa != taxa || !compDone) {
			compDone = true;
			mss.setCompatibilityCheck(taxa);
			resetContainingMenuBar();
		}
		this.taxa = taxa;
	}
	public boolean checkMatrix( Taxa taxa, boolean warn) {
		if (taxa == null && getProject().getNumberTaxas()!=1) {
			return false;
		}
		if (taxa == null)
			taxa = getProject().getTaxa(0);
		setTaxa(taxa);
		if (data==null || data.getTaxa() != taxa) {
			int numMatrices = getNumberOfMatrices(taxa);
			if (numMatrices == 0) {
				if (warn && !warned){
					discreetAlert("Sorry, there are no character matrices available for the indicated block of taxa (" + taxa.getName() + ").  Stored Characters cannot be supplied.");
					warned = true;
					return false;
				}
			}
			else if ((!MesquiteThread.isScripting())  && numMatrices>1) {
				int which = queryUser(taxa, numMatrices);
				if (MesquiteInteger.isCombinable(which)) {

					CharacterData d = retrieveDataSet(taxa, which);
					useMatrix(taxa, d);
				}
				else {
					return false;
				}
			}
			else
				useFirstMatrix(taxa);
			if (data == null)
				return false;
			return true;
		}
		else
			return true;
	}
	/*.................................................................................................................*/
	private CharacterDistribution getC(Taxa taxa) {
		if (checkMatrix( taxa, true)){
			int whichCharacter = currentChar;
			if (respectExclusion)
				whichCharacter = findIncludedCharacter(data, currentChar);
			if (whichCharacter <0 && countIncludedCharacters(data)>0) {
				MesquiteMessage.printStackTrace("Error: Character < 0 in StoredCharacters " + currentChar + " " + whichCharacter);
			}
			states = data.getCharacterDistribution(whichCharacter);
			if (states == null)
				MesquiteMessage.warnUser("Error: Data set failed to return a character distribution " + currentChar);
			return states;
		}
		else {
			return null;
		}
	}
	/*.................................................................................................................*/
	public CharacterDistribution getCharacter(Taxa taxa, int ic) {
		//CommandRecord.tick("Getting stored character " + (ic+1));
		currentChar=ic;
		return getC(taxa);
	}

	/*.................................................................................................................*/
	/*.................................................................................................................*/
	boolean respectExclusion = true;
	public int countIncludedCharacters(CharacterData data){
		if (data==null)
			return 0;
		if (!respectExclusion)
			return data.getNumChars();
		CharInclusionSet incl = (CharInclusionSet)data.getCurrentSpecsSet(CharInclusionSet.class);
		int numChars = data.getNumChars();
		if (incl==null)
			return numChars;
		int count = 0;
		for (int i=0; i<numChars; i++)
			if (incl.isBitOn(i))
				count++;
		return count;

	}
	public int findIncludedCharacter(CharacterData data, int ic){
		if (!respectExclusion)
			return ic;
		CharInclusionSet incl = (CharInclusionSet)data.getCurrentSpecsSet(CharInclusionSet.class);
		int numChars = data.getNumChars();
		if (incl==null)
			return ic;
		int count = 0;
		for (int i=0; i<numChars; i++){
			if (incl.isBitOn(i)){
				if (count == ic)
					return i;
				count++;
			}
		}
		return -1;

	}
	public Selectionable getSelectionable(){
		return this;  //data or this
	}
	/** Sets whether or not the part is selected */
	public void setSelected(int part, boolean select){
		if (data == null)
			return;
		int ic = findIncludedCharacter(data, part);
		if (ic>=0)
			data.setSelected(ic, select);
	}

	/** Returns whether the part is selected */
	public boolean getSelected(int part){
		if (data == null)
			return false;
		int ic = findIncludedCharacter(data, part);
		if (ic>=0)
			return data.getSelected(ic);
		return false;
	}

	/** Deselects all parts */
	public void deselectAll(){
		if (data == null)
			return;
		data.deselectAll();
	}

	/** Selects all parts */
	public void selectAll(){
		if (data == null)
			return;
		data.selectAll();
	}



	/** Returns whether there are any selected parts */
	public boolean anySelected(){
		if (data == null)
			return false;
		return data.anySelected();
	}

	/** Returns number of selected parts */
	public int numberSelected(){
		if (data == null)
			return 0;

		CharInclusionSet incl = (CharInclusionSet)data.getCurrentSpecsSet(CharInclusionSet.class);
		int numChars = data.getNumChars();
		if (incl==null){
			return data.numberSelected();
		}
		int count = 0;
		for (int i=0; i<numChars; i++)
			if (incl.isBitOn(i) && data.getSelected(i)) //included and selected
				count++;
		return count;
	}

	/** Returns number of parts that can be selected*/
	public int getNumberOfSelectableParts(){
		if (data == null)
			return 0;
		return countIncludedCharacters(data);
	}
	private void removeIndirectListeners(CharacterData d){
		for (int i=0; i<indirectListeners.size(); i++)
			data.removeListener((MesquiteListener)indirectListeners.elementAt(i));
		indirectListeners.removeAllElements();
	}
	/*.................................................................................................................*/
	/** lists listeners of element*/
	public boolean amIListening(MesquiteListener obj) {
		if (data == null)
			return false;
		return data.amIListening(obj);
	}
	/*.................................................................................................................*/
	/** lists listeners of element*/
	public void listListeners() {
		if (data == null)
			return;
		data.listListeners();
	}
	/*.................................................................................................................*/

	/** notifies listeners that element has changed*/
	public void notifyListeners(Object caller, Notification notification){
		if (data == null)
			return;
		data.notifyListeners(caller, notification);
	}
	/** adds a listener to notify if the element changes*/
	public void addListener(MesquiteListener listener){
		if (data == null)
			return;
		if (indirectListeners.indexOf(listener)<0)
			indirectListeners.addElement(listener);
		data.addListener(listener);
	}
	/** adds a listener to notify if the element changes; add to start of listener vector so it will be notified early*/
	public void addListenerHighPriority(MesquiteListener listener){
		if (data == null)
			return;
		if (indirectListeners.indexOf(listener)<0)
			indirectListeners.addElement(listener);
		data.addListenerHighPriority(listener);
	}
	/** removes a listener*/
	public void removeListener(MesquiteListener listener){
		if (data == null)
			return;
		indirectListeners.removeElement(listener);
		data.removeListener(listener);
	}
	/** Increments the suppression of listener notification*/
	public void incrementNotifySuppress(){
		if (data == null)
			return;
		data.incrementNotifySuppress();
	}
	/** Decrements the suppression of listener notification*/
	public void decrementNotifySuppress(){
		if (data == null)
			return;
		data.decrementNotifySuppress();
	}
	/*.................................................................................................................*/
	public int getNumberOfCharacters(Taxa taxa) {
		if (checkMatrix( taxa, false)) {
			if (respectExclusion)
				return countIncludedCharacters(data);
			return data.getNumChars();
		}
		else
			return 0;
	}

	/*.................................................................................................................*/
	/** returns the name of character ic*/
	public String getCharacterName(Taxa taxa, int ic){
		if (checkMatrix( taxa, false)) {
			if (data==null)
				return null;
			int whichCharacter = ic;
			if (respectExclusion)
				whichCharacter = findIncludedCharacter(data, ic);
			return data.getCharacterName(whichCharacter);  
		}
		return null;
	}
	/*.................................................................................................................*/
	private  int getNumberOfMatrices(Taxa taxa){
		if (getProject() == null)
			return 0;
		else
			if (dataClass == null) {
				return getProject().getNumberCharMatricesVisible(taxa); 
			}
			else {
				return getProject().getNumberCharMatricesVisible(taxa, dataClass); 
			}
	}
	/*.................................................................................................................*/
	private  CharacterData retrieveDataSet(Taxa taxa, int index){
		if (getNumberOfMatrices(taxa)==0 && autoBail){
			iQuit();
			return null;
		}
		return getProject().getCharacterMatrixVisible(taxa, index, dataClass);
	}

	/*.................................................................................................................*/
	private  void useFirstMatrix(Taxa taxa){
		setTaxa(taxa);
		CharacterData tempData = retrieveDataSet(taxa, 0);
		if (data!=tempData && tempData!=null) {
			if (data!=null) 
				data.removeListener(this);
			removeIndirectListeners(data);
			data = tempData;
			dataName.setValue(data.getName());
			data.addListener(this);
		}
	}
	private void useMatrix(Taxa taxa, CharacterData tempData){
		setTaxa(taxa);
		if (data!=tempData && tempData!=null) {
			if (data!=null) 
				data.removeListener(this);
			removeIndirectListeners(data);
			data = tempData;
			dataName.setValue(data.getName());
			data.addListener(this);
		}
	}
	CharacterPartition colorSet;
	public void setEnableWeights(boolean enable){
	}
	public boolean itemsHaveWeights(Taxa taxa){
		return false;
	}
	public double getItemWeight(Taxa taxa, int ic){
		return MesquiteDouble.unassigned;
	}
	/*.................................................................................................................*/
	public void prepareItemColors(Taxa taxa){
		if (taxa==null || data == null || data.getTaxa()!=taxa)
			colorSet=null;
		else
			colorSet = (CharacterPartition)data.getCurrentSpecsSet(CharacterPartition.class); 
	}
	public Color getItemColor(Taxa taxa, int ic){
		if (taxa==null || data == null || data.getTaxa()!=taxa)
			return null;
		
		//DRM 9 Feb 2013   Added this section so that charts were colored correctly
		int whichCharacter = ic;
		if (respectExclusion)
			whichCharacter = findIncludedCharacter(data, ic);
		if (whichCharacter <0 && countIncludedCharacters(data)>0) {
			MesquiteMessage.printStackTrace("Error: Character < 0 in StoredCharacters " + currentChar + " " + whichCharacter);
		}
		//DRM end
		if (colorSet!=null){
			CharactersGroup mi = (CharactersGroup)colorSet.getProperty(whichCharacter);
			if (mi!=null && mi.getColor()!=null) {
				return mi.getColor();
			}
			return null;
		}
		return data.getDefaultCharacterColor(whichCharacter);
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Stored Characters";  
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}

	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Supplies characters from data files (as opposed to simulated characters, for example)." ;
	}
	/*.................................................................................................................*/
	public boolean showCitation() {
		return true;
	}
	/*.................................................................................................................*/
	/** returns current parameters, for logging etc..*/
	public String getParameters() {
		if (getProject() == null)
			return "";
		String s = "Characters stored in: " + getProject().getName();
		if (data!=null) {
			s +=  " (current matrix: " + data.getName() + ")";
			if (getHiredAs() == CharacterOneSource.class)
				s +=  "; current character: " + data.getCharacterName(currentChar);
			s += ")";
		}
		return s;
	}
	/*.................................................................................................................*/
	/** returns current parameters, for logging etc..*/
	public String getNameAndParameters() {
		if (data!=null) {
			String s =  "Character matrix: " + data.getName();
			if (getHiredAs() == CharacterOneSource.class)
				s +=  "; current character: " + data.getCharacterName(currentChar);
			return s;
		}
		String s = "Characters stored in: " + getProject().getName();
		return s;
	}

	public void endJob() {
		if (data!=null) data.removeListener(this);
		//if (taxa != null) taxa.removeListener(this);
		super.endJob();
	}

}

class CSMCompatibilityTest extends CompatibilityTest{
//	should find out if available matrices of chosen sort
	public  boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer){
		return isCompatible(obj, project, prospectiveEmployer, null);
	}
	public  boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer, MesquiteString report){
		if (obj == null) {
			boolean can = (project == null || project.getNumberCharMatricesVisible()>0);
			if (!can && report != null)
				report.setValue("there are no character matrices stored in the data file or project");
			return can;
		}
		if (!(obj instanceof Class) || !(CharacterState.class.isAssignableFrom((Class)obj)))
			return true;
		if (project==null)
			return true;
		else {
			boolean  matricesExist = project.getNumberCharMatricesVisible((Class)obj)>0;
			if (!matricesExist && report != null)
				report.setValue("there are no appropriate character matrices stored in the data file or project");
			return matricesExist; //still not perfect, since data set might apply to other taxa
		}
	}
}

