/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.opentree.ExportTreeForOpenTree;


import java.awt.Checkbox;

import mesquite.lib.Arguments;
import mesquite.lib.DoubleArray;
import mesquite.lib.ExporterDialog;
import mesquite.lib.Listable;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteString;
import mesquite.lib.NameReference;
import mesquite.lib.Parser;
import mesquite.lib.StringUtil;
import mesquite.lib.duties.FileInterpreterI;
import mesquite.lib.duties.TaxaManager;
import mesquite.lib.duties.TreeSource;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.AdjustableTree;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeVector;
import mesquite.lib.ui.ListDialog;
import mesquite.lib.ui.ProgressIndicator;



public class ExportTreeForOpenTree extends FileInterpreterI {
	TreeSource treeSourceTask;
	MesquiteString treeSourceName;
	MesquiteCommand tstC;
	Taxa currentTaxa = null;
	boolean suspend = false;

	/*.................................................................................................................*/
	public boolean loadModule(){
		return true;
	}

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;  //make this depend on taxa reader being found?)
	}

	public boolean isPrerelease(){
		return false;
	}
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public String preferredDataFileExtension() {
		return "nex";
	}
	/*.................................................................................................................*/
	public boolean canExportEver() {  
		return true;  //
	}
	/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		return true;
		//return (project.getNumberOfFileElements(TreeVector.class) > 0) ;
	}

	/*.................................................................................................................*/
	public boolean canExportData(Class dataClass) {  
		return false;
	}
	/*.................................................................................................................*/
	public boolean canImport() {  
		return false;
	}

	/*.................................................................................................................*/
	public void readFile(MesquiteProject mf, MesquiteFile file, String arguments) {
	}


	/* ============================  exporting ============================*/
	/*.................................................................................................................*/
	boolean includeTaxaBlock = true;
	boolean convertToBranchLengths = true;
	String fileName = "untitledTrees.nex";

	public boolean getExportOptions(TreeVector trees){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExporterDialog exportDialog = new ExporterDialog(this,containerOfModule(), "Export tree file for Open Tree", buttonPressed);
		String helpString = "This will save a tree file ready to be uploaded into Open Tree (opentreeoflife.org).  It will optionally convert node values "+
		"such as consensus frequences as branch lengths (as that is how Open Tree imports support values for branches).";
		exportDialog.appendToHelpString(helpString);
		exportDialog.setSuppressLineEndQuery(true);
		exportDialog.setDefaultButton(null);
		Checkbox convertToBranchLengthsBox = exportDialog.addCheckBox("convert node values to branch lengths", convertToBranchLengths);

		exportDialog.completeAndShowDialog();

		boolean ok = (exportDialog.query()==0);

		convertToBranchLengths = convertToBranchLengthsBox.getState();

		exportDialog.dispose();
		return ok;
	}	
	/*.................................................................................................................*/
	/** Called to provoke any necessary initialization.  This helps prevent the module's initialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa){
		setPreferredTaxa(taxa);
		treeSourceTask = (TreeSource)hireEmployee(TreeSource.class, "Tree Source");
		if (treeSourceTask == null)
			return;
	}
	public void endJob(){
		if (currentTaxa!=null)
			currentTaxa.removeListener(this);
		storePreferences();
		super.endJob();
	}
	/*.................................................................................................................*/
	/** passes which object changed*/
	public void disposing(Object obj){
		if (obj == currentTaxa) {
			setHiringCommand(null); //since there is no rehiring
			iQuit();
		}
	}

	/*.................................................................................................................*/

	public void setPreferredTaxa(Taxa taxa){
		if (taxa !=currentTaxa) {
			if (currentTaxa!=null)
				currentTaxa.removeListener(this);
			currentTaxa = taxa;
			currentTaxa.addListener(this);
		}

	}

	/** Get the translation table as a string. */
	public String getTranslationTable(Taxa taxa) {
		if (taxa == null)
			return null;
		StringBuffer sb = new StringBuffer();
		sb.append("\tTRANSLATE" + StringUtil.lineEnding());
		for(int i=0; i<taxa.getNumTaxa(); i++) {
			if (i>0)
				sb.append(","+ StringUtil.lineEnding());
			String thisLabel = "" + (i+1);
			sb.append("\t\t" + thisLabel + " " + StringUtil.tokenize(taxa.getTaxonName(i))) ;
		}
		sb.append(";"+ StringUtil.lineEnding());

		return sb.toString();
	}

	/*.................................................................................................................*/
	public   void visitNodes(int node, AdjustableTree tree, NameReference nr) {
		double value = tree.getAssociatedDouble(nr,node);
		if (MesquiteDouble.isCombinable(value))
			tree.setBranchLength(node, value, false);
		else
			tree.setBranchLength(node, MesquiteDouble.unassigned, false);
		if (tree instanceof MesquiteTree)
			((MesquiteTree)tree).setAssociatedDouble(nr, node, MesquiteDouble.unassigned);
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) 
			visitNodes(d, tree, nr);
	}
	/*.................................................................................................................*/
	public boolean transformTree(AdjustableTree tree){
		if (tree == null)
			return false;
		ListableVector v = new ListableVector();
		int num = tree.getNumberAssociatedDoubles();
		if (num==1) {
			DoubleArray da = tree.getAssociatedDoubles(0);
			NameReference nr = NameReference.getNameReference(da.getName());
			visitNodes(tree.getRoot(), tree, nr);
			return true;
		} else if (num>1){
			boolean[] shown = new boolean[num]; //bigger than needed probably
			for (int i = 0; i< num; i++){
				DoubleArray da = tree.getAssociatedDoubles(i);
				if (da != null)
					v.addElement(new MesquiteString(da.getName(), ""), false);
			}
			Listable result = ListDialog.queryList(containerOfModule(), "Choose attached value", "Choose attached value to transfer to branch lengths", null, v, 0);
			if (result != null){
				MesquiteString name = (MesquiteString)result;
				String sName = name.getName();
				NameReference nr = NameReference.getNameReference(sName);

				visitNodes(tree.getRoot(), tree, nr);

				return true;
			}
		}
		return false;

	}

	/*.................................................................................................................*/
	public boolean exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export
		Arguments args = new Arguments(new Parser(arguments), true);
		boolean usePrevious = args.parameterExists("usePrevious");

		Taxa taxa = 	 getProject().chooseTaxa(getModuleWindow(), "Choose taxa block"); 
		if (taxa==null) 
			return false;

		if (treeSourceTask==null || taxa!=currentTaxa) {
			initialize(taxa);
			if (treeSourceTask==null)
				return false;
			treeSourceTask.initialize(currentTaxa);
		}


		MesquiteString dir = new MesquiteString();
		MesquiteString fn = new MesquiteString();
		String suggested = fileName;
		if (file !=null) {
			suggested = file.getFileName();
			if (StringUtil.getLastItem(suggested, ".").equalsIgnoreCase("NEX")) {
				suggested = StringUtil.getAllButLastItem(suggested, ".") +".trees.nex";
			} else if (StringUtil.getLastItem(suggested, ".").equalsIgnoreCase("NXS")) {
				suggested = StringUtil.getAllButLastItem(suggested, ".") +".trees.nxs";
			} else
				suggested = suggested +".trees.nex";
		}

		MesquiteFile f;
		if (!usePrevious){
			if (!getExportOptions(null)) {
				treeSourceTask=null;
				return false;
			}
		}
		int numOriginalTrees = treeSourceTask.getNumberOfTrees(taxa);
		if (!treeSourceTask.hasLimitedTrees(taxa)){
			numOriginalTrees = MesquiteInteger.queryInteger(containerOfModule(), "Number of Trees", "Number of trees to export", 100, 1, 99999, true); 
			if (!MesquiteInteger.isCombinable(numOriginalTrees))
				return false;
		}

		String path = getPathForExport(arguments, suggested, dir, fn);
		if (path != null) {
			f = MesquiteFile.newFile(dir.getValue(), fn.getValue());
			if (f !=null){
				f.openWriting(true);
				f.writeLine("#NEXUS" + StringUtil.lineEnding());
				if (includeTaxaBlock)
					f.writeLine(((TaxaManager)findElementManager(Taxa.class)).getTaxaBlock(taxa, null, file));

				f.writeLine("begin TREES;" + StringUtil.lineEnding());
				f.writeLine(getTranslationTable(taxa) + StringUtil.lineEnding());
				int count=0;
				String name;

				ProgressIndicator progIndicator = null;
				if (MesquiteInteger.isCombinable(numOriginalTrees))
					progIndicator = new ProgressIndicator(getProject(),getName(), "Processing trees", numOriginalTrees, true);
				else 
					progIndicator = new ProgressIndicator(getProject(),getName(), "Processing trees", 1, true);
				if (progIndicator!=null){
					progIndicator.setButtonMode(ProgressIndicator.OFFER_CONTINUE);
					progIndicator.setOfferContinueMessageString("Are you sure you want to stop the export?");
					progIndicator.start();
				}
				boolean done = false;
				for (int i=0; i<numOriginalTrees && !done; i++) {
					Tree tree =  treeSourceTask.getTree(taxa, i);
					tree = tree.cloneTree();  // copy it so that we can modify it as we want.
					if (convertToBranchLengths && tree instanceof AdjustableTree) {
						transformTree((AdjustableTree)tree);
					}

					if (progIndicator!=null) {
						if (progIndicator.isAborted()) {
							progIndicator.goAway();
							break;
						}
						progIndicator.setText("Tree " + (i+1));
						if (MesquiteInteger.isCombinable(numOriginalTrees))
							progIndicator.setCurrentValue(i+1);
						else
							progIndicator.spin();
					}
					if (tree !=null) {
						name = tree.getName();
						if (StringUtil.notEmpty(name))
							name = StringUtil.tokenize(name);
						if (StringUtil.blank(name))
							name = "tree " + (i+1);
						f.writeLine("\tTREE "+ name +" = "+ tree.writeTree(Tree.BY_NUMBERS));
						count++;
						if (count %100 == 0)
							logln("   Writing tree " + count);
					}
					else
						done = true;
				}
				if (progIndicator != null)
					progIndicator.goAway();

				f.writeLine("end;" + StringUtil.lineEnding());


				f.closeWriting();
				treeSourceTask=null;
				logln("    " + count + " trees written");
				return true;
			}
		}
		treeSourceTask=null;
		return false;
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Export for OpenTree";
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		String s =  "Exports NEXUS file with a taxa and tree block for OpenTree based upon a source of trees.  ";
		return s;
	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 360;  
	}

}

