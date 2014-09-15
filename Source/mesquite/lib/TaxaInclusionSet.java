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
/** A taxon inclusion set.   */
public class TaxaInclusionSet extends TaxaBitsSet { 
	public TaxaInclusionSet(String name, int numTaxa, Taxa taxa){
		super(name, numTaxa, taxa);
	}
	public String getTypeName(){
		return "Taxon Inclusion set";
	}
	public SpecsSet cloneSpecsSet(){
		TaxaInclusionSet bss = new TaxaInclusionSet("cloned", getNumberOfParts(), taxa);
		for (int i=0; i< getNumberOfParts(); i++) {
			bss.setSelected(i, isBitOn(i));
		}
		return bss;
	}
	public SpecsSet makeSpecsSet(AssociableWithSpecs parent, int numParts){
		if (!(parent instanceof Taxa))
			return null;
		TaxaInclusionSet tis = new TaxaInclusionSet("Inclusion Set", numParts, (Taxa)parent);
		tis.selectAll();
		return tis;
	}
	/*.................................................................................................................*/
	/** Add num parts just after "starting" (filling with default values)  */
  	public boolean addParts(int starting, int num){  
		boolean success = super.addParts(starting, num); 
		if (success) {
			for (int i=starting+1; i< starting+num+1; i++) //default is for the bit to be set
				setSelected(i, true);
		}
		return success;
	}


}


