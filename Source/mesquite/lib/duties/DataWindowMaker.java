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
import mesquite.lib.table.*;


/* ======================================================================== */
/**This is superclass of modules to present data matrix editor.*/

public abstract class DataWindowMaker extends MesquiteModule  {
	
   	 public Class getDutyClass() {
   	 	return DataWindowMaker.class;
   	 }
 	public String getDutyName() {
 		return "Data Window Maker";
   	}

   	public boolean isSubstantive(){
   		return false;  
   	}
   	//not abstract so as not to cause incompatibilities with 1.12 and previous modules
 	 public void resignCellColorer(MesquiteModule m){
 	 }
  	 public abstract void requestCellColorer(MesquiteModule m, int ic, int it, String message);
   	 public abstract void demandCellColorer(MesquiteModule m, int ic, int it, String message);

  	 public abstract mesquite.lib.characters.CharacterData getCharacterData();
  	 public abstract void linkEditor(DataWindowMaker mb, boolean linkeeIsNew);
  	 public abstract void unlinkEditor(DataWindowMaker mb);
	public abstract MesquiteTable getTable();
	public void setAsExtra(boolean e){
	}
}


