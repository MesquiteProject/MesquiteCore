/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.ancstates.ShadeStatesOnTree;

import java.util.*;

import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class ShadeStatesOnTree extends DisplayStatesAtNodes {
	Vector shaders;
	public boolean holding = false;
	MesquiteBoolean showLabels;
	MesquiteBoolean showPredictions;
	MesquiteBoolean useGray;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		showLabels = new MesquiteBoolean(false);
		useGray = new MesquiteBoolean(false);
		showPredictions = new MesquiteBoolean(false);
		addCheckMenuItem(null, "Show Labels for States", makeCommand("toggleLabels", this), showLabels);
		addCheckMenuItem(null, "Show Predictions for Missing and Inapplicable Data", makeCommand("togglePredictions", this), showPredictions);
		addCheckMenuItem(null, "Use Gray for Equivocal", makeCommand("toggleGray", this), useGray);
		shaders = new Vector();
		return true;
	}

	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public   TreeDecorator createTreeDecorator(TreeDisplay treeDisplay, TreeDisplayExtra ownerExtra) {
		ShadeStatesDecorator newShader = new ShadeStatesDecorator(this, treeDisplay, ownerExtra);
		shaders.addElement(newShader);
		return newShader;
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("toggleLabels " + showLabels.toOffOnString());
		temp.addLine("togglePredictions " + showPredictions.toOffOnString());
		temp.addLine("toggleGray " + useGray.toOffOnString());
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets whether or not states are labeled", "[on = labeled; off]", commandName, "toggleLabels")) {
			showLabels.toggleValue(parser.getFirstToken(arguments));
			parametersChanged();
		}
		else	if (checker.compare(this.getClass(), "Sets whether or not states equivocal is shown as gray", "[on = labeled; off]", commandName, "toggleGray")) {
			useGray.toggleValue(parser.getFirstToken(arguments));
			parametersChanged();
		}
		else	if (checker.compare(this.getClass(), "Sets whether or not predictions are shown for clades with missing or inapplicable data", "[on = labeled; off]", commandName, "togglePredictions")) {
			showPredictions.toggleValue(parser.getFirstToken(arguments));
			parametersChanged();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Shade states";
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Shows the states at nodes by shading branches or nodes using colors, black and white, or shades of gray.";
	}
	public void onHold() {
		holding = true;
	}

	public void offHold() {
		holding = false;
	}
}
/* ======================================================================== */
class ShadeStatesDecorator extends TreeDecorator {
	ShadeStatesOnTree ownerModule;
	ColorDistribution colors;
	ColorDistribution grayEquivocal;
	ColorEventVector colorSequence;
	TreeDrawing oldDrawing = null;
	boolean turnedOff = false;

	public ShadeStatesDecorator (ShadeStatesOnTree ownerModule, TreeDisplay treeDisplay, TreeDisplayExtra ownerExtra) {
		super(treeDisplay, ownerExtra);
		this.ownerModule=ownerModule;
		colors = new ColorDistribution();
		grayEquivocal = new ColorDistribution();
		grayEquivocal.setColor(0, Color.lightGray);
		turnedOff = false;
		if (treeDisplay!=null && treeDisplay.getTreeDrawing()!=null) {
			treeDisplay.getTreeDrawing().incrementEnableTerminalBoxes();
			oldDrawing = treeDisplay.getTreeDrawing();
		}
	}
	/*.................................................................................................................*/
	private   void writeStateAtNode(CharacterHistory statesAtNodes,CharacterDistribution observedStates, Graphics g, int N,  Tree tree) {
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			writeStateAtNode(statesAtNodes, observedStates, g, d, tree);
		int nodeX = (int)treeDisplay.getTreeDrawing().x[N];  // integer node approximation
		int nodeY = (int)treeDisplay.getTreeDrawing().y[N];  // integer node approximation
		if (treeDisplay.getOrientation() == treeDisplay.UP) {
			nodeY+=10;
			//nodeX+=10;
		}
		else if (treeDisplay.getOrientation() == treeDisplay.DOWN) {
			nodeY-=10;
			//nodeX+=10;
		}
		else if (treeDisplay.getOrientation() == treeDisplay.RIGHT) {
			//nodeY=20;
			nodeX-=10;
		}
		else if (treeDisplay.getOrientation() == treeDisplay.LEFT) {
			//nodeY+=20;
			nodeX+=10;
		}
		StringUtil.highlightString(g, statesAtNodes.toString(N, " "), nodeX, nodeY, Color.blue, Color.white);
	}
	CharacterState cs;

	MesquiteColorTable colorTable = MesquiteColorTable.DEFAULTCOLORTABLE; //MesquiteColorTable.GRAYSCALE

	/*.................................................................................................................*/
	private boolean shadeNode(int N, Tree tree, CharacterHistory statesAtNodes, CharacterDistribution observedStates, MesquiteBoolean showStateWeights, Graphics g) {
		boolean anyDataInClade = ownerModule.showPredictions.getValue();
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
			boolean dataInDaughterClade = shadeNode(d, tree, statesAtNodes, observedStates, showStateWeights, g);
			if (dataInDaughterClade)
				anyDataInClade = true;
		}
		//This is the tracing of changes along the internode, for instance for stochastic character mapping
		colorSequence = statesAtNodes.getColorSequenceAtNode(N, colorTable);
		if (colorSequence != null) {
			treeDisplay.getTreeDrawing().fillBranchWithColorSequence(tree,  N, colorSequence, g);
			anyDataInClade = true;
		}
		else {

			if (tree.nodeIsTerminal(N)) {  //terminal
				if (observedStates !=null) {
					int M = tree.taxonNumberOfNode(N);
					if (!observedStates.isUnassigned(M)&&!(observedStates.isInapplicable(M))) {
						int numColors=0;
						
						numColors=statesAtNodes.getColorsAtNode(N, colors, colorTable, showStateWeights == null || showStateWeights.getValue()); //, ownerModule.getProject().stateColors
						treeDisplay.getTreeDrawing().fillBranchWithColors(tree,  N, colors, g);
						numColors = statesAtNodes.getColorsOfState(cs = observedStates.getCharacterState(cs, M), colors, colorTable); //, ownerModule.getProject().stateColors

						treeDisplay.getTreeDrawing().fillTerminalBoxWithColors(tree,  N, colors, g);
						anyDataInClade = true;
					}
					else {  //missing data! Terminal
						if (ownerModule.showPredictions.getValue()){
							int numColors=statesAtNodes.getColorsAtNode(N, colors, colorTable, showStateWeights == null || showStateWeights.getValue()); //, ownerModule.getProject().stateColors
							treeDisplay.getTreeDrawing().fillBranchWithColors(tree,  N, colors, g);
						}
						else
							treeDisplay.getTreeDrawing().fillBranchWithMissingData(tree,  N, g);
					}
				}
				else {
					int numColors=statesAtNodes.getColorsAtNode(N, colors, colorTable, showStateWeights == null || showStateWeights.getValue()); //, ownerModule.getProject().stateColors
					if (numColors!=0)
						treeDisplay.getTreeDrawing().fillTerminalBoxWithColors(tree,  N, colors, g);
					anyDataInClade = true;
				}
			}
			else {
				if (!anyDataInClade){  //missing data!!!
					treeDisplay.getTreeDrawing().fillBranchWithMissingData(tree,  N, g);
				}
				else {
					int numColors=statesAtNodes.getColorsAtNode(N, colors, colorTable, showStateWeights == null || showStateWeights.getValue()); //, ownerModule.getProject().stateColors

					if (ownerModule.useGray.getValue() && numColors >1) {
						treeDisplay.getTreeDrawing().fillBranchWithColors(tree,  N, grayEquivocal, g);
					}
					//This is the basic, old-fashioned tracing of a character
					else
						treeDisplay.getTreeDrawing().fillBranchWithColors(tree,  N, colors, g);
				}
			}

		}

		g.setColor(Color.black);
		return anyDataInClade;
	}
	public void useColorTable(MesquiteColorTable table){  
		colorTable = table;
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Object obj, Object obj2, Object obj3, Graphics g) {
		if (!(obj instanceof CharacterHistory))
			return;
		CharacterHistory statesAtNodes = (CharacterHistory)obj;
		CharacterDistribution observedStates = (CharacterDistribution)obj2;
		MesquiteBoolean showStateWeights = null;
		if (obj3 instanceof MesquiteBoolean)
			showStateWeights= (MesquiteBoolean)obj3;
		if (treeDisplay.getTreeDrawing()!=oldDrawing) {
			oldDrawing = treeDisplay.getTreeDrawing();
			oldDrawing.incrementEnableTerminalBoxes();
		}
		if (!ownerModule.holding) {
			if (treeDisplay!=null && tree!=null && statesAtNodes!=null) {
				if (observedStates == null)
					observedStates = statesAtNodes.getObservedStates();
				statesAtNodes.prepareColors(tree, drawnRoot);
				shadeNode(drawnRoot, tree, statesAtNodes, observedStates, showStateWeights, g);
				if (ownerModule.showLabels.getValue())
					writeStateAtNode(statesAtNodes, observedStates, g,drawnRoot,tree);
			}
			else
				MesquiteMessage.warnProgrammer("Shade states -- null tree display, tree, or states at nodes");
		}
	}
	public void turnOff() {
		if (!turnedOff && treeDisplay!=null && treeDisplay.getTreeDrawing()!=null)
			treeDisplay.getTreeDrawing().decrementEnableTerminalBoxes();
		turnedOff = true;
	}
}

