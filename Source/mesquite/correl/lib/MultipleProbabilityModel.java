/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.correl.lib;

import mesquite.lib.CommandRecord;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.characters.CharacterModel;
import mesquite.lib.characters.CharacterState;
import mesquite.lib.characters.CharacterStatesHolder;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.characters.MCharactersStatesHolder;
import mesquite.lib.characters.ProbabilityModel;
import mesquite.lib.characters.WholeCharacterModel;
import mesquite.lib.tree.Tree;
/* ======================================================================== */
/** Sublcass of models of character evolution for likelihood and other calculations involving stochastic models of character evolution. It is expected
that subclasses of this will be specialized for different classes of data (categorical, continuous, etc.)*/
public abstract class MultipleProbabilityModel extends WholeCharacterModel {
	int characterNumbers[] = null;
	private CharacterStatesHolder[] charDistribution = null;
	private MCharactersStatesHolder charMatrix = null;
	int defaultNumChars = 100; //??

	public MultipleProbabilityModel (String name, Class stateClass) {
		super(name, stateClass);
	}

	public abstract CharacterModel cloneModelWithMotherLink(CharacterModel formerClone);
	
	public void copyToClone(CharacterModel pm){
		if (pm==null || !(pm instanceof ProbabilityModel))
			return;
		MultipleProbabilityModel model = (MultipleProbabilityModel)pm;
		model.characterNumbers = characterNumbers;  //why are we not cloning these? PEM
		model.charDistribution = charDistribution;
		model.charMatrix = charMatrix;
		super.copyToClone(model);
	}
	/** sets the seed for simulated character evolution */
	public abstract void setSeed(long seed);
	
	/** returns the seed for simulated character evolution */
	public abstract long getSeed();
	
	public String getTypeName(){
		return "Probability model";
	}
	public String getParadigm(){
		return "Probability";
	}
	
	/** Returns a description of current parameters suitable for viewing by users */
	public abstract String getParameters();
	
	/** Returns whether parameters of model are fully specified.  If so, it can be used for evolving states.*/
	public abstract boolean isFullySpecified();
	
	/** Randomly generates according to model an end state on branch from beginning states*/
	public abstract void evolveState (CharacterState[] beginState, CharacterState[] endState, Tree tree, int node);

	/** Randomly generates according to model an ancestral state for root of tree*/
	public abstract CharacterState[] getRootState (CharacterState[] state, Tree tree);
	
	/** Tells the model that subsequent calculations/simulations will apply to a different set of characters from
	previous calculations.  Between calls to this, the model may assume that the same characters are being dealt with (note: initForNextCharacter
	is called automatically by setCharacter */
	public void initForNewCharacterSet(){
	}
	/** Sets the character number to which this model applies */
	public void setCharacterNumbers(int[] ic){
		characterNumbers = ic;
	}
	/** returns the character number to which this model applies */
	public int[] getCharacterNumber(){
		return characterNumbers;
	}
 	/*.................................................................................................................*/
 	/** Should be overridden to return true if model needs an empirical matrix*/
	public boolean needsEmpirical(){
		return false;
	}
 	/*.................................................................................................................*/
 	/** Should be overridden to return matrix if model has an empirical matrix*/
	public MCharactersDistribution getEmpirical(){
		return null;
	}
	/** Sets the character distribution or history to which this model is currently applying */
	public void setCharacterDistribution (CharacterStatesHolder[] cd){
		charDistribution = cd;
	}
	public CharacterStatesHolder[] getCharacterDistribution (){
		return charDistribution;
	}
	/** To be overridden if processing needs to happen after CharMatrix is set */
	public void recalcAfterSetMCharactersStatesHolder (){
	}
	/** Returns default number of characters to simulate. This can be useful if the model wants to specify whether to use an empirical number or some other number*/ 
   	public int getDefaultNumChars(){
		return defaultNumChars;
   	}
	/** sets default number of characters to simulate. */ 
   	public void setDefaultNumChars(int numChars){
		defaultNumChars = numChars;
   	}
	/** Sets the character matrix or  history to which this model is currently applying */
	public void setMCharactersStatesHolder (MCharactersStatesHolder cd){
		charMatrix = cd;
		recalcAfterSetMCharactersStatesHolder();
	}
	public MCharactersStatesHolder getMCharactersStatesHolder (){
		return charMatrix;
	}



}
