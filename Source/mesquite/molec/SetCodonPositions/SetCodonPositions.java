/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.SetCodonPositions;
/*~~  */

import java.awt.Checkbox;
import java.awt.Dialog;
import java.awt.Label;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.categ.lib.*;
import mesquite.lib.table.*;
import mesquite.lists.lib.ListModule;

/* ======================================================================== */
public class SetCodonPositions extends DNADataAlterer {
	MesquiteTable table;
	CharacterData data;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	boolean isSelected(int ic, DNAData data, MesquiteTable table){
		if (table!= null && table.isColumnSelected(ic))
			return true;
		if (data.getSelected(ic))
			return true;
		return false;
	}

	int startingPos = 0;
	double skipPercentage = 100.00;

	boolean queryOptions(){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(containerOfModule(), "Setting Codon Positions",  buttonPressed);
		queryDialog.addLabel("Setting Codon Positions of selected characters", Label.CENTER);
		RadioButtons choices = queryDialog.addRadioButtons (new String[]{"123123...", "23123...", "3123...", "Minimize Stop Codons"}, 0);
		DoubleField skip = queryDialog.addDoubleField ("Skip (and set non-coding) sites with % gaps >", skipPercentage, 6, 0, 100.00);

		queryDialog.completeAndShowDialog(true);

		boolean ok = (queryDialog.query()==0);

		if (ok) {
			startingPos = choices.getValue();
			skipPercentage = skip.getValue();
		}

		queryDialog.dispose();   		 

		return ok;
	}
	/*.................................................................................................................*/
	/** Called to alter data in those cells selected in table*/
	public boolean alterData(CharacterData dData, MesquiteTable table,  UndoReference undoReference){
		this.table = table;
		if (!(dData instanceof DNAData)){
			MesquiteMessage.warnProgrammer("Can use " + getName() + " only on DNA data");
			return false;
		}
		DNAData data = (DNAData)dData;
		if (okToInteractWithUser(CAN_PROCEED_ANYWAY, "Querying about options")){ //need to check if can proceed
			boolean ok = queryOptions();
			if (!ok)
				return false;

		}
		UndoInstructions undoInstructions = data.getUndoInstructionsAllData();
		boolean noColumnsSelected =  !((table != null && table.anyColumnSelected()) || data.anySelected());

		if (startingPos < 3)
			setPositions(startingPos+1, false,  noColumnsSelected,  data,  table); 
		else
			setPositionsMinStops(false,  noColumnsSelected,  data,  table); 
			
		if (undoInstructions!=null){
			undoInstructions.setNewData(data);
			if (undoReference!=null){
				undoReference.setUndoer(undoInstructions);
				undoReference.setResponsibleModule(this);
			}
		}
		return true;
	}
	/*.................................................................................................................*/
	public void alterCell(CharacterData ddata, int ic, int it){
		/*CategoricalData data = (CategoricalData)ddata;
		if (data.isUnassigned(ic, it))
			data.setState(ic, it, CategoricalState.inapplicable);
		 */
	}

	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return true;
	}
	boolean skippable(int ic, DNAData data){
		if (skipPercentage >99.99999)
			return false;
		int countWithoutData = 0;
		int threshold = (int)((skipPercentage+0.000001)/100.00*data.getNumTaxa());
		for (int it = 0; it<data.getNumTaxa() && countWithoutData<=threshold; it++)
			if (data.isInapplicable(ic, it)) 
				countWithoutData++;
		if (countWithoutData>threshold)
			return true;
		return false;
	}
	/*.................................................................................................................*/
	private void setPositions(int position,  boolean notify, boolean noColumnsSelected, DNAData data, MesquiteTable table){
		if (table !=null && data!=null) {
			boolean changed=false;
			MesquiteNumber num = new MesquiteNumber();
			num.setValue(position);
			CodonPositionsSet modelSet = (CodonPositionsSet) data.getCurrentSpecsSet(CodonPositionsSet.class);
			if (modelSet == null) {
				modelSet= new CodonPositionsSet("Codon Positions", data.getNumChars(), data);
				modelSet.addToFile(data.getFile(), getProject(), findElementManager(CodonPositionsSet.class)); //THIS
				data.setCurrentSpecsSet(modelSet, CodonPositionsSet.class);
			}
			if (modelSet != null) {
				for (int ic=0; ic<data.getNumChars(); ic++) {
					if (noColumnsSelected || isSelected(ic, data, table)){
						if (skippable(ic, data)) 
							modelSet.setValue(ic, 0);

						else {
							modelSet.setValue(ic, num);
							num.setValue(num.getIntValue()+1);
							if (num.getIntValue()>3)
								num.setValue(1);
						}
						changed = true;
					}

				}
			}
			if (notify) {
				if (changed)
					data.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  //not quite kosher; HOW TO HAVE MODEL SET LISTENERS??? -- modelSource
			}
		}
	}
	/*.................................................................................................................*/
	private void setPositionsMinStops(boolean notify, boolean noColumnsSelected, DNAData data, MesquiteTable table){
		if (table !=null && data!=null) {
			Taxa taxa = data.getTaxa();
			int minStops = -1;
			int posMinStops = 1;

			for (int i = 1; i<=3; i++) {
				int totNumStops = 0;
				setPositions(i, false,  noColumnsSelected,  data,  table);  //set them temporarily

				for (int it= 0; it<taxa.getNumTaxa(); it++) {
					totNumStops += ((DNAData)data).getAminoAcidNumbers(it,ProteinData.TER);					 
				}
				logln("Number of stops with first selected as codon position " + i + ": " + totNumStops);
				if (minStops<0 || totNumStops<minStops) {
					minStops = totNumStops;
					posMinStops=i;
				}
			}
			setPositions(posMinStops, notify,  noColumnsSelected,  data,  table); 
		}

	}	/*.................................................................................................................*/
	public boolean showCitation(){
		return false;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Set Codon Positions";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Sets codon positions of selected characters." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 302;  
	}

}


