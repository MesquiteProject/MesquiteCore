/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 



Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.AlterData; 

import java.util.*;
import java.awt.*;
import java.awt.image.*;

import mesquite.align.lib.AlignMultipleSequencesMachine;
import mesquite.align.lib.MultipleSequenceAligner;
import mesquite.categ.lib.MolecularData;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.lib.ui.MesquiteMenuItemSpec;
import mesquite.lib.ui.MesquiteMenuSpec;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.lib.ui.MesquiteWindow;

/* ======================================================================== */
public class AlterData extends DataWindowAssistantI  {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(DataAlterer.class, getName() + " needs a particular method to alter data in the Character Matrix Editor.",
				"These options are available in the Alter submenu of the Matrix menu of the Character Matrix Editor");
		e2.setPriority(2);
	}
	MesquiteTable table;
	CharacterData data;
	MesquiteSubmenuSpec mss= null;
	MesquiteMenuSpec alterMenu; 
	MesquiteMenuSpec alterMenu2; 
	
	/*
	 *January 2025:  Special handling of alignment stripped from here, with consequence that all alterers are assumed to operate on main thread.
	 *In future could allow alterers to work on separate threads, but would need to require alterers to indicate whether they want to work on a separate thread.
	 * For the moment, it's assumed they work on one thread
	boolean separateThread = false;
	
	implements CalculationMonitor, SeparateThreadStorage
*/
	
//	MesquiteCMenuItemSpec bySMmi; 

	//Specify various interfaces here
	String[] labels = new String[]{ "Whole Character Add/Remove", "Align/Shift/Reverse Data", "Transformations", "Metadata", "Basic Data Cell Manipulation", "DNA/RNA Data Cell Manipulation", "Convert Gap/Missing/Polymorph/Uncertain", "Randomizations"};
	Class[] interfaces = new Class[]{AltererWholeCharacterAddRemove.class, AltererAlignShift.class, AltererContinuousTransformations.class, AltererMetadata.class, AltererSimpleCell.class, AltererDNACell.class, AltererConvertGapMissPolyUncert.class,  AltererRandomizations.class};
	boolean[] asSubmenu = new boolean[]{false,false,false, true, true, true, true, true};


	MesquiteSubmenuSpec[] submenu = new MesquiteSubmenuSpec[interfaces.length+1];
	MesquiteSubmenuSpec otherInterfaces= null;
	OtherAltererQualificationsTest qualificationsTest;
	MesquiteBoolean bySubmenus;
	boolean jobDone=false;
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		qualificationsTest = new OtherAltererQualificationsTest(interfaces);
		bySubmenus = new MesquiteBoolean(false);
		alterMenu = makeMenu("Alter");
		buildMenu();

		return true;
	}

	/*.................................................................................................................*/
	void buildMenu(){
		deleteAllMenuItems();
		
		
		for (int i=0; i< interfaces.length; i++) {
			if (!asSubmenu[i])
				addItemsOfInterface(alterMenu, interfaces[i], labels[i], null);
		}
		
		addMenuItem(alterMenu, "-", null);

		for (int i=0; i< interfaces.length; i++) {
			if (asSubmenu[i])
				addSubmenu(alterMenu, labels[i], makeCommand("doAlter",  this), interfaces[i]);
		}

		submenu[interfaces.length] = addSubmenu(alterMenu, "Other Alterations", makeCommand("doAlter",  this), DataAlterer.class);
		submenu[interfaces.length].setQualificationsTest(qualificationsTest);
		resetContainingMenuBar();
	}
	/*.................................................................................................................*/
	void setCompatibilityForMatrix(){
		if (data == null)
			return;

	if (bySubmenus.getValue()){
			//SUBMENUS
			for (int i=0; i< submenu.length; i++)
				submenu[i].setCompatibilityCheck(data.getStateClass());
		}
		else {
			//GROUPS
			alterMenu.setCompatibilityCheck(data.getStateClass());
		}
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
	
	private void addItemsOfInterface(MesquiteMenuSpec menu, Class alterInterface, String title, QualificationsTest qualificationsTest){
		Class dataClass  = null;
		if (data !=null)
			dataClass = data.getClass();
		int numModules = MesquiteTrunk.mesquiteModulesInfoVector.getNumModulesOfDuty(alterInterface, dataClass, this);
		if (numModules <=0)
			return;
		if (getNumMenuItemSpecs()>0) {
			addMenuItem(menu, "-", null);
		}
		addMenuItem(menu, title, null);
		MesquiteMenuItemSpec mmis = addModuleMenuItems(menu, makeCommand("doAlter",  this), alterInterface);
		mmis.setQualificationsTest(qualificationsTest);


	}
	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = table;
		this.data = data;
		setCompatibilityForMatrix();
		resetContainingMenuBar();
		if (data!=null)
			data.addListener(this);
		buildMenu();
	}

	/* possible alterers:
		recode (as in MacClade)
		reverse (sequences) �
		shuffle �
		random fill �
		fill (via dialog) �
		search and replace

	DNA only:
		complement �

	continuous only:
		log transform �
		scale �
		standardize (mean 0, variance 1)
	 */
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
 	 public Snapshot getSnapshot(MesquiteFile file) {
  	 	Snapshot temp = new Snapshot();
		temp.addLine("toggleBySubmenus " + bySubmenus.toOffOnString());
 	 	return temp;
 	 }
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Hires module to alter the data matrix", "[name of module]", commandName, "doAlter")) {
			if (table!=null && data !=null){
				if (data.isEditInhibited()){
					discreetAlert("This matrix is marked as locked against editing. To unlock, uncheck the menu item Matrix>Current Matrix>Editing Not Permitted");
					return null;
				}
				DataAlterer tda= (DataAlterer)hireNamedEmployee(DataAlterer.class, arguments);
				
				if (tda!=null) {
					MesquiteWindow w = table.getMesquiteWindow();
					UndoReference undoReference = new UndoReference();
					AlteredDataParameters alteredDataParameters = new AlteredDataParameters();
					if (MesquiteTrunk.debugMode)
						logln("Memory available before data alterer invoked: " + MesquiteTrunk.getMaxAvailableMemory());
					int a = tda.alterData(data, table, undoReference, alteredDataParameters);
					if (MesquiteTrunk.debugMode)
						logln("Memory available after data alterer invoked: " + MesquiteTrunk.getMaxAvailableMemory());

					if (a== DataAlterer.SUCCEEDED) {
						table.repaintAll();
						Notification notification = new Notification(MesquiteListener.DATA_CHANGED, alteredDataParameters.getParameters(), undoReference);
						if (alteredDataParameters.getSubcodes()!=null)
							notification.setSubcodes(alteredDataParameters.getSubcodes());
						data.notifyListeners(this, notification);
					}
					if (!tda.pleaseLeaveMeOn()) {  
						fireEmployee(tda); //Note: this assumes the alterer completes the job, i.e. is on same thread
					}
				}
			}
		}
	 	else if (checker.compare(this.getClass(), "Toggles whether shows by submenus", "[on or off]", commandName, "toggleBySubmenus")) {
	 		boolean current = bySubmenus.getValue();
	 		bySubmenus.toggleValue(parser.getFirstToken(arguments));
	 		if (current!=bySubmenus.getValue()){
	 			buildMenu();
	 			setCompatibilityForMatrix();
	 			resetContainingMenuBar();
	 		}
	 	}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	

	/*.................................................................................................................*/
	public String getName() {
		return "Alter Data";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Manages data-transforming modules." ;
	}

}

class OtherAltererQualificationsTest extends QualificationsTest {
	Class[] interfaces;
	public OtherAltererQualificationsTest(Class[] interfaces){
		this.interfaces = interfaces;
	}
	public  boolean isQualified(Object prospectiveEmployee){  //Object either a MesquiteModule or a MesquiteModuleInfo
		if (prospectiveEmployee instanceof MesquiteModule){
			MesquiteModule mb = (MesquiteModule)prospectiveEmployee;
			for (int i=0; i< interfaces.length; i++){
				if (interfaces[i].isInstance(mb))
					return false;
			}
		}
		else if (prospectiveEmployee instanceof MesquiteModuleInfo){
			MesquiteModuleInfo mbi = (MesquiteModuleInfo)prospectiveEmployee;
			for (int i=0; i< interfaces.length; i++){
				if (interfaces[i].isAssignableFrom(mbi.getModuleClass()))
					return false;
			}


		}
		return true;
	}
}

