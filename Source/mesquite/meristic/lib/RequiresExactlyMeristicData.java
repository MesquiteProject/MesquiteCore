package mesquite.meristic.lib;

import mesquite.lib.CompatibilityTest;
import mesquite.lib.EmployerEmployee;
import mesquite.lib.MesquiteProject;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.CharacterState;

/* ======================================================================== */
/** An object a module can create and pass back to store in module info.  Tests whether module will be compatible with
passed object.  Classes of modules will have known ways of responding to particular classes of objects, e.g. character sources
should test whether they can handle given CharacterState types.*/
public class RequiresExactlyMeristicData extends CompatibilityTest {
	public boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer){
		if (obj == null)
			return true;
		if (obj instanceof RequiresExactlyMeristicData)
			return true;
		if (obj instanceof CompatibilityTest)
			return false;
		if (!(obj instanceof Class))
			return true;
		if (!CharacterState.class.isAssignableFrom((Class)obj) &&  !CharacterData.class.isAssignableFrom((Class)obj))
			return true;
		return  (MeristicState.class == obj) || MeristicState.class == obj;
	}
	public Class getAcceptedClass(){
		return MeristicState.class;
	}
}
