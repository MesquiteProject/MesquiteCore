/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.genomic.ShowMatrixInTreeWindow;

import java.util.*;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import mesquite.categ.lib.CategoricalData;
import mesquite.categ.lib.CategoricalState;
import mesquite.categ.lib.MolecularData;
import mesquite.charMatrices.BasicDataWindowCoord.BasicDataWindowCoord;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDisplayActive;
import mesquite.lib.tree.TreeDisplayBkgdExtra;
import mesquite.lib.tree.TreeDisplayEarlyExtra;
import mesquite.lib.tree.TreeDisplayExtra;
import mesquite.lib.tree.TreeDisplayHolder;
import mesquite.lib.tree.TreeDisplayRequests;
import mesquite.lib.tree.TreeDrawing;
import mesquite.lib.tree.TreeTool;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.ColorTheme;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.Legend;
import mesquite.lib.ui.MQPanel;
import mesquite.lib.ui.MesquiteImage;
import mesquite.lib.ui.MesquitePopup;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lib.ui.MessagePanel;
import mesquite.lib.ui.RadioButtons;
import mesquite.lib.ui.TextRotator;

/* ======================================================================== */
public class ShowMatrixInTreeWindow extends TreeWindowAssistantI implements ItemListener  {
	//	Vector extras = new Vector();
	ShowMatrixLinkedExtra extra;
	/*.................................................................................................................*/
	CharacterData data = null;
	ListableVector datas = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		datas = getProject().getCharacterMatrices();
		if (datas != null)
			datas.addListener(this);
		return true;
	}
	public void endJob(){
		if (datas != null)
			datas.removeListener(this);
		super.endJob();
	}
	//This method is optional for TreeWindowAssistants, unlike TreeDisplayAssistants
	public TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay){
		extra = new ShowMatrixLinkedExtra(this, treeDisplay);
		//	extras.addElement(extra);
		return extra;
	}



	public boolean isSubstantive(){
		return false;
	}

	// settings ******
	boolean showMatrix = false;
	int choose0Link1 = 0;
	boolean selectedCharatersOnly = false;
	boolean showControlIcon = true;
	boolean useTraceColors = true;
	int fieldWidth = 0;
	// ******************

	/* ................................................................................................................. */

	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		StringUtil.appendXMLTag(buffer, 2, "showControlIcon", showControlIcon);  
		StringUtil.appendXMLTag(buffer, 2, "useTraceColors", useTraceColors);  
		return buffer.toString();
	}
	/* ................................................................................................................. */

	public void processSingleXMLPreference (String tag, String content) {
		if ("showControlIcon".equalsIgnoreCase(tag))
			showControlIcon = MesquiteBoolean.fromTrueFalseString(content);
		if ("useTraceColors".equalsIgnoreCase(tag))
			useTraceColors = MesquiteBoolean.fromTrueFalseString(content);
	}

	String treeName = null;
	boolean warned = false;
	Taxa taxa = null;
	Tree tree = null;
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		treeName = tree.getName();
		taxa = tree.getTaxa();
		this.tree = tree;
		if (choose0Link1 == 1)
			resetMatrix(tree);
	}


	void resetMatrix(Tree tree){
		if (!showMatrix)
			return;
		CharacterData oldData = data;
		if (choose0Link1 == 0){
			data = getProject().chooseData(containerOfModule(), taxa, null, "Choose matrix to show with tree");
		}
		else if (tree != null) {
			//here also look for matrix from source if needed
			CharacterData d = ((MesquiteTree)tree).findLinkedMatrix(getProject());
			if (d != null){
				data = d;
			}
			else {
				data = null;
			}
		}
		else 
			data = getProject().getCharacterMatrix(taxa, 0);
		if (data!= oldData){
			if (oldData != null)
				oldData.removeListener(this);
			if (data != null)
				data.addListener(this);
			if (extra!= null)
				extra.forceRefresh();

		}
		if (data == null){
			showMatrix = false;
			if (extra!= null)
				extra.turnOnOff(showMatrix);
		}
	}
	/* ................................................................................................................. */
	/** passes which object changed (from MesquiteListener interface) */
	public void changed(Object caller, Object obj, Notification notification) {
		int code = Notification.getCode(notification);
		int[] parameters = Notification.getParameters(notification);
		if (obj instanceof CharacterData || obj instanceof ListableVector ) {
			resetMatrix(tree);
			extra.forceRefresh();
		}
	}

	Checkbox showControlIconCB;
	ExtensibleDialog dialog;
	boolean queryOptions(boolean linkOnly, boolean notify, boolean fromIcon){
		int numMatrices = 0;
		if (taxa != null)
			numMatrices = getProject().getNumberCharMatrices(taxa);
		if (numMatrices == 0){
			alert("Sorry, there are no matrices available for this set of taxa");
			return false;
		}
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		dialog = new ExtensibleDialog(containerOfModule(),  "Show matrix with tree?",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		dialog.addLabel("Do you want to show a matrix with the tree?");
		dialog.addLabelSmallText("Note: the matrix will be visible only when the tree is shown in horizontal or vertical orientation.");
		RadioButtons choices = null;
		if (!linkOnly){	
			if (numMatrices >1){
				dialog.addBlankLine();
				choices = dialog.addRadioButtons (new String[]{"Choose matrix to show", "Show matrix linked to tree, if any          "}, choose0Link1);
				dialog.addBlankLine();
			}
		}
		Checkbox selectedOnly = dialog.addCheckBox("Selected characters only", selectedCharatersOnly);
		Checkbox useTraceColorsCB = dialog.addCheckBox("For standard categorical matrices, use trace colours", useTraceColors);
		dialog.addHorizontalLine(1);
		showControlIconCB = dialog.addCheckBox("Show matrix link icon in tree window", showControlIcon);
		showControlIconCB.addItemListener(this);
		if (fromIcon){
			dialog.addBlankLine();
			dialog.addLabelSmallText("(This dialog also available via menu Tree>Show Matrix in Tree Window.)");
		}
		dialog.addAuxiliaryDefaultPanels();
		String noButton = "Cancel";
		String yesButton = "Show Matrix";
		if (showMatrix) {
			noButton = "Turn Off";
			yesButton = "OK";
		}
		dialog.addPrimaryButtonRow(yesButton, noButton);

		dialog.prepareAndDisplayDialog();
		boolean wasShowIcon = showControlIcon;
		// button 0 is show, 1 is don't show
		if (buttonPressed.getValue()==0)  {
			if (choices != null)
				choose0Link1= choices.getValue();
			else if (linkOnly)
				choose0Link1 = 1;
			else
				choose0Link1 = 0;
			selectedCharatersOnly = selectedOnly.getState();
			useTraceColors = useTraceColorsCB.getState();
			showControlIcon = showControlIconCB.getState();
			showMatrix = true;
		}
		else {
			showMatrix = false;
			showControlIcon = showControlIconCB.getState();
		}
		dialog.dispose();

		if (showMatrix)
			resetMatrix(tree);
		if (extra!= null)
			extra.turnOnOff(showMatrix);

		if (buttonPressed.getValue()==0 && notify)  
			parametersChanged();
		else if (wasShowIcon != showControlIcon)
			parametersChanged();
		storePreferences();
		return true;
	}

	public void itemStateChanged(ItemEvent e){
		if (e.getItemSelectable() instanceof Checkbox){
			if (dialog != null){
				if (dialog.getPrimaryButtonLabel(1).equals("Cancel"))
					dialog.resetPrimaryButtonLabel(1, "Done");
			}
		}

	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {

		Snapshot sn = new Snapshot();
		sn.addLine("chooseOrLink " + choose0Link1);
		sn.addLine("fieldWidth " + fieldWidth);
		if (data != null && choose0Link1 == 0)
			sn.addLine("setMatrix " +getProject().getCharMatrixReferenceExternal(data));
		sn.addLine("selectedOnly " + selectedCharatersOnly);
		sn.addLine("showControlIcon " + showControlIcon);
		sn.addLine("useTraceColors " + useTraceColors);
		sn.addLine("showMatrix " + showMatrix);
		return sn;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Queries options", null, commandName, "queryOptions")) {
			queryOptions(false, true, false);
		}
		else if (checker.compare(this.getClass(), "Queries options", null, commandName, "queryOptionsFromIcon")) {
			queryOptions(false, true, true);
		}
		else if (checker.compare(this.getClass(), "Queries options", null, commandName, "queryLinkedOptions")) {
			queryOptions(true, true, false);
		}
		else if (checker.compare(this.getClass(), "Whether to show a matrix", "[true/false]", commandName, "showMatrix")) {
			showMatrix = MesquiteBoolean.fromTrueFalseString(arguments);
			if (extra!= null){
				extra.turnOnOff(showMatrix);
				//extra.forceRefresh();
			}
		}
		else if (checker.compare(this.getClass(), "Turns off showing a matrix", "[]", commandName, "hideMatrix")) {
			showMatrix = false;
			if (extra!= null){
				extra.turnOnOff(false);
			}
		}
		else if (checker.compare(this.getClass(), "Shows the linked matrix", "[]", commandName, "showLinked")) {
			showMatrix = true;
			choose0Link1 = 1;
			if (extra!= null){
				resetMatrix(extra.getTreeDisplay().getTree());
				extra.turnOnOff(true);
				//extra.forceRefresh();
			}
		}
		else if (checker.compare(this.getClass(), "Whether to show selected characters only", "[true/false]", commandName, "selectedOnly")) {
			selectedCharatersOnly = MesquiteBoolean.fromTrueFalseString(arguments);
		}
		else if (checker.compare(this.getClass(), "Whether to show use trace colors for standard categorical matrices", "[true/false]", commandName, "useTraceColors")) {
			useTraceColors = MesquiteBoolean.fromTrueFalseString(arguments);
		}
		else if (checker.compare(this.getClass(), "Whether to show the control icon", "[true/false]", commandName, "showControlIcon")) {
			showControlIcon = MesquiteBoolean.fromTrueFalseString(arguments);
		}
		else if (checker.compare(this.getClass(), "Choose matrix vs. show one linked to tree", "[0 choose 1 link]", commandName, "chooseOrLink")) {
			int cL = MesquiteInteger.fromString(arguments);
			if (cL == 0 || cL ==1){
				choose0Link1 = cL;
			}
		}
		else if (checker.compare(this.getClass(), "Which matrix to show", "[matrix only]", commandName, "setMatrix")) {
			String dataReference =parser.getFirstToken(arguments);
			CharacterData d = getProject().getCharacterMatrixByReference(checker.getFile(), taxa, null, dataReference, true);
			if (d != null){
				data = d;
			}
		}
		else if (checker.compare(this.getClass(), "Sets field width", "[pixels]", commandName, "fieldWidth")) {
			int cL = MesquiteInteger.fromString(arguments);
			if (MesquiteInteger.isCombinable(cL)){
				fieldWidth = cL;
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Show Matrix In Tree Window";
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Displays character matrix with the current tree." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return MesquiteModule.NEXTRELEASE;  
	}



}

class ShowMatrixLinkedExtra extends TreeDisplayExtra implements TreeDisplayBkgdExtra {
	ShowMatrixInTreeWindow ownerModule;
	TreeDisplay treeDisplay;
	TreeDrawing treeDrawing;
	CharacterData data;
	int fieldWidth = 0;
	boolean naiveFieldWidth = true;
	int baseICSel = 0;
	int numIC = 0;
	double birdsEyeW = 2;
	double perBox = birdsEyeW;
	TreeDisplayRequests borders = new TreeDisplayRequests(0,0,0,0, fieldWidth, 0);
	DoubleArray boxEdges;
	Rectangle scroller, edgeGrabber, scrollPageDecrease, scrollPageIncrease;
	Image linkIcon, linkOffIcon;
	//	MesquiteTimer[] timers = new MesquiteTimer[8];

	public ShowMatrixLinkedExtra(ShowMatrixInTreeWindow ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		this.ownerModule = ownerModule;
		this.treeDisplay = treeDisplay;
		if (ownerModule.showMatrix)
			fieldWidth = ownerModule.fieldWidth;
		scroller = new Rectangle(0,0,0,0);
		scrollPageDecrease = new Rectangle(0,0,0,0);
		scrollPageIncrease = new Rectangle(0,0,0,0);
		edgeGrabber = new Rectangle(0,0,0,0);
		linkIcon = MesquiteImage.getImage(ownerModule.getPath() +  "linkedMatrix.gif");  
		linkOffIcon = MesquiteImage.getImage(ownerModule.getPath() +  "linkedMatrixOff.gif");  
		//	for (int i = 0; i<8; i++)
		//		timers[i] = new MesquiteTimer();
	}

	void turnOnOff(boolean on){
		boolean changed = false;
		if (on && fieldWidth == 0){
			if (ownerModule.fieldWidth>0)
				fieldWidth = ownerModule.fieldWidth;
			else
				fieldWidth = 186;
			changed = true;
		}
		else if (!on && fieldWidth > 0){
			fieldWidth = 0;
			changed = true;
		}
		if (changed){
			if (ownerModule.showMatrix)
				ownerModule.setTree(treeDisplay.getTree());
			borders.tipsFieldDistance = fieldWidth;
			treeDisplay.reviseBorders(false);
			forceRefresh();
		}
	}

	void forceRefresh(){
		treeDisplay.redoCalculations(78344);
		treeDisplay.forceRepaint();
	}
	public TreeDisplayRequests getRequestsOfTreeDisplay(Tree tree, TreeDrawing treeDrawing){
		naiveFieldWidth = false;
		return borders;
	}

	public void setTree(Tree tree) {
		if (naiveFieldWidth && data != null){
			if (data.getNumChars()<10)
				borders.tipsFieldDistance = 100;
		}
		if (boxEdges == null)
			boxEdges = new DoubleArray(tree.getTaxa().getNumTaxa());
		else if (tree.getTaxa().getNumTaxa()>boxEdges.getSize())
			boxEdges.resetSize(tree.getTaxa().getNumTaxa());
		boxEdges.deassignArray();
		baseICSel = 0;
	}

	//Sets boxEdges, which are the lower or right (trailing) edges of boxes between taxa. Start will 
	int prevTaxon = MesquiteInteger.unassigned;
	double prevTip = MesquiteDouble.unassigned;
	double minSpace = MesquiteDouble.unassigned;  // to help edges

	public void getTipEdges(Tree tree, int node) {
		if (tree.nodeIsTerminal(node)){
			if (treeDisplay.isRight() || treeDisplay.isLeft()) {
				if (MesquiteInteger.isCombinable(prevTaxon)){ //not the first
					double y = treeDrawing.y[node];
					boxEdges.setValue(prevTaxon, (y + prevTip)/2); //this sets the previous, leaving the last to guess!
					minSpace = MesquiteDouble.minimum(y-prevTip, minSpace);
				}
				prevTaxon = tree.taxonNumberOfNode(node);
				prevTip = treeDrawing.y[node];
			}
			else if (treeDisplay.isUp() || treeDisplay.isDown()) {
				if (MesquiteInteger.isCombinable(prevTaxon)){ //not the first
					double x = treeDrawing.x[node];
					boxEdges.setValue(prevTaxon, (x + prevTip)/2); //this sets the previous, leaving the last to guess!
					minSpace = MesquiteDouble.minimum(x-prevTip, minSpace);
				}
				prevTaxon = tree.taxonNumberOfNode(node);
				prevTip = treeDrawing.x[node];
			}
		}
		else {
			for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) 
				getTipEdges(tree, daughter);
		}
	}
	/* .. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */
	int bufferForEdgeGrabber = 6;
	int getBase(){
		if (treeDisplay.isRight())
			return borders.tipsFieldBase+ treeDisplay.effectiveFieldWidth()+treeDisplay.effectiveFieldLeftMargin()-treeDisplay.getTipsMargin()+bufferForEdgeGrabber; 
		else if (treeDisplay.isLeft())
			return treeDisplay.effectiveFieldLeftMargin()+treeDisplay.getTipsMargin() - borders.tipsFieldDistance  - treeDisplay.getMinTaxonNameDistanceFromTip()-bufferForEdgeGrabber; //+ borders.tipsFieldBase
		else if (treeDisplay.isUp())
			return treeDisplay.effectiveFieldTopMargin()+treeDisplay.getTipsMargin() - borders.tipsFieldDistance  - treeDisplay.getMinTaxonNameDistanceFromTip(); //+ borders.tipsFieldBase
		else if (treeDisplay.isDown())
			return borders.tipsFieldBase+ treeDisplay.effectiveFieldHeight()+treeDisplay.effectiveFieldTopMargin()-treeDisplay.getTipsMargin()+bufferForEdgeGrabber;
		return -1;
	}
	int fieldSize(){
		return borders.tipsFieldDistance -bufferForEdgeGrabber;
	}

	int getNumCharsTotal(){
		if (data.anySelected())
			return data.numberSelected();
		else
			return data.getNumChars();
	}
	/* .. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */
	//get the left/right or up/down location of the kth character
	int getLocationOfCharacter(int k){
		if (k<baseICSel || k>baseICSel+numIC)
			return -1;
		return (int)(getBase() + perBox*(k-baseICSel));
	}
	/* .. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */
	//get the left/right or up/down location of the kth character. This is not necessarily the kth character of the matrix!
	int getLocationOfCharacterInScroll(int k){
		if (k<0 || k>getNumCharsTotal())
			return -1;
		return (int)(getBase() + (k)*1.0*fieldSize()/getNumCharsTotal());
	}
	/* .. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */
	//get the character for the scroll location
	int getCharacterOfLocationInScroll(int z){
		return (int)((z-getBase())*getNumCharsTotal()*1.0/fieldSize());
	}
	int getCharacterOfLocationInField(int xLoc, int yLoc){
		if (treeDisplay.isRight() || treeDisplay.isLeft()) {
			double x = getBase();
			if (treeDisplay.isLeft())
				x += perBox;
			if (xLoc<x || xLoc > x + fieldSize())
				return -1;

			perBox = spacingPerCharacter();
			x += perBox; //to shift it over one, in case there are terminal boxes

			int count = 0;
			int baseIC = data.selectedIndexToPartIndex(baseICSel);
			for (int ic = baseIC; (count+1)*perBox<fieldSize() && (ic<data.getNumChars()); ic=data.nextPart(ic, ownerModule.selectedCharatersOnly)){
				x += perBox; 
				if (x>xLoc)
					return ic;
				count++;
			}
		}
		else if (treeDisplay.isUp() || treeDisplay.isDown()) {  // to count+2
			double y = getBase();
			if (yLoc<y || yLoc >y + fieldSize())
				return -1;

			perBox = spacingPerCharacter();
			if (treeDisplay.isDown())
				y += perBox;
			int count=0;
			int baseIC = data.selectedIndexToPartIndex(baseICSel);
			for (int ic = baseIC; (count+1)*perBox<fieldSize() && (ic<data.getNumChars()); ic=data.nextPart(ic, ownerModule.selectedCharatersOnly)){
				y += perBox; 
				if (y>yLoc)
					return ic;
				count++;
			}
		}
		return -1;
	}

	double spacingPerCharacter(){
		if (data == null)
			return 0;
		if (data instanceof MolecularData)
			return birdsEyeW;
		int w = treeDrawing.getEdgeWidth();
		if (w<16)
			w = 16;
		return w;
	}

	Color getColorOfState(CharacterData data, int ic, int it){

		if (data instanceof CategoricalData && !ownerModule.useTraceColors && !(data instanceof MolecularData)) {
			return ((CategoricalData)data).getColorOfStatesUpperLimit(ic,it, 9);
		} else
			return data.getColorOfStates(ic, it);
	}
	void drawStateRectangle(Graphics g, double x, double y, double taxonSpace, CharacterData data, int ic, int it){

		if (data instanceof MolecularData){
			g.setColor(data.getColorOfStates(ic, it));  
			if (treeDisplay.isRight() || treeDisplay.isLeft())
				g.fillRect((int)x,(int)y,(int)( spacingPerCharacter()),  (int)(taxonSpace));
			else if (treeDisplay.isUp() || treeDisplay.isDown()) 
				g.fillRect((int)x,(int)y,(int)(taxonSpace), (int)( spacingPerCharacter()));
		}
		else {
			if (data.isUnassigned(ic, it) || data.isInapplicable(ic, it))
				return;
			g.setColor(getColorOfState(data, ic, it));
			int rectTaxDim = MesquiteInteger.minimum((int)taxonSpace, 12);
			if (treeDisplay.isRight() || treeDisplay.isLeft()){
				g.fillRoundRect((int)x,(int)(y+taxonSpace/2),(int)( spacingPerCharacter()-4),  rectTaxDim, 6, 6);
				g.setColor(Color.black);
				g.drawRoundRect((int)x,(int)(y+taxonSpace/2),(int)( spacingPerCharacter()-4),  rectTaxDim, 6, 6);
			}
			else if (treeDisplay.isUp() || treeDisplay.isDown()) {
				g.fillRoundRect((int)(x+taxonSpace/2),(int)y,  rectTaxDim,(int)( spacingPerCharacter()-4), 6, 6);
				g.setColor(Color.black);
				g.drawRoundRect((int)(x+taxonSpace/2),(int)y,  rectTaxDim,(int)( spacingPerCharacter()-4), 6, 6);
			}
		}

	}
	double minTip = MesquiteDouble.unassigned;
	double maxTip = MesquiteDouble.unassigned;
	/* ========================================= */
	public void drawOnTreeRec(Tree tree, int node, Graphics g) {
		if (tree.nodeIsTerminal(node)){
			//int edgeWidth = treeDrawing.getEdgeWidth();
			//g.setColor(Color.black);
			//for left-right
			int taxonNumber = tree.taxonNumberOfNode(node);
			if (treeDisplay.isRight() || treeDisplay.isLeft()) {
				double topY = treeDrawing.y[node] - minSpace/2;
				if (MesquiteInteger.isCombinable(prevTaxon)) // not the first
					topY = boxEdges.getValue(prevTaxon);
				minTip=MesquiteDouble.minimum(minTip, topY);
				double bottomY = boxEdges.getValue(taxonNumber);
				if (!MesquiteDouble.isCombinable(bottomY)) // the last
					bottomY = treeDrawing.y[node] + minSpace/2;
				maxTip=MesquiteDouble.maximum(maxTip, bottomY);
				if (data != null){
					double x = getBase();

					perBox = spacingPerCharacter();
					x += perBox; //to shift it over one, in case there are terminal boxes
					numIC = 0;  //redundant, but avoids isolating an example
					int count = 0;
					int baseIC = data.selectedIndexToPartIndex(baseICSel);
					for (int ic = baseIC; (count+1)*perBox<fieldSize() && (ic<data.getNumChars()); ic=data.nextPart(ic, ownerModule.selectedCharatersOnly)){
						drawStateRectangle(g, x, topY,bottomY-topY, data, ic, taxonNumber);
						x += perBox; 
						count++;
						numIC++;
					}
					/*					
					for (int ic = 0; ic*perBox<fieldSize() && (ic+baseIC<data.getNumChars()); ic++){
						drawStateRectangle(g, x, topY,bottomY-topY, data, ic + baseIC, tree.taxonNumberOfNode(node));
						x += perBox; 
						lastIC = ic;
						numIC++;
					}
					 */
				}
			}
			else if (treeDisplay.isUp() || treeDisplay.isDown()) {
				double leftX = treeDrawing.x[node] - minSpace/2;  
				if (MesquiteInteger.isCombinable(prevTaxon)) // not the first
					leftX = boxEdges.getValue(prevTaxon);
				minTip=MesquiteDouble.minimum(minTip, leftX);
				double rightX = boxEdges.getValue(taxonNumber);
				if (!MesquiteDouble.isCombinable(rightX)) // the last
					rightX = treeDrawing.x[node] + minSpace/2;
				maxTip=MesquiteDouble.maximum(maxTip, rightX);

				if (data != null){
					double y = getBase();

					perBox = spacingPerCharacter();
					if (treeDisplay.isDown())
						y += perBox;
					numIC = 0;  //redundant, but avoids isolating an example
					int count=0;
					int baseIC = data.selectedIndexToPartIndex(baseICSel);
					for (int ic = baseIC; (count+1)*perBox<fieldSize() && (ic<data.getNumChars()); ic=data.nextPart(ic, ownerModule.selectedCharatersOnly)){
						//	timers[2].start();
						drawStateRectangle(g, leftX, y,rightX-leftX, data, ic,taxonNumber);
						//	timers[2].end();
						y += perBox; 
						count++;
						numIC++;
					}
					/*
					for (int ic = 0; ic*perBox<fieldSize() && (ic+baseIC<data.getNumChars()); ic++){
						drawStateRectangle(g, leftX, y,rightX-leftX, data, ic + baseIC, tree.taxonNumberOfNode(node));
						y += perBox; 
				//		lastIC = ic;
						numIC++;
					}
					 */
				}
			}
			prevTaxon = tree.taxonNumberOfNode(node);

		}
		else {
			for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) 
				drawOnTreeRec(tree, daughter, g);
		}
	}
	TextRotator textRotator = new TextRotator();
	Rectangle linkIconRect = new Rectangle(0, 0, 22, 14);


	void drawAndPrintOnTree(Tree tree, int drawnRoot, Graphics g){
		//	timers[0].start();
		data = ownerModule.data;
		treeDrawing = treeDisplay.getTreeDrawing(); //just making sure this is most current
		Color oldColor = g.getColor();
		//###################### find edges between adjacent tips
		boxEdges.deassignArray();
		minSpace = MesquiteDouble.unassigned;  // to help edges
		prevTaxon = MesquiteInteger.unassigned;
		prevTip = MesquiteDouble.unassigned;
		getTipEdges(tree, drawnRoot);

		//###################### recurse through tree to tips to draw! Also record last character and number of characters.
		prevTaxon = MesquiteInteger.unassigned;
		//	lastIC = 0;
		numIC = 0;
		minTip = MesquiteDouble.unassigned;
		maxTip = MesquiteDouble.unassigned;

		//	timers[0].end();
		//	timers[1].start();
		drawOnTreeRec(tree, drawnRoot, g);
		//	timers[1].end();

		if (data == null) {
			g.setColor(ColorDistribution.veryLightGray);
			if (treeDisplay.isRight() || treeDisplay.isLeft()) 
				g.fillRect(getBase(), (int)minTip, fieldSize(), (int)(maxTip-minTip));
			else if (treeDisplay.isUp()) 
				g.fillRect((int)minTip, getBase(), (int)(maxTip-minTip), fieldSize());
			else if (treeDisplay.isUp() || treeDisplay.isDown()) 
				g.fillRect((int)minTip, getBase(), (int)(maxTip-minTip), fieldSize());

		}
		g.setColor(oldColor);
	}
	int pixelWidthScroller =1;
	boolean iconShown = false;
	/* ========================================= */
	public void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		//	timers[3].start();

		//###################### icons for linked matrix
		if (treeDisplay.getOrientation() != TreeDisplay.LEFT && treeDisplay.getOrientation() != TreeDisplay.RIGHT && treeDisplay.getOrientation() != TreeDisplay.UP && treeDisplay.getOrientation() != TreeDisplay.DOWN)
			return;
		if (!ownerModule.showControlIcon && !ownerModule.showMatrix){
			linkIconRect.setLocation(-10000, -10000);
		}
		else
			if (treeDisplay.isRight())
				linkIconRect.setLocation(treeDisplay.effectiveFieldLeftMargin()-30, treeDisplay.effectiveFieldTopMargin() + treeDisplay.effectiveFieldHeight()-16);
			else if (treeDisplay.isLeft())
				linkIconRect.setLocation(treeDisplay.effectiveFieldLeftMargin()+ treeDisplay.effectiveFieldWidth(), treeDisplay.effectiveFieldTopMargin() + treeDisplay.effectiveFieldHeight()-16);
			else if (treeDisplay.isDown())
				linkIconRect.setLocation(treeDisplay.effectiveFieldLeftMargin()+ treeDisplay.effectiveFieldWidth()-20, treeDisplay.effectiveFieldTopMargin()-16);
			else if (treeDisplay.isUp())
				linkIconRect.setLocation(treeDisplay.effectiveFieldLeftMargin()+ treeDisplay.effectiveFieldWidth()-20, treeDisplay.effectiveFieldTopMargin() + treeDisplay.effectiveFieldHeight()-16);

		iconShown = false;
		CharacterData linkedMatrix = ((MesquiteTree)tree).findLinkedMatrix(ownerModule.getProject());
		if (linkedMatrix != null){
			if (!ownerModule.showMatrix && ownerModule.showControlIcon) {
				g.drawImage(linkIcon, linkIconRect.x, linkIconRect.y, treeDisplay);
				iconShown = true;
				return;
			}
			else {
				g.drawImage(linkOffIcon, linkIconRect.x, linkIconRect.y, treeDisplay);
				iconShown = true;
			}
		}		
		else if (!ownerModule.showMatrix){
			return;
		}
		else {
			g.drawImage(linkOffIcon, linkIconRect.x, linkIconRect.y, treeDisplay);
			iconShown = true;
		}
		//	timers[3].end();
		//###################### draw Matrix!!!
		drawAndPrintOnTree(tree, drawnRoot, g);

		if (!ownerModule.showMatrix)
			return;
		//	timers[4].start();

		//###################### draw scroll and other decorations
		Color oldColor = g.getColor();
		String matrixName = "";
		if (data != null)
			matrixName = data.getName();
		else
			matrixName = "No matrix is available";
		FontMetrics fontMet = g.getFontMetrics(g.getFont());
		int lengthName = fontMet.stringWidth(matrixName); //what to do if underlined?
		int edgeGrabberSize = 16;
		int scrollRound = 8;
		int scrollEdge =  (int)(prevTip+ minSpace/2+6);
		g.setColor(Color.gray);
		if (treeDisplay.isRight() || treeDisplay.isLeft()){
			if (data != null){
				g.drawRect(getBase(), scrollEdge-1, fieldSize(), 2); //line

				if (treeDisplay.isRight()){
					g.fillRect(getBase()-bufferForEdgeGrabber, scrollEdge-edgeGrabberSize, 2, edgeGrabberSize+2); // edge grabber
					g.fillRect(getBase()-bufferForEdgeGrabber+3, scrollEdge-edgeGrabberSize, 2, edgeGrabberSize+2); // edge grabber

					edgeGrabber.x =getBase()-bufferForEdgeGrabber-2; edgeGrabber.y = treeDisplay.effectiveFieldTopMargin(); edgeGrabber.width = 8; edgeGrabber.height = treeDisplay.effectiveFieldHeight();
				}
				else {
					g.fillRect(getBase()+fieldSize()+3, scrollEdge-edgeGrabberSize, 2, edgeGrabberSize+2); // edge grabber
					g.fillRect(getBase()+fieldSize()+6, scrollEdge-edgeGrabberSize, 2, edgeGrabberSize+2); // edge grabber

					edgeGrabber.x =getBase()+fieldSize(); edgeGrabber.y = treeDisplay.effectiveFieldTopMargin(); edgeGrabber.width = 8; edgeGrabber.height = treeDisplay.effectiveFieldHeight();
				}


				g.setColor(Color.gray);
				int x = getLocationOfCharacterInScroll(baseICSel); //ERROR! this baseIC is currently in data's number, not the selectedNumbering! needs to be in
				int xEnd = getLocationOfCharacterInScroll(baseICSel+numIC);
				if (xEnd - x<12) 
					xEnd = x+12;
				scroller.x = x-3; scroller.y = scrollEdge-6; scroller.width = xEnd-x+6; scroller.height = 12;
				g.fillRoundRect(x, scrollEdge-4, xEnd-x, 9, scrollRound, scrollRound); //scroller
				pixelWidthScroller = scroller.width;
				scrollPageDecrease.x = getBase(); scrollPageDecrease.y = scrollEdge-6; scrollPageDecrease.width = x-3-getBase(); scrollPageDecrease.height = 12;
				scrollPageIncrease.x = xEnd+3; scrollPageIncrease.y = scrollEdge-6; scrollPageIncrease.width = fieldSize()-(xEnd+3-getBase()); scrollPageIncrease.height = 12;

				g.setColor(Color.lightGray);
				g.fillRoundRect(x+2, scrollEdge-1, xEnd-x-4, 3, scrollRound, scrollRound); //scroller
			}
			g.setColor(Color.black);
			g.drawString(matrixName, getBase()+fieldSize()/2-lengthName/2, scrollEdge+16);


		}
		else if (treeDisplay.isUp() || treeDisplay.isDown()){
			if (data != null){
				g.fillRect(scrollEdge, getBase(), 2, fieldSize()); //line
				if (treeDisplay.isDown()){
					g.fillRect(scrollEdge-edgeGrabberSize, getBase()-bufferForEdgeGrabber, edgeGrabberSize+2, 2); // edge grabber
					g.fillRect(scrollEdge-edgeGrabberSize, getBase()-bufferForEdgeGrabber+3, edgeGrabberSize+2, 2); // edge grabber
					g.fillRect(scrollEdge-4, getBase(), 6, 2); //start
					//	g.fillRect(scrollEdge-4, getBase()+fieldSize()-1, 6, 2); //end
					edgeGrabber.y =getBase()-bufferForEdgeGrabber; edgeGrabber.x = treeDisplay.effectiveFieldLeftMargin(); edgeGrabber.height = 8; edgeGrabber.width = treeDisplay.effectiveFieldWidth();
				}
				else {  //UP
					g.fillRect(scrollEdge-edgeGrabberSize, getBase()+fieldSize()+3, edgeGrabberSize+2, 2); // edge grabber
					g.fillRect(scrollEdge-edgeGrabberSize, getBase()+fieldSize()+6, edgeGrabberSize+2, 2); // edge grabber
					g.fillRect(scrollEdge-4, getBase()+fieldSize()-1, 6, 2); //start
					//	g.fillRect(scrollEdge-4, getBase(), 6, 2); //end
					edgeGrabber.y =getBase()+fieldSize(); edgeGrabber.x = treeDisplay.effectiveFieldLeftMargin(); edgeGrabber.height = 8; edgeGrabber.width = treeDisplay.effectiveFieldWidth();
				}

				g.setColor(Color.gray);
				int y = getLocationOfCharacterInScroll(baseICSel);
				int yEnd = getLocationOfCharacterInScroll(baseICSel+numIC);
				if (yEnd - y<12) 
					yEnd = y+12;
				scroller.y = y-3; scroller.x = scrollEdge-6; scroller.height = yEnd-y+6; scroller.width = 12;
				g.fillRoundRect(scrollEdge-4, y, 9, yEnd-y, scrollRound, scrollRound); //scroller
				pixelWidthScroller = scroller.height;
				scrollPageDecrease.y = getBase(); scrollPageDecrease.x = scrollEdge-6; scrollPageDecrease.height = y-3-getBase(); scrollPageDecrease.width = 12;
				scrollPageIncrease.y = yEnd+3; scrollPageIncrease.x = scrollEdge-6; scrollPageIncrease.height = fieldSize()-(yEnd+3-getBase()); scrollPageIncrease.width = 12;

				g.setColor(Color.lightGray);
				g.fillRoundRect(scrollEdge-1, y+2, 3,  yEnd-y-4, scrollRound, scrollRound); //scroller
			}
			g.setColor(Color.black);
			textRotator.drawRotatedText(matrixName, g, treeDisplay, scrollEdge+14, getBase()+fieldSize()/2+lengthName/2);

		}
		g.setColor(oldColor);
		//	timers[4].end();
		//	System.err.println("  before " + timers[0].getAccumulatedTime() + " draw "  + timers[1].getAccumulatedTime() + " rect " + timers[2].getAccumulatedTime()  + " overallBefore " + timers[3].getAccumulatedTime() + " overallAfter " + timers[4].getAccumulatedTime());
	}
	/*.................................................................................................................*/
	/* ========================================= */
	int taxonNum = 0;
	public void drawUnderTreeRec(Tree tree, int node, Graphics g) {
		if (tree.nodeIsTerminal(node)){
			//for left-right
			taxonNum++;
			if (taxonNum %2 ==0) {
				if (treeDisplay.isRight() || treeDisplay.isLeft()) {
					double topY = treeDrawing.y[node] - minSpace/2;
					if (MesquiteInteger.isCombinable(prevTaxon)) // not the first
						topY = boxEdges.getValue(prevTaxon);
					double bottomY = boxEdges.getValue(tree.taxonNumberOfNode(node));
					if (!MesquiteDouble.isCombinable(bottomY)) // the last
						bottomY = treeDrawing.y[node] + minSpace/2;
					g.fillRect(0, (int)topY, 10000, (int)(bottomY-topY));
				}
				else if (treeDisplay.isUp() || treeDisplay.isDown()) {
					double leftX = treeDrawing.x[node] - minSpace/2;  
					if (MesquiteInteger.isCombinable(prevTaxon)) // not the first
						leftX = boxEdges.getValue(prevTaxon);
					double rightX = boxEdges.getValue(tree.taxonNumberOfNode(node));
					if (!MesquiteDouble.isCombinable(rightX)) // the last
						rightX = treeDrawing.x[node] + minSpace/2;
					g.fillRect((int)leftX, 0, (int)(rightX-leftX), 10000);

				}
			}
			prevTaxon = tree.taxonNumberOfNode(node);

		}
		else {
			for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) 
				drawUnderTreeRec(tree, daughter, g);
		}
	}	
	public   void drawUnderTree(Tree tree, int drawnRoot, Graphics g) {

		if (!ownerModule.showMatrix)
			return;
		treeDrawing = treeDisplay.getTreeDrawing();
		if (treeDrawing == null)
			return;
		//###################### find edges between adjacent tips, just in case this comes before drawOnTree
		boxEdges.deassignArray();
		minSpace = MesquiteDouble.unassigned;  // to help edges
		prevTaxon = MesquiteInteger.unassigned;
		prevTip = MesquiteDouble.unassigned;
		getTipEdges(tree, drawnRoot);
		//######################


		taxonNum = 0;//draw grey bars for taxa
		Color c = g.getColor();
		g.setColor(ColorDistribution.veryVeryLightGray);
		//######################
		drawUnderTreeRec(tree, drawnRoot, g);
		//######################
		g.setColor(c);
	}
	/* ========================================= */

	public void printUnderTree(Tree tree, int drawnRoot, Graphics g){}

	public void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		if (!ownerModule.showMatrix)
			return;
		//######################
		drawAndPrintOnTree(tree, drawnRoot, g);
		//######################
		String matrixName = "";
		if (data != null)
			matrixName = data.getName();
		else
			matrixName = "No matrix is available";
		FontMetrics fontMet = g.getFontMetrics(g.getFont());
		int lengthName = fontMet.stringWidth(matrixName); //what to do if underlined?
		int scrollEdge =  (int)(prevTip+ minSpace/2+6);
		Color oldColor = g.getColor();
		g.setColor(Color.black);
		if (treeDisplay.isRight() || treeDisplay.isLeft())
			g.drawString(matrixName, getBase()+fieldSize()/2-lengthName/2, scrollEdge+16);

		else if (treeDisplay.isUp() || treeDisplay.isDown())
			textRotator.drawRotatedText(matrixName, g, treeDisplay, scrollEdge+14, getBase()+fieldSize()/2+lengthName/2);
		g.setColor(oldColor);	
	}
	/* ========================================= */

	void resetFieldSize(int increase){
		if (((treeDisplay.isRight() || treeDisplay.isLeft()) && treeDisplay.effectiveFieldWidth()-treeDisplay.getTipsMargin()-increase<100)
				|| ((treeDisplay.isUp() || treeDisplay.isDown()) && treeDisplay.effectiveFieldHeight()-treeDisplay.getTipsMargin()<100))
			return;
		if (borders.tipsFieldDistance + increase<50)
			return;
		borders.tipsFieldDistance += increase;
		ownerModule.fieldWidth = borders.tipsFieldDistance;
		treeDisplay.reviseBorders(false);
		treeDisplay.redoCalculations(78244);
		treeDisplay.forceRepaint();

	}
	void resetBaseCharacter(int increase){
		int zBaseIC = getLocationOfCharacterInScroll(baseICSel);
		int newBaseIC = getCharacterOfLocationInScroll(increase + zBaseIC);
		if (newBaseIC<0)
			newBaseIC =0;
		if (newBaseIC+numIC>=getNumCharsTotal())
			newBaseIC =getNumCharsTotal()-numIC;
		baseICSel = newBaseIC;
		treeDisplay.forceRepaint();

	}


	/* ========================================= */
	/**Add any desired menu items to the right click popup*/
	public void addToRightClickPopup(MesquitePopup popup, MesquiteTree tree, int branch){
		try {
			if (ownerModule.showMatrix){
				CharacterData linkedMatrix = ((MesquiteTree)treeDisplay.getTree()).findLinkedMatrix(ownerModule.getProject());
				if (ownerModule.choose0Link1 == 1 && linkedMatrix != null)
					popup.addItem("Display of Matrix Linked to Tree...", ownerModule, new MesquiteCommand("queryLinkedOptions", ownerModule), "");
				else if (ownerModule.getProject().getNumberCharMatrices(treeDisplay.getTree().getTaxa()) >0)
					popup.addItem("Display of Matrix with Tree...", ownerModule, new MesquiteCommand("queryOptions", ownerModule), "");
			}
			else
				popup.addItem("Show Matrix with Tree...", ownerModule, new MesquiteCommand("queryOptions", ownerModule), "");
		}
		catch (Exception e){
		}

	}
	public void cursorMove(Tree tree, int x, int y, Graphics g){
		if (edgeGrabber.contains(x, y)) {
			if (treeDisplay.isRight() || treeDisplay.isLeft())
				treeDisplay.setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));  //this is undone in ScanFlash of BasicTreeWindow
			else if (treeDisplay.isUp() || treeDisplay.isDown())
				treeDisplay.setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
		}
		int ic = getCharacterOfLocationInField(x, y);
		if (ic>=0){
			MesquiteWindow w = ownerModule.containerOfModule();
			String s = "Character " + (ic+1);
			if (data != null && data.characterHasName(ic))
				s += " (" + data.getCharacterName(ic) + ")";

			w.setExplanation(s);
		}
	}
	int edgeGrabTouch = -1;
	int scrollerTouch = -1;
	/**to inform TreeDisplayExtra that cursor has just touched the field (not in a branch or taxon)*/
	public boolean cursorTouchField(Tree tree, Graphics g, int x, int y, int modifiers, int clickID){
		if (scroller.contains(x, y)) {
			if (treeDisplay.isRight() || treeDisplay.isLeft())
				scrollerTouch = x;
			else if (treeDisplay.isUp() || treeDisplay.isDown())
				scrollerTouch = y;
			return true;
		}
		else if (scrollPageIncrease.contains(x, y)) {
			resetBaseCharacter(pixelWidthScroller);
			return true;
		}
		else if (scrollPageDecrease.contains(x, y)) {
			resetBaseCharacter(-pixelWidthScroller);
			return true;
		}
		else if (edgeGrabber.contains(x, y)) {
			if (treeDisplay.isRight() || treeDisplay.isLeft())
				edgeGrabTouch = x;
			else if (treeDisplay.isUp() || treeDisplay.isDown())
				edgeGrabTouch = y;
			return true;
		}
		else if (linkIconRect.contains(x, y)){
			if (MesquiteEvent.optionKeyDown(modifiers)){
				ownerModule.queryOptions(true, true, true);
				return true;
			}
			else if (!ownerModule.showMatrix){ //not on yet
				if (iconShown){
					MesquitePopup popup = new MesquitePopup(treeDisplay);
					ownerModule.choose0Link1 = 1;
					ownerModule.resetMatrix(treeDisplay.getTree());
					CharacterData data = ownerModule.data;
					if (data != null) {
						popup.addItem("Tree has linked matrix: " + data.getName(),  MesquiteCommand.nullCommand, null);
						popup.addItem("-",  MesquiteCommand.nullCommand, null);
					}
					popup.addItem("Show Matrix Linked to Tree",  new MesquiteCommand("showLinked", ownerModule), null);
					popup.addItem("Display Options...", new MesquiteCommand("queryOptionsFromIcon", ownerModule), null);
					popup.showPopup(x, y);


					/*					ownerModule.showMatrix = true;
					ownerModule.choose0Link1 = 1;
					ownerModule.resetMatrix(treeDisplay.getTree());
					turnOnOff(true); */
				}
			}
			else { //on; turn off
				MesquitePopup popup = new MesquitePopup(treeDisplay);
				CharacterData data = ownerModule.data;
				if (data != null) {
					popup.addItem("Tree has linked matrix: " + data.getName(),  MesquiteCommand.nullCommand, null);
					popup.addItem("-",  MesquiteCommand.nullCommand, null);
				}
				popup.addItem("Hide Matrix",  new MesquiteCommand("hideMatrix", ownerModule), null);
				popup.addItem("Display Options...", new MesquiteCommand("queryOptionsFromIcon", ownerModule), null);
				popup.showPopup(x, y);

			}

		}
		return false;
	}
	/**to inform TreeDisplayExtra that cursor has just dragged in the field (not in a branch or taxon)*/
	public void cursorDragField(Tree tree, Graphics g, int x, int y, int modifiers, int clickID){
	}
	/**to inform TreeDisplayExtra that cursor has just dropped the field (not in a branch or taxon)*/
	public void cursorDropField(Tree tree, Graphics g, int x, int y, int modifiers, int clickID){
		if (scrollerTouch>=0){
			if (treeDisplay.isLeft() || treeDisplay.isRight())
				resetBaseCharacter(x-scrollerTouch);
			else if (treeDisplay.isDown() || treeDisplay.isUp())
				resetBaseCharacter(y-scrollerTouch);
		}
		else if (edgeGrabTouch>=0){
			if (treeDisplay.isRight())
				resetFieldSize(edgeGrabTouch-x);
			else if (treeDisplay.isLeft())
				resetFieldSize(x-edgeGrabTouch);
			if (treeDisplay.isDown())
				resetFieldSize(edgeGrabTouch-y);
			else if (treeDisplay.isUp())
				resetFieldSize(y-edgeGrabTouch);
		}
		edgeGrabTouch = -1;
		scrollerTouch = -1;
	}

}

