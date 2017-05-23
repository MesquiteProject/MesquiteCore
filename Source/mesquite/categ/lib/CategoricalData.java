/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


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
import java.util.zip.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
/** A subclass of CharacterData for data stored as Categorical sets (e.g, "{0, 2}").  Associated with the CharacterState subclass CategoricalState.*/
public class CategoricalData extends CharacterData {
	private String[][] stateNames;
	private String[][] stateNotes;
	protected char[] symbols;
	protected final static char[] defaultSymbols;
	public static String DATATYPENAME = "Standard Categorical Data";

	private long[][] matrix;
	private short[][] matrixShort;
	private static Vector defaultModels;
	protected static char ANDseparator = '&';  //why static?
	protected static char ORseparator = '/';
	protected String puncString;
	private int maxSymbolDefined = -1;
	private static boolean permitShortMatrices = true;
	private Object[][] stateAnnotations; //one vector (possibly) per state per character

	static {
		defaultModels= new Vector(2);
		defaultSymbols = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm', 'n', 'p' , 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
	}

	public CategoricalData(CharMatrixManager manager, int numTaxa, int numChars, Taxa taxa){
		super(manager, numTaxa, numChars, taxa);
		setUseDiagonalCharacterNames(true);
		symbols = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm', 'n', 'p' , 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
		try {
			puncString = "" + ANDseparator + ORseparator + getUnassignedSymbol() + getInapplicableSymbol();
			if (permitShortMatrices && CategoricalState.compressibleToShort(getDefaultState())) {
				matrixShort = new short[ numChars][numTaxa];
				for (int it=0; it<numTaxa; it++)
					for (int ic=0; ic<numChars; ic++)
						matrixShort[ic][it] = CategoricalState.compressToShort(getDefaultState());
			}
			else {
				matrix = new long[ numChars][numTaxa];
				for (int it=0; it<numTaxa; it++)
					for (int ic=0; ic<numChars; ic++)
						matrix[ic][it] = getDefaultState();
			}
		}
		catch (OutOfMemoryError e){
			MesquiteMessage.warnProgrammer("Sorry, the Character Matrix could not be created because of lack of memory.  See file memory.txt in the Mesquite_Folder.");
			discreetAlert("Sorry, the Character Matrix could not be created because of lack of memory.  See file memory.txt in the Mesquite_Folder.");
			matrix = null;
			numChars = 0;
			numTaxa = 0;
		}
	}
	public String getDefaultIconFileName(){ //for small 16 pixel icon at left of main bar
		return "matrixCategSmall.gif";
	}

	/** true if the matrix is a valid one */
	public boolean isValid(){
		return (matrix!=null || matrixShort!=null) && numTaxa>0; // && numChars>0;
	}

	public int getNumDataBlocks(int it, int icStart, int icEnd) {
		int count=0;
		boolean inBlock=false;
		for (int ic = icStart; ic < getNumChars() && ic<=icEnd; ic++) {
			if (!isInapplicable(ic, it) && !inBlock){  // start of block
				inBlock=true;
				count++;
			} else if (isInapplicable(ic,it) && inBlock){  // block has ended
				inBlock=false;
			}
		}
		return count;
	}
	
	public void getDataBlockBoundaries(int it, int icStart, int icEnd, int whichBlock, MesquiteInteger blockStart, MesquiteInteger blockEnd) {
		if (blockStart==null || blockEnd==null) 
			return;
		int count=0;
		boolean inBlock=false;
		for (int ic = icStart; ic < getNumChars() && ic<=icEnd; ic++) {
			if (!isInapplicable(ic, it) && !inBlock){  // start of block
				inBlock=true;
				count++;
				if (count==whichBlock) {
					blockStart.setValue(ic);
				}
			} else if (isInapplicable(ic,it) && inBlock){  // block has ended
				inBlock=false;
				if (count==whichBlock) {
					blockEnd.setValue(ic);
				}
			}
		}
		if (blockStart.isCombinable()&&!blockEnd.isCombinable()) // end didn't get assigned, but start did
			blockEnd.setValue(getNumChars()-1);
	}

	/*.................................................................................................................*/
	public String searchData(String s, MesquiteString commandResult) {
		if (commandResult != null)
			commandResult.setValue((String)null);
		if (StringUtil.blank(s))
			return null;
		String list = "";
		String fc =""; //to receive the direct command
		int numFound = 0;
		for (int ic=0; ic< getNumChars(); ic++){
			String name = getCharacterName(ic);

			String statesString = "";

			for (int k = 0; k<= maxStateWithName(ic); k++){
				if (hasStateName(ic, k)){
					String stateName = getStateName(ic, k);
					if (StringUtil.foundIgnoreCase(stateName, s)){
						statesString += "<li>State " + k + ": <strong>" + StringUtil.protectForXML(stateName) + "</strong>. <a href=\"selectCharacter:" + ic+ " " + getID() + "\">Touch character</a></li>";
						numFound++;
						fc = "selectCharacter:" + ic+ " " + getID();
					}
				}
			}
			if (!StringUtil.blank(statesString))
				statesString = "<ul>" + statesString + "</ul>";

			if (name != null && StringUtil.foundIgnoreCase(name, s)){
				list += "<li>Character " + (ic+1) + ": <strong>" + StringUtil.protectForXML(name) + "</strong>. <a href=\"selectCharacter:" + ic+ " " + getID() + "\">Touch character</a></li>";
				numFound++;
				fc = "selectCharacter:" + ic+ " " + getID() ;
			}
			else if (!StringUtil.blank(statesString)){
				if (name != null)
					list += "<li>Character " + (ic+1) + ": <strong>" + StringUtil.protectForXML(name) + "</strong>. " + statesString + "</li>";
				else
					list += "<li>Character " + (ic+1) + ". " + statesString + "</li>";
			}

		}

		if (commandResult != null && numFound == 1)
			commandResult.setValue(fc);
		if (StringUtil.blank(list))
			return list;
		return "<h2>Characters of matrix <strong>" + StringUtil.protectForXML(getName()) + "</strong></h2><ul>" + list + "</ul>";
	}
	public boolean usingShortMatrix(){
		return matrix == null;
	}
	private void upgradeToFullMatrix(){
		matrix = new long[ numChars][numTaxa];
		for (int ic=0; ic<numChars && ic<matrixShort.length; ic++)
			for (int it=0; it<numTaxa && it<matrixShort[ic].length; it++)
				matrix[ic][it] = CategoricalState.expandFromShort(matrixShort[ic][it]);
		matrixShort = null;
		setDirty(true);
		incrementStatesVersion();
	}
	public boolean longCompressibleToShort(){
		if (usingShortMatrix())
			return false;
		for (int ic=0; ic<numChars && ic<matrix.length; ic++)
			for (int it=0; it<numTaxa && it<matrix[ic].length; it++)
				if (!CategoricalState.compressibleToShort(matrix[ic][it]))
					return false;
		return true;
	}
	/*..........................................  CategoricalData  ..................................................*/
	public long getDefaultState(){
		return CategoricalState.unassigned;
	}
	/** sets the state of character ic in taxon it to the default state (which in some circumstances may be inapplicable, e.g. gaps for molecular data)*/
	public  void deassign(int ic, int it){
		setState(ic, it, getDefaultState());
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** returns an array of longs for the cells in taxon it, starting at character icStart, for length "length" */
	public long[] getLongArray(int icStart, int length, int it, boolean includeGaps) {
		long[] array= new long[length];
		for (int ic=0; ic<length; ic++) 
			array[ic] = CategoricalState.inapplicable;
		int count=0;
		for (int ic=icStart; ic<getNumChars() && count<length; ic++)
			if (includeGaps || !isInapplicable(ic,it)){
				array[count] = getState(ic,it);
				count++;
			}
		return array;
	}
	/*..........................................  CategoricalData  ..................................................*/
	public void dispose(){
		super.dispose();
		matrix = null;
		matrixShort = null;
		stateNames = null;
		stateAnnotations = null;
		stateNotes = null;
		symbols = null;
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** Indicates the type of character stored */ 
	public Class getStateClass(){
		return CategoricalState.class;
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** returns the name of the type of data stored */
	public String getDataTypeName(){
		return DATATYPENAME;
	}

	public void copyDataBlock(CharacterData sourceData, int icStart, int icEnd, int itStart, int itEnd){
		if (sourceData == null)
			return;
		for (int ic=icStart; ic<=icEnd; ic++){
			for (int it=itStart; it<=itEnd; it++) {
				int itSource = it-itStart;
				int icSource = ic-icStart;
				if (itSource<sourceData.getNumTaxa() && icSource<sourceData.getNumChars())
					setState(ic, it, ((CategoricalData)sourceData).getStateRaw(icSource, itSource)); 
			}
		}
		resetCellMetadata();

	}
	public void moveDataBlock(int icSourceStart,  int icSourceEnd, int icStart, int itStart,int  itEnd, boolean allowOverwrite, boolean warn){
		int numCharsToMove = icSourceEnd-icSourceStart+1;
		for (int it=itStart; it<=itEnd; it++) {
			for (int ic=icSourceStart; ic<=icSourceEnd; ic++){
				int icMove = icStart+ (ic-icSourceStart);
				if (it<getNumTaxa() && ic<getNumChars()){
					if (!isInapplicable(icMove,it) && !allowOverwrite) {
						if (warn) {
							if (MesquiteTrunk.debugMode) {
								MesquiteMessage.discreetNotifyUser("Attempt to overwrite data in data.moveDataBlock");
							}
						}
					} else {
						setState(icMove, it, getStateRaw(ic, it)); 
						setToInapplicable(ic, it); 
					}
				}

			}
		}
		resetCellMetadata();

	}
	/** Copies the block of data from the source to this data object */
	public void copyDataBlock(CharacterData sourceData, int icSourceStart,  int itSourceStart, int icStart, int icEnd, int itStart, int itEnd){
		if (sourceData == null)
			return;
		
		for (int ic=icStart; ic<=icEnd; ic++){
			for (int it=itStart; it<=itEnd; it++) {
				int itSource = it-itStart+itSourceStart;
				int icSource = ic-icStart+icSourceStart;
				if (it<getNumTaxa() && ic<getNumChars() && itSource<sourceData.getNumTaxa() && icSource<sourceData.getNumChars())
					setState(ic, it, ((CategoricalData)sourceData).getStateRaw(icSource, itSource)); 
			}
		}
		resetCellMetadata();

	}


	/*..........................................  CategoricalData  ..................................................*/
	/** copy the basic data.  Does not copy the associated specs sets etc.*/
	public  void copyData(CharacterData sourceData, boolean allowDifferentSizes){
		if (sourceData==null)
			return;
		if ((sourceData.getNumTaxa()==getNumTaxa() && sourceData.getNumChars() == getNumChars()) || allowDifferentSizes) {
			for (int ic=0; ic<numChars; ic++){
				for (int it=0; it<numTaxa; it++) {
					if (it>=sourceData.getNumTaxa() || ic>=sourceData.getNumChars())
						setState(ic, it, getDefaultState()); 
					else
						setState(ic, it, ((CategoricalData)sourceData).getStateRaw(ic, it)); 
				}
			}
			resetCellMetadata();
		}
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** copy the basic data.  Does not copy the associated specs sets etc.*/
	public  void copyData(CharacterData sourceData){
		copyData(sourceData,false);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/**returns a copy of simply the data matrix with symbols */
	public CategoricalData getDataCopy(){
		CategoricalData data = (CategoricalData)makeCharacterData(numTaxa, numChars);
		if (symbols!=null)
			for (int i=0; i<=CategoricalState.maxCategoricalState && i<symbols.length; i++) {
				data.setSymbolDirect(i, symbols[i]);
			}
		for (int ic=0; ic<numChars; ic++){
			for (int it=0; it<numTaxa; it++) {
				data.setState(ic, it, getStateRaw(ic, it)); 
			}
		}
		data.resetCellMetadata();
		return data;
	}
	/*..........................................  CategoricalData  ..................................................*/
	/**clone this CharacterData and return new copy.  Does not clone the associated specs sets etc.*/ //TODO: here should use super.setToClone(data) to handle specssets etc.???
	public CharacterData cloneData(){
		CategoricalData data = new CategoricalData(matrixManager, numTaxa, numChars, getTaxa());
		if (symbols!=null)
			for (int i=0; i<=CategoricalState.maxCategoricalState && i<symbols.length; i++) {
				data.setSymbolDirect(i, symbols[i]);
			}
		for (int ic=0; ic<numChars; ic++){
			if (hasStateNames() && hasStateNames(ic))
				for (int i = 0; i <= CategoricalState.maxCategoricalState; i++)
					if (hasStateName(ic,i))
						data.setStateName(ic,i,getStateName(ic,i));
			if (hasStateNotes() && hasStateNotes(ic))
				for (int i = 0; i <= CategoricalState.maxCategoricalState; i++)
					if (hasStateNote(ic,i))
						data.setStateNote(ic,i,getStateNote(ic,i));
			for (int it=0; it<numTaxa; it++) {
				data.setState(ic, it, getStateRaw(ic, it)); 
			}
			if (getSelected(ic))
				data.setSelected(ic, true);
		}
		data.resetCellMetadata();
		return data;
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

		CategoricalData data = new CategoricalData(matrixManager, blockTaxa, blockChars, taxa);
		if (symbols!=null)
			for (int i=0; i<=CategoricalState.maxCategoricalState && i<symbols.length; i++) {
				data.setSymbolDirect(i, symbols[i]);
			}
		for (int ic=icStart; ic<=icEnd; ic++){
			if (hasStateNames())
				for (int i = 0; i <= CategoricalState.maxCategoricalState; i++)
					data.setStateName(ic-icStart,i,getStateName(ic,i));
			for (int it=itStart; it<=itEnd; it++) {
				data.setState(ic-icStart, it-itStart, getStateRaw(ic, it)); 
			}
			if (getSelected(ic))
				data.setSelected(ic-icStart, true);
		}
		data.resetCellMetadata();
		return data;
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
		if (matrix == null && matrixShort == null)
			dataIntegrityAlert("Categorical data with null internal matrix. getNumTaxa() = " + n + " name " + getName() + " file " + getFile());
		else if (MesquiteThread.isThreadBelongingToMesquite() && MesquiteThread.numFilesBeingRead==0 ){  //since files read on other thread, suppress warnings
			if (matrix != null && matrix.length>0 && matrix[0] !=null && matrix[0].length != n)
				dataIntegrityAlert("Categorical matrix with incorrect record of number of taxa. getNumTaxa() = " + n + " matrix[0].length " + matrix[0].length + " name " + getName() + " file " + getFile());
			else if (matrixShort != null && matrixShort.length>0 && matrixShort[0] !=null && matrixShort[0].length != n)
				dataIntegrityAlert("Categorical matrix with incorrect record of number of taxa. getNumTaxa() = " + n + " matrixShort[0].length " + matrixShort[0].length + " name " + getName() + " file " + getFile());
		}
		return n;
	}
	public int getNumChars(){
		return getNumChars(true);
	}
	public int getNumChars(boolean notifyIfError){
		if (disposed)
			return 0;
		int n = super.getNumChars(notifyIfError);
		if (matrix == null && matrixShort == null){
			if (notifyIfError)
				dataIntegrityAlert("Categorical data with null internal matrix. getNumChar() = " + n  + " nAdd = " + nAdd + " nDel = " + nDel + " nMove = " + nMove + " name " + getName() + " file " + getFile());
			return 0;
		}
		else if (matrix != null && matrix.length !=n){
			if (MesquiteThread.isThreadBelongingToMesquite() && MesquiteThread.numFilesBeingRead==0 && notifyIfError && !charNumChanging) //since files read on other thread, suppress warnings
				dataIntegrityAlert("Categorical matrix with incorrect record of number of characters. getNumChar() = " + n + " matrix.length " + matrix.length + " nAdd = " + nAdd + " nDel = " + nDel + " nMove = " + nMove + " name " + getName() + " file " + getFile());
			if (matrix.length>n)
				return n;
			else
				return matrix.length;
		}
		else if (matrixShort != null && matrixShort.length !=n){
			if (MesquiteThread.isThreadBelongingToMesquite() && MesquiteThread.numFilesBeingRead==0 && notifyIfError && !charNumChanging) //since files read on other thread, suppress warnings
				dataIntegrityAlert("Categorical matrix with incorrect record of number of characters. getNumChar() = " + n + " matrixShort.length " + matrixShort.length + " nAdd = " + nAdd + " nDel = " + nDel + " nMove = " + nMove + " name " + getName() + " file " + getFile());
			if (matrixShort.length>n)
				return n;
			else
				return matrixShort.length;
		}
		return n;
	}


	/*..........................................  CategoricalData  ..................................................*/
	/**recodes stateA to stateB in character IC*/
	public  boolean recodeStates(int ic, long[] rules, int maxState){
		if (ic<0 || ic>= numChars)
			return false;
		//first, handle state names and notes
		if (this.getClass() == CategoricalData.class) {
			String[] oldStateNames = null;
			String[] oldStateNotes = null;
			AttachedNotesVector[] oldStateAnnotations = null;
			if (stateNames != null && stateNames[ic] != null) {
				oldStateNames = new String[stateNames[ic].length];
				for (int is = 0; is < stateNames[ic].length; is++)
					if (stateNames[ic][is]!=null)
						oldStateNames[is] = new String(stateNames[ic][is]);
				stateNames[ic] = null; //wipe out in preparation for new
			}
			if (stateNotes!= null && stateNotes[ic] != null) {
				oldStateNotes = new String[stateNotes[ic].length];
				for (int is = 0; is < stateNotes[ic].length; is++)
					if (stateNotes[ic][is]!=null)
						oldStateNotes[is] = new String(stateNotes[ic][is]);
				stateNotes[ic] = null;
			}
			if (stateAnnotations!= null && stateAnnotations[ic] != null) {
				oldStateAnnotations = new AttachedNotesVector[stateAnnotations[ic].length];
				for (int is = 0; is< stateAnnotations[ic].length; is++) {
					if (stateAnnotations[ic][is] != null)
						oldStateAnnotations[is] =  ((AttachedNotesVector)stateAnnotations[ic][is]).cloneVector();
					stateAnnotations[ic][is] = null;
				}
			}

			//use rules to assign new state names and notes
			for (int e=0; e<= CategoricalState.maxCategoricalState; e++) {
				long recodeE = rules[e]; //this is the state set that state e would be recoded to.  It 
				if (recodeE != CategoricalState.unassigned && recodeE != CategoricalState.inapplicable){ //recoded not to unassigned or inapp., otherwise state name and notes are lost
					for (int e2=0; e2<= CategoricalState.maxCategoricalState; e2++) {
						//about to recode state e2 to state recodeE
						if (CategoricalState.isElement(recodeE, e2)){  
							if (oldStateNames != null && e< oldStateNames.length && oldStateNames[e] != null) {
								String name = getStateName(ic, e2, true);
								if (name != null)
									name += "-" + oldStateNames[e];
								else
									name = oldStateNames[e];
								setStateName(ic, e2, name);
							}
							if (oldStateNotes != null && e< oldStateNotes.length && oldStateNotes[e] != null) {
								String note = getStateNote(ic, e2);
								if (note != null)
									note += "-" + oldStateNotes[e];
								else
									note = oldStateNotes[e];
								setStateNote(ic, e2, note);
							}
							if (oldStateAnnotations != null && e < oldStateAnnotations.length && oldStateAnnotations[e] != null){
								AttachedNotesVector anv = getStateAnnotationsVector(ic, e2);
								if (anv != null) {
									anv.concatenate(oldStateAnnotations[e]);
									anv.notifyOwner(MesquiteListener.ANNOTATION_CHANGED);
								}
								else 
									setStateAnnotationsVector(ic, e2, oldStateAnnotations[e]);



							}
						}
					}
				}
			}


		}

		for (int it=0; it<getNumTaxa(); it++){
			long state = getState(ic, it);
			if (state == CategoricalState.unassigned){
				if (rules[maxState+1] != CategoricalState.unassigned)
					setState(ic, it, rules[maxState+1]);
			}
			else if (state == CategoricalState.inapplicable){
				if (rules[maxState+2] != CategoricalState.inapplicable)
					setState(ic, it, rules[maxState+2]);
			}
			else {
				long newCoding = (~CategoricalState.statesBitsMask) & state;  //begin with uncertain bits etc.
				for (int e=0; e<= CategoricalState.maxCategoricalState; e++) {
					if (((1L<<e)&state)!=0) {  //test bit
						newCoding |= rules[e]; //add corresponding bit from rules
					}
				}
				if ((newCoding & CategoricalState.statesBitsMask)== 0L) {
					if ((newCoding & CategoricalState.unassigned) != 0L)
						newCoding = CategoricalState.unassigned;
					else
						newCoding = CategoricalState.inapplicable;
				}
				setState(ic, it, newCoding);

			}
		}
		return true;

	}
	/*..........................................  CategoricalData  ..................................................*/
	/**swaps characters first and second*/
	public  boolean swapAssociated(int first, int second){
		if (first<0 || first >= numChars)
			return false;
		if (second<0 || second >= numChars)
			return false;

		return super.swapParts(first,second);

	}
	/*..........................................  CategoricalData  ..................................................*/
	/**swaps characters first and second*/
	public  boolean swapParts(int first, int second){
		if (first<0 || first >= numChars)
			return false;
		if (second<0 || second >= numChars)
			return false;
		if (!usingShortMatrix())
			Long2DArray.swapColumns(matrix, first, second);
		else
			Short2DArray.swapColumns(matrixShort, first, second);
		incrementStatesVersion();
		StringArray.swapColumns(stateNames, first, second);
		StringArray.swapColumns(stateNotes, first, second);
		Object2DArray.swapColumns(stateAnnotations, first, second);

		return super.swapParts(first,second);

	}
	/*..........................................  CategoricalData  ..................................................*/
	/**move num characters starting at first to just after character justAfter */
	public  boolean moveParts(int first, int num, int justAfter){
		if (justAfter<0)
			justAfter = -1;
		else if (justAfter>=numChars)
			justAfter = numChars-1;
		if (first<0 || first >= numChars)
			return false;
		if (num<=0)
			return false;
		if (first + num>numChars)
			return false;
		if (!usingShortMatrix())
			Long2DArray.moveColumns(matrix, first, num, justAfter);
		else
			Short2DArray.moveColumns(matrixShort, first, num, justAfter);
		incrementStatesVersion();
		StringArray.moveColumns(stateNames, first, num, justAfter);
		StringArray.moveColumns(stateNotes, first, num, justAfter);
		Object2DArray.moveColumns(stateAnnotations, first, num, justAfter);

		return super.moveParts(first,num,justAfter);

	}

	/*..........................................  CategoricalData  ..................................................*/
	/**Adds num characters after position "starting"; returns true iff successful.*/
	public  boolean addParts(int starting, int num){
		if (starting<0)
			starting = -1;
		else if (starting>=numChars)
			starting = numChars-1;
		int newNumChars = numChars + num;

		try {

			incrementStatesVersion();
			if (!usingShortMatrix())
				matrix = Long2DArray.addColumns(matrix, numTaxa, getDefaultState(), starting, num);
			else
				matrixShort = Short2DArray.addColumns(matrixShort, numTaxa, CategoricalState.compressToShort(getDefaultState()), starting, num);

			//numChars = newNumChars; don't do this since addCharactersAdjust needs to remember old number
			if (stateNames!=null){
				String[][] newStateNames = new String[newNumChars][];
				for (int i=0; i<=starting; i++) {
					newStateNames[i] = stateNames[i];
				}
				for (int i=0; i<num; i++) {
					newStateNames[starting + i + 1] = null;
				}
				for (int i=0; i<numChars-starting-1; i++) {
					newStateNames[i +starting+num+1] = stateNames[starting + i+1];
				}
				stateNames = newStateNames;
			}
			if (stateNotes !=null){
				String[][] newStateNotes = new String[newNumChars][];
				for (int i=0; i<=starting; i++) {
					newStateNotes[i] = stateNotes[i];
				}
				for (int i=0; i<num; i++) {
					newStateNotes[starting + i + 1] = null;
				}
				for (int i=0; i<numChars-starting-1; i++) {
					newStateNotes[i +starting+num+1] = stateNotes[starting + i+1];
				}
				stateNotes = newStateNotes;
			}
			if (stateAnnotations !=null){
				Object[][] newStateAnnotations = new Object[newNumChars][];
				for (int i=0; i<=starting; i++) {
					newStateAnnotations[i] = stateAnnotations[i];
				}
				for (int i=0; i<num; i++) {
					newStateAnnotations[starting + i + 1] = null;
				}
				for (int i=0; i<numChars-starting-1; i++) {
					newStateAnnotations[i +starting+num+1] = stateAnnotations[starting + i+1];
				}
				stateAnnotations = newStateAnnotations;
			}
			return super.addParts(starting, num);
		}
		catch (OutOfMemoryError e){
			MesquiteMessage.warnProgrammer("Sorry, characters could not be added to the Character Matrix because of lack of memory.  See file memory.txt in the Mesquite_Folder.");
		}
		return false;

	}
	/*..........................................  CategoricalData  ..................................................*/
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
		incrementStatesVersion();
		if (!usingShortMatrix())
			matrix = Long2DArray.deleteColumns(matrix, starting, num);
		else
			matrixShort = Short2DArray.deleteColumns(matrixShort, starting, num);

		//numChars = newNumChars; don't do this since super.deleteCharacters needs to remember old number
		if (stateNames!=null){
			String[][] newStateNames = new String[newNumChars][];
			for (int i=0; i<starting; i++) {
				newStateNames[i] = stateNames[i];
			}
			for (int i=starting + num;i<numChars; i++) {
				newStateNames[i-num] = stateNames[i];
			}
			stateNames = newStateNames;
		}
		if (stateNotes !=null){
			String[][] newStateNotes = new String[newNumChars][];
			for (int i=0; i<starting; i++) {
				newStateNotes[i] = stateNotes[i];
			}
			for (int i=starting + num;i<numChars; i++) {
				newStateNotes[i-num] = stateNotes[i];
			}
			stateNotes = newStateNotes;
		}
		if (stateAnnotations !=null){
			Object[][] newStateAnnotations = new Object[newNumChars][];
			for (int i=0; i<starting; i++) {
				newStateAnnotations[i] = stateAnnotations[i];
			}
			for (int i=starting + num;i<numChars; i++) {
				newStateAnnotations[i-num] = stateAnnotations[i];
			}
			stateAnnotations = newStateAnnotations;
		}
		return super.deleteParts(starting, num);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/**Adds num taxa after position "starting"; returns true iff successful.*/
	public boolean addTaxa(int starting, int num){

		setDirty(true);
		if (starting<0)
			starting = -1;
		else if (starting>=numTaxa)
			starting = numTaxa-1;
		int newNumTaxa = numTaxa + num;

		try {
			incrementStatesVersion();
			if (usingShortMatrix()){
				short shortUnassigned = CategoricalState.compressToShort(getDefaultState());
				for (int ic=0; ic<numChars; ic++) {
					matrixShort[ic] = ShortArray.addParts(matrixShort[ic], starting, num, CategoricalState.compressToShort(getDefaultState()));
				}
			}
			else {
				for (int ic=0; ic<numChars; ic++) {
					matrix[ic] = LongArray.addParts(matrix[ic], starting, num, getDefaultState());
				}
			}
		}
		catch (OutOfMemoryError e){
			MesquiteMessage.warnProgrammer("Sorry, taxa could not be added to the Character Matrix because of lack of memory.  See file memory.txt in the Mesquite_Folder.");
		}
		numTaxa = newNumTaxa; 
		return super.addTaxa(starting, num);
	}
	/*..........................................  CategoricalData  ..................................................*/

	/**Deletes num taxa from position "starting"; returns true iff successful.*/
	public boolean deleteTaxa(int starting, int num){
		if (num<=0)
			return false;
		if (starting<0)
			return false;
		else if (starting>=numTaxa)
			return false;
		setDirty(true);
		if (num+starting>numTaxa)
			num = numTaxa-starting;
		int newNumTaxa = numTaxa - num;

		incrementStatesVersion();

		if (usingShortMatrix()){
			for (int ic=0; ic<numChars; ic++)
				matrixShort[ic] = ShortArray.deleteParts(matrixShort[ic], starting, num);
		}
		else for (int ic=0; ic<numChars; ic++)
			matrix[ic] = LongArray.deleteParts(matrix[ic], starting, num);


		numTaxa = newNumTaxa; 
		return super.deleteTaxa(starting, num);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/**moves num taxa from position "starting" to just after position "justAfter"; returns true iff successful.*/
	public boolean moveTaxa(int starting, int num, int justAfter){
		if (num<=0)
			return false;
		if (starting<0)
			return false;
		else if (starting>=numTaxa)
			return false;
		setDirty(true);
		incrementStatesVersion();
		if (usingShortMatrix()) {
			for (int ic=0; ic<numChars; ic++)
				ShortArray.moveParts(matrixShort[ic], starting, num, justAfter);
		}
		else for (int ic=0; ic<numChars; ic++)
			LongArray.moveParts(matrix[ic], starting, num, justAfter);
		return super.moveTaxa(starting, num, justAfter);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** Get String[] of symbols */
	public String[] getSymbols() {
		//int maxSymb = getMaxSymbolDefined();
		String[] labels = new String[symbols.length];
		for (int i=0; i<symbols.length; i++)
			labels[i] = ""+ getSymbol(i);
		return labels;
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** Get symbol for state st. */
	public char getSymbol(int st) {
		if (symbols == null || st <0 || st>=symbols.length)
			return getUnassignedSymbol();
		return symbols[st];
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** Set symbol for state st.  Does not check for duplicate symbols */
	public void setSymbolDirect(int st, char symbol) {
		if (symbols == null ||st <0 || st>=symbols.length)
			return;
		setDirty(true);
		int current = findSymbol(symbol);
		if (maxSymbolDefined<st)
			maxSymbolDefined = st;
		symbols[st] = symbol;
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** Set symbol for state st.  Returns whether setting implied duplicate symbol */
	public boolean setSymbol(int st, char symbol) {
		if (symbols == null || st <0 || st>=symbols.length)
			return false;
		setDirty(true);
		int current = findSymbol(symbol);
		if (maxSymbolDefined<st)
			maxSymbolDefined = st;
		if (current >=0 && current != st){ //duplicate symbol!
			symbols[st] = symbol;
			char unusedSymbol = findUnusedSymbol();
			if (unusedSymbol != (char)0)
				symbols[current] = unusedSymbol;
			return true;
		}
		else {
			symbols[st] = symbol;
			return false;
		}
	}

	private int findSymbol(char symbol){
		for (int ist = 0; ist<symbols.length; ist++) {
			if (symbols[ist]==symbol) {
				return ist;
			}
		}
		return -1;
	}
	private char findUnusedSymbol(){
		for (int ist = 0; ist<defaultSymbols.length; ist++) {
			if (findSymbol(defaultSymbols[ist])<0) {
				return defaultSymbols[ist];
			}
		}
		return (char)0;
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** sets the symbol used for inapplicable character (e.g., gap)*/
	public void setInapplicableSymbol(char inapp) {
		super.setInapplicableSymbol(inapp);
		puncString = "" + ANDseparator + ORseparator + getUnassignedSymbol() + getInapplicableSymbol();
	}
	/** sets the symbol used for missing data (unassigned) character*/
	public void setUnassignedSymbol(char mc) { 
		super.setUnassignedSymbol(mc);
		puncString = "" + ANDseparator + ORseparator + getUnassignedSymbol() + getInapplicableSymbol();
	}
	/*..........................................  CategoricalData  ..................................................*/
	/**Reverses the data from character icStart to icEnd in taxon it.  If reverseTerminalGaps
	 * is true, then it reversed the entire stretch; if it is false, it only reverses from the first non-gap to last non-gap in the stretch. 
	 * If adjustLinks is true, then the linked matrices will also have their cells reversed.  adjustLinks should be true
	 * in almost all cases, as reversing without it will disrupt linkages between matrices and cause features in
	 * Mesquite to fail (e.g., within Chromaseq). */
	public void reverse(int icStart, int icEnd, int it, boolean reverseTerminalGaps, boolean adjustCellLinked){
		int ic1;
		int ic2; 
		if (reverseTerminalGaps) {
			ic1 = icStart;
			ic2 = icEnd;
		}
		else {
			ic1 = thisOrNextApplicableChar(icStart, it);
			ic2 = thisOrPreviousApplicableChar(icEnd,it); 
		}
		int numChars = ic2-ic1+1;
		int halfWay = numChars/2;
		for (int ic= 0; ic<halfWay; ic++){
			tradeStatesBetweenCharactersInternal(ic1+ic,ic2-ic,it,false, false);  // don't ask it to adjust linked as we will do it ourselves, below
		}


		if (adjustCellLinked){
			for (int i=0; i<linkedDatas.size(); i++){
				CharacterData d= (CharacterData)linkedDatas.elementAt(i);
				for (int ic= 0; ic<halfWay; ic++){
					if (d instanceof CategoricalData)
						((CategoricalData)d).tradeStatesBetweenCharactersInternal(ic1+ic,ic2-ic,it,false, false);
					else
						d.tradeStatesBetweenCharacters(ic1+ic,ic2-ic,it,false);
				}

			}
		}
	}
	/*..........................................  CategoricalData  ..................................................*/
	/**Reverses the metadata from character icStart to icEnd.  
	 * If adjustLinks is true, then the linked matrices will also have their metadata reversed.  adjustLinks should be true
	 * in almost all cases, as reversing without it will disrupt linkages between matrices and cause features in
	 * Mesquite to fail */
	public void reverseCharacterMetadata(int icStart, int icEnd, boolean adjustCellLinked){
		int numChars = icEnd-icStart+1;
		int halfWay = numChars/2;
		for (int ic= 0; ic<halfWay; ic++){
			swapCharacterMetadata(icStart+ic,icEnd-ic);  // don't ask it to adjust linked as we will do it ourselves, below
		}


		if (adjustCellLinked){
			for (int i=0; i<linkedDatas.size(); i++){
				CharacterData d= (CharacterData)linkedDatas.elementAt(i);
				for (int ic= 0; ic<halfWay; ic++){
					d.swapCharacterMetadata(icStart+ic,icEnd-ic);
				}

			}
		}
	}
	/*..........................................  CategoricalData  ..................................................*
	 * Re-enable this one, with adjustCellLinked set to true, once the code settles down

	public void reverse(int icStart, int icEnd, int it, boolean reverseTerminalGaps){
		reverse(icStart,icEnd, it, reverseTerminalGaps, true);
	}
	/*..........................................CategoricalData................*/
	/** Trades the states of character ic and ic2 in taxon it.  Used for sorting by characters or reversing sequences (for example).
	 * If adjustCellLinked is true, then linked matrices will also have their cells traded. This should generally
	 * be set to true; if you set it to false, then you must trade states of linked matrices yourself. 
	 * This is the public version; the internal version is used internally to distinguish between the internal call from reverse
	 * (which will handle directions otherwise) and external calls (which should do the direction bookkeeping for the user)*/
	public void tradeStatesBetweenCharacters(int ic, int ic2, int it, boolean adjustCellLinked) { 
		tradeStatesBetweenCharactersInternal(ic, ic2, it, adjustCellLinked, true);
	}
	/*..........................................CategoricalData................*/
	/** Trades the states of character ic and ic2 in taxon it.  Used for sorting by characters or reversing sequences (for example).
	 * If adjustCellLinked is true, then linked matrices will also have their cells traded. This should generally
	 * be set to true; if you set it to false, then you must trade states of linked matrices yourself. */
	protected void tradeStatesBetweenCharactersInternal(int ic, int ic2, int it, boolean adjustCellLinked, boolean adjustDirections) { 
		if (notInStorage(ic, it) || notInStorage(ic2, it))
			return;
		super.tradeStatesBetweenCharacters(ic, ic2, it,adjustCellLinked);
		if (usingShortMatrix()){
			short temp = matrixShort[ic][it];
			matrixShort[ic][it] =  matrixShort[ic2][it];
			matrixShort[ic2][it] = temp;
		}
		else {
			long temp = matrix[ic][it];
			matrix[ic][it] =  matrix[ic2][it];
			matrix[ic2][it] = temp;
		}
		incrementStatesVersion();
		setDirty(true, ic, it);
		setDirty(true, ic2, it);
	}
	/*..........................................CategoricalData................*/
	/* trades the states of character ic and ic2 in taxon it.  Used for reversing sequences (for example).*
	 * Re-enable this version, with adjustCellLinked set to true, once the code settles down for linked matrix adjustment.
	public void tradeStatesBetweenCharacters(int ic, int ic2, int it) { 
		tradeStatesBetweenCharacters(ic,ic2,it,true);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** trades the states of character ic between taxa it and it2.  Used for reshuffling.*/
	public void tradeStatesBetweenTaxa(int ic, int it, int it2) {
		if (notInStorage(ic, it) || notInStorage(ic, it2))
			return;
		super.tradeStatesBetweenTaxa(ic, it, it2);
		if (usingShortMatrix()){
			short temp = matrixShort[ic][it];
			matrixShort[ic][it] =  matrixShort[ic][it2];
			matrixShort[ic][it2] = temp;
		}
		else {
			long temp = matrix[ic][it];
			matrix[ic][it] =  matrix[ic][it2];
			matrix[ic][it2] = temp;
		}
		incrementStatesVersion();
		setDirty(true, ic, it);
		setDirty(true, ic, it2);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/**Dump matrix to log */
	public void logMatrix(){
		MesquiteModule.mesquiteTrunk.logln("Matrix of " + numTaxa + " taxa and " + numChars + " characters.");
		String matrixString="";
		for (int it=0; it<numTaxa; it++) {
			for (int ic=0; ic<numChars; ic++) {
				matrixString+=CategoricalState.toString(getStateRaw(ic, it));
			}
			matrixString+= '\r';
		}
		MesquiteModule.mesquiteTrunk.logln(matrixString);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** Fills matrix with data from passed StsOfCharacters object.*/
	public void setMatrix(MCharactersDistribution states){ //TODO: make this so it will change size of matrix if needed???
		if (!(states instanceof MCategoricalDistribution))
			return;
		setDirty(true);
		incrementStatesVersion();
		MCategoricalDistribution dStates = (MCategoricalDistribution)states;
		for (int ic = 0; ic< numChars; ic++) {
			for (int it = 0; it< numTaxa; it++)  {
				if (ic>dStates.getNumChars())
					setState(ic, it, getDefaultState());
				else
					setState(ic, it, dStates.getStateRaw(ic, it));  //1. 06 changed from getState 
				stampHistoryChange(ic, it);
			}
		}
		calculateFirstLastApplicable();
	}
	/*..........................................  CategoricalData  ..................................................*/
	/**get matrix and return as StsOfCharacters */
	public MCharactersDistribution getMCharactersDistribution(){
		MCategoricalEmbedded states = new MCategoricalEmbedded(this);
		return states;
	}
	/*..........................................  CategoricalData  ..................................................*/
	/**return a CharacterDistribution that points to character ic */
	public CharacterDistribution getCharacterDistribution(int ic){
		CategoricalEmbedded states = new CategoricalEmbedded(this, ic);
		return states;
	}
	/*..........................................  CategoricalData  ..................................................*/
	/**extract character ic and return as an array of long's */
	public long[] getCharacter(int ic){
		if (notInStorage(ic, 0)) //illegal check
			return null;
		if (ic<numChars && ic>=0) {
			long[] c = new long[getNumTaxa()];
			for (int i=0; i<c.length; i++)
				c[i] = getStateRaw(ic, i);
			return c;
		}
		return null;
	}
	/*..........................................  CategoricalData  ..................................................*/
	/**Return CharacterDistribution object with same number of taxa as this object (but not with any particular data; just so that of same data type) */
	public CharacterDistribution makeCharacterDistribution(){
		CategoricalAdjustable c= new CategoricalAdjustable(getTaxa(), numTaxa);
		c.setParentData(this);
		return c;
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** creates an empty CharacterState object of the same data type as CharacterData subclass used.*/
	public CharacterState makeCharacterState(){
		return new CategoricalState();
	}
	/*..........................................  CategoricalData  ..................................................*/
	/**Return CharacterData object with same number of taxa & characters as this object (but not with any particular data; just so that of same data type) */
	public CharacterData makeCharacterData() {
		return new CategoricalData(getMatrixManager(), getNumTaxa(), getNumChars(), getTaxa());
	}
	/*..........................................  CategoricalData  ..................................................*/
	/**Return CharacterData object with passed number of taxa & characters (but not with any particular data; just so that of same data type) */
	public CharacterData makeCharacterData(int ntaxa, int nchars) {
		return new CategoricalData(getMatrixManager(), ntaxa, nchars, getTaxa());
	}
	/*..........................................  CategoricalData  ..................................................*/
	public CharacterData makeCharacterData(CharMatrixManager manager, Taxa taxa){ 
		return new CategoricalData(getMatrixManager(), taxa.getNumTaxa(), 0, taxa);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** returns whether a given char is acceptable as the value for a character state */
	public boolean isAcceptableCharForState(char c){
		long value = fromChar(c);
		return (value == CategoricalState.unassigned || value == CategoricalState.inapplicable || CategoricalState.isCombinable(value));
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** returns Color for state of character, scaled to maxState (e.g. for tracing to yield white/black for binary) */
	public Color getColorOfState(int ic, int istate, int maxState, MesquiteColorTable colors){
		if (colors == null)
			return MesquiteColorTable.getDefaultColor(maxState,istate, MesquiteColorTable.COLORS);
		else 
			return colors.getColor(maxState,istate);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** returns Color for state of character, scaled to maxState (e.g. for tracing to yield white/black for binary) */
	public Color getColorOfState(int ic, int istate, int maxState){
		return MesquiteColorTable.getDefaultColor(maxState,istate, MesquiteColorTable.COLORS);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/**Returns the color for the given state of character ic.  Currently only returns default */
	public Color getColorOfState(int ic, int istate){
		return MesquiteColorTable.getDefaultColor(CategoricalState.maxCategoricalState,istate, MesquiteColorTable.COLORS);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** Gets the color representing state(s) of character ic in taxon it */ 
	public Color getColorOfStates(int ic, int it){
		if (notInStorage(ic, it)) //illegal check
			return ColorDistribution.unassigned;
		long s = getStateRaw(ic, it);
		if (CategoricalState.isCombinable(s)) {
			int colorCount = CategoricalState.cardinality(s);
			if (colorCount>1){
				return Color.lightGray;
			}
			else {
				return getColorOfState(ic, CategoricalState.maximum(s), getMaxState(ic));
			}
		}
		else 
			return ColorDistribution.unassigned;
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** Gets the color representing state(s) of character ic in taxon it */ 
	public Color getColorOfStatesUpperLimit(int ic, int it, int maxState){
		if (notInStorage(ic, it)) //illegal check
			return ColorDistribution.unassigned;
		long s = getStateRaw(ic, it);
		if (CategoricalState.isCombinable(s)) {
			int colorCount = CategoricalState.cardinality(s);
			if (colorCount>1){
				return Color.lightGray;
			}
			else {
				int state = CategoricalState.maximum(s);
				if (state>maxState)
					state=maxState;
				return getColorOfState(ic, state, maxState);
			}
		}
		else 
			return ColorDistribution.unassigned;
	}
	/*..........................................  CategoricalData  ..................................................*/
	public boolean charIsVariable( int ic, boolean selectedOnly, boolean considerAllVariants) {
		long intersection = CategoricalState.statesBitsMask;
		boolean anySel = getTaxa().anySelected();
		long firstState = CategoricalState.impossible;
		for (int it=0; it<getNumTaxa(); it++){
			if (!selectedOnly || !anySel || getTaxa().getSelected(it)){
				long state = getState(ic, it);
				if (firstState==CategoricalState.impossible)
					firstState=state;
				if (considerAllVariants && state!=firstState)
					return true;
				if (CategoricalState.isCombinable(state)){
					if (CategoricalState.cardinality(state)>1){ //polymorphic or uncertain
						if (CategoricalState.isUncertain(state)){ //uncertain; ok if overlaps
							intersection &= state;
							if (intersection == 0L)
								return true;
						}
						else
							return true;
					}
					else {
						intersection &= state;
						if (intersection == 0L)
							return true;
					}
				}
			}
		}
		return false;
	}

	/*..........................................  CategoricalData  ..................................................*/
	public boolean charIsVariable( int ic, boolean selectedOnly) {
		return charIsVariable(ic,selectedOnly,false);
	}

	/*..........................................  CategoricalData  ..................................................*/
	/** returns whether the character ic is inapplicable to taxon it*/
	public  boolean isValidAssignedState(int ic, int it){
		if (ic<0 || ic>=numChars) return false;
		long s = getStateRaw(ic,it);
		return CategoricalState.isCombinable(s);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** returns whether the character ic is inapplicable to taxon it*/
	public  int numValidAssignedState(int icStart, int icEnd, int it){
		int count =0;
		for (int ic=icStart; ic<=icEnd; ic++)
			if (isValidAssignedState(ic,it))
				count++;
		return count;
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** returns whether the character ic is inapplicable to taxon it*/
	public  boolean isValidStateOrUnassigned(int ic, int it){
		if (ic<0 || ic>=numChars) return false;
		long s = getStateRaw(ic,it);
		return (CategoricalState.isCombinable(s) || CategoricalState.isUnassigned(s));
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** returns whether the character ic is inapplicable to taxon it*/
	public  int numValidStateOrUnassigned(int icStart, int icEnd, int it){
		int count =0;
		for (int ic=icStart; ic<=icEnd; ic++)
			if (isValidStateOrUnassigned(ic,it))
				count++;
		return count;
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** returns whether the character ic is inapplicable to taxon it*/
	public  boolean isInternalInapplicable(int ic, int it){
		if (!isInapplicable(ic, it))
			return false;
		// it is inappllicable.  But is it internal?  
		if (ic< getNumChars()/2){ // first half, look left first
			boolean foundState = false;
			for (int icc = ic-1; icc>=0 && !foundState; icc--)  //look left for state
				foundState = !isInapplicable(icc, it);
			if (!foundState)
				return false;
			foundState = false;
			for (int icc = ic+1; icc<getNumChars() && !foundState; icc++)  //look right for state
				foundState = !isInapplicable(icc, it);
			if (!foundState)
				return false;
		}
		else {
			boolean foundState = false;
			for (int icc = ic+1; icc<getNumChars() && !foundState; icc++)  //look right for state
				foundState = !isInapplicable(icc, it);
			if (!foundState)
				return false;
			foundState = false;
			for (int icc = ic-1; icc>=0 && !foundState; icc--)  //look left for state
				foundState = !isInapplicable(icc, it);
			if (!foundState)
				return false;
		}
		return true;
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** returns whether the character ic is inapplicable to taxon it*/
	public  boolean isInapplicable(int ic, int it){
		long s = getStateRaw(ic,it);
		return(s== CategoricalState.inapplicable || s==CategoricalState.impossible);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** returns whether the state of character ic is missing in taxon it*/
	public  boolean isUnassigned(int ic, int it){
		long s = getStateRaw(ic,it);
		return(s== CategoricalState.unassigned || s==CategoricalState.impossible);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** returns whether the state of character ic is missing in taxon it*/
	public  boolean isValid(int ic, int it){
		long s = getStateRaw(ic,it);
		return !(s== 0L || s==CategoricalState.impossible);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** returns whether the state of character ic is missing in taxon it*/
	public  boolean isAmbiguousOrPolymorphic(int ic, int it){
		long s = getStateRaw(ic,it);
		return s== CategoricalState.unassigned || s==CategoricalState.impossible || (CategoricalState.hasMultipleStates(s));
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** returns whether the state of character ic is missing in taxon it*/
	public  boolean isMultistateUncertainty(int ic, int it){
		long s = getStateRaw(ic,it);
		return (CategoricalState.isUncertain(s) && CategoricalState.hasMultipleStates(s));
	}
	/*..........................................    ..................................................*/
	/** returns whether the state of character ic is a multistate uncertainty or polymorphism in taxon it*/
	public  boolean isMultistateOrUncertainty(int ic, int it){
		long s = getStateRaw(ic,it);
		return (CategoricalState.hasMultipleStates(s));
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** returns the maximum symbol defined*/
	public int getMaxSymbolDefined(){
		if (maxSymbolDefined>=0)
			return maxSymbolDefined;
		else
			return getMaxState();
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** returns the maximum state in character ic*/
	public int getMaxState(int ic){
		if (notInStorage(ic, 0)) //illegal check
			return -1;

		long allstates = 0;
		for (int it=0; it<numTaxa; it++)
			allstates |= getState(ic, it);
		return CategoricalState.maximum(allstates);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** returns the set of states in character ic*/
	public long getAllStates(int ic){
		if (notInStorage(ic, 0)) //illegal check
			return 0L;

		long allstates = 0;
		for (int it=0; it<numTaxa; it++)
			allstates |=getState(ic, it);
		return allstates & CategoricalState.statesBitsMask;
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** returns the maximum state in the matrix*/
	public int getMaxState(){
		if ((matrix == null && matrixShort == null) ) //illegal check
			return -1;
		long allstates = 0;
		for (int ic=0; ic<numChars; ic++)
			for (int it=0; it<numTaxa; it++)
				allstates |=getState(ic, it);
		return CategoricalState.maximum(allstates);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** returns the state of character ic in taxon it*/
	public  CharacterState getCharacterState(CharacterState cs, int ic, int it){
		if (notInStorage(ic, it)) //illegal check
			return null;
		if (cs!=null && cs.getClass()==CategoricalState.class) {
			((CategoricalState)cs).setValue(getStateRaw(ic, it));
			return cs;
		}
		return new CategoricalState(getStateRaw(ic, it));
	}
	/*..........................................  CategoricalData  ..................................................*/
	protected boolean notInStorage(int ic, int it) {
		if (ic>=numChars || it>=numTaxa || ic < 0 || it < 0) {
			return true;
		}
		if (matrix != null && (ic >= matrix.length || matrix.length<1 || matrix[0] == null || it >= matrix[0].length)) {
			if (!doomed) MesquiteMessage.printStackTrace("Error: CategoricalData notInStorage #1");
			return true;
		}
		if (matrixShort != null && (ic >= matrixShort.length || matrixShort.length<1 || matrixShort[0] == null || it >= matrixShort[0].length)){
			if (!doomed) MesquiteMessage.printStackTrace("Error: CategoricalData notInStorage #2");
			return true;
		}
		if (matrixShort == null && matrix == null){
			if (!doomed) MesquiteMessage.printStackTrace("Error: CategoricalData notInStorage #3");
			return true;
		}
		return false;
	}
	MesquiteInteger resultCode = new MesquiteInteger();
	/*..........................................  CategoricalData  ..................................................*/
	/** sets the state of character ic in taxon it to inapplicable*/
	public  void setToInapplicable(int ic, int it){
		setState(ic, it, CategoricalState.inapplicable);
		setDirty(true, ic, it);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** sets the state of character ic in taxon it to unassigned*/
	public  void setToUnassigned(int ic, int it){
		setState(ic, it, CategoricalState.unassigned);
		setDirty(true, ic, it);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** sets the state of character ic in taxon it from CharacterState cs*/
	public  void setState(int ic, int it, CharacterState cs){
		if (cs !=null && cs instanceof CategoricalState) {
			setState(ic, it, ((CategoricalState)cs).getValue());
			setDirty(true, ic, it);
		}
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** Set stateset in node it and character ic to state indicated by state symbol. */
	public void setState(int ic, int it, char c){
		setState(ic, it, fromChar(c));
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** Set stateset in node it and character ic to states. */
	public void setState(int ic, int it, long states){
		if (notInStorage(ic, it)) //illegal check
			return;
		if (usingShortMatrix()) {
			if (!CategoricalState.compressibleToShort(states)) {
				try {
					upgradeToFullMatrix();
					matrix[ic][it]= states;
				}
				catch (OutOfMemoryError e){
					matrix = null;
					MesquiteMessage.warnProgrammer("Sorry, the character state of character " + (ic+1) + " in taxon " + (it+1) + " could not be set to " + CategoricalState.toString(states) + " because of insufficient memory.  See file memory.txt in the Mesquite_Folder.");
				}
			}
			else {
				matrixShort[ic][it] = CategoricalState.compressToShort(states);
			}
		}
		else
			matrix[ic][it]= states;
		incrementStatesVersion();
		setDirty(true, ic, it);
	}

	/*..........................................  CategoricalData  ..................................................*/
	/**Set the state at character ic and taxon it from the string of the parser, beginning at current position of the parser.
	If pos == null, assumes from data editor and looks for state names, treats differently*/
	public int setStateQuickNexusReading(int ic, int it, Parser parser){ 
		resultCode.setValue(MesquiteInteger.unassigned);
		long v = fromStringQuickNexusReading(ic, parser, resultCode);
		if (v==CategoricalState.impossible) {
			//setState(ic, it, v);
			if (resultCode.getValue() == EOL)
				return EOL;
			return ERROR;
		}
		else if (v!=getStateRaw(ic, it)) {
			setState(ic, it, v);
			//	setDirty(true, ic, it);
		}
		if (resultCode.isUnassigned())
			return OK;
		else
			return resultCode.getValue();
	}
	/*..........................................  CategoricalData  ..................................................*/
	/**Set the state at character ic and taxon it from the string of the parser, beginning at current position of the parser.
	If pos == null, assumes from data editor and looks for state names, treats differently*/
	public int setState(int ic, int it, Parser parser, boolean fromEditor, MesquiteString result){ 
		if (notInStorage(ic, it)) {//illegal check
			if (result!=null)
				result.setValue("Character or taxon number out of bounds (Set state, CategoricalData)." + ic + "  " + ic);
			MesquiteMessage.printStackTrace("out of bounds ic " + ic + " it " + it);
			return OUTOFBOUNDS;
		}
		if (parser.blank()) {
			if (result!=null)
				result.setValue("State string is blank.");
			return ERROR;
		}
		resultCode.setValue(MesquiteInteger.unassigned);
		long v = fromString(ic, parser, fromEditor, resultCode, result);
		if (v==CategoricalState.impossible) {
			if (resultCode.getValue() == EOL)
				return EOL;
			if (result !=null) {
				if (fromEditor)
					result.setValue("\"" + parser.getString() + "\" cannot be translated into a state for this character");
				else
					result.setValue("Illegal state entry for categorical character");
			}
			return ERROR;
		}
		else if (v!=getStateRaw(ic, it)) {
			setState(ic, it, v);
			setDirty(true, ic, it);
		}
		if (resultCode.isUnassigned())
			return OK;
		else
			return resultCode.getValue();
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** return stateset in node it and character ic. */
	public long getState(long[] sequence, int ic){
		if (ic<0 ||  ic>=sequence.length)
			return CategoricalState.impossible;
		return CategoricalState.dataBitsMask & sequence[ic];
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** return stateset in node it and character ic. */
	public long getState(int ic, int it){
		if (notInStorage(ic, it)) //illegal check
			return CategoricalState.impossible;
		if (usingShortMatrix())
			return CategoricalState.dataBitsMask & CategoricalState.expandFromShort(matrixShort[ic][it]);
		else
			return CategoricalState.dataBitsMask & matrix[ic][it];
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** return stateset in node it and character ic, raw, as in matrix. */
	public long getStateRaw(int ic, int it){
		if (notInStorage(ic, it)) //illegal check
			return CategoricalState.impossible;
		if (usingShortMatrix())
			return CategoricalState.expandFromShort(matrixShort[ic][it]);
		else
			return matrix[ic][it];
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** returns true if state names are available for matrix */
	public boolean hasStateNames() {
		return (stateNames!=null);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** returns true if state names are available for character ic*/
	public boolean hasStateNames(int ic) {
		return (stateNames!= null && ic>=0 && ic<numChars && stateNames[ic]!=null );
	}
	/*.................................................................................................................*/
	public String getChecksumSummaryString(){
		CategoricalState state=null;
		long allstates =0L;
		long allData =0L;
		double sumSquaresStatesOnly = 0;
		double sumSquares = 0;
		for (int ic = 0; ic<numChars; ic++)
			for (int it=0; it<numTaxa; it++) {
				state =(CategoricalState)getCharacterState(state, ic, it);
				long st = state.getValue();
				sumSquares += st * st;
				if (!CategoricalState.isUnassigned(st) && !CategoricalState.isInapplicable(st)){
					allData |= st;
					st = st & CategoricalState.statesBitsMask;
					sumSquaresStatesOnly += st * st;
					allstates |= st;
				}
			}
		return "getNumChars " + getNumChars()+ " numChars " + numChars  + " getNumTaxa " + getNumTaxa()  + " numTaxa " + numTaxa + "   short " + usingShortMatrix() + "   bits " + allData + "   states " + allstates + "   sumSquaresStatesOnly " +  sumSquaresStatesOnly + " sumSquares " + sumSquares + " longCompressibleToShort " + longCompressibleToShort() + " usingShortMatrix " + usingShortMatrix();
	}
	/*..........................................  CategoricalData  ..................................................*/
	public long calculateChecksum(CRC32 crc32, int version){
		if (version == 1)
			return calculateChecksum(crc32);
		if (longCompressibleToShort()){
			byte[] bytes = new byte[2];
			crc32.reset();
			short unc = 0;
			if (version == 2)
				unc = (short)(((short)1)<<CategoricalState.uncertainBit);
			else
				unc = CategoricalState.compressToShort(1L<<CategoricalState.uncertainBit);
			for (int ic=0; ic<matrix.length; ic++)
				for (int it=0; it<matrix[ic].length; it++)  {
					long longState = matrix[ic][it];
					short state = CategoricalState.compressToShort(longState);
					if (CategoricalState.isUnassigned(state))
						state = CategoricalState.unassignedShort;
					else if (CategoricalState.isInapplicable(state))
						state = CategoricalState.inapplicableShort;
					else if (CategoricalState.isImpossible(state))
						state = 0;
					else
						state &= CategoricalState.statesBitsMaskShort | unc;
					bytes = MesquiteNumber.shortToBytes(state, bytes);
					crc32.update(bytes[0]);
					crc32.update(bytes[1]);
				}

			return crc32.getValue();
		}
		else if (usingShortMatrix()){
			byte[] bytes = new byte[2];
			crc32.reset();
			short unc = 0;
			if (version == 2)
				unc = (short)(((short)1)<<CategoricalState.uncertainBit);
			else
				unc = CategoricalState.compressToShort(1L<<CategoricalState.uncertainBit);
			for (int ic=0; ic<matrixShort.length; ic++)
				for (int it=0; it<matrixShort[ic].length; it++)  {
					short state = matrixShort[ic][it];
					if (CategoricalState.isUnassigned(state))
						state = CategoricalState.unassignedShort;
					else if (CategoricalState.isInapplicable(state))
						state = CategoricalState.inapplicableShort;
					else if (CategoricalState.isImpossible(state))
						state = 0;
					else
						state &= CategoricalState.statesBitsMaskShort | unc;
					bytes = MesquiteNumber.shortToBytes(state, bytes);
					crc32.update(bytes[0]);
					crc32.update(bytes[1]);
				}

			return crc32.getValue();
		}
		byte[] bytes = new byte[8];
		crc32.reset();
		for (int ic=0; ic<matrix.length; ic++)
			for (int it=0; it<matrix[ic].length; it++)  {
				long state = matrix[ic][it];
				if (CategoricalState.isUnassigned(state))
					state = CategoricalState.unassigned;
				else if (CategoricalState.isInapplicable(state))
					state = CategoricalState.inapplicable;
				else if (CategoricalState.isImpossible(state))
					state = 0L;
				else
					state &= CategoricalState.statesBitsMask | (1L<<CategoricalState.uncertainBit);

				bytes = MesquiteNumber.longToBytes(state, bytes);
				for (int i = 0; i<8; i++)
					crc32.update(bytes[i]);
			}

		return crc32.getValue();
	}
	/*..........................................  CategoricalData  ..................................................*/
	public long calculateChecksum(CRC32 crc32){
		byte[] bytes = new byte[8];
		crc32.reset();
		for (int ic=0; ic<numChars; ic++)
			for (int it=0; it<numTaxa; it++)  {
				long state = getStateRaw(ic, it);
				if (CategoricalState.isUnassigned(state))
					state = CategoricalState.unassigned;
				else if (CategoricalState.isInapplicable(state))
					state = CategoricalState.inapplicable;
				else if (CategoricalState.isImpossible(state))
					state = 0L;
				else
					state &= CategoricalState.statesBitsMask | (1L<<CategoricalState.uncertainBit);

				bytes = MesquiteNumber.longToBytes(state, bytes);
				for (int i = 0; i<8; i++)
					crc32.update(bytes[i]);
			}

		return crc32.getValue();
	}
	/*..........................................  CategoricalData  ..................................................*/
	public long calculateDataChecksum(int icStart, int icEnd, int itStart, int itEnd, CRC32 crc32){
		byte[] bytes = new byte[8];
		crc32.reset();
		for (int ic=icStart; ic<=icEnd; ic++)
			for (int it=itStart; it<=itEnd; it++)  {
				long state = getStateRaw(ic, it);
				if (!CategoricalState.isUnassigned(state) && !CategoricalState.isInapplicable(state) && !CategoricalState.isImpossible(state)) {
					state &= CategoricalState.statesBitsMask | (1L<<CategoricalState.uncertainBit);

					bytes = MesquiteNumber.longToBytes(state, bytes);
					for (int i = 0; i<8; i++)
						crc32.update(bytes[i]);
				}
			}

		return crc32.getValue();
	}
	private CRC32 tempcrc32 = new CRC32();
	/*.................................................................................................................*/
	public long storeCheckSum (int icStart, int icEnd, int itStart, int itEnd) {
		return calculateDataChecksum(icStart, icEnd, itStart, itEnd, tempcrc32);
	}
	/*.................................................................................................................*/
	public boolean examineCheckSum (int icStart, int icEnd, int itStart, int itEnd, String warning, MesquiteBoolean warnCheckSum, long originalCheckSum) {
		long newCheckSum = calculateDataChecksum(icStart, icEnd, itStart, itEnd, tempcrc32);
		if (originalCheckSum != newCheckSum) {
			if (!warnCheckSum.getValue())
				MesquiteTrunk.mesquiteTrunk.logln(warning);
			else if (!AlertDialog.query(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "  Checksum doesn't match",warning + "\n\nYou may suppress warnings of this type within this run of Mesquite.", "Continue", "Suppress warnings"))
				warnCheckSum.setValue(false);
			return false;
		} 
		else {
			MesquiteTrunk.mesquiteTrunk.logln("  Passed checksum");
			return true;
		}
	}
	/*.................................................................................................................*/
	public long storeCheckSum (int icStart, int icEnd, Bits whichTaxa) {
		return calculateDataChecksum(icStart, icEnd, whichTaxa, tempcrc32);
	}
	/*.................................................................................................................*/
	public boolean examineCheckSum (int icStart, int icEnd, Bits whichTaxa, String warning, MesquiteBoolean warnCheckSum, long originalCheckSum) {
		long newCheckSum = calculateDataChecksum(icStart, icEnd, whichTaxa, tempcrc32);
		if (originalCheckSum != newCheckSum) {
			if (!warnCheckSum.getValue())
				MesquiteTrunk.mesquiteTrunk.logln(warning);
			else if (!AlertDialog.query(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Checksum doesn't match",warning + "\n\nYou may suppress warnings of this type within this run of Mesquite.", "Continue", "Suppress warnings"))
				warnCheckSum.setValue(false);
			return false;
		} 
		else {
			MesquiteTrunk.mesquiteTrunk.logln("Passed checksum");
			return true;
		}
	}
	/*..........................................  CategoricalData  ..................................................*/
	public long calculateDataChecksum(int icStart, int icEnd, Bits whichTaxa, CRC32 crc32){
		byte[] bytes = new byte[8];
		crc32.reset();
		int start = whichTaxa.firstBitOn();
		if (start<0) start=0;
		int end = whichTaxa.lastBitOn();
		if (end<0) end = numTaxa-1;
		for (int ic=icStart; ic<=icEnd; ic++)
			for (int it=start; it<=end; it++)  
				if (whichTaxa.isBitOn(it)){
					long state = getStateRaw(ic, it);
					if (!CategoricalState.isUnassigned(state) && !CategoricalState.isInapplicable(state) && !CategoricalState.isImpossible(state)) {
						state &= CategoricalState.statesBitsMask | (1L<<CategoricalState.uncertainBit);

						bytes = MesquiteNumber.longToBytes(state, bytes);
						for (int i = 0; i<8; i++)
							crc32.update(bytes[i]);
					}
				}

		return crc32.getValue();
	}
	boolean anyImpossible(MesquiteInteger icM, MesquiteInteger itM){
		try {
			if (usingShortMatrix()){
				for (int ic=0; ic<matrixShort.length; ic++)
					for (int it=0; it<matrixShort[ic].length; it++)  {
						short state = matrixShort[ic][it];
						if (CategoricalState.isImpossible(state)) {
							icM.setValue(ic);
							itM.setValue(it);
							return true;
						}
					}
			}
			else {
				for (int ic=0; ic<matrix.length; ic++)
					for (int it=0; it<matrix[ic].length; it++)  {
						long state = matrix[ic][it];
						if (CategoricalState.isImpossible(state)) {
							icM.setValue(ic);
							itM.setValue(it);
							return true;
						}
					}
			}
		}
		catch (NullPointerException e){  //would happen if matrix being disposed of asynchronously
		}
		return false;
	}
	public String checkIntegrity(){
		String warning = super.checkIntegrity();
		MesquiteInteger ic = new MesquiteInteger();
		MesquiteInteger it = new MesquiteInteger();
		if (anyImpossible(ic, it)) {
			String note = "Matrix has impossible states [char " + ic + " taxon " + it + "]";
			if (warning == null)
				warning = note;
			else
				warning += "\n" + note;
		}
		return warning;
	}

	/*..........................................  CategoricalData  ..................................................*/
	/** returns true if state name for state s is available for character ic*/
	public boolean hasStateName(int ic, int s) {
		return (stateNames!= null && ic>=0 && ic<numChars && stateNames[ic]!=null && s< stateNames[ic].length && stateNames[ic][s]!=null);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** returns true if state name for state s is available for character ic*/
	public boolean hasStateFootnote(int ic, int s) {
		return (stateNotes!= null && ic>=0 && ic<numChars && stateNotes[ic]!=null && s< stateNotes[ic].length && stateNotes[ic][s]!=null);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** returns true if state name for state s is available for character ic*/
	public boolean hasStateAnnotations(int ic, int s) {
		return (stateAnnotations!= null && ic>=0 && ic<numChars && stateAnnotations[ic]!=null && s< stateAnnotations[ic].length && stateAnnotations[ic][s]!=null && ((AttachedNotesVector)stateAnnotations[ic][s]).getNumNotes()>0);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** sets state name of state "state" of character ic to name */
	public void setStateName(int ic, int state, String name) { //todo: check if in bounds
		if (ic< 0 || ic >= numChars)
			return;
		setDirty(true);
		if (StringUtil.blank(name))
			name = null;
		if (stateNames==null) {
			stateNames = new String[numChars][];
		}
		if (stateNames[ic]==null) {
			if (state ==0)
				stateNames[ic] = new String[2];
			else
				stateNames[ic] = new String[state+1];
		}
		else if (state>= stateNames[ic].length) {
			String[] newStateNames = new String[state+1];
			for (int i=0; i<stateNames[ic].length; i++) {
				newStateNames[i] = stateNames[ic][i];
			}
			stateNames[ic]=newStateNames;
		}
		stateNames[ic][state]=name;

		notifyListeners(this, new Notification(NAMES_CHANGED, new int[] {ic}));
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** gets state name of state "state" of character ic */
	private String getStateName(int ic, int state, boolean raw) {
		if (ic>=0 && stateNames!= null && state>=0 && stateNames.length> ic && stateNames[ic]!=null && state< stateNames[ic].length && stateNames[ic][state]!=null) {
			return stateNames[ic][state];
		}
		else if (!raw && symbols!=null && state < symbols.length)
			return String.valueOf(symbols[state]);
		else return null;
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** gets state name of state "state" of character ic */
	public String getStateName(int ic, int state) {
		return getStateName(ic, state, false);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** returns true if state notes are available for matrix */
	public boolean hasStateNotes() {
		return (stateNotes!=null);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** returns true if state note for state s is available for character ic*/
	public boolean hasStateNote(int ic, int s) {
		return (stateNotes!= null && ic>=0 && ic<numChars && stateNotes[ic]!=null && s< stateNotes[ic].length && stateNotes[ic][s]!=null);
	}

	/*..........................................  CategoricalData  ..................................................*/
	/** returns true if state notes are available for character ic*/
	public boolean hasStateNotes(int ic) {
		return (stateNotes!= null && ic>=0 && ic<numChars && stateNotes[ic]!=null );
	}

	/*..........................................  CategoricalData  ..................................................*/
	/** sets state name of state "state" of character ic to name */
	public void setStateNote(int ic, int state, String name) { //todo: check if in bounds
		setDirty(true);
		if (StringUtil.blank(name))
			name = null;
		if (stateNotes==null) {
			stateNotes = new String[numChars][];
		}
		if (stateNotes[ic]==null) {
			if (state ==0)
				stateNotes[ic] = new String[2];
			else
				stateNotes[ic] = new String[state+1];
		}
		else if (state>= stateNotes[ic].length) {
			String[] newStateNotes = new String[state+1];
			for (int i=0; i<stateNotes[ic].length; i++) {
				newStateNotes[i] = stateNotes[ic][i];
			}
			stateNotes[ic]=newStateNotes;
		}
		stateNotes[ic][state]=name;

		notifyListeners(this, new Notification(ANNOTATION_CHANGED, new int[] {ic}));
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** gets state name of state "state" of character ic */
	public String getStateNote(int ic, int state) {
		if (stateNotes!= null && stateNotes.length> ic && ic >= 0 && stateNotes[ic]!=null && state>=0 && state< stateNotes[ic].length && stateNotes[ic][state]!=null) {
			return stateNotes[ic][state];
		}
		else return null;
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** sets state name of state "state" of character ic to name */
	public void setStateAnnotationsVector(int ic, int state, AttachedNotesVector v) { //todo: check if in bounds
		setDirty(true);
		if (stateAnnotations==null) {
			stateAnnotations = new Object[numChars][];
		}
		if (stateAnnotations[ic]==null) {
			if (state ==0)
				stateAnnotations[ic] = new Object[2];
			else
				stateAnnotations[ic] = new Object[state+1];
		}
		else if (state>= stateAnnotations[ic].length) {
			Object[] newStateAnnotations = new Object[state+1];
			for (int i=0; i<stateAnnotations[ic].length; i++) {
				newStateAnnotations[i] = stateAnnotations[ic][i];
			}
			stateAnnotations[ic]=newStateAnnotations;
		}
		stateAnnotations[ic][state]=v;

		notifyListeners(this, new Notification(ANNOTATION_CHANGED, new int[] {ic}));
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** gets state name of state "state" of character ic */
	public AttachedNotesVector getStateAnnotationsVector(int ic, int state) {
		if (stateAnnotations!= null && stateAnnotations.length> ic && ic >= 0 && stateAnnotations[ic]!=null && state>=0 && state< stateAnnotations[ic].length && stateAnnotations[ic][state]!=null) {
			return (AttachedNotesVector)stateAnnotations[ic][state];
		}
		else return null;
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** returns state number for last state with name for character ic*/
	public int maxStateWithName(int ic) {
		if (ic<0 || ic>=getNumChars())
			return -1;
		if (stateNames == null || stateNames[ic]==null) 
			return -1;
		else
			for (int state=stateNames[ic].length-1; state >= 0; state--) {
				if (!((stateNames[ic][state] == null)||(stateNames[ic][state] == ""))){
					return state;
				}
			}
		return -1;
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** returns state number for last state scored, with name, with footnote, or with annotation for character ic*/
	public int maxStateWithDefinition(int ic) {
		if (ic < 0 || ic>=getNumChars())
			return -1;
		int maxState = Math.max(getMaxState(ic), maxStateWithName(ic));
		for (int st = maxState+1; st <= CategoricalState.maxCategoricalState; st++){
			if (getStateNote(ic, st) != null)
				maxState = st;
			else {
				AttachedNotesVector s = getStateAnnotationsVector(ic, st);
				if (s != null && s.getNumNotes()>0)
					maxState = st;
			}

		}
		return maxState;
	}
	/*-----------------------------------------------------------*/
	/** checks to see if the two cells have the same states */
	public boolean sameState(int ic1, int it1, int ic2, int it2){
		CharacterState cs1 = getCharacterState(null, ic1, it1);
		CharacterState cs2 = getCharacterState(null, ic2, it2);
		return cs1.equals(cs2);
	}
	/*-----------------------------------------------------------*/
	/** checks to see if the two cells have the same states */
	public boolean sameStateIgnoreCase(int ic1, int it1, int ic2, int it2){
		CategoricalState cs1 = (CategoricalState)getCharacterState(null, ic1, it1);
		CategoricalState cs2 = (CategoricalState)getCharacterState(null, ic2, it2);
		return cs1.equalsIgnoreCase(cs2);
	}
	/*-----------------------------------------------------------*/
	/** checks to see if the two cells have the same states */
	public boolean sameStateIgnoreCase(int ic1, int it1, int ic2, int it2, boolean allowMissing, boolean allowNearExact, boolean allowSubset){
		CategoricalState cs1 = (CategoricalState)getCharacterState(null, ic1, it1);
		CategoricalState cs2 = (CategoricalState)getCharacterState(null, ic2, it2);
		return cs1.equals(cs2,allowMissing,allowNearExact,allowSubset);
	}
	/*-----------------------------------------------------------*/
	/** checks to see if the two characters have identical distributions of states */
	public boolean samePattern(int oic, int ic){
		
		for (int it=0; it<numTaxa; it++) {
			if (!sameState(oic, it, ic, it))
				return false;
		}
		return true;
	}
	/*-----------------------------------------------------------*/
	/** Sets all cells in a character to inapplicable */
	public void clearCharacter(int ic){
		
		for (int it=0; it<numTaxa; it++) {
			setToInapplicable(ic,it);
		}
	}
	/*-----------------------------------------------------------*/
	/** Sets all cells in a taxon to inapplicable */
	public void clearTaxon(int it){
		
		for (int ic=0; ic<numChars; ic++) {
			setToInapplicable(ic,it);
		}
	}
	/*-----------------------------------------------------------*/
	public void equalizeCharacter(CharacterData oData, int oic, int ic){
		//state names
		if (oData instanceof CategoricalData){
			CategoricalData cData = (CategoricalData)oData;
			if (cData.stateNames == null)
				stateNames = null;
			else {
				for (int is = 0; is<= CategoricalState.maxCategoricalState; is++) {
					if (cData.hasStateName(oic, is)){
						String s = cData.getStateName(oic, is);
						if (!StringUtil.blank(s))
							setStateName(ic, is, s);
					}
				}
			} 
			for (int is = 0; is<= CategoricalState.maxCategoricalState; is++) {
				AttachedNotesVector s = cData.getStateAnnotationsVector(oic, is);
				if (s != null) 
					setStateAnnotationsVector(ic, is, s.cloneVector());
			}
		}
		super.equalizeCharacter(oData, oic, ic);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** gets state symbol for state "state", returned as string. */
	public static String getDefaultStateSymbol(Class dataClass, int state) {
		if (dataClass==DNAState.class) 
			return DNAData.getDefaultStateSymbol(state);
		else if (dataClass==RNAState.class) 
			return RNAData.getDefaultStateSymbol(state);
		else if (dataClass==ProteinState.class) 
			return ProteinData.getDefaultStateSymbol(state);
		else
			return getDefaultStateSymbol(state);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** gets state symbol for state "state", returned as string. */
	public static String getDefaultStateSymbol( int state) {
		if (state <0 || state>=defaultSymbols.length)
			return String.valueOf(CharacterData.defaultInapplicableChar);
		if (state>=0 &&  state <= CategoricalState.maxCategoricalState && defaultSymbols!=null )
			return String.valueOf(defaultSymbols[state]);
		else 
			return Integer.toString(state);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** gets state symbol for state "state" of character ic, returned as string. */
	public String getStateSymbol(int ic, int state) {

		if (state <0 || symbols == null || state>=symbols.length)
			return String.valueOf(getUnassignedSymbol());
		if (state>=0 && symbols!=null ) {
			return String.valueOf(symbols[state]);
		}
		else if (state >=0 && state <= CategoricalState.maxCategoricalState)
			return String.valueOf(defaultSymbols[state]);
		else 
			return Integer.toString(state);
	}
	/*..........................................CategoricalData................*/
	/** returns a long containing all of the states of a character.*/
	public long getAllStates(int ic, boolean selectedOnly){
		CategoricalState state=null;
		long allstates =0L;
		for (int it=0; it<numTaxa; it++) 
			if (!selectedOnly || getTaxa().getSelected(it)) {
				state =(CategoricalState)getCharacterState(state, ic, it);
				if (!CategoricalState.isUnassigned(state.getValue()) && !CategoricalState.isInapplicable(state.getValue()))
					allstates |= state.getValue();
			}
		return allstates;
	}
	/*..........................................CategoricalData................*/
	/** returns a String summarizing the states of a character (e.g., "2 states", "0.1-0.9").*/
	public String getStatesSummary(int ic, boolean selectedOnly){
		CategoricalState state=null;
		long allstates =0L;
		for (int it=0; it<numTaxa; it++) 
			if (!selectedOnly || getTaxa().getSelected(it)) {
				state =(CategoricalState)getCharacterState(state, ic, it);
				if (!CategoricalState.isUnassigned(state.getValue()) && !CategoricalState.isInapplicable(state.getValue()))
					allstates |= state.getValue();
			}
		return Integer.toString(CategoricalState.cardinality(allstates));
	}
	/*..........................................CategoricalData................*/
	/** returns a String summarizing the states of a character (e.g., "2 states", "0.1-0.9").*/
	public String getStatesSummary(int ic){
		return getStatesSummary(ic, false);
	}
	/*..........................................CategoricalData................*/
	/** returns an integer array summarizing the frequencies of states of a character.*/
	public int[] getStateFrequencyArray(int ic){
		CategoricalState state=null;
		int [] frequencies = new int[getTotalBitsInStateSet()];
		IntegerArray.zeroArray(frequencies);
		for (int it=0; it<numTaxa; it++) {
			state =(CategoricalState)getCharacterState(state, ic, it);
			if (state.isUnassigned())
				frequencies[CategoricalState.unassignedBit]++;
			else if (state.isInapplicable())
				frequencies[CategoricalState.inapplicableBit]++;
			else if (state.cardinality()==1){
				frequencies[CategoricalState.getOnlyElement(state.getValue())]++;
			}
			else if  (state.cardinality()>1)
				if (CategoricalState.isUncertain(state.getValue()))
					frequencies[CategoricalState.polymorphismElement]++;
				else
					frequencies[CategoricalState.uncertainBit]++;
		}
		return frequencies;
	}
	/*..........................................CategoricalData................*/
	/** returns an integer array summarizing the frequencies of states of a taxon.*/
	public int[] getStateFrequencyArrayOfTaxon(int it){
		CategoricalState state=null;
		int [] frequencies = new int[getTotalBitsInStateSet()];
		IntegerArray.zeroArray(frequencies);
		for (int ic=0; ic<numChars; ic++) {
			state =(CategoricalState)getCharacterState(state, ic, it);
			if (state.isUnassigned())
				frequencies[CategoricalState.unassignedBit]++;
			else if (state.isInapplicable())
				frequencies[CategoricalState.inapplicableBit]++;
			else if (state.cardinality()==1){
				frequencies[CategoricalState.getOnlyElement(state.getValue())]++;
			}
			else if  (state.cardinality()>1)
				if (CategoricalState.isUncertain(state.getValue()))
					frequencies[CategoricalState.polymorphismElement]++;
				else
					frequencies[CategoricalState.uncertainBit]++;
		}
		return frequencies;
	}
	/*..........................................CategoricalData................*/
	/** checks to see if two frequency arrays have the same applicable state frequencies.*/
	public boolean stateFrequencyArraysEqual(int[] array1, int[] array2){
		if (array1==null || array2==null)
			return false;
		if (array1.length!=array2.length)
			return false;
		if (array1.length==0 || array2.length==0)
			return false;
		for (int i=0; i<array1.length; i++) 
			if (i!=CategoricalState.inapplicableBit)  // don't check for inapplicables changing in frequency
				if (array1[i]!=array2[i])
					return false;
		return true;
		
	}
	/*..........................................CategoricalData................*/
	/** returns a String summarizing the frequencies of states of a character .*/
	public String  getStateFrequencyString(int ic){
		int [] frequencies = getStateFrequencyArray(ic);
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (int i=0; i<=getMaxState(); i++)
			if (frequencies[i]>0) {
				if (!first)
					sb.append(", ");
				sb.append(getStateSymbol(ic,i)+ ": " + frequencies[i]);
				first = false;
			}
		if (frequencies[CategoricalState.polymorphismElement]>0) {
			if (!first)
				sb.append(", ");
			sb.append("Polymorphisms: "+ frequencies[CategoricalState.polymorphismElement]);
			first = false;
		}
		if (frequencies[CategoricalState.uncertainBit]>0) {
			if (!first)
				sb.append(", ");
			sb.append("Ambiguous: " + frequencies[CategoricalState.uncertainBit]);
			first = false;
		}
		if (frequencies[CategoricalState.unassignedBit]>0) {
			if (!first)
				sb.append(", ");
			sb.append(getUnassignedSymbol()+ ": " + frequencies[CategoricalState.unassignedBit]);
			first = false;
		}
		if (frequencies[CategoricalState.inapplicableBit]>0) {
			if (!first)
				sb.append(", ");
			sb.append(getInapplicableSymbol()+ ": " + frequencies[CategoricalState.inapplicableBit]);
			first = false;
		}
		return sb.toString();
	}
	/*..........................................CategoricalData................*/
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
	/*..........................................CategoricalData................*/
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
	/*..........................................  CategoricalData  ..................................................*/
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
	/*..........................................  CategoricalData  ..................................................*/
	/** appends to buffer string describing the state(s) specified in the long array s.  The first element in s is presumed (for the sake of state symbols
	 * and state names) to correspond to character ic. */
	public void statesIntoStringBufferCore(int ic, long[] s, StringBuffer sb, boolean forDisplay, boolean includeInapplicable, boolean includeUnassigned){
		for (int i=0; i<s.length; i++) {
			statesIntoStringBufferCore(ic+i, s[i],  sb, forDisplay, includeInapplicable, includeUnassigned);
		}
	}

	/*..........................................  CategoricalData  ..................................................*/
	/** appends to buffer string describing the state(s) of character ic in taxon it. �*/
	public void statesIntoStringBufferCore(int ic, long s, StringBuffer sb, boolean forDisplay){
		statesIntoStringBufferCore(ic,s,sb,forDisplay, true, true);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** appends to buffer string describing the state(s) of character ic in taxon it. �*/
	public void statesIntoStringBufferCore(int ic, long s, StringBuffer sb, boolean forDisplay, boolean includeInapplicable, boolean includeUnassigned){
		if (s==CategoricalState.inapplicable) {
			if (includeInapplicable)
				sb.append(getInapplicableSymbol());
		}
		else if (s==CategoricalState.unassigned) {
			if (includeUnassigned)
				sb.append(getUnassignedSymbol());
		}
		else {
			boolean first=true;
			char sep;
			if (CategoricalState.isUncertain(s))
				sep = '/';
			else
				sep = '&';
			for (int e=0; e<=CategoricalState.maxCategoricalState; e++) {
				if (CategoricalState.isElement(s, e)) {
					if (!first)
						sb.append(sep);
					if (forDisplay)
						sb.append(getStateName(ic, e));
					else
						sb.append(getStateSymbol(ic, e));
					first=false;
				}
			}
			if (first)
				sb.append('!'); //no state found!
		}
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** appends to buffer string describing the state(s) of character ic in taxon it.�*/
	public void statesIntoStringBuffer(int ic, int it, StringBuffer sb, boolean forDisplay, boolean includeInapplicable, boolean includeUnassigned){
		if (notInStorage(ic, it)) //illegal check
			return;
		long s = getStateRaw(ic, it);
		statesIntoStringBufferCore(ic,s,sb, forDisplay, includeInapplicable, includeUnassigned);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** appends to buffer string describing the state(s) of character ic in taxon it.�*/
	public void statesIntoStringBuffer(int ic, int it, StringBuffer sb, boolean forDisplay){
		statesIntoStringBuffer(ic,it,sb, forDisplay, true, true);
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** appends to buffer string describing the state(s) of character ic in taxon it.�*/
	public void statesIntoStringBuffer(int ic, int it, StringBuffer sb, String separatorForMultistate, String bracketForMultistateStart, String bracketForMultistateEnd){
		if (notInStorage(ic, it)) //illegal check
			return;
		long s = getStateRaw(ic, it);
		if (s==CategoricalState.inapplicable) {
			sb.append(getInapplicableSymbol());
		}
		else if (s==CategoricalState.unassigned) {
			sb.append(getUnassignedSymbol());
		}
		else {
			boolean first=true;
			if (CategoricalState.hasMultipleStates(s))
				sb.append(bracketForMultistateStart);
			for (int e=0; e<=CategoricalState.maxCategoricalState; e++) {
				if (CategoricalState.isElement(s, e)) {
					if (!first)
						sb.append(separatorForMultistate);
					sb.append(getStateSymbol(ic, e));
					first=false;
				}
			}
			if (CategoricalState.hasMultipleStates(s))
				sb.append(bracketForMultistateEnd);
			if (first)
				sb.append('!'); //no state found!
		}
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** appends to buffer string describing the state(s) of character ic in taxon it. �*/
	public void statesIntoNEXUSStringBuffer(int ic, int it, StringBuffer sb){
		if (notInStorage(ic, it)) //illegal check
			return;
		boolean first=true;
		long s = getStateRaw(ic, it);
		if (s == 0L || s == CategoricalState.impossible)
			sb.append('!');
		else if (s==CategoricalState.inapplicable)
			sb.append(getInapplicableSymbol());
		else if (s==CategoricalState.unassigned)
			sb.append(getUnassignedSymbol());
		else {
			int card =0;
			int current = sb.length();
			for (int e=0; e<=CategoricalState.maxCategoricalState; e++) {
				if (CategoricalState.isElement(s, e)) {
					card++;
					if (!first) {
						sb.append(' ');
					}
					sb.append(getStateSymbol(ic, e));
					first=false;
				}
			}
			if (card>1) {
				if (CategoricalState.isUncertain(s)) {
					sb.insert(current,'{');
					sb.append('}');
				}
				else {
					sb.insert(current,'(');
					sb.append(')');
				}
			}
			if (first){ //nothing written!  Write illegal character so that change will not go unnoticed
				sb.append('!');
				if (ic == 0 && it == 0)
					ecount = 0;
				if (ecount++<100)
					MesquiteMessage.warnProgrammer("ERROR: nothing written for character " + (ic+1) + " taxon " + (it+1) + " state as long: " + s + "  (matrix " + getName() + ")");
			}
		}
	}
	int ecount = 0;

	/*..........................................  CategoricalData  ..................................................*/
	CategoricalState tempState = (CategoricalState)makeCharacterState(); //a utility CategoricalState for the fromChar method, which isn't static

	/*..........................................  CategoricalData  ..................................................*/
	/**Set the state at character ic from the string s, beginning at position pos in the string. �*/ 
	public long fromString(int ic, Parser parser, boolean fromEditor, MesquiteInteger resultCode, MesquiteString result){
		if (ic>=numChars || ic < 0) { //does not allow request for state for character other than those in matrix
			if (resultCode!=null)
				resultCode.setValue(OUTOFBOUNDS);
			return CategoricalState.impossible;
		}

		if (parser== null || parser.blank()) { //no string passed // 
			if (resultCode!=null)
				resultCode.setValue(ERROR);
			return CategoricalState.unassigned;
		}
		long stateSet = 0;
		int multi = 0;
		if (fromEditor)  { //string is from editor; thus will use /,& instead of {},() for uncertainty,polymorphism
			parser.setPunctuationString(puncString);
			parser.setPosition(0);
			parser.setWhitespaceString("");  // whitespace can be present WITHIN a state name!
			String t;
			while (!StringUtil.blank(t = parser.getNextToken())){
				//todo: what if linefeed comes in because parser so set?
				if (t.length()==1) {  //single character token
					char c = t.charAt(0) ;
					if (c == ANDseparator || c == ',') 
						multi = 1;
					else if (c == ORseparator) 
						multi = 2;
					else if (c == getInapplicableSymbol()) {
						multi = 0;
						stateSet = CategoricalState.inapplicable;
					}
					else if (c== getUnassignedSymbol()){
						multi = 0;
						stateSet = CategoricalState.unassigned;
					}
					else if (c == getMatchChar()) {
						stateSet = getStateRaw(ic, 0);
					}
					else {
						long thisState = fromChar(c);
						if (thisState == 0L)
							stateSet |= CategoricalState.makeSet(MesquiteInteger.fromString(t));
						else
							stateSet |= thisState;
					}
				}
				else { //multi character token
					boolean found=false;
					if (hasStateNames(ic)) { //try to interpret as state name
						for (int i=0; i<stateNames[ic].length && !found; i++) {
							if (t.equalsIgnoreCase(getStateName(ic, i))) {
								stateSet |= CategoricalState.makeSet(i);
								found = true;
							}
						}
					}
					if (!found) { //no state name; possibly several symbols without punctuation
						for (int i=0; i<t.length(); i++){
							long stt = fromChar(t.charAt(i));
							if (stt == CategoricalState.impossible) {
								if (result!=null) {
									result.setValue("Illegal entry in cell.");
									resultCode.setValue(ERROR);
								}
								return CategoricalState.impossible;
							}
							stateSet |= stt;
						}
						if (stateSet == 0L && result!=null) {
							result.setValue("Illegal entry in cell.");
							resultCode.setValue(ERROR);
						}
					}
				}
			}
		}

		else { //String is not from editor; assume it's from matrix in NEXUS file.  Thus, assume symbols, not names
			boolean done = false;
			int loc = parser.getPosition();
			parser.setPunctuationString(null);
			StringBuffer s = parser.getBuffer(); 
			while (loc<s.length() && !done) {
				char c = s.charAt(loc++); //get next dark character
				boolean wasWhitespace = false;
				while ((wasWhitespace=parser.whitespace(c))  && c!=0 && loc<s.length())
					c = s.charAt(loc++);

				if (!wasWhitespace){
					if (parser.lineEndCharacter(c)) {
						char r = parser.charOnDeck(1);
						//if next character is also lineEndCharacter but of opposite sort (i.e. \n instead of \r) then eat it up
						if (r!=c && parser.lineEndCharacter(r))
							loc++;
						if (resultCode!=null)
							resultCode.setValue(EOL);
						stateSet =CategoricalState.impossible;
						done = true;
					}
					else if (c == '(') //polymorphism
						multi = 1;
					else if (c == '{') //uncertainty
						multi = 2;
					else if (c == '}' || c == ')') 
						done = true;
					else if (c == getInapplicableSymbol()) {
						stateSet = CategoricalState.inapplicable;
						multi = 0;
						done = true;
					}
					else if (c== getUnassignedSymbol()) {
						done = true;
						multi = 0;
						stateSet = CategoricalState.unassigned;
					}
					else if (c == getMatchChar()) {
						done = true;
						//OKKKKKK
						stateSet = getStateRaw(ic, 0);
					}
					else if (multi>0) {
						long state =  fromChar(c);
						if (CategoricalState.isCombinable(state))
							stateSet |=  state;
					}
					else {
						stateSet =  fromChar(c);  //OKKKKKKK
						done = true;
					}
				}
			}
			parser.setPosition(loc);
		}
		if (multi == 2)
			stateSet = CategoricalState.setUncertainty(stateSet, true);
		return stateSet;
	}
	/*..........................................  CategoricalData  ..................................................*/
	public long obviousFromChar(char c){
		if (c == symbols[0])
			return 1L;
		else if (c == symbols[1])
			return 2L;
		return 0L;
	}
	/*..........................................  CategoricalData  ..................................................*/
	/**Set the state at character ic from the string s, beginning at position pos in the string. �*/ 
	public long fromStringQuickNexusReading(int ic, Parser parser, MesquiteInteger resultCode){
		long stateSet = 0;
		int multi = 0;
		boolean done = false;
		int loc = parser.getPosition();
		parser.setPunctuationString(null);
		StringBuffer s = parser.getBuffer(); 
		while (loc<s.length() && !done) {
			char c = s.charAt(loc++); //get next dark character
			long obviousState = obviousFromChar(c);
			if (obviousState != 0L && multi<1){   //  added "&& multi<1" as it couldn't process entries like "(01)" DRM 8 July '09
				parser.setPosition(loc);

				return obviousState;
			}
			else {
				boolean wasWhitespace = false;
				while ((wasWhitespace=parser.whitespace(c))  && c!=0 && loc<s.length())
					c = s.charAt(loc++);

				if (!wasWhitespace){
					if (parser.lineEndCharacter(c)) {
						char r = parser.charOnDeck(1);
						//if next character is also lineEndCharacter but of opposite sort (i.e. \n instead of \r) then eat it up
						if (r!=c && parser.lineEndCharacter(r))
							loc++;
						if (resultCode!=null)
							resultCode.setValue(EOL);
						stateSet =CategoricalState.impossible;
						done = true;
					}
					else if (c == '(') //polymorphism
						multi = 1;
					else if (c == '{') //uncertainty
						multi = 2;
					else if (c == '}' || c == ')') 
						done = true;
					else if (c == getInapplicableSymbol()) {
						stateSet = CategoricalState.inapplicable;
						multi = 0;
						done = true;
					}
					else if (c== getUnassignedSymbol()) {
						done = true;
						multi = 0;
						stateSet = CategoricalState.unassigned;
					}
					else if (c == getMatchChar()) {
						done = true;
						//OKKKKKK
						stateSet = getStateRaw(ic, 0);
					}
					else if (multi>0) {
						long state =  fromChar(c);
						if (CategoricalState.isCombinable(state))
							stateSet |=  state;
					}
					else {
						stateSet =  fromChar(c);  //OKKKKKKK
						done = true;
					}
				}
			}
		}
		parser.setPosition(loc);

		if (multi == 2)
			stateSet = CategoricalState.setUncertainty(stateSet, true);
		return stateSet;
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** Given a character, what state set is implied.  Looks up character among current symbols. �*/
	public long fromChar(char state){
		if (state == getInapplicableSymbol())
			return CategoricalState.inapplicable;
		else if (state== getUnassignedSymbol())
			return  CategoricalState.unassigned;
		else if (state == getMatchChar()) {
			return CategoricalState.unassigned; //note: this should't happen, as matchchar with no char #
		}
		else {
			for (int ist = 0; ist<symbols.length; ist++) {
				if (symbols[ist]==state) {
					return CategoricalState.makeSet(ist);
				}
			}
			return tempState.fromChar(state);
		}
	}
	/*..........................................CategoricalData.....................................*/
	/**returns the maximum possible state */
	public int getMaxPossibleState() {
		return CategoricalState.getMaxPossibleStateStatic();
	}
	/*..........................................CategoricalData.....................................*/
	/**returns the maximum possible state */
	public int getTotalBitsInStateSet() {
		return CategoricalState.getTotalBitsInStateSet();
	}
	/*..........................................CategoricalData.....................................*/
	/**returns the maximum possible state */
	public double[] getFrequencies(boolean includeAmbiguous, boolean ambiguousBasedOnFreq, int it1, int it2) {
		int max = getMaxPossibleState()+1;
		double[] freq = new double[max];
		long count = 0;
		for (int e = 0; e<max; e++)
			freq[e]=0.0;
		for (int ic= 0; ic<numChars; ic++){
			double[] freqIT = new double[max];;
			if (ambiguousBasedOnFreq) {
				long itCount = 0;
				for (int e = 0; e<max; e++)
					freqIT[e]=0.0;
				for (int it=0; it<numTaxa; it++) {
					if (it==it1 || it==it2 || it1<0 || it2<0 || !MesquiteInteger.isCombinable(it1) || !MesquiteInteger.isCombinable(it2)) {
						long s = getState(ic,it);
						int e = CategoricalState.getOnlyElement(s);
						if (e>=0 && e<max) {
							freqIT[e]+=1.0;
							itCount++;
						}
					}
				}
				if (itCount>0)
					for (int e = 0; e<max; e++)
						freqIT[e]=freqIT[e]/itCount;
			}
			for (int it=0; it<numTaxa; it++) {
				if (it==it1 || it==it2 || it1<0 || it2<0 || !MesquiteInteger.isCombinable(it1) || !MesquiteInteger.isCombinable(it2)) {
					long s = getState(ic,it);
					if (CategoricalState.isUnassigned(s)||CategoricalState.isInapplicable(s)||CategoricalState.isImpossible(s))
						continue;
					int e = CategoricalState.getOnlyElement(s);
					if (e>=0 && e<max) {
						freq[e]+=1.0;
						count++;
						continue;
					}
					int card = CategoricalState.cardinality(s);
					if (card>0) {
						if (!CategoricalState.isUncertain(s)) {

							for (e = 0; e<max; e++) {
								if (CategoricalState.isElement(s,e)) {
									count++;
									freq[e]+=1.0;
								}
							}
						}
						else if (includeAmbiguous) {
							count++;
							for (e = 0; e<max; e++) {
								if (CategoricalState.isElement(s,e))
									if (ambiguousBasedOnFreq && freqIT[e]!=0.0)
										freq[e]+=freqIT[e];
									else
										freq[e]+=1.0/card;
							}
						}
					}
				}
			}
		}
		if (count>0)
			for (int e = 0; e<max; e++)
				freq[e]=freq[e]/count;

		return freq;
	}

	/*..........................................CategoricalData.....................................*/
	/**merges the states for taxon it2 into it1  within this Data object */
	public boolean mergeSecondTaxonIntoFirst(int it1, int it2, boolean mergeMultistateAsUncertainty) {
		if ( it1<0 || it1>=getNumTaxa() || it2<0 || it2>=getNumTaxa() )
			return false;

		boolean mergedAssigned = false;
		for (int ic=0; ic<getNumChars(); ic++) {
			long s1 = getState(ic,it1);
			long s2 = getState(ic,it2);
			if ((s1& CategoricalState.statesBitsMask) != 0L && (s2& CategoricalState.statesBitsMask) != 0L)
				mergedAssigned = true;

			long sMerged = CategoricalState.mergeStates(s1,s2);
			if (mergeMultistateAsUncertainty && CategoricalState.hasMultipleStates(sMerged)) {   // set to uncertainty if it makes sense
				if ((CategoricalState.hasMultipleStates(s1) && !CategoricalState.isUncertain(s1)) || (CategoricalState.hasMultipleStates(s2) && !CategoricalState.isUncertain(s2))) {  // has polymorphism, don't do anything
				} else {
					sMerged = CategoricalState.setUncertainty(sMerged, true);		
				}
			}
			setState(ic,it1,sMerged);
		}
		return mergedAssigned;
	}
	/*..........................................CategoricalData.....................................*/
	/**merges the states for taxon it2 into it1  within this Data object */
	public boolean mergeSecondTaxonIntoFirst(int it1, int it2) {
		return mergeSecondTaxonIntoFirst(it1, it2, false);
	}


}


