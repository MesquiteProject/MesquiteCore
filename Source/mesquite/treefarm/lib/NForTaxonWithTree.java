/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.lib;
/*~~  */

import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteString;
import mesquite.lib.NumberArray;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.Tree;

/** ======================================================================== */
/*
provides a superclass for NumberForTaxon that use a tree;
*/

public abstract class NForTaxonWithTree extends MesquiteModule {
 	 public Class getDutyClass() {
    	 	return NForTaxonWithTree.class;
    	 }
  	public String getDutyName() {
  		return "Number for Taxon using Tree";
    	 }
 
	public abstract void calculateNumbers(Taxa taxa, Tree tree, NumberArray results, MesquiteString resultString);

}


