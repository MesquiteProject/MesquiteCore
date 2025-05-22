/* Mesquite source code.  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib;

import java.awt.*;
import java.awt.event.*;


public class ResultCodes {
	public final static int SPECIFICATION_MISSING = -10;
	public final static int NOTHING_RETURNED = -9;
	public static int USERCANCELONINITIALIZE = -8;
	public final static int NOT_YET_DONE = -7;
	public final static int FILE_PROBLEM = -6;
	public final static int INPUT_NULL = -5;
	public final static int OBJECT_LOCKED = -4;
	public final static int INCOMPATIBLE_DATA = -3;
	public final static int ERROR = -2;  //use only if you don't want to say what kind of error
	public final static int USER_STOPPED = -1;
	public final static int SUCCEEDED = 0;
	public final static int NO_ERROR = 1;
	public final static int NO_RESPONSE = 2;
	public final static int NO_CHANGE = 3;
	public final static int MEH = 4;

}


