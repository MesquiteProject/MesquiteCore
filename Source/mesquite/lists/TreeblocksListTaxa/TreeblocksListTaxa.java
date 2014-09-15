/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.TreeblocksListTaxa;
/*~~  */

import mesquite.lists.lib.*;
import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class TreeblocksListTaxa extends TreeblocksListAssistant implements MesquiteListener {
	/*.................................................................................................................*/
	public String getName() {
		return "Taxa of tree block";
	}
	public String getExplanation() {
		return "Indicates taxa of tree block." ;
	}
	ListableVector treeBlocks=null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		/* hire employees here */
		return true;
	}

	public void setTableAndObject(MesquiteTable table, Object obj){
		if (treeBlocks !=null)
			treeBlocks.removeListener(this);
		if (obj instanceof ListableVector)
			this.treeBlocks = (ListableVector)obj;
		treeBlocks.addListener(this);
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
		return "Taxa";
	}
	public String getStringForRow(int ic){
		try{
			if (treeBlocks ==null || ic<0 || ic> treeBlocks.size())
				return "";
			TreeVector trees = ((TreeVector)treeBlocks.elementAt(ic));
			if (trees !=null) {
				Taxa taxa = trees.getTaxa();
				if (taxa !=null)
					return taxa.getName();
				else
					return "?";
			}
		}
		catch (NullPointerException e){}
		return "";
	}
	public String getWidestString(){
		String best = " 888888 ";
		if (treeBlocks==null)
			return best;
		int m = 8;
		for (int i=0; i< treeBlocks.size(); i++) {
			Taxa t = ((TreeVector)treeBlocks.elementAt(i)).getTaxa();
			if (t!=null && t.getName()!=null){
				String s = t.getName();
				int n = s.length();
				if (n>m) {
					m=n;
					best = s;
				}
			}
		}
		return best + "888";

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
		if (treeBlocks !=null)
			treeBlocks.removeListener(this);
		super.endJob();
	}

}

