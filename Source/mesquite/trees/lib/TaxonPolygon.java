/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.lib;

import java.awt.Polygon;
import java.awt.Rectangle;

public class TaxonPolygon  extends Polygon {
	Rectangle b;

	public void setB(int x, int y, int w, int h){
		if (b == null)
			b = new Rectangle(x, y, w, h);
		else {
			b.x = x;
			b.y = y;
			b.width = w;
			b.height = h;
		}
	}
	
	public boolean isHidden(){
		if (b == null)
			return true;
		return b.width == 0 || b.height == 0;
	}
	public Rectangle getB(){
		return b;
	}
}