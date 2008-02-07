/* Mesquite source code.  Copyright 1997-2007 W. Maddison and D. Maddison.
Version 2.01, December 2007.
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
import mesquite.lib.Bits;

public class Bipartition {
	 Bits bits;
	 long freq;
	 
	public Bipartition(int numTaxa){
		bits = new Bits(numTaxa);
		freq = 0;
	}
	void reset(){
		freq = 0;
	}
	void increment(){
		freq++;
	}
	public long getFreq(){
		return freq;
	}
	public void setFreq(long freq){
		this.freq=freq;
	}
	public Bits getBits(){
		return bits;
	}
	public void setBits(Bits bits){
		this.bits=bits;
	}

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
}