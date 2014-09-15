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
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;


/* ======================================================================== */
public abstract class TreeListAssistant extends ListAssistant  {

   	 public Class getDutyClass() {
   	 	return TreeListAssistant.class;
   	 }
 	public String getDutyName() {
 		return "Tree list assistant";
   	 }

	public abstract void setTableAndTreeBlock(MesquiteTable table, TreeVector trees);
	
	public String getStringForTree(int ic){
		return null;
	}
	public void drawInCell(int ic, Graphics g, int x, int y,  int w, int h, boolean selected){
	}
	public void setTableAndObject(MesquiteTable table, Object object){
		if (object instanceof TreeVector)
			setTableAndTreeBlock(table, (TreeVector)object);
	}
	public String getStringForRow(int ic){
		try{
			return getStringForTree(ic);
		}
		catch (NullPointerException e){}
		return "";
	}
}

