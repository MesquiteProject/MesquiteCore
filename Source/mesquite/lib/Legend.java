/* Mesquite source code.  Copyright 1997-2007 W. Maddison and D. Maddison.Version 2.0, September 2007.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html) */package mesquite.lib;import java.awt.*;import java.awt.event.*;import java.awt.image.*;/* ======================================================================== *//** Legends for TreeDisplays and Charts, e.g. the TraceCharacter Legend. */public abstract class Legend extends MesquitePanel implements Commandable {	protected int offsetX = 0;	protected int offsetY=0;	protected int origTouchX, origTouchY, dragOffsetX, dragOffsetY;	protected int defaultWidth, defaultHeight;	protected Container constrainingContainer = null;	public static long totalCreated = 0;	MesquiteInteger pos = new MesquiteInteger();	public Legend (int defaultWidth, int defaultHeight) {		super();		setMoveFrequency(4);		this.defaultWidth = defaultWidth;		this.defaultHeight = defaultHeight;		totalCreated++;		setCursor(Cursor.getDefaultCursor());		defaultOffsets();	}	public void setConstrainingContainer(Container c){		constrainingContainer = c;	}	public int getOffsetX() {		return offsetX;	}	public int getOffsetY() {		return offsetY;	}	public void setOffsetX(int newOffsetX) {		if (newOffsetX != MesquiteInteger.unassigned)			offsetX=newOffsetX;	}	public void setOffsetY(int newOffsetY) {		if (newOffsetY != MesquiteInteger.unassigned)			offsetY=newOffsetY;	}	/*.................................................................................................................*/	public Snapshot getSnapshot(MesquiteFile file) {		Snapshot temp = super.getSnapshot(file);		temp.addLine("setOffsetX " +  offsetX);		temp.addLine("setOffsetY " +  offsetY);		return temp;	}	/*.................................................................................................................*/	public Object doCommand(String commandName, String arguments, CommandChecker checker) {		if (checker.compare(this.getClass(), "Sets the x offset of the legend from home position", "[x offset in pixels]", commandName, "setOffsetX")) {			int offset= MesquiteInteger.fromFirstToken(arguments, pos);			if (MesquiteInteger.isCombinable(offset)) {				setOffsetX(offset);			}		}		else if (checker.compare(this.getClass(), "Sets the y offset of the legend from home position", "[y offset in pixels]", commandName, "setOffsetY")) {			int offset= MesquiteInteger.fromFirstToken(arguments, pos);			if (MesquiteInteger.isCombinable(offset)) {				setOffsetY(offset);			}		}		else 			return  super.doCommand(commandName, arguments, checker);		return null;	}	/*.................................................................................................................*/	int buffer = 4;	public void defaultOffsets() {		setOffsetX(buffer);		setOffsetY(buffer);		//setOffsetX(-(defaultWidth+buffer));		//setOffsetY(-(defaultHeight+buffer));	}	public void adjustLocation() {		int legendX, legendY;		int baseX;		int baseY;		int conWidth;		int conHeight;		if (constrainingContainer ==null) {			conWidth = getParent().getBounds().width;			conHeight = getParent().getBounds().height;		}		else {			conWidth = constrainingContainer.getBounds().width;			conHeight = constrainingContainer.getBounds().height;		}		baseX = 0; //getParent().getBounds().width;		baseY =0; // getParent().getBounds().height;		legendX = baseX+getOffsetX();		legendY = baseY+getOffsetY();		if (legendX > (conWidth + buffer)) {			setOffsetX(conWidth-baseX-getBounds().width-buffer);			legendX = baseX+getOffsetX();		}		if (legendX<0) {			setOffsetX(-baseX+4);			legendX = baseX+getOffsetX();		}		if (legendY>conHeight + buffer) {			setOffsetY(conHeight-baseY-getBounds().height-buffer);			legendY = baseY+getOffsetY();		}		if (legendY<0) {			setOffsetY(-baseY+4);			legendY = baseY+getOffsetY();		}		if ((legendX!=getBounds().x) || (legendY!=getBounds().y)) {			setLocation(legendX, legendY);			repaint();		}	}	public void mouseDrag (int modifiers, int x, int y, MesquiteTool tool) {		if (MesquiteWindow.checkDoomed(this))			return;		if (GraphicsUtil.useXORMode(null, false)){			Graphics g=getParent().getGraphics();			if (g!=null){				g.setXORMode(Color.white);				g.setColor(Color.black);				g.drawRect(getBounds().x + dragOffsetX, getBounds().y + dragOffsetY, getBounds().width-1,  getBounds().height-1);				dragOffsetX=x-origTouchX;				dragOffsetY=y-origTouchY;				g.drawRect(getBounds().x + dragOffsetX, getBounds().y + dragOffsetY,  getBounds().width-1,  getBounds().height-1);				g.dispose();			}		}		MesquiteWindow.uncheckDoomed(this);	}	public void mouseDown(int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {		if (MesquiteWindow.checkDoomed(this))			return;		if (MesquiteEvent.controlKeyDown(modifiers)){			super.mouseDown(modifiers, clickCount, when, x, y, tool);		}		else {			origTouchX=x;			origTouchY=y;			dragOffsetX=0;			dragOffsetY=0;			if (GraphicsUtil.useXORMode(null, false)){				Graphics g=getParent().getGraphics();				if (g!=null){					g.setXORMode(Color.white);					g.setColor(Color.black);					g.drawRect(getBounds().x, getBounds().y,  getBounds().width-1,  getBounds().height-1);					g.dispose();				}			}		}		MesquiteWindow.uncheckDoomed(this);	}	public void mouseUp (int modifiers, int x, int y, MesquiteTool tool) {		if (MesquiteWindow.checkDoomed(this))			return;		boolean doAdjust = false;		if (dragOffsetX==0 && dragOffsetY ==0)			panelTouched(modifiers, x,y, true);		else doAdjust = true;		offsetX=offsetX + dragOffsetX;		offsetY=offsetY + dragOffsetY;		if (GraphicsUtil.useXORMode(null, false)){			Graphics g=getParent().getGraphics();			if (g!=null){				g.setXORMode(Color.white);				g.setColor(Color.black);				g.drawRect(getBounds().x + dragOffsetX, getBounds().y + dragOffsetY,  getBounds().width-1,  getBounds().height-1);				g.dispose();			}		}		dragOffsetX=0;		dragOffsetY=0;		if (doAdjust) {			adjustLocation();		}		repaint();		MesquiteWindow.uncheckDoomed(this);	}}