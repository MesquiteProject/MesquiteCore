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
public class LogValueToPixel extends ValueToPixel {
	public LogValueToPixel  (double minValue, double maxValue, double minSweetSpotValue, double maxSweetSpotValue, int totalPixels) {
		super(minValue, maxValue, minSweetSpotValue,maxSweetSpotValue,totalPixels);
	}
	public double getValue(int pixel){
			//0 = 0;  maxValue = 0.5; infinity = 1;   
			
			if (pixel*1.0/totalPixels >0.7) //in right half of slider
				return maxSweetSpotValue - 5* Math.log((1.0 - pixel*1.0/totalPixels)/0.3);
			else
				return Math.log(pixel*1.0/totalPixels/0.7) + maxSweetSpotValue;
	}
	public double getPixelPercent(double value){
		/* if above maxSweetSpotValue then   1- 0.3*exp(maxSweetSpotValue-x)
			if below maxSweetSpotValue then 0.7*exp(x-maxSweetSpotValue) */
		if (value>maxSweetSpotValue)
			return 1.0 - 0.3*Math.exp((maxSweetSpotValue-value)/5);
		else  
			return 0.7*Math.exp(value-maxSweetSpotValue);
	}
}



