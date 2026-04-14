/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.align.HighlightInternalPartialTriplets; 

import java.awt.Color;

import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.RequiresAnyDNAData;
import mesquite.lib.CompatibilityTest;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.CellColorer;
import mesquite.lib.duties.CellColorerMatrix;
import mesquite.lib.duties.DataWindowAssistantID;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.ColorRecord;


/* ======================================================================== */
public class HighlightInternalPartialTriplets extends DataWindowAssistantID implements CellColorer, CellColorerMatrix {
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
		return true;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
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
		return "Highlight Internal Incomplete Codons";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Highlights bases that are part of an incomplete codon, and are internal within the sequence.";
	}
	/*.................................................................................................................*/
	public void viewChanged(){
	}
	public String getCellString(int ic, int it){
		if (!isActive())
			return null;
			return "Cells colored to bases that are part of an incomplete codon, and are internal within the sequence.";
	}
	ColorRecord[] legend;
	/*.................................................................................................................*/
	public ColorRecord[] getLegendColors(){
		return null;
	}
	/*.................................................................................................................*/
	public String getColorsExplanation(){
		if (data == null)
			return null;
		return null;
	}
	/*.................................................................................................................*/

	public Color getCellColor(int ic, int it){
		if (ic<0 || it<0)
			return null;
		if (data == null)
			return null;
		if (!data.isInapplicable(ic, it))  // has a base
			if (data.isInPartialTriplet(ic, it, null))  // is in a partial triplet
				if (data.getCodonPosition(ic)==1 && (data.hasDataToLeft(ic, it) && data.hasDataToRight(ic+2,it))) // is internal
					return Color.blue;
				else if (data.getCodonPosition(ic)==2 && (data.hasDataToLeft(ic-1, it) && data.hasDataToRight(ic+1,it))) // is internal
					return Color.blue;
				else if (data.getCodonPosition(ic)==3 && (data.hasDataToLeft(ic-2, it) && data.hasDataToRight(ic,it))) // is internal
					return Color.blue;
		Color color = data.getColorOfStates(ic, it);
		//return color;
		return ColorDistribution.brighter(color, 0.15);
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





