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
/**Supplies trees; the number is guaranteed to be finite and known.*/

public abstract class TreeSourceDefinite extends MesquiteModule implements ItemsSource  {
   	 public Class getDutyClass() {
   	 	return TreeSourceDefinite.class;
   	 }
 	public String getDutyName() {
 		return "Tree Source (Finite)";
   	 }
	 public String getFunctionIconPath(){
   		 return getRootImageDirectoryPath() + "functionIcons/treeSource.gif";
   	 }
   	 
   	 public String[] getDefaultModule() {
   	 	return new String[] {"#FiniteTreeSourceCoord"};
   	 }
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public abstract void initialize(Taxa taxa);


   	 /** indicates preferred set of taxa for which trees will be requested */
  	public abstract void setPreferredTaxa(Taxa taxa);
  	
    	
   	 /**Returns tree number itree, and sets current tree number to itree*/
   	public abstract Tree getTree(Taxa taxa, int itree);
   	
    /**Returns number of trees available.  If trees can be supplied indefinitely, returns MesquiteInteger.infinite.  If number of trees is finite but unknown, returns MesquiteInteger.finite*/
   	public abstract int getNumberOfTrees(Taxa taxa);

   	 /**Returns name of ith tree.*/
   	public abstract String getTreeNameString(Taxa taxa, int i);
   	
 
	/*===== For ItemsSource interface ======*/
   	/** returns item numbered ic*/
   	public Object getItem(Taxa taxa, int ic){
   		return getTree(taxa, ic);
   	}
   	/** returns number of characters for given Taxa*/
   	public int getNumberOfItems(Taxa taxa){
   		return getNumberOfTrees(taxa);
   	}
   	/** returns name of type of item, e.g. "Character", or "Taxon"*/
   	public String getItemTypeName(){
   		return "Tree";
   	}
   	/** returns name of type of item, e.g. "Characters", or "Taxa"*/
   	public String getItemTypeNamePlural(){
   		return "Trees";
   	}
   	public Selectionable getSelectionable(){
   		return null;
   	}
   	/**--------*/
    	public void setEnableWeights(boolean enable){
    	}
   	public boolean itemsHaveWeights(Taxa taxa){
   		return false;
   	}
   	public double getItemWeight(Taxa taxa, int ic){
   		return MesquiteDouble.unassigned;
   	}
   	/**--------*/
   	public void prepareItemColors(Taxa taxa){
   	}
   	public Color getItemColor(Taxa taxa, int ic){
   		return null;
   	}
	/** zzzzzzzzzzzz*/
   	public String getItemName(Taxa taxa, int ic){
   		return getTreeNameString(taxa, ic);
   	}
	/*.................................................................................................................*/
  	 public CompatibilityTest getCompatibilityTest() {
  	 	return new TSrcCompatibilityTest();
  	 }
}







