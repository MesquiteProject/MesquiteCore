/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.CharListPartition;
/*~~  */

import mesquite.lists.lib.*;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class CharListPartition extends CharListAssistant {
	/*.................................................................................................................*/
	public String getName() {
		return "Group Membership (characters)";
	}
	public String getExplanation() {
		return "Lists and allows changes to group membership in the current partition of characters, for List of Characters window." ;
	}
	/*.................................................................................................................*/
	CharacterData data=null;
	MesquiteTable table=null;
	MesquiteSubmenuSpec mss, mEGC, mDGC, mEGN;
	MesquiteMenuItemSpec mScs, mStc, mRssc, mLine, nNG, mLine2, mss2;
	CharactersGroupVector groups;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		groups = (CharactersGroupVector)getProject().getFileElement(CharactersGroupVector.class, 0);
		groups.addListener(this);
		return true;
	}
	public void endJob(){
		if (data != null)
			data.removeListener(this);
		if (groups != null)
			groups.removeListener(this);
		super.endJob();
	}
	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	private void setPartition(CharactersGroup group, String arguments){
		if (table !=null && data!=null) {
			boolean changed=false;
			String name = parser.getFirstToken(arguments);
			if (group == null && StringUtil.blank(name))
				return;
			CharacterPartition partition = (CharacterPartition) data.getCurrentSpecsSet(CharacterPartition.class);
			if (partition==null){
				partition= new CharacterPartition("Partition", data.getNumChars(), null, data);
				partition.addToFile(data.getFile(), getProject(), findElementManager(CharacterPartition.class));
				data.setCurrentSpecsSet(partition, CharacterPartition.class);
			}
			if (group == null){
				CharactersGroupVector groups = (CharactersGroupVector)getProject().getFileElement(CharactersGroupVector.class, 0);
				Object obj = groups.getElement(name);
				group = (CharactersGroup)obj;
			}
			if (group != null) {
				if (partition != null) {
					if (employer!=null && employer instanceof ListModule) {
						int c = ((ListModule)employer).getMyColumn(this);
						for (int i=0; i<data.getNumChars(); i++) {
							if (table.isCellSelectedAnyWay(c, i)) {
								partition.setProperty(group, i);
								if (!changed)
									outputInvalid();
								changed = true;
							}
						}
					}
				}
				if (changed)
					data.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED)); //TODO: bogus! should notify via specs not data???
							outputInvalid();
							parametersChanged();
			}
		}
	}
	private void removePartition(){
		if (table !=null && data!=null) {
			boolean changed=false;
			CharacterPartition partition = (CharacterPartition) data.getCurrentSpecsSet(CharacterPartition.class);
			if (partition!=null){
				if (employer!=null && employer instanceof ListModule) {
					int c = ((ListModule)employer).getMyColumn(this);
					for (int i=0; i<data.getNumChars(); i++) {
						if (table.isCellSelectedAnyWay(c, i)) {
							partition.setProperty(null, i);
							if (!changed)
								outputInvalid();
							changed = true;
						}
					}
				}

				if (changed)
					data.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED)); //TODO: bogus! should notify via specs not data???
							outputInvalid();
							parametersChanged();

			}
		}
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the character group of the selected characters", "[name of group]", commandName, "setPartition")) {
			setPartition(null, arguments);
		}
		else if (checker.compare(this.getClass(), "Edits the name and color of a character group label", "[name of group]", commandName, "editGroup")) {
			String name = parser.getFirstToken(arguments);
			if (StringUtil.blank(name))
				return null;
			String num = parser.getNextToken();
			Object obj = CharListPartitionUtil.editGroup(this, data,containerOfModule(),name, num);
			if (obj!=null) {
				outputInvalid();
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Deletes a character group label", "[name of group]", commandName, "deleteGroup")) {
			String name = parser.getFirstToken(arguments);
			if (StringUtil.blank(name))
				return null;
			String num = parser.getNextToken();
			boolean b = CharListPartitionUtil.deleteGroup(this, data,containerOfModule(),name, num);
			if (b) {
				outputInvalid();
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Creates a new group for use in character partitions", null, commandName, "newGroup")) {
			MesquiteString ms = new MesquiteString("");
			CharactersGroup group = CharListPartitionUtil.makeGroup(this,data,containerOfModule(), ms);
			if (group==null) return null;
			setPartition(group, ms.getValue());
		}
		else if (checker.compare(this.getClass(), "Stores the current character partition set", null, commandName, "storeCurrent")) {
			if (data!=null){
				SpecsSetVector ssv = data.getSpecSetsVector(CharacterPartition.class);
				if (ssv == null || ssv.getCurrentSpecsSet() == null) {
					CharacterPartition partition= new CharacterPartition("Partition", data.getNumChars(), null, data);
					partition.addToFile(data.getFile(), getProject(), findElementManager(CharacterPartition.class));
					data.setCurrentSpecsSet(partition, CharacterPartition.class);
					ssv = data.getSpecSetsVector(CharacterPartition.class);
				}
				if (ssv!=null) {
					SpecsSet s = ssv.storeCurrentSpecsSet();
					if (s.getFile() == null)
						s.addToFile(data.getFile(), getProject(), findElementManager(CharacterPartition.class));
					s.setName(ssv.getUniqueName("Partition"));
					String name = MesquiteString.queryString(containerOfModule(), "Name", "Name of character partition to be stored", s.getName());
					if (!StringUtil.blank(name))
						s.setName(name);
					ssv.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED));  
				}
				else MesquiteMessage.warnProgrammer("sorry, can't store because no specssetvector");
			}
			//return ((ListWindow)getModuleWindow()).getCurrentObject();
		}
		else if (checker.compare(this.getClass(), "Replaces a stored character partition set by the current one", null, commandName, "replaceWithCurrent")) {
			if (data!=null){
				SpecsSetVector ssv = data.getSpecSetsVector(CharacterPartition.class);
				if (ssv!=null) {
					SpecsSet chosen = (SpecsSet)ListDialog.queryList(containerOfModule(), "Replace stored set", "Choose stored partition to replace by current set",MesquiteString.helpString, ssv, 0);
					if (chosen!=null){
						SpecsSet current = ssv.getCurrentSpecsSet();
						ssv.replaceStoredSpecsSet(chosen, current);
					}
				}

				outputInvalid();
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Loads the stored character partition to be the current one", "[number of partition to load]", commandName, "loadToCurrent")) {
			if (data !=null) {
				int which = MesquiteInteger.fromFirstToken(arguments, stringPos);
				if (MesquiteInteger.isCombinable(which)){
					SpecsSetVector ssv = data.getSpecSetsVector(CharacterPartition.class);
					if (ssv!=null) {
						SpecsSet chosen = ssv.getSpecsSet(which);
						if (chosen!=null){
							ssv.setCurrentSpecsSet(chosen.cloneSpecsSet()); 
							data.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED)); //TODO: bogus! should notify via specs not data???
							return chosen;
						}
					}
				}
				outputInvalid();
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Removes the group designation from the selected characters", null, commandName, "removeGroup")) {
			removePartition();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		/* hire employees here */
		deleteMenuItem(mss);
		deleteMenuItem(mss2);
		deleteMenuItem(mScs);
	//	deleteMenuItem(mScsPF);
		deleteMenuItem(mRssc);
		deleteMenuItem(mLine);
		deleteMenuItem(mLine2);
		deleteMenuItem(mStc);
		deleteMenuItem(nNG);
		deleteMenuItem(mEGC);
		deleteMenuItem(mDGC);
		deleteMenuItem(mEGN);
		deleteMenuItem(nNG);
		mss = addSubmenu(null, "Set Group", makeCommand("setPartition", this));
		mss.setList((StringLister)getProject().getFileElement(CharactersGroupVector.class, 0));
		mss2 = addMenuItem("Remove Group Designation", makeCommand("removeGroup", this));
		mLine2 = addMenuSeparator();
		nNG = addMenuItem("New Group...", makeCommand("newGroup",  this));
		mEGC = addSubmenu(null, "Edit Group...", makeCommand("editGroup", this));
		mEGC.setList((StringLister)getProject().getFileElement(CharactersGroupVector.class, 0));
		mDGC = addSubmenu(null, "Delete Group...", makeCommand("deleteGroup", this));
		mDGC.setList((StringLister)getProject().getFileElement(CharactersGroupVector.class, 0));
		mLine = addMenuSeparator();
		mScs = addMenuItem("Store current partition...", makeCommand("storeCurrent",  this));
		mRssc = addMenuItem("Replace stored partition by current", makeCommand("replaceWithCurrent",  this));
		if (data !=null)
			mStc = addSubmenu(null, "Load set", makeCommand("loadToCurrent",  this), data.getSpecSetsVector(CharacterPartition.class));
		//mScsPF = addMenuItem("Create Partition Based upon RAxML Format...", makeCommand("createByRAxML",  this));
		this.data = data;
		this.table = table;
		if (data != this.data){
			if (this.data != null)
			this.data.removeListener(this);
			data.addListener(this);
		}
	}
	public void changed(Object caller, Object obj, Notification notification){
		if (caller == this)
			return;
		outputInvalid();
		parametersChanged(notification);
	}
	public String getTitle() {
		return "Group";
	}
	public String getStringForCharacter(int ic){
		if (data!=null) {
			CharacterPartition partition = (CharacterPartition)data.getCurrentSpecsSet(CharacterPartition.class);
			if (partition != null) {
				CharactersGroup group = (CharactersGroup)partition.getProperty(ic);
				if (group!=null) {
					return group.getName();
				}
			}
		}
		return "?";
	}
	public boolean useString(int ic){
		return false;
	}
	public void drawInCell(int ic, Graphics g, int x, int y,  int w, int h, boolean selected){
		if (data==null || g==null)
			return;
		boolean colored = false;
		Color c = g.getColor();
		CharacterPartition part = (CharacterPartition)data.getCurrentSpecsSet(CharacterPartition.class);
		if (part!=null) {
			CharactersGroup tg = part.getCharactersGroup(ic);
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
			if (c!=null) g.setColor(c);
		}
	}
	public String getWidestString(){
		if (data!=null) {
			int length = 20;
			String longest = null;
			CharacterPartition partition = (CharacterPartition)data.getCurrentSpecsSet(CharacterPartition.class);
			if (partition != null) {
				for (int ic= 0; ic< data.getNumChars(); ic++){
					CharactersGroup group = (CharactersGroup)partition.getProperty(ic);
					if (group!=null) {
						String s = group.getName();
						if (s != null)
							if (s.length()> length) {  //just counting string length to avoid font metrics calculations
								length = s.length();
								longest = s;
							}
					}
				}
				if (longest !=null)
					return longest;
			}
		}
		return "Partition     ";
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}

	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}
}


