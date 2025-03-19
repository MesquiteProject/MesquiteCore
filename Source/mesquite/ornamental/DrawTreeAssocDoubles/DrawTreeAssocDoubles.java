/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.ornamental.DrawTreeAssocDoubles;
/*~~  */

import java.util.*;

import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDisplayExtra;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.MesquiteCheckMenuItem;
import mesquite.lib.ui.MesquiteMenuItem;
import mesquite.lib.ui.MesquiteMenuSpec;
import mesquite.lib.ui.MesquitePopup;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.lib.ui.StringInABox;
import mesquite.trees.NodePropertyDisplayControl.NodePropertyDisplayControl;

/* ======================================================================== */
/* No longer used, subsumed under NodePropertyDisplayControl, but kept here to forward old scripts thereto */
public class DrawTreeAssocDoubles extends TreeDisplayAssistantDI {
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
		return 250;  
	}
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		return null;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Node or Branch-Associated Values";
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		return null;
	
	}
	
	/*.................................................................................................................*/
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
		else if (checker.compare(this.getClass(), "Sets whether to write the values as percentage", "[on or off]", commandName, "writeAsPercentage")) {//forwarded
			String token = parser.getFirstToken(arguments);
			boolean B = true;
			if (token.equalsIgnoreCase("off") || token.equalsIgnoreCase("false"))
				B = false;
			MesquiteModule mb = findNearestColleagueWithDuty(NodePropertyDisplayControl.class);
			if (mb != null)
				mb.doCommand("setBooleansAllDoubles",  " x x x x x " + B + " x ", checker);
	}
		else if (checker.compare(this.getClass(), "Sets whether to write the values with horizontally", "[on or off]", commandName, "toggleHorizontal")) { //forwarded
			String token = parser.getFirstToken(arguments);
			boolean B = true;
			if (token.equalsIgnoreCase("off") || token.equalsIgnoreCase("false"))
				B = false;
			MesquiteModule mb = findNearestColleagueWithDuty(NodePropertyDisplayControl.class);
			if (mb != null)
				mb.doCommand("setBooleansAllDoubles",  " x x x x x x " + !B + " ", checker);
		}
		else if (checker.compare(this.getClass(), "Sets whether to write the values white edges", "[on or off]", commandName, "toggleWhiteEdges")) {//forwarded
			String token = parser.getFirstToken(arguments);
			boolean B = true;
			if (token.equalsIgnoreCase("off") || token.equalsIgnoreCase("false"))
				B = false;
			MesquiteModule mb = findNearestColleagueWithDuty(NodePropertyDisplayControl.class);
			if (mb != null)
				mb.doCommand("setBooleansAllDoubles",  " x x " + B + " x x x x ", checker);
		}
		else if (checker.compare(this.getClass(), "Sets whether to show the values on the terminal branches", "[on or off]", commandName, "toggleShowOnTerminals")) {//forwarded
			String token = parser.getFirstToken(arguments);
			boolean B = true;
			if (token.equalsIgnoreCase("off") || token.equalsIgnoreCase("false"))
				B = false;
			MesquiteModule mb = findNearestColleagueWithDuty(NodePropertyDisplayControl.class);
			if (mb != null)
				mb.doCommand("setBooleansAllDoubles",  " x x x " + B + " x x x ", checker);
		}
		else if (checker.compare(this.getClass(), "Sets whether to write the values centrally over the branches", "[on or off]", commandName, "toggleCentred")) {//forwarded
			String token = parser.getFirstToken(arguments);
			boolean B = true;
			if (token.equalsIgnoreCase("off") || token.equalsIgnoreCase("false"))
				B = false;
			MesquiteModule mb = findNearestColleagueWithDuty(NodePropertyDisplayControl.class);
			if (mb != null)
				mb.doCommand("setBooleansAllDoubles",  " x " + B + " x x x x x ", checker);
			
		}
	//" fontSize xOffset yOffset digits color thresholdValue "
		else if (checker.compare(this.getClass(), "Sets how many digits are shown", "[number of digits]", commandName, "setDigits")) {//forwarded
			int newWidth= MesquiteInteger.fromFirstToken(arguments, pos);
			if (newWidth>=0 && newWidth<24 ) {
				MesquiteModule mb = findNearestColleagueWithDuty(NodePropertyDisplayControl.class);
				if (mb != null)
					mb.doCommand("setNumbersAllDoubles",  " x x x  " + newWidth + " x x  ", checker);
			}
		}
		else if (checker.compare(this.getClass(), "Sets threshold value — values have to be above this to be shown", "[value]", commandName, "setThreshold")) {//forwarded
			parser.setString(arguments);
			double newThreshold = MesquiteDouble.fromString(parser);
			if (MesquiteDouble.isCombinable(newThreshold)) {
				MesquiteModule mb = findNearestColleagueWithDuty(NodePropertyDisplayControl.class);
				if (mb != null)
					mb.doCommand("setNumbersAllDoubles",  " x x x x x " + newThreshold, checker);
			}
		}
		else if (checker.compare(this.getClass(), "Set's to David's style", "", commandName, "setCorvallisStyle")) {//forwarded
			String token = parser.getFirstToken(arguments);
			boolean B = true;
			if (token.equalsIgnoreCase("off") || token.equalsIgnoreCase("false"))
				B = false;
			MesquiteModule mb = findNearestColleagueWithDuty(NodePropertyDisplayControl.class);
			if (mb != null){
				mb.doCommand("setBooleansAllDoubles",  " false false false false false true false ", checker);
				mb.doCommand("setNumbersAllDoubles",  " x -2 9  0 x ?  ", checker);
			}
		}
		else if (checker.compare(this.getClass(), "Sets offset of label from nodes", "[offsetX] [offsetY]", commandName, "setOffset")) {//forwarded
			int newX= MesquiteInteger.fromFirstToken(arguments, pos);
			int newY= MesquiteInteger.fromString(arguments, pos);
			if (MesquiteInteger.isCombinable(newX) && newX>-200 && newX <200 && newY>-200 && newY <200) {
				MesquiteModule mb = findNearestColleagueWithDuty(NodePropertyDisplayControl.class);
				if (mb != null)
					mb.doCommand("setNumbersAllDoubles",  " x " + newX + " " + newY + " x x x x  ", checker);
			}
		}
		else if (checker.compare(this.getClass(), "Sets font size", "[font size]", commandName, "setFontSize")) {//forwarded
			int newWidth= MesquiteInteger.fromFirstToken(arguments, pos);
			if (newWidth>1 && newWidth<96 ) {
				MesquiteModule mb = findNearestColleagueWithDuty(NodePropertyDisplayControl.class);
				if (mb != null)
					mb.doCommand("setNumbersAllDoubles",  " " + newWidth + " x x x x x x  ", checker);
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether to show a node or branch associated value", "[on or off]", commandName, "toggleShow")) {//forwarded
			String name = parser.getFirstToken(arguments);
			String token = parser.getFirstToken(arguments);
			boolean B = true;
			if (token.equalsIgnoreCase("off") || token.equalsIgnoreCase("false"))
				B = false;
			if (B && on.getValue()){
				MesquiteModule mb = findNearestColleagueWithDuty(NodePropertyDisplayControl.class);
			if (mb != null){
				String toggle = "true";
				if (name.equalsIgnoreCase("consensusFrequency") ||name.equalsIgnoreCase("posteriorProbability") ||name.equalsIgnoreCase("bootstrapFrequency"))  //because old system used to treat thses as starting at true!
					toggle = "false";
				mb.doCommand("showAssociate",  StringUtil.tokenize(name) + " " + Associable.DOUBLES + " " + toggle, checker);
				mb.doCommand("setBooleans",  StringUtil.tokenize(name) + " " + Associable.DOUBLES + " false x x x false x x ", checker); //defaults
				mb.doCommand("setNumbers",  StringUtil.tokenize(name) + " " + Associable.DOUBLES + " x x x x x ? ", checker); //defaults
		}
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Shows values attached to nodes on the tree." ;
	}
	public boolean isSubstantive(){
		return false;
	}   	 

}



