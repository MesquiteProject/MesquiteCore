/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.align.AlignSequencesCodon;
/*~~  */


import mesquite.align.lib.MultipleSequenceAligner;
import mesquite.categ.lib.MolecDataEditorInit;
import mesquite.categ.lib.MolecularData;
import mesquite.lib.CommandChecker;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.Notification;
import mesquite.lib.ResultCodes;
import mesquite.lib.UndoReference;
import mesquite.lib.characters.AlteredDataParameters;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.DataAlterer;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.ui.MesquiteMenuItemSpec;
import mesquite.lib.ui.MesquiteWindow;

/* ======================================================================== */
/** This class duplicates part of the function of MultipleAlignService in the Alter menu, but appears in the Matrix menu */
public class AlignSequencesCodon extends MolecDataEditorInit { //implements CalculationMonitor, SeparateThreadStorage {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(MultipleSequenceAligner.class, getName() + " needs a module to calculate alignments.",
		"The sequence aligner is chosen in the Codon Align Entire Matrix submenu");
	}

	MolecularData data ;

	MesquiteTable table;

	MesquiteMenuItemSpec mss= null;
	
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		mss = addModuleMenuItems(null, makeCommand("doCodonAligner",  this), mesquite.align.AMultipleAlignServiceCodon.AMultipleAlignServiceCodon.class);
	
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		if (!(data instanceof MolecularData)){
			mss.setEnabled(false);
			resetContainingMenuBar();
			return;
		}
		this.table = table;
		this.data = (MolecularData)data;
		if (mss != null)
			mss.setCompatibilityCheck(data.getStateClass());
		resetContainingMenuBar();

	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Hires module to align sequences", "[name of module]", commandName, "doCodonAligner")) {
			DataAlterer tda= (DataAlterer)hireNamedEmployee(DataAlterer.class,  arguments);
			if (tda!=null) {
				MesquiteWindow w = table.getMesquiteWindow();
				UndoReference undoReference = new UndoReference();
				AlteredDataParameters alteredDataParameters = new AlteredDataParameters();
				if (MesquiteTrunk.debugMode)
					logln("Memory available before data alterer invoked: " + MesquiteTrunk.getMaxAvailableMemory());
				int a = tda.alterData(data, table, undoReference, alteredDataParameters);
				if (MesquiteTrunk.debugMode)
					logln("Memory available after data alterer invoked: " + MesquiteTrunk.getMaxAvailableMemory());

				if (a== ResultCodes.SUCCEEDED) {
					table.repaintAll();
					Notification notification = new Notification(MesquiteListener.DATA_CHANGED, alteredDataParameters.getParameters(), undoReference);
					if (alteredDataParameters.getSubcodes()!=null)
						notification.setSubcodes(alteredDataParameters.getSubcodes());
					data.notifyListeners(this, notification);
				}
				fireEmployee(tda); //Note: this assumes the alterer completes the job, i.e. is on same thread

			}
		}		
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	
	
	/*.................................................................................................................*/
	public boolean showCitation() {
		return false;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Codon Align Sequences";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Sends the selected sequence to be codon aligned." ;
	}


}

