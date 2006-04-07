package mesquite.molec.lib;


import mesquite.molec.lib.*;

public class GenCodeStandard extends GeneticCode {

	public void setCode() {
		setStandardCode();
	}

	public String getName() {
		return "The standard (\"universal\") genetic code";
	}

	public static String getShortName (){
		return "Standard";
	}

	public String getNEXUSName(){
		return "universal";
	}

	public int getNCBITranslationTableNumber(){
		return 1;
	}

}
