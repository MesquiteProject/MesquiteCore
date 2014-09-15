/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lists.lib;

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;



/* ======================================================================== */
/**Supplies a column for a taxa list window.*/

public abstract class TaxonListAssistant extends ListAssistant  {

   	 public Class getDutyClass() {
   	 	return TaxonListAssistant.class;
   	 }
 	public String getDutyName() {
 		return "Taxon list assistant";
   	 }

	public abstract void setTableAndTaxa(MesquiteTable table, Taxa taxa);
	public abstract String getStringForTaxon(int it);
	public abstract String getWidestString();
	public abstract String getTitle();
	public void setTableAndObject(MesquiteTable table, Object object){
		setTableAndTaxa(table, (Taxa)object);
	}
	public String getStringForRow(int ic){
		try {
			return getStringForTaxon(ic);
		}
		catch (NullPointerException e){}
		return "";
	}
}


