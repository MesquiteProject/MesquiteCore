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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;

import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.StringUtil;
import mesquite.lib.misc.HPanel;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.InfoBar;
import mesquite.lib.ui.MQPanel;
import mesquite.lib.ui.MesquiteWindow;

/* ======================================================================== */
/** The Panel containing the Mesquite logo on the startup window */
public class AboutPanel extends MQPanel {
	Image logo;
	HPanel superimposed = null;
	public AboutPanel (Image logo) {
		this.logo = logo;
		setBackground(Color.white);
	}
	void superimposePanel(HPanel p){
		superimposed = p;
		add(p);
		p.setSize(getBounds().width, AboutWindow.aboutHeight - MesquiteModule.textEdgeCompensationHeight - 16);
	}
	/*.................................................................................................................*/
	public void paint(Graphics g) {
	   	if (superimposed!=null || MesquiteWindow.checkDoomed(this))
	   		return;
		g.drawImage(logo,0,0,(ImageObserver)this);
		
		g.setColor(ColorDistribution.lightYellow);
		g.drawString("Version " + MesquiteModule.getMesquiteVersion() + MesquiteModule.getBuildVersion(), 8,95); //was 15
		if (MesquiteTrunk.mesquiteTrunk.isPrerelease())
			g.drawString(MesquiteModule.getBuildDate() , 8,110); //was 15
		if (StringUtil.notEmpty(MesquiteModule.getSpecialVersion()))
			g.drawString( MesquiteModule.getSpecialVersion() , 8,125);
		
		if (MesquiteTrunk.substantivePrereleasesExist) {
			g.drawString( "Some installed modules", 21,12);
			g.drawString("are pre-release versions.", 21,27);
			//StringUtil.highlightString(g, "Touch on red alert symbol in windows for information.", 25,45);
			g.drawImage(InfoBar.prereleaseImage,3,3,(ImageObserver)this);
		}
		g.drawString("http://www.mesquiteproject.org", 5,210);
		g.drawString("Copyright (c) 1997-2025 W. & D. Maddison.", 5,225);
		
		MesquiteWindow.uncheckDoomed(this);
	}
}

