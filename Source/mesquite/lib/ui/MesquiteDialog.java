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
import java.awt.event.*;
import java.util.Vector;

import mesquite.lib.CommandChecker;
import mesquite.lib.CommandRecord;
import mesquite.lib.Commandable;
import mesquite.lib.ConsoleThread;
import mesquite.lib.Listable;
import mesquite.lib.ListableVector;
import mesquite.lib.MainThread;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteThread;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.ParseUtil;
import mesquite.lib.Parser;
import mesquite.lib.StringUtil;

/* =============================================== */
/** A dialog box */
public abstract class MesquiteDialog implements Commandable, Listable, ComponentListener, KeyListener, MouseListener {
	static Vector dialogListeners = new Vector();
	static int numCreated = 0;

	int id = 0;

	boolean alreadyDisposed = false;

	protected String defaultButtonString = null;

	int savedX = 0;

	int savedY = 0;

	public int tickled = 0;

	boolean autoDispose = true;
	String title;

	Thread thread;

	boolean holdsConsoleFocus = false;
	protected MesquiteDialogParent parentDialog;
	MDPanel outerContents;

	public static boolean responsePending = false;  //is there currently a dialog with response pending?

	public static boolean useWizards = false;  //enable wizards in general
	boolean usingWizard = false;
	boolean waitingOnButtonPush = false;
	public static final int wizardWidth = 700;
	public static final int wizardHeight = 500;
	public static MesquiteDialogParent currentWizard;

	public MesquiteDialog(Object parent, String title) {
		this(parent, title, false);
	}
	public MesquiteDialog(Object parent, String title, boolean suppressWizardIfFirst) {
		//super(MesquiteWindow.dialogAnchor, title, true);
		numCreated++;
		id = numCreated;
		this.title = title;
		CommandRecord rec = CommandRecord.getRecDIfNull();
		parentDialog = rec.getWizard();
		if (parentDialog ==null) {
			if (currentWizard== null && !suppressWizardIfFirst && rec.establishWizard()){
				usingWizard = true;
			}
			Frame f = MesquiteWindow.dialogAnchor;
			if (parent != null){
				if (parent instanceof Frame)
					f = (Frame)parent;
				else if (parent instanceof MesquiteWindow){
					MesquiteWindow mw = ((MesquiteWindow)parent);
					if (!mw.isVisible() && mw.getOwnerModule() != null)
						mw = mw.getOwnerModule().visibleContainerOfModule();
					if (mw == null) {
						f = MesquiteWindow.dialogAnchor;
					}
					else
						f = mw.getParentFrame();
				}
			}
			if (f == MesquiteWindow.dialogAnchor && (MesquiteTrunk.debugMode))
				MesquiteMessage.printStackTrace("#################DIALOG ANCHOR USED FOR " + title); 

			parentDialog = new MesquiteDialogParent(f, title, !usingWizard);
			//parentDialog = new MesquiteDialogParent(MesquiteWindow.dialogAnchor, title, !usingWizard);
			MesquiteWindow.numDialogs++;
			if (!usingWizard)
				MesquiteWindow.numNonWizardDialogs++;
			if (MesquiteModule.mesquiteTrunk.dialogVector != null)
				MesquiteModule.mesquiteTrunk.dialogVector.addElement(this, false);
			if (usingWizard){
				rec.setWizard(parentDialog);
				rec.requestEstablishWizard(false);
				parentDialog.setSize(wizardWidth, wizardHeight);
				parentDialog.setAsWizard();

			}
		}
		else {
			usingWizard = true;

		}

		setTitle(title);
		outerContents = new MDPanel(usingWizard);
		outerContents.setForeground(Color.black);
		outerContents.addComponentListener(this);
		setBackground(ColorTheme.getInterfaceBackgroundPale());
		//	setBackground(ColorTheme.getInterfaceBackground());
		outerContents.setCursor(Cursor.getDefaultCursor());
		parentDialog.add(this);



		if (!usingWizard)
			MainThread.incrementSuppressWaitWindow();
		alreadyDisposed = false;
		// MesquiteWindow.dialogAnchor.toFront();

	}
	public static void closeWizard(){
		if (MesquiteDialog.currentWizard != null)
			MesquiteDialog.currentWizard.dispose();
	}
	public static void hideWizardForCalculation(){
		if (currentWizard != null && !MesquiteTrunk.isMacOSX()){
			currentWizard.hideForCalculation();
		}
	}
	public static void cleanUpWizard(){
		CommandRecord rec = CommandRecord.getRecDIfNull();
		rec.requestEstablishWizard(false);
		MesquiteDialogParent dlog = rec.getWizard();
		if (dlog != null) {
			if (dlog.hiddenForCalculation)
				dlog.setVisible(true);
			MesquiteDialog currentDialog =dlog.getCurrentDialog();
			if (currentDialog != null){
				currentDialog.usingWizard = false;
				currentDialog.dispose();
			}
			else 
				dlog.pleaseDispose();
		}
		rec.setWizard(null);

		MesquiteDialog.currentWizard = null;

	}
	public boolean isAlreadyDisposed(){
		return alreadyDisposed;
	}
	public boolean isInWizard(){
		return usingWizard;
	}
	public void setInWizard(boolean w){
		usingWizard = w;
	}

	public static void addDialogListener(DialogListener dlogL){
		dialogListeners.addElement(dlogL);
	}
	public static void removeDialogListener(DialogListener dlogL){
		dialogListeners.addElement(dlogL);
	}

	public void repaintAll(){
		if (outerContents != null)
			MesquiteWindow.rpAll(outerContents);
	}
	/*=====@@@@@@@@@@@@@@@============*/
	public Font getFont(){
		return outerContents.getFont();
	}
	public void setFont(Font f){
		outerContents.setFont(f);
	}
	public void setBackground(Color c){
		outerContents.setBackground(c);
	}
	public Color getBackground (){
		return outerContents.getBackground();
	}
	public void add(Component c){
		outerContents.add(c);
	}
	public void add(Component c, String s){
		outerContents.add(c, s);
	}
	public void add(String s, Component c){
		outerContents.add(s, c);
	}

	public void setLayout(LayoutManager f){
		outerContents.setLayout(f);
	}
	public void repaint(){
		outerContents.repaint();
	}
	public void doLayout(){
		outerContents.doLayout();
	}

	public Graphics getGraphics(){
		return outerContents.getGraphics();
	}
	public Component getOuterContents(){
		return outerContents;
	}
	public Component[] getComponents(){
		return outerContents.getComponents();
	}
	public void setCursor(Cursor c){
		outerContents.setCursor(c);
	}

	/*=====@@@@@@@@@@@@@@@============*/
	public Point getLocation(){
		return parentDialog.getLocation();
	}
	public Rectangle getBounds(){
		Rectangle bounds = parentDialog.getBounds();
		return bounds;
	}
	public void setDialogLocation(int x, int y) {
		savedX = x;
		savedY = y;
		//		parentDialog.setLocationSet(true);
		parentDialog.setLocation(x, y);
	}
	public void addWindowListener(WindowListener w){
		parentDialog.addWindowListener(w);
	}
	public void setResizable(boolean f){
		parentDialog.setResizable(f);
	}
	public void invalidate(){
		parentDialog.invalidate();
	}
	public void validate(){
		parentDialog.validate();
	}
	public void requestFocus(){
		parentDialog.pleaseRequestFocus();
	}

	public void pack(){
		//	if (!usingWizard)
		parentDialog.pack();
	}
	public Dimension getPreferredSize(boolean contentsOnly){
		return parentDialog.getPreferredSize(contentsOnly);
	}
	public Dimension getPreferredSize(){
		return parentDialog.getPreferredSize();
	}
	public void setDialogSize(Dimension h){
		parentDialog.setSize(h);
	}
	public void setDialogSize(int w, int h){
		parentDialog.setSize(w,h);
	}
	public void setDialogBounds(int x, int y, int w, int h){
		/*if (usingWizard && parentDialog.isLocationSet()){
			parentDialog.setSize(w,h);
			return;
		}*/
		parentDialog.setBounds(x, y, w,h);
	}
	public void setSize(Dimension h){
		if (!usingWizard)
			parentDialog.setSize(h);
	}
	public void setSize(int w, int h){
		if (!usingWizard)
			parentDialog.setSize(w,h);
	}
	public void setBounds(int x, int y, int w, int h){
		/*if (usingWizard && parentDialog.isLocationSet()){
			parentDialog.setSize(w,h);
			return;
		}*/
		if (!usingWizard)
			parentDialog.setBounds(x, y, w,h);
	}
	public void toFront(){
		parentDialog.pleaseBringToFront();
	}
	public MesquiteDialogParent getParentDialog(){
		return parentDialog;
	}
	public Dimension getSize(){
		return parentDialog.getSize();
	}
	public void setTitle(String s){
		parentDialog.setTitle(s);
	}
	public String getTitle(){
		return parentDialog.getTitle();
	}
	/*=====@@@@@@@@@@@@@@@============*/
	public void showDialog(){
		setResponsePending(true);

		if (usingWizard){
			if (!(Thread.currentThread() instanceof MesquiteThread))
				MesquiteMessage.printStackTrace("Wizard show on non-MT");
			if (isVisible())
				toFront();

			else 
				parentDialog.pleaseShow();  //called so as not to hang this thread (in case a wizard is being used and it needs to return on button hit
			try {

				if (waitingOnButtonPush)

					MesquiteMessage.printStackTrace("Wizard open waitingOnButtonPush already true");				
				waitingOnButtonPush = true;
				while (waitingOnButtonPush){
					Thread.sleep(20);
				}

			}
			catch(InterruptedException e){
			}
		}
		else {
			repaintAll();
			parentDialog.setVisible(true);
			
		}

	}
	public void hide(){
		setResponsePending(false);

		if (!usingWizard){
			parentDialog.hide();
		}
		else {
			//outerContents.setVisible(false);
			parentDialog.removeDialog(this);
			waitingOnButtonPush = false;
		}
	}
	public boolean isVisible(){
		return parentDialog.isVisible();
	}
	public void setVisible(boolean vis) {
		setResponsePending(vis);
		if (!vis) {
			waiting = false;
			if (!usingWizard){
				parentDialog.setVisible(false);
			}
			else {
				//outerContents.setVisible(false);
				parentDialog.removeDialog(this);
				if (!(Thread.currentThread() instanceof MesquiteThread))
					MesquiteMessage.printStackTrace("Wizard close on non-MT");
				if (!waitingOnButtonPush)

					MesquiteMessage.printStackTrace("Wizard close waitingOnButtonPush already false");
				waitingOnButtonPush = false;
			}
		}
		else {
			if (Thread.currentThread() instanceof ConsoleThread){  //can't set visible on console thread, since then dlog will be hanging on consoleThread and consoleThread can't feed it input

				MDConsoleShowThread t = new MDConsoleShowThread(this);
				t.start();
				return;
			}
			System.out.println("Dialog box shown.  Title: " + getName());
			System.out.println("");
			System.out.println("For console choice use component numbers");
			showDialogComponents(outerContents, new MesquiteInteger(0), " ", true);
			ConsoleThread.setConsoleObjectCommanded(this, false, true);
			holdsConsoleFocus = true;
			if (!MesquiteWindow.GUIavailable || MesquiteWindow.suppressAllWindows) {
				if (MesquiteThread.isScripting())
					return;
				waitingOnButtonPush = true;
				try {
					waiting = true;
					while (waiting && waitingOnButtonPush)
						Thread.sleep(20);
					
				} catch (InterruptedException e) {
				}
				return;
			}

			// if defaultButtonString flag is non null, then recurse through components setting key listeners
			if (defaultButtonString != null)
				setKeyListeners(outerContents);
			setMouseListeners(outerContents);
			// pack();
			thread = Thread.currentThread();
			for (int i=0; i< dialogListeners.size(); i++){
				DialogListener dL = (DialogListener)dialogListeners.elementAt(i);
				dL.dialogShown(this);
			}
			showDialog();
			invalidate();// attempt to workaround bug in Jaguar
			validate(); // attempt to workaround bug in Jaguar
			/*
			 * if (mt instanceof MesquiteThread) { thread = (MesquiteThread)mt; }
			 */
			// waitForOtherDialogThreads();
		}


	}
	private void setResponsePending(boolean p){
		responsePending = p;
		if (Thread.currentThread() instanceof MesquiteThread){
			MesquiteThread thread = (MesquiteThread)Thread.currentThread();
			if (thread.getProgressIndicator() != null && thread.getProgressIndicator().isVisible()){
				thread.getProgressIndicator().getProgressWindow().setVisible(false);
			}
		}
	}
	public void dispose() {
		setResponsePending(false);
		waiting = false;
		if (holdsConsoleFocus) {
			ConsoleThread.releaseConsoleObjectCommanded(this, true);
			System.out.println("Dialog closed");
		}
		holdsConsoleFocus = false;
		if (!usingWizard){
			outerContents.setVisible(false);
			if (alreadyDisposed) {
				return;
			}
			alreadyDisposed = true;
			MesquiteWindow.numDialogs--;
			MesquiteWindow.numNonWizardDialogs--;
			if (MesquiteModule.mesquiteTrunk.dialogVector != null)
				MesquiteModule.mesquiteTrunk.dialogVector.removeElement(this, false);
			if (MesquiteWindow.numDialogs <= 0)
				MesquiteWindow.dialogAnchor.toBack();
			MainThread.decrementSuppressWaitWindow();
			parentDialog.pleaseDispose();
		}
		else {
			//	outerContents.setVisible(false);

			parentDialog.removeDialog(this); 
			MesquiteWindow.numDialogs--;
			if (MesquiteModule.mesquiteTrunk.dialogVector != null)
				MesquiteModule.mesquiteTrunk.dialogVector.removeElement(this, false);
			waitingOnButtonPush = false;
		}
	}
	/*=====@@@@@@@@@@@@@@@============*/
	public long getID() {
		return id;
	}

	public String getName() {
		return parentDialog.getTitle();
	}

	/* ................................................................................................................. */
	/** A request for the object to perform a command. It is passed two strings, the name of the command and the arguments. */
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(getClass(), null, null, commandName, "show")) {
			System.out.println("Components of dialog");
			showDialogComponents(this, new MesquiteInteger(0), " ", true);
		}
		else {
			MesquiteInteger pos = new MesquiteInteger();
			int whichComponent = MesquiteInteger.fromFirstToken(commandName, pos);
			if (MesquiteInteger.isCombinable(whichComponent)) {
				Component c = findDialogComponents(this, new MesquiteInteger(0), whichComponent);
				if (c instanceof TextComponent && ((TextComponent) c).isEditable()) {
					String text = ParseUtil.getFirstToken(arguments, pos);
					if (text != null)
						((TextComponent) c).setText(text);
					showDialogComponents(this, new MesquiteInteger(0), " ", true);
				}
				else if (c instanceof Choice) {
					System.out.println("Choice selected");
					Choice choice = (Choice) c;
					int whichItem = MesquiteInteger.fromString(arguments, new MesquiteInteger(0));
					if (MesquiteInteger.isCombinable(whichItem)) {
						choice.select(whichItem - 1);
						showDialogComponents(this, new MesquiteInteger(0), " ", true);
					}

					else {
						Parser parser = new Parser();
						String iss = parser.getFirstToken(arguments);
						boolean selected = false;
						if (iss != null)
							for (int i = choice.getItemCount() - 1; i >= 0; i--) {
								String s = choice.getItem(i);
								if (s.equalsIgnoreCase(iss)) {
									choice.select(i);
									selected = true;
								}
							}
						if (selected)
							showDialogComponents(this, new MesquiteInteger(0), " ", true);

					}
				}
				else if (c instanceof Label) {

				}
				else if (c instanceof Button) {

					selectButton(((Button) c).getLabel());
				}
				else if (c instanceof Checkbox) {
					System.out.println("Checkbox selected");
					Checkbox cb = (Checkbox) c;
					cb.setState(!cb.getState()); // needs to use event
					ItemListener[] listeners = cb.getItemListeners();
					int is;
					if (cb.getState())
						is = ItemEvent.SELECTED;
					else
						is = ItemEvent.DESELECTED;
					if (listeners != null)
						for (int i = 0; i < listeners.length; i++)
							listeners[i].itemStateChanged(new ItemEvent(cb, ItemEvent.ITEM_STATE_CHANGED, cb.getLabel(), is));
					showDialogComponents(this, new MesquiteInteger(0), " ", true);
				}
				else if (c instanceof List) {
					List cList = (List) c;
					int whichItem = MesquiteInteger.fromString(arguments, new MesquiteInteger(0));
					if (MesquiteInteger.isCombinable(whichItem)) {
						cList.select(whichItem - 1);
						showDialogComponents(this, new MesquiteInteger(0), " ", true);
					}

					else {
						System.out.println("Item in list selected");
						Parser parser = new Parser();
						String iss = parser.getFirstToken(arguments);
						boolean selected = false;
						if (iss != null)
							for (int i = cList.getItemCount() - 1; i >= 0; i--) {
								String s = cList.getItem(i);
								if (s.equalsIgnoreCase(iss)) {
									cList.select(i);
									selected = true;
								}
							}
						if (selected)
							showDialogComponents(this, new MesquiteInteger(0), " ", true);

					}
				}
			}
			else if (checker.compare(getClass(), null, null, commandName, "show")) {
			}

		}
		/*
		 * else return  super.doCommand(commandName, arguments, checker);
		 */
		return null;
	}

	public static Component findDialogComponents(MesquiteDialog c, MesquiteInteger count, int target) {
		return findDialogComponents(c.outerContents, count, target);
	}
	public static Component findDialogComponents(Component c, MesquiteInteger count, int target) {

		if ((c instanceof TextComponent && ((TextComponent) c).isEditable()) || c instanceof Choice || c instanceof Button || c instanceof Checkbox || c instanceof List) {
			count.increment();
			if (count.getValue() == target)
				return c;
		}
		if (c instanceof Container) {
			Component[] cc = ((Container) c).getComponents();
			if (cc != null && cc.length > 0)
				for (int i = 0; i < cc.length; i++) {
					Component q = findDialogComponents(cc[i], count, target);
					if (q != null)
						return q;
				}
		}
		return null;
	}

	public static void showDialogComponents(MesquiteDialog c, MesquiteInteger count, String spacer, boolean outer) {
		showDialogComponents(c.outerContents, count, spacer, true);
	}
	static boolean hasText, hasListOrChoice;

	public static void showDialogComponents(Component c, MesquiteInteger count, String spacer, boolean outer) {
		if (outer){
			hasText = false;
			hasListOrChoice = false;

		}
		if (c instanceof TextComponent) {
			String s = ((TextComponent) c).getText();
			if (StringUtil.blank(s))
				s = "<blank>";
			if (((TextComponent) c).isEditable()) {
				hasText = true;
				count.increment();
				System.out.println(spacer + count.getValue() + " " + s);
			}
			else
				System.out.println(spacer + " " + s);
		}
		else if (c instanceof Label) {
			System.out.println(spacer + " " + ((Label) c).getText());
		}
		else if (c instanceof Choice) {
			hasListOrChoice = true;

			count.increment();
			Choice choice = (Choice) c;
			System.out.println(spacer + "--------------");
			System.out.println(spacer + count.getValue() + " Choice ");
			for (int k = 0; k < choice.getItemCount(); k++) {
				if (choice.getSelectedIndex() == k)
					System.out.println(spacer + "  " + (k+1) + " *" + choice.getItem(k));
				else
					System.out.println(spacer + "  " + (k+1) + "  " + choice.getItem(k));
			}
			System.out.println(spacer + "--------------");
		}
		else if (c instanceof Button) {
			count.increment();
			System.out.println(spacer + count.getValue() + " (" + ((Button) c).getLabel() + ")");
		}
		else if (c instanceof Checkbox) {
			count.increment();
			if (((Checkbox) c).getState())
				System.out.print(spacer + count.getValue() + " " + "[on] ");
			else
				System.out.print(spacer + count.getValue() + " " + "[off] ");

			System.out.println(((Checkbox) c).getLabel());
		}
		else if (c instanceof List) {
			hasListOrChoice = true;
			System.out.println(spacer + "--------------");
			count.increment();
			System.out.println(spacer + count.getValue() + " List ");
			List list = (List) c;
			for (int k = 0; k < list.getItemCount(); k++) {
				if (list.isIndexSelected(k))
					System.out.println(spacer + "  " + (k+1) + " *" + list.getItem(k));
				else
					System.out.println(spacer + "  " + (k+1) + "  " + list.getItem(k));
			}
			System.out.println(spacer + "--------------");
		}
		else {
			// System.out.println(spacer + count.getValue() + " COMPONENT " + c.getClass().getName());
		}
		if (c instanceof Container) {
			Component[] cc = ((Container) c).getComponents();
			if (cc != null && cc.length > 0)
				for (int i = 0; i < cc.length; i++)
					showDialogComponents(cc[i], count, spacer + "  ", false);
		}
		if (outer){
			System.out.println("Enter number of component to select.");
			if (hasListOrChoice){
				System.out.println(" If component is a list or choice menu, enter");
				System.out.println(" both number of component and number of item within it to choose");
				System.out.println(" (e.g. if a list is component 1, and you want to select its item 2, enter \"1 2\"");
				System.out.println("");
			}
			if (hasText){
				System.out.println(" If component is a editable text, enter");
				System.out.println(" both number of component and the text within it");
				System.out.println(" (e.g. if you want to specify the text for component 1 to be \"100.0\",");
				System.out.println(" enter \"1 100.0\"");
			}
		}
	}

	/* ................................................................................................................. */
	/*
	 * public Component add(Component c){ if (c!=null) c.addKeyListener(this); return super.add(c); } public void add(Component c, Object obj){ if (c!=null) c.addKeyListener(this); super.add(c, obj); } public void add(Component c, Object obj, int i){ if (c!=null) c.addKeyListener(this); super.add(c, obj, i); } public Component add(Component c, int i){ if (c!=null) c.addKeyListener(this); return super.add(c, i); } public Component add(String s, Component c){ if (c!=null) c.addKeyListener(this); return super.add(s, c); } public Component addWithoutKeyListener(Component c){
	 * return super.add(c); } public void addWithoutKeyListener(Component c, Object obj){ super.add(c, obj); } public void addWithoutKeyListener(Component c, Object obj, int i){ super.add(c, obj, i); } public Component addWithoutKeyListener(Component c, int i){ return super.add(c, i); } public Component addWithoutKeyListener(String s, Component c){ return super.add(s, c); }
	 */
	/* ................................................................................................................. */
	public void setAutoDispose(boolean autoDispose) {
		this.autoDispose = autoDispose;
	}

	/* ................................................................................................................. */
	public boolean getAutoDispose() {
		return autoDispose;
	}

	/* ................................................................................................................. */
	public void componentResized(ComponentEvent e) {
	}

	/* ................................................................................................................. */
	public void componentMoved(ComponentEvent e) {
	}

	/* ................................................................................................................. */
	public void componentHidden(ComponentEvent e) {
	}

	/* ................................................................................................................. */
	public void componentShown(ComponentEvent e) {
		if (MesquiteTrunk.isMacOSXJaguar()) { // attempts to get around Jaguar bugs
			valAll(this);

			setDialogLocation(savedX, savedY);
		}
		Toolkit.getDefaultToolkit().sync();
	}

	public void getButtons(MesquiteDialog c, Vector v) {
		getButtons(c.outerContents, v);
	}
	public void getButtons(Component c, Vector v) {
		if (c == null || v == null)
			return;
		if (c instanceof Button)
			v.addElement(((Button) c).getLabel());
		if (c instanceof Container) {
			Component[] cc = ((Container) c).getComponents();
			if (cc != null && cc.length > 0)
				for (int i = 0; i < cc.length; i++)
					getButtons(cc[i], v);
		}

	}

	private void valAll(MesquiteDialog c) {
		valAll(c.outerContents);
	}
	private void valAll(Component c) {
		if (c == null)
			return;
		if (c instanceof Container) {
			c.invalidate();
			c.validate();
			Component[] cc = ((Container) c).getComponents();
			if (cc != null && cc.length > 0)
				for (int i = 0; i < cc.length; i++)
					valAll(cc[i]);
		}

	}

	private void rpAll(Component c) {
		if (c == null)
			return;
		c.repaint();
		if (c instanceof Container) {
			Component[] cc = ((Container) c).getComponents();
			if (cc != null && cc.length > 0)
				for (int i = 0; i < cc.length; i++)
					rpAll(cc[i]);
		}

	}

	/* ................................................................................................................. */
	public static void addComponent(MesquiteDialog container, Component component, int gridx, int gridy, int gridwidth, int gridheight, int weightx, int weighty, int fill, int anchor) {
		addComponent(container.outerContents, component, gridx, gridy, gridwidth, gridheight, weightx, weighty,fill, anchor);
	}
	/* ................................................................................................................. */
	public static void addComponent(Container container, Component component, int gridx, int gridy, int gridwidth, int gridheight, int weightx, int weighty, int fill, int anchor) {
		LayoutManager lm = container.getLayout();
		if (lm instanceof GridBagLayout) {
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = gridx;
			gbc.gridy = gridy;
			gbc.gridwidth = gridwidth;
			gbc.gridheight = gridheight;
			gbc.weightx = weightx;
			gbc.weighty = weighty;
			gbc.fill = fill;
			gbc.anchor = anchor;
			((GridBagLayout) lm).setConstraints(component, gbc);
		}
		container.add(component);
	}

	public abstract void buttonHit(String buttonLabel, Button button);


	private void setMouseListeners(Component c) {
		if (c == null)
			return;
		if (c instanceof Button)
			c.addMouseListener(this);
		if (c instanceof Container) {
			Component[] cc = ((Container) c).getComponents();
			if (cc != null && cc.length > 0)
				for (int i = 0; i < cc.length; i++)
					setMouseListeners(cc[i]);
		}

	}

	// TODO: have way to remove default button or set default button in all dialog boxes
	private void setKeyListeners(Component c) {
		if (c == null)
			return;
		c.addKeyListener(this);
		if (c instanceof Button && defaultButtonString != null && defaultButtonString.equals(((Button) c).getLabel())) {
			Font f = c.getFont();
			Font fontToSet = new Font(f.getName(), Font.ITALIC, f.getSize());
			c.setFont(fontToSet);
		}
		if (c instanceof Container) {
			Component[] cc = ((Container) c).getComponents();
			if (cc != null && cc.length > 0)
				for (int i = 0; i < cc.length; i++)
					setKeyListeners(cc[i]);
		}

	}


	boolean waiting = false;


	// don't show dialog if there are already showing dialogs on other threads!!!!!
	private void waitForOtherDialogThreads() {
		try {
			while (othersExist()) {
				Thread.sleep(20);
			}
		} catch (Exception e) {
		} catch (Error e) {
			if (e instanceof OutOfMemoryError)
				MesquiteMessage.println("OutofMemoryError.  See file startingMesquiteAndMemoryAllocation.txt in the Mesquite_Folder for information on how to increase memory allocated to Mesquite.");
		}
	}
	private boolean othersExist() {
		ListableVector ds = MesquiteModule.mesquiteTrunk.dialogVector;
		if (ds == null || ds.size() < 1)
			return false;
		for (int i = 0; i < ds.size(); i++) {
			MesquiteDialog dlog = (MesquiteDialog) ds.elementAt(i);
			if (dlog != this && dlog.isVisible() && dlog.thread != null && thread != null && dlog.thread != thread)
				return true;
		}
		return false;
	}

	public void setDefaultButton(String s) {
		defaultButtonString = s;
	}

	public void keyTyped(KeyEvent e) {
		if ((e.getKeyCode() == 10 || e.getKeyCode() == 13) && defaultButtonString != null) {
			buttonHit(defaultButtonString, null);
			if (autoDispose)
				dispose();
		}
	}

	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
		if ((e.getKeyCode() == 10 || e.getKeyCode() == 13) && defaultButtonString != null) {
			buttonHit(defaultButtonString, null);
			if (autoDispose)
				dispose();
		}
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	/*	if (MesquiteTrunk.isMacOSXYosemite() && MesquiteTrunk.isJava VersionLessThan(1.7) && e.getComponent() instanceof Button) {
			buttonHit(((Button) e.getComponent()).getLabel(), (Button) e.getComponent());
			dispose();
		}
		*/
	}

	public void mouseReleased(MouseEvent e) {
		if (e.getComponent() instanceof Button) {
			buttonHit(((Button) e.getComponent()).getLabel(), (Button) e.getComponent());
			dispose();
		}
	}

	public void selectButton(String label) { // for use by scripting & console

		buttonHit(label, null);
		dispose();
	}
}
class MDPanel extends MQPanel {
	boolean usingWizard = false;
	public MDPanel (boolean usingWizard){
		this.usingWizard = usingWizard;
		//setBackground(Color.green);
	}
	public Dimension getPreferredSize(boolean contentsOnly){
		if (usingWizard && !contentsOnly)
			return new Dimension(MesquiteDialog.wizardWidth - MesquiteDialogParent.infoWidth, MesquiteDialog.wizardHeight);
		return super.getPreferredSize();
	}
	public Dimension getPreferredSize(){
		if (usingWizard)
			return new Dimension(MesquiteDialog.wizardWidth - MesquiteDialogParent.infoWidth, MesquiteDialog.wizardHeight);
		return super.getPreferredSize();
	}



}

class MDConsoleShowThread extends Thread {
	MesquiteDialog dlog;
	public MDConsoleShowThread(MesquiteDialog dlog){
		this.dlog = dlog;
	}
	public void run(){
		dlog.setVisible(true);
	}
}


