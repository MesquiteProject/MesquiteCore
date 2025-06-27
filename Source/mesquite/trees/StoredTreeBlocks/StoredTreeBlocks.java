/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.StoredTreeBlocks;
/*~~  */

import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Notification;
import mesquite.lib.duties.TreeBlockSource;
import mesquite.lib.duties.TreesManager;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.TreeVector;
import mesquite.lib.ui.ListDialog;

/** Supplies tree blocks stored in the projects.*/
public class StoredTreeBlocks extends TreeBlockSource implements MesquiteListener {
	int currentTreeBlockIndex=MesquiteInteger.unassigned;
	TreeVector currentTreeBlock = null;
	TreeVector lastUsedTreeBlock = null;
	TreesManager manager;
	Taxa preferredTaxa =null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		manager = (TreesManager)findElementManager(TreeVector.class);
		if (manager==null)
			return sorry(getName() + " couldn't start because no tree manager module was found.");
		if (manager.getNumberTreeBlocks()==0 && !MesquiteThread.isScripting())
			return sorry("No stored blocks of trees are available.");
		manager.addBlockListener(this);
		return true;
  	 }
	public void endJob(){
		if (manager != null)
			manager.removeBlockListener(this);
		super.endJob();
	}
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return true;
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
	/** passes which object changed*/
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification);
		if (!doomed && code != MesquiteListener.SELECTION_CHANGED && code != MesquiteListener.ANNOTATION_CHANGED && code != MesquiteListener.ANNOTATION_DELETED && code != MesquiteListener.ANNOTATION_ADDED)
			parametersChanged(notification);
	}
	
	/*.................................................................................................................*/
	/** passes which object disposed*/
	public void disposing(Object obj){
		if (obj == preferredTaxa) {
			setHiringCommand(null); //since there is no rehiring
			if (!MesquiteThread.isScripting())
				iQuit();
		}
	}
	/** Asks whether it's ok to delete the object as far as the listener is concerned (e.g., is it in use?)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;
	}
	/*.................................................................................................................*/
  	public void setPreferredTaxa(Taxa taxa) {
  		if (preferredTaxa != taxa){
	  		if (preferredTaxa != null)
	  			preferredTaxa.removeListener(this);
	  		taxa.addListener(this);
  		}
  		preferredTaxa = taxa;
  	}
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(Taxa taxa){
   	}
	/*.................................................................................................................*/
   	public TreeVector getFirstBlock(Taxa taxa) {
   		currentTreeBlockIndex=0;
   		return getCurrentBlock(taxa);
   	}
	/*.................................................................................................................*/
   	public TreeVector getBlock(Taxa taxa, int ic) {
   		currentTreeBlockIndex=ic;
   		return getCurrentBlock(taxa);
   	}
   	private void checkBlock(Taxa taxa){
   		if (manager == null)
   			return;
		int nt = manager.getNumberTreeBlocks(taxa);
		setPreferredTaxa(taxa);
		if ((!MesquiteInteger.isCombinable(currentTreeBlockIndex) || currentTreeBlockIndex>=nt || currentTreeBlockIndex<0)) {
			if (MesquiteThread.isScripting())
				currentTreeBlockIndex = 0;
			else if (nt<=1)
				currentTreeBlockIndex = 0;
			else {
				String[] list = new String[nt];
				for (int i=0; i< nt; i++)
					list[i]=manager.getTreeBlock(taxa, i).getName();
				currentTreeBlockIndex = ListDialog.queryList(containerOfModule(), "Use which tree block?", "Use which tree block? \n(for " + employer.getName() + ")", MesquiteString.helpString,list, 0);
				if (!MesquiteInteger.isCombinable(currentTreeBlockIndex))
					currentTreeBlockIndex = 0;
			}
		}
 		currentTreeBlock = manager.getTreeBlock(taxa, currentTreeBlockIndex);
   		if (currentTreeBlock!=lastUsedTreeBlock) {
   			if (currentTreeBlock != null)
   				currentTreeBlock.addListener(this);
   			if (lastUsedTreeBlock!=null)
   				lastUsedTreeBlock.removeListener(this);
   			lastUsedTreeBlock = currentTreeBlock;
   		}
   	}
	/*.................................................................................................................*/
   	public TreeVector getCurrentBlock(Taxa taxa) {
   		if (currentTreeBlockIndex>getNumberOfTreeBlocks(taxa) || currentTreeBlockIndex<0)
   			return  null;
 		checkBlock(taxa);
   		return currentTreeBlock;
   	}
	/*.................................................................................................................*/
   	public TreeVector getNextBlock(Taxa taxa) {
   		currentTreeBlockIndex++;
   		return getCurrentBlock(taxa);
   	}
	/*.................................................................................................................*/
   	public int getNumberOfTreeBlocks(Taxa taxa) {
		return manager.getNumberTreeBlocks(taxa);
   	}
   
	/*.................................................................................................................*/
   	public String getTreeBlockNameString(Taxa taxa, int index) {
		return manager.getTreeBlock(taxa, index).getName();
   	}
	/*.................................................................................................................*/
   	public String getCurrentTreeBlockNameString(Taxa taxa) {
 		checkBlock(taxa);
 		return currentTreeBlock.getName();
  	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Stored Tree Blocks";
   	 }
   	 
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Supplies lists of trees stored, for instance in a file.";
   	 }
}

