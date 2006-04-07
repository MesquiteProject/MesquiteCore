package mesquite.molec.lib;

import mesquite.categ.lib.*;
import mesquite.molec.lib.*;

public class GenCodeVertMito extends GeneticCode {

	public void setCode() {
		setStandardCode();
		setCode(A,G,A, ProteinData.TER);
		setCode(A,G,G, ProteinData.TER);
		setCode(A,U,A, ProteinData.MET);
		setCode(U,G,A, ProteinData.TRP);
	}

	public String getName() {
		return "The Vertebrate Mitochondrial Genetic Code";
	}

	public static String getShortName (){
		return "Vertebrate Mitochondrial";
	}

	public String getNEXUSName(){
		return "mtdna.mam";
	}

	public int getNCBITranslationTableNumber(){
		return 2;
	}

}
