/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.TreeListAttachments;
/*~~  */

import java.awt.Color;

import java.awt.Graphics;
import java.awt.Image;
import java.util.Vector;

import mesquite.lib.Bits;
import mesquite.lib.CommandChecker;
import mesquite.lib.Debugg;
import mesquite.lib.DoubleArray;
import mesquite.lib.Explainable;
import mesquite.lib.Listable;
import mesquite.lib.ListableVector;
import mesquite.lib.LongArray;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteEvent;
import mesquite.lib.MesquiteFlag;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteLong;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteString;
import mesquite.lib.NameReference;
import mesquite.lib.Notification;
import mesquite.lib.ObjectArray;
import mesquite.lib.Parser;
import mesquite.lib.StringArray;
import mesquite.lib.StringUtil;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeVector;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.ListDialog;
import mesquite.lib.ui.MesquiteImage;
import mesquite.lib.ui.MesquitePopup;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lists.lib.ListModule;
import mesquite.lists.lib.TreeListAssistant;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.ShowLinkedMatrixMachine;
/* ======================================================================== */
public class TreeListAttachments extends TreeListAssistant {
	TreeVector treesBlock;
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addMenuItem("Delete attachments...", new MesquiteCommand("deleteAttachments", this));
		return true;
	}
	MesquiteTable table;
	public void setTableAndTreeBlock(MesquiteTable table, TreeVector trees){
		treesBlock = trees;
		this.table = table;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Manage Attachments to Trees";
	}
	public String getExplanation() {
		return "Shows and manages what metadata is attached to trees." ;
	}
	/*.................................................................................................................*/
	public String getTitle() {
		return "Attachments";
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
		parametersChanged(notification);
	}

	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Deletes attachments to selected trees", "[]", commandName, "deleteAttachments")) {
			if (table == null)
				return null;
			ListableVector vec = getAttachmentInfo(false);
			//present dialog with multiselect list to delete
			//MesquiteWindow parent, String title, String message, String helpString, ListableVector vector, boolean[] selected
			String which = "all";
			if (table.anyCellsInColumnSelectedAnyWay(((ListModule)employer).getMyColumn(this)))
				which = "selected";
			Listable[] toDelete = ListDialog.queryListMultiple(containerOfModule(), "Attachments to delete from " + which + " trees", "Which attachments to delete?", null, vec, null);
			if (toDelete == null)
				return null;
			int col = ((ListModule)employer).getMyColumn(this);
			boolean anySelected = table.anyCellsInColumnSelectedAnyWay(col);
			for (int itr = 0; itr<treesBlock.size(); itr++){
				if (!anySelected || table.isCellSelectedAnyWay(col, itr)){
					MesquiteTree tree = (MesquiteTree)treesBlock.getTree(itr);

					for (int i = 0; i< toDelete.length; i++){
						AttachmentReference aRef = (AttachmentReference)toDelete[i];
						if (aRef.toTree){
							Object obj = tree.getAttachment(aRef.name, aRef.cl);
							if (obj == null)
								Debugg.errln("ATTACHMENT to delete not found");
							else 
								tree.detach(obj);
						}
						else {
							if (aRef.type == LONGS)
								tree.removeAssociatedLongs(aRef.nr);
							else if (aRef.type == DOUBLES)
								tree.removeAssociatedDoubles(aRef.nr);
							else if (aRef.type == STRINGS)
								tree.removeAssociatedStrings(aRef.nr);
							else if (aRef.type == BITS)
								tree.removeAssociatedBits(aRef.nr);
							else if (aRef.type == OBJECTS)
								tree.removeAssociatedObjects(aRef.nr);
							else if (aRef.type == FLAGS)
								tree.removeAssociatedObjects(aRef.nr);
						}
					}
				}
			}
			parametersChanged();


		}
		else if (checker.compare(this.getClass(), "Menu item selected for attachment for row", "[row][item]", commandName, "treeAttachMI")) {
			Parser parser = new Parser(arguments);
			int row = MesquiteInteger.fromString(parser);
			int item = MesquiteInteger.fromString(parser);
			MesquiteTree tree = (MesquiteTree)treesBlock.getTree(row);
			//Add tree attachments to popup
			String[] tNames = tree.getAttachentTypesAndExplanations();
			if (tNames != null)
				System.err.println("tree " + row + " attachment " + item + " to tree " + tNames[item]);
			
		}
		else if (checker.compare(this.getClass(), "Menu item selected for attachment for row", "[row][item]", commandName, "branchAttachMI")) {
			Parser parser = new Parser(arguments);
			int row = MesquiteInteger.fromString(parser);
			int item = MesquiteInteger.fromString(parser);
			MesquiteTree tree = (MesquiteTree)treesBlock.getTree(row);
			//Add tree attachments to popup
			String[] tNames = tree.getAssociatesTypesAndNames();

			if (tNames != null)
				System.err.println("tree " + row + " attachment " + item + " to branches " + tNames[item]);
			
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*==========================*/
	static int LONGS = 0;
	static int DOUBLES = 1;
	static int STRINGS = 2;
	static int BITS = 3;
	static int FLAGS = 4;
	static int OBJECTS = 5;

	class AttachmentReference implements Explainable {
		int type = -1;
		NameReference nr; //for associable attachments
		Class cl; // for whole tree attachments
		String name;
		boolean toTree = false;
		String explanation = null;
		
		public AttachmentReference(String name, Class cl, String explanation){
			this.name = name;
			this.cl = cl;
			toTree = true;
			this.explanation = explanation;
		}
		public AttachmentReference(NameReference nr, int typ, String explanation){
			this.nr = nr;
			this.type = typ;
			toTree = false;
			this.explanation = explanation;
		}
		public String getExplanation(){
			if (toTree){
				String s = name;
				if (!StringUtil.blank(explanation))
					s = explanation;
				return "Attached to tree: " + s + " type " + type + " cl " + cl;
			}
			if (nr == null)
				return null;
			String s = nr.getName();
			if (!StringUtil.blank(explanation))
				s = explanation;
			return "Attached to branches: " + s;
		}
		public String getName(){
			if (toTree){
				return "Attached to tree: " + name + " type " + type + " cl " + cl;
			}
			if (nr == null)
				return null;
			return "Attached to branches: " + nr.getName();
		}
		public boolean equals(AttachmentReference ir){
			if (toTree != ir.toTree || cl != ir.cl || type != ir.type) return false;
			if (name != null && ir.name != null && !name.equals(ir.name)) return false;
			if (nr != null && ir.nr != null && !nr.equals(ir.nr)) return false;
			if ((name == null && ir.name != null) || (name != null && ir.name == null)) return false;
			if ((nr == null && ir.nr != null) || (nr != null && ir.nr == null)) return false;
			return true;
		}
	}
	/*==========================*/
	void addIfNew(ListableVector vec, AttachmentReference ar){
		for (int i = 0; i<vec.size(); i++){
			AttachmentReference ir = (AttachmentReference)vec.elementAt(i);
			if (ir.equals(ar))
				return;
		}
		vec.addElement(ar, false);
	}
	
	String explanationIfPresent(Object obj){
		if (obj instanceof Explainable)
			return ((Explainable)obj).getExplanation();
		return null;
	}
	/* -------------------------*/
	ListableVector getAttachmentInfo(boolean allRegardless){
		if (treesBlock == null)
			return null;
		if (table == null && !allRegardless)
			return null;
		int col = ((ListModule)employer).getMyColumn(this);
		boolean anySelected = !allRegardless && table.anyCellsInColumnSelectedAnyWay(col);
		ListableVector vec = new ListableVector();
		//go through all or selected trees, accumulating all kinds of attachments
		for (int itr = 0; itr<treesBlock.size(); itr++){
			if (!anySelected || table.isCellSelectedAnyWay(col, itr)){
				MesquiteTree tree = (MesquiteTree)treesBlock.getTree(itr);
				Vector att = tree.getAttachments();
				if (att!= null)
					for (int i =0; i<att.size(); i++){
						Object obj = att.elementAt(i);
						if (obj instanceof Listable && ((Listable)obj).getName()!=null){
							addIfNew(vec, new AttachmentReference(((Listable)obj).getName(), obj.getClass(), explanationIfPresent(obj)));
						}
					}
				for (int i =0; i<tree.getNumberAssociatedLongs(); i++){
					LongArray array = tree.getAssociatedLongs(i);
					addIfNew(vec, new AttachmentReference(array.getNameReference(), LONGS, explanationIfPresent(array)));

				}
				for (int i =0; i<tree.getNumberAssociatedDoubles(); i++){
					DoubleArray array = tree.getAssociatedDoubles(i);
					addIfNew(vec, new AttachmentReference(array.getNameReference(), DOUBLES, explanationIfPresent(array)));
				}
				for (int i =0; i<tree.getNumberAssociatedStrings(); i++){
					StringArray array = tree.getAssociatedStrings(i);
					addIfNew(vec, new AttachmentReference(array.getNameReference(), STRINGS, explanationIfPresent(array)));
				}
				for (int i =0; i<tree.getNumberAssociatedBits(); i++){
					Bits array = tree.getAssociatedBits(i);
					addIfNew(vec, new AttachmentReference(array.getNameReference(), BITS, explanationIfPresent(array)));
				}
				for (int i =0; i<tree.getNumberAssociatedObjects(); i++){
					ObjectArray array = tree.getAssociatedObjects(i);
					addIfNew(vec, new AttachmentReference(array.getNameReference(), OBJECTS, explanationIfPresent(array)));
				}
			}
		}
		return vec;
	}
	/*==========================*
	boolean alreadyRepresentedByTreeAttachment(){
		Object obj = attachments.elementAt(i);
		if (obj instanceof MesquiteFlag){
			MesquiteFlag mf = (MesquiteFlag)obj;
			NameReference nr = NameReference.getNameReference(mf.getName());
			if (tree.getAssociatedObjects(nr) != null)
				total--;
			
		}
		return false;
	}
	/*.................................................................................................................*/
	public String getStringForTree(int ic){
		if (treesBlock==null)
			return "-";
		MesquiteTree tree = (MesquiteTree)treesBlock.getTree(ic);

		//calculate total number of attachments
		Vector attachments = tree.getAttachments();
		if (attachments == null)
			return "-";
		int total = attachments.size();
		//Calculate total number of node associates
		total += tree.getNumberAssociatedBits() +tree.getNumberAssociatedLongs() + tree.getNumberAssociatedDoubles() 
		+ tree.getNumberAssociatedStrings() + tree.getNumberAssociatedObjects();
		//But subtract associates of branches that are already represented by attached MesquiteFlag to the tree.
		if (attachments != null){
			for (int i = 0; i< attachments.size(); i++){
				Object obj = attachments.elementAt(i);
				if (obj instanceof MesquiteFlag){
					MesquiteFlag mf = (MesquiteFlag)obj;
					NameReference nr = NameReference.getNameReference(mf.getName());
					if (tree.getAssociatedObjects(nr) != null)
						total--;
					
				}
			}
		}
		if (total>0)
			return Integer.toString(total);
		return "-";
	}

	/*.................................................................................................................*/
	public boolean arrowTouchInRow(Graphics g, int ic, int x, int y, boolean doubleClick, int modifiers){ //so assistant can do something in response to arrow touch; return true if the event is to stop there, i.e. be intercepted
		if (table!= null){
			MesquitePopup popup = new MesquitePopup(table.getMatrixPanel());
			MesquiteTree tree = (MesquiteTree)treesBlock.getTree(ic);
			//Add tree attachments to popup
			popup.addItem("Attached to tree", (MesquiteCommand)null, null);
			String[] tNames = tree.getAttachentTypesAndExplanations();
			if (tNames != null)
				for (int i= 0; i<tNames.length; i++)
					popup.addItem(tNames[i], new MesquiteCommand("treeAttachMI", this), "" + ic + " " + i);
			else
				popup.addItem("Nothing", (MesquiteCommand)null, null);
			//Add node attachments to popup
			popup.addItem("-", (MesquiteCommand)null, null);
			popup.addItem("Attached to branches/nodes", (MesquiteCommand)null, null);
			tNames = tree.getAssociatesTypesAndNames();
			if (tNames != null)
				for (int i= 0; i<tNames.length; i++) {
					
					popup.addItem(tNames[i], new MesquiteCommand("branchAttachMI", this), "" + ic + " " + i);
				}
			else
				popup.addItem("Nothing", (MesquiteCommand)null, null);


			popup.showPopup(x, y);						
			return true;
		}
		return false;
	}

	public String getWidestString(){
		return " Attachments 88888 ";
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;    //release number
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
 	/*.................................................................................................................*/
  	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
  	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
  	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
     	public int getVersionOfFirstRelease(){
     		return NEXTRELEASE;  
     	}
}

