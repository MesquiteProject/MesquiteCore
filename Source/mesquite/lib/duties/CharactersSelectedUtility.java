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

import mesquite.lib.MesquiteModule;
import mesquite.lib.SelectionInformer;
import mesquite.lib.characters.CharacterData;


/* ======================================================================== */
/***/

public abstract class CharactersSelectedUtility extends MesquiteModule  {

   	 public Class getDutyClass() {
   	 	return CharactersSelectedUtility.class;
   	 }
 	public String getDutyName() {
 		return "Utility to act give selected characters";
   	}
 	public abstract void setDataAndSelectionInformer(CharacterData data, SelectionInformer informer);
 	public abstract void characterTouched(int ic);

}


