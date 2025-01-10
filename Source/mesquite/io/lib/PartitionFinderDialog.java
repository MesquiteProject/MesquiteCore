/* Mesquite (package mesquite.io).  Copyright 2000 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/

package mesquite.io.lib;

import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lib.ui.SingleLineTextField;
	
public class PartitionFinderDialog extends ExporterDialog {
//	int tnl = 200;
//	IntegerField f;
	boolean linked = true;
	boolean separateCodPos = true;
	Checkbox linkedBox;
	Checkbox writeExcludedBox;
	Checkbox separateCodePosBox;
	String models;
	SingleLineTextField modelsListField;
	Choice modelsChoice;
	Choice modelSelectionChoice;
	Choice schemeChoice;
	boolean isProtein;
	ExportPartitionFinder module;
	
	public PartitionFinderDialog (ExportPartitionFinder module, MesquiteWindow parent, String title, MesquiteInteger buttonPressed, boolean isProtein) {
		super(module, parent, title, buttonPressed);
//		this.tnl = module.taxonNameLength;
		this.linked = module.branchesLinked;
		this.isProtein = isProtein;
		this.module = module;
		
	}
			
	static final String LISTMODELS="<list>";
	/*.................................................................................................................*/
	public void completeAndShowDialog (boolean dataSelected, boolean taxaSelected) {
//		 f = addIntegerField ("Maximum length of taxon names", tnl, 4, 1, 40);
		 linkedBox = addCheckBox("branch lengths linked", linked);
		if (isProtein)
			modelsChoice = addPopUpMenu ("models", new String[] {"all_protein", LISTMODELS}, 0);
		else
			modelsChoice = addPopUpMenu ("models", new String[] {"all", "mrbayes", "beast", LISTMODELS}, 0);
		 modelsListField = addTextField ("model list: ", "",40);
		 modelSelectionChoice = addPopUpMenu ("model selection", new String[] {"AIC", "AICc", "BIC"}, 2);
		 schemeChoice = addPopUpMenu ("scheme", new String[] {"all", "greedy", "rcluster", "hcluster"}, 1);
		if (!isProtein)
			separateCodePosBox = addCheckBox("separate by codon positions", separateCodPos);
		writeExcludedBox = addCheckBox("write excluded characters", module.writeExcludedCharacters);
		super.completeAndShowDialog(dataSelected, taxaSelected);
	}
	public boolean getLinked(){
		return linkedBox.getState();
	}
	public boolean getSeparateCodPos(){
		if (isProtein)
			return false;
		return separateCodePosBox.getState();
	}
	public boolean getWriteExcluded(){
		return writeExcludedBox.getState();
	}
	public String getModels(){
		String modelsSelected = modelsChoice.getSelectedItem();
		if (StringUtil.notEmpty(modelsSelected)){
			if (modelsSelected.equalsIgnoreCase(LISTMODELS)) {
				String list = modelsListField.getText();
				if (StringUtil.notEmpty(list))
					return list;
			}
			else
				return modelsSelected;
		}
		if (isProtein)
			return "all_protein";
		else
			return "all";
	}
	public int getModelSelection(){
		return modelSelectionChoice.getSelectedIndex();
	}
	public int getScheme(){
		return schemeChoice.getSelectedIndex();
	}

}