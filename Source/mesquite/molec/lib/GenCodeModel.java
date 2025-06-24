/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.molec.lib;

import mesquite.lib.Priority;
import mesquite.lib.characters.WholeCharacterModel;

/* ======================================================================== */
/** Sublcass of models of character evolution for parsimony calculations.  It is expected
that subclasses of this will be specialized for different classes of data (categorical, continuous, etc.)*/
public abstract class GenCodeModel extends WholeCharacterModel implements Priority {
	GeneticCode geneticCode;
	public GenCodeModel (String name, Class stateClass) {
		super(name, stateClass);
	}
	public String getTypeName(){
		return "Genetic code model";
	}
	public String getParadigm(){
		return "GeneticCode";
	}
	/** returns nexus command introducing this model (e.g. "USERTYPE" or "CHARMODEL")*/
	public String getNEXUSCommand() {
		return "GENETICCODE";
	}
	public String getModelTypeName(){
		return "Genetic code model";
	}
	public GeneticCode getGeneticCode(){
		return geneticCode;
	}
	public String getNEXUSClassName(){
		return "builtInGeneticCode";
	}
	public int getPriority(){
		if (geneticCode!=null)
			return geneticCode.getNCBITranslationTableNumber();
		else
			return 0;
	}
	/** return an explanation of the model. */
	public String getExplanation (){
		if (geneticCode!=null)
			return geneticCode.getName();
		else
			return "A genetic code model.";
	}

	public String getNEXUSName(){
		if (geneticCode!=null)
			return geneticCode.getNEXUSName();
		else
			return "";
	}

}

