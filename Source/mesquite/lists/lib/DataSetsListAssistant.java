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
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;



/* ======================================================================== */
public abstract class DataSetsListAssistant extends ListAssistant  {
	protected ListableVector datas =null;

   	 public Class getDutyClass() {
   	 	return DataSetsListAssistant.class;
   	 }
 	public String getDutyName() {
 		return "Data sets list assistant";
   	 }

	public void drawInCell(int ic, Graphics g, int x, int y,  int w, int h, boolean selected){
	}
	
	/** Gets text color for cell for row ic.  Override it if you want to change the color from the default. */
	public Color getTextColorOfCell(int ic, boolean selected){
		if (datas ==null || ic<0 || ic>= datas.size())
			return null;
		CharacterData data =((CharacterData)datas.elementAt(ic));
		if (data!=null && !data.isUserVisible())
			return Color.gray;
		return null;
	}

}

