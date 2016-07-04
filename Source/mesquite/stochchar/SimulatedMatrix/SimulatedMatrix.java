/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.stochchar.SimulatedMatrix;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class SimulatedMatrix extends CharMatrixSource implements Incrementable {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(CharacterSimulator.class, getName() + "  needs a particular method to simulate character evolution.",
		"The method to simulate character evolution can be selected initially or in the Character Simulator submenu");
		EmployeeNeed e2 = registerEmployeeNeed(OneTreeSource.class, getName() + "  needs a current tree on which to simulate character evolution.",
		"The source of a current tree is arranged initially");
	}
	/*.................................................................................................................*/
	int currentDataSet=0;
	long originalSeed=System.currentTimeMillis(); //0L;
	CharacterSimulator simulatorTask;
	Random rng;
	MesquiteLong seed;
	OneTreeSource treeTask;
	int numChars = 100;
	int numMatrices = MesquiteInteger.infinite;
	MesquiteString simulatorName;
	Tree lastTree;
	Taxa lastTaxa;	
	Object dataCondition;
	MesquiteCommand stC;
	boolean initialized = false;
	boolean numCharsSet = false;
	/*.................................................................................................................*/
 	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		if (condition !=null && condition instanceof CompatibilityTest)
			condition = ((CompatibilityTest)condition).getAcceptedClass();
    	 	if (condition!=null)
    	 		simulatorTask= (CharacterSimulator)hireCompatibleEmployee(CharacterSimulator.class, condition, "Character simulator");
    	 	else
    	 		simulatorTask= (CharacterSimulator)hireEmployee(CharacterSimulator.class, "Character simulator");
    	 	if (simulatorTask == null) {
    	 		return sorry("Simulated Matrices could not start because no appropriate simulator module could be obtained");
    	 	}
    	 	stC = makeCommand("setCharacterSimulator",  this);
    	 	simulatorTask.setHiringCommand(stC);
		dataCondition = condition;
		if (RandomBetween.askSeed && !MesquiteThread.isScripting()){
			long response = MesquiteLong.queryLong(containerOfModule(), "Seed for Matrix simulation", "Set Random Number seed for Matrix simulation:", originalSeed);
			if (MesquiteLong.isCombinable(response))
				originalSeed = response;
		}
    	 	seed = new MesquiteLong(1);
    	 	seed.setValue(originalSeed);
 		simulatorName = new MesquiteString(simulatorTask.getName());
		if (numCompatibleModulesAvailable(CharacterSimulator.class, condition, this)>1){
			MesquiteSubmenuSpec mss = addSubmenu(null, "Character Simulator", stC, CharacterSimulator.class);
 			mss.setSelected(simulatorName);
 			mss.setCompatibilityCheck(condition);
 		}
  	 	if (getHiredAs() != CharMatrixObedSource.class) {
			MesquiteMenuItemSpec mm = addMenuItem(null, "Next Simulated Matrix", makeCommand("nextMatrix",  this));
			mm.setShortcut(KeyEvent.VK_RIGHT); //right
			mm = addMenuItem(null, "Previous Simulated Matrix", makeCommand("prevMatrix",  this));
			mm.setShortcut(KeyEvent.VK_LEFT); //right
 		}
 		addMenuItem( "Number of characters...",makeCommand("setNumChars",  this));
 		
  		addMenuItem("Set Seed (Matrix simulation)...", makeCommand("setSeed",  this));
 		rng = new Random(originalSeed);
 		treeTask = (OneTreeSource)hireEmployee(OneTreeSource.class, "Source of tree on which to simulate character evolution");
 		if (treeTask == null) {
 			return sorry("Simulated Matrices could not start because no source of trees was obtained");
 		}
  	 	return true; 
  	 }
  	 
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public void endJob(){
			if (lastTaxa!=null)
				lastTaxa.removeListener(this);
	  	 storePreferences();
			super.endJob();
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		if (obj instanceof Taxa && (Taxa)obj == lastTaxa) {
			iQuit();
		}
	}
	/*.................................................................................................................*/
	/** Generated by an employee who quit.  The MesquiteModule should act accordingly. */
 	public void employeeQuit(MesquiteModule employee) {
 		if (employee == simulatorTask)
 			iQuit();
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
   	 	Snapshot temp = new Snapshot();
  	 	if (getHiredAs() != CharMatrixObedSource.class)
  	 		temp.addLine("setMatrix " + CharacterStates.toExternal(currentDataSet));
   	 	temp.addLine("setNumChars " + numChars);
   	 	if (MesquiteInteger.isCombinable(numMatrices))
   	 		temp.addLine("setNumMatrices " + numMatrices);
 	 	temp.addLine("setCharacterSimulator ", simulatorTask);
  	 	temp.addLine("setSeed " + originalSeed);
 	 	temp.addLine("getTreeSource ", treeTask);
  	 	return temp;
  	 }
	/*.................................................................................................................*/
	MesquiteInteger pos = new MesquiteInteger(0);
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets the module used to simulate character evolution", "[name of module]", commandName, "setCharacterSimulator")) {
   	 		CharacterSimulator temp;
    	 		if (dataCondition==null)
    	 			temp =  (CharacterSimulator)replaceEmployee(CharacterSimulator.class, arguments, "Character simulator", simulatorTask);
    	 		else
    	 			temp =  (CharacterSimulator)replaceCompatibleEmployee(CharacterSimulator.class, arguments, simulatorTask, dataCondition);
	    	 	if (temp!=null) {
	    	 		simulatorTask=  temp;
		    	 	simulatorTask.setHiringCommand(stC);
	    	 		simulatorName.setValue(simulatorTask.getName());
	    	 		seed.setValue(originalSeed);
 				parametersChanged(); //?
 				return simulatorTask;
 			}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Returns the source of trees", null, commandName, "getTreeSource")) {
    	 		return treeTask;
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the number of characters simulated in each matrix", "[number of characters]", commandName, "setNumChars")) {
			int newNum= MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newNum))
				newNum = MesquiteInteger.queryInteger(containerOfModule(), "Set number of characters", "Number of characters to simulate:", numChars);
    	 		if (MesquiteInteger.isCombinable(newNum) && newNum>0 && newNum<1000000 && newNum!=numChars) {
    	 			numChars=newNum;
    				numCharsSet = true;
    	 			parametersChanged();
    	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the number of matrices to be simulated", "[number of matrices]", commandName, "setNumMatrices")) {
			int newNum= MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newNum))
				newNum = MesquiteInteger.queryInteger(containerOfModule(), "Set number of matrices", "Number of matrices available from simulations:", numMatrices);
    	 		if (MesquiteInteger.isCombinable(newNum) && newNum>0 && newNum<1000000 && newNum!=numMatrices) {
    	 			numMatrices=newNum;
    	 			parametersChanged();
    	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the taxa block used", "[block number]", commandName, "setTaxa")) {
    	 		int setNumber = MesquiteInteger.fromFirstToken(arguments, pos); //TODO: should use just first token
    	 		if (lastTaxa!=null)
    	 			lastTaxa.removeListener(this);
   	 		lastTaxa = getProject().getTaxa(checker.getFile(), setNumber);
    	 		if (lastTaxa!=null)
    	 			lastTaxa.addListener(this);
    	 	}
    	 	else if (checker.compare(this.getClass(), "Returns the current tree", null, commandName, "getTree")) {
    	 		if (lastTree==null && lastTaxa!=null && treeTask!=null) {
				return treeTask.getTree(lastTaxa,"This will be the tree on which character evolution is simulated");
    	 		}
    	 		else 
    	 			return lastTree;
    	 	}
     	 	else if (checker.compare(this.getClass(), "Sets the random number seed to that passed", "[long integer seed]", commandName, "setSeed")) {
    	 		long s = MesquiteLong.fromString(parser.getFirstToken(arguments));
    	 		if (!MesquiteLong.isCombinable(s)){
    	 			s = MesquiteLong.queryLong(containerOfModule(), "Random number seed", "Enter an integer value for the random number seed for character evolution simulation", originalSeed);
    	 		}
    	 		if (MesquiteLong.isCombinable(s)){
    	 			originalSeed = s;
	    	 		seed.setValue(originalSeed);
 				parametersChanged(); //?
 			}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Simulates the next matrix", null, commandName, "nextMatrix")) {
    	 		currentDataSet++;
    	 		parametersChanged();
    	 	}
    	 	else if (checker.compare(this.getClass(), "Simulates the previous matrix", null, commandName, "prevMatrix")) {
    	 		currentDataSet--;
    	 		if (currentDataSet>=0) {
    	 			parametersChanged();
    	 		}
    	 		else
    	 			currentDataSet=0;
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets which character matrix to simulate", "[matrix number]", commandName, "setMatrix")) {
    	 		pos.setValue(0);
    	 		int icNum = MesquiteInteger.fromString(arguments, pos);
    	 		if (!MesquiteInteger.isCombinable(icNum))
    	 			return null;
    	 		int ic = CharacterStates.toInternal(icNum);
    	 		if ((ic>=0) && (MesquiteInteger.isCombinable(ic))) {
    	 			currentDataSet = ic;
    	 			parametersChanged();
 			}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
   	public void initialize(Taxa taxa){
		if (simulatorTask.isDoomed())
				return;
		initialized = true;
		treeTask.initialize(taxa, "This will be the tree on which character evolution is simulated");
		simulatorTask.initialize(taxa);
		if (!numCharsSet){
			numChars = simulatorTask.getMaximumNumChars(taxa); //not quite right; should have separate maxnum
	 		if (!MesquiteThread.isScripting() && !MesquiteInteger.isCombinable(numChars)){
	 		
				int defaultNumChars = simulatorTask.getDefaultNumChars();
				numChars = MesquiteInteger.queryInteger(containerOfModule(), "Number of characters in matrix", "Number of characters to simulate:", defaultNumChars, 1, 1000000, false);

		 		if (!MesquiteInteger.isCombinable(numChars))
		 			numChars = defaultNumChars;
		 		if (!MesquiteInteger.isCombinable(numChars))
		 			numChars = 100;
	 		}
	 		else if (!MesquiteInteger.isCombinable(numChars))
	 			numChars = 100;
 		}
   	}
   	int countt = 0;
	/*.................................................................................................................*/
  	private MCharactersDistribution getM(Taxa taxa){
		if (treeTask == null) {
			System.out.println("Tree task null");
			return null;
		}
		else if (simulatorTask==null) {
			System.out.println("Simulator task null");
			return null;
		}
		else if (taxa==null){
			System.out.println("taxa null");
			return null;
		}
		else if (simulatorTask.isDoomed()) {
			System.out.println("sim task doomed " + simulatorTask);
				return null;
		}
		if (!initialized)
			initialize(taxa);
		MAdjustableDistribution matrix = null;
		Class c = simulatorTask.getStateClass();//getDataClass from simulator and get it to make matrix
		if (c==null)
			return null;
		try {
			CharacterState s = (CharacterState)c.newInstance();
			if (s!=null) {
				matrix = s.makeMCharactersDistribution(taxa, numChars, taxa.getNumTaxa());
				if (matrix == null)
					return null;
			}
		}
		catch (IllegalAccessException e){alert("iae getM"); return null; }
		catch (InstantiationException e){alert("ie getM");  return null;}
		
		Tree tree = treeTask.getTree(taxa, "This will be the tree on which character evolution is simulated");
		lastTree = tree;
		CharacterDistribution states = null;
		
		rng.setSeed(originalSeed);
		long rnd = originalSeed;
		for (int it = 0; it<=currentDataSet; it++)
			rnd =  rng.nextInt();
		rng.setSeed(rnd+1);
		seed.setValue(rnd + 1); //v. 1. 1, October 05: changed so as to avoid two adjacent matrices differing merely by a frameshift of random numbers
		
		for (int ic = 0; ic<numChars; ic++) {
			if (simulatorTask.isDoomed()) {
				return null;
			}
			//TODO: getSimulatedCharacter should be passed scripting and should be initializable
			if (states!=null && states instanceof AdjustableDistribution)
				((AdjustableDistribution)states).setParentCharacter(ic);
			states = simulatorTask.getSimulatedCharacter(states, tree, seed); 
 	 		matrix.transferFrom(ic, states);
 	 	}
   		matrix.setName("Matrix #" + CharacterStates.toExternal(currentDataSet)  + " simulated by " + simulatorTask.getName());
   		matrix.setAnnotation(accumulateParameters(" "), false);
   		matrix.setBasisTree(tree);
  		return matrix;
   	}
	/*.................................................................................................................*/
    	 public String getMatrixName(Taxa taxa, int ic) {
   		return "Matrix #" + CharacterStates.toExternal(ic)  + " simulated by " + simulatorTask.getName();
   	 }
	/*.................................................................................................................*/
  	public MCharactersDistribution getCurrentMatrix(Taxa taxa){
   		return getM(taxa);
   	}
	/*.................................................................................................................*/
  	public MCharactersDistribution getMatrix(Taxa taxa, int im){
   		CommandRecord.tick("Simulating matrix " + im);
   		currentDataSet = im;
   		return getM(taxa);
   	}
	/*.................................................................................................................*/
    	public  int getNumberOfMatrices(Taxa taxa){
    		return numMatrices; 
    	}
	/*.................................................................................................................*/
   	/** returns the number of the current matrix*/
   	public int getNumberCurrentMatrix(){
   		return currentDataSet;
   	}
	/*.................................................................................................................*/
 	public void setCurrent(long i){  //SHOULD NOT notify (e.g., parametersChanged)
 		currentDataSet = (int)i;
 	}
 	public long getCurrent(){
 		return currentDataSet;
 	}
 	public long getMin(){
		return 0;
 	}
 	public long getMax(){
		return numMatrices;
 	}
 	public long toInternal(long i){
 		return i-1;
 	}
 	public long toExternal(long i){ //return whether 0 based or 1 based counting
 		return i+1;
 	}
	/*.................................................................................................................*/
    	 public String getParameters() {
			if (simulatorTask !=null) {
				String s =  "";
				if (getHiredAs() != CharMatrixObedSource.class)
					s += "Matrix #" + CharacterStates.toExternal(currentDataSet) + "; "; 
				s += "Simulator: " + simulatorTask.getName();
				if (lastTree !=null)
					s+= "; most recent tree: " + lastTree.getName();
				
				return s + " [seed for matrix sim. " + originalSeed + "]";
			}
			else
				return "";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Simulated Matrices on Current Tree";
   	 }
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Supplies simulated character matrices.";
   	 }
   	 
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
  	 public CompatibilityTest getCompatibilityTest() {
  	 	return new SMCompatibilityTest();
  	 }
}

class SMCompatibilityTest extends CompatibilityTest{
	public  boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer){
			return (null!=MesquiteTrunk.mesquiteModulesInfoVector.findModule (CharacterSimulator.class, obj, project, prospectiveEmployer));
	}
}

