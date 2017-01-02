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
import java.awt.event.KeyEvent;

import mesquite.lib.*;
import mesquite.lib.duties.*;
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
	protected String myFont = null;
	protected int myFontSize = -1;
	protected FontMetrics fm;
	protected int rise;
	protected int descent;
	protected int oldNumTaxa=0;
	protected MesquiteString fontSizeName, fontName;
	protected MesquiteBoolean colorPartition, colorAssigned, shadePartition, showFootnotes;
	/*New code added Feb.15.07 centerNodeLabels oliver*/ //TODO: delete new code comments
	protected MesquiteBoolean showNodeLabels, showTaxonNames, centerNodeLabels; /*deleted centerNodeLables declaration Feb.26.07 oliver*/
	/*end new code added Feb.15.07 oliver*/
	protected MesquiteString fontColorName;
	protected Color fontColor=Color.black;
	protected Color fontColorLight = Color.gray;
	protected NumberForTaxon shader = null;
	protected int longestString = 0;
	protected MesquiteMenuItemSpec offShadeMI = null;
	/* New code added Feb.26.07 oliver*/ //TODO: delete new code comments
	protected MesquiteMenuItemSpec centerNodeLabelItem = null;
	/* End new code Feb.26.07 oliver*/
	protected double[] shades = null;
	protected double minValue, maxValue;
	double namesAngle = MesquiteDouble.unassigned;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		currentFont = MesquiteWindow.defaultFont;
		fontName = new MesquiteString(MesquiteWindow.defaultFont.getName());
		fontSizeName = new MesquiteString(Integer.toString(MesquiteWindow.defaultFont.getSize()));
		MesquiteSubmenuSpec namesMenu = addSubmenu(null, "Names");
		addItemToSubmenu(null, namesMenu, "Taxon Name Angle...", makeCommand("namesAngle", this));
		
		MesquiteSubmenuSpec msf = FontUtil.getFontSubmenuSpec(this,this);
		msf.setSelected(fontName);
		
		MesquiteSubmenuSpec mss = addSubmenu(null, "Font Size", makeCommand("setFontSize", this), MesquiteSubmenu.getFontSizeList());
		mss.setList(MesquiteSubmenu.getFontSizeList());
		mss.setDocumentItems(false);
		mss.setSelected(fontSizeName);
		fontColorName = new MesquiteString("Black");
		MesquiteSubmenuSpec mmis = addSubmenu(null, "Default Font Color", makeCommand("setColor",  this));
		mmis.setList(ColorDistribution.standardColorNames);
		mmis.setSelected(fontColorName);

		//MesquiteSubmenuSpec mssNames = addSubmenu(null, "Names");
		colorPartition = new MesquiteBoolean(false);
		colorAssigned = new MesquiteBoolean(true);
		addCheckMenuItemToSubmenu(null, namesMenu, "Color by Taxon Group", makeCommand("toggleColorPartition", this), colorPartition);
		addCheckMenuItemToSubmenu(null, namesMenu, "Color by Assigned Color", makeCommand("toggleColorAssigned",  this), colorAssigned);
		addItemToSubmenu(null, namesMenu, "Shade by Value...", makeCommand("shadeByNumber",  this));
		offShadeMI = addItemToSubmenu(null, namesMenu, "Turn off Shading", makeCommand("offShading",  this));
		offShadeMI.setEnabled(false);
		shadePartition = new MesquiteBoolean(false);
		addCheckMenuItemToSubmenu(null, namesMenu, "Background Color by Taxon Group", makeCommand("toggleShadePartition", this), shadePartition);
		showFootnotes = new MesquiteBoolean(true);
		addCheckMenuItemToSubmenu(null, namesMenu, "Show Footnotes Etc.", makeCommand("toggleShowFootnotes", this), showFootnotes);
		showNodeLabels = new MesquiteBoolean(true);
		addCheckMenuItemToSubmenu(null, namesMenu, "Show Branch Names", makeCommand("toggleNodeLabels", this), showNodeLabels);
		/*New code added Feb.15.07 oliver*/ //TODO: delete new code comments
		centerNodeLabels = new MesquiteBoolean(false);
		centerNodeLabelItem = addCheckMenuItemToSubmenu(null, namesMenu, "Center Branch Names", makeCommand("toggleCenterNodeNames", this), centerNodeLabels);
		centerNodeLabelItem.setEnabled(true);

		/*End new code added Feb.15.07 oliver*/
		showTaxonNames = new MesquiteBoolean(true);
		addCheckMenuItemToSubmenu(null, namesMenu, "Show Taxon Names", makeCommand("toggleShowNames", this), showTaxonNames);
		addSubmenu(namesMenu, "Alter Names", makeCommand("alterBranchNames",  this), BranchNamesAlterer.class);

		return true;
	}

	public void endJob(){
		treeDisplay = null;
		treeDrawing = null;
		textRotator = null;
		super.endJob();
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
		temp.addLine("toggleColorPartition " + colorPartition.toOffOnString());
		temp.addLine("toggleColorAssigned " + colorAssigned.toOffOnString());
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
		if (checker.compare(this.getClass(), "Sets module that calculates a number for a taxon by which to shade", "[name of module]", commandName, "shadeByNumber")) {
			NumberForTaxon temp= (NumberForTaxon)replaceEmployee(NumberForTaxon.class, arguments, "Value by which to shade taxon names", shader);
			if (temp!=null) {
				shader = temp;
				offShadeMI.setEnabled(true);
				MesquiteTrunk.resetMenuItemEnabling();
				shades = null;
				calcShades(tree);

				parametersChanged();
				return temp;
			}
		}
		else if (checker.compare(this.getClass(), "Sets the angle names are shown at in default UP orientation", "[angle in degrees clockwise from horizontal; ? = default]", commandName, "namesAngle")) {
			if (arguments == null && !MesquiteThread.isScripting()){
				double current;
				if (!MesquiteDouble.isCombinable(namesAngle))
					current = namesAngle;
				else
					current = namesAngle/2/Math.PI*360;
				MesquiteDouble d = new MesquiteDouble(current);
				if (!QueryDialogs.queryDouble(containerOfModule(), "Names Angle", "Angle of taxon names, in degrees clockwise from horizontal.  Use \"?\" to indicate default.  Typical settings are between 0 degrees and -90 degrees.  0 = text reads from left to right (long dash = �); -90 = text reads from bottom to top (long dash = |); -45 = text angled diagonally (long dash = /).  This setting applies only when tree is in UP orientation", d))
					return null;
				namesAngle = d.getValue();
				if (MesquiteDouble.isCombinable(namesAngle))
					namesAngle = namesAngle/360*2*Math.PI;
				parametersChanged();
			}
			else {

				double angle = MesquiteDouble.fromString(parser.getFirstToken(arguments));
				namesAngle = angle;
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Turns of shading by number", null, commandName, "offShading")) {
			if (shader != null)
				fireEmployee(shader);
			shader = null;
			shades = null;
			offShadeMI.setEnabled(false);
			MesquiteTrunk.resetMenuItemEnabling();
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Toggles whether taxon names are colored according to their group in the current taxa partition", "[on or off]", commandName, "toggleColorPartition")) {
			boolean current = colorPartition.getValue();

			colorPartition.toggleValue(parser.getFirstToken(arguments));
			if (colorAssigned.getValue() && colorPartition.getValue())
				colorAssigned.setValue(false);
			if (current!=colorPartition.getValue())
				parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Toggles whether taxon names are colored according to their current assigned color", "[on or off]", commandName, "toggleColorAssigned")) {
			boolean current = colorAssigned.getValue();

			colorAssigned.toggleValue(parser.getFirstToken(arguments));
			if (colorAssigned.getValue() && colorPartition.getValue())
				colorPartition.setValue(false);
			if (current!=colorAssigned.getValue())
				parametersChanged();
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
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		calcShades(tree);
		parametersChanged(notification);

	}
	private void calcShades(Tree tree){
		if (tree == null)
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
	boolean nameExposedOnTree(Tree tree, int taxonNumber, int triangleBase){
		if (triangleBase >=0 ){
			int node = tree.nodeOfTaxonNumber(taxonNumber);
			return (tree.leftmostTerminalOfNode(triangleBase)==node) || tree.rightmostTerminalOfNode(triangleBase)==node;
		}

		return true;
	}
	NameReference colorNameRef = NameReference.getNameReference("color");
	/*.................................................................................................................*/
	protected void drawNamesOnTree(Tree tree, int drawnRoot, int N, TreeDisplay treeDisplay, TaxaPartition partitions, int triangleBase) {
		if (triangleBase < 0 && tree.getAssociatedBit(triangleNameRef, N))
			triangleBase = N;
		if  (tree.nodeIsTerminal(N)) {   //terminal
			if (!showTaxonNames.getValue())
				return;
			Color bgColor = null;
			//Color bgColor = bgTransparent;

			textRotator.assignBackground(bgColor);
			double horiz=treeDrawing.x[N];
			double vert=treeDrawing.y[N];
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
			boolean selected = taxa.getSelected(taxonNumber);
			//check all extras to see if they want to add anything
			boolean underlined = false;
			Color taxonColor;
			if (!tree.anySelected() || tree.getSelected(N))
				taxonColor = fontColor;
			else
				taxonColor = fontColorLight;

			if (partitions!=null && (colorPartition.getValue() || shadePartition.getValue())){
				TaxaGroup mi = (TaxaGroup)partitions.getProperty(taxonNumber);
				if (mi!=null) {
					if (colorPartition.getValue() && mi.getColor() != null)
						taxonColor = mi.getColor();
					if (shadePartition.getValue()){
						bgColor =mi.getColor();
						textRotator.assignBackground(bgColor);
					}
				}
			}
			if (colorAssigned.getValue()){
				long c = taxa.getAssociatedLong(colorNameRef, taxonNumber);
				if (MesquiteLong.isCombinable(c))
					taxonColor= ColorDistribution.getStandardColor((int)c);
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

			Composite composite =null;
			if(shades != null && taxonNumber < shades.length) {
				composite = ColorDistribution.getComposite(gL);
				ColorDistribution.setTransparentGraphics(gL,getTaxonShade(shades[taxonNumber]));		
			}
			gL.setColor(taxonColor); 

			lengthString = fm.stringWidth(s); //what to do if underlined?
			int centeringOffset = 0;
			if (treeDisplay.centerNames)
				centeringOffset = (longestString-lengthString)/2;

			if (treeDrawing.namesFollowLines ){
				double slope = (treeDrawing.lineBaseY[N]*1.0-treeDrawing.lineTipY[N])*1.0/(treeDrawing.lineBaseX[N]*1.0-treeDrawing.lineTipX[N]);
				double radians = Math.atan(slope);
				
				boolean right = treeDrawing.lineTipX[N]>treeDrawing.lineBaseX[N];
				Font font = gL.getFont();
				FontMetrics fontMet = gL.getFontMetrics(font);
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
			else if ((treeDrawing.labelOrientation[N]==270) || treeDisplay.getOrientation()==TreeDisplay.UP) {
				horiz += treeDrawing.getEdgeWidth()/2;
				if (Math.abs(treeDrawing.namesAngle)<0.01) {
					horiz -= StringUtil.getStringDrawLength(gL,"A");
				}
				if (MesquiteDouble.isCombinable(treeDrawing.namesAngle) && treeDrawing.labelOrientation[N]!=270){
					textRotator.drawFreeRotatedText(s,  gL, (int)horiz-rise/2, (int)vert-separation, treeDrawing.namesAngle, null, true, namePolys[taxonNumber]); //integer nodeloc approximation
				}
				else {
					vert -= centeringOffset;
					if (!nameExposedOnTree(tree, taxonNumber, triangleBase))
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
			else if ((treeDrawing.labelOrientation[N]==90) || treeDisplay.getOrientation()==TreeDisplay.DOWN) {
				horiz += treeDrawing.getEdgeWidth()/2;
				/*if (MesquiteWindow.Java2Davailable && (MesquiteDouble.isCombinable(treeDrawing.namesAngle) || treeDrawing.labelOrientation[N]!=90)){
					textRotator.drawFreeRotatedText(s,  gL, horiz-rise*2, vert+separation, treeDrawing.namesAngle, null, false, namePolys[taxonNumber]); // /2
				}
				else */
				{
					vert += centeringOffset;
					if (!nameExposedOnTree(tree, taxonNumber, triangleBase))
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
			else if ((treeDrawing.labelOrientation[N]==0) || treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
				vert += treeDrawing.getEdgeWidth()/2;
				/*if (MesquiteWindow.Java2Davailable && (MesquiteDouble.isCombinable(treeDrawing.namesAngle) || treeDrawing.labelOrientation[N]!=0)){
					textRotator.drawFreeRotatedText(s,  gL, horiz+separation, vert-rise*2, treeDrawing.namesAngle, null, false, namePolys[taxonNumber]); ///2
				}
				else */{
					horiz += centeringOffset;
					if (!nameExposedOnTree(tree, taxonNumber, triangleBase))
						setBounds(namePolys[taxonNumber], 0, 0, 0, 0);
					else
					setBounds(namePolys[taxonNumber], (int)horiz+separation, (int)vert-rise/2, lengthString, rise+descent); //integer nodeloc approximation

					if (nameIsVisible(treeDisplay, taxonNumber)){
						if (bgColor!=null) {
							gL.setColor(bgColor);
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
			else if ((treeDrawing.labelOrientation[N]==180) || treeDisplay.getOrientation()==TreeDisplay.LEFT) {
				vert += treeDrawing.getEdgeWidth()/2;
				/*if (MesquiteWindow.Java2Davailable && (MesquiteDouble.isCombinable(treeDrawing.namesAngle) || treeDrawing.labelOrientation[N]!=0)){
					textRotator.drawFreeRotatedText(s,  gL, horiz - separation, vert-rise*2, treeDrawing.namesAngle, null, true, namePolys[taxonNumber]);
				}
				else */{
					horiz -= centeringOffset;
					if (!nameExposedOnTree(tree, taxonNumber, triangleBase))
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
				if (!nameExposedOnTree(tree, taxonNumber, triangleBase))
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
						if (!nameExposedOnTree(tree, taxonNumber, triangleBase))
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
						if (!nameExposedOnTree(tree, taxonNumber, triangleBase))
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
						if (!nameExposedOnTree(tree, taxonNumber, triangleBase))
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
						if (!nameExposedOnTree(tree, taxonNumber, triangleBase))
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
			if (selected  && !namePolys[taxonNumber].isHidden()){ //&& GraphicsUtil.useXORMode(gL, false)
				//gL.setXORMode(Color.white);
				//gL.fillPolygon(namePolys[taxonNumber]);
				GraphicsUtil.fillTransparentBorderedSelectionPolygon(gL, namePolys[taxonNumber]);

			//	gL.setPaintMode();
			}

		}
		else {
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				drawNamesOnTree(tree,drawnRoot, d, treeDisplay, partitions, triangleBase);

			String label = null;
			if (showNodeLabels.getValue())
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
				/*New code added Feb.15.07 oliver*/ //TODO: delete new code comments
				//				TODO: Currently only really works for square trees, and an ugly hack at that
				if (!centerNodeLabels.getValue() || !(MesquiteModule.getShortClassName(treeDrawing.getClass()).toString().equalsIgnoreCase("SquareTreeDrawing"))){
					StringUtil.highlightString(gL,s, (int)treeDrawing.x[N], (int)treeDrawing.y[N], taxonColor, Color.white); //integer nodeloc approximation
					if (MesquiteModule.getShortClassName(treeDrawing.getClass()).toString().equalsIgnoreCase("SquareTreeDrawing"))
						centerNodeLabelItem.setEnabled(true);
					else centerNodeLabelItem.setEnabled(false); // TODO: these conditionals don't work right.  Should work now April.03.07 oliver
				}
				else {
					centerNodeLabelItem.setEnabled(true);
					int edgeWidth = treeDrawing.getEdgeWidth();
					int parentN = tree.motherOfNode(N);
					double centerH, centerV, startH, startV;
					int nameDrawLength = StringUtil.getStringDrawLength(gL, s);
					int nameDrawHeight = StringUtil.getTextLineHeight(gL);
					if (treeDisplay.getOrientation()==TreeDisplay.UP){
						startV = treeDrawing.y[N] + ((treeDrawing.y[parentN] - treeDrawing.y[N])/2) + edgeWidth; 
						startH = treeDrawing.x[N] + edgeWidth;
						StringUtil.highlightString(gL, s, (int)startH, (int)startV, taxonColor, Color.white); //integer nodeloc approximation
					}
					else if (treeDisplay.getOrientation()==TreeDisplay.DOWN){
						startV = treeDrawing.y[N] - (int)((treeDrawing.y[N] - treeDrawing.y[parentN])/2); 
						startH = treeDrawing.x[N] + edgeWidth;
						StringUtil.highlightString(gL, s, (int)startH, (int)startV, taxonColor, Color.white); //integer nodeloc approximation
					}
					else if (treeDisplay.getOrientation()==TreeDisplay.RIGHT){
						centerH = treeDrawing.x[N] - (int)((treeDrawing.x[N] - treeDrawing.x[parentN])/2) - edgeWidth; 
						startH = centerH -(int)(nameDrawLength/2);
						// this conditional tests for overlap between branch and names, and shifts name accordingly.
						if((centerH + (int)nameDrawLength/2) > treeDrawing.x[N] - edgeWidth){
							startH -= (centerH + (int)nameDrawLength/2) - (treeDrawing.x[N] - edgeWidth);
						}
						startV = (int)(treeDrawing.y[N] - 1);
						StringUtil.highlightString(gL, s, (int)startH, (int)startV, taxonColor, Color.white); //integer nodeloc approximation
					}
					else if (treeDisplay.getOrientation()==TreeDisplay.LEFT){
						centerH = treeDrawing.x[N] + (int)((treeDrawing.x[parentN] - treeDrawing.x[N])/2) + edgeWidth; 
						startH = centerH -(int)(nameDrawLength/2);
						// this conditional tests for overlap between branch and names, and shifts name accordingly.
						if((centerH - (int)nameDrawLength/2 < treeDrawing.x[N] + edgeWidth)){
							startH += (treeDrawing.x[N] + edgeWidth) - (centerH - (int)nameDrawLength/2);
						}
						startV = (int)(treeDrawing.y[N] - 1);
						StringUtil.highlightString(gL, s, (int)startH, (int)startV, taxonColor, Color.white); //integer nodeloc approximation
					}
					// TODO: figure out how to check for initialization of startH & startV, then pull the highlightString method out of the four conditionals above and put it here
					// StringUtil.highlightString(gL, s, startH, startV, taxonColor, Color.white);
				}
				/*end new code added Feb.15.07 oliver*/
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

	int count=0;
	NameReference triangleNameRef = NameReference.getNameReference("triangled");
	/*.................................................................................................................*/
	public void drawNames(TreeDisplay treeDisplay,  Tree tree, int drawnRoot, Graphics g) {
		if (treeDisplay==null)
			return; // alert("tree display null in draw taxon names");
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
				}
				Font tempFont = g.getFont();
				//currentFont = currentFont.deriveFont(Font.BOLD);
				g.setFont(currentFont);
				fm=g.getFontMetrics(currentFont);
				rise= fm.getMaxAscent();
				descent = fm.getMaxDescent();
				separation = treeDisplay.getTaxonNameDistance();

				TaxaPartition part = null;
				if (colorPartition.getValue() || shadePartition.getValue())
					part = (TaxaPartition)tree.getTaxa().getCurrentSpecsSet(TaxaPartition.class);
				if (treeDisplay.centerNames) {
					longestString = 0;
					findLongestString(tree, drawnRoot);
				}
				int triangleBase;
				if (tree.getAssociatedBit(triangleNameRef, drawnRoot))
					triangleBase = drawnRoot;
				else
					triangleBase = -1;
				drawNamesOnTree(tree, drawnRoot, drawnRoot, treeDisplay, part, triangleBase);
		
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

			int lengthString = fm.stringWidth(tree.getTaxa().getTaxonName(taxonNumber)); 
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
		return foundTaxon; }

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
}


