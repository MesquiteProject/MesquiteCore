/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.assoc.ExportAssociation;


import mesquite.assoc.lib.AssociationSource;
import mesquite.assoc.lib.TaxaAssociation;
import mesquite.lib.Arguments;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteStringBuffer;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Parser;
import mesquite.lib.StringUtil;
import mesquite.lib.duties.FileInterpreterI;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.Taxon;
import mesquite.lib.tree.TreeVector;



public class ExportAssociation extends FileInterpreterI {
	AssociationSource associationTask;
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
		return true;
	}
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public String preferredDataFileExtension() {
		return "dat";
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
	String fileName = "Association.dat";

	public boolean getExportOptions(TreeVector trees){
		/*MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExporterDialog exportDialog = new ExporterDialog(this,containerOfModule(), "Export tree file for DELINEATE", buttonPressed);
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
		*/
		return true;
	}	
	/*.................................................................................................................*/
	/** Called to provoke any necessary initialization.  This helps prevent the module's initialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa){
		setPreferredTaxa(taxa);
		if (associationTask==null) {
			associationTask = (AssociationSource)hireEmployee(AssociationSource.class, "Source of taxon associations");
			if (associationTask == null) 
				return;
		}

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



	/*.................................................................................................................*/
	private String prepareExportName(String name) {
		return StringUtil.tokenize(name);
	}
	/*.................................................................................................................*/
	public boolean exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export
		Arguments args = new Arguments(new Parser(arguments), true);
		boolean usePrevious = args.parameterExists("usePrevious");

		Taxa taxa = 	 getProject().chooseTaxa(getModuleWindow(), "Choose taxa to be listed first in each row:"); 
		if (taxa==null) 
			return false;
		if (taxa!=currentTaxa || associationTask==null) {
			initialize(taxa);
			if (associationTask==null)
				return false;
		}


		if (!MesquiteThread.isScripting() && !usePrevious)
			if (!getExportOptions(null)) {
				associationTask=null;
				return false;
			}

		
		TaxaAssociation association = associationTask.getCurrentAssociation(taxa);
		if (association==null)
			return false;
		MesquiteStringBuffer outputBuffer = new MesquiteStringBuffer(100);


		for (int it=0; it<taxa.getNumTaxa(); it++) {
			Taxon taxon = taxa.getTaxon(it);
			Taxon[] associatedTaxa = association.getAssociates(taxon);
			for (int j=0; j<associatedTaxa.length; j++) {
				outputBuffer.append(prepareExportName(taxon.getName())+"\t" +prepareExportName(associatedTaxa[j].getName())+"\n");
			}
			
		}
		outputBuffer.append("\n\n");

		
		associationTask = null;
		
		
		if (outputBuffer!=null) {
			saveExportedFileWithExtension(outputBuffer, arguments, preferredDataFileExtension());
			return true;
		}

		return false;
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Export Current Taxa Association";
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		String s =  "Exports file containing a list of taxa in one taxa block (e.g., specimens) and the taxa in another block (e.g., populations) with which they are currently associated.  ";
		return s;
	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}

}

