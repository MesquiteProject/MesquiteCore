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

import mesquite.assoc.lib.AssociationsManager;
import mesquite.assoc.lib.PopulationsAndAssociationMaker;
import mesquite.assoc.lib.TaxaAssociation;
import mesquite.lib.Bits;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteThread;
import mesquite.lib.NameParser;
import mesquite.lib.Notification;
import mesquite.lib.ObjectContainer;
import mesquite.lib.StringArray;
import mesquite.lib.StringUtil;
import mesquite.lib.duties.TaxaManager;
import mesquite.lib.taxa.Taxa;

/* ======================================================================== */
public class PopulationsFromSpecimenNames extends PopulationsAndAssociationMaker {
	NameParser nameParser;
	String nameOfPopulationTaxaBlock = "Populations";

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		nameParser = new NameParser(this, "specimen");
		loadPreferences();
		return true;
	}
	public String getKeywords(){
		return "genes species";
	}

	public boolean isPrerelease(){
		return false;
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
			if (taxa.getNumTaxa()>=3)
				nameParser.setExamples(new String[]{taxa.getTaxonName(0), taxa.getTaxonName(taxa.getNumTaxa()/2), taxa.getTaxonName(taxa.getNumTaxa()-1)});
			else if (taxa.getNumTaxa()>0)
				nameParser.setExamples(new String[]{taxa.getTaxonName(0)});
			if (!MesquiteThread.isScripting()) {

				String helpString = "New containing taxa (i.e., populations or containing taxa) will be created based on a portion of the taxon names of the contained taxon (e.g., specimen or gene copy)." 
						+ " In particular, the name of each specimen will be reduced "
						+ "by removing a piece from the start and/or end; that reduced name will become the name of the population containing the specimen.  If two specimens have the same"
						+ " reduced name, they will be assigned to the same population";
				if (nameParser.queryOptions("Options for Creating Populations", "Population names will be derived from specimen names.", "In choosing what parts of the specimen name to use as its population name,",helpString)) {
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
			Taxa masterTaxa =  manager.quietMakeNewTaxaBlock(numMasterTaxa);
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
		if (populations == null)
			return null;
		if (populationTaxaContainer != null)
			populationTaxaContainer.setObject(populations);
		TaxaAssociation association=null;
		AssociationsManager manager = (AssociationsManager)findElementManager(TaxaAssociation.class);
		association = manager.makeNewAssociation(populations, specimensTaxa, "Populations-Specimens");

		if (association != null) {  // taxa is population, otherTaxa is contained/specimens
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
		return 400;  
	}

}

