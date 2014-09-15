/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.characters; 

import java.awt.*;
import mesquite.lib.duties.*;
import mesquite.lib.*;

/*Last documented:  April 2003 */
/* ======================================================================== */
/**Stores the character states at a series of terminal taxa or nodes in a tree.  The class on which particular classes implementing
CharacterDistribution and CharacterHistory are based.  See general discussion of character storage classes under CharacterState*/
public abstract class CharacterStates implements CharacterStatesHolder, Listable, Explainable  {  
	protected CharacterData data=null; //parent CharacterData
	protected int characterNumber=0; //parent character within the parent CharacterData
	public static long totalCreated = 0;
	private String name = null;
	private Taxa  taxa;
	private long id;
	public CharacterStates (Taxa taxa) {
		id = totalCreated;
		totalCreated++;
		this.taxa = taxa;
	}
	
   	public CharacterState makeCharacterState(){
		Class c = getStateClass();
		try {
			CharacterState s = (CharacterState)c.newInstance();
			return s;
		}
		catch (IllegalAccessException e){MesquiteTrunk.mesquiteTrunk.alert("iae csmcs"); }
		catch (InstantiationException e){MesquiteTrunk.mesquiteTrunk.alert("ie csmcs"); }
		return null;
   	}
   	public long getID(){
   		return id;
   	}
	String explanation;
	public void setExplanation(String expl){
		explanation = expl;
	}
	public String getExplanation(){
		return explanation;
	}
	/*.................................................................................................................*/
	/** returns taxa object */
	public Taxa getTaxa (){
		return taxa;
	}
	/*.................................................................................................................*/
	/** sets taxa object */
	public void setTaxa (Taxa taxa){
		this.taxa = taxa;
	}
	/*.................................................................................................................*/
	/** returns name */
	public String getName (){
		return name;
	}
	/*.................................................................................................................*/
	/** sets name */
	public void setName (String name){
		this.name = name;
	}
	/*.................................................................................................................*/
	/** returns parent data of this CharacterStates.  There is not a corresponding set procedure because the parent data
	is set either by the constructor (for Embedded distributions) or by a set procedure (for History or Adjustables) */
	public CharacterData getParentData (){
		return data;
	}
	/*.................................................................................................................*/
	/** returns parent character of this CharacterStates*/
	public int getParentCharacter (){
		return characterNumber;
	}
	/*.................................................................................................................*/
	protected boolean checkIllegalNode(int node, int where) {
		if (node<0 || node>=getNumNodes()) {
			//MesquiteMessage.println("illegal node in CharacterStates (node: " + node + " max: " + getNumNodes() + ") " + where);
			//MesquiteMessage.printStackTrace();
			return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	/**sets the parent CharacterData from which this CharacterDistribution is derived or related*/
	public void setParentData (CharacterData cd){
		data = cd;
	}
	/*.................................................................................................................*/
	/**sets the parent character number from which this CharacterDistribution is derived or related. */
	public void setParentCharacter (int ic){
		if (ic<0)
			ic = 0;
		if (data!=null) {
			if (ic<data.getNumChars())
				this.characterNumber = ic;
			else
				this.characterNumber = 0;
		}
		else
			this.characterNumber = ic;
	}
	/*.................................................................................................................*/
	/**Translates internal numbering system of characters to external (currently, 0 based to 1 based*/
	public static long toExternalLong(long i){
		if (!MesquiteLong.isCombinable(i))
			return i;
		else
			return i+1;
	}
	/*.................................................................................................................*/
	/**Translates external numbering system of characters to internal (currently, 1 based to 0 based*/
	public static long toInternalLong(long i){
		if (!MesquiteLong.isCombinable(i))
			return i;
		else
			return i-1;
	}
	/*.................................................................................................................*/
	/**Translates internal numbering system of characters to external (currently, 0 based to 1 based*/
	public static int toExternal(int i){
		if (!MesquiteInteger.isCombinable(i))
			return i;
		else
			return i+1;
	}
	/*.................................................................................................................*/
	/**Translates external numbering system of characters to internal (currently, 1 based to 0 based*/
	public static int toInternal(int i){
		if (!MesquiteInteger.isCombinable(i))
			return i;
		else
			return i-1;
	}
	public String toStringWithDetails(){
		StringBuffer sb = new StringBuffer();
		CharacterState cs = null;
		boolean first = true;
		for (int i = 0; i< getNumNodes(); i++){
			if (!isUnassigned(i)){
				if (!first)
					sb.append('\t');
				first = false;
				sb.append(Integer.toString(i));
				sb.append('\t');
				cs = getCharacterState(cs, i);
				sb.append(cs.toString());
			}
		}
		return sb.toString();
	}
}

