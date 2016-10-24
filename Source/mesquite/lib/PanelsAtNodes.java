/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib;

import java.awt.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public abstract class PanelsAtNodes  {
	protected MesquiteModule ownerModule;
	protected int numNodes;
	protected Panel[] panels;
	protected TreeDisplay treeDisplay;
	private boolean[] inTree;
	private boolean[] shown;
	
	public PanelsAtNodes(MesquiteModule ownerModule, int numNodes, TreeDisplay treeDisplay){
		this.ownerModule = ownerModule;
		this.numNodes = numNodes;
		this.treeDisplay = treeDisplay;
		panels=new Panel[numNodes];
		shown=new boolean[numNodes];
		inTree=new boolean[numNodes];

		for (int i=0; i<numNodes; i++) {
			panels[i]=makePanel(i);
			shown[i]=true;
			inTree[i]=false;
			
			if (treeDisplay!= null)
				treeDisplay.addPanelPlease(panels[i]);
			if (panels[i]!=null)
				panels[i].setVisible(false);
			
	
		}
	}
	
	public abstract Panel makePanel(int i);
	
	public TreeDisplay getTreeDisplay(){
		return treeDisplay;
	}
	public void setTreeDisplay(TreeDisplay treeDisplay){
		if (treeDisplay == this.treeDisplay)
			return;
		for (int i=0; i<numNodes; i++) {
			if (this.treeDisplay!=null)
				this.treeDisplay.removePanelPlease(getPanel(i));
			treeDisplay.addPanelPlease(getPanel(i));
		}
		this.treeDisplay = treeDisplay;
	}
	
	public void resetNumNodes(int numNodes){
		for (int i=0; i<this.numNodes; i++) {
 			Panel p =getPanel(i);
			if (p!= null) {
				p.setVisible(false);
				if (treeDisplay!=null)
					treeDisplay.removePanelPlease(p);
			}
		}
		this.numNodes = numNodes;
		panels=new Panel[numNodes];
		for (int i=0; i<numNodes; i++) {
			panels[i]=makePanel(i);
			if (treeDisplay != null)
				treeDisplay.addPanelPlease(panels[i]);
			panels[i].setVisible(true);
		}
	}
 	public void hideAllPanels(){
 		for (int i=0; i<numNodes; i++) {
 			Panel p =getPanel(i);
 			if (p != null) {
				shown[i]=false;
				if (p.isVisible())
					p.setVisible(false);
			}
		}
 	}
 	public void showAllPanels(){
 		for (int i=0; i<numNodes; i++) {
 			Panel p =getPanel(i);
 			if (p !=null) {
				shown[i]=true;
				if (!p.isVisible())
					p.setVisible(true);
			}
		}
 	}
 	public void toggleShowPanel(int i){
 		//check if legal!!!
 		Panel p =getPanel(i);
		if (p!=null) {
			shown[i]=!shown[i];
			p.setVisible(shown[i]);
		}
 	}
 	public void showPanel(int i){
 		//check if legal!!!
 		Panel p =getPanel(i);
		if (p!=null) {
			if (!p.isVisible())
				p.setVisible(true);
			shown[i]=true;
		}
 	}
 	public void hidePanel(int i){
 		Panel p =getPanel(i);
		if (p!=null) {
			shown[i]=false;
			if (p.isVisible())
				p.setVisible(false);
		}
 	}
 	public void toggleShowPanel(Panel panel){
 		for (int i=0; i<numNodes; i++) {
 			Panel p =getPanel(i);
 			if (p == panel) {
				shown[i]=!shown[i];
				p.setVisible(shown[i]);
				return;
			}
			
		}
 	}
 	public void hidePanel(Panel panel){
 		for (int i=0; i<numNodes; i++) {
 			Panel p =getPanel(i);
 			if (p == panel) {
				shown[i]=false;
				if (p.isVisible())
					p.setVisible(false);
				return;
			}
			
		}
 	}
 	public void showPanel(Panel panel){
 		for (int i=0; i<numNodes; i++) {
 			Panel p =getPanel(i);
 			if (p == panel) {
				shown[i]=true;
				if (!p.isVisible())
					p.setVisible(true);
				return;
			}
			
		}
 	}
	/*.................................................................................................................*/
	public   void repaintPanels(Tree tree, int node) {
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				repaintPanels(tree, d);
				
		Panel p = getPanel(node);
		if (p!=null) {
			p.repaint(); 
		}
		
	}
	/*.................................................................................................................*/
	private void recShowPanels(Tree tree, int N) {
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				recShowPanels(tree, d);
		inTree[N]=true;
	}
	/*.................................................................................................................*/
	public void showPanels(Tree tree, int drawnRoot) {
		for (int i=0; i<inTree.length; i++)
			inTree[i]=false;
		recShowPanels(tree, drawnRoot);
		int count =0;
		for (int i=0; i<inTree.length; i++) {
			if (!inTree[i] || !shown[i]) {
		 		Panel p =getPanel(i);
				if (p!=null && p.isVisible()) 
					p.setVisible(false);
			}
			else {
		 		Panel p =getPanel(i);
				if (p!=null && !p.isVisible()) 
					p.setVisible(true);
			}
		}
	}
	/*.................................................................................................................*/
	public   void locatePanels(Tree tree, int node) {
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				locatePanels(tree, d);
				
		int nodeX = (int)treeDisplay.getTreeDrawing().x[node];  //integer nodeloc approximation
		int nodeY = (int)treeDisplay.getTreeDrawing().y[node];  //integer nodeloc approximation
		Panel p = getPanel(node);
		if (p!=null){
			if  ((p.getLocation()==null) || ((p.getLocation().x!=nodeX) || (p.getLocation().y!=nodeY)))
				p.setLocation(nodeX, nodeY);  //only do if moved
		}
		
	}
	public void dispose() {
		for (int i=0; i<numNodes; i++) {
			Panel C = getPanel(i);
			
			if (C!=null) {
				treeDisplay.removePanelPlease(C);
			}
			panels[i] = null;
		}
	}
	public Panel getPanel(int N) {
		if (N<0 || N>= panels.length)
			return null;
		return panels[N];
	}
	public int getNumNodes() {
		return numNodes;
	}
}

