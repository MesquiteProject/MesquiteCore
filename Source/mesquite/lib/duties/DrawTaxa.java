/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.duties;

import java.awt.*;
import mesquite.lib.*;


/* ======================================================================== */
/** This is superclass of all Mesquite taxa drawing modules.  Creates a TaxaDrawing, which does
the drawing of the taxa. */

public abstract class DrawTaxa extends MesquiteModule  {

   	public Class getDutyClass() {
   		return DrawTaxa.class;
   	}
   	 
 	public String getDutyName() {
 		return "Draw Taxa";
   	}
   	public String[] getDefaultModule() {
   		return new String[] {""};
   	}
   	   	
	/** Returns the preferred size (if any) of the tree drawing */
	public Dimension getPreferredSize(){
		return null;
	}
   	 /** Returns a TaxaDrawing to be used in the given TaxaDisplay, with the given inital number of terminal taxa*/
	public abstract TaxaDrawing createTaxaDrawing(TaxaDisplay taxaDisplay, int numTaxa);
   	public boolean isSubstantive(){
   		return false;  
   	}
   	
	public double getRescaleValue() {
		return 1.0;
	}

}


