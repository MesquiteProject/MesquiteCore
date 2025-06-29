/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.ColorByAA; 

import java.awt.Color;

import mesquite.categ.lib.CategoricalState;
import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.ProteinData;
import mesquite.categ.lib.ProteinState;
import mesquite.categ.lib.RequiresAnyDNAData;
import mesquite.lib.CommandChecker;
import mesquite.lib.CompatibilityTest;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.Snapshot;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.CellColorer;
import mesquite.lib.duties.CellColorerMatrixHighPriority;
import mesquite.lib.duties.DataWindowAssistantID;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.ColorRecord;


/* ======================================================================== */
public class ColorByAA extends DataWindowAssistantID implements CellColorer, CellColorerMatrixHighPriority {
	MesquiteTable table;
	protected DNAData data;
	MesquiteBoolean emphasizeDegeneracy;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		emphasizeDegeneracy = new MesquiteBoolean(false);
		return true;
	}
	public boolean setActiveColors(boolean active){
		setActive(active);
		deleteAllMenuItems();
		if (active){
			addMenuSeparator();
			addCheckMenuItem(null, "Emphasize Less Degenerate Amino Acids", makeCommand("emphasizeDegeneracy", this), emphasizeDegeneracy);
		}
		resetContainingMenuBar();
		return true; //TODO: check success
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 110;  
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("emphasizeDegeneracy " + emphasizeDegeneracy.toOffOnString());
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Turns on or off the emphasizing of less degenerate codons.", null, commandName, "emphasizeDegeneracy")) {
			emphasizeDegeneracy.toggleValue(parser.getFirstToken(arguments));
			parametersChanged();
		}

		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = table;
		if (data instanceof DNAData)
			this.data = (DNAData)data;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Color By Amino Acid";
	}
	public String getNameForMenuItem() {
		return "Color by Amino Acid";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Colors the cells of a DNA matrix by the amino acids for which they code.";
	}
	/*.................................................................................................................*/
	public void viewChanged(){
	}
  	public String getCellExplanation(int ic, int it){
		if (ic<0 || !isActive())
			return null;
		if (data == null)
			return null;
 		int i = data.getCodonPosition(ic);
 		String s;
  		if (!MesquiteInteger.isCombinable(i))
  			s= "Codon position unassigned";
  		if (i == 0)
  			s= "Non-coding";
  		else
  			s= "Codon position: " + i;
		long aa = data.getAminoAcid(ic,it,true);
		if (aa!=CategoricalState.inapplicable)
			s += ", Amino acid: " + ProteinState.toString(aa, null, 0, false, false);
		return s;
	}
	public String getCellString(int ic, int it){

		if (!isActive())
			return null;
		if (ic<0 || it<0)
			return "Cells colored to show translated amino acids";
		if (data == null)
			return null;
		else if (!data.isCoding(ic)) {
			return "Cells colored to show translated amino acids";
		}
		else {
			long s = data.getAminoAcid(ic,it,true);
			if (!CategoricalState.isImpossible(s)) {
				String st = "Translated amino acid: " + ProteinState.toString(s, false) + " (followed by ";
				MesquiteInteger nextCharacter = new MesquiteInteger(ic);
				for (int i = 0; i<20; i++){
					s = data.getNextAminoAcid(nextCharacter, it, true);
					if (s!=CategoricalState.inapplicable)
						st += ProteinState.toString(s, false);
				}
				return st + ")";
			}
			else {
				return "Cells colored to show translated amino acids";
			}
		}
	}
	ColorRecord[] legend;
	/*.................................................................................................................*/
	public ColorRecord[] getLegendColors(){
		if (data == null)
			return null;
		legend = new ColorRecord[ProteinState.maxProteinState+1];
		Color color;
		for (int is = 0; is<=ProteinState.maxProteinState; is++) {
			 if (emphasizeDegeneracy.getValue()) {
				 color = ProteinData.getProteinColorOfState(is);
				 color = ((DNAData)data).alterColorToDeemphasizeDegeneracy(is,color);
			 } else
				 color = ProteinData.getProteinColorOfState(is);
			 legend[is] = new ColorRecord(color, ProteinData.getStateLongName(is));
		}

		return legend;
	}
	/*.................................................................................................................*/
	public String getColorsExplanation(){
		if (data == null)
			return null;
		/*  		if (data.getClass() == CategoricalData.class){
   			return "Colors of states may vary from character to character";
   		}
		 */
		return null;
	}
	/*.................................................................................................................*/

	public Color getCellColor(int ic, int it){

		if (ic<0 || it<0)
			return null;
		if (data == null)
			return null;
		else if (!data.isCoding(ic)) {
			Color color = data.getColorOfStates(ic, it);
			//return color;
			return ColorDistribution.brighter(color, 0.6);
		}
		else {
			long s = data.getAminoAcid(ic,it,true);
			if (!CategoricalState.isImpossible(s)) {
				Color color;
				 if (emphasizeDegeneracy.getValue()) {
					 color = ProteinData.getAminoAcidColor(s, Color.white);
					 color = ((DNAData)data).alterColorToDeemphasizeDegeneracy(ic,s,color);
				 } else
					 color = ProteinData.getAminoAcidColor(s, ProteinData.multistateColor);
				return color;
			}
			else {
				return data.getColorOfStates(ic, it);

			}
		}
	}
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyDNAData();
	}
	public String getParameters(){
		if (isActive())
			return getName();
		return null;
	}
}





