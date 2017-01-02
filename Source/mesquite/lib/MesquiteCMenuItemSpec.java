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
/**Specifications to later make check menu items.*/

public class MesquiteCMenuItemSpec extends MesquiteMenuItemSpec{
	MesquiteBoolean checkBoolean;
	public static CommandChecker checkerMCMI = null;
	public MesquiteCMenuItemSpec(MesquiteMenuSpec whichMenu, String itemName, MesquiteModule ownerModule, MesquiteCommand command, MesquiteBoolean checkBoolean) {
		super(whichMenu, itemName, ownerModule, command);
		this.checkBoolean = checkBoolean;
		if (checkBoolean!=null)
			checkBoolean.bindMenuItem(this);
	}

	//This is constructor used, e.g. when submenu of candidate employees not requested
	public MesquiteCMenuItemSpec(MesquiteMenuSpec whichMenu, String itemName, MesquiteModule ownerModule, MesquiteCommand command, String argument, MesquiteBoolean checkBoolean) {
		super(whichMenu,  itemName,  ownerModule,  command);
		setArgument(argument);
		this.checkBoolean = checkBoolean;
		if (checkBoolean!=null)
			checkBoolean.bindMenuItem(this);
	}
	public static MesquiteCMenuItemSpec getMCMISpec(MesquiteMenuSpec whichMenu, String itemName, MesquiteModule ownerModule, MesquiteCommand command, MesquiteBoolean checkBoolean){
		if (checkerMCMI!=null) {
			checkerMCMI.registerMenuItem(ownerModule, itemName, command);
			return null;
		}
		else {
			ownerModule.checkMISVector();
			MesquiteCMenuItemSpec mmis = new MesquiteCMenuItemSpec(whichMenu, itemName, ownerModule, command, checkBoolean);
			ownerModule.getMenuItemSpecs().addElement(mmis, false);
			return mmis;
		}
	}
	public void dispose(){
		releaseBoolean();
		super.dispose();
	}
	public void releaseBoolean() {
		if (checkBoolean!=null)
			checkBoolean.releaseMenuItem();
	}
	public MesquiteBoolean getBoolean() {
		return checkBoolean;
	}
}


