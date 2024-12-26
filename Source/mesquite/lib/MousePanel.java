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
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;

import mesquite.lib.duties.*;
import mesquite.lib.table.AutoScrollThread;


/* ======================================================================== */
/** This class adds drop down menu capabilities to panels.  A default component of the drop down
menu is the Font selection */

public class MousePanel extends Panel implements Commandable, FileDirtier, MouseMotionListener, MouseListener, DropTargetListener {
	MesquiteCommand downCommand, upCommand, dragCommand, movedCommand, clickedCommand, enteredCommand, exitedCommand;
	MesquiteCommand filesDroppedCommand, fileStringDroppedCommand;
	MesquiteCommand showWindowCommand;
	MesquiteDropManager dropManager;
	long moveFrequency, moveCount;
	boolean suppressEvents = false; //WWW
	private int currentX = -1;
	private int currentY = -1;
	int ps = 0;  //reserved for use in preferred with as window sidepanel
	private Cursor disabledCursor = null;
	protected DropTarget dropTarget;
	Vector dndObjectVector = new Vector();
	AutoScrollThread autoScrollThread=null;
	public static final int AUTOSCROLLOFF= 0;
	public static final int AUTOSCROLLBOTH = 1;
	public static final int AUTOSCROLLHORIZONTAL = 2;
	public static final int AUTOSCROLLVERTICAL = 3;
	private DragSource dragSource;
	private DragGestureListener dgListener; 
	private DragSourceListener dsListener;
	
	int autoscrollDirection = AUTOSCROLLBOTH;

	static long exited, clicked, entered,pressed, released, dragged, moved;
	static{
		exited = clicked= entered=pressed=released=dragged= moved=0;
	}
	public MousePanel() {
		super();

		try {
			setFocusTraversalKeysEnabled(false);
		}
		catch (Error e){
		}
		dropManager = new MesquiteDropManager();
		moveCount =0;
		moveFrequency = 0;
		setForeground(Color.black);
		addMouseMotionListener(this);
		addMouseListener(this);
		downCommand= MesquiteModule.makeCommand("mouseDown", this);
		upCommand= MesquiteModule.makeCommand("mouseUp", this);
		dragCommand= MesquiteModule.makeCommand("mouseDrag", this);
		movedCommand= MesquiteModule.makeCommand("mouseMoved", this);
		movedCommand.setLetMeFinish(false);
		clickedCommand= MesquiteModule.makeCommand("mouseClicked", this);
		enteredCommand= MesquiteModule.makeCommand("mouseEntered", this);
		enteredCommand.setLetMeFinish(false);
		exitedCommand= MesquiteModule.makeCommand("mouseExited", this);
		filesDroppedCommand= MesquiteModule.makeCommand("filesDropped", this);
		fileStringDroppedCommand= MesquiteModule.makeCommand("fileStringDropped", this);
		exitedCommand.setLetMeFinish(false);
		downCommand.setSuppressLogging(true);
		upCommand.setSuppressLogging(true);
		dragCommand.setSuppressLogging(true);
		movedCommand.setSuppressLogging(true);
		clickedCommand.setSuppressLogging(true);
		enteredCommand.setSuppressLogging(true);
		exitedCommand.setSuppressLogging(true);
		downCommand.hideInList = true;
		upCommand.hideInList = true;
		movedCommand.hideInList = true;
		clickedCommand.hideInList = true;
		enteredCommand.hideInList = true;
		exitedCommand.hideInList = true;
		setDontDuplicateCommands(false);
		disabledCursor = setupDisabledCursor("disabled.gif", "disabled", 4,2);
		if (disabledCursor ==null)
			disabledCursor = Cursor.getDefaultCursor();
		
		this.dragSource = DragSource.getDefaultDragSource();
//		this.dgListener = new DragGestureListener();
//		this.dsListener = new DragSourceAdapter();
	}
	public void dispose(){
		if (downCommand!=null)
			downCommand.dispose();
		if (upCommand!=null)
			upCommand.dispose();
		if (movedCommand!=null)
			movedCommand.dispose();
		if (clickedCommand!=null)
			clickedCommand.dispose();
		if (enteredCommand!=null)
			enteredCommand.dispose();
		if (exitedCommand!=null)
			exitedCommand.dispose();
		downCommand = null;
		upCommand = null;
		movedCommand = null;
		clickedCommand = null;
		enteredCommand = null;
		exitedCommand = null;
		removeMouseListener(this);
		if (autoScrollThread!=null)
			removeMouseListener(autoScrollThread);
	}
	public void setMoveFrequency(long mf){
		moveFrequency = mf;
	}
	public MesquiteWindow getMesquiteWindow() {
		Container c = getParent();
		while (c!=null && !(c instanceof OuterContentArea))
			c = c.getParent();
		if (c==null)
			return null;
		return ((OuterContentArea)c).ownerWindow;
	}
	boolean fontSet = false;
	public void setFont(Font f){
		super.setFont(f);
	}
	public void setPanelFont(Font f){
		fontSet = true;
		super.setFont(f);
	}
	public boolean fontExplicitlySet(){
		return fontSet;
	}
	public void update(Graphics paramGraphics){
		if (paramGraphics instanceof Graphics2D){
			Graphics2D g = (Graphics2D) paramGraphics;
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,  // to recover antialiasing in OS X java 1.7 and in Windows.  1.7 broke text rotation when not antialiased on OSX.
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}
		super.update(paramGraphics);
	}
	public Graphics getGraphics(){
		Graphics gg = super.getGraphics();
		if (gg instanceof Graphics2D){

			Graphics2D g = (Graphics2D)gg;
			if (g ==null)
				return null;
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}
		return gg;
	}
	/*.................................................................................................................*/
	public boolean canAutoscrollHorizontally() {
		return autoscrollDirection == AUTOSCROLLBOTH || autoscrollDirection == AUTOSCROLLHORIZONTAL;
	}
	/*.................................................................................................................*/
	public boolean canAutoscrollVertically() {
		return autoscrollDirection == AUTOSCROLLBOTH || autoscrollDirection == AUTOSCROLLVERTICAL;
	}
	/*.................................................................................................................*/
	public boolean askListenersToProcess(Object obj, boolean onlyOneProcessor) {
		if (dropManager!=null)
			return dropManager.askListenersToProcess(obj, onlyOneProcessor);
		return false;
	}
	/*.................................................................................................................*/
	public void addDropListener(MesquiteDropListener dropListener) {
		if (dropManager!=null)
			dropManager.addListener(dropListener);
	}

	/*.................................................................................................................*/
	public void removeDropListener(MesquiteDropListener dropListener) {
		if (dropManager!=null)
			dropManager.removeListener(dropListener);
	}


	/*.................................................................................................................*/

	public void addMouseListener(MouseListener l){
		if (l instanceof AutoScrollThread) {
			autoScrollThread = (AutoScrollThread)l;
		}
		super.addMouseListener(l);
	}


	public void deletePendingMoveDrag(){
		dragCommand.deleteOnQueue();
		movedCommand.deleteOnQueue();
	}
	public void setDontDuplicateCommands(boolean dd){
		downCommand.setDontDuplicate(dd);
		dragCommand.setDontDuplicate(dd);
		upCommand.setDontDuplicate(dd);
		movedCommand.setDontDuplicate(dd);
		clickedCommand.setDontDuplicate(dd);
		enteredCommand.setDontDuplicate(dd);
		exitedCommand.setDontDuplicate(dd);
	}
	public MesquiteWindow getWindow(){
		return MesquiteWindow.windowOfItem(this);
	}

	MesquiteTool getT(){
		try{
			Container c = this;
			while ((c= c.getParent())!=null){
				if (c instanceof OuterContentArea) {
					MesquiteTool tool = ((OuterContentArea)c).ownerWindow.getCurrentTool();
					return tool;
				}
			}
		}
		catch (Exception e){
		}
		return null;
	}
	Image disabledCursorImage = null;
	/*.................................................................................................................*/
	private Cursor setupDisabledCursor(String imageFileName, String name, int x, int y){  
		try { //just in case Java2 not available
			Image im =  disabledCursorImage;  
			if (im == null){
				String s=MesquiteModule.getRootImageDirectoryPath();
				Dimension best = Toolkit.getDefaultToolkit().getBestCursorSize(16, 16);
				if ((best.width>16 || best.height>16) && MesquiteFile.fileExists(MesquiteModule.getSizedRootImageFilePath(best.width, imageFileName))){
					im = MesquiteImage.getImage((MesquiteModule.getSizedRootImageFilePath(best.width, imageFileName)));
					if (im == null && s!=null)
						im = MesquiteImage.getImage(s + imageFileName);
				}
				else if (s!=null)
					im = MesquiteImage.getImage(s + imageFileName);
				//setCursorImage(im);
			}
			Point p = new Point(x, y);
			if (im== null)
				return null;
			disabledCursorImage = im;
			Cursor c = Toolkit.getDefaultToolkit().createCustomCursor(im, p, name);
			return c;
		}
		catch (Throwable t){
			return null;
		}
	}
	/*...............................................................................................................*/
	public Cursor getDisabledCursor() {
		return disabledCursor;
	}

	public void setCursor(Cursor c){
		super.setCursor(c);
	}
	public int getMouseX(){
		return currentX;
	}
	public int getMouseY(){
		return currentY;
	}
	public void setVisible(boolean vis){
		super.setVisible(vis);
		invalidate(); //to workaround bug in Jaguar
		validate(); //to workaround bug in Jaguar
	}

	ObjectContainer  findObjectContainer(int objectID) {
		if (dndObjectVector==null)
			return null;
		for (int i = 0; i<dndObjectVector.size(); i++) {
			ObjectContainer oc = (ObjectContainer)dndObjectVector.get(i);
			if (objectID==oc.getID())
				return oc;
		}
		return null;
	}

	void  deleteObjectContainer(int objectID) {
		if (dndObjectVector==null)
			return;
		for (int i = 0; i<dndObjectVector.size(); i++) {
			ObjectContainer oc = (ObjectContainer)dndObjectVector.get(i);
			if (objectID==oc.getID())
				dndObjectVector.remove(i);
		}
	}

	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (MesquiteWindow.checkDoomed(this))
			return null;
		if (checker.compare(this.getClass(), "Mouse down", "[modifiers as integer][click count][when][x][y]", commandName, "mouseDown")) {
			int modifiers = MesquiteInteger.fromString(ParseUtil.getFirstToken(arguments, pos));
			int clickCount = MesquiteInteger.fromString(arguments, pos);
			long when = MesquiteLong.fromString(ParseUtil.getToken(arguments, pos));
			int x = MesquiteInteger.fromString(arguments, pos);
			int y = MesquiteInteger.fromString(arguments, pos);
			MesquiteException.lastLocation = 101;
			requestFocus();
			try{
				mouseDown(modifiers, clickCount, when, x, y, getT());
			}
			catch (Exception e){
			}
			MesquiteException.lastLocation = 0;
		}
		else if (checker.compare(this.getClass(), "Mouse up", "[modifiers as integer][x][y]", commandName, "mouseUp")) {
			int modifiers = MesquiteInteger.fromString(ParseUtil.getFirstToken(arguments, pos));
			int x = MesquiteInteger.fromString(arguments, pos);
			int y = MesquiteInteger.fromString(arguments, pos);
			MesquiteException.lastLocation = 102;
			try{
				mouseUp(modifiers, x, y, getT());
			}
			catch (Exception e){
			}
			MesquiteException.lastLocation = 0;
		}
		else if (checker.compare(this.getClass(), "Mouse drag", "[modifiers as integer][x][y]", commandName, "mouseDrag")) {
			int modifiers = MesquiteInteger.fromString(ParseUtil.getFirstToken(arguments, pos));
			int x = MesquiteInteger.fromString(arguments, pos);
			int y = MesquiteInteger.fromString(arguments, pos);
			MesquiteException.lastLocation = 103;
			try{
				mouseDrag(modifiers, x, y, getT());
			}
			catch (Exception e){
			}
			MesquiteException.lastLocation = 0;
		}
		else if (checker.compare(this.getClass(), "Mouse moved", "[modifiers as integer][x][y]", commandName, "mouseMoved")) {
			int modifiers = MesquiteInteger.fromString(ParseUtil.getFirstToken(arguments, pos));
			int x = MesquiteInteger.fromString(arguments, pos);
			int y = MesquiteInteger.fromString(arguments, pos);
			MesquiteTool t = getT();
			MesquiteException.lastLocation = 104;
			if (t!=null)
				t.cursorInPanel(modifiers, x, y, this, true);
			try{
				mouseMoved(modifiers, x, y, getT());
			}
			catch (Exception e){
			}
			MesquiteException.lastLocation = 0;
		}
		else if (checker.compare(this.getClass(), "Mouse entered", "[modifiers as integer][x][y]", commandName, "mouseEntered")) {
			int modifiers = MesquiteInteger.fromString(ParseUtil.getFirstToken(arguments, pos));
			int x = MesquiteInteger.fromString(arguments, pos);
			int y = MesquiteInteger.fromString(arguments, pos);
			MesquiteException.lastLocation = 107;
			MesquiteTool t = getT();
			if (t!=null)
				t.cursorInPanel(modifiers, x, y, this, true);
			try{
				mouseEntered(modifiers, x, y, getT());
			}
			catch (Exception e){
			}
			MesquiteException.lastLocation = 0;
		}
		else if (checker.compare(this.getClass(), "Mouse exited", "[modifiers as integer][x][y]", commandName, "mouseExited")) {
			int modifiers = MesquiteInteger.fromString(ParseUtil.getFirstToken(arguments, pos));
			int x = MesquiteInteger.fromString(arguments, pos);
			int y = MesquiteInteger.fromString(arguments, pos);
			MesquiteException.lastLocation = 105;
			MesquiteTool t = getT();
			if (t!=null)
				t.cursorInPanel(modifiers, x, y, this, false);
			try{
				mouseExited(modifiers, x, y, getT());
			}
			catch (Exception e){
			}
			MesquiteException.lastLocation = 0;
		}
		else if (checker.compare(this.getClass(), "Mouse clicked", "[modifiers as integer][x][y]", commandName, "mouseClicked")) {
			int modifiers = MesquiteInteger.fromString(ParseUtil.getFirstToken(arguments, pos));
			int x = MesquiteInteger.fromString(arguments, pos);
			int y = MesquiteInteger.fromString(arguments, pos);
			MesquiteException.lastLocation = 106;
			try{
				mouseClicked(modifiers, x, y, getT());
			}
			catch (Exception e){
			}
			MesquiteException.lastLocation = 0;
		}
		else if (checker.compare(this.getClass(), "File String Dropped into Panel", "[ObjectContainer ID]", commandName, "fileStringDropped")) {
			int objectID = MesquiteInteger.fromString(ParseUtil.getFirstToken(arguments, pos));
			ObjectContainer objectContainer = findObjectContainer(objectID);
			if (objectContainer!=null) {
				String path = (String)objectContainer.getObject();

				processFileStringDroppedOnPanel(path);

				deleteObjectContainer(objectID);
			}

		}
		else if (checker.compare(this.getClass(), "Files Dropped into Panel", "[ObjectContainer ID]", commandName, "filesDropped")) {
			int objectID = MesquiteInteger.fromString(ParseUtil.getFirstToken(arguments, pos));
			ObjectContainer objectContainer = findObjectContainer(objectID);
			if (objectContainer!=null) {
				List files = (List)objectContainer.getObject();

				processFilesDroppedOnPanel(files);

				deleteObjectContainer(objectID);
			}
			//data.notifyListeners(this, new Notification(CharacterData.DATA_CHANGED, null, null));
		}
		MesquiteWindow.uncheckDoomed(this);
		return null;
	}
	/*.................................................................................................................*/
	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
	}
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
	}
	public void mouseDrag(int modifiers, int x, int y, MesquiteTool tool) {
	}
	public void mouseMoved(int modifiers, int x, int y, MesquiteTool tool) {
	}
	public void mouseClicked(int modifiers, int x, int y, MesquiteTool tool) {
	}
	public void mouseEntered(int modifiers, int x, int y, MesquiteTool tool) {
	}
	public void mouseExited(int modifiers, int x, int y, MesquiteTool tool) {
	}

	/*...............................................................................................................*/
	public void mouseClicked(MouseEvent e)   {
		if (MesquiteDialog.currentWizard != null){
			MesquiteDialog.currentWizard.toFront();
			return;
		}
		if (suppressEvents || clickedCommand == null)
			return;
		currentX = e.getX();
		currentY = e.getY();
		MesquiteException.lastLocation = 108;
		clickedCommand.doItMainThread(Integer.toString(MesquiteEvent.getModifiers(e)) + " " +  e.getX() + " " + e.getY(), null, false, false);
		MesquiteException.lastLocation = 0;
	}
	/*...............................................................................................................*/
	public void mouseEntered(MouseEvent e)   {
		if (MesquiteDialog.currentWizard != null){
			return;
		}

		if (suppressEvents || enteredCommand == null)
			return;
		currentX = e.getX();
		currentY = e.getY();
		MesquiteException.lastLocation = 109;
		enteredCommand.doItMainThread(Integer.toString(MesquiteEvent.getModifiers(e)) + " " +  e.getX() + " " + e.getY(), null, false, false);
		MesquiteException.lastLocation = 0;
	}
	/*...............................................................................................................*/
	public void mouseExited(MouseEvent e)   {
		if (MesquiteDialog.currentWizard != null){
			return;
		}
		if (suppressEvents || exitedCommand == null)
			return;
		currentX = e.getX();
		currentY = e.getY();
		MesquiteException.lastLocation = 110;
		exitedCommand.doItMainThread(Integer.toString(MesquiteEvent.getModifiers(e)) + " " +  e.getX() + " " + e.getY(), null, false, false);
		MesquiteException.lastLocation = 0;
		currentX = MesquiteInteger.unassigned;
		currentY = MesquiteInteger.unassigned;
	}
	/*...............................................................................................................*/
	/* these events are handled via the MesquiteCommand system so that they can be put on the thread cue to prevent interference with already running threads */
	public void  mousePressed(MouseEvent e)   {
		if (MesquiteDialog.currentWizard != null){
			MesquiteDialog.currentWizard.toFront();
			return;
		}
		if (suppressEvents || downCommand == null)
			return;
		currentX = e.getX();
		currentY = e.getY();
		MesquiteException.lastLocation = 111;
		downCommand.doItMainThread(Integer.toString(MesquiteEvent.getModifiers(e)) + " " + e.getClickCount() + " " + e.getWhen() + " " +  e.getX() + " " + e.getY(), null, false, false);
		MesquiteException.lastLocation = 0;
	}

	/*...............................................................................................................*/
	public void mouseReleased(MouseEvent e)  {
		if (MesquiteDialog.currentWizard != null){
			MesquiteDialog.currentWizard.toFront();
			return;
		}
		if (suppressEvents || upCommand == null)
			return;
		currentX = e.getX();
		currentY = e.getY();
		MesquiteWindow w = MesquiteWindow.windowOfItem(this);
		if (w != null && w.setAsFrontOnClickAnyContent() && w.getParentFrame().frontWindow != w){
			if (showWindowCommand == null)
				showWindowCommand = new MesquiteCommand("setAsFront", w);
			showWindowCommand.doItMainThread("", null, false, false);
		}
		upCommand.doItMainThread(Integer.toString(MesquiteEvent.getModifiers(e)) + " "  +  e.getX() + " " + e.getY(), null, false, false);
		currentX = MesquiteInteger.unassigned;
		currentY = MesquiteInteger.unassigned;
	}
	/*...............................................................................................................*/
	public void mouseDragged(MouseEvent e)  {
		if (MesquiteDialog.currentWizard != null){
			MesquiteDialog.currentWizard.toFront();
			return;
		}
		if (suppressEvents || dragCommand == null)
			return;
		currentX = e.getX();
		currentY = e.getY();
		MesquiteException.lastLocation = 112;
		if (moveFrequency == 0 || moveCount++ % moveFrequency ==0) {
			dragCommand.doItMainThread(Integer.toString(MesquiteEvent.getModifiers(e)) + " "  +  e.getX() + " " + e.getY(), null, false, false);
		}
		MesquiteException.lastLocation = 0;
	}
	/*...............................................................................................................*/
	public void mouseMoved(MouseEvent e) {
		if (MesquiteDialog.currentWizard != null){
			return;
		}
		if (suppressEvents || movedCommand == null)
			return;
		currentX = e.getX();
		currentY = e.getY();
		MesquiteException.lastLocation = 113;
		if (moveFrequency == 0 || moveCount++ % moveFrequency ==0) {
			movedCommand.doItMainThread(Integer.toString(MesquiteEvent.getModifiers(e)) + " " +  e.getX() + " " + e.getY(), null, false, false);
		}
		MesquiteException.lastLocation = 0;
	}


	/*.................................................................................................................*/
	/** For FileDirtier interface */
	public void fileDirtiedByCommand(MesquiteCommand command){
		if (command != null && command.getOwner() == this && ("mouseMoved".equalsIgnoreCase(command.getName()) || "mouseExited".equalsIgnoreCase(command.getName()) || "mouseEntered".equalsIgnoreCase(command.getName())))
			return;
		MesquiteWindow w = MesquiteWindow.windowOfItem(this);
		if (w!=null)
			w.fileDirtiedByCommand(command);
	}
	public void printAll(Graphics g){
		print(g);
		printComponents(g);
	}
	// ----------------------- DND Support -------------------------------
	public void dragEnter(DropTargetDragEvent dtde) {
		dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
	}
	public void dragOver(DropTargetDragEvent dtde) {
	}
	public void dropActionChanged(DropTargetDragEvent dtde) {
	}
	public void dragExit(DropTargetEvent dte) {
	}
	/*..................................................................*/
	public void drop(DropTargetDropEvent e) {
		Transferable t = e.getTransferable();	
		DataFlavor flavors[] = e.getTransferable().getTransferDataFlavors();
		for (int i = 0; i < flavors.length; i++) {
			DataFlavor flavor = flavors[i];
			//system.out.println("flavor's name is: " + flavor.getHumanPresentableName());
		}		
		if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			//system.out.println("drop action is: " + e.getDropAction());
			//system.out.println("file list supported");
			e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
			//system.out.println("drop accepted");
			List files = null;
			try {
				//system.out.println("going to get transfer data");
				files = (List) t.getTransferData(DataFlavor.javaFileListFlavor);				
			} catch (Exception e1) {
				//system.out.println("exception!");
				e1.printStackTrace();
			}
			//system.out.println("have files");
			if (files != null) {
				handleDroppedFileList(files);
			} else {
				//system.out.println("files null");
			}
		} else if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);			
			StringReader fileContents = null;
			try {
				fileContents = (StringReader) t.getTransferData(DataFlavor.stringFlavor);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			handleDroppedFileStringContents(fileContents);
		}
		e.dropComplete(true);
	}
	/*..................................................................*/
	private void handleDroppedFileStringContents(StringReader fileContents) {
		int nextByte;
		List bytes = new ArrayList();
		try {
			while ((nextByte = fileContents.read()) != -1) {
				// need to mask it if it's below 0
				if (nextByte < 0) {
					nextByte = nextByte & 0xff;
				}
				bytes.add(new Integer(nextByte));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] byteArray = new byte[bytes.size()];
		int i = 0;
		for (Iterator iter = bytes.iterator(); iter.hasNext();) {
			Integer nextInteger = (Integer) iter.next();
			byteArray[i++] = nextInteger.byteValue();
		}
		try {
			String path = new String(byteArray, "ISO-8859-1");
			handleDroppedFileString(path);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}


	/**
	 * Action method that acts upon a string that
	 * was dropped onto the panel.  For Linux, the string
	 * is a local url to the filename i.e. 
	 * file:///home/dmandel/Desktop/test.txt
	 * @param string
	 */
	/*..................................................................*/
	protected void handleDroppedFileString(String path) {
		try {
			ObjectContainer droppedObject = new ObjectContainer(path);
			dndObjectVector.add(droppedObject);
			fileStringDroppedCommand.doItMainThread(""+ droppedObject.getID(), null, false, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	/**
	 * For OS X, when file(s) are dropped on the panel,
	 * they are passed as a List of file objects that can then
	 * be acted upon
	 * @param files
	 */
	/*..................................................................*/
	protected void handleDroppedFileList(List files) {
		if (!(this instanceof MesquiteDroppedFileHandler)) {
			MesquiteMessage.warnProgrammer("DropTargetListener for files must implement MesquiteDroppedFileHandler");
			return;
		}
		ObjectContainer droppedObject = new ObjectContainer(files);
		dndObjectVector.add(droppedObject);
		filesDroppedCommand.doItMainThread(""+ droppedObject.getID(), null, false, false);
	}


	/*..................................................................*/
	/**
	 * Method for panels to override in order to do determine 
	 * the fileInterpreter needed for dropped file contents
	 * @param droppedContents
	 */
	public FileInterpreter findFileInterpreter(String droppedContents, String fileName) {
		return null;
	}
	/**
	 * Method for panels to override in order to do something
	 * with dropped file contents
	 * @param droppedContents
	 */
	public void actUponDroppedFileContents(FileInterpreter fileInterpreter, String path) {
		MesquiteMessage.warnProgrammer("Uncaught Dropped File in Mesquite Panel");
	}
	/*.................................................................................................................*/
	public void processFilesDroppedOnPanel(List files) {
		MesquiteMessage.warnProgrammer("Unprocessed Dropped File in Mesquite Panel");

	}
	/*.................................................................................................................*/
	public void processFileStringDroppedOnPanel(String path) {
		MesquiteMessage.warnProgrammer("Unprocessed Dropped File String in Mesquite Panel");

	}
	public int getAutoscrollDirection() {
		return autoscrollDirection;
	}
	public void setAutoscrollDirection(int autoscrollDirection) {
		this.autoscrollDirection = autoscrollDirection;
	}

}

