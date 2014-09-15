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
import java.util.*;

/* ======================================================================== */
public class ImageLabel implements NexusWritable {
	private String text;
	private int x, y, pointX, pointY;
	private int fontSize = 12;
	private int width = 100;
	private int height = 30;
	private int color = -1;
	private String colorName = " ";
	private String font = "SanSerif";
	String extras = "";
	Font f;
	StringInABox textBox;
	
	boolean showPointer = true;
	boolean showShadow = true;
	boolean fixedToImage = true;
	public ImageLabel(){
		textBox = new StringInABox("", new Font (font, Font.PLAIN, fontSize), 100);
	}
	public ImageLabel cloneLabel(){
		ImageLabel label = new ImageLabel();
		label.text = text;
		label.font = font;
		label.extras = extras;
		label.x = x;
		label.y = y;
		label.pointX = pointX;
		label.pointY = pointY;
		label.fontSize = fontSize;
		label.width = width;
		label.height = height;
		label.color = color;
		label.showPointer = showPointer;
		label.showShadow = showShadow;
		label.fixedToImage = fixedToImage;
		return label;
	}
	public boolean equals(ImageLabel other){
		if (!StringUtil.stringsEqual(text, other.text))
			return false;
		if (!StringUtil.stringsEqual(font, other.font))
			return false;
		if (!StringUtil.stringsEqual(extras, other.extras))
			return false;
			
		if (x != other.x || y != other.y  || pointX != other.pointX || fontSize != other.fontSize  || pointY != other.pointY ||width != other.width || height != other.height || color != other.color)
			return false;
		if (showPointer != other.showPointer || showShadow != other.showShadow || fixedToImage != other.fixedToImage)
			return false;
		return true;
	}
	public String getNexusString(){
		if (text == null || text.equals(""))
			text = " ";
		String c =  "";
		if (colorName!=null)
			c = " COLOR = " + StringUtil.tokenize(colorName);
		else
			c = " COLORNUMBER = " + color;
		String s = "(TEXT = " + StringUtil.tokenize(text) + " FONT = " + StringUtil.tokenize(font) + c + " SIZE = " + fontSize;
		s += " x = " + x + " y = " + y + " fixedToImage = " + fixedToImage + " pointerX = " + pointX + " pointerY = " + pointY + " showpointer = " + showPointer + " width = " + width + " " + extras + " )";
		return s;
	}
	public Font getFont(){
		return f;
	}
	public void setText(String t){
		text = t;
		textBox.setString(t);
		setHeight(textBox.getHeight());
	}
	public String getText(){
		return text;
	}
	public void setWidth(int i){
		width = i;
		textBox.setWidth(width);
		setHeight(textBox.getHeight());
	}
	public int getWidth(){
		return width;
	}
	public void setHeight(int i){ //not stored in file; calculated when drawing
		height = i;
	}
	public int getHeight(){
		return height;
	}
	public StringInABox getTextBox(){
		return textBox;
	}
	
	public void setFontSize(int i){
		fontSize = i;
		f = new Font (font, Font.PLAIN, fontSize);
		textBox.setFont(f);
		setHeight(textBox.getHeight());
	}
	public int getFontSize(){
		return fontSize;
	}
	public void setFontColor(int i){
		color = i;
		colorName = ColorDistribution.getStandardColorName(i);
	}
	public void setFontColor(String name){
		colorName = name;
		color = ColorDistribution.getStandardColorNumber(name);
	}
	public int getFontColor(){
		return color;
	}
	public void setFontName(String s){
		font = s;
		f = new Font (font, Font.PLAIN, fontSize);
		textBox.setFont(f);
		setHeight(textBox.getHeight());
	}
	public String getFontName(){
		return font;
	}
	public void setLocation(int x, int y){
		this.x = x;
		this.y = y;
	}
	public void setX(int x){
		this.x = x;
	}
	public void setY(int y){
		this.y = y;
	}
	public int getX(){
		return x;
	}
	public int getY(){
		return y;
	}
	public void setFixedToImage(boolean sh){
		fixedToImage = sh;
	}
	public boolean getFixedToImage(){
		return fixedToImage;
	}
	
	public void setPointerLocation(int x, int y){
		pointX = x;
		pointY = y;
	}
	public void setPointerX(int x){
		pointX = x;
	}
	public void setPointerY(int y){
		pointY = y;
	}
	public int getPointerX(){
		return pointX;
	}
	public int getPointerY(){
		return pointY;
	}
	public void setShowPointer(boolean sh){
		showPointer = sh;
	}
	public boolean getShowPointer(){
		return showPointer;
	}
	public void setShowShadow(boolean sh){
		showShadow = sh;
	}
	public boolean getShowShadow(){
		return showShadow;
	}
	
	public void addExtraSubcommand(String s){
		extras += s;
	}
	
}
	


