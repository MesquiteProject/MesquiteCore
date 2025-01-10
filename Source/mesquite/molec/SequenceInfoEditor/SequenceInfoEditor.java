/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)

Modified 27 July 01: name reverted to "Tree Legend"; added getNameForMenuItem "Tree Legend..."
 */
package mesquite.molec.SequenceInfoEditor;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.charMatrices.lib.MatrixInfoExtraPanel;
import mesquite.lib.table.*;
import mesquite.lib.ui.ClosablePanelContainer;
import mesquite.lib.ui.MQTextArea;

public class SequenceInfoEditor extends MatrixInfoPanelAssistantI  {
	SequenceInfoPanel panel;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return 250;  
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}

	/*.................................................................................................................*/
	public MatrixInfoExtraPanel getPanel(ClosablePanelContainer container){
		panel =  new SequenceInfoPanel(container, this);
		return panel;
	}

	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyMolecularData();
		}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("panelOpen " + panel.isOpen());
		return temp;
	}

	public void employeeQuit(MesquiteModule m){
		if (m == null)
			return;
		//zap values panel line

	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the panel open", null, commandName, "panelOpen")) {
			if (panel != null)
				panel.setOpen(arguments == null || arguments.equalsIgnoreCase("true"));
		}
		else 
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public void endJob() {
		super.endJob();
		resetContainingMenuBar();
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Sequence Information Editor";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Edits character state names within the editor info panel.";
	}
}
/*===========================================*/
class SequenceInfoPanel extends MatrixInfoExtraPanel  {
	String message = null;
	SequenceInfoEditor ownerModule;
	NoteField infoArea;
	CategoricalData cData;
	Associable info;
	NameReference siRef = NameReference.getNameReference("SequenceInfo");
	public SequenceInfoPanel(ClosablePanelContainer container, SequenceInfoEditor ownerModule){
		super(container, "Sequence Info");
		infoArea = new NoteField(this);
		currentHeight = 60 + MINHEIGHT;
		setLayout(null);
		add(infoArea);
		infoArea.setVisible(true);
		resetLocs();
		this.ownerModule = ownerModule;
		setOpen(false);
	}
	public boolean userExpandable(){
		return true;
	}
	public void setMatrixAndTable(CharacterData data, MesquiteTable table){
		super.setMatrixAndTable(data, table);
		this.cData = (CategoricalData)data;
		info = data.getTaxaInfo(true);
		siRef = info.makeAssociatedObjects("SequenceInfo");
		container.requestHeightChange(this);
		repaint();
	}
	public void setCell(int ic, int it){
		if (!isVisible())
			return;
		super.setCell(ic, it);
		adjustMessage();
		container.requestHeightChange(this);
		repaint();
	}
	private void adjustMessage(){
		if (data == null)
			infoArea.setText("", ic, it);
		else if (it < 0 && ic < 0)
			infoArea.setText("", ic, it);
		else {
			message = "";
			if (it >= 0 && it < data.getNumTaxa()) {
				Object o = info.getAssociatedObject(siRef, it);
				if (o != null && o instanceof String){
					infoArea.setText((String)o, ic, it);
				}
				else
				infoArea.setText("", ic, it);
			}
			else {
				infoArea.setText("", ic, it);
			}
		}
	}

	public void setOpen(boolean open){
		infoArea.setVisible(open);
		resetLocs();
		super.setOpen(open);
	}
	void resetLocs(){
		infoArea.setBounds(2, MINHEIGHT + 4, getWidth()-4, currentHeight- MINHEIGHT-4);
	}
	public void setSize(int w, int h){
		super.setSize(w, h);
		resetLocs();
	}
	public void setBounds(int x, int y, int w, int h){
		super.setBounds(x, y, w, h);
		resetLocs();
	}
	public void enterText(NoteField n, String text, int rIC, int rIT, boolean warn){
		if (data == null)
			return;
		else if (it < 0) {
			if (warn)
				infoArea.setText("A row must be touched or selected first to edit the sequence information");
			return;
		}
		else {
			if (rIT != it){
				MesquiteMessage.warnProgrammer("rIT/it mismatch in seqinfopanel");
				return;
			}
			info.setAssociatedObject(siRef, it, text);	
		}
	}
	
	boolean selectionInSingleRow(){
		if (table.anyColumnNameSelected())
			return false;
		if (table.anyColumnSelected())
			return false;

		Dimension fC = table.getFirstTableCellSelected();
		Dimension LC = table.getLastTableCellSelected();
		int f = table.firstRowSelected();
		int L = table.lastRowSelected();
		if ( fC.height<0 && LC.height<0){
			return (f>-1 && f==L);
		}
		else if (f<0 && L<0){
			return (fC.height>-1 && fC.height == LC.height);
		}
		return  (f>-1 && f==L && f== fC.height && fC.height == LC.height);
	}
	public void cellEnter(int ic, int it){
		// if a single column or cells within a single column are selected, then cut out
		if (selectionInSingleRow())
			return;
		super.cellEnter(ic, it);
	}
	public void cellExit(int ic, int it){
		if (selectionInSingleRow())
			return;
		super.cellExit(ic, it);

	}


}

class NoteField extends MQTextArea implements FocusListener {
	SequenceInfoPanel panel;
	boolean somethingTyped;
	int ic = -1;
	int it= -1;

	public NoteField(SequenceInfoPanel panel){
		super("", 4, 40, TextArea.SCROLLBARS_NONE);
		this.panel = panel;
		setText("");
		addKeyListener(new KListener(this));
		addFocusListener(this);
	}

	public void setText(String s, int ic, int it){
		setText(s);
		this.ic = ic;
		this.it = it;
	}
	public void focusGained(FocusEvent e){
	}
	public void focusLost(FocusEvent e){
		
		if (somethingTyped)
			panel.enterText(this, getText(), ic, it, false);
		somethingTyped = false;
	}
	class KListener extends KeyAdapter {
		NoteField nf = null;
		public KListener (NoteField nf){
			super();
			this.nf = nf;
		}
		public void keyPressed(KeyEvent e){
			//Event queue
			if (e.getKeyCode()== KeyEvent.VK_ENTER) {
				if (somethingTyped){
					String s = getText();
					panel.enterText(nf, s, ic, it, true);
					somethingTyped = false;
					e.consume();
					setSelectionStart(s.length());
					setSelectionEnd(s.length());
				}
			}
			else { 
				somethingTyped=true;
			}

		}
	}

}

