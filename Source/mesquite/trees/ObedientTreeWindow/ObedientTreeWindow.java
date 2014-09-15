/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.ObedientTreeWindow;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.trees.lib.*;

/* ======================================================================== */
//see mesquite.lib.duties.TreeInferer for example of use
public class ObedientTreeWindow extends SimpleTreeWindowMaker  {
	OTreeWindow tw;
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	protected SimpleTreeWindow makeTreeWindow(SimpleTreeWindowMaker stwm, DrawTreeCoordinator dtwc){
		tw= new OTreeWindow( this, treeDrawCoordTask);
		return tw;
	}
	protected String getMenuName(){
		return "Tree";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Simple Tree Window";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Displays a single tree." ;
	}

	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Ladderizes the clade", "[branch number]", commandName, "ladderize")) {
			Parser parser = new Parser();
			String s = parser.getFirstToken(arguments);
			int branchFound= MesquiteInteger.fromString(s);
			Tree tree = tw.getTree();
			MesquiteTree mTree = null;
			if (tree instanceof MesquiteTree) 
				mTree = (MesquiteTree)tree;
			else
				return null;
			if (s.equalsIgnoreCase("root"))
				branchFound = mTree.getRoot();
			else
				branchFound= MesquiteInteger.fromString(s);
			if (branchFound >0) {
				boolean direction = true;
				if (arguments.indexOf("option")>=0)
					direction = false;
				if (mTree.standardize(branchFound, direction, true)){
						;
				}
			}
		}		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

}

/* ======================================================================== */
class OTreeWindow extends SimpleTreeWindow  {
	public OTreeWindow ( ObedientTreeWindow ownerModule, DrawTreeCoordinator treeDrawCoordTask){
		super(ownerModule, treeDrawCoordTask); //infobar
	}

	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree blocks, etc.)
	they can reset their titles properly*/
	public void resetTitle(){
		setTitle("Tree"); //TODO: what tree?
	}

}


