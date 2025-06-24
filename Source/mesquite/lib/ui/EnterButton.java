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

import java.awt.Graphics;
import java.awt.Image;

import mesquite.lib.MesquiteModule;

/* ��������������������������� commands ������������������������������� */
/* includes commands,  buttons, miniscrolls
/* ======================================================================== */
/** enter button for MiniScroll & MiniTextEditor*/
public class EnterButton extends MousePanel {
	public MiniControl miniControl;
	public static Image enterVerticalDisabled, enterHorizontalDisabled;
	public static Image enterVertical, enterHorizontal;
	public static Image enterVerticalPressed, enterHorizontalPressed;
	private Image enter;
	private Image enterDisabled;
	private Image enterPressed;
	private boolean firsttime=true;
	private boolean pressed = false;
	private boolean disabled = false;
	public static final int MIN_DIMENSION = 13;
	
	static {
		enterVerticalDisabled = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "enterVertDisabled.gif");
		enterHorizontalDisabled = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "enterHorizDisabled.gif");
		enterVertical = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "enterVert.gif");
		enterHorizontal = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "enterHoriz.gif");
		enterVerticalPressed = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "enterVertPressed.gif");
		enterHorizontalPressed = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "enterHorizPressed.gif");
	}
	public EnterButton (MiniControl miniControl, boolean vertical) {
		this.miniControl = miniControl;
		if (vertical) {
			enter = enterVertical;
			enterDisabled = enterVerticalDisabled;
			enterPressed = enterVerticalPressed;
			setBounds(0,0,MIN_DIMENSION,16);
		}
		else {
			enter = enterHorizontal;
			enterDisabled = enterHorizontalDisabled;
			enter = enterHorizontalPressed;
			setBounds(0,0,16,MIN_DIMENSION);
		}
	}
	public EnterButton (MiniControl miniControl) {
		this(miniControl, true);
	}
	
	public  void setEnabled(boolean b) {
		disabled = !b;
		repaint();
	}
	public void print (Graphics g) {
	}
	public void paint (Graphics g) {
	   	if (MesquiteWindow.checkDoomed(this))
	   		return;
		if (getBackground() != null && getParent() != null && !getBackground().equals(getParent().getBackground()))
			setBackground(getParent().getBackground());
		if (pressed)
			g.drawImage(enterPressed,0,0,this);
		else if (disabled)
			g.drawImage(enterDisabled,0,0,this);
		else
			g.drawImage(enter,0,0,this);
		MesquiteWindow.uncheckDoomed(this);
	}
  	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		if (!disabled) {
			Graphics g=getGraphics();
			if (g == null)
				return;
			pressed = true;
			g.drawImage(enterPressed,0,0,this);
			g.dispose();
		}
		//super.mouseDown(modifiers,x,y,tool);
	}
   	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		if (!disabled) {
			Graphics g=getGraphics();
			if (g == null)
				return;
			pressed=false;
			miniControl.acceptText();
			paint(g);
			g.dispose();
		}
	}
	public void mouseEntered(int modifiers, int x, int y, MesquiteTool tool) {
		if (MesquiteWindow.windowOfItem(this)!=null) {
			String s="Values in the associated text field will be entered if this button is pressed. ";
	 		if (disabled) 
	 			s += "It is disabled as the value entered is identical to the current value, too small or too large, or otherwise invalid.";
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

