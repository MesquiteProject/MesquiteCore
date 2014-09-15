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

import java.awt.*;
import java.net.URI;


/* ======================================================================== */
/**A class to hold a string.  It is used to speed up string checking.  That is, in some circumstances (see ContinuousData)
one can obtain a NameReference containing a string giving the name of some object.  Then, if one later is checking objects
one can ask if its NameReference is the one obtained previously.  Before checking that the contained strings are identical,
it can check if the NameReferences are identical, thus saving lots of time.  Used where some name/string needs to be checked often.*/
public class NameReference implements Listable {
	static ListableVector names = new ListableVector();
	private String value=null;
	private URI mNamespace=null;
	public static long totalCreated = 0;
	public NameReference() {
		totalCreated++;
	}
	public NameReference(String value) {
		if (value == null)
			this.value=value;
		else
			this.value = new String(value);
		totalCreated++;
	}
	/*...........................................................*/
	public static NameReference getNameReference(String name){
		Object obj = names.getElement(name);
		if (obj==null) {
			NameReference nr = new NameReference(name);
			names.addElement(nr, false);
			return nr;
		}
		else
			return (NameReference)obj;
	}
	public boolean equals (NameReference nr){
		if (nr==this)
			return true;
		else if (nr == null )
			return false;
		else if (nr.getValue()==null && value == null)
			return true;
		else if (nr.getValue()==null && value != null)
			return false;
		else if (nr.getValue()!=null && value == null)
			return false;
		else if (nr.getValue().equalsIgnoreCase(value))
			return true;
		return false;
	}
	public static boolean equal (NameReference nr, NameReference nr2){
		if (nr!= null)
			return nr.equals(nr2);
		if (nr2 == null)
			return true;
		return false;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value=new String(value);
	}
	public String getName() {
		return value;
	}
	public void setName(String value) {
		this.value=new String(value);
	}
	public URI getNamespace() {
		return mNamespace;
	}
	public void setNamespace(URI namespace) {
		mNamespace = namespace;
	}
	
	
	public String toString(){
		if (value == null)
			return "NameReference, value is null";
		return "NameReference to String (" + value + ")";
	}
	
}


