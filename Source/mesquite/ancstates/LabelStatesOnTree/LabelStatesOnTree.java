/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.ancstates.LabelStatesOnTree;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class LabelStatesOnTree extends DisplayStatesAtNodes {
	TreeDisplay treeDisplay;
 	Vector labellers;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		labellers = new Vector();
 		return true;
	}
	/*.................................................................................................................*/
	public   TreeDecorator createTreeDecorator(TreeDisplay treeDisplay, TreeDisplayExtra ownerExtra) {
		LabelStatesDecorator newLabeller = new LabelStatesDecorator(this, treeDisplay, ownerExtra);
		labellers.addElement(newLabeller);
		return newLabeller;
	}
 	
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
 	public void endJob() {
		Enumeration e = labellers.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof LabelStatesDecorator) {
				LabelStatesDecorator tCO = (LabelStatesDecorator)obj;
	 			tCO.turnOff();
	 		}
		}
 		super.endJob();
   	 }
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Label states";
   	 }
   	 
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "A module that displays character states on tree using labels.  This is a display-only module,"
 		+ " and would be hired by another module that organizes assigning states to the nodes." ;
   	 }
	/*.................................................................................................................*/
   	 
}

/* ======================================================================== */
class LabelStatesDecorator extends TreeDecorator {
 	LabelStatesOnTree ownerModule;

	public LabelStatesDecorator (LabelStatesOnTree ownerModule, TreeDisplay treeDisplay, TreeDisplayExtra ownerExtra) {
		super(treeDisplay, ownerExtra);
		this.ownerModule=ownerModule;
	}
	/*.................................................................................................................*/
	public   void labelNode(int N,  Tree tree, CharacterHistory statesAtNodes, CharacterDistribution observedStates, Graphics g) {
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				labelNode(d, tree, statesAtNodes, observedStates, g);
		int nodeX = (int)treeDisplay.getTreeDrawing().x[N];  // integer node approximation
		int nodeY = (int)treeDisplay.getTreeDrawing().y[N];  // integer node approximation
		String label = statesAtNodes.toString(N, "\n"); 
		if (label != null){
			int width = StringUtil.getStringDrawLength(g, label);
			int height = StringUtil.getTextLineHeight(g);
			g.setColor(ColorDistribution.veryLightGray);
			g.fillRect(nodeX-4, nodeY-4, width+8, height+8);
			g.setColor(Color.black);
			g.drawRect(nodeX-4, nodeY-4, width+8, height+8);
			g.drawString(label, nodeX, nodeY+ height);
		}
		//g.setColor(Color.black);
		/*Note: this does not yet show the observed states separately.  In light of the likely limited use of this compared to ShadeStates, it doesn't
		seem worth enhancing this too much.
		if (tree.nodeIsTerminal(N)) {
			if (observedStates !=null) {
				if (!observedStates.isUnassigned(tree.taxonNumberOfNode(N))&&!(observedStates.isInapplicable(tree.taxonNumberOfNode(N))))
					;
			}
			else
				;
		}
		*/
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Object obj, Object obj2, Object obj3, Graphics g) {
		if (!(obj instanceof CharacterHistory))
			return;
		CharacterHistory statesAtNodes = (CharacterHistory)obj;
		CharacterDistribution observedStates = (CharacterDistribution)obj2;
		if (treeDisplay!=null && tree!=null && statesAtNodes!=null) {
				if (observedStates == null)
					observedStates = statesAtNodes.getObservedStates();
			
			statesAtNodes.prepareColors(tree, drawnRoot);
			labelNode(drawnRoot, tree, statesAtNodes,observedStates, g);

		}
		else
			MesquiteMessage.warnProgrammer("Error: draw on tree label states//null");
	}
	
	/*.................................................................................................................*/
	public void turnOff() {
	}
}

