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

import mesquite.lib.AssociableWithSpecs;
import mesquite.lib.SpecsSet;

/*======================================================================== */
/** a ModelSet for probability models. */
public class ProbabilityModelSet  extends ModelSet {
	
	public ProbabilityModelSet (String name, int numChars, CharacterModel defaultModel, CharacterData data) {
		super(name, numChars, defaultModel, data);
	}
	public String getTypeName(){
		return "Probability Model set";
	}
	public SpecsSet cloneSpecsSet(){
		ProbabilityModelSet ms = new ProbabilityModelSet(new String(name), getNumberOfParts(), (CharacterModel)getDefaultProperty(), data);
		for (int i=0; i<getNumberOfParts(); i++) {
			ms.setModel(getModel(i), i);
		}
		return ms;
	}
	public SpecsSet makeSpecsSet(AssociableWithSpecs parent, int numParts){
		if (!(parent instanceof CharacterData))
			return null;
		return new ProbabilityModelSet("Model Set", numParts, (CharacterModel)getDefaultProperty(), (CharacterData)parent);
	}
	/*.................................................................................................................*/
 	/** Gets default model specified for ModelSet for character ic or new character added just after ic.  May take from current assignment*/
	public CharacterModel getDefaultModel(int ic) {
		return (CharacterModel)getDefaultProperty(ic);
	}
	public boolean hasDefaultProperty(int ic) {
		if (data != null)
			return getProperty(ic) == data.getDefaultModel("Likelihood");
		return super.hasDefaultProperty(ic);
	}


}

