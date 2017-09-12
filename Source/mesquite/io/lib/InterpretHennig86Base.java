/* Mesquite (package mesquite.io).  Copyright 2000 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)

28 July 01 (WPM): checked for treeVector == null on export; use getCompatibleFileElements
 */
package mesquite.io.lib;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.io.lib.*;
import mesquite.categ.lib.*;
import mesquite.cont.lib.*;
import mesquite.parsimony.lib.*;


/* 



- export trees

- quote within XREAD, DREAD
- tread   : check to see if format allows named taxa in tree description

- handle abbreviations

 */
/*==================================================*/
/* ============  a file interpreter for NONA files ============*/


public abstract class InterpretHennig86Base extends FileInterpreterITree {
	ProgressIndicator progIndicator;
	Class[] acceptedClasses;
	HennigNonaCommand[] availableCommands;
	int treeNumber = 0;
	boolean convertGapsToMissing = false;
	boolean includeQuotes = true;
	Class futureDataClass = null;
	
	protected TaxonNamer taxonNamer = null;
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		availableCommands = new HennigNonaCommand[numCommands];
		acceptedClasses = getAcceptedClasses();
		initializeCommands();
		return true;  //make this depend on taxa reader being found?)
	}
	/*.................................................................................................................*/
	public Class[] getAcceptedClasses() {
		return new Class[] {CategoricalState.class,ProteinState.class, DNAState.class};
	}
	/*.................................................................................................................*/
	public String preferredDataFileExtension() {
		return "ss";
	}
	public void setTaxonNamer(TaxonNamer namer) {
		this.taxonNamer = namer;
	}

	/*.................................................................................................................*/
	public void resetTreeNumber() {
		treeNumber=0;
	}
	/*.................................................................................................................*/
	public boolean isTNT() {
		return false;
	}
	/*.................................................................................................................*/
	public Class getFutureDataClass() {
		return futureDataClass;
	}
	/*.................................................................................................................*/
	public void setFutureDataClass(Class futureDataClass) {
		this.futureDataClass = futureDataClass;
	}

	/*.................................................................................................................*/
	public int getTreeNumber() {
		return treeNumber;
	}
	/*.................................................................................................................*/
	public void incrementTreeNumber() {
		treeNumber++;
	}
	/*.................................................................................................................*/
	public boolean canExportEver() {  
		return true;  //
	}
	/*.................................................................................................................*/
	public boolean additiveIsDefault() {  
		return true;
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
	/*.................................................................................................................*/
	public void setConvertGapsToMissing(boolean convertGapsToMissing) {  
		this.convertGapsToMissing = convertGapsToMissing;
	}
	/*.................................................................................................................*/
	public boolean getConvertGapsToMissing() {  
		return convertGapsToMissing;
	}

	/*.................................................................................................................*/
	static final int numCommands = 8;   // number of available commands
	static final int cnamesElement = 7;
	/*.................................................................................................................*/
	public void initializeCommands() {  
		for (int j = 0; j<numCommands; j++) {
			availableCommands[j] = null;
		}

		availableCommands[0] = new HennigDREAD(this, parser);
		availableCommands[1] = new HennigXREAD(this, parser);
		availableCommands[2] = new HennigCCODE(this, parser);
		availableCommands[3] = new HennigQUOTE(this, parser);
		availableCommands[4] = new HennigTREAD(this, parser);
		availableCommands[5] = new HennigCOMMENTS(this, parser);
		availableCommands[6] = new HennigNSTATES(this, parser);
		availableCommands[cnamesElement] = new HennigCNAMES(this, parser);
	}
	
	/*...............................................  read tree ....................................................*/
	/** Continues reading a tree description, starting at node "node" and the given location on the string*/
	public boolean readClade(MesquiteTree tree, int node, Parser treeParser, NameReference valuesAtNodes) {
		return readClade( tree,  node,  treeParser,  valuesAtNodes, null);

	}

	/*...............................................  read tree ....................................................*/
	/** Continues reading a tree description, starting at node "node" and the given location on the string*/
	public boolean readClade(MesquiteTree tree, int node, Parser treeParser, NameReference valuesAtNodes, TaxonNamer namer) {

		// from BasicTreeWindowMaker in Minimal

		String c = treeParser.getNextToken();
		if ("(".equals(c)){  //internal node
			int sprouted = tree.sproutDaughter(node, false);
			readClade(tree, sprouted, treeParser, valuesAtNodes, namer);
			boolean keepGoing = true;
			while (keepGoing) {
				int loc = treeParser.getPosition();
				String next = treeParser.getNextToken();
				if (")".equals(next)) {  
					keepGoing = false;
					loc = treeParser.getPosition();
					next = treeParser.getNextToken();
					 if ("=".equals(next)) {
						next = treeParser.getNextToken();
						int value = MesquiteInteger.fromString(next);
						tree.setAssociatedDouble(valuesAtNodes, node, 0.01*value, true);  // value will be a percentage

					} else
						treeParser.setPosition(loc);
				}
				else {
					treeParser.setPosition(loc);
					sprouted = tree.sproutDaughter(node, false);
					keepGoing = readClade(tree, sprouted, treeParser, valuesAtNodes, namer);
				}
			}
			return true;
		}
		else if (")".equals(c)) {
			return false;
		}
		else {
			int taxonNumber=-1;
			if (namer != null){  //first try to use the namer if it exists
				int newNumber = namer.whichTaxonNumber(tree.getTaxa(), c);
				if (newNumber >=0 && MesquiteInteger.isCombinable(newNumber))
					taxonNumber = newNumber;
			}
			if (taxonNumber<0) {  // if that didn't work, try normal ways
				taxonNumber = MesquiteInteger.fromString(c);  // try to convert it to a simple integer
				if (taxonNumber <0 || !MesquiteInteger.isCombinable(taxonNumber))    // check to see if it is an integer
					taxonNumber = tree.getTaxa().whichTaxonNumber(c); 
			}
			if (taxonNumber >=0 && MesquiteInteger.isCombinable(taxonNumber)){ //taxon specifier is a number
				if (tree.nodeOfTaxonNumber(taxonNumber)<=0){  // first time taxon encountered
					tree.setTaxonNumber(node, taxonNumber, false);
					return true;
				}
			}
			return false;
		}

	}
	/*.................................................................................................................*/
	public MesquiteTree readTREAD(ProgressIndicator progIndicator, Taxa taxa, String line, boolean firstTree, MesquiteString quoteString, NameReference valuesAtNodes){
		return  readTREAD( progIndicator,  taxa,  line,  firstTree,  quoteString,  valuesAtNodes, null);
	}
	/*.................................................................................................................*/
	public MesquiteTree readTREAD(ProgressIndicator progIndicator, Taxa taxa, String line, boolean firstTree, MesquiteString quoteString, NameReference valuesAtNodes, TaxonNamer namer){
		if (StringUtil.blank(line))
			return null;
		Parser treeParser;
		treeParser =  new Parser();
		line = line.trim();
		treeParser.setQuoteCharacter((char)0);
		treeParser.setPunctuationString("():[];,@+=");
		String lowerLine = line.toLowerCase();
		int treadpos = lowerLine.indexOf("tread");   // check to see it is at start
		if (treadpos>=0){
			line = line.substring(treadpos+5, line.length());
		}
		String token;
		String quote = null;
		char c;
		int linePos;
		parser.setString(line);
		Tree tree = null;
		long totalLength = line.length();


		while (!StringUtil.blank(line)) {
			parser.setString(line);
			c = parser.nextDarkChar();
			if ((treeNumber==0) && (c=='\'' && c!='\0'))  {   // we have a leading quote in the TREAD command
				linePos = parser.getPosition();
				c = parser.nextDarkChar();
				while (c!='\'' && c!='\0') {
					c = parser.nextDarkChar();
				}
				quote = line.substring(linePos,parser.getPosition()-1);  // saving entire quoted section  TODO: place in trees block
				if (quoteString!=null && StringUtil.notEmpty(quote))
					quoteString.setValue(line.substring(linePos,parser.getPosition()-1));
				int startPos = linePos-1;
				int endPos = parser.getPosition()-1;
				if (endPos>line.length())
					endPos=line.length()-1;
				if (startPos<= endPos && startPos>=0 && endPos<=line.length())
					line = StringUtil.removePiece(line, startPos, endPos);  // removing first quote
				parser.setString(line);
				//parser.setPosition(0);
			}	
			if (StringUtil.blank(line)) {
				return null;
			}
			treeNumber++;
			if (progIndicator!=null) {
				progIndicator.setCurrentValue(totalLength-line.length());
				progIndicator.setText("Reading tree " + treeNumber);
			}

			if (c==';' || c=='\0') {
				return null;
			}
			else {    // info for this tree
				int star = line.indexOf("*");
				String treeDescription = null;
				if (star>0) {
					treeDescription = line.substring(0, star);
					line = line.substring(star+1, line.length());
				}
				else  {
					treeDescription = line;
					line = null;
				}
				if (!StringUtil.blank(treeDescription) && treeDescription.indexOf("(")>=0){
					MesquiteTree t = new MesquiteTree(taxa);
					MesquiteInteger pos = new MesquiteInteger(0);
					treeParser.setString(treeDescription);
					readClade(t, t.getRoot(), treeParser, valuesAtNodes, namer);
					t.setAsDefined(true);
					t.setName("Imported tree " + treeNumber);
					return t;
					//trees.addElement(t, false);
					//	if (counter==1 && firstTree)
					//	return trees;
				}
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	public void readTreeFile(MesquiteProject mf, MesquiteFile file, String arguments) {
		Taxa taxa = getProject().chooseTaxa(containerOfModule(), "Of what taxa are these trees composed?");
		if (taxa== null) 
			return;
		incrementMenuResetSuppression();
		//file.linkProgressIndicator(progIndicator);
		if (file.openReading()) {

			parser.setPunctuationString(";");
			parser.setQuoteCharacter((char)0);
			String line = file.readLine(";");   // reads first line
			String command;

			boolean abort = false;
			while (!StringUtil.blank(line) && !abort) {
				parser.setString(line); //sets the string to be used by the parser to "line" and sets the pos to 0
				command = parser.getFirstToken(line); //command name
				if ((availableCommands[4] != null) && availableCommands[4].canRead(command)) {
					availableCommands[4].readCommand(mf,file,null,null,taxa,line);
				}
				line = file.readLine(";");		
				if (file.getFileAborted()) {
					abort = true;
				}
			}
			finishImport(null, file, abort);
		}
		decrementMenuResetSuppression();
	}
	/*.................................................................................................................*/
	public void readFile(MesquiteProject mf, MesquiteFile file, String arguments) {
		incrementMenuResetSuppression();
		progIndicator = new ProgressIndicator(mf,"Importing File "+ file.getName(), file.existingLength());
		progIndicator.start();
		file.linkProgressIndicator(progIndicator);
		if (file.openReading()) {

			parser.setPunctuationString(";");
			parser.setQuoteCharacter((char)0);
			String line = file.readLine(";");   // reads first line
			String command;

			CategoricalData data = null;
			Taxa taxa = null;

			boolean abort = false;
			progIndicator.setText("Reading commands in file of format " + getName());					
			while (!StringUtil.blank(line) && !abort) {
				parser.setString(line); //sets the string to be used by the parser to "line" and sets the pos to 0
				command = parser.getFirstToken(line); //command name
				progIndicator.setText("Reading command " + command);					
				for (int j = 0; j<numCommands; j++) {
					if ((availableCommands[j] != null) && availableCommands[j].canRead(command)) {
						if (availableCommands[j].returnData()) {
							data = availableCommands[j].readCommandReturnData(mf, file,progIndicator);
							if (data == null) {

								discreetAlert( "Sorry, the file could not be read.  The log file may indicate reasons.");
								abort = true;
							}
						}
						else if (data != null)
							availableCommands[j].readCommand(mf,file,progIndicator,data,data.getTaxa(),line);
						else
							availableCommands[j].readCommand(mf,file,progIndicator,null,null,line);
					}
				}
				line = file.readLine(";");		
				if (file.getFileAborted()) {
					abort = true;
				}
			}
			progIndicator.setText("Finishing import of file of format " + getName());					
			finishImport(progIndicator, file, abort);
		}
		decrementMenuResetSuppression();
	}



	/* ============================  exporting ============================*/
	/*.................................................................................................................*/

	public boolean getExportOptions(boolean dataSelected, boolean taxaSelected){
		setLineDelimiter(WINDOWSDELIMITER);
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExporterDialog exportDialog = new ExporterDialog(this,containerOfModule(), "Export TNT/Nona/Hennig86 Options", buttonPressed);
		
		Checkbox convertGapsBox = exportDialog.addCheckBox("convert gaps to missing", convertGapsToMissing);


		exportDialog.completeAndShowDialog(dataSelected, taxaSelected);

		boolean ok = (exportDialog.query(dataSelected, taxaSelected)==0);
		if (ok)  {
			convertGapsToMissing = convertGapsBox.getState();
			//storePreferences();
		}

		exportDialog.dispose();
		return ok;
	}	

	/*.................................................................................................................*/
	public boolean characterShouldBeIncluded(CharacterData data, int ic) {
		return (!writeOnlySelectedData || (data.getSelected(ic)));
		//return ((!writeOnlySelectedData || (data.getSelected(ic))) && (writeExcludedCharacters || data.isCurrentlyIncluded(ic)));
	}
	/*.................................................................................................................*/
	public int getLastCharacterToBeIncluded(CharacterData data) {
		if (writeOnlySelectedData)
			return data.lastSelected();
	/*	if (!writeExcludedCharacters) {
			for (int ic=data.getNumChars()-1; ic>=0; ic--) {
				if (data.isCurrentlyIncluded(ic)) {
					if (!writeOnlySelectedData || data.getSelected(ic))
						return ic;
				}
			}
		}
		*/
		return data.getNumChars()-1;
	}
	/*.................................................................................................................*/

	public boolean getExportOptionsSimple(boolean dataSelected, boolean taxaSelected){   // an example of a simple query, that only proved line delimiter choice; not used here
		return (ExporterDialog.query(this,containerOfModule(), "Export TNT/Nona/Hennig86 Options")==0);
	}	

	/*.................................................................................................................*/
	public CharacterData findDataToExport(MesquiteFile file, String arguments) { 
		return (CharacterData)getProject().chooseData(containerOfModule(), file, null, acceptedClasses, "Select data to export");
	}
	/*.................................................................................................................*/
	public int getNumExportTotal(Taxa taxa, CharacterData data) {  
		int exportTotalElements = 0;
		exportTotalElements+= taxa.getNumTaxa();   // for XREAD
		CategoricalData catData = null;
		if (data instanceof CategoricalData)
			catData = (CategoricalData)data;
		if (catData==null || (catData.hasStateNames()|| catData.characterNamesExist())) {
			exportTotalElements+= data.getNumChars();   // for CNAMES
		}
		return exportTotalElements;
	}
	/*.................................................................................................................*/
	public StringBuffer getDataAsFileText(MesquiteFile file, CharacterData data) {
		Taxa taxa = data.getTaxa();
		CategoricalData catData = null;
		if (data instanceof CategoricalData)
			catData = (CategoricalData)data;
		HennigXDREAD dread = (HennigXDREAD)availableCommands[0];
		HennigXDREAD xread = (HennigXDREAD)availableCommands[1];
		
		if (file != null){
			writeTaxaWithAllMissing = file.writeTaxaWithAllMissing;
			writeExcludedCharacters = file.writeExcludedCharacters;
			fractionApplicable = file.fractionApplicable;
		}


		StringBuffer outputBuffer = new StringBuffer(taxa.getNumTaxa()*(20 + data.getNumChars()));
		if (getIncludeQuotes())
			availableCommands[3].appendCommandToStringBuffer(outputBuffer, taxa, data, progIndicator);  //quote
		if (data.getStateClass()==DNAData.class && !isTNT())
			dread.appendCommandToStringBuffer(outputBuffer, taxa, data, progIndicator);
		else
			xread.appendCommandToStringBuffer(outputBuffer, taxa, data, progIndicator);
		if (catData==null || (catData.hasStateNames()|| data.characterNamesExist()))
			availableCommands[cnamesElement].appendCommandToStringBuffer(outputBuffer, taxa, data, progIndicator);

		availableCommands[2].appendCommandToStringBuffer(outputBuffer, taxa, data, progIndicator);  //ccode
		return outputBuffer;
	}
	/*.................................................................................................................*/
	public boolean exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export
		Arguments args = new Arguments(new Parser(arguments), true);
		boolean usePrevious = args.parameterExists("usePrevious");

		CharacterData  data = findDataToExport(file, arguments);
		if (data ==null) {
			showLogWindow(true);
			logln("WARNING: No suitable data available for export to a file of format \"" + getName() + "\".  The file will not be written.\n");
			return false;
		}
		Taxa taxa = data.getTaxa();
		if (!MesquiteThread.isScripting() && !usePrevious)
			if (!getExportOptions(data.anySelected(), taxa.anySelected())) {
				return false;
			}
		int totalProgressElements = getNumExportTotal(taxa, data);
		progIndicator = new ProgressIndicator(getProject(),"Exporting File ", totalProgressElements, false);
		progIndicator.start();


		StringBuffer outputBuffer = getDataAsFileText(file, data);  //ccode
		if (outputBuffer==null)
			return false;

		if (!args.parameterExists("noTrees"))
			availableCommands[4].appendCommandToStringBuffer(outputBuffer, taxa, data, progIndicator);  //trees

		outputBuffer.append(getLineEnding()+ "proc /;");

		//	outputBuffer.append(getLineEnding());
		//	availableCommands[cnamesElement].appendCommandToStringBuffer(outputBuffer, taxa, data, progIndicator);  //character names
		outputBuffer.append(getLineEnding());
		availableCommands[5].appendCommandToStringBuffer(outputBuffer, taxa, data, progIndicator);  //comments
		outputBuffer.append(getLineEnding());




		progIndicator.goAway();

		saveExportedFileWithExtension(outputBuffer, arguments, preferredDataFileExtension());
		return true;
	}



	/*.................................................................................................................*/
	public String getName() {
		return "TNT, NONA, Hennig86, PiWe, WinClada";
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Imports and exports TNT/NONA/Hennig86/PiWe/WinClada files." ;
	}
	public boolean getIncludeQuotes() {
		return includeQuotes;
	}
	public void setIncludeQuotes(boolean includeQuotes) {
		this.includeQuotes = includeQuotes;
	}




}




/*========================================================*/
abstract class HennigNonaCommand {
	protected  InterpretHennig86Base ownerModule;
	public FileInterpreterI fileInterpreter;
	public Parser parser;


	public HennigNonaCommand(InterpretHennig86Base ownerModule, Parser parser){
		fileInterpreter = (FileInterpreterI)ownerModule;
		this.ownerModule = ownerModule;
		this.parser = parser;
	}
	/*.................................................................................................................*/
	public abstract String getCommandName();   // returns the command name
	/*.................................................................................................................*/
	public  boolean canRead(String s){
		boolean can;
		if (s.length()<getCommandName().length()) {
			String shortCommandName = getCommandName().substring(0,s.length());
			can = getCommandName().equalsIgnoreCase(s) || shortCommandName.equalsIgnoreCase(s);
		}
		else
			can = getCommandName().equalsIgnoreCase(s);
		return can;
	}
	/*.................................................................................................................*/
	public abstract boolean returnData();
	/*.................................................................................................................*/
	public boolean readCommand(MesquiteProject mp, MesquiteFile file, ProgressIndicator progIndicator, CategoricalData data, Taxa taxa, String firstLine){
		return false;
	}
	/*.................................................................................................................*/
	public CategoricalData readCommandReturnData(MesquiteProject mp, MesquiteFile file, ProgressIndicator progIndicator){
		return null;
	}
	/*.................................................................................................................*/
	public void incrementAndUpdateProgIndicator(ProgressIndicator progIndicator, String s){
		if (progIndicator!=null) {
			progIndicator.setCurrentValue(progIndicator.getCurrentValue()+1);
			progIndicator.setText(s);
		}
	}
	/*.................................................................................................................*/
	public void appendCommandToStringBuffer(StringBuffer outputBuffer, Taxa taxa, CharacterData charData, ProgressIndicator progIndicator){
	}
}
/*========================================================*/

/*.................................................................................................................*/
class HennigCCODE extends HennigNonaCommand {
	CharWeightSet weightSet = null;
	CharacterModel additive = null;
	CharacterModel nonadditive = null;
	ParsimonyModelSet modelSet= null;
	CharInclusionSet inclusionSet = null;

	public HennigCCODE (InterpretHennig86Base ownerModule, Parser parser){
		super(ownerModule, parser);
	}
	/*.................................................................................................................*/
	public boolean returnData(){
		return false;
	}
	/*.................................................................................................................*/
	public  String getCommandName(){
		return "ccode";
	}
	/*.................................................................................................................*/
	public boolean readCommand(MesquiteProject mp, MesquiteFile file, ProgressIndicator progIndicator, CategoricalData data, Taxa taxa, String firstLine){
		if (data == null || taxa == null)
			return false;
		/*		[ 	make following characters active   
		]	make following characters inactive;  i.e., exclude
		- 	make following characters nonadditive;  i.e., unordered
		+	make following characters additive;   i.e., ordered
		/N	set weight to number N, then apply to following characters
		 *     discard all previous specifiers
	        =N	set internal steps to number N, then apply to following characters, not supported by Mesquite
		 */     
		/*    	scopes work as follows:  character numbers are used, with a . representing "to".  
		if . is used, without a number on one side, represents end of matrix

		zero based.
		e.g.,
		3	character 3 
		.3	characters 0-3
		3.	characters 3 to the end of the matrix
		3.7	characters 3 through 7 inclusive
		 */ 		

		//weights
		if (weightSet ==null) {
			weightSet= new CharWeightSet("Imported Weight Set", data.getNumChars(), data);  // making a weight set
			weightSet.addToFile(ownerModule.getProject().getHomeFile(), ownerModule.getProject(), ownerModule.findElementManager(CharWeightSet.class)); //attaching the weight set to a file
			data.setCurrentSpecsSet(weightSet, CharWeightSet.class); 

			//types
			additive = ownerModule.getProject().getCharacterModel("ord");
			nonadditive = ownerModule.getProject().getCharacterModel("unord");
			if (ownerModule.additiveIsDefault())
				modelSet= new ParsimonyModelSet("Imported Model Set", data.getNumChars(), nonadditive,data);  // making a parsimony set
			else
				modelSet= new ParsimonyModelSet("Imported Model Set", data.getNumChars(), additive,data);  // making a parsimony set
			modelSet.addToFile(ownerModule.getProject().getHomeFile(), ownerModule.getProject(), ownerModule.findElementManager(ParsimonyModelSet.class)); //attaching the type set to a file
			data.setCurrentSpecsSet(modelSet, ParsimonyModelSet.class);  

			//inclusion exclusion
			inclusionSet= new CharInclusionSet("Imported Inclusion Set", data.getNumChars(), data);  // making an inclusion set
			inclusionSet.addToFile(ownerModule.getProject().getHomeFile(), ownerModule.getProject(), ownerModule.findElementManager(CharInclusionSet.class)); //attaching the inclusion set to a file
			data.setCurrentSpecsSet(inclusionSet, CharInclusionSet.class);  
			inclusionSet.selectAll();
		}




		boolean makeActive = true;
		boolean makeInactive = false;
		boolean makeNonAdditive = false;
		boolean makeAdditive = true;
		boolean setWeight = false;
		int weightToSet = 1;
		int scopeStart = 0;
		int scopeEnd = data.getNumChars()-1;

		parser.setPunctuationString("*-+[]/");
		String token;
		progIndicator.setText("Reading CCODE");
		token = parser.getNextToken();

		while (!StringUtil.blank(token)) {
			if ("[".equals(token)) makeActive = true;
			else if ("]".equals(token)) makeInactive = true;
			else if ("-".equals(token)) makeNonAdditive = true;
			else if ("+".equals(token)) makeAdditive = true;
			else if ("*".equals(token)) {
				makeActive = true;
				makeInactive = false;
				makeNonAdditive = false;
				makeAdditive = true;
				setWeight = false;
			}
			else if ("/".equals(token)) {
				token = parser.getNextToken();
				weightToSet = MesquiteInteger.fromString(token);
				setWeight=true;
			}
			else if ("=".equals(token)) {
				token = parser.getNextToken();
				MesquiteTrunk.mesquiteTrunk.alert("Sorry, Mesquite does not support setting of internal steps in NONA files.");
			}
			else  { // it is a scope, we need to process it
				scopeStart = 0;
				scopeEnd = data.getNumChars()-1;
				if (token.indexOf(".")<0) {// it's just a single character
					scopeStart = MesquiteInteger.fromString(token);
					scopeEnd = scopeStart;
				}
				else if (".".equals(token)) {  // it's just at dot
					scopeStart = 0;
					scopeEnd = data.getNumChars()-1;
				}
				else if (token.charAt(0) =='.') {  // it's at the start
					scopeStart = 0;
					scopeEnd = MesquiteInteger.fromString(token.substring(1, token.length()));
				}
				else if (token.charAt(token.length()-1) =='.') {  // it's at the end
					scopeStart = MesquiteInteger.fromString(token.substring(0, token.length()-1));
					scopeEnd = data.getNumChars()-1;
				}
				else  {  // it's in the middle
					scopeStart = MesquiteInteger.fromString(token.substring(0, token.indexOf(".")));
					scopeEnd = MesquiteInteger.fromString(token.substring(token.indexOf(".")+1, token.length()));
				}
				for (int ic=scopeStart; ic<=scopeEnd; ic++) {   // setting the values of each character in the scope
					if (makeAdditive) modelSet.setModel(additive, ic);
					if (makeNonAdditive) modelSet.setModel(nonadditive, ic);
					if (setWeight) weightSet.setValue(ic, weightToSet);
					if (makeActive) inclusionSet.setSelected(ic, true);
					if (makeInactive) inclusionSet.setSelected(ic, false);
				}

			}
			token = parser.getNextToken();
		}
		parser.setPunctuationString(";");

		return true;
	}
	/*.................................................................................................................*/
	private String scopeToString(int scopeStart, int scopeEnd) {
		String s =" "+scopeStart;
		if (scopeEnd==scopeStart+1)
			s+=" "+scopeEnd;
		else if (scopeEnd>scopeStart+1)
			s+="."+scopeEnd;
		return s;
	}
	/*.................................................................................................................*/
	public void appendCommandToStringBuffer(StringBuffer outputBuffer, Taxa taxa, CharacterData charData, ProgressIndicator progIndicator){
		int numChars = charData.getNumChars();
		String ccode="";

//		writing the character weights
		CharWeightSet weightSet = (CharWeightSet) charData.getCurrentSpecsSet(CharWeightSet.class);
		CharInclusionSet processedSet= new CharInclusionSet("", charData.getNumChars(), charData); // temporary specset
		for (int ic=0; ic<numChars; ic++) 
			processedSet.setSelected(ic, false);

		int ic = 0;
		int scopeStart;
		int icWeight = 1;
		String ccodePart = "";
		int counter = 0;
		int outerLoopCharNumber = -1;
		int innerLoopCharNumber = 0;
		
		int lastCharIncluded = ownerModule.getLastCharacterToBeIncluded(charData);

		if (weightSet != null)
			while (ic< numChars) {
				if (ownerModule.characterShouldBeIncluded(charData, ic)){
					outerLoopCharNumber++;
					if (weightSet.isUnassigned(ic))
						icWeight = 1;  //unassigned treated as 1
					else if (weightSet.isDouble())
						icWeight = (int)(0.5 + weightSet.getDouble(ic));
					else 
						icWeight = weightSet.getInt(ic);
					if (icWeight!=1 && !processedSet.isBitOn(ic)) {
						processedSet.setSelected(ic, true);   // marks this one as already chosen
						ccodePart+=" /"+icWeight + " ";
						scopeStart= counter;
						boolean foundFirstBreak = false;
						innerLoopCharNumber = outerLoopCharNumber;
						for (int icFollow=ic+1; icFollow<numChars; icFollow++) {
							if (ownerModule.characterShouldBeIncluded(charData, icFollow)){
								innerLoopCharNumber++;
								int icFollowWeight = 0;
								if (weightSet.isUnassigned(icFollow))
									icFollowWeight = 1;
								else if (weightSet.isDouble())
									icFollowWeight = (int)(0.5 + weightSet.getDouble(icFollow));
								else 
									icFollowWeight = weightSet.getInt(icFollow);
								if (icFollowWeight==icWeight && !processedSet.isBitOn(icFollow))  {  //it's a weight I've seen before
									processedSet.setSelected(icFollow, true);
									if (scopeStart==-1) {
										scopeStart=innerLoopCharNumber; 
									}
									if ((icFollow==lastCharIncluded)) {   // deal with excluded characters
										ccodePart += scopeToString(scopeStart, innerLoopCharNumber);
										if (!foundFirstBreak)
											ic=numChars;
									}
								}
								else if (scopeStart>=0) {   // we've found a break, write previous scope
									ccodePart += scopeToString(scopeStart, innerLoopCharNumber-1);
									scopeStart=-1;
									if (!foundFirstBreak) {
										foundFirstBreak = true;
									}

								}
							}
						}
					
					}
					counter++;
				}
				ic ++;
			}

		if (ccodePart!="")
			ccode += ccodePart+" *";
		ccodePart = "";

//		now to write which characters are unordered (non-additive)
		ParsimonyModelSet modelSet= (ParsimonyModelSet)charData.getCurrentSpecsSet(ParsimonyModelSet.class);

		String nonDefaultName = "ordered";
		if (ownerModule.additiveIsDefault())
			nonDefaultName="unordered";
		boolean firstTime=true;
		ic = 0;
		counter = 0;
		if (modelSet!=null)
			while (ic<numChars) {
				if (ownerModule.characterShouldBeIncluded(charData, ic)) {
					if (nonDefaultName.equalsIgnoreCase(modelSet.getModel(ic).getName())) {
						if (firstTime) {
							if (ownerModule.additiveIsDefault())
								ccodePart+=" - ";
							else
								ccodePart+=" + ";
							firstTime=false;
						}
						scopeStart=counter;
						while (ic<numChars && ((nonDefaultName.equalsIgnoreCase(modelSet.getModel(ic).getName())) ||  !ownerModule.characterShouldBeIncluded(charData,ic)  )) {
							ic++;
							if (ownerModule.characterShouldBeIncluded(charData, ic))
								counter++;
						}
						if (ic>=lastCharIncluded)
							ccodePart += scopeToString(scopeStart, lastCharIncluded);
						else
							ccodePart += scopeToString(scopeStart, counter-1);
					}
					counter++;
				}
				ic++;
			}

//		now to write which characters are excluded (inactive)
		if (ccodePart!="")
			ccode += ccodePart+" *";
		ccodePart = "";

		CharInclusionSet inclusionSet= (CharInclusionSet)charData.getCurrentSpecsSet(CharInclusionSet.class);

		firstTime=true;
		ic = 0;
		counter = 0;
		if (inclusionSet!=null)
			while (ic<numChars) {
				if (ownerModule.characterShouldBeIncluded(charData, ic)) {
					if (!inclusionSet.isBitOn(ic)) {   // i.e, bit is off
						if (firstTime) {
							ccodePart+=" ] ";
							firstTime=false;
						}
						scopeStart=counter;
						while (ic<numChars && ((!inclusionSet.isBitOn(ic)) ||  !ownerModule.characterShouldBeIncluded(charData,ic)  )) {
							ic++;
							if (ownerModule.characterShouldBeIncluded(charData, ic))
								counter++;
						}
						if (ic>=lastCharIncluded)
							ccodePart += scopeToString(scopeStart, lastCharIncluded);
						else
							ccodePart += scopeToString(scopeStart, counter-1);
					}
					counter++;
				}
				ic++;
			}

		ccode += ccodePart;

		if (!StringUtil.blank(ccode)) {
			outputBuffer.append(getCommandName()+"\t");
			outputBuffer.append(ccode);
			outputBuffer.append(";"+ fileInterpreter.getLineEnding()+ fileInterpreter.getLineEnding());
		}
	}
}
/*========================================================*/

/*.................................................................................................................*/
class HennigQUOTE extends HennigNonaCommand {
	public HennigQUOTE (InterpretHennig86Base ownerModule, Parser parser){
		super(ownerModule, parser);
	}
	/*.................................................................................................................*/
	public boolean returnData(){
		return false;
	}
	/*.................................................................................................................*/
	public  String getCommandName(){
		return "quote";
	}
	/*.................................................................................................................*/
	public boolean readCommand(MesquiteProject mp, MesquiteFile file, ProgressIndicator progIndicator, CategoricalData data, Taxa taxa, String firstLine){

		int pos = firstLine.indexOf("quote");
		if (pos>=0) {
			firstLine = firstLine.substring(pos+6, firstLine.length());
		}
		file.setAnnotation(firstLine, false);

		return true;
	}
	/*.................................................................................................................*/
	public void appendCommandToStringBuffer(StringBuffer outputBuffer, Taxa taxa, CharacterData charData, ProgressIndicator progIndicator){
		if (!StringUtil.blank(charData.getAnnotation())) {
			outputBuffer.append(getCommandName()+" ");
			outputBuffer.append(charData.getAnnotation());
			outputBuffer.append(";"+ fileInterpreter.getLineEnding()+ fileInterpreter.getLineEnding());
		}
	}
}
/*========================================================*/

/*.................................................................................................................*/
class HennigNSTATES extends HennigNonaCommand {
	public HennigNSTATES (InterpretHennig86Base ownerModule, Parser parser){
		super(ownerModule, parser);
	}
	/*.................................................................................................................*/
	public boolean returnData(){
		return false;
	}
	/*.................................................................................................................*/
	public  String getCommandName(){
		return "nstates";
	}
	/*.................................................................................................................*/
	public boolean readCommand(MesquiteProject mp, MesquiteFile file, ProgressIndicator progIndicator, CategoricalData data, Taxa taxa, String firstLine){
		Parser parser = new Parser(firstLine);
		parser.getFirstToken();
		String datatype = parser.getNextToken();
		if ("dna".equalsIgnoreCase(datatype)) {
			ownerModule.setFutureDataClass(DNAData.class);
		}
		else	if ("prot".equalsIgnoreCase(datatype)) {
			ownerModule.setFutureDataClass(ProteinData.class);
		}

		return true;
	}
	/*.................................................................................................................*/
	public void appendCommandToStringBuffer(StringBuffer outputBuffer, Taxa taxa, CharacterData charData, ProgressIndicator progIndicator){
		outputBuffer.append(getCommandName()+" ");
		if (charData instanceof DNAData) 
			outputBuffer.append("dna");
		else if (charData instanceof ProteinData) 
			outputBuffer.append("prot");
		else if (charData instanceof ContinuousData) 
			outputBuffer.append("cont");
		outputBuffer.append(";"+ fileInterpreter.getLineEnding());
	}
}
/*========================================================*/
/*.................................................................................................................*/
class HennigCNAMES extends HennigNonaCommand {
	public HennigCNAMES (InterpretHennig86Base ownerModule, Parser parser){
		super(ownerModule, parser);
	}
	/*.................................................................................................................*/
	public boolean returnData(){
		return false;
	}
	/*.................................................................................................................*/
	public  String getCommandName(){
		return "cnames";
	}
	/*.................................................................................................................*/
	public boolean readCommand(MesquiteProject mp, MesquiteFile file, ProgressIndicator progIndicator, CategoricalData data, Taxa taxa, String firstLine){
		if (parser == null || taxa == null || data == null)
			return false;
		parser.setPunctuationString("{;");
		int charNumber = 0;
		String line = firstLine;
		String token;

		progIndicator.setText("Reading character and state names");
		while (!StringUtil.blank(line)) {
			token = parser.getNextToken();
			if (token==";") 
				return true;
			else {    // info for this character
				token = parser.getNextToken();
				if (token=="+")
					charNumber++;
				else {
					charNumber = MesquiteInteger.fromString(token);
					if ((charNumber>=0)&&(charNumber<=data.getNumChars())) {
						token = parser.getNextToken();
						if (token != "_")
							data.setCharacterName(charNumber,token);
						if ((token !=";") && (token != null)) {
							token = parser.getNextToken();
							if ((token !=";") && (token != null)) {
								int stateNumber = 0;
								while ((token !=";") && (token != null)) {
									if (token != "_") 
										((CategoricalData)data).setStateName(charNumber, stateNumber, token);
									token = parser.getNextToken();
									stateNumber ++;
								}
							}
						}


					}
				}
			}
			line = file.readLine(";");
			parser.setString(line);
		}
		parser.setPunctuationString(";");
		return true;
	}
	/*.................................................................................................................*/
	public void appendCommandToStringBuffer(StringBuffer outputBuffer, Taxa taxa, CharacterData charData, ProgressIndicator progIndicator){
		CategoricalData catData = null;
		if (charData instanceof CategoricalData)
			catData = (CategoricalData)charData;
		outputBuffer.append(getCommandName()+fileInterpreter.getLineEnding());
		int numChars = charData.getNumChars();
		int counter = 0;

		for (int ic = 0; ic<numChars; ic++) {

			if (ownerModule.characterShouldBeIncluded(charData, ic) &&(charData.characterHasName(ic)|| (catData!=null && catData.hasStateNames(ic)))) {
				incrementAndUpdateProgIndicator(progIndicator,"Exporting character and state names");
				outputBuffer.append("{"+counter+" ");
				if (charData.characterHasName(ic))
					outputBuffer.append(StringUtil.tokenize(charData.getCharacterName(ic),";"));
				else if (catData!=null && catData.hasStateNames(ic))
					outputBuffer.append(StringUtil.tokenize("Character_" + (ic+1),";"));

				if (catData!=null && catData.hasStateNames(ic)) {
					for (int stateNumber = 0; stateNumber<=catData.maxStateWithName(ic); stateNumber++) {
						if (catData.hasStateName(ic,stateNumber))
							outputBuffer.append(" " + StringUtil.tokenize(catData.getStateName(ic,stateNumber),";"));
						else
							outputBuffer.append(" " + "_");
					}
				}
				outputBuffer.append(";" + fileInterpreter.getLineEnding());
			}
			if (ownerModule.characterShouldBeIncluded(charData,ic))
				counter++;
		}

		outputBuffer.append(";"+ fileInterpreter.getLineEnding()+ fileInterpreter.getLineEnding());
	}
}
/*========================================================*/
/*.................................................................................................................*/
class HennigCOMMENTS extends HennigNonaCommand {
	public HennigCOMMENTS (InterpretHennig86Base ownerModule, Parser parser){
		super(ownerModule, parser);
	}
	/*.................................................................................................................*/
	public boolean returnData(){
		return false;
	}
	/*.................................................................................................................*/
	public  String getCommandName(){
		return "comments";
	}
	/*.................................................................................................................*/
	public boolean readCommand(MesquiteProject mp, MesquiteFile file, ProgressIndicator progIndicator, CategoricalData data, Taxa taxa, String firstLine){
		if (parser == null || taxa == null || data == null)
			return false;
		parser.setPunctuationString("{;");

		String line = firstLine;
		String token;

		if (progIndicator != null)
			progIndicator.setText("Reading comments");
		while (!StringUtil.blank(line)) {
			token = parser.getNextToken(); //getting
			if (token==";") 
				return true;
			else {    // info for this character
				if (token == null || !("{".equals(token))){
					token = parser.getNextToken(); //getting
				}						
				token = parser.getNextToken();
				int taxonNumber = MesquiteInteger.fromString(token);
				if (taxa != null && (taxonNumber>=0)&&(taxonNumber<=taxa.getNumTaxa())) {
					token = parser.getNextToken(); //taxon number
					int charNumber = MesquiteInteger.fromString(token);
					if ((charNumber>=0)&&(charNumber<=data.getNumChars())) {
						token = parser.getRemaining(); 
						if (data != null && (token !=";") && (token != null)) {
							data.setAnnotation(charNumber, taxonNumber, token);
						}
					}

				}

			}
			line = file.readLine(";");
			parser.setString(line);
		}
		parser.setPunctuationString(";");
		return true;
	}
	/*.................................................................................................................*/
	public void appendCommandToStringBuffer(StringBuffer outputBuffer, Taxa taxa, CharacterData charData, ProgressIndicator progIndicator){
		int numChars = charData.getNumChars();
		int numTaxa = taxa.getNumTaxa();
		int charCounter = 0;
		int totalCounter = 0;
		int startPos = outputBuffer.length();

		incrementAndUpdateProgIndicator(progIndicator,"Exporting comments");
		for (int it = 0; it<numTaxa; it++){
			charCounter = 0;
			for (int ic = 0; ic<numChars; ic++) {
				if (ownerModule.characterShouldBeIncluded(charData, ic)) {
					String note = charData.getAnnotation(ic, it);
					if (note !=null){
						outputBuffer.append("{"+it+" " + charCounter + " ");
						outputBuffer.append(note);
						outputBuffer.append(";" + fileInterpreter.getLineEnding());
						totalCounter++;
					}							
					charCounter++;
				}
			}
		}

		outputBuffer.insert(startPos, getCommandName()+ " " + totalCounter + fileInterpreter.getLineEnding());
		outputBuffer.append(";"+ fileInterpreter.getLineEnding()+ fileInterpreter.getLineEnding());
	}
}
/*========================================================*/




/*.................................................................................................................*/
abstract class HennigXDREAD extends HennigNonaCommand {
	public HennigXDREAD (InterpretHennig86Base ownerModule, Parser parser){
		super(ownerModule, parser);
	}
	/*.................................................................................................................*/
	public boolean returnData(){
		return true;
	}
	/*.................................................................................................................*/
	public abstract CharacterData createData(CharactersManager charTask, Taxa taxa);
	/*.................................................................................................................*/
	public abstract boolean readStartXDREAD(Parser parser);
	/*.................................................................................................................*/
	public CategoricalData readCommandReturnData(MesquiteProject mp, MesquiteFile file, ProgressIndicator progIndicator){
		TaxaManager taxaTask = (TaxaManager)ownerModule.findElementManager(Taxa.class);
		CharactersManager charTask = (CharactersManager)ownerModule.findElementManager(CharacterData.class);

		Taxa taxa = taxaTask.makeNewTaxa("Taxa", 0, false);
		taxa.addToFile(file, ownerModule.getProject(), taxaTask);

		InterpretHennig86Base interpretHN = (InterpretHennig86Base)ownerModule;
		CategoricalData newData = (CategoricalData)createData(charTask,taxa);

		if (newData==null) 
			return null;

		newData.saveChangeHistory = false;

		newData.addToFile(file, ownerModule.getProject(), null);

		if (! readStartXDREAD(parser))
			return null;
		String token;
		char c;

		c = parser.nextDarkChar();
		if (c=='\'')  {   // we have a leading quote in the XREAD command
			int linePos = parser.getPosition();
			c = parser.nextDarkChar();
			while (c!='\'' && c!='\0') {   // TODO: check if at end
				c = parser.nextDarkChar();
			}
			String quote = parser.getString().substring(linePos,parser.getPosition()-1);  // saving entire quoted section  TODO: place in appropriate place
			newData.setAnnotation(quote, false);
		}
		else {   //need to backtrack
			parser.setPosition(parser.getPosition()-1);
		}

		int numTaxa = 0;
		int numChars = 0;
		token = parser.getNextToken();  // numchars
		numChars = MesquiteInteger.fromString(token);
		token = parser.getNextToken();  // numTaxa
		numTaxa = MesquiteInteger.fromString(token);
		boolean wassave = newData.saveChangeHistory;
		newData.saveChangeHistory = false;
		try {
			taxa.addTaxa(-1, numTaxa, true);
			newData.addParts(-1, numChars);
		}
		catch (OutOfMemoryError e){
			MesquiteMessage.warnProgrammer("Sorry, the file could not be read (OutOfMemoryError).  See file memory.txt in the Mesquite_Folder.");
			return null;
		}

		for (int it=0; it<numTaxa; it++) {
			token = parser.getNextToken();
			if (token == ";") 
				return newData;
			Taxon t = taxa.getTaxon(it);
			if (t!=null) {
				t.setName(token);
				progIndicator.setText("Reading taxon: "+token);
				for (int ic=0; ic<numChars; ic++) {
					c=parser.nextDarkChar();
					if (c==';' || c=='\0') 
						return newData;
					else {
						if (c=='[') {
							long set = 0;
							c=parser.nextDarkChar();
							while ((c!=']' && c!='\0')) {
								long newSet;
								if (newData instanceof DNAData || newData instanceof ProteinData)
									newSet= newData.fromChar(c); 
								else
									newSet= newData.fromChar(TNTtoMesquite(c)); 
								set |= newSet;
								c=parser.nextDarkChar();
						//		if (c=='[')  give warning
									
							}
							newData.setState(ic, it, set);
						}
						else {
							if (newData instanceof DNAData || newData instanceof ProteinData)
								newData.setState(ic, it, c);     // setting state to that specified by character c
							else
								newData.setState(ic, it, TNTtoMesquite(c));     // setting state to that specified by character c
						}
					}
				}
			}
		}
		newData.saveChangeHistory = wassave;
		newData.resetCellMetadata();
		return newData;
	}
	/*As of 2. 73 recognizing different default symbol lists to eliminate different step counting when ordered and states span I and O.
	 * */
	static char TNTtoMesquite(char tnt){
		if (tnt == 'I') return 'J';
		if (tnt == 'J') return 'K';
		if (tnt == 'K') return 'M';
		if (tnt == 'L') return 'N';
		if (tnt == 'M') return 'P';
		if (tnt == 'N') return 'Q';
		if (tnt == 'O') return 'R';
		if (tnt == 'P') return 'S';
		if (tnt == 'Q') return 'T';
		if (tnt == 'R') return 'U';
		if (tnt == 'S') return 'V';
		if (tnt == 'T') return 'W';
		if (tnt == 'U') return 'X';
		if (tnt == 'V') return 'Y';
		if (tnt == 'W') return 'Z';
		if (tnt == 'X') return 'a';
		if (tnt == 'Y') return 'b';
		if (tnt == 'Z') return 'c';
		if (tnt == 'a') return 'd';
		if (tnt == 'b') return 'e';
		if (tnt == 'c') return 'f';
		if (tnt == 'd') return 'g';
		if (tnt == 'e') return 'h';
		if (tnt == 'f') return 'j';
		if (tnt == 'g') return 'k';
		if (tnt == 'h') return 'm';
		if (tnt == 'i') return 'n';
		if (tnt == 'j') return 'p';
		if (tnt == 'k') return 'q';
		if (tnt == 'l') return 'r';
		if (tnt == 'm') return 's';
		if (tnt == 'n') return 't';
		if (tnt == 'o') return 'u';
		if (tnt == 'p') return 'v';
		if (tnt == 'q') return 'w';
		if (tnt == 'r') return 'x';
		if (tnt == 's') return 'y';
		if (tnt == 't') return 'z';
		return tnt;
	}
	
	//takes state integer and yields TNT symbol
	static char standardTNTSymbolForState(int s){
		if (s == 0) return '0';
		if (s == 1) return '1';
		if (s == 2) return '2';
		if (s == 3) return '3';
		if (s == 4) return '4';
		if (s == 5) return '5';
		if (s == 6) return '6';
		if (s == 7) return '7';
		if (s == 8) return '8';
		if (s == 9) return '9';
		if (s == 10) return 'A';
		if (s == 11) return 'B';
		if (s == 12) return 'C';
		if (s == 13) return 'D';
		if (s == 14) return 'E';
		if (s == 15) return 'F';
		if (s == 16) return 'G';
		if (s == 17) return 'H';
		if (s == 18) return 'I';
		if (s == 19) return 'J';
		if (s == 20) return 'K';
		if (s == 21) return 'L';
		if (s == 22) return 'M';
		if (s == 23) return 'N';
		if (s == 24) return 'O';
		if (s == 25) return 'P';
		if (s == 26) return 'Q';
		if (s == 27) return 'R';
		if (s == 28) return 'S';
		if (s == 29) return 'T';
		if (s == 30) return 'U';
		if (s == 31) return 'V';
		if (s == 32) return 'W';
		if (s == 33) return 'X';
		if (s == 34) return 'Y';
		if (s == 35) return 'Z';
		if (s == 36) return 'a';
		if (s == 37) return 'b';
		if (s == 38) return 'c';
		if (s == 39) return 'd';
		if (s == 40) return 'e';
		if (s == 41) return 'f';
		if (s == 42) return 'g';
		if (s == 43) return 'h';
		if (s == 44) return 'i';
		if (s == 45) return 'j';
		if (s == 46) return 'k';
		if (s == 47) return 'l';
		if (s == 48) return 'm';
		if (s == 49) return 'n';
		if (s == 50) return 'o';
		if (s == 51) return 'p';
		if (s == 52) return 'q';
		if (s == 53) return 'r';
		if (s == 54) return 's';
		if (s == 55) return 't';
		return '?';
	}
	/*..........................................   ..................................................*/
	/** returns string describing the state(s) of character ic in taxon it.  Uses default symbols, and uses separators 
   		between states in polymorphic (andSep) or partially uncertain (orSep) taxa.  
   		Uses leftBracket and rightBracket to bound entries with multiple symbols */
	String statesToStringDefaultSymbols(CategoricalData data, int ic,int it, char leftBracket, char rightBracket, char andSep, char orSep){
		long s =data.getState(ic, it);
		char INAPPLICABLE = '-';
		char UNASSIGNED = '?';
		boolean first=true;
		char sep = andSep;
		if (CategoricalState.isUncertain(s))
			sep = orSep;
		String stateString="";
		if (s==CategoricalState.inapplicable) 
			return stateString+ INAPPLICABLE;
		else if (s==CategoricalState.unassigned) 
			return stateString+ UNASSIGNED; 
		else if (CategoricalState.cardinality(s)>1) {
			stateString = "" + leftBracket;
			for (int e=0; e<=CategoricalState.maxCategoricalState; e++) {
				if (CategoricalState.isElement(s,e)) {
					if ((!first) && (sep!=Character.UNASSIGNED))
						stateString+=sep;
					stateString += standardTNTSymbolForState(e);
					first = false;
				}
			}
			stateString += rightBracket;
			return stateString;
		}
		else
			return stateString+standardTNTSymbolForState(CategoricalState.minimum(s));
	}
	/*.................................................................................................................*/
	public void appendStateToBuffer(int ic, int it, StringBuffer outputBuffer, CharacterData data){
		CategoricalData catData = null;
		if (data instanceof CategoricalData)
			catData= (CategoricalData)data;
		if (ownerModule.isTNT()){
			if (catData!=null) {
				if (data instanceof MolecularData)
					catData.statesIntoStringBuffer(ic, it, outputBuffer, " ", "[", "]");
				else
					outputBuffer.append(statesToStringDefaultSymbols(catData, ic,it,'[',']', ' ', ' '));
			}
			else if (data instanceof ContinuousData) {
				data.statesIntoStringBuffer(ic, it, outputBuffer, false);
				outputBuffer.append(" ");
			}
		}
		else if (catData!=null) 
			outputBuffer.append(statesToStringDefaultSymbols(catData, ic,it,'[',']',(char)Character.UNASSIGNED, (char)Character.UNASSIGNED));
	}
	/*.................................................................................................................*/
	public void appendCommandToStringBuffer(StringBuffer outputBuffer, Taxa taxa, CharacterData charData, ProgressIndicator progIndicator){
		int numTaxa = taxa.getNumTaxa();
		int numChars = charData.getNumChars();

		if (ownerModule.isTNT())
			if (charData instanceof DNAData) 
				outputBuffer.append("nstates dna;"+fileInterpreter.getLineEnding());
			else if (charData instanceof ProteinData) 
				outputBuffer.append("nstates prot;"+fileInterpreter.getLineEnding());
			else if (charData instanceof ContinuousData) 
				outputBuffer.append("nstates cont;"+fileInterpreter.getLineEnding());
		outputBuffer.append(getCommandName()+fileInterpreter.getLineEnding());
		
		
		int numCharWrite = 0;
/*		if (!fileInterpreter.writeExcludedCharacters)
			numCharWrite =data.numCharsCurrentlyIncluded(fileInterpreter.writeOnlySelectedData);
		else
*/			numCharWrite = charData.numberSelected(fileInterpreter.writeOnlySelectedData);
		
		
		int countTaxa = 0;
		for (int it = 0; it<numTaxa; it++)
			if ((!fileInterpreter.writeOnlySelectedTaxa || taxa.getSelected(it)) && (fileInterpreter.writeTaxaWithAllMissing || charData.hasDataForTaxon(it, fileInterpreter.writeExcludedCharacters)))
				if (fileInterpreter.fractionApplicable==1.0 || charData.getFractionApplicableInTaxon(it, fileInterpreter.writeExcludedCharacters)>=fileInterpreter.fractionApplicable) 
					countTaxa++;
		int numTaxaWrite = countTaxa;

		outputBuffer.append(Integer.toString(numCharWrite)+" ");
		outputBuffer.append(Integer.toString(numTaxaWrite)+fileInterpreter.getLineEnding());

		for (int it = 0; it<numTaxa; it++){
			if ((!fileInterpreter.writeOnlySelectedTaxa || taxa.getSelected(it)) && (fileInterpreter.writeTaxaWithAllMissing || charData.hasDataForTaxon(it, fileInterpreter.writeExcludedCharacters)))
				if (fileInterpreter.fractionApplicable==1.0 || charData.getFractionApplicableInTaxon(it, fileInterpreter.writeExcludedCharacters)>=fileInterpreter.fractionApplicable) {
					incrementAndUpdateProgIndicator(progIndicator,"Exporting data matrix");
					String name = null;
					if (ownerModule.taxonNamer!=null)
						name = ownerModule.taxonNamer.getNameToUse(taxa,it);
					else
						name = (taxa.getTaxonName(it));
					outputBuffer.append(StringUtil.tokenize(name,";") + "\t");
					for (int ic = 0; ic<numChars; ic++) {
						if (ownerModule.characterShouldBeIncluded(charData, ic)){
							if (ownerModule.getConvertGapsToMissing() && charData.isInapplicable(ic, it))
								outputBuffer.append("?");
							else
								appendStateToBuffer(ic, it, outputBuffer, charData);
						}
					}
					outputBuffer.append(fileInterpreter.getLineEnding());
				}
		}
		outputBuffer.append(";"+ fileInterpreter.getLineEnding()+ fileInterpreter.getLineEnding());
	}
}
/*========================================================*/


/*.................................................................................................................*/
class HennigDREAD extends HennigXDREAD {
	public HennigDREAD (InterpretHennig86Base ownerModule, Parser parser){
		super(ownerModule, parser);
	}
	/*.................................................................................................................*/
	public  String getCommandName(){
		return "dread";
	}
	/*.................................................................................................................*/
	public boolean readStartXDREAD(Parser parser){
		String token = parser.getNextToken();
		if (token==null || !token.equalsIgnoreCase("GAP")) {
			ownerModule.logln("GAP subcommand could not be found.");
			return false;
		}
		char c = parser.nextDarkChar();  // ? if treat gap as missing, or ; if treat as fifth base
		token = parser.getNextToken();
		if (token==null || !token.equalsIgnoreCase("MATCH")) {
			ownerModule.logln("MATCH subcommand could not be found.");
			return false;
		}
		c = parser.nextDarkChar();  //�� set matchchar to this, set matchcharon;
		return true;
	}
	/*.................................................................................................................*/
	public CharacterData createData(CharactersManager charTask, Taxa taxa) {  
		return charTask.newCharacterData(taxa, 0, DNAData.DATATYPENAME);  //
	}
}
/*========================================================*/


/*.................................................................................................................*/
class HennigXREAD extends HennigXDREAD {
	public HennigXREAD (InterpretHennig86Base ownerModule, Parser parser){
		super(ownerModule, parser);
	}
	/*.................................................................................................................*/
	public  String getCommandName(){
		return "xread";
	}
	/*.................................................................................................................*/
	public boolean readStartXDREAD(Parser parser){
		return true;
	}
	

	/*.................................................................................................................*/
	public CharacterData createData(CharactersManager charTask, Taxa taxa) {  
		if (ownerModule.getFutureDataClass()==DNAData.class)
			return charTask.newCharacterData(taxa, 0, DNAData.DATATYPENAME);  //
		if (ownerModule.getFutureDataClass()==ProteinData.class)
			return charTask.newCharacterData(taxa, 0, ProteinData.DATATYPENAME);  //
		return charTask.newCharacterData(taxa, 0, CategoricalData.DATATYPENAME);  //
	}

}


/*========================================================*/

/*.................................................................................................................*/
class HennigTREAD extends HennigNonaCommand {
	public HennigTREAD (InterpretHennig86Base ownerModule, Parser parser){
		super(ownerModule, parser);
	}
	/*.................................................................................................................*/
	public boolean returnData(){
		return false;
	}
	/*.................................................................................................................*/
	public  String getCommandName(){
		return "tread";
	}


	/*.................................................................................................................*/
	public boolean readCommand(MesquiteProject mp, MesquiteFile file, ProgressIndicator progIndicator, CategoricalData data, Taxa taxa, String firstLine){
		if (taxa == null)
			taxa = mp.chooseTaxa(ownerModule.containerOfModule(), "Of what taxa are these trees composed?");
		if (taxa == null){
			ownerModule.discreetAlert("Sorry, you cannot read the tree block in this file because no corresponding block of taxa can be found.  Make sure you are linking or including this with a file that includes the block of taxa");
			return false;
		}

		String content = firstLine.trim();
		long totalLength = content.length();
		String lowerLine = content.toLowerCase();
		int treadpos = lowerLine.indexOf("tread"); //TODO: this is case sensitive!!!!
		boolean startOfCommand = (treadpos>=0) ;
		//line = line.substring(treadpos+5, line.length());
		TreeVector trees = new TreeVector(taxa);
		ProgressIndicator progress = null;
		if (progIndicator==null) {
			progress=new ProgressIndicator(mp,"Importing Trees "+ file.getName(), totalLength);
			progress.start();
		}


		MesquiteTree tree = null;
		MesquiteString quote = new MesquiteString();

		Parser parser = new Parser();
		parser.setString(content);
		parser.setLineEndString("*");
		String line = "";

		((InterpretHennig86Base)ownerModule).resetTreeNumber();

		while (!parser.atEnd()) {
			line = parser.getRawNextDarkLine();
			tree = null;
			if (progIndicator==null) {
				tree = ((InterpretHennig86Base)ownerModule).readTREAD(progress, taxa, line, false, quote, null, null);
			}
			else 
				tree = ((InterpretHennig86Base)ownerModule).readTREAD(progIndicator, taxa, line, false, quote, null, null);
			if (tree!=null)
				trees.addElement(tree, false);
		}


		/*		Parser treeParser;
		treeParser =  new Parser();
		treeParser.setQuoteCharacter((char)0);
		String token;
		String quote = null;
		char c;
		int linePos;
		parser.setString(line);
		int counter = 0;


		while (!StringUtil.blank(line)) {
			parser.setString(line);
			c = parser.nextDarkChar();
			if ((counter==0) && (c=='\'' && c!='\0'))  {   // we have a leading quote in the TREAD command
				linePos = parser.getPosition();
				c = parser.nextDarkChar();
				while (c!='\'' && c!='\0') {
					c = parser.nextDarkChar();
				}
				quote = line.substring(linePos,parser.getPosition()-1);  // saving entire quoted section  TODO: place in trees block
				line = line.substring(parser.getPosition()+1, line.length());  // removing first quote
				parser.setString(line);
				//parser.setPosition(0);
			}	
			if (progIndicator==null) {
				progress.setCurrentValue(totalLength-line.length());
				progress.setText("Reading tree " + (++counter));
			}
			else
				progIndicator.setText("Reading tree " + (++counter));

			if (c==';' || c=='\0') {
				return true;
			}
			else {    // info for this tree
				int star = line.indexOf("*");
				String treeDescription = null;
				if (star>0) {
					treeDescription = line.substring(0, star);
					line = line.substring(star+1, line.length());
				}
				else  {
					treeDescription = line;
					line = null;
				}
				if (!StringUtil.blank(treeDescription) && treeDescription.indexOf("(")>=0){
					if (trees == null) {
						trees = new TreeVector(taxa);
						trees.setName("Imported trees");
						if (quote != null)
							trees.setAnnotation(quote, false);
					}
					MesquiteTree t = new MesquiteTree(taxa);
					MesquiteInteger pos = new MesquiteInteger(0);
					treeParser.setString(treeDescription);
					((InterpretNonaHennig)ownerModule).readClade(t, t.getRoot(), treeParser);
					t.setAsDefined(true);
					t.setName("Imported tree " + counter);
					trees.addElement(t, false);
				}
			}
		}
		 */

		if (trees != null) {
			trees.setName("Imported trees");
			if (quote != null)
				trees.setAnnotation(quote.getValue(), false);
		}

		if (trees!=null)
			trees.addToFile(file,file.getProject(),ownerModule.findElementManager(TreeVector.class));
		if (progress!=null) 
			progress.goAway();
		return true;
	}

	/*.................................................................................................................*/
	public void appendCommandToStringBuffer(StringBuffer outputBuffer, Taxa taxa, CharacterData charData, ProgressIndicator progIndicator){
		Listable[] treeVectors = fileInterpreter.getProject().getCompatibleFileElements(TreeVector.class, taxa);
		TreeVector treeVector;
		if (treeVectors.length==0)
			return;
		if (treeVectors.length==1)
			treeVector = (TreeVector)treeVectors[0];
		else
			treeVector = (TreeVector)ListDialog.queryList(fileInterpreter.containerOfModule(), "Include trees in file?", "Include trees in file?", MesquiteString.helpString, treeVectors, 0);
		Tree tree;
		if (treeVector !=null && treeVector.size()>0) {
			outputBuffer.append(getCommandName()+fileInterpreter.getLineEnding());
			for (int iTree = 0; iTree < treeVector.size(); iTree++) {
				tree = (Tree)treeVector.elementAt(iTree);
				outputBuffer.append(tree.writeTreeSimple(Tree.BY_NUMBERS, false, false, false, true, null));  //or Tree.BY_NUMBERS  or Tree.BY_NAMES
				// if do it BY_NAMES, make sure you truncate the taxon names to 10 characters!!
				outputBuffer.append("*"+fileInterpreter.getLineEnding());
			}
			outputBuffer.append(";"+ fileInterpreter.getLineEnding()+ fileInterpreter.getLineEnding());
		}

	}
}



