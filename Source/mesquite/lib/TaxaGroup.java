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
import java.math.*;

import mesquite.lists.lib.GroupDialog;


/* ======================================================================== */
/** DOCUMENT.*/
public class TaxaGroup extends GroupLabel {
	Taxa taxa;
	public TaxaGroup(Taxa taxa) {
		this.taxa = taxa;
	}
	public TaxaGroup() {
	}
	public static boolean supportsSymbols() {
		return true;
	}
	/*.................................................................................................................*/
	public static TaxaGroup makeGroup(MesquiteModule mod, String name, Taxa taxa, TaxaGroupVector groups){
		TaxaGroup group = new TaxaGroup(taxa);
		group.setName(name);
		group.addToFile(taxa.getFile(), mod.getProject(), null);
		if (groups.indexOf(group)<0) 
			groups.addElement(group, false);
		return group;
	}
	/*.................................................................................................................*/
	public void editMe(){
		GroupDialog d = new GroupDialog(getProject(),getProject().getCoordinatorModule().containerOfModule(), "Edit Taxa Group", getName(), getColor(), getSymbol(), supportsSymbols());

		d.completeAndShowDialog();
		name = d.getName();
		boolean ok = d.query()==0;
		Color c = d.getColor();
		MesquiteSymbol symbol = d.getSymbol();
		d.dispose();
		if (!ok)
			return;

		if (!StringUtil.blank(name)) {
			setName(name);
			setColor(c);
			if (symbol!=null)
				setSymbol(symbol);
			if (taxa!=null)
				taxa.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  
			notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
		}

	}

}

