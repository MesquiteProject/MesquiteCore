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
import java.util.*;

/** A class that provides for a site-to-site rate variation model. */
/* ======================================================================== */
public abstract class CharRatesModel extends ProbSubModel {
	double siteRate = 1.0;
	String name = "Name";
	Class stateClass=CategoricalState.class;

 	/*.................................................................................................................*/
	public CharRatesModel (String name, Class stateClass) {
		super(name, stateClass);
	}
 	/*.................................................................................................................*/
	public CharRatesModel () {
		super("Name", CategoricalState.class);
	}
	/** Returns nexus command introducing this model.*/
	//public String getNEXUSCommand() {
	//	return "CharRatesModel";
	//}
	//public String getTypeName(){
	//	return "Character rates model";
	//}
	public String getCapitalizedTypeName(){
		return "Character Rates Model";
	}
 	/*.................................................................................................................*/
	public void initialize() {
	}
 	/*.................................................................................................................*/
	public void taxaSet() {
	}
	/*.................................................................................................................*/
	public boolean checkValidityCharRates () {
		return true;
	}
	/*.................................................................................................................*/
	public double getRate (int ic) {
		if (!checkValidityCharRates())
			return 1.0;
		else
			return siteRate;
	}
	/*.................................................................................................................*/
	public double getRate () {
		return siteRate;
	}
	/*.................................................................................................................*/
	public void setRate (double newRate) {
		if (newRate!=MesquiteDouble.unassigned)
			siteRate = newRate;
	}
	/*.................................................................................................................*/
 	/*.................................................................................................................*/
	public abstract void initForNextCharacter();
 	/*.................................................................................................................*/
	public String getExplanation(){
		return "This specifies the model of rate variation across characters.";
	}
 	/*.................................................................................................................*/
	public String getParadigm(){
		return "CharRates";
	}
 	/*.................................................................................................................*/
	/** returns parameters of the model. */
	public String getParameters (){
		return "";
	}
 	public String getModelTypeName(){
		return "Rate variation model";
	}
}

