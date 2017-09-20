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
import java.math.*;

/* ======================================================================== */
/** A specset with bit information about taxa.   Called "taxa bits set" because it amounts to a selection (bit on = yes, bit off  = no)
for a whole list of taxa.  It is abstract because it doesn't define cloneSpecsSet  */
public abstract class TaxaBitsSet extends BitsSpecsSet implements TaxaSpecsSet { //should extend a generic BitsSpecsSet
	Taxa taxa;
	public TaxaBitsSet(String name, int numTaxa, Taxa taxa){
		super(name, numTaxa);
		this.taxa = taxa;
	}
	public String getTypeName(){
		return "Taxa bits set";
	}
 	/*.................................................................................................................*/
	public Taxa getTaxa(){
		return taxa;
	}
 	/*.................................................................................................................*/
	/** Add num taxa just after "starting" (filling with default values)  */
  	public boolean addParts(int starting, int num){  
		setDirty(true);
		return super.addParts(starting, num);
	}
	/*.................................................................................................................*/
	/** Delete num Taxa beginning and including character "starting"*/
	public boolean deleteParts(int starting, int num){  
		setDirty(true);
		return super.deleteParts(starting, num);
	}
	/*.................................................................................................................*/
	public boolean moveParts(int first, int num, int justAfter){  
		setDirty(true);
		return super.moveParts(first, num, justAfter);
	}
	/*.................................................................................................................*/
	public boolean swapParts(int first, int second){  
		setDirty(true);
		return super.swapParts(first, second);
	}
	/*.................................................................................................................*/
	public String getStringList(String delimiter, TaxonNamer namer, boolean justFirst){  
		StringBuffer sb= new StringBuffer();
		boolean first = true;
		for (int i=0; i<getNumberOfParts(); i++)
			if (isPresent(i)) {
				String taxonName = "";
				if (namer==null) 
					taxonName=taxa.getName(i);
				else
					taxonName=namer.getNameToUse(taxa, i);
				if (first){
					sb.append(taxonName);
					first = false;
				}
				else
					sb.append(delimiter + taxonName);
				if (justFirst)
					break;
			}
		return sb.toString();

	}

}


