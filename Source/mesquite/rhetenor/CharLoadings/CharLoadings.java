/* Mesquite source code (Rhetenor package).  Copyright 1997 and onward E. Dyreson and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.rhetenor.CharLoadings;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.rhetenor.lib.*;

/* ======================================================================== */
public class CharLoadings extends CharacterLoadings {
	Ordination ord;
	int currentAxis = 0;
	Taxa taxa;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
  	 }
  	 
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(CharacterDistribution charStates){
   	}
   	public void setOrdination(Ordination ord, Taxa taxa){
   		this.ord = ord;
   		this.taxa = taxa;
   	}
   	public void setCurrentAxis(int i){
   		if (MesquiteInteger.isPositive(i) || i==0) {
	   		if (ord==null) 
	   			currentAxis = i;
	   		else {
	   			if (i>= ord.getNumberOfAxes())
	   				currentAxis = ord.getNumberOfAxes()-1;
	   			else
	   				currentAxis = i;
	   		}
   		}
   	}
   	public int getCurrentAxis(){
   		return currentAxis;
   	}
   	public int getNumberOfAxes(){
   		if (ord==null) 
   			return 0;
   		else
   			return ord.getNumberOfAxes();
   	}
   	public boolean getUserChooseable(){
   		return false;
   	}
	/*.................................................................................................................*/
	public  void calculateNumber(CharacterDistribution charStates, MesquiteNumber result, MesquiteString resultString) {
    	 	if (result==null || ord == null || charStates == null)
    	 		return;
    	 	
    	   	clearResultAndLastResult(result);
    	   	double[][] loads = ord.getEigenvectors();
    	 	if (loads==null){
    	 		MesquiteMessage.warnProgrammer("sorry, loadings null");
    	 		return;
    	 	}
    	 	if (charStates instanceof CharacterStates && currentAxis < loads.length && currentAxis>=0 && charStates.getParentCharacter()< loads[currentAxis].length)
       	 		result.setValue(loads[currentAxis][charStates.getParentCharacter()]);
       	 	else
       	 		result.setToUnassigned();
		if (resultString!=null)
			resultString.setValue("Loading: "+ result.toString());
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public String getParameters(){
		if (ord==null)
			return "";
		return "Character loadings on " + ord.getAxisName(currentAxis); 
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Character Loadings";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Returns loadings of a character in an ordination" ;
   	 }
   	 
	/*.................................................................................................................*/
    	 public boolean isPrerelease() {
		return false;
   	 }
   	 
}

