package mesquite.trees.lib;

import mesquite.lib.Debugg;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lists.lib.ListAssistant;

/* ======================================================================== */
public abstract class NodeAssociatesListAssistant extends ListAssistant  {

   	 public Class getDutyClass() {
   	 	return NodeAssociatesListAssistant.class;
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

}

