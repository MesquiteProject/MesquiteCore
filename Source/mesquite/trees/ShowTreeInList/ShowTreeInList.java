/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.ShowTreeInList;
/*~~  */

import mesquite.lists.lib.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class ShowTreeInList extends TreeListInit  {
	TreeVector trees;
	MesquiteTable table;
	TableTool tool;
	public String getName() {
		return "Show Tree";
	}
	public String getExplanation() {
		return "Provides tool in the List of Trees window to request that tree be shown in window.";
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 275;  
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		return temp;
	}
	TreeWindowMaker getTreeWindowModule(){
		mesquite.trees.BasicTreeWindowCoord.BasicTreeWindowCoord treeWindowCoord = (mesquite.trees.BasicTreeWindowCoord.BasicTreeWindowCoord)findNearestModuleWithDuty(mesquite.trees.BasicTreeWindowCoord.BasicTreeWindowCoord.class);
		MesquiteModule[] treeWindowMakers = treeWindowCoord.getImmediateEmployeesWithDuty(mesquite.trees.BasicTreeWindowMaker.BasicTreeWindowMaker.class);
		if (treeWindowMakers != null && treeWindowMakers.length>0){
			for (int i = 0; i<treeWindowMakers.length; i++){
				TreeWindowMaker mod = (TreeWindowMaker)treeWindowMakers[i];
				MesquiteModule source = mod.getTreeSource();
				if (source instanceof mesquite.trees.StoredTrees.StoredTrees){
					mesquite.trees.StoredTrees.StoredTrees ts = (mesquite.trees.StoredTrees.StoredTrees)source;
					if (ts.showing(trees)){
						return mod;
					}
				}
			}
		}
		
		TreeWindowMaker mod = treeWindowCoord.makeWindowShowingTrees(trees.getTaxa(), trees);
		return mod;
		
	}
	/*.................................................................................................................*/
	/** A request for the MesquiteModule to perform a command.  It is passed two strings, the name of the command and the arguments.
	This should be overridden by any module that wants to respond to a command.*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 
		if (checker.compare(MesquiteModule.class, null, null, commandName, "showTree")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int column= MesquiteInteger.fromString(arguments, io);
			int row= MesquiteInteger.fromString(arguments, io);

			mesquite.trees.BasicTreeWindowMaker.BasicTreeWindowMaker treeWindowModule = (mesquite.trees.BasicTreeWindowMaker.BasicTreeWindowMaker)getTreeWindowModule();
			
			treeWindowModule.goToTreeNumber(row);
			treeWindowModule.showMe();
			
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public void setTableAndTreeBlock(MesquiteTable table, TreeVector trees){
		this.table = table;
		this.trees = trees;
		if (containerOfModule() instanceof ListWindow && tool == null){
			String sortExplanation = "Shows tree in tree window. ";
			tool = new TableTool(this, "showTree", getPath(),"showTree.gif", 0,0,"Show tree", sortExplanation, MesquiteModule.makeCommand("showTree",  this) , null, null);
			tool.setWorksOnRowNames(true);
			((ListWindow)containerOfModule()).addTool(tool);
			//tool.setPopUpOwner(this);
		}
	}

}
