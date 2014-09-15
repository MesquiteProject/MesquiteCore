/* Mesquite (package mesquite.ornamental).  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.tol.SearchToLTaxon;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.tol.lib.*;

/* ======================================================================== */
public class SearchToLTaxon extends BaseSearchToLTaxon {
	/*.................................................................................................................*/
	public String getBaseURLForUser() {
		return "tolweb.org";
	}
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		SearchToLTaxonToolExtra newPj = new SearchToLTaxonToolExtra(this, treeDisplay);
		if (extras!=null)
			extras.addElement(newPj);
		return newPj;
	}

}

/* ======================================================================== */
class SearchToLTaxonToolExtra extends BaseSearchToLToolTaxonExtra  {
	public SearchToLTaxonToolExtra (SearchToLTaxon ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule,treeDisplay);
	}
	/*.................................................................................................................*/
	public  String getToolName() {
		return "Go To ToL";
	}
	
	public String getToolExplanation() {
		return "This tool downloads the tree from the page of the Tree of Life Web Project for the taxon touched.";
	}

	/*.................................................................................................................*/
	public  String getToolScriptName() {
		return "goToToLTaxon";
	}

	/*.................................................................................................................*/
	public String getBaseURL() {
		return "tolweb.org";
	}
	/*.................................................................................................................*/
	public String getGetToLTreeModuleName() {
		return "GetToLTree";
	}
	/*.................................................................................................................*/
	public String getBaseURLForUser() {
		return "tolweb.org";
	}

}



