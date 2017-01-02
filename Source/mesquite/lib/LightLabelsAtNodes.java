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
public class LightLabelsAtNodes  {
	protected int numNodes;
	protected LightLabel[] labels;
	protected TreeDisplay treeDisplay;
	private boolean[] inTree;
	private boolean[] shown;
	
	public LightLabelsAtNodes(int numNodes, TreeDisplay treeDisplay){
		this.numNodes = numNodes;
		this.treeDisplay = treeDisplay;
		labels=new LightLabel[numNodes];
		shown=new boolean[numNodes];
		inTree=new boolean[numNodes];

		for (int i=0; i<numNodes; i++) {
			labels[i]=new LightLabel();
			shown[i]=true;
			inTree[i]=false;
			labels[i].setVisible(false);
		}
	}
	
	public TreeDisplay getTreeDisplay(){
		return treeDisplay;
	}
	public void setTreeDisplay(TreeDisplay treeDisplay){
		this.treeDisplay = treeDisplay;
	}
	
	public void resetNumNodes(int numNodes){
	//save old text???
		this.numNodes = numNodes;
		for (int i=0; i<numNodes; i++) {
			labels[i]=new LightLabel();
			shown[i]=true;
			inTree[i]=false;
			labels[i].setVisible(false);
		}
	}
 	public void toggleShowLightLabel(int i){
 		//check if legal!!!
 		LightLabel p =getLabel(i);
		if (p!=null) {
			shown[i]=!shown[i];
			p.setVisible(shown[i]);
		}
 	}
 	public void showLightLabel(int i){
 		//check if legal!!!
 		LightLabel p =getLabel(i);
		if (p!=null) {
			p.setVisible(true);
			shown[i]=true;
		}
 	}
 	public void hideLightLabel(int i){
 		LightLabel p =getLabel(i);
		if (p!=null) {
			shown[i]=false;
			p.setVisible(false);
		}
 	}
 	public void toggleShowLightLabel(LightLabel label){
 		for (int i=0; i<numNodes; i++) {
 			LightLabel p =getLabel(i);
 			if (p == label) {
				shown[i]=!shown[i];
				p.setVisible(shown[i]);
				return;
			}
			
		}
 	}
 	public void hideLightLabel(LightLabel label){
 		for (int i=0; i<numNodes; i++) {
 			LightLabel p =getLabel(i);
 			if (p == label) {
				shown[i]=false;
				p.setVisible(false);
				return;
			}
			
		}
 	}
 	public void showLightLabel(LightLabel label){
 		for (int i=0; i<numNodes; i++) {
 			LightLabel p =getLabel(i);
 			if (p == label) {
				shown[i]=true;
				p.setVisible(true);
				return;
			}
			
		}
 	}
	/*.................................................................................................................*/
	public   void paintLabels(Tree tree, int node, Graphics g) {
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				paintLabels(tree, d, g);
		LightLabel p = getLabel(node);
		if (p!=null) {
			p.paint(g); 
		}
		
	}
	/*.................................................................................................................*/
	private void recShowLightLabels(Tree tree, int N) {
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				recShowLightLabels(tree, d);
		inTree[N]=true;
	}
	/*.................................................................................................................*/
	public void showLightLabels(Tree tree, int drawnRoot) {
		for (int i=0; i<inTree.length; i++)
			inTree[i]=false;
		recShowLightLabels(tree, drawnRoot);
		int count =0;
		for (int i=0; i<inTree.length; i++) {
			if (!inTree[i] || !shown[i]) {
		 		LightLabel p =getLabel(i);
				if (p!=null && p.isVisible()) 
					p.setVisible(false);
			}
			else {
		 		LightLabel p =getLabel(i);
				if (p!=null && !p.isVisible()) 
					p.setVisible(true);
			}
		}
	}
	/*.................................................................................................................*/
	public   void locateLightLabels(Tree tree, int node) {
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				locateLightLabels(tree, d);
				
		int nodeX = (int)treeDisplay.getTreeDrawing().x[node];  //integer nodeloc approximation
		int nodeY = (int)treeDisplay.getTreeDrawing().y[node];  //integer nodeloc approximation
		LightLabel p = getLabel(node);
		if (p!=null){
			if  ((p.getLocation()==null) || ((p.getLocation().x!=nodeX) || (p.getLocation().y!=nodeY)))
				p.setLocation(nodeX, nodeY);  //only do if moved
		}
		
	}
	public void dispose() {
		for (int i=0; i<numNodes; i++) {
			labels[i] = null;
		}
	}
	public LightLabel getLabel(int N) {
		if (N<0 || N>= labels.length)
			return null;
		return labels[N];
	}
	public void setColor(Color c){
		if (c==null)
			return;
		for (int i=0; i<numNodes; i++){
			LightLabel p = getLabel(i);
			if (p !=null && p instanceof LightLabel){
				((LightLabel)p).setColor(c);
			}
		}
	}
	public int getNumNodes() {
		return numNodes;
	}
}

