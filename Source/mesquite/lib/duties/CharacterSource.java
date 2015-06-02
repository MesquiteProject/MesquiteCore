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
/**
This class of modules supplies characters for use in calculation routines.  
These methods must be passed a Taxa object because which characters are appropriate may depend on the
taxa*/

public abstract class CharacterSource extends CharacterOneSource  {
	public static MesquiteBoolean storedAsDefault = new MesquiteBoolean(true);
   	 public Class getDutyClass() {
   	 	return CharacterSource.class;
   	 }
   	 public static boolean useStoredAsDefault(){
   		 return storedAsDefault.getValue() || (mesquite.lib.simplicity.InterfaceManager.isSimpleMode());
   	 }
   	 public String[] getDefaultModule() {
   	 	return new String[] {"#StoredCharacters", "#SimulatedCharacters"};
   	 }
	 public String getFunctionIconPath(){
   		 return getRootImageDirectoryPath() + "functionIcons/charSource.gif";
   	 }
}


