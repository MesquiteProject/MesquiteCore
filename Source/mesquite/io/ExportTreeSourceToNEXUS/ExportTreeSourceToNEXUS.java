/* Mesquite source code.  Copyright 1997-2008 W. Maddison and D. Maddison.
Version 2.5, June 2008.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.io.ExportTreeSourceToNEXUS;


import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;



public class ExportTreeSourceToNEXUS extends FileInterpreterI {
	TreeSource treeSourceTask;
	MesquiteString treeSourceName;
	MesquiteCommand tstC;
	Taxa currentTaxa = null;
	boolean suspend = false;

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
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 250;  
	}


	/* ============================  exporting ============================*/
	/*.................................................................................................................*/
	boolean includeTaxaBlock = false;
	String fileName = "untitledTrees.nex";
	String addendum = "";

	public boolean getExportOptions(TreeVector trees){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExporterDialog exportDialog = new ExporterDialog(this,containerOfModule(), "Export NEXUS Trees file", buttonPressed);
		exportDialog.setSuppressLineEndQuery(true);
		exportDialog.setDefaultButton(null);
		Checkbox itCheckBox = exportDialog.addCheckBox("Include Taxa Block", includeTaxaBlock);
		exportDialog.addLabel("Addendum: ");

		addendum = "";

		TextArea fsText =exportDialog.addTextAreaSmallFont(addendum,16);

		exportDialog.completeAndShowDialog();

		boolean ok = (exportDialog.query()==0);

		includeTaxaBlock = itCheckBox.getState();

		addendum = fsText.getText();
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
		if (numOriginalTrees == MesquiteInteger.infinite){
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
				for (int i=0; i<numOriginalTrees; i++) {
					Tree tree =  treeSourceTask.getTree(taxa, i);

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
				}
				if (progIndicator != null)
					progIndicator.goAway();

				f.writeLine("end;" + StringUtil.lineEnding());


				if (addendum != null)
					f.writeLine(addendum);
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
		return "Export NEXUS Tree File from Tree Source";
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		String s =  "Exports NEXUS file with a tree block, and optionally a taxa block, based upon a source of trees.  " +
		"One major advantage of this is that it allows a collection of trees to a file without having them all in memory at once. " +
		"For example, if the Tree Source is \"Transform Trees From Other Source\", and the other source is "+
		"\"Use Trees from Separate NEXUS file\", then Mesquite will read in a tree from the other NEXUS file, transform the  tree,"+
		" and wrte it to the file, one at a tme." ;
		return s;
	}
	/*.................................................................................................................*/


}

