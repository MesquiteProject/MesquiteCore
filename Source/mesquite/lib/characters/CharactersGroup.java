/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.characters; 

import mesquite.lib.GroupLabel;

	
/* ======================================================================== */
/** A group to which characters may belong.*/
public class CharactersGroup extends GroupLabel {
	public static boolean supportsSymbols() {
		return false;
	}

	/*.................................................................................................................*
	public void editMe(){
			GroupDialog d = new GroupDialog(getProject(),getProject().getCoordinatorModule().containerOfModule(), "Edit Character Group", getName(), getColor(), getSymbol(), supportsSymbols());
			d.completeAndShowDialog();
			String name = d.getName();
			boolean ok = d.query()==0;
			Color c = d.getColor();
			d.dispose();
			if (!ok)
				return;


			if (!StringUtil.blank(name)) {
				setName(name);
				setColor(c);
	 		//	data.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED)); //TODO: bogus! should notify via specs not data???
				notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
			}
		
		
	}
	*/
}


