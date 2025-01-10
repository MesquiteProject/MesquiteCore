/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.ColorByMatchToFirstTaxon; 

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
public class ColorByMatchToFirstTaxon extends DataWindowAssistantID implements CellColorer, CellColorerMatrix {
	MesquiteTable table;
	CharacterData data;
	static final int MATCH = 1;
	static final int SILENT = 0;
	static final int MISMATCH = 2;
	Color[] colors;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		colors = new Color[3];
		colors[MATCH] = ColorDistribution.lightBlue;
		colors[SILENT] = Color.lightGray;
		colors[MISMATCH] = ColorDistribution.lightYellow;
		return true;
	}
	public boolean setActiveColors(boolean active){
		setActive(true);
		return true; 

	}
	

	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = table;
		this.data = data;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Color By Match to First Taxon";
	}
	public String getNameForMenuItem() {
		return "Matching First Taxon";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Colors the cells of a character matrix blue if they match the first taxon, yellow if different, grey if cell or first taxon has missing or inapplicable data.";
	}
	/*.................................................................................................................*/
	public void viewChanged(){
	}
	public String getCellString(int ic, int it){
		if (!isActive())
			return null;
		return "Colored to show whether matches first taxon";
	}
	/*.................................................................................................................*/
	int isMatch(int ic, int it){
		if (data == null || !(data instanceof CategoricalData))
			return SILENT;
		CategoricalData cData = (CategoricalData)data;
		if (cData.isUnassigned(ic,  it) || cData.isInapplicable(ic, it) || cData.isUnassigned(ic,  0) || cData.isInapplicable(ic, 0))
			return SILENT;
		long first = cData.getState(ic, 0);
		long taxon = cData.getState(ic, it);
		if ((first & taxon) > 0)
			return MATCH;
		return MISMATCH;
	}
	/*.................................................................................................................*/
	
	ColorRecord[] legend;
	public ColorRecord[] getLegendColors(){
		if (data == null)
			return null;
		legend = new ColorRecord[3];
				legend[MATCH] = new ColorRecord(colors[MATCH], "Match");
				legend[SILENT] = new ColorRecord(colors[SILENT], "Missing or inapplicable");
				legend[MISMATCH] = new ColorRecord(colors[MISMATCH], "Mismatch");
	

		return legend;
	}
	public String getColorsExplanation(){
		if (data == null)
			return null;
		if (data.getClass() == CategoricalData.class){
			return "Colors show whether cell matches first taxon (blue, match; yellow, mismatch)";
		}
		return null;
	}
	public Color getCellColor(int ic, int it){
		if (ic < 0 || it < 0)  
			return null;
		if (data == null)
			return null;
		else if (data instanceof CategoricalData) {
			return (colors[isMatch(ic, it)]);
		} else
			return data.getColorOfStates(ic, it);
	}
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyCategoricalData();
	}
	public String getParameters(){
		if (isActive())
			return getName();
		return null;
	}
}





