/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.ManyTreesFromFile;
/*~~  */

import java.io.*;
import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.trees.lib.ManyTreesFromFileLib;


public class ManyTreesFromFile extends ManyTreesFromFileLib {

	
	/*.................................................................................................................*/
	 public String getName() {
	return "Use Trees from Separate NEXUS File";
	 }
/*.................................................................................................................*/
	 public String getNameForMenuItem() {
	return "Use Trees from Separate NEXUS File...";
	 }
	 
		/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Supplies trees directly from a file, without bringing the contained tree block entirely into memory.  " + 
		"This is a special purpose module designed to allow much larger blocks of trees to be used within constraints of memory, but will make some calculations slower.  " + 
		"Except for this special use, we recommend you use Include or Link from the file menu to access external tree files.  " + 
		"This module does NOT copy the trees into your main data file, and so if you save your main data file then move it or the tree file, the data file will no longer be able to find the trees.  " + 
		"This module does not know how many trees are in the file, and hence may attempt to read files beyond the number in the file.";
   	 }

}



