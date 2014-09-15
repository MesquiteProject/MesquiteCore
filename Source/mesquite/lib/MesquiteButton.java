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
import mesquite.lib.duties.*;
import mesquite.lib.simplicity.InterfaceManager;

/* ��������������������������� commands ������������������������������� */
/* includes commands,  buttons, miniscrolls
/* ======================================================================== */
/** A button that responds to a touch by calling its associated command.  Has "off" and "on" images associated with it.
Used, for instance, for palette of tool-choosing buttons in the standard tree window.*/
public class MesquiteButton extends MousePanel implements Explainable, ImageOwner { //, ComponentListener {
	Image image;
	Image optionImage;
	public static Image[] offImage, onImage;  
	MesquiteModule ownerModule;
	public int totalWidth;
	public int totalHeight;
	MesquiteCommand command;
	public String arguments;
	boolean on = false;
	private MediaTracker mt;
	MesquiteTool tool;
	int scheme;
	Polygon dropDownTriangle;
	boolean useWaitThread = true;
	static {
		offImage=  new Image[ColorDistribution.numColorSchemes];  
		onImage=  new Image[ColorDistribution.numColorSchemes];  
	}
	public MesquiteButton (MesquiteModule ownerModule, MesquiteCommand command, String arguments, boolean initialState, String imagePath, int height, int width) {
		this.image = MesquiteImage.getImage(imagePath);
		this.ownerModule = ownerModule;
		mt= new MediaTracker(ownerModule.containerOfModule().getOuterContentsArea());
		mt.addImage(this.image, 0);
		scheme = ColorDistribution.getColorScheme(ownerModule);
		//mt.addImage(this.offImage, 1);
		//mt.addImage(this.onImage, 2);
		this.on = initialState;
		this.totalWidth = width;
		this.totalHeight = height;
		this.command=command;
		this.arguments = arguments;
		dropDownTriangle=MesquitePopup.getDropDownTriangle();
		setSize(totalWidth, totalHeight);
		setBackground(ColorTheme.getInterfaceBackground()); //ggray
		//addComponentListener(this);
	}
	public void setCommand(MesquiteCommand command){
		this.command = command;
	}
	public MesquiteCommand getCommand(){
		return command;
	}
	public void dispose(){ 
		if (mt!=null)
			mt.removeImage(image);
		command = null;
		mt = null;
		if (waitThread !=null){
			waitThread.interrupt();
		}
	}
	public void setUseWaitThread(boolean useThread){
		useWaitThread = useThread;
	}
	public String getToolExplanation(){
		if (tool !=null) {
			if (tool.getDescription()!= null){
				String s = "Tool: " +tool.getDescription();
				if (!tool.getEnabled())
					s += " (disabled)";
				if (!(tool.getExplanation()==null || tool.getDescription().equals(tool.getExplanation())))
					s += "\n(" +tool.getExplanation() + ")";
				return s;
			}
		}
		return "";
	}
	public String getExplanation(){
		if (tool!=null) {
			String s= tool.getExplanation();
			if (ownerModule!=null)
				s+=" <font color=\"#808080\">(for module: " + ownerModule.getName() + ")</font>";
			return s;
		}
		return "";
	}
	String buttonExplanation = null;
	public void setButtonExplanation(String e){
		buttonExplanation = e;
	}
	public String getButtonExplanation(){
		if (buttonExplanation != null)
			return buttonExplanation;
		return getToolExplanation();
	}
	public void setOptionImagePath(String path){
		if (path == null)
			optionImage = null;
		else
			optionImage = MesquiteImage.getImage(path);
	}
	public String getImagePath(){
		if (tool!=null)
			return tool.getImagePath();
		return "";
	}
	public void resetImage(String path){
		if (mt!=null)
			mt.removeImage(image);

		this.image = MesquiteImage.getImage(path);
		if (mt!=null)
			mt.addImage(image, 4);
		waitUntilImagesLoaded();
		repaint();
	}
	public boolean imagesLoaded(){
		return mt.checkAll();
	}
	public void waitUntilImagesLoaded(){
		try {
			mt.waitForAll();
		} catch (Exception e) {
		}
	}
	public void setTool(MesquiteTool tool){
		this.tool = tool;
		if (tool !=null)
			tool.setButton(this);
	}
	public MesquiteTool getTool(){
		return tool;
	}
	public void setOffOn(boolean on) {
		this.on = on;
}
	public void update (Graphics g) {
		paint(g);
	}
	boolean showBackground = true;
	public void setShowBackground(boolean sh){
		showBackground = sh;
	}
	private void drawImage(Graphics g, Image im, int x, int y, int w, int h, Component c){
		if (im !=null)
			g.drawImage(im, x, y, w, h, c);
	}
	private void drawImage(Graphics g, Image im, int x, int y, Component c){
		if (im !=null)
			g.drawImage(im, x, y, c);
	}
	public int getHiddenStatus(){
		if (tool == null)
			return InterfaceManager.NORMAL;
		return InterfaceManager.isHiddenTool(tool);
	}
	public boolean getEnabled(){
		if (tool == null)
			return true;
		return tool.getEnabled();
	}
	
	public void paint (Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		//Rectangle rect = getBounds();
		g.clearRect(0,0, totalWidth, totalHeight);
		int hiddenStatus = getHiddenStatus();
		if (hiddenStatus != InterfaceManager.NORMAL && InterfaceManager.isSimpleMode() && !InterfaceManager.isEditingMode()){
			MesquiteWindow.uncheckDoomed(this);
			return;
		}

		Composite composite = null;
		if (tool!=null && !tool.getEnabled()){
			composite = ColorDistribution.getComposite(g);
			ColorDistribution.setTransparentGraphics(g,0.2f);
		}
		int offset = 0;
		if (showBackground){
			offset = 5;
			if (on){
				g.setColor(ColorTheme.getActiveLight());
				g.fillRoundRect(0, 0, totalWidth-1, totalHeight-1, 3, 3);
				g.setColor(ColorTheme.getActiveDark());
				g.drawRoundRect(0, 0, totalWidth-1, totalHeight-1, 3, 3);
				//	drawImage(g, onImage[scheme],0,0, totalWidth, totalHeight, this);
			}
			else {
				g.setColor(ColorTheme.getInterfaceElement());
				g.fillRoundRect(1, 1, totalWidth-3, totalHeight-3, 3, 3);
				g.setColor(ColorTheme.getInterfaceEdgeNegative());
				g.drawRoundRect(1, 1, totalWidth-3, totalHeight-3, 3, 3);
				//drawImage(g, offImage[scheme], 0, 0, totalWidth, totalHeight, this);
			}
		}
		if (optionDown && optionImage!=null) 
			drawImage(g, optionImage, offset,offset, this);
		else
			drawImage(g, image,offset,offset, this);  //16x16 pixels is standard tool cursor size
		if (tool !=null){
			if (tool.getPopUpOwner()!=null || tool.getOptionsCommand() !=null) {
				dropDownTriangle.translate(totalWidth-7,2);
				g.setColor(Color.white);
				g.drawPolygon(dropDownTriangle);
				g.setColor(Color.black);
				g.fillPolygon(dropDownTriangle);
				dropDownTriangle.translate(-(totalWidth-7),-(2));
			}
		}
		if (tool!=null && !tool.getEnabled()) 
			ColorDistribution.setComposite(g,composite);
		if (hiddenStatus != InterfaceManager.NORMAL && InterfaceManager.isEditingMode()){
			if (hiddenStatus == InterfaceManager.TOBEHIDDEN)
				g.setColor(Color.red);
			else {
				g.setColor(Color.blue);
			}
			g.drawRoundRect(0, 0, totalWidth-1, totalHeight-1, 3, 3);
			g.drawRoundRect(1, 1, totalWidth-3, totalHeight-3, 3, 3);
			g.drawRoundRect(2, 2, totalWidth-5, totalHeight-5, 3, 3);
		}
		MesquiteWindow.uncheckDoomed(this);
	}
	public void setOptionDown(boolean d){
		optionDown = d;
	}
	/* pass modifiers */
	public void modifierKeysPressed (int modifiers) {
		String modString = MesquiteEvent.modifiersToString(modifiers);
		if (modString.indexOf("option")>=0) {
			if (tool!=null && !optionDown){
				if (optionImage!=null)
					tool.setCursorImage(null);
				tool.setOptionDown(true);

				if (tool.getInUse() && (optionImage!=null || tool.hasTemporaryOptionCursor())) {
					MesquiteWindow w = MesquiteWindow.windowOfItem(this);
					if (w!=null)
						w.resetCursor();
				}
			}
			optionDown = true;
			repaint();
		} else if (modString.indexOf("shift")>=0) {
			if (tool!=null){
				if (tool.getInUse() && ( tool.hasTemporaryShiftCursor())) {
					MesquiteWindow w = MesquiteWindow.windowOfItem(this);
					if (w!=null)
						w.resetCursor();
				}
			}
		}

	}
	boolean optionDown;
	/* pass modifiers */
	public void modifierKeysReleased (int modifiers) {
		String modString = MesquiteEvent.modifiersToString(modifiers);
		if (modString.indexOf("option")<0) {
			if (tool!=null && optionDown){
				if (optionImage!=null)
					tool.setCursorImage(null);
				tool.setOptionDown(false);
				if (tool.getInUse() && (optionImage!=null || tool.hasTemporaryOptionCursor())) {
					MesquiteWindow w = MesquiteWindow.windowOfItem(this);
					if (w!=null)
						w.resetCursor();
				}
			}
			optionDown = false;
			repaint();
		} else if (modString.indexOf("shift")>=0) {
			if (tool!=null){
				if (tool.getInUse() && ( tool.hasTemporaryShiftCursor())) {
					MesquiteWindow w = MesquiteWindow.windowOfItem(this);
					if (w!=null)
						w.resetCursor();
				}
			}
		}

	}
	ButtonWaitThread waitThread;
	/* pass modifiers */
	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool toolTouching) {
		if (tool!=null && !tool.getEnabled()) 
			return;
		if (MesquiteWindow.getQueryMode(this)) {
			MesquiteWindow.respondToQueryMode("Button", command, this);
		}
		else if (tool != null && InterfaceManager.isHiddenTool(tool) != InterfaceManager.NORMAL)
			;
		else if (useWaitThread){
			String modString = MesquiteEvent.modifiersToString(modifiers);
			//command.doItMainThread(arguments + "  " + Integer.toString(x) + "  " + Integer.toString(y) + "  " + modString, CommandChecker.getQueryModeString("Button", command, this));
			if (modString == null || modString.indexOf("command")<0){
				waitThread = new ButtonWaitThread(this, when, x, y);
				waitThread.start();
			}

		}
	}
	boolean suppressUp = false;
	boolean hideable = true;
	public void mouseUp(int modifiers, int x, int y, MesquiteTool toolTouching) {
		if (tool!=null && !tool.getEnabled()) 
			return;
		if (waitThread!=null){
			waitThread.goOff = true;
			waitThread.interrupt();
			waitThread = null;

		}
		if (command == null)
			return;
		String modString = MesquiteEvent.modifiersToString(modifiers);
		int hiddenStatus = 0;
		if (InterfaceManager.isEditingMode()){
			if (tool != null && hideable){
				hiddenStatus = InterfaceManager.isHiddenTool(tool);
				if (hiddenStatus == InterfaceManager.NORMAL){
					InterfaceManager.addToolToHidden(tool, true);
					MesquiteTrunk.resetAllToolPalettes();
				}
				else if (hiddenStatus != InterfaceManager.HIDDENCLASS) {
					InterfaceManager.removeToolFromHidden(tool.getName(), tool.getDescription(), true);
					MesquiteTrunk.resetAllToolPalettes();
				}

			}
		}
		else 		if (!suppressUp)
			command.doItMainThread(arguments + "  " + Integer.toString(x) + "  " + Integer.toString(y) + "  " + modString, CommandChecker.getQueryModeString("Button", command, this), this);
	}
	public void mouseEntered(int modifiers, int x, int y, MesquiteTool toolTouching) {
		if (MesquiteWindow.windowOfItem(this)!=null) {
			MesquiteWindow.windowOfItem(this).setExplanation(getButtonExplanation());
		}
		if (this.tool !=null && !this.tool.getEnabled()) {
			setCursor(getDisabledCursor());
		}
		super.mouseEntered(modifiers,x,y, toolTouching);
	}
	public void mouseExited(int modifiers, int x, int y, MesquiteTool toolTouching) {
		if (MesquiteWindow.windowOfItem(this)!=null) 
			MesquiteWindow.windowOfItem(this).setExplanation("");
		super.mouseExited(modifiers,x,y, toolTouching);
	}
	public void setOn () {
		if (!on) {
			on=true;
			repaint();
		}
	}
	public void setOff () {
		if (on) {
			on=false;
			repaint();
		}
	}
}

class ButtonWaitThread extends Thread {
	static final int waitTime = 500;
	MesquiteButton button;
	long when;
	int x, y;
	boolean goOff = false;
	public ButtonWaitThread (MesquiteButton button, long when, int x, int y) {
		this.button = button;
		this.when = when;
		this.x = x;
		this.y=y;
	}
	/** DOCUMENT */
	public void run() {
		try {
			while (!goOff && !MesquiteTrunk.mesquiteTrunk.mesquiteExiting){
				sleep(50);
				if (!goOff && System.currentTimeMillis()-when>waitTime){
					button.suppressUp = true;
					button.command.doIt(button.arguments + "  " + Integer.toString(x) + "  " + Integer.toString(y) + "  command"); //double click and command interpreted as same
					button.suppressUp = false;
					goOff = true;
				}
			}

		}
		catch (InterruptedException e) {
		}
	}
}


