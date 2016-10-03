/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.ornamental.NodeNumbers;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class NodeNumbers extends TreeDisplayAssistantD {
	public MesquiteBoolean terminalShow;
	SpotsDrawing spots;
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		addMenuItem("Remove Node Numbers", makeCommand("offNumbers", this));
		terminalShow = new MesquiteBoolean(true);
		addCheckMenuItem(null, "Show terminal numbers", MesquiteModule.makeCommand("showTerminals",  this), terminalShow);
		
		return true;
	}
 	public boolean getUserChooseable(){
 		return false;  //this module is treated as defunct, now that the tree draw coordiantor does it directly, but is retained for compatibility with old scripts
 	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		spots = new SpotsDrawing(this, treeDisplay, 0); //TODO: should remember all of these
		return spots;
	}
  	 
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
   	 	Snapshot temp = new Snapshot();
		temp.addLine("showTerminals " + terminalShow.toOffOnString()); 
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Turns off node numbers", null, commandName, "offNumbers")) {
			iQuit();
			resetContainingMenuBar();
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets whether terminal nodes are numbered", "[on or off]", commandName, "showTerminals")) {
    	 		terminalShow.toggleValue(parser.getFirstToken(arguments));
    	 		if (spots!=null)
    	 			spots.getTreeDisplay().pleaseUpdate(false);
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Show Node Numbers";
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Shows the node numbers on a tree." ;
   	 }
   	 public void endJob(){
	 	if (spots !=null)
	 		spots.turnOff(); //should do all
   	 	super.endJob();
   	 }
}

/* ======================================================================== */
class SpotsDrawing extends TreeDisplayDrawnExtra {
	public int oldNumTaxa;
	public int spotsize = 18;
	public NodeNumbers nnModule;
	public SpotsDrawing (NodeNumbers ownerModule, TreeDisplay treeDisplay, int numTaxa) {
		super(ownerModule, treeDisplay);
		nnModule = ownerModule;
		oldNumTaxa = numTaxa;
	}
	/*_________________________________________________*/
	private   void drawSpot(TreeDisplay treeDisplay, Tree tree, Graphics g, int N) {
		if (tree.nodeExists(N)) {
			if (tree.nodeIsInternal(N) || nnModule.terminalShow.getValue()){
				int i=0;
				int j=2;
				String s = Integer.toString(N);
				FontMetrics fm = g.getFontMetrics(g.getFont());
				int width = fm.stringWidth(s) + 6;
				int height = fm.getAscent()+fm.getDescent() + 6;
				if (spotsize>width)
					width = spotsize;
				if (spotsize>height)
					height = spotsize;
		        	g.setColor(Color.yellow);
				GraphicsUtil.fillOval(g,treeDisplay.getTreeDrawing().x[N] +i - width/2 , treeDisplay.getTreeDrawing().y[N] +i - height/2, width-i-i, height-i-i);
		        	g.setColor(Color.black);
		        	
				GraphicsUtil.drawOval(g,treeDisplay.getTreeDrawing().x[N] +i - width/2 , treeDisplay.getTreeDrawing().y[N] +i - height/2, width-i-i, height-i-i);
		        	
				GraphicsUtil.drawString(g, Integer.toString(N), treeDisplay.getTreeDrawing().x[N]+2- width/2, treeDisplay.getTreeDrawing().y[N]-4+ height/2);
			}
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				drawSpot(treeDisplay, tree, g, d);
		}
	}
	/*_________________________________________________*/
	public   void drawSpots(TreeDisplay treeDisplay, Tree tree, int drawnRoot, Graphics g) {
	        if (MesquiteTree.OK(tree)) {
	        	g.setColor(Color.red);
	        	//if (oldNumTaxa!= tree.getNumTaxa())
	        	//	adjustNumTaxa(tree.getNumTaxa());
	       	 	drawSpot(treeDisplay, tree, g, drawnRoot);  
	       	 }
	   }
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawSpots(treeDisplay, tree, drawnRoot, g);
	}
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}
	public   void setTree(Tree tree) {
	}
}
	


