package mesquite.oliver.nca.MatrixCreator;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.characters.*;
import mesquite.categ.lib.*;
import mesquite.charMatrices.*;
import mesquite.assoc.*;
import mesquite.assoc.lib.*;

public class MatrixCreator extends DataUtility{
	TaxaAssociation hapAssociation = new TaxaAssociation();
	
	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		return true;
	}

	/*................................................................................................................*/
	/** Returns the number of haplotypes for a given data matrix */
	public int numberOfHaplotypes(CharacterData data){
		int numberUnique = 0;
		if (data == null)
			return numberUnique;
		else {
			Taxa taxa = data.getTaxa();
			CharacterState csi = null;
			CharacterState csj = null;
			boolean[] unique = new boolean[taxa.getNumTaxa()];
			if (taxa != null && taxa.getNumTaxa() > 0) {
				outerLoop: for (int i = 0; i <taxa.getNumTaxa(); i++) { 
					unique[i] = true;
					middleLoop: for (int j = 0; j < i; j++) {
						innerLoop: for (int k = 0; k < data.getNumChars(); k++) {
							csj = data.getCharacterState(csj, k, j);
							csi = data.getCharacterState(csi, k, i);
							if (!csi.equals(csj)) {
								unique[i] = true;
								break innerLoop;
							} else {
								unique[i] = false;
							}
						} // inner (k) loop - over all characters
					if(unique[i] == false){
						break middleLoop;
					}
					} // middle (j) loop - over all previous taxa
					
				} // outer (i) loop - over all taxa
			
			for (int countingHaps = 0; countingHaps < taxa.getNumTaxa(); countingHaps++){
				if (unique[countingHaps])
					numberUnique++;
			}
			return numberUnique;
			} // end of conditional checking for non-empty matrix
			else return numberUnique;
		}
	}
	
	/*.................................................................................................................*/
	public CharacterData newHaplotypeMatrix (Taxa haplotypeTaxa, int numberOfCharacters, CommandRecord commandRec){
		CharacterData newMatrix = null;
		String title = getProject().getCharacterMatrices().getUniqueName("Haplotype DNA Character Matrix");
		String dataTypeName = "DNA data";
		CharactersManager manager = (CharactersManager)findElementManager(CharacterData.class);
		if (manager == null)
			Debugg.println("The manager for Character Data could not be found.");
		newMatrix = manager.newCharacterData(haplotypeTaxa, numberOfCharacters, "DNA data");
		if(newMatrix == null)
			Debugg.println("Sorry, a character matrix could not be made (newMatrix = null)");
		if(title!=null){
			newMatrix.setName(title);
			MesquiteFile file;
			if (getProject().getNumberLinkedFiles()==1)
				file = getProject().getHomeFile();
			else file = haplotypeTaxa.getFile();
			newMatrix.addToFile(file, getProject(), manager); 
			MesquiteModule mb = findNearestColleagueWithName("Data Window Coordinator");
		}
		resetAllMenuBars();
		return newMatrix;
	}

	/*.................................................................................................................*/
	public void fillHaplotypeMatrix(CharacterData seqData, CharacterData hapData, CommandRecord commandRec, int numHaps){
		if (seqData == null || hapData == null || numHaps == 0){
		} else {
			CharacterState csi = null;
			CharacterState csj = null;
			Taxa seqTaxa = seqData.getTaxa();
			for(int fillFirst=0; fillFirst < seqData.getNumChars(); fillFirst++){
				csi = seqData.getCharacterState(csi, fillFirst, 0);
				hapData.setState(fillFirst,0,csi);
			}
			int[] haplotypeIdentity = new int[seqData.getNumTaxa()];
			boolean[] unique = new boolean[seqData.getNumTaxa()];
			unique[0] = true;
			int internalHapCounter = 1;
			int anotherHapCounter = 1;
			initializeHaplotypeIdentity: for(int initial = 0; initial < seqData.getNumTaxa(); initial++)
				haplotypeIdentity[initial] = initial;
			
			outerLoop: for (int i = 1; i < seqData.getNumTaxa(); i++) { 
				unique[i] = true;
				middleLoop: for (int j = 0; j < internalHapCounter; j++) {
					innerLoop: for (int k = 0; k < seqData.getNumChars(); k++) {
						csj = hapData.getCharacterState(csj, k, j);
						csi = seqData.getCharacterState(csi, k, i);
						if (!csi.equals(csj)) {
							unique[i] = true;
							break innerLoop;
						} else {
							unique[i] = false;
						}
					} // inner (k) loop - over all characters
				if(!unique[i]){
					haplotypeIdentity[i] = j;
					Debugg.println(seqTaxa.getName(i) + " is the same as " + hapData.getTaxa().getName(j));
					break middleLoop;
				}
				} // middle (j) loop - over all previous taxa
				if(unique[i]){
					charsLoop: for (int chars = 0; chars < hapData.getNumChars(); chars++){
						csi = seqData.getCharacterState(csi, chars, i);
						hapData.setState(chars,anotherHapCounter,csi);
					}
				internalHapCounter++;
				anotherHapCounter++;
				}
				
			} // outer (i) loop - over all taxa
		}
	}
	
	/*.................................................................................................................*/
	public boolean operateOnData(CharacterData data, CommandRecord commandRec) {
		if(data == null){
			return false;
		}
		else{
			MesquiteProject project = getProject();
			int numOfHaps = numberOfHaplotypes(data);
			Taxa newTaxa = project.createTaxaBlock(numOfHaps);
			newTaxa.setName("Haplotypes");
			for (int it = 0; it < newTaxa.getNumTaxa(); it++)
				(newTaxa.getTaxon(it)).setName("Haplotype " + Integer.toString(it + 1));
			CharacterData haplotypeMatrix = newHaplotypeMatrix(newTaxa, data.getNumChars(), commandRec);
			haplotypeMatrix.setName("DNA Character matrix of haplotypes from " + data.getName());
			fillHaplotypeMatrix(data, haplotypeMatrix, commandRec, numOfHaps);
			haplotypeMatrix.show(commandRec);
			// hapAssociation.setTaxa(data.getTaxa(), 1);
			// hapAssociation.setTaxa(newTaxa, 0);
			// hapAssociation.setName("Genes associated with Haplotypes");
			return true;
		}
	}

	public String getName() {
		return "New Haplotype Taxa Block from Current Matrix";
	}
	public boolean isPrerelease(){
		return true;
	}
	public boolean requestPrimaryChoice(){
		return true;
	}
}
