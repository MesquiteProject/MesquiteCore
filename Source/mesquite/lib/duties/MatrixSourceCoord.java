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
import mesquite.lib.characters.*;


/* ======================================================================== */
/**
*/

public abstract class MatrixSourceCoord extends MesquiteModule  {

   	 public Class getDutyClass() {
   	 	return MatrixSourceCoord.class;
   	 }
 	public String getDutyName() {
 		return "Independent Character Matrix Source Coordinator";
   	 }
   	 public String[] getDefaultModule() {
   	 	return new String[] {"#CharMatrixCoordObed"}; //, "#CharMatrixCoordIndep"};
   	 }
	 public String getFunctionIconPath(){
   		 return getRootImageDirectoryPath() + "functionIcons/matrixSource.gif";
   	 }

   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public abstract void initialize(Taxa taxa);

   	/** gets the current matrix.*/
   	public abstract MCharactersDistribution getCurrentMatrix(Taxa taxa);

        /* -- the following are a preliminary attempt to allow matrix sources to know exactly what tree the 
        matrix will be used with.  These can be overridden by modules to do simulations on the trees, or to see if there
        is a simulated matrix attached to the tree, or what ---*/
   	public void initialize(Tree tree){
   		if (tree==null) return;
   		else initialize(tree.getTaxa());
   	}

   	/** If this matrix source does in fact depend on the tree (i.e. it overrides the methods being passed a tree)
   	then this method should be overridden to return true.  This allows modules using it to know they should
   	re-request a matrix if the tree has changed.  (Relying on TreeContext or listening systems could be
   	too cumbersome since such requests could come in tight loops that are cycling through trees .*/
   	public abstract boolean usesTree();
   	
   	/** gets the current matrix.*/
   	public MCharactersDistribution getCurrentMatrix(Tree tree){
   		if (tree==null) return null;
   		else return getCurrentMatrix(tree.getTaxa());
   	}
	/*.................................................................................................................*/
   	public abstract String getCurrentMatrixName(Taxa taxa);
}


