/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.TreeListAttachment;
/*~~  */

import java.util.Vector;

import mesquite.lib.Attachable;
import mesquite.lib.CommandChecker;
import mesquite.lib.Listable;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteLong;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Notification;
import mesquite.lib.ParseUtil;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeVector;
import mesquite.lib.ui.ListDialog;
import mesquite.lib.ui.SingleLineTextField;
import mesquite.lists.lib.TreeListAssistant;

/* ======================================================================== */
public class TreeListAttachment extends TreeListAssistant {
	/*.................................................................................................................*/
	public String getName() {
		return "Attachment to Tree";
	}
	public String getExplanation() {
		return "Displays an item attached to trees." ;
	}
	TreeVector treesBlock;
	String nameOfAttached = null;
	ListableVector names = new ListableVector();
	MesquiteTable treeListTable;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addMenuItem( "Attachment to Show...", makeCommand("setAttached",  this));
		addMenuItem( "New Attachment...", makeCommand("newAttachment",  this));
		addMenuItem( "Paste Values from Clipboard", makeCommand("paste",  this));
		return true;
	}

	/*...............................................................................................................*/
	/** returns whether or not any cells can be pasted into.*/
	public boolean allowPasting(){
		return true;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		if (nameOfAttached==null)
			return null;
		Snapshot temp = super.getSnapshot(file);
		temp.addLine("setAttached " + ParseUtil.tokenize(nameOfAttached));
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the name of the attached numbers to be used", "[name of attached]", commandName, "setAttached")) {
			String name= parser.getFirstToken(arguments);
			if (StringUtil.blank(name)){
				reviewAttachmentsAvailable(names, treesBlock);
				Listable L = ListDialog.queryList(containerOfModule(), "Attachment", "Which attachment to show?\n(This list may change as other tree sources are used or trees are read.)", MesquiteString.helpString, names, 0);
				if (L!=null)
					name = L.getName();
			}
			if (!StringUtil.blank(name)) {
				nameOfAttached = name;
				if (!MesquiteThread.isScripting()) parametersChanged();
			}

		}
		else if (checker.compare(this.getClass(), "Adds a new attachment", "[]", commandName, "newAttachment")) {

			String[] kinds = new String[]{ "Decimal number", "Integer number", "String of text", "Boolean (true/false)"};
			MesquiteInteger selectedInDialog = new MesquiteInteger(0);
			ListDialog dialog = new ListDialog(containerOfModule(), "New Attachment", "What kind of attachment to add to selected trees?", false,null, kinds, 8, selectedInDialog, "OK", null, false, true);
			SingleLineTextField nameF = dialog.addTextField("Name of Attachment:", "", 30);
			dialog.completeAndShowDialog(true);
			boolean switchName = false;
			if (dialog.buttonPressed.getValue() == 0)  {
				int result = selectedInDialog.getValue();
				String name = nameF.getText();
				if (StringUtil.blank(name)) {
					discreetAlert("You need to give a name to the attachment.");
					return null;
				}
				if (result>=0){
					switchName = true;
					for (int i=0; i<treeListTable.getNumRows(); i++){
							MesquiteTree tree = (MesquiteTree)treesBlock.getTree(i);
							Object obj = tree.getAttachment(name);
							if (obj == null){
								if (result == 0)
									obj = new MesquiteDouble(name, MesquiteDouble.unassigned);
								else if (result == 1)
									obj = new MesquiteLong(name, MesquiteLong.unassigned);
								else if (result == 2)
									obj = new MesquiteString(name, "");
								else if (result == 3){
									MesquiteBoolean mbool = new MesquiteBoolean();
									mbool.setName(name);
									obj = mbool;
								}
								tree.attachIfUniqueName(obj);
						}
					}
				}
				if (switchName)
					nameOfAttached = name;
			}
			dialog.dispose();
			if (switchName){
				if (!MesquiteThread.isScripting()) parametersChanged();
			}

		}
		/*else if (checker.compare(this.getClass(), "Pastes", "", commandName, "paste")) {
			MesquiteBoolean success = pasteIntoRows(treeListTable);
			if (StringUtil.notEmpty(success.getName()))
				discreetAlert("Problem with pasting:" + success.getName());
			if (!MesquiteThread.isScripting() && success.getValue()) parametersChanged();
		}*/
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}





	Class getClassOfAttachment(TreeVector trees, String nameOfAttached){
		Class c = null;
		boolean conflict = false;
		if (trees !=null){
			for (int itree =0; itree < trees.size() && !conflict; itree++){
				Tree tree = trees.getTree(itree);
				if (tree !=null && tree instanceof Attachable){
					Object a = ((MesquiteTree)tree).getAttachment(nameOfAttached);
					if (a != null){
						if (c == null)
							c = a.getClass();
						else if (c != a.getClass()) {
							conflict = true;
							c = Error.class;
							break;
						}
					}
				}
			}
		}
		return c;
	}
	void reviewAttachmentsAvailable(ListableVector v, TreeVector trees){
		names.removeAllElements(false);
		if (trees !=null){
			for (int itree =0; itree < trees.size(); itree++){
				Tree tree = trees.getTree(itree);
				if (tree !=null && tree instanceof Attachable){
					Vector at = ((Attachable)tree).getAttachments();
					if (at !=null) {
						for (int i =0; i < at.size(); i++){
							Object obj = at.elementAt(i);
							String name = ((Listable)obj).getName();
							if (v.indexOfByNameIgnoreCase(name)<0) {
								MesquiteString ms = new MesquiteString(name);
								ms.setName(name);
								v.addElement(ms, false);
							}
						}
					}
				}
			}
		}
	}
	public void setTableAndTreeBlock(MesquiteTable table, TreeVector trees){
		treesBlock = trees;
		this.treeListTable = table;
		if (!MesquiteThread.isScripting()){
			reviewAttachmentsAvailable(names, trees);
			if (names.size()==0)
				alert("Sorry, there is nothing attached to the trees to show");
			else {
				Listable L = ListDialog.queryList(containerOfModule(), "Select item", "What attachments to tree should be shown?", MesquiteString.helpString, names, 0);
				if (L!=null)
					nameOfAttached = L.getName();
			} 

		}
	}

	/*...............................................................................................................*/
	/** returns whether or not a cell of table is editable.*/
	public boolean isCellEditable(int row){
		return true;
	}
	/*...............................................................................................................*/
	/** for those permitting editing, indicates user has edited to incoming string.*/
	public void setString(int row, String s){

		if (treesBlock==null || row < 0 || row >= treesBlock.size() || nameOfAttached == null)
			return;
		if (StringUtil.blank(s))
			s = null;
		Class classOfAttached = getClassOfAttachment(treesBlock, nameOfAttached);
		if (classOfAttached == Error.class){
			logln("Can't edit because this attachment is heterogenous among trees.");
		}
		else if (classOfAttached == null){
			logln("Can't edit because this attachment appears to be undefined.");
		}
		else {
			MesquiteTree tree = (MesquiteTree)treesBlock.getTree(row);
			Object a = ((MesquiteTree)tree).getAttachment(nameOfAttached);
			if (classOfAttached == MesquiteString.class){
				MesquiteString mb = null;
				if (a != null)
					mb = (MesquiteString)a;
				else
					mb = new MesquiteString("");
				mb.setValue(s);
				if (a == null){
					mb.setName(nameOfAttached);
					tree.attachIfUniqueName(mb);
				}
			}
			else if (classOfAttached == MesquiteDouble.class){
				MesquiteDouble mb = null;
				if (a != null)
					mb = (MesquiteDouble)a;
				else
					mb = new MesquiteDouble();
				mb.setValue(s);
				if (a == null){
					mb.setName(nameOfAttached);
					tree.attachIfUniqueName(mb);
				}
			}
			else if (classOfAttached == MesquiteInteger.class){
				MesquiteInteger mb = null;
				if (a != null)
					mb = (MesquiteInteger)a;
				else
					mb = new MesquiteInteger();
				mb.setValue(s);
				if (a == null){
					mb.setName(nameOfAttached);
					tree.attachIfUniqueName(mb);
				}
			}
			else if (classOfAttached == MesquiteLong.class){
				MesquiteLong mb = null;
				if (a != null)
					mb = (MesquiteLong)a;
				else
					mb = new MesquiteLong();
				mb.setValue(s);
				if (a == null){
					mb.setName(nameOfAttached);
					tree.attachIfUniqueName(mb);
				}
			}
			else if (classOfAttached == MesquiteBoolean.class){
				MesquiteBoolean mb = null;
				if (a != null)
					mb = (MesquiteBoolean)a;
				else
					mb = new MesquiteBoolean();
				mb.setValue(s);
				if (a == null){
					mb.setName(nameOfAttached);
					tree.attachIfUniqueName(mb);
				}
			}
	}

		//cycle through trees to discover kind of attachment
	}
	/*
	public Object getAttachment(String name){
		return getAttachment(name, null);
	}
	 */
	public String getTitle() {
		if (nameOfAttached !=null)
			return nameOfAttached;
		return "Attached";
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		if (obj == treesBlock)
			treesBlock=null;
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;  //TODO: respond
	}
	public void changed(Object caller, Object obj, Notification notification){
		if (Notification.appearsCosmetic(notification))
			return;
		parametersChanged(notification);
	}
	public boolean canHireMoreThanOnce(){
		return true;
	}
	/*.................................................................................................................*/
	public String getStringForTree(int ic){
		if (treesBlock==null)
			return "";
		Tree tree = treesBlock.getTree(ic);
		if (tree ==null || !(tree instanceof MesquiteTree))
			return "";
		Object a = ((MesquiteTree)tree).getAttachment(nameOfAttached);
		
		if (a==null)
			return "";
		else
			return "" + a;
	}
	public String getWidestString(){
		return " 88888888.8888 ";
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}

}

