/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.BasicDataWindowCoord; 
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/** Coordinates the display of the spreadsheet editor window for character matrices.  This doesn't actually make the window (see BasicDataWindowMaker). */
public class BasicDataWindowCoord extends FileInit {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(DataWindowMaker.class, "Character Matrix Editors display character matrices to the user.", 
		"You may request a character matrix editor by selecting the Character Matrix item under the Characters menu");

		e.setAsEntryPoint("showDataWindow " + 0);
	}
	MesquiteSubmenuSpec elementsSubmenu, newViewSubmenu;
	MesquiteMenuItemSpec cadw = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		elementsSubmenu = getFileCoordinator().addSubmenu(MesquiteTrunk.charactersMenu, "Character Matrix Editor", makeCommand("showDataWindow",  this));
		elementsSubmenu.setBehaviorIfNoChoice(MesquiteSubmenuSpec.ONEMENUITEM_ZERODISABLE);
		newViewSubmenu = getFileCoordinator().addSubmenu(MesquiteTrunk.charactersMenu, "Extra Matrix Editor", makeCommand("showExtraDataWindow",  this));
		newViewSubmenu.setBehaviorIfNoChoice(MesquiteSubmenuSpec.ONEMENUITEM_ZERODISABLE);
		cadw = getFileCoordinator().addMenuItem(MesquiteTrunk.charactersMenu, "Close All Character Matrix Editors", makeCommand("closeAllMatrixWindows",  this));
		cadw.setEnabled(false);
		return true;
	}
	/*.................................................................................................................*/
	/** A method called immediately after the file has been read in.*/
	public void projectEstablished() {
		elementsSubmenu.setList( getProject().datas);
		newViewSubmenu.setList( getProject().datas);
	}
	String dataRef(CharacterData d, boolean internal){
		if (internal)
			return getProject().getCharMatrixReferenceInternal(d);
		return getProject().getCharMatrixReferenceExternal(d);
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}
	/*.................................................................................................................*/
	/**Returns command to hire employee if clonable*/
	public String getClonableEmployeeCommand(MesquiteModule employee){
		if (employee!=null && employee.getEmployer()==this) {
			if (employee.getHiredAs()==DataWindowMaker.class) {
				CharacterData d = (CharacterData)employee.doCommand("getDataSet", null, CommandChecker.defaultChecker);
				if (d != null) {
					return ("showDataWindow " + dataRef(d, true) + "  " + StringUtil.tokenize(employee.getName()) + ";");//quote
				}
			}
		}
		return null;
	}
	/* ................................................................................................................. */
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (employee instanceof DataWindowMaker){
			if (cadw != null) cadw.setEnabled(getNumMatrixWindows()>0);
			resetAllWindowsMenus();
		}
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		Vector datasShown = new Vector();
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof DataWindowMaker) {
				DataWindowMaker dwm = (DataWindowMaker)e;
				CharacterData d = (CharacterData)dwm.doCommand("getDataSet", null, CommandChecker.defaultChecker);
				if (d != null && (d.getWritable() || file == null)) {
					if (datasShown.indexOf(d)>=0)
						temp.addLine("showExtraDataWindow " + dataRef(d, false), dwm);
					else {
						temp.addLine("showDataWindow " + dataRef(d, false), dwm);
						datasShown.addElement(d);
					}
				}
			}
		}
		datasShown.removeAllElements();
		return temp;
	}
	/*.................................................................................................................*/
	MesquiteInteger pos = new MesquiteInteger();
	CharacterData findData(String arguments, MesquiteFile file){
		CharacterData data = null;
		if (StringUtil.blank(arguments)){
			ListableVector datas = getProject().getCharacterMatrices();
			int numDatas = 0;
			for (int i=0; i<datas.size(); i++)
				if (((CharacterData)datas.elementAt(i)).isUserVisible()){
					data = (CharacterData)datas.elementAt(i);
					numDatas++;
				}
			//if only one taxa block, use it
			if (numDatas<=0){
				return null;
			}
			else if (numDatas==1){
				return data;
			}
			else {
				data =  getProject().chooseData(containerOfModule(), null, null, "Which data matrix would you like to display in an data editor window?");
				//else, query user
			}
		}
		else { 
			// in general, will only show user visible matrices.  However, if arguments starts with #, then assume a direct, non-numbered request that will be obeyed even if not user visible
			data =  getProject().getCharacterMatrixByReference(file, parser.getFirstToken(arguments), !arguments.startsWith("#"));
		}
		return data;
	}

	DataWindowMaker findEditor(CharacterData data){
		//next, find if already has DataWindowMaker module for it
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof DataWindowMaker) {
				DataWindowMaker dwm = (DataWindowMaker)e;
				CharacterData d = dwm.getCharacterData();
				if (d == data) {
					if (dwm.getModuleWindow() !=null) {
						dwm.getModuleWindow().setVisible(true);
						dwm.getModuleWindow().setShowExplanation(true);
						dwm.getModuleWindow().toFront();
					}
					return dwm;
				}
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	/** Generated by an employee who quit.  The MesquiteModule should act accordingly. */
	public void employeeQuit(MesquiteModule employee) {
		if (employee instanceof DataWindowMaker) {  // character source quit and none rehired automatically
			DataWindowMaker dwm = (DataWindowMaker)employee;
			unlinkEditors(dwm.getCharacterData(), dwm);
			if (cadw != null) cadw.setEnabled(getNumMatrixWindows()>0);
			resetAllMenuBars();

		}
	}
	
	int getNumMatrixWindows(){
		int count = 0;
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof DataWindowMaker && ((DataWindowMaker) e).getModuleWindow().isVisible()) 
				count++;
		}
		return count;
	}
	void linkEditors(CharacterData data, DataWindowMaker newDWM){
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof DataWindowMaker && e != newDWM) {
				DataWindowMaker dwm = (DataWindowMaker)e;
				CharacterData d = dwm.getCharacterData();
				if (d == data) {
					dwm.linkEditor(newDWM, true);
					newDWM.linkEditor(dwm, false);
				}
			}
		}
	}
	void unlinkEditors(CharacterData data, DataWindowMaker newDWM){
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof DataWindowMaker && e != newDWM) {
				DataWindowMaker dwm = (DataWindowMaker)e;
				CharacterData d = dwm.getCharacterData();
				if (d == data) {
					dwm.unlinkEditor(newDWM);
					newDWM.unlinkEditor(dwm);
				}
			}
		}
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Shows the data editor window.  If a data editor window for this matrix already exists, it is brought to the front", "[number of data matrix to be shown] [name of data matrix to be shown]", commandName, "showDataWindow")) {  //IF WINDOW ALREADY SHOWN, JUST BRING IT TO FRONT
			//first, find the data set requested
			CharacterData data = findData(arguments, checker.getFile());
			if (data == null)
				return null;
			/**/
			DataWindowMaker dwm = findEditor(data);
			if (dwm!=null){
				if (cadw != null) cadw.setEnabled(true);
				//resetAllMenuBars();
				return dwm;
			}

			//if no data window module active, hire one
			DataWindowMaker mb = (DataWindowMaker)hireEmployee(DataWindowMaker.class, null);
			if (mb!=null) {
				mb.setAsExtra(false);
				mb.doCommand("makeWindow", getProject().getCharMatrixReferenceInternal(data), checker);
			}
			if (cadw != null) cadw.setEnabled(true);
			resetAllMenuBars();
		return mb;
		}
		else if (checker.compare(this.getClass(), "Shows an extra data editor window.", "[number of data matrix to be shown] [name of data matrix to be shown]", commandName, "showExtraDataWindow")) {  //IF WINDOW ALREADY SHOWN, JUST BRING IT TO FRONT
			//first, find the data set requested
			CharacterData data = findData(arguments, checker.getFile());
			if (data == null)
				return null;

			//if no data window module active, hire one
			DataWindowMaker mb = (DataWindowMaker)hireEmployee(DataWindowMaker.class, null);
			if (mb!=null) {
				mb.setAsExtra(true);
				mb.doCommand("makeWindow", getProject().getCharMatrixReferenceInternal(data), checker);
				linkEditors(data, mb);
			}
			if (cadw != null) cadw.setEnabled(true);
			resetAllMenuBars();
			return mb;
		}
		else if (checker.compare(this.getClass(), "Closes all character matrix editor windows", null, commandName, "closeAllMatrixWindows")) {
			for (int i = getNumberOfEmployees()-1; i>=0; i--) {
				Object e=getEmployeeVector().elementAt(i);
				if (e instanceof DataWindowMaker) {
					DataWindowMaker dwm = (DataWindowMaker)e;
					dwm.windowGoAway(dwm.getModuleWindow());
				}
			}
			if (cadw != null) cadw.setEnabled(false);
			resetAllMenuBars();
			return null;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Data Window Coordinator";
	}

	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Coordinates the creation of basic data windows." ;
	}

}


