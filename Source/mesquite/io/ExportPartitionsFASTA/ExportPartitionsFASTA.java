/* Mesquite (package mesquite.io).  Copyright 2000 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.io.ExportPartitionsFASTA;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.Taxon;
import mesquite.lib.ui.ProgressIndicator;
import mesquite.assoc.lib.*;
import mesquite.categ.lib.*;
import mesquite.cont.lib.*;



public class ExportPartitionsFASTA extends FileInterpreterI {
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
		return "fas";
	}
	/*.................................................................................................................*/
	/** Returns wether this interpreter uses a flavour of NEXUS.  Used only to determine whether or not to add "nex" as a file extension to imported files (if already NEXUS, doesn't).**/
	public boolean usesNEXUSflavor(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean canExportEver() {  
		return true;  //
	}
	/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		return (project.getNumberCharMatricesVisible(MolecularState.class) > 0) ;
	}

	/*.................................................................................................................*/
	public boolean canExportData(Class dataClass) {  
		return MolecularData.class.isAssignableFrom(dataClass);
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
	String fileName = "untitled.fas";
	String lineEnding = getLineEnding();

	public CategoricalData findDataToExport(MesquiteFile file, String arguments) { 
		return (CategoricalData)getProject().chooseData(containerOfModule(), file, null, MolecularState.class, "Select data to export");
	}

 	

	boolean saveFile(CategoricalData data, CharacterPartition partition, CharactersGroup group, String path){
		Taxa taxa = data.getTaxa();
		int numTaxa = taxa.getNumTaxa();
		int numChars = data.getNumChars();
		MesquiteStringBuffer outputBuffer = new MesquiteStringBuffer(numTaxa*(20 + numChars));
		
		int counter = 1;
		for (int it = 0; it<numTaxa; it++){
			if (!data.entirelyInapplicableTaxon(it)){

					counter = 1;
					outputBuffer.append(">");
					outputBuffer.append(taxa.getTaxonName(it));
					outputBuffer.append(getLineEnding());

					for (int ic = 0; ic<numChars; ic++) {
						if (partition.getProperty(ic) == group) {
							long currentSize = outputBuffer.length();
							boolean wroteMoreThanOneSymbol = false;
							boolean wroteSymbol = false;
							if (data.isUnassigned(ic, it)){
								outputBuffer.append("-");
								counter ++;
								wroteSymbol = true;
							}
							else {
								data.statesIntoStringBuffer(ic, it, outputBuffer, false);
								counter ++;
								wroteSymbol = true;
							}
							wroteMoreThanOneSymbol = outputBuffer.length()-currentSize>1;
							if ((counter % 50 == 1) && (counter > 1) && wroteSymbol) {    // modulo
								outputBuffer.append(getLineEnding());
							}

							if (wroteMoreThanOneSymbol) {
								discreetAlert("Sorry, this data matrix can't be exported to this format (some character states aren't represented by a single symbol [char. " + CharacterStates.toExternal(ic) + ", taxon " + Taxon.toExternal(it) + "])");
								return false;
							}
						}
						}
					
					outputBuffer.append(getLineEnding());
				}
			}
		

		String name = null;
		if (group == null)
			name = "NO_GROUP";
		else
			name = group.getName();

		MesquiteFile.putFileContents(path  + MesquiteFile.fileSeparator + name + ".fas", outputBuffer.toString(), true);
		return true;

	}
	/*.................................................................................................................*/
	public boolean exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export

		CategoricalData data = findDataToExport(file, arguments);
		if (data == null)
			return false;
		String path = MesquiteFile.chooseDirectory("Directory in which to write files from partitions", getProject().getHomeDirectoryName());

		CharacterPartition partition = (CharacterPartition) data.getCurrentSpecsSet(CharacterPartition.class);
		ProgressIndicator progIndicator = null;
		boolean abort = false;
		boolean found = false;
		if (partition != null){
			CharactersGroupVector groups = (CharactersGroupVector)getProject().getFileElement(CharactersGroupVector.class, 0);
			if (groups != null){
				progIndicator = new ProgressIndicator(getProject(),"Saving partitions as FASTA files", groups.size());
				progIndicator.setStopButtonName("Stop");
				
				for (int i=0; i< groups.size(); i++){
					progIndicator.start();
					CharactersGroup group = (CharactersGroup)groups.elementAt(i);
					boolean tF = saveFile(data, partition, group, path);
					found = found || tF;
					if (progIndicator!=null){
						progIndicator.setCurrentValue(i);
						progIndicator.setText("Number of partitions exported: " + (i+1));
						if (progIndicator.isAborted())
							abort = true;
					}
					if (abort)
						break;
				}
			}
			if (!abort && progIndicator != null) {
				progIndicator.spin();
			}
			if (!abort){
			boolean tF = saveFile(data, partition, null, path);
			found = found || tF;
			}


		}
		if (progIndicator!=null)
			progIndicator.goAway();
		if (!found && !abort)
			discreetAlert("The matrix being exported is not partitioned or has no data in those partitions, and so no files were written");

		return true;
	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Export Partitions as Separate FASTA files";
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Exports character partitions in a matrix as separate FASTA files." ;
	}
	/*.................................................................................................................*/


}


