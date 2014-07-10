package mesquite.lists.TaxonGroupListColor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Shape;

import mesquite.lib.*;
import mesquite.lib.characters.CharInclusionSet;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.table.MesquiteTable;
import mesquite.lists.lib.*;

/* ======================================================================== */
public class TaxonGroupListColor extends TaxonGroupListAssistant  {
	/*.................................................................................................................*/
	public String getName() {
		return "Color";
	}
	public String getExplanation() {
		return "Shows color assigned to taxon group." ;
	}

	CharacterData data=null;
	MesquiteTable table = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
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
	/** Gets background color for cell for row ic.  Override it if you want to change the color from the default. */
	public Color getBackgroundColorOfCell(int ic, boolean selected){
		
		TaxaGroup tg = getTaxonGroup(ic);
		if (tg!=null){
			return tg.getColor();

		}
		return null;

	}

	/*.................................................................................................................*/
	public boolean arrowTouchInRow(int ic){ //so assistant can do something in response to arrow touch; return true if the event is to stop there, i.e. be intercepted
		TaxaGroup tg = getTaxonGroup(ic);
		if (tg!=null){
			tg.editMe();
			parametersChanged();
			return true;
		}

		return false;
	}

	public String getWidestString(){
		return "888888";
	}
	/*.................................................................................................................*/
	public String getTitle() {
		return "Color";
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
	public void setTableAndObject(MesquiteTable table, Object object) {
		this.table = table;
		
	}
	public String getStringForRow(int ic) {
		return "";
	}


}
