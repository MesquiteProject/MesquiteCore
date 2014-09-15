/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.NexusBlockList;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lists.lib.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class NexusBlockList extends ListLVModule {
	/*.................................................................................................................*/
	public String getName() {
		return "NEXUS Blocks List";
	}
	public String getExplanation() {
		return "Makes windows listing NEXUS blocks." ;
	}
	/*.................................................................................................................*/
	public boolean deleteRow(int row, boolean notify){
	return false;
}

	public int currentTaxa = 0;
	ListableVector blocks;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	public boolean showing(Object obj){
		if (obj instanceof ListableVector) {
			return (blocks == blocks && getModuleWindow()!=null);
		}
		return false;
	}

	public void showListWindow(Object obj){
		if (obj instanceof ListableVector)
			blocks =(ListableVector)obj;
		else
			return;
		setModuleWindow( new ListableVectorWindow(this));

		((ListableVectorWindow)getModuleWindow()).setCurrentObject(blocks);
		//makeMenu("NEXUS_Blocks");
		makeMenu("List");

		resetContainingMenuBar();
		resetAllWindowsMenus();
	}
	/*.................................................................................................................*/
	/* following required by ListModule*/
	public Object getMainObject(){
		return blocks;
	}
	public int getNumberOfRows(){
		if (blocks==null)
			return 0;
		else
			return blocks.size();
	}
	public Class getAssistantClass(){
		return null;
	}
	public String getItemTypeName(){
		return "NEXUS block";
	}
	public String getItemTypeNamePlural(){
		return "NEXUS blocks";
	}

	/*.................................................................................................................*/
	/** returns a String of annotation for a row*/
	public String getAnnotation(int row){
		if (blocks==null || row>=blocks.size())
			return null;
		NexusBlock nb = (NexusBlock)blocks.elementAt(row);
		return "This block belongs to the file " + nb.getFile().getName();
	}


	/*.................................................................................................................*/
	/** Requests a window to close.  In the process, subclasses of MesquiteWindow might close down their owning MesquiteModules etc.*/
	public void windowGoAway(MesquiteWindow whichWindow) {
		whichWindow.hide();
	}


}


