/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.CharReferenceStrip;

import java.awt.Color;
import java.awt.Font;
import java.awt.TextArea;

import mesquite.lib.CommandChecker;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteFile;
import mesquite.lib.NameReference;
import mesquite.lib.Notification;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.DataWindowAssistantID;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.MQTextArea;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lib.ui.MousePanel;


/* ======================================================================== */
public class CharReferenceStrip extends DataWindowAssistantID {
	Ledge panel;
	MesquiteBoolean showStrip;
	boolean shown;
	CharacterData data = null;
	MesquiteTable table = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		showStrip = new MesquiteBoolean(false);
		shown = false;
		addCheckMenuItem(null, "Show Character Reference Strip", makeCommand("showStrip", this), showStrip);
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
				panel.setMessage("", -1);
				lastShown = -1;
				resetMessage();
			}
		}
		else if (!showStrip.getValue() && shown && panel != null){ //need to hide
			MesquiteWindow f = containerOfModule();
			if (f instanceof MesquiteWindow){
				shown = false;
				panel.setVisible(false);
				((MesquiteWindow)f).removeLedgePanel(panel);
				lastShown = -1;
			}
		}
	}
	private void resetMessage(){
		if (panel == null)
			return;
		if (lastShown >=0){ 
			int sel = charSelected();
			if (sel != lastShown){
				if (panel!=null) {
					setReference(lastShown, panel.getMessage());
					panel.setMessage(getReference(sel), sel);
				}
				lastShown = sel;
			}
		}
		else {
			int sel = charSelected();
			if (panel!=null) {
				panel.setMessage(getReference(sel), sel);
			}

			lastShown = sel;
		}
	}
	/*  	private void resetMessage(int ic){
   		if (lastShown >=0){ 
   			int sel = ic;
   			if (sel != lastShown){
   				setReference(lastShown, panel.getMessage());
   		   		panel.setMessage(getReference(sel), sel);
   		   		lastShown = sel;
   		   	}
   		}
   		else {
   			int sel = ic;
   			panel.setMessage(getReference(sel), sel);
   		   	lastShown = sel;
  		}
   	}*/
	private int charSelected(){
		if (table.anythingSelected()){
			int fC = table.firstColumnSelected();
			if (fC >=0){
				if (table.lastColumnSelected() == fC)
					return fC;
				return -1;
			}
			else {
				int fCC = table.firstColumnWithSelectedCell();
				if (fCC >=0){
					if (table.lastColumnWithSelectedCell()== fCC)
						return fCC;
					return -1;
				}
				else {
					int fCN = -1;
					int lCN = -1;

					for (int ic = 0; ic< data.getNumChars(); ic++){
						if (table.isColumnNameSelected(ic)){

							if (fCN<0)
								fCN = ic;
							lCN = ic;
						}
					}
					if (fCN>=0 && fCN == lCN)
						return fCN;

				}
			}
		}
		return -1;
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
	NameReference refRef = NameReference.getNameReference("charReference");
	int lastShown = -1;
	/*...............................................................................................................*/
	private String getReference(int column){
		if (data == null || column <0 || column >= data.getNumChars())
			return "";
		lastShown = column;
		//	String s = Integer.toString(column+1) + ". " + data.getCharacterName(column);
//		data.setCellObject(refRef, ic, it, null);
		String obj  = data.getAssociatedString(refRef, column);
		if (obj != null)
			return obj;



		return "";
	}
	/*...............................................................................................................*/
	private void setReference(int column, String refString){
		if (data == null || column <0 || column >= data.getNumChars())
			return;
		if (StringUtil.blank(refString))
			refString = null;
		data.setAssociatedString(refRef, column, refString);
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
		return "Character Reference Strip";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Shows the strip at bottom of matrix with character reference." ;
	}
}
class Ledge extends MousePanel {
	TextArea message, title;
	CharReferenceStrip ownerModule;
	int titleWidth = 80;
	int ic = -1;
	public Ledge(CharReferenceStrip ownerModule){
		setLayout(null);
		this.ownerModule = ownerModule;
		/* */
		message = new MQTextArea(" ", 20, 2, TextArea.SCROLLBARS_NONE);
		message.setVisible(true);
		add(message);
		message.setBounds(titleWidth,0,getBounds().width-titleWidth, getBounds().height);
		message.setBackground(Color.white);



		title = new MQTextArea(" ", 20, 2, TextArea.SCROLLBARS_NONE);
		title.setVisible(true);
		add(title);
		title.setBounds(0,0,titleWidth, getBounds().height);
		title.setBackground(ColorDistribution.veryLightBlue);
		title.setFont(new Font ("SanSerif", Font.PLAIN, 12));
		title.setText("Ref.");
		message.setFont(new Font ("SanSerif", Font.PLAIN, 12));
		/**/
	}
	public void setMessage(String s, int ic){

		if (StringUtil.stringsEqual(s, message.getText()))
			return;
		if (s == null)
			s = "";
		this.ic = ic;
		if (ic<0)
			title.setText("Ref.");
		else
			title.setText("Ref. (" + (ic+1) + ")");
		message.setText(s);

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


