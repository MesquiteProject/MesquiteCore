/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.dmanager.RandomizeTaxonOrder;

import java.util.*;
import java.awt.*;
import mesquite.lib.characters.*;
import mesquite.categ.lib.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;


/* ======================================================================== */
public class RandomizeTaxonOrder extends TaxonUtility {
	static Random rng = new Random(System.currentTimeMillis());
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	/** if returns true, then requests to remain on even after operateOnTaxa is called.  Default is false*/
	public boolean pleaseLeaveMeOn(){
		return false;
	}
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
 	/** Swaps parts first and second*/
	void swapParts(Associable assoc, int first, int second, int[] nums){  
  		int temp = nums[first];
  		nums[first] = nums[second];
  		nums[second] = temp;
		assoc.swapParts(first, second); 
	}
	/** Called to operate on the taxa in the block.  Returns true if taxa altered*/
	public  boolean operateOnTaxa(Taxa taxa){
		int[] nums = new int[taxa.getNumTaxa()];
		for (int i=0; i<nums.length; i++) {
			nums[i] = rng.nextInt();
		}
		
		for (int i=1; i<taxa.getNumTaxa(); i++) {
			for (int j= i-1; j>=0 && nums[j]> nums[j+1]; j--) {
				swapParts(taxa, j, j+1, nums);
			}
		}
		taxa.notifyListeners(this, new Notification(PARTS_MOVED));
		return true;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Randomize Taxon Order";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Randomizes the order of taxa.";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 250;  
	}

}





