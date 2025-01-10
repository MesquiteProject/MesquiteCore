/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.cont.Standardize;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.table.*;
import mesquite.lib.ui.AlertDialog;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.cont.lib.*;

/* ======================================================================== */
public class Standardize extends ContDataAlterer  implements AltererContinuousTransformations{
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return false;  
   	}

   	/** Called to alter data in those cells selected in table*/
   	public int alterData(CharacterData data, MesquiteTable table,  UndoReference undoReference){
   		boolean did=false;
   		if (!(data instanceof ContinuousData))
   			return INCOMPATIBLE_DATA;
   		ContinuousData cData = (ContinuousData)data;
   		if (data !=null){
   			UndoInstructions undoInstructions = data.getUndoInstructionsAllMatrixCells(new int[] {UndoInstructions.NO_CHAR_TAXA_CHANGES});
   			boolean sample = (AlertDialog.query(containerOfModule(), "Standardize", "Do you want to treat the taxa as a sample (i.e. use standard deviation calculated with /(n-1)) or a population (i.e., use /n)?", "Sample (n-1)", "Population (n)"));
   			double sum;
   			for (int i=0; i<cData.getNumChars(); i++) {
   				sum = 0;
   				int n = 0;
   				for (int j=0; j<cData.getNumTaxa(); j++) {
   					if (shouldIDoIt(i,j, table)){
   						double s = cData.getState(i,j,0);
   						if (MesquiteDouble.isCombinable(s)) {
   							sum+= s;
   							n++;
   						}
   					}
   				}
   				int nUsedForSD = n-1;
   				if (!sample)
   					nUsedForSD = n;
   				if (n>1){
   					double mean = sum/n;
   					sum = 0;
   					for (int j=0; j<cData.getNumTaxa(); j++) {
   						if (shouldIDoIt(i,j, table)){
   							double s = cData.getState(i,j,0);
   							did = true;
   							if (MesquiteDouble.isCombinable(s)) {
   								sum+= (mean-s)*(mean-s);
   							}
   						}
   					}
   					double v = Math.sqrt(sum/nUsedForSD); //std dev
   					if (v>0){
   						for (int j=0; j<cData.getNumTaxa(); j++) {
   							if (shouldIDoIt(i,j, table)){
   								double s = cData.getState(i,j,0);
   								if (MesquiteDouble.isCombinable(s))
   									cData.setState(i,j, 0, (s-mean)/v);
   							}
   						}
   					}
   					else //no variance; just subtract mean, which should yield 0!
   						for (int j=0; j<cData.getNumTaxa(); j++) {
   							if (shouldIDoIt(i,j, table)){
   								double s = cData.getState(i,j,0);
   								if (MesquiteDouble.isCombinable(s))
   									cData.setState(i,j, 0, (s-mean));
   							}
   						}




   				}
   			}
   			if (undoInstructions!=null) {
   				undoInstructions.setNewData(data);
   				if (undoReference!=null){
   					undoReference.setUndoer(undoInstructions);
   					undoReference.setResponsibleModule(this);
   				}
   			}
  		}
   		if (did)
   			return SUCCEEDED;
   		return MEH;
   	}

   	boolean shouldIDoIt(int ic, int it, MesquiteTable table){
   		if (table==null)
   			return true;
   		if (table.isColumnSelected(ic) || table.isCellSelected(ic, it) || !table.anythingSelected())
   			return true;
   		return false;
   	}
	
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Standardize";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Alters continuous data by standardizing to have mean = 0 and unbiased estimate of variance = 1.  Modifies only the first item of a multi-item matrix" ;
   	 }
   	 
}


