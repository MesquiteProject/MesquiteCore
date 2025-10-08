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
	//synchronized please
	public int getTotalPossibleParallelItemCount(); 

	/*Using parallizer.setItemStatus(item, Parallelizer.INAPPLICABLE), mark which items are not to be calculated.
	 * This is optional, and it may be unnecessary, but it could help the parallelizer know in advance which aren't important to calculate */
	public void markInappropriateItems();


	//Returns next uncalculated item. Return -1 if none more
	//synchronized please
	public int getNextParallelItem();  

	/* Do any initiation calculations, including resetting  AND calculation of first item to make sure employees are warmed up.	
	 * The firstItem is given, even though getNextParallelItem could be called, so that Parallelizable can be responsible to set the status of that first item, 
	 * e.g.  parallelizer.setItemStatus(firstItem, Parallelizer.CALCULATED);, without blocking it for calculation. 
	 * Also, the principle is that the Parallelizer is always the one to supply the items.
	 */
	public ParallelParams doFirstCalculation_Parallel(int firstItem);

	//Clone and give snapshots to employee modules (Parallelizer provides one method as a service)
	//synchronized please
	public ParallelParams cloneForParallel(ParallelParams params); 

	/* Do a calculation on item using employee modules and other params passed.
	 * Return 0 if success
	 * Return negative number if failure*/
	public int doItemCalculation_Parallel(int item, ParallelParams params); 


	public boolean pleaseReuseParallelThreads(); // if true, then the parallelizer doesn't call cloneParams second time if thread already has them.

}

