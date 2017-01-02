/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.ValuesAtNodes;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class ValuesAtNodes extends TreeDisplayAssistantMA implements LegendHolder {
	public String getName() {
		return "Values for Nodes";
	}
	public String getExplanation() {
		return "Shows on a drawn tree various possible numbers at the nodes.";
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumbersForNodes.class, getName() + "  needs a method to calculate values at the nodes of the tree.",
		"The method to calculate values at nodes can be selected initially or in the Values at Nodes submenu of the Node_Values menu");
		EmployeeNeed e2 = registerEmployeeNeed(DisplayNumbersAtNodes.class, getName() + "  uses a module to display values at nodes.",
		"The method to display values can be selected initially");
	}
	/*.................................................................................................................*/
	public NumbersForNodes numForNodesTask;
	public DisplayNumbersAtNodes displayTask;
	Vector traces;
	MesquiteString numberTaskName;
	MesquiteCommand nfntC;
	int initialOffsetX=MesquiteInteger.unassigned;
	int initialOffsetY= MesquiteInteger.unassigned;
	boolean suppress = false;

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (arguments !=null) {
			numForNodesTask = (NumbersForNodes)hireNamedEmployee(NumbersForNodes.class, arguments);
			if (numForNodesTask == null)
				return sorry(getName() + " cannot start because the requested module to calculate values was not obtained.");
		}
		else {
			numForNodesTask = (NumbersForNodes)hireEmployee(NumbersForNodes.class, "Values at nodes");
			if (numForNodesTask == null) {
				return sorry(getName() + " cannot start because no appropriate module to calculate values was obtained.");
			}
		}
		nfntC = makeCommand("setNumForNodes",  this);
		numForNodesTask.setHiringCommand(nfntC);
		numberTaskName = new MesquiteString(numForNodesTask.getName());
		displayTask = (DisplayNumbersAtNodes)hireNamedEmployee(DisplayNumbersAtNodes.class, "#ShadeNumbersOnTree");
		if (displayTask == null) 
			displayTask = (DisplayNumbersAtNodes)hireEmployee(DisplayNumbersAtNodes.class, "Choose display method for Values at Nodes");
		if (displayTask == null) {
			return sorry(getName() + " couldn't start because no display module was obtained");
		}
		setDefaultsFromNumbersForNodes();
		traces = new Vector();
		makeMenu("Node_Values");
		resetContainingMenuBar();
		if (numModulesAvailable(NumbersForNodes.class)>1){
			MesquiteSubmenuSpec mss = addSubmenu(null, "Value at Nodes", nfntC, NumbersForNodes.class);
			mss.setSelected(numberTaskName);
		}
		addMenuItem( "Transfer to Associated", makeCommand("transferToAssociated",  this));
		addMenuItem( "Close Node Values", makeCommand("closeTrace",  this));
		addMenuSeparator();
		return true;
	}
	/*.................................................................................................................*/
	private void setDefaultsFromNumbersForNodes(){
		if (displayTask!=null && numForNodesTask!=null) {
			displayTask.setShadeBranches(numForNodesTask.getDefaultShadeBranches());
			displayTask.setShadeInColor(numForNodesTask.getDefaultShadeInColor());
			displayTask.setShowLabels(numForNodesTask.getDefaultShowLabels());
			displayTask.setLabelTerminals(numForNodesTask.getDefaultLabelTerminals());
		}
	}
	/*.................................................................................................................*/

	public boolean suppliesWritableResults(){
		return traces.size()<2;
	}
	public Object getWritableResults(){
		if (traces.size() != 1)
			return null;
		ValuesAtNodesOperator trace = (ValuesAtNodesOperator)traces.elementAt(0);
		String results = trace.vModule.getName();
		for (int i = 0; i<trace.numArray.getSize(); i++){
			results += "\t" + trace.numArray.toString(i);
		}
		return results;
	}
	public Object getResultsHeading(){
		if (traces.size() != 1)
			return null;
		ValuesAtNodesOperator trace = (ValuesAtNodesOperator)traces.elementAt(0);
		String results = trace.vModule.getName();
		return results;
	}

	public boolean showLegend(){
		return true;
	}
	public int getInitialOffsetX(){
		return initialOffsetX;
	}
	public int getInitialOffsetY(){
		return initialOffsetY;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	public void employeeQuit(MesquiteModule m){
		if(m==displayTask)
			iQuit();
	}
	/*.................................................................................................................*/
	public  Class getHireSubchoice(){
		return NumbersForNodes.class;
	}
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		ValuesAtNodesOperator newTrace = new ValuesAtNodesOperator(this, treeDisplay);
		traces.addElement(newTrace);
		return newTrace;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("suppress");
		temp.addLine("setNumForNodes", numForNodesTask);
		temp.addLine("setDisplay", displayTask);
		ValuesAtNodesOperator tco = (ValuesAtNodesOperator)traces.elementAt(0);
		if (tco!=null && tco.vLegend!=null) {
			temp.addLine("setInitialOffsetX " + tco.vLegend.getOffsetX()); //Should go operator by operator!!!
			temp.addLine("setInitialOffsetY " + tco.vLegend.getOffsetY());
		}
		temp.addLine("desuppress");
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the module calculating the numbers for the nodes", "[name of module]", commandName, "setNumForNodes")) {
			NumbersForNodes temp=  (NumbersForNodes)replaceEmployee(NumbersForNodes.class, arguments, "Value to calculate at nodes", numForNodesTask);
			if (temp!=null) {
				numForNodesTask= temp;
				numForNodesTask.setHiringCommand(nfntC);
				numberTaskName.setValue(numForNodesTask.getName());
				resetAllTraceOperators();
				recalculateAllTraceOperators();
				if (!suppress)
					parametersChanged();
			}
			return numForNodesTask;
		}
		else if (checker.compare(this.getClass(), "Sets the module displaying the numbers for the nodes", "[name of module]", commandName, "setDisplay")) {
			DisplayNumbersAtNodes temp=  (DisplayNumbersAtNodes)replaceEmployee(DisplayNumbersAtNodes.class, arguments, "Displayer of numbers at nodes", displayTask);
			if (temp!=null) {
				displayTask= temp;
				setDefaultsFromNumbersForNodes();
				resetAllTraceOperators();
				recalculateAllTraceOperators();
				if (!suppress)
					parametersChanged();
			}
			return displayTask;
		}
		else if (checker.compare(this.getClass(), "Suppresses calculations", null, commandName, "suppress")) {
			suppress = true;
		}
		else if (checker.compare(this.getClass(), "Desuppresses calculations", null, commandName, "desuppress")) {
			suppress = false;
			recalculateAllTraceOperators();
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Transfers node values to information associated with nodes of tree", null, commandName, "transferToAssociated")) {
			String name = parser.getFirstToken(arguments);
			if (StringUtil.blank(name) && !MesquiteThread.isScripting())
				name = MesquiteString.queryString(containerOfModule(), "Transfer", "Name of variable to be associated with nodes of tree and to receive transfered node values (e.g., \"GCBias\")", "Untitled");
			if (!StringUtil.blank(name))
				transferAllTraceOperators(name);
		}
		else if (checker.compare(this.getClass(), "Sets initial horizontal offset of legend from home position", "[offset in pixels]", commandName, "setInitialOffsetX")) {
			MesquiteInteger pos = new MesquiteInteger();
			int offset= MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(offset)) {
				initialOffsetX = offset;

			}
		}
		else if (checker.compare(this.getClass(), "Sets initial vertical offset of legend from home position", "[offset in pixels]", commandName, "setInitialOffsetY")) {
			MesquiteInteger pos = new MesquiteInteger();
			int offset= MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(offset)) {
				initialOffsetY = offset;
			}
		}
		else if (checker.compare(this.getClass(), "Turns off the values at nodes trace", null, commandName, "closeTrace")) {
			iQuit();
			resetContainingMenuBar();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (employee == numForNodesTask)
			recalculateAllTraceOperators();
		resetAllTraceOperators();
	}
	/*.................................................................................................................*/
	public void closeAllTraceOperators() {
		if (traces==null)
			return;
		Enumeration e = traces.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof ValuesAtNodesOperator) {
				ValuesAtNodesOperator tCO = (ValuesAtNodesOperator)obj;
				tCO.turnOff();
			}
		}
	}
	/*.................................................................................................................*/
	public void resetAllTraceOperators() {
		if (traces==null)
			return;
		Enumeration e = traces.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof ValuesAtNodesOperator) {
				ValuesAtNodesOperator tCO = (ValuesAtNodesOperator)obj;
				if (tCO.decorator!=null)
					tCO.decorator.turnOff();
				tCO.decorator = null;
				if (tCO.treeDisplay!=null)
					tCO.treeDisplay.pleaseUpdate(false);
				//tCO.traceLegend.characterScroll.setMaximumValue(characterSourceTask.getNumberOfCharacters());
				if (tCO.vLegend!=null)
					tCO.vLegend.repaint();
			}
		}
	}
	/*.................................................................................................................*/
	public void recalculateAllTraceOperators() {
		if (traces==null || suppress)
			return;
		Enumeration e = traces.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof ValuesAtNodesOperator) {
				ValuesAtNodesOperator tCO = (ValuesAtNodesOperator)obj;
				tCO.doCalculations();
				//tCO.traceLegend.characterScroll.setCurrentValue(currentChar);
			}
		}
	}
	/*.................................................................................................................*/
	public void transferAllTraceOperators(String name) {
		if (traces==null)
			return;
		Enumeration e = traces.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof ValuesAtNodesOperator) {
				ValuesAtNodesOperator tCO = (ValuesAtNodesOperator)obj;
				tCO.transferNumbersToAssociated(name);
				//tCO.traceLegend.characterScroll.setCurrentValue(currentChar);
			}
		}
	}

	/*.................................................................................................................*/
	public void endJob() {
		closeAllTraceOperators();
		super.endJob();
	}
	/*.................................................................................................................*/
	/** returns current parameters, for logging etc..*/
	public String getParameters() {
		return "Values calculated for nodes: " + numForNodesTask.getName();
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}

	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
}

/* ======================================================================== */
class ValuesAtNodesOperator extends TreeDisplayDrawnExtra {
	private Tree myTree;
	ValuesAtNodes vModule;
	public TDLegendWithColors vLegend;
	public TreeDecorator decorator;
	private boolean holding = false;
	NumberArray numArray;
	static Color lilac;
	static {
		lilac = new Color(210, 170, 255);
	}
	public ValuesAtNodesOperator (ValuesAtNodes ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		vModule = ownerModule;
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree){
		myTree = tree;
		if ((vModule.numForNodesTask!=null) && (vModule.displayTask!=null)) {
			doCalculations();
		}
	}
	/*.................................................................................................................*/
	public   Tree getTree(){
		return myTree;
	}
	/*.................................................................................................................*/
	public   Taxa getTaxa(){
		if (myTree !=null)
			return myTree.getTaxa();
		else
			return null;
	}
	public void transferNumbersToAssociated(String name){
		if (myTree == null || numArray == null) 
			return;
		if (myTree instanceof MesquiteTree) {
			MesquiteTree tree = (MesquiteTree)myTree;
			NameReference nr = NameReference.getNameReference(name);
			transfer(tree, tree.getRoot(), numArray, nr);
			tree.notifyListeners(this, new Notification(MesquiteListener.UNKNOWN));
		}
	}
	/*.................................................................................................................*/
	public   void transfer(MesquiteTree tree, int node, NumberArray numArray, NameReference nr) {
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			transfer(tree, d, numArray, nr);
		if (numArray.getValueClass() == NumberArray.DOUBLE)
			tree.setAssociatedDouble(nr, node, numArray.getDouble(node));
		else 
			tree.setAssociatedLong(nr, node, numArray.getLong(node));
	}		
	/*.................................................................................................................*/
	public void doCalculations() {
		holding = true;
		if (myTree == null) {
			System.out.println("tree null in Values at Nodes calculations");
		}
		else {

			vModule.displayTask.onHold();
			if (vLegend!=null) {
				vLegend.onHold();
				vLegend.setTitle(vModule.numForNodesTask.getName());
			}

			if (numArray == null || numArray.getSize() < myTree.getNumNodeSpaces()) 
				numArray = new NumberArray(myTree.getNumNodeSpaces());
			else
				numArray.deassignArray();
			vModule.numForNodesTask.calculateNumbers(myTree, numArray, resultString);
			
			int drawnRoot = treeDisplay.getTreeDrawing().getDrawnRoot();
			if (!myTree.nodeExists(drawnRoot))
				drawnRoot = myTree.getRoot();
			if (decorator==null) {
				decorator = vModule.displayTask.createTreeDecorator(treeDisplay, this);
				decorator.setThemeColor(lilac);
			}
			decorator.calculateOnTree(myTree, drawnRoot, numArray);
			vModule.displayTask.offHold();
			holding = false;
			treeDisplay.pleaseUpdate(false);
			if (vLegend!=null) {
				vLegend.offHold();
				vLegend.repaint();
			}
		}
	}
	MesquiteString resultString = new MesquiteString();

	String getResultString(){ //not used currently
		if (StringUtil.blank(resultString.getValue()))
			return "";
		else
			return " [" + resultString + "]";
	}
	public void cursorEnterBranch(Tree tree, int N, Graphics g) {
		if (vLegend!=null)
			vLegend.setMessage(textAtNode(tree, N));
	}
	public void cursorExitBranch(Tree tree, int N, Graphics g) {
		if (vLegend!=null)
			vLegend.setMessage("");
	}
	/**return a text version of information at node*/
	public String textAtNode(Tree tree, int node){
		if (numArray== null)
			return "?";
		else {
			String s = numArray.toString(node);
			if (StringUtil.blank(s))
				return "?";
			else
				return s;
		}
	}
	/**return a text version of any legends or other explanatory information*/
	public String textForLegend(){
		return "Values at nodes: "  + vModule.numForNodesTask.getNameAndParameters();
	}
	public String getNote(){
		return vModule.numForNodesTask.getNameAndParameters();
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		boolean toShow = false;
		if (!holding) {
			if (vLegend==null) {
				vLegend = new TDLegendWithColors(vModule, treeDisplay, new ParamString(this), vModule.numForNodesTask.getName(), Color.black);
				addPanelPlease(vLegend);
				toShow = true;
			}
			//decorator or vLegend being set to null; MUST PROTECT
			try {
				if (vModule.numForNodesTask!=null) {
					if (decorator==null) {
						decorator = vModule.displayTask.createTreeDecorator(treeDisplay, this); 
						decorator.setThemeColor(lilac);
					}
					decorator.drawOnTree(tree, drawnRoot, numArray, null, null, g);
					//vLegend.setValues(numArray);
					vLegend.adjustLocation();
					vLegend.setColorRecords(decorator.getLegendColorRecords());
					if (toShow || !vLegend.isVisible())
						vLegend.setVisible(true);
				}
			}
			catch (Exception e){
			}
		}
	}
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
		//vLegend.print(g);
	}
	public void turnOff(){
		if (vLegend!=null && treeDisplay!=null)
			removePanelPlease(vLegend);
		super.turnOff();
	}
}
class ParamString extends MesquiteString {
	ValuesAtNodesOperator vANO;
	public ParamString (ValuesAtNodesOperator vANO){
		super();
		this.vANO = vANO;
	}
	public String getValue(){
		return vANO.getNote();
	}
}


