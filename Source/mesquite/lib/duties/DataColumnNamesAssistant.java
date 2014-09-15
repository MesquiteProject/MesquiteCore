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
import java.awt.event.*;
import java.util.*;
import mesquite.lib.duties.*;
import mesquite.lib.*;
import mesquite.lib.table.*;
import mesquite.lib.characters.*;



/* ======================================================================== */
public abstract class DataColumnNamesAssistant extends MesquiteModule  {
	protected mesquite.lib.characters.CharacterData data=null;
	protected MesquiteTable table=null;
	
   	 public Class getDutyClass() {
   	 	return DataColumnNamesAssistant.class;
   	 }
 	public String getDutyName() {
 		return "Data column names panel assistant";
   	 }
	/**  Set the table and data for the module.  This allows the rest of Mesquite to communicate to the module
	 * the MesquiteTable and CharacterData objects.  The module should store the table and data so that they can 
	 * use them in the drawInCell method.  */
   	public abstract void setTableAndData(MesquiteTable table, mesquite.lib.characters.CharacterData data);
   
	/** Draw cell for row ic */
	public abstract void drawInCell(int ic, Graphics g, int x, int y,  int w, int h, boolean selected);
	

	/** Returns string to be placed in cell for row */
	public  String getStringForCharacter(int ic){
		return "";
	}

	/** Returns string to be displayed in explanation area on mouseover of cell */
	public String getStringForExplanation(int ic){
		return "";
	}

	/** Returns widest string in column; can return by default a long string for safety */
	public String getWidestString(){
		return "";
	}
	public boolean canHireMoreThanOnce(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean similarChar(int ic, int ic2) {
		return false;
	}
	/*.................................................................................................................*/
	/** returns the first (if right is true) or last (if right is false) character in the next block with the same 
	properties as the block that is touched */
	public boolean getNextBlock(int touched, boolean right, MesquiteInteger startBlock, MesquiteInteger endBlock) {
		if (data==null || startBlock==null || endBlock==null)
			return false;
		boolean currentBlock = true;
		boolean sim = false;
		startBlock.setValue(0);
		endBlock.setValue(data.getNumChars()-1);
		if (right) {
			for (int ic=touched+1; ic<data.getNumChars(); ic++) {
				sim = similarChar(touched,ic);
				if (!sim)
					currentBlock = false;
				else if (sim && !currentBlock) {
					startBlock.setValue(ic);
					for (int ic2=ic+1; ic2<data.getNumChars(); ic2++) {
						if (!similarChar(touched,ic2)) {
							endBlock.setValue(ic2-1);
							break;
						}
					}
					return true;
				}
			}
			return false;
		}
		else {  // going left
			for (int ic=touched-1; ic>=0; ic--) {
				sim = similarChar(touched,ic);
				if (!sim)
					currentBlock = false;
				else if (sim && !currentBlock) { 
					endBlock.setValue(ic);
					for (int ic2=ic-1; ic2>=0; ic2--) {
						if (!similarChar(touched,ic2)) {
							startBlock.setValue(ic2+1);
							break;
						}
					}
					return true;
				}
			}
			return false;
		}
	}
	/*.................................................................................................................*/
	public String toString() {
		return "";
 	}
	/*.................................................................................................................*/
	public void setElement(int which) {
 	}
	public int getColumnWidth(){
		return 0;
	}
	/** Returns title of item */
	public abstract String getTitle();
}

