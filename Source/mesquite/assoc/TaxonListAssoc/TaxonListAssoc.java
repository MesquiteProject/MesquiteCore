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
	MesquiteMenuItemSpec m0, m1, m2, m3, m4, m5, m6;
	AssociationSource associationTask;
	MesquiteWindow containingWindow;
	TaxaAssociation association;
	AssocEditor panel;

	boolean ignoreWhitespace=true;
	boolean ignoreCase = true;
	boolean matchNumbers=false;
	int minDigitsToMatch = 3;
	NameParser nameParser;
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		associationTask = (AssociationSource)hireNamedEmployee(AssociationSource.class, "#mesquite.assoc.StoredAssociations.StoredAssociations");
		if (associationTask == null)
			return sorry(getName() + " couldn't start because no source of taxon associations obtained.");
		MesquiteWindow f = containerOfModule();
		if (f instanceof MesquiteWindow){
			containingWindow = (MesquiteWindow)f;
			containingWindow.addSidePanel(panel = new AssocEditor(this), 160);
		}
		if (nameParser==null)
			nameParser = new NameParser(this, "taxon");
		loadPreferences();
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
	/*.................................................................................................................*/
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
		if (taxa!=null && association != null) {  // taxa is population, otherTaxa is contained/specimens
			Taxa otherTaxa = association.getOtherTaxa(taxa);
			if (nameParser==null)
				nameParser = new NameParser(this, "taxon");
			if (!MesquiteThread.isScripting()) {
				String helpString = "This tool requires that the taxon names of the master block of taxa are formed as reduced versions of the taxon names of the "
						+ "other block of taxa.  In particular, the taxon names of the master block must exactly match a portion of the names of the other block.  "
						+ "This tool find the match by removing the start and/or end of the longer names according to the criteria you specify, and, if that shorter name"
						+ "matches the name of a taxon in the master block, then the other taxon having that longer name is associated with the taxon in the master block.";
				nameParser.queryOptions("Options for matching associates", "Associates will be found by examining their names", helpString);
			}
			for (int it=0; it<taxa.getNumTaxa(); it++)
				for (int ito = 0; ito<otherTaxa.getNumTaxa(); ito++){
					String name = taxa.getTaxonName(it);
					String nameOther = nameParser.extractPart(otherTaxa.getTaxonName(ito));
					if (name == null || nameOther == null)
						continue;
					boolean matches = name.equals(nameOther);
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
	private void setAssociate(Taxon taxon, boolean add, boolean append){
		if (table !=null && taxa!=null && association != null) {
			boolean changed=false;
			if (add){
				Taxa otherTaxa = association.getOtherTaxa(taxa);
				if (taxon == null)
					taxon = otherTaxa.userChooseTaxon(containerOfModule(), "Select the taxon to be associated with the selected rows");
				if (taxon == null)
					return;
			}
			if (employer!=null && employer instanceof ListModule) {
				int c = ((ListModule)employer).getMyColumn(this);
				for (int i=0; i<taxa.getNumTaxa(); i++) {
					if (table.isCellSelectedAnyWay(c, i)) {
						Taxon t = taxa.getTaxon(i);
						if (!append)
							association.zeroAllAssociations(t);
						if (add)
							association.setAssociation(t, taxon, true);
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
	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("getAssociationsTask " + associationTask); 
		temp.addLine("resetAssociation"); 
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
		else  if (checker.compare(this.getClass(), "Calculates associates based upon an algorithm", null, commandName, "calculateAssociation")) {
			calculateAssociation();
		}
		else  if (checker.compare(this.getClass(), "Automatically sets associates if there is an exact match of names", null, commandName, "autoAssignExact")) {
			if (queryOptions())
				autoAssign(ignoreWhitespace, ignoreCase);
		}
		else  if (checker.compare(this.getClass(), "Sets which other taxon is associated with these; replaces existing", null, commandName, "setAssociate")) {
			setAssociate(null, true, false);
		}
		else  if (checker.compare(this.getClass(), "Sets which other taxon is associated with these; replaces existing", null, commandName, "createNewTaxaFromSelected")) {
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
		else if (checker.compare(this.getClass(), "Sets which other taxon is associated with these; adds to existing", null, commandName, "addAssociate")) {
			setAssociate(null, true, true);
		}
		else if (checker.compare(this.getClass(), "Creates a new taxon and adds to existing", null, commandName, "createAssociate")) {
			if (association == null)
				return null;
			Taxa otherTaxa = association.getOtherTaxa(taxa);
			otherTaxa.addTaxa(otherTaxa.getNumTaxa()-1, 1, false);
			Taxon t = otherTaxa.getTaxon(otherTaxa.getNumTaxa()-1);
			String n = MesquiteString.queryString(containerOfModule(), "Name of Taxon", "Name the new taxon", "Taxon");
			t.setName(n);
			otherTaxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));
			setAssociate(t, true, true);
		}
		else if (checker.compare(this.getClass(), "Deletes associations", null, commandName, "removeAssociates")) {
			setAssociate(null, false, false);
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	public boolean isShowing(TaxaAssociation assoc){
		return assoc == association;
	}
	/*.................................................................................................................*/
	public void setTableAndTaxa(MesquiteTable table, Taxa taxa){
		deleteMenuItem(m0);
		deleteMenuItem(m1);
		deleteMenuItem(m2);
		deleteMenuItem(m3);
		deleteMenuItem(m4);
		deleteMenuItem(m5);
		deleteMenuItem(m6);
		m0 = addMenuItem(null, "Auto-assign Matches...", makeCommand("autoAssignExact", this));
		m6 = addMenuItem(null, "Calculate Matches...", makeCommand("calculateAssociation", this));
		m1 = addMenuItem(null, "Assign Associate...", makeCommand("setAssociate", this));
		m2 = addMenuItem(null, "Add Associate...", makeCommand("addAssociate", this));
		m3 = addMenuItem(null, "Remove Associates", makeCommand("removeAssociates", this));
		m4 = addMenuItem(null, "Create New Associated Taxon...", makeCommand("createAssociate", this));
		m5 = addMenuItem(null, "Create New Taxa in Master Block from Selected", makeCommand("createNewTaxaFromSelected", this));
		if (this.taxa != null)
			taxa.removeListener(this);
		this.taxa = taxa;
		if (this.taxa != null)
			taxa.addListener(this);
		this.table = table;
		if (!MesquiteThread.isScripting())
			resetAssociation(false);
	}
	void resetAssociation(boolean doEvenIfSame){
		Taxa oldOtherTaxa = otherTaxa;
		TaxaAssociation oldAssociation = association;
		association = associationTask.getCurrentAssociation(taxa); 
		if (association == null)
			association = associationTask.getAssociation(taxa, 0); 
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
		if (!doEvenIfSame && oldAssociation == association && oldOtherTaxa == otherTaxa)
			return;
		panel.setAssociation(association, otherTaxa, taxa);
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		resetAssociation(true);
		parametersChanged(notification);
	}






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
		if (panel != null)
			panel.prepareList(true);
		outputInvalid();
		parametersChanged(notification);
		super.changed(caller, obj, notification);
	}


	public String getTitle() {
		if (otherTaxa != null && otherTaxa.getName() != null && !StringUtil.startsWithIgnoreCase(otherTaxa.getName(),"Untitled") &&  !StringUtil.startsWithIgnoreCase(otherTaxa.getName(),"Taxa Association"))
			return otherTaxa.getName();
		return "Associates";
	}
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
	/*public void drawInCell(int ic, Graphics g, int x, int y,  int w, int h, boolean selected){
	 if (taxa==null || g==null)
	 return;
	 TaxaPartition part = (TaxaPartition)taxa.getCurrentSpecsSet(TaxaPartition.class);
	 Color c = g.getColor();
	 boolean colored = false;
	 if (part!=null) {
	 TaxaGroup tg = part.getTaxaGroup(ic);
	 if (tg!=null){
	 Color cT = tg.getColor();
	 if (cT!=null){
	 g.setColor(cT);
	 g.fillRect(x+1,y+1,w-1,h-1);
	 colored = true;
	 }
	 }
	 }
	 if (!colored){ 
	 if (selected)
	 g.setColor(Color.black);
	 else
	 g.setColor(Color.white);
	 g.fillRect(x+1,y+1,w-1,h-1);
	 }

	 String s = getStringForRow(ic);
	 if (s!=null){
	 FontMetrics fm = g.getFontMetrics(g.getFont());
	 if (fm==null)
	 return;
	 int sw = fm.stringWidth(s);
	 int sh = fm.getMaxAscent()+ fm.getMaxDescent();
	 if (selected)
	 g.setColor(Color.white);
	 else
	 g.setColor(Color.black);
	 g.drawString(s, x+(w-sw)/2, y+h-(h-sh)/2);
	 g.setColor(c);
	 }
	 }
	 */
	public String getWidestString(){
		return "88888888888  ";
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
		super.endJob();
	}
	void close(){
		iQuit();
	}
}
/*=======================================*/
class AssocEditor extends MousePanel {
	Font df = new Font("Dialog", Font.BOLD, 12);
	TaxaAssociation assoc;
	Taxa otherTaxa, taxa;
	TaxonListAssoc ownerModule;
	//Button button, rbutton;
	TextArea text;
	int titleH = 18;
	int nameH = 30;
	int buttonH = 25;
	Image goaway, replace, add, subtract;
	SimpleTaxaList taxonList;


	public AssocEditor(TaxonListAssoc ownerModule){
		super();
		setLayout(null);
		goaway = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "goaway.gif");
		add = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "add.gif");
		replace = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "replaceLeft.gif");
		subtract = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "subtract.gif"); //should be subtract.gif
		text = new TextArea(" ", 50, 50, TextArea.SCROLLBARS_NONE);
		add(text);
		text.setVisible(true);
		text.setBounds(1, titleH, getBounds().width-2, nameH);
		text.setBackground(Color.lightGray);   //lightGray
		this.ownerModule  = ownerModule;

		taxonList = new SimpleTaxaList(otherTaxa,this);
		taxonList.setLocation(0,titleH + nameH + buttonH);
		taxonList.setSize(getBounds().width, getBounds().height-titleH - nameH - buttonH);
		taxonList.setVisible(true);
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
		if (text!=null && assoc!=null)
			text.setText(assoc.getName());
		this.otherTaxa = otherTaxa;
		this.taxa = taxa;
		prepareList(true);
	}
	private void close(){
		ownerModule.close();
	}
	void reset(boolean completeReset){
		if (assoc == null)
			return;
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
		repaint();
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
		if (otherTaxa != null && completeReset){
			taxonList.setTaxa(otherTaxa);
		}
		resetAssigned();
		reset(completeReset);
	}

	public void setSize(int w, int h){
		super.setSize(w, h);
		taxonList.setSize(w, h-titleH -  nameH - buttonH);
		text.setBounds(1, titleH, getBounds().width-2, nameH);
		repaint();
		text.repaint();
		taxonList.repaint();
	}
	public void setBounds(int x, int y, int w, int h){
		super.setBounds(x, y, w, h);
		taxonList.setSize(w, h-titleH - nameH - buttonH);
		text.setBounds(1, titleH, getBounds().width-2, nameH);
		repaint();
		text.repaint();
		taxonList.repaint();
	}
	public void paint(Graphics g){
		g.drawImage(goaway, 2, 2, this);
		g.drawImage(replace, 8, titleH + nameH+2, this);
		g.drawImage(add, 32, titleH + nameH+2, this);
		g.drawImage(subtract, 56, titleH + nameH+2, this);
		g.setFont(df);

		g.setColor(Color.black);
		if (ownerModule!=null)
			g.drawString(ownerModule.getTitle(), 24, 16);
		g.drawLine(getBounds().width-1, 0, getBounds().width-1, getBounds().height);
		g.drawLine(0, 0, 0, getBounds().height);
		int bottom = titleH + nameH+buttonH -2;
		g.drawLine(0, bottom, getBounds().width, bottom);
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
		if (ownerModule!=null)
			ownerModule.getContainingWindow().setExplanation(message);

	}
	int clickCount=0;
	/* to be used by subclasses to tell that panel touched */
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		if (x >= 2 && x <2+16 && y>= 2 && y< 2 + 16)
			close();
		else if (x >= 8 && x <8+16 && y>= titleH + nameH+2 && y< titleH + nameH+2 + 16)
			assignSelectedAssociates();
		else if (x >= 32 && x <32+16 && y>= titleH + nameH+2 && y< titleH + nameH+2 + 16)
			addSelectedAssociates();
		else if (x >= 56 && x <56+16 && y>= titleH + nameH+2 && y< titleH + nameH+2 + 16)
			removeSelectedAssociates();

	}

	/*.................................................................................................................*/
	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		this.clickCount = clickCount;
	}

}

