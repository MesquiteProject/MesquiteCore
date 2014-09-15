/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


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
import java.util.*;
import mesquite.lib.duties.*;
import mesquite.lib.*;

/*Last documented:  May 2000 */
/*======================================================================== */
/** a set of Models, for file to keep track of all of its defined character models. */
public class ModelVector extends ListableVector {
	public ModelVector  () {
		super();
	}
	
	public String getTypeName(){
		return "Character Models";
	}
	/** return CharacterModel of given name*/
	public CharacterModel getCharacterModel(String name) {
		if (name==null)
			return null;
		Enumeration enumeration =elements();
		while (enumeration.hasMoreElements()){
			Object obj = enumeration.nextElement();
			CharacterModel cm = (CharacterModel)obj;
			if (name.equalsIgnoreCase(cm.getName()) || name.equalsIgnoreCase(cm.getNEXUSName()) )
				return cm;
		}
		return null;
	}

	/** disposes model*/
	public void dispose() {
		Enumeration enumeration =elements();
		while (enumeration.hasMoreElements()){
			Object obj = enumeration.nextElement();
			CharacterModel cm = (CharacterModel)obj;
			cm.dispose();
		}
		super.dispose();
	}
	public int getNumNotBuiltIn() {
		Enumeration enumeration =elements();
		int count = 0;
		while (enumeration.hasMoreElements()){
			Object obj = enumeration.nextElement();
			CharacterModel cm = (CharacterModel)obj;
			if (!cm.isBuiltIn())
				count++;
		}
		return count;
	}
}

