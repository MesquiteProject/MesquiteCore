/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.FetchAndAddGenBank; 


import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.molec.lib.*;

/* ======================================================================== */
public class FetchAndAddGenBank extends DataUtility { 
	CharacterData data;
	String genBankNumbers;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 110;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryFilesDialog = new ExtensibleDialog(containerOfModule(), "Fetch & Add GenBank",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		queryFilesDialog.addLabel("Accession Numbers (separated by commas); \nranges with commas allowed:");

		genBankNumbers = "";
		TextArea numbersArea = queryFilesDialog.addTextArea("",  5);

		queryFilesDialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			genBankNumbers = numbersArea.getText();
		}
		queryFilesDialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	/** Called to operate on the data in all cells.  Returns true if data altered*/
	public boolean operateOnData(CharacterData data){ 
		this.data = data;

		if (queryOptions()) {
			logln("\nFetching GenBank entries: "  + genBankNumbers);

			try {
				String[] accessionNumbers = StringUtil.delimitedTokensToStrings(genBankNumbers,',',true);
				for (int i=0; i<accessionNumbers.length; i++) 
					if (!StringUtil.blank(accessionNumbers[i])) 				
						logln ("Accession numbers " + accessionNumbers[i]);

				logln("Querying for IDs of entries.");
				String[] idList = NCBIUtil.getGenBankIDs(accessionNumbers, data instanceof DNAData,  this, true);
				if (idList==null)
					return false;
				logln("IDs acquired.");
					/*for (int i=0; i<idList.length; i++) 
						if (!StringUtil.blank(idList[i])) 				
							logln ("To Fetch " + idList[i]);*/

				logln("\nRequesting sequences.\n");
				StringBuffer report = new StringBuffer();
				String fasta = NCBIUtil.fetchGenBankSequences(idList,data instanceof DNAData, this, true, report);
				NCBIUtil.importFASTASequences(data, fasta, this, report, -1, -1, false, false);
				log(report.toString());
				return StringUtil.notEmpty(fasta);


			} catch ( Exception e ){
				// better warning
				return false;
			}
		}
		else
			return false;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		return temp;
	}
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyMolecularData();
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Fetch & Add GenBank Sequences...";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Fetch & Add GenBank Sequences";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Fetches GenBank nucleotide sequences given their GenBank accession numbers and adds them to the matrix.";
	}
}





