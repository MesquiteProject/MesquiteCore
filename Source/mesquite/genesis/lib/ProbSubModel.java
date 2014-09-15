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

/** A class that provides for a site-to-site rate variation model. */
/* ======================================================================== */
public abstract class ProbSubModel extends CharacterSubmodel {
	CompositProbCategModel probabilityModel;
	int defaultNumChars = 0;

	private CharacterStatesHolder charDistribution = null;
	private MCharactersStatesHolder charMatrix = null;

 	/*.................................................................................................................*/
	public ProbSubModel (String name, Class stateClass) {
		super(name,stateClass);
	}
 	/*.................................................................................................................*/
	public abstract void initialize();
 	/*.................................................................................................................*/
	public int getDefaultNumChars(){
		return defaultNumChars;
	}
 	/*.................................................................................................................*/
	public void setDefaultNumChars(int numChars){
		defaultNumChars = numChars;
	}
 	/*.................................................................................................................*/
	public abstract void taxaSet() ;
 	/*.................................................................................................................*/
	public void setCompositProbCategModel (CompositProbCategModel probabilityModel) {
		this.probabilityModel = probabilityModel;
	}
 	/*.................................................................................................................*/
	public boolean isLineageSpecific(){
		return false;
	}
 	/*.................................................................................................................*/
 	/** Should be overridden to return true if submodel needs an empirical matrix*/
	public boolean needsEmpirical(){
		return false;
	}
 	/*.................................................................................................................*/
	public void addModelOptions(ExtensibleDialog dialog) {
		//PopUpPanelOfCards cardPanels = dialog.addPopUpPanelOfCards();

		addOptions(dialog);
		//cardPanels.finalizeCards();
	}
	/*.................................................................................................................*/
	public abstract CharacterModel cloneModelWithMotherLink(CharacterModel formerClone);
  	/*.................................................................................................................*/
	public void recoverModelOptions() {
		recoverOptions();
	}
	/*.................................................................................................................*/
	public abstract void addOptions(ExtensibleDialog dialog);
 	/*.................................................................................................................*/
	public abstract boolean recoverOptions();
 	/*.................................................................................................................*/
	public abstract boolean checkOptions();
 	/*.................................................................................................................*/
	public abstract String checkOptionsReport();
 	/*.................................................................................................................*/
	public abstract void setOptions();
 	/*.................................................................................................................*/
	public void setSeed(long seed){
	}
	/** Returns whether parameters of model are fully specified.  If so, it can be used for evolving states.*/
	public abstract boolean isFullySpecified();
	
	/** Sets the character distribution or history to which this model is currently applying */
	public void setCharacterDistribution (CharacterStatesHolder cd){
		charDistribution = cd;
	}
	public CharacterStatesHolder getCharacterDistribution (){
		return charDistribution;
	}
	/** To be overridden if processing needs to happen after CharMatrix is set */
	public void recalcAfterSetMCharactersStatesHolder (){
	}
	/** Sets the character matrix or  history to which this model is currently applying */
	public void setMCharactersStatesHolder (MCharactersStatesHolder cd){
		charMatrix = cd;
		recalcAfterSetMCharactersStatesHolder();
	}
	public MCharactersStatesHolder getMCharactersStatesHolder (){
		return charMatrix;
	}
	public String toString(){
		return super.toString() + "; Fully specified: " + isFullySpecified();
	}
 }

