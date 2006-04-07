package mesquite.chromaseq.lib;


import mesquite.lib.*;
import mesquite.lib.duties.*;
import java.awt.*;

public abstract class NameParserManager extends MesquiteInit {
	public ListableVector nameParsingRules;
	public Choice choice;

	public Class getDutyClass(){
		return NameParserManager.class;
	}
	
	public abstract ChromFileNameParsing chooseNameParsingRules(ChromFileNameParsing rule) ;

	
	public void setChoice (Choice choice) {
		this.choice = choice;
	}
	public Choice getChoice() {
		return choice;
	}
	public int getNumRules() {
		return nameParsingRules.getNumberOfParts();
	}
//	public abstract ExtensibleDialog getChooseTemplateDLOG(Taxa taxa, String title, ObjectContainer oC, MesquiteInteger buttonPressed, boolean includeMatrices);

}


