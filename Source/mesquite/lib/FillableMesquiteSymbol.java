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
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

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
	/**sets fill color*/
	public void setColor(Color color){
		this.fillColor = color;
		this.color = color;
	}
	/**gets whether the symbol is drawn via a Polygon*/
	public boolean getIsPolygon(){
		return true;
	}
	/**gets the Polygon*/
	public Path2D.Double getPolygon(){
		return getPolygon(0);
	}
	/**gets the Polygon*/
	public Path2D.Double getPolygon(int maxSize){
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
	public boolean inSymbol(double symbolX, double symbolY, int x, int y, int maxSize){
		return false;
	}
	/**inSymbols if non-Polygon*/
	public boolean inRect(double symbolX, double symbolY, int x1, int y1, int x2, int y2, int bound){
		return false;
	}

	/**draws the Symbol*/
	public void drawSymbol(Graphics g, double x, double y, int maxWidth, int maxHeight, boolean fillBlack){
		int bound = 0;
		if (maxWidth > 0)
			bound = maxWidth;
		if (maxHeight>0 && maxHeight<maxWidth)
			bound = maxHeight;
		//rescaleValue=1.0;
		//rescaleValue=1.1;

		if (getIsPolygon()) {
			Path2D poly = getPolygon(bound);
			if (poly!=null){
				Graphics2D g2 = (Graphics2D)g;
				AffineTransform polyTransform = new AffineTransform();
				polyTransform.translate(x,y);
				//polyTransform.translate(x/rescaleValue,y/rescaleValue);
				//g2.setTransform(polyTransform);
				//polyTransform.translate(-x/rescaleValue,-y/rescaleValue);
				polyTransform.scale(rescaleValue, rescaleValue);
				//Debugg.println("rescale value " + rescaleValue);
				AffineTransform saveTransform = g2.getTransform();
				g2.setTransform(polyTransform);
				if (poly!=null) {
					if (fillBlack) {
						g.setColor(Color.black);
						GraphicsUtil.fill(g,poly);
					}
					else if (getFill()) {
						g.setColor(fillColor);
						GraphicsUtil.fill(g,poly);
					}
					g.setColor(rimColor);

					GraphicsUtil.draw(g,poly);
				}
				g2.setTransform(saveTransform);
			}
		}
		else
			drawShape(g, bound, fillBlack);
	}

	/**fills the Symbol*/
	public void fillSymbol(Graphics g, double x, double y, int maxWidth, int maxHeight){
		int bound = 0;
		if (maxWidth > 0)
			bound = maxWidth;
		if (maxHeight>0 && maxHeight<maxWidth)
			bound = maxHeight;

		if (getIsPolygon()) {
			Path2D poly = getPolygon(bound);
			if (poly!=null) {
				if (g instanceof Graphics2D) {
					Graphics2D g2 = (Graphics2D)g;
					AffineTransform polyTransform = new AffineTransform();
					polyTransform.translate(x/rescaleValue,y/rescaleValue);
					polyTransform.scale(rescaleValue, rescaleValue);
					AffineTransform saveTransform = g2.getTransform();
					g2.setTransform(polyTransform);
			    	 GraphicsUtil.fill(g,poly);
					g2.setTransform(saveTransform);
		       } else
		    	   GraphicsUtil.fill(g,poly);
			}
		}
		else
			fillShape(g, bound);
	}


	public boolean inSymbol(double symbolX, double symbolY, int x, int y, int maxWidth, int maxHeight){
		int bound = 0;
		if (maxWidth > 0)
			bound = maxWidth;
		if (maxHeight>0 && maxHeight<maxWidth)
			bound = maxHeight;

		if (getIsPolygon()) {
			Path2D.Double poly = getPolygon(bound);
			if (poly!=null) {
				AffineTransform polyTransform = new AffineTransform();
				polyTransform.translate(symbolX, symbolY);
				poly.transform(polyTransform);
				boolean in = poly.contains(x,y);
				return in;
			}
		}
		else  {
			return inSymbol(symbolX, symbolY,x,y,bound);
		}
		return false;
	}

	public boolean inRect(double symbolX, double symbolY, int x1, int y1, int x2, int y2, int maxWidth, int maxHeight){
		if (x1==x2 && y1==y2)
			return inSymbol(symbolX, symbolY, x1,y1,maxWidth,maxHeight);
		int bound = 0;
		if (maxWidth > 0)
			bound = maxWidth;
		if (maxHeight>0 && maxHeight<maxWidth)
			bound = maxHeight;

		if (getIsPolygon()) {
			Path2D.Double poly = getPolygon(bound);
			if (poly!=null) {
				AffineTransform polyTransform = new AffineTransform();
				polyTransform.translate(symbolX, symbolY);
				poly.transform(polyTransform);
				Rectangle bounds = poly.getBounds();
				boolean in = bounds.intersects(new Rectangle(x1,y1,x2-x1,y2-y1));
				return in;
			}

		}
		else {
			return inRect(symbolX, symbolY,x1,y1,x2,y2,bound);
		}
		return false;
	}


}

