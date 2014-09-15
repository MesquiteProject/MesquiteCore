/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.assoc.lib;

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;


/* ======================================================================== */

public abstract class ReconstructAssociation extends MesquiteModule   {

   	 public Class getDutyClass() {
   	 	return ReconstructAssociation.class;
   	 }
 	public String getDutyName() {
 		return "Reconstructs a taxon association history";
   	 }
	/** reconstructs history of contained tree within containing.  Contained tree must be free to be modified; hence send in a clone instead of the original!*/
	public abstract AssociationHistory reconstructHistory(Tree containing, MesquiteTree contained, TaxaAssociation association, MesquiteNumber cost, MesquiteString resultString);
	/** calculates cost of fitting contained tree within containing.  Exact meaning depends on module (e.g., for RecCoalescenceHistory, cost is deep coalescences).  Contained tree must be free to be modified; hence send in a clone instead of the original!*/
	public abstract void calculateHistoryCost(Tree containing, MesquiteTree contained, TaxaAssociation association, MesquiteNumber cost, MesquiteString resultString);
}


