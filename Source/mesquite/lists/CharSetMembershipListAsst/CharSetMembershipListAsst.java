/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.CharSetMembershipListAsst;
/*~~  */

import mesquite.lists.lib.*;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class CharSetMembershipListAsst extends CharListAssistant {
	/*.................................................................................................................*/
	public String getName() {
		return "Character Set Membership";
	}

	public String getExplanation() {
		return "Shows, in the character list window, the character sets to which a character belongs." ;
	}

	CharacterData data=null;
	MesquiteTable table=null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
		/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.data = data;
		this.table = table;
	}
	public void changed(Object caller, Object obj, Notification notification){
		if (Notification.appearsCosmetic(notification))
			return;
		parametersChanged(notification);
	}
	public String getTitle() {
		return "Character Set Membership";
	}

	int timesDrawn = 0;
	public  boolean useString(int ic){
		return true;
	}
	public  String getStringForRow(int ic){
		try{
			boolean first = true;
			StringBuffer sb = new StringBuffer();
 			SpecsSetVector ssv = data.getSpecSetsVector(CharSelectionSet.class);
 			if (ssv!=null) {
 				for (int i=0; i<ssv.getNumSpecsSets(); i++) {
 					CharSelectionSet css = (CharSelectionSet)ssv.elementAt(i);
 					if (css!=null) {
 						if (css.isBitOn(ic)) {
 							if (!first)
 								sb.append(", ");
 							sb.append(css.getName());
 							first=false;
 						}
 					}
 				}
 			}
 			return sb.toString();

		}
		catch (NullPointerException e){
		}
		return "";
	}
	public String getWidestString(){
		return "8888888888888888 ";
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 310;  
	}

}

