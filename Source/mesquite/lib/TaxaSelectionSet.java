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
/** A taxon set. */
public class TaxaSelectionSet extends TaxaBitsSet { 
	public TaxaSelectionSet(String name, int numTaxa, Taxa taxa){
		super(name, numTaxa, taxa);
	}
	public String getTypeName(){
		return "Taxon Selection set";
	}
	public SpecsSet cloneSpecsSet(){
		TaxaSelectionSet bss = new TaxaSelectionSet("cloned", getNumberOfParts(), taxa);
		for (int i=0; i< getNumberOfParts(); i++) {
			bss.setSelected(i, isBitOn(i));
		}
		return bss;
	}
	public SpecsSet makeSpecsSet(AssociableWithSpecs parent, int numParts){
		if (!(parent instanceof Taxa))
			return null;
		return new TaxaSelectionSet("Selection Set", numParts, (Taxa)parent);
	}
}

