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
import mesquite.lib.table.*;
import mesquite.lib.characters.*;


/* ======================================================================== */
/**This is superclass of modules to assist data matrix editor, for analysis (has menu item in menu).*/
public abstract class DataWindowAssistant extends MesquiteModule  {
	boolean active = false;

   	 public Class getDutyClass() {
   	 	return DataWindowAssistant.class;
   	 }
   	 
   	 public String getFunctionIconPath(){
   		 return getRootImageDirectoryPath() + "functionIcons/matrixEditor.gif";
   	 }
  	/** method called by data window to inform assistant that data have changed*/
	public abstract void setTableAndData(MesquiteTable table, mesquite.lib.characters.CharacterData data);
	
	//called by data editor when selection changed
	public void tableSelectionChanged(){
	}
  	public void colorsLegendGoAway(){
  	}
  	 	
 	public String getDutyName() {
 		return "Data Window Assistant";
   	}
   	public boolean hasDisplayModifications(){
   		return false;
   	}
   	public String getDisplayModString(int ic, int it){
   		return null;
   	}
   	public String getCellString(int ic, int it){
   		return null;
   	}
  	public String getCellExplanation(int ic, int it){
  		return null;
	}
  	public void focusInCell(int ic, int it){
  		
	}
 	public void setActive(boolean a){
   		active = a;
   	}
   	public boolean isActive(){
   		return active;
   	}
}


