/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.categ.ManageCategoricalChars;

import java.util.*;
import java.awt.*;
import java.io.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;

/* ======================================================================== 
Manages matrices of categorical characters (excluding molecular sequences)*/
public class ManageCategoricalChars extends CharMatrixManager {
	
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	public Class getDataClass(){
		return CategoricalData.class;
	}
	/*.................................................................................................................*/
	public String getDataClassName(){
		return CategoricalData.DATATYPENAME;
	}
	/*.................................................................................................................*/
	public  CharacterData getNewData(Taxa taxa, int numChars){
		if (taxa == null)
			return null;
		return new CategoricalData(this, taxa.getNumTaxa(), numChars, taxa);
	}
	/*.................................................................................................................*/
	public boolean readsWritesDataType(Class dataClass){
		return (dataClass == CategoricalData.class);
	}
	/*.................................................................................................................*/
	public boolean readsWritesDataType(String dataType){
		return dataType.equalsIgnoreCase("Standard");
	}
	/*.................................................................................................................*/
	public CharacterData processFormat(MesquiteFile file, Taxa taxa, String dataType, String formatCommand, MesquiteInteger stringPos, int numChars, String title, String fileReadingArguments) {
	//	MesquiteProject proj=null;
	//	if (file!=null)
	//		proj = file.getProject();
		CategoricalData data= null;
		if (stringPos == null)
			stringPos = new MesquiteInteger(0);
		//@@@@@@@@@@@@@@@@@@@@
		boolean fuse = parser.hasFileReadingArgument(fileReadingArguments, "fuseTaxaCharBlocks");
		boolean merging = false;
		
		if (fuse){
			String message = "In the file being imported, there is a matrix called \"" + title + "\". Mesquite will either fuse this matrix into the matrix you select below, or it will import that matrix as new, separate matrix.";
			data = (CategoricalData)getProject().chooseData(containerOfModule(), null, taxa, CategoricalState.class, message,  true, "Fuse with Selected Matrix", "Add as New Matrix");
			if (data != null && numChars > data.getNumChars())
				data.addCharacters(data.getNumChars()-1, numChars - data.getNumChars(), false);
			if (data != null)
				file.characterDataNameTranslationTable.addElement(new MesquiteString(title, data.getName()), false);
			
			
		}
		if (data == null) {
			if (taxa == null)
				return null;
			data= new CategoricalData(this, taxa.getNumTaxa(), numChars, taxa);
			if (fuse)
				data.setName(title);  //because otherwise titles are not set for fused matrices within ManageCharacters, since on the outside they don't know if it's new
		}
		else {
			if (fuse)
				data.setSuppressSpecssetReading(true);
			if (taxa == null)
				taxa = data.getTaxa();
			data.suppressChecksum = true;
		}
		//@@@@@@@@@@@@@@@@@@@@
		data.interleaved = false;   //reset default in case this is fused
		
		String tok = ParseUtil.getToken(formatCommand, stringPos);
		while (tok != null && !tok.equals(";")) {
			if (tok.equalsIgnoreCase("TRANSPOSE")) {
				alert("Sorry, Transposed matrices of categorical characters can't yet be read");
				return null;
			}
		
			else if (tok.equalsIgnoreCase("format")) { 
			}
			else if (tok.equalsIgnoreCase("RESPECTCASE")) {
				//ignored for the moment 1.05 toDo
			}
		
			else if (tok.equalsIgnoreCase("INTERLEAVE")) {
				int sp = stringPos.getValue();
				String e = ParseUtil.getToken(formatCommand, stringPos); //eating up = ?
				if ("=".equals(e)){
					String y = ParseUtil.getToken(formatCommand, stringPos); //yes or no ?
					data.interleaved = ("yes".equalsIgnoreCase(y));
						
				}
				else {
					stringPos.setValue(sp);
					data.interleaved = true;
				}
			}
			else if (tok.equalsIgnoreCase("MISSING")) { 
				ParseUtil.getToken(formatCommand, stringPos); //eating up =
				String t = ParseUtil.getToken(formatCommand, stringPos);
				if (t!=null && t.length()==1)
					data.setUnassignedSymbol(t.charAt(0));
			}
			else if (tok.equalsIgnoreCase("DATATYPE")) { 
				ParseUtil.getToken(formatCommand, stringPos); //eating up =
				ParseUtil.getToken(formatCommand, stringPos); //eating up datatype
				//alert("Error in NEXUS file format: DATATYPE subcommand must appear as the first item in the FORMAT command");
				//return null;
			}
			else if (tok.equalsIgnoreCase("GAP")) { 
				ParseUtil.getToken(formatCommand, stringPos); //eating up =
				String t = ParseUtil.getToken(formatCommand, stringPos);
				if (t!=null && t.length()==1)
					data.setInapplicableSymbol(t.charAt(0));
			}
			else if (tok.equalsIgnoreCase("MATCHCHAR")) { 
				ParseUtil.getToken(formatCommand, stringPos); //eating up =
				String t = ParseUtil.getToken(formatCommand, stringPos);
				if (t!=null && t.length()==1)
					data.setMatchChar(t.charAt(0));
			}
			else if (tok.equalsIgnoreCase("SYMBOLS")) { 
				ParseUtil.getToken(formatCommand, stringPos); //eating up =
				String t = ParseUtil.getToken(formatCommand, stringPos); //eating up "
				if (t!=null && t.equalsIgnoreCase("\"")){
					t = ParseUtil.getToken(formatCommand, stringPos); //getting next token
					int count = 0;
					boolean rangeMode=false;
					char prevChar = ' ';
					while (t!=null && !t.equals("\"") &&  !t.equals(";")){ // this is the loop were we are processing the text between the quotes
						for (int i = 0; i<t.length(); i++){ //process entire string
							char c = t.charAt(i);
							if (!StringUtil.whitespace(c, null)) {
								if (c=='~') {  //I've modified this to allow for symbols statements like this:  "0~6".  This is in violation of the NEXUS standard, but
									// apparently TNT writes files like this, and MacClade at least wil accept them, so Mesquite now accepts them.  But it will NOT accept it for letters,
									// as the ordering of letters is not so clearly defined.  DRM Aug. 08
									rangeMode=true;
								}
								else {
									if (rangeMode) {  
										if (Character.isDigit(prevChar) && Character.isDigit(c)) {
											String prevS = Character.toString(prevChar);
											String s = Character.toString(c);
											int start = MesquiteInteger.fromString(prevS);
											int end = MesquiteInteger.fromString(s);					
											for (int symbolToAdd= start+1;symbolToAdd<=end && count<=CategoricalState.maxCategoricalState; symbolToAdd++) {
												s=MesquiteInteger.toString(symbolToAdd);
												if (s!=null && s.length()>=1)
													data.setSymbol(count, s.charAt(0));
												count++;
											}
										} 
										rangeMode=false;
									} else {
										if (count<=CategoricalState.maxCategoricalState)
											data.setSymbol(count, c);
										/*
								else {
									if (!file.getOpenAsUntitled())
										file.setOpenAsUntitled("The SYMBOLS subcommand indicated symbols for more than " + (CategoricalState.maxCategoricalState+1) + " states.  Mesquite categorical matrices can have at most " + (CategoricalState.maxCategoricalState+1) + " states. The data matrix may be incorrectly read.");

								}
										 */
										count++;
									}
									prevChar=c;
								}
							}
						}
						t = ParseUtil.getToken(formatCommand, stringPos); //eating up "
					}
				}
			}
			else {
				alert("Unrecognized token (\"" + tok+ "\") in FORMAT statement of categorical matrix; matrix will be stored as foreign, and not processed.");
				return null;
			}
			tok = ParseUtil.getToken(formatCommand, stringPos);
		}
		return data;
	}
	/*.................................................................................................................*/
	public boolean processCommand(CharacterData data, String commandName, String commandString) {
		if (commandName == null)
			return false;
		if (commandName.equalsIgnoreCase("CHARSTATELABELS")){
			String cN = parser.getFirstToken(commandString); //eating up command name
			cN = parser.getNextToken();
			String charName;
			String stateName;
			while (!StringUtil.blank(cN) && !cN.equals(";") ) {
				int charNumber =MesquiteInteger.fromString(cN);
				charName = parser.getNextToken();  
				if (charName != null &&  !charName.equals(",") && !charName.equals(";")) {
					if (!charName.equals("/")) {   // there is a character name
						data.setCharacterName(charNumber-1, charName);
						stateName = parser.getNextToken(); // eating up slash
					}
					else   // there is no character name, only a slash
						stateName = "";
					int stateNumber = 0;
					while (stateName!=null && !stateName.equals(",") && !stateName.equals(";")) {
						stateName = parser.getNextToken();
						if (!stateName.equals(",") && !stateName.equals(";") && data instanceof CategoricalData) 
							((CategoricalData)data).setStateName(charNumber-1, stateNumber, stateName);
						stateNumber++;
					}
				}
				if (charName != null && charName.equals(";"))    // we've encountered the semicolon
					cN = ";";
				else
					cN = parser.getNextToken();
			}
			return true;
		}
		else if (commandName.equalsIgnoreCase("STATELABELS")){

			String cN = parser.getFirstToken(commandString); //eating up command name
			cN = parser.getNextToken();
			while (!StringUtil.blank(cN) && !cN.equals(";") ) {
				int charNumber =MesquiteInteger.fromString(cN);

				String stateName;
				int stateNumber = 0;
				while ((stateName = parser.getNextToken())!=null && !stateName.equals(",") && !stateName.equals(";")) {
					if (!stateName.equals(",")&&!stateName.equals(";") && data instanceof CategoricalData) 
						((CategoricalData)data).setStateName(charNumber-1, stateNumber, stateName);
					stateNumber++;
				}
				
				cN = parser.getNextToken();
			}
			return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	public String getCharStateLabels(CharacterData data, MesquiteFile file) {
		if (file.writeCharLabels){
			String sS = "";
			String csl = "\tCHARLABELS " + StringUtil.lineEnding() + "\t";
			boolean found = false;
			for (int i = 0; i<data.getNumChars(); i++) {
				if (data.characterHasName(i)) {
					found = true;
					csl += " " + StringUtil.tokenize(data.getCharacterName(i));
				}
				else {
					csl += " _";
				}
			}
			if (found)
				sS =  csl + " ; " + StringUtil.lineEnding();
			String ssl = "\tSTATELABELS " + StringUtil.lineEnding();
			found = false;
			CategoricalData dData = (CategoricalData)data;
			for (int i = 0; i<data.getNumChars(); i++) {
				if (dData.hasStateNames(i)) {
					ssl +=  "\t\t" + (i+1);
					String spacers = "";
					for (int s = 0; s<=CategoricalState.maxCategoricalState; s++) {
						if (dData.hasStateName(i,s)) {
							String sn = dData.getStateName(i, s);
							if (sn!=null) {
								ssl += spacers + " " + StringUtil.tokenize(sn);
								spacers = "";
								found = true;
							}
						}
						else 
							spacers = " _";
					}
					ssl += ", " + StringUtil.lineEnding();
				}
				
			}
			if (found)
				sS += ssl + "\t; " + StringUtil.lineEnding();
			return sS;
		}
		if (file.useSimplifiedNexus) //1. 12 so that MrBayes won't choke on exported file
			return "";
		
		String csl = "\tCHARSTATELABELS " + StringUtil.lineEnding();
		boolean found = false;
		String end = " "; //StringUtil.lineEnding()
		CategoricalData dData = (CategoricalData)data;
		for (int i = 0; i<data.getNumChars(); i++) {
			String cslC="";
			if (i>0 && found)
				cslC += "," + end;
			else if (i ==0)
				cslC += "\t\t";
			cslC += Integer.toString(i+1) + " ";    //i+1 to convert to 1 based
			boolean foundInCharacter = false;
			if (data.characterHasName(i)) {
				foundInCharacter = true;
				cslC += StringUtil.tokenize(data.getCharacterName(i));
			}
			if (dData.hasStateNames(i)) {
				cslC +=   " / ";
				String spacers = "";
				for (int s = 0; s<=CategoricalState.maxCategoricalState; s++) {
					if (dData.hasStateName(i,s)) {
						String sn = dData.getStateName(i, s);
						if (sn!=null) {
							foundInCharacter = true;
						
							cslC += spacers + " " + StringUtil.tokenize(sn);
							spacers = "";
						}
					}
					else 
						spacers = " _";
				}
			}
			if (foundInCharacter) {
				csl += cslC;
				found = true;
			}
		}
		if (found)
			return csl + " ; " + StringUtil.lineEnding();
		else
			return "";
	}
	/*.................................................................................................................*/
	public void writeCharactersBlock(CharacterData data, CharactersBlock cB, MesquiteFile file, ProgressIndicator progIndicator){
		String endLine =";" + StringUtil.lineEnding();
		StringBuffer line = new StringBuffer(2*data.getNumChars());
		if (file == null)
			file = data.getFile();
		if (file == null)
			return;
		if (file.useDataBlocks)
			file.write("BEGIN DATA");
		else
			file.write("BEGIN CHARACTERS");
		if (data.getAnnotation()!=null && !file.useSimplifiedNexus) {
			file.write("[!" + StringUtil.tokenize(data.getAnnotation()) + "]");
		}
		file.write(endLine);
		if (MesquiteFile.okToWriteTitleOfNEXUSBlock(file, data)){
			file.write("\tTITLE  ");
			file.write( StringUtil.tokenize(data.getName()));
			file.write(endLine);
		}
		//if (data.getTaxa().getName()!=null  && getProject().getNumberTaxas(cB.getFile())>1){ //before 13 Dec 01 had been this
		if (MesquiteFile.okToWriteTitleOfNEXUSBlock(file, data.getTaxa()) && getProject().getNumberTaxas()>1){ //��� should have an isUntitled method??
			file.write("\tLINK TAXA = ");
			file.write(StringUtil.tokenize(data.getTaxa().getName()));
			file.write(endLine);
		}
		file.write("\tDIMENSIONS ");
		if (file.useSimplifiedNexus && file.useDataBlocks) {
			int numTaxaToWrite = data.getNumTaxa();
			if (!file.writeTaxaWithAllMissing)
				if (file.writeOnlySelectedTaxa)
					numTaxaToWrite = data.numSelectedTaxaWithSomeApplicable(false);
				else
					numTaxaToWrite = data.numTaxaWithSomeApplicable(false);
			else if (file.writeOnlySelectedTaxa)
				numTaxaToWrite = data.numSelectedTaxa();

				
				file.write(" NTAX=" + numTaxaToWrite);
		}
		int numCharsToWrite;
		if (file.writeExcludedCharacters)
			numCharsToWrite = data.getNumChars();
		else
			numCharsToWrite = data.getNumCharsIncluded();
		file.write(" NCHAR=" + numCharsToWrite);
		file.write(endLine);
		file.write("\tFORMAT");
		file.write(" DATATYPE = STANDARD");
		file.write(" GAP = " + data.getInapplicableSymbol() + " MISSING = " + data.getUnassignedSymbol());
		file.write(" SYMBOLS = \" ");
		int maxSt = ((CategoricalData)data).getMaxState();
		int maxS = ((CategoricalData)data).getMaxSymbolDefined();
		if (maxS < 1)
			maxS = 1;
		if (maxS <maxSt)
			maxS = maxSt;
		for (int i=0; i<=maxS; i++)
			file.write(" " + ((CategoricalData)data).getSymbol(i));
		file.write("\"");
		file.write(endLine);
		if (data.isLinked() && !file.useSimplifiedNexus  && !file.useConservativeNexus){
			file.write("\tOPTIONS ");
			Vector ds = data.getDataLinkages();
			for (int i = 0; i<ds.size(); i++) {
				file.write(" LINKCHARACTERS = ");
				file.write(StringUtil.tokenize(((CharacterData)ds.elementAt(i)).getName()));
			}
			file.write(endLine);
		}
		if (progIndicator!=null) progIndicator.setText("Writing character & state names");
		file.write(getCharStateLabels(data, file));
		if (progIndicator!=null) progIndicator.setText("Writing character matrix");
		file.write("\tMATRIX" + StringUtil.lineEnding());
		int numTotal = data.getNumTaxa() * numCharsToWrite;
		//MesquiteModule.mesquiteTrunk.mesquiteMessage("Composing categorical matrix ", numTotal, 0);
		int numTaxa = data.getTaxa().getNumTaxa();
		int tot = 0;
		String taxonName="";
		String taxonNameToWrite = "";
		int maxNameLength = data.getTaxa().getLongestTaxonNameLength();
		for (int it=0; it<numTaxa; it++) {
			if ((data.someApplicableInTaxon(it, false)|| file.writeTaxaWithAllMissing) && (!file.writeOnlySelectedTaxa || data.getTaxa().getSelected(it))){
				taxonName = data.getTaxa().getTaxon(it).getName();
				if (taxonName!=null) {
					if (file.useStandardizedTaxonNames)
						taxonNameToWrite = "t" + it;
					else
						taxonNameToWrite = StringUtil.simplifyIfNeededForOutput(taxonName,file.simplifyNames);
					file.write("\t"+taxonNameToWrite);
					for (int i = 0; i<(maxNameLength-taxonNameToWrite.length()+2); i++)
						file.write(" ");
				}
				int totInTax = 0;

				if (progIndicator!=null) progIndicator.setText("Writing data for taxon " + taxonName);
				line.setLength(0);
				//USE SYMBOLS
				for (int ic=0;  ic<data.getNumChars(); ic++) {
					if (data.isCurrentlyIncluded(ic) || file.writeExcludedCharacters) {
						if (file.ambiguityToMissing && ((CategoricalData)data).isAmbiguousOrPolymorphic(ic, it))
							line.append('?');
						else {
							data.statesIntoNEXUSStringBuffer(ic, it, line);
								
						}
						tot++;
						totInTax++;
						if (tot % 5000 == 0)
							logln("Composing categorical matrix: " + tot + " of " + numTotal);
						if (totInTax % 1000 == 0)
							line.append(StringUtil.lineEnding()); //this is here becuase of current problem (at least on mrj 2.2) of long line slow writing

					}
				}
				file.write(line.toString());
				file.write(StringUtil.lineEnding());
				//file.write(blocks.toString());
				//blocks.setLength(0);
			}
		}
		file.write(StringUtil.lineEnding());
		file.write(endLine);
		if (!file.useSimplifiedNexus  && !file.useConservativeNexus){
			String idsCommand = getIDsCommand(data);
			if (!StringUtil.blank(idsCommand))
				file.write("\t" + idsCommand + StringUtil.lineEnding());
		}
		if (cB != null)
			file.write(cB.getUnrecognizedCommands() + StringUtil.lineEnding());
		file.write("END;" + StringUtil.lineEnding());
		if (progIndicator!=null) progIndicator.setText("Finished writing matrix");

	//	file.write( blocks.toString());
	}

	/*.................................................................................................................*/
	public NexusCommandTest getNexusCommandTest(){ 
		return new CategNexusCommandTest();
	}
	/*...................................................................................................................*/
	public boolean readNexusCommand(MesquiteFile file, NexusBlock nBlock, String blockName, String command, MesquiteString comment){ 
		if (blockName.equalsIgnoreCase("NOTES")) {
			boolean fuse = parser.hasFileReadingArgument(file.fileReadingArguments, "fuseTaxaCharBlocks");
			if (fuse)
				return true;
			MesquiteProject project = file.getProject();
			String commandName = parser.getFirstToken(command);
			if  (commandName.equalsIgnoreCase("TEXT")){
				int integer = MesquiteInteger.unassigned;
				String name = null;
				stringPos.setValue(parser.getPosition());
				String[][] subcommands  = ParseUtil.getSubcommands(command, stringPos);
				if (subcommands == null || subcommands.length == 0 || subcommands[0] == null || subcommands[0].length == 0)
					return false;
				int whichState = MesquiteInteger.unassigned;
				int whichCharacter = MesquiteInteger.unassigned;
				String text = null;
				Taxa taxa = nBlock.getDefaultTaxa();
				CharacterData data = nBlock.getDefaultCharacters();
				
				for (int i=0; i<subcommands[0].length; i++){
					String subC = subcommands[0][i];
					if ("TAXA".equalsIgnoreCase(subC)) {
						String token = subcommands[1][i];
						Taxa t = getProject().getTaxaLastFirst(token);
						if (t==null){
							int wt = MesquiteInteger.fromString(token);
							if (MesquiteInteger.isCombinable(wt))
								t = getProject().getTaxa(wt-1);
						}
						if (t!=null)
							taxa = t;
						else
							return false;
					}
					else if ("CHARACTERS".equalsIgnoreCase(subC)) {
						String token = subcommands[1][i];
						 CharacterData t = getProject().findCharacterMatrix(file, taxa, token);
						if (t!=null)
							data = t;
						else
							return false;
					}
					else if ("TEXT".equalsIgnoreCase(subC)) {
						text = subcommands[1][i];
					}
					else if ("STATE".equalsIgnoreCase(subC)) {
						String token = subcommands[1][i];
						whichState = MesquiteInteger.fromString(token);
						if (!MesquiteInteger.isCombinable(whichState))
							return false;
					}
					else if ("INTEGER".equalsIgnoreCase(subC)) {
						integer = MesquiteInteger.fromString(subcommands[1][i]);
					}
					else if ("CHARACTER".equalsIgnoreCase(subC)) {
						String token = subcommands[1][i];
						whichCharacter = MesquiteInteger.fromString(token);
						if (!MesquiteInteger.isCombinable(whichCharacter))
							return false;
						whichCharacter = CharacterStates.toInternal(whichCharacter);
					}
				}
				if (!MesquiteInteger.isCombinable(whichCharacter))
					return false;
				if (taxa !=null && text !=null && data !=null) {
					if (MesquiteInteger.isCombinable(whichState) && (data instanceof CategoricalData)) {
						((CategoricalData)data).setStateNote(whichCharacter, whichState, text);
						return true;
					}
				}
				
			}
			else if  (commandName.equalsIgnoreCase("ANS")){
				int integer = MesquiteInteger.unassigned;
				String name = null;
				stringPos.setValue(parser.getPosition());
				String[][] subcommands  = ParseUtil.getSubcommands(command, stringPos);
				if (subcommands == null || subcommands.length == 0 || subcommands[0] == null || subcommands[0].length == 0)
					return false;
				int whichState = MesquiteInteger.unassigned;
				int whichCharacter = MesquiteInteger.unassigned;
				String text = null;
				Taxa taxa = nBlock.getDefaultTaxa();
				CharacterData data = nBlock.getDefaultCharacters();
				
				for (int i=0; i<subcommands[0].length; i++){
					String subC = subcommands[0][i];
					if ("TAXA".equalsIgnoreCase(subC)) {
						String token = subcommands[1][i];
						Taxa t = getProject().getTaxa(token);
						if (t==null){
							int wt = MesquiteInteger.fromString(token);
							if (MesquiteInteger.isCombinable(wt))
								t = getProject().getTaxa(wt-1);
						}
						if (t!=null)
							taxa = t;
						else
							return false;
					}
					else if ("CHARACTERS".equalsIgnoreCase(subC)) {
						String token = subcommands[1][i];
						 CharacterData t = getProject().findCharacterMatrix(file, taxa, token);
						if (t!=null)
							data = t;
						else
							return false;
					}
					else if ("TEXT".equalsIgnoreCase(subC)) {
						text = subcommands[1][i];
					}
					else if ("STATE".equalsIgnoreCase(subC)) {
						String token = subcommands[1][i];
						whichState = MesquiteInteger.fromString(token);
						if (!MesquiteInteger.isCombinable(whichState))
							return false;
					}
					else if ("INTEGER".equalsIgnoreCase(subC)) {
						integer = MesquiteInteger.fromString(subcommands[1][i]);
					}
					else if ("CHARACTER".equalsIgnoreCase(subC)) {
						String token = subcommands[1][i];
						whichCharacter = MesquiteInteger.fromString(token);
						if (!MesquiteInteger.isCombinable(whichCharacter))
							return false;
						whichCharacter = CharacterStates.toInternal(whichCharacter);
					}
				}
				if (!MesquiteInteger.isCombinable(whichCharacter))
					return false;
				if (taxa !=null && text !=null && data !=null) {
					if (MesquiteInteger.isCombinable(whichState) && (data instanceof CategoricalData)) {
						((CategoricalData)data).setStateNote(whichCharacter, whichState, text);
						return true;
					}
				}
				
			}
		}
		return false;
	}
	/*.................................................................................................................*/
	public String getNexusCommands(MesquiteFile file, String blockName){ 
		if (blockName.equalsIgnoreCase("NOTES")) {
			String s ="";
			boolean found = false;
			MesquiteProject project = file.getProject();
			for (int i=0; i<project.getNumberCharMatrices(); i++){
				CharacterData d = getProject().getCharacterMatrix(i);
				if (d instanceof CategoricalData && d.getFile() == file){
					CategoricalData data = (CategoricalData)d;
					String dataSpec = "";
					if (project.getNumberTaxas()>1)
						dataSpec = " TAXA = " +  StringUtil.tokenize(data.getTaxa().getName()) + "";
					if (project.getNumberCharMatrices()>1 && MesquiteFile.okToWriteTitleOfNEXUSBlock(file, data))
						dataSpec += " CHARACTERS = " +  StringUtil.tokenize(data.getName()) + "";
					for (int ic = 0; ic<data.getNumChars(); ic++){
						for (int is = 0; is<=CategoricalState.maxCategoricalState; is++){
							String obj = data.getStateNote(ic, is);
							if (!StringUtil.blank(obj)){
								s += "\tTEXT  " + dataSpec + " CHARACTER = " + CharacterStates.toExternal(ic) + " STATE = " + is+ " TEXT = " + StringUtil.tokenize(obj) + ";" + StringUtil.lineEnding();
								found = true;
							}
						}
					}
				}
			}
			if (found)
				return s;
			else
				return null;
		}
		return null;
	}

	/*.................................................................................................................*/
    	 public String getName() {
		return "Manage categorical character matrices";
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Manages categorical data matrices (including read/write in NEXUS file)." ;
   	 }
}
	
/* ======================================================================== */
/** An object of this kind can be returned by getNexusCommandTest that will be stored in the modulesinfo vector and used
to search for modules that can read a particular command in a particular block.  (Much as the NexusBlockObject.)*/
class CategNexusCommandTest extends NexusCommandTest  {
	MesquiteInteger pos = new MesquiteInteger();
	/**returns whether or not the module can deal with command*/
	public boolean readsWritesCommand(String blockName, String commandName, String command){
		boolean b = (blockName.equalsIgnoreCase("NOTES")  && (commandName.equalsIgnoreCase("TEXT")));
		if (b){
			pos.setValue(0);
			String firstToken = ParseUtil.getFirstToken(command,  pos);
			
			String[][] subcommands  = ParseUtil.getSubcommands(command, pos);
			if (subcommands == null)
				return false;
			if (StringArray.indexOfIgnoreCase(subcommands, 0, "CHARACTER")<0)
				return false;
			if (StringArray.indexOfIgnoreCase(subcommands, 0, "STATE")<0)
				return false;
			return true;
		}
		if (blockName.equalsIgnoreCase("NOTES")  && (commandName.equalsIgnoreCase("ANS")))
			return true;
		return false;
	} 
}

