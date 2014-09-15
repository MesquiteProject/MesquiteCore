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

/* ��������������������������� commands ������������������������������� */
/* includes commands,  buttons, miniscrolls

/* ======================================================================== */
/** arrow for MiniScroll*/
public class MiniScrollButton extends MousePanel {
	public MiniScroll miniScroll;
	Polygon arrowPoly;
	int orientation;
	public static final int UP = 0;
	public static final int RIGHT = 1;
	public static final int DOWN =2;
	public static final int LEFT = 3;
	boolean disabled = false, pressed = false;
	public static Image leftArrow,leftArrowPressed, leftArrowDisabled;
	public static Image rightArrow,rightArrowPressed, rightArrowDisabled;
	public static Image upArrow,upArrowPressed, upArrowDisabled;
	public static Image downArrow,downArrowPressed, downArrowDisabled;
	private boolean firsttime=true;
	String itemName="";
	
	static {
		leftArrow = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "leftarrow.gif");
		leftArrowPressed = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "leftarrowPressed.gif");
		leftArrowDisabled = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "leftarrowDisabled.gif");
		rightArrow = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "rightarrow.gif");
		rightArrowPressed = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "rightarrowPressed.gif");
		rightArrowDisabled = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "rightarrowDisabled.gif");
		upArrow = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "uparrow.gif");
		upArrowPressed = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "uparrowPressed.gif");
		upArrowDisabled = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "uparrowDisabled.gif");
		downArrow = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "downarrow.gif");
		downArrowPressed = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "downarrowPressed.gif");
		downArrowDisabled = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "downarrowDisabled.gif");
	}
	public MiniScrollButton (MiniScroll miniScroll, int orientation, String itemName) {
		this.orientation = orientation;
		this.miniScroll = miniScroll;
		this.itemName = itemName;
		arrowPoly= new Polygon();
		arrowPoly.xpoints = new int[4];
		arrowPoly.ypoints = new int[4];
		if (orientation==LEFT) {
			arrowPoly.npoints=0;
			arrowPoly.addPoint(1, 7);
			arrowPoly.addPoint(15, 15);
			arrowPoly.addPoint(15, 0);
			arrowPoly.addPoint(1, 7);
			arrowPoly.npoints=4;
		}
		else if (orientation == RIGHT){
			arrowPoly.npoints=0;
			arrowPoly.addPoint(15, 7);
			arrowPoly.addPoint(1, 15);
			arrowPoly.addPoint(1, 0);
			arrowPoly.addPoint(15, 7);
			arrowPoly.npoints=4;
		}
		else if (orientation==UP) {
			arrowPoly.npoints=0;
			arrowPoly.addPoint(7, 1);
			arrowPoly.addPoint(15, 15);
			arrowPoly.addPoint(0, 15);
			arrowPoly.addPoint(7,1);
			arrowPoly.npoints=4;
		}
		else if (orientation == DOWN){
			arrowPoly.npoints=0;
			arrowPoly.addPoint(7, 15);
			arrowPoly.addPoint(15, 1);
			arrowPoly.addPoint(0, 1);
			arrowPoly.addPoint(7, 15);
			arrowPoly.npoints=4;
		}
		setBackground(ColorTheme.getInterfaceBackground());
		setBounds(0,0,16,16);
	}
	
	public void setEnabled(boolean b) {
		disabled = !b;
		repaint();
	}
	public void print (Graphics g) {
	}
	public void paint (Graphics g) {
	   	if (MesquiteWindow.checkDoomed(this))
	   		return;
		if (!getBackground().equals(getParent().getBackground()))
			setBackground(getParent().getBackground());
		/*
		g.setColor(getBackground());
		g.fillRect(0,0,16,16);
		g.setColor(Color.black);
		*/
		if (orientation==LEFT) {
			if (firsttime) {  //done to cache so doesn't flash on user touch
				g.drawImage(leftArrowPressed,0,0,this);
				g.drawImage(leftArrowDisabled,0,0,this);
				g.drawImage(leftArrow,0,0,this);
			}
			firsttime=false;
			if (pressed)
				g.drawImage(leftArrowPressed,0,0,this);
			else if (disabled)
				g.drawImage(leftArrowDisabled,0,0,this);
			else
				g.drawImage(leftArrow,0,0,this);
		}
		else if (orientation==RIGHT){
			if (firsttime) {  //done to cache so doesn't flash on user touch
				g.drawImage(rightArrowPressed,0,0,this);
				g.drawImage(rightArrowDisabled,0,0,this);
				g.drawImage(rightArrow,0,0,this);
			}
			firsttime=false;
			if (pressed)
				g.drawImage(rightArrowPressed,0,0,this);
			else if (disabled)
				g.drawImage(rightArrowDisabled,0,0,this);
			else
				g.drawImage(rightArrow,0,0,this);
		}
		else if (orientation==UP){
			if (firsttime) {  //done to cache so doesn't flash on user touch
				g.drawImage(upArrowPressed,0,0,this);
				g.drawImage(upArrowDisabled,0,0,this);
				g.drawImage(upArrow,0,0,this);
			}
			firsttime=false;
			if (pressed)
				g.drawImage(upArrowPressed,0,0,this);
			else if (disabled)
				g.drawImage(upArrowDisabled,0,0,this);
			else
				g.drawImage(upArrow,0,0,this);
		}
		else if (orientation==DOWN){
			if (firsttime) {  //done to cache so doesn't flash on user touch
				g.drawImage(downArrowPressed,0,0,this);
				g.drawImage(downArrowDisabled,0,0,this);
				g.drawImage(downArrow,0,0,this);
			}
			firsttime=false;
			if (pressed)
				g.drawImage(downArrowPressed,0,0,this);
			else if (disabled)
				g.drawImage(downArrowDisabled,0,0,this);
			else
				g.drawImage(downArrow,0,0,this);
		}
		MesquiteWindow.uncheckDoomed(this);
	}
  	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		
		Graphics g=getGraphics();
		if (g == null)
			return;
 		if (arrowPoly.contains(x,y)) {
	 		if (orientation==LEFT || orientation == DOWN) {
	 			pressed = true;
	 			if (miniScroll.currentValue <= miniScroll.minValue) {
	 				disabled = true;
					if (orientation==LEFT)
						g.drawImage(leftArrowDisabled,0,0,this);
					else
						g.drawImage(downArrowDisabled,0,0,this);
				}
	 			else {
		 			disabled=false;
					if (orientation==LEFT)
						g.drawImage(leftArrowPressed,0,0,this);
					else
						g.drawImage(downArrowPressed,0,0,this);
				}
	 		}
			else if (orientation==RIGHT || orientation == UP){
				pressed = true;
	 			if (miniScroll.currentValue >= miniScroll.maxValue) {
	 				disabled=true;
	 				if (orientation==RIGHT)
						g.drawImage(rightArrowDisabled,0,0,this);
					else
						g.drawImage(upArrowDisabled,0,0,this);
				}
	 			else {
		 			disabled = false;
		 			if (orientation==RIGHT)
						g.drawImage(rightArrowPressed,0,0,this);
					else
						g.drawImage(upArrowPressed,0,0,this);
				}
	 		}
 			//repaint();
 		}
  		g.dispose();
	}
   	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
 		pressed = false;
 		pressed = false;
 		if (arrowPoly.contains(x,y)) {
	 		if (orientation==LEFT || orientation == DOWN) {
	 			if (miniScroll.currentValue <= miniScroll.minValue) {
				}
	 			else {
					miniScroll.decrement();
				}
	 		}
			else if (orientation==RIGHT || orientation == UP){
	 			if (miniScroll.currentValue >= miniScroll.maxValue) {

				}
	 			else {
		 			miniScroll.increment();
				}
	 		}
 			//repaint();
 		}
 		repaint();
	}
	public void mouseEntered(int modifiers, int x, int y, MesquiteTool tool) {
		if (MesquiteWindow.windowOfItem(this)!=null) {
			String name = "item";
			if (!StringUtil.blank(itemName))
				name = itemName;
			String s="";
	 		if (orientation==LEFT || orientation == DOWN) {
	 			s+="This button will take you to the previous "+name+". ";
	 			if (miniScroll.currentValue <= miniScroll.minValue) 
	 				s += "It is disabled as it is already at the first "+name+".";
	 		}
			else if (orientation==RIGHT || orientation == UP){
	 			s+="This button will take you to the next "+name+". ";
	 			if (miniScroll.currentValue >= miniScroll.maxValue) 
	 				s += "It is disabled as it is already at the last "+name+".";
	 		}
			MesquiteWindow.windowOfItem(this).setExplanation(s);
		}
		super.mouseEntered(modifiers,x,y, tool);
	}
	public void mouseExited(int modifiers, int x, int y, MesquiteTool tool) {
		if (MesquiteWindow.windowOfItem(this)!=null) 
			MesquiteWindow.windowOfItem(this).setExplanation("");
		super.mouseExited(modifiers,x,y, tool);
	}
}


