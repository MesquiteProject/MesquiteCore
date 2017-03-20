/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.meristic.ManageMeristicChars;
/*~~  */

import java.util.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.meristic.lib.*;

/* ======================================================================== 
Manages meristic data matrices  */
public class ManageMeristicChars extends CharMatrixManager {
	
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return 250;  
	}

	/*.................................................................................................................*/
	public Class getStateClass(){
		return MeristicState.class;
	}
	/*.................................................................................................................*/
	public Class getDataClass(){
		return MeristicData.class;
	}
	/*.................................................................................................................*/
	public  String getDataClassName(){
		return MeristicData.DATATYPENAME;
	}
	/*.................................................................................................................*/
	public  CharacterData getNewData(Taxa taxa, int numChars){
		return new MeristicData(this, taxa.getNumTaxa(), numChars, taxa);
	}
	/*.................................................................................................................*/
	public boolean readsWritesDataType(Class dataClass){
		return (dataClass == MeristicData.class);
	}
	/*.................................................................................................................*/
	public boolean readsWritesDataType(String dataType){
		return dataType.equalsIgnoreCase("Meristic");
	}
	public void fileReadIn(MesquiteFile f){
		ListableVector datas = getProject().getCharacterMatrices();
		for (int i=0; i<datas.size(); i++){
			CharacterData data = (CharacterData)datas.elementAt(i);
			if (data instanceof MeristicData && data.getFile() == f){
				if (data.getInapplicableSymbol() == '-')  //not permitted because it causes problems for negative numbers
					data.setInapplicableSymbol(MeristicState.inapplicableChar);
					((MeristicData)data).hasNegatives = false;  //reset this just in case
			}
		}
	}
	public CharacterData processFormat(MesquiteFile file, Taxa taxa, String dataType, String formatCommand, MesquiteInteger stringPos, int numChars, String title, String fileReadingArguments) {

		MeristicData data= null;
		//@@@@@@@@@@@@@@@@@@@@
		boolean fuse = parser.hasFileReadingArgument(fileReadingArguments, "fuseTaxaCharBlocks");

		if (fuse){
			String message = "In the file being imported, there is a matrix called \"" + title + "\". Mesquite will either fuse this matrix into the matrix you select below, or it will import that matrix as new, separate matrix.";
			data = (MeristicData)getProject().chooseData(containerOfModule(), null, taxa, getStateClass(), message,  true,"Fuse with Selected Matrix", "Add as New Matrix");
			if (data != null && numChars > data.getNumChars())
				data.addCharacters(data.getNumChars()-1, numChars - data.getNumChars(), false);
			if (data != null)
				file.characterDataNameTranslationTable.addElement(new MesquiteString(title, data.getName()), false);
		}
		if (data == null){
			data= (MeristicData)getNewData(taxa,numChars);
			if (fuse)
				data.setName(title);  //because otherwise titles are not set for fused matrices within ManageCharacters, since on the outside they don't know if it's new
		}
		else {
			if (fuse)
				data.setSuppressSpecssetReading(true);
			data.suppressChecksum = true;
		}
		data.interleaved = false;   //reset default in case this is fused

		//@@@@@@@@@@@@@@@@@@@@
		String tok = ParseUtil.getToken(formatCommand, stringPos);
		while (!tok.equals(";")) {
			if (tok.equalsIgnoreCase("ITEMS")) {
				tok = ParseUtil.getToken(formatCommand, stringPos); //getting rid of "="
				tok = ParseUtil.getToken(formatCommand, stringPos); //getting rid of "("
				tok = ParseUtil.getToken(formatCommand, stringPos); //getting first item name
				while (tok != null && !tok.equals(")")) {
					if ("unnamed".equalsIgnoreCase(tok))
						tok = null;
					data.establishItem(tok);
					tok = ParseUtil.getToken(formatCommand, stringPos); //getting item name
				}

			}
			else if (tok.equalsIgnoreCase("format")) { 
			}
			else if (tok.equalsIgnoreCase("TRANSPOSE")) {
				alert("Sorry, Transposed matrices of meristic characters can't yet be read");
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
				if (t!=null && t.length()==1) {
					data.setInapplicableSymbol(t.charAt(0));
					if (t.charAt(0) == '-' && !MesquiteThread.isScripting()){
						if (AlertDialog.query(containerOfModule(), "Has negatives?", "Does this meristic matrix \"" + title + "\" have negative values?.  " +
								"Explanation: A meristic matrix being read has GAP (inapplicable) symbol set to '-'.  This will be changed to 'x' to avoid possible confusion inapplicable and negative numbers.", "Yes, Negative values", "No"))
							data.hasNegatives = true;
					}
				}
			}
			else if (tok.equalsIgnoreCase("MATCHCHAR")) { 
				ParseUtil.getToken(formatCommand, stringPos); //eating up =
				String t = ParseUtil.getToken(formatCommand, stringPos);
				if (t!=null && t.length()==1)
					data.setMatchChar(t.charAt(0));
			}
			else {
				alert("Unrecognized token (\"" + tok+ "\") in FORMAT statement of meristic matrix; matrix will be stored as foreign, and not processed.");
				return null;
			}
			tok = ParseUtil.getToken(formatCommand, stringPos);
		}

		return data;
	}
	/*.................................................................................................................*/
	public boolean processCommand(CharacterData data, String commandName, String commandString) {
		if ("CHARSTATELABELS".equalsIgnoreCase(commandName)){
			MesquiteInteger startCharT = new MesquiteInteger(0);
			String cN = ParseUtil.getToken(commandString, startCharT); //eating up command name
			cN = ParseUtil.getToken(commandString, startCharT);
			while (!StringUtil.blank(cN) && !cN.equals(";") ) {
				int charNumber =MesquiteInteger.fromString(cN);
				String charName = (ParseUtil.getToken(commandString, startCharT));
				if (!charName.equals(",")) {
					data.setCharacterName(charNumber-1, charName);
					String stateName = ParseUtil.getToken(commandString, startCharT); // eat up slash
					int stateNumber = 0;
					while (stateName!=null && !stateName.equals(",") &&!stateName.equals(";")) { //skipping state names as they don't make any sense for meristic!
						stateName = (ParseUtil.getToken(commandString, startCharT));
						stateNumber++;
					}
				}
				cN = ParseUtil.getToken(commandString, startCharT);
			}
			return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	public String getCharStateLabels(CharacterData data, MesquiteFile file) {
		if (file.writeCharLabels){
			String csl = "CHARLABELS " + StringUtil.lineEnding();
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
				return csl + " ; " + StringUtil.lineEnding();
			else
				return "";
		}
		String csl = "CHARSTATELABELS " + StringUtil.lineEnding();
		boolean found = false;
		for (int i = 0; i<data.getNumChars(); i++) {
			String cslC="";
			if (i>0 && found)
				cslC += "," + StringUtil.lineEnding();
			cslC += "\t\t" + Integer.toString(i+1) + " ";    //i+1 to convert to 1 based
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
	public String getDataTypeString() {
		return "MERISTIC";
	}
	/*.................................................................................................................*/
	public void writeCharactersBlock(CharacterData data, CharactersBlock cB, MesquiteFile file, ProgressIndicator progIndicator){
		MeristicData cData = (MeristicData)data;
		//StringBuffer blocks = new StringBuffer(cData.getNumChars()*cData.getNumTaxa()*10*cData.getNumItems());
		StringBuffer line = new StringBuffer(cData.getNumChars()*10*cData.getNumItems());
		file.write("BEGIN CHARACTERS");
		if (data.getAnnotation()!=null && !file.useSimplifiedNexus) 
			file.write("[!" + StringUtil.tokenize(data.getAnnotation()) + "]");
		
		file.write(";" + StringUtil.lineEnding());
		if (MesquiteFile.okToWriteTitleOfNEXUSBlock(file, cData)){
			file.write("\tTITLE  " + StringUtil.tokenize(cData.getName()) + ";" + StringUtil.lineEnding());
		}

		if (MesquiteFile.okToWriteTitleOfNEXUSBlock(file, cData.getTaxa()) && getProject().getNumberTaxas()>1){ //should have an isUntitled method??
			file.write("\tLINK TAXA = " +  StringUtil.tokenize(cData.getTaxa().getName()) + ";" + StringUtil.lineEnding());
		}
		file.write("\tDIMENSIONS ");
		int numCharsToWrite;
		if (file.writeExcludedCharacters)
			numCharsToWrite = cData.getNumChars();
		else
			numCharsToWrite = cData.getNumCharsIncluded();
		file.write(" NCHAR=" + numCharsToWrite);
		file.write(";" + StringUtil.lineEnding());
		file.write("\tFORMAT");
		file.write(" DATATYPE = " + getDataTypeString());
		if (cData.getNumItems()>0 && !(cData.getNumItems()==1 && cData.getItemName(0) ==null)) {
			file.write(" ITEMS = (");
			for (int i=0; i<cData.getNumItems(); i++) {
				String iName = cData.getItemName(i);
				if (iName == null)
					file.write("unnamed ");
				else
					file.write(iName + " ");
			}
			file.write(") ");
		}
		file.write(" GAP = " + data.getInapplicableSymbol() + " MISSING = " + data.getUnassignedSymbol());
		file.write(";" + StringUtil.lineEnding());
		if (data.isLinked() && !file.useSimplifiedNexus  && !file.useConservativeNexus){
			file.write("\tOPTIONS ");
			Vector ds = data.getDataLinkages();
			for (int i = 0; i<ds.size(); i++) {
				file.write(" LINKCHARACTERS = ");
				file.write(StringUtil.tokenize(((CharacterData)ds.elementAt(i)).getName()));
			}
			file.write(";" + StringUtil.lineEnding());
		}
		file.write(getCharStateLabels(cData, file));
		file.write("\tMATRIX" + StringUtil.lineEnding());
		String taxonName="";
		int maxNameLength = cData.getTaxa().getLongestTaxonNameLength();
		for (int it=0; it<cData.getTaxa().getNumTaxa(); it++) {
			if ((data.someApplicableInTaxon(it, false)|| file.writeTaxaWithAllMissing) && (!file.writeOnlySelectedTaxa || data.getTaxa().getSelected(it))){
				taxonName = cData.getTaxa().getTaxon(it).getName();
				if (file.useStandardizedTaxonNames)
					taxonName = "t" + it;
				else
					taxonName = StringUtil.simplifyIfNeededForOutput(taxonName,file.simplifyNames);
				if (taxonName!=null) {
					file.write("\t"+ taxonName);
					for (int i = 0; i<(maxNameLength-taxonName.length()+2); i++)
						file.write(" ");
				}
				line.setLength(0);
				for (int ic=0;  ic<cData.getNumChars(); ic++)  {
					if (data.isCurrentlyIncluded(ic) || file.writeExcludedCharacters) {
						line.append(' ');
						cData.statesIntoNEXUSStringBuffer(ic, it, line);
					}
				}
				file.write(line.toString());
				file.write(StringUtil.lineEnding());
			}
		}
		file.write(StringUtil.lineEnding()+ ";");
		file.write( StringUtil.lineEnding());
		if (!file.useSimplifiedNexus  && !file.useConservativeNexus && !NexusBlock.suppressNEXUSIDS){
			String idsCommand = getIDsCommand(data);
			if (!StringUtil.blank(idsCommand))
				file.write("\t" + idsCommand + StringUtil.lineEnding());
		}
		if (cB != null) file.write(cB.getUnrecognizedCommands() + StringUtil.lineEnding());
		file.write("END");
		file.write(";" + StringUtil.lineEnding());

		//file.write( blocks.toString());
	}


	/*.................................................................................................................*/
    	 public String getName() {
		return "Manage Meristic char. matrices";
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Manages meristic data matrices (including read/write in NEXUS file)." ;
   	 }
}
	
	

