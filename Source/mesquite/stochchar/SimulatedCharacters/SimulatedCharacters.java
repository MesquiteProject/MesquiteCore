/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.stochchar.SimulatedCharacters;
/*~~  */

import java.util.Random;

import mesquite.lib.CommandChecker;
import mesquite.lib.CommandRecord;
import mesquite.lib.CompatibilityTest;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.EmployerEmployee;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteLong;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteThread;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.RandomBetween;
import mesquite.lib.Snapshot;
import mesquite.lib.characters.CharacterDistribution;
import mesquite.lib.characters.CharacterStates;
import mesquite.lib.duties.CharacterSimulator;
import mesquite.lib.duties.CharacterSource;
import mesquite.lib.duties.OneTreeSource;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.Tree;
import mesquite.lib.ui.MesquiteSubmenuSpec;

/* ======================================================================== */
public class SimulatedCharacters extends CharacterSource {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(CharacterSimulator.class, getName() + "  needs a particular method to simulate character evolution.",
		"The method to simulate character evolution can be selected initially or in the Character Simulator submenu");
		EmployeeNeed e2 = registerEmployeeNeed(OneTreeSource.class, getName() + "  needs a current tree.",
		"The source of current tree is arranged initially");
	}
	/*.................................................................................................................*/
	int currentChar=0;
	long originalSeed=System.currentTimeMillis(); //0L;
	CharacterSimulator simulatorTask;
	CharacterDistribution states;
	Random rng;
	MesquiteLong seed;
	OneTreeSource treeTask;
	MesquiteString simulatorName;
	Tree lastTree;
	Taxa lastTaxa;	
	Object dataCondition;
	MesquiteCommand stC;
	/*.................................................................................................................*/  
 	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		if (condition !=null && condition instanceof CompatibilityTest)
			condition = ((CompatibilityTest)condition).getAcceptedClass();
    	 	if (condition!=null)
    	 		simulatorTask= (CharacterSimulator)hireCompatibleEmployee(CharacterSimulator.class, condition, "Character Simulator");
    	 	else
    	 		simulatorTask= (CharacterSimulator)hireEmployee(CharacterSimulator.class, "Character Simulator");
    	 	if (simulatorTask == null) {
    	 		return sorry("Simulated Characters could not be started because an appropriate simulator module could not be hired");
    	 	}
    	 	stC = makeCommand("setCharacterSimulator",  this);
    	 	simulatorTask.setHiringCommand(stC);
    	 	
		if (RandomBetween.askSeed && !MesquiteThread.isScripting()){
			long response = MesquiteLong.queryLong(containerOfModule(), "Seed for character simulation", "Set Random Number seed for character simulation:", originalSeed);
			if (MesquiteLong.isCombinable(response))
				originalSeed = response;
		}
    	 	seed = new MesquiteLong(1);

    	 	seed.setValue(originalSeed);
 		dataCondition = condition; 
 		simulatorName = new MesquiteString(simulatorTask.getName());
		if (numCompatibleModulesAvailable(CharacterSimulator.class, condition, this)>1){
			MesquiteSubmenuSpec mss = addSubmenu(null, "Character Simulator",stC, CharacterSimulator.class);
 			mss.setSelected(simulatorName);
 			mss.setCompatibilityCheck(condition);
 		}
 		
 		rng = new Random(originalSeed);
 		treeTask = (OneTreeSource)hireEmployee(OneTreeSource.class, "Source of tree on which to simulate character evolution");
 		if (treeTask == null)  {
 			return sorry("Simulated Characters could not be started because no tree was found on which to simulate");
 		}
  		addMenuItem("Set Seed (Character simulation)...", makeCommand("setSeed",  this));
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
	/** Generated by an employee who quit.  The MesquiteModule should act accordingly. */
 	public void employeeQuit(MesquiteModule employee) {
 		if (employee == simulatorTask || employee == treeTask)
 			iQuit();
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		if (obj instanceof Taxa && (Taxa)obj == lastTaxa) {
			iQuit();
		}
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
   	 	Snapshot temp = new Snapshot();
   	 	temp.addLine("setSeed " + originalSeed);
 	 	temp.addLine("setCharacterSimulator ", simulatorTask);
 	 	temp.addLine("getTreeSource ", treeTask);
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    		 if (checker.compare(this.getClass(), "Sets the module used to simulate character evolution", "[name of module]", commandName, "setCharacterSimulator")) {
    	 		CharacterSimulator temp;
    	 		if (dataCondition==null)
    	 			temp =  (CharacterSimulator)replaceEmployee(CharacterSimulator.class, arguments, "Character Simulator", simulatorTask);
    	 		else
    	 			temp =  (CharacterSimulator)replaceCompatibleEmployee(CharacterSimulator.class, arguments, simulatorTask, dataCondition);
	    	 	if (temp!=null) {
	    	 		simulatorTask=  temp;
		    	 	simulatorTask.setHiringCommand(stC);
	    	 		simulatorName.setValue(simulatorTask.getName());
	    	 		states = null;
	    	 		seed.setValue(originalSeed);
 				if (!MesquiteThread.isScripting())
 					parametersChanged(); //?
 				return simulatorTask;
 			}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Returns the source of trees", null, commandName, "getTreeSource")) {
    	 		return treeTask;
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the bock of taxa used", "[block number]", commandName, "setTaxa")) {
    	 		int setNumber = MesquiteInteger.fromFirstToken(arguments, stringPos); //TODO: should use just first token
    	 		if (lastTaxa!=null)
    	 			lastTaxa.removeListener(this);
   	 		lastTaxa = getProject().getTaxa(checker.getFile(), setNumber);
    	 		if (lastTaxa!=null)
    	 			lastTaxa.addListener(this);
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
    	 	else if (checker.compare(this.getClass(), "Returns the current tree", null, commandName, "getTree")) {
    	 		if (lastTree==null && lastTaxa!=null && treeTask!=null) {
				return treeTask.getTree(lastTaxa);
    	 		}
    	 		else 
    	 			return lastTree;
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(Taxa taxa){
		if (simulatorTask.isDoomed())
				return;
		treeTask.initialize(taxa);
		simulatorTask.initialize(taxa);
   	}
   	
   	private long charSeed(int ic){
		rng.setSeed(originalSeed);

		long rnd = originalSeed;
		for (int i = 0; i<ic; i++)
			rnd =  rng.nextInt();
		rng.setSeed(rnd+1);
		return rnd + 1; //v. 1. 1, October 05: changed so as to avoid two adjacent characters differing merely by a frameshift of random numbers
   	}
	/*.................................................................................................................*/
   	public CharacterDistribution getCharacter(Taxa taxa, int ic) {
   		lastTaxa = taxa;
   		if (ic<0)
   			return null;
   		currentChar =ic;
		if (treeTask == null) {
			System.out.println("Tree task null");
			return null;
		}
		else if (simulatorTask==null) {
			System.out.println("Simulator task null");
			return null;
		}
		if (simulatorTask.isDoomed())
				return null;
   		CommandRecord.tick("Simulating character " + ic);

		seed.setValue(charSeed(currentChar));
		lastTree = treeTask.getTree(taxa);
		CharacterDistribution states = simulatorTask.getSimulatedCharacter(null, lastTree, seed, ic); //1. 06: had passed states back in, which caused problems for employers requiring multiple
		if (simulatorTask.isDoomed())
				return null;
   		if (states!=null)
   			states.setName("Character #" + Integer.toString(CharacterStates.toExternal(currentChar))  + " simulated by " + simulatorTask.getName());
  		return states;
   	}
	/*.................................................................................................................*/
   	public int getNumberOfCharacters(Taxa taxa) {
   		return MesquiteInteger.infinite;
   	}
   
	/*.................................................................................................................*/
   	/** returns the name of character ic*/
   	public String getCharacterName(Taxa taxa, int ic){
     		return "Character #" + Integer.toString(CharacterStates.toExternal(ic))  + " simulated by " + simulatorTask.getName() + " (seed: " + charSeed(ic) + ")";
   	}
	/*.................................................................................................................*/
    	 public String getParameters() {
			if (simulatorTask !=null) {
				String s =  "Simulator: " + simulatorTask.getName();
				if (lastTree !=null)
					s+= "; most recent tree: " + lastTree.getName();
				return s + " (seed for char sim. " + originalSeed + ")";
			}
			else
				return "";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Simulated Characters on Tree";
   	 }
   	 
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Supplies simulated characters.";
   	 }
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
  	 public CompatibilityTest getCompatibilityTest() {
  	 	return new SCCompatibilityTest();
  	 }
}

class SCCompatibilityTest extends CompatibilityTest{
	public  boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer){
			return (null!=MesquiteTrunk.mesquiteModulesInfoVector.findModule (CharacterSimulator.class, obj, project, prospectiveEmployer));
	}
}


