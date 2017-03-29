/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.stochchar.SimAncestralStates;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class SimAncestralStates extends CharHistorySource {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(CharacterSimulator.class, getName() + "  needs a particular method to simulate character evolution.",
		"The method to simulate character evolution can be selected initially or in the Character Simulator submenu");
	}
	/*.................................................................................................................*/
	int currentChar=0;
	long originalSeed=System.currentTimeMillis(); //0L;
	CharacterSimulator simulatorTask;
	CharacterHistory states;
	Random rng;
	MesquiteLong seed;
	MesquiteString simulatorName;
	Tree lastTree;
	Taxa lastTaxa;	
	Object dataCondition;
	MesquiteCommand stC;

	/*.................................................................................................................*/  
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (condition !=null && condition instanceof CompatibilityTest)
			condition = ((CompatibilityTest)condition).getAcceptedClass();
		if (condition!=null)
			simulatorTask= (CharacterSimulator)hireCompatibleEmployee(CharacterSimulator.class, condition, "Character Simulator");
		else {
			simulatorTask= (CharacterSimulator)hireEmployee(CharacterSimulator.class, "Character Simulator");
		}
		if (simulatorTask == null) {
			return sorry("Simulated Characters module could not be started because an appropriate simulator module could not be hired");
		}
		stC = makeCommand("setCharacterSimulator",  this);
		simulatorTask.setHiringCommand(stC);
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
		addMenuItem("Set Seed (Anc. States simulation)...", makeCommand("setSeed",  this));
		return true; 
	}
	public boolean isPrerelease(){
		return false;
	}
	public boolean allowsStateWeightChoice(){
		return false;
	}
	public String getMappingTypeName(){
		return "Mapping";
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setCharacterSimulator ", simulatorTask);
		temp.addLine("setSeed " + originalSeed);
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
				seed.setValue(originalSeed);
				parametersChanged(); 
				return simulatorTask;
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
				parametersChanged(); 
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	/** Generated by an employee who quit.  The MesquiteModule should act accordingly. */
	public void employeeQuit(MesquiteModule employee) {
		if (employee == simulatorTask)
			iQuit();
	}
	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa){
		if (simulatorTask.isDoomed())
			return;
		simulatorTask.initialize(taxa);
	}
	Tree tree = null;
	public  void prepareHistory(Tree tree, int ic){
		this.tree = tree;
		if (ic<0)
			return;
		currentChar =ic;
	}
	/*.................................................................................................................*/
	public CharacterHistory getMapping(long im, CharacterHistory history, MesquiteString resultString) {
		if (currentChar<0 || tree == null){
			System.out.println("current char " + currentChar);
			return null;
		}
		if (simulatorTask==null) {
			System.out.println("Simulator task null");
			return null;
		}
		if (simulatorTask.isDoomed())
			return null;
		rng.setSeed(originalSeed);

		long rnd = originalSeed;
		for (int it = 0; it<currentChar; it++)
			rnd =  rng.nextInt();
		rng.setSeed(rnd+1);
		seed.setValue(rnd + 1); //v. 1. 1, October 05: changed so as to avoid two adjacent characteres differing merely by a frameshift of random numbers

		history = simulatorTask.getSimulatedHistory(states, tree, seed);
		if (simulatorTask.isDoomed())
			return null;
		if (history!=null)
			history.setName(getHistoryName(tree, currentChar));
		if (resultString!=null)
			resultString.setValue(getHistoryName(tree, currentChar));
		return history;
	}
	/** returns the name of history ic*/
	public String getHistoryName(Taxa taxa, int ic){
		return getHistoryName((Tree)null, ic);
	}
	/** returns the name of history ic*/
	public String getHistoryName(Tree tree, int ic){
		return "Character History #" + Integer.toString(CharacterStates.toExternal(ic))  + " simulated by " + simulatorTask.getName();
	}
	public int getNumberOfHistories(Tree tree){
		return MesquiteInteger.infinite;
	}
	public int getNumberOfHistories(Taxa taxa){
		return MesquiteInteger.infinite;
	}
	public long getNumberOfMappings(Tree tree,  int ic){
		return 1;
	}
	public long getNumberOfMappings(Taxa taxa,  int ic){
		return 1;
	}
	/** returns the name of history ic and mapping im*/
	public String getMappingName(Taxa taxa, int ic, long im){
		return getHistoryName(taxa, ic);
	}
	/** returns the name of history ic and mapping im*/
	public String getMappingName(Tree tree, int ic, long im){
		return getHistoryName(tree, ic);
	}
	/** returns the name of histories for menu items, e.g. if each history represents a character, return "Character"*/
	public  String getHistoryTypeName(){
		return "Character";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Simulate Ancestral States";
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	/*.................................................................................................................*/
	public String getParameters() {
		if (simulatorTask !=null) {
			String s =  "Simulator: " + simulatorTask.getName();
			if (lastTree !=null)
				s+= "; most recent tree: " + lastTree.getName();
			return s + " [seed " + originalSeed + "]";
		}
		else
			return "";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Simulates ancestral states on the nodes of a tree.";
	}
}


