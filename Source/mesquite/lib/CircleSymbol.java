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
import mesquite.lib.duties.*;

/* ======================================================================== */
public class CircleSymbol extends FillableMesquiteSymbol  {
	public CircleSymbol() {
	}
	public CircleSymbol(int size) {
		super();
		setSize(size);
	}
	/**gets the name of the symbol*/
	public String getName(){
		return "Circle";
	}
	/*.................................................................................................................*/
	public  MesquiteSymbol  cloneMethod(){
		CircleSymbol newSymbol = new CircleSymbol();
		newSymbol.setToCloned(this);
		return  newSymbol;
	}
	/*.................................................................................................................*/
	public void addDialogElements(ExtensibleDialog dialog, boolean includeSize){
		super.addDialogElements(dialog,includeSize);
	}
	/*.................................................................................................................*/
	public boolean inRect(int symbolX, int symbolY, int x1, int y1, int x2, int y2, int bound){
		int symSize = size;
		if (size>bound && bound>0)
			symSize = bound;
		Rectangle rect = new Rectangle(symbolX-symSize,symbolY-symSize, symSize+symSize,symSize+symSize);
		return rect.intersects(new Rectangle(x1,y1,x2,y2));
	}
	/*.................................................................................................................*/
	public boolean inRect(int symbolX, int symbolY, int x1, int y1, int x2, int y2, int maxWidth, int maxHeight){
		int symSize = size;
		if (size>maxWidth && maxWidth>0)
			symSize = maxWidth;
		if (size>maxHeight && maxHeight>0)
			symSize = maxHeight;
		Rectangle rect = new Rectangle(symbolX-symSize,symbolY-symSize, symSize+symSize,symSize+symSize);
		return rect.intersects(new Rectangle(x1,y1,x2-x1,y2-y1));
	}
	/*.................................................................................................................*/
	public boolean inSymbol(int symbolX, int symbolY, int x, int y, int bound){
		int symSize = size;
		if (size>bound && bound>0)
			symSize = bound;
		Rectangle rect = new Rectangle(symbolX-symSize,symbolY-symSize, symSize+symSize,symSize+symSize);
		return rect.contains(x,y);
	}
	/*.................................................................................................................*/
	public boolean inSymbol(int symbolX, int symbolY, int x, int y, int maxWidth, int maxHeight){
		int symSize = size;
		if (size>maxWidth && maxWidth>0)
			symSize = maxWidth;
		if (size>maxHeight && maxHeight>0)
			symSize = maxHeight;
		Rectangle rect = new Rectangle(symbolX-symSize,symbolY-symSize, symSize+symSize,symSize+symSize);
		return rect.contains(x,y);
	}
	/*.................................................................................................................*/
	public void fillSymbol(Graphics g, int x, int y,  int maxWidth, int maxHeight){
		int symSize = size;
		if (size>maxWidth && maxWidth>0)
			symSize = maxWidth;
		if (size>maxHeight && maxHeight>0)
			symSize = maxHeight;
		g.fillOval(x-symSize,y-symSize, symSize+symSize,symSize+symSize);
	}
	/*.................................................................................................................*/
	public void drawSymbol(Graphics g, int x, int y,  int maxWidth, int maxHeight, boolean fillBlack){
		int symSize = size;
		if (size>maxWidth && maxWidth>0)
			symSize = maxWidth;
		if (size>maxHeight && maxHeight>0)
			symSize = maxHeight;
		if (fillBlack) {
			g.setColor(Color.black);
			g.fillOval(x-symSize,y-symSize, symSize+symSize,symSize+symSize);
		}
		else if (getFill()) {
			g.setColor(fillColor);
			g.fillOval(x-symSize,y-symSize, symSize+symSize,symSize+symSize);
		}
		g.setColor(color);
		g.drawOval(x-symSize,y-symSize, symSize+symSize,symSize+symSize);
	}
	/*.................................................................................................................*/
	public static void drawBlackCircle(Graphics g, int x, int y, int size){
		int symSize = size;
		g.setColor(Color.black);
		g.fillOval(x-symSize,y-symSize, symSize+symSize,symSize+symSize);
		g.drawOval(x-symSize,y-symSize, symSize+symSize,symSize+symSize);
	}
}

