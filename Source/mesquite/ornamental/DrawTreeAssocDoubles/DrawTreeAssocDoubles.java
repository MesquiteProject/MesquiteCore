/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.ornamental.DrawTreeAssocDoubles;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.characters.CentralModelListener;
import mesquite.lib.duties.*;
import mesquite.stochchar.lib.AsymmModel;

/* ======================================================================== */
public class DrawTreeAssocDoubles extends TreeDisplayAssistantDI {
	public Vector extras;
	public boolean first = true;
	MesquiteBoolean on, percentage, horizontal, centred, whiteEdges, showOnTerminals;
	MesquiteInteger positionAlongBranch;
	MesquiteSubmenuSpec positionSubMenu;
	public static final boolean CENTEREDDEFAULT = false;
	public ListableVector names;
	static boolean asked= false;
	int digits = 4;
	int fontSize = 10;
	int xOffset = 0;
	int yOffset = 0;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		extras = new Vector();
		names = new ListableVector();
		if (!MesquiteThread.isScripting())
			names.addElement(new MesquiteString("consensusFrequency", "consensusFrequency"), false);
		on = new MesquiteBoolean(true);  //ON is currently true always
		percentage = new MesquiteBoolean(false);
		horizontal = new MesquiteBoolean(true);
		centred = new MesquiteBoolean(CENTEREDDEFAULT);
		whiteEdges = new MesquiteBoolean(true);
		showOnTerminals = new MesquiteBoolean(true);
		MesquiteSubmenuSpec mss = addSubmenu(null, "Node-Associated Values");
		addItemToSubmenu(null, mss, "Choose Values To Show...", makeCommand("chooseValues",  this));
		MesquiteSubmenuSpec mss2 =  addSubmenu(mss, "Styles");  //Wayne: here it is.   x123y
		addItemToSubmenu(mss, mss2, "Percentage, Below Branch", makeCommand("setCorvallisStyle",  this));
		addItemToSubmenu(null, mss, "Digits...", makeCommand("setDigits",  this));
		addCheckMenuItemToSubmenu(null, mss, "Show As Percentage", makeCommand("writeAsPercentage",  this), percentage);
		addItemToSubmenu(null, mss, "Font Size...", makeCommand("setFontSize",  this));
		addCheckMenuItemToSubmenu(null, mss, "White Edges", makeCommand("toggleWhiteEdges",  this), whiteEdges);
		addCheckMenuItemToSubmenu(null, mss, "Show on Terminal Branches", makeCommand("toggleShowOnTerminals",  this), showOnTerminals);
		addCheckMenuItemToSubmenu(null, mss, "Centered on Branch", makeCommand("toggleCentred",  this), centred);
//		addItemToSubmenu(null, mss, "Position Along Branch...", makeCommand("setPositionAlongBranch",  this));
		addItemToSubmenu(null, mss, "Locations...", makeCommand("setOffset",  this));
		addCheckMenuItemToSubmenu(null, mss, "Horizontal", makeCommand("toggleHorizontal",  this), horizontal);
		return true;
	} 
	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return 250;  
	}
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		NodeAssocValuesExtra newPj = new NodeAssocValuesExtra(this, treeDisplay);
		extras.addElement(newPj);
		return newPj;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Node or Branch-Associated Values";
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		if (on.getValue()){
			temp.addLine("setOn " + on.toOffOnString());
			for (int i=0; i< names.size(); i++)
				temp.addLine("toggleShow " + StringUtil.tokenize(((Listable)names.elementAt(i)).getName()));
			temp.addLine("setDigits " + digits); 
			temp.addLine("writeAsPercentage " + percentage.toOffOnString());
			temp.addLine("toggleCentred " + centred.toOffOnString());
//			temp.addLine("setPositionAlongBranch " + positionAlongBranch); 
			temp.addLine("toggleHorizontal " + horizontal.toOffOnString());
			temp.addLine("toggleWhiteEdges " + whiteEdges.toOffOnString());
			temp.addLine("toggleShowOnTerminals " + showOnTerminals.toOffOnString());
			temp.addLine("setFontSize " + fontSize); 
			temp.addLine("setOffset " + xOffset + "  " + yOffset); 
		}
		return temp;
	}
	/*.................................................................................................................*
	public void queryLocationAlongBranch(){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog settingsDialog = new ExtensibleDialog(containerOfModule(), "Location Along Branch",  buttonPressed);
		RadioButtons rb = settingsDialog.addRadioButtons(new String[]{"Just apical to node","Just basal to node","Centered on branch"}, positionAlongBranch.getValue());
		settingsDialog.completeAndShowDialog(true);
		boolean ok = (settingsDialog.query()==0);
		
		if (ok) {
			positionAlongBranch.setValue(rb.getValue());
			
		}
		settingsDialog.dispose();
	 	storePreferences();
	}
	/*.................................................................................................................*/
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets whether to show the node or branch associated values", "[on or off]", commandName, "setOn")) {  //on always except if scripted off
			if (StringUtil.blank(arguments))
				on.setValue(!on.getValue());
			else
				on.toggleValue(parser.getFirstToken(arguments));
			for (int i =0; i<extras.size(); i++){
				NodeAssocValuesExtra e = (NodeAssocValuesExtra)extras.elementAt(i);
				e.setOn(on.getValue());
			}

		}
		else if (checker.compare(this.getClass(), "Shows dialog box to choose what values to display", null, commandName, "chooseValues")) {
			if (extras.size() == 0)
				return null;
			showChoiceDialog((Associable)((NodeAssocValuesExtra)extras.elementAt(0)).lastTree, names);
		}
		else if (checker.compare(this.getClass(), "Sets whether to write the values as percentage", "[on or off]", commandName, "writeAsPercentage")) {
			if (StringUtil.blank(arguments))
				percentage.setValue(!percentage.getValue());
			else
				percentage.toggleValue(parser.getFirstToken(arguments));
			if (!MesquiteThread.isScripting()) parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets whether to write the values with horizontally", "[on or off]", commandName, "toggleHorizontal")) {
			if (StringUtil.blank(arguments))
				horizontal.setValue(!horizontal.getValue());
			else
				horizontal.toggleValue(parser.getFirstToken(arguments));
			if (!MesquiteThread.isScripting()) parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets whether to write the values white edges", "[on or off]", commandName, "toggleWhiteEdges")) {
			if (StringUtil.blank(arguments))
				whiteEdges.setValue(!whiteEdges.getValue());
			else
				whiteEdges.toggleValue(parser.getFirstToken(arguments));
			if (!MesquiteThread.isScripting()) parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets whether to show the values on the terminal branches", "[on or off]", commandName, "toggleShowOnTerminals")) {
			if (StringUtil.blank(arguments))
				showOnTerminals.setValue(!showOnTerminals.getValue());
			else
				showOnTerminals.toggleValue(parser.getFirstToken(arguments));
			if (!MesquiteThread.isScripting()) parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets whether to write the values centrally over the branches", "[on or off]", commandName, "toggleCentred")) {
			if (StringUtil.blank(arguments))
				centred.setValue(!centred.getValue());
			else
				centred.toggleValue(parser.getFirstToken(arguments));
			if (!MesquiteThread.isScripting()) parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets how many digits are shown", "[number of digits]", commandName, "setDigits")) {
			int newWidth= MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newWidth))
				newWidth = MesquiteInteger.queryInteger(containerOfModule(), "Set number of digits", "Number of digits (after decimal point) to display for values on tree:", digits, 0, 24);
			if (newWidth>=0 && newWidth<24 && newWidth!=digits) {
				digits = newWidth;
				if (!MesquiteThread.isScripting()) parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Set's to David's style", "", commandName, "setCorvallisStyle")) {
			whiteEdges.setValue(false);
			percentage.setValue(true);
			centred.setValue(false);
			horizontal.setValue(true);
			digits=0;
			xOffset = 0;
			yOffset = 9;
			if (!MesquiteThread.isScripting()) parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets offset of label from nodes", "[offsetX] [offsetY]", commandName, "setOffset")) {
			int newX= MesquiteInteger.fromFirstToken(arguments, pos);
			int newY= MesquiteInteger.fromString(arguments, pos);
			if (!MesquiteInteger.isCombinable(newX)){
				MesquiteBoolean answer = new MesquiteBoolean(false);
				MesquiteInteger nX = new MesquiteInteger(xOffset);
				MesquiteInteger nY = new MesquiteInteger(yOffset);
				MesquiteInteger.queryTwoIntegers(containerOfModule(), "Location of Values", "X offset from node", "Y offset from node", answer, nX, nY,-200,200,-200,200, null);
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
					NodeAssocValuesExtra e = (NodeAssocValuesExtra)extras.elementAt(i);
					e.resetFontSize();
				}
				if (!MesquiteThread.isScripting()) parametersChanged();
			}
		}
/*		else if (checker.compare(this.getClass(), "Sets the location of the value along the branch", "[location constant]", commandName, "setPositionAlongBranch")) {
			int newPos= MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newPos))
				queryLocationAlongBranch();
			else
				positionAlongBranch.setValue(newPos);
			
			if (!MesquiteThread.isScripting()) parametersChanged();
		}
*/		
		
		else if (checker.compare(this.getClass(), "Sets whether to show a node or branch associated value", "[on or off]", commandName, "toggleShow")) {
			String name = parser.getFirstToken(arguments);
			if (isShowing(name)){
				names.removeElementAt(names.indexOfByName(name), false);
			}
			else
				names.addElement(new MesquiteString(name, name), false);

			for (int i =0; i<extras.size(); i++){
				NodeAssocValuesExtra e = (NodeAssocValuesExtra)extras.elementAt(i);
				e.update();
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	void showChoiceDialog(Associable tree, ListableVector names) {
		if (tree == null)
			return;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ListableVector v = new ListableVector();
		int num = tree.getNumberAssociatedDoubles();
		boolean[] shown = new boolean[num + names.size()]; //bigger than needed probably
		for (int i = 0; i< num; i++){
			DoubleArray da = tree.getAssociatedDoubles(i);
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
			alert("This Tree has no values associated with nodes");
		else {
			ExtensibleDialog queryDialog = new ExtensibleDialog(containerOfModule(), "Values to show",  buttonPressed);
			queryDialog.addLabel("Values to display on tree", Label.CENTER);
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
					NodeAssocValuesExtra e = (NodeAssocValuesExtra)extras.elementAt(i);
					e.setOn(on.getValue());
				}
			}

			queryDialog.dispose();
		}
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Shows values attached to nodes on the tree." ;
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
class NodeAssocValuesExtra extends TreeDisplayExtra  {
	DrawTreeAssocDoubles assocDoublesModule;
	MesquiteCommand taxonCommand, branchCommand;
	boolean on;
	Tree lastTree = null;
//	StringInABox
	public NodeAssocValuesExtra (DrawTreeAssocDoubles ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		assocDoublesModule = ownerModule;
		on = assocDoublesModule.on.getValue();
		resetFontSize();
	}
	StringInABox box = new StringInABox( "", treeDisplay.getFont(),1500);
	public void resetFontSize(){
		Font f = treeDisplay.getFont();
		box.setFont(new Font(f.getName(),f.getStyle(), assocDoublesModule.fontSize)); 
	}
	/*.................................................................................................................*/
	public   void myDraw(Tree tree, int node, Graphics g, DoubleArray[] arrays) {
		if (!assocDoublesModule.showOnTerminals.getValue() && tree.nodeIsTerminal(node))
			return;
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			myDraw(tree, d, g, arrays);
		for (int i=0; i<arrays.length; i++){
			double d = arrays[i].getValue(node);
			if (MesquiteDouble.isCombinable(d)){
				if (assocDoublesModule.percentage.getValue())
					d *= 100;
				if (assocDoublesModule.whiteEdges.getValue())
					box.setColors(Color.black, Color.white);
				else
					box.setColors(Color.black, null);
				box.setString(MesquiteDouble.toStringDigitsSpecified(d, assocDoublesModule.digits));

				double x, y;
				if (assocDoublesModule.centred.getValue()){   // center on branch
					double centreBranchX = treeDisplay.getTreeDrawing().getBranchCenterX(node) + assocDoublesModule.xOffset;
					double centreBranchY =  treeDisplay.getTreeDrawing().getBranchCenterY(node)+ assocDoublesModule.yOffset;
					/*g.setColor(Color.yellow);
					g.drawLine(treeDisplay.getTreeDrawing().lineBaseX[node], treeDisplay.getTreeDrawing().lineBaseY[node], treeDisplay.getTreeDrawing().lineTipX[node], treeDisplay.getTreeDrawing().lineTipY[node]);
					/*g.setColor(Color.red);
					g.drawRect(centreBranchX-10, centreBranchY-10, 20, 20);
					g.drawString(Integer.toString(node), centreBranchX, centreBranchY);*/
					int stringWidth = box.getMaxWidthMunched();
					if (assocDoublesModule.horizontal.getValue()){
						x = centreBranchX - stringWidth/2;
						y = centreBranchY - assocDoublesModule.fontSize;
					}
					else {
						x = centreBranchX - assocDoublesModule.fontSize*2;
						y = centreBranchY + stringWidth/2;
					}
				}
				else {
					int stringWidth = box.getMaxWidthMunched();
					x= treeDisplay.getTreeDrawing().getNodeValueTextBaseX(node, treeDisplay.getTreeDrawing().getEdgeWidth(), stringWidth, assocDoublesModule.fontSize, assocDoublesModule.horizontal.getValue()) + assocDoublesModule.xOffset;
					y = treeDisplay.getTreeDrawing().getNodeValueTextBaseY(node, treeDisplay.getTreeDrawing().getEdgeWidth(), stringWidth,assocDoublesModule.fontSize,assocDoublesModule.horizontal.getValue()) + assocDoublesModule.yOffset;
				}
/*				else {
					x= treeDisplay.getTreeDrawing().x[node] + assocDoublesModule.xOffset;
					y = treeDisplay.getTreeDrawing().y[node] + assocDoublesModule.yOffset + i*assocDoublesModule.fontSize*2;
				}
	
*/
				if (assocDoublesModule.horizontal.getValue())
					box.draw(g,  x, y);
				else
					box.draw(g,  x, y, 0, 1500, treeDisplay, false, false);

			}
		}

	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int node, Graphics g) {
		if (!on)
			return;
		int num = tree.getNumberAssociatedDoubles();
		int total = 0;
		for (int i = 0; i< num; i++){
			DoubleArray da = tree.getAssociatedDoubles(i);
			if (assocDoublesModule.isShowing(da.getName()))
				total++;
		}
		DoubleArray[] arrays = new DoubleArray[total];
		int count = 0;
		for (int i = 0; i< num; i++){
			DoubleArray da = tree.getAssociatedDoubles(i);
			if (assocDoublesModule.isShowing(da.getName()))
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
		if (tree.getNumberAssociatedDoubles() == 0 || tree.getWhichAssociatedDouble(assocValueRef)==null)
			return "";
		double d = getValue(tree, node);
		return MesquiteDouble.toString(d);
	}
	/**return a text version of information on tree, displayed on a text version of the tree*/
	public String writeOnTree(Tree tree, int node){
		if (!on || tree.getNumberAssociatedDoubles() == 0)
			return null;
		return super.writeOnTree(tree, node);
	}
	/**return a text version of information on tree, displayed as list of nodes with information at each*/
	public String infoAtNodes(Tree tree, int node){
		if (!on || tree.getNumberAssociatedDoubles() == 0)
			return null;
		return super.infoAtNodes(tree, node);
	}
	/**return a table version of information on tree, displayed as list of nodes with information at each*/
	public String tableAtNodes(Tree tree, int node){
		if (!on || tree.getNumberAssociatedDoubles() == 0)
			return null;
		return super.tableAtNodes(tree, node);
	}
	MesquitePopup popup=null;
	/*.................................................................................................................*/
	void redoMenu(Associable tree) {

		if (popup==null)
			popup = new MesquitePopup(treeDisplay);
		popup.removeAll();
		popup.add(new MesquiteMenuItem("Display Node or Branch-Associated Values", null, null));
		popup.add(new MesquiteMenuItem("-", null, null));
		int num = tree.getNumberAssociatedDoubles();
		if (num == 0)
			popup.add(new MesquiteMenuItem("This Tree has no values associated with nodes or brances", null, null));
		else 
			for (int i = 0; i< num; i++){
				DoubleArray da = tree.getAssociatedDoubles(i);
				MesquiteCommand mc = new MesquiteCommand("toggleShow", assocDoublesModule);
				String selName = " ";
				if (assocDoublesModule.isShowing(da.getName()))
					selName = da.getName();
				popup.add(new MesquiteCheckMenuItem(da.getName(), assocDoublesModule, mc, StringUtil.tokenize(da.getName()), new MesquiteString(selName )));
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
	double getValue(Tree tree, int node){
		return tree.getAssociatedDouble(assocValueRef, node);
	}

	public void turnOff() {
		assocDoublesModule.extras.removeElement(this);
		super.turnOff();
	}
}



