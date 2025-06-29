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

import mesquite.categ.lib.CategoricalState;



public class OrderedModel extends CategParsimonyModel{
	public OrderedModel(){
		super("Ordered", CategoricalState.class);
		setBuiltIn(true);
	}
	public String getNEXUSName(){
		return "ord";
	}
	/** return an explanation of the model. */
	public String getExplanation (){
		return "Cost of change from state i to stage j is |i-j|";
	}
	public String getNEXUSClassName(){
		return "Ordered";
	}
}

