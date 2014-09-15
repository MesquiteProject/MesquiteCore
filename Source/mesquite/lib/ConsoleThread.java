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
import java.net.*;
import java.util.*;
import java.io.*;
import mesquite.lib.*;
import mesquite.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class ConsoleThread extends Thread {
	CommandCommunicator communicator;
	boolean useSystemIn;
	MesquiteModule module;
	Vector commands;
	public ConsoleThread (MesquiteModule module, Object objectCommanded, boolean useSystemIn) {
		this.module = module;
		setPriority(Thread.MIN_PRIORITY);
		this.useSystemIn = useSystemIn;
		communicator = new CommandCommunicator(module, objectCommanded, true);
		commands = new Vector();
	}
	public void run() {
			String input;
			int count =0;
			byte[] bytes = new byte[1000];
			// fix for windows hanging issue, since System.in.read
			// was preventing file opening on windows
			String noConsoleProperty = System.getProperty("mesquite.lib.noconsoleinput");
			if (!StringUtil.blank(noConsoleProperty)) {
				useSystemIn = false;
			}
			while (!MesquiteTrunk.mesquiteTrunk.mesquiteExiting) { 
				try {

					if (useSystemIn){
						Thread.sleep(20);
						int n = System.in.read(bytes);
						count++;
						if (n>0) {
							communicator.enterTyped(new String(bytes,0, n-1));
						}
					}
					else {

						Thread.sleep(20);
						count++;
						if (commands.size() !=0) {
							String c = (String)commands.elementAt(0);
							commands.removeElementAt(0);
							communicator.enterTyped(c);
						}
					}
				}
				catch (IOException e){
				}
				catch (InterruptedException e){
				}
			}
	}
	public void enterCommand(String c){
		commands.addElement(c);
	}
	public CommandCommunicator getCommunicator(){
		return communicator;
	}
	public static void setConsoleObjectCommanded(Object obj, boolean useQueue, boolean showPrompt){
	if (MesquiteTrunk.consoleThread !=null && MesquiteTrunk.consoleThread.getCommunicator() != null)
		MesquiteTrunk.consoleThread.getCommunicator().setObjectCommanded(obj, useQueue, showPrompt);
	}
	public static void releaseConsoleObjectCommanded(Object objectReleasing, boolean showPrompt){
		if (MesquiteTrunk.consoleThread !=null && MesquiteTrunk.consoleThread.getCommunicator() != null)
			MesquiteTrunk.consoleThread.getCommunicator().releaseObjectCommanded(objectReleasing, showPrompt);
		}
}



