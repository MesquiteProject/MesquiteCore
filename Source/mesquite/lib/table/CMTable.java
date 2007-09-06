/* Mesquite source code.  Copyright 1997-2007 W. Maddison and D. Maddison.
Version 2.0, September 2007.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.table;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.duties.*;

/**  An interface used to expose extra methods of character matrix editor table to assistants. */
public abstract class CMTable extends MesquiteTable {
	public CMTable (int numRowsTotal, int numColumnsTotal, int totalWidth, int totalHeight, int rowNamesWidth, int colorScheme, boolean showRowNumbers, boolean showColumnNumbers) {
		super(numRowsTotal,  numColumnsTotal,  totalWidth,  totalHeight,  rowNamesWidth,  colorScheme,  showRowNumbers,  showColumnNumbers);
	}
	public abstract DataColumnNamesAssistant getDataColumnNamesAssistant(int subRow);
	public abstract void setLastColumnVisibleLinked(int column);
	public abstract void setFirstColumnVisibleLinked(int column);
	public abstract void setLastRowVisibleLinked(int row);
	public abstract void setFirstRowVisibleLinked(int row);
	public abstract CellColorer getCellColorer();


}
