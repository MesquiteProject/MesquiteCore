package mesquite.molec.lib;

import mesquite.categ.lib.CategoricalState;
import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.DNAState;
import mesquite.categ.lib.ProteinData;
import mesquite.lib.MesquiteModule;
import mesquite.lib.Taxa;
import mesquite.lib.characters.CharacterData;

public class MolecUtil {
	/*.................................................................................................................*/
	static boolean acceptableState(DNAData data, int ic, int it, int candidateState, long originalState, boolean avoidStopCodons) {
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
	public static int resolveDNAAmbiguity(MesquiteModule ownerModule, CharacterData ddata, int ic, int it, DNAState charState, boolean avoidStopCodons, boolean randomlyChooseStateAsFallback, boolean warn){
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
						if (onlyState<=DNAState.maxDNAState && acceptableState(data,ic,it,onlyState,state, avoidStopCodons))
							freq[onlyState]++;
					}
				}
			}
			int maxFreq =0;
			int maxFreqState = -1;
			for (int i=0; i<=DNAState.maxDNAState; i++) {
				if (freq[i]>maxFreq && acceptableState(data,ic,it,i,state, avoidStopCodons)) {  // check to see if is in original state set
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
			
			if (stateToAssign>=0 && acceptableState(data,ic,it,stateToAssign,state, avoidStopCodons)) {
				return stateToAssign;
			} else if (warn)
				ownerModule.logln("Warning!  Ambiguity not resolved for taxon " + it + " and character " + ic);


		}
		return -1;
	}
	/*.................................................................................................................*/
	public static boolean resolveAndAssignDNAAmbiguity(MesquiteModule ownerModule, CharacterData ddata, int ic, int it, DNAState charState, boolean avoidStopCodons, boolean randomlyChooseStateAsFallback, boolean warn){
		int stateToAssign = resolveDNAAmbiguity(ownerModule, ddata, ic, it, charState,avoidStopCodons,randomlyChooseStateAsFallback, warn);
		if (stateToAssign>=0) {
			long newState = 0;
			newState = CategoricalState.addToSet(newState,stateToAssign);
			newState = charState.setUncertainty(newState, false);
			DNAState specifiedState = new DNAState(newState);
			ddata.setState(ic, it, specifiedState);
			return true;
		}
		return false;
	}

}
