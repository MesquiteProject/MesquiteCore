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
import java.util.zip.*;

import mesquite.categ.lib.CategoricalData;
import mesquite.categ.lib.CategoricalState;
import mesquite.categ.lib.DNAData;
import mesquite.lib.duties.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
/* ======================================================================== */
/** A subclass of CharacterData for continuous-valued data.  Can handle more than one item per matrix of cell
(e.g., for mean, variance, etc.)  Each item is stored in a separate matrix; new items can be added by adding matrices.   Items are refered to by
their name (e.g., MEAN, VARIANCE, etc.); each item has a NameReference associated to speed up queries. NOTE: At present,
the support for multiple items is only just beginning to be implemented, and is fragmentary.  Many methods currently use just the first item.*/
public class ContinuousData extends CharacterData implements ItemContainer {
	private Double2DArray firstMatrix;
	public static String DATATYPENAME="Continuous Data";
	protected Vector matrices;
	private static Vector defaultModels;
	static {
		defaultModels= new Vector(2);
	}
	public ContinuousData(CharMatrixManager manager, int numTaxa, int numChars, Taxa taxa){
		super(manager, numTaxa, numChars,taxa);
		setUseDiagonalCharacterNames(true);
		firstMatrix = new Double2DArray(numChars, numTaxa);
		matrices = new Vector(1);
		matrices.addElement(firstMatrix);
		firstMatrix.deassignArray();
	}
	public String getDefaultIconFileName(){ //for small 16 pixel icon at left of main bar
		return "matrixContSmall.gif";
	}
	/*..........................................  ContinuousData  ..................................................*/
	public void dispose(){
		super.dispose();
		if (matrices!=null){ //update size of specification sets
			for (int i=0; i<matrices.size(); i++) {
				Double2DArray sv = (Double2DArray)matrices.elementAt(i);
				sv.dispose();
			}
		}
		matrices = null;
	}
	/*..........................................  ContinuousData  ..................................................*/
	/** copy the basic data.  Does not copy the associated specs sets etc.*/
	public  void copyData(CharacterData sourceData, boolean allowDifferentSizes){
		if (sourceData==null)
			return;
		if ((sourceData.getNumTaxa()==getNumTaxa() && sourceData.getNumChars() == getNumChars()) || allowDifferentSizes) {
			ContinuousData cD = (ContinuousData)sourceData;
			int nItems = getNumItems();
			setItemsAs(cD);
			for (int i=0; i<nItems; i++){
				Double2DArray imatrix = null;
					imatrix = cD.getItem(i);
				Double2DArray currentMatrix= getItem(i);
				for (int ic = 0; ic< numChars; ic++)
					for (int it = 0; it<numTaxa; it++)
						if (it>=sourceData.getNumTaxa() || ic>=sourceData.getNumChars())
							currentMatrix.setValue(ic, it, ContinuousState.unassigned); 
						else
							currentMatrix.setValue(ic,it,imatrix.getValue(ic,it));
			}
		}
	}

	public void copyDataBlock(CharacterData sourceData, int icStart, int icEnd, int itStart, int itEnd){
		ContinuousData cD = (ContinuousData)sourceData;
		setItemsAs(cD);
		int nItems = getNumItems();
		for (int i=0; i<nItems; i++){
			Double2DArray imatrix = null;
			imatrix = cD.getItem(i);
			Double2DArray currentMatrix= getItem(i);
			for (int ic=icStart; ic<=icEnd; ic++){
				for (int it=itStart; it<=itEnd; it++) {
					int itSource = it-itStart;
					int icSource = ic-icStart;
					if (itSource<sourceData.getNumTaxa() || icSource<sourceData.getNumChars())
						currentMatrix.setValue(ic,it,imatrix.getValue(icSource,itSource));
				}
			}
		}
		resetCellMetadata();

	}

	/*..........................................  ContinuousData  ..................................................*/
	/** copy the basic data.  Does not copy the associated specs sets etc.*/
	public  void copyData(CharacterData sourceData){
		copyData(sourceData,false);
	}
	/*..........................................ContinuousData................*/
	/** clones this ContinousData.  Does not include associated specsets etc. */
	public CharacterData cloneData(){
	
		ContinuousData cD = (ContinuousData)makeCharacterData();
		int nItems = getNumItems();
		cD.setItemsAs(this);
		for (int i=0; i<nItems; i++){
			Double2DArray imatrix = null;
			imatrix = cD.getItem(i);
			Double2DArray oldMatrix= getItem(i);
			for (int ic = 0; ic< numChars; ic++)
				for (int it = 0; it<numTaxa; it++)
					imatrix.setValue(ic,it, oldMatrix.getValue(ic,it));
		}
		for (int ic = 0; ic< numChars; ic++)
			if (getSelected(ic))
				cD.setSelected(ic, true);

		return cD;
	}

	/*..........................................  CategoricalData  ..................................................*/
	/**clone a portion of CharacterData and return new copy.  Does not clone the associated specs sets etc.*/ //TODO: here should use super.setToClone(data) to handle specssets etc.???
	public CharacterData cloneDataBlock(int icStart, int icEnd, int itStart, int itEnd){
		int blockChars = icEnd-icStart+1;
		int blockTaxa = itEnd-itStart+1;
		boolean[] taxaToClone = new boolean[getNumTaxa()];
		for (int it=0; it<getNumTaxa(); it++) {
			taxaToClone[it] = it>=itStart && it<=itEnd;
		}
		Taxa taxa = getTaxa().cloneTaxa(taxaToClone);

		ContinuousData cD = new ContinuousData(getMatrixManager(), blockTaxa, blockChars, taxa);
		int nItems = getNumItems();
		cD.setItemsAs(this);
		for (int i=0; i<nItems; i++){
			Double2DArray imatrix = null;
				imatrix = cD.getItem(i);
			Double2DArray oldMatrix= getItem(i);
			for (int ic=icStart; ic<=icEnd; ic++)
				for (int it = itStart; it<=itEnd; it++)
					imatrix.setValue(ic-icStart,it-itStart,oldMatrix.getValue(ic,it));
		}
		for (int ic = 0; ic< numChars; ic++)
			if (getSelected(ic))
				cD.setSelected(ic-icStart, true);

		return cD;
	}

	public int getNumTaxa(){
		return getNumTaxa(true);
	}
	public int getNumTaxa(boolean notifyIfError){
		if (disposed)
			return 0;
		int n = super.getNumTaxa(notifyIfError);
		if (!notifyIfError)
			return n;
		if (firstMatrix == null)
			dataIntegrityReportableAlert(getDataTypeName() + " with null internal matrix. getNumTaxa() = " + n);
		else if (firstMatrix.getSizeT() != n)
			dataIntegrityReportableAlert("Continuous matrix with incorrect record of number of taxa. getNumTaxa() = " + n + " firstMatrix.getSizeT() " + firstMatrix.getSizeT());
		return n;
	}
	public int getNumChars(){
		return getNumChars(true);
	}
	public int getNumChars(boolean notifyIfError){
		if (disposed)
			return 0;
		int n = super.getNumChars(notifyIfError);
		if (firstMatrix == null){
			if (notifyIfError)
				dataIntegrityReportableAlert(getDataTypeName() + " with null internal matrix. getNumChar() = " + n  + " nAdd = " + nAdd + " nDel = " + nDel + " nMove = " + nMove);
			return 0;
		}
		else if (firstMatrix.getSizeC() < n){
			if (notifyIfError)
				dataIntegrityReportableAlert("Continuous matrix with incorrect record of number of characters. getNumChar() = " + n + " firstMatrix.getSizeC() " + firstMatrix.getSizeC() + " nAdd = " + nAdd + " nDel = " + nDel + " nMove = " + nMove);
			return firstMatrix.getSizeC();
		}
		else if (firstMatrix.getSizeC() > n){
			if (notifyIfError)
				dataIntegrityReportableAlert("Continuous matrix with incorrect record of number of characters. getNumChar() = " + n + " firstMatrix.getSizeC() " + firstMatrix.getSizeC() + " nAdd = " + nAdd + " nDel = " + nDel + " nMove = " + nMove);
			return n;
		}
		return n;
	}
	/*..........................................ContinuousData................*/
	/** Removes item.*/
	public void removeItem(int item){
		if (item<0 || item> getNumItems()-1)
			return;
		Double2DArray matrix = getItem(item);
		if (matrix==null)
			return;
		matrices.removeElement(matrix);
		if (item == 0) {
			firstMatrix = (Double2DArray)matrices.elementAt(0);
		}
	}
	/*..........................................ContinuousData................*/
	/** Make an item with the passed name.  If the an item already made is not yet named,
	then this will simply use this item and give it a new name.  Otherwise, a new
	matrix is made for a new item, and the name is attached to it.*/
	public Double2DArray establishItem(String name){

		setDirty(true);
		incrementStatesVersion();
		if (firstMatrix != null && firstMatrix.getNameReference()==null && name!=null && matrices.size() ==1) {
			firstMatrix.setNameReference(NameReference.getNameReference(name));
			return firstMatrix;
		}
		else {
			for (int i=0; i<getNumItems(); i++) {
				Double2DArray matrix = (Double2DArray)matrices.elementAt(i);
				if (matrix.getNameReference()== null) {
					matrix.setNameReference(NameReference.getNameReference(name));
					return matrix;
				}
			}

			return addItem(name);
		}
	}
	/*..........................................ContinuousData................*/
	/** Make an item with the passed name.  If the first item already made is not yet named and if it is the only item so far,
	then makeItem is assumed to be simply applying a name to this first item, and no new item is made.  Otherwise, a new
	matrix is made for a new item, and the name is attached to it.*/
	public Double2DArray addItem(String name){
		setDirty(true);
		incrementStatesVersion();
		Double2DArray matrix = new Double2DArray(numChars, numTaxa);
		if (firstMatrix == null)
			firstMatrix = matrix;
		matrix.setNameReference(NameReference.getNameReference(name));
		matrix.deassignArray();
		matrices.addElement(matrix);
		return matrix;
	}
	/*..........................................ContinuousData................*/
	/**Get the name reference for the item of name "name".  This is done so that subsequent calls can compare name references,
	which may be identical thus saving the effort of checking strings*/
	public NameReference getItemReference(String name){
		NameReference nr = NameReference.getNameReference(name);
		if (nr ==null) return null; //added 8 Dec 01
		for (int i=0; i<getNumItems(); i++) {
			Double2DArray matrix = (Double2DArray)matrices.elementAt(i);
			if (nr.equals(matrix.getNameReference()))
				return matrix.getNameReference();
		}
		return null;

	}
	/*..........................................ContinuousData................*/
	/**Get the name reference for the item number "index".  This is done so that subsequent calls can compare name references,
	which may be identical thus saving the effort of checking strings*/
	public NameReference getItemReference(int index){
		Double2DArray matrix = (Double2DArray)matrices.elementAt(index);
		if (matrix ==null) return null; //added 8 Dec 01
		return matrix.getNameReference();
	}
	/*..........................................ContinuousState................*/
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
	/**Sets the name reference for the item number "index".*/
	public void setItemReference(int index, NameReference nr){
		Double2DArray matrix = (Double2DArray)matrices.elementAt(index);
		if (matrix ==null) return; //added 8 Dec 01
		matrix.setNameReference(nr);
		setDirty(true);
	}
	/*..........................................ContinuousData................*/
	/**Get the name reference for the item whose name matches the name in the passed NameReference.  This is done so that subsequent calls can compare name references,
	which may be identical thus saving the effort of checking strings*/
	public NameReference getItemReference(NameReference nr){
		if (nr ==null) return null; //added 8 Dec 01
		for (int i=0; i<getNumItems(); i++) {
			Double2DArray matrix = (Double2DArray)matrices.elementAt(i);
			if (matrix !=null && nr.equals(matrix.getNameReference())) //modified 8 Dec 01
				return matrix.getNameReference();
		}
		return null;

	}
	/*..........................................ContinuousData................*/
	/** Get the item name of the item number "index" */
	public String getItemName(int index){
		Double2DArray matrix = null;
		if (matrices == null)
			matrix = firstMatrix;
		else if (index< matrices.size())
			matrix = (Double2DArray)matrices.elementAt(index);
		if (matrix ==null) return null; //added 8 Dec 01

		if (matrix.getNameReference()==null)
			return null;
		else
			return matrix.getNameReference().getValue();
	}
	/*..........................................ContinuousData................*/
	/** Get which item has name matching the reference */
	public int getItemNumber(NameReference nr){
		if (nr==null)
			return -1;
		for (int i=0; i<getNumItems(); i++)
			if (nr.equals(((Double2DArray)matrices.elementAt(i)).getNameReference()))
				return i;
		return -1;
	}
	/*..........................................ContinuousData................*/
	/** get the number of items. */
	public int getNumItems(){
		if (matrices == null)
			return 0;
		return matrices.size();
	}
	public String getCellContentsDescription(){
		boolean nameFound = false;
		for (int i=0; i<getNumItems() && !nameFound; i++)
			nameFound= !StringUtil.blank(getItemName(i));
		if (!nameFound)
			return null;
		String descr = "(";
		boolean first = true;
		for (int i=0; i<getNumItems(); i++) {
			String iN = getItemName(i);
			if (StringUtil.blank(iN))
				iN = "unnamed";
			if (first) {
				descr += iN;
				first = false;
			}
			else
				descr += ", " + iN;
		}
		return descr + ")";
	}
	/*..........................................ContinuousData................*/
	/**Adds num characters after position "starting"; returns true iff successful.*/
	public  boolean addParts(int starting, int num){
		if (getMaxNumChars()!=MesquiteInteger.infinite && numChars+num>getMaxNumChars())
			return false;
		if (starting<0)
			starting = -1;
		else if (starting>=numChars)
			starting = numChars-1;
		int newNumChars = numChars + num;
		Vector newMatrices = new Vector();
		for (int item = 0; item<getNumItems(); item++){
			Double2DArray newMatrix = new Double2DArray(newNumChars, numTaxa);
			if (item == 0)
				firstMatrix = newMatrix;
			Double2DArray oldMatrix = ((Double2DArray)matrices.elementAt(item));
			newMatrix.setNameReference(oldMatrix.getNameReference());
			for (int it=0; it<numTaxa; it++) {
				for (int ic=0; ic<=starting; ic++)
					newMatrix.setValue(ic,it,oldMatrix.getValue(ic,it)); //transferring old first part
				for (int ic=0; ic<num; ic++)
					newMatrix.setValue(ic + starting +1,it,ContinuousState.unassigned); //filling new part with missing data
				for (int ic=0; ic<numChars - starting -1 ; ic++)
					newMatrix.setValue(ic + starting + num + 1,it,oldMatrix.getValue(starting + ic+1,it)); //transferring old second part
			}
			newMatrices.addElement(newMatrix);
		}
		matrices = newMatrices;
		return super.addParts(starting, num);
	}
	/*..........................................ContinuousData................*/
	/**Deletes num characters from position "starting"; returns true iff successful.*/
	public boolean deleteParts(int starting, int num){
		if (num<=0)
			return false;
		if (starting<0)
			return false;
		else if (starting>numChars)
			return false;
		if (num+starting>numChars)
			num = numChars-starting;
		int newNumChars =numChars-num;

		Vector newMatrices = new Vector();
		for (int item = 0; item<getNumItems(); item++){
			Double2DArray newMatrix = new Double2DArray(newNumChars, numTaxa);
			if (item == 0)
				firstMatrix = newMatrix;
			Double2DArray oldMatrix = ((Double2DArray)matrices.elementAt(item));
			newMatrix.setNameReference(oldMatrix.getNameReference());
			for (int ic=0; ic<starting; ic++){
				for (int it=0; it<numTaxa; it++) 
					newMatrix.setValue(ic,it,oldMatrix.getValue(ic,it)); //transferring old first part
			}
			for (int ic=starting + num; ic<numChars ; ic++){
				for (int it=0; it<numTaxa; it++) 
					newMatrix.setValue(ic-num,it,oldMatrix.getValue(ic,it)); //transferring old second part
			}
			newMatrices.addElement(newMatrix);
		}
		matrices.removeAllElements();
		matrices = newMatrices;
		return super.deleteParts(starting, num);
	}
	/*..........................................ContinuousData................*/
	/**swaps characters first and second.*/
	public boolean swapParts(int first, int second){
		if (first<0 || first >= numChars)
			return false;
		if (second<0 || second >= numChars)
			return false;
		for (int item = 0; item<getNumItems(); item++){
			Double2DArray oldMatrix = ((Double2DArray)matrices.elementAt(item));
			Double2DArray.swapColumns(oldMatrix.getMatrix(), first, second);
		}
		return super.swapParts(first, second);
	}
	/*..........................................ContinuousData................*/
	/**moves num characters from position "first" to just after position "justAfter"; returns true iff successful.*/
	public boolean moveParts(int starting, int num, int justAfter){
		if (!canMoveChars()) 
			return false;
		if (justAfter<0)
			justAfter = -1;
		else if (justAfter>=numChars)
			justAfter = numChars-1;
		if (starting<0 || starting >= numChars)
			return false;
		if (num<=0)
			return false;
		if (starting + num>numChars)
			return false;

		for (int item = 0; item<getNumItems(); item++){
			Double2DArray oldMatrix = ((Double2DArray)matrices.elementAt(item));
			Double2DArray.moveColumns(oldMatrix.getMatrix(), starting, num, justAfter);
		}
		return super.moveParts(starting, num, justAfter);
	}
	/*..........................................ContinuousData................*/
	/**Adds num taxa after position "starting"; returns true iff successful.*/
	public boolean addTaxa(int starting, int num){
		setDirty(true);
		if (starting<0)
			starting = -1;
		else if (starting>=numTaxa)
			starting = numTaxa-1;
		int newNumTaxa = numTaxa + num;

		Vector newMatrices = new Vector();
		for (int item = 0; item<getNumItems(); item++){
			Double2DArray newMatrix = new Double2DArray(numChars, newNumTaxa);
			if (item == 0)
				firstMatrix = newMatrix;
			Double2DArray oldMatrix = ((Double2DArray)matrices.elementAt(item));
			newMatrix.setNameReference(oldMatrix.getNameReference());
			for (int ic=0; ic<numChars; ic++){
				for (int it=0; it<=starting; it++)
					newMatrix.setValue(ic,it,oldMatrix.getValue(ic,it)); //transferring old first part

				for (int it=0; it<num; it++) 
					newMatrix.setValue(ic,it + starting +1,ContinuousState.unassigned); //filling new part with missing data

				for (int it=0; it<numTaxa - starting -1; it++) 
					newMatrix.setValue(ic,it + starting + num + 1,oldMatrix.getValue(ic,starting + it+1)); //transferring old second part
			}
			newMatrices.addElement(newMatrix);
		}
		if (matrices!=null)
			matrices.removeAllElements();
		matrices = newMatrices;
		numTaxa = newNumTaxa; 
		incrementStatesVersion();
		return super.addTaxa(starting, num);
	}
	/*..........................................ContinuousData................*/
	/**Deletes num taxa from position "starting"; returns true iff successful.*/
	public boolean deleteTaxa(int starting, int num){
		setDirty(true);

		if (num<=0)
			return false;
		if (starting<0)
			return false;
		else if (starting>=numTaxa)
			return false;
		if (num+starting>numTaxa)
			num = numTaxa-starting;
		int newNumTaxa = numTaxa - num;

		Vector newMatrices = new Vector();
		for (int item = 0; item<getNumItems(); item++){
			Double2DArray newMatrix = new Double2DArray(numChars, newNumTaxa);
			if (item == 0)
				firstMatrix = newMatrix;
			Double2DArray oldMatrix = ((Double2DArray)matrices.elementAt(item));
			newMatrix.setNameReference(oldMatrix.getNameReference());
			for (int ic=0; ic<numChars; ic++){
				for (int it=0; it<starting; it++)
					newMatrix.setValue(ic,it,oldMatrix.getValue(ic,it)); //transferring old first part

				for (int it=starting+num; it<numTaxa; it++) 
					newMatrix.setValue(ic,it-num,oldMatrix.getValue(ic,it)); //transferring old second part
			}
			newMatrices.addElement(newMatrix);
		}
		matrices.removeAllElements();
		matrices = newMatrices;
		numTaxa = newNumTaxa; 
		incrementStatesVersion();
		return super.deleteTaxa(starting, num);
	}
	/*..........................................ContinuousData................*/
	/**moves num taxa from position "starting" to just after position "justAfter"; returns true iff successful.*/
	public boolean moveTaxa(int starting, int num, int justAfter){
		if (num<=0)
			return false;
		if (starting<0)
			return false;
		else if (starting>=numTaxa)
			return false;
		setDirty(true);
		for (int item = 0; item<getNumItems(); item++){
			Double2DArray matrixA = ((Double2DArray)matrices.elementAt(item));
			double[][] matrix = matrixA.getMatrix();
			for (int ic=0; ic<numChars; ic++)
				DoubleArray.moveParts(matrix[ic], starting, num, justAfter);
		}
		incrementStatesVersion();
		return super.moveTaxa(starting, num, justAfter);
	}

	/*..........................................ContinuousData................*/
	/** Indicates the type of character stored */ 
	public Class getStateClass(){
		return ContinuousState.class;
	}
	/** returns the name of the type of data stored */
	public String getDataTypeName(){
		return ContinuousData.DATATYPENAME;
	}

	/*..........................................ContinuousData................*/
	public Double2DArray getItem(int i){
		if (matrices !=null && i>=0 && i<matrices.size())
			return (Double2DArray)matrices.elementAt(i);
		return null;
	}
	/*..........................................ContinuousData................*/
	private Double2DArray getItem(NameReference nr){
		if (matrices == null)
			return null;
		for (int i=0; i<matrices.size(); i++) {
			Double2DArray matrix = (Double2DArray)matrices.elementAt(i);
			NameReference nRm = matrix.getNameReference();
			if (nRm!=null && nRm.equals(nr))
				return matrix;
		}
		return null;
	}
	/*..........................................ContinuousData................*/
	/** trades the states of character ic and ic2 in taxon it.  Used for reversing sequences (for example).*/
	public void tradeStatesBetweenCharacters(int ic, int ic2, int it, boolean adjustCellLinked) { 
		if (ic>=numChars || it>=numTaxa || ic < 0 || it < 0)
			return;
		super.tradeStatesBetweenCharacters(ic, ic2, it, adjustCellLinked);
		for (int item=0; item<getNumItems(); item++) {
			Double2DArray matrix = getItem(item);
			double temp = matrix.getValue(ic, it);
			matrix.setValue(ic, it, matrix.getValue(ic2,it));
			matrix.setValue(ic2, it, temp);
		}
		incrementStatesVersion();
		setDirty(true, ic, it);
		setDirty(true, ic2, it);
	}
	/*..........................................ContinuousData................*/
	public void tradeStatesBetweenTaxa(int ic, int it, int it2) { 
		if (ic>=numChars && it>=numTaxa &&  it2 >=numTaxa) 
			return;
		super.tradeStatesBetweenTaxa(ic, it, it2);
		incrementStatesVersion();
		setDirty(true);
		for (int item=0; item<getNumItems(); item++) {
			Double2DArray matrix = getItem(item);
			double temp = matrix.getValue(ic, it);
			matrix.setValue(ic, it, matrix.getValue(ic,it2));
			matrix.setValue(ic, it2, temp);
		}
		setDirty(true, ic, it);
		setDirty(true, ic, it2);
	}
	/*..........................................ContinuousData................*/
	/** returns Color for state of character, scaled to maxState*/

	public Color getColorOfState(int ic, int istate, int maxState){
		return MesquiteColorTable.getDefaultColor(maxState,istate, MesquiteColorTable.COLORS_NO_BW); 
	}
	/*..........................................ContinuousData................*/
	public Color getColorOfState(int ic, int istate){
		return MesquiteColorTable.getDefaultColor(10, istate, MesquiteColorTable.COLORS_NO_BW);
	}
	/*..........................................ContinuousData................*/
	protected String statesToString(int ic, int it, boolean forDisplay) {
		String s = "";
		boolean first = true;
		for (int item = 0; item<getNumItems(); item++) {
			if (!first)
				s+=" ";
			first = false;
			double iState = getState(ic, it, item);
			if (iState == MesquiteDouble.unassigned)
				s += getUnassignedSymbol();
			else if (forDisplay && iState == MesquiteDouble.inapplicable)
				s += getInapplicableSymbol();
			else {
				if (forDisplay || !MesquiteDouble.isCombinable(iState))
					s +=  MesquiteDouble.toString(iState);
				else
					s +=  Double.toString(iState);
			}
		}
		if (getNumItems()>1)
			return "(" + s + ")";
		else
			return s;
	}

	/** appends to buffer string describing the state(s) of character ic in taxon it.*/
	public void statesIntoStringBuffer(int ic, int it, StringBuffer sb, boolean forDisplay){
		statesIntoStringBuffer(ic,it,sb,forDisplay,true,true);
	}
	/** appends to buffer string describing the state(s) of character ic in taxon it.*/
	public void statesIntoStringBuffer(int ic, int it, StringBuffer sb, boolean forDisplay, boolean includeInapplicable, boolean includeUnassigned){
		sb.append(statesToString(ic, it, forDisplay)); //TODO: use buffer
	}
	/** appends to buffer string describing the state(s) of character ic in taxon it.*/
	public void statesIntoNEXUSStringBuffer(int ic, int it, StringBuffer sb){
		sb.append(statesToString(ic, it, false));  //TODO: use buffer
	}
	float[] hsb = new float[3];
	/*..........................................  ContinuousData  ..................................................*/
	/** Gets the color representing state(s) of character ic in taxon it */ 
	public Color getColorOfStates(int ic, int it){

		double s = getState(ic, it, 0);
		if (ContinuousState.isCombinable(s)) {
			double minState=MesquiteDouble.unassigned;
			double maxState=MesquiteDouble.unassigned;
			for (int it2=0; it2<getNumTaxa(); it2++){
				maxState = MesquiteDouble.maximum(maxState, getState(ic, it2, 0));
				minState = MesquiteDouble.minimum(minState, getState(ic, it2, 0));
			}
			int place = (int)(((s-minState)/(maxState-minState))*10); 
			return getColorOfState(ic, place, 10);
		}
		else 
			return ColorDistribution.unassigned;
	}

	/*..........................................ContinuousData................*/
	public CharacterDistribution getCharacterDistribution(int ic){
		ContinuousEmbedded states =new ContinuousEmbedded(this, ic);
		return states;
	}
	/*..........................................  ContinuousData  ..................................................*/
	/** Fills matrix with data from passed StsOfCharacters object.*/
	public void setMatrix(MCharactersDistribution states){ 
		if (!(states instanceof MContinuousDistribution))
			return;
		setDirty(true);
		incrementStatesVersion();
		MContinuousDistribution cStates = (MContinuousDistribution)states;
		setItemsAs(cStates);
		for (int i = 0; i< getNumItems(); i++) {
			Double2DArray matrix =getItem(i);
			matrix.setValues(cStates.getItem(i));
		}
		stampHistoryChange();
	}
	/*..........................................  ContinuousData  ..................................................*/
	/**get copy of matrix and return as MCharactersDistribution */
	public MCharactersDistribution getMCharactersDistribution(){
		MContinuousEmbedded states = new MContinuousEmbedded(this);
		return states;
	}
	/*..........................................ContinuousData................*/
	public CharacterDistribution makeCharacterDistribution(){
		ContinuousAdjustable c = new ContinuousAdjustable(getTaxa(), numTaxa);
		c.setItemsAs(this);
		c.setParentData(this);
		return c;
	}
	/*..........................................ContinuousData................*/
	public CharacterState makeCharacterState() {
		ContinuousState cs = new ContinuousState();
		cs.setItemsAs(this);
		return cs;

	}
	/*..........................................ContinuousData................*/
	public CharacterData makeCharacterData() {
		ContinuousData data = new ContinuousData(getMatrixManager(), getNumTaxa(), getNumChars(), getTaxa());
		data.setItemsAs(this);
		return data;
	}
	/*..........................................ContinuousData................*/
	public CharacterData makeCharacterData(int ntaxa, int nchars) {
		ContinuousData data = new ContinuousData(getMatrixManager(), ntaxa, nchars,  getTaxa());
		data.setItemsAs(this);
		return data;
	}
	public CharacterData makeCharacterData(CharMatrixManager manager, Taxa taxa){ 
		ContinuousData data =  new ContinuousData(getMatrixManager(), taxa.getNumTaxa(), 0, taxa);
		data.setItemsAs(this);
		return data;
	}
	/*..........................................ContinuousData................*/
	/** If incoming CharacterState has more items
   	or items with names not seen in this ContinuousData, then new items are created in the ContinuousData*
   	public  void reconcileItems(CharacterState cs){

   		if (cs !=null && cs instanceof ContinuousState) {
   			ContinuousState contS = (ContinuousState)cs;
   			if (contS.getNumItems()==1 && getNumItems()==1 && (contS.getItemReference(0) == null || getItemReference(0) == null || contS.getItemReference(0) == getItemReference(0))){
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
	   				addItem(null);
   			}
  		}
   	}
	/*..........................................ContinuousData................*/
	/** sets the state of character ic in taxon it from CharacterState cs.  If incoming CharacterState has more items
   	or items with names not seen in this ContinuousData, then new items are created in the ContinuousData*/
	public  void setState(int ic, int it, CharacterState cs){

		if (cs !=null && cs instanceof ContinuousState) {
			ContinuousState contS = (ContinuousState)cs;
			if (contS.getNumItems()==1 && getNumItems()==1 && NameReference.equal(contS.getItemReference(0), getItemReference(0))){
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
					addItem(null);
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
	/*..........................................ContinuousData................*/
	public void setState(int ic, int it, int item, double states){
		if (ic<numChars && it<numTaxa && item<getNumItems()) {
			Double2DArray m = ((Double2DArray)matrices.elementAt(item));
			if (m.getValue(ic, it)!= states){
				m.setValue(ic, it, states);
				setDirty(true, ic, it);
				incrementStatesVersion();
			}
		}
	}
	/** sets the state of character ic in taxon it to the default state (which in some circumstances may be inapplicable, e.g. gaps for molecular data)*/
	public  void deassign(int ic, int it){
		setToUnassigned(ic, it);	
	}
	/*..........................................  ContinuousData  ..................................................*/
	/** sets the state of character ic in taxon it to inapplicable*/
	public  void setToInapplicable(int ic, int it){
		for (int item = 0; item<getNumItems(); item++){
			Double2DArray matrix = ((Double2DArray)matrices.elementAt(item));
					matrix.setValue(ic,it,ContinuousState.inapplicable); //filling with missing data
		}	
		setDirty(true, ic, it);
	}
	/*..........................................  ContinuousData  ..................................................*/
	/** sets the state of character ic in taxon it to unassigned*/
	public  void setToUnassigned(int ic, int it){
		for (int item = 0; item<getNumItems(); item++){
			Double2DArray matrix = ((Double2DArray)matrices.elementAt(item));
					matrix.setValue(ic,it,ContinuousState.unassigned); //filling with missing data
		}	
		setDirty(true, ic, it);
	}
	/*..........................................ContinuousData................*/
	public void setState(int ic, int it, String s){
		MesquiteMessage.warnProgrammer("Error: simple set state used in " + getDataTypeName());
		setState(ic, it, null, true, null);
	}
	/*..........................................    ..................................................*/
	public boolean legalValue(int ic, double states){
		return true;
	}
	/*..........................................ContinuousData................*/
	/** set the state of char ic and taxon it to that in string starting at position pos.  If pos==null,
	assume that whole string is relevant*/
	public int setState(int ic, int it, Parser parser, boolean fromEditor, MesquiteString result){ 
		if (ic>=numChars || ic < 0 || it>=numTaxa ||it<0) {
			if (result!=null)
				result.setValue("Character or taxon number out of bounds in setState " + ic + "  " + it);
			MesquiteMessage.printStackTrace("");
			return OUTOFBOUNDS;
		}
		if (parser==null)
			return ERROR;
		if (parser.blank()) {
			if (result!=null)
				result.setValue("String blank in setState");
			return ERROR;
		}
		StringBuffer s = parser.getBuffer();
		boolean dirt = false;
		if (s.length() == 1){
			char c= s.charAt(0);
			if (c ==getInapplicableSymbol()){
				for (int i=0; i<getNumItems(); i++) {
					if (getState(ic, it, i)!=MesquiteDouble.inapplicable) {
						setState(ic, it, i, MesquiteDouble.inapplicable);
						dirt = true;
					}
				}
				if (dirt)
					setDirty(true, ic, it);
				return OK;
			}
			else if (c ==getUnassignedSymbol()){
				for (int i=0; i<getNumItems(); i++) {
					if (getState(ic, it, i)!=MesquiteDouble.unassigned) {
						setState(ic, it, i, MesquiteDouble.unassigned);
						dirt = true;
					}
				}
				if (dirt)
					setDirty(true, ic, it);
				return OK;
			}
			else if (c ==getMatchChar()){
				for (int i=0; i<getNumItems(); i++) {
					if (getState(ic, it, i)!=getState(ic,  0, i)) {
						setState(ic, it, i, getState(ic,  0, i));
						dirt = true;
					}
				}
				if (dirt)
					setDirty(true, ic, it);
				return OK;
			}
		}
		boolean contin = false;
		boolean wasNull = false;
		if (fromEditor) { 
			parser.setPosition(0);
			contin = true;
			wasNull = true;
		}
		boolean done = false;
		int item = 0;
		String t = null;
		int prev = parser.getPosition();
		//MesquiteInteger pos = new MesquiteInteger(parser.getPosition()); //how to avoid this instantiation but be reentrant safe?
		while (!done){
			parser.setPosition(prev);
			t = parser.getNextToken();
			if ("(".equals(t)) {
				contin = true;
				prev = parser.getPosition();
			}
			if (StringUtil.blank(t)|| ")".equals(t)) {
				done = true;
				prev = parser.getPosition();
			}
			else  {
				if (!contin)
					done = true;
				parser.setPosition(prev);
				double d = MesquiteDouble.fromString(parser); 
				prev = parser.getPosition();
				if (d!=MesquiteDouble.impossible && legalValue(ic,d)) {
					if (d != getState(ic, it, item)) {
						setState(ic,it, item++,d);
						dirt = true;
					}
					else
						item++;
				}
				else {
					if (result != null) {
						result.setValue("Illegal state value for "+getDataTypeName());
						return ERROR;
					}
				}
				if (done)
					parser.setPosition(prev);
			}
		}
		if (dirt)
			setDirty(true, ic, it);
		return OK;
	}

	/*..........................................ContinuousData................*/
	/** returns the state of character ic in taxon it*/
	public  CharacterState getCharacterState(CharacterState cs, int ic, int it){
		if (cs==null || cs.getClass()!=ContinuousState.class) {
			cs =new ContinuousState();
		}
		((ContinuousState)cs).setItemsAs(this);
		for (int i=0; i< getNumItems(); i++) {
			((ContinuousState)cs).setValue(i, getState(ic, it, i));
		}
		return cs; 
	}

	/*..........................................ContinuousData................*/
	/** returns the state of character ic in taxon it*/
	public  double getState(int ic, int it, int item){
		if (matrices!=null && ic<numChars && it<numTaxa && item<getNumItems()) {
			Double2DArray da = ((Double2DArray)matrices.elementAt(item));
			if (da!=null)
				return da.getValue(ic, it);
		}
		return MesquiteDouble.unassigned;
	}
	/** returns a String summarizing the states of a character (e.g., "2 states", "0.1-0.9").*/
	/*..........................................ContinuousData................*/
	public String getStatesSummary(int ic, boolean selectedOnly){
		ContinuousState state=null;
		double max = MesquiteDouble.unassigned;
		double min = MesquiteDouble.unassigned;
		//ITEMS: ask if more than one item, and if so list column for each
		for (int it=0; it<numTaxa; it++) 
			if (!selectedOnly || getTaxa().getSelected(it)) {
				state = (ContinuousState)getCharacterState(state, ic, it);
				if (MesquiteDouble.greaterThan(state.getValue(0), max, 0))
					max = state.getValue(0);
				if (MesquiteDouble.lessThan(state.getValue(0), min, 0))
					min = state.getValue(0);
			}
		return MesquiteDouble.toString(min) + " - " + MesquiteDouble.toString(max);	
	}
	/*..........................................ContinuousData................*/
	public String getStatesSummary(int ic){
		return getStatesSummary(ic,false);
	}
	/*..........................................ContinuousData................*/
	/** returns whether the character ic is inapplicable to taxon it.   True if inapplicable in all items.*/
	public  boolean isInapplicable(int ic, int it){
		if (getNumItems()==1)
			return firstMatrix.getValue(ic,it)== ContinuousState.inapplicable; 
		else {
			for (int i=0; i< getNumItems(); i++) {
				if (!ContinuousState.isInapplicable(getState(ic, it, i)));
				return false;
			}
			return true;
		}
	}
	/*..........................................ContinuousData................*/
	/** returns whether the state of character ic is missing in taxon it.  True if unassigned in all items.*/
	public  boolean isUnassigned(int ic, int it){
		if (getNumItems()==1)
			return firstMatrix.getValue(ic,it)== ContinuousState.unassigned; 
		else {
			for (int i=0; i< getNumItems(); i++) {
				if (!ContinuousState.isUnassigned(getState(ic, it, i)));
				return false;
			}
			return true;
		}
	}
	/*..........................................ContinuousData................*/
	/** returns whether the state of character ic is missing in taxon it.  True if unassigned in all items.*/
	public  boolean isValid(int ic, int it){
		if (getNumItems()==1)
			return firstMatrix.getValue(ic,it)!= ContinuousState.impossible; 
		else {
			for (int i=0; i< getNumItems(); i++) {
				if (ContinuousState.isImpossible(getState(ic, it, i)))
				return false;
			}
			return true;
		}
	}
	/*..........................................ContinuousData................*/
	/** dump the matrix to the log*/
	public void logMatrix(){
		StringBuffer matrixString= new StringBuffer();
		for (int it=0; it<numTaxa; it++) {
			matrixString.append(getTaxa().getTaxon(it).getName() + "  ");
			for (int ic=0; ic<numChars; ic++)
				statesIntoStringBuffer(ic,it, matrixString, true); 
			matrixString.append(StringUtil.lineEnding());
		}
		MesquiteModule.mesquiteTrunk.logln(matrixString.toString());
	}

	/*.................................................................................................................*/
	public String getChecksumSummaryString(){
		ContinuousState state=null;
		double minValue = MesquiteDouble.unassigned;
		double maxValue = MesquiteDouble.unassigned;
		double sumSquares = 0;
		for (int ic = 0; ic<numChars; ic++)
			for (int it=0; it<numTaxa; it++) {
				state =(ContinuousState)getCharacterState(state, ic, it);
				if (!state.isUnassigned() && !state.isInapplicable())
					for (int item = 0; item <getNumItems(); item ++) {
						double value = state.getValue(item);
						minValue = MesquiteDouble.minimum(minValue, value);
						maxValue = MesquiteDouble.maximum(maxValue, value);
						if (MesquiteDouble.isCombinable(value))
								sumSquares += value*value;
					}
			}
		return "numChars " + getNumChars() + "   numItems " + getNumItems() + "   min " + minValue + "   max " + maxValue + "   sumSquares " + sumSquares;
	}
	/*..........................................ContinuousData................*/
	public long calculateChecksum(CRC32 crc32){
		byte[] bytes = new byte[8];
		crc32.reset();
		int numItems = getNumItems();
		for (int ic=0; ic<numChars; ic++)
			for (int it=0; it<numTaxa; it++) 
				for (int item=0; item<numItems; item++) {
					bytes = MesquiteNumber.doubleToBytes(getState(ic, it, item), bytes);
					for (int i = 0; i<8; i++)
						crc32.update(bytes[i]);
				}

		return crc32.getValue();
	}
	/*..........................................ContinuousData................*/
	/** returns default column width in editors..*/
	public int getDefaultColumnWidth() {
		return 70;
	}
	/*..........................................ContinuousData................*/
	public static DefaultReference findDefaultReference(NameReference paradigm){
		if (defaultModels == null) {
			MesquiteMessage.warnProgrammer("findDefaultReference with null default models ");
			MesquiteMessage.printStackTrace();
			return null;
		}
		for (int i=0; i<defaultModels.size(); i++){
			DefaultReference dR = (DefaultReference)defaultModels.elementAt(i);
			if (dR.getParadigm() != null && dR.getParadigm().equals(paradigm))
				return dR;
		}
		return null;

	}
	/*..........................................ContinuousData................*/
	/** this should not be used except by modules INTENDING to steal away the defaults */
	public static void registerDefaultModel(String paradigm, String name){
		if (defaultModels == null)
			return;
		NameReference p = NameReference.getNameReference(paradigm);
		DefaultReference dR = findDefaultReference(p);
		if (dR == null) {
			dR = new DefaultReference(p);
			defaultModels.addElement(dR);
		}
		dR.setDefault(name);
	}
	/*..........................................ContinuousData................*/
	public CharacterModel getDefaultModel(String paradigm){
		NameReference p = NameReference.getNameReference(paradigm);
		DefaultReference dR = findDefaultReference(p);
		if (dR==null)
			return null;
		else {
			CharacterModel cm = getProject().getCharacterModel(dR.getDefault());
			if (cm==null) 
				MesquiteMessage.println("Default model not found / " + dR.getDefault());
			return cm;
		}
	}
	/*..........................................ContinuousData................*/
	public String toString(){
		return "Continuous data matrix id: " + getID() + "; chars: " + numChars + "; taxa: " + numTaxa + "; items " + getNumItems() + "; name: " + getName();
	}
}


