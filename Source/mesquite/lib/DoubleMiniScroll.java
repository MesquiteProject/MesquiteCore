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
import mesquite.lib.duties.*;

/* ======================================================================== */
/** A miniature scroller containing two MiniScrolls. */
public class DoubleMiniScroll extends Panel  {
	MiniScroll xScroll, yScroll;
	String xTitle, yTitle;
	MesquiteLabel yTitlePanel;
	Color bg;
	public DoubleMiniScroll(MesquiteCommand setXCommand, MesquiteCommand setYCommand, int minX, int currentX, int maxX, int minY, int currentY, int maxY) {
		setLayout(null);
		setCursor(Cursor.getDefaultCursor());
		
		yScroll = new MiniScroll(setYCommand, false, false, minY, minY, maxY,"");
		yScroll.setLocation(0, 0);
		yScroll.setVisible(true);
		xScroll = new MiniScroll(setXCommand, false, true, minX, minX, maxX,"");
		xScroll.setLocation(20, 46);
		xScroll.setVisible(true);
		yTitlePanel = new MesquiteLabel(null);
		yTitlePanel.setColor(null);
		yTitlePanel.setLocation(16, 0);
		setSize(20+xScroll.getTotalWidth(), yScroll.getTotalHeight());
		add(yTitlePanel);
		add(xScroll);
		add(yScroll);
	}
	public void setVisible(boolean b){
		if (b)
			checkBackground();
		super.setVisible(b);
	}
	
	private boolean checkBackground(){
		if (getParent() !=null && getBackground()!=null && !getBackground().equals(getParent().getBackground())) {
			bg =getParent().getBackground();
			setBackground(bg);
			return true;
		}
		else return false;
	}
	public void setXVisible(boolean vis){
		xScroll.setVisible(vis);
	}
	public void setYVisible(boolean vis){
		yScroll.setVisible(vis);
		yTitlePanel.setVisible(vis);
	}
	public void setXValues(int min, int current, int max){
		xScroll.setMinimumValue(min);
		xScroll.setMaximumValue(max);
		xScroll.setCurrentValue(current);
	}
	public void setYValues(int min, int current, int max){
		yScroll.setMinimumValue(min);
		yScroll.setMaximumValue(max);
		yScroll.setCurrentValue(current);
	}
	public void setXTitle(String s){
		xTitle = s;
	}
	public void setYTitle(String s){
		yTitle = s;
		yTitlePanel.setText(yTitle);
	}
	public void paint(Graphics g){
		if (g instanceof PrintGraphics)
			return;
		if (xScroll.isVisible() && !StringUtil.blank(xTitle))
			g.drawString(xTitle, 42, 42);
	//	if (!StringUtil.blank(yTitle))
		//	g.drawString(yTitle, 36, 18);
	}
	public void printAll(Graphics g) { 
	}
	public void paintComponents(Graphics g) { 
		if (g instanceof PrintGraphics)
			return;
		else
			super.paintComponents(g);
	}
	public void printComponents(Graphics g) { 
	}
	public void print(Graphics g) { 
	}
}

