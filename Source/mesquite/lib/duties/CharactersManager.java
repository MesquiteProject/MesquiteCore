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
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;


/* ======================================================================== */
/** Coordinates managers of character data matrices. Relies on CharMatrixManagers for the details of
each data type.  Example module: "Manage CHARACTERS blocks" (class ManageCharacters)*/

public abstract class CharactersManager extends FileElementManager   {

   	 public Class getDutyClass() {
   	 	return CharactersManager.class;
   	 }
 	public String getDutyName() {
 		return "Coordinator of managers of character data matrices";
   	 }
 	 public String[] getDefaultModule() {
    	 	return new String[] {"#ManageCharacters"};
    	 }
	/** create new CharacterData object with data type indicated by passed string.  String is *not* NEXUS keyword
	for data type, but rather the descriptive phrase returned by the CharMatrixManagers' getDataClassName methods.*/
	public abstract mesquite.lib.characters.CharacterData newCharacterData(Taxa taxa, int numChars, String dataType);
	
	public abstract String[] dataClassesAvailable();
	
	public abstract mesquite.lib.characters.CharacterData processFormat(MesquiteFile file, Taxa taxa, String formatCommand, int numChars, String title, String fileReadingArguments);
	
	/**Returns the module that reads and writes matrices of a particular subclass*/
	public abstract CharMatrixManager getMatrixManager(Class dataClass);
	public abstract MesquiteModule getListOfCharactersModule(CharacterData data, boolean showIfClosed);

	public Class getElementClass(){
		return mesquite.lib.characters.CharacterData.class;
	}
 	public abstract MesquiteSubmenuSpec getListsSubmenu();
 	
	public boolean getSearchableAsModule(){
		return false;
	}
}


