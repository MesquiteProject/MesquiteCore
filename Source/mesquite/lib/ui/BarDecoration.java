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

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;



/* ======================================================================== */
/** For bars on tree branches, e.g. TraceAllChanges
 */
public class BarDecoration {
	public int node;
	public Color lineColor;
	public Color fillColor;
	public String text;
	public Font font;
	public Color fontColor;
	public Rectangle r;
	public BarDecoration(int node, Color lineColor, Color fillColor, String text, Font font, Color fontColor){
		this.fillColor = fillColor;
		this.lineColor = lineColor;
		this.text=text;
		this.node = node;
		this.font = font;
		this.fontColor = fontColor;
	}
	
	public void setRectangle(Rectangle r){
		this.r = r;
	}
	public boolean contains(int x, int y){
		return r.contains(x, y);
	}

}