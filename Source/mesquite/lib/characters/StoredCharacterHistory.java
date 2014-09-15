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
import mesquite.lib.duties.*;
import mesquite.lib.*;

/*Last documented:  April 2003 */

/* ======================================================================== */
/**Used to store character History connected with a tree.  Available but not used as of April 2003.*/
public class StoredCharacterHistory extends FileElement implements Identifiable  {
	CharacterHistory history;
	Tree tree;
	public StoredCharacterHistory(CharacterHistory history, Tree tree){
		this.history = history;
		this.tree = tree;
	}
	
	public CharacterHistory getHistory(){
		return history;
	}
	public NexusBlock addToFile(MesquiteFile f, MesquiteProject proj, ElementManager manager){

		return super.addToFile(f, proj, manager);
		
	}
	public Tree getTree(){
		return tree;
	}
}

