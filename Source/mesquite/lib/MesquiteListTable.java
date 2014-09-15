/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib;

import java.awt.*;
import java.awt.event.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
/* ======================================================================== */
/** A table for List windows*/
public class MesquiteListTable extends MesquiteTable {
	MesquiteModule ownerModule;
	Listable[] array;
	ListableVector vector; 
	boolean useArray=true;
	
	public MesquiteListTable (Listable[] array, MesquiteModule ownerModule, int numRowsTotal, int numColumnsTotal, int totalWidth, int totalHeight, int columnNamesWidth) {
		super(numRowsTotal, numColumnsTotal, totalWidth, totalHeight, columnNamesWidth, ColorDistribution.getColorScheme(ownerModule),true,false);
		this.ownerModule=ownerModule;
		this.array = array;
		useArray=true;
	}
	
	public MesquiteListTable (ListableVector vector, MesquiteModule ownerModule, int numRowsTotal, int numColumnsTotal, int totalWidth, int totalHeight, int columnNamesWidth) {
		super(numRowsTotal, numColumnsTotal, totalWidth, totalHeight, columnNamesWidth, ColorDistribution.getColorScheme(ownerModule),true,false);
		this.ownerModule=ownerModule;
		this.vector = vector;
		useArray=false;
	}
	
	public void drawMatrixCell(Graphics g, int x, int y,  int w, int h, int column, int row, boolean selected){  
	}
	
	public void drawColumnNameCell(Graphics g, int x, int y, int w, int h, int column){
	}
	/*.................................................................................................................*/
//	public String getRowComment(int row) {
//		return getExplanation(row);
/*		String rN = "";
		if (useArray) {
//			if (!(array == null || row>=array.length || array[row] == null))
//				rN = ((Listable)array[row]).getExplanation();
		}
		else {
			if (!(vector == null || row>=vector.size() || vector.elementAt(row) == null)) 
				rN = ((Listable)vector.elementAt(row)).getExplanation();
		}
		return rN;
*/
//	}

	/*.................................................................................................................*/

	public void drawRowNameCell(Graphics g, int x, int y,  int w, int h, int row){
		String rN = null;
		if (useArray) {
			if (array == null) {
				MesquiteMessage.warnProgrammer("Error: List table is using array but array is null");
				rN = "<array null>";
			}
			else if (row>=array.length) {
				MesquiteMessage.warnProgrammer("Error: List table has too many rows (array) " + row);
				rN = "<no array element>";
			}
			else if (array[row] == null) {
				MesquiteMessage.warnProgrammer("Error: List table has null element (array)" + row);
				rN = "<null array element>";
			}
			else
				rN = ((Listable)array[row]).getName();
		}
		else {
			if (vector == null) {
				MesquiteMessage.warnProgrammer("Error: List table is using vector but vector is null");
				rN = "<vector null>";
			}
			else if (row>=vector.size()) {
				MesquiteMessage.warnProgrammer("Error: List table has too many rows (vector)" + row);
				rN = "<no vector element>";
			}
			else if (vector.elementAt(row) == null) {
				MesquiteMessage.warnProgrammer("Error: List table has null element (vector)" + row);
				rN = "<null vector element>";
			}
			else
				rN = ((Listable)vector.elementAt(row)).getName();
		}
		if (rN==null)
			rN="<no name>";
		g.drawString(rN, x+getNameStartOffset(), StringUtil.getStringVertPosition(g,y,h, null));
	}
	public void cellTouched(int column, int row, int regionInCellH, int regionInCellV,int modifiers, int clickCount) {
		System.out.println(" Item " + Integer.toString(row) + " and feature " + Integer.toString(column) + " touched");
	}
}

