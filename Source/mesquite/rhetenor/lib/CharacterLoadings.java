/* Mesquite source code (Rhetenor package).  Copyright 1997 and onward E. Dyreson and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.rhetenor.lib; 

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.cont.lib.*;


/* ======================================================================== */
/***/

public abstract class CharacterLoadings extends NumberForCharacter implements NumForCharTreeIndep  {

	public Class getDutyClass(){
		return CharacterLoadings.class;
	}
 	public String getDutyName() {
 		return "Character Loadings";
   	 }
 	public abstract void setOrdination(Ordination ord, Taxa taxa);
   	public abstract void setCurrentAxis(int i);
   	public abstract int getCurrentAxis();
   	public abstract int getNumberOfAxes();
}



