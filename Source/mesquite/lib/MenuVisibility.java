package mesquite.lib;

public class MenuVisibility implements Listable {
	public String label;
	public String command;
	public String arguments;
	public Class commandable;
	public Class dutyClass;
	
	public MenuVisibility(String label, String arguments, String command, Class commandable, Class dutyClass){
		this.label = label;
		this.arguments = arguments;
		this.commandable = commandable;
		this.dutyClass = dutyClass;
		this.command = command;
	}
	public String getName(){
		return label;
	}

	public boolean matchesMenuItem(String label, String arguments, String command, Class commandable, Class dutyClass){
		if (this.command == null)
			return false;
		if (this.dutyClass != dutyClass)
			return false;
		if (dutyClass == null){ //no dutyclass
			if (commandable != this.commandable)
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


}

