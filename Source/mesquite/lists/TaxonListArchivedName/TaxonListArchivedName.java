/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.TaxonListArchivedName;
/*~~  */

import mesquite.lists.lib.*;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.ProbabilityModelSet;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class TaxonListArchivedName extends TaxonListAssistant {
	/*.................................................................................................................*/
	public String getName() {
		return "Alternative Names";
	}
	public String getExplanation() {
		return "Lists the alternative names for the taxon." ;
	}
	/*.................................................................................................................*/
	Taxa taxa;
	MesquiteTable table=null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}
	/*.................................................................................................................*/
	public void setTableAndTaxa(MesquiteTable table, Taxa taxa){
		if (this.taxa != null)
			this.taxa.removeListener(this);
		this.taxa = taxa;
		if (this.taxa != null)
			this.taxa.addListener(this);
		this.table = table;
		deleteAllMenuItems();
		addMenuItem("Trade Taxon Names with Alternatives", makeCommand("trade",  this));
		addMenuItem("Replace Alternatives by Taxon Names", makeCommand("copyToAlt",  this));
		addMenuItem("Replace Taxon Names by Alternatives", makeCommand("replaceByAlt",  this));
	//	addMenuItem("Replace Alternatives by List in File...", makeCommand("replaceByFileList",  this));
		addMenuItem("Replace Alternatives using Translation Table in File...", makeCommand("replaceUsingTranslationTable",  this));
		addMenuSeparator();
		addMenuItem("Store Alternatives...", makeCommand("storeCurrent",  this));
		addMenuItem("Replace Stored Alternatives...", makeCommand("replaceWithCurrent",  this));
		if (taxa !=null){
			taxa.prepareSpecsSetVector(TaxaStringsSet.class, "Alternative Names");
			addSubmenu(null, "Load Alternatives", makeCommand("loadToCurrent",  this), taxa.getSpecSetsVector(TaxaStringsSet.class));
		}
		
	}
	
	/*.................................................................................................................*/
	private Object getAlternativesFromTranslationFile (Taxa taxa) {

		MesquiteString directoryName = new MesquiteString("");
		MesquiteString fileName = new MesquiteString("");
		String filePath = MesquiteFile.openFileDialog("Choose file containing list of taxa.",  directoryName,  fileName);
		if (filePath != null) {
			MesquiteFile file =MesquiteFile.open(true, filePath);
			if (file!=null && (file.isLocal() && file.existingLength()<=0)) { 
				alert("Error: File is empty.");
				return null;
			}

			if (file.openReading()) {
				TaxaStringsSet part = (TaxaStringsSet)taxa.getCurrentSpecsSet(TaxaStringsSet.class);
				if (part == null){
					part= new TaxaStringsSet("Alternative Naming", taxa.getNumTaxa(), taxa);
					part.setTypeName("Alternative Names");
					part.addToFile(taxa.getFile(), getProject(), findElementManager(TaxaStringsSet.class));
					taxa.setCurrentSpecsSet(part, TaxaStringsSet.class);
				}
				if (part != null) {
					String line="  ";
					Parser parser = new Parser();
					parser.addToDefaultPunctuationString(",");
					int it = 0;
					StringBuffer notMatched = new StringBuffer(0);
					StringBuffer matched = new StringBuffer(0);
					while (line!=null) {
						line = file.readLine();
						if (StringUtil.notEmpty(line)){
							parser.setString(line);
							String name = parser.getFirstItem(line, "\t");
							if (StringUtil.notEmpty(name)) {
								it = taxa.whichTaxonNumber(name, false, false);
								if (it>=0 && it<taxa.getNumTaxa()) {
									name = parser.getItem(line, "\t",2);
									if (StringUtil.notEmpty(name)) {
										part.setProperty(name, it);
										matched.append("\t"+name+ "\n");
								//		taxa.setSelected(it, true);
									} else
										notMatched.append("\t"+name+ "\n");
										
								} else {
									notMatched.append("\t"+name+ "\n");
								}
							}
						}
					}
					if (notMatched.length()>0){
						logln("\nNames not matched\n");
						logln(notMatched.toString());
					}
					if (matched.length()>0){
						logln("\nNames matched\n");
						logln(matched.toString());
					}
					return part;
				}
			}
		}
		return null;

	}
	/*.................................................................................................................*/
	private Object getAlternativesFromFile (int numTaxa) {

		MesquiteString directoryName = new MesquiteString("");
		MesquiteString fileName = new MesquiteString("");
		String filePath = MesquiteFile.openFileDialog("Choose file containing list of taxa.",  directoryName,  fileName);
		if (filePath != null) {
			MesquiteFile file =MesquiteFile.open(true, filePath);
			if (file!=null && (file.isLocal() && file.existingLength()<=0)) { 
				alert("Error: File is empty.");
				return null;
			}

			if (file.openReading()) {
				TaxaStringsSet part = (TaxaStringsSet)taxa.getCurrentSpecsSet(TaxaStringsSet.class);
				if (part == null){
					part= new TaxaStringsSet("Alternative Naming", taxa.getNumTaxa(), taxa);
					part.setTypeName("Alternative Names");
					part.addToFile(taxa.getFile(), getProject(), findElementManager(TaxaStringsSet.class));
					taxa.setCurrentSpecsSet(part, TaxaStringsSet.class);
				}
				if (part != null) {
					String line="  ";
					int it = 0;
					while (line!=null && it<numTaxa) {
						line = file.readLine();
						if (StringUtil.notEmpty(line)){
							part.setProperty(line, it);
							it++;
						}
					}
					return part;
				}
			}
		}
		return null;

	}

	
	MesquiteInteger pos = new MesquiteInteger(0);
	
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Copies the current names to the alternatives", null, commandName, "copyToAlt")) {
			if (taxa !=null) {
				TaxaStringsSet part = (TaxaStringsSet)taxa.getCurrentSpecsSet(TaxaStringsSet.class);
				if (part == null){
					//create current specs set
					part= new TaxaStringsSet("Alternative Naming", taxa.getNumTaxa(), taxa);
					part.setTypeName("Alternative Names");
					part.addToFile(taxa.getFile(), getProject(), findElementManager(TaxaStringsSet.class));
					taxa.setCurrentSpecsSet(part, TaxaStringsSet.class);
				}
				for (int it = 0; it< taxa.getNumTaxa(); it++){
					if (!taxa.anySelected() || taxa.getSelected(it))
						part.setProperty(taxa.getTaxonName(it), it);
				}
				SpecsSetVector ssv = taxa.getSpecSetsVector(TaxaStringsSet.class);
				if (ssv != null)
					ssv.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Replaces the current names by the alternatives", null, commandName, "replaceByAlt")) {
			if (taxa !=null) {
				TaxaStringsSet part = (TaxaStringsSet)taxa.getCurrentSpecsSet(TaxaStringsSet.class);
				if (part == null){
					discreetAlert("You need to load or make an alternative naming scheme before trading it with the current taxon names.  You can enter alternative names in the \"Alternative Names\" column, "
							+ "or you can Replace Alternatives by Taxon Names and then edit the alternatives");
					return null;
				}
				for (int it = 0; it< taxa.getNumTaxa(); it++) {
					if (!taxa.anySelected() || taxa.getSelected(it)){
					String alt = (String)part.getProperty(it);
					if (!StringUtil.blank(alt))
						taxa.setTaxonName(it, alt, false);
					}
				}
				taxa.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED));  
			}
		}
		else if (checker.compare(this.getClass(), "Replaces the alternatives with those in a list read in from a file", null, commandName, "replaceByFileList")) {
			if (taxa !=null && !MesquiteThread.isScripting()) {
					Object obj = getAlternativesFromFile(taxa.getNumTaxa());
					if (obj!=null)
						taxa.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED)); //TODO: bogus! should notify via specs not data???			
				}
		}
		else if (checker.compare(this.getClass(), "Replaces the alternatives with those using a translation table read in from a file", null, commandName, "replaceUsingTranslationTable")) {
			if (taxa !=null && !MesquiteThread.isScripting()) {
					Object obj = getAlternativesFromTranslationFile(taxa);
					if (obj!=null)
						taxa.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED)); //TODO: bogus! should notify via specs not data???			
				}
		}
		else if (checker.compare(this.getClass(), "Trades the current names with the alternatives", null, commandName, "trade")) {
			if (taxa !=null) {
				TaxaStringsSet part = (TaxaStringsSet)taxa.getCurrentSpecsSet(TaxaStringsSet.class);
				if (part == null){
					discreetAlert("You need to load or make an alternative naming scheme before trading it with the current taxon names.  You can enter alternative names in the \"Alternative Names\" column, "
							+ "or you can Replace Alternatives by Taxon Names and then edit the alternatives");
					return null;
				}
				for (int it = 0; it< taxa.getNumTaxa(); it++) {
					if (!taxa.anySelected() || taxa.getSelected(it)){
					String alt = (String)part.getProperty(it);
					part.setProperty(taxa.getTaxonName(it), it);
					if (!StringUtil.blank(alt))
						taxa.setTaxonName(it, alt, false);
					}
					
				}
				taxa.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED));  
				SpecsSetVector ssv = taxa.getSpecSetsVector(TaxaStringsSet.class);
				if (ssv != null)
					ssv.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Stores the current taxa naming as a ALTTAXNAMES", null, commandName, "storeCurrent")) {
			if (taxa!=null){
				SpecsSetVector ssv = taxa.getSpecSetsVector(TaxaStringsSet.class);
				if (ssv == null || ssv.getCurrentSpecsSet() == null) {
					TaxaStringsSet partition= new TaxaStringsSet("Alternative Naming", taxa.getNumTaxa(), taxa);
					partition.setTypeName("Alternative Names");
					partition.addToFile(taxa.getFile(), getProject(), findElementManager(TaxaStringsSet.class));
					taxa.setCurrentSpecsSet(partition, TaxaStringsSet.class);
					ssv = taxa.getSpecSetsVector(TaxaStringsSet.class);
				}
				if (ssv!=null) {
					SpecsSet s = ssv.storeCurrentSpecsSet();
					if (s.getFile() == null)
						s.addToFile(taxa.getFile(), getProject(), findElementManager(TaxaStringsSet.class));
					s.setName(ssv.getUniqueName("Alternative Naming"));
					String name = MesquiteString.queryString(containerOfModule(), "Name", "Name for alternative naming scheme to be stored", s.getName());
					if (!StringUtil.blank(name))
						s.setName(name);
					ssv.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  
				}
				else MesquiteMessage.warnProgrammer("sorry, can't store because no specssetvector");
			}
			//return ((ListWindow)getModuleWindow()).getCurrentObject();
		}
		else if (checker.compare(this.getClass(), "Replaces a stored taxa naming by the current one", null, commandName, "replaceWithCurrent")) {
			if (taxa!=null){
				SpecsSetVector ssv = taxa.getSpecSetsVector(TaxaStringsSet.class);
				if (ssv!=null) {
					SpecsSet chosen = (SpecsSet)ListDialog.queryList(containerOfModule(), "Replace stored naming", "Choose alternative naming scheme to replace by current alternative", MesquiteString.helpString,ssv, 0);
					if (chosen!=null){
						SpecsSet current = ssv.getCurrentSpecsSet();
						ssv.replaceStoredSpecsSet(chosen, current);
					}
				}

			}
			//return ((ListWindow)getModuleWindow()).getCurrentObject();
		}
		else if (checker.compare(this.getClass(), "Loads the stored taxa naming scheme to be the current alternative", "[number of naming scheme to load]", commandName, "loadToCurrent")) {
			if (taxa !=null) {
				int which = MesquiteInteger.fromFirstToken(arguments, stringPos);
				if (MesquiteInteger.isCombinable(which)){
					SpecsSetVector ssv = taxa.getSpecSetsVector(TaxaStringsSet.class);
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
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	public void changed(Object caller, Object obj, Notification notification){
		outputInvalid();
		parametersChanged(notification);
	}
	public String getTitle() {
		return "Alternative Names";
	}
	/*...............................................................................................................*/
	/** returns whether or not a cell of table is editable.*/
	public boolean isCellEditable(int row){
		return true;
	}
	/*...............................................................................................................*/
	/** for those permitting editing, indicates user has edited to incoming string.*/
	public void setString(int row, String s){
		if (taxa!=null) {
			TaxaStringsSet part = (TaxaStringsSet)taxa.getCurrentSpecsSet(TaxaStringsSet.class);
			if (part == null){
//				create current specs set
				part= new TaxaStringsSet("Alternative Naming", taxa.getNumTaxa(), taxa);
				part.setTypeName("Alternative Names");
				part.addToFile(taxa.getFile(), getProject(), findElementManager(TaxaStringsSet.class));
				taxa.setCurrentSpecsSet(part, TaxaStringsSet.class);
				}
			if (part != null)
			part.setProperty(s, row);
		}
	}
	public String getStringForTaxon(int ic){
		if (taxa!=null) {
			TaxaStringsSet part = (TaxaStringsSet)taxa.getCurrentSpecsSet(TaxaStringsSet.class);
			if (part==null)
				return "-";
			String tg = (String)part.getProperty(ic);
			if (tg==null)
				return "-";
			return tg;
		}
		return "?";
	}
	public boolean useString(int ic){
		return true;
	}

	public String getWidestString(){
		return "88888888888888888  ";
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

}

