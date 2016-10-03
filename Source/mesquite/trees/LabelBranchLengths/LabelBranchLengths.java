/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.LabelBranchLengths;
import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class LabelBranchLengths extends TreeDisplayAssistantD {
	Vector labelers;
	static boolean warningGiven = false;
	boolean useLabels = false;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		labelers = new Vector();
		addMenuItem( "Float length labels", makeCommand("floatLabels",  this));
		addMenuItem( "Remove Length Labels", makeCommand("hideLabels",  this));
 		return true;  //should make conditional on whether branch lengths shown
 	}
 	public boolean getUserChooseable(){
 		return false;  //this module is treated as defunct, now that the tree draw coordiantor does it directly, but is retained for compatibility with old scripts
 	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		BranchLengthsLabeler bLA = new BranchLengthsLabeler(this, treeDisplay);
		labelers.addElement(bLA);
		return bLA;
	}
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Turns off labeling of branch lengths", null, commandName, "hideLabels")) {
			iQuit();
    	 	}
    	 	else if (checker.compare(this.getClass(), "Toggles whether the branch length labels are on their own little panels", null, commandName, "floatLabels")) {
    	 		useLabels = !useLabels;
			Enumeration e = labelers.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof BranchLengthsLabeler) {
					BranchLengthsLabeler tCO = (BranchLengthsLabeler)obj;
		 			tCO.setUseLabels(useLabels);
		 		}
			}
			parametersChanged();
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Label Branch Lengths";
   	 }
   	 
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "An assistant to a tree display that labels branches to show their lengths." ;
   	 }
	/*.................................................................................................................*/
 	/** returns current parameters, for logging etc..*/
 	public String getParameters() {
 		return "";
   	 }
   	 
   	 public void endJob(){
		Enumeration e = labelers.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof BranchLengthsLabeler) {
				BranchLengthsLabeler tCO = (BranchLengthsLabeler)obj;
	 			tCO.turnOff();
	 		}
		}
		super.endJob();
   	 }
}

/* ======================================================================== */
class BranchLengthsLabeler extends TreeDisplayDrawnExtra   {
	TreeTool adjustTool;
	HandlesAtNodes handlesAtNodes;
	int originalX, originalY, lastX, lastY;
	int idNumber;
	boolean lineOn = false;
	boolean useLabels = false;
	public BranchLengthsLabeler (MesquiteModule ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
	}
	/*.................................................................................................................*/
	public void writeLengthAtNode(Graphics g, int N,  Tree tree) {
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				writeLengthAtNode(g, d, tree);
				
		double nodeX = treeDisplay.getTreeDrawing().x[N];
		double nodeY = treeDisplay.getTreeDrawing().y[N];
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
		StringUtil.highlightString(g, MesquiteDouble.toString(tree.getBranchLength(N)), nodeX, nodeY, Color.blue, Color.white);
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		if (treeDisplay!=null && tree!=null) {
 			if (useLabels) {
	 			if (handlesAtNodes==null)
	 				handlesAtNodes = new HandlesAtNodes(ownerModule, tree.getNumNodeSpaces(), treeDisplay);
	 			else if (handlesAtNodes.getTreeDisplay()!=treeDisplay)
	 				handlesAtNodes = new HandlesAtNodes(ownerModule, tree.getNumNodeSpaces(), treeDisplay);
	 			else if (handlesAtNodes.getNumNodes()!=tree.getNumNodeSpaces())
	 				handlesAtNodes.resetNumNodes(tree.getNumNodeSpaces());
				handlesAtNodes.locatePanels(tree, drawnRoot);
				handlesAtNodes.showPanels(tree, drawnRoot);
			}
			else {
				g.setColor(Color.blue);
				writeLengthAtNode(g, drawnRoot, tree);
				g.setColor(Color.black);
			}
		}
	}
	public void setUseLabels(boolean use){
		if (use==useLabels)
			return;
		useLabels = use;
		if (!useLabels) {
			if (handlesAtNodes!=null)
			 	handlesAtNodes.dispose();
			 handlesAtNodes=null;
			 
		}
	}
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}
	
	public   void setTree(Tree tree) {
	}
	public void turnOff() {
		if (handlesAtNodes!=null)
			handlesAtNodes.dispose();
		super.turnOff();
	}
}
	

/* ======================================================================== */
class HandlesAtNodes extends PanelsAtNodes  {
	
	
	public HandlesAtNodes(MesquiteModule ownerModule, int numNodes, TreeDisplay treeDisplay){
		super(ownerModule, numNodes, treeDisplay);
	} 
	
	public Panel makePanel(int i){
		HandlePanel c = new HandlePanel(ownerModule, getTreeDisplay(), i);
		return c;
	}
}

class HandlePanel extends MesquitePanel {
	MesquiteModule ownerModule;
	int idNumber;
	TreeDisplay treeDisplay;
	int originalX, originalY, lastX, lastY;
	boolean lineOn = false;
	
	public HandlePanel (MesquiteModule ownerModule, TreeDisplay treeDisplay, int idNumber) {
		this.ownerModule=ownerModule;
		this.idNumber=idNumber;
		this.treeDisplay=treeDisplay;
		setSize(8,8);
	}
	
	public void setTreeDisplay(TreeDisplay treeDisplay){
		this.treeDisplay=treeDisplay;
	}
	public void paint (Graphics g) {//^^^
	   	if (MesquiteWindow.checkDoomed(this))
	   		return;
		if (treeDisplay==null)  {
			System.out.println("tree display null in label branch lengths panel");
			return;
		}
		else if (treeDisplay.getTree()==null) {
			System.out.println("tree null in label branch lengths panel");
			return;
		}
		String s = MesquiteDouble.toStringDigitsSpecified(treeDisplay.getTree().getBranchLength(idNumber), 3);
		Font font = g.getFont();
		FontMetrics fontMet = g.getFontMetrics(font);
		int handleHeight = fontMet.getHeight()+2;
		int handleWidth = fontMet.stringWidth(s)+8;
		if (getBounds().width!= handleWidth || getBounds().height!= handleHeight) {
			setSize(handleWidth,handleHeight);
		}
		
		setBackground(getParent().getBackground());
		
		g.setColor( Color.blue);
		g.drawRoundRect(0,0,getBounds().width, getBounds().height,6,6);
		for (int green=1; green<9; green++) {
			g.setColor( new Color((((green*10) & 0xFF)<<16)| (((green*16+100) & 0xFF)<<8)| (255&0xFF)));
			g.drawRoundRect(green,green,getBounds().width-green-green, getBounds().height-green-green,6, 6);
		}
		g.setColor(Color.white);
		g.fillOval(2,2, 2, 2);
		g.setColor(Color.black);
		g.drawString(s,4, getBounds().height-3);
		MesquiteWindow.uncheckDoomed(this);
	}
	
	
	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		if (MesquiteEvent.controlKeyDown(modifiers)) {
			if (!(treeDisplay.getTree() instanceof AdjustableTree))
				return;
			AdjustableTree t = (AdjustableTree)treeDisplay.getTree();
			double oldLength =t.getBranchLength(idNumber);
			double newLength = MesquiteDouble.queryDouble(ownerModule.containerOfModule(), "Set Branch length", "Branch Length:", oldLength);
    	 		if (newLength>=0.0 && newLength!=oldLength) {
    	 			t.setBranchLength(idNumber, newLength, true);
    	 			repaint();
    	 			treeDisplay.pleaseUpdate(false);
    	 		}
		}
	}
	
}

