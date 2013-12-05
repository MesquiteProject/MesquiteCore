/* Mesquite source code (Rhetenor package).  Copyright 1997-2010 E. Dyreson and W. Maddison. 
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.rhetenor.NodeLocs3DPlot;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.rhetenor.lib.*;

/* ======================================================================== */
public class NodeLocs3DPlot extends NodeLocsPlot3D {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumbersForNodesIncr.class, getName() + "  needs a method to calculate positions of nodes.",
		"The method to calculate positions can be selected initially or in the Node Values submenu");
	}
	/*.................................................................................................................*/
	Point[] location;
	double[] z;
	boolean[] badLocs;
	NumbersForNodesIncr numbersForNodesTask;
	int margin = 40;
	TextRotator textRotator;
	MesquiteString numberTaskName;
	MesquiteNumber tempNum;
	Vector extras;
	boolean veryFirstTime=true;
	int initialOffsetH = MesquiteInteger.unassigned;
	int initialOffsetV = MesquiteInteger.unassigned;
	public MesquiteBoolean showLegend, showAxes, showAxisPlanes;
	int currentX = 0;
	int currentY=1;
	int currentZ=2;
	String xString, yString, zString;
	boolean hide = false;
	MesquiteCommand nfntC;
	double phi, theta, rho, D;
	MesquiteCommand setThetaCommand, setPhiCommand, setDCommand;

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		extras = new Vector();
		phi =   theta = Math.PI/4;
		D = MesquiteDouble.unassigned;
		numbersForNodesTask= (NumbersForNodesIncr)hireEmployee(NumbersForNodesIncr.class, "Values to calculate for axes");
		if (numbersForNodesTask == null )
			return sorry(getName() + " couldn't start because no modules calculating numbers for nodes obtained.");
		nfntC = makeCommand("setAxis",  this);
		numbersForNodesTask.setHiringCommand(nfntC);
		showLegend = new MesquiteBoolean(true);
		showAxes = new MesquiteBoolean(true);
		showAxisPlanes = new MesquiteBoolean(true);
		addCheckMenuItem(null, "Show Plot Legend", makeCommand("toggleShowLegend",  this), showLegend);
		addCheckMenuItem(null, "Show Axes", makeCommand("toggleShowAxes",  this), showAxes);
		addCheckMenuItem(null, "Show Axis Planes", makeCommand("toggleShowAxisPlanes",  this), showAxisPlanes);
		makeMenu("Plot");
		numberTaskName = new MesquiteString(numbersForNodesTask.getName());
		if (numModulesAvailable(NumbersForNodesIncr.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Node Values", nfntC, NumbersForNodesIncr.class);
			mss.setSelected(numberTaskName);
		}
		addMenuItem("Theta", makeCommand("setTheta",  this));
		addMenuItem("Phi", makeCommand("setPhi",  this));

		setThetaCommand = makeCommand("setTheta",  this);
		setPhiCommand = makeCommand("setPhi",  this);

		setDCommand =makeCommand("setD",  this);

		tempNum = new MesquiteNumber();
		MesquiteSubmenuSpec xsub = addSubmenu(null, "X Axis");
		addItemToSubmenu(null, xsub, "Next", makeCommand("nextX",  this));
		addItemToSubmenu(null, xsub, "Previous", makeCommand("previousX",  this));
		addItemToSubmenu(null, xsub, "Choose", makeCommand("setX",  this));

		MesquiteSubmenuSpec ysub = addSubmenu(null, "Y Axis");
		addItemToSubmenu(null, ysub, "Next", makeCommand("nextY",  this));
		addItemToSubmenu(null, ysub, "Previous", makeCommand("previousY",  this));
		addItemToSubmenu(null, ysub, "Choose", makeCommand("setY",  this));

		MesquiteSubmenuSpec zsub = addSubmenu(null, "Z Axis");
		addItemToSubmenu(null, zsub, "Next", makeCommand("nextZ",  this));
		addItemToSubmenu(null, zsub, "Previous", makeCommand("previousZ",  this));
		addItemToSubmenu(null, zsub, "Choose", makeCommand("setZ",  this));
		return true;
	}

	/*.................................................................................................................*/

	public void endJob(){
		if (extras!=null) {
			for (int i=0; i<extras.size(); i++){
				TreeDisplayExtra extra = (TreeDisplayExtra)extras.elementAt(i);
				if (extra!=null){
					TreeDisplay td = extra.getTreeDisplay();
					extra.turnOff();
					if (td!=null)
						td.removeExtra(extra);
				}
			}
			extras.removeAllElements();
		}
		super.endJob();
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
	public boolean showCitation(){
		return true;
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		adjustScrolls();
		super.employeeParametersChanged(employee, source, notification);
	}
	public NodeLocs3DPlotExtra getFirstExtra(){
		if (extras!=null) {
			for (int i=0; i<extras.size(); i++){
				NodeLocs3DPlotExtra extra = (NodeLocs3DPlotExtra)extras.elementAt(i);
				if (extra!=null){
					return extra;
				}
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine( "hide");
		temp.addLine( "setAxis " , numbersForNodesTask);
		temp.addLine("toggleShowLegend " + showLegend.toOffOnString());
		temp.addLine("toggleShowAxes " + showAxes.toOffOnString());
		temp.addLine("toggleShowAxisPlanes " + showAxisPlanes.toOffOnString());
		if (getFirstExtra()!=null && getFirstExtra().legend!=null) {
			temp.addLine("setInitialOffsetH " + getFirstExtra().legend.getOffsetX()); //Should go operator by operator!!!
			temp.addLine("setInitialOffsetV " + getFirstExtra().legend.getOffsetY());
		}
		if (MesquiteDouble.isCombinable(theta))
			temp.addLine( "setTheta " +  MesquiteDouble.toString(theta));
		if (MesquiteDouble.isCombinable(phi))
			temp.addLine( "setPhi " +  MesquiteDouble.toString(phi));
		if (MesquiteDouble.isCombinable(D))
			temp.addLine( "setD " + MesquiteDouble.toString(D));
		temp.addLine( "setCurrentX " + CharacterStates.toExternal(currentX));
		temp.addLine( "setCurrentY " + CharacterStates.toExternal(currentY));
		temp.addLine( "setCurrentZ " + CharacterStates.toExternal(currentZ));
		temp.addLine( "show");
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the module that calculates coordinates for the nodes", "[name of module]", commandName, "setAxis")) {
			NumbersForNodesIncr temp =  (NumbersForNodesIncr)replaceEmployee(NumbersForNodesIncr.class, arguments, "Value for axes", numbersForNodesTask);
			if (temp!=null) {
				numbersForNodesTask = temp;
				numbersForNodesTask.setHiringCommand(nfntC);
				numberTaskName.setValue(numbersForNodesTask.getName());
				resetContainingMenuBar();
				parametersChanged();
			}
			return temp;
		}

		else if (checker.compare(this.getClass(), "Hides the plot", null, commandName, "hide")) {
			hide = true;
		}
		else if (checker.compare(this.getClass(), "Shows the plot", null, commandName, "show")) {
			hide = false;
		}
		else if (checker.compare(this.getClass(), "Sets the current item displayed on the x axis (used by scripting, before display)", "[number of item]", commandName, "setCurrentX")) {
			int ic = CharacterStates.toInternal(MesquiteInteger.fromString(arguments, new MesquiteInteger(0)));
			currentX = ic;
			numbersForNodesTask.setCurrent(ic);
			if (!MesquiteThread.isScripting())
				parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets the current item displayed on the y axis (used by scripting, before display)", "[number of item]", commandName, "setCurrentY")) {
			int ic = CharacterStates.toInternal(MesquiteInteger.fromString(arguments, new MesquiteInteger(0)));
			currentY = ic;
			numbersForNodesTask.setCurrent(ic);
			if (!MesquiteThread.isScripting())
				parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets the current item displayed on the z axis (used by scripting, before display)", "[number of item]", commandName, "setCurrentZ")) {
			int ic = CharacterStates.toInternal(MesquiteInteger.fromString(arguments, new MesquiteInteger(0)));
			currentZ = ic;
			numbersForNodesTask.setCurrent(ic);
			if (!MesquiteThread.isScripting())
				parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets theta", "[angle in radians]", commandName, "setTheta")) {
			pos.setValue(0);
			double w = MesquiteDouble.fromString(arguments, pos);
			if (!MesquiteDouble.isCombinable(w))
				w = MesquiteDouble.queryDouble(containerOfModule(), "Theta", "Set theta", theta);
			if (MesquiteDouble.isCombinable(w)) {
				theta = w;
				if (extras!=null) {
					for (int i=0; i<extras.size(); i++){
						NodeLocs3DPlotExtra extra = (NodeLocs3DPlotExtra)extras.elementAt(i);
						if (extra!=null && extra.legend !=null){
							extra.legend.horizSlider.setCurrentValue(theta);
						}
					}
				}
				if (!MesquiteThread.isScripting())
					parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets phi", "[angle in radians]", commandName, "setPhi")) {
			pos.setValue(0);
			double w = MesquiteDouble.fromString(arguments, pos);
			if (!MesquiteDouble.isCombinable(w))
				w = MesquiteDouble.queryDouble(containerOfModule(), "Phi", "Set phi", phi);
			if (MesquiteDouble.isCombinable(w)) {
				phi = w;
				if (extras!=null) {
					for (int i=0; i<extras.size(); i++){
						NodeLocs3DPlotExtra extra = (NodeLocs3DPlotExtra)extras.elementAt(i);
						if (extra!=null && extra.legend !=null){
							extra.legend.vertSlider.setCurrentValue(phi);
						}
					}
				}
				if (!MesquiteThread.isScripting())
					parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets D", "[distance (>0)]", commandName, "setD")) {
			pos.setValue(0);
			double w = MesquiteDouble.fromString(arguments, pos);
			if (!MesquiteDouble.isCombinable(w))
				w = MesquiteDouble.queryDouble(containerOfModule(), "D", "Set D", D);
			if (MesquiteDouble.isCombinable(w)) {
				D = w;
				if (extras!=null) {
					for (int i=0; i<extras.size(); i++){
						NodeLocs3DPlotExtra extra = (NodeLocs3DPlotExtra)extras.elementAt(i);
						if (extra!=null && extra.legend !=null){
							extra.legend.magSlider.setCurrentValue(D);
						}
					}
				}
				if (!MesquiteThread.isScripting())
					parametersChanged();
			}
		}

		else if (checker.compare(this.getClass(), "Sets the current item displayed on the x axis", "[number of item]", commandName, "setX")) {
			int ic = CharacterStates.toInternal(MesquiteInteger.fromString(arguments, new MesquiteInteger(0)));
			if (!MesquiteInteger.isCombinable(ic))
				ic = MesquiteInteger.queryInteger(containerOfModule(), "Choose item (X axis)", "Item to map", 1);
			if (MesquiteInteger.isCombinable(ic) && (ic>=numbersForNodesTask.getMin()) && (ic<=numbersForNodesTask.getMax())) {
				currentX = ic;
				numbersForNodesTask.setCurrent(ic);
				adjustScrolls();
				if (!MesquiteThread.isScripting())
					parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets the current item displayed on the y axis", "[number of item]", commandName, "setY")) {
			int ic = CharacterStates.toInternal(MesquiteInteger.fromString(arguments, new MesquiteInteger(0)));
			if (!MesquiteInteger.isCombinable(ic))
				ic = MesquiteInteger.queryInteger(containerOfModule(), "Choose item (Y axis)", "Item to map", 1);
			if (MesquiteInteger.isCombinable(ic) && (ic>=numbersForNodesTask.getMin()) && (ic<=numbersForNodesTask.getMax())) {
				currentY = ic;
				numbersForNodesTask.setCurrent(ic);
				adjustScrolls();
				if (!MesquiteThread.isScripting())
					parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets the current item displayed on the z axis", "[number of item]", commandName, "setZ")) {
			int ic = CharacterStates.toInternal(MesquiteInteger.fromString(arguments, new MesquiteInteger(0)));
			if (!MesquiteInteger.isCombinable(ic))
				ic = MesquiteInteger.queryInteger(containerOfModule(), "Choose item (Z axis)", "Item to map", 1);
			if (MesquiteInteger.isCombinable(ic) && (ic>=numbersForNodesTask.getMin()) && (ic<=numbersForNodesTask.getMax())) {
				currentZ = ic;
				numbersForNodesTask.setCurrent(ic);
				adjustScrolls();
				if (!MesquiteThread.isScripting())
					parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Goes to next item for X axis", null, commandName, "nextX")) {
			if (currentX<numbersForNodesTask.getMax()) {
				currentX++;
				adjustScrolls();
				numbersForNodesTask.setCurrent(currentX);
				if (!MesquiteThread.isScripting())
					parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Goes to next item for Y axis", null, commandName, "nextY")) {
			if (currentY<numbersForNodesTask.getMax()) {
				currentY++;
				adjustScrolls();
				numbersForNodesTask.setCurrent(currentY);
				if (!MesquiteThread.isScripting())
					parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Goes to next item for Z axis", null, commandName, "nextZ")) {
			if (currentZ<numbersForNodesTask.getMax()) {
				currentZ++;
				adjustScrolls();
				numbersForNodesTask.setCurrent(currentZ);
				if (!MesquiteThread.isScripting())
					parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Goes to previous item for X axis", null, commandName, "previousX")) {
			if (currentX>numbersForNodesTask.getMin()) {
				currentX--;
				adjustScrolls();
				numbersForNodesTask.setCurrent(currentX);
				if (!MesquiteThread.isScripting())
					parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Goes to previous item for Y axis", null, commandName, "previousY")) {
			if (currentY>numbersForNodesTask.getMin()) {
				currentY--;
				adjustScrolls();
				numbersForNodesTask.setCurrent(currentY);
				if (!MesquiteThread.isScripting())
					parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Goes to previous item for Z axis", null, commandName, "previousZ")) {
			if (currentZ>numbersForNodesTask.getMin()) {
				currentZ--;
				adjustScrolls();
				numbersForNodesTask.setCurrent(currentZ);
				if (!MesquiteThread.isScripting())
					parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets the initial horizontal offset from home position for the legend", "[offset in pixels]", commandName, "setInitialOffsetH")) {
			int offset= MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(offset)) {
				initialOffsetH = offset;
			}
		}
		else if (checker.compare(this.getClass(), "Sets the initial vertical offset from home position for the legend", "[offset in pixels]", commandName, "setInitialOffsetV")) {
			int offset= MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(offset)) {
				initialOffsetV = offset;
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to show the legend", "[on or off]", commandName, "toggleShowLegend")) {
			showLegend.toggleValue(parser.getFirstToken(arguments));
			if (extras!=null) {
				for (int i=0; i<extras.size(); i++){
					NodeLocs3DPlotExtra extra = (NodeLocs3DPlotExtra)extras.elementAt(i);
					if (extra!=null && extra.legend !=null){
						extra.legend.setVisible(showLegend.getValue());
					}
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to show the axes", "[on or off]", commandName, "toggleShowAxes")) {
			showAxes.toggleValue(parser.getFirstToken(arguments));
			if (extras!=null) {
				for (int i=0; i<extras.size(); i++){
					NodeLocs3DPlotExtra extra = (NodeLocs3DPlotExtra)extras.elementAt(i);
					if (extra!=null && extra.legend !=null){
						extra.treeDisplay.pleaseUpdate();
					}
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to show the axis planes", "[on or off]", commandName, "toggleShowAxisPlanes")) {
			showAxisPlanes.toggleValue(parser.getFirstToken(arguments));
			if (extras!=null) {
				for (int i=0; i<extras.size(); i++){
					NodeLocs3DPlotExtra extra = (NodeLocs3DPlotExtra)extras.elementAt(i);
					if (extra!=null && extra.legend !=null){
						extra.treeDisplay.pleaseUpdate();
					}
				}
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	private void adjustScrolls(){
		if (extras!=null) {
			for (int i=0; i<extras.size(); i++){
				NodeLocs3DPlotExtra extra = (NodeLocs3DPlotExtra)extras.elementAt(i);
				if (extra!=null){
					extra.adjustScrolls();
				}
			}
		}
	}
	public String getName() {
		return "Node Locations (3D plot)";
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Calculates the node locations for a tree plotted in a three dimensional space." ;
	}
	/*_________________________________________________*/
	public boolean compatibleWithOrientation(int orientation) {
		return false;
	}
	/*_________________________________________________*/
	boolean first=true;
	MesquiteNumber minX, maxX, minY, maxY, minZ, maxZ, sumX, sumY, sumZ, sumSqX, sumSqY, sumSqZ;
	int nX, nY, nZ;
	MesquiteNumber xNumber = new MesquiteNumber();
	MesquiteNumber yNumber = new MesquiteNumber();
	MesquiteNumber zNumber = new MesquiteNumber();

	private void surveyValues (Tree tree, int node, NumberArray numbersX, NumberArray numbersY, NumberArray numbersZ, MesquiteBoolean illegalValue){
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			surveyValues(tree, d, numbersX, numbersY, numbersZ, illegalValue);
		if (badLocs[node])
		return;
		numbersX.placeValue(node, xNumber);
		numbersY.placeValue(node, yNumber);
		numbersZ.placeValue(node, zNumber);
		boolean thisLocBad = false;
		if (xNumber.isCombinable()) {
			sumX.add(xNumber);
			nX++;
		}
		else thisLocBad = true;

		if (yNumber.isCombinable()) {
			sumY.add(yNumber);
			nY++;
		}
		else thisLocBad = true;

		if (zNumber.isCombinable()) {
			sumZ.add(zNumber);
			nZ++;
		}
		else thisLocBad = true;

		if (illegalValue != null){
			if (thisLocBad){
				illegalValue.setValue(true);
				badLocs[node] = true;
			}
			else {
				badLocs[node] = false;
			}
		}

		if (!thisLocBad){
			if (first) {
				maxX.setValue(xNumber);
				minX.setValue(xNumber);
				maxY.setValue(yNumber);
				minY.setValue(yNumber);
				maxZ.setValue(zNumber);
				minZ.setValue(zNumber);
				first = false;
			}
			else {
				maxX.setMeIfIAmLessThan(xNumber);
				minX.setMeIfIAmMoreThan(xNumber);
				maxY.setMeIfIAmLessThan(yNumber);
				minY.setMeIfIAmMoreThan(yNumber);
				maxZ.setMeIfIAmLessThan(zNumber);
				minZ.setMeIfIAmMoreThan(zNumber);
			}
		}
		/**/
	}
	private void surveyValues2 (Tree tree, int node, NumberArray numbersX, NumberArray numbersY, NumberArray numbersZ){
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			surveyValues2(tree, d, numbersX, numbersY, numbersZ);
		if (!badLocs[node]){
			numbersX.placeValue(node, xNumber);
			numbersY.placeValue(node, yNumber);
			numbersZ.placeValue(node, zNumber);
			if (xNumber.isCombinable()) {
				double x = xNumber.getDoubleValue();
				sumSqX.add((sumX.getDoubleValue()/nX-x)*(sumX.getDoubleValue()/nX-x));
			}
			if (yNumber.isCombinable()) {
				double y = yNumber.getDoubleValue();
				sumSqY.add((sumY.getDoubleValue()/nY-y)*(sumY.getDoubleValue()/nY-y));
			}
			if (zNumber.isCombinable()) {
				double z = zNumber.getDoubleValue();
				sumSqZ.add((sumZ.getDoubleValue()/nZ-z)*(sumZ.getDoubleValue()/nZ-z));
			}
		}

	}
	/*_________________________________________________*/
	private void fill3Dlocs (Tree tree, int node, MesquiteInteger n, double[][] XYZ, NumberArray numbersX, NumberArray numbersY, NumberArray numbersZ){
		if (!badLocs[node]){
			numbersX.placeValue(node, xNumber);
		XYZ[0][n.getValue()] = xNumber.getDoubleValue();
		numbersY.placeValue(node, xNumber);
		XYZ[1][n.getValue()] = xNumber.getDoubleValue();
		numbersZ.placeValue(node, xNumber);
		XYZ[2][n.getValue()] = xNumber.getDoubleValue();
		}
		n.increment();

		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			fill3Dlocs(tree, d, n, XYZ, numbersX, numbersY, numbersZ);
	}
	/*_________________________________________________*/
	private void fill2Dlocs (Tree tree, int node, MesquiteInteger n, double[][] XrYr, NumberArray numbersH, NumberArray numbersV, NumberArray numbersD){
		if (!badLocs[node]){
			numbersH.setValue(node, XrYr[0][n.getValue()]);
		numbersV.setValue(node, XrYr[1][n.getValue()]);
		numbersD.setValue(node, XrYr[2][n.getValue()]);
		}
		n.increment();
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			fill2Dlocs(tree, d, n, XrYr, numbersH, numbersV, numbersD);
	}

	/*_________________________________________________*/
	private void calcNodeLocs (Tree tree, int node, int pixels, NumberArray numbersX, NumberArray numbersY, NumberArray numbersD){
		if (location==null|| xNumber==null|| yNumber==null)
			return;
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			calcNodeLocs(tree, d, pixels, numbersX, numbersY, numbersD);
		if (badLocs[node]){
			location[node].x = MesquiteInteger.unassigned;
			location[node].y = MesquiteInteger.unassigned;
		}
		else {
			numbersX.placeValue(node, xNumber);
			numbersY.placeValue(node, yNumber);
			if (xNumber!=null)
				location[node].x = xNumber.setWithinBounds(minX, maxX, pixels) /*+ rect.x*/+ margin;
			if (yNumber!=null)
				location[node].y = yNumber.setWithinBounds(minY, maxY, pixels) /*+ rect.y*/ + margin;
			numbersD.placeValue(node, xNumber);
			z[node] = xNumber.getDoubleValue();
		}
	}

	/*.................................................................................................................*/
	/** Returns the purpose for which the employee was hired (e.g., "to reconstruct ancestral states" or "for X axis").*/
	public String purposeOfEmployee(MesquiteModule employee){
		if (employee == numbersForNodesTask)
			return "for axes";
		else
			return "";
	}
	// *-------------------standardardize ----------------------------------------;
	public void standardize(double[][] matrix, double xMean, double yMean, double zMean, double xSD, double ySD, double zSD){
		int numRows    = MatrixUtil.numFullRows(matrix);
		int numColumns = MatrixUtil.numFullColumns(matrix);
		if (numRows==0 ||  numColumns==0 )
			return; 

		for (int j=0; j<numRows; j++) {
			matrix[0][j] = (matrix[0][j]-xMean)/xSD;
			matrix[1][j] = (matrix[1][j]-yMean)/ySD;
			matrix[2][j] = (matrix[2][j]-zMean)/zSD;
		}

	}
	/*_________________________________________________*/
	public void calculateNodeLocs(TreeDisplay treeDisplay, Tree tree, int drawnRoot, Rectangle rect) { 
		if (hide ||  isDoomed())
			return;
		if (MesquiteTree.OK(tree)) {
			if (veryFirstTime) {
				veryFirstTime=false;
				numbersForNodesTask.initialize(tree);
			}
			NodeLocs3DPlotExtra extra = null;
			if (treeDisplay.getExtras() !=null) {
				if (treeDisplay.getExtras().myElements(this)==null) {  //todo: need to do one for each treeDisplay!
					extra = new NodeLocs3DPlotExtra(this, treeDisplay); 

					treeDisplay.addExtra(extra); 
					extras.addElement(extra);
				}
				else {
					Listable[] mine = treeDisplay.getExtras().myElements(this);
					if (mine !=null && mine.length>0)
						extra =(NodeLocs3DPlotExtra) mine[0];
				}
			}
			extra.setTree(tree);
			NumberArray numbersX= new NumberArray(tree.getNumNodeSpaces());
			NumberArray numbersY=new NumberArray(tree.getNumNodeSpaces());
			NumberArray numbersZ=new NumberArray(tree.getNumNodeSpaces());

			NumberArray numbersH = new NumberArray(tree.getNumNodeSpaces());
			NumberArray numbersV = new NumberArray(tree.getNumNodeSpaces());
			NumberArray numbersD = new NumberArray(tree.getNumNodeSpaces());

			numbersForNodesTask.setCurrent(currentX);
			numbersForNodesTask.calculateNumbers(tree, numbersX, null);
			xString = "#" + (currentX+1) + " from " + numbersForNodesTask.getNameAndParameters();

			numbersForNodesTask.setCurrent(currentY);
			numbersForNodesTask.calculateNumbers(tree, numbersY, null);
			yString = "#" + (currentY+1) + " from " + numbersForNodesTask.getNameAndParameters();

			numbersForNodesTask.setCurrent(currentZ);
			numbersForNodesTask.calculateNumbers(tree, numbersZ, null);
			zString = "#" + (currentZ+1) + " from " + numbersForNodesTask.getNameAndParameters();
			extra.textPositions.setLength(0);
			nodePositions(tree.getRoot(),  tree, extra.textPositions,  numbersX,  numbersY, numbersZ);

			if (extra!=null)
				extra.parameters = numbersForNodesTask.getParameters();
			first = true;
			int subRoot = tree.motherOfNode(drawnRoot);
			location = new Point[tree.getNumNodeSpaces()];
			z = new double[tree.getNumNodeSpaces()];
			badLocs = new boolean[tree.getNumNodeSpaces()];
			for (int i=0; i<location.length; i++) {
				location[i]= new Point();
				badLocs[i] = false;
			}
			minX = new MesquiteNumber();
			maxX = new MesquiteNumber();
			minY = new MesquiteNumber();
			maxY = new MesquiteNumber();
			minZ = new MesquiteNumber();
			maxZ = new MesquiteNumber();
			sumX = new MesquiteNumber(0);
			nX = 0;
			sumY = new MesquiteNumber(0);
			nY = 0;
			sumZ = new MesquiteNumber(0);
			nZ = 0;
			sumSqX = new MesquiteNumber(0);
			sumSqY = new MesquiteNumber(0);
			sumSqZ = new MesquiteNumber(0);
			MesquiteBoolean illegalValue = new MesquiteBoolean(false);

			surveyValues(tree, drawnRoot, numbersX, numbersY, numbersZ, illegalValue); // getting min and max values of X,Y,Z
			if (illegalValue.getValue()) {
				/*for (int i=0; i<tree.getNumNodeSpaces() && i<treeDisplay.getTreeDrawing().y.length; i++) {
						treeDisplay.getTreeDrawing().y[i] = 0;
						treeDisplay.getTreeDrawing().x[i] = 0;
						treeDisplay.getTreeDrawing().z[i] = 0;
					}*/
				if (extra!=null)
					extra.addWarning(true);
				//return;
			}
			else 	if (extra!=null)
				extra.addWarning(false);
			summarizeBad(0);
			surveyValues2(tree, drawnRoot, numbersX, numbersY, numbersZ); // getting min and max values of X,Y,Z
			numNodes = tree.numberOfNodesInClade(drawnRoot);
			double[][] XYZ = new double[3][numNodes +4 + 6]; //array to hold positions of nodes; extra for center, x, y, z AND rotator points
			MesquiteInteger n = new MesquiteInteger(0);
			fill3Dlocs(tree, drawnRoot, n, XYZ, numbersX, numbersY, numbersZ); //filling array with node positions
			summarizeBad(1);
			double xCenter;
			if (nX>0) {
				xCenter =(sumX.getDoubleValue())/nX; 
			}
			else
				xCenter =(maxX.getDoubleValue()+minX.getDoubleValue())/2; 
			double yCenter;
			if (nY>0)
				yCenter =(sumY.getDoubleValue())/nY; 
			else
				yCenter =(maxY.getDoubleValue()+minY.getDoubleValue())/2;
			double zCenter;
			if (nZ>0)
				zCenter =(sumZ.getDoubleValue())/nZ; 
			else
				zCenter =(maxZ.getDoubleValue()+minZ.getDoubleValue())/2;
			double xSpan, ySpan, zSpan;
			xSpan = Math.sqrt(sumSqX.getDoubleValue()/(nX-1));
			ySpan = Math.sqrt(sumSqY.getDoubleValue()/(nY-1));
			zSpan = Math.sqrt(sumSqZ.getDoubleValue()/(nZ-1));
			XYZ[0][numNodes] = xCenter; //center
			XYZ[1][numNodes] = yCenter; //center
			XYZ[2][numNodes] = zCenter; //center

			XYZ[0][numNodes+1] = xSpan; //axis
			XYZ[1][numNodes+1] = yCenter; //axis
			XYZ[2][numNodes+1] = zCenter; //axis

			XYZ[0][numNodes+2] =xCenter; //axis
			XYZ[1][numNodes+2] = ySpan; //axis
			XYZ[2][numNodes+2] = zCenter; //axis

			XYZ[0][numNodes+3] = xCenter; //axis
			XYZ[1][numNodes+3] = yCenter; //axis
			XYZ[2][numNodes+3] = zSpan; //axis

			XYZ[0][numNodes+4] = xSpan; //rotator top
			XYZ[1][numNodes+4] = yCenter; //rotator was 0
			XYZ[2][numNodes+4] =zCenter; //rotator

			XYZ[0][numNodes+5] =-xSpan; //rotator bottom
			XYZ[1][numNodes+5] = yCenter; //rotator
			XYZ[2][numNodes+5] = zCenter; //rotator

			double sr2 = 1.0/Math.sqrt(2.0);
			XYZ[0][numNodes+6] = xCenter; //rotator corner
			XYZ[1][numNodes+6] = ySpan*sr2; //rotator
			XYZ[2][numNodes+6] = zSpan*sr2; //rotator

			XYZ[0][numNodes+7] = xCenter; //rotator corner
			XYZ[1][numNodes+7] = -ySpan*sr2; //rotator
			XYZ[2][numNodes+7] = zSpan*sr2; //rotator

			XYZ[0][numNodes+8] = xCenter; //rotator corner
			XYZ[1][numNodes+8] = ySpan*sr2; //rotator
			XYZ[2][numNodes+8] = -zSpan*sr2; //rotator

			XYZ[0][numNodes+9] = xCenter; //rotator corner
			XYZ[1][numNodes+9] = -ySpan*sr2; //rotator
			XYZ[2][numNodes+9] = -zSpan*sr2; //rotator

			standardize(XYZ, xCenter, xCenter, xCenter, xSpan, ySpan, zSpan);
			summarizeBad(2);

			double rangeX = Double2DArray.maximumInColumn(XYZ, 0)-Double2DArray.minimumInColumn(XYZ, 0);
			double rangeY = Double2DArray.maximumInColumn(XYZ, 1)-Double2DArray.minimumInColumn(XYZ, 1);
			double rangeZ = Double2DArray.maximumInColumn(XYZ, 2)-Double2DArray.minimumInColumn(XYZ, 2);
			double longAxis = Math.sqrt(rangeX*rangeX + rangeY*rangeY + rangeZ*rangeZ);
			double rho = 1.1*longAxis;

			double useD;
			if (D == MesquiteDouble.unassigned)
				useD = rho;
			else
				useD = D;

			XrYr  = MatrixUtil.transform3Dto2D(XYZ, theta, phi, rho, useD);  //transform to 2D
			if (extra!=null && extra.legend!=null)
				extra.legend.setRotatorPoints(XrYr, numNodes, numNodes+4);

			n.setValue(0);
			fill2Dlocs(tree, drawnRoot, n, XrYr, numbersH, numbersV, numbersD); //taking 2D matrix and setting NumberArrays to store them
			first = true;
			summarizeBad(3);
			surveyValues(tree, drawnRoot, numbersH, numbersV, numbersZ, null); //finding new (transformed) maxX and maxY
			rangeX = maxX.getDoubleValue()-minX.getDoubleValue();
			rangeY = maxY.getDoubleValue()-minY.getDoubleValue();
			rangeZ = maxZ.getDoubleValue()-minZ.getDoubleValue();
			double corrScaling = (1.1*Math.sqrt(rangeX*rangeX + rangeY*rangeY + rangeZ*rangeZ))/rho; 
			double midX = XrYr[0][numNodes];

			minX.setValue(midX - (longAxis/2));
			maxX.setValue(midX + (longAxis/2));
			double midY = XrYr[1][numNodes];
			minY.setValue(midY - (longAxis/2));
			maxY.setValue(midY + (longAxis/2));
			pixels = MesquiteInteger.minimum(rect.width, rect.height) - 2*margin;
			summarizeBad(4);
			calcNodeLocs(tree, drawnRoot, pixels, numbersH, numbersV, numbersD); //calculate pixel node locations from NumberArrays
			summarizeBad(5);
			location[subRoot].x = location[drawnRoot].x;
			location[subRoot].y = location[drawnRoot].y;
			for (int i=0; i<tree.getNumNodeSpaces() && i<treeDisplay.getTreeDrawing().y.length; i++) {
				treeDisplay.getTreeDrawing().y[i] = location[i].y;
				treeDisplay.getTreeDrawing().x[i] = location[i].x;
				treeDisplay.getTreeDrawing().z[i] = z[i];
			}

		}
	}
	private void summarizeBad(int p){
		int bad = 0;
		for (int i=0; i<badLocs.length; i++)
			if (badLocs[i])
				bad++;
	}
	/*.................................................................................................................*/
	double[][] XrYr;
	int numNodes;
	int pixels;
	/*.................................................................................................................*/
	public void nodePositions(int N,  Tree tree, StringBuffer sb, NumberArray numbersX, NumberArray numbersY, NumberArray numbersZ) {
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			nodePositions(d, tree, sb, numbersX, numbersY, numbersZ);
		if (tree.nodeIsTerminal(N))
			sb.append(tree.getTaxa().getName(tree.taxonNumberOfNode(N)) + '\t');
		else
			sb.append("node " + N +  '\t');
		sb.append(numbersX.toString(N) + '\t' + numbersY.toString(N) + '\t' + numbersZ.toString(N) + "\n");
	}

	private void drawString(Graphics g, String s, int x, int y){
		if (g == null || StringUtil.blank(s))
			return;
		try {
			g.drawString(s, x, y);
		}
		catch (Exception e){
		}
	}
	void drawAxes(Graphics g, NodeLocs3DPlotExtra extra){
		if (XrYr == null)
			return;
		if (showAxes.getValue())
			drawGrid(g, pixels, XrYr[0][numNodes], XrYr[1][numNodes], XrYr[0][numNodes+1], XrYr[1][numNodes+1], XrYr[0][numNodes+2], XrYr[1][numNodes+2], XrYr[0][numNodes+3], XrYr[1][numNodes+3]);
	}
	public void drawGrid (Graphics g, int pixels, double centerXd, double centerYd, double xTipXd, double xTipYd, double yTipXd, double yTipYd, double zTipXd, double zTipYd) {
		if (hide)
			return;
		//double factor = 1;
		//xTipXd *= factor; xTipYd *= factor; yTipXd *= factor; yTipYd *= factor; zTipXd *= factor; zTipYd *= factor;
		xNumber.setValue(xTipXd);
		int xTipX = xNumber.setWithinBounds(minX, maxX, pixels) /*+ rect.x*/+ margin;
		xNumber.setValue(xTipYd);
		int xTipY = xNumber.setWithinBounds(minY, maxY, pixels) /*+ rect.x*/+ margin;
		xNumber.setValue(yTipXd);
		int yTipX = xNumber.setWithinBounds(minX, maxX, pixels) /*+ rect.x*/+ margin;
		xNumber.setValue(yTipYd);
		int yTipY = xNumber.setWithinBounds(minY, maxY, pixels) /*+ rect.x*/+ margin;
		xNumber.setValue(zTipXd);
		int zTipX = xNumber.setWithinBounds(minX, maxX, pixels) /*+ rect.x*/+ margin;
		xNumber.setValue(zTipYd);
		int zTipY = xNumber.setWithinBounds(minY, maxY, pixels) /*+ rect.x*/+ margin;
		xNumber.setValue(centerXd);
		int centerX = xNumber.setWithinBounds(minX, maxX, pixels) /*+ rect.x*/+ margin;
		xNumber.setValue(centerYd);
		int centerY = xNumber.setWithinBounds(minY, maxY, pixels) /*+ rect.x*/+ margin;
		Color c=g.getColor();

		if (showAxisPlanes.getValue()) {
			//xy rectangle
			Composite comp = ((Graphics2D)g).getComposite();
			ColorDistribution.setTransparentGraphics(g,0.5f);		
			g.setColor(Color.orange);
			Polygon xyPoly = new Polygon();
			xyPoly.addPoint(centerX, centerY);
			xyPoly.addPoint(xTipX, xTipY);
			//xyPoly.addPoint(xTipX, yTipY);
			xyPoly.addPoint(yTipX, yTipY);
			xyPoly.addPoint(centerX, centerY);
			g.fillPolygon(xyPoly);
			Polygon xzPoly = new Polygon();
			xzPoly.addPoint(centerX, centerY);
			xzPoly.addPoint(xTipX, xTipY);
			//xyPoly.addPoint(xTipX, yTipY);
			xzPoly.addPoint(zTipX, zTipY);
			xzPoly.addPoint(centerX, centerY);
			g.fillPolygon(xzPoly);
			Polygon zyPoly = new Polygon();
			zyPoly.addPoint(centerX, centerY);
			zyPoly.addPoint(zTipX, zTipY);
			//xyPoly.addPoint(xTipX, yTipY);
			zyPoly.addPoint(yTipX, yTipY);
			zyPoly.addPoint(centerX, centerY);
			g.fillPolygon(zyPoly);
			((Graphics2D)g).setComposite(comp);
		}

		g.setColor(Color.blue);
		g.drawLine(centerX, centerY, xTipX, xTipY);
		g.drawLine(centerX+1, centerY, xTipX+1, xTipY);
		g.drawLine(centerX, centerY+1, xTipX, xTipY+1);
		g.drawLine(centerX+1, centerY+1, xTipX+1, xTipY+1);
		drawString(g, "X", xTipX+20, xTipY +20);


		g.drawLine(centerX, centerY, yTipX, yTipY);
		g.drawLine(centerX+1, centerY, yTipX+1, yTipY);
		g.drawLine(centerX, centerY+1, yTipX, yTipY+1);
		g.drawLine(centerX+1, centerY+1, yTipX+1, yTipY+1);
		drawString(g, "Y", yTipX+20, yTipY +20);


		g.drawLine(centerX, centerY, zTipX, zTipY);
		g.drawLine(centerX+1, centerY, zTipX+1, zTipY);
		g.drawLine(centerX, centerY+1, zTipX, zTipY+1);
		g.drawLine(centerX+1, centerY+1, zTipX+1, zTipY+1);
		drawString(g, "Z", zTipX+20, zTipY +20);


		if (c!=null) g.setColor(c);
	}

}



class NodeLocs3DPlotExtra extends TreeDisplayBkgdExtra {
	public NodeLocs3DPlotLegend legend;
	NodeLocs3DPlot locsModule;
	public String parameters = "";
	boolean doWarn = false;
	StringBuffer textPositions;

	public NodeLocs3DPlotExtra (NodeLocs3DPlot ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		locsModule = ownerModule;
		textPositions = new StringBuffer();
	}
	/*.................................................................................................................*/
	public   String infoAtNodes(Tree tree, int drawnRoot) {
		return parameters + "\n\nNodes with X, Y, Z positions\n\n" + textPositions.toString();
	}
	/*.................................................................................................................*/
	public   String additionalText(Tree tree, int drawnRoot) {
		return parameters;
	}

	public void addWarning(boolean warn){
		if (legend!=null)
			legend.addWarning(warn);
		doWarn = warn;
	}
	/*.................................................................................................................*/
	boolean legendMade = false;
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		locsModule.drawAxes(g, this);
		if (legend!=null){
			legend.adjustLocation();
			legend.repaint();
		}
	}
	public void adjustScrolls(){
		if (legend!=null){
			legend.resetScrolls((int)locsModule.numbersForNodesTask.getMin()+1, (int)locsModule.numbersForNodesTask.getMax()+1);
			legend.resetScrollCurrent(CharacterStates.toExternal(locsModule.currentX), CharacterStates.toExternal(locsModule.currentY), CharacterStates.toExternal(locsModule.currentZ));
		}
	}
	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}

	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		if (!legendMade && legend == null) {
			legendMade= true;
			legend = new NodeLocs3DPlotLegend(locsModule, this);
			legend.setVisible(locsModule.showLegend.getValue());
			legend.resetScrolls((int)locsModule.numbersForNodesTask.getMin()+1, (int)locsModule.numbersForNodesTask.getMax()+1);
			legend.resetScrollCurrent(CharacterStates.toExternal(locsModule.currentX), CharacterStates.toExternal(locsModule.currentY), CharacterStates.toExternal(locsModule.currentZ));
			legend.addWarning(doWarn);
			addPanelPlease(legend);
		}
	}

	public void turnOff() {
		if (treeDisplay!=null && legend!=null)
			removePanelPlease(legend);
		super.turnOff();
	}
}

/* ======================================================================== */
class NodeLocs3DPlotLegend extends TreeDisplayLegend {
	private NodeLocs3DPlot ownerModule;
	private NodeLocs3DPlotExtra pD;
	boolean doWarn = false;
	int edgeTop = 8;
	int edgeLeft = 24;
	int scrollWidth = 62;
	int scrollHeight = 20;
	private int oldX=-1;
	private int oldMaxX=0;
	private int oldY=-1;
	private int oldMaxY=0;
	MesquiteCommand setVertical;
	MesquiteCommand setHorizontal;
	MesquiteCommand setMagnification;
	int[] polyOrder;
	double[][] points;
	int[][] triangles;
	double[] centerPoint;
	Polygon[] polys;
	TextArea text;
	MiniSlider vertSlider, horizSlider, magSlider;
	MiniScroll xScroll, yScroll, zScroll;
	public NodeLocs3DPlotLegend(NodeLocs3DPlot ownerModule, NodeLocs3DPlotExtra pD) {
		super(pD.treeDisplay, 240, 170);
		this.setVertical = ownerModule.setPhiCommand;
		this.setHorizontal =ownerModule.setThetaCommand;
		this.setMagnification =ownerModule.setDCommand;
		this.pD = pD;
		text = new TextArea("", 3, 3, TextArea.SCROLLBARS_VERTICAL_ONLY);
		add(text);
		text.setLocation(30, 4);
		text.setSize(legendWidth-30-4, legendHeight -80);
		points = new double[3][7];
		centerPoint = new double[3];
		polyOrder = new int[8];
		polys = new Polygon[8];
		for (int i=0; i<8; i++) {
			polys[i] = new Polygon();
			polys[i].xpoints = new int[3];
			polys[i].ypoints = new int[3];
		}
		triangles = new int[3][8];
		centerPoint[0] = legendWidth/2;
		centerPoint[1] = legendHeight/2;
		//setBackground(Color.cyan);
		centerPoint[2] = 0;
		this.ownerModule = ownerModule;
		setLayout(null);
		setOffsetX(ownerModule.initialOffsetH);
		setOffsetY(ownerModule.initialOffsetV);
		magSlider = new MiniSlider(setMagnification, true, ownerModule.D, 0.0, 100.0, 0.0, 100.0);
		vertSlider = new MiniSlider(setVertical, false, ownerModule.phi, 0.0, Math.PI*2, 0.0, Math.PI*2);
		horizSlider = new MiniSlider(setHorizontal, true, ownerModule.theta, 0.0, Math.PI*2, 0.0, Math.PI*2);
		add(vertSlider);
		vertSlider.setLocation(4, edgeTop);
		vertSlider.setRangeInPixels(legendHeight-edgeTop-edgeTop-edgeTop);
		vertSlider.setColor(Color.green);
		add(horizSlider);
		horizSlider.setLocation(edgeLeft, legendHeight-55);
		horizSlider.setRangeInPixels(legendWidth-edgeLeft-edgeLeft - scrollWidth);
		horizSlider.setColor(Color.green);
		add(magSlider);
		magSlider.setLocation(edgeLeft, legendHeight-20);
		magSlider.setRangeInPixels(legendWidth-edgeLeft-edgeLeft - scrollWidth);
		magSlider.setVisible(true);
		magSlider.setColor(Color.red);
		vertSlider.setVisible(true);
		horizSlider.setVisible(true);
		setSize(legendWidth, legendHeight);
		xScroll = new MiniScroll(MesquiteModule.makeCommand("setX",  ownerModule), false, 1, 1, 1,"");
		add(xScroll);
		xScroll.setLocation(legendWidth-edgeLeft - scrollWidth, legendHeight-(scrollHeight+5)*3);
		xScroll.setColor(Color.blue);
		xScroll.setVisible(true);
		yScroll = new MiniScroll(MesquiteModule.makeCommand("setY",  ownerModule), false, 1, 1, 1,"");
		add(yScroll);
		yScroll.setLocation(legendWidth-edgeLeft - scrollWidth, legendHeight-(scrollHeight+5)*2);
		yScroll.setColor(Color.blue);
		yScroll.setVisible(true);
		zScroll = new MiniScroll(MesquiteModule.makeCommand("setZ",  ownerModule), false, 1, 1, 1,"");
		add(zScroll);
		zScroll.setLocation(legendWidth-edgeLeft - scrollWidth, legendHeight-scrollHeight-5);
		zScroll.setColor(Color.blue);
		zScroll.setVisible(true);
	}
	void resetScrolls(int min, int max){
		resetScroll(xScroll, min, max);
		resetScroll(yScroll, min, max);
		resetScroll(zScroll, min, max);
	}
	void resetScrollCurrent(int x, int y, int z){
		xScroll.setCurrentValue(x);
		yScroll.setCurrentValue(y);
		zScroll.setCurrentValue(z);
	}
	void resetScroll(MiniScroll scroll, int min, int max){
		if (scroll == null)
			return;
		scroll.setMinimumValue(min);
		scroll.setMaximumValue(max);
		if (scroll.getCurrentValue()<min)
			scroll.setCurrentValue(min);
		else if (scroll.getCurrentValue()>max)
			scroll.setCurrentValue(max);
	}
	public void addWarning(boolean warn){
		doWarn = warn;
	}
	public void adjustRotation() {
	}
	double distance(int p0, int p1){
		double vertSpanX = points[0][p0]-points[0][p1];
		double vertSpanY = points[1][p0]-points[1][p1];
		double vertSpanZ = points[2][p0]-points[2][p1];
		return Math.sqrt(vertSpanX*vertSpanX + vertSpanY*vertSpanY + vertSpanZ*vertSpanZ);
	}
	void makePoly(int whichPoly, int p0, int p1, int p2){
		triangles[0][whichPoly]=p0;
		triangles[1][whichPoly]=p1;
		triangles[2][whichPoly]=p2;
		polys[whichPoly].npoints=0;
		polys[whichPoly].addPoint((int)(points[0][p0]+0.5), (int)(points[1][p0]+0.5));
		polys[whichPoly].addPoint((int)(points[0][p1]+0.5), (int)(points[1][p1]+0.5));
		polys[whichPoly].addPoint((int)(points[0][p2]+0.5), (int)(points[1][p2]+0.5));
		polys[whichPoly].npoints=3;
	}
	boolean isCorner(int poly, int corner){
		for (int i=0; i<3; i++)
			if (triangles[i][poly]== corner)
				return true;
		return false;		
	}
	boolean shareEdge(int poly1, int poly2){
		int count = 0;
		for (int i=0; i<3; i++)
			if (isCorner(poly2, triangles[i][poly1]))
				count++;
		return count==2;		
	}
	boolean sharePoint(int poly1, int poly2){
		for (int i=0; i<3; i++)
			if (isCorner(poly2, triangles[i][poly1]))
				return true;
		return false;		
	}
	int cornerTouching(int poly1, int poly2){ //returns the first corner touching found
		for (int i=0; i<3; i++)
			if (isCorner(poly2, triangles[i][poly1]))
				return triangles[i][poly1];
		return -1;		
	}
	int cornerNotTouching(int poly1, int poly2){ //returns the first corner touching found
		for (int i=0; i<3; i++)
			if (!isCorner(poly2, triangles[i][poly1]))
				return triangles[i][poly1];
		return -1;		
	}
	boolean inFront(int poly1, int poly2){
		if (shareEdge(poly1, poly2)) {
			//1 is in front if third point (which they are *not* sharing) of 1 is in front of that of 2
			int poly1Off = cornerNotTouching(poly1, poly2);
			int poly2Off = cornerNotTouching(poly2, poly1);
			boolean q= points[2][poly1Off]<points[2][poly2Off];
			return q;
		}
		else if (sharePoint(poly1, poly2)) {
			//1 is in front if closest point belonging to 1 of the two they don't share is behind that of 2;
			int polyTouch = cornerTouching(poly1, poly2);
			double closeness1 = MesquiteDouble.unassigned;
			for (int i=0; i<3; i++)
				if (triangles[i][poly1]!=polyTouch)
					if (MesquiteDouble.lessThan(points[2][triangles[i][poly1]], closeness1, 0)) {
						closeness1 = points[2][triangles[i][poly1]];
					}
			double closeness2 = MesquiteDouble.unassigned;
			for (int i=0; i<3; i++)
				if (triangles[i][poly2]!=polyTouch)
					if (MesquiteDouble.lessThan(points[2][triangles[i][poly2]], closeness2, 0)) {
						closeness2 = points[2][triangles[i][poly2]];
					}
			boolean q =closeness1<closeness2;
			return q;
		}
		else {
			//test which has closest point
			double closeness1 = MesquiteDouble.unassigned;
			for (int i=0; i<3; i++)
				if (MesquiteDouble.lessThan(points[2][triangles[i][poly1]], closeness1, 0)) {
					closeness1 = points[2][triangles[i][poly1]];
				}
			double closeness2 = MesquiteDouble.unassigned;
			for (int i=0; i<3; i++)
				if (MesquiteDouble.lessThan(points[2][triangles[i][poly2]], closeness2, 0)) {
					closeness2 = points[2][triangles[i][poly2]];
				}
			boolean q =closeness1<closeness2;
			return q;
		}
	}
	/*--------------------------------*/
	void sortPolys(){
		for (int i =0; i<8; i++)
			polyOrder[i] = i;
		for (int i=1; i<8; i++) {
			for (int j= i-1; j>=0  &&  inFront(polyOrder[j],polyOrder[j+1]); j--) {
				int temp = polyOrder[j];
				polyOrder[j] = polyOrder[j+1];
				polyOrder[j+1]=temp;
			}
		}
	}
	/*--------------------------------*/
	void setRotatorPoints(double[][] XrYr, int centerIndex, int firstIndex){
		try {
			for (int i=0; i<3; i++){
			points[i][0] = XrYr[i][centerIndex];
			for (int j =1; j<7; j++)
				points[i][j] = XrYr[i][firstIndex +j-1];
		}

		double span = MesquiteDouble.maximum(MesquiteDouble.maximum(distance(1,2), distance(3,6)), distance(5 ,4));
		double scaling = 1.0;
		if (span!=0)
			scaling = MesquiteDouble.minimum(legendHeight,legendWidth)*0.8/span;
		for (int i=0; i<3; i++){
			for (int j =0; j<7; j++)
				points[i][j] = (points[i][j] - XrYr[i][centerIndex])*scaling + centerPoint[i];
		}
		makePoly(0, 1, 3, 4);
		makePoly(1, 1, 4, 6);
		makePoly(2, 1, 6, 5);
		makePoly(3, 1, 5, 3);
		makePoly(4, 2, 3, 4);
		makePoly(5, 2, 4, 6);
		makePoly(6, 2, 6, 5);
		makePoly(7, 2, 5, 3);
		sortPolys();
		repaint();
		}
		catch(Exception e){
			MesquiteMessage.warnUser("Oops, setRotatorPoints in NodeLocs3DPlot with exception " + e);
		}

	}	
	void doPoly(Graphics g,  int index){
		if (index<4)
			g.setColor(Color.lightGray);
		else
			g.setColor(Color.darkGray);
		g.fillPolygon(polys[index]);
		g.setColor(Color.black);
		g.drawPolygon(polys[index]);
	}
	void fillHandle(Graphics g, double[][] p, int index, Color c, boolean onTop){
		if (points[2][index]>=points[2][0] && onTop)
			return;
		double s = 4;
		if (c!=null) g.setColor(c);
		g.fillOval((int)(points[0][index] - s+0.5), (int)(points[1][index] - s+0.5), (int)(s*2), (int)(s*2));
		g.setColor(Color.black);
		g.drawOval((int)(points[0][index] - s+0.5), (int)(points[1][index] - s+0.5), (int)(s*2), (int)(s*2));  
	}
	public void paint(Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		Color c = g.getColor();
		g.setColor(Color.blue);
		g.drawRect(0,0, legendWidth-1, legendHeight-1);
		if (doWarn)
			text.setText("3D plot MISSING VALUES\nX:  " + ownerModule.xString +"\nY:  " + ownerModule.yString + "\nZ:  " + ownerModule.zString);
		else	
			text.setText("3D plot\nX:  " + ownerModule.xString +"\nY:  " + ownerModule.yString + "\nZ:  " + ownerModule.zString);

		g.setColor(Color.green);
		g.drawString("Rotation", 30, legendHeight-57);
		g.setColor(Color.red);
		g.drawString("Magnification", 20, legendHeight-22);
		if (c!=null) g.setColor(c);
		super.paint(g);
		MesquiteWindow.uncheckDoomed(this);
	}


}




