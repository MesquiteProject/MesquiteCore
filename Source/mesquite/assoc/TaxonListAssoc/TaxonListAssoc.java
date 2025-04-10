/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
 Perhaps with your help we can be more than a few, and make Mesquite better.

 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
 Mesquite's web site is http://mesquiteproject.org

 This source code and its compiled class files are free and modifiable under the terms of 
 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.assoc.TaxonListAssoc;
/*~~  */

import mesquite.lists.lib.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import mesquite.lib.*;
import mesquite.assoc.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.Taxon;
import mesquite.lib.ui.AlertDialog;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.MQTextArea;
import mesquite.lib.ui.MesquiteImage;
import mesquite.lib.ui.MesquiteMenuItemSpec;
import mesquite.lib.ui.MesquiteTool;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lib.ui.MousePanel;

/* ======================================================================== */
public class TaxonListAssoc extends TaxonListAssistant {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(AssociationSource.class, "The current taxon association is displayed and can be edited in the List of Taxa window.",
				"The source of an association is chosen initially.");
	}
	public String getName() {
		return "Associated Taxa";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Lists and edits what other taxa (e.g. contained or containing) are associated with these." ;
	}
	public String getKeywords(){
		return "gene_trees species genes";
	}
	/*.................................................................................................................*/
	Taxa taxa, otherTaxa;
	MesquiteTable table=null;
	AssociationSource associationTask;
	MesquiteWindow containingWindow;
	TaxaAssociation association;
	AssocEditor panel;

	boolean ignoreWhitespace=true;
	boolean ignoreCase = true;
	boolean matchNumbers=false;
	int minDigitsToMatch = 3;
	NameParser nameParser;
	MesquiteBoolean editorShown = new MesquiteBoolean(false);

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		associationTask = (AssociationSource)hireNamedEmployee(AssociationSource.class, "#mesquite.assoc.StoredAssociations.StoredAssociations");
		if (associationTask == null)
			return sorry(getName() + " couldn't start because no source of taxon associations obtained.");
		MesquiteWindow f = containerOfModule();
		if (f instanceof MesquiteWindow){
			containingWindow = (MesquiteWindow)f;
		}
		if (nameParser==null)
			nameParser = new NameParser(this, "specimen/taxon");
		loadPreferences();
		return true;
	}

	/*.................................................................................................................*/
	public void setTableAndTaxa(MesquiteTable table, Taxa taxa){
		deleteAllMenuItems();
		addCheckMenuItem(null, "Show Editor", makeCommand("showEditor", this), editorShown);
		addMenuItem(null, "View from Other Taxa's Perspective", makeCommand("otherPerspective", this));
		addMenuItem(null, "-", null);
		addMenuItem(null, "Assign Associates to Selected Taxa...", makeCommand("setAssociates", this));
		addMenuItem(null, "Add Associates to Selected Taxa...", makeCommand("addAssociates", this));
		addMenuItem(null, "Remove Associates", makeCommand("removeAssociates", this));
		//addMenuItem(null, "?Auto-assign Matches...", makeCommand("autoAssignExact", this)); 
		addMenuItem(null, "Assign Associates by Name Matching...", makeCommand("calculateAssociation", this));
		addMenuItem(null, "Trade Status Contained-Containing", makeCommand("tradeStatus", this));
		addMenuItem(null, "-", null);
		addMenuItem(null, "New Association...", makeCommand("newAssociation", this));
		addMenuItem(null, "Duplicate Association", makeCommand("duplicateAssociation", this));
		addMenuItem(null, "-", null);
		addMenuItem(null, "Create New Associated Taxon...", makeCommand("createAssociate", this));
	//	addMenuItem(null, "?Create New Taxa in Containing Block from Selected", makeCommand("createNewTaxaFromSelected", this)); 
		addMenuItem(null, "Delete Associated Taxa...", makeCommand("deleteAssociateTaxa", this));
		addMenuItem(null, "-", null);
		if (this.taxa != null)
			taxa.removeListener(this);
		this.taxa = taxa;
		if (this.taxa != null)
			taxa.addListener(this);
		this.table = table;
		if (!MesquiteThread.isScripting())
			resetAssociation(false);
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


	public MesquiteWindow getContainingWindow(){
		return containingWindow;
	}
	public boolean isPrerelease(){
		return false;
	}
	public boolean canHireMoreThanOnce(){
		return true;
	}
	/*.................................................................................................................*/
	public MesquiteWindow getParentWindow(){
		return containingWindow;
	}
	/*.................................................................................................................*
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Auto-assign Options",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()

		Checkbox ignoreWhiteCheckBox= dialog.addCheckBox("ignore whitespace (spaces, tabs, etc.)", ignoreWhitespace);
		Checkbox ignoreCaseCheckBox= dialog.addCheckBox("ignore case", ignoreCase);

		Checkbox matchNumberCheckBox= dialog.addCheckBox("match if a number in both names is the same", matchNumbers);
		IntegerField matchDigitsField = dialog.addIntegerField("minimum number of digits in number:", minDigitsToMatch, 5, 1,50);

		//		SingleLineTextField clustalOptionsField = queryFilesDialog.addTextField("Clustal options:", clustalOptions, 26, true);

		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			ignoreCase = ignoreCaseCheckBox.getState();
			ignoreWhitespace = ignoreWhiteCheckBox.getState();
			matchNumbers = matchNumberCheckBox.getState();
			minDigitsToMatch = matchDigitsField.getValue();
			//storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	public String numberSubString(String s, int minDigits){
		StringBuffer sb = new StringBuffer();
		boolean longEnough = false;
		for (int i=0; i<s.length(); i++) {
			if (Character.isDigit(s.charAt(i))){
				sb.append(s.charAt(i));
				if (sb.length()>=minDigits)
					longEnough=true;
			}
			else if (!longEnough) {
				sb.setLength(0);
			} else
				break;
		}
		if (longEnough)
			return sb.toString();
		return "";
	}
	/*.................................................................................................................*/
	public boolean containsSameNumbers(String name1, String name2, int minDigits){
		String s1 = numberSubString(name1,minDigits);
		String s2 =  numberSubString(name2,minDigits);
		if (!StringUtil.blank(s1) && !StringUtil.blank(s2))
			return s1.equals(s2);
		return false;
	}
	/*.................................................................................................................*/
	private void autoAssign(boolean ignoreWhitespace, boolean ignoreCase){
		boolean changed = false;
		if (taxa!=null && association != null) {
			Taxa otherTaxa = association.getOtherTaxa(taxa);
			for (int it=0; it<taxa.getNumTaxa(); it++)
				for (int ito = 0; ito<otherTaxa.getNumTaxa(); ito++){
					String name = taxa.getTaxonName(it);
					String nameOther = otherTaxa.getTaxonName(ito);
					if (name == null || nameOther == null)
						continue;
					boolean matches = name.equals(nameOther);
					if (!matches && ignoreCase)
						matches = name.equalsIgnoreCase(nameOther);
					if (!matches && ignoreWhitespace) {
						String strippedName = StringUtil.removeCharacters(name, StringUtil.defaultWhitespace);
						String strippedNameOther = StringUtil.removeCharacters(nameOther, StringUtil.defaultWhitespace);
						matches = strippedName.equals(strippedNameOther);
						if (!matches && ignoreCase) {
							matches = strippedName.equalsIgnoreCase(strippedNameOther);
						}
					}
					if (!matches && matchNumbers) {
						matches = containsSameNumbers(name, nameOther,minDigitsToMatch);
					}
					if (matches){
						//association.zeroAllAssociations(taxa.getTaxon(it));
						association.setAssociation(taxa.getTaxon(it), otherTaxa.getTaxon(ito), true);
						changed = true;
					}
				}

			if (changed) association.notifyListeners(this, new Notification(MesquiteListener.VALUE_CHANGED));
		}
	}
	/*.................................................................................................................*/
	private void calculateAssociation(){
		boolean changed = false;
		if (association == null) {
			association = newAssociation();
			if (association == null)
				return;
		}
		if (taxa!=null && association != null) {  // taxa is population, otherTaxa is contained/specimens
			Taxa populations = association.getContainingTaxa();
			Taxa specimens = association.getContainedTaxa();
			if (specimens != taxa && populations != taxa){
				discreetAlert("Can't calculate the association because current association does not refer to the taxa in this list");
				return;
			}
			nameParser.setTrimVerb("attempts to match containing taxon");
			if (specimens.getNumTaxa()>=3)
				nameParser.setExamples(new String[]{specimens.getTaxonName(0), specimens.getTaxonName(specimens.getNumTaxa()/2), specimens.getTaxonName(specimens.getNumTaxa()-1)});
			else if (specimens.getNumTaxa()>0)
				nameParser.setExamples(new String[]{specimens.getTaxonName(0)});
			if (!MesquiteThread.isScripting()) {
				String helpString = "This tool requires that the names of the containing taxa (e.g., populations) are formed as reduced versions of the taxon names of the "
						+ "other block of taxa (e.g., specimens).  In particular, the names of the populations must exactly match a portion of specimen names of the other block.  "
						+ "This tool finds the match by reducing the specimen names by including or excluding pieces according to the criteria you specify, and, if that reduced name"
						+ "matches the name of a population, then the specimen is associated with that population.";
				boolean ok = nameParser.queryOptions("Options for matching specimens to populations", "Populations will be matched to specimens by examining their names", "In choosing what parts of the specimen name to compare to the population names,", helpString);
				if (!ok)
					return;
				storePreferences();
			}
			for (int itPop=0; itPop<populations.getNumTaxa(); itPop++)
				for (int itSpecimen = 0; itSpecimen<specimens.getNumTaxa(); itSpecimen++){
					String populationName = populations.getTaxonName(itPop);
					String specimenName = nameParser.extractPart(specimens.getTaxonName(itSpecimen));
					if (populationName == null || specimenName == null)
						continue;
					boolean matches = populationName.equals(specimenName);
					if (matches){
						//association.zeroAllAssociations(taxa.getTaxon(it));
						association.setAssociation(populations.getTaxon(itPop), specimens.getTaxon(itSpecimen), true);
						changed = true;
					}
				}

			if (changed) association.notifyListeners(this, new Notification(MesquiteListener.VALUE_CHANGED));
		}
	}
	/*.................................................................................................................*/
	private void deleteAssociatedTaxa(){
		boolean askedOK = false;
		if (table !=null && taxa!=null && association != null) {
			boolean changed=false;
			Taxa otherTaxa = null;
			if (employer!=null && employer instanceof ListModule) {
				int c = ((ListModule)employer).getMyColumn(this);
				for (int i=0; i<taxa.getNumTaxa(); i++) {
					if (table.isCellSelectedAnyWay(c, i)) {
						Taxon t = taxa.getTaxon(i);
						Taxon[] associates = association.getAssociates(t);
						if (associates != null)
							for (int k = 0; k<associates.length; k++) {
								if (!askedOK && !AlertDialog.query(containerOfModule(), "Delete associated taxa?", "Are you sure you want to delete the associated taxa, "
										+"not only from the association but also from their taxa block and from the file? You cannot undo this."
										+"\nIf you want to remove them only from the association, use Remove Associates instead."))
									return;
								else
									askedOK = true;
								otherTaxa = associates[k].getTaxa();
								otherTaxa.deleteTaxon( associates[k], false);
								changed = true;
							}
					}
				}
			}

			if (changed) {
				otherTaxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_DELETED));
				association.notifyListeners(this, new Notification(MesquiteListener.UNKNOWN));  
				parametersChanged();
			}
		}
	}

	//QQ: change tomultiple taxa selected
	//QQ: trade status
	/*private void chooseAndSetAssociate(boolean append){
		if (numberSelected() ==0){
			discreetAlert("To assign or add an associate, rows (taxa) in this table need to be selected.");
			return;
		}				
		if (table !=null && taxa!=null && association != null) {
			Taxa otherTaxa = association.getOtherTaxa(taxa);
			Taxon taxon = otherTaxa.userChooseTaxon(containerOfModule(), "Select the taxon to be associated with the selected rows");
			if (taxon == null)
				return;
			setAssociateOfSelected(taxon, append);
		}
	}*/

	private void chooseAndSetAssociates(boolean append){
		if (numberSelected() ==0){
			discreetAlert("To assign or add an associate, rows (taxa) in this table need to be selected.");
			return;
		}				

		if (table !=null && taxa!=null && association != null) {
			Taxa otherTaxa = association.getOtherTaxa(taxa);
			Taxon[] taxons = otherTaxa.userChooseTaxa(containerOfModule(), "Select the associated taxa to be associated with the selected taxa");
			if (taxons == null)
				return;
			setAssociatesOfSelected(taxons, append);
		}
	}

	//if associate passed is null, interprets as signal to remove all associates from selected
	private void addAssociateOfSelected(Taxon associate){
		if (table !=null && taxa!=null && association != null) {
			boolean changed=false;

			if (employer!=null && employer instanceof ListModule) {
				int c = ((ListModule)employer).getMyColumn(this);
				for (int i=0; i<taxa.getNumTaxa(); i++) {
					if (table.isCellSelectedAnyWay(c, i)) {
						Taxon t = taxa.getTaxon(i);
						association.setAssociation(t, associate, true); 
						changed = true;
					}
				}
			}
			if (changed) {
				association.notifyListeners(this, new Notification(MesquiteListener.UNKNOWN));  
				parametersChanged();
			}
		}
	}
	/*	*/

	int numberSelected(){
		if (employer!=null && employer instanceof ListModule) {
			int c = ((ListModule)employer).getMyColumn(this);
			int countSelected = 0;
			for (int i=0; i<taxa.getNumTaxa(); i++) //check to see how many selected
				if (table.isCellSelectedAnyWay(c, i))
					countSelected++;
			return countSelected;
		}
		return 0;
	}
	//if associate passed is null, interprets as signal to remove all associates from selected
	private void setAssociatesOfSelected(Taxon[] associates, boolean append){
		if (table !=null && taxa!=null && association != null) {
			boolean changed=false;
			boolean modifyRegardless = (numberSelected()==0 && associates == null); //if the associate is null and none are selected, then remove all
			if (employer!=null && employer instanceof ListModule) {
				int c = ((ListModule)employer).getMyColumn(this);
				for (int i=0; i<taxa.getNumTaxa(); i++) {
					if (modifyRegardless || table.isCellSelectedAnyWay(c, i)) {
						Taxon t = taxa.getTaxon(i);
						if (associates == null)
							association.zeroAllAssociations(t);
						else {
							if (!append)
								association.zeroAllAssociations(t);
							for (int k = 0; k<associates.length; k++)
								association.setAssociation(t, associates[k], true); 
						}

						changed = true;
					}
				}
			}

			if (changed) {
				association.notifyListeners(this, new Notification(MesquiteListener.UNKNOWN));  
				parametersChanged();
			}
		}
	}
	/*if associate passed is null, interprets as signal to remove all associates from selected *
	private void setAssociatesOfSelected(Taxon[] associates, boolean append){
		if (table !=null && taxa!=null && association != null && associates != null && associates.length>0) {
			boolean changed=false;
			boolean modifyRegardless = (numberSelected()==0 && associates == null); //if the associate is null and none are selected, then remove all
		if (employer!=null && employer instanceof ListModule) {
				int c = ((ListModule)employer).getMyColumn(this);
				for (int i=0; i<taxa.getNumTaxa(); i++) {
					if (table.isCellSelectedAnyWay(c, i)) {
						Taxon t = taxa.getTaxon(i);
						if (!append)
							association.zeroAllAssociations(t);
						for (int k = 0; k<associates.length; k++)
							association.setAssociation(t, associates[k], true); 

						changed = true;
					}
				}
			}

			if (changed) {
				association.notifyListeners(this, new Notification(MesquiteListener.UNKNOWN));  
				parametersChanged();
			}
		}
	}
/*	 */
	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("getAssociationsTask " + associationTask); 
		temp.addLine("resetAssociation"); 
		temp.addLine("showEditor " + editorShown.toOffOnString());
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Gets the current associations module", null, commandName, "getAssociationsTask")) {
			return associationTask;
		}
		else  if (checker.compare(this.getClass(), "Resets the association", null, commandName, "resetAssociation")) {
			resetAssociation(false);
		}
		else  if (checker.compare(this.getClass(), "Resets the association", null, commandName, "renameAssociation")) {
			String name = parser.getFirstToken(arguments);
			if(name == null)
				return null;
			association.setName(name);
			association.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED));
			resetAllMenuBars();
		}
		else  if (checker.compare(this.getClass(), "Calculates associates based upon an algorithm", null, commandName, "calculateAssociation")) {
			calculateAssociation();
		}
		else  if (checker.compare(this.getClass(), "Trade status as to whether contained or containing", null, commandName, "tradeStatus")) {
			if (association !=null){
				association.transposeAssociation();
				association.notifyListeners(this, new Notification(MesquiteListener.VALUE_CHANGED));
				resetAssociation(true);
			}
		}
		else  if (checker.compare(this.getClass(), "Open from perspective of other taxa", null, commandName, "otherPerspective")) {
			AssociationsManager manager = (AssociationsManager)findElementManager(TaxaAssociation.class);
			if (association !=null && taxa != null){
				Taxa otherTaxa = association.getOtherTaxa(taxa);
				manager.showAssociationInTaxonList(otherTaxa,  association, false);
			}

		}
		else  if (checker.compare(this.getClass(), "Duplicate the current association", null, commandName, "duplicateAssociation")) {
			if (association == null)
				return null;

			AssociationsManager manager = (AssociationsManager)findElementManager(TaxaAssociation.class);
			if (manager != null)
				return manager.duplicateAssociation(association);
		}
		else  if (checker.compare(this.getClass(), "Asks the association manager to make a new association", null, commandName, "newAssociation")) {
			return newAssociation();
		}
	/*	else  if (checker.compare(this.getClass(), "Automatically sets associates if there is an exact match of names", null, commandName, "autoAssignExact")) {
			if (queryOptions())
				autoAssign(ignoreWhitespace, ignoreCase);
		}
		*/
		/*else  if (checker.compare(this.getClass(), "Sets which other taxon is associated with these; replaces existing", null, commandName, "setAssociate")) {
			chooseAndSetAssociate(false);
		}
		else if (checker.compare(this.getClass(), "Sets which other taxon is associated with these; adds to existing", null, commandName, "addAssociate")) {
			chooseAndSetAssociate(true);
		}*/
		else  if (checker.compare(this.getClass(), "Sets which other taxa are associated with single selected taxon; replaces existing", null, commandName, "setAssociates")) {
			chooseAndSetAssociates(false);
		}
		else if (checker.compare(this.getClass(), "Sets which other taxa are associated with single selected taxon; adds to existing", null, commandName, "addAssociates")) {
			chooseAndSetAssociates(true);
		}
		/*
		 * else  if (checker.compare(this.getClass(), "Creates new taxa", null, commandName, "createNewTaxaFromSelected")) {
			if (association == null)
				return null;
			Taxa otherTaxa = association.getOtherTaxa(taxa);
			boolean added = false;
			boolean[] selectedInList = panel.getSelectedInList();
			if (selectedInList==null)
				return null;
			for (int it=0; it<otherTaxa.getNumTaxa(); it++) {
				if (selectedInList[it]) {
					taxa.addTaxa(taxa.getNumTaxa()-1, 1, false);
					taxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));
					Taxon otherT = otherTaxa.getTaxon(it);
					Taxon t = taxa.getTaxon(taxa.getNumTaxa()-1);
					t.setName(otherT.getName());
					added = true;
					association.setAssociation(t, otherT, true);
				}
			}
			if (added) { 
				//taxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));
				association.notifyListeners(this, new Notification(MesquiteListener.UNKNOWN));  
				parametersChanged();
			}
		}
		*/
		else if (checker.compare(this.getClass(), "Creates a new taxon and adds to existing", null, commandName, "createAssociate")) {
			if (association == null)
				return null;
			if (numberSelected() ==0){
				discreetAlert("To create a new associate, rows (taxa) in this table need to be selected to which to assign the new associate.");
				return null;
			}				
			String n = MesquiteString.queryString(containerOfModule(), "Name of Taxon", "Name the new taxon", "Taxon");
			if (StringUtil.blank(n))
				return null;
			Taxa otherTaxa = association.getOtherTaxa(taxa);
			otherTaxa.addTaxa(otherTaxa.getNumTaxa()-1, 1, false);
			Taxon t = otherTaxa.getTaxon(otherTaxa.getNumTaxa()-1);
			t.setName(n);
			otherTaxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));
			addAssociateOfSelected(t);
		}
		else if (checker.compare(this.getClass(), "Deletes associated taxa; ask user first!", null, commandName, "deleteAssociateTaxa")) {
			deleteAssociatedTaxa();
		}
		else if (checker.compare(this.getClass(), "Toggles show editor", "[true or false]", commandName, "showEditor")) {
			editorShown.toggleValue(parser.getFirstToken(arguments));
			showPanel(editorShown.getValue());
		}
		else if (checker.compare(this.getClass(), "Removes associates from association", null, commandName, "removeAssociates")) {
			setAssociatesOfSelected(null, false);
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	TaxaAssociation newAssociation(){
		AssociationsManager manager = (AssociationsManager)findElementManager(TaxaAssociation.class);
		if (manager != null){
			boolean containing = AlertDialog.query(containerOfModule(), "Contained or containing?", "Should the block of taxa in this window be considered as containing the other taxa (e.g., as species to specimens), "
					+"or as being contained by them?", "Containing", "Contained");
			if (containing)
				return manager.makeNewAssociation(taxa, null);
			else
				return manager.makeNewAssociation(null, taxa);

		}
		return null;
	}
	public boolean isShowing(TaxaAssociation assoc){
		return assoc == association;
	}

	/*.................................................................................................................*/
	void resetAssociation(boolean doEvenIfSame){
		Taxa oldOtherTaxa = otherTaxa;
		TaxaAssociation oldAssociation = association;
		association = associationTask.getCurrentAssociation(taxa); 
		if (association == null)
			association = associationTask.getAssociation(taxa, 0); 
		if (oldAssociation != null)
			oldAssociation.removeListener(this);

		if (this.otherTaxa != null)
			otherTaxa.removeListener(this);
		if (association == null)
			otherTaxa=null; 
		else if (association.getTaxa(0)== taxa)
			otherTaxa = association.getTaxa(1);
		else
			otherTaxa = association.getTaxa(0);
		if (this.otherTaxa != null)
			otherTaxa.addListener(this);
		if (association != null)
			association.addListener(this);
		if (!doEvenIfSame && oldAssociation == association && oldOtherTaxa == otherTaxa)
			return;
		if (panel != null)
			panel.setAssociation(association, otherTaxa, taxa);
		parametersChanged(null);
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		resetAssociation(true);
		parametersChanged(notification);
	}

	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		if (obj == association){
			association.removeListener(this);
			association = null;
			resetAssociation(true);
		}
	}
	/*.................................................................................................................*/
	public void changed(Object caller, Object obj, Notification notification){
		if (Notification.appearsCosmetic(notification)){
			if (panel != null)
				panel.prepareList(false);
			return;
		}
		else if (notification != null && notification.getCode()== MesquiteListener.SELECTION_CHANGED){
			//here should behave diffently: if associated taxa, then change selection in panel (indeed panel should use natural selection
			if (panel != null)
				panel.prepareList(false);
			return;
		}
		else if (caller == association){
			resetAssociation(true);
			return;
		}
		if (panel != null)
			panel.prepareList(true);
		outputInvalid();
		parametersChanged(notification);
		super.changed(caller, obj, notification);
	}


	/*.................................................................................................................*/
	Taxon[] associates;
	public String getStringForTaxon(int ic){

		if (taxa!=null) {
			if (association==null)
				return "?";

			if (associates==null ||  associates.length < otherTaxa.getNumTaxa())
				associates = new Taxon[otherTaxa.getNumTaxa()];
			associates = association.getAssociates(taxa.getTaxon(ic), associates);
			if (associates!= null) {
				String s = "";
				boolean first = true;
				for (int i=0; i<associates.length; i++)
					if (associates[i]!=null){
						if (!first)
							s += ", ";
						s += associates[i].getName();
						first = false;
					}
				return s; 
			}
			return "-";
		}
		return "?";
	}
	public boolean useString(int ic){
		return true;
	}

	/*.................................................................................................................*/
	public String getWidestString(){
		return "88888888888888  ";
	}
	/*.................................................................................................................*/
	public String getTitle() {
		if (association != null && taxa != null && otherTaxa != null){
			Taxa containing = association.getContainingTaxa();
			if (containing == taxa)
				return "Contained taxa";
			else if (containing == otherTaxa)
				return "Containing taxa";

		}
		if (otherTaxa != null && otherTaxa.getName() != null && !StringUtil.startsWithIgnoreCase(otherTaxa.getName(),"Untitled") &&  !StringUtil.startsWithIgnoreCase(otherTaxa.getName(),"Taxa Association"))
			return otherTaxa.getName();
		return "Associates";
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	public void endJob() {
		if (panel != null && containingWindow != null)
			containingWindow.removeSidePanel(panel);
		if (this.taxa != null)
			taxa.removeListener(this);
		if (this.otherTaxa != null)
			otherTaxa.removeListener(this);
		if (this.association != null)
			association.removeListener(this);
		super.endJob();
	}

	void showPanel(boolean show){
		if (show){
			if (panel == null)
				panel = new AssocEditor(this);
			resetAssociation(true);
			containingWindow.addSidePanel(panel , 160);
			panel.setVisible(true);
			editorShown.setValue(true);
		}
		else {
			containingWindow.removeSidePanel(panel);

			editorShown.setValue(false);
		}

	}
}
/*=======================================*/
class AssocEditor extends MousePanel implements TextListener, FocusListener {
	Font df = new Font("Dialog", Font.BOLD, 12);
	TaxaAssociation assoc;
	Taxa otherTaxa, taxa;
	TaxonListAssoc ownerModule;
	//Button button, rbutton;
	MQTextArea text;
	int titleH = 18;
	int nameH = 30;
	int buttonH = 25;
	int headerH = 34;
	Image goaway, replace, add, subtract, query;
	SimpleTaxaList taxonList, headerList;
	Taxa headerTaxa;
	MesquiteCommand renameCommand;

	public AssocEditor(TaxonListAssoc ownerModule){
		super();
		setLayout(null);
		goaway = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "goAway.gif");
		add = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "add.gif");
		replace = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "replaceLeft.gif");
		subtract = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "subtract.gif"); 
		query = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "query.gif"); 
		text = new MQTextArea(" ", 50, 50, TextArea.SCROLLBARS_NONE);
		add(text);
		text.setVisible(true);
		text.setBounds(1, titleH, getBounds().width-2, nameH);
		text.setBackground(Color.lightGray);   //lightGray
		renameCommand = new MesquiteCommand("renameAssociation", ownerModule);
		this.ownerModule  = ownerModule;
		headerTaxa = new Taxa(2);
		headerTaxa.setTaxonName(0, "ASSIGNED");
		headerTaxa.setTaxonName(1, "UNASSIGNED");
		headerList = new SimpleTaxaList(headerTaxa,this, false, 2, false);
		headerList.setLocation(0,titleH + nameH + buttonH);
		headerList.setSize(getBounds().width, headerH);
		headerList.setVisible(true);
		add(headerList);		
		taxonList = new SimpleTaxaList(otherTaxa,this, true, 2, true);
		taxonList.setLocation(0,titleH + nameH + buttonH+headerH);
		taxonList.setSize(getBounds().width, getBounds().height-titleH - nameH - buttonH-headerH);
		taxonList.setVisible(true);
		text.addTextListener(this);
		text.addFocusListener(this);
		add(taxonList);		
	}
	void assignSelectedAssociates(){
		if (assoc == null)
			return;
		boolean[] selectedInList = taxonList.getSelectedList();

		if (selectedInList == null)
			return;
		for (int it= 0; it< taxa.getNumTaxa(); it++){
			if (taxonSelectedInListWindow(it)){
				assoc.zeroAllAssociations(taxa.getTaxon(it));
				for (int ito = 0; ito<selectedInList.length; ito++)
					if (selectedInList[ito])
						assoc.setAssociation(taxa.getTaxon(it), otherTaxa.getTaxon(ito), true);

			} 
		}
		assoc.notifyListeners(this, new Notification(MesquiteListener.VALUE_CHANGED));
	}

	public boolean[] getSelectedInList(){
		return taxonList.getSelectedList();
	}
	boolean taxonSelectedInListWindow(int it){
		return taxa.getSelected(it) || ((ListWindow)ownerModule.containingWindow).isRowSelected(it);
	}
	void addSelectedAssociates(){
		if (assoc == null)
			return;
		boolean[] selectedInList = taxonList.getSelectedList();

		if (selectedInList == null)
			return;
		for (int it= 0; it< taxa.getNumTaxa(); it++){
			if (taxa.getSelected(it)){
				for (int ito = 0; ito<selectedInList.length; ito++)
					if (selectedInList[ito])
						assoc.setAssociation(taxa.getTaxon(it), otherTaxa.getTaxon(ito), true);

			} 
		}
		assoc.notifyListeners(this, new Notification(MesquiteListener.VALUE_CHANGED));
	}
	void removeSelectedAssociates(){
		if (assoc == null)
			return;
		boolean[] selectedInList = taxonList.getSelectedList();

		if (selectedInList == null)
			return;
		for (int it= 0; it< taxa.getNumTaxa(); it++){
			if (taxonSelectedInListWindow(it)){
				for (int ito = 0; ito<selectedInList.length; ito++)
					if (selectedInList[ito]) {
						assoc.setAssociation(taxa.getTaxon(it), otherTaxa.getTaxon(ito), false);
					}

			} 
		}
		assoc.notifyListeners(this, new Notification(MesquiteListener.VALUE_CHANGED));
	}
	void removeAllAssociates(){
		if (assoc == null)
			return;
		for (int it= 0; it< taxa.getNumTaxa(); it++){
			if (taxonSelectedInListWindow(it)){
				assoc.zeroAllAssociations(taxa.getTaxon(it));
			}
		}
		assoc.notifyListeners(this, new Notification(MesquiteListener.VALUE_CHANGED));
	}
	void setAssociation(TaxaAssociation assoc, Taxa otherTaxa, Taxa taxa){
		this.assoc = assoc;
		if (text!=null){
			if (assoc!=null)
				text.setText(assoc.getName());
			else
				text.setText("");
		}
		this.otherTaxa = otherTaxa;
		this.taxa = taxa;
		prepareList(true);
	}
	private void close(){
		ownerModule.showPanel(false);
	}
	void reset(boolean completeReset){
		if (assoc == null)
			return;
		text.setText(assoc.getName());
		if (completeReset){
			taxonList.deselectAll();
			for (int it= 0; it< taxa.getNumTaxa(); it++){
				if (taxonSelectedInListWindow(it)){
					Taxon[] associates = assoc.getAssociates(taxa.getTaxon(it));
					if (associates != null)
						for (int ito = 0; ito < associates.length; ito++){
							int i = associates[ito].getNumber();
							taxonList.selectRow(i,true);
						}
				}
			}
		}
		int rowHeight = taxonList.getRowHeight();
		if (rowHeight>10)
			headerH = (int)(2.2*rowHeight);
		headerList.setSize(getWidth(), headerH);
		taxonList.setSize(getWidth(), getHeight()-titleH -  nameH - buttonH-headerH);
		taxonList.setLocation(0,titleH + nameH + buttonH+headerH);
		repaint();
	/*	int rowHeight = taxonList.getRowHeight();
		if (rowHeight>10){
			headerList.setSize(a,b);
		}*/
		headerList.repaint();
		taxonList.repaint();
		//	button.repaint();

	}
	boolean isAssignedSomewhere(int ito){
		Taxon oT = otherTaxa.getTaxon(ito);

		for (int it= 0; it< taxa.getNumTaxa(); it++){
			Taxon[] associates = assoc.getAssociates(taxa.getTaxon(it));
			if (associates != null)
				for (int i = 0; i < associates.length; i++)
					if (associates[i] == oT)
						return true;

		}
		return false;

	}

	void resetAssigned(){
		headerList.setAssigned(0, true);
		headerList.setAssigned(1, false);
		if (otherTaxa != null && assoc != null){
			for (int i= 0; i<otherTaxa.getNumTaxa(); i++){
				taxonList.setAssigned (i,false);
			}
			for (int it= 0; it< taxa.getNumTaxa(); it++){
				Taxon[] associates = assoc.getAssociates(taxa.getTaxon(it));
				if (associates != null)
					for (int i = 0; i < associates.length; i++)
						if (associates[i] !=null){
							int ito=associates[i].getNumber();
							taxonList.setAssigned (ito,true);
						}

			}
		}
	}

	void prepareList(boolean completeReset){
		if (completeReset){
			taxonList.setTaxa(otherTaxa);
		}
		resetAssigned();
		reset(completeReset);
	}

	public void setSize(int w, int h){
		super.setSize(w, h);
		int rowHeight = taxonList.getRowHeight();
		if (rowHeight>10)
			headerH = (int)(2.2*rowHeight);
		headerList.setSize(w, headerH);
		taxonList.setSize(w, h-titleH -  nameH - buttonH-headerH);
		taxonList.setLocation(0,titleH + nameH + buttonH+headerH);
		text.setBounds(1, titleH, getBounds().width-2, nameH);
		repaint();
		text.repaint();
		headerList.repaint();
		taxonList.repaint();
	}
	public void setBounds(int x, int y, int w, int h){
		super.setBounds(x, y, w, h);
		int rowHeight = taxonList.getRowHeight();
		if (rowHeight>10)
			headerH = (int)(2.2*rowHeight);
		headerList.setSize(w, headerH);
		taxonList.setSize(w, h-titleH - nameH - buttonH-headerH);
		taxonList.setLocation(0,titleH + nameH + buttonH+headerH);
		text.setBounds(1, titleH, getBounds().width-2, nameH);
		repaint();
		text.repaint();
		headerList.repaint();
		taxonList.repaint();
	}
	public void paint(Graphics g){
		g.drawImage(goaway, 2, 2, this);
		g.drawImage(replace, 8, titleH + nameH+2, this);
		g.drawImage(add, 32, titleH + nameH+2, this);
		g.drawImage(subtract, 56, titleH + nameH+2, this);
		g.drawImage(query, 80, titleH + nameH+2, this);
		g.setFont(df);

		g.setColor(Color.black);
		if (ownerModule!=null)
			g.drawString(getTitle(), 24, 16);
		g.drawLine(getBounds().width-1, 0, getBounds().width-1, getBounds().height);
		g.drawLine(0, 0, 0, getBounds().height);
		int bottom = titleH + nameH+buttonH -2;
		g.drawLine(0, bottom, getBounds().width, bottom);
	}
	String getTitle(){
		if (assoc != null && taxa != null && otherTaxa != null){
			Taxa containing = assoc.getContainingTaxa();
			if (containing == taxa)
				return "Contained taxa";
			else if (containing == otherTaxa)
				return "Containing taxa";
		}
		return "Associates";
	}
	/* to be used by subclasses to tell that panel touched */
	public void mouseMoved(int modifiers, int x, int y, MesquiteTool tool) {
		String message = "";
		if (x >= 2 && x <2+16 && y>= 2 && y< 2 + 16)
			message = "Close this Association Editor";
		else if (x >= 8 && x <8+16 && y>= titleH + nameH+2 && y< titleH + nameH+2 + 16)
			message = "For the taxa selected on the left, replaces the associated taxa with the taxa that are selected in list below";
		else if (x >= 32 && x <32+16 && y>= titleH + nameH+2 && y< titleH + nameH+2 + 16)
			message = "For the taxa selected on the left, adds to the association the taxa that are selected in list below";
		else if (x >= 56 && x <56+16 && y>= titleH + nameH+2 && y< titleH + nameH+2 + 16)
			message = "For the taxa selected on the left, removes from the association the taxa that are selected in list below";
		else if (x >= 80 && x <80+16 && y>= titleH + nameH+2 && y< titleH + nameH+2 + 16)
			message = "Get instructions";
		if (ownerModule!=null)
			ownerModule.getContainingWindow().setExplanation(message);

	}
	int clickCount=0;
	/* to be used by subclasses to tell that panel touched */
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		if (x >= 2 && x <2+16 && y>= 2 && y< 2 + 16)
			ownerModule.showPanel(false);
		else if (x >= 8 && x <8+16 && y>= titleH + nameH+2 && y< titleH + nameH+2 + 16)
			assignSelectedAssociates();
		else if (x >= 32 && x <32+16 && y>= titleH + nameH+2 && y< titleH + nameH+2 + 16)
			addSelectedAssociates();
		else if (x >= 56 && x <56+16 && y>= titleH + nameH+2 && y< titleH + nameH+2 + 16)
			removeSelectedAssociates();
		else if (x >= 80 && x <80+16 && y>= titleH + nameH+2 && y< titleH + nameH+2 + 16)
			ownerModule.alert(instructions());

	}

	String instructions (){
		String s = "This editor manages taxa to taxa associations (e.g., specimens associated with species). "
				+"The main list window to the left lists one block of taxa; the editor lists the other block of taxa associated with it. "
				+"To assign or remove associated taxa:\n\n(1) First, select a taxon row in the table at left.\n\n(2) In the editor, select the associated taxa whose status you want to change.\n\n"
				+"(3) Hit the arrow to assign those associates to the selected row (replacing whatever is there), the plus to add the associates, and the minus to remove them from association with the selected row.";
		return s;
	}
	/*.................................................................................................................*/
	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		this.clickCount = clickCount;
	}

	boolean nameChanged = false;
	String oldName = "";
	public void textValueChanged(TextEvent e) {
		if (assoc != null && text != null) {
			nameChanged = true;
		}
	}
	public void focusGained(FocusEvent e) {
		if (assoc != null && text != null) {
			oldName = assoc.getName();
		}
	}
	public void focusLost(FocusEvent e) {
		if (assoc != null && text != null && nameChanged) {
			if (StringUtil.stringsEqual(oldName, text.getText()))
				return;
			if (text.getText()== null)
				return;
			renameCommand.doItMainThread(ParseUtil.tokenize(text.getText()), null, null);
		}
	}

}

