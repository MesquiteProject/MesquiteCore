/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
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
import java.util.*;
import java.awt.event.*;
import mesquite.lib.duties.*;
/* ======================================================================== */
/** A container fitting within the InterContentArea, below the information bar.  It is within the ContentArea that
modules draw their stuff.*/
public class ContentArea extends MousePanel {
	protected ToolPalette palette;
	protected MousePanel mainPanel;

	protected ContentSideContainer sidePanelContainer;
	Vector sidePanels;
	static int sideSpacer = 3;
	protected int sidePanelWidth = 0;

	protected ContentLedgeContainer ledgePanelContainer;
	Vector ledgePanels;
	static int ledgeSpacer = 3;
	protected int ledgePanelHeight = 0;

	public ContentArea (MesquiteWindow w) {
		this(w, false);
	}
	public ContentArea (MesquiteWindow w, boolean makePalette) {
		setLayout(null);
		if (makePalette){
			w.setPalette(palette = w.makeToolPalette());
			setPalette(palette); //setting palette into main graphics page
			w.addKeyListenerToAll(this, palette, true);
		}
		mainPanel = new MousePanel();
		mainPanel.setLayout(null);
		mainPanel.setBounds(0,0,getBounds().width, getBounds().height);
		//mainPanel.setBackground(Color.green);
		super.add(mainPanel);
		mainPanel.requestFocus(); //this may address a MRJ 2.2.3 bug

		sidePanels = new Vector();
		sidePanelContainer = new ContentSideContainer(this);
		super.add(sidePanelContainer);
		sidePanelContainer.setBackground(Color.lightGray);
		sidePanelContainer.setBounds(getBounds().width, 0,0,getBounds().height);
		sidePanelContainer.setVisible(true);

		ledgePanels = new Vector();
		ledgePanelContainer = new ContentLedgeContainer(this);
		super.add(ledgePanelContainer);
		ledgePanelContainer.setBackground(Color.lightGray);
		ledgePanelContainer.setBounds(0, getBounds().height,getBounds().width,0);
		ledgePanelContainer.setVisible(true);
	}
	public void setPalette(ToolPalette palette){
		if (palette== null) {
			super.remove(this.palette);
			this.palette = null;
			resetBounds();
		}
		else {
			this.palette = palette;
			super.add(palette);
			resetBounds();
			palette.setVisible(true);
			palette.repaint(); //attempt to deal with bug in OS X 10.2
		}
	}
	public ToolPalette getPalette(){
		return palette;
	}
	public void setCursor(Cursor c){
		for (int i = 0; i<sidePanels.size(); i++){
			Panel p = (Panel)sidePanels.elementAt(i);
			p.setCursor(c);
		}
		for (int i = 0; i<ledgePanels.size(); i++){
			Panel p = (Panel)ledgePanels.elementAt(i);
			p.setCursor(c);
		}
		super.setCursor(c);
	}
	public void addSidePanel(MousePanel p, int width){
		if (p!= null && sidePanels.indexOf(p)<0) {

			sidePanels.addElement(p);
			//p.setCursor(Cursor.getDefaultCursor());
			//this.sidePanel = p;
			sidePanelWidth += width+sideSpacer;
			p.ps = width;
			sidePanelContainer.add(p);
			p.setVisible(true);
			resetBounds();
			p.repaint();
		}
	}
	public boolean hasSidePanel(MousePanel p){
		return (p!= null && sidePanels.indexOf(p)>=0);
	}
	public void removeSidePanel(MousePanel p){
		if (p!= null && sidePanels.indexOf(p)>=0) {
			p.setVisible(false);
			sidePanelWidth -= p.ps + sideSpacer;
			sidePanelContainer.remove(p);
			sidePanels.removeElement(p);
			resetBounds();
		}
	}
	public int getSidePanelWidth(){
		return sidePanelWidth;
	}
	public void addLedgePanel(MousePanel p, int height){
		if (p!= null && ledgePanels.indexOf(p)<0) {
			ledgePanels.addElement(p);
			//p.setCursor(Cursor.getDefaultCursor());
			//this.sidePanel = p;
			ledgePanelHeight += height+ledgeSpacer;
			p.ps = height;
			ledgePanelContainer.add(p);
			p.setVisible(true);
			resetBounds();
			p.repaint();
		}
	}
	public void removeLedgePanel(MousePanel p){
		if (p!= null && ledgePanels.indexOf(p)>=0) {
			p.setVisible(false);
			ledgePanelHeight -= p.ps + ledgeSpacer;
			ledgePanelContainer.remove(p);
			ledgePanels.removeElement(p);
			resetBounds();
		}
	}
	public int getLedgePanelHeight(){
		return ledgePanelHeight;
	}

	public Panel getMainPanel(){
		return mainPanel;
	}
	void rFocus(){
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
		    public void run() {			
		    	mainPanel.requestFocusInWindow();
		    }
		});    
	}
	public Component add(Component comp, int i){
		if (comp==null)
			return null;
		Component c = mainPanel.add(comp,  i);
		rFocus();

		return c;
	}
	public void add(Component comp, Object s, int i){
		if (comp==null)
			return;
		mainPanel.add(comp, s, i);
		rFocus();
	}
	public void add(Component comp, Object s){
		if (comp==null)
			return;
		mainPanel.add(comp, s);
		rFocus();
	}
	public Component add(Component comp){
		if (comp==null)
			return null;
		Component c = mainPanel.add(comp);
		rFocus();
		return c;
	}
	public Component add(String name, Component comp){
		if (comp==null)
			return null;
		Component c = mainPanel.add(name, comp);
		rFocus();
		return c;
	}
	public void remove(Component comp){
		if (comp==null)
			return;
		mainPanel.remove(comp);
		rFocus();
	}

	public void update (Graphics g) {
		try {
			super.update(g);
		}
		catch (OutOfMemoryError e){
			MesquiteMessage.println("Sorry, insufficient memory.   See file memory.txt in the Mesquite_Folder.");
		}
	}
	public void setBounds(int x, int y, int w, int h){
		int paletteWidth = 0;
		if (palette!=null && palette.getNumTools()>0) {
			palette.setVisible(true);
			palette.setSize(palette.getWidth(),h);
			palette.recheckSize();
			palette.setSize(palette.getWidth(),h);
			paletteWidth = palette.getWidth();
			mainPanel.setBounds(paletteWidth, 0, w-paletteWidth-sidePanelWidth,h - ledgePanelHeight);
		}
		else {
			if (palette != null){
				palette.setSize(0,0);
				palette.setVisible(false);
			}
			mainPanel.setBounds(0,0,w-sidePanelWidth,h - ledgePanelHeight);
		}
		if (sidePanels.size()>0) {
			sidePanelContainer.setBounds(w- sidePanelWidth, 0, sidePanelWidth, h);
			int edge = sideSpacer;
			for (int i=0; i<sidePanels.size(); i++){
				MousePanel sidePanel = (MousePanel)sidePanels.elementAt(i);
				sidePanel.setBounds(edge ,0,sidePanel.ps,h );
				edge += sidePanel.ps +sideSpacer;
			}
		}
		else
			sidePanelContainer.setBounds(w, h, 0, 0);
		if (ledgePanels.size()>0) {
			ledgePanelContainer.setBounds(paletteWidth, h-ledgePanelHeight, w-paletteWidth, ledgePanelHeight);
			int edge = ledgeSpacer;
			for (int i=0; i<ledgePanels.size(); i++){
				MousePanel ledgePanel = (MousePanel)ledgePanels.elementAt(i);
				ledgePanel.setBounds(0, edge ,w-paletteWidth-sidePanelWidth, ledgePanel.ps);
				edge += ledgePanel.ps +ledgeSpacer;
			}
		}
		else
			ledgePanelContainer.setBounds(w, h, 0, 0);
		super.setBounds(x,y,w,h);
	}
	public void setSize(int w, int h){
		int paletteWidth = 0;
		if (palette!=null && palette.getNumTools()>0) {
			palette.setVisible(true);
			palette.setSize(palette.getWidth(),h);
			palette.recheckSize();
			palette.setSize(palette.getWidth(),h);
			paletteWidth = palette.getWidth();
			mainPanel.setSize(w-paletteWidth-sidePanelWidth,h - ledgePanelHeight);
		}
		else {
			if (palette != null){
				palette.setSize(0,0);
				palette.setVisible(false);
			}
			mainPanel.setSize(w-sidePanelWidth,h - ledgePanelHeight);
		}
		if (sidePanels.size()>0) {
			sidePanelContainer.setSize(sidePanelWidth, h);
			for (int i=0; i<sidePanels.size(); i++){
				MousePanel sidePanel = (MousePanel)sidePanels.elementAt(i);
				sidePanel.setSize(sidePanel.ps,h );
			}
		}
		else
			sidePanelContainer.setSize(0, 0);
		if (ledgePanels.size()>0) {
			ledgePanelContainer.setSize(w-paletteWidth, ledgePanelHeight);
			for (int i=0; i<ledgePanels.size(); i++){
				MousePanel ledgePanel = (MousePanel)ledgePanels.elementAt(i);
				ledgePanel.setSize(w-paletteWidth-sidePanelWidth, ledgePanel.ps);
			}
		}
		else
			ledgePanelContainer.setSize(0, 0);
		super.setSize(w,h);
	}
	void resetBounds(){
		if (getBounds() == null || mainPanel == null)
			return;
		setBounds(0,0,getBounds().width, getBounds().height);
		MesquiteWindow w = MesquiteWindow.windowOfItem(this);
		if (w != null)
			w.windowResized();
	}
}

/*=====================================*/
class ContentSideContainer extends MousePanel {
	int xTouched = -1;
	MousePanel panelFollowing = null;
	ContentArea parent;
	public ContentSideContainer(ContentArea parent){
		this.parent = parent;
		setLayout(null);
	}
	public void mouseEntered(int modifiers, int x, int y, MesquiteTool tool) {
		setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));  //set just edge to this so rest retains
	}
	public void mouseExited(int modifiers, int x, int y, MesquiteTool tool) {
		setCursor(null);
		MesquiteWindow w = MesquiteWindow.windowOfItem(this);
		if (w != null)
			w.resetCursor();  //set just edge to this so rest retains
	}
	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		xTouched = x;
		if (x>0) {
			GraphicsUtil.shimmerVerticalOn(null,this,0,getBounds().height,x);
		}
		int edge = parent.sideSpacer;
		for (int i=0; i<parent.sidePanels.size(); i++){
			MousePanel sidePanel = (MousePanel)parent.sidePanels.elementAt(i);
			if (x < edge) {
				panelFollowing =sidePanel;
				return;
			}
			edge += sidePanel.ps + parent.sideSpacer;
		}


	}
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		if (x>0)
			GraphicsUtil.shimmerVerticalOn(null,this,0,getBounds().height,x);
		if (panelFollowing != null) {
			int distance = x - xTouched;
			if (distance + 16 > panelFollowing.ps)
				distance = panelFollowing.ps - 16; //minimum
			panelFollowing.ps -= distance;
			parent.sidePanelWidth -= distance;
			parent.resetBounds();
		}
		xTouched = -1;
		panelFollowing = null;

	}
	public void mouseDrag(int modifiers, int x, int y, MesquiteTool tool) {
		if (x>0) {
			GraphicsUtil.shimmerVerticalOn(null,this,0,getBounds().height,x);
			GraphicsUtil.shimmerVerticalOn(null,this,0,getBounds().height,x);
		}
		else {
			GraphicsUtil.shimmerVerticalOn(null,parent,0,getBounds().height,parent.getBounds().width-getBounds().width+x);
			GraphicsUtil.shimmerVerticalOn(null,parent,0,getBounds().height,parent.getBounds().width-getBounds().width+x);
		}
	}
	public void paint(Graphics g){
		g.setColor(Color.darkGray);
		g.fillRect(0,0,2, getBounds().height);
	}
}

class ContentLedgeContainer extends MousePanel {
	int yTouched = -1;
	MousePanel panelFollowing = null;
	ContentArea parent;
	public ContentLedgeContainer(ContentArea parent){
		this.parent = parent;
		setLayout(null);
	}
	public void mouseEntered(int modifiers, int x, int y, MesquiteTool tool) {
		setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));  //set just edge to this so rest retains
	}
	public void mouseExited(int modifiers, int x, int y, MesquiteTool tool) {
		setCursor(null);
		MesquiteWindow w = MesquiteWindow.windowOfItem(this);
		if (w != null)
			w.resetCursor();  //set just edge to this so rest retains
	}
	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		yTouched = y;
		int edge = parent.ledgeSpacer;
		for (int i=0; i<parent.ledgePanels.size(); i++){
			MousePanel ledgePanel = (MousePanel)parent.ledgePanels.elementAt(i);
			if (y < edge) {
				panelFollowing =ledgePanel;
				return;
			}
			edge += ledgePanel.ps + parent.ledgeSpacer;
		}


	}
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		if (panelFollowing != null) {
			int distance = y - yTouched;
			if (distance + 16 > panelFollowing.ps)
				distance = panelFollowing.ps - 16; //minimum
			panelFollowing.ps -= distance;
			parent.ledgePanelHeight -= distance;
			parent.resetBounds();
		}
		yTouched = -1;
		panelFollowing = null;

	}
	public void mouseDrag(int modifiers, int x, int y, MesquiteTool tool) {
	}
	public void paint(Graphics g){
		g.setColor(Color.darkGray);
		g.fillRect(0,0,getBounds().width, 2);
	}
}
