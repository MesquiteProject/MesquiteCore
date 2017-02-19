/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 

 
 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
 Perhaps with your help we can be more than a few, and make Mesquite better.
 
 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
 Mesquite's web site is http://mesquiteproject.org
 
 This source code and its compiled class files are free and modifiable under the terms of 
 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.align.lib;
/*~~  */

import java.util.*;
import java.util.zip.CRC32;
import java.lang.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class AlignUtil {
	StringBuffer sb = new StringBuffer();
	private boolean checksumValid = false;
	CRC32 checksum = new CRC32();
	
	public long getChecksum(Object o, int it, int icStart, int icEnd){
		if (o instanceof MolecularData){
			MolecularData data = (MolecularData)o;
			checksum.reset();
			for (int ic = icStart; ic<=icEnd; ic++){
				long s = data.getState(ic, it);
				if (!CategoricalState.isInapplicable(s))
					checksum.update(Long.toString(s).getBytes());
			}
			return checksum.getValue();
		}
		else if (o instanceof MCategoricalDistribution){
			MCategoricalDistribution data = (MCategoricalDistribution)o;
			checksum.reset();
			for (int ic = icStart; ic<=icEnd; ic++){
				long s = data.getState(ic, it);
				if (!CategoricalState.isInapplicable(s))
					checksum.update(Long.toString(s).getBytes());
			}
			return checksum.getValue();
		}
		else if (o instanceof long[][]){
			long[][] states = (long[][])o;
			checksum.reset();
			for (int ic = 0; ic<states.length; ic++){
				long s = states[ic][it];
				if (!CategoricalState.isInapplicable(s))
					checksum.update(Long.toString(s).getBytes());
			}
			return checksum.getValue();
		}
		else if (o instanceof long[]){
			long[] states = (long[])o;
			checksum.reset();
			for (int ic = 0; ic<states.length; ic++){
				long s = states[ic];
				if (!CategoricalState.isInapplicable(s))
					checksum.update(Long.toString(s).getBytes());
			}
			return checksum.getValue();
		}
		return 0;
	}
	
	/** given an original matrix, orig,  */	
	int findTaxonAligned(MolecularData orig, int itOrig, int itAlignedStart, Object aligned){
		if (aligned instanceof String[])
			return itOrig+itAlignedStart;
		if (aligned instanceof long[][])
			return itOrig+itAlignedStart;
		if (aligned instanceof long[])  //there is only one taxon here
			return itAlignedStart;
		if (aligned instanceof CharacterData){
			Taxa origTaxa = orig.getTaxa();
			Taxa alignTaxa = ((CharacterData)aligned).getTaxa();
			if (origTaxa == alignTaxa)
				return itOrig+itAlignedStart;
			else
				return alignTaxa.whichTaxonNumber(origTaxa.getTaxonName(itOrig));
		}
		if (aligned instanceof MCategoricalDistribution){
			Taxa origTaxa = orig.getTaxa();
			Taxa alignTaxa = ((MCategoricalDistribution)aligned).getTaxa();
			if (origTaxa == alignTaxa)
				return itOrig+itAlignedStart;
			else
				return alignTaxa.whichTaxonNumber(origTaxa.getTaxonName(itOrig));
		}
		
		return itOrig+itAlignedStart;
	}
	int getNumChars(Object o){
		if (o == null)
			return 0;
		if (o instanceof String[]){
			String[] s = (String[])o;
			if (s.length == 0)
				return 0;
			if (s[0] == null)
				return 0;
			return s[0].length();
		}
		if (o instanceof long[][])
			return ((long[][])o).length;
		if (o instanceof long[])
			return ((long[])o).length;
		if (o instanceof CharacterData){
			CharacterData data = (CharacterData)o;
			return data.getNumChars();
		}
		else if (o instanceof MCategoricalDistribution){
			MCategoricalDistribution data = (MCategoricalDistribution)o;
			return data.getNumChars();
		}
		return 0;
	}
	boolean isInapplicable(Object o, int ic, int it){
		if (o == null)
			return true;
		if (o instanceof String[]){
			String[] s = (String[])o;
			if (it >= s.length)
				return true;
			if (s[it] == null)
				return true;
			if (ic >= s[it].length())
				return true;
			return s[it].charAt(ic) == '-' || s[it].charAt(ic) == '0';
		}
		if (o instanceof long[][] ){
			long[][] s = (long[][])o;
			if (ic >= s.length)
				return true;
			if (s[ic] == null)
				return true;
			if (it >= s[ic].length)
				return true;
			return CategoricalState.isInapplicable(s[ic][it]) || s[ic][it]==0L;
		}
		if (o instanceof long[]){
			long[] s = (long[])o;
			if (s == null)
				return true;
			if (ic >= s.length)
				return true;
			return CategoricalState.isInapplicable(s[ic]) || s[ic]==0L;
		}
		if (o instanceof CharacterData){
			CharacterData data = (CharacterData)o;
			if (it >= data.getNumTaxa() || ic >= data.getNumChars())
				return true;
			return data.isInapplicable(ic, it);
		}
		if (o instanceof MCategoricalDistribution){
			MCategoricalDistribution data = (MCategoricalDistribution)o;
			if (it >= data.getNumTaxa() || ic >= data.getNumChars())
				return true;
			return CategoricalState.isInapplicable(data.getState(ic, it));
		}
		return true;
	}
	boolean sameState(CategoricalData orig, int icOrig, int itOrig, Object o, int icAligned, int itAligned){
		if (o == null)
			return false;
		if (o instanceof long[][] ){
			long[][] s = (long[][])o;
			if (icAligned >= s.length)
				return false;
			if (s[icAligned] == null)
				return false;
			if (itAligned >= s[icAligned].length)
				return false;
			long aligned = CategoricalState.setUncertainty(s[icAligned][itAligned],false);
			long origState = CategoricalState.setUncertainty(orig.getState(icOrig, itOrig),false);
			boolean success = (CategoricalState.statesBitsMask & s[icAligned][itAligned]) == (CategoricalState.statesBitsMask & orig.getState(icOrig, itOrig));
			if (!success && CategoricalState.isUnassigned(s[icAligned][itAligned])) {  // this is a case in which the data was reset to full missing on import into the aligner as the aligner couldn't cope otherwise
				success = true;
			}
			if (!success)
				MesquiteMessage.println("   Incorporation mismatch, aligned: " + aligned + ", orig: " + origState);		
			return success;
		}
		if (o instanceof long[] ){
			long[] s = (long[])o;
			if (s == null)
				return false;
			if (icAligned >= s.length)
				return false;
			long aligned = s[icAligned];
			long origState = orig.getState(icOrig, itOrig);
			if (!((CategoricalState.statesBitsMask & s[icAligned]) == (CategoricalState.statesBitsMask & orig.getState(icOrig, itOrig))))
				MesquiteMessage.println("   Incorporation mismatch, aligned: " + aligned + ", orig: " + origState);
			
			return (CategoricalState.statesBitsMask & s[icAligned]) == (CategoricalState.statesBitsMask & orig.getState(icOrig, itOrig));
		}
		if (o instanceof String[]){
			String[] s = (String[])o;
			if (itAligned >= s.length)
				return false;
			if (s[itAligned] == null)
				return false;
			if (icAligned >= s[itAligned].length())
				return false;
			sb.setLength(0);
			orig.statesIntoStringBuffer(icOrig, itOrig, sb, true);
			if (sb.length()!= 1)
				return false;
			boolean success = s[itAligned].charAt(icAligned) == sb.charAt(0);
			if (!success && MesquiteTrunk.debugMode)
				MesquiteMessage.println("   Incorporation mismatch 2, aligned: " + s[itAligned].charAt(icAligned)  + ", orig: " +  sb.charAt(0));
			return success;
		}
		else if (o instanceof CategoricalData){
			CategoricalData aligned = (CategoricalData)o;
			boolean success = aligned.getState(icAligned, itAligned) == orig.getState(icOrig, itOrig);
			if (!success && MesquiteTrunk.debugMode)
				MesquiteMessage.println("   Incorporation mismatch 3, aligned: " + aligned.getState(icAligned, itAligned) + ", orig: " +  orig.getState(icOrig, itOrig));
			return success;
		}
		else if (o instanceof MCategoricalDistribution){
			MCategoricalDistribution aligned = (MCategoricalDistribution)o;
			boolean success =  aligned.getState(icAligned, itAligned) == orig.getState(icOrig, itOrig);
			if (!success && MesquiteTrunk.debugMode)
				MesquiteMessage.println("   Incorporation mismatch 4, aligned: " + aligned.getState(icAligned, itAligned) + ", orig: " +  orig.getState(icOrig, itOrig));
			return success;
		}
		return false;
	}
	
	/** This inserts the needed new characters into the matrix contained in the MolecularData object.  The number of new characters
	 * to be inserted at each site is contained in the newGaps array.  Those sites that are to receive new terminal gaps are indicated
	 * by the values of preSequenceTerminalFlaggedGap and postSequenceTerminalFlaggedGap.
	 * */
	public void  insertNewGaps(MolecularData data, int[] newGaps,  int preSequenceTerminalFlaggedGap,  int postSequenceTerminalFlaggedGap){
		//preSequenceTerminalFlaggedGap,  int postSequenceTerminalFlaggedGap added DRM 7 Aug 2014
		int start = newGaps.length-1;
		if (data.getNumChars()<newGaps.length)
			start = data.getNumChars()-1;
		for (int ic = start; ic>=0; ic--) {  // go down from last characters to see if there are any gaps that need to be inserted, and insert them.
			if (newGaps[ic]>0){
				data.addCharacters(ic-1, newGaps[ic], false); 
				data.addInLinked(ic-1, newGaps[ic], false);
			}
		}
		start = newGaps.length-1;
		if (data.getNumChars()<newGaps.length)
			start = data.getNumChars()-1;
		
		for (int ic = start; ic>=0; ic--) {   // go down from start and look for negative values denoting terminal gaps at the END that might need to be inserted
			if (newGaps[ic]<0 && postSequenceTerminalFlaggedGap==ic){  // negative value AND this is the array position marked as the terminal gap AFTER the sequence
				int numGaps = - newGaps[ic] - (data.getNumChars()-1-ic);  // are extra characters needed?
				if (numGaps>0) {
					data.addCharacters(data.getNumChars(), numGaps, false); 
					data.addInLinked(data.getNumChars(), numGaps, false);
				}
				newGaps[ic]=0;  // have to zero it so that it isn't acted upon by the next loop.  *** added by DRM 7 Aug 2014
				break;
			} 
		}
		
		for (int ic = 0; ic<data.getNumChars() && ic<newGaps.length; ic++) {   // go up from zero and look for negative values denoting terminals
			if (newGaps[ic]<0 && preSequenceTerminalFlaggedGap==ic){ // negative value AND this is the array position marked as the terminal gap BEFORE the sequence
				int numGaps = - newGaps[ic] - (ic);  // are extra characters needed?
				if (numGaps>0) {
					data.addCharacters(0, numGaps, false); 
					data.addInLinked(0, numGaps, false);
				}
				break;
			}
		}
	}
	
	/** This takes the original data matrix "origData", and re-aligns the block from icOrigStart, icOrigEnd, itOrigStart, and itOrigEnd 
	 * according to the alignment present in alignedSequences.   Note that alignedData need not be exactly the same same size as the block.  
	 * In particular, sometimes there will be more sequences in alignedSequences than in origData.  The sequences to which origData should be
	 * matched should start within alignedSequences at itAlignedStart.
	 * */
	public Rectangle forceAlignment(MolecularData origData, int icOrigStart, int icOrigEnd, int itOrigStart, int itOrigEnd, int itAlignedStart, Object alignedSequences){
		if (!MesquiteInteger.isCombinable(icOrigStart) || icOrigStart<0)
			icOrigStart = 0;
		if (!MesquiteInteger.isCombinable(icOrigEnd) || icOrigEnd<0)
			icOrigEnd = origData.getNumChars()-1;
		if (!MesquiteInteger.isCombinable(itOrigStart) || itOrigStart<0)
			itOrigStart = 0;
		if (!MesquiteInteger.isCombinable(itOrigEnd) || itOrigEnd<0)
			itOrigEnd = origData.getNumTaxa()-1;
		int numCharsAligned = getNumChars(alignedSequences);
		int numCharsOrig = icOrigEnd-icOrigStart+1;
		int[] charsToAddToOrig = new int[numCharsOrig];
		for (int ic = 0; ic<numCharsOrig; ic++)
			charsToAddToOrig[ic] = 0;
		int csproblem = -1;
		int numTaxaInOrig = itOrigEnd - itOrigStart+1;  //numtaxa within the matrix.  this should match the number of taxa in "aligned"
		long[] checkSums = new long[numTaxaInOrig];
		//checksums on original matrix
		for (int it = 0; it<numTaxaInOrig; it++)
			checkSums[it] = getChecksum(origData, it+itOrigStart,  0, origData.getNumChars()-1);
		
		if (numCharsAligned>numCharsOrig) {
			origData.addCharacters(icOrigEnd, numCharsAligned-numCharsOrig, false);
			origData.addInLinked(icOrigEnd, numCharsAligned-numCharsOrig, false);
			numCharsOrig = numCharsAligned;
		}
		//prepareAligned(alignedSequences);
		Rectangle rect = null;
		boolean failed = false;
		String failedReportString = "";
		for (int itOrig = itOrigStart; itOrig<=itOrigEnd; itOrig++){   //cycle through taxa in "aligned"
			int itAligned = findTaxonAligned(origData, itOrig-itOrigStart, itAlignedStart, alignedSequences);  //this gives one the taxon number within "aligned"
			if (itAligned>=0){
				int icAligned = 0;
				int icOrig = icOrigStart;
				int icOrigEnd2 = icOrigStart+numCharsOrig-1;
				while(icAligned < numCharsAligned && icOrig <= icOrigEnd2){ //keep marching along looking for mismatch of gaps
					
					// start at left side; proceed as long as both gaps or both nucleotides.  
					while (icAligned< numCharsAligned && icOrig <= icOrigEnd2 && ((isInapplicable(alignedSequences, icAligned, itAligned) && isInapplicable(origData, icOrig, itOrig)) || (!isInapplicable(alignedSequences, icAligned, itAligned) && !isInapplicable(origData, icOrig, itOrig)))){
						icAligned++;
						icOrig++;
					}
					//aligned and orig differ at this point on whether gap or nucleotide; push or pull original accordingly
					if (icAligned < numCharsAligned && icOrig <= icOrigEnd2) {
						// if aligned has gap, need to open up gap in orig.  
						if (isInapplicable(alignedSequences, icAligned, itAligned)) {
							int startOfGapAligned = icAligned;
							//first, find end of gap in aligned to know how much has to be opened in original
							while(icAligned < numCharsAligned && isInapplicable(alignedSequences, icAligned, itAligned))
								icAligned++;
							
							if (icAligned<numCharsAligned){
								int gapSize = icAligned-startOfGapAligned;
								//now know how big of a gap is needed; push original to match
								//in case gaps that can be eaten up in orig are not all at end, but push block by block until a big enough gap has opened
								int spaceNeeded = gapSize;
								while (spaceNeeded >0){
									int startOfGapOrig = icOrig;
									int endOfGapOrig = -1;
									while (startOfGapOrig <= icOrigEnd2 && !origData.isInapplicable(startOfGapOrig, itOrig))
										startOfGapOrig++;
									if (origData.isInapplicable(startOfGapOrig, itOrig)){ //actually is gap
										endOfGapOrig = startOfGapOrig;
										while (endOfGapOrig <= icOrigEnd2 && origData.isInapplicable(endOfGapOrig, itOrig))
											endOfGapOrig++;
										endOfGapOrig--;
										if (origData.isInapplicable(endOfGapOrig, itOrig)){
											int spaceAvailable = endOfGapOrig-startOfGapOrig +1;
											int gapToUse = spaceNeeded;
											if (spaceAvailable<spaceNeeded)
												gapToUse = spaceAvailable;
											int added = origData.moveCells(icOrig, startOfGapOrig-1, gapToUse, itOrig,itOrig, false, true, true, false,null,null, null);
											icOrig += gapToUse;
											spaceNeeded -= gapToUse;
											if (added>0)
												MesquiteMessage.warnProgrammer("Alignment added characters when shouldn't have");
										}
										else 
											spaceNeeded = 0;
									}
									else 
										spaceNeeded = 0; 
								}
							}
						}
						// if original has gap, pull accordingly from end of chunk forward
						else {
							int startOfGap = icOrig;
							//findEndOf gap
							while(icOrig <= icOrigEnd2 && isInapplicable(origData, icOrig, itOrig))
								icOrig++;
							if (icOrig<=icOrigEnd2){
								int gapSize = icOrig-startOfGap;
								//now know how big of a gap must be closed
								int added = origData.moveCells(icOrig, icOrigEnd2, -gapSize, itOrig,itOrig, false, true, true, false,null,null, null);
								icOrig -= gapSize;
								if (added>0)
									MesquiteMessage.warnProgrammer("Alignment added characters when shouldn't have");
							}
						}
					}
				}
				for (int ic = 0; ic < numCharsAligned; ic++)
					if (!sameState(origData, ic+icOrigStart, itOrig, alignedSequences, ic, itAligned)) {
						if (!failed){ //firsttime
							rect = new Rectangle(ic+icOrigStart, itOrig , ic, itAligned);
						}
						failed = true;
						failedReportString = "At least one site has had its character state changed (site " + (ic+1) + " of taxon " + (itOrig+1) + "). ";
						break;
					}
				
				
			}
		}
		
		//checksums on alignment
		for (int it = 0; it<numTaxaInOrig && csproblem == -1; it++){
			long cs = getChecksum(origData, it+itOrigStart,  0, origData.getNumChars()-1);
			if (cs != checkSums[it]) {
				csproblem = it;
				failedReportString += " Sequence " + it + " failed checksum.";
			}
		}
		if (csproblem >=0){
			rect = new Rectangle(0, csproblem , origData.getNumChars()-1, csproblem);
			failed = true;
		}
		if (failed)
			MesquiteTrunk.mesquiteTrunk.alert("ERROR: alignment incorporation failed to recover states as in aligned matrix. " + failedReportString);
		else {
			MesquiteTrunk.mesquiteTrunk.logln("Checksum passed for incorporating data alignment. ");
			if (numCharsAligned< icOrigEnd-icOrigStart+1 && numTaxaInOrig == origData.getNumTaxa()) {  // the aligned section has fewer characters than the old section
				int ic = icOrigEnd;  //set ic to the end of the piece aligned in the original matrix
				int numToDelete=0;
				int firstDelete = -1;
				while (origData.entirelyInapplicable(ic) && ic >= icOrigStart + numCharsAligned) {
					if (origData.entirelyInapplicable(ic)) {
						numToDelete++;
						firstDelete=ic;
					}
					ic--;
				}
				if (firstDelete>=0){
					origData.deleteCharacters(firstDelete, numToDelete, false); 
					origData.deleteInLinked(firstDelete, numToDelete, false); 
				}
			}
		}
		return rect;
		
	}
	
	public static void getDefaultGapCosts(MesquiteInteger gapOpen, MesquiteInteger gapExtend, MesquiteInteger gapOpenTerminal, MesquiteInteger gapExtendTerminal){
		if (gapOpen!=null)
			gapOpen.setValue(8);
		if (gapExtend!=null)
			gapExtend.setValue(3);
		if (gapOpenTerminal!=null)
			gapOpenTerminal.setValue(2);
		if (gapExtendTerminal!=null)
			gapExtendTerminal.setValue(2);
	}
	
	/*.................................................................................................................*/
	public static int[][] getDefaultSubstitutionCosts(int alphabetLength){
		
		int subs[][] =  new int[alphabetLength][alphabetLength];	
		for (int i = 0; i<alphabetLength; i++) 
			for (int j = 0; j<alphabetLength; j++) {
				if (i!=j)
					subs[i][j] = 5;
				else
					subs[i][j] = 0;
			}
		if (alphabetLength==4) {  //then  it should be DNA
			subs[0][1] = subs[1][0] = subs[0][3] = subs[3][0] = 10; //transversions involving A
			subs[2][3] = subs[3][2] = subs[1][2] = subs[2][1]= 10; //transversions involving G
		}
		return subs;
	}
	
	/*.................................................................................................................*/
	/** Given a matrix, int[][] subs, containing costs for going from state i to state j, 
	 *  and two int a and b representing 32-bit state sets, and the alphabetLength, this
	 *  method calculates the minimum cost for going between the two statesets. 
	 *  Note that this presumes multiple states in each state set represents ambiguity!
	 */
	public static int getCost(int[][] subs, int a, int b, int alphabetLength) {
		if ((a & b) != 0 || CategoricalState.isUnassignedInt(a) || CategoricalState.isUnassignedInt(b))  //  the sets intersect, so there is no cost
			return 0;
		int minCost = Integer.MAX_VALUE;
		for (int elementA=0; elementA<alphabetLength; elementA++)   
			if (((1<<elementA)&a)!=0){ //then e is in set a
				for (int elementB=0; elementB<alphabetLength; elementB++)
					if (((1<<elementB)&b)!=0){ //then e is in set b
						if (minCost>subs[elementA][elementB])
							minCost = subs[elementA][elementB];   // find the minimum cost between a member of a and a member of b
					}
			}
		if (minCost==Integer.MAX_VALUE) {
			return 0;
		}
		return minCost;
		
	}
	
	/*.................................................................................................................*/
	public static boolean queryGapCosts(MesquiteWindow parent, MesquiteModule module, MesquiteInteger gapOpen, MesquiteInteger gapExtend, MesquiteInteger gapOpenTerminal, MesquiteInteger gapExtendTerminal) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(parent, "Gap Costs",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		
		IntegerField gapOpenField=null;
		IntegerField gapExtendField=null;
		IntegerField gapOpenTerminalField=null;
		IntegerField gapExtendTerminalField=null;
		
		if (gapOpen!=null)
			gapOpenField = dialog.addIntegerField("Gap Opening Cost", gapOpen.getValue(), 5, 0, 1000);
		if (gapExtend!=null)
			gapExtendField = dialog.addIntegerField("Gap Extension Cost", gapExtend.getValue(), 5, 0, 1000);
		if (gapOpenTerminal!=null)
			gapOpenTerminalField = dialog.addIntegerField("Terminal Gap Opening Cost", gapOpenTerminal.getValue(), 5, 0, 1000);
		if (gapExtendTerminal!=null)
			gapExtendTerminalField = dialog.addIntegerField("Terminal Gap Extension Cost", gapExtendTerminal.getValue(), 5, 0, 1000);
		
		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			if (gapOpen!=null)
				gapOpen.setValue(gapOpenField.getValue());
			if (gapExtend!=null)
				gapExtend.setValue(gapExtendField.getValue());
			if (gapOpenTerminal!=null)
				gapOpenTerminal.setValue(gapOpenTerminalField.getValue());
			if (gapExtendTerminal!=null)
				gapExtendTerminal.setValue(gapExtendTerminalField.getValue());
			module.storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}

	/*.................................................................................................................*/
	public static boolean querySubCosts(MesquiteWindow parent, MesquiteModule module, Integer2DArray subArray, String[] labels) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(parent, "Substitution Costs",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		
		IntegerSqMatrixFields subsField;
		int[][] matrix = subArray.getMatrix();
		if (matrix.length<11)
			subsField = new IntegerSqMatrixFields(dialog, matrix, labels, false, false, 3);
		else
			subsField = new IntegerSqMatrixFields(dialog, matrix, labels, false, false, 2);
		subsField.setLastValueEditable(true);
		
		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			module.storePreferences();
			subArray.setValues(subsField.getInteger2DArray());
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	public static boolean integrateAlignment(long[][] alignedMatrix, MolecularData data, int icStart, int icEnd, int itStart, int itEnd){
		if (alignedMatrix == null || data == null)
			return false;
		AlignUtil util = new AlignUtil();
		Rectangle problem = null;
		//alignedMatrix.setName("Aligned (" + data.getName() + ")");
		boolean wasSel;
		if (data.anySelected()) {
			wasSel = true;
		}
		else {
			wasSel = false;
		}
		MesquiteTrunk.mesquiteTrunk.logln("Alignment for " + (icEnd-icStart+1) + " sites; aligned to " + alignedMatrix.length + " sites.");
		problem = util.forceAlignment(data, icStart, icEnd, itStart, itEnd, 0, alignedMatrix);
		if (wasSel) {
			data.deselectAll();
			int numCharsOrig = icEnd-icStart+1;
			if (alignedMatrix.length>numCharsOrig)
				numCharsOrig = alignedMatrix.length;
			for (int i = icStart; i<icStart + numCharsOrig; i++)
				data.setSelected(i, true);

		}
		MesquiteTrunk.mesquiteTrunk.logln("Alignment completed.");
		data.removeCharactersThatAreEntirelyGaps(icStart, icEnd, true);  // added 1 June 2016

		return true;
	}	
	/*.................................................................................................................*/
	public static boolean hasSomeAlignedSites(long[][] alignedMatrix){
		if (alignedMatrix == null)
			return false;
		int numSequences = Long2DArray.numFullRows(alignedMatrix);
		int numSites = Long2DArray.numFullColumns(alignedMatrix);
		for (int k = 0; k<numSites; k++) {
			boolean foundData = false;
			for (int i =0; i<numSequences; i++) {
				if (!CategoricalState.isInapplicable(alignedMatrix[k][i])) {
					if (foundData)
						return true;
					foundData=true;
				}
			}
		}
		return false;
	}	

}

