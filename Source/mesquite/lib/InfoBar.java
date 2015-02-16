/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)

modified 26 July 01: protected against NullPointerException if null images in paint
 */
package mesquite.lib;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.io.UnsupportedEncodingException;
import java.util.*;

import mesquite.lib.duties.*;
import mesquite.lib.simplicity.InterfaceManager;
import mesquite.lib.simplicity.SimplicityStrip;
/* ======================================================================== */
/** The information bar just above the window contents.  This has icon-buttons to choose graphics versus various text modes.
For the most part the InfoBar is responsible for controlling the display of the window itself. There are 9 modes: 
Two represent the output of the modules controlling the window (the standard graphics output and a text version).
The other seven modes are informational modes that Mesquite composes and controls to inform the user about 
the modules currently contributing to the window and their parameters, explanations, and citations.*/
public class InfoBar extends MousePanel implements Commandable {
	MesquiteWindow window;
	MesquiteInteger which;
	int defaultMenuBase = 40;
	int menuBase = defaultMenuBase;
	//	int[] tabOffsets = new int[] {54, 92, 162, 213, 272};
	int triangleWidth = 4;
	final boolean showTabs = false;  //turning off post-2.75
	final int numSpecialButtons =0;
	public static final int numModes = 5;
	static final int CLOSEINFOBAR = -1;
	Color bgColor = Color.white;
	int rightBracketMenu = -1;
	int leftBracketX = -1;
	MesquitePopup leftNotice, rightNotice;
	//	Following are modes
	int mode = 0; 
	/** The standard graphics mode by which modules display their results in the window. */
	public static final int GRAPHICS = 0;
	/** The standard text mode showing the modules' results. */
	public static final int TEXT_CONTENTS = 1;
	/** Displays the parameter Strings returned by the modules participating in the window. */
	public static final int TEXT_PARAMETERS = 2;
	/** Displays the citations for the modules. */
	public static final int TEXT_CITATIONS = 4;
	/** Displays the tree of employees currently involved in the window. */
	public static final int EMPLOYEE_TREE = 3;

	/* the following modes/constants are defunct (perhaps permanently)*/
	/** Turns on Query Mode for the window. */
	public static final int QUERYMODE = 22;
	/** Displays the log of commands sent to the modules involved in the window (currently excluding those in a MesquiteBlock of a NEXUS file) */
	public static final int TEXT_LOG = 25;
	/** Displays the explanations provide by the modules involved in the window as to what they do. */
	public static final int TEXT_EXPLANATIONS = 26;

	int[] menuOffsets;
	ContentArea[] graphics;
	TextContentArea[] text;
	InterContentArea iC;
	//HelpSearchStrip searchStrip;
	SimplicityStrip simplicityStrip;
	static Image iconImages;  
	public static Image triangleImage, triangleImageDown, popIn;
	public static Image releaseImage, prereleaseImage;
	//	public static Image[] graphicsTab, textTab, parametersTab, modulesTab, citationsTab;
	//	public static Image[] baseImage;  
	Font smallFont = new Font("SanSerif", Font.PLAIN, 12);
	MesquiteCommand searchCommand = MesquiteTrunk.makeCommand("searchKeywords", this);
	static {
		/*	baseImage=  new Image[ColorDistribution.numColorSchemes];
		graphicsTab=  new Image[ColorDistribution.numColorSchemes];
		textTab=  new Image[ColorDistribution.numColorSchemes];
		parametersTab=  new Image[ColorDistribution.numColorSchemes];
		modulesTab=  new Image[ColorDistribution.numColorSchemes];
		citationsTab=  new Image[ColorDistribution.numColorSchemes];
		//	iconImages=  MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "infoBarIcons.gif");  */
	}

	public InfoBar(MesquiteWindow window) {
		super();
		this.window = window;
		//showTabs = window.showInfoTabs();
		setLayout(null);
		menuOffsets = new int[20];

		/*
		 * searchStrip = new HelpSearchStrip(window, false);
		add(searchStrip);
		searchStrip.setSize(230, 15);
		int searchLeft = tabBase ; //+ tabOffsets[numModes-1]+40;
		if (!showTabs)
			searchLeft = tabBase;
		searchStrip.setLocation(searchLeft, 0);
		searchStrip.setVisible(true);
		 */
		if (InterfaceManager.enabled && !(window instanceof SystemWindow) && !(window instanceof mesquite.trunk.AboutWindow)){
			simplicityStrip = new SimplicityStrip(window, false);
			add(simplicityStrip);
			simplicityStrip.setSize(120, 16);
			simplicityStrip.setLocation(getWidth()-24, 1);
			simplicityStrip.setBackground(ColorDistribution.veryVeryLightGray);
			simplicityStrip.setVisible(true);
		}
		if (iconImages==null && MesquiteModule.getRootPath()!=null){ //done here instead of static in case root path not yet defined when static run
			iconImages=  MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "infoBarIcons.gif");  
			popIn = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "compactTransparent.gif");  
		}
		which = new MesquiteInteger(0);
		setFont(smallFont);
		setBackground(bgColor = ColorDistribution.veryVeryLightGray);
		leftNotice =  new MesquitePopup(this);
		leftNotice.add(new MesquiteMenuItem("Menus between " + MenuOwner.leftBracket + " " + MenuOwner.rightBracket, null, null, null));
		leftNotice.add(new MesquiteMenuItem("   are specific to current window", null, null, null));
		rightNotice =  new MesquitePopup(this);
		rightNotice.add(new MesquiteMenuItem("Menus between " + MenuOwner.leftBracket + " " + MenuOwner.rightBracket, null, null, null));
		rightNotice.add(new MesquiteMenuItem("   are specific to current window", null, null, null));

		//	searchStrip.setBackground(ColorTheme.getInterfaceBackground());
		//simplicityStrip.setBackground(ColorTheme.getInterfaceBackground());

		setCursor(Cursor.getDefaultCursor());
	}
	/*.................................................................................................................*/
	/** . */
	public void dispose(){
		window=null;

		for (int i=0; i < graphics.length; i++) 
			graphics[i]=null;
		for (int i=0; i < text.length; i++) 
			text[i]=null;
		iC = null;
		super.dispose();
	}
	/*.................................................................................................................*/
	/** Registers the content areas with the InfoBar. */
	public void setContentsArea(ContentArea[] graphics, TextContentArea[] text, InterContentArea iC){
		this.graphics=graphics;
		this.iC = iC;
		this.text = text;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Shows the status window for an employee module", "[employee number]", commandName, "showStatus")) {
			which.setValue(0);
			MesquiteModule module =findNthEmployee(window.getOwnerModule(), which, Integer.parseInt(arguments));
			if (module!=null)
				new ModuleInfoWindow(module);
		}
		else if (checker.compare(this.getClass(), "Shows the manual web page for an employee module", "[employee number]", commandName, "showManual")) {
			which.setValue(0);
			MesquiteModule module =findNthEmployee(window.getOwnerModule(), which, Integer.parseInt(arguments));
			if (module!=null)
				module.showManual();
		}

		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	public int getInfoBarWidth() {
		int width = menuBase + 20; //  +  tabOffsets[numModes-1]  + 20;
		width += 12;  // to accommodate release/nonrelease icon
		return width;
	}
	/*.................................................................................................................*/
	String[] label = new String[]{"Graphics", "Text", "Parameters", "Modules", "Citations"};
	public void paint (Graphics g) {
		if (window == null || MesquiteWindow.checkDoomed(this))
			return;
		/*if (InterfaceManager.isEditingMode()){
			Color co = g.getColor();
			g.setColor(Color.cyan);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(co);
		}*/
		int colo = window.getColorScheme();
		FontMetrics fm = g.getFontMetrics();
		if (popIn!=null && window.isPoppedOut()) g.drawImage(popIn,0,0, this);
		int height = getHeight();
		int top = 2;
		int round = 6;
		MesquiteModule mod = window.getOwnerModule() ;
		/*if (mod != null){
			MesquiteModule fc = mod.getFileCoordinator();
			if (mod.getProject() != null){
				if (MesquiteTrunk.getProjectList().g etNumProjects()>1){
					g.setColor(ColorTheme.getInterfaceBackground());
					g.fillOval(left + 7, 4, 8, 8);
				}
				g.setColor(ColorTheme.getInterfaceEdgePositive());
				g.drawOval(left + 7, 4, 8, 8);
			}
		}
		 */

		g.setColor(ColorTheme.getContentFrame());  //rounded upper left corner to feel like a screen corner, to make it easer to sense the menu
		//g.setColor(Color.green);
		if (g instanceof Graphics2D) {
			Graphics2D g2 = (Graphics2D)g;
			Stroke st = g2.getStroke();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		
			try {
				Class.forName("java.awt.geom.Path2D");
				Path2D.Float path = new Path2D.Float();
				int adjust = 0;
				int pathLeft = 0;
				int pathRight = 14;
				int pathTop = 0;
				int pathBottom = 10;
				path.moveTo(adjust+pathRight, adjust+pathTop);
				path.lineTo(adjust+pathLeft, adjust+pathTop);
				path.lineTo(adjust+pathLeft,adjust+pathBottom);
				path.curveTo(adjust+pathLeft, adjust+pathTop+(pathBottom-pathTop)/3, adjust+pathLeft+(pathRight-pathLeft)/3, adjust+pathTop, adjust+pathRight, adjust+pathTop);
				path.closePath();
				g2.fill(path);
			}
			catch (ClassNotFoundException exception) {
				g2.setStroke(new BasicStroke(4));
				g2.drawArc(-2,-2, 20, 20, -180, -90);
			//	g2.drawArc(10,10, 8, 8, -180, -90);
			}
			catch (NoClassDefFoundError exception) {
				g2.setStroke(new BasicStroke(4));
				g2.drawArc(-2,-2, 20, 20, -180, -90);
			//	g2.drawArc(10,10, 8, 8, -180, -90);
			}
			g2.setStroke(st);
		} 
		else {
			g.fillRect(0,0,2,2);
			g.fillRect(0,0,4,1);
			g.fillRect(0,0,1,4);
		}
		g.setColor(Color.black);
		
		// MENUS ================
		menuBase = defaultMenuBase;
		ToolPalette palette = window.getPalette();
		if (palette != null && palette.getWidth()>defaultMenuBase/2){
			menuBase = defaultMenuBase/2 + palette.getWidth();
		}
		int left=menuBase ; //+ tabOffsets[numModes-1];
		Vector menus = mod.getEmbeddedMenusVector();
		int count = 0;
		if (menus != null){
			g.drawString(MenuOwner.leftBracket, left-20, 16);
			leftBracketX = left-20;
			//g.fillRect(left-12, 2, 8, 8);
			menuOffsets = new int[menus.size()];
			for (int i=0; i< menuOffsets.length; i++) menuOffsets[i] = 12;
			for (int i=0; i< menus.size(); i++){
				Object m = menus.elementAt(i);
				if (m instanceof MesquitePopup){
					MesquitePopup menu = (MesquitePopup)menus.elementAt(i);

					if (menu.getItemCount()>0){
						String label = menu.getLabel();
						if (i == menus.size()-1 && label.equals("Window") && mod != MesquiteTrunk.mesquiteTrunk){
							rightBracketMenu = i;
							g.drawString(MenuOwner.rightBracket, left , 16);
							left += 26;
							//menuOffsets[count] += 26;
						}
						if (menuWithSameLabelExists(label, i, menus))
							label = label + ".";
						g.drawString(label, left, 16);
						left += fm.stringWidth(label) + 16;
						if (count< menuOffsets.length)
							menuOffsets[count++] = left - 12;

					}
				}
				else {
					MesquiteMenu menu = (MesquiteMenu)menus.elementAt(i);
					//if (menu.getItemCount()>0))
					MesquiteMessage.warnProgrammer("MENU PROBLEM: MesquiteMenu where popup should be " + menu.getLabel());
				}
			}
		//	for (int i=0; i< menuOffsets.length; i++) g.drawRect(menuOffsets[i], 0, 6, 16);

		}
		
		int preReleaseLoc = 36;
		
		String title = window.title;
		g.setColor(Color.gray);
		int leftStart = getWidth()-GraphicsUtil.stringWidth(g, title)-10;
		if (mod!=null)
			leftStart=leftStart-preReleaseLoc;
		if (leftStart<left)
			leftStart=left;
		g.drawString(title, leftStart, GraphicsUtil.stringHeight(g, title));
		g.setColor(Color.black);
		
		int rel = getWidth()  - preReleaseLoc;
		if (mod != null) {
			
			if (prereleaseImage !=null && releaseImage!=null){
				if (mod.anySubstantivePrereleases())
					g.drawImage(prereleaseImage,rel,1, this);
				//else
				//	g.drawImage(releaseImage,rel,1, this);
			}
		}
		/*if (window.queryMode)
			g.setColor(Color.red);
		else
			g.setColor(ColorTheme.getInterfaceBackground());
		g.fillRect(0,262, getBounds().width-262, getBounds().height-1);
		 */
		g.setColor(Color.black);
		//if (window.queryMode)
		//	g.drawString("QUERY MODE ON (\"?\" button turns off)",270,12);
		g.drawLine(0,getBounds().height-1, getBounds().width, getBounds().height-1);
		MesquiteWindow.uncheckDoomed(this);
	}
	/*.................................................................................................................*/
	boolean menuWithSameLabelExists(String label, int position, Vector menus){
		for (int i=0; i< menus.size() && i<position; i++){
			Object m = menus.elementAt(i);
			if (m instanceof MesquitePopup){
				MesquitePopup menu = (MesquitePopup)menus.elementAt(i);

				if (menu.getItemCount()>0){
					String menuLabel = menu.getLabel();
					if (menuLabel.equals(label))
							return true;
				}
			}
		}
		return false;
	}

	/*.................................................................................................................*/
	private String listSubstantivePrereleases(MesquiteModule module, String spacer) {
		if (!module.anySubstantivePrereleases())
			return "";
		String thisBranch = "";
		if (module.isSubstantive() && module.isPrerelease())
			thisBranch =spacer + module.getName() + ":  " + module.getExplanation() + StringUtil.lineEnding();

		int num = module.getEmployeeVector().size();
		spacer +="    ";
		for (int i=0; i<num; i++) {
			Object obj = module.getEmployeeVector().elementAt(i);
			MesquiteModule mb = (MesquiteModule)obj;
			thisBranch += listSubstantivePrereleases(mb, spacer);
		}
		return thisBranch;
	}
	/*.................................................................................................................*/
	private String listExplanations(MesquiteModule module, String spacer) {
		String thisBranch=spacer + module.getName() + ":  " + module.getExplanation() + StringUtil.lineEnding();

		int num = module.getEmployeeVector().size();
		spacer +="    ";
		for (int i=0; i<num; i++) {
			Object obj = module.getEmployeeVector().elementAt(i);
			MesquiteModule mb = (MesquiteModule)obj;
			thisBranch += listExplanations(mb, spacer);
		}
		return thisBranch;
	}
	/*.................................................................................................................*/
	private boolean citationsInPath(MesquiteModule module) {
		if  (!StringUtil.blank(module.getCitation()) || module.showCitation() || (module.isSubstantive() && module.isPrerelease())) {
			return true;
		}
		else  {
			int num = module.getEmployeeVector().size();
			for (int i=0; i<num; i++) {
				Object obj = module.getEmployeeVector().elementAt(i);
				MesquiteModule mb = (MesquiteModule)obj;
				if (citationsInPath(mb))
					return true;
			}
		}
		return false;
	}
	ListableVector citations = new ListableVector(); //reentrancy problems!
	boolean warnPrerelease = false;
	/*.................................................................................................................*/
	private String listCitations(MesquiteModule module, String spacer) {
		if (citationsInPath(module)){
			String thisBranch = "";
			String citation = module.getCitation();
			String asterisk;
			if (module.isPrerelease() && module.isSubstantive()){
				warnPrerelease = true;
				asterisk = " * ";
			}
			else asterisk = "";
			if (!module.showCitation())
				thisBranch += spacer + module.getName()+ asterisk + "\n";
			else {
				if (!StringUtil.blank(citation)) {
					int which = citations.indexOfByName(citation);
					MesquiteString ms = null;
					if (which<0) {
						ms = new MesquiteString();
						ms.setName(citation);
						ms.setValue("(" + (citations.size()+1) + ")");
						citations.addElement(ms, false);
					}
					else
						ms = (MesquiteString)citations.elementAt(which);
					thisBranch +=spacer + module.getName() + ":  " + ms.getValue() + asterisk +  "\n"; //spacer + module.getAuthors() + " " + module.getDateReleased() + ". " + module.getName() + " (version " + versionString + ")" + StringUtil.lineEnding();
				}
				else //asks to show citation but none is supplied!
					thisBranch += spacer + module.getName()+ asterisk + "\n";
			}

			int num = module.getEmployeeVector().size();
			spacer +="    ";
			for (int i=0; i<num; i++) {
				Object obj = module.getEmployeeVector().elementAt(i);
				MesquiteModule mb = (MesquiteModule)obj;
				thisBranch += listCitations(mb, spacer);
			}
			return thisBranch;
		}
		else
			return "";
	}
	/*.................................................................................................................*/
	private MesquiteModule findNthEmployee(MesquiteModule mb, MesquiteInteger which, int n){
		if (mb==null)
			return null;
		if (which.getValue()==n)
			return mb;
		int num = mb.employees.size();
		which.increment();
		for (int i=0; i<num; i++) {
			Object obj = mb.employees.elementAt(i);
			MesquiteModule mbq = (MesquiteModule)obj;
			MesquiteModule mfound = findNthEmployee(mbq, which, n);
			if (mfound!=null) 
				return mfound;
		}
		return null;
	}
	/*.................................................................................................................*/
	/** Get the current mode (i.e. which page is currently being shown) */
	public int getMode(){
		return mode;
	}
	private String getWindowStamp(){
		Date d = new Date(System.currentTimeMillis());
		String t = "Mesquite version " + MesquiteModule.getMesquiteVersion() + MesquiteModule.getBuildVersion() + "   Date: " + d.toString()  +StringUtil.lineEnding();
		MesquiteModule mod = window.getOwnerModule() ;
		if (mod != null){
			MesquiteProject p = mod.getProject();
			if (p!=null){
				t+= "Window refers to project with home file " + p.getHomeFileName() + StringUtil.lineEnding();
				t+="File path: " + p.getHomeDirectoryName() + StringUtil.lineEnding();
			}
		}
		t +="=======================" +StringUtil.lineEnding();
		return t;
	}
	/*.................................................................................................................*/
	/** Gets the text for mode m */
	public String getText(int m){
		if (window == null)
			return null;
		if (m== TEXT_CONTENTS){ //text version of contents
			return getWindowStamp() +  StringUtil.lineEnding() + window.getTextContents();
		}
		else if (m== QUERYMODE){ //window info
			MesquiteModule mod = window.getOwnerModule() ;
			if (mod == null)
				return "There is no information available for this window";
			else {
				String t = getWindowStamp();
				t += "This window is owned by the module " + mod + StringUtil.lineEnding() + "Employer path " + mod.getEmployerPath();
				return t;
			}
		}
		else if (m== TEXT_PARAMETERS){
			return  getWindowStamp() +  StringUtil.lineEnding() +"Parameters of modules participating in window: " + StringUtil.lineEnding() + StringUtil.lineEnding() + window.getOwnerModule().accumulateParameters("  ");
		}
		else if (m== TEXT_LOG){ //log
			return window.getLogText();
		}
		else if (m== TEXT_EXPLANATIONS){
			return getWindowStamp() +  StringUtil.lineEnding() +"Explanations of functions of modules participating in window: " + StringUtil.lineEnding() + StringUtil.lineEnding() + listExplanations(window.getOwnerModule(),"  ");
		}
		else if (m== TEXT_CITATIONS){ //citations
			citations.removeAllElements(false);
			warnPrerelease = false;
			MesquiteString msm = new MesquiteString();
			msm.setName(MesquiteTrunk.mesquiteTrunk.getCitation());
			msm.setValue("(1)");
			citations.addElement(msm, false);
			String cit = getWindowStamp() +  StringUtil.lineEnding() +"Citations of modules participating in window: " + StringUtil.lineEnding() + StringUtil.lineEnding() + listCitations(window.getOwnerModule(),"  ");
			if (warnPrerelease)
				cit += "* NOTE: these modules are indicated to be prerelease.  Read their documentation or contact their authors to determine if they are considered ready for publishable results." + StringUtil.lineEnding();
			for (int i = 0; i<citations.size(); i++) {
				MesquiteString ms = (MesquiteString)citations.elementAt(i);
				cit +=  StringUtil.lineEnding() + ms.getValue() + " " + ms.getName();
			}
			return cit;
		}
		else 
			return null;
	}
	/*.................................................................................................................*/
	/** Refresh the text for mode */
	public void refreshText(int mode){
		if (text!=null && mode>0 && mode<numModes && mode<text.length && text[mode]!=null){ //text versions
			text[mode].setText(getText(mode));
		}
	}

	/*.................................................................................................................*/
	/** Returns the text content area */
	public TextArea getTextArea(int mode){
		if (text!=null && mode>0 && mode<numModes && mode<text.length && text[mode] != null){ //text versions
			return text[mode].getTextArea();
		}
		return null;
	}
	/*.................................................................................................................*/
	/** Sets the mode (i.e. the page to show) */
	public void setMode(int mode){
		window.setExplanation("");
		if (mode == QUERYMODE){
			window.queryMode = !window.queryMode;
			if (window.queryMode) {
				MesquiteTrunk.mesquiteTrunk.showLogWindow(true);
				MesquiteTrunk.mesquiteTrunk.logln("\nNOTE: You have just turned on query mode.  While query mode is active for the window, the info bar will be red, and when you select menu items or buttons, explanations for them will appear in the log window.  The menu items and buttons in the window will be otherwise inactive until you hit the \"?\" again to turn query mode off.\n");
				setBackground(Color.red);
			}
			else
				setBackground(bgColor = ColorDistribution.veryVeryLightGray);
			repaint();
		}
		else if (!window.queryMode) {
			window.setGraphicsWaitCursor();
			this.mode = mode;
			refreshText(mode);
			if (mode == EMPLOYEE_TREE)
				window.updateEmployeeTree();
			window.showPage(mode);
			repaint();
			window.setGraphicsDefaultCursor();
		}
	}
	/*.................................................................................................................*/
	private void respondToButton(int m, int x, int y){
		if (m==CLOSEINFOBAR) { //close infobar
			//if (mode == GRAPHICS) //can close infobar only if graphics mode
			//	window.setShowInfoBar(false);
		}
		else if (m<numModes && m>=0) { 
			setMode(m);
		}
	}
	Vector extraButtons = new Vector();
	public MesquiteButton addExtraButton(String imagePath, MesquiteCommand command){
		MesquiteButton b;
		extraButtons.addElement(b = new MesquiteButton (window.ownerModule, command, null, true, imagePath, 14, 16));
		add(b);
		b.setVisible(true);
		b.setShowBackground(false);
		return b;
	}
	public void setSize(int w, int h){
		super.setSize(w, h);
		for (int i=0; i<extraButtons.size(); i++){
			MesquiteButton b = (MesquiteButton)extraButtons.elementAt(i);
			b.setLocation(w- ( i+1)*20, 2);

		}
		if (simplicityStrip != null)
			simplicityStrip.setLocation(w - 20, 1);

	}
	public void setBounds(int x, int y, int w, int h){
		super.setBounds(x, y, w, h);
		for (int i=0; i<extraButtons.size(); i++){
			MesquiteButton b = (MesquiteButton)extraButtons.elementAt(i);
			b.setLocation(w- ( i+1)*20, 2);
		}
		if (simplicityStrip != null)
			simplicityStrip.setLocation(w - 20, 1);

	}
	/*.................................................................................................................*/
	int whichButton(int x, int y){
		return -2;	
	}
	/*.................................................................................................................*
	public void mouseMoved(int modifiers, int x, int y, MesquiteTool tool) {
		String s="";
		int theButton = whichButton(x,y);
		if (theButton==CLOSEINFOBAR)
			s ="This button will hide the information bar.";
		else {
			if (theButton==GRAPHICS)
				s = "This tab will display a graphical view of the contents of this window. ";
			else if (theButton==TEXT_CONTENTS)
				s = "This tab will display a text view of the contents of this window. ";
			else if (theButton==TEXT_PARAMETERS)
				s = "This tab will display the parameters of the contents of this window. ";
			else if (theButton==TEXT_CITATIONS)
				s = "This tab will display information about how to cite the modules used in this window. ";
			else if (theButton==EMPLOYEE_TREE)
				s = "This tab will display the modules used in this window. ";
			if (theButton==mode)
				s+="This is the current view.";
		}
		if (window!=null)
			window.setExplanation(s);
		super.mouseMoved(modifiers,x,y,tool);
	}
	*/
	/*.................................................................................................................*/
	public void mouseDown(int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		menuBase = defaultMenuBase;
		ToolPalette palette = window.getPalette();
		if (palette != null && palette.getWidth()>defaultMenuBase/2){
			menuBase = defaultMenuBase/2 + palette.getWidth();
		}
		if (x> leftBracketX -10 && x < leftBracketX + 20){
			leftNotice.showPopup(x, 24);
			MesquiteWindow.uncheckDoomed(this);
			return;
		}
		int rel = menuBase ; //  + tabOffsets[numModes-1] ;
		for (int i=0; i< menuOffsets.length; i++) 
			if (x > menuBase -2 && x < menuOffsets[i]){
				if (i == rightBracketMenu -1 && i > 0 && x < menuOffsets[i-1] + 26){
					rightNotice.showPopup(x, 24);
						MesquiteWindow.uncheckDoomed(this);
						return;
				}
				
				MesquiteModule mod = window.getOwnerModule() ;
				Vector menus = mod.getEmbeddedMenusVector();
				if (menus != null){
					int count = 0;
					for (int k=0; k< menus.size(); k++){

						Object m = menus.elementAt(k);
						if (m instanceof MesquitePopup){
							MesquitePopup menu = (MesquitePopup)menus.elementAt(k);
							if (menu.getItemCount()>0){
								if (count == i){
									int showX = 0;
									if (i ==0)
										showX = menuBase;
									else
										showX = menuOffsets[i-1];
									if (menu.getComponent() == null)
										menu.setComponent(this);
									menu.showPopup(showX, 24);
								}
								count++;

							}
						}
					}
				}
				MesquiteWindow.uncheckDoomed(this);
				return;
			}

		if (x< menuBase){
			if (MesquiteWindow.compactWindows && window.isPoppedOut())
				window.getOwnerModule().getFileCoordinator().getModuleWindow().getParentFrame().popIn(window);
		}
		/*else if (x>=rel+7 && x <= rel+7+8){
			MesquiteModule mod = window.getOwnerModule() ;
			if (mod != null){
				MesquiteModule fc = mod.getFileCoordinator();
				if (fc != null){
					MesquiteCommand mc = fc.makeCommand("allToFront", fc);
					mc.doIt("");
					window.toFront();
				}
			}
		}*/
		else if (x>=rel + 20 && x<=rel+20 + 16) {
			MesquiteModule mb = window.getOwnerModule();
			if (mb!=null){
				if (mb.anySubstantivePrereleases()) {
					mb.alert("Among the modules participating in this window are some that are pre-release versions and that claim are involved in calculating substantive results.  To see which ones, use the Modules tab of the window.  Modules listed in orange, with an alert symbol, are prerelease and claim to be involved in substantive calculations");
					/*
					if (AlertDialog.query(mb.containerOfModule(), "Prerelease modules", "Among the modules participating in this window are some that are pre-release versions and that claim are involved in calculating substantive results.  Do you want to see a list?")){
						String intro = "These are the modules participating in this window are some that are pre-release versions and that claim are involved in calculating substantive results\n";
						AlertDialog.verboseNotice(mb.containerOfModule(), "Prerelease modules", intro + listSubstantivePrereleases(mb, "  "));
					}
					 */
				}
				//else
				//	mb.alert("All of the modules participating in this window that claim to be involved in calculating substantive results are release versions.  None is listed as a pre-release version.");
			}
		}
		else if (window!=null) {
			MesquiteModule mod = window.getOwnerModule() ;
			int b = whichButton(x,y);
			if (b>=-1 && b!=mode) {
				respondToButton(b,x,y);
			}

		}
		MesquiteWindow.uncheckDoomed(this);
	}
}


