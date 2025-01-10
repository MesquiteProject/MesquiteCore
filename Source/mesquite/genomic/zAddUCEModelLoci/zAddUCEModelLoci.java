/* Mesquite (package mesquite.io).  Copyright 2000 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.genomic.zAddUCEModelLoci;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.Taxon;
import mesquite.lists.lib.*;
import mesquite.categ.lib.*;
import mesquite.cont.lib.ContinuousData;
import mesquite.io.InterpretFastaProtein.InterpretFastaProtein;


/* ============  a file interpreter for FASTA files ============*/
/** This is the class for interpreting FASTA files.  It is subclassed to make interpreters specifically for
DNA and Protein files. */
public class zAddUCEModelLoci extends DatasetsListUtility {

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		acceptedClasses = new Class[] {ProteinState.class, DNAState.class};
		loadPreferences();
		return true;  //make this depend on taxa reader being found?)
	}


	Class[] acceptedClasses;

	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;  
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}
	boolean badImportWarningGiven = false;

	/*.................................................................................................................*/
	public void setFastaState(CharacterData data, int ic, int it, char c) { 
		if (!(data instanceof DNAData))
			return;
		if ((c=='U')||(c=='u')) {
			((DNAData)data).setDisplayAsRNA(true);
		}
		((DNAData)data).setState(ic,it,c);
		if (CategoricalState.isImpossible(((CategoricalData)data).getState(ic,it))){
			data.badImport = true;
			MesquiteTrunk.errorReportedDuringRun = true;
			if (!badImportWarningGiven)
				discreetAlert("THE DATA WILL BE INCORRECTLY IMPORTED.  The imported sequence includes symbols not interpretable as DNA sequence data.  If this is a protein data file, please use the FASTA protein interpreter. " + 
						"Also, please ensure this is not an rtf or zip file or other format that is not simple text.  This warning may not be given again, but you may see subsequent warnings about impossible states.");
			badImportWarningGiven = true;
		}
	}


	public int[] getNewTaxaAdded(){
		return null;
	}



	public boolean operateOnDatas(ListableVector datas, MesquiteTable table) {

		MesquiteString UCELociDirectory = new MesquiteString();
		MesquiteString UCELociFile = new MesquiteString();
		String filePath= MesquiteFile.openFileDialog("Choose file containing UCE Loci", UCELociDirectory, UCELociFile);
		if (!StringUtil.blank(filePath)) {
			String uceLoci = MesquiteFile.getFileContentsAsString(filePath);
			String line = "";
			Parser parser = new Parser(uceLoci);
			line=parser.getRawNextDarkLine();


			Parser subParser = new Parser();

			// get file from 

			int taxonNumber = -1;


			boolean abort = false;
			subParser.setString(line); //sets the string to be used by the parser to "line" and sets the pos to 0
			subParser.setPunctuationString(">");
			parser.setPunctuationString(">");
			String token = subParser.getFirstToken(line); //should be >
			boolean added = false;

			Taxa originalTaxa = null;
			boolean taxonAdded = false;
			Taxon t = null;
			boolean charAdded = false;

			while (!StringUtil.blank(line) && !abort) {

				token = subParser.getRemaining();  //locus name

				CategoricalData data = (CategoricalData)datas.getElement(token);   // get data matrix with same name as incoming locus
				if (data!=null) {
					Taxa taxa = data.getTaxa();
					if (originalTaxa !=null) {  // we've already seen one
						if (taxa !=originalTaxa) {
							abort=true; // deal with this
							break;
						}
					}
					else {  // our first taxa
						originalTaxa=taxa;
						taxonNumber = taxa.getNumTaxa();
						taxa.addTaxa(taxonNumber-1, 1, true);
						if (taxonNumber>=0)
							t = taxa.getTaxon(taxonNumber);
						if (t!=null)
							t.setName("UCE Locus");
					}

					if (data!=null) {
						//	int numFilledChars = data.getNumChars();
						boolean wassave = data.saveChangeHistory;
						data.saveChangeHistory = false;

						line = parser.getRemainingUntilChar('>', true);
						if (line==null) break;
						subParser.setString(line); 
						int ic = 0;

						while (subParser.getPosition()<line.length()) {
							char c=subParser.nextDarkChar();
							if (c!= '\0') {
								if (data.getNumChars() <= ic) {
									int numChars = data.getNumChars();
									data.addCharacters(numChars-1, 10, false);   // add characters
									data.addInLinked(numChars-1, 10, false);
								}
								setFastaState(data,ic, taxonNumber, c);    // setting state to that specified by character c
							}
							//			if (numFilledChars<ic) //DAVIDCHECK This had been after the ic += 1 which led to a blank site at end for some matrices
							//				numFilledChars=ic;  //Debugg.println
							ic += 1;

						}

						line = parser.getRawNextDarkLine();

						subParser.setString(line); //sets the string to be used by the parser to "line" and sets the pos to 0
					} 

				}
				else {
					line = parser.getRemainingUntilChar('>', true);
					line = parser.getRawNextDarkLine();

					subParser.setString(line); //sets the string to be used by the parser to "line" and sets the pos to 0

				}
				if (charAdded) {
					data.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));
					data.notifyInLinked(new Notification(MesquiteListener.PARTS_ADDED));
				}
			}
		}








		return true;
	}




	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		//		StringUtil.appendXMLTag(buffer, 2, "includeGaps", includeGaps);  
		return buffer.toString();
	}
	public void processSingleXMLPreference (String tag, String flavor, String content){
		processSingleXMLPreference(tag, null, content);
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		//		if ("includeGaps".equalsIgnoreCase(tag))
		//			includeGaps = MesquiteBoolean.fromTrueFalseString(content);
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		//		temp.addLine("includeGaps " + includeGaps);
		return temp;
	}






	/*.................................................................................................................*/
	public String getName() {
		return "Add UCE Loci from FASTA file";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Add UCE Loci from FASTA file...";
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Imports UCE Loci and adds each one to its associated matrix." ;
	}




}


