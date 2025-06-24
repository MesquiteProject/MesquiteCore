/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/

package mesquite.lib;

/* ======================================================================== */
/** An object a module can create and pass back to store in module info.  This test is done by the prospective
 *  employee module.  It tests whether module will be compatible with
passed object.  Classes of modules will have known ways of responding to particular classes of objects, e.g. character sources
should test whether they can handle given CharacterState types.
(In contrast, a QualificationsTest is done by the employer, inspecting the prospective employee to see if it's OK)*/
public abstract class CompatibilityTest implements CompatibilityChecker {
	public abstract  boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer);
	public boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer, MesquiteString report){
		return isCompatible(obj, project, prospectiveEmployer);
	}
	/*----*/
	public Class getAcceptedClass() {
		return null;
	}
}



