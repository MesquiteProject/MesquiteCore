package mesquite.lib.characters;

import java.util.Vector;

import mesquite.lib.Bits;

public class MatrixFlags {
	Bits characterFlags;
	Bits taxonFlags;
	boolean[][] cellFlags;
	int numChars, numTaxa;

	public MatrixFlags(CharacterData data) {
		reset(data);
	}
	public void reset(CharacterData data) {
		numChars = data.getNumChars();
		numTaxa = data.getNumTaxa();
		if (characterFlags == null)
			characterFlags = new Bits(numChars);
		else {
			if (characterFlags.getSize()!= numChars)
				characterFlags.resetSize(numChars);
			characterFlags.clearAllBits();
		}
		if (taxonFlags == null)
			taxonFlags = new Bits(numTaxa);
		else {
			if (taxonFlags.getSize()!= numTaxa)
				taxonFlags.resetSize(numTaxa);
			taxonFlags.clearAllBits();
		}
		if (cellFlags == null)
			cellFlags = new boolean[numChars][numTaxa];
		else {
			if (cellFlags.length != numChars || (cellFlags.length>0 && cellFlags[0].length != numTaxa))
				cellFlags = new boolean[numChars][numTaxa];
			else
				for (int ic = 0; ic<numChars; ic++)
					for (int it=0; it<numTaxa; it++)
						cellFlags[ic][it] = false;
		}
	}
	public void invert() {
		if (characterFlags == null)
			return;
		for (int ic = 0; ic<numChars; ic++)
			for (int it=0; it<numTaxa; it++)
				cellFlags[ic][it] = !cellFlags[ic][it];
		characterFlags.invertAllBits();
		taxonFlags.invertAllBits();

	}
	public void invertCharacters() {
		if (characterFlags == null)
			return;
		characterFlags.invertAllBits();

	}
	public void invertTaxa() {
		if (taxonFlags == null)
			return;
		taxonFlags.invertAllBits();

	}
	public void invertCells() {
		if (cellFlags == null)
			return;
		for (int ic = 0; ic<numChars; ic++)
			for (int it=0; it<numTaxa; it++)
				cellFlags[ic][it] = !cellFlags[ic][it];

	}
	public boolean anyFlagsSet(){
		if (characterFlags.anyBitsOn())
			return true;
		if (taxonFlags.anyBitsOn())
			return true;
		for (int ic = 0; ic<cellFlags.length; ic++)
			for (int it = 0; it<cellFlags[ic].length; it++)
				if (cellFlags[ic][it])
					return true;
		return false;
	}
	public void setCharacterFlag(int ic, boolean value) {
		characterFlags.setBit(ic, value);
	}
	public boolean isCellFlaggedAnyWay(int ic, int it) {
		if (characterFlags.isBitOn(ic))
			return true;
		if (taxonFlags.isBitOn(it))
			return true;
		if (cellFlags[ic][it])
			return true;
		return false;
	}
	public boolean isCharacterFlagOn(int ic) {
		return characterFlags.isBitOn(ic);
	}
	public void setTaxonFlag(int ic, boolean value) {
		taxonFlags.setBit(ic, value);
	}
	public boolean isTaxonFlagOn(int ic) {
		return taxonFlags.isBitOn(ic);
	}

	public void addCellFlag(int it, int icStart, int icEnd) {
		for (int ic = icStart; ic<=icEnd; ic++)
			cellFlags[ic][it] = true;
	}
	public Bits getCharacterFlags() {
		return characterFlags;
	}
	public Bits getTaxonFlags() {
		return taxonFlags;
	}
	public boolean[][] getCellFlags() {
		return cellFlags;
	}
	public int getNumChars() {
		return numChars;
	}
	public int getNumTaxa() {
		return numTaxa;
	}
	public String toString() {
		int countCells = 0;
		for (int ic = 0; ic<cellFlags.length; ic++)
			for (int it = 0; it<cellFlags[ic].length; it++)
				if (cellFlags[ic][it])
					countCells++;
		String s = "Matrix Flags: of " + numChars + " chars, " + characterFlags.numBitsOn() + " on; of " + numTaxa + ", " + taxonFlags.numBitsOn() + " on; cells on: " + countCells;
		return s;
	}

	public void copyFlags(MatrixFlags flags) {
		characterFlags.setBits(flags.getCharacterFlags());
		taxonFlags.setBits(flags.getTaxonFlags());
		boolean[][] other = flags.getCellFlags();
		for (int ic = 0; ic<numChars; ic++)
			for (int it=0; it<numTaxa; it++)
				cellFlags[ic][it] = other[ic][it];

	}
	public void orFlags(MatrixFlags flags) {
		characterFlags.orBits(flags.getCharacterFlags());
		taxonFlags.orBits(flags.getTaxonFlags());
		boolean[][] other = flags.getCellFlags();
		for (int ic = 0; ic<numChars; ic++)
			for (int it=0; it<numTaxa; it++)
				cellFlags[ic][it] |= other[ic][it];
	}
}
