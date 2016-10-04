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

import mesquite.lib.duties.*;
import mesquite.lib.*;
import mesquite.lib.table.*;


/* ======================================================================== */
public class ListableVectorWindow extends ListWindow implements MesquiteListener {
	ListableVector myVector;
	ListModule listModule;
	public ListableVectorWindow (ListModule ownerModule) {
		super(ownerModule);
		listModule = ownerModule;
		resetTitle();
	}
	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree blocks, etc.)
	they can reset their titles properly*/
	public void resetTitle(){
		if (listModule!=null)
				setTitle(listModule.getItemTypeNamePlural()); 
		else
			setTitle(""); 
	}
	/*.................................................................................................................*/
	public Object getCurrentObject(){
		return myVector;
	}
	public void setCurrentObject(Object obj){
		if (obj instanceof ListableVector) {
			if (myVector!=null)
				myVector.removeListener(this);
			myVector = (ListableVector)obj;
			myVector.addListener(this);
		}
	}
	protected Listable getListable(int row){
		if (myVector!=null) {
			if (row>=0 && row<myVector.size())
				return(Listable)myVector.elementAt(row);
		}
		return null;
	}
	public void setRowName(int row, String name){
		Listable item = getListable(row);
		if (item!=null && item instanceof Renamable){
			((Renamable)item).setName(name);
			if (listModule.resetMenusOnNameChange()){
				resetAllTitles();
				listModule.resetAllMenuBars();
			}
		}
	}
	public String getRowName(int row){
		if (myVector!=null) {
			Listable lR = getListable(row);
			if (lR ==null)
				return null;
			return lR.getName();
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
		if (obj ==myVector)
			ownerModule.windowGoAway(this);
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;  //TODO: respond
	}
	/*.................................................................................................................*/
	/** passes which object changed*/
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification);
		if (!disposing && obj instanceof ListableVector && (ListableVector)obj ==myVector) {
			if (code==MesquiteListener.NAMES_CHANGED) {
				getTable().redrawRowNames();
			}
			else if (code==MesquiteListener.SELECTION_CHANGED) {
				getTable().synchronizeRowSelection(myVector);
				getTable().repaintAll();
			}
			else if (code==MesquiteListener.PARTS_ADDED || code==MesquiteListener.PARTS_DELETED || code==MesquiteListener.PARTS_MOVED || code==MesquiteListener.PARTS_CHANGED) {
				getTable().setNumRows(myVector.size());
				getTable().synchronizeRowSelection(myVector);
				getTable().repaintAll();
			}
			else if (code!=MesquiteListener.ANNOTATION_CHANGED && code!=MesquiteListener.ANNOTATION_ADDED && code!=MesquiteListener.ANNOTATION_DELETED) {
				getTable().synchronizeRowSelection(myVector);
				getTable().repaintAll();
			}
		}
		else
			super.changed(caller, obj, notification);
	}
	/*.................................................................................................................*/
	public void dispose(){
		myVector.removeListener(this);
		super.dispose();
	}
	
}




