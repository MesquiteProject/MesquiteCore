/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.BranchPropertyDisplayControl;
/*~~  */


import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.accessibility.AccessibleContext;
import javax.swing.JLabel;

import mesquite.lib.*;
import mesquite.lib.duties.DrawNamesTreeDisplay;
import mesquite.lib.duties.TreeDisplayAssistantDI;
import mesquite.lib.duties.TreeDisplayAssistantI;
import mesquite.lib.duties.TreeWindowMaker;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.DisplayableBranchProperty;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDisplayEarlyExtra;
import mesquite.lib.tree.TreeDisplayExtra;
import mesquite.lib.tree.TreeDisplayLateExtra;
import mesquite.lib.tree.TreeDisplayRequests;
import mesquite.lib.tree.TreeDrawing;
import mesquite.lib.tree.TreeTool;
import mesquite.lib.ui.AlertDialog;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.DoubleClickList;
import mesquite.lib.ui.DoubleField;
import mesquite.lib.ui.ListDialog;
import mesquite.lib.ui.MQJLabel;
import mesquite.lib.ui.MesquiteCheckMenuItem;
import mesquite.lib.ui.MesquiteMenuItem;
import mesquite.lib.ui.MesquitePopup;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lib.ui.StringInABox;
import mesquite.trees.BranchPropertiesAManager.BranchPropertiesAManager;

/* ======================================================================== */
public class BranchPropertyDisplayControl extends TreeDisplayAssistantI implements ActionListener, ItemListener, TextListener {
	public Vector extras;

//	MesquiteSubmenuSpec positionSubMenu;
//	String[] reservedNames = new String[]{"!color"};
//	String[] builtInNames = new String[]{MesquiteTree.branchLengthName, MesquiteTree.nodeLabelName};

//	ListableVector propertyList;
	static boolean asked= false;

	boolean moduleIsNaive = true; //so as not to save the snapshot
	ListableVector propertyList;
	BranchPropertiesAManager managerModule;
	MesquiteTree tree;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		TreeWindowMaker twMB = (TreeWindowMaker)findEmployerWithDuty(TreeWindowMaker.class);
		if (twMB!= null){
			twMB.addMenuItem(null, "Display Branch/Node Properties on Tree...", makeCommand("showDialog", this));
			propertyList = twMB.getBranchPropertiesList();
		}
		managerModule = (BranchPropertiesAManager)findNearestColleagueWithDuty(BranchPropertiesAManager.class);
		extras = new Vector();
		return true;
	}
	public void endJob(){
			
		if (this.tree != null)
			this.tree.removeListener(this);

		super.endJob();
	}
	/*.................................................................................................................*/
	void setTree(MesquiteTree tree){
		if (this.tree != null)
			this.tree.removeListener(this);
		this.tree = tree;
		this.tree.addListener(this);

//		addPropertiesToList(tree);
	}
	public void changed(Object caller, Object obj, Notification notification){
		if (caller == tree || obj == tree){
			setTree(tree);
		}
	}
	
	/*.========================================================..**/
	DisplayableBranchProperty findInList(String s, int kind){
		if (propertyList.indexOfByName(s)<0)
			return null;
		for (int i=0; i<propertyList.size(); i++){
			DisplayableBranchProperty mi = (DisplayableBranchProperty)propertyList.elementAt(i);
			if (mi.getName().equalsIgnoreCase(s) && mi.kind ==kind)
				return mi;
		}
		return null;
	}
	/*...............................................................................*/
	public void pleaseShowHide(DisplayableBranchProperty[] list, boolean show){
		if (list == null)
			return;

		for (int i = 0; i<list.length; i++){
			if (list[i]!= null){
				list[i].showing = show;
			}
		}
		for (int i =0; i<extras.size(); i++){
			NodeAssocDisplayExtra e = (NodeAssocDisplayExtra)extras.elementAt(i);
			e.changeInShowing();
		}
		parametersChanged();
	}
	/*...............................................................................*/
	public boolean isShowing(DisplayableBranchProperty mi){
		if (mi == null)
			return false;
		return mi.showing;
	}
	/*...............................................................................*/
	boolean anyShowing(){

		for (int i=0; i<propertyList.size(); i++){
			DisplayableBranchProperty mi = (DisplayableBranchProperty)propertyList.elementAt(i);
			if (mi.showing)
				return true;
		}
		return false;
	}
	
	/*.................................................................................................................*/
	Button[] selectOrHide;
	Listable[] queryPropertiesList;
	Listable[] queryNamesArray;
	ListDialog dialog;
	boolean settingsChanged = false;
	IntegerField fontSizeF;
	Choice colorF;
	Checkbox showNamesF;
	Checkbox centredF;
	Checkbox whiteEdgesF;
	Checkbox verticalF;
	Checkbox showOnTerminalsF;
	Checkbox showIfNoValueF;
	IntegerField xOffsetF;
	IntegerField yOffsetF;
	IntegerField digitsF;
	Checkbox percentageF;
	DoubleField thresholdValueToShowF;

	void resetCheckbox(Checkbox cb, MesquiteBoolean cbBoolean, String baseName){
		if (cbBoolean.isUnanimous()) {
			cb.setState(cbBoolean.unanimousValue());
			cb.setLabel(baseName);
		}
		else {
			cb.setState(cbBoolean.unanimousValue());
			cb.setLabel("(?) " + baseName); //	cb.setLabel("(✓/✗) " + baseName);
		}
	}
	void resetIntegerField(IntegerField cb, MesquiteInteger intFInteger){
		if (intFInteger.isUnanimous()) 
			cb.setValue(intFInteger.unanimousValue());
		else 
			cb.setValue(MesquiteInteger.unassigned);
	}
	void resetDoubleField(DoubleField cb, MesquiteDouble intFDouble){
		if (intFDouble.isUnanimous())
			cb.setValue(intFDouble.unanimousValue());
		else 
			cb.setValue(MesquiteDouble.unassigned);
	}
	void resetChoice(Choice cb, MesquiteInteger intFInteger){
		if (intFInteger.isUnanimous()) {
			if (MesquiteInteger.isCombinable(intFInteger.unanimousValue()))
				cb.select(intFInteger.unanimousValue());
			else
				cb.select(0);
		}
		else 
			cb.select(ColorDistribution.standardColorNames.getSize());
	}
	/*.========================================================..*/
	void resetStyleWidgets(){
		if (thresholdValueToShowF== null)
			return;

		MesquiteInteger fontSizeConsensus = new MesquiteInteger();
		MesquiteInteger xOffsetConsensus = new MesquiteInteger();
		MesquiteInteger yOffsetConsensus = new MesquiteInteger();
		MesquiteInteger digitsConsensus = new MesquiteInteger();
		MesquiteInteger colorConsensus = new MesquiteInteger();

		MesquiteBoolean showNameConsensus = new MesquiteBoolean();
		MesquiteBoolean centeredConsensus = new MesquiteBoolean();
		MesquiteBoolean whiteEdgesConsensus = new MesquiteBoolean();
		MesquiteBoolean verticalConsensus = new MesquiteBoolean();
		MesquiteBoolean showOnTerminalsConsensus = new MesquiteBoolean();
		MesquiteBoolean showIfNoValueConsensus = new MesquiteBoolean();
		MesquiteBoolean percentageConsensus = new MesquiteBoolean();

		MesquiteDouble thresholdValueToShowConsensus = new MesquiteDouble();
		thresholdValueToShowConsensus.resetVote();

		IntegerArray indicesSelected = dialog.getIndicesCurrentlySelected();
		for (int i= 0; i<queryPropertiesList.length; i++){
			DisplayableBranchProperty property = (DisplayableBranchProperty)queryPropertiesList[i];
			if (indicesSelected == null || indicesSelected.getSize() == 0 || indicesSelected.indexOf(i)>=0){
				showNameConsensus.vote(property.showName);
				centeredConsensus.vote(property.centered);
				whiteEdgesConsensus.vote(property.whiteEdges);
				verticalConsensus.vote(property.vertical);
				showOnTerminalsConsensus.vote(property.showOnTerminals);
				showIfNoValueConsensus.vote(property.showIfUnassigned);

				colorConsensus.vote(property.color);
				fontSizeConsensus.vote(property.fontSize);
				xOffsetConsensus.vote(property.xOffset);
				yOffsetConsensus.vote(property.yOffset);
				if (property.kind == Associable.DOUBLES){
					digitsConsensus.vote(property.digits);
					percentageConsensus.vote(property.percentage);
					thresholdValueToShowConsensus .vote(property.thresholdValueToShow);
				}
			}
		}
		resetCheckbox(showNamesF, showNameConsensus, "Show Name");
		resetCheckbox(centredF, centeredConsensus, "Centered");
		resetCheckbox(whiteEdgesF, whiteEdgesConsensus, "White Edges");
		resetCheckbox(verticalF, verticalConsensus, "Vertical");
		resetCheckbox(showOnTerminalsF, showOnTerminalsConsensus, "Show on Terminals");
		resetCheckbox(showIfNoValueF, showIfNoValueConsensus, "Show if No Value");
		resetCheckbox(percentageF, percentageConsensus, "Show as Percentage");

		resetIntegerField(fontSizeF, fontSizeConsensus);
		resetIntegerField(xOffsetF, xOffsetConsensus);
		resetIntegerField(yOffsetF, yOffsetConsensus);
		resetIntegerField(digitsF, digitsConsensus);

		resetChoice(colorF, colorConsensus);

		resetDoubleField(thresholdValueToShowF, thresholdValueToShowConsensus);

	}

	/*.................................................................................................................*/
	void rePrefaceList(){
		if (queryPropertiesList == null){
			return;
		}
		for (int i= 0; i<queryPropertiesList.length; i++){
			DisplayableBranchProperty property = (DisplayableBranchProperty)queryPropertiesList[i];
			String before = "";
			if (property.showing)
				before =  "✓ "; 
			else
				before =   "  "; 
			String propertyName = ((MesquiteString)queryNamesArray[i]).getValue();
			String spacer = "   ";
			if (propertyName.length()<22)
				spacer = "                         ".substring(0, 22-propertyName.length());
			String after = spacer + "[";
			if (property.showName)
				after += "Name, ";
			if (property.centered)
				after += "centered, ";
			if (property.color>=0 && property.color<ColorDistribution.standardColorNames.getSize())
				after += ColorDistribution.standardColorNames.getValue(property.color) + ", ";
			if (property.whiteEdges)
				after += "white edges, ";
			if (property.vertical)
				after += "vertical, ";
			if (!property.showOnTerminals)
				after += "✗ terminals, ";
			after += "font " + property.fontSize + ", ";
			if (property.xOffset != 0 && property.yOffset != 0)
				after += "offset x " + property.xOffset +", y " + property.yOffset + ", ";
			if (property.kind == Associable.DOUBLES){
				after += "digits " + property.digits + ", ";
				if (property.percentage)
					after += "percentage, ";
				if (MesquiteDouble.isCombinable(property.thresholdValueToShow))
					after += "threshold " + MesquiteDouble.toString(property.thresholdValueToShow) + ", ";				
			}
			after = StringUtil.stripTrailingWhitespaceAndPunctuation(after);
			after += "]";
			((MesquiteString)queryNamesArray[i]).setName(before + propertyName + after);
		}
		dialog.resetList(queryNamesArray, true);

	}

	void settingsChanged(){
		dialog.getSecondButton().setLabel("Done");
		settingsChanged = true;

	}
	/*.................................................................................................................*/
	public boolean queryDialog(){
		if (propertyList==null)
			return false;
		moduleIsNaive = false;
		queryPropertiesList = propertyList.getListables();
		queryNamesArray =  new Listable[queryPropertiesList.length];
		for (int i=0; i<queryNamesArray.length; i++)
			queryNamesArray[i] = new MesquiteString(queryPropertiesList[i].getName());

		String helpString = "xxxx";  //add here notes on threashold
		dialog = new ListDialog(containerOfModule(), "Branch/Node Properties", "What properties to show and their styles?", false,helpString, queryNamesArray, 8, null, null, null, false, true);
		rePrefaceList();
		DoubleClickList list = dialog.getList();
		list.setMultipleMode(true);
		list.addItemListener(this);
		list.setMinimumWidth(650);

		list.setFont(new Font( "Monospaced", Font.PLAIN, 14 ));
		
		dialog.addLargeTextLabel("To change whether and how properties are shown on the tree, select properties in the list above, then hit the Show, Hide, and Set Style buttons below.");
		selectOrHide = dialog.addButtonRow("✓ Show Selected", "Hide Selected", null, this);
		selectOrHide[0].setActionCommand("showSelected");
		selectOrHide[1].setActionCommand("hideSelected");
		dialog.addHorizontalLine(1);

		//DISPLAY STYLES
		Button[] stylesButtons = dialog.addButtonRow("Set Style of Selected Properties", "Set Styles as Defaults", null, this);
		stylesButtons[0].addActionListener(this);
		stylesButtons[0].setActionCommand("setStyle");
		stylesButtons[1].addActionListener(this);
		stylesButtons[1].setActionCommand("saveStylesAsDefaults");


		Checkbox[] boxes = dialog.addCheckboxRow( new String[] {"Show Names", "Centered", "White Edges", "Vertical"}, 
				new boolean[] {true, true, true,true});
		dialog.addBlankLine();
		showNamesF = boxes[0];
		centredF = boxes[1];
		whiteEdgesF = boxes[2];
		verticalF = boxes[3];
		Checkbox[] boxes2 = dialog.addCheckboxRow( new String[] {"Show on Terminals", "Show if No Value"}, 
				new boolean[] {true, false});
		dialog.addBlankLine();
		showOnTerminalsF = boxes2[0];
		showIfNoValueF = boxes2[1];

		fontSizeF = dialog.addIntegerField("Font Size", 12, 6, 0, 100);
		fontSizeF.getTextField().addTextListener(this);
		int numColors = ColorDistribution.standardColorNames.getSize();
		String[] colorNames = new String[numColors+1];
		for (int k = 0; k<numColors; k++)
			colorNames[k] = ColorDistribution.standardColorNames.getValue(k);
		colorNames[numColors] = "(Mixed)";

		JLabel colorLabel = new MQJLabel ("         Color");
		colorF = dialog.addPopUpMenu (colorLabel, colorNames, 0);
		colorF.addItemListener(this);
		Container parentOfFont = fontSizeF.getTextField().getParent();
		Container parentOfColor = colorF.getParent();
		parentOfColor.remove(colorLabel);
		parentOfColor.remove(colorF);
		parentOfFont.add(colorLabel);
		parentOfFont.add(colorF);

		xOffsetF = dialog.addIntegerField("Horizontal Offset (pixels, more is to right)", 0, 6);
		yOffsetF = dialog.addIntegerField("Vertical Offset (pixels, more is lower)", 0, 6);
		xOffsetF.getTextField().addTextListener(this);
		yOffsetF.getTextField().addTextListener(this);
		dialog.addHorizontalLine(1);
		dialog.addLabel("For decimal numbers");
		digitsF = dialog.addIntegerField("Digits", 4, 8, 0, 8);
		digitsF.getTextField().addTextListener(this);
		percentageF = dialog.addCheckBox("Show as Percentage", false);
		Container parentOfDigits = digitsF.getTextField().getParent();
		Container parentOfPercentage = percentageF.getParent();
		parentOfPercentage.remove(percentageF);
		parentOfDigits.add(new MQJLabel ("            "));
		parentOfDigits.add(percentageF);
		thresholdValueToShowF = dialog.addDoubleField("Threshold Value to Show", MesquiteDouble.unassigned, 8);
		thresholdValueToShowF.getTextField().addTextListener(this);

		resetStyleWidgets();
		dialog.addHorizontalLine(2);
		dialog.addPrimaryButtonRow(null, "Cancel");
		dialog.prepareAndDisplayDialog();

		if (settingsChanged)  {
			storePreferences();
			parametersChanged();
		}

		dialog.dispose();
		dialog = null;
		return true;
	}
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() != colorF)
			resetStyleWidgets();
	}
	public void textValueChanged(TextEvent e) {
	}
	public void actionPerformed(ActionEvent e){
		IntegerArray indicesSelected = dialog.getIndicesCurrentlySelected();
		if (indicesSelected == null)
			return;
		boolean actionTaken = false;
		if (e.getActionCommand().equalsIgnoreCase("showSelected")) {
			for (int k = 0; k<indicesSelected.getSize(); k++){
				((DisplayableBranchProperty)queryPropertiesList[indicesSelected.getValue(k)]).showing = true;	
			}
			actionTaken = true;
		}
		else if (e.getActionCommand().equalsIgnoreCase("hideSelected")) {
			for (int k = 0; k<indicesSelected.getSize(); k++){
				((DisplayableBranchProperty)queryPropertiesList[indicesSelected.getValue(k)]).showing = false;
			}
			actionTaken = true;
		}
		else if (e.getActionCommand().equalsIgnoreCase("setStyle")) {
			for (int k = 0; k<indicesSelected.getSize(); k++){
				DisplayableBranchProperty property = (DisplayableBranchProperty)queryPropertiesList[indicesSelected.getValue(k)];
				if (MesquiteInteger.isCombinable(fontSizeF.getValue()))
					property.fontSize = fontSizeF.getValue();
				if (MesquiteInteger.isCombinable(digitsF.getValue()))
					property.digits = digitsF.getValue();
				if (MesquiteInteger.isCombinable(xOffsetF.getValue()))
					property.xOffset = xOffsetF.getValue();
				if (MesquiteInteger.isCombinable(yOffsetF.getValue()))
					property.yOffset =yOffsetF.getValue();

				if (MesquiteDouble.isCombinable(thresholdValueToShowF.getValue()))
					property.thresholdValueToShow =thresholdValueToShowF.getValue();

				property.whiteEdges = whiteEdgesF.getState();
				property.showOnTerminals = showOnTerminalsF.getState();
				property.showName = showNamesF.getState();
				property.centered = centredF.getState();
				property.vertical = verticalF.getState();
				property.percentage = percentageF.getState();			
				property.showIfUnassigned = showIfNoValueF.getState();			
				if (colorF.getSelectedIndex()<ColorDistribution.getNumStandardColors())
					property.color = colorF.getSelectedIndex();			
			}
			actionTaken = true;
		}
		else if (e.getActionCommand().equalsIgnoreCase("saveStylesAsDefaults")) {
				DisplayableBranchProperty.mergeIntoPreferences(propertyList); 
		}
		if (actionTaken){
			rePrefaceList();
			resetStyleWidgets();
			settingsChanged();
		
		}

	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		if (moduleIsNaive)
			return null;
		Snapshot temp = new Snapshot();
		for (int i=0; i< propertyList.size(); i++) {
			DisplayableBranchProperty n = (DisplayableBranchProperty)propertyList.elementAt(i);
			temp.addLine("showAssociate " + StringUtil.tokenize(n.getName()) + " " + n.kind + " " + n.showing);

			// sequence: showName, centered, whiteEdges, showOnTerminals, showIfUnassigned, percentage, vertical
			temp.addLine("setBooleans "  + StringUtil.tokenize(n.getName()) + " " + n.kind 
					+ " " + n.getBooleansString()); 

			// sequence fontSize, xOffset, yOffset, digits, color, thresholdValueToShow
			temp.addLine("setNumbers "  + StringUtil.tokenize(n.getName()) + " " + n.kind 
					+ " " + n.getNumbersString()); 

		}
		return temp;
	}
	/*.................................................................................................................*/
	MesquiteInteger pos = new MesquiteInteger();
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		moduleIsNaive = false;
		if (checker.compare(this.getClass(), "Shows the dialog to choose display options", "[]", commandName, "showDialog")) {
			queryDialog();
		}
		else if (checker.compare(this.getClass(), "Sets whether to show a node or branch associated value", "[on or off]", commandName, "showAssociate")) {
			String name = parser.getFirstToken(arguments);
			int kind = MesquiteInteger.fromString(parser.getNextToken());
			boolean show = MesquiteBoolean.fromTrueFalseString(parser.getNextToken());
			if (managerModule!= null)
				managerModule.addPropertyFromScript(name, kind,  show);
		}
		else if (checker.compare(this.getClass(), "Sets booleans", "[name][kind][on or off for 7 booleans]", commandName, "setBooleans")) {
			// sequence: showName, centered, whiteEdges, showOnTerminals, showIfUnassigned, percentage, vertical
			//use "x" to ignore
			String name = parser.getFirstToken(arguments);
			int kind = MesquiteInteger.fromString(parser.getNextToken());
			DisplayableBranchProperty property = findInList(name, kind);
			if (property!= null){
				property.setBooleans(parser);				 
			if (!MesquiteThread.isScripting()) parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets numbers", "[name][kind][5 integers & 1 double]", commandName, "setNumbers")) {
			// sequence fontSize, xOffset, yOffset, digits, color, thresholdValueToShow
			String name = parser.getFirstToken(arguments);
			int kind = MesquiteInteger.fromString(parser.getNextToken());
			DisplayableBranchProperty property = findInList(name, kind);
	
			if (property!= null){
				property.setNumbers(parser);				 
				if (!MesquiteThread.isScripting()) parametersChanged();
		}
	}

		else if (checker.compare(this.getClass(), "Sets booleans for all double properties. For reading of 3.x scripts", "[on or off for 7 booleans]", commandName, "setBooleansAllDoubles")) {
			for (int i= 0; i< propertyList.size(); i++){
				DisplayableBranchProperty property = (DisplayableBranchProperty)propertyList.elementAt(i);
				if (property.kind == Associable.DOUBLES)
					doCommand("setBooleans", ParseUtil.tokenize(property.getName()) + " " + property.kind + " " + arguments, checker);
			}
		}
		else if (checker.compare(this.getClass(), "Sets booleans for all string properties. For reading of 3.x scripts", "[on or off for 7 booleans]", commandName, "setBooleansAllStrings")) {
			for (int i= 0; i< propertyList.size(); i++){
				DisplayableBranchProperty property = (DisplayableBranchProperty)propertyList.elementAt(i);
				if (property.kind == Associable.STRINGS)
					doCommand("setBooleans", ParseUtil.tokenize(property.getName()) + " " + property.kind + " " + arguments, checker);
			}
		}
		else if (checker.compare(this.getClass(), "Sets numbers for all double properties. For reading of 3.x scripts", "[numbers for 7 values]", commandName, "setNumbersAllDoubles")) {
			for (int i= 0; i< propertyList.size(); i++){
				DisplayableBranchProperty property = (DisplayableBranchProperty)propertyList.elementAt(i);
				if (property.kind == Associable.DOUBLES)
					doCommand("setNumbers", ParseUtil.tokenize(property.getName()) + " " + property.kind + " " + arguments, checker);
			}
		}
		else if (checker.compare(this.getClass(), "Sets booleans for all string properties. For reading of 3.x scripts", "[numbers for 7 values]", commandName, "setNumbersAllStrings")) {
			for (int i= 0; i< propertyList.size(); i++){
				DisplayableBranchProperty property = (DisplayableBranchProperty)propertyList.elementAt(i);
				if (property.kind == Associable.STRINGS)
					doCommand("setNumbers", ParseUtil.tokenize(property.getName()) + " " + property.kind + " " + arguments, checker);
			}
		}
		/*
		else if (checker.compare(this.getClass(), "Set's to David's style", "", commandName, "setCorvallisStyle")) {
			whiteEdges.setValue(false);
			percentage.setValue(true);
			centred.setValue(false);
			vertical.setValue(false);
			thresholdValueToShow.setToUnassigned();
			digits=0;
			xOffset = -2;
			yOffset = 9;
			if (!MesquiteThread.isScripting()) parametersChanged();
		}
		 */
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}


	/*.................................................................................................................*/
	public String getName() {
		return "Show Branch/Node Properties on Tree";
	}
	public String getExplanation() {
		return "Controls display of Branch/Node properties on the tree." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return MesquiteModule.NEXTRELEASE;  
	}


	public TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		NodeAssocDisplayExtra extra = new NodeAssocDisplayExtra(this, treeDisplay);
		extras.addElement(extra);
		return extra;
	}

}


/* ======================================================================== */
class NodeAssocDisplayExtra extends TreeDisplayExtra implements Commandable, TreeDisplayLateExtra{
	BranchPropertyDisplayControl controlModule;
	MesquiteCommand taxonCommand, branchCommand, respondCommand;
	MesquiteTree myTree = null;
	StringInABox box = new StringInABox( "", treeDisplay.getFont(),1500);
	TreeTool infoTool;

	/*.--------------------------------------------------------------------------------------..*/
	public NodeAssocDisplayExtra (BranchPropertyDisplayControl ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		controlModule = ownerModule;
		infoTool = new TreeTool(this, "BranchInfo", ownerModule.getPath(), "branchInfo.gif", 5,2,"Branch Info", "This tool is used to show information about a branch.");
		infoTool.setTouchedCommand(MesquiteModule.makeCommand("showPopup",  this));
		if (ownerModule.containerOfModule() instanceof MesquiteWindow)
			((MesquiteWindow)ownerModule.containerOfModule()).addTool(infoTool);

		respondCommand = ownerModule.makeCommand("respondToPopup", this);
	}

	void changeInShowing(){
		treeDisplay.accumulateRequestsFromExtras(myTree);
		treeDisplay.redoCalculations(319283);
	}
	/*.................................................................................................................*/
	String stringAtNodeForPropertyExplanation(MesquiteTree tree, int node, DisplayableBranchProperty property, boolean showNames){
		String nodeString  = property.getStringAtNode(tree, node, showNames, !showNames, false);
		return nodeString;
	}
	/*.................................................................................................................*/


	String stringAtNode(MesquiteTree tree, int node, boolean showingAll, boolean showNamesRegardless, boolean includeLineBreaks, int forWhere){
		String[] strings = stringsAtNode(tree, node, showingAll, showNamesRegardless, null, forWhere);
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
	String[] stringsAtNode(MesquiteTree tree, int node, boolean showingAll, boolean showNamesRegardless, Vector nameCodes, int forWhere){ //0 elsewhere; 1 = screen display; 2 = popup
		Vector nodeStrings = new Vector();
		for (int p = 0; p< controlModule.propertyList.size(); p++){
			DisplayableBranchProperty property = (DisplayableBranchProperty)controlModule.propertyList.elementAt(p);
			if (property.inCurrentTree){
				String nodeString = null;
				if ((showingAll || property.showing) && (tree.nodeIsInternal(node) || property.showOnTerminals)){
					if (forWhere == 2){ //popup, don't show node label; shownames regardless; show if in tree but unassigned
						if (property.kind != Associable.BUILTIN || !property.getNameReference().equals(MesquiteTree.nodeLabelNameRef)){
							nodeStrings.addElement(property.getStringAtNode(tree, node, showNamesRegardless, true));
							if (nameCodes != null)
								nameCodes.addElement(property);
						}
					}
				}
			}
		}
		String[] strings = new String[nodeStrings.size()];
		for (int i= 0; i<nodeStrings.size(); i++)
			strings[i] = (String)nodeStrings.elementAt(i);
		return strings;
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public void cursorTouchBranch(Tree tree, int N, Graphics g, int modifiers, boolean isArrowTool){
		if (MesquiteEvent.rightClick(modifiers) && isArrowTool){
			showPopup(N);
		}
	}
	/*...........................................*/
	MesquitePopup popup;
	Vector popupKeys = new Vector();
	MesquiteInteger pos = new MesquiteInteger();
	/*...........................................*/
	void addToPopup(String s, int node, int response){
		if (popup==null)
			return;
		popup.addItem(s, ownerModule, respondCommand, Integer.toString(node) + " " + Integer.toString(response));
	}
	/*...........................................*/
	void showPopup(int branchFound){
		if (popup==null)
			popup = new MesquitePopup(treeDisplay);
		popup.removeAll();
		popupKeys.removeAllElements();
		int responseNumber = 0;
		String nL = "Node Label: ";
		if (StringUtil.blank(myTree.getNodeLabel(branchFound)))
			nL +=  "[none]";
		else
			nL += myTree.getNodeLabel(branchFound);
		popupKeys.addElement(new DisplayableBranchProperty(MesquiteTree.nodeLabelName, Associable.BUILTIN));
		addToPopup(nL, branchFound, responseNumber++);

		addToPopup("Branch/node number: " + branchFound, branchFound, responseNumber++);
		popupKeys.addElement(new DisplayableBranchProperty("nodenumber", Associable.BUILTIN));
		addToPopup("-", branchFound, responseNumber++);
		popupKeys.addElement(new DisplayableBranchProperty("dash", Associable.BUILTIN));
		String[] strings = stringsAtNode(myTree, branchFound, true, true, popupKeys, 2);
		if (strings != null)
			for (int i = 0; i<strings.length; i++)
				addToPopup(strings[i], branchFound, responseNumber++);

		addToPopup("-", branchFound, -1);
		popup.addItem("Control Display of Properties on Tree...", ownerModule, new MesquiteCommand("showDialog", ownerModule));
		popup.showPopup((int)treeDisplay.getTreeDrawing().x[branchFound], (int)treeDisplay.getTreeDrawing().y[branchFound]);
	}

	Parser parser = new Parser();
	/*...........................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 

		if (checker.compare(this.getClass(), "Shows popup menu with information about the branch", "[branch number]", commandName, "showPopup")) {
			int branchFound= MesquiteInteger.fromFirstToken(arguments, pos);
			if (branchFound >0 && MesquiteInteger.isCombinable(branchFound)) {
				showPopup(branchFound);
			}	 	
		}
		else if (checker.compare(this.getClass(), "Responds to popup choice", "[branch number]", commandName, "respondToPopup")) {
			int branchFound= MesquiteInteger.fromFirstToken(arguments, pos);
			int responseNumber= MesquiteInteger.fromString(arguments, pos);
			if (responseNumber>=0 && branchFound >0 && MesquiteInteger.isCombinable(branchFound) && MesquiteInteger.isCombinable(responseNumber) ) {
				if (responseNumber<popupKeys.size()){
					DisplayableBranchProperty property = (DisplayableBranchProperty)popupKeys.elementAt(responseNumber);
					String name = property.getName();
					NameReference nameRef = NameReference.getNameReference(name);
					MesquiteWindow container = controlModule.containerOfModule();
					int kind = property.kind;
					if (name.equalsIgnoreCase(MesquiteTree.branchLengthName) && kind == Associable.BUILTIN) {
						controlModule.discreetAlert("This is the length of the branch (stored in the primary branch length storage of the tree). You can adjust it using various tools, or with items in the Alter menu.");
					}
					else  if (name.equalsIgnoreCase(MesquiteTree.nodeLabelName) && kind == Associable.BUILTIN){
						String nC = MesquiteString.queryString(container, "Name of node", "This is the label of the node/clade/branch. You can edit it with the Name Nodes tool, or here.", myTree.getNodeLabel(branchFound), 1);
						if (nC != null){
							myTree.setNodeLabel(nC, branchFound);
							myTree.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED));
						}
					}
					else  if (name.equalsIgnoreCase("!color") && kind == Associable.STRINGS){
						String nC = MesquiteString.queryString(container, "Color", "This is the color of the branch (stored in the reserved color storage of the tree). "
								+"You can change it via the paintbrush tool, or here via hexadecimal color string, which must start with a # and the 6 digits" 
								+ " consisting of only the characters 0 to 9 or A to F (e.g., #ff0000 is red, #00ff00 is green, #0000ff is blue).", 
								(String)myTree.getAssociatedString(nameRef, branchFound), 1);
						if (!StringUtil.blank(nC) && nC.length() == 7 && nC.charAt(0) == '#'){
							myTree.setAssociatedString(nameRef, branchFound, nC);
							myTree.notifyListeners(this, new Notification(MesquiteListener.ASSOCIATED_CHANGED));
							return null;
						}

					}
					else  if (kind == Associable.BITS) {
						//	controlModule.discreetAlert("This is a boolean (true/false value) attached to the branch of the tree, with name \"" + name + "\" and value \"" + stringAtNodeForPropertyExplanation(myTree, branchFound,  mi, false) + "\".");
						boolean current = myTree.getAssociatedBit(property.getNameReference(), branchFound);
						MesquiteBoolean check = MesquiteBoolean.queryCheckBox(controlModule.containerOfModule(), "Boolean value",
								"This is a boolean (true/false value) attached to the branch of the tree, with name \"" + name 
								+ "\" and value \"" + stringAtNodeForPropertyExplanation(myTree, branchFound,  property, false) + "\". "
								+" You can change its value here (checked = true; unchecked = false).", property.getName(), current);
						if (check != null){
							myTree.setAssociatedBit(property.getNameReference(), branchFound, check.getValue());
							myTree.notifyListeners(this, new Notification(MesquiteListener.ASSOCIATED_CHANGED));
						}

					}
					else  if (kind == Associable.LONGS){
						long d = myTree.getAssociatedLong(nameRef, branchFound);  
						long dN = MesquiteLong.queryLong(container, "Integer number", "This is an integral (long) value attached to the branch of the tree, with name \"" + name + "\" "
								+"and value \"" + stringAtNodeForPropertyExplanation(myTree, branchFound,  property, false) + "\". You can edit it here, although there may be restrictions on its value.", d);
						if (!MesquiteLong.isUnassigned(dN)){
							myTree.setAssociatedLong(nameRef, branchFound, dN);  
							myTree.notifyListeners(this, new Notification(MesquiteListener.ASSOCIATED_CHANGED));
						}
					}
					else  if (kind == Associable.DOUBLES){
						double d = myTree.getAssociatedDouble(nameRef, branchFound);  
						double dN = MesquiteDouble.queryDouble(container, "Decimal number", "This is a decimal number (floating-point) attached to the branch of the tree, with name \"" + name + "\" and value \"" + 
								stringAtNodeForPropertyExplanation(myTree, branchFound,  property, false) + "\". You can edit it here, although there may be restrictions on its value.", d);
						if (!MesquiteDouble.isUnassigned(dN)){
							myTree.setAssociatedDouble(nameRef, branchFound, dN);  
							myTree.notifyListeners(this, new Notification(MesquiteListener.ASSOCIATED_CHANGED));
						}
						//controlModule.discreetAlert("This is a floating-point (double) value attached to the branch of the tree, with name \"" + name + "\" and value \"" + stringAtNode(myTree, branchFound,  name,  kind, false) + "\".");
					}
					else  if (kind == Associable.STRINGS){
						String d = myTree.getAssociatedString(nameRef, branchFound);  
						String nC = MesquiteString.queryString(container,"String at node","This is a string of text, attached to the branch of the tree, with name \"" + name + "\". You can edit it here, "
								+"although it may have a specific purpose and need to be in a specific format.", (String)d, 2);
						if (nC != null){
							myTree.setAssociatedString(nameRef, branchFound, nC);
							myTree.notifyListeners(this, new Notification(MesquiteListener.ASSOCIATED_CHANGED));
							return null;
						}
						//controlModule.discreetAlert("This is a floating-point (double) value attached to the branch of the tree, with name \"" + name + "\" and value \"" + stringAtNode(myTree, branchFound,  name,  kind, false) + "\".");
					}
					else  if (kind == Associable.OBJECTS) {
						Object d = myTree.getAssociatedObject(nameRef, branchFound);  
						String typeName = "an unspecified object";
						if (d instanceof DoubleArray)
							typeName = "an array of floating-point numbers";
						else if (d instanceof LongArray)
							typeName = "an array of integer numbers";
						else if (d instanceof StringArray)
							typeName = "an array of strings of text";
						else if (d instanceof ObjectArray)
							typeName = "an array of unspecified objects";
						controlModule.discreetAlert("This is " + typeName +  ", attached to the branch of the tree, with name \"" + name + "\". "
								+ "You can't yet edit properties of this kind in Mesquite.");
					}

				}
			}	 	
		}
		return null;
	}

	TreeDisplayRequests blank = new TreeDisplayRequests();
	/*.................................................................................................................*/
	/* The TreeDisplayRequests object has public int fields leftBorder, topBorder, rightBorder, bottomBorder (in pixels and in screen orientation)
	 * and a public double field extraDepthAtRoot (in branch lengths units and rootward regardless of screen orientation) */
	public TreeDisplayRequests getRequestsOfTreeDisplay(Tree tree, TreeDrawing treeDrawing){
		if (treeDisplay.getOrientation() != TreeDisplay.LEFT && treeDisplay.getOrientation() != TreeDisplay.RIGHT)
			return blank;
		String[] stringsAtRoot = stringsAtNode((MesquiteTree)tree, tree.getRoot(), false, false, null, 1);
		if (stringsAtRoot == null || stringsAtRoot.length == 0)
			return blank;
		String longest  = "";
		for (int i= 0; i<stringsAtRoot.length; i++)
			if (stringsAtRoot[i]!= null && stringsAtRoot[i].length()>longest.length())
				longest = stringsAtRoot[i];
		Graphics g = treeDisplay.getGraphics();
		int width = 0;
		if (g!=null) {
			FontMetrics fm = g.getFontMetrics(g.getFont());
			width = fm.stringWidth(longest);
			g.dispose();
			//	if (controlModule.centred.getValue()) HERE
			width = width/2;
		}
		else 
			width = longest.length()*4;
		TreeDisplayRequests requests = new TreeDisplayRequests();
		if (treeDisplay.getOrientation() == TreeDisplay.LEFT)
			requests.rightBorder = width;
		else
			requests.leftBorder = width;
		return requests;
	}
	/*.................................................................................................................*/
	void myDraw(MesquiteTree tree, int node, Graphics g) {
		if (tree.withinCollapsedClade(node))
			return;
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			myDraw(tree, d, g);

		int offsetY = 0;
		int offsetX = 0;
		if (treeDisplay.getOrientation() == TreeDisplay.RIGHT) {
			offsetY = treeDisplay.getTreeDrawing().getEdgeWidth()+2;
			offsetX = -2;
		}
		else if (treeDisplay.getOrientation() == TreeDisplay.LEFT){
			offsetY = treeDisplay.getTreeDrawing().getEdgeWidth()+2;
			offsetX = 2;
		}
		else if (treeDisplay.getOrientation() == TreeDisplay.UP){
			offsetX = treeDisplay.getTreeDrawing().getEdgeWidth()+4;
		}
		else if (treeDisplay.getOrientation() == TreeDisplay.DOWN){
			offsetX = treeDisplay.getTreeDrawing().getEdgeWidth()+4;
			offsetY = -8;
		}
		
		for (int p = 0; p< controlModule.propertyList.size(); p++){
			DisplayableBranchProperty property = (DisplayableBranchProperty)controlModule.propertyList.elementAt(p);
			if (property.inCurrentTree && property.showing && (tree.nodeIsInternal(node) || property.showOnTerminals)){
				String nodeString = property.getStringAtNode(tree, node);
				if (StringUtil.notEmpty(nodeString)){
					box.setFont(property.getFont(treeDisplay.getFont())); 
					Color textColor = Color.black;
					Color color = ColorDistribution.getStandardColor(property.color);
					if (color != null)
						textColor = color;

					if (property.whiteEdges)
						box.setColors(textColor, Color.white);
					else
						box.setColors(textColor, null);
					box.setString(nodeString);
					double x, y;
					int propertyOffsetX = offsetX;
					int propertyOffsetY = offsetY;
					if (treeDisplay.getOrientation() == TreeDisplay.RIGHT || treeDisplay.getOrientation() == TreeDisplay.LEFT){
						propertyOffsetX += property.xOffset;
						propertyOffsetY += property.yOffset + (box.getLineAscent()/2);
					}
					else {
						if (!property.vertical)
							propertyOffsetX += property.xOffset + (box.getLineAscent()/2);
						propertyOffsetY += property.xOffset;
						
					}
					if (property.centered){   // center on branch
						double centreBranchX = treeDisplay.getTreeDrawing().getBranchCenterX(node) + propertyOffsetX;
						double centreBranchY =  treeDisplay.getTreeDrawing().getBranchCenterY(node)+ propertyOffsetY;
						int stringWidth = box.getMaxWidthMunched();
						if (!property.vertical){
							x = centreBranchX - stringWidth/2;
							y = centreBranchY - property.fontSize;
						}
						else {
							x = centreBranchX - property.fontSize*2;
							y = centreBranchY + stringWidth/2;
						}
					}
					else {
						int stringWidth = box.getMaxWidthMunched();
						x= treeDisplay.getTreeDrawing().getNodeValueTextBaseX(node, treeDisplay.getTreeDrawing().getEdgeWidth(), stringWidth, property.fontSize, !property.vertical) + propertyOffsetX;
						y = treeDisplay.getTreeDrawing().getNodeValueTextBaseY(node, treeDisplay.getTreeDrawing().getEdgeWidth(), stringWidth,property.fontSize,!property.vertical) + propertyOffsetY;
					}
					Shape ss = g.getClip();
					g.setClip(null);
					if (!property.vertical)
						box.draw(g,  x, y);
					else
						box.draw(g,  x, y, 0, 1500, treeDisplay, false, false);  //Debugg.println fix

					if (property.vertical){
						offsetX += box.getLineHeight();
					}
					else {
						offsetY += box.getLineHeight();
					}
				
					g.setClip(ss);	
				}
			}
		}

	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int node, Graphics g) {
		if (!controlModule.anyShowing())
			return;
		myDraw((MesquiteTree)tree, node, g);

	}
	/*.................................................................................................................*/

	void update(){
		treeDisplay.pleaseUpdate(false);
	}

	/*.................................................................................................................*/

	/**return a text version of information at node*/
	public String textAtNode(Tree tree, int node){
		if (!controlModule.anyShowing())
			return "";
		return stringAtNode((MesquiteTree) tree,  node, true, true, false, 0);
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

