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

/* ============  a file interpreter for NBRF files ============*/
/** This is the class for interpreting NBRF/PIR files.  It is subclassed to make interpreters specifically for
DNA and Protein files. */
public abstract class InterpretNBRF extends FileInterpreterI implements ReadFileFromString {
	Class[] acceptedClasses;
/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		acceptedClasses = new Class[] {ProteinState.class, DNAState.class};
 		return true;  //make this depend on taxa reader being found?)
  	 }
  	 
/*.................................................................................................................*/
	public boolean canExportEver() {  
		 return true;  //
	}
/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		 return project.getNumberCharMatrices(acceptedClasses) > 0;  //
	}
	
/*.................................................................................................................*/
	public boolean canExportData(Class dataClass) {  
		 for (int i = 0; i<acceptedClasses.length; i++)
		 	if (dataClass==acceptedClasses[i])
		 		return true;
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
	public abstract void setNBRFState(CharacterData data, int ic, int it, char c);
/*.................................................................................................................*/
	public abstract CharacterData createData(CharactersManager charTask, Taxa taxa);

	/*.................................................................................................................*/
	public void readFileCore(Parser parser, MesquiteFile file, CharacterData data, Taxa taxa, int numTaxa, ProgressIndicator progIndicator, String arguments) {
			boolean wassave = data.saveChangeHistory;
			data.saveChangeHistory = false;
			Parser subParser = new Parser();

			MesquiteStringBuffer sb = new MesquiteStringBuffer(1000);
			if (file!=null)
				file.readLine(sb);
			else
				sb.append(parser.getRawNextLine());
			String line = sb.toString();
			
			boolean abort = false;
			while (!StringUtil.blank(line) && !abort) {
				subParser.setString(line); //sets the string to be used by the parser to "line" and sets the pos to 0
				
				subParser.setPunctuationString(";");
				String token = subParser.getFirstToken(line); //should be >DL
				char c = subParser.nextDarkChar();      // should be a semicolon
				subParser.setPunctuationString(null);
				parser.setPunctuationString(null);
				
				token = subParser.getNextToken();  //taxon Name
				taxa.addTaxa(numTaxa-1, 1, false);
				taxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED), CharacterData.class, true);
				Taxon t = taxa.getTaxon(numTaxa);
				
				if (t!=null) {
					t.setName(token);
					progIndicator.setText("Reading taxon: "+token);
					if (file!=null)
						file.readLine(sb);  // skip over comment line
					else {
						sb.setLength(0);
						sb.append(parser.getRawNextLine());
					}
					line = sb.toString();
					if (line==null) break;
					if (file!=null)
						line = file.readLine("*");  // pull in sequence
					else
						line = parser.getRemainingUntilChar('*', true);
					if (line==null) break;
					subParser.setString(line); 
					int ic = 0;
					while (subParser.getPosition()<line.length()) {
						c=subParser.nextDarkChar();
						if (c!='*' && c!= '\0') {
							if (data.getNumChars() <= ic) {
								data.addCharacters(data.getNumChars()-1, 1, false);   // add a character if needed
								data.addInLinked(data.getNumChars()-1, 1, false);
							}
							//data.setState(ic, numTaxa, c);    // setting state to that specified by character c
							setNBRFState(data,ic, numTaxa, c);    // setting state to that specified by character c
						}
						ic += 1;
					}
				}
				numTaxa++;
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
	public void readFileFromString(CharacterData data, Taxa taxa, String contents, String fileName, String arguments) {
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
	


/* ============================  exporting ============================*/
	boolean includeGaps = false;
	boolean excludeEmpty = false;
	/*.................................................................................................................*/
	
	public boolean getExportOptions(boolean dataSelected, boolean taxaSelected){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExporterDialog exportDialog = new ExporterDialog(this,containerOfModule(), "Export NBRF Options", buttonPressed);
		
		Checkbox includeGapsCheckBox = exportDialog.addCheckBox("include gaps", includeGaps);
		Checkbox excludeEmptyCheckBox = exportDialog.addCheckBox("exclude empty sequences", suppressAllGapTaxa);
		
		exportDialog.completeAndShowDialog(dataSelected, taxaSelected);
			
		boolean ok = (exportDialog.query(dataSelected, taxaSelected)==0);
		suppressAllGapTaxa = excludeEmptyCheckBox.getState();
		
		includeGaps = includeGapsCheckBox.getState();
		exportDialog.dispose();
		return ok;
	}	

	/*.................................................................................................................*/
	
	public boolean getExportOptionsSimple(boolean dataSelected, boolean taxaSelected){   // an example of a simple query, that only proved line delimiter choice; not used here
		return (ExporterDialog.query(this,containerOfModule(), "Export NBRF Options")==0);
	}	
	
	/*.................................................................................................................*/
	public String getLineStart(){  
		return ">DL; ";
	}	
	/*.................................................................................................................*/
	public abstract CharacterData findDataToExport(MesquiteFile file, String arguments);
	/*.................................................................................................................*/
	public MesquiteStringBuffer getDataAsFileText(MesquiteFile file, CharacterData data) {
		if (data==null)
			return null;
		Taxa taxa = data.getTaxa();
		int numTaxa = taxa.getNumTaxa();
		int numChars = data.getNumChars();

		MesquiteStringBuffer outputBuffer = new MesquiteStringBuffer(numTaxa*(20L + numChars));
		boolean wroteMoreThanOneSymbol = false;
		
		int counter = 1;
		for (int it = 0; it<numTaxa; it++){
			if ((!writeOnlySelectedTaxa || (taxa.getSelected(it))) && (!suppressAllGapTaxa || !taxonEntirelyInapplicable(it, data))) {
			// TO DO: also have the option of only writing taxa with data in them
				counter = 1;
				outputBuffer.append(getLineStart());
				outputBuffer.append(ParseUtil.tokenize(taxa.getTaxonName(it)) + getLineEnding());
				outputBuffer.append(ParseUtil.tokenize(taxa.getTaxonName(it)) + getLineEnding());
				for (int ic = 0; ic<numChars; ic++) {
					if (!writeOnlySelectedData || (data.getSelected(ic))){
						long currentSize = outputBuffer.length();

						wroteMoreThanOneSymbol = false;
						if (includeGaps || (!data.isInapplicable(ic,it))) {
								if (data.isUnassigned(ic,it))
									outputBuffer.append("X");
								else
									data.statesIntoStringBuffer(ic, it, outputBuffer, false);
								wroteMoreThanOneSymbol = outputBuffer.length()-currentSize>1;
								counter ++;
								if ((counter % 50 == 1) && (counter > 1)) {    // modulo
									outputBuffer.append(getLineEnding());
								}
						}
						if (wroteMoreThanOneSymbol) {
							alert("Sorry, this data matrix can't be exported to this format (some character states aren't represented by a single symbol [char. " + CharacterStates.toExternal(ic) + ", taxon " + Taxon.toExternal(it) + "])");
							return null;
						}
					}
				}
				outputBuffer.append("*" + getLineEnding());
			}
		}
		return outputBuffer;
	}
   	/** returns whether the character ic is entirely inapplicable codings*/
   	public boolean taxonEntirelyInapplicable(int it, CharacterData data){
   		for (int ic = 0; ic< data.getNumChars(); ic++)
   			if (!data.isInapplicable(ic, it))
   				return false;
   		return true;
   	}
	boolean suppressAllGapTaxa = false;
	boolean oldIncludeGaps = false;
	/*.................................................................................................................*/
	public boolean exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export
		Arguments args = new Arguments(new Parser(arguments), true);
		boolean usePrevious = args.parameterExists("usePrevious");
		oldIncludeGaps = includeGaps;
		suppressAllGapTaxa = args.parameterExists("suppressAllGapTaxa");

		CharacterData data = findDataToExport(file, arguments);
		if (data ==null) {
			showLogWindow(true);
			logln("WARNING: No suitable data available for export to a file of format \"" + getName() + "\".  The file will not be written.\n");
			return false;
		}
		Taxa taxa = data.getTaxa();
		writeOnlySelectedData = args.parameterExists("writeOnlySelectedData");
		if (!MesquiteThread.isScripting() && !usePrevious)
			if (!getExportOptions(data.anySelected(), taxa.anySelected()))
				return false;
		
		if (args.parameterExists("includeGaps"))
			includeGaps = true;

		MesquiteStringBuffer outputBuffer = getDataAsFileText(file, data);
		includeGaps = oldIncludeGaps;
		
		if (outputBuffer==null) {
			return false;
		}
		
		saveExportedFileWithExtension(outputBuffer, arguments, "nbrf");
		suppressAllGapTaxa = false;
		return true;
	}

	/*.................................................................................................................*/
    	 public String getName() {
		return "NBRF/PIR file";
   	 }
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Imports and exports NBRF files that consist of molecular sequence data." ;
   	 }
	/*.................................................................................................................*/
   	 

   	 
}
	

