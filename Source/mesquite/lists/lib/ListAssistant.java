/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lists.lib;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import mesquite.lib.duties.*;
import mesquite.lib.*;
import mesquite.lib.table.*;



/* ======================================================================== */
public abstract class ListAssistant extends MesquiteModule  {
	
   	 public Class getDutyClass() {
   	 	return ListAssistant.class;
   	 }
 	public String getDutyName() {
 		return "List assistant";
   	 }
	/**  Set the table and list's object (e.g, listable vector, data matrix for characters, etc.) */
	public abstract void setTableAndObject(MesquiteTable table, Object object);
	
	/** Returns whether to use the string from getStringForRow; otherwise call drawInCell*/
	public boolean useString(int ic){
		return (getStringForRow(ic)!=null);
	}
	/** Returns string to be placed in cell for row */
	public abstract String getStringForRow(int ic);

	/** Gets background color for cell for row ic.  Override it if you want to change the color from the default. */
	public Color getBackgroundColorOfCell(int ic, boolean selected){
		return null;
	}
	/*...............................................................................................................*/
	/** returns whether or not a cell of table is editable.*/
	public boolean isCellEditable(int row){
		return false;
	}
	/*...............................................................................................................*/
	/** for those permitting editing, indicates user has edited to incoming string.*/
	public void setString(int row, String s){
		
	}

	/** Draw cell for row ic */
	public void drawInCell(int ic, Graphics g, int x, int y,  int w, int h, boolean selected){
	}
	/** Returns widest string in column; can return by default a long string for safety */
	public String getWidestString(){
		return "";
	}
	public String getExplanationForRow(int ic){
		return null;
	}
	
	public boolean canHireMoreThanOnce(){
		return false;
	}
	
	/*.................................................................................................................*/
	public boolean arrowTouchInRow(int ic, boolean doubleClick){ //so assistant can do something in response to arrow touch; return true if the event is to stop there, i.e. be intercepted
		return false;
	}
	

	/*.................................................................................................................*/
	public String moduleActiveWord() {
		return "shown";
	}
	public int getColumnWidth(){
		return 0;
	}
	/** Returns column title */
	public abstract String getTitle();
}

