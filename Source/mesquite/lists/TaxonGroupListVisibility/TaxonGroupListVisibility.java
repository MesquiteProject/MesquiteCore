package mesquite.lists.TaxonGroupListVisibility;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Shape;

import mesquite.lib.*;
import mesquite.lib.characters.CharInclusionSet;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.table.MesquiteTable;
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
	public boolean arrowTouchInRow(int ic, boolean doubleClick){ //so assistant can do something in response to arrow touch; return true if the event is to stop there, i.e. be intercepted
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
		return "Show on Maps";
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;  
	}
	public void setTableAndObject(MesquiteTable table, Object object) {
		this.table = table;
		
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Taxon Group Visibility in Maps";
	}
	public String getExplanation() {
		return "Shows whether or not a taxon group is shown on a map." ;
	}

}
