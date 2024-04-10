/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.ConcatTreeBlocks;

import java.awt.*;
import java.util.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lists.lib.*;


/* ======================================================================== */
public class ConcatTreeBlocks extends TreeBlockListUtility  {
	boolean prefixTreeNameByBlockName = true;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		loadPreferences();
		return true;
	}
	
	
	/*.................................................................................................................*/
	public void processSingleXMLPreference(String tag, String content) {
		if ("prefixTreeNameByBlockName".equalsIgnoreCase(tag))
			prefixTreeNameByBlockName = MesquiteBoolean.fromTrueFalseString(content);
	}

	/*.................................................................................................................*/
	public String preparePreferencesForXML() {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "prefixTreeNameByBlockName", prefixTreeNameByBlockName);
		return buffer.toString();
	}

	/*.................................................................................................................*/
   	public boolean isPrerelease(){
   		return false;
   	}
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}

   	/** if returns true, then requests to remain on even after operateOnTrees is called.  Default is false*/
   	public boolean pleaseLeaveMeOn(){
   		return false;
   	}
   	
	/*.................................................................................................................*/
	public boolean queryOptions() {
		loadPreferences();

		if (!MesquiteThread.isScripting()){
			MesquiteInteger buttonPressed = new MesquiteInteger(1);
			ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Concatenation Options",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
			Checkbox prefixBox = dialog.addCheckBox("prefix tree names by tree block name", prefixTreeNameByBlockName);
			dialog.completeAndShowDialog(true);
			if (buttonPressed.getValue()==0)  {
				prefixTreeNameByBlockName = prefixBox.getState();
				storePreferences();
			}
			dialog.dispose();
			return (buttonPressed.getValue()==0);
		}
		return true;
	}

   	/** Called to operate on the tree blocks.  Returns true if tree blocks altered*/
   	public boolean operateOnTreeBlocks(TreeVector[] blocks){
   		if (blocks == null || blocks.length <=1 || blocks[0]==null) {
   			alert("Sorry, no tree blocks were chosen or available for concatenation.");
   			return false;
   		}
		if (!queryOptions())
			return false;

   		TreeVector block = blocks[0];
    		Taxa taxa = block.getTaxa();
   		int count = 0;
   		for (int i=0; i<blocks.length; i++){
   			if (blocks[i].getTaxa() == taxa)
   				count++;
   		}
   		if (count<=1) {
   			discreetAlert( "Sorry, there is only one block of trees for the set of taxa (concatenation uses only tree blocks with the same the taxa block as the first tree block selected).");
   			return false;
   		}
  		boolean otherTaxaBlockFound = false;
    	 	MesquiteFile file = getProject().chooseFile( "Select file to which to add the concatenated tree block");
		incrementMenuResetSuppression();
		getProject().incrementProjectWindowSuppression();
		Vector v = pauseAllPausables();
		TreeVector concat = new TreeVector(taxa);
   		for (int i=0; i<blocks.length; i++){
   			if (blocks[i].getTaxa() == taxa) {
				for (int j=0; j<blocks[i].size(); j++){
						Tree tree = blocks[i].getTree(j);
						if (tree!=null) {
							MesquiteTree t = tree.cloneTree();
							if (prefixTreeNameByBlockName){
								String name = blocks[i].getName()+": "+t.getName();
								t.setName(name);
							}
							concat.addElement(t, false);
						}
				}
   			}
   			else
   				otherTaxaBlockFound = true;
   		}
   		unpauseAllPausables(v);
		concat.setName("Concatenated");
		concat.addToFile(file, getProject(), findElementManager(TreeVector.class));  
		if (otherTaxaBlockFound)
			discreetAlert( "Not all of the tree blocks were concatenated, because some were for different taxon blocks.  Only those tree blocks referring to the same taxa as the first were concatenated.");
		getProject().decrementProjectWindowSuppression();
		decrementMenuResetSuppression();
		resetAllMenuBars();
		return true;
   	}
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Concatenate tree blocks...";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Concatenate tree blocks";
   	 }
   	 
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Concatenates selected tree blocks into a new tree block.";
   	 }
}

