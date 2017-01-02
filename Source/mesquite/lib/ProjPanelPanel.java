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
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.util.*;
import java.util.List;

import mesquite.lib.duties.*;
import mesquite.minimal.BasicFileCoordinator.ProjectWindow;

/* ======================================================================== */
/**An interface for a set of trees or a tree source.*/
public class ProjPanelPanel extends ClosablePanel implements MesquiteListener, ClosablePanelContainer, MesquiteDroppedFileHandler {
	protected Vector commands = new Vector();
	protected MesquitePopup popup=null;
	protected FileCoordinator bfc;
	protected StringInABox notes, commandBox;
	protected int notesWidth = 560;
	protected int notesLeft = 10;
	protected int commandBoxWidth = 64;
	protected int COMMANDHEIGHT = 64 - MINHEIGHT;
	protected Image im;
	protected Vector subPanels = new Vector();
	protected MesquiteWindow w = null;
	MesquiteModule owner;
	ProjectWindow projectWindow;

	public ProjPanelPanel(FileCoordinator bfc, ClosablePanelContainer container, MesquiteWindow w, String name, MesquiteModule owner){
		super(container, name);
		this.owner = owner;
		
		this.w = w;
		projectWindow = (ProjectWindow)w;
		setOpen(false);
		setColors(ColorTheme.getExtInterfaceBackground(), ColorTheme.getExtInterfaceBackground(), ColorTheme.getExtInterfaceElement(), ColorTheme.getExtInterfaceTextMedium());
		
		this.bfc = bfc;
		if (getIconFilePath() != null)
			im = 	MesquiteImage.getImage(getIconFilePath());
		currentHeight = COMMANDHEIGHT + MINHEIGHT;

		setTightness(2);
		notes =  new StringInABox(getNotes(), new Font("SansSerif", Font.PLAIN, MesquiteFrame.resourcesFontSize), notesWidth);
		commandBox =  new StringInABox(getNotes(), new Font("SansSerif", Font.PLAIN, MesquiteFrame.resourcesFontSize), commandBoxWidth);
		dropTarget = new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);  

		refresh();
	}
	public void setOpen(boolean b){
		super.setOpen(false);  //sorry, closed only
	}
	public boolean isOpen(){
		return false;
	}
public void resetTitle(){
	}
	public MesquiteModule getOwnerModule(){
		return owner;
	}
	protected int getFontSize(){
		return MesquiteFrame.resourcesFontSize;
	}
	public void refreshGraphics(){
		if (getBold())
			setFont(new Font("SansSerif", Font.BOLD, getFontSize()));
		else
			setFont(new Font("SansSerif", Font.PLAIN, getFontSize()));
		notes.setFont(new Font("SansSerif", Font.PLAIN, MesquiteFrame.resourcesFontSize));
		commandBox.setFont(new Font("SansSerif", Font.PLAIN, MesquiteFrame.resourcesFontSize));
		setColors(ColorTheme.getExtInterfaceBackground(), ColorTheme.getExtInterfaceBackground(), ColorTheme.getExtInterfaceElement(), ColorTheme.getExtInterfaceTextMedium());
		repaint();
		if (subPanels != null)
			for (int i = 0; i<subPanels.size(); i++){
				ProjPanelPanel p = (ProjPanelPanel)subPanels.elementAt(i);
				p.refreshGraphics();
			}
	}
	public void actUponDroppedFileContents(FileInterpreter fileInterpreter, String droppedContents) {
	}
	public FileInterpreter findFileInterpreter(String droppedContents, String fileName) {
		return null;
	}
	/*.................................................................................................................*/
	public void processFilesDroppedOnPanel(List files) {
	}
	/*.................................................................................................................*/
	public void processFileStringDroppedOnPanel(String path) {
	}

	public void refreshIcon(){
		if (getIconFilePath() != null)
			im = 	MesquiteImage.getImage(getIconFilePath());
		else
			im = null;
	}
	/*.................................................................................................................*/
	public String getIconFilePath(){ //for small 16 pixel icon at left of main bar
		if (getIconFileName() != null) {
			String p = bfc.getPath()+ "projectHTML" + MesquiteFile.fileSeparator + getIconFileName();
			return p;
		}
		return null;
	}
	/*.................................................................................................................*/
	public String getIconFileName(){ //for small 16 pixel icon at left of main bar
		return null;
	}
	public Image getIcon(){ //for small 16 pixel icon at left of main bar
		return im;
	}
	public int requestSpacer(){
		return 0;
	}
	public String getNotes(){
		return null;
	}
	public String getFootnote(){
		return null;
	}
	public String getFootnoteHeading(){
		return null;
	}
		
	public void cursorEnter(){
		String text = getFootnote();
		String heading = getFootnoteHeading();
		
		hover = true;
		repaint();
		projectWindow.setFootnote(heading, text);
	}

	public void cursorExit(){
		hover = false;
		repaint();
		projectWindow.setFootnote(null,null);
	}
	public boolean upToDate(){
		return true;
	}
	public void setColors(Color bgTopColor, Color bgBottomColor, Color barColor, Color textColor){
		super.setColors(bgTopColor, bgBottomColor, barColor, textColor);
		for (int i = 0; i<subPanels.size(); i++){
			ClosablePanel panel = ((ClosablePanel)subPanels.elementAt(i));
			panel.setColors(bgBottomColor, bgBottomColor, bgBottomColor, textColor);
		}
	}
	public void refresh(){
		if (upToDate()) {
			resetSizes(getWidth(), getHeight());
			repaintAllPanels();
			return;
		}
		for (int i = 0; i<subPanels.size(); i++){
			ClosablePanel panel = ((ClosablePanel)subPanels.elementAt(i));
			remove(panel);
			panel.dispose();
		}
		subPanels.removeAllElements();
	}
	public ClosablePanel getPrecedingPanel(ClosablePanel panel){
		return null;
	}
	public void chart(){  //to be overridden to respond to command to chart the element
	}
	public void changed(Object caller, Object obj, Notification notification){
			refresh();
	}
	/*.................................................................................................................*/

	public void paint(Graphics g){
		if (!sizesMatch(getWidth(), getHeight())){
			resetSizes(getWidth(), getHeight());
			repaintAllPanels();
			repaint();
			return;
		}
		g.setColor(ColorTheme.getExtInterfaceTextMedium());
		int nH = 0;
		if (!StringUtil.blank(getNotes())){
			String s = notes.getString();
			if (s != null && !s.equals(getNotes()))
				notes.setString(getNotes());
			notes.draw(g,notesLeft, MINHEIGHT-4);
			nH = 16;
		}
		g.setColor(barColor);
		g.fillRect(0, getHeight()-2, getWidth(), 2);
		g.setColor(ColorTheme.getExtInterfaceTextMedium());
		if (commands.size()>0){
			int left = notesLeft;
			for (int i=0; i<commands.size(); i++){
				ProjPanelCommand ec = (ProjPanelCommand)commands.elementAt(i);
				if (!ec.menuOnly){
					ec.left=left;
					if (ec.icon !=null){
						Composite comp = ((Graphics2D)g).getComposite();
						ColorDistribution.setTransparentGraphics(g,0.8f);		
						g.drawImage(ec.icon, left, MINHEIGHT+nH + 4, this);
						((Graphics2D)g).setComposite(comp);
						left += ec.icon.getWidth(this) + 2;
					}
					if (ec.label != null){
						commandBox.setString(ec.label);
						g.setColor(ColorTheme.getExtInterfaceTextLink());
						commandBox.draw(g,left, MINHEIGHT+nH);

						g.setColor(ColorTheme.getExtInterfaceTextMedium());
						left += commandBox.getMaxWidthDrawn() + 2;
					}
					ec.right=left;
					left += 12;

				}
			}

		}
		super.paint(g);
	}	
	protected void addExtraPanel(ProjPanelPanel p, boolean doReset){
		subPanels.addElement(p);
		add(p);
		if (doReset) {
			resetSizes(getWidth(), getHeight());
			p.setVisible(true);
		}
	}
	public void repaintAllPanels(){
		for (int i = 0; i<subPanels.size(); i++){
			ClosablePanel panel = ((ClosablePanel)subPanels.elementAt(i));
			panel.repaint();
		}
	}
	public void requestHeightChange(ClosablePanel panel){
		resetSizes(getWidth(), getHeight());
		container.requestHeightChange(this);
	}
	static final int indent = 20;
	protected void resetSizes(int w, int h){
		if (bfc.isDoomed() || bfc.getProject().refreshSuppression>0)
			return;
		if (!isOpen()){
			for (int i = 0; i<subPanels.size(); i++){
				ClosablePanel panel = ((ClosablePanel)subPanels.elementAt(i));
				panel.setBounds(0, 0, 0, 0);
				panel.setVisible(false);
			}
			return;
		}

		int vertical = MINHEIGHT+2;
		if (anyGraphicalCommands())
			vertical += COMMANDHEIGHT;
		if (getNotes() != null)
			vertical += 24;

		for (int i = 0; i<subPanels.size(); i++){
			ClosablePanel panel = ((ClosablePanel)subPanels.elementAt(i));
			int requestedlHeight = panel.getRequestedHeight(w);
			panel.setVisible(false);
			panel.setBounds(indent, vertical, w-indent, requestedlHeight);
			panel.setVisible(true);

			vertical += requestedlHeight;
		}
	}
	boolean sizesMatch(int w, int h){
		if (!isOpen()){
			for (int i = 0; i<subPanels.size(); i++){
				ClosablePanel panel = ((ClosablePanel)subPanels.elementAt(i));
				if (panel.getWidth() != 0 || panel.getHeight() != 0)
					return false;

			}
			return true;
		}
		for (int i = 0; i<subPanels.size(); i++){
			ClosablePanel panel = ((ClosablePanel)subPanels.elementAt(i));
			int requestedlHeight = panel.getRequestedHeight(w);
			if (panel.getWidth() != w-indent || panel.getHeight() != requestedlHeight)
				return false;
		}
		return true;
	}
	boolean anyGraphicalCommands(){
		if (commands.size() == 0)
			return false;
		for (int i=0; i<commands.size(); i++){
			ProjPanelCommand ec = (ProjPanelCommand)commands.elementAt(i);
			if (!ec.menuOnly)
				return true;

		}
		return false;
	}
	public int getRequestedHeight(int width){
		if (!isOpen())
			return MINHEIGHT;
		int total = MINHEIGHT+5;
		if (anyGraphicalCommands())
			total += COMMANDHEIGHT;
		if (getNotes() != null)
			total += 24;
		for (int i=0; i<subPanels.size(); i++){
			ClosablePanel mp = (ClosablePanel)subPanels.elementAt(i);
			total += mp.getRequestedHeight(width-20);
		}
		return total;
	}
	public void setBounds(int x, int y, int w, int h){
		super.setBounds(x,y,w,h);
		resetSizes(w, h);
	}
	public void setSize(int w, int h){
		super.setSize(w,h);
		resetSizes(w, h);
	}	
	public void deleteAllCommands(){
		commands.removeAllElements();
	}
	public void addCommand(boolean menuOnly, String iconFileName, String label, String shortLabel, MesquiteCommand command){
		ProjPanelCommand ec = new ProjPanelCommand(menuOnly, iconFileName, label, shortLabel, command);
		if (iconFileName != null)
			ec.icon = MesquiteImage.getImage(bfc.getPath() + "projectHTML" + MesquiteFile.fileSeparator + iconFileName);

		commands.addElement(ec);
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Charts the file element", null, commandName, "chart")) {
			chart();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	public void disposing(Object obj){

	}
	public boolean okToDispose(Object obj, int queryUser){
		return true;
	}
	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		//if modifiers include right click/control, then do dropdown menu
		if (MesquiteEvent.rightClick(modifiers) || y<= MINHEIGHT && x> triangleOffset + 8) {
			redoMenu();
			popup.show(this, x,y);
			return;
		}

		else if (y<= MINHEIGHT) {
			super.mouseDown(modifiers,  clickCount,  when,  x,  y,  tool);
			return;
		}
		else {
			for (int i=0; i<commands.size(); i++){
				ProjPanelCommand ec = (ProjPanelCommand)commands.elementAt(i);
				if (!ec.menuOnly){
					if (x>=ec.left && x<ec.right){
						ec.command.doItMainThread(null, null, this);
						repaint();
						return;
					}

				}
			}
		}
	}

	protected String getMenuHeading(){
		String heading = getTitle();
		return heading;
	}
	protected void resetCommands(){ //use this only if last minute command changes might exist
	}
	/*.................................................................................................................*/
	void redoMenu() {
		if (popup==null)
			popup = new MesquitePopup(this);
		resetCommands();
		popup.removeAll();
		String heading = getMenuHeading();
		popup.add(new MesquiteMenuItem(heading, bfc, null));
		for (int i=0; i<commands.size(); i++) {
			ProjPanelCommand m = (ProjPanelCommand)commands.elementAt(i);
			MesquiteMenuItem mItem = new MesquiteMenuItem(m.shortLabel, bfc, m.command);
			popup.add(mItem);
		}
		add(popup);
	}
	public void mouseEntered(int modifiers, int x, int y, MesquiteTool tool) {
		super.mouseEntered(modifiers, x, y, tool);
		cursorEnter();
	}
	public void mouseMoved(int modifiers, int x, int y, MesquiteTool tool) {
		super.mouseMoved(modifiers, x, y, tool);
		cursorEnter();
	}
	public void mouseExited(int modifiers, int x, int y, MesquiteTool tool) {
		super.mouseExited(modifiers, x, y, tool);
		cursorExit();
	}
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		if (!MesquiteEvent.rightClick(modifiers) && y<= MINHEIGHT) {
			super.mouseUp(modifiers,  x,  y,  tool);
			return;
		}
	}
	public void dispose(){
		if (popup!=null)
			remove(popup);
		for (int i = 0; i<subPanels.size(); i++){
			ClosablePanel panel = ((ClosablePanel)subPanels.elementAt(i));
			panel.dispose();
		}
		super.dispose();
	}

}



