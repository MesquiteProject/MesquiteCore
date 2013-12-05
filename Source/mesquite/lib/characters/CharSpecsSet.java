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
package mesquite.lib.characters; 

import java.awt.*;
import mesquite.lib.duties.*;
import mesquite.lib.*;

/*Last documented:  December 1998 */
/* ======================================================================== */
/** This abstract class serves to superclass weight sets, model sets, inclusion sets, character sets, and any
other listing of characters with specification of values for each character.  The different sorts of specsets are stored in different vectors
within a CharacterData object.*/
public interface CharSpecsSet extends Listable, Identifiable {	
 	/*.................................................................................................................*/
	/** Add num characters just after "starting" (filling with default values)  */
  	public boolean addParts(int starting, int num);
	/*.................................................................................................................*/
	/** Delete characters specified  */
	public boolean deleteParts(int starting, int num);
	/*.................................................................................................................*/
	/** Move num characters starting at first, to just after character justAfter  */
	public boolean moveParts(int first, int num, int justAfter);
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public boolean swapParts(int first, int second);
	
	public CharacterData getCharacterData();
}

