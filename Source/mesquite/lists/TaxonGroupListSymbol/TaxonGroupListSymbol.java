/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lists.TaxonGroupListSymbol;

import java.awt.Button;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Container;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Shape;
import java.awt.event.*;

import javax.swing.JLabel;

import mesquite.lib.*;
import mesquite.lib.characters.CharInclusionSet;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.taxa.TaxaGroup;
import mesquite.lib.taxa.TaxaGroupVector;
import mesquite.lib.ui.MesquiteSymbol;
import mesquite.lib.ui.SymbolsVector;
import mesquite.lists.lib.*;

/* ======================================================================== */
public class TaxonGroupListSymbol extends TaxonGroupListAssistant   {
	CharacterData data=null;
	MesquiteTable table = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addMenuItem("Set Symbol...", makeCommand("setSymbol", this));
		return true;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Taxon Group Symbol";
	}
	public String getExplanation() {
		return "Shows symbol assigned to taxon group." ;
	}


	public void setTableAndData(MesquiteTable table, CharacterData data){
		//if (this.data !=null)
		//	this.data.removeListener(this);
		this.data = data;
		//data.addListener(this);
		this.table = table;
	}
	/*.................................................................................................................*/
	static final String chooseTemplate = "Choose Symbol";
	MesquiteSymbol newSymbol = null;
	MesquiteSymbol currentDialogSymbol = null;
	String symbolOptionsButtonName = "Symbol Options...";
	Button symbolOptionsButton;
	Choice symbolsPopUp=null;
	SymbolsVector symVector=null;
	JLabel symbolLabel;

	/*.................................................................................................................*/
	public boolean queryOptions(MesquiteSymbol oldSymbol) {
		if (!okToInteractWithUser(CAN_PROCEED_ANYWAY, "Querying Options")) 
			return true;
		GroupSymbolsDialog symbolsDialog = new GroupSymbolsDialog(getProject(), containerOfModule(), "Symbol Options", "", oldSymbol);
		symbolsDialog.completeAndShowDialog();
		String name = symbolsDialog.getName();
		boolean ok = symbolsDialog.query()==0;
		if (ok) {
			newSymbol = symbolsDialog.getSymbol();
		}
		symbolsDialog.dispose();
		if (!ok)
			return false;
		return true;
	}

	/*.................................................................................................................*/
	public void specifySymbol(int ic) {
		TaxaGroup tg = getTaxonGroup(ic);
		if (tg!=null){
			MesquiteSymbol oldSymbol = tg.getSymbol();
			if (queryOptions(oldSymbol)){
				MesquiteSymbol groupSymbol = newSymbol.cloneMethod();
				groupSymbol.setSize(oldSymbol.getSize());
				tg.setSymbol(groupSymbol);
				tg.setColor(tg.getColor());
				if (table != null)
					table.repaintAll();
				parametersChanged();
			}
		}
	}


	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the symbol", null, commandName, "setSymbol")) {
			String newSymbolName = parser.getFirstToken(arguments);
			if (StringUtil.blank(newSymbolName)){
				TaxaGroupVector groups = (TaxaGroupVector)getProject().getFileElement(TaxaGroupVector.class, 0);
				if (groups!=null  && table != null) {
					String oldSymbolName = null;
					MesquiteSymbol oldSymbol = null;
					boolean variable = false;
					for (int i = 0; i< groups.size(); i++){
						if (groups.getSelected(i) || table.isRowSelected(i)){
							TaxaGroup tg = getTaxonGroup(i);
							if (tg!=null){
								MesquiteSymbol symbol = tg.getSymbol();
								if (symbol!=null){
									String symbolName = symbol.getName();
									if (oldSymbolName!=null && !oldSymbolName.equals(symbolName))
										variable=true;
									oldSymbolName=symbolName;
									oldSymbol = symbol.cloneMethod();
								}
							}
						}
					}
					if (variable==true)
						oldSymbol=null;
					if (queryOptions(oldSymbol)){
						for (int i = 0; i< groups.size(); i++){
							if (groups.getSelected(i) || table.isRowSelected(i)){
								TaxaGroup tg = getTaxonGroup(i);
								if (tg!=null){
									MesquiteSymbol symbol = tg.getSymbol();
									MesquiteSymbol groupSymbol = newSymbol.cloneMethod();
									if (symbol!=null)
										groupSymbol.setSize(symbol.getSize());
									tg.setSymbol(groupSymbol);
									tg.setColor(tg.getColor());
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
			g.setColor(Color.lightGray);
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
				symbol.drawSymbol(g,x+w/2-size/2,y+h/2-size/2+3,size,size,false);
			}

		}
	}
	public String getStringForRow(int ic) {
		TaxaGroup tg = getTaxonGroup(ic);
		if (tg!=null){
			MesquiteSymbol symbol = tg.getSymbol();
			if (symbol!=null)
				return ""+symbol.getName();
		}
		return "";
	}

	/*.................................................................................................................*/
	public boolean arrowTouchInRow(Graphics g, int ic,  int x, int y, boolean doubleClick, int modifiers){ //so assistant can do something in response to arrow touch; return true if the event is to stop there, i.e. be intercepted
		if (ic>=0 && doubleClick) {
			specifySymbol(ic);
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
		return false;  
	}
	public void setTableAndObject(MesquiteTable table, Object object) {
		this.table = table;

	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 300;  
	}


}
