/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.ScaleBranchLengths;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.tree.AdjustableTree;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.ui.DoubleField;
import mesquite.lib.ui.ExtensibleDialog;

/* ======================================================================== */
public class ScaleBranchLengths extends BranchLengthsAltererMult {
	double resultNum;
	double scale = 1.0;
	boolean scaleOtherLengthProperties = true; 
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		queryOptions(); 
 		//scale = MesquiteDouble.queryDouble(containerOfModule(), "Scale branch lengths", "Multiply all branch lengths by", 1.0);
		return true;
  	 }
  	 void queryOptions(){
 		MesquiteInteger buttonPressed = new MesquiteInteger(1);
 		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(),  "Scale branch lengths",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		DoubleField scaleField = dialog.addDoubleField("Multiply all branch lengths by:", scale, 4);
		Checkbox others = dialog.addCheckBox("Scale also \"height\" and \"length\" properties, if present in tree.", scaleOtherLengthProperties);
		dialog.addAuxiliaryDefaultPanels();//************
		dialog.addPrimaryButtonRow("OK");
		dialog.prepareAndDisplayDialog();
		if (buttonPressed.getValue()==0)  {
			scale = scaleField.getValue();
			scaleOtherLengthProperties = others.getState();
		}

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
   	 public boolean isSubstantive(){
   	 	return true;
   	 }
   
 	NameReference heightNR = NameReference.getNameReference("height");
 	NameReference lengthNR = NameReference.getNameReference("length");
	/*.................................................................................................................*/
	public  boolean transformTree(AdjustableTree tree, MesquiteString resultString, boolean notify){
  	 		if (MesquiteDouble.isCombinable(scale) && tree instanceof MesquiteTree) {
  	 			if (tree.hasBranchLengths()){
   					((MesquiteTree)tree).scaleAllBranchLengths(scale, false);
   					if (scaleOtherLengthProperties){
   						int numNodes = tree.getNumNodeSpaces();
   						DoubleArray lengths = tree.getAssociatedDoubles(lengthNR);
   						DoubleArray heights = tree.getAssociatedDoubles(heightNR);
   						if (lengths != null)
   							for (int i = 0; i<numNodes && i<lengths.getSize(); i++)
   								if (MesquiteDouble.isCombinable(lengths.getValue(i)))
   									lengths.setValue(i, lengths.getValue(i)*scale);
   						if (heights != null)
   							for (int i = 0; i<numNodes && i<heights.getSize(); i++)
   								if (MesquiteDouble.isCombinable(heights.getValue(i)))
   									heights.setValue(i, heights.getValue(i)*scale);
   						if (notify && tree instanceof Listened) ((Listened)tree).notifyListeners(this, new Notification(MesquiteListener.ASSOCIATED_CHANGED));
   					}
					if (notify && tree instanceof Listened) ((Listened)tree).notifyListeners(this, new Notification(MesquiteListener.BRANCHLENGTHS_CHANGED));
   				
   					return true;
   				}
   				else {
   					discreetAlert("Branch lengths of tree are all unassigned.  Cannot scale branch lengths.");
   				}
   			}
   			return false;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Scale All Branch Lengths";
   	 }
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Scale All Branch Lengths...";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Adjusts a tree's branch lengths by multiplying them by an amount." ;
   	 }
}

