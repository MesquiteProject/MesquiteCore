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
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.TaxaSelectionSet;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeVector;


/* ======================================================================== */
/**Searchers to find trees that optimize something.*/

public abstract class TreeSearcherFromMatrix extends TreeSearcher  {

	public Class getDutyClass() {
		return TreeSearcherFromMatrix.class;
	}
	public String getDutyName() {
		return "Tree Searcher From Matrix";
	}

	//outsideMatrix
	// optional, in case employer wants to force use of a matrix
	MatrixSourceCoord matrixSourceImposed = null;
	public MatrixSourceCoord getMatrixSource() {
		return matrixSourceImposed;
	}
	public void setMatrixSource(MatrixSourceCoord msource) {
		this.matrixSourceImposed = msource;
	}
	public abstract Class getCharacterClass();

}


