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
import java.awt.event.*;
import java.util.*;

import mesquite.lib.duties.*;
import mesquite.lib.*;

/* ======================================================================== */
public class TaxaSpecsListWindow extends ListWindow implements MesquiteListener {
	private Taxa taxa = null;
	SpecsSetVector ssv;
	public TaxaSpecsListWindow (TaxaSpecssetList ownerModule, Taxa taxa) {
		super(ownerModule); //INFOBAR
		if (taxa==null) {
			ownerModule.alert("Sorry, a list window failed because the taxa block was not found");
			ownerModule.iQuit();
			return;
		}
		this.taxa = taxa;
		checkSSV();
		setCurrentObject(taxa);
		taxa.addListener(this);
	}
	
	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree blocks, etc.)
	they can reset their titles properly*/
	public void resetTitle(){
		if (taxa!=null)
			setTitle(((TaxaSpecssetList)ownerModule).getItemTypeNamePlural() + " of " + taxa.getName() ); //TODO: file???
		else if (taxa==null)
			setTitle(((TaxaSpecssetList)ownerModule).getItemTypeNamePlural() + " (taxa NULL)" ); 
		
	}
	/*.................................................................................................................*/
	public Object getCurrentObject(){
		return taxa;
	}
	public void setCurrentObject(Object obj){
		if (obj instanceof Taxa) {
			taxa = (Taxa)obj;
			resetTitle();
		}
	}
	public void setRowName(int row, String name){
		if (taxa!=null) {
			SpecsSet ss = taxa.getSpecsSet(row, ((TaxaSpecssetList)ownerModule).getItemType());
			if (ss!=null)
				ss.setName(name);
		}
	}
	public boolean rowHighlighted(int row) {
		//SpecsSetVector ssv = taxa.getSpecSetsVector(((TaxaSpecssetList)ownerModule).getItemType());
		//if (ssv!=null)
		//	return ssv.indexOfCurrent()==row;
		return false;
	}
	void checkSSV(){
		if (taxa==null || ownerModule == null)
			return;
		SpecsSetVector ssvNow = taxa.getSpecSetsVector(((TaxaSpecssetList)ownerModule).getItemType());
		if (ssv != ssvNow) {
			if (ssv!=null)
				ssv.removeListener(this);
			ssv =ssvNow;
			if (ssv!=null) {
				ssv.addListener(this);
			}
		}
	}
	public String getRowName(int row){
		if (taxa!=null) {
			checkSSV();
			SpecsSet ss = taxa.getSpecsSet(row, ((TaxaSpecssetList)ownerModule).getItemType());
			if (ss!=null)
				return ss.getName();
			return null;
		}
		else
			return null;
	}
	public String getRowNameForSorting(int row){
		return getRowName(row);
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		if (ownerModule!=null && (obj ==  taxa))
			ownerModule.windowGoAway(this);
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;  //TODO: respond
	}
	/*.................................................................................................................*/
	/** passes which object changed (from MesquiteListener interface)*/
	public void changed(Object caller, Object obj, Notification notification){
		if (obj ==taxa) {
			if (Notification.appearsCosmetic(notification))
				return;
			checkSSV();
			if (ownerModule instanceof ListModule)
				table.setNumRows(((ListModule)ownerModule).getNumberOfRows());
			table.repaintAll();
			//fundamental change in taxa (only need to revise number of rows etc; employees will take care of taxa themselves
		}
		else if (obj instanceof SpecsSetVector && (SpecsSetVector)obj ==ssv){
			checkSSV();
			if (ownerModule instanceof ListModule)
				table.setNumRows(((ListModule)ownerModule).getNumberOfRows());
			table.repaintAll();
		}
		super.changed(caller, obj, notification);
	}
	
	
}


