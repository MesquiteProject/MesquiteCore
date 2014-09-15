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

import mesquite.lib.characters.CharactersGroup;

/* ======================================================================== */
/** A specset with bit information about taxa.   Called "taxa String set" because it stores a string for each of
for a whole list of taxa.  It is abstract because it doesn't define cloneSpecsSet  */
public class TaxaStringsSet extends ObjectSpecsSet implements TaxaSpecsSet { //should extend a generic BitsSpecsSet
	Taxa taxa;
	String typeName = "Taxa String set";
	public TaxaStringsSet(String name, int numTaxa, Taxa taxa){
		super(name, numTaxa, "");
		this.taxa = taxa;
	}
	public String getTypeName(){
		return typeName;
	}
	public void setTypeName(String typeName){
		this.typeName = typeName;
	}
	public SpecsSet cloneSpecsSet(){
		TaxaStringsSet s = new TaxaStringsSet(name, numParts, taxa);
		for (int i = 0; i< numParts; i++)
			s.setProperty(getProperty(i), i);
		return s;
	}
	public SpecsSet makeSpecsSet(AssociableWithSpecs parent, int i){
		if (!(parent instanceof Taxa))
			return null;
		TaxaStringsSet tis = new TaxaStringsSet("Alternative Naming", numParts, (Taxa)parent);
		return tis;
	}
	/*.................................................................................................................*/
 	/** gets storage for set of properties*/
	public Object[] getNewPropertyStorage(int numParts){
		return new String[numParts];
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
}


