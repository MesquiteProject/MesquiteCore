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

import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import java.lang.Math.*;

/** A class that provides for a site-to-site rate variation model. */
/* ======================================================================== */
public class CharRatesInvar extends CharRatesModel {
	Random randomNumber;
	double pInvar = MesquiteDouble.unassigned;
	double pInvarTemp = MesquiteDouble.unassigned;
	DoubleField pInvarField;
	String errorMessage="";

	public CharRatesInvar (double pInvar) {
		super();
		this.pInvar = pInvar;
		randomNumber = new Random();
		setNewRate();
 	}
	public CharRatesInvar () {
		super();
		randomNumber = new Random();
		setNewRate();
 	}

 	/*.................................................................................................................*/
	public void setSeed(long seed){
		randomNumber.setSeed(seed);
	}
 	/*.................................................................................................................*/
	public CharacterModel cloneModelWithMotherLink(CharacterModel formerClone){
		CharRatesInvar model = new CharRatesInvar(pInvar);
		completeDaughterClone(formerClone, model);
		return model;
	}
 	/* copy information from this to model passed (used in cloneModelWithMotherLink to ensure that superclass info is copied); should call super.copyToClone(pm) */
	public void copyToClone(CharacterModel pm){
		if (pm == null)
			return;
		if (pm instanceof CharRatesInvar) {
			CharRatesInvar gi = (CharRatesInvar) pm;
			gi.pInvar = pInvar;
			gi.setNewRate();
		}
		super.copyToClone(pm);
	}
	/*.................................................................................................................*/
	public void setNewRate() {
		if (pInvar != MesquiteDouble.unassigned)
			if (randomNumber.nextDouble()<=pInvar)
				setRate(0.0);
			else
				setRate(1.0/(1-pInvar));
	}
	/*.................................................................................................................*/
	public void addOptions(ExtensibleDialog dialog) {
		pInvarField = dialog.addDoubleField("proportion invariant:",pInvar, 10);
	}
 	/*.................................................................................................................*/
	public boolean recoverOptions() {
		pInvarTemp = pInvarField.getValue();
		return true;
	}
 	/*.................................................................................................................*/
	public boolean checkOptions() {
		errorMessage = "";
		pInvarTemp = pInvarField.getValue();
		if (!MesquiteDouble.isCombinable(pInvarTemp)) {
			errorMessage = "The proportion of invariant characters is not valid.";
			return false;
		}
		if (pInvarTemp<0.0 || pInvarTemp>1.0) {
			errorMessage = "The proportion of invariant characters must be between 0.0 and 1.0";
			return false;
		}
		return true;
	}
	/*.................................................................................................................*/
	public String checkOptionsReport() {
		if (!checkOptions())
			return errorMessage;
		return "";
	}
	/*.................................................................................................................*/
	public void setOptions() {
		setPInvar(pInvarTemp);
		notifyListeners(this, new Notification(MesquiteListener.UNKNOWN));
	}
 	/*.................................................................................................................*/
	public boolean isFullySpecified(){
		return (pInvar != MesquiteDouble.unassigned);
	}
 	/*.................................................................................................................*/
	public void setPInvar (double pInvar) {
		this.pInvar = pInvar;
		setNewRate();
 	}
 	/*.................................................................................................................*/
	public double getPInvar () {
		return pInvar;
 	}

 	/*.................................................................................................................*/
	public void initForNextCharacter(){
		setNewRate();
	}

	/*.................................................................................................................*/
	/** returns speficiations*/
	public String getNexusSpecification() {
		return "pInvar = " + MesquiteDouble.toString(pInvar);
	}
	/*.................................................................................................................*/
	/** reads parameters from string (same format as written by "toSTring"*/
	public void fromString(String description, MesquiteInteger stringPos, int format) {
		ParseUtil.getToken(description, stringPos);
		ParseUtil.getToken(description, stringPos);
		String s = ParseUtil.getToken(description, stringPos);
   		pInvar = MesquiteDouble.fromString(s);
	}
	/*.................................................................................................................*/
	/** returns name of model class (e.g. "stepmatrix")*/
	public String getNEXUSClassName() {
		return "pInvar";
	}
 	/*.................................................................................................................*/
	/** returns parameters of the model. */
	public String getParameters (){
		return "proportion of characters invariant = "+pInvar;
	}
}

