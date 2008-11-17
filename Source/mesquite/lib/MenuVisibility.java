package mesquite.lib;

public class MenuVisibility implements Listable {
	public static final boolean enabled = false;
	
	//modes
	public static final int ALL = 0;
	public static final int SIMPLE = 1;
	public static final int EDITING = 2;

	public static int mode = ALL;

	//status
	public static final int NORMAL = 0;
	public static final int HIDDEN = 1;
	public static final int TOBEHIDDEN = 2;

	public static ListableVector hidden;
	static {
		hidden = new ListableVector();
	}
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

	public String label;
	public String command;
	public String arguments;
	public Class commandable;
	public Class dutyClass;

	public static void report(){
		MesquiteMessage.println("-----vvv-----");
		if (mode == EDITING)
			MesquiteMessage.println("EDITING");
		for (int i = 0; i< hidden.size(); i++){
			MenuVisibility h = (MenuVisibility)hidden.elementAt(i);
			MesquiteMessage.println("HIDDEN " + h.getName() + " h commandable " + h.commandable + " arguments " + h.arguments + " command " + h.command);
		}
		MesquiteMessage.println("-----^^^-----");

	}
	public static void addToHidden(String label, String arguments, MesquiteCommand command, Class dutyClass){
		if (onHiddenList(label, arguments, command, dutyClass))
			return;
		if (command == null)
			return;
		Class commandable = null;
		Object owner = command.getOwner();
		if (owner != null)
			commandable = owner.getClass();
		hidden.addElement(new MenuVisibility(label, arguments, command.getName(), commandable, dutyClass), false);
	}

	public static void removeFromHidden(String label, String arguments, MesquiteCommand command, Class dutyClass){
		if (onHiddenList(label, arguments, command, dutyClass))
			return;
		if (command == null)
			return;
		Class commandable = null;
		Object owner = command.getOwner();
		if (owner != null)
			commandable = owner.getClass();
		for (int i = 0; i<hidden.size(); i++){
			MenuVisibility vis = (MenuVisibility)hidden.elementAt(i);
			if (vis.matches(label, arguments, command.getName(), commandable, dutyClass)){
				hidden.removeElement(vis, false);
				return;
			}
		}
	}

	public static boolean onHiddenList(String label, String arguments, MesquiteCommand command, Class dutyClass){
		if (command == null)
			return onHiddenList(label, arguments, null, null, dutyClass);
		Object commandable = command.getOwner();
		Class c = null;
		if (commandable != null)
			c = commandable.getClass();

		if (arguments == null)
			arguments = command.getDefaultArguments();
		return onHiddenList(label, arguments, command.getName(), c, dutyClass);
	}

	public static boolean onHiddenList(String label, String arguments, String command, Class commandable, Class dutyClass){
		for (int i = 0; i<hidden.size(); i++){
			MenuVisibility vis = (MenuVisibility)hidden.elementAt(i);
			if (vis.matches(label, arguments, command, commandable, dutyClass)){
				return true;
			}
		}
		return false;
	}
	public static int isHidden(String label, String arguments, MesquiteCommand command){
		return isHiddenWD(label, arguments, command, null);
	}

	public static int isHidden(String label, String arguments, String command, Class commandable){
		return isHiddenWD(label, arguments, command, commandable, null);
	}
	public static int isHiddenWD(String label, String arguments, MesquiteCommand command, Class dutyClass){
		if (mode == ALL)
			return NORMAL;
		boolean onList = onHiddenList(label, arguments, command, dutyClass);
		if (onList){ 
			if (mode == SIMPLE)
				return HIDDEN;
			if (mode == EDITING)
				return TOBEHIDDEN;
		}
		return NORMAL;
	}

	public static int isHiddenWD(String label, String arguments, String command, Class commandable, Class dutyClass){
		if (mode == ALL)
			return NORMAL;
		boolean onList =  onHiddenList(label, arguments, command, commandable, dutyClass);
		if (onList){ 
			if (mode == SIMPLE)
				return HIDDEN;
			if (mode == EDITING)
				return TOBEHIDDEN;
		}
		return NORMAL;
	}

	/*public boolean OLDmatches(String label, String arguments, String command, Class commandable, Class dutyClass){
		if (this.command == null)
			return false;
		if (this.dutyClass != dutyClass)
			return false;
		if (dutyClass == null){ //no dutyclass
			if (commandable != this.commandable)
				return false;
			if ((arguments == null && this.arguments != null) || (arguments == null && this.arguments != null))
				return false;
			if (arguments == null){
				return (this.label.equals(label) && this.command.equals(command));
			}
			return (this.label.equals(label) && this.command.equals(command) && arguments.equals(this.arguments));
		}
		else {  //with dutyclass; 
			if ((arguments == null && this.arguments != null) || (arguments == null && this.arguments != null))
				return false;
			if (arguments == null){
				return (this.label.equals(label) && this.command.equals(command));
			}
			return (this.label.equals(label) && this.command.equals(command) && arguments.equals(this.arguments));
		}
	}
	*/
	
	public boolean matches(String label, String arguments, String command, Class commandable, Class dutyClass){
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
			if (this.label.equals(label)) //this is sufficient
					return true;
			if ((arguments == null && this.arguments != null) || (arguments == null && this.arguments != null))
				return false;
			return (this.command.equals(command));
		}
	}


}

