/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.cont.ManageGeogChars;
/*~~  */

import java.util.*;
import java.awt.*;
import java.io.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.cont.lib.*;

/* ======================================================================== 
Manages geographic data matrices  */
public class ManageGeogChars extends ManageContCharsA {
	int maxNumChars = 2;
	
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	
	/*.................................................................................................................*/
	public Class getStateClass(){
		return GeographicState.class;
	}
	/*.................................................................................................................*/
	public Class getDataClass(){
		return GeographicData.class;
	}
	/*.................................................................................................................*/
	public  String getDataClassName(){
		return "Geographic Data";
	}
	/*.................................................................................................................*/
	public  CharacterData getNewData(Taxa taxa, int numChars){
		return new GeographicData(this, taxa.getNumTaxa(), numChars, taxa);
	}
	/*.................................................................................................................*/
	public boolean readsWritesDataType(Class dataClass){
		return (dataClass == GeographicData.class);
	}
	/*.................................................................................................................*/
	public boolean readsWritesDataType(String dataType){
		return dataType.equalsIgnoreCase("Geographic");
	}
	/*.................................................................................................................*/
	public String getDataTypeString() {
		return "GEOGRAPHIC";
	}

	/*.................................................................................................................*/
    	 public String getName() {
		return "Manage Geographic character matrices";
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Manages data matrices of geographic data (including read/write in NEXUS file)." ;
   	 }
}
	
	

