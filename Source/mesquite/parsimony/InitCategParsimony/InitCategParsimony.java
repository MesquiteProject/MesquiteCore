/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.parsimony.InitCategParsimony;
/*~~  */

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;

import mesquite.lib.duties.*;
import mesquite.parsimony.lib.*;
import mesquite.categ.lib.*;

public class InitCategParsimony extends FileInit {
	public String getName() {
		return "Initialize predefined categorical parsimony models";
	}
	public String getExplanation() {
		return "Initializes the predefined categorical parsimony models." ;
	}
	/*.................................................................................................................*/
	CharacterModel unorderedModel,orderedModel,irreversibleModel, dolloModel;

	public boolean startJob(String arguments, Object condition, boolean hiredByName){

		unorderedModel = new UnorderedModel();
		orderedModel = new OrderedModel();
		irreversibleModel = new IrreversibleModel();
		dolloModel = new DolloModel();
		CategoricalData.registerDefaultModel("Parsimony", "Unordered");
		DNAData.registerDefaultModel("Parsimony", "Unordered");
		return true;
	}
	/*.................................................................................................................*/
	/** A method called immediately after the file has been established but not yet read in.*/
	public void projectEstablished() {
		unorderedModel.addToFile(null, getProject(), null);
		orderedModel.addToFile(null, getProject(), null);
		irreversibleModel.addToFile(null, getProject(), null);
		dolloModel.addToFile(null, getProject(), null);
		super.projectEstablished();
	}

	/*.................................................................................................................*/
	public boolean isSubstantive() {
		return false;
	}

}


