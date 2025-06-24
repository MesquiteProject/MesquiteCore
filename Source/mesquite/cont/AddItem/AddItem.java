/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.cont.AddItem;
/*~~  */

import mesquite.cont.lib.ContDataUtility;
import mesquite.cont.lib.ContinuousData;
import mesquite.lib.MesquiteString;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;

/* ======================================================================== */
public class AddItem extends ContDataUtility {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
   	/** Called to alter data in all cells*/
   	public boolean operateOnData(CharacterData data){
			if (!(data instanceof ContinuousData))
				return false;
			ContinuousData cData = (ContinuousData)data;
			String items = "";
			for (int i=0; i<cData.getNumItems(); i++){
				if (StringUtil.blank(cData.getItemName(i)))
					items += " (unnamed)";
				else
					items += " " + cData.getItemName(i);
			}
			String d = MesquiteString.queryString(containerOfModule(), "New item", "Currently the items are: " + items + "; enter name of new item", "");
			if (StringUtil.blank(d))
				return false;
			else {
				cData.addItem(d);
				return true;
			}
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Add item...";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Adds an item in each cell of a continuous matrix." ;
   	 }
   	 
}


