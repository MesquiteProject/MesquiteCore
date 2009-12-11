/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison. 
Version 2.72, December 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib;

public class MenuVisibility implements Listable {
	public String label;
	public String command;
	public String arguments;
	public String commandableClassName;
	public String dutyClass;
	
	public MenuVisibility(String label, String arguments, String command, String commandableClassName, String dutyClass){
		this.label = label;
		this.arguments = arguments;
		this.commandableClassName = commandableClassName;
		this.dutyClass = dutyClass;
		this.command = command;
	}
	public String getName(){
		return label;
	}

	public boolean matchesMenuItem(String label, String arguments, String command, String commandableClassName, String dutyClass){
		if (this.command == null)
			return false;
		if (!stringsEqual(dutyClass,this.dutyClass))
			return false;
		if (dutyClass == null){ //no dutyclass
			if (!stringsEqual(commandableClassName,this.commandableClassName))
				return false;
			if (!this.command.equals(command))
				return false;
			if (this.label.equals(label)) //this is sufficient
					return true;
			return false;
			// ((arguments == null && this.arguments != null) || (arguments == null && this.arguments != null));
		}
		else {  //with dutyclass; 
			return (this.label.equals(label)); //this is sufficient
		/*			return true;
			if ((arguments == null && this.arguments != null) || (arguments == null && this.arguments != null))
				return false;
			return (this.command.equals(command));*/
		}
	}
	/*.................................................................................................................*/
	public static boolean stringsEqual(String a, String b) {
		if (a ==b)
			return true;
		if (a == null && b == null)
			return true;
		if ((a == null && b != null) || (a != null && b == null))
			return false;
		
		return (a.equals(b));
	}


}

