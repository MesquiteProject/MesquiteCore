/* Mesquite (package mesquite.io).  Copyright 2000 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.io.InterpretTabbedConts;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.cont.lib.*;

/* ============  a file interpreter for tabbed continuous files ============*/

public class InterpretTabbedConts extends FileInterpreterI {
/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;  //make this depend on taxa reader being found?)
  	 }
  	 
/*.................................................................................................................*/
	public boolean canExportEver() {  
		 return true; //
	}
/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		 return project.getNumberCharMatrices(ContinuousState.class) > 0; //
	}
/*.................................................................................................................*/
	public boolean canExportData(Class dataClass) {  
		return (dataClass==ContinuousState.class);
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
			CharacterData data = charTask.newCharacterData(taxa, 0, ContinuousData.DATATYPENAME);
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
							; //ic++;
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
				parser.setString(line);

				token = parser.getRemainingUntilChar('\t');
				
//				token = parser.getNextToken();  changed post 2. 75
				taxa.addTaxa(numTaxa-1, 1, true);
				Taxon t = taxa.getTaxon(numTaxa);
				if (t!=null) {
					t.setName(token);
					progIndicator.setText("Reading taxon: "+token);
					int ic = 0;
					while (parser.getPosition()<line.length()) {
						if (data.getNumChars() <= ic) {
							data.addParts(data.getNumChars()-1, 1);   // add a character if needed
						}
						data.setState(ic, numTaxa, parser, false, null);
						ic += 1;
					}
				}
				numTaxa++;
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
	


/* ============================  exporting ============================*/
	boolean includeGaps = false;
	/*.................................................................................................................*/
	
	public boolean getExportOptions(boolean dataSelected, boolean taxaSelected){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExporterDialog exportDialog = new ExporterDialog(this,containerOfModule(), "Export Tabbed Continuous Options", buttonPressed);
		
		exportDialog.completeAndShowDialog(dataSelected, taxaSelected);
			
		boolean ok = (exportDialog.query(dataSelected, taxaSelected)==0);
		
		exportDialog.dispose();
		return ok;
	}	

	/*.................................................................................................................*/

	public boolean exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export
		Arguments args = new Arguments(new Parser(arguments), true);
		boolean usePrevious = args.parameterExists("usePrevious");

		ContinuousData data = (ContinuousData)getProject().chooseData(containerOfModule(), file, null, ContinuousState.class, "Select data to export");
		if (data ==null) {
			showLogWindow(true);
			logln("WARNING: No suitable data available for export to a file of format \"" + getName() + "\".  The file will not be written.\n");
			return false;
		}
		Taxa taxa = data.getTaxa();
		if (!MesquiteThread.isScripting() && !usePrevious)
			if (!getExportOptions(data.anySelected(), taxa.anySelected()))
				return false;

		int numTaxa = taxa.getNumTaxa();
		int numChars = data.getNumChars();
		StringBuffer outputBuffer = new StringBuffer(numTaxa*(20 + numChars));
		
		for (int it = 0; it<numTaxa; it++){
			if (!writeOnlySelectedTaxa || (taxa.getSelected(it))){
				outputBuffer.append(ParseUtil.tokenize(taxa.getTaxonName(it)));
				for (int ic = 0; ic<numChars; ic++) {
					if (!writeOnlySelectedData || (data.getSelected(ic))){
						outputBuffer.append(ParseUtil.tokenize("\t"));
						data.statesIntoStringBuffer(ic, it, outputBuffer, false);
					}
				}
				outputBuffer.append(getLineEnding());
			}
		}
		
		saveExportedFileWithExtension(outputBuffer, arguments, "txt");
		return true;
	}

	/*.................................................................................................................*/
    	 public String getName() {
		return "Tab-delimited continuous data file";
   	 }
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Imports and exports simple tab-delimited files of continuous data." ;
   	 }
	/*.................................................................................................................*/
   	 
   	 
}
	

