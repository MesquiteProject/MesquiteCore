/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison. 
Version 2.6, January 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.rLink.common;
/*~~  */
import mesquite.lib.MesquiteNumber;

/* ======================================================================== */
public class RNumericMatrix  {
	public String[] columnNames;
	public String[] rowNames;
	public MesquiteNumber[][] values;
	public RNumericMatrix(){
		this(new MesquiteNumber());
	}
	public RNumericMatrix(int columns, int rows){
		columnNames = new String[columns];
		rowNames = new String[rows];
		values = new MesquiteNumber[columns][rows];
		for (int i = 0; i< columns; i++)
			for (int k =0; k< rows; k++)
				values[i][k] = new MesquiteNumber();
	}
	public RNumericMatrix(MesquiteNumber n){
		MesquiteNumber[]  aux = n.getAuxiliaries();
		if (aux == null || aux.length == 0){
			columnNames = new String[1];
			rowNames = new String[1];
			values = new MesquiteNumber[1][1];
			rowNames[0] = n.getName();
			columnNames[0] = n.getName();
			values[0][0] = new MesquiteNumber(n);
		}
		else {
			int columns = aux.length+1;
			columnNames = new String[columns];
			rowNames = new String[1];
			rowNames[0] = n.getName();
			
			values = new MesquiteNumber[columns][1];
			columnNames[0] = n.getName();
			values[0][0] = new MesquiteNumber(n);
			for (int i=1; i<columns; i++){
				values[i][0] = new MesquiteNumber(aux[i-1]);
				columnNames[i] = aux[i-1].getName();
			}
		}
		
	}
	public String[] getColumnNames(){
		return columnNames;
	}
	public String[] getRowNames(){
		return rowNames;
	}
	public MesquiteNumber[][] getValues(){
		return values;
	}
}
