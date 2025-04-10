/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.BasicDrawTaxonNames;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.TaxaGroup;
import mesquite.lib.taxa.TaxaPartition;
import mesquite.lib.taxa.Taxon;
import mesquite.lib.tree.AdjustableTree;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.SquareTipDrawer;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDisplayExtra;
import mesquite.lib.tree.TreeDrawing;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.FontUtil;
import mesquite.lib.ui.GraphicsUtil;
import mesquite.lib.ui.MQPanel;
import mesquite.lib.ui.MesquiteMenuItemSpec;
import mesquite.lib.ui.MesquiteMenuSpec;
import mesquite.lib.ui.MesquiteSubmenu;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lib.ui.TextRotator;
import mesquite.trees.lib.*;


/** Draws the taxon names in a tree drawing */
public class BasicDrawTaxonNames extends DrawNamesTreeDisplay {
	/*.................................................................................................................*/
	public String getName() {
		return "Basic Draw Names for Tree Display";
	}

	public String getExplanation() {
		return "Draws taxon names on a tree.  Chooses orientation of names according to orientation of tree." ;
	}
	/*.................................................................................................................*/


	protected TreeDisplay treeDisplay;
	protected TreeDrawing treeDrawing;
	public TaxonPolygon[] namePolys;
	protected TextRotator textRotator;
	protected Tree tree;
	protected Graphics gL;
	protected int separation = 10;
	protected Font currentFont = null;
	protected Font currentFontBOLD = null;
	protected Font currentFontBOLDITALIC = null;

	protected Font[] currentFontsCollapsed= new Font[8]; //0 normal, 1bold, 2 italic, 3 bold+italic, 4 biggish, 5 bold biggish, 6 italic biggish, 7 bold italic biggish

	protected Font currentFontBIG = null;
	protected Font currentFontBIGBOLD = null;
	protected int bigFontChoice = TreeDisplay.sTHM_BIGNAME;
	protected String myFont = null;
	protected int myFontSize = -1;
	protected FontMetrics fm;
	protected int rise;
	protected int descent;
	protected int oldNumTaxa=0;
	protected MesquiteString fontSizeName, fontName;
	protected MesquiteBoolean shadePartition, showFootnotes;
	/*New code added Feb.15.07 centerNodeLabels oliver*/ //TODO: delete new code comments
	protected MesquiteBoolean showNodeLabels, showTaxonNames, centerNodeLabels; /*deleted centerNodeLables declaration Feb.26.07 oliver*/
	/*end new code added Feb.15.07 oliver*/
	protected MesquiteString fontColorName;
	protected Color fontColor=Color.black;
	protected Color fontColorLight = Color.gray;
	protected NumberForTaxon shader = null;
	protected TaxonNameStyler colorerTask = null;
	protected int longestString = 0;
	//protected MesquiteMenuItemSpec offShadeMI = null;
	/* New code added Feb.26.07 oliver*/ //TODO: delete new code comments
	protected MesquiteMenuItemSpec centerNodeLabelItem = null;
	/* End new code Feb.26.07 oliver*/
	protected double[] shades = null;
	protected double minValue, maxValue;
	double namesAngle = MesquiteDouble.unassigned;
	MesquiteCommand tNC;
	MesquiteString colorerName = null;
	MesquiteMenuItemSpec angleMenuItem;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		currentFont = MesquiteWindow.defaultFont;
		resetFonts();
		MesquiteMenuSpec textMenu = findMenuAmongEmployers("Text");
		MesquiteMenuSpec colorMenu = findMenuAmongEmployers("Color");
		MesquiteSubmenuSpec msf = FontUtil.getFontSubmenuSpec(textMenu, "Font", this, this);

		msf.setSelected(fontName);
		MesquiteSubmenuSpec mss = addSubmenu(textMenu, "Font Size", makeCommand("setFontSize", this), MesquiteSubmenu.getFontSizeList());
		mss.setList(MesquiteSubmenu.getFontSizeList());
		mss.setDocumentItems(false);
		mss.setSelected(fontSizeName);
		fontColorName = new MesquiteString("Black");
		MesquiteSubmenuSpec mmis = addSubmenu(colorMenu, "Default Font Color", makeCommand("setColor",  this));
		mmis.setList(ColorDistribution.standardColorNames);
		mmis.setSelected(fontColorName);
		colorerTask =  (TaxonNameStyler)hireNamedEmployee(TaxonNameStyler.class, "#NoColorForTaxon");
		tNC = makeCommand("setTaxonNameStyler",  this);
		colorerTask.setHiringCommand(tNC);

		MesquiteSubmenuSpec mmTNC = addSubmenu(colorMenu, "Color Of Taxon Names", tNC);
		mmTNC.setList(TaxonNameStyler.class);
		colorerName = new MesquiteString(colorerTask.getName());
		mmTNC.setSelected(colorerName);

		MesquiteSubmenuSpec namesMenu = addSubmenu(null, "Taxon Names");
		shadePartition = new MesquiteBoolean(false);
		addCheckMenuItem(colorMenu, "Taxon Background Color by Group", makeCommand("toggleShadePartition", this), shadePartition);
		showFootnotes = new MesquiteBoolean(true);
		addCheckMenuItemToSubmenu(textMenu, namesMenu, "Mark Footnotes in Taxon Name", makeCommand("toggleShowFootnotes", this), showFootnotes);
		showNodeLabels = new MesquiteBoolean(true);


		MesquiteSubmenuSpec branchNamesMenu = addSubmenu(null, "Node/Branch Names");
		/*addItemToSubmenu(textMenu, namesMenu, "Shade by Value...", makeCommand("shadeByNumber",  this));
		offShadeMI = addItemToSubmenu(textMenu, namesMenu, "Turn off Shading", makeCommand("offShading",  this));
		offShadeMI.setEnabled(false); */
		addCheckMenuItemToSubmenu(textMenu, branchNamesMenu, "Show Node/Branch Names", makeCommand("toggleNodeLabels", this), showNodeLabels);
		showTaxonNames = new MesquiteBoolean(true);
		addCheckMenuItemToSubmenu(textMenu, namesMenu, "Show Taxon Names", makeCommand("toggleShowNames", this), showTaxonNames);
		angleMenuItem = addMenuItem(textMenu, "Taxon Name Angle...", makeCommand("namesAngle", this));

		centerNodeLabels = new MesquiteBoolean(false);
		centerNodeLabelItem = addCheckMenuItemToSubmenu(textMenu, branchNamesMenu, "Center Branch Names", makeCommand("toggleCenterNodeNames", this), centerNodeLabels);
		centerNodeLabelItem.setEnabled(true);


		addMenuItem(textMenu, "-", null);
		addSubmenu(textMenu, "Alter Node Names", makeCommand("alterBranchNames",  this), BranchNamesAlterer.class);

		return true;
	}
	void resetFonts(){
		currentFontBOLD = new Font(currentFont.getName(), Font.BOLD, currentFont.getSize());
		currentFontBOLDITALIC = new Font(currentFont.getName(), Font.BOLD+Font.ITALIC, currentFont.getSize());
		currentFontBIG = new Font(currentFont.getName(), Font.PLAIN, (int)(currentFont.getSize()*highlightMultiplier()));
		currentFontBIGBOLD = new Font(currentFont.getName(), Font.BOLD, (int)(currentFont.getSize()*highlightMultiplier()));
		fontName = new MesquiteString(MesquiteWindow.defaultFont.getName());
		fontSizeName = new MesquiteString(Integer.toString(MesquiteWindow.defaultFont.getSize()));
		currentFontsCollapsed[0] =currentFont; //0 normal, 1bold, 2 italic, 3 bold+italic, 4 biggish, 5 bold biggish, 6 italic biggish, 7 bold italic biggish
		currentFontsCollapsed[1] =currentFontBOLD; //0 normal, 1bold, 2 italic, 3 bold+italic, 4 biggish, 5 bold biggish, 6 italic biggish, 7 bold italic biggish
		currentFontsCollapsed[2] =new Font(currentFont.getName(), Font.ITALIC, currentFont.getSize()); //0 normal, 1bold, 2 italic, 3 bold+italic, 4 biggish, 5 bold biggish, 6 italic biggish, 7 bold italic biggish
		currentFontsCollapsed[3] =currentFontBOLDITALIC; //0 normal, 1bold, 2 italic, 3 bold+italic, 4 biggish, 5 bold biggish, 6 italic biggish, 7 bold italic biggish
		currentFontsCollapsed[4] =new Font(currentFont.getName(), Font.PLAIN, (int)(currentFont.getSize()*1.25)); //0 normal, 1bold, 2 italic, 3 bold+italic, 4 biggish, 5 bold biggish, 6 italic biggish, 7 bold italic biggish
		currentFontsCollapsed[5] =new Font(currentFont.getName(), Font.BOLD, (int)(currentFont.getSize()*1.25)); //0 normal, 1bold, 2 italic, 3 bold+italic, 4 biggish, 5 bold biggish, 6 italic biggish, 7 bold italic biggish
		currentFontsCollapsed[6] =new Font(currentFont.getName(), Font.ITALIC, (int)(currentFont.getSize()*1.25)); //0 normal, 1bold, 2 italic, 3 bold+italic, 4 biggish, 5 bold biggish, 6 italic biggish, 7 bold italic biggish
		currentFontsCollapsed[7] =new Font(currentFont.getName(), Font.BOLD+Font.ITALIC, (int)(currentFont.getSize()*1.25)); //0 normal, 1bold, 2 italic, 3 bold+italic, 4 biggish, 5 bold biggish, 6 italic biggish, 7 bold italic biggish
	}

	public void endJob(){
		treeDisplay = null;
		treeDrawing = null;
		textRotator = null;
		super.endJob();
	}
	/*.................................................................................................................*/
	double highlightMultiplier(){
		return (bigFontChoice+3)/4.0;
	}
	/*.................................................................................................................*/
	/** A method called immediately after the file has been read in or completely set up (if a new file).*/
	public void fileReadIn(MesquiteFile f) {
		if (treeDisplay != null)
			treeDisplay.forceRepaint();
	}
	/*.................................................................................................................*/
	/** return whether or not this module should have snapshot saved when saving a macro given the current snapshot mode.*/
	public boolean satisfiesSnapshotMode(){
		return (MesquiteTrunk.snapshotMode == Snapshot.SNAPALL || MesquiteTrunk.snapshotMode == Snapshot.SNAPDISPLAYONLY);
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		if (myFont!=null)
			temp.addLine("setFont " + StringUtil.tokenize(myFont));  //TODO: this causes problem since charts come before tree window
		if (myFontSize>0)
			temp.addLine("setFontSize " + myFontSize);  //TODO: this causes problem since charts come before tree window
		temp.addLine("setColor " + ParseUtil.tokenize(fontColorName.toString()));  //TODO: this causes problem since charts come before tree window
		temp.addLine("setTaxonNameStyler " , colorerTask);
		if (shader != null)
			temp.addLine("shadeByNumber ", shader);
		temp.addLine("toggleShadePartition " + shadePartition.toOffOnString());
		temp.addLine("toggleShowFootnotes " + showFootnotes.toOffOnString());
		temp.addLine("toggleNodeLabels " + showNodeLabels.toOffOnString());
		/*New code added Feb.15.07 oliver*/ //TODO: delete new code comments
		temp.addLine("toggleCenterNodeNames " + centerNodeLabels.toOffOnString());
		/*End new code added Feb.15.07 oliver*/
		temp.addLine("toggleShowNames " + showTaxonNames.toOffOnString());
		temp.addLine("namesAngle " + MesquiteDouble.toString(namesAngle));
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {

		if (checker.compare(this.getClass(), "Sets the angle names are shown at in default UP orientation", "[angle in degrees clockwise from horizontal; ? = default]", commandName, "namesAngle")) {
			if (arguments == null && !MesquiteThread.isScripting()){
				namesAngle = queryAngleRadians();  //could be unassigned
				parametersChanged();
				/**/
			}
			else {

				double angle = MesquiteDouble.fromString(parser.getFirstToken(arguments));
				namesAngle = angle;
				parametersChanged();
			}
		}

		else if (checker.compare(this.getClass(), "Toggles whether to show taxon names colored by partition", "[on or off]", commandName, "toggleColorPartition")) { //for backwards compatibility
			String s = parser.getFirstToken(arguments);
			if (s != null){
				String replacement = null;
				if (s.equalsIgnoreCase("on")){
					replacement = "#ColorTaxonByPartition";

					TaxonNameStyler temp = (TaxonNameStyler)replaceEmployee(TaxonNameStyler.class, replacement, "How to color taxon names?", colorerTask);
					if (temp!=null) {
						colorerTask = temp;
						colorerName.setValue(colorerTask.getName());
						if (tree != null)
							colorerTask.initialize(tree.getTaxa());
						parametersChanged();
						return colorerTask;
					}
				}
			}
		}
		else if (checker.compare(this.getClass(), "Toggles whether to show taxon names colored by assigned", "[on or off]", commandName, "toggleColorAssigned")) { //for backwards compatibility
			String s = parser.getFirstToken(arguments);
			if (s != null){
				String replacement = null;
				if (s.equalsIgnoreCase("on")){
					replacement = "#ColorTaxonByAssigned";

					TaxonNameStyler temp = (TaxonNameStyler)replaceEmployee(TaxonNameStyler.class, replacement, "How to color taxon names?", colorerTask);
					if (temp!=null) {
						colorerTask = temp;
						colorerName.setValue(colorerTask.getName());
						if (tree != null)
							colorerTask.initialize(tree.getTaxa());
						parametersChanged();
						return colorerTask;
					}
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets the module to be used to choose taxon name colors and styles", "[name of taxon color-style module]", commandName, "setTaxonNameStyler")) {
			TaxonNameStyler temp = (TaxonNameStyler)replaceEmployee(TaxonNameStyler.class, arguments, "How to color taxon names?", colorerTask);
			if (temp!=null) {
				colorerTask = temp;
				colorerName.setValue(colorerTask.getName());
				if (tree != null)
					colorerTask.initialize(tree.getTaxa());
				parametersChanged();
				return colorerTask;
			}
		}
		else if (checker.compare(this.getClass(), "Toggles whether taxon names are given a background color according to their group in the current taxa partition", "[on or off]", commandName, "toggleShadePartition")) {
			boolean current = shadePartition.getValue();
			shadePartition.toggleValue(parser.getFirstToken(arguments));
			if (current!=shadePartition.getValue())
				parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Toggles whether asterisks (for footnotes) and other indications are shown with taxon names", "[on or off]", commandName, "toggleShowFootnotes")) {
			boolean current = showFootnotes.getValue();
			showFootnotes.toggleValue(parser.getFirstToken(arguments));
			if (current!=showFootnotes.getValue())
				parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Hires a module to alter or transform branch names", "[name of module]", commandName, "alterBranchNames")) {
			BranchNamesAlterer bna = (BranchNamesAlterer)hireNamedEmployee(BranchNamesAlterer.class, arguments);
			if (bna!=null && tree instanceof AdjustableTree) {
				boolean success = bna.transformTree((AdjustableTree)tree, null, true);
				if (success){
					//					if (treeSourceLocked())
					//						((AdjustableTree)tree).setName("Untitled Tree");
					//					treeEdited(false);
				}
				fireEmployee(bna); //todo: for branch length estimators, might be good to keep it around, and remembering it if user wants to change parameters
			}
		}
		else if (checker.compare(this.getClass(), "Toggles whether names of internal branches (clades) are shown", "[on or off]", commandName, "toggleNodeLabels")) {
			boolean current = showNodeLabels.getValue();
			showNodeLabels.toggleValue(parser.getFirstToken(arguments));
			if (current!=showNodeLabels.getValue())
				parametersChanged();
		}
		/*New code added Feb.15.07 oliver*/ //TODO: delete new code comments
		else if (checker.compare(this.getClass(), "Toggles whether names of internal branches (clades) are centered", "[on or off]", commandName, "toggleCenterNodeNames")){
			boolean current = centerNodeLabels.getValue();
			centerNodeLabels.toggleValue(parser.getFirstToken(arguments));
			if (current!=centerNodeLabels.getValue())
				parametersChanged();
		}
		/*End new code added Feb.15.07 oliver*/
		else if (checker.compare(this.getClass(), "Toggles whether names of terminal taxa are shown", "[on or off]", commandName, "toggleShowNames")) {
			boolean current = showTaxonNames.getValue();
			showTaxonNames.toggleValue(parser.getFirstToken(arguments));
			if (current!=showTaxonNames.getValue())
				parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets the font used for the taxon names", "[name of font]", commandName, "setFont")) {
			String t= parser.getFirstToken(arguments);
			if (currentFont==null){
				myFont = t;
				fontName.setValue(t);
			}
			else {
				Font fontToSet = new Font (t, currentFont.getStyle(), currentFont.getSize());
				if (fontToSet!= null) {
					myFont = t;
					fontName.setValue(t);
					currentFont = fontToSet;
					resetFonts();
					parametersChanged();
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets the font used for the taxon names", "[name of font]", commandName, FontUtil.setFontOther)) {
			String t=FontUtil.getFontNameFromDialog(containerOfModule());
			if (t!=null) {
				logln("Font chosen: " + t);
				if (currentFont==null){
					myFont = t;
					fontName.setValue(t);
				}
				else {
					Font fontToSet = new Font (t, currentFont.getStyle(), currentFont.getSize());
					if (fontToSet!= null) {
						myFont = t;
						fontName.setValue(t);
						currentFont = fontToSet;
						resetFonts();

						parametersChanged();
					}
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets the font size used for the taxon names", "[size of font]", commandName, "setFontSize")) {
			int fontSize = MesquiteInteger.fromString(arguments, new MesquiteInteger(0));
			if (currentFont==null){
				if (!MesquiteThread.isScripting() && !MesquiteInteger.isPositive(fontSize))
					fontSize = MesquiteInteger.queryInteger(containerOfModule(), "Font Size", "Font Size", 12);
				if (MesquiteInteger.isPositive(fontSize)) {
					myFontSize = fontSize;
					fontSizeName.setValue(Integer.toString(fontSize));
				}
			}
			else {
				if (!MesquiteThread.isScripting() && !MesquiteInteger.isPositive(fontSize))
					fontSize = MesquiteInteger.queryInteger(containerOfModule(), "Font Size", "Font Size", currentFont.getSize());
				if (MesquiteInteger.isPositive(fontSize)) {
					myFontSize = fontSize;
					Font fontToSet = new Font (currentFont.getName(), currentFont.getStyle(), fontSize);
					if (fontToSet!= null) {
						currentFont = fontToSet;
						resetFonts();
						fontSizeName.setValue(Integer.toString(fontSize));
						parametersChanged(new Notification(TreeDisplay.FONTSIZECHANGED));
					}
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets color of taxon names", "[name of color]", commandName, "setColor")) {
			String token = ParseUtil.getFirstToken(arguments, stringPos);
			Color bc = ColorDistribution.getStandardColor(token);
			if (bc == null)
				return null;
			fontColor = bc;
			fontColorLight = ColorDistribution.brighter(bc, ColorDistribution.dimmingConstant);
			fontColorName.setValue(token);
			parametersChanged();
		}
		else 
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	public Font getFont(){
		return currentFont;
	}
	public void invalidateNames(TreeDisplay treeDisplay){
		if (textRotator!=null)
			textRotator.invalidateAll();
	}
	public float getTaxonShade(double value){
		if (!MesquiteDouble.isCombinable(value) || !MesquiteDouble.isCombinable(minValue) || !MesquiteDouble.isCombinable(maxValue) || minValue == maxValue)
			return 1.0f;
		//return (float)((value-minValue)/(maxValue-minValue)*0.8 + 0.2);  //todo: need to distinguish polarity
		return (float)((maxValue - value)/(maxValue-minValue)*0.8 + 0.2);
	}
	public void setTree(Tree tree) {
		if (shader != null ){
			calcShades(tree);
		}
		if (colorerTask !=null && tree != null)
			colorerTask.initialize(tree.getTaxa());
	}
	public String getObjectComment(Object obj){
		if (colorerTask !=null && obj != null)
			return colorerTask.getObjectComment(obj);
		return null;
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (shader != null)
			calcShades(tree);
		if (colorerTask !=null && tree != null)
			colorerTask.prepareToStyle(tree.getTaxa());
		parametersChanged(notification);

	}
	private void calcShades(Tree tree){
		if (tree == null || shader == null)
			return;
		minValue = MesquiteDouble.unassigned;
		maxValue = MesquiteDouble.unassigned;
		Taxa taxa = tree.getTaxa();
		if (shades == null || shades.length < taxa.getNumTaxa())
			shades = new double[taxa.getNumTaxa()];
		for (int i = 0; i<shades.length; i++)
			shades[i] = MesquiteDouble.unassigned;
		MesquiteNumber n = new MesquiteNumber();
		for (int i = 0; i< taxa.getNumTaxa(); i++){
			shader.calculateNumber(taxa.getTaxon(i), n, null);
			if (n.isCombinable()) {
				shades[i] = n.getDoubleValue();
				if (minValue == MesquiteDouble.unassigned)
					minValue = shades[i];
				else if (minValue > shades[i])
					minValue = shades[i];
				if (maxValue == MesquiteDouble.unassigned)
					maxValue = shades[i];
				else if (maxValue < shades[i])
					maxValue = shades[i];
			}
		}
	}
	Color bgTransparent = new Color(255,255,255,0);  //make transparent so as not to overwrite boxes and branches if font big
	boolean nameIsVisible(TreeDisplay treeDisplay, int taxonNumber){
		boolean vis = treeDisplay.getVisRect() == null || namePolys[taxonNumber].intersects(treeDisplay.getVisRect());
		/*if (vis){
			if (triangleBase >=0 ){
				Tree tree = treeDisplay.getTree();
				if (tree != null){
					int node = tree.nodeOfTaxonNumber(taxonNumber);
					if (!tree.nodeExists(node))
						return false;
					return (tree.leftmostTerminalOfNode(triangleBase)==node) || tree.rightmostTerminalOfNode(triangleBase)==node;

				}
			}
		}*/
		return vis;
	}
	boolean nameExposedOnTree(Tree tree, int taxonNumber){
		int node = tree.nodeOfTaxonNumber(taxonNumber);
		return (!tree.withinCollapsedClade(node) || tree.isLeftmostTerminalOfCollapsedClade(node));
	}
	/*_________________________________________________*/
	void zapNamePolysCollapsed(Tree tree, int node){
		if (namePolys == null)
			return;
		if  (tree.nodeIsTerminal(node) && tree.withinCollapsedClade(node)  && !tree.isLeftmostTerminalOfCollapsedClade(node)) {   //terminal
			int taxonNumber = tree.taxonNumberOfNode(node);
			if (taxonNumber>=0 && taxonNumber<namePolys.length){
				setBounds(namePolys[taxonNumber], 0, 0, 0, 0);
			}
		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			zapNamePolysCollapsed(tree, d);
	}
	MesquiteInteger pos = new MesquiteInteger();
	String getCladeName(int node){
		String cc = tree.getNodeLabel(tree.deepestCollapsedAncestor(node));
		pos.setValue(0);
		if (StringUtil.blank(cc) || MesquiteDouble.interpretableAsDouble(cc, pos)){  //if labels is, say, consensusfrequency stored as node label, don't use it
			int taxonNumber = tree.taxonNumberOfNode(node);
			return "Clade of " + tree.getTaxa().getName(taxonNumber);
		}
		else
			return cc;
	}
	/*.................................................................................................................*/
	protected void drawNamesOnTree(Tree tree, int drawnRoot, int N, TreeDisplay treeDisplay, TaxaPartition partitions) {
		if  (tree.nodeIsTerminal(N)) {   //terminal ####################################################
			if (!tree.isVisibleEvenIfInCollapsed( N))
				return;
			if (!showTaxonNames.getValue())
				return;
			Color bgColor = null;
			//Color bgColor = bgTransparent;

			textRotator.assignBackground(bgColor);
			double horiz=treeDrawing.x[N];
			double vert=treeDrawing.y[N];
			if (!treeDisplay.collapsedCladeNameAtLeftmostAncestor && tree.isLeftmostTerminalOfCollapsedClade(N)){
				horiz = treeDrawing.x[tree.deepestCollapsedAncestor(N)];
				vert = treeDrawing.y[tree.deepestCollapsedAncestor(N)];
			}
			int lengthString;
			boolean warn = true;
			Taxa taxa = tree.getTaxa();
			int taxonNumber = tree.taxonNumberOfNode(N);
			if (taxonNumber<0) {
				if (warn)
					MesquiteMessage.warnProgrammer("error: negative taxon number found in DrawNamesTreeDisplay " + taxonNumber + "  tree: " + tree.writeTree());
				return;
			}
			else if (taxonNumber>=taxa.getNumTaxa()) {
				if (warn)
					MesquiteMessage.warnProgrammer("error: taxon number too high found in DrawNamesTreeDisplay " + taxonNumber + "  tree: " + tree.writeTree());
				return;
			}
			if (taxonNumber>= namePolys.length) {
				if (warn)
					MesquiteMessage.warnProgrammer("error: taxon number " + taxonNumber + " / name polys " + namePolys.length);
				return;
			}
			String s=taxa.getName(taxonNumber);
			if (tree.isLeftmostTerminalOfCollapsedClade(N)){
				s = getCladeName(N);
			}
			if (s== null){
				if (warn)
					MesquiteMessage.warnProgrammer("error: taxon name null");
				return;
			}
			Taxon taxon = taxa.getTaxon(taxonNumber);
			if (taxon== null){
				if (warn)
					MesquiteMessage.warnProgrammer("error: taxon null");
				return;
			}
			//@@@@ preparing for the specifics of the taxon name @@@@@@
			boolean selected = taxa.getSelected(taxonNumber);
			//check all extras to see if they want to add anything
			boolean underlined = false;
			Color taxonColor;
			if (!tree.anySelected() || tree.getSelected(N))
				taxonColor = fontColor;
			else
				taxonColor = fontColorLight;

			Color tempColor = Color.black;
			if (!tree.isLeftmostTerminalOfCollapsedClade(N))
				tempColor = colorerTask.getTaxonNameColor(taxa, taxonNumber);
			if (tempColor != null){
				taxonColor = tempColor;
			}
			Font previousFont = gL.getFont();
			
			if (tree.isLeftmostTerminalOfCollapsedClade(N)){
				gL.setFont(currentFontsCollapsed[treeDisplay.collapsedCladeHighlightMode]);
				underlined = treeDisplay.collapsedCladeUnderline;
			}
			else {
				boolean useBold = colorerTask.getTaxonNameBoldness(taxa, taxonNumber);
				if (treeDisplay.selectedTaxonHighlightMode > TreeDisplay.sTHM_GREYBOX){
					if (bigFontChoice!= treeDisplay.selectedTaxonHighlightMode){ //there's been a shift
						bigFontChoice = treeDisplay.selectedTaxonHighlightMode;
						currentFontBIG = new Font(currentFont.getName(), Font.PLAIN, (int)(currentFont.getSize()*highlightMultiplier()));
						currentFontBIGBOLD = new Font(currentFont.getName(), Font.BOLD, (int)(currentFont.getSize()*highlightMultiplier()));
					}
				}
				if (useBold){
					if (selected && treeDisplay.selectedTaxonHighlightMode >TreeDisplay.sTHM_GREYBOX)
						gL.setFont(currentFontBIGBOLD);
					else
						gL.setFont(currentFontBOLD);
				}
				else if (selected && treeDisplay.selectedTaxonHighlightMode > TreeDisplay.sTHM_GREYBOX){
					gL.setFont(currentFontBIG);
				}
				else
					gL.setFont(currentFont);
			}


			if (!tree.isLeftmostTerminalOfCollapsedClade(N)) {
				if (partitions!=null && shadePartition.getValue()){
					TaxaGroup mi = (TaxaGroup)partitions.getProperty(taxonNumber);
					if (mi!=null) {
						if (shadePartition.getValue()){
							bgColor =mi.getColor();
							textRotator.assignBackground(bgColor);
						}
					}
				}
				if (showFootnotes.getValue()){
					ListableVector extras = treeDisplay.getExtras();
					if (extras!=null){
						Enumeration e = extras.elements();
						while (e.hasMoreElements()) {
							Object obj = e.nextElement();
							TreeDisplayExtra ex = (TreeDisplayExtra)obj;
							if (ex.getTaxonUnderlined(taxon))
								underlined = true;
							Color tc = ex.getTaxonColor(taxon);
							if (tc!=null) {
								taxonColor = tc;
							}
							String es = ex.getTaxonStringAddition(taxon);
							if (!StringUtil.blank(es))
								s+= es;
						}
					}
				}
			}

			Composite composite =null;
			if(shades != null && taxonNumber < shades.length) {
				composite = ColorDistribution.getComposite(gL);
				ColorDistribution.setTransparentGraphics(gL,getTaxonShade(shades[taxonNumber]));		
			}
			gL.setColor(taxonColor); 

			
			Font font = gL.getFont();
			FontMetrics fontMet = gL.getFontMetrics(font);
			lengthString = fontMet.stringWidth(s); //what to do if underlined?
			int centeringOffset = 0;
			if (treeDisplay.centerNames)
				centeringOffset = (longestString-lengthString)/2;

			if (treeDrawing.namesFollowLines ){    // ################## === For CircleTree or PlotTree === ####################
				double slope = (treeDrawing.lineBaseY[N]*1.0-treeDrawing.lineTipY[N])*1.0/(treeDrawing.lineBaseX[N]*1.0-treeDrawing.lineTipX[N]);
				double radians = Math.atan(slope);

				boolean right = treeDrawing.lineTipX[N]>treeDrawing.lineBaseX[N];
				double height = fontMet.getHeight(); //0.667
				int length = fontMet.stringWidth(s)+ separation;
				int textOffsetH = 0; //fontMet.getHeight()*2/3;
				int textOffsetV = 0;
				if (!right) {
					textOffsetH = -(int)(Math.cos(radians)*length);

					textOffsetV = -(int)(Math.sin(radians)*length);
				}
				else {
					textOffsetH = (int)(Math.cos(radians + Math.atan(height*0.6/separation))*separation*1.4);  //1.0

					textOffsetV = (int)(Math.sin(radians + Math.atan(height*0.6/separation))*separation*1.4);
				}

				double horizPosition = treeDrawing.lineTipX[N];
				double vertPosition = treeDrawing.lineTipY[N];

				textRotator.drawFreeRotatedText(s, gL, (int)horizPosition, (int)vertPosition, radians, new Point(textOffsetH, textOffsetV), false, namePolys[taxonNumber]);  //integer nodeloc approximation

			}
			// ####################################### Taxon names for NodeLocsStandard trees ####################################################
			else if ((treeDrawing.labelOrientation[N]==270) || treeDisplay.getOrientation()==TreeDisplay.UP) { // ################## === UP === ####################
				//if there is a tipsField, then vert changes from the one based on the tip's y, to the one based on fields
				
				horiz += treeDrawing.getEdgeWidth()/2;
				if (Math.abs(treeDrawing.namesAngle)<0.01) {
					horiz -= StringUtil.getStringDrawLength(gL,"A");
				}
				if (MesquiteDouble.isCombinable(treeDrawing.namesAngle) && treeDrawing.labelOrientation[N]!=270){
					textRotator.drawFreeRotatedText(s,  gL, (int)horiz-rise/2, (int)vert-separation, treeDrawing.namesAngle, null, true, namePolys[taxonNumber]); //integer nodeloc approximation
				}
				else {
					if (treeDisplay.totalTipsFieldDistance()>0){
						vert = treeDisplay.effectiveFieldTopMargin()+treeDisplay.getTipsMargin();
					}
					else vert -= centeringOffset;
					if (!nameExposedOnTree(tree, taxonNumber))
						setBounds(namePolys[taxonNumber], 0, 0, 0, 0);
					else
						setBounds(namePolys[taxonNumber], (int)horiz-rise/2, (int)vert-separation-lengthString, rise+descent, lengthString); //integer nodeloc approximation
					if (nameIsVisible(treeDisplay, taxonNumber))
						textRotator.drawRotatedText(s, taxonNumber, gL, treeDisplay, (int)horiz-rise/2, (int)vert-separation); //integer nodeloc approximation

					if (underlined){
						Rectangle b =namePolys[taxonNumber].getB();
						gL.drawLine(b.x+b.width, b.y, b.x+b.width, b.y+b.height);
						//gL.fillPolygon(namePolys[taxonNumber]);
					}
				}
			}
			else if ((treeDrawing.labelOrientation[N]==90) || treeDisplay.getOrientation()==TreeDisplay.DOWN) {// ################## === DOWN === ####################
				horiz += treeDrawing.getEdgeWidth()/2;
				/*Name rotation now works only for UP
				 * if (MesquiteWindow.Java2Davailable && (MesquiteDouble.isCombinable(treeDrawing.namesAngle) || treeDrawing.labelOrientation[N]!=90)){
					textRotator.drawFreeRotatedText(s,  gL, horiz-rise*2, vert+separation, treeDrawing.namesAngle, null, false, namePolys[taxonNumber]); // /2
				}
				else */
				{
					if (treeDisplay.totalTipsFieldDistance()>0){
						vert = treeDisplay.effectiveFieldHeight()+treeDisplay.effectiveFieldTopMargin()-treeDisplay.getTipsMargin();
					}
				vert += centeringOffset;
					if (!nameExposedOnTree(tree, taxonNumber))
						setBounds(namePolys[taxonNumber], 0, 0, 0, 0);
					else
						setBounds(namePolys[taxonNumber], (int)horiz-rise/2, (int)vert+separation, rise+descent, lengthString); //integer nodeloc approximation
					if (nameIsVisible(treeDisplay, taxonNumber))
						textRotator.drawRotatedText(s, taxonNumber, gL, treeDisplay, (int)horiz-rise/2, (int)vert+separation, false); //integer nodeloc approximation
					if (underlined){
						Rectangle b =namePolys[taxonNumber].getB();
						gL.drawLine(b.x+b.width, b.y, b.x+b.width, b.y+b.height);
						//gL.fillPolygon(namePolys[taxonNumber]);
					}
				}
			}
			else if ((treeDrawing.labelOrientation[N]==0) || treeDisplay.getOrientation()==TreeDisplay.RIGHT) {// ################## ===RIGHT === ####################
				vert += treeDrawing.getEdgeWidth()/2;
				/*Name rotation now works only for UP
				 *if (MesquiteWindow.Java2Davailable && (MesquiteDouble.isCombinable(treeDrawing.namesAngle) || treeDrawing.labelOrientation[N]!=0)){
					textRotator.drawFreeRotatedText(s,  gL, horiz+separation, vert-rise*2, treeDrawing.namesAngle, null, false, namePolys[taxonNumber]); ///2
				}
				else */{
				//Debugg.println must do for other orientations!
					if (treeDisplay.totalTipsFieldDistance()>0){
						horiz = treeDisplay.effectiveFieldWidth()+treeDisplay.effectiveFieldLeftMargin()-treeDisplay.getTipsMargin();
					}
					else {
					//At this point horiz is just the x of the terminal node
					//centering offset is just the left shift if we are to centre taxon names! (e.g. for mirror tree window)
					horiz += centeringOffset;
					}
					if (!nameExposedOnTree(tree, taxonNumber))
						setBounds(namePolys[taxonNumber], 0, 0, 0, 0);
					else
						setBounds(namePolys[taxonNumber], (int)horiz+separation, (int)vert-rise/2, lengthString, rise+descent); //integer nodeloc approximation

					if (nameIsVisible(treeDisplay, taxonNumber)){
						if (bgColor!=null) {
							gL.setColor(bgColor);
							//separation is getTaxonNameDistanceFromTip
							GraphicsUtil.fillRect(gL,horiz+separation, vert-rise/2, lengthString, rise+descent);
							gL.setColor(taxonColor);
						}
						gL.drawString(s, (int)horiz+separation, (int)vert+rise/2); //integer nodeloc approximation
						if (underlined){
							Rectangle b =namePolys[taxonNumber].getB();
							gL.drawLine(b.x, b.y+b.height, b.x+b.width, b.y+b.height);
							//gL.fillPolygon(namePolys[taxonNumber]);
						}
					}
				}
			}
			else if ((treeDrawing.labelOrientation[N]==180) || treeDisplay.getOrientation()==TreeDisplay.LEFT) {// ################## ===LEFT === ####################
				vert += treeDrawing.getEdgeWidth()/2;
				/*Name rotation now works only for UP
				 *if (MesquiteWindow.Java2Davailable && (MesquiteDouble.isCombinable(treeDrawing.namesAngle) || treeDrawing.labelOrientation[N]!=0)){
					textRotator.drawFreeRotatedText(s,  gL, horiz - separation, vert-rise*2, treeDrawing.namesAngle, null, true, namePolys[taxonNumber]);
				}
				else */{
					if (treeDisplay.totalTipsFieldDistance()>0){
						horiz = treeDisplay.effectiveFieldLeftMargin()+treeDisplay.getTipsMargin();
					}
					else {
						horiz -= centeringOffset;
				}
					if (!nameExposedOnTree(tree, taxonNumber))
						setBounds(namePolys[taxonNumber], 0, 0, 0, 0);
					else
						setBounds(namePolys[taxonNumber], (int)horiz - separation - lengthString, (int)vert-rise/2, lengthString, rise+descent); //integer nodeloc approximation
					if (nameIsVisible(treeDisplay, taxonNumber)){
						if (bgColor!=null) {
							gL.setColor(bgColor);
							GraphicsUtil.fillRect(gL,horiz - separation - lengthString, vert-rise/2, lengthString, rise+descent);
							gL.setColor(taxonColor);
						}
						gL.drawString(s, (int)horiz - separation - lengthString, (int)vert+rise/2); //integer nodeloc approximation
						if (underlined){
							Rectangle b =namePolys[taxonNumber].getB();
							gL.drawLine(b.x, b.y+b.height, b.x+b.width, b.y+b.height);
							//gL.fillPolygon(namePolys[taxonNumber]);
						}
					}
				}
			}
			else if (treeDisplay.getOrientation()==TreeDisplay.FREEFORM) {
				if (!nameExposedOnTree(tree, taxonNumber))
					setBounds(namePolys[taxonNumber], 0, 0, 0, 0);
				else
					setBounds(namePolys[taxonNumber], (int)horiz+separation, (int)vert-rise/2, lengthString, rise+descent); //integer nodeloc approximation
				if (nameIsVisible(treeDisplay, taxonNumber)){
					if (bgColor!=null) {
						gL.setColor(bgColor);
						GraphicsUtil.fillRect(gL,horiz+separation, vert-rise/2, lengthString, rise+descent);
						gL.setColor(taxonColor);
					}
					gL.drawString(s, (int)horiz+separation, (int)vert+rise/2);//integer nodeloc approximation
					if (underlined){
						Rectangle b =namePolys[taxonNumber].getB();
						gL.drawLine(b.x, b.y+b.height, b.x+b.width, b.y+b.height);
						//gL.fillPolygon(namePolys[taxonNumber]);
					}
				}
			}
			else {
				double slope = (treeDrawing.lineBaseY[N]*1.0-treeDrawing.lineTipY[N])/(treeDrawing.lineBaseX[N]-treeDrawing.lineTipX[N]);
				if (slope>=-1 && slope <= 1) {  //right or left side
					if (treeDrawing.lineTipX[N]> treeDrawing.lineBaseX[N]) { // right
						if (!nameExposedOnTree(tree, taxonNumber))
							setBounds(namePolys[taxonNumber], 0, 0, 0, 0);
						else
							setBounds(namePolys[taxonNumber], (int)horiz+separation, (int)vert, lengthString, rise+descent); //integer nodeloc approximation
						if (nameIsVisible(treeDisplay, taxonNumber)){
							if (bgColor!=null) {
								gL.setColor(bgColor);
								GraphicsUtil.fillRect(gL,horiz+separation, vert, lengthString, rise+descent);
								gL.setColor(taxonColor);
							}
							gL.drawString(s, (int)horiz+separation, (int)vert + rise); //integer nodeloc approximation
							if (underlined){
								Rectangle b =namePolys[taxonNumber].getB();
								gL.drawLine(b.x, b.y+b.height, b.x+b.width, b.y+b.height);
								//gL.fillPolygon(namePolys[taxonNumber]);
							}
						}
					}
					else {
						if (!nameExposedOnTree(tree, taxonNumber))
							setBounds(namePolys[taxonNumber], 0, 0, 0, 0);
						else
							setBounds(namePolys[taxonNumber], (int)horiz - separation - lengthString, (int)vert, lengthString, rise+descent); //integer nodeloc approximation
						if (nameIsVisible(treeDisplay, taxonNumber)){
							if (bgColor!=null) {
								gL.setColor(bgColor);
								GraphicsUtil.fillRect(gL,horiz - separation - lengthString, vert, lengthString, rise+descent);
								gL.setColor(taxonColor);
							}
							gL.drawString(s, (int)horiz - separation - lengthString, (int)vert + rise); //integer nodeloc approximation
							if (underlined){
								Rectangle b =namePolys[taxonNumber].getB();
								gL.drawLine(b.x, b.y+b.height, b.x+b.width, b.y+b.height);
								//gL.fillPolygon(namePolys[taxonNumber]);
							}
						}
					}
				}
				else {//top or bottom
					if (treeDrawing.lineTipY[N]> treeDrawing.lineBaseY[N]) { // bottom
						if (!nameExposedOnTree(tree, taxonNumber))
							setBounds(namePolys[taxonNumber], 0, 0, 0, 0);
						else
							setBounds(namePolys[taxonNumber], (int)horiz, (int)vert+separation, rise+descent, lengthString); //integer nodeloc approximation
						if (nameIsVisible(treeDisplay, taxonNumber)){
							textRotator.drawRotatedText(s, taxonNumber, gL, treeDisplay, (int)horiz, (int)vert+separation, false); //integer nodeloc approximation
							if (underlined){
								Rectangle b =namePolys[taxonNumber].getB();
								gL.drawLine(b.x+b.width, b.y, b.x+b.width, b.y+b.height);
								//gL.fillPolygon(namePolys[taxonNumber]);
							}
						}
					}
					else { // top
						if (!nameExposedOnTree(tree, taxonNumber))
							setBounds(namePolys[taxonNumber], 0, 0, 0, 0);
						else
							setBounds(namePolys[taxonNumber], (int)horiz, (int)vert-separation-lengthString, rise+descent, lengthString); //integer nodeloc approximation
						if (nameIsVisible(treeDisplay, taxonNumber)){
							textRotator.drawRotatedText(s, taxonNumber, gL, treeDisplay, (int)horiz, (int)vert-separation); //integer nodeloc approximation
							if (underlined){
								Rectangle b =namePolys[taxonNumber].getB();
								gL.drawLine(b.x+b.width, b.y, b.x+b.width, b.y+b.height);
								//gL.fillPolygon(namePolys[taxonNumber]);
							}
						}
					}
				}
			}
			textRotator.assignBackground(null);
			gL.setColor(Color.black);
			ColorDistribution.setComposite(gL,composite);		
			if (selected  && !namePolys[taxonNumber].isHidden() && treeDisplay.selectedTaxonHighlightMode == TreeDisplay.sTHM_GREYBOX){ //&& GraphicsUtil.useXORMode(gL, false)
				GraphicsUtil.fillTransparentBorderedSelectionPolygon(gL, namePolys[taxonNumber]);
			}

				gL.setFont(previousFont);
		}   //end terminal ##############################################################################################
		else {
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				drawNamesOnTree(tree,drawnRoot, d, treeDisplay, partitions);

			String label = null;
			if (showNodeLabels.getValue() && !tree.isCollapsedClade(N)) //collapsed clade labels not drawn becuase they are handled by BasicDrawTaxonNames, as they appear terminally when collapsed
				label = tree.getNodeLabel(N);
			if (label!=null && label.length() >0 && label.charAt(0)!='^') {
				//check all extras to see if they want to add anything
				boolean underlined = false;
				Color taxonColor = Color.black;
				if (!tree.anySelected()|| tree.getSelected(N))
					taxonColor = fontColor;
				else
					taxonColor = fontColorLight;
				String s = StringUtil.deTokenize(label);
				ListableVector extras = treeDisplay.getExtras();
				if (extras!=null){
					Enumeration e = extras.elements();
					while (e.hasMoreElements()) {
						Object obj = e.nextElement();
						TreeDisplayExtra ex = (TreeDisplayExtra)obj;
						if (ex.getCladeLabelUnderlined(label, N))
							underlined = true;
						Color tc = ex.getCladeLabelColor(label, N);
						if (tc!=null)
							taxonColor = tc;
						String es = ex.getCladeLabelAddition(label, N);
						if (!StringUtil.blank(es))
							s+= es;
					}
				}
				boolean squareBranches = treeDisplay.getOwnerModule().findEmployeeWithDuty(DrawTree.class) instanceof SquareTipDrawer;

				int nameDrawLength = StringUtil.getStringDrawLength(gL, s);
				int nameDrawHeight = StringUtil.getTextLineHeight(gL);
				double startH = treeDrawing.x[N];
				double startV = treeDrawing.y[N]; 
				int edgeWidth = treeDrawing.getEdgeWidth();
				if (treeDisplay.getOrientation()==TreeDisplay.UP){
					startH += edgeWidth;
					startV += nameDrawHeight; 
					if (squareBranches) startV += edgeWidth;
					if (centerNodeLabels.getValue())
						startH -= (nameDrawLength)/2;
				}
				else if (treeDisplay.getOrientation()==TreeDisplay.DOWN){
					startH += edgeWidth;
					if (squareBranches) {
						startV -= edgeWidth + nameDrawHeight;
					}
					if (centerNodeLabels.getValue())
						startH -= (nameDrawLength)/2;
				}
				else if (treeDisplay.getOrientation()==TreeDisplay.RIGHT){
					startH += -nameDrawLength-edgeWidth;  //could be a parent node
					startV += edgeWidth;
					if (squareBranches) {
						startH -= edgeWidth;
						startV += nameDrawHeight;
					}
					if (centerNodeLabels.getValue())
						startH += (nameDrawLength)/2;
				}
				else if (treeDisplay.getOrientation()==TreeDisplay.LEFT){
					startH += edgeWidth;//could be a parent node
					startV += edgeWidth;
					if (squareBranches) {
						startH += edgeWidth;
						startV += nameDrawHeight;
					}
					if (centerNodeLabels.getValue())
						startH -= (nameDrawLength)/2;
				}
				StringUtil.highlightString(gL, s, (int)startH, (int)startV, taxonColor, Color.white); //integer nodeloc approximation

				gL.setColor(taxonColor);
				if (underlined)
					GraphicsUtil.drawLine(gL,treeDrawing.x[N], treeDrawing.y[N]+1,treeDrawing.x[N] +  fm.stringWidth(s), treeDrawing.y[N]+1);
			}
		}
	}
	protected void setBounds(TaxonPolygon poly, int x, int y, int w, int h){
		//		poly.getBounds();
		poly.setB(x,y,w,h);
		//int[] xs = poly.xpoints;
		//int[] ys = poly.ypoints;
		//if (true || xs == null || xs.length !=4 || ys == null || ys.length !=4){
		poly.reset();
		poly.addPoint(x, y);
		poly.addPoint(x+w, y);
		poly.addPoint(x+w, y+h);
		poly.addPoint(x, y+h);
		//poly.npoints=4;
		/*}
			else {
				xs[0] = x;
				xs[1] = x+w;
				xs[2] = x+w;
				xs[3] = x;
				ys[0] = y;
				ys[1] = y;
				ys[2] = y+h;
				ys[3] = y+h;
			}*/
	}

	private void resetAngleMenuItem(TreeDisplay treeDisplay){
		// non-default taxon name angles are allowed only for UP trees
		if (angleMenuItem != null && treeDisplay != null && (angleMenuItem.isEnabled() != (treeDisplay.getOrientation()==TreeDisplay.UP))){
			angleMenuItem.setEnabled((treeDisplay.getOrientation()==TreeDisplay.UP));
			MesquiteTrunk.resetMenuItemEnabling();
		}
	}
	int count=0;
	/*.................................................................................................................*/
	public void drawNames(TreeDisplay treeDisplay,  Tree tree, int drawnRoot, Graphics g) {
		if (treeDisplay==null)
			return; // alert("tree display null in draw taxon names");

		resetAngleMenuItem(treeDisplay);
		if (tree==null)
			return; // alert("tree null in draw taxon names");
		if (g==null)
			return; // alert("graphics null in draw taxon names");
		int totalNumTaxa = tree.getTaxa().getNumTaxa();
		if (textRotator == null)
			textRotator = new TextRotator(totalNumTaxa);
		if (namePolys==null) {
			namePolys = new TaxonPolygon[totalNumTaxa];
			oldNumTaxa=totalNumTaxa;
			for (int i = 0; i<totalNumTaxa; i++) {
				namePolys[i] = new TaxonPolygon();
				namePolys[i].xpoints = new int[4];
				namePolys[i].ypoints = new int[4];
				namePolys[i].npoints=4;
			}
		}
		else if (oldNumTaxa<totalNumTaxa) {
			for (int i = 0; i<oldNumTaxa; i++)
				namePolys[i]=null;
			namePolys = new TaxonPolygon[totalNumTaxa];
			for (int i = 0; i<totalNumTaxa; i++) {
				namePolys[i] = new TaxonPolygon();
				namePolys[i].xpoints = new int[4];
				namePolys[i].ypoints = new int[4];
				namePolys[i].npoints=4;
			}
			oldNumTaxa=totalNumTaxa;
		}
		treeDisplay.getTreeDrawing().namePolys = namePolys;

		this.treeDisplay =treeDisplay;
		this.treeDrawing =treeDisplay.getTreeDrawing();
		treeDrawing.namesAngle = namesAngle;

		this.tree =tree;
		this.gL =g;
		if (treeDrawing==null)
			alert("node displays null in draw taxon names");
		try{
			if (MesquiteTree.OK(tree)) {

				if (currentFont ==null) {
					currentFont = g.getFont();
					if (myFont==null)
						myFont = currentFont.getName();
					if (myFontSize<=0)
						myFontSize = currentFont.getSize();
					Font fontToSet = new Font (myFont, currentFont.getStyle(), myFontSize);
					if (fontToSet==null)
						currentFont = g.getFont();
					else
						currentFont = fontToSet;
					currentFontBOLD = new Font(currentFont.getName(), Font.BOLD, currentFont.getSize());
				}
				Font tempFont = g.getFont();
				//currentFont = currentFont.deriveFont(Font.BOLD);
				g.setFont(currentFont);
				fm=g.getFontMetrics(currentFont);
				rise= fm.getMaxAscent();
				descent = fm.getMaxDescent();
				separation = treeDisplay.getTaxonNameDistanceFromTip();

				TaxaPartition part = null;
				if (shadePartition.getValue())
					part = (TaxaPartition)tree.getTaxa().getCurrentSpecsSet(TaxaPartition.class);
				if (treeDisplay.centerNames) {
					longestString = 0;
					findLongestString(tree, drawnRoot);
				}
				if (colorerTask !=null)
					colorerTask.prepareToStyle(tree.getTaxa());
				zapNamePolysCollapsed(tree, drawnRoot);
				drawNamesOnTree(tree, drawnRoot, drawnRoot, treeDisplay, part);

				g.setFont(tempFont);
			}
		}
		catch (Exception e){
			MesquiteMessage.warnProgrammer("Exception in draw taxon names");
			e.printStackTrace();
		}
	}

	/*.................................................................................................................*/
	private void findLongestString(Tree tree,  int N) {
		if  (tree.nodeIsTerminal(N)) {   //terminal
			int taxonNumber = tree.taxonNumberOfNode(N);
			if (taxonNumber<0 || taxonNumber>=tree.getTaxa().getNumTaxa()) 
				return;
			String s = tree.getTaxa().getTaxonName(taxonNumber);
			if (tree.isLeftmostTerminalOfCollapsedClade(N)){
				s = getCladeName(N);
			}

			int lengthString = fm.stringWidth(s); 
			if (lengthString>longestString)
				longestString = lengthString;
		}
		else {
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				findLongestString(tree, d);
		}
	}
	/*.................................................................................................................*/
	int foundTaxon;
	/*.................................................................................................................*/
	private void findNameOnTree(Tree tree,  int N, int x, int y) {
		if  (tree.nodeIsTerminal(N)) {   //terminal

			int taxonNumber = tree.taxonNumberOfNode(N);
			if (taxonNumber<0) {
				//MesquiteMessage.warnProgrammer("error: negative taxon number found in findNameOnTree");
				return;
			}
			if (taxonNumber>=tree.getTaxa().getNumTaxa()) {
				MesquiteMessage.warnProgrammer("error:  taxon number too large found in findNameOnTree (" + taxonNumber + ") node: " + N); 
				return;
			}
			if (taxonNumber>= namePolys.length) {
				MesquiteMessage.warnProgrammer("error in draw taxon names: Name polys not big enough; taxon number " + taxonNumber + " / name boxes " + namePolys.length);
				return;
			}
			if (namePolys[taxonNumber]!=null && namePolys[taxonNumber].contains(x,y)) {
				foundTaxon=taxonNumber;
			}
		}
		else {
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d) && foundTaxon==-1; d = tree.nextSisterOfNode(d))
				findNameOnTree(tree, d, x, y);
		}
	}
	/*.................................................................................................................*/
	public   int findTaxon(Tree tree, int drawnRoot, int x, int y) { 

		foundTaxon=-1;
		if (tree!=null && namePolys!=null) {
			if (tree.getTaxa().isDoomed())
				return -1;
			findNameOnTree(tree, drawnRoot, x, y);
		}
		return foundTaxon; 
		
	}

	/*.................................................................................................................*/
	public   void fillTaxon(Graphics g, int M) {
		try {
			if ((namePolys!=null) && (namePolys[M]!=null) && !namePolys[M].isHidden()) {
				GraphicsUtil.fillTransparentBorderedSelectionPolygon(g,namePolys[M]);
			}
		}
		catch (ArrayIndexOutOfBoundsException e) {
			alert("taxon flash out of getBounds");}
	}


	double queryAngleRadians(){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(),  "Taxon Names Angle",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		dialog.addLabel("Click in circle to set angle of taxon names.");
		dialog.addLabel("Click a triangle for 0, 45, 90 degrees.");
		dialog.addHorizontalLine(1);
		AnglePickerPanel anglePicker = new AnglePickerPanel(namesAngle);
		dialog.addNewDialogPanel(anglePicker);
		dialog.addBlankLine();
		Button useDefaultsButton = null;
		useDefaultsButton = dialog.addAListenedButton("Set to Default", null, anglePicker);
		useDefaultsButton.setActionCommand("setToDefault");
		dialog.addLabel("Note: angled taxon names in effect");
		dialog.addLabel("only when tree is in \"Up\" orientation.");
		dialog.addHorizontalLine(1);
		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			namesAngle = anglePicker.getAngle();
			storePreferences();
		}
		dialog.dispose();
		return namesAngle;	
	}
}

class AnglePickerPanel extends MQPanel implements MouseListener, ActionListener {
	double angle = 0.0;
	Polygon ninetyTriangle, zeroTriangle, fortyFiveTriangle;
	//square is of w/h R*2, and within it is a circle of R.
	//The centre of the circle is therefore at (R, R)
	static final int R = 140;
	static final int trisize = 8;
	static final int border = 12;
	static final int circleInset = border*3;
	TextRotator textRotator;
	public AnglePickerPanel(double initAngle){
		this.angle = initAngle;
		addMouseListener(this);

		ninetyTriangle=new Polygon();
		ninetyTriangle.xpoints = new int[4];
		ninetyTriangle.ypoints = new int[4];
		ninetyTriangle.npoints=0;
		ninetyTriangle.addPoint(R-trisize, border);
		ninetyTriangle.addPoint(R+trisize, border);
		ninetyTriangle.addPoint(R, trisize+trisize+border);
		ninetyTriangle.addPoint(R-trisize, border);
		ninetyTriangle.npoints=4;
		zeroTriangle=new Polygon();
		zeroTriangle.xpoints = new int[4];
		zeroTriangle.ypoints = new int[4];
		zeroTriangle.npoints=0;
		zeroTriangle.addPoint(R*2-border, R-trisize);
		zeroTriangle.addPoint(R*2-border, R+trisize);
		zeroTriangle.addPoint(R*2-border-trisize-trisize, R);
		zeroTriangle.addPoint(R*2-border, R-trisize);
		zeroTriangle.npoints=4;
		fortyFiveTriangle=new Polygon();
		fortyFiveTriangle.xpoints = new int[4];
		fortyFiveTriangle.ypoints = new int[4];
		fortyFiveTriangle.npoints=0;
		int push = 8;
		int xy45 = (int)((R-circleInset+push)*1.0/Math.sqrt(2));
		fortyFiveTriangle.addPoint(R+xy45, R-xy45);
		fortyFiveTriangle.addPoint(R+xy45+6, R-xy45-17);
		fortyFiveTriangle.addPoint(R+xy45+17, R-xy45-6);
		fortyFiveTriangle.addPoint(R+xy45, R-xy45);
		fortyFiveTriangle.npoints=4;
		textRotator = new TextRotator(1);
	}
	public void setAngle(double angle){
		this.angle = angle;
		repaint();
	}

	public Dimension getPreferredSize() {
		return new Dimension(R*2, R*2);
	}
	public void paint(Graphics g){
		g.setClip(0, 0, getBounds().width, getBounds().height);

		g.drawOval(circleInset, circleInset, R*2-circleInset*2, R*2-circleInset*2);
		if (MesquiteDouble.isCombinable(angle)){
			g.setFont(new Font("SansSerif", Font.BOLD, 18));
			int x, y;

			int hypotenuse = R-46;
			double adjacent = Math.abs(Math.cos(angle)*hypotenuse); //R is hypotenuse
			double opposite = Math.abs(Math.sin(angle)*hypotenuse);
			if (angle < -Math.PI/2){ //upper right
				x = (int)(R-adjacent);
				y = (int)(R-opposite);
			}
			else if (angle < 0){ // upper left
				x = (int)(R+adjacent);
				y = (int)(R-opposite);
			}
			else if (angle > Math.PI/2) { //lower left
				x = (int)(R-adjacent);
				y = (int)(R+opposite);
			}
			else {
				x = (int)(R+adjacent);
				y = (int)(R+opposite);
			}

			textRotator.assignBackground(getBackground());
			textRotator.drawFreeRotatedText(" Abcdefg", g, R, R, angle, new Point(0, 0), false, null);  //integer nodeloc approximation
			g.setColor(Color.gray);
		}
		else {
			g.setColor(Color.gray);
			g.fillOval(R-8, R-8, 16, 16);
		}
		g.fillPolygon(zeroTriangle);
		g.fillPolygon(ninetyTriangle);
		g.fillPolygon(fortyFiveTriangle);
	}


	public void mouseClicked(MouseEvent e) {
		int cursorX = e.getX();
		int cursorY = e.getY();
		if (zeroTriangle.contains(cursorX, cursorY))
			angle = 0.0;
		else if (ninetyTriangle.contains(cursorX, cursorY))
			angle = -Math.PI/2;
		else if (fortyFiveTriangle.contains(cursorX, cursorY))
			angle = -Math.PI/4;
		else{
			double hypotenuse = Math.sqrt((cursorX-R)*(cursorX-R) + (cursorY-R)*(cursorY-R));
			if (hypotenuse<= R-circleInset){ // click is within the circle
				//Now calculate angle
				double opposite = (R-cursorY)*1.0;
				angle = Math.asin(opposite/hypotenuse);
				if (cursorX>=R && cursorY<R) //upper right quadrant; 
					angle = -angle;
				else if (cursorX<R && cursorY<R) //upper left quadrant; 
					angle = - Math.PI + angle;
				else if (cursorX<=R && cursorY>R) //lower left quadrant
					angle = Math.PI + angle;
				else if (cursorX>R && cursorY>R) // lower right quadrant
					angle = -angle;
			}
		}
		repaint();
	}


	public void mouseEntered(MouseEvent e) {
	}
	public void mouseExited(MouseEvent e) {
	}
	public void mousePressed(MouseEvent e) {
	}
	public void mouseReleased(MouseEvent e) {
	}
	/*.................................................................................................................*/
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("setToDefault")) {
			angle = MesquiteDouble.unassigned;
			repaint();

		} 
	}

	public double getAngle(){
		return angle;
	}
}
