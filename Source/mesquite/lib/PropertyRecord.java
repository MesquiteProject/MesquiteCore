/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib;

import java.awt.Font;

/* ======================================================================== */
/*represents the bits, longs, doubles, strings and objects belonging to parts of an Associable.
 *  Not permanently in an Associable, but for temporary use, e.g. for display in tree window, hence the graphics parameters*/
public class PropertyRecord implements Listable, Nameable  {
	protected String name = null;
	protected NameReference nRef;
	public int kind = -1;

	//as of 4.0, this isn't instantiated at this level, but only as DisplayableTreeProperty
	public PropertyRecord(String name,int kind){
		this.name = name;
		nRef = NameReference.getNameReference(name);
		this.kind = kind;
	}
	public PropertyRecord(NameReference nr,int kind){
		this.name = nr.getName();
		nRef = nr;
		this.kind = kind;
	}

	/*-------------------------------------*/
	//Associables assume not between, except MesquiteTree, which assumes between as default (therefore overridden in TreeProperty)
	public static boolean preferredBetweenness(String name){
		return false;
	}
	/*-------------------------------------*/
	public static int preferredKind(String name){
		return -1;
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
	public int getKind(){
		return kind;
	}

	/*-------------------------------------*/
	public boolean equals(PropertyRecord other){  //style doesn't matter; just name and kind
		if (other ==null)
			return false;
		return nRef.equals(other.nRef) && kind == other.kind;
	}
	/*-------------------------------------*/
	public static void addIfNotInList(ListableVector pList, PropertyRecord pr){
		addIfNotInList(pList, pList, pr);
	}
	/*-------------------------------------*/
	public static void addIfNotInList(ListableVector listToWhichToAdd, ListableVector source){
		for (int i = 0; i< source.size(); i++) {
			PropertyRecord pr = (PropertyRecord)source.elementAt(i);
			addIfNotInList(listToWhichToAdd, pr); 
		}
	}
	/*-------------------------------------*/
	public static void addIfNotInList(ListableVector listToCheck, ListableVector listToWhichToAdd, PropertyRecord pr){
		if (findInList(listToCheck, pr.getNameReference(), pr.getKind())==null){
			listToWhichToAdd.addElement(pr, false);
		}
	}
	/*-------------------------------------*/
	public static void subtractIfInList(ListableVector listToCheck, PropertyRecord pr){
		PropertyRecord found = findInList(listToCheck, pr.getNameReference(), pr.getKind());
		if (found!=null)
			listToCheck.removeElement(found, false);
	}
	/*-------------------------------------*/
	public static PropertyRecord findInList(ListableVector pList, NameReference nr, int kind){
		for (int i=0; i<pList.size(); i++){
			Object obj = pList.elementAt(i);
			if (obj instanceof PropertyRecord){
				PropertyRecord mi = (PropertyRecord)obj;
				if (mi.getNameReference().equals(nr) && mi.kind ==kind)
					return mi;
			}
		}
		return null;
	}
	/*-------------------------------------*/
	public static PropertyRecord findInListNameOnly(ListableVector pList, NameReference nr){
		for (int i=0; i<pList.size(); i++){
			Object obj = pList.elementAt(i);
			if (obj instanceof PropertyRecord){
				PropertyRecord mi = (PropertyRecord)obj;
				if (mi.getNameReference().equals(nr))
					return mi;
			}
		}
		return null;
	}
	/*-------------------------------------*/
	public static boolean inListButOtherKind(ListableVector pList, NameReference nr, int kind){
		return (findInListNameOnly(pList, nr)!= null && findInList(pList, nr,kind)== null);
	}

	/*-------------------------------------*/
	public static PropertyRecord[] findInList(ListableVector pList, String name){
		int count = 0;
		for (int i=0; i<pList.size(); i++){
			Object obj = pList.elementAt(i);
			if (obj instanceof PropertyRecord){
				PropertyRecord mi = (PropertyRecord)obj;
				if (mi.getNameReference().equalsString(name))
					count++;
			}
		}
		if (count == 0)
			return null;
		PropertyRecord[] props = new PropertyRecord[count];
		count = 0;
		for (int i=0; i<pList.size(); i++){
			Object obj = pList.elementAt(i);
			if (obj instanceof PropertyRecord){
				PropertyRecord mi = (PropertyRecord)obj;
				if (mi.getNameReference().equalsString(name))
					props[count++] = mi;
			}
		}
		return props;
	}
	/*-------------------------------------*/
	public static String reportPropertyList(ListableVector pList){
		String s = "Properties (" + pList.size() + ")\n";
		for (int i=0; i<pList.size(); i++){
			Object obj = pList.elementAt(i);
			if (obj instanceof PropertyRecord){
				PropertyRecord mi = (PropertyRecord)obj;
				s += mi.toString() + "\n";
			}
		}
		return s;
	}

	/*-------------------------------------*/
	public String toString(){
		return "Property name: " + name + " kind " + kind;
	}

}

