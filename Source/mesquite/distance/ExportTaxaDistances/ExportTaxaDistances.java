/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.distance.ExportTaxaDistances;


import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.distance.lib.*;



public class ExportTaxaDistances extends FileInterpreterI {
	TaxaDistanceSource distanceTask;
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
		return "txt";
	}
	/*.................................................................................................................*/
	public boolean canExportEver() {  
		return true;  //
	}
	/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		return true;
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
		return 260;  
	}


	/* ============================  exporting ============================*/
	/*.................................................................................................................*/
	String fileName = "distances.txt";
	String addendum = "";
/*
	public boolean getExportOptions(Taxa taxa){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExporterDialog exportDialog = new ExporterDialog(this,containerOfModule(), "Export distance matrix", buttonPressed);
		exportDialog.setSuppressLineEndQuery(true);
		exportDialog.setDefaultButton(null);
		exportDialog.addLargeOrSmallTextLabel("Distances are to be exported from:\n\n" + distanceTask.getNameAndParameters());
/ *		Checkbox itCheckBox = exportDialog.addCheckBox("Include Taxa Block", includeTaxaBlock);
		exportDialog.addLabel("Addendum: ");

		addendum = "";

		TextArea fsText =exportDialog.addTextAreaSmallFont(addendum,16);
* /
		exportDialog.completeAndShowDialog();

		boolean ok = (exportDialog.query()==0);


		exportDialog.dispose();
		return ok;
	}	
	/*.................................................................................................................*/
	/** Called to provoke any necessary initialization.  This helps prevent the module's initialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa){
		setPreferredTaxa(taxa);
		distanceTask = (TaxaDistanceSource)hireEmployee(TaxaDistanceSource.class, "Source of Distances among Taxa");
		if (distanceTask == null)
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
			currentTaxa = null;
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
	public boolean exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export
		Arguments args = new Arguments(new Parser(arguments), true);

		Taxa taxa = 	 getProject().chooseTaxa(getModuleWindow(), "Choose taxa block"); 
		if (taxa==null) 
			return false;

		if (distanceTask==null || taxa!=currentTaxa) {
			initialize(taxa);
			if (distanceTask==null)
				return false;
			distanceTask.initialize(currentTaxa);
		}


		MesquiteString dir = new MesquiteString();
		MesquiteString fn = new MesquiteString();
		String suggested = fileName;
		if (file !=null) {
			suggested = file.getFileName();
			if (StringUtil.getLastItem(suggested, ".").equalsIgnoreCase("nex")) {
				suggested = StringUtil.getAllButLastItem(suggested, ".") +".distances.txt";
			}
			else	if (StringUtil.getLastItem(suggested, ".").equalsIgnoreCase("nxs")) {
				suggested = StringUtil.getAllButLastItem(suggested, ".") +".distances.txt";
			}else
				suggested = suggested +".distances.txt";
		}

		MesquiteFile f;
		/*if (!usePrevious){
			if (!getExportOptions(null)) {
				distanceTask=null;
				return false;
			}
		}
		*/
		if (!distanceTask.getDistanceOptions()) {
			fireEmployee(distanceTask);
			distanceTask=null;
			return false;
		}
		TaxaDistance distances = distanceTask.getTaxaDistance(taxa);
		if (distances == null){
			return false;
		}
		String path = getPathForExport(arguments, suggested, dir, fn);
		if (path != null) {
			f = MesquiteFile.newFile(dir.getValue(), fn.getValue());
			if (f !=null){
				f.openWriting(true);
				StringBuffer line = new StringBuffer(1000);
				for (int it = 0; it<taxa.getNumTaxa(); it++){
					line.setLength(0);
					line.append(taxa.getTaxonName(it));
					for (int it2 = 0; it2<taxa.getNumTaxa(); it2++){
						line.append('\t');
						line.append(MesquiteDouble.toString(distances.getDistance(it, it2)));
					}
					f.writeLine(line.toString());
				}
				f.closeWriting();
				fireEmployee(distanceTask);
				distanceTask=null;
				return true;
			}
		}
		fireEmployee(distanceTask);
		distanceTask=null;
		return false;
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Export Taxa Distance Matrix";
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		String s =  "Exports a tab-delimited text file of distances among the taxa." ;
		return s;
	}
	/*.................................................................................................................*/

}

