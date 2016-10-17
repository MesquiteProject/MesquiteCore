/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.duties;

import java.awt.*;
import mesquite.lib.*;


/* ======================================================================== */
/**Supplies TreeVectors for instance from a file or simulated.*/

public abstract class TreeBlockSource extends MesquiteModule implements ItemsSource  {
   	 public Class getDutyClass() {
   	 	return TreeBlockSource.class;
   	 }
 	public String getDutyName() {
 		return "Tree Block Source";
   	 }
	 public String getFunctionIconPath(){
   		 return getRootImageDirectoryPath() + "functionIcons/treeSource.gif";
   	 }
 	 
   	 public String[] getDefaultModule() {
   	 	return new String[] {"#StoredTreeBlocks"};
   	 }
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public abstract void initialize(Taxa taxa);

   	 /** indicates preferred set of taxa for which trees will be requested */
  	public abstract void setPreferredTaxa(Taxa taxa);
  	
   	 /**Returns first tree block, and sets current tree number to 0*/
   	public abstract TreeVector getFirstBlock(Taxa taxa);
   	
   	 /**Returns indexth tree block*/
   	public abstract TreeVector getBlock(Taxa taxa, int ic);
   	
   	 /**Increments current list number and returns that list.*/
   	public abstract TreeVector getNextBlock(Taxa taxa);
   	
   	 /**Returns current tree block.*/
   	public abstract TreeVector getCurrentBlock(Taxa taxa);

   	 /**Returns number of trees available.  If trees can be supplied indefinitely, returns MesquiteInteger.infinite*/
   	public abstract int getNumberOfTreeBlocks(Taxa taxa);
   	
   	 /**Returns name of ith tree block.*/
   	public abstract String getTreeBlockNameString(Taxa taxa, int i);

   	/** queryies the user to choose a tree and returns an integer of the tree chosen*/
   	public int queryUserChoose(Taxa taxa, String forMessage){
 		int ic=MesquiteInteger.unassigned;
 		int numBlocks = getNumberOfTreeBlocks(taxa);
 		if (MesquiteInteger.isCombinable(numBlocks)){
 			String[] s = new String[numBlocks];
 			for (int i=0; i<numBlocks; i++){
 				s[i]= getTreeBlockNameString(taxa, i);
 			}
 			if (forMessage == null) {
 				forMessage = "Choose tree block";
 			}
 			return ListDialog.queryList(containerOfModule(), "Choose tree block", forMessage, MesquiteString.helpString, s, 0);
 		}
 		else  {
 			if (forMessage == null) {
 				forMessage = "Number of tree block to be used";
 			}
 			int r = MesquiteInteger.queryInteger(containerOfModule(), "Choose tree block", forMessage, 1);
 			if (MesquiteInteger.isCombinable(r))
 				return MesquiteTree.toInternal(r);
 			else
 				return r;
 		}
 				
    	}

	/*===== For ItemsSource interface ======*/
   	/** returns item numbered ic*/
   	public Object getItem(Taxa taxa, int ic){
   		return getBlock(taxa, ic);
   	}
   	/** returns number of characters for given Taxa*/
   	public int getNumberOfItems(Taxa taxa){
   		return getNumberOfTreeBlocks(taxa);
   	}
   	/** returns name of type of item, e.g. "Character", or "Taxon"*/
   	public String getItemTypeName(){
   		return "Tree Block";
   	}
   	/** returns name of type of item, e.g. "Characters", or "Taxa"*/
   	public String getItemTypeNamePlural(){
   		return "Tree Blocks";
   	}
   	
   	public Selectionable getSelectionable(){
   		return null;
   	}
     	public void setEnableWeights(boolean enable){
    	}
  	public boolean itemsHaveWeights(Taxa taxa){
   		return false;
   	}
   	public double getItemWeight(Taxa taxa, int ic){
   		return MesquiteDouble.unassigned;
   	}
  	/** zzzzzzzzzzzz*/
   	public void prepareItemColors(Taxa taxa){
   	}
  	/** zzzzzzzzzzzz*/
   	public Color getItemColor(Taxa taxa, int ic){
   		return null;
   	}
	/** zzzzzzzzzzzz*/
   	public String getItemName(Taxa taxa, int ic){
   		return getTreeBlockNameString(taxa, ic);
   	}
}


