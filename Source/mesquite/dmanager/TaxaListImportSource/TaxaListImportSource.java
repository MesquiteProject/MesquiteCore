/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.dmanager.TaxaListImportSource;
/*~~  */

import mesquite.lists.lib.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.lib.taxa.Taxa;

/* ======================================================================== */
public class TaxaListImportSource extends TaxonListAssistant  {
	Taxa taxa=null;
	MesquiteTable table = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
  	 }
  	 
	public void setTableAndTaxa(MesquiteTable table, Taxa taxa){
		//if (this.data !=null)
		//	this.data.removeListener(this);
		this.taxa = taxa;
		//data.addListener(this);
		this.table = table;
	}
	NameReference importSourceRef = NameReference.getNameReference("importsource");
	public String getStringForTaxon(int ic){
		String s = null;
		if (taxa != null) {
			Object b  = taxa.getAssociatedString(importSourceRef, ic); //+1 because zero based
			if (b instanceof String)
				s = (String)b;
		}
		if (StringUtil.blank(s))
			s = "?";
		return s;
	}
	public String getWidestString(){
		return "88888888888888888";
	}
	/*.................................................................................................................*/
	public String getTitle() {
		return "Original File";
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Original File (import source)";
   	 }
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
 	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
 	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
 	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
    	public int getVersionOfFirstRelease(){
    		return 110;  
    	}
	/*.................................................................................................................*/
   	public boolean isPrerelease(){
   		return false;  
   	}
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Shows the orignal file from which a fuse/imported taxon came." ;
   	 }
   	 public void endJob() {
		//if (data !=null)
		//	data.removeListener(this);
		super.endJob();
   	 }
   	 
}

