/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.mb.TreesFromMrBayes;
/*~~  */



import mesquite.lib.*;
import mesquite.lib.duties.*;

public class TreesFromMrBayes extends TreeSource implements MesquiteListener {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(mesquite.trees.ManyTreesFromFile.ManyTreesFromFile.class, getName() + "  needs a source of trees.",
		"The source of trees is arranged initially");
	}
	TreeSource treeSource;
	MesquiteFile file = null;
	LongArray filePos;
	DoubleArray scores;
	MesquiteBoolean showLast;
	int highestSuccessfulTree = -1;
	int burnInIgnore = 0;
	double burnInFraction = 0.0;
	boolean ignoreEPC = false;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		filePos = new LongArray(100, 100);
		scores = new DoubleArray(100, 100);
		if (!MesquiteThread.isScripting()) 
			alert("\"" + getName() + "\" is a special source of trees that reads from a MrBayes trees file.  Please select the \".t\" file from MrBayes.  For the MrBayes scores to be used subsequently, the associated .p file with parameters and scores MUST be in the same directory as the .t file and otherwise with the same name.");
		treeSource = (TreeSource)hireNamedEmployee(TreeSource.class, "#ManyTreesFromFile");
		if (treeSource==null)
			return sorry(getName() + " couldn't start because no source of trees was obtained.");		
		ignoreEPC = true;
		treeSource.doCommand("toggleReread", "off", CommandChecker.defaultChecker);
		ignoreEPC = false;
		showLast = new MesquiteBoolean(false);
		addMenuItem("Ignore MCMC Burn-in...", makeCommand("ignoreBurnIn",  this));
		addCheckMenuItem( null, "Show Last MCMC Tree Only", makeCommand("toggleShowLast",  this), showLast);
		if (!MesquiteThread.isScripting()){
			int newBurn= MesquiteInteger.queryInteger(containerOfModule(), "Number of trees to ignore as burn-in", "Ignore:", burnInIgnore);
			if (newBurn>=0 && MesquiteInteger.isCombinable(newBurn)) 
				burnInIgnore = newBurn;
		}
		return true;
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		if (this.file == null)
			return null;
		Snapshot temp = new Snapshot();
		temp.addLine("ignoreBurnIn " + burnInIgnore);  
		temp.addLine("toggleShowLast " + showLast.toOffOnString());
		return temp;
	}
	/*.................................................................................................................*
 	public boolean queryBurnin() {
 		MesquiteInteger buttonPressed = new MesquiteInteger(1);
 		ExtensibleDialog queryFilesDialog = new ExtensibleDialog(containerOfModule(), "Burn-in Options",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
 		queryFilesDialog.addLabel("MrBayes Trees Burn-in");

 		IntegerField burnInIgnoreField = queryFilesDialog.addIntegerField("Number of trees to ignore as burn-in", burnInIgnore, 8, 0, MesquiteInteger.infinite);
		DoubleField burnInFractionField = queryFilesDialog.addDoubleField("Fraction of trees to ignore as burn-in", burnInFraction, 8, 0.0, 1.0);

 		queryFilesDialog.addLabel("(Mesquite will ignore whichever number of trees is largest.)");

 		queryFilesDialog.completeAndShowDialog(true);
 		if (buttonPressed.getValue()==0)  {
 			burnInIgnore = burnInIgnoreField.getValue();
 			burnInFraction = burnInFractionField.getValue();
 		}
 		queryFilesDialog.dispose();
 		return (buttonPressed.getValue()==0);
 	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets how many trees in burn-in to ignore", "[number of saved trees to ignore]", commandName, "ignoreBurnIn")) {
			int newBurn = MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newBurn))
				newBurn= MesquiteInteger.queryInteger(containerOfModule(), "Number of trees to ignore as burn-in", "Ignore:", burnInIgnore);
			if (newBurn>=0 && MesquiteInteger.isCombinable(newBurn)) {
				burnInIgnore = newBurn;
				parametersChanged();
			}


		}
		else if (checker.compare(this.getClass(), "Sets whether or not to show just the last tree", "[on or off]", commandName, "toggleShowLast")) {
			boolean current = showLast.getValue();
			showLast.toggleValue(parser.getFirstToken(arguments));
			if (current!=showLast.getValue())
				parametersChanged();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public void endJob(){
		if (file !=null){
			file.closeReading();
			file.dispose();
		}
		super.endJob();
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
	public void setPreferredTaxa(Taxa taxa) {
		if (treeSource!=null)
			treeSource.setPreferredTaxa(taxa);
	}
	/*.................................................................................................................*/
	public void initialize(Taxa taxa) {
		if (treeSource!=null)
			treeSource.initialize(taxa);
		scoreFileNotReadable = false;

	}

	/*.................................................................................................................*/
	double getCachedScore( int itree){
		return scores.getValue(itree);
	}

	/*.................................................................................................................*/
	void goToFilePosition(long pos){
		file.goToFilePosition(pos);

	}
	int getHighIndex(){
		int high = -1;
		double score = 0;
		while (MesquiteDouble.isCombinable(score))
			score = getCachedScore(++high);
		return --high;
	}
	MesquiteInteger pos = new MesquiteInteger();
	boolean scoreFileNotReadable = false;

	/*.................................................................................................................*/
	double getScore(int itree){
		//if score is cached, then get it
		double score = getCachedScore( itree);
		if (MesquiteDouble.isCombinable(score)){
			return score;
		}
		if (scoreFileNotReadable)
			return MesquiteDouble.unassigned;
		long prevPosition = 0;
		//initiate file if needed
		if (file == null) {
			String path = ((PathHolder)treeSource).getFilePath();
			StringBuffer scoresPath = new StringBuffer(path);
			if (!StringUtil.getLastItem(path, ".", MesquiteFile.fileSeparator).equalsIgnoreCase("t")) {
				logln("\nWARNING: MrBayes tree file does not end in \".t\"; the score file may be unreadable.\n");
			}
			scoresPath.setCharAt(scoresPath.length()-1, 'p');
			if (MesquiteFile.fileExists(scoresPath.toString()))
				file = MesquiteFile.open(true, scoresPath.toString());
			if (file==null) {
				scoreFileNotReadable = true;
				return MesquiteDouble.unassigned;
			}
			file.openReading();
			file.readLine();
			file.readLine();//skipping first two lines
			prevPosition = file.getFilePosition();
			filePos.setValue(0, prevPosition);
		}
		int highIndex = getHighIndex();	
		if (highIndex>=0) {
			long highPos = filePos.getValue(highIndex);
			file.goToFilePosition(highPos);
			file.readLine();//skipping old line
			prevPosition = file.getFilePosition();
		}
		else {
			file.goToFilePosition(0);
			file.readLine();
			file.readLine();//skipping first two lines
			prevPosition = file.getFilePosition();
			filePos.setValue(0, prevPosition);
		}
		score = 0;
		for (int i=highIndex+1; i<=itree && MesquiteDouble.isCombinable(score); i++){
			String line = file.readLine();
			pos.setValue(0);
			int tree = MesquiteInteger.fromString(line, pos);
			double thisScore = MesquiteDouble.fromString(line, pos);
			scores.setValue(i, thisScore);
			filePos.setValue(i, prevPosition);
			prevPosition = file.getFilePosition();

		}

		return getCachedScore( itree);
	}
	/*.................................................................................................................*/
	public Tree getTree(Taxa taxa, int itree) {
		if (showLast.getValue())
			return getLastTree(taxa);
		if (treeSource == null)
			return null;
		Tree t = treeSource.getTree(taxa, itree+burnInIgnore);
		if (t!= null && highestSuccessfulTree<itree)
			highestSuccessfulTree = itree;
		if (t !=null && t instanceof Attachable){
			double d = getScore(itree + burnInIgnore);
			if (MesquiteDouble.isCombinable(d)){
				MesquiteDouble s = new MesquiteDouble(-d);
				s.setName("MrBayesScore");
				((Attachable)t).attachIfUniqueName(s);
			}
		}
		return t;
	}
	/*.................................................................................................................*/

	public Tree getLastTree(Taxa taxa){
		int i = highestSuccessfulTree+1;
		Tree tree = null;
		Tree t = null;

		while((tree = treeSource.getTree(taxa, i + burnInIgnore))!=null){
			t = tree;
			highestSuccessfulTree = i;
			i++;
		}
		if (t != null && t instanceof Attachable){
			double d = getScore(highestSuccessfulTree + burnInIgnore);
			if (MesquiteDouble.isCombinable(d)){
				MesquiteDouble s = new MesquiteDouble(-d);
				s.setName("MrBayesScore");
				((Attachable)t).attachIfUniqueName(s);
			}
		}
		return t;
	}
	/*.................................................................................................................*/
	public int getNumberOfTrees(Taxa taxa) {
		if (showLast.getValue())
			return 1;
		if (treeSource == null)
			return 0;
		int nt = treeSource.getNumberOfTrees(taxa); 
		if (MesquiteInteger.isCombinable(nt))
			return nt-burnInIgnore;
		else
			return nt;
	}
	/*.................................................................................................................*/
	public int getNumberOfTrees(Taxa taxa, boolean determineNumberIfFinite) {
		if (showLast.getValue())
			return 1;
		if (treeSource == null)
			return 0;
		int nt = treeSource.getNumberOfTrees(taxa, determineNumberIfFinite); 
		if (MesquiteInteger.isCombinable(nt))
			return nt-burnInIgnore;
		else
			return nt;
	}

	/*.................................................................................................................*/
	public String getTreeNameString(Taxa taxa, int itree) {
		if (treeSource == null)
			return null;
		if (showLast.getValue()){
			if (itree > 0)
				return null;
			getLastTree(taxa);
			return treeSource.getTreeNameString(taxa, highestSuccessfulTree + burnInIgnore);
		}
		return treeSource.getTreeNameString(taxa, itree + burnInIgnore);
	}
	/*.................................................................................................................*/
	public String getName() {
		return "MrBayes Trees from .t File";
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Supplies trees from a \".t\" file produced by MrBayes";
	}
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (ignoreEPC)
			return;
		if (Notification.getCode(notification) != MesquiteListener.ITEMS_ADDED)
			highestSuccessfulTree = -1;
		if (showLast.getValue() && Notification.getCode(notification) == MesquiteListener.ITEMS_ADDED) {
			parametersChanged(new Notification(MesquiteListener.PARTS_CHANGED));
		}
		else	
			super.employeeParametersChanged(employee, source, notification);
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 110;  
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "MrBayes Trees from .t File...";
	}

}


