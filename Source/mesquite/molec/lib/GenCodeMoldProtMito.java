package mesquite.molec.lib;

import mesquite.categ.lib.*;

public class GenCodeMoldProtMito extends GeneticCode {

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
		return "The Mold, Protozoan, and Coelenterate Mitochondrial Code and the Mycoplasma/Spiroplasma Code";
	}

	public static String getShortName (){
		return "Mold Mitochondrial";
	}

	public String getNEXUSName(){
		return "mtdna.mold";
	}

	public int getNCBITranslationTableNumber(){
		return 4;
	}

}
