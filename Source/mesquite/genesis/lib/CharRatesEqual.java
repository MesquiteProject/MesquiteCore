/* Mesquite source code (Genesis package).  Copyright 2001-2010 D. Maddison and W. Maddison. 
Version 2.74, October 2010.
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

/** A class that provides for a site-to-site rate variation model. */
/* ======================================================================== */
public class CharRatesEqual extends CharRatesModel {

	/*.................................................................................................................*/
	public double getRate (int ic) {
	/*	if (ic % 3 == 0)
			return 1.0;
		else
	*/		return 1.0;
	}
	/*.................................................................................................................*/
	public String getNexusSpecification(){
		return "";
	}
 	/*.................................................................................................................*/
	public CharacterModel cloneModelWithMotherLink(CharacterModel formerClone){
		CharRatesEqual model = new CharRatesEqual();
		completeDaughterClone(formerClone, model);
		return model;
	}
 	/*.................................................................................................................*/
	public void initForNextCharacter(){
	}
	public boolean isFullySpecified(){
		return true;
	}
	/** returns whether model is built in*/
	public boolean isBuiltIn() {
		return true;
	}
	/*.................................................................................................................*/
	public void addOptions(ExtensibleDialog dialog) {}
 	/*.................................................................................................................*/
	public boolean recoverOptions() {return true;}
 	/*.................................................................................................................*/
	public boolean checkOptions() {
		return true;
	}
	/*.................................................................................................................*/
	public String checkOptionsReport() {
		return "";
	}
 	/*.................................................................................................................*/
	public void setOptions() {}
 	/*.................................................................................................................*/
	/** returns name of model*/
	public String getName() {
		return "Equal Rates";
	}
 	/*.................................................................................................................*/
	/** returns name of model*/
	public String getNEXUSName() {
		return "equal";
	}
 	/*.................................................................................................................*/
	/** returns parameters of the model. */
	public String getParameters (){
		return "Equal Rates";
	}
}

