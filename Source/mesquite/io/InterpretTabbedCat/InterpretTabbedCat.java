/* Mesquite (package mesquite.io).  Copyright 2000 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.io.InterpretTabbedCat;
/*~~  */

import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;

/* ============  a file interpreter for tabbed continuous files ============*/

public class InterpretTabbedCat extends FileInterpreterI {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;  //make this depend on taxa reader being found?)
	}
	/*.................................................................................................................*/
	public boolean canExportEver() {  
		return false; //
	}
	/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		return false; //project.getNumberCharMatrices(CategoricalState.class) > 0; //
	}
	/*.................................................................................................................*/
	public boolean canExportData(Class dataClass) {  
		return false; //(dataClass==CategoricalState.class);
	}
	boolean firstLineAreCharacterNames = false;
	/*.................................................................................................................*/
	public boolean canImport() {  
		return true;
	}
	public void getImportOptions(boolean fuse){
		firstLineAreCharacterNames = AlertDialog.query(containerOfModule(), "Import", "Does the first line of the file contain character names?", "Yes", "No", 1);  //post-1.03
	}
	/*.................................................................................................................*/
	public void readFile(MesquiteProject mf, MesquiteFile file, String arguments) {
		incrementMenuResetSuppression();
		ProgressIndicator progIndicator = new ProgressIndicator(mf,"Importing File "+ file.getName(), file.existingLength());
		progIndicator.start();
		file.linkProgressIndicator(progIndicator);
		if (file.openReading()) {
			boolean abort = false;
			TaxaManager taxaTask = (TaxaManager)findElementManager(Taxa.class);
			CharactersManager charTask = (CharactersManager)findElementManager(CharacterData.class);

			Taxa taxa = taxaTask.makeNewTaxa(getProject().getTaxas().getUniqueName("Taxa"), 0, false);
			taxa.addToFile(file, getProject(), taxaTask);
			CharacterData data = charTask.newCharacterData(taxa, 0,CategoricalData.DATATYPENAME);
			boolean wassave = data.saveChangeHistory;
			data.saveChangeHistory = false;
			data.addToFile(file, getProject(), null);

			int numTaxa = 0;
			StringBuffer sb = new StringBuffer(1000);
			file.readLine(sb);
			String line = sb.toString();

			String token;

			//boolean firstLineAreCharacterNames = MesquiteBoolean.yesNoQuery(containerOfModule(), "Does the first line of the file contain character names?");  //post-1.03

			if (firstLineAreCharacterNames) {
				StringTokenizer st = new StringTokenizer(line, "\t", true);
				int ic = 0;
				if (st.hasMoreTokens()){
					String sc = st.nextToken();  //first tab, ignore
					while (st.hasMoreTokens()) {
						sc = st.nextToken();  //get next token, hopefully character name
						if ("\t".equals(sc)) //no char name; this is tab for next character
							ic++;
						else { //sc isn't tab; is name of ic'th character
							if (data.getNumChars() <= ic) {
								data.addParts(data.getNumChars()-1, 1);   // add a character if needed
							}
							data.setCharacterName(ic, sc);
							if (st.hasMoreTokens())
								sc = st.nextToken();  //this should be a tab
							ic++;

						}
					}
				}
				file.readLine(sb);
				line = sb.toString();		
				if (file.getFileAborted()) {
					abort = true;
				}
			}

			while (!StringUtil.blank(line)&& !abort) {
				StringTokenizer st = new StringTokenizer(line, "\t", false);
				if (st.hasMoreTokens()){
					parser.setString(line);
					String taxonName = st.nextToken();
					taxa.addTaxa(numTaxa-1, 1, true);
					Taxon t = taxa.getTaxon(numTaxa);
					if (t!=null) {
						t.setName(taxonName);
						progIndicator.setText("Reading taxon: "+taxonName);
						int ic = 0;
						while (st.hasMoreTokens()) {
							parser.setString(st.nextToken());
							if (data.getNumChars() <= ic) {
								data.addParts(data.getNumChars()-1, 1);   // add a character if needed
							}
							data.setState(ic, numTaxa, parser, true, null);
							ic += 1;
						}
					}
					numTaxa++;
				}
				file.readLine(sb);
				line = sb.toString();		
				if (file.getFileAborted()) {
					abort = true;
				}
			}
			data.saveChangeHistory = wassave;
			data.resetCellMetadata();
			finishImport(progIndicator, file, abort);
		}
		decrementMenuResetSuppression();
	}

	/** exports data to a file.*/
	public boolean exportFile(MesquiteFile file, String arguments){
		return false;
	}


	/*.................................................................................................................*/
	public String getName() {
		return "Tab-delimited categorical data file";
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Imports simple tab-delimited files of categorical data." ;
	}
	/*.................................................................................................................*/

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}

}


