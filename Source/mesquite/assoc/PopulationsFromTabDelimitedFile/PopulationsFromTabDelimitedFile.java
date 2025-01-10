/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.assoc.PopulationsFromTabDelimitedFile;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.SingleLineTextField;
import mesquite.assoc.lib.*;

/* ======================================================================== */
public class PopulationsFromTabDelimitedFile extends PopulationsAndAssociationMaker {
	String[][] sampleCodeList;
	int chosenNameCategory = 0;
	MesquiteTabDelimitedFileProcessor mesquiteTabbedFile;
	String nameOfPopulationTaxonBlock = "Populations";

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		mesquiteTabbedFile = new MesquiteTabDelimitedFileProcessor();
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
	public void processSingleXMLPreference (String tag, String content) {
		if ("nameOfPopulationTaxonBlock".equalsIgnoreCase(tag)){
			nameOfPopulationTaxonBlock = StringUtil.cleanXMLEscapeCharacters(content);
		}
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		StringUtil.appendXMLTag(buffer, 2, "nameOfPopulationTaxonBlock", nameOfPopulationTaxonBlock);  
		return buffer.toString();
	}

	/*.................................................................................................................*/
	public boolean queryForOptionsAsNeeded() {
		if (StringUtil.blank(mesquiteTabbedFile.getSampleCodeListPath()))
			return queryOptions();
		return mesquiteTabbedFile.processNameCategories();
	}
	/*.................................................................................................................*/
	/** A stub method for querying the user about options. If alterIndividualTaxonNames is used to 
   	alter the names, and options need to be specified for the operation, then optionsQuery should be overridden.*/
	public  boolean getOptions(Taxa taxa, int firstSelected){
		mesquiteTabbedFile.scanTabbedDocument();  // do this just in case there is already one there
		if (!queryOptions())
			return false;
		if (!mesquiteTabbedFile.scanTabbedDocument())
			return false;
		return true;
	}
	/*.................................................................................................................*/
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Create containing taxa based on list in file",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		String helpString = "New master taxa (e.g., populations or containing taxa) will be created based upon a column in a tab-delimited text file.  "
				+ "In particular, the contained taxa (e.g., specimens or gene copies) should be listed in the first column of the file, and the names of the master taxa in a later column.   "
				+ "There should be one row at the top that contains the titles for each column.  There can be more than one column of potential master taxa names.  "
				+ "If two contained taxa have the same name in the later column, they will be assigned to the same master taxon";
		
		dialog.appendToHelpString(helpString);

		SingleLineTextField populationBlockNameField = dialog.addTextField("Name of population/containing taxa block:",nameOfPopulationTaxonBlock, 20);
		mesquiteTabbedFile.addTabbedFileChooser(dialog, "File containing columns of specimen and population names", "Column containing population names");

		dialog.completeAndShowDialog(true);
		boolean success=(buttonPressed.getValue()== dialog.defaultOK);
		if (success)  {
			mesquiteTabbedFile.processTabbedFileChoiceExtensibleDialog();
			mesquiteTabbedFile.processNameCategories();
			sampleCodeList = mesquiteTabbedFile.getSampleCodeList();
			chosenNameCategory = mesquiteTabbedFile.getChosenNameCategory();
			nameOfPopulationTaxonBlock = populationBlockNameField.getText();
		}
		storePreferences();  // do this here even if Cancel pressed as the File Locations subdialog box might have been used
		dialog.dispose();
		return success;
	}	/*.................................................................................................................*/
	
	/*.................................................................................................................*/
	public  String getContainingNameFromTabDelimitedFile(MesquiteString sampleCode, String taxonName) {
		queryForOptionsAsNeeded();
		return mesquiteTabbedFile.getNameFromTabDelimitedFile(sampleCode, taxonName);
	}

	/*.................................................................................................................*/
	private Taxa createNewMasterTaxaBlock(Taxa taxa, String[] taxonNames) {
		if (taxonNames!=null){
			int numMasterTaxa = taxonNames.length;
			Taxa masterTaxa =  taxa.createTaxonBlock(numMasterTaxa);
			masterTaxa.setName(nameOfPopulationTaxonBlock);
			for (int it=0; it<masterTaxa.getNumTaxa(); it++) {
				masterTaxa.setTaxonName(it, taxonNames[it]);
			}
			return  masterTaxa;
		}	
		return null;
	}
	/*.................................................................................................................*/
	public TaxaAssociation makePopulationsAndAssociation(Taxa specimensTaxa, ObjectContainer populationTaxaContainer) {
		//find population names in table and make taxa block
		if (!queryOptions())
			return null;
		if (sampleCodeList==null)
			return null;
		String[] populationNames = mesquiteTabbedFile.getCondensedSecondaryColumn();
		Taxa populations = createNewMasterTaxaBlock(specimensTaxa, populationNames);
		if (populations == null)
			return null;
		
		AssociationsManager manager = (AssociationsManager)findElementManager(TaxaAssociation.class);
		TaxaAssociation	association = manager.makeNewAssociation(populations, specimensTaxa, "Populations-Specimens");


		boolean changed = false;
		for (int ito = 0; ito<specimensTaxa.getNumTaxa(); ito++){
			String specimenName = specimensTaxa.getTaxonName(ito);
			
			int row = StringArray.indexOfIgnoreCaseInSecondArray(sampleCodeList, 0, specimenName, true);
			if (row>=0) { //we've found it
				String populationName = sampleCodeList[row][chosenNameCategory];
				int populationNumber = StringArray.indexOfIgnoreCase(populationNames, populationName);
				association.setAssociation(populations.getTaxon(populationNumber), specimensTaxa.getTaxon(ito), true);
				changed = true;
			}

		}

		if (changed) association.notifyListeners(this, new Notification(MesquiteListener.VALUE_CHANGED)); //ZQ: maybe not needed for a newly created aassociation

		//now associate populations with specimens
		//see PopulationsFromSpecimenNames for useful code
		if (populationTaxaContainer != null)
			populationTaxaContainer.setObject(populations);
		return association;

	}

	/*.................................................................................................................*/
	public String getName() {
		return "Make Populations from Specimens Table";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Specimens – Populations Tab Delimited File...";
	}

	
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Makes a new taxa block of populations based on a tab-delimited text file containing a table of a specimens and populations.";
	}
	
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}

}

