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


/* ======================================================================== */
/** A little panel placed within a container to indicate an ongoing calculations.  Serves like a 
thermometer window, but embedded in relevant container since there may be several ongoing
at once.*/
public class ThermoPanel extends Panel {
	long current=MesquiteLong.unassigned;
	long total = MesquiteLong.unassigned;
	String s = null;
	Color barColor = ColorDistribution.spinDark;
	Color barBackColor = ColorDistribution.spinLight;
	static final int polyBarW = 10;
	int polyBarSlant = 4;
	int polyBarOffset = 0;
	Component drawHereToo;
	
	public ThermoPanel (ProgressIndicator progressIndicator) {
		super();
		setProgressIndicator(progressIndicator);
	}
	public ThermoPanel () {
		super();
	}
	public void setExtraDraw(Component p){
		drawHereToo = p;
	}
	public void setProgressIndicator(ProgressIndicator progressIndicator){
		if (progressIndicator == null)
			return;
		barColor = progressIndicator.getBarColor();
		barBackColor = progressIndicator.getBarBackColor();
	}
	public String getText() {
		return s;
	}
	static final boolean INSTAPAINT = false; //true was causing problems for OS X 1.4.1 beta
	public void setText(String s) {
		this.s = s;
		if (INSTAPAINT) {
			Graphics g = getGraphics();
			if (g!=null){
				dr(g);
				g.dispose();
			}
		}
		else
			repaint();
	}
	public void setTime(long total, long current) {
		if (MesquiteLong.isCombinable(current))
			this.current = current;
		if (MesquiteLong.isCombinable(total)) {
			this.total = total;
			if (total<=0)
				total = 1;
		}
		if (current> total)
			current = total;
		if (INSTAPAINT) {
			Graphics g = getGraphics();
			if (g!=null){
				dr(g);
				g.dispose();
			}
		}
		else
			repaint();
	}
	
	/*.................................................................................................................*/
	public void setBarColor(Color barColor) {
		this.barColor = barColor;
	}
	/*.................................................................................................................*/
	public void setBarBackColor(Color barBackColor) {
		this.barBackColor = barBackColor;
	}
	
	/*.................................................................................................................*/
	/** This method is called to draw a simply "spinning" bar */
	void spin (Graphics g){
		int w = getBounds().width;
		int h = getBounds().height;
		polyBarSlant = h;
		Polygon spinPoly = new Polygon(new int[]{0, polyBarW, polyBarSlant+polyBarW, polyBarSlant}, new int[]{h, h, 0,0}, 4);
		int numPolys = (w + polyBarSlant)/(polyBarW*2);
		spinPoly.translate(-polyBarW+polyBarOffset - polyBarSlant,0);
		int shift = 0;
		for (int poly=0; poly<=numPolys; poly++) {
			g.setColor(barBackColor);
			//g.setColor(ColorDistribution.spinLight);
			//g.setColor(ColorDistribution.projectLight[ColorDistribution.getColorScheme(MesquiteWindow.windowOfItem(this).ownerModule)]);
			g.fillPolygon(spinPoly);
			spinPoly.translate(polyBarW,0);
			shift+=polyBarW;
			g.setColor(barColor);
			//g.setColor(ColorDistribution.spinDark);
			//g.setColor(ColorDistribution.medium[ColorDistribution.getColorScheme(MesquiteWindow.windowOfItem(this).ownerModule)]);
			g.fillPolygon(spinPoly);
			spinPoly.translate(polyBarW,0);
			shift+=polyBarW;
		}
		polyBarOffset+=2;
		if (polyBarOffset>polyBarW)
			polyBarOffset=0;
	 }
	/*.................................................................................................................*/
	
	void dr(Graphics g){
	   	if (MesquiteWindow.checkDoomed(this))
	   		return;
		int w = getBounds().width;
		int h = getBounds().height;
		if (total==0) {
			spin(g);
		}
		else if ( current == 0 || current == MesquiteLong.unassigned) {
			g.setColor(getBackground());
			g.fillRect(1,1, w-3, h-2);
		}
		else {
			g.setColor(barColor);
			int progressLine;
			try {
				progressLine = (int)(w*current/total);
				g.fillRect(1,1,progressLine, h-2);
				g.setColor(barBackColor);
				g.fillRect(progressLine+1,1, w-2 -progressLine, h-2);
			}
			catch (Exception e) {
			}
		}
		g.setColor(getForeground());
		if (!StringUtil.blank(s)) {
			g.drawString(s, 4, h-8);
		}
		g.setColor(Color.black);
		g.drawRect(0,0, w-1, h-1);
		MesquiteWindow.uncheckDoomed(this);
	}
	public void update(Graphics g){
		paint(g);
	}
	public void paint (Graphics g) {
	   	dr(g);
	   	if (drawHereToo != null){
	   		Graphics gg = drawHereToo.getGraphics();
	   		if (gg != null){
	   			dr(gg);
	   		}
	   	}
	}
}



