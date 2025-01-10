/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.HighlightMultistateCells; 

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
public class HighlightMultistateCells extends DataWindowAssistantID implements CellColorer, CellColorerMatrix {
	MesquiteTable table;
	protected DNAData data;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	public boolean setActiveColors(boolean active){
		setActive(true);
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
		return "Highlight Multistate Cells";
	}
	public String getNameForMenuItem() {
		return "Highlight Multistate Cells";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Highlights multistate cells, including those with ? or N.";
	}
	/*.................................................................................................................*/
	public void viewChanged(){
	}
	public String getCellString(int ic, int it){
		if (!isActive())
			return null;
		return "Cells colored to highlight multistate cells";
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

		Color color = data.getColorOfStates(ic, it);
		//return color;
		if (data.isMultistateOrUncertainty(ic, it))
			return Color.red;
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





