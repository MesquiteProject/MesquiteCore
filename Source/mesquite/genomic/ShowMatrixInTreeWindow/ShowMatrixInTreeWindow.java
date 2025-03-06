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

import mesquite.categ.lib.MolecularData;
import mesquite.charMatrices.BasicDataWindowCoord.BasicDataWindowCoord;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDisplayActive;
import mesquite.lib.tree.TreeDisplayExtra;
import mesquite.lib.tree.TreeDisplayHolder;
import mesquite.lib.tree.TreeDisplayRequests;
import mesquite.lib.tree.TreeDrawing;
import mesquite.lib.tree.TreeTool;
import mesquite.lib.ui.ColorTheme;
import mesquite.lib.ui.Legend;
import mesquite.lib.ui.MQPanel;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lib.ui.MessagePanel;

/* ======================================================================== */
public class ShowMatrixInTreeWindow extends TreeWindowAssistantN  {
//	Vector extras = new Vector();
	/*.................................................................................................................*/
	CharacterData data = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	//This method is optional for TreeWindowAssistants, unlike TreeDisplayAssistants
	public TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay){
		ShowMatrixLinkedExtra extra = new ShowMatrixLinkedExtra(this, treeDisplay);
	//	extras.addElement(extra);
		return extra;
	}

	public void employeeQuit(MesquiteModule m){
		//	if (m==treeDrawCoordTask)
		iQuit();
	}

	public boolean isSubstantive(){
		return false;
	}

	String treeName = null;
	boolean warned = false;
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		treeName = tree.getName();
		CharacterData d = null;
		MesquiteProject project = getProject();
		if (tree instanceof Attachable){
			Object obj = ((Attachable)tree).getAttachment("fromMatrix", MesquiteString.class);
			if (obj != null)
				d = project.getCharacterMatrixByReference(null, tree.getTaxa(), null, ((MesquiteString)obj).getValue());
		}
		if (d == null)
			d = project.getCharacterMatrixByReference(null, tree.getTaxa(), null, treeName);
		if (d == null)
			d = project.getCharacterMatrixByReference(null, tree.getTaxa(), null, StringUtil.getAllButLastItem(treeName, "."));
		if (d == null)
			d = project.getCharacterMatrixByReference(null, tree.getTaxa(), null, StringUtil.getAllButLastItem(treeName, "#"));
		if (d == null)
			d = project.getCharacterMatrix(tree.getTaxa(), 0);
		if (d != null){
			data = d;
			// add listeners to update if needed


			/*
			if (matrixEditorTask ==null){
				BasicDataWindowCoord coordTask = (BasicDataWindowCoord)findNearestColleagueWithDuty(BasicDataWindowCoord.class);
				matrixEditorTask = (DataWindowMaker)coordTask.doCommand("showDataWindow", StringUtil.tokenize(project.getCharMatrixReferenceExternal(d)), CommandChecker.defaultChecker);
				MesquiteWindow matrixWindow = matrixEditorTask.getModuleWindow();
				matrixWindow.setPopAsTile(true);
				matrixWindow.popOut(true);
			}
			else {
				matrixEditorTask = (DataWindowMaker)matrixEditorTask.doCommand("showMatrix", StringUtil.tokenize(project.getCharMatrixReferenceExternal(d)), CommandChecker.defaultChecker);
			}
			 */
		}
		else {
			data = null;
			/*
			if (!warned){
			discreetAlert("No matrices were found linked to the current tree");
			warned = true;
		} */
		}
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {

		Snapshot sn = new Snapshot();

		return sn;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Queries options", null, commandName, "options")) {
			//queryOptions();
			parametersChanged();
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


}

class ShowMatrixLinkedExtra extends TreeDisplayExtra {
	ShowMatrixInTreeWindow ownerModule;
	TreeDisplay treeDisplay;
	TreeDrawing treeDrawing;
	CharacterData data;
	int fieldWidth = 200;
	boolean naiveFieldWidth = true;
	int baseIC = 0;
	int lastIC = 0;
	int numIC = 0;
	double birdsEyeW = 2;
	double perBox = birdsEyeW;
	TreeDisplayRequests borders = new TreeDisplayRequests(0,0,0,0, fieldWidth, 0);
	DoubleArray boxEdges;
	Rectangle scroller, edgeGrabber;

	public ShowMatrixLinkedExtra(ShowMatrixInTreeWindow ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		this.ownerModule = ownerModule;
		this.treeDisplay = treeDisplay;
		scroller = new Rectangle(0,0,0,0);
		edgeGrabber = new Rectangle(0,0,0,0);

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
		baseIC = 0;
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
			return treeDisplay.effectiveFieldTopMargin()+treeDisplay.getTipsMargin() - borders.tipsFieldDistance  - treeDisplay.getMinTaxonNameDistanceFromTip()-bufferForEdgeGrabber; //+ borders.tipsFieldBase
		else if (treeDisplay.isDown())
			return borders.tipsFieldBase+ treeDisplay.effectiveFieldHeight()+treeDisplay.effectiveFieldTopMargin()-treeDisplay.getTipsMargin()+bufferForEdgeGrabber;
		return -1;
	}
	int fieldSize(){
		return borders.tipsFieldDistance -bufferForEdgeGrabber;
	}

	int getNumCharsTotal(){
		return data.getNumChars();
	}
	/* .. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */
	//get the left/right or up/down location of the kth character
	int getLocationOfCharacter(int k){
		if (k<baseIC || k>baseIC+numIC)
			return -1;
		return (int)(getBase() + perBox*(k-baseIC));
	}
	/* .. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */
	//get the left/right or up/down location of the kth character
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
			g.setColor(data.getColorOfStates(ic, it));
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

	/* ========================================= */
	public void drawOnTreeRec(Tree tree, int node, Graphics g) {
		if (tree.nodeIsTerminal(node)){
			//int edgeWidth = treeDrawing.getEdgeWidth();
			//g.setColor(Color.black);
			//for left-right
			if (treeDisplay.isRight() || treeDisplay.isLeft()) {
				double topY = treeDrawing.y[node] - minSpace/2;
				if (MesquiteInteger.isCombinable(prevTaxon)) // not the first
					topY = boxEdges.getValue(prevTaxon);
				double bottomY = boxEdges.getValue(tree.taxonNumberOfNode(node));
				if (!MesquiteDouble.isCombinable(bottomY)) // the last
					bottomY = treeDrawing.y[node] + minSpace/2;
				double x = getBase();

				perBox = spacingPerCharacter();
				numIC = 0;  //redundant, but avoids isolating an example
				for (int ic = 0; ic*perBox<fieldSize() && (ic+baseIC<data.getNumChars()); ic++){
					//See ColorByState for how matrix editor chooseColors
					//			g.setColor(data.getColorOfStates(ic + baseIC, tree.taxonNumberOfNode(node)));
					drawStateRectangle(g, x, topY,bottomY-topY, data, ic + baseIC, tree.taxonNumberOfNode(node));
					x += perBox; 
					lastIC = ic;
					numIC++;
				}
			}
			else if (treeDisplay.isUp() || treeDisplay.isDown()) {
				double leftX = treeDrawing.x[node] - minSpace/2;  
				if (MesquiteInteger.isCombinable(prevTaxon)) // not the first
					leftX = boxEdges.getValue(prevTaxon);
				double rightX = boxEdges.getValue(tree.taxonNumberOfNode(node));
				if (!MesquiteDouble.isCombinable(rightX)) // the last
					rightX = treeDrawing.x[node] + minSpace/2;
				double y = getBase();

				perBox = spacingPerCharacter();
				numIC = 0;  //redundant, but avoids isolating an example
				for (int ic = 0; ic*perBox<fieldSize() && (ic+baseIC<data.getNumChars()); ic++){
					//See ColorByState for how matrix editor chooseColors
					//	g.setColor(data.getColorOfStates(ic + baseIC, tree.taxonNumberOfNode(node)));
					//	g.fillRect((int)leftX,(int)y,(int)(rightX-leftX), (int)( perBox));
					drawStateRectangle(g, leftX, y,rightX-leftX, data, ic + baseIC, tree.taxonNumberOfNode(node));
					y += perBox; 
					lastIC = ic;
					numIC++;
				}
			}
			prevTaxon = tree.taxonNumberOfNode(node);

		}
		else {
			for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) 
				drawOnTreeRec(tree, daughter, g);
		}
	}

	/* ========================================= */
	public void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		if (treeDisplay.getOrientation() != TreeDisplay.LEFT && treeDisplay.getOrientation() != TreeDisplay.RIGHT && treeDisplay.getOrientation() != TreeDisplay.UP && treeDisplay.getOrientation() != TreeDisplay.DOWN)
			return;
		if (ownerModule.data == null)
			return;
		data = ownerModule.data;
		treeDrawing = treeDisplay.getTreeDrawing(); //just making sure this is most current
		if (data == null)  //!!! draw bank field with message that no data matches
			return;

		//###################### find edges between adjacent tips
		boxEdges.deassignArray();
		minSpace = MesquiteDouble.unassigned;  // to help edges
		prevTaxon = MesquiteInteger.unassigned;
		prevTip = MesquiteDouble.unassigned;
		getTipEdges(tree, drawnRoot);

		//###################### recurse through tree to tips to draw! Also record last character and number of characters.
		prevTaxon = MesquiteInteger.unassigned;
		lastIC = 0;
		numIC = 0;
		drawOnTreeRec(tree, drawnRoot, g);

		//###################### draw scroll and other decorations
		int edgeGrabberSize = 16;
		int scrollRound = 8;
		int scrollEdge =  (int)(prevTip+ minSpace/2+6);
		g.setColor(Color.gray);
		if (treeDisplay.isRight() || treeDisplay.isLeft()){
			g.drawRect(getBase(), scrollEdge-1, fieldSize(), 2); //line
			//g.fillRect(getBase(), scrollEdge, fieldSize(), 2); //line
			if (treeDisplay.isRight()){
				g.fillRect(getBase()-bufferForEdgeGrabber, scrollEdge-edgeGrabberSize, 2, edgeGrabberSize+2); // edge grabber
				g.fillRect(getBase()-bufferForEdgeGrabber+3, scrollEdge-edgeGrabberSize, 2, edgeGrabberSize+2); // edge grabber
				//		g.fillRect(getBase()+fieldSize()-1, scrollEdge-4, 2, 10); //end
				edgeGrabber.x =getBase()-bufferForEdgeGrabber-2; edgeGrabber.y = scrollEdge-edgeGrabberSize; edgeGrabber.width = 8; edgeGrabber.height = edgeGrabberSize+4;
			}
			else {
				g.fillRect(getBase()+fieldSize()+3, scrollEdge-edgeGrabberSize, 2, edgeGrabberSize+2); // edge grabber
				g.fillRect(getBase()+fieldSize()+6, scrollEdge-edgeGrabberSize, 2, edgeGrabberSize+2); // edge grabber
				//		g.fillRect(getBase(), scrollEdge-4, 2, 10); //end
				edgeGrabber.x =getBase()+fieldSize(); edgeGrabber.y = scrollEdge-edgeGrabberSize; edgeGrabber.width = 8; edgeGrabber.height = edgeGrabberSize+4;
			}


			g.setColor(Color.gray);
			int x = getLocationOfCharacterInScroll(baseIC);
			int xEnd = getLocationOfCharacterInScroll(baseIC+numIC);
			if (xEnd - x<12) 
				xEnd = x+12;
			scroller.x = x-3; scroller.y = scrollEdge-6; scroller.width = xEnd-x+6; scroller.height = 12;
			g.fillRoundRect(x, scrollEdge-4, xEnd-x, 9, scrollRound, scrollRound); //scroller

			g.setColor(Color.lightGray);
			g.fillRoundRect(x+2, scrollEdge-1, xEnd-x-4, 3, scrollRound, scrollRound); //scroller

		}
		else if (treeDisplay.isUp() || treeDisplay.isDown()){
			g.fillRect(scrollEdge, getBase(), 2, fieldSize()); //line
			if (treeDisplay.isDown()){
				g.fillRect(scrollEdge-edgeGrabberSize, getBase()-bufferForEdgeGrabber, edgeGrabberSize+2, 2); // edge grabber
				g.fillRect(scrollEdge-edgeGrabberSize, getBase()-bufferForEdgeGrabber+3, edgeGrabberSize+2, 2); // edge grabber
				g.fillRect(scrollEdge-4, getBase(), 6, 2); //start
				//	g.fillRect(scrollEdge-4, getBase()+fieldSize()-1, 6, 2); //end
				edgeGrabber.y =getBase()-bufferForEdgeGrabber; edgeGrabber.x = scrollEdge-edgeGrabberSize; edgeGrabber.height = 8; edgeGrabber.width = edgeGrabberSize+8;
			}
			else {  //UP
				g.fillRect(scrollEdge-edgeGrabberSize, getBase()+fieldSize()+3, edgeGrabberSize+2, 2); // edge grabber
				g.fillRect(scrollEdge-edgeGrabberSize, getBase()+fieldSize()+6, edgeGrabberSize+2, 2); // edge grabber
				g.fillRect(scrollEdge-4, getBase()+fieldSize()-1, 6, 2); //start
				//	g.fillRect(scrollEdge-4, getBase(), 6, 2); //end
				edgeGrabber.y =getBase()+fieldSize(); edgeGrabber.x = scrollEdge-edgeGrabberSize; edgeGrabber.height = 8; edgeGrabber.width = edgeGrabberSize+8;
			}

			g.setColor(Color.gray);
			int y = getLocationOfCharacterInScroll(baseIC);
			int yEnd = getLocationOfCharacterInScroll(baseIC+numIC);
			if (yEnd - y<12) 
				yEnd = y+12;
			scroller.y = y-3; scroller.x = scrollEdge-6; scroller.height = yEnd-y+6; scroller.width = 12;
			g.fillRoundRect(scrollEdge-4, y, 9, yEnd-y, scrollRound, scrollRound); //scroller

			g.setColor(Color.lightGray);
			g.fillRoundRect(scrollEdge-1, y+2, 3,  yEnd-y-4, scrollRound, scrollRound); //scroller


		}

	}
	/* ========================================= */


	public void printOnTree(Tree tree, int drawnRoot, Graphics g) {
	}
	/* ========================================= */

	void resetFieldSize(int increase){
		if (((treeDisplay.isRight() || treeDisplay.isLeft()) && treeDisplay.effectiveFieldWidth()-treeDisplay.getTipsMargin()-increase<100)
				|| ((treeDisplay.isUp() || treeDisplay.isDown()) && treeDisplay.effectiveFieldHeight()-treeDisplay.getTipsMargin()<100))
			return;
		if (borders.tipsFieldDistance + increase<50)
			return;
		borders.tipsFieldDistance += increase;
		treeDisplay.reviseBorders(false);
		treeDisplay.redoCalculations(78244);
		treeDisplay.forceRepaint();

	}
	void resetBaseCharacter(int increase){
		int zBaseIC = getLocationOfCharacterInScroll(baseIC);
		int newBaseIC = getCharacterOfLocationInScroll(increase + zBaseIC);
		if (newBaseIC<0)
			newBaseIC =0;
		if (newBaseIC+numIC>=getNumCharsTotal())
			newBaseIC =getNumCharsTotal()-numIC;
		baseIC = newBaseIC;
		treeDisplay.forceRepaint();

	}
	/* ========================================= */
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
		if (edgeGrabber.contains(x, y)) {
			if (treeDisplay.isRight() || treeDisplay.isLeft())
				edgeGrabTouch = x;
			else if (treeDisplay.isUp() || treeDisplay.isDown())
				edgeGrabTouch = y;
			return true;
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

