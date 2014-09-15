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
import java.util.*;


/* ======================================================================== */
/** Specifications to later make a menu.*/
public class MesquiteMenuSpec extends MesquiteMenuItemSpec { 
	String label;
	Vector guests;
	boolean universalMenu;
	public MesquiteMenuSpec(MesquiteMenuSpec whichMenu, String menuName,  MesquiteModule ownerModule, boolean universalMenu) {
		super(whichMenu, menuName, ownerModule, null);
		this.label = menuName; 
		this.universalMenu = universalMenu;
	}
	public MesquiteMenuSpec(MesquiteMenuSpec whichMenu, String menuName,  MesquiteModule ownerModule) {
		super(whichMenu, menuName, ownerModule, null);
		this.label = menuName; 
	}
	boolean filterable = true;
	public boolean isFilterable() {
		return filterable;
	}
	public void setFilterable(boolean f) {
		filterable = f;
	}

	public String getLabel() {
		return label;
	}
	public boolean isUniversalMenu() {
		return universalMenu;
	}
	public void addGuestModule(MesquiteModule mb) {
		if (mb == null)
			return;
		if (guests == null)
			guests = new Vector();
		guests.addElement(mb);
	}
	public void removeGuestModule(MesquiteModule mb) {
		if (mb == null || guests == null)
			return;
		guests.removeElement(mb);
	}
	public boolean isGuestModule(MesquiteModule mb) {
		if (mb == null || guests == null)
			return false;
		return guests.indexOf(mb) >=0;
	}
	public Vector getGuests() {
		return guests;
	}
}

