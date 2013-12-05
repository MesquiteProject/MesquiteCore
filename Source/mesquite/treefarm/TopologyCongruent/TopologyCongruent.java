/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.TopologyCongruent;

/*New October 7, '08. oliver
 * Modified 16 October '08  to use the built-in Tree.equalsTopology method - DRM */
import mesquite.lib.*;
import mesquite.lib.duties.*;

public class TopologyCongruent extends BooleanForTree {
	OneTreeSource constraintTreeSource;
	Tree constraintTree;

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		constraintTreeSource = (OneTreeSource)hireEmployee(OneTreeSource.class, "One Tree Source");
		if(constraintTreeSource==null){
			return sorry(getName() + " couldn't start because no constraint tree was obtained.");
		}
		return true;
	}
	/*..............................................................................*/
	public void calculateBoolean(Tree tree, MesquiteBoolean result,	MesquiteString resultString) {
		if(tree==null || result==null || constraintTreeSource == null){
			return;
		}
		constraintTree = constraintTreeSource.getTree(tree.getTaxa()).cloneTree();
		if(constraintTree==null || constraintTree.getTaxa()!=tree.getTaxa())
			return;
		MesquiteBoolean isConsistent = new MesquiteBoolean(true);
		isConsistent.setValue(tree.equalsTopology(constraintTree, false));

		result.setValue(isConsistent.getValue());
		if (resultString!=null)
			if (isConsistent.getValue())
				resultString.setValue("Tree congruent");
			else
				resultString.setValue("Tree incongruent");
	}
	/*..............................................................................*/
	public String getName() {
		return "Tree Congruent with Constraint Tree Topology";
	}
	/*..............................................................................*/
 	/** returns an explanation of what the module does.*/
	public String getExplanation(){
		return "Determines if tree matches topology of a given constraint tree.  This module does not handle backbone constraints; all trees must have the same taxa present.  For backbone constraint trees, where the constraint tree need not contain all taxa, use the 'Tree Congruent with Backbone Constraint Tree Topology' module.";
	}
	/*........................................................*/
    public int getVersionOfFirstRelease(){
        return 260;
    }
    /*........................................................*/
    public boolean isPrerelease(){
    	return false;
    }
}

