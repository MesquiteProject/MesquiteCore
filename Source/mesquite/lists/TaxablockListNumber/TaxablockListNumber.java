/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.TaxablockListNumber;
/*~~  */

import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteListener;
import mesquite.lib.Notification;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.taxa.Taxa;
import mesquite.lists.lib.TaxaBlocksListAssistant;

/* ======================================================================== */
public class TaxablockListNumber extends TaxaBlocksListAssistant implements MesquiteListener {
	/*.................................................................................................................*/
	public String getName() {
		return "Number of taxa in set";
	}
	public String getExplanation() {
		return "Indicates number of taxa in list window." ;
	}
	ListableVector taxas=null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		/* hire employees here */
		return true;
	}

	public void setTableAndObject(MesquiteTable table, Object obj){
		if (taxas !=null)
			taxas.removeListener(this);
		if (obj instanceof ListableVector)
			this.taxas = (ListableVector)obj;
		taxas.addListener(this);
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
		if (Notification.appearsCosmetic(notification))
			return;
		parametersChanged(notification);
	}
	public String getTitle() {
		return "# taxa";
	}
	public String getStringForRow(int ic){
		try{
			if (taxas ==null || ic<0 || ic> taxas.size())
				return "";
			return Integer.toString( ((Taxa)taxas.elementAt(ic)).getNumTaxa());
		}
		catch (NullPointerException e){}
		return "";
	}
	public String getWidestString(){
		return " 888888 ";
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public void endJob() {
		if (taxas !=null)
			taxas.removeListener(this);
		super.endJob();
	}

}

