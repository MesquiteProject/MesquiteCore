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
/**
This class of modules supplies character matrices for use in calculation routines.  See its superclass
CharMatrixObedSource for methods.  
A Taxa object must be passed to the class's methods because
the matrices that are relevant may be Taxa specific.  For instance, only some of the available data matrices
in a file might apply to a given set of taxa, or a matrix may consist of all possible binary character 
distributions for a given set of taxa.<p>
This class assumes there is a current matrix that is active, and
available on request.
<p>
This class of modules is a simple subclass of CharMatrixObedSource.  The only reason for the distinction is to allow a module to
know why it was hired.  A CharMatrixObedSource is not to take control of which matrix is current; it is to allow its
employers complete control.  Thus a CharMatrixObedSource should not add menu items for which data set is to be used.
On the other hand, a CharMatrixSource can have such menu items and force a change to the next matrix.
*/

public abstract class CharMatrixSource extends CharMatrixOneSource  {

   	 public Class getDutyClass() {
   	 	return CharMatrixSource.class;
   	 }
 	public String getDutyName() {
 		return "Character Matrix Source";
   	 }
   	 public String[] getDefaultModule() {
   	 	return new String[] {"#StoredMatrices", "#SimulatedMatrix"};
   	 }
}


