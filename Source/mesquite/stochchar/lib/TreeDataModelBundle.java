/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.stochchar.lib;

import mesquite.lib.*;
import mesquite.lib.characters.*;


/* ======================================================================== */

public class TreeDataModelBundle   {
	Tree tree;
	ProbabilityModel model;
	CharacterDistribution distribution;
	CLikelihoodCalculator calc;
	Object params; //a list of which parameters are to be estimated
	CommandRecord comRec;
	
	public TreeDataModelBundle(Tree t, ProbabilityModel m, CharacterDistribution d, Object params, CLikelihoodCalculator lc){
		tree = t;
		model = m;
		distribution = d;
		calc = lc;
		this.params = params;
		this.comRec = MesquiteThread.getCurrentCommandRecord();
	}
	public Tree getTree(){
		return tree;
	}
	public ProbabilityModel getProbabilityModel(){
		return model;
	}
	public CharacterDistribution getCharacterDistribution(){
		return distribution;
	}
	public CLikelihoodCalculator getLikelihoodCalculator(){
		return calc;
	}
	public Object getParams(){
		return params;
	}
	public CommandRecord getCommandRecord(){
		return comRec;
	}
}



