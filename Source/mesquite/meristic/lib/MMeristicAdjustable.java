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
public class MMeristicAdjustable  extends MMeristicDistribution implements MAdjustableDistribution {
	int numTaxa=0;
	int numChars=0;
	protected Integer2DArray firstMatrix; //other dimension for items (sample size, variance, etc., etc.)
	protected Vector matrices;
	String annotation;
	public MMeristicAdjustable (Taxa taxa, int numChars, int numTaxa) {
		super(taxa);
		this.numTaxa= numTaxa;
		this.numChars= numChars;
		matrices = new Vector(0);
		firstMatrix = new Integer2DArray(numChars, numTaxa); //no items yet
		matrices.addElement(firstMatrix);
	}
	
	public MMeristicAdjustable (Taxa taxa) {
		super(taxa);
		matrices = new Vector(0);
	}
	public MMeristicAdjustable () {
		super(null);
		matrices = new Vector(0);
	}
	public void setAnnotation(String s, boolean notify){
		annotation = s;
	}
	public String getAnnotation(){
		return annotation;
	}
	/*..........................................MMeristicAdjustable................*/
	/**sets the parent CharacterData from which this CharacterDistribution is derived or related*/
	public void setParentData (CharacterData cd){
		data = cd;
	}
	/*..........................................MMeristicAdjustable................*/
	public void setSize(int numChars, int numTaxa) {
		if (numChars!= this.numChars || numTaxa !=this.numTaxa) {
			this.numTaxa= numTaxa;
			this.numChars= numChars;
			matrices = new Vector(0);
			firstMatrix = new Integer2DArray(numChars, numTaxa); //no items yet
			matrices.addElement(firstMatrix);
			MeristicData data = (MeristicData)getParentData();
			if (data!=null) {
				for (int i=0; i<data.getNumItems(); i++)
					establishItem(data.getItemReference(i));
			}
		}
	}
	/*..........................................MMeristicAdjustable................*/
	public Integer2DArray addItem(NameReference nr){
		Integer2DArray matrix = new Integer2DArray(numChars, numTaxa);
		if (firstMatrix == null)
			firstMatrix = matrix;
		if (nr!=null)
			matrix.setNameReference(nr);
		matrix.deassignArray();
		matrices.addElement(matrix);
		return matrix;
	}
	public Integer2DArray addItem(String name){
		return addItem(NameReference.getNameReference(name));
	}
	public Integer2DArray establishItem(String name){
		return establishItem(NameReference.getNameReference(name));
	}
	/*..........................................MMeristicAdjustable................*/
	/** Make an item with the passed name.  If the an item already made is not yet named,
	then this will simply use this item and give it a new name.  Otherwise, a new
	matrix is made for a new item, and the name is attached to it.*/
	public Integer2DArray establishItem(NameReference nr){
		if (firstMatrix != null && firstMatrix.getNameReference()==null  && matrices.size() ==1) {
			if (nr!=null)
				firstMatrix.setNameReference(nr);
			return firstMatrix;
		}
		else {
			for (int i=0; i<getNumItems(); i++) {
				Integer2DArray matrix = (Integer2DArray)matrices.elementAt(i);
				if (matrix.getNameReference()== null) {
					matrix.setNameReference(nr);
					return matrix;
				}
			}
			return addItem(nr);
		}
	}
	/*..........................................MMeristicAdjustable................*/
	public int getNumberOfItems(){
		return matrices.size();
	}
	/*..........................................MMeristicAdjustable................*/
	public Integer2DArray getItem(int index){
		if (index<0 || index >= getNumItems())
			return null;
		return (Integer2DArray)matrices.elementAt(index);
	}
	/*..........................................MMeristicEmbedded................*/
	public String getItemName(int index){
		if (index<0 || index >= getNumItems())
			return null;
		return ((Integer2DArray)matrices.elementAt(index)).getName();
	}
	/*..........................................MMeristicAdjustable................*/
	public int getItemNumber(NameReference nr){
		if (nr==null)
			return -1;
		for (int i=0; i<getNumItems(); i++)
			if (nr.equals(((Integer2DArray)matrices.elementAt(i)).getNameReference()))
				return i;
		return -1;
	}
	/*..........................................MMeristicAdjustable................*/
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
	/*..........................................MMeristicAdjustable................*/
	/** Removes item.*/
	public void removeItem(int item){
		if (item<0 || item> getNumItems()-1)
			return;
		Integer2DArray matrix = getItem(item);
		if (matrix==null)
			return;
		matrices.removeElement(matrix);
		if (item == 0) {
			firstMatrix = (Integer2DArray)matrices.elementAt(0);
		}
	}
	/*..........................................MMeristicAdjustable................*/
	/**Sets the name reference for the item number "index".*/
	public void setItemReference(int index, NameReference nr){
			Integer2DArray matrix =getItem(index);
			matrix.setNameReference(nr);
	}
	/*..........................................MMeristicAdjustable................*/
	public void deassignStates(){
		for (int i=0; i<getNumItems(); i++){
			Integer2DArray matrix = getItem(i);
			if (matrix !=null)
				for (int k=0; k<numChars; k++)
					for (int j=0; j<numTaxa; j++)
						matrix.setValue(k, j, MeristicState.unassigned); 
		}
	}
	
	/*..........................................MMeristicAdjustable................*/
	/** obtain the states of character ic from the given CharacterDistribution*/
	public void transferFrom(int ic, CharacterDistribution s) { 
		if (s instanceof MeristicDistribution) {
			setItemsAs(((MeristicDistribution)s));
			for (int item=0; item<((MeristicDistribution)s).getNumItems(); item++) {
				Integer2DArray matrix = getItem(item);
				if (matrix !=null)
					for (int j=0; j<getNumNodes(); j++)
						matrix.setValue(ic, j,  ((MeristicDistribution)s).getState(j, item));
			}
		}
	}
	
	/*..........................................MMeristicAdjustable................*/
	public int getState (int ic, int N, int item) {
		
		if (getItem(item)!=null)
			return getItem(item).getValue(ic, N);
		else
			return MesquiteInteger.unassigned;
	}
	/*..........................................MMeristicAdjustable................*/
	public void setStates (Vector matrices) {
		if (matrices==null)
			return;
		this.matrices = matrices;
		firstMatrix = (Integer2DArray)matrices.elementAt(0);
		if (firstMatrix !=null) {
			numTaxa = firstMatrix.getSizeT();
			numChars = firstMatrix.getSizeC();
		}
		
	}
	/*..........................................MMeristicAdjustable................*/
	public void setStates (Integer2DArray matrix) {
		if (matrix==null)
			return;
		matrices.removeAllElements();
		matrices.addElement(matrix);
		firstMatrix = matrix;
		if (firstMatrix !=null) {
			numTaxa = firstMatrix.getSizeT();
			numChars = firstMatrix.getSizeC();
		}
		
	}
	/*..........................................MMeristicAdjustable................*/
	public void setState (int ic, int N,  int item, int d) {  //THIS SHOULD BE ic, it not it, ic
		if (item>= matrices.size())
			return;
		Integer2DArray matrix = (Integer2DArray)matrices.elementAt(item);
		matrix.setValue(ic,N, d);
	}
	/*..........................................MMeristicAdjustable................*/
	public int getState (int ic, int N) {
		return firstMatrix.getValue(ic, N);
	}
	/*..........................................MMeristicAdjustable................*/
	public void setState (int ic, int N, int d) {
		firstMatrix.setValue(ic,N, d);
	}
	
	/*..........................................  MMeristicAdjustable  ..................................................*
	/**sets the character state of character ic and taxon it to that in the passed CharacterState*
   	public void setCharacterState(CharacterState s, int ic, int it){
   		if (s instanceof MeristicState){
   			MeristicState cs = ((MeristicState)s);
			for (int item=0; item<getNumItems() && item<cs.getNumItems(); item++) {
				Integer2DArray matrix = (Integer2DArray)matrices.elementAt(item);
				matrix.setValue(ic, it, cs.getValue(item));
			}
   		}
   	}
	/*..........................................MMeristicAdjustable................*/
   	/** sets the state of character ic in taxon it from CharacterState cs.  If incoming CharacterState has more items
   	or items with names not seen in this MeristicData, then new items are created in the MeristicData*/
   	public  void setCharacterState(CharacterState cs, int ic, int it){
   		
   		if (cs !=null && cs instanceof MeristicState) {
   			MeristicState contS = (MeristicState)cs;
   			if (contS.getNumItems()==1 && getNumItems()==1 && (contS.getItemReference(0) == null || getItemReference(0) == null || contS.getItemReference(0) == getItemReference(0))){
	   			setState(ic, it, 0, contS.getValue(0));  
   			}
   			else {
	   			int made = 0;
	   			for (int i=0; i<contS.getNumItems(); i++) {
	   				NameReference nr = contS.getItemReference(i);
	   				if (nr != null && getItemNumber(nr)<0){ //named item not found; make new one
	   					establishItem(nr.getValue());
	   					made++;
	   				}
	   			}
	   			int numToMake = contS.getNumItems() - getNumItems();
	   			for (int i=0; i< numToMake; i++)
	   				addItem((NameReference)null);
	   			int currentOpen = 0;
	   			//unnamed items in incoming character state will be applied to first available items whose names are not in incoming character state
	   			for (int i=0; i<contS.getNumItems(); i++) {
	   				NameReference nr = contS.getItemReference(i);
	   				if (getItemNumber(nr)>=0) {//named item found; set state
	   					setState(ic, it, getItemNumber(nr), contS.getValue(i));  
	   				}
	   				else { //put it in next slot in this that has no corresponding in contS
	   					boolean found = false;
	   					for (int item = currentOpen; item<getNumItems() && !found; item++){
	   						NameReference nrM = getItemReference(item);
	   						if (nrM == null || contS.getItemNumber(nrM) < 0){
	   							setState(ic, it, item, contS.getValue(i));
	   							currentOpen = item+1;
	   							found = true;
	   						}
	   					}
	   				}
	   			}
   			}
  		}
   	}
	/*..........................................MMeristicAdjustable................*/
	public void tradeStatesBetweenTaxa(int ic, int it, int it2) { 
		if (checkIllegalNode(it, 9132) && checkIllegalNode(it, 9133) && ic <numChars)
			return;
		for (int item=0; item<getNumItems(); item++) {
			Integer2DArray matrix = (Integer2DArray)matrices.elementAt(item);
			int temp = matrix.getValue(ic, it);
			matrix.setValue(ic, it, matrix.getValue(ic,it2));
			matrix.setValue(ic, it2, temp);
		}
	}
	/*..........................................MMeristicAdjustable................*/
	public CharacterDistribution getCharacterDistribution (int ic) {
		MeristicAdjustable soc = new MeristicAdjustable(getTaxa(), numTaxa);
		soc.setItemsAs(this);
		for (int it = 0; it<numTaxa; it++) {
			for (int item=0; item<getNumItems(); item++)
				soc.setState(it, item, getState(ic, it, item)); 
		}
		if (getParentData()!=null)
			soc.setName("Character " + ic + " of " + getParentData().getName());
		else
			soc.setName("Character " + ic );
		return soc;
	}
	public int getNumTaxa(){
		return numTaxa;
	}
	public int getNumNodes(){ //TODO: a distinction should be made in Histories between taxa and nodes
		return numTaxa;
	}
	public int getNumChars(){
		return numChars;
	}
}


