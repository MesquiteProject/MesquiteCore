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


/* ======================================================================== */
public class Author implements Listable  {
	private String name = "Anonymous";
	private String code = "0";
	private boolean current = false;
	private boolean defaultSettings = true;
	public static boolean addAuthorBlockByDefault = false;
	
	public Author(){
		super();
	}
 	public static boolean authorsEqual(Author a, Author b){
 		if (a == b)
 			return true;
 		if (a == null || b == null)
 			return false;
 		return (StringUtil.stringsEqual(a.name, b.name) && StringUtil.stringsEqual(a.code, b.code));
 	}
 	public String getName(){
 		return name;
 	}
 	public void setCurrent(boolean c){
 		current = c;
 	}
 	public boolean isCurrent(){
 		return current;
 	}
 	public void setName(String name){
 		defaultSettings = false;
 		this.name = name;
 	}
 	public String getCode(){
 		return code;
 	}
 	public void setCode(String code){
 		defaultSettings = false;
 		this.code = code;
 	}
 	public boolean hasDefaultSettings(){	
 		return defaultSettings || (name !=null && "Anonymous".equals(name));
 	}
 	
 	public String toString(){
 		if (code != null)
 			return code;
 		return name;
 	}
}

