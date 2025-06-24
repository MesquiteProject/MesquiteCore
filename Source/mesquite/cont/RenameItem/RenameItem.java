/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.cont.RenameItem;
/*~~  */

import mesquite.cont.lib.ContDataUtility;
import mesquite.cont.lib.ContinuousData;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteString;
import mesquite.lib.NameReference;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.ui.ListDialog;

/* ======================================================================== */
public class RenameItem extends ContDataUtility {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
   	/** Called to alter data in all cells*/
   	public boolean operateOnData(CharacterData data){
			if (!(data instanceof ContinuousData))
				return false;
			ContinuousData cData = (ContinuousData)data;
			int numItems =cData.getNumItems();
			String[] items = new String[numItems];
			for (int i=0; i<items.length; i++){
				if (StringUtil.blank(cData.getItemName(i)))
					items[i] = "(unnamed)";
				else
					items[i]= cData.getItemName(i);
			}
			int d = ListDialog.queryList(containerOfModule(), "Rename item", "Rename item:", MesquiteString.helpString, items, 0);
			if (!MesquiteInteger.isCombinable(d) || d<0 || d>=numItems)
				return false;
			else {
				String s = MesquiteString.queryString(containerOfModule(), "Rename Item", "New name for " + items[d], items[d]);
				if (StringUtil.blank(s))
					return false;
				cData.setItemReference(d, NameReference.getNameReference(s));
				return true;
			}
   	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Rename item...";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Renames an item of a continuous matrix." ;
   	 }
   	 
}


