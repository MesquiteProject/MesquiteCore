package mesquite.lists.TaxonGroupListColor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Shape;

import javax.swing.*;

import mesquite.lib.*;
import mesquite.lib.characters.CharInclusionSet;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.table.MesquiteTable;
import mesquite.lists.lib.*;

/* ======================================================================== */
public class TaxonGroupListColor extends TaxonGroupListAssistant  {
	CharacterData data=null;
	MesquiteTable table = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addMenuItem("Set Color...", makeCommand("setColor", this));
		return true;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Taxon Group Colors";
	}
	public String getExplanation() {
		return "Shows color assigned to taxon group." ;
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
			Color color = tg.getColor();
			return color;
		}
		return null;

	}
	public void drawInCell(int ic, Graphics g, int x, int y,  int w, int h, boolean selected){
		Color c = getBackgroundColorOfCell(ic,selected);
		g.setColor(c);
		g.fillRect(x+1, y+1, w-1, h-1);
		if (selected) {
			g.setColor(ColorDistribution.getContrasting(c));
			g.drawRect(x+1, y+1, w-2, h-2);
			g.drawRect(x+2, y+2, w-4,h-4);
		}
	}
	Color newColor = null;
	/*.................................................................................................................*/
	public boolean chooseColor(Color oldColor){ //so assistant can do something in response to arrow touch; return true if the event is to stop there, i.e. be intercepted
		if (!okToInteractWithUser(CAN_PROCEED_ANYWAY, "Querying Options"))  //Debugg.println needs to check that options set well enough to proceed anyway
			return true;
		JFrame guiFrame = new JFrame();
		newColor = JColorChooser.showDialog(guiFrame, "Pick a Color", oldColor);
		if (newColor!=null){
			return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the color", null, commandName, "setColor")) {
			String newColorText = parser.getFirstToken(arguments);
			if (StringUtil.blank(newColorText)){
				TaxaGroupVector groups = (TaxaGroupVector)getProject().getFileElement(TaxaGroupVector.class, 0);
				if (groups!=null  && table != null) {
					Color oldColor = null;
					boolean variable = false;
					for (int i = 0; i< groups.size(); i++){
						if (groups.getSelected(i) || table.isRowSelected(i)){
							TaxaGroup tg = getTaxonGroup(i);
							if (tg!=null){
								Color color = tg.getColor();
								if (color!=null){
									if (ColorDistribution.equalColors(color, oldColor))
										variable=true;
									oldColor = color;
								}
							}
						}
					}
					if (variable==true)
						oldColor=null;
					if (chooseColor(oldColor)){
						for (int i = 0; i< groups.size(); i++){
							if (groups.getSelected(i) || table.isRowSelected(i)){
								TaxaGroup tg = getTaxonGroup(i);
								if (tg!=null){
									tg.setColor(newColor);
									MesquiteSymbol symbol = tg.getSymbol();
									if (symbol!=null)
										symbol.setColor(newColor);
								}
							}
						}
						if (table != null)
							table.repaintAll();
						parametersChanged();
					}

				}
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*
	public boolean arrowTouchInRow(int ic){ //so assistant can do something in response to arrow touch; return true if the event is to stop there, i.e. be intercepted
		TaxaGroup tg = getTaxonGroup(ic);
		if (tg!=null){
			JFrame guiFrame = new JFrame();
			Color selectedColor = JColorChooser.showDialog(guiFrame, "Pick a Color", tg.getColor());
			if (selectedColor!=null){
				tg.setColor(selectedColor);
				MesquiteSymbol symbol = tg.getSymbol();
				if (symbol!=null)
					symbol.setColor(selectedColor);
			//tg.editMe();
			parametersChanged();
			}
			return true;
		}

		return false;
	}
	/*.................................................................................................................*/

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

	/** Returns whether to use the string from getStringForRow; otherwise call drawInCell*/
	public boolean useString(int ic){
		return false;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;  
	}
	public void setTableAndObject(MesquiteTable table, Object object) {
		this.table = table;

	}
	public String getStringForRow(int ic) {
		TaxaGroup tg = getTaxonGroup(ic);
		if (tg!=null){
			return tg.getColor().toString();
		}
		return "";
	}


}
