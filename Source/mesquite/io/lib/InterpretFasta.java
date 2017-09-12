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
import mesquite.cont.lib.ContinuousData;


/* ============  a file interpreter for FASTA files ============*/
/** This is the class for interpreting FASTA files.  It is subclassed to make interpreters specifically for
DNA and Protein files. */
public abstract class InterpretFasta extends FileInterpreterI implements ReadFileFromString {
	StringMatcher nameMatcherTask = null;
	Class[] acceptedClasses;
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e1 = registerEmployeeNeed(StringMatcher.class, "FASTA file import needs a way to determine if the taxon in a ; choose the one that appropriately determines the sequence names from the sample codes.", "This is activated automatically.");
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		acceptedClasses = new Class[] {ProteinState.class, DNAState.class};
		return true;  //make this depend on taxa reader being found?)
	}

	public void getImportOptions(boolean fuse){
		if (fuse) {
			if (nameMatcherTask==null) 
				nameMatcherTask = (StringMatcher)hireEmployee(StringMatcher.class,  "Module to determine whether a taxon name in the incoming FASTA file matches that in an existing matrix.");
			if (nameMatcherTask==null) 
				return;
			else {
				if (!nameMatcherTask.optionsSpecified())
					if (!MesquiteThread.isScripting())
						if (!nameMatcherTask.queryOptions())
							return;
			}
		}
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
	public void readString(CharacterData data, String s, int insertAfterTaxon, String appendToTaxonName) {
		Taxa taxa = data.getTaxa();
		//int numTaxa = taxa.getNumTaxa();
		int newTaxon = insertAfterTaxon+1;
		Parser parser = new Parser(s);
		parser.setPunctuationString(">");
		String line = parser.getRawNextLine();
		Parser firstLineParser = new Parser(line); //sets the string to be used by the parser to "line" and sets the pos to 0
		firstLineParser.setPunctuationString(">");
		String token = firstLineParser.getFirstToken(line); //should be >
		
		int numCharToAdd = 10;  // DRM June '14  to increase speed
		int warnCount = 0;
		
		while (!StringUtil.blank(line)) {

			token = firstLineParser.getRemaining();  //taxon Name
			taxa.addTaxa(newTaxon-1, 1, false);
			taxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED), CharacterData.class, true); //notifying only matrices
			Taxon t = taxa.getTaxon(newTaxon);

			if (t!=null) {
				t.setName(token+appendToTaxonName);
				line = parser.getRemainingUntilChar('>');
				line=StringUtil.stripWhitespace(line);
				if (line==null) break;
				int ic = 0;
				int added = 0;
				for (int i=0; i<line.length(); i++) {
					char c=line.charAt(i);
					if (c!= '\0') {
						if (data.getNumChars() <= i) {
							warnCount++;
							data.addCharacters(data.getNumChars()-1, numCharToAdd, false);   // add a character if needed
							data.addInLinked(data.getNumChars()-1, numCharToAdd, false);
							if (warnCount % 50 ==0)
								CommandRecord.tick("Importing, character " + ic);
						
							added++;
						}
						setFastaState(data,ic, newTaxon, c);    // setting state to that specified by character c
					}
					ic++;
				}
//				data.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED, new int[] {data.getNumChars(), added}));
			}
			newTaxon++;
			line = parser.getRawNextLine();
			firstLineParser.setString(line); //sets the string to be used by the parser to "line" and sets the pos to 0
			if (StringUtil.notEmpty(line))
				token = firstLineParser.getFirstToken(line); //should be >
		}
	}
	
	
	public int[] getNewTaxaAdded(){
		return null;
	}

	/*.................................................................................................................*/
	public void readFileCore(Parser parser, MesquiteFile file, CharacterData data, Taxa taxa, ProgressIndicator progIndicator, String arguments) {
		readFileCore(parser, file, data, taxa, 0, progIndicator, arguments, true);
	}
	public int queryOptionsDuplicate() {
		String helpString = "If you choose Don't Add, then any incoming sequence with the same name as an existing sequence will be ignored. ";
		helpString += "If you choose Replace Data, then the incoming sequence will replace any existing sequence for that taxon.  ";
		helpString += "If you choose Replace If Empty, Otherwise Add, then the incoming sequence will be put into the existing spot for that taxon ONLY if that taxon has no previous data there; if there is already a sequence there, then the incoming sequence will be added as a new taxon. ";
		helpString += "If you choose Replace If Empty, Otherwise Ignore, then the incoming sequence will be put into the existing spot for that taxon ONLY if that taxon has no previous data there; if there is already a sequence there, then the incoming sequence with the same name as an existing taxon will be ignored. ";
		helpString+= "If you choose Add As New Taxa then all incoming sequences will be added as new taxa, even if there already exist taxa in the matrix with identical names.";
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog id = new ExtensibleDialog(containerOfModule(), "Incoming taxa match existing taxa",buttonPressed);
		id.addLargeTextLabel("Some of the taxa already in the matrix have the same name as incoming taxa.  Please choose how incoming sequences with the same name as exising taxa will be treated.");
		if (StringUtil.blank(helpString) && id.isInWizard())
			helpString = "<h3>" + StringUtil.protectForXML("Incoming taxa match existing taxa") + "</h3>Please choose.";
		id.appendToHelpString(helpString);
		
		
		
		RadioButtons radio = id.addRadioButtons(new String[] {"Don't Add", "Replace Data","Replace If Empty, Otherwise Add","Replace If Empty, Otherwise Ignore","Add As New Taxa"},treatmentOfIncomingDuplicates);
		
		id.completeAndShowDialog(true);
		
		int value = -1;
		if (buttonPressed.getValue()==0)  {
			value = radio.getValue();
			treatmentOfIncomingDuplicates = value;
		}
		id.dispose();
		return value;
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
			int numFilledChars = data.getNumChars();
			boolean added = false;
			//StringMatcher nameMatcher = null;
		//	if (MesquiteTrunk.debugMode)
		//		nameMatcher = (MesquiteStringMatcher)hireNamedEmployee(MesquiteStringMatcher.class, "#PrefixedStringMatcher"); //TEMP

			
			while (!StringUtil.blank(line) && !abort) {

				//parser.setPunctuationString(null);

				token = subParser.getRemaining();  //taxon Name
				taxonNumber = taxa.whichTaxonNumber(nameMatcherTask, token, false, false);   // checking to see if a taxon of that name already exists in the file

				if (!hasQueriedAboutSameNameTaxa && taxonNumber >= 0) {
					if (!MesquiteThread.isScripting()){
						replaceDataOfTaxonWithSameNameInt = queryOptionsDuplicate();
					}
					hasQueriedAboutSameNameTaxa= true;
				}
				if (replaceDataOfTaxonWithSameNameInt==STOPIMPORT){
					abort=true;
					break;
				}
				boolean replace = false;
				boolean skipThisSequence = false;
				
				if (taxonNumber>=0) {   // a taxon number of the same name exists
					if (replaceDataOfTaxonWithSameNameInt==DONTADD) {
						skipThisSequence = true;

					}  else if (replaceDataOfTaxonWithSameNameInt==REPLACEDATA) 
						replace=true;
					else if (replaceDataOfTaxonWithSameNameInt==REPLACEIFEMPTYOTHERWISEADD && !data.hasDataForTaxon(taxonNumber)) 
						replace=true;
					else if (replaceDataOfTaxonWithSameNameInt==REPLACEIFEMPTYOTHERWISEIGNORE) {
						if (!data.hasDataForTaxon(taxonNumber))   // empty
							replace=true;
						else   // not empty, so need to ignore this incoming sequence
							skipThisSequence = true;
					}
				}
				added=false;

				
				if (true) {
					if (replace) {
						CharacterState cs = data.makeCharacterState(); //so as to get the default state
						int numChars = data.getNumChars();
						if (taxonNumber<lastTaxonNumber)
							for (int ic=0; ic<numChars; ic++)
								data.setState(ic, taxonNumber, cs);
						added=false;
					} else if (!skipThisSequence) {  // adding to end, not replacing an existing one
						if (getLastNewTaxonFilled()>-1 && getMultiFileImport()) {
							taxonNumber = getLastNewTaxonFilled()+1;
							if (taxonNumber>taxa.getNumTaxa())
								taxonNumber=taxa.getNumTaxa();
							setLastNewTaxonFilled(taxonNumber);
							if (data.hasDataForTaxa(taxonNumber, taxonNumber)) {
								MesquiteMessage.discreetNotifyUser("Warning: InterpretFASTA attempted to overwrite existing data, and so failed.");
								taxonNumber = -1;
							}
						}
						else 
							taxonNumber = taxa.getNumTaxa();



						setLastNewTaxonFilled(taxonNumber);

						if (data.getNumTaxa()<=taxonNumber) {
							int numTaxaAdded = getTotalFilesToImport();  // we may not need all of those, as some of them might be placed in existing taxa.
							if (numTaxaAdded<1) numTaxaAdded =1;
							taxa.addTaxa(taxonNumber-1, numTaxaAdded, false);
							added=true;
							if (newFile)
								data.addTaxa(taxonNumber-1, numTaxaAdded);
							else {
								taxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED), CharacterData.class, true);
							}
						}
					}

					Taxon t = null;

					if (taxonNumber>=0)
						t = taxa.getTaxon(taxonNumber);

					if (t!=null) {
						recordAsNewlyAddedTaxon(taxa,taxonNumber);

						checkMaximumTaxonFilled(taxonNumber);  // record this taxonNumber to see if it is the biggest yet.
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
								if (!skipThisSequence)
									setFastaState(data,ic, taxonNumber, c);    // setting state to that specified by character c
							}
							ic += 1;
							if (numFilledChars<ic) 
								numFilledChars=ic;
							if (ic % 100==0)//== 0 && timer.timeSinceVeryStartInSeconds() % 1.0 <0.001)
								progIndicator.setSecondaryMessage("Reading character " + ic);

						}

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
			if (getMultiFileImport() && getImportFileNumber()>=getTotalFilesToImport()-1)  // last import
				if (getOriginalNumTaxa()>0 && getMaximumTaxonFilled()>=getOriginalNumTaxa() && getMaximumTaxonFilled()<taxa.getNumTaxa()-1)    
					if (!taxa.taxaHaveAnyData(getMaximumTaxonFilled()+1, taxa.getNumTaxa()-1))
						taxa.deleteTaxa(getMaximumTaxonFilled()+1, taxa.getNumTaxa()-getMaximumTaxonFilled(), true);   // delete a character if needed
			
			
			if (numFilledChars<data.getNumChars())
				if (data.hasDataForCharacters(numFilledChars, data.getNumChars()-1)) {
					MesquiteMessage.discreetNotifyUser("Warning: InterpretFASTA attempted to delete extra characters, but these contained data, and so were not deleted.");
					//if (MesquiteTrunk.debugMode) {
						for (int ic=numFilledChars; ic<data.getNumChars(); ic++)
							if (data.hasDataForCharacter(ic))
								logln("has data in character: " + ic);

					//}
				}
				else
					data.deleteCharacters(numFilledChars, data.getNumChars()-numFilledChars, true);   // delete a character if needed

/*		
	 		if (charAdded>0) {
				data.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));
				data.notifyInLinked(new Notification(MesquiteListener.PARTS_ADDED));
			}
*/

			data.saveChangeHistory = wassave;
			data.resetCellMetadata();
		

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
			TaxaManager taxaTask = (TaxaManager)findElementManager(Taxa.class);
			CharactersManager charTask = (CharactersManager)findElementManager(CharacterData.class);
		if (taxaTask == null || getProject() == null || getProject().getTaxas() == null)
			return;
		incrementMenuResetSuppression();
		ProgressIndicator progIndicator = new ProgressIndicator(mf,"Importing File "+ file.getName(), file.existingLength());
		progIndicator.start();
		boolean fuse = parser.hasFileReadingArgument(arguments, "fuseTaxaCharBlocks");
		file.linkProgressIndicator(progIndicator);
		if (file.openReading()) {
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

	public boolean getExportOptions(CharacterData data, boolean dataSelected, boolean taxaSelected){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExporterDialog exportDialog = new ExporterDialog(this,containerOfModule(), "Export FASTA Options", buttonPressed);
		exportDialog.appendToHelpString("Choose the options for exporting the matrix as FASTA file.");
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
				if (!data.isUnassigned(ic, it) && !data.isInapplicable(ic, it) && (writeExcludedCharacters || data.isCurrentlyIncluded(ic)))
						return true;
			}
		}
		return false;
	}
	protected boolean taxonHasMissing(CharacterData data, int it){
		for (int ic = 0; ic<data.getNumChars(); ic++) {
			if (!writeOnlySelectedData || (data.getSelected(ic))){
				if (data.isUnassigned(ic, it) && (writeExcludedCharacters || data.isCurrentlyIncluded(ic)))
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
				if (fractionApplicable==1.0 || data.getFractionApplicableInTaxon(it, writeExcludedCharacters)>=fractionApplicable) {

					counter = 1;
					outputBuffer.append(">");
					outputBuffer.append(getTaxonName(taxa,it));
					String sup = getSupplementForTaxon(taxa, it);
					if (StringUtil.notEmpty(sup))
						outputBuffer.append(sup);
					outputBuffer.append(getLineEnding());

					for (int ic = 0; ic<numChars; ic++) {
						if ((!writeOnlySelectedData || (data.getSelected(ic))) && (writeExcludedCharacters || data.isCurrentlyIncluded(ic))){
							int currentSize = outputBuffer.length();
							boolean wroteMoreThanOneSymbol = false;
							boolean wroteSymbol = false;
							if (data.isUnassigned(ic, it) || (convertMultStateToMissing && isProtein && pData.isMultistateOrUncertainty(ic, it))){
								outputBuffer.append(getUnassignedSymbol());
								counter ++;
								wroteSymbol = true;
							}
							else if (includeGaps || (!data.isInapplicable(ic,it))) {
								data.statesIntoStringBuffer(ic, it, outputBuffer, false);
								counter ++;
								wroteSymbol = true;
							}
							wroteMoreThanOneSymbol = outputBuffer.length()-currentSize>1;
							if ((counter % 50 == 1) && (counter > 1) && wroteSymbol) {    // modulo
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
		}
		return outputBuffer;
 	}
	/*.................................................................................................................*/
	public String preferredDataFileExtension() {  
		return "fas";
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
			if (!getExportOptions(data, data.anySelected(), taxa.anySelected()))
				return false;

		StringBuffer outputBuffer = getDataAsFileText(file, data);

		if (outputBuffer!=null) {
			saveExportedFileWithExtension(outputBuffer, arguments, preferredDataFileExtension());
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


