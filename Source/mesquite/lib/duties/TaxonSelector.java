/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.duties;

import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;


/* ======================================================================== */
/**This is superclass of modules to select taxa in a table.*/

public abstract class TaxonSelector extends MesquiteModule  {

   	 public Class getDutyClass() {
   	 	return TaxonSelector.class;
   	 }
 	public String getDutyName() {
 		return "Taxon selector";
   	}
   	
   	/** if returns true, then requests to remain on even after operateData is called.  Default is false*/
   	public boolean pleaseLeaveMeOn(){
   		return false;
   	}
   	/** Called to select the taxa.  It should only add to a selection, not deselect any currently selected.*/
   	public abstract void selectTaxa(Taxa taxa, mesquite.lib.characters.CharacterData data);
   	/** Called to select the taxa.  It should only add to a selection, not deselect any currently selected.*/
   	public abstract void selectTaxa(Taxa taxa);
	/*.................................................................................................................*/
	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
	public CompatibilityTest getCompatibilityTest(){
		return new CharacterStateTest();
	}
}



