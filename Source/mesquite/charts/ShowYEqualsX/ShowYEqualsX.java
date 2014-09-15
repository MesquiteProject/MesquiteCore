/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charts.ShowYEqualsX;

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;



public class ShowYEqualsX extends ScattergramAssistantA  {
	/*.................................................................................................................*/
	public String getName(){
		return "Show Y = X";
	}
	public String getExplanation() {
		return "Shows the Y=X line on a scattergram";
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}

	public ChartExtra createExtra(MesquiteChart chart){
		return new ShowYEqualsXExtra(this, chart);
	}

	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}

}


class ShowYEqualsXExtra extends ChartExtra {
	double meanX, meanY;
	public ShowYEqualsXExtra(MesquiteModule ownerModule, MesquiteChart chart){
		super(ownerModule, chart);
	}
	/**Do any calculations needed*/
	public boolean doCalculations(){
		return false;
	}

	MesquiteNumber max = new MesquiteNumber();
	MesquiteNumber temp = new MesquiteNumber();

	public void drawOnChart(Graphics g){
		Charter c = chart.getCharter();
		max.setValue(0);
		temp.setValue(chart.getMinimumX());
		temp.abs();
		max.setMeIfIAmLessThan(temp);
		temp.setValue(chart.getMinimumY());
		temp.abs();
		max.setMeIfIAmLessThan(temp);
		temp.setValue(chart.getMaximumX());
		temp.abs();
		max.setMeIfIAmLessThan(temp);
		temp.setValue(chart.getMaximumY());
		temp.abs();
		max.setMeIfIAmLessThan(temp);
		Color col = g.getColor();
		g.setColor(Color.green);
		int xMin = c.xToPixel(-max.getDoubleValue(),chart);
		int yMin =  c.yToPixel(-max.getDoubleValue(),chart);
		int xMax = c.xToPixel(max.getDoubleValue(),chart);
		int yMax = c.yToPixel(max.getDoubleValue(),chart);
		g.drawLine(xMin,yMin ,xMax, yMax);
		g.drawLine(xMin+1,yMin+1 ,xMax+1, yMax+1);
		g.setColor(col);
	}
	/**print on the chart*/
	public void printOnChart(Graphics g){
		drawOnChart(g);
	}

	public String writeOnChart(){
		return null;
	}

	/**to inform ChartExtra that cursor has just entered point*/
	public void cursorEnterPoint(int point, int exactPoint, Graphics g){
	}
	/**to inform ChartExtra that cursor has just exited point*/
	public void cursorExitPoint(int point, int exactPoint, Graphics g){
	}
	/**to inform ChartExtra that cursor has just touched point*/
	public void cursorTouchPoint(int point, int exactPoint, Graphics g){
	}
}

