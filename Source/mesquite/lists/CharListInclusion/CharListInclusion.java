/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.CharListInclusion;
/*~~  */

import mesquite.lists.lib.*;
import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class CharListInclusion extends CharListAssistant {
	/*.................................................................................................................*/
	public String getName() {
		return "Character Inclusion";
	}

	public String getExplanation() {
		return "Shows current character inclusion in character list window." ;
	}

	CharacterData data=null;
	MesquiteTable table=null;
	Image included, excluded;
	MesquiteMenuItemSpec mss, mScs, mStc, mRssc, mLine;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addMenuItem("Include", makeCommand("include", this));
		addMenuItem("Exclude", makeCommand("exclude", this));
		addMenuItem("Reverse", makeCommand("reverse", this));
		addMenuSeparator();
		return true;
	}
	void doChange(int which){
		if (table !=null && data!=null) {
			boolean changed=false;

			CharInclusionSet inclusionSet = (CharInclusionSet) data.getCurrentSpecsSet(CharInclusionSet.class);
			if (inclusionSet == null) {
				inclusionSet= new CharInclusionSet("Inclusion Set", data.getNumChars(), data);
				inclusionSet.selectAll();
				inclusionSet.addToFile(data.getFile(), getProject(), findElementManager(CharInclusionSet.class)); //THIS
				data.setCurrentSpecsSet(inclusionSet, CharInclusionSet.class);
			}
			if (inclusionSet != null) {
				if (employer!=null && employer instanceof ListModule) {
					int c = ((ListModule)employer).getMyColumn(this);
					for (int i=0; i<data.getNumChars(); i++) {
						if (table.isCellSelectedAnyWay(c, i)) {
							if (which==0) //include
								inclusionSet.setSelected(i, true);
							else if (which==1) //exclude
								inclusionSet.setSelected(i, false);
							else //reverse
								inclusionSet.setSelected(i, !inclusionSet.isBitOn(i));
							changed = true;
						}
					}
				}
			}


			if (changed)
				data.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  //not quite kosher; HOW TO HAVE MODEL SET LISTENERS??? -- modelSource
			parametersChanged();
		}
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the selected characters to included", null, commandName, "include")) {
			doChange(0);
		}
		else if (checker.compare(this.getClass(), "Sets the selected characters to excluded", null, commandName, "exclude")) {
			doChange(1);
		}
		else if (checker.compare(this.getClass(), "Reverses the inclusion status of the selected characters", null, commandName, "reverse")) {
			doChange(2);
		}
		else if (checker.compare(this.getClass(), "Stores the current inclusion status of all the characters as a character inclusion set", null, commandName, "storeCurrent")) {
			if (data!=null){
				SpecsSetVector ssv = data.getSpecSetsVector(CharInclusionSet.class);
				if (ssv == null || ssv.getCurrentSpecsSet() == null) {
					CharInclusionSet inclusionSet= new CharInclusionSet("Inclusion Set", data.getNumChars(), data);
					inclusionSet.selectAll();
					inclusionSet.addToFile(data.getFile(), getProject(), findElementManager(CharInclusionSet.class)); //THIS
					data.setCurrentSpecsSet(inclusionSet, CharInclusionSet.class);
					ssv = data.getSpecSetsVector(CharInclusionSet.class);
				}
				if (ssv!=null) {
					SpecsSet s = ssv.storeCurrentSpecsSet();
					if (s.getFile() == null)
						s.addToFile(data.getFile(), getProject(), findElementManager(CharInclusionSet.class));
					s.setName(ssv.getUniqueName("Inclusion Set"));
					String name = MesquiteString.queryString(containerOfModule(), "Name", "Name of character inclusion set to be stored", s.getName());
					if (!StringUtil.blank(name))
						s.setName(name);
					ssv.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED));  
				}
				else MesquiteMessage.warnProgrammer("sorry, can't store because no specssetvector");
			}
		}
		else if (checker.compare(this.getClass(), "Replaces a stored character inclusion set by the current inclusion status of all the characters", null, commandName, "replaceWithCurrent")) {
			if (data!=null){
				SpecsSetVector ssv = data.getSpecSetsVector(CharInclusionSet.class);
				if (ssv!=null) {
					SpecsSet chosen = (SpecsSet)ListDialog.queryList(containerOfModule(), "Replace stored set", "Choose stored character inclusion set to replace by current set", MesquiteString.helpString,ssv, 0);
					if (chosen!=null){
						SpecsSet current = ssv.getCurrentSpecsSet();
						ssv.replaceStoredSpecsSet(chosen, current);
					}
				}

			}
		}
		else if (checker.compare(this.getClass(), "Loads the stored inclusion set to be the current one", "[number of inclusion set to load]", commandName, "loadToCurrent")) {
			if (data !=null) {
				int which = MesquiteInteger.fromFirstToken(arguments, stringPos);
				if (MesquiteInteger.isCombinable(which)){
					SpecsSetVector ssv = data.getSpecSetsVector(CharInclusionSet.class);
					if (ssv!=null) {
						SpecsSet chosen = ssv.getSpecsSet(which);
						if (chosen!=null){
							ssv.setCurrentSpecsSet(chosen.cloneSpecsSet()); 
							data.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED)); //TODO: bogus! should notify via specs not data???
							return chosen;
						}
					}
				}
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		deleteMenuItem(mss);
		deleteMenuItem(mScs);
		deleteMenuItem(mRssc);
		deleteMenuItem(mLine);
		deleteMenuItem(mStc);
		mScs = addMenuItem("Store current set", makeCommand("storeCurrent",  this));
		mRssc = addMenuItem("Replace stored set by current", makeCommand("replaceWithCurrent",  this));
		if (data !=null)
			mStc = addSubmenu(null, "Load set", makeCommand("loadToCurrent",  this), data.getSpecSetsVector(CharInclusionSet.class));
		this.data = data;
		this.table = table;
	}
	public void changed(Object caller, Object obj, Notification notification){
		if (Notification.appearsCosmetic(notification))
			return;
		parametersChanged(notification);
	}
	public String getTitle() {
		return "Inclusion";
	}

	int timesDrawn = 0;
	public void drawInCell(int ic, Graphics g, int x, int y,  int w, int h, boolean selected){
		if (data!=null) {
			Color c = g.getColor();
			CharInclusionSet inclusionSet = (CharInclusionSet) data.getCurrentSpecsSet(CharInclusionSet.class);
			Shape clip = g.getClip();
			g.setClip(x,y,w,h);
			if (inclusionSet == null || inclusionSet.isBitOn(ic)) {
				int pointX = x+(w)/2 - 2;
				int pointY =y+ (h)/2 + 3;
				if (selected)
					g.setColor(ColorDistribution.darkGreen);
				else
					g.setColor(ColorDistribution.lightGreen);
				g.fillRect(x+1,y+1,w,h);
				if (selected)
					g.setColor(Color.white);
				else
					g.setColor(Color.black);
				thickLine(g,pointX,pointY, pointX+8, pointY-8);
				thickLine(g,pointX,pointY, pointX-4, pointY-4);
			}
			else {
				if (selected)
					g.setColor(ColorDistribution.darkRed);
				else
					g.setColor(ColorDistribution.lightRed);
				g.fillRect(x+1,y+1,w,h);
				int pointX = x+(w)/2;
				int pointY =y+ (h)/2;
				if (selected)
					g.setColor(Color.white);
				else
					g.setColor(Color.black);
				thickLine(g,pointX-4,pointY+4, pointX+4, pointY-4);
				thickLine(g,pointX+4,pointY+4, pointX-4, pointY-4);
			}

			if (c!=null) g.setColor(c);
			g.setClip(clip);
		}
	}
	void thickLine(Graphics g, int x1, int y1, int x2, int y2){
		for (int i=0; i<2; i++) {
			g.drawLine(x1+i,y1, x2+i, y2);
			g.drawLine(x1,y1, x2, y2);
			g.drawLine(x1,y1+i, x2, y2+i);
			g.drawLine(x1+i,y1+i, x2+i, y2+i);
		}
	}
	public  boolean useString(int ic){
		return false;
	}
	public  String getStringForRow(int ic){
		try{
			CharInclusionSet inclusionSet = (CharInclusionSet) data.getCurrentSpecsSet(CharInclusionSet.class);
			if (inclusionSet == null || inclusionSet.isBitOn(ic)) {
				return "included";
			}
			else {
				return "excluded";
			}
		}
		catch (NullPointerException e){
		}
		return "";
	}
	public int getColumnWidth(){
		return 16;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}
}

