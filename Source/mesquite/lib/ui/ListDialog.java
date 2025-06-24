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

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Component;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import mesquite.lib.Context;
import mesquite.lib.EmployerEmployee;
import mesquite.lib.Explainable;
import mesquite.lib.IntegerArray;
import mesquite.lib.Listable;
import mesquite.lib.ListableVector;
import mesquite.lib.Listened;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteModuleInfo;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.Prioritizable;
import mesquite.lib.Showable;
import mesquite.lib.SpecialListName;
import mesquite.lib.StringUtil;
import mesquite.lib.simplicity.InterfaceManager;

/*===============================================*/
/** A little dialog box with a list.*/
public class ListDialog extends ExtensibleDialog implements ItemListener{
	DoubleClickList list;
	MesquiteInteger selected;
	IntegerArray indicesSelected = new IntegerArray(0);
	String[] strings = null;
	Listable[] listables = null;
	boolean[] isSubchoice = null;
	Listable[] listablesUsed = null;
	Listable[] originalListables = null;
	boolean[] isSubchoiceUsed = null;
	TextArea explanation = null;
	//Button ok;
	String thirdButton = null;
	String okButton = null;
	String cancelButton = null;
	Checkbox hideSecondaryCheckbox;
	
	boolean hideSecondary;
	Class priorityClass;
	String hideSecString = "Hide Secondary Choices";
	boolean prioritize = false;
	boolean hasDefault=true;
	WorkaroundThread thread;
	boolean requiresResetWorkaround = true;
	public ListDialog (MesquiteWindow parent, String title, String message, boolean autoComplete, String helpString, Object names, int numRows, boolean[] isSubchoice, MesquiteInteger selected, String okButton, String cancelButton, String thirdButton, boolean prioritize, Class priorityClass, boolean hasDefault, boolean multipleMode) {
		super(parent,title);
		
		//setting up list
		int numNames = -1;
		if (names instanceof Listable[]){
			this.listables = (Listable[])names;
			numNames = ((Listable[])names).length;
		}
		else if (names instanceof String[]){
			strings = (String[])names;
			numNames = ((String[])names).length;
		}
		if (numRows<0) {
			if (numNames<0 || numNames>16)
				numRows = 16;
			else
				numRows = numNames;
			if (numRows<8)
				numRows = 8;
		}
		originalListables = listables;
		listables = filterHiddenListables(listables);
		listablesUsed = listables; 
		
		
		this.selected = selected;
		this.thirdButton =thirdButton;
		this.prioritize = prioritize;
		this.priorityClass=priorityClass;
		this.hasDefault=hasDefault;
		this.isSubchoice = isSubchoice;
		this.isSubchoiceUsed = isSubchoice;
		this.hideSecondary = !EmployerEmployee.secondaryChoicesOnInDialogs;
		if (!priorityDifferenceExists()){
			this.prioritize = false;
			this.hideSecondary = false;
		}

		if (helpString!=null)
			appendToHelpString(helpString);

		if (message!= null) {
			addLargeOrSmallTextLabel(message);
		}

		Panel mainPanel = addNewDialogPanel();
		list = createListenedList (names, isSubchoice, selected, numRows, this,this, multipleMode);
		list.setForceSize(true);
		mainPanel.add(list);

		if (anyDocumentables(listables)){
			explanation = addTextAreaSmallFont("", 4);
			if (explanation.getParent() != null)
				explanation.setBackground(ColorDistribution.brighter(explanation.getParent().getBackground(),0.5));
			explanation.setEditable(false);
		}


		if (this.prioritize){
			hideSecondaryCheckbox = addCheckBox(hideSecString, false);
			hideSecondaryCheckbox.setState(hideSecondary);
			hideSecondaryCheckbox.addItemListener(this);
			requiresResetWorkaround = MesquiteTrunk.isMacOSX();
			if (requiresResetWorkaround)
				thread = new WorkaroundThread(this);
			resetShowSecondary(hideSecondary);
			if (requiresResetWorkaround)
				thread.start();
		}
		else {
			hideSecondaryCheckbox = null;
			resetShowSecondary(false);
		}


		if (!StringUtil.blank(thirdButton)) 
			setExtraButton(thirdButton);

		setAutoDispose(false);

		list.requestFocus();
		list.requestFocusInWindow();
		if (numSelected() == 1)
			showExplanation(list.getSelectedIndex());

		this.okButton = okButton;
		this.cancelButton = cancelButton;
		if (autoComplete){
			String OK = defaultOKLabel;
			if (!StringUtil.blank(okButton))
				OK = okButton;
			this.okButton = OK;
			String cancel = defaultCancelLabel;
			if (!StringUtil.blank(cancelButton))
				cancel = cancelButton;
			completeDialog(OK,cancel, hasDefault,this);
		}

	}
	
	public void resetList(Object names, boolean justUpdateNames){
		super.resetList(list, names, isSubchoice, selected, justUpdateNames);
	}

	public ListDialog (MesquiteWindow parent, String title, String message, boolean autoComplete, String helpString, Object names, boolean[] isSubchoice, MesquiteInteger selected, String okButton, String cancelButton, String thirdButton, boolean prioritize, Class priorityClass, boolean hasDefault, boolean multipleMode) {
		this(parent,title,message, autoComplete, helpString, names, -1, isSubchoice, selected,okButton,cancelButton, thirdButton,prioritize,priorityClass,hasDefault, multipleMode);
	}
	public ListDialog (MesquiteWindow parent, String title, String message, boolean autoComplete, String helpString, Object names, MesquiteInteger selected, String okButton, String thirdButton, boolean prioritize, Class priorityClass, boolean hasDefault, boolean multipleMode) {
		this(parent,title,message, autoComplete, helpString, names, null, selected,okButton,null, thirdButton,prioritize,priorityClass,hasDefault, multipleMode);
	}
	public ListDialog (MesquiteWindow parent, String title, String message, boolean autoComplete, String helpString, Object names, int numRows, MesquiteInteger selected, String okButton, String thirdButton, boolean prioritize, Class priorityClass, boolean hasDefault, boolean multipleMode) {
		this(parent,title,message, autoComplete, helpString, names, numRows, null, selected,okButton,null, thirdButton,prioritize,priorityClass,hasDefault, multipleMode);
	}
	public ListDialog (MesquiteWindow parent, String title, String message, boolean autoComplete, String helpString, Object names, MesquiteInteger selected, String okButton, String cancelButton, String thirdButton, boolean prioritize, Class priorityClass, boolean hasDefault, boolean multipleMode) {
		this(parent,title,message, autoComplete, helpString, names, null, selected,okButton,cancelButton, thirdButton,prioritize,priorityClass,hasDefault, multipleMode);
	}

	public ListDialog (MesquiteWindow parent, String title, String message, boolean autoComplete, String helpString, Object names, MesquiteInteger selected, String okButton,String thirdButton, boolean hasDefault, boolean multipleMode) {
		this(parent,title,message, autoComplete, helpString, names,selected,okButton,thirdButton,false,null,hasDefault, multipleMode);
	}
	public ListDialog (MesquiteWindow parent, String title, String message, boolean autoComplete, String helpString, Object names, int numRows, MesquiteInteger selected, String okButton,String thirdButton, boolean hasDefault, boolean multipleMode) {
		this(parent,title,message, autoComplete, helpString, names,numRows, selected,okButton,thirdButton,false,null,hasDefault, multipleMode);
	}
	public ListDialog (MesquiteWindow parent, String title, String message, boolean autoComplete, String helpString, Object names, MesquiteInteger selected, String thirdButton, boolean hasDefault, boolean multipleMode) {
		this(parent,title,message, autoComplete, helpString, names,selected,"OK",thirdButton,false,null,hasDefault, multipleMode);
	}
	public ListDialog (MesquiteWindow parent, String title, String message, boolean autoComplete, String helpString, Object names, MesquiteInteger selected, String thirdButton, boolean hasDefault, boolean multipleMode, String okButton, String cancelButton) {
		this(parent,title,message, autoComplete, helpString, names,selected,okButton,cancelButton, thirdButton,false,null,hasDefault, multipleMode);
	}
	private Listable[] filterHiddenListables(Listable[] L){
		Listable[] list = L;
		if (list == null)
			return null;
		int count = 0;
		for (int i=0; i< list.length; i++){
			Listable item = list[i];
			if (item != null)
				if (item instanceof Listened && !((Listened)item).isUserVisible()){
				}
				else
					count++;
		}
		Listable[] result = new Listable[count];
		count = 0;
		for (int i=0; i< list.length; i++){
			Listable item = list[i];
			if (item != null)
				if (item instanceof Listened && !((Listened)item).isUserVisible()){
				}
				else {
					result[count++] = item;
				}
		}
		return result;
	}
	private void filterHiddenListablesForSimplicity(){
		Listable[] list = listablesUsed;
		if (list == null)
			return;
		if (!InterfaceManager.isSimpleMode())
			return;
		Listable[] v = new Listable[list.length];
		int count = 0;
		Vector sc = new Vector();
		for (int i=0; i< list.length; i++){
			Listable item = list[i];
			if (item != null && item instanceof MesquiteModuleInfo){
				if (!InterfaceManager.onHiddenClassList(((MesquiteModuleInfo)item).getClassName())){
					v[count++] = item;
					if (isSubchoiceUsed != null)
						sc.addElement(new MesquiteBoolean(isSubchoiceUsed[i]));
				}
				else if (isSubchoiceUsed!= null && i<isSubchoiceUsed.length) {
					if (!isSubchoiceUsed[i]){  //to be deleted, and not a subchoice.  Hence look for subchoices and delete them
						for (int k = i+1; k<list.length && k<isSubchoiceUsed.length && isSubchoiceUsed[k]; k++){
							list[k] = null; //zapping
						}
					}
				}
			}
			else if (item != null)
				if (!InterfaceManager.onHiddenClassList(item.getClass())){
					v[count++] = item;
					if (isSubchoiceUsed != null)
						sc.addElement(new MesquiteBoolean(isSubchoiceUsed[i]));
				}
				else if (isSubchoiceUsed!= null && i<isSubchoiceUsed.length) {
					if (!isSubchoiceUsed[i]){  //to be deleted, and not a subchoice.  Hence look for subchoices and delete them
						for (int k = i+1; k< list.length && k<isSubchoiceUsed.length && isSubchoiceUsed[k]; k++){
							list[k] = null; //zapping
						}
					}
				}
		}
		Listable[] result = new Listable[count];
		for (int i=0; i< count; i++)
			result[i] = v[i];

		if (isSubchoiceUsed != null){
			boolean[] s = new boolean[count];
			for (int i=0; i< count; i++)
				s[i] = ((MesquiteBoolean)sc.elementAt(i)).getValue();
			isSubchoiceUsed = s;
		}
		listablesUsed = result;
	}
	private boolean priorityDifferenceExists(){

		if (listables!=null){
			boolean secondPriorityFound = false;
			boolean firstPriorityFound = false;
			for (int i=0; i<listables.length; i++) {
				if (listables[i]!=null){
					if (listables[i] instanceof Prioritizable && !((Prioritizable)listables[i]).isFirstPriority(priorityClass)){
						secondPriorityFound = true;
						if (firstPriorityFound)
							return true;
					}
					else {
						firstPriorityFound = true;
						if (secondPriorityFound)
							return true;
					}
				}
			}
		}
		return false;
	}
	public void setVisible(boolean vis){
		if (!vis && requiresResetWorkaround && thread !=null)
			thread.done = true;
		super.setVisible(vis);
	}
	public void dispose(){
		if (requiresResetWorkaround && thread !=null)
			thread.done = true;
		super.dispose();
	}
	boolean firstTouchWorkaround = false;
	boolean redoResetWorkaround = false;

	public synchronized void resetWorkaround(){
		if (redoResetWorkaround){

			list.setVisible(false);
			list.setVisible(true);

			list.requestFocus();
			list.requestFocusInWindow();
			list.invalidate();
			list.validate();
			invalidate();
			validate();
			list.repaint();
			repaint();
		}
		//	else
		//		resetShowSecondary(showSecondary);
		redoResetWorkaround = false;

	}
	private boolean isPrimary(Listable[] listables, boolean[] isSubchoice, int i, Listable previousChoice){
		if ((isSubchoice == null || isSubchoice[i]) && previousChoice != null && previousChoice instanceof MesquiteModuleInfo){
			Class dutyClass = ((MesquiteModuleInfo)previousChoice).getHireSubchoice();
			if (dutyClass != null)
				return !(listables[i] instanceof Prioritizable) || ((Prioritizable)listables[i]).isFirstPriority(dutyClass);


		}
		return !(listables[i] instanceof Prioritizable) || ((Prioritizable)listables[i]).isFirstPriority(priorityClass);
	}
	private synchronized void resetShowSecondary(boolean hideSec){
		if (listables!=null){

			list.setVisible(false);
			list.removeAll();
			Listable previousChoice = null;
			if (!hideSec && priorityDifferenceExists()) {
				listablesUsed = listables;
				isSubchoiceUsed = isSubchoice;
			}
			else {
				int count = 0;
				boolean previousChoiceShown = isSubchoice == null;
				if (listables.length > 0)
					previousChoice = listables[0];
				for (int i=0; i<listables.length; i++) {
					if (listables[i]!=null && (!hideSec || isPrimary(listables, isSubchoice, i, previousChoice))){
						if (! (isSubchoice != null && isSubchoice[i] && !previousChoiceShown)){  //don't show if subchoice and previous not shown
							count++;
							if (isSubchoice != null && !isSubchoice[i]) {
								previousChoiceShown = true;
								previousChoice = listables[i];
							}
						}
					}
					else if (isSubchoice != null && !isSubchoice[i])
						previousChoiceShown = false;
				}
				listablesUsed = new Listable[count];
				if (isSubchoice != null)
					isSubchoiceUsed = new boolean[count];
				count = 0;
				previousChoiceShown = isSubchoice == null;
				if (listables.length > 0)
					previousChoice = listables[0];

				for (int i=0; i<listables.length; i++) {
					if (listables[i]!=null && (!hideSec || isPrimary(listables, isSubchoice, i, previousChoice))){
						if (! (isSubchoice != null && isSubchoice[i] && !previousChoiceShown)){  //don't show if subchoice and previous not shown
							if (isSubchoice != null)
								isSubchoiceUsed[count] = isSubchoice[i];
							listablesUsed[count++] = listables[i];
							if (isSubchoice != null && !isSubchoice[i]){
								previousChoiceShown = true;
								previousChoice = listables[i];
							}
						}
					}
					else if (isSubchoice != null && !isSubchoice[i])
						previousChoiceShown = false;
				}
			}
			int wasLength = listablesUsed.length;
			filterHiddenListablesForSimplicity();
			if (listablesUsed.length == 0 && wasLength > 0){
				list.add("(No choices are available.");
				list.add("     All choices are hidden because");
				list.add("     a simplified interface is in use.");
				list.add("     Please go to the Simplification tab");
				list.add("     of log window to adjust interface.)");
			}
			else {
				for (int i=0; i<listablesUsed.length; i++) {
					String name = "";
					if (listablesUsed[i] instanceof SpecialListName && ((SpecialListName)listablesUsed[i]).getListName()!=null)
						name = ((SpecialListName)listablesUsed[i]).getListName();
					else if (listablesUsed[i].getName()!=null)
						name = listablesUsed[i].getName();
					if (isSubchoiceUsed != null && isSubchoiceUsed[i])
						name = "  - " + name;
					list.add(StringUtil.truncateIfLonger(name, maxLengthInList), -1);
				}
				if (selected!=null && selected.getValue()>=0 && selected.getValue()<listables.length)
					list.select(selected.getValue());
				if (numSelected() == 1)
					showExplanation(list.getSelectedIndex());
			}


			list.setVisible(true);
			list.requestFocus();
			list.invalidate();
			list.validate();
			invalidate();
			validate();
			list.repaint();
			repaint();

		}
	}
	protected void resetList(){
		if (listables!=null){
			resetShowSecondary(hideSecondary);
		}
		else if (strings!=null){
			list.setVisible(false);
			int vis = list.getVisibleIndex();
			boolean[] sel = new boolean[list.getItemCount()];
			for (int i = 0; i<list.getItemCount(); i++){
				sel[i] = list.isIndexSelected(i);
			}
			list.removeAll();
			for (int i=0; i<strings.length; i++)
				if (strings[i]!=null)
					list.add(strings[i], -1);
			for (int i = 0; i<sel.length && i < list.getItemCount(); i++){
				if (sel[i])
					list.select(i);
			}
			if (selected!=null && selected.getValue()>=0 && selected.getValue()<strings.length)
				list.select(selected.getValue());
			if (numSelected() == 1)
				showExplanation(list.getSelectedIndex());
			list.makeVisible(vis);
			list.setVisible(true);
			list.invalidate();
			list.validate();
			invalidate();
			validate();
			list.repaint();
		}
	}

	private boolean anyDocumentables(Listable[] L){
		if (L==null)
			return false;
		for (int i=0; i<L.length; i++)
			if (L[i] instanceof Explainable)
				return true;
		return false;
	}
	protected int numSelected(){
		int count = 0;
		for (int i=0; i<list.getItemCount(); i++)
			if (list.isIndexSelected(i))
				count++;
		return count;
	}
	public void setSelected(boolean[] selected){
		if (selected == null)
			return;
		for (int i=0; i<list.getItemCount(); i++)
			list.deselect(i);

		for (int i=0; i<list.getItemCount() && i<selected.length; i++)
			if (selected[i])
				list.select(i);
	}
	private void showExplanation(int selected){
		if (selected <0) {
			if (explanation !=null) {
				explanation.setText("");
				return;
			}
		}
		else if (listablesUsed !=null && selected < listablesUsed.length && listablesUsed[selected] instanceof Explainable) {
			if (explanation !=null) {
				String exp = ((Explainable)listablesUsed[selected]).getExplanation();
				if (exp == null)
					exp = "";
				explanation.setText(exp);
				return;
			}
		}
		if (explanation !=null) 
			explanation.setText("");
	}
	public void itemStateChanged(ItemEvent e){
		if (listablesUsed==null)
			return;
		if (e.getItem() instanceof String && hideSecString.equals((String)e.getItem())){
			hideSecondary = hideSecondaryCheckbox.getState();
			resetShowSecondary(hideSecondary);
			redoResetWorkaround = true;
		}
		else if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof Integer){
			if (numSelected() > 1)
				showExplanation(-1);
			else {
				showExplanation(((Integer)e.getItem()).intValue());
			}
		}
	}
	/*	public void actionPerformed(ActionEvent e){
		if (defaultButtonString!=null) {
			buttonHit(defaultButtonString, null);
			dispose();
		}
	}
	 */
	public static Listable queryList(MesquiteWindow parent, String title, String message, String helpString, ListableVector vector, int current, boolean doShow) {
		if (vector==null) 
			return null;
		MesquiteInteger io = new MesquiteInteger(current);
		Listable[] names = vector.getListables();
		String thirdButton = null;
		if (doShow)
			thirdButton = " Show ";
		while (true) {
			ListDialog id = new ListDialog(parent, title, message, true, helpString, names, io, thirdButton,true, false);

			id.setVisible(true);
			if (io.isUnassigned()) {
				id.dispose();
				return null;
			}
			int result = io.getValue();
			if (result <0) { //negative value; must be asking to show
				result = - result - 1;  
				if (result>=0 && result<vector.size()) {
					Listable li =  (Listable)vector.elementAt(result);
					if (li instanceof Showable) {
						((Showable)li).showMe();
					}
					io.setValue(result);
				}
			}
			else if (result>=0 && result<vector.size()) {
				id.dispose();
				MesquiteTrunk.mesquiteTrunk.logln("Chosen: " + ((Listable)vector.elementAt(result)).getName());
				return (Listable)vector.elementAt(result);
			}
			else {
				id.dispose();
				return null;
			}
		}
	}
	//TODO: probably better if all of these return array of selected!
	public static Listable queryList(MesquiteWindow parent, String title, String message, String helpString, ListableVector vector, int current) {
		if (vector==null) 
			return null;
		MesquiteInteger io = new MesquiteInteger(current);
		Listable[] names = vector.getListables();
		if (names==null)
			return null;
		ListDialog id = new ListDialog(parent, title, message, true,helpString, names, io, null,true, false);
		id.setVisible(true);
		if (id!=null)
			id.dispose();
		int result = io.getValue();
		if (io.isUnassigned())
			return null;
		if (result>=0 && result<vector.size()){
			MesquiteTrunk.mesquiteTrunk.logln("Chosen: " + ((Listable)vector.elementAt(result)).getName());
			return (Listable)vector.elementAt(result);
		}
		else return null;
	}
	public static Listable[] queryListMultiple(MesquiteWindow parent, String title, String message, String helpString, ListableVector vector, boolean[] selected) {
		return queryListMultiple(parent, title, message, helpString, null, true, vector, selected);
	}
	public static Listable[] queryListMultiple(MesquiteWindow parent, String title, String message, String helpString, String okButton, boolean hasDefault, ListableVector vector, boolean[] selected) {
		if (vector==null) 
			return null;
		Listable[] names = vector.getListables();
		if (names==null)
			return null;
		ListDialog id = new ListDialog(parent, title, message, true,helpString, names, null, okButton,null,hasDefault, true);
		//		if (okButton!=null)
		//		id.ok.setLabel(okButton);
		id.list.setMultipleMode(true);
		id.setSelected(selected);
		id.setVisible(true);
		IntegerArray result = id.indicesSelected;
		if (result==null || result.getSize()==0) {
			id.dispose();
			return null;
		}
		Listable[] L = new Listable[result.getSize()];
		for (int i=0; i<result.getSize(); i++)
			L[i] = (Listable)vector.elementAt(result.getValue(i));
		id.dispose();
		return L;
	}
	public static int queryModuleList(MesquiteModule employer, String title, String message, String helpString, Listable[] objects, boolean[] isSubchoice, boolean prioritize, Class priorityClass, int current) {
		if (objects==null) 
			return MesquiteInteger.unassigned;
		MesquiteInteger io = new MesquiteInteger(current);
		ListDialog id = new ListDialog(employer.containerOfModule(), title, message, true,helpString, objects, isSubchoice, io, null, null,null,prioritize, priorityClass, true, false);

		id.setVisible(true);
		id.dispose();
		int result = io.getValue();
		if (io.isUnassigned())
			return MesquiteInteger.unassigned;
		if (result>=0 && result<objects.length) ///ERROR here it's returning item in original list even if prioritized!!!!!!!!!!
			return result;
		else return MesquiteInteger.unassigned;
	}
	public static int queryList(MesquiteWindow parent, String title, String message, String helpString, Listable[] objects, boolean[] isSubchoice, boolean prioritize, Class priorityClass, int current) {
		if (objects==null) 
			return MesquiteInteger.unassigned;
		MesquiteInteger io = new MesquiteInteger(current);
		ListDialog id = new ListDialog(parent, title, message, true,helpString, objects, isSubchoice, io, null,null,null,prioritize, priorityClass, true, false);
		id.setVisible(true);
		id.dispose();
		int result = io.getValue();
		if (io.isUnassigned())
			return MesquiteInteger.unassigned;
		if (result>=0 && result<objects.length){ ///ERROR here it's returning item in original list even if prioritized!!!!!!!!!!
			MesquiteTrunk.mesquiteTrunk.logln("Chosen: " + objects[result].getName());
			return result;
		}
		else return MesquiteInteger.unassigned;
	}
	public static Listable queryModuleList(MesquiteModule employer, String title, String message, String helpString, Listable[] objects, boolean prioritize, Class priorityClass, int current) {
		if (objects==null) 
			return null;
		MesquiteInteger io = new MesquiteInteger(current);
		ListDialog id = new ListDialog(employer.containerOfModule(), title, message, true,helpString, objects, io, null,null,prioritize, priorityClass, true, false);

		id.setVisible(true);
		id.dispose();
		int result = io.getValue();
		if (io.isUnassigned())
			return null;
		if (result>=0 && result<objects.length){ ///ERROR here it's returning item in original list even if prioritized!!!!!!!!!!
			MesquiteTrunk.mesquiteTrunk.logln("Chosen: " + objects[result].getName());
			return objects[result];
		}
		else return null;
	}
	public static Listable queryList(MesquiteWindow parent, String title, String message, String helpString, Listable[] objects, boolean prioritize, Class priorityClass, int current) {
		if (objects==null) 
			return null;
		MesquiteInteger io = new MesquiteInteger(current);
		ListDialog id = new ListDialog(parent, title, message, true,helpString, objects, io, null,null,prioritize, priorityClass, true, false);
		id.setVisible(true);
		id.dispose();
		int result = io.getValue();
		if (io.isUnassigned())
			return null;
		if (result>=0 && result<objects.length){ ///ERROR here it's returning item in original list even if prioritized!!!!!!!!!!
			MesquiteTrunk.mesquiteTrunk.logln("Chosen: " + objects[result].getName());
			return objects[result];
		}
		else return null;
	}
	public static Listable queryList(MesquiteWindow parent, String title, String message, String helpString, Listable[] objects, int current) {
		if (objects==null) 
			return null;
		MesquiteInteger io = new MesquiteInteger(current);
		ListDialog id = new ListDialog(parent, title, message, true,helpString, objects, io, null,true, false);
		id.setVisible(true);
		id.dispose();
		int result = io.getValue();
		if (io.isUnassigned())
			return null;
		if (result>=0 && result<objects.length){
				MesquiteTrunk.mesquiteTrunk.logln("Chosen: " + objects[result].getName());
			return objects[result];
		}
		else return null;
	}
	public static Listable queryList(MesquiteWindow parent, String title, String message, String helpString,  String OKButton, String cancelButton, Listable[] objects, int current) {
		if (objects==null) 
			return null;
		MesquiteInteger io = new MesquiteInteger(current);
//		public ListDialog (MesquiteWindow parent, String title, String message, boolean autoComplete, String helpString, Object names, MesquiteInteger selected, String okButton, String cancelButton, String thirdButton, boolean prioritize, Class priorityClass, boolean hasDefault, boolean multipleMode) {
		ListDialog id = new ListDialog(parent, title, message, true,helpString, objects, io, OKButton, cancelButton, null,false, null, true, false);
//		public ListDialog (MesquiteWindow parent, String title, String message, boolean autoComplete, String helpString, Object names, MesquiteInteger selected, String thirdButton, boolean hasDefault, boolean multipleMode) {
//		ListDialog id = new ListDialog(parent, title, message, true,helpString, objects, io, null,true, false);
		id.setVisible(true);
		id.dispose();
		int result = io.getValue();
		if (io.isUnassigned())
			return null;
		if (result>=0 && result<objects.length){
				MesquiteTrunk.mesquiteTrunk.logln("Chosen: " + objects[result].getName());
			return objects[result];
		}
		else return null;
	}
	public static int queryList(MesquiteWindow parent, String title, String message, String helpString, Context[] contexts, int current, boolean doShow) {
		if (contexts==null)
			return MesquiteInteger.unassigned;
		MesquiteInteger io = new MesquiteInteger(current);
		String[] names = new String[contexts.length];
		for (int i = 0; i<names.length; i++)
			names[i]=contexts[i].getContextName();

		String thirdButton = null;
		if (doShow)
			thirdButton = "Show"; //NOTE about a kludge: the name of this button needs to remain "show".  See buttonHit in ListDialog
		while (true) {
			ListDialog id = new ListDialog(parent, title, message, true,helpString, names, io, thirdButton,true, false);

			id.setVisible(true);
			id.dispose();
			if (io.isUnassigned())
				return MesquiteInteger.unassigned;
			int result = io.getValue();
			if (result <0) { //negative value; must be asking to show
				result = - result - 1;  
				if (result>=0 && result<contexts.length) {
					Context li =  contexts[result];
					if (li instanceof Showable) {
						((Showable)li).showMe();
					}
					io.setValue(result);
				}
			}
			else if (result>=0 && result<contexts.length)
				return result;

			else return MesquiteInteger.unassigned;
		}
	}
	public static int queryList(MesquiteWindow parent, String title, String message, String helpString, String[] names, int current, String okButton, String cancelButton) {
		if (names==null)
			return MesquiteInteger.unassigned;
		MesquiteInteger io = new MesquiteInteger(current);
		ListDialog id = new ListDialog(parent, title, message, true,helpString, names, io, null,true, false, okButton, cancelButton);
		id.setVisible(true);
		id.dispose();
		if (io.isCombinable() && io.getValue()>=0 && io.getValue()<names.length)
			MesquiteTrunk.mesquiteTrunk.logln("Chosen: " + names[io.getValue()]);
		return io.getValue();
	}
	public static int queryList(MesquiteWindow parent, String title, String message, String helpString, String[] names, int current) {
		if (names==null)
			return MesquiteInteger.unassigned;
		MesquiteInteger io = new MesquiteInteger(current);
		ListDialog id = new ListDialog(parent, title, message, true,helpString, names, io, null,true, false);
		id.setVisible(true);
		id.dispose();
		if (io.isCombinable() && io.getValue()>=0 && io.getValue()<names.length)
			MesquiteTrunk.mesquiteTrunk.logln("Chosen: " + names[io.getValue()]);
		return io.getValue();
	}
	public static String queryList(MesquiteWindow parent, String title, String message, String helpString, Class dutyClass, Object condition, MesquiteModule prospectiveEmployer) {
		Listable[] names = MesquiteTrunk.mesquiteModulesInfoVector.getModulesOfDuty(dutyClass, condition, prospectiveEmployer);
		if (names==null)
			return null;
		MesquiteInteger io = new MesquiteInteger(0);
		ListDialog id = new ListDialog(parent, title, message, true,helpString, names, io, null,true, false);
		id.setVisible(true);
		id.dispose();
		if (io.isUnassigned())
			return null;
		if (io.getValue()>=0){
			MesquiteTrunk.mesquiteTrunk.logln("Chosen: " + names[io.getValue()].getName());
			return names[io.getValue()].getName();
		}
		else
			return null;
	}
	public DoubleClickList getList(){
		return list;
	}
	public IntegerArray getIndicesSelected(){
		return indicesSelected;
	}
	public IntegerArray getIndicesCurrentlySelected(){
		indicesSelected.setValues(translateIndicesUsedToOriginal(list.getSelectedIndexes()));
		return indicesSelected;
	}
	private int[] translateIndicesUsedToOriginal(int[] indices){
		if (indices !=null && listablesUsed !=null && listablesUsed != originalListables){
			int count = 0;
			for (int i=0; i< indices.length; i++){
				if (translateIndexUsedToOriginal(indices[i]) >=0)
					count++;
			}
			if (count == 0)
				return indices;
			int[] usedIndices = new int[count];
			count =0;
			for (int i=0; i< indices.length; i++){
				int used = translateIndexUsedToOriginal(indices[i]);
				if (used >=0)
					usedIndices[count++] = used;
			}
			return usedIndices;
		}
		return indices;
	}
	private int findListable(Listable[] lArray, Listable target){
		if (lArray==null)
			return -1;
		for (int i=0; i< lArray.length; i++)
			if (target == lArray[i])
				return i;
		return -1;
	}
	private int translateIndexUsedToOriginal(int index){
		if (listablesUsed !=null && listablesUsed != originalListables && index>0 && index < originalListables.length && index < listablesUsed.length){
			Listable listable = listablesUsed[index];
			int which = findListable(originalListables, listable);
			return which;
		}
		return index;
	}
	/*.................................................................................................................*/
	public void doubleClicked(Component c){
		if (c==list) {
			try {
				try {   
					Thread.sleep(100);   // for reasons that are entirely unclear, putting this in here prevents the Java 1.6/MacOS X 10.6.2 crash
					// that happens if one doubleclicks in a list to set up a Maj Rule consensus tree and the like
				} 
				catch (Exception e) {
				}
				if (selected!=null) {
					selected.setValue(translateIndexUsedToOriginal(list.getSelectedIndex()));
				}
				indicesSelected.setValues(translateIndicesUsedToOriginal(list.getSelectedIndexes()));
				if (buttonPressed!=null) {
					buttonPressed.setValue(getButtonNumber(okButton));
				}
				
			}
			catch (NumberFormatException eq){}
			dispose();
		}
	}
	/*.................................................................................................................*/
	public void buttonHit(String buttonLabel, Button button) {
		if (buttonLabel!=null)
			if (buttonPressed!=null) {
				buttonPressed.setValue(getButtonNumber(buttonLabel));
			}
			if (getMainButton() != null && buttonLabel.equalsIgnoreCase(getMainButton().getLabel())){
				try {
					if (selected!=null) {
						selected.setValue(translateIndexUsedToOriginal(list.getSelectedIndex()));
					}
					indicesSelected.setValues(translateIndicesUsedToOriginal(list.getSelectedIndexes()));
				}
				catch (NumberFormatException eq){}

				dispose();
			}
			else if (buttonLabel.equalsIgnoreCase("Show")){
				int i = translateIndexUsedToOriginal(list.getSelectedIndex());
				if (i>=0 && selected!=null) 
					selected.setValue(-i-1); //if nothing selected do not respond!
				dispose();
			}
			else if (buttonLabel.equalsIgnoreCase(thirdButton)){ //ZQ: The seems to have a special use, to deselect, and yet it is written as if the label is flexible/generic.
				if (selected != null)
					selected.setValue(-1); 
				for (int i = 0; i<list.getRows(); i++)
					list.deselect(i);
				if (indicesSelected != null)
				indicesSelected.setValues(translateIndicesUsedToOriginal(list.getSelectedIndexes()));
			}
			else if (buttonLabel.equalsIgnoreCase("Cancel") || buttonLabel.equalsIgnoreCase("Done") || buttonLabel.equalsIgnoreCase(cancelButton)){
				if (selected!=null)
					selected.setToUnassigned();
				indicesSelected.setValues(null);
				//if (autoDispose || buttonLabel.equalsIgnoreCase("Cancel"))
				dispose();
			}
	}
}

class WorkaroundThread extends Thread { //workaround for bug in Java 1.5 on OS X
	ListDialog dlog;
	boolean done = false;
	public WorkaroundThread(ListDialog dlog){
		this.dlog = dlog;
	}
	public void run(){
		int count = 0;
		while (!done && !MesquiteTrunk.mesquiteTrunk.mesquiteExiting){
			try {
				Thread.sleep(20);
				count++;
				if (dlog.redoResetWorkaround){
					dlog.resetWorkaround();
					count = 0;
				}
				else if (count % 25 == 0 && dlog.firstTouchWorkaround){
					dlog.redoResetWorkaround = true;
				}
			}
			catch (InterruptedException e){
				done = true;
			}

		}
	}
}
