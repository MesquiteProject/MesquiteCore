/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.TaxaSetListNum;
/*~~  */

import mesquite.lists.lib.*;
import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class TaxaSetListNum extends TaxaSetListAssistant implements MesquiteListener {
	/*.................................................................................................................*/
	public String getName() {
		return "Number of taxa in taxa set";
	}
	public String getExplanation() {
		return "Indicates number of taxa in taxa set in list window." ;
	}
	Taxa taxa=null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		/* hire employees here */
		return true;
	}

	public void setTableAndObject(MesquiteTable table, Object obj){
		if (taxa !=null)
			taxa.removeListener(this);
		if (obj instanceof Taxa)
			taxa = (Taxa)obj;
		if (taxa !=null)
			taxa.addListener(this);
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
		return "# taxa included";
	}
	public String getStringForRow(int ic){
		if (taxa == null)
			return null;
		SpecsSetVector tsets = taxa.getSpecSetsVector(TaxaSelectionSet.class);
		if (tsets ==null || ic<0 || ic>= tsets.size())
			return "";
		return Integer.toString(((TaxaSelectionSet)tsets.elementAt(ic)).numberSelected());
	}
	public String getWidestString(){
		return " # taxa included ";
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
		if (taxa !=null)
			taxa.removeListener(this);
		super.endJob();
	}

}

