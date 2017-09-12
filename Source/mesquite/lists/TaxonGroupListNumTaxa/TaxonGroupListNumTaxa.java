/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.TaxonGroupListNumTaxa;

import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.lib.table.MesquiteTable;
import mesquite.lists.lib.*;


//TODO: add choice of taxa block

/* ======================================================================== */
public class TaxonGroupListNumTaxa extends TaxonGroupListAssistant  {
	CharacterData data=null;
	MesquiteTable table = null;
	Taxa taxa = null;
//	TaxonSource taxonSourceTask;
	MesquiteCommand tstC;
	MesquiteString taxonSourceName;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (getProject().getNumberTaxas()==0)
			return sorry(getName() + " couldn't start because no blocks of taxa are available.");
/*		taxonSourceTask= (TaxonSource)hireEmployee(TaxonSource.class, "Source of taxa ("+getName()+")");
		if (taxonSourceTask == null)
			return sorry(getName() + " couldn't start because no source of taxa was obtained.");
		//todo: doesn't use taxonSource!!!!!!!
		tstC = makeCommand("setTaxonSource",  this);
		taxonSourceTask.setHiringCommand(tstC);
		taxonSourceName = new MesquiteString();
		taxonSourceName.setValue(taxonSourceTask.getName());
		if (numModulesAvailable(TaxonSource.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Taxon Source", tstC, TaxonSource.class);
			mss.setSelected(taxonSourceName);
		}
		*/
		taxa = getProject().chooseTaxa(containerOfModule(), "For which block of taxa do you want to show group membership?");
		if (taxa==null)
			return sorry(getName() + " couldn't start because taxa block not obtained.");
		taxa.addListener(this);
		return true;
	}
	/*.................................................................................................................*/
	public void endJob(){
		if (taxa!=null)
			taxa.removeListener(this);
		super.endJob();
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		if (obj instanceof Taxa && (Taxa)obj == taxa) {
			iQuit();
		}
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
	//	temp.addLine("setTaxonSource ", taxonSourceTask); 
		temp.addLine("setTaxa " + getProject().getTaxaReferenceExternal(taxa)); 
		return temp;
	}

	public void setTableAndData(MesquiteTable table, CharacterData data){
		//if (this.data !=null)
		//	this.data.removeListener(this);
		this.data = data;
		//data.addListener(this);
		this.table = table;
		taxa = data.getTaxa();
	}
	/*.................................................................................................................*/
	TaxaGroup getTaxonGroup(int ic){
		TaxaGroupVector groups = (TaxaGroupVector)getProject().getFileElement(TaxaGroupVector.class, 0);
		if (groups!=null) {
			if (ic>=0 && ic<groups.size())
				return(TaxaGroup)groups.elementAt(ic);
		}
		return null;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the block of taxa used", "[block reference, number, or name]", commandName, "setTaxa")) {
			Taxa t = getProject().getTaxa(checker.getFile(), parser.getFirstToken(arguments));
			if (t!=null){
				if (taxa!=null)
					taxa.removeListener(this);
				taxa = t;
				if (taxa!=null)
					taxa.addListener(this);
				return taxa;
			}
		}
/*		else  if (checker.compare(this.getClass(), "Sets the module supplying taxa", "[name of module]", commandName, "setTaxonSource")) {
			TaxonSource temp =   (TaxonSource)replaceEmployee(TaxonSource.class, arguments, "Source of taxa for chart", taxonSourceTask);
			if (temp!=null) {
				taxonSourceTask = temp;
				taxonSourceTask.setHiringCommand(tstC);
				taxonSourceName.setValue(taxonSourceTask.getName());
			}
			return taxonSourceTask;

		}
		*/
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/

	public String getStringForRow(int ic) {
		if (taxa==null)
			return "";
		TaxaGroup tg = getTaxonGroup(ic);
		if (taxa==null)
			return "";
		long thisGroupID = tg.getID();
		TaxaPartition partition = (TaxaPartition)taxa.getCurrentSpecsSet(TaxaPartition.class);
		if (partition==null)
			return "";
		int count=0;
		for (int it=0; it<taxa.getNumTaxa(); it++) {
			TaxaGroup tg2 = partition.getTaxaGroup(it);
			if (tg2!=null) {
				if (thisGroupID==tg2.getID())
					count++;
			}
		}
		return ""+count;
	}

	/*.................................................................................................................*/

	public String getWidestString(){
		return "888888888888";
	}
	/*.................................................................................................................*/
	public String getTitle() {
		return "Number of Taxa";
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
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 330;  
	}

	public void setTableAndObject(MesquiteTable table, Object object) {
		this.table = table;

	}

	/*.................................................................................................................*/
	public String getName() {
		return "Number of Taxa Assigned to Group";
	}
	public String getExplanation() {
		return "Shows the number of taxa assigned to each taxon group." ;
	}

}
