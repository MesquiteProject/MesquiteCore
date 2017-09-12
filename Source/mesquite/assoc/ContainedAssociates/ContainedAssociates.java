/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.assoc.ContainedAssociates;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.assoc.lib.*;

/* ======================================================================== */
public class ContainedAssociates extends AnalyticalDrawTree {
	static int maxEdgeWidth = 499;
	
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
//		EmployeeNeed e = registerEmployeeNeed(OneTreeSource.class, getName() + " needs a source for the contained trees (e.g. gene trees) to fit within the containing tree (e.g. a species tree).",
//		"The source of contained trees can be chosen initially or in the Contained Tree Source submenu");
		EmployeeNeed e2 = registerEmployeeNeed(TreeSource.class, getName() + " needs a source for the contained trees (e.g. gene trees) to fit within the containing tree (e.g. a species tree).",
		"The source of contained trees can be chosen initially or in the Contained Tree Source submenu");
		EmployeeNeed e3 = registerEmployeeNeed(NodeLocsVH.class,  getName() + " uses a module to define node locations.",
		"This is dealt with automatically.");
		e3.setSuppressListing(true);
		EmployeeNeed e4 = registerEmployeeNeed(AssociationSource.class,  getName() + " needs to know how contained taxa (e.g. genes) fit within containing taxa (e.g. species).",
		"The taxa association is probably found automatcially; otherwise you can choose it initially.");
		EmployeeNeed e5 = registerEmployeeNeed(ReconstructAssociation.class,  getName() + " needs a method to reconstruct how contained lineages (e.g. genes) fit within containing lineages (e.g. species).",
		"The reconstruction method is chosen initially.");
		EmployeeNeed e6 = registerEmployeeNeed(mesquite.assoc.DepContTreeWindow.DepContTreeWindow.class,  getName() + " can show the contained tree.",
		"The contained tree can be shown by selecting Display Contained Tree.");

	}
	public String getName() {
		return "Contained Gene (or Other) Trees";
	}
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Draws wide trees containing trees of associates (e.g., species tree containing gene tree)" ;
	}
	public String getKeywords(){
		return "gene_tree species_tree coalescence coalescent";
	}
	/*.................................................................................................................*/

//	TODO: have standard AssociateHistory (like CharacterHistory)
	NodeLocsVH nodeLocsTask;
	//OneTreeSource oneTreeSourceTask;
	TreeSource treeSourceTask;
	Tree originalContainedTree;
	AssociationSource associationTask;
	ReconstructAssociation reconstructTask;
	MesquiteCommand edgeWidthCommand;
	MesquiteBoolean scale = new MesquiteBoolean(true);
	MesquiteBoolean showContainedTaxNames = new MesquiteBoolean(true);
	Vector drawings;
	public int minimalEdgeWidth = 64;
	int oldEdgeWidth =64;
	int ornt;
	boolean startedUnderScripting;
	boolean usingOneTreeSource = false;//TODO: if this is ever used, need to deal with hiring comamnd and so on
	MesquiteString treeSourceName;
	int initialOffsetX=MesquiteInteger.unassigned;
	int initialOffsetY= MesquiteInteger.unassigned;
	MesquiteCommand tstC;
	TWindowMaker depWindowMaker;
	boolean suppressed = false;
	boolean suppressedSet = false; //this is to handle old version files
	
	Color containedBranchColor;
	MesquiteString containedBrColorName;
	Color containingBranchColor =null; //WideTreeDrawing.defaultBranchColor;
	MesquiteString containingBrColorName;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		startedUnderScripting = MesquiteThread.isScripting();
		if (getProject().getNumberTaxas()<=1) {
			return sorry("Sorry, you can't use the Contained Associates tree drawing if there is only a single set of taxa available.  It is designed to show contained trees within containing trees (e.g. genes within species)");
		}
		nodeLocsTask= (NodeLocsVH)hireCompatibleEmployee(NodeLocsVH.class, new boolean[]{true}, "Calculator of node locations");
		if (nodeLocsTask == null) {
			return sorry(getName() + " couldn't start because node locator module not obtained");
		}
		drawings = new Vector();
		associationTask = (AssociationSource)hireEmployee(AssociationSource.class, "Source of taxon associations");
		if (associationTask == null) {
			return sorry(getName() + " couldn't start because no source of taxon associations obtained");
		}
		//TODO: allow choice of association source, reconstruction method
		reconstructTask = (ReconstructAssociation)hireEmployee(ReconstructAssociation.class, "Method to reconstruct association history");
		if (reconstructTask == null) {
			return sorry(getName() + " couldn't start because no module ot reconstruct associaton histories obtained");
		}
		treeSourceTask = (TreeSource)hireEmployee(TreeSource.class, "Source of contained trees (for Contained Associates tree drawing)");
		if (treeSourceTask == null) {
			return sorry(getName() + " couldn't start because no source of contained trees obtained");
		}
		tstC = makeCommand("setTreeSource",  this);
		treeSourceTask.setHiringCommand(tstC);
		makeMenu("Contained");
		treeSourceName = new MesquiteString(treeSourceTask.getName());
		if (numModulesAvailable(TreeSource.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Contained Tree Source", tstC, TreeSource.class);
			mss.setSelected(treeSourceName);
		}
		addCheckMenuItem(null, "Scale to depth", makeCommand("toggleScale",  this), scale);
		addMenuItem( "Next Contained Tree", makeCommand("nextContained",  this));
		addMenuItem( "Previous Contained Tree", makeCommand("previousContained",  this));
		addMenuItem( "Line Width...", makeCommand("setEdgeWidth",  this));
		if (getEmployer() instanceof DrawTreeCoordinator && containingBranchColor!= null){
			((DrawTreeCoordinator)getEmployer()).setBranchColor(containingBranchColor);
		}
		addCheckMenuItem(null, "Show Contained Names", makeCommand("toggleContainedNames",  this), showContainedTaxNames);
		addMenuItem( "Display Contained Tree", makeCommand("showContainedTree",  this));
		
		/*containingBrColorName = new MesquiteString("Blue");
		MesquiteSubmenuSpec mmis = addSubmenu(null, "Containing Branch Color", makeCommand("setContainingBranchColor",  this));
		mmis.setList(ColorDistribution.standardColorNames);
		mmis.setSelected(containingBrColorName);
*/
		containedBrColorName = new MesquiteString("Black");
		MesquiteSubmenuSpec mmis2 = addSubmenu(null, "Contained Branch Color", makeCommand("setContainedBranchColor",  this));
		mmis2.setList(ColorDistribution.standardColorNames);
		mmis2.setSelected(containedBrColorName);

		suppressed = MesquiteThread.isScripting();
		return true;
	}
	public void endJob(){
		suppressed = true;
		depWindowMaker = null;
		super.endJob();
	}
	public void employeeQuit(MesquiteModule m){
		if (m!=treeSourceTask)
			iQuit();
		else if (drawings != null && drawings.size()>0){
			TaxaAssociation a = ((WideTreeDrawing)drawings.elementAt(0)).association;
			if (a.getTaxa(0).isDoomed() || a.getTaxa(1).isDoomed())
				iQuit();

		}
	}

	/*.................................................................................................................*/
	public   TreeDrawing createTreeDrawing(TreeDisplay treeDisplay, int numTaxa) { //TODO: should be passed scripting
		if (treeDisplay.getEdgeWidth()<minimalEdgeWidth) {
			treeDisplay.setEdgeWidth(minimalEdgeWidth);
		}
		treeDisplay.setOrientation(TreeDisplay.UP);
		WideTreeDrawing treeDrawing;
		/*	if (usingOneTreeSource && oneTreeSourceTask !=null)
			treeDrawing =  new WideTreeDrawing (treeDisplay, numTaxa, this, associationTask, oneTreeSourceTask, reconstructTask, startedUnderScripting);
		else */
		CommandRecord prev = MesquiteThread.getCurrentCommandRecord();
		MesquiteThread.setCurrentCommandRecord(new CommandRecord(startedUnderScripting));

		treeDrawing =  new WideTreeDrawing (treeDisplay, numTaxa, this, associationTask, treeSourceTask, reconstructTask);
		MesquiteThread.setCurrentCommandRecord(prev);
		drawings.addElement(treeDrawing);
		return treeDrawing;
	}
	/** Returns true if other modules can control the orientation */
	public boolean allowsReorientation(){
		return false;
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (MesquiteThread.isScripting())
			return;
		if (employee instanceof OneTreeSource || employee instanceof TreeSource) {
			if (Notification.getCode(notification) != MesquiteListener.SELECTION_CHANGED) {
				Enumeration e = drawings.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					WideTreeDrawing treeDrawing = (WideTreeDrawing)obj;
					treeDrawing.currentContainedTree = null;
					treeDrawing.containedSourceChanged();
					if (treeDrawing.treeDisplay!=null)
						treeDrawing.treeDisplay.pleaseUpdate(true);
				}
			}
		}
		else if (employee instanceof AssociationSource){
			Enumeration e = drawings.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				WideTreeDrawing treeDrawing = (WideTreeDrawing)obj;
				treeDrawing.resetAssociation(treeDrawing.treeDisplay.getTree().getTaxa(), true);
				treeDrawing.zapContained();
				if (treeDrawing.treeDisplay!=null)
					treeDrawing.treeDisplay.pleaseUpdate(true);
			}
		}
		else if (employee instanceof ReconstructAssociation){
			Enumeration e = drawings.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				WideTreeDrawing treeDrawing = (WideTreeDrawing)obj;
				treeDrawing.zapContained();
				if (treeDrawing.treeDisplay!=null)
					treeDrawing.treeDisplay.pleaseUpdate(true);
			}
		}
		else {
			super.employeeParametersChanged(employee, source, notification);
		}
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("suppress "); 
		temp.addLine("getAssociationSource ",  associationTask); 
		temp.addLine("getReconstructor ",  reconstructTask); 
		temp.addLine("setEdgeWidth " + oldEdgeWidth); 
		temp.addLine("setTreeSource ", treeSourceTask); 
		if (depWindowMaker != null && depWindowMaker.getModuleWindow() != null && depWindowMaker.getModuleWindow().isVisible())
			temp.addLine("showContainedTree", depWindowMaker);
		if (treeSourceTask !=null) { //not using oneTreeSourceTask
			if (drawings.size()>0) {
				Object obj = drawings.elementAt(0);
				WideTreeDrawing treeDrawing = (WideTreeDrawing)obj;
				temp.addLine("setContained " + MesquiteTree.toExternal(treeDrawing.currentContained)); 
			}
		}
		temp.addLine("toggleScale " + scale.toOffOnString()); 
		temp.addLine("toggleContainedNames " + showContainedTaxNames.toOffOnString()); 
		WideTreeDrawing tco = (WideTreeDrawing)drawings.elementAt(0);
		if (tco!=null && tco.legend!=null) {
			temp.addLine("setInitialOffsetX " + tco.legend.getOffsetX()); //Should go operator by operator!!!
			temp.addLine("setInitialOffsetY " + tco.legend.getOffsetY());
		}
		if (containedBranchColor !=null) {
			String bName = ColorDistribution.getStandardColorName(containedBranchColor);
			if (bName!=null)
				temp.addLine("setContainedBranchColor " + StringUtil.tokenize(bName));//quote
		}
		if (containingBranchColor !=null) {
			String bName = ColorDistribution.getStandardColorName(containingBranchColor);
			if (bName!=null)
				temp.addLine("setContainingBranchColor " + StringUtil.tokenize(bName));//quote
		}
		temp.addLine("desuppress "); 
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the thickness of the drawn branches", "[width in pixels]", commandName, "setEdgeWidth")) {
			int newWidth= MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newWidth))
				newWidth = MesquiteInteger.queryInteger(containerOfModule(), "Set edge width", "Edge Width:", oldEdgeWidth, 1, maxEdgeWidth);
			if (newWidth>minimalEdgeWidth && newWidth<=maxEdgeWidth && newWidth!=oldEdgeWidth) {
				oldEdgeWidth=newWidth;
				Enumeration e = drawings.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					WideTreeDrawing treeDrawing = (WideTreeDrawing)obj;
					treeDrawing.setEdgeWidth(newWidth);
					treeDrawing.treeDisplay.setEdgeWidth(newWidth);
					treeDrawing.treeDisplay.setMinimumTaxonNameDistance(minimalEdgeWidth/4, 4); 
				}
				if (!MesquiteThread.isScripting()) parametersChanged();
			}

		}
		else if (checker.compare(this.getClass(), "Sets default color of contained branches of tree in tree display", "[name of color]", commandName, "setContainedBranchColor")) {
			String token = ParseUtil.getFirstToken(arguments, stringPos);
			Color bc = ColorDistribution.getStandardColor(token);
			if (bc == null)
				return null;
			Color bcD = ColorDistribution.getStandardColorDimmed(ColorDistribution.getStandardColorNumber(bc));
			containedBranchColor = bc;
			containedBrColorName.setValue(token);
			Enumeration e = drawings.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				WideTreeDrawing treeDrawing = (WideTreeDrawing)obj;
				treeDrawing.setContainedColor(bc);
			}
			if (!MesquiteThread.isScripting()) parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets default color of containing branches of tree in tree display", "[name of color]", commandName, "setContainingBranchColor")) {
			String token = ParseUtil.getFirstToken(arguments, stringPos);
			Color bc = ColorDistribution.getStandardColor(token);
			if (bc == null)
				return null;
			Color bcD = ColorDistribution.getStandardColorDimmed(ColorDistribution.getStandardColorNumber(bc));
			containingBranchColor = bc;
			containingBrColorName.setValue(token);
			if (getEmployer() instanceof DrawTreeCoordinator){
				((DrawTreeCoordinator)getEmployer()).setBranchColor(containingBranchColor);
			}
			Enumeration e = drawings.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				WideTreeDrawing treeDrawing = (WideTreeDrawing)obj;
				treeDrawing.setContainingColor(bc);
			}
			if (!MesquiteThread.isScripting()) parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets whether contained trees are drawn to scale of their branch lengths", "[on or off to indicate whether scaled]", commandName, "toggleScale")) {
			boolean current = scale.getValue();
			scale.toggleValue(parser.getFirstToken(arguments));
			if (current!=scale.getValue()) {
				Enumeration e = drawings.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					WideTreeDrawing treeDrawing = (WideTreeDrawing)obj;
					treeDrawing.treeDisplay.pleaseUpdate(false);
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether contained trees show taxon names", "[on or off to indicate whether name shown]", commandName, "toggleContainedNames")) {
			boolean current = showContainedTaxNames.getValue();
			showContainedTaxNames.toggleValue(parser.getFirstToken(arguments));
			if (current!=showContainedTaxNames.getValue()) {
				Enumeration e = drawings.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					WideTreeDrawing treeDrawing = (WideTreeDrawing)obj;
					treeDrawing.treeDisplay.pleaseUpdate(false);
				}
			}
		}
		else if (checker.compare(this.getClass(), "Returns the module supplying taxa associations", null, commandName, "getAssociationSource")) {
			return  associationTask; 

		}
		else if (checker.compare(this.getClass(), "Returns the module reconstructing history of contained tree within containing tree", null, commandName, "getReconstructor")) {
			return  reconstructTask; 

		}
		else if (checker.compare(this.getClass(), "Suppresses tree drawing", null, commandName, "suppress")) {
			suppressed = true;
			suppressedSet = true;
		}
		else if (checker.compare(this.getClass(), "Desuppresses tree drawing", null, commandName, "desuppress")) {
			suppressed = false;
			Enumeration e = drawings.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				WideTreeDrawing treeDrawing = (WideTreeDrawing)obj;
				treeDrawing.desuppress();
			}
		}
		else if (checker.compare(this.getClass(), "Sets the source of contained trees", "[name of module]", commandName, "setTreeSource")) {

			TreeSource temp = (TreeSource)replaceEmployee(TreeSource.class, arguments, "Source of contained trees", treeSourceTask);
			if (temp !=null){
				treeSourceTask = temp;
				treeSourceTask.setHiringCommand(tstC);
				treeSourceName.setValue(treeSourceTask.getName());

				Enumeration e = drawings.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					WideTreeDrawing treeDrawing = (WideTreeDrawing)obj;
					treeDrawing.setTreeSourceTask(treeSourceTask);
					treeDrawing.treeDisplay.pleaseUpdate(false);
				}
				return treeSourceTask;
			}
		}
		else if (checker.compare(this.getClass(), "Shows the contained tree in a separate window", null, commandName, "showContainedTree")) {
			if (depWindowMaker == null) {
				depWindowMaker = (TWindowMaker)hireNamedEmployee(TWindowMaker.class, "#DepContTreeWindow");
				if (depWindowMaker != null)
					depWindowMaker.setTree(originalContainedTree);
			}
			if (depWindowMaker == null)
				return null;
			depWindowMaker.setWindowVisible(true);
			return depWindowMaker;

		}
		else if (checker.compare(this.getClass(), "Goes to the next contained tree", null, commandName, "nextContained")) {
			Enumeration e = drawings.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				WideTreeDrawing treeDrawing = (WideTreeDrawing)obj;
				treeDrawing.nextContained();
			}

		}
		else if (checker.compare(this.getClass(), "Goes to the previous contained tree", null, commandName, "previousContained")) {
			Enumeration e = drawings.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				WideTreeDrawing treeDrawing = (WideTreeDrawing)obj;
				treeDrawing.previousContained();
			}
		}
		else if (checker.compare(this.getClass(), "Sets which contained tree is shown", "[number of contained tree]", commandName, "setContained")) {
			int ic = MesquiteTree.toInternal(MesquiteInteger.fromFirstToken(arguments, pos)); 
			Enumeration e = drawings.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				WideTreeDrawing treeDrawing = (WideTreeDrawing)obj;
				treeDrawing.setContained(ic);
			}
			if (!suppressedSet && suppressed) { //for old version files
				suppressed = false;
			}
		}
		else if (checker.compare(this.getClass(), "Sets the initial x offset from base position for the legend", "[offset]", commandName, "setInitialOffsetX")) {
			int offset= MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(offset)) {
				initialOffsetX = offset;
				if (drawings!=null) {
					Enumeration e = drawings.elements();
					while (e.hasMoreElements()) {
						Object obj = e.nextElement();
						WideTreeDrawing treeDrawing = (WideTreeDrawing)obj;
						treeDrawing.legend.setOffsetX(offset);
					}
				}
			}
		}
		else if (checker.compare(this.getClass(),  "Sets the initial y offset from base position for the legend", "[offset]", commandName, "setInitialOffsetY")) {
			int offset= MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(offset)) {
				initialOffsetY = offset;
				if (drawings!=null) {
					Enumeration e = drawings.elements();
					while (e.hasMoreElements()) {
						Object obj = e.nextElement();
						WideTreeDrawing treeDrawing = (WideTreeDrawing)obj;
						treeDrawing.legend.setOffsetY(offset);
					}
				}
			}
		}
		/*
    	 	else if (checker.compare(this.getClass(), nullxxx, null, commandName, "useOne")) { //TODO: if this is ever used, need to deal with hiring comamnd and so on
			oneTreeSourceTask = (OneTreeSource)hireEmployee(OneTreeSource.class, "Source of contained tree");
			usingOneTreeSource = true;
			Enumeration e = drawings.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				WideTreeDrawing treeDrawing = (WideTreeDrawing)obj;
 				treeDrawing.setTreeSourceTask(oneTreeSourceTask);
				treeDrawing.treeDisplay.pleaseUpdate(true);
			}
    	 		return oneTreeSourceTask;
    	 	}
		 */
		else if (checker.compare(this.getClass(), "Returns the module calculating node locations", null, commandName, "getNodeLocsEmployee")) {
			return nodeLocsTask;
		}
		else {
			return  super.doCommand(commandName, arguments, checker);
		}
		return null;
	}
	/*.................................................................................................................*/
	public boolean showCitation() {
		return true;
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	/** returns current parameters, for logging etc..*/
	public String getParameters() {
		if (reconstructTask!=null && treeSourceTask!=null)
			return "Contained tree source: " + treeSourceTask.getName() + "; Association reconstruced by: " + reconstructTask.getName();
		return "";
	}
	public boolean isPrerelease(){
		return false;
	}

}

/* ======================================================================== */

/* ======================================================================== */
class WideTreeDrawing extends TreeDrawing  {
	public Path2D[] branchPoly;
	public Path2D[] fillBranchPoly;

	private double lastleft;
	private int taxspacing;
	public int highlightedBranch, branchFrom;
	//public double xFrom, yFrom, xTo, yTo;
	public ContainedAssociates ownerModule;
	Color resolvedColor = Color.magenta;
	Color defaultContainedColor = Color.black;
	Color containedColor = defaultContainedColor;
	Color selectedContainedColor = Color.cyan;
	Color migratedColor = Color.yellow;
	static Color defaultBranchColor = ColorDistribution.burlyWood;
	Color containingColor = defaultBranchColor;
	AssociationSource associationTask;
	TaxaAssociation association;
	MesquiteModule treeSourceTask;
	Taxa containedTaxa=null;
	MesquiteTree containedTree;
	MesquiteTree originalContainedTree=null;
	public int edgeWidth = 64;
	int oldNumTaxa = 0;
	Path2D utilityPolygon;
	public static final int inset=1;
	private boolean ready=false;
	private int boxHeight;
	private int foundBranch;
	NameReference triangleNameRef;
	NameReference widthNameReference;
	DoubleArray widths = null;
	double maxWidth = 0;
	//private int[][] contained;
	private double[] miniX, miniY;
	private boolean[] inTree;
	private int oldNumSpaces =0;
	TextRotator textRotator;
	int currentContained = 0;
	public ContainedLegend legend;
	public Tree currentTree=null;
	public Tree currentContainedTree = null;
	Taxa currentContainedTaxa = null;
	TaxaAssociation currentAssociation = null;
	ContainedAssocExtra extra;
	AssociationHistory history;
	ReconstructAssociation reconstructTask;
	
	//MesquiteTimer firstTime, secondTime, thirdTime, d1Time, d2Time, d3Time, d4Time;
	public WideTreeDrawing (TreeDisplay treeDisplay, int numTaxa, ContainedAssociates ownerModule, AssociationSource associationTask, MesquiteModule treeSourceTask, ReconstructAssociation reconstructTask) {
		super(treeDisplay, MesquiteTree.standardNumNodeSpaces(numTaxa));
		this.associationTask = associationTask;
		this.treeSourceTask = treeSourceTask;
		this.reconstructTask = reconstructTask;
		edgeWidth = treeDisplay.getEdgeWidth();
		boxHeight = ownerModule.minimalEdgeWidth/4;
		treeDisplay.setMinimumTaxonNameDistance(boxHeight,  4); 
		treeDisplay.setOrientation(TreeDisplay.UP);

		triangleNameRef = NameReference.getNameReference("triangled");
		widthNameReference = NameReference.getNameReference("width");
		this.ownerModule = ownerModule;
		this.treeDisplay = treeDisplay;
		oldNumTaxa = numTaxa;
		history = new AssociationHistory();

		extra = new ContainedAssocExtra(ownerModule, treeDisplay, this);
		treeDisplay.addExtra(extra);
		treeDisplay.branchColor = containingColor;
		ready = true;
		utilityPolygon=new Path2D.Double();

		legend = new ContainedLegend(ownerModule, this);
		if (treeSourceTask !=null && treeSourceTask instanceof TreeSource && containedTaxa != null)
			legend.adjustScroll(currentContained, ((TreeSource)treeSourceTask).getNumberOfTrees(containedTaxa));
		extra.addPanelPlease(legend);
		legend.setVisible(true);
		resetAssociation(treeDisplay.getTaxa(), true);
		if (treeSourceTask instanceof TreeSource) 
			((TreeSource)treeSourceTask).initialize(containedTaxa);
		else if (treeSourceTask instanceof OneTreeSource)
			((OneTreeSource)treeSourceTask).initialize(containedTaxa);
	}
	public void resetNumNodes(int numNodes){
		super.resetNumNodes(numNodes);
		branchPoly= new Path2D.Double[numNodes];
		fillBranchPoly= new Path2D.Double[numNodes];
		for (int i=0; i<numNodes; i++) {
			branchPoly[i] = new Path2D.Double();
			fillBranchPoly[i] = new Path2D.Double();
		}
	}
	int branchEdgeWidth(int node){
		if (widths !=null && maxWidth!=0 && MesquiteDouble.isCombinable(maxWidth)) {
			double w = widths.getValue(node);
			if (MesquiteDouble.isCombinable(w)) {
				int ew = (int)((w/maxWidth) * edgeWidth);
				if (ew<3)
					ew = 3;
				return ew;
			}
		}	
		return edgeWidth;
	}
	
	void setContainedColor(Color c) {
		containedColor = c;
	}
	void setContainingColor(Color c) {
		containingColor = c;
	}
	/*_________________________________________________*/
	private void UPdefineFillPoly(int node, Path2D poly, boolean internalNode, double Nx, double Ny, double mNx, double mNy, int sliceNumber, int numSlices) {
		if (poly!=null) {
			poly.reset();
			int sliceWidth=branchEdgeWidth(node);
			if (numSlices>1) {
				Nx+= (sliceNumber-1)*(branchEdgeWidth(node)-inset)/numSlices;
				mNx+= (sliceNumber-1)*(branchEdgeWidth(node)-inset)/numSlices;
				sliceWidth=(branchEdgeWidth(node)-inset)-((sliceNumber-1)*(branchEdgeWidth(node)-inset)/numSlices);
			}
			if ((internalNode) && (numSlices==1)){ 
				poly.moveTo(Nx+inset, Ny);
				poly.lineTo(Nx+sliceWidth/2, Ny-sliceWidth/2-inset);
				poly.lineTo(Nx+sliceWidth-inset, Ny);
				poly.lineTo(mNx+sliceWidth-inset, mNy);
				poly.lineTo(mNx+inset, mNy);
				poly.lineTo(Nx+inset, Ny);
			}
			else {
				if (Nx==mNx) {
					if ((internalNode) && (numSlices>1)) {
						Ny-=(branchEdgeWidth(node)-inset)/4;
					}
					poly.moveTo(Nx+inset, Ny+inset);
					poly.lineTo(Nx+sliceWidth-inset, Ny+inset);
					poly.lineTo(mNx+sliceWidth-inset, mNy);
					poly.lineTo(mNx+inset, mNy);
					poly.lineTo(Nx+inset, Ny+inset);
				}
				else if (Nx>mNx) {
					if ((internalNode) && (numSlices>1)) {
						Nx+=(branchEdgeWidth(node)-inset)/4;
						Ny-=(branchEdgeWidth(node)-inset)/4;
					}
					poly.moveTo(Nx, Ny+inset);
					poly.lineTo(Nx+sliceWidth-inset-inset, Ny+inset);
					poly.lineTo(mNx+sliceWidth-inset, mNy);
					poly.lineTo(mNx+inset, mNy);
					poly.lineTo(Nx, Ny+inset);
				}
				else if (Nx<mNx) {
					if ((internalNode) && (numSlices>1)) {
						Nx-=(branchEdgeWidth(node)-inset)/4;
						Ny-=(branchEdgeWidth(node)-inset)/4;
					}
					poly.moveTo(Nx+inset+inset, Ny+inset);
					poly.lineTo(Nx+sliceWidth, Ny+inset);
					poly.lineTo(mNx+sliceWidth-inset, mNy);
					poly.lineTo(mNx+inset, mNy);
					poly.lineTo(Nx+inset+inset, Ny+inset);
				}
			}
		}
	}
	/*_________________________________________________*/
	private void UPCalcFillBranchPolys(Tree tree, int node) {
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			UPCalcFillBranchPolys(tree, d);
		UPdefineFillPoly(node, fillBranchPoly[node], tree.nodeIsInternal(node),x[node],y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], 0, 0);
	}
	/*_________________________________________________*/
	private void UPdefinePoly(int node, Path2D poly, boolean internalNode, double Nx, double Ny, double mNx, double mNy) {
		if (poly!=null) {
			poly.reset();
			if (internalNode&& false) 
			{
				poly.moveTo(Nx, Ny);
				poly.lineTo(Nx+branchEdgeWidth(node)/2, Ny-branchEdgeWidth(node)/2);
				poly.lineTo(Nx+branchEdgeWidth(node), Ny);
				poly.lineTo(mNx+branchEdgeWidth(node), mNy);
				poly.lineTo(mNx, mNy);
				poly.lineTo(Nx, Ny);
			}
			else
			{
				poly.moveTo(Nx, Ny);
				poly.lineTo(Nx+branchEdgeWidth(node), Ny);
				poly.lineTo(mNx+branchEdgeWidth(node), mNy);
				poly.lineTo(mNx, mNy);
				poly.lineTo(Nx, Ny);
			}
		}
	}
	/*_________________________________________________*/
	private void UPCalcBranchPolys(Tree tree, int node)
	{
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			UPCalcBranchPolys(tree, d);
		UPdefinePoly(node, branchPoly[node], tree.nodeIsInternal(node), x[node],y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)]);
	}
	/*_________________________________________________*/
	/*_________________________________________________*/
	private void calculateLines(Tree tree, int node) {
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			calculateLines( tree, d);
		lineTipY[node]=y[node];
		lineTipX[node]=x[node];
		lineBaseY[node]=y[tree.motherOfNode(node)];
		lineBaseX[node]=x[tree.motherOfNode(node)];
	}
	/*_________________________________________________*/
	private void calcBranchPolys(Tree tree, int drawnRoot) {
		if (ownerModule==null) {MesquiteTrunk.mesquiteTrunk.logln("ownerModule null"); return;}
		if (ownerModule.nodeLocsTask==null) {ownerModule.logln("nodelocs task null"); return;}
		if (treeDisplay==null) {ownerModule.logln("treeDisplay null"); return;}
		if (tree==null) { ownerModule.logln("tree null"); return;}
		edgeWidth = treeDisplay.getEdgeWidth();

		ownerModule.nodeLocsTask.calculateNodeLocs(treeDisplay,  tree, drawnRoot,  treeDisplay.getField()); //Graphics g removed as parameter May 02
		calculateLines(tree, drawnRoot);
		if (treeDisplay.getTaxonSpacing()<edgeWidth+2) {
			edgeWidth= treeDisplay.getTaxonSpacing()-2;
			if (edgeWidth<2)
				edgeWidth=2;
		}

		UPCalcBranchPolys(tree, drawnRoot);
		UPCalcFillBranchPolys(tree, drawnRoot);
	}
	public void getSingletonLocation(Tree tree, int N, MesquiteNumber xValue, MesquiteNumber yValue){
		if(tree==null || xValue==null || yValue==null)
			return;
		if(!tree.nodeExists(N))
			return;
		int mother = tree.motherOfNode(N);
		int daughter = tree.firstDaughterOfNode(N);
		if(treeDisplay.getOrientation()==TreeDisplay.UP){
			xValue.setValue(x[daughter]);
			yValue.setValue(y[mother]+(y[daughter]-y[mother])/2);
		}
		else if (treeDisplay.getOrientation()==TreeDisplay.DOWN){
			xValue.setValue(x[daughter]);
			yValue.setValue(y[mother]+(y[mother]-y[daughter])/2);
		}
		else  if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
		//	int offset = (x[N]-x[mother])/2;
			xValue.setValue(x[mother]+(x[daughter]-x[mother])/2);
			yValue.setValue(y[daughter]);
		}
		else  if (treeDisplay.getOrientation()==TreeDisplay.LEFT){
			xValue.setValue(x[daughter]+(x[mother]-x[daughter])/2);
			yValue.setValue(y[daughter]);
		}
	}

	/*_________________________________________________*/
	/** Draw highlight for branch node with current color of graphics context */
	public void drawHighlight(Tree tree, int node, Graphics g, boolean flip){
		tC = g.getColor();
		if (flip)
			g.setColor(Color.red);
		else
			g.setColor(containingColor);
		if (treeDisplay.getOrientation()==TreeDisplay.DOWN || treeDisplay.getOrientation()==TreeDisplay.UP){
			for (int i=0; i<4; i++)
				GraphicsUtil.drawLine(g,x[node]-2 - i, y[node], x[tree.motherOfNode(node)]-2 - i, y[tree.motherOfNode(node)]);
		}
		else {
			for (int i=0; i<4; i++)
				GraphicsUtil.drawLine(g,x[node], y[node]-2 - i, x[tree.motherOfNode(node)], y[tree.motherOfNode(node)]-2 - i);
		}
		g.setColor(tC);
	}
	Color tC;
	/*_________________________________________________*/
	private   void drawBranches(Tree tree, Graphics g, int node) {
		if (tree.nodeExists(node)) {
			if ((tree.getRooted() || tree.getRoot()!=node) && branchPoly[node]!=null) {
				GraphicsUtil.fill(g,branchPoly[node]);
				
			}
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				drawBranches( tree, g, d);
			
		}
	}
	/*_________________________________________________*/
	private   void whiteBranches(Tree tree, Graphics g, int node) {
		if (tree.nodeExists(node)) {
			if (treeDisplay.getBranchColor(node)!=null)
				g.setColor(treeDisplay.getBranchColor(node));
			else
				g.setColor(containingColor);
			if ((tree.getRooted() || tree.getRoot()!=node)) {
				fillBranch(tree, node, g);
			}

			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				whiteBranches( tree, g, d);
			
			if (emphasizeNodes()) {
				Color prev = g.getColor();
				g.setColor(Color.red);//for testing
				GraphicsUtil.fill(g,nodePoly(node));
				g.setColor(prev);
			}
			
		}
	}
	boolean multipleHomes = false;
	boolean firstMultipleHomes = true;
	/*....................................................................................................*/
	/* returns true if at least one of the taxa is in the tree*/
	private boolean containingInTree(Tree tree, Taxon[] containing){
		if (containing == null)
			return false;
		int count = 0;
		for (int i=0; i<containing.length; i++){
			int w = tree.getTaxa().whichTaxonNumber(containing[i]);
			if (w >=0 && tree.nodeOfTaxonNumber(w)>0) {
				count++;
			}
		}
		if (count == 1)
			return true;
		else if (count >1) {
			if (!multipleHomes) {
				String warning = "Terminal taxon of contained tree is associated with more than one terminal taxon in the containing tree.  This is not allowed in the current version of Contained Associates.  (This warning may be given only once during this particular calculation).";
				if (firstMultipleHomes) {
					MesquiteMessage.notifyUser(warning);
				}
				else
					MesquiteMessage.warnUser("Terminal taxon of contained tree is associated with more than one terminal taxon in the containing tree.  This is not allowed in the current version of Contained Associates.  (This warning may be given only once during this particular calculation).");
				firstMultipleHomes = false;
			}
			multipleHomes = true;
		}	
		return false;
	}
	boolean first = true;
	/*....................................................................................................*/
	/*goes through contained tree marking which nodes are in actually in the contained tree (some might not be if
	terminals of the contained associates are contained in containing taxa that are not in the containing tree*/
	private void checkInTree(Tree tree, Tree containedTree, int node) {
		//inTree[node]=false;
		if (containedTree.nodeIsInternal(node)) {
			for (int d = containedTree.firstDaughterOfNode(node); containedTree.nodeExists(d); d = containedTree.nextSisterOfNode(d)) {
				checkInTree(tree, containedTree, d);
				if (inTree[d])
					inTree[node]=true;
			}
		}
		else {
			Taxon t = containedTree.getTaxa().getTaxon(containedTree.taxonNumberOfNode(node));
			Taxon[] containing = association.getAssociates(t);

			if (t == null)
				inTree[node]=false;
			else if (containingInTree(tree, containing)) {
				inTree[node]=true;
			}
			else {
				if (first)
					MesquiteMessage.warnUser("Terminal taxon \"" + t.getName() + "\"  of contained tree is not associated with a terminal taxon in the containing tree (this warning may be given only once during this particular calculation).");
				first = false;
			}
		}
	}
	NameReference migrateRef = NameReference.getNameReference("Migration");
	/*....................................................................................................*/
	private void miniTerminals(Graphics g, Tree tree, int node, int[] terminals, double terminalY, int miniSpacing, boolean atTip, Color containedColor) {
		boolean inA =IntegerArray.inArray(node, terminals);
		if (tree.nodeIsInternal(node) /*&& !inA*/) {
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				miniTerminals(g, tree, d, terminals, terminalY, miniSpacing, atTip, containedColor);
			}
		}
		if (inA && inTree[node]) { //terminal

			lastleft+= miniSpacing;
			double oldX = miniX[node];
			double oldY = miniY[node];

			if (!atTip){ //(tree.nodeIsInternal(node) ||
				boolean useOld = false;
				if (terminalY>= oldY || !legalY(oldY)) { //test for legalY added 9 Nov 01
					useOld = true;
					miniY[node] = terminalY;
					miniX[node] = lastleft;
				}
				if (tree.getSelected(node))
					g.setColor(resolvedColor);
				else if (tree.getAssociatedBit(migrateRef, node))
					g.setColor(migratedColor);
				else
					g.setColor(containedColor);

				if (legalXY(oldX, oldY) && legalXY(miniX[node], miniY[node])){
					GraphicsUtil.drawLine(g,oldX, oldY, miniX[node], miniY[node]);
					GraphicsUtil.drawLine(g,oldX+1, oldY, miniX[node]+1, miniY[node]);
				}
				else if (legalXY(miniX[node], miniY[node])) {
					Color cc = g.getColor();
					g.setColor(Color.red);
					GraphicsUtil.fillOval(g,miniX[node], miniY[node], 5, 5);
					g.setColor(Color.yellow);
					GraphicsUtil.drawOval(g,miniX[node], miniY[node], 5, 5);
					g.setColor(cc);
				}
				else if (legalXY(oldX, oldY)) {
					Color cc = g.getColor();
					g.setColor(Color.red);
					GraphicsUtil.fillOval(g,oldX, oldY, 5, 5);
					g.setColor(Color.yellow);
					GraphicsUtil.drawOval(g,oldX, oldY, 5, 5);
					g.setColor(cc);
				}
			}
			else if (tree.nodeIsTerminal(node)) {
				miniY[node] = terminalY;
				miniX[node] = lastleft;
				if (ownerModule.showContainedTaxNames.getValue()){
				int taxonNumber = tree.taxonNumberOfNode(node);
				//draw only if node has associates and is part of tree
				String s=tree.getTaxa().getName(taxonNumber);
				if (tree.getTaxa().getSelected(taxonNumber))
					g.setColor(selectedContainedColor);
				else
					g.setColor(containedColor);
				textRotator.drawRotatedText(s, taxonNumber, g, treeDisplay, (int)miniX[node]-2, (int)miniY[node]-6);  //integer nodeloc approximation
				}
			}

		}
	}
	/*_________________________________________________*/
	private void miniCalcInternalLocs(Tree tree, int node, int[] terminals) {
		if (!IntegerArray.inArray(node, terminals)) { //internal
			int fD = 0;
			int lD = 0;
			boolean first=true;
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				if (inTree[d]) {
					miniCalcInternalLocs(tree, d, terminals);
					if (first) {
						fD = d;
						first = false;
					}
					lD = d;
				}
			}
			if (fD!=0) {
				//lD = fD;
				double nFDx = miniX[fD];
				double nFDy = miniY[fD];
				double nLDx = miniX[lD];
				double nLDy = miniY[lD];
				miniY[node] = (-nFDx + nLDx+nFDy + nLDy) / 2.0;
				miniX[node] =(nFDx + nLDx - nFDy + nLDy) / 2.0;
			}
		}

	}
	/*_________________________________________________*/
	private void miniSlantInternalLocs(Tree tree, int node, int[] terminals, double yStart, double xDiff, double yDiff) {
		if (yDiff == 0)
			return;
		if (!IntegerArray.inArray(node, terminals)) { //internal
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				if (inTree[d]) {
					miniSlantInternalLocs(tree, d, terminals, yStart, xDiff, yDiff);
				}
			}
			miniX[node] += xDiff*(miniY[node] - yStart)/yDiff;  //(miniY[node] - yStart)/yDiff is proportion of containing branch
		}

	}

	/*-----------------------------------------*/
	/** returns total of branchlengths from node up to tallest terminal, with unassigned lengths given value "perUnassignedLength" */
	public double minimumYOfContained (Tree containingTree, int node) {
		if (containingTree.nodeIsTerminal(node)) {
			Taxon[] t =association.getAssociates(containingTree.getTaxa().getTaxon(containingTree.taxonNumberOfNode(node)));
			if (t==null || t.length==0)
				return MesquiteInteger.unassigned;
			return y[node];
		}
		double minimum = MesquiteDouble.unassigned;
		for (int daughter=containingTree.firstDaughterOfNode(node); containingTree.nodeExists(daughter); daughter = containingTree.nextSisterOfNode(daughter) ) {
			minimum = MesquiteDouble.minimum(minimumYOfContained(containingTree, daughter), minimum);
		}
		return minimum;
	}
	/*....................................................................................................*/
	private void miniScaleInternalLocs (Tree tree, int node, int[] terminals, double top, double cladeTop, int containing, int root, double scaling) {
		
		if (IntegerArray.inArray(node, terminals)){ //internal
			miniY[node]=top;
		}
		else  { //internal
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				if (inTree[d]) {
					miniScaleInternalLocs(tree, d, terminals, top, cladeTop, containing, root, scaling);
				}
			}
			//not just branch length; need to see how much of the branch lengths already used up in descendant containing nodes
			miniY[node]=cladeTop + (int)(tree.tallestPathAboveNode(node, 1.0)*scaling + 0.5); //0.5 to round 
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				if (inTree[d]) {
					if (miniY[node] < miniY[d]) //round-off error might cause node to be above descendants; check here
						miniY[node]=miniY[d];
				}
			}
		}

	}
	/*....................................................................................................*/
	private void OLDminiScaleInternalLocs (Tree tree, int node, int[] terminals, int bottom, double ancH, int root, double scaling) {
		if (!IntegerArray.inArray(node, terminals)) { //internal
			double nH;
			if (node==root) {
				nH=bottom;
			}
			else {
				nH=ancH - (tree.getBranchLength(node, 1.0)*scaling);
			}
			miniY[node]=(int)(nH);
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				if (inTree[d]) {
					OLDminiScaleInternalLocs(tree, d, terminals, bottom, nH, root, scaling);
				}
			}
		}

	}
	/*_________________________________________________*/
	private double miniDepth(Tree tree, int node, int[] terminals) {
		if (!IntegerArray.inArray(node, terminals)) { //internal
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				if (inTree[d]) {
					return tree.getBranchLength(d, 1.0) + miniDepth(tree, d, terminals);
				}
			}
		}
		return 0;
	}
	boolean warned = false;
	boolean legalXY(double x, double y){
		boolean L = (x<treeDisplay.getBounds().width && y>treeDisplay.getBounds().height && y<300000000);
		if (!L)
			L = x>=0 && x<treeDisplay.getBounds().width && y>=0;
			return L;

	}
	boolean legalX(int x){
		return x>=0 && x<treeDisplay.getBounds().width;
	}
	boolean legalY(double y){
		return y>0 && MesquiteDouble.isCombinable(y); // && y<treeDisplay.getBounds().height;
	}
	/*....................................................................................................*/
	private void miniDraw(Graphics g, Tree tree, int node, int[] terminals, Color containedColor) {
		if (!IntegerArray.inArray(node, terminals)) { //internal
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				miniDraw(g, tree, d, terminals, containedColor);
				if (inTree[d] && inTree[node]){

					if (tree.getSelected(d))
						g.setColor(resolvedColor);
					else
						g.setColor(containedColor);

					if (!warned && (!legalY(miniY[d]) || !legalY(miniY[node]))) {
						warned = true;
						ownerModule.logln("There has been an error in drawing a contained tree.  " +  
								"This can sometimes happen if you are modeling coalescence with very short branch lengths (e.g., divergence times of one generation ). " + 
								" Points of error will be shown by a yellow-ringed red spot.  This message will not be repeated, even when the error recurs. (" + miniY[d] + " " + miniY[node] +")");
						ownerModule.showLogWindow();
						treeDisplay.repaint();
						//if (miniY[d]<=0) MesquiteMessage.warnProgrammer("miniDraw Error: miniY[d] <=0 " + d + " miniY[d] " + miniY[d]);
						//if (miniY[node]<=0) MesquiteMessage.warnProgrammer("miniDraw Error: miniY[node] <=0 " + node + " miniY[node] " + miniY[node]);
					}
					if (legalXY(miniX[node], miniY[node]) && legalXY(miniX[d], miniY[d])){
						GraphicsUtil.drawLine(g,miniX[d], miniY[d], miniX[node], miniY[node]);
						GraphicsUtil.drawLine(g,miniX[d]+1, miniY[d], miniX[node]+1, miniY[node]);

					}
					g.setColor(containedColor);
				}
			}
		}
	}
	boolean allLengthsAssigned(Tree tree, int node){
		if (tree.nodeIsTerminal(node))
			return !tree.branchLengthUnassigned(node);

		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			if (!allLengthsAssigned(tree, d)) 
				return false;
		}
		return true;
	}
	/*_________________________________________________*/
	int howMany(int[] nodes){
		if (nodes==null)
			return 0;
		int num=0;
		for (int i=0; i<nodes.length; i++)
			if (nodes[i]!=MesquiteInteger.unassigned)
				num++;
		return num;
	}
	/*_________________________________________________*/
	int[] terminalsOnly(Tree tree, int[] nodes){
		if (nodes==null)
			return null;
		int[] terms = new int[nodes.length];
		IntegerArray.deassignArray(terms);
		int count=0;
		for (int a=0; a<nodes.length; a++) {
			if (MesquiteInteger.isCombinable(nodes[a])){
				boolean ancestor = false;
				for (int d = 0; d<nodes.length; d++){
					if (d!=a && tree.descendantOf(nodes[d], nodes[a]))
						ancestor = true;
				}
				if (!ancestor)
					terms[count++] = nodes[a];
			}
		}
		return terms;
	}

	/*checks if a is among the descendants, or is an ancestor of them*/
	private boolean isInOrAncestor(Tree cTree, int[] descendants, int a){
		if (descendants==null)
			return false;
		for (int i=0; i<descendants.length; i++)
			if (MesquiteInteger.isCombinable(a) && MesquiteInteger.isCombinable(descendants[i]) && (descendants[i]==a || cTree.descendantOf(descendants[i],a)))
				return true;
		return false;
	}
	int[] tempA=new int[1];
	/*_________________________________________________*/
	private boolean sisterAmong(Tree cTree, int[] aNodes, int i){
		for (int j =0; j<aNodes.length; j++)
			if (i !=j && cTree.nodesAreSisters(aNodes[i], aNodes[j]) && cTree.nodeIsFirstDaughter(aNodes[i]))
				return true;
		return false;
	}
	
	/*private int highestContaining(Tree tree, int node){
		if (tree.nodeIsTerminal(node))
			return y[node];
		int max = 0;
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			int h = highestContaining( tree, d);
			if (h>max)
				max = h;
		}
		return max;
	}*/
	/*draws small tree at each containing node, to show coalescences within that containing branch*/
	private   void drawMiniTrees(Tree containedTree, Tree tree, int containingNode,  Graphics g, boolean atTip) {
		int[] cNodes = history.getContainedNodes(containingNode);
		if (cNodes == null || cNodes.length == 0)
			return;
		for (int i=0; i<cNodes.length; i++) {
			if (MesquiteInteger.isCombinable(cNodes[i]))
				inTree[cNodes[i]]=true;
		}

		int[] terminals = cNodes; //terminalsOnly(containedTree, cNodes);
		int[] aNodes;
		if (tree.getRoot()==containingNode) {
			tempA[0] = containedTree.getRoot();
			aNodes = tempA; // aNodes = history.condenseClades(containedTree, cNodes);// 
		}
		else
			aNodes = history.getContainedNodes(tree.motherOfNode(containingNode));//aNodes = history.condenseClades(containedTree, cNodes);//
		double xC= x[containingNode];
		double yC= y[containingNode];

		int taxaSpacing = branchEdgeWidth(containingNode)/(howMany(terminals) +1);
		lastleft = xC;
		for (int i=0; i<aNodes.length; i++) {
			Color cc = containedColor;  //if sister clade isn't 

			if (!MesquiteInteger.isCombinable(aNodes[i]))
				return;
			else if (isInOrAncestor(containedTree, cNodes,aNodes[i])) {

				miniTerminals(g, containedTree, aNodes[i], terminals, yC, taxaSpacing, atTip, cc);
				miniCalcInternalLocs(containedTree, aNodes[i], terminals);
				int mother = tree.motherOfNode(containingNode);
				double ySpan = (yC-y[tree.motherOfNode(containingNode)]);
				if (ownerModule.scale.getValue() && allLengthsAssigned(containedTree, containedTree.getRoot())){
					double scaling = 1.0;
					double top = yC;
					double cladeTop = minimumYOfContained(tree, containingNode); 
					if (tree.tallestPathAboveNode(tree.getRoot(), 1.0) ==0){ //tree.tallestPathAboveNode(tree.getRoot(), 1.0)
						scaling = 0.1;  //this is arbitrary; scale not shown anyway
					}
					else
						scaling = (Math.abs(minimumYOfContained(tree, tree.getRoot())-y[tree.getRoot()])/(tree.tallestPathAboveNode(tree.getRoot(), 1.0)));
					
					//if this is the root and branch lengths are not being shown for the containing tree, don't scale
					if (tree.hasBranchLengths() && (treeDisplay.showBranchLengths || (true|| containingNode!=tree.getRoot()))){
						miniScaleInternalLocs (containedTree, aNodes[i], terminals,  top, cladeTop, containingNode, aNodes[i], scaling);
					}

				}
				if (ySpan!=0)
					miniSlantInternalLocs(containedTree, aNodes[i], terminals, yC, xC-x[tree.motherOfNode(containingNode)], ySpan);
				miniDraw(g, containedTree, aNodes[i], terminals, cc);
			}
		}
	}
	/*_________________________________________________*/
	private   void drawContained(Tree tree, Graphics g, int node) {
		if (tree.nodeExists(node)) {
			//if (contained[node]!=null) {
			if ( history.getNumberContainedNodes(node) !=0) {
				for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
					drawContained( tree, g, d);
				}
				//	if (tree.nodeIsTerminal(node))
				g.setColor(containedColor);
				//	else
				//		g.setColor(Color.pink);
				drawMiniTrees(containedTree, tree, node, g, tree.nodeIsTerminal(node));
			}
		}
	}
	/*_________________________________________________*/
	public void setTreeSourceTask (MesquiteModule module){
		this.treeSourceTask = module;
		currentContained = 0;
		if (treeSourceTask instanceof TreeSource)
			((TreeSource)treeSourceTask).initialize(containedTaxa);
		else if (treeSourceTask instanceof OneTreeSource)
			((OneTreeSource)treeSourceTask).initialize(containedTaxa);
		if (treeSourceTask !=null && treeSourceTask instanceof TreeSource && containedTaxa != null){
			legend.adjustScroll(currentContained, ((TreeSource)treeSourceTask).getNumberOfTrees(containedTaxa));
			((TreeSource)treeSourceTask).setPreferredTaxa(containedTaxa);
		}
		currentContainedTree=null; 
		treeDisplay.pleaseUpdate(true);
	}
	/*_________________________________________________*/
	public void nextContained (){
		if (treeSourceTask instanceof OneTreeSource)
			return;
		if (containedTaxa == null)
			return;
		if (currentContained>=((TreeSource)treeSourceTask).getNumberOfTrees(containedTaxa)-1)
			currentContained=0;
		else
			currentContained++;
		currentContainedTree = null;
		legend.adjustScroll(currentContained, ((TreeSource)treeSourceTask).getNumberOfTrees(containedTaxa));
		treeDisplay.pleaseUpdate(true);
	}
	/*_________________________________________________*/
	public void previousContained (){
		if (treeSourceTask instanceof OneTreeSource)
			return;
		if (containedTaxa == null)
			return;
		if (currentContained<=0)
			currentContained=((TreeSource)treeSourceTask).getNumberOfTrees(containedTaxa)-1;
		else
			currentContained--;
		currentContainedTree = null;
		legend.adjustScroll(currentContained, ((TreeSource)treeSourceTask).getNumberOfTrees(containedTaxa));
		treeDisplay.pleaseUpdate(true);
	}
	/*_________________________________________________*/
	public void setContained (int index){
		if (treeSourceTask instanceof OneTreeSource)
			return;
		resetAssociation(treeDisplay.getTaxa(), true);
		if (containedTaxa == null)
			return;
		if (index<((TreeSource)treeSourceTask).getNumberOfTrees(containedTaxa) && index>=0)
			currentContained=index;
		currentContainedTree = null;
		legend.adjustScroll(currentContained, ((TreeSource)treeSourceTask).getNumberOfTrees(containedTaxa));
		if (!MesquiteThread.isScripting()){
			recalculatePositions(rememberedTree);
			treeDisplay.repaintAll();
			treeDisplay.pleaseUpdate(true);
		}
	}
	/*_________________________________________________*/
	public void zapContained(){
		currentContainedTree=null;
	}
	void containedSourceChanged(){
		if (treeSourceTask!=null && legend!=null && treeSourceTask instanceof TreeSource) {
			int numtrees =  ((TreeSource)treeSourceTask).getNumberOfTrees(containedTaxa);
			if (currentContained>=numtrees)
				currentContained = numtrees-1;
			legend.adjustScroll(currentContained, numtrees);
			((TreeSource)treeSourceTask).setPreferredTaxa(containedTaxa);
		}
	}
	/*_________________________________________________*/
	public void resetAssociation(Taxa containingTaxa, boolean force){
		if (ownerModule.suppressed) {
			force = true;
			return;
		}
		if (force || association == null || (association.getTaxa(0)!= containingTaxa && association.getTaxa(1)!= containingTaxa)) {
			association = associationTask.getCurrentAssociation(containingTaxa);
			if (association == null)
				association = associationTask.getAssociation(containingTaxa, 0);
			if (association == null)
				return;
			if (association.getTaxa(0)== containingTaxa)
				containedTaxa = association.getTaxa(1);
			else
				containedTaxa = association.getTaxa(0);
			if (treeSourceTask instanceof TreeSource) {
				legend.adjustScroll(currentContained, ((TreeSource)treeSourceTask).getNumberOfTrees(containedTaxa));
				((TreeSource)treeSourceTask).setPreferredTaxa(containedTaxa);
			}
		}
		resetContainedTree(containedTaxa); 
	}
	/*_________________________________________________*/
	private void resetContainedTree(Taxa containedTaxa){
		if (ownerModule.suppressed || ownerModule.isDoomed())
			return;
		Tree t;
		if (treeSourceTask instanceof OneTreeSource)
			t = ((OneTreeSource)treeSourceTask).getTree(containedTaxa);
		else
			t = ((TreeSource)treeSourceTask).getTree(containedTaxa, currentContained); 
		if (t instanceof MesquiteTree)
			originalContainedTree = (MesquiteTree)t;
		else {
			if (t == null) {
				if (MesquiteThread.isScripting())
					MesquiteMessage.warnUser("Error: no contained tree " + currentContained);
					else MesquiteMessage.warnProgrammer("Error: no contained tree " + currentContained);
			}
			else
				MesquiteMessage.warnProgrammer("Error: contained tree not of class MesquiteTree ");
			originalContainedTree = null;
		}
		ownerModule.originalContainedTree = originalContainedTree;
		if (ownerModule.depWindowMaker!=null)
			ownerModule.depWindowMaker.setTree(originalContainedTree);
	}
	/*_________________________________________________*/
	private double findMaxWidth(Tree tree, int node) {
		if (!tree.getAssociatedBit(triangleNameRef,node)) {
			if (tree.nodeIsTerminal(node))
				return widths.getValue(node);

			double mw = MesquiteDouble.unassigned;
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				mw = MesquiteDouble.maximum(mw, findMaxWidth(tree, d));
			return mw;
		}
		return (MesquiteDouble.unassigned);
	}
	MesquiteNumber cost = new MesquiteNumber();
	MesquiteString resultString = new MesquiteString();

	Tree rememberedTree = null;
	void desuppress(){
		recalculatePositions(rememberedTree);
		treeDisplay.pleaseUpdate(false);
	}
	/*_________________________________________________*/
	public   void recalculatePositions(Tree tree) {  
		if (ownerModule.suppressed) {
			rememberedTree = tree;
			return;
		}
		if (MesquiteTree.OK(tree)) {

			if (tree.getNumNodeSpaces()!=numNodes)
				resetNumNodes(tree.getNumNodeSpaces());
			widths = tree.getWhichAssociatedDouble(widthNameReference);
			if (widths!=null)
				maxWidth = findMaxWidth(tree, tree.getRoot());

			if (!tree.nodeExists(getDrawnRoot()))
				setDrawnRoot(tree.getRoot());
			int drawnRoot = getDrawnRoot();
			resetAssociation(tree.getTaxa(), rememberedTree == tree);
			rememberedTree = tree;
			calcBranchPolys(tree, drawnRoot);
			if (association == null) {
				originalContainedTree = null;
				return;
			}
			if (originalContainedTree== null || containedTaxa !=currentContainedTaxa || currentContainedTree==null) 
				resetContainedTree(containedTaxa); 

			if (originalContainedTree !=null){
				if (miniX==null || oldNumSpaces != originalContainedTree.getNumNodeSpaces()){
					miniX = new double[originalContainedTree.getNumNodeSpaces()];
					miniY = new double[originalContainedTree.getNumNodeSpaces()];
					inTree = new boolean[originalContainedTree.getNumNodeSpaces()];
					oldNumSpaces = originalContainedTree.getNumNodeSpaces();
				}
				if (tree!=currentTree || tree.getVersionNumber()!=currentTree.getVersionNumber() || containedTaxa != currentContainedTaxa || currentContainedTree != originalContainedTree || association!= currentAssociation) {
					cost.setToUnassigned();
					if (originalContainedTree!=null) {
						if (containedTree!=null && containedTree instanceof MesquiteTree && originalContainedTree instanceof MesquiteTree && containedTaxa == currentContainedTaxa)
							containedTree.setToClone(originalContainedTree);
						else
							containedTree = originalContainedTree.cloneTree(); //no need to establish listener to Taxa, as will be remade when needed?
					}

					history = reconstructTask.reconstructHistory(tree, containedTree, association, cost, resultString); //TODO: pass back string describing cost (e.g. "deep coalescence cost"
					if (history != null){
						MesquiteInteger duplications = new MesquiteInteger(0);
						MesquiteInteger extinctions = new MesquiteInteger(0);
						history.countDuplicationsExtinctions(tree, containedTree, duplications, extinctions);
						resultString.append("\nor:  " + duplications.getValue() + " Duplications, " + extinctions.getValue() + " Extinctions\n");	
					}
				}
			}
			if (originalContainedTree == null) {
				return;
			}

			for (int i=0; i< inTree.length; i++)
				inTree[i] = false;
			multipleHomes = false;
			if (association !=null)
				checkInTree(tree, containedTree, containedTree.getRoot());
			DoubleArray.zeroArray(miniX);
			DoubleArray.zeroArray(miniY);
			currentTree=tree;
			currentContainedTree = originalContainedTree;

			currentContainedTaxa = containedTaxa;
			currentAssociation = association;
		}
	}
	/*_________________________________________________*/
	public   void drawTree(Tree tree, int drawnRoot, Graphics g) { //TODO: use drawnRoot
		if (ownerModule.suppressed)
			return;
		if (MesquiteTree.OK(tree)) {

			if (tree.getNumNodeSpaces()!=numNodes)
				resetNumNodes(tree.getNumNodeSpaces());
			if (legend!=null){
				legend.adjustLocation();
				legend.setVisible(true);
			}

			if (textRotator == null || textRotator.getNumStrings() < containedTaxa.getNumTaxa())
				textRotator = new TextRotator(containedTaxa.getNumTaxa()+2);

			String tex = "";
			if (originalContainedTree == null)
				tex += "Contained tree is unavailable";
			else 
				tex += originalContainedTree.getName() + ParseUtil.lineEnding() + resultString;
			tex += ParseUtil.lineEnding() +  "[" + treeSourceTask.getNameAndParameters() + "]";
			if (legend!=null){
				legend.setText(tex);
				legend.repaint();
			}
			g.setColor(containingColor);
			drawBranches(tree, g, drawnRoot);  
			g.setColor(Color.white);
			whiteBranches(tree, g, drawnRoot);  
			if (!multipleHomes) {
				g.setColor(resolvedColor);
				if (association!=null && tree!=null && containedTree !=null) {
					drawContained(tree, g, drawnRoot); 
				} 
			}
			g.setColor(Color.black);
		}
	}

	/*_________________________________________________*/
	public  void fillTerminalBox(Tree tree, int node, Graphics g) {
		Rectangle2D box;
		int ew = edgeWidth-2;

		box = new Rectangle2D.Double(x[node], y[node]-ew-3, ew, boxHeight);
		GraphicsUtil.fillRect(g, box.getX(), box.getY(), box.getWidth(), box.getHeight());
		g.setColor(Color.black);
		GraphicsUtil.drawRect(g, box.getX(), box.getY(), box.getWidth(), box.getHeight());
	}

	/*_________________________________________________*/
	public  void fillTerminalBoxWithColors(Tree tree, int node, ColorDistribution colors, Graphics g){
		Rectangle2D box;
		int ew = edgeWidth-2;
		int numColors = colors.getNumColors();
		if (numColors == 0) numColors = 1;

		box = new Rectangle2D.Double(x[node], y[node]-ew-3, ew, boxHeight);
		for (int i=0; i<colors.getNumColors(); i++) {
			Color color;
			if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
				g.setColor(color);
			GraphicsUtil.fillRect(g,box.getX() + (i*box.getWidth()/numColors), box.getY(), box.getWidth()-  (i*box.getWidth()/numColors), box.getHeight());
		}
		g.setColor(Color.black);
		GraphicsUtil.drawRect(g, box.getX(), box.getY(), box.getWidth(), box.getHeight());
	}
	/*_________________________________________________*/
	public void fillBranchWithColors(Tree tree, int node, ColorDistribution colors, Graphics g) {
		if (node>0 && (tree.getRooted() || tree.getRoot()!=node)) {
			int numColors = colors.getNumColors();

			for (int i=0; i<numColors; i++) {
				UPdefineFillPoly(node, utilityPolygon, tree.nodeIsInternal(node), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], i+1, colors.getNumColors());
				Color color;
				if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
					g.setColor(color);
				GraphicsUtil.fill(g, utilityPolygon);
			}
			g.setColor(Color.black);
		}
	}
	/*_________________________________________________*/
	public   void fillBranch(Tree tree, int node, Graphics g) {
		if (fillBranchPoly[node] !=null && node>0 && (tree.getRooted() || tree.getRoot()!=node)) {
			GraphicsUtil.fill(g, fillBranchPoly[node]);
		}
	}

	/*_________________________________________________*/
	public Path2D nodePoly(int node) {
		int offset = (getNodeWidth()-getEdgeWidth())/2;
		int halfNodeWidth = getNodeWidth()/2;
		double startX =0;
		double startY =0;
		if (treeDisplay.getOrientation()==TreeDisplay.UP || treeDisplay.getOrientation()==TreeDisplay.DOWN){
			startX = x[node]-offset;
			startY= y[node]-offset;
		}	else if (treeDisplay.getOrientation()==TreeDisplay.RIGHT || treeDisplay.getOrientation()==TreeDisplay.LEFT){
			startX = x[node];
			startY= y[node]-offset;
		}
		Path2D poly = new Path2D.Double();
		poly.moveTo(startX,startY);
		poly.lineTo(startX+getNodeWidth(),startY);
		poly.lineTo(startX+getNodeWidth(),startY+offset*2);
		poly.lineTo(startX,startY+offset*2);
		poly.lineTo(startX,startY);
		return poly;
	}
	/*_________________________________________________*/
	public boolean inNode(int node, int x, int y){
		Path2D nodeP = nodePoly(node);
		if (nodeP!=null && nodeP.contains(x,y))
			return true;
		else
			return false;
	}
	/*_________________________________________________*/
	private void ScanBranches(Tree tree, int node, int x, int y, MesquiteDouble fraction){
		if (foundBranch==0) {
			if (branchPoly != null && branchPoly[node] != null && branchPoly[node].contains(x, y) || inNode(node,x,y)){
				foundBranch = node;
				if (fraction!=null)
					if (inNode(node,x,y))
						fraction.setValue(ATNODE);
					else {
						int motherNode = tree.motherOfNode(node);
						fraction.setValue(EDGESTART);  //TODO: this is just temporary: need to calculate value along branch.
						if (tree.nodeExists(motherNode)) 
							fraction.setValue(GraphicsUtil.fractionAlongLine(x, y, this.x[motherNode], this.y[motherNode], this.x[node], this.y[node],false,true));
						}
					}
			}
			if (!tree.getAssociatedBit(triangleNameRef, node)) 
				for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
					ScanBranches(tree, d, x, y, fraction);

		}
	/*_________________________________________________*/
	public   int findBranch(Tree tree, int drawnRoot, int x, int y, MesquiteDouble fraction) { 
		if (MesquiteTree.OK(tree) && ready) {
			foundBranch=0;
			ScanBranches(tree, drawnRoot, x, y, fraction);
			if (foundBranch == tree.getRoot() && !tree.getRooted())
				return 0;
			else
				return foundBranch;
		}
		return 0;
	}

	/*_________________________________________________*/
	public void reorient(int orientation) {
	//	treeDisplay.setOrientation(orientation);
	//	treeDisplay.pleaseUpdate(true);
	}
	/*_________________________________________________*/
	public void setEdgeWidth(int edw) {
		edgeWidth = edw;
	}
	/*New code Feb.22.07 allows eavesdropping on edgewidth by the TreeDrawing oliver*/ //TODO: delete new code comments
	/*_________________________________________________*/
	public int getEdgeWidth() {
		return edgeWidth;
	}
	/*End new code Feb.22.07 oliver*/
	/*_________________________________________________*/
	public   void dispose() { 
		for (int i=0; i<numNodes; i++) {
			branchPoly[i] = null;
			fillBranchPoly[i] = null;
		}
		extra.removePanelPlease(legend);
		super.dispose();
	}

}

/* ======================================================================== */
class ContainedLegend extends TreeDisplayLegend {
	private ContainedAssociates ownerModule;
	public MiniScroll treeScroll = null;
	private TreeDrawing tcOp;
	private static final int defaultLegendWidth=142;
	private static final int defaultLegendHeight=160;
	private int oldNumTrees = 0;
	private int oldCurrentTree = -1;
	final int scrollAreaHeight = 41;
	String treeName = "Untitled tree";
	TextArea text;
	public ContainedLegend(ContainedAssociates ownerModule, TreeDrawing tcOp) {
		super(tcOp.treeDisplay,defaultLegendWidth, defaultLegendHeight);
		setVisible(false);
		this.tcOp = tcOp;
		this.ownerModule = ownerModule;

		setOffsetX(ownerModule.initialOffsetX);
		setOffsetY(ownerModule.initialOffsetY);
		setLayout(null);
		setSize(legendWidth, legendHeight);
		add(text = new TextArea("", 6, 3, TextArea.SCROLLBARS_NONE));
		text.setEditable(false);  
		text.setBackground(Color.lightGray);
		text.setBounds(0, scrollAreaHeight, legendWidth, legendHeight-scrollAreaHeight);
		text.setLocation(0, scrollAreaHeight);
		text.setVisible(true);

	}

	public void adjustScroll(int currentTree, int numTrees) {
		if (treeScroll == null) {
			treeScroll = new MiniScroll(MesquiteModule.makeCommand("setContained",  ownerModule), false, MesquiteTree.toExternal(currentTree), MesquiteTree.toExternal(0),  MesquiteTree.toExternal(numTrees-1),"tree");
			add(treeScroll);
			treeScroll.setLocation(2,18);
			treeScroll.setColor(Color.blue);
			repaint();
			oldNumTrees = numTrees;
			oldCurrentTree = currentTree;
		}
		else {
			if (oldNumTrees != numTrees) {
				treeScroll.setMaximumValue(MesquiteTree.toExternal(numTrees -1));
				oldNumTrees = numTrees;
			}
			if (oldCurrentTree != currentTree) {
				treeScroll.setCurrentValue(MesquiteTree.toExternal(currentTree));
				oldCurrentTree = currentTree;
			}
		}
	}
	public void setText(String s){
		treeName = s;
		text.setText(s);
	}
	public void setVisible(boolean b) {
		super.setVisible(b);
		if (treeScroll !=null) {
			treeScroll.setVisible(b);
		}
	}

	public void paint(Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		g.setColor(Color.cyan);
		g.drawRect(0, 0, legendWidth-1, legendHeight-1);
		g.drawLine(0, scrollAreaHeight, legendWidth-1, scrollAreaHeight);
		g.setColor(Color.blue);
		g.drawString("Contained Tree", 4, 14);
		g.setColor(Color.black);
		if (text.getBackground() != getBackground())
			text.setBackground(getBackground());
		MesquiteWindow.uncheckDoomed(this);
	}


}
/* ======================================================================== */
class ContainedAssocExtra extends TreeDisplayDrawnExtra {
	WideTreeDrawing drawing;
	public ContainedAssocExtra (MesquiteModule ownerModule, TreeDisplay treeDisplay, WideTreeDrawing drawing) {
		super(ownerModule, treeDisplay);
		this.drawing = drawing;
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
	}
	public   void setTree(Tree tree) {
		drawing.currentTree = null; //to indicate that association history needs recaclualting
	}
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
	}
	
	/**to inform TreeDisplayExtra that cursor has just entered branch N*/
	public void cursorEnterBranch(Tree tree, int N, Graphics g){
		int numbersAtEnd = drawing.history.getNumberContainedNodes(N);
		int numbersAtStart = -1;
		int anc = tree.parentOfNode(N,1);
		if (anc>=0){
			numbersAtStart = drawing.history.getNumberContainedNodes(anc);
			MesquiteMessage.println("Number of contained branches at start: "+numbersAtStart + ",  at end: " + numbersAtEnd);
		}
		else 
			MesquiteMessage.println("Number of contained branches at end: " + numbersAtEnd);
		super.cursorEnterBranch(tree, N, g);
	}

	
	
}


