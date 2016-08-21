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
import java.awt.event.*;
import java.io.*;
import mesquite.lib.duties.*;


/* ======================================================================== */
/** An object that sends commands; for use in console input (e.g., ConsoleWindow, ConsoleThread). */
public class CommandCommunicator {
	Puppeteer puppeteer;
	Object objectCommanded = null;
	File currentDirectory = null;
	//Object previousObject = null;
	Vector previousObjects = new Vector();
	Vector previousUseQueues = new Vector();

	Object result;
	boolean echoToSystemOut;
	boolean done = false;
	boolean captureLog = false;
	boolean suppressPrompt = false;
	//boolean previousUseQueue = true;
	boolean useQueue = true;

	public CommandCommunicator(MesquiteModule module, Object initCommanded, boolean echoToSystemOut){
		puppeteer = new Puppeteer(module);
		setObjectCommanded(initCommanded, MesquiteWindow.GUIavailable && !MesquiteWindow.suppressAllWindows, false);

		this.echoToSystemOut = echoToSystemOut;
		if (MesquiteTrunk.suggestedDirectory!=null)
			currentDirectory = new File(MesquiteTrunk.suggestedDirectory);
	}
	public void setObjectCommanded(Object obj, boolean useQueue, boolean showPrompt){
		if (obj == null)
			return;
		previousObjects.addElement(objectCommanded);
		previousUseQueues.addElement(new MesquiteBoolean(useQueue));
		//previousObject = objectCommanded;
		objectCommanded = obj;
		//previousUseQueue = this.useQueue;
		this.useQueue = useQueue;
		
		if (showPrompt)
			showPrompt();

	}
	public void releaseObjectCommanded(Object objectReleased, boolean showPrompt){
		int num = previousObjects.size();

		int i = previousObjects.indexOf(objectReleased);
		if (i>=0){
			previousObjects.removeElement(objectReleased);
			previousUseQueues.removeElementAt(i);
			num = previousObjects.size();
			objectCommanded = previousObjects.elementAt(num-1);
			useQueue = ((MesquiteBoolean)previousUseQueues.elementAt(num-1)).getValue();
		}
		else {
			objectCommanded = MesquiteTrunk.mesquiteTrunk;
			useQueue = true;
		}

		if (showPrompt)
			showPrompt();
	}
	private void revertToPreviousObjectCommanded(boolean showPrompt){
		int num = previousObjects.size();
		if (num>0){
			objectCommanded = previousObjects.elementAt(num-1);
			useQueue = ((MesquiteBoolean)previousUseQueues.elementAt(num-1)).getValue();
			previousObjects.removeElementAt(num-1);
			previousUseQueues.removeElementAt(num-1);
		}
		else {
			objectCommanded = MesquiteTrunk.mesquiteTrunk;
			useQueue = true;
		}

		if (showPrompt)
			showPrompt();
	}
	public void setCaptureLog(boolean capt){
		captureLog = capt;
	}
	public void setSuppressPrompt(boolean s){
		suppressPrompt = s;
	}
	String objectName(Object obj){
		if (obj == null)
			return "no object";
		else if (obj instanceof Listable) {
			if (obj instanceof MesquiteWindow)
				return "Window: " + ((Listable)obj).getName();
			else
				return ((Listable)obj).getName();
		}
		else
			return obj.getClass().getName();
	}
	private void write(String s){
		if (echoToSystemOut)
			MesquiteTrunk.mesquiteTrunk.log(s);
		else
			MesquiteTrunk.mesquiteTrunk.logNoEcho(s); //here use nonechoversion
	}
	private String append(String s, String t){
		if (s == null)
			return t;
		if (t == null)
			return s;
		return s + t;
	}
	//THIS SHOULD NOT BE CALLED ON MAIN EVENT THREAD, as it sleeps until answer returned
	public void enterTyped(String commandLine){
		write(enterInput(commandLine));
	}

	//THIS SHOULD NOT BE CALLED ON MAIN EVENT THREAD, as it sleeps until answer returned
	public String enterInput(String commandLine){
		String output = "\n";
		if (commandLine == null) {
			output += getPrompt();
			return output;
		}
		MesquiteInteger pos = new MesquiteInteger(0);
		String command = ParseUtil.getToken(commandLine, pos);
		String arguments = commandLine.substring(pos.getValue(), commandLine.length());
		if (arguments !=null){
			arguments = StringUtil.stripTrailingWhitespace(arguments);
			if (arguments.length() == 0)
				arguments = null;
			else if (arguments.charAt(arguments.length()-1) == ';')
				arguments = arguments.substring(0, arguments.length()-1);
			if (arguments !=null && ";".equals(arguments))
				arguments = null;
			if (arguments !=null && arguments.length() == 0)
				arguments = null;
		}
		if ("who".equalsIgnoreCase(command)) {
			output += ("Object being commanded: " + objectName(objectCommanded) + "\n");
		}
		else if ("help".equalsIgnoreCase(command)) { //console help
//			*****put all of these here at this level as asynchronous
//			***** make commands to window and module different (window:)
//			***** put console window listing on other thread
//			***** single line commands send to command, not puppetter
			output += ("\nConsole mode commands:\n");
			output += ("who  - indicates what is currently commanded object\n");
			output += ("?    -show commands of currently commanded object (equivalently, sc)\n");
			output += ("??    -show commands of currently commanded object, with explanations\n");
			output += ("? <name of command>    -show explanation for that command of currently commanded object\n");
			output += ("..    - if MesquiteModule, command employer module instead; if window, command its owner module instead\n");
			output += (">    - command the returned object\n");
			output += ("<    - command the previously commanded object\n");
			output += ("mesquite  - command root module of Mesquite\n");
			output += ("ssf <filename>    - send script file to object commanded\n");
			output += ("se    - show immediate employee modules (command to be given to module)\n");
			output += ("ge  <number>  - shift command to employee module, numbered as from command se\n");
			output += ("gr  - shift command to employer module\n");
			output += ("etree    - show employee tree (command to be given to module)\n");
			output += ("jet  <number>  - jump to employee module, numbered as from command etree\n");
			output += ("gm  - shift command to module owning the window (command to be given to window)\n");
			output += ("gw  - shift command to window belonging to module (command to be given to module)\n");
			output += ("sw  - list current windows\n");
			output += ("jw  <number> - jump to window, numbered as from command sw\n");
			output += ("menus - show menus of current window (window related to object commanded)\n");
			output += ("menu <number> - command given menu of current window (window related to object commanded)\n");
			output += ("text  - show text version of window (command to be given to window)\n");
			output += ("sb  - show buttons of any dialog up front\n");
			output += ("press <buttonName>  - press buttons of any dialog up front\n");
			output += ("cd  - show current directory.  The current directory is to explore contents of disk, and as default for openFile command.\n");
			output += ("cd ..  - change directory to parent\n");
			output += ("cd <directoryPath>  - change directory to path\n");
			output += ("ls  - list contents of current directory\n");
			output += ("po  - list previous objects commanded\n");
			//ALSO needed: "tell"
		} 
		else if ("ssf".equalsIgnoreCase(command)) {
			MesquiteProject project = null;
			MesquiteModule module = MesquiteTrunk.mesquiteTrunk;

			if (objectCommanded instanceof MesquiteModule) 
				project = ((MesquiteModule)objectCommanded).getProject();
			else if (objectCommanded instanceof FileElement)
				project = ((FileElement)objectCommanded).getProject();
			else if (objectCommanded instanceof Component) {
				MesquiteWindow w= MesquiteWindow.windowOfItem((Component)objectCommanded);
				if (w != null && w.getOwnerModule() != null)
					project = w.getOwnerModule().getProject();
			}
			else if (objectCommanded instanceof MesquiteWindow) {
				MesquiteWindow w= (MesquiteWindow)objectCommanded;
				if (w != null && w.getOwnerModule() != null)
					project = w.getOwnerModule().getProject();
			}
			String script = null;
			if (project != null) {
				script = MesquiteFile.getFileContentsAsString(MesquiteFile.composePath(project.getHomeDirectoryName(), arguments));
				module = project.getCoordinatorModule();
			}
			else
				script = MesquiteFile.getFileContentsAsString(arguments);
			if (!StringUtil.blank(script)){
				Puppeteer puppeteer = new Puppeteer(module);
				puppeteer.execute(objectCommanded, script, new MesquiteInteger(0), null, false);
			}


		}
		else if ("po".equalsIgnoreCase(command)) {

			for (int i=0; i<previousObjects.size(); i++) {
				output += ("  " + i + "  --  " + previousObjects.elementAt(i) +"\n");
			}
		}
		else if ("se".equalsIgnoreCase(command)) {
			if (objectCommanded instanceof MesquiteModule){
				MesquiteModule mb = (MesquiteModule)objectCommanded;
				output += ("Employee Modules of " + mb.getName() +"\n");
				for (int i=0; i<mb.getEmployeeVector().size(); i++) {
					MesquiteModule mbb = (MesquiteModule)mb.getEmployeeVector().elementAt(i);
					output += ("  " + i + "  --  " + mbb.getName() +"\n");
				}
			}
			else
				output += ("The se command can be given only to modules");
		}
		else if ("cd".equalsIgnoreCase(command)) {
			if (MesquiteTrunk.suggestedDirectory!=null)
				currentDirectory = new File(MesquiteTrunk.suggestedDirectory);

			if ("..".equals(arguments)) {
				if (currentDirectory !=null) {
					currentDirectory = new File(currentDirectory.getParent());
					MesquiteTrunk.suggestedDirectory = currentDirectory.toString();
				}
			}
			else if (!StringUtil.blank(arguments)){
				File cd = new File(arguments);
				if (cd.exists() && cd.isDirectory())
					currentDirectory = cd;
				else if (currentDirectory !=null) {
					cd = new File(currentDirectory + MesquiteFile.fileSeparator + arguments);
					if (cd.exists() && cd.isDirectory())
						currentDirectory = cd;
				}
				MesquiteTrunk.suggestedDirectory = currentDirectory.toString();
			}

			output += ("Current directory: " + currentDirectory);
			if (MesquiteFileDialog.currentFileDialog != null)
				MesquiteFileDialog.currentFileDialog.setDirectory(currentDirectory.toString());
		}
		else if ("ls".equalsIgnoreCase(command)) {
			if (MesquiteTrunk.suggestedDirectory!=null && currentDirectory == null)
				currentDirectory = new File(MesquiteTrunk.suggestedDirectory);
			if (currentDirectory !=null && currentDirectory.exists() && currentDirectory.isDirectory()){
				String[] list = currentDirectory.list();
				for (int i=0; i<list.length; i++) {
					if (i % 3 == 1)
						output += (list[i] +"\n");
					else if (i % 3 == 0)
						output += ("\t" + list[i] +"\n");
					else
						output += ("\t" + list[i]);
				}
			}
		}
		else if ("sw".equalsIgnoreCase(command)) {
			ListableVector w = MesquiteTrunk.windowVector;
			if (w == null || w.size() == 0)
				output += ("There are no windows showing");
			else {
				output += ("Current Windows:\n");
				for (int i=0; i<w.size(); i++) {
					MesquiteWindow mbb = (MesquiteWindow)w.elementAt(i);
					output += ("  " + i + "  --  " + mbb.getName() +"\n");
				}
			}
		}
		else if ("mesquite".equalsIgnoreCase(command)) {
			setObjectCommanded(MesquiteTrunk.mesquiteTrunk, useQueue, false);
		}
		else if ("tellIt".equalsIgnoreCase(command)) {
			setObjectCommanded(result, useQueue, false);
		}
		else if ("jw".equalsIgnoreCase(command)) {
			int i = MesquiteInteger.fromString(arguments);
			ListableVector w = MesquiteTrunk.windowVector;
			if (w == null || w.size() == 0)
				output += ("There are no windows showing");
			else if (i< w.size() && MesquiteInteger.isCombinable(i)){
				setObjectCommanded(w.elementAt(i), useQueue, false);
			}
			else {
				output += "To jump to a window, you need to enter its number\n(see numbers by entering sw)";
			}
		}
		else if ("menus".equalsIgnoreCase(command)) {
			MenuBar menus = getMenuBar(objectCommanded);
			if (menus !=null){
				for (int i= 0; i<menus.getMenuCount(); i++){
					Menu menu = menus.getMenu(i);
					output += ("  " + (i+1) + "  --  " + menu.getLabel() +"\n");
				}
				output += "\nEnter  \"menu i\" to select the i'th menu\n";
			}

		}
		else if ("menu".equalsIgnoreCase(command)) {
			MenuBar menus = getMenuBar(objectCommanded);
			if (menus !=null){
				MesquiteInteger poss = new MesquiteInteger();
				int i = MesquiteInteger.fromFirstToken(arguments, poss);
				if (MesquiteInteger.isCombinable(i))
					i--;
				if (i>=0 && i<menus.getMenuCount() && menus.getMenu(i) instanceof MesquiteMenu){
					((MesquiteMenu)menus.getMenu(i)).listItems();
					setObjectCommanded(menus.getMenu(i), MesquiteWindow.GUIavailable && !MesquiteWindow.suppressAllWindows, false);
				}

			}

		}
		else if ("sb".equalsIgnoreCase(command)) {
			ListableVector d = MesquiteModule.mesquiteTrunk.dialogVector;

			if (d!=null && d.size()>0) {
				Object obj = d.elementAt(d.size()-1);
				if (obj !=null && obj instanceof MesquiteDialog){
					MesquiteDialog dlog = (MesquiteDialog)obj;
					Vector v = new Vector();
					dlog.getButtons(dlog,  v);
					if (v.size()==0)
						output += ("There are no buttons");
					else {
						output += ("Buttons of dialog (to use type \"press buttonName\"):\n");
						for (int i= 0; i< v.size(); i++){
							output += ("(" + (String)v.elementAt(i) + ") ");
						}
						output += ("\n");
					}
				}
			}
			else
				output += ("There are no dialog boxes being shown at the moment.\n");
		}
		else if ("press".equalsIgnoreCase(command)) {
			ListableVector d = MesquiteModule.mesquiteTrunk.dialogVector;

			if (d!=null && d.size()>0) {
				Object obj = d.elementAt(d.size()-1);
				if (obj !=null && obj instanceof MesquiteDialog){
					MesquiteDialog dlog = (MesquiteDialog)obj;
					dlog.selectButton(arguments);
				}
			}
			else
				output += ("There are no dialog boxes being shown at the moment.\n");
		}
		else if ("etree".equalsIgnoreCase(command)) {
			if (objectCommanded instanceof MesquiteModule){
				MesquiteModule mb = (MesquiteModule)objectCommanded;
				output += ("Employee Tree of " + mb.getName() + "  use jet <number> to jump to command that employee\n" + mb.listEmployees("  ", new MesquiteInteger(0)) + "\n");
			}
			else
				output += ("The etree command can be given only to modules");
		}
		else if ("jet".equalsIgnoreCase(command)) {
			if (objectCommanded instanceof MesquiteModule){
				MesquiteModule mb = (MesquiteModule)objectCommanded;
				int i = MesquiteInteger.fromString(arguments);
				if (MesquiteInteger.isCombinable(i)) {
					Object r = mb.getDeepEmployee(i, new MesquiteInteger(0));
					if (r !=null) {
						setObjectCommanded(r, MesquiteWindow.GUIavailable && !MesquiteWindow.suppressAllWindows, false);

					}
				}
			}
			else
				output += ("The jet command can be given only to modules");
		}
		else if ("ge".equalsIgnoreCase(command)) {
			if (objectCommanded instanceof MesquiteModule){
				MesquiteModule mb = (MesquiteModule)objectCommanded;
				int i = MesquiteInteger.fromString(arguments);
				if (MesquiteInteger.isCombinable(i) && i>=0 && i<mb.getEmployeeVector().size()) {
					Object r = mb.getEmployeeVector().elementAt(i);
					if (r !=null) {
						setObjectCommanded(r, MesquiteWindow.GUIavailable && !MesquiteWindow.suppressAllWindows, false);

					}
				}
			}
			else
				output += ("The ge command can be given only to modules");
		}
		else if ("gr".equalsIgnoreCase(command)) {
			if (objectCommanded instanceof MesquiteModule){
				MesquiteModule mb = (MesquiteModule)objectCommanded;
						setObjectCommanded(mb.getEmployer(), MesquiteWindow.GUIavailable && !MesquiteWindow.suppressAllWindows, false);
			}
			else
				output += ("The gr command can be given only to modules");
		}
		else if ("gw".equalsIgnoreCase(command)) {
			if (objectCommanded instanceof MesquiteModule){
				MesquiteModule mb = (MesquiteModule)objectCommanded;
				Object r = mb.getModuleWindow();
				if (r !=null) {
					setObjectCommanded(r, MesquiteWindow.GUIavailable && !MesquiteWindow.suppressAllWindows, false);

				}

			}
			else
				output += ("The gw command can be given only to modules");
		}
		else if ("gm".equalsIgnoreCase(command)) {
			if (objectCommanded instanceof MesquiteWindow){
				MesquiteWindow w = (MesquiteWindow)objectCommanded;
				MesquiteModule r = w.getOwnerModule();
				if (r !=null) {
					setObjectCommanded(r, MesquiteWindow.GUIavailable && !MesquiteWindow.suppressAllWindows, false);

				}

			}
			else
				output += ("The gm command can be given only to windows");
		}
		else if (">".equalsIgnoreCase(command)) { 
			if (result != null) {
				setObjectCommanded(result, MesquiteWindow.GUIavailable && !MesquiteWindow.suppressAllWindows, false);

			}
		}
		else if ("<".equalsIgnoreCase(command)) { 
			revertToPreviousObjectCommanded(false);
		}
		else if ("..".equalsIgnoreCase(command)) { 
			Object r = null;
			if (objectCommanded instanceof MesquiteModule) {
				r = ((MesquiteModule)objectCommanded).getEmployer();
				if (r !=null){
					setObjectCommanded(r, MesquiteWindow.GUIavailable && !MesquiteWindow.suppressAllWindows, false);
				}
			}
			else if (objectCommanded instanceof MesquiteWindow){
				r = ((MesquiteWindow)objectCommanded).getOwnerModule();
				if (r !=null){
					setObjectCommanded(r, MesquiteWindow.GUIavailable && !MesquiteWindow.suppressAllWindows, false);
				}
			}
		}
		else if ("reportCrash".equalsIgnoreCase(command)) {
		//	MesquiteTrunk.mesquiteTrunk.reportCrashToHome(new NullPointerException(),  "Reporting to home");
			MesquiteCommand c = new MesquiteCommand("crash", MesquiteTrunk.mesquiteTrunk);
			c.doItMainThread(null, null, null);
		//	PendingCommand pc = new PendingCommand(this,  puppeteer, MesquiteTrunk.mesquiteTrunk, "crash", false);
		//	MainThread.pendingCommands.addElement(pc, false);
		}
		else if (!StringUtil.blank(commandLine)) {
			boolean useQueueHere = useQueue;
			boolean dontSleep = false;
			if ("exit".equalsIgnoreCase(command) || "quit".equalsIgnoreCase(command)){
				setObjectCommanded(MesquiteTrunk.mesquiteTrunk, useQueue, false);
				dontSleep = true;
				useQueueHere = true;
				
		}
			String c = StringUtil.stripTrailingWhitespace(commandLine);
			if (c.length()>0 && c.charAt(c.length()-1) != ';')
				c += ";";
			done = false;
			if (useQueueHere){

				//HERE set up Pending command and put it on command queue
				PendingCommand pc = new PendingCommand(this,  puppeteer, objectCommanded, c, false);
				pc.logRequestFocus = true;
				pc.setFromCommandLine(true);
				if (captureLog) {
					resultBuffer.setLength(0);
					pc.setEchoToCommunicator(true);
				}
				MainThread.pendingCommands.addElement(pc, false);
				//then sleep inutil Pending command calls back commandDone method of this CommandCommunicator with an object reutrned;
				if (!dontSleep){
					try {
					while (!done)
						Thread.sleep(20);
				}
				catch (InterruptedException e){
				}
				}
			}
			else {				
				if (objectCommanded instanceof Commandable){
					Puppeteer p = new Puppeteer(MesquiteTrunk.mesquiteTrunk);
					CommandRecord prev = MesquiteThread.getCurrentCommandRecord();
					CommandRecord cRec = new CommandRecord(false);
					MesquiteThread.setCurrentCommandRecord(cRec);
					p.execute(objectCommanded, c, new MesquiteInteger(0), null, false, null, null);
					MesquiteThread.setCurrentCommandRecord(prev);
					MesquiteTrunk.requestLogFocus();
				}
				done = true;
			}
			if (result instanceof Commandable)
				output += ("    [> " + objectName(result) + " ]");
			else {
				if (result instanceof String)
					output += ("    [ result: " + result + " ]");
				else 
					output += ("    [ result: " + result + " ]");
				result = null;
			}
			if (captureLog) {
				output += resultBuffer.toString();
				resultBuffer.setLength(0);
			}
		}
		output += getPrompt();
		return output;

	}
	/*------------*/
	StringBuffer resultBuffer = new StringBuffer(100);
	public void log(String s){
		if (s != null)
			resultBuffer.append(s);
	}
	public void logln(String s){
		if (s != null)
			resultBuffer.append(s);
		resultBuffer.append('\n');
	}
	private MenuBar getMenuBar(Object objectCommanded){
		MesquiteWindow w= null;
		if (objectCommanded instanceof MesquiteWindow)
			w = (MesquiteWindow)objectCommanded;
		else if (objectCommanded instanceof MesquiteModule) {
			MesquiteWindow f = ((MesquiteModule)objectCommanded).containerOfModule();
			if (f instanceof MesquiteWindow)
				w = (MesquiteWindow)f;
		}
		else if (objectCommanded instanceof OwnedByModule){
			MesquiteModule mb = ((OwnedByModule)objectCommanded).getOwnerModule();
			if (mb !=null) {
				MesquiteWindow f = mb.containerOfModule();
				if (f instanceof MesquiteWindow)
					w = (MesquiteWindow)f;
			}
		}
		if (w == null)
			return null;
		MenuBar menus = w.getMenuBar();
		return menus;
	}
	public void commandDone(Object returned){
		result = returned;
		done = true;
	}
	public String getPrompt(){

		if (suppressPrompt)
			return "\n";
		return ("\n(" + objectName(objectCommanded) + ") # ");
	}
	public void showPrompt(){
		write(getPrompt());
	}
}

