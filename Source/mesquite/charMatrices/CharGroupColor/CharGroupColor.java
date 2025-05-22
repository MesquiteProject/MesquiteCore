/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.CharGroupColor; 

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.ColorRecord;


/* ======================================================================== */
public class CharGroupColor extends DataWindowAssistantID implements CellColorer, CellColorerCharacters, CellColorerMatrix {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return 250;  
	}
	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.data = data;
		taxa = data.getTaxa();
	}
	CharacterData data;
	Taxa taxa;
	/*.................................................................................................................*/
	public String getName() {
		return "Character Group Color";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Colors by Character Group.";
	}

	/*.................................................................................................................*/
	public void viewChanged(){
	}
	public boolean setActiveColors(boolean active){
		setActive(active);
		return true; 
	}
	public ColorRecord[] getLegendColors(){
		return null;
	}
	public String getColorsExplanation(){
		return null;
	}
	public Color getCellColor(int ic, int it){
		if (ic>=0) {  
			CharacterPartition part = (CharacterPartition)data.getCurrentSpecsSet(CharacterPartition.class);
			if (part!=null){
				CharactersGroup mi = (CharactersGroup)part.getProperty(ic);
				if (mi!=null)
					return mi.getColor();
			}
			return null;
		}
		return null;
	}
	public String getCellString(int ic, int it){
		if (ic>=0) {  
			CharacterPartition part = (CharacterPartition)data.getCurrentSpecsSet(CharacterPartition.class);
			if (part!=null){
				CharactersGroup mi = (CharactersGroup)part.getProperty(ic);
				if (mi!=null)
					return "This character belongs to the group " + mi.getName();
			}
		}
		return "";
	}
	public String getParameters(){
		return null;
	}
}





