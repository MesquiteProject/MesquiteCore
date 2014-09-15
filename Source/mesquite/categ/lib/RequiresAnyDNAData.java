/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.categ.lib;

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;

/* ======================================================================== */
/** An object a module can create and pass back to store in module info.  Tests whether module will be compatible with
passed object.  Classes of modules will have known ways of responding to particular classes of objects, e.g. character sources
should test whether they can handle given CharacterState types.

This version says "I need to receive DNA data".  This is different from OffersDNAState, which is used for sources to say that is what they can offer*/
public class RequiresAnyDNAData extends CompatibilityTest {  
	public boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer){
		if (obj == null)
			return true;
		if (obj instanceof RequiresAnyDNAData)
			return true;
		if (obj instanceof CompatibilityTest)
			return false;
		if (!(obj instanceof Class))
			return true;
		if (!CharacterState.class.isAssignableFrom((Class)obj) && !CharacterData.class.isAssignableFrom((Class)obj))
			return true;
	//	return  obj == DNAData.class || obj == DNAState.class || obj == RNAData.class || obj == RNAState.class; 
return (((DNAData.class).isAssignableFrom((Class)obj)) || ((DNAState.class).isAssignableFrom((Class)obj)));
	//	return  (((DNAData.class).isAssignableFrom((Class)obj)) || ((DNAState.class).isAssignableFrom((Class)obj)));
		//To pass the test, the datatype MUST be a DNA or a subclass of DNA
	}
	public Class getAcceptedClass(){
		return DNAState.class;
	}
}

