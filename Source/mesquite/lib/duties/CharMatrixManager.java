/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib.duties;

import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;


/* ======================================================================== */
/** Manages character data matrices of particular types.  Also reads and writes parts of CHARACTERS NEXUS block.  Different data
types have their own managers, so that the coordinating CharactersManager doesn't need to know about the details.
Example modules: "Manage Categorical char. matrices" (class ManageCategoricalChars); "Manage DNA/RNA matrices" (class ManageDNARNAChars) 
"Manage Continuous char. matrices" (class ManageContChars)*/

public abstract class CharMatrixManager extends MesquiteModule   {
	boolean logVerbose = true;

	public boolean getSearchableAsModule(){
		return false;
	}
	public Class getDutyClass() {
		return CharMatrixManager.class;
	}
	public String getDutyName() {
		return "Manager of Character data matrix, including read/write CHARACTERS block";
	}
	/** Process format command of NEXUS CHARACTERS block. Return the resulting CharacterData object*/
	public abstract mesquite.lib.characters.CharacterData processFormat(MesquiteFile file, Taxa taxa, String dataType, String formatCommand, MesquiteInteger stringPos, int numChars, String title, String fileReadingArguments);

	/** Process command in CHARACTERS block other than FORMAT or MATRIX command (e.g., CHARSTATELABELS command).  Should return true if processed
	successfully, false if not*/
	public abstract boolean processCommand(mesquite.lib.characters.CharacterData data, String commandName, String commandString);
	/** return string consisting of CHARACTERS block for NEXUS file.*/
	public abstract void writeCharactersBlock(mesquite.lib.characters.CharacterData data, CharactersBlock cB, MesquiteFile file, ProgressIndicator progIndicator);
	/** return true if can read the data type passed (token from NEXUS file is the string passed, e.g. "STANDARD", "CONTINUOUS") */
	public abstract boolean readsWritesDataType(String dataType);
	/** return true if can read the data class passed, e.g. CategoricalData.class) */
	public abstract boolean readsWritesDataType(Class dataClass);
	/** Return name of data class managed, for listing in menus and so on (e.g. "DNA data" .*/
	public abstract String getDataClassName();
	/** Returns data class managed, e.g. ContinuousData.*/
	public abstract Class getDataClass();
	/** return a new CharacterData object for the taxa indicated.*/
	public abstract mesquite.lib.characters.CharacterData getNewData(Taxa taxa, int numChars);

	//	public abstract void processMatrix(Taxa taxa, CharacterData data, String matrixString, boolean nameTaxa);

	/*.................................................................................................................*/
	public boolean isLogVerbose() {	
		return logVerbose;
	}
	/*.................................................................................................................*/
	public void setLogVerbose(boolean logVerbose) {	
		this.logVerbose = logVerbose;
	}
	/*.................................................................................................................*/
	/** Process the matrix, placing data into passed CharacterData object */
	public void processMatrix(Taxa taxa, mesquite.lib.characters.CharacterData data, Parser parser, int numChars, boolean nameTaxa, int firstTaxon, boolean makeNewTaxaIfNeeded, boolean fuse, MesquiteFile fileBeingRead) {
		if (data == null)
			return;
		if (taxa == null)
			taxa = data.getTaxa();

		String taxonName;
		parser.setLineEndingsDark(false);
		String d = parser.getNextToken(); //eating MATRIX
		int extraTaxon = -1;
		if (fuse){
			int numTaxa = taxa.getNumTaxa();
			extraTaxon = numTaxa;
			taxa.addTaxa(numTaxa-1, 1, false);
			data.addTaxa(numTaxa-1, 1);
			NameReference colorNameRef = NameReference.getNameReference("color");
			for (int it = extraTaxon; it<taxa.getNumTaxa(); it++)
				taxa.setAssociatedLong(colorNameRef, it, 8, true);
		}
		if (numChars<0 || !MesquiteInteger.isCombinable(numChars))
			numChars = data.getNumChars();
		int overwritingRule = -1;
		int PREFEREXISTING = 1;
		int PREFERINCOMING = 0;
		int SEPARATENOTOVERWRITE = 2;
		NameReference colorNameRef = NameReference.getNameReference("color");
		MesquiteTimer readTime = new MesquiteTimer();
		MesquiteTimer totalTime = new MesquiteTimer();

		totalTime.start();
		if (data.interleaved) {  //vvvvvvvv  INTERLEAVED #################################################################
			boolean warned = false;
			int[] currentCharacter = new int[taxa.getNumTaxa()];
			for (int i=firstTaxon; i<taxa.getNumTaxa(); i++) currentCharacter[i] =0;
			boolean done = false;
			parser.setLineEndingsDark(false);
			int it=firstTaxon;
			String deleteID = MesquiteTrunk.getUniqueIDBase() + taxa.getID();
			boolean toDelete = false;
			String problem = null;
			int lastTaxonNumber = -1;
			int[] overwrites = new int[taxa.getNumTaxa()];
			int UNTOUCHED = 0;
			int NOOVERWRITEBLANK = 1;
			int OVERWRITE = 2;
			int warnChimera = -1;

			while (!done && !(isEndLine(taxonName=parser.getNextToken()))) {
				parser.setLineEndingsDark(true);

				if (nameTaxa && it<taxa.getNumTaxa() && taxa.whichTaxonNumber(taxonName,false,false)>=0){ // this name already exists in taxon block
					if (fuse){
						if (toDelete || AlertDialog.query(containerOfModule(), "Duplicated taxa", "Some taxon names in the file being read are the same as some already in the project for the taxa block \"" + taxa.getName() + "\". Do you want to merge these taxa? (example of duplicated name: " + taxonName + ").  WARNING: if these taxa have data in matrices that you are fusing to existing matrices, then the taxon will take on the newly fused values. (cmm1)")){
							taxa.setTaxonNameNoWarnNoNotify(it, deleteID);
							toDelete = true;
						}
						else 
							taxa.setTaxonName(it,taxonName);
					} 
					else {
						if (!warned)
							MesquiteMessage.discreetNotifyUser("Duplicate taxon name in interleaved matrix (" + taxonName + "); file will not be properly read.  Data will be incorrect!");
						else {
							MesquiteMessage.warnUser("\n****  Duplicate taxon name in interleaved matrix (" + taxonName + "); file will not be properly read.  Data will be incorrect!");
							MesquiteModule.showLogWindow(true);
						}
						warned=true;
						if (nameTaxa)  
							taxa.setTaxonName(it,taxonName);
					}
				}
				else if (nameTaxa && it<taxa.getNumTaxa())  
					taxa.setTaxonName(it,taxonName);

				int whichTaxon = lastTaxonNumber+1;
				if (!taxonName.equalsIgnoreCase(taxa.getTaxonName(whichTaxon)))
					whichTaxon = taxa.whichTaxonNumberRev(taxonName, false);  //use reverse order lookup in case newly added taxa with identical names as previous
				if (whichTaxon == -1) {
					if (!warned) {
						MesquiteMessage.warnUser("Unrecognized taxon name: " + taxonName + " in matrix ");
						if (problem == null)
							problem = "Unrecognized taxon name: " + taxonName + " in matrix ";
						MesquiteModule.showLogWindow(true);
					}
					whichTaxon = it % taxa.getNumTaxa();
				}
				lastTaxonNumber = whichTaxon;
				int response = mesquite.lib.characters.CharacterData.OK;
				CommandRecord.tick("Reading character states for " + taxa.getTaxonName(whichTaxon));
				if (fuse){   //vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv    FUSE

					//first write into extra taxon, in case incoming states are all blank
					int ice = 0;
					for (int icc =0; icc< numChars; icc++)
						data.deassign(icc, extraTaxon);
					CharacterState csTEST = null;
					while (ice<numChars && response!=mesquite.lib.characters.CharacterData.EOL) {
						int prevPos = parser.getPosition();
						readTime.start();
						response = data.setStateQuickNexusReading(ice, extraTaxon, parser);
						readTime.end();
						csTEST = data.getCharacterState(csTEST, ice, extraTaxon);
						ice++;

						if (response == CharacterData.ERROR){
							problem = " taxon " + (whichTaxon +1);
							int curPos = parser.getPosition();
							parser.setPosition(prevPos);
							String chunk = parser.getPieceOfLine(10);
							parser.setPosition(curPos);
							problem += " (section of matrix as stored: \"" + chunk + "\")";
							data.problemReading = problem;
						}
					}


					if (data.hasDataForTaxon(extraTaxon)){  //something read in.  Now transfer to correct taxon
						int numRead = ice;
						if (response==mesquite.lib.characters.CharacterData.EOL)
							numRead--;
						boolean overwritten = false;
						int lastOverwritten = -1;
						ice = 0;
						CharacterState cs = null;
						while (currentCharacter[whichTaxon]<numChars && ice < numRead) {
							int ic = currentCharacter[whichTaxon];
							if (fuse && (!(data.isUnassigned(ic, whichTaxon) || data.isInapplicable(ic, whichTaxon))))
								overwritten = true;
							cs = data.getCharacterState(cs, ice, extraTaxon);
							data.setState(ic, whichTaxon, cs);
							if (overwritten)
								lastOverwritten = ic;
							currentCharacter[whichTaxon]++;
							ice++;
						}
						if (overwritten){
							markAsOverwritten(data, whichTaxon, fileBeingRead);
							for (int ic = lastOverwritten+1; ic<data.getNumChars(); ic++)
								data.deassign(ic, whichTaxon);
						}
						if (overwrites[whichTaxon] == NOOVERWRITEBLANK)
							warnChimera = whichTaxon;
						else
							overwrites[whichTaxon] = OVERWRITE;
					}
					else {
						if (overwrites[whichTaxon] == OVERWRITE)
							warnChimera = whichTaxon;
						else
							overwrites[whichTaxon] = NOOVERWRITEBLANK;
					}

				}   // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  FUSE
				else {
					while (whichTaxon<currentCharacter.length && currentCharacter[whichTaxon]<numChars && response!=mesquite.lib.characters.CharacterData.EOL) {
						int prevPos = parser.getPosition();
						int ic = currentCharacter[whichTaxon];
						readTime.start();
						response = data.setStateQuickNexusReading(ic, whichTaxon, parser);
						readTime.end();
						if (response == CharacterData.ERROR){
							problem = " taxon " + (whichTaxon +1) + ", character " + (ic + 1);
							int curPos = parser.getPosition();
							parser.setPosition(prevPos);
							String chunk = parser.getPieceOfLine(10);
							parser.setPosition(curPos);
							problem += " (section of matrix as stored: \"" + chunk + "\")";
							data.problemReading = problem;
						}
						if (response !=mesquite.lib.characters.CharacterData.EOL)
							currentCharacter[whichTaxon]++;
					}
				}

				if (it==0) 
					data.interleavedLength=currentCharacter[0];
				done = true;
				for (int ic=0; ic<currentCharacter.length; ic++) 
					if (currentCharacter[ic] != numChars)
						done = false;
				parser.setLineEndingsDark(false);
				it++;


			}
			if (fuse){
				taxa.deleteTaxa(extraTaxon, 1, false);
				data.deleteTaxa(extraTaxon, 1);
			}
			if (problem != null){
				discreetAlert("There was a problem reading the character matrix " + data.getName() + ".  It appears that the file is corrupt (" + problem + ").");
				MesquiteTrunk.errorReportedDuringRun = true;
			}
			if (warnChimera>=0){
				discreetAlert("WARNING: The merging process resulted in INCORRECT assignments of states for taxon " + (warnChimera+1) + " (" + taxa.getTaxonName(warnChimera) + ").  The problem may have affected other taxa as well.  It is recommended that you DO NOT SAVE THE FILE and instead merge again so as to prevent this.  To prevent this, ensure that the incoming (merging) data matrix is NOT INTERLEAVED.  Interleaving the matrix presents special problems in merging.");
				MesquiteTrunk.errorReportedDuringRun = true;
			}
			if (toDelete){
				for (it = taxa.getNumTaxa()-1; it>=0; it--)
					if (taxa.getTaxonName(it).equalsIgnoreCase(deleteID)){
						taxa.deleteTaxa(it, 1, false);
						data.deleteTaxa(it, 1);
					}
				taxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_DELETED));
			}
			taxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));
			parser.setLineEndingsDark(false);
		} //^^^^^^^^  INTERLEAVED #################################################################
		else {  //vvvvvvvv NOT INTERLEAVED #################################################################
			String deleteID = MesquiteTrunk.getUniqueIDBase() + taxa.getID();
			boolean toDelete = false;
			String problem = null;
			int lastTaxonNumber = -1;

			for (int it=firstTaxon; it<taxa.getNumTaxa() && !isEndLine(taxonName=parser.getNextToken()); it++) {

				boolean preserveNewTaxon = false;
				int whichTaxon = -1;
				if (fuse) {
					whichTaxon = taxa.whichTaxonNumberRev(taxonName, false);  //use reverse order lookup in case newly added taxa with identical names as previous
					if (whichTaxon<0){ //name only if absent
						if (nameTaxa)
							taxa.setTaxonName(it,taxonName);
					}
					else {
						if (nameTaxa){
							if (toDelete || AlertDialog.query(containerOfModule(), "Duplicated taxa", "Some taxon names in the file being read are the same as some already in the project for the taxa block \"" + taxa.getName() + "\". Do you want to merge these taxa? (example of duplicated name: " + taxonName + ").  WARNING: if these taxa have data in matrices that you are fusing to existing matrices, then the taxon will take on the newly fused values.  (cmm2)")){
								taxa.setTaxonNameNoWarnNoNotify(it, deleteID);
								toDelete = true;
							}
							else 
								taxa.setTaxonName(it,taxonName);
						}
					}
				}
				else if (nameTaxa){
					taxa.setTaxonName(it,taxonName);
				}
				whichTaxon = lastTaxonNumber+1;
				if (!taxonName.equalsIgnoreCase(taxa.getTaxonName(whichTaxon)))
					whichTaxon = taxa.whichTaxonNumberRev(taxonName, false);  //use reverse order lookup in case newly added taxa with identical names as previous

				if (whichTaxon == -1) {
					if (makeNewTaxaIfNeeded){
						int numTaxa = taxa.getNumTaxa();
						taxa.addTaxa(numTaxa-1, 1, false);
						data.addTaxa(numTaxa-1, 1);

						whichTaxon = numTaxa;
						Taxon taxon = taxa.getTaxon(whichTaxon);
						taxon.setName(taxonName);
					}
					else {
						MesquiteMessage.warnUser("Unrecognized taxon name: " + taxonName + " in matrix ");
						MesquiteModule.showLogWindow(true);
						whichTaxon = it;
					}
				}
				CommandRecord.tick("Reading character states for " + taxa.getTaxonName(whichTaxon));
				int ic=0;
				lastTaxonNumber = whichTaxon;
				if (fuse){ //vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv    FUSE
					//first write into extra taxon, in case incoming states are all blank
					ic = 0;
					for (int icc =0; icc< numChars; icc++)
						data.deassign(icc, extraTaxon);

					while (ic<numChars) {
						int prevPos = parser.getPosition();
						readTime.start();
						int response = data.setStateQuickNexusReading(ic++, extraTaxon, parser);
						readTime.end();
						if (response == CharacterData.ERROR){
							problem = " taxon " + (whichTaxon +1) + ", character " + (ic);
							int curPos = parser.getPosition();
							parser.setPosition(prevPos);
							String chunk = parser.getPieceOfLine(10);
							parser.setPosition(curPos);
							problem += " (section of matrix as stored: \"" + chunk + "\")";
							data.problemReading = problem;
						}
					}



					if (data.hasDataForTaxon(extraTaxon)){  //something read in.  Now transfer to correct taxon
						if (data.hasDataForTaxon(whichTaxon)){
							if (fuse && overwritingRule == -1)
								overwritingRule = AlertDialog.query(containerOfModule(), "Overwrite states?", "States for taxon " + (whichTaxon+1) + " (" + taxa.getTaxonName(whichTaxon) + ") are already in the matrix, as well as in the incoming matrix.  Do you want to overwrite the old states with the incoming, ignore the incoming states, or create a new taxon with the incoming states?\nNOTE: this will apply to all subsequent taxa with potential for overwriting for this matrix.", "Overwrite", "Ignore", "New Taxon");
							//	(PREFEREXISTING; PREFERINCOMING;  SEPARATENOTOVERWRITE)
							if (overwritingRule == SEPARATENOTOVERWRITE){  //don't deassign extra taxon, and invent new taxon and change value of extraTaxon
								preserveNewTaxon = true;
								taxa.setTaxonName(extraTaxon, taxa.getUniqueName(taxa.getTaxonName(whichTaxon)+" (for " + data.getName() + ", from file " + fileBeingRead.getFileName() + ")"));
								taxa.setAssociatedLong(colorNameRef, extraTaxon, 13, true);
								int numTaxa = taxa.getNumTaxa();
								taxa.addTaxa(numTaxa-1, 1, false);  //this is for future
								data.addTaxa(numTaxa-1, 1);
								taxa.setTaxonName(numTaxa, deleteID);
							}

						}
						if (!preserveNewTaxon && overwritingRule != PREFEREXISTING){
							boolean overwritten = false;
							int lastOverwritten = -1;
							ic = 0;
							CharacterState cs = null;
							while (ic<numChars) {
								if (!(data.isUnassigned(ic, whichTaxon) || data.isInapplicable(ic, whichTaxon)))
									overwritten = true;
								if (overwritten)
									lastOverwritten = ic;
								cs = data.getCharacterState(cs, ic, extraTaxon);
								data.setState(ic++, whichTaxon, cs);
							}
							if (overwritten){
								markAsOverwritten(data, whichTaxon, fileBeingRead);
								for (int icc = lastOverwritten+1; icc<data.getNumChars(); icc++)
									data.deassign(icc, whichTaxon);
							}
						}
					}
				} // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  FUSE
				else {
					while (ic<numChars) {
						int prevPos = parser.getPosition();
						readTime.start();
						int response = data.setStateQuickNexusReading(ic++, whichTaxon, parser);
						readTime.end();
						if (response == CharacterData.ERROR){
							problem = " taxon " + (whichTaxon +1) + ", character " + (ic);
							int curPos = parser.getPosition();
							parser.setPosition(prevPos);
							String chunk = parser.getPieceOfLine(10);
							parser.setPosition(curPos);
							problem += " (section of matrix as stored: \"" + chunk + "\")";
							data.problemReading = problem;
						}
					}
				}

				if (fuse && preserveNewTaxon)
						extraTaxon++;
						
			}
			if (fuse){
					taxa.deleteTaxa(extraTaxon, 1, false); 
					data.deleteTaxa(extraTaxon, 1);
			}
			if (problem != null){
				discreetAlert("There was a problem reading the character matrix " + data.getName() + ".  It appears that the file is corrupt (" + problem + ").");
				MesquiteTrunk.errorReportedDuringRun = true;
			}
			if (toDelete){
				for (int it = taxa.getNumTaxa()-1; it>=0; it--)
					if (taxa.getTaxonName(it).equalsIgnoreCase(deleteID)){
						taxa.deleteTaxa(it, 1, false);
						data.deleteTaxa(it, 1);
					}
				taxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_DELETED));
			}

			taxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));
		}  //^^^^^^^^ NOT INTERLEAVED #################################################################

		totalTime.end();
	}
	
	NameReference orRef = NameReference.getNameReference("Overwritten");
	private void markAsOverwritten(CharacterData data, int whichTaxon, MesquiteFile fileBeingRead){
		Associable tInfo = data.getTaxaInfo(true);

		Object obj = tInfo.getAssociatedObject(orRef, whichTaxon);
		String s = "";
		if (obj != null && obj instanceof String)
			s = "; " + (String)obj;
		s = "Overwritten by states from file " + fileBeingRead.getFileName() + " on " + getDateAndTime() + s;
		tInfo.setAssociatedObject(orRef, whichTaxon, s);

		MesquiteMessage.warnUser("NOTE: Previous states in matrix \"" + data.getName() + "\" for taxon " + (whichTaxon+1) + " (" + data.getTaxa().getTaxonName(whichTaxon) + ") overwritten using states from file " + fileBeingRead.getFileName());

		MesquiteModule.showLogWindow(true);
	}
	private boolean isEndLine(String tN){
		if (tN == null)
			return true;
		return tN.equals(";");
	}

	protected String getIDsCommand(mesquite.lib.characters.CharacterData data){
		String s = "";
		if (anyIDs(data, 0)){
			s += "IDS ";
			for (int it=0; it<data.getNumChars() && anyIDs(data, it); it++) {
				String id = data.getUniqueID(it);
				if (StringUtil.blank(id))
					s += " _ ";
				else
					s += id + " ";
			}
			s += ";" + StringUtil.lineEnding();
		}
		if (!StringUtil.blank(data.getUniqueID())&& !NexusBlock.suppressNEXUSIDS)
			s += "\tBLOCKID " + data.getUniqueID() + ";" + StringUtil.lineEnding();
		if (StringUtil.blank(s))
			return null;
		return s;
	}
	boolean anyIDs(mesquite.lib.characters.CharacterData data, int start){
		for (int ic=start; ic<data.getNumChars(); ic++) {
			String id = data.getUniqueID(ic);
			if (!StringUtil.blank(id))
				return true;
		}
		return false;
	}
}


