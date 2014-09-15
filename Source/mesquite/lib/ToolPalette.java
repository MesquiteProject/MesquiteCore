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
import java.awt.image.*;
import java.util.*;


/* ======================================================================== */

import mesquite.lib.simplicity.InterfaceManager;

public class ToolPalette extends MesquitePanel implements Commandable, KeyListener {
	protected MesquiteWindow container;
	public ListableVector tools;
	protected MesquiteModule ownerModule;
	MesquiteTool currentTool;
	private int numTools=0;
	private static final int buttonSize = 26;
	int defaultTool = 0;
	private boolean firsttime=true;
	MesquiteButton[] toolButtons;
	int firstHeight = 12;
	int numColumns = 1;
	int defaultNumColumns = 1;
	private int paletteWidth;
	int spacer =5;
	int singleColumnSpacer = 20; //13;
	int colorScheme;
	static final int triangleLeft= 4;
	static final int triangleTop = 4;
	static final int triangleWidth = 11;
	static final int triangleHeight = 6;
	Color bgColor;
	public ToolPalette(MesquiteModule ownerModule, MesquiteWindow container, int numColumns) {  //in future pass general MesquiteWindow
		super();
	

		this.numColumns = numColumns;
		this.defaultNumColumns = numColumns;
		this.ownerModule=ownerModule;
		this.container = container;
		tools = new ListableVector();
		toolButtons = new MesquiteButton[64];
		setLayout(null);
		colorScheme = ColorDistribution.getColorScheme(ownerModule);
		//bgColor = ColorDistribution.medium[colorScheme];
		bgColor = ColorTheme.getInterfaceBackground();
		setBackground(bgColor);
		//	setBackground(ColorDistribution.medium[colorScheme]);  //used to be light//ggray
		if (numColumns ==1)
			paletteWidth =  (buttonSize+singleColumnSpacer);
		else
			paletteWidth =  (buttonSize+spacer)*numColumns + spacer;
		setCursor(Cursor.getDefaultCursor());
	}
	public int getWidth(){
		if (numTools == 0)
			return 0;
		return paletteWidth;
	}
	public void keyTyped(KeyEvent e){
	}

	boolean modifiersHadBeenPressed = false;

	public void keyPressed(KeyEvent e){
		if (MesquiteEvent.getModifiers(e)!=0 && toolButtons!=null) {
			modifiersHadBeenPressed = true;
			for (int i = 0; i<toolButtons.length; i++) {
				if (toolButtons[i] !=null)
					toolButtons[i].modifierKeysPressed(MesquiteEvent.getModifiers(e));
			}
		}

	}
	public void keyReleased(KeyEvent e){
		if (modifiersHadBeenPressed && toolButtons!=null) {
			for (int i = 0; i<toolButtons.length; i++) {
				if (toolButtons[i] !=null)
					toolButtons[i].modifierKeysReleased(MesquiteEvent.getModifiers(e));
			}
		}
		if (MesquiteEvent.getModifiers(e)==0)
			modifiersHadBeenPressed = false;
	}
	public void setNumColumns(int i){
		numColumns = i;
		if (numColumns ==1)
			paletteWidth =  (buttonSize+singleColumnSpacer);
		else
			paletteWidth =  (buttonSize+spacer)*numColumns + spacer;
		paletteWidth=paletteWidth;
		if (paletteWidth <minWidth())
			paletteWidth = minWidth();
		recheckSize();
	}
	/*.................................................................................................................*
	public void mouseMoved(int modifiers, int x, int y, MesquiteTool tool) {
		String s="";
		try {
			if (inShowInfoBarButton(x,y)) {
				if (!((MesquiteWindow)container).getShowInfoBar())
					s += "This button will show the information bar at the top of this window, which will allow one to see other graphical and text views, as well as parameter information, active modules, and citations. ";
			}
			((MesquiteWindow)container).setExplanation(s);
			super.mouseMoved(modifiers,x,y,tool);
		}
		catch (Exception e){
		}
	}
	/*.................................................................................................................*/
	public boolean inShowInfoBarButton(int x, int y) {
		return (x<=triangleLeft+triangleWidth && x>=triangleLeft && y<=triangleTop + triangleHeight && y>=triangleTop);
	}
	/*.................................................................................................................*
   	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
   		if (inShowInfoBarButton(x,y)){
   			if (container instanceof MesquiteWindow) {
   				if (!((MesquiteWindow)container).getShowInfoBar()) {
   					((MesquiteWindow)container).setShowInfoBar(true);
   					repaint();
				}
   			}

   		}
   	}
	/*.................................................................................................................*/
	public MesquiteTool getToolWithName(String name) {
		if (tools!=null) {
			Enumeration e = tools.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				MesquiteTool mt = (MesquiteTool)obj;
				if (mt.getName().equalsIgnoreCase(name))
					return mt;
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	public MesquiteButton getButtonWithName(String name) {
		if (name !=null && toolButtons!=null) {
			for (int i = 0; i<toolButtons.length; i++) {
				if (toolButtons[i] !=null){
					MesquiteTool tool = toolButtons[i].getTool();
					if (tool!=null)
						if (name.equalsIgnoreCase(tool.getName()))
							return toolButtons[i];
				}
			}
		}
		return null;
	}
	public void repaintAll(){
		repaint();
		if (toolButtons!=null){
			for (int i=0; i<toolButtons.length; i++) {
				if (toolButtons[i]!=null) {
					toolButtons[i].repaint();
				}
			}
		}
	}
	/**/
	public void dispose(){
		MesquiteWindow w = MesquiteWindow.windowOfItem(this);
		if (w!=null)
			w.waitUntilDisposable();
		try {
			if (toolButtons!=null){
				for (int i=0; i<toolButtons.length; i++) {
					if (toolButtons[i]!=null) {
						toolButtons[i].dispose();
						remove(toolButtons[i]);
						toolButtons[i]=null;
					}
				}
				toolButtons=null;
			}
			if (tools!=null) {
				tools.removeAllElements(true);
			}
		}
		catch (Exception e){
		}
		tools = null;
		ownerModule=null;
		currentTool = null;
		container = null;
	}
	public void setFirstToolHeight(int h) {
		this.firstHeight = h;
	}

	public int getBottomOfTools() {
		return firstHeight + ((numTools-1)/numColumns) * (buttonSize+inBetween) + buttonSize;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		if (currentTool==null)
			return null;
		Snapshot temp = new Snapshot();
		temp.addLine("setTool " + StringUtil.tokenize(currentTool.getName()));
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger(0);
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the active tool in the tool palette", "[tool name][x coordinate touched][y coordinate touched][modifiers]", commandName, "setTool")) {
			pos.setValue(0);
			String toolName = (ParseUtil.getToken(arguments, pos));
			MesquiteButton button =getButtonWithName(toolName);
			if (button==null) {
				//MesquiteMessage.warnProgrammer("button not found!  (" + toolName + ")");
				return null;
			}
			MesquiteTool tool =button.getTool();//getToolWithName(toolName);
			if (tool!=null){
				if (tool!=currentTool) {
					setCurrentTool(tool);
					container.setCurrentTool(tool);
				}
				int x = MesquiteInteger.fromString(arguments, pos);
				int y = MesquiteInteger.fromString(arguments, pos);
				String arg = ParseUtil.getToken(arguments, pos);
				if (MesquiteInteger.isCombinable(x) && MesquiteInteger.isCombinable(y) && arg!=null && arg.indexOf("command")>=0) {
					tool.chooseOptions(button, x, y);
				}

			}
		}
		else 
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	int inBetween = 2;
	int findBestNumColumns(int bottom){
		if (getBounds().height<firstHeight + buttonSize)
			return 4;
		int nC = 1;
		while (nC<5 && bottom <firstHeight + ((numTools-1)/nC) * (buttonSize+inBetween) + buttonSize) {
			nC++;
		}
		if (nC>4)
			return 4;
		if (nC<defaultNumColumns)
			return defaultNumColumns;
		return nC;
	}
	public int minWidth(){
		return 0;
	}
	void recheckSize(){
		if (toolButtons==null)
			return;
		if (numTools==0)
			return;
		int totalWidth = getWidth();
		int neededHeight =   ((numTools)/numColumns) * (buttonSize+inBetween);
		int neededWidth = numColumns *buttonSize + (numColumns-1) *inBetween;

		int leftEdge = (totalWidth-neededWidth)/2;
		int ht =getBounds().height;
		int curBest = findBestNumColumns(ht-48);
		if (curBest != numColumns) {
			setNumColumns(curBest);
		}
		//else if (numTools-1<toolButtons.length && toolButtons[numTools-1]!=null){
		//	toolButtons[numTools-1].setLocation(leftEdge+ (buttonSize+inBetween)*((numTools-1)%numColumns), firstHeight + ((numTools-1)/numColumns) * (buttonSize+inBetween));
		//	toolButtons[numTools-1].setVisible(true);
		//}
		if (toolButtons==null)
			return;
		deepestButton = 0;
		int count = 0;
		for (int i=0; i<= numTools && i<toolButtons.length; i++)
			if (toolButtons[i]!=null) {
				if ((!InterfaceManager.isSimpleMode() || InterfaceManager.isEditingMode()) || (toolButtons[i].getHiddenStatus() != InterfaceManager.HIDDEN && toolButtons[i].getHiddenStatus() != InterfaceManager.HIDDENCLASS)){
					int bDepth = firstHeight + ((count)/numColumns) * (buttonSize+inBetween);
					toolButtons[i].setLocation(leftEdge+ (buttonSize+inBetween)*((count)%numColumns), bDepth);
					if (bDepth>deepestButton)
						deepestButton = bDepth;
					count++;
				}
				else
					toolButtons[i].setLocation(-100, -100);

			}
	}
	int deepestButton = 0;
	public int getDeepestButton(){
		return deepestButton + buttonSize;
	}
	public MesquiteButton addTool(MesquiteTool tool) {
		if (tool == null || tools == null)
			return null;
		tools.addElement(tool, false);
		tool.setPalette(this);

		MesquiteButton button = new MesquiteButton(ownerModule, MesquiteModule.makeCommand("setTool", this), StringUtil.tokenize(tool.getName()), false, tool.getImagePath(), buttonSize,buttonSize);
		toolButtons[numTools] = button;
		if (tool.getOptionImagePath()!=null)
			button.setOptionImagePath(tool.getOptionImagePath());
		toolButtons[numTools].setTool(tool);
		toolButtons[numTools].waitUntilImagesLoaded();
		if (numTools==0)
			toolButtons[0].setOffOn(true);
		add(toolButtons[numTools]);
		numTools++;
		recheckSize();
		if (toolButtons[numTools-1]!=null)
			toolButtons[numTools-1].setVisible(true);
		button.setVisible(true);
		tool.setButton(button);
		repaint();
		return button;

	}
	public void removeTool(MesquiteTool tool) {
		if (tool == null || tools == null)
			return;
		tools.removeElement(tool, false);
		for (int i=0; i<toolButtons.length; i++) {
			if (toolButtons[i]!=null) {
				if (toolButtons[i].getTool() == tool){
					MesquiteButton temp = toolButtons[i];
					for (int j=i+1; j<toolButtons.length; j++){
						toolButtons[j-1]=toolButtons[j];
					}
					numTools--;
					if (tool == currentTool && toolButtons.length>0)
						setCurrentTool(0);
					toolButtons[toolButtons.length-1]=null;
					temp.dispose();
					remove(temp);
					recheckSize();
					repaint();
					return;
				}
			}
		}
	}
	public MesquiteTool getCurrentTool() {
		return currentTool;
	}
	public int getNumTools() {
		return numTools;
	}
	public void toolTextChanged(){
		container.setExplanation("");
		if (currentTool!=null) {
			container.setExplanation(currentTool.fullDescription);
			if (currentTool.getDescription()!= null){
				if (currentTool.getDescription()!=null)
					container.setExplanation("Current Tool: " + currentTool.getDescription());
			}
		}
	}

	public void setToDefaultTool() {
		setCurrentTool(defaultTool);
	}
	public void setCurrentTool(int button) {
		MesquiteTool tool =  toolButtons[button].getTool();
		setCurrentTool(tool);
	}
	public void setCurrentTool(MesquiteTool tool) {
		if (tool==null || !tool.getEnabled())
			return;
		Parser parser = new Parser();
		for (int i=0; i<numTools ; i++) {
			if (tool.getName().equalsIgnoreCase(parser.getFirstToken(toolButtons[i].arguments))) {  //SHOULDN't RELY ON NAME HERE!!!!!
				toolButtons[i].setOn();
			}
			else
				toolButtons[i].setOff();
		}
		if (currentTool!=null)
			currentTool.setInUse(false);
		currentTool = tool;
		currentTool.setInUse(true);
	}
	public void setSize(int w, int h){
		super.setSize(w,h);
		//recheckSize();
	}
	public void setBounds(int x, int y, int w, int h){
		super.setBounds(x,y,w,h);
	}
	public void paint(Graphics g) {//^^^
		bgColor = ColorTheme.getInterfaceBackground();
		if (	getBackground() != bgColor){
			setBackground(bgColor);
			repaint();
			return;
		}

		if (MesquiteWindow.checkDoomed(this))
			return;
		int ht =getBounds().height;
		int w =getBounds().width;


		int locX = 0;
		int locY = 0;
		/*if (InterfaceManager.isEditingMode()){
			Color co = g.getColor();
			g.setColor(Color.cyan);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(co);
		}*/
		g.setColor(ColorTheme.getContentEdgeDark());  //used to be light
		g.fillRect(w-2, 0, 2, ht);
		g.setColor(bgColor);  //used to be light
		MesquiteWindow.uncheckDoomed(this);
	}

}


