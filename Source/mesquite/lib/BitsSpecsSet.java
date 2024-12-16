/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib;

import java.awt.*;

/*==========================  Mesquite Basic Class Library    ==========================*/
/*===  the basic classes used by the trunk of Mesquite and available to the modules

/* ======================================================================== */
/** A specset with bit information.*/
public abstract class BitsSpecsSet extends SpecsSet  {
	Bits myBits;
	public BitsSpecsSet(String name, int numParts){
		super(name, numParts);
		myBits = new Bits(numParts);
	}
	public String getTypeName(){
		return "Bits specificaton set";
	}
	
	public String toString(int ic){
		if (isBitOn(ic))
			return "true";
		else
			return "false";
	}
	public boolean allDefault(){
		return !myBits.anyBitsOn();
	}
 	/*.................................................................................................................*/
 	/** Sets the value for part "part" to be the same as that at part "otherPart" in the incoming specsSet*/
	public void equalizeSpecs(SpecsSet other, int otherPart, int part){
		if (other instanceof BitsSpecsSet){
			setSelected(part, ((BitsSpecsSet)other).isBitOn(otherPart));
		}
	}
 	/*.................................................................................................................*/
	/** Returns a string that simply lists the on bits, NOT zero-based. */
	public String getListOfOnBits(String delimiter){
		StringBuffer sb= new StringBuffer();
		boolean first = true;
		for (int i=0; i<getNumberOfParts(); i++)
			if (isPresent(i))
				if (first){
					sb.append("" + (i+1));
					first = false;
				}
				else
					sb.append(delimiter + (i+1));
		return sb.toString();
	}
 	/*.................................................................................................................*/
	//We can't remember why this doesn't use the selected field already within the superclass Associable
	/** Returns bits. */
	public Bits getBits(){
		return myBits;
	}
	/*.................................................................................................................*/
	/** Returns first part on. */
	public int firstBitOn() {
		return myBits.firstBitOn();
	}
	/*.................................................................................................................*/
	/** Returns if part ic is on. */
	public boolean isBitOn(int ic){
		return myBits.isBitOn(ic);
	}
	/*.................................................................................................................*/
	/** Returns if part ic is on. */
	public boolean getSelected(int ic){
		return myBits.isBitOn(ic);
	}
	/*.................................................................................................................*/
	/** Returns number of bits on. */
	public int numBitsOn(){
		return myBits.numBitsOn();
	}
 	/*------------------------------------------*/
 	public boolean allBitsOn () {
		return myBits.allBitsOn();
 	}

	/*.................................................................................................................*/
	/** Returns if part ic is on. */
	public boolean isPresent(int ic){
		return myBits.isBitOn(ic);
	}
	/** Returns if any part are on */
	public boolean anySelected(){
		return myBits.anyBitsOn();
		
	}
	/** Returns how many are on */
	public int numberSelected(){
		return myBits.numBitsOn();
		
	}
	/** Set bits of this to be the same as passed bits */
	public void setBits(Bits bits){
		setDirty(true);
		if (bits!=null)
			bits.copyBits(myBits);
	}
	/** Set part ic to either on or not according to boolean */
	public void setSelected(int ic, boolean select){
		setDirty(true);
		if (select)
			myBits.setBit(ic);
		else
			myBits.clearBit(ic);
	}
	/** Deselect all part */
	public void deselectAll(){
		setDirty(true);
		myBits.clearAllBits();
	}
	/** Select all parts */
	public void selectAll(){
		setDirty(true);
		myBits.setAllBits();
	}
	/*-----------------------------------------*/
	/** Returns index of first selected part */
	public int firstSelected() {
		if (!anySelected())
			return -1;
		for (int i = 0; i<getNumberOfParts(); i++) {
			if (myBits.isBitOn(i))
				return i;
		}
		return -1;
	}
	/*-----------------------------------------*/
	/** Returns index of last selected part */
	public int lastSelected() {
		if (!anySelected())
			return -1;
		for (int i = getNumberOfParts()-1; i>=0; i--) {
			if (myBits.isBitOn(i)) {
				return i;
				
			}
		}
		return -1;
	}
	/*-----------------------------------------*/
	/** Returns selected directly */
	public Bits getSelectedBits() {
		return myBits;
	}
	/*-----------------------------------------*/
	/** returns number of parts that are selected or, if onlyCountSelected is false, all parts*/
	public int numberSelected(boolean onlyCountSelected) {
		if (onlyCountSelected)
			return numberSelected();
		else
			return getNumberOfParts();
	}
	/*-----------------------------------------*/
	/** Returns whether there are selected parts that form a contiguous selection */
	public boolean contiguousSelection() {
		if (!anySelected())
			return false;
		int firstSelected = myBits.nextBit(0, true);
		if (firstSelected<0)  // none is selected
			return false;
		int nextNotSelected = myBits.nextBit(firstSelected+1,false);  // first one, after the block, that is not selected
		if (nextNotSelected>0) {
			return (myBits.nextBit(nextNotSelected+1,true)==-1);  //no more have been found
		}
		return true;
	}
	/*.................................................................................................................*/
	/** Add num parts just after "starting" (filling with default values)  */
  	public boolean addParts(int starting, int num){  
		setDirty(true);
 		myBits.addParts(starting, num); //TODO: set default (for inclusion, set to on!!!)
 		numParts = myBits.getSize();
		return true;
	}
	/*.................................................................................................................*/
	/** Delete num parts from and including "starting"  */
	public boolean deleteParts(int starting, int num){ 
		setDirty(true);
 		myBits.deleteParts(starting, num);
 		numParts = myBits.getSize();
		return true;
	}
	/*.................................................................................................................*/
	/** Deletes parts flagged in Bits.*/
	protected boolean deletePartsFlagged(Bits toBeDeleted){ 
		setDirty(true);
		myBits.deletePartsFlagged(toBeDeleted); 
 		numParts = myBits.getSize();
		return true;
	}
	/*.................................................................................................................*/
	/** Deletes parts by blocks.
	 * blocks[i][0] is start of block; blocks[i][1] is end of block
	 * Assumes that these blocks are in sequence, non-overlapping, etc!!! *
	protected boolean deletePartsBy Blocks(int[][] blocks){ 
		setDirty(true);
 		myBits.deletePartsBy Blocks(blocks);
 		numParts = myBits.getSize();
		return true;
	}
	/*.................................................................................................................*/
 	/** */
	public boolean moveParts(int starting, int num, int justAfter){  
		setDirty(true);
 		myBits.moveParts(starting, num, justAfter);
		return true;
	}
	/*.................................................................................................................*/
 	/** */
	public boolean swapParts(int first, int second, boolean notify){  
		setDirty(true);
 		myBits.swapParts(first, second);
		return true;
	}
}

