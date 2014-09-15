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

import java.awt.event.*;
import java.awt.*;
import mesquite.lib.duties.*;

/*=================*/
public abstract class ValueToPixel extends Object {
	double minSweetSpotValue, maxSweetSpotValue;
	double minValue, maxValue;
	double effectiveMaxValue, effectiveMinValue;
	int totalSweetPixels;
	int startSweetPixels;
	int endSweetPixels;
	double startSweetPixelFraction, endSweetPixelFraction, sweetPixelFraction;
	double nonSweetPixelFraction=0.25;
	int totalPixels;
	
	public ValueToPixel (double minValue, double maxValue, double minSweetSpotValue, double maxSweetSpotValue, int totalPixels) {
		this.minSweetSpotValue = minSweetSpotValue;
		this.maxSweetSpotValue = maxSweetSpotValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.totalPixels = totalPixels;
		calcSweetSpot();
	}
	public void calcSweetSpot(){
		sweetPixelFraction = 1.0;
		startSweetPixels = 0;
		endSweetPixels = totalPixels;
		totalSweetPixels = totalPixels;
		startSweetPixelFraction = 0.0;
		endSweetPixelFraction = 0.0;
		int nonSweetPixels = (int)(totalPixels*nonSweetPixelFraction);
		effectiveMinValue = minValue;
		effectiveMaxValue = maxValue;
		if (minValue!=minSweetSpotValue) {
			sweetPixelFraction-= nonSweetPixelFraction;
			startSweetPixels = nonSweetPixels;
			totalSweetPixels-= nonSweetPixels;
			startSweetPixelFraction = nonSweetPixelFraction;
		}
		if (maxValue!=maxSweetSpotValue) {
			sweetPixelFraction-= nonSweetPixelFraction;
			endSweetPixels = totalPixels-nonSweetPixels;
			totalSweetPixels-= nonSweetPixels;
			endSweetPixelFraction = nonSweetPixelFraction;
		}
		effectiveMinValue = minValue;
		if (minValue==MesquiteDouble.negInfinite) {
			effectiveMinValue=-10000.0;
			if (effectiveMinValue > minSweetSpotValue*10)
				effectiveMinValue=minSweetSpotValue*10;
		}
		effectiveMaxValue = maxValue;
		if (maxValue==MesquiteDouble.infinite) {
			effectiveMaxValue=10000.0;
			if (effectiveMaxValue < maxSweetSpotValue*10)
				effectiveMaxValue=maxSweetSpotValue*10;
		}
	}
	public boolean hasStartingNonSweet(){
		return minValue!=minSweetSpotValue;
	}
	public boolean hasEndingNonSweet(){
		return maxValue!=maxSweetSpotValue;
	}
	public int getStartSweetPixels(){
		return startSweetPixels;
	}
	public int getEndSweetPixels(){
		return endSweetPixels;
	}
	public int getPostSweetPixels(){
		return totalPixels-endSweetPixels;
	}
	public void setMinSweetValue(double minSweetSpotValue){
		this.minSweetSpotValue = minSweetSpotValue;
		calcSweetSpot();
	}
	public void setMaxSweetValue(double maxSweetSpotValue){
		this.maxSweetSpotValue = maxSweetSpotValue;
		calcSweetSpot();
	}
	public void setMinValue(double minValue){
		this.minValue = minValue;
		calcSweetSpot();
	}
	public void setMaxValue(double maxValue){
		this.maxValue = maxValue;
		calcSweetSpot();
	}
	public void setTotalPixels(int totalPixels){
		this.totalPixels = totalPixels-1;  // subtract 1 because of zero-based
		calcSweetSpot();
	}
	public abstract double getValue(int pixel);
	public abstract double getPixelPercent(double value);
}



