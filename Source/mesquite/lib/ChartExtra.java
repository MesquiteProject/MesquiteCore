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
import java.util.*;


/* ======================================================================== */
/** A class used for additional graphical and calculations elements to be drawn and calculated within 
charts -- legends, regression lines, etc. The ChartTextra is notified when the cursor 
is moved over a point and so on, and when the chart is drawn the extra is notified via drawOnChart so that
it can add its items to the graphic.*/
public abstract class ChartExtra implements Listable, OwnedByModule {
	protected MesquiteChart chart;
	protected MesquiteModule ownerModule;
	public static long totalCreated = 0;
	private Vector panels = new Vector();
	public ChartExtra (MesquiteModule ownerModule, MesquiteChart chart) {
		this.chart = chart;
		this.ownerModule=ownerModule;
		totalCreated++;
	}
	
	public MesquiteModule getOwnerModule(){
		return ownerModule;
	}
	
	public String getName(){
		if (ownerModule !=null)
			return ownerModule.getName();
		else
			return getClass().getName();
	}
	
	public MesquiteChart getChart(){
		return chart;
	}
	public void dispose(){
		ownerModule =null;
		chart=null;
	}
	/**Do any calculations needed.  If chart needs to be re-munched, pass back true*/
	public abstract boolean doCalculations();
	/**draw on the chart*/
	public abstract void drawOnChart(Graphics g);
	/**print on the chart*/
	public abstract void printOnChart(Graphics g);
	/**draw on the axis*/
	public void drawOnAxes(Graphics g, MesquiteChart chart){
	}
	
	protected int xPixelOnAxis(double x, MesquiteChart chart){
		if (chart != null && chart.getCharter() != null)
			return chart.getCharter().xToPixel(x,chart) + chart.getXAxisEdge() - chart.scrollXOffset();
		return -1;
	}
	protected int yPixelOnAxis(double y, MesquiteChart chart){
		if (chart != null && chart.getCharter() != null)
			return chart.getCharter().yToPixel(y,chart) - chart.scrollXOffset();
		return -1;
	}
	
	/**return true if any notes at point in chart*/
	private boolean anyText(int point){
		return false;
	}
	/**return a text version of information on chart*/
	public String writeOnChart(){
		return "";
			
	}
	/**return a text version of information at node*/
	public String textAtPoint(int node){
		return "";
	}
	/**return a text version of any legends or other explanatory information*/
	public String textForLegend(){
		return "";
	}

	/**to inform ChartExtra that cursor has just entered point*/
	public void cursorEnterPoint(int point, int exactPoint, Graphics g){}
	/**to inform ChartExtra that cursor has just exited point*/
	public void cursorExitPoint(int point, int exactPoint, Graphics g){}
	/**to inform ChartExtra that cursor has just touched point*/
	public void cursorTouchPoint(int point, int exactPoint, Graphics g){}
	
	public void addPanelPlease(Panel p){
		panels.addElement(p);
		chart.addPanelPlease(p);
	}
	public void removePanelPlease(Panel p){
		panels.addElement(p);
		chart.removePanelPlease(p);
	}
	protected Vector getPanels(){
		return panels;
	}

	public void turnOff() {
		if (chart==null)
			return;
		chart.removeExtra(this);
		chart.repaint();
	}
	
}




