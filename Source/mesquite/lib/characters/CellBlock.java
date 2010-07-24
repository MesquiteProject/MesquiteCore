package mesquite.lib.characters;

/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.73, July 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

import mesquite.lib.*;
import mesquite.lib.table.*;
import mesquite.categ.lib.*;

/* ======================================================================== */
public class CellBlock {
	CategoricalData data;
	MesquiteTable table;

	Bits whichTaxa;
	Bits originalWhichTaxa;
	MesquiteInteger firstCharInBlock;
	MesquiteInteger lastCharInBlock;
	int originalFirstCharInFullBlock;
	int originalLastCharInFullBlock;
	int originalFirstCharInBlock;
	int originalLastCharInBlock;
	int currentFirstCharInBlock = 0;
	int currentLastCharInBlock = 0;
	int previousFirstCharInBlock = 0;
	int previousLastCharInBlock = 0;

	MesquiteInteger firstTaxonInBlock;
	MesquiteInteger lastTaxonInBlock;
//	int originalFirstTaxonInFullBlock;
//	int originalLastTaxonInFullBlock;
	int originalFirstTaxonInBlock;
	int originalLastTaxonInBlock;
	int currentFirstTaxonInBlock = 0;
	int currentLastTaxonInBlock = 0;
//	int previousFirstTaxonInBlock = 0;
//	int previousLastTaxonInBlock = 0;

	int maxLeftMovement=0;
	int maxRightMovement=0;

	int currentLeftMovement = 0;
	int currentRightMovement = 0;

	boolean atEdgeLeft = false;
	boolean atEdgeRight = false;
	boolean isRight = false;
	boolean isLeft = false;

	boolean locked = false;
	boolean useWhichTaxaBits = false;

	public CellBlock(CategoricalData data, MesquiteTable table) {
		this.data = data;
		this.table = table;
		whichTaxa = new Bits(data.getNumTaxa());
		whichTaxa.clearAllBits();
		originalWhichTaxa = new Bits(data.getNumTaxa());
		originalWhichTaxa.clearAllBits();
	}
	/*.................................................................................................................*/
	public void reset(){    
		currentLeftMovement = 0;
		currentRightMovement = 0;
		setAllBlocks(originalFirstCharInBlock, originalLastCharInBlock,originalFirstTaxonInBlock,originalLastTaxonInBlock);
		locked = false;
	}
	/*.................................................................................................................*/
	public void restoreCharBlock(MesquiteBoolean dataChanged){    // takes data that is currently at currentBlock location and move to original location
		if (!(currentLeftMovement==0 && currentRightMovement==0)) {
			int distanceToMove = originalFirstCharInBlock - previousFirstCharInBlock;
			int added = data.moveCells(previousFirstCharInBlock,previousLastCharInBlock, distanceToMove, whichTaxa, true, false, true, false,dataChanged,null);

			table.redrawBlock(MesquiteInteger.minimum(previousFirstCharInBlock, originalFirstCharInBlock), MesquiteInteger.maximum(previousLastCharInBlock, originalLastCharInBlock), whichTaxa);
			reset();
		}
	}
	/*.................................................................................................................*/


	/*.................................................................................................................*/
	public void setMaximumMovements (int maxLeftMovement, int maxRightMovement){
		this.maxLeftMovement = maxLeftMovement;
		this.maxRightMovement = maxRightMovement;
	}
	/*.................................................................................................................*/
	public void setMaximumMovements (){
		this.maxLeftMovement = gapColumnsToLeftOfBlock();
		this.maxRightMovement = gapColumnsToRightOfBlock();
	}
	public int getMaxLeftMovement (){
		return  maxLeftMovement;
	}
	public int getMaxRightMovement (){
		return maxRightMovement;
	}
	/*.................................................................................................................*/
	public int availableLeftMovement (){
		return maxLeftMovement-currentLeftMovement;
	}
	/*.................................................................................................................*/
	public int availableRightMovement (){
		return maxRightMovement-currentRightMovement;
	}
	/*.................................................................................................................*/
	public int movementAllowed (int candidateMovement, boolean canExpand){
		if (candidateMovement<0){  // move to left
			if (canExpand & isLeft & atEdgeLeft)   // any amount can be accommodated
				return candidateMovement;
			if ((-candidateMovement)<=availableLeftMovement())  // it is acceptable
				return candidateMovement;
			return -availableLeftMovement();
		}
		if (candidateMovement>0){  // move to right
			if (canExpand & isRight & atEdgeRight)
				return candidateMovement;
			if (candidateMovement<=availableRightMovement())  // it is acceptable
				return candidateMovement;
			return availableRightMovement();
		}
		return 0;
	}
	/*.................................................................................................................*/
	public void switchCharBlock(int icStart, int icEnd){  
		originalFirstCharInBlock=icStart;
		originalLastCharInBlock=icEnd;
		currentFirstCharInBlock=icStart;
		currentLastCharInBlock=icEnd;
		previousFirstCharInBlock=icStart;
		previousLastCharInBlock=icEnd;
	}
	/*.................................................................................................................*/
	public Bits getWhichTaxa() {
		return whichTaxa;
	}
	/*.................................................................................................................*/
	public void resetWhichTaxa() {
		whichTaxa.clearAllBits();
		for (int it=currentFirstTaxonInBlock; it<=currentLastTaxonInBlock; it++)
			whichTaxa.setBit(it);
	}
	/*.................................................................................................................*/
	public void setWhichTaxa(int itStart, int itEnd) {
		whichTaxa.clearAllBits();
		for (int it=itStart; it<=itEnd; it++)
			whichTaxa.setBit(it);
	}
	/*.................................................................................................................*/
	public String listWhichTaxa() {
		StringBuffer sb = new StringBuffer();
		for (int it=0; it<=whichTaxa.getSize(); it++)
			if (whichTaxa.isBitOn(it))
				sb.append(" " + it);
		return sb.toString();
				
	}
	/*.................................................................................................................*/
	public void resetWhichTaxa(boolean reverse) {
		whichTaxa.setAllBits();
		for (int it=currentFirstTaxonInBlock; it<=currentLastTaxonInBlock; it++)
			whichTaxa.clearBit(it);
	}
	/*.................................................................................................................*/
	public void reverseWhichTaxa() {
		whichTaxa.invertAllBits();
	}

	/*.................................................................................................................*/
	public void setCurrentTaxa(int itStart,int itEnd) {
		currentFirstTaxonInBlock=itStart;
		currentLastTaxonInBlock=itEnd;
		resetWhichTaxa();
	}
	/*.................................................................................................................*/
	public void setAllBlocks(int icStart, int icEnd, int itStart, int itEnd){  
		originalFirstCharInBlock=icStart;
		originalLastCharInBlock=icEnd;
		originalFirstTaxonInBlock=itStart;
		originalLastTaxonInBlock=itEnd;
		currentFirstCharInBlock=icStart;
		currentLastCharInBlock=icEnd;
		currentFirstTaxonInBlock=itStart;
		currentLastTaxonInBlock=itEnd;
		previousFirstCharInBlock=icStart;
		previousLastCharInBlock=icEnd;
	//	previousFirstTaxonInBlock=itStart;
	//	previousLastTaxonInBlock=itEnd;

	}
	/*.................................................................................................................*/
	public void setAllBlocks(int icStart, int icEnd, Bits whichTaxa){  
		originalFirstCharInBlock=icStart;
		originalLastCharInBlock=icEnd;
		originalWhichTaxa = whichTaxa.cloneBits();
		currentFirstCharInBlock=icStart;
		currentLastCharInBlock=icEnd;
		this.whichTaxa = whichTaxa.cloneBits();
		previousFirstCharInBlock=icStart;
		previousLastCharInBlock=icEnd;
	//	previousFirstTaxonInBlock=itStart;
	//	previousLastTaxonInBlock=itEnd;

	}
	/*.................................................................................................................*/
	public void setOriginalFullBlockOnTouch(int icStart, int icEnd, int itStart, int itEnd){  
		originalFirstCharInFullBlock=icStart;
		originalLastCharInFullBlock=icEnd;
		//originalFirstTaxonInFullBlock=itStart;
		//originalLastTaxonInFullBlock=itEnd;
	}
	/*.................................................................................................................*/
	public void setOriginalBlock(int icStart, int icEnd, int itStart, int itEnd){  
		originalFirstCharInBlock=icStart;
		originalLastCharInBlock=icEnd;
		originalFirstTaxonInBlock=itStart;
		originalLastTaxonInBlock=itEnd;
	}
	/*.................................................................................................................*/
	public void setOriginalFirstCharInBlock(int icStart){  
		originalFirstCharInBlock=icStart;
	}
	/*.................................................................................................................*/
	public int getOriginalFirstCharInBlock(){  
		return originalFirstCharInBlock;
	}
	/*.................................................................................................................*/
	public int getOriginalLastCharInBlock(){  
		return originalLastCharInBlock;
	}
	/*.................................................................................................................*
	public int getOriginalFirstTaxonInBlock(){  
		return originalFirstTaxonInBlock;
	}
	/*.................................................................................................................*
	public int getOriginalLastTaxonInBlock(){  
		return originalLastTaxonInBlock;
	}
	/*.................................................................................................................*/
	public int getOriginalFirstCharInFullBlock(){  
		return originalFirstCharInFullBlock;
	}
	/*.................................................................................................................*/
	public int getOriginalLastCharInFullBlock(){  
		return originalLastCharInFullBlock;
	}
	/*.................................................................................................................*
	public int getOriginalFirstTaxonInFullBlock(){  
		return originalFirstTaxonInFullBlock;
	}
	/*.................................................................................................................*
	public int getOriginalLastTaxonInFullBlock(){  
		return originalLastTaxonInFullBlock;
	}
	/*.................................................................................................................*/
	public void setCurrentCharBlock(int icStart, int icEnd){  
		currentFirstCharInBlock=icStart;
		currentLastCharInBlock=icEnd;
	}
	/*.................................................................................................................*/
	public void setCurrentBlock(int icStart, int icEnd, int itStart, int itEnd){  
		currentFirstCharInBlock=icStart;
		currentLastCharInBlock=icEnd;
		currentFirstTaxonInBlock=itStart;
		currentLastTaxonInBlock=itEnd;
	}
	/*.................................................................................................................*/
	public void setCurrentBlock(int icStart, int icEnd, Bits whichTaxa){  
		currentFirstCharInBlock=icStart;
		currentLastCharInBlock=icEnd;
		this.whichTaxa = whichTaxa.cloneBits();
	}
	/*.................................................................................................................*/
	public void shiftCurrentBlock(int shift){  
		currentFirstCharInBlock+=shift;
		currentLastCharInBlock+=shift;
		//currentFirstTaxonInBlock+=shift;
		//currentLastTaxonInBlock+=shift;
	}
	/*.................................................................................................................*/
	public int getCurrentFirstCharInBlock(){  
		return currentFirstCharInBlock;
	}
	/*.................................................................................................................*/
	public int getCurrentLastCharInBlock(){  
		return currentLastCharInBlock;
	}
	/*.................................................................................................................*
	public int getCurrentFirstTaxonInBlock(){  
		return currentFirstTaxonInBlock;
	}
	/*.................................................................................................................*
	public int getCurrentLastTaxonInBlock(){  
		return currentLastTaxonInBlock;
	}
	/*.................................................................................................................*/
	public void setPreviousCharBlock(int icStart, int icEnd){  
		previousFirstCharInBlock=icStart;
		previousLastCharInBlock=icEnd;
	}
	/*.................................................................................................................*/
	public void addToCharBlockValues(int added){  
		originalFirstCharInBlock+=added;
		originalLastCharInBlock+=added;
		currentFirstCharInBlock+=added;
		currentLastCharInBlock+=added;
		previousFirstCharInBlock+=added;
		previousLastCharInBlock+=added;
	}

	/*.................................................................................................................*/
	public void transferCurrentToPrevious(){  
		previousFirstCharInBlock=currentFirstCharInBlock;
		previousLastCharInBlock=currentLastCharInBlock;
		//previousFirstTaxonInBlock=currentFirstTaxonInBlock;
		//previousLastTaxonInBlock=currentLastTaxonInBlock;
	}
	/*.................................................................................................................*/
	public void setPreviousBlock(int icStart, int icEnd, int itStart, int itEnd){  
		previousFirstCharInBlock=icStart;
		previousLastCharInBlock=icEnd;
		//previousFirstTaxonInBlock=itStart;
		//previousLastTaxonInBlock=itEnd;
	}
	/*.................................................................................................................*/
	public int getPreviousFirstCharInBlock(){  
		return previousFirstCharInBlock;
	}
	/*.................................................................................................................*/
	public int getPreviousLastCharInBlock(){  
		return previousLastCharInBlock;
	}
	/*.................................................................................................................*
	public int getPreviousFirstTaxonInBlock(){  
		return previousFirstTaxonInBlock;
	}
	/*.................................................................................................................*
	public int getPreviousLastTaxonInBlock(){  
		return previousLastTaxonInBlock;
	}
	/*.................................................................................................................*/
	public void adjustToMove(int movement) {
		if (movement<0){   //moving left
			currentLeftMovement-=movement;
			currentRightMovement+=movement;
		}
		else if (movement>0){   //moving right
			currentLeftMovement-=movement;
			currentRightMovement+=movement;
		}
		shiftCurrentBlock(movement);

	}
	/*.................................................................................................................*/
	public void addCharacters(int added, boolean toStart){  
		if (toStart) {
			originalFirstCharInBlock+=added;
			originalLastCharInBlock+=added;
			currentFirstCharInBlock+=added;
			currentLastCharInBlock+=added;
			previousFirstCharInBlock+=added;
			previousLastCharInBlock+=added;
		}
		if (isLeft && toStart)
			maxLeftMovement+=added;
		if (isRight && !toStart)
			maxRightMovement+=added;
	}
	/*.................................................................................................................*/
	public int gapColumnsToLeftOfBlock() {
		atEdgeLeft = false;
		if (currentFirstCharInBlock<=0) {
			atEdgeLeft = true;
			return 0;
		}
		int count = 0;
		for (int ic = currentFirstCharInBlock-1; ic>=0; ic--) {
			if (data.inapplicableBlock(ic, ic, whichTaxa)) {
				count++;
				if (ic==0)
					atEdgeLeft=true;
			}
			else
				break;
		}
		return count;
	}
	/*.................................................................................................................*/
	public int gapColumnsToRightOfBlock() {
		atEdgeRight=false;
		if (currentLastCharInBlock>=data.getNumChars()-1) {
			atEdgeRight=true;
			return 0;
		}
		int count = 0;
		for (int ic = currentLastCharInBlock+1; ic<data.getNumChars(); ic++) {
			if (data.inapplicableBlock(ic, ic, whichTaxa)){
				count++;
				if (ic==data.getNumChars()-1)
					atEdgeRight=true;
			}
			else
				break;
		}
		return count;
	}
	/*.................................................................................................................*/
	public void getBlockInSequence(int ic, int it, MesquiteInteger firstInBlock, MesquiteInteger lastInBlock, boolean wholeSelectedBlock, boolean wholeSequenceLeft, boolean wholeSequenceRight, MesquiteBoolean cellHasInapplicable, MesquiteBoolean leftIsInapplicable, MesquiteBoolean rightIsInapplicable){  // determines the block that was touched
		cellHasInapplicable.setValue(false);
		leftIsInapplicable.setValue(false);
		rightIsInapplicable.setValue(false);
		firstInBlock.setValue(0);
		lastInBlock.setValue(data.getNumChars()-1);
		if (ic>0)
			if (data.isInapplicable(ic-1, it))
				leftIsInapplicable.setValue(true);
		if (ic<data.getNumChars())
			if (data.isInapplicable(ic+1, it))
				rightIsInapplicable.setValue(true);
		/*
		 * 		if (data.isInapplicable(ic, it)) {
			firstInBlock.setValue(ic);
			lastInBlock.setValue(-1);
			cellHasInapplicable.setValue(true);
			return;
		}
		 */
		if (wholeSequenceLeft) {
			firstInBlock.setValue(data.firstApplicable(it));
		} 
		else if (wholeSelectedBlock) {
			for (int i=ic; i>=0; i--) {   // find first unselected cell to the left of this point
				if (!table.isCellSelected(i, it)){  // should be isToolInapplicable
					firstInBlock.setValue(i+1);
					break;
				}
			}
		}
		else {
			for (int i=ic; i>=0; i--) {   // find first gap to the left of this point
				if (data.isInapplicable(i, it)){  // should be isToolInapplicable
					firstInBlock.setValue(i+1);
					break;
				}
			}
		}


		if (wholeSequenceRight) {
			lastInBlock.setValue(data.lastApplicable(it));
		}
		else if (wholeSelectedBlock) {
			for (int i=ic; i<data.getNumChars(); i++) {  // find first unselected cell to the right of this point
				if (!table.isCellSelected(i, it)){ 
					lastInBlock.setValue(i-1);
					return;
				}
			}
		}
		else {
			for (int i=ic; i<data.getNumChars(); i++) {  // find first gap to the right of this point
				if (data.isInapplicable(i, it)){  // should be isToolInapplicable
					lastInBlock.setValue(i-1);
					return;
				}
			}
		}
	}
	/** Gets the cell block that contains the cells of character ic from itStart to itEnd */
	/*.................................................................................................................*/
	public void getCellBlock(int icStart, int icEnd, int itStart, int itEnd, MesquiteInteger firstInBlock, MesquiteInteger lastInBlock, boolean wholeSelectedBlock, boolean wholeSequenceLeft, boolean wholeSequenceRight, MesquiteBoolean cellHasInapplicable, MesquiteBoolean leftIsInapplicable, MesquiteBoolean rightIsInapplicable){  // determines the block that was touched
		Bits whichTaxa = new Bits(data.getNumTaxa());
		for (int it=itStart; it<=itEnd; it++)
			whichTaxa.setBit(it);
		getCellBlock( icStart,  icEnd, whichTaxa,  firstInBlock,  lastInBlock,  wholeSelectedBlock,  wholeSequenceLeft,  wholeSequenceRight,  cellHasInapplicable,  leftIsInapplicable,  rightIsInapplicable);  // determines the block that was touched

	}
	/*.................................................................................................................*/
	public void getCellBlock(int icStart, int icEnd, Bits whichTaxa, MesquiteInteger firstInBlock, MesquiteInteger lastInBlock, boolean wholeSelectedBlock, boolean wholeSequenceLeft, boolean wholeSequenceRight, MesquiteBoolean cellHasInapplicable, MesquiteBoolean leftIsInapplicable, MesquiteBoolean rightIsInapplicable){  // determines the block that was touched
		cellHasInapplicable.setValue(false);
		leftIsInapplicable.setValue(false);
		rightIsInapplicable.setValue(false);
		firstInBlock.setValue(0);
		lastInBlock.setValue(data.getNumChars()-1);
		if (icStart>0)
			if (data.inapplicableBlock(icStart-1, icStart-1, whichTaxa))
				leftIsInapplicable.setValue(true);
		if (icEnd<data.getNumChars())
			if (data.inapplicableBlock(icEnd, icEnd, whichTaxa))
				rightIsInapplicable.setValue(true);
		if (wholeSequenceLeft) {
			firstInBlock.setValue(data.firstApplicable(whichTaxa));
		} 
		else if (wholeSelectedBlock) {
			for (int i=icStart; i>=0; i--) {   // find first unselected cell to the left of this point
				if (!table.isAnyCellSelectedInBlock(i, i, whichTaxa)){ 
					firstInBlock.setValue(i+1);
					break;
				}
			}
		}
		else {
			for (int i=icStart; i>=0; i--) {   // find first gap to the left of this point
				if (data.inapplicableBlock(i, i, whichTaxa)){  // should be isToolInapplicable
					firstInBlock.setValue(i+1);
					break;
				} else if (i<icStart&&!data.applicableInBothCharacters(i,i+1,whichTaxa)) {
					firstInBlock.setValue(i+1);
					break;
				}
			}
		}


		if (wholeSequenceRight) {
			lastInBlock.setValue(data.lastApplicable(whichTaxa));
		}
		else if (wholeSelectedBlock) {
			for (int i=icEnd; i<data.getNumChars(); i++) {  // find first unselected cell to the right of this point
				if (!table.isAnyCellSelectedInBlock(i, i, whichTaxa)){ 
					lastInBlock.setValue(i-1);
					return;
				}
			}
		}
		else {
			for (int i=icEnd; i<data.getNumChars(); i++) {  // find first gap to the right of this point
				if (data.inapplicableBlock(i,i, whichTaxa)){  // should be isToolInapplicableú
					lastInBlock.setValue(i-1);
					return;
				} else if (i>icEnd &&!data.applicableInBothCharacters(i,i-1,whichTaxa)) {
					lastInBlock.setValue(i-1);
					return;
				}
			}
		}
	}

	/* ............................................................................................................... */
	/** Select block of cells. */
	public void deselectOthersAndSelectBlock() {
		table.deSelectAndRedrawOutsideBlock(currentFirstCharInBlock, currentLastCharInBlock, whichTaxa);
		table.selectBlock(currentFirstCharInBlock, currentLastCharInBlock, whichTaxa);

	}

	public int getCurrentLeftMovement() {
		return currentLeftMovement;
	}
	public void setCurrentLeftMovement(int currentLeftMovement) {
		this.currentLeftMovement = currentLeftMovement;
	}
	public int getCurrentRightMovement() {
		return currentRightMovement;
	}
	public void setCurrentRightMovement(int currentRightMovement) {
		this.currentRightMovement = currentRightMovement;
	}
	public boolean isAtEdgeLeft() {
		return atEdgeLeft;
	}
	public void setAtEdgeLeft(boolean atEdgeLeft) {
		this.atEdgeLeft = atEdgeLeft;
	}
	public boolean isAtEdgeRight() {
		return atEdgeRight;
	}
	public void setAtEdgeRight(boolean atEdgeRight) {
		this.atEdgeRight = atEdgeRight;
	}
	public boolean getLeft() {
		return isLeft;
	}
	public boolean getRight() {
		return isRight;
	}
	public void setRight(boolean isRight) {
		this.isRight = isRight;
	}
	public void setLeft(boolean isLeft) {
		this.isLeft = isLeft;
	}
	public boolean isLocked() {
		return locked;
	}
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	public boolean isUseWhichTaxaBits() {
		return useWhichTaxaBits;
	}
	public void setUseWhichTaxaBits(boolean useWhichTaxaBits) {
		this.useWhichTaxaBits = useWhichTaxaBits;
	}
}

