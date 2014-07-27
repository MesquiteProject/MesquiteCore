package mesquite.lists.TaxonGroupListSymbol;

import java.awt.Button;
import java.awt.Choice;
import java.awt.Color;
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
		if (!okToInteractWithUser(CAN_PROCEED_ANYWAY, "Querying Options"))  //Debugg.println needs to check that options set well enough to proceed anyway
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
								String symbolName = symbol.getName();
								if (symbol!=null){
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
	public boolean arrowTouchInRow(int ic){ //so assistant can do something in response to arrow touch; return true if the event is to stop there, i.e. be intercepted
		/*TaxaGroup tg = getTaxonGroup(ic);
		if (tg!=null){
			tg.editMe();
			parametersChanged();
			return true;
		}
		 */
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
