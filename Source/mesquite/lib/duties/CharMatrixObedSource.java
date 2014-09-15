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
import mesquite.lib.characters.*;


/* ======================================================================== */
/**
See description of its subclass, CharMatrixSource.
*/

public abstract class CharMatrixObedSource extends CharMatrixFiller implements ItemsSource  {

   	 public Class getDutyClass() {
   	 	return CharMatrixObedSource.class;
   	 }
 	public String getDutyName() {
 		return "Character Matrix Source (obed.)";
   	 }
        
   	/** returns item numbered ic*/
   	public Object getItem(Taxa taxa, int ic){
		CommandRecord.tick("Getting matrix " + ic);
   		return getMatrix(taxa, ic);
   	}
   	/** returns number of characters for given Taxa*/
   	public int getNumberOfItems(Taxa taxa){
   		return getNumberOfMatrices(taxa);
   	}
   	/** returns name of type of item, e.g. "Character", or "Taxon"*/
   	public String getItemTypeName(){
   		return "Matrix";
   	}
   	/** returns name of type of item, e.g. "Characters", or "Taxa"*/
   	public String getItemTypeNamePlural(){
   		return "Matrices";
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
   		return getMatrixName(taxa, ic);
   	}
}


