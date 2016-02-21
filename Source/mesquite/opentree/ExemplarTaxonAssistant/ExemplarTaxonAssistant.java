package mesquite.opentree.ExemplarTaxonAssistant;
/* Mesquite source code.  Copyright 1997-2007 W. Maddison and D. Maddison. 
Version 2.0, September 2007.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
/*~~  */


import mesquite.lists.lib.*;
import mesquite.lib.*;
import mesquite.lib.table.*;



	/* ======================================================================== */
	public class ExemplarTaxonAssistant extends TaxonListAssistant {
		Taxa taxa;
		MesquiteTable table=null;
		NameReference anr = NameReference.getNameReference("Non-exemplarTaxon");
		MesquiteMenuItemSpec exemplarMenuID, nonExemplarMenuID;
		/*.................................................................................................................*/
		public boolean startJob(String arguments, Object condition, boolean hiredByName) {
			return true;
	  	 }
	 
		/*.................................................................................................................*/
		public void setTableAndTaxa(MesquiteTable table, Taxa taxa){
			if (this.taxa != null)
				this.taxa.removeListener(this);
			this.taxa = taxa;
			if (this.taxa != null)
				this.taxa.addListener(this);
			this.table = table;
			deleteMenuItem(exemplarMenuID);
			deleteMenuItem(nonExemplarMenuID);
			exemplarMenuID = addMenuItem("Exemplar Taxon", makeCommand("exemplar", this));
			nonExemplarMenuID = addMenuItem("Non-Exemplar Taxon", makeCommand("nonexemplar", this));
		}
		/*...............................................................................................................*/
		public void changed(Object caller, Object obj, Notification notification){
			outputInvalid();
			parametersChanged(notification);
		}
		/*...............................................................................................................*/
		public String getTitle() {
			return "Exemplar Taxon";
		}
		public String getStringForTaxon(int ic){

			if (taxa!=null) {
				boolean isNonExemplar = taxa.getAssociatedBit(anr, ic);
					if (isNonExemplar)
						return "non-exemplar";
					else
						return "exemplar";

			}
			return "-";
		}
		/*...............................................................................................................*/
		/** for those permitting editing, indicates user has edited to incoming value.*/
		public void setValue(int row, boolean exemplar){
			if (taxa!=null) {
				taxa.setAssociatedBit(anr, row, !exemplar);
			}
		}

		/*.................................................................................................................*/
		private void setTaxonStatus(boolean exemplar){
			if (table !=null && taxa!=null) {
				boolean changed=false;
				if (employer!=null && employer instanceof ListModule) {
					int c = ((ListModule)employer).getMyColumn(this);
					for (int i=0; i<taxa.getNumTaxa(); i++) {
						if (table.isCellSelectedAnyWay(c, i)) {
							boolean currentStatus = taxa.getAssociatedBit(anr, i);
							if (currentStatus!=!exemplar) {
								setValue(i,exemplar);
								if (!changed)
									outputInvalid();
								changed = true;
							}

						}
					}
				}
				outputInvalid();
				if (changed)
					parametersChanged();

			}
		}
		/*.................................................................................................................*/
		public Object doCommand(String commandName, String arguments, CommandChecker checker) {
			if (checker.compare(this.getClass(), "Sets the taxon to be an exemplar", null, commandName, "exemplar")) {
				setTaxonStatus(true);	
			}
			else
				if (checker.compare(this.getClass(), "Sets the taxon to be a non-exemplar", null, commandName, "nonexemplar")) {
					setTaxonStatus(false);	
				}
				else
					return  super.doCommand(commandName, arguments, checker);
			return null;
		}
		/*...............................................................................................................*/
		/** returns whether or not a cell of table is editable.*/
		public boolean isCellEditable(int row){
			return false;
		}
		/*...............................................................................................................*/
		/** for those permitting editing, indicates user has edited to incoming string.*/
		public void setString(int row, String s){
			if (taxa!=null) {
				taxa.setAssociatedObject(anr, row, s);
			}
			
		}
		public boolean useString(int ic){
			return true;
		}
		
		public String getWidestString(){
			return "88888888888888888  ";
		}
		/*.................................................................................................................*/
	    	 public String getName() {
			return "Exemplar Taxon";
	   	 }
		/*.................................................................................................................*/
	   	public boolean isPrerelease(){
	   		return true;  
	   	}
		/*.................................................................................................................*/
		/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
		 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
		 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
		public int getVersionOfFirstRelease(){
			return NEXTRELEASE;  
		}
		/*.................................................................................................................*/
		/** returns whether this module is requesting to appear as a primary choice */
	   	public boolean requestPrimaryChoice(){
	   		return true;  
	   	}
	   	 
		/*.................................................................................................................*/
	 	/** returns an explanation of what the module does.*/
	 	public String getExplanation() {
	 		return "Lists whether a taxon is to be considered as an exemplar." ;
	   	 }
	}
