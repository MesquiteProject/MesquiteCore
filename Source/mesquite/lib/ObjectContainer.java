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
import java.text.*;


/* ======================================================================== */
/**An object that holds a reference to another object; to pass as argument into method and have filled in response*/
public class ObjectContainer implements Listable {
	Object object;
	static int totalCreated = 0;
	String name = null;
	public ObjectContainer() {
 		totalCreated++;
	}
	public ObjectContainer(Object o) {
 		totalCreated++;
 		object = o;
	}
	public ObjectContainer(String name, Object o) {
 		totalCreated++;
 		object = o;
 		this.name = name;
	}
	public int getID() {
		return totalCreated;
	}
 	public Object getObject(){
 		return object;
 	}
 	public void setObject(Object o){
 		object = o;
 	}
 	public void setName(String name){
 		this.name = name;
 	}
 	public String getName(){
 		if (name == null && object != null && object instanceof Listable)
 			return ((Listable)object).getName();
 		return name;
 	}
}

