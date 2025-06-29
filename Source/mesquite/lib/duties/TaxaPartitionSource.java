/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.duties;

import mesquite.lib.MesquiteModule;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.TaxaPartition;


/* ======================================================================== */
/**
This class of modules supplies TaxaPartitions for use in calculation routines.
(example, CurrentTaxaPartition and PartitionFromAssociation)*/

public abstract class TaxaPartitionSource extends MesquiteModule  {

   	 public Class getDutyClass() {
   	 	return TaxaPartitionSource.class;
   	 }
 	public String getDutyName() {
 		return "Source of Taxa Partitions";
   	 }
   	 public String[] getDefaultModule() {
   	 	return new String[] {"#CurrentTaxaPartition"};
   	 }
   	/** returns partition for taxa */
   	public abstract TaxaPartition getPartition(Taxa taxa);
   	
}


