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

import java.awt.Graphics2D;
import java.awt.geom.Area;


/* ======================================================================== */
public class MesquitePath2DFloat {
	Object pFloat;
	public MesquitePath2DFloat(){
		try {
			Class.forName("java.awt.geom.Path2D");
		pFloat = new java.awt.geom.Path2D.Float();
		}
		catch (Error e){
		}
		catch (Exception e){
		}
	}
	
	
	public void closePath(){
		((java.awt.geom.Path2D.Float)pFloat).closePath();
	}
	             

	public void moveTo(int x, int y){
		((java.awt.geom.Path2D.Float)pFloat).moveTo(x, y);
	}
	public void lineTo(int x, int y){
		((java.awt.geom.Path2D.Float)pFloat).lineTo(x, y);
	}

	public void curveTo(double d1, double d2, double d3, double d4, double d5, double d6){
		((java.awt.geom.Path2D.Float)pFloat).curveTo(d1, d2, d3, d4,d5,d6);
	}
	public void fill(Graphics2D g){
		g.fill(((java.awt.geom.Path2D.Float)pFloat));
	}
	public Area getArea(){
		return new Area(((java.awt.geom.Path2D.Float)pFloat));
	}
	public boolean OK(){
		return pFloat != null;
	}
}


