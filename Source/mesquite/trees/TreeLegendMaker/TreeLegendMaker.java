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
package mesquite.trees.TreeLegendMaker;

import java.awt.*;
import java.net.*;
import java.util.*;
import java.io.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;


public class TreeLegendMaker extends TreeDisplayAssistantMA  {
	private Vector legendOperators;
	private ListableVector legends;

	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumberForTree.class, getName() + " needs methods to calculate values that pertain to the tree in the tree window, such as a parsimony score, an imbalance statistic, and so on.", 
		"The Values For Current Tree legend showing values for current tree can be started under the Analysis menu of the Tree Window.  You may add new values to display either when the legend starts, or later using the Legend menu");
		e.setPriority(1);
	}

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		makeMenu("Legend");
		resetContainingMenuBar();
		addSubmenu(null, "Show", makeCommand("newLegendItem",  this), NumberForTree.class);
		addMenuSeparator();
		MesquiteSubmenuSpec mCloseAsst = addSubmenu(null, "Close");
		mCloseAsst.setList(getEmployeeVector());
		mCloseAsst.setCommand(makeCommand("closeEmployee",  this));

		addMenuItem( "Close Values Legend", makeCommand("closeLegend",  this));

		legendOperators = new Vector();
		legends = new ListableVector();
		boolean success = true;
		if (arguments !=null) {
			hireNamedEmployee(NumberForTree.class, arguments);
		}
		else if (!MesquiteThread.isScripting()) {
			hireEmployee(NumberForTree.class, "Choose information to be displayed in Values for Current Tree legend.  If you hit Cancel now, the tree legend will appear with only the tree name.");
			/*Listable[] list = MesquiteTrunk.mesquiteModulesInfoVector.getModulesOfDuty(NumberForTree.class, null, this);
 			if (list!=null && list.length>0) {
 				ListableVector possibleAssistants = new ListableVector();
 				for (int i = 0; i<list.length; i++)
 					possibleAssistants.addElement(list[i], false);
				success = false;
				Listable[] hires = ListDialog.queryListMultiple(containerOfModule(), "Tree legend", "Choose information to be displayed in tree legend.  If you hit Cancel now, the tree legend will appear with only the tree name.",MesquiteString.helpString, possibleAssistants, null);
 				if (hires!=null) {
 					for (int i = 0; i<hires.length; i++) {
						MesquiteModule mb = hireEmployeeFromModuleInfo ((MesquiteModuleInfo)hires[i], NumberForTree.class);
						if (mb != null)
							success = true;
					}
				}
 			}*/
		}

		return success;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*
	public  Class getHireSubchoice(){
		return NumberForTree.class;
	}
	 */
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) { //todo: pass scripting
		TreeLegendOperator newLegend =new TreeLegendOperator(this, treeDisplay);
		legendOperators.addElement(newLegend);
		legends.addElement(newLegend.treeLegend, false);
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof NumberForTree) {
				NumberForTree startupNumber = (NumberForTree)e;
				if (newLegend.treeLegend !=null){
					MesquiteNumber cNum = new MesquiteNumber();
					MesquiteString resultString = new MesquiteString();
					if (newLegend.getTree()!=null){
						startupNumber.calculateNumber(newLegend.getTree(), cNum, resultString);
						newLegend.treeLegend.setString(resultString.toString(), startupNumber.needsMenu(), newLegend.treeLegend.getNumStrings(), startupNumber.getID()); //TODO: maybe pass MesquiteString also in calculateNumber?
						newLegend.treeLegend.repaint();
					}
				}
			}
		}
		resetContainingMenuBar();
		return newLegend;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		if (legendOperators.size()<=0)
			return null;
		Snapshot temp = new Snapshot();
		TreeLegendOperator tL = (TreeLegendOperator)legendOperators.elementAt(0);
		temp.addLine("setOffsetsX " + tL.treeLegend.getOffsetX());
		temp.addLine("setOffsetsY " + tL.treeLegend.getOffsetY());
		temp.addLine("getLegendsVector");
		temp.addLine("tell It");
		//to set details of legends, send commands to vector which will pass on to all of contents;
		temp.addLine("distributeCommands");
		temp.incorporate(tL.treeLegend.getSnapshot(file), true);
		temp.addLine("endDistributeCommands");
		temp.addLine("endTell");
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof NumberForTree) {
				temp.addLine("newLegendItemNoCalc ", ((MesquiteModule)e));
			}
		}
		temp.addLine("calculate");
		return temp;
	}

	public void employeeQuit(MesquiteModule m){
		if (m == null)
			return;

		for (int j=0; j<legends.size(); j++) {
			Object obj = legends.elementAt(j);
			if (obj instanceof TreeLegend) {
				TreeLegend treeLegend = (TreeLegend)obj;
				int line = treeLegend.findLine(m.getID());
				if (line>=0)
					treeLegend.zapLine(line);
				treeLegend.repaint();
			}
		}
	}
	public String nameForWritableResults(){
		EmployeeVector ev = getEmployeeVector();
		if (ev.size() == 0)
			return null;
		String results = "";
		for (int i = 0; i<ev.size(); i++){
			if (i != 0)
				results += "; ";
			results += ((MesquiteModule)ev.elementAt(i)).getNameAndParameters();
		}
		return results;
	}
	
	public boolean suppliesWritableResults(){
		EmployeeVector ev = getEmployeeVector();
		if (ev == null || ev.size() == 0)
			return false;
		
		return true;
	}
	public Object getWritableResults(){
		String results = "";
		Enumeration e = legendOperators.elements();
		boolean first = true;
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof TreeLegendOperator) {
				TreeLegendOperator tLO = (TreeLegendOperator)obj;
				if (!first)
					results += "\t";
				first = false;
				results += tLO.getWritableResults();
			}
		}
		return results;
	}
	public Object getResultsHeading(){
		EmployeeVector ev = getEmployeeVector();
		if (ev.size() == 0)
			return null;
		String results = "";
		for (int i = 0; i<ev.size(); i++){
			if (i != 0)
				results += "\t";
			results += ((MesquiteModule)ev.elementAt(i)).getNameAndParameters();
		}
		return results;
	}


	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Turns off the Values for Current Tree legend", null, commandName, "closeLegend")) {
			closeAllLegends();
			iQuit();
		}
		else if (checker.compare(this.getClass(), "Returns the Vector of legends", null, commandName, "getLegendsVector")) {
			return legends;
		}
		else if (checker.compare(this.getClass(), "Turns off one of the modules employed to display a line in the tree legend", "[employee number]", commandName, "closeEmployee")) {
			MesquiteInteger pos = new MesquiteInteger(0);
			int ox = MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(ox)){
				for (int i=0; i<legends.size(); i++) {
					Object obj = legends.elementAt(i);
					if (obj instanceof TreeLegend) {
						TreeLegend treeLegend = (TreeLegend)obj;
						treeLegend.zapLine(ox+1);
						treeLegend.repaint();
					}
				}
				EmployeeVector ev = getEmployeeVector();
				if (ev==null)
					return null;
				for (int i=0; i< ev.size(); i++){
					MesquiteModule mb = (MesquiteModule)ev.elementAt(i);
					if (mb!=null) {
						if (i== ox) {
							fireEmployee(mb);
							return null;
						}
					}
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets the initial horizontal offset of the legend", "[offset]", commandName, "setOffsetsX")) {
			MesquiteInteger pos = new MesquiteInteger(0);
			int ox = MesquiteInteger.fromString(arguments, pos);
			if (MesquiteInteger.isCombinable(ox)){
				Enumeration e = legendOperators.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					if (obj instanceof TreeLegendOperator) {
						TreeLegendOperator tLO = (TreeLegendOperator)obj;
						tLO.treeLegend.setOffsetX(ox);
					}
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets the initial vertical offset of the legend", "[offset]", commandName, "setOffsetsY")) {
			MesquiteInteger pos = new MesquiteInteger(0);
			int oy = MesquiteInteger.fromString(arguments, pos);
			if (MesquiteInteger.isCombinable(oy)){
				Enumeration e = legendOperators.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					if (obj instanceof TreeLegendOperator) {
						TreeLegendOperator tLO = (TreeLegendOperator)obj;
						tLO.treeLegend.setOffsetY(oy);
					}
				}
			}
		}
		else if (checker.compare(this.getClass(), "Hires a module to display a new line in the tree legend", "[name of module]", commandName, "newLegendItem")) {
			incrementMenuResetSuppression();
			NumberForTree ntt= (NumberForTree)hireNamedEmployee(NumberForTree.class, arguments);
			decrementMenuResetSuppression();
			if (ntt!=null) {
				ntt.setUseMenubar(true);  //June 07: changed to true, DRM
				MesquiteNumber cNum = new MesquiteNumber();
				MesquiteString resultString = new MesquiteString();

				Enumeration e = legendOperators.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					if (obj instanceof TreeLegendOperator) {
						cNum.setToUnassigned();
						resultString.setValue("");
						TreeLegendOperator tLO = (TreeLegendOperator)obj;
						ntt.calculateNumber(tLO.getTree(), cNum, resultString);
						tLO.treeLegend.setString(resultString.toString(), ntt.needsMenu(), whichString(ntt), ntt.getID()); //TODO: maybe pass MesquiteString also in calculateNumber?
						tLO.treeLegend.repaint();
					}
				}
				resetContainingMenuBar();
			}
			return ntt;
		}
		else if (checker.compare(this.getClass(), "Hires a module to display a new line in the tree legend", "[name of module]", commandName, "newLegendItemNoCalc")) {
			incrementMenuResetSuppression();
			NumberForTree ntt= (NumberForTree)hireNamedEmployee(NumberForTree.class, arguments);
			decrementMenuResetSuppression();
			if (ntt!=null) {
				ntt.setUseMenubar(true);  //June 07: changed to true, DRM
				resetContainingMenuBar();
			}
			return ntt;
		}
		else if (checker.compare(this.getClass(), "Hires a module to display a new line in the tree legend", "[name of module]", commandName, "calculate")) {
			MesquiteNumber cNum = new MesquiteNumber();
			MesquiteString resultString = new MesquiteString();
			ListableVector v = getEmployeeVector();
			for (int i=0; i<v.size(); i++){
				if (v.elementAt(i) instanceof NumberForTree){
					NumberForTree ntt = (NumberForTree)v.elementAt(i);
					Enumeration e = legendOperators.elements();
					while (e.hasMoreElements()) {
						Object obj = e.nextElement();
						if (obj instanceof TreeLegendOperator) {
							cNum.setToUnassigned();
							resultString.setValue("");
							TreeLegendOperator tLO = (TreeLegendOperator)obj;
							if (tLO.getTree()!= null){
								ntt.calculateNumber(tLO.getTree(), cNum, resultString);
								tLO.treeLegend.setString(resultString.toString(), ntt.needsMenu(), whichString(ntt), ntt.getID()); //TODO: maybe pass MesquiteString also in calculateNumber?
								tLO.treeLegend.repaint();
							}
						}
					}
				}
			}
		}
		else 
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	public void closeAllLegends() {
		Enumeration e = legendOperators.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof TreeLegendOperator) {
				TreeLegendOperator tCO = (TreeLegendOperator)obj;
				tCO.turnOff();
			}
		}
	}
	/*.................................................................................................................*/
	public void employeeOutputInvalid(MesquiteModule employee, MesquiteModule source) {
		if (employee !=null) {
			int sn = getEmployeeVector().indexOf(employee);
			Enumeration e = legendOperators.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof TreeLegendOperator) {
					TreeLegendOperator tLO = (TreeLegendOperator)obj;
					if (tLO.treeLegend!=null) {
						tLO.treeLegend.blank(sn+1);
					}
				}
			}
		}
	}
	/*.................................................................................................................*/
	public int whichString(MesquiteModule employee) {
		if (employee !=null) {
			return getEmployeeVector().indexOf(employee) +1;
		}
		return -1;
	}
	boolean 	treeIsEdited(){
		MesquiteModule mb = getEmployer();
		if (mb instanceof TreeWindowMaker)
			return ((TreeWindowMaker)mb).treeIsEdited();
		return false;
	}

	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (employee !=null) {
			int sn = getEmployeeVector().indexOf(employee);
			Enumeration e = legendOperators.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof TreeLegendOperator) {
					TreeLegendOperator tLO = (TreeLegendOperator)obj;
					if (tLO.treeLegend!=null) {
						tLO.treeLegend.blank(sn+1);
					}
				}
			}

			MesquiteNumber cNum = new MesquiteNumber();
			MesquiteString resultString = new MesquiteString();
			e = legendOperators.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof TreeLegendOperator) {
					TreeLegendOperator tLO = (TreeLegendOperator)obj;
					if (tLO.treeLegend==null)
						System.out.println("Tree legend null");
					else {
						cNum.setToUnassigned();
						resultString.setValue("");
						((NumberForTree)employee).calculateNumber(tLO.getTree(), cNum, resultString);
						tLO.treeLegend.setString(resultString.toString(), employee.needsMenu(), sn +1, employee.getID()); 
						tLO.treeLegend.repaint();
					}
				}
			}
		}
	}
	/*.................................................................................................................*/
	public void endJob() {
		closeAllLegends();
		super.endJob();
		resetContainingMenuBar();
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Values for Current Tree...";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Values for Current Tree";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Makes the legend in a tree window to display the tree name and values pertaining to the tree.";
	}
}
/* ======================================================================== */
class TreeLegendOperator extends TreeDisplayDrawnExtra {
	private Tree myTree;
	private TreeLegendMaker legendModule;
	public TreeLegend treeLegend;


	public TreeLegendOperator (TreeLegendMaker ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		this.legendModule=ownerModule;
		treeLegend = new TreeLegend(legendModule, this);
		addPanelPlease(treeLegend);
	}
	/*.................................................................................................................*/
	public   Tree getTree() {
		return myTree;
	}
	/*.................................................................................................................*/
	public void lineTouched(int which, int where){
		int i=0;
		if (legendModule.getEmployeeVector()!=null) {
			Enumeration e = legendModule.getEmployeeVector().elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof NumberForTree) {
					i++;
					if (i==which) {
						((MesquiteModule)obj).showPopUp(treeLegend, 0, where + 12);
						return;
					}
				}
			}
		}
	}
	String writableResults;
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		myTree = tree;
		int i=0;
		if (tree == null)
			return;
		if (legendModule.treeIsEdited())
			treeLegend.setString("Tree: Modified from " + tree.getName(), false, i, -1);
		else 	
			treeLegend.setString("Tree: " + tree.getName(), false, i, -1);
		MesquiteNumber cNum = new MesquiteNumber();
		MesquiteString resultString = new MesquiteString();
		writableResults = "";
		if (legendModule.getEmployeeVector()!=null) {
			Enumeration e = legendModule.getEmployeeVector().elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof NumberForTree) {
					NumberForTree dT = (NumberForTree)obj;
					cNum.setToUnassigned();
					resultString.setValue("");
					dT.calculateNumber(tree, cNum, resultString);
					writableResults += dT.getNameAndParameters() + "\t" + cNum.toString();
					i++;
					treeLegend.setString(resultString.toString(), dT.needsMenu(), i, dT.getID());
				}
			}
		}
		if (treeLegend!=null) {
			treeLegend.invalidate();
			treeLegend.repaint();
		}
	}
	String getWritableResults(){
		return writableResults;
	}
	/*.................................................................................................................*/
	public   void placeLegend(Tree tree, int drawnRoot) {
		if (!treeLegend.isVisible()) {
			treeLegend.setVisible(true);
		}

	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {

		placeLegend(tree, drawnRoot);
	}
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		placeLegend(tree, drawnRoot);
		//treeLegend.print(g);
	}

	/**return a text version of any legends or other explanatory information*/
	public String textForLegend(){
		return treeLegend.getTextVersion();
	}
	public void turnOff() {
		if (treeLegend!=null && treeDisplay!=null)
			removePanelPlease(treeLegend);
		super.turnOff();
	}
}

/* ======================================================================== */
class TreeLegend extends Legend implements Listable {
	TreeLegendMaker ownerModule;
	TreeLegendOperator legendOperator;
	int legendWidth=100;
	int lineHeight=16;
	int topEdge = 6;
	int legendHeight=16;
	int descent = 0;
	int lineGap = 4;
	boolean dragging = false;
	boolean invalid;
	String[] lines;
	boolean[] dropDown;
	long[] employeeID;
	int numLines=0;
	Polygon dropDownTriangle;
	int maxNumLines = 32;
	StringInABox box;
	int[] lineBottom;
	public TreeLegend(TreeLegendMaker ownerModule, TreeLegendOperator legendOperator) {
		super(100, 16);
		this.ownerModule = ownerModule;
		this.legendOperator = legendOperator;
		//setBackground(getParent().getBackground());
		box = new StringInABox("", getFont(), 300);
		setLayout(null);
		setSize(legendWidth, legendHeight);
		lines = new  String [maxNumLines];
		lineBottom = new int[maxNumLines];
		dropDown = new boolean[maxNumLines];
		employeeID = new long[maxNumLines];
		dropDownTriangle=MesquitePopup.getDropDownTriangle();
	}

	public String getName(){
		return "Values for Current Tree Legend";
	}
	public void invalidate(){
		invalid = true;
	}
	boolean usingStringBox = false;
	private void setLegendDimensions() {
		FontMetrics fm = getFontMetrics(getFont());
		descent = fm.getMaxDescent();
		lineHeight = fm.getMaxAscent()+ descent+ lineGap;

		legendHeight = (numLines) * lineHeight+topEdge - 1;
		legendWidth=0;
		usingStringBox = false;
		for (int i=0 ; i<numLines; i++) {

			if (!StringUtil.blank(lines[i]) && fm.stringWidth(lines[i])> legendWidth)
				legendWidth = fm.stringWidth(lines[i]) + 4;
			lineBottom[i] = topEdge + (i+1)*lineHeight;
		}
		legendWidth+= 11;
		if (legendWidth > 300){
			legendHeight = 0;
			legendWidth = 300;
			usingStringBox = true;
			for (int i=0 ; i<numLines; i++) {				
				if (!StringUtil.blank(lines[i])){
					box.setStringAndFontAndWidth(lines[i], getFont(), 300, getGraphics());
					legendHeight += box.getHeight() + 4;
					lineBottom[i] = legendHeight;
				}
			}
		}
		if (legendHeight <20)
			legendHeight = 20;
		setSize(legendWidth, legendHeight);
	}
	public int findLine(long id) {
		for (int i=1; i<employeeID.length; i++){
			if (id == employeeID[i])
				return i;
		}
		return -1;
	}
	public void zapLine(int index) {
		if (index<maxNumLines) {
			if (index< numLines) {
				numLines--;
			}
			lines[index]=null;
			dropDown[index] = false;
			employeeID[index] = -1;
			for (int i=index; i<lines.length-1; i++){
				lines[i]=lines[i+1];
				dropDown[i] = dropDown[i+1];
				employeeID[i] = employeeID[i+1];
			}
			setLegendDimensions();
		}
	}
	public void setString(String s, boolean needsMenu, int index, long id) {
		if (index >=0 && index<maxNumLines) {
			if (index> numLines-1) {
				numLines = index+1;
			}
			lines[index]=s;
			dropDown[index] = needsMenu;
			employeeID[index] = id;
			setLegendDimensions();
		}
	}

	public int getNumStrings() {
		return numLines;
	}

	private int lineBottom(int index) {
		return lineBottom[index];  //topEdge + (index+1)*lineHeight; //12 +topEdge + (index)*lineHeight + descent;
	}
	public void place(boolean p) {
	}
	public void blank(int whichLine) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		Graphics g = getGraphics();
		if (g!=null){
			g.setColor(getBackground());
			g.fillRect(2, lineBottom(whichLine-1), legendWidth-1, lineHeight-1);
			g.dispose();
		}
		MesquiteWindow.uncheckDoomed(this);
	}
	public void paint(Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		invalid = false;
		int icount=-1;
		g.setColor(Color.cyan);
		legendWidth = getBounds().width;
		legendHeight = getBounds().height;
		g.drawRect(0, 0, legendWidth-1, legendHeight-1);
		g.fillRect(0, 0, legendWidth-1, topEdge);
		g.drawLine(0, topEdge, legendWidth-1,topEdge);
		g.setColor(Color.black);
		int prevBottom = 0;
		for (int i=0; i<numLines; i++) {
			g.setColor(Color.black);
			icount++;
			if (!usingStringBox)
				g.drawString(lines[i], 4, lineBottom(icount)-descent-4);
			else {
				box.setString(lines[i]);
				box.draw(g, 4, prevBottom);
			}
			prevBottom = lineBottom(icount);
			g.setColor(Color.cyan);
			g.drawLine(0, lineBottom(icount), legendWidth-1,lineBottom(icount));
			if (dropDown[i]) {
				dropDownTriangle.translate(legendWidth-7,lineBottom(icount-1)+1);
				g.setColor(Color.white);
				g.drawPolygon(dropDownTriangle);
				g.setColor(Color.black);
				g.fillPolygon(dropDownTriangle);
				dropDownTriangle.translate(-(legendWidth-7),-(lineBottom(icount-1)+1));
			}
		}
		if (invalid)
			repaint();
		MesquiteWindow.uncheckDoomed(this);
	}
	public String getTextVersion() {
		String s="";
		for (int i=0; i<numLines; i++) {
			s+=lines[i]+"\n";
		}
		return s;
	}
	public void setFont (Font f){

		super.setFont(f);
		setLegendDimensions();
	}
	public void printAll(Graphics g) {
		invalid = false;
		int icount=-1;
		g.setColor(Color.black);
		for (int i=0; i<numLines; i++) 
			g.drawString(lines[i], 4, 10 +topEdge + (++icount)*lineHeight);

		if (invalid)
			repaint();
	}
	private int whichLine(int y) {
		for (int i=0; i<=numLines; i++) {
			if (lineBottom(i)>y)
				return i;
		}
		return -1;
	}
	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		if (y<=topEdge) {
			super.mouseDown(modifiers, clickCount, when, x, y, tool);
		}
		else	 { 
			int w = whichLine(y);
			if (w>0)
				legendOperator.lineTouched(w,y);
			else
				super.mouseDown(modifiers, clickCount, when, x, y, tool);
		}
	}
}


