/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 



Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/


package mesquite.charMatrices.ColorTInfo; 


import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.categ.lib.*;

//make display of ambiguities optional!!!!!

/** ======================================================================== */
public class ColorTInfo extends DataWindowAssistantID implements CellColorer, CellColorerTaxa {
	CharacterData data;
	MesquiteTable table;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	public void viewChanged(){
		if (data != null)
			tInfoData = data.getTaxaInfo(false);
	}
	boolean wasActive = false;
	public boolean setActiveColors(boolean active){
		wasActive = active;
		if (data != null)
			tInfoData = data.getTaxaInfo(false);
		return true; 
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}

	Associable tInfoData;

	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = table;
		this.data = data;
		if (data != null)
			tInfoData = data.getTaxaInfo(false);
	}
	/*.................................................................................................................*/
	public boolean hasDisplayModifications(){
		return false;
	}
	public String getColorsExplanation(){
		return null;
	}

	String NOINFOSTRING = "No information attached to this taxon for this matrix";
	String YESINFOSTRING = "Taxon has information attached for this matrix";
	public String getCellString(int ic, int it){
		if (it<0 || ic<0 ||  !isActive())
			return null;
		if (ic<0)   {
			if (tInfoData == null)
				return NOINFOSTRING;
			String s = tInfoData.toString(it);
			if (StringUtil.blank(s))
				return NOINFOSTRING;
			else
				return YESINFOSTRING;
		}
		return null;
	}

	ColorRecord[] legend;
	public ColorRecord[] getLegendColors(){
		if (legend == null) {
			legend = new ColorRecord[2];
			legend[0] = new ColorRecord(Color.white, NOINFOSTRING);
			legend[1] = new ColorRecord(ColorDistribution.veryLightGreen, YESINFOSTRING);
		}
		return legend;
	}
	/*.................................................................................................................*/
	public Color getCellColor(int ic, int it){


		if (it < 0) 
			return Color.white;
		if (ic<0)  {
			String s = tInfoData.toString(it);
			if (!StringUtil.blank(s))
					return ColorDistribution.veryLightGreen;
		}
			return Color.white;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Information Attached To Matrix";
	}
	/*.................................................................................................................*/
	public String getVersion() {
		return null;
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Colors taxon names by whether or not there is information attached to this matrix.";
	}
}



