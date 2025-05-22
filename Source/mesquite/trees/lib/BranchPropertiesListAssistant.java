package mesquite.trees.lib;

import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.DisplayableBranchProperty;
import mesquite.lists.lib.ListAssistant;
import mesquite.trees.BranchPropertiesList.BranchPropertiesList;
import mesquite.trees.BranchPropertyDisplayControl.BranchPropertyDisplayControl;

/* ======================================================================== */
public abstract class BranchPropertiesListAssistant extends ListAssistant  {
	//It's a bit risky (and unorthodox) sto store direct references to some of the other moduels, but too bad
	BranchPropertyDisplayControl displayModule;
	BranchPropertiesList listModule;

   	 public Class getDutyClass() {
   	 	return BranchPropertiesListAssistant.class;
   	 }
 	public String getDutyName() {
 		return "Node associates list assistant";
   	 }
 	public abstract void setTree(MesquiteTree tree);
	public void cursorTouchBranch(MesquiteTree tree, int N){
	}
	public void cursorEnterBranch(MesquiteTree tree, int N){
	}
	public void cursorExitBranch(MesquiteTree tree, int N){
	}
	public void cursorMove(MesquiteTree tree){
	}
	
	/* This arrangement is quite unorthodox in Mesquite, where this list assistants ask of their mother (BranchPropertiesList) 
	 * but also of their aunt (BranchPropertyDisplayControl). Normally the latter is discouraged. The mother and the aunt
	 * should probably have been one module (managing and controlling display of tree associates) but it would have been quite big.
	 * */
	protected void controlAppearanceOnTree(){
		if (displayModule == null)
			displayModule = (BranchPropertyDisplayControl)findNearestColleagueWithDuty(BranchPropertyDisplayControl.class);
		if (displayModule != null)
			 displayModule.queryDialog();
	}
	protected boolean isShowingOnTree(DisplayableBranchProperty property){
		if (displayModule == null)
			displayModule = (BranchPropertyDisplayControl)findNearestColleagueWithDuty(BranchPropertyDisplayControl.class);
		if (displayModule != null)
			return displayModule.isShowing(property);
		return false;
	}
	
	protected boolean associateInListIsBuiltIn(int row){
		if (listModule == null)
			listModule = (BranchPropertiesList)findEmployerWithDuty(BranchPropertiesList.class);
		if (listModule != null)
			return listModule.associateIsBuiltIn(row);
		return false;
	}
	protected DisplayableBranchProperty getPropertyAtRow(int row){
		if (listModule == null)
			listModule = (BranchPropertiesList)findEmployerWithDuty(BranchPropertiesList.class);
		if (listModule != null)
			return listModule.getPropertyAtRow(row);
		return null;
	}
	protected void pleaseShowHideOnTree(DisplayableBranchProperty[] properties, boolean show){
		if (displayModule == null)
			displayModule = (BranchPropertyDisplayControl)findNearestColleagueWithDuty(BranchPropertyDisplayControl.class);
		if (displayModule != null)
			displayModule.pleaseShowHide(properties, show);
	}
	protected void pleaseDeleteRow(int row, boolean notify){
		if (listModule == null)
			listModule = (BranchPropertiesList)findEmployerWithDuty(BranchPropertiesList.class);
		if (listModule != null)
			 listModule.internalDeleteRow(row, notify);
	}
}

