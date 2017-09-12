/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.ManageDATAblock;
/*~~  */

import java.util.*;
import java.awt.*;
import java.io.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/** Interprets the DATA block of a NEXUS file (this is more of an importer than manager; see ManageTaxa and ManageCharacters for the full management system for taxa and character blocks)*/
public class ManageDATAblock extends MesquiteModule {
	private CharactersManager charTask;
	private TaxaManager taxaTask;
	
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		taxaTask = (TaxaManager)findElementManager(Taxa.class);
		charTask = (CharactersManager)findElementManager(CharacterData.class);
		return taxaTask!=null && charTask!=null;
	}
	
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	public Class getDutyClass(){
		return ManageDATAblock.class;
	}
	/*.................................................................................................................*/
	public NexusBlockTest getNexusBlockTest(){ return new DATABlockTest();}
	/*.................................................................................................................*/
	public CharacterData processFormat(MesquiteFile file, Taxa taxa, String formatCommand, int numChars, String title, String fileReadingArguments) {
		return charTask.processFormat(file, taxa, formatCommand, numChars, title, fileReadingArguments);
	}
	/*.................................................................................................................*/
	public NexusBlock readNexusBlock(MesquiteFile file, String name, FileBlock block, StringBuffer blockComments, String fileReadingArguments){
		if (block == null || file == null)
			return null;
		CharacterData data=null;
		Parser commandParser = new Parser();
		commandParser.setString(block.toString());
		MesquiteInteger startCharC = new MesquiteInteger(0);
		String dataTitle=getProject().getCharacterMatrices().getUniqueName("Character Matrix");
		String taxaTitle=getProject().getCharacterMatrices().getUniqueName("Taxa");
		int firstTaxon = 0;
		boolean fuse = parser.hasFileReadingArgument(fileReadingArguments, "fuseTaxaCharBlocks");
		
		String commandName;
		Taxa taxa= null;
		int numChars=0;
		NexusBlock b = null;
		int previousPos = 0;
		while (!commandParser.blankByCurrentWhitespace(commandName=commandParser.getNextCommandName(startCharC))) {
			if (commandName.equalsIgnoreCase("DIMENSIONS")) { 
				parser.setString(commandParser.getNextCommand(startCharC)); 
				int numTaxa = MesquiteInteger.fromString(parser.getTokenNumber(4));
				numChars = MesquiteInteger.fromString(parser.getTokenNumber(7));
				logln("   " + MesquiteInteger.toString(numTaxa) + " taxa, " + MesquiteInteger.toString(numChars) + " characters.");
				if (!MesquiteInteger.isCombinable(numTaxa) || numTaxa<0){
					alert("Sorry, the DIMENSIONS statement of the DATA block appears to be misformatted.  The number of taxa is not validly specified. File reading will fail.");
					return null;
				}
				if (!MesquiteInteger.isCombinable(numChars) || numChars<0){
					alert("Sorry, the DIMENSIONS statement of the DATA block appears to be misformatted.  The number of characters is not validly specified. File reading will fail.");
					return null;
				}
				if (fuse){
					String message = "In the file being imported, there is a taxa block. Mesquite will either fuse this imported taxa block into the taxa block you select below, or it will import that taxa block as new, separate taxa block.";
					taxa = getProject().chooseTaxa(containerOfModule(), message, true, "Fuse with Selected Taxa Block", "Add as New Taxa Block");
					if (taxa != null){
						firstTaxon = taxa.getNumTaxa();
						taxa.addTaxa( taxa.getNumTaxa()-1, numTaxa, true);
					}
				}
				if (taxa == null)
					taxa = taxaTask.makeNewTaxa(taxaTitle, numTaxa, false);
			}
			else if (commandName.equalsIgnoreCase("TITLE")) {
				parser.setString(commandParser.getNextCommand(startCharC)); 
				dataTitle = parser.getTokenNumber(2);
				logln("Reading CHARACTERS block " + dataTitle);
			}
			else if (commandName.equalsIgnoreCase("FORMAT")) {
				data = processFormat(file, taxa, commandParser.getNextCommand(startCharC), numChars, dataTitle, fileReadingArguments);
				if (data==null) {
					alert("Sorry, the DATA block could not be read, possibly because it is of an unrecognized format.  You may need to activate or install other modules that would allow you to read the data block");
					return null;
				}
				taxa.addToFile(file, getProject(), taxaTask);
				if (!fuse)
					data.setName(dataTitle);
				b = data.addToFile(file, getProject(), null);
				}
			else if (commandName.equalsIgnoreCase("MATRIX")) {
				if (data==null) {
					alert("Error in NEXUS file:  Matrix without FORMAT statement");
					return null;
				}
				else if (data.getMatrixManager()!=null) {
					if (data.interleaved) {
						startCharC.setValue(previousPos);
						commandParser.setLineEndingsDark(true);
						commandParser.setPosition(previousPos);
						commandParser.getNextToken();
					}
					boolean wassave = data.saveChangeHistory;
					data.saveChangeHistory = false;
					if (data.interleaved)
						logln("  reading interleaved DATA block");

					data.getMatrixManager().processMatrix(taxa, data, commandParser, numChars, true, firstTaxon, false, fuse, file); 
					if (data.interleaved) 
						commandParser.setLineEndingsDark(false);
					startCharC.setValue(commandParser.getPosition());
					String token = commandParser.getNextCommand();
					if (token == null || !token.equals(";"))
						commandParser.setPosition(startCharC.getValue());
					data.saveChangeHistory = wassave;
				}
				else {
					return null;
				}
			}
			else if (commandName.equalsIgnoreCase("CHARLABELS")) {
				parser.setString(commandParser.getNextCommand(startCharC)); 
				parser.getNextToken();
				String cN = parser.getNextToken();
				int charNumber = 0;
				while (!StringUtil.blank(cN) && !cN.equals(";") ) {
					data.setCharacterName(charNumber++, cN);
					cN = parser.getNextToken();
				}
			}
			else if (!(commandName.equalsIgnoreCase("BEGIN") || commandName.equalsIgnoreCase("END")  || commandName.equalsIgnoreCase("ENDBLOCK"))) {
				boolean success = false;
				String commandString = commandParser.getNextCommand(startCharC);
				if (data !=null && data.getMatrixManager()!=null)
					success = data.getMatrixManager().processCommand(data, commandName, commandString);
				if (!success && b != null) 
					readUnrecognizedCommand(file,b, name, block, commandName, commandString, blockComments, null);
			}
			else
				commandParser.getNextCommand(startCharC); //eating up the full command
			previousPos = startCharC.getValue();
		}
		if (!fuse && StringUtil.blank(dataTitle))
			data.setName(getProject().getCharacterMatrices().getUniqueName("Untitled (" + data.getDataTypeName() + ")"));
		if (data != null && blockComments!=null && blockComments.length()>0)
			data.setAnnotation(blockComments.toString(), false);
		if (data !=null) {
			data.resetCellMetadata();
		}
		file.setCurrentTaxa(taxa);
		file.setCurrentData(data);
		return b;
	}

	/*.................................................................................................................*/
    	 public String getName() {
		return "Read DATA blocks";
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Coordinates the reading of a DATA block in NEXUS file." ;
   	 }
}
	
	
/* ======================================================================== */
class DATABlockTest extends NexusBlockTest  {
	public DATABlockTest () {
	}
	public  boolean readsWritesBlock(String blockName, FileBlock block){ //returns whether or not can deal with block
		return blockName.equalsIgnoreCase("DATA");
	}
}


