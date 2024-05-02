/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.TaxonListCurrPartition;
/*~~  */

import mesquite.lists.lib.*;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class TaxonListCurrPartition extends TaxonListAssistant {
	/*.................................................................................................................*/
	public String getName() {
		return "Group Membership (taxa)";
	}
	public String getExplanation() {
		return "Lists and allows changes to group membership in the current partition of taxa, for List of Taxa window." ;
	}
	/*.................................................................................................................*/
	Taxa taxa;
	MesquiteTable table=null;
	MesquiteSubmenuSpec mss, mEGC;
	MesquiteMenuItemSpec mCreatec, mScs, mStc, mRssc, mLine, nNG, mLine2, ms2, mCreateTaxac;
	TaxaGroupVector groups;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		groups = (TaxaGroupVector)getProject().getFileElement(TaxaGroupVector.class, 0);
		groups.addListener(this);
		return true;
	}
	public void endJob(){
		if (taxa != null)
			taxa.removeListener(this);
		if (groups != null)
			groups.removeListener(this);
		super.endJob();
	}
	private void setGroup(TaxaGroup group, String name, Bits bits, boolean notify){
		if (table !=null && taxa!=null) {
			boolean changed=false;
			if (group == null && StringUtil.blank(name))
				return;
			TaxaPartition partition = (TaxaPartition) taxa.getCurrentSpecsSet(TaxaPartition.class);
			if (partition==null){
				partition= new TaxaPartition("Partition", taxa.getNumTaxa(), null, taxa);
				partition.addToFile(taxa.getFile(), getProject(), findElementManager(TaxaPartition.class));
				taxa.setCurrentSpecsSet(partition, TaxaPartition.class);
			}
			if (group == null){
				TaxaGroupVector groups = (TaxaGroupVector)getProject().getFileElement(TaxaGroupVector.class, 0);
				Object obj = groups.getElement(name);
				group = (TaxaGroup)obj;
			}

			if (group != null) {
				if (partition != null) {
					if (bits!=null) {
						for (int i=0; i<taxa.getNumTaxa(); i++) {
							if (bits.isBitOn(i)) {
								partition.setProperty(group, i);
								if (!changed)
									outputInvalid();
								changed = true;
							}
						}
					} else if (employer!=null && employer instanceof ListModule) {
						int c = ((ListModule)employer).getMyColumn(this);
						for (int i=0; i<taxa.getNumTaxa(); i++) {
							if (table.isCellSelectedAnyWay(c, i)) {
								partition.setProperty(group, i);
								if (!changed)
									outputInvalid();
								changed = true;
							}
						}
					}
				}
				if (changed && notify)
					taxa.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  
				parametersChanged();
			}
		}
	}
	private void removeGroupDesigation(){
		if (table !=null && taxa!=null) {
			boolean changed=false;
			TaxaPartition partition = (TaxaPartition) taxa.getCurrentSpecsSet(TaxaPartition.class);
			if (partition!=null){
				if (employer!=null && employer instanceof ListModule) {
					int c = ((ListModule)employer).getMyColumn(this);
					for (int i=0; i<taxa.getNumTaxa(); i++) {
						if (table.isCellSelectedAnyWay(c, i)) {
							partition.setProperty(null, i);
							if (!changed)
								outputInvalid();
							changed = true;
						}
					}
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
	private String groupNameFromTaxonName(String taxonName) {
		String name = StringUtil.getAllButLastItem(taxonName, " ");  //TODO:   give options
		return name;
	}
	/*.................................................................................................................*/

	private void createPartitionBasedOnNames() {
		if (taxa!=null){
			String groupName = "";
			String name="";
			Bits taxonProcessed = new Bits(taxa.getNumTaxa());
			Bits taxonInGroup = new Bits(taxa.getNumTaxa());
			MesquiteFile file=taxa.getFile();

			for (int it=0; it<taxa.getNumTaxa(); it++) {
				if (!taxonProcessed.isBitOn(it)) {
					groupName = groupNameFromTaxonName(taxa.getTaxonName(it));
					TaxaGroup group = new TaxaGroup();
					group.setName(groupName);
					group.addToFile(file, file.getProject(), null);
					taxonProcessed.setBit(it, true);
					taxonInGroup.setBit(it,true);
					for (int ij=it+1; ij<taxa.getNumTaxa(); ij++) {
						name = groupNameFromTaxonName(taxa.getTaxonName(ij));
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
	
	/*.................................................................................................................*/   //WAYNECHECK
	private Taxa createTaxonBlock(int numTaxa) {
		incrementMenuResetSuppression();
		Taxa newTaxa=null;
		String title= getProject().getTaxas().getUniqueName("Taxa");
		MesquiteBoolean answer = new MesquiteBoolean(true);
		MesquiteFile file = getProject().chooseFile( "Select file to which to add the new block of taxa"); //added 20 Dec 01

		newTaxa = new Taxa(numTaxa);
		if (title!=null)
			newTaxa.setName(title);
		if (newTaxa==null)
			return null;
		newTaxa.addToFile(file, getProject(), null);

		decrementMenuResetSuppression();
		return newTaxa;
	}
	/*.................................................................................................................*/

	private void createTaxonBlockBasedOnNames() {
		if (taxa!=null){
			int numGroups=0;
			TaxaPartition part = (TaxaPartition)taxa.getCurrentSpecsSet(TaxaPartition.class);
			if (part!=null) {
				Bits taxonProcessed = new Bits(taxa.getNumTaxa());
				for (int it=0; it<taxa.getNumTaxa(); it++) {  // first we count
					if (!taxonProcessed.isBitOn(it)) {
						TaxaGroup tg = part.getTaxaGroup(it);
						if (tg!=null){
							numGroups++;
							taxonProcessed.setBit(it, true);
							for (int ij=it+1; ij<taxa.getNumTaxa(); ij++) {
								TaxaGroup tg2 = part.getTaxaGroup(ij);
								if (tg2!=null && tg.equals(tg2))  // we've encountered this one before.
									taxonProcessed.setBit(ij, true);
							}

						}
					}
				}
				int count=0;
				taxonProcessed.clearAllBits();
				Taxa newTaxa = createTaxonBlock(numGroups);   // now create taxa
				for (int it=0; it<taxa.getNumTaxa(); it++) {  // now name the new taxa
					if (!taxonProcessed.isBitOn(it)) {
						TaxaGroup tg = part.getTaxaGroup(it);
						if (tg!=null){
							String name = tg.getName();
							if (count<newTaxa.getNumTaxa())
								newTaxa.getTaxon(count).setName(name);
							count++;
							taxonProcessed.setBit(it, true);
							for (int ij=it+1; ij<taxa.getNumTaxa(); ij++) {
								TaxaGroup tg2 = part.getTaxaGroup(ij);
								if (tg2!=null && tg.equals(tg2))  // we've encountered this one before.
									taxonProcessed.setBit(ij, true);
							}

						}
					}
				}
			}
		}

	}	


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
			TaxaGroup group= TaxaListPartitionUtil.createNewTaxonGroup(this, taxa.getFile());
			if (group!=null)
				setGroup(group, parser.getFirstToken(group.getName()), null, true);
			return group;
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
		else if (checker.compare(this.getClass(), "Creates a taxa partition based upon the names of taxa", null, commandName, "createTaxonBlock")) {
			createTaxonBlockBasedOnNames();
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
	public void setTableAndTaxa(MesquiteTable table, Taxa taxa){
		/* hire employees here */
		deleteMenuItem(mss);
		deleteMenuItem(mScs);
		deleteMenuItem(mCreatec);
		deleteMenuItem(mCreateTaxac);
		deleteMenuItem(mRssc);
		deleteMenuItem(mLine);
		deleteMenuItem(mLine2);
		deleteMenuItem(mStc);
		deleteMenuItem(ms2);
		deleteMenuItem(mEGC);
		deleteMenuItem(nNG);
		mss = addSubmenu(null, "Set Group", makeCommand("setPartition", this));
		mss.setList((StringLister)getProject().getFileElement(TaxaGroupVector.class, 0));

		ms2 = addMenuItem("Remove Group Designation", makeCommand("removeGroup",  this));

		mLine2 = addMenuSeparator();
		nNG = addMenuItem("New Group...", makeCommand("newGroup",  this));
		mEGC = addSubmenu(null, "Edit Group...", makeCommand("editGroup", this));
		mEGC.setList((StringLister)getProject().getFileElement(TaxaGroupVector.class, 0));

		mLine = addMenuSeparator();
		mScs = addMenuItem("Store current partition...", makeCommand("storeCurrent",  this));
		mCreatec = addMenuItem("Create partition based on taxon names...", makeCommand("createBasedOnNames",  this));
		mCreateTaxac = addMenuItem("Create taxon block based on current groups...", makeCommand("createTaxonBlock",  this));
		mRssc = addMenuItem("Replace stored partition by current", makeCommand("replaceWithCurrent",  this));
		if (taxa !=null) {
			mStc = addSubmenu(null, "Load partition", makeCommand("loadToCurrent",  this), taxa.getSpecSetsVector(TaxaPartition.class));
		}
		if (taxa != this.taxa){
			if (this.taxa != null)
				this.taxa.removeListener(this);
			taxa.addListener(this);
		}
		this.taxa = taxa;
		this.table = table;
	}
	public void changed(Object caller, Object obj, Notification notification){
		if (caller == this)
			return;
		outputInvalid();
		parametersChanged(notification);
	}
	public String getTitle() {
		return "Group";
	}
	public String getStringForTaxon(int ic){
		if (taxa!=null) {
			TaxaPartition part = (TaxaPartition)taxa.getCurrentSpecsSet(TaxaPartition.class);
			if (part==null)
				return "?";
			TaxaGroup tg = part.getTaxaGroup(ic);
			if (tg==null)
				return "?";
			return tg.getName();
		}
		return "?";
	}
	public boolean useString(int ic){
		return false;
	}
	public void drawInCell(int ic, Graphics g, int x, int y,  int w, int h, boolean selected){
		if (taxa==null || g==null)
			return;
		TaxaPartition part = (TaxaPartition)taxa.getCurrentSpecsSet(TaxaPartition.class);
		Color c = g.getColor();
		MesquiteSymbol symbol = null;
		boolean colored = false;
		Color backgroundColor = null;
		if (part!=null) {
			TaxaGroup tg = part.getTaxaGroup(ic);
			if (tg!=null){
				backgroundColor= tg.getColor();
				if (backgroundColor!=null){
					g.setColor(backgroundColor);
					g.fillRect(x+1,y+1,w-1,h-1);
					colored = true;
				}
				symbol = tg.getSymbol();
			}
		}
		if (!colored){ 
			if (selected)
				g.setColor(Color.black);
			else
				g.setColor(Color.white);
			g.fillRect(x+1,y+1,w-1,h-1);
		}
		if (symbol!=null) {
			symbol.drawSymbol(g,x+w-h/2,y+h/2,w-3,h/2-3,true);
		}

		String s = getStringForRow(ic);
		if (s!=null){
			FontMetrics fm = g.getFontMetrics(g.getFont());
			if (fm==null)
				return;
			int sw = fm.stringWidth(s);
			int sh = fm.getMaxAscent()+ fm.getMaxDescent();
			if (backgroundColor==null) {
				if (selected)
					g.setColor(Color.white);
				else
					g.setColor(Color.black);
			} else {  // background is color; choose contrasting color
				Color contrast = ColorDistribution.getContrastingTextColor(backgroundColor);
				g.setColor(contrast);
			}
			g.drawString(s, x+(w-sw)/2, y+h-(h-sh)/2);
			if (c!=null) g.setColor(c);
		}

	}

	public String getWidestString(){
		return "88888888888  ";
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}

	/*.................................................................................................................*/
}

