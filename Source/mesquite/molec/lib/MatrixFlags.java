package mesquite.molec.lib;

import java.util.Vector;

import mesquite.lib.Bits;
import mesquite.lib.characters.CharacterData;

public class MatrixFlags {
	Bits characterFlags;
	Bits taxonFlags;
	Vector cellFlags;
	int numChars, numTaxa;

	public MatrixFlags(CharacterData data) {
		cellFlags = new Vector();
		reset(data);
	}
	public void reset(CharacterData data) {
		numChars = data.getNumChars();
		numTaxa = data.getNumTaxa();
		if (characterFlags == null)
			characterFlags = new Bits(numChars);
		else {
			if (characterFlags.getSize()< numChars)
				characterFlags.resetSize(numChars);
			characterFlags.clearAllBits();
		}
		if (taxonFlags == null)
			taxonFlags = new Bits(numTaxa);
		else {
			if (taxonFlags.getSize()< numTaxa)
				taxonFlags.resetSize(numTaxa);
			taxonFlags.clearAllBits();
		}
		cellFlags.removeAllElements();
	}
	public void setCharacterFlag(int ic, boolean value) {
		characterFlags.setBit(ic, value);
	}
	public boolean isCellFlaggedAnyWay(int ic, int it) {
		if (characterFlags.isBitOn(ic))
			return true;
		if (taxonFlags.isBitOn(it))
			return true;
		for (int i = 0; i<cellFlags.size(); i++) {
			int[] stretch = (int[])cellFlags.elementAt(i);
			if (stretch[0] == it && ic>= stretch[1] && ic<=stretch[2])
				return true;
		}
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
		cellFlags.addElement(new int[] {it, icStart, icEnd});
	}
	public Bits getCharacterFlags() {
		return characterFlags;
	}
	public Bits getTaxonFlags() {
		return taxonFlags;
	}
	public Vector getCellFlags() {
		return cellFlags;
	}
	public int getNumChars() {
		return numChars;
	}
	public int getNumTaxa() {
		return numTaxa;
	}
	public String toString() {
		String s = "Matrix Flags " + numChars + " (" + characterFlags.numBitsOn() + "on) X " + numTaxa + " (" + taxonFlags.numBitsOn() + "on) plus blocks " + cellFlags.size();
		return s;
	}

	public void copyFlags(MatrixFlags flags) {
		characterFlags.setBits(flags.getCharacterFlags());
		taxonFlags.setBits(flags.getTaxonFlags());
		cellFlags.removeAllElements();
		Vector other = flags.getCellFlags();
		for (int i = 0; i<other.size(); i++) {
			int[] stretch = (int[])other.elementAt(i);
			int[] nS = new int[] {stretch[0], stretch[1], stretch[2]};
			cellFlags.addElement(nS);
		}

	}
	public void orFlags(MatrixFlags flags) {
		characterFlags.orBits(flags.getCharacterFlags());
		taxonFlags.orBits(flags.getTaxonFlags());
		Vector other = flags.getCellFlags();
		for (int i = 0; i<other.size(); i++) {
			int[] stretch = (int[])other.elementAt(i);
			int[] nS = new int[] {stretch[0], stretch[1], stretch[2]};
			cellFlags.addElement(nS);
		}
	}
}
