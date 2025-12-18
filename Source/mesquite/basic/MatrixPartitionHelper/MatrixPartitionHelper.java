/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.basic.MatrixPartitionHelper;
/*~~  */

import java.awt.Color;

import mesquite.categ.lib.DNAData;
import mesquite.charMatrices.ManageCharPartitions.ManageCharPartitions;
import mesquite.charMatrices.ManageMatrixPartitions.ManageMatrixPartitions;
import mesquite.lib.AssociableWithSpecs;
import mesquite.lib.CommandChecker;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteString;
import mesquite.lib.Notification;
import mesquite.lib.SelectionInformer;
import mesquite.lib.SpecsSet;
import mesquite.lib.SpecsSetVector;
import mesquite.lib.StringLister;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.MatricesGroup;
import mesquite.lib.characters.MatricesGroupVector;
import mesquite.lib.characters.MatrixPartition;
import mesquite.lib.characters.MatricesGroup;
import mesquite.lib.characters.MatricesGroupVector;
import mesquite.lib.characters.CodonPositionsSet;
import mesquite.lib.duties.CharactersSelectedUtility;
import mesquite.lib.duties.MatricesSelectedUtility;
import mesquite.lib.ui.ListDialog;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lists.lib.CharListPartitionUtil;
import mesquite.lists.lib.GroupDialog;
import mesquite.lists.lib.MatrixListPartitionUtil;

/* ======================================================================== */
public class MatrixPartitionHelper extends MatricesSelectedUtility {
	//"@MATRIXGROUP
	ListableVector datas;
	/*.................................................................................................................*/
	public String getName() {
		return "Matrix Partition Helper";
	}
	public String getExplanation() {
		return "Controls adjustments to matrix partitions (e.g., in List of Character Matrices, Groups column)." ;
	}
	/*.................................................................................................................*/
	MatricesGroupVector groups;
	SelectionInformer selectionInformer;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		groups = (MatricesGroupVector)getProject().getFileElement(MatricesGroupVector.class, 0);
		datas = getProject().getCharacterMatrices();
		return true;
	}

	int touchedMatrix = -1;
	public void matrixTouched(int it){  //an additional possibility for setting
		touchedMatrix = it;
	}
	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	private void setPartition(MatricesGroup group, String arguments){
		if (selectionInformer !=null && datas!=null) {
			boolean changed=false;
			String name = parser.getFirstToken(arguments);
			if (group == null && StringUtil.blank(name))
				return;
			MatrixPartition partition = (MatrixPartition) datas.getCurrentSpecsSet(MatrixPartition.class);
			if (partition==null){
				partition= new MatrixPartition("Partition", null, datas);
				partition.addToFile(datas.getFile(), getProject(), findElementManager(MatrixPartition.class));
				datas.setCurrentSpecsSet(partition, MatrixPartition.class);
			}
			if (group == null){
				MatricesGroupVector groups = (MatricesGroupVector)getProject().getFileElement(MatricesGroupVector.class, 0);
				Object obj = groups.getElement(name);
				group = (MatricesGroup)obj;
			}
			if (group != null) {
				if (partition != null) {
					for (int i=0; i<datas.size(); i++) {
						if (selectionInformer.isItemSelected(i, this)) {
							partition.setProperty(group, i);
							if (!changed)
								outputInvalid();
							changed = true;
						}
					}
					if (!changed && touchedMatrix>=0){
						partition.setProperty(group, touchedMatrix);
						matrixTouched(-1);
						outputInvalid();
						changed = true;
					}
				}
				if (changed)
					datas.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED)); //TODO: bogus! should notify via specs not data???
				outputInvalid();
				parametersChanged();
			}
		}
	}

	private void removePartition(){
		if ( selectionInformer !=null && datas!=null) {
			boolean changed=false;
			MatrixPartition partition = (MatrixPartition) datas.getCurrentSpecsSet(MatrixPartition.class);
			if (partition!=null){
				for (int i=0; i<datas.size(); i++) {
					if (selectionInformer.isItemSelected(i, this)) {
						partition.setProperty(null, i);
						if (!changed)
							outputInvalid();
						changed = true;
					}
				}
				if (!changed && touchedMatrix>=0){
					partition.setProperty(null, touchedMatrix);
					matrixTouched(-1);
					outputInvalid();
					changed = true;
				}


				if (changed)
					datas.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED)); //TODO: bogus! should notify via specs not data???
				outputInvalid();
				parametersChanged();

			}
		}
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the matrix group of the selected matrices", "[name of group]", commandName, "setPartition")) {
			setPartition(null, arguments);
		}
		else if (checker.compare(this.getClass(), "Edits the name and color of a matrix group label", "[name of group]", commandName, "editGroup")) {
			String name = parser.getFirstToken(arguments);
			if (StringUtil.blank(name))
				return null;
			String num = parser.getNextToken();
			Object obj = MatrixListPartitionUtil.editGroup(this, containerOfModule(),name, num);
			if (obj!=null) {
				outputInvalid();
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Deletes a character group label", "[name of group]", commandName, "deleteGroup")) {
			String name = parser.getFirstToken(arguments);
			if (StringUtil.blank(name))
				return null;
			String num = parser.getNextToken();
			boolean b = MatrixListPartitionUtil.deleteGroup(this, containerOfModule(),name, num);
			if (b) {
				outputInvalid();
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Creates a new group for use in matrix partitions", null, commandName, "newGroup")) {
			MesquiteString ms = new MesquiteString("");
			MatricesGroup group = MatrixListPartitionUtil.makeGroup(this,containerOfModule(), ms);
			if (group==null) return null;
			setPartition(group, ms.getValue());
		}
		else if (checker.compare(this.getClass(), "Stores the current matrix partition set", null, commandName, "storeCurrent")) {
			if (datas!=null){
				SpecsSetVector ssv = datas.getSpecSetsVector(MatrixPartition.class);
				if (ssv == null || ssv.getCurrentSpecsSet() == null) {
					MatrixPartition partition= new MatrixPartition("Partition", null, datas);
					partition.addToFile(datas.getFile(), getProject(), findElementManager(MatrixPartition.class));
					datas.setCurrentSpecsSet(partition, MatrixPartition.class);
					ssv = datas.getSpecSetsVector(MatrixPartition.class);
				}
				if (ssv!=null) {
					SpecsSet s = ssv.storeCurrentSpecsSet();
					if (s.getFile() == null)
						s.addToFile(datas.getFile(), getProject(), findElementManager(MatrixPartition.class));
					s.setName(ssv.getUniqueName("Partition"));
					String name = MesquiteString.queryString(containerOfModule(), "Name", "Name of matrix partition to be stored", s.getName());
					if (!StringUtil.blank(name))
						s.setName(name);
					ssv.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED));  
				}
				else MesquiteMessage.warnProgrammer("sorry, can't store because no specssetvector");
			}
			//return ((ListWindow)getModuleWindow()).getCurrentObject();
		}
		else if (checker.compare(this.getClass(), "Replaces a stored matrix partition set by the current one", null, commandName, "replaceWithCurrent")) {
			if (datas!=null){
				SpecsSetVector ssv = datas.getSpecSetsVector(MatrixPartition.class);
				if (ssv!=null) {
					SpecsSet chosen = (SpecsSet)ListDialog.queryList(containerOfModule(), "Replace stored set", "Choose stored partition to replace by current set",MesquiteString.helpString, ssv, 0);
					if (chosen!=null){
						SpecsSet current = ssv.getCurrentSpecsSet();
						ssv.replaceStoredSpecsSet(chosen, current);
					}
				}

				outputInvalid();
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Loads the stored matrix partition to be the current one", "[number of partition to load]", commandName, "loadToCurrent")) {
			if (datas !=null) {
				int which = MesquiteInteger.fromFirstToken(arguments, pos);
				if (MesquiteInteger.isCombinable(which)){
					SpecsSetVector ssv = datas.getSpecSetsVector(MatrixPartition.class);
					if (ssv!=null) {
						SpecsSet chosen = ssv.getSpecsSet(which);
						if (chosen!=null){
							ssv.setCurrentSpecsSet(chosen.cloneSpecsSet()); 
							datas.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED)); //TODO: bogus! should notify via specs not data???
							return chosen;
						}
					}
				}
				outputInvalid();
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Removes the group designation from the selected matrices", null, commandName, "removeGroup")) {
			removePartition();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public void setSelectionInformer(SelectionInformer informer){
		deleteAllMenuItems();
		MesquiteSubmenuSpec mss = addSubmenu(null, "Set Group", makeCommand("setPartition", this));
		mss.setList((StringLister)getProject().getFileElement(MatricesGroupVector.class, 0));

		addMenuItem("Remove Group Designation", makeCommand("removeGroup", this));
		addMenuSeparator();
		addMenuItem("New Group...", makeCommand("newGroup",  this));
		MesquiteSubmenuSpec mEGC = addSubmenu(null, "Edit Group...", makeCommand("editGroup", this));
		mEGC.setList((StringLister)getProject().getFileElement(MatricesGroupVector.class, 0));
		MesquiteSubmenuSpec mDGC = addSubmenu(null, "Delete Group...", makeCommand("deleteGroup", this));
		mDGC.setList((StringLister)getProject().getFileElement(MatricesGroupVector.class, 0));
		addMenuSeparator();
		addMenuItem("Store current partition...", makeCommand("storeCurrent",  this));
		addMenuItem("Replace stored partition by current", makeCommand("replaceWithCurrent",  this));
		if (datas !=null)
			addSubmenu(null, "Load set", makeCommand("loadToCurrent",  this), datas.getSpecSetsVector(MatrixPartition.class));
		ManageMatrixPartitions manageCharPart = (ManageMatrixPartitions)findElementManager(MatrixPartition.class);
		addMenuItem("Import Group Labels & Colors Only from File...", MesquiteModule.makeCommand("importLabels",  manageCharPart));
		addMenuItem("Export Group Labels & Colors to File...", MesquiteModule.makeCommand("exportLabels",  manageCharPart));
		//mScsPF = addMenuItem("Create Partition Based upon RAxML Format...", makeCommand("createByRAxML",  this));
		this.selectionInformer = informer;
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}

	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;  
	}
}

