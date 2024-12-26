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
package mesquite.categ.SmallStateNamesEditor;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.charMatrices.lib.MatrixInfoExtraPanel;
import mesquite.lib.table.*;

public class SmallStateNamesEditor extends MatrixInfoPanelAssistantI  {
	SStateNamesPanel panel;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return 250;  
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*
	public  Class getHireSubchoice(){
		return NumberForTree.class;
	}*/
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresExactlyCategoricalData();
		}

	/*.................................................................................................................*/
	public MatrixInfoExtraPanel getPanel(ClosablePanelContainer container){
		panel =  new SStateNamesPanel(container, this);
		return panel;
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
		return "Small State Names Editor";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Edits character state names within the editor info panel.";
	}
}
/*===========================================*/
class SStateNamesPanel extends MatrixInfoExtraPanel  {
	String message = null;
	SmallStateNamesEditor ownerModule;
	StringInABox statesBox;
	NameField charName;
	StatesField statesNames;
	CategoricalData cData;
	ScrollPane pane;
	public SStateNamesPanel(ClosablePanelContainer container, SmallStateNamesEditor ownerModule){
		super(container, "State Names");
		statesBox =  new StringInABox("", null, 50);
		charName = new NameField(this, -1);
		pane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
		statesNames = new StatesField(this);
		statesNames.setBackground(getBackground());
		currentHeight = 100 + MINHEIGHT + charNameHeight;
		setLayout(null);
		add(charName);
		pane.add(statesNames);
		add(pane);
		resetLocs();
		this.ownerModule = ownerModule;
		setOpen(true);
	}
	public boolean userExpandable(){
		return true;
	}
	public void setMatrixAndTable(CharacterData data, MesquiteTable table){
		super.setMatrixAndTable(data, table);
		this.cData = (CategoricalData)data;
		statesNames.setData(cData);
		container.requestHeightChange(this);
		repaint();
	}
	public void setCell(int ic, int it){
		if (!isVisible())
			return;
		super.setCell(ic, it);
		adjustMessage();
			statesNames.setEditEnabled(ic >= 0 && ic< cData.getNumChars());
			charName.setEditEnabled(ic >= 0 && ic< cData.getNumChars());
		container.requestHeightChange(this);
		repaint();
	}
	private void adjustMessage(){
		setTitle("State Names");
		if (data == null)
			message = "no matrix";
		else if (it < 0 && ic < 0) {
			message = " ";
			charName.setText("");
			statesNames.setCharacter(-1);
		}
		else {
			message = "";
			if (ic >= 0 && ic < data.getNumChars()) {
				setTitle("State Names (Char. " + (ic+1) + ")");
				if (!charName.getText().equals(data.getCharacterName(ic)))
					charName.setText(data.getCharacterName(ic));
				statesNames.setCharacter(ic);
				message = "";
				/*if (data.characterHasName(ic))
					message += data.getCharacterName(ic) + " (" + (ic+1) + ")";
				else
					message += (ic+1);
				 */
				if (data instanceof CategoricalData){
					CategoricalData cData = (CategoricalData)data;
					for (int s= 0; s<= CategoricalState.maxCategoricalState; s++){
						if (cData.hasStateName(ic, s)){
							message += "\n" + s + ": " + cData.getStateName(ic, s);
						}
					}
				}
			}
			else {
				charName.setText("");
				statesNames.setCharacter(-1);
			}
			message += "\n*Under Construction*";
		}
	}
	void enterName(int state, String name){
		if (ic<0 || ic>= data.getNumChars())
			return;
		if (state == -1)
			data.setCharacterName(ic, name);
		else if (state<= CategoricalState.maxCategoricalState)
			cData.setStateName(ic, state, name);
	}
	void changeFocus(int state, int change){
		if (state+change >= 0 && state+change<= CategoricalState.maxCategoricalState)
			statesNames.states[state+change].requestFocusInWindow();
	}
	boolean selectionInSingleColumn(){
		if (table.anyRowNameSelected())
			return false;
		if (table.anyRowSelected())
			return false;

		Dimension fC = table.getFirstTableCellSelected();
		Dimension LC = table.getLastTableCellSelected();
		int f = table.firstColumnSelected();
		int L = table.lastColumnSelected();
		if ( fC.width<0 && LC.width<0){
			return (f>-1 && f==L);
		}
		else if (f<0 && L<0){
			return (fC.width>-1 && fC.width == LC.width);
		}
		return  (f>-1 && f==L && f== fC.width && fC.width == LC.width);
	}
	public void cellEnter(int ic, int it){
		// if a single column or cells within a single column are selected, then cut out
		if (selectionInSingleColumn())
			return;
		super.cellEnter(ic, it);
	}
	public void cellExit(int ic, int it){
		if (selectionInSingleColumn())
			return;
		super.cellExit(ic, it);

	}
	public void setOpen(boolean open){
		charName.setVisible(open);
		statesNames.setVisible(open);
		super.setOpen(open);
	}
	int charNameHeight = 24;
	void resetLocs(){
		charName.setBounds(2, MINHEIGHT + 4, getWidth()-4, charNameHeight);
		pane.setBounds(2, MINHEIGHT + 4+charNameHeight+2, getWidth()-4, getHeight() - (MINHEIGHT + 4+charNameHeight+2));
		statesNames.setContainingWidth(getWidth());		
		pane.doLayout();
	}
	public void setSize(int w, int h){
		super.setSize(w, h);
		resetLocs();
	}
	public void setBounds(int x, int y, int w, int h){
		super.setBounds(x, y, w, h);
		resetLocs();
	}
	/*public int getRequestedHeight(int width){
		if (!open)
			return MINHEIGHT;
		return 100 + MINHEIGHT + charNameHeight;
	}

*/
}
/*-----------------------------*/
class StatesField extends Panel {
	NameField[] states;
	SStateNamesPanel panel;
	CategoricalData data;
	int ic = -1;
	int w = 400;
	public StatesField(SStateNamesPanel panel){
		this.panel = panel;
		setLayout(null);
		states = new NameField[CategoricalState.maxCategoricalState+1];

		for (int state = 0; state<=CategoricalState.maxCategoricalState; state++){
			states[state] = new NameField(panel, state);
			add(states[state]);
		}
		resetBounds();
	}
	void setEditEnabled(boolean en){
		for (int state = 0; state<=CategoricalState.maxCategoricalState; state++){
			states[state].setEditEnabled(en);
		}
	}
	void setData(CategoricalData data){
		this.data = data;
	}
	public void setVisible(boolean vis){
		super.setVisible(vis);
		for (int state = 0; state<=CategoricalState.maxCategoricalState; state++)
			states[state].setVisible(vis);
	}
	void setContainingWidth(int w){
		this.w = w-30;
		resetSizes();
	}
	public Dimension getPreferredSize(){
		
		return new Dimension(w, getDesiredHeight());
	}
	int charNameHeight = 24;
	void resetBounds(){
		for (int state = 0; state<=CategoricalState.maxCategoricalState; state++){
			states[state].setBounds(20, charNameHeight*state, w-4, charNameHeight);
		}
	}
	void resetSizes(){
		for (int state = 0; state<=CategoricalState.maxCategoricalState; state++){
			states[state].setSize(w-4, charNameHeight);
		}
	}
	int getDesiredHeight(){
		return charNameHeight*(CategoricalState.maxCategoricalState+1);
	}
	public void paint(Graphics g){
		super.paint(g);
		if (!isVisible())
			return;
		for (int state = 0; state<=CategoricalState.maxCategoricalState; state++){
			g.drawString(data.getStateSymbol(ic, state), 2, charNameHeight*(state+1)-6);
		}
	}
	public void setSize(int w, int h){
		super.setSize(w, h);
		this.w = w-30;
		resetSizes();
	}
	public void setBounds(int x, int y, int w, int h){
		super.setBounds(x, y, w, h);
		this.w = w-30;
		resetSizes();
	}
	public void setCharacter(int ic){
		this.ic = ic;
		if (ic<0 || ic > data.getNumChars()){
			String s = "";
			for (int state = 0; state<=CategoricalState.maxCategoricalState; state++){
				if (!states[state].getText().equals(s))
					states[state].setText(s);
			}
		}
		else
			for (int state = 0; state<=CategoricalState.maxCategoricalState; state++){
				String s = "";
				if (data.hasStateName(ic, state))
					s = data.getStateName(ic, state);
				if (!states[state].getText().equals(s))
					states[state].setText(s);
			}
	}
}
class NameField extends TextField implements FocusListener {
	SStateNamesPanel panel;
	int state = -1;
	boolean somethingTyped;

	public NameField(SStateNamesPanel panel, int state){
		this.panel = panel;
		this.state = state;
		setText("");
		addKeyListener(new KListener());
		addFocusListener(this);
	}

	public void setEditEnabled(boolean en){
		if (en){
			setBackground(Color.white);
		setEditable(true);
	}
		else {
			setBackground(ColorDistribution.veryLightGray);
			setEditable(false);
		}
	}
	public void focusGained(FocusEvent e){
	}
	public void focusLost(FocusEvent e){
		if (somethingTyped)
			panel.enterName(state, getText());
	}
	class KListener extends KeyAdapter {
		MesquiteWindow window = null;
		public KListener (){
			super();
		}
		public void keyPressed(KeyEvent e){
			//Event queue
			if (e.getKeyCode()== KeyEvent.VK_ENTER) {
				if (somethingTyped){
					panel.enterName(state, getText());
					setSelectionStart(getText().length());
					setSelectionEnd(getText().length());
				}
				panel.changeFocus(state, 1);
		}
			else if (e.getKeyCode()== KeyEvent.VK_UP) {
				if (somethingTyped)
					panel.enterName(state, getText());
				panel.changeFocus(state, -1);
			}
			else if (e.getKeyCode()== KeyEvent.VK_DOWN || e.getKeyCode()== KeyEvent.VK_TAB){
				if (somethingTyped)
					panel.enterName(state, getText());
				panel.changeFocus(state, 1);
			}
			else { 
				somethingTyped=true;
			}

		}
	}

}

