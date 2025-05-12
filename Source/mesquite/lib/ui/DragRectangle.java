/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.ui;

import java.awt.*;

import mesquite.lib.MesquiteInteger;

/* ======================================================================== */
/**A class to indicate how something is to be colored, either an array of colors to be shown in equal pieces, or 
same with distribution of weights for pie slices.  A maximum number of 64 colors may be shown.*/
public class DragRectangle {
	int whichDown = MesquiteInteger.unassigned;
	int xDown = MesquiteInteger.unassigned;
	int yDown = MesquiteInteger.unassigned;
	int xDrag = MesquiteInteger.unassigned;
	int yDrag = MesquiteInteger.unassigned;
	Graphics g;
	Component component;
	
	public DragRectangle(Component component, Graphics g, int xDown, int yDown){
		this.g = g;
		this.xDown=xDown;
		this.yDown = yDown;
		xDrag = xDown;
		yDrag= yDown;
		this.component = component;
		drawRectangleDown();
	}
	public DragRectangle(){
	}
	public void drawFirstRectangle(Graphics g, int xDown, int yDown){
		this.g = g;
		this.xDown=xDown;
		this.yDown = yDown;
		xDrag = xDown;
		yDrag= yDown;
		drawRectangleDown();
	}
	/* ----------------------------------*/
	void drawRect(Graphics g, int x1, int y1, int x2, int y2){
		if (x1>x2){
			if (y1>y2)
				GraphicsUtil.drawXORRect(g, x2, y2, x1-x2, y1-y2);
			else 
				GraphicsUtil.drawXORRect(g, x2, y1, x1-x2, y2-y1);
		}
		else {
			if (y1>y2)
				GraphicsUtil.drawXORRect(g, x1, y2, x2-x1, y1-y2);
			else 
				GraphicsUtil.drawXORRect(g, x1, y1, x2-x1, y2-y1);
		}
	}
	/* ----------------------------------*/
	void undrawRect(Component c, Graphics g, int x1, int y1, int x2, int y2){
		if (x1>x2){
			if (y1>y2)
				GraphicsUtil.undrawXORRect(c, g, x2, y2, x1-x2, y1-y2);
			else 
				GraphicsUtil.undrawXORRect(c, g, x2, y1, x1-x2, y2-y1);
		}
		else {
			if (y1>y2)
				GraphicsUtil.undrawXORRect(c, g, x1, y2, x2-x1, y1-y2);
			else 
				GraphicsUtil.undrawXORRect(c, g, x1, y1, x2-x1, y2-y1);
		}
	}
	/* ----------------------------------*/
	public void drawRectangleDown(){
    		if (g!=null){
	   		g.setColor(Color.black);
			drawRect(g, xDown, yDown,  xDrag, yDrag);
		}
	}
	/* ----------------------------------*/
	public void drawRectangleUp(){
    		if (g!=null){
	   		g.setColor(Color.black);
			undrawRect(component, g, xDown, yDown,  xDrag, yDrag);
		}
	}
	/* ----------------------------------*/
	public void drawRectangleDrag(int xPixel, int yPixel){
    		if (g!=null){
	   		g.setColor(Color.black);
			undrawRect(component, g, xDown, yDown,  xDrag, yDrag);
			drawRect(g, xDown, yDown,  xPixel, yPixel);
			xDrag=xPixel;
			yDrag=yPixel;
		}
	}
	/* ----------------------------------*/
	public void dispose(){
    		if (g!=null){
	    		g.dispose();
		}
	}
}

