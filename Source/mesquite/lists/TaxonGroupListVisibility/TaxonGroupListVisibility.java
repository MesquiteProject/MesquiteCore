/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lists.TaxonGroupListVisibility;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Shape;

import mesquite.lib.*;
import mesquite.lib.characters.CharInclusionSet;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.taxa.TaxaGroup;
import mesquite.lib.taxa.TaxaGroupVector;
import mesquite.lists.lib.*;

/* ======================================================================== */
public class TaxonGroupListVisibility extends TaxonGroupListAssistant  {
	CharacterData data=null;
	MesquiteTable table = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addMenuItem("Show in Maps", makeCommand("showInMaps", this));
		addMenuItem("Hide in Maps", makeCommand("hideInMaps", this));
		return true;
	}


	public void setTableAndData(MesquiteTable table, CharacterData data){
		//if (this.data !=null)
		//	this.data.removeListener(this);
		this.data = data;
		//data.addListener(this);
		this.table = table;
	}
	/*.................................................................................................................*/
	TaxaGroup getTaxonGroup(int ic){
		TaxaGroupVector groups = (TaxaGroupVector)getProject().getFileElement(TaxaGroupVector.class, 0);
		if (groups!=null) {
			if (ic>=0 && ic<groups.size())
				return(TaxaGroup)groups.elementAt(ic);
		}
		return null;
	}
		/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the visibility of the group in maps", null, commandName, "showInMaps")) {
			TaxaGroupVector groups = (TaxaGroupVector)getProject().getFileElement(TaxaGroupVector.class, 0);
			for (int i = 0; i< groups.size(); i++){
				if (groups.getSelected(i) || table.isRowSelected(i)){
					TaxaGroup tg = getTaxonGroup(i);
					if (tg!=null){
						tg.setVisible(true);
					}
				}
			}
			if (table != null)
				table.repaintAll();
		}
		else
			if (checker.compare(this.getClass(), "Sets the group to be hidden in maps", null, commandName, "hideInMaps")) {
				TaxaGroupVector groups = (TaxaGroupVector)getProject().getFileElement(TaxaGroupVector.class, 0);
				for (int i = 0; i< groups.size(); i++){
					if (groups.getSelected(i) || table.isRowSelected(i)){
						TaxaGroup tg = getTaxonGroup(i);
						if (tg!=null){
							tg.setVisible(false);
						}
					}
				}
				if (table != null)
					table.repaintAll();
			}
			else
				return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/

	public String getStringForRow(int ic) {
		TaxaGroup tg = getTaxonGroup(ic);
		if (tg!=null){
			if (tg.isVisible())
				return "show";
			else 
				return "HIDE";
		}
		return "";
	}

	/*.................................................................................................................*
	public boolean arrowTouchInRow(Graphics g, int ic,  int x, int y, boolean doubleClick, int modifiers){ //so assistant can do something in response to arrow touch; return true if the event is to stop there, i.e. be intercepted
		TaxaGroup tg = getTaxonGroup(ic);
		if (tg!=null){
			tg.editMe();
			parametersChanged();
			return true;
		}

		return false;
	}

	/*.................................................................................................................*/

	public String getWidestString(){
		return "888888888888";
	}
	/*.................................................................................................................*/
	public String getTitle() {
		return "Show on Taxa Maps";
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 300;  
	}

	public void setTableAndObject(MesquiteTable table, Object object) {
		this.table = table;
		
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Taxon Group Visibility in Taxa Maps";
	}
	public String getExplanation() {
		return "Shows whether or not a taxon group is shown on a taxa map." ;
	}

}
