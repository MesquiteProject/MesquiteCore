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
/** A small label that automatically resizes itself.  Still needs work, since should check if font has changed.
Handles up to MAXLINES lines.*/
public class MesquiteLabel extends MousePanel {
	String[] s;
	private int myWidth = 5;
	//private MesquiteModule ownerModule;
	int idNumber=0;
	boolean recheckSize = false;
	int numLines = 0;
	Font curFont;
	int oldLineHeight = 16;
	Color color, dark, darkdark;
	MesquiteCommand command = null;
	static final int MAXLINES = 32;
	String arguments = null;
	boolean checkedDoomed = true; //used because DrawHierarchy was having trouble on quit
	public MesquiteLabel(MesquiteModule ownerModule, int idNumber){ //TODO: get rid of ownerModule
		super();
		//this.ownerModule = ownerModule;
		this.idNumber=idNumber;
		//setBackground(Color.green);
		setSize(1, 1);
		setVisible(false);
		this.color = Color.lightGray;
		s = new String[MAXLINES];
		for (int i=0; i<MAXLINES; i++)
			s[i]=null;
	}
	public MesquiteLabel(MesquiteModule ownerModule){
		this(ownerModule, 0);
	}
	public void setCheckDoomed(boolean checkedDoomed){
		this.checkedDoomed = checkedDoomed;
	}
	/*public MesquiteModule getOwnerModule() {
		return ownerModule;
	}*/
	public void dispose(){
		if (command !=null)
			command.dispose();
		command = null;
		super.dispose();
	}
	public void setText(String st) {  
		if ((st==null) || (st.equals(""))) {
			numLines = 0;
			setSize(1, 1);
			setVisible(false);
		}
		else  {
			if (st.indexOf('\n')<0) {
				this.s[0]=st;
				numLines = 1;
			}
			else {
				numLines = 0;
				while(st.indexOf('\n')>=0){
					String firstLine = st.substring(0, st.indexOf('\n'));
					s[numLines]=firstLine;
					if (numLines<MAXLINES)
						numLines++;
					st = st.substring(st.indexOf('\n')+1, st.length());
				}
				if (!StringUtil.blank(st)){
					addLine(st);
					setVisible(true);
					recheckSize = true;
					repaint();
					return;
				}
			}
			setVisible(true);
			recheckSize = true;
			repaint();
		}
	}
	public void setCommand(MesquiteCommand command) {  
		this.command = command;
	}
	public void setArguments(String arguments) {  
		this.arguments = arguments;
	}
	public String getText(int line) {  
		if (line<numLines)
			return s[line];
		else
			return "";
	}
	public void addLine(String st) {  
		if ((st==null) || (st.equals(""))) {
		}
		else {
			setVisible(true);
			s[numLines]=st;
			if (numLines<MAXLINES)
				numLines++;
			recheckSize = true;
			repaint();
		}
	}
	
	public void setFontName (String name) {
		if (curFont==null)
			curFont = getParent().getFont();
 		Font fontToSet = new Font (name, curFont.getStyle(), curFont.getSize());
 		if (fontToSet!= null) {
 			curFont = fontToSet;
 			setFont(curFont);
 		}
    	 }
	public void setFontStyle (int style) {
		if (curFont==null)
			curFont = getParent().getFont();
 		Font fontToSet = new Font (curFont.getName(), style, curFont.getSize());
 		if (fontToSet!= null) {
 			curFont = fontToSet;
 			setFont(curFont);
 		}
    	 }
    	 public void setFontSize (int size) {
		if (curFont==null)
			curFont = getParent().getFont();
 		Font fontToSet = new Font (curFont.getName(), curFont.getStyle(), size);
 		if (fontToSet!= null) {
 			curFont = fontToSet;
 			setFont(curFont);
 		}
    	 }
    	 public void setColor (Color color) {
		this.color = color;
		if (color != null) {
			dark = color.darker();
			darkdark = dark.darker();
		}
    	 }
    	public int getWidth(Graphics g){
    		if (g==null)
    			return 0;
			FontMetrics fm=g.getFontMetrics(g.getFont());
			int maxWidth = 0;
			for (int i=0; i<numLines; i++){
				int thisWidth =fm.stringWidth(s[i]);
				if (thisWidth>maxWidth)
					maxWidth = thisWidth;
			}
			if (myWidth!=maxWidth+10) 
				return maxWidth+10;
			else
				return myWidth;
    	}
    	public void update(Graphics g){
    		paint(g);
    	}
    	
	public void paint(Graphics g) {
	   	if (checkedDoomed){
		   	if (MesquiteWindow.checkDoomed(this))
		   		return;
		}
		int w =getBounds().width;
		int h = getBounds().height;
		FontMetrics fm=g.getFontMetrics(g.getFont());
		int lineHeight = fm.getAscent() + fm.getDescent() + 4;
		
		if (recheckSize || lineHeight!=oldLineHeight) {
			recheckSize = false;
			int maxWidth = 0;
			for (int i=0; i<numLines; i++){
				int thisWidth =fm.stringWidth(s[i]);
				if (thisWidth>maxWidth)
					maxWidth = thisWidth;
			}
			if (myWidth!=maxWidth+10 || lineHeight!=oldLineHeight) {
				oldLineHeight = lineHeight;
				myWidth = maxWidth+10;
				setSize(myWidth, lineHeight*numLines);
				repaint();
			   	if (checkedDoomed)
				   	MesquiteWindow.uncheckDoomed(this);
				return;
			}
			oldLineHeight = lineHeight;
		} 
		g.setColor(getBackground());
		g.fillRect(0,0,w,h);
			
		if (color != null) {
			g.setColor(color);
			g.fillRoundRect(0,0,w,h, 5, 5);
			g.setColor(Color.darkGray);
			g.drawRoundRect(0,0,w-1,h-1, 5, 5);
			g.setColor(Color.white);
			g.drawRoundRect(1,1,w-3,h-3, 5, 5);
			//g.setColor(dark);
			//g.drawRoundRect(1,1,w-1,h-1, 5, 5);
		}
		g.setColor(Color.black);
		for (int i=0; i<numLines && i < s.length; i++){
			if (s[i] == null || s[i].length() == 0){
			}
			else if (s[i].charAt(0) == '\2') {
				GraphicsUtil.setFontStyle(Font.BOLD, g);
				g.drawString(s[i].substring(1), 4,(i+1)*lineHeight-fm.getDescent()-2);
				GraphicsUtil.setFontStyle(Font.PLAIN, g);
			}
			else if (s[i].charAt(0) == '\1') {
				GraphicsUtil.setFontStyle(Font.ITALIC, g);
				g.drawString(s[i].substring(1), 4,(i+1)*lineHeight-fm.getDescent()-2);
				GraphicsUtil.setFontStyle(Font.PLAIN, g);
			}
			else
				g.drawString(s[i], 4,(i+1)*lineHeight-fm.getDescent()-2);
		}
		if (checkedDoomed)
			MesquiteWindow.uncheckDoomed(this);
	}
  	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		if (MesquiteWindow.getQueryMode(this)) {
			MesquiteWindow.respondToQueryMode("Label", command, this);
			return;
		}
		else if (command == null)
 			MesquiteModule.mesquiteTrunk.logln("Label touched with ID number " + Integer.toString(idNumber));
 		else
 			command.doItMainThread(arguments +  " "  + MesquiteEvent.modifiersToString(modifiers), CommandChecker.getQueryModeString("Label", command, this), this);
 		return;
	}
}


