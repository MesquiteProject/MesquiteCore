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
/**Supplies a single tree.  (see TreeSource.  The distinctions between these are not yet fully settled.  See issues
discussed under CharacterSource and CharMatrixSource*/

public abstract class OneTreeSource extends MesquiteModule  {
	
   	 public Class getDutyClass() {
   	 	return OneTreeSource.class;
   	 }
 	public String getDutyName() {
 		return "One Tree Source";
   	 }
	 public String getFunctionIconPath(){
   		 return getRootImageDirectoryPath() + "functionIcons/treeSource.gif";
   	 }
 	 
   	 public String[] getDefaultModule() {
   		 return new String[] {"#TreeOfContext"};
   	 }
   	 /** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
    	happening at inopportune times (e.g., while a long chart calculation is in mid-progress).  This version allows one to supply an explanation to the user*/
   	 public void initialize(Taxa taxa, String explanationForUser){
   		 initialize(taxa);
   	 }

   	 /** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
       	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	 public abstract void initialize(Taxa taxa);

   	 /** Called to reset if needed.  E.g., This allows TreeOfContext to set context to null */
   	 public void reset(){
   	 }

   	 public abstract Tree getTree(Taxa taxa, String explanationForUser);

   	 public abstract Tree getTree(Taxa taxa);
	/*.................................................................................................................*/
  	public abstract void setPreferredTaxa(Taxa taxa);

	/* if tree source is getting trees from some other source, e.g. from TreeContext, then pass back here ultimate tree source from which trees are given*/
  	public abstract MesquiteModule getUltimateSource();
}


