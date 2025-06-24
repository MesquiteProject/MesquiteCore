/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.ui;

import java.util.Vector;

/* ======================================================================== */
public class ColorEventVector extends Vector {
	
	public ColorEventVector(int num){
		super(num);
	}
	public ColorEventVector(){
		super();
	}
	public void sort(){
		for (int i=1; i<size(); i++) {
			boolean done = false;
			for (int j= i-1; j>=0 && !done; j--) {
				ColorEvent ej = (ColorEvent)elementAt(j);
				ColorEvent ej1 = (ColorEvent)elementAt(j+1);
				if (ej.getPosition()<=ej1.getPosition())
					done = true;
				else 
					ej.tradeValues(ej1);
			}
		}
	}
}

