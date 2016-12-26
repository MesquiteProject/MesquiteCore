/* Mesquite source code.  Copyright 2001 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lists.CharGroupListColor;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Shape;

import javax.swing.JColorChooser;
import javax.swing.JFrame;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.table.MesquiteTable;
import mesquite.lists.lib.*;

/* ======================================================================== */
public class CharGroupListColor extends CharGroupListAssistant  {
	CharacterData data=null;
	MesquiteTable table = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addMenuItem("Set Color...", makeCommand("setColor", this));
		return true;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Character Group Color in List";
	}
	public String getExplanation() {
		return "Shows color assigned to character group." ;
	}


	public void setTableAndData(MesquiteTable table, CharacterData data){
		//if (this.data !=null)
		//	this.data.removeListener(this);
		this.data = data;
		//data.addListener(this);
		this.table = table;
	}
	public void drawInCell(int ic, Graphics g, int x, int y,  int w, int h, boolean selected){
		Color c = getBackgroundColorOfCell(ic,selected);
		Color oldColor = g.getColor();
		Color highlightColor = Color.black;
		if (c!=null)
			highlightColor = ColorDistribution.getContrastingTextColor(c);
		if (c!=null){ 
			g.setColor(c);
			g.fillRect(x+1, y+1, w-1, h-1);
		}
		if (selected) {
			g.setColor(highlightColor);
			g.drawRect(x+1, y+1, w-2, h-2);
			g.drawRect(x+2, y+2, w-4,h-4);
		}
		g.setColor(oldColor);
	}
	/*.................................................................................................................*/
	CharactersGroup getCharacterGroup(int ic){
		CharactersGroupVector groups = (CharactersGroupVector)getProject().getFileElement(CharactersGroupVector.class, 0);
		if (groups!=null) {
			if (ic>=0 && ic<groups.size())
				return(CharactersGroup)groups.elementAt(ic);
		}
		return null;
	}
	/** Gets background color for cell for row ic.  Override it if you want to change the color from the default. */
	public Color getBackgroundColorOfCell(int ic, boolean selected){
		
		CharactersGroup tg = getCharacterGroup(ic);
		if (tg!=null){
			return tg.getColor();

		}
		return null;

	}
	Color newColor = null;
	/*.................................................................................................................*/
	public boolean chooseColor(Color oldColor){ //so assistant can do something in response to arrow touch; return true if the event is to stop there, i.e. be intercepted
		if (!okToInteractWithUser(CAN_PROCEED_ANYWAY, "Querying Options"))  
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
				CharactersGroupVector groups = (CharactersGroupVector)getProject().getFileElement(CharactersGroupVector.class, 0);
				if (groups!=null  && table != null) {
					Color oldColor = null;
					boolean variable = false;
					for (int i = 0; i< groups.size(); i++){
						if (groups.getSelected(i) || table.isRowSelected(i)){
							CharactersGroup cg = getCharacterGroup(i);
							if (cg!=null){
								Color color = cg.getColor();
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
								CharactersGroup cg = getCharacterGroup(i);
								if (cg!=null){
									cg.setColor(newColor);
									MesquiteSymbol symbol = cg.getSymbol();
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
	/*.................................................................................................................*/
	public void specifyColor (int ic) {
		CharactersGroup cg = getCharacterGroup(ic);
		if (cg!=null){
			Color oldColor = null;
			if (chooseColor(oldColor)){
				cg.setColor(newColor);
				if (table != null)
					table.repaintAll();
				parametersChanged();
			}
		}
	}
	/*.................................................................................................................*/
	public boolean arrowTouchInRow(Graphics g, int ic, int x, int y, boolean doubleClick, int modifiers){
		if (ic>=0 && doubleClick) {
			specifyColor(ic);
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
		return false;  
	}
	public void setTableAndObject(MesquiteTable table, Object object) {
		this.table = table;
		
	}
	public String getStringForRow(int ic) {
		CharactersGroup cg = getCharacterGroup(ic);
		if (cg!=null){
			return cg.getColor().toString();
		}
		return "";
	}


}
