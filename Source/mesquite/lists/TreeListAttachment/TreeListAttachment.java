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

import mesquite.lists.lib.*;
import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class TreeListAttachment extends TreeListAssistant {
	/*.................................................................................................................*/
	public String getName() {
		return "Attachment";
	}
	public String getExplanation() {
		return "Displays an item attached to trees." ;
	}
	TreeVector treesBlock;
	String nameOfAttached = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
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
		if (checker.compare(this.getClass(), "Sets what attachment to show", "[name of attached to show]", commandName, "setAttached")) {
			nameOfAttached = parser.getFirstToken(arguments);
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	public void setTableAndTreeBlock(MesquiteTable table, TreeVector trees){
		treesBlock = trees;
		if (!MesquiteThread.isScripting()){
			ListableVector v = new ListableVector();
			if (nameOfAttached == null && treesBlock !=null){
				for (int itree =0; itree < treesBlock.size(); itree++){
					Tree tree = treesBlock.getTree(itree);
					if (tree !=null && tree instanceof Attachable){
						Vector at = ((Attachable)tree).getAttachments();
						if (at !=null) {
							for (int i =0; i < at.size(); i++){
								Object obj = at.elementAt(i);
								if (obj instanceof Listable) {
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
				if (v.size()==0)
					alert("Sorry, there is nothing attached to the trees to show");
				else {
					Listable L = ListDialog.queryList(containerOfModule(), "Select item", "What attachments to tree should be shown?", MesquiteString.helpString, v, 0);
					if (L!=null)
						nameOfAttached = L.getName();
				} 
			}
		}
	}
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

