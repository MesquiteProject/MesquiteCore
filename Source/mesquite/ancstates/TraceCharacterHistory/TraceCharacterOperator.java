/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.ancstates.TraceCharacterHistory;

import java.awt.*;
import java.util.Vector;

import mesquite.categ.lib.CategoricalState;
import mesquite.cont.lib.ContinuousState;
import mesquite.lib.*;
import mesquite.lib.characters.*;

/* ======================================================================== */
public class TraceCharacterOperator extends TreeDisplayDrawnExtra implements CharHistoryContainer {
	Tree myTree;
	TraceCharacterHistory traceModule;
	public TraceLegend traceLegend;
	public CharacterHistory history;
	//private CharacterDistribution observedStates;
	public TreeDecorator decorator;
	private boolean holding = false;
	boolean firstTime = true;
	MesquiteColorTable colorTable = MesquiteColorTable.DEFAULTCOLORTABLE.cloneColorTable(); 
	public TraceCharacterOperator (TraceCharacterHistory ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		traceModule = ownerModule;
	}
	public CharacterHistory getCharacterHistory(){
		return history;
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree){
		myTree = tree;
		if (tree!=null && (traceModule.historyTask!=null) && (traceModule.displayTask!=null) && !traceModule.suspend) {
			if (traceLegend != null)
				traceLegend.setSpecsMessage("Calculating...");
			doCalculations(true);
		}
	}
	/*.................................................................................................................*/
	public void toggleReconstruct(){
		if (traceLegend!=null)
			traceLegend.reviseBounds();
	}
	/*.................................................................................................................*/
	public   Tree getTree(){
		return myTree;
	}
	/*.................................................................................................................*/
	public   Taxa getTaxa(){
		if (myTree !=null)
			return myTree.getTaxa();
		else
			return null;
	}
	CharacterState state=null;

	/*
	void fillWithObserved(Tree tree, int node, CharacterHistory history, CharacterDistribution observed){
		if (tree.nodeIsTerminal(node)){
			state = observed.getCharacterState(state, tree.taxonNumberOfNode(node));
			history.setCharacterState(node, state);
		}
		else {
			state = observed.getCharacterState(state, 1); //merely to get an object to fill
			state.setToUnassigned();
			history.setCharacterState(node, state);
		}
		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter))
				fillWithObserved(tree, daughter, history, observed);
	}
	 */
	MesquiteString resultString = new MesquiteString();
	/*.................................................................................................................*/
	public void doCalculations(boolean doPreps) {
		holding = true;
		if (myTree == null) {
		}
		else {
			traceModule.displayTask.onHold();
			if (doPreps) {
				traceModule.prepareHistory(myTree);
			}
			if (traceLegend==null  && traceModule.showLegend.getValue()) {
				traceLegend = new TraceLegend(traceModule, this);
				traceLegend.adjustScroll(traceModule.getNumberCurrentHistory(), traceModule.getNumberOfHistories(myTree)); 
				traceLegend.adjustMappingScroll(traceModule.getNumberCurrentMapping(), traceModule.getNumberOfMappings(myTree)); 
				addPanelPlease(traceLegend);
			}
			if (traceLegend!=null && traceModule.showLegend.getValue()) {
				traceLegend.onHold();
				traceLegend.adjustScroll(traceModule.getNumberCurrentHistory(), traceModule.getNumberOfHistories(myTree));
				traceLegend.adjustMappingScroll(traceModule.getNumberCurrentMapping(), traceModule.getNumberOfMappings(myTree)); 
			}
			resultString.setValue("");

			history = traceModule.getMapping(myTree, resultString);
			if (history == null){
				holding = false;
				return;
			}

			traceModule.binsMenuItem.setEnabled(history instanceof mesquite.cont.lib.ContinuousHistory);
			traceModule.numBinsMenuItem.setEnabled(history instanceof mesquite.cont.lib.ContinuousHistory);
			traceModule.colorSubmenu.setEnabled(history instanceof mesquite.cont.lib.ContinuousHistory);
			MesquiteColorTable ct = colorTable;
			colorTable = history.getColorTable(colorTable);
			colorTable.setPreferredBinBoundaries(traceModule.binBoundaries);
			if (history!=null) 
				history.prepareColors(myTree, myTree.getRoot());
			if (history instanceof mesquite.cont.lib.ContinuousHistory){
				mesquite.cont.lib.ContinuousHistory cHistory = (mesquite.cont.lib.ContinuousHistory)history;
				int numBoundaries = 10;
				if (traceModule.binBoundaries != null)
					numBoundaries = traceModule.binBoundaries.length;
				double[] usedBoundaries = new double[numBoundaries];
				for (int i = 0; i<numBoundaries; i++)  //starting at 1 because getBinBoundaries reports starting and ending
					usedBoundaries[i] = cHistory.getBinBoundary( i, colorTable);
				traceModule.usedBoundaries = usedBoundaries;
			}
			if (ct != colorTable && traceLegend != null)
				for (int i = 0; i<64; i++)
					for (int k= 0; k<64; k++)
						traceLegend.modifiedColors[i][k] = false;

			if (traceLegend != null && traceModule != null && colorTable != null && colorTable.setColorEnabled() && !modColorsIncorporated && traceModule.whichColorsModified != null){
				if (traceModule.whichColorsModified != null && traceModule.newColors != null)
					for (int box = 0; box<64; box++)
						traceLegend.resetColor(traceModule.whichColorsModified[box], traceModule.newColors[box]);
				modColorsIncorporated = true;
				traceModule.startingColors = traceLegend.getModColorsCommand();
			}
			String s = "";
			if (!traceModule.showStateWeights.getValue())
				resultString.append(" (NOTE: the graphic display of reconstruction is NOT showing the reconstruction proportional to the weights (e.g. likelihoods) of the states)");
			traceModule.displayTask.offHold();
			holding = false;
			treeDisplay.pleaseUpdate(firstTime);
			firstTime = traceModule.Q;
			if (traceLegend!=null && traceModule.showLegend.getValue()) {
				traceLegend.offHold();
				traceLegend.repaint();
			}
		}
		traceModule.calculationsDone();
	}
	void revertColors(){
		for (int i = 0; i<64; i++)
			for (int k= 0; k<64; k++)
				traceLegend.modifiedColors[i][k] = false;

		resetColors();
		modColorsIncorporated = true;

	}
	boolean modColorsIncorporated = true;
	public void incorporateModColors(){
		modColorsIncorporated = false;
	}
	public void cursorEnterBranch(Tree tree, int N, Graphics g) {
		if (traceModule.showLegend.getValue() && traceLegend!=null && history!=null){
			if (tree.nodeIsTerminal(N)){
				taxonMessage( tree, tree.taxonNumberOfNode(N));
			}
			else {
				String expl = history.getExplanation();
				if (expl == null)
					expl = "";
				else expl += "\n";
				traceLegend.setMessage(expl + history.toString(N, "\n"));
			}
		}
		if (treeDisplay != null)
			treeDisplay.requestFocus();
	}
	public void cursorExitBranch(Tree tree, int N, Graphics g) {
		if (traceModule.showLegend.getValue() && traceLegend!=null)
			traceLegend.setMessage("");
		if (treeDisplay != null)
			treeDisplay.requestFocus();
	}
	public void cursorEnterTaxon(Tree tree, int M, Graphics g) {
		taxonMessage(tree, M);
	}
	public void cursorExitTaxon(Tree tree, int M, Graphics g) {
		if (traceModule.showLegend.getValue() && traceLegend!=null)
			traceLegend.setMessage("");
	}

	private void taxonMessage(Tree tree, int it){
		if (traceModule.showLegend.getValue() && traceLegend!=null && history!=null && history.getObservedStates()!=null){
			if (it == -1)
				traceLegend.setMessage("");
			else {

				String h = history.getExplanation();
				if (h == null)
					h = "";
				else h += "\n";
				int node = tree.nodeOfTaxonNumber(it);
				h += history.toString(node, "\n");

				traceLegend.setMessage("Observed states: " + history.getObservedStates().toString(it, "\n") + "\n" + h);
			}
		}
	}
	public void cursorMove(Tree tree, int x, int y, Graphics g) {
		int boxFound = treeDisplay.getTreeDrawing().findTerminalBox(tree,  x, y);
		taxonMessage(tree, boxFound);
	}
	public void resetColors(){
		
		if (traceModule.colorMode.getValue()==1)
			colorTable = MesquiteColorTable.DEFAULTGRAYTABLE.cloneColorTable();
		else if (traceModule.colorMode.getValue()==2)
			colorTable = MesquiteColorTable.DEFAULTREDTABLE.cloneColorTable();
		else if (traceModule.colorMode.getValue()==3)
			colorTable = MesquiteColorTable.DEFAULTGREENTABLE.cloneColorTable();
		else if (traceModule.colorMode.getValue()==4)
			colorTable = MesquiteColorTable.DEFAULTBLUETABLE.cloneColorTable();
		else
			colorTable = MesquiteColorTable.DEFAULTCOLORTABLE.cloneColorTable();
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		if (!holding) {
			if (traceModule.historyTask!=null) {
				if (decorator==null)
					decorator = traceModule.displayTask.createTreeDecorator(treeDisplay, this);
				if (decorator==null)
					return;
				decorator.useColorTable(colorTable);
				if (history !=null)
					decorator.drawOnTree(tree, drawnRoot,  history, history.getObservedStates(), traceModule.showStateWeights, g); 
				if (traceModule.showLegend.getValue()  && traceLegend!=null) {
					if (history!=null) {
						history.prepareColors(tree, drawnRoot);
						traceLegend.setStates(history);
					}
					traceLegend.adjustLocation();
					if (!traceLegend.isVisible())
						traceLegend.setVisible(true);
				}
			}
		}
	}
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}
	/**return a text version of information at node*/
	public String textAtNode(Tree tree, int node){
		if (history!=null)
			return history.toString(node, ", ");
		else
			return null;
	}
	
	CharacterState htmlNodeState, htmlAncestorState;
	void getChangesHTML(Tree tree, int node, CharacterHistory history, StringBuffer sb){
		if (tree.getRoot() != node){
			htmlNodeState = history.getCharacterState(htmlNodeState, node); 
			if (tree.nodeIsInternal(node))
				adjustConservatism(htmlNodeState);
			htmlAncestorState = history.getCharacterState(htmlAncestorState, tree.motherOfNode(node)); 
			adjustConservatism(htmlAncestorState);
			if (htmlNodeState != null && htmlAncestorState != null && !htmlNodeState.equals(htmlAncestorState)){
				sb.append("<tr><td>" + node +"</td><td>" + htmlAncestorState.toDisplayString() +"</td><td>" + htmlNodeState.toDisplayString() + "</td>");
				if (htmlNodeState instanceof CategoricalState){
					sb.append("<td>");
					CategoricalState cN = (CategoricalState)htmlNodeState;
					CategoricalState cA = (CategoricalState)htmlAncestorState;
					if (!CategoricalState.statesShared(cN.getValue(), cA.getValue()))
							sb.append("*");
					sb.append("</td>" );
				}
				sb.append("</tr>" );
			}
		}
		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter))
			getChangesHTML(tree, daughter, history, sb);
	}

	/**return a text version of information at node*/
	public void getSummaryHTML(StringBuffer sb){
		if (history!=null && myTree != null && sb!= null){
			htmlNodeState = history.getCharacterState(htmlNodeState, myTree.getRoot()); 
			sb.append("State at root: " + htmlNodeState.toDisplayString());
			sb.append("<p>Table of nodes whose reconstructed state or state sets differ from those of their immediate ancestors:");
			sb.append("<table border=\"1\">");
			sb.append("<tr><td>node</td><td>state at ancestor</td><td>state at node</td>" );
			if (htmlNodeState instanceof CategoricalState)
				sb.append("<td>unambiguous change?</td>");
			sb.append("</tr>");
			getChangesHTML(myTree, myTree.getRoot(),  history, sb);
			sb.append("</table>");
		}

	}
	void adjustConservatism(CharacterState state){ //this is a KLUDGE!  Required because reconstruction doesn't set uncertainty bit to represent equivocal, and thus doesn't distinguish true polymorphic ancestors from uncertainty.  Here forced to be uncertain for display sake
		if (state != null && state instanceof CategoricalState){
			CategoricalState c = (CategoricalState)state;
			if (CategoricalState.hasMultipleStates(c.getValue()))
					c.setUncertainty(true);
				
		}
	}
	void getChangesAtNodes(Tree tree, int node, CharacterHistory history, StringBuffer sb){
		if (tree.getRoot() != node){
			htmlNodeState = history.getCharacterState(htmlNodeState, node); 
			if (tree.nodeIsInternal(node))
				adjustConservatism(htmlNodeState);
			htmlAncestorState = history.getCharacterState(htmlAncestorState, tree.motherOfNode(node)); 
			adjustConservatism(htmlAncestorState);
			if (htmlNodeState != null && htmlAncestorState != null && !htmlNodeState.equals(htmlAncestorState)){
				sb.append("" + node + "\t" + htmlAncestorState.toDisplayString() + "\t" + htmlNodeState.toDisplayString());
				if (htmlNodeState instanceof CategoricalState){
					sb.append("\t");
					CategoricalState cN = (CategoricalState)htmlNodeState;
					CategoricalState cA = (CategoricalState)htmlAncestorState;
					if (!CategoricalState.statesShared(cN.getValue(), cA.getValue()))
							sb.append("*");
				}
				
				sb.append("\n");
			}
		}
		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter))
			getChangesAtNodes(tree, daughter, history, sb);
	}

	/**return a text version of information at node*/
	public void getSummaryTable(StringBuffer sb){
		if (history!=null && myTree != null && sb!= null){
			htmlNodeState = history.getCharacterState(htmlNodeState, myTree.getRoot()); 
			sb.append("Tree: " + myTree.getName() + "\n\n");
			sb.append(resultString.getValue() + "\n\n");
			sb.append("State at root: " + htmlNodeState.toDisplayString() + "\n");
			sb.append("Table of nodes whose reconstructed state or state sets differ from those of their immediate ancestor:\n\n");
			sb.append("node\tstate at ancestor\tstate at node" );
			if (htmlNodeState instanceof CategoricalState)
				sb.append("\tunambiguous change?");
				
			sb.append("\n");
			getChangesAtNodes(myTree, myTree.getRoot(),  history, sb);
		}

	}
	/**return a text version of any legends or other explanatory information*/
	public String textForLegend(){
		String s = "";
		if (!traceModule.showStateWeights.getValue())
			s = " (NOTE: the graphic display of reconstruction is NOT showing the reconstruction proportional to the weights (e.g. likelihoods) of the states)";
		return resultString.getValue() + s;
	}
	public void turnOff(){
		if (decorator!=null) {
			decorator.turnOff();
			decorator = null;
		}
		if (traceLegend!=null && treeDisplay!=null){
			removePanelPlease(traceLegend);
			traceLegend = null;
		}
		super.turnOff();
	}
}
/* ======================================================================== */
class TraceLegend extends TreeDisplayLegend {
	private TraceCharacterHistory traceModule;
	public MiniScroll characterScroll = null;
	public MiniScroll mappingScroll = null;
	private TraceCharacterOperator tcOp;
	private String[] stateNames;
	private Color[] legendColors;
	Point[] tableMappings;
	boolean[][] modifiedColors;
	private static final int defaultLegendWidth=142;
	private static final int defaultLegendHeight=120;
	private int numBoxes=0;
	private int oldNumChars = 0;
	private long oldNumMappings = 0;
	private int oldNumBoxes=0;
	private int oldCurrentChar = -1;
	private long oldCurrentMapping = -1;
	private boolean resizingLegend=false;
	private TCMPanel messageBox;
	private SpecsPrintTextArea specsBox;
	private boolean holding = false;
	int scrollAreaHeight = 41;

	final int charScrollAreaHeight = 41;
	final int mapScrollAreaHeight = 0;
	private int messageHeight = 22;
	final int defaultSpecsHeight = (14 + MesquiteModule.textEdgeCompensationHeight) * 3;
	private int specsHeight = defaultSpecsHeight;
	private int e = 4;


	public TraceLegend(TraceCharacterHistory traceModule, TraceCharacterOperator tcOp) {
		super(tcOp.treeDisplay,defaultLegendWidth, defaultLegendHeight);
		setVisible(false);
		legendWidth=defaultLegendWidth;
		legendHeight=defaultLegendHeight;
		if (MesquiteInteger.isCombinable(traceModule.initialLegendWidth))
			legendWidth = traceModule.initialLegendWidth;
		if (MesquiteInteger.isCombinable(traceModule.initialLegendHeight))
			legendHeight = traceModule.initialLegendHeight;
		setOffsetX(traceModule.initialOffsetX);
		setOffsetY(traceModule.initialOffsetY);
		this.tcOp = tcOp;
		this.traceModule = traceModule;
		setLayout(null);
		setSize(legendWidth, legendHeight);
		stateNames = new String[64];
		legendColors = new Color[64];
		tableMappings = new Point[64];
		modifiedColors = new boolean[64][64];
		for (int i=0; i<64; i++) {
			stateNames[i] = null;
			legendColors[i] = null;
			tableMappings[i] = null;
			for (int j=0; j<64; j++)
				modifiedColors[i][j] = false;
		}
		specsBox = new SpecsPrintTextArea(" ", 2, 2, TextArea.SCROLLBARS_NONE);
		specsBox.setEditable(false);
		if (traceModule.showLegend.getValue())
			specsBox.setVisible(false);
		specsBox.setBounds(1,scrollAreaHeight +2,legendWidth-2, specsHeight);

		messageBox = new TCMPanel();
		int mTop = MesquiteInteger.maximum(legendHeight-messageHeight-8, scrollAreaHeight+2)+2;
		int mHeight = MesquiteInteger.minimum(legendHeight-mTop-8, messageHeight);
		messageBox.setBounds(2,mTop,legendWidth-6, mHeight);
		messageBox.setText("\n");
		try {
			messageBox.setFocusTraversalKeysEnabled(false);
		}
		catch (Error e){
		}
		//messageBox.setColor(Color.pink);
		//messageBox.setBackground(Color.pink);
		add(messageBox);
		add(specsBox);
	}

	public void adjustScroll(int currentCharExternal, int numChars) {
		if (characterScroll == null) {
			characterScroll = new MiniScroll( MesquiteModule.makeCommand("setCharacter",  (Commandable)traceModule), false, currentCharExternal, 1,  numChars,"character");
			add(characterScroll);
			characterScroll.setLocation(2,18);//18);
			characterScroll.setColor(Color.blue);
			repaint();
			oldNumChars = numChars;
			oldCurrentChar = currentCharExternal;
		}
		else {
			if (oldNumChars != numChars) {
				characterScroll.setMaximumValue(numChars);
				oldNumChars = numChars;
			}
			if (oldCurrentChar != currentCharExternal) {
				characterScroll.setCurrentValue(currentCharExternal);
				oldCurrentChar = currentCharExternal;
			}
		}
	}
	public void adjustMappingScroll(long currentMappingExternal, long numMappings) {
		if (mappingScroll == null) {
			mappingScroll = new MiniScroll( MesquiteModule.makeCommand("setMapping",  (Commandable)traceModule), false, currentMappingExternal, 1,  numMappings,"mapping");
			add(mappingScroll);
			if (characterScroll != null)
				mappingScroll.setLocation(characterScroll.getBounds().x + getBounds().width + 4,18 + charScrollAreaHeight);//18);
			else
				mappingScroll.setLocation(2,18 + charScrollAreaHeight);//18);
			mappingScroll.setColor(Color.blue);
			repaint();
			oldNumMappings = numMappings;
			oldCurrentMapping = currentMappingExternal;
		}
		else {
			if (oldNumMappings != numMappings) {
				mappingScroll.setMaximumValueLong(numMappings);
				oldNumMappings = numMappings;
			}
			if (oldCurrentMapping != currentMappingExternal) {
				mappingScroll.setCurrentValueLong(currentMappingExternal);
				oldCurrentMapping = currentMappingExternal;
			}
		}
		if (numMappings >1){
			mappingScroll.setVisible(true);
			scrollAreaHeight = charScrollAreaHeight + mapScrollAreaHeight;
			checkComponentSizes();
		}
		else {
			mappingScroll.setVisible(false);
			scrollAreaHeight = charScrollAreaHeight;
			checkComponentSizes();
		}
	}
	public void checkComponentSizes(){
		int oldWidth = legendWidth;
		int oldHeight = legendHeight;
		if (characterScroll != null)
			mappingScroll.setLocation(characterScroll.getBounds().x + characterScroll.getBounds().width + 4,18);//18);
		else
			mappingScroll.setLocation(2,18 + charScrollAreaHeight);//18);
		if (mappingScroll.isVisible() && legendWidth< mappingScroll.getX() + mappingScroll.getWidth())
			legendWidth=mappingScroll.getX() + mappingScroll.getWidth()+ 4;
		else if (legendWidth< characterScroll.getX() + characterScroll.getWidth())
			legendWidth=characterScroll.getX() + characterScroll.getWidth()+ 4;
		specsBox.setBounds(1,scrollAreaHeight+2,legendWidth-2, specsHeight);
		specsBox.setVisible(true);
		messageHeight = messageBox.getHeightNeeded();
		if (messageHeight<20)
			messageHeight = 20;
		legendHeight=numBoxes*16+scrollAreaHeight + specsHeight + e + messageHeight + 4;
		int mTop = MesquiteInteger.maximum(legendHeight-messageHeight-8, scrollAreaHeight+2) +2;
		int mHeight = MesquiteInteger.minimum(legendHeight-mTop-8, messageHeight);
		messageBox.setBounds(2,mTop,legendWidth-6, mHeight);
		if (oldWidth != legendWidth || oldHeight != legendHeight || getWidth() != legendWidth || getHeight() != legendHeight){
			setSize(legendWidth, legendHeight);
			repaint();
		}
	}

	public void setVisible(boolean b) {
		super.setVisible(b);
		if (characterScroll !=null)
			characterScroll.setVisible(b);
		if (messageBox!=null)
			messageBox.setVisible(b);
		if (specsBox!=null)
			specsBox.setVisible(b);
	}

	public void setLegendWidth(int w){
		legendWidth = w;
	}
	public void setLegendHeight(int h){
		legendHeight = h;
	}
	public int findBox(int x, int y) {
		if (numBoxes!=0) {
			for (int ibox=0; ibox<numBoxes; ibox++) {
				int top = ibox*16+scrollAreaHeight + specsHeight+ e;
				if (x >= 4 && y >= top && x <= 24 && y <= top +12)
					return ibox;
			}
		}
		return -1;
	}
	public void paint(Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		if (!holding) {
			g.setColor(Color.black);
			if (numBoxes!=0) {
				for (int ibox=0; ibox<numBoxes; ibox++) {
					g.setColor(legendColors[ibox]);
					g.fillRect(4, ibox*16+scrollAreaHeight + specsHeight+ e, 20, 12);
					g.setColor(Color.black);
					g.drawRect(4, ibox*16+scrollAreaHeight + specsHeight + e, 20, 12);
					if (stateNames[ibox]!=null)
						g.drawString(stateNames[ibox], 28, ibox*16 + specsHeight+scrollAreaHeight + 12 + e);
				}
			}
			g.setColor(Color.cyan);
			g.drawRect(0, 0, legendWidth-1, legendHeight-1);
			g.fillRect(legendWidth-6, legendHeight-6, 6, 6);
			g.drawLine(0, scrollAreaHeight, legendWidth-1, scrollAreaHeight);
			g.setColor(Color.blue);
			g.drawString("Trace Character", 4, 14);
			if (mappingScroll.isVisible()){
				if (legendWidth< mappingScroll.getX() + mappingScroll.getWidth()) {
					checkComponentSizes();
					MesquiteWindow.uncheckDoomed(this);
					return;
				}
				String mtn = "Mapping";
				if (traceModule.historyTask != null)
					mtn = traceModule.historyTask.getMappingTypeName();
				if (characterScroll.isVisible())
					g.drawString(mtn, 8 + characterScroll.getBounds().width + characterScroll.getBounds().x, 14);
				else
					g.drawString(mtn, 4 + 40, 14);
			}
			g.setColor(Color.black);
			if (tcOp.resultString.getValue()!=null && !tcOp.resultString.getValue().equals(specsBox.getText())) {

				specsBox.setText(tcOp.resultString.getValue()); 
			}
			if (specsBox.getBackground() != getBackground())
				specsBox.setBackground(getBackground());
		}
		MesquiteWindow.uncheckDoomed(this);
	}
	public void print(Graphics g) {
		if (!traceModule.showLegend.getValue())
			return;
		if (MesquiteWindow.checkDoomed(this))
			return;
		if (!holding) {
			int printHeight = legendHeight-1;
			if (numBoxes!=0) 
				printHeight = (numBoxes)*16+scrollAreaHeight + specsHeight + e;
			g.setColor(Color.white);

			g.fillRect(0, 0, legendWidth-1, printHeight);
			g.setColor(Color.cyan);
			g.drawRect(0, 0, legendWidth-1, printHeight);
			g.setColor(Color.black);
			String info = tcOp.resultString.getValue();
			StringInABox sib = new StringInABox(info, g.getFont(), legendWidth);
			sib.draw(g, 4, 4);
			if (numBoxes!=0) {
				for (int ibox=0; ibox<numBoxes; ibox++) {
					g.setColor(legendColors[ibox]);
					g.fillRect(4, ibox*16+scrollAreaHeight + specsHeight+ e, 20, 12);
					g.setColor(Color.black);
					g.drawRect(4, ibox*16+scrollAreaHeight + specsHeight + e, 20, 12);
					if (stateNames[ibox]!=null)
						g.drawString(stateNames[ibox], 28, ibox*16 + specsHeight+scrollAreaHeight + 12 + e);
				}
			}
		}
		MesquiteWindow.uncheckDoomed(this);
	}


	public void setStates(CharacterHistory statesAtNodes){
		numBoxes = statesAtNodes.getLegendStates(legendColors, stateNames, tableMappings, tcOp.colorTable);

		if (numBoxes!=oldNumBoxes) {
			reviseBounds();
			oldNumBoxes=numBoxes;
		}
		repaint();
	}
	/*-----------------------------*/
	public String getModColorsCommand(){
		String s = "modifyColors ";
		boolean found = false;
		for (int i = 0; i<64; i++){
			for (int j = 0; j<64; j++){
				if (modifiedColors[i][j]) {
					Color c = tcOp.colorTable.getColorDirect(i, j);
					if (c == null)
						return null;
					s += " " + i + " " + j + "  " + c.getRed() + " " + c.getGreen() + " " + c.getBlue();
					found = true;
				}
			}
		}
		if (!found)
			return null;
		return s;
	}
	public void resetColor(Point tablePoint, Color color){
		if (color != null && tcOp.colorTable.setColorEnabled()){

			tcOp.colorTable.setColor(tablePoint.x, tablePoint.y, color);
			modifiedColors[tablePoint.x][tablePoint.y] = true;
			legendColors[tablePoint.y] = color;
		}

	}
	/*-----------------------------*/
	public void resetColor(int box){
		if (tableMappings != null && box >= 0 && box <tableMappings.length){
			Point tablePoint = tableMappings[box];
			if (tablePoint != null){
				if (!tcOp.colorTable.setColorEnabled()){
					tcOp.ownerModule.alert("You aren't able to change the colors for this character");
					return;
				}
				else if (tcOp.colorTable.getMode()==MesquiteColorTable.GRAYSCALE){
					tcOp.ownerModule.alert("You can't change the gray scale colors");
					return;
				}
				Color color = ColorDialog.queryColor(MesquiteWindow.windowOfItem(this), "Choose Color", "Color for state", tcOp.colorTable.getColor(tablePoint.x, tablePoint.y));
				if (color != null){

					tcOp.colorTable.setColor(tablePoint.x, tablePoint.y, color);
					modifiedColors[tablePoint.x][tablePoint.y] = true;
					legendColors[box] = color;
					tcOp.treeDisplay.pleaseUpdate(true);
					tcOp.traceModule.startingColors = getModColorsCommand();
					tcOp.traceModule.revertColorsItem.setEnabled(true);
					tcOp.traceModule.setColorsAsDefaultItem.setEnabled(true);
					MesquiteTrunk.resetMenuItemEnabling();
					repaint();
				}
			}
		}
	}
	public void mouseDown(int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		super.mouseDown(modifiers, clickCount, when, x, y, tool);
		int box = findBox(x, y);
		if (box >=0 && clickCount>=2)
			resetColor(box);
	}

	public void legendResized(int widthChange, int heightChange){
		if ((specsHeight + heightChange)>= defaultSpecsHeight)
			specsHeight += heightChange;
		else
			specsHeight  = defaultSpecsHeight;
		checkComponentSizes();
	}
	public void reviseBounds(){
		checkComponentSizes();
		Point where = getLocation();
		setBounds(where.x,where.y,legendWidth, legendHeight);
	}

	public void setMessage(String s) {
		if (s==null || s.equals("")) {
			//messageBox.setBackground(ColorDistribution.light);
			messageBox.setText("\n");
			reviseBounds();
		}
		else {
			//messageBox.setBackground(Color.white);
			messageBox.setText(s);
			reviseBounds();
		}
	}
	public void setSpecsMessage(String s) {
		if (s!=null) {
			specsBox.setText(s);
		}
	}
	public void onHold() {
		holding = true;
	}

	public void offHold() {
		holding = false;
	}
}

class TCMPanel extends Panel {
	String message = "";
	StringInABox box;
	public TCMPanel(){
		super();
		box =  new StringInABox("", null, getBounds().width);
	}
	public void setBounds(int x, int y, int w, int h){
		super.setBounds(x,y,w,h);
		box.setWidth(w);
	}
	public void setSize(int w, int h){
		super.setSize(w,h);
		box.setWidth(w);
	}
	public int getHeightNeeded(){
		return box.getHeight() + 5;
	}
	public void paint (Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		box.setFont(g.getFont());
		box.draw(g,0, 0);
		MesquiteWindow.uncheckDoomed(this);
	}

	public void setText(String s) {
		message = s;
		box.setString(s, getGraphics());
		repaint();
	}
	public void print(Graphics g){
	}
	public void printAll(Graphics g){
	}
	public void printComponents(Graphics g){
	}
}

class SpecsPrintTextArea extends TextArea {
	public SpecsPrintTextArea(String s, int a, int b, int c){
		super(s, a, b, c);
	}

	public void setText(String s){
		try {
			super.setText(s);
			if (isVisible())
				setCaretPosition(0);
		}
		catch (Exception e){
		}
	}

}
