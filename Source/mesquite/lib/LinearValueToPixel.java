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
public class LinearValueToPixel extends ValueToPixel {
	public LinearValueToPixel (double minValue, double maxValue, double minSweetSpotValue, double maxSweetSpotValue, int totalPixels) {
		super(minValue, maxValue, minSweetSpotValue,maxSweetSpotValue,totalPixels);
	}
	public double getValue(int pixel){
		if (pixel< startSweetPixels)
			if (minValue==MesquiteDouble.negInfinite)  // it's logarithmic below
				if (pixel==0)
					return effectiveMinValue;
				else {
					double preSweetPixelFraction =1.0*(startSweetPixels-pixel)/startSweetPixels;
					double exp0to1 =  (Math.exp(preSweetPixelFraction) -1)/(Math.exp(1)-1);
					double value = minSweetSpotValue - exp0to1 * (minSweetSpotValue-effectiveMinValue); 
					return value; 
				}
			else 
				return minValue + (minSweetSpotValue-minValue)*((1.0* pixel)/(startSweetPixels));
		else if (pixel> endSweetPixels)
			if (maxValue==MesquiteDouble.infinite)  // it's logarithmic above
				if (pixel==totalPixels)
					return effectiveMaxValue;
				else {
					
					double postSweetPixelFraction =1.0*(pixel-endSweetPixels)/getPostSweetPixels();
					double exp0to1 =  (Math.exp(postSweetPixelFraction) -1)/(Math.exp(1)-1);
					double value = maxSweetSpotValue + exp0to1 * (effectiveMaxValue-maxSweetSpotValue); 
					return value; 
				}
			else 
				return maxSweetSpotValue + (maxValue-maxSweetSpotValue)*((1.0* (pixel-endSweetPixels))/(totalPixels-endSweetPixels));
		else
			return minSweetSpotValue + (maxSweetSpotValue-minSweetSpotValue)*((1.0* (pixel-startSweetPixels))/(totalSweetPixels));
	}
	public double getPixelPercent(double value){
		if (MesquiteDouble.isUnassigned(value))
			return startSweetPixelFraction;
		else if (value>=minSweetSpotValue && value <= maxSweetSpotValue) 
			return  startSweetPixelFraction + ((value-minSweetSpotValue)/(maxSweetSpotValue-minSweetSpotValue)) * sweetPixelFraction;
		else if (value<minSweetSpotValue) 
			if (minValue==MesquiteDouble.negInfinite) // it is logarithmic below
				if (value<=effectiveMinValue || value == MesquiteDouble.negInfinite)
					return 0.0;
				else { // it is logarithmic below; just do the reverse of what's above
					double exp0to1 = (minSweetSpotValue-value)/(minSweetSpotValue-effectiveMinValue);  
					double preSweetPixelFraction = Math.log((exp0to1 * (Math.exp(1)-1)) +1);
					if (preSweetPixelFraction>1.0)
						preSweetPixelFraction=1.0;
					if (preSweetPixelFraction<0.0)
						preSweetPixelFraction=0.0;
					double pixelPercent =  (1.0-preSweetPixelFraction)*startSweetPixelFraction;   
					return pixelPercent; 
				}
			else  // it is linear below
				return  ((value-minValue)/(minSweetSpotValue-minValue)) * startSweetPixelFraction;
		else 
			if (value>=effectiveMaxValue || value == MesquiteDouble.infinite)
				return 1.0;
			else if (maxValue==MesquiteDouble.infinite) {  // it is logarithmic above; just do the reverse of what's above
				double exp0to1 = (value-maxSweetSpotValue)/(effectiveMaxValue-maxSweetSpotValue);  
				double postSweetPixelFraction = Math.log((exp0to1 * (Math.exp(1)-1)) +1);
				if (postSweetPixelFraction>1.0)
					postSweetPixelFraction=1.0;
				if (postSweetPixelFraction<0.0)
					postSweetPixelFraction=0.0;
				double pixelPercent =  startSweetPixelFraction+sweetPixelFraction + postSweetPixelFraction*endSweetPixelFraction;   
				return pixelPercent; 
			}
			else  //  it is linear above
				return  startSweetPixelFraction+sweetPixelFraction+((value-maxSweetSpotValue)/(maxValue-maxSweetSpotValue))*endSweetPixelFraction;
	}
}



