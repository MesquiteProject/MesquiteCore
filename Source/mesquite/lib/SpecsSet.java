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
/** DOCUMENT.*/
public abstract class SpecsSet extends FileElement {	
	//protected int numParts;
	public static long totalCreated = 0;
	public static long totalDisposed = 0;
	private String nexusBlockStored = null;
	AssociableWithSpecs parent = null;
	
	public SpecsSet (String name, int numParts) {
		this.name = name;
		this.numParts = numParts;
		totalCreated++;
	}
	public SpecsSet (int numParts) {
		this.numParts = numParts;
		totalCreated++;
	}
	public void dispose(){
		totalDisposed++;
		super.dispose();
	}
	public abstract String getTypeName();
	public abstract SpecsSet cloneSpecsSet();
	public abstract SpecsSet makeSpecsSet(AssociableWithSpecs parent, int numParts);
	public void setNexusBlockStored(String s){
		if (s==null)
			nexusBlockStored = null;
		else
			nexusBlockStored = new String(s);
	}
	public String toHTMLStringDescription(){
		return "<li>" + getName() + "<ul>" + super.toHTMLStringDescription() + "</ul></li>";
	}
	public String getNexusBlockStored(){
		return nexusBlockStored;
	}
	public void setParent(AssociableWithSpecs aws){
 		this.parent = aws;
	}
	public AssociableWithSpecs getParent(){
 		return parent;
	}
	public void setChangedFromCloned(boolean ch){
 		setDirty(true);
	}
	/*.................................................................................................................*/
	public int getNumberOfParts(){
		return numParts;
	}
 	/*.................................................................................................................*/
 	/** Sets the value for part "part" to be the same as that at part "otherPart" in the incoming specsSet*/
	public abstract void equalizeSpecs(SpecsSet other, int otherPart, int part);
 	/*.................................................................................................................*/
	/** Add num parts just after "starting" (filling with default values)  */
  	public abstract boolean addParts(int starting, int num);
	/*.................................................................................................................*/
	/** Delete parts specified  */
	public abstract boolean deleteParts(int starting, int num);
	/*.................................................................................................................*/
	/** Move num parts starting at first, to just after parts justAfter  */
	public abstract boolean moveParts(int starting, int num, int justAfter);
	/*----*/
	
	public abstract String toString(int part);
}

