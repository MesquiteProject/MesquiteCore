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

import mesquite.lib.duties.*;


/* ��������������������������� commands ������������������������������� */
/* includes commands,  buttons, miniscrolls

/* ======================================================================== */
/**Used by modules (and sometimes other Commandables) to return commands that would restore the
module to its current state on file read-in or in other contexts (e.g., cloning a window).  On saving a file, a module (currently Manage Mesquite Blocks)
recurses through the employee tree, collecting their Snapshots, and using them to compose a Mesquite block
for a NEXUS file that will return the active modules and windows to their current state.*/
public class Snapshot {
	public static final int SNAPALL = 0;
	public static final int SNAPDISPLAYONLY = 1;
	String[] lines;
	MesquiteModule[] modules;
	boolean[] conditional;
	int numLines = 0;
	public Snapshot () {
		lines = new String[10];
		modules = new MesquiteModule[10];
		conditional = new boolean[10];
		for (int i=0; i<10; i++) {
			lines[i] = null;
			modules[i] = null;
			conditional[i]=false;
		}
	}
	
	/** Add the given command to the list of commands.  This command should be one that hires an employee, and the current
	module that would represent that hiring should be passed.  The command passed is only the first token (command name),
	not the entire command.  Thus addHiringLine("setTreeDrawer", drawTreeTask) would compose a command of the sort
	"setTreeDrawer 'Arc Tree';".
	<p>This allows the module saving the Nexus block to
	insert immediately afterward the tell It... endTell block of commands that refer to that employee.  (this is
	important to avoid ambiguity of a getEmployee command that can result if more than one employee has the
	same name.  By placing the tell block immediately afterward, that hired employee will get the commands
	appropriate to just it. */
	public void addLine(String command, MesquiteModule module) {
		if (numLines>=lines.length)
			upgrade();
		lines[numLines] = command;
		modules[numLines] = module;
		numLines++;
	}
	/** Add command if there is any snapshot from module*/
	public void addLineIfAnythingToTell(String command, MesquiteModule module) {
		if (numLines>=lines.length)
			upgrade();
		lines[numLines] = command;
		modules[numLines] = module;
		conditional[numLines] = true;
		numLines++;
	}
	/** suppress commands to module; used by module that doesn't want to rehire employee*/
	public void suppressCommandsToEmployee(MesquiteModule module) {
		if (numLines>=lines.length)
			upgrade();
		lines[numLines] = null;
		modules[numLines] = module;
		numLines++;
	}
	/** Add the given command to the list of commands.  The command should be the entire command line EXCEPT the
	semicolon.*/
	public void addLine(String commandLine) {
		if (numLines>=lines.length)
			upgrade();
		lines[numLines] = commandLine;
		modules[numLines] = null;
		numLines++;
	}
	/** Incorporates given Snapshot into current ones, pulling in lines and modules*/
	public void incorporate(Snapshot snap, boolean addTab){
		if (snap !=null) {
			for (int i= 0; i<snap.getNumLines(); i++)
				if (addTab)
					addLine("\t" + snap.getCommand(i), snap.getModule(i));
				else
					addLine(snap.getCommand(i), snap.getModule(i));
		}
	}
	
	static final boolean useClassName= true;
	/** Returns the given command from the list of commands. The line returned is the fully formatted command*/
	public String getLine(int index) {
		if (index < lines.length) {
			if (lines[index]== null)
				return null;
			if (modules[index]!=null) {
				if (useClassName)
					return  lines[index] + " #" + modules[index].getClass().getName() + ";";
				else
					return  lines[index] + " " + StringUtil.tokenize(modules[index].getName()) + ";";//quote
			}
			else
				return lines[index] + ";";
		}
		else
			return "";
	}
	/** Returns the given command from the list of commands.*/
	public String getCommand(int index) {
		if (index < lines.length) {
			return lines[index];
		}
		else
			return "";
	}
	/** Returns the associated module from the list of commands.*/
	public MesquiteModule getModule(int index) {
		if (index < modules.length)
			return modules[index];
		else
			return null;
	}
	/** Returns whether the module is associated with some command.*/
	public boolean modulePresent(MesquiteModule module) {
		for (int i=0; i<modules.length; i++)
			if (modules[i] == module)
				return true;
		return false;
	}
	/** Returns whether the line is conditional on anything to tell*/
	public boolean getConditional(int index) {
		if (index < conditional.length)
			return conditional[index];
		else
			return false;
	}
	/** Returns the number of lines in the snapshot.*/
	public int getNumLines() {
		return numLines;
	}
	/** Dumps the snapshot to the system console*/
	public void dump(){
			for (int i= 0; i<getNumLines(); i++) {
				System.out.println(" command: (" + getCommand(i) + ") module : (" + getModule(i) + ")");
			}
	}
	/** allocates more space in arrays for snapshot  */
	private void upgrade() {
		int oldNum = lines.length;
		String[] newLines = new String[oldNum+10];
		boolean [] newConditional = new boolean[oldNum+10];
		MesquiteModule[] newModules = new MesquiteModule[oldNum+10];
		for (int i=0; i<oldNum; i++) {
			newLines[i] = lines[i];
			newModules[i] = modules[i];
			newConditional[i]= conditional[i];
		}
		for (int i=oldNum; i<oldNum+10; i++) {
			newLines[i] = null;
			newModules[i] = null;
			newConditional[i]=false;
		}
		lines = newLines;
		modules = newModules;
		conditional = newConditional;
	}
	
	public String toString(MesquiteFile file, String spacer){
 		StringBuffer sb = new StringBuffer();
		for (int i = 0; i<getNumLines(); i++) {
				//if conditional then wait to se if anything added
			if (getLine(i)==null)
				;
			else if (getConditional(i) && getModule(i)!=null) {
				String emp = getSnapshotCommands(getModule(i), file, spacer);
				if (!StringUtil.blank(emp)) {
					sb.append(spacer + getLine(i) + StringUtil.lineEnding());
					sb.append(spacer + "tell It;" + StringUtil.lineEnding());
					sb.append(emp);
					sb.append(spacer + "endTell;" + StringUtil.lineEnding());
				}
			}
			else {
				sb.append(spacer + getLine(i) + StringUtil.lineEnding());
  				MesquiteModule mb = getModule(i);
  				if (mb != null) {
			 		String emp = getSnapshotCommands(mb, file, spacer);
					if (!StringUtil.blank(emp)) {
						sb.append(spacer + "tell It;" + StringUtil.lineEnding());
						sb.append(emp);
						sb.append(spacer + "endTell;" + StringUtil.lineEnding());
			 		}
  				}
			}
		}
		return sb.toString();
	}
	public static String getIDSnapshotCommands(FileElementManager module, MesquiteFile file, String spacer) {
 		StringBuffer sb = new StringBuffer();
  		spacer += "\t"; 
		Snapshot snapshot = module.getIDSnapshot(file);
		if (snapshot != null) {
			for (int i = 0; i<snapshot.getNumLines(); i++) {
					//if conditional then wait to se if anything added
				if (snapshot.getLine(i)==null)
					;
				else if (snapshot.getConditional(i) && snapshot.getModule(i)!=null) {
				 	String emp = getSnapshotCommands(snapshot.getModule(i), file, spacer);
					if (!StringUtil.blank(emp)) {
							sb.append(spacer + snapshot.getLine(i) + StringUtil.lineEnding());
							sb.append(spacer + "tell It;" + StringUtil.lineEnding());
							sb.append(emp);
							sb.append(spacer + "endTell;" + StringUtil.lineEnding());
				 		}
				}
 				else {
 					sb.append(spacer + snapshot.getLine(i) + StringUtil.lineEnding());
	  				MesquiteModule mb = snapshot.getModule(i);
	  				if (mb != null) {
				 		String emp = getSnapshotCommands(mb, file, spacer);
						if (!StringUtil.blank(emp)) {
							sb.append(spacer + "tell It;" + StringUtil.lineEnding());
							sb.append(emp);
							sb.append(spacer + "endTell;" + StringUtil.lineEnding());
				 		}
	  				}
  				}
			}
		}
 		return sb.toString();
	}


	/** accumulate snapshot (e.g., as for MESQUITE block of NEXUS file) beginning at the passed module, as if
		writing for the file indicated.  The spacer string is passed to prefix lines with tabs to give an indented organization.

		A problem to be solved are dependencies among modules (especially TreeContexts).  If a snapshot creating a module A needed by module B is written
		after the snapshot creating module B, then module B on starting up will not find the needed module A.   */
	public static String getSnapshotCommands(MesquiteModule module, MesquiteFile file, String spacer) {


		StringBuffer sb = new StringBuffer();
		spacer += "\t"; 
		String previousSpacer = spacer;
		Snapshot snapshot = module.getSnapshot(file);
		if (snapshot != null) {
			for (int i = 0; i<snapshot.getNumLines(); i++) {
				//if conditional then wait to se if anything added
				if (snapshot.getLine(i)==null)
					;
				else if (snapshot.getConditional(i) && snapshot.getModule(i)!=null) {
					String emp = getSnapshotCommands(snapshot.getModule(i), file, spacer);
					if (!StringUtil.blank(emp)) {
						sb.append(spacer + snapshot.getLine(i) + StringUtil.lineEnding());
						sb.append(spacer + "tell It;" + StringUtil.lineEnding());
						sb.append(emp);
						sb.append(spacer + "endTell;" + StringUtil.lineEnding());
					}
				}
				else {
					sb.append(spacer + snapshot.getLine(i) + StringUtil.lineEnding());
					MesquiteModule mb = snapshot.getModule(i);
					if (mb != null) {
						String emp = getSnapshotCommands(mb, file, spacer);
						if (!StringUtil.blank(emp)) {
							sb.append(spacer + "tell It;" + StringUtil.lineEnding());
							sb.append(emp);
							sb.append(spacer + "endTell;" + StringUtil.lineEnding());
						}
					}
				}
			}
			//deal with all employees not dealt with in snapshot
			spacer = previousSpacer;

			for (int i = 0; i< module.getNumberOfEmployees(); i++) {
				MesquiteModule mb = (MesquiteModule)module.getEmployeeVector().elementAt(i);
				if (!snapshot.modulePresent(mb) && (MesquiteTrunk.snapshotMode == Snapshot.SNAPALL || mb.satisfiesSnapshotMode())) {  //employee not covered in snapshot
					String emp = getSnapshotCommands(mb, file, spacer);
					if (!StringUtil.blank(emp)) {
						sb.append(spacer + "getEmployee " + StringUtil.tokenize(module.getEmployeeReference(mb)) + ";" + StringUtil.lineEnding());//quote
						sb.append(spacer + "tell It;" + StringUtil.lineEnding());
						sb.append(emp);
						sb.append(spacer + "endTell;" + StringUtil.lineEnding());
					}
				}
			}
		}
		return sb.toString();
	}
}

