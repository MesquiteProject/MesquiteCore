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
/**Supplies TaxaAssociation's for instance from a file or simulated.*/

public abstract class AssociationSource extends MesquiteModule  {
	protected int numTaxaForNewBlock=0;
	protected Taxa otherTaxa = null;
	public Class getDutyClass() {
   	 	return AssociationSource.class;
   	 }
 	public String getDutyName() {
 		return "Taxa Association Source";
   	 }
   	 
   	 public String[] getDefaultModule() {
   	 	return new String[] {"#StoredAssociations"};
   	 }

   	 /** THE following assume a single taxa block is used as anchor point, and looks for others.  
   	  * These routines are independent of the two-taxa block routines, and snapshotting will only be done on the single taxa block routines */
   	 /**Returns number of TaxaAssociation available.  If TaxaAssociation can be supplied indefinitely, returns MesquiteInteger.infinite*/
    	public abstract int getNumberOfAssociations(Taxa taxa);
  	 /**Returns indexth TaxaAssociation*/
   	public abstract TaxaAssociation getAssociation(Taxa taxa, int index);
   	
   	 /**Returns current TaxaAssociation.*/
   	public abstract TaxaAssociation getCurrentAssociation(Taxa taxa);
   	/**Returns String naming list number index*/
   	public String getAssociationNameString(Taxa taxa, int index) {
   		return "";
   	}
   	
   	public boolean requiresCreationOfTaxaA() {
   		return false;
   	}

   	public boolean requiresCreationOfTaxaB() {
   		return false;
   	}
   	
   	public void setNumTaxaForNewBlock(int numTaxa) {
   		numTaxaForNewBlock=numTaxa;
   	}

   	public int getNumTaxaForNewBlock() {
   		return numTaxaForNewBlock;
   	}
   	
  	 public Taxa getOtherTaxa() {
 		return otherTaxa;
 	}
 	public void setOtherTaxa(Taxa otherTaxa) {
 		this.otherTaxa = otherTaxa;
 	}


 	 /** THE following assume two taxa blocks are specified.  
  	  * These routines are independent of the one-taxa block routines, and snapshotting will only be done on the single taxa block routines */
  	
 	 /**Returns number of TaxaAssociation available.  If TaxaAssociation can be supplied indefinitely, returns MesquiteInteger.infinite*/
   	public abstract int getNumberOfAssociations(Taxa taxa1, Taxa taxa2);
  	 /**Returns indexth TaxaAssociation*/
   	public abstract TaxaAssociation getAssociation(Taxa taxa1, Taxa taxa2, int index);
    	
}


