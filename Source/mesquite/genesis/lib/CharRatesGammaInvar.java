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

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import java.lang.Math.*;
import JSci.maths.statistics.*;
import JSci.maths.*;

/** A class that provides for a site-to-site rate variation model using the gamma distribution. */
/* ======================================================================== */
public class CharRatesGammaInvar extends CharRatesModel {
	Random randomNumber;

	double pInvar = MesquiteDouble.unassigned;
	double pInvarTemp = MesquiteDouble.unassigned;
	DoubleField pInvarField;
	
	double shape = MesquiteDouble.unassigned;
	double shapeTemp=MesquiteDouble.unassigned;
	DoubleField shapeField;
	boolean isDiscrete = true;
	boolean isDiscreteTemp;
	Checkbox isDiscreteCheckBox;
	int numCategories=4;  
	int numCategoriesTemp;
	IntegerField numCategoriesField; 
	GammaDistribution gammaDist; 
	double increment;
	String errorMessage="";
	
	double[] categoryBoundaries = null;
	double[] categoryRates = null;
		

	public CharRatesGammaInvar (double pInvar, double shape, boolean isDiscrete, int numCategories) {
		super();
		this.pInvar = pInvar;
		this.shape = shape;
		this.numCategories = numCategories; 
		increment = 1.0/numCategories;
		this.isDiscrete = isDiscrete;
		randomNumber = new Random();
		gammaDist = new GammaDistribution(shape); 
		setShape(shape);
		setRate(sampleGammaInvar());
 	}
	public CharRatesGammaInvar () {
		super();
		randomNumber = new Random();
		gammaDist = new GammaDistribution(0.5); 
		//setRate(RndGamma(shape));
 	}

	/*.................................................................................................................*/
	public CharacterModel cloneModelWithMotherLink(CharacterModel formerClone){
		CharRatesGammaInvar model = new CharRatesGammaInvar(pInvar, shape, isDiscrete, numCategories);
		completeDaughterClone(formerClone, model);
		return model;
	}
 	/* copy information from this to model passed (used in cloneModelWithMotherLink to ensure that superclass info is copied); should call super.copyToClone(pm) */
	public void copyToClone(CharacterModel pm){
		if (pm == null)
			return;
		if (pm instanceof CharRatesGammaInvar) {
			CharRatesGammaInvar gi = (CharRatesGammaInvar) pm;
			gi.pInvar = pInvar;
			gi.shape = shape;
			gi.isDiscrete = isDiscrete;
			gi.setShape(shape);
		}
		super.copyToClone(pm);
	}
	/*.................................................................................................................*/
	/** returns specifications*/
	public String getNexusSpecification() {
		String s = "pInvar = " + MesquiteDouble.toString(pInvar);
		s += " shape = " + MesquiteDouble.toString(shape);
		if (isDiscrete) {
			s += " discrete categories = "+numCategories;
		}
		else
			s += "continuous";
		return s;
	}
 	/*.................................................................................................................*/
	public void setSeed(long seed){
		randomNumber.setSeed(seed);
	}
	/*.................................................................................................................*/
	/** reads parameters from string (same format as written by "toString")*/
	public void fromString(String description, MesquiteInteger stringPos, int format) {
		String s = ParseUtil.getToken(description, stringPos);
		while (!StringUtil.blank(s)) {
			if (s.equalsIgnoreCase("shape")) {
				ParseUtil.getToken(description, stringPos);  // =
				s = ParseUtil.getToken(description, stringPos);
   				shape = MesquiteDouble.fromString(s);
			}
			else if (s.equalsIgnoreCase("categories")) {
				ParseUtil.getToken(description, stringPos);  // =
				s = ParseUtil.getToken(description, stringPos);
   				numCategories = MesquiteInteger.fromString(s);
   				increment = 1.0/numCategories;
			}
			else if (s.equalsIgnoreCase("pInvar")) {
				ParseUtil.getToken(description, stringPos);  // =
				s = ParseUtil.getToken(description, stringPos);
   				pInvar = MesquiteDouble.fromString(s);
			}
			else if (s.equalsIgnoreCase("discrete")) {
				isDiscrete = true;
			}
			else if (s.equalsIgnoreCase("continuous")) {
				isDiscrete = false;
			}
			s = ParseUtil.getToken(description, stringPos);
		}

	}
	/*.................................................................................................................*/
	/** returns name of model class (e.g. "stepmatrix")*/
	public String getNEXUSClassName() {
		return "GammaInvar";
	}
	/*.................................................................................................................*/
	/** adds items to extensible dialog box used for setting parameter values*/
	public void addOptions(ExtensibleDialog dialog) {
		pInvarField = dialog.addDoubleField("proportion invariable:",pInvar, 10);
		shapeField = dialog.addDoubleField("gamma shape parameter:",shape, 10);
		isDiscreteCheckBox = dialog.addCheckBox("discrete gamma", isDiscrete);
		numCategoriesField = dialog.addIntegerField("number of categories:",numCategories, 4);
	}
 	/*.................................................................................................................*/
	/** recovers values from items in extensible dialog box used for setting parameter values*/
	public boolean recoverOptions() {
		pInvarTemp = pInvarField.getValue();
		shapeTemp = shapeField.getValue();
		isDiscreteTemp = isDiscreteCheckBox.getState();
		numCategoriesTemp = numCategoriesField.getValue();
		return true;
	}
 	/*.................................................................................................................*/
	public boolean checkOptions() {
		errorMessage = "";
		pInvarTemp = pInvarField.getValue();
		shapeTemp = shapeField.getValue();
		isDiscreteTemp = isDiscreteCheckBox.getState();
		numCategoriesTemp = numCategoriesField.getValue();
		if (!MesquiteDouble.isCombinable(pInvarTemp)) {
			errorMessage = "The proportion of invariable characters is not valid.";
			return false;
		}
		if (pInvarTemp<0.0 || pInvarTemp>1.0) {
			errorMessage = "The proportion of invariable characters must be between 0.0 and 1.0";
			return false;
		}
		if (!MesquiteDouble.isCombinable(shapeTemp)) {
			errorMessage = "The shape parameter is not valid.";
			return false;
		}
		if (shapeTemp<=0.0) {
			errorMessage = "The shape parameter must be greater than 0.0";
			return false;
		}
		if (isDiscreteTemp && !MesquiteInteger.isCombinable(numCategoriesTemp)) {
			errorMessage = "The number of categories must be entered if a discrete gamma is chosen.";
			return false;
		}
		if (isDiscreteTemp && !numCategoriesField.isValidInteger()) {
			errorMessage = "The number of categories must be an integer.";
			return false;
		}
		if (isDiscreteTemp && (numCategoriesTemp<=1)) {
			errorMessage = "The number of categories must be greater than 1";
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
	/** moves parameter values from temporary storage (where they were put from dialog box) into permanent storage.*/
	public void setOptions() {
		isDiscrete = isDiscreteTemp;
		numCategories = numCategoriesTemp;
		increment = 1.0/numCategories;
		pInvar = pInvarTemp;
		setShape(shapeTemp);
		notifyListeners(this, new Notification(MesquiteListener.UNKNOWN));
	}
 	/*.................................................................................................................*/
	public boolean isFullySpecified(){
		return shape != MesquiteDouble.unassigned;
	}
 	/*.................................................................................................................*/
	/** Calculates mean value for a section of a gamma distribution.*/
	public double meanRate(double lowerBoundary, double upperBoundary, double increment){
		return (SpecialMath.incompleteGamma(shape+1,upperBoundary*shape)-SpecialMath.incompleteGamma(shape+1,lowerBoundary*shape))/increment;
	}
 	/*.................................................................................................................*/
	public void calculateDiscreteRates () {
		categoryBoundaries = new double[numCategories+1];
		categoryBoundaries[0]=0.0;
		categoryBoundaries[numCategories]=100000.0;
		for (int i=1; i<numCategories; i++) {
			categoryBoundaries[i] = gammaDist.inverse(i*increment)/shape;
		}
		categoryRates = new double[numCategories];
		for (int i=0; i<numCategories; i++) {
			categoryRates[i] = meanRate(categoryBoundaries[i],categoryBoundaries[i+1], increment);
		}
 	}
 	/*.................................................................................................................*/
	public void setShape (double shape) {
		this.shape = shape;
		gammaDist.setShapeParameter(shape);
		calculateDiscreteRates();
		setRate(sampleGammaInvar());
 	}
 	/*.................................................................................................................*/
	public double getShape() {
		return shape;
 	}
	/*.................................................................................................................*/
	double sampleGammaInvar () {
		
		if (pInvar != MesquiteDouble.unassigned)
			if (randomNumber.nextDouble()<=pInvar)
				return 0.0;
			else
				if (isDiscrete) {
					int category = (int)(randomNumber.nextDouble()/increment);
					return categoryRates[category]/(1-pInvar);  // divide by (1-pInvar) so that the average rate is 1.0
				}	
				else
					return ((gammaDist.inverse(randomNumber.nextDouble())/shape)/(1-pInvar));// divide by (1-pInvar) so that the average rate is 1.0
		return 1.0;
	}

 	/*.................................................................................................................*/
	public void initForNextCharacter(){
		double g = sampleGammaInvar();
		setRate(g);
	}

 	/*.................................................................................................................*/
	/** returns parameters of the model. */
	public String getParameters (){
		String s = "Proportion of invariable characters: "+ pInvar+"; the remainder follow a ";
		if (isDiscrete) {
			s += "discrete gamma distribution with shape parameter = " + shape + ", with " +numCategories + " categories.";
		}
		else
			s += "continuous gamma distribution with shape parameter " + shape + ".";
		return s;
	}


}

