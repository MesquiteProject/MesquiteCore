/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.pairwise.lib;

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;


/* ======================================================================== */
/***/
public class TaxaPairing   {
	private TaxaPath[] paths;
	private int numPairs = 0;
	public TaxaPairing (int numTaxa) {
		paths = new TaxaPath[numTaxa];
	}
	public int getNumPairs() {
		return numPairs;
	}
	public void addPath (TaxaPath path) {
		if (numPairs>=0 && numPairs <paths.length)
			paths[numPairs]= path;
		numPairs++;
	}

	public TaxaPath getPath (int index) {
		if (index>=0 && index < numPairs) 
			return paths[index];
		MesquiteMessage.warnProgrammer("path out of range " + index + " " + numPairs);
		return null;
	}
	public void addPath (int i, int j, int base) {
		TaxaPath path = new TaxaPath();
		path.setBase(base);
		path.setTaxon(i, 1);
		path.setTaxon(j, 2);
		paths[numPairs]= path;
		numPairs++;
	}
 	/*  */
}


