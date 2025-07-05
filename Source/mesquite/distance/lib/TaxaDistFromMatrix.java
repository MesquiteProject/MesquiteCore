/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.distance.lib;
/*~~  */

import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteThread;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.MesquiteSubmenuSpec;

/* ======================================================================== */

public abstract class TaxaDistFromMatrix extends MesquiteModule {
	protected MesquiteSubmenuSpec distParamSubmenu;
	/*.................................................................................................................*/
	public boolean superStartJob(String arguments, Object condition, boolean hiredByName) {
		distParamSubmenu = addSubmenu(null, "Distance Parameters");
		if (!getDistanceOptions())  //QZ: WAYNECHECK4.01:  This is called BEFORE startjob for the module is called, which means some variables are not yet initialized.
			return false;
		return true;
  	 }	 
  	 public Class getDutyClass() {
   	 	return TaxaDistFromMatrix.class;
   	 }
	
   	 public String[] getDefaultModule() {
   	 	return new String[] {"#UncorrectedDistance"};
   	 }

	public abstract TaxaDistance getTaxaDistance(Taxa taxa, MCharactersDistribution charMatrix);

	public boolean optionsAdded() {
		return false;
	}
	public void addOptions(ExtensibleDialog dialog) {
	}
	public void processOptions(ExtensibleDialog dialog) {
	}
	
	public boolean getDistanceOptions() {
		if (MesquiteThread.isScripting() || !optionsAdded())
			return true;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Distance Options",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		dialog.addLabel("Distance Options for " + getName());

		addOptions(dialog);

		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			processOptions(dialog);
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0) ;

	}

	
	 public Class getRequiredStateClass(){
		return null;
	}

   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(Taxa taxa){}

}
