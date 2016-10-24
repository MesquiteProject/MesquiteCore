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
public abstract class MesquiteSymbol extends Listened implements Listable  {
	protected String name;
	protected int size=8;
	Color color = Color.black;
	IntegerField sizeField = null;
	double rescaleValue = 1.0;
	public MesquiteSymbol() {
	}
	/**sets the size of the symbol*/
	public void setSize(int size){
		this.size = size;
	}
	/**gets the size of the symbol*/
	public int getSize(){
		return size;
	}
	/**gets the name of the symbol*/
	public String getName(){
		return "";
	}
	/**gets the NEXUS commands to specify the options specific to this tool*/
	public String getBasicNexusOptions(){
		return " ";
	}
	/**gets the NEXUS commands to specify the options specific to this tool*/
	public String getExtraNexusOptions(){
		return " ";
	}
	public double getRescaleValue() {
		return rescaleValue;
	}
	public void setRescaleValue(double rescaleValue) {
		this.rescaleValue = rescaleValue;
	}

	/*.................................................................................................................*/
	public void  setToCloned(MesquiteSymbol cloned){
		setSize(cloned.getSize());
	}
	/*.................................................................................................................*/
	public  abstract MesquiteSymbol  cloneMethod();
	/*.................................................................................................................*/
	public void addDialogElements(ExtensibleDialog dialog, boolean includeSize){
		if (includeSize)
			sizeField = dialog.addIntegerField("Size: ", size,4);
	}
	/*.................................................................................................................*/
	public void getDialogOptions(){
		if (sizeField!=null)
			size = sizeField.getValue();
	}
	/*.................................................................................................................*/
	public void interpretNexus(Parser subcommands){
		if (subcommands !=null){ //this should be passed into group to handle?
			String token = null;
			while ((token = subcommands.getNextToken())!=null){
				processSubcommand(token, subcommands);
			}
		}
	}
	/*.................................................................................................................*/
	public void processSubcommand(String token, Parser subcommands){
		if (token.equalsIgnoreCase("SIZE")){
			token = subcommands.getNextToken(); //=
			int symSize = MesquiteInteger.fromString(subcommands.getNextToken()); 
			if (MesquiteInteger.isCombinable(symSize))
				size=symSize;
		}
	}
	/**sets fill color*/
	public void setColor(Color color){
		this.color = color;
	}
	/**draws the Symbol*/
	public void drawSymbol(Graphics g, double x, double y){
		drawSymbol(g,x,y,0,0,false);
	}
	/**draws the Symbol*/
	public void drawSymbol(Graphics g, double x, double y, int maxWidth, int maxHeight, boolean fillBlack){
	}
	/**fills the Symbol with current color*/
	public void fillSymbol(Graphics g, double x, double y){
		fillSymbol(g,x,y,0,0);
	}
	/**fills the Symbol with current color*/
	public void fillSymbol(Graphics g, double x, double y, int maxWidth, int maxHeight){
	}
	/**returns if the point x, y is contained within the Symbol*/
	public boolean inSymbol(double symbolX, double symbolY, int x, int y, int maxWidth, int maxHeight){
		return false;
	}
	/**returns if the point x, y is contained within the Symbol*/
	public boolean inSymbol(double symbolX, double symbolY, int x, int y){
		return inSymbol(symbolX, symbolY, x,y,0,0);
	}
	/**returns if the point x, y is contained within the Symbol*/
	public boolean inRect(double symbolX, double symbolY, int x1, int y1, int x2, int y2, int maxWidth, int maxHeight){
		return false;
	}
	/**returns if the point x, y is contained within the Symbol*/
	public boolean inRect(double symbolX, double symbolY, int x1, int y1, int x2, int y2){
		return inRect(symbolX, symbolY, x1,y1,x2,y2,0,0);
	}
}

