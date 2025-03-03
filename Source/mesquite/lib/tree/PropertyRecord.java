/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib.tree;

import java.awt.Font;

import mesquite.lib.Associable;
import mesquite.lib.Listable;
import mesquite.lib.ListableVector;
import mesquite.lib.NameReference;
import mesquite.lib.Nameable;

/* ======================================================================== */
/*represents the bits, longs, doubles, strings and objects belonging to parts of an Associable.
 *  Not permanently in an Associable, but for temporary use, e.g. for display in tree window, hence the graphics parameters*/
public class PropertyRecord implements Listable, Nameable  {
	protected String name = null;
	protected NameReference nRef;
	public int kind = -1;

	private boolean belongsToBranch = true; //This is settable only from the settings managed by BranchPropertiesInit.
	
	public static ListableVector preferenceRecords = new ListableVector();
	
	public PropertyRecord(String name,int kind){
		this.name = name;
		nRef = NameReference.getNameReference(name);
		this.kind = kind;
	}
	public static int preferredKind(String name){
		PropertyRecord[] props = findInList(MesquiteTree.propertiesSettingsVector, name);
		if (props == null || props.length>1)
			return -1;
		return props[0].kind;
	}

	public void setName(String name){
		this.name = name;
		nRef = NameReference.getNameReference(name);
	}
	public String getName(){
		return name;
	}
	public NameReference getNameReference(){
		return nRef;
	}
	
	public boolean getBelongsToBranch(){
		return belongsToBranch;
	}

	//returns true if succeeds
	public boolean setBelongsToBranch(boolean belongsToBranch, boolean recordIfNew){
		if (kind == Associable.BUILTIN || PropertyRecord.findInList(MesquiteTree.propertiesSettingsVector, nRef, kind) != null)
			return false;
		if (this.belongsToBranch != belongsToBranch){
			//Debugg.println("@  recordInVector;
		}
		this.belongsToBranch = belongsToBranch;
		return true;
	}

	public boolean equals(PropertyRecord other){  //style doesn't matter; just name and kind
		if (other ==null)
			return false;
		return nRef.equals(other.nRef) && kind == other.kind;
	}
	public static PropertyRecord findInList(ListableVector pList, NameReference nr, int kind){
		for (int i=0; i<pList.size(); i++){
			PropertyRecord mi = (PropertyRecord)pList.elementAt(i);
			if (mi.getNameReference().equals(nr) && mi.kind ==kind)
				return mi;
		}
		return null;
	}
	public static PropertyRecord[] findInList(ListableVector pList, String name){
		int count = 0;
		for (int i=0; i<pList.size(); i++){
			PropertyRecord mi = (PropertyRecord)pList.elementAt(i);
			if (mi.getNameReference().equalsString(name))
				count++;
		}
		if (count == 0)
			return null;
		PropertyRecord[] props = new PropertyRecord[count];
		count = 0;
		for (int i=0; i<pList.size(); i++){
			PropertyRecord mi = (PropertyRecord)pList.elementAt(i);
			if (mi.getNameReference().equalsString(name))
				props[count++] = mi;
		}
		return props;
	}
	public static String reportPropertyList(ListableVector pList){
		String s = "Properties (" + pList.size() + ")\n";
		for (int i=0; i<pList.size(); i++){
			PropertyRecord mi = (PropertyRecord)pList.elementAt(i);
			s += mi.toString() + "\n";
		}
		return s;
	}
	
	public String toString(){
		return "Property name: " + name + " kind " + kind;
	}
	
}

