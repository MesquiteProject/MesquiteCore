/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.basic.TaxonPartitionHelper;
/*~~  */

import mesquite.lists.lib.*;


import java.awt.*;

import mesquite.basic.ManageTaxaPartitions.ManageTaxaPartitions;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.TaxaGroup;
import mesquite.lib.taxa.TaxaGroupVector;
import mesquite.lib.taxa.TaxaPartition;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.ListDialog;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.lib.ui.MesquiteSymbol;

/* ======================================================================== */
public class TaxonPartitionHelper  extends TaxaSelectedUtility{
	NameParser nameParser;
	SelectionInformer selectionInformer;
	/*.................................................................................................................*/
	public String getName() {
		return "Taxon Partition Helper";
	}
	public String getExplanation() {
		return "Controls adjustments to taxa partitions (e.g., in List of Taxa, Groups column)." ;
	}

	/*.................................................................................................................*/
	Taxa taxa;
	TaxaGroupVector groups;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		groups = (TaxaGroupVector)getProject().getFileElement(TaxaGroupVector.class, 0);
		loadPreferences();
		return true;
	}
	int touchedTaxon = -1;
	public void taxonTouched(int it){//an additional possibility for setting
		touchedTaxon = it;
	}
	private void setGroup(TaxaGroup group, String name, Bits bits, boolean notify){
		if (selectionInformer !=null && taxa!=null) {
			boolean changed=false;
			if (group == null && StringUtil.blank(name))
				return;
			TaxaPartition partition = (TaxaPartition) taxa.getOrMakeCurrentSpecsSet(TaxaPartition.class);
			/*	if (partition==null){
				partition= new TaxaPartition("Partition", taxa.getNumTaxa(), null, taxa);
				partition.addToFile(taxa.getFile(), getProject(), findElementManager(TaxaPartition.class));
				taxa.setCurrentSpecsSet(partition, TaxaPartition.class);
			}*/
			if (group == null){
				TaxaGroupVector groups = (TaxaGroupVector)getProject().getFileElement(TaxaGroupVector.class, 0);
				Object obj = groups.getElement(name);
				group = (TaxaGroup)obj;
			}

			if (group != null && partition != null) {
				if (bits!=null) {
					for (int i=0; i<taxa.getNumTaxa(); i++) {
						if (bits.isBitOn(i)) {
							partition.setProperty(group, i);
							if (!changed)
								outputInvalid();
							changed = true;
						}
					}
				} else {
					for (int i=0; i<taxa.getNumTaxa(); i++) {
						if (selectionInformer.isItemSelected(i, this)) {
							partition.setProperty(group, i);
							if (!changed)
								outputInvalid();
							changed = true;
						}
					}
					if (!changed && touchedTaxon>=0){
						partition.setProperty(group, touchedTaxon);
						taxonTouched(-1);
						outputInvalid();
						changed = true;
				}

				}

			}
			if (changed && notify)
				taxa.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  
			parametersChanged();
		}

	}
	private void removeGroupDesigation(){
		if (selectionInformer !=null && taxa!=null) {
			boolean changed=false;
			TaxaPartition partition = (TaxaPartition) taxa.getCurrentSpecsSet(TaxaPartition.class);
			if (partition!=null){
				for (int i=0; i<taxa.getNumTaxa(); i++) {
					if (selectionInformer.isItemSelected(i, this)) {
						partition.setProperty(null, i);
						if (!changed)
							outputInvalid();
						changed = true;
					}
				}
				if (!changed && touchedTaxon>=0){
					partition.setProperty(null, touchedTaxon);
					taxonTouched(-1);
					outputInvalid();
					changed = true;
			}


				if (changed)
					taxa.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED)); //TODO: bogus! should notify via specs not data???
				parametersChanged();

			}
		}
	}


	/*.................................................................................................................*/
	public  static TaxaGroup createNewTaxonGroup(MesquiteModule module, MesquiteFile file) {
		String n = "Untitled Group";
		if (file==null)
			return null;
		n = file.getFileElements().getUniqueName(n);
		GroupDialog d = new GroupDialog(module.getProject(),module.containerOfModule(), "New Taxon Group", n, Color.white, null, TaxaGroup.supportsSymbols());
		d.completeAndShowDialog();
		String name = d.getName();
		boolean ok = d.query()==0;
		Color c = d.getColor();
		MesquiteSymbol symbol = d.getSymbol();

		d.dispose();
		if (!ok)
			return null;
		//String name = MesquiteString.queryString(containerOfModule(), "New character group", "New character group label", "Untitled Group");
		if (StringUtil.blank(name))
			return null;
		TaxaGroup group = new TaxaGroup();
		group.setName(name);
		if (symbol!=null)
			group.setSymbol(symbol);
		group.addToFile(file, file.getProject(), null);
		if (c!=null) {
			group.setColor(c);
		}
		return group;
	}

	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		if (nameParser!=null){
			String s = nameParser.preparePreferencesForXML(); 
			if (StringUtil.notEmpty(s))
				buffer.append(s);
		}
		return buffer.toString();
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if (nameParser!=null)
			nameParser.processSingleXMLPreference(tag,content);
	}

	/*.................................................................................................................*/

	private void createPartitionBasedOnNames() {
		if (taxa!=null){
			String groupName = "";
			String name="";
			Bits taxonProcessed = new Bits(taxa.getNumTaxa());
			Bits taxonInGroup = new Bits(taxa.getNumTaxa());
			if (!MesquiteThread.isScripting()) {
				String helpString = "Taxon groups will be created based upon a portion of the taxon names.  In particular, the name of each taxon will be reduced "
						+ "by removing a piece from the start and/or end; that reduced name will become the name of the taxon group.  If two taxa have the same"
						+ " reduced name, the will be assigned to the same taxon group";
				nameParser.setTrimVerb("yields group name");
				if (taxa.getNumTaxa()>=3)
					nameParser.setExamples(new String[]{taxa.getTaxonName(0), taxa.getTaxonName(taxa.getNumTaxa()/2), taxa.getTaxonName(taxa.getNumTaxa()-1)});
				else if (taxa.getNumTaxa()>0)
					nameParser.setExamples(new String[]{taxa.getTaxonName(0)});

				if (nameParser.queryOptions("Options for Creating Groups", "Group names will be extracted from taxon names.", "In constructing the name of the group from the taxon name,", helpString)) {
					storePreferences();
				}
				else
					return;
			}

			boolean anySelected= selectionInformer.anyItemsSelected(this); 

			for (int it=0; it<taxa.getNumTaxa(); it++) {
				if (!taxonProcessed.isBitOn(it)  && (!anySelected || selectionInformer.isItemSelected(it, this))) {
					groupName = nameParser.extractPart(taxa.getTaxonName(it));
					TaxaGroup group = TaxaGroup.makeGroupIfNovel(this, groupName,taxa,groups);
					taxonProcessed.setBit(it, true);
					taxonInGroup.setBit(it,true);
					for (int ij=it+1; ij<taxa.getNumTaxa(); ij++) {
						name = nameParser.extractPart(taxa.getTaxonName(ij));
						if (groupName!=null && groupName.equalsIgnoreCase(name)) {   // would have same name as the current group, therefore set as this group
							taxonInGroup.setBit(ij,true);
							taxonProcessed.setBit(ij, true);
						}

					}
					setGroup(group, name, taxonInGroup, false);
					taxonInGroup.clearAllBits();

				}
			}
			taxa.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  
		}	
	}


	/*.................................................................................................................*/


	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets to which group a taxon belongs in the current taxa partition", "[name of group]", commandName, "setPartition")) {
			setGroup(null, parser.getFirstToken(arguments),null, true);
		}
		else if (checker.compare(this.getClass(), "Edits the name, color, and symbol of a taxon group label", "[name of group]", commandName, "editGroup")) {
			String name = parser.getFirstToken(arguments);
			if (StringUtil.blank(name))
				return null;
			String num = parser.getNextToken();
			int i = MesquiteInteger.fromString(num);
			TaxaGroupVector groups = (TaxaGroupVector)getProject().getFileElement(TaxaGroupVector.class, 0);
			Object obj;
			if (MesquiteInteger.isCombinable(i) && i< groups.size())
				obj = groups.elementAt(i);
			else
				obj = groups.getElement(name);
			if (obj != null) {
				TaxaGroup group = (TaxaGroup)obj;

				GroupDialog d = new GroupDialog(getProject(),containerOfModule(), "Edit Taxon Group", group.getName(), group.getColor(), group.getSymbol(),group.supportsSymbols());
				d.completeAndShowDialog();
				name = d.getName();
				boolean ok = d.query()==0;
				Color c = d.getColor();
				MesquiteSymbol symbol = d.getSymbol();
				d.dispose();
				if (!ok)
					return null;


				if (!StringUtil.blank(name)) {
					group.setName(name);
					group.setColor(c);
					if (symbol!=null)
						group.setSymbol(symbol);
					taxa.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  
					group.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
				}
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Creates a new group for use in taxon partitions", null, commandName, "newGroup")) {
			TaxaGroup group= TaxaGroup.createNewTaxonGroup(this, taxa.getFile());
			if (group!=null)
				setGroup(group, parser.getFirstToken(group.getName()), null, true);
			return group;
		}
		else if (checker.compare(this.getClass(), "To any groups that have not color, assign color randomly", null, commandName, "assignColorsRandomly")) {
			if (taxa!=null){
				TaxaPartition partition = (TaxaPartition)taxa.getCurrentSpecsSet(TaxaPartition.class);
				if (partition!=null) {
					Color randomColor = null;
					for (int it = 0; it< taxa.getNumTaxa(); it++){
						TaxaGroup group = (TaxaGroup)partition.getProperty(it);
						if (group != null && group.getColor() == null){
							randomColor = ColorDistribution.getRandomColor(randomColor);
							group.setColor(randomColor);
						}
					}

					partition.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  
					taxa.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  
				}
			}
		}
		else if (checker.compare(this.getClass(), "Stores the current taxa partition as a TAXAPARTITION", null, commandName, "storeCurrent")) {
			if (taxa!=null){
				SpecsSetVector ssv = taxa.getSpecSetsVector(TaxaPartition.class);
				if (ssv == null || ssv.getCurrentSpecsSet() == null) {
					TaxaPartition partition= new TaxaPartition("Partition", taxa.getNumTaxa(), null, taxa);
					partition.addToFile(taxa.getFile(), getProject(), findElementManager(TaxaPartition.class));
					taxa.setCurrentSpecsSet(partition, TaxaPartition.class);
					ssv = taxa.getSpecSetsVector(TaxaPartition.class);
				}
				if (ssv!=null) {
					SpecsSet s = ssv.storeCurrentSpecsSet();
					if (s.getFile() == null)
						s.addToFile(taxa.getFile(), getProject(), findElementManager(TaxaPartition.class));
					s.setName(ssv.getUniqueName("Partition"));
					String name = MesquiteString.queryString(containerOfModule(), "Name", "Name of taxa partition to be stored", s.getName());
					if (!StringUtil.blank(name))
						s.setName(name);
					ssv.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  
				}
				else MesquiteMessage.warnProgrammer("sorry, can't store because no specssetvector");
			}
			//return ((ListWindow)getModuleWindow()).getCurrentObject();
		}
		else if (checker.compare(this.getClass(), "Replaces a stored taxa partition by the current one", null, commandName, "replaceWithCurrent")) {
			if (taxa!=null){
				SpecsSetVector ssv = taxa.getSpecSetsVector(TaxaPartition.class);
				if (ssv!=null) {
					SpecsSet chosen = (SpecsSet)ListDialog.queryList(containerOfModule(), "Replace stored partition", "Choose stored partition to replace by current partition", MesquiteString.helpString,ssv, 0);
					if (chosen!=null){
						SpecsSet current = ssv.getCurrentSpecsSet();
						ssv.replaceStoredSpecsSet(chosen, current);
					}
				}

			}
			//return ((ListWindow)getModuleWindow()).getCurrentObject();
		}
		else if (checker.compare(this.getClass(), "Creates a taxa partition based upon the names of taxa", null, commandName, "createBasedOnNames")) {
			createPartitionBasedOnNames();
			//return ((ListWindow)getModuleWindow()).getCurrentObject();
		}
		else if (checker.compare(this.getClass(), "Creates a taxa block based upon the current partition", null, commandName, "createTaxaBlock")) {
			//ZQ: FYI: I moved the two methods out of Taxa. They are best not done there. 
			// A module should do this, partly because it is not just the Taxa's internal management but affects the whole project, and partly to have interface choices.
			//  ManageTaxa is exactly for this sort of thing. Also changed name of method, since it's from the partition.
			TaxaManager manager = (TaxaManager)findElementManager(Taxa.class);
			if (manager!=null) {
				manager.createTaxaBlockBasedOnPartition(taxa);
			}
			//return ((ListWindow)getModuleWindow()).getCurrentObject();
		}
		else if (checker.compare(this.getClass(), "Loads the stored taxa partition to be the current one", "[number of partition to load]", commandName, "loadToCurrent")) {
			if (taxa !=null) {
				int which = MesquiteInteger.fromFirstToken(arguments, stringPos);
				if (MesquiteInteger.isCombinable(which)){
					SpecsSetVector ssv = taxa.getSpecSetsVector(TaxaPartition.class);
					if (ssv!=null) {
						SpecsSet chosen = ssv.getSpecsSet(which);
						if (chosen!=null){
							ssv.setCurrentSpecsSet(chosen.cloneSpecsSet()); 
							taxa.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED)); //TODO: bogus! should notify via specs not data???
							return chosen;
						}
					}
				}
			}
		}
		else if (checker.compare(this.getClass(), "Removes the group designation from the selected taxa", null, commandName, "removeGroup")) {
			removeGroupDesigation();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	public void setTaxaAndSelectionInformer(Taxa taxa, SelectionInformer informer){
		deleteAllMenuItems();
		MesquiteSubmenuSpec mss = addSubmenu(null, "Set Group", makeCommand("setPartition", this));
		mss.setList((StringLister)getProject().getFileElement(TaxaGroupVector.class, 0));

		addMenuItem("Remove Group Designation", makeCommand("removeGroup",  this));

		addMenuSeparator();
		addMenuItem("New Group...", makeCommand("newGroup",  this));
		MesquiteSubmenuSpec mEGC = addSubmenu(null, "Edit Group...", makeCommand("editGroup", this));
		mEGC.setList((StringLister)getProject().getFileElement(TaxaGroupVector.class, 0));
		addMenuSeparator();
		ManageTaxaPartitions manageTaxPart = (ManageTaxaPartitions)findElementManager(TaxaPartition.class);
		addMenuItem("Import Partition (Groups) from File...", new MesquiteCommand("importPartitions",  "#" + taxa.getAssignedID(), manageTaxPart));
		addMenuItem("Export Current Partition and Group Labels/Colors to File...", new MesquiteCommand("exportPartitionAndLabels",  "#" + taxa.getAssignedID(), manageTaxPart));
		addMenuItem("Import Group Labels & Colors Only from File...", MesquiteModule.makeCommand("importLabels",  manageTaxPart));
		addMenuItem("Export Group Labels & Colors Only to File...", MesquiteModule.makeCommand("exportLabels",  manageTaxPart));
		addMenuItem("Assign Colours Randomly to Colourless Groups", makeCommand("assignColorsRandomly", this));

		addMenuSeparator();
		addMenuItem("Store Current Partition...", makeCommand("storeCurrent",  this));
		addMenuItem("Create and assign groups based on taxon names...", makeCommand("createBasedOnNames",  this));
		addMenuItem("Create taxa block based on current groups...", makeCommand("createTaxaBlock",  this));
		addMenuItem("Replace stored partition by current", makeCommand("replaceWithCurrent",  this));
		if (taxa !=null) {
			addSubmenu(null, "Load partition", makeCommand("loadToCurrent",  this), taxa.getSpecSetsVector(TaxaPartition.class));
		}
		/*if (taxa != this.taxa){
			if (this.taxa != null)
				this.taxa.removeListener(this);
			taxa.addListener(this);
		}*/
		this.taxa = taxa;
		this.selectionInformer = informer;
	}
	/*
	public void changed(Object caller, Object obj, Notification notification){
		if (caller == this)
			return;
		parametersChanged(notification);
	}

	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;  
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}


	/*.................................................................................................................*/
}

