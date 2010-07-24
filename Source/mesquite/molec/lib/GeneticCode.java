package mesquite.molec.lib;
/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison. 
Version 2.73, July 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/

import mesquite.lib.*;
import mesquite.categ.lib.*;


/* ======================================================================== */
/**Provides a genetic code*/
public abstract class GeneticCode implements Listable {
	protected static int A=0;
	protected static int C=1;
	protected static int G=2;
	protected static int T=3;
	protected static int U=3;
	public static final int CUSTOM_CODE=-1;
	
	protected int[][][] code = new int[4][4][4];
	
	public GeneticCode() {
		setStandardCode();
		setCode();
	}
	
	/** This method sets the values in the translation table from triplets to amino acid.  
	 * Use the setCode(int,int,int,int) method to set each element.  To initially set the 
	 * translation table to match that of the standard code, use setStandardCode() */
	public abstract void setCode();

	/** Returns the NCBI translation table number for the code. Return -1 if not in GenBank.*/
	public abstract int getNCBITranslationTableNumber();

	public void setCode(int c1, int c2, int c3, int aminoAcid) {
		code[c1][c2][c3] = aminoAcid;
	}
	/** Returns the int value of the amino acid that is specified by the codon c1-c2-c3 */
	public int getCode(int c1, int c2, int c3) {
		return code[c1][c2][c3];
	}

	/*.................................................................................................................*/
  	/** Returns true if the GeneticCode objects are the same */
 		public boolean equals (GeneticCode otherGeneticCode) {
 			if (otherGeneticCode == null)
 				return false;
 	 		if (getNCBITranslationTableNumber() == CUSTOM_CODE ||  otherGeneticCode.getNCBITranslationTableNumber() == CUSTOM_CODE) {
 	 			for (int c1=0; c1<4; c1++) 
 	 				for (int c2=0; c2<4; c2++) 
 	 					for (int c3=0; c3<4; c3++)
 	 						if (getCode(c1,c2,c3) != otherGeneticCode.getCode(c1,c2,c3)) 
 	 							return false;
	 			return true;
 	 		}
 	 		else
 	 			return (getNCBITranslationTableNumber() == otherGeneticCode.getNCBITranslationTableNumber());
		}
	/*.................................................................................................................*/
  	/** Returns the maximum amino acid int value in the matrix */
 		public int getMaxAA () {
 	 		int value = 0;
 	 		for (int c1=0; c1<4; c1++) 
 	  			for (int c2=0; c2<4; c2++) 
 	  				for (int c3=0; c3<4; c3++)
 	  					value =MesquiteInteger.maximum(value, code[c1][c2][c3]);
 	 		return value;
		}
 		/*.................................................................................................................*/
 		public int getDegeneracy (int aa) {
 			int count = 0;
 			for (int c1=0; c1<4; c1++) 
 				for (int c2=0; c2<4; c2++) 
 					for (int c3=0; c3<4; c3++) {
 						if (code[c1][c2][c3]==aa) {  //we've found the first aa
 							count++;
 						}
 					}
 			return count;
 		}
	/*.................................................................................................................*/
	public boolean[] getNearAAs (int aa) {
		int numAAs = getMaxAA();
		boolean[] nearAA = new boolean[numAAs];
		for (int c1=0; c1<4; c1++) 
			for (int c2=0; c2<4; c2++) 
				for (int c3=0; c3<4; c3++) {
					if (code[c1][c2][c3]==aa) {  //we've found the first aa
						for (int mut=0; mut<4; mut++) {
							if (mut!=c1 && code[mut][c2][c3] >=0 && code[mut][c2][c3]<numAAs) 
								nearAA[code[mut][c2][c3]] = true;
							if (mut!=c2 && code[c1][mut][c3] >=0 && code[c1][mut][c3]<numAAs) 
								nearAA[code[c1][mut][c3]] = true;
							if (mut!=c3 && code[c1][c2][mut] >=0 && code[c1][c2][mut]<numAAs) 
								nearAA[code[c1][c2][mut]] = true;
						}
					}
				}
		return nearAA;
		
	}

	/*.................................................................................................................*/
	public boolean[] getTwoAwayAAs (int aa) {
		boolean[] nearAA = getNearAAs(aa);

 		int numAAs = nearAA.length;
		boolean[] twoAwayAA = getNearAAs(aa);

		for (int j=0; j<numAAs; j++)
			if (nearAA[j]){  // this is an aa that is 1 step away from aa1
				boolean[] twoAway = getNearAAs(j);  //these are the aa's that are two steps away
				for (int i=0; i<numAAs; i++)
					if (twoAway[i])
						twoAwayAA[i]=true;
			}
		return twoAwayAA;
	}

	/*.................................................................................................................*/
  	/** Returns the  minimum number of replacement changes between two amino acids.
	 * If the minimum number of replacement changes is >3, MesquiteInteger.unassigned is returned. */
	  	public int getMinReplacementChanges(int aa1, int aa2){
  		if (aa1==aa2)
  			return 0;
 		boolean[] nearAA1 = getNearAAs(aa1);
		boolean[] nearAA2 = getNearAAs(aa2);

 		int numAAs = nearAA1.length;
		for (int aa=0; aa<numAAs; aa++)
			if ((nearAA1[aa] && (aa==aa2)) || (nearAA2[aa] && (aa==aa1)))  // there is an amino acid that is just one step away from one of them
				return 1;

		for (int aa=0; aa<numAAs; aa++)
			if (nearAA1[aa] && nearAA2[aa])  // there is an amino acid that is just one step away from each of them
				return 2;

		boolean[] twoAwayAA1 = getTwoAwayAAs(aa1);
		boolean[] twoAwayAA2 = getTwoAwayAAs(aa2);

		for (int aa=0; aa<numAAs; aa++)
			if (nearAA1[aa] && twoAwayAA2[aa])  
				return 3;

		for (int aa=0; aa<numAAs; aa++)
			if (nearAA2[aa] && twoAwayAA1[aa])  
				return 3;
		
		return MesquiteInteger.unassigned;
   	}

	/*.................................................................................................................*/
  	/** Returns a 2-D array containing the  minimum number of replacement changes between amino acids. 
  	 * If the minimum number of replacement changes is >3, MesquiteInteger.unassigned is entered. */
 		public int[][] getMinReplacementMatrix () {
 			int numAAs = getMaxAA();
 			int[][] replacementMatrix = new int [numAAs][numAAs];
 			for (int i=0; i<numAAs; i++) {
				for (int j=0; j<numAAs; j++)
					replacementMatrix[i][j] = getMinReplacementChanges(i,j);
			}
 			return replacementMatrix;
 		}

		
		
  	public void setStandardCode() {
			for (int j = 0; j<=3; j++) {			
				setCode(C, C, j, ProteinData.PRO);
				setCode(C, G, j, ProteinData.ARG);
				setCode(G, C, j, ProteinData.ALA);
				setCode(G, G, j, ProteinData.GLY);
				setCode(G, T, j, ProteinData.VAL);
				setCode(T, C, j, ProteinData.SER);
				setCode(C, T, j, ProteinData.LEU);
				setCode(A, C, j, ProteinData.THR);
		}
		setCode(A, A, T, ProteinData.ASN);
		setCode(A, A, C, ProteinData.ASN);
		setCode(A, A, A, ProteinData.LYS);
		setCode(A, A, G, ProteinData.LYS);

		setCode(C, A, T, ProteinData.HIS);
		setCode(C, A, C, ProteinData.HIS);
		setCode(C, A, G, ProteinData.GLN);
		setCode(C, A, A, ProteinData.GLN);

		setCode(G, A, T, ProteinData.ASP);
		setCode(G, A, C, ProteinData.ASP);
		setCode(G, A, A, ProteinData.GLU);
		setCode(G, A, G, ProteinData.GLU);

		setCode(T, T, T, ProteinData.PHE);
		setCode(T, T, C, ProteinData.PHE);
		setCode(T, T, A, ProteinData.LEU);
		setCode(T, T, G, ProteinData.LEU);

		setCode(T, G, T, ProteinData.CYS);
		setCode(T, G, C, ProteinData.CYS);
		setCode(T, G, G, ProteinData.TRP);
		setCode(T, G, A, ProteinData.TER);


		setCode(T, A, T, ProteinData.TYR);
		setCode(T, A, C, ProteinData.TYR);
		setCode(T, A, A, ProteinData.TER);
		setCode(T, A, G, ProteinData.TER);

// Davidv4.1: check this section as it is different from what I had: this warning is in 3.08 code
		setCode(A, T, C, ProteinData.ILEU);
		setCode(A, T, G, ProteinData.MET);
		setCode(A, T, T, ProteinData.ILEU);
		setCode(A, T, A, ProteinData.ILEU);

		setCode(A, G, T, ProteinData.SER);
		setCode(A, G, C, ProteinData.SER);
		setCode(A, G, A, ProteinData.ARG);
		setCode(A, G, G, ProteinData.ARG);
		}
	int reorderBase(int i){
		switch (i) {
			case 0: return 3;
			case 1: return 1;
			case 2: return 0;
			case 3: return 2;
			default: return i;
		}
	}
	public String toNCBIString(){
		StringBuffer sb = new StringBuffer();
		sb.append("\n" + getName() + "\n");
		sb.append("  AAs  = ");
		for (int i = 0; i<=3; i++) {
			for (int j = 0; j<=3; j++) {
				for (int k = 0; k<=3; k++) {
					int state = getCode(reorderBase(i),reorderBase(j),reorderBase(k));
					sb.append(ProteinData.getAASymbol(state));
				}
			}
		}
		sb.append("\nBase1  = ");
		for (int i = 0; i<=3; i++) {
			for (int j = 0; j<=3; j++) {
				for (int k = 0; k<=3; k++) {
					int state = reorderBase(i);
					sb.append(DNAData.getDefaultStateSymbol(state));
				}
			}
		}
		sb.append("\nBase2  = ");
		for (int i = 0; i<=3; i++) {
			for (int j = 0; j<=3; j++) {
				for (int k = 0; k<=3; k++) {
					int state = reorderBase(j);
					sb.append(DNAData.getDefaultStateSymbol(state));
				}
			}
		}
		sb.append("\nBase3  = ");
		for (int i = 0; i<=3; i++) {
			for (int j = 0; j<=3; j++) {
				for (int k = 0; k<=3; k++) {
					int state = reorderBase(k);
					sb.append(DNAData.getDefaultStateSymbol(state));
				}
			}
		}
		sb.append("\n");
		return sb.toString();
	}
	
	public String getNCBIURL(){
		return "http://www.ncbi.nlm.nih.gov/Taxonomy/Utils/wprintgc.cgi#SG" + getNCBITranslationTableNumber();
	}
	public String getURLString(){
		return getNCBIURL();
	}
	public void showNCBIPage(){
		String s = getNCBIURL();
		if (!StringUtil.blank(s))
			MesquiteModule.showWebPage(s, false, false);
	}
	public abstract String getName() ;
	public static String getShortName(){
		return "Genetic Code";
	}
	public abstract String getNEXUSName();


}



