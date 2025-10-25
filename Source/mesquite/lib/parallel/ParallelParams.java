/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.parallel;

import mesquite.lib.MesquiteModule;

/* ======================================================================== */
/** */
public class ParallelParams {
	public MesquiteModule responsibleEmployer;
	public MesquiteModule[] employees; //employees hired just for one thread
	public Object[] ownerObjects; //For various objects, e.g. the base set of values to compare with those from each item. Could also contain parameter settings for item, but owner Parallelizable can simply use constant params throughout
	public Object[] threadObjects; //For various objects the thread wants to keep distinct for it, e.g. a tree that that thread object is using

}


