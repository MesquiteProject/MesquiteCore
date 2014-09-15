/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.ColorByFootnote; 

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;


/* ======================================================================== */
public class ColorByFootnote extends DataWindowAssistantID implements CellColorer, CellColorerCharacters, CellColorerTaxa, CellColorerMatrix {
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
		return "Color By Footnote";
   	 }
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Footnote Present";
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Colors the cells of a character matrix by whether or not they have a footnote.";
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
   			legend[0] = new ColorRecord(Color.white, "None");
   			legend[1] = new ColorRecord(ColorDistribution.lightGreen, "With Footnote");
  		}
   		return legend;
   	}
   	public String getColorsExplanation(){
   		return null;
   	}
	public Color getCellColor(int ic, int it){
		if (data == null)
			return null;
		else if (ic== -1 && data.getTaxa().getAnnotation(it)!=null){ //taxon
			return ColorDistribution.lightGreen;
		}
		else if (it == -1 && data.getAnnotation(ic) !=null){ //character
			return ColorDistribution.lightGreen;
		}
		else if (data.getAnnotation(ic, it) !=null)
			return ColorDistribution.lightGreen;
		else {
			return null;
		}
	}
   	public String getCellString(int ic, int it){
		if (data == null || !isActive())
			return null;
		else if (ic== -1 && data.getTaxa().getAnnotation(it)!=null){ //taxon
			return "Taxon has footnote";
		}
		else if (it == -1 && data.getAnnotation(ic) !=null){ //character
			return "Character has footnote";
		}
		else if (data.getAnnotation(ic, it) !=null)
			return "Cell corresponding to character and taxon has footnote";
		else {
			return null;
		}
   	}
	public CompatibilityTest getCompatibilityTest(){
		return new CharacterStateTest();
	}
	public String getParameters(){
		if (isActive())
			return getName();
		return null;
	}
}


	


