package mesquite.molec.lib;

import mesquite.categ.lib.*;
import mesquite.molec.lib.*;

public class GenCodeEuplotid extends GeneticCode {

	public void setCode() {
		setStandardCode();
		setCode(U,G,A, ProteinData.CYS);
	}

	public String getName (){
		return "The Euplotid Nuclear Code";
	}

	public static String getShortName (){
		return "Euplotid Nuclear";
	}

	public String getNEXUSName(){
		return "nuc.euplotid";
	}

	public int getNCBITranslationTableNumber(){
		return 10;
	}

}
