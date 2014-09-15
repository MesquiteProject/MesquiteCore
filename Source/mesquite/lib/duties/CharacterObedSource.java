/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.duties;

import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;


/* ======================================================================== */
/**
A class whose declaration is exactly the same as a CharacterSource & CharacterOneSource, since both can return characters one at a time.
The only distinction is to know purpose hired.  If hired as an obedient source, the module should not control the current character, for instance
(if hired as not obedient, it is free to do so).  See similar organization in CharMatrixObedSource.*/

public abstract class CharacterObedSource extends MesquiteModule implements ItemsSource  {

   	 public Class getDutyClass() {
   	 	return CharacterObedSource.class;
   	 }
   	 public String[] getDefaultModule() {
   	 	return new String[] {"#StoredCharacters", "#SimulatedCharacters"};
   	 }
 	public String getDutyName() {
 		return "Character Source (obed.)";
   	 }
	 public String getFunctionIconPath(){
   		 return getRootImageDirectoryPath() + "functionIcons/charSource.gif";
   	 }
  	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public abstract void initialize(Taxa taxa);
   	/** returns character numbered ic*/
   	public abstract CharacterDistribution getCharacter(Taxa taxa, int ic);
   	/** returns number of characters for given Taxa*/
   	public abstract int getNumberOfCharacters(Taxa taxa);

   	/** queryies the user to choose a character and returns an integer of the character chosen*/
   	public int queryUserChoose(Taxa taxa, String forMessage){
 		int ic=MesquiteInteger.unassigned;
 		int numChars = getNumberOfCharacters(taxa); 
 		if (MesquiteInteger.isCombinable(numChars)){
 			String[] s = new String[numChars];
 			for (int i=0; i<numChars; i++){
 				s[i]= getCharacterName(taxa, i);
 			}
 			return ListDialog.queryList(containerOfModule(), "Choose character", "Choose character " + forMessage, MesquiteString.helpString, s, 0);
 		}
 		else  {
 			int r = MesquiteInteger.queryInteger(containerOfModule(), "Choose character", "Number of character " + forMessage, 1);
 			if (MesquiteInteger.isCombinable(r))
 				return CharacterStates.toInternal(r);
 			else
 				return r;
 		}	
 				
    	}
   	/** returns the name of character ic*/
   	public abstract String getCharacterName(Taxa taxa, int ic);

        /* -- the following are a preliminary attempt to allow character sources to know exactly what tree the 
        character will be used with.  These can be overridden by modules to do simulations on the trees, or to see if there
        is a simulated character attached to the tree, or what ---*/
   	public void initialize(Tree tree){
   		if (tree==null) return;
   		else initialize(tree.getTaxa());
   	}
   	 /** returns the number of character matrices that can be supplied for the given taxa*/
    	public int getNumberOfCharacters(Tree tree){
   		if (tree==null) return 0;
   		else return getNumberOfCharacters(tree.getTaxa());
   	}
   	/** gets the indicated matrix.*/
   	public CharacterDistribution getCharacter(Tree tree, int ic){
   		if (tree==null) return null;
   		else return getCharacter(tree.getTaxa(), ic);
   	}
   	/** returns the name of character ic*/
   	public String getCharacterName(Tree tree, int ic){
   		if (tree==null) return null;
   		else return getCharacterName(tree.getTaxa(), ic);
   	}
    	/** If this character source does in fact depend on the tree (i.e. it overrides the methods being passed a tree)
   	then this method should be overridden to return true.  This allows modules using it to know they should
   	re-request a character if the tree has changed.  (Relying on TreeContext or listening systems could be
   	too cumbersome since such requests could come in tight loops that are cycling through trees .*/
   	public boolean usesTree(){
   		return false;
   	}
        /* ----------------*/
	/*===== For ItemsSource interface ======*/
   	/** returns item numbered ic*/
   	public Object getItem(Taxa taxa, int ic){
		CommandRecord.tick("Getting character " + ic);
   		return getCharacter(taxa, ic);
   	}
   	/** returns number of characters for given Taxa*/
   	public int getNumberOfItems(Taxa taxa){
		CommandRecord.tick("");
   		return getNumberOfCharacters(taxa);
   	}
   	/** returns name of type of item, e.g. "Character", or "Taxon"*/
   	public String getItemTypeName(){
   		return "Character";
   	}
   	/** returns name of type of item, e.g. "Characters", or "Taxa"*/
   	public String getItemTypeNamePlural(){
   		return "Characters";
   	}
   	public Selectionable getSelectionable(){
   		return null;
   	}
   	
   	CharSelectionSet charHighlightSet;
   	mesquite.lib.characters.CharacterData highlightData;
   	String charSetName;
    	public void setEnableWeights(boolean enable){
    	}
   	public boolean itemsHaveWeights(Taxa taxa){
   		return false;
   	}
   	public double getItemWeight(Taxa taxa, int ic){
   		return MesquiteDouble.unassigned;
   	}
  	/** zzzzzzzzzzzz*/
   	public void prepareItemColors(Taxa taxa){
   	}
  	/** zzzzzzzzzzzz*/
   	public Color getItemColor(Taxa taxa, int ic){
   		return null;
   	}
  	/** zzzzzzzzzzzz*/
   	public String getItemName(Taxa taxa, int ic){
   		return getCharacterName(taxa, ic);
   	}
}



