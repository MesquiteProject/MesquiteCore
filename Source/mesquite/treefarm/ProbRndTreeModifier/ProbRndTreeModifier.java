package mesquite.treefarm.ProbRndTreeModifier;

import java.util.Random;
import mesquite.lib.*;
import mesquite.treefarm.lib.*;

/** This module is basically a random tree modifier, but it works by hiring another one, and only invoking it on a tree with a particular probability */
public class ProbRndTreeModifier extends RndTreeModifier {
	Random probModifyRNG ;
	MesquiteLong seed;
	long originalSeed=System.currentTimeMillis(); //0L;
	RndTreeModifier modifierTask;
	MesquiteString modifierName;
	MesquiteCommand stC;
	double defaultProbability = 0.5;
	MesquiteDouble prob = new MesquiteDouble(defaultProbability);
	
	public boolean startJob(String arguments, Object condition,boolean hiredByName) {
		if (arguments ==null)
			modifierTask = (RndTreeModifier)hireEmployee(RndTreeModifier.class, "Random modifier of tree");
		else {
			modifierTask = (RndTreeModifier)hireNamedEmployee(RndTreeModifier.class, arguments);
			if (modifierTask == null)
				modifierTask = (RndTreeModifier)hireEmployee(RndTreeModifier.class, "Random modifier of tree");
		}
		if (modifierTask == null) {
			return sorry(getName() + " couldn't start because no random tree modifier was obtained.");
		}
		stC = makeCommand("setModifier",  this);
		modifierTask.setHiringCommand(stC);
		modifierName = new MesquiteString();
		modifierName.setValue(modifierTask.getName());
		if (numModulesAvailable(RndTreeModifier.class)>1){
			MesquiteSubmenuSpec mss = addSubmenu(null, "Random Modifier of Tree (used by Occasionally Modify)", stC, RndTreeModifier.class);
			mss.setSelected(modifierName);
		}
  		addMenuItem("Set Probability of Random Tree Modification...", makeCommand("setProbability",  this));
	 	seed = new MesquiteLong(1);
 	 	seed.setValue(originalSeed);
  		addMenuItem("Set Seed (Occasionally Modify Tree)...", makeCommand("setSeed",  this));
  		probModifyRNG= new RandomBetween(originalSeed);
		return true;
	}

	/*.................................................................................................................*/
	public  Class getHireSubchoice(){
		return RndTreeModifier.class;
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = super.getSnapshot(file);
  	 	temp.addLine("setProbability " + prob.getValue());
  	 	temp.addLine("setSeed " + originalSeed);
  	 	temp.addLine("setModifier ", modifierTask); 
  	 	return temp;
  	 }
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the tree modifier", "[name of module]", commandName, "setModifier")) {
			RndTreeModifier temp = (RndTreeModifier)replaceEmployee(RndTreeModifier.class, arguments, "Random modifier of tree", modifierTask);
			if (temp !=null){
				modifierTask = temp;
				modifierName.setValue(modifierTask.getName());
				modifierTask.setHiringCommand(stC);
				parametersChanged();
				return modifierTask;
			}
		}
  	 	 else if (checker.compare(this.getClass(), "Sets the probability of modification", "[probability (a double)]", commandName, "setProbability")) {
	 		 double s = MesquiteDouble.fromString(parser.getFirstToken(arguments));
	 		 if (!MesquiteDouble.isCombinable(s)){
	 			 s = MesquiteDouble.queryDouble(containerOfModule(), "Random number seed", "Enter a value for the probability a tree will be randomly modified", prob.getValue());
	 		 }
	 		 if (MesquiteDouble.isCombinable(s)){
	 			 prob.setValue(s);
	 			 parametersChanged(); //?
	 		 }
	 	 }
   	 	 else	if (checker.compare(this.getClass(), "Sets the random number seed", "[long integer seed]", commandName, "setSeed")) {
	 		 long s = MesquiteLong.fromString(parser.getFirstToken(arguments));
	 		 if (!MesquiteLong.isCombinable(s)){
	 			 s = MesquiteLong.queryLong(containerOfModule(), "Random number seed", "Enter an integer value for the random number seed for probabily of randomly modifying the tree.", originalSeed);
	 		 }
	 		 if (MesquiteLong.isCombinable(s)){
	 			originalSeed = s;
	 			probModifyRNG.setSeed(originalSeed); 
	 			 parametersChanged(); //?
	 		 }
	 	 }
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	public void modifyTree(Tree tree, MesquiteTree modified, RandomBetween rng) {
		if (modifierTask==null || tree==null)
			return;
		if (probModifyRNG.nextDouble()<=prob.getValue())
			modifierTask.modifyTree(tree,modified, rng);
	}

	public String getName() {
		return "Occasionally Randomly Modify";
	}

	public String getExplanation() {
		return "With specified probabililty, will randomly modifier current tree.";
	}

}
