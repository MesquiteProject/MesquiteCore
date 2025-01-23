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
import mesquite.lib.taxa.Taxa;


/* ======================================================================== */
/** Manages Taxa Associations.  Also reads and writes ASSOCIATION block.  Example module: "Manage ASSOCIATION blocks" .*/

public abstract class AssociationsManager extends FileElementManager   {

   	 public Class getDutyClass() {
   	 	return AssociationsManager.class;
   	 }
 	public String getDutyName() {
 		return "Manager of Taxa Associations, including read/write ASSOCIATION block";
   	 }
	
	public abstract int getNumberOfAssociations(Taxa containingTaxa, Taxa containedTaxa);
	public abstract int getNumberOfAssociations(Taxa taxa);
	public abstract int getNumberOfAssociations();
	public abstract TaxaAssociation getAssociation(Taxa containingTaxa, Taxa containedTaxa, int i);
	public abstract TaxaAssociation getAssociation(Taxa taxa, int i);
	public abstract int getWhichAssociation(Taxa taxa, TaxaAssociation assoc);
	public abstract TaxaAssociation getAssociation(int i);
	public abstract TaxaAssociation findAssociationByID(long id, Taxa taxa);
  	 public abstract TaxaAssociation makeNewAssociation(Taxa containingTaxa, Taxa containedTaxa);
  	 public abstract TaxaAssociation makeNewAssociation(Taxa containingTaxa, Taxa containedTaxa, String name);
  	 public abstract TaxaAssociation duplicateAssociation(TaxaAssociation association);
	public abstract ListableVector getAssociationsVector();
	public abstract boolean showAssociationInTaxonList(Taxa taxa, TaxaAssociation association, boolean showEditor);
	public Class getElementClass(){
		return TaxaAssociation.class;
	}
}


