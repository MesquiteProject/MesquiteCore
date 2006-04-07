package mesquite.molec.lib;

import mesquite.categ.lib.*;
import mesquite.molec.lib.*;

public class GenCodeEchino extends GeneticCode {

	public void setCode() {
		setStandardCode();
		setCode(A,A,A, ProteinData.ASN);
		setCode(A,G,A, ProteinData.SER);
		setCode(A,G,G, ProteinData.SER);
		setCode(U,G,A, ProteinData.TRP);
	}

	public String getName() {
		return "The Echinoderm and Flatworm Mitochondrial Code";
	}

	public static String getShortName (){
		return "Echinoderm Mitochondrial";
	}

	public String getNEXUSName(){
		return "mtdna.echino";
	}
	public int getNCBITranslationTableNumber(){
		return 9;
	}

}
