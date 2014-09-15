/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.SelSummaryStrip;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.categ.lib.*;
import mesquite.cont.lib.*;


/* ======================================================================== */
public class SelSummaryStrip extends DataWindowAssistantID {
	Ledge panel;
	MesquiteBoolean showStrip;
	boolean shown;
	CharacterData data = null;
	MesquiteTable table = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		showStrip = new MesquiteBoolean(false);
		shown = false;
		addCheckMenuItem(null, "Show Selection Summary Strip", makeCommand("showStrip", this), showStrip);
		return true;
	}
	/** Called to alter data in all cells*/
	public void setTableAndData(MesquiteTable table, CharacterData cData){
		if (data != null)
			data.removeListener(this);
		data = cData;
		data.addListener(this);
		this.table = table;
		resetLedge();

	}
	public void changed(Object caller, Object obj, Notification notification){
		if (notification == null || obj != data || caller == this)
			return;
		resetMessage();

	}
	public void resetLedge(){
		if (showStrip.getValue() && !shown){  //need to show
			MesquiteWindow f = containerOfModule();
			if (f instanceof MesquiteWindow){
				shown = true;
				if (panel == null)
					panel = new Ledge(this);
				((MesquiteWindow)f).addLedgePanel(panel, 22);
				panel.setVisible(true);
				panel.setMessage("");
				resetMessage();
			}
		}
		else if (!showStrip.getValue() && shown && panel != null){ //need to hide
			MesquiteWindow f = containerOfModule();
			if (f instanceof MesquiteWindow){
				shown = false;
				panel.setVisible(false);
				((MesquiteWindow)f).removeLedgePanel(panel);
			}
		}
	}
	private void resetMessage(){
		if (panel == null)
			return;
		panel.setMessage(getMessage());

	}


	private String getMessage(){
		if (!table.anythingSelected())
			return "Nothing selected";

		String result = "";
		if (table.anyRowSelected()){
			int numRows = table.numRowsSelected();
			if (numRows == 1)
				result += "1 Taxon selected.  ";
			else
				result += numRows + " Taxa selected.  ";
		}
		if (table.anyRowSelected()){
			int numColumns = table.numColumnsSelected();
			if (numColumns == 1)
				result += "1 Character selected.  ";
			else
				result += numColumns + " Characters selected.  ";
		}

		if (table.anyCellSelectedAnyWay()){
			int numCells = table.numCellsSelectedAnyWay();
			if (numCells == 1)
				result += "1 Cells selected.  ";
			else
				result += numCells + " Cells selected.  ";
		}
		if (data instanceof CategoricalData){
			
			int[] count = new int[CategoricalState.maxCategoricalState+1];
			CategoricalData catData = (CategoricalData)data;
			int maxState = -1;
			for (int ic= 0; ic< table.numColumnsTotal; ic++)
				for (int it= 0; it< table.numRowsTotal; it++)
					if (table.isCellSelectedAnyWay(ic, it)){
						long state = catData.getState(ic, it);
						state = state & CategoricalState.statesBitsMask;
						for (int i=0; i<=CategoricalState.maxCategoricalState && state != 0L; i++)

							if (CategoricalState.isElement(state, i)){
								state = CategoricalState.clearFromSet(state, i);
								count[i]++;
								if (i> maxState)
									maxState = i;
							}
					}
			
			if (maxState > -1){
				result += "Counts of state ";
				for (int i=0; i<=maxState; i++)
					if (count[i]>0)
						result += catData.getSymbol(i) + ": " + count[i] + "; ";
			}

		}
		else if (data instanceof ContinuousData){
			for (int ic= 0; ic< table.numColumnsTotal; ic++)
				for (int it= 0; it< table.numRowsTotal; it++)
					if (table.isCellSelectedAnyWay(ic, it)){
						//
					}
		}
		return result;
	}
	/*.................................................................................................................*/
	public void endJob() {
		if (data != null)
			data.removeListener(this);
		MesquiteWindow f = containerOfModule();
		if (f instanceof MesquiteWindow){
			((MesquiteWindow)f).removeLedgePanel(panel);
		}
		super.endJob();
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	void focusLost(){
		resetMessage();
	}
	public void focusInCell(int ic, int it){
		if (panel == null)
			return;
		/*if (ic >=0)
  			resetMessage(ic);
  		else*/
		resetMessage();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("showStrip " + showStrip.toOffOnString());
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Shows a strip with the reference for the character", null, commandName, "showStrip")) {
			showStrip.toggleValue(parser.getFirstToken(arguments));
			resetLedge();

		}

		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Selection Summary Strip";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 273;  
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Shows the strip at bottom of matrix with character reference." ;
	}
}
class Ledge extends MousePanel {
	TextArea message, title;
	SelSummaryStrip ownerModule;
	int titleWidth = 80;
	int ic = -1;
	public Ledge(SelSummaryStrip ownerModule){
		setLayout(null);
		this.ownerModule = ownerModule;
		/* */
		message = new TextArea(" ", 20, 2, TextArea.SCROLLBARS_NONE);
		message.setVisible(true);
		add(message);
		message.setBounds(titleWidth,0,getBounds().width-titleWidth, getBounds().height);
		message.setBackground(Color.white);



		title = new TextArea(" ", 20, 2, TextArea.SCROLLBARS_NONE);
		title.setVisible(true);
		add(title);
		title.setBounds(0,0,titleWidth, getBounds().height);
		title.setBackground(ColorDistribution.veryVeryLightGreen);
		title.setFont(new Font ("SanSerif", Font.PLAIN, 12));
		title.setText("Selected:");
		message.setFont(new Font ("SanSerif", Font.PLAIN, 12));
		/**/
	}
	public void setMessage(String s){

		if (StringUtil.stringsEqual(s, message.getText()))
			return;
		if (s == null)
			s = "";
		title.setText("Selected:");
		message.setText(s);
		message.repaint();

	}
	public String getMessage(){
		return message.getText();

	}
	public void setSize(int w, int h){
		message.setBounds(titleWidth,0,w-titleWidth, h);
		title.setBounds(0,0,titleWidth, h);
		super.setSize(w,h);
	}
	public void setBounds(int x, int y, int w, int h){
		message.setBounds(titleWidth,0,w-titleWidth, h);
		title.setBounds(0,0,titleWidth, h);
		super.setBounds(x, y, w,h);
	}
}


