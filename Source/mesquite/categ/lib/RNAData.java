/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.categ.lib;

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;


/* ======================================================================== */
/** A subclass of CharacterData for RNA data stored as Categorical sets (e.g, "{A, C}" */
public class RNAData extends DNAData {
	public static String DATATYPENAME="RNA Data";
	public RNAData(CharMatrixManager manager, int numTaxa, int numChars, Taxa taxa){
		super(manager, numTaxa, numChars, taxa);
		super.setSymbolDirect(3, 'U');
	}
   	public CharacterDistribution makeCharacterDistribution(){
   		RNACharacterAdjustable c= new RNACharacterAdjustable(getTaxa(), numTaxa);
   		c.setParentData(this);
   		return c;
   	}
	/** Indicates the type of character stored */ 
	public Class getStateClass(){
		return RNAState.class;
	}
	public String getDataTypeName(){
		return RNAData.DATATYPENAME;
	}
	/*..........................................    ..................................................*/
	public CharacterState makeCharacterState(){
  		return (CategoricalState) new RNAState(getDefaultState());
	}
	/*............................................................................................*/
	/** gets default state symbol for state "state", returned as string. */
	public static String getDefaultStateSymbol(int state) {
		if (state==0)
			return "A";
		else if (state==1)
			return "C";
		else if (state==2)
			return "G";
		else if (state==3)
			return "U";
		else
			return Integer.toString(state);
	}
	/*..........................................  RNAData  ..................................................*/
	/* Appends to buffer state symbol for state e Ã*/
	protected void appendStateSymbol(int e, boolean lowerCase, StringBuffer sb){
		if (lowerCase){
			if (e==0)
				sb.append('a');
			else if (e==1)
				sb.append('c');
			else if (e==2)
				sb.append('g');
			else if (e==3)
				sb.append('u');
		}
		else {
			if (e==0)
				sb.append('A');
			else if (e==1)
				sb.append('C');
			else if (e==2)
				sb.append('G');
			else if (e==3)
				sb.append('U');
		}
	}
	/*..........................................  RNAData  ..................................................*/
	/* Returns state set from single state symbol Ã*/
   	public long fromChar(char state){
		if (state == getInapplicableSymbol())
			return CategoricalState.inapplicable;
		else if (state== getUnassignedSymbol())
			return  CategoricalState.unassigned;
		else if (state == getMatchChar()) {
			return CategoricalState.unassigned;//note: this should't happen, as matchchar with no char #
		}
		else {
   			return DNAState.fromCharStatic(state);
  		}
 	}
}



