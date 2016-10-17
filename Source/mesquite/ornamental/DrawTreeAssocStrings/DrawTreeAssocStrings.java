/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.ornamental.DrawTreeAssocStrings;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class DrawTreeAssocStrings extends TreeDisplayAssistantDI {
	public Vector extras;
	public boolean first = true;
	MesquiteBoolean on, horizontal, centred, showOnTerminals;
	public ListableVector names;
	static boolean asked= false;
	int fontSize = 10;
	int xOffset = 0;
	int yOffset = 0;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		extras = new Vector();
		names = new ListableVector();
		on = new MesquiteBoolean(true);  //ON is currently true always
		horizontal = new MesquiteBoolean(true);
		centred = new MesquiteBoolean(true);
		showOnTerminals = new MesquiteBoolean(true);
		MesquiteSubmenuSpec mss = addSubmenu(null, "Node-Associated Text");
		addItemToSubmenu(null, mss, "Choose Associated Text To Show...", makeCommand("chooseText",  this));
		addCheckMenuItemToSubmenu(null, mss, "Centered on Branch", makeCommand("toggleCentred",  this), centred);
		addCheckMenuItemToSubmenu(null, mss, "Horizontal", makeCommand("toggleHorizontal",  this), horizontal);
		addCheckMenuItemToSubmenu(null, mss, "Show on Terminal Branches", makeCommand("toggleShowOnTerminals",  this), showOnTerminals);
		addItemToSubmenu(null, mss, "Font Size...", makeCommand("setFontSize",  this));
		addItemToSubmenu(null, mss, "Locations...", makeCommand("setOffset",  this));
		return true;
	} 
	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return 274;  
	}
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		NodeAssocTextExtra newPj = new NodeAssocTextExtra(this, treeDisplay);
		extras.addElement(newPj);
		return newPj;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Node-Associated Text";
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		if (on.getValue()){
			temp.addLine("setOn " + on.toOffOnString());
			for (int i=0; i< names.size(); i++)
				temp.addLine("toggleShow " + StringUtil.tokenize(((Listable)names.elementAt(i)).getName()));
			temp.addLine("toggleCentred " + centred.toOffOnString());
			temp.addLine("toggleHorizontal " + horizontal.toOffOnString());
			temp.addLine("setFontSize " + fontSize); 
			temp.addLine("setOffset " + xOffset + "  " + yOffset); 
			temp.addLine("toggleShowOnTerminals " + showOnTerminals.toOffOnString());
		}
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets whether to show the node associated text", "[on or off]", commandName, "setOn")) {  //on always except if scripted off
			if (StringUtil.blank(arguments))
				on.setValue(!on.getValue());
			else
				on.toggleValue(parser.getFirstToken(arguments));
			for (int i =0; i<extras.size(); i++){
				NodeAssocTextExtra e = (NodeAssocTextExtra)extras.elementAt(i);
				e.setOn(on.getValue());
			}

		}
		else if (checker.compare(this.getClass(), "Shows dialog box to choose what text to display", null, commandName, "chooseText")) {
			if (extras.size() == 0)
				return null;
			showChoiceDialog((Associable)((NodeAssocTextExtra)extras.elementAt(0)).lastTree);
		}
		else if (checker.compare(this.getClass(), "Sets whether to show the values on the terminal branches", "[on or off]", commandName, "toggleShowOnTerminals")) {
			if (StringUtil.blank(arguments))
				showOnTerminals.setValue(!showOnTerminals.getValue());
			else
				showOnTerminals.toggleValue(parser.getFirstToken(arguments));
			if (!MesquiteThread.isScripting()) parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets whether to write the text horizontally", "[on or off]", commandName, "toggleHorizontal")) {
			if (StringUtil.blank(arguments))
				horizontal.setValue(!horizontal.getValue());
			else
				horizontal.toggleValue(parser.getFirstToken(arguments));
			if (!MesquiteThread.isScripting()) parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets whether to write the text centrally over the branches", "[on or off]", commandName, "toggleCentred")) {
			if (StringUtil.blank(arguments))
				centred.setValue(!centred.getValue());
			else
				centred.toggleValue(parser.getFirstToken(arguments));
			if (!MesquiteThread.isScripting()) parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets offset of label from nodes", "[offsetX] [offsetY]", commandName, "setOffset")) {
			int newX= MesquiteInteger.fromFirstToken(arguments, pos);
			int newY= MesquiteInteger.fromString(arguments, pos);
			if (!MesquiteInteger.isCombinable(newX)){
				MesquiteBoolean answer = new MesquiteBoolean(false);
				MesquiteInteger nX = new MesquiteInteger(xOffset);
				MesquiteInteger nY = new MesquiteInteger(yOffset);
				MesquiteInteger.queryTwoIntegers(containerOfModule(), "Location of Text", "X offset from node", "Y offset from node", answer, nX, nY,-200,200,-200,200, null);
				if (!answer.getValue())
					return null;
				newX = nX.getValue();
				newY = nY.getValue();
			}
			if (newX>-200 && newX <200 && newY>-200 && newY <200 && (newX!=xOffset ||   newY!=yOffset)) {
				xOffset = newX;
				yOffset = newY;
				if (!MesquiteThread.isScripting()) parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets font size", "[font size]", commandName, "setFontSize")) {
			int newWidth= MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newWidth))
				newWidth = MesquiteInteger.queryInteger(containerOfModule(), "Set font size", "Font Size:", fontSize, 2, 96);
			if (newWidth>1 && newWidth<96 && newWidth!=fontSize) {
				fontSize = newWidth;
				for (int i =0; i<extras.size(); i++){
					NodeAssocTextExtra e = (NodeAssocTextExtra)extras.elementAt(i);
					e.resetFontSize();
				}
				if (!MesquiteThread.isScripting()) parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether to show a node associated text", "[on or off]", commandName, "toggleShow")) {
			String name = parser.getFirstToken(arguments);
			if (isShowing(name)){
				names.removeElementAt(names.indexOfByName(name), false);
			}
			else
				names.addElement(new MesquiteString(name, name), false);

			for (int i =0; i<extras.size(); i++){
				NodeAssocTextExtra e = (NodeAssocTextExtra)extras.elementAt(i);
				e.update();
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	void showChoiceDialog(Associable tree) {
		if (tree == null)
			return;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ListableVector v = new ListableVector();
		int num = tree.getNumberAssociatedObjects();
		boolean[] shown = new boolean[num + names.size()]; //bigger than needed probably
		for (int i = 0; i< num; i++){
			ObjectArray da = tree.getAssociatedObjects(i);
			if (da != null){
				v.addElement(new MesquiteString(da.getName(), ""), false);
			if (names.indexOfByName(da.getName())>=0)
				shown[i] = true;
			}
		}
		for (int i = 0; i<names.size(); i++){
			String name = ((MesquiteString)names.elementAt(i)).getName();
			if (v.indexOfByName(name)<0){
				v.addElement(new MesquiteString(name, " (not in current tree)"), false);
				if (v.size()-1>= shown.length)
					shown[v.size()-1] = true;
			}
		}
		if (v.size()==0)
			alert("This Tree has no text associated with nodes");
		else {
			ExtensibleDialog queryDialog = new ExtensibleDialog(containerOfModule(), "Text to show",  buttonPressed);
			queryDialog.addLabel("Text to display on tree", Label.CENTER);
			Checkbox[] checks = new Checkbox[v.size()];
			for (int i=0; i<v.size(); i++){
				MesquiteString ms = (MesquiteString)v.elementAt(i);
				checks[i] = queryDialog.addCheckBox (ms.getName() + ms.getValue(), shown[i]);
			}

			queryDialog.completeAndShowDialog(true);

			boolean ok = (queryDialog.query()==0);

			if (ok) {
				names.removeAllElements(false);
				for (int i=0; i<checks.length; i++){
					MesquiteString ms = (MesquiteString)v.elementAt(i);
					if (checks[i].getState())
						names.addElement(new MesquiteString(ms.getName(), ms.getName()), false);
				}
				for (int i =0; i<extras.size(); i++){
					NodeAssocTextExtra e = (NodeAssocTextExtra)extras.elementAt(i);
					e.setOn(on.getValue());
				}
			}

			queryDialog.dispose();
		}
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Shows text attached to nodes on the tree." ;
	}
	public boolean isSubstantive(){
		return false;
	}   	 

	boolean isShowing(String name){
		boolean s = names.indexOfByName(name)>=0;
		return s;
	}
}

/* ======================================================================== */
class NodeAssocTextExtra extends TreeDisplayExtra  {
	DrawTreeAssocStrings assocTextModule;
	MesquiteCommand taxonCommand, branchCommand;
	boolean on;
	Tree lastTree = null;
//	StringInABox
	public NodeAssocTextExtra (DrawTreeAssocStrings ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		assocTextModule = ownerModule;
		on = assocTextModule.on.getValue();
		resetFontSize();
	}
	StringInABox box = new StringInABox( "", treeDisplay.getFont(),1500);
	public void resetFontSize(){
		Font f = treeDisplay.getFont();
		box.setFont(new Font(f.getName(),f.getStyle(), assocTextModule.fontSize)); 
	}
	/*.................................................................................................................*/
	public   void myDraw(Tree tree, int node, Graphics g, ObjectArray[] arrays) {
		if (!assocTextModule.showOnTerminals.getValue() && tree.nodeIsTerminal(node))
			return;
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			myDraw(tree, d, g, arrays);
		for (int i=0; i<arrays.length; i++){
			Object d = arrays[i].getValue(node);
			String s = null;
			if (d instanceof String)
				s = (String)d;
			else if (d instanceof MesquiteString)
				s = ((MesquiteString)d).getValue();
			if (!StringUtil.blank(s)){
				box.setString(s);
				box.setColors(Color.black, Color.white);
				int x, y;
				if (assocTextModule.centred.getValue()){
					int centreBranchX = treeDisplay.getTreeDrawing().getBranchCenterX(node) + assocTextModule.xOffset;
					int centreBranchY =  treeDisplay.getTreeDrawing().getBranchCenterY(node)+ assocTextModule.yOffset;
					/*g.setColor(Color.yellow);
					g.drawLine(treeDisplay.getTreeDrawing().lineBaseX[node], treeDisplay.getTreeDrawing().lineBaseY[node], treeDisplay.getTreeDrawing().lineTipX[node], treeDisplay.getTreeDrawing().lineTipY[node]);
					/*g.setColor(Color.red);
					g.drawRect(centreBranchX-10, centreBranchY-10, 20, 20);
					g.drawString(Integer.toString(node), centreBranchX, centreBranchY);*/
					int stringWidth = box.getMaxWidthMunched();
					if (assocTextModule.horizontal.getValue()){
						x = centreBranchX - stringWidth/2;
						y = centreBranchY - assocTextModule.fontSize;
					}
					else {
						x = centreBranchX - assocTextModule.fontSize*2;
						y = centreBranchY + stringWidth/2;
					}
				}
				else {
					x= treeDisplay.getTreeDrawing().x[node] + assocTextModule.xOffset;
					y = treeDisplay.getTreeDrawing().y[node] + assocTextModule.yOffset + i*assocTextModule.fontSize*2;
				}
				if (assocTextModule.horizontal.getValue())
					box.draw(g,  x, y);
				else
					box.draw(g,  x, y, 0, 1500, treeDisplay, false);

			}
		}

	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int node, Graphics g) {
		if (!on)
			return;
		int num = tree.getNumberAssociatedObjects();
		int total = 0;
		for (int i = 0; i< num; i++){
			ObjectArray da = tree.getAssociatedObjects(i);
			if (assocTextModule.isShowing(da.getName()))
				total++;
		}
		ObjectArray[] arrays = new ObjectArray[total];
		int count = 0;
		for (int i = 0; i< num; i++){
			ObjectArray da = tree.getAssociatedObjects(i);
			if (assocTextModule.isShowing(da.getName()))
				arrays[count++] = da;
		}

		myDraw(tree, node, g, arrays);

	}

	void update(){
		treeDisplay.pleaseUpdate(false);
	}
	void setOn(boolean a){
		on = a;
		treeDisplay.pleaseUpdate(false);
	}
	/**return a text version of information at node*/
	public String textAtNode(Tree tree, int node){
		if (!on)
			return null;
		if (tree.getNumberAssociatedObjects() == 0 || tree.getWhichAssociatedObject(assocValueRef)==null)
			return "";
		return getValue(tree, node);
	}
	/**return a text version of information on tree, displayed on a text version of the tree*/
	public String writeOnTree(Tree tree, int node){
		if (!on || tree.getNumberAssociatedObjects() == 0)
			return null;
		return super.writeOnTree(tree, node);
	}
	/**return a text version of information on tree, displayed as list of nodes with information at each*/
	public String infoAtNodes(Tree tree, int node){
		if (!on || tree.getNumberAssociatedObjects() == 0)
			return null;
		return super.infoAtNodes(tree, node);
	}
	/**return a table version of information on tree, displayed as list of nodes with information at each*/
	public String tableAtNodes(Tree tree, int node){
		if (!on || tree.getNumberAssociatedObjects() == 0)
			return null;
		return super.tableAtNodes(tree, node);
	}
	MesquitePopup popup=null;
	/*.................................................................................................................*/
	void redoMenu(Associable tree) {

		if (popup==null)
			popup = new MesquitePopup(treeDisplay);
		popup.removeAll();
		popup.add(new MesquiteMenuItem("Display Node-Associated Text", null, null));
		popup.add(new MesquiteMenuItem("-", null, null));
		int num = tree.getNumberAssociatedObjects();
		if (num == 0)
			popup.add(new MesquiteMenuItem("This Tree has no text associated with nodes", null, null));
		else 
			for (int i = 0; i< num; i++){
				ObjectArray da = tree.getAssociatedObjects(i);
				MesquiteCommand mc = new MesquiteCommand("toggleShow", assocTextModule);
				String selName = " ";
				if (assocTextModule.isShowing(da.getName()))
					selName = da.getName();
				popup.add(new MesquiteCheckMenuItem(da.getName(), assocTextModule, mc, StringUtil.tokenize(da.getName()), new MesquiteString(selName )));
			}
		treeDisplay.add(popup);
	}
	public void cursorTouchField(Tree tree, Graphics g, int x, int y, int modifiers){
		/*if (!on)  popup menu style disabled
			return;
		redoMenu((Associable)tree);
		popup.show(treeDisplay, x, y);*/
	}
	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		if (!on)
			return;
		drawOnTree(tree, drawnRoot, g); //should draw numbered footnotes!
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		lastTree = tree;
	}
	NameReference assocValueRef = NameReference.getNameReference("consensusFrequency");
	String getValue(Tree tree, int node){
		Object obj  = tree.getAssociatedObject(assocValueRef, node);
		if (obj instanceof String)
			return (String)obj;
		else if (obj instanceof MesquiteString)
			return ((MesquiteString)obj).getValue();
		return "";
	}

	public void turnOff() {
		assocTextModule.extras.removeElement(this);
		super.turnOff();
	}
}



