/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.categ.ManageProteinChars;
/*~~  */

import java.util.*;
import java.awt.*;
import java.io.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;

/* ======================================================================== 
Manages Protein sequence data matrices  */
public class ManageProteinChars extends CategMatrixManager {
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
  	 }
	public Class getDataClass(){
		return ProteinData.class;
	}
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public  String getDataClassName(){
		return ProteinData.DATATYPENAME;
	}
	/*.................................................................................................................*/
	public  CharacterData getNewData(Taxa taxa, int numChars){
		return new ProteinData(this, taxa.getNumTaxa(), numChars, taxa);
	}
	/*.................................................................................................................*/
	public boolean readsWritesDataType(Class dataClass){
		return (dataClass == ProteinData.class);
	}
	/*.................................................................................................................*/
	public boolean readsWritesDataType(String dataType){
		return dataType.equalsIgnoreCase("Protein");
	}
	/*.................................................................................................................*/
	public CharacterData processFormat(MesquiteFile file, Taxa taxa, String dataType, String formatCommand, MesquiteInteger stringPos, int numChars, String title, String fileReadingArguments) {
		if (taxa==null) {
			MesquiteMessage.warnProgrammer("Error: taxa null in processFormat  in " + getName());
			return null;
		}
		MesquiteProject proj=null;
		if (file!=null)
			proj = file.getProject();
		CategoricalData data = null;
		//@@@@@@@@@@@@@@@@@@@@
		boolean fuse = parser.hasFileReadingArgument(fileReadingArguments, "fuseTaxaCharBlocks");

		if (fuse){
			String message = "In the file being imported, there is a matrix called \"" + title + "\". Mesquite will either fuse this matrix into the matrix you select below, or it will import that matrix as new, separate matrix.";
			data = (ProteinData)getProject().chooseData(containerOfModule(), null, taxa, ProteinState.class, message,  true,"Fuse with Selected Matrix", "Add as New Matrix");
			if (data != null && numChars > data.getNumChars())
				data.addCharacters(data.getNumChars()-1, numChars - data.getNumChars(), false);
		}
		if (data == null) {
			data= new ProteinData(this, taxa.getNumTaxa(), numChars, taxa);
			if (fuse)
				data.setName(title);  //because otherwise titles are not set for fused matrices within ManageCharacters, since on the outside they don't know if it's new
		}
		else {
			if (fuse)
				data.setSuppressSpecssetReading(true);
			data.suppressChecksum = true;
		}
		//@@@@@@@@@@@@@@@@@@@@
		String tok = ParseUtil.getToken(formatCommand, stringPos);
		while (!tok.equals(";")) {
			if (tok.equalsIgnoreCase("TRANSPOSE")) {
				alert("Sorry, Transposed matrices of protein data can't yet be read");
				return null;
			}
			else if (tok.equalsIgnoreCase("interleave")) {
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
			else if (tok.equalsIgnoreCase("format")) { 
			}
			else if (tok.equalsIgnoreCase("MISSING")) { 
				ParseUtil.getToken(formatCommand, stringPos); //eating up =
				String t = ParseUtil.getToken(formatCommand, stringPos);
				if (t!=null && t.length()==1)
					data.setUnassignedSymbol(t.charAt(0));
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
			else if (tok.equalsIgnoreCase("DATATYPE")) { 
				ParseUtil.getToken(formatCommand, stringPos); //eating up =
				ParseUtil.getToken(formatCommand, stringPos); //eating up datatype
				//alert("Error in NEXUS file format: DATATYPE subcommand must appear as the first item in the FORMAT command");
				//return null;
			}
			else if (tok.equalsIgnoreCase("SYMBOLS")) { 
				logln("\n*******\nNEXUS file format warning: SYMBOLS command with PROTEIN data has undefined behavior in NEXUS format; file may not be read as you expect.\n*******\n");
				ParseUtil.getToken(formatCommand, stringPos); //eating up =
				String t = ParseUtil.getToken(formatCommand, stringPos); //eating up "
				if (t!=null && t.equalsIgnoreCase("\"")){
					t = ParseUtil.getToken(formatCommand, stringPos); //getting next token
					int count = ProteinState.maxProteinState+1;  //add them to the end
					while (t!=null && !t.equals("\"") &&  !t.equals(";")){
						for (int i = 0; i<t.length(); i++){ //process entire string
							char c = t.charAt(i);
							if (!StringUtil.whitespace(c, null)) {
								if (count<=CategoricalState.maxCategoricalState)
									data.setSymbol(count, c);
								count++;
							}
						}
						t = ParseUtil.getToken(formatCommand, stringPos); //eating up "
					}
				}
			}
			else {
				alert("Unrecognized token (\"" + tok+ "\") in FORMAT statement of Protein matrix; matrix will be stored as foreign, and not processed.");
				return null;
			}
			tok = ParseUtil.getToken(formatCommand, stringPos);
		}
		return data;
	}
	/*.................................................................................................................*
	/*.................................................................................................................*/
	public boolean processCommand(CharacterData data, String commandName, String commandString) {
		if (commandName.equalsIgnoreCase("CHARSTATELABELS")){
			String cN = parser.getFirstToken(commandString); //eating up command name
			cN = parser.getNextToken();
			String charName;
			String stateName;
			while (!StringUtil.blank(cN) && !cN.equals(";") ) {
				int charNumber =MesquiteInteger.fromString(cN);
				charName = parser.getNextToken();  
				if (!charName.equals(",") && !charName.equals(";")) {
					if (!charName.equals("/")) {   // there is a character name
						data.setCharacterName(charNumber-1, charName);
						stateName = parser.getNextToken(); // eating up slash
					}
					else   // there is no character name, only a slash
						stateName = "";
					while (stateName!=null && !stateName.equals(",") && !stateName.equals(";")) {
						stateName = parser.getNextToken();
					}
				}
				if (charName.equals(";"))    // we've encountered the semicolon
					cN = ";";
				else
					cN = parser.getNextToken();
			}
			return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public String getCharStateLabels(CharacterData data) {
		String csl = "CHARSTATELABELS " + StringUtil.lineEnding();
		boolean found = false;
		String end = " "; //
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
		String endLine = ";" + StringUtil.lineEnding();
		StringBuffer blocks = new StringBuffer(5000);
		if (file!=null && file.useDataBlocks)
			blocks.append("BEGIN DATA");
		else
			blocks.append("BEGIN CHARACTERS");
		if (data.getAnnotation()!=null && !file.useSimplifiedNexus) 
			file.write("[!" + StringUtil.tokenize(data.getAnnotation()) + "]");
		blocks.append(endLine);
		if (MesquiteFile.okToWriteTitleOfNEXUSBlock(file, data)){
			blocks.append("\tTITLE  ");
			blocks.append( StringUtil.tokenize(data.getName()));
			blocks.append(endLine);
		}
		if (MesquiteFile.okToWriteTitleOfNEXUSBlock(file, data.getTaxa())&& getProject().getNumberTaxas()>1){ //��� should have an isUntitled method??
			blocks.append("\tLINK TAXA = ");
			blocks.append(StringUtil.tokenize(data.getTaxa().getName()));
			blocks.append(endLine);
		}
		blocks.append("\tDIMENSIONS ");
		if (file!=null && file.useSimplifiedNexus && file.useDataBlocks){
			int numTaxaToWrite = data.getNumTaxa();
			if (!file.writeTaxaWithAllMissing)
				numTaxaToWrite = data.numTaxaWithSomeApplicable(false, file.writeOnlySelectedTaxa, file.writeExcludedCharacters, file.fractionApplicable);
			else if (file.writeOnlySelectedTaxa)
				numTaxaToWrite = data.numSelectedTaxa();
			blocks.append(" NTAX=" + numTaxaToWrite);
		}
		int numCharsToWrite;
		if (file.writeExcludedCharacters)
			numCharsToWrite = data.getNumChars();
		else
			numCharsToWrite = data.getNumCharsIncluded();
		blocks.append(" NCHAR=" + numCharsToWrite);
		blocks.append(endLine);
		blocks.append("\tFORMAT");
		blocks.append(" DATATYPE = Protein");
		if (data.interleaved && file.interleaveAllowed)
			blocks.append(" INTERLEAVE");
		blocks.append(" GAP = " + data.getInapplicableSymbol() + " MISSING = " + data.getUnassignedSymbol());
		blocks.append(endLine);
		if (data.isLinked() && !file.useSimplifiedNexus  && !file.useConservativeNexus){
			blocks.append("\tOPTIONS ");
			Vector ds = data.getDataLinkages();
			for (int i = 0; i<ds.size(); i++) {
				blocks.append(" LINKCHARACTERS = ");
				blocks.append(StringUtil.tokenize(((CharacterData)ds.elementAt(i)).getName()));
			}
			blocks.append(endLine);
		}
		if (!file.useSimplifiedNexus) //1. 12 so that MrBayes won't choke on exported file
			blocks.append(getCharStateLabels(data));

		writeNexusMatrix(data, cB, blocks, file, progIndicator);

		if (!file.useSimplifiedNexus  && !file.useConservativeNexus && !NexusBlock.suppressNEXUSIDS){
			String idsCommand = getIDsCommand(data);
			if (!StringUtil.blank(idsCommand))
				blocks.append("\t" + idsCommand + StringUtil.lineEnding());
		}
		if (cB != null)
			blocks.append(cB.getUnrecognizedCommands() + StringUtil.lineEnding());
		blocks.append("END");
		
		blocks.append(";" + StringUtil.lineEnding());

		file.writeLine( blocks.toString());
	}


	/*.................................................................................................................*/
    	 public String getName() {
		return "Manage Protein matrices";
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Manages Protein data matrices (including read/write in NEXUS file)." ;
   	 }
}
	
	

