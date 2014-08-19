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

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;

/** A class that contains information regarding submodels of stochastic models of character evolution . */
/* ======================================================================== */
public class SubmodelInfo extends FileElement {
	Class c;
	String explanation;
	public SubmodelInfo (Class c, String name, String explanation) {
		this.c = c;
		setName(name);
		this.explanation = explanation;
 	}
 	public String getExplanation(){
 		return explanation;
 	}
 	public Class getContainedClass(){
 		return c;
 	}
 	public boolean containsSubclassOf(Class queryClass){
 		if (queryClass == null)
 			return false;
 		return queryClass.isAssignableFrom(c);
 	}
 	public Object makeModel(){
 		if (c == null)
 			return null;
		try {
			Object s =c.newInstance();
			return s;
		}
		catch (IllegalAccessException e){
			MesquiteTrunk.mesquiteTrunk.alert("iae smi");
			e.printStackTrace(); 
		}
		catch (InstantiationException e){
			MesquiteTrunk.mesquiteTrunk.alert("ie smi");
			e.printStackTrace(); 
		}
		return null;
 		
 	}
}

