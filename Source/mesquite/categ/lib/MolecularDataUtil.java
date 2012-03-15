package mesquite.categ.lib;

import java.awt.Rectangle;

import mesquite.basic.ManageSetsBlock.ManageSetsBlock;
import mesquite.lib.*;
import mesquite.lib.characters.CodonPositionsSet;
import mesquite.lib.duties.ElementManager;
import mesquite.lists.lib.ListModule;
import mesquite.align.lib.*;

/** This is a utility class that provides static methods to do various jobs with molecular data  */

public class MolecularDataUtil {

	/*.................................................................................................................*
	private boolean alignTouchedToDropped(int rowToAlign, int recipientRow){
		MesquiteNumber score = new MesquiteNumber();
		if (aligner==null) {
			aligner = new PairwiseAligner(true,allowNewGaps.getValue(), subs,gapOpen.getValue(), gapExtend.getValue(), gapOpenTerminal.getValue(), gapExtendTerminal.getValue(), alphabetLength);
			//aligner.setUseLowMem(true);
		}
		if (aligner!=null){
			//aligner.setUseLowMem(data.getNumChars()>aligner.getCharThresholdForLowMemory());
			originalCheckSum = ((CategoricalData)data).storeCheckSum(0, data.getNumChars()-1,rowToAlign, rowToAlign);
			aligner.setAllowNewInternalGaps(allowNewGaps.getValue());
			long[][] aligned = aligner.alignSequences((MCategoricalDistribution)data.getMCharactersDistribution(), recipientRow, rowToAlign,MesquiteInteger.unassigned,MesquiteInteger.unassigned,true,score);
			if (aligned==null) {
				logln("Alignment failed!");
				return false;
			}
			logln("Align " + (rowToAlign+1) + " onto " + (recipientRow+1));
			long[] newAlignment = Long2DArray.extractRow(aligned,1);

			int[] newGaps = aligner.getGapInsertionArray();
			if (newGaps!=null)
				alignUtil.insertNewGaps((MolecularData)data, newGaps);
			Rectangle problem = alignUtil.forceAlignment((MolecularData)data, 0, data.getNumChars()-1, rowToAlign, rowToAlign, 1, aligned);

			((CategoricalData)data).examineCheckSum(0, data.getNumChars()-1,rowToAlign, rowToAlign, "Bad checksum; alignment has inapproppriately altered data!", warnCheckSum, originalCheckSum);
			return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	public static void pairwiseAlignMatrix(MesquiteModule module, MolecularData data, int referenceTaxon, boolean allowNewGaps) {
		Taxa taxa = data.getTaxa();
		MesquiteNumber score = new MesquiteNumber();
		AlignUtil alignUtil = new AlignUtil();
		PairwiseAligner aligner = PairwiseAligner.getDefaultAligner(false,data);
		MesquiteBoolean warnCheckSum = new MesquiteBoolean(false);
		aligner.setAllowNewInternalGaps(allowNewGaps);
		for (int it=0; it<data.getNumTaxa(); it++) {
			if (it!=referenceTaxon) {
				long originalCheckSum = ((CategoricalData)data).storeCheckSum(0, data.getNumChars()-1,it, it);
				long[][] aligned = aligner.alignSequences((MCategoricalDistribution)data.getMCharactersDistribution(), referenceTaxon, it,MesquiteInteger.unassigned,MesquiteInteger.unassigned,true,score);
				//long[] newAlignment = Long2DArray.extractRow(aligned,1);

				long[] newAlignment = Long2DArray.extractRow(aligned,1);
				int[] newGaps = aligner.getGapInsertionArray();
				if (newGaps!=null)
					alignUtil.insertNewGaps((MolecularData)data, newGaps);
				Rectangle problem = alignUtil.forceAlignment((MolecularData)data, 0, data.getNumChars()-1, it, it, 1, aligned);
				
				if (problem!=null)
					Debugg.println("problem " + problem.x + " " +problem.y + " " + problem.width + " " + problem.height);
				
				((CategoricalData)data).examineCheckSum(0, data.getNumChars()-1,it, it, "Bad checksum; alignment has inapproppriately altered data!", warnCheckSum, originalCheckSum);
				Debugg.println("---------------- completed taxon " + it);
//				data.notifyListeners(module, new Notification(MesquiteListener.DATA_CHANGED, null, null));

			}
		}
		Debugg.println("================= completed pairwiseAlign ");

	}
	/*.................................................................................................................*/
	public static void shiftAlignMatrix(MolecularData data, Taxa taxa, int referenceTaxon) {

	}

	/*.................................................................................................................*/
	public static void setCodonPositions(DNAData data, CodonPositionsSet modelSet,  int position,  boolean calc, boolean notify){
		if (modelSet==null)
			return;
		MesquiteNumber num = new MesquiteNumber();
		num.setValue(position);
		if (modelSet != null) {
			for (int i=0; i<data.getNumChars(); i++) {
				modelSet.setValue(i, num);
				if (calc) {
					num.setValue(num.getIntValue()+1);
					if (num.getIntValue()>3)
						num.setValue(1);
				}
			}


		}
	}
	/*.................................................................................................................*/
	public static int getMinimumStops(DNAData data, int it, CodonPositionsSet modelSet){
		int minStops = -1;
		for (int i = 1; i<=3; i++) {
			setCodonPositions(data,modelSet, i,true,false);  //set them temporarily
			int totNumStops = ((DNAData)data).getAminoAcidNumbers(it,ProteinData.TER);					 
			if (minStops<0 || totNumStops<minStops) {
				minStops = totNumStops;
			}
		}
		return minStops;
	}
	/*.................................................................................................................*
	public static int getShiftForMinimumStops(DNAData data, int it, CodonPositionsSet modelSet){
		int minStops = -1;
		for (int i = 1; i<=3; i++) {
			setCodonPositions(data,modelSet, i,true,false);  //set them temporarily
			int totNumStops = ((DNAData)data).getAminoAcidNumbers(it,ProteinData.TER);					 
			if (minStops<0 || totNumStops<minStops) {
				minStops = totNumStops;
			}
		}
		return minStops;
	}

	/*.................................................................................................................*/
	public static void reverseComplementSequencesIfNecessary(DNAData data, MesquiteModule module, Taxa taxa, int itStart, int itEnd) {

		CodonPositionsSet modelSet = (CodonPositionsSet) data.getCurrentSpecsSet(CodonPositionsSet.class);
		if (modelSet == null) {
			modelSet= new CodonPositionsSet("Codon Positions", data.getNumChars(), data);
			modelSet.addToFile(data.getFile(), module.getProject(), module.findElementManager(CodonPositionsSet.class)); //THIS
			data.setCurrentSpecsSet(modelSet, CodonPositionsSet.class);
		}

		for (int it = itStart; it<taxa.getNumTaxa() && it<itEnd; it++) {
			int stops = getMinimumStops(data, it, modelSet);
			data.reverseComplement(0, data.getNumChars()-1, it, false, false);
			int stopsRC = getMinimumStops(data, it, modelSet);
			if (stops>stopsRC) {
				data.reverseComplement(0, data.getNumChars(), it, false, false);
				module.logln("Reverse complemented sequence " + (it+1));
			}
		}
	}
	/*.................................................................................................................*
	private void setPositions(int position,  boolean calc, boolean notify){
		if (table !=null && data!=null) {
			boolean changed=false;
			MesquiteNumber num = new MesquiteNumber();
			num.setValue(position);
			CodonPositionsSet modelSet = (CodonPositionsSet) data.getCurrentSpecsSet(CodonPositionsSet.class);
			if (modelSet == null) {
				modelSet= new CodonPositionsSet("Codon Positions", data.getNumChars(), data);
				modelSet.addToFile(data.getFile(), getProject(), findElementManager(CodonPositionsSet.class)); //THIS
				data.setCurrentSpecsSet(modelSet, CodonPositionsSet.class);
			}
			if (modelSet != null) {
				if (employer!=null && employer instanceof ListModule) {
					int c = ((ListModule)employer).getMyColumn(this);
					for (int i=0; i<data.getNumChars(); i++) {
						if (table.isCellSelectedAnyWay(c, i)) {
							modelSet.setValue(i, num);
							if (!changed)
								outputInvalid();
							if (calc) {
								num.setValue(num.getIntValue()+1);
								if (num.getIntValue()>3)
									num.setValue(1);
							}

							changed = true;
						}
					}
				}
			}
			if (notify) {
				if (changed)
					data.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  //not quite kosher; HOW TO HAVE MODEL SET LISTENERS??? -- modelSource
				parametersChanged();
			}
		}
	}
	/*.................................................................................................................*/
	public static void setCodonPositions(DNAData data, MesquiteModule module, Taxa taxa, int itStart, int itEnd, int startPos) {

		CodonPositionsSet modelSet = (CodonPositionsSet) data.getCurrentSpecsSet(CodonPositionsSet.class);
		if (modelSet == null) {
			modelSet= new CodonPositionsSet("Codon Positions", data.getNumChars(), data);
			modelSet.addToFile(data.getFile(), module.getProject(), module.findElementManager(CodonPositionsSet.class)); //THIS
			data.setCurrentSpecsSet(modelSet, CodonPositionsSet.class);
		} 
		setCodonPositions(data,modelSet, startPos,true,false);  
	}
	/*.................................................................................................................*/
	public static void setCodonPositionsToMinimizeStops(DNAData data, MesquiteModule module, Taxa taxa, int itStart, int itEnd) {

		CodonPositionsSet modelSet = (CodonPositionsSet) data.getCurrentSpecsSet(CodonPositionsSet.class);
		if (modelSet == null) {
			modelSet= new CodonPositionsSet("Codon Positions", data.getNumChars(), data);
			modelSet.addToFile(data.getFile(), module.getProject(), module.findElementManager(CodonPositionsSet.class)); //THIS
			data.setCurrentSpecsSet(modelSet, CodonPositionsSet.class);
		} 
		int minStops = -1;
		int minPosStart = -1;
		for (int i = 1; i<=3; i++) {
			setCodonPositions(data,modelSet, i,true,false);  //set them temporarily
			int totNumStops = 0;
			for (int it=itStart; it<=itEnd; it++)
				totNumStops+=((DNAData)data).getAminoAcidNumbers(it,ProteinData.TER);					 
			if (minStops<0 || totNumStops<minStops) {
				minStops = totNumStops;
				minPosStart=i;
			}
		}
		setCodonPositions(data,modelSet, minPosStart,true,false);  
	}
	/*.................................................................................................................*/
	public static void shiftToMinimizeStops(DNAData data, MesquiteModule module, Taxa taxa, int itStart, int itEnd) {

		CodonPositionsSet modelSet = (CodonPositionsSet) data.getCurrentSpecsSet(CodonPositionsSet.class);
		if (modelSet == null) {
			modelSet= new CodonPositionsSet("Codon Positions", data.getNumChars(), data);
			modelSet.addToFile(data.getFile(), module.getProject(), module.findElementManager(CodonPositionsSet.class)); //THIS
			data.setCurrentSpecsSet(modelSet, CodonPositionsSet.class);
		}

		for (int it = itStart; it<taxa.getNumTaxa() && it<itEnd; it++) {
			int stops = getMinimumStops(data, it, modelSet);
		}
	}

}
