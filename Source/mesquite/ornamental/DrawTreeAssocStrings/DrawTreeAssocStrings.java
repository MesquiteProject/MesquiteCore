/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.ornamental.DrawTreeAssocStrings;
/*~~  */

import mesquite.lib.Associable;
import mesquite.lib.CommandChecker;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteModule;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.duties.TreeDisplayAssistantDI;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDisplayExtra;
import mesquite.trees.BranchPropertyDisplayControl.BranchPropertyDisplayControl;

/* ======================================================================== */
/* No longer used, subsumed under BranchPropertyDisplayControl, but kept here to forward old scripts thereto */
public class DrawTreeAssocStrings extends TreeDisplayAssistantDI {
	public boolean loadModule(){
		return true;
	}   	 
	MesquiteBoolean on;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		on = new MesquiteBoolean(true);  //ON is currently true always
	
		return true;
	} 
	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return 274;  
	}
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		return null;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Node or Branch-Associated Text";
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		return null;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets whether to show the node or branch associated values", "[on or off]", commandName, "setOn")) {  //on always except if scripted off
			if (StringUtil.blank(arguments))
				on.setValue(!on.getValue());
			else
				on.toggleValue(parser.getFirstToken(arguments));
		}
		//" showName centred whiteEdges showOnTerminals showIfUnassigned showPercentage vertical "
		else if (checker.compare(this.getClass(), "Sets whether to write the values with horizontally", "[on or off]", commandName, "toggleHorizontal")) { //forwarded
			String token = parser.getFirstToken(arguments);
			boolean B = true;
			if (token.equalsIgnoreCase("off") || token.equalsIgnoreCase("false"))
				B = false;
			MesquiteModule mb = findNearestColleagueWithDuty(BranchPropertyDisplayControl.class);
			if (mb != null)
				mb.doCommand("setBooleansAllStrings",  " false x x x x x " + !B + " ", checker);
		}
		else if (checker.compare(this.getClass(), "Sets whether to show the values on the terminal branches", "[on or off]", commandName, "toggleShowOnTerminals")) {//forwarded
			String token = parser.getFirstToken(arguments);
			boolean B = true;
			if (token.equalsIgnoreCase("off") || token.equalsIgnoreCase("false"))
				B = false;
			MesquiteModule mb = findNearestColleagueWithDuty(BranchPropertyDisplayControl.class);
			if (mb != null)
				mb.doCommand("setBooleansAllStrings",  " false x x " + B + " x x x ", checker);
		}
		else if (checker.compare(this.getClass(), "Sets whether to write the values centrally over the branches", "[on or off]", commandName, "toggleCentred")) {//forwarded
			String token = parser.getFirstToken(arguments);
			boolean B = true;
			if (token.equalsIgnoreCase("off") || token.equalsIgnoreCase("false"))
				B = false;
			MesquiteModule mb = findNearestColleagueWithDuty(BranchPropertyDisplayControl.class);
			if (mb != null)
				mb.doCommand("setBooleansAllStrings",  " false " + B + " x x x x x ", checker);
			
		}
		//" fontSize xOffset yOffset digits color thresholdValue "
		else if (checker.compare(this.getClass(), "Sets offset of label from nodes", "[offsetX] [offsetY]", commandName, "setOffset")) {//forwarded
			int newX= MesquiteInteger.fromFirstToken(arguments, pos);
			int newY= MesquiteInteger.fromString(arguments, pos);
			if (MesquiteInteger.isCombinable(newX) && newX>-200 && newX <200 && newY>-200 && newY <200) {
				MesquiteModule mb = findNearestColleagueWithDuty(BranchPropertyDisplayControl.class);
				if (mb != null)
					mb.doCommand("setNumbersAllStrings",  " x " + newX + " " + newY + " x x x x  ", checker);
			}
		}
		else if (checker.compare(this.getClass(), "Sets font size", "[font size]", commandName, "setFontSize")) {//forwarded
			int newWidth= MesquiteInteger.fromFirstToken(arguments, pos);
			if (newWidth>1 && newWidth<96 ) {
				MesquiteModule mb = findNearestColleagueWithDuty(BranchPropertyDisplayControl.class);
				if (mb != null)
					mb.doCommand("setNumbersAllStrings",  " " + newWidth + " x x x x x x  ", checker);
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether to show a node or branch associated value", "[on or off]", commandName, "toggleShow")) {//forwarded
			String name = parser.getFirstToken(arguments);
			String token = parser.getFirstToken(arguments);
			boolean B = true;
			if (token.equalsIgnoreCase("off") || token.equalsIgnoreCase("false"))
				B = false;
			if (B && on.getValue()){
				MesquiteModule mb = findNearestColleagueWithDuty(BranchPropertyDisplayControl.class);
			if (mb != null){
				mb.doCommand("showAssociate",  StringUtil.tokenize(name) + " " + Associable.STRINGS + " true", checker);
				mb.doCommand("setBooleans",  StringUtil.tokenize(name) + " " + Associable.STRINGS + " false x x x false x x ", checker); //defaults
				mb.doCommand("setNumbers",  StringUtil.tokenize(name) + " " + Associable.STRINGS + " x x x x x ? ", checker); //defaults
			}
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Shows text attached to nodes on the tree." ;
	}
	public boolean isSubstantive(){
		return false;
	}   	 

	
}



