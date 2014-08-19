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

public class BooleanUtil {

	/*-----------------------------------------*/
	public static boolean anyValuesTrue(boolean[] booleanArray) {
		for (int i=0; i<booleanArray.length; i++) {
			if (booleanArray[i])
				return true;
		}
		return false;
	}

	/*-----------------------------------------*/
	public static boolean[]  copyBooleanArray(boolean[] booleanArray) {
		boolean[] newArray = new boolean[booleanArray.length];
		for (int i=0;i<newArray.length; i++)
			newArray[i]=booleanArray[i];
		return newArray;
	}

	
	/*------------------------------------------*/
 	/**Reverses the elements of a boolean array*/
 	public static boolean[] reverseBooleanArray (boolean[] d) {
		if (d==null)
			return null;
		boolean[] values = new boolean[d.length];
		for (int i=0; i<values.length; i++)
			values[values.length-1-i] =  d[i];
		return values;
 	}

}
