/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


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


/* ======================================================================== */
/**A class for an array of  meristic character states for many characters, at each of the taxa or nodes.*/
public abstract class MMeristicStates extends MCharactersStates implements ItemContainer {
	long id;
	static long totalCreated = 0;
	public MMeristicStates (Taxa taxa) {
		super(taxa);
		totalCreated++;
		id = totalCreated;
	}
	
	public long getID(){
		return id;
	}
	/*..........................................  MMeristicStates  ..................................................*/
	/** Indicates the type of character stored */ 
	public Class getStateClass(){
		return MeristicState.class;
	}
	/**returns the corresponding CharacterData subclass*/
	public Class getCharacterDataClass (){
		return MeristicData.class;
	}
	/*..........................................  MMeristicStates  ..................................................*/
	/** returns the name of the type of data stored */
	public String getDataTypeName(){
		return MeristicData.DATATYPENAME;
	}
	/*..........................................  MMeristicStates  ..................................................*/
	/**returns state of character ic in taxon/node it */
	public abstract int getState (int ic, int it);

	/*..........................................MMeristicStates................*/
	/**returns state of item item of character ic in taxon/node it */
	public abstract int getState (int ic, int it, int item);
	
	/*..........................................MMeristicStates................*/
	public abstract Integer2DArray getItem(int index);
	
	/*..........................................MMeristicStates................*/
	public int[][] getMatrix(int index){
		Integer2DArray m2da = getItem(index);
		if (m2da==null)
			return null;
		return m2da.getMatrix();
	}
	/*..........................................MMeristicStates................*/
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
	/*..........................................MMeristicStates................*/
	public abstract String getItemName(int index);
	/*..........................................MMeristicStates................*/
	public abstract int getItemNumber(NameReference nr);
	/*..........................................MMeristicStates................*/
	public abstract int getNumberOfItems();
	/*..........................................MMeristicStates................*/
	public int getNumItems(){
		return getNumberOfItems();
	}
	/*..........................................MMeristicStates................*/
	public boolean allCombinable(){
		for (int item = 0; item < getNumberOfItems(); item ++)
			for (int ic = 0; ic < getNumChars(); ic++)
				for (int it = 0; it<getNumNodes(); it++) {
					if (!MesquiteInteger.isCombinable(getState(ic, it, item)))
						return false;
				}
		return true;
	}
	/*..........................................MMeristicStates................*/
	public boolean allCombinable(int item){
		for (int ic = 0; ic < getNumChars(); ic++)
			for (int it = 0; it<getNumNodes(); it++) {
				if (!MesquiteInteger.isCombinable(getState(ic, it, item)))
					return false;
			}
		return true;
	}
	/*..........................................MMeristicStates................*/
	public NameReference getItemReference(int n){
		return NameReference.getNameReference(getItemName(n));
	}
	/*..........................................MMeristicStates................*/
	public NameReference getItemReference(String name){
		return NameReference.getNameReference(name);
	}
	/*..........................................MMeristicStates................*/
	/** get CharacterState at node N*/
	public CharacterState getCharacterState (CharacterState cs, int ic, int it){  
		MeristicState c;
		if (cs !=null && cs instanceof MeristicState)
			c = (MeristicState)cs;
		else
			c = new MeristicState(); 
		c.setItemsAs(this);
		for (int item = 0; item<getNumberOfItems(); item++)
			c.setValue(item, getState(ic, it, item));
		return c;
	}
	/*..........................................MMeristicDistribution................*/
   	public String toString(){
   		return "Meristic matrix (" + getClass().getName() + ") id: " + getID() + " chars: " + getNumChars() + " taxa: " + getNumTaxa() + " items " + getNumItems();
   	}
}

