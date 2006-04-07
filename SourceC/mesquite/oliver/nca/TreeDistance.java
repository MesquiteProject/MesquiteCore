package mesquite.oliver.nca;

import mesquite.lib.*;

public abstract class TreeDistance extends MesquiteModule {

	public Class getDutyClass() {
		return TreeDistance.class;
	}
	public String getDutyName() {
		return "Distance Tree";
	}
	public String[] getDefaultModule(){
		return new String[] {"#MinimumSpanningTree"};
	}
	
	public abstract int getNumberOfTrees(Taxa taxa, CommandRecord commandRec);
	public abstract void initialize (Taxa taxa, CommandRecord commandRec);
	public abstract Tree getDistanceTree(Taxa taxa, Tree tree, int treeNumber, ObjectContainer extra, CommandRecord commandRec);

}