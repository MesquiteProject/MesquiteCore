/* Mesquite source code.  Copyright 2001 and onward, D. Maddison and W. Maddison. 


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

import javax.swing.*;
import javax.swing.text.*;

import mesquite.lib.simplicity.InterfaceManager;
import mesquite.lib.table.EditorTextField;

import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

/*
	To do:
		- add ability to set radio buttons so that they are in a row
		- clean up event handling

 */

/*===============================================*/
/** An extensible dialog box.  */
public class ExtensibleDialog extends MesquiteDialog implements ActionListener, WindowListener, DoubleClickListener, ImagePanelListener, ComponentListener {
	public static final int defaultCANCEL = 1;
	public static final int defaultOK = 0;
	ImagePanel helpURLPanel;
	ImagePanel helpStringPanel;
	public MesquiteInteger buttonPressed;
	String buttonLabel0;
	String buttonLabel1;
	String buttonLabel2;
	MesquiteModule helpURLOwnerModule=null;
	Button button0, button1, button2;
	Image helpImage;
	boolean useManualPage = false;
	public String defaultOKLabel = "OK";
	public String defaultLabel = defaultOKLabel;
	public String defaultCancelLabel="Cancel";
	String helpString = "";
	String helpURL = "";
	int numButtons = 0;
	int numExtraButtons = 5;
	int currentExtraButton = -1;
	String[] extraButtonLabels = new String[numExtraButtons];
	//	CardPanels cardPanels = null;
	//	Panel cardPanel = null;
	Panel addPanel=null;
	JPanel addJPanel = null;
	int cardPanelNumber = 0;
	Panel lastPanel;
	int defaultButtonNumber = 0;
	int dialogWidth=20;
	//	public static final int checkBoxHeight = 24;
	//	public static final int buttonHeight = 30;
	//	public static final int popUpHeight = 30;
	//	public static final int textFieldHeight = 30;
	//	public static final int labelHeight = 24;
	int dialogHeight=50;	
	static final int minDialogWidthWithCanvas=240;
	int minDialogWidth=200;
	int minDialogHeight=120;
	Dimension d;

	static final int maxDialogWidthWithCanvas=840;
	int maxDialogWidth=820;
	boolean samePanelAsLast = false;
	boolean wasSamePanelAsLast = false;
	GridBagLayout gridBag;
	GridBagLayout currentGridBag=null;
	GridBagConstraints constraints;
	Object customConstraints=null;
	Component focalComponent=null;
	Component defaultTextComponent=null;


	public int sideBuffer = 12;

	public static Font defaultBigFont = new Font ("Dialog", Font.PLAIN, 12);
	public static Font boldFont = new Font ("Dialog", Font.BOLD, 12);
	public static Font defaultSmallFont = new Font ("SanSerif", Font.PLAIN, 10);
	public static Font defaultVerySmallFont = new Font ("SanSerif", Font.PLAIN, 9);

	/*.................................................................................................................*/
	public ExtensibleDialog (Object parent, String title, MesquiteInteger buttonPressed, boolean suppressWizardIfFirst) {
		super(parent, title, suppressWizardIfFirst);
		setFont(defaultBigFont);
		intializeDialog(title,buttonPressed);
		addWindowListener(this);
	}
	/*.................................................................................................................*/
	public ExtensibleDialog (Object parent, String title, MesquiteInteger buttonPressed) {
		super(parent, title);
		setFont(defaultBigFont);
		intializeDialog(title,buttonPressed);
		addWindowListener(this);
	}
	/*.................................................................................................................*/
	public ExtensibleDialog (Object parent, String title) {
		super(parent, title);
		setFont(defaultBigFont);
		buttonPressed = new MesquiteInteger(1);
		intializeDialog(title,buttonPressed);
		addWindowListener(this);
	}
	int itemCount = 0;
	/*.................................................................................................................*/
	private GridBagConstraints createGridBagConstraints() {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy = GridBagConstraints.RELATIVE;
		c.gridwidth=1;
		c.gridheight=1;
		c.fill=GridBagConstraints.BOTH;
		c.anchor=GridBagConstraints.WEST;
		return c;
	}
	/*.................................................................................................................*/
	private GridBagConstraints adjustGridBagConstraints() {
		return constraints;
	}
	/*.................................................................................................................*/
	void intializeDialog (String title, MesquiteInteger buttonPressed) {
		d = new Dimension(minDialogWidth, minDialogHeight);
		gridBag = new GridBagLayout();
		constraints = createGridBagConstraints();
		setLayout(gridBag);
		this.buttonPressed = buttonPressed;
		this.setResizable(false);

	}
	public void setContentPane(JPanel panel) {
		parentDialog.setContentPane(panel);
	}

	/*.................................................................................................................*/
	public void setAddPanel (Panel addPanel, LayoutManager layout) {
		this.addPanel = addPanel;
		//setContentPane(addPanel);
		if (layout==null){   // in this case, inherit the basic layout structure from dialog
			currentGridBag = new GridBagLayout();
			addPanel.setLayout(currentGridBag);
		}
		else {
			addPanel.setLayout(layout);
		}
	}
	/*.................................................................................................................*/
	public void setAddJPanel (JPanel addPanel, LayoutManager layout) {
		this.addJPanel = addPanel;
		//setContentPane(addPanel);
		if (layout==null){   // in this case, inherit the basic layout structure from dialog
			currentGridBag = new GridBagLayout();
			addPanel.setLayout(currentGridBag);
		}
		else {
			addPanel.setLayout(layout);
		}
	}
	/*.................................................................................................................*/
	public void setAddPanel (Panel addPanel) {
		setAddPanel(addPanel,null);
	}
	/*.................................................................................................................*/
	public void setAddJPanel (JPanel addPanel) {
		setAddJPanel(addPanel,null);
	}
	/*.................................................................................................................*/
	public Panel getAddPanel () {
		return addPanel;
	}
	/*.................................................................................................................*/
	public JPanel getAddJPanel () {
		return addJPanel;
	}
	/*.................................................................................................................*/
	public void nullifyAddPanel () {
		addPanel = null;
		addJPanel=null;
		//setContentPane(null);
		//  	lastPanel = addPanel;
	}
	/*.................................................................................................................*/
	public void suppressNewPanel () {
		samePanelAsLast = true;
	}
	/*.................................................................................................................*/
	public void forceNewPanel () {
		samePanelAsLast = false;
	}
	/*.................................................................................................................*/
	public Panel addNewDialogPanel (Panel panel,GridBagConstraints c) {
		wasSamePanelAsLast = samePanelAsLast;
		if (!samePanelAsLast) {
			Panel newPanel;
			if (panel != null)
				newPanel = panel;
			else
				newPanel = new Panel();
			addToDialog(newPanel, c);
			newPanel.setVisible(true);
			if (currentGridBag!=null && (addPanel!=null || addJPanel!=null))
				currentGridBag.setConstraints(newPanel,createGridBagConstraints());
			else
				gridBag.setConstraints(newPanel,constraints);
			lastPanel = newPanel;
			return newPanel;
		}
		samePanelAsLast = false;
		return lastPanel;
	}
	/*.................................................................................................................*
	public void setAlignmentOfLastPanel (int alignment) {
		if (lastPanel!=null) {
			GridBagConstraints constraints = null;
			if (currentGridBag!=null) {
				constraints = currentGridBag.getConstraints(lastPanel);
				if (constraints!=null) {
					constraints.anchor = alignment;
					currentGridBag.setConstraints(lastPanel,constraints);
				}
			}
			else {
				constraints = gridBag.getConstraints(lastPanel);
				if (constraints!=null) {
					constraints.anchor = alignment;
					gridBag.setConstraints(lastPanel,constraints);
				}
			}
		}
	}

	/*.................................................................................................................*/
	public Panel addNewDialogPanel () {
		return addNewDialogPanel(null,null);
	}
	/*.................................................................................................................*/
	public Panel addNewDialogPanel (Panel panel) {
		return addNewDialogPanel(panel, null);
	}
	/*.................................................................................................................*/
	public Panel addNewDialogPanel (GridBagConstraints c) {
		return addNewDialogPanel(null, c);
	}
	/*.................................................................................................................*/
	public Panel getLastPanel () {
		return lastPanel;
	}
	/*.................................................................................................................*/
	public void addToDialog (Component component, Object c) {
		if (component!=null) {
			if (addPanel!=null && component !=addPanel) {
				if (customConstraints!=null)
					addPanel.add(component,(String)customConstraints);
				else
					addPanel.add(component, adjustGridBagConstraints());
				component.setBackground(addPanel.getBackground());
			} else if (addJPanel!=null && component !=addJPanel) {
				if (customConstraints!=null)
					addJPanel.add(component,(String)customConstraints);
				else
					addJPanel.add(component, adjustGridBagConstraints());
				//addJPanel.add(component);


				component.setBackground(addJPanel.getBackground());
			}
			else {
				//if (c==null)
				add(component);
				//else 
				//	add(component, c);
				component.setBackground(getBackground());
			}
		}
	}
	/*.................................................................................................................*/
	public void addToDialog (Component component) {
		addToDialog(component, null);
	}
	/*.................................................................................................................*/
	public GridBagLayout getGridBagLayout () {
		return gridBag;
	}
	/*.................................................................................................................*/
	public GridBagConstraints getGridBagConstraints () {
		return constraints;
	}
	/*.................................................................................................................*/
	public void setCustomConstraints (Object customConstraints) {
		this.customConstraints = customConstraints;
	}
	/*.................................................................................................................*/
	public void nullifyCustomConstraints () {
		customConstraints = null;
	}
	/*.................................................................................................................*/
	public Object getCustomConstraints () {
		return customConstraints;
	}
	/*.................................................................................................................*/
	public void setGridBagConstraints (GridBagConstraints constraints) {
		this.constraints = constraints;
	}
	/*.................................................................................................................*/
	public void setDefaultTextComponent (Component textComponent) {
		defaultTextComponent = textComponent;
	}
	/*.................................................................................................................*/
	public void appendToHelpString (String s) {
		helpString += s + " ";
	}
	/*.................................................................................................................*/
	public String getHelpString () {
		return helpString;
	}

	/*.................................................................................................................*/
	public boolean hasHelpURL () {
		return (!StringUtil.blank(getHelpURL()) || (helpURLOwnerModule!=null));
	}

	/*.................................................................................................................*/
	private boolean getUseManualPage () {
		return useManualPage;
	}
	/*.................................................................................................................*/
	private void setUseManualPage (boolean useManual) {
		useManualPage = useManual;
	}
	/*.................................................................................................................*/
	public MesquiteModule getHelpURLOwnerModule () {
		return helpURLOwnerModule;
	}
	/*.................................................................................................................*/
	public void setHelpURLOwnerModule (MesquiteModule ownerModule) {
		helpURLOwnerModule = ownerModule;
	}
	/*.................................................................................................................*/
	public void showManual(){
		if (getHelpURLOwnerModule()!=null) {
			if (!StringUtil.blank(helpURL)) {
				String s = getHelpURLOwnerModule().getPackageIntroModule().getDirectoryPath() + getHelpURL();
				MesquiteModule.showWebPage(s, false);
			}
			else if (useManualPage)
				getHelpURLOwnerModule().showManual();
			else
				MesquiteModule.showWebPage(getHelpURLOwnerModule().getPackageIntroModule().getSplashURL(), false);
		}
		else if (!StringUtil.blank(helpURL)) 
			MesquiteModule.showWebPage(getHelpURL(), false);
	}
	/*.................................................................................................................*/
	public void setHelpURL (MesquiteModule ownerModule, String s) {
		setHelpURL(ownerModule,s,false);
	}
	/*.................................................................................................................*/
	public void setHelpURL (MesquiteModule ownerModule, String s, boolean useManual) {
		helpURLOwnerModule = ownerModule;
		helpURL = s;
		useManualPage = useManual;
	}
	/*.................................................................................................................*/
	public void setHelpURL (String s) {
		helpURL = s;
	}
	/*.................................................................................................................*/
	public String getHelpURL () {
		return helpURL;
	}
	/*.................................................................................................................*/
	public String getDefaultOKLabel () {
		return defaultLabel;
	}
	/*.................................................................................................................*/
	public String getDefaultCancelLabel () {
		return defaultCancelLabel;
	}
	/*.................................................................................................................*/
	public Button getMainButton () {
		return button0;
	}
	/*.................................................................................................................*/
	public Button getCancelButton () {
		int cancel = getButtonNumber(getDefaultCancelLabel());
		if (cancel==0)
			return button0;
		else if (cancel==1)
			return button1;
		else if (cancel==2)
			return button2;
		return null;
	}
	/*.................................................................................................................*/
	void drawLine(int x1, int y1, int x2, int y2, Color color) {
		Graphics g = getGraphics();
		if (g!=null) {
			g.setColor(color);
			g.drawLine(x1,y1,x2, y2);
		}
	}
	/*.................................................................................................................*/
	public int getButtonNumber (String buttonLabel) {
		if (buttonLabel == null)
			return -1;
		else if (buttonLabel.equalsIgnoreCase(buttonLabel0))
			return 0;
		else if (buttonLabel.equalsIgnoreCase(buttonLabel1))
			return 1;
		else if (buttonLabel.equalsIgnoreCase(buttonLabel2))
			return 2;
		else
			return -1; 
	}
	/*.................................................................................................................*/
	public boolean hasDefaultButton(){
		return !StringUtil.blank(defaultLabel);
	}
	/*.................................................................................................................*/
	public void setDefaultButton (String defaultLabel) {
		super.setDefaultButton(defaultLabel);
		if (defaultLabel != null)
			this.defaultLabel = defaultLabel;
		this.defaultButtonNumber = getButtonNumber(defaultLabel);		
	}
	/*.................................................................................................................*/
	void setPrimaryButtonConstraints(Panel buttons) {
		gridBag.setConstraints(buttons,constraints);
	}
	/*.................................................................................................................*/

	public boolean addExtraButtons (Panel panel, ActionListener actionListener) {
		boolean buttonAdded=false;
		for (int i=0; i<numExtraButtons; i++) {
			if (!StringUtil.blank(extraButtonLabels[i])) {
				if (actionListener!=null)
					addAListenedButton(extraButtonLabels[i],panel, actionListener);
				else
					addAButton(extraButtonLabels[i],panel);
				buttonAdded = true;
			}
		}
		if (buttonAdded)
			addAnEmptyImage(panel);
		return buttonAdded;
	}
	/*.................................................................................................................*/

	public boolean addExtraButtons (Panel panel) {
		return addExtraButtons(panel, null);
	}
	/*.................................................................................................................*/

	public void setExtraButton (String s) {
		currentExtraButton++;
		if (currentExtraButton<numExtraButtons)
			extraButtonLabels[currentExtraButton]=s;
	}
	/*.................................................................................................................*/

	public void addHelpButtons (Panel panel) {
		boolean buttonAdded=false;
		if (hasHelpURL()) {
			helpURLPanel = addAListenedImage("manual", "manual.gif","manualPressed.gif",true,panel, this);
			helpURLPanel.setVisible(true);
			buttonAdded = true;
		}
		if (!isInWizard() && !StringUtil.blank(getHelpString())) {
			//addAListenedButton("?",buttons,this);
			helpStringPanel = addAListenedImage("?", "help.gif","helpPressed.gif",true,panel, this);
			helpStringPanel.setVisible(true);
			buttonAdded = true;
		}
		if (buttonAdded) {
			addAnEmptyImage(panel);
		}
	}
	/*.................................................................................................................*/
	TextField textEdgeCompensationDetector; //shown only once; to help detect text edge compenstation factors;
	boolean checkTextEdge = false;
	public void setCheckTextEdge(boolean cte){
		checkTextEdge = cte;
	}
	/*.................................................................................................................*/
	public void addPrimaryButtonRow (String buttonLabel0, String buttonLabel1, String buttonLabel2) {
		Panel buttons = new Panel();
		//		buttons.setSize(dialogWidth, buttonHeight);
		String singleButton = null;
		boolean noButtons=true;
		if (hasHelpURL())
			noButtons=false;
		if (!isInWizard() && !StringUtil.blank(getHelpString()))
			noButtons=false;
		addHelpButtons(buttons);
		if (addExtraButtons(buttons))
			noButtons=false;
		if (buttonLabel2!=null) {
			button2=addAButton(buttonLabel2,buttons);
			this.buttonLabel2 = buttonLabel2;
			if (noButtons) {
				singleButton = buttonLabel2;
				noButtons = false;
			}
		}
		if (buttonLabel1!=null) {
			button1=addAButton(buttonLabel1,buttons);
			this.buttonLabel1 = buttonLabel1;
			if (noButtons) {
				singleButton = buttonLabel1;
				noButtons = false;
			}
			else if (!noButtons)
				singleButton = null;
		}
		if (buttonLabel0!=null) {
			button0=addAButton(buttonLabel0,buttons);
			this.buttonLabel0 = buttonLabel0;
			if (noButtons) {
				singleButton = buttonLabel0;
				noButtons = false;
			}
			else if (!noButtons)
				singleButton = null;
		}
		if (!MesquiteModule.textEdgeRemembered || checkTextEdge)
			buttons.add("West", textEdgeCompensationDetector = new TextField(" "));
		addToDialog(buttons);	
		setPrimaryButtonConstraints(buttons);	
		if (singleButton!=null)
			setDefaultButton(singleButton);	
	}
	/*.................................................................................................................*/
	public void addPrimaryButtonRowListener (String buttonLabel0, String buttonLabel1, ActionListener actionListener) {
		Panel buttons = new Panel();
		//		buttons.setSize(dialogWidth, buttonHeight);
		addHelpButtons(buttons);
		addExtraButtons(buttons, actionListener);
		button1=addAListenedButton(buttonLabel1,buttons,actionListener);
		this.buttonLabel1 = buttonLabel1;
		button0=addAListenedButton(buttonLabel0,buttons,actionListener);
		this.buttonLabel0 = buttonLabel0;
		addToDialog(buttons);			
		setPrimaryButtonConstraints(buttons);
		addBlankLine();
	}
	/*.................................................................................................................*/
	public void addPrimaryButtonRow (String buttonLabel0, String buttonLabel1) {
		addPrimaryButtonRowListener(buttonLabel0,buttonLabel1,null);
	}
	/*.................................................................................................................*/
	public void addPrimaryButtonRow (String buttonLabel0) {
		Panel buttons = new Panel();
		//		buttons.setSize(dialogWidth, buttonHeight);
		addHelpButtons(buttons);
		addExtraButtons(buttons);
		button0=addAButton(buttonLabel0,buttons);
		this.buttonLabel0 = buttonLabel0;
		addToDialog(buttons);	
		setPrimaryButtonConstraints(buttons);	
		setDefaultButton(buttonLabel0);	
		addBlankLine();
	}
	/*.................................................................................................................*/
	public Button addAButton (String s, Panel buttons) { 
		return addAButton(s,buttons,null);
	}
	/*.................................................................................................................*/
	public Button addAButton (String s, Panel buttons, GridBagConstraints c) { 
		Button button;
		if (buttons==null && lastPanel!=null)
			buttons = lastPanel;
		if (buttons == null)
			return null;
		if (c==null)
			buttons.add(button = new Button(s));
		else
			buttons.add(button = new Button(s),c);
		numButtons++;
		return button;
	}
	/*.................................................................................................................*
	public Button addAListenedButton (String s, Panel buttons, ActionListener actionListener) { //David: modified to return button
		if (StringUtil.blank(s))
			return null;
		Button button=null;
		if (buttons==null) {
			if (lastPanel!=null) {
				lastPanel.add(button = new Button(s));
				numButtons++;
			}
		}
		else 	{
			buttons.add(button = new Button(s));
			numButtons++;
		}
		if ((actionListener!=null) && (button!=null))
			button.addActionListener(actionListener);
		return button;
	}
	/*.................................................................................................................*/
	public Button addAListenedButton (String s, Panel buttons, ActionListener actionListener) { //David: modified to return button
		return addAListenedButton(s,buttons,actionListener,null);
	}
	/*.................................................................................................................*/
	public Button addAListenedButton (String s, Panel buttons, ActionListener actionListener, GridBagConstraints c) { 
		if (StringUtil.blank(s))
			return null;
		Button button=addAButton(s,buttons,c);
		if ((actionListener!=null) && (button!=null))
			button.addActionListener(actionListener);
		return button;
	}

	/*.................................................................................................................*/
	public TextCanvasWithButtons addATextCanvasWithButtons(String s, String button1Name, String button1Command, String button2Name, String button2Command, ActionListener actionListener){
        Panel newPanel = addNewDialogPanel();
        GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints gridConstraints = new GridBagConstraints();
		gridConstraints.gridwidth=1;
		gridConstraints.gridheight=2;
		gridConstraints.gridy = 1;
		gridConstraints.gridx = 1;
		gridConstraints.weighty=1.0;

		newPanel.setLayout(gridbag);

		TextCanvasWithButtons textCanvasWithButtons = new TextCanvasWithButtons();

		pack();
		Dimension d = getPreferredSize();
		dialogWidth = d.width;
		if (dialogWidth<minDialogWidthWithCanvas) //this had been hardcoded at 240
			dialogWidth = minDialogWidthWithCanvas;
		else if (dialogWidth> maxDialogWidthWithCanvas) 
			dialogWidth =maxDialogWidthWithCanvas;
		MesquiteTextCanvas textCanvas = MesquiteTextCanvas.getTextCanvas(dialogWidth-sideBuffer*2, defaultSmallFont, s);
        gridbag.setConstraints(textCanvas, gridConstraints);
        newPanel.add(textCanvas);
		textCanvasWithButtons.setTextCanvas(textCanvas);
		
		gridConstraints.weighty = 0.0;		   //reset to the default
		gridConstraints.gridwidth = GridBagConstraints.REMAINDER; //end row
		gridConstraints.gridheight = 1;		   //reset to the default
		gridConstraints.gridy = 1;
		gridConstraints.gridx = 3;

        Button button = new Button(button1Name);
        gridbag.setConstraints(button, gridConstraints);
        newPanel. add(button);
		button.addActionListener(actionListener);
		button.setActionCommand(button1Command);
		textCanvasWithButtons.setButton(button);

		if (StringUtil.notEmpty(button2Name)) {
			gridConstraints.gridy = 2;
	        Button button2 = new Button(button2Name);
	        gridbag.setConstraints(button2, gridConstraints);
	        newPanel.add(button2);
			button2.addActionListener(actionListener);
			button2.setActionCommand(button2Command);
			textCanvasWithButtons.setButton2(button2);
		}
		
		lastPanel=null;
		return textCanvasWithButtons;
	}
	/*.................................................................................................................*/
	public TextCanvasWithButtons addATextCanvasWithButtons(String s, String button1Name, String button1Command, ActionListener actionListener){
		return addATextCanvasWithButtons(s,button1Name, button1Command, null,null, actionListener);
	}
	/*.................................................................................................................*/
	public ImagePanel addAListenedImage (String imageCommandName, String imageName, String imagePressedName, boolean inRoot, Panel panel, ImagePanelListener imageListener) {
		ImagePanel imagePanel = null;
		Image image=null;
		Image imagePressed=null;
		if (inRoot) {
			image = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + imageName);
			imagePressed = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + imagePressedName);
		}
		imagePanel = new ImagePanel(image,imagePressed, imageCommandName, imageListener);
		if (panel==null) {
			if (lastPanel!=null) 
				lastPanel.add(imagePanel);
		}
		else 	
			panel.add(imagePanel);
		return imagePanel;
	}
	/*.................................................................................................................*/
	public ImagePanel addAnEmptyImage (Panel panel) {
		Image image = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "empty.gif");
		ImagePanel imagePanel = new ImagePanel(image);

		if (panel==null) {
			if (lastPanel!=null) 
				lastPanel.add(imagePanel);
		}
		else 	
			panel.add(imagePanel);
		return imagePanel;
	}
	/*.................................................................................................................*/
	public Button addButton (String s) {
		Panel newPanel = addNewDialogPanel();
		Button button = new Button(s);
		newPanel.add(button);
		numButtons++;
		return button;
	}
	/*.................................................................................................................*/
	int currentWidth=0;

	/*.................................................................................................................*/
	public MesquiteTabbedPanel addMesquiteTabbedPanel() {
		MesquiteTabbedPanel tabbedPanel = new MesquiteTabbedPanel(this);
		add(tabbedPanel);
		return tabbedPanel;
	}
	/*.................................................................................................................*/
	public PopUpPanelOfCards addPopUpPanelOfCards (String name) {
		PopUpPanelOfCards panelOfCards = new PopUpPanelOfCards(this,name);
		addNewDialogPanel(panelOfCards);
		return panelOfCards;
	}
	/*.................................................................................................................*/
	private void resetTextCanvasSizes(Component c){
		if (c==null)
			return;
		if (c instanceof MesquiteTextCanvas) {

			minDialogWidth = minDialogWidthWithCanvas;
			maxDialogWidth = maxDialogWidthWithCanvas;
			if (currentWidth>maxDialogWidth)
				currentWidth = maxDialogWidth;
			else if (currentWidth<minDialogWidth)
				currentWidth = minDialogWidth;
			((MesquiteTextCanvas)c).setCanvasRows(currentWidth);

		}
		if (c instanceof Container){
			Component[] cc = ((Container)c).getComponents();
			if (cc!=null && cc.length>0)
				for (int i=0; i<cc.length; i++)
					resetTextCanvasSizes(cc[i]);
		}
	}
	/*.................................................................................................................*/
	public void report(Component c, String spacer){
		if (c==null)
			return;
		System.out.println(spacer + "x " + c.getX() + " y " + c.getY() + " w " + c.getWidth() + " h " + c.getHeight() + " vis " + c.isVisible() + " " + c);
		spacer += "  ";
		if (c instanceof Container){
			Component[] cc = ((Container)c).getComponents();
			if (cc!=null && cc.length>0)
				for (int i=0; i<cc.length; i++)
					report(cc[i], spacer);
		}
	}
	/*.................................................................................................................*/
	private void doLayouts(Component c){
		if (c==null)
			return;
		try {
			if (c instanceof Container){
				if (((Container)c).getLayout() != null)
					c.doLayout();
				Component[] cc = ((Container)c).getComponents();
				if (cc!=null && cc.length>0)
					for (int i=0; i<cc.length; i++)
						doLayouts(cc[i]);
			}
		}
		catch(Exception e){
			MesquiteMessage.println("exception in doLayouts of ExtensibleDialog");
		}
	}
	/*.................................................................................................................*/
	private void makeAllVisible(Component c){
		if (c==null)
			return;
		c.setVisible(true);
		if (c instanceof Container && !(c instanceof JTabbedPane)){
			Component[] cc = ((Container)c).getComponents();
			if (cc!=null && cc.length>0)
				for (int i=0; i<cc.length; i++)
					makeAllVisible(cc[i]);
		}
	}
	/*
	static void resetCardLayouts(Component c){  //a workaround for apparent OS X bug
 		if (c instanceof Container){
 			Component[] comps = ((Container)c).getComponents();
 			if (comps != null)
 				for (int i=0; i<comps.length; i++)
 				resetCardLayouts(comps[i]);
 		}
 		if (c instanceof PanelOfCards){
 			((PopUpPanelOfCards)c).reset();
 		}
 	}
	/*.................................................................................................................*/
	public void prepareDialog () {


		pack();
		d = getPreferredSize(true);
		/*if (isInWizard()){
			d.width -= MesquiteDialogParent.infoWidth;
		}
		 */
		if (MesquiteTrunk.isMacOSXJaguar()){
			//		int targetWidth = 300;
			//		int targetHeight = 160;
			int targetWidth = d.width;
			int targetHeight = d.height;
			int h, v;
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			h = (screenSize.width-targetWidth)/2;
			int menuBarHeight = 0;
			if (!StringUtil.blank(System.getProperty("mrj.version")))
				menuBarHeight = 32;
			v = (screenSize.height-targetHeight - menuBarHeight)/2 + menuBarHeight;
			if (v < menuBarHeight)
				v = menuBarHeight;
			setDialogLocation(h, v);
		}

		//	pack();
		//	d = getPreferredSize();
		currentWidth = d.width;
		if (currentWidth < 240) {
			currentWidth = 240;
		}
		if (currentWidth >600) {
			currentWidth = 600;
		}
		resetTextCanvasSizes(parentDialog);
		pack();
		d = getPreferredSize(true);
		if (d.width<minDialogWidth) {
			d.width = minDialogWidth;
		}
		else if (d.width> maxDialogWidth) {
			d.width = maxDialogWidth;
		}

		if (d.height<minDialogHeight) {
			d.height = minDialogHeight;
		}
		pack();
		if (MesquiteTrunk.isMacOSX() && MesquiteTrunk.getJavaVersionAsDouble()>=1.4)
			d.height +=25;
		doLayouts(outerContents);
		makeAllVisible(outerContents);

		if (isInWizard()){
			d.width += MesquiteDialogParent.infoWidth;
			d.width += 100;
		}
		else
			d.width += 20;


		setDialogSize(d);
		try {
			outerContents.invalidate();
			outerContents.validate();
			doLayout();
		}
		catch (Throwable t){
		}
		MesquiteWindow.centerWindow(getParentDialog());
		if (MesquiteTrunk.isMacOSXJaguar())
			pack(); //add for Jaguar bug, possibly also linux
		if (helpURLPanel!=null)  // for reasons that are unclear, these often aren't drawn.
			helpURLPanel.repaint();
		if (helpStringPanel!=null) // for reasons that are unclear, these often aren't drawn.
			helpStringPanel.repaint();
		setFoci();
		//resetCardLayouts(outerContents);


	}


	/*.................................................................................................................*/
	public void setFoci () {
		boolean success=false;
		if (focalComponent!=null)
			success = focalComponent.requestFocusInWindow();
		if (defaultTextComponent!=null) {
			if (defaultTextComponent instanceof TextComponent)
				((TextComponent)defaultTextComponent).selectAll();
			else if (defaultTextComponent instanceof JTextComponent)
				((JTextComponent)defaultTextComponent).selectAll();
		}
	}

	/*.................................................................................................................*/
	public void prepareDialogHideFirst () {
		boolean wasVisible = isVisible();
		//if (wasVisible && MesquiteTrunk.isMacOSXJaguar())
		//	setVisible(false);
		prepareDialog();
		if (focalComponent!=null)
			focalComponent.requestFocusInWindow();
		//if (wasVisible && MesquiteTrunk.isMacOSXJaguar())
		//	setVisible(true);
	}
	/*.................................................................................................................*/
	void prepareAndDisplayDialogForAWTThread () {
		prepareDialog();
		setVisible(true);
		setFoci();
	}
	/*.................................................................................................................*/
	public void prepareAndDisplayDialog (boolean onAWTThreadOnly) {
		if (onAWTThreadOnly){
			try {
				SwingUtilities.invokeAndWait(new PADDThread(this, true));
			} catch (InterruptedException e) {
				prepareDialog();
				setVisible(true);
				setFoci();
			} catch (InvocationTargetException e) {
				prepareDialog();
				setVisible(true);
				setFoci();
			}
		}
		else {
			prepareDialog();
			setVisible(true);
			setFoci();
		}
	}
	class PADDThread implements Runnable {
		ExtensibleDialog parent;
		public PADDThread(ExtensibleDialog parent, boolean vis){
			this.parent = parent;
		}
		public void run(){
			parent.prepareAndDisplayDialogForAWTThread();
		}
	}
	/*.................................................................................................................*/
	public void prepareAndDisplayDialog () {
		prepareAndDisplayDialog(false);
	}

	/*.................................................................................................................*/
	public void setSize(int w, int h){
		super.setSize(w,h);
	}
	/*.................................................................................................................*/
	public void setBounds(int x, int y, int w, int h){
		super.setBounds(x, y, w,h);
	}
	/*.................................................................................................................*/
	public void componentResized(ComponentEvent e){
		if (e==null || e.getComponent()!= getParentDialog())
			return;
		if (d == null){
		}
		else if (getBounds().width< d.width || getBounds().height<d.height) {
			setSize(d);
		}
	}
	/*.................................................................................................................*/
	public void setDefaultComponent (Component component) {
		focalComponent = component;
	}
	/*.................................................................................................................*/
	/**Can be overrided to provide extra panels above button row*/
	public void addAuxiliaryDefaultPanels(){//************
	}
	/*.................................................................................................................*/
	public void completeDialog () {
		addAuxiliaryDefaultPanels();//************
		addPrimaryButtonRow(defaultLabel, defaultCancelLabel);
		prepareDialog();
	}
	/*.................................................................................................................*/
	public void completeAndShowDialog () {
		addAuxiliaryDefaultPanels();//************
		addPrimaryButtonRow(defaultLabel, defaultCancelLabel);
		prepareAndDisplayDialog();
	}
	/*.................................................................................................................*/
	public void completeAndShowDialog (boolean setDefaultToOK) {
		addDefaultPanelsListener(setDefaultToOK, null);
		prepareAndDisplayDialog();
	}
	/*.................................................................................................................*/
	public void completeAndShowDialog (boolean setDefaultToOK, ActionListener actionListener) {
		addDefaultPanelsListener(setDefaultToOK, actionListener);
		prepareAndDisplayDialog();
	}
	/*.................................................................................................................*/
	public void completeAndShowDialog (boolean setDefaultToOK, ActionListener actionListener, boolean onAWTThreadOnly) {
		addDefaultPanelsListener(setDefaultToOK, actionListener);
		prepareAndDisplayDialog(onAWTThreadOnly);
	}
	/*.................................................................................................................*/
	private void addDefaultPanelsListener (boolean setDefaultToOK, ActionListener actionListener) {
		addAuxiliaryDefaultPanels();//************
		addPrimaryButtonRowListener(defaultLabel, defaultCancelLabel, actionListener);
		if (setDefaultToOK)
			setDefaultButton(defaultLabel);
		else 
			setDefaultButton(defaultCancelLabel);
	}
	/*.................................................................................................................*/
	public void completeDialog (String buttonLabel0, String buttonLabel1, boolean setDefaultToOK, ActionListener actionListener) {
		addAuxiliaryDefaultPanels();//************
		addPrimaryButtonRowListener(buttonLabel0, buttonLabel1,actionListener);
		if (setDefaultToOK)
			setDefaultButton(buttonLabel0);
		prepareDialog();
	}
	/*.................................................................................................................*/
	public void completeAndShowDialog (String buttonLabel0, String buttonLabel1, boolean setDefaultToOK, ActionListener actionListener) {
		addAuxiliaryDefaultPanels();//************
		addPrimaryButtonRowListener(buttonLabel0, buttonLabel1,actionListener);
		if (setDefaultToOK)
			setDefaultButton(buttonLabel0);
		prepareAndDisplayDialog();
	}
	/*.................................................................................................................*/
	public void completeDialog (String buttonLabel0, String buttonLabel1, String buttonLabel2, String defaultButton) {
		addAuxiliaryDefaultPanels();//************
		addPrimaryButtonRow(buttonLabel0, buttonLabel1,buttonLabel2);
		//if (defaultButton!=null)  //David: why is this here? If not here, how do we disable default buttons?
		setDefaultButton(defaultButton);
		prepareDialog();
	}
	/*.................................................................................................................*/
	public void completeAndShowDialog (String buttonLabel0, String buttonLabel1, String buttonLabel2, String defaultButton) {
		addAuxiliaryDefaultPanels();//************
		addPrimaryButtonRow(buttonLabel0, buttonLabel1,buttonLabel2);
		//if (defaultButton!=null)  //David: why is this here? If not here, how do we disable default buttons?
		setDefaultButton(defaultButton);
		prepareAndDisplayDialog();
	}
	/*.................................................................................................................*/
	void addListableToList(Listable[] listables, boolean[] isSubchoice, DoubleClickList list, int priorityLevel) {
		for (int i=0; i<listables.length; i++) {
			if (listables[i]!=null){
				if (!MenuOwner.considerPriorities || ((listables[i] instanceof Priority && ((Priority)listables[i]).getPriority()==priorityLevel) || (!(listables[i] instanceof Priority) && priorityLevel==0))) {
					String name = "";
					if (listables[i] instanceof SpecialListName && ((SpecialListName)listables[i]).getListName()!=null)
						name = ((SpecialListName)listables[i]).getListName();
					else if (listables[i].getName()!=null)
						name = listables[i].getName();
					if (isSubchoice != null && isSubchoice[i])
						name = "  - " + name;
					list.add(StringUtil.truncateIfLonger(name, maxLengthInList), -1);
				}
			}
		}
	}
	/*.................................................................................................................*/
	static int maxLengthInList = 80;
	public DoubleClickList createListenedList (Object names, MesquiteInteger selected, int numElements, ActionListener actionListener, ItemListener itemListener, boolean multipleMode) {
		return createListenedList (names, null, selected, numElements, actionListener, itemListener, multipleMode);
	}
	public DoubleClickList createListenedList (Object names, boolean[] isSubchoice, MesquiteInteger selected, int numElements, ActionListener actionListener, ItemListener itemListener, boolean multipleMode) {
		String[] strings = null;
		Listable[] listables = null;
		if (names != null)
			if (names instanceof Listable[])
				listables = (Listable[])names;
			else if (names instanceof String[])
				strings = (String[])names;
		DoubleClickList list = new DoubleClickList (numElements, multipleMode); 
		list.setDoubleClickListener(this);
		list.setBackground(Color.white);
		list.addItemListener(itemListener);
		list.addActionListener(actionListener);
		if (listables!=null){
			if (MenuOwner.considerPriorities)
				for (int i=1; i<MenuOwner.MAXPRIORITY; i++)
					addListableToList(listables, isSubchoice, list, i);
			addListableToList(listables, isSubchoice, list, 0);
			if (selected!=null && selected.getValue()>=0 && selected.getValue()<listables.length)
				list.select(selected.getValue());
		}
		else if (strings!=null){
			for (int i=0; i<strings.length; i++)
				if (strings[i]!=null)
					list.add(StringUtil.truncateIfLonger(strings[i], maxLengthInList), -1);
			if (selected!=null && selected.getValue()>=0 && selected.getValue()<strings.length)
				list.select(selected.getValue());
		}
		return list;
	}
	/*.................................................................................................................*/
	/**Adds a panel with three labels closely stacked, one on top of the other. */
	public Panel addStackedLabels (String s1, String s2, String s3, int boldString) {  
		Panel newPanel = addNewDialogPanel();
		GridBagConstraints gridConstraints=null;
		GridBagLayout gridBagLabels = new GridBagLayout();
		gridConstraints = new GridBagConstraints();
		gridConstraints.gridwidth=1;
		gridConstraints.gridheight=1;
		/*  enable these if you want it left-justified
	       gridConstraints.ipadx=1;
	       gridConstraints.ipady=1;
	        gridConstraints.weightx=1;
	       gridConstraints.weighty=1;
		 */
		/* enable this if you want them in the center, but left-justified with respect to each other
	        gridConstraints.fill=GridBagConstraints.HORIZONTAL;
		 */

		newPanel.setLayout(gridBagLabels);
		gridBagLabels.setConstraints(newPanel,gridConstraints);
		gridConstraints.gridy = 0;
		gridConstraints.gridx=1; 
		/*
	        if (StringUtil.blank(s3))
	        	newPanel.setLayout(new GridLayout(2,1));
	        else
	        	newPanel.setLayout(new GridLayout(3,1));
		 */
		Label label;
		if (!StringUtil.blank(s1)) {
			gridConstraints.gridy++;
			label = new Label(s1);
			if (boldString==1)
				label.setFont(boldFont);
			newPanel.add(label,gridConstraints);
		}
		if (!StringUtil.blank(s2)) {
			gridConstraints.gridy++;
			label = new Label(s2);
			if (boldString==2)
				label.setFont(boldFont);
			newPanel.add(label,gridConstraints);
		}
		if (!StringUtil.blank(s3)) {
			gridConstraints.gridy++;
			label = new Label(s3);
			if (boldString==3)
				label.setFont(boldFont);
			newPanel.add(label);
		}
		return newPanel;
	}
	/*.................................................................................................................*/
	/**Adds a panel with two labels closely stacked, one on top of the other. */
	public Panel addStackedLabels (String s1, String s2, int boldString) {
		return addStackedLabels(s1,s2,null,boldString);
	}
	/*.................................................................................................................*/
	/**Adds a panel with two labels closely stacked, one on top of the other, with the second on in a smaller font. */
	public void addStackedLabelsBottomSmall (String s1, String s2) {  
		Panel newPanel = addNewDialogPanel();
		//		newPanel.setLayout(new GridLayout(1,1));
		GridBagLayout gridBagLabels = new GridBagLayout();
		GridBagConstraints gridConstraints = new GridBagConstraints();
		gridConstraints.gridwidth=1;
		gridConstraints.gridheight=1;
		gridConstraints.fill=GridBagConstraints.BOTH;
		gridConstraints.anchor = GridBagConstraints.WEST;

		newPanel.setLayout(gridBagLabels);
		gridBagLabels.setConstraints(newPanel,gridConstraints);
		gridConstraints.gridy = 0;
		gridConstraints.gridx=1;
		newPanel.setLayout(new GridLayout(2,1));
		Label label;
		if (!StringUtil.blank(s1)) {
			gridConstraints.gridy++;
			label = new Label(s1);
			newPanel.add(label);
		}
		if (!StringUtil.blank(s2)) {
			gridConstraints.gridy++;
			label = new Label(s2);
			label.setFont(defaultSmallFont);
			newPanel.add(label);
		}
	}
	/*.................................................................................................................*/
	public JLabel addLabelSmallText (String s) {

		Panel newPanel;
		JLabel label = new JLabel(s);

		label = new JLabel(s);
		newPanel = addNewDialogPanel();
		newPanel.add(label);
		label.setFont(defaultSmallFont);

		return label;
	}
	/*.................................................................................................................*/
	public JLabel addLabel (String s, int alignment, boolean doAlignment, boolean isBold) {

		Panel newPanel;
		JLabel label = new JLabel(s);

		if (doAlignment && alignment == Label.LEFT) {
			newPanel = addNewDialogPanel();
			newPanel.add(label);
			newPanel.setLayout(new GridLayout(1,1));
			label.setHorizontalAlignment(JLabel.LEFT);
			if (isBold)
				label.setFont(boldFont);
		}
		else {
			label = new JLabel(s);
			newPanel = addNewDialogPanel();
			newPanel.add(label);
			if (isBold)
				label.setFont(boldFont);
		}
		return label;
	}
	/*.................................................................................................................*/
	public JLabel addLabel (String s, int alignment, boolean doAlignment) {
		return addLabel(s,alignment,doAlignment, false);
	}
	/*.................................................................................................................*/
	public JLabel addBoldLabel (String s) {
		return addLabel(s,0,false,true);
	}
	/*.................................................................................................................*/
	public JLabel addLabel (String s) {
		return addLabel(s,0,false,false);
	}
	/*.................................................................................................................*/
	public JLabel addLabel (String s, int alignment) {
		return addLabel(s,alignment,true,false);
	}
	/*.................................................................................................................*/
	public void addLargeOrSmallTextLabel (String message) {
		if (message!=null) {
			int totalLength = StringUtil.getStringDrawLength(getParentDialog(), message);
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			if (totalLength>(screenSize.width/4) || message.indexOf("\n")>=0 || message.indexOf("\r")>=0)
				addLargeTextLabel(message); // can be way too wide
			else
				addLabel(message);
		}
	}
	public Choice addPopUpMenu (JLabel messageLabel, String[] choices, int defaultChoice) {
		Panel newPanel = addNewDialogPanel();
		newPanel.add(messageLabel);

		Choice choice = new Choice();
		if (choices!=null){
			int numChoices=0;
			for (int i=0; i<choices.length; i++) 
				if (!StringUtil.blank(choices[i])) {
					choice.add(choices[i]);
					numChoices++;
				}
			if ((numChoices>0) && (defaultChoice<numChoices))
				choice.select(defaultChoice);
		}
		newPanel.add(choice);
		return choice;
	}
	/*.................................................................................................................*/
	public Choice addPopUpMenu (String message, String[] choices, int defaultChoice) {
		JLabel label = new JLabel (message);
		return addPopUpMenu(label, choices, defaultChoice);
	}
	/*.................................................................................................................*/
	public Choice addPopUpMenu (String message, String string1, String string2, String string3, String string4, int defaultChoice) {
		Panel newPanel = addNewDialogPanel();
		Label label = new Label (message);
		newPanel.add(label);

		Choice choice = new Choice();
		if (string1!=null)
			choice.add(string1);
		if (string2!=null)
			choice.add(string2);
		if (string3!=null)
			choice.add(string3);
		if (string4!=null)
			choice.add(string4);
		if (choice.getItemCount()>0)
			choice.select(defaultChoice);
		newPanel.add(choice);
		return choice;
	}
	/*.................................................................................................................*/
	public Choice addPopUpMenu (String message, ListableVector choices, int defaultChoice) {
		Panel newPanel = addNewDialogPanel();
		Label label = new Label (message);
		newPanel.add(label);

		Choice choice = new Choice();
		if (choices!=null)
			for (int i=0; i<choices.size(); i++) {
				if (choices.elementAt(i).getName()!=null)
					choice.add(choices.elementAt(i).getName());
			}
		if (choice.getItemCount()>0)
			choice.select(defaultChoice);
		newPanel.add(choice);
		return choice;
	}
	/*.................................................................................................................*/
	public Choice addPopUpMenu (String message, Listable[] choices, int defaultChoice) {
		Panel newPanel = addNewDialogPanel();
		Label label = new Label (message);
		newPanel.add(label);

		Choice choice = new Choice();
		if (choices!=null)
			for (int i=0; i<choices.length; i++) {
				if (choices[i].getName()!=null)
					choice.add(choices[i].getName());
			}
		if (choice.getItemCount()>0)
			choice.select(defaultChoice);
		newPanel.add(choice);
		return choice;
	}
	/*.................................................................................................................*/
	public Choice addPopUpMenu (String message, String string1, String string2, String string3, int defaultChoice) {
		return addPopUpMenu(message, string1, string2, string3, null, defaultChoice);
	}
	/*.................................................................................................................*/
	public Choice addPopUpMenu (String message, String string1, String string2, int defaultChoice) {
		return addPopUpMenu(message, string1, string2, null, null, defaultChoice);
	}
	/*.................................................................................................................*/
	public Choice addPopUpMenu (String message) {
		Panel newPanel = addNewDialogPanel();
		Label label = new Label (message);
		newPanel.add(label);

		Choice choice = new Choice();
		newPanel.add(choice);
		return choice;
	}
	/*.................................................................................................................*/
	public Choice addPopUpMenu (String message, Class dutyClass, int defaultChoice) {
		String[] choices = getModuleList(dutyClass);
		JLabel label = new JLabel (message);
		return addPopUpMenu(label, choices, defaultChoice);
	}
	/*.................................................................................................................*/
	public String[] getModuleList(Class dutyClass) {
		StringArray stringArray = new StringArray(5);
		if (dutyClass!=null) {  //module dutyClass specified; need to create list of modules to choose
			MesquiteModuleInfo mbi=null;
			int count = 0;
			while (count++<128 && (mbi = MesquiteTrunk.mesquiteModulesInfoVector.findNextModule(dutyClass, mbi))!=null) {
				if (mbi.getUserChooseable()) {
					stringArray.addAndFillNextUnassigned(mbi.getName());
				}
			}
			return stringArray.getFilledStrings();
		}
		return null;
	}
	/*.................................................................................................................*/
	public String getModuleClassName(Class dutyClass, int index) {
		if (dutyClass!=null) { 
			MesquiteModuleInfo mbi=null;
			int count = -1;
			while (count++<128 && (mbi = MesquiteTrunk.mesquiteModulesInfoVector.findNextModule(dutyClass, mbi))!=null) {
				if (mbi.getUserChooseable()) {
					if (count==index)
						return mbi.getClassName();
				}
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	public int getModuleClassNumber(Class dutyClass, String className) {
		if (dutyClass!=null) { 
			MesquiteModuleInfo mbi=null;
			int count = -1;
			while (count++<128 && (mbi = MesquiteTrunk.mesquiteModulesInfoVector.findNextModule(dutyClass, mbi))!=null) {
				if (mbi.getUserChooseable()) {
					if (className.equalsIgnoreCase(mbi.getClassName()))
						return count;
				}
			}
		}
		return 0;
	}

	/*.................................................................................................................*/
	public void addBlankLine () {
		Panel newPanel = addNewDialogPanel();
	}
	/*.................................................................................................................*/
	public void addHorizontalLine (int thickness) {
		addHorizontalLine(thickness,0);
	}
	/*.................................................................................................................*/
	public void addHorizontalLine (int thickness, int startx) {
		forceNewPanel();
		Panel newPanel = addNewDialogPanel();
		newPanel.add(new HorizontalLine(this, thickness,startx));
	}
	/*.................................................................................................................*/
	public void addNormalHorizontalLine (int thickness) {
		Panel newPanel = addNewDialogPanel();
		//addABlackImage(newPanel,thickness);
		//newPanel.setLayout(gridBag);
		newPanel.add(new HorizontalLine(this, thickness));
	}
	/*.................................................................................................................*
	public ImagePanel addABlackImage (Panel panel,int height) {
		Image image = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "blackPixel.gif");
		ImagePanel imagePanel = new ImagePanel(image);
		imagePanel.setForcedImageHeight(height);
		imagePanel.setForcedImageWidth(20);

		if (panel==null) {
			if (lastPanel!=null) 
				lastPanel.add(imagePanel);
		}
		else 	
			panel.add(imagePanel);
		return imagePanel;
	}
	/*.................................................................................................................*/
	public Checkbox addCheckBox (String s, boolean value) {
		Panel newPanel = addNewDialogPanel();
		Checkbox checkbox =new Checkbox(s, null, value);
		newPanel.add(checkbox);
		return checkbox;
	}
	/*.................................................................................................................*/
	public void addGraphicsPanel (DialogGraphicsPanel panel) {
		Panel newPanel = addNewDialogPanel();
		newPanel.add(panel);
	}

	/*.................................................................................................................*/
	public List addList (Object names, MesquiteInteger selected, String message) {
		return addList(names, selected, message, 4);
	}
	/*.................................................................................................................*/
	public List addList (Object names, MesquiteInteger selected, String message, int numLines) {
		if (message!=null)
			constraints.fill=GridBagConstraints.NONE;
		Panel newPanel = addNewDialogPanel();
		if (message!=null) {
			newPanel.add(new Label(message));
		}
		List list = new List(numLines,false);

		newPanel.setLayout(new GridLayout(1,1));
		newPanel.add(list);
		focalComponent = list;
		constraints.fill=GridBagConstraints.BOTH;

		String[] strings = null;
		Listable[] listables = null;
		if (names != null)
			if (names instanceof Listable[])
				listables = (Listable[])names;
			else if (names instanceof ListableVector)
				listables = ((ListableVector)names).getListables();
			else if (names instanceof String[])
				strings = (String[])names;
		list.setBackground(Color.white);
		if (listables!=null){
			for (int i=0; i<listables.length; i++) {
				if (listables[i]!=null){
					if (listables[i] instanceof SpecialListName && ((SpecialListName)listables[i]).getListName()!=null)
						list.add(StringUtil.truncateIfLonger(((SpecialListName)listables[i]).getListName(), maxLengthInList), -1);
					else if (listables[i].getName()!=null)
						list.add(StringUtil.truncateIfLonger(listables[i].getName(), maxLengthInList), -1);
				}
			}
			if (selected!=null && selected.getValue()>=0 && selected.getValue()<listables.length)
				list.select(selected.getValue());
		}
		else if (strings!=null){
			for (int i=0; i<strings.length; i++)
				if (strings[i]!=null)
					list.add(StringUtil.truncateIfLonger(strings[i], maxLengthInList), -1);
			if (selected!=null && selected.getValue()>=0 && selected.getValue()<strings.length)
				list.select(selected.getValue());
		}
		return list;
	}
	/*.................................................................................................................*/
	public SingleLineTextField addTextField (String message, String initialString, int fieldLength, boolean preserveBlanks) {
		if (message!=null)
			constraints.fill=GridBagConstraints.NONE;
		Panel newPanel = addNewDialogPanel();
		Label fieldLabel = null;
		if (message!=null) {
			newPanel.add(fieldLabel = new Label(message));
		}
		SingleLineTextField textField;
		if (initialString == null)
			initialString = "";
		if (fieldLength>=1) {
			textField =new SingleLineTextField(initialString,fieldLength, preserveBlanks);
		}
		else {
			newPanel.setLayout(new GridLayout(1,1));
			textField =new SingleLineTextField(initialString, preserveBlanks);
			textField.setSize(new Dimension(dialogWidth-sideBuffer*2, textField.getSize().height));
			newPanel.setSize(textField.getSize());
		}
		textField.setBackground(Color.white);
		textField.setLabel(fieldLabel);
		newPanel.add(textField);
		focalComponent = textField;
		textField.selectAll();
		try {
			if (MesquiteTrunk.getJavaVersionAsDouble() >= 1.5)
				textField.setCaretPosition(0);
		}
		catch (Exception e){
		}
		constraints.fill=GridBagConstraints.BOTH;
		return textField;
	}
	/*.................................................................................................................*/
	public SingleLineTextField addTextField (String message, String initialString, int fieldLength) {
		return addTextField(message, initialString, fieldLength, false);
	}
	/*.................................................................................................................*/
	public SingleLineTextField addTextField (String initialString, int fieldLength) {
		return addTextField(null, initialString, fieldLength, false);
	}
	/*.................................................................................................................*/
	public SingleLineTextField addTextField (String initialString) {
		return addTextField(null, initialString, -1, false);
	}
	/*.................................................................................................................*/
	public SingleLineTextArea addSingleLineTextArea (String initialString, int numRows) {
		forceNewPanel();
		if (initialString == null)
			initialString = "";
		Panel newPanel = addNewDialogPanel();
		newPanel.setLayout(new GridLayout(1,1));
		SingleLineTextArea textArea = new SingleLineTextArea(initialString,numRows,40,TextArea.SCROLLBARS_VERTICAL_ONLY, !hasDefaultButton());
		//		Dimension d = textArea.getPreferredSize();
		textArea.setSize(new Dimension(dialogWidth-sideBuffer*2, textArea.getSize().height));
		textArea.setBackground(Color.white);
		newPanel.add(textArea);
		focalComponent = textArea;
		return textArea;
	}
	/*.................................................................................................................*/
	public SingleLineTextField[] addNameFieldsRow (int numFields, String[] labels, int[] widths) {

		SingleLineTextField[] textFields = new SingleLineTextField [numFields]; 

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridwidth=1;
		constraints.gridheight=1;
		constraints.fill=GridBagConstraints.BOTH;

		Panel newPanel = new Panel();
		newPanel.setLayout(gridBag);
		gridBag.setConstraints(newPanel,constraints);
		constraints.gridy = 1;

		if (labels!=null){
			for (int i = 0; i<numFields && i<labels.length; i++) {
				constraints.gridx=i+1;
				newPanel.add(new Label(labels[i]),constraints);
			}
		}

		constraints.gridy=2;
		for (int i = 0; i<numFields && i<labels.length; i++) {
			constraints.gridx=i+1;
			if (widths!=null && i<widths.length)
				textFields[i] = new SingleLineTextField("",widths[i]);
			else
				textFields[i] = new SingleLineTextField("",10);
			newPanel.add(textFields[i],constraints);					
		}

		addNewDialogPanel(newPanel);
		return textFields;
	}

	/*.................................................................................................................*/
	public SingleLineTextField[][] addNameFieldsMatrix(int numFields, int numRows, String[] labels, int[] widths) {

		SingleLineTextField[][] textFields = new SingleLineTextField [numFields][numRows]; 

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridwidth=1;
		constraints.gridheight=1;
		constraints.fill=GridBagConstraints.BOTH;

		Panel newPanel = new Panel();
		newPanel.setLayout(gridBag);
		gridBag.setConstraints(newPanel,constraints);
		constraints.gridy = 1;

		if (labels!=null){
			for (int i = 0; i<numFields && i<labels.length; i++) {
				constraints.gridx=i+1;
				newPanel.add(new Label(labels[i]),constraints);
			}
		}

		for (int j = 0; j<numRows; j++) {
			constraints.gridy=j+2;
			for (int i = 0; i<numFields && i<labels.length; i++) {
				constraints.gridx=i+1;
				if (widths!=null && i<widths.length)
					textFields[i][j] = new SingleLineTextField("",widths[i]);
				else
					textFields[i][j] = new SingleLineTextField("",10);
				newPanel.add(textFields[i][j],constraints);		
			}
		}

		addNewDialogPanel(newPanel);
		return textFields;
	}
	/*.................................................................................................................*/
	public SimpleIntegerField[][] addIntegerFieldsMatrix(int numFields, int numRows, String[] rowLabels, String[] columnLabels, int[] widths, int[][] initialValues, int min, int max) {

		SimpleIntegerField[][] textFields = new SimpleIntegerField [numFields][numRows]; 

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridwidth=1;
		constraints.gridheight=1;
		constraints.fill=GridBagConstraints.BOTH;

		Panel newPanel = new Panel();
		newPanel.setLayout(gridBag);
		gridBag.setConstraints(newPanel,constraints);
		constraints.gridy = 1;

		if (columnLabels!=null){
			constraints.gridx=1;
			if (rowLabels!=null)
				newPanel.add(new Label(""),constraints);

			for (int i = 0; i<numFields && i<columnLabels.length; i++) {
				constraints.gridx=i+2;
				newPanel.add(new Label(columnLabels[i]),constraints);
			}
		}

		//	public IntegerField (ExtensibleDialog dialog, String message, int initialValue, int fieldLength, int min, int max) {

		for (int j = 0; j<numRows; j++) {
			constraints.gridy=j+2;
			constraints.gridx=1;
			if (rowLabels!=null)
				if (j>=rowLabels.length || rowLabels[j]==null)
					newPanel.add(new Label(""),constraints);
				else
					newPanel.add(new Label(rowLabels[j]),constraints);

			for (int i = 0; i<numFields && i<columnLabels.length; i++) {
				constraints.gridx=i+2;
				int initValue = 1;
				if (i<initialValues.length && j<initialValues[i].length)
					initValue = initialValues[i][j];
				if (widths!=null && i<widths.length)
					textFields[i][j] = new SimpleIntegerField(initValue, widths[i],min,max);
				else
					textFields[i][j] = new SimpleIntegerField(initValue, 3,min,max);
				newPanel.add(textFields[i][j],constraints);		
			}
		}

		addNewDialogPanel(newPanel);
		return textFields;
	}


	/*.................................................................................................................*/
	public Checkbox[][] addCheckboxMatrix(int numColumns, int numRows, String[] columnLabels, String[] rowLabels) {

		Checkbox[][] textFields = new Checkbox [numColumns][numRows]; 

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridwidth=1;
		constraints.gridheight=1;
		constraints.fill=GridBagConstraints.BOTH;

		Panel newPanel = new Panel();
		newPanel.setLayout(gridBag);
		gridBag.setConstraints(newPanel,constraints);
		constraints.gridy = 1;

		if (columnLabels!=null){
			for (int i = 0; i<numColumns && i<columnLabels.length; i++) {
				constraints.gridx=i+2;
				newPanel.add(new Label(columnLabels[i]),constraints);
			}
		}

		for (int j = 0; j<numRows; j++) {
			constraints.gridy=j+2;
			constraints.gridx=1;

			newPanel.add(new Label(rowLabels[j]),constraints);
			for (int i = 0; i<numColumns; i++) {
				constraints.gridx=i+2;
				textFields[i][j] = new Checkbox("",null, true);
				newPanel.add(textFields[i][j],constraints);		
			}
		}

		addNewDialogPanel(newPanel);
		return textFields;
	}



	/*.................................................................................................................*/
	public TextArea addTextArea (String initialString, int numRows, Font font) {
		return addTextArea(initialString, numRows, 40, font);
	}
	/*.................................................................................................................*/
	public TextArea addTextArea (String initialString, int numRows, int numColumns, Font font, int scrollbars, boolean textAreaFullDialogWidth) {
		forceNewPanel();
		if (initialString == null)
			initialString = "";
		Panel newPanel = addNewDialogPanel();
		if (textAreaFullDialogWidth)
			newPanel.setLayout(new GridLayout(1,1)); 
		TextArea textArea = new TextArea(initialString,numRows,numColumns,scrollbars);
		//textArea.setSize(new Dimension(dialogWidth-sideBuffer*2, textArea.getSize().height));
		textArea.setBackground(Color.white);
		if (font !=null)
			textArea.setFont(font);
		newPanel.add(textArea);
		//lastTextPanel = newPanel;
		focalComponent = textArea;
		return textArea;
	}
	/*.................................................................................................................*/
	public TextArea addTextArea (String initialString, int numRows, int numColumns, Font font) {
		return addTextArea(initialString, numRows, numColumns, font, TextArea.SCROLLBARS_VERTICAL_ONLY, true);
	}
	/*.................................................................................................................*/
	public TextArea addTextArea (String initialString, int numRows) {
		return addTextArea(initialString,numRows,null);
	}
	/*.................................................................................................................*/
	public TextArea addTextAreaVerySmallFont (String initialString, int numRows, int numColumns, int scrollbars) {
		return addTextArea(initialString,numRows,numColumns, defaultVerySmallFont, scrollbars,true);
	}
	/*.................................................................................................................*/
	public TextArea addTextAreaSmallFont (String initialString, int numRows, int numColumns) {
		return addTextArea(initialString,numRows,numColumns, defaultSmallFont);
	}
	/*.................................................................................................................*/
	public TextArea addTextAreaSmallFontNoScroll (String initialString, int numRows, boolean textAreaFullDialogWidth) {
		return addTextArea(initialString,numRows,40, defaultSmallFont, TextArea.SCROLLBARS_NONE, textAreaFullDialogWidth);
	}
	/*.................................................................................................................*/
	public TextArea addTextAreaSmallFont (String initialString, int numRows) {
		return addTextArea(initialString,numRows, defaultSmallFont);
	}
	/*.................................................................................................................*/
	public TextArea addLargeTextLabel (String message, Font font) {
		if (message!=null) {
			Panel newPanel = addNewDialogPanel();

			/*	        GridBagLayout panelGrid = new GridBagLayout();
	        GridBagConstraints panelGridConstraints = new GridBagConstraints();
	        panelGridConstraints.gridx=0;
	       	panelGridConstraints.gridy = 0;
	        panelGridConstraints.gridwidth=1;
	        panelGridConstraints.gridheight=1;
	        panelGridConstraints.fill=GridBagConstraints.HORIZONTAL;
	        panelGridConstraints.anchor=GridBagConstraints.CENTER;
	       	panelGridConstraints.weightx = 1.0;


			newPanel.setLayout(panelGrid);

			 */
			newPanel.setLayout(new GridLayout(1,1));
			pack();
			Dimension d = getPreferredSize();
			dialogWidth = d.width;
			if (dialogWidth<minDialogWidthWithCanvas) //this had been hardcoded at 240
				dialogWidth = minDialogWidthWithCanvas;
			else if (dialogWidth> maxDialogWidthWithCanvas) 
				dialogWidth =maxDialogWidthWithCanvas;
			if (font==null)
				font = getFont();
			MesquiteTextCanvas textCanvas = MesquiteTextCanvas.getTextCanvas(dialogWidth-sideBuffer*2, font, message);
			newPanel.add(textCanvas);
			return textCanvas;
			//newPanel.setSize(textCanvas.getPreferredSize());
		}
		return null;
	}
	/*.................................................................................................................*/
	public TextArea addLargeTextLabel (String message) {
		return addLargeTextLabel(message, null);
	}
	/*.................................................................................................................*/
	public LongField addLongField (String message, long initialValue, int fieldLength) {
		return new LongField(this,message,initialValue, fieldLength);
	}
	/*.................................................................................................................*/
	public IntegerField addIntegerField (String message, int initialValue, int fieldLength, int min, int max) {
		return new IntegerField(this,message,initialValue, fieldLength, min, max);
	}
	/*.................................................................................................................*/
	public IntegerField addIntegerField (String message, int initialValue, int fieldLength) {
		return new IntegerField(this,message,initialValue, fieldLength);
	}
	/*.................................................................................................................*/
	public IntegerField addIntegerField (String message, int fieldLength) {
		return new IntegerField(this,message, fieldLength);
	}
	/*.................................................................................................................*/
	public DoubleField addDoubleField (String message, double initialValue, int fieldLength, double min, double max) {
		return new DoubleField(this,message,initialValue, fieldLength, min, max);
	}
	/*.................................................................................................................*/
	public DoubleField addDoubleField (String message, double initialValue, int fieldLength) {
		return new DoubleField(this,message,initialValue, fieldLength);
	}
	/*.................................................................................................................*/
	public DoubleField addDoubleField (String message, int fieldLength) {
		return new DoubleField(this,message, fieldLength);
	}
	public JEditorPane addHTMLPanel(String message, int w, int h, MesquiteCommand linkTouchedCommand){
		JEditorPane tA= new MesqJEditorPane("text/html","<html></html>");
		tA.setBackground(Color.white);
		tA.setForeground(Color.black);
		tA.setVisible(true);
		if (message == null)
			message = "";
		tA.setText(message);
		tA.setEditable(false);
		ConstrJEP scrollPane;
		scrollPane = new  ConstrJEP(w, h, linkTouchedCommand); 
		tA.addHyperlinkListener(scrollPane);
		scrollPane.getViewport().add( tA,  BorderLayout.CENTER ); 

		addToDialog(scrollPane);

		return tA;
	}
	class ConstrJEP extends JScrollPane implements javax.swing.event.HyperlinkListener {
		int w, h;
		MesquiteCommand linkTouchedCommand;
		ConstrJEP(int w, int h, MesquiteCommand linkTouchedCommand){
			this.w = w; this.h = h; this.linkTouchedCommand = linkTouchedCommand;
		}
		public Dimension getPreferredSize(){
			return new Dimension(w, h);
		}
		public Dimension getMinimumSize(){
			return new Dimension(w, h);
		}
		public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent e) {
			if (e.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
				String link = e.getDescription();
				if (link != null && StringUtil.startsWithIgnoreCase(link, "http"))
					MesquiteTrunk.showWebPage(e.getURL().toString(), false);
				else  if (linkTouchedCommand != null)
					linkTouchedCommand.doItNewThread(ParseUtil.tokenize(e.getDescription()), null);

			}

		}
	}
	/*.................................................................................................................*/
	public RadioButtons addRadioButtons (String[] labels, int defaultBox) {
		return new RadioButtons(this,labels, defaultBox);
	}
	/*.................................................................................................................*/
	public int getDialogWidth() {
		return dialogWidth;
	}
	/*.................................................................................................................*/
	public void setDialogWidth(int width) {
		dialogWidth = width;
	}
	/*.................................................................................................................*/
	public void buttonHit(String buttonLabel, Button button) {
		if (buttonPressed!=null) {
			buttonPressed.setValue(getButtonNumber(buttonLabel));

		}
		if (textEdgeCompensationDetector!= null){
			Dimension d = textEdgeCompensationDetector.getSize();
			Graphics g = getGraphics();
			FontMetrics fm = g.getFontMetrics(g.getFont());
			g.dispose();
			int sw = fm.stringWidth(" ");
			int ma = fm.getMaxAscent();
			int md = fm.getMaxDescent();
			MesquiteModule.textEdgeCompensationHeight = d.height-ma-md;
			MesquiteModule.textEdgeCompensationWidth = d.width-sw;
		}
	}
	/*.................................................................................................................*/
	/* Override this to provide a check on whether the dialog box should be dismissed or not in response to hitting OK. 
	 */
	public boolean dialogValuesAcceptable () {
		return true;
	}
	/*.................................................................................................................*/
	/* Override this to change the button that will be affected by the dialogValuesAcceptable check
	 */
	public int checkValuesAcceptableButton () {
		return 0;
	}
	/*.................................................................................................................*/
	/* Override this to change the message that will be presented if the dialogValuesAcceptable check fails
	 */
	public void dialogValuesNotAcceptableWarning () {
		MesquiteMessage.notifyUser("Values unacceptable.");
	}
	/*.................................................................................................................*/
	public void checkForButtonHit(MouseEvent e){
		if (e.getComponent() instanceof Button) {
			if (e.getComponent().getBounds().contains(e.getComponent().getBounds().x+e.getX(),e.getComponent().getBounds().y+e.getY())) {
				String label = ((Button)e.getComponent()).getLabel();
				if (getButtonNumber(label)>=0) {
					if ((getButtonNumber(label)==checkValuesAcceptableButton()) && !dialogValuesAcceptable())
						dialogValuesNotAcceptableWarning();
					else {
						buttonHit(label, (Button)e.getComponent());
						if (autoDispose)
							dispose();
					}
				}
				else 
					buttonHit(label, (Button)e.getComponent());  //this added 1. 12 because buttons other than primary ones were being ignored
			}

		}
	}
	/*.................................................................................................................*/
	/* David: this overridden here because dlog was getting disposed for any button, even if not primary.  Might be 
	good to store Buttons directly and not just strings; that way to know easily if button hit was button desired */
	public void mouseReleased(MouseEvent e){
		checkForButtonHit(e);
	}
	/*.................................................................................................................*/
	public void mousePressed(MouseEvent e){
		if (MesquiteTrunk.isMacOSXYosemite() && MesquiteTrunk.isJavaVersionLessThan(1.7))  // workaround because of bug in Yosemite Java 1.6
			checkForButtonHit(e);
	}

	public void selectButton(String label){ //for use by scripting & console
		buttonHit(label, null);
		if (autoDispose)
			dispose();
	}
	/*.................................................................................................................*/
	public static int query(MesquiteWindow parent, String title) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog extensibleDialog = new ExtensibleDialog(parent, title,buttonPressed);
		extensibleDialog.completeAndShowDialog();
		extensibleDialog.dispose();
		return buttonPressed.getValue();
	}
	/*.................................................................................................................*/
	public void resetButtonPressed() {
		buttonPressed.setValue(1);
	}
	/*.................................................................................................................*/
	public int query() {
		return buttonPressed.getValue();
	}

	/*.................................................................................................................*/
	public void windowOpened(WindowEvent e){ }
	/*.................................................................................................................*/
	public void windowClosing(WindowEvent e){
		if (getButtonNumber(defaultCancelLabel)>=0) {
			buttonHit(defaultCancelLabel,null);
			dispose();
		}
		else if (getButtonNumber("Done")>=0) {
			buttonHit("Done",null);
			dispose();
		}
		else if (numButtons==1) {
			buttonPressed.setValue(0);
			dispose();
		}
	} 	
	/*.................................................................................................................*/
	public void windowClosed(WindowEvent e){}
	/*.................................................................................................................*/
	public void windowIconified(WindowEvent e){}
	/*.................................................................................................................*/
	public void windowDeiconified(WindowEvent e){}
	/*.................................................................................................................*/
	public void windowActivated(WindowEvent e){
	}
	/*.................................................................................................................*/
	public void windowDeactivated(WindowEvent e){}
	/*.................................................................................................................*/
	/** This displays the help note.  If you override this, make sure you call super.actionPerformed(e) at the end of your method so that the help system still works */
	public  void actionPerformed(ActionEvent e) {
		if   ("?".equals(e.getActionCommand())) {
			MesquiteTrunk.mesquiteTrunk.alertHTML(getHelpString(),"Mesquite Help", "NOTE");
			toFront();
		}

		//		else
		//		super.actionPerformed(e);
	}
	/*.................................................................................................................*/
	public void mouseOnImage(String imageName) {
		if   ("?".equals(imageName)) {
			MesquiteTrunk.mesquiteTrunk.alertHTML(getHelpString(),"Mesquite Help", "NOTE");
			toFront();
		}
		else if (imageName =="manual")
			showManual();
	}
	/*.................................................................................................................*/
	/** This should be overridden to process double-click events, if you have added this as a DoubleClickListener to some component */
	public void doubleClicked(Component c){
	}
}





