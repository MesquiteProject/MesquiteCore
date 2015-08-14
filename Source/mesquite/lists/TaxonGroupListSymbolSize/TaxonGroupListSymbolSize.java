/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lists.TaxonGroupListSymbolSize;

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
public class TaxonGroupListSymbolSize extends TaxonGroupListAssistant  {
	CharacterData data=null;
	MesquiteTable table = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addMenuItem("Set Size...", makeCommand("setSize", this));
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
	/*.................................................................................................................*

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
		/*.................................................................................................................*/
	public void specifySymbolSize(int ic) {
		TaxaGroup tg = getTaxonGroup(ic);
		if (tg!=null){
			MesquiteSymbol symbol = tg.getSymbol();
			if (symbol!=null){
				int oldSize = symbol.getSize();
				int newSize = MesquiteInteger.queryInteger(containerOfModule(), "Symbol Size", "Symbol Size", oldSize, 1, 500, true);
				if (MesquiteInteger.isCombinable(newSize)){
					symbol.setSize(newSize);
					if (table != null)
						table.repaintAll();
					parametersChanged();
				}
			}
		}
	}

		/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the size of the symbol", null, commandName, "setSize")) {
			String size = parser.getFirstToken(arguments);
			if (StringUtil.blank(size)){
				TaxaGroupVector groups = (TaxaGroupVector)getProject().getFileElement(TaxaGroupVector.class, 0);
				if (groups!=null  && table != null) {
					int oldSize = -1;
					boolean variableSize = false;
					for (int i = 0; i< groups.size(); i++){
						if (groups.getSelected(i) || table.isRowSelected(i)){
							TaxaGroup tg = getTaxonGroup(i);
							if (tg!=null){
								MesquiteSymbol symbol = tg.getSymbol();
								if (symbol!=null){
									int symbolSize = symbol.getSize();
									if (oldSize>-1 && oldSize!=symbolSize)
										variableSize=true;
									oldSize=symbolSize;
								}
							}
						}
					}
					int newSize = MesquiteInteger.queryInteger(containerOfModule(), "Symbol Size", "Symbol Size", oldSize, 1, 500, true);
					if (MesquiteInteger.isCombinable(newSize)){
						for (int i = 0; i< groups.size(); i++){
							if (groups.getSelected(i) || table.isRowSelected(i)){
								TaxaGroup tg = getTaxonGroup(i);
								if (tg!=null){
									MesquiteSymbol symbol = tg.getSymbol();
									if (symbol!=null)
										symbol.setSize(newSize);
								}
							}
						}
						if (table != null)
							table.repaintAll();
					}

				}
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/

	public String getStringForRow(int ic) {
		TaxaGroup tg = getTaxonGroup(ic);
		if (tg!=null){
			MesquiteSymbol symbol = tg.getSymbol();
			if (symbol!=null)
				return ""+symbol.getSize();
		}
		return "";
	}

	/*.................................................................................................................*/
	public boolean arrowTouchInRow(int ic,  int x, int y, boolean doubleClick, int modifiers){ //so assistant can do something in response to arrow touch; return true if the event is to stop there, i.e. be intercepted
		if (ic>=0 && doubleClick) {
			specifySymbolSize(ic);
			return true;
		}
		return false;
	}

	/*.................................................................................................................*/

	public String getWidestString(){
		return "888888888";
	}
	/*.................................................................................................................*/
	public String getTitle() {
		return "Symbol SIze";
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
		return "Taxon Group Symbol Size";
	}
	public String getExplanation() {
		return "Shows size of symbol assigned to taxon group." ;
	}

}
