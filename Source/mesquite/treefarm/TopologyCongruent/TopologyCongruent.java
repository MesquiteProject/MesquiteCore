/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


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
 * Modified 16 October 2008  to use the built-in Tree.equalsTopology method - DRM 
 * January 2012: added non-exact match. */
import java.awt.*;

import mesquite.consensus.lib.StrictConsenser;
import mesquite.lib.*;
import mesquite.lib.duties.*;

public class TopologyCongruent extends BooleanForTree {
	OneTreeSource constraintTreeSource;
	Tree constraintTree;
	MesquiteBoolean exactMatch = new MesquiteBoolean(false);
	StrictConsenser strictConsenser = new StrictConsenser();

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		constraintTreeSource = (OneTreeSource)hireEmployee(OneTreeSource.class, "One Tree Source");
		if(constraintTreeSource==null){
			return sorry(getName() + " couldn't start because no comparison tree was obtained.");
		}
		loadPreferences();
		MesquiteMenuItemSpec exactMatchItem = addCheckMenuItem( null, "Topologies must be exactly equal", makeCommand("exactMatch",  this), exactMatch);

		if (!MesquiteThread.isScripting()) 
			if (!queryOptions()) 
				return false;
		return true;
	}
	/*.................................................................................................................*/
	public void processMorePreferences (String tag, String content) {
		if ("exactMatch".equalsIgnoreCase(tag))
			exactMatch.setFromTrueFalseString(content);
	}
	/*.................................................................................................................*/
	public String prepareMorePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "exactMatch", exactMatch);  
		return buffer.toString();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("exactMatch "+ exactMatch.toOffOnString()); 
		return temp;
	}
	/*.................................................................................................................*/
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), getName() + " Options",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		dialog.addLabel(getName() + " Options");

		Checkbox exactMatchCheck = dialog.addCheckBox("Topology must be exact match", exactMatch.getValue());

		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			exactMatch.setValue(exactMatchCheck.getState());
			storePreferences();

		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets whether or not exact matching should be used", "[on off]", commandName, "exactMatch")) {
			boolean oldValue = exactMatch.getValue();
			exactMatch.toggleValue(arguments);
			if (oldValue != exactMatch.getValue()) {
				parametersChanged();
			}
			return exactMatch;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
	}

	/*..............................................................................*/
	public void calculateBoolean(Tree tree, MesquiteBoolean result,	MesquiteString resultString) {
		if(tree==null || result==null || constraintTreeSource == null){
			return;
		}
		Tree sourceTree = constraintTreeSource.getTree(tree.getTaxa());
		if (sourceTree==null)
			return;
		constraintTree = sourceTree.cloneTree();
		if(constraintTree==null || constraintTree.getTaxa()!=tree.getTaxa())
			return;
		MesquiteBoolean isConsistent = new MesquiteBoolean(true);

		if (exactMatch.getValue()) {
			isConsistent.setValue(tree.equalsTopology(constraintTree, false));

			result.setValue(isConsistent.getValue());
			if (resultString!=null)
				if (isConsistent.getValue())
					resultString.setValue("Tree identical");
				else
					resultString.setValue("Tree different");
		} else {
			strictConsenser.reset(tree.getTaxa());
			strictConsenser.addTree(tree);
			strictConsenser.addTree(constraintTree);
			Tree strict = strictConsenser.getConsensus();
			isConsistent.setValue(strict.equalsTopology(constraintTree, false));

			result.setValue(isConsistent.getValue());
			if (resultString!=null)
				if (isConsistent.getValue())
					resultString.setValue("Tree consistent");
				else
					resultString.setValue("Tree inconsistent");
		}
	}
	/*..............................................................................*/
	public String getName() {
		return "Tree Congruent with Specified Tree Topology";
	}
	/*..............................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation(){
		return "Determines if tree matches topology of a specified tree.  All trees must have the same taxa present.";
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

