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
import java.io.*;

import mesquite.lib.duties.*;

/** The main thread for executing commands */
public class MainThread extends MesquiteThread {
	public static ListableVector pendingCommands;
	public static MainThread mainThread;
	public static boolean emergencyCancelled = false;
	static PendingCommand currentlyExecuting;
	long c = 0;
	static {
		pendingCommands = new ListableVector(10);
	}
	public MainThread () {
		super();
	}
	boolean dead = false;

	public static void deleteCommand(MesquiteCommand command){
		try{
			while (!commandAlreadyOnQueue(command)) {
				for (int i= 0; i< pendingCommands.size(); i++) {
					PendingCommand pc =  (PendingCommand)pendingCommands.elementAt(i); 
					MesquiteCommand mc = pc.getCommand();
					if (command == mc){
						pendingCommands.removeElement(pc, false);
						break;
					}
				}
			}
		}
		catch (Exception e){
			//MesquiteFile.throwableToLog(null, e);
		}
	}
	public static boolean commandAlreadyOnQueue(MesquiteCommand command){
		try{
			for (int i= 0; i< pendingCommands.size(); i++) {
				PendingCommand pc =  (PendingCommand)pendingCommands.elementAt(i); 
				MesquiteCommand mc = pc.getCommand();
				if (command == mc)
					return true;
			}
		}
		catch (Exception e){
			//MesquiteFile.throwableToLog(null, e);
		}
		return false;
	}
	public static ListableVector listPendingCommands(){
		ListableVector list = new ListableVector(10);
		for (int i= 0; i< pendingCommands.size(); i++) {
			PendingCommand pc =  (PendingCommand)pendingCommands.elementAt(i); 
			MesquiteCommand mc = pc.getCommand();
			if (!mc.hideInList)
				list.addElement(pc, false);
		}
		return list;
	}

	public static PendingCommand getCurrentlyExecuting(){
		return currentlyExecuting;
	}
	public CommandRecord getCommandRecord(){
		if (currentlyExecuting !=null)
			return currentlyExecuting.getCommandRecord();
		return null;
	}
	public void setCommandRecord(CommandRecord c){
		if (currentlyExecuting !=null)
			currentlyExecuting.setCommandRecord(c);
	}
	public String getCurrentCommandName(){
		if (currentlyExecuting !=null)
			return currentlyExecuting.getListName();
		return null;
	}
	public String getCurrentCommandExplanation(){
		if (currentlyExecuting !=null)
			return currentlyExecuting.getExplanation();
		return null;
	}
	/** DOCUMENT */
	public void run() {
		try {
			while (!MesquiteTrunk.mesquiteTrunk.isDoomed() && !isInterrupted()) { 
				while  (pendingCommands.size()==0) {
					Thread.sleep(100);
				}
				try {
					if (pendingCommands.size()>0) {

						PendingCommand pc = (PendingCommand)pendingCommands.elementAt(0);  // get it so that we can set it to busy
						MesquiteCommand c = pc.getCommand();
						pc = (PendingCommand)pendingCommands.elementAt(0);  // pluck it off again
						if (pc != null){
							pendingCommands.removeElement(pc, false);
							pc.setThread(this);
							currentlyExecuting = pc;
							setCurrent(pc.id);
							patience = 5;
							boolean loggerSet = false;
							if (c != null && c.getSupplementalLogger() != null && logger == null){
								MesquiteThread.setLoggerCurrentThread(c.getSupplementalLogger());
								loggerSet = true;
							}
								
							pc.go();
							if (loggerSet){
								MesquiteThread.setLoggerCurrentThread(null);
							}
							setCurrent(-1);
							currentlyExecuting = null;
						}
					}
				}
				catch (Exception e){
					setCurrent(-1);
					currentlyExecuting = null;
					MesquiteDialog.closeWizard();
					MesquiteFile.throwableToLog(this, e);
				}
				catch (Error e){
					if (e instanceof OutOfMemoryError)
						MesquiteMessage.println("OutofMemoryError.  See file startingMesquiteAndMemoryAllocation.txt in the Mesquite_Folder for information on how to increase memory allocated to Mesquite.");
					currentlyExecuting = null;
					setCurrent(-1);
					MesquiteFile.throwableToLog(this, e);
					throw e;
				}
			}
		}
		catch (InterruptedException e){
			Thread.currentThread().interrupt();
		}
		dead = true;
		threadGoodbye();
	}
	public void interrupt(){
			MesquiteDialog.closeWizard();
		super.interrupt();
	}

}

