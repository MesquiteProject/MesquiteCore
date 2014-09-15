/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charts.ShowMean;

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;



public class ShowMean extends HistogramAssistantA  {
	/*.................................................................................................................*/
	public String getName(){
		return "Display Mean";
		}
 	public String getExplanation() {
 		return "Calculates and shows the mean value." ;
   	 }
	/*.................................................................................................................*/
	Vector extras;
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
   		extras = new Vector();
		return true;
	}
	
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}

	public ChartExtra createExtra(MesquiteChart chart){
		ChartExtra chartExtra = new ShowMeanExtra(this, chart);
		extras.addElement(chartExtra);
		return chartExtra;
	}
	
	/*.................................................................................................................*/
	public String getNameForMenuItem(){
		return "Display Mean...";
		}
	/*.................................................................................................................*/
  	 public String getAuthors() {
		return "Wayne P. Maddison & David R. Maddison";
   	 }
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
  	 public boolean isPrerelease() {
		return false;
   	 }

	/*.................................................................................................................*/
 	public void endJob() {
		if (extras!=null) {
			Enumeration e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof ChartExtra) {
					ChartExtra tCO = (ChartExtra)obj;
		 			tCO.turnOff();
		 			
		 		}
			}
			extras.removeAllElements();
		}
 		super.endJob();
   	 }
}


class ShowMeanExtra extends ChartBkgdExtra {
	MesquiteNumber valueX = new MesquiteNumber();
	MesquiteNumber valueY = new MesquiteNumber();
	MesquiteNumber mean = new MesquiteNumber(); //mean, or if some selected, mean of selected
	MesquiteNumber totalMean = new MesquiteNumber(); //for total (if some selected)
	Polygon upTriangle,rightTriangle;
	Charter charter;
	int trisize = 5;
	
	public ShowMeanExtra(MesquiteModule ownerModule, MesquiteChart chart){
		super(ownerModule, chart);
		upTriangle=new Polygon();
		upTriangle.xpoints = new int[4];
		upTriangle.ypoints = new int[4];
		upTriangle.npoints=0;
		upTriangle.addPoint(0, trisize);
		upTriangle.addPoint(trisize, 0);
		upTriangle.addPoint(trisize+trisize, trisize);
		upTriangle.addPoint(0, trisize);
		upTriangle.npoints=4;
		rightTriangle=new Polygon();
		rightTriangle.xpoints = new int[4];
		rightTriangle.ypoints = new int[4];
		rightTriangle.npoints=0;
		rightTriangle.addPoint(0, 0);
		rightTriangle.addPoint(trisize, trisize);
		rightTriangle.addPoint(0, trisize+trisize);
		rightTriangle.addPoint(0, 0);
		rightTriangle.npoints=4;
		charter = chart.getCharter();
	}
	/**Do any calculations needed*/
	public boolean doCalculations(){
		if (chart == null)
			return false;
		mean.setValue(0);
		totalMean.setValue(0);
		int n=0;
		int nTotal = 0;
		boolean someSelected = chart.getSelected().anyBitsOn();
		if (chart.getOrientation() == 0) {  //orientation stored in chart as 0 = items by values; 1 = values by items
			for (int i= 0; i<chart.getNumPoints(); i++) {
				if (chart.getXArray().isCombinable(i) && chart.getYArray().isCombinable(i)) {
					chart.getXArray().placeValue(i, valueX);
					chart.getYArray().placeValue(i, valueY);
					valueX.multiplyBy(valueY);
					if (someSelected){
						if (chart.getSelected().isBitOn(i)) {
							mean.add(valueX);
							n++;
						}
						totalMean.add(valueX);
						nTotal++;
					}
					else {
						mean.add(valueX);
						n++;
					}
				}
			}
		}
		else {
			for (int i= 0; i<chart.getNumPoints(); i++) {
				if (chart.getXArray().isCombinable(i) && chart.getYArray().isCombinable(i)) {
					chart.getYArray().placeValue(i, valueY);
					if (someSelected){
						if (chart.getSelected().isBitOn(i)) {
							mean.add(valueY);
							n++;
						}
						totalMean.add(valueY);
						nTotal++;
					}
					else {
						mean.add(valueY);
						n++;
					}
				}
			}
		}
		if (n==0)
			mean.setToUnassigned();
		else
			mean.divideBy(n);
		if (nTotal==0)
			totalMean.setToUnassigned();
		else
			totalMean.divideBy(nTotal);
		return false;
	}
	
	Color mainColor = Color.green;
	Color secondColor = ColorDistribution.lightGreen;
	/**draw on the chart*/
	public void drawOnChart(Graphics g){
		if (!mean.isCombinable())
			return;
		Charter c = chart.getCharter();
		Color col = g.getColor();
		g.setColor(mainColor);

		if (chart.getOrientation() == 0) {  //orientation stored in chart as 0 = items by values; 1 = values by items
			//drawing vertical line
			int x = c.xToPixel(mean.getDoubleValue(),chart);
			g.drawLine(x, 0,x, chart.getFieldHeight());
			g.drawLine(x+1, 0,x+1, chart.getFieldHeight());
		}
		else {  
			//drawing horizontal line
			int y = c.yToPixel(mean.getDoubleValue(),chart);
			g.drawLine(0,y, chart.getFieldWidth(), y);
			g.drawLine(0,y+1, chart.getFieldWidth(), y+1);
		}
		if (totalMean.isCombinable()){
			g.setColor(secondColor);
			if (chart.getOrientation() == 0) {  //orientation stored in chart as 0 = items by values; 1 = values by items
				int x = c.xToPixel(totalMean.getDoubleValue(),chart);
				g.drawLine(x, 0,x, chart.getFieldHeight());
				g.drawLine(x+1, 0,x+1, chart.getFieldHeight());
			}
			else {  
				int y = c.yToPixel(totalMean.getDoubleValue(),chart);
				g.drawLine(0,y, chart.getFieldWidth(), y);
				g.drawLine(0,y+1, chart.getFieldWidth(), y+1);
			}
		}
		g.setColor(col);
	}
	public void drawOnAxes(Graphics g, MesquiteChart chart){
		if (!mean.isCombinable())
			return;
		Color col = g.getColor();
		g.setColor(mainColor);
		if (chart.getOrientation() == 0) {  //orientation stored in chart as 0 = items by values; 1 = values by items
			int xPixel = xPixelOnAxis(mean.getDoubleValue(), chart)-trisize;
			int yPixel = chart.getFieldHeight();
			upTriangle.translate(xPixel,yPixel);
			g.fillPolygon(upTriangle);
			//g.setColor(Color.black);
			//g.drawPolygon(upTriangle);
			upTriangle.translate(-xPixel,-yPixel);
		}
		else {  
			int xPixel = chart.getXAxisEdge()-trisize;
			int yPixel = yPixelOnAxis(mean.getDoubleValue(), chart)-trisize;
			rightTriangle.translate(xPixel,yPixel);
			g.fillPolygon(rightTriangle);
			//g.setColor(Color.black);
			//g.drawPolygon(rightTriangle);
			rightTriangle.translate(-xPixel,-yPixel);
		}
		if (totalMean.isCombinable()){
			g.setColor(secondColor);
			if (chart.getOrientation() == 0) {  //orientation stored in chart as 0 = items by values; 1 = values by items
				int xPixel = xPixelOnAxis(totalMean.getDoubleValue(), chart)-trisize;
				int yPixel = chart.getFieldHeight();
				upTriangle.translate(xPixel,yPixel);
				g.fillPolygon(upTriangle);
				//g.setColor(Color.black);
				//g.drawPolygon(upTriangle);
				upTriangle.translate(-xPixel,-yPixel);
			}
			else {  
				int xPixel = chart.getXAxisEdge()-trisize;
				int yPixel = yPixelOnAxis(totalMean.getDoubleValue(), chart)-trisize;
				rightTriangle.translate(xPixel,yPixel);
				g.fillPolygon(rightTriangle);
				//g.setColor(Color.black);
				//g.drawPolygon(rightTriangle);
				rightTriangle.translate(-xPixel,-yPixel);
			}
		}
		g.setColor(col);
		
	}

	/**print on the chart*/
	public void printOnChart(Graphics g){
		drawOnChart(g);
	}
	
	public String writeOnChart(){
		if (!mean.isCombinable())
			return "";
		String s;
		if (totalMean.isCombinable())
			s = "Mean value of selection: ";
		else
			s = "Mean value: ";
		s+= mean.toString();
		if (totalMean.isCombinable())
			s += "\nMean value of all items: " + totalMean.toString();
		return s;
	}
	
	/**to inform ChartExtra that cursor has just entered point*/
	public void cursorEnterPoint(int point, int exactPoint, Graphics g){
		if (chart == null || charter == null)
			return;
		MesquiteNumber mn;
		if (chart.getOrientation() == 0) {  //orientation stored in chart as 0 = items by values; 1 = values by items
			mn = new MesquiteNumber();
			charter.pixelToX(exactPoint,chart, mn);
		}
		else {  
			mn = new MesquiteNumber();
			charter.pixelToY(exactPoint,chart, mn);
		}
	}
	/**to inform ChartExtra that cursor has just exited point*/
	public void cursorExitPoint(int point, int exactPoint, Graphics g){
	}
	/**to inform ChartExtra that cursor has just touched point*/
	public void cursorTouchPoint(int point, int exactPoint, Graphics g){
	}
}

