/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
Version 2.75, September 2011.
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
public abstract class FillableMesquiteSymbol extends MesquiteSymbol {
	Checkbox fillBox = null;
	protected boolean fill=true;
	Color fillColor = Color.black;
	Color rimColor = Color.black;
	/**sets whether to fill the symbol or not*/
	public void setFill(boolean fill){
		this.fill = fill;
	}
	/**gets whether to fill the symbol or not*/
	public boolean getFill(){
		return fill;
	}
	/**gets the NEXUS commands to specify the options specific to this tool*/
	public String getBasicNexusOptions(){
		if (getFill())
			return " FILL ";
		else
			return " OPEN ";
	}
	/*.................................................................................................................*/
	public void  setToCloned(MesquiteSymbol cloned){
		super.setToCloned(cloned);
		setFill(((FillableMesquiteSymbol)cloned).getFill());
	}
	/*.................................................................................................................*/
	public void addDialogElements(ExtensibleDialog dialog, boolean includeSize){
		super.addDialogElements(dialog, includeSize);
		fillBox = dialog.addCheckBox("filled", fill);
	}
	/*.................................................................................................................*/
	public void getDialogOptions(){
		super.getDialogOptions();
		fill = fillBox.getState();
	}
	/*.................................................................................................................*/
	public void processSubcommand(String token, Parser subcommands){
		super.processSubcommand(token, subcommands);
		if (token.equalsIgnoreCase("FILL"))
			setFill(true);
		else if (token.equalsIgnoreCase("OPEN"))
			setFill(false);
	}
	/**sets fill color*/
	public void setFillColor(Color color){
		this.fillColor = color;
	}
	/**gets whether the symbol is drawn via a Polygon*/
	public boolean getIsPolygon(){
		return true;
	}
	/**gets the Polygon*/
	public Polygon getPolygon(){
		return getPolygon(0);
	}
	/**gets the Polygon*/
	public Polygon getPolygon(int maxSize){
		return null;
	}
	/**draws shape if non-Polygon*/
	public void drawShape(Graphics g){
		drawShape(g,0,false);
	}
	/**draws shape if non-Polygon*/
	public void drawShape(Graphics g, int maxSize, boolean fillBlack){
	}

	/**fills shape if non-Polygon*/
	public void fillShape(Graphics g, int maxSize){
	}

	/**inSymbols if non-Polygon*/
	public boolean inSymbol(int symbolX, int symbolY, int x, int y, int maxSize){
		return false;
	}
	/**inSymbols if non-Polygon*/
	public boolean inRect(int symbolX, int symbolY, int x1, int y1, int x2, int y2, int bound){
		return false;
	}

	/**draws the Symbol*/
	public void drawSymbol(Graphics g, int x, int y, int maxWidth, int maxHeight, boolean fillBlack){
		int bound = 0;
		if (maxWidth > 0)
			bound = maxWidth;
		if (maxHeight>0 && maxHeight<maxWidth)
			bound = maxHeight;

		if (getIsPolygon()) {
			Polygon poly = getPolygon(bound);
			if (poly!=null) {
				poly.translate(x, y);
				if (fillBlack) {
					g.setColor(Color.black);
					g.fillPolygon(poly);
				}
				else if (getFill()) {
					g.setColor(fillColor);
					g.fillPolygon(poly);
				}
				g.setColor(rimColor);
				g.drawPolygon(poly);
				poly.translate(-x, -y);
			}
		}
		else
			drawShape(g, bound, fillBlack);
	}

	/**fills the Symbol*/
	public void fillSymbol(Graphics g, int x, int y, int maxWidth, int maxHeight){
		int bound = 0;
		if (maxWidth > 0)
			bound = maxWidth;
		if (maxHeight>0 && maxHeight<maxWidth)
			bound = maxHeight;

		if (getIsPolygon()) {
			Polygon poly = getPolygon(bound);
			if (poly!=null) {
				poly.translate(x, y);
				g.fillPolygon(poly);
				poly.translate(-x, -y);
			}
		}
		else
			fillShape(g, bound);
	}


	public boolean inSymbol(int symbolX, int symbolY, int x, int y, int maxWidth, int maxHeight){
		int bound = 0;
		if (maxWidth > 0)
			bound = maxWidth;
		if (maxHeight>0 && maxHeight<maxWidth)
			bound = maxHeight;

		if (getIsPolygon()) {
			Polygon poly = getPolygon(bound);
			if (poly!=null) {
				poly.translate(symbolX, symbolY);
				boolean in = poly.contains(x,y);
				poly.translate(-symbolX, -symbolY);
				return in;
			}
		}
		else  {
			return inSymbol(symbolX, symbolY,x,y,bound);
		}
		return false;
	}

	public boolean inRect(int symbolX, int symbolY, int x1, int y1, int x2, int y2, int maxWidth, int maxHeight){
		if (x1==x2 && y1==y2)
			return inSymbol(symbolX, symbolY, x1,y1,maxWidth,maxHeight);
		int bound = 0;
		if (maxWidth > 0)
			bound = maxWidth;
		if (maxHeight>0 && maxHeight<maxWidth)
			bound = maxHeight;

		if (getIsPolygon()) {
			Polygon poly = getPolygon(bound);
			if (poly!=null) {
				poly.translate(symbolX, symbolY);
				Rectangle bounds = poly.getBounds();
				boolean in = bounds.intersects(new Rectangle(x1,y1,x2-x1,y2-y1));
				poly.translate(-symbolX, -symbolY);
				return in;
			}
		}
		else {
			return inRect(symbolX, symbolY,x1,y1,x2,y2,bound);
		}
		return false;
	}


}

