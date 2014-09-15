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


/* ======================================================================== */
/**
*/

public interface ItemsSource  {

   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(Taxa taxa);
   	
   	public String accumulateParameters(String spacer);
   	public String getParameters();
   	public String getNameAndParameters();
   	public String getName();
   	
   	/** returns item numbered ic*/
   	public Object getItem(Taxa taxa, int ic);
   	/** returns number of characters for given Taxa*/
   	public int getNumberOfItems(Taxa taxa);
   	/** returns name of type of item, e.g. "Character", or "Taxon"*/
   	public String getItemTypeName();
   	/** returns name of type of item, e.g. "Characters", or "Taxa"*/
   	public String getItemTypeNamePlural();
   	/** If the items given by this source are parts of a Selectionable, then this method allows charts and other things to query about it,
   	which allows coordination of item selection to work.*/
   	public abstract Selectionable getSelectionable();
   	
  	/** zzzzzzzzzzzz*/
   	public void setEnableWeights(boolean enable);
   	public boolean itemsHaveWeights(Taxa taxa);
  	/** zzzzzzzzzzzz*/
   	public double getItemWeight(Taxa taxa, int ic);
  	/** zzzzzzzzzzzz*/
   	public void prepareItemColors(Taxa taxa);
  	/** zzzzzzzzzzzz*/
   	public Color getItemColor(Taxa taxa, int ic);
 	/** zzzzzzzzzzzz*/
   	public String getItemName(Taxa taxa, int ic);
}

