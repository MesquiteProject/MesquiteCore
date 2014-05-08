/* Mesquite (package mesquite.io).  Copyright 2000-2011 D. Maddison and W. Maddison. 
Version 2.75, September 2011.
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


/* ============  a file interpreter for FASTA files ============*/
/** This is the class for interpreting FASTA files.  It is subclassed to make interpreters specifically for
DNA and Protein files. */
public abstract class InterpretFasta extends FileInterpreterI implements ReadFileFromString {
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
	public abstract void setFastaState(CharacterData data, int ic, int it, char c);
	/*.................................................................................................................*/
	public abstract CharacterData createData(CharactersManager charTask, Taxa taxa);
	/*.................................................................................................................*/
	//NOTE: it is the responsibility of the caller to notify listeners of taxa and data that taxa & possibly characters have been added!
	public void readString(CharacterData data, String s) {
		Taxa taxa = data.getTaxa();
		int numTaxa = taxa.getNumTaxa();
		Parser parser = new Parser(s);
		parser.setPunctuationString(">");
		String line = parser.getRawNextLine();
		Parser firstLineParser = new Parser(line); //sets the string to be used by the parser to "line" and sets the pos to 0
		firstLineParser.setPunctuationString(">");
		String token = firstLineParser.getFirstToken(line); //should be >
		while (!StringUtil.blank(line)) {

			token = firstLineParser.getRemaining();  //taxon Name
			taxa.addTaxa(numTaxa-1, 1, false);
			taxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED), CharacterData.class, true); //notifying only matrices
			Taxon t = taxa.getTaxon(numTaxa);

			if (t!=null) {
				t.setName(token);
				line = parser.getRemainingUntilChar('>');
				line=StringUtil.stripWhitespace(line);
				if (line==null) break;
				int ic = 0;
				int added = 0;
				for (int i=0; i<line.length(); i++) {
					char c=line.charAt(i);
					if (c!= '\0') {
						if (data.getNumChars() <= i) {
							data.addCharacters(data.getNumChars()-1, 1, false);   // add a character if needed
							data.addInLinked(data.getNumChars()-1, 1, false);
						
							added++;
						}
						setFastaState(data,ic, numTaxa, c);    // setting state to that specified by character c
					}
					ic++;
				}
//				data.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED, new int[] {data.getNumChars(), added}));
			}
			numTaxa++;
			line = parser.getRawNextLine();
			firstLineParser.setString(line); //sets the string to be used by the parser to "line" and sets the pos to 0
			if (StringUtil.notEmpty(line))
				token = firstLineParser.getFirstToken(line); //should be >
		}
	}
	/*.................................................................................................................*/
	public void readFileCore(Parser parser, MesquiteFile file, CharacterData data, Taxa taxa, int lastTaxonNumber, ProgressIndicator progIndicator, String arguments, boolean newFile) {
			boolean wassave = data.saveChangeHistory;
			data.saveChangeHistory = false;
			Parser subParser = new Parser();
			long pos = 0;

			StringBuffer sb = new StringBuffer(1000);
			if (file!=null)
				file.readLine(sb);
			else
				sb.append(parser.getRawNextLine());
			String line = sb.toString();
			int taxonNumber = -1;

			boolean abort = false;
			subParser.setString(line); //sets the string to be used by the parser to "line" and sets the pos to 0
			subParser.setPunctuationString(">");
			parser.setPunctuationString(">");
			String token = subParser.getFirstToken(line); //should be >
			int charAdded = 0;
			int numFilledChars = data.getNumChars();
			boolean added = false;
			boolean replaceExisting = false;
			int originalLastTaxonNumber = data.getNumTaxa();
			
			while (!StringUtil.blank(line) && !abort) {

				//parser.setPunctuationString(null);

				token = subParser.getRemaining();  //taxon Name
				taxonNumber = taxa.whichTaxonNumber(token);
				replaceExisting = false;

				if (!hasQueriedAboutSameNameTaxa && taxonNumber >= 0) {
					if (!MesquiteThread.isScripting()){
						//replaceDataOfTaxonWithSameName = AlertDialog.query(this.containerOfModule(), "Replace Data", "Some of the taxa already in the matrix have the same name as incoming taxa.  Do you want to replace their data with the incoming data, or add the incoming data as new taxa?", "Replace Data", "Add As New Taxa", 2);
						replaceDataOfTaxonWithSameNameInt = AlertDialog.query(this.containerOfModule(), "Replace Data", "Some of the taxa already in the matrix have the same name as incoming taxa.  Do you want to replace their data with the incoming data, or add the incoming data as new taxa?", "Replace Data","Replace If Empty", "Add As New Taxa", 3);
					}
					hasQueriedAboutSameNameTaxa= true;
				}
				boolean replace = false;
				if (taxonNumber>=0) {
					if (replaceDataOfTaxonWithSameNameInt==REPLACEDATA) 
						replace=true;
					else if (replaceDataOfTaxonWithSameNameInt==REPLACEIFEMPTY && !data.hasDataForTaxon(taxonNumber)) {
						replace=true;
					}
				}
				
				if (replace) {
					CharacterState cs = data.makeCharacterState(); //so as to get the default state
					int numChars = data.getNumChars();
					replaceExisting = true;
					if (taxonNumber<lastTaxonNumber)
						for (int ic=0; ic<numChars; ic++)
							data.setState(ic, taxonNumber, cs);
					added=false;
				} else {
					if (data.getNumTaxa()<=lastTaxonNumber) {
						int numTaxaAdded = 1;
						if (lastTaxonNumber>10000)
							numTaxaAdded=500;
						else if (lastTaxonNumber>5000)
							numTaxaAdded=200;
						else if (lastTaxonNumber>2500) 
							numTaxaAdded=100;
						else if (lastTaxonNumber>1000)
							numTaxaAdded=50;
						else if (lastTaxonNumber>500)
							numTaxaAdded=10;
						taxa.addTaxa(lastTaxonNumber-1, numTaxaAdded, false);
						added=true;
						if (newFile)
							data.addTaxa(lastTaxonNumber-1, numTaxaAdded);
						else
							taxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED), CharacterData.class, true);
					}
					taxonNumber = lastTaxonNumber;
				}
				
				Taxon t = taxa.getTaxon(taxonNumber);
				
				if (t!=null) {
					t.setName(token);
					if (progIndicator!=null) {
						progIndicator.setText("Reading taxon " + taxonNumber+": "+token);
						CommandRecord.tick("Reading taxon " + taxonNumber+": "+token);
						progIndicator.setCurrentValue(pos);
					}
					if (file!=null)
						line = file.readLine(">");  // pull in sequence up until next >
					else
						line = parser.getRemainingUntilChar('>', true);
					if (line==null) break;
					subParser.setString(line); 
					int ic = 0;
					progIndicator.setSecondaryMessage("Reading character 1");

					while (subParser.getPosition()<line.length()) {
						char c=subParser.nextDarkChar();
						if (c!= '\0') {
							if (data.getNumChars() <= ic) {
								charAdded ++;
								int numChars = data.getNumChars();
								int numToAdd = 1;
								if (numChars>10000) {
									numToAdd=1000;
								} else	if (numChars>5000) {
									numToAdd=500;
								} else if (numChars>2000) {
									numToAdd=100;
								} else if (numChars>200) {
									numToAdd=10;
								} 								
								data.addCharacters(numChars-1, numToAdd, false);   // add characters
								data.addInLinked(numChars-1, numToAdd, false);
							}
							setFastaState(data,ic, taxonNumber, c);    // setting state to that specified by character c
						}
						ic += 1;
						if (numFilledChars<ic) 
							numFilledChars=ic;
						if (ic % 100==0)//== 0 && timer.timeSinceVeryStartInSeconds() % 1.0 <0.001)
							progIndicator.setSecondaryMessage("Reading character " + ic);

					}

				}
				if (added) 
					lastTaxonNumber++;
				//			file.readLine(sb);
				if (file!=null) {
					line = file.readNextDarkLine();		// added 1.01
					pos = file.getFilePosition();
				}
				else {
					line = parser.getRawNextDarkLine();
					pos = parser.getPosition();
				}
				subParser.setString(line); //sets the string to be used by the parser to "line" and sets the pos to 0
				if (file !=null && file.getFileAborted()) {
					abort = true;
				}
			}
			if (lastTaxonNumber<taxa.getNumTaxa())
				if (data.hasDataForTaxa(lastTaxonNumber+1, taxa.getNumTaxa()-1))
					MesquiteMessage.discreetNotifyUser("Warning: InterpretFASTA attempted to delete extra taxa, but these contained data, and so were not deleted");
				else
					taxa.deleteTaxa(lastTaxonNumber, taxa.getNumTaxa()-lastTaxonNumber, true);   // add a character if needed
			if (numFilledChars<data.getNumChars())
				if (data.hasDataForCharacters(numFilledChars+1, data.getNumChars()-1))
					MesquiteMessage.discreetNotifyUser("Warning: InterpretFASTA attempted to delete extra characters, but these contained data, and so were not deleted");
				else
					data.deleteCharacters(numFilledChars+1, data.getNumChars()-numFilledChars, true);   // add a character if needed

/*		
	 		if (charAdded>0) {
				data.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));
				data.notifyInLinked(new Notification(MesquiteListener.PARTS_ADDED));
			}
*/

			data.saveChangeHistory = wassave;
			data.resetChangedSinceSave();
		

			finishImport(progIndicator, file, abort);

	}

	/** readFileFromString takes the FASTA-formated string "contents" and pumps it into the CharacterData data.  This method is required for the ReadFileFromString interface */
	/*.................................................................................................................*/
	public void readFileFromString(CharacterData data, Taxa taxa, String contents, String arguments) {
		MesquiteProject mf = getProject();
		if (taxa == null || StringUtil.blank(contents))
			return;
		incrementMenuResetSuppression();
		ProgressIndicator progIndicator = new ProgressIndicator(mf,"Importing Sequences ", contents.length());
		progIndicator.start();
		String fRA = parser.getFirstToken(arguments);
		while (!StringUtil.blank(fRA)) {
			fRA = parser.getNextToken();
		}
		int numTaxa = taxa.getNumTaxa();
		parser.setString(contents);
		readFileCore(parser, null, data,  taxa, numTaxa, progIndicator, arguments, false);	
		taxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));
		data.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));

		decrementMenuResetSuppression();
	}

	/*.................................................................................................................*/
	public void readFile(MesquiteProject mf, MesquiteFile file, String arguments) {
		incrementMenuResetSuppression();
		ProgressIndicator progIndicator = new ProgressIndicator(mf,"Importing File "+ file.getName(), file.existingLength());
		progIndicator.start();
		boolean fuse = parser.hasFileReadingArgument(arguments, "fuseTaxaCharBlocks");
		file.linkProgressIndicator(progIndicator);
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
		//		data = (CategoricalData)getProject().chooseData(containerOfModule(), null, taxa, CategoricalState.class, "Select matrix with which to fuse the matrix from the file \"" + file.getName() + "  being read.   If you choose cancel, a new matrix will be created instead.",  true);
			}
			if (data == null){
				data =(CategoricalData)createData(charTask,taxa);
				data.addToFile(file, getProject(), null);
			}
			
			int numTaxa = 0;
			if (fuse)
				numTaxa = taxa.getNumTaxa();
			
			readFileCore(parser, file, data,  taxa, numTaxa, progIndicator, arguments, !fuse);	
			
		}
		decrementMenuResetSuppression();
	}



	/* ============================  exporting ============================*/
	protected boolean includeGaps = false;
	protected boolean simplifyTaxonName = false;
	protected String uniqueSuffix = "";
	protected boolean convertMultStateToMissing = true;

	/*.................................................................................................................*/

	public boolean getExportOptions(boolean dataSelected, boolean taxaSelected){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExporterDialog exportDialog = new ExporterDialog(this,containerOfModule(), "Export FASTA Options", buttonPressed);
		exportDialog.appendToHelpString("Choose the options for exporting the matrix as FASTA file prepared for GenBank submission.");
		exportDialog.appendToHelpString(" The Taxon Name Suffix, if present, will be appended to each taxon name.");
		exportDialog.appendToHelpString(" Some systems (e.g., GenBank) require simple taxon names, and these will be used if you check 'simplify taxon names'");
		SingleLineTextField uniqueSuffixField = exportDialog.addTextField("Taxon Name Suffix", "", 20);
		Checkbox simpleTaxonNamesCheckBox = exportDialog.addCheckBox("simplify taxon names", simplifyTaxonName);
		Checkbox includeGapsCheckBox = exportDialog.addCheckBox("include gaps", includeGaps);  
		Checkbox converMultiStateToMissingCheckBox = exportDialog.addCheckBox("convert multistate to missing for protein data", convertMultStateToMissing);

		
		exportDialog.completeAndShowDialog(dataSelected, taxaSelected);
		uniqueSuffix = uniqueSuffixField.getText();
		if (uniqueSuffix==null)
			uniqueSuffix="";


		boolean ok = (exportDialog.query(dataSelected, taxaSelected)==0);

		includeGaps = includeGapsCheckBox.getState();
		simplifyTaxonName=simpleTaxonNamesCheckBox.getState();
		convertMultStateToMissing=converMultiStateToMissingCheckBox.getState();
		
		exportDialog.dispose();
		return ok;
	}	
	/*.................................................................................................................*/

	public boolean getExportOptionsSimple(boolean dataSelected, boolean taxaSelected){   // an example of a simple query, that only proved line delimiter choice; not used here
		return (ExporterDialog.query(this,containerOfModule(), "Export FASTA Options")==0);
	}	

	/*.................................................................................................................*/
	public abstract CharacterData findDataToExport(MesquiteFile file, String arguments);
	/*.................................................................................................................*/
	public abstract String getUnassignedSymbol();

	protected String getSupplementForTaxon(Taxa taxa, int it){
		return "";
	}
	protected String getTaxonName(Taxa taxa, int it){
		if (simplifyTaxonName)
			return StringUtil.cleanseStringOfFancyChars(taxa.getTaxonName(it)+uniqueSuffix,false,true);
		else 
			return taxa.getTaxonName(it)+uniqueSuffix;
			//return ParseUtil.tokenize(taxa.getTaxonName(it)+uniqueSuffix);
	}
	protected void saveExtraFiles(CharacterData data){
	}
	
	protected boolean taxonHasData(CharacterData data, int it){
		for (int ic = 0; ic<data.getNumChars(); ic++) {
			if (!writeOnlySelectedData || (data.getSelected(ic))){
				if (!data.isUnassigned(ic, it) && !data.isInapplicable(ic, it))
					return true;
			}
		}
		return false;
	}
	protected boolean includeOnlyTaxaWithData = true;// TO DO: also have the option of only writing taxa with data in them
	
 	public  StringBuffer getDataAsFileText(MesquiteFile file, CharacterData data) {
		Taxa taxa = data.getTaxa();

		int numTaxa = taxa.getNumTaxa();
		int numChars = data.getNumChars();
		StringBuffer outputBuffer = new StringBuffer(numTaxa*(20 + numChars));
		boolean isProtein = data instanceof ProteinData;
		ProteinData pData =null;
		if (isProtein)
			pData = (ProteinData)data;

		int counter = 1;
		for (int it = 0; it<numTaxa; it++){
			if ((!writeOnlySelectedTaxa || (taxa.getSelected(it))) && (!includeOnlyTaxaWithData || taxonHasData(data, it))){
				
				
				counter = 1;
				outputBuffer.append(">");
				outputBuffer.append(getTaxonName(taxa,it));
				String sup = getSupplementForTaxon(taxa, it);
				if (StringUtil.notEmpty(sup))
					outputBuffer.append(sup);
				outputBuffer.append(getLineEnding());
				for (int ic = 0; ic<numChars; ic++) {
					if (!writeOnlySelectedData || (data.getSelected(ic))){
						int currentSize = outputBuffer.length();
						boolean wroteMoreThanOneSymbol = false;
						if (data.isUnassigned(ic, it) || (convertMultStateToMissing && isProtein && pData.isMultistateOrUncertainty(ic, it)))
							outputBuffer.append(getUnassignedSymbol());
						else if (includeGaps || (!data.isInapplicable(ic,it))) {
							data.statesIntoStringBuffer(ic, it, outputBuffer, false);
                        }
						wroteMoreThanOneSymbol = outputBuffer.length()-currentSize>1;
                        counter ++;
                        if ((counter % 50 == 1) && (counter > 1)) {    // modulo
                            outputBuffer.append(getLineEnding());
                        }

						if (wroteMoreThanOneSymbol) {
							alert("Sorry, this data matrix can't be exported to this format (some character states aren't represented by a single symbol [char. " + CharacterStates.toExternal(ic) + ", taxon " + Taxon.toExternal(it) + "])");
							return null;
						}
					}
				}
				outputBuffer.append(getLineEnding());
			}
		}
		return outputBuffer;
 	}

	/*.................................................................................................................*/
	public boolean exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export
		Arguments args = new Arguments(new Parser(arguments), true);
		boolean usePrevious = args.parameterExists("usePrevious");

		CharacterData data = findDataToExport(file, arguments);
		if (data ==null) {
			showLogWindow(true);
			logln("WARNING: No suitable data available for export to a file of format \"" + getName() + "\".  The file will not be written.\n");
			return false;
		}
		Taxa taxa = data.getTaxa();
		if (!MesquiteThread.isScripting() && !usePrevious)
			if (!getExportOptions(data.anySelected(), taxa.anySelected()))
				return false;

		StringBuffer outputBuffer = getDataAsFileText(file, data);

		if (outputBuffer!=null) {
			saveExportedFileWithExtension(outputBuffer, arguments, "fas");
			saveExtraFiles(data);  
			return true;
		}
		return false;
	}

	/*.................................................................................................................*/
	public String getName() {
		return "FASTA file";
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Imports and exports FASTA files that consist of molecular sequence data." ;
	}
	/*.................................................................................................................*/

	public boolean getConvertMultStateToMissing() {
		return convertMultStateToMissing;
	}

	public void setConvertMultStateToMissing(boolean convertMultStateToMissing) {
		this.convertMultStateToMissing = convertMultStateToMissing;
	}



}


