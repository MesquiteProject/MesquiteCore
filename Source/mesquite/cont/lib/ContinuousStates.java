/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.cont.lib;

import java.awt.*;
import java.util.*;
import mesquite.lib.duties.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;

/** Contains an array of  continuous character states for one character, at each of the taxa or nodes 
 See notes under <a href = "ContinuousData.html">ContinuousData</a> regarding items */
public abstract class ContinuousStates  extends CharacterStates implements ItemContainer {
	
	public ContinuousStates (Taxa taxa) {
		super(taxa);
	}
	/*..........................................ContinuousStates................*/
	/**returns blank CharacterState object */
	public CharacterState getCharacterState (){
		return new ContinuousState();
	}
	/** Indicates the type of character stored */ 
	public Class getStateClass(){
		return ContinuousState.class;
	}
	/**returns the corresponding CharacterData subclass*/
	public Class getCharacterDataClass (){
		return ContinuousData.class;
	}
	/** returns the name of the type of data stored */
	public String getDataTypeName(){
		return ContinuousData.DATATYPENAME;
	}
	/*..........................................ContinuousStates................*/
	public abstract int getNumItems();
	/*..........................................ContinuousStates................*/
	public abstract String getItemName(int index);
	/*..........................................ContinuousStates................*/
	public abstract NameReference getItemReference(String name);
	/*..........................................ContinuousStates................*/
	public abstract NameReference getItemReference(int index);
	/*..........................................ContinuousStates................*/
	public abstract int getItemNumber(NameReference nr);
	/*..........................................ContinuousStates................*/
	public CharacterModel getDefaultModel(MesquiteProject file, String paradigm){
   		NameReference p = NameReference.getNameReference(paradigm);
   		DefaultReference dR = ContinuousData.findDefaultReference(p);
   		if (dR==null)
   			return null;
   		else {
   			CharacterModel cm = file.getCharacterModel(dR.getDefault());
   			if (cm==null) 
   				MesquiteMessage.println("Default model not found / " + dR.getDefault());
   			return cm;
   		}
   	}
	/*..........................................ContinuousStates................*/
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
	/*..........................................ContinuousStates................*/
	public abstract double getState (int N, int item);
	/*..........................................ContinuousStates................*/
	public abstract double getState (int N);
	/*..........................................ContinuousStates................*/
	
   	public  boolean isInapplicable(int N){
   		for (int item = 0; item< getNumItems(); item++)
   			if ( getState(N, item)!= ContinuousState.inapplicable)
   				return false;
   		return true; 
   	}

	/*..........................................ContinuousStates................*/
   	public  boolean isUnassigned(int N){
   		for (int item = 0; item< getNumItems(); item++)
   			if ( getState(N, item)!= ContinuousState.unassigned)
   				return false;
   		return true; 
   	}
   	
	/*.................................................................................................................*/
   	/** returns whether the state of character has uncertainty in node/taxon N*/
   	public boolean isUncertain(int N){  //TODO: implement uncertainty for continuous data
   		return false;
   	}
	/*..........................................ContinuousStates................*/
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
	/*..........................................ContinuousStates................*/
	public CharacterState getCharacterState (CharacterState cs, int N){
		ContinuousState c;
		if (cs==null || !(cs instanceof ContinuousState))
			c = new ContinuousState();
		else c = (ContinuousState)cs;
		c.setItemsAs(this);
		for (int i=0; i<getNumItems(); i++)
			c.setValue(i, getState(N, i));
		return c;
	}
	/*..........................................ContinuousStates................*/
	/** Is the first state greater than second?  This uses only the first item! */
	public boolean firstIsGreater (int N, int M){ 
		if (getState(N) > getState(M))
			return true;
		return false;
		
	}
	/*..........................................ContinuousStates................*/
	public boolean statesEqual(int N, int M) {
   		for (int item = 0; item< getNumItems(); item++)
   			if (getState(N, item)!= getState(M, item))
   				return false;
   		return true; 
	}
	/*..........................................ContinuousStates................*/
	public String toString (int node, String lineEnding) {
		if (getNumItems() == 1)
			return MesquiteDouble.toString(getState(node)); 
		else { //if min and max do special string
			String s="";
			for (int i=0; i<getNumItems(); i++) {
				s += getItemName(i) + ":" + MesquiteDouble.toString(getState(node, i)) + lineEnding;
			}
			return s;
		}
	}
	
}


