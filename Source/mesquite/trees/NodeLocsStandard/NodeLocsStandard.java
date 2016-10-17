/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.NodeLocsStandard;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;

/** Calculates node locations for tree drawing in a standard vertical/horizontal position, as used by DiagonalDrawTree and SquareTree (for example).*/
public class NodeLocsStandard extends NodeLocsVH {

	int lastOrientation = 0;
	Vector extras;
	double fixedDepth = 1;
	boolean leaveScaleAlone = true;
	boolean fixedScale = false;
	MesquiteBoolean inhibitStretch;
	MesquiteBoolean showScale;
	MesquiteBoolean broadScale;
	MesquiteBoolean showBranchLengths;
	boolean resetShowBranchLengths = false;
	int fixedTaxonDistance = 0;

	static final int totalHeight = 0;
	static final int stretchfactor = 1;
	static final int  scaling = 2;
	
	String scaleTitle;
	int scaleBorderWidth;
	String scaleBorderLineStyle;
	String scaleBorderColor;
	String scaleFont;
	int scaleFontSize;
	String scaleFontFace;
	Color scaleColor = Color.cyan;
	Color scaleCounterColor = Color.blue;
	
	

//	double namesAngle = MesquiteDouble.unassigned;

	int ROOTSIZE = 20;
	MesquiteMenuItemSpec fixedScalingMenuItem, showScaleMenuItem, broadScaleMenuItem;
	MesquiteMenuItemSpec offFixedScalingMenuItem, stretchMenuItem, evenMenuItem;
	/**MesquiteMenuItemSpec scaleColorMenuItem, scaleBorderWidthMenuItem, scaleBorderColorMenuItem, scaleBorderLineStyleMenuItem;
	MesquiteMenuItemSpec scaleTitleMenuItem,scaleFontMenuItem,scaleFontFaceMenuItem; */

	NameReference triangleNameRef;
	MesquiteBoolean center;
	boolean[] fixedSettings = null;
	MesquiteBoolean even;
	/*.................................................................................................................*/

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		if (condition instanceof boolean[]){
			fixedSettings = (boolean[])condition;
		}
		extras = new Vector();
		inhibitStretch = new MesquiteBoolean(false);
		center = new MesquiteBoolean(false);
		even = new MesquiteBoolean(false);
		if (getEmployer()!=null && ("Square Tree".equalsIgnoreCase(getEmployer().getName()) || "Square Line Tree".equalsIgnoreCase(getEmployer().getName()))){ //a bit non-standard but a helpful service to use different defaults for square
			even.setValue(true);
			center.setValue(true);
		}
		triangleNameRef = NameReference.getNameReference("triangled");
		showBranchLengths = new MesquiteBoolean(false);
		showScale = new MesquiteBoolean(true);
		broadScale = new MesquiteBoolean(false);

		if (fixedSettings != null && fixedSettings.length>0 && fixedSettings[0]){
			showBranchLengths.setValue(true);
		}
		else
			addCheckMenuItem(null, "Branches Proportional to Lengths", makeCommand("branchLengthsToggle", this), showBranchLengths);

		if (showBranchLengths.getValue()) {
			fixedScalingMenuItem = addMenuItem( "Fixed Scaling...", makeCommand("setFixedScaling", this));
			showScaleMenuItem = addCheckMenuItem(null, "Show scale", makeCommand("toggleScale", this), showScale);
			broadScaleMenuItem = addCheckMenuItem(null, "Broad scale", makeCommand("toggleBroadScale", this), broadScale);
			resetShowBranchLengths=true;
		}
		else {
			stretchMenuItem = addCheckMenuItem(null, "Inhibit Stretch Tree to Fit", makeCommand("inhibitStretchToggle", this), inhibitStretch);
			evenMenuItem = addCheckMenuItem(null, "Even root to tip spacing", makeCommand("toggleEven", this), even);
		}
		addMenuItem( "Fixed Distance Between Taxa...", makeCommand("setFixedTaxonDistance",  this));
		addCheckMenuItem(null, "Centered Branches", makeCommand("toggleCenter", this), center);
		if (employerAllowsReorientation())
			addMenuItem("Set Current Orientation as Default", makeCommand("setDefaultOrientation",  this));

		//	addMenuItem("Taxon Name Angle...", makeCommand("namesAngle", this));
		return true;
	}
	/*.................................................................................................................*/
	private boolean employerAllowsReorientation(){
		if (getEmployer()== null || !(getEmployer() instanceof DrawTree))
			return true;
		DrawTree dt = (DrawTree)getEmployer();
		return dt.allowsReorientation();

	}
	public void endJob(){
		storePreferences();
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
		//treeDrawing = null;
		//tree=null;
		//treeDisplay=null;
		if (showBranchLengths != null)
			showBranchLengths.releaseMenuItem();
		broadScale.releaseMenuItem();
		showScale.releaseMenuItem();
		inhibitStretch.releaseMenuItem();
		center.releaseMenuItem();
		even.releaseMenuItem();
		super.endJob();
	}
	/*.................................................................................................................*/
	public void processPreferencesFromFile (String[] prefs) {
		if (prefs!=null && prefs.length>0) {
			defaultOrientation = MesquiteInteger.fromString(prefs[0]);
		}
	}

	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		StringUtil.appendXMLTag(buffer, 2, "defaultOrientation", defaultOrientation);   
		return buffer.toString();
	}

	public void processSingleXMLPreference (String tag, String content) {
		if ("defaultOrientation".equalsIgnoreCase(tag))
			defaultOrientation = MesquiteInteger.fromString(content);
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		if (stretchWasSet)
			temp.addLine("inhibitStretchToggle " + inhibitStretch.toOffOnString());
		temp.addLine("branchLengthsToggle " + showBranchLengths.toOffOnString());
		temp.addLine("toggleScale " + showScale.toOffOnString());
		temp.addLine("toggleBroadScale " + broadScale.toOffOnString());
		temp.addLine("toggleCenter " + center.toOffOnString());
		temp.addLine("toggleEven " + even.toOffOnString());
		temp.addLine("setFixedTaxonDistance " + fixedTaxonDistance); 

		if (fixedScale)
			temp.addLine("setFixedScaling " + MesquiteDouble.toString(fixedDepth) );
		return temp;
	}

	boolean stretchWasSet = false;
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "(For old scripts) Sets whether or not to stretch the tree to fit the drawing area", "[on = stretch; off]", commandName, "stretchToggle")) {
			inhibitStretch.toggleValue(parser.getFirstToken(arguments));
			inhibitStretch.toggleValue();  //since old sense is reverse
			stretchWasSet = true;
			parametersChanged();
		}
		else 	if (checker.compare(this.getClass(), "Sets a fixed distance between taxa for drawing the tree", "[distance in pixels]", commandName, "setFixedTaxonDistance")) {

			int newDistance= MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newDistance))
				newDistance = MesquiteInteger.queryInteger(containerOfModule(), "Set taxon distance", "Distance between taxa:", "(Use a value of 0 to tell Mesquite to calculate the distance itself.)", "", fixedTaxonDistance, 0, 99, true);
			if (newDistance>=0 && newDistance<100 && newDistance!=fixedTaxonDistance) {
				fixedTaxonDistance=newDistance;
/*				Enumeration e = drawings.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					SquareLineTreeDrawing treeDrawing = (SquareLineTreeDrawing)obj;
					treeDrawing.treeDisplay.setFixedTaxonSpacing(newDistance);
				}
				*/
				
				if ( !MesquiteThread.isScripting()) parametersChanged(new Notification(TREE_DRAWING_SIZING_CHANGED));
			}

		}
	else if (checker.compare(this.getClass(), "Sets whether or not to inhibit automatic stretching the tree to fit the drawing area", "[on =inihibit stretch; off]", commandName, "inhibitStretchToggle")) {
			inhibitStretch.toggleValue(parser.getFirstToken(arguments));
			stretchWasSet = true;
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Here to avoid scripting error; user will need to reset taxon names", null, commandName, "namesAngle")) {

		}
		else if (checker.compare(this.getClass(), "Sets whether or not to center the nodes between the immediate descendents, or the terminal in the clade", "[on = center over immediate; off]", commandName, "toggleCenter")) {
			center.toggleValue(parser.getFirstToken(arguments));
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to space the nodes evenly from root to tips", "[on = space evenly; off]", commandName, "toggleEven")) {
			even.toggleValue(parser.getFirstToken(arguments));
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets the current orientation to be the default", null, commandName, "setDefaultOrientation")) {
			defaultOrientation = lastOrientation;
			storePreferences();
		}
		else if (checker.compare(this.getClass(), "[no longer available; here to prevent warning given as old scripts are read]", "[]", commandName, "namesAngle")) {
		}
		else if (checker.compare(this.getClass(), "Sets whether or not the branches are to be shown proportional to their lengths", "[on = proportional; off]", commandName, "branchLengthsToggle")) {
			if (fixedSettings != null && fixedSettings.length>0 && fixedSettings[0])
				return null;
			resetShowBranchLengths=true;
			showBranchLengths.toggleValue(parser.getFirstToken(arguments));
			if (!showBranchLengths.getValue()) {
				deleteMenuItem(fixedScalingMenuItem);
				deleteMenuItem(showScaleMenuItem);
				deleteMenuItem(broadScaleMenuItem);
				if (stretchMenuItem == null)
					stretchMenuItem = addCheckMenuItem(null, "Inhibit Stretch tree to Fit", makeCommand("inhibitStretchToggle", this), inhibitStretch);
				if (evenMenuItem == null)
					evenMenuItem = addCheckMenuItem(null, "Even root to tip spacing", makeCommand("toggleEven", this), even);

			}
			else {
				fixedScalingMenuItem = addMenuItem( "Fixed Scaling...", makeCommand("setFixedScaling", this));
				showScaleMenuItem = addCheckMenuItem(null, "Show scale", makeCommand("toggleScale", this), showScale);
				broadScaleMenuItem = addCheckMenuItem(null, "Broad scale", makeCommand("toggleBroadScale", this), broadScale);
				deleteMenuItem(stretchMenuItem);
				stretchMenuItem = null;
				deleteMenuItem(evenMenuItem);
				evenMenuItem = null;
			}
			resetContainingMenuBar();
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets fixed scale length", "[length of branch lengths scale]", commandName, "setFixedScaling")) {
			double newDepth;
			if (StringUtil.blank(arguments))
				newDepth= MesquiteDouble.queryDouble(containerOfModule(), "Set scaling depth", "Depth:", fixedDepth);
			else 
				newDepth= MesquiteDouble.fromString(arguments);
			if (MesquiteDouble.isCombinable(newDepth) && newDepth>0) {
				//TODO: remember these fixedScaling and depth to set in calcnodelocs below!!!!
				fixedScale = true;
				fixedDepth = newDepth;
				leaveScaleAlone = false;
				if (offFixedScalingMenuItem == null) {
					offFixedScalingMenuItem = addMenuItem( "Off Fixed Scaling", makeCommand("offFixedScaling", this));
					resetContainingMenuBar();
				}
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to draw the scale for branch lengths", "[on or off]", commandName, "toggleScale")) {
			showScale.toggleValue(parser.getFirstToken(arguments));
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to draw the scale broadly, beneath the entire tree", "[on or off]", commandName, "toggleBroadScale")) {
			broadScale.toggleValue(parser.getFirstToken(arguments));
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Turns off fixed scaling", null, commandName, "offFixedScaling")) {
			fixedScale = false;
			leaveScaleAlone = false;
			deleteMenuItem(offFixedScalingMenuItem);
			offFixedScalingMenuItem = null;
			resetContainingMenuBar();
			parametersChanged();
		}

		else if (checker.compare(this.getClass(), "Sets scale color", "[scale color]", commandName, "scaleColor")) {
		        //Debugg.println("*Scale color got" + parser.getFirstToken(arguments));
			scaleColor = ColorDistribution.getStandardColor(MesquiteInteger.fromString(arguments));
			scaleCounterColor = Color.BLACK;  //ColorDistribution.getContrasting(scaleColor);
		}
		else if (checker.compare(this.getClass(), "Sets scale border width", "[border width in pixels]", commandName, "scaleBorderWidth")) {
		        //Debugg.println("*Scale border width got" + parser.getFirstToken(arguments));
			scaleBorderWidth = MesquiteInteger.fromString(arguments);
		}
		else if (checker.compare(this.getClass(), "Sets scale border color", "[border color]", commandName, "scaleBorderColor")) {
		        //Debugg.println("*Scale border color got" + parser.getFirstToken(arguments));
			scaleBorderColor = parser.getFirstToken(arguments);
		}
		else if (checker.compare(this.getClass(), "Sets scale border style", "[border line style]", commandName, "scaleBorderLineStyle")) {
	                //Debugg.println("Scale border style got" + parser.getFirstToken(arguments));
			scaleBorderLineStyle = parser.getFirstToken(arguments);
		}
		else if (checker.compare(this.getClass(), "Sets scale title", "[title string]", commandName, "scaleTitle")) {
		        //Debugg.println("Scale title got" + parser.getFirstToken(arguments));
			scaleTitle = arguments;
		}
		else if (checker.compare(this.getClass(), "Sets scale font", "[font family]", commandName, "scaleFont")) {
		        //Debugg.println("*Scale font got" + parser.getFirstToken(arguments));
			scaleFont = parser.getFirstToken(arguments);
		}
		else if (checker.compare(this.getClass(), "Sets scale font size", "[font size]", commandName, "scaleFontSize")) {
		        //Debugg.println("*Scale font size got" + parser.getFirstToken(arguments));
			scaleFontSize = MesquiteInteger.fromString(arguments);
		}
		else if (checker.compare(this.getClass(), "Sets scale font face", "[font face]", commandName, "scaleFontFace")) {
		        //Debugg.println("Scale font face got" + parser.getFirstToken(arguments));
			scaleFontFace = parser.getFirstToken(arguments);
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	public String getName() {
		return "Node Locations (standard)";
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Calculates the node locations in a tree drawing, for use with vertical or horizontal tree drawers (e.g., the standard diagnonal or square trees)." ;
	}
	public boolean compatibleWithOrientation(int orientation) {
		
		return (orientation==TreeDisplay.UP || orientation==TreeDisplay.DOWN || orientation==TreeDisplay.RIGHT ||orientation==TreeDisplay.LEFT);
	}
	public void setDefaultOrientation(TreeDisplay treeDisplay) {
		if (employerAllowsReorientation())
			treeDisplay.setOrientation(defaultOrientation);
	}
	public int getDefaultOrientation() {
		return defaultOrientation;
	}
	/*_________________________________________________*/

	private double getNonZeroBranchLength(Tree tree, int N) {
		if (tree.branchLengthUnassigned(N))
			return 1;
		else
			return tree.getBranchLength(N);
	}
	/*_________________________________________________*/
	private int lastleft;
	/*_________________________________________________*/
	private void UPCalcInternalLocs(TreeDrawing treeDrawing, Tree tree, int N) {
		if (tree.nodeIsInternal(N)) { //internal
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				UPCalcInternalLocs(treeDrawing, tree, d);
			int fD =tree.firstDaughterOfNode(N);
			int lD =tree.lastDaughterOfNode(N);
			if (lD==fD)   {//only one descendant
				treeDrawing.y[N] = treeDrawing.y[fD];
				treeDrawing.x[N] =treeDrawing.x[fD];
			}
			else {
				int nFDx = treeDrawing.x[fD];
				int nFDy = treeDrawing.y[fD];
				int nLDx = treeDrawing.x[lD];
				int nLDy = treeDrawing.y[lD];
				treeDrawing.y[N] = (-nFDx + nLDx+nFDy + nLDy) / 2;
				treeDrawing.x[N] =(nFDx + nLDx - nFDy + nLDy) / 2;
			}
		}
	}
	/*_________________________________________________*/
	private void UPDOWNCenterInternalLocs(TreeDrawing treeDrawing, Tree tree, int N) {
		if (tree.nodeIsInternal(N)) { //internal
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				UPDOWNCenterInternalLocs(treeDrawing, tree, d);
			int fD =tree.firstDaughterOfNode(N);
			int lD =tree.lastDaughterOfNode(N);
			if (lD!=fD)   {//> one descendant
				int nFDx = treeDrawing.x[fD];
				int nLDx = treeDrawing.x[lD];
				treeDrawing.x[N] =(nFDx + nLDx) / 2;
			}
		}
	}

	/*....................................................................................................*/
	private void UPCalcTerminalLocs(TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int N, boolean inTriangle, int numInTriangle, int triangleBase) {
		if  (tree.nodeIsTerminal(N)) {   //terminal
			if (inTriangle && tree.numberOfTerminalsInClade(triangleBase)>3 && treeDisplay.getSimpleTriangle()){
				if (tree.leftmostTerminalOfNode(triangleBase)==N)
					lastleft+= getSpacing(treeDisplay, tree, N, inTriangle); 
				else {
					//more than 2 in triangle; triangle as wide as 3.  Thus each 
					if (tree.rightmostTerminalOfNode(triangleBase)==N)
						lastleft= treeDrawing.x[tree.leftmostTerminalOfNode(triangleBase)] + 2*treeDisplay.getTaxonSpacing();
					else 
						lastleft+= (getSpacing(treeDisplay, tree, N, inTriangle)*2)/(numInTriangle-1);
				}
			}
			else
				lastleft+= getSpacing(treeDisplay, tree, N, inTriangle);
			treeDrawing.y[N] = treeDisplay.getTipsMargin();
			treeDrawing.x[N] = lastleft;
		}
		else {
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				if (inTriangle)
					UPCalcTerminalLocs(treeDisplay, treeDrawing, tree, d,true, numInTriangle, triangleBase);
				else
					UPCalcTerminalLocs(treeDisplay, treeDrawing, tree, d, tree.getAssociatedBit(triangleNameRef, d), tree.numberOfTerminalsInClade(d), d);
			}
		}
	}
	/*....................................................................................................*/
	private void UPevenNodeLocs(TreeDrawing treeDrawing, Tree tree, int N, int evenVertSpacing) {
		if (tree.nodeIsInternal(N)){
			int deepest = 0;
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				UPevenNodeLocs(treeDrawing, tree, d, evenVertSpacing);
				if (treeDrawing.y[d]>deepest)
					deepest = treeDrawing.y[d];
			}
			treeDrawing.y[N] = deepest + evenVertSpacing;
		}
	}
	/*....................................................................................................*/
	private void UPstretchNodeLocs(TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int N) {
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			UPstretchNodeLocs(treeDisplay, treeDrawing, tree, d);
		treeDrawing.y[N] = treeDisplay.getTipsMargin() + (int)((treeDrawing.y[N]-treeDisplay.getTipsMargin())*treeDisplay.nodeLocsParameters[stretchfactor]);
	}

	/*....................................................................................................*/
	private void UPdoAdjustLengths (TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int bottom, int N, double ancH, int root) {
		double nH;
		if (N==root) {
			nH=bottom;
		}
		else {
			nH=ancH - (getNonZeroBranchLength(tree, N)*treeDisplay.nodeLocsParameters[scaling]);
		}
		treeDrawing.y[N]=(int)(nH);
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			UPdoAdjustLengths(treeDisplay, treeDrawing, tree, bottom, d, nH, root);

	}
	/*_________________________________________________*/
	private void DOWNCalcInternalLocs(TreeDrawing treeDrawing, Tree tree, int N) {
		if (tree.nodeIsInternal(N)) { //internal
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				DOWNCalcInternalLocs(treeDrawing, tree, d);
			int nFD = tree.firstDaughterOfNode(N);
			int nLD = tree.lastDaughterOfNode(N);
			int nFDx = treeDrawing.x[nFD];
			int nFDy = treeDrawing.y[nFD];
			int nLDx = treeDrawing.x[nLD];
			int nLDy = treeDrawing.y[nLD];
			if (nLD==nFD)   {//only one descendant; put same as descendant, to be adjusted later
				treeDrawing.y[N] = treeDrawing.y[nFD];
				treeDrawing.x[N] =treeDrawing.x[nFD];
			}
			else {
				treeDrawing.y[N] = (nFDx - nLDx + nFDy + nLDy) / 2;
				treeDrawing.x[N] =(nFDx + nLDx + nFDy - nLDy) / 2;
			}
		}
	}

	/*....................................................................................................*/
	private void DOWNCalcTerminalLocs(TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int N, int margin,  boolean inTriangle, int numInTriangle, int triangleBase) {
		if  (tree.nodeIsTerminal(N)) {   //terminal
			if (inTriangle && tree.numberOfTerminalsInClade(triangleBase)>3 && treeDisplay.getSimpleTriangle()){
				if (tree.leftmostTerminalOfNode(triangleBase)==N)
					lastleft+= getSpacing(treeDisplay, tree, N, inTriangle); 
				else {
					//more than 2 in triangle; triangle as wide as 3.  Thus each 
					if (tree.rightmostTerminalOfNode(triangleBase)==N)
						lastleft= treeDrawing.x[tree.leftmostTerminalOfNode(triangleBase)] + 2*getSpacing(treeDisplay, tree, N, inTriangle);
					else 
						lastleft+= (getSpacing(treeDisplay, tree, N,inTriangle)*2)/(numInTriangle-1);
				}
			}
			else
				lastleft+= getSpacing(treeDisplay, tree, N, inTriangle);
			treeDrawing.y[N] = margin;
			treeDrawing.x[N] = lastleft;
		}
		else {
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				if (inTriangle)
					DOWNCalcTerminalLocs(treeDisplay, treeDrawing, tree, d, margin, true, numInTriangle, triangleBase);
				else
					DOWNCalcTerminalLocs(treeDisplay, treeDrawing, tree, d, margin, tree.getAssociatedBit(triangleNameRef, d), tree.numberOfTerminalsInClade(d), d);
		}
	}
	/*....................................................................................................*/
	private void DOWNstretchNodeLocs(TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int N, int margin) {
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			DOWNstretchNodeLocs(treeDisplay, treeDrawing, tree, d, margin);
		treeDrawing.y[N] = margin-(int)((margin-treeDrawing.y[N])*treeDisplay.nodeLocsParameters[stretchfactor]);
	}
	/*....................................................................................................*/
	private void DOWNevenNodeLocs(TreeDrawing treeDrawing, Tree tree, int N, int evenVertSpacing) {
		if (tree.nodeIsInternal(N)){
			int deepest = 10000000;
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				DOWNevenNodeLocs(treeDrawing, tree, d, evenVertSpacing);
				if (treeDrawing.y[d]<deepest)
					deepest = treeDrawing.y[d];
			}
			treeDrawing.y[N] = deepest - evenVertSpacing;
		}
	}

	/*....................................................................................................*/
	private void DOWNdoAdjustLengths (TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int bottom, int N, double ancH, int root) {
		double nH;
		if (N==root) 
			nH=bottom;
		else
			nH=ancH + (getNonZeroBranchLength(tree, N)*treeDisplay.nodeLocsParameters[scaling]);

		treeDrawing.y[N]=(int)(nH);
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			DOWNdoAdjustLengths(treeDisplay, treeDrawing, tree, bottom, d, nH, root);

	}
	/*_________________________________________________*/
	private void RIGHTCalcInternalLocs(TreeDrawing treeDrawing, Tree tree, int N) {
		if (tree.nodeIsInternal(N)) { //internal
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				RIGHTCalcInternalLocs(treeDrawing, tree,  d);
			int fD = tree.firstDaughterOfNode(N);
			int lD = tree.lastDaughterOfNode(N);
			int nFDx = treeDrawing.x[fD];
			int nFDy = treeDrawing.y[fD];
			int nLDx = treeDrawing.x[lD];
			int nLDy = treeDrawing.y[lD];
			if (lD==fD)   {//only one descendant
				treeDrawing.y[N] = treeDrawing.y[fD];
				treeDrawing.x[N] =treeDrawing.x[fD];
			}
			else {
				treeDrawing.x[N] =(nFDy - nLDy + nFDx + nLDx) / 2;
				treeDrawing.y[N] =(nFDx - nLDx + nFDy + nLDy) / 2;
			}
		}
	}
	/*_________________________________________________*/
	private void RIGHTLEFTCenterInternalLocs(TreeDrawing treeDrawing, Tree tree, int N) {
		if (tree.nodeIsInternal(N)) { //internal
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				RIGHTLEFTCenterInternalLocs(treeDrawing, tree, d);
			int fD =tree.firstDaughterOfNode(N);
			int lD =tree.lastDaughterOfNode(N);
			if (lD!=fD)   {//> one descendant
				int nFDy = treeDrawing.y[fD];
				int nLDy = treeDrawing.y[lD];
				treeDrawing.y[N] =(nFDy + nLDy) / 2;
			}
		}
	}

	/*....................................................................................................*/
	private void RIGHTCalcTerminalLocs(TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int N, int margin,  boolean inTriangle, int numInTriangle, int triangleBase) {
		if  (tree.nodeIsTerminal(N)) {   //terminal
			if (inTriangle && tree.numberOfTerminalsInClade(triangleBase)>3 && treeDisplay.getSimpleTriangle()){
				if (tree.leftmostTerminalOfNode(triangleBase)==N)
					lastleft+= getSpacing(treeDisplay, tree, N, inTriangle); 
				else {
					//more than 2 in triangle; triangle as wide as 3.  Thus each 
					if (tree.rightmostTerminalOfNode(triangleBase)==N)
						lastleft= treeDrawing.y[tree.leftmostTerminalOfNode(triangleBase)] + 2*getSpacing(treeDisplay, tree, N, inTriangle);
					else 
						lastleft+= (getSpacing(treeDisplay, tree, N, inTriangle)*2)/(numInTriangle-1);
				}
			}
			else
				lastleft+= getSpacing(treeDisplay, tree, N, inTriangle);
			treeDrawing.x[N] = margin;
			treeDrawing.y[N] = lastleft;
		}
		else {
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				if (inTriangle)
					RIGHTCalcTerminalLocs(treeDisplay, treeDrawing, tree, d, margin, true, numInTriangle, triangleBase);
				else
					RIGHTCalcTerminalLocs(treeDisplay, treeDrawing, tree, d, margin, tree.getAssociatedBit(triangleNameRef, d), tree.numberOfTerminalsInClade(d),d);
		}
	}
	/*....................................................................................................*/
	private void RIGHTstretchNodeLocs(TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int N, int margin) {
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			RIGHTstretchNodeLocs(treeDisplay, treeDrawing, tree, d, margin);
		treeDrawing.x[N] =  margin- (int)((margin - treeDrawing.x[N])*treeDisplay.nodeLocsParameters[stretchfactor]);
	}

	/*....................................................................................................*/
	private void RIGHTevenNodeLocs(TreeDrawing treeDrawing, Tree tree, int N, int evenVertSpacing) {
		if (tree.nodeIsInternal(N)){
			int deepest = 1000000;
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				RIGHTevenNodeLocs(treeDrawing, tree, d, evenVertSpacing);
				if (treeDrawing.x[d]<deepest)
					deepest = treeDrawing.x[d];
			}
			treeDrawing.x[N] = deepest - evenVertSpacing;
		}
	}
	/*....................................................................................................*/
	private void RIGHTdoAdjustLengths (TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int bottom, int N, double ancH, int root) {
		double nH;

		if (N==root) 
			nH=bottom;
		else 
			nH=ancH + (getNonZeroBranchLength(tree, N)*treeDisplay.nodeLocsParameters[scaling]);
		treeDrawing.x[N]=(int)(nH);

		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			RIGHTdoAdjustLengths(treeDisplay, treeDrawing, tree, bottom, d, nH, root);
	}
	/*_________________________________________________*/
	private void LEFTCalcInternalLocs(TreeDrawing treeDrawing, Tree tree, int N) {
		if (tree.nodeIsInternal(N)) { //internal
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				LEFTCalcInternalLocs(treeDrawing, tree, d);
			int fD = tree.firstDaughterOfNode(N);
			int lD = tree.lastDaughterOfNode(N);
			int nFDx = treeDrawing.x[fD];
			int nFDy = treeDrawing.y[fD];
			int nLDx = treeDrawing.x[lD];
			int nLDy = treeDrawing.y[lD];
			if (lD==fD)   {//only one descendant
				treeDrawing.y[N] = treeDrawing.y[fD];
				treeDrawing.x[N] =treeDrawing.x[fD];
			}
			else {
				treeDrawing.x[N] =(nLDy - nFDy + nLDx + nFDx) / 2;
				treeDrawing.y[N] =(nLDx - nFDx + nLDy + nFDy) / 2;
			}
		}
	}

	/*....................................................................................................*/
	private void LEFTCalcTerminalLocs(TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int N, int margin,  boolean inTriangle, int numInTriangle, int triangleBase) {
		if  (tree.nodeIsTerminal(N)) {   //terminal
			if (inTriangle && tree.numberOfTerminalsInClade(triangleBase)>3 && treeDisplay.getSimpleTriangle()){
				if (tree.leftmostTerminalOfNode(triangleBase)==N)
					lastleft+= getSpacing(treeDisplay, tree, N, inTriangle); 
				else {
					//more than 2 in triangle; triangle as wide as 3.  Thus each 
					if (tree.rightmostTerminalOfNode(triangleBase)==N)
						lastleft= treeDrawing.y[tree.leftmostTerminalOfNode(triangleBase)] + 2*getSpacing(treeDisplay, tree, N, inTriangle);
					else 
						lastleft+= (getSpacing(treeDisplay, tree, N, inTriangle)*2)/(numInTriangle-1);
				}
			}
			else
				lastleft+= getSpacing(treeDisplay, tree, N, inTriangle);
			treeDrawing.x[N] = margin;
			treeDrawing.y[N] = lastleft;
		}
		else {
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				if (inTriangle)
					LEFTCalcTerminalLocs(treeDisplay, treeDrawing, tree, d, margin, true, numInTriangle, triangleBase);
				else
					LEFTCalcTerminalLocs(treeDisplay, treeDrawing, tree, d, margin, tree.getAssociatedBit(triangleNameRef, d),tree.numberOfTerminalsInClade(d),d);
		}
	}
	/*....................................................................................................*/
	private void LEFTstretchNodeLocs(TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int N) {
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			LEFTstretchNodeLocs(treeDisplay, treeDrawing, tree, d);
		treeDrawing.x[N] = treeDisplay.getTipsMargin() + (int)((treeDrawing.x[N]-treeDisplay.getTipsMargin())*treeDisplay.nodeLocsParameters[stretchfactor]);
	}

	/*....................................................................................................*/
	private void LEFTevenNodeLocs(TreeDrawing treeDrawing, Tree tree, int N, int evenVertSpacing) {
		if (tree.nodeIsInternal(N)){
			int deepest = 0;
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				LEFTevenNodeLocs(treeDrawing, tree, d, evenVertSpacing);
				if (treeDrawing.x[d]>deepest)
					deepest = treeDrawing.x[d];
			}
			treeDrawing.x[N] = deepest + evenVertSpacing;
		}
	}
	/*....................................................................................................*/
	private void LEFTdoAdjustLengths (TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int bottom, int N, double ancH, int root) {
		double nH;
		if (N==root) 
			nH=bottom;
		else 
			nH=ancH - (getNonZeroBranchLength(tree, N)*treeDisplay.nodeLocsParameters[scaling]);

		treeDrawing.x[N]=(int)(nH);

		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			LEFTdoAdjustLengths(treeDisplay, treeDrawing, tree, bottom, d, nH, root);
	}
	/*....................................................................................................*/
	private int edgeNode (TreeDrawing treeDrawing, Tree tree, int node, boolean x, boolean max) {
		if (tree.nodeIsTerminal(node)) {
			if (x)
				return treeDrawing.x[node];
			else
				return treeDrawing.y[node];
		}
		int t;
		if (max)
			t = 0;
		else
			t = 1000000;
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			int e = edgeNode( treeDrawing, tree, d, x, max);
			if (max && e> t)
				t = e;
			else if (!max && e < t)
				t = e;
		}
		return t;
	}
	/*_________________________________________________*/
	private int propAverage(int xd, int xa, int i, int L){
		return (int)(1.0*i*(xa-xd)/L + xd);
	}

	private void placeSingletons (TreeDrawing treeDrawing, Tree tree, int N) {
		if (tree.numberOfDaughtersOfNode(N)==1)	{
			int bD = tree.branchingDescendant(N);
			int bA;
			if (N==tree.getRoot()) {
				bA = tree.getSubRoot();
			}
			else {
				bA = tree.branchingAncestor(N);
				if (bA == tree.getRoot() && tree.numberOfDaughtersOfNode(bA)==1)
					bA = tree.getSubRoot();
			}
			int nA = tree.depthToAncestor(N, bA);
			int nD = tree.depthToAncestor(bD, N);
			
			MesquiteNumber xValue=new MesquiteNumber();
			MesquiteNumber yValue=new MesquiteNumber();
			MesquiteDouble angle = new MesquiteDouble();
			treeDrawing.getSingletonLocation(tree, N,  xValue,  yValue);
			treeDrawing.x[N]=xValue.getIntValue();
			treeDrawing.y[N]=yValue.getIntValue();

				
		//	treeDrawing.x[N]=propAverage(treeDrawing.x[bD], treeDrawing.x[bA], nD, nA+nD);
		//	treeDrawing.y[N]=propAverage(treeDrawing.y[bD], treeDrawing.y[bA], nD, nA+nD);
		}
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			placeSingletons(treeDrawing, tree, d);
	}
	/*....................................................................................................*/
	private void AdjustForUnbranchedNodes(TreeDrawing treeDrawing, Tree tree, int N, int subRoot) {
		if (tree.nodeIsInternal(N)) { //internal
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				AdjustForUnbranchedNodes(treeDrawing, tree, d, subRoot);
			if (tree.lastDaughterOfNode(N) == tree.firstDaughterOfNode(N)) {  // has only one Daughter
				if (tree.numberOfDaughtersOfNode(tree.motherOfNode(N)) != 1 || tree.motherOfNode(N)==subRoot) { //and is base of chain w 1
					//count length of chain of nodes with only one Daughter
					int count = 2;  // at least 2 in chain
					int q = tree.firstDaughterOfNode(N);
					while (tree.nodeIsInternal(q) && tree.firstDaughterOfNode(q) ==tree.lastDaughterOfNode(q)) {
						count++;
						q = tree.firstDaughterOfNode(q);
					}
					//adjust nodes in chain
					int bottomX =treeDrawing.x[tree.motherOfNode(N)] ;
					int bottomY =treeDrawing.y[tree.motherOfNode(N)] ;
					int topX =treeDrawing.x[N] ;
					int topY =treeDrawing.y[N] ;
					treeDrawing.y[N] = (bottomY+topY)/count;
					treeDrawing.x[N] = (bottomX+topX)/count;
					int count2=1;
					q = tree.firstDaughterOfNode(N);
					while (tree.nodeIsInternal(q) && tree.firstDaughterOfNode(q) ==tree.lastDaughterOfNode(q)) {
						count2++;
						treeDrawing.y[q] = (bottomY+topY)*count2/count;
						treeDrawing.x[q] = (bottomX+topX)*count2/count;
						q = tree.firstDaughterOfNode(q);
					}
				}
			}
		}
	}
	/*....................................................................................................*/
	FontMetrics fm;
	private int findMaxNameLength(Tree tree, int N) {
		if (tree.nodeIsTerminal(N)) {
			String s = tree.getTaxa().getName(tree.taxonNumberOfNode(N));
			if (s==null)
				return 0;
			else
				return fm.stringWidth(s);
		}
		else {
			int max = 0;
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				int cur = findMaxNameLength(tree, d);
				if (cur>max)
					max = cur;
			}
			return max;
		}
	}
	/*.................................................................................................................*/
	public int effectiveNumberOfTerminals(Tree tree, int node){
		if (tree.nodeIsTerminal(node))
			return 1;
		else if (tree.getAssociatedBit(triangleNameRef, node)) {
			if (tree.numberOfTerminalsInClade(node)>2)
				return 3;
			else 
				return 2;
		}
		int num=0;
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			num += effectiveNumberOfTerminals(tree, d);
		}
		return num;
	}
	/*.................................................................................................................*/
	int zoomNode = -1;
	double zoomFactor = 1.0;
	void setZoom(TreeDisplay treeDisplay, int node, double factor){
		if (!treeDisplay.getTree().descendantOf(node, zoomNode)){ //in zoom
			int inZoom = treeDisplay.getTree().numberOfTerminalsInClade(zoomNode);
			int outZoom = treeDisplay.getTree().numberOfTerminalsInClade(treeDisplay.getTreeDrawing().getDrawnRoot()) - inZoom;
			double currentFactor = (1 - (zoomFactor-1)*inZoom/outZoom);
			factor *= currentFactor;
		}
		zoomNode = node;
		zoomFactor = factor;
		parametersChanged();
	}
	int getSpacing(TreeDisplay treeDisplay, Tree tree, int node, boolean inTriangle){
		if (inTriangle && !treeDisplay.getSimpleTriangle()) {
			int ancestralNode = tree.ancestorWithNameReference(triangleNameRef, node);
			if (ancestralNode==0 ||  node != tree.leftmostTerminalOfNode(ancestralNode))
				return 4;
		}
		int baseSpacing =treeDisplay.getTaxonSpacing();
		if (zoomNode > 0){
			int inZoom = tree.numberOfTerminalsInClade(zoomNode);
			if (inZoom<2)
				return baseSpacing;
			int outZoom = tree.numberOfTerminalsInClade(treeDisplay.getTreeDrawing().getDrawnRoot()) - inZoom;

			if (tree.descendantOf(node, zoomNode)){ //in zoom
				//	x * baseSpacing*  inZoom + y *baseSpacing*  outZoom = (inZoom + outZoom)*baseSpacing;
				return (int)(baseSpacing *zoomFactor);
				//return baseSpacing * (inZoom + outZoom) / inZoom/2;
			}
			else {
				return (int)(baseSpacing * (1 - (zoomFactor-1)*inZoom/outZoom));

				// return baseSpacing * (inZoom + outZoom) / outZoom/2;
			}
		}
		return baseSpacing;
	}
	
	/*.................................................................................................................*/
	public void calculateNodeLocs(TreeDisplay treeDisplay, Tree tree, int drawnRoot, Rectangle rect) { //Graphics g removed as parameter May 02
		if (MesquiteTree.OK(tree)) {
			int effectiveROOTSIZE = ROOTSIZE;
			if (tree.numberOfTerminalsInClade(drawnRoot) == 1){
				effectiveROOTSIZE += rect.height/2;
			}
			/*if (!stretchWasSet){
				if (treeDisplay.inhibitStretchByDefault != inhibitStretch.getValue())
					inhibitStretch.setValue(treeDisplay.inhibitStretchByDefault);
			}*/
			treeDisplay.setFixedTaxonSpacing(fixedTaxonDistance);  //NEW
		//	int fixedTaxonDistance = treeDisplay.getFixedTaxonSpacing();  OLD code
			lastOrientation = treeDisplay.getOrientation();
			//this.treeDisplay = treeDisplay; 
			if (!leaveScaleAlone) {
				treeDisplay.fixedDepthScale = fixedDepth;
				treeDisplay.fixedScalingOn = fixedScale;
			}
			TreeDrawing treeDrawing = treeDisplay.getTreeDrawing();
			//		treeDrawing.namesAngle = namesAngle;
			//this.tree = tree;
			if (treeDisplay.getExtras() !=null) {
				if (treeDisplay.getExtras().myElements(this)==null) {  //todo: need to do one for each treeDisplay!
					NodeLocsExtra extra = new NodeLocsExtra(this, treeDisplay); 
					treeDisplay.addExtra(extra); 
					extras.addElement(extra);
				}
			}
			int root = drawnRoot;
			int subRoot = tree.motherOfNode(drawnRoot);
			int buffer = 20;

			Graphics g = treeDisplay.getGraphics();
			if (g!=null) {
				if (!treeDisplay.suppressNames) {
					DrawNamesTreeDisplay dtn = treeDisplay.getDrawTaxonNames();
					Font f = null;
					if (dtn!=null)
						f = dtn.getFont();

					if (f==null)
						f = g.getFont();
					fm=g.getFontMetrics(f);
					if (fm!=null)
						treeDisplay.setTipsMargin(findMaxNameLength(tree, root) + treeDisplay.getTaxonNameBuffer() + treeDisplay.getTaxonNameDistance());
				}
				else 
					treeDisplay.setTipsMargin(treeDisplay.getTaxonNameBuffer());
				g.dispose();
			}

			int marginOffset=0;
			if (resetShowBranchLengths)
				treeDisplay.showBranchLengths=showBranchLengths.getValue();
			else {
				if (treeDisplay.showBranchLengths != showBranchLengths.getValue()) {
					showBranchLengths.setValue(treeDisplay.showBranchLengths);
					if (!showBranchLengths.getValue()) {
						deleteMenuItem(fixedScalingMenuItem);
						deleteMenuItem(showScaleMenuItem);
						deleteMenuItem(broadScaleMenuItem);
						if (stretchMenuItem == null)
							stretchMenuItem = addCheckMenuItem(null, "Stretch tree to Fit", makeCommand("stretchToggle", this), inhibitStretch);
						if (evenMenuItem == null)
							evenMenuItem = addCheckMenuItem(null, "Even root to tip spacing", makeCommand("toggleEven", this), even);
					}
					else {
						fixedScalingMenuItem = addMenuItem( "Fixed Scaling...", makeCommand("setFixedScaling", this));
						showScaleMenuItem = addCheckMenuItem(null, "Show scale", makeCommand("toggleScale", this), showScale);
						broadScaleMenuItem = addCheckMenuItem(null, "Broad scale", makeCommand("toggleBroadScale", this), broadScale);
						deleteMenuItem(stretchMenuItem);
						stretchMenuItem = null;
						deleteMenuItem(evenMenuItem);
						evenMenuItem = null;
					}
					resetContainingMenuBar();
				}
			}
			if (!compatibleWithOrientation(treeDisplay.getOrientation()))
				setDefaultOrientation(treeDisplay);
			
			if (treeDisplay.getOrientation()==TreeDisplay.UP) {
				int numTerms = effectiveNumberOfTerminals(tree, root);
				if (numTerms == 0)
					numTerms = 1;
				if (fixedTaxonDistance!=0 && MesquiteInteger.isCombinable(fixedTaxonDistance))
					treeDisplay.setTaxonSpacing(fixedTaxonDistance);
				else
					treeDisplay.setTaxonSpacing( (rect.width - 30) / numTerms);
				if (numTerms*treeDisplay.getTaxonSpacing()>.95*rect.width && treeDisplay.getTaxonSpacing()/2*2 != treeDisplay.getTaxonSpacing())  //if odd
					treeDisplay.setTaxonSpacing(treeDisplay.getTaxonSpacing()-1);
				lastleft = -treeDisplay.getTaxonSpacing()/3*2; //TODO: this causes problems for shrunk, since first taxon doesn't move over enough
				UPCalcTerminalLocs(treeDisplay, treeDrawing, tree, root, tree.getAssociatedBit(triangleNameRef, root), tree.numberOfTerminalsInClade(root), root);
				UPCalcInternalLocs( treeDrawing, tree, root);
				if (center.getValue())
					UPDOWNCenterInternalLocs( treeDrawing, tree, root);
				//AdjustForUnbranchedNodes(root, subRoot);
				marginOffset = treeDisplay.getTipsMargin() + rect.y;
				treeDrawing.y[subRoot] = (treeDrawing.y[root])+effectiveROOTSIZE;
				treeDrawing.x[subRoot] = (treeDrawing.x[root])-effectiveROOTSIZE;
				placeSingletons(treeDrawing, tree, root);
				if (treeDisplay.showBranchLengths) {
					treeDisplay.nodeLocsParameters[totalHeight]= tree.tallestPathAboveNode(root, 1.0);
					if (!treeDisplay.fixedScalingOn) {
						treeDisplay.fixedDepthScale = treeDisplay.nodeLocsParameters[totalHeight];
						fixedDepth = treeDisplay.fixedDepthScale;
						if (treeDisplay.nodeLocsParameters[totalHeight]==0){
							treeDisplay.nodeLocsParameters[scaling]=1;
						}
						else
							treeDisplay.nodeLocsParameters[scaling]=((double)(rect.height-treeDisplay.getTipsMargin()-buffer - effectiveROOTSIZE))/(treeDisplay.nodeLocsParameters[totalHeight]); 
						UPdoAdjustLengths( treeDisplay, treeDrawing, tree, rect.height-effectiveROOTSIZE-buffer, root, 0, root);
						if (showScale.getValue()) {
							drawGrid(treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.nodeLocsParameters[scaling], tree, drawnRoot, treeDisplay, g);
						}
					}
					else {
						if (treeDisplay.fixedDepthScale == 0)
							treeDisplay.fixedDepthScale = 1;

						treeDisplay.nodeLocsParameters[scaling]=((double)(rect.height-treeDisplay.getTipsMargin()-buffer - effectiveROOTSIZE))/(treeDisplay.fixedDepthScale); 
						UPdoAdjustLengths( treeDisplay, treeDrawing, tree, rect.height-effectiveROOTSIZE-(int)(treeDisplay.nodeLocsParameters[scaling]*(treeDisplay.fixedDepthScale-treeDisplay.nodeLocsParameters[totalHeight])+buffer), root, 0, root);
						if (showScale.getValue()) {
							drawGrid(treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.fixedDepthScale, treeDisplay.nodeLocsParameters[scaling], tree, drawnRoot, treeDisplay, g);
						}
					}

					treeDrawing.y[subRoot] = (treeDrawing.y[root])+(int)(getNonZeroBranchLength(tree, root)*treeDisplay.nodeLocsParameters[scaling]); //effectiveROOTSIZE
					treeDrawing.x[subRoot] = (treeDrawing.x[root])-(int)(getNonZeroBranchLength(tree, root)*treeDisplay.nodeLocsParameters[scaling]);
				}
				else {
					if (even.getValue()){
						int evenVertSpacing =(int)((treeDrawing.y[subRoot] - edgeNode(treeDrawing, tree, root, false, false))/ (tree.mostStepsAboveNode(root) + 1));
						if (evenVertSpacing > 0)
							UPevenNodeLocs(treeDrawing, tree, root, evenVertSpacing);
					}
					if (!inhibitStretch.getValue() && (treeDisplay.autoStretchIfNeeded )) { //&& treeDrawing.y[subRoot]>rect.height
						treeDisplay.nodeLocsParameters[stretchfactor]=((double)(rect.height-treeDisplay.getTipsMargin())) / (treeDrawing.y[subRoot] - (int)treeDisplay.getTipsMargin());
						UPstretchNodeLocs(treeDisplay, treeDrawing, tree, root);
						treeDrawing.y[subRoot]=rect.height-5;
					}
				}

			}
			else if (treeDisplay.getOrientation()==TreeDisplay.DOWN) {
				int numTerms = effectiveNumberOfTerminals(tree, root);
				if (numTerms == 0)
					numTerms = 1;
				if (fixedTaxonDistance!=0 && MesquiteInteger.isCombinable(fixedTaxonDistance))
					treeDisplay.setTaxonSpacing(fixedTaxonDistance);
				else
					treeDisplay.setTaxonSpacing( (rect.width - 30) / numTerms);
				if (numTerms*treeDisplay.getTaxonSpacing()>.95*rect.width && treeDisplay.getTaxonSpacing()/2*2 != treeDisplay.getTaxonSpacing())  //if odd
					treeDisplay.setTaxonSpacing(treeDisplay.getTaxonSpacing()-1);
				lastleft = -treeDisplay.getTaxonSpacing()/3*2;
				DOWNCalcTerminalLocs(treeDisplay, treeDrawing, tree, root, rect.height-treeDisplay.getTipsMargin(), tree.getAssociatedBit(triangleNameRef, root), tree.numberOfTerminalsInClade(root), root);
				DOWNCalcInternalLocs(treeDrawing, tree, root);
				if (center.getValue())
					UPDOWNCenterInternalLocs(treeDrawing, tree, root);
				//AdjustForUnbranchedNodes(root, subRoot);
				marginOffset = 0;
				treeDrawing.y[subRoot] = (treeDrawing.y[root])-effectiveROOTSIZE;
				treeDrawing.x[subRoot] = (treeDrawing.x[root])-effectiveROOTSIZE;
				placeSingletons(treeDrawing, tree, root);
				if (treeDisplay.showBranchLengths) {
					treeDisplay.nodeLocsParameters[totalHeight]=tree.tallestPathAboveNode(root, 1.0);
					if (!treeDisplay.fixedScalingOn) {
						treeDisplay.fixedDepthScale = treeDisplay.nodeLocsParameters[totalHeight];
						fixedDepth = treeDisplay.fixedDepthScale;
						if (treeDisplay.nodeLocsParameters[totalHeight]==0)
							treeDisplay.nodeLocsParameters[scaling]=1;
						else
							treeDisplay.nodeLocsParameters[scaling]=((double)(rect.height-treeDisplay.getTipsMargin() -buffer - effectiveROOTSIZE))/(treeDisplay.nodeLocsParameters[totalHeight]); 
						DOWNdoAdjustLengths(treeDisplay, treeDrawing, tree, effectiveROOTSIZE+ buffer, root, 0, root);
						if (showScale.getValue())
							drawGrid(treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.nodeLocsParameters[scaling], tree, drawnRoot, treeDisplay, g);
					}
					else {
						if (treeDisplay.fixedDepthScale == 0)
							treeDisplay.fixedDepthScale = 1;
						treeDisplay.nodeLocsParameters[scaling]=((double)(rect.height-treeDisplay.getTipsMargin()-buffer - effectiveROOTSIZE))/(treeDisplay.fixedDepthScale); 
						DOWNdoAdjustLengths(treeDisplay, treeDrawing, tree, effectiveROOTSIZE+(int)(treeDisplay.nodeLocsParameters[scaling]*(treeDisplay.fixedDepthScale-treeDisplay.nodeLocsParameters[totalHeight]) + buffer), root, 0, root);
						if (showScale.getValue())
							drawGrid(treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.fixedDepthScale, treeDisplay.nodeLocsParameters[scaling], tree, drawnRoot, treeDisplay, g);
					}

					treeDrawing.y[subRoot] = (treeDrawing.y[root])-(int)(getNonZeroBranchLength(tree, root)*treeDisplay.nodeLocsParameters[scaling]);
					treeDrawing.x[subRoot] = (treeDrawing.x[root])-(int)(getNonZeroBranchLength(tree, root)*treeDisplay.nodeLocsParameters[scaling]);
				}
				else {
					if (even.getValue()){
						int evenVertSpacing =(int)((- treeDrawing.y[subRoot] + edgeNode(treeDrawing, tree, root, false, true))/ (tree.mostStepsAboveNode(root) + 1));
						if (evenVertSpacing > 0)
							DOWNevenNodeLocs(treeDrawing, tree, root, evenVertSpacing);
					}
					if (!inhibitStretch.getValue() && treeDisplay.autoStretchIfNeeded) {  //&& treeDrawing.y[subRoot]>0)
						treeDisplay.nodeLocsParameters[stretchfactor]=((double)(rect.height-treeDisplay.getTipsMargin())) / (rect.height - treeDrawing.y[subRoot] - treeDisplay.getTipsMargin());
						DOWNstretchNodeLocs(treeDisplay, treeDrawing, tree, root, rect.height-treeDisplay.getTipsMargin());
						treeDrawing.y[subRoot]=5;
					}
				}
			}
			else if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
				int numTerms = effectiveNumberOfTerminals(tree, root);
				if (numTerms == 0)
					numTerms = 1;
				if (fixedTaxonDistance!=0 && MesquiteInteger.isCombinable(fixedTaxonDistance))
					treeDisplay.setTaxonSpacing(fixedTaxonDistance);
				else
					treeDisplay.setTaxonSpacing( (rect.height - 30) / numTerms);
				if (numTerms*treeDisplay.getTaxonSpacing()>.95*rect.height && treeDisplay.getTaxonSpacing()/2*2 != treeDisplay.getTaxonSpacing())  //if odd
					treeDisplay.setTaxonSpacing(treeDisplay.getTaxonSpacing()-1);
				lastleft = -treeDisplay.getTaxonSpacing()/3*2;
				RIGHTCalcTerminalLocs(treeDisplay, treeDrawing, tree, root, rect.width-treeDisplay.getTipsMargin(), tree.getAssociatedBit(triangleNameRef, root), tree.numberOfTerminalsInClade(root), root);
				RIGHTCalcInternalLocs(treeDrawing, tree, root);
				if (center.getValue())
					RIGHTLEFTCenterInternalLocs( treeDrawing, tree, root);
				//AdjustForUnbranchedNodes(root, subRoot);
				treeDrawing.y[subRoot] = (treeDrawing.y[root])-effectiveROOTSIZE;
				treeDrawing.x[subRoot] = (treeDrawing.x[root])-effectiveROOTSIZE;
				placeSingletons(treeDrawing, tree, root);
				if (treeDisplay.showBranchLengths) {
					treeDisplay.nodeLocsParameters[totalHeight]=tree.tallestPathAboveNode(root, 1.0);
					if (!treeDisplay.fixedScalingOn) {
						treeDisplay.fixedDepthScale = treeDisplay.nodeLocsParameters[totalHeight];
						fixedDepth = treeDisplay.fixedDepthScale;
						if (treeDisplay.nodeLocsParameters[totalHeight]==0)
							treeDisplay.nodeLocsParameters[scaling]=1;
						else
							treeDisplay.nodeLocsParameters[scaling]=((double)(rect.width-treeDisplay.getTipsMargin()-buffer - effectiveROOTSIZE))/(treeDisplay.nodeLocsParameters[totalHeight]); 
						RIGHTdoAdjustLengths(treeDisplay, treeDrawing, tree, effectiveROOTSIZE + buffer, root, 0, root);
						if (showScale.getValue())
							drawGrid(treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.nodeLocsParameters[scaling], tree, drawnRoot, treeDisplay, g);
					}
					else {
						if (treeDisplay.fixedDepthScale == 0)
							treeDisplay.fixedDepthScale = 1;
						treeDisplay.nodeLocsParameters[scaling]=((double)(rect.width-treeDisplay.getTipsMargin()-buffer - effectiveROOTSIZE))/(treeDisplay.fixedDepthScale); 
						RIGHTdoAdjustLengths(treeDisplay, treeDrawing, tree, effectiveROOTSIZE+(int)(treeDisplay.nodeLocsParameters[scaling]*(treeDisplay.fixedDepthScale-treeDisplay.nodeLocsParameters[totalHeight]) + buffer), root, 0, root);
						if (showScale.getValue())
							drawGrid(treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.fixedDepthScale, treeDisplay.nodeLocsParameters[scaling], tree, drawnRoot, treeDisplay, g);
					}

					treeDrawing.y[subRoot] = (treeDrawing.y[root])-(int)(getNonZeroBranchLength(tree, root)*treeDisplay.nodeLocsParameters[scaling]);
					treeDrawing.x[subRoot] = (treeDrawing.x[root])-(int)(getNonZeroBranchLength(tree, root)*treeDisplay.nodeLocsParameters[scaling]);
				}
				else {
					if (even.getValue()){
						int evenVertSpacing =(int)((-treeDrawing.x[subRoot] +edgeNode(treeDrawing, tree, root, true, true))/ (tree.mostStepsAboveNode(root) + 1));
						if (evenVertSpacing > 0)
							RIGHTevenNodeLocs(treeDrawing, tree, root, evenVertSpacing);
					}
					if (!inhibitStretch.getValue() && treeDisplay.autoStretchIfNeeded) { //&& treeDrawing.x[subRoot]>0
						treeDisplay.nodeLocsParameters[stretchfactor]=((double)(rect.width-treeDisplay.getTipsMargin())) / (rect.width - treeDrawing.x[subRoot] -treeDisplay.getTipsMargin());
						RIGHTstretchNodeLocs(treeDisplay,treeDrawing, tree, root,rect.width-treeDisplay.getTipsMargin());
						treeDrawing.x[subRoot]=5;
					}
				}
			}
			else if (treeDisplay.getOrientation()==TreeDisplay.LEFT) {
				int numTerms = effectiveNumberOfTerminals(tree, root);
				if (numTerms == 0)
					numTerms = 1;
				if (fixedTaxonDistance!=0 && MesquiteInteger.isCombinable(fixedTaxonDistance))
					treeDisplay.setTaxonSpacing(fixedTaxonDistance);
				else
					treeDisplay.setTaxonSpacing( (rect.height - 30) / numTerms);
				if (numTerms*treeDisplay.getTaxonSpacing()>.95*rect.height && treeDisplay.getTaxonSpacing()/2*2 != treeDisplay.getTaxonSpacing())  //if odd
					treeDisplay.setTaxonSpacing(treeDisplay.getTaxonSpacing()-1);
				lastleft = -treeDisplay.getTaxonSpacing()/3*2;
				LEFTCalcTerminalLocs(treeDisplay, treeDrawing, tree, root,treeDisplay.getTipsMargin(), tree.getAssociatedBit(triangleNameRef, root), tree.numberOfTerminalsInClade(root), root);
				LEFTCalcInternalLocs(treeDrawing, tree, root);
				if (center.getValue())
					RIGHTLEFTCenterInternalLocs(treeDrawing, tree, root);
				//AdjustForUnbranchedNodes(root, subRoot);
				treeDrawing.y[subRoot] = (treeDrawing.y[root])+effectiveROOTSIZE;
				treeDrawing.x[subRoot] = (treeDrawing.x[root])+effectiveROOTSIZE;
				placeSingletons(treeDrawing, tree, root);
				if (treeDisplay.showBranchLengths) {
					treeDisplay.nodeLocsParameters[totalHeight]=tree.tallestPathAboveNode(root, 1.0);
					if (!treeDisplay.fixedScalingOn) {
						treeDisplay.fixedDepthScale = treeDisplay.nodeLocsParameters[totalHeight];
						fixedDepth = treeDisplay.fixedDepthScale;
						if (treeDisplay.nodeLocsParameters[totalHeight]==0)
							treeDisplay.nodeLocsParameters[scaling]=1;
						else
							treeDisplay.nodeLocsParameters[scaling]=((double)(rect.width-treeDisplay.getTipsMargin()-buffer - effectiveROOTSIZE))/(treeDisplay.nodeLocsParameters[totalHeight]); 
						LEFTdoAdjustLengths(treeDisplay, treeDrawing, tree, rect.width - effectiveROOTSIZE -buffer, root, 0, root);
						if (showScale.getValue())
							drawGrid(treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.nodeLocsParameters[scaling], tree, drawnRoot, treeDisplay, g);
					}
					else {
						if (treeDisplay.fixedDepthScale == 0)
							treeDisplay.fixedDepthScale = 1;
						treeDisplay.nodeLocsParameters[scaling]=((double)(rect.width-treeDisplay.getTipsMargin()-buffer - effectiveROOTSIZE))/(treeDisplay.fixedDepthScale); 
						LEFTdoAdjustLengths(treeDisplay, treeDrawing, tree, rect.width - effectiveROOTSIZE-(int)(treeDisplay.nodeLocsParameters[scaling]*(treeDisplay.fixedDepthScale-treeDisplay.nodeLocsParameters[totalHeight])+buffer), root, 0, root);
						if (showScale.getValue())
							drawGrid(treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.fixedDepthScale, treeDisplay.nodeLocsParameters[scaling], tree, drawnRoot, treeDisplay, g);
					}

					treeDrawing.y[subRoot] = (treeDrawing.y[root])+(int)(getNonZeroBranchLength(tree, root)*treeDisplay.nodeLocsParameters[scaling]);
					treeDrawing.x[subRoot] = (treeDrawing.x[root])+(int)(getNonZeroBranchLength(tree, root)*treeDisplay.nodeLocsParameters[scaling]);
				}
				else {
					if (even.getValue()){
						int evenVertSpacing =(int)((treeDrawing.x[subRoot] - edgeNode(treeDrawing, tree, root, true, false))/ (tree.mostStepsAboveNode(root) + 1));
						if (evenVertSpacing > 0)
							LEFTevenNodeLocs(treeDrawing, tree, root, evenVertSpacing);
					}
					if (!inhibitStretch.getValue() && (treeDisplay.autoStretchIfNeeded )) {  //&& treeDrawing.x[subRoot]>rect.width
						treeDisplay.nodeLocsParameters[stretchfactor]=((double)(rect.width-treeDisplay.getTipsMargin())) / (treeDrawing.x[subRoot] - (int)treeDisplay.getTipsMargin());
						LEFTstretchNodeLocs(treeDisplay, treeDrawing, tree, root);
						treeDrawing.x[subRoot]=rect.width-5;
					}
				}
			}
			treeDisplay.scaling=treeDisplay.nodeLocsParameters[scaling];
		}

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
	/*.................................................................................................................*/
	public void drawGrid(double totalTreeHeight, double totalScaleHeight, double scaling, Tree tree, int drawnRoot, TreeDisplay treeDisplay, Graphics g) {
		if (g == null)
			return;
		boolean narrowScaleOnly = !broadScale.getValue();
		boolean rulerOnly = false;
		int rulerWidth = 8;
		Color c=g.getColor();
		//Debugg.println("Scalecolor is " + scaleColor);
		g.setColor(scaleColor);
		int scaleBuffer = 28;
		TreeDrawing treeDrawing = treeDisplay.getTreeDrawing();
		int buffer = 8;
		double log10 = Math.log(10.0);
		double hundredthHeight = Math.exp(log10* ((int) (Math.log(totalScaleHeight)/log10)-1));
		if (totalScaleHeight/hundredthHeight <20.0)
			hundredthHeight /= 10.0;
		int countTenths = 0;
		double thisHeight = totalScaleHeight + hundredthHeight;
		if (treeDisplay.getOrientation()==TreeDisplay.UP) {
			double base = (totalScaleHeight-totalTreeHeight)*scaling +treeDisplay.getTreeDrawing().y[drawnRoot];
			int leftEdge = treeDisplay.getTreeDrawing().x[tree.leftmostTerminalOfNode(drawnRoot)];

			int rightEdge = treeDisplay.getTreeDrawing().x[tree.rightmostTerminalOfNode(drawnRoot)]+ scaleBuffer;
			if (narrowScaleOnly)
				leftEdge = rightEdge - 10;
			while ( thisHeight>=0) {
				if (countTenths % 10 == 0)
					g.setColor(scaleCounterColor);
				else
					g.setColor(scaleColor);
				//Debugg.println("Scalecolor is " + scaleColor);
				thisHeight -= hundredthHeight;
				if (rulerOnly)
					g.drawLine(rightEdge-rulerWidth, (int)(base- (thisHeight*scaling)), rightEdge,  (int)(base- (thisHeight*scaling)));
				else
					g.drawLine(leftEdge, (int)(base- (thisHeight*scaling)), rightEdge,  (int)(base- (thisHeight*scaling)));
				if (countTenths % 10 == 0)
					drawString(g, MesquiteDouble.toStringInRange(totalScaleHeight - thisHeight, totalScaleHeight), rightEdge + buffer, (int)(base- (thisHeight*scaling)));

				countTenths ++;
			}
			if (rulerOnly)
				g.drawLine(rightEdge, (int)(base), rightEdge,  (int)(base- (totalScaleHeight*scaling)));
		}
		else if (treeDisplay.getOrientation()==TreeDisplay.DOWN) {
			int leftEdge = treeDisplay.getTreeDrawing().x[tree.leftmostTerminalOfNode(drawnRoot)];
			int rightEdge = treeDisplay.getTreeDrawing().x[tree.rightmostTerminalOfNode(drawnRoot)]+ scaleBuffer;
			if (narrowScaleOnly)
				leftEdge = rightEdge - 10;
			double base = treeDrawing.y[drawnRoot];
			if (fixedScale)
				base += (totalTreeHeight - fixedDepth)*scaling;
			while ( thisHeight>=0) {
				if (countTenths % 10 == 0)
					g.setColor(scaleCounterColor);
				else
					g.setColor(scaleColor);
				//Debugg.println("Scalecolor is " + scaleColor);
				thisHeight -= hundredthHeight;
				if (rulerOnly)
					g.drawLine(rightEdge-rulerWidth, (int)(base+ (thisHeight*scaling)), rightEdge,  (int)(base+ (thisHeight*scaling)));
				else
					g.drawLine(leftEdge, (int)(base+ (thisHeight*scaling)), rightEdge,  (int)(base+ (thisHeight*scaling)));
				if (countTenths % 10 == 0)
					drawString(g, MesquiteDouble.toStringInRange(totalScaleHeight - thisHeight, totalScaleHeight), rightEdge + buffer, (int)(base+ (thisHeight*scaling)));
				countTenths ++;
			}
			if (rulerOnly)
				g.drawLine(rightEdge, (int)(base), rightEdge,  (int)(base+ (totalScaleHeight*scaling)));
		}
		else if (treeDisplay.getOrientation()==TreeDisplay.LEFT) {
			fm=g.getFontMetrics(g.getFont());
			int textHeight = fm.getHeight();
			int leftEdge = treeDisplay.getTreeDrawing().y[tree.leftmostTerminalOfNode(drawnRoot)];
			int rightEdge = treeDisplay.getTreeDrawing().y[tree.rightmostTerminalOfNode(drawnRoot)]+ scaleBuffer;
			if (narrowScaleOnly)
				leftEdge = rightEdge - 10;

			//if fixed then base is centered on root!
			double base = (totalScaleHeight-totalTreeHeight)*scaling +treeDisplay.getTreeDrawing().x[drawnRoot];
			while ( thisHeight>=0) {
				if (countTenths % 10 == 0)
					g.setColor(scaleCounterColor);
				else
					g.setColor(scaleColor);
				//Debugg.println("Scalecolor is " + scaleColor);
				thisHeight -= hundredthHeight;
				if (rulerOnly)
					g.drawLine((int)(base- (thisHeight*scaling)), rightEdge,  (int)(base- (thisHeight*scaling)),  rightEdge-rulerWidth);
				else
					g.drawLine((int)(base- (thisHeight*scaling)), rightEdge,  (int)(base- (thisHeight*scaling)),  leftEdge);
				if (countTenths % 10 == 0)
					drawString(g, MesquiteDouble.toStringInRange(totalScaleHeight - thisHeight, totalScaleHeight), (int)(base- (thisHeight*scaling)), rightEdge + buffer + textHeight);
				countTenths ++;
			}
			if (rulerOnly)
				g.drawLine((int)(base), rightEdge, (int)(base- (totalScaleHeight*scaling)),rightEdge);
		}
		else if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
			fm=g.getFontMetrics(g.getFont());
			int textHeight = fm.getHeight();
			int leftEdge = treeDisplay.getTreeDrawing().y[tree.leftmostTerminalOfNode(drawnRoot)];
			int rightEdge = treeDisplay.getTreeDrawing().y[tree.rightmostTerminalOfNode(drawnRoot)] + scaleBuffer;
			if (narrowScaleOnly) {
				leftEdge = rightEdge - 10;
			}
			double base = treeDrawing.x[drawnRoot];
			if (fixedScale)
				base += (totalTreeHeight - fixedDepth)*scaling;
			while ( thisHeight>=0) {
				if (countTenths % 10 == 0)
					g.setColor(scaleCounterColor);
				else
					g.setColor(scaleColor);
				//Debugg.println("Scalecolor is " + scaleColor);
				thisHeight -= hundredthHeight;
				if (rulerOnly)
					g.drawLine((int)(base+ (thisHeight*scaling)), rightEdge-rulerWidth,  (int)(base+ (thisHeight*scaling)),  rightEdge);
				else
					g.drawLine((int)(base+ (thisHeight*scaling)), leftEdge,  (int)(base+ (thisHeight*scaling)),  rightEdge);
				if (countTenths % 10 == 0)
					drawString(g, MesquiteDouble.toStringInRange(totalScaleHeight - thisHeight, totalScaleHeight), (int)(base+ (thisHeight*scaling)), rightEdge + buffer + textHeight);
				countTenths ++;
			}
			if (rulerOnly)
				g.drawLine((int)(base), rightEdge, (int)(base+ (totalScaleHeight*scaling)),rightEdge);
		}
		if (c !=null)
			g.setColor(c);
		g.setPaintMode();
	}

	public int getFixedTaxonDistance() {
		return fixedTaxonDistance;
	}

	public void setFixedTaxonDistance(int fixedTaxonDistance) {
		this.fixedTaxonDistance = fixedTaxonDistance;
	}
}


class NodeLocsExtra extends TreeDisplayBkgdExtra implements Commandable {
	NodeLocsStandard locsModule;
	TreeTool stretchTool;
	MesquiteWindow window = null;
	public NodeLocsExtra (NodeLocsStandard ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		locsModule = ownerModule;
		/*
		stretchTool = new TreeTool(this,  "cladeexpander", locsModule.getPath() , "expand.gif", 8,0,"Stretch clade", "This tool stretches larger a clade.");
		stretchTool.setTouchedCommand(MesquiteModule.makeCommand("touchedCladeStretch",  this));
		stretchTool.setDroppedCommand(MesquiteModule.makeCommand("droppedCladeStretch",  this));
		MesquiteCommand dragCommand = MesquiteModule.makeCommand("draggedCladeStretch",  this);
		stretchTool.setDraggedCommand(dragCommand);
		stretchTool.setTouchedFieldCommand(MesquiteModule.makeCommand("fieldCladeStretch",  this));
		dragCommand.setDontDuplicate(true);
		if (locsModule.containerOfModule() instanceof MesquiteWindow) {
			window = ((MesquiteWindow)locsModule.containerOfModule());
			window.addTool(stretchTool);
		}
		*/
	}
	/*.................................................................................................................*/
	public   String writeOnTree(Tree tree, int drawnRoot) {
		return null;
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		if (locsModule.showScale.getValue() && locsModule.showBranchLengths.getValue())
			locsModule.drawGrid(treeDisplay.nodeLocsParameters[locsModule.totalHeight], treeDisplay.fixedDepthScale, treeDisplay.nodeLocsParameters[locsModule.scaling], tree, drawnRoot, treeDisplay, g);
	}
	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
	}
	/**to inform TreeDisplayExtra that cursor has just entered branch N*/
	public void cursorEnterBranch(Tree tree, int N, Graphics g){
		if (window == null || window.getCurrentTool() != stretchTool)
			return;
		drawCladeBox(N);
			lastBranch = N;
			lineOnMove = true;
	}
	/**to inform TreeDisplayExtra that cursor has just entered branch N*/
	public void cursorExitBranch(Tree tree, int N, Graphics g){
		if (window == null || window.getCurrentTool() != stretchTool)
			return;
			if (lineOnMove)
				drawCladeBox(N);
			lastBranch = -1;
			lineOnMove = false;
	}
	int findBranch(Tree tree, int x, int y){
		TreeDrawing drawing = treeDisplay.getTreeDrawing(); 
		int drawnRoot = drawing.getDrawnRoot();
		for (int ky = y; ky>= 0; ky--){
			int b = drawing.findBranch(tree, drawnRoot, x, ky, null);
			if (b>0)
				return b;
		}
		return -1;
	}
	int lastBranch = -1;
	/**to inform TreeDisplayExtra that cursor has just moved OUTSIDE of taxa or branches*/
	public void cursorMove(Tree tree, int x, int y, Graphics g){
		if (window == null || window.getCurrentTool() != stretchTool)
			return;
		if (lineOnMove && lastBranch>0)
			drawCladeBox(lastBranch);
		lineOnMove = false;
	}
	int originalX, originalY, lastX, lastY;
	boolean lineOnTouch = false;
	boolean lineOnMove = false;
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		MesquiteTree t=null; //
		if (treeDisplay!=null) {
			Tree trt = treeDisplay.getTree();
			if (trt instanceof MesquiteTree)
				t = (MesquiteTree)trt;
			else
				t = null;
		}
		if (window == null || window.getCurrentTool() != stretchTool)
			return null;
		if (checker.compare(this.getClass(), "Touch on branch to stretch it", "[branch number] [x coordinate touched] [y coordinate touched] [modifiers]", commandName, "touchedCladeStretch")) {
			if (t==null)
				return null;
			MesquiteInteger io = new MesquiteInteger(0);
			int node= MesquiteInteger.fromString(arguments, io);
			int x= MesquiteInteger.fromString(arguments, io);
			int y= MesquiteInteger.fromString(arguments, io);
			String mod= ParseUtil.getRemaining(arguments, io);
			stretchTouched(node, x, y, mod);

		}
		else if (checker.compare(this.getClass(),  "Drop branch being stretched.", "[branch number] [x coordinate dropped] [y coordinate dropped] ", commandName, "droppedCladeStretch")) {
			if (t==null)
				return null;
			MesquiteInteger io = new MesquiteInteger(0);
			int node= MesquiteInteger.fromString(arguments, io);
			int x= MesquiteInteger.fromString(arguments, io);
			int y= MesquiteInteger.fromString(arguments, io);
			if (lineOnTouch) {
				stretchDropped(t, node, x, y);
			}
		}
		else if (checker.compare(this.getClass(), "Drag branch being stretched.", "[branch number] [current x coordinate] [current y coordinate] ", commandName, "draggedCladeStretch")) {
			if (t==null)
				return null;
			MesquiteInteger io = new MesquiteInteger(0);
			int node= MesquiteInteger.fromString(arguments, io);
			int x= MesquiteInteger.fromString(arguments, io);
			int y= MesquiteInteger.fromString(arguments, io);
			if (lineOnTouch) {
				stretchDragged(t, node, x, y);
			}
		}
		else if (checker.compare(this.getClass(), "Touched Field.", "[current x coordinate] [current y coordinate] ", commandName, "fieldCladeStretch")) {
			if (t==null)
				return null;
			locsModule.setZoom(treeDisplay,-1, 1.0);
		}
		return null;
	}
	int cladeTop = -1;
	int cladeBottom = -1;
	int cladeLeft = -1;
	int cladeRight = -1;
	int tempHeight = -1;
	int tempLeft = -1;
	int tempWidth = -1;
	int touchX = -1;
	int touchY = -1;

	void drawCladeBox(int node){
		TreeDrawing drawing =  treeDisplay.getTreeDrawing();
		Tree tree = treeDisplay.getTree();
		cladeTop = tallestNode(tree, drawing.getDrawnRoot(), drawing.x, drawing.y);
		cladeBottom = drawing.y[node];
		cladeLeft = drawing.x[tree.leftmostTerminalOfNode(node)];
		cladeRight = drawing.x[tree.rightmostTerminalOfNode(node)];
		tempHeight = cladeBottom - cladeTop;
		tempLeft = cladeLeft;
		tempWidth = cladeRight - cladeLeft;
		if (GraphicsUtil.useXORMode(null, false)){
			Graphics g = treeDisplay.getGraphics();
			g.setXORMode(Color.white);
			g.setColor(Color.red);
			g.drawRect(tempLeft, cladeTop, tempWidth, tempHeight);
			g.dispose();
		}
	}
	int tallestNode(Tree tree, int node, int[] x, int[] y){
		if (tree.nodeIsTerminal(node))
			return y[node];
		int tallest = MesquiteInteger.unassigned;
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
			int height = tallestNode(tree, d, x, y);
			if (tallest == MesquiteInteger.unassigned || height < tallest)
				tallest = height;
		}
		return tallest;
	}
	void stretchTouched(int node, int x, int y, String mod) {
		MesquiteTree tree=null; //
		if (treeDisplay!=null) {
			Tree trt = treeDisplay.getTree();
			if (trt instanceof MesquiteTree)
				tree = (MesquiteTree)trt;
		}
		if (tree == null)
			return;
		touchX = x;
		touchY = y;
		TreeDrawing drawing =  treeDisplay.getTreeDrawing();
		if (GraphicsUtil.useXORMode(null, false)){
			Graphics g = treeDisplay.getGraphics();
			g.setXORMode(Color.white);
			g.setColor(Color.red);
			if (lineOnTouch)
				g.drawRect(tempLeft, cladeTop, tempWidth, tempHeight);
			cladeTop = tallestNode(tree, drawing.getDrawnRoot(), drawing.x, drawing.y);
			cladeBottom = drawing.y[node];
			cladeLeft = drawing.x[tree.leftmostTerminalOfNode(node)];
			cladeRight = drawing.x[tree.rightmostTerminalOfNode(node)];
			tempHeight = cladeBottom - cladeTop;
			tempLeft = cladeLeft;
			tempWidth = cladeRight - cladeLeft;
			tempZoomFactor = -1;
				
			g.drawRect(tempLeft, cladeTop, tempWidth, tempHeight);
			g.dispose();
		}
		lineOnTouch=true;
	}


	void stretchDragged(Tree t, int node, int x, int y){
		if (y== touchY || !lineOnTouch)
			return;
		MesquiteTree tree=null; //
		if (treeDisplay!=null) {
			Tree trt = treeDisplay.getTree();
			if (trt instanceof MesquiteTree)
				tree = (MesquiteTree)trt;
		}
		if (tree == null)
			return;

		if (GraphicsUtil.useXORMode(null, false)){
			Graphics g = treeDisplay.getGraphics();
			g.setXORMode(Color.white);
			g.setColor(Color.red);
			int oldHeight =(cladeBottom - cladeTop);
			double expansionFactor = (oldHeight + (y - touchY))*1.0/oldHeight;
			if (setTempZoom(treeDisplay, tree, node, expansionFactor)){
			//	locsModule.setZoom(treeDisplay, node, expansionFactor);
				g.drawRect(tempLeft, cladeTop, tempWidth, tempHeight);
				tempHeight = (int)(oldHeight * expansionFactor);
				int newWidth = (int)((cladeRight-cladeLeft) * expansionFactor);
				tempLeft = cladeLeft - (newWidth-(cladeRight-cladeLeft))/2;
				tempWidth = newWidth;
				g.drawRect(tempLeft, cladeTop, tempWidth, tempHeight);
				lineOnTouch = true;
			}
			g.dispose();
		}
	}
	void stretchDropped(MesquiteTree t, int node, int x, int y){
		if (y== touchY || !lineOnTouch)
			return;

		MesquiteTree tree=null; //
		if (treeDisplay!=null) {
			Tree trt = treeDisplay.getTree();
			if (trt instanceof MesquiteTree)
				tree = (MesquiteTree)trt;
		}
		if (tree == null)
			return;

		if (GraphicsUtil.useXORMode(null, false)){
			Graphics g = treeDisplay.getGraphics();
			g.setXORMode(Color.white);
			g.setColor(Color.red);
			g.drawRect(tempLeft, cladeTop, tempWidth, tempHeight);
			g.dispose();
		}

		if (tempZoomFactor>=0){
			locsModule.setZoom(treeDisplay,node, tempZoomFactor);
		}
		lineOnTouch = false;
	}

	double tempZoomFactor = 1.0;
	/*.................................................................................................................*/
	boolean setTempZoom(TreeDisplay treeDisplay, Tree tree, int zoomNode, double proposedChange){
		int baseSpacing =treeDisplay.getTaxonSpacing();
		int inZoom = tree.numberOfTerminalsInClade(zoomNode);
		int outZoom = tree.numberOfTerminalsInClade(treeDisplay.getTreeDrawing().getDrawnRoot()) - inZoom;
		double totalFactor = proposedChange;
		if (locsModule.zoomNode == zoomNode || tree.descendantOf(zoomNode, locsModule.zoomNode))
			totalFactor= locsModule.zoomFactor * proposedChange;
		if ( (int)(baseSpacing *totalFactor)>0 && (int)(baseSpacing * (1 - (totalFactor-1)*inZoom/outZoom))>0){
			tempZoomFactor = totalFactor;
			return true;
		}
		return false;
	}
	/*.................................................................................................................*
	boolean setTempZoom(TreeDisplay treeDisplay, Tree tree, int zoomNode, double proposedChange){
		int baseSpacing =treeDisplay.getTaxonSpacing();
		int inZoom = tree.numberOfTerminalsInClade(zoomNode);
		int outZoom = tree.numberOfTerminalsInClade(treeDisplay.getTreeDrawing().getDrawnRoot()) - inZoom;
		double totalFactor = proposedChange;
		if (locsModule.zoomNode == zoomNode || tree.descendantOf(zoomNode, locsModule.zoomNode))
			totalFactor= locsModule.zoomFactor * proposedChange;
		if ( (int)(baseSpacing *totalFactor)>0 && (int)(baseSpacing * (1 - (totalFactor-1)*inZoom/outZoom))>0){
			tempZoomFactor = totalFactor;
			return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	public void turnOff(){
		super.turnOff();
		if (window != null)
			window.removeTool(stretchTool);
	}
}



