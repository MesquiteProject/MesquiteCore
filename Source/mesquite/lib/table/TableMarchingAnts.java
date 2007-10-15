package mesquite.lib.table;

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
