/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.CharMatricesListNumChars;
/*~~  */

import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteListener;
import mesquite.lib.Notification;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.table.MesquiteTable;
import mesquite.lists.lib.CharMatricesListAssistant;

/* ======================================================================== */
public class CharMatricesListNumChars extends CharMatricesListAssistant implements MesquiteListener {
	/*.................................................................................................................*/
	public String getName() {
		return "Number of characters of data matrix";
	}
	public String getExplanation() {
		return "Indicates taxa of data matrix." ;
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		/* hire employees here */
		return true;
	}

	public void setTableAndObject(MesquiteTable table, Object obj){
		if (datas !=null)
			datas.removeListener(this);
		if (obj instanceof ListableVector)
			this.datas = (ListableVector)obj;
		datas.addListener(this);
		//table would be used if selection needed
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		//TODO: respond
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;  //TODO: respond
	}
	public void changed(Object caller, Object obj, Notification notification){
		parametersChanged(notification);
	}
	public String getTitle() {
		return "# Chars.";
	}
	public String getStringForRow(int ic){
		try {
			if (datas ==null || ic<0 || ic> datas.size())
				return "";
			return Integer.toString(((CharacterData)datas.elementAt(ic)).getNumChars());
		}
		catch (NullPointerException e){
		}
		return "";
	}
	public String getWidestString(){
		return " 888888 ";
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
	/*.................................................................................................................*/
	public void endJob() {
		if (datas !=null)
			datas.removeListener(this);
		super.endJob();
	}

}

