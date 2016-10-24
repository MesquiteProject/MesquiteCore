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
import java.util.Random;

/*==========================  Mesquite Basic Class Library    ==========================*/
/**  Bits - an object to contain and manage a field of booleans.  This was invented before the Java Bitfield
was discovered.  Perhaps not needed. */
public class Bits implements Listable{
	int[] array;
	int numBits;
	int numInts;
	NameReference nr;
	static final int SIZECHUNK = 32;
	static final int SIZECHUNKMINUS1 = 31;
	public Bits (int numBits) {
 		this.numBits = numBits;
 		numInts = numBits/SIZECHUNK + 1;
 		if (numInts/2*2!=numInts)
 			numInts++;
 		array = new int[numInts];
 		for (int i=0; i<numInts; i++) {
 			array[i]=0;
 		}
 	}
	//used for Associables that might need to record whether reference is to part or in between part
	//intially for MesquiteTree to know if info applies to node or branch ancestral to node
	boolean between = false;
	public void setBetweenness(boolean b){
		between = true;  
	}
	public boolean isBetween(){
		return between;
	}
	public int getSize(){
 		return numBits;
 	}
 	public int getNumInts(){
 		return numInts;
 	}
 	public Bits cloneBits(){
 		Bits b = new Bits(numBits);
 		for (int i=0; i<numInts; i++)
 			b.array[i]=array[i];
 		return b;
 	}
 	public void copyBits(Bits b){
 		if (b==null)
 			return;
 		for (int i=0; i<b.getNumInts(); i++)
 			b.array[i]=0;
		for (int i=0; i<numInts && i<b.getNumInts(); i++)
 			b.array[i]=array[i];
 	}
 	public void setBits(Bits b){
 		if (b==null)
 			return;
 		for (int i=0; i<getNumInts(); i++)
 			array[i]=0;
		for (int i=0; i<numInts && i<b.getNumInts(); i++)
 			array[i]=b.array[i];
 	}
 	public void standardizeComplement( int standard){
 		if (isBitOn(standard))
 			return;
 		invertAllBits();
 	}
	public void setNameReference(NameReference nr){
 		this.nr = nr;
 	}
 	public NameReference getNameReference(){
 		return nr;
 	}
	public String getName(){
		if (nr!=null)
			return nr.getValue();
		else
			return "";
	}
 	/*------------------------------------------*/
 	public void resetSize (int newNumBits) {  //TODO: should be setNumberOfParts
 		if (numBits==newNumBits)
 			return;
 		int[] newArray;
 		int newNumInts = newNumBits/SIZECHUNK + 1;
 		if (newNumInts/2*2!=newNumInts)
 			newNumInts++;
 		newArray = new int[newNumInts];
 		for (int i=0; i<newNumInts; i++) {
 			if (i< numInts)
 				newArray[i]=array[i];
 			else
 				newArray[i]=0;
 		}
 		numInts = newNumInts;
  		numBits = newNumBits;
		array=newArray;
 	}
 	/*------------------------------------------*/
 	/**Inserts num (cleared) bits just after bit "starting"*/
 	public void addParts (int starting, int num) {
		if (num==0)
			return;
		if (starting<0)
			starting = -1;
		else if (starting>=numBits)
			starting = numBits-1;
		int newNumBits = numBits + num;

 		int startingInt = inWhichInteger(starting);
  		int newNumInts = newNumBits/SIZECHUNK + 1;
		
 		if (newNumInts/2*2!=newNumInts) //odd; add 1 to make even
 			newNumInts++;
 		
  		int[] newArray = new int[newNumInts];
    	for (int i=0; i< newNumInts; i++) 
 			newArray[i]=0;
 		
 		//first, put integers up to start of insertion
  		for (int ibit=0; ibit<=starting; ibit++) 
 			if (isBitOn(ibit))
 				setBit(newArray, ibit);
		
  		//next, go bitwise from there
  		for (int ibit=starting+1; ibit<numBits; ibit++) {
 			if (isBitOn(ibit))
 				setBit(newArray, ibit+num);
 		}

  		/*
 		//first, put integers up to start of insertion
  	  	for (int i=0; i<=startingInt; i++) 
  	 			newArray[i]=array[i];
  	    	for (int i=startingInt+1; i< newNumInts; i++) 
  	 			newArray[i]=0;
  			
  	  		//next, go bitwise from there
  	  		for (int ibit=starting; ibit<numBits; ibit++) {
  	 			if (isBitOn(ibit))
  	 				setBit(newArray, ibit+num);
  	 		}
*/
  		numBits = newNumBits;
  		numInts = newNumInts;
		array=newArray;
 	}
 	
 	/*------------------------------------------*/
 	/**Removes num bits starting with and incuding bit "starting"*/
 	public void deleteParts (int starting, int num) {
		if (num<=0)
			return;
		if (starting<0)
			return;
		else if (starting>numBits)
			return;
		if (num+starting>numBits)
			num = numBits-starting;

		int newNumBits = numBits - num;
 		int startingInt = inWhichInteger(starting);
  		int newNumInts = newNumBits/SIZECHUNK + 1;
		
 		if (newNumInts/2*2!=newNumInts) //odd; add 1 to make even
 			newNumInts++;
 		
 		//first, put integers up to start of deletion
  		int[] newArray = new int[newNumInts];

    		for (int i=0; i<startingInt; i++) 
 			newArray[i]=array[i];
    		for (int i=startingInt; i< newNumInts; i++) 
 			newArray[i]=0;
		
  		//next, go bitwise from there
  		for (int ibit=startingInt*SIZECHUNK; ibit<starting; ibit++) {
 			if (isBitOn(ibit))
 				setBit(newArray, ibit);
 		}
  		for (int ibit=starting + num; ibit<numBits; ibit++) {
 			if (isBitOn(ibit))
 				setBit(newArray, ibit-num);
 		}

  		numBits = newNumBits;
 		numInts = newNumInts;
		array=newArray;
 	}
	/*...........................................................*/
	public void swapParts(int first, int second) {
		if (first<0 || first>=numBits || second<0 || second>=numBits) 
			return;
		boolean firstOn = isBitOn(first);
		setBit(first, isBitOn(second));
		setBit(second, firstOn);
	}
	/*...........................................................*/
	public void moveParts(int starting, int num, int justAfter) {
		if (num<=0 || starting>=numBits || (justAfter>=starting && justAfter<=starting+num-1)) //starting???
			return;
		if (justAfter>=numBits)
			justAfter = numBits-1;
		if (justAfter<0)
			justAfter = -1;
		Bits newBits = new Bits(numBits);
		if (starting>justAfter){
			int count =0;
			for (int i=0; i<=justAfter; i++)
				newBits.setBit(count++, isBitOn(i));
			
			for (int i=starting; i<=starting+num-1; i++)
				newBits.setBit(count++, isBitOn(i));
			for (int i=justAfter+1; i<=starting-1; i++)
				newBits.setBit(count++, isBitOn(i));
			for (int i=starting+num; i<numBits; i++)
				newBits.setBit(count++, isBitOn(i));
		}
		else {
			int count =0;
			for (int i=0; i<=starting-1; i++)
				newBits.setBit(count++, isBitOn(i));
			
			for (int i=starting+num; i<=justAfter; i++)
				newBits.setBit(count++, isBitOn(i));
			for (int i=starting; i<=starting+num-1; i++)
				newBits.setBit(count++, isBitOn(i));
			for (int i=justAfter+1; i<numBits; i++)
				newBits.setBit(count++, isBitOn(i));
		}
		for (int i=0; i<numBits; i++)
			setBit(i, newBits.isBitOn(i));
	}
 	/*------------------------------------------*/
 	private String showBits(int s) {
 		String sr = "";
 		 for (int i=0; i<SIZECHUNK; i++)
 		 	if ((s & (1<< (SIZECHUNKMINUS1- (i % SIZECHUNK)))) == 0)
 		 		sr += "0";
 		 	else
 		 		sr += "1";
 		 return sr;
 	}
 	
 	/*------------------------------------------*/
 	public int numBitsOn (int bitInteger) {
 		int count = 0;
 		for (int i=0; i<SIZECHUNK; i++)
 			if (isBitOn(bitInteger,i))
 				count++;
 		return count;
 	}
	/*------------------------------------------*/
 	public int numBitsOn () {
 		int count = 0;
 		for (int i=0; i<numInts; i++)
 			if (array[i]!=0) {
 				count += numBitsOn(array[i]);
 			}
 		return count;
 	}

 	/*Returns 0 if no bits on, 1 if exactly one, 2 if more than 1*/
 	public int numBitsOnPlural () {
 		int count = 0;
 		for (int i=0; i<numInts; i++)
 			if (array[i]!=0) {
 				count += numBitsOn(array[i]);
 	 			if (count>1)
 	 				return 2;
 			}
 		return count;
 	}
 	/*------------------------------------------*/
 	public boolean single () {
 		for (int i=0; i<numInts; i++)
 			if (array[i]!=0)
 				return true;
 		return false;
 	}
 	
 	/*------------------------------------------*/
 	public int setNewRandomBit (Random rng) {
 		int available = numBits - numBitsOn();
 		if (available<1)
 			return -1;
 		if (available==1){
 			int bit = firstBitOff();
 			setBit(bit,true);
 			return bit;
 		}
 		int bitToSet = (int)(rng.nextDouble()*available);
 		int count=0;
 		for (int i=0; i<numBits; i++) {
 			if (!isBitOn(i)) {
 				if (bitToSet==count){
 					setBit(i,true);
 					return i;
 				}
 				count++;
 			}
 		}
 		return -1;
	}
 	/*------------------------------------------*/
 	public int unsetNewRandomBit (Random rng) {
 		int available = numBitsOn();
 		if (available<1)
 			return -1;
 		if (available==1){
 			int bit = firstBitOn();
 			setBit(bit,false);
 			return bit;
 		}
 		int bitToSet = (int)(rng.nextDouble()*available);
 		int count=0;
 		for (int i=0; i<numBits; i++) {
 			if (isBitOn(i)) {
 				if (bitToSet==count){
 					setBit(i,false);
 					return i;
 				}
 				count++;
 			}
 		}
 		return -1;
	}

 	
 	public void invertAllBits () {
 		for (int i=0; i<numInts; i++)
 			array[i]=~array[i];
 		cleanEnd();
 	}
	public void clearAllBits () {
 		for (int i=0; i<numInts; i++)
 			array[i]=0;
 	}
 	public void setAllBits () {
 		for (int i=0; i<numInts; i++)
 			array[i]=~0;
 		cleanEnd();
 	}
 	private void cleanEnd(){
 		for (int i=numBits; i<numInts*SIZECHUNK; i++)
 			clearBit(i);
 	}
 	public void clearBit (int whichBit) {
  		int whichInt = whichBit/SIZECHUNK;
  		if (whichInt<0 || whichInt>=array.length)
  			return;
  		int theInt = array[whichInt];
		theInt &= ~(1<< (SIZECHUNKMINUS1- (whichBit % SIZECHUNK)));
		array[whichInt] = theInt;
 	}
 	public int clearBit (int chunk, int whichBit) {
  		int theInt = chunk;
		theInt &= ~(1<< (SIZECHUNKMINUS1- (whichBit % SIZECHUNK)));
		return theInt;
 	}
	public void setBit (int[]a, int whichBit) {
  		if (a==null)
  			return;
  		int whichInt = whichBit/SIZECHUNK;
  		if (whichInt<0 || whichInt>=a.length)
  			return;
  		int theInt = a[whichInt];
		theInt |= (1<< (SIZECHUNKMINUS1- (whichBit % SIZECHUNK)));
		a[whichInt] = theInt;
 	}
 	public void setBit (int whichBit) {
  		int whichInt = whichBit/SIZECHUNK;
  		if (whichInt<0 || whichInt>=array.length)
  			return;
  		int theInt = array[whichInt];
		theInt |= (1<< (SIZECHUNKMINUS1- (whichBit % SIZECHUNK)));
		array[whichInt] = theInt;
 	}
 	public void setBit (int whichBit, boolean on) {
  		int whichInt = whichBit/SIZECHUNK;
  		if (whichInt<0 || whichInt>=array.length)
  			return;
  		int theInt = array[whichInt];
		if (on)
			theInt |= (1<< (SIZECHUNKMINUS1- (whichBit % SIZECHUNK)));
		else
			theInt &= ~(1<< (SIZECHUNKMINUS1- (whichBit % SIZECHUNK)));
		array[whichInt] = theInt;
 	}
 	public void swapValues (int i, int j) {
  		if ((inWhichInteger(i)<0 || inWhichInteger(i)>=array.length) ||(inWhichInteger(j)<0 || inWhichInteger(j)>=array.length))
  			return;
  		boolean temp = isBitOn(i);
  		setBit(i, isBitOn(j));
  		setBit(j, temp);
 	}
 	private int inWhichInteger (int whichBit) {
  		return whichBit/SIZECHUNK;
 	}
 	public void orBits (Bits other) {
 		for (int i=0; i< numInts && i< other.numInts; i++) 
 				array[i] = array[i] | other.array[i];
 	}
 	public void andBits (Bits other) {
 		for (int i=0; i< numInts && i< other.numInts; i++) 
 				array[i] = array[i] & other.array[i];
 	}
 	/** Examines the two bits to see if all four combinations of paired values occur across the Bits.
 	 * Returns true if there is:
 	 * 	- at least one position with bits1=on and bits2=on
 	 * 	- at least one position with bits1=on and bits2=off
 	 * 	- at least one position with bits1=off and bits2=on
 	 * 	- at least one position with bits1=off and bits2=off
 	 */
 	public static boolean compatible (Bits bits1, Bits bits2) {
 		boolean onon = false;
 		boolean onoff = false;
 		boolean offon = false;
 		boolean offoff=false;
 		for (int i=0; i< bits1.numBits && i< bits2.numBits; i++) {
 			if (bits1.isBitOn(i) && bits2.isBitOn(i))
 				onon=true;
 			if (bits1.isBitOn(i) && !bits2.isBitOn(i))
 				onoff=true;
 			if (!bits1.isBitOn(i) && bits2.isBitOn(i))
 				offon=true;
 			if (!bits1.isBitOn(i) && !bits2.isBitOn(i))
 				offoff=true;
 			if (onon && onoff && offon && offoff)
 				return false;
 		}
 		return true;
 	}
	public boolean equals (Bits other) {
 		int max = MesquiteInteger.maximum(numInts, other.numInts);
 		for (int i=0; i<max; i++) {
 			if (i>= numInts) {
 				if (other.array[i] != 0)
 					return false;
 			}
 			else if (i>= other.numInts) {
 				if (array[i] != 0)
 					return false;
 			}
 			else {
 				if (array[i] != other.array[i])
 					return false;
 			}
 		}
 		return true;
 	}

 	public boolean equalsComplement (Bits other) {
 		int max = MesquiteInteger.maximum(numInts, other.numInts);
 		for (int i=0; i<max; i++) {
 			if (i>= numInts) {
 				if (other.array[i] != ~0)
 					return false;
 			}
 			else if (i>= other.numInts) {
 				if (array[i] != ~0)
 					return false;
 			}
 			else {
 				int chunk = ~array[i];
				if (i==numInts-1){//at last chunk; need to clear last bits
					//clear last numInts*SIZECHUNK - numBits
					for (int k = 32-(numInts*SIZECHUNK - numBits); k<32; k++)
			  			chunk = clearBit(chunk, k);
				}
 				if (chunk != other.array[i])
 					return false;
 			}
 		}
 		return true;
 	}
	public boolean isBitOn (int whichBit) {
  		int whichInt = whichBit/SIZECHUNK;
  		if (whichInt<0 || whichInt>=array.length)
  			return false;
  		int theInt = array[whichInt];
  		theInt &= (1<< (SIZECHUNKMINUS1- (whichBit % SIZECHUNK)));
		return (theInt != 0);
 	}
 	public static boolean isBitOn (int[] bits, int whichBit) {
  		int whichInt = whichBit/SIZECHUNK;
  		if (whichInt<0 || whichInt>=bits.length)
  			return false;
  		int theInt = bits[whichInt];
  		theInt &= (1<< (SIZECHUNKMINUS1- (whichBit % SIZECHUNK)));
		return (theInt != 0);
 	}
 	public static boolean isBitOn (int bitInteger, int whichBit) {
  		int theInt = bitInteger;
  		theInt &= (1<< (SIZECHUNKMINUS1- (whichBit % SIZECHUNK)));
		return (theInt != 0);
 	}
	/*------------------------------------------*/
 	/** returns the next bit, starting a startBit, that has the same value as "on" */
 	public int nextBit (int startBit, boolean on) {
//first, see if there are any bits remaining in the current Int that are the same value as "on";
  		int whichInt = startBit/SIZECHUNK;
 		for (int i=startBit; i<(whichInt+1)*SIZECHUNK && i < numBits; i++)  // check to see if it is in the first int
 			if (isBitOn(i)==on)   // then we've found a bit that has the same value as "on"
 				return i;
//now let's check later Ints
 		whichInt ++;
 		for (int i=whichInt; i<numInts; i++) {
// if array[i] == 0, then they are all off; if it is ==-1, then they are all on (that is, there are non off)
 			if (((array[i]!=0)&&on) || ((array[i]!=-1)&&!on)) {  // then we've found a block that has the same value as on
//now look to see which bit in the block has the correct value
		 		for (int j=i*SIZECHUNK; j<(i+1)*SIZECHUNK && j< numBits; j++)
		 			if (isBitOn(j)==on)   // then we've found the bit that has the same value as "on"
		 				return j;
 			}
 		}
 		return -1;  //didn't find any, return signal that none found
 	}
 	/** given a bit whichBit, this method returns the first bit ≤ whichBit that has the same value as whichBit*/
	public int startOfBlock (int whichBit) {
		boolean isOn = isBitOn(whichBit);
 		for (int i=whichBit; i>-1; i--)
 			if (isBitOn(i)!=isOn) {
 				return i+1;
 			}
 		return whichBit;
 	}

 	/*------------------------------------------*/
 	public boolean allBitsOn () {
 		for (int i=0; i<numBits; i++)
 			if (!isBitOn(i))
 				return false;
 		return true;
 	}
 	/*------------------------------------------*/
 	public boolean anyBitsOn () {
 		for (int i=0; i<numInts; i++)
 			if (array[i]!=0)
 				return true;
 		return false;
 	}
 	/*------------------------------------------*
 	public boolean anyBitsOff () {
 		for (int i=0; i<numInts; i++)
 			if (array[i]!=~0)
 				return true;
 		return false;
 	}
 	/*------------------------------------------*/
 	public int firstBitOn (int bitInteger) {
 		for (int i=0; i<SIZECHUNK; i++)
 			if (isBitOn(bitInteger,i))
 				return i;
 		return -1;
 	}
 	/*------------------------------------------*/

 	public int firstBitOn () {
 		for (int i=0; i<numInts; i++)
 			if (array[i]!=0) {
 				return 32*i+firstBitOn(array[i]);
 			}
 		return -1;
 	}
 	/*------------------------------------------*
 	public int firstBitOff (int bitInteger) {
 		for (int i=0; i<SIZECHUNK; i++)
 			if (!isBitOn(bitInteger,i))
 				return i;
 		return -1;
 	}

 	/*------------------------------------------*/
	public int firstBitOff () {
 		for (int i=0; i<numBits; i++)
 			if (!isBitOn(i)) {
 				return i;
 			}
 		return -1;
 	}


 	/*------------------------------------------*/
 	public int lastBitOn (int bitInteger) {
 		for (int i=SIZECHUNK-1; i>=0; i--)
 			if (isBitOn(bitInteger,i))
 				return i;
 		return -1;
 	}

 	public int lastBitOn () {
 		for (int i=numInts-1; i>=0; i--)
 			if (array[i]!=0) {
 				return 32*i+lastBitOn(array[i]);
 			}
 		return -1;
 	}

 	/** Returns true iff exactly one bit is on.   If one bit is on, it returns in "single" the single bit that is on.  */ 
 	public boolean oneBitOn (MesquiteInteger single) {
 		boolean foundOne = false;
 		for (int i=0; i<numBits; i++)
 			if (isBitOn(i))
 				if (!foundOne) {
 					foundOne = true;
 					if (single!=null)
 						single.setValue(i);
 				}
 				else  // we've already found one, so there is more than one bit on
 					return false;
 		return foundOne;
 	}
 	


 	public String toString () {
 		String s = "";
 		for (int i=0; i<numBits; i++)
 			if (isBitOn(i))
 				s+= '1';
 			else
 				s+= '0';
 		return s;
 	}
 	public String toAsteriskString () {
 		String s = "";
 		for (int i=0; i<numBits; i++)
 			if (isBitOn(i))
 				s+= '*';
 			else
 				s+= '.';
 		return s;
 	}

 	public String toPlusMinusString () {
 		String s = "";
 		for (int i=0; i<numBits; i++)
 			if (isBitOn(i))
 				s+= '+';
 			else
 				s+= '-';
 		return s;
 	}

	/** returns a string listing the bits on.  In the format
	of NEXUS character, taxa lists (e.g., "1- 3 6 201-455".  The offset is what the first element is to be numbered
	(e.g., 0 or 1)  */
	public String getListOfBitsOn(int offset) {
		int continuing = 0;
		String s="";
		boolean found=false;
		int lastWritten = -1;
		for (int i=0; i<numBits; i++) {
			if (isBitOn(i)) {
				found=true;
				if (continuing == 0) {
					s += " " + (i + offset);
					lastWritten = i;
					continuing = 1;
				}
				else if (continuing == 1) {
					s += " - ";
					continuing = 2;
				}
			}
			else if (continuing >0) {
				if (lastWritten != i-1) {
					s += " " + (i-1 + offset);
					lastWritten = i-1;
				}
				else
					lastWritten = -1;
				continuing = 0;
			}
		}
		if (continuing>1)
			s += " " + (numBits-1 + offset);
		if (found)
			return s;
		else
			return "None";
	}
	/*...........................................................*/
	public static void moveColumns(boolean[][] d, int starting, int num, int justAfter) {
		if (num<=0 || d==null || starting>=d.length || (justAfter>=starting && justAfter<=starting+num-1)) //starting???
			return;
		if (justAfter>=d.length)
			justAfter = d.length-1;
		if (justAfter<0)
			justAfter = -1;
		boolean[][] newValues = new boolean[d.length][];
		if (starting>justAfter){
			int count =0;
			for (int i=0; i<=justAfter; i++)
				newValues[count++]=d[i];
			for (int i=starting; i<=starting+num-1; i++)
				newValues[count++]=d[i];
			for (int i=justAfter+1; i<=starting-1; i++)
				newValues[count++]=d[i];
			for (int i=starting+num; i<d.length; i++)
				newValues[count++]=d[i];
		}
		else {   // (starting<=justAfter)
			int count =0;
			for (int i=0; i<=starting-1; i++)
				newValues[count++]=d[i];
			for (int i=starting+num; i<=justAfter; i++)
				newValues[count++]=d[i];
			for (int i=starting; i<=starting+num-1; i++)
				newValues[count++]=d[i];
			for (int i=justAfter+1; i<d.length; i++)
				newValues[count++]=d[i];
		}
		for (int i=0; i<d.length; i++)
			d[i]=newValues[i];
	}
	/*...........................................................*/
	public static void swapColumns(boolean[][] d, int first, int second) {
		if (first<0 || d==null || first>=d.length || second<0 || second>=d.length) 
			return;
		boolean[] temp = d[first];
		d[first]=d[second];
		d[second] = temp;
	}
	/*...........................................................*/
	public static void swapCell(boolean[][] d, int first, int second, int k) {
		if (k<0)
			return;
		if (first<0 || d==null || first>=d.length || second<0 || second>=d.length) 
			return;
		if (d[first] == null || k>= d[first].length || d[second] == null || k>= d[second].length)
			return;
		boolean temp = d[first][k];
		d[first][k]=d[second][k];
		d[second][k] = temp;
	}
	/*...........................................................*/
	public static void moveParts(short[] d, int starting, int num, int justAfter) {   //DRM new 7 March 08
		if (num<=0 || d==null || starting>=d.length || (justAfter>=starting && justAfter<=starting+num-1)) //starting???
			return;
		if (justAfter>=d.length)
			justAfter = d.length-1;
		if (justAfter<0)
			justAfter = -1;
		short[] newValues = new short[d.length];
		if (starting>justAfter){
			int count =justAfter+1;
			
			for (int i=starting; i<=starting+num-1; i++)
				newValues[count++]=d[i];
			for (int i=justAfter+1; i<=starting-1; i++)
				newValues[count++]=d[i];

			for (int i=justAfter+1; i<=starting+num-1; i++)
				d[i]=newValues[i];

		}
		else {  // moving down			
			int count =starting;
			
			for (int i=starting+num; i<=justAfter; i++)
				newValues[count++]=d[i];
			for (int i=starting; i<=starting+num-1; i++)
				newValues[count++]=d[i];
			
			for (int i=starting; i<=justAfter; i++)
				d[i]=newValues[i];
		}
	}
	/*...........................................................*/
	public static void moveRows(boolean[][] d, int starting, int num, int justAfter) {  //DRM: new
		if (num<=0 || d==null || d.length == 0)
			return;
		int numRows = d[0].length;
		if (starting>=numRows || (justAfter>=starting && justAfter<=starting+num-1)) //starting???
			return;
		if (justAfter>=numRows)
			justAfter = numRows-1;
		if (justAfter<0)
			justAfter = -1;
		boolean[] newValues = new boolean[numRows];
		for (int column = 0; column<d.length; column++){
			if (starting>justAfter){
				int count =justAfter+1;
				
				for (int i=starting; i<=starting+num-1; i++)
					newValues[count++]=d[column][i];
				for (int i=justAfter+1; i<=starting-1; i++)
					newValues[count++]=d[column][i];
				
				for (int i=justAfter+1; i<=starting+num-1; i++)
					d[column][i]=newValues[i];
			}
			else {
				int count =starting;
				
				for (int i=starting+num; i<=justAfter; i++)
					newValues[count++]=d[column][i];
				for (int i=starting; i<=starting+num-1; i++)
					newValues[count++]=d[column][i];
				
				for (int i=starting; i<=justAfter; i++)
					d[column][i]=newValues[i];
		}
		}
	}
	/*...........................................................*/
	public static void moveRowsOriginal(boolean[][] d, int starting, int num, int justAfter) {
		if (num<=0 || d==null || d.length == 0)
			return;
		int numRows = d[0].length;
		if (starting>=numRows || (justAfter>=starting && justAfter<=starting+num-1)) //starting???
			return;
		if (justAfter>=numRows)
			justAfter = numRows-1;
		if (justAfter<0)
			justAfter = -1;
		boolean[] newValues = new boolean[numRows];
		for (int column = 0; column<d.length; column++){
			if (starting>justAfter){
				int count =0;
				for (int i=0; i<=justAfter; i++)
					newValues[count++]=d[column][i];
				
				for (int i=starting; i<=starting+num-1; i++)
					newValues[count++]=d[column][i];
				for (int i=justAfter+1; i<=starting-1; i++)
					newValues[count++]=d[column][i];
				for (int i=starting+num; i<numRows; i++)
					newValues[count++]=d[column][i];
			}
			else {
				int count =0;
				for (int i=0; i<=starting-1; i++)
					newValues[count++]=d[column][i];
				
				for (int i=starting+num; i<=justAfter; i++)
					newValues[count++]=d[column][i];
				for (int i=starting; i<=starting+num-1; i++)
					newValues[count++]=d[column][i];
				for (int i=justAfter+1; i<numRows; i++)
					newValues[count++]=d[column][i];
			}
			for (int i=0; i<numRows; i++)
				d[column][i]=newValues[i];
		}
	}
	public static boolean[][] addRows(boolean[][] d, int starting, int num) {
		if (num==0 || d == null || d.length == 0)
			return d;
		for (int column = 0; column<d.length; column++){
			d[column]=addParts(d[column], starting, num);
		}
		return d;
	}
	public static boolean[][] deleteRows(boolean[][] d, int starting, int num) {
		if (num==0 || d == null || d.length == 0)
			return d;
		for (int column = 0; column<d.length; column++){
			d[column]=deleteParts(d[column], starting, num);
		}
		return d;
	}
 	/*------------------------------------------*/
 	/**Inserts num (cleared) bits just after bit "starting"*/
 	public static boolean[] addParts (boolean[] d, int starting, int num) {
		if (num==0 || d == null)
			return d;
		if (starting<0)
			starting = -1;
		else if (starting>=d.length)
			starting = d.length-1;
		int newNum = d.length + num;
		boolean[] newValues = new boolean[newNum];
		for (int i=0; i<=starting; i++)
			newValues[i]=d[i];
		for (int i=0; i<num ; i++)
			newValues[starting + i + 1]=false;
		for (int i=0; i<d.length-starting-1; i++) 
			newValues[i +starting+num+1]=d[starting + i + 1];
		return newValues;
 	}
	/*...........................................................*/
	public static boolean[] deleteParts(boolean[] values, int starting, int num){
		if (num<=0 || values == null)
			return values;
		if (num+starting>values.length)
			num = values.length-starting;
		int newNumParts = values.length-num;
		boolean[] newValues = new boolean[newNumParts];
		
		for (int i=0; i<starting; i++) {
			newValues[i] = values[i];
		}
		for (int i=starting+num; i<values.length; i++) {
			newValues[i-num ] = values[i];
		}
		return newValues;
	}

	/*------------------------------------------*/
 	public static String toString (int[] bits) {
 		if (bits==null)
 			return null;
 		String s = "";
 		for (int i=0; i<bits.length*SIZECHUNK; i++) {
 			if (isBitOn(bits, i))
 				s+= '1';
 			else
 				s+= '0';
 		}
 		return s;
 	}
 	/*------------------------------------------*/
 	public static String toString (boolean[] bits) {
 		if (bits==null)
 			return null;
 		String s = "";
 		for (int i=0; i<bits.length; i++) {
 			if (bits[i])
 				s+= '1';
 			else
 				s+= '0';
 		}
 		return s;
 	}

	/*..........................................Bits.....................................*/
	/** converts passed int  to string, as its bits representation.  Used for development/debugging. �*/
	public static String toString(byte s) {
		String temp="";
		for (int e=7; e>=0; e--) {
			if (((((byte)1)<<e)&s)!=((byte)0)) 
				temp+="1";
			else
				temp+="0";
		}
		return temp;
	}
	/*..........................................Bits.....................................*/
	/** converts passed int  to string, as its bits representation.  Used for development/debugging. �*/
	public static String toString(short s) {
		String temp="";
		for (int e=15; e>=0; e--) {
			if (((((short)1)<<e)&s)!=((short)0)) 
				temp+="1";
			else
				temp+="0";
		}
		return temp;
	}
	/*..........................................Bits.....................................*/
	/** converts passed int  to string, as its bits representation.  Used for development/debugging. �*/
	public static String toString(int s) {
		String temp="";
		for (int e=SIZECHUNKMINUS1; e>=0; e--) {
			if (((((int)1)<<e)&s)!=((int)0)) 
				temp+="1";
			else
				temp+="0";
		}
		return temp;
	}
	/*..........................................Bits.....................................*/
	/** converts passed long  to string, as its bits representation.  Used for development/debugging. �*/
	public static String toString(long s) {
		String temp="";
		for (int e=63; e>=0; e--) {
			if (((1L<<e)&s)!=0L) 
				temp+="1";
			else
				temp+="0";
		}
		return temp;
	}
 	/*------------------------------------------*/
}


