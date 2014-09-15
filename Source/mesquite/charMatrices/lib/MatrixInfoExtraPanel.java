/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.lib;


import mesquite.lib.ClosablePanel;
import mesquite.lib.ClosablePanelContainer;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.table.MesquiteTable;


public class MatrixInfoExtraPanel extends ClosablePanel {
	protected CharacterData data;
	protected MesquiteTable table;
	protected int ic = -8;
	protected int it = -8;
	public MatrixInfoExtraPanel(ClosablePanelContainer container, String title){
		super(container, title);
		setShowTriangle(true);
	}
	public void setMatrixAndTable(CharacterData data, MesquiteTable table){
		this.data = data;
		this.table = table;
	}
	public void setCell(int ic, int it){
		this.ic = ic;
		this.it = it;
	}
	public void cellEnter(int ic, int it){
		try{
			setCell(ic, it);
		}
		catch (Exception e){
		}
	}
	public void cellExit(int ic, int it){
		try{
			setCell(-1, -1);
		}
		catch (Exception e){
		}
	}
	public void cellTouch(int ic, int it){
		try{
			setCell(ic, it);
		}
		catch (Exception e){
		}
	}
}

