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

/* ======================================================================== */
/** Contains an array of  continuous character states for one character, at each of the taxa or nodes */
public class ContinuousAdjustable  extends ContinuousDistribution implements AdjustableDistribution {
	protected DoubleArray firstItem; //other dimension for items (sample size, variance, etc., etc.)
	protected Vector items;
	protected int numNodes;
	
	public ContinuousAdjustable (Taxa taxa, int numNodes) {
		super(taxa);
		items = new Vector();
		this.numNodes= numNodes;
		firstItem = new DoubleArray(numNodes); 
		items.addElement(firstItem);
		deassignStates();
	}
	
	/*..........................................ContinuousAdjustable................*/
	public int getNumItems() {
		return items.size();
	}
	/*.........................................ContinuousAdjustable...............*/
	public int getNumTaxa() {
		return numNodes;
	}
	/*.........................................ContinuousAdjustable...............*/
	public int getNumNodes() {
		return numNodes;
	}
	/*.........................................ContinuousAdjustable...............*/
	/* makes new item*/
	public DoubleArray addItem(NameReference nr){
			DoubleArray item = new DoubleArray(numNodes);
			if (firstItem == null)
				firstItem = item; 
			if (nr!=null)
				item.setNameReference(nr);
			for (int it=0; it<numNodes; it++)
					item.setValue(it, ContinuousState.unassigned);
			items.addElement(item);
			return item;
	}
	
	/*.........................................ContinuousAdjustable...............*/
	public DoubleArray establishItem(String name){
		return establishItem(NameReference.getNameReference(name));
	}
	/*.........................................ContinuousAdjustable...............*/
	/** Make an item with the passed name.  If the an item already made is not yet named,
	then this will simply use this item and give it a new name.  Otherwise, a new
	matrix is made for a new item, and the name is attached to it.*/
	public DoubleArray establishItem(NameReference nr){
		if (firstItem != null && firstItem.getNameReference()==null && items.size() ==1) {
			if (nr!=null)
				firstItem.setNameReference(nr);
			return firstItem;
		}
		else {
			for (int i=0; i<getNumItems(); i++) {
				DoubleArray matrix = (DoubleArray)items.elementAt(i);
				if (matrix.getNameReference()== null) {
					matrix.setNameReference(nr);
					return matrix;
				}
			}
			return addItem(nr);
		}
	}
	/*.........................................ContinuousAdjustable...............*/
	public DoubleArray addItem(String name){
		return addItem(NameReference.getNameReference(name));
	}
	/*..........................................ContinuousStates................*/
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
	/*..........................................ContinuousData................*/
	/** Removes item.*/
	public void removeItem(int item){
		if (item<0 || item> getNumItems()-1)
			return;
		DoubleArray matrix = getItem(item);
		if (matrix==null)
			return;
		items.removeElement(matrix);
		if (item == 0) {
			firstItem = (DoubleArray)items.elementAt(0);
		}
	}
	/*..........................................ContinuousStates................*/
	/**Sets the name reference for the item number "index".*/
	public void setItemReference(int index, NameReference nr){
			DoubleArray matrix =getItem(index);
			matrix.setNameReference(nr);
	}
	/*.........................................ContinuousAdjustable...............*/
	public void setItems(ContinuousAdjustable s){
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
	/*.........................................ContinuousAdjustable...............*/
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
					DoubleArray item = (DoubleArray)items.elementAt(i);
					item.setNameReference(NameReference.getNameReference(names[i])); //SHOULD pass string and change only if needed
				}
			}
			else if (getNumItems()< names.length) {
				for (int i=0; i<getNumItems(); i++) {
					DoubleArray item = (DoubleArray)items.elementAt(i);
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
	/*.........................................ContinuousAdjustable...............*/
	public DoubleArray getItem(int index){
		if (index<0 || index>= items.size())
			return null;
		return (DoubleArray)items.elementAt(index);
	}
	/*.........................................ContinuousAdjustable...............*/
	public String getItemName(int index){
			if (items == null)
				return null;
			else
				return ((DoubleArray)items.elementAt(index)).getName();
	}
	/*.........................................ContinuousAdjustable...............*/
	public NameReference getItemReference(String name){
		NameReference nr = NameReference.getNameReference(name);
		for (int i=0; i<getNumItems(); i++) {
			DoubleArray item = (DoubleArray)items.elementAt(i);
			if (nr.equals(item.getNameReference()))
				return item.getNameReference();
		}
		return null;
		
	}
	/*.........................................ContinuousAdjustable...............*/
	public NameReference getItemReference(int index){
			DoubleArray item = (DoubleArray)items.elementAt(index);
			return item.getNameReference();
	}
	/*.........................................ContinuousAdjustable...............*/
	public int getItemNumber(NameReference nr){
		if (nr==null)
			return -1;
		for (int i=0; i<getNumItems(); i++)
			if (nr.equals(((DoubleArray)items.elementAt(i)).getNameReference()))
				return i;
		return -1;
	}
	
	/*.........................................ContinuousAdjustable...............*/
	public void deassignStates(){
		for (int i=0; i<getNumItems(); i++)
			for (int j=0; j<numNodes; j++)
				((DoubleArray)items.elementAt(i)).setValue(j, ContinuousState.unassigned);
	}
	/*.........................................ContinuousAdjustable...............*/
	public double getState (int N, int item) {
		if (item>=getNumItems()) //ERROR MESSAGE
			return 0;
		else if (N>=numNodes) {
			return 0;
		}
		else
			return ((DoubleArray)items.elementAt(item)).getValue(N);
	}
	/*.........................................ContinuousAdjustable...............*/
	public double getState (int N) {
		return firstItem.getValue(N);
	}
	/*.........................................ContinuousAdjustable...............*/
	public void setState (int N, int item, double d) {
		((DoubleArray)items.elementAt(item)).setValue(N, d);
	}
	/*.........................................ContinuousAdjustable...............*/
	public void setState (int N, double d) {
		firstItem.setValue(N, d);
	}
	/*.........................................ContinuousAdjustable...............*/
	public void setCharacterState (int N, CharacterState cs) {
		if (checkIllegalNode(N, 7))
			return;
		if (cs == null || !(cs instanceof ContinuousState))
			return; 
		for(int i=0; i<getNumItems(); i++)
			setState(N, i, ((ContinuousState)cs).getValue(i));
	}
	/*..........................................  ContinuousAdjustable ..................................................*/
	/**Trade states of nodes it and it2*/
	public void tradeStatesBetweenTaxa(int it, int it2) {
		if (checkIllegalNode(it, 9128) && checkIllegalNode(it, 9129))
			return;
		for (int item=0; item<getNumItems(); item++) {
			DoubleArray matrix = getItem(item);
			double temp = matrix.getValue(it);
			matrix.setValue(it, matrix.getValue(it2));
			matrix.setValue(it2, temp);
		}
	}
	/*.........................................ContinuousAdjustable...............*/
	/** This readjust procedure can be called to readjust the size of storage of
	states of a character for nodes. */
	public AdjustableDistribution adjustSize(Taxa taxa) {
		if (taxa.getNumTaxa() == this.getNumTaxa())
			return this;
		else {
			ContinuousAdjustable soc = new ContinuousAdjustable(taxa, taxa.getNumTaxa());
			soc.setItemsAs(this);
			soc.setParentData(getParentData());
			soc.setParentCharacter(getParentCharacter());
			((CharacterStates)soc).setExplanation(getExplanation());
			return soc;
		}
	}
/**/
}



