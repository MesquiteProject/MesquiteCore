package mesquite.molec.lib;

import mesquite.categ.lib.*;
import mesquite.molec.lib.*;

public class GenCodeYeastMito extends GeneticCode {

	public void setCode() {
		setStandardCode();
		setCode(A,U,A, ProteinData.MET);
		setCode(C,U,A, ProteinData.THR);
		setCode(C,U,C, ProteinData.THR);
		setCode(C,U,G, ProteinData.THR);
		setCode(C,U,U, ProteinData.THR);
		setCode(U,G,A, ProteinData.TRP);

		setCode(C,G,A, ProteinData.ABSENT);
		setCode(C,G,C, ProteinData.ABSENT);
	}

	public String getName() {
		return "The Yeast Mitochondrial Genetic Code";
	}

	public static String getShortName (){
		return "Yeast Mitochondrial";
	}

	public String getNEXUSName(){
		return "mtdna.yeast";
	}

	public int getNCBITranslationTableNumber(){
		return 3;
	}

}
