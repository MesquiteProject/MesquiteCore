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
import mesquite.lib.characters.*;
import mesquite.lib.table.*;


/* ======================================================================== */
public class DataSpecsListWindow extends ListWindow implements MesquiteListener {
	private int currentDataSet = 0;
	private CharacterData data = null;
	SpecsSetVector ssv;
	public DataSpecsListWindow (DataSpecssetList ownerModule, int currentDataSet, CharacterData data) {
		super(ownerModule); //INFOBAR
		if (data==null) {
			ownerModule.alert("Sorry, a list window failed because the data matrix was not found");
			ownerModule.iQuit();
			return;
		}
		this.currentDataSet = currentDataSet;
		this.data = data;
		checkSSV();
		setCurrentObject(data);
		data.addListener(this);
		MesquiteTable table = getTable();
		if (table !=null)
			table.setRowNamesWidth(table.getRowNamesWidth()*2); //since not much else!
	}
	
	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree blocks, etc.)
	they can reset their titles properly*/
	public void resetTitle(){
		if (data!=null&& data.hasTitle())
			setTitle(((DataSpecssetList)ownerModule).getItemTypeNamePlural() + " of " + data.getName() ); //TODO: file???
		else if (data==null)
			setTitle(((DataSpecssetList)ownerModule).getItemTypeNamePlural() + " (DATA NULL)" ); 
		else
			setTitle(((DataSpecssetList)ownerModule).getItemTypeNamePlural()); 
		
	}
	/*.................................................................................................................*/
	public Object getCurrentObject(){
		return data;
	}
	public void setCurrentObject(Object obj){
		if (obj instanceof CharacterData) {
			data = (CharacterData)obj;
			resetTitle();
		}
		super.setCurrentObject(obj);
	}
	public void setRowName(int row, String name){
		if (data!=null) {
			SpecsSet ss = data.getSpecsSet(row, ((DataSpecssetList)ownerModule).getItemType());
			if (ss!=null)
				ss.setName(name);
		}
	}
	public boolean rowHighlighted(int row) {
		//SpecsSetVector ssv = data.getSpecSetsVector(((DataSpecssetList)ownerModule).getItemType());
		//if (ssv!=null)
		//	return ssv.indexOfCurrent()==row;
		return false;
	}
	void checkSSV(){
		if (data==null || ownerModule == null)
			return;
		SpecsSetVector ssvNow = data.getSpecSetsVector(((DataSpecssetList)ownerModule).getItemType());
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
		if (data!=null) {
			checkSSV();
			SpecsSet ss = data.getSpecsSet(row, ((DataSpecssetList)ownerModule).getItemType());
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
		if (ownerModule!=null && ((obj instanceof Taxa &&  data!=null && (Taxa)obj ==data.getTaxa())||(obj instanceof CharacterData && (CharacterData)obj ==data)))
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
		if (obj instanceof CharacterData && (CharacterData)obj ==data) {
			checkSSV();
			if (ownerModule instanceof ListModule)
				table.setNumRows(((ListModule)ownerModule).getNumberOfRows());
			table.repaintAll();
			//fundamental change in data (only need to revise number of rows etc; employees will take care of data themselves
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


