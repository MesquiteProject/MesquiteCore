/* Mesquite source code.  Copyright 1997-2008 W. Maddison and D. Maddison.
Version 2.5, June 2008.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.ExcludedCharStrip;

import java.awt.Color;
import java.awt.Graphics;

import mesquite.lib.MesquiteMenuItemSpec;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.DataColumnNamesAssistant;
import mesquite.lib.table.MesquiteTable;

public class ExcludedCharStrip extends DataColumnNamesAssistant {
	MesquiteTable table;
	CharacterData data;

	public boolean startJob(String arguments, Object condition,boolean hiredByName) {
		return true;
	}

	public void drawInCell(int ic, Graphics g, int x, int y, int w, int h, boolean selected) {
		if (data == null)
			return;
		if (data.isCurrentlyIncluded(ic))
			g.setColor(Color.white); 
		else {
			g.setColor(Color.gray);
		}
		g.fillRect(x, y, w, h);
	}

	public String getTitle() {
			return "Excluded Character Strip";
	}

	public void setTableAndData(MesquiteTable table, CharacterData data) {
		this.table = table;
		this.data = data;
	}

	public String getName() {
		return "Excluded Character Strip";
	}

	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}


}
