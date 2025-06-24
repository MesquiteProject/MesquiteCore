/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.DefaultCellColor; 

import java.util.*;
import java.awt.*;

import mesquite.categ.lib.MolecularData;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.ColorRecord;


/* ======================================================================== */
public class DefaultCellColor extends DataWindowAssistantID implements CellColorer, CellColorerMatrixHighPriority {
	DataWindowAssistantID colorerByState = null;
	DataWindowAssistantID noColor = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		colorerByState = (DataWindowAssistantID)hireNamedEmployee(DataWindowAssistantID.class, "#ColorByState");
		noColor = (DataWindowAssistantID)hireNamedEmployee(DataWindowAssistantID.class, "#NoColor");
		return true;
	}
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return false;
   	 }
   	 public boolean requestPrimaryChoice(){
   		 return true;
   	 }
	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.data = data;
		taxa = data.getTaxa();
		colorerByState.setTableAndData(table, data);
		noColor.setTableAndData(table, data);
	}
	CharacterData data;
	Taxa taxa;
	/*.................................................................................................................*/
	 public String getNameForMenuItem() {
	return "Default";
	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Default Cell Color";
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Shows default cell colors.";
   	 }
   	 
	/*.................................................................................................................*/
   	public void viewChanged(){
   	}
   	 public boolean setActiveColors(boolean active){
   		setActive(active);
		return true; 
  	 }
   	public ColorRecord[] getLegendColors(){
   		if (data == null || !(data instanceof MolecularData))
   			return ((CellColorer)noColor).getLegendColors();
   		return ((CellColorer)colorerByState).getLegendColors();
   	}
   	public String getColorsExplanation(){
   		if (data == null || !(data instanceof MolecularData))
   	   		return ((CellColorer)noColor).getColorsExplanation();
   		return ((CellColorer)colorerByState).getColorsExplanation();
   	}
	public Color getCellColor(int ic, int it){
   		if (data == null || !(data instanceof MolecularData))
   			return ((CellColorer)noColor).getCellColor(ic, it);
   		return ((CellColorer)colorerByState).getCellColor(ic, it);
	}
   	public String getCellString(int ic, int it){
   		if (data == null || !(data instanceof MolecularData))
   			return ((DataWindowAssistantID)noColor).getCellString(ic, it);
   		return ((DataWindowAssistantID)colorerByState).getCellString(ic, it);
   	}
	public CompatibilityTest getCompatibilityTest(){
		return new CharacterStateTest();
	}
	public String getParameters(){
   		if (data == null || !(data instanceof MolecularData))
   			return noColor.getParameters();
		return colorerByState.getParameters();
	}
}


	


