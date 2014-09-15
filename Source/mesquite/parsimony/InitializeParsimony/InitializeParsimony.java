/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.parsimony.InitializeParsimony;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.parsimony.lib.*;

/* ======================================================================== */
public class InitializeParsimony extends FileInit {
	public String getName() {
		return "Initialize Parsimony";
	}
	public String getExplanation() {
		return "Initializes default character models for parsimony." ;
	}
	/*.................................................................................................................*/

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	/*.................................................................................................................*/
	public void fileElementAdded(FileElement element) {
		if (element == null || getProject()==null)
			return;
		if (element instanceof CharacterData) {
			CharacterModel defaultModel=null;
			ParsimonyModelSet currentParsimonyModels;
			CharacterData data = (CharacterData)element;
			if (data.getCurrentSpecsSet(ParsimonyModelSet.class) == null) {
				defaultModel =  data.getDefaultModel("Parsimony");
				currentParsimonyModels= new ParsimonyModelSet("UNTITLED", data.getNumChars(), defaultModel, data);
				currentParsimonyModels.addToFile(element.getFile(), getProject(), null);
				data.setCurrentSpecsSet(currentParsimonyModels, ParsimonyModelSet.class);
			}
			if (getProject().getCharacterModels()==null)
				MesquiteMessage.warnProgrammer("charModels null in iMS Init Pars");
		}
	}

	/*.................................................................................................................*/
	public boolean isSubstantive() {
		return false;
	}
	/*.................................................................................................................*/

}

