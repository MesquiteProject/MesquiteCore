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
/** Subclass of FileAssistant that produces scattergram charts.*/

public abstract class FileAssistantCS extends FileAssistantC  {

   	 public Class getDutyClass() {
   	 	return FileAssistantCS.class;
   	 }
	 public String[] getDefaultModule() {
 	 	return new String[] {"#CharsScattergram", "#TreesScattergram", "#TaxaScattergram"};
 	 }
 	public String getDutyName() {
 		return "Scattergram Chart Assistant for File";
   	 }

	/*.................................................................................................................*/
 	public void windowGoAway(MesquiteWindow whichWindow) {
		if (whichWindow == null)
			return;
			whichWindow.hide();
			whichWindow.dispose();
			iQuit();
	}
}


