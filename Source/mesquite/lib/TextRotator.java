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
import java.awt.geom.*;


/* ======================================================================== */
/** A utility class to draw 90 degree rotated text.  Because this can be slow (bitmap manipulation), it
can store a series of images for a series of strings.  If on next request nothing seems to have changed,
it simply uses the old images.*/
public class TextRotator { 
	int numStrings;
	String[] strings;
	Color[] colors, backgrounds;
	Font[] fonts;
	Image[] images;
	boolean[] validity;
	Color bg;
	public TextRotator () {
	}
	
	public TextRotator (int numStrings) {
		this();
		this.numStrings = numStrings;
		strings = new String[numStrings];
		fonts = new Font[numStrings];
		colors = new Color[numStrings];
		backgrounds = new Color[numStrings];
		images = new Image[numStrings];
		validity = new boolean[numStrings];
		for (int i=0; i<numStrings; i++) {
			strings[i]="";
			validity[i]=false;
		}
	}
	public void invalidateAll(){
		if (validity!=null)
		for (int i=0; i<numStrings; i++) {
			validity[i]=false;
		}
	}
	public int getNumStrings() {
		if (strings==null)
			return 0;
		else
			return strings.length;
	}
	public void assignBackground(Color bkg) {
		bg = bkg;
	}
	
	/* -------------------------------------------------*/
	public void drawRotatedText(String s, Graphics g, Component component, int horizPosition, int vertPosition) {
		if (s == null || component == null)
			return;
		
		//for Java 1.2

			drawFreeRotatedText(s, g,horizPosition, vertPosition, -Math.PI/2, null, false, null);
		
	}
	
	public void drawFreeRotatedText(String s, Graphics g, int horizPosition, int vertPosition, double rotation, Point offsets, boolean alignBottom, Polygon poly) {
			if (s==null)
				return;
			Font font = g.getFont();
			FontMetrics fontMet = g.getFontMetrics(font);
			int height = fontMet.getHeight();
			int width = fontMet.stringWidth(s);
			
			int textOffsetH, textOffsetV;
			if (offsets != null) {
				textOffsetH = offsets.x;
				textOffsetV = offsets.y;
			}
			else {
				textOffsetH = height*2/3;
				textOffsetV = 0;
				if (!alignBottom)
					textOffsetV = fontMet.stringWidth(s);
			}
			Graphics2D g2 = (Graphics2D)g;
			g2.translate(textOffsetH, textOffsetV);
			g2.rotate(rotation, horizPosition, vertPosition);
			if (poly !=null){
				double x = horizPosition;
				double y = vertPosition-height*0.667;
				double w = width;
				double h = height;
				AffineTransform transform = g2.getTransform();
				float[] dest = new float[8];
				double[] src = new double[8];
				src[0] = x;
				src[1] = y;
				src[2] = x+w;
				src[3] = y;
				src[4] = x+w;
				src[5] = y+h;
				src[6] = x;
				src[7] = y+h;
				transform.transform(src, 0, dest, 0, 4);
				poly.npoints=0;
				poly.addPoint((int)dest[0], (int)dest[1]);
				poly.addPoint((int)dest[2], (int)dest[3]);
				poly.addPoint((int)dest[4], (int)dest[5]);
				poly.addPoint((int)dest[6], (int)dest[7]);
				poly.npoints=4;
			}
			if (bg!=null) {
				Color c = g2.getColor();
				g2.setColor(bg);
				g2.fillRect(horizPosition,(int)(vertPosition-height*0.667),width, height);
				g2.setColor(c);
			} 
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g2.drawString(s, horizPosition, vertPosition);
			g2.rotate(-rotation, horizPosition, vertPosition);
			g2.translate(-textOffsetH, -textOffsetV);
	}
	/* -------------------------------------------------*/
	/** This version is to speed up redraws, since it checks if it has already stored the image */
	public void drawRotatedText(String s, int stringNumber, Graphics g, Component component, int horizPosition, int vertPosition, boolean alignBottom) {
		if (s == null)
			return;
		//for Java 1.2

			drawFreeRotatedText(s, g,horizPosition, vertPosition, -Math.PI/2, null, alignBottom, null);

	}
	/* -------------------------------------------------*/
	public void drawRotatedText(String s, int stringNumber, Graphics g, Component component, int horizPosition, int vertPosition) {
		drawRotatedText(s, stringNumber, g, component,  horizPosition, vertPosition, true);
	}
	/* -------------------------------------------------*/
	private int[] rotateCCW (int pixels[], int width, int height) {
		int newPixels[] = null;
		if ((width*height)== pixels.length) {
			newPixels = new int[width*height];
			//for (int i=0; i<width*height; i++)
			//	newPixels[i]= 0;
				
			int newIndex=0;
			for (int oldX= width-1; oldX>=0; oldX--) 
				for (int oldY= 0; oldY<height; oldY++)
					newPixels[newIndex++] = pixels[oldY*width+(oldX)];
			
		}
		return newPixels;
	}
}


