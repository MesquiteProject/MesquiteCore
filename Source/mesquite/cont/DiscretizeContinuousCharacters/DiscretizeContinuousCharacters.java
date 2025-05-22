/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.cont.DiscretizeContinuousCharacters;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.ui.QueryDialogs;
import mesquite.parsimony.lib.ParsimonyModel;
import mesquite.parsimony.lib.ParsimonyModelSet;
import mesquite.cont.lib.*;
import mesquite.categ.lib.*;

/* ======================================================================== */
public class DiscretizeContinuousCharacters extends ContDataUtility {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 275;  
	}
	/*.................................................................................................................*/
	static String  bstring  = "";
	double[] getBins(){
		MesquiteString s = new MesquiteString(bstring);
		if (!QueryDialogs.queryString(containerOfModule(), "Bins for Discretization", 
					"Indicate boundaries of bins for discretization as a series of one or more numbers, separated by spaces.  " +
					"For instance, if you enter simply the number 5.2, then all values below 5.2 will be given state 0, all values at or above 5.2 will be given state 1. "  +
					"If you enter more than one number, in increasing value, then the character will be discretized as a multistate ordered character/ " +
					"For instance, if you enter \"0.7  2.3  4.1\", then values below 0.7 will be given state 0, values at or above 0.7 and below 2.3 will be given state 1, " +
					"values at or above 2.3 and below 4.1 will be given state 2, and values at or above 4.1 will be given state 3," , s, 5))
			return null;
		bstring = s.getValue();
		if (StringUtil.blank(bstring))
			return null;
		MesquiteInteger pos = new MesquiteInteger(0);
		double d = 0;
		Vector v = new Vector();
		while (MesquiteDouble.isCombinable(d = MesquiteDouble.fromString (bstring, pos))) {
			v.addElement(new MesquiteDouble(d));
		}
		if (v.size() == 0)
			return null;
		double[] bins = new double[v.size()];
		for (int i = 0; i<v.size(); i++){
			bins[i] = ((MesquiteDouble)v.elementAt(i)).getValue();
		}

		for (int i = 1; i<bins.length; i++){
			if (bins[i]<= bins[i-1]){
				alert("Boundaries between bins must be listed in strictly increasing order");
				return null;
			}
		}

		return bins;
	}

	int findBin(double value, double[] bins){
		for (int i= 0; i< bins.length; i++){
			if (bins[i] > value)
				return i;
		}
		return bins.length;
	}
	/*.................................................................................................................*/
/** Called to alter data in all cells*/
	public boolean operateOnData(CharacterData data){
		if (!(data instanceof ContinuousData))
			return false;
		ContinuousData cont = (ContinuousData)data;
		if (!cont.anySelected()){
			alert("Characters to be discretized must first be selected in the matrix");
			return false;
		}

		if (getProject().getNumberCharMatricesVisible(data.getTaxa(), CategoricalState.class)==0){
			alert("There must be an available categorical matrix to which discretized characters can be added.");
			return false;
		}
		CategoricalData cat =  (CategoricalData)getProject().chooseDataExactClass(containerOfModule(), null, data.getTaxa(), CategoricalState.class, "Choose matrix to which to add discretized character(s)", false, null, null);
		if (cat == null)
			return false;
		
		
		//now ask for bins
		double[] bins = getBins();
		if (bins == null)
			return false;
		if (bins.length > 50){
			alert("Sorry, at most 50 bin boundaries are allowed");
			return false;
		}
		CharacterModel ordered = getProject().getCharacterModel(new ModelCompatibilityInfo(ParsimonyModel.class, CategoricalState.class), "ordered");
		ParsimonyModelSet modelSet = (ParsimonyModelSet)cat.getCurrentSpecsSet(ParsimonyModelSet.class);
		if (ordered!=null){
			if (modelSet == null) {
				CharacterModel defaultModel =  cat.getDefaultModel("Parsimony");
				modelSet= new ParsimonyModelSet("Parsimony Model Set", cat.getNumChars(), defaultModel, cat);
				cat.storeSpecsSet(modelSet, ParsimonyModelSet.class);
				cat.setCurrentSpecsSet(modelSet, ParsimonyModelSet.class);
				modelSet.addToFile(cat.getFile(), getProject(), findElementManager(ParsimonyModelSet.class)); 
			}
		}
		int numAdded = cont.numberSelected();
		int origNumCat = cat.getNumChars();
		cat.addCharacters(origNumCat, numAdded, true);
		
		
		int count = 0;
		for (int ic = 0; ic<cont.getNumChars(); ic++){
			if (cont.getSelected(ic)){
				int icCat = origNumCat + count;
				String name = "Discretized from character " + (ic+1);
				if (cont.characterHasName(ic))
					name += " (" + cont.getCharacterName(ic) + ")";
				String binsString = "Bin boundaries:";
				for (int ib = 0; ib<bins.length; ib++)
					binsString += " " + MesquiteDouble.toStringDigitsSpecified(bins[ib], 4);
				cat.setAnnotation(icCat, binsString);
				
				cat.setCharacterName(icCat, name);
				int max = 1;
				count++;
				for (int it = 0; it<cont.getNumTaxa(); it++){
					double value = cont.getState(ic, it, 0);
					if (ContinuousState.isUnassigned(value))
							cat.setState(icCat, it, CategoricalState.unassigned);
					else if (ContinuousState.isInapplicable(value))
							cat.setState(icCat, it, CategoricalState.inapplicable);
					else {
						int bin = findBin(value, bins);
						if (bin > max)
							max = bin;
						cat.setState(icCat, it, CategoricalState.makeSet(bin));
					}
				}
				if (max>1){
					if (modelSet != null)
						modelSet.setModel(ordered, icCat);
				}
			}
		}
		
		cat.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED, null, null));
		cat.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED, null, null));
		cat.notifyInLinked(new Notification(MesquiteListener.PARTS_ADDED, null, null));
		return true;
	}
	
	
	/*.................................................................................................................*/
	public String getName() {
		return "Discretize Selected Characters...";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Bins the selected continuous characters to discretize them into categorical characters (ordered if more than two states), and adds them to an existing categorical matrix.  If the continuous matrix is multi-item, only the first item is used." ;
	}

}


