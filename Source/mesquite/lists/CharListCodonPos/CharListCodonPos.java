/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.CharListCodonPos;
/*~~  */

import mesquite.lists.lib.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.table.*;
import mesquite.categ.lib.*;

/* ======================================================================== */
public class CharListCodonPos extends CharListAssistant {
	/*.................................................................................................................*/
	public String getName() {
		return "Current Codon Positions";
	}
	public String getExplanation() {
		return "Supplies current codon positions applied to characters for character list window." ;
	}

	CharacterData data=null;
	MesquiteTable table=null;
	MesquiteSubmenuSpec mPos;
	MesquiteMenuItemSpec mScs, mStc, mRssc, mLine;
	static final int numMMIS = 8;
	MesquiteMenuItemSpec[] setCodMMIS;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		setCodMMIS = new MesquiteMenuItemSpec[numMMIS];
		return true;
	}
	/*.................................................................................................................*/
	public CompatibilityTest getCompatibilityTest() {
		return new RequiresAnyDNAData();
	}
	/*.................................................................................................................*/
	private void setPositions(int position,  boolean calc, boolean notify){
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
			boolean anySelected = table.anyCellSelectedAnyWay();
			if (modelSet != null) {
				if (employer!=null && employer instanceof ListModule) {
					int c = ((ListModule)employer).getMyColumn(this);
					for (int i=0; i<data.getNumChars(); i++) {
						if (!anySelected || table.isCellSelectedAnyWay(c, i)) {
							modelSet.setValue(i, num);
							if (!changed)
								outputInvalid();
							if (calc) {
								num.setValue(num.getIntValue()+1);
								if (num.getIntValue()>3)
									num.setValue(1);
							}

							changed = true;
						}
					}
				}
			}
			if (notify) {
				if (changed)
					data.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  //not quite kosher; HOW TO HAVE MODEL SET LISTENERS??? -- modelSource
				parametersChanged();
			}
		}
	}
	/*.................................................................................................................*/
	private void setPositionsMinStops(){
		if (table !=null && data!=null) {
			Taxa taxa = data.getTaxa();
			int minStops = -1;
			int posMinStops = 1;

			for (int i = 1; i<=3; i++) {
				int totNumStops = 0;
				setPositions(i,true,false);  //set them temporarily

				for (int it= 0; it<taxa.getNumTaxa(); it++) {
					totNumStops += ((DNAData)data).getAminoAcidNumbers(it,ProteinData.TER);					 
				}
				
				logln("Number of stops with first selected as codon position " + i + ": " + totNumStops);
				if (minStops<0 || totNumStops<minStops) {
					minStops = totNumStops;
					posMinStops=i;
				}
			}
			setPositions(posMinStops,true,true);
		}

	}
	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the codon positions of the selected characters to non-coding", null, commandName, "setPositionN")) {
			setPositions(0, false,true);
		}
		else  if (checker.compare(this.getClass(), "Sets the codon positions of the selected characters to first position", null, commandName, "setPosition1")) {
			setPositions(1, false,true);
		}
		else  if (checker.compare(this.getClass(), "Sets the codon positions of the selected characters to second position", null, commandName, "setPosition2")) {
			setPositions(2, false,true);
		}
		else  if (checker.compare(this.getClass(), "Sets the codon positions of the selected characters to third position", null, commandName, "setPosition3")) {
			setPositions(3, false,true);
		}
		else  if (checker.compare(this.getClass(), "Sets the codon positions of the selected characters to be 123123123...", null, commandName, "setPositionCalc123")) {
			setPositions(1,true,true);
		}
		else  if (checker.compare(this.getClass(), "Sets the codon positions of the selected characters to be 23123123...", null, commandName, "setPositionCalc231")) {
			setPositions(2,true,true);
		}
		else  if (checker.compare(this.getClass(), "Sets the codon positions of the selected characters to be 3123123...", null, commandName, "setPositionCalc312")) {
			setPositions(3,true,true);
		}
		else  if (checker.compare(this.getClass(), "Sets the codon positions of the selected characters to minimize the number of stop codons", null, commandName, "setPositionCalcMinStops")) {
			setPositionsMinStops();
		}
		else if (checker.compare(this.getClass(), "Stores current codon position set", null, commandName, "storeCurrent")) {
			if (data!=null){
				SpecsSetVector ssv = data.getSpecSetsVector(CodonPositionsSet.class);
				if (ssv == null || ssv.getCurrentSpecsSet() == null) {
					CodonPositionsSet modelSet= new CodonPositionsSet("Codon Positions", data.getNumChars(), data);
					modelSet.addToFile(data.getFile(), getProject(), findElementManager(CodonPositionsSet.class)); //THIS
					data.setCurrentSpecsSet(modelSet, CodonPositionsSet.class);
					ssv = data.getSpecSetsVector(CodonPositionsSet.class);
				}
				if (ssv!=null) {
					SpecsSet s = ssv.storeCurrentSpecsSet();
					if (s.getFile() == null)
						s.addToFile(data.getFile(), getProject(), findElementManager(CodonPositionsSet.class));
					s.setName(ssv.getUniqueName("Codon Positions"));
					String name = MesquiteString.queryString(containerOfModule(), "Name", "Name of codon positions to be stored", s.getName());
					if (!StringUtil.blank(name))
						s.setName(name);
					ssv.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED));  
				}
				else MesquiteMessage.warnProgrammer("sorry, can't store because no specssetvector");
			}
		}
		else if (checker.compare(this.getClass(), "Replace stored codon position set by the current one", null, commandName, "replaceWithCurrent")) {
			if (data!=null){
				SpecsSetVector ssv = data.getSpecSetsVector(CodonPositionsSet.class);
				if (ssv!=null) {
					SpecsSet chosen = (SpecsSet)ListDialog.queryList(containerOfModule(), "Replace stored set", "Choose stored codon positions to replace by current",MesquiteString.helpString, ssv, 0);
					if (chosen!=null){
						SpecsSet current = ssv.getCurrentSpecsSet();
						ssv.replaceStoredSpecsSet(chosen, current);
					}
				}

			}
		}
		else if (checker.compare(this.getClass(), "Loads the stored codon positions to be the current one", "[number of codon position set to load]", commandName, "loadToCurrent")) {
			if (data !=null) {
				int which = MesquiteInteger.fromFirstToken(arguments, stringPos);
				if (MesquiteInteger.isCombinable(which)){
					SpecsSetVector ssv = data.getSpecSetsVector(CodonPositionsSet.class);
					if (ssv!=null) {
						SpecsSet chosen = ssv.getSpecsSet(which);
						if (chosen!=null){
							ssv.setCurrentSpecsSet(chosen.cloneSpecsSet()); 
							data.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED)); 
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
		//deleteMenuItem(mss);
		deleteMenuItem(mScs);
		deleteMenuItem(mRssc);
		deleteMenuItem(mLine);
		deleteMenuItem(mStc);
		deleteMenuItem(mPos);
		for (int i=0; i<numMMIS && i<setCodMMIS.length; i++)
			deleteMenuItem(setCodMMIS[i]);
		mPos = addSubmenu(null, "Set Codon Position");
		setCodMMIS[0] = addItemToSubmenu(null, mPos, "N", makeCommand("setPositionN", this));
		setCodMMIS[1] = addItemToSubmenu(null, mPos, "1", makeCommand("setPosition1", this));
		setCodMMIS[2] = addItemToSubmenu(null, mPos, "2", makeCommand("setPosition2", this));
		setCodMMIS[3] = addItemToSubmenu(null, mPos, "3", makeCommand("setPosition3", this));
		setCodMMIS[4] = addItemToSubmenu(null, mPos, "123123...", makeCommand("setPositionCalc123", this));
		setCodMMIS[5] = addItemToSubmenu(null, mPos, "231231...", makeCommand("setPositionCalc231", this));
		setCodMMIS[6] = addItemToSubmenu(null, mPos, "312312...", makeCommand("setPositionCalc312", this));
		setCodMMIS[7] = addItemToSubmenu(null, mPos, "Minimize Stop Codons", makeCommand("setPositionCalcMinStops", this));
		mScs = addMenuItem("Store current set", makeCommand("storeCurrent",  this));
		mRssc = addMenuItem("Replace stored set by current", makeCommand("replaceWithCurrent",  this));
		if (data !=null)
			mStc = addSubmenu(null, "Load codon position set", makeCommand("loadToCurrent",  this), data.getSpecSetsVector(CodonPositionsSet.class));
		this.data = data;
		this.table = table;
	}
	public void changed(Object caller, Object obj, Notification notification){
		if (Notification.appearsCosmetic(notification))
			return;
		outputInvalid();
		parametersChanged(notification);
	}
	public String getTitle() {
		return "Codon Position";
	}
	public String getStringForCharacter(int ic){
		if (data!=null) {
			CodonPositionsSet modelSet = (CodonPositionsSet)data.getCurrentSpecsSet(CodonPositionsSet.class);
			if (modelSet != null) {
				int i = modelSet.getInt(ic);
				if (i==0)
					return "N";
				else if (i<4 && i>0)
					return Integer.toString(i);
				else
					return "?";
			}
			else {
				return "N";
			}
		}
		return "?";
	}
	public String getWidestString(){
		return "Codon Position  ";
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

