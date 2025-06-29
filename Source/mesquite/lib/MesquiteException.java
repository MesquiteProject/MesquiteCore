/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib;

public class MesquiteException extends RuntimeException{
	public static int lastLocation = 0;
	public static String lastCommand = "";
	public static  MesquiteBoolean reportErrorsAutomatically = new MesquiteBoolean(false);
	public static String lastLocMessage(){
		if (lastLocation == 0 || StringUtil.blank(lastCommand))
		return "";
		return "(loc: " + lastLocation +") " + lastCommand;
	}
	/*
	 * 1 - tree drawing
	 * 100 - MousePanel
	 */
}

