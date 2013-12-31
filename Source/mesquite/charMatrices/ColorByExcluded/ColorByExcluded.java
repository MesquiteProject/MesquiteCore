/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.ColorByExcluded; 

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;


/* ======================================================================== */
public class ColorByExcluded extends DataWindowAssistantID implements CellColorer, CellColorerCharacters, CellColorerMatrix {
	MesquiteTable table;
	CharacterData data;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
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
		return "Color If Excluded";
   	 }
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Excluded";
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Colors the excluded characters gray.";
   	 }
   	 
	/*.................................................................................................................*/
   	public void viewChanged(){
   	}
   	 public boolean setActiveColors(boolean active){
   	 	setActive(true);
		return true; 

   	 }
   	ColorRecord[] legend;
   	public ColorRecord[] getLegendColors(){
   		if (legend == null) {
   			legend = new ColorRecord[2];
   			legend[0] = new ColorRecord(Color.white, "Included");
   			legend[1] = new ColorRecord(Color.gray, "Excluded");
  		}
   		return legend;
   	}
   	public String getColorsExplanation(){
   		return null;
   	}
	public Color getCellColor(int ic, int it){
		if (data == null)
			return null;
		CharInclusionSet incl = (CharInclusionSet)data.getCurrentSpecsSet(CharInclusionSet.class);
		if (incl == null)
			return Color.white;
		if (incl.isBitOn(ic))
			return Color.white;
		else
			return Color.gray;
	}
   	public String getCellString(int ic, int it){
		if (ic<0 || it<0 || !isActive())
			return null;
		CharInclusionSet incl = (CharInclusionSet)data.getCurrentSpecsSet(CharInclusionSet.class);
		if (incl == null || incl.isBitOn(ic))
			return "Character is included";
		else
			return "Character is excluded";
   	}
	public String getParameters(){
		if (isActive())
			return getName();
		return null;
	}
}


	


