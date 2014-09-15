/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.BlockDirectlyFromFile;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.treefarm.lib.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/** Supplies tree blocks from other sources.*/
public class BlockDirectlyFromFile extends BlockFromTreeSource { //BlockFromTreeSource
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(mesquite.trees.ManyTreesFromFile.ManyTreesFromFile.class, getName() + "  needs a source of trees.",
		"The source of trees can be selected initially");
	}
	/*.................................................................................................................*/
 	 public String getSource(){
 		 return "#ManyTreesFromFile";
 	 }
	/*.................................................................................................................*/
	 public boolean enableNumTreesChoice(){
 	 	return false;
   	 }

	/*.................................................................................................................*/
    public String getName() {
		return "Tree Block Directly From File";
   	 }
   	 
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Supplies a block of trees directly from a file";
   	 }
 	/*.................................................................................................................*/
  	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
  	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
  	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
     	public int getVersionOfFirstRelease(){
     		return 110;  
     	}
     	/*.................................................................................................................*/
     	public boolean isPrerelease(){
     		return false;
     	}
}

