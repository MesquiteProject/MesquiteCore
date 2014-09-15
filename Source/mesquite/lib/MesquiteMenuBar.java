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

/* ======================================================================== */
/** A menu bar attached to a window.*/
public class MesquiteMenuBar extends MenuBar {
	MesquiteWindow ownerWindow;
	public static long totalCreated = 0;
	public MesquiteMenuBar(MesquiteWindow ownerWindow){
		super();
		this.ownerWindow=ownerWindow;
		totalCreated++;
	}
	
	public MesquiteWindow getOwnerWindow(){
		return ownerWindow;
	}
	boolean menuWithSameLabelExists(String label){
		for (int i=0; i<getMenuCount(); i++)
			if (getMenu(i).getLabel().equals(label))
				return true;
		return false;
	}
	public Menu add(Menu menu) {
		if (menu==null)
			return null;
		while (menuWithSameLabelExists(menu.getLabel()))
			menu.setLabel(menu.getLabel()+".");
		return super.add(menu);
	}
	public void disconnect() {
		ownerWindow = null;
	/*
		for (int i=0; i<getMenuCount(); i++){
			Menu menu = getMenu(i);
			if (menu instanceof MesquiteMenu)
				((MesquiteMenu)menu).recycle = true; //setting recycle flag so can be recycled
			for (int j=0; j<menu.getItemCount(); j++) {
				MenuItem item = menu.getItem(i);
				if (item instanceof MesquiteMenu)
					((MesquiteMenu)item).recycle = true; //setting recycle flag so can be recycled
			}
		}
		for (int i=getMenuCount()-1; i>=0; i--){
			Menu menu = getMenu(i);
			menu.removeAll();
			remove(menu);
		}
	//	removeAll();
	*/
	}
}


