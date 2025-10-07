/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.parallel;


/* ======================================================================== */
/** */
/* ############################# */
public interface Parallelizable {
	/*Total possible count, for parallelizer to prepare array recording status of which items are done, being calculated, etc..
	 *  If some items aren't appropriate, and will be filtered, that is OK, handled in getNextParallelItem.
	 * */
	public int getTotalPossibleParallelItemCount();  //synchronized please
	
	
	public int getNextParallelItem();  //synchronized please; return -1 if none more
	
	/* Do any initiation calculations AND calculation of first item to make sure employees are warmed up.	
Note: Parallelizable is responsible to set the status of that first item, e.g.  parallelizer.setItemStatus(firstItem, 2); 
Item Status 2 = finished successfully. Negative number = finished with error.*/
	public ParallelParams doFirstParallelCalculation();
	
	public ParallelParams cloneForParallel(ParallelParams params); //synchronized please; Clone and give snapshots to employee modules (Parallelizer provides one method as a service)
	
	public boolean pleaseReuseParallelThreads(); // if true, then the parallelizer doesn't call cloneParams second time if thread already has them.
	
	/* Do a calculation on item using employee modules and other params passed*/
	public int doParallelCalculation(int item, ParallelParams params); //please call parallelizer.setItemStatus(item, 1); to say calculation in progress

}

