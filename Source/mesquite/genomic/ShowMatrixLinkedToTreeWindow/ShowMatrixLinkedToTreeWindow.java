/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.genomic.ShowMatrixLinkedToTreeWindow;

import java.util.*;

import java.awt.*;

import mesquite.charMatrices.BasicDataWindowCoord.BasicDataWindowCoord;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDisplayActive;
import mesquite.lib.tree.TreeDisplayExtra;
import mesquite.lib.tree.TreeDisplayHolder;
import mesquite.lib.tree.TreeTool;
import mesquite.lib.ui.ColorTheme;
import mesquite.lib.ui.Legend;
import mesquite.lib.ui.MQPanel;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lib.ui.MessagePanel;

/* ======================================================================== */
public class ShowMatrixLinkedToTreeWindow extends TreeWindowAssistantN  {

	/*.................................................................................................................*/
	DataWindowMaker matrixEditorTask = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}


	public void employeeQuit(MesquiteModule m){
		//	if (m==treeDrawCoordTask)
		iQuit();
	}

	public boolean isSubstantive(){
		return false;
	}

	String treeName = null;
	boolean warned = false;
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		treeName = tree.getName();
		CharacterData d = null;
		MesquiteProject project = getProject();
		if (tree instanceof Attachable){
			Object obj = ((Attachable)tree).getAttachment("fromMatrix", MesquiteString.class);
			if (obj != null)
				d = project.getCharacterMatrixByReference(null, tree.getTaxa(), null, ((MesquiteString)obj).getValue());
			Debugg.println("@ from attachment " + d);
		}

		if (d == null)
			d = project.getCharacterMatrixByReference(null, tree.getTaxa(), null, treeName);
		if (d == null)
			d = project.getCharacterMatrixByReference(null, tree.getTaxa(), null, StringUtil.getAllButLastItem(treeName, "."));
		if (d == null)
			d = project.getCharacterMatrixByReference(null, tree.getTaxa(), null, StringUtil.getAllButLastItem(treeName, "#"));
		if (d != null){
			if (matrixEditorTask ==null){
				BasicDataWindowCoord coordTask = (BasicDataWindowCoord)findNearestColleagueWithDuty(BasicDataWindowCoord.class);
				matrixEditorTask = (DataWindowMaker)coordTask.doCommand("showDataWindow", StringUtil.tokenize(project.getCharMatrixReferenceExternal(d)), CommandChecker.defaultChecker);
				MesquiteWindow matrixWindow = matrixEditorTask.getModuleWindow();
				matrixWindow.setPopAsTile(true);
				matrixWindow.popOut(true);
			}
			else {
				matrixEditorTask = (DataWindowMaker)matrixEditorTask.doCommand("showMatrix", StringUtil.tokenize(project.getCharMatrixReferenceExternal(d)), CommandChecker.defaultChecker);
			}
		}
		else if (!warned){
			discreetAlert("No matrices were found linked to the current tree");
			warned = true;
		}

	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {

		Snapshot sn = new Snapshot();

		return sn;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Returns module coordinating tree drawing", null, commandName, "getTreeDrawCoordinator")) {
			//	return treeDrawCoordTask;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Show Matrix Linked to Tree";
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Displays character matrix linked to the current tree, e.g. with the same name." ;
	}


}



