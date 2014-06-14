/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)

Modified 27 July 01: name reverted to "Tree Legend"; added getNameForMenuItem "Tree Legend..."
 */
package mesquite.meristic.MerItemsEditInfo;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.meristic.lib.*;
import mesquite.charMatrices.lib.MatrixInfoExtraPanel;
import mesquite.lib.table.*;

public class MerItemsEditInfo extends MatrixInfoPanelAssistantI  {
	ItemsPanel panel;

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
		return true;
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
	public CompatibilityTest getCompatibilityTest(){
		return new MeristicStateTest();
		}

	/*.................................................................................................................*/
	public MatrixInfoExtraPanel getPanel(ClosablePanelContainer container){
		panel =  new ItemsPanel(container, this);
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
		return "Meristic Items Editor";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Edits items for meristic data matrix within the editor info panel.";
	}
}
/*===========================================*/
class ItemsPanel extends MatrixInfoExtraPanel  {
	String message = null;
	MerItemsEditInfo ownerModule;
	StringInABox statesBox;
	ItemsField itemsField;
	MeristicData cData;
	Image add, subtract, query;
	public ItemsPanel(ClosablePanelContainer container, MerItemsEditInfo ownerModule){
		super(container, "Items");
		statesBox =  new StringInABox("", null, 50);
		itemsField = new ItemsField(this);
		itemsField.setBackground(getBackground());
		add = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "addGray.gif");
		subtract = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "subtractGray.gif");
		query = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "queryGray.gif");
		currentHeight = 100 + MINHEIGHT + charNameHeight;
		setLayout(null);
		add(itemsField);
		resetLocs();
		this.ownerModule = ownerModule;
	}
	public int getRequestedHeight(int width){
		if (isOpen())
			return MINHEIGHT + itemsField.getDesiredHeight() + 30;
		else
			return MINHEIGHT;
	}

	public void setMatrixAndTable(CharacterData data, MesquiteTable table){
		super.setMatrixAndTable(data, table);
		this.cData = (MeristicData)data;
		itemsField.setData(cData);
		resetLocs();
		container.requestHeightChange(this);
		repaint();
	}
	void enterName(int item, String name){
		if (item<0 || item>= cData.getNumItems())
			return;
		cData.setItemReference(item, NameReference.getNameReference(name));
		((CharacterData)cData).notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
	}
	public void setOpen(boolean open){
		itemsField.setVisible(open);
		resetLocs();
		super.setOpen(open);
	}
	int charNameHeight = 24;
	void resetLocs(){
		if (itemsField == null)
			return;
		//itemsField.setBounds(0, 0, getWidth()-4, getHeight());
		itemsField.setBounds(2, MINHEIGHT + 4, getWidth()-4, itemsField.getDesiredHeight());
	}
	public void setSize(int w, int h){
		super.setSize(w, h);
		resetLocs();
	}
	public void setBounds(int x, int y, int w, int h){
		super.setBounds(x, y, w, h);
		resetLocs();
	}
	void changeFocus(int item, int change){
		if (item+change >= 0 && item+change< cData.getNumItems())
			itemsField.items[item+change].requestFocusInWindow();
	}
	public void paint(Graphics g){
		super.paint(g);
		int h = itemsField.getY() + itemsField.getHeight() + 4;
		g.drawImage(add, 2, h, this);
		g.drawImage(subtract, 24, h, this);
		g.drawImage(query, getWidth()-20, 4, this);
	}

	/* to be used by subclasses to tell that panel touched */
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		int h = itemsField.getY() + itemsField.getHeight() + 4;
		if (y>=h && y< h+19) {
			if (x>=2 && x<20){
				String d = MesquiteString.queryString(MesquiteWindow.windowOfItem(this), "New item", "Name of new item.", "");
				if (StringUtil.blank(d))
					return;
				else {
					cData.addItem(d);
					itemsField.setData(cData);
					((CharacterData)cData).notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
					container.requestHeightChange(this);
				}
			}
			else if (x>= 24 && x<=40){
				String[] items = new String[cData.getNumItems()];
				for (int i=0; i<items.length; i++){
					items[i]= cData.getItemName(i);
				}
				int d = ListDialog.queryList(MesquiteWindow.windowOfItem(this), "Remove item", "Remove item:", MesquiteString.helpString, items, 0);
				if (!MesquiteInteger.isCombinable(d) || d<0 || d>=cData.getNumItems())
					return;
				else {
					cData.removeItem(d);
					itemsField.setData(cData);
					((CharacterData)cData).notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
					container.requestHeightChange(this);
				}
			}
		}
		else if (y<MINHEIGHT && (x> getWidth()- 20)) {
			MesquiteTrunk.mesquiteTrunk.alert("These are the items of the meristic matrix.  A meristic matrix can have multiple items in each cell. Here you can control how many items there are, and their names.");  //query button hit
		}
		else
			super.mouseUp(modifiers,  x,  y,  tool);
	}
}
/*-----------------------------*/
class ItemsField extends Panel {
	NameField[] items;
	ItemsPanel panel;
	MeristicData data;
	int ic = -1;
	public ItemsField(ItemsPanel panel){
		this.panel = panel;
		setLayout(null);
		resetLocs();
	}
	void setData(MeristicData data){
		this.data = data;
		resetItems();
		resetLocs();

	}
	public void setVisible(boolean vis){
		super.setVisible(vis);
		if (items == null)
			return;
		for (int state = 0; state<items.length; state++)
			items[state].setVisible(vis);
	}
	int w = 10;
	void setContainingWidth(int w){
		this.w = w-30;
		resetLocs();
	}
	
	public void resetItems(){
		if (items == null || items.length != data.getNumItems()){
			if (items != null)
				for (int item = 0; item< items.length; item++){
					remove(items[item]);
				}
			items = new NameField[data.getNumItems()];

			for (int item = 0; item<data.getNumItems(); item++){
				items[item] = new NameField(panel, item);
				add(items[item]);
				items[item].setVisible(true);
			}
		}
		if (items == null)
			return;
		for (int item = 0; item<data.getNumItems() && item < items.length; item++){
			items[item].setText(data.getItemName(item));
		}
	}
	int charNameHeight = 24;
	void resetLocs(){
		if (items == null)
			return;
		for (int state = 0; state<items.length; state++){
			items[state].setBounds(2, charNameHeight*state, getWidth()-4, charNameHeight);
		}
	}
	int getDesiredHeight(){
		if (data == null)
			return  charNameHeight*2;
		return charNameHeight*(data.getNumItems());
	}

	public void setSize(int w, int h){
		super.setSize(w, h);
		resetLocs();
	}
	public void setBounds(int x, int y, int w, int h){
		super.setBounds(x, y, w, h);
		resetLocs();
	}
}
class NameField extends TextField implements FocusListener {
	ItemsPanel panel;
	int item = -1;
	boolean somethingTyped;

	public NameField(ItemsPanel panel, int item){
		this.panel = panel;
		this.item = item;
		setText("");
		addKeyListener(new KListener());
		addFocusListener(this);
	}

	public void focusGained(FocusEvent e){
	}
	public void focusLost(FocusEvent e){
		if (somethingTyped)
			panel.enterName(item, getText());
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
					panel.enterName(item, getText());
					setSelectionStart(getText().length());
					setSelectionEnd(getText().length());
				}
				panel.changeFocus(item, 1);
			}
			else if (e.getKeyCode()== KeyEvent.VK_UP) {
				if (somethingTyped)
					panel.enterName(item, getText());
				panel.changeFocus(item, -1);
			}
			else if (e.getKeyCode()== KeyEvent.VK_DOWN || e.getKeyCode()== KeyEvent.VK_TAB){
				if (somethingTyped)
					panel.enterName(item, getText());
				panel.changeFocus(item, 1);
			}
			else { 
				somethingTyped=true;
			}

		}
	}

}

