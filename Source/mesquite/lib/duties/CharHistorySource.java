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
/**Assigns states to nodes of tree for a character.
Example modules:  RecAncestralStates.*/

public abstract class CharHistorySource extends MesquiteModule  {
   	 public Class getDutyClass() {
   	 	return CharHistorySource.class;
   	 }
 	public String getDutyName() {
 		return "Supply history of evolution of a character along a tree";
   	 }
   	 public String[] getDefaultModule() {
 		return new String[] {"#RecAncestralStates"};
   	 }
   	
	public abstract boolean allowsStateWeightChoice();
	
	public abstract void prepareHistory(Tree tree, int ic);
	public abstract CharacterHistory getMapping(long im, CharacterHistory history, MesquiteString resultString);
	public abstract String getMappingTypeName();
	
	/* in parlance of these modules, 
	 * History = assignment of possibly-ambiguous states to nodes, e.g. a usual parsimony reconstruction; usually equivalent to Character
	 * Mappings = a single sampling or resolution, unambiguous, e.g. from equivocal cycling or stochastic character mapping
	 */
	public abstract int getNumberOfHistories(Tree tree);
	public abstract int getNumberOfHistories(Taxa taxa);
	public abstract long getNumberOfMappings(Tree tree,  int ic);
	public abstract long getNumberOfMappings(Taxa taxa,  int ic);
	

	public void prepareForMappings(boolean permissiveOfNoSeparateMappings){
	}
	
	/** sets whether or not module will be requested to do one character at a time; if so, then it might know to put up menu items
	to change the model of the character, or adjust it in some other way*/
   	public void setOneCharacterAtATime(boolean chgbl){}

   	/** returns the name of history ic and mapping im*/
   	public abstract String getMappingName(Taxa taxa, int ic, long im);
   	/** returns the name of history ic and mapping im*/
   	public abstract String getMappingName(Tree tree, int ic, long im);
 
   	/** returns the name of history ic*/
   	public abstract String getHistoryName(Taxa taxa, int ic);
   	/** returns the name of history ic*/
   	public abstract String getHistoryName(Tree tree, int ic);
  
   	/** returns the name of histories for menu items, e.g. if each history represents a character, return "Character"*/
   	public abstract String getHistoryTypeName();
  
   	/** queryies the user to choose a history and returns an integer of the history chosen*/
   	public int queryUserChoose(Taxa taxa, String forMessage){
 		int ic=MesquiteInteger.unassigned;
 		int numChars = getNumberOfHistories(taxa);
 		if (MesquiteInteger.isCombinable(numChars)){
 			String[] s = new String[numChars];
 			for (int i=0; i<numChars; i++){
 				s[i]= getHistoryName(taxa, i);
 			}
 			return ListDialog.queryList(containerOfModule(), "Choose character history", "Choose character history " + forMessage,MesquiteString.helpString,  s, 0);
 		}
 		else  {
 			int r = MesquiteInteger.queryInteger(containerOfModule(), "Choose character history", "Number of character history " + forMessage, 1);
 			if (MesquiteInteger.isCombinable(r))
 				return CharacterStates.toInternal(r);
 			else
 				return r;
 		}	
 				
    	}
   	/** queryies the user to choose a history and returns an integer of the history chosen*/
   	public int queryUserChoose(Tree tree, String forMessage){
 		int ic=MesquiteInteger.unassigned;
 		int numChars = getNumberOfHistories(tree);
 		if (MesquiteInteger.isCombinable(numChars)){
 			String[] s = new String[numChars];
 			for (int i=0; i<numChars; i++){
 				s[i]= getHistoryName(tree, i);
 			}
 			return ListDialog.queryList(containerOfModule(), "Choose character history", "Choose character history " + forMessage, MesquiteString.helpString, s, 0);
 		}
 		else  {
 			int r = MesquiteInteger.queryInteger(containerOfModule(), "Choose character history", "Number of character history " + forMessage, 1);
 			if (MesquiteInteger.isCombinable(r))
 				return CharacterStates.toInternal(r);
 			else
 				return r;
 		}	
 				
    	}
}



