/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.minimal.DrawHierarchy;
/*~~  */

import java.awt.*;
import java.net.*;
import java.util.*;
import java.io.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/** Draws the trees of HNode's in various places (e.g., module tree in Module tab of windows; Project element tree in 
Projects & Files window). */

public class DrawHierarchy extends BrowseHierarchy  {
	/*.................................................................................................................*/
	 public String getName() {
	return "DrawHierarchy";
	 }
		/*.................................................................................................................*/
	 public String getExplanation() {
	return "Draws project and module trees";
	 }
	 
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;
 	}
 	public HPanel makeHierarchyPanel(){
 		return new HierarchyPanel(this);
 	}
}

/* ======================================================================== */
class HierarchyPanel extends HPanel {
	FieldPanel ePane;
	public HierarchyPanel (MesquiteModule ownerModule) {
		//setFont(new Font ("SanSerif", Font.PLAIN, 10));
		ePane = new FieldPanel(ownerModule, this);
		addImpl(ePane, null, 0);
		ePane.setVisible(true);
	}
	public void renew() {
		ePane.renew();
		setScrollPosition(0,0);
	}
	public void dispose() {
		ePane.dispose();
	}
	public void disposeReferences() {
		if (ePane !=null)
			ePane.disposeReferences();
	}
	public void setRootNode(HNode node) {
		ePane.setRootNode(node);
	}
	public void highlightNode(HNode node) {
		ePane.highlightNode(node);
	}
	public void setTitle(String title) {
		ePane.setTitle(title);
	}
	public void setBackground(Color color){
		ePane.setBackground(color);
	}
	public void setDefaultDepth(int depth){
		ePane.setDefaultDepth(depth);
	}
	public void showTypes(boolean s){
		ePane.setShowTypes(s);
	}
}

/* ======================================================================== */
class FieldPanel extends Panel {
	final static int HSpacer = 24;
	int VSpacer = 22; 
	HierarchyPanel pane;
	int maximumRight, maximumDown;
	NodeLabel rootLabel=null;
	HNode  rootNode;
	TextArea titleText;
	public boolean showTypes;
	String title;
	int defaultDepth = -1;
	int initialY = 22;
	MesquiteModule ownerModule;
	HNode highlighted=null;
	public FieldPanel (MesquiteModule ownerModule, HierarchyPanel pane) {
		super();
		this.ownerModule = ownerModule;
		this.pane = pane;
		setLayout(null);
		setSize(500, 500);
		setBackground(Color.yellow);
		pane.repaint();
		repaint();
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public void setRootNode(HNode node) {
		rootNode = node;
		renew();
	}
	public void setDefaultDepth(int depth){
		defaultDepth = depth;
	}
	public void setShowTypes(boolean s){
		showTypes = s;
	}
	public void highlightNode(HNode node){
		highlighted = node;
	}
	public void renew() {
		if (rootLabel!= null)
			disposeLabels(rootLabel);
		if (rootNode == null)
			return;
		maximumDown = MesquiteInteger.unassigned; //forces recalculation of size
		maximumRight = MesquiteInteger.unassigned;
		MesquiteInteger yplace = new MesquiteInteger(0);
		rootLabel = new NodeLabel(ownerModule, this, rootNode, null);
		addClade(rootNode, rootLabel, 1);
		if (title == null)
			yplace.setValue(-12);
		else
			yplace.setValue(initialY-12);
		//resetHighlights(rootLabel);
		setLabelLocations(rootLabel, yplace, 0, 12);
	}
	public void dispose() {
		disposeLabels(rootLabel);
		rootLabel = null;
		rootNode=null;
		highlighted = null;
		ownerModule = null;
	}
 	public void disposeReferences() {
		disposeRefsRec(rootLabel);
	}
	public void disposeRefsRec(NodeLabel label) {
		try {
			HNode[] daughters = label.getHDaughters();
			if (daughters!=null) {
				for (int i=0; i<daughters.length; i++) {
					disposeRefsRec((NodeLabel)daughters[i]);
		 		}
	 		}
	 		label.node = null;
	 		label.motherNode = null;
 		}
 		catch (NullPointerException e){
 			//these just tend to happen!
 		}
 	 }
	public void disposeLabels(NodeLabel label) {
		try {
			if (label.getHShow()){
				HNode[] daughters = label.getHDaughters();
				if (daughters!=null) {
					for (int i=0; i<daughters.length; i++) {
						disposeLabels((NodeLabel)daughters[i]);
			 		}
		 		}
		 		if (label.supplements!=null) {
			 		for (int i=0; i<label.supplements.length; i++) {
			 			label.supplements[i].setVisible(false);
						remove(label.supplements[i]);
			  			label.supplements[i] = null;
			  		}
		 		}
	 		}
	 		label.setVisible(false);
	 		label.dispose();
			remove(label);
	  		label = null;
 		}
 		catch (NullPointerException e){
 			//these just tend to happen!
 		}
 	 }
 	public void addClade(HNode node, NodeLabel label, int depth) {
		try {
			add(label);
			label.setVisible(true);
			if (label.supplements!=null) {
				for (int i=0;i<label.supplements.length; i++){
					add(label.supplements[i]);
					label.supplements[i].setVisible(true);
				}
			}
			HNode[] daughters = node.getHDaughters();
			if (daughters!=null && (defaultDepth<0 || depth<=defaultDepth)) {
				for (int i=0; i<daughters.length; i++) {
					if (daughters[i].getHShow()){
						NodeLabel eL = label.setDaughter(daughters[i]);
						addClade(daughters[i], eL, depth+1);
					}
		 		}
	 		}
 		}
 		catch (NullPointerException e){
 			MesquiteMessage.warnProgrammer("NPE in addClade in DrawHierarchy " + node);
 			//these just tend to happen!
 		}
 		// don't need to trim indent since not returned by reference
   	 }
 	/*public void resetHighlights(NodeLabel label) {
 		if (label.getNode() == highlighted)
 			label.setColor(Color.yellow);
 		else
 			label.setColor(ColorDistribution.straw);
		HNode[] daughters = label.getHDaughters();
		if (daughters!=null) {
			for (int i=0; i<daughters.length; i++) {
				if (daughters[i].getHShow()){
					NodeLabel eL = (NodeLabel)daughters[i];
					resetHighlights(eL);
				}
	 		}
 		}
 		// don't need to trim indent since not returned by reference
   	 }
   	 */
 	public boolean checkLabelLocations(NodeLabel label) {
 		try {
	 		if (label == null)
	 			return false;
			Point d = label.getLocation();
			if (d.y == 0)
				return true;
			HNode[] daughters = label.getHDaughters();
			if (daughters!=null) {
				for (int i=0; i<daughters.length; i++) {
					if (daughters[i]!=null && daughters[i].getHShow()){
						NodeLabel eL = (NodeLabel)daughters[i];
						if (checkLabelLocations(eL))
							return true;
					}
		 		}
	 		}
 		}
 		catch (NullPointerException e){
 			//these just tend to happen!
 		}
 		// don't need to trim indent since not returned by reference
 		return false;
   	 }
 	public boolean checkSupplementLocations(NodeLabel label) {
 		try {
	 		if (label == null)
	 			return false;
			if (label.supplements!=null) {
				int prevX = label.getLocation().x + label.getBounds().width;
				for (int i=0;i<label.supplements.length; i++){
					Point d = label.supplements[i].getLocation();
					if (d.x != prevX + 10) {
						return true;
					}
					prevX = label.supplements[i].getLocation().x + label.supplements[i].getBounds().width;
				}
			}
			HNode[] daughters = label.getHDaughters();
			if (daughters!=null) {
				for (int i=0; i<daughters.length; i++) {
					if (daughters[i]!=null && daughters[i].getHShow()){
						NodeLabel eL = (NodeLabel)daughters[i];
						if (checkSupplementLocations(eL))
							return true;
					}
		 		}
	 		}
 		}
 		catch (NullPointerException e){
 			//these just tend to happen!
 		}
 		// don't need to trim indent since not returned by reference
 		return false;
   	 }
 	private void setLabelLocations(NodeLabel label, MesquiteInteger y, int employerY, int indent) {
 		try {
	 		if (y==null || label == null)
	 			return;
	 		y.setValue( y.getValue()+VSpacer);
			if ((indent + label.getBounds().width)> maximumRight)
				maximumRight = (indent + label.getBounds().width);
			if (y.getValue()> maximumDown)
				maximumDown = y.getValue();
	 		int myY = y.getValue() + VSpacer/2-4;
			label.setLocation(indent, y.getValue());
			
			HNode[] daughters = label.getHDaughters();
			if (daughters!=null) {
				for (int i=0; i<daughters.length; i++) {
					if (daughters[i]!=null && daughters[i].getHShow()){
						NodeLabel eL = (NodeLabel)daughters[i];
						setLabelLocations(eL, y, myY, indent+HSpacer);
					}
		 		}
	 		}
 		}
 		catch (NullPointerException e){
 			//these just tend to happen!
 		}
 		// don't need to trim indent since not returned by reference
   	 }
 	private void setSupplementLocations(NodeLabel label) {
 		try {
	 		if (label == null)
	 			return;
			if (label.supplements!=null) {
				int prevX = label.getLocation().x + label.getBounds().width;
				for (int i=0; i<label.supplements.length; i++) {
					if (label.supplements[i]!=null){
						label.supplements[i].setLocation(prevX + 10, label.getLocation().y);
						prevX = label.supplements[i].getLocation().x + label.supplements[i].getBounds().width;
					}
				}
			}
			
			HNode[] daughters = label.getHDaughters();
			if (daughters!=null) {
				for (int i=0; i<daughters.length; i++) {
					if (daughters[i]!=null && daughters[i].getHShow()){
						NodeLabel eL = (NodeLabel)daughters[i];
						setSupplementLocations(eL);
					}
		 		}
	 		}
 		}
 		catch (NullPointerException e){
 			//these just tend to happen!
 		}
 		// don't need to trim indent since not returned by reference
   	 }
 	public void drawTree(NodeLabel label, Graphics g, MesquiteInteger y, int employerY, int indent) {
 		try {
	 		if (y==null || label == null)
	 			return;
	 		y.setValue( y.getValue()+VSpacer);
			if ((indent + label.getBounds().width)> maximumRight)
				maximumRight = (indent + label.getBounds().width);
			if (y.getValue()> maximumDown)
				maximumDown = y.getValue();
	 		int myY = y.getValue() + VSpacer/2-4;
	 		g.setColor(Color.blue);
			if (label != rootLabel) {
				g.drawLine(indent- HSpacer/2, myY, indent-HSpacer/2, employerY);
				g.drawLine(indent- HSpacer/2+1, myY, indent-HSpacer/2+1, employerY);
				g.drawLine(indent- HSpacer/2, myY, indent,myY);
				g.drawLine(indent- HSpacer/2, myY+1, indent,myY+1);
			}
	 		g.setColor(Color.black);
			
			HNode[] daughters = label.getHDaughters();
			if (daughters!=null) {
				for (int i=0; i<daughters.length; i++) {
					if (daughters[i]!=null && daughters[i].getHShow()){
						NodeLabel eL = (NodeLabel)daughters[i];
						drawTree(eL, g, y, myY, indent+HSpacer);
					}
		 		}
	 		}
	 		if (label.getHImage()!=null)
	 			g.drawImage(label.getHImage(), indent- HSpacer/2 + label.getBounds().width + 16, myY-5, this);
 		}
 		catch (NullPointerException e){
 			//these just tend to happen!
 		}
 		// don't need to trim indent since not returned by reference
   	 }
	public void paint(Graphics g) {
	   	if (MesquiteWindow.checkDoomed(this))
	   		return;
	   	if (rootNode != null) {
	   		
			FontMetrics fm = g.getFontMetrics();
			VSpacer = fm.getAscent() + fm.getDescent() + 9;
			if (title != null)
				g.drawString(title, 10, VSpacer);
			MesquiteInteger yplace = new MesquiteInteger(0);
			int oldMaximumRight = maximumRight;
			maximumRight = pane.getBounds().width;
			int oldMaximumDown = maximumDown;
			maximumDown = pane.getBounds().height;
			if (title == null)
				yplace.setValue(-12);
			else
				yplace.setValue(initialY-12);
			//resetHighlights(rootLabel);
			drawTree(rootLabel, g, yplace, 0, 12);
			if (maximumDown!=oldMaximumDown || maximumRight != oldMaximumRight) {
				setSize(maximumRight + 50, maximumDown + 50);
				pane.setScrollPosition(0,0);
				if (title == null)
					yplace.setValue(-12);
				else
					yplace.setValue(initialY-12);
				setLabelLocations(rootLabel, yplace, 0, 12);
				setSupplementLocations(rootLabel);
				pane.doLayout();
				pane.repaint();
				repaint();
			}
			if (checkLabelLocations(rootLabel)){
				if (title == null)
					yplace.setValue(-12);
				else
					yplace.setValue(initialY-12);
				setLabelLocations(rootLabel, yplace, 0, 12);
				pane.repaint();
				repaint();
			}
			if (checkSupplementLocations(rootLabel))
				setSupplementLocations(rootLabel);
		}
		MesquiteWindow.uncheckDoomed(this);
	}
	/*public void setSize(int width, int height){
		MesquiteInteger yplace = new MesquiteInteger(0);
		int oldMaximumRight = maximumRight;
		maximumRight = pane.getBounds().width;
		int oldMaximumDown = maximumDown;
		maximumDown = pane.getBounds().height;
		yplace.setValue(initialY);
		resetHighlights(rootLabel);
		drawTree(rootLabel, g, yplace, 0, 12);
		if (maximumDown!=oldMaximumDown || maximumRight != oldMaximumRight) {
			setSize(maximumRight + 50, maximumDown + 50);
			pane.setScrollPosition(0,0);
			pane.doLayout();
			pane.repaint();
		}
		super.setSize(width, height);
	}*/
}
/* ======================================================================== */
class NodeLabel extends MesquiteLabel implements HNode {
	HNode node;
	public Vector daughterVector;
	HNode motherNode;
	public SupplementLabel[] supplements;
	FieldPanel panel;
	
	public NodeLabel(MesquiteModule ownerModule, FieldPanel panel, HNode node, HNode motherNode){
		super(ownerModule);
		setCheckDoomed(false);
		if (node !=null){
			setBackground(node.getHColor());
			setColor(node.getHColor());
		}
		setSize(10,10);
		daughterVector = new Vector(1);
		this.node = node;
		this.panel = panel;
		this.motherNode = motherNode;
		if (node !=null){
			int numSupp = node.getNumSupplements();
			
			if (numSupp>0) {
				supplements = new SupplementLabel[numSupp];
				for (int i = 0; i<numSupp; i++)
					supplements[i] = new SupplementLabel(ownerModule, node, i);
			}
			if (panel.showTypes && !StringUtil.blank(node.getTypeName()))
				setText(node.getTypeName() + ": " + node.getName());
			else
				setText(node.getName());
		}
	}
	
	public NodeLabel setDaughter(HNode node) {
		NodeLabel eL = new NodeLabel(MesquiteTrunk.mesquiteTrunk, panel, node, motherNode);
		if (daughterVector!=null)
			daughterVector.addElement(eL);
		return eL;
	}
  	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		if (node!=null)
			node.hNodeAction(this, x, y, HNode.MOUSEDOWN);   
	}
   	public void mouseMoved(int modifiers, int x, int y, MesquiteTool tool) {
		if (node!=null)
			node.hNodeAction(this, x, y, HNode.MOUSEMOVE);   
	}
   	public void mouseEntered(int modifiers, int x, int y, MesquiteTool tool) {
		if (node!=null)
			node.hNodeAction(this, x, y, HNode.MOUSEMOVE);   
	}
   	public void mouseExited(int modifiers, int x, int y, MesquiteTool tool) {
		if (node!=null)
			node.hNodeAction(this, x, y, HNode.MOUSEEXIT);   
	}
	/*.................................................................................................................*/
	public void zapHDaughters(){
		if (daughterVector == null || daughterVector.size() == 0)
			return;
		daughterVector.removeAllElements();
		node = null;
	}
	public HNode[] getHDaughters(){
		if (daughterVector == null || daughterVector.size() == 0)
			return null;
		int num = daughterVector.size();
		HNode[] daughters = new HNode[num];
		try {
			for (int i= 0; i<num && i<daughters.length && daughterVector!=null && i<daughterVector.size(); i++)
				daughters[i] = (HNode)daughterVector.elementAt(i);
		}
		catch (ArrayIndexOutOfBoundsException e){
		}
		catch (NullPointerException e){
		}
		return daughters;
	}
	public void dispose(){
		daughterVector.removeAllElements();
		daughterVector = null;
		super.dispose();
	}
	/*.................................................................................................................*/
	public HNode getHNode(){
		return node;
	}
	/*.................................................................................................................*/
	public HNode getHMother(){
		return motherNode;
	}
	public int getNumSupplements(){
		return node.getNumSupplements();
	}
	public String getSupplementName(int index){
		return node.getSupplementName(index);
	}
	public void hNodeAction(Container c, int x, int y, int action){
	}
	public void hSupplementTouched(int index){}
	public String getTypeName(){
		return "Label for node in hierarchy";
	}
	public Image getHImage(){
		return node.getHImage();
	}
	public Color getHColor(){
		return node.getHColor();
	}
	public boolean getHShow(){
		return node.getHShow();
	}
}
/* ======================================================================== */
class SupplementLabel extends MesquiteLabel {
	HNode node;
	int which;
	public SupplementLabel(MesquiteModule ownerModule, HNode node, int which){
		super(ownerModule);
		setSize(10,10);
		this.node = node;
		this.which = which;
		setText(node.getSupplementName(which)); //TODO: not just 0 in future
		setColor(Color.white);
		setVisible(true);
		repaint();
	}
	
  	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		node.hSupplementTouched(which);   
	}
}

