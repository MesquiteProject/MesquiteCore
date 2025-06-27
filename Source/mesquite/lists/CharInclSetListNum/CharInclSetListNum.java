/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.CharInclSetListNum;
/*~~  */

import mesquite.lib.MesquiteListener;
import mesquite.lib.Notification;
import mesquite.lib.SpecsSetVector;
import mesquite.lib.characters.CharInclusionSet;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.table.MesquiteTable;
import mesquite.lists.lib.CharInclSetListAsst;

/* ======================================================================== */
public class CharInclSetListNum extends CharInclSetListAsst implements MesquiteListener {
	/*.................................................................................................................*/
	public String getName() {
		return "Number of characters in inclusion set";
	}
	public String getExplanation() {
		return "Indicates number of characters in inclusion set in list window." ;
	}

	CharacterData data=null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		/* hire employees here */
		return true;
	}

	public void setTableAndObject(MesquiteTable table, Object obj){
		Object d = obj;
		if (obj instanceof SpecsSetVector) {
			d = ((SpecsSetVector)obj).getObjectCharacterized();
		}
		if (d instanceof CharacterData){
			if (data !=null)
			data.removeListener(this);
			data = (CharacterData)d;
		if (data !=null)
			data.addListener(this);
		}
		//table would be used if selection needed
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		//TODO: respond
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;  //TODO: respond
	}
	public void changed(Object caller, Object obj, Notification notification){
		if (Notification.appearsCosmetic(notification))
			return;
		parametersChanged();
	}
	public String getTitle() {
		return "# chars. included";
	}
	public String getStringForRow(int ic){
		try{
			if (data == null)
				return null;
			SpecsSetVector tsets = data.getSpecSetsVector(CharInclusionSet.class);
			if (tsets ==null || ic<0 || ic>= tsets.size())
				return "";
			return Integer.toString(((CharInclusionSet)tsets.elementAt(ic)).numberSelected());
		}
		catch (NullPointerException e){
		}
		return "";
	}
	public String getWidestString(){
		return " # chars. included ";
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}

	/*.................................................................................................................*/
	public void endJob() {
		if (data !=null)
			data.removeListener(this);
		super.endJob();
	}

}

