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
import java.awt.event.*;



/* ======================================================================== */
public abstract class ResizableLegend extends Legend {
	boolean sizing = false;
	protected int sizeOffsetX = 0;
	protected int sizeOffsetY=0;
	protected	int legendWidth;
	protected	int legendHeight;
	
	public ResizableLegend (int defaultWidth, int defaultHeight) {
		super(defaultWidth, defaultHeight);
		legendWidth = defaultWidth;
		legendHeight = defaultHeight;
	}
	/*.................................................................................................................*/
	public void legendResized(int widthChange, int heightChange){
	}
	
	public void setSize(int w, int h){
		legendWidth = w;
		legendHeight = h;
		super.setSize(w, h);
	}
	public void setBounds(int x, int y, int w, int h){
		legendWidth = w;
		legendHeight = h;
		super.setBounds(x, y, w, h);
	}
	public int getLegendWidth(){
		return legendWidth;
	}
	public int getLegendHeight(){
		return legendHeight;
	}
	public void mouseDrag (int modifiers, int x, int y, MesquiteTool tool) {
	   	if (MesquiteWindow.checkDoomed(this))
	   		return;
		if (sizing && GraphicsUtil.useXORMode(null, false)) {
			Graphics g=getParent().getGraphics();
	    	if (g!=null){

		    		g.setXORMode(Color.white);
		    		g.setClip(null);
		   		g.setColor(Color.black);
				g.drawRect(getBounds().x, getBounds().y, getBounds().width-1 + sizeOffsetX, getBounds().height-1 + sizeOffsetY);
				sizeOffsetX=x-origTouchX;
				sizeOffsetY=y-origTouchY;
				g.drawRect(getBounds().x, getBounds().y, getBounds().width-1 + sizeOffsetX, getBounds().height-1 + sizeOffsetY);
				g.dispose();
			}
		}
		else
			super.mouseDrag(modifiers, x, y, tool);
		MesquiteWindow.uncheckDoomed(this);
	}
	public void mouseDown(int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
	   	if (MesquiteWindow.checkDoomed(this))
	   		return;
		if (MesquiteEvent.controlKeyDown(modifiers)){
			super.mouseDown(modifiers, clickCount, when, x, y, tool);
		}
		else if (y> getBounds().height-8 && x > getBounds().width - 8) {
			sizing = true;
			origTouchX=x;
			origTouchY=y;
			sizeOffsetX=0;
			sizeOffsetY=0;
			if (GraphicsUtil.useXORMode(null, false)){
				Graphics g=getParent().getGraphics();
	    	if (g!=null){
	    		g.setClip(null);
	    		g.setXORMode(Color.white);
		   		g.setColor(Color.black);
				g.drawRect(getBounds().x, getBounds().y, getBounds().width-1, getBounds().height-1);
				g.dispose();
			}
			}
		}
		else {
			super.mouseDown(modifiers, clickCount, when, x, y, tool);
		}
		MesquiteWindow.uncheckDoomed(this);
	}
	public void mouseUp (int modifiers, int x, int y, MesquiteTool tool) {
	   	if (MesquiteWindow.checkDoomed(this))
	   		return;
		if (sizing && GraphicsUtil.useXORMode(null, false)) {
			Graphics g=getParent().getGraphics();
	    	if (g!=null){
		    	g.setXORMode(Color.white);
		   		g.setColor(Color.black);
				g.drawRect(getBounds().x, getBounds().y, getBounds().width-1 + sizeOffsetX, getBounds().height-1 + sizeOffsetY);
				g.dispose();
				legendWidth = getBounds().width + sizeOffsetX;
				legendHeight = getBounds().height + sizeOffsetY;
				if (legendWidth<16)
					legendWidth = 16;
				if (legendHeight<16)
					legendHeight = 16;
					
				legendResized(sizeOffsetX, sizeOffsetY);
				setSize(legendWidth, legendHeight);
				sizing = false;
			}
		}
		else
			super.mouseUp(modifiers, x, y, tool);

		MesquiteWindow.uncheckDoomed(this);
	}
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets the bounds of the panel, in pixels", "[x (left bound)] [y (upper bound)] [width] [height]", commandName, "setBounds")) {
    	 		MesquiteInteger pos = new MesquiteInteger(0);
    	 		int xA = MesquiteInteger.fromString(arguments, pos);
    	 		int yA = MesquiteInteger.fromString(arguments, pos);
    	 		int wA =  MesquiteInteger.fromString(arguments, pos);
    	 		int hA  = MesquiteInteger.fromString(arguments, pos);
    	 		int currentW = getBounds().width;
    	 		int currentH = getBounds().height;
    	 		setBounds(xA, yA, wA, hA);
    	 		legendResized(wA - currentW, hA - currentH);
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
    	 	return null;
   	 }
}

