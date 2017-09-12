/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.stochchar.SimMatricesOnTrees;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class SimMatricesOnTrees extends CharMatrixSource {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(TreeSource.class, getName() + "  needs a source of trees.",
		"The source of trees can be selected initially or in the Tree Source submenu");
		EmployeeNeed e2 = registerEmployeeNeed(CharacterSimulator.class, getName() + "  needs a method to simulate character evolution.",
		"The method to simulate character evolution can be selected initially or in the Character Simulator submenu");
	}

	int currentDataSet=0;
	long originalSeed=System.currentTimeMillis(); //0L;
	CharacterSimulator charSimulatorTask;
	CharacterDistribution states;
	Random rng;
	MesquiteLong seed;
	TreeSource treeTask;
	int numChars = 100;
	int numMatrices = MesquiteInteger.infinite;
	MesquiteString simulatorName;
	MesquiteString treeTaskName;
	Tree tree;
	Taxa lastTaxa;	
	Object dataCondition;
	MesquiteCommand stC, ttC;
	/*.................................................................................................................*/
 	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		if (condition !=null && condition instanceof CompatibilityTest)
			condition = ((CompatibilityTest)condition).getAcceptedClass();
    	 	if (condition!=null)
    	 		charSimulatorTask= (CharacterSimulator)hireCompatibleEmployee(CharacterSimulator.class, condition, "Character Simulator");
    	 	else
    	 		charSimulatorTask= (CharacterSimulator)hireEmployee(CharacterSimulator.class, "Character Simulator");
    	 	if (charSimulatorTask == null) {
    	 		return sorry("Simulated Matrices on Trees can't start because not appropiate character simulator module was obtained");
    	 	}
		stC =  makeCommand("setCharacterSimulator",  this);
		ttC =  makeCommand("setTreeSource",  this);
		charSimulatorTask.setHiringCommand(stC);
		if (RandomBetween.askSeed && !MesquiteThread.isScripting()){
			long response = MesquiteLong.queryLong(containerOfModule(), "Seed for Matrix simulation", "Set Random Number seed for Matrix on Trees simulation:", originalSeed);
			if (MesquiteLong.isCombinable(response))
				originalSeed = response;
		}
    	 	seed = new MesquiteLong(1);
    	 	seed.setValue(originalSeed);
  		
 		rng = new Random(originalSeed);
 		simulatorName = new MesquiteString(charSimulatorTask.getName());
		if (numModulesAvailable(CharacterSimulator.class)>1){
			MesquiteSubmenuSpec mss = addSubmenu(null, "Character Simulator",stC, CharacterSimulator.class);
 			mss.setSelected(simulatorName);
 		}
 		if (!MesquiteThread.isScripting()){
			numChars = MesquiteInteger.queryInteger(containerOfModule(), "Number of characters in matrix", "Number of characters to simulate:", 100, 1, 10000);
	 		if (MesquiteInteger.isUnassigned(numChars))
	 			return false;
	 		if (!MesquiteInteger.isCombinable(numChars))
	 			numChars = 100;
 		}
  	 	if (getHiredAs() != CharMatrixObedSource.class) {
			MesquiteMenuItemSpec mm = addMenuItem(null, "Next Simulated Matrix", makeCommand("nextMatrix",  this));
			mm.setShortcut(KeyEvent.VK_RIGHT); //right
			mm = addMenuItem(null, "Previous Simulated Matrix", makeCommand("prevMatrix",  this));
			mm.setShortcut(KeyEvent.VK_LEFT); //right
 		}
 		addMenuItem( "Number of characters...",makeCommand("setNumChars",  this));
  		addMenuItem("Set Seed (Matrix simulation)...", makeCommand("setSeed",  this));
		
	 	if (MesquiteThread.isScripting())
	 		treeTask= (TreeSource)hireNamedEmployee(TreeSource.class, StringUtil.tokenize("#SimulateTree"));
    	 	if (treeTask == null)
	 		treeTask= (TreeSource)hireEmployee(TreeSource.class, "Source of trees on which to simulate character evolution for matrices");
    	 	if (treeTask == null) {
    	 		return sorry("Simulated Matrices on Trees can't start because not appropiate tree source module was obtained");
    	 	}
 		treeTaskName = new MesquiteString();
	    	treeTaskName.setValue(treeTask.getName());
		if (numModulesAvailable(TreeSource.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Tree Source", ttC);
			mss.setSelected(treeTaskName);
			mss.setList(TreeSource.class);
		}
    	 	treeTask.setHiringCommand(ttC);
    	 	
  	 	return true; 
  	 }
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
  	 public void employeeQuit(MesquiteModule m){
  	 	if (m==treeTask)
  	 		iQuit();
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
 	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
 		if (!MesquiteThread.isScripting() && (employee != treeTask || Notification.getCode(notification) != MesquiteListener.SELECTION_CHANGED))
 			super.employeeParametersChanged(employee, source, notification);
   	 }
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
   	 	Snapshot temp = new Snapshot();
  	 	if (getHiredAs() != CharMatrixObedSource.class)
  	 		temp.addLine("setMatrix " + CharacterStates.toExternal(currentDataSet));
   	 	temp.addLine("setNumChars " + numChars);
   	 	if (MesquiteInteger.isCombinable(numMatrices))
   	 		temp.addLine("setNumMatrices " + numMatrices);
 	 	temp.addLine("setCharacterSimulator ", charSimulatorTask);
  	 	temp.addLine("setSeed " + originalSeed);
  	 	temp.addLine("setTreeSource ", treeTask);
  	 	return temp;
  	 }
	/*.................................................................................................................*/
	MesquiteInteger pos = new MesquiteInteger(0);
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	  if (checker.compare(this.getClass(), "Sets the module used to simulate character evolution", "[name of module]", commandName, "setCharacterSimulator")) {
    	 		CharacterSimulator temp;
    	 		if (dataCondition==null)
    	 			temp =  (CharacterSimulator)replaceEmployee(CharacterSimulator.class, arguments, "Character Simulator", charSimulatorTask);
    	 		else
    	 			temp =  (CharacterSimulator)replaceCompatibleEmployee(CharacterSimulator.class, arguments, charSimulatorTask, dataCondition);
	    	 	if (temp!=null) {
	    	 		charSimulatorTask=  temp;
				charSimulatorTask.setHiringCommand(stC);
	    	 		simulatorName.setValue(charSimulatorTask.getName());
	    	 		seed.setValue(originalSeed);
 				if (!MesquiteThread.isScripting())
 					parametersChanged(); 
 				return charSimulatorTask;
 			}
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
    	 	else if (checker.compare(this.getClass(), "Sets the module used to supply trees for matrix simulation", "[name of module]", commandName, "setTreeSource")) {
    	 		TreeSource temp=  (TreeSource)replaceEmployee(TreeSource.class, arguments, "Source of trees on which to simulate matrices", treeTask);
	    	 	if (temp!=null) {
	    	 		treeTask = temp;
				treeTask.setHiringCommand(ttC);
	    	 		treeTaskName.setValue(treeTask.getName());
	    	 		seed.setValue(originalSeed);
 				if (!MesquiteThread.isScripting())
 					parametersChanged(); 
 			}
 			return temp;
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the number of characters simulated in each matrix", "[number of characters]", commandName, "setNumChars")) {
			int newNum= MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newNum))
				newNum = MesquiteInteger.queryInteger(containerOfModule(), "Set number of characters", "Number of characters to simulate:", numChars);
    	 		if (MesquiteInteger.isCombinable(newNum) && newNum>0 && newNum<1000000 && newNum!=numChars) {
    	 			numChars=newNum;
 				if (!MesquiteThread.isScripting())
 					parametersChanged(); 
    	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the number of matrices (if indefinite number allowed)", "[number of matrices]", commandName, "setNumMatrices")) {
			int newNum= MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newNum))
				newNum = MesquiteInteger.queryInteger(containerOfModule(), "Set number of matrices", "Number of matrices available from simulations:", numMatrices);
    	 		if (MesquiteInteger.isCombinable(newNum) && newNum>0 && newNum<1000000 && newNum!=numMatrices) {
    	 			numMatrices=newNum;
 				if (!MesquiteThread.isScripting())
 					parametersChanged(); 
    	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the block of taxa used", "[block number]", commandName, "setTaxa")) {
    	 		int setNumber = MesquiteInteger.fromFirstToken(arguments, pos);
    	 		if (lastTaxa!=null)
    	 			lastTaxa.removeListener(this);
   	 		lastTaxa = getProject().getTaxa(checker.getFile(), setNumber);
    	 		if (lastTaxa!=null)
    	 			lastTaxa.addListener(this);
    	 	}
    	 	else if (checker.compare(this.getClass(), "Returns the current tree", null, commandName, "getTree")) {
    	 		if (tree==null && lastTaxa!=null && treeTask!=null) {
				return null;
    	 		}
    	 		else 
    	 			return tree;
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
		treeTask.initialize(taxa);
		charSimulatorTask.initialize(taxa);
   	}
	/*.................................................................................................................*/
  	private MCharactersDistribution getM(Taxa taxa){
		if (treeTask == null) {
			System.out.println("Tree task null");
			return null;
		}
		else if (charSimulatorTask==null) {
			System.out.println("Simulator task null");
			return null;
		}
		else if (taxa==null){
			System.out.println("taxa null");
			return null;
		}
		
		MAdjustableDistribution matrix = null;
		Class c = charSimulatorTask.getStateClass();//getDataClass from simulator and get it to make matrix
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
		

		rng.setSeed(originalSeed);
		long rnd = originalSeed;
		for (int it = 0; it<=currentDataSet; it++)
			rnd =  rng.nextInt();
		rng.setSeed(rnd+1);
		seed.setValue(rnd + 1);//v. 1. 1, October 05: changed so as to avoid two adjacent matrices differing merely by a frameshift of random numbers
		
		tree = treeTask.getTree(taxa, currentDataSet);
		//if (tree instanceof MesquiteTree)
		//	((MesquiteTree)tree).setName("Tree # " + MesquiteTree.toExternal(currentDataSet)  + " from " + treeTask.getName());
		CharacterDistribution states = null;

		for (int ic = 0; ic<numChars; ic++) {
			states = charSimulatorTask.getSimulatedCharacter(states, tree, seed, ic); 
 	 		matrix.transferFrom(ic, states);
 	 	}
   		matrix.setName("Matrix #" + (currentDataSet)  + " simulated by " + charSimulatorTask.getName());
   		matrix.setAnnotation(accumulateParameters(" "), false);
   		matrix.setBasisTree(tree);
 
  		return matrix;
   	}
	/*.................................................................................................................*/
    	 public String getMatrixName(Taxa taxa, int ic) {
   		return "Matrix #" + CharacterStates.toExternal(ic)  + " simulated by " + charSimulatorTask.getName() + " on trees from " + treeTask.getName();
   	 }
	/*.................................................................................................................*/
  	public MCharactersDistribution getCurrentMatrix(Taxa taxa){
   		return getM(taxa);
   	}
	/*.................................................................................................................*/
  	public MCharactersDistribution getMatrix(Taxa taxa, int im){
   		currentDataSet = im;
   		return getM(taxa);
   	}
	/*.................................................................................................................*/
    	public  int getNumberOfMatrices(Taxa taxa){
    		if (treeTask ==null)
    			return numMatrices; 
    		int numTrees = treeTask.getNumberOfTrees(taxa);
    		if (MesquiteInteger.isFinite(numTrees))
    			numMatrices = numTrees;
    		return numMatrices;
    	}
	/*.................................................................................................................*/
   	/** returns the number of the current matrix*/
   	public int getNumberCurrentMatrix(){
   		return currentDataSet;
   	}
	/*.................................................................................................................*/
    	 public String getParameters() {
		String s = null;
		if (getHiredAs() != CharMatrixObedSource.class)
			s = "Matrix #" + CharacterStates.toExternal(currentDataSet) + " simulated by " + charSimulatorTask.getName() + " on trees from " + treeTask.getName();
		else
			s = "Matrix simulated by " + charSimulatorTask.getName() + " on trees from " + treeTask.getName();
		return s + " [seed for matrix sim. " + originalSeed + "]";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Simulated Matrices on Trees";
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Supplies simulated character matrices, each evolved on a different of a series of trees.";
   	 }
   	 
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
  	 public CompatibilityTest getCompatibilityTest() {
  	 	return new SMoSTCompatibilityTest();
  	 }
}

class SMoSTCompatibilityTest extends CompatibilityTest{
	public  boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer){
			return (null!=MesquiteTrunk.mesquiteModulesInfoVector.findModule (CharacterSimulator.class, obj, project, prospectiveEmployer));
	}
}

