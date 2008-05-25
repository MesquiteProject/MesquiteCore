package mesquite.io.ExportTreeSourceToNEXUS;


import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;



public class ExportTreeSourceToNEXUS extends FileInterpreterI {
	TreeSourceDefinite treeSourceTask;
	MesquiteString treeSourceName;
	MesquiteCommand tstC;
	Taxa currentTaxa = null;
	boolean suspend = false;

/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;  //make this depend on taxa reader being found?)
  	 }

	public boolean isPrerelease(){
		return true;
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
		 return (project.getNumberOfFileElements(TreeVector.class) > 0) ;
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
		return NEXTRELEASE;  
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
		treeSourceTask = (TreeSourceDefinite)hireEmployee(TreeSourceDefinite.class, "Tree Source");
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
		if (file !=null)
			suggested = file.getFileName();
		MesquiteFile f;
		if (!usePrevious){
			if (!getExportOptions(null)) {
 				treeSourceTask=null;
				return false;
			}
		}
		int numOriginalTrees = treeSourceTask.getNumberOfTrees(taxa);

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

				for (int i=0; i<numOriginalTrees; i++) {
						Tree tree =  treeSourceTask.getTree(taxa, i);
						
						if (tree !=null) {
							if (tree instanceof AdjustableTree)
								((AdjustableTree)tree).setName("tree " + (i+1));
							f.writeLine("\tTREE tree"+ (i+1)+" = "+ tree.writeTree(Tree.BY_NUMBERS));
						}
					}
				
				f.writeLine("end TREES;" + StringUtil.lineEnding());

				
				if (addendum != null)
					f.writeLine(addendum);
				f.closeWriting();
				treeSourceTask=null;
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
 		return "Exports NEXUS file with a tree block, and optionally a taxa block, based upon a source of trees." ;
   	 }
	/*.................................................................................................................*/
   	 
   	 
}
	
