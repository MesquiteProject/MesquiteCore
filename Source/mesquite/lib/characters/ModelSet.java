/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.characters; 

import java.awt.*;
import mesquite.lib.duties.*;
import mesquite.lib.*;

/* ======================================================================== */
/**A ModelSet is a specification of which character model applies to each character (TYPESET in NEXUS 1 file format).
In Mesquite, character models can be used by parsimony, MLE, etc. calculations.  Different calculations paradigms
probably will use different models (though not necessarily), and thus parsimony would want to use one current set of models,
while MLE would want to use another ModelSet, and so on.  Because paradigms are not formally defined in
Mesquite, the currently active ModelSet that applies to a paradigm is requested by modules according to agreed-upon
names.  Thus, a parsimony procedure would request a character's
model in the current ModelSet.  If such a set exists, it would be used by the parsimony
procedure, otherwise default would be used.  (There would also be a set "Current Parsimony Weights". )
This will allow multiple defaults, so that for instance, MLE would be using one set of models and parsimony
another.  */

public abstract class ModelSet  extends CharObjectSpecsSet {
	
	public ModelSet (String name, int numChars, CharacterModel defaultModel, CharacterData data) {
		super(name, numChars, defaultModel, data);
	}
	public String getTypeName(){
		return "Model Set";
	}

 	/*.................................................................................................................*/
 	/** gets storage for set of properties*/
	public Object[] getNewPropertyStorage(int numParts){
		return new CharacterModel[numParts];
	}
 	/*.................................................................................................................*/
 	/** Sets model of character*/
	public void setModel(CharacterModel model, int index) {
		setDirty(true);
		setProperty(model, index);
	}
 	/*.................................................................................................................*/
 	/** return array of character models (can also get indivitually using getModel)*/
	public CharacterModel[] getModels() {
		Object p = getProperties();
		if (p==null)
			return null;
		else
			return (CharacterModel[])p;
	}
 	/*.................................................................................................................*/
 	/** returns model for specified character*/
	public CharacterModel getModel(int index) {
		return (CharacterModel)getProperty(index);
	}
 	/*.................................................................................................................*/
 	/** Gets default model specified for ModelSet*/
	public CharacterModel getDefaultModel() {
		return (CharacterModel)getDefaultProperty();
	}
	/*.................................................................................................................*/
 	/** Gets default model specified for ModelSet*/
	public CharacterModel getDefaultModel(int ic) {
		return getDefaultModel();
	}
	/*.................................................................................................................*/
 	/** sets default model*/
	public void setDefaultModel(CharacterModel defaultModel) {
		setDirty(true);
		setDefaultProperty(defaultModel);
	}
 	/*.................................................................................................................*/
 	public String toString(){
		String s = "Model set " + getName() + ": ";
		for (int i=0; i<getNumberOfParts(); i++) {
			s +=  getModel(i);
		}
		return s;
 	}
}

