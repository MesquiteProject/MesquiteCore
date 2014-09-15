/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.ConcatenateMatrices; 

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class ConcatenateMatrices extends DataUtility { 
	CharacterData data;
	/*.................................................................................................................*/
	public String getName() {
		return "Concatenate Other Matrix";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Concatenates matrix onto one in data editor.  Assumptions like weights and character models are NOT transferred.  For categorical data, state names are not included.  For continuous data, new items may need to be created to accommodate differences in items between the matrices.";
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
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
	/** returns number of data sets of a given data class (CharacterState subclass is passed) belonging to given taxa.  If permitTaxaMatching is true, 
	 * considers taxa equal if names coincide*/
	private int  getNumberCharMatricesPAMT(Taxa taxa, Object dataClass) {   //MesquiteProject
		ListableVector datas = getProject().getCharacterMatrices();
		int count=0;
		for (int i=0; i<datas.size(); i++) { 
			mesquite.lib.characters.CharacterData data = (mesquite.lib.characters.CharacterData)datas.elementAt(i);
			if (!data.isDoomed() && (taxa == null || taxa.equals(data.getTaxa(), true)) && (dataClass==null || getProject().compatibleMatrix(dataClass, data)))
				count++;
		}
		return count;
	}

	/*.................................................................................................................*/
	/** returns the jth of data sets belonging to a given file*, permitting matrices with different but equivalent taxa */
	private mesquite.lib.characters.CharacterData getCharacterMatrixPAMT(Taxa taxa, Object dataClass, int j) {  //MesquiteProject
		ListableVector datas = getProject().getCharacterMatrices();
		int count=0;
		for (int i=0; i<datas.size(); i++) { 
			mesquite.lib.characters.CharacterData data = (mesquite.lib.characters.CharacterData)datas.elementAt(i);

			if (!data.isDoomed() && (taxa == null || taxa.equals(data.getTaxa(), true)) && (dataClass == null || getProject().compatibleMatrix(dataClass, data))) {
				if (count==j) {
					return data;
				}
				count++;
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	/** returns a data matrix that is of the same class as that passed, but is a different one.*/  //ConcatenateMatrices
	public mesquite.lib.characters.CharacterData getDifferentButCompatibleMatrix(mesquite.lib.characters.CharacterData data, boolean permitOtherTaxa,  String messageIfNoneFound, String messageForChoice, boolean subclassOK) {
		if (data == null)
			return null;
		else {
			Taxa taxa = data.getTaxa();
			if (permitOtherTaxa)   // in matching methods, setting taxa to null becomes permissive
				taxa = null; 
			//taxa.prepareEqualTaxaMessage();
			int numDatasTotal = getNumberCharMatricesPAMT(taxa, null);//PERMITs use of matrix with different but equivalent taxa
			//String equalTaxaMessage = taxa.getEqualTaxaMessage();
			//taxa.turnOffEqualTaxaMessage();		
			String s ="";
			boolean allDataIsCurrentData = true;
			boolean allDataIsWrongClass = true;
			boolean someDataAreLinked = false;
			if (numDatasTotal==0)
				s += " There are no matrices for these taxa.";
			int numDiffComparable = numDatasTotal;
			Vector v = new Vector();
			//this seems to retain remnants of a more relaxed system that would allow matrices connected to other but equivalent Taxa
			for (int i = 0; i<numDatasTotal; i++) {
				mesquite.lib.characters.CharacterData pData =getCharacterMatrixPAMT(taxa, null, i); //PERMITs use of matrix with different but equivalent taxa

				if (pData != null) {
					if (pData== data) //same matrix; don't count
						numDiffComparable--;
					else {
						allDataIsCurrentData = false;   // at least one data matrix with matching taxa is not the current data matrix
						if (!((subclassOK && (data.getClass().isAssignableFrom(pData.getClass()))) || pData.getClass() == data.getClass())) //different type of data; don't count
							numDiffComparable--;
						else { 
							allDataIsWrongClass = false;   // at least one novel data matrix with matching taxa is of the correct dataClass
							if (pData.isLinked(data)){ //is linked; don't count
								numDiffComparable--;
								someDataAreLinked = true; 
							}
							else 
								v.addElement(pData);
						}

					}
				}
			}		
			if (numDiffComparable<=0) {
				if (!MesquiteThread.isScripting() && messageIfNoneFound!=null) {
					if (numDatasTotal>=1) {
						if (allDataIsCurrentData) {
							if (numDatasTotal==1) {
								s += " There are no other matrices for these taxa. "; //+ equalTaxaMessage;
							}
							else
								s += " All matrices are the same as the current one."; //one shouldn't get here!
						}
						else if (allDataIsWrongClass)
							s += " All other matrices are of a different class of data.";
						else if (someDataAreLinked)
							s += " The only comparable matrices are linked.";
					}
					MesquiteTrunk.mesquiteTrunk.alert(messageIfNoneFound + "\n\nProblem: "+s);
				}
				return null;
			}
			else {
				Listable[] matrices = new Listable[v.size()];
				for (int i = 0; i<v.size(); i++) {
					mesquite.lib.characters.CharacterData pData =(mesquite.lib.characters.CharacterData)v.elementAt(i);
					matrices[i]=pData;
				}
				mesquite.lib.characters.CharacterData oData = (mesquite.lib.characters.CharacterData)ListDialog.queryList(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Choose matrix", messageForChoice, MesquiteString.helpString, matrices, 0);
				return oData;
			}
		}
	}
	
	boolean concatenate(CharacterData data, CharacterData oData, boolean notify){
		if (oData==null)
			return false;
		if (oData.isLinked(data) || data.isLinked(oData)) {
			discreetAlert( "Sorry, those two matrices cannot be concatenated because they are linked");
			return false;
		}
		CommandRecord.tick("Concatenating matrices");
		if (!oData.getTaxa().equals(data.getTaxa(), true, true)){
			Taxa oTaxa = oData.getTaxa();
			Taxa taxa = data.getTaxa();
			boolean extra = false;
			for (int oit = 0; oit<oTaxa.getNumTaxa() && !extra; oit++)
				if (taxa.findEquivalentTaxon(oTaxa, oit)<0)
					extra = true;
			//different taxa block, with different names.  Offer to add names
			if (extra){
				if (AlertDialog.query(containerOfModule(), "Import taxa from other matrix?", "The matrix you are concatenating to this one is based on a different block of taxa, and includes taxa not in this matrix.  Do you want to add these taxa to this matrix before concatenating?")){
					String names = "";
					
					for (int oit = 0; oit<oTaxa.getNumTaxa(); oit++){
						if (taxa.findEquivalentTaxon(oTaxa, oit)<0){
							taxa.addTaxa(taxa.getNumTaxa(), 1, false);
							taxa.equalizeTaxon(oTaxa, oit, taxa.getNumTaxa()-1);
							names += taxa.getTaxonName(taxa.getNumTaxa()-1) + "\n";
							CommandRecord.tick("Added taxon " + taxa.getTaxonName(taxa.getNumTaxa()-1));
							
						}
					}
					if (!StringUtil.blank(names)){
						logln("Added to taxa block were:\n" + names);
						taxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED, null,null));
					}
				}
			}

		}
		int origNumChars = data.getNumChars();
		data.addParts(data.getNumChars()+1, oData.getNumChars());
		CharacterPartition partition = (CharacterPartition) data.getCurrentSpecsSet(CharacterPartition.class);
		if (partition==null) // let's give the original ones a group
			data.setToNewGroup(data.getName(), 0, origNumChars-1, this);  //set group
		data.setToNewGroup(oData.getName(), origNumChars, data.getNumChars()-1, this);  //set group
		data.addInLinked(data.getNumChars()+1, oData.getNumChars(), true);
		CharacterState cs = null;
		for (int ic = 0; ic<oData.getNumChars(); ic++){
			CommandRecord.tick("Copying character " + (ic+1) + " in concatenation");
			data.equalizeCharacter(oData, ic, ic+origNumChars);
		}
		if (notify)
			data.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED, new int[] {origNumChars, oData.getNumChars()}));
		return true;
}
	
	/** Called to operate on the data in all cells.  Returns true if data altered*/
	public boolean operateOnData(CharacterData data){ 
		this.data = data;
		CharacterData oData =getDifferentButCompatibleMatrix(data, true,  "Sorry, there are no other compatible data matrices available for concatenation.  If the other matrix is in another file, open the file as a linked file before attempting to concatenate.", "Concatenate which matrix?", true);
		//modified v. 1.05 to allow matrix of a subclass to be merged into a matrix (e.g. DNA into categorical).
		//modivied v. 1.2 to allow enlargment of taxa block from new taxa in concatenated matrix
		if (oData==null)
			return false;
		boolean success = data.concatenate(oData, true, true, true);
		/*
		if (oData.isLinked(data) || data.isLinked(oData)) {
			discreetAlert( "Sorry, those two matrices cannot be concatenated because they are linked");
			return false;
		}
		CommandRecord.tick("Concatenating matrices");
		if (!oData.getTaxa().equals(data.getTaxa(), true, true)){
			Taxa oTaxa = oData.getTaxa();
			Taxa taxa = data.getTaxa();
			boolean extra = false;
			for (int oit = 0; oit<oTaxa.getNumTaxa() && !extra; oit++)
				if (taxa.findEquivalentTaxon(oTaxa, oit)<0)
					extra = true;
			//different taxa block, with different names.  Offer to add names
			if (extra){
				if (AlertDialog.query(containerOfModule(), "Import taxa from other matrix?", "The matrix you are concatenating to this one is based on a different block of taxa, and includes taxa not in this matrix.  Do you want to add these taxa to this matrix before concatenating?")){
					String names = "";
					
					for (int oit = 0; oit<oTaxa.getNumTaxa(); oit++){
						if (taxa.findEquivalentTaxon(oTaxa, oit)<0){
							taxa.addTaxa(taxa.getNumTaxa(), 1, false);
							taxa.equalizeTaxon(oTaxa, oit, taxa.getNumTaxa()-1);
							names += taxa.getTaxonName(taxa.getNumTaxa()-1) + "\n";
							CommandRecord.tick("Added taxon " + taxa.getTaxonName(taxa.getNumTaxa()-1));
							
						}
					}
					if (!StringUtil.blank(names)){
						logln("Added to taxa block were:\n" + names);
						taxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED, null,null));
					}
				}
			}

		}
		int origNumChars = data.getNumChars();
		data.addParts(data.getNumChars()+1, oData.getNumChars());
		CharacterPartition partition = (CharacterPartition) data.getCurrentSpecsSet(CharacterPartition.class);
		if (partition==null) // let's give the origjnal ones a group
			data.setToNewGroup(data.getName(), 0, origNumChars-1, this);  //set group
		data.setToNewGroup(oData.getName(), origNumChars, data.getNumChars()-1, this);  //set group
		data.addInLinked(data.getNumChars()+1, oData.getNumChars(), true);
		CharacterState cs = null;
		for (int ic = 0; ic<oData.getNumChars(); ic++){
			CommandRecord.tick("Copying character " + (ic+1) + " in concatenation");
			data.equalizeCharacter(oData, ic, ic+origNumChars);
		}
		*/
		return success;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		return temp;
	}
}





