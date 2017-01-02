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
import mesquite.lib.table.MesquiteTable;
import mesquite.lists.lib.ListModule;
import mesquite.molec.lib.*;

/* ======================================================================== */
/** A subclass of CharacterData for DNA data stored as Categorical sets (e.g, "{A, C}" */
public class DNAData extends MolecularData {
	boolean displayAsRNA = false;
	public static String DATATYPENAME="DNA Data";

	boolean colorCodPosInColumnNumber = true;
	public static final NameReference complementedRef = NameReference.getNameReference("complemented"); //long: tInfo, data(ch); MesquiteInteger: data(cells)

	public static Color dnaRed, dnaGreen, dnaYellow, dnaBlue;
	public static Color dnaRedPale, dnaGreenPale, dnaYellowPale, dnaBluePale, dnaInapplicable, dnaUnassigned;

	CodonPositionsSet codonPositionsSet = null;

	private static Vector defaultModels;

	static String A, C, G, T;
	static {
		defaultModels = new Vector(2);
		dnaRed = new Color((float) 1, (float) 0.3, (float) 0.3);
		dnaGreen = new Color((float) 0.3, (float) 1, (float) 0.3);
		dnaYellow = new Color((float) 1, (float) 1, (float) 0.3);
		dnaBlue = new Color((float) 0.4, (float) 0.4, (float) 1);

		dnaRedPale = new Color((float) 1, (float) 0.6, (float) 0.6);
		dnaGreenPale = new Color((float) 0.6, (float) 1, (float) 0.6);
		dnaYellowPale = new Color((float) 1, (float) 1, (float) 0.6);
		dnaBluePale = new Color((float) 0.7, (float) 0.7, (float) 1);
		dnaInapplicable = new Color((float)0.93, (float)0.90, (float)0.87);  //ColorDistribution.inapplicable
		dnaUnassigned = new Color((float)0.92, (float)0.94, (float)0.98); //ColorDistribution.unassigned;
		A = "A";
		C = "C";
		G = "G";
		T = "T";
	}

	public DNAData(CharMatrixManager manager, int numTaxa, int numChars, Taxa taxa) {
		super(manager, numTaxa, numChars, taxa);
		setUseDiagonalCharacterNames(false);

		super.setSymbolDirect(0, 'A');
		super.setSymbolDirect(1, 'C');
		super.setSymbolDirect(2, 'G');
		super.setSymbolDirect(3, 'T');
	}


	/* .......................................... DNAData .................................................. */
	public void setDisplayAsRNA(boolean displayAsRNA) {
		this.displayAsRNA = displayAsRNA;
	}

	/* .......................................... DNAData .................................................. */
	public boolean getDisplayAsRNA() {
		return displayAsRNA;
	}

	/* .......................................... DNAData .................................................. */
	public long getDefaultState() {
		return CategoricalState.inapplicable;
	}

	/* .......................................... DNAData .................................................. */
	/** Indicates the type of character stored */
	public Class getStateClass() {
		return DNAState.class;
	}

	/* ................................................................................................................. */
	/** Indicates whether the data are molecular sequence data or not */
	public boolean isMolecularSequence() {
		return true;
	}

	/* .......................................... DNAData .................................................. */
	public String getDataTypeName() {
		return DNAData.DATATYPENAME;
	}

	/* .......................................... DNAData .................................................. */
	/** clone this CharacterData and return new copy. Does not clone the associated specs sets etc. */
	public CharacterData cloneData() {
		DNAData data = new DNAData(matrixManager, numTaxa, numChars, getTaxa());
		if (!data.isValid())
			return null;
		for (int ic = 0; ic < numChars; ic++) {
			for (int it = 0; it < numTaxa; it++) {
				data.setState(ic, it, getStateRaw(ic, it));
			}
			if (getSelected(ic))
				data.setSelected(ic, true);
		}
		data.resetCellMetadata();
		return data;
	}

	/* .......................................... DNAData .................................................. */
	/** get matrix and return as StsOfCharacters */
	public MCharactersDistribution getMCharactersDistribution() {
		MDNAEmbedded states = new MDNAEmbedded(this);
		return states;
	}

	/* .......................................... DNAData .................................................. */
	/** return a CharacterDistribution that points to character ic */
	public CharacterDistribution getCharacterDistribution(int ic) {
		DNAEmbedded states = new DNAEmbedded(this, ic);
		return states;
	}

	/* .......................................... DNAData .................................................. */
	/** Return CharacterDistribution object with same number of taxa as this object (but not with any particular data; just so that of same data type) */
	public CharacterDistribution makeCharacterDistribution() {
		DNACharacterAdjustable c = new DNACharacterAdjustable(getTaxa(), numTaxa);
		c.setParentData(this);
		return c;
	}

	/* .......................................... DNAData .................................................. */
	/** creates an empty CharacterState object of the same data type as CharacterData subclass used. */
	public CharacterState makeCharacterState() {
		return new DNAState();
	}

	/* .......................................... DNAData .................................................. */
	/** Return CharacterData object with same number of taxa & characters as this object (but not with any particular data; just so that of same data type) */
	public CharacterData makeCharacterData() {
		return new DNAData(getMatrixManager(), getNumTaxa(), getNumChars(), getTaxa());
	}

	/* .......................................... DNAData .................................................. */
	/** Return CharacterData object with passed number of taxa & characters (but not with any particular data; just so that of same data type) */
	public CharacterData makeCharacterData(int ntaxa, int nchars) {
		return new DNAData(getMatrixManager(), ntaxa, nchars, getTaxa());
	}

	public CharacterData makeCharacterData(CharMatrixManager manager, Taxa taxa) {
		return new DNAData(getMatrixManager(), taxa.getNumTaxa(), 0, taxa);
	}

	/* .......................................... DNAData .................................................. */
	/** returns whether the matrix would prefer to have columns sized individually in editors. Default is true. */
	public boolean pleaseAutoSizeColumns() {
		return false;
	}

	/* .......................................... DNAData .................................................. */
	public boolean colorCellsByDefault() { // added 19Jan02
		return true;
	}



	/* .......................................... DNAData .................................................. */
	/** returns the state of character ic in taxon it */
	int countWarn = 0;
	public CharacterState getCharacterState(CharacterState cs, int ic, int it) {
		if (notInStorage(ic, it)) {
			String s = "character number (" + ic;
			if (ic>=numChars|| ic < 0) 
				s+="* [numChars " + numChars + "]";
			s+=") or taxon number (" + it ;
			if (it>=numTaxa || it < 0) 
				s+="* [numTaxa " + numTaxa + "]";
			s+=") out of bounds in getCharacterState";

			if (countWarn++ < 1000)
				MesquiteMessage.warnProgrammer(s);
			if (countWarn<4)
				MesquiteMessage.printStackTrace();
			return null;
		}
		if (cs != null && cs.getClass() == DNAState.class) {
			((DNAState) cs).setValue(getStateRaw(ic, it));
			return cs;
		}
		return new DNAState(getStateRaw(ic, it));
	}

	/* .......................................... DNAData .................................................. */
	public Color getColorOfState(int ic, int istate, int maxState) {
		return getDNAColorOfState(istate);
	}

	public Color getColorOfState(int ic, int istate) {
		return getDNAColorOfState(istate);
	}

	/* .......................................... CategoricalData .................................................. */
	/** Gets the color representing state(s) of character ic in taxon it */
	public Color getColorOfStates(int ic, int it) {
		if (notInStorage(ic, it)) // illegal check
			return ColorDistribution.unassigned;
		long s = getStateRaw(ic, it);
		if (CategoricalState.isCombinable(s)) {
			int colorCount = CategoricalState.cardinality(s);
			if (colorCount > 1) {
				return Color.lightGray;
			}
			else {
				return getDNAColorOfState(CategoricalState.maximum(s));
			}
		}
		else if (isInapplicable(ic,it))
			return dnaInapplicable;
		else
			return dnaUnassigned;
	}

	public static Color getDNAColorOfState(int istate) {
		if (istate == 0)
			return dnaRed;
		else if (istate == 1)
			return dnaGreen;
		else if (istate == 2)
			return dnaYellow;
		else if (istate == 3)
			return dnaBlue;
		else
			return dnaUnassigned;
	}

	public static Color getDNAColorOfStatePale(int istate) {
		if (istate == 0)
			return dnaRedPale;
		else if (istate == 1)
			return dnaGreenPale;
		else if (istate == 2)
			return dnaYellowPale;
		else if (istate == 3)
			return dnaBluePale;
		else
			return dnaUnassigned;
	}
	/** returns the color of character ic; e.g., to indicate codon positions */
	public Color getDefaultCharacterColor(int ic) {
		if (colorCodPosInColumnNumber) {
			int position = getCodonPosition(ic);
			if (position >= 1 && position <= 3)
				return ColorDistribution.codPosMedium[position - 1];
			else
				return null; // ColorDistribution.codPosMedium[3];
		}
		return super.getDefaultCharacterColor(ic);
	}

	/** returns the dark color of character ic; e.g., to indicate codon positions */
	public Color getDarkDefaultCharacterColor(int ic) {
		if (colorCodPosInColumnNumber) {
			int position = getCodonPosition(ic);
			if (position >= 1 && position <= 3)
				return ColorDistribution.codPosDark[position - 1];
			else
				return ColorDistribution.codPosDark[3];
		}
		return null;
	}

	/* .......................................... DNAData .................................................. */
	/** returns the codon position of character ic in taxon it */
	public int getCodonPosition(int ic) {
		if (ic>=getNumChars(false) || ic<0)
			return MesquiteInteger.unassigned;
		if (codonPositionsSet == null)
			codonPositionsSet = (CodonPositionsSet) getCurrentSpecsSet(CodonPositionsSet.class);
		if (codonPositionsSet != null && ic < codonPositionsSet.getNumberOfParts() && ic >= 0)
			return codonPositionsSet.getInt(ic);
		return MesquiteInteger.unassigned;
	}

	/* .......................................... DNAData .................................................. */
	/** returns the whether character ic is in a first, second, or third position */
	public boolean isCoding(int ic) {
		int position = getCodonPosition(ic);
		return  (position >= 1 && position <= 3);

	}
	/*.................................................................................................................*/
	public void setCodonPosition(int ic,  int pos, boolean canCreateCodPosSet, boolean notify){
		boolean changed=false;
		CodonPositionsSet modelSet = (CodonPositionsSet) getCurrentSpecsSet(CodonPositionsSet.class);
		if (modelSet == null && canCreateCodPosSet) {
			modelSet= new CodonPositionsSet("Codon Positions", getNumChars(), this);
			modelSet.addToFile(getFile(), getProject(), (CharactersManager) getMatrixManager().findElementManager(CodonPositionsSet.class)); //THIS
			setCurrentSpecsSet(modelSet, CodonPositionsSet.class);
		}
		if (modelSet != null) {
			modelSet.setValue(ic,pos);
			changed=true;
		}
		if (notify) {
			if (changed)
				notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  //not quite kosher; HOW TO HAVE MODEL SET LISTENERS??? -- modelSource
		}
	}
	/* .......................................... DNAData .................................................. */
	public void assignCodonPositionsToTerminalChars(int whichTermChars){
		if (whichTermChars<0) {  //added whichTermChars to front, so we need to get info from the one just after those, which is character -whichTermChars
			int nextChar = -whichTermChars;
			int nextPos = getCodonPosition(nextChar);
			if (MesquiteInteger.isCombinable(nextPos) && (nextPos>=1) && (nextPos<=3)) { // the immediately adjacent character (the previous first character) has an assigned codon position
				for (int ic=nextChar-1; ic>=0; ic--) {
					nextPos--;
					if (nextPos<=0) nextPos=3;
					setCodonPosition(ic,nextPos,false,false);
				}
			}
		} else if (whichTermChars>0) {  //added whichTermChars to end, so we need to get info from the one just before then.
				// the last character is character getNumChars()-1.  We then go back whichTermChars more to find the one we want.
			int nextChar = getNumChars()-1-whichTermChars;
			int nextPos = getCodonPosition(nextChar);
			if (MesquiteInteger.isCombinable(nextPos) && (nextPos>=1) && (nextPos<=3)) { // the immediately adjacent character (the previous first character) has an assigned codon position
				for (int ic=nextChar+1; ic<getNumChars(); ic++) {
					nextPos++;
					if (nextPos>3) nextPos=1;
					setCodonPosition(ic,nextPos,false,false);
				}
			}
		}
	}


	/* .......................................... DNAData .................................................. */
	/** returns if there is a current codon position set */
	public boolean isCurrentCodonPositionsSet() {
		if (codonPositionsSet == null)
			codonPositionsSet = (CodonPositionsSet) getCurrentSpecsSet(CodonPositionsSet.class);
		return (codonPositionsSet != null);
	}

	/*-----------------------------------------------------------*/
	/** checks to see if the two cells have the same states */
	public boolean sameState(int ic1, int it1, int ic2, int it2){
		DNAState cs1 = (DNAState)getCharacterState(null, ic1, it1);
		DNAState  cs2 = (DNAState)getCharacterState(null, ic2, it2);
		return DNAState.equalsIgnoreCase(cs1.getValue(), cs2.getValue());
	}
	/* .......................................... DNAData .................................................. */
	/** returns if there is there is at least one base that is codon position 1, 2, or 3 */
	public boolean someCoding() {
		if (!isCurrentCodonPositionsSet())
			return false;
		for (int ic=0; ic<getNumChars(); ic ++){
			int cp = getCodonPosition(ic);
			if (cp>=1 && cp<=3)
				return true;
		}
		return false;
	}
	/* .......................................... DNAData .................................................. */
	/** returns if there is there is at least one base that is codon position specified */
	public boolean anyCodPos(int pos, boolean considerExcluded) {
		if (!isCurrentCodonPositionsSet())
			return false;
		for (int ic=0; ic<getNumChars(); ic ++){
			if (getCodonPosition(ic)==pos && (considerExcluded || isCurrentlyIncluded(ic)))
				return true;
		}
		return false;
	}

	/* .......................................... .................................................. */
	/** Returns true if ic is the first position in a codon. */
	public boolean isStartOfCodon(int ic) {
		if (ic + 3 > getNumChars()) 
			return false;
		return (getCodonPosition(ic)==1 && getCodonPosition(ic+1)==2 && getCodonPosition(ic+2)==3);

	}
	/* .......................................... .................................................. */
	/** Returns the character number of the start of the codon following the one in which character ic participates. */
	public int getStartOfNextCodon(int ic) {
		while (ic<getNumChars()){  //this was broken in 3 .03.  I added this outer while loop.  This would only look at the first candidate; if there were two 1's in a row, it would find no more codons
			int icPos = getCodonPosition(ic);
			int candidate = -1;
			if (icPos==1)
				ic++;
			icPos = getCodonPosition(ic);


			while (icPos!=1 && ic<getNumChars()){
				icPos = getCodonPosition(ic);
				if (icPos==1) {
					candidate = ic;
					break;
				}
				ic++;
			}

			int[] triplet = getCodonTriplet(candidate);   
			if (triplet!=null)
				return candidate;
		}
		return -1;
	}
	/* ................................................................................................................. */
	/** Returns the codon in which character ic in taxon it participates. Returned is a long[3] containing the three DNAStates */
	public long[] getCodon(int ic, int it) {
		long[] codon = new long[3];
		int[] triplet = getCodonTriplet(ic);

		if (triplet != null) {
			codon[0] = getState(triplet[0], it);
			codon[1] = getState(triplet[1], it);
			codon[2] = getState(triplet[2], it);
			return codon;
		}
		return null;
	}
	/* ................................................................................................................. */
	/** Returns the codon in which character ic in taxon it participates. Returned is a long[3] containing the three DNAStates */
	public long[] getCodon(int[] triplet, int it) {
		long[] codon = new long[3];

		if (triplet != null) {
			codon[0] = getState(triplet[0], it);
			codon[1] = getState(triplet[1], it);
			codon[2] = getState(triplet[2], it);
			return codon;
		}
		return null;
	}

	/* ................................................................................................................. */
	/** Returns the codon in which character ic in taxon it participates. Returned is a long[3] containing the three DNAStates */
	public long[] getCodon(long[] sequence, int ic) {
		long[] codon = new long[3];
		int[] triplet = getCodonTriplet(ic);

		if (triplet != null) {
			codon[0] = getState(sequence, triplet[0]);
			codon[1] = getState(sequence, triplet[1]);
			codon[2] = getState(sequence, triplet[2]);
			return codon;
		}
		return null;
	}

	/* ................................................................................................................. */
	/** Returns the amino acid */
	public static long getAminoAcid(long[] codon, GeneticCode geneticCode) {
		if (codon == null || geneticCode == null)
			return CategoricalState.inapplicable;
		if (DNAState.isUnassigned(codon[0]) && DNAState.isUnassigned(codon[1]) && DNAState.isUnassigned(codon[2]))
			return CategoricalState.unassigned;
		if (DNAState.isInapplicable(codon[0]) || DNAState.isInapplicable(codon[1]) || DNAState.isInapplicable(codon[2]))
			return CategoricalState.inapplicable;

		long s = DNAState.emptySet();
		if (DNAState.isUncertain(codon[0]) || DNAState.isUncertain(codon[1]) || DNAState.isUncertain(codon[2]))
			s = DNAState.setUncertainty(s, true);
		if (DNAState.isUnassigned(codon[0]) || DNAState.isUnassigned(codon[1]) || DNAState.isUnassigned(codon[2]))
			s = DNAState.setUncertainty(s, true);
		if (codon != null) {
			for (int i = 0; i <= 3; i++)
				if (DNAState.isElement(codon[0], i) || DNAState.isUnassigned(codon[0]))
					for (int j = 0; j <= 3; j++)
						if (DNAState.isElement(codon[1], j) || DNAState.isUnassigned(codon[1]))
							for (int k = 0; k <= 3; k++)
								if (DNAState.isElement(codon[2], k) || DNAState.isUnassigned(codon[2]))
									s = DNAState.addToSet(s, geneticCode.getCode(i, j, k));
			if (DNAState.isUncertain(s) && DNAState.cardinality(s)==1)
				s = DNAState.setUncertainty(s, false);
			return s;
		}
		return CategoricalState.inapplicable;
	}

	/* ................................................................................................................. */
	/** Returns the amino acid that is coded for by a codon that contains character ic in taxon it */
	public int[] getCodonTriplet(int ic) {
		int[] triplet = new int[3];
		triplet[0]=-1;
		triplet[1]=-1;
		triplet[2]=-1;
		int icPos = getCodonPosition(ic);
			switch (icPos) {
			case 1:  {// we are at a first position
				triplet[0]=ic;
				int ic2=-1;
				for (ic2=ic+1; ic2<numChars && triplet[1]<0; ic2++){
					int pos = getCodonPosition(ic2);
					if (pos==1 || pos==3)
						return null;
					if (pos==2){
						triplet[1]=ic2;
					}
				}
				for (int ic3=triplet[1]+1; ic3<numChars; ic3++){
					int pos = getCodonPosition(ic3);
					if (pos==1 || pos==2)
						return null;
					if (pos==3){
						triplet[2]=ic3;
						break;
					}
				}
				break;
			}
			case 2: {  // we are at a second position
				triplet[1]=ic;
				int ic2=-1;
				for (ic2=ic-1; ic2>=0 && triplet[0]<0; ic2--){
					int pos = getCodonPosition(ic2);
					if (pos==2 || pos==3)
						return null;
					if (pos==1){
						triplet[0]=ic2;
					}
				}
				for (int ic3=ic+1; ic3<numChars; ic3++){
					int pos = getCodonPosition(ic3);
					if (pos==1 || pos==2)
						return null;
					if (pos==3){
						triplet[2]=ic3;
						break;
					}
				}
				break;
			}
			case 3:  {// we are at a third position
				triplet[2]=ic;
				int ic2=-1;
				for (ic2=ic-1; ic2>=0 && triplet[1]<0; ic2--){
					int pos = getCodonPosition(ic2);
					if (pos==1 || pos==3)
						return null;
					if (pos==2){
						triplet[1]=ic2;
					}
				}
				for (int ic3=triplet[1]-1; ic3>=0; ic3--){
					int pos = getCodonPosition(ic3);
					if (pos==2 || pos==3)
						return null;
					if (pos==1){
						triplet[0]=ic3;
						break;
					}
				}
				break;
			}
			default:
				return null;
			}
			if (triplet[0]==-1 || triplet[1]==-1 || triplet[2]==-1 )
				return null;
			return triplet;
	}

	/* ................................................................................................................. */
	/** Returns the amino acid that is coded for by a codon that contains character ic in taxon it */
	public long getAminoAcid(int ic, int it, boolean checkForVariableCodes) {
		int[] triplet = getCodonTriplet(ic); 
		if (triplet!=null) {
			GeneticCode genCode = getGeneticCode(triplet[0]);
			if (genCode!=null && checkForVariableCodes) {
				if (!genCode.equals(getGeneticCode(triplet[1])) || !genCode.equals(getGeneticCode(triplet[2])))
					return CategoricalState.inapplicable;
			}
			return getAminoAcid(getCodon(triplet, it), genCode);
		}
		return CategoricalState.inapplicable;
	}
	/* ................................................................................................................. */
	/** Returns the amino acid that follows the one coded for by a codon that contains character ic in taxon it */
	public long getNextAminoAcid(MesquiteInteger currentCharacter, int it, boolean checkForVariableCodes) {
		if (currentCharacter==null)
			return CategoricalState.inapplicable;
		int ic = currentCharacter.getValue();
		int[] triplet = getCodonTriplet(ic); 
		if (triplet!=null) {
			triplet = getCodonTriplet(triplet[2]+1);  // go one more character along
			if (triplet!=null) {
				currentCharacter.setValue(triplet[0]);
				GeneticCode genCode = getGeneticCode(triplet[0]);
				if (genCode!=null && checkForVariableCodes) {
					if (!genCode.equals(getGeneticCode(triplet[1])) || !genCode.equals(getGeneticCode(triplet[2])))
						return CategoricalState.inapplicable;
				}
				return getAminoAcid(getCodon(triplet, it), genCode);
			}
		}
		return CategoricalState.inapplicable;
	}

	/* ................................................................................................................. */
	/** Returns whether or not ic participates in partial coding triplet, i.e., one not containing all three nucleotides*/
	public boolean isInPartialTriplet(int ic, int it, MesquiteInteger matchLength){
		int[] triplet = getCodonTriplet(ic); 
		if (triplet!=null) {
			boolean oneInapplicable = false;
			boolean oneApplicable = false;
			for (int i=0; i<=2; i++) 
				if (isInapplicable(triplet[i],it))
					oneInapplicable = true;
				else
					oneApplicable = true;
			if (oneApplicable && oneInapplicable) {
			if (matchLength!=null)
				matchLength.setValue(triplet[2]-triplet[0]+1);
				return true;
			}
		}
		return false;
	}

	/*.................................................................................................................*/
	/** Returns whether or not ic is start of a partial coding triplet, i.e., one not containing all three nucleotides*/
	public boolean isStartOfPartialTriplet(int ic, int it, MesquiteInteger matchLength){
		if (!someCoding()) 
			return false;
	
		if (getCodonPosition(ic)==1) {
			return isInPartialTriplet(ic,it,matchLength);
		}
		return false;
	}

	/* ................................................................................................................. */
	public  Color alterColorToDeemphasizeDegeneracy(int aa, Color color){
		int degeneracy = getAminoAcidDegeneracy(0,aa);
		if (degeneracy==1)
			color = ColorDistribution.darker(color, 0.3);
		else if (degeneracy==2)
			color = ColorDistribution.brighter(color, 0.5);
		else if (degeneracy>2)
			color = ColorDistribution.brighter(color, 0.1); 
		else 
			color = Color.white;				

		return color;
	}
	/* ................................................................................................................. */
	public  Color alterColorToDeemphasizeDegeneracy(int ic, long s, Color color){
		if (!CategoricalState.hasMultipleStates(s)) {
			int aa = CategoricalState.minimum(s);
			int degeneracy = getAminoAcidDegeneracy(ic,aa);
			if (degeneracy==1)
				color = ColorDistribution.darker(color, 0.3);
			else if (degeneracy==2)
				color = ColorDistribution.brighter(color, 0.5);
			else if (degeneracy>2)
				color = ColorDistribution.brighter(color, 0.1); 
			else 
				color = Color.white;				
		}

		return color;
	}
	/* ................................................................................................................. */
	/** Returns the amino acid that is coded for by a codon that contains character ic in taxon it */
	public int getAminoAcidDegeneracy(int ic, int aa) {
		GeneticCode genCode = getGeneticCode(ic);
		if (genCode!=null)
			return genCode.getDegeneracy(aa);
		return 0;
	}

	/* ................................................................................................................. */
	/** Returns the amino acid that is coded for by a codon that contains character ic in taxon it */
	public long getAminoAcid(long[] sequence, int ic, boolean checkForVariableCodes) {
		if (sequence==null)
			return CategoricalState.inapplicable;
		int icPos = getCodonPosition(ic);
		GeneticCode genCode = getGeneticCode(ic);
		if (checkForVariableCodes) {
			switch (icPos) {
			case 1:  {// we are at a first position
				if (ic + 2 >= getNumChars())
					return CategoricalState.inapplicable;
				else
					if (!genCode.equals(getGeneticCode(ic+1)) || !genCode.equals(getGeneticCode(ic+2)))
						return CategoricalState.inapplicable;

				break;
			}
			case 2: {
				if (ic + 1 >= getNumChars() || ic-1<0)
					return CategoricalState.inapplicable;
				else
					if (!genCode.equals(getGeneticCode(ic-1)) || !genCode.equals(getGeneticCode(ic+1)))
						return CategoricalState.inapplicable;

				break;
			}
			case 3: {
				if (ic - 2 <0)
					return CategoricalState.inapplicable;
				else
					if (!genCode.equals(getGeneticCode(ic-2)) || !genCode.equals(getGeneticCode(ic-1)))
						return CategoricalState.inapplicable;

				break;
			}
			default:
				return CategoricalState.inapplicable;
			}
		}
		return getAminoAcid(getCodon(sequence, ic), genCode);
	}

	/* ................................................................................................................. */
	/** Returns true if there is more than one code within the current genetic code set */
	public boolean getVariableCodes() {
		GeneticCode genCode = getGeneticCode(0);
		for (int ic=1;ic<getNumChars(); ic++)
			if (!genCode.equals(getGeneticCode(ic)))
				return true;
		return false;
	}
	/* ................................................................................................................. */
	/** Returns the number of amino acids who state value is "aa" in taxon it */
	public int getAminoAcidNumbers(int it, int aa, boolean countEvenIfOthersInUncertain) {
		int count = 0;
		/*
		 * long s = 0; int ic=0; while (ic<getNumChars()&& ic>=0) { s = getAminoAcid(ic,it); if (CategoricalState.isElement(s,aa)) count++; ic = getStartOfNextCodon(ic); //returns -1 when no more codons }
		 */
		boolean variableCodes = getVariableCodes();
		if (countEvenIfOthersInUncertain) {
			for (int ic = 0; ic < getNumChars() && ic >= 0; ic = getStartOfNextCodon(ic)) {
				if (CategoricalState.isElement(getAminoAcid(ic, it, variableCodes), aa))
					count++;
			}
		}
		else {
			for (int ic = 0; ic < getNumChars() && ic >= 0; ic = getStartOfNextCodon(ic)) {
				long cellAA = getAminoAcid(ic, it, variableCodes);
				if (CategoricalState.isOnlyElement(cellAA, aa)) {
					count++;
				}
			}
		}
		return count;
	}
	/* ................................................................................................................. */
	/** Returns the number of amino acids who state value is "aa" in taxon it */
	public int getAminoAcidNumbers(int it, int aa) {
		return getAminoAcidNumbers(it,aa,true);
	}

	/*.................................................................................................................*  Deleted Oct 09 in favour of method in CodonPositionsSet
	public String getCodonsAsNexusCharSets(MesquiteInteger numberCharSets, MesquiteString charSetList){
		return getCodonsAsNexusCharSets(0,"", numberCharSets,charSetList);
	}
	/*.................................................................................................................*  Didn't handle \3 properly; deleted Oct 09 in favour of method in CodonPositionsSet
	public String getCodonsAsNexusCharSets(int startingCharNum, String startString, MesquiteInteger numberCharSets, MesquiteString charSetList){
		String sT= "";
		if (!someCoding())
			return sT;
		int numCharSets = 0;
		int unassignedPosition=4;
		String thisValueString = "";
		String cslist = "";
		int numChars = getNumChars();
//		MesquiteNumber codPos = new MesquiteNumber();
//		boolean firstTime = true;
		boolean someValues=false;


		for (int iw = 0; iw<4; iw++){
//			codPos.setValue(iw);
//			int continuing = 0;
			thisValueString = "";
			String charSetName = "";
			if (!StringUtil.blank(startString)){
				if (iw==0) 
					charSetName = StringUtil.tokenize(startString+"NonCoding");
				else 
					charSetName = StringUtil.tokenize(startString+"CodonPos" + iw);
			}
			else if (iw==0) 
				charSetName = "nonCoding";
			else 
				charSetName = "codonPos" + iw;
			thisValueString += "\n\tcharset " + charSetName + " = ";
			int lastWritten = -1;
			int nextWritten = -1;       
			int numInRun = 1;
			someValues=false;
			boolean firstOfThisSort = true;
//			boolean skipTwo = false;
			boolean inTripletRun = true;

			int ic = 0;

			while (ic<numChars) {
				if (getCodonPosition(ic)==iw) {
					someValues=true;
					if (firstOfThisSort) {
						if (numCharSets>0)
							cslist += ",";
						numCharSets++;
						cslist += " " + charSetName;
						firstOfThisSort = false;
					}
					lastWritten = ic;
					numInRun = 1;
					thisValueString += " " + CharacterStates.toExternal(ic+startingCharNum);
					while (ic<numChars && inTripletRun) {
						boolean nextIsNotSame = (ic+1<numChars && getCodonPosition(ic+1)!=iw) || ic + 1 >= numChars; 
						boolean next2IsNotSame = (ic+2<numChars && getCodonPosition(ic+2)!=iw) || ic + 2 >= numChars; 
						boolean next3IsSame = (ic+3<numChars && getCodonPosition(ic+3)==iw); 
						if (nextIsNotSame && next2IsNotSame && next3IsSame){
							ic += 3;
							nextWritten = ic;
							numInRun++;
						} else {
							if (numInRun>2)
								thisValueString += "-" + CharacterStates.toExternal(nextWritten+startingCharNum) + "\\3";
							else if (lastWritten!=nextWritten && nextWritten >= 0)  
								thisValueString += " " + CharacterStates.toExternal(nextWritten+startingCharNum);
							inTripletRun = false;
							numInRun=0;
						}
					}
				}
				ic++;
			}

			/*
			for (int ic=0; ic<getNumChars(); ic++) {
				if (getCodonPosition(ic)==iw) {  //we've found one of this sort.
					if (firstOfThisSort) {
						numCharSets++;
						if (iw == 0)
							cslist += "nonCoding ";
						else
							cslist += "codonPosition"+iw + " ";
					}

					if (continuing == 0) {  // the last one found was not of this sort
						lastWritten = ic;
						thisValueString += " " + CharacterStates.toExternal(ic);
						continuing = 1;
						skipTwo = false;
						inTripletRun = false;
						someValues = true;
					}
					else if (continuing == 1) { // we are in a run of these that is longer than 
						thisValueString += "-";
						continuing = 2;
						someValues = true;
					} 
					firstOfThisSort = false;
				}
				else if (continuing>0) {
					if (lastWritten != ic-1){
						thisValueString += " " + CharacterStates.toExternal(ic-1);
						lastWritten = ic-1;
						someValues = true;
					}
					else
						lastWritten = -1;
					continuing = 0;
				}

			}
			if (continuing>1) {
				thisValueString += " " + CharacterStates.toExternal(getNumChars()-1) + " ";
				someValues = true;
			}

/*

			thisValueString += ";";
			if (someValues) {
				sT += thisValueString;
			}
		} 
		if (numberCharSets !=null)
			numberCharSets.setValue(numCharSets);
		if (charSetList!=null)
			charSetList.setValue(cslist);
		return sT;
	} */	
	/* ................................................................................................................. */
	/** Returns a protein data object that corresponds to this nucleotide sequence */
	public ProteinData getProteinData(ProteinData protData, boolean warn) {
		if (!someCoding()) {
			if (warn)
				MesquiteMessage.discreetNotifyUser("Mesquite cannot translate DNA to Protein as codon positions have not been designated.");
			return null;
		}
		ProteinData proteins = protData;
		if (protData == null || protData.getTaxa() != getTaxa()) {
			CharactersManager manageCharacters = (CharactersManager) getMatrixManager().findElementManager(CharacterData.class);
			CharMatrixManager manager = manageCharacters.getMatrixManager(ProteinData.class);
			proteins = new ProteinData(manager, getNumTaxa(), getNumChars()/3+1, getTaxa());
		}
		boolean variableCodes = getVariableCodes();

		for (int it = 0; it < getNumTaxa(); it++) {
			int icProt = 0;
			for (int ic = 0; ic < getNumChars() && ic >= 0; ic = getStartOfNextCodon(ic)) {
				proteins.setState(icProt, it, getAminoAcid(ic, it, variableCodes));
				icProt++;
			}
		}

		return proteins;
	}

	/*.................................................................................................................*/
	/** gets the get titles for tabbed summary data about matrix*/
	public String getTabbedTitles() {
		return "Number of Taxa\tNumber of Characters\tA\tC\tG\tT";
	}
	/*.................................................................................................................*/
	/** gets the get  tabbed summary data about matrix*/
	public String getTabbedSummary() {
		StringBuffer sb = new StringBuffer();
		sb.append( ""+getNumTaxa() + "\t" + getNumChars()+"\t");
		double[] freq = getBaseFrequencies();
		if (freq !=null) {
			sb.append(""+ freq[0] + "\t" + freq[1] + "\t" + freq[2] + "\t" +freq[3]+"\t");
		} else
			sb.append("\t\t\t\t");
	

		return sb.toString();
	}
	/* ................................................................................................................. */
	public double[] getBaseFrequencies(){
		int[] freq = new int[4];
		for (int i = 0; i < 4; i++)
			freq[i] = 0;
		int count = 0;
		for (int it = 0; it < getNumTaxa(); it++) {
			for (int ic = 0; ic < getNumChars(); ic++) {
				long s = getStateRaw(ic, it);
				if (!CategoricalState.isUnassigned(s) && !CategoricalState.isInapplicable(s)) {
					count++;
					for (int i = 0; i < 4; i++)
						if (CategoricalState.isElement(s, i))
							freq[i]++;
				}
			}
		}
		if (count!=0)
			return new double[] {	((double) freq[0]) / count,((double) freq[1]) / count,((double) freq[2]) / count,((double) freq[3]) / count};
		return null;

	}
	/* ................................................................................................................. */
	/** gets the explanation of this matrix */
	public String getExplanation() {
		String extra = "This DNA character matrix for the taxa block \"" + getTaxa().getName() + "\" has " + getNumChars() + " characters for the " + getNumTaxa() + " taxa. Category of data: " + getDataTypeName() + "\n";
		double[] freq = getBaseFrequencies();
		if (freq !=null) {
			extra += "Frequency of A: " + freq[0] + " C: " + freq[1] + " G: " + freq[2] + " T: " +freq[3];
		}
		return extra;

	}

	/* ..........................................DNAData................ */
	public static DefaultReference findDefaultReference(NameReference paradigm) {
		if (defaultModels == null) {
			MesquiteMessage.warnProgrammer("findDefaultReference with null default models ");
			MesquiteMessage.printStackTrace();
			return null;
		}
		for (int i = 0; i < defaultModels.size(); i++) {
			DefaultReference dR = (DefaultReference) defaultModels.elementAt(i);

			if (dR.getParadigm() != null && dR.getParadigm().equals(paradigm))
				return dR;
		}
		return null;

	}

	/* ..........................................DNAData................ */
	public static void registerDefaultModel(String paradigm, String name) {
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

	/* ..........................................DNAData................ */
	public CharacterModel getDefaultModel(String paradigm) {
		if (paradigm == null)
			return null;
		NameReference p = NameReference.getNameReference(paradigm);
		DefaultReference dR = findDefaultReference(p);
		if (dR == null)
			return null;
		else {
			CharacterModel cm = getProject().getCharacterModel(dR.getDefault());
			if (cm == null)
				MesquiteMessage.println("Default model not found / " + dR.getDefault());
			return cm;
		}
	}




	/* .......................................... DNAData .................................................. */
	public static String collapseGaps(String seq) {
		if (seq == null)
			return null;
		StringBuffer s = new StringBuffer(seq.length());
		for (int i = 0; i < seq.length(); i++) {
			if (seq.charAt(i) != '-')
				s.append(seq.charAt(i));
		}
		return s.toString();
	}
	/* .......................................... DNAData .................................................. */
	public static String collapseSymbol(String seq, char remove) {
		if (seq == null)
			return null;
		StringBuffer s = new StringBuffer(seq.length());
		for (int i = 0; i < seq.length(); i++) {
			if (seq.charAt(i) != remove)
				s.append(seq.charAt(i));
		}
		return s.toString();
	}

	/* .......................................... DNAData .................................................. */
	public static String reverseString(String seq) {
		if (seq == null)
			return null;
		StringBuffer s = new StringBuffer(seq.length());
		for (int i = 0; i < seq.length(); i++)
			s.append(seq.charAt(seq.length() - i - 1));
		return s.toString();
	}

	/* .......................................... DNAData .................................................. */
	public static String complementString(String seq) {
		if (seq == null)
			return null;
		StringBuffer s = new StringBuffer(seq.length());
		for (int i = 0; i < seq.length(); i++)
			s.append(complementChar(seq.charAt(i)));
		return s.toString();
	}
	/* .......................................... DNAData .................................................. */
	public static long[] sequenceLongsFromString(String s){
		if (s == null)
			return null;
		long[] sites = new long[s.length()];

		for (int i = 0; i< s.length(); i++){
			sites[i] = DNAState.fromCharStatic(s.charAt(i));
			if (sites[i] == CategoricalState.impossible){
				sites[i] = CategoricalState.inapplicable;

			}
		}
		return sites;
	}


	/* .......................................... DNAData .................................................. */
	public static long[] sequenceLongsFromString(String s, int length){
		if (s == null)
			return null;
		int sL = s.length();
		if (length>sL)
			sL = length;
		long[] sites = new long[sL];
		for(int i=0; i<sL; i++)
			sites[i] = CategoricalState.inapplicable;
		for (int i = 0; i< s.length(); i++){
			sites[i] = DNAState.fromCharStatic(s.charAt(i));
			if (sites[i] == CategoricalState.impossible){
				sites[i] = CategoricalState.inapplicable;
			}
		}
		return sites;
	}
	/* .......................................... DNAData .................................................. */
	public static String sequenceStringFromLongs(long[] s){
		return sequenceStringFromLongs(s, false);
	}
	/* .......................................... DNAData .................................................. */
	public static String sequenceStringFromLongs(long[] s, boolean stripTerminalMissingOrGap){
		if (s == null)
			return null;
		StringBuffer sb = new StringBuffer(s.length);
		int start = 0;
		int end = s.length-1;
		if (stripTerminalMissingOrGap){
			boolean done = false;
			for (int i=0; i<s.length && !done; i++){
				if (s[i] != DNAState.inapplicable && s[i] != DNAState.unassigned){
					start = i;
					done = true;
				}
			}
			done = false;
			for (int i=s.length-1; i>=0 && !done; i--){
				if (s[i] != DNAState.inapplicable && s[i] != DNAState.unassigned){
					end = i;
					done = true;
				}
			}
		}

		for (int i = start; i<= end; i++){
			sb.append(DNAState.toCharStatic(s[i]));
		}
		return sb.toString();
	}

	/* .......................................... DNAData .................................................. */
	public static long[] convertTerminalMissingToGaps(long[] s){
		boolean done = false;
		for (int i=0; i<s.length && !done; i++){
			if (s[i] != DNAState.inapplicable && s[i] != DNAState.unassigned)
				done = true;
			if (s[i] == DNAState.unassigned)
				s[i] = DNAState.inapplicable;
		}
		done = false;
		for (int i=s.length-1; i>=0 && !done; i--){
			if (s[i] != DNAState.inapplicable && s[i] != DNAState.unassigned)
				done = true;
			if (s[i] == DNAState.unassigned)
				s[i] = DNAState.inapplicable;
		}
		return s;
	}

	/* .......................................... DNAData .................................................. */
	public static char complementChar(char c) {
		if (c == 'a')
			return 't';
		else if (c == 'A')
			return 'T';
		else if (c == 'c')
			return 'g';
		else if (c == 'C')
			return 'G';
		else if (c == 'g')
			return 'c';
		else if (c == 'G')
			return 'C';
		else if (c == 't')
			return 'a';
		else if (c == 'T')
			return 'A';

		else if (c == 'v')
			return 'b';
		else if (c == 'V')
			return 'B';
		else if (c == 'd')
			return 'h';
		else if (c == 'D')
			return 'H';
		else if (c == 'h')
			return 'd';
		else if (c == 'H')
			return 'D';
		else if (c == 'b')
			return 'v';
		else if (c == 'B')
			return 'V';
		else if (c == 'm')
			return 'k';
		else if (c == 'M')
			return 'K';
		else if (c == 'r')
			return 'y';
		else if (c == 'R')
			return 'Y';
		else if (c == 'y')
			return 'r';
		else if (c == 'Y')
			return 'R';
		else if (c == 'k')
			return 'm';
		else if (c == 'K')
			return 'M';
		else
			return c;
	}


	/* .......................................... DNAData .................................................. */
	/** Complement the stateset at ic,it */
	public void complement(int ic, int it) {
		long s = getStateRaw(ic, it);
		setState(ic, it, DNAState.complement(s));
	}

	/* .......................................... DNAData .................................................. */
	public boolean isComplemented( int it) {
		Associable tInfo = getTaxaInfo(false);
		if (tInfo!=null) {
			return tInfo.getAssociatedBit(complementedRef,it);
		}
		return false;
	}
	/* .......................................... DNAData .................................................. */
	public void setComplemented( int it, boolean value) {
		Associable tInfo = getTaxaInfo(false);
		if (tInfo!=null) {
			tInfo.setAssociatedBit(complementedRef, it, value);
		}
	}
	/* .......................................... DNAData .................................................. */
	/** Complements a stretch of DNA and complements linked data matrices too. */
	public void complement(int icStart, int icEnd, int it, boolean adjustComplementLinked) {
		if ((icStart==0 && icEnd==getNumChars()-1) || (!anyApplicableBefore(icStart, it)&& !anyApplicableAfter(icEnd,it))) {
			Associable tInfo = getTaxaInfo(true);
			if (tInfo!=null) {
				boolean prevValue = tInfo.getAssociatedBit(complementedRef,it);
				tInfo.setAssociatedBit(complementedRef, it, !prevValue);
			}
		}
		for (int ic = icStart; ic <= icEnd; ic++)
			complement(ic, it);
		if (adjustComplementLinked) {
			for (int i = 0; i < linkedDatas.size(); i++) {
				CharacterData d = (CharacterData) linkedDatas.elementAt(i);
				if (d instanceof DNAData)
					for (int ic = icStart; ic <= icEnd; ic++)
						((DNAData) d).complement(ic, it);
			}
		}
	}

	/* .......................................... DNAData .................................................. */
	/**
	 * Does the reverse complement of the data from character icStart to icEnd in taxon it. If reverseTerminalGaps is true, then it reversed the entire stretch; if it is false, it only reverses from the first non-gap to last non-gap in the stretch.
	 */
	public void reverseComplement(int icStart, int icEnd, int it, boolean reverseTerminalGaps, boolean adjustCellLinked) {
		reverse(icStart, icEnd, it, reverseTerminalGaps, adjustCellLinked);
		complement(icStart, icEnd, it, adjustCellLinked);
	}

	
	public void reverseComplement(int icStart, int icEnd, boolean adjustCellLinked) { //NOTE: this version reverses character metadata (codon positions, etc.)
		reverse(icStart, icEnd, adjustCellLinked);
		for (int it = 0; it<getNumTaxa(); it++)
			complement(icStart, icEnd, it, adjustCellLinked);
	}
	/* .......................................... DNAData .................................................. */
	/** Returns IUPAC symbol of a state */
	public static String getIUPACSymbol(long s) {
		boolean s0 = CategoricalState.isElement(s, 0);
		boolean s1 = CategoricalState.isElement(s, 1);
		boolean s2 = CategoricalState.isElement(s, 2);
		boolean s3 = CategoricalState.isElement(s, 3);

		if (s == CategoricalState.unassigned || (s0 && s1 && s2 && s3))
			return "N";
		else if (s0 && s1 && s2)
			return "V";
		else if (s0 && s2 && s3)
			return "D";
		else if (s0 && s1 && s3)
			return "H";
		else if (s1 && s2 && s3)
			return "B";
		else if (s0 && s1)
			return "M";
		else if (s0 && s2)
			return "R";
		else if (s0 && s3)
			return "W";
		else if (s1 && s2)
			return "S";
		else if (s1 && s3)
			return "Y";
		else if (s2 && s3)
			return "K";
		else if (s0)
			return "A";
		else if (s1)
			return "C";
		else if (s2)
			return "G";
		else if (s3)
			return "T";
		return "X";
	}

	/* .......................................... DNAData .................................................. */
	/** Returns IUPAC symbol of a state as a char */
	public static char getIUPACChar(long s) {
		boolean s0 = CategoricalState.isElement(s, 0);
		boolean s1 = CategoricalState.isElement(s, 1);
		boolean s2 = CategoricalState.isElement(s, 2);
		boolean s3 = CategoricalState.isElement(s, 3);

		if (s0 && s1 && s2 && s3)
			return 'N';
		else if (s0 && s1 && s2)
			return 'V';
		else if (s0 && s2 && s3)
			return 'D';
		else if (s0 && s1 && s3)
			return 'H';
		else if (s1 && s2 && s3)
			return 'B';
		else if (s0 && s1)
			return 'M';
		else if (s0 && s2)
			return 'R';
		else if (s0 && s3)
			return 'W';
		else if (s1 && s2)
			return 'S';
		else if (s1 && s3)
			return 'Y';
		else if (s2 && s3)
			return 'K';
		else if (s0)
			return 'A';
		else if (s1)
			return 'C';
		else if (s2)
			return 'G';
		else if (s3)
			return 'T';
		return 'X';
	}

	/* .......................................... DNAData .................................................. */
	/** appends to buffer string describing the state(s) of character ic in taxon it. */
	public void statesIntoStringBufferCore(int ic, long s, StringBuffer sb, boolean forDisplay) {
		statesIntoStringBufferCore(ic, s, sb, forDisplay, true, true);
	}

	/* .......................................... DNAData .................................................. */
	/** appends to buffer string describing the state(s) of character ic in taxon it. */
	public void statesIntoStringBufferCore(int ic, long s, StringBuffer sb, boolean forDisplay, boolean includeInapplicable, boolean includeUnassigned) {
		if (CategoricalState.cardinality(s) > 1 && CategoricalState.isUncertain(s))
			sb.append(getIUPACSymbol(s));
		else {
			if (s == CategoricalState.inapplicable) {
				if (includeInapplicable)
					sb.append(getInapplicableSymbol());
			}
			else if (s == CategoricalState.unassigned) {
				if (includeUnassigned) {
					if (!forDisplay)
						sb.append(getIUPACSymbol(s));
					else
						sb.append(getUnassignedSymbol());
				}
			}
			else {
				boolean first = true;
				char sep;
				if (CategoricalState.isUncertain(s))
					sep = '/';
				else
					sep = '&';
				boolean lowerCase = CategoricalState.isLowerCase(s);
				for (int e = 0; e <= CategoricalState.maxCategoricalState; e++) {
					if (CategoricalState.isElement(s, e)) {
						if (!first)
							sb.append(sep);
						appendStateSymbol(e, lowerCase, sb);
						first = false;
					}
				}
				if (first)
					sb.append('!'); // no state found!
			}
		}
	}

	/* ............................................................................................ */
	/** gets default state symbol for state "state", returned as string. */
	public static String getDefaultStateSymbol(int state) {
		if (state == 0)
			return "A";
		else if (state == 1)
			return "C";
		else if (state == 2)
			return "G";
		else if (state == 3)
			return "T";
		else
			return Integer.toString(state);
	}

	/* .......................................... DNAData .................................................. */
	/* Appends to buffer state symbol for state e � */
	protected void appendStateSymbol(int e, boolean lowerCase, StringBuffer sb) {
		if (lowerCase) {
			if (e == 0)
				sb.append('a');
			else if (e == 1)
				sb.append('c');
			else if (e == 2)
				sb.append('g');
			else if (e == 3) {
				if (displayAsRNA)
					sb.append('u');
				else
					sb.append('t');
			}
		}
		else {
			if (e == 0)
				sb.append('A');
			else if (e == 1)
				sb.append('C');
			else if (e == 2)
				sb.append('G');
			else if (e == 3) {
				if (displayAsRNA)
					sb.append('U');
				else
					sb.append('T');
			}
		}
	}

	/* .......................................... DNAData .................................................. */
	/* Fills buffer with string version of state in char ic and taxon it � */
	public void statesIntoNEXUSStringBuffer(int ic, int it, StringBuffer sb) {
		boolean first = true;
		long s = getStateRaw(ic, it);
		if (s == 0L || s == CategoricalState.impossible)
			sb.append('!');
		else if (s == CategoricalState.inapplicable)
			sb.append(getInapplicableSymbol());
		else if (s == CategoricalState.unassigned)
			sb.append(getUnassignedSymbol());
		else if (CategoricalState.cardinality(s) > 1 && CategoricalState.isUncertain(s))  //DavidJan07: OK?
			sb.append(getIUPACSymbol(s));
		else {
			int card = 0;
			int current = sb.length();
			for (int e = 0; e <= DNAState.maxDNAState; e++) {
				if (CategoricalState.isElement(s, e)) {
					card++;
					if (!first)
						sb.append(' ');
					appendStateSymbol(e, CategoricalState.isLowerCase(s), sb);
					first = false;
				}
			}
			if (card > 1) {
				if (CategoricalState.isUncertain(s)) {
					sb.insert(current, '{');
					sb.append('}');
				}
				else {
					sb.insert(current, '(');
					sb.append(')');
				}
			}
			if (first) { // nothing written! Write illegal character so that change will not go unnoticed
				sb.append('!');
				if (ic == 0 && it == 0)
					ecount = 0;
				if (ecount++ < 100)
					MesquiteMessage.warnProgrammer("ERROR: nothing written for character " + (ic + 1) + " taxon " + (it + 1) + " state as long: " + s);
			}
		}
	}

	int ecount = 0;

	/* .......................................... DNAData .................................................. */
	public long obviousFromChar(char c){
		if (c == symbols[0])
			return DNAState.A;
		if (c == symbols[1])
			return DNAState.C;
		if (c == symbols[2])
			return DNAState.G;
		if (c == symbols[3])
			return DNAState.T;
		switch (c) {
		case 'A': 
			return DNAState.A;
		case 'C': 
			return DNAState.C;
		case 'G': 
			return DNAState.G;
		case 'T': 
			return DNAState.T;
		case 'a': 
			return DNAState.a;
		case 'c': 
			return DNAState.c;
		case 'g': 
			return DNAState.g;
		case 't': 
			return DNAState.t;
		case '-': 
			return CategoricalState.inapplicable;
		default: 
			return 0L;
		}
	}
	/* .......................................... DNAData .................................................. */
	/* Returns state set from single state symbol � */
	public long fromChar(char state) {
		if (state == getInapplicableSymbol())
			return CategoricalState.inapplicable;
		else if (state == getUnassignedSymbol() || state == 'N')
			return CategoricalState.unassigned;
		else if (state == getMatchChar()) {
			return CategoricalState.unassigned;// note: this should't happen, as matchchar with no char #
		}
		else {
			long s = DNAState.fromCharStatic(state);
			return s;
		}
	}

	/* ..........................................DNAData..................................... */
	/** returns the maximum possible state */
	public int getMaxPossibleState() {
		return DNAState.getMaxPossibleStateStatic();
	}

	/* ..........................................DNAData..................................... */
	/** returns the name of a non-missing, applicable entry in a matrix cell; e.g. "base" for DNAData*/
	public String getNameOfCellEntry(int number){
		if (number==1)
			return "base";
		else
			return "bases";
	}


}

