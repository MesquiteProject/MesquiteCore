/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison.
Version 2.7, August 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.consensus.lib;

import mesquite.consensus.lib.Bipartition;
import mesquite.lib.*;

public class Bipartition {
	Bits bits;
	double freqDouble;
	static final int numFreqs = 2;
	long[] freq;   // have more than one in case want to split at things like avg std dev split frequencies
	double splitLength;   // for storing branch lengths
	boolean present = false;


	public Bipartition(int numTaxa){
		bits = new Bits(numTaxa);
		freq = new long[numFreqs];
		reset();
	}
	void reset(){
		for (int i=0;i<numFreqs; i++)
			freq[i]=0;
		splitLength = MesquiteDouble.unassigned;
		freqDouble = 0.0;
	}
	void weightedIncrement(double weight){
		if (MesquiteDouble.isCombinable(weight))
			freqDouble+=weight;
	}
	void increment(){
		freq[0]++;
	}
	void increment(int whichFreq){
		freq[whichFreq]++;
	}
	public long getFreq(){
		return freq[0];
	}
	public long getFreq(int whichFreq){
		return freq[whichFreq];
	}
	public void setFreq(long freq){
		this.freq[0]=freq;
	}
	public void setFreq(long freq, int whichFreq){
		this.freq[whichFreq]=freq;
	}
	public double getSplitLength(){
		return splitLength;
	}
	public void addToSplitLength(double length){
		if (MesquiteDouble.isCombinable(length)) {
			if (!MesquiteDouble.isCombinable(splitLength))
				splitLength =0.0;
			splitLength+= length;
		}
	}
	public void avgSplitLength(boolean weighted){
		if (MesquiteDouble.isCombinable(splitLength)) {
			if (!weighted)
				splitLength=splitLength/freq[0];
			else
				splitLength=splitLength/freqDouble;
		}
	}


	public Bits getBits(){
		return bits;
	}
	/*....................................................................*/
	public void copyIntoBits(Bits bits){
		this.bits.setBits(bits);
	}
	/*....................................................................*/
	public void setBits(Bits bits){
		this.bits=bits;
	}
	/*....................................................................*/
	void add(Bipartition b){
		bits.orBits(b.bits);
	}

	boolean equals(Bipartition b, boolean rooted){
		if (rooted)
			return bits.equals(b.bits);
		return bits.equals(b.bits) || bits.equalsComplement(b.bits);
	}
	boolean equals(Bits b,  boolean rooted){
		if (rooted)
			return bits.equals(b);
		return bits.equals(b) || bits.equalsComplement(b);
	}
	public double getFreqDouble() {
		return freqDouble;
	}
	public void setFreqDouble(double freqDouble) {
		this.freqDouble = freqDouble;
	}
	public boolean isPresent() {
		return present;
	}
	public void setPresent(boolean present) {
		this.present = present;
	}
}