/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.NodePropertiesAManager;
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
import mesquite.lib.tree.DisplayableTreeProperty;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDisplayExtra;

/* ======================================================================== */
public class NodePropertiesAManager extends TreeDisplayAssistantI {

	String[] reservedNames = new String[]{"!color"};
	String[] builtInNames = new String[]{MesquiteTree.branchLengthName, MesquiteTree.nodeLabelName};

	ListableVector propertyList;
	static boolean asked= false;

	boolean moduleIsNaive = true; //so as not to save the snapshot
	NAAMDisplayExtra extra;

	MesquiteTree tree;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		TreeWindowMaker twMB = (TreeWindowMaker)findEmployerWithDuty(TreeWindowMaker.class);
		if (twMB!= null){
			propertyList = twMB.getBranchPropertiesList();
		}
	//	extras = new Vector();
		propertyList.addElement(new DisplayableTreeProperty(MesquiteTree.nodeLabelName, Associable.BUILTIN), false);
		propertyList.addElement(new DisplayableTreeProperty(MesquiteTree.branchLengthName, Associable.BUILTIN), false);

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

		addPropertiesToList(tree);
	}
	public void changed(Object caller, Object obj, Notification notification){
		if (caller == tree || obj == tree){
			setTree(tree);
		}
	}

	/*.========================================================..*/

	public void writeList(ListableVector list){
		System.out.println("Properties on record & to show");

		for (int i=0; i<list.size(); i++){
			DisplayableTreeProperty mi = (DisplayableTreeProperty)list.elementAt(i);
			System.out.println(mi.getName() + "\t" + mi.kind + " showing " + mi.showing);
		}
	}
	DisplayableTreeProperty findInList(NameReference nr, int kind){
		return (DisplayableTreeProperty)DisplayableTreeProperty.findInList(propertyList, nr, kind);
	}
	DisplayableTreeProperty findInList(String s, int kind){
		if (propertyList.indexOfByName(s)<0)
			return null;
		for (int i=0; i<propertyList.size(); i++){
			DisplayableTreeProperty mi = (DisplayableTreeProperty)propertyList.elementAt(i);
			if (mi.getName().equalsIgnoreCase(s) && mi.kind ==kind)
				return mi;
		}
		return null;
	}
	/*...............................................................................*/
	int indexInList(DisplayableTreeProperty property){
		for (int i=0; i<propertyList.size(); i++){
			DisplayableTreeProperty mi = (DisplayableTreeProperty)propertyList.elementAt(i);
			if (mi.equals(property))
				return i;
		}
		return propertyList.indexOf(property);  //just in case?
	}
	/*...............................................................................*
	boolean inList(PropertyDisplayRecord property){
		return indexInList(property)>=0;
	}
	/*...............................................................................*/
	public boolean isBuiltIn(DisplayableTreeProperty mi){
		return mi.kind== Associable.BUILTIN;
	}
	/*...............................................................................*
	public void pleaseShowHide(PropertyDisplayRecord[] list, boolean show){
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
	/*...............................................................................*
	public boolean isShowing(PropertyDisplayRecord mi){
		if (mi == null)
			return false;
		return mi.showing;
	}
	/*...............................................................................*
	boolean anyShowing(){

		for (int i=0; i<propertyList.size(); i++){
			PropertyDisplayRecord mi = (PropertyDisplayRecord)propertyList.elementAt(i);
			if (mi.showing)
				return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	public void addPropertyFromScript(String name, int kind, boolean show){
		if (StringUtil.blank(name) || !MesquiteInteger.isCombinable(kind))
			return;
		if (name.equalsIgnoreCase("selected") && kind == Associable.BITS)
			return;
		DisplayableTreeProperty mi = findInList(name, kind);
		if (mi==null){  
			mi = new DisplayableTreeProperty(name, kind);
			mi.showing = show;
			propertyList.addElement(mi, false);
		}
		else
				mi.showing = show;
		if (mi != null && tree != null)
			mi.inCurrentTree = tree.isPropertyAssociated(mi);
	}
	/*.................................................................................................................*/
	NameReference selectedNRef = NameReference.getNameReference("selected");
	private void addPropertiesToList(MesquiteTree tree){
		if (tree == null)
			return;
		int count = 0;
		for (int i=0; i<propertyList.size(); i++){
			DisplayableTreeProperty property = (DisplayableTreeProperty)propertyList.elementAt(i);
			property.inCurrentTree = tree.isPropertyAssociated(property);
			count++;
			property.setBelongsToBranch(tree.propertyIsBetween(property), true);
		}
		DisplayableTreeProperty[] properties = tree.getPropertyRecords();
		if (properties == null)
			return;
		for (int i=0; i<properties.length; i++){
			boolean toBeAdded = indexInList(properties[i])<0;
			if (selectedNRef.equals(properties[i].getNameReference()) && properties[i].kind == Associable.BITS)
				toBeAdded = false;
			if (toBeAdded){  
				propertyList.addElement(properties[i], false);
				properties[i].inCurrentTree = true;
				count++;
			}
		}
	}

	/*.................................................................................................................*
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
	/*.========================================================..*
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
			PropertyDisplayRecord property = (PropertyDisplayRecord)queryPropertiesList[i];
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

	/*.................................................................................................................*
	void rePrefaceList(){
		if (queryPropertiesList == null){
			return;
		}
		for (int i= 0; i<queryPropertiesList.length; i++){
			PropertyDisplayRecord property = (PropertyDisplayRecord)queryPropertiesList[i];
			String before = "";
			if (property.showing)
				before =  "✓\t"; 
			else
				before =   "  \t"; 
			String after = "        [";
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
			((MesquiteString)queryNamesArray[i]).setName(before + ((MesquiteString)queryNamesArray[i]).getValue() + after);
		}
		dialog.resetList(queryNamesArray, true);

	}

	void settingsChanged(){
		dialog.getSecondButton().setLabel("Done");
		settingsChanged = true;

	}
	/*.................................................................................................................*
	public boolean queryDialog(){
		if (propertyList==null)
			return false;
		moduleIsNaive = false;
		queryPropertiesList = propertyList.getListables();
		queryNamesArray =  new Listable[queryPropertiesList.length];
		for (int i=0; i<queryNamesArray.length; i++)
			queryNamesArray[i] = new MesquiteString(queryPropertiesList[i].getName());

		String helpString = "xxxx";  //add here notes on threashold
		dialog = new ListDialog(containerOfModule(), "Branch/Node Properties", "What properties to show and how to show them?", false,helpString, queryNamesArray, 8, null, null, null, false, true);
		rePrefaceList();
		dialog.getList().setMultipleMode(true);
		dialog.getList().addItemListener(this);
		dialog.getList().setMinimumWidth(600);
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
				((PropertyDisplayRecord)queryPropertiesList[indicesSelected.getValue(k)]).showing = true;	
			}
			actionTaken = true;
		}
		else if (e.getActionCommand().equalsIgnoreCase("hideSelected")) {
			for (int k = 0; k<indicesSelected.getSize(); k++){
				((PropertyDisplayRecord)queryPropertiesList[indicesSelected.getValue(k)]).showing = false;
			}
			actionTaken = true;
		}
		else if (e.getActionCommand().equalsIgnoreCase("setStyle")) {
			for (int k = 0; k<indicesSelected.getSize(); k++){
				PropertyDisplayRecord property = (PropertyDisplayRecord)queryPropertiesList[indicesSelected.getValue(k)];
				if (MesquiteInteger.isCombinable(fontSizeF.getValue()))
					property.fontSize = fontSizeF.getValue();
				if (MesquiteInteger.isCombinable(digitsF.getValue()))
					property.digits = digitsF.getValue();
				if (MesquiteInteger.isCombinable(xOffsetF.getValue()))
					property.xOffset = xOffsetF.getValue();
				if (MesquiteInteger.isCombinable(yOffsetF.getValue()))
					property.yOffset = yOffsetF.getValue();

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
				PropertyDisplayRecord.mergeIntoPreferences(propertyList); 
		}
		if (actionTaken){
			rePrefaceList();
			resetStyleWidgets();
			settingsChanged();
		
		}

	}

	/*.................................................................................................................*
	public Snapshot getSnapshot(MesquiteFile file) {
		if (moduleIsNaive)
			return null;
		Snapshot temp = new Snapshot();
		for (int i=0; i< propertyList.size(); i++) {
			PropertyDisplayRecord n = (PropertyDisplayRecord)propertyList.elementAt(i);
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
	/*.................................................................................................................*
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
			addPropertyFromScript(name, kind,  show);
		}
		else if (checker.compare(this.getClass(), "Sets booleans", "[name][kind][on or off for 7 booleans]", commandName, "setBooleans")) {
			// sequence: showName, centered, whiteEdges, showOnTerminals, showIfUnassigned, percentage, vertical
			//use "x" to ignore
			String name = parser.getFirstToken(arguments);
			int kind = MesquiteInteger.fromString(parser.getNextToken());
			PropertyDisplayRecord property = findInList(name, kind);
			if (property!= null){
				property.setBooleans(parser);				 
			if (!MesquiteThread.isScripting()) parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets numbers", "[name][kind][5 integers & 1 double]", commandName, "setNumbers")) {
			// sequence fontSize, xOffset, yOffset, digits, color, thresholdValueToShow
			String name = parser.getFirstToken(arguments);
			int kind = MesquiteInteger.fromString(parser.getNextToken());
			PropertyDisplayRecord property = findInList(name, kind);
			if (property!= null){
				property.setNumbers(parser);				 
				if (!MesquiteThread.isScripting()) parametersChanged();
		}
	}

		else if (checker.compare(this.getClass(), "Sets booleans for all double properties. For reading of 3.x scripts", "[on or off for 7 booleans]", commandName, "setBooleansAllDoubles")) {
			for (int i= 0; i< propertyList.size(); i++){
				PropertyDisplayRecord property = (PropertyDisplayRecord)propertyList.elementAt(i);
				if (property.kind == Associable.DOUBLES)
					doCommand("setBooleans", ParseUtil.tokenize(property.getName()) + " " + property.kind + " " + arguments, checker);
			}
		}
		else if (checker.compare(this.getClass(), "Sets booleans for all string properties. For reading of 3.x scripts", "[on or off for 7 booleans]", commandName, "setBooleansAllStrings")) {
			for (int i= 0; i< propertyList.size(); i++){
				PropertyDisplayRecord property = (PropertyDisplayRecord)propertyList.elementAt(i);
				if (property.kind == Associable.STRINGS)
					doCommand("setBooleans", ParseUtil.tokenize(property.getName()) + " " + property.kind + " " + arguments, checker);
			}
		}
		else if (checker.compare(this.getClass(), "Sets numbers for all double properties. For reading of 3.x scripts", "[numbers for 7 values]", commandName, "setNumbersAllDoubles")) {
			for (int i= 0; i< propertyList.size(); i++){
				PropertyDisplayRecord property = (PropertyDisplayRecord)propertyList.elementAt(i);
				if (property.kind == Associable.DOUBLES)
					doCommand("setNumbers", ParseUtil.tokenize(property.getName()) + " " + property.kind + " " + arguments, checker);
			}
		}
		else if (checker.compare(this.getClass(), "Sets booleans for all string properties. For reading of 3.x scripts", "[numbers for 7 values]", commandName, "setNumbersAllStrings")) {
			for (int i= 0; i< propertyList.size(); i++){
				PropertyDisplayRecord property = (PropertyDisplayRecord)propertyList.elementAt(i);
				if (property.kind == Associable.STRINGS)
					doCommand("setNumbers", ParseUtil.tokenize(property.getName()) + " " + property.kind + " " + arguments, checker);
			}
		}
		
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}


	/*.................................................................................................................*/
	public String getName() {
		return "Branch/Node Properties Secretary";
	}
	public String getExplanation() {
		return "Keeps record of Branch/Node properties on the tree." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return MesquiteModule.NEXTRELEASE;  
	}



	public TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		extra = new NAAMDisplayExtra(this, treeDisplay);
		return extra;
	}

}


/* ======================================================================== */
class NAAMDisplayExtra extends TreeDisplayExtra{
	NodePropertiesAManager controlModule;
	MesquiteTree myTree = null;

	/*.--------------------------------------------------------------------------------------..*/
	public NAAMDisplayExtra (NodePropertiesAManager ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		controlModule = ownerModule;
	}

	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		myTree = (MesquiteTree)tree;
		controlModule.setTree((MesquiteTree)tree);
	}

	public void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
	}

	@Override
	public void printOnTree(Tree tree, int drawnRoot, Graphics g) {
	}

}

