/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.NodeAssociatesZDisplayControl;
/*~~  */


import java.awt.Checkbox;
import java.awt.Color;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.util.Enumeration;
import java.util.Vector;

import mesquite.lib.*;
import mesquite.lib.duties.TreeDisplayAssistantDI;
import mesquite.lib.duties.TreeDisplayAssistantI;
import mesquite.lib.duties.TreeWindowMaker;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDisplayExtra;
import mesquite.lib.tree.TreeTool;
import mesquite.lib.ui.DoubleField;
import mesquite.lib.ui.ListDialog;
import mesquite.lib.ui.MesquiteCheckMenuItem;
import mesquite.lib.ui.MesquiteMenuItem;
import mesquite.lib.ui.MesquitePopup;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lib.ui.StringInABox;

/* ======================================================================== */
public class NodeAssociatesZDisplayControl extends TreeDisplayAssistantI {
	public Vector extras;

	MesquiteBoolean horizontal, centred, whiteEdges, showOnTerminals, showNames;

	//For double values
	MesquiteDouble thresholdValueToShow;
	MesquiteBoolean percentage;


	MesquiteSubmenuSpec positionSubMenu;
	public static final boolean CENTEREDDEFAULT = false;
	ListableVector names;
	Bits selected;
	static boolean asked= false;
	int digits = 4;
	int fontSize = 10;
	int xOffset = 0;
	int yOffset = 0;

	static final int BITS = 0;
	static final int DOUBLES = 1;
	static final int LONGS = 2;
	static final int OBJECTS = 3;

	MesquiteTree tree;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		findEmployerWithDuty(TreeWindowMaker.class).addMenuItem(null, "Display Node/Branch Properties on Tree...", makeCommand("showDialog", this));
		extras = new Vector();
		names = new ListableVector();
		selected = new Bits(0);
		//if (!MesquiteThread.isScripting())
		thresholdValueToShow = new MesquiteDouble();
		percentage = new MesquiteBoolean(false);
		horizontal = new MesquiteBoolean(true);
		centred = new MesquiteBoolean(CENTEREDDEFAULT);
		whiteEdges = new MesquiteBoolean(true);
		showOnTerminals = new MesquiteBoolean(true);
		showNames = new MesquiteBoolean(true);


		return true;
	}

	/*.................................................................................................................*/
	void setTree(MesquiteTree tree){
		this.tree = tree;
		addAssociatesToNames(tree);
	}

	private void addAssociateName(String name, boolean show){
		if (StringUtil.blank(name) || "!color".equalsIgnoreCase(name))
			return;
		if (names.indexOfByName(name)<0){  //color is not available to be shown in this way
			names.addElement(new MesquiteString(name), false);
			selected.resetSize(selected.getSize()+1);
			selected.setBit(selected.getSize()-1, show);
		}
		else
			selected.setBit(names.indexOfByName(name), show);
	}
	private void addAssociatesToNames(MesquiteTree tree){
		if (tree == null)
			return;
		String[] assocNames = tree.getAssociatesNames();
		if (assocNames == null)
			return;
		for (int i=0; i<assocNames.length; i++){
			if (!"!color".equalsIgnoreCase(assocNames[i]) && names.indexOfByName(assocNames[i])<0){  //color is not available to be shown in this way
				names.addElement(new MesquiteString(assocNames[i]), false);
				selected.resetSize(selected.getSize()+1);
			}
		}
	}

	/*.................................................................................................................*/
	/*.................................................................................................................*/

	public boolean queryDialog(){
		if (names==null)
			return false;
		Listable[] namesAsArray = names.getListables();
		String helpString = "xxxx";  //add here notes on threashold
		ListDialog dialog = new ListDialog(containerOfModule(), "Node/Branch properties", "What properties to show and how to show them?", false,helpString, namesAsArray, 6, null, "OK",null,false, true);
		//		if (okButton!=null)
		//		id.ok.setLabel(okButton);
		dialog.getList().setMultipleMode(true);

		IntegerField fontSizeF = dialog.addIntegerField("Font Size", fontSize, 6, 2, 100);
		Checkbox showNamesF = dialog.addCheckBox("Show Names", showNames.getValue());
		Checkbox whiteEdgesF = dialog.addCheckBox("White Edges", whiteEdges.getValue());
		Checkbox showOnTerminalsF = dialog.addCheckBox("Show on Terminal Branches", showOnTerminals.getValue());
		Checkbox centredF = dialog.addCheckBox("Centered on Branch", centred.getValue());
		//dialog.addIntegerField("XXXPosition Along Branch...", 12);
		IntegerField xOffsetF = dialog.addIntegerField("Horizontal Offset (pixels, more is to right)", xOffset, 6);
		IntegerField yOffsetF = dialog.addIntegerField("Vertical Offset (pixels, more is lower)", yOffset, 6);
		Checkbox horizontalF = dialog.addCheckBox("Horizontal", horizontal.getValue());
		dialog.addHorizontalLine(1);
		dialog.addLabel("For decimal numbers");
		IntegerField digitsF = dialog.addIntegerField("Digits", digits, 8, 0, 8);
		Checkbox percentageF = dialog.addCheckBox("Percentage", percentage.getValue());
		DoubleField thresholdValueToShowF = dialog.addDoubleField("Threshold Value", thresholdValueToShow.getValue(), 8);

		boolean[] toShow = selected.getAsArray();
		dialog.setSelected(toShow);
		dialog.addHorizontalLine(1);


		dialog.completeAndShowDialog(true);
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		if (dialog.query()==1)  {
			IntegerArray result = dialog.getIndicesSelected();
			if (result==null || result.getSize()==0) {
				dialog.dispose();
				return false;
			}
			if (result!=null) {
				selected.clearAllBits();
				for (int i = 0; i<result.getSize(); i++){
					int sel = result.getValue(i);
					if (sel>=0 && sel<selected.getSize()){
						selected.setBit(sel, true);
					}
				}
			}
			Debugg.println("Query accepted " + dialog.query());
			int oldFontSize = fontSize;
			fontSize = fontSizeF.getValue();
			if (oldFontSize != fontSize)
				for (int i =0; i<extras.size(); i++){
					NodeAssocDisplayExtra e = (NodeAssocDisplayExtra)extras.elementAt(i);
					e.resetFontSize();
				}
			digits = digitsF.getValue();
			thresholdValueToShow.setValue(thresholdValueToShowF.getValue());
			xOffset = xOffsetF.getValue();
			yOffset = yOffsetF.getValue();
			whiteEdges.setValue(whiteEdgesF.getState());
			showOnTerminals.setValue(showOnTerminalsF.getState());
			showNames.setValue(showNamesF.getState());
			centred.setValue(centredF.getState());
			horizontal.setValue(horizontalF.getState());
			percentage.setValue(percentageF.getState());
			storePreferences();
			parametersChanged();
		}
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		dialog.dispose();

		return true;
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		for (int i=0; i< names.size(); i++)
			if (isShowing(((Listable)names.elementAt(i)).getName()))
				temp.addLine("showAssociate " + StringUtil.tokenize(((Listable)names.elementAt(i)).getName()));
		temp.addLine("setDigits " + digits); 
		temp.addLine("setThreshold " + thresholdValueToShow); 
		temp.addLine("writeAsPercentage " + percentage.toOffOnString());
		temp.addLine("toggleCentred " + centred.toOffOnString());
		temp.addLine("toggleHorizontal " + horizontal.toOffOnString());
		temp.addLine("toggleWhiteEdges " + whiteEdges.toOffOnString());
		temp.addLine("toggleShowOnTerminals " + showOnTerminals.toOffOnString());
		temp.addLine("toggleShowNames " + showNames.toOffOnString());
		temp.addLine("setFontSize " + fontSize); 
		temp.addLine("setOffset " + xOffset + "  " + yOffset); 

		return temp;
	}
	/*.................................................................................................................*/
	MesquiteInteger pos = new MesquiteInteger();
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Shows the dialog to choose display options", "[]", commandName, "showDialog")) {
			queryDialog();
		}
		else if (checker.compare(this.getClass(), "Sets whether to show a node or branch associated value", "[on or off]", commandName, "showAssociate")) {
			String name = parser.getFirstToken(arguments);
			addAssociateName(name, true);
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
		else if (checker.compare(this.getClass(), "Sets whether to write the names of the variables are written", "[on or off]", commandName, "toggleShowNames")) {
			if (StringUtil.blank(arguments))
				showNames.setValue(!showNames.getValue());
			else
				showNames.toggleValue(parser.getFirstToken(arguments));
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
			if (newWidth>=0 && newWidth<24 && newWidth!=digits) {
				digits = newWidth;
				if (!MesquiteThread.isScripting()) parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets threshold value - values have to be above this to be shown", "[value]", commandName, "setThreshold")) {
			String value = parser.getFirstToken(arguments);
			double newThreshold = MesquiteDouble.unassigned;
			if (!"off".equalsIgnoreCase(value) && "?"!=value){
				newThreshold= MesquiteDouble.fromString(value);
				if (!MesquiteDouble.isCombinable(newThreshold) && !MesquiteThread.isScripting())
					newThreshold = MesquiteDouble.queryDouble(containerOfModule(), "Set threshold value", "Only show values above this threshold:", "Remember to enter the threshold in its native format.  For example, if percentages are being shown, "+
							" and you wish to have a threshold of 50%, then enter 0.5. To turn off the threshold, enter ?",thresholdValueToShow.getValue());
			}
			if (newThreshold!=thresholdValueToShow.getValue()) {
				thresholdValueToShow.setValue(newThreshold);
				if (!MesquiteThread.isScripting()) parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Set's to David's style", "", commandName, "setCorvallisStyle")) {
			whiteEdges.setValue(false);
			percentage.setValue(true);
			centred.setValue(false);
			horizontal.setValue(true);
			thresholdValueToShow.setToUnassigned();
			digits=0;
			xOffset = -2;
			yOffset = 9;
			if (!MesquiteThread.isScripting()) parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets offset of label from nodes", "[offsetX] [offsetY]", commandName, "setOffset")) {
			int newX= MesquiteInteger.fromFirstToken(arguments, pos);
			int newY= MesquiteInteger.fromString(arguments, pos);
			if (newX>-200 && newX <200 && newY>-200 && newY <200 && (newX!=xOffset ||   newY!=yOffset)) {
				xOffset = newX;
				yOffset = newY;
				if (!MesquiteThread.isScripting()) parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets font size", "[font size]", commandName, "setFontSize")) {
			int newWidth= MesquiteInteger.fromFirstToken(arguments, pos);
			if (newWidth>1 && newWidth<96 && newWidth!=fontSize) {
				fontSize = newWidth;
				for (int i =0; i<extras.size(); i++){
					NodeAssocDisplayExtra e = (NodeAssocDisplayExtra)extras.elementAt(i);
					e.resetFontSize();
				}
				if (!MesquiteThread.isScripting()) parametersChanged();
			}
		}

		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public boolean isShowing(String name){
		//	boolean s = 
		int nameIndex = names.indexOfByName(name);
		if (nameIndex<0)
			return false;
		return selected.isBitOn(nameIndex);

		//	return s;
	}
	/*.................................................................................................................*/
	boolean anyShowing(){
		//	boolean s = 
		return (selected.anyBitsOn() && names.size()>0);
		//	return s;
	}


	/*.................................................................................................................*/
	public String getName() {
		return "Show Node/Branch Properties on Tree";
	}
	public String getExplanation() {
		return "Controls display of Node/Branch properties on the tree." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return MesquiteModule.NEXTRELEASE;  
	}

	public Class getDutyClass() {
		return getClass();
	}


	public TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		NodeAssocDisplayExtra extra = new NodeAssocDisplayExtra(this, treeDisplay);
		extras.addElement(extra);
		return extra;
	}

}


/* ======================================================================== */
class NodeAssocDisplayExtra extends TreeDisplayExtra implements Commandable {
	NodeAssociatesZDisplayControl controlModule;
	MesquiteCommand taxonCommand, branchCommand, respondCommand;
	MesquiteTree myTree = null;
	StringInABox box = new StringInABox( "", treeDisplay.getFont(),1500);
	TreeTool infoTool;

	/*.--------------------------------------------------------------------------------------..*/
	public NodeAssocDisplayExtra (NodeAssociatesZDisplayControl ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		controlModule = ownerModule;
		infoTool = new TreeTool(this, "BranchInfo", ownerModule.getPath(), "branchInfo.gif", 5,2,"Branch Info", "This tool is used to show information about a branch.");
		infoTool.setTouchedCommand(MesquiteModule.makeCommand("showPopup",  this));
		if (ownerModule.containerOfModule() instanceof MesquiteWindow)
			((MesquiteWindow)ownerModule.containerOfModule()).addTool(infoTool);
			
		respondCommand = ownerModule.makeCommand("respond", this);
		resetFontSize();
	}

	String stringAtNode(MesquiteTree tree, int node, boolean showingAll, boolean showNames, boolean includeLineBreaks){
		String[] strings = stringsAtNode(tree, node, showingAll, showNames, null);
		if (strings == null)
			return "";
		String singleString = "";
		String separator = " ";
		if (includeLineBreaks)
			separator = "\n";
		boolean first = true;
		for (int i = 0; i<strings.length; i++){
			if (!first)
				singleString += separator;
			first = false;
			singleString += strings[i];
		}
		return singleString;
	}

	/*.................................................................................................................*/
	String[] stringsAtNode(MesquiteTree tree, int node, boolean showingAll, boolean showNames, Vector nameCodes){
		Vector nodeStrings = new Vector();
		int num = tree.getNumberAssociatedBits();
		for (int i = 0; i< num; i++){
			Bits da = tree.getAssociatedBits(i);
			if (showingAll || controlModule.isShowing(da.getName())){
				boolean d = tree.getAssociatedBit(NameReference.getNameReference(da.getName()), node);  //Debugg.println save vector of name references
				String nodeString = "";
				if (showNames)
					nodeString += da.getName()+ ": ";
				nodeString += MesquiteBoolean.toTrueFalseString(d);
				nodeStrings.addElement(nodeString);
				if (nameCodes != null)
					nameCodes.addElement(new MesquiteInteger(da.getName(), controlModule.BITS));
			}
		}
		num = tree.getNumberAssociatedDoubles();
		for (int i = 0; i< num; i++){
			DoubleArray da = tree.getAssociatedDoubles(i);
			if (showingAll || controlModule.isShowing(da.getName())){
				double d = tree.getAssociatedDouble(NameReference.getNameReference(da.getName()), node); 
				if (MesquiteDouble.isCombinable(d) && (!controlModule.thresholdValueToShow.isCombinable() || (d>=controlModule.thresholdValueToShow.getValue()))){
					if (controlModule.percentage.getValue())
						d *= 100;
					String nodeString = "";
					if (showNames)
						nodeString += da.getName()+ ": ";
					nodeString += MesquiteDouble.toStringDigitsSpecified(d, controlModule.digits) + "\n";
					nodeStrings.addElement(nodeString);
					if (nameCodes != null)
						nameCodes.addElement(new MesquiteInteger(da.getName(), controlModule.DOUBLES));
				}
			}
		}
		num = tree.getNumberAssociatedLongs();
		for (int i = 0; i< num; i++){
			LongArray da = tree.getAssociatedLongs(i);
			if (showingAll || controlModule.isShowing(da.getName())){
				long d = tree.getAssociatedLong(NameReference.getNameReference(da.getName()), node);  //Debugg.println save vector
				String nodeString = "";
				if (showNames)
					nodeString += da.getName()+ ": ";
				nodeString += MesquiteLong.toString(d) + "\n";
				nodeStrings.addElement(nodeString);
				if (nameCodes != null)
					nameCodes.addElement(new MesquiteInteger(da.getName(), controlModule.LONGS));
			}
		}
		num = tree.getNumberAssociatedObjects();
		for (int i = 0; i< num; i++){
			ObjectArray da = tree.getAssociatedObjects(i);
			if (showingAll || controlModule.isShowing(da.getName())){
				Object d = tree.getAssociatedObject(NameReference.getNameReference(da.getName()), node);  //Debugg.println save vector
				if (d!= null){
					String nodeString = "";
					if (showNames)
						nodeString += da.getName()+ ": ";
					nodeString += d.toString() + "\n";
					nodeStrings.addElement(nodeString);
					if (nameCodes != null)
						nameCodes.addElement(new MesquiteInteger(da.getName(), controlModule.OBJECTS));
				}
			}
		}
		String[] strings = new String[nodeStrings.size()];
		for (int i= 0; i<nodeStrings.size(); i++)
			strings[i] = (String)nodeStrings.elementAt(i);
		return strings;
	}
	/*.................................................................................................................*/
	public void cursorTouchBranch(Tree tree, int N, Graphics g, int modifiers, boolean isArrowTool){
		if (MesquiteEvent.rightClick(modifiers) && isArrowTool){
			showPopup(N);
		}
	}
	/*.................................................................................................................*/
	MesquitePopup popup;
	Vector popupKeys = new Vector();
	MesquiteInteger pos = new MesquiteInteger();
	void addToPopup(String s, int node, int response){
		if (popup==null)
			return;
		popup.addItem(s, ownerModule, respondCommand, Integer.toString(node) + " " + Integer.toString(response));
	}
	void showPopup(int branchFound){
		if (popup==null)
			popup = new MesquitePopup(treeDisplay);
		popup.removeAll();
		popupKeys.removeAllElements();
		int responseNumber = 0;
		String[] strings = stringsAtNode(myTree, branchFound, true, true, popupKeys);
		addToPopup("Branch/node number: " + branchFound, branchFound, responseNumber++);
		addToPopup("-", branchFound, responseNumber++);
		addToPopup("Length: " + MesquiteDouble.toString(myTree.getBranchLength(branchFound)), branchFound, responseNumber++);
		if (strings != null)
			for (int i = 0; i<strings.length; i++)
			addToPopup(strings[i], branchFound, responseNumber++);
		popup.showPopup((int)treeDisplay.getTreeDrawing().x[branchFound], (int)treeDisplay.getTreeDrawing().y[branchFound]);
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 

		if (checker.compare(this.getClass(), "Shows popup menu with information about the branch", "[branch number]", commandName, "showPopup")) {
			int branchFound= MesquiteInteger.fromFirstToken(arguments, pos);
			if (branchFound >0 && MesquiteInteger.isCombinable(branchFound)) {
				showPopup(branchFound);
			}	 	
		}
		return null;
	}
	/*.................................................................................................................*/
	void myDraw(MesquiteTree tree, int node, Graphics g) {
		if (!controlModule.showOnTerminals.getValue() && tree.nodeIsTerminal(node))
			return;
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			myDraw(tree, d, g);
		String nodeString = stringAtNode(tree, node, false, controlModule.showNames.getValue(), true);
		if (controlModule.whiteEdges.getValue())
			box.setColors(Color.black, Color.white);
		else
			box.setColors(Color.black, null);
		box.setString(nodeString);

		double x, y;
		if (controlModule.centred.getValue()){   // center on branch
			double centreBranchX = treeDisplay.getTreeDrawing().getBranchCenterX(node) + controlModule.xOffset;
			double centreBranchY =  treeDisplay.getTreeDrawing().getBranchCenterY(node)+ controlModule.yOffset;
			int stringWidth = box.getMaxWidthMunched();
			if (controlModule.horizontal.getValue()){
				x = centreBranchX - stringWidth/2;
				y = centreBranchY - controlModule.fontSize;
			}
			else {
				x = centreBranchX - controlModule.fontSize*2;
				y = centreBranchY + stringWidth/2;
			}
		}
		else {
			int stringWidth = box.getMaxWidthMunched();
			x= treeDisplay.getTreeDrawing().getNodeValueTextBaseX(node, treeDisplay.getTreeDrawing().getEdgeWidth(), stringWidth, controlModule.fontSize, controlModule.horizontal.getValue()) + controlModule.xOffset;
			y = treeDisplay.getTreeDrawing().getNodeValueTextBaseY(node, treeDisplay.getTreeDrawing().getEdgeWidth(), stringWidth,controlModule.fontSize,controlModule.horizontal.getValue()) + controlModule.yOffset;
		}

		if (controlModule.horizontal.getValue())
			box.draw(g,  x, y);
		else
			box.draw(g,  x, y, 0, 1500, treeDisplay, false, false);

	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int node, Graphics g) {
		if (!controlModule.anyShowing())
			return;
		int num = tree.getNumberAssociatedDoubles();
		int total = 0;
		for (int i = 0; i< num; i++){
			DoubleArray da = tree.getAssociatedDoubles(i);
			if (controlModule.isShowing(da.getName()))
				total++;
		}
		DoubleArray[] arrays = new DoubleArray[total];
		int count = 0;
		for (int i = 0; i< num; i++){
			DoubleArray da = tree.getAssociatedDoubles(i);
			if (controlModule.isShowing(da.getName()))
				arrays[count++] = da;
		}

		myDraw((MesquiteTree)tree, node, g);

	}
	/*.................................................................................................................*/

	void update(){
		treeDisplay.pleaseUpdate(false);
	}
	/*.--------------------------------------------------------------------------------------..*/
	public void resetFontSize(){
		Font f = treeDisplay.getFont();
		box.setFont(new Font(f.getName(),f.getStyle(), controlModule.fontSize)); 
	}
	/*.................................................................................................................*/

	/**return a text version of information at node*/
	public String textAtNode(Tree tree, int node){
		if (!controlModule.anyShowing())
			return "";
		int num = tree.getNumberAssociatedDoubles();
		if (num == 0)
			return "";
		String s = "";
		boolean first = true;
		for (int i = 0; i< num; i++){
			DoubleArray da = tree.getAssociatedDoubles(i);
			if (controlModule.isShowing(da.getName())){
				double d = da.getValue(node);
				if (!first)
					s += ", " + MesquiteDouble.toString(d);
				first = false;
			}
		}
		return s;
	}
	/*.................................................................................................................*/
	/**return a text version of information on tree, displayed on a text version of the tree*/
	public String writeOnTree(Tree tree, int node){
		if (!controlModule.anyShowing() || tree.getNumberAssociatedDoubles() == 0)
			return null;
		return super.writeOnTree(tree, node);
	}
	/*.................................................................................................................*/
	/**return a text version of information on tree, displayed as list of nodes with information at each*/
	public String infoAtNodes(Tree tree, int node){
		if (!controlModule.anyShowing() || tree.getNumberAssociatedDoubles() == 0)
			return null;
		return super.infoAtNodes(tree, node);
	}
	/*.................................................................................................................*/
	/**return a table version of information on tree, displayed as list of nodes with information at each*/
	public String tableAtNodes(Tree tree, int node){
		if (!controlModule.anyShowing() || tree.getNumberAssociatedDoubles() == 0)
			return null;
		return super.tableAtNodes(tree, node);
	}

	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		if (!controlModule.anyShowing())
			return;
		drawOnTree(tree, drawnRoot, g); //should draw numbered footnotes!
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		myTree = (MesquiteTree)tree;
		controlModule.setTree((MesquiteTree)tree);
	}
	
	public void turnOff() {
		controlModule.extras.removeElement(this);
		super.turnOff();
	}
}

