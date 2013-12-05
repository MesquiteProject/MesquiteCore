/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.categ.StateNamesStrip;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.categ.lib.*;


/* ======================================================================== */
public class StateNamesStrip extends CategDataEditorInit {
	CategoricalData data;
	Ledge panel;
	MesquiteBoolean showStrip;
	boolean shown;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		showStrip = new MesquiteBoolean(false);
		shown = false;
		addCheckMenuItem(null, "Show State Names Strip", makeCommand("showStrip", this), showStrip);
		return true;
	}
   	/** Called to alter data in all cells*/
   	public void setTableAndData(MesquiteTable table, CharacterData data){
			this.data = (CategoricalData)data;
			if (data.getClass() != CategoricalData.class)
				return;
			resetLedge();
			
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
	/*.................................................................................................................*/
 	public void endJob() {
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
   	public void focusInCell(int ic, int it){
  		if (panel == null)
  			return;
   		panel.setMessage(statesExplanation(ic, it));
   	}
	/*...............................................................................................................*/
	private String statesExplanation(int column,int row){
		if (data == null || column <0 || column >= data.getNumChars())
			return "";
		
		String s = Integer.toString(column+1) + ". " + data.getCharacterName(column);
		if (data.getClass() == CategoricalData.class){
			s += ": ";
			CategoricalData cData = (CategoricalData)data;
			long state = cData.getState(column, row);
			for (int i = 0; i<=CategoricalState.maxCategoricalState; i++)
				if (cData.hasStateName(column, i)) {
					s +=  "(" + cData.getSymbol(i) + ")";
					if (CategoricalState.isElement(state, i))
						s += "*";
					s += " " + cData.getStateName(column, i);
					s +="; ";
				}
		}
		else if (data instanceof DNAData)
			s = "  Ambiguity codes: R-AG, Y-CT, M-AC, W-AT, S-CG, Y-CT, K-GT, V-ACG, D-AGT, H-ACT, B-CGT, N-ACGT";
		return s;
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot();
  	 	temp.addLine("showStrip " + showStrip.toOffOnString());
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Shows a strip with state names", null, commandName, "showStrip")) {
     	 		showStrip.toggleValue(parser.getFirstToken(arguments));
     	 		resetLedge();
   	 		
    	 	}

    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "State Names Strip";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Shows the strip at bottom of matrix with state names." ;
   	 }
}
class Ledge extends MousePanel {
	TextArea message;
	StateNamesStrip ownerModule;
	public Ledge(StateNamesStrip ownerModule){
		setLayout(null);
		this.ownerModule = ownerModule;
		/* */
		message = new TextArea(" ", 20, 2, TextArea.SCROLLBARS_NONE);
		message.setVisible(true);
		add(message);
		message.setBounds(0,0,getBounds().width, getBounds().height);
		message.setBackground(ColorTheme.getContentBackground());
		message.setFont(new Font ("SanSerif", Font.PLAIN, 12));
		/**/
	}
	public void setMessage(String s){
		if (StringUtil.stringsEqual(s, message.getText()))
			return;
		message.setText(s);
		
	}
	public void setSize(int w, int h){
		message.setBounds(0,0,w, h);
		super.setSize(w,h);
	}
	public void setBounds(int x, int y, int w, int h){
		message.setBounds(0,0,w, h);
		super.setBounds(x, y, w,h);
	}
}


