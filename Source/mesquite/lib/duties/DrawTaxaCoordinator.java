/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.duties;

import java.awt.*;
import mesquite.lib.*;
import java.util.*;


/* ======================================================================== */
/**A class that supervises the drawing of taxa.  It creates TaxaDisplay objects
and probably hires DrawTaxa modules. Old and clunky, it either makes a single TaxaDisplay, or multiple
ones.  Example module: BasicTaxaDrawCoordinator*/

public abstract class DrawTaxaCoordinator extends MesquiteModule  {
	protected TaxaDisplay taxaDisplay;
	protected TaxaDisplay[] taxaDisplays;
	protected Vector assistantTasks;
	protected int numDisplays = 0;
	public boolean getSearchableAsModule(){
		return false;
	}

   	 public Class getDutyClass() {
   	 	return DrawTaxaCoordinator.class;
   	 }
 	public String getDutyName() {
 		return "Draw Taxa Coordinator";
   	 }
   	 
	/** Returns the preferred size (if any) of the taxa drawing */
	public abstract Dimension getPreferredSize();


	/** return the module responsible for drawing terminal taxon names. */
   	 public abstract DrawNamesTaxaDisplay getNamesTask();
   	 
	/** Create one taxa display in the given window. */
 	public abstract TaxaDisplay createOneTaxaDisplay(Taxa taxa, MesquiteWindow window);
	/** Create a vector of taxa displays. */
 	public abstract TaxaDisplay[] createTaxaDisplays(int numDisplays, Taxa taxa, MesquiteWindow window);
	/** Create a vector of taxa displays, each with a different Taxa object. */
 	public abstract TaxaDisplay[] createTaxaDisplays(int numDisplays, Taxa[] taxa, MesquiteWindow window);


	/** Add taxa display assistant */
	public void addAssistantTask(TaxaDisplayAssistant mb) {
		if (assistantTasks==null)
			assistantTasks= new Vector();
		assistantTasks.addElement(mb);
	}
	
	/** Remove taxa display assistant */
	public void removeAssistantTask(TaxaDisplayAssistant mb) {
		if (assistantTasks!=null)
			assistantTasks.removeElement(mb);
	}

	public double getRescaleValue() {
		return 1.0;
	}

	public void setRescaleValue() {
	}

   	public boolean isSubstantive(){
   		return false;  
   	}
}


