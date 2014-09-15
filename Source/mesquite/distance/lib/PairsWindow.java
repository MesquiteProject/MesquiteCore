/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.distance.lib;

import java.awt.*;
import java.util.*;
import mesquite.lib.duties.*;
import mesquite.lib.*;


/* ======================================================================== */
public class PairsWindow extends ChartWindow {
	private Number2DArray pairValues;
	public String averageString = "";
	public String explanationString = "";
	private int windowWidth=0;
	private int windowHeight=0;
	private int chartInsetTop = 10;
	private int chartInsetBottom = 60;
	private int chartInsetLeft = 0;
	private int chartInsetRight = 20;
	MesquiteModule ownerModule;
	MesquiteChart chart;
	MesquiteNumber utilNum;
	PairsChartMessages messagePane;
	Taxa taxa;
	MesquiteInteger numItems;
	DrawChart charterTask;
	String itemTypeName;
	public PairsWindow (MesquiteModule ownerModule, Number2DArray pairValues, DrawChart charterTask, String itemTypeName, MesquiteInteger numItems) {
		super(ownerModule,  true); //infobar
		this.numItems = numItems;
		this.itemTypeName = itemTypeName;
		this.charterTask =  charterTask;
		this.ownerModule=ownerModule;
		//this.taxa = taxa;
		this.pairValues = pairValues;
		chart=new MesquiteChart(ownerModule, numItems.getValue()*numItems.getValue(), 0, charterTask.createCharter(null));
 		setChart(chart);
		
		chart.deassignChart();
		chart.setYAxisName(itemTypeName);
		chart.setXAxisName(itemTypeName);
		utilNum = new MesquiteNumber(0);
		chart.constrainMinimumY(utilNum);
		chart.setLocation(chartInsetLeft, chartInsetTop);
		chart.setChartSize(getWidth()-chartInsetRight - chartInsetLeft, getHeight()-chartInsetTop - chartInsetBottom);
		messagePane = new PairsChartMessages(this);
		addToWindow(messagePane);
		messagePane.setVisible(true);
		setChartVisible();
		resetTitle();
	}
	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree lists, etc.)
	they can reset their titles properly*/
	public void resetTitle(){
		setTitle("Value for Pairs of " + itemTypeName); 
	}
	public void setVisible(boolean vis){
		if (vis && chart !=null) {
			chart.setVisible(true);
			chart.repaint();
		}
		super.setVisible(vis);
	}
	/*.................................................................................................................*/
	public void setCharter(Charter charter) {
		chart.setCharter(charter);
	}
	public void setAverageString(String t) {
		averageString=t;
	}
	public void setExplanationString(String t) {
		explanationString=t;
	}
	public void setWindowSize(int width, int height){
		super.setWindowSize(width, height);
		checkSizes();
	}

	public void recalcChart() {
		chart.deassignChart();
		MesquiteNumber iValue = new MesquiteNumber();
		MesquiteNumber jValue = new MesquiteNumber();
		MesquiteNumber resultZ = new MesquiteNumber();

		for (int i=0; i<numItems.getValue(); i++)  //TODO: update if taxa included/excluded
			for (int j=0; j<numItems.getValue(); j++) {
				pairValues.placeValue(i, j, resultZ);
				iValue.setValue(i);
				jValue.setValue(j);
				chart.addPoint(iValue, jValue, resultZ);
			}
		
		chart.munch();
		messagePane.repaint();
	}
	void checkSizes(){
   	 	if (chart==null)
   	 		return;
   	 	if ((getHeight()!=windowHeight) || (getWidth()!=windowWidth) || (chart.getChartHeight()!=windowHeight-chartInsetTop - chartInsetBottom) || (chart.getChartWidth()!=windowWidth-chartInsetRight - chartInsetLeft)) {
   	 		windowHeight =getHeight();
   	 		windowWidth = getWidth();
			chart.setLocation(chartInsetLeft, chartInsetTop);
			chart.setChartSize(windowWidth-chartInsetRight - chartInsetLeft, windowHeight-chartInsetTop - chartInsetBottom);
			messagePane.setLocation(0, windowHeight- chartInsetBottom);
			messagePane.setSize(windowWidth, chartInsetBottom);
		}
	}
	
	public void paint(Graphics g) {
	 	checkSizes();
	}
}

class PairsChartMessages extends MesquitePanel {
	PairsWindow window;
	public PairsChartMessages (PairsWindow window) {
		super();
		this.window = window;
		//setBackground(window.ownerModule.getProject().getProjectColor());
		//setBackground(Color.yellow);
	}
	public void paint(Graphics g) {
	 	g.setColor(Color.black);
		g.drawString(window.averageString, 20, 25);
		g.drawString(window.explanationString, 20, 45);
	}
}



