/* Mesquite source code (Genesis package).  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.genesis.lib;

import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;


/* ======================================================================== */
/**
This class of modules curates a subclass of character models for use in calculation routines.*/

public abstract class RateMatrixCurator extends ProbSubModelCurator  {

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
  	 }
}


