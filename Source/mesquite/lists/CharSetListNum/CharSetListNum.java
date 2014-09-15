/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.CharSetListNum;
/*~~  */

import mesquite.lists.lib.*;
import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class CharSetListNum extends CharSetListAsst implements MesquiteListener {
	/*.................................................................................................................*/
	public String getName() {
		return "Number of characters in character set";
	}

	public String getExplanation() {
		return "Indicates number of characters in character set in list window." ;
	}
	CharacterData data=null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		/* hire employees here */
		return true;
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	public void setTableAndObject(MesquiteTable table, Object obj){
		if (data !=null)
			data.removeListener(this);
		if (obj instanceof CharacterData)
			data = (CharacterData)obj;
		if (data !=null)
			data.addListener(this);
		//table would be used if selection needed
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;  //TODO: respond
	}
	public void changed(Object caller, Object obj, Notification notification){
		if (Notification.appearsCosmetic(notification))
			return;
		parametersChanged(notification);
	}
	public String getTitle() {
		return "# chars. included";
	}
	public String getStringForRow(int ic){
		try{
			if (data == null)
				return null;
			SpecsSetVector tsets = data.getSpecSetsVector(CharSelectionSet.class);
			if (tsets ==null || ic<0 || ic>= tsets.size())
				return "";
			return Integer.toString(((CharSelectionSet)tsets.elementAt(ic)).numberSelected());
		}
		catch (NullPointerException e){
		}
		return "";
	}
	public String getWidestString(){
		return " # chars. included ";
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}
	/*.................................................................................................................*/
	public void endJob() {
		if (data !=null)
			data.removeListener(this);
		super.endJob();
	}

}

