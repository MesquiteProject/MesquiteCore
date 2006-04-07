package mesquite.geiger.lib;
import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

public abstract class TreesThroughTime extends FileAssistantT {
	protected TreeSource treeSourceTask;
	TreesThroughTimeWindow lineagesWindow;
	MesquiteString treeSourceName;
	Taxa taxa;
	MesquiteCommand tstC;
	public DrawChart charterTask;
	int numTrees = MesquiteInteger.unassigned;
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
 		charterTask = (DrawChart)hireNamedEmployee(commandRec, DrawChart.class, "Histogram");
		if (charterTask == null)
			return sorry(commandRec, getName() + " couldn't start because no charting module was obtained.");
		treeSourceTask= (TreeSource)hireEmployee(commandRec, TreeSource.class, "Source of trees (for Lineages through time)");
		if (treeSourceTask == null)
			return sorry(commandRec, getName() + " couldn't start because no source of trees obtained.");
		tstC = makeCommand("setTreeSource",  (Commandable)this);
		treeSourceTask.setHiringCommand(tstC);
 		makeMenu("Lineages");
		treeSourceName = new MesquiteString(treeSourceTask.getName());
		if (numModulesAvailable(TreeSource.class)>1){
			MesquiteSubmenuSpec mss = addSubmenu(null, "Tree Source", tstC, TreeSource.class);
			mss.setSelected(treeSourceName);
		}
		addMenuItem("Number of Trees...", makeCommand("setNumTrees",  (Commandable)this));
		
		taxa = getProject().chooseTaxa(containerOfModule(), "For which block of taxa do you want to show Lineages through time?",commandRec);
 

		if (!commandRec.scripting()) {
			if (taxa==null)
				return sorry(commandRec, getName() + " couldn't start because no block of taxa found.");
 			
			treeSourceTask.initialize(taxa, commandRec);
			numTrees = treeSourceTask.getNumberOfTrees(taxa, commandRec);
 			if (!MesquiteInteger.isCombinable(numTrees))
 				numTrees = MesquiteInteger.queryInteger(containerOfModule(), "Number of Trees", "Number of Trees", 100);
	 		lineagesWindow= new TreesThroughWindow( this, treeSourceTask, commandRec);
	 		setModuleWindow(lineagesWindow);
	 		lineagesWindow.setVisible(true);
	 		resetContainingMenuBar();
			resetAllWindowsMenus();
	 	}
 		return true;
  	 }
	 public TreesThroughTimeWindow makeWindow(TreesThroughTime ownerModule, TreeSource treeSourceTask, CommandRecord commandRec){
	 }
  	 public void employeeQuit(MesquiteModule m){
  	 	if (m == charterTask)
  	 		iQuit();
  	 }
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		if (obj instanceof Taxa && (Taxa)obj == taxa) {
			iQuit();
		}
	}
	/*.................................................................................................................*/
	/** Query module as to whether conditions are such that it will have to quit soon -- e.g. if its taxa block has been doomed.  The tree window, data window, 
	etc. override this to return true if their object is doomed. This is useful in case MesquiteListener disposing method is not called for an employer before one of its
	employees discovers that it needs to quit.  If the employer is going to quit anyway,there is no use to use auto rehire for the quit employee.*/
	public boolean quittingConditions(){
		return (taxa.isDoomed());
	}
	public void endJob(){
			if (taxa!=null)
				taxa.removeListener(this);
			super.endJob();
	}
	/*.................................................................................................................*/
 	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification, CommandRecord commandRec) {
			if ((lineagesWindow!=null) ) 
				lineagesWindow.renew(commandRec);
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
  	 	if (lineagesWindow ==null)
  	 		return null;
  	 	Snapshot fromWindow = lineagesWindow.getSnapshot(file);
    	 	Snapshot temp = new Snapshot();
  	 	
		temp.addLine("setTaxa " + getProject().getTaxaReference(taxa));
		temp.addLine("setNumTrees " + numTrees);
		temp.addLine("setTreeSource " , treeSourceTask);
  	 	temp.addLine("setNumForNodes", numForNodesTask);
  	  	temp.addLine("makeWindow");
		temp.addLine("getWindow");
		temp.addLine("tell It");
		temp.incorporate(fromWindow, true);
		temp.addLine("endTell");
		temp.addLine("showWindow");
  	 	return temp;
  	 }
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {
     	 	 if (checker.compare(this.getClass(), "Sets the taxa block", "[block reference, number, or name]", commandName, "setTaxa")){
   	 		Taxa t = getProject().getTaxa(checker.getFile(), parser.getFirstToken(arguments));
   	 		if (t!=null){
	   	 		taxa = t;
	   	 		return taxa;
   	 		}
      	 	 } 
     	 	 else if (checker.compare(this.getClass(), "Sets the module calculating the numbers for the nodes", "[name of module]", commandName, "setNumForNodes")) {
        	 		NumbersForNodes temp=  (NumbersForNodes)replaceEmployee(commandRec, NumbersForNodes.class, arguments, "Value to calculate for Lineages through time", numForNodesTask);
    	 			if (temp!=null) {
    	 				numForNodesTask= temp;
    			 		numForNodesTask.setHiringCommand(nfntC);
    	 				numberTaskName.setValue(numForNodesTask.getName());
    	 				if (!commandRec.scripting())
    	 					lineagesWindow.renew(commandRec);
    	 			}
    	 			return numForNodesTask;
        	 	}
        	 	else if (checker.compare(this.getClass(), "Sets the total number of trees", "[number]", commandName, "setNumTrees")) {
    	 		pos.setValue(0);
    	 		int s = MesquiteInteger.fromString(arguments, pos);
    	 		if (!MesquiteInteger.isCombinable(s))
    	 			s = MesquiteInteger.queryInteger(containerOfModule(), "Number of Trees", "Number of Trees", numTrees);
    	 		if (MesquiteInteger.isCombinable(s)) {
	 			numTrees = s;
				if (lineagesWindow!=null){
		 			lineagesWindow.numTrees = s;
	 				lineagesWindow.renew(commandRec);
		 		}
	 		}
    		}
    	 	else if (checker.compare(this.getClass(), "Makes but doesn't show the window", null, commandName, "makeWindow")) {
	 		if (getModuleWindow()==null) {
	 			lineagesWindow= new LineagesWindow( this, treeSourceTask, commandRec);
	 			setModuleWindow(lineagesWindow);
		 		resetContainingMenuBar();
				resetAllWindowsMenus();
	 		}
	 		return lineagesWindow;
    	 	}
    	 	else if (checker.compare(this.getClass(), "Shows the window", null, commandName, "showWindow")) {
	 		if (lineagesWindow!=null)
	 			lineagesWindow.setVisible(true);
	 		return lineagesWindow;
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the tree source", "[name of module]", commandName, "setTreeSource")) {
    	 		TreeSource temp=  (TreeSource)replaceEmployee(commandRec, TreeSource.class, arguments, "Source of trees", treeSourceTask);
    	 		if (temp!= null) {
    	 			treeSourceTask=temp;
				treeSourceTask.setHiringCommand(tstC);
				treeSourceName.setValue(treeSourceTask.getName());
    	 			if (lineagesWindow!=null)
    	 				lineagesWindow.setTreeSource(treeSourceTask, commandRec);
    	 		}
    	 		return treeSourceTask;
    	 	}
    	 	else
    	 		return super.doCommand(commandName, arguments, commandRec, checker);
    	 	
	return null;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Lineages Through Time";
   	 }
	/*.................................................................................................................*/
 	public void windowGoAway(MesquiteWindow whichWindow) {
			whichWindow.hide();
			whichWindow.dispose();
			iQuit();
	}
	/*.................................................................................................................*/
  	 public String getVersion() {
		return null;
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Displays a window showing plot of lineages through time averaged over many trees." ;
   	 }
}
	