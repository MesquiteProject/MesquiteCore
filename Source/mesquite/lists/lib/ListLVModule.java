/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison. 
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lists.lib;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import mesquite.lib.duties.*;
import mesquite.lib.*;


/* ======================================================================== */

public abstract class ListLVModule extends ListModule  {
   	 public Class getDutyClass() {
   	 	return ListLVModule.class;
   	 }
 	public String getDutyName() {
 		return "List Module for Listable Vectors";
   	 }

	/*.................................................................................................................*/
	public boolean rowsMovable(){
		return true;
	}
	/** returns either a String (if not editable) or a MesquiteString (if to be editable) */
	public String getAnnotation(int row){
		Object mo = getMainObject();
		if (mo instanceof ListableVector){
			ListableVector myVector = (ListableVector)mo;
			if (myVector!=null && row>=0 && row < myVector.size()){
				Object obj = myVector.elementAt(row);
				if (obj instanceof Annotatable){
					return ((Annotatable)obj).getAnnotation();
				}
			}
		}
		return null;
	}
	/** sets the annotation for a row*/
	public void setAnnotation(int row, String s, boolean notify){
		Object mo = getMainObject();
		if (mo instanceof ListableVector){
			ListableVector myVector = (ListableVector)mo;
			if (myVector!=null && row>=0 && row < myVector.size()){
				Object obj = myVector.elementAt(row);
				if (obj instanceof Annotatable){
					((Annotatable)obj).setAnnotation(s, notify);
				}
			}
		}
	}
	/** returns a String explaining the row */
	public String getExplanation(int row){
		Object mo = getMainObject();
		if (mo instanceof ListableVector){
			ListableVector myVector = (ListableVector)mo;
			if (myVector!=null && row>=0 && row < myVector.size()){
				Object obj = myVector.elementAt(row);
				if (obj instanceof Explainable){
					return ((Explainable)obj).getExplanation();
				}
			}
		}
		return null;
	}

}




