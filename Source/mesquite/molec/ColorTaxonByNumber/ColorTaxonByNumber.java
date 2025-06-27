/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.ColorTaxonByNumber;

import java.awt.Color;

import mesquite.lib.CommandChecker;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.Snapshot;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.NumberForTaxon;
import mesquite.lib.duties.TaxonNameStyler;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.Taxon;
import mesquite.lib.ui.MesquiteColorTable;
import mesquite.lib.ui.MesquiteSubmenuSpec;

/* ======================================================================== */
public class ColorTaxonByNumber extends TaxonNameStyler {
	NumberForTaxon numberTask;
	MesquiteString ntName;
	MesquiteCommand nTC;
	MesquiteSubmenuSpec mss;
	Taxa myTaxa = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		numberTask = (NumberForTaxon)hireEmployee(NumberForTaxon.class, "Value for coloring cells");//shouldn't ask as this is an init and might not be needed.  "Value to calculate for character state in taxon"
		if (numberTask == null)
			return false;
		ntName = new MesquiteString();
		nTC =makeCommand("setNumberTask",  this);
		numberTask.setHiringCommand(nTC);
		ntName.setValue(numberTask.getName());
		if (numModulesAvailable(NumberForTaxon.class)>0) {
			mss = addSubmenu(null, "Values for Colors of Taxon Names", nTC, NumberForTaxon.class);
			mss.setSelected(ntName);
		}
		return true;
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return 360;  
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		if (numberTask ==null)
			return null;
		Snapshot temp = new Snapshot();
		temp.addLine("setNumberTask ", numberTask);  
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the module that calculates numbers by which to color the taxon names", "[name of module]", commandName, "setNumberTask")) {
			NumberForTaxon temp =  (NumberForTaxon)replaceEmployee(NumberForTaxon.class, arguments, "Module to calculate numbers by which to color taxon names", numberTask);
			if (temp!=null) {
				numberTask = temp;
				ntName.setValue(numberTask.getName());
				numberTask.setHiringCommand(nTC);
				ntName.setValue(numberTask.getName());
				parametersChanged();
				return numberTask;
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	private int getNumMatricesWithDataForTaxon(Taxa taxa, int it){
		int totMatrices =  getProject().getNumberCharMatrices(taxa);
		int numMatrices = 0;
		if (totMatrices >0){
			for (int im = 0; im < totMatrices; im++){
				CharacterData data = getProject().getCharacterMatrix(taxa, im);
				if (data.hasDataForTaxon(it)){
					numMatrices++;
				}
			}
		}
		return numMatrices;
	}
	/*.................................................................................................................*/
	MesquiteColorTable colorTable = MesquiteColorTable.DEFAULTGRAYTABLE.cloneColorTable();
	MesquiteNumber minimum = new MesquiteNumber();
	MesquiteNumber maximum = new MesquiteNumber();
	public void initialize(Taxa taxa){
		colorTable.setMode(MesquiteColorTable.GRAYSCALE);
		if (myTaxa != taxa){
			numberTask.initialize(taxa);
			myTaxa = taxa;
		}
	}

	public void prepareToStyle(Taxa taxa){
		if (myTaxa != taxa){
			numberTask.initialize(taxa);
			myTaxa = taxa;
			parametersChanged();
			return;
		}
		minimum.setToUnassigned();
		maximum.setToUnassigned();
		MesquiteNumber result = new MesquiteNumber();
		for (int it = 0; it<taxa.getNumTaxa(); it++){
			Taxon taxon = taxa.getTaxon(it);
			numberTask.calculateNumber(taxon, result, null);
			minimum.setMeIfIAmMoreThan(result);
			maximum.setMeIfIAmLessThan(result);
		}
	}

	public Color getTaxonNameColor(Taxa taxa, int it){
		if (myTaxa != taxa){
			prepareToStyle(taxa);
		}
		MesquiteNumber result = new MesquiteNumber();
		if (minimum.isCombinable() && maximum.isCombinable()){
			double MIN =  minimum.getDoubleValue();
			double MAX =  maximum.getDoubleValue();
			if (MAX>MIN){
				//make min 20% smaller so that min isn't white
				MIN = MIN - 0.2*(MAX-MIN);
				Taxon taxon = taxa.getTaxon(it);
				numberTask.calculateNumber(taxon, result, null);
				if (result.isCombinable()){
					return colorTable.getColor(result.getDoubleValue(), MIN, MAX);
				}
			}
		}
		return null;
	}
	public boolean getTaxonNameBoldness(Taxa taxa, int it){
		return true;
	}
	public String getObjectComment(Object obj){
		Taxon taxon = ((Taxon)obj);
		Taxa taxa = taxon.getTaxa();
		if (myTaxa != taxa)
			prepareToStyle(taxa);
		MesquiteNumber result = new MesquiteNumber();
		MesquiteString resultString = new MesquiteString();
		numberTask.calculateNumber(taxon, result, resultString);
		return resultString.getValue();
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Color Taxa By Number";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Supplies the color for a taxon name according to a number calculated for the taxa." ;
	}
}
