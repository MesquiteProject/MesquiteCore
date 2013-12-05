/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.meristic.lib;

import java.awt.*;
import java.util.*;
import mesquite.lib.duties.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.cont.lib.ItemContainer;

/** Contains an array of  meristic character states for one character, at each of the taxa or nodes 
 See notes under <a href = "MeristicData.html">MeristicData</a> regarding items */
public abstract class MeristicStates  extends CharacterStates implements ItemContainer {
	
	public MeristicStates (Taxa taxa) {
		super(taxa);
	}
	/*..........................................MeristicStates................*/
	/**returns blank CharacterState object */
	public CharacterState getCharacterState (){
		return new MeristicState();
	}
	/** Indicates the type of character stored */ 
	public Class getStateClass(){
		return MeristicState.class;
	}
	/**returns the corresponding CharacterData subclass*/
	public Class getCharacterDataClass (){
		return MeristicData.class;
	}
	/** returns the name of the type of data stored */
	public String getDataTypeName(){
		return MeristicData.DATATYPENAME;
	}
	/*..........................................MeristicStates................*/
	public abstract int getNumItems();
	/*..........................................MeristicStates................*/
	public abstract String getItemName(int index);
	/*..........................................MeristicStates................*/
	public abstract NameReference getItemReference(String name);
	/*..........................................MeristicStates................*/
	public abstract NameReference getItemReference(int index);
	/*..........................................MeristicStates................*/
	public abstract int getItemNumber(NameReference nr);
	/*..........................................MeristicStates................*/
	public CharacterModel getDefaultModel(MesquiteProject file, String paradigm){
   		NameReference p = NameReference.getNameReference(paradigm);
   		DefaultReference dR = MeristicData.findDefaultReference(p);
   		if (dR==null)
   			return null;
   		else {
   			CharacterModel cm = file.getCharacterModel(dR.getDefault());
   			if (cm==null) 
   				MesquiteMessage.println("Default model not found / " + dR.getDefault());
   			return cm;
   		}
   	}
	/*..........................................MeristicStates................*/
	public void logStates(){
		MesquiteModule.mesquiteTrunk.logln("States ");
		String statesString="";
		for (int ic=0; ic<getNumNodes(); ic++) {
			for (int iitems=0; iitems<getNumItems(); iitems++)
				statesString+=toString(ic, " ") + " ";
		}
		statesString+= '\r';
		MesquiteModule.mesquiteTrunk.logln(statesString);
	}
	/*..........................................MeristicStates................*/
	public abstract int getState (int N, int item);
	/*..........................................MeristicStates................*/
	public abstract int getState (int N);
	/*..........................................MeristicStates................*/
	
   	public  boolean isInapplicable(int N){
   		for (int item = 0; item< getNumItems(); item++)
   			if ( getState(N, item)!= MeristicState.inapplicable)
   				return false;
   		return true; 
   	}

	/*..........................................MeristicStates................*/
   	public  boolean isUnassigned(int N){
   		for (int item = 0; item< getNumItems(); item++)
   			if ( getState(N, item)!= MeristicState.unassigned)
   				return false;
   		return true; 
   	}
   	
	/*.................................................................................................................*/
   	/** returns whether the state of character has uncertainty in node/taxon N*/
   	public boolean isUncertain(int N){  //TODO: implement uncertainty for meristic data
   		return false;
   	}
	/*..........................................MeristicStates................*/
   	public int userQueryItem(String message, MesquiteModule module){
		int numItems =getNumItems();
		String[] items = new String[numItems];
		for (int i=0; i<items.length; i++){
			if (StringUtil.blank(getItemName(i)))
				items[i] = "(unnamed)";
			else
				items[i]= getItemName(i);
		}
		return ListDialog.queryList(module.containerOfModule(), "Select item", message, MesquiteString.helpString,  items, 0);
   	}
	/*..........................................MeristicStates................*/
	public CharacterState getCharacterState (CharacterState cs, int N){
		MeristicState c;
		if (cs==null || !(cs instanceof MeristicState))
			c = new MeristicState();
		else c = (MeristicState)cs;
		c.setItemsAs(this);
		for (int i=0; i<getNumItems(); i++)
			c.setValue(i, getState(N, i));
		return c;
	}
	/*..........................................MeristicStates................*/
	/** Is the first state greater than second?  This uses only the first item! */
	public boolean firstIsGreater (int N, int M){ 
		if (getState(N) > getState(M))
			return true;
		return false;
		
	}
	/*..........................................MeristicStates................*/
	public boolean statesEqual(int N, int M) {
   		for (int item = 0; item< getNumItems(); item++)
   			if (getState(N, item)!= getState(M, item))
   				return false;
   		return true; 
	}
	/*..........................................MeristicStates................*/
	public String toString (int node, String lineEnding) {
		if (getNumItems() == 1)
			return MeristicState.toString(getState(node)); 
		else { //if min and max do special string
			String s="";
			for (int i=0; i<getNumItems(); i++) {
				s += getItemName(i) + ":" + MeristicState.toString(getState(node, i)) + lineEnding;
			}
			return s;
		}
	}
	
}


