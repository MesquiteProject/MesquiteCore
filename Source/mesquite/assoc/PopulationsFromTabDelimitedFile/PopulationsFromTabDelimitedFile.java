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

import mesquite.assoc.lib.AssociationsManager;
import mesquite.assoc.lib.PopulationsAndAssociationMaker;
import mesquite.assoc.lib.TaxaAssociation;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteTabDelimitedFileProcessor;
import mesquite.lib.Notification;
import mesquite.lib.ObjectContainer;
import mesquite.lib.StringArray;
import mesquite.lib.StringUtil;
import mesquite.lib.duties.TaxaManager;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.SingleLineTextField;

/* ======================================================================== */
public class PopulationsFromTabDelimitedFile extends PopulationsAndAssociationMaker {
	String[][] sampleCodeList;
	int chosenNameCategory = 0;
	MesquiteTabDelimitedFileProcessor mesquiteTabbedFile;
	String nameOfPopulationTaxaBlock = "Populations";

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
		return false;
	}
	
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("nameOfPopulationTaxaBlock".equalsIgnoreCase(tag)){
			nameOfPopulationTaxaBlock = StringUtil.cleanXMLEscapeCharacters(content);
		}
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		StringUtil.appendXMLTag(buffer, 2, "nameOfPopulationTaxaBlock", nameOfPopulationTaxaBlock);  
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
		String helpString = "New containing taxa (e.g., populations) will be created based upon a column in a tab-delimited text file.  "
				+ "In particular, the contained taxa (e.g., specimens or gene copies) should be listed in the first column of the file, and the names of the populations in a later column.   "
				+ "There should be one row at the top that contains the titles for each column.  There can be more than one column of potential population names.  "
				+ "If two contained taxa have the same name in the later column, they will be assigned to the same population";
		
		dialog.appendToHelpString(helpString);
		
		SingleLineTextField populationBlockNameField = dialog.addTextField("Name of containing taxa block (e.g., populations):",nameOfPopulationTaxaBlock, 20);
		mesquiteTabbedFile.addTabbedFileChooser(dialog, "File with columns of contained and containing taxon names", "Column with containing taxon (e.g. population) names");
		
		dialog.completeAndShowDialog(true);
		boolean success=(buttonPressed.getValue()== dialog.defaultOK);
		if (success)  {
			if (!mesquiteTabbedFile.processTabbedFileChoiceExtensibleDialog()) {
				MesquiteMessage.discreetNotifyUser("You must enter a path to the tabbed file.");
				return false;
			} 
			sampleCodeList = mesquiteTabbedFile.getSampleCodeList();
			if (sampleCodeList==null)
				return false;
			mesquiteTabbedFile.processNameCategories();
			chosenNameCategory = mesquiteTabbedFile.getChosenNameCategory();
			nameOfPopulationTaxaBlock = populationBlockNameField.getText();
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
			TaxaManager manager = (TaxaManager)findElementManager(Taxa.class);
			Taxa masterTaxa =  manager.quietMakeNewTaxaBlock(numMasterTaxa);
			masterTaxa.setName(nameOfPopulationTaxaBlock);
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

		if (changed) association.notifyListeners(this, new Notification(MesquiteListener.VALUE_CHANGED)); //ZQ: Most likely not needed for a newly created aassociation

		//now associate populations with specimens
		//see PopulationsFromSpecimenNames for useful code
		if (populationTaxaContainer != null)
			populationTaxaContainer.setObject(populations);
		return association;

	}

	/*.................................................................................................................*/
	public String getName() {
		return "Make Containing from Contained Taxa Table";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Tab Delimited File of Contained and Containing Taxa...";
	}

	
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Makes a new taxa block of containing taxa (e.g. populations) based on a tab-delimited text file indicating how contained taxa (e.g. specimens) belong to the containing taxa.";
	}
	
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 400;  
	}

}

