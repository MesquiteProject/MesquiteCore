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

import java.awt.*;
import mesquite.lib.*;




/* ======================================================================== */
/**Supplies a number for two trees, e.g. how different they are.*/

public abstract class NumberFor2Trees extends MesquiteModule  {

   	 public Class getDutyClass() {
   	 	return NumberFor2Trees.class;
   	 }
 	public String getDutyName() {
 		return "Number for Two Trees";
   	 }
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public abstract void initialize(Tree t1, Tree t2);
   	
	public abstract void calculateNumber(Tree t1, Tree t2, MesquiteNumber result, MesquiteString resultString);
	
	public boolean largerIsFurther(){
		return true;
	}
	
   	 public String[] getDefaultModule() {
   	 	return new String[] {"#RFtreeDifference", "#WeightedRFtreeDifference"};
   	 }
}


