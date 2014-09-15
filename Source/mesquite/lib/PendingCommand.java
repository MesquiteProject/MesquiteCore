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
import java.io.*;

public class PendingCommand implements SpecialListName, Explainable {
	MesquiteCommand command;
	String arguments;
	public static long numInstances =0; 
	long id = 0; 
	String uiCallInformation;
	//String explanation; 
	boolean logCommand;
	public boolean logRequestFocus = false;
	static int waitCursorDepth =0;
	boolean showWaitCursor;
	boolean running = false;
	boolean crashed = false;
	boolean fromCommandLine = false;
	Thread thread;
	public boolean interrupted = false;
	boolean usingCommand = false;
	CommandRecord comRec = null;
	CommandCommunicator communicator;
	Puppeteer puppeteer;
	Object commandedObject;
	String commands;
	boolean echo;
	boolean useWizard;
	//For a single command
	public PendingCommand (MesquiteCommand command, String arguments, String uiCallInformation, boolean showWaitCursor, boolean logCommand) {
		id = numInstances++; 
		usingCommand = true;
		this.command = command;

		this.arguments = arguments;
		this.uiCallInformation = uiCallInformation;
		this.logCommand = logCommand;
		this.showWaitCursor = showWaitCursor;
		if (showWaitCursor)
			waitCursorDepth++;
	}

	//Will route through a puppeteer; may be a whole script (new as of 0.994)
	public PendingCommand (CommandCommunicator communicator, Puppeteer puppeteer, Object commandedObject, String commands, boolean showWaitCursor) {
		id = numInstances++; 
		usingCommand = false;
		this.puppeteer = puppeteer;
		this.commandedObject = commandedObject;
		this.commands = commands;
		this.communicator = communicator;
		this.showWaitCursor = showWaitCursor;
		if (showWaitCursor)
			waitCursorDepth++;
	}
	public long getID(){
		return id;
	}
	public MesquiteCommand getCommand(){
		return command;
	}

	public void setFromCommandLine(boolean b){
		fromCommandLine = b;
	}
	public void setUseWizard(boolean uw){
		useWizard = uw;
	}
	public boolean getUseWizard(){
		return useWizard;
	}
	public void setEchoToCommunicator(boolean echo){
		this.echo = echo;
	}

	public boolean permitQuitUnqueried(){
		if (command == null)
			return false;
		return !command.getLetMeFinish();
	}
	public void setThread(Thread t){
		thread = t;
	}
	public Thread getThread(){
		return thread;
	}

	public CommandRecord getCommandRecord(){
		return comRec;
	}
	public void setCommandRecord(CommandRecord c){
		comRec = c;
	}
	public boolean hasCrashed(){
		return crashed;
	}
	public String getName(){
		return getListName();
	}
	public String getListName(){
		String s;
		if (usingCommand){
			if (command == null)
				s = "No command";
			else if (command.getOwner() instanceof Listable)
				s= "Command: " + command.getName() + " to " + ((Listable)command.getOwner()).getName() + " ID: " + getID();
			else
				s= "Command: " + command.getName() + " to unknown object";
			/*
			if (running)
				return "(IN PROGRESS) " + s;
			 */
		}
		else {
			if (commandedObject == null)
				s = "No commanded object";
			else if (commandedObject instanceof Listable)
				s= "Commanded object: " + ((Listable)commandedObject).getName();
			else
				s= "Command object of class: " + commandedObject.getClass().getName();
			/*
			if (running)
				return "(IN PROGRESS) " + s;
			 */
		}
		return s;
	}
	/**/
	public String getExplanation(){
		/*String e;
		if (explanation == null)
			e = "";
		else
			e = "\n" + explanation;

		if (running)
			return "(IN PROGRESS) Called by: " + uiCallInformation;*/
		return "Called by: " + uiCallInformation;
	}
	/** DOCUMENT Returns false only if thread died*/
	public void go() {
		int menuResetLevel = MesquiteModule.getMenuResetSuppression();
		String logString = "";
		try {
			if (usingCommand){
				int count = 0;
				//execute command here
				if (command.disposed){
					finishUp();
					return;
				}
				else if (command.getOwner() == null) {
					MesquiteMessage.warnProgrammer("Warning: Command given to null object (" + command.getName() + "  " + arguments + ") + PendingCommand");
					finishUp();
					return;
				}
				else if (command.getOwner() instanceof Component){  //don't send command to graphical component about to be disposed
					MesquiteWindow w = MesquiteWindow.windowOfItem((Component)command.getOwner());
					if (w != null && w.getOwnerModule()!=null && (w.getOwnerModule().isDoomed() || w.disposing)) {
						finishUp();
						return;
					}
				}
				logString = " > " + command.getName();
				if (arguments!=null && !arguments.equals(""))
					logString +=  " \"" + arguments + "\"";
				if (command.getOwner() instanceof Listable)
					logString = ((Listable)command.getOwner()).getName() + logString;
				else
					logString = "[" + command.getOwner().getClass().getName() + "]"  + logString;
				MesquiteException.lastCommand = logString;
				if (logCommand){
					MesquiteModule.mesquiteTrunk.logln(logString);
				}
				running = true;
				CommandRecord previous = MesquiteThread.getCurrentCommandRecord();
				comRec = new CommandRecord(Thread.currentThread(), false);
				if (useWizard)
					comRec.requestEstablishWizard(true);
				if (echo)
					comRec.setEchoCommunicator(communicator);
				comRec.setFromCommandLine(fromCommandLine);


				MesquiteThread.setCurrentCommandRecord(comRec);
				comRec.addListener(command);
				command.notifyDoing(arguments);
				if (!command.getSuppressLogging()) MesquiteWindow.tickClock("Command: " + command.getName());
				MesquiteModule.incrementMenuResetSuppression();
				if (MesquiteCommand.logEverything)
					MesquiteCommand.stampLog(command.getOwner(), command.getName(), arguments, false);
				menuResetLevel = MesquiteModule.getMenuResetSuppression();
				Object returned = command.getOwner().doCommand(command.getName(), arguments, CommandChecker.defaultChecker);  //if a command is executed in this way, assumed that not scripting
				MesquiteModule.decrementMenuResetSuppression();
				
				if (comRec != null) {
					useWizard = useWizard || comRec.tempWizardWasEstablished(); //just in case the wizard was requested along the way
					comRec.requestEstablishWizardTemp(false);
				}
				if (useWizard && comRec != null){
					comRec.requestEstablishWizard(false);
					MesquiteDialogParent dlog = comRec.getWizard();
					if (dlog != null) {
						if (dlog.hiddenForCalculation)
							dlog.setVisible(true);
						MesquiteDialog currentDialog =dlog.getCurrentDialog();
						if (currentDialog != null){
							currentDialog.usingWizard = false;
							currentDialog.dispose();
							MesquiteDialog.currentWizard = null;
						}
						else 
							dlog.pleaseDispose();
					}
				}
				if (!command.getSuppressLogging()) MesquiteWindow.hideClock();
				MesquiteException.lastCommand = "";
				command.notifyDone(arguments);
				if (comRec !=null){
					comRec.removeListener(command);
					ProgressIndicator pi=null;
					if (comRec!=null)
						 pi = comRec.getProgressIndicator();
					if (pi !=null)
						pi.goAway();
				}
				if (logRequestFocus)
					MesquiteTrunk.requestLogFocus();
				MesquiteThread.setCurrentCommandRecord(previous);

			}
			else {
				running = true;
				CommandRecord previous = MesquiteThread.getCurrentCommandRecord();
				comRec = new CommandRecord(Thread.currentThread(), false);
				comRec.setFromCommandLine(fromCommandLine);
				if (echo)
					comRec.setEchoCommunicator(communicator);
				if (comRec !=null){
					MesquiteThread.setCurrentCommandRecord(comRec);

					if (command != null && !command.getSuppressLogging()) MesquiteWindow.tickClock("Command: " + command.getName());
					MesquiteModule.incrementMenuResetSuppression();	
					menuResetLevel = MesquiteModule.getMenuResetSuppression();
					Object returned = puppeteer.sendCommands(commandedObject, commands, new MesquiteInteger(0), null, false, null, CommandChecker.defaultChecker);
					MesquiteModule.decrementMenuResetSuppression();	
					if (command != null &&!command.getSuppressLogging()) MesquiteWindow.hideClock();

					ProgressIndicator pi = comRec.getProgressIndicator();
					if (pi !=null)
						pi.goAway();
					MesquiteThread.setCurrentCommandRecord(previous);
					communicator.commandDone(returned);
					if (logRequestFocus)
						MesquiteTrunk.requestLogFocus();
				}
			}
			finishUp();
			return;
		}
		/*
		catch (InterruptedException e){
			Thread.currentThread().interrupt();
		}
		 */
		catch (Exception e){
			crashed = true;
			MesquiteDialog.closeWizard();
			//corruption warning
			if (Thread.currentThread() instanceof MesquiteThread && ((MesquiteThread)Thread.currentThread()).dead()) {

			}
			else {
				MesquiteFile.throwableToLog(this, e);
				MesquiteTrunk.mesquiteTrunk.exceptionAlert(e, "A command could not be completed because an exception occurred (i.e. a crash; " + e.getClass() + " " + MesquiteException.lastLocMessage() + "). WARNING: there is a chance this crash could have corrupted your data.  Please check your data carefully.  If you save any files, you might best use Save As... in case file saving doesn't work properly.\n\n" + logString);
			}

			//TODO: should have flag in modules and windows that successfully completed startup; if not, then zap them
			finishUp();
			MesquiteTrunk.zeroMenuResetSuppression(); //EXCEPTION HANDLER
			MesquiteTrunk.resetAllMenuBars();
		

		}
		catch (Error e){
			if (!(e instanceof ThreadDeath)){
				crashed = true;
				//corruption warning
				MesquiteFile.throwableToLog(this, e);
				if (e instanceof OutOfMemoryError)
					MesquiteTrunk.mesquiteTrunk.discreetAlert("OutofMemoryError.  See file startingMesquiteAndMemoryAllocation.txt in the Mesquite_Folder for information on how to increase memory allocated to Mesquite. ("+ MesquiteException.lastLocMessage()+ "). WARNING: there is a chance this crash could have corrupted your data.  Please check your data carefully.  If you save any files, you might best use Save As... in case file saving doesn't work properly.\n\n" + logString);
				else
					MesquiteTrunk.mesquiteTrunk.exceptionAlert(e, "A command could not be completed because an error occurred (i.e. a crash; " + e.getClass()  + " " + MesquiteException.lastLocMessage()+ "). WARNING: there is a chance this crash could have corrupted your data.  Please check your data carefully.  If you save any files, you might best use Save As... in case file saving doesn't work properly.\n\n" + logString);
				MesquiteDialog.closeWizard();
				//TODO: should have flag in modules and windows that successfully completed startup; if not, then zap them
				finishUp();
				MesquiteTrunk.zeroMenuResetSuppression(); //EXCEPTION HANDLER
				MesquiteTrunk.resetAllMenuBars();
			
			}
			throw e;

		}
	}

	private void finishUp(){
		running = false;
		if (usingCommand){
			if (command.getOwner() instanceof FileDirtier)
				((FileDirtier)command.getOwner()).fileDirtiedByCommand(command);
		}
		else {
			if (commandedObject instanceof FileDirtier)
				((FileDirtier)commandedObject).fileDirtiedByCommand(null);
		}
		if (showWaitCursor) {
			--waitCursorDepth;
			if (waitCursorDepth<1) //MesquiteCommand.currentThreads.size()==1 && 
				MesquiteWindow.restoreAllCursors();
		}
	}
}

