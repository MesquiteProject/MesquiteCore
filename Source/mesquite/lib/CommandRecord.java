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
import mesquite.lib.duties.*;

/* ��������������������������� commands ������������������������������� */
/* includes commands,  buttons, miniscrolls */

public class CommandRecord extends Listened {
	public static CommandRecord nonscriptingRecord;
	public static CommandRecord scriptingRecord;
	public static CommandRecord macroRecord; 
	public static CommandRecord dialogRecord;
	private static boolean checkThread = false;
	Listable canceller = null;
	Thread thread;
	public static long numInstances =0; 
	long id = 0; 
	private boolean warnModuleNotFound = true;  
	private boolean warnObjectToldNull = true;
	private boolean emergencyRehire = false;
	private boolean cancelled = false;
	private boolean errorFound = false;
	private boolean isScripting = false;
	private MesquiteFile scriptingFile = null;
	private boolean isMacro = false;
	private long clock = 0;
	ProgressIndicator progressIndicator;
	String progressNote = "";
	private boolean fromCommandLine = false;
	MesquiteDialogParent wizard;
	boolean wizEstablish = false;
	Vector hiringPath = null;
	public static CommandRecord nr = null;
	static {
		dialogRecord = new CommandRecord((MesquiteThread)null, false);
		//dialogRecord.requestEstablishWizard(MesquiteDialog.useWizards);
		nonscriptingRecord = new CommandRecord((MesquiteThread)null, false);
		scriptingRecord = new CommandRecord((MesquiteThread)null, true);
		macroRecord = new CommandRecord((MesquiteThread)null, true);
		macroRecord.isMacro = true;
	}

	public CommandRecord(boolean scripting) { 
		id = numInstances++; 
		this.isScripting = scripting;
	}
	public CommandRecord(Thread thread, boolean scripting) {
		id = numInstances++; 
		this.isScripting = scripting;
		this.thread = thread;
	}
	public static CommandRecord getRecNSIfNull(){
		return MesquiteThread.getCurrentCommandRecordDefIfNull(CommandRecord.nonscriptingRecord);
	}
	public static CommandRecord getRecSIfNull(){
		return MesquiteThread.getCurrentCommandRecordDefIfNull(CommandRecord.scriptingRecord);
	}
	public static CommandRecord getRecDIfNull(){
		return MesquiteThread.getCurrentCommandRecordDefIfNull(CommandRecord.dialogRecord);
	}
	public String toString(){
		String s = "Command Record " + getID();
		s += " (scripting: " + isScripting + ") ";
		if (thread != null){
			s += " for thread " + thread;
			if (thread instanceof MesquiteThread)
				s +=  " id " +  ((MesquiteThread)thread).getID();
		}
		s += " [establish wizard " + wizEstablish + "]";
		return s;
	}
	public static boolean wizardInEffect(){
		CommandRecord rec =  getRecDIfNull();
		return (rec.getWizard() != null || rec.establishWizard());

	}
	/*boolean commandInProgress = false;
	public void setInScriptingCommand(boolean i){
		commandInProgress = i;
	}
	public boolean inScriptingCommand(){
		if (!recordIsScripting())
			return false;
		return commandInProgress;
	}
*/
	public MesquiteDialogParent getWizard(){
		if (wizard != null && wizard.alreadyDisposed)  //just in case no one told me
			wizard = null;
		return wizard;
	}
	public void setWizard(MesquiteDialogParent w){
		MesquiteDialog.currentWizard = w;
		wizard = w;
	}
	
	public boolean establishWizard(){
		return wizEstablish;
	}

	public void requestEstablishWizard(boolean e){
		wizEstablish = e;
	}
	boolean tempWizEstablish = false;
	//called if wizard requested until PendingCommand done
	public void requestEstablishWizardTemp(boolean e){
		wizEstablish = e;
		tempWizEstablish = e;
	}
	public boolean tempWizardWasEstablished(){
		return tempWizEstablish;
	}

	public long getTicks(){
		return clock;
	}
	public static void setHiringPathS(Vector path){
		CommandRecord cr = MesquiteThread.getCurrentCommandRecord();
		if (cr == null){
			if (MesquiteTrunk.debugMode)
			MesquiteMessage.println("setHiringPath when no commandrecord attached to thread");
		}
		else
			cr.setHiringPath(path);
	}
	public void setHiringPath(Vector path){
		hiringPath = path;
	}
	public static Vector getHiringPathS(){
		CommandRecord cr = MesquiteThread.getCurrentCommandRecord();
		if (cr == null){
			if (MesquiteTrunk.debugMode && MesquiteTrunk.startupShutdownThread != Thread.currentThread())
			MesquiteMessage.println("getHiringPath when no commandrecord attached to thread");
			return null;
		}
		else
			return cr.getHiringPath();
	}
	private Vector getHiringPath(){
		return hiringPath;
	}
	public static void dumpHiringPathS(String s){
		CommandRecord cr = MesquiteThread.getCurrentCommandRecord();
		if (cr == null){
			if (MesquiteTrunk.debugMode)
			MesquiteMessage.println("dumpHiringPathS when no commandrecord attached to thread");
		}
		else
			cr.dumpHiringPath(s);
	}
	private void dumpHiringPath(String s){
		if (hiringPath ==null){
			System.out.println(s + "NO HIRING PATH WITH CommandRecord");
			return;
		}
		System.out.println(s + "HIRING PATH WITH CommandRecord");
		String space = "<<==  ";
		for (int i= 0; i<hiringPath.size(); i++){
			MesquiteModuleInfo mmi = (MesquiteModuleInfo)hiringPath.elementAt(i);
			System.out.println(space + mmi.getClassName());
			space += "  ";
		}
	}

	CommandCommunicator echoComm = null;
	public void setEchoCommunicator(CommandCommunicator c){
		echoComm = c;
	}
	public CommandCommunicator getEchoCommunicator(){
		return echoComm;
	}
	public void tickThisRecord(String progressNote){
		
		clock++;
		this.progressNote = progressNote;
		MesquiteWindow.tickClock(progressNote);
		if (thread !=null && thread instanceof MesquiteThread){
			MesquiteThread mt = (MesquiteThread)thread;
			ProgressIndicator pi = mt.getProgressIndicator();
			if (pi !=null) {
				String commandNote = "";
				if (!StringUtil.blank(progressNote))
					commandNote = progressNote + "\n";
				else
					commandNote = "";
				pi.setSecondaryMessage(commandNote);
				pi.setText(commandNote, false);
			}
		}
	}
	
	
	/**  The progress note string will be displayed in the explanation area and in the automatic progress indicator for operations
	 * that take a long time */
	
	public static void tick(String progressNote){
		MesquiteWindow.tickClock(progressNote);
		Thread thread = Thread.currentThread();
		if (thread.isInterrupted()){
//			MesquiteMessage.println("\n\nThe following exception is thrown intentionally to show where the thread was interrupted; it does not mean necessarily there "
//					+ " a bug in the code.");
			//MesquiteInteger i = null;
			//i.setValue("0"); // to generate a null pointer exception
		}
		if (thread !=null && thread instanceof MesquiteThread){
			MesquiteThread mt = (MesquiteThread)thread;
			ProgressIndicator pi = mt.getProgressIndicator();
			if (pi !=null) {
				String commandNote = "";
				if (!StringUtil.blank(progressNote))
					commandNote = progressNote + "\n";
				else
					commandNote = "";
				pi.setSecondaryMessage(commandNote);
				pi.setText(commandNote, false);
			}
		}
	}
	
	/** This is a synonym of tick; it is here because David can never remember the method name "tick".   */
	public static void setDetailsOfProgress(String progressNote){
		tick(progressNote);
	}
	
	public static void incrementProgress(){
		
		
		Thread thread = Thread.currentThread();
		if (thread.isInterrupted()){
//			MesquiteMessage.println("\n\nThe following exception is thrown intentionally to show where the thread was interrupted; it does not mean necessarily there "
//					+ " a bug in the code.");
			MesquiteInteger i = null;
			i.setValue("0"); // to generate a null pointer exception
		}
		if (thread !=null && thread instanceof MesquiteThread){
			MesquiteThread mt = (MesquiteThread)thread;
			ProgressIndicator pi = mt.getProgressIndicator();
			if (pi !=null) {
				pi.increment();
			}
		}
	}

	public void setProgressIndicator(ProgressIndicator pi){
		this.progressIndicator = pi;
	}
	public ProgressIndicator getProgressIndicator(){
		return progressIndicator;
	}

	
	public String getProgressNote(){
		return progressNote;
	}

	public long getID(){
		return id;
	}


	public void setScriptingFile(MesquiteFile file){
		scriptingFile = file;
	}
	public  MesquiteFile getScriptingFile(){
		return scriptingFile;
	}
	
	public static void setScriptingFileS(MesquiteFile file){
		CommandRecord cr = MesquiteThread.getCurrentCommandRecord();
		if (cr == null){
			if (MesquiteTrunk.debugMode)
			MesquiteMessage.printStackTrace("setScriptingFileS when no commandrecord attached to thread");
		}
		else
			cr.scriptingFile = file;
	}

	public static MesquiteFile getScriptingFileS(){
		CommandRecord cr = MesquiteThread.getCurrentCommandRecord();
		if (cr == null){
			if (MesquiteTrunk.debugMode)
			MesquiteMessage.printStackTrace("getScriptingFileS when no commandrecord attached to thread");
		}
		else
			return cr.scriptingFile;
		return null;
	}
	
	public boolean recordIsScripting(){
		return isScripting;
	}
	/*
	public static boolean threadIsScripting(){
		Thread mt = Thread.currentThread();
		if (mt instanceof MesquiteThread) {
			CommandRecord pi = ((MesquiteThread)mt).getCommandRecord();
			if (pi !=null)
				return pi.scripting();
		}
		return false;
	}
	*/
	public void setScripting(boolean s){
		isScripting = s;
	}

	public void setFromCommandLine(boolean s){
		fromCommandLine = s;
	}
	public boolean isFromCommandLine(){
		return fromCommandLine;
	}
	/*&& clean out non-static references &&*/
	public void setMacro(boolean m){ 
		if (this == scriptingRecord || this == nonscriptingRecord)
			return;
		isMacro = m;
	}
	/*&& clean out non-static references &&*/
	public static boolean macro(){ 
		CommandRecord cr = MesquiteThread.getCurrentCommandRecord();
		if (cr == null)
			return false;
		return cr.isMacro;
	}
	/*&& clean out non-static references &&*/
	public void setErrorFound(){ 
		if (this == scriptingRecord || this == nonscriptingRecord)
			return;
		errorFound = true;
	}
	/*&& clean out non-static references &&*/
	public boolean getErrorFound(){ 
		return errorFound;
	}
	/*&& clean out non-static references &&*/
	public void setObjectToldNullWarning(){ 
		if (this == scriptingRecord || this == nonscriptingRecord)
			return;
		warnObjectToldNull = true;
	}
	/*&& clean out non-static references &&*/
	public boolean getObjectToldNullWarning(){ 
		return warnObjectToldNull;
	}
	/*&& clean out non-static references &&*/
	public void setModuleNotFoundWarning(boolean m){ 
		if (this == scriptingRecord || this == nonscriptingRecord)
			return;
		warnModuleNotFound = m;
	}
	/*&& clean out non-static references &&*/
	public boolean getModuleNotFoundWarning(){ 
		return warnModuleNotFound;
	}
	/*&& clean out non-static references &&*/
	public void setEmergencyRehire(boolean m){ 
		if (this == scriptingRecord || this == nonscriptingRecord)
			return;
		emergencyRehire = m;
	}
	/*&& clean out non-static references &&*/
	public boolean getEmergencyRehire(){ 
		return emergencyRehire;
	}
	/*&& clean out non-static references &&*/
	public void cancelCommand(Listable canceller){
		this.canceller = canceller;
		if (this == scriptingRecord)
			MesquiteMessage.warnProgrammer("attempt to cancel scripting Record");
		else if (this == nonscriptingRecord) 
			MesquiteMessage.warnProgrammer("attempt to cancel nonScripting Record");
		cancelled = true;
		if (thread==null)
			notifyListeners(thread, new Notification(MesquiteListener.COMMAND_CANCELLED));
	}
	/*&& clean out non-static references &&*/
	public boolean isCancelled(){
		return cancelled;
	}

}

