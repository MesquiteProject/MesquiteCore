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
import java.awt.Shape;
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
import mesquite.lib.tree.TreeDrawing;
import mesquite.lib.tree.TreeTool;
import mesquite.lib.ui.AlertDialog;
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
	String[] reservedNames = new String[]{"!color"};
	String[] builtInNames = new String[]{MesquiteTree.branchLengthName, MesquiteTree.nodeLabelName};

	ListableVector propertyNames;
	Bits propertiesToShow;
	static boolean asked= false;
	int digits = 4;
	int fontSize = 10;
	int xOffset = 0;
	int yOffset = 0;
	boolean moduleIsNaive = true; //hasn't yet received script or user input; therefore display consensusFrequency automatically and without label


	MesquiteTree tree;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		MesquiteModule twMB = findEmployerWithDuty(TreeWindowMaker.class);
		if (twMB!= null)
			twMB.addMenuItem(null, "Display Branch/Node Properties on Tree...", makeCommand("showDialog", this));
		extras = new Vector();
		propertyNames = new ListableVector();
		propertyNames.addElement(new MesquiteInteger(MesquiteTree.branchLengthName, Associable.BUILTIN), false);
		propertyNames.addElement(new MesquiteInteger(MesquiteTree.nodeLabelName, Associable.BUILTIN), false);
		propertiesToShow = new Bits(2); //two bits already for branch lengths and node labels
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
		addAssociatesToNames(tree);
	}
	public void changed(Object caller, Object obj, Notification notification){
		if (caller == tree || obj == tree){
			setTree(tree);
		}
	}

	/*.========================================================..*/
	public void changeName(String s, int kind, String newName){
		if (propertyNames.indexOfByName(s)<0)
			return;
		for (int i=0; i<propertyNames.size(); i++){
			MesquiteInteger mi = (MesquiteInteger)propertyNames.elementAt(i);
			if (mi.getName().equals(s) && mi.getValue() ==kind){
				mi.setName(newName);
				return;
			}
		}
	}	

	public void writeList(){
		System.out.println("Properties on record & to show");
		
		for (int i=0; i<propertyNames.size(); i++){
			MesquiteInteger mi = (MesquiteInteger)propertyNames.elementAt(i);
			String toShow = "[>]";
			if (i<propertiesToShow.getSize())
				toShow = " " + propertiesToShow.isBitOn(i);
			System.out.println(mi.getName() + "\t" + mi.getValue() + toShow);
		}
	}
	int indexInList(String s, int kind){
		if (propertyNames.indexOfByName(s)<0)
			return -1;
		for (int i=0; i<propertyNames.size(); i++){
			MesquiteInteger mi = (MesquiteInteger)propertyNames.elementAt(i);
			if (mi.getName().equals(s) && mi.getValue() ==kind)
				return i;
		}
		return -1;
	}
	/*...............................................................................*/
	int indexInList(MesquiteInteger name){
		return indexInList(name.getName(), name.getValue());
	}
	/*...............................................................................*/
	boolean inList(MesquiteInteger name){
		return indexInList(name)>=0;
	}
	/*...............................................................................*/
	boolean inList(String name, int kind){
		return indexInList(name, kind)>=0;
	}
	/*...............................................................................*/
	public boolean isShowing(String name, int kind){
		int nameIndex = indexInList(name, kind);
		if (nameIndex<0)
			return false;
		return propertiesToShow.isBitOn(nameIndex);
	}
	/*...............................................................................*/
	public boolean isBuiltIn(MesquiteInteger mi){
		return isBuiltIn(mi.getName(), mi.getValue());
	}
	/*...............................................................................*/
	public boolean isBuiltIn(String name, int kind){
		return (kind == Associable.BUILTIN);
	}
	/*...............................................................................*/
	public void pleaseShowHide(MesquiteInteger[] list, boolean show){
		if (list == null)
			return;

		for (int i = 0; i<list.length; i++){
			if (list[i]!= null){
					int nameIndex = indexInList(list[i]);
					if (nameIndex>=0)
						propertiesToShow.setBit(nameIndex, show);
			}
		}
		parametersChanged();
	}
	/*...............................................................................*/
	public boolean isShowing(MesquiteInteger name){
		int nameIndex = indexInList(name);
		if (nameIndex<0)
			return false;
		return propertiesToShow.isBitOn(nameIndex);
	}
	/*...............................................................................*/
	boolean anyShowing(){
		return (propertiesToShow.anyBitsOn() && propertyNames.size()>0);
	}
	/*.................................................................................................................*/
	private void addAssociateName(String name, int kind, boolean show){
		//if (StringUtil.blank(name) || StringArray.indexOfIgnoreCase(builtInNames, name)>=0 || !MesquiteInteger.isCombinable(kind))
		if (StringUtil.blank(name) || !MesquiteInteger.isCombinable(kind))
			return;
		if (name.equalsIgnoreCase("selected") && kind == Associable.BITS)
			return;
		if (!inList(name, kind)){  //color is not available to be shown in this way
			propertyNames.addElement(new MesquiteInteger(name, kind), false);
			propertiesToShow.resetSize(propertiesToShow.getSize()+1);
			propertiesToShow.setBit(propertiesToShow.getSize()-1, show);
		}
		else
			propertiesToShow.setBit(indexInList(name, kind), show);
	}
	private void addAssociatesToNames(MesquiteTree tree){
		if (tree == null)
			return;
		MesquiteInteger[] assocNames = tree.getAssociatesNamesWithKinds();
		if (assocNames == null)
			return;
		for (int i=0; i<assocNames.length; i++){
			if (!(assocNames[i] != null && assocNames[i].getName()!= null && assocNames[i].getName().equalsIgnoreCase("selected") && assocNames[i].getValue() == Associable.BITS) //selected is not to be added
					&& !inList(assocNames[i])){  //color is not available to be shown in this way
				propertyNames.addElement(assocNames[i], false);
				propertiesToShow.resetSize(propertiesToShow.getSize()+1);
				if (moduleIsNaive && assocNames[i].getName().equalsIgnoreCase("consensusFrequency")){
					propertiesToShow.setBit(propertiesToShow.getSize()-1);
					showNames.setValue(false);
				}
			}
		}
	}

	/*.................................................................................................................*/
	/*.========================================================..*/

	public boolean queryDialog(){
		if (propertyNames==null)
			return false;
		moduleIsNaive = false;
		Listable[] namesAsArray = propertyNames.getListables();
		Listable[] listWithTwoMore = null;
		if (namesAsArray == null || namesAsArray.length == 0)
			listWithTwoMore = new Listable[2];
		else {
			listWithTwoMore = new Listable[namesAsArray.length+2];
			for (int i=0; i<namesAsArray.length; i++)
				listWithTwoMore[i] = namesAsArray[i];
		}
		String helpString = "xxxx";  //add here notes on threashold
		ListDialog dialog = new ListDialog(containerOfModule(), "Branch/Node Properties", "What properties to show and how to show them?", false,helpString, namesAsArray, 6, null, "OK", "Deselect", false, true);
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

		boolean[] toShow = propertiesToShow.getAsArray();
		dialog.setSelected(toShow);
		dialog.addHorizontalLine(1);

		
		/*\nSystem.out.println("PROPERTY NAMES and TOSHOW\n");
		writeList();
		*/
		
		dialog.completeAndShowDialog(true);
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		if (dialog.buttonPressed.getValue() == 0)  {
			IntegerArray result = dialog.getIndicesSelected();
			if (result==null) 
				propertiesToShow.clearAllBits();
			else {
				propertiesToShow.clearAllBits();
				for (int i = 0; i<result.getSize(); i++){
					int sel = result.getValue(i);
					if (sel>=0 && sel<propertiesToShow.getSize()){
						propertiesToShow.setBit(sel, true);
					}
				}
			}

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
		if (moduleIsNaive)
			return null;
		Snapshot temp = new Snapshot();
		for (int i=0; i< propertyNames.size(); i++) {
			MesquiteInteger n = (MesquiteInteger)propertyNames.elementAt(i);
			if (isShowing(n))
				temp.addLine("showAssociate " + StringUtil.tokenize(n.getName()) + " " + n.getValue());
		}
		
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
		moduleIsNaive = false;
		if (checker.compare(this.getClass(), "Shows the dialog to choose display options", "[]", commandName, "showDialog")) {
			queryDialog();
		}
		else if (checker.compare(this.getClass(), "Sets whether to show a node or branch associated value", "[on or off]", commandName, "showAssociate")) {
			String name = parser.getFirstToken(arguments);
			int kind = MesquiteInteger.fromString(parser.getNextToken());
			addAssociateName(name, kind,  true);
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

		respondCommand = ownerModule.makeCommand("respondToPopup", this);
		resetFontSize();
	}

	/*.................................................................................................................*/
	String stringAtNode(MesquiteTree tree, int node, String name, int kind, boolean showNames){
		String nodeString  = "";
		NameReference nr = NameReference.getNameReference(name);
		if (kind == Associable.BITS && tree.getWhichAssociatedBits(nr)!=null){
			boolean d = tree.getAssociatedBit(nr, node);  //Debugg.println save vector of name references
			if (showNames)
				nodeString += name+ ": ";
			nodeString += MesquiteBoolean.toTrueFalseString(d);
		}

		else if (kind == Associable.LONGS && tree.getWhichAssociatedLong(nr)!=null){
			long d = tree.getAssociatedLong(nr, node);  //Debugg.println save vector
			if (showNames)
				nodeString +=name+ ": ";
			nodeString += MesquiteLong.toString(d);
		}
		else if (kind == Associable.DOUBLES && tree.getWhichAssociatedDouble(nr)!=null){
			double d = tree.getAssociatedDouble(nr, node); 
			if (MesquiteDouble.isCombinable(d) && (!controlModule.thresholdValueToShow.isCombinable() || (d>=controlModule.thresholdValueToShow.getValue()))){
				if (controlModule.percentage.getValue())
					d *= 100;
				if (showNames)
					nodeString += name+ ": ";
				if (controlModule.digits == 0 && controlModule.percentage.getValue())
					nodeString += MesquiteInteger.toString((int)(d+0.5));
				else
					nodeString += MesquiteDouble.toStringDigitsSpecified(d, controlModule.digits);
			}
		}
		else if (kind == Associable.OBJECTS && tree.getWhichAssociatedObject(nr)!=null){
			Object d = tree.getAssociatedObject(nr, node);  //Debugg.println save vector
			if (d!= null){
				if (showNames)
					nodeString += name+ ": ";
				nodeString += d.toString();
			}

		}
		return nodeString;
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

		if (showingAll || controlModule.isShowing(MesquiteTree.branchLengthName, Associable.BUILTIN)){
			double d = tree.getBranchLength(node);
			String nodeString = "";
			if (showNames)
				nodeString += MesquiteTree.branchLengthName+ ": ";
			nodeString += MesquiteDouble.toStringDigitsSpecified(d, controlModule.digits);
			nodeStrings.addElement(nodeString);
			if (nameCodes != null)
				nameCodes.addElement(new MesquiteInteger(MesquiteTree.branchLengthName, Associable.BUILTIN));
		}
		if (showingAll || controlModule.isShowing(MesquiteTree.nodeLabelName, Associable.BUILTIN)){
			if (tree.nodeIsInternal(node)){
				double d = tree.getBranchLength(node);
				String nodeString = "";
				if (showNames)
					nodeString += MesquiteTree.nodeLabelName + ": ";
				if (StringUtil.blank(tree.getNodeLabel(node)))
					nodeString  +="[none]";
				else 
					nodeString += tree.getNodeLabel(node);
				nodeStrings.addElement(nodeString);
				if (nameCodes != null)
					nameCodes.addElement(new MesquiteInteger(MesquiteTree.nodeLabelName, Associable.BUILTIN));
			}
		}
		int num = tree.getNumberAssociatedBits();
		for (int i = 0; i< num; i++){
			Bits da = tree.getAssociatedBits(i);
			if (showingAll || controlModule.isShowing(da.getName(), Associable.BITS)){
				boolean d = tree.getAssociatedBit(NameReference.getNameReference(da.getName()), node);  //Debugg.println save vector of name references
				String nodeString = "";
				if (showNames)
					nodeString += da.getName()+ ": ";
				nodeString += MesquiteBoolean.toTrueFalseString(d);
				nodeStrings.addElement(nodeString);
				if (nameCodes != null)
					nameCodes.addElement(new MesquiteInteger(da.getName(), Associable.BITS));
			}
		}
		num = tree.getNumberAssociatedLongs();
		for (int i = 0; i< num; i++){
			LongArray da = tree.getAssociatedLongs(i);
			if (showingAll || controlModule.isShowing(da.getName(), Associable.LONGS)){
				long d = tree.getAssociatedLong(NameReference.getNameReference(da.getName()), node);  //Debugg.println save vector
				String nodeString = "";
				if (showNames)
					nodeString += da.getName()+ ": ";
				nodeString += MesquiteLong.toString(d);
				nodeStrings.addElement(nodeString);
				if (nameCodes != null)
					nameCodes.addElement(new MesquiteInteger(da.getName(), Associable.LONGS));
			}
		}

		num = tree.getNumberAssociatedDoubles();
		for (int i = 0; i< num; i++){
			DoubleArray da = tree.getAssociatedDoubles(i);
			if (showingAll || controlModule.isShowing(da.getName(), Associable.DOUBLES)){
				double d = tree.getAssociatedDouble(NameReference.getNameReference(da.getName()), node); 
				if (showingAll || (MesquiteDouble.isCombinable(d) && (!controlModule.thresholdValueToShow.isCombinable() || (d>=controlModule.thresholdValueToShow.getValue())))){
					if (controlModule.percentage.getValue())
						d *= 100;
					String nodeString = "";
					if (showNames)
						nodeString += da.getName()+ ": ";
					if (controlModule.digits == 0 && controlModule.percentage.getValue())
						nodeString += MesquiteInteger.toString((int)(d+0.5));
					else
						nodeString += MesquiteDouble.toStringDigitsSpecified(d, controlModule.digits);
					nodeStrings.addElement(nodeString);
					if (nameCodes != null)
						nameCodes.addElement(new MesquiteInteger(da.getName(), Associable.DOUBLES));
				}
			}
		}
		num = tree.getNumberAssociatedObjects();
		for (int i = 0; i< num; i++){
			ObjectArray da = tree.getAssociatedObjects(i);
			if (showingAll || controlModule.isShowing(da.getName(), Associable.OBJECTS)){
				Object d = tree.getAssociatedObject(NameReference.getNameReference(da.getName()), node);  //Debugg.println save vector
					String nodeString = "";
					if (showNames)
						nodeString += da.getName()+ ": ";
					if (d == null)
						nodeString += "[none]";
					else
						nodeString += d.toString();
					nodeStrings.addElement(nodeString);
					if (nameCodes != null)
						nameCodes.addElement(new MesquiteInteger(da.getName(), Associable.OBJECTS));
				
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
		addToPopup("Branch/node number: " + branchFound, branchFound, responseNumber++);
		popupKeys.addElement(new MesquiteInteger("nodenumber", Associable.BUILTIN));
		addToPopup("-", branchFound, responseNumber++);
		popupKeys.addElement(new MesquiteInteger("dash", Associable.BUILTIN));
		String[] strings = stringsAtNode(myTree, branchFound, true, true, popupKeys);
		if (strings != null)
			for (int i = 0; i<strings.length; i++)
				addToPopup(strings[i], branchFound, responseNumber++);

		addToPopup("-", branchFound, -1);
		popup.addItem("Control Display of Properties on Tree...", ownerModule, new MesquiteCommand("showDialog", ownerModule));

		//other modules showing things on the tree
		boolean first = true;
		Enumeration e = treeDisplay.getExtras().elements();
		while (e.hasMoreElements()) {
			TreeDisplayExtra ex = (TreeDisplayExtra)e.nextElement();
			String strEx =ex.textAtNode(myTree, branchFound);
			if (!StringUtil.blank(strEx)) {
				if (first)
					addToPopup("-", branchFound, -1);
				first = false;
				if (ex.ownerModule!=null)
					strEx = ex.ownerModule.getName() + ": " + strEx;
				addToPopup(strEx, branchFound, -1);
			}
		}
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
			if (responseNumber>0 && branchFound >0 && MesquiteInteger.isCombinable(branchFound) && MesquiteInteger.isCombinable(responseNumber) ) {
				if (responseNumber<popupKeys.size()){
					MesquiteInteger mi = (MesquiteInteger)popupKeys.elementAt(responseNumber);
					String name = mi.getName();
					NameReference nameRef = NameReference.getNameReference(name);
					MesquiteWindow container = controlModule.containerOfModule();
					int kind = mi.getValue();
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
					else  if (name.equalsIgnoreCase("!color") && kind == Associable.OBJECTS && myTree.getAssociatedObject(nameRef, branchFound) instanceof String){
						String nC = MesquiteString.queryString(container, "Color", "This is the color of the branch (stored in the reserved color storage of the tree). "
								+"You can change it via the paintbrush tool, or here via hexadecimal color string, which must start with a # and the 6 digits" 
								+ " consisting of only the characters 0 to 9 or A to F (e.g., #ff0000 is red, #00ff00 is green, #0000ff is blue).", 
								(String)myTree.getAssociatedObject(nameRef, branchFound), 1);
						if (!StringUtil.blank(nC) && nC.length() == 7 && nC.charAt(0) == '#'){
							myTree.setAssociatedObject(nameRef, branchFound, nC);
							myTree.notifyListeners(this, new Notification(MesquiteListener.ASSOCIATED_CHANGED));
							return null;
						}

					}
					else  if (kind == Associable.BITS)
						controlModule.discreetAlert("This is a boolean (true/false value) attached to the branch of the tree, with name \"" + name + "\" and value \"" + stringAtNode(myTree, branchFound,  name,  kind, false) + "\".");
					else  if (kind == Associable.LONGS){
						long d = myTree.getAssociatedLong(nameRef, branchFound);  
						long dN = MesquiteLong.queryLong(container, "Integer number", "This is an integral (long) value attached to the branch of the tree, with name \"" + name + "\" "
								+"and value \"" + stringAtNode(myTree, branchFound,  name,  kind, false) + "\". You can edit it here, although there may be restrictions on its value.", d);
						if (!MesquiteLong.isUnassigned(dN)){
							myTree.setAssociatedLong(nameRef, branchFound, dN);  
							myTree.notifyListeners(this, new Notification(MesquiteListener.ASSOCIATED_CHANGED));
						}
					}
					else  if (kind == Associable.DOUBLES){
						double d = myTree.getAssociatedDouble(nameRef, branchFound);  
						double dN = MesquiteDouble.queryDouble(container, "Decimal number", "This is a decimal number (floating-point) attached to the branch of the tree, with name \"" + name + "\" and value \"" + 
								stringAtNode(myTree, branchFound,  name,  kind, false) + "\". You can edit it here, although there may be restrictions on its value.", d);
						if (!MesquiteDouble.isUnassigned(dN)){
							myTree.setAssociatedDouble(nameRef, branchFound, dN);  
							myTree.notifyListeners(this, new Notification(MesquiteListener.ASSOCIATED_CHANGED));
						}
						//controlModule.discreetAlert("This is a floating-point (double) value attached to the branch of the tree, with name \"" + name + "\" and value \"" + stringAtNode(myTree, branchFound,  name,  kind, false) + "\".");
					}
					else  if (kind == Associable.OBJECTS) {
						Object d = myTree.getAssociatedObject(nameRef, branchFound);  
						if (d instanceof String){
							String nC = MesquiteString.queryString(container,"String at node","This is a string of text, attached to the branch of the tree, with name \"" + name + "\". You can edit it here, "
									+"although it may have a specific purpose and need to be in a specific format.", (String)d, 2);
							if (nC != null){
								myTree.setAssociatedObject(nameRef, branchFound, nC);
								myTree.notifyListeners(this, new Notification(MesquiteListener.ASSOCIATED_CHANGED));
								return null;
							}
						}
						else {
							String typeName = "an unspecified object";
							if (d instanceof DoubleArray)
								typeName = "an array of floating-point numbers";
							else if (d instanceof LongArray)
								typeName = "an array of integer numbers";
							else if (d instanceof StringArray)
								typeName = "an array of strings of text";
							else if (d instanceof ObjectArray)
								typeName = "an array of unspecified objects";
							controlModule.discreetAlert("This is " + typeName +  ", attached to the branch of the tree, with name \"" + name + "\".");
						}
					}

				}
			}	 	
		}
		return null;
	}
	
	/*
	public int[] getRequestedExtraBorders(Tree tree, TreeDrawing treeDrawing){ //must return null or int[4], left, top, right, bottom
	//	String stringAtRoot = stringAtNode((MesquiteTree)tree, tree.getRoot(), false, false, false);
	//	if (StringUtil.blank(stringAtRoot))
		//return new int[]{0, 0, 0, 0};
		return new int[]{100, 80, 50, 150};
	}

	/*.................................................................................................................*/
	void myDraw(MesquiteTree tree, int node, Graphics g) {
		if (tree.withinCollapsedClade(node) || (!controlModule.showOnTerminals.getValue() && tree.nodeIsTerminal(node)))
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
		Shape ss = g.getClip();
		g.setClip(null);
		if (controlModule.horizontal.getValue())
			box.draw(g,  x, y);
		else
			box.draw(g,  x, y, 0, 1500, treeDisplay, false, false);
		g.setClip(ss);

	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int node, Graphics g) {
//Debugg.println(testing borders system
	//	g.drawRect(2, 2, getRequestedExtraBorders(tree, treeDisplay.getTreeDrawing())[0], getRequestedExtraBorders(tree, treeDisplay.getTreeDrawing())[1]);
	//	g.drawRect(treeDisplay.getField().width -getRequestedExtraBorders(tree, treeDisplay.getTreeDrawing())[2]-2, treeDisplay.getField().height -getRequestedExtraBorders(tree, treeDisplay.getTreeDrawing())[3]-2, getRequestedExtraBorders(tree, treeDisplay.getTreeDrawing())[2], getRequestedExtraBorders(tree, treeDisplay.getTreeDrawing())[3]);
		if (!controlModule.anyShowing())
			return;
		
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
		return stringAtNode((MesquiteTree) tree,  node, true, true, false);
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

