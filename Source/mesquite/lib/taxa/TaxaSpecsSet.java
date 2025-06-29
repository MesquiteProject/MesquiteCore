/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.taxa;

import mesquite.lib.Listable;

/* ======================================================================== */
/** */
public interface TaxaSpecsSet extends Listable {	
 	/*.................................................................................................................*/
	/** Add num taxa just after "starting" (filling with default values)  */
  	public boolean addParts(int starting, int num);
	/*.................................................................................................................*/
	/** Delete taxa specified  */
	public boolean deleteParts(int starting, int num);
	/*.................................................................................................................*/
	/** Move num taxa starting at first, to just after taxon justAfter  */
	public boolean moveParts(int first, int num, int justAfter);
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public boolean swapParts(int first, int second, boolean notify);
	
	public Taxa getTaxa();
	public void setTaxa(Taxa taxa);
}


