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

/*.................................................................................................................*/
/**    */
public interface Selectionable extends Listenable {
	/** Sets whether or not the part is selected */
	public void setSelected(int part, boolean select);

	/** Returns whether the part is selected */
	public boolean getSelected(int part);

	/** Deselects all parts */
	public void deselectAll();

	/** Selects all parts */
	public void selectAll();

	/** Returns whether there are any selected parts */
	public boolean anySelected();

	/** Returns number of selected parts */
	public int numberSelected();
	
	/** Returns number of parts that can be selected*/
	public int getNumberOfSelectableParts();
}




