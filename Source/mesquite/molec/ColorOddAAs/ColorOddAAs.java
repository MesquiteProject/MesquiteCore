/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.ColorOddAAs; 

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.ColorRecord;
import mesquite.categ.lib.*;


/* ======================================================================== */
public class ColorOddAAs extends DataWindowAssistantID implements CellColorer, CellColorerMatrix {
	MesquiteTable table;
	protected DNAData data;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	public boolean setActiveColors(boolean active){
		setActive(active);
		return true; //TODO: check success
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
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 300;  
	}

	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = table;
		if (data instanceof DNAData)
			this.data = (DNAData)data;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Color AAs to Check";
	}
	public String getNameForMenuItem() {
		return "Color AAs to Check";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Colors the cells of a DNA matrix by stop codons, partial triplets, and missing data.";
	}
	/*.................................................................................................................*/
	public void viewChanged(){
	}
	public String getCellString(int ic, int it){

		if (!isActive())
			return null;
		if (ic<0 || it<0)
			return "Cells colored to show regions to check";
		if (data == null)
			return null;
		else if (!data.isCoding(ic)) {
			return "Cells colored to show regions to check";
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
				return "Cells colored to show regions to check";
			}
		}
	}
	ColorRecord[] legend;
	Color stopColor = Color.black;
	Color partTripletColor = Color.red;
	Color missingColor = Color.blue;
	Color terminalColor = ColorDistribution.veryLightGray;
	Color immediateTerminusColor = Color.green;
	/*.................................................................................................................*/
	public ColorRecord[] getLegendColors(){
		if (data == null)
			return null;
		legend = new ColorRecord[6];
		legend[0] = new ColorRecord(stopColor, "stop");
		legend[1] = new ColorRecord(partTripletColor, "incomplete codon triplet");
		legend[2] = new ColorRecord(missingColor, "missing");
		legend[3] = new ColorRecord(immediateTerminusColor, "start/end of sequence");
		legend[4] = new ColorRecord(terminalColor, "terminal gaps");
		legend[5] = new ColorRecord(Color.white, "other");

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
		boolean isStop = false;
		Color color = null;
		 if (!data.isCoding(ic)) {
			color = data.getColorOfStates(ic, it);
		}
		else {
			long s = data.getAminoAcid(ic,it,true);
			
			if (!CategoricalState.isImpossible(s)) {
				color = ProteinData.getAminoAcidColor(s, ProteinData.multistateColor);
				if (CategoricalState.isOnlyElement(s, ProteinData.TER))
					isStop=true;
			}
			else {
				color = data.getColorOfStates(ic, it);
			}
		}
		  if (data.isInapplicable(ic,it)) {
			  if (ic+1<data.getNumChars()){
				  if (!data.anyApplicableBefore(ic,it)) 
					 if (!data.isInapplicable(ic+1,it))
						 return immediateTerminusColor;
					 else
						 return terminalColor;
				  }
			  if (ic-1>0){
				  if (!data.anyApplicableAfter(ic,it)) 
						 if (!data.isInapplicable(ic-1,it))
							 return immediateTerminusColor;
						 else
							 return terminalColor;
				  }
		 }
		 if (isStop) {
			 return stopColor;
		 } else if (data.isInPartialTriplet(ic, it, null)) {
			 return partTripletColor;
		 }
		 else if (data.isUnassigned(ic,it)) {
			 return missingColor;
		 }
		 color = ColorDistribution.brighter(color, 0.25);
			
		return color;
		 
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




