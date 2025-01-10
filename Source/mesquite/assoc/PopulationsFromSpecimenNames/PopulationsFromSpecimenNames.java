/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.assoc.PopulationsFromSpecimenNames;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.assoc.lib.*;

/* ======================================================================== */
public class PopulationsFromSpecimenNames extends PopulationsAndAssociationMaker {
	NameParser nameParser;
	String nameOfPopulationTaxaBlock = "Populations";

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (nameParser==null)
			nameParser = new NameParser(this, "taxon");
		loadPreferences();
		return true;
	}
	public String getKeywords(){
		return "genes species";
	}

	public boolean isPrerelease(){
		return true;
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
	private Taxa createNewMasterTaxaBlockBasedOnNames(Taxa taxa) {
		if (taxa!=null){
			String groupName = "";
			String name="";
			Bits taxonProcessed = new Bits(taxa.getNumTaxa());
			Bits taxonInGroup = new Bits(taxa.getNumTaxa());
			if (nameParser==null)
				nameParser = new NameParser(this, "taxon");
			if (!MesquiteThread.isScripting()) {
				String helpString = "New master taxa (i.e., populations or containing taxa) will be created based upon a portion of the taxon names of the contained taxon.  In particular, the name of each contained taxon will be reduced "
						+ "by removing a piece from the start and/or end; that reduced name will become the name of the new master taxon.  If two contained taxa have the same"
						+ " reduced name, they will be assigned to the same master taxon";
				if (nameParser.queryOptions("Options for Creating Master Taxa", "Master taxon names will be extracted from taxon names.", helpString)) {
					storePreferences();
				}
				else
					return null;
			}

			StringArray masterTaxaNames = new StringArray(0);
			
			for (int it=0; it<taxa.getNumTaxa(); it++) {
				if (!taxonProcessed.isBitOn(it)) {
					groupName = nameParser.extractPart(taxa.getTaxonName(it));
					if (novelGroupName(masterTaxaNames, groupName))
						masterTaxaNames.addAndFillNextUnassigned(groupName);
					taxonProcessed.setBit(it, true);
					taxonInGroup.setBit(it,true);
					for (int ij=it+1; ij<taxa.getNumTaxa(); ij++) {
						name = nameParser.extractPart(taxa.getTaxonName(ij));
						if (groupName!=null && groupName.equalsIgnoreCase(name)) {   // would have same name as the current group, therefore set as this group
							taxonInGroup.setBit(ij,true);
							taxonProcessed.setBit(ij, true);
						}

					}
					taxonInGroup.clearAllBits();
					
				}
			}
			int numMasterTaxa = masterTaxaNames.getFilledSize();
			TaxaManager manager = (TaxaManager)findElementManager(Taxa.class);
			Taxa masterTaxa =  manager.quietMakeNewTaxa(numMasterTaxa);
			masterTaxa.setName(nameOfPopulationTaxaBlock);
			for (int it=0; it<masterTaxa.getNumTaxa(); it++) {
				masterTaxa.setTaxonName(it, masterTaxaNames.getValue(it));
			}
			return  masterTaxa;
		}	
		return null;
	}
	/*.................................................................................................................*/
	private boolean novelGroupName(StringArray names, String name) {
		if (names==null) return true;
		return !names.exists(name);
	}

	
	/*.................................................................................................................*/
   	public TaxaAssociation makePopulationsAndAssociation(Taxa specimensTaxa, ObjectContainer populationTaxaContainer) {
		Taxa populations = createNewMasterTaxaBlockBasedOnNames(specimensTaxa);
		if (populationTaxaContainer != null)
			populationTaxaContainer.setObject(populations);
		TaxaAssociation association=null;
		AssociationsManager manager = (AssociationsManager)findElementManager(TaxaAssociation.class);
		if (populations!=null)
			association = manager.makeNewAssociation(populations, specimensTaxa, "Populations-Specimens");

		if (association != null) {  // taxa is population, otherTaxa is contained/specimens
			if (nameParser==null) {
				nameParser = new NameParser(this, "taxon");
				if (!MesquiteThread.isScripting()) {
					String helpString = "Taxa in the specimens/gene copies block will be matched with populations based upon their names.  In particular, the name of each specimen/gene copy will be reduced "
							+ "by removing a piece from the start and/or end; that reduced name will be compared to the name of a population taxon.";
					nameParser.queryOptions("Options for matching associates", "Associates will be found by examining their names", helpString);
				}
			}
			boolean changed = false;
			for (int it=0; it<populations.getNumTaxa(); it++)
				for (int ito = 0; ito<specimensTaxa.getNumTaxa(); ito++){
					String name = populations.getTaxonName(it);
					String nameOther = nameParser.extractPart(specimensTaxa.getTaxonName(ito));
					if (name == null || nameOther == null)
						continue;
					boolean matches = name.equals(nameOther);
					if (matches){
						//association.zeroAllAssociations(taxa.getTaxon(it));
						association.setAssociation(populations.getTaxon(it), specimensTaxa.getTaxon(ito), true);
						changed = true;
					}
				}

			if (changed) association.notifyListeners(this, new Notification(MesquiteListener.VALUE_CHANGED)); //ZQ: maybe not needed for a newly created aassociation?? This had been here when in ManageAssociations
			return association;
		}
		return null;
   	}

	/*.................................................................................................................*/
	public String getName() {
		return "Make Populations from Specimen Names";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Specimen Name Components...";
	}

	
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Makes a new taxa block of populations based on the naming of a taxa (e.g. specimens/gene copies) in a current taxa block .";
	}
	
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}

}

