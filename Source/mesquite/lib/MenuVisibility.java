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

