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
import java.awt.image.*;
import java.awt.event.*;
import mesquite.lib.duties.*;
import mesquite.trunk.ProjectTreeWindow;


/* ======================================================================== */
/** The window that displays the log. */
public class LogWindow extends ConsoleWindow implements SystemWindow {
	boolean keyHasBeenDown = false;
	int bannerHeight = 46;
	BannerPanel bannerPanel;
	HelpSearchStrip searchStrip;
	int searchHeight = 20;
	int searchLeft = 4;
	public LogWindow(String t) {
		super(MesquiteModule.mesquiteTrunk, "Log", false); //infobar
		setText(t);
		System.out.println(t);
		MesquiteFile.writeToLog(t);  
		bannerPanel = new BannerPanel(this);
		addToWindow(bannerPanel);
		bannerPanel.setSize(getWidth(), bannerHeight);
		bannerPanel.setLocation(0,0);
		bannerPanel.setVisible(true);
		searchStrip = new HelpSearchStrip(this, false);

		addToWindow(searchStrip);
		searchStrip.setBounds(searchLeft, getHeight()-searchHeight, getWidth() - searchLeft, searchHeight);
		searchStrip.setVisible(true);
		
		tA.setLocation(0, bannerHeight);
		
		//setBackground(ColorDistribution.paleGoldenRod);
		setBackground(ColorDistribution.mesquiteBrown);
		//setBackground(ColorTheme.getInterfaceBackground());

	}
	public boolean isCompactible(){
		return true;
	}
	public boolean permitViewMode(){
		return false;
	}

	/** Returns menu location for item to bring the window to the for (0 = custom or don't show; 1 = system area of Windows menu; 2 = after system area of Windows menu)*/
	public int getShowMenuLocation(){
		return 1;
	}
	public void requestFocus(){
		tA.requestFocus();
		tA.requestFocusInWindow();
	}
	public boolean keyDown(){
		return keyHasBeenDown;
	}
	public void repaintSearchStrip(){
		searchStrip.repaint();
	}
	/*.................................................................................................................*/
	public void windowResized(){
		super.windowResized();
		if (tA!=null)
			tA.setSize(getWidth(), getHeight() - bannerHeight - searchHeight); //getFullWidth
		if (bannerPanel!=null)
			bannerPanel.setSize(getWidth(), bannerHeight);
		if (searchStrip!= null)
			searchStrip.setBounds(searchLeft, getHeight()-searchHeight, getWidth() - searchLeft, searchHeight);

	}
	/** Sets the window size.  To be used instead of setSize. 
	public void containerSizeSet(int width, int height) {
	}
	/*.................................................................................................................*/
	/** Sets the window size.  To be used instead of setSize.  */
	public void setWindowSize(int width, int height) {
		super.setWindowSize(width, height);
		if (tA!=null)
			tA.setSize(getWidth(), getHeight() - bannerHeight - searchHeight);
		if (bannerPanel!=null)
			bannerPanel.setSize(getWidth(), bannerHeight);
		if (searchStrip!= null)
			searchStrip.setBounds(searchLeft, getHeight()-searchHeight, getWidth() - searchLeft, searchHeight);
	}
	public void keyTyped(KeyEvent e){
		keyHasBeenDown = true;
		super.keyTyped(e);
	}
	
	public void keyPressed(KeyEvent e){ 
		keyHasBeenDown = true;
		super.keyPressed(e);
	}
	
	public void keyReleased(KeyEvent e){
		super.keyReleased(e);
	}
}
class BannerPanel extends Panel {
	Image banner;
	int bannerHeight;
	public BannerPanel(LogWindow w){
		if (MesquiteTrunk.mesquiteTrunk.isPrerelease())
			banner=  MesquiteImage.getImage(MesquiteModule.getRootPath() +"images" + MesquiteFile.fileSeparator + "bannerBeta.gif");  
		else
			banner=  MesquiteImage.getImage(MesquiteModule.getRootPath() +"images" + MesquiteFile.fileSeparator + "banner.gif");  
		bannerHeight = w.bannerHeight;
		setBackground(ColorDistribution.mesquiteBrown);

	}
	
	public void paint(Graphics g) {
	   	if (MesquiteWindow.checkDoomed(this))
	   		return;
		g.drawImage(banner, 0, 0, this);
		g.setColor(ColorDistribution.lightYellow);
		if (StringUtil.notEmpty(MesquiteModule.getSpecialVersion()))
			g.drawString( MesquiteModule.getSpecialVersion() , 224,bannerHeight-18);
		g.drawString( "Version " + MesquiteModule.getMesquiteVersion() + " " + MesquiteModule.getBuildVersion() , 224,bannerHeight-4);
		MesquiteWindow.uncheckDoomed(this);
	}
}


