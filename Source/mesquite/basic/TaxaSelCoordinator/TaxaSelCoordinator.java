/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.basic.TaxaSelCoordinator;

import mesquite.lists.lib.*;
import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class TaxaSelCoordinator extends TaxaSelectCoordinator {
	Taxa taxa=null;
	MesquiteTable table = null;
	boolean rows = true;
	Object dataCondition;
	MesquiteSubmenuSpec mss = null;
	MesquiteSubmenuSpec mssc = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addMenuItem("Select All Taxa", makeCommand("selectAll",  this));
		addMenuItem("Deselect All Taxa", makeCommand("deselectAll",  this));
		addMenuItem("Reverse taxon selection", makeCommand("reverse",  this));
		return true;
  	 }
	/*.................................................................................................................*/
  	 public void employeeQuit(MesquiteModule m){
  	 	iQuit();
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Selects all taxa", null, commandName, "selectAll")) {
			if (taxa !=null && table!=null) {
 				for (int i=0; i<taxa.getNumTaxa(); i++)
 					taxa.setSelected(i, true);
				taxa.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				table.repaintAll();
    	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Deselects all taxa", null, commandName, "deselectAll")) {
			if (taxa !=null && table!=null) {
 				for (int i=0; i<taxa.getNumTaxa(); i++)
 					taxa.setSelected(i, false);
				taxa.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				table.repaintAll();
    	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Reverses the taxon selection", null, commandName, "reverse")) {
			if (taxa !=null && table!=null) {
 				for (int i=0; i<taxa.getNumTaxa(); i++)
 					taxa.setSelected(i, !taxa.getSelected(i));
				taxa.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				table.repaintAll();
    	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Selects taxa in the set", null, commandName, "selectSet")) {
			if (taxa !=null && table!=null) {
				int whichSet = MesquiteInteger.fromString(parser.getFirstToken(arguments));
	 			SpecsSetVector ssv = taxa.getSpecSetsVector(TaxaSelectionSet.class);
 				if (ssv!=null) {
 					TaxaSelectionSet chosen;
  	 				if (!MesquiteInteger.isCombinable(whichSet))
	 					chosen = (TaxaSelectionSet)ListDialog.queryList(containerOfModule(), "Set taxa selection", "Select taxa of which set?", MesquiteString.helpString,ssv, 0);
    	 				else 
	    	 				chosen = (TaxaSelectionSet)ssv.getSpecsSet(whichSet);
	    	 			if (chosen!=null){
	    	 				for (int i=0; i<taxa.getNumTaxa(); i++)
	    	 					taxa.setSelected(i, chosen.isBitOn(i));
						taxa.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
	 					table.repaintAll();
	    	 				return chosen;
	    	 			}
    	 			}
    	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Hires taxon selection module to operate on the taxa", "[name of module]", commandName, "doSelectTaxa")) {
   	 		if (table!=null && taxa !=null){
	    	 		TaxonSelector tda= (TaxonSelector)hireNamedEmployee(TaxonSelector.class, arguments);
				if (tda!=null) {
					tda.selectTaxa(taxa);
	 	   			table.repaintAll();
					if (!tda.pleaseLeaveMeOn())
						fireEmployee(tda);
				}
			}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
	/** passes which object changed, along with optional code number (type of change) and integers (e.g. which character)*/
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification);
		if (obj == taxa && (code == MesquiteListener.ASSOCIATED_CHANGED || code == AssociableWithSpecs.SPECSSET_CHANGED)){
			menuReset();
			resetContainingMenuBar();
		}
	}
	private void menuReset(){
		if (mss!=null)
			deleteMenuItem(mss);
		if (mssc!=null)
			deleteMenuItem(mssc);
		mss = addSubmenu(null, "Taxa Set", makeCommand("selectSet",  this), taxa.getSpecSetsVector(TaxaSelectionSet.class));
		mssc = addSubmenu(null, "Select Taxa", MesquiteModule.makeCommand("doSelectTaxa",  this));
		mssc.setList(TaxonSelector.class);
	}
	/*.................................................................................................................*/
	public void setTableAndObject(MesquiteTable table, Object obj, boolean rows){
		if (obj==null || !(obj instanceof Taxa))
			return;
		if (taxa != this.taxa && this.taxa != null)
			this.taxa.removeListener(this);
		this.taxa = (Taxa)obj;
		this.taxa.addListener(this);
		
		this.table = table;
		this.rows = rows;
		menuReset();
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Taxon selection coordinator";
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Coordinates taxon selection." ;
   	 }
}

