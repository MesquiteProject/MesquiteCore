package mesquite.molec.lib;

import mesquite.categ.lib.*;
import mesquite.molec.lib.*;

public class GenCodeCiliate extends GeneticCode {

	public void setCode() {
		setStandardCode();
		setCode(U,A,A, ProteinData.GLN);
		setCode(U,A,G, ProteinData.GLN);
	}

	public String getName (){
		return "The Ciliate, Dasycladacean and Hexamita Nuclear Code";
	}

	public static String getShortName (){
		return "Ciliate Nuclear";
	}

	public String getNEXUSName(){
		return "nuc.ciliate";
	}

	public int getNCBITranslationTableNumber(){
		return 6;
	}

}
