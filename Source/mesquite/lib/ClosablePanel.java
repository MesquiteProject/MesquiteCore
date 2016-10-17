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


/* ======================================================================== */
public class ClosablePanel extends MousePanel {
	public static final int MINHEIGHT = 20;
	protected int height = MINHEIGHT;
	protected int currentHeight = 60;
	private boolean open = false;
	private boolean wholeOpen = false;
	private boolean showTriangle = false;
	protected ClosablePanelContainer container;
	protected String title = "";
	protected Color bgTopColor = Color.white;
	protected Color bgBottomColor = ColorDistribution.veryVeryVeryLightGray;
	protected Color barColor = ColorDistribution.veryLightGray;
	protected Color textColor = Color.darkGray;
	protected boolean hover = false;  //if true, then mouse is hovering over it
	/*
	 * 	Color bgTopColor = Color.lightGray;
	Color bgBottomColor = ColorDistribution.veryVeryLightGray;
	Color barColor = Color.darkGray;
	Color textColor = Color.black;
	 */

	public ClosablePanel(ClosablePanelContainer container, String title){
		super();
		this.container = container;
		this.title = title;
		if (getBold())
			setFont(new Font("SansSerif", Font.BOLD, getFontSize()));
		else
			setFont(new Font("SansSerif", Font.PLAIN, getFontSize()));
		setBackground(bgBottomColor);
	}
	public void setContainer(ClosablePanelContainer container){
		this.container = container;
	}
	protected int getFontSize(){
		return 10;
	}
	public boolean getBold(){
		return false;
	}
	public void setTitle(String title){
		if (title == null)
			title = "";
		this.title = title;
	}
	public String getTitle(){
		return title;
	}
	public void setWholeOpen(boolean b){
		wholeOpen = b;
	}
	public void setOpen(boolean b){
		open = b;
		if (container != null)
			container.requestHeightChange(this);
	}
	public boolean isOpen(){
		return open;
	}
	public void setShowTriangle(boolean b){
		showTriangle = b;
	}
	public boolean getShowTriangle(){
		return showTriangle;
	}
	public void setColors(Color bgTopColor, Color bgBottomColor, Color barColor, Color textColor){
		this.bgTopColor = bgTopColor;
		this.bgBottomColor = bgBottomColor;
		setBackground(bgBottomColor);
		this.barColor = barColor;
		this.textColor = textColor;
	}
	public Color getBgTopColor(){
		return bgTopColor;
	}
	public Color getBgBottomColor(){
		return bgBottomColor;
	}
	public Color getBarColor(){
		return barColor;
	}
	public Color getTextColor(){
		return textColor;
	}
	public void setBgTopColor(Color c){
		bgTopColor= c;
	}
	public void setBgBottomColor(Color c){
		bgBottomColor= c;
	}
	public void setBarColor(Color c){
		barColor= c;
	}
	public void setTextColor(Color c){
		textColor= c;
	}
	int tightness = 8;
	public void setTightness(int tightness){
		this.tightness = tightness;
	}
	public Image getIcon(){ //for small 16 pixel icon at left of main bar
		return null;
	}
	protected int triangleOffset = 0;
	public void paint(Graphics g){
		g.setColor(bgTopColor);
		g.fillRect(0,0,getWidth(), MINHEIGHT);
		g.setColor(barColor);
		g.fillRect(0,0,getWidth(), 4);
		triangleOffset = 0;
		Image im = getIcon();
		if (im != null){
			triangleOffset = 16;
			g.drawImage(im, 0, 4, this);
		}
		if (showTriangle){
			if (open)
				g.drawImage(InfoBar.triangleImageDown, 0 + triangleOffset, 0, this);
			else
				g.drawImage(InfoBar.triangleImage, -4 + triangleOffset, 2, this);
			triangleOffset += 10;
		}
		g.setColor(textColor);
		if (getTitle() != null){
			g.drawString(getTitle(), tightness + triangleOffset, MINHEIGHT-4);
		}
		if (hover){
			g.setColor(Color.lightGray);
			g.drawRect(0, 4, getWidth()-2, MINHEIGHT-6);
		}

	}
	/* to be used by subclasses to tell that panel touched */
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		ClosablePanel prev = container.getPrecedingPanel(this);
		if (prev != null && prev.userExpandable() && prev.getTouchExpand() != MesquiteInteger.unassigned){
			prev.incrementExpectedCurrentHeight(y-prev.getTouchExpand());
			prev.setTouchExpand(MesquiteInteger.unassigned);
			container.requestHeightChange(prev);
		}
		else if (showTriangle && (wholeOpen || (x< triangleOffset + 8 && x >= triangleOffset - 16)) && y<MINHEIGHT){
			setOpen(!open);
			repaint();
		}
	}
	int touchExpand = MesquiteInteger.unassigned;
	public int getTouchExpand(){
		return touchExpand;
	}
	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		super.mouseDown(modifiers, clickCount, when, x, y, tool);

		ClosablePanel prev = container.getPrecedingPanel(this);
		if (prev != null && prev.userExpandable()){
			if (y < 4){
				prev.setTouchExpand( y);
				return;
			}
			else 
				prev.setTouchExpand(MesquiteInteger.unassigned);

		}
	}
	Cursor rC = new Cursor(Cursor.S_RESIZE_CURSOR);
	Cursor dC = new Cursor(Cursor.DEFAULT_CURSOR);
	public void mouseEntered(int modifiers, int x, int y, MesquiteTool tool) {
		super.mouseEntered(modifiers, x, y, tool);
		hover = true;
		ClosablePanel prev = container.getPrecedingPanel(this);
		if (prev != null && prev.userExpandable()){
			if (y < 4){
				setCursor(rC);
			}
			else 
				setCursor(dC);

		}
	}
	public void mouseMoved(int modifiers, int x, int y, MesquiteTool tool) {
		super.mouseMoved(modifiers, x, y, tool);
		hover = true;
		ClosablePanel prev = container.getPrecedingPanel(this);
		if (prev != null && prev.userExpandable()){
			if (y < 4){
				setCursor(rC);
			}
			else 
				setCursor(dC);

		}
	}
	public void mouseExited(int modifiers, int x, int y, MesquiteTool tool) {
		super.mouseExited(modifiers, x, y, tool);
		hover = false;
		ClosablePanel prev = container.getPrecedingPanel(this);
		if (prev != null && prev.userExpandable()){

			setCursor(dC);

		}
	}
	public void setTouchExpand(int c){
		touchExpand = c;
	}
	public boolean userExpandable(){
		return false;
	}
	public int getExpectedCurrentHeight(){
		return currentHeight;
	}
	public void incrementExpectedCurrentHeight(int c){
		currentHeight += c;
		if (currentHeight< MINHEIGHT)
			currentHeight = MINHEIGHT;
	}
	public int getRequestedHeight(int width){
		if (open)
			return currentHeight;
		else
			return MINHEIGHT;
	}
}

