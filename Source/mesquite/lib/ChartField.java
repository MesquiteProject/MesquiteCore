/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
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


/* ======================================================================== */
public class ChartField extends MesquitePanel {

	private MesquiteChart chart;
	private MesquiteNumber valueX, valueY;
	private Menu chartTypeSubmenu;
	boolean suppressBlank = false;
	public ChartField( MesquiteChart chart){
		this.chart = chart;
		valueX = new MesquiteNumber();
		valueY = new MesquiteNumber();
		setLayout(null);
		//setBackground(Color.green);
	}
	public void print(Graphics g) {
	   	if (MesquiteWindow.checkDoomed(this)) {
	   		return;
	   	}
		g.setColor(Color.black);
		if (chart.getCharter()!=null)
			chart.getCharter().drawChartBackground(g, chart);
		chart.drawAllBackgroundExtras(g);
		if (chart.getCharter()!=null)
			chart.getCharter().drawChart(g, chart);
		chart.drawAllExtras(g);
		chart.drawAxes(true);
		MesquiteWindow.uncheckDoomed(this);
	}
	public void paint(Graphics g) {
	   	if (MesquiteWindow.checkDoomed(this)) {
	   		return;
	   	}
		if (chart.getCharter() != null){
			g.setColor(Color.black);
			chart.getCharter().drawBlank( g, chart);
			chart.getCharter().drawChartBackground(g, chart);
			chart.drawAllBackgroundExtras(g);
			chart.getCharter().drawChart(g, chart);
			chart.drawAllExtras(g);
			chart.drawAxes(false);
		}
		MesquiteWindow.uncheckDoomed(this);

	}
	public void drawBlank() {   
		if (suppressBlank)
			return;
		
		Graphics g = this.getGraphics();
		if (g!=null) {
			int fieldWidth= getBounds().width; //chart.getChartWidth()-chart.getXAxisEdge();
			int fieldHeight= getBounds().height; //chart.getChartHeight()-chart.getYAxisEdge();
			g.setColor(Color.lightGray);
			g.fillRoundRect(0,0,fieldWidth,fieldHeight, 5, 5);
			g.setColor(Color.black);
			g.drawRoundRect(0,0,fieldWidth,fieldHeight, 5, 5);
			g.setColor(Color.white);
			g.drawRoundRect(1,1,fieldWidth-2,fieldHeight-2, 5, 5);
			g.setColor(Color.gray);
			g.drawRoundRect(1,1,fieldWidth-1,fieldHeight-1, 5, 5);
			g.setColor(Color.black);
			int margin = chart.getMargin();
			if (chart.getCharter()!=null)
				chart.getCharter().drawBlank(g, chart);
			g.dispose();
		}
	}
	/*_________________________________________________*/
	public void mouseMoved(int modifiers, int x, int y, MesquiteTool tool) {
		if (!(tool instanceof ChartTool))
			tool = null;
		if (chart.getCharter()!=null)
			chart.getCharter().mouseMoveInField(modifiers,x,y, chart, (ChartTool)tool);
	}
	/*_________________________________________________*/
	   public void mouseDown(int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		if (!(tool instanceof ChartTool))
			tool = null;
		if (chart.getCharter()!=null)
			chart.getCharter().mouseDownInField(modifiers,x,y, chart, (ChartTool)tool);
	   }

	/*_________________________________________________*/
	public void mouseDrag(int modifiers, int x, int y, MesquiteTool tool) {
		if (!(tool instanceof ChartTool))
			tool = null;
		if (chart.getCharter()!=null)
			chart.getCharter().mouseDragInField(modifiers,x,y, chart, (ChartTool)tool);
	}
	/*_________________________________________________*/
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		if (!(tool instanceof ChartTool))
			tool = null;
		if (chart.getCharter()!=null)
			chart.getCharter().mouseUpInField(modifiers,x,y, chart, (ChartTool)tool);
	}
}


