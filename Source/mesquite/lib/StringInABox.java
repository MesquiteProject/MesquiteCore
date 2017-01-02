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
import java.awt.geom.Rectangle2D;
import java.util.*;



/* ======================================================================== */
/** A utility class to draw a string in a box, useful for printing and screen display.  A bit like TextArea, but
without rectangle around it and with multipage printing facility.  The string, font, and width are given to it, 
and it calculates a breakdown of the string into an array of strings each of which will fit in the allotted
width, and also calcualted is how high of a rectangle is needed to accommodate all of the text.  */
public class StringInABox {
	Font font = null;
	StringBuffer sb = null;
	int width = 0;
	int maxWidth = -1;
	int maxWidthMunched = -1;
	int height = 0;
	static final char linebreak = '\n';
	static int spacesPerTab = 4;
	static final char space = ' ';
	int buffer = 2;
	static final int leftMargin = 0;
	String[] strings;
	Color textColor, edgeColor;
	int linkEnd = -1;
	int linkStart = -1;
	Rectangle2D linkBox = null;
	/* ----------------------------------------------- */
	public StringInABox(StringBuffer s, Font f, int w){
		font = f;
		width = w;
		sb = s;
		munch();
	}
	/* ----------------------------------------------- */
	public StringInABox(String s, Font f, int w){
		font = f;
		width = w;
		if (s==null)
			s = "";
		sb = new StringBuffer(s);
		munch();
	}
	/* ----------------------------------------------- */
	public void setLink(int start, int end){
		//permits characters start to end to be treated as a link; in blue
		linkStart = start;
		linkEnd = end;
	}
	public Rectangle2D getLinkBounds(){  //the box must have been drawn before the bounds are known
		if (linkEnd < 0 && linkStart<0)
			return null;
		return linkBox;
	}
	public boolean inLink(int x, int y){  //the box must have been drawn before the bounds are known
		if (linkEnd < 0 && linkStart<0)
			return false;
		if (linkBox == null)
			return false;
		return x >= linkBox.getX() && x <= linkBox.getX() + linkBox.getWidth() && y >= linkBox.getY() && y <= linkBox.getY() + linkBox.getHeight();
	}
	/* ----------------------------------------------- */
	public void setBuffer(int b){
		buffer = b;
	}
	public void setStringAndFont(StringBuffer s, Font f){
		sb = s;
		font = f;
		munch();
	}
	public void setFont(Font f){
		if (font==f)
			return;
		font = f;
		munch();
	}
	public Font getFont(){
		return font;
	}
	Font storedFont;
	public void storeFont(){
		storedFont = font;
	}
	public void recallFont(){
		if (storedFont != null)
		setFont(storedFont);
	}
	public void scaleFont(double scale){
		if (font == null)
			return;
		int size = (int)(font.getSize()*scale);
		if (size == 0)
			size = 2;
		setFont(new Font(font.getName(), font.getStyle(), size));
	}
	public void setWidth(int w){
		if (width==w)
			return;
		width = w;
		munch();
	}
	public int getWidth(){
		return width;
	}
	public int getMaxWidthMunched(){
		if (maxWidthMunched >=0)
			return maxWidthMunched;
		return width;
	}
	public int getMaxWidthDrawn(){
		if (maxWidth >=0)
			return maxWidth;
		return width;
	}
	public void setString(StringBuffer s){
		sb = s;
		munch();
	}
	public void setString(String s, Graphics g){
		if (s==null)
			s = "";
		sb = new StringBuffer(s);
		munch(g);
	}
	public void setStringAndFontAndWidth(String s, Font f, int w, Graphics g){
		if (s==null)
			s = "";
		font = f;
		width = w;
		sb = new StringBuffer(s);
		munch(g);
	}
	public void setString(String s){
		if (s==null)
			s = "";
		sb = new StringBuffer(s);
		munch();
	}
	public String getString(){
		if (sb!=null)
			return sb.toString();
		else
			return null;
	}
	/* ----------------------------------------------- */
	/**Prints the passed string (automatically prints with full page width and on
	multiple pages as needed.  \n are used as linebreads.  Text is wrapped as necessary to
	fit onto page*/
	public static void printText(Frame f, String s, Font font) {
		if (s == null || f==null || font==null)
			return;
		MainThread.incrementSuppressWaitWindow();
		PrintJob pjob = f.getToolkit().getPrintJob(f, "Print?", null);
		if (pjob != null) {
			Dimension dim = pjob.getPageDimension();
			StringBuffer sB = new StringBuffer(s);
			StringInABox sBox = new StringInABox(sB,font, dim.width);  //TODO: should set font here!!!
			int tot =  sBox.getHeight();
			double lastY = 0;
			boolean done = false; 
			while (lastY<tot && !done){
				int r = sBox.getRemainingHeight((int)lastY);
				if (r> dim.height)
					r=dim.height;
				Graphics pg = pjob.getGraphics();
				if (pg!=null) {
					lastY = sBox.draw(pg, 0, 0, lastY, r);  
					pg.dispose();
				}
				else done = true;
			}
		}
		pjob.end();
		MainThread.decrementSuppressWaitWindow();
	}
	/* ----------------------------------------------- */
	/**Prints the passed string using given PrintJob (automatically prints with full page width and on
	multiple pages as needed.  \n are used as linebreads.  Text is wrapped as necessary to
	fit onto page*/
	public static void printText(PrintJob pjob, String s, Font font) {
		if (s == null || font==null || pjob==null)
			return;
		Dimension dim = pjob.getPageDimension();
		StringBuffer sB = new StringBuffer(s);
		StringInABox sBox = new StringInABox(sB, font, dim.width);  
		int tot =  sBox.getHeight();
		double lastY = 0;
		boolean done = false; 
		while (lastY<tot && !done){
			int r = sBox.getRemainingHeight((int)lastY);
			if (r> dim.height)
				r=dim.height;
			Graphics pg = pjob.getGraphics();
			if (pg!=null) {
				lastY = sBox.draw(pg, 0, 0, lastY, r);  
				pg.dispose();
			}
			else done = true;
		}
	}
	/* ----------------------------------------------- */
	private boolean lineEnd(char c){
		return (c=='\n' || c=='\r');
	}
	/* ----------------------------------------------- */
	private boolean isPunc(char c){
		return (c=='\n' || c=='\r' || c==',' || c==';' || c==':' || c==')' || c==']'|| c=='}'|| c=='!'|| c=='?'|| c=='-');
	}
	/* ----------------------------------------------- */
	/* takes stringBuffer and inserts line breaks as needed to satisfy available width, does some other processing
	(e.g., tabs to spaces, \r to \n), then converts to an array of strings*/
	private void munch(){
		try {
		munch(null);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	private char getCharAt(StringBuffer sb, int i){
		try {
			if (i < sb.length())
			return sb.charAt(i);
		}
		catch (StringIndexOutOfBoundsException e){
		}
		return 0;
	}
	/* ----------------------------------------------- */
	/* takes stringBuffer and inserts line breaks as needed to satisfy available width, does some other processing
	(e.g., tabs to spaces, \r to \n), then converts to an array of strings*/
	private synchronized void munch(Graphics g){
		if (sb!=null && font!=null && width>0 && sb.length()>0){
			StringBuffer sMunched = new StringBuffer(sb.length());

			FontMetrics fm = null;
			if (g != null){
				Font prevFont = g.getFont();
				g.setFont(font);
				fm  = g.getFontMetrics();
				g.setFont(prevFont);

			}
			else fm =Toolkit.getDefaultToolkit().getFontMetrics(font);
			int pos = 0;
			maxWidthMunched = 0;
			int i=0;
			long count = 0;
			do {
				count++;
				if (count>100000) {
					//MesquiteMessage.warnProgrammer("munch in StringInABox exceeded 100000 iterations (i " + i + " sb.length() " + sb.length() + ") ");
					return;
				}
				char nextChar = 0;
					nextChar = getCharAt(sb, i);
				if (lineEnd(nextChar)) {
					sMunched.append(linebreak);
					pos = 0;
					i++;
				}
				else if (nextChar == '\t'){  //tab
					for (int j = 1; j<spacesPerTab && pos<width; j++) {
						sMunched.append(space);
						pos += fm.charWidth(space);
					}
					if (pos > maxWidthMunched)  //this may be off by one character's worth of width if over
						maxWidthMunched = pos;
					if (pos>width) {
						sMunched.append(linebreak);
						pos = 0;
					}
					i++;
				}
				else if (nextChar == space) {
					sMunched.append(space);
					pos += fm.charWidth(space);
					if (pos > maxWidthMunched)  //this may be off by one character's worth of width if over
						maxWidthMunched = pos;
					if (pos>width) {
						sMunched.append(linebreak);
						pos = 0;
					}
					i++;
				}
				else if (StringUtil.punctuation(nextChar, null)) {
					sMunched.append(nextChar);
					pos += fm.charWidth(nextChar);
					if (pos > maxWidthMunched)  //this may be off by one character's worth of width if over
						maxWidthMunched = pos;
					if (pos>width) {
						sMunched.append(linebreak);
						pos = 0;
					}
					i++;
				}
				else {
					int wordLength = 0;
					int j = i;
					char c =(char)0;;
						c = getCharAt(sb, j);
					while (j<sb.length() && !StringUtil.punctuation(c, null) && !StringUtil.whitespace(c, null)) { //measuring next word
						wordLength +=fm.charWidth(c);
						j++;
							c = getCharAt(sb, j);
					}
					if ((wordLength>=width && pos==0) || pos+wordLength<width){ 
						//single word wider than width; break into pieces;  or, will fit and og as far as can
							c = getCharAt(sb, i);
						while (i<sb.length() && !StringUtil.punctuation(c, null) && !StringUtil.whitespace(c, null) && pos<width) {
							sMunched.append(c);
							pos += fm.charWidth(c);
							i++;
								c = getCharAt(sb, i);
						}
						if (pos > maxWidthMunched)  //this may be off by one character's worth of width if over
							maxWidthMunched = pos;
					}
					else {
						sMunched.append(linebreak);
						pos = 0;
					}
				}
			} while(i<sb.length());


			StringTokenizer tokenizer = new StringTokenizer(sMunched.toString(), ""+ linebreak);  
			storeInStrings(tokenizer);
		}
	}
	/* ----------------------------------------------- */
	private void storeInStrings(StringTokenizer tokenizer){
		strings = new String[tokenizer.countTokens()];
		int i=0;
		while (tokenizer.hasMoreTokens() && i<strings.length) {
			strings[i] = tokenizer.nextToken();
			i++;
		}	
	}
	/* To use StringUtil.highlightString (only works for horizontal text)*/
	public void setColors(Color textColor, Color edgeColor){
		this.textColor = textColor;
		this.edgeColor = edgeColor;
	}
	/* ----------------------------------------------- */
	/** draw text based at x, y, starting at local position y in box for height h*/
	public int getNextPageTop(int yPos, int h){

		if (sb!=null && strings!=null && font!=null && width>0){
			int lastDrawnYPos = yPos;
			FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
			int increment = fm.getMaxAscent() + fm.getMaxDescent() + buffer;
			int accumulatedHeight = increment;
			int i = 0;
			while (i<strings.length && accumulatedHeight<yPos) {
				accumulatedHeight += increment;
				i++;
			}
			int heightOnThisPage = increment;
			while (i<strings.length && heightOnThisPage<h) {
				lastDrawnYPos = heightOnThisPage + accumulatedHeight;
				heightOnThisPage += increment;
				i++;
			}	
			return lastDrawnYPos;
		}
		return 0;
	}
	/* ----------------------------------------------- */
	TextRotator textRotator;
	/** draw text based at x, y, starting at local position y in box for height h*/
	public double draw(Graphics g, int x, int y, int yPos, int h, Component component, boolean drawHorizontal){
		return draw(g, x, y, yPos, h, component, drawHorizontal, false);
	}
	public double draw(Graphics g, double x, double y, double yPos, int h, Component component, boolean drawHorizontal, boolean tightVertical){
		try {
			if (sb!=null && sb.length()>0 && strings!=null && font!=null && width>0){
				double lastDrawnYPos = yPos;
				Font prevFont = g.getFont();
				Color prevColor = g.getColor();
				g.setFont(font); //use g's font if font is null
				FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
				int increment = fm.getMaxAscent() + fm.getMaxDescent() + buffer;
				int accumulatedHeight = increment;
				int i = 0;
				while (i<strings.length && accumulatedHeight<yPos) {
					accumulatedHeight += increment;
					i++;
				}
				if (!drawHorizontal && textRotator==null)
					textRotator = new TextRotator(MesquiteInteger.maximum(10, strings.length));
				int heightOnThisPage = increment;
				if (tightVertical)
					increment -= 4;
				maxWidth = -1;
				int charCount = 0;
				linkBox = null;
				while (i<strings.length && heightOnThisPage<h) {
					if (strings[i] != null){
						if (drawHorizontal) {
							if (textColor !=null && edgeColor!=null) {
								StringUtil.highlightString(g, strings[i], (int)x, (int)y + heightOnThisPage, textColor, edgeColor);  // integer nodeloc approximation
							}
							else {
								boolean drawn = false;
								
								//LINKS=====
								if (linkStart >= 0 && linkEnd>=0){
									int localLength = strings[i].length();
									int localLinkStart = linkStart - charCount;
									int localLinkEnd = linkEnd - charCount;
									if (localLinkStart<localLength && localLinkEnd >= 0){

										if (localLinkStart<0)
											localLinkStart = 0;
										if (localLinkEnd>localLength)
											localLinkEnd = localLength;
										int prelinkWidth = 0;
										int linkWidth = 0;
										String prelink = strings[i].substring(0, localLinkStart);
										if (prelink == null)
											prelink = "";
										else
											prelinkWidth =  fm.stringWidth(prelink);
										String link = strings[i].substring(localLinkStart, localLinkEnd);
										if (link == null)
											link = "";
										else
											linkWidth =  fm.stringWidth(link);
										String remnant = strings[i].substring(localLinkEnd, strings[i].length());
										if (remnant == null)
											remnant = "";
										GraphicsUtil.drawString(g,prelink, x, y + heightOnThisPage);  
										Color c = g.getColor();
										g.setColor(Color.blue);
										GraphicsUtil.drawString(g,link,  x + prelinkWidth, y + heightOnThisPage);  
										GraphicsUtil.drawLine(g, x + prelinkWidth, y + heightOnThisPage+1, x + prelinkWidth + linkWidth, y + heightOnThisPage+1);  
										if (linkBox == null) {
											linkBox = new Rectangle2D.Double(x + prelinkWidth, y + heightOnThisPage+1- increment, linkWidth, increment);
										}
										else {
											double linkX = linkBox.getX(), linkY =linkBox.getY(), linkW =linkBox.getWidth(), linkH = linkBox.getHeight();
											if (linkBox.getX() + linkBox.getWidth() < x + prelinkWidth + linkWidth)
												linkW = x + prelinkWidth + linkWidth - linkBox.getX();
											if (linkBox.getX()> x + prelinkWidth)
												linkX = x + prelinkWidth;
											if (linkBox.getY() + linkBox.getHeight() < y + heightOnThisPage+1)
												linkH = y + heightOnThisPage+1 - linkBox.getY();
											if (linkBox.getY()> y + heightOnThisPage+1- increment)
												linkY = y + heightOnThisPage+1- increment;
											linkBox.setFrame(linkX, linkY, linkW, linkH);
										}
										//g.drawRect(linkBox.getX(), linkBox.y, linkBox.width, linkBox.height);
										g.setColor(c);
										GraphicsUtil.drawString(g,remnant, x + fm.stringWidth(prelink + link), y + heightOnThisPage);  
										drawn = true;
									}
								}
								//LINKS=====
								
								
								if (!drawn) {
									GraphicsUtil.drawString(g,strings[i], x, y + heightOnThisPage); 
								}
							}
						}
						else {
							g.setColor(textColor);
							textRotator.assignBackground(edgeColor);
							textRotator.drawRotatedText(strings[i], i, g, component, (int)x + heightOnThisPage, (int)y); // integer nodeloc approximation
						}

						lastDrawnYPos = y + heightOnThisPage + accumulatedHeight;
						heightOnThisPage += increment;
						int w = 0;
						if (i>=0 && i< strings.length && strings[i] != null)
							w = fm.stringWidth(strings[i]);
						if (w>maxWidth)
							maxWidth = w;
						charCount += strings[i].length();
					}
					i++;
				}	
				g.setFont(prevFont);
				g.setColor(prevColor);

				return lastDrawnYPos;
			}
		}
		catch (Exception e){
			return 0;
		}
		return 0;
	}
	/* ----------------------------------------------- */
	/** draw text based at x, y, starting at local position y in box for height h*/
	public double draw(Graphics g, double x, double y, double yPos, int h){
		return draw(g,x,y,yPos,h, null, true, false);
	}
	/* ----------------------------------------------- */
	/** draw text based at x, y*/
	public double draw(Graphics g, int x, int y){
		return draw(g, x, y, 0, getHeight());
	}
	/* ----------------------------------------------- */
	/** draw text based at x, y*/
	public double draw(Graphics g, double x, double y){
		return draw(g, x, y, 0, getHeight());
	}
	/* ----------------------------------------------- */
	/** draw text based at x, y*/
	public double drawTight(Graphics g, int x, int y){
		return draw(g,x,y,0,getHeight(), null, true, true);
	}
	/* ----------------------------------------------- */
	/** draw text based at x, y*/
	public double drawTight(Graphics g, double x, double y){
		return draw(g,x,y,0,getHeight(), null, true, true);
	}
	/* ----------------------------------------------- */
	/** draw text based at x, y*/
	public double drawInBox(Graphics g, Color backgroundColor, int x, int y){
		int h = getHeight();
		Color c = g.getColor();
		g.setColor(backgroundColor);
		g.fillRect(x-8, y, width+12, h+8);
		if (c!=null) g.setColor(c);
		g.drawRect(x-8, y, width+11, h+7);
		return draw(g, x, y, 0, h);
	}
	/* ----------------------------------------------- */
	public int getHeight(){
		if (strings==null)
			return 0;
		FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
		int increment = fm.getMaxAscent() + fm.getMaxDescent() + buffer;
		return (strings.length) * increment + buffer;	
	}
	/* ----------------------------------------------- */
	public int getRemainingHeight(int usedSoFar){
		FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
		int increment = fm.getMaxAscent() + fm.getMaxDescent() + buffer;
		return getHeight() - usedSoFar/increment*increment + increment+ buffer;	
	}
	/*.................................................................................................................*/
	public static void drawStringIfNotBlank(Graphics g, String s, int x, int y){
		if (g == null || StringUtil.blank(s))
			return;
		try {
			g.drawString(s, x, y);
		}
		catch (Exception e){
		}
	}
}



