package mesquite.oliver.nca.CalculateTree;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.oliver.nca.*;

/** Supplies calculated trees.  This is a coordinator; the calculations themselves are done by modules hired.*/
public class CalculateTree extends TreeSource {
	int currentTree = 0;
	TreeDistance calculatorTask;
	MesquiteString calculatorName;
	MesquiteTree tree;
	Taxa currentTaxa = null;
	MesquiteCommand stC;
	long count = 0;
	int notifyEvery = 0;

	public boolean startJob(String arguments, Object condition,	CommandRecord commandRec, boolean hiredByName) {
		calculatorTask = (TreeDistance)hireEmployee(commandRec, TreeDistance.class, "Distance Tree Calculator");
		if(calculatorTask == null)
			return sorry(commandRec, "Calculated Trees could not start because no source of distance calculations was obtained.");
		calculatorName = new MesquiteString();
		calculatorName.setValue(calculatorTask.getName());
		if(numModulesAvailable(TreeDistance.class) > 1){
			MesquiteSubmenuSpec mss = addSubmenu(null, "Tree Calculator", stC, TreeDistance.class);
			mss.setSelected(calculatorName);
		}
		return true;
	}
	/*..................................................................................*/	
	public Class getHireSubchoice(){
		return TreeDistance.class;
	}
	public void endJob(){
		if (currentTaxa!=null)
			currentTaxa.removeListener(this);
		super.endJob();
	}
	/*..................................................................................*/	
	int changes = 0;
	/** Passes which object changed*/
	public void changed (Object caller, Object obj, Notification notification, CommandRecord commandRec){
		if(Notification.appearsCosmetic(notification))
			return;
		int code = Notification.getCode(notification);
		if(obj == currentTaxa && !(code == MesquiteListener.SELECTION_CHANGED)){
			changes++;
			parametersChanged(notification, commandRec);
		}
	}
	/*..................................................................................*/	
	/** Passes which object changed*/
	public void disposing (Object obj){
		if (obj == currentTaxa){
			setHiringCommand(null);
			iQuit();
		}
	}
	/*..................................................................................*/	
	public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker){
		if(checker.compare(this.getClass(), "Sets the module calculating trees", "[name of module]", commandName, "setTreeCalculator")){
			TreeDistance temp = (TreeDistance)replaceEmployee(commandRec, TreeDistance.class, arguments, "Tree Calculator", calculatorTask);
			if(temp!=null){
				calculatorTask = temp;
				calculatorTask.setHiringCommand(stC);
				calculatorName.setValue(calculatorTask.getName());
				calculatorTask.initialize(currentTaxa, commandRec);
				if(tree!=null && tree instanceof MesquiteTree){
					((MesquiteTree)tree).removeAllSensitives();
					((MesquiteTree)tree).deassignAssociated();
					tree = null;
				}
				return temp;
			} else return super.doCommand(commandName, arguments, commandRec, checker);
		}
		return null;
	}
	/*..................................................................................*/	
	public void setPreferredTaxa(Taxa taxa) {
		if(taxa != currentTaxa){
			if (currentTaxa!=null)
				currentTaxa.removeListener(this);
			currentTaxa = taxa;
			currentTaxa.addListener(this);
		}
	}
	/*..................................................................................*/	
	public void initialize(Taxa taxa, CommandRecord commandRec) {
		setPreferredTaxa(taxa);
		if(calculatorTask!=null)
			calculatorTask.initialize(taxa, commandRec);
	}
	/*..................................................................................*/	
	public Tree getTree(Taxa taxa, int itree, CommandRecord commandRec) {
		setPreferredTaxa(taxa);
		currentTree = itree;
		if(taxa == null){
			MesquiteMessage.warnProgrammer("taxa null in getTree of CalculateTree");
			return null;
		}
		if(notifyEvery > 0 && count++ % notifyEvery == 0)
			System.out.println("   tree " + currentTree);
		tree = new MesquiteTree(taxa);
		tree = (MesquiteTree)calculatorTask.getDistanceTree(taxa, tree, currentTree, null, commandRec);
		if (tree == null)
			return null;
		((MesquiteTree)tree).setName(getTreeNameString(taxa, currentTree, commandRec));
		return tree;
	}
	/*..................................................................................*/	
	public int getNumberOfTrees(Taxa taxa, CommandRecord commandRec) {
		if(calculatorTask!=null)
			return calculatorTask.getNumberOfTrees(taxa, commandRec);
		return MesquiteInteger.infinite;
	}
	/*..................................................................................*/	
	public String getTreeNameString(Taxa taxa, int iTree, CommandRecord commandRec) {
		if (calculatorTask == null)
			return "";
		return "Tree # " + MesquiteTree.toExternal(iTree) + " calculated by " + calculatorTask.getName();
	}
	/*..................................................................................*/	
	public String getCurrentTreeNameString() {
		if (calculatorTask == null)
			return "";
		return "Tree # " + MesquiteTree.toExternal(currentTree) + " calculated by " + calculatorTask.getName();
	}
	/*..................................................................................*/	
	public String getParameters(){
		if(calculatorTask == null)
			return "";
		return "Tree calculator: " + calculatorTask.getName();
	}
	/*..................................................................................*/	
	public boolean requestPrimaryChoice(){
		return true;
	}
	/*..................................................................................*/	
	public String getName() {
		return "Calculated Trees";
	}
	public String getExplanation(){
		return "Supplies trees from calculations";
	}
}
