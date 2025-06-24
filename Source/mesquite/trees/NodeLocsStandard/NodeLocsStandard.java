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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.Vector;

import mesquite.lib.CommandChecker;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteThread;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.NameReference;
import mesquite.lib.Notification;
import mesquite.lib.Parser;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.duties.DrawNamesTreeDisplay;
import mesquite.lib.duties.DrawTree;
import mesquite.lib.duties.NodeLocsVH;
import mesquite.lib.duties.TreeWindowMaker;
import mesquite.lib.tree.DiagonalRootDrawer;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDisplayBkgdExtra;
import mesquite.lib.tree.TreeDisplayExtra;
import mesquite.lib.tree.TreeDisplayHolder;
import mesquite.lib.tree.TreeDisplayRequests;
import mesquite.lib.tree.TreeDrawing;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.GraphicsUtil;
import mesquite.lib.ui.MesquiteMenuItemSpec;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.lib.ui.MesquiteWindow;

/** Calculates node locations for tree drawing in a standard vertical/horizontal position, as used by DiagonalDrawTree and SquareTree (for example).*/
public class NodeLocsStandard extends NodeLocsVH {

	//	static int lastLengthsDisplayMode = TreeDisplay.AUTOSHOWLENGTHS;
	//	static int lastOrientation = NodeLocsVH.defaultOrientation;
	Vector extras;
	double fixedDepth = 1;
	boolean leaveScaleAlone = true;
	boolean fixedScale = false;
	MesquiteBoolean inhibitStretch;
	MesquiteBoolean showScale;
	MesquiteBoolean broadScale;
	double taxonSqueeze = 1.0;

	MesquiteInteger branchLengthsDisplayMode;  

	boolean resetShowBranchLengths = false;
	double fixedTaxonDistance = 0;

	static final int totalHeight = 0;
	static final int stretchfactor = 1;
	static final int  scaling = 2;
	static int minRootwardBorder = 60;
	static int minPortBorder = 10;
	static int minStarboardBorder = 10;
	static int minTipwardBorder = 10;

	//	double namesAngle = MesquiteDouble.unassigned;

	MesquiteMenuItemSpec fixedScalingMenuItem, showScaleMenuItem, broadScaleMenuItem;
	MesquiteMenuItemSpec stretchMenuItem, evenMenuItem, squeezeMenuItem;

	MesquiteBoolean center;
	boolean[] fixedSettings = null;
	MesquiteBoolean even;
	//	MesquiteString orientationName;
	MesquiteBoolean upOn, downOn, rightOn, leftOn, autoOn, ultraOn, blOn;

	//ORIENTATIONS
	int ornt = TreeDisplay.RIGHT;

	/*.................................................................................................................*/

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		if (condition instanceof boolean[]){
			fixedSettings = (boolean[])condition;
		}
		upOn = new MesquiteBoolean(false);
		downOn = new MesquiteBoolean(false);
		rightOn = new MesquiteBoolean(false);
		leftOn = new MesquiteBoolean(false);
		extras = new Vector();
		inhibitStretch = new MesquiteBoolean(false);
		center = new MesquiteBoolean(false);
		even = new MesquiteBoolean(false);
		if (getEmployer()!=null && !(getEmployer() instanceof DiagonalRootDrawer)){ //a bit non-standard but a helpful service to use different defaults for square
			even.setValue(true);
			center.setValue(true);
		}

		branchLengthsDisplayMode = new MesquiteInteger(TreeDisplay.AUTOSHOWLENGTHS);
		int d = recoverLastLengthsDisplayMode();
		if (d != TreeDisplay.INVALIDMODE)
			branchLengthsDisplayMode.setValue(d);
		showScale = new MesquiteBoolean(true);
		broadScale = new MesquiteBoolean(false);

		//really, this should all have been in node locs, but too busy to fix (also in other DrawTree modules that use NodeLocsVH)
		if (employerAllowsReorientation()) {
			int or = recoverLastOrientation();
			if (or != TreeDisplay.INVALIDORIENTATION && or != TreeDisplay.NOTYETSET)
				ornt = or;  
			MesquiteSubmenuSpec orientationSubmenu = addSubmenu(null, "Orientation");
			addCheckMenuItemToSubmenu(null, orientationSubmenu, "Up", makeCommand("orientUp",  this), upOn = new MesquiteBoolean(ornt == TreeDisplay.UP));
			addCheckMenuItemToSubmenu(null, orientationSubmenu, "Right", makeCommand("orientRight",  this), rightOn = new MesquiteBoolean(ornt == TreeDisplay.RIGHT));
			addCheckMenuItemToSubmenu(null, orientationSubmenu, "Down", makeCommand("orientDown",  this), downOn = new MesquiteBoolean(ornt == TreeDisplay.DOWN));
			addCheckMenuItemToSubmenu(null, orientationSubmenu, "Left", makeCommand("orientLeft",  this), leftOn = new MesquiteBoolean(ornt == TreeDisplay.LEFT));
			addItemToSubmenu(null, orientationSubmenu, "-", null);
			MesquiteMenuItemSpec mRC = addItemToSubmenu(null, orientationSubmenu, "Rotate Clockwise", makeCommand("rotateClockwise", this));
			mRC.setShortcut(KeyEvent.VK_R);
			MesquiteMenuItemSpec mLC = addItemToSubmenu(null, orientationSubmenu, "Rotate Counterclockwise", makeCommand("rotateCounterClockwise", this));
			mLC.setShortcut(KeyEvent.VK_L);
			addItemToSubmenu(null, orientationSubmenu, "-", null);
			addItemToSubmenu(null, orientationSubmenu, "Set Current Orientation as Default", makeCommand("setDefaultOrientation",  this));
		}


		if (fixedSettings != null && fixedSettings.length>0 && fixedSettings[0]){
			branchLengthsDisplayMode.setValue(TreeDisplay.AUTOSHOWLENGTHS);
		}
		else {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Branch Length Display");
			addCheckMenuItemToSubmenu(null, mss, "Automatic", new MesquiteCommand("branchLengthsDisplay", "" + TreeDisplay.AUTOSHOWLENGTHS, this), autoOn = new MesquiteBoolean(branchLengthsDisplayMode.getValue() == TreeDisplay.AUTOSHOWLENGTHS));
			addCheckMenuItemToSubmenu(null, mss, "Draw as Ultrametric (No branch lengths implied)", new MesquiteCommand("branchLengthsDisplay", "" + TreeDisplay.DRAWULTRAMETRIC, this), ultraOn = new MesquiteBoolean(branchLengthsDisplayMode.getValue() == TreeDisplay.DRAWULTRAMETRIC));
			addCheckMenuItemToSubmenu(null, mss, "Draw with Lengths, Unassigned as One", new MesquiteCommand("branchLengthsDisplay", "" + TreeDisplay.DRAWUNASSIGNEDASONE, this), blOn = new MesquiteBoolean(branchLengthsDisplayMode.getValue() == TreeDisplay.DRAWUNASSIGNEDASONE));
		}

		fixedScalingMenuItem = addMenuItem( "Fixed Scaling...", makeCommand("setFixedScaling", this));
		showScaleMenuItem = addCheckMenuItem(null, "Show scale", makeCommand("toggleScale", this), showScale);
		broadScaleMenuItem = addCheckMenuItem(null, "Broad scale", makeCommand("toggleBroadScale", this), broadScale);
		stretchMenuItem = addCheckMenuItem(null, "Inhibit Stretch Tree to Fit", makeCommand("inhibitStretchToggle", this), inhibitStretch);
		evenMenuItem = addCheckMenuItem(null, "Even root to tip spacing", makeCommand("toggleEven", this), even);
		if (branchLengthsDisplayMode.getValue()==TreeDisplay.DRAWUNASSIGNEDASONE || branchLengthsDisplayMode.getValue()==TreeDisplay.AUTOSHOWLENGTHS) {
			stretchMenuItem.setEnabled(false);
			evenMenuItem.setEnabled(false);
			if (fixedScale) {
				fixedScalingMenuItem.setName("Fixed Scaling Off");
				fixedScalingMenuItem.setCommand(makeCommand("offFixedScaling", this));
			}
			/*fixedScalingMenuItem = addMenuItem( "Fixed Scaling...", makeCommand("setFixedScaling", this));
			showScaleMenuItem = addCheckMenuItem(null, "Show scale", makeCommand("toggleScale", this), showScale);
			broadScaleMenuItem = addCheckMenuItem(null, "Broad scale", makeCommand("toggleBroadScale", this), broadScale);*/
			resetShowBranchLengths=true;
		}
		else {
			fixedScalingMenuItem.setEnabled(false);
			showScaleMenuItem.setEnabled(false);
			broadScaleMenuItem.setEnabled(false);

			/*Debugg.println: if ultrametric, just add menu and use. If AUTO, add menu but rename as "Inhibit stretch if no BL".
			 * 	if (showBranchLengths.getValue()==TreeDisplay.DRAWULTRAMETRIC || showBranchLengths.getValue()==TreeDisplay.AUTOSHOWLENGTHS)
			 */
			/*stretchMenuItem = addCheckMenuItem(null, "Inhibit Stretch Tree to Fit", makeCommand("inhibitStretchToggle", this), inhibitStretch);
			evenMenuItem = addCheckMenuItem(null, "Even root to tip spacing", makeCommand("toggleEven", this), even);*/
		}


		addMenuItem( "Fixed Distance Between Taxa...", makeCommand("setFixedTaxonDistance",  this));
		squeezeMenuItem = addMenuItem( "Squeeze Taxa...", makeCommand("setTaxonSqueeze",  this));
		squeezeMenuItem.setEnabled(fixedTaxonDistance == 0);
		addCheckMenuItem(null, "Centered Branches", makeCommand("toggleCenter", this), center);

		return true;
	}
	private boolean inBasicTreeWindow(){
		MesquiteModule mb = findEmployerWithDuty(TreeDisplayHolder.class);
		if (mb != null && mb instanceof TreeWindowMaker)
			return true;
		return false;
	}

	private void recordLastLengthsDisplayMode(int bld){
		MesquiteModule mb = findEmployerWithDuty(TreeDisplayHolder.class);
		if (mb != null && mb instanceof TreeWindowMaker){
			((TreeWindowMaker)mb).setPreferredBranchLengthsDisplay(bld);
		}
	}
	private int recoverLastLengthsDisplayMode(){
		MesquiteModule mb = findEmployerWithDuty(TreeDisplayHolder.class);
		if (mb != null && mb instanceof TreeWindowMaker){
			return ((TreeWindowMaker)mb).getPreferredBranchLengthsDisplay();

		}
		return TreeDisplay.INVALIDMODE;
	}
	private void recordLastOrientation(int orient){
		MesquiteModule mb = findEmployerWithDuty(TreeDisplayHolder.class);
		if (mb != null && mb instanceof TreeWindowMaker){
			((TreeWindowMaker)mb).setPreferredOrientationForNewDisplay(orient);
		}
	}
	private int recoverLastOrientation(){
		MesquiteModule mb = findEmployerWithDuty(TreeDisplayHolder.class);
		if (mb != null && mb instanceof TreeWindowMaker){
			return ((TreeWindowMaker)mb).getPreferredOrientationForNewDisplay();

		}
		return TreeDisplay.INVALIDORIENTATION;
	}
	/*.................................................................................................................*/
	private boolean employerAllowsReorientation(){
		DrawTree dt = (DrawTree)findEmployerWithDuty(DrawTree.class);
		if (dt!= null)
			return dt.allowsReorientation();
		return false;

	}
	public String orient (int orientation){
		if (orientation == TreeDisplay.UP)
			return "Up";
		else if (orientation == TreeDisplay.DOWN)
			return "Down";
		else if (orientation == TreeDisplay.RIGHT)
			return "Right";
		else if (orientation == TreeDisplay.LEFT)
			return "Left";
		else return "other";
	}

	void deleteMostMenuItems(){
		//	deleteMenuItem(stretchMenuItem);
		stretchMenuItem.setEnabled(false);
		//	deleteMenuItem(evenMenuItem);
		evenMenuItem.setEnabled(false);
		//	deleteMenuItem(fixedScalingMenuItem);
		fixedScalingMenuItem.setEnabled(false);
		//	deleteMenuItem(showScaleMenuItem);
		showScaleMenuItem.setEnabled(false);
		//	deleteMenuItem(broadScaleMenuItem);
		broadScaleMenuItem.setEnabled(false);
		//	deleteMenuItem(offFixedScalingMenuItem);
		//	offFixedScalingMenuItem.setEnabled(false);
	}


	public void endJob(){
		storePreferences();
		if (extras!=null) {
			for (int i=0; i<extras.size(); i++){
				TreeDisplayExtra extra = (TreeDisplayExtra)extras.elementAt(i);
				if (extra!=null){
					TreeDisplay td = extra.getTreeDisplay();
					extra.turnOff();
					if (td!=null){
						td.setFixedTaxonSpacing(0);
						td.removeExtra(extra);
					}
				}
			}
			extras.removeAllElements();
		}
		//treeDrawing = null;
		//tree=null;
		//treeDisplay=null;
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
		if (ornt== TreeDisplay.UP)
			temp.addLine("orientUp"); 
		else if (ornt== TreeDisplay.DOWN)
			temp.addLine("orientDown"); 
		else if (ornt== TreeDisplay.LEFT)
			temp.addLine("orientLeft"); 
		else if (ornt== TreeDisplay.RIGHT)
			temp.addLine("orientRight"); 
		temp.addLine("branchLengthsDisplay " + branchLengthsDisplayMode.getValue());
		temp.addLine("toggleScale " + showScale.toOffOnString());
		temp.addLine("toggleBroadScale " + broadScale.toOffOnString());
		temp.addLine("toggleCenter " + center.toOffOnString());
		temp.addLine("toggleEven " + even.toOffOnString());
		temp.addLine("setFixedTaxonDistance " + (int)fixedTaxonDistance); 
		temp.addLine("setTaxonSqueeze " + taxonSqueeze); 

		if (fixedScale)
			temp.addLine("setFixedScaling " + MesquiteDouble.toString(fixedDepth) );
		return temp;
	}

	void setFixedTaxonSpacings(double fixedTaxonDistance){
		if (extras!=null) {
			for (int i=0; i<extras.size(); i++){
				TreeDisplayExtra extra = (TreeDisplayExtra)extras.elementAt(i);
				if (extra!=null){
					TreeDisplay td = extra.getTreeDisplay();
					td.setFixedTaxonSpacing(fixedTaxonDistance);
				}
			}
			extras.removeAllElements();
		}
		if (squeezeMenuItem != null) {
			squeezeMenuItem.setEnabled(fixedTaxonDistance == 0);
			MesquiteTrunk.resetMenuItemEnabling();
		}
	}

	boolean changeInOrientation(){
		if (upOn.getValue() != (ornt == TreeDisplay.UP))
			return true;
		if (downOn.getValue() != (ornt == TreeDisplay.DOWN))
			return true;
		if (rightOn.getValue() != (ornt == TreeDisplay.RIGHT))
			return true;
		if (leftOn.getValue() != (ornt == TreeDisplay.LEFT))
			return true;
		return false;
	}
	void resetMenus(){
		upOn.setValue(ornt == TreeDisplay.UP);
		downOn.setValue(ornt == TreeDisplay.DOWN);
		rightOn.setValue(ornt == TreeDisplay.RIGHT);
		leftOn.setValue(ornt == TreeDisplay.LEFT);
		resetContainingMenuBar();
	}

	void resetOrientation(int orientation){
		ornt = orientation;
		DrawTree dt = (DrawTree)findEmployerWithDuty(DrawTree.class);
		Enumeration e = dt.getDrawings().elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			TreeDrawing treeDrawing = (TreeDrawing)obj;
			treeDrawing.getTreeDisplay().setOrientation(ornt);
			treeDrawing.getTreeDisplay().pleaseUpdate(true);
		}
		if (changeInOrientation()) {
			resetMenus();
			parametersChanged();
		}
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
				newDistance = MesquiteInteger.queryInteger(containerOfModule(), "Set taxon distance", "Distance between taxa:", "(Use a value of 0 to tell Mesquite to calculate the distance itself.)", "", (int)fixedTaxonDistance, 0, 99, true);
			if (newDistance>=0 && newDistance<100 && newDistance!=fixedTaxonDistance) {
				fixedTaxonDistance=newDistance;
				setFixedTaxonSpacings(fixedTaxonDistance);
				if ( !MesquiteThread.isScripting()) parametersChanged(new Notification(TREE_DRAWING_SIZING_CHANGED));
			}

		}
		else if (checker.compare(this.getClass(), "Squeezes the taxa closer", "[multiplier of natural distance]", commandName, "setTaxonSqueeze")){
			Parser parser = new Parser(arguments);
			double newSqueeze= MesquiteDouble.fromString(parser);
			if (fixedTaxonDistance != 0 && MesquiteThread.isScripting()){
				return null;
			}
			if (!MesquiteDouble.isCombinable(newSqueeze) && !MesquiteThread.isScripting())
				newSqueeze = MesquiteDouble.queryDouble(containerOfModule(), "Set multiplier for taxon distances", "Multiplier for taxon distances (e.g., 0.5 squeezes by half):",  taxonSqueeze, 0, 99);
			if (newSqueeze>=0 && newSqueeze<100 && newSqueeze!=fixedTaxonDistance) {
				taxonSqueeze=newSqueeze;
				if ( !MesquiteThread.isScripting()) parametersChanged(new Notification(TREE_DRAWING_SIZING_CHANGED));
			}

		}
		else if (checker.compare(this.getClass(), "Sets whether or not to inhibit automatic stretching the tree to fit the drawing area", "[on =inihibit stretch; off]", commandName, "inhibitStretchToggle")) {
			inhibitStretch.toggleValue(parser.getFirstToken(arguments));
			stretchWasSet = true;
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets default orientation", null, commandName, "setDefaultOrientation")) {
			defaultOrientation = ornt;
			storePreferences();
		}
		else if (checker.compare(this.getClass(), "Here to avoid scripting error; user will need to reset taxon names", null, commandName, "namesAngle")) {

		}
		else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are on top", null, commandName, "orientUp")) {
			resetOrientation(TreeDisplay.UP);
		}
		else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are at the bottom", null, commandName, "orientDown")) {
			resetOrientation(TreeDisplay.DOWN);
		}
		else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are at right", null, commandName, "orientRight")) {
			resetOrientation(TreeDisplay.RIGHT);
		}
		else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are at left", null, commandName, "orientLeft")) {
			resetOrientation(TreeDisplay.LEFT);
		}
		else if (checker.compare(this.getClass(), "Rotates the tree drawing clockwise", null, commandName, "rotateClockwise")) {
			if (ornt == TreeDisplay.UP)
				ornt = TreeDisplay.RIGHT;
			else if (ornt == TreeDisplay.RIGHT)
				ornt = TreeDisplay.DOWN;
			else if (ornt == TreeDisplay.DOWN)
				ornt = TreeDisplay.LEFT;
			else if (ornt == TreeDisplay.LEFT)
				ornt = TreeDisplay.UP;

			resetOrientation(ornt);
		}
		else if (checker.compare(this.getClass(), "Rotates the tree drawing clockwise", null, commandName, "rotateCounterClockwise")) {
			if (ornt == TreeDisplay.UP)
				ornt = TreeDisplay.LEFT;
			else if (ornt == TreeDisplay.LEFT)
				ornt = TreeDisplay.DOWN;
			else if (ornt == TreeDisplay.DOWN)
				ornt = TreeDisplay.RIGHT;
			else if (ornt == TreeDisplay.RIGHT)
				ornt = TreeDisplay.UP;

			resetOrientation(ornt);
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to center the nodes between the immediate descendents, or the terminal in the clade", "[on = center over immediate; off]", commandName, "toggleCenter")) {
			center.toggleValue(parser.getFirstToken(arguments));
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to space the nodes evenly from root to tips", "[on = space evenly; off]", commandName, "toggleEven")) {
			even.toggleValue(parser.getFirstToken(arguments));
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "[no longer available; here to prevent warning given as old scripts are read]", "[]", commandName, "namesAngle")) {
		}
		else if (checker.compare(this.getClass(), "Sets whether or not the branches are to be shown proportional to their lengths", "[on = proportional; off]", commandName, "branchLengthsToggle")) { //old; no longer used but may be in old scripts
			if (fixedSettings != null && fixedSettings.length>0 && fixedSettings[0])
				return null;
			if (arguments == null) //reading old scripts only
				return null;

			MesquiteBoolean mbool = new MesquiteBoolean();
			mbool.toggleValue(parser.getFirstToken(arguments));
			if (mbool.getValue())
				branchLengthsDisplayMode.setValue(TreeDisplay.DRAWUNASSIGNEDASONE);
			else
				branchLengthsDisplayMode.setValue(TreeDisplay.DRAWULTRAMETRIC);

			autoOn.setValue(branchLengthsDisplayMode.getValue() == TreeDisplay.AUTOSHOWLENGTHS);
			ultraOn.setValue(branchLengthsDisplayMode.getValue() == TreeDisplay.DRAWULTRAMETRIC);
			blOn.setValue(branchLengthsDisplayMode.getValue() == TreeDisplay.DRAWUNASSIGNEDASONE);
			//param changed not needed because this is an old script
		}
		else if (checker.compare(this.getClass(), "Sets whether or not the branches are to be shown proportional to their lengths", "[integer for mode]", commandName, "branchLengthsDisplay")) {
			if (fixedSettings != null && fixedSettings.length>0 && fixedSettings[0])
				return null;
			resetShowBranchLengths=true;
			int choice = MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(choice) || choice <0 || choice >2)
				return null;
			branchLengthsDisplayMode.setValue(choice);
			autoOn.setValue(branchLengthsDisplayMode.getValue() == TreeDisplay.AUTOSHOWLENGTHS);
			ultraOn.setValue(branchLengthsDisplayMode.getValue() == TreeDisplay.DRAWULTRAMETRIC);
			blOn.setValue(branchLengthsDisplayMode.getValue() == TreeDisplay.DRAWUNASSIGNEDASONE);
			if (!MesquiteThread.isScripting() && inBasicTreeWindow())
				recordLastLengthsDisplayMode(branchLengthsDisplayMode.getValue());
			/*
			 * static final int SHOWULTRAMETRIC = 0; //	
			static final int AUTOSHOWLENGTHS = 1;
			static final int SHOWUNASSIGNEDASONE = 2; //if a branch has unassigned length, treat as length 1
			 */
			if (branchLengthsDisplayMode.getValue() == TreeDisplay.DRAWULTRAMETRIC) {
				deleteMostMenuItems();
				stretchMenuItem.setEnabled(true);
				evenMenuItem.setEnabled(true);
			}
			else {
				deleteMostMenuItems();
				fixedScalingMenuItem.setEnabled(true);
				if (fixedScale) {
					fixedScalingMenuItem.setName("Fixed Scaling Off");
					fixedScalingMenuItem.setCommand(makeCommand("offFixedScaling", this));
				}
				else {
					fixedScalingMenuItem.setName("Fixed Scaling...");
					fixedScalingMenuItem.setCommand(makeCommand("setFixedScaling", this));
				}
				//offFixedScalingMenuItem.setEnabled(fixedScale);
				showScaleMenuItem.setEnabled(true);
				broadScaleMenuItem.setEnabled(true);
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
				if (!fixedScalingMenuItem.getName().equalsIgnoreCase("Fixed Scaling Off")) {
					fixedScalingMenuItem.setName("Fixed Scaling Off");
					fixedScalingMenuItem.setCommand(makeCommand("offFixedScaling", this));
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
			fixedScalingMenuItem.setName("Fixed Scaling...");
			fixedScalingMenuItem.setCommand(makeCommand("setFixedScaling", this));
			//deleteMenuItem(offFixedScalingMenuItem);
			//offFixedScalingMenuItem.setEnabled(false);
			resetContainingMenuBar();
			parametersChanged();
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
	public int getDefaultOrientation() {
		return defaultOrientation;
	}
	void prepareFontMetrics(Font f, Graphics g){
		Font big2 = new Font(f.getName(), Font.PLAIN, f.getSize()*2);
		fmBIG = g.getFontMetrics(big2);
		fm = g.getFontMetrics(f);
	}

	boolean showScaleConsideringAuto(Tree tree, TreeDisplay treeDisplay){
		return showScale.getValue()
				&& branchLengthsDisplayMode.getValue()!= TreeDisplay.DRAWULTRAMETRIC 
				&& ((tree.hasBranchLengths() || treeDisplay.fixedScalingOn) && (tree.getAssociatedDoubles(consensusNR) == null));
	}
	NameReference consensusNR = NameReference.getNameReference("consensusFrequency");

	void checkAndAdjustParameterSettings(TreeDisplay treeDisplay, Tree tree){
		//Making sure my extra is in the treeDisplay. If not, this is a new connection!
		if (treeDisplay.getExtras() !=null) {
			if (treeDisplay.getExtras().myElements(this)==null) {  //todo: need to do one for each treeDisplay!
				NodeLocsExtra extra = new NodeLocsExtra(this, treeDisplay); 
				treeDisplay.addExtra(extra); 
				extras.addElement(extra);
			}
		}
		if (treeDisplay.getOrientation() == TreeDisplay.NOTYETSET || !compatibleWithOrientation(treeDisplay.getOrientation())){
			if (employerAllowsReorientation()){
				if (inBasicTreeWindow()){
					int or = recoverLastOrientation();
					if (or !=TreeDisplay.INVALIDORIENTATION && or !=TreeDisplay.NOTYETSET){
						treeDisplay.setOrientation(or);
						ornt = or;
					}
					else
						treeDisplay.setOrientation(ornt);

					resetMenus();
				}
				else {
					resetOrientation(ornt);
				}
			}
		}
		else { //inherited, accept ast ornt
			if (ornt!= treeDisplay.getOrientation()) {
				ornt = treeDisplay.getOrientation();
			}
			if (changeInOrientation()) 
				resetMenus();
		}
		treeDisplay.setFixedTaxonSpacing(fixedTaxonDistance);

		recordLastOrientation(treeDisplay.getOrientation());

		if (!leaveScaleAlone) {
			treeDisplay.fixedDepthScale = fixedDepth;
			treeDisplay.fixedScalingOn = fixedScale;
		}
		if (resetShowBranchLengths){
			treeDisplay.branchLengthDisplay=branchLengthsDisplayMode.getValue();
			treeDisplay.accumulateRequestsFromExtras(tree);
		}
		else {
			if (treeDisplay.branchLengthDisplay != branchLengthsDisplayMode.getValue()) {
				branchLengthsDisplayMode.setValue(treeDisplay.branchLengthDisplay);
				autoOn.setValue(branchLengthsDisplayMode.getValue() == TreeDisplay.AUTOSHOWLENGTHS);
				ultraOn.setValue(branchLengthsDisplayMode.getValue() == TreeDisplay.DRAWULTRAMETRIC);
				blOn.setValue(branchLengthsDisplayMode.getValue() == TreeDisplay.DRAWUNASSIGNEDASONE);
				if (branchLengthsDisplayMode.getValue() == TreeDisplay.DRAWULTRAMETRIC) {
					deleteMostMenuItems();
					stretchMenuItem.setEnabled(true);
					evenMenuItem.setEnabled(true);
				}
				else {
					deleteMostMenuItems();
					fixedScalingMenuItem.setEnabled(true);
					//offFixedScalingMenuItem.setEnabled(fixedScale);
					showScaleMenuItem.setEnabled(true);
					broadScaleMenuItem.setEnabled(true);
				}
				resetContainingMenuBar();
			}
		}
	}

	/*_________________________________________________*/

	private double getNonZeroBranchLength(Tree tree, int N) {
		if (tree.branchLengthUnassigned(N))
			return 1;
		else
			return tree.getBranchLength(N);
	}

	/*_________________________________________________*/
	private double lastleft;
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
				double nFDx = treeDrawing.x[fD];
				double nFDy = treeDrawing.y[fD];
				double nLDx = treeDrawing.x[lD];
				double nLDy = treeDrawing.y[lD];
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
				double nFDx = treeDrawing.x[fD];
				double nLDx = treeDrawing.x[lD];
				treeDrawing.x[N] =(nFDx + nLDx) / 2;
			}
		}
	}

	/*....................................................................................................*/
	private void UPCalcTerminalLocs(TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int N) {
		if  (tree.nodeIsTerminal(N)) {   //terminal
			if (tree.withinCollapsedClade(N)){
				int dCA = tree.deepestCollapsedAncestor(N);
				if (tree.leftmostTerminalOfNode(dCA)==N)
					lastleft+= getSpacing(treeDisplay, tree, N); 
				else {
					lastleft= treeDrawing.x[tree.leftmostTerminalOfNode(dCA)];
				}
			}
			else
				lastleft+= getSpacing(treeDisplay, tree, N);
			treeDrawing.y[N] = treeDisplay.getTipsMargin();
			treeDrawing.x[N] = lastleft;
		}
		else {
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				UPCalcTerminalLocs(treeDisplay, treeDrawing, tree, d);
			}
		}
	}
	/*....................................................................................................*/
	private void UPevenNodeLocs(TreeDrawing treeDrawing, Tree tree, int N, double evenVertSpacing) {
		if (tree.nodeIsInternal(N)){
			double deepest = MesquiteDouble.unassigned;
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				UPevenNodeLocs(treeDrawing, tree, d, evenVertSpacing);
				deepest = MesquiteDouble.maximum(deepest,  treeDrawing.y[d]);
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
		treeDrawing.y[N]=nH;
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
			double nFDx = treeDrawing.x[nFD];
			double nFDy = treeDrawing.y[nFD];
			double nLDx = treeDrawing.x[nLD];
			double nLDy = treeDrawing.y[nLD];
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
	private void DOWNCalcTerminalLocs(TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int N, double margin) {
		if  (tree.nodeIsTerminal(N)) {   //terminal
			if (tree.withinCollapsedClade(N)){
				int dCA = tree.deepestCollapsedAncestor(N);
				if (tree.leftmostTerminalOfNode(dCA)==N)
					lastleft+= getSpacing(treeDisplay, tree, N); 
				else {
					lastleft= treeDrawing.x[tree.leftmostTerminalOfNode(dCA)];
				}
			}
			else
				lastleft+= getSpacing(treeDisplay, tree, N);
			treeDrawing.y[N] = margin;
			treeDrawing.x[N] = lastleft;
		}
		else {
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				DOWNCalcTerminalLocs(treeDisplay, treeDrawing, tree, d, margin);
		}
	}
	/*....................................................................................................*/
	private void DOWNstretchNodeLocs(TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int N, double margin) {
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			DOWNstretchNodeLocs(treeDisplay, treeDrawing, tree, d, margin);
		treeDrawing.y[N] = margin-(int)((margin-treeDrawing.y[N])*treeDisplay.nodeLocsParameters[stretchfactor]);
	}
	/*....................................................................................................*/
	private void DOWNevenNodeLocs(TreeDrawing treeDrawing, Tree tree, int N, double evenVertSpacing) {
		if (tree.nodeIsInternal(N)){
			double deepest = MesquiteDouble.unassigned;
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				DOWNevenNodeLocs(treeDrawing, tree, d, evenVertSpacing);
				deepest = MesquiteDouble.minimum(deepest, treeDrawing.y[d]);
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

		treeDrawing.y[N]=nH;

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
			double nFDx = treeDrawing.x[fD];
			double nFDy = treeDrawing.y[fD];
			double nLDx = treeDrawing.x[lD];
			double nLDy = treeDrawing.y[lD];
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
				double nFDy = treeDrawing.y[fD];
				double nLDy = treeDrawing.y[lD];
				treeDrawing.y[N] =(nFDy + nLDy) / 2;
			}
		}
	}

	/*....................................................................................................*/
	private void RIGHTCalcTerminalLocs(TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int N, double margin) {
		if  (tree.nodeIsTerminal(N)) {   //terminal
			if (tree.withinCollapsedClade(N)){
				int dCA = tree.deepestCollapsedAncestor(N);
				if (tree.leftmostTerminalOfNode(dCA)==N)
					lastleft+= getSpacing(treeDisplay, tree, N); 
				else {
					lastleft= treeDrawing.y[tree.leftmostTerminalOfNode(dCA)];
				}
			}
			else
				lastleft+= getSpacing(treeDisplay, tree, N);

			treeDrawing.x[N] = margin;
			treeDrawing.y[N] = lastleft;

		}
		else {
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				RIGHTCalcTerminalLocs(treeDisplay, treeDrawing, tree, d, margin);
		}
	}
	/*....................................................................................................*/
	private void RIGHTstretchNodeLocs(TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int N, double margin) {
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			RIGHTstretchNodeLocs(treeDisplay, treeDrawing, tree, d, margin);
		treeDrawing.x[N] =  margin- (int)((margin - treeDrawing.x[N])*treeDisplay.nodeLocsParameters[stretchfactor]);
	}
	/*....................................................................................................*/
	private double RIGHThighestTaxonByPixels(TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int N) {
		if (tree.nodeIsTerminal(N))
			return treeDrawing.x[N];
		double highest = MesquiteDouble.unassigned;
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
			highest = MesquiteDouble.maximum(highest, RIGHThighestTaxonByPixels(treeDisplay, treeDrawing, tree, d));
		}
		return highest;
	}

	/*....................................................................................................*/
	private void RIGHTevenNodeLocs(TreeDrawing treeDrawing, Tree tree, int N, double evenVertSpacing) {
		if (tree.nodeIsInternal(N)){
			double deepest = MesquiteDouble.unassigned;
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				RIGHTevenNodeLocs(treeDrawing, tree, d, evenVertSpacing);
				deepest = MesquiteDouble.minimum(deepest, treeDrawing.x[d]);
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
		treeDrawing.x[N]=nH;

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
			double nFDx = treeDrawing.x[fD];
			double nFDy = treeDrawing.y[fD];
			double nLDx = treeDrawing.x[lD];
			double nLDy = treeDrawing.y[lD];
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
	private void LEFTCalcTerminalLocs(TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int N) {
		if  (tree.nodeIsTerminal(N)) {   //terminal
			if (tree.withinCollapsedClade(N)){
				int dCA = tree.deepestCollapsedAncestor(N);
				if (tree.leftmostTerminalOfNode(dCA)==N)
					lastleft+= getSpacing(treeDisplay, tree, N); 
				else {
					lastleft= treeDrawing.y[tree.leftmostTerminalOfNode(dCA)];
				}
			}
			else
				lastleft+= getSpacing(treeDisplay, tree, N);
			treeDrawing.x[N] = treeDisplay.getTipsMargin();
			treeDrawing.y[N] = lastleft;
		}
		else {
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				LEFTCalcTerminalLocs(treeDisplay, treeDrawing, tree, d);
		}
	}
	/*....................................................................................................*/
	private void LEFTstretchNodeLocs(TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int N) {
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			LEFTstretchNodeLocs(treeDisplay, treeDrawing, tree, d);
		treeDrawing.x[N] = treeDisplay.getTipsMargin() + (int)((treeDrawing.x[N]-treeDisplay.getTipsMargin())*treeDisplay.nodeLocsParameters[stretchfactor]);
	}

	/*....................................................................................................*/
	private void LEFTevenNodeLocs(TreeDrawing treeDrawing, Tree tree, int N, double evenVertSpacing) {
		if (tree.nodeIsInternal(N)){
			double deepest = MesquiteDouble.unassigned;
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				LEFTevenNodeLocs(treeDrawing, tree, d, evenVertSpacing);
				deepest = MesquiteDouble.maximum(deepest,  treeDrawing.x[d]);
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
	/*_________________________________________________*/
	private void calcInternalLocsPushHiddenInCollapsed(TreeDrawing treeDrawing, Tree tree, int N) {
		if (tree.nodeIsInternal(N)) { //internal
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				calcInternalLocsPushHiddenInCollapsed(treeDrawing, tree,  d);
			if (tree.withinCollapsedClade(N)){
				treeDrawing.x[N] = treeDrawing.x[tree.deepestCollapsedAncestor(N)];
				treeDrawing.y[N] = treeDrawing.y[tree.deepestCollapsedAncestor(N)];
			}
		}
	}
	/*_________________________________________________*/
	private double highestDescendant(TreeDrawing treeDrawing, Tree tree, int N, int orientation) {
		if (tree.nodeIsInternal(N)) { //internal
			double highestInClade = MesquiteDouble.unassigned;
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				double highestInSubclade = highestDescendant(treeDrawing, tree,  d, orientation);
				if (orientation==TreeDisplay.UP || orientation==TreeDisplay.LEFT)
					highestInClade = MesquiteDouble.minimum(highestInSubclade, highestInClade);
				else
					highestInClade = MesquiteDouble.maximum(highestInSubclade, highestInClade);
			}
			return highestInClade;
		}
		if (orientation==TreeDisplay.UP || orientation==TreeDisplay.DOWN)
			return treeDrawing.y[N];
		else
			return treeDrawing.x[N];
	}
	/*_________________________________________________*/
	private double lowestDescendant(TreeDrawing treeDrawing, Tree tree, int N, int orientation) {
		if (tree.nodeIsInternal(N)) { //internal
			double lowestInClade = MesquiteDouble.unassigned;
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				double lowestInSubclade = lowestDescendant(treeDrawing, tree,  d, orientation);
				if (orientation==TreeDisplay.UP || orientation==TreeDisplay.LEFT)
					lowestInClade = MesquiteDouble.maximum(lowestInSubclade, lowestInClade);
				else
					lowestInClade = MesquiteDouble.minimum(lowestInSubclade, lowestInClade);
			}
			return lowestInClade;
		}
		if (orientation==TreeDisplay.UP || orientation==TreeDisplay.DOWN)
			return treeDrawing.y[N];
		else
			return treeDrawing.x[N];
	}
	/*_________________________________________________*/
	private void calcTerminalLocsPushHiddenInCollapsed(TreeDrawing treeDrawing, Tree tree, int N, int orientation) {
		if (tree.nodeIsTerminal(N) && tree.withinCollapsedClade(N)) {
			int dCA = tree.deepestCollapsedAncestor(N);
			if (tree.leftmostTerminalOfNode(dCA)==N){ //this is leftmost in collapsed clade; therefore take its height from the highest of the descendants of that clade
				double highest = highestDescendant(treeDrawing, tree, dCA, orientation);
				double lowest = lowestDescendant(treeDrawing, tree, dCA, orientation);
				if (orientation==TreeDisplay.UP || orientation==TreeDisplay.DOWN) {
					treeDrawing.y[N] = highest;
					/*treeDrawing.yShortestTerminal[dCA] = lowest;
						treeDrawing.xShortestTerminal[dCA] = treeDrawing.x[N] ;*/
					int mother = tree.motherOfNode(N);
					treeDrawing.yDashed[mother] = lowest;
					treeDrawing.xDashed[mother] = treeDrawing.x[N] ;
					treeDrawing.yDashed[N] = treeDrawing.y[N];
					treeDrawing.xDashed[N] = treeDrawing.x[N] ;
					treeDrawing.ySolid[mother] = treeDrawing.y[tree.deepestCollapsedAncestor(N)] ;
					treeDrawing.xSolid[mother] = treeDrawing.x[tree.deepestCollapsedAncestor(N)] ;
					treeDrawing.ySolid[N] = lowest;
					treeDrawing.xSolid[N] = treeDrawing.x[N] ;
				}
				else {
					treeDrawing.x[N] = highest;
					/*	treeDrawing.xShortestTerminal[dCA] = lowest;
						treeDrawing.yShortestTerminal[dCA] = treeDrawing.y[N] ;*/
					int mother = tree.motherOfNode(N);
					treeDrawing.xDashed[mother] = lowest;
					treeDrawing.yDashed[mother] = treeDrawing.y[N] ;
					treeDrawing.yDashed[N] = treeDrawing.y[N];
					treeDrawing.xDashed[N] = treeDrawing.x[N] ;
					treeDrawing.ySolid[mother] = treeDrawing.y[tree.deepestCollapsedAncestor(N)] ;
					treeDrawing.xSolid[mother] = treeDrawing.x[tree.deepestCollapsedAncestor(N)] ;
					treeDrawing.ySolid[N] = treeDrawing.y[N] ;
					treeDrawing.xSolid[N] = lowest;
				}
			}
		}
		if (tree.nodeIsInternal(N)) { //internal
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				calcTerminalLocsPushHiddenInCollapsed(treeDrawing, tree,  d, orientation);
		}
	}
	/*....................................................................................................*/
	private double edgeNode (TreeDrawing treeDrawing, Tree tree, int node, boolean x, boolean max) {
		if (tree.nodeIsTerminal(node)) {
			if (x)
				return treeDrawing.x[node];
			else
				return treeDrawing.y[node];
		}
		double t;
		if (max)
			t = 0;
		else
			t = 1000000;
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			double e = edgeNode( treeDrawing, tree, d, x, max);
			if (max && e> t)
				t = e;
			else if (!max && e < t)
				t = e;
		}
		return t;
	}
	/*_________________________________________________*/
	private double propAverage(double xd, double xa, double i, double L){
		return (double)(1.0*i*(xa-xd)/L + xd);
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
			MesquiteNumber xValue=new MesquiteNumber();
			MesquiteNumber yValue=new MesquiteNumber();
			treeDrawing.getSingletonLocation(tree, N,  xValue,  yValue);
			treeDrawing.x[N]=xValue.getDoubleValue();
			treeDrawing.y[N]=yValue.getDoubleValue();
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
					double bottomX =treeDrawing.x[tree.motherOfNode(N)] ;
					double bottomY =treeDrawing.y[tree.motherOfNode(N)] ;
					double topX =treeDrawing.x[N] ;
					double topY =treeDrawing.y[N] ;
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
	FontMetrics fm, fmBIG;
	private int findMaxNameLength(TreeDisplay treeDisplay, Tree tree, int N) {
		if (tree.nodeIsTerminal(N)) {
			String s = null;
			if (tree.withinCollapsedClade(N)){
				if (tree.isLeftmostTerminalOfCollapsedClade(N)){
					s = tree.getNodeLabel(tree.deepestCollapsedAncestor(N));
					if (StringUtil.blank(s))
						s = "Clade of " + tree.getTaxa().getName(tree.taxonNumberOfNode(N));
				}
				else return 0;
			}
			else
				s = tree.getTaxa().getName(tree.taxonNumberOfNode(N));
			if (s==null)
				return 0;
			else if (treeDisplay.selectedTaxonHighlightMode > TreeDisplay.sTHM_GREYBOX && tree.getTaxa().getSelected(tree.taxonNumberOfNode(N)))
				return fmBIG.stringWidth(s);
			else
				return fm.stringWidth(s);
		}
		else {
			int max = 0;
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				int cur = findMaxNameLength(treeDisplay, tree, d);
				if (cur>max)
					max = cur;
			}
			return max;
		}
	}
	/*.................................................................................................................*/
	double highlightMultiplier(TreeDisplay treeDisplay){
		return (treeDisplay.selectedTaxonHighlightMode+3)/4.0;
	}
	/*.................................................................................................................*/
	public double effectiveNumberOfTerminalsInClade(Tree tree, int node, TreeDisplay treeDisplay){
		if (tree.isCollapsedClade(node))
			return 1;
		if (tree.nodeIsTerminal(node)){
			if (treeDisplay.selectedTaxonHighlightMode > TreeDisplay.sTHM_GREYBOX && tree.getTaxa().getSelected(tree.taxonNumberOfNode(node)))
				return highlightMultiplier(treeDisplay);
			else
				return 1;
		}
		double num=0;
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			num += effectiveNumberOfTerminalsInClade(tree, d, treeDisplay);
		}
		return num;
	}

	/*.................................................................................................................*/
	public double effectiveNumberOfTerminals(Tree tree, int node, TreeDisplay treeDisplay){
		if (tree.nodeIsTerminal(node)){
			if (treeDisplay.selectedTaxonHighlightMode > TreeDisplay.sTHM_GREYBOX && tree.getTaxa().getSelected(tree.taxonNumberOfNode(node)))
				return highlightMultiplier(treeDisplay);
			else
				return 1;
		}
		else if (tree.isCollapsedClade(node)) {
			if (effectiveNumberOfTerminalsInClade(tree, node, treeDisplay)>2)
				return 3;
			else 
				return 2;
		}
		double num=0;
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			num += effectiveNumberOfTerminals(tree, d, treeDisplay);
		}
		return num;
	}
	/*.................................................................................................................*/
	int zoomNode = -1;
	double zoomFactor = 1.0;
	void setZoom(TreeDisplay treeDisplay, int node, double factor){
		if (!treeDisplay.getTree().descendantOf(node, zoomNode)){ //in zoom
			double inZoom = treeDisplay.getTree().numberOfVisibleTerminalsInClade(zoomNode);
			double outZoom = treeDisplay.getTree().numberOfVisibleTerminalsInClade(treeDisplay.getTreeDrawing().getDrawnRoot()) - inZoom;
			double currentFactor = (1 - (zoomFactor-1)*inZoom/outZoom);
			factor *= currentFactor;
		}
		zoomNode = node;
		zoomFactor = factor;
		parametersChanged();
	}

	double getSpacing(TreeDisplay treeDisplay, Tree tree, int node){
		double baseSpacing =treeDisplay.getTaxonSpacing();
		if (treeDisplay.selectedTaxonHighlightMode > TreeDisplay.sTHM_GREYBOX  && tree.getTaxa().getSelected(tree.taxonNumberOfNode(node)))
			baseSpacing = baseSpacing *highlightMultiplier(treeDisplay)*0.89;  //0.89 is magical constant to prevent too big a space for highlighted taxa
		if (zoomNode > 0){
			double inZoom = tree.numberOfVisibleTerminalsInClade(zoomNode);
			if (inZoom<2)
				return baseSpacing;
			double outZoom = tree.numberOfVisibleTerminalsInClade(treeDisplay.getTreeDrawing().getDrawnRoot()) - inZoom;

			if (tree.descendantOf(node, zoomNode)) //in zoom
				return (baseSpacing *zoomFactor);
			else 
				return (baseSpacing * (1 - (zoomFactor-1)*inZoom/outZoom));
		}
		return baseSpacing;
	}

	/*_________________________________________________*
	private   void reportLocs(Tree tree, int node, TreeDisplay treeDisplay) {
		if (tree.nodeExists(node)) {
			int thisSister = tree.firstDaughterOfNode(node);
			while (tree.nodeExists(thisSister)) {
				reportLocs( tree, thisSister, treeDisplay);
				thisSister = tree.nextSisterOfNode(thisSister);
			}
		}
	}
	/*.................................................................................................................*/
	public void calculateNodeLocs(TreeDisplay treeDisplay, Tree tree, int drawnRoot) { //Graphics g removed as parameter May 02
		if (MesquiteTree.OK(tree)) {
			//Making sure treeDisplay and here are in tune about some settings
			checkAndAdjustParameterSettings(treeDisplay, tree);
			TreeDrawing treeDrawing = treeDisplay.getTreeDrawing();
			int root = drawnRoot;
			int subRoot = tree.motherOfNode(drawnRoot);
			//If it's just a single terminal on the tree, have to make the root size handle the whole tree
			int effectiveROOTSIZE = 20;
			if (tree.numberOfVisibleTerminalsInClade(drawnRoot) == 1){  // it is just a single terminal in the tree
				if (treeDisplay.getOrientation()==TreeDisplay.UP || treeDisplay.getOrientation()==TreeDisplay.DOWN)
					effectiveROOTSIZE += treeDisplay.effectiveFieldHeight()/6;
				else if (treeDisplay.getOrientation()==TreeDisplay.LEFT || treeDisplay.getOrientation()==TreeDisplay.RIGHT)
					effectiveROOTSIZE +=  treeDisplay.effectiveFieldWidth()/6;
			}
			else {
				if (treeDisplay.getOrientation()==TreeDisplay.UP)
					effectiveROOTSIZE = treeDisplay.effectiveFieldBottomMargin();
				else if (treeDisplay.getOrientation()==TreeDisplay.DOWN)
					effectiveROOTSIZE = treeDisplay.effectiveFieldTopMargin();
				else if (treeDisplay.getOrientation()==TreeDisplay.LEFT)
					effectiveROOTSIZE +=  treeDisplay.effectiveFieldRightMargin();
				else if (treeDisplay.getOrientation()==TreeDisplay.RIGHT)
					effectiveROOTSIZE +=  treeDisplay.effectiveFieldLeftMargin();
			}
			boolean drawDiagonalRoot = getEmployer() instanceof DiagonalRootDrawer;
			double extraDepthAtRootRequested = 0.0;
			TreeDisplayRequests requested = treeDisplay.getExtraTreeDisplayRequests(); //accumulated if needed in checkAndAdjustParameterSettings
			if (requested != null)
				extraDepthAtRootRequested = requested.extraDepthAtRoot;

			//Resetting tips margin according to length of taxon names
			Graphics g = treeDisplay.getGraphics();
			if (g!=null) {
				if (!treeDisplay.suppressNames) {
					DrawNamesTreeDisplay dtn = treeDisplay.getDrawTaxonNames();
					Font f = null;
					if (dtn!=null)
						f = dtn.getFont();
					if (f==null)
						f = g.getFont();
					prepareFontMetrics(f, g);
					if (fm!=null)
						treeDisplay.setTipsMargin(findMaxNameLength(treeDisplay, tree, root) + treeDisplay.getTaxonNameBuffer() + treeDisplay.getTaxonNameDistanceFromTip());
				}
				else 
					treeDisplay.setTipsMargin(treeDisplay.getTaxonNameBuffer());
				g.dispose();
			}
			boolean branchesProportionalToLength = treeDisplay.branchLengthDisplay == TreeDisplay.DRAWUNASSIGNEDASONE || 
					(treeDisplay.branchLengthDisplay == TreeDisplay.AUTOSHOWLENGTHS && (tree.hasBranchLengths() || treeDisplay.fixedScalingOn));
			branchesProportionalToLength = branchesProportionalToLength & 
					!((tree.getAssociatedDoubles(consensusNR) != null) && (treeDisplay.branchLengthDisplay == TreeDisplay.AUTOSHOWLENGTHS));

			if (treeDisplay.getOrientation()==TreeDisplay.UP) {  // ################################  UP #########################
				double availableTreeHeight = treeDisplay.effectiveFieldHeight()-treeDisplay.getTipsMargin();
				int numTerms = (int)effectiveNumberOfTerminals(tree, root, treeDisplay);
				if (numTerms == 0)
					numTerms = 1;
				if (fixedTaxonDistance!=0 && MesquiteDouble.isCombinable(fixedTaxonDistance))
					treeDisplay.setTaxonSpacing(fixedTaxonDistance);
				else {
					treeDisplay.setTaxonSpacing( 1.0*(treeDisplay.effectiveFieldWidth() - treeDisplay.bufferForScaleEtc) / numTerms);
					treeDisplay.setTaxonSpacing((taxonSqueeze*treeDisplay.getTaxonSpacing()));
				}

				lastleft = -treeDisplay.getTaxonSpacing()/3*2; //TODO: this causes problems for shrunk, since first taxon doesn't move over enough
				UPCalcTerminalLocs(treeDisplay, treeDrawing, tree, root);
				UPCalcInternalLocs( treeDrawing, tree, root);
				if (center.getValue())
					UPDOWNCenterInternalLocs( treeDrawing, tree, root);
				placeSingletons(treeDrawing, tree, root);
				if (branchesProportionalToLength) {
					treeDisplay.nodeLocsParameters[totalHeight]= tree.tallestPathAboveNode(root, 1.0) + extraDepthAtRootRequested;
					if (!treeDisplay.fixedScalingOn) {
						treeDisplay.fixedDepthScale = treeDisplay.nodeLocsParameters[totalHeight];
						fixedDepth = treeDisplay.fixedDepthScale;
						if (treeDisplay.nodeLocsParameters[totalHeight]==0){
							treeDisplay.nodeLocsParameters[scaling]=1;
						}
						else
							treeDisplay.nodeLocsParameters[scaling]=availableTreeHeight/(treeDisplay.nodeLocsParameters[totalHeight]); 
						UPdoAdjustLengths( treeDisplay, treeDrawing, tree, treeDisplay.effectiveFieldHeight(), root, 0, root);
						if (showScaleConsideringAuto(tree,  treeDisplay)) {
							drawGrid(treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.nodeLocsParameters[scaling], treeDisplay, g);
						}
					}
					else {
						if (treeDisplay.fixedDepthScale == 0)
							treeDisplay.fixedDepthScale = 1;
						treeDisplay.nodeLocsParameters[scaling]=availableTreeHeight/(treeDisplay.fixedDepthScale); 
						UPdoAdjustLengths( treeDisplay, treeDrawing, tree, treeDisplay.effectiveFieldHeight()-(int)(treeDisplay.nodeLocsParameters[scaling]*(treeDisplay.fixedDepthScale-treeDisplay.nodeLocsParameters[totalHeight])), root, 0, root);
						if (showScaleConsideringAuto(tree,  treeDisplay)) {
							drawGrid(treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.fixedDepthScale, treeDisplay.nodeLocsParameters[scaling], treeDisplay, g);
						}
					}
				}
				else {
					if (even.getValue()){
						double evenVertSpacing =(treeDrawing.y[root] - treeDisplay.getTipsMargin())/ (tree.mostStepsAboveNode(root) + 1);
						if (evenVertSpacing > 0)
							UPevenNodeLocs(treeDrawing, tree, root, evenVertSpacing);
					}
					if (!inhibitStretch.getValue() && (treeDisplay.autoStretchIfNeeded )) { //&& treeDrawing.y[subRoot]>treeDisplay.effectiveFieldHeight()
						treeDisplay.nodeLocsParameters[stretchfactor]=availableTreeHeight / (treeDrawing.y[root] - (int)treeDisplay.getTipsMargin());
						UPstretchNodeLocs(treeDisplay, treeDrawing, tree, root);
					}
				}
				treeDrawing.y[subRoot] = (treeDrawing.y[root])+effectiveROOTSIZE;
				treeDrawing.x[subRoot] = (treeDrawing.x[root]);
				if (drawDiagonalRoot)
					treeDrawing.x[subRoot] -= effectiveROOTSIZE;
			}
			else if (treeDisplay.getOrientation()==TreeDisplay.DOWN) {  // ################################  DOWN #########################
				double availableTreeHeight = treeDisplay.effectiveFieldHeight()-treeDisplay.getTipsMargin();
				int numTerms = (int)effectiveNumberOfTerminals(tree, root, treeDisplay);
				if (numTerms == 0)
					numTerms = 1;
				if (fixedTaxonDistance!=0 && MesquiteDouble.isCombinable(fixedTaxonDistance))
					treeDisplay.setTaxonSpacing(fixedTaxonDistance);
				else {
					treeDisplay.setTaxonSpacing( 1.0*(treeDisplay.effectiveFieldWidth() - treeDisplay.bufferForScaleEtc) / numTerms);
					treeDisplay.setTaxonSpacing((taxonSqueeze*treeDisplay.getTaxonSpacing()));
				}

				lastleft = -treeDisplay.getTaxonSpacing()/3*2;
				DOWNCalcTerminalLocs(treeDisplay, treeDrawing, tree, root, availableTreeHeight);
				DOWNCalcInternalLocs(treeDrawing, tree, root);
				if (center.getValue())
					UPDOWNCenterInternalLocs(treeDrawing, tree, root);
				placeSingletons(treeDrawing, tree, root);
				if (branchesProportionalToLength) {
					treeDisplay.nodeLocsParameters[totalHeight]=tree.tallestPathAboveNode(root, 1.0) + extraDepthAtRootRequested;
					if (!treeDisplay.fixedScalingOn) {
						treeDisplay.fixedDepthScale = treeDisplay.nodeLocsParameters[totalHeight];
						fixedDepth = treeDisplay.fixedDepthScale;
						if (treeDisplay.nodeLocsParameters[totalHeight]==0)
							treeDisplay.nodeLocsParameters[scaling]=1;
						else
							treeDisplay.nodeLocsParameters[scaling]=availableTreeHeight/(treeDisplay.nodeLocsParameters[totalHeight]); 
						DOWNdoAdjustLengths(treeDisplay, treeDrawing, tree, 0, root, 0, root);
						if (showScaleConsideringAuto(tree,  treeDisplay)) 
							drawGrid(treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.nodeLocsParameters[scaling], treeDisplay, g);
					}
					else {
						if (treeDisplay.fixedDepthScale == 0)
							treeDisplay.fixedDepthScale = 1;
						treeDisplay.nodeLocsParameters[scaling]=availableTreeHeight/(treeDisplay.fixedDepthScale); 
						DOWNdoAdjustLengths(treeDisplay, treeDrawing, tree, (int)(treeDisplay.nodeLocsParameters[scaling]*(treeDisplay.fixedDepthScale-treeDisplay.nodeLocsParameters[totalHeight])), root, 0, root);
						if (showScaleConsideringAuto(tree,  treeDisplay)) 
							drawGrid(treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.fixedDepthScale, treeDisplay.nodeLocsParameters[scaling], treeDisplay, g);
					}
				}
				else {
					if (even.getValue()){
						double evenVertSpacing = (- treeDrawing.y[subRoot] + edgeNode(treeDrawing, tree, root, false, true))/ (tree.mostStepsAboveNode(root) + 1);
						if (evenVertSpacing > 0)
							DOWNevenNodeLocs(treeDrawing, tree, root, evenVertSpacing);
					}
					if (!inhibitStretch.getValue() && treeDisplay.autoStretchIfNeeded) {  //&& treeDrawing.y[subRoot]>0)
						treeDisplay.nodeLocsParameters[stretchfactor]=availableTreeHeight / (availableTreeHeight - treeDrawing.y[root]);
						DOWNstretchNodeLocs(treeDisplay, treeDrawing, tree, root, availableTreeHeight);
						treeDrawing.y[subRoot]=5;
					}
				}
				treeDrawing.y[subRoot] = (treeDrawing.y[root])-effectiveROOTSIZE;
				treeDrawing.x[subRoot] = (treeDrawing.x[root]);
				if (drawDiagonalRoot)
					treeDrawing.x[subRoot] -= effectiveROOTSIZE;
			}
			else if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) {  // ################################  RIGHT #########################
				double availableTreeHeight = treeDisplay.effectiveFieldWidth()-treeDisplay.getTipsMargin();
				int numTerms = (int)effectiveNumberOfTerminals(tree, root, treeDisplay);
				if (numTerms == 0)
					numTerms = 1;
				if (fixedTaxonDistance!=0 && MesquiteDouble.isCombinable(fixedTaxonDistance))
					treeDisplay.setTaxonSpacing(fixedTaxonDistance);
				else {
					treeDisplay.setTaxonSpacing( 1.0*(treeDisplay.effectiveFieldHeight() - treeDisplay.bufferForScaleEtc) / numTerms);
					treeDisplay.setTaxonSpacing((taxonSqueeze*treeDisplay.getTaxonSpacing()));
				}


				lastleft = -treeDisplay.getTaxonSpacing()/3*2;

				RIGHTCalcTerminalLocs(treeDisplay, treeDrawing, tree, root, availableTreeHeight);
				RIGHTCalcInternalLocs(treeDrawing, tree, root);
				if (center.getValue())
					RIGHTLEFTCenterInternalLocs( treeDrawing, tree, root);
				placeSingletons(treeDrawing, tree, root);
				if (branchesProportionalToLength) {
					treeDisplay.nodeLocsParameters[totalHeight]=tree.tallestPathAboveNode(root, 1.0) + extraDepthAtRootRequested;
					if (!treeDisplay.fixedScalingOn) { 
						treeDisplay.fixedDepthScale = treeDisplay.nodeLocsParameters[totalHeight];
						fixedDepth = treeDisplay.fixedDepthScale;
						if (treeDisplay.nodeLocsParameters[totalHeight]==0)
							treeDisplay.nodeLocsParameters[scaling]=1;
						else
							treeDisplay.nodeLocsParameters[scaling]=availableTreeHeight/treeDisplay.nodeLocsParameters[totalHeight]; 
						RIGHTdoAdjustLengths(treeDisplay, treeDrawing, tree, 0, root, 0, root);
						if (showScaleConsideringAuto(tree,  treeDisplay)) 
							drawGrid(treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.nodeLocsParameters[scaling], treeDisplay, g);
					}
					else {
						if (treeDisplay.fixedDepthScale == 0)
							treeDisplay.fixedDepthScale = 1;
						treeDisplay.nodeLocsParameters[scaling]=availableTreeHeight/treeDisplay.fixedDepthScale; 
						RIGHTdoAdjustLengths(treeDisplay, treeDrawing, tree, (int)(treeDisplay.nodeLocsParameters[scaling]*(treeDisplay.fixedDepthScale-treeDisplay.nodeLocsParameters[totalHeight])), root, 0, root);
						if (showScaleConsideringAuto(tree,  treeDisplay)) {
							drawGrid(treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.fixedDepthScale, treeDisplay.nodeLocsParameters[scaling], treeDisplay, g);
						}
					}
				}
				else {
					if (even.getValue()){
						double evenVertSpacing =treeDisplay.getTipsMargin()/ (tree.mostStepsAboveNode(root) + 1);
						if (evenVertSpacing > 0)
							RIGHTevenNodeLocs(treeDrawing, tree, root, evenVertSpacing);
					}
					if (!inhibitStretch.getValue() && treeDisplay.autoStretchIfNeeded) { //&& treeDrawing.x[subRoot]>0
						treeDisplay.nodeLocsParameters[stretchfactor]=availableTreeHeight/ (availableTreeHeight - treeDrawing.x[root]);
						RIGHTstretchNodeLocs(treeDisplay,treeDrawing, tree, root,availableTreeHeight);
					}
				}
				treeDrawing.y[subRoot] = (treeDrawing.y[root]);
				treeDrawing.x[subRoot] = (treeDrawing.x[root])-effectiveROOTSIZE;
				if (drawDiagonalRoot)
					treeDrawing.y[subRoot] -= effectiveROOTSIZE;
			}
			else if (treeDisplay.getOrientation()==TreeDisplay.LEFT) {  // ################################  LEFT #########################
				double availableTreeHeight = treeDisplay.effectiveFieldWidth()-treeDisplay.getTipsMargin();
				int numTerms = (int)effectiveNumberOfTerminals(tree, root, treeDisplay);
				if (numTerms == 0)
					numTerms = 1;
				if (fixedTaxonDistance!=0 && MesquiteDouble.isCombinable(fixedTaxonDistance))
					treeDisplay.setTaxonSpacing(fixedTaxonDistance);
				else {
					treeDisplay.setTaxonSpacing( 1.0*(treeDisplay.effectiveFieldHeight() - treeDisplay.bufferForScaleEtc) / numTerms);
					treeDisplay.setTaxonSpacing((taxonSqueeze*treeDisplay.getTaxonSpacing()));
				}

				lastleft = -treeDisplay.getTaxonSpacing()/3*2;
				LEFTCalcTerminalLocs(treeDisplay, treeDrawing, tree, root);
				LEFTCalcInternalLocs(treeDrawing, tree, root);
				if (center.getValue())
					RIGHTLEFTCenterInternalLocs(treeDrawing, tree, root);
				placeSingletons(treeDrawing, tree, root);
				if (branchesProportionalToLength) {
					treeDisplay.nodeLocsParameters[totalHeight]=tree.tallestPathAboveNode(root, 1.0) + extraDepthAtRootRequested;
					if (!treeDisplay.fixedScalingOn) {
						treeDisplay.fixedDepthScale = treeDisplay.nodeLocsParameters[totalHeight];
						fixedDepth = treeDisplay.fixedDepthScale;
						if (treeDisplay.nodeLocsParameters[totalHeight]==0)
							treeDisplay.nodeLocsParameters[scaling]=1;
						else
							treeDisplay.nodeLocsParameters[scaling]=availableTreeHeight/treeDisplay.nodeLocsParameters[totalHeight]; 
						LEFTdoAdjustLengths(treeDisplay, treeDrawing, tree, treeDisplay.effectiveFieldWidth(), root, 0, root);
						if (showScaleConsideringAuto(tree,  treeDisplay)) 
							drawGrid(treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.nodeLocsParameters[scaling], treeDisplay, g);
					}
					else { 
						if (treeDisplay.fixedDepthScale == 0)
							treeDisplay.fixedDepthScale = 1;
						treeDisplay.nodeLocsParameters[scaling]=availableTreeHeight/(treeDisplay.fixedDepthScale); 
						LEFTdoAdjustLengths(treeDisplay, treeDrawing, tree, treeDisplay.effectiveFieldWidth()-(int)(treeDisplay.nodeLocsParameters[scaling]*(treeDisplay.fixedDepthScale-treeDisplay.nodeLocsParameters[totalHeight])), root, 0, root);
						if (showScaleConsideringAuto(tree,  treeDisplay)) 
							drawGrid(treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.fixedDepthScale, treeDisplay.nodeLocsParameters[scaling], treeDisplay, g);
					}
				}
				else {  
					if (even.getValue()){
						double evenVertSpacing =(treeDrawing.x[root] - treeDisplay.getTipsMargin())/ (tree.mostStepsAboveNode(root) + 1);
						if (evenVertSpacing > 0)
							LEFTevenNodeLocs(treeDrawing, tree, root, evenVertSpacing);
					}
					if (!inhibitStretch.getValue() && (treeDisplay.autoStretchIfNeeded )) {  //&& treeDrawing.x[subRoot]>treeDisplay.effectiveFieldWidth()
						treeDisplay.nodeLocsParameters[stretchfactor]=availableTreeHeight / (treeDrawing.x[root] - (int)treeDisplay.getTipsMargin());
						LEFTstretchNodeLocs(treeDisplay, treeDrawing, tree, root);
					}
				}
				treeDrawing.y[subRoot] = (treeDrawing.y[root]);
				treeDrawing.x[subRoot] = (treeDrawing.x[root])+effectiveROOTSIZE;
				if (drawDiagonalRoot)
					treeDrawing.y[subRoot] -= effectiveROOTSIZE;
			}
			calcTerminalLocsPushHiddenInCollapsed(treeDrawing, tree, root, treeDisplay.getOrientation());
			calcInternalLocsPushHiddenInCollapsed(treeDrawing, tree, root);
			treeDisplay.scaling=treeDisplay.nodeLocsParameters[scaling];
			if (extraDepthAtRootRequested>0 && branchesProportionalToLength){
				int scaled = (int)(extraDepthAtRootRequested*treeDisplay.nodeLocsParameters[scaling]);
				if (treeDisplay.getOrientation()==TreeDisplay.UP)
					treeDrawing.translateAll(0, -scaled);
				else if (treeDisplay.getOrientation()==TreeDisplay.DOWN)
					treeDrawing.translateAll(0, scaled);
				else if (treeDisplay.getOrientation()==TreeDisplay.RIGHT)
					treeDrawing.translateAll(scaled, 0);
				else if (treeDisplay.getOrientation()==TreeDisplay.LEFT)
					treeDrawing.translateAll(-scaled, 0);
			}
			treeDrawing.translateAll(treeDisplay.effectiveFieldLeftMargin(), treeDisplay.effectiveFieldTopMargin());
			calculateScale(treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.nodeLocsParameters[scaling], tree, drawnRoot, treeDisplay, extraDepthAtRootRequested);
		}
	}
	private void drawString(Graphics g, String s, double x, double y, boolean upDown){
		if (g == null || StringUtil.blank(s))
			return;
		try {
			FontMetrics fm = g.getFontMetrics();
			//if upDown, then move y up by half of ascent
			if (upDown){
				y += fm.getMaxAscent()/2;
			}
			//if not, then move x left by half of string length
			else {
				x -= fm.stringWidth(s)/2;
			}

			Graphics2D g2 = (Graphics2D)g;
			g2.drawString(s,(float) x, (float)y);
		}
		catch (Exception e){
		}
	}
	/*.................................................................................................................*/
	//returned are startingX, starting Y, ending X, ending Y, starting scale value, ending scale value
	public void calculateScale(double totalTreeHeight, double totalScaleHeight, double scaling, Tree tree, int drawnRoot, TreeDisplay treeDisplay, double extraDepthAtRootRequested) {
		TreeDrawing treeDrawing = treeDisplay.getTreeDrawing();
		double buffer = treeDisplay.getTaxonSpacing()/4;
		double[] scaleValues = null;
		//Debugg.println CHECK FIXED SCALING
		if (treeDisplay.getOrientation()==TreeDisplay.UP) {
			double yBase = (totalScaleHeight-totalTreeHeight+extraDepthAtRootRequested)*scaling +treeDisplay.getTreeDrawing().y[drawnRoot];
			double xPos = treeDisplay.getTreeDrawing().x[tree.rightmostTerminalOfNode(drawnRoot)] + buffer;
			if (fixedScale)
				scaleValues = new double[]{xPos, yBase- (totalScaleHeight*scaling), xPos,yBase+(fixedDepth-totalScaleHeight)*scaling, 0, totalScaleHeight};
			else
				scaleValues = new double[]{xPos, yBase- (totalScaleHeight*scaling), xPos,yBase, 0, totalScaleHeight};
		}
		else if (treeDisplay.getOrientation()==TreeDisplay.DOWN) {
			double yBase = treeDrawing.y[drawnRoot] - extraDepthAtRootRequested*scaling;
			//	if (fixedScale)
			//		yBase += (totalTreeHeight - fixedDepth)*scaling;
			double xPos = treeDisplay.getTreeDrawing().x[tree.rightmostTerminalOfNode(drawnRoot)]+ buffer;
			if (fixedScale)
				scaleValues = new double[]{xPos, yBase+ (totalScaleHeight*scaling), xPos,yBase-(fixedDepth-totalScaleHeight)*scaling, 0, totalScaleHeight};
			else
				scaleValues = new double[]{xPos, yBase+ (totalScaleHeight*scaling), xPos,yBase, 0, totalScaleHeight};
		}
		else if (treeDisplay.getOrientation()==TreeDisplay.LEFT) {
			double yPos = treeDisplay.getTreeDrawing().y[tree.rightmostTerminalOfNode(drawnRoot)]+ buffer;
			//if fixed then base is centered on root!
			double xBase = (totalScaleHeight-totalTreeHeight +extraDepthAtRootRequested)*scaling +treeDisplay.getTreeDrawing().x[drawnRoot];
			if (fixedScale)
				scaleValues = new double[]{xBase- (totalScaleHeight*scaling), yPos, xBase+(fixedDepth-totalScaleHeight)*scaling,yPos, 0, totalScaleHeight};
			else
				scaleValues = new double[]{xBase- (totalScaleHeight*scaling), yPos, xBase,yPos, 0, totalScaleHeight};
		}
		else if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
			double yPos = treeDisplay.getTreeDrawing().y[tree.rightmostTerminalOfNode(drawnRoot)] + buffer;
			double xBase = treeDrawing.x[drawnRoot] - extraDepthAtRootRequested*scaling;
			if (fixedScale)
				scaleValues = new double[]{xBase+ (totalScaleHeight*scaling), yPos, xBase-(fixedDepth-totalScaleHeight)*scaling,yPos, 0, totalScaleHeight};
			else
				scaleValues = new double[]{xBase+ (totalScaleHeight*scaling), yPos, xBase,yPos, 0, totalScaleHeight};
		}

		if (scaleValues != null)
			treeDisplay.setScale(scaleValues);
	}

	/*.................................................................................................................*/
	public void drawGrid(double totalTreeHeight, double totalScaleHeight, double scaling, TreeDisplay treeDisplay, Graphics g) {
		if (g == null)
			return;
		if (treeDisplay.inhibitDefaultScaleBar ||  treeDisplay.getScale() == null)
			return;
		boolean narrowScaleOnly = !broadScale.getValue();
		boolean rulerOnly = false;
		int rulerWidth = 8;
		Color c=g.getColor();

		Color smallTickColor = Color.lightGray;
		Color bigTickColor = Color.darkGray;
		//returned are startingX, starting Y, ending X, ending Y, starting scale value, ending scale value
		double startX = treeDisplay.getScale()[0];
		double startY = treeDisplay.getScale()[1];
		double endX = treeDisplay.getScale()[2];
		double endY = treeDisplay.getScale()[3];
		double startScale = treeDisplay.getScale()[4];
		double endScale = treeDisplay.getScale()[5];

		g.setColor(smallTickColor);
		int scaleBuffer = 28;
		TreeDrawing treeDrawing = treeDisplay.getTreeDrawing();
		int buffer = 4;
		double log10 = Math.log(10.0);
		double hundredthHeight = Math.exp(log10* ((int) (Math.log(totalScaleHeight)/log10)-1));
		if (totalScaleHeight/hundredthHeight <20.0)
			hundredthHeight /= 10.0;
		int countTenths = 0;
		double thisHeight = totalScaleHeight + hundredthHeight;
		if (treeDisplay.getOrientation()==TreeDisplay.UP) {
			double base = endY;
			double leftEdge = startX;
			double rightEdge = startX + scaleBuffer;
			//	if (fixedScale)
			//		base -= (totalTreeHeight - fixedDepth)*scaling;
			if (narrowScaleOnly)
				leftEdge = rightEdge - 10;
			while ( thisHeight>=0) {
				if (countTenths % 10 == 0)
					g.setColor(bigTickColor);
				else
					g.setColor(smallTickColor);
				thisHeight -= hundredthHeight;
				if (rulerOnly)
					GraphicsUtil.drawLine(g,rightEdge-rulerWidth, (base- (thisHeight*scaling)), rightEdge,  (base- (thisHeight*scaling)));
				else
					GraphicsUtil.drawLine(g,leftEdge, (base- (thisHeight*scaling)), rightEdge,  (base- (thisHeight*scaling)));
				if (countTenths % 10 == 0)
					drawString(g, MesquiteDouble.toStringInRange(totalScaleHeight - thisHeight, totalScaleHeight), rightEdge + buffer, (base- (thisHeight*scaling)), true);

				countTenths ++;
			}
			if (rulerOnly)
				GraphicsUtil.drawLine(g,rightEdge, (base), rightEdge,  (base- (totalScaleHeight*scaling)));
		}
		else if (treeDisplay.getOrientation()==TreeDisplay.DOWN) {
			double leftEdge = startX;
			double rightEdge = startX + scaleBuffer;
			if (narrowScaleOnly)
				leftEdge = rightEdge - 10;
			double base = endY;
			while ( thisHeight>=0) {
				if (countTenths % 10 == 0)
					g.setColor(bigTickColor);
				else
					g.setColor(smallTickColor);
				thisHeight -= hundredthHeight;
				if (rulerOnly)
					GraphicsUtil.drawLine(g,rightEdge-rulerWidth, (base+ (thisHeight*scaling)), rightEdge,  (base+ (thisHeight*scaling)));
				else
					GraphicsUtil.drawLine(g,leftEdge, (base+ (thisHeight*scaling)), rightEdge,  (base+ (thisHeight*scaling)));
				if (countTenths % 10 == 0)
					drawString(g, MesquiteDouble.toStringInRange(totalScaleHeight - thisHeight, totalScaleHeight), rightEdge + buffer, (base+ (thisHeight*scaling)), true);
				countTenths ++;
			}
			if (rulerOnly)
				GraphicsUtil.drawLine(g,rightEdge, (base), rightEdge,  (base+ (totalScaleHeight*scaling)));
		}
		else if (treeDisplay.getOrientation()==TreeDisplay.LEFT) {
			prepareFontMetrics(g.getFont(), g);
			int textHeight = fm.getAscent();
			double leftEdge = startY;
			double rightEdge = startY+ scaleBuffer;
			if (narrowScaleOnly)
				leftEdge = rightEdge - 10;

			double base = endX;
			while ( thisHeight>=0) {
				if (countTenths % 10 == 0)
					g.setColor(bigTickColor);
				else
					g.setColor(smallTickColor);
				thisHeight -= hundredthHeight;
				if (rulerOnly)
					GraphicsUtil.drawLine(g,(base- (thisHeight*scaling)), rightEdge,  (base- (thisHeight*scaling)),  rightEdge-rulerWidth);
				else
					GraphicsUtil.drawLine(g,(base- (thisHeight*scaling)), rightEdge,  (base- (thisHeight*scaling)),  leftEdge);
				if (countTenths % 10 == 0)
					drawString(g, MesquiteDouble.toStringInRange(totalScaleHeight - thisHeight, totalScaleHeight), (base- (thisHeight*scaling)), rightEdge + buffer + textHeight, false);
				countTenths ++;
			}
			if (rulerOnly)
				GraphicsUtil.drawLine(g,(base), rightEdge, (base- (totalScaleHeight*scaling)),rightEdge);
		}
		else if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
			prepareFontMetrics(g.getFont(), g);
			int textHeight = fm.getAscent();
			double leftEdge = startY;
			double rightEdge = startY+ scaleBuffer;
			if (narrowScaleOnly) {
				leftEdge = rightEdge - 10;
			}
			double base = endX;
			while ( thisHeight>=0) {
				if (countTenths % 10 == 0)
					g.setColor(bigTickColor);
				else
					g.setColor(smallTickColor);
				thisHeight -= hundredthHeight;
				if (rulerOnly)
					GraphicsUtil.drawLine(g,(base+ (thisHeight*scaling)), rightEdge-rulerWidth,  (base+ (thisHeight*scaling)),  rightEdge);
				else
					GraphicsUtil.drawLine(g,(base+ (thisHeight*scaling)), leftEdge,  (base+ (thisHeight*scaling)),  rightEdge);
				if (countTenths % 10 == 0)
					drawString(g, MesquiteDouble.toStringInRange(totalScaleHeight - thisHeight, totalScaleHeight), (base+ (thisHeight*scaling)), rightEdge + buffer + textHeight, false);
				countTenths ++;
			}
			if (rulerOnly)
				GraphicsUtil.drawLine(g,(base), rightEdge, (base+ (totalScaleHeight*scaling)),rightEdge);
		}		

		if (c !=null)
			g.setColor(c);
	}

	public double getFixedTaxonDistance() {
		return fixedTaxonDistance;
	}

	public void setFixedTaxonDistance(double fixedTaxonDistance) {
		this.fixedTaxonDistance = fixedTaxonDistance;
	}
}


class NodeLocsExtra extends TreeDisplayExtra implements TreeDisplayBkgdExtra {
	NodeLocsStandard locsModule;
	MesquiteWindow window = null;
	public NodeLocsExtra (NodeLocsStandard ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		locsModule = ownerModule;
		treeDisplay.setFixedTaxonSpacing(locsModule.fixedTaxonDistance);

	}
	/*.................................................................................................................*/
	public   String writeOnTree(Tree tree, int drawnRoot) {
		return null;
	}

	TreeDisplayRequests blank = new TreeDisplayRequests();
	/*.................................................................................................................*/
	/* The TreeDisplayRequests object has public int fields leftBorder, topBorder, rightBorder, bottomBorder (in pixels and in screen orientation)
	 * and a public double field extraDepthAtRoot (in branch lengths units and rootward regardless of screen orientation) */
	public TreeDisplayRequests getRequestsOfTreeDisplay(Tree tree, TreeDrawing treeDrawing){
		if (treeDisplay.getOrientation() == TreeDisplay.UP){
			return new TreeDisplayRequests(NodeLocsStandard.minPortBorder, NodeLocsStandard.minTipwardBorder, NodeLocsStandard.minStarboardBorder, NodeLocsStandard.minRootwardBorder, 0, 0);
		}
		else if (treeDisplay.getOrientation() == TreeDisplay.DOWN){
			return new TreeDisplayRequests(NodeLocsStandard.minStarboardBorder, NodeLocsStandard.minRootwardBorder, NodeLocsStandard.minPortBorder, NodeLocsStandard.minTipwardBorder, 0, 0);
		}
		else if (treeDisplay.getOrientation() == TreeDisplay.RIGHT){
			return new TreeDisplayRequests(NodeLocsStandard.minRootwardBorder, NodeLocsStandard.minPortBorder, NodeLocsStandard.minTipwardBorder, NodeLocsStandard.minStarboardBorder, 0, 0); 
		}
		else if (treeDisplay.getOrientation() == TreeDisplay.LEFT){
			return new TreeDisplayRequests(NodeLocsStandard.minTipwardBorder, NodeLocsStandard.minStarboardBorder, NodeLocsStandard.minRootwardBorder, NodeLocsStandard.minPortBorder, 0, 0);
		}
		return new TreeDisplayRequests();
	}

	void drawTranslatedRect(Graphics g, int x, int y, int w, int h, Color c){
		g.setColor(c);
		int offX = treeDisplay.effectiveFieldLeftMargin();
		int offY = treeDisplay.effectiveFieldTopMargin();
		g.drawRect(x+offX, y+offY, w, h); 
	}
	/*.................................................................................................................*/
	boolean showRectangles = false; //see also drawDebuggingLines in TreeDrawing
	public   void drawUnderTree(Tree tree, int drawnRoot, Graphics g) {

		if (showRectangles){  //rectangles
			drawTranslatedRect(g, 2, 2, treeDisplay.getField().width, treeDisplay.getField().height, Color.green);
			drawTranslatedRect(g, 2, 2, treeDisplay.effectiveFieldWidth(), treeDisplay.effectiveFieldHeight(), Color.cyan);

			g.setColor(Color.blue);
			g.drawRect(2, 2, treeDisplay.effectiveFieldLeftMargin()-2, treeDisplay.effectiveFieldTopMargin()-2);
			g.setColor(Color.red);
			g.drawRect(treeDisplay.getField().width - treeDisplay.effectiveFieldRightMargin(), treeDisplay.getField().height - treeDisplay.effectiveFieldBottomMargin(), treeDisplay.effectiveFieldRightMargin()-2, treeDisplay.effectiveFieldBottomMargin()-2);


			int xTips = treeDisplay.effectiveFieldWidth()+treeDisplay.effectiveFieldLeftMargin()-treeDisplay.getTipsMargin();
			g.setColor(ColorDistribution.lightBlue);
			g.fillRect(xTips, treeDisplay.effectiveFieldTopMargin(), treeDisplay.getTaxonNameDistanceFromTip(), treeDisplay.effectiveFieldHeight());

			//	g.setColor(ColorDistribution.lightBlue);

			//	g.fillRect(xTips + treeDisplay.getTaxonNameDistanceFromTip(), treeDisplay.effectiveFieldTopMargin(), treeDisplay.totalTipsFieldDistance(), treeDisplay.effectiveFieldHeight());
		}
		if (locsModule.showScaleConsideringAuto(tree,  treeDisplay)) 
			locsModule.drawGrid(treeDisplay.nodeLocsParameters[locsModule.totalHeight], treeDisplay.fixedDepthScale, treeDisplay.nodeLocsParameters[locsModule.scaling], treeDisplay, g);
	}
	/*.................................................................................................................*/
	public   void printUnderTree(Tree tree, int drawnRoot, Graphics g) {
		drawUnderTree(tree, drawnRoot, g);
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
	}
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
	}



}



