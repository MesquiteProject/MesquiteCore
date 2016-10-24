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
import java.awt.geom.*;
import java.awt.image.*;



/* ======================================================================== */
/** This class provides general graphics utilities */
public class GraphicsUtil {
	public static TexturePaint missingDataTexture = null;
	/*_________________________________________________*/
	public static void drawString(Graphics g, String s, double x, double y) {
		if (!(g instanceof Graphics2D))
			return;
		Graphics2D g2 = (Graphics2D)g;
		g2.drawString(s, (float)x, (float)y);
	}
	/*_________________________________________________*/
	public static void drawString(Graphics2D g2, String s, double x, double y) {
		g2.drawString(s, (float)x, (float)y);
	}
	/*_________________________________________________*/
	public static void drawLine(Graphics2D g2, double fromX, double fromY, double toX, double toY) {
		Line2D line = new Line2D.Double(fromX, fromY, toX, toY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.draw(line);
	}
	/*_________________________________________________*/
	public static void drawLine(Graphics g, double fromX, double fromY, double toX, double toY) {
		if (!(g instanceof Graphics2D))
			return;
		Graphics2D g2 = (Graphics2D)g;
		drawLine(g2,fromX, fromY, toX, toY);
	}
	/*_________________________________________________*/
	public static void fillRect(Graphics2D g2, double x, double y, double width, double height) {
		Rectangle2D rect = new Rectangle2D.Double(x,y,width,height);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.fill(rect);
	}
	/*_________________________________________________*/
	public static void fillRect(Graphics g, double x, double y, double width, double height) {
		if (!(g instanceof Graphics2D))
			return;
		Graphics2D g2 = (Graphics2D)g;
		fillRect(g2,x,y,width,height);
	}
	/*_________________________________________________*/
	public static void drawRect(Graphics2D g2, double x, double y, double width, double height) {
		Rectangle2D rect = new Rectangle2D.Double(x,y,width,height);
		g2.draw(rect);
	}
	/*_________________________________________________*/
	public static void drawRect(Graphics g, double x, double y, double width, double height) {
		if (!(g instanceof Graphics2D))
			return;
		Graphics2D g2 = (Graphics2D)g;
		drawRect(g2,x,y,width,height);
	}
	/*_________________________________________________*/
	public static void fillOval(Graphics2D g2, double x, double y, double width, double height) {
		Ellipse2D oval = new Ellipse2D.Double(x,y,width,height);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.fill(oval);
	}
	/*_________________________________________________*/
	public static void fillOval(Graphics g, double x, double y, double width, double height) {
		if (!(g instanceof Graphics2D))
			return;
		Graphics2D g2 = (Graphics2D)g;
		fillOval(g2,x,y,width,height);
	}
	/*_________________________________________________*/
	public static void fillArc(Graphics2D g2, double x, double y, double width, double height, double startingAngle, double angleExtent) {
		Arc2D arc = new Arc2D.Double(x,y,width,height,startingAngle,  angleExtent, Arc2D.OPEN);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.fill(arc);
	}
	/*_________________________________________________*/
	public static void fillArc(Graphics g, double x, double y, double width, double height, double startingAngle, double angleExtent) {
		if (!(g instanceof Graphics2D))
			return;
		Graphics2D g2 = (Graphics2D)g;
		fillArc(g2,x,y,width,height,startingAngle, angleExtent);
	}
	/*_________________________________________________*/
	public static void drawArc(Graphics2D g2, double x, double y, double width, double height, double startingAngle, double angleExtent) {
		Arc2D arc = new Arc2D.Double(x,y,width,height,startingAngle,  angleExtent, Arc2D.OPEN);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.draw(arc);
	}
	/*_________________________________________________*/
	public static void drawArc(Graphics g, double x, double y, double width, double height, double startingAngle, double angleExtent) {
		if (!(g instanceof Graphics2D))
			return;
		Graphics2D g2 = (Graphics2D)g;
		drawArc(g2,x,y,width,height,startingAngle, angleExtent);
	}
	/*_________________________________________________*/
	public static void fill(Graphics g, Shape shape) {
		if (!(g instanceof Graphics2D))
			return;
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.fill(shape);
	}
	/*_________________________________________________*/
	public static void draw(Graphics g, Shape shape) {
		if (!(g instanceof Graphics2D))
			return;
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.draw(shape);
	}

	/*_________________________________________________*/
	public static void drawArrow(Graphics2D g2, int fromX, int fromY, int toX, int toY, int thickness) {
		// based on Vincent Reig's stackoverflow answer http://stackoverflow.com/a/3094933
		// create an AffineTransform 
		// and a triangle centered on (0,0) and pointing downward
		// somewhere outside Swing's paint loop
		Stroke stroke = g2.getStroke();
		g2.setStroke(new BasicStroke(thickness));
		g2.drawLine(fromX, fromY, toX, toY);
		g2.setStroke(stroke);
		AffineTransform tx = new AffineTransform();
		Line2D.Double line = new Line2D.Double(fromX, fromY, toX, toY);

		Polygon arrowHead = new Polygon();  
		int size = thickness*3;
		arrowHead.addPoint( 0,size);
		arrowHead.addPoint( -size, -size);
		arrowHead.addPoint( size,-size);

		// [...]
		tx.setToIdentity();
		double angle = Math.atan2(line.y2-line.y1, line.x2-line.x1);
		tx.translate(line.x2, line.y2);
		tx.rotate((angle-Math.PI/2d));  

		Graphics2D g = (Graphics2D) g2.create();
		g.setTransform(tx);   
		g.fill(arrowHead);
		g.dispose();
	}
	/*_________________________________________________*/
	public static void drawCross(Graphics g, int x, int y, int size) {
		Shape oldClip = g.getClip();
		g.setClip(x-size, y-size, size*2, size*2);
		g.drawLine(x, y-size, x, y+size);
		g.drawLine(x-size, y, x+size, y);
		g.setClip(oldClip);
	}
	/*_________________________________________________*/
	public static void drawCross(Graphics g, double x, double y, int size) {
		Shape oldClip = g.getClip();
		//g.setClip(x-size, y-size, size*2, size*2);
		drawLine(g,x, y-size, x, y+size);
		drawLine(g, x-size, y, x+size, y);
		//g.setClip(oldClip);
	}

	/* ............................................................................................................... */
	/** Given the coordinates of the start and end of a line, returns how far along the line (x,y) is */
	public static double fractionAlongLine(double x, double y, double xStart, double yStart, double xEnd, double yEnd, boolean xBias, boolean yBias) {
		if (xStart==xEnd)  //it's a vertical line or a single point
			if (y<=yStart)
				return 0.0;
			else if (y>=yEnd)
				return 1.0;
		if (yStart==yEnd)  //it's a vertical line or a single point
			if (x<=xStart)
				return 0.0;
			else if (x>=xEnd)
				return 1.0;

		if (!MesquiteDouble.contains(x, xStart, xEnd) && !MesquiteDouble.contains(y, yStart, yEnd)) {   //outside the bounds
			if ((yEnd>=yStart && y>yEnd) || (yEnd<yStart && y<yEnd))
				return 1.0;
			if ((xEnd>=xStart && x>xEnd) || (xEnd<xStart && x<xEnd))
				return 1.0;
			else
				return 0.0;
		}

		if (!MesquiteDouble.contains(y, yStart, yEnd)) // we are not in y range, must use x
			return Math.abs(1.0*(x-xStart)/(xEnd-xStart));
		if (!MesquiteDouble.contains(x, xStart, xEnd)) // we are not in x range, must use y
			return Math.abs(1.0*(y-yStart)/(yEnd-yStart));
		if (xBias)
			return Math.abs(1.0*(x-xStart)/(xEnd-xStart));
		if (yBias)
			return Math.abs(1.0*(y-yStart)/(yEnd-yStart));
		if (Math.abs(yEnd-yStart) > Math.abs(xEnd-xStart))  // y range larger
			return Math.abs(1.0*(y-yStart)/(yEnd-yStart));
		return Math.abs(1.0*(x-xStart)/(xEnd-xStart));
	}

	/* ............................................................................................................... */
	/** Returns the width of string in the current Graphics */
	public static int stringWidth(Graphics g, String s) {
		FontMetrics fm = g.getFontMetrics(g.getFont());
		if (fm==null)
			return -1;
		return fm.stringWidth(s);
	}
	/* ............................................................................................................... */
	/** Returns the width of string in the current Graphics */
	public static int stringHeight(Graphics g, String s) {
		FontMetrics fm = g.getFontMetrics(g.getFont());
		if (fm==null)
			return -1;
		return fm.getMaxAscent()+ fm.getMaxDescent();
	}

	/* ............................................................................................................... */
	/** Given the coordinates of the start and end of a line, returns the value of x at the middle of the line */
	public static double xCenterOfLine(double x1, double y1, double x2, double y2) {
		if (x1==x2)  //it's a vertical line or a single point
			return x1;
		return (Math.min(x1, x2) + Math.abs(x1-x2)/2);
	}
	/* ............................................................................................................... */
	/** Given the coordinates of the start and end of a line, returns the value of y at the middle of the line */
	public static double yCenterOfLine(double x1, double y1, double x2, double y2) {
		if (y1==y2)  //it's a horizontal line or a single point
			return y1;
		return (Math.min(y1, y2) + Math.abs(y1-y2)/2);
	}
	/* ............................................................................................................... */
	/** Given the coordinates of the start and end of a line, returns the angle in radians of the line */
	public static double angleOfLine(double x1, double y1, double x2, double y2) {
		if (y1==y2)  //it's a horizontal line or a single point
			if (x2>=x1)
				return 0.0;
			else
				return Math.PI;
		if (x1==x2)  //it's a vertical line or a single point
			if (y2>=y1)
				return Math.PI/2.0;
			else
				return -Math.PI/2.0;
		double a = Math.PI -Math.atan(-(y2-y1)*1.0/(x2-x1));  
		if (x2>x1)
			a = Math.PI+a;
		return a;
	}
	/* ............................................................................................................... */
	/** draws a line starting at x,y, and going in the direction of the angle */
	public static void drawAngledLine(Graphics g, double x, double y, double angle, double length) {
		double endX= (Math.cos(angle)*length)+x;
		double endY =(Math.sin(angle)*length)+y;
		GraphicsUtil.drawLine(g,x,y,endX, endY);
	}
	/*
	 * 		Graphics2D g2 = (Graphics2D)g;
		g2.rotate(-Math.PI/4, x, y);
		g2.translate(x, y);
		g2.setColor(Color.blue);
		g2.fillRect(0, 0, 30, 40);
		g2.translate(-x, -y);
		g2.rotate(Math.PI/4, x, y);


	/* ............................................................................................................... */
	/** creates a square beginning at x,y and tilted in the direction of the angle. */
	public static void translateAlongAngle(MesquiteDouble x, MesquiteDouble y, double angle, int length) {
		double adj= -(Math.cos(angle)*length);
		double opp =-(Math.sin(angle)*length);
		x.add(adj);
		y.add(opp);  
	}
	/* ............................................................................................................... */
	/** creates a square beginning at x,y and tilted in the direction of the angle. */
	public static Path2D.Double createAngledSquare(double x, double y, double angle, int length) {
		double adj= -(Math.cos(angle)*length);
		double opp =-(Math.sin(angle)*length);
		x -=opp/2;
		y-=adj/2;

		Path2D.Double poly = new Path2D.Double();
		poly.moveTo(x,y);
		poly.lineTo(x+opp,y+adj);
		poly.lineTo(x+opp-adj,y+adj+opp);
		poly.lineTo(x-adj,y+opp);
		poly.lineTo(x,y);
		return poly;

	}
	/* ............................................................................................................... */
	public static void shimmerVerticalOn(Graphics g, Panel panel, int top, int bottom, int x) {
		if (g==null && panel==null)
			if (!MesquiteInteger.isCombinable(x))
				return;
		Graphics mg = g;
		if (mg==null)
			mg = panel.getGraphics();
		if (mg == null)
			return;
//		mg.setColor(Color.black);
		if (GraphicsUtil.useXORMode(g, false)){
			mg.setXORMode(Color.white);
			mg.drawLine(x, top, x, bottom);
		}
//		mg.drawLine(x+1, top, x+1, bottom);
		if (g==null)
			mg.dispose();
	}
	/* ............................................................................................................... */
	public static void shimmerHorizontalOn(Graphics g, Panel panel, int left, int right, int y) {
		if (g==null && panel==null)
			if (!MesquiteInteger.isCombinable(y))
				return;
		Graphics mg = g;
		if (mg==null)
			mg = panel.getGraphics();
		if (mg == null)
			return;
		if (GraphicsUtil.useXORMode(g, false)){
			mg.setXORMode(Color.white);
			mg.drawLine(left, y, right, y);
		}
		if (g==null)
			mg.dispose();
	}
	/* -------------------------------------------------*/
	public static boolean useXOR = true;
	/*Bugs is os x Tiger ca. 10.4.8 can cause crashes with XORMode.  With OS X Tiger, it does not do XORMode with printing or if senstive is on */
	public static boolean useXORMode(Graphics g, boolean sensitive){
		if (sensitive || g instanceof PrintGraphics){
			//if (MesquiteTrunk.isMacOSX()  && System.getProperty("os.version").indexOf("10.4")>=0)
			//	return false;
			return useXOR;
		}
		return true;
	}
	/* -------------------------------------------------*/
	public static void drawXORLine (Graphics g, int xFrom, int yFrom, int xTo, int yTo, int thickness, Color color) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setXORMode(Color.white);
		g2.setColor(color); 
		
		Stroke st = g2.getStroke();
		g2.setStroke(new BasicStroke(thickness));
		
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.drawLine(xFrom,yFrom,xTo,yTo);
		g2.setStroke(st);

	}
	/* -------------------------------------------------*/
	public static void fillTransparentSelectionRectangle (Graphics g, int x, int y, int w, int h) {
		Composite composite = ColorDistribution.getComposite(g);
		ColorDistribution.setTransparentGraphics(g,0.3f);		
		g.setColor(Color.gray);
		g.fillRect(x,y,w, h);
		ColorDistribution.setComposite(g, composite);		
	}
	/* -------------------------------------------------*/
	public static void fillTransparentSelectionPolygon (Graphics g, Polygon poly) {
		Composite composite = ColorDistribution.getComposite(g);
		ColorDistribution.setTransparentGraphics(g,0.3f);		
		g.setColor(Color.gray);
		g.fillPolygon(poly);
		ColorDistribution.setComposite(g, composite);		
	}
	/* -------------------------------------------------*/
	public static void fillTransparentSelectionArea (Graphics2D g, Area area) {
		Composite composite = ColorDistribution.getComposite(g);
		ColorDistribution.setTransparentGraphics(g,0.3f);		
		g.setColor(Color.gray);
		g.fill(area);
		ColorDistribution.setComposite(g, composite);		
	}
	/* -------------------------------------------------*/
	public static void fillTransparentBorderedSelectionRectangle (Graphics g, int x, int y, int w, int h) {
		if (w < 0){
			int nx = x + w;
			x = nx;
			w = -w;
		}
		if (h < 0){
			int ny = y + h;
			y = ny;
			h = -h;
		}
		Composite composite = ColorDistribution.getComposite(g);
		ColorDistribution.setTransparentGraphics(g,0.3f);		
		g.setColor(Color.gray);
		g.fillRect(x,y,w,h);
		ColorDistribution.setComposite(g, composite);		
		g.setColor(Color.gray);
		g.drawRect(x,y,w,h);
	}
	/* -------------------------------------------------*/
	public static void fillTransparentBorderedSelectionPolygon (Graphics g, Polygon poly) {
		Composite composite = ColorDistribution.getComposite(g);
		ColorDistribution.setTransparentGraphics(g,0.3f);		
		g.setColor(Color.gray);
		g.fillPolygon(poly);
		ColorDistribution.setComposite(g, composite);		
		g.setColor(Color.gray);
		g.drawPolygon(poly);
	}
	/* -------------------------------------------------*/
	public static void shadeRectangle (Graphics g, int x, int y, int w, int h, Color color) {
		Composite composite = ColorDistribution.getComposite(g);
		ColorDistribution.setTransparentGraphics(g,0.2f);		
		g.setColor(color);
		g.fillRect(x,y,w, h);
		ColorDistribution.setComposite(g, composite);		
	}
	/* -------------------------------------------------*/
	public static void darkenRectangle (Graphics g, int x, int y, int w, int h, float f) {
		Composite composite = ColorDistribution.getComposite(g);
		ColorDistribution.setTransparentGraphics(g,f);		
		g.setColor(Color.black);
		g.fillRect(x,y,w, h);
		ColorDistribution.setComposite(g, composite);		
	}
	/* -------------------------------------------------*/
	public static void darkenRectangle (Graphics g, int x, int y, int w, int h) {
		darkenRectangle(g,x,y,w,h,0.2f);
	}
	/* -------------------------------------------------*/
	public static void fixRectangle (Rectangle rect) {
		if (rect.width < 0){
			int nx = rect.x + rect.width;
			rect.x = nx;
			rect.width = -rect.width;
		}
		if (rect.height < 0){
			int ny = rect.y + rect.height;
			rect.y = ny;
			rect.height = -rect.height;
		}
	}

	/* -------------------------------------------------*/
	public static void drawRect (Graphics g, int x, int y, int w, int h) {
		if (w < 0){
			int nx = x + w;
			x = nx;
			w = -w;
		}
		if (h < 0){
			int ny = y + h;
			y = ny;
			h = -h;
		}
		g.drawRect(x, y, w, h);
	}
	/* -------------------------------------------------*/
	public static void setFontName (String name, Graphics g) {
		Font curFont = g.getFont();
		Font fontToSet = new Font (name, curFont.getStyle(), curFont.getSize());
		if (fontToSet!= null) {
			curFont = fontToSet;
			g.setFont(curFont);
		}
	}
	/* -------------------------------------------------*/
	public static void setFontStyle (int style, Graphics g) {
		Font curFont = g.getFont();
		Font fontToSet = new Font (curFont.getName(), style, curFont.getSize());
		if (fontToSet!= null) {
			curFont = fontToSet;
			g.setFont(curFont);
		}
	}
	/* -------------------------------------------------*/
	public static void setFontSize (int size, Graphics g) {
		Font curFont = g.getFont();
		Font fontToSet = new Font (curFont.getName(), curFont.getStyle(), size);
		if (fontToSet!= null) {
			curFont = fontToSet;
			g.setFont(curFont);
		}
	}
	/* -------------------------------------------------*/
	public static void drawOval(Graphics g, int x, int y, int w, int h){
		try {
			Graphics2D g2 = (Graphics2D)g;
			Stroke st = g2.getStroke();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.drawOval(x,y,w,h); 
			g2.setStroke(st);
		}
		catch(NullPointerException e){
			MesquiteMessage.warnProgrammer("npe in draw oval x " + x + " y " + y + " w " + w + " h " + h);
		}
	}
	/* -------------------------------------------------*/
	public static void drawOval(Graphics g, double x, double y, int w, int h){
		try {
			Graphics2D g2 = (Graphics2D)g;
			Stroke st = g2.getStroke();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			Ellipse2D oval = new Ellipse2D.Double(x,y,w,h);
			g2.draw(oval); 
			g2.setStroke(st);
		}
		catch(NullPointerException e){
			MesquiteMessage.warnProgrammer("npe in draw oval x " + x + " y " + y + " w " + w + " h " + h);
		}
	}
	/* -------------------------------------------------*/
	public static void fillOval(Graphics g, double x,double y, double w, double h, boolean threeD){
		try {
			Graphics2D g2 = (Graphics2D)g;
			Stroke st = g2.getStroke();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			if (threeD){
				Color c = g.getColor();
				Color current = c;
				current = ColorDistribution.darker(current, 0.75);
				while (w>0) {
					g.setColor(current);
					fillOval(g,x,y,w,h);
					x++;
					y++;
					w-=4;
					h-=4;
					current = ColorDistribution.brighter(current, 0.75);
				}
				if (c!=null) g.setColor(c);
			}
			else
				fillOval(g,x,y,w,h); 
			g2.setStroke(st);

		}
		catch(NullPointerException e){
			MesquiteMessage.warnProgrammer("npe in fill oval x " + x + " y " + y + " w " + w + " h " + h);
		}
	}
	/* -------------------------------------------------*/
	public static void fillArc(Graphics g, double x, double y, double w, double h, int startAngle, int arcAngle, boolean threeD){
		if (arcAngle < 1)
			return;
		if (MesquiteTrunk.isWindows()){ //this is workaround to Windows problem by which goes all black if too close to 0 or 360
			double spotsize = MesquiteDouble.maximum(w, h);
			if (3.14*spotsize*(360-arcAngle)/360<1){
				fillOval(g, x, y, w, h, threeD);
				return;
			}
			if (3.14*spotsize*arcAngle/360<1)
				return;
		}
		if (threeD){
			Color c = g.getColor();
			Color current = c;
//			TODO: needs to define a polygon that clips to prevent upward curved edges on left side
			current = ColorDistribution.darker(current, 0.75);
			while (w>0) {
				g.setColor(current);
				fillArc(g,x,y,w,h, startAngle, arcAngle);
				x++;
				y++;
				w-=4;
				h-=4;
				current = ColorDistribution.brighter(current, 0.75);
			}
			if (c!=null) g.setColor(c);
		}
		else
			fillArc(g,x,y,w,h, startAngle, arcAngle); 
	}

}


