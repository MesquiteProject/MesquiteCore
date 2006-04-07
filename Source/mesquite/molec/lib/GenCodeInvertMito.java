package mesquite.molec.lib;

import mesquite.categ.lib.*;
import mesquite.molec.lib.*;

public class GenCodeInvertMito extends GeneticCode {

	public void setCode() {
		setStandardCode();
		setCode(A,G,A, ProteinData.SER);
		setCode(A,G,G, ProteinData.SER);
		setCode(A,U,A, ProteinData.MET);
		setCode(U,G,A, ProteinData.TRP);
	}

	public String getName() {
		return "The Invertebrate Mitochondrial Genetic Code";
	}

	public static String getShortName (){
		return "Invertebrate Mitochondrial";
	}

	public String getNEXUSName(){
		return "mtdna.dros";
	}

	public int getNCBITranslationTableNumber(){
		return 5;
	}

}
