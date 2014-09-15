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


/* ======================================================================== */
/** Specifications to later make a submenu.*/
public class MesquiteSubmenuSpec extends MesquiteMenuSpec{ 
	//public String submenuName;
	MesquiteString selected;
	public static final int SHOW_SUBMENU =0;
	public static final int ONEMENUITEM_ZERODISABLE = 1;
	public static final int ONEDISABLE_ZERODISABLE = 2;
	public static final int ONESUBMENU_ZERODISABLE = 3;
	int beh = ONESUBMENU_ZERODISABLE;
	public static CommandChecker checkerMS = null;
	
	public MesquiteSubmenuSpec(MesquiteMenuSpec whichMenu, String submenuName,  MesquiteModule ownerModule) {
		super(whichMenu,  submenuName,  ownerModule);
	//	this.submenuName = submenuName;
	}
	
	public static MesquiteSubmenuSpec getMSSSpec(MesquiteMenuSpec whichMenu, String itemName, MesquiteModule ownerModule){
		if (checkerMS!=null) {
			checkerMS.registerMenuItem(ownerModule, itemName, null);
			return null;
		}
		else {
			ownerModule.checkMISVector();
			MesquiteSubmenuSpec mmis = new MesquiteSubmenuSpec(whichMenu, itemName, ownerModule);
			ownerModule.getMenuItemSpecs().addElement(mmis, false);
			return mmis;
		}
	}
	public String getSubmenuName(){
		return getCurrentItemName();
	}
	
	public void setBehaviorIfNoChoice(int beh) {
		this.beh = beh;
	}
	public int getBehaviorIfNoChoice() {
		return beh;
	}
	
	public void setSelected(MesquiteString selected) {
		this.selected = selected;
	}
	public MesquiteString getSelected() {
		return selected;
	}
}

