/* Mesquite (package mesquite.io).  Copyright 2000 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.io.lib;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;


/* ============  a file interpreter for Clustal files ============*/

public abstract class InterpretClustal extends FileInterpreterI {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;  //make this depend on taxa reader being found?)
	}
	/*.................................................................................................................*/
	public boolean canExportEver() {  
		return false;  //
	}
	/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		return false; 
	}
	/*.................................................................................................................*/
	public boolean canExportData(Class dataClass) {  
		return false; 
	}
	/*.................................................................................................................*/
	public boolean canImport() {  
		return true;
	}
	/** Returns whether the module can read (import) files considering the passed argument string (e.g., fuse) */
	public boolean canImport(String arguments){
		return true;
	}
	/*.................................................................................................................*/
	public abstract CharacterData createData(CharactersManager charTask, Taxa taxa);
	/*.................................................................................................................*/
	StringBuffer sb = new StringBuffer(1000);
	public String skipBlankLines(MesquiteFile file, Parser parser, String line){
		while ((line!=null) && StringUtil.blank(line,"*.:")) {
			if (!file.readLine(sb))
				break; 
			line = sb.toString();
		}
		return line;
	}

	boolean badImportWarningGiven = false;
	/*.................................................................................................................*/
	public void readFile(MesquiteProject mf, MesquiteFile file, String arguments) {
		incrementMenuResetSuppression();
		ProgressIndicator progIndicator = new ProgressIndicator(mf,"Importing File "+ file.getName(), file.existingLength());
		progIndicator.start();
		progIndicator.setTextRefreshInterval(200);
		file.linkProgressIndicator(progIndicator);
		if (file.openReading()) {
			TaxaManager taxaTask = (TaxaManager)findElementManager(Taxa.class);
			CharactersManager charTask = (CharactersManager)findElementManager(CharacterData.class);

			Taxa taxa = taxaTask.makeNewTaxa("Taxa", 0, false);
			taxa.addToFile(file, getProject(), taxaTask);
			CategoricalData data = (CategoricalData)createData(charTask,taxa);

			data.addToFile(file, getProject(), null);
			boolean wassave = data.saveChangeHistory;
			data.saveChangeHistory = false;

			boolean firstBlock=true;
			String token;
			char c;

			int numTaxa = 0;
			int it = 0;
			int startChars=0;
			int maxChars = 0;
			int block = 1;
			int ic = 0;
			String line = null;
			StringBuffer sb = new StringBuffer(1000);
			file.readLine(sb);   // reads first line
			file.readLine(sb);   // reads first line of data
			line = sb.toString();
			parser.setString(line);
			line = skipBlankLines(file, parser,line);
			parser.setString(line);

			boolean abort = false;
			while (!StringUtil.blank(line) && !abort) {
				parser.setString(line); //sets the string to be used by the parser to "line" and sets the pos to 0
				parser.setPunctuationString("");

				token = parser.getFirstToken();  //taxon Name
				if (firstBlock) 
					taxa.addTaxa(numTaxa-1, 1, true);
				Taxon t = taxa.getTaxon(it);

				if (t!=null) {
					if (firstBlock)
						t.setName(token);
					progIndicator.setText("Reading block " + block + ", taxon: "+token );
					ic = startChars;
					while (parser.getPosition()<line.length()) {
						c=parser.nextDarkChar();
						if (c=='\0') break;
						if (data.getNumChars() <= ic) {
							data.addCharacters(data.getNumChars()-1, 1, false);   // add a character if needed
						}
						if (c=='~')
							data.setState(ic,it,CategoricalState.inapplicable);
						else
							data.setState(ic, it, c);    // setting state to that specified by character c
						if (CategoricalState.isImpossible(((CategoricalData)data).getState(ic,it))){
							data.badImport = true;
							MesquiteTrunk.errorReportedDuringRun = true;
							if (!badImportWarningGiven) {
								if (data instanceof DNAData)
									discreetAlert("THE DATA WILL BE INCORRECTLY IMPORTED.  The imported sequence includes symbols not interpretable as DNA sequence data.  If this file is a protein data file, please use the Clustal protein interpreter. " + 
									"Also, please ensure this is not an rtf or zip file or other format that is not simple text.  This warning may not be given again, but you may see subsequent warnings about impossible states.");
								else
									discreetAlert("THE DATA WILL BE INCORRECTLY IMPORTED.  The imported sequence includes symbols not interpretable as Protein sequence data.  If this file is a DNA data file, please use the Clustal DNA interpreter. " + 
									"Also, please ensure this is not an rtf or zip file or other format that is not simple text.  This warning may not be given again, but you may see subsequent warnings about impossible states.");
							}
							badImportWarningGiven = true;
						}

						ic += 1;        					
					}
				}
				if (firstBlock)
					numTaxa++;
				if (ic > maxChars)
					maxChars = ic;
				it ++;
				file.readLine(sb);
				line = sb.toString();		

				if (StringUtil.blank(line,"*.:")) {  // at end of block
					line = skipBlankLines(file, parser,line);
					parser.setString(line);
					firstBlock=false;
					block++;
					startChars=maxChars;
					it = 0;
				}

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
	/*.................................................................................................................*/
	public boolean exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export
		return false;
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Clustal file";
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Imports Clustal files that consist of molecular sequence data." ;
	}
	/*.................................................................................................................*/



}


