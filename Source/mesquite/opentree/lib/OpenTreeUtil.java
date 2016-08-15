package mesquite.opentree.lib;

import mesquite.lib.*;

public class OpenTreeUtil {
	
	public static final NameReference nonExemplarRef = NameReference.getNameReference("Non-exemplarTaxon");
	
	/*.................................................................................................................*/
	public static void convertNodeValues(int node, AdjustableTree tree, NameReference nr) {
		double value = tree.getAssociatedDouble(nr,node);
		if (MesquiteDouble.isCombinable(value))
			tree.setBranchLength(node, value, false);
		else
			tree.setBranchLength(node, MesquiteDouble.unassigned, false);
		if (tree instanceof MesquiteTree)
			((MesquiteTree)tree).setAssociatedDouble(nr, node, MesquiteDouble.unassigned);
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) 
			convertNodeValues(d, tree, nr);
	}

	/*.................................................................................................................*/
	public static boolean convertNodeValuesToBranchLengths(MesquiteModule mb, AdjustableTree tree){
		if (tree == null)
			return false;
		ListableVector v = new ListableVector();
		int num = tree.getNumberAssociatedDoubles();
		if (num==1) {
			DoubleArray da = tree.getAssociatedDoubles(0);
			NameReference nr = NameReference.getNameReference(da.getName());
			convertNodeValues(tree.getRoot(), tree, nr);
			return true;
		} else if (num>1){
			boolean[] shown = new boolean[num]; //bigger than needed probably
			for (int i = 0; i< num; i++){
				DoubleArray da = tree.getAssociatedDoubles(i);
				if (da != null)
					v.addElement(new MesquiteString(da.getName(), ""), false);
			}
			Listable result = ListDialog.queryList(mb.containerOfModule(), "Choose attached value", "Choose attached value to transfer to branch lengths", null, v, 0);
			if (result != null){
				MesquiteString name = (MesquiteString)result;
				String sName = name.getName();
				NameReference nr = NameReference.getNameReference(sName);

				convertNodeValues(tree.getRoot(), tree, nr);

				return true;
			}
		}
		return false;

	}


}
