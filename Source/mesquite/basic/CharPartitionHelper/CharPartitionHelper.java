/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.basic.CharPartitionHelper;
/*~~  */

import mesquite.categ.lib.DNAData;
import mesquite.charMatrices.ManageCharPartitions.ManageCharPartitions;
import mesquite.lib.AssociableWithSpecs;
import mesquite.lib.CommandChecker;
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
import mesquite.lib.characters.CharacterPartition;
import mesquite.lib.characters.CharactersGroup;
import mesquite.lib.characters.CharactersGroupVector;
import mesquite.lib.characters.CodonPositionsSet;
import mesquite.lib.duties.CharactersSelectedUtility;
import mesquite.lib.ui.ListDialog;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.lists.lib.CharListPartitionUtil;

/* ======================================================================== */
public class CharPartitionHelper extends CharactersSelectedUtility {
	/*.................................................................................................................*/
	public String getName() {
		return "Character Partition Helper";
	}
	public String getExplanation() {
		return "Controls adjustments to character partitions (e.g., in List of Characters, Groups column)." ;
	}
	/*.................................................................................................................*/
	CharacterData data=null;
	CharactersGroupVector groups;
	SelectionInformer selectionInformer;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		groups = (CharactersGroupVector)getProject().getFileElement(CharactersGroupVector.class, 0);
		return true;
	}

	int touchedCharacter = -1;
	public void characterTouched(int it){  //an additional possibility for setting
		touchedCharacter = it;
	}
	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	private void setPartition(CharactersGroup group, String arguments){
		if (selectionInformer !=null && data!=null) {
			boolean changed=false;
			String name = parser.getFirstToken(arguments);
			if (group == null && StringUtil.blank(name))
				return;
			CharacterPartition partition = (CharacterPartition) data.getCurrentSpecsSet(CharacterPartition.class);
			if (partition==null){
				partition= new CharacterPartition("Partition", data.getNumChars(), null, data);
				partition.addToFile(data.getFile(), getProject(), findElementManager(CharacterPartition.class));
				data.setCurrentSpecsSet(partition, CharacterPartition.class);
			}
			if (group == null){
				CharactersGroupVector groups = (CharactersGroupVector)getProject().getFileElement(CharactersGroupVector.class, 0);
				Object obj = groups.getElement(name);
				group = (CharactersGroup)obj;
			}
			if (group != null) {
				if (partition != null) {
					for (int i=0; i<data.getNumChars(); i++) {
						if (selectionInformer.isItemSelected(i, this)) {
							partition.setProperty(group, i);
							if (!changed)
								outputInvalid();
							changed = true;
						}
					}
					if (!changed && touchedCharacter>=0){
						partition.setProperty(group, touchedCharacter);
						characterTouched(-1);
						outputInvalid();
						changed = true;
					}
				}
				if (changed)
					data.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED)); //TODO: bogus! should notify via specs not data???
				outputInvalid();
				parametersChanged();
			}
		}
	}
	private void refineByCodonPosition(){
		if (selectionInformer !=null && data!=null) {
			boolean changed=false;
			CodonPositionsSet codons = (CodonPositionsSet) data.getCurrentSpecsSet(CodonPositionsSet.class);
			CharacterPartition partition = (CharacterPartition) data.getCurrentSpecsSet(CharacterPartition.class);
			if (codons == null || partition == null)
				return;
			for (int i=0; i<data.getNumChars(); i++) {
				if (selectionInformer.isItemSelected(i, this)) {
					if (codons.getInt(i)>0 && codons.getInt(i)<=3 ){
						CharactersGroup currentGroup = (CharactersGroup)partition.getProperty(i);
						String newName = currentGroup.getName() + "_" + codons.toString(i);
						CharactersGroup newGroup = (CharactersGroup)groups.elementWithName(newName);
						if (newGroup == null){
							newGroup = new CharactersGroup();
							newGroup.setName(newName);
							newGroup.setColor(currentGroup.getColor());
							newGroup.setSymbol(currentGroup.getSymbol());
							groups.addElement(newGroup, true);
						}
						partition.setProperty(newGroup, i);
						if (!changed)
							outputInvalid();
						changed = true;
					}
				}

			}

			if (changed)
				data.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED)); //TODO: bogus! should notify via specs not data???
			outputInvalid();
			parametersChanged();


		}
	}
	private void removePartition(){
		if ( selectionInformer !=null && data!=null) {
			boolean changed=false;
			CharacterPartition partition = (CharacterPartition) data.getCurrentSpecsSet(CharacterPartition.class);
			if (partition!=null){
				for (int i=0; i<data.getNumChars(); i++) {
					if (selectionInformer.isItemSelected(i, this)) {
						partition.setProperty(null, i);
						if (!changed)
							outputInvalid();
						changed = true;
					}
				}
				if (!changed && touchedCharacter>=0){
					partition.setProperty(null, touchedCharacter);
					characterTouched(-1);
					outputInvalid();
					changed = true;
				}


				if (changed)
					data.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED)); //TODO: bogus! should notify via specs not data???
				outputInvalid();
				parametersChanged();

			}
		}
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the character group of the selected characters", "[name of group]", commandName, "setPartition")) {
			setPartition(null, arguments);
		}
		else if (checker.compare(this.getClass(), "Edits the name and color of a character group label", "[name of group]", commandName, "editGroup")) {
			String name = parser.getFirstToken(arguments);
			if (StringUtil.blank(name))
				return null;
			String num = parser.getNextToken();
			Object obj = CharListPartitionUtil.editGroup(this, data,containerOfModule(),name, num);
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
			boolean b = CharListPartitionUtil.deleteGroup(this, data,containerOfModule(),name, num);
			if (b) {
				outputInvalid();
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Creates a new group for use in character partitions", null, commandName, "newGroup")) {
			MesquiteString ms = new MesquiteString("");
			CharactersGroup group = CharListPartitionUtil.makeGroup(this,data,containerOfModule(), ms);
			if (group==null) return null;
			setPartition(group, ms.getValue());
		}
		else if (checker.compare(this.getClass(), "Stores the current character partition set", null, commandName, "storeCurrent")) {
			if (data!=null){
				SpecsSetVector ssv = data.getSpecSetsVector(CharacterPartition.class);
				if (ssv == null || ssv.getCurrentSpecsSet() == null) {
					CharacterPartition partition= new CharacterPartition("Partition", data.getNumChars(), null, data);
					partition.addToFile(data.getFile(), getProject(), findElementManager(CharacterPartition.class));
					data.setCurrentSpecsSet(partition, CharacterPartition.class);
					ssv = data.getSpecSetsVector(CharacterPartition.class);
				}
				if (ssv!=null) {
					SpecsSet s = ssv.storeCurrentSpecsSet();
					if (s.getFile() == null)
						s.addToFile(data.getFile(), getProject(), findElementManager(CharacterPartition.class));
					s.setName(ssv.getUniqueName("Partition"));
					String name = MesquiteString.queryString(containerOfModule(), "Name", "Name of character partition to be stored", s.getName());
					if (!StringUtil.blank(name))
						s.setName(name);
					ssv.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED));  
				}
				else MesquiteMessage.warnProgrammer("sorry, can't store because no specssetvector");
			}
			//return ((ListWindow)getModuleWindow()).getCurrentObject();
		}
		else if (checker.compare(this.getClass(), "Replaces a stored character partition set by the current one", null, commandName, "replaceWithCurrent")) {
			if (data!=null){
				SpecsSetVector ssv = data.getSpecSetsVector(CharacterPartition.class);
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
		else if (checker.compare(this.getClass(), "Loads the stored character partition to be the current one", "[number of partition to load]", commandName, "loadToCurrent")) {
			if (data !=null) {
				int which = MesquiteInteger.fromFirstToken(arguments, pos);
				if (MesquiteInteger.isCombinable(which)){
					SpecsSetVector ssv = data.getSpecSetsVector(CharacterPartition.class);
					if (ssv!=null) {
						SpecsSet chosen = ssv.getSpecsSet(which);
						if (chosen!=null){
							ssv.setCurrentSpecsSet(chosen.cloneSpecsSet()); 
							data.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED)); //TODO: bogus! should notify via specs not data???
							return chosen;
						}
					}
				}
				outputInvalid();
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Removes the group designation from the selected characters", null, commandName, "removeGroup")) {
			removePartition();
		}
		else if (checker.compare(this.getClass(), "Refines current groups by intersecting them with codon positions", null, commandName, "refineByCodonPosition")) {
			refineByCodonPosition();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public void setDataAndSelectionInformer(CharacterData data, SelectionInformer informer){
		deleteAllMenuItems();
		MesquiteSubmenuSpec mss = addSubmenu(null, "Set Group", makeCommand("setPartition", this));
		mss.setList((StringLister)getProject().getFileElement(CharactersGroupVector.class, 0));
		if (data != null && data instanceof DNAData)
			addMenuItem("Refine Groups by Codon Position", makeCommand("refineByCodonPosition", this));


		addMenuItem("Remove Group Designation", makeCommand("removeGroup", this));
		addMenuSeparator();
		addMenuItem("New Group...", makeCommand("newGroup",  this));
		MesquiteSubmenuSpec mEGC = addSubmenu(null, "Edit Group...", makeCommand("editGroup", this));
		mEGC.setList((StringLister)getProject().getFileElement(CharactersGroupVector.class, 0));
		MesquiteSubmenuSpec mDGC = addSubmenu(null, "Delete Group...", makeCommand("deleteGroup", this));
		mDGC.setList((StringLister)getProject().getFileElement(CharactersGroupVector.class, 0));
		addMenuSeparator();
		addMenuItem("Store current partition...", makeCommand("storeCurrent",  this));
		addMenuItem("Replace stored partition by current", makeCommand("replaceWithCurrent",  this));
		if (data !=null)
			addSubmenu(null, "Load set", makeCommand("loadToCurrent",  this), data.getSpecSetsVector(CharacterPartition.class));
		ManageCharPartitions manageCharPart = (ManageCharPartitions)findElementManager(CharacterPartition.class);
		addMenuItem("Import Group Labels & Colors Only from File...", MesquiteModule.makeCommand("importLabels",  manageCharPart));
		addMenuItem("Export Group Labels & Colors to File...", MesquiteModule.makeCommand("exportLabels",  manageCharPart));
		//mScsPF = addMenuItem("Create Partition Based upon RAxML Format...", makeCommand("createByRAxML",  this));
		this.data = data;
		this.selectionInformer = informer;
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}

	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}
}


