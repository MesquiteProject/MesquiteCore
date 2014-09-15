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
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
/** A subclass of CharacterData for Protein data stored as Categorical sets (e.g, "{A, C}" */
public class ProteinData extends MolecularData {
	static char[] proteinSymbols;
	static String[] symbolsAsStrings;
	static String[] namesAsStrings;
	static String[] longNamesAsStrings;
	public static String DATATYPENAME="Protein Data";

	static {
		proteinSymbols = new char[] {'A', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'Y', '*', '1', '2', '3', '4'};
		symbolsAsStrings = new String[] {"A", "C", "D", "E", "F", "G", "H", "I", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "V", "W", "Y", "*", "1", "2", "3", "4"};
		namesAsStrings = new String[] {"Ala", "Cys", "Asp", "Glu", "Phe", "Gly", "His", "Ileu", "Lys", "Leu", "Met", "Asn", "Pro", "Gln", "Arg", "Ser", "Thr", "Val", "Trp", "Tyr", "Ter", "1", "2", "3", "4"};
		longNamesAsStrings = new String[] {"Alanine", "Cysteine", "Aspartic acid", "Glutamic acid", "Phenylalanine", "Glycine", "Histidine", "Isoleucine", "Lysine", "Leucine", "Methionine", "Asparagine", "Proline", "Glutamine", "Arginine", "Serine", "Threonine", "Valine", "Tryptophan", "Tyrosine", "Stop", "1", "2", "3", "4"};
	}

	public static final int ALA = 0;
	public static final int CYS = 1;
	public static final int ASP = 2;
	public static final int GLU = 3;
	public static final int PHE = 4;
	public static final int GLY = 5;
	public static final int HIS = 6;
	public static final int ILEU = 7;
	public static final int LYS = 8;
	public static final int LEU = 9;
	public static final int MET = 10;
	public static final int ASN = 11;
	public static final int PRO = 12;
	public static final int GLN = 13;
	public static final int ARG = 14;
	public static final int SER = 15;
	public static final int THR = 16;
	public static final int VAL = 17;
	public static final int TRP = 18;
	public static final int TYR = 19;
	public static final int TER = 20;
	public static final int STOP = 20;
	public static final int EXTRA1 = 21;
	public static final int EXTRA2 = 22;
	public static final int EXTRA3 = 23;
	public static final int EXTRA4 = 24;
	public static final int ABSENT=-1;

	static Color noBaseColor = new Color((float)240/255, (float)240/255, (float)240/255);
	public static Color multistateColor = new Color((float)100/255, (float)100/255, (float)50/255);
	static Color[] proteinColors;
	static {
		proteinColors = new Color[26];
		proteinColors[0] = new Color((float)130/255, (float)233/255, (float)1.0);
		proteinColors[1] = new Color((float)1.0, (float)171/255, (float)1.0);
		proteinColors[2] = new Color((float)174/255, (float)168/255, (float)118/255);
		proteinColors[3] = new Color((float)191/255, (float)185/255, (float)136/255);
		proteinColors[4] = new Color((float)148/255, (float)182/255, (float)1.0);
		proteinColors[5] = new Color((float)227/255, (float)6/255, (float)176/255);
		proteinColors[6] = new Color((float)185/255, (float)20/255, (float)1.0);
		proteinColors[7] = new Color((float)79/255, (float)155/255, (float)1.0);
		proteinColors[8] = new Color((float)167/255, (float)13/255, (float)227/255);
		proteinColors[9] = new Color((float)0.0, (float)18/255, (float)184/255);
		proteinColors[10] = new Color((float)94/255, (float)181/255, (float)181/255);
		proteinColors[11] = new Color((float)1.0, (float)0.0, (float)12/255);
		proteinColors[12] = new Color((float)20/255, (float)184/255, (float)110/255);
		proteinColors[13] = new Color((float)194/255, (float)1/255, (float)5/255);
		proteinColors[14] = new Color((float)214/255, (float)138/255, (float)1.0);
		proteinColors[15] = new Color((float)1.0, (float)223/255, (float)10/255);
		proteinColors[16] = new Color((float)176/255, (float)21/255, (float)26/255);
		proteinColors[17] = new Color((float)21/255, (float)255/255, (float)17/255);
		proteinColors[18] = new Color((float)192/255, (float)1.0, (float)156/255);
		proteinColors[19] = new Color((float)235/255, (float)144/255, (float)134/255);
		proteinColors[20] = new Color((float)0.0, (float)0.0, (float)0.0);
		proteinColors[21] = new Color((float)248/255, (float)170/255, (float)44/255);
		proteinColors[22] = new Color((float)1.0, (float)192/255, (float)2/255);
		proteinColors[23] = new Color((float)1.0, (float)238/255, (float)166/255);
		proteinColors[24] = new Color((float)1.0, (float)227/255, (float)186/255);
		proteinColors[25] = new Color((float)0.0, (float)0.0, (float)0.0);
	}
	public ProteinData(CharMatrixManager manager, int numTaxa, int numChars, Taxa taxa){
		super(manager, numTaxa, numChars, taxa);
		symbols=proteinSymbols;
		setUseDiagonalCharacterNames(false);

	}
	/*..........................................  ProteinData  ..................................................*/
	/** Indicates the type of character stored */ 
	public Class getStateClass(){
		return ProteinState.class;
	}
	/* .......................................... ProteinData .................................................. */
	public boolean colorCellsByDefault() { 
		return true;
	}
	/*..........................................  ProteinData  ..................................................*/
	public String getDataTypeName(){
		return ProteinData.DATATYPENAME;
	}
	/*.................................................................................................................*/
	/** Indicates whether the data are molecular sequence data or not */ 
	public boolean isMolecularSequence() {
		return true;
	}
	/*..........................................  CategoricalData  ..................................................*/
	/** Get symbol for state st. */
	public char getSymbol(int st) {
			//TODO: check that state is legal
		if (st<0 || st>=symbols.length)
			return 0;
		return symbols[st];
	}
	/*............................................................................................*/
	/** Get amino acid symbol for state st. */
	public static char getAASymbol(int st) {
		if (st<0 || st>=proteinSymbols.length)
			return 0;
		return proteinSymbols[st];
	}
	
	/*............................................................................................*

	protected int findSymbol(char symbol){
		for (int ist = 0; ist<symbols.length; ist++) {
			if (symbols[ist]==symbol) {
				return ist;
			}
		}
		return -1;
	}


	/*............................................................................................*/
	/** gets default state symbol for state "state" of character ic, returned as string. */
	public String getStateSymbol(int ic, int state) {
		if (state <0 || state>=symbols.length)
			return String.valueOf(getUnassignedSymbol());
		if (state>=0 && symbols!=null && state<symbols.length) {
			return String.valueOf(symbols[state]);
		}
		else if (state >=0 && state <= ProteinState.maxProteinState)
			return String.valueOf(defaultSymbols[state]);
		else 
			return Integer.toString(state);
	}
	/*............................................................................................*/
	/** gets default state symbol for state "state", returned as string. */
	public static String getDefaultStateSymbol(int state) {
		if (state <0 || state>=proteinSymbols.length)
			return String.valueOf(CharacterData.defaultInapplicableChar);
		if (state>=0 && proteinSymbols!=null && state<proteinSymbols.length) {
			return String.valueOf(proteinSymbols[state]);
		}
		else if (state >=0 && state <= ProteinState.maxProteinState)
			return String.valueOf(defaultSymbols[state]);
		else 
			return Integer.toString(state);
	}
	/*..........................................  ProteinData  ..................................................*/
	/**clone this CharacterData and return new copy.  Does not clone the associated specs sets etc. */
	public CharacterData cloneData(){
		ProteinData data = new ProteinData(matrixManager, numTaxa, numChars, getTaxa());
		for (int ic=0; ic<numChars; ic++){
			for (int it=0; it<numTaxa; it++) {
				data.setState(ic, it, getStateRaw(ic, it)); 
			}
			if (getSelected(ic))
				data.setSelected(ic, true);
		}
		data.resetCellMetadata();
		return data;
	}
	/*..........................................  ProteinData  ..................................................*/
	/**get matrix and return as StsOfCharacters */
   	public MCharactersDistribution getMCharactersDistribution(){
   		MProteinEmbedded states = new MProteinEmbedded(this);
		return states;
   	}
	/*..........................................  ProteinData  ..................................................*/
	/**return a CharacterDistribution that points to character ic */
   	public CharacterDistribution getCharacterDistribution(int ic){
   		ProteinEmbedded states = new ProteinEmbedded(this, ic);
		 return states;
   	}
	/*..........................................  ProteinData  ..................................................*/
	/**Return CharacterDistribution object with same number of taxa as this object (but not with any particular data; just so that of same data type) */
   	public CharacterDistribution makeCharacterDistribution(){
   		ProteinAdjustable c= new ProteinAdjustable(getTaxa(), numTaxa);
   		c.setParentData(this);
   		return c;
   	}
	/*..........................................  ProteinData  ..................................................*/
   	/** creates an empty CharacterState object of the same data type as CharacterData subclass used.*/
   	public CharacterState makeCharacterState(){
   		return new ProteinState();
   	}
	/*..........................................  ProteinData  ..................................................*/
	/**Return CharacterData object with same number of taxa & characters as this object (but not with any particular data; just so that of same data type) */
   	public CharacterData makeCharacterData() {
   		return new ProteinData(getMatrixManager(), getNumTaxa(), getNumChars(), getTaxa());
   	}
   	public CharacterData makeCharacterData(CharMatrixManager manager, Taxa taxa){
   		return new ProteinData(getMatrixManager(), taxa.getNumTaxa(), 0, taxa);
   	}
	/*..........................................  ProteinData  ..................................................*/
	/**Return CharacterData object with passed number of taxa & characters (but not with any particular data; just so that of same data type) */
   	public CharacterData makeCharacterData(int ntaxa, int nchars) {
   		return new ProteinData(getMatrixManager(), ntaxa, nchars, getTaxa());
   	}
	/*..........................................  ProteinData  ..................................................*/
   	/** returns whether the matrix would prefer to have columns sized individually in editors.  Default is true.*/
	public boolean pleaseAutoSizeColumns() {
		return false;
	}
	/*..........................................  ProteinData  ..................................................*/
   	/** returns the state of character ic in taxon it*/
   	public  CharacterState getCharacterState(CharacterState cs, int ic, int it){
   		if (cs!=null && cs.getClass()==ProteinState.class) {
   			((ProteinState)cs).setValue(getStateRaw(ic, it));
   			return cs;
		}
   		return new ProteinState(getStateRaw(ic, it));
   	}
	/*..........................................  ProteinData  ..................................................*/
	/** gets state name of state "state" of character ic */
	public String getStateName(int ic, int state) {
		/*if (stateNames!= null && state>=0 && stateNames.length> ic && stateNames[ic]!=null && state< stateNames[ic].length && stateNames[ic][state]!=null) {
			return stateNames[ic][state];
		}
		else */ if (symbolsAsStrings!=null && state < symbolsAsStrings.length)
			return symbolsAsStrings[state];//Integer.toString(state)
		else return null;
	}
	/*..........................................  ProteinData  ..................................................*/
	/** gets state name of state "state" */
	public static String getStateFullName(int state) {
		if (namesAsStrings!=null && state>=0 && state < namesAsStrings.length)
			return namesAsStrings[state];//Integer.toString(state)
		else return null;
	}
	/*..........................................  ProteinData  ..................................................*/
	/** gets the long state name of state "state"*/
	public static String getStateLongName(int state) {
		if (longNamesAsStrings!=null && state>=0 && state < longNamesAsStrings.length)
			return longNamesAsStrings[state];//Integer.toString(state)
		else return null;
	}
	/*..........................................  DNAData  ..................................................*/
	public Color getColorOfState(int ic, int istate, int maxState){
		return getProteinColorOfState(istate);
	}
	public Color getColorOfState(int ic, int istate){
		return getProteinColorOfState(istate);
	}
	public static Color getProteinColorOfState(int istate){
		if (istate>=0 && istate<=25)
			return proteinColors[istate];
		else
			return noBaseColor;
	}
//		return MesquiteColorTable.getDefaultColor(ProteinState.maxProteinState+1, istate, MesquiteColorTable.COLORS_NO_BW);
	/*..........................................    ..................................................*/
	public static Color getAminoAcidColor(long s){
		if (CategoricalState.hasMultipleStates(s))
			return multistateColor;
		else {
			int istate = CategoricalState.minimum(s);
			return getProteinColorOfState(istate);
		}
}
	/*..........................................    ..................................................*/
	public static Color getAminoAcidColor(long s, Color multipleStateColor){
		if (CategoricalState.hasMultipleStates(s))
			return multipleStateColor;
		else {
			int istate = CategoricalState.minimum(s);
			return getProteinColorOfState(istate);
		}
}

	/*...............  ProteinData  ...............*/
	public long getDefaultState(){
  		return CategoricalState.inapplicable;
	}

	/*..........................................  ProteinData  ..................................................*/
	/**Returns the states at character ic and taxon it as a string *
	public String statesToStringCore(int ic, long s) {
		if ((CategoricalState.cardinality(s) == 2) && CategoricalState.isUncertain(s)) {
			if (CategoricalState.isElement(s,2)&&CategoricalState.isElement(s,11))
				return "B";
			else if (CategoricalState.isElement(s,3)&&CategoricalState.isElement(s,13))
				return "Z";
			else
				return super.statesToStringCore(ic,s);
		}
		else
			return super.statesToStringCore(ic,s);
	}


	/*..........................................  ProteinData  ..................................................*/
   	/** appends to buffer string describing the state(s) of character ic in taxon it.*/
	public void statesIntoStringBufferCore(int ic, long s, StringBuffer sb, boolean forDisplay){
		statesIntoStringBufferCore(ic,s,sb,forDisplay, true, true);
	}
	/*..........................................  ProteinData  ..................................................*/
   	/** appends to buffer string describing the state(s) of character ic in taxon it.*/
	public void statesIntoStringBufferCore(int ic, long s, StringBuffer sb, boolean forDisplay, boolean includeInapplicable, boolean includeUnassigned){
		if ((CategoricalState.cardinality(s) == 2) && CategoricalState.isUncertain(s)) {
			if (CategoricalState.isElement(s,2)&&CategoricalState.isElement(s,11))
				sb.append("B");
			else if (CategoricalState.isElement(s,3)&&CategoricalState.isElement(s,13))
				sb.append("Z");
			else
				super.statesIntoStringBufferCore(ic,s, sb, forDisplay,includeInapplicable, includeUnassigned);
		}
		else
			super.statesIntoStringBufferCore(ic,s, sb, forDisplay,includeInapplicable, includeUnassigned);
	}
	/*................................................................................................*/
	private void appendStateSymbol(int e, StringBuffer sb){
			if (e>=0 && e<= ProteinState.maxProteinState)
				sb.append(symbols[e]);
	}
	
	/*................................................................................................*/
	public void statesIntoNEXUSStringBuffer(int ic, int it, StringBuffer sb){
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
			for (int e=0; e<=ProteinState.maxProteinState; e++) {
				if (CategoricalState.isElement(s, e)) {
					card++;
					if (!first)
						sb.append(' ');
					appendStateSymbol(e, sb);
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
					MesquiteMessage.warnProgrammer("ERROR: nothing written for character " + (ic+1) + " taxon " + (it+1)+ " state as long: " + s);
			}
		}
	}
	int ecount = 0;
	/*..........................................  ProteinData  ..................................................*/
	/* Returns state set from single state symbol �*/
   	public long fromChar(char state){
		if (state == getInapplicableSymbol())
			return CategoricalState.inapplicable;
		else if ((state== getUnassignedSymbol()) ||  (state == 'X')  ||  (state== 'x'))
			return  CategoricalState.unassigned;
		else if (state == getMatchChar()) {
			return CategoricalState.unassigned;//note: this should't happen, as matchchar with no char #
		}
		else {
   			return ProteinState.fromCharStatic(state);
  		}
 	}
	/*..........................................ProteinData.....................................*/
	/**returns the maximum possible state */
	public int getMaxPossibleState() {
		return ProteinState.getMaxPossibleStateStatic();
	}
	/* ..........................................ProteinData..................................... */
	/** returns the name of a non-missing, applicable entry in a matrix cell; e.g. "base" for DNAData*/
	public String getNameOfCellEntry(int number){
		if (number==1)
			return "amino acid";
		else
			return "amino acids";
	}

}



