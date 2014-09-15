/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.align.ColorForAlignment; 

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.categ.lib.*;


/* ======================================================================== */
public class ColorForAlignment extends DataWindowAssistantID implements CellColorer, CellColorerMatrix {
	MesquiteTable table;
	CharacterData data;
	Color Acolor, Ccolor, Gcolor, Tcolor, AGcolor, CTcolor;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		Acolor = Color.red;
		Gcolor = Color.orange;
		Ccolor = ColorDistribution.veryLightBlue;
		Tcolor = Color.blue;
		AGcolor = new Color((Acolor.getRed() + Gcolor.getRed())/2, (Acolor.getGreen() + Gcolor.getGreen())/2, (Acolor.getBlue() + Gcolor.getBlue())/2);
		CTcolor = new Color((Tcolor.getRed() + Ccolor.getRed())/2, (Tcolor.getGreen() + Ccolor.getGreen())/2, (Tcolor.getBlue() + Ccolor.getBlue())/2);
		return true;
	}
	public boolean setActiveColors(boolean active){
		setActive(true);
		return true; 

	}
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyDNAData();
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
		return "Color For Aligning";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}
	public String getNameForMenuItem() {
		return "Aligning Colors";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Colors the cells of a character matrix for easy of alignment.";
	}
	/*.................................................................................................................*/
	public void viewChanged(){
	}
	public String getCellString(int ic, int it){
		if (!isActive())
			return null;
		return "Colored to show state of character";
	}
	ColorRecord[] legend;
	public ColorRecord[] getLegendColors(){
		if (data == null)
			return null;
		legend = null;
		if (data instanceof DNAData){
			legend = new ColorRecord[4];
			legend[0] = new ColorRecord(Acolor, DNAData.getDefaultStateSymbol(0));
			legend[1] = new ColorRecord(Ccolor, DNAData.getDefaultStateSymbol(1));
			legend[2] = new ColorRecord(Gcolor, DNAData.getDefaultStateSymbol(2));
			legend[3] = new ColorRecord(Tcolor, DNAData.getDefaultStateSymbol(3));
		}
		return legend;
	}
	public String getColorsExplanation(){
		if (data == null)
			return null;
		if (data.getClass() == CategoricalData.class){
			return "Colors of states may vary from character to character";
		}
		return null;
	}

	public Color getCellColor(int ic, int it){
		if (data == null)
			return null;
		else {
			if (data instanceof DNAData){
				DNAData dData = (DNAData)data;
				long state = dData.getState(ic, it) & CategoricalState.statesBitsMask;
				if (state == DNAState.A)
					return Acolor;
				else if (state == DNAState.G)
					return Gcolor;
				else if (state == (DNAState.A | DNAState.G))
					return AGcolor;
				else if (state == DNAState.C)
					return Ccolor;
				else if (state == DNAState.T)
					return Tcolor;
				else if (state == (DNAState.C | DNAState.T))
					return CTcolor;
				return Color.lightGray;

			}
			return data.getColorOfStates(ic, it);
		}
	}
	public String getParameters(){
		if (isActive())
			return getName();
		return null;
	}
}





