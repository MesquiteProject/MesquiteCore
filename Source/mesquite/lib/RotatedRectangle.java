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

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;


/* ======================================================================== */
/** This class draws and fills a rotated rectangle, using an internal polygon.  The rectangle is defined by two points
(x1, y1 and x2, y2), the line between which serve as one edge of the rectangle (its "spine"), and a width.
The width is either above or below the line (higher or lower y's) depending on the boolean "extendAbove".  The shape
parameter determines if it is a full rectangle or some truncated part thereof.*/
public class RotatedRectangle {
	public static final int RECTANGLE =0;
	public static final int FLATHORIZONTAL =1;
	public static final int FLATVERTICAL =2;
	/*
	public static final int HOUSEUP =3;
	public static final int HOUSEDOWN =4;
	public static final int HOUSERIGHT =5;
	public static final int HOUSELEFT =6;
	*/
	private int primaryX1, primaryY1,primaryX2, primaryY2;
	boolean extendAbove = true;
	Polygon poly;
	public RotatedRectangle(){
		poly = new Polygon();
		poly.xpoints = new int[8];
		poly.ypoints = new int[8];
	}
	public void setShape(double x1, double y1, double x2, double y2, int width, boolean extendAbove, int shape){  //DAVIDCHECK  [DRM added] temporary 
		setShape((int)x1, (int)y1, (int) x2, (int) y2, width, extendAbove, shape);
		
	}
		public void setShape(int x1, int y1, int x2, int y2, int width, boolean extendAbove, int shape){
		primaryX1 = x1;
		primaryY1 = y1;
		primaryX2 = x2;
		primaryY2 = y2;
		this.extendAbove = extendAbove;
		if (x1==x2) {
			poly.npoints=0;
			poly.addPoint(x1, y1);
			if (extendAbove) {
				poly.addPoint(x1-width, y1);
				poly.addPoint(x2-width, y2);
			}
			else {
				poly.addPoint(x1+width, y1);
				poly.addPoint(x2+width, y2);
			}
			poly.addPoint(x2, y2);
			poly.addPoint(x1, y1);
			poly.npoints=5;
		}
		else if (y1==y2){
			poly.npoints=0;
			poly.addPoint(x1, y1);
			if (extendAbove) {
				poly.addPoint(x1, y1-width);
				poly.addPoint(x2, y2-width);
			}
			else {
				poly.addPoint(x1, y1+width);
				poly.addPoint(x2, y2+width);
			}
			poly.addPoint(x2, y2);
			poly.addPoint(x1, y1);
			
			poly.npoints=5;
		}
		else {
			poly.npoints=0;
			double oldSlope = (y1-y2)*1.0/(x1-x2);
			double newSlope = 1.0/oldSlope;
			double ns2 = (1+newSlope*newSlope);
			double os2 = (1+oldSlope*oldSlope);
			int offsetX = (int) Math.sqrt((width*width)*1.0/ns2);
			int offsetY = (int) Math.sqrt((width*width)*1.0/os2);

			if (shape == FLATHORIZONTAL) {
				offsetX += (int)Math.abs((Math.sqrt((width*width)*1.0/os2))/oldSlope);
				offsetY = 0;
			}
			else if (shape == FLATVERTICAL){
				offsetY += (int)Math.abs(Math.sqrt((width*width)*1.0/ns2)*oldSlope);
				offsetX = 0;
			}
			else {
				offsetX = (int) Math.sqrt((width*width)*1.0/ns2);
				offsetY = (int) Math.sqrt((width*width)*1.0/os2);
			}
			poly.addPoint(x1, y1);
			if (extendAbove) {
				if ((x1>x2 && y1>y2)|| (x1<x2 && y1<y2)) {
					poly.addPoint(x1+ offsetX, y1-offsetY);
					poly.addPoint(x2+ offsetX, y2-offsetY);
				}
				else {
					poly.addPoint(x1- offsetX, y1-offsetY);
					poly.addPoint(x2- offsetX, y2-offsetY);
				}
			}
			else {
				if ((x1>x2 && y1<y2) || (x1<x2 && y1>y2)){
					poly.addPoint(x1+ offsetX, y1+offsetY);
					poly.addPoint(x2+ offsetX, y2+offsetY);
				}
				else {
					poly.addPoint(x1- offsetX, y1+offsetY);
					poly.addPoint(x2- offsetX, y2+offsetY);
				}
			}
			poly.addPoint(x2, y2);
			poly.addPoint(x1, y1);
			poly.npoints=5;
		}
	}
	public boolean contains(int x, int y){ 
		return poly.contains(x,y);		
	}
	public void draw(Graphics g){ 
		g.drawPolygon(poly);		
	}
	private void offsetLine(Graphics g, int  x1, int  y1, int  x2, int  y2, int offset){
		if (extendAbove) 
			g.drawLine(x1, y1-offset, x2, y2-offset); 
		else
			g.drawLine(x1, y1+offset, x2, y2+offset); 
	}
	
	public void fill(Graphics g, boolean threeD){ 
		if (threeD){
			Color c = g.getColor();
			Color light = ColorDistribution.brighter(c, 0.66);
			g.setColor(light);
			g.fillPolygon(poly);
			light = ColorDistribution.brighter(light, 0.66);
			g.setColor(light);
			offsetLine(g, primaryX1,primaryY1, primaryX2, primaryY2, 1); 
			offsetLine(g, primaryX1, primaryY1, primaryX2, primaryY2, 3); 
			offsetLine(g, primaryX1, primaryY1, primaryX2, primaryY2, 4); 
			g.setColor(Color.white);
			offsetLine(g, primaryX1, primaryY1, primaryX2, primaryY2, 2); 
			if (c!=null) g.setColor(c);
			g.drawPolygon(poly);
		}
		else
			g.fillPolygon(poly);		
	}
}


