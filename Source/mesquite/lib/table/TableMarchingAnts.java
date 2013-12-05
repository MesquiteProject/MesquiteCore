/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison. 
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */package mesquite.lib.table;

import mesquite.lib.*;
import java.awt.*;

public class TableMarchingAnts extends MarchingAnts {
	MesquiteTable table;
	int column1;
	int column2;
	int row1;
	int row2;
	
	public  TableMarchingAnts(MesquiteTable table, Graphics g, int column1, int row1, int column2, int row2) {
		this.column1 = column1;
		this.column2 = column2;
		this.row1 = row1;
		this.row2 = row2;
		this.table = table;
		calcMarchingAntsFromRowsColumns(column1, row1,column2, row2);
		this.g = g;
		startAnts();
	}
	public void calcMarchingAntsFromRowsColumns(int column1, int row1, int column2, int row2) {
		this.column1 = column1;
		this.column2 = column2;
		this.row1 = row1;
		this.row2 = row2;
 		x = table.getColumnX(column1)-2;
		y = table.getRowY(row1)-2;
		if (column2 == column1) 
			width = 4;
		else
			width = table.getColumnX(column2) - table.getColumnX(column1)+2;
		if (row2 == row1) 
			height = 4;
		else
			height = table.getRowY(row2) - table.getColumnX(row1)+2;
	}
	public void resetMarchingAntsFromRowsColumns(int column1, int row1, int column2, int row2) {
		calcMarchingAntsFromRowsColumns(column1, row1,column2, row2);
		resize (x, y, width, height);
	}
	public void moveAnts() {
		calcMarchingAntsFromRowsColumns(column1, row1,column2, row2);
		resize (x, y, width, height);
	}

}
