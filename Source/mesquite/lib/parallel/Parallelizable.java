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

import mesquite.lib.CommandChecker;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteThread;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.duties.TreeSearcherFromMatrix;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeVector;

/* ======================================================================== */
/** The Parallelizable interface is used for code to be parallelized by an instance of the Parallelizer utility. */

/* EXAMPLE
public class ParallelizeMe implements Parallelizable {
	Parallelizer parallelizer;
	
	public ParallelizeMe (){
		parallelizer = new Parallelizer(this, 4);
		//Here hire the first instances of any employee modules. They will be cloned for the other threads.
		SpecialCalculatorModule calculatorModule = (SpecialCalculatorModule)ownerModule.hireNamedEmployee(SpecialCalculatorModule.class, "#MyCalculator");
		calculatorModule.setBigParameter(true);  //setting whatever parameters in employee modules

		parallelizer.go();
	}
}
*/
/* ############################# */
public interface Parallelizable {

	/* The Parallelizable 


	/*\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\*/
	/**Total possible count, for parallelizer to prepare array recording status of which items are done, being calculated, etc..
	 *  If some items aren't appropriate, and will be filtered, that is OK, handled in getNextParallelItemAndReserve.
 	— SYNCHRONIZED please * */
	public int getTotalPossibleParallelItemCount(); 
	/*EXAMPLE:
	 * 		
	 	return totalNumber;  //e.g., the size of the vector, the number of taxa or characters or branches, etc.
	 */

	/*\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\*/
	/**Using parallizer.setItemStatus(item, Parallelizer.INAPPLICABLE), mark which items are not to be calculated.
	 * This is optional, and it may be unnecessary, but it could help the parallelizer know in advance which aren't important to calculate */
	public void markInappropriateItems(Parallelizer parallelizer);


	/*\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\*/
	/**Finds next uncalculated item and reserves it by setting its status as being calculated, and then returns the number. Return -1 if none more
	— SYNCHRONIZED please   */
	public int getNextParallelItemAndReserve(Parallelizer parallelizer);  
	/*EXAMPLE:
	 * 		
	 	int iN = 0;
		while (iN< totalNumber){
			if (thisOneIsCompatible(iN)) && parallelizer.itemUncalculated(iN)){
				parallelizer.setItemStatus(iN, Parallelizer.BEINGCALCULATED);
				return iN;
			}
			iN++;
		}
		return -1;
	 */

	/*\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\*/
	/** Do any initiation calculations, including resetting  AND calculation of first item to make sure employees are warmed up.	
	 * The firstItem is given, even though getNextParallelItemAndReserve could be called, so that Parallelizable can be responsible to set the status of that first item, 
	 * e.g.  parallelizer.setItemStatus(firstItem, Parallelizer.CALCULATED);, without blocking it for calculation. 
	 * Also, the principle is that the Parallelizer is always the one to supply the items.
	 */
	public ParallelParams doFirstCalculation_Parallel(int firstItem, Parallelizer parallelizer, MesquiteInteger resultCode);  
	/*EXAMPLE:
	 * 		
	 	ParallelParams pp = new ParallelParams();
		pp.responsibleEmployer = ownerModule;

		//Assume ownerModule.calculatorModule is the first calculator module hired in ownerModule with full interface, options, etc.
		//It is thus set up, and ready to get a snapshot of when needed.
		pp.employees = new MesquiteModule[]{(MesquiteModule)ownerModule.calculatorModule}; //recording employees for cloning etc.

		IntegerArray myStorageArray = new IntegerArray(totalNumber); //preparing data storage for first calculation
		pp.threadObjects = new Object[]{ myStorageArray}; //remembering thread's data storage

		parallelizer.setItemStatus(firstItem, Parallelizer.BEINGCALCULATED);  //prob not necessary
		int result = doItemCalculation_Parallel(firstItem, pp, parallelizer);
		return pp;

	 * */

	/*\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\*/
	/**Clone and give snapshots to employee modules, and prepare's threads own data storage. 
	 * Parallelizer's cloneEmployee clones the employees and gets and sends the snapshots)
	SYNCHRONIZED please 
	 */
	public ParallelParams cloneForParallel(ParallelParams params, Parallelizer parallelizer); 
	/*EXAMPLE: In this example, the params are not cloned because there's no information inherited from thread to thread
	 * 		
		if (params == null)  
			return null;
		ParallelParams pp = new ParallelParams(); //setting up this thread's parallel parameters
		pp.responsibleEmployer = ownerModule;
		MesquiteModule mb = parallelizer.cloneEmployee(ownerModule, (MesquiteModule)ownerModule.calculatorModule, SpecialCalculatorModule.class);
		((SpecialCalculatorModule)mb).setBigParameter(true);  //setting whatever parameters in employee modules
		mb.setUseMenubar(false); 
		IntegerArray myStorageArray = new IntegerArray(totalNumber); //preparing thread's data storage
		pp.employees = new MesquiteModule[]{mb};
		pp.threadObjects = new Object[]{myStorageArray};
		return pp; 

	 * */

	/*\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\*/
	/** Do a calculation on item using employee modules and other params passed.
	 * Return ResultCodes.NO_ERROR if success
	 * Return others numbers otherwise*/
	public int doItemCalculation_Parallel(int item, ParallelParams params, Parallelizer parallelizer);  
	/*EXAMPLE
	 * 
		IntegerArray myData = (IntegerArray)params.threadObjects[0];  //recovering the thread's storage from params threadObjects
		SpecialCalculatorModule calculatorModule = (SpecialCalculatorModule)params.employees[0]; //recovering the thread's employee module

		//HERE do things with the employee and add results to myData
		calculatorModule.doCalculations(myData);

		//if the Parallelizable has a progress indicator, it can be updated here
		progIndicator.setText("Item just calculated " +item);
		progIndicator.setCurrentValue(parallelizer.getTotalCalculated());

		return 0;
	 * */


	/*\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\*/
	/** Return whether parallelizer shouldn't call cloneParams if the threads are reused  (e.g. because of a persistent, updated calculation.)
	 * If true, then parallelizer will just act as if the thread will keep reusing its parameter objects and employees
	 * If false, then parallelizer will re-clone everything each time the parallel calculation is redone.
	 * */
	public boolean pleaseReuseParallelThreads(); 

}

