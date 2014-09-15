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
/** Contains an array of  meristic character states for one character, at each of the taxa or nodes */
public class MeristicAdjustable  extends MeristicDistribution implements AdjustableDistribution {
	protected IntegerArray firstItem; //other dimension for items (sample size, variance, etc., etc.)
	protected Vector items;
	protected int numNodes;
	
	public MeristicAdjustable (Taxa taxa, int numNodes) {
		super(taxa);
		items = new Vector();
		this.numNodes= numNodes;
		firstItem = new IntegerArray(numNodes); 
		items.addElement(firstItem);
		deassignStates();
	}
	
	/*..........................................MeristicAdjustable................*/
	public int getNumItems() {
		return items.size();
	}
	/*.........................................MeristicAdjustable...............*/
	public int getNumTaxa() {
		return numNodes;
	}
	/*.........................................MeristicAdjustable...............*/
	public int getNumNodes() {
		return numNodes;
	}
	/*.........................................MeristicAdjustable...............*/
	/* makes new item*/
	public IntegerArray addItem(NameReference nr){
		IntegerArray item = new IntegerArray(numNodes);
			if (firstItem == null)
				firstItem = item; 
			if (nr!=null)
				item.setNameReference(nr);
			for (int it=0; it<numNodes; it++)
					item.setValue(it, MeristicState.unassigned);
			items.addElement(item);
			return item;
	}
	
	/*.........................................MeristicAdjustable...............*/
	public IntegerArray establishItem(String name){
		return establishItem(NameReference.getNameReference(name));
	}
	/*.........................................MeristicAdjustable...............*/
	/** Make an item with the passed name.  If the an item already made is not yet named,
	then this will simply use this item and give it a new name.  Otherwise, a new
	matrix is made for a new item, and the name is attached to it.*/
	public IntegerArray establishItem(NameReference nr){
		if (firstItem != null && firstItem.getNameReference()==null && items.size() ==1) {
			if (nr!=null)
				firstItem.setNameReference(nr);
			return firstItem;
		}
		else {
			for (int i=0; i<getNumItems(); i++) {
				IntegerArray matrix = (IntegerArray)items.elementAt(i);
				if (matrix.getNameReference()== null) {
					matrix.setNameReference(nr);
					return matrix;
				}
			}
			return addItem(nr);
		}
	}
	/*.........................................MeristicAdjustable...............*/
	public IntegerArray addItem(String name){
		return addItem(NameReference.getNameReference(name));
	}
	/*..........................................MeristicStates................*/
	public void setItemsAs(ItemContainer iCont){
		if (iCont == null)
			return;
		for (int i=0; i<iCont.getNumItems(); i++) {
			if (i<getNumItems() )
				setItemReference(i, iCont.getItemReference(i));
			else
				addItem(iCont.getItemName(i));
		}
		if (getNumItems()>iCont.getNumItems())
			for (int i = getNumItems(); i>=iCont.getNumItems(); i--)
				removeItem(i);

	}
	/*..........................................MeristicData................*/
	/** Removes item.*/
	public void removeItem(int item){
		if (item<0 || item> getNumItems()-1)
			return;
		IntegerArray matrix = getItem(item);
		if (matrix==null)
			return;
		items.removeElement(matrix);
		if (item == 0) {
			firstItem = (IntegerArray)items.elementAt(0);
		}
	}
	/*..........................................MeristicStates................*/
	/**Sets the name reference for the item number "index".*/
	public void setItemReference(int index, NameReference nr){
			IntegerArray matrix =getItem(index);
			matrix.setNameReference(nr);
	}
	/*.........................................MeristicAdjustable...............*/
	public void setItems(MeristicAdjustable s){
		if (s==null)
			setItems((String[])null);
		else {
			String[] names = new String[s.getNumItems()];
			for (int i=0; i<s.getNumItems(); i++) {
				NameReference nr = s.getItemReference(i);
				if (nr==null)
					names[i] = null;
				else
					names[i] = nr.getValue();
			}
			setItems(names);
		}
	}
	/*.........................................MeristicAdjustable...............*/
	public void setItems(String[] names){
		if (names == null) { //setting to a single item with null name
			if (getNumItems() == 0) {
				addItem((NameReference)null);
			}
			else if (getNumItems() == 1)
				firstItem.setNameReference(null);
			else {
				items.removeAllElements();
				firstItem = null;
				establishItem((NameReference)null);
			}
		}
		else {
			if (getNumItems() == names.length) {
				for (int i=0; i<getNumItems(); i++) {
					IntegerArray item = (IntegerArray)items.elementAt(i);
					item.setNameReference(NameReference.getNameReference(names[i])); //SHOULD pass string and change only if needed
				}
			}
			else if (getNumItems()< names.length) {
				for (int i=0; i<getNumItems(); i++) {
					IntegerArray item = (IntegerArray)items.elementAt(i);
					item.setNameReference(NameReference.getNameReference(names[i]));
				}
				for (int i=getNumItems(); i<names.length; i++)
					addItem(names[i]);
			}
			else {
				items.removeAllElements();
				firstItem = null;
				for (int i=getNumItems(); i<names.length; i++)
					establishItem(names[i]);
			}
		}
		
	}
	/*.........................................MeristicAdjustable...............*/
	public IntegerArray getItem(int index){
		if (index<0 || index>= items.size())
			return null;
		return (IntegerArray)items.elementAt(index);
	}
	/*.........................................MeristicAdjustable...............*/
	public String getItemName(int index){
			if (items == null)
				return null;
			else
				return ((IntegerArray)items.elementAt(index)).getName();
	}
	/*.........................................MeristicAdjustable...............*/
	public NameReference getItemReference(String name){
		NameReference nr = NameReference.getNameReference(name);
		for (int i=0; i<getNumItems(); i++) {
			IntegerArray item = (IntegerArray)items.elementAt(i);
			if (nr.equals(item.getNameReference()))
				return item.getNameReference();
		}
		return null;
		
	}
	/*.........................................MeristicAdjustable...............*/
	public NameReference getItemReference(int index){
			IntegerArray item = (IntegerArray)items.elementAt(index);
			return item.getNameReference();
	}
	/*.........................................MeristicAdjustable...............*/
	public int getItemNumber(NameReference nr){
		if (nr==null)
			return -1;
		for (int i=0; i<getNumItems(); i++)
			if (nr.equals(((IntegerArray)items.elementAt(i)).getNameReference()))
				return i;
		return -1;
	}
	
	/*.........................................MeristicAdjustable...............*/
	public void deassignStates(){
		for (int i=0; i<getNumItems(); i++)
			for (int j=0; j<numNodes; j++)
				((IntegerArray)items.elementAt(i)).setValue(j, MeristicState.unassigned);
	}
	/*.........................................MeristicAdjustable...............*/
	public int getState (int N, int item) {
		if (item>=getNumItems()) //ERROR MESSAGE
			return 0;
		else if (N>=numNodes) {
			return 0;
		}
		else
			return ((IntegerArray)items.elementAt(item)).getValue(N);
	}
	/*.........................................MeristicAdjustable...............*/
	public int getState (int N) {
		return firstItem.getValue(N);
	}
	/*.........................................MeristicAdjustable...............*/
	public void setState (int N, int item, int d) {
		((IntegerArray)items.elementAt(item)).setValue(N, d);
	}
	/*.........................................MeristicAdjustable...............*/
	public void setState (int N, int d) {
		firstItem.setValue(N, d);
	}
	/*.........................................MeristicAdjustable...............*/
	public void setCharacterState (int N, CharacterState cs) {
		if (checkIllegalNode(N, 7))
			return;
		if (cs == null || !(cs instanceof MeristicState))
			return; 
		for(int i=0; i<getNumItems(); i++)
			setState(N, i, ((MeristicState)cs).getValue(i));
	}
	/*..........................................  MeristicAdjustable ..................................................*/
	/**Trade states of nodes it and it2*/
	public void tradeStatesBetweenTaxa(int it, int it2) {
		if (checkIllegalNode(it, 9128) && checkIllegalNode(it, 9129))
			return;
		for (int item=0; item<getNumItems(); item++) {
			IntegerArray matrix = getItem(item);
			int temp = matrix.getValue(it);
			matrix.setValue(it, matrix.getValue(it2));
			matrix.setValue(it2, temp);
		}
	}
	/*.........................................MeristicAdjustable...............*/
	/** This readjust procedure can be called to readjust the size of storage of
	states of a character for nodes. */
	public AdjustableDistribution adjustSize(Taxa taxa) {
		if (taxa.getNumTaxa() == this.getNumTaxa())
			return this;
		else {
			MeristicAdjustable soc = new MeristicAdjustable(taxa, taxa.getNumTaxa());
			soc.setItemsAs(this);
			soc.setParentData(getParentData());
			soc.setParentCharacter(getParentCharacter());
			((CharacterStates)soc).setExplanation(getExplanation());
			return soc;
		}
	}
/**/
}



