/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.ColorBranches;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Vector;

import mesquite.lib.CommandChecker;
import mesquite.lib.Commandable;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteModule;
import mesquite.lib.Notification;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.duties.TreeDisplayAssistantI;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDisplayExtra;
import mesquite.lib.tree.TreeTool;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.MesquiteMenuItemSpec;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.lib.ui.MesquiteWindow;

/* ======================================================================== */
public class ColorBranches extends TreeDisplayAssistantI {
	Vector extras;
	String branchColor = ColorDistribution.hexFromColor(Color.red);
	String colorString = "Color Red";
	String savedColor = branchColor;
	MesquiteBoolean removeColor;
	public String getFunctionIconPath(){
		return getPath() + "color.gif";
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		extras = new Vector();
		MesquiteSubmenuSpec mss = addSubmenu(null, "Branch paint color", makeCommand("setColor",  this), ColorDistribution.standardColorNames);
		removeColor = new MesquiteBoolean(false);
		addCheckMenuItem(null, "Remove color", makeCommand("removeColor",  this), removeColor);
		addMenuItem(null, "Remove all color from branches", makeCommand("removeAllColor",  this));
		return true;
	} 
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		ColorToolExtra newPj = new ColorToolExtra(this, treeDisplay);
		extras.addElement(newPj);
		return newPj;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setColorRGB " + branchColor);
		temp.addLine("removeColor " + removeColor.toOffOnString());
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the color to be used to paint branches", "[name of color]", commandName, "setColor")) {
			int bc = ColorDistribution.standardColorNames.indexOf(parser.getFirstToken(arguments)); 
			if (bc >=0 && MesquiteInteger.isCombinable(bc)){
				removeColor.setValue(false);
				branchColor = ColorDistribution.hexFromColor(bc); //ColorDistribution.standardColors[bc];
				savedColor = ColorDistribution.hexFromColor(bc); 
				colorString = "Color " + ColorDistribution.standardColorNames.getValue(bc);
				for (int i =0; i<extras.size(); i++){
					ColorToolExtra e = (ColorToolExtra)extras.elementAt(i);
					e.setToolText(colorString);
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets the color to be used to paint branches", "[name of color]", commandName, "setColorRGB")) {
			String bcRGB = parser.getFirstToken(arguments); 
			if (StringUtil.notEmpty(bcRGB)){
				removeColor.setValue(false);
				branchColor = bcRGB;
				savedColor = bcRGB;
				colorString = "Color " + ColorDistribution.standardName(bcRGB);
				for (int i =0; i<extras.size(); i++){
					ColorToolExtra e = (ColorToolExtra)extras.elementAt(i);
					e.setToolText(colorString);
				}
		
			}
		}
		else if (checker.compare(this.getClass(), "Removes color from all the branches", null, commandName, "removeAllColor")) {
			for (int i =0; i<extras.size(); i++){
				ColorToolExtra e = (ColorToolExtra)extras.elementAt(i);
				e.removeAllColor();
			}
		}
		else if (checker.compare(this.getClass(), "Sets the paint brush so that it removes colors from any branches touched", null, commandName, "removeColor")) {
			if (StringUtil.blank(arguments))
				removeColor.setValue(!removeColor.getValue());
			else
				removeColor.toggleValue(parser.getFirstToken(arguments));

			if (removeColor.getValue()) {
				colorString = "Remove color";
				branchColor = null;
			}
			else {
				//colorString = "Color " + ColorDistribution.standardColorNames.getValue((int)branchColor);
				branchColor = savedColor;
			}
			for (int i =0; i<extras.size(); i++){
				ColorToolExtra e = (ColorToolExtra)extras.elementAt(i);
				e.setToolText(colorString);
			}

		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Color Branches";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Provides a tool with which to color branches in a tree window.";
	}
}

/* ======================================================================== */
class ColorToolExtra extends TreeDisplayExtra implements Commandable  {
	public TreeTool colorTool;
	MesquiteMenuItemSpec hideMenuItem = null;
	ColorBranches colorModule;
	MesquiteTree tree;
	public ColorToolExtra (ColorBranches ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		colorModule = ownerModule;
		colorTool = new TreeTool(this, "ColorBranches", ownerModule.getPath(), "color.gif", 1,1,colorModule.colorString, "This tool colors the branches of the tree.  This has cosmetic effect only.  The color painted can be changed using the Branch Colors submenu.  Control-click colors the branch and the clade desdendant from it; Shift-click shrink wraps the color. ");
		colorTool.setTouchedCommand(MesquiteModule.makeCommand("colorBranch",  this));
		colorTool.setTouchedTaxonCommand(MesquiteModule.makeCommand("colorTaxon",  this));
		if (ownerModule.containerOfModule() instanceof MesquiteWindow) {
			((MesquiteWindow)ownerModule.containerOfModule()).addTool(colorTool);
			colorTool.setPopUpOwner(ownerModule);
			ownerModule.setUseMenubar(false); //menu available by touching button
		}
	}
	/*.................................................................................................................*/
	private boolean anyColored(MesquiteTree tree, int node, String targetColor){
		if (tree.getColorAsHexString(node) != null && tree.getColorAsHexString(node).equals(targetColor))
			return true;
		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
			if (anyColored(tree, daughter, targetColor))
				return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	private void wrapColors(MesquiteTree tree, int node, boolean coloredBelow, String targetColor){
		if (coloredBelow)
			tree.setColor( node, colorModule.branchColor);
		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
			wrapColors(tree, daughter, coloredBelow || (tree.getColorAsHexString(node) != null && tree.getColorAsHexString(node).equals(targetColor)), targetColor);
		}
	}
	/*.................................................................................................................*/
	private void coloredTwiceAbove(MesquiteTree tree, int node, String targetColor){
		int numColoredAbove = 0;
		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
			coloredTwiceAbove(tree, daughter, targetColor);
			if (anyColored(tree, daughter, targetColor))
				numColoredAbove ++;
		}
		if (numColoredAbove>1)
			tree.setColor(node, colorModule.branchColor);
	}
	/*.................................................................................................................*/
	void setToolText(String s){
		colorTool.setDescription(s);
		if (colorModule.containerOfModule() instanceof MesquiteWindow) 
			((MesquiteWindow)colorModule.containerOfModule()).toolTextChanged();

	}
	/*.................................................................................................................*/
	private void shrinkWrapSelections(MesquiteTree tree, int node){
		coloredTwiceAbove(tree, node, colorModule.branchColor);
		wrapColors(tree, node, false, colorModule.branchColor);
	}
	/*.................................................................................................................*/
	private void removeColorClade(MesquiteTree tree, int node){
		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
			removeColorClade(tree, daughter);
		}
		removeColor(node);
	}
	/*.................................................................................................................*/
	private void colorClade(MesquiteTree tree, int node){
		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
			colorClade(tree, daughter);
		}
		setColor(node);
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		if (tree instanceof MesquiteTree)
			this.tree = (MesquiteTree)tree;
		else
			this.tree = null;
	}

	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		if (tree instanceof MesquiteTree)
			this.tree = (MesquiteTree)tree;
		else
			this.tree = null;
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		//remember which colored
		return null;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	void removeAllColor(){
		if (tree == null)
			return;
		removeColorClade(tree, tree.getRoot());
		tree.notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_CHANGED));
		treeDisplay.pleaseUpdate(false);

	}
	private void setColor(int node){
		if (tree == null)
			return;
		if (!colorModule.removeColor.getValue())
			tree.setColor(node, colorModule.branchColor);
		else
			tree.setColor(node, (String)null);
	}
	private void removeColor(int node){
		tree.setColor(node, (String)null);
	}
	
	
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 

		if (checker.compare(this.getClass(), "Color branch with current paint color", "[branch number][x coordinate touched][y coordinate touch][modifiers]", commandName, "colorBranch")) {
			int branchFound= MesquiteInteger.fromFirstToken(arguments, pos);
			if (branchFound<=0)
				return null;
			if (arguments.indexOf("shift")>=0) {  //color smallest containing clade
				if (!ColorDistribution.hexColorsEqual(tree.getColorAsHexString(branchFound), colorModule.branchColor)) {
					setColor(branchFound);
					shrinkWrapSelections(tree, tree.getRoot());
				}
				else
					tree.deassignAllColor();
				tree.notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_CHANGED));
				treeDisplay.pleaseUpdate(false);
			}
			else if (arguments.indexOf("control")>=0) {  //color clade
				colorClade(tree, branchFound);
				tree.notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_CHANGED));
				treeDisplay.pleaseUpdate(false);
			}
			else if (branchFound >0) {
				if (!ColorDistribution.hexColorsEqual(tree.getColorAsHexString(branchFound), colorModule.branchColor)) 
					setColor(branchFound);
				else
					removeColor(branchFound);
				tree.notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_CHANGED));
				treeDisplay.pleaseUpdate(false);
			}
		}
		else if (checker.compare(this.getClass(), "Color taxon with current paint color", "[taxon number][x coordinate touched][y coordinate touch][modifiers]", commandName, "colorTaxon")) {
			int taxon= MesquiteInteger.fromFirstToken(arguments, pos);
			if (taxon<=0)
				return null;
			setTaxonColor(taxon);
			
			tree.getTaxa().notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_CHANGED));
			treeDisplay.pleaseUpdate(false);
			
		}
		return null;
	}
	private void setTaxonColor(int taxon){
		if (tree == null)
			return;
		Taxa taxa = tree.getTaxa();
		if (!colorModule.removeColor.getValue()){
			taxa.setColor(taxon, colorModule.branchColor);
		}
		else
			taxa.setColor(taxon, (String)null);
	}
	public void turnOff() {
		colorModule.extras.removeElement(this);
		super.turnOff();
	}
}




