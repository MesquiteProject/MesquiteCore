/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.ResolveDNAAmbiguities;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.characters.*;

import java.awt.Checkbox;

import mesquite.categ.lib.*;
import mesquite.lib.table.*;
import mesquite.molec.lib.GeneticCode;

/* ======================================================================== */
public class ResolveDNAAmbiguities extends DNADataAlterer implements AltererDNACell {
	MesquiteTable table;
	DNAState charState = new DNAState();
	boolean randomlyChooseStateAsFallback = true;
	boolean avoidStopCodons = true;
	boolean preferencesSet=false;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		return true;
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference(String tag, String content) {
		if ("randomlyChooseStateAsFallback".equalsIgnoreCase(tag))
			randomlyChooseStateAsFallback = MesquiteBoolean.fromTrueFalseString(content);
		if ("avoidStopCodons".equalsIgnoreCase(tag))
			avoidStopCodons = MesquiteBoolean.fromTrueFalseString(content);

		preferencesSet = true;
	}

	/*.................................................................................................................*/
	public String preparePreferencesForXML() {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "randomlyChooseStateAsFallback", randomlyChooseStateAsFallback);
		StringUtil.appendXMLTag(buffer, 2, "avoidStopCodons", avoidStopCodons);

		preferencesSet = true;
		return buffer.toString();
	}

	/*.................................................................................................................*/
	public boolean queryOptions() {
		loadPreferences();

		if (!MesquiteThread.isScripting()){
			MesquiteInteger buttonPressed = new MesquiteInteger(1);
			ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "DNA state uncertainty resolution options",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
			dialog.appendToHelpString("This feature will scan the matrix for any taxa with ambiguity codes (e.g., R, Y, N).  It will then examine "
		+ "the other taxa in the matrix (if you have selected any taxa, it will only do this within the selected taxa) and will look for the commonest"
		+ "nucleotide possessed by the other taxa at that site, and convert the ambiguity to that state. <BR>");
			dialog.appendToHelpString("You can choose to avoid chosing any state that would cause any triplet in which that site participates "
					+ "to have a stop codon. <BR> If this procedure fails to yield a state, you can ask Mesquite to randomly choose one of the"
					+ "states within the uncertainty as the state of that site.");
			Checkbox randomlyChooseStateAsFallbackBox = dialog.addCheckBox("Randomly choose a contained state as fallback", randomlyChooseStateAsFallback);
			Checkbox avoidStopCodonsBox = dialog.addCheckBox("Do not choose a state that would yield a stop codon", avoidStopCodons);
			dialog.completeAndShowDialog(true);
			if (buttonPressed.getValue()==0)  {
				randomlyChooseStateAsFallback = randomlyChooseStateAsFallbackBox.getState();
				avoidStopCodons = avoidStopCodonsBox.getState();
				storePreferences();
			}
			dialog.dispose();
			return (buttonPressed.getValue()==0);

		}
		return true;
	}	/*.................................................................................................................*/
	/** Called to alter data in those cells selected in table*/
	public boolean alterData(CharacterData data, MesquiteTable table, UndoReference undoReference){
		this.table = table;
		if (!(data instanceof DNAData)){
			MesquiteMessage.warnProgrammer("Can use " + getName() + " only on nucleotide data");
			return false;
		}
		if (!queryOptions())
			return false;
		return alterContentOfCells(data,table, undoReference);
	}


	/*.................................................................................................................*/
	boolean acceptableState(DNAData data, int ic, int it, int candidateState, long originalState) {
		boolean inOriginalSet = CategoricalState.isElement(originalState, candidateState) || data.isUnassigned(ic, it);
		if (!inOriginalSet)
			return false;
		if (avoidStopCodons && data.isCoding(ic)) {
			GeneticCode genCode =data.getGeneticCode(ic);
			int[] triplet = data.getCodonTriplet(ic);
			long[] codon = data.getCodon(triplet,it);
			int pos = data.getCodonPosition(ic);
			if (pos>=1 && pos<=3) {
				codon[pos-1]=DNAState.makeSet(candidateState);
				int aminoAcid = CategoricalState.getOnlyElement(data.getAminoAcid(codon, genCode));
				if (aminoAcid==ProteinData.TER)
					return false;
			}
		} 
		return true;
	}
	/*.................................................................................................................*/
	public void alterCell(CharacterData ddata, int ic, int it){
		DNAData data = (DNAData)ddata;
		if (data.isMultistateOrUncertainty(ic, it) || data.isUnassigned(ic, it)) {
			long state = data.getState(ic, it);
			int[] freq = new int[DNAState.maxDNAState+1];
			for (int i=0; i<=DNAState.maxDNAState; i++)
				freq[i]=0;
			Taxa taxa = data.getTaxa();

			for (int otherTaxa=0; otherTaxa<data.getNumTaxa(); otherTaxa++) {
				if ((otherTaxa!=it) && (!taxa.anySelected() || taxa.getSelected(otherTaxa))) { 
					long otherState = data.getState(ic, otherTaxa);
					int onlyState = CategoricalState.getOnlyElement(otherState);
					if (onlyState>=0) {  // not multistate, not unassigned
						if (onlyState<=DNAState.maxDNAState && acceptableState(data,ic,it,onlyState,state))
							freq[onlyState]++;
					}
				}
			}
			int maxFreq =0;
			int maxFreqState = -1;
			for (int i=0; i<=DNAState.maxDNAState; i++) {
				if (freq[i]>maxFreq && acceptableState(data,ic,it,i,state)) {  // check to see if is in original state set
					maxFreq=freq[i];
					maxFreqState = i;
				}
			}
			int stateToAssign = maxFreqState;
			
			

			if (maxFreqState<0 && randomlyChooseStateAsFallback) {  // we didn't find it one of the states in the other taxa, so let's choose randomly
				int card = CategoricalState.cardinality(state);
				int resolve = (int)Math.round(Math.random()*card+0.5);
				int count=0;
				for (int e=0; e<=DNAState.maxDNAState; e++) {
					if (CategoricalState.isElement(state, e)) {
						count++;
						if (count>=resolve) {
							stateToAssign = e;
							break;
						}
					}
				}
			}

			if (stateToAssign>=0 && acceptableState(data,ic,it,stateToAssign,state)) {
				long newState = 0;
				newState = CategoricalState.addToSet(newState,stateToAssign);
				newState = charState.setUncertainty(newState, false);
				DNAState specifiedState = new DNAState(newState);
				data.setState(ic, it, specifiedState);
				if (!MesquiteLong.isCombinable(numCellsAltered))
					numCellsAltered=0;
				numCellsAltered++;
			} else
				logln("Warning!  Ambiguity not resolved for taxon " + it + " and character " + ic);


		}
	}

	/*.................................................................................................................*/
	public boolean isPrerelease() {
			return false;
		}
	/*.................................................................................................................*/
	public String getName() {
		return "Resolve DNA Ambiguities";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Resolve DNA Ambiguities...";
	}
		/*.................................................................................................................*/
		/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
		 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
		 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
		public int getVersionOfFirstRelease(){
			return 380;  
		}
		/*.................................................................................................................*/
		/** returns an explanation of what the module does.*/
		public String getExplanation() {
			return "Resolves uncertainties in DNA data by converting it to the most common state among chosen taxa." ;
		}

	}

