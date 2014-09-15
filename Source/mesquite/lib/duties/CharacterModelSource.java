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


/* ======================================================================== */
/**
This class of modules supplies character models for use in calculation routines.
These methods must be passed a Data or a CharacterDistribution object because which models are appropriate will depend on these*/

public abstract class CharacterModelSource extends MesquiteModule  {

   	 public Class getDutyClass() {
   	 	return CharacterModelSource.class;
   	 }
 	public String getDutyName() {
 		return "Model Source";
   	 }
   	/** returns model for character ic in data */
   	public abstract CharacterModel getCharacterModel(mesquite.lib.characters.CharacterData data, int ic);
   	/** returns model for character represented by states */
   	public abstract CharacterModel getCharacterModel(CharacterStatesHolder states);
   	
   	/** returns whether requires an input CharacterData or can run off of any CharacterDistribution. Default is false. Should be overridden as appropriate. */
   	public boolean requiresCharacterData(){
   		return false;
   	}
   	
   	/**sets whether module may reassign a different model to current character; indicates that employers will have only one character active
   	at a time, and thus a menu item to change the active character's model is appropriate*/
   	public void setOneCharacterAtATime(boolean chgbl){}
}


