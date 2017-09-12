/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trunk;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import mesquite.lib.*;


/* ======================================================================== */
/** The About window (the window with the mesquite leaf that appears on startup)*/
public class AboutWindow extends MesquiteWindow implements WindowListener {
	public AboutPanel aboutPanel;
	static AboutScrollPane scrollPane;
	public SplashPanel splashPanel;
	public ThermoPanel upperMessagePanel; //aboutMessagePanel, 
//	public static int scrollWidth = 16;
	public static int aboutHeight = 259;
	public static int aboutWidth = 287; 
	public static int splashWidth = 186;
	public static int splashHeight = 400;
	boolean quitIfGoAway=false;
	public static int totalWidth = aboutWidth + splashWidth+4; 
	private int splashColumns = 1;
	private int splashColumnsUsed = 1;
	private Vector splashes;
	private int numSplashes = 0;
	/** to be used only for accumulating commands*/
	public AboutWindow () {
	}
	public AboutWindow (MesquiteModule ownerModule) {
		super(ownerModule,  false);
		getParentFrame().setSavedDimensions(totalWidth, aboutHeight);
		setResizable(false);
		setWindowSize(totalWidth, aboutHeight);
		addWindowListener(this);
		resetTitle();
	//	ownerModule.setModuleWindow(this);
		splashes = new Vector();
	}
	/*.................................................................................................................*/

	public void setImage(Image logo){
		addToWindow(aboutPanel = new AboutPanel(logo));
		addToWindow(scrollPane = new AboutScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED)); 
		//scrollPane.getHAdjustable().addAdjustmentListener(this);
		//scrollPane.setWheelScrollingEnabled(true);
		setBackground(ColorDistribution.mesquiteBrown);
		aboutPanel.setSize(aboutWidth,aboutHeight);
		aboutPanel.setVisible(true);
		aboutPanel.repaint();
		aboutPanel.setLayout(null);
		aboutPanel.setBackground(ColorDistribution.mesquiteBrown);
		scrollPane.setSize(splashWidth+4, aboutHeight);
		scrollPane.setLocation(aboutWidth, 0);
		splashPanel = new SplashPanel();
		scrollPane.addSplashPanel(splashPanel);
		splashPanel.setSize(splashWidth,splashHeight);
		splashPanel.setLocation(0,0);
		splashPanel.setVisible(true);
		splashPanel.repaint();
		splashPanel.setLayout(null);
		scrollPane.setScrollPosition(0, 0);
		scrollPane.setVisible(true);
		Adjustable adj = scrollPane.getVAdjustable();
		adj.setUnitIncrement(65);
		scrollPane.doLayout();
		//splashPanel.textArea.setLocation(3, 197);
		//splashPanel.textArea.setSize(188, 60);
		//splashPanel.textArea.setVisible(true);
		int h = 26;
		aboutPanel.add(upperMessagePanel = new ThermoPanel());
		upperMessagePanel.setText("Mesquite modules loading");
		upperMessagePanel.setSize(220,h);
		upperMessagePanel.setForeground(ColorDistribution.lightYellow);
		upperMessagePanel.setBackground(ColorDistribution.mesquiteBrown);
		upperMessagePanel.setBarColor(ColorDistribution.darkMesquiteBrown);
		upperMessagePanel.setBarBackColor(ColorDistribution.mesquiteBrown);
		upperMessagePanel.setLocation(3,aboutHeight -h-2);
		upperMessagePanel.setVisible(true);
		upperMessagePanel.repaint();

		//aboutPanel.add(aboutMessagePanel = new ThermoPanel());
		
		/*aboutMessagePanel.setText("");
		//aboutMessagePanel.setEditable(false);
		aboutMessagePanel.setSize(281,h);
		aboutMessagePanel.setForeground(ColorDistribution.lightYellow);
		aboutMessagePanel.setBackground(ColorDistribution.mesquiteBrown);
		aboutMessagePanel.setBarColor(ColorDistribution.lightGreen);
		aboutMessagePanel.setBarBackColor(ColorDistribution.mesquiteBrown);
		aboutMessagePanel.setLocation(3,aboutHeight -h-2);
		aboutMessagePanel.setVisible(true);
		aboutMessagePanel.repaint();*/
		setShowExplanation(false);
	}
	public boolean isCompactible(){
		return false;
	}
	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree blocks, etc.)
	they can reset their titles properly*/
	public void resetTitle(){
		setTitle("About Mesquite");
	}
	/** Returns menu location for item to bring the window to the for (0 = custom or don't show; 1 = system area of Windows menu; 2 = after system area of Windows menu)*/
	public int getShowMenuLocation(){
		return 1;
	}
	public void checkSize(){
		int h = MesquiteModule.textEdgeCompensationHeight + 16;
		/*if (aboutMessagePanel == null)
			return;
		if (aboutMessagePanel.getSize().height != h)
			aboutMessagePanel.setSize(220,h);
		if (aboutMessagePanel.getLocation().y != aboutHeight -h)
			aboutMessagePanel.setLocation(3,aboutHeight -h-2);
		*/
		if (upperMessagePanel == null)
			return;
		if (upperMessagePanel.getSize().height != h)
			upperMessagePanel.setSize(220,h);
		if (upperMessagePanel.getLocation().y != aboutHeight -h-2)
			upperMessagePanel.setLocation(3,aboutHeight  -h-2);
	}
	public Vector getSplashes(){
		return splashes;
	}
	public void setQuitIfGoAway(boolean quitIfGoAway){
		this.quitIfGoAway = quitIfGoAway;
	}
	public void superimposePanel(HPanel p){
		if (aboutPanel!=null)
			aboutPanel.superimposePanel(p);
	}
	public void addSplash(MesquiteModuleInfo mmi){
		if (mmi !=null) {
			splashes.addElement(mmi);
			numSplashes++;
			int y = splashPanel.addSplashImage(mmi);
			
			if (numSplashes <2)//
				setWindowSize(totalWidth, aboutHeight);

		}
	}
	/*.................................................................................................................*/
	public void windowActivated(WindowEvent e) {
	}
	/*.................................................................................................................*/
	public void windowClosed(WindowEvent e) {
	}
	/*.................................................................................................................*/
	public void windowClosing(WindowEvent e) {
		if (quitIfGoAway)
			MesquiteTrunk.mesquiteTrunk.exit(false, 0);
	}
	/*.................................................................................................................*/
	public void windowDeactivated(WindowEvent e) {
	}
	/*.................................................................................................................*/
	public void windowDeiconified(WindowEvent e) {
	}
	/*.................................................................................................................*/
	public void windowIconified(WindowEvent e) {
	}
	/*.................................................................................................................*/
	public void windowOpened(WindowEvent e) {
	}
	/*.................................................................................................................*/
}
/* ======================================================================== */
/** The Panel to the right of the About window (the startup window) that shows the logos of installed packages within the Mesquite system */
class SplashPanel extends MesquitePanel {
	//static final int numDown = 3;
	//static final int numAcross = 3;
	Vector splashImages;
	Vector splashModules;
	Color brightGreen;
	//TextArea textArea;
	//boolean starting = true;
	public SplashPanel () {
		splashImages = new Vector();
		splashModules = new Vector();
		setBackground(ColorDistribution.mesquiteBrown);
	}
	public int addSplashImage(MesquiteModuleInfo mmi){ //returns scroll setting
		if (mmi !=null) {
			Image im = MesquiteImage.getImage(mmi.getDirectoryPath() + "splash.gif", false);
			if (im==null)
				im = MesquiteImage.getImage(mmi.getDirectoryPath() + "splash.jpg", false);
			if (im==null)
				im = MesquiteImage.getImage(mmi.getDirectoryPath() + "splash.jpeg", false);
			if (im!=null){
				MediaTracker mt = new MediaTracker(this);
				mt.addImage(im, 0);
				try {
					mt.waitForAll();
				} catch (Exception e) {
					MesquiteMessage.warnProgrammer("splash image exception------------");
					e.printStackTrace();
				}
				splashImages.addElement(im);
				splashModules.addElement(mmi);
				if (splashImages.size()*65>AboutWindow.splashHeight) {
					AboutWindow.splashHeight = splashImages.size()*2*65;
					setSize(AboutWindow.splashWidth,AboutWindow.splashHeight);
					AboutWindow.scrollPane.invalidate();
					AboutWindow.scrollPane.validate();
				}
				int newSplashTop = (splashImages.size()-1)*65; //top of this new splash
				int currentSplashAreaTop = AboutWindow.scrollPane.getScrollPosition().y; //what position is currently visible at top of splash
				int currentSplashAreaBottom = currentSplashAreaTop + AboutWindow.aboutHeight;//what position is currently visible at bottom of splash
				if (newSplashTop<currentSplashAreaTop || newSplashTop+65>currentSplashAreaBottom) {
					int y = newSplashTop -AboutWindow.aboutHeight +65;
					if (splashImages.size()>2)
						y += 30;
					AboutWindow.scrollPane.setScrollPosition(AboutWindow.scrollPane.getScrollPosition().x, y);
					repaint();
					return y;
				}
				repaint();
			}
		}
		return -1;
	}
	/*.................................................................................................................*/
	public void paint(Graphics g) {
	   	if (MesquiteWindow.checkDoomed(this))
	   		return;
		for (int i=0; i<splashImages.size(); i++){
			Image im = (Image)splashImages.elementAt(i);
			g.drawImage(im,0, i*65, (ImageObserver)this);
		}
		if (StringUtil.defaultFontMetrics == null) 
			StringUtil.defaultFontMetrics = g.getFontMetrics(ExtensibleDialog.defaultBigFont);
		MesquiteWindow.uncheckDoomed(this);
	}
  	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		for (int i=0; i<splashImages.size(); i++){
			if (y< i*65 + 65) {
				MesquiteModuleInfo mbi = ((MesquiteModuleInfo)splashModules.elementAt(i));
				mbi.explainSplash();
				return;
			}	
		}
	}
}

/* ======================================================================== */
class AboutScrollPane extends ScrollPane{
	public AboutScrollPane (int scrollPolicy) {
		super(scrollPolicy);
	}
	public void addSplashPanel(Component c){
		addImpl(c, null, 0);
	}
}

