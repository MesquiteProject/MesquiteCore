/* Mesquite source code.  Copyright 1997-2005 W. Maddison and D. Maddison. 
Version 1.06, August 2005.
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

/* еееееееееееееееееееееееееее commands еееееееееееееееееееееееееееееее */
/* includes commands,  buttons, miniscrolls*/
/* ======================================================================== */
/** A tool (i.e. cursor) that can be active and used on items on the screen.*/
public class MesquiteTool implements Listable, Explainable, ImageOwner, KeyListener {
	protected String name; 
	protected String imageDirectoryPath;
	protected String imageFileName;
	protected String fullDescription;
	protected String explanation;
	protected String optionImageFileName;
	protected ToolPalette palette;
	private boolean inUse = false;
	boolean amIArrowTool = false;
	private ToolKeyListener toolKeyListener;
	MesquiteCommand optionsCommand;
	MesquiteModule popupOwner;
	MesquiteButton button;
	Point hotSpot, optionHotSpot;
	boolean optionDown = false;
	Image cursorImage = null;
	boolean[] currentKeyCodesDown, currentKeyCharsDown;
	private boolean allowAnnotate = false;
	MesquiteCursor standardCursor;
	protected MesquiteCursor optionCursor;
	MesquiteCursor currentStandardCursor;
	MesquiteCursor currentOptionCursor;
	protected Object initiator;
	protected boolean enabled=true;
	protected boolean onlyWorksWhereSpecified = false;
	
	public MesquiteTool (Object initiator, String name, String imageDirectoryPath, String imageFileName, int hotX, int hotY, String fullDescription, String explanation) {
		if (initiator !=null)
			this.name = initiator.getClass().getName() + "." + name;
		else
			this.name = name;
		this.initiator = initiator;
		this.imageDirectoryPath = imageDirectoryPath;
		this.imageFileName = imageFileName;
		this.fullDescription = fullDescription;
		this.explanation = explanation;
		hotSpot = new Point(hotX, hotY);
		optionHotSpot = new Point(hotX, hotY);
		currentKeyCodesDown = new boolean[512];
		currentKeyCharsDown = new boolean[512];
		standardCursor = new MesquiteCursor(initiator, name, imageDirectoryPath, imageFileName, hotX, hotY);
		currentStandardCursor = standardCursor;
		
	}
	
	public void setAllowAnnotate(boolean allow){
		allowAnnotate = allow;
	}
	public boolean getAllowAnnotate(){
		return allowAnnotate;
	}
	public void setCursorImage(Image i){
		cursorImage = i;
	}
	public Image getCursorImage(){
		return cursorImage;
	}
	public void setOptionDown(boolean od){
		optionDown = od;
	}
	public String getName(){
		return name;
	}
	public void setButton(MesquiteButton button){
		this.button = button;
	}
	public MesquiteButton getButton(){
		return button;
	}
	public void setPalette(ToolPalette palette){
		this.palette = palette;
	}
	public ToolPalette getPalette(){
		return palette;
	}
	public void setEnabled(boolean enabled){
		boolean oldEnabled = this.enabled;
		this.enabled = enabled;
		if (oldEnabled!=enabled) {
			if (getButton()!=null)
				getButton().repaint();
			if (!enabled && getPalette()!=null)  //turning off
				getPalette().setToDefaultTool();
		}
	}
	public boolean getEnabled(){
		return enabled;
	}
	
	
	//can be overridden to, for instance, change the cursor
	public void cursorInPanel(int modifiers, int x, int y, MousePanel panel, boolean in){
	}
	
	public void setIsArrowTool(boolean a){
		amIArrowTool = a;
	}
	public boolean isArrowTool(){
		return amIArrowTool;
	}
	public void setHotSpot(int x, int y){
		hotSpot.x = x;
		hotSpot.y = y;
	}
	public Point getHotSpot(){
		if (optionDown && optionImageFileName!=null)
			return optionHotSpot;
		return hotSpot;
	}
	public MesquiteCommand getOptionsCommand(){
		return optionsCommand;
	}
	public void setOptionsCommand(MesquiteCommand oc){
		optionsCommand = oc;
	}
	public void setPopUpOwner(MesquiteModule own){
		popupOwner = own;
	}
	public MesquiteModule getPopUpOwner(){
		return popupOwner;
	}
	public void chooseOptions(Container c, int x, int y){
		if (optionsCommand!=null)
			optionsCommand.doItMainThread(Integer.toString(x) + " " + y + " " + ParseUtil.tokenize(getName()), CommandChecker.getQueryModeString("Tool", optionsCommand, this), this);
		else if (popupOwner!=null)
			popupOwner.showPopUp(c, x+5, y+5);  
	}
	public void setDescription(String text){
		this.fullDescription = text;
	}
	public String getDescription(){
		return fullDescription;
	}
	/*.................................................................................................................*/
	public Cursor getCursor(){  
		if (optionDown && currentOptionCursor!=null) 
			return currentOptionCursor.getCursor();
		else
			return currentStandardCursor.getCursor();
	}
	/*.................................................................................................................*
	public Cursor getCursor(Image im, String imageFileName, Point hotSpot){  
		try { //just in case Java2 not available
			if (im == null){
				Dimension best = Toolkit.getDefaultToolkit().getBestCursorSize(16, 16);
				if ((best.width>16 || best.height>16) && MesquiteFile.fileExists(getSizedImagePath(best.width, imageFileName))){
					im = MesquiteImage.getImage(getSizedImagePath(best.width, imageFileName));
					if (im == null)
						im = MesquiteImage.getImage(getImagePath(imageFileName));
				}
				else 
					im = MesquiteImage.getImage(getImagePath(imageFileName));
				setCursorImage(im);
			}
			if (!MesquiteImage.waitForImageToLoad(im)) {
				MesquiteMessage.println("Note: image of cursor of tool not obtained: " + getName() + "  " + getImagePath(imageFileName));
				return Cursor.getDefaultCursor();
			}
			
			Cursor c = Toolkit.getDefaultToolkit().createCustomCursor(im, hotSpot, getName());
			return c;
		}
		catch (Throwable t){
			return Cursor.getDefaultCursor();
		}
	}
	/*.................................................................................................................*/
	public String getExplanation(){
		if (explanation==null)
			return getDescription();
		else
			return explanation; 
	}
	public void setInUse(boolean inUse){
		this.inUse = inUse;
	}
	public boolean getInUse(){
		return inUse;
	}
	public void setOptionImageFileName(String path, int x, int y){
		optionImageFileName = path;
		optionHotSpot.x = x;
		optionHotSpot.y = y;
		optionCursor = new MesquiteCursor(null, name, imageDirectoryPath, path, x, y);
		currentOptionCursor = optionCursor;
	}
	public void setCurrentOptionCursor(MesquiteCursor mc){
		if (mc==null)
			currentOptionCursor = optionCursor;
		else
			currentOptionCursor = mc;
	}
	public void setCurrentStandardCursor(MesquiteCursor mc){
		if (mc==null)
			currentStandardCursor = standardCursor;
		else
			currentStandardCursor = mc;
	}
	public String getOptionImagePath(){
		if (optionImageFileName ==null)
			return null;
		return imageDirectoryPath + optionImageFileName;
	}
	public String getSizedOptionImagePath(int s){
		if (optionImageFileName ==null)
			return null;
		return imageDirectoryPath + s + optionImageFileName;
	}
	public String getImagePath(){
		if (optionDown && getOptionImagePath()!=null) {
			return getOptionImagePath();
		}
		return imageDirectoryPath + imageFileName;
	}
	/*.................................................................................................................*/
	public String getImagePath(String imageFileName){
		return imageDirectoryPath + imageFileName;
	}
	public void setImageFileName(String name){
		imageFileName = name;
		cursorImage = null; // to force reloading
		if (button !=null) {
			button.resetImage(getImagePath());
		}
	}
	/*.................................................................................................................*/
	public String getSizedImagePath(int s, String imageFileName){
		return imageDirectoryPath + s + imageFileName;
	}
	public String getSizedImagePath(int s){
		if (optionDown && getSizedOptionImagePath(s)!=null) {
			return getSizedOptionImagePath(s);
		}
		return imageDirectoryPath + s + imageFileName;
	}
	
	public void setToolKeyListener(ToolKeyListener  t){ 
		toolKeyListener = t;
	}
	
	/**Returns whether or not Tool considers that a particular key is down and not yet released; by keyCode.  Note this does not distinguish upper case from lower case letter*/
	public boolean keyCodeDown(int kc){ 
		if (kc>=0 && kc<currentKeyCodesDown.length)
			return currentKeyCodesDown[kc];
		return false;
	}
	/**Returns whether or not Tool considers that a particular key is down and not yet released; by keyChar. Note this does not report modifiers*/
	public boolean keyCharDown(int kc){ 
		if (kc>=0 && kc<currentKeyCharsDown.length)
			return currentKeyCharsDown[kc];
		return false;
	}
	/*-------*/
	public void keyTyped(KeyEvent e){
		if (toolKeyListener!=null)
			toolKeyListener.keyTyped(e, this);
	}
	
	public void keyPressed(KeyEvent e){
		int kc = e.getKeyCode();
		if (kc>=0 && kc<currentKeyCodesDown.length)
			currentKeyCodesDown[kc] = true;
		int kch = e.getKeyChar();
		if (kch>=0 && kch<currentKeyCharsDown.length)
			currentKeyCharsDown[kch] = true;
		if (toolKeyListener!=null)
			toolKeyListener.keyPressed(e, this);
	}
	public void keyReleased(KeyEvent e){
		int kc = e.getKeyCode();
		if (kc>=0 && kc<currentKeyCodesDown.length)
			currentKeyCodesDown[kc] = false;
		int kch = e.getKeyChar();
		if (kch>=0 && kch<currentKeyCharsDown.length)
			currentKeyCharsDown[kch] = false;
		if (toolKeyListener!=null)
			toolKeyListener.keyReleased(e, this);
	}
	public void dispose(){ 
	}

	public boolean getOnlyWorksWhereSpecified() {
		return onlyWorksWhereSpecified;
	}

	public void setOnlyWorksWhereSpecified(boolean onlyWorksWhereSpecified) {
		this.onlyWorksWhereSpecified = onlyWorksWhereSpecified;
	}
}
