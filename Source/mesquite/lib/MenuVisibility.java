package mesquite.lib;

public class MenuVisibility implements Listable {
	
	/*vvvvvvvvvvvvvvvvvvvvv*/
	public static final boolean enabled = false;
	/*^^^^^^^^^^^^^^^^^^^*/
	
	//modes
	public static final int ALL = 0;
	public static final int SIMPLE = 1;
	public static final int EDITING = 2;

	public static int mode = ALL;

	//status
	public static final int NORMAL = 0;
	public static final int HIDDEN = 1;
	public static final int TOBEHIDDEN = 2;
	public static final int HIDDENCLASS = 3;

	public static ListableVector hiddenMenuItems;
	public static ListableVector hiddenPackages;
	static {
		hiddenMenuItems = new ListableVector();
		hiddenPackages = new ListableVector();
		//addPackageToHidden("mesquite.coalesce.");
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
		MesquiteMessage.println("MENU ITEMS");
		for (int i = 0; i< hiddenMenuItems.size(); i++){
			MenuVisibility h = (MenuVisibility)hiddenMenuItems.elementAt(i);
			MesquiteMessage.println("HIDDEN " + h.getName() + " h commandable " + h.commandable + " arguments " + h.arguments + " command " + h.command);
		}
		MesquiteMessage.println("...");
		MesquiteMessage.println("PACKAGES");
		for (int i = 0; i< hiddenPackages.size(); i++){
			MesquiteString h = (MesquiteString)hiddenPackages.elementAt(i);
			MesquiteMessage.println("HIDDEN " + h.getName() + " =  " + h.getValue());
		}
		MesquiteMessage.println("-----^^^-----");

	}
	public static void addPackageToHidden(String packagePath){
		hiddenPackages.addElement(new MesquiteString(packagePath, packagePath), false);
	}
	public static void removePackageFromHidden(String packagePath){
		int i = hiddenPackages.indexOfByName(packagePath);
		if (i>=0)
			hiddenPackages.removeElementAt(i, false);
	}
	
	public static void addMenuItemToHidden(String label, String arguments, MesquiteCommand command, Class dutyClass){
		if (onHiddenMenuItemList(label, arguments, command, dutyClass))
			return;
		if (command == null)
			return;
		Class commandable = null;
		Object owner = command.getOwner();
		if (owner != null)
			commandable = owner.getClass();
		hiddenMenuItems.addElement(new MenuVisibility(label, arguments, command.getName(), commandable, dutyClass), false);
	}

	public static void removeMenuItemFromHidden(String label, String arguments, MesquiteCommand command, Class dutyClass){
		if (!onHiddenMenuItemList(label, arguments, command, dutyClass))
			return;
		if (command == null)
			return;
		Class commandable = null;
		Object owner = command.getOwner();
		if (owner != null)
			commandable = owner.getClass();
		for (int i = 0; i<hiddenMenuItems.size(); i++){
			MenuVisibility vis = (MenuVisibility)hiddenMenuItems.elementAt(i);
			if (vis.matchesMenuItem(label, arguments, command.getName(), commandable, dutyClass)){
				hiddenMenuItems.removeElement(vis, false);
				return;
			}
		}
	}

	static boolean onHiddenMenuItemList(String label, String arguments, MesquiteCommand command, Class dutyClass){
		if (command == null)
			return onHiddenMenuItemList(label, arguments, null, null, dutyClass);
		Object commandable = command.getOwner();
		Class c = null;
		if (commandable != null)
			c = commandable.getClass();

		if (arguments == null)
			arguments = command.getDefaultArguments();
		return onHiddenMenuItemList(label, arguments, command.getName(), c, dutyClass);
	}

	static boolean onHiddenMenuItemList(String label, String arguments, String command, Class commandable, Class dutyClass){
		for (int i = 0; i<hiddenMenuItems.size(); i++){
			MenuVisibility vis = (MenuVisibility)hiddenMenuItems.elementAt(i);
			if (vis.matchesMenuItem(label, arguments, command, commandable, dutyClass)){
				return true;
			}
		}
		return false;
	}
	static boolean onHiddenClassList(Class c){
		if (c == null)
			return false;
		String name = c.getName();
		for (int i = 0; i<hiddenPackages.size(); i++){
			MesquiteString vis = (MesquiteString)hiddenPackages.elementAt(i);
			String hidden = vis.getName();
			if (hidden != null && name.startsWith(hidden))
				return true;
		}
		
		return false;
	}
	public static int isHiddenMenuItem(String label, String arguments, MesquiteCommand command, Class moduleClass){
		return isHiddenMenuItem(label, arguments, command, moduleClass, (Class)null);
	}

	public static int isHiddenMenuItem(String label, String arguments, MesquiteCommand command, Class moduleClass, Class dutyClass){
		if (mode == ALL)
			return NORMAL;
		
		boolean classHidden = onHiddenClassList(moduleClass);
		if (classHidden){
			if (mode == SIMPLE)
				return HIDDEN;
			if (mode == EDITING)
				return HIDDENCLASS;
		}
		boolean onList = onHiddenMenuItemList(label, arguments, command, dutyClass);
		
		if (onList){ 
			if (mode == SIMPLE)
				return HIDDEN;
			if (mode == EDITING)
				return TOBEHIDDEN;
		}
		return NORMAL;
	}
/*
	private static int XXisHiddenMenuItem(String label, String arguments, String command, Class commandable, Class moduleClass){
		return XXisHiddenMenuItem(label, arguments, command, commandable, moduleClass, null);
	}
	private static int XXisHiddenMenuItem(String label, String arguments, String command, Class commandable, Class moduleClass, Class dutyClass){
		if (mode == ALL)
			return NORMAL;
		boolean onList =  onHiddenMenuItemList(label, arguments, command, commandable, dutyClass);
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

