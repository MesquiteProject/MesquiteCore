package mesquite.molec.lib;

import mesquite.categ.lib.*;
import mesquite.molec.lib.*;

public class GenCodeBacteria extends GeneticCode {

	public void setCode() {
		setStandardCode();
	}

	public String getName (){
		return "The Bacterial and Plant Plastid Code";
	}

	public static String getShortName (){
		return "Bacteria and Plant Plastids";
	}

	public String getNEXUSName(){
		return "bacterial";
	}

	public int getNCBITranslationTableNumber(){
		return 11;
	}

}
