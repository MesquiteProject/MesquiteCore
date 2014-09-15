/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.parsimony.lib;

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;

/* ======================================================================== */
/** Sublcass of models of character evolution for parsimony calculations.  It is expected
that subclasses of this will be specialized for different classes of data (categorical, continuous, etc.)*/
public abstract class ParsimonyModel extends WholeCharacterModel {
	public ParsimonyModel (String name, Class stateClass) {
		super(name, stateClass);
	}
	public String getTypeName(){
		return "Parsimony model";
	}
	public String getParadigm(){
		return "Parsimony";
	}
	/** returns nexus command introducing this model (e.g. "USERTYPE" or "CHARMODEL")*/
	public String getNEXUSCommand() {
		return "USERTYPE";
	}
	public String getModelTypeName(){
		return "Parsimony model";
	}
}

