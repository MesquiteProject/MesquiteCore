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

/* 

- make more things protected
- document classes, add doc target to project (use mesquite.lib for example).
- scripting

general exporting stuff:
	- tokenize properly

general importing stuff
	- set punctuation on read in, including single quotes
	- set reading to not consider [ ], or to consider different comment tokens
*/

/* 
Post version 1.0:
	- verbose file reading
	- on reading, store comment line as footnote
*/

/* ============  a file interpreter for GenBank files ============*/
/** This is the class for interpreting GenBank files.  It is subclassed to make interpreters specifically for
DNA and Protein files. */
public abstract class InterpretGenBank extends FileInterpreterI implements ReadFileFromString {
	Class[] acceptedClasses;
/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		acceptedClasses = new Class[] {ProteinState.class, DNAState.class};
 		return true;  //make this depend on taxa reader being found?)
  	 }
  	 
/*.................................................................................................................*/
	public boolean canExportEver() {  
		 return false;  //
	}
/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		 return false;  //
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
	public abstract void setGenBankState(CharacterData data, int ic, int it, char c);
/*.................................................................................................................*/
	public abstract CharacterData createData(CharactersManager charTask, Taxa taxa);

	/*...............................................................................................................*/
	/** for those permitting editing, indicates user has edited to incoming string.*/
	public void setGenBankNumber(CharacterData data, Taxa taxa, int it, String s){
		if (data==null || taxa==null)
			return;
		Taxon taxon = data.getTaxa().getTaxon(it);
		Associable tInfo = data.getTaxaInfo(true);
		if (tInfo != null && taxon != null) {
			tInfo.setAssociatedObject(MolecularData.genBankNumberRef, it, s);
		}
	}
	/*.................................................................................................................*/
	public void readFileCore(Parser parser, MesquiteFile file, CharacterData data, Taxa taxa, int numTaxa, ProgressIndicator progIndicator, String arguments) {
			boolean wassave = data.saveChangeHistory;
			data.saveChangeHistory = false;
			Parser subParser = new Parser();

			StringBuffer sb = new StringBuffer(1000);
			if (file!=null)
				file.readLine(sb);
			else
				sb.append(parser.getRawNextLine());
			String line = sb.toString();
			
			boolean abort = false;

			String taxonName = "";
			String definitionString = "";
			String organismString = "";
			String sourceString = "";
			String locusString = "";
			String accession = "";
			boolean charAdded = false;

			while (!StringUtil.blank(line) && !abort) {
				subParser.setString(line); //sets the string to be used by the parser to "line" and sets the pos to 0
				
				//subParser.setPunctuationString(";");
				String token = subParser.getFirstToken(); 
				
				if (token.equalsIgnoreCase("DEFINITION")){
					definitionString=StringUtil.stripBoundingWhitespace(subParser.getRemaining());
				} 
				else if (token.equalsIgnoreCase("LOCUS")){
					locusString=StringUtil.stripBoundingWhitespace(subParser.getRemaining());
				} 
				else if (token.equalsIgnoreCase("ORGANISM")){
					organismString=StringUtil.stripBoundingWhitespace(subParser.getRemaining());
				} 
				else if (token.equalsIgnoreCase("SOURCE")){
					sourceString=StringUtil.stripBoundingWhitespace(subParser.getRemaining());
				} 
				else if (token.equalsIgnoreCase("ACCESSION")){
					accession=StringUtil.stripBoundingWhitespace(subParser.getRemaining());
				} 
				else if (token.equalsIgnoreCase("ORIGIN")){
					if (!StringUtil.blank(definitionString))
						taxonName=definitionString;
					else if (!StringUtil.blank(organismString))
						taxonName=organismString;
					else if (!StringUtil.blank(sourceString))
						taxonName=sourceString;
					else if (!StringUtil.blank(locusString))
						taxonName=locusString;
					taxa.addTaxa(numTaxa-1, 1, false);
					taxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED), CharacterData.class, true);
					Taxon t = taxa.getTaxon(numTaxa);
					if (!StringUtil.blank(accession)) {
						setGenBankNumber(data, taxa, numTaxa,accession);
					}
					parser.setPunctuationString(null);
					
					if (t!=null) {
						t.setName(taxonName);
						if (file!=null)
							progIndicator.setText(file.getName() + " [Reading sequence: "+taxonName + "]");
						else
							progIndicator.setText(" [Reading sequence: "+taxonName + "]");
						log("[Reading sequence: "+taxonName + "]");
						CommandRecord.tick("[Reading sequence: "+taxonName + "]");
						if (file!=null)
							line = file.readLine("//");  // pull in sequence
						else
							line = parser.getRemainingUntilChar('/', true);
						if (line==null) break;
						subParser.setString(line); 
						int ic = 0;
						char c;
						while (subParser.getPosition()<line.length()) {
							c=subParser.nextDarkChar();
							if (c!='/' && c!= '\0'){
								if (!Character.isDigit(c)) {
									if (data.getNumChars() <= ic) {
										data.addCharacters(data.getNumChars()-1, 20, false);   // add characters if needed
										data.addInLinked(data.getNumChars()-1, 20, false);
										charAdded = true;
									}
									setGenBankState(data,ic, numTaxa, c);    // setting state to that specified by character c
									ic += 1;
									if (ic%100==0) log(".");
								}
							}
						}
						logln("");

					}
					numTaxa++;
			
				} 
				else if (token.equalsIgnoreCase("//")){
				} 
				if (charAdded)
					data.notifyListeners(this, new Notification(CharacterData.PARTS_ADDED, null, null));
				
				if (file!=null)
					line = file.readNextDarkLine();		// added 1.01
				else
					line = parser.getRawNextDarkLine();
				if (file !=null && file.getFileAborted()) {
					abort = true;
				}
			}
			
			data.saveChangeHistory = wassave;
			data.resetCellMetadata();
			finishImport(progIndicator, file, abort);
	}
	
	/** readFileFromString takes the NBRF-formated string "contents" and pumps it into the CharacterData data.  This method is required for the ReadFileFromString interface */
	/*.................................................................................................................*/
	public void readFileFromString(CharacterData data, Taxa taxa, String contents, String arguments) {
		MesquiteProject mf = getProject();
		incrementMenuResetSuppression();
		ProgressIndicator progIndicator = new ProgressIndicator(mf,"Importing Sequences ", contents.length());
		progIndicator.start();
		String fRA = parser.getFirstToken(arguments);
		while (!StringUtil.blank(fRA)) {
			fRA = parser.getNextToken();
		}
		int numTaxa = taxa.getNumTaxa();
		parser.setString(contents);
		readFileCore(parser, null, data,  taxa, numTaxa, progIndicator, arguments);	
		taxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));
		data.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));

		decrementMenuResetSuppression();
	}

/*.................................................................................................................*/
	public void readFile(MesquiteProject mf, MesquiteFile file, String arguments) {
		incrementMenuResetSuppression();
		ProgressIndicator progIndicator = new ProgressIndicator(mf,"Importing File "+ file.getName(), file.existingLength());
		progIndicator.start();
		file.linkProgressIndicator(progIndicator);
		boolean fuse = parser.hasFileReadingArgument(arguments, "fuseTaxaCharBlocks");
		if (file.openReading()) {
			TaxaManager taxaTask = (TaxaManager)findElementManager(Taxa.class);
			 CharactersManager charTask = (CharactersManager)findElementManager(CharacterData.class);

			Taxa taxa = null;
			if (fuse){
				String message = "There is a taxa block in the file \"" + file.getName() + "\" being imported. Mesquite will either fuse this imported taxa block into the taxa block you select below, or it will import that taxa block as new, separate taxa block.";
				taxa = getProject().chooseTaxa(containerOfModule(), message, true, "Fuse with Selected Taxa Block", "Add as New Taxa Block");
			}
			
			if (taxa == null){
				taxa = taxaTask.makeNewTaxa(getProject().getTaxas().getUniqueName("Taxa"), 0, false);
				taxa.addToFile(file, getProject(), taxaTask);
			}
			CategoricalData data = null;
			if (fuse){
				String message = "There is a matrix in the file \"" + file.getName() + "\" being imported. Mesquite will either fuse this matrix into the matrix you select below, or it will import that matrix as new, separate matrix.";
				data = (CategoricalData)getProject().chooseData(containerOfModule(), null, taxa, CategoricalState.class, message,  true,"Fuse with Selected Matrix", "Add as New Matrix");
			}
			if (data == null){
				data =(CategoricalData)createData(charTask,taxa);
				data.addToFile(file, getProject(), null);
			}

			int numTaxa = 0;
			if (fuse)
				numTaxa = taxa.getNumTaxa();

			readFileCore(parser, file, data,  taxa, numTaxa, progIndicator, arguments);
		}
		decrementMenuResetSuppression();
	}
	
	public boolean exportFile(MesquiteFile file, String arguments) {
		return true;
		
	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 201;  
	}

	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive() {
		return true;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "GenBank file";
	}
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Imports GenBank  files that consist of molecular sequence data." ;
   	 }
	/*.................................................................................................................*/
   	 

   	 
}
	

