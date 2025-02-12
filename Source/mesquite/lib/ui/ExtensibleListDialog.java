/* Mesquite source code.  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.ui;

import java.awt.*;
import java.awt.event.*;

import mesquite.lib.Debugg;
import mesquite.lib.Listable;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteTrunk;

/*  To Do:
	- double click on list, brings up Edit
	
*/

/*===============================================*/
/** An extensible dialog box containing a list with standard buttons.  */
public abstract class ExtensibleListDialog extends ExtensibleDialog implements ItemListener {
	DoubleClickList list = null;
	Button editButton;
	Button newButton;
	Button renameButton;
	Button deleteButton;
	Button duplicateButton;
	Object names;
	ListableVector listableVector;
	protected Listable currentElement;
	protected int currentItem=0;
	String blankName = "Untitled";
	String message;
	boolean namesMustBeUnique = true;
	boolean allowMultipleSelections = false;
	Panel mainPanel;
	GridBagConstraints gridConstraints;

	/*.................................................................................................................*/
	public ExtensibleListDialog (MesquiteWindow parent, String title, String message, MesquiteInteger buttonPressed, ListableVector listableVector) {
		super(parent, title, buttonPressed);
		this.listableVector = listableVector;
		this.message = message;
		addListAndButtons();
	}
	/*.................................................................................................................*/
	public ExtensibleListDialog (MesquiteWindow parent, String title, String message, ListableVector listableVector) {
		super(parent,title);
		this.listableVector = listableVector;
		this.message = message;
		addListAndButtons();
	}
	/*.................................................................................................................*/
	public void addListAndButtons(){
		addLabel(message);

		GridBagConstraints c = getGridBagConstraints();
		c.fill=GridBagConstraints.BOTH;
		c.weightx=1;
		c.insets=new Insets(0,8,0,8);
		mainPanel = addNewDialogPanel(c);
		GridBagLayout gridBackButtons = new GridBagLayout();
		gridConstraints = new GridBagConstraints();
		gridConstraints.gridwidth=1;
		gridConstraints.gridheight=GridBagConstraints.REMAINDER;
		gridConstraints.fill=GridBagConstraints.BOTH;
		gridConstraints.anchor = GridBagConstraints.CENTER;	        
		mainPanel.setLayout(gridBackButtons);
		gridBackButtons.setConstraints(mainPanel,gridConstraints);

		gridConstraints.gridy = 1;
		gridConstraints.gridx=1;
		gridConstraints.ipadx = 2;
		gridConstraints.weightx=2;
		gridConstraints.weighty=1;
		MesquiteInteger selected = new MesquiteInteger(0);
		list = createListenedList(listableVector.getElementArray(),selected,8,this,this, getAllowMultipleSelections());  //getAllowMultipleSelections() put here because setMultipleMode not working

		mainPanel.add(list,gridConstraints);
		setAllowMultipleSelections(getAllowMultipleSelections());

		gridConstraints.gridy = 1;
		gridConstraints.gridx=2;
		addAnEmptyImage(mainPanel);

		gridConstraints.weightx=0;
		gridConstraints.weighty=1;
		gridConstraints.gridheight=1;
		gridConstraints.gridx=3;
		gridConstraints.gridy = 0;
		gridConstraints.ipady = 4;
		gridConstraints.fill=GridBagConstraints.NONE;
		newButton = addNewButtonBesideList("New...");
		editButton = addNewButtonBesideList("Edit...");
		renameButton = addNewButtonBesideList("Rename...");
		duplicateButton = addNewButtonBesideList("Duplicate");
		deleteButton = addNewButtonBesideList("Delete...");

		addAdditionalButtonsBesideList();

		gridConstraints.gridy ++;
		mainPanel.add(new Label(" "), gridConstraints);  // just to fill up space

		resetCurrent();

		if (!allNamesUnique()) {
			AlertDialog.notice(MesquiteModule.mesquiteTrunk.containerOfModule(), "Alert", "Two or more " + pluralObjectName() + " share the same name.");
		}
	}
	/*.................................................................................................................*/
	public boolean allNamesUnique() {
		if (listableVector==null)
			return true;
		else
			return listableVector.allNamesUnique();
	}
/*.................................................................................................................*/
/** override this to to add new buttons beside list; use addNewButtonBesideList to add them */
	public void addAdditionalButtonsBesideList() {   }
/*.................................................................................................................*/
	public Button addNewButtonBesideList(String s) {  
	     	gridConstraints.gridy ++;
		Button button = addAButton(s, mainPanel, gridConstraints);
		button.addActionListener(this);
		return button;
	}
	/*.................................................................................................................*/
	public List getList(){
		return list;
	}
/*.................................................................................................................*/
	public void setNamesMustBeUnique(boolean unique) {
		namesMustBeUnique = unique;
	}
/*.................................................................................................................*/
	public boolean getNamesMustBeUnique() {
		return namesMustBeUnique;
	}
/*.................................................................................................................*/
	public void setAllowMultipleSelections(boolean multipleSelections) {   // NOTE: this is not working yet!!!!
		allowMultipleSelections = multipleSelections;
		if (list!=null)
			list.setMultipleMode(multipleSelections);
	}
/*.................................................................................................................*/
	public boolean getAllowMultipleSelections() {
		return allowMultipleSelections;
	}
/*.................................................................................................................*/
	public void setEditButtonLabel(String s) {
		editButton.setLabel(s);
	}
/*.................................................................................................................*/
	public String getEditButtonLabel() {
		return editButton.getLabel();
	}
/*.................................................................................................................*/
	public void setEditButtonEnabled(boolean enabled) {
		editButton.setEnabled(enabled);
	}
/*.................................................................................................................*/
	public void setRenameButtonEnabled(boolean enabled) {
		renameButton.setEnabled(enabled);
	}
/*.................................................................................................................*/
	public void setDeleteButtonEnabled(boolean enabled) {
		deleteButton.setEnabled(enabled);
	}
/*.................................................................................................................*/
	public void setDuplicateButtonEnabled(boolean enabled) {
		duplicateButton.setEnabled(enabled);
	}
/*.................................................................................................................*/
	public void enableButtons(){
		setEditButtonEnabled(true);
		setRenameButtonEnabled(true);
		setDeleteButtonEnabled(true);
		setDuplicateButtonEnabled(true);
	}
/*.................................................................................................................*/
	public void disableButtons(){
		setEditButtonEnabled(false);
		setRenameButtonEnabled(false);
		setDeleteButtonEnabled(false);
		setDuplicateButtonEnabled(false);
	}
/*.................................................................................................................*/
	public int getLastItem(){
		return list.getItemCount()-1;
	}
/*.................................................................................................................*/
	void resizeDialog() {
		list.invalidate();
		list.doLayout();
		invalidate();
		validate();
		prepareDialogHideFirst();
	}
/*.................................................................................................................*/
	public void resetCurrent(){
		if (listableVector.size()>0) {
			currentElement = listableVector.elementAt(0);
			enableButtons();
			newListElementSelected();
		}
		else {
			currentElement = null; 
			disableButtons();
		}
	}
/*.................................................................................................................*/
  	private void setElement(int item){
  		if (item >=0 && item < listableVector.size()){
  			currentElement = listableVector.elementAt(item);
  			if (currentElement==null)
  				return;
			currentItem=item;
  			list.select(item);
			enableButtons();
			newListElementSelected();
			return;
		}
  	}
/*.................................................................................................................*/
/** Can be overridden if needed */
  	public void newListElementSelected(){
  	}
/*.................................................................................................................*/
	public String untitledElementName(){
		return "untitled";
	}
/*.................................................................................................................*/
/** this is the name of the class of objects */
	public abstract String objectName();
/*.................................................................................................................*/
/** this is the name of the class of objects */
	public abstract String pluralObjectName();
/*.................................................................................................................*/
	public abstract Listable createNewElement(String name, MesquiteBoolean success);
/*.................................................................................................................*/
	public void addNewElement(Listable obj, String s) {
		listableVector.addElement(obj, false);
		list.add(s);
		resizeDialog();
		setElement(listableVector.indexOf(obj));
	}
/*.................................................................................................................*/
	protected void newElement(String s){
		MesquiteBoolean success = new MesquiteBoolean (false);
		Listable obj = createNewElement(s, success);
		 
		if (success.getValue())
			addNewElement(obj, s);
	}
/*.................................................................................................................*/
	private void newElement(){
		MesquiteString io = new MesquiteString(untitledElementName());
		if  (QueryDialogs.queryShortString(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Name of New " + objectName(),  "Name of New " + objectName(), io)) {

			if (namesMustBeUnique && listableVector.nameAlreadyInList(io.getValue())) {
				MesquiteTrunk.mesquiteTrunk.alert("This name is already used by another " + objectName() + "; please pick a unique name.");
				return;
			}
			newElement(io.getValue());
		}
	}
/*.................................................................................................................*/
	public void editNumberedElement(int item){	
		if (getEditable(item))
			editElement(item);
		else if (getViewable(item))
			viewElement(item);
	}
/*.................................................................................................................*/
	public abstract boolean getEditable(int item);
/*.................................................................................................................*/
	public abstract void editElement(int item);
/*.................................................................................................................*/
	public boolean getViewable(int item){
		return true;
	}
/*.................................................................................................................*/
	public void viewElement(int item){
	}
/*.................................................................................................................*/
/**  This method should return an object that is a duplicate of currentElement  */
	public abstract Listable duplicateElement(String name);
/*.................................................................................................................*/
	public abstract void renameElement(int item, Listable element, String newName);
/*.................................................................................................................*/
	protected void renameCurrentElement(boolean forceRenameIfNotUnique){
		if (currentElement==null)
			return;

		MesquiteString io = new MesquiteString(((Listable)currentElement).getName());
		if  (QueryDialogs.queryShortString(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Name of " + objectName(),  "Name of " + objectName(), io)) {

			int currentItem = list.getSelectedIndex();
			if (namesMustBeUnique && listableVector.nameAlreadyInList(io.getValue(),currentItem)) {
				MesquiteTrunk.mesquiteTrunk.alert("This name is already used by another " + objectName() + "; please pick a unique name.");
				if (forceRenameIfNotUnique)
					renameCurrentElement(true);
				return;
			}

			//MesquiteString ms = new MesquiteString("");
			//ms.setName(io.getValue());
			
			renameElement(currentItem, currentElement, io.getValue());
			//list.remove(currentItem);
			list.replaceItem(io.getValue(),currentItem);
			resizeDialog();
			setElement(currentItem);
		}
	}
/*.................................................................................................................*/
	public abstract void deleteElement(int item, int newSelectedItem);
/*.................................................................................................................*/
	private void deleteCurrentElement(){
		if (currentElement==null)
			return;
		if (AlertDialog.query(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Delete", "Are you sure you want to delete the "+objectName()+"?", "Delete", "Cancel", 2)){
			int item= list.getSelectedIndex();
			int newSelectedItem=item-1;
			if (newSelectedItem<0)
				newSelectedItem=0;
			list.remove(item);
			deleteElement(item, newSelectedItem);
			resetCurrent();
			resizeDialog();
			setElement(newSelectedItem);
		}
	}
/*.................................................................................................................*/
  	public void itemStateChanged(ItemEvent e){
  		if (e.getItemSelectable() == list){
	  		if (list.getSelectedIndex()>=0) {
	  			setElement(list.getSelectedIndex());
	  		}
  		}
  	}
/*.................................................................................................................*/
	public void doubleClicked(Component c){
	  	if (c==list) {
	  		currentItem=list.getSelectedIndex();
	  		editNumberedElement(list.getSelectedIndex());
	  	}
	}
/*.................................................................................................................*/
/**  If you override this to add more buttons, make sure you call super.actonPerformed()  */
	public void actionPerformed(ActionEvent e){
		Debugg.println("@e extensibleLISTdialog " + e.getActionCommand());
		currentItem =list.getSelectedIndex();
		if (getEditButtonLabel().equals(e.getActionCommand()))
			editNumberedElement(list.getSelectedIndex());
		else if ("New...".equals(e.getActionCommand()))
			newElement();
		else if ("Rename...".equals(e.getActionCommand())) 
			renameCurrentElement(false);
		else if ("Delete...".equals(e.getActionCommand()))
			deleteCurrentElement();
		else if ("Duplicate".equals(e.getActionCommand())) {
			String name = listableVector.getUniqueName(((Listable)currentElement).getName());
			Listable obj = duplicateElement(name);
			addNewElement(obj,name);
		}
		else
			super.actionPerformed(e);
	}
}




