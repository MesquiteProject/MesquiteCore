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
import java.util.*;
import java.io.*;
import mesquite.lib.duties.*;

/* ��������������������������� commands ������������������������������� */
/*
 * includes commands, buttons, miniscrolls
 *  /* ========================================================================
 */
/* ======================================================================== */
/**
 * An object designed primarily to allow Mesquite, at startup, to harvest a list
 * of available commands. In its default mode, it is nothing but a string
 * comparer (using equalsIgnoresCase or startsWidth), in that its 'compare' and
 * 'compareStart' methods are used in the doCommand methods of Commandable
 * objects to see what command was passed (it is also used in Puppeteer's
 * sendCommands method). However, when in accumulateMode, a CommandChecker
 * always returns false to the query of whether the strings are the same, but in
 * the process accumulates information on what commands are being sought by the
 * Commandable. This means that one call to the doCommand method of the
 * Commandable, passing an accumulating checker, will cause the checker to
 * acquire a list of all the commands sought by the commandable. This system was
 * designed so that the available commands would be harvested automatically at
 * startup, and wouldn't depend on the programmer keeping a separate manual page
 * of commands up to date.
 */
public class CommandChecker {
	boolean accumulateMode = false;

	boolean includeParameters = true;
	
	boolean warnIfNoResponse = true;

	boolean LIMode = false;

	boolean HTMLmode = true;

	boolean skip = false;

	boolean sparse = false; // should be renamed; not just sparse

	Vector explanationStrings, commandNameStrings, menuStrings;

	public static Vector registeredClasses, representingClasses;

	Class classLimited = null;

	/** The file (if any) in which resides the script being executed */
	MesquiteFile file = null;

	public static CommandChecker defaultChecker;
	public static CommandChecker quietDefaultChecker;

	static CommandChecker defaultSkipChecker;
	static {
		registeredClasses = new Vector();
		representingClasses = new Vector();
		defaultChecker = new CommandChecker();
		quietDefaultChecker = new CommandChecker();
		quietDefaultChecker.warnIfNoResponse = false;
		defaultSkipChecker = new CommandChecker();
		defaultSkipChecker.skip = true;
		defaultSkipChecker.accumulateMode = true;
		defaultSkipChecker.includeParameters = true;
	}

	public CommandChecker() {
		explanationStrings = new Vector();
		commandNameStrings = new Vector();
		menuStrings = new Vector();
	}

	private void reinitialize() {
		explanationStrings.removeAllElements();
		commandNameStrings.removeAllElements();
		menuStrings.removeAllElements();
		accumulateMode = false;
		includeParameters = true;
		LIMode = false;
		skip = false;
		classLimited = null;
	}

	/** Sets the file of the script being executed via this checker. */
	public void setFile(MesquiteFile f) {
		file = f;
	}

	/** Returns the file of the script being executed via this checker. */
	public MesquiteFile getFile() {
		return file;
	}

	/**
	 * Sets the mode of the checker: if true is passed, it accumulates a list of
	 * commands instead of comparing a commandname to a target
	 */
	public void setAccumulateMode(boolean mode) {
		accumulateMode = mode;
	}

	/** Returns whether the checker is an accumulator (or comparer). */
	public boolean getAccumulateMode() {
		return accumulateMode;
	}

	/**
	 * Sets whether an accumulating checker is to put '
	 * <li>' at the start of each line of the accumulated commands.
	 */
	public void setLIMode(boolean mode) {
		LIMode = mode;
	}

	/**
	 * Sets whether an accumulating checker is to put HTML codes in the
	 * explanations of commands.
	 */
	public void setHTMLMode(boolean mode) {
		HTMLmode = mode;
	}

	/** Sets checker as sparse in accumulated text. */
	public void setSparseMode() {
		HTMLmode = false;
		LIMode = false;
		sparse = true;
	}

	/**
	 * Sets what exact class the checker is to accumulate commands from (will
	 * not accumulate from super or subclasses).
	 */
	public void accumulateOnlyFrom(Class c) {
		classLimited = c;
	}

	/**
	 * Accumulates (menus and ) commands from the given commandable and passes
	 * back a vector of strings describing the (menus and ) commands.
	 */
	public static CommandChecker accumulate(Commandable c,
			CommandChecker checker) {
		if (c == null)
			return null;
		if (checker == null)
			checker = new CommandChecker();
		else
			checker.reinitialize();
		checker.setAccumulateMode(true);
		if (!checker.sparse)
			checker.accumulateOnlyFrom(c.getClass());


		c.doCommand(null, null, checker);// �
		if (c instanceof MesquiteModule) {
			MesquiteModule mb = (MesquiteModule) c;
			MesquiteMenuItemSpec.checkerMMI = checker;
			MesquiteCMenuItemSpec.checkerMCMI = checker;
			MesquiteSubmenuSpec.checkerMS = checker;
			mb.defineMenus(true);
			MesquiteMenuItemSpec.checkerMMI = null;
			MesquiteCMenuItemSpec.checkerMCMI = null;
			MesquiteSubmenuSpec.checkerMS = null;
		}
		return checker;
	}

	/**
	 * Returns string explaining the operation of the widget (menu item, tool,
	 * button, etc.) that uses the given command; for use in query mode and in
	 * commands pending list
	 */
	public static String getQueryModeString(String item,
			MesquiteCommand command, Object widget) {
		String s = item
		+ " "
		+ CommandChecker.getCommandExplanation(command, null, false,
				false) + "\n";
		Object ref = null;
		if (widget instanceof MesquiteMenuItem)
			ref = ((MesquiteMenuItem) widget).getReferent();
		else if (widget instanceof MesquiteCheckMenuItem)
			ref = ((MesquiteCheckMenuItem) widget).getReferent();
		else if (widget instanceof MesquiteSubmenu)
			ref = ((MesquiteSubmenu) widget).getReferent();
		if (ref != null) {
			String e = "";
			if (ref instanceof MesquiteModuleInfo)
				e += "Class: " + ((MesquiteModuleInfo)ref).getClassName()+"\n";
			if (ref instanceof Listable && ref instanceof Explainable)
				e += "Uses: " + ((Listable) ref).getName() + " ("
				+ ((Explainable) ref).getExplanation() + ")";
			else if (ref instanceof Listable)
				e += "Uses: " + ((Listable) ref).getName();
			else if (ref instanceof Explainable)
				e += "(" + ((Explainable) ref).getExplanation() + ")";
			if (e.length() > 0)
				s += e + '\n';
		}
		if (command != null && command.getOwner() instanceof Listable) {
			s += "Command handled by: "
				+ ((Listable) command.getOwner()).getName() + "\n";
		}
		return s;

	}

	/**
	 * Accumulates (menus and ) commands from the given commandable and passes
	 * back a vector of strings describing the (menus and ) commands.
	 */
	public static String getCommandExplanation(MesquiteCommand command,
			CommandChecker checker, boolean includeParameters) {
		return getCommandExplanation(command, checker, includeParameters, true);
	}

	/**
	 * Accumulates (menus and ) commands from the given commandable and passes
	 * back a vector of strings describing the (menus and ) commands.
	 */
	public static String getCommandExplanation(MesquiteCommand command,
			CommandChecker checker, boolean includeParameters,
			boolean useHTMLmode) {
		if (command == null || command.getOwner() == null)
			return null;
		if (checker == null)
			checker = new CommandChecker();
		else
			checker.reinitialize();
		checker.setAccumulateMode(true);
		checker.setHTMLMode(useHTMLmode);
		checker.includeParameters = includeParameters;
		command.getOwner().doCommand(command.getName(), null,
				checker);// �
		Vector exp = checker.getAccumulatedExplanations();
		if (exp == null || exp.size() == 0)
			return null;
		else
			return (String) exp.elementAt(0);
	}

	/** Adds a string to the vector of strings being accumulated by the checker. */
	public void addString(String s) {
		explanationStrings.addElement(s);
		commandNameStrings.addElement("");
	}

	public void registerMenuItem(MesquiteModule ownerModule, String itemName,
			MesquiteCommand command) {
		if (accumulateMode) {
			String s = "";
			if (LIMode)
				s += "<li>";
			s += "<strong>" + itemName + "</strong>";
			if (command != null) {
				String commandName = command.getName();
				boolean found = false;
				for (int i = 0; i < commandNameStrings.size() && !found; i++) {
					if (commandName.equals((String) commandNameStrings
							.elementAt(i))) {
						s += (String) explanationStrings.elementAt(i);
						found = true;
					}
				}
			}
			menuStrings.addElement(s);
		}
	}

	/*------------------------*/
	StringBuffer sb = new StringBuffer(300);

	private String getEString(String explanation, String parameters,
			String targetCommand) {
		sb.setLength(0);
		if (HTMLmode) {
			if (LIMode) {
				sb.append("<li>");
				sb.append("<strong>" + targetCommand + "</strong>");
			}
			if ((includeParameters && !StringUtil.blank(parameters))
					|| !StringUtil.blank(explanation)) {
				sb.append(" " + "<ul>");
				if (!StringUtil.blank(explanation))
					sb.append(" <li><u>Explanation:</u> " + explanation);
				if (includeParameters && !StringUtil.blank(parameters))
					sb.append(" <li><u>Parameters:</u> " + parameters);
				sb.append("</ul>");
			}
			return sb.toString();
		} else {
			if (LIMode) {
				sb.append(targetCommand);
			}
			if ((includeParameters && !StringUtil.blank(parameters))
					|| !StringUtil.blank(explanation)) {
				if (!StringUtil.blank(explanation))
					sb.append("\nExplanation: " + explanation);
				if (includeParameters && !StringUtil.blank(parameters))
					sb.append("\nParameters:> " + parameters);
			}
			return sb.toString();
		}
	}

	boolean verbose = false;

	/*------------------------*/
	/**
	 * This method either (1) compares the command parameter to the
	 * targetCommand parameter and returns true if they are the same or (2)
	 * accumulates the targetCommand and associated explanations and parameters
	 * strings into a description of the command. Whether it does (1) or (2)
	 * depends on whether it is set to be in accumulateMode. This method is used
	 * typically in the doCommand methods of Commandables.
	 */
	static int mess = 0;

	public boolean compare(Class classCommanded, String explanation,
			String parameters, String command, String targetCommand) {
		if (classLimited != null && classLimited != classCommanded)
			return false;
		else if (accumulateMode) {
			if (targetCommand != null && command != null
					&& !command.equals("null")
					&& !command.equalsIgnoreCase(targetCommand))
				return false;
			String s = null;
			if (sparse) {
				s = explanation + "  Parameters: " + parameters;
				if (explanation == null)
					s = "[No Explanation] ";
				else
					s = explanation;
				if (parameters != null)
					s += "  Parameters: " + parameters;
			} else
				s = getEString(explanation, parameters, targetCommand);
			explanationStrings.addElement(s);
			commandNameStrings.addElement(targetCommand);
			return false;
		}
		boolean targetHit = (targetCommand != null && command != null && command
				.equalsIgnoreCase(targetCommand));
		if (verbose && targetHit)
			MesquiteMessage.println("> " + command + " <to> " + classCommanded);
		return targetHit;
	}

	/**
	 * This method is the same as compare except that when in comparing mode (1)
	 * it returns true of the command starts with the targetCommand as a
	 * substring.
	 */
	public boolean compareStart(Class classCommanded, String explanation,
			String parameters, String command, String targetCommand) {
		if (classLimited != null && classLimited != classCommanded)
			return false;
		else if (accumulateMode) {
			if (targetCommand != null && command != null
					&& !command.equalsIgnoreCase(targetCommand))
				return false;
			String s = null;
			if (sparse) {
				s = explanation + "  Parameters: " + parameters;
				if (explanation == null)
					s = "[No Explanation] ";
				else
					s = explanation;
				if (parameters != null)
					s += "  Parameters: " + parameters;
			} else
				s = getEString(explanation, parameters, targetCommand);
			explanationStrings.addElement(s);
			commandNameStrings.addElement(targetCommand); // targetCommand

			return false;
		}
		boolean targetHit = (targetCommand != null && command != null && StringUtil
				.startsWithIgnoreCase(command, targetCommand));
		if (verbose && targetHit)
			MesquiteMessage.println("> " + command + " <to> " + classCommanded);
		return targetHit;
	}

	/**
	 * returns the vector of strings for commands accumulated by the checker
	 * when in accumulateMode
	 */
	public Vector getAccumulatedCommands() {
		return (Vector) commandNameStrings.clone();
	}

	/**
	 * returns the vector of strings for commands accumulated by the checker
	 * when in accumulateMode
	 */
	public Vector getAccumulatedExplanations() {
		return (Vector) explanationStrings.clone();
	}

	/**
	 * returns the vector of strings for menus accumulated by the checker when
	 * in accumulateMode
	 */
	public Vector getAccumulatedMenus() {
		return (Vector) menuStrings.clone();
	}

	/**
	 * This static method registers a class for inclusion in the list of
	 * commands available (under 'other' in the Scripting Commands page
	 * available from the Help menu in Mesquite). This must be done early in
	 * startup to be useful (e.g., in a module's mesquiteStartup() method). The
	 * first class passed must be instantiable (i.e. Not abstract) and have at
	 * least one constructor without arguments. This first class is the class to
	 * be registered, or a subclass of it. The second class passed is the actual
	 * class whose commands are to be documented. (This second class may be
	 * abstract, which is the reason two classes need to be passed.) See the
	 * documentation for the method commandsFromRegisteredClasses for
	 * information on how the two classes are used.
	 */
	public static void registerClass(Class c, Class representing) {
		registeredClasses.addElement(c);
		representingClasses.addElement(representing);
	}

	/**
	 * Returns a vector of strings showing the classes that were registered with
	 * registerClass and their available commands. As noted under the method
	 * registerClass, two classes are registered at a time: a first class (which
	 * must be instantiable) and a second class (which may be abstract and must
	 * be the same as or a superclass of the first class). The method
	 * commandsFromRegisteredClasses makes an accumulating CommandChecker and
	 * then examines each of the registered pair of classes. For each pair, the
	 * CommandChecker's accumulateOnlyFrom method is the second class, (the
	 * 'representing' parameter in registerClass) to restrict the class from
	 * which commands are to be accumulated to the second (possibly abstract)
	 * class. Then, it instantiates the first class passed, and (if it's
	 * appropriately a Commandable) passes it the CommandChecker to its
	 * doCommand method to accumulate the commands.
	 */
	public static Vector commandsFromRegisteredClasses() {
		CommandChecker checker = new CommandChecker();
		checker.setLIMode(true);
		checker.setAccumulateMode(true);
		for (int i = 0; i < registeredClasses.size(); i++) {
			Class c = (Class) registeredClasses.elementAt(i);
			Class representing = (Class) representingClasses.elementAt(i);
			if (Commandable.class.isAssignableFrom(c)){
				try {
					checker.accumulateOnlyFrom(representing);
					Commandable instance = (Commandable) c.newInstance();
					checker.addString("<li>"
							+ MesquiteModule.getShortClassName(representing)
							+ "<ul>");
					instance.doCommand(null, null,checker);// �
					checker.addString("</ul>");
				} catch (Exception e) {
					if (c != null)
						MesquiteMessage.println("Error getting command from " + c.getName());
					MesquiteDialog.closeWizard();
					MesquiteFile.throwableToLog(null, e);
				} catch (Error e) {
					if (c != null)
						MesquiteMessage.println("Error getting command from " + c.getName());
					MesquiteDialog.closeWizard();
					MesquiteFile.throwableToLog(null, e);
					throw e;
				}
			}
		}
		return checker.getAccumulatedExplanations();
	}

	/* ...................................................... */
	File commandsDirectory;

	String commandsPath;

	Vector dutyClasses;

	private boolean writeCommands = true;

	public static boolean documentationComposed = false;

	long progress = 0;

	ProgressIndicator omp = null;

	/* ...................................................... */
	/**
	 * Composes documentation about modules and scripting. This is done once per
	 * run of Mesquite, the first time a call is made to show a web page.
	 */
	public void composeDocumentation() {
		omp = new ProgressIndicator(null, "Composing web pages... ",
				MesquiteTrunk.mesquiteModulesInfoVector.size(), false);
		omp.start();
		progress = 0;
		omp.setCurrentValue(0);
		documentationComposed = true;
		commandsPath = MesquiteModule.prefsDirectory
		+ MesquiteFile.fileSeparator + "commands"
		+ MesquiteFile.fileSeparator;
		commandsDirectory = new File(commandsPath);
		if (!commandsDirectory.exists())
			commandsDirectory.mkdir();

		composeModuleListing();
		composePuppeteerWebPage();

		Vector special = CommandChecker.commandsFromRegisteredClasses();
		String registeredHTML = " <title>Commands for other objects</title>";
		registeredHTML += "<body bgcolor=\"#ffffcc\">";
		registeredHTML += getNavBar("");
		registeredHTML += "<hr><h1>Commands for other classes</h1>";
		registeredHTML += ("<ul>");
		for (int i = 0; i < special.size(); i++)
			registeredHTML += ((String) special.elementAt(i));
		registeredHTML += ("</ul>");
		registeredHTML += ("</body>");
		MesquiteFile.putFileContents(MesquiteModule.prefsDirectory
				+ MesquiteFile.fileSeparator + "registered.html",
				registeredHTML, true);
		omp.goAway();
		omp = null;

	}

	/* ...................................................... */
	static String getNavBar(String leader) {
		String s = "<table  width=\"137\"><tr><td><img src=\""
			+ MesquiteFile.massageFilePathToURL(MesquiteModule
					.getRootPath() 
					+ "images/mesquiteIcon.gif")
					+ "\"></td><td><table width=\"450\" border=\"1\"  cellspacing=\"1\"><tr>";
		s += "<td align=\"center\" bgcolor=\"#f0e68c\"><a href=\""
			+ MesquiteModule
					.mesquiteWebSite + "\">Manual</a></td>";
		s += "<td align=\"center\" bgcolor=\"#f0e68c\"><a href=\""
			+ (leader + "modules.html") + "\">Modules Loaded</a></td>";
		s += "<td align=\"center\" bgcolor=\"#f0e68c\"><a href=\""
			+ (leader + "commands.html")
			+ "\">Module scripting commands</a></td>";
		s += "<td align=\"center\" bgcolor=\"#f0e68c\"><a href=\""
			+ (leader + "puppeteer.html")
			+ "\">General scripting commands</a></td>";
		s += "<td align=\"center\" bgcolor=\"#f0e68c\"><a href=\""
			+ MesquiteModule
					.mesquiteWebSite 
					+ "//Scripts+%26+Macros" + "\">Scripting</a></td>";
		s += "</tr></table></td></tr></table>";
		return s;
	}

	/* ...................................................... */
	void composePuppeteerWebPage() {
		CommandChecker checker = new CommandChecker();
		checker.setAccumulateMode(true);
		checker.setLIMode(true);
		Puppeteer p = new Puppeteer(MesquiteTrunk.mesquiteTrunk);
		MesquiteInteger pos = new MesquiteInteger(0);
		p.sendCommands(null, "null", pos, ";", false, null, checker);
		Vector commands = checker.getAccumulatedCommands();
		Vector explanations = checker.getAccumulatedExplanations();
		// Vector explan = checker.getAccumulatedExplanations();
		String puppetHTML = " <title>Universal Mesquite scripting commands</title>";
		puppetHTML += "<body bgcolor=\"#ffffcc\">";
		puppetHTML += getNavBar("");
		puppetHTML += "<hr><h1>Universal Mesquite scripting commands</h1>";
		puppetHTML += ("<ul>");
		for (int i = 0; i < explanations.size(); i++) {
			// puppetHTML+=((String)commands.elementAt(i));
			if (!StringUtil.blank((String) explanations.elementAt(i)))
				puppetHTML += ((String) explanations.elementAt(i));
		}
		puppetHTML += ("</ul>");

		puppetHTML += ("</body>");
		MesquiteFile.putFileContents(MesquiteModule.prefsDirectory
				+ MesquiteFile.fileSeparator + "puppeteer.html", puppetHTML,
				true);
	}

	/* ................................................................................................................. */
	void addToDutyClasses(Class duty) {
		int numDuties = dutyClasses.size();
		int found = -1;
		for (int d = 0; d < numDuties && found != -2; d++) {
			Class storedDuty = (Class) dutyClasses.elementAt(d);
			if (duty == storedDuty)
				found = -2;
			else if (storedDuty.isAssignableFrom(duty))
				found = d;
			else if (duty.isAssignableFrom(storedDuty))
				found = d - 1;
		}

		if (found == -1) // not found; add
			dutyClasses.addElement(duty);
		else if (found != -2) // subclass/superclass of existing
			dutyClasses.insertElementAt(duty, found + 1);

	}

	/* ................................................................................................................. */
	void composeModuleListing() {
		dutyClasses = new Vector(50);
		addToDutyClasses(MesquiteTrunk.class);
		addToDutyClasses(CharacterSource.class);
		addToDutyClasses(CharacterModelSource.class);
		addToDutyClasses(TreeSource.class);

		addToDutyClasses(FileCoordinator.class);
		addToDutyClasses(FileInit.class);
		addToDutyClasses(FileInterpreter.class);
		addToDutyClasses(FileElementManager.class);
		addToDutyClasses(ManagerAssistant.class);
		addToDutyClasses(FileAssistant.class);

		addToDutyClasses(BooleanForTree.class);

		addToDutyClasses(NumberForTree.class);
		addToDutyClasses(NumberForCharacter.class);
		addToDutyClasses(NumberForCharAndTree.class);
		addToDutyClasses(NumberForMatrix.class);
		addToDutyClasses(NumberForMatrixAndTree.class);
		addToDutyClasses(NumbersForNodes.class);

		addToDutyClasses(TreeWindowMaker.class);
		addToDutyClasses(TreeDisplayAssistant.class);
		addToDutyClasses(TreeWindowAssistant.class);
		addToDutyClasses(DrawTreeCoordinator.class);
		addToDutyClasses(DrawTree.class);

		addToDutyClasses(NodeLocs.class);
		addToDutyClasses(DrawNamesTreeDisplay.class);

		addToDutyClasses(TaxaWindowMaker.class);
		addToDutyClasses(TaxaDisplayAssistant.class);
		addToDutyClasses(TaxaWindowAssistant.class);
		addToDutyClasses(DrawTaxaCoordinator.class);
		addToDutyClasses(DrawTaxa.class);
		addToDutyClasses(DrawNamesTaxaDisplay.class);

		addToDutyClasses(DataWindowMaker.class);
		addToDutyClasses(DataWindowAssistant.class);

		addToDutyClasses(DrawChart.class);
		Vector moduleListing = new Vector(300);
		String zero = "<title>Available modules</title><body bgcolor=\"#ffffcc\">";
		zero += getNavBar("");
		zero += "<h2>Modules currently available in Mesquite</h2>";
		moduleListing.addElement(zero);
		String one = "The following modules are currently available.  Links are given to the module's manual, if one is available, and to a listing of commands the module understands. (This list is updated every time Mesquite is run.)";
		moduleListing.addElement(one);

		Vector commandsListing = new Vector(300);
		commandsListing
		.addElement("<title>Commands available for scripting</title><body bgcolor=\"#ffffcc\">");
		commandsListing.addElement(getNavBar("")
				+ "<hr><h1>Commands available for scripting</h1>");
		commandsListing
		.addElement("<h2>Scripting language</h2>Mesquite uses a scripting language whose specifications may be found in the <a href=\""
				+ MesquiteModule
						.mesquiteWebSite  
						+ "/Scripts+%26+Macros" + "\">manual</a>.");
		commandsListing
		.addElement("<p>There are some <a href = puppeteer.html>universal commands</a> that can be used in the scripting language.");
		commandsListing
		.addElement("<p>Various objects can be scripted in Mesquite.  These include modules (whose scripting commands can be found via the list below),");
		commandsListing
		.addElement("and <a href = registered.html>other objects</a>.");
		commandsListing.addElement("<h2>Commands of modules</h2><ul>");
		commandsListing
		.addElement("<li><a href = \"commands/mesquite.html\">Mesquite trunk module</a>");
		ModulesInfoVector mods = MesquiteModule.mesquiteTrunk.mesquiteModulesInfoVector;
		for (int i = 0; i < mods.size(); i++) {
			addToDutyClasses(((MesquiteModuleInfo) mods.elementAt(i))
					.getDutyClass());
		}

		int numDuties = dutyClasses.size();
		Class previousDuty = null;
		for (int d = 0; d < numDuties; d++) {
			Class duty = (Class) dutyClasses.elementAt(d);
			if (previousDuty == null || !previousDuty.isAssignableFrom(duty)) {
				moduleListing.addElement("<hr><h3>"
						+ MesquiteModule.getShortClassName(duty) + "</h3>\n");
				previousDuty = duty;
			} else
				moduleListing.addElement("<h4>"
						+ MesquiteModule.getShortClassName(duty) + "</h4>\n");

			int num = mods.size();
			MesquiteModuleInfo mbi;
			boolean first = true;
			for (int i = 0; i < num; i++) {
				mbi = (MesquiteModuleInfo) mods.elementAt(i);
				if (mbi.getDutyClass() == duty) {
					if (first) {
						moduleListing.addElement("Duty performed:  "
								+ mbi.getDutyName() + "<p><ul>");
						first = false;
					}
					addToManual(mbi, moduleListing, commandsListing);
				}
			}
			moduleListing.addElement("</ul>");
		}

		commandsListing.addElement("</ul></body>");

		MesquiteFile.putFileContents(MesquiteModule.prefsDirectory
				+ MesquiteFile.fileSeparator + "commands.html",
				commandsListing, true);
		/*--------------------------------------*/

		moduleListing.addElement("</ul></body>");
		MesquiteFile.putFileContents(MesquiteModule.prefsDirectory
				+ MesquiteFile.fileSeparator + "modules.html", moduleListing,
				true);

	}

	/* ................................................................................................................. */
	void addToManual(MesquiteModuleInfo mBI, Vector moduleListing,
			Vector commandsListing) {
		String gmp = (mBI.getManualPath());

		Vector commands = mBI.getCommands();
		Vector explanations = mBI.getExplanations();
		Vector menus = mBI.getMenus();

		StringBuffer commandHtml = new StringBuffer(500);
		commandHtml.append(" <title>Module: " + mBI.getName() + "</title>");
		commandHtml.append("<body bgcolor=\"#ffffcc\">");
		commandHtml.append(getNavBar("../"));
		commandHtml.append(" <h1>Module: " + mBI.getName());
		if (gmp != null)
			commandHtml.append(" (<a href = \""
					+ MesquiteFile.massageFilePathToURL(mBI.getDirectoryPath()
							+ "manual.html") + "\">manual</a>)");
		commandHtml.append(" </h1>");
		commandHtml.append("Full package name: "
				+ mBI.getModuleClass().getName() + "<br>");
		commandHtml.append("Duty: " + mBI.getDutyName() + " ("
				+ mBI.getDutyClass().getName() + ")");
		commandHtml.append("<hr>");
		commandHtml.append("<h3>Module Explanation</h3>");
		commandHtml.append(mBI.getExplanation());

		commandHtml.append("<ul>");
		if (!StringUtil.blank(mBI.getAuthors()))
			commandHtml.append("<li>Author(s): " + mBI.getAuthors());
		if (!StringUtil.blank(mBI.getVersion()))
			commandHtml.append("<li>Version " + mBI.getVersion());
		commandHtml.append("</ul>");
		if (menus != null && menus.size() > 0) {
			commandHtml.append("<hr><h2>Menu items of " + mBI.getName()
					+ "</h2>");
			commandHtml.append(("<ul>"));
			for (int i = 0; i < menus.size(); i++)
				commandHtml.append(("<li>" + (String) menus.elementAt(i)));
			commandHtml.append(("</ul>"));
		}
		if (commands != null && commands.size() > 0) {
			commandHtml
			.append("<hr><h2>Commands of " + mBI.getName() + "</h2>");
			commandHtml.append(("<ul>"));
			for (int i = 0; i < commands.size(); i++)
				if (!StringUtil.blank((String) commands.elementAt(i))) {
					commandHtml
					.append(("<li><strong>"
							+ (String) commands.elementAt(i)
							+ "</strong>" + (String) explanations
							.elementAt(i)));
				}
			commandHtml.append(("</ul>"));
		}
		commandHtml.append(("</body>"));
		MesquiteFile.putFileContents(commandsPath + mBI.getShortClassName()
				+ ".html", commandHtml.toString(), true);

		StringBuffer s = new StringBuffer(300);
		s.append("<li><strong><font color=\"#009900\">" + mBI.getName()
				+ "</font></strong>\n");
		if (gmp != null)
			s.append(" <img src = \""
					+ MesquiteFile.massageFilePathToURL(MesquiteModule
							.getRootPath()
							+ "images/green-ball-small.gif") 
							+ "\"> <a href = \""
							+ MesquiteFile.massageFilePathToURL(mBI.getDirectoryPath()
									+ "manual.html") + "\"> Manual </a>\n");

		s.append(" <img src = \""
				+ MesquiteFile.massageFilePathToURL(MesquiteModule
						.getRootPath()
						+ "images/blue-ball-small.gif")  
						+ "\"> <a href = \""
						+ MesquiteFile.massageFilePathToURL(commandsPath
								+ mBI.getShortClassName() + ".html")
								+ "\"> Information</a>\n");
		commandsListing.addElement(" <li><a href = \""
				+ MesquiteFile.massageFilePathToURL(commandsPath
						+ mBI.getShortClassName() + ".html") + "\">"
						+ mBI.getName() + "</a>\n");
		s.append("<ul><li>" + mBI.getExplanation() + "</ul>");

		moduleListing.addElement(s);
		if (omp != null)
			omp.setCurrentValue(++progress);
	}

	/* ................................................................................................................. */
	/**
	 * Shows the explanations of menu items and controls (Explainable Components
	 * including tools of tool palettes) for a window. Must be passed the
	 * parental component (typically graphics[0] of the window) and the menu bar
	 * of the window
	 */
	public static void showExplanations(Component c, MenuBar mBar, String name) {
		prepareExplanations(c, mBar, name);
		MesquiteModule.showWebPage(MesquiteModule.prefsDirectory
				+ MesquiteFile.fileSeparator + "tempExplanations.html", true);
	}

	private static boolean prepareExplanations(Component c, MenuBar mBar,
			String name) {
		String content = null;
		CommandChecker checker = new CommandChecker();
		if (mBar != null || c != null) {
			if (mBar == null) {
				content = "<title>Controls for "
					+ name
					+ "</title><body bgcolor=\"#ffffff\"><a name=\"top\"></a>"
					+ getNavBar("") + "<h2>Controls for " + name + "</h2>";
				content += "Below are listed the current controls (such as buttons) pertaining to the window "
					+ name;
			} else if (c == null) {
				content = "<title>Menus for "
					+ name
					+ "</title><body bgcolor=\"#ffffff\"><a name=\"top\"></a>"
					+ getNavBar("") + "<h2>Menus for " + name + "</h2>";
				content += "Below are listed the current menus pertaining to the window "
					+ name;
			} else {
				content = "<title>Menus and Controls for "
					+ name
					+ "</title><body bgcolor=\"#ffffff\"><a name=\"top\"></a>"
					+ getNavBar("") + "<h2>Menus and Controls for " + name
					+ "</h2>";
				content += "Below are listed the current menus and controls (such as buttons) pertaining to the window "
					+ name;
			}
			content += "<ul>";
			if (c != null)
				content += "<li><a href=\"#controls\">Controls (including buttons and tools)</a>";
			if (mBar != null)
				content += "<li><a href=\"#menus\">Menus</a>";
			content += "</ul>";
		}
		if (c != null) {
			content += "<hr><h2><a href=\"#top\"><img src=\""
				+ MesquiteFile.massageFilePathToURL(MesquiteModule
						.getRootPath() 
						+ "images/small.top.arrow.gif")
						+ "\"</a> <a name=\"controls\"></a>Controls (including buttons and tools)</h2>";//
			content += getComponentExplanation(c);

			content += "</ul><hr>";
		}
		if (mBar != null) {

			// MENUS
			content += "<hr><h2><a href=\"#top\"><img src=\""
				+ MesquiteFile.massageFilePathToURL(MesquiteModule
						.getRootPath() 
						+ "images/small.top.arrow.gif")
						+ "\"</a> <a name=\"menus\"></a>Menus</h2>";//
			for (int i = 0; i < mBar.getMenuCount(); i++) {
				Menu menu = mBar.getMenu(i);
				content += "<hr><h4>" + menu.getLabel() + "</h4>"
				+ StringUtil.lineEnding()
				+ getItemExplanation(mBar.getMenu(i), checker, false, false);
			}
		}
		if (content != null) {
			content += "</body>";
			MesquiteFile.putFileContents(MesquiteModule.prefsDirectory
					+ MesquiteFile.fileSeparator + "tempExplanations.html",
					content, true);
			return true;
		}
		return false;
	}

	private static String getComponentExplanation(Component c) {
		String s = "";
		if (c instanceof Explainable && c instanceof ImageOwner) {
			s += "<li><img src = \""
				+ MesquiteFile.massageFilePathToURL(((ImageOwner) c)
						.getImagePath()) + "\"> ";
			s += ((Explainable) c).getExplanation();
		}
		if (c instanceof Container) {
			Component[] cs = ((Container) c).getComponents();
			if (cs != null)
				for (int i = 0; i < cs.length; i++)
					s += getComponentExplanation(cs[i]);
		}
		return s;
	}

	private static String getModuleReference(long id) {
		if (id == 0)
			return "";
		String ex = "<font color=\"#808080\"> (for module ";
		MesquiteModule mb = MesquiteTrunk.mesquiteTrunk
		.findEmployeeWithIDNumber(id);
		if (mb == null)
			ex += "id#" + id + ")</font>";
		else {
			String gmp = MesquiteFile.massageFilePathToURL(mb.getManualPath());
			if (gmp == null)
				gmp = "";
			else
				gmp = " (<a href = \"" + gmp + "\">[manual]</a>)";
			String gcp = MesquiteFile.massageFilePathToURL(mb
					.getCommandPagePath());
			if (StringUtil.blank(gcp))
				ex += "\'" + mb.getName() + "\'" + gmp + ")</font>";
			else
				ex += "\'<a href =\"" + gcp + "\">" + mb.getName() + "</a>\'"
				+ gmp + ")</font>";

		}
		return ex;
	}

	public static String getItemExplanation(MenuItem item,
			CommandChecker checker, boolean label, boolean suppressCommandName) {
		if (item == null)
			return null;
		if (item instanceof MesquiteMenuItem
				&& !((MesquiteMenuItem) item).getDocument())
			return null;
		String ex = "";
		if (item instanceof MesquiteMenuItem) {
			MesquiteMenuItem mmi = (MesquiteMenuItem) item;
			if (mmi.getCommand() != null) {
				if (label)
					ex += "<li><b>" + item.getLabel() + "</b>";
				long id = mmi.getOwnerModuleID();
				ex += getModuleReference(id);
				if (!suppressCommandName){
					ex += " <font color=\"#808080\">(command: "
					+ mmi.getCommand().getName() + ")</font>";
				}
				String expl = CommandChecker.getCommandExplanation(mmi
						.getCommand(), checker, false);
				Object ref = null;
				if ((ref = mmi.getReferent()) != null) {
					String e = "";
					if (ref instanceof Listable && ref instanceof Explainable)
						e += ".  Uses: " + ((Listable) ref).getName() + " ("
						+ ((Explainable) ref).getExplanation() + ")";
					else if (ref instanceof Listable)
						e += ".  Uses: " + ((Listable) ref).getName();
					else if (ref instanceof Explainable)
						e += "(" + ((Explainable) ref).getExplanation() + ")";
					if (e.length() > 0) {
						if (expl.endsWith("</ul>")) {
							StringBuffer sp = new StringBuffer(expl);
							sp.insert(expl.length() - 5, e);
							expl = sp.toString();
						} else {
							expl += e;
						}
					}

				}
				if (!StringUtil.blank(expl))
					ex += "<font color=\"#FF0000\">" + expl + "</font>";
			}
		} else if (item instanceof MesquiteSubmenu) {
			if (label)
				ex += "<li><b>" + item.getLabel() + "</b>";
			MesquiteSubmenu mmi = (MesquiteSubmenu) item;
			long id = mmi.getOwnerModuleID();
			ex += getModuleReference(id);
			if (mmi.getCommand() != null)
				ex += "<font color=\"#808080\"> (command: "
					+ mmi.getCommand().getName() + ")</font>";
		} else if (label) {
			if (!("-".equals(item.getLabel())))
				ex += "<li><b>" + item.getLabel() + "</b>";
		}
		ex += StringUtil.lineEnding();
		if (item instanceof Menu) {
			Menu menu = ((Menu) item);
			String iex = "";
			for (int i = 0; i < menu.getItemCount(); i++) {
				String ix = getItemExplanation(menu.getItem(i), checker, true, false);
				if (!StringUtil.blank(ix))
					iex += ix;
			}
			if (!StringUtil.blank(iex))
				ex += "<ul>" + StringUtil.lineEnding() + iex + "</ul>"
				+ StringUtil.lineEnding();
		}
		return ex;
	}
}

