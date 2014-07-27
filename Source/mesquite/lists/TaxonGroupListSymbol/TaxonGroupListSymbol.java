package mesquite.lists.TaxonGroupListSymbol;

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
public class TaxonGroupListSymbol extends TaxonGroupListAssistant  {
	/*.................................................................................................................*/
	public String getName() {
		return "Taxon Group Symbol in List";
	}
	public String getExplanation() {
		return "Shows symbol assigned to taxon group." ;
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
	/** Gets background color for cell for row ic.  Override it if you want to change the color from the default. *
	public Color getBackgroundColorOfCell(int ic, boolean selected){
		
		TaxaGroup tg = getTaxonGroup(ic);
		if (tg!=null){
			return tg.getColor();

		}
		return null;

	}
	/*.................................................................................................................*/

	public void drawInCell(int ic, Graphics g, int x, int y,  int w, int h, boolean selected){
		if (selected) {
			g.setColor(Color.gray);
		} else
			g.setColor(Color.white);
		g.fillRect(x+1, y+1, w-1, h-1);
		MesquiteSymbol symbol = null;
		TaxaGroup tg = getTaxonGroup(ic);
		boolean colored = false;
		if (tg!=null) {
				symbol = tg.getSymbol();
		if (symbol!=null) {
			int size = symbol.getSize();
			if (w-3<size)
				size=w-3;
			if (h/2-3<size)
				size=h/2-3;
			symbol.drawSymbol(g,x+w/2-size/2,y+h/2-size/2+3,size,size,true);
		}
			
		}
	}
	public String getStringForRow(int ic) {
		TaxaGroup tg = getTaxonGroup(ic);
		if (tg!=null){
			return tg.getSymbol().getName();
		}
		return "";
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

	/** Returns whether to use the string from getStringForRow; otherwise call drawInCell*/
	public boolean useString(int ic){
		return false;
	}

	public String getWidestString(){
		return "888888";
	}
	/*.................................................................................................................*/
	public String getTitle() {
		return "Symbol";
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


}
