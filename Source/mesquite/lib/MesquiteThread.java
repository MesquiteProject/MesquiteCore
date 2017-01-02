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

import java.io.*;

/** A thread for executing commands */
public class MesquiteThread extends Thread implements CommandRecordHolder {
	public static Vector threads;
	static ListableVector doomedIndicators = null; //for progress indicators hidden but not yet disposed; to counter an OS X bug
	static int suppressWaitWindow = 0;
	private long currentlyExecutingID = 0; 
	private long previouslyExecutingID = -1; 
	protected boolean spontaneousIndicator = true;
	public long count = 0;
	public boolean checkOften = false;
	protected int patience = 5;
	private boolean suppressAllProgressIndicators = false;
	private ProgressIndicator progressIndicator;
	int id=0;
	boolean dead = false;
	protected CommandRecord comRec;
	protected boolean sayGoodbye = true;
	public static boolean suppressInteractionAsLibrary = false;
	public static boolean unknownThreadIsScripting = true;
	private Vector hints;  // a vector of MesquiteStrings supplying hints to MesquiteModules during execution, e.g. temporary defaults for module startup
	private Vector cleanUpJobs;
	private int listenerSuppressionLevel = 0; //0 no suppression; 1 lower level only; 2 all suppressed
	public static int numFilesBeingRead =0;
	boolean readingThread = false;
	public boolean resetUIOnMe = true;
	public static String SEPARATETHREADHELPMESSAGE = "If you use a separate thread, you will then regain control of Mesquite once the process starts."+
	 " This has the advantage that it will allow you to continue to use Mesquite.  However, it is dangerous, as you can alter aspects of your data that will eventually cause problems for the separate process.";
	static int numInst = 1;
	static {
		threads = new Vector(10);
		doomedIndicators = new ListableVector(10);
	}
	public MesquiteThread () {
		super();
		threads.addElement(this);
		id = numInst++;
		if (MesquiteTrunk.checkMemory)
			MesquiteTrunk.mesquiteTrunk.logln("Thread started (id " + id + ")");
	}
	public MesquiteThread (Runnable r) {
		super(r);
		threads.addElement(this);
		id = numInst++;
		if (MesquiteTrunk.checkMemory)
			MesquiteTrunk.mesquiteTrunk.logln("Thread started (id " + id + ")");
	}
	public static boolean isThreadBelongingToMesquite(){
		Thread t = Thread.currentThread();
		return (t instanceof MesquiteThread) ||(t instanceof ConsoleThread);
	}
	public static boolean isReadingThread(){
		Thread t = Thread.currentThread();
		if (t instanceof MesquiteThread)
			return ((MesquiteThread)t).isReading();
		return false;
	}
	public String toString(){
		return getClass().getName() + " = " + super.toString();
	}
	public static boolean okToResetUI(){
		Thread thread = Thread.currentThread();
		if (!(thread instanceof MesquiteThread))
			return false;
		return ((MesquiteThread)thread).resetUIOnMe;
	}
	//=====================
	public void setIsReading(boolean isReading){
		this.readingThread = isReading;
	}
	public boolean isReading(){
		return readingThread;
	}
	//=====================
	/*logger for current thread */
	Logger logger = null;
	public void setLogger(Logger logger){
		this.logger = logger;
	}
	public Logger getLogger(){
		return logger;
	}
	public static void setLoggerCurrentThread(Logger logger){
		Thread t = Thread.currentThread();
		if (t instanceof MesquiteThread){
			MesquiteThread mt = ((MesquiteThread)t);
			mt.setLogger(logger);
		}
	}
	public static void loglnToThreadLogger(String s){
		Thread t = Thread.currentThread();
		if (t instanceof MesquiteThread){
			MesquiteThread mt = ((MesquiteThread)t);
			mt.loglnToLogger(s);
		}
	}
	public void loglnToLogger(String s){
		if (logger != null && !loggingSuspended)
			logger.logln(s);
	}
	public static void logToThreadLogger(String s){
		Thread t = Thread.currentThread();
		if (t instanceof MesquiteThread){
			MesquiteThread mt = ((MesquiteThread)t);
			mt.logToLogger(s);
		}
	}
	public void logToLogger(String s){
		if (logger != null && !loggingSuspended)
			logger.log(s);
	}
	boolean loggingSuspended;
	public static void suspendThreadLogging(){
		Thread t = Thread.currentThread();
		if (t instanceof MesquiteThread){
			MesquiteThread mt = ((MesquiteThread)t);
			mt.loggingSuspended = true;
		}
	}
	public static void resumeThreadLogging(){
		Thread t = Thread.currentThread();
		if (t instanceof MesquiteThread){
			MesquiteThread mt = ((MesquiteThread)t);
			mt.loggingSuspended = false;
		}
	}
	
	boolean indicatorSuppressed = false;
	public static boolean pleaseSuppressProgressIndicatorsCurrentThread(){
		Thread t = Thread.currentThread();
		if (t instanceof MesquiteThread){
			MesquiteThread mt = ((MesquiteThread)t);
			return mt.indicatorSuppressed;
		}
		return false;
	}
	//request that explictly made progress indicators be suppressed
	public static void setHintToSuppressProgressIndicatorCurrentThread(boolean s){
		Thread t = Thread.currentThread();
		if (t instanceof MesquiteThread){
			MesquiteThread mt = ((MesquiteThread)t);
			mt.indicatorSuppressed = s;
		}
	}
	//=====================
	//suppression level 0: no suppression; 1: lower level notifications suppressed; 2: 
	public static boolean setListenerSuppressionLevel(int s){
		Thread t = Thread.currentThread();
		if (t instanceof MesquiteThread){
			((MesquiteThread)t).setListenerSuppressionLevelOnThread(s);
			return true;
		}
		return false;
	}
	public static int getListenerSuppressionLevel(){
		Thread t = Thread.currentThread();
		if (t instanceof MesquiteThread){
			return ((MesquiteThread)t).getListenerSuppressionLevelOnThread();
		}
		return 0;
	}
	public int getListenerSuppressionLevelOnThread(){
		return listenerSuppressionLevel;
	}
	public void setListenerSuppressionLevelOnThread(int s){
		listenerSuppressionLevel = s;
	}
	public static void addCleanUpJob(CleanUpJob job){ //the name of the string is the module class name to which it applies; the value is the hint
		Thread t = Thread.currentThread();
		if (t instanceof MesquiteThread){
			MesquiteThread mt = (MesquiteThread)t;
			if (mt.cleanUpJobs == null)
				mt.cleanUpJobs = new Vector();
			mt.cleanUpJobs.addElement(job);
		}

	}
	public static void removeCleanUpJob(CleanUpJob job){ //the name of the string is the module class name to which it applies; the value is the hint
		Thread t = Thread.currentThread();
		if (t instanceof MesquiteThread){
			MesquiteThread mt = (MesquiteThread)t;
			if (mt.cleanUpJobs != null)
			mt.cleanUpJobs.removeElement(job);
		}

	}
	public void doCleanUp(){
			if (cleanUpJobs != null){
				for (int i = 0; i< cleanUpJobs.size(); i++){
					CleanUpJob job = (CleanUpJob)cleanUpJobs.elementAt(i);
					job.cleanUp();
				}
				cleanUpJobs.removeAllElements();
		}
		
	}
	public static void addHint(MesquiteString hint){ //the name of the string is the module class name to which it applies; the value is the hint
		Thread t = Thread.currentThread();
		if (t instanceof MesquiteThread){
			MesquiteThread mt = (MesquiteThread)t;
			if (mt.hints == null)
				mt.hints = new Vector();
			mt.hints.addElement(hint);
		}

	}
	public static String retrieveAndDeleteHint(MesquiteModule mb){
		Thread t = Thread.currentThread();
		if (t instanceof MesquiteThread){
			MesquiteThread mt = (MesquiteThread)t;
			if (mt.hints == null)
				return null;
			for (int i=0; i<mt.hints.size(); i++){
				MesquiteString s = (MesquiteString)mt.hints.elementAt(i);
				if (mb.nameMatches(s.getName())) {
					String q =  s.getValue();
					mt.hints.removeElement(s);
					return q;
				}
			}
		}
		return null;
	}
	public static void triggerWizard(){
		CommandRecord cr = MesquiteThread.getCurrentCommandRecord();
		if (cr != null)
			cr.requestEstablishWizard(true);
	}
	public static void detriggerWizard(){
		CommandRecord comRec = getCurrentCommandRecord();
		if (comRec == null)
			return;
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

	public void run(){
		try {
			super.run();
			if (sayGoodbye)
				threadGoodbye();
		}
		catch (MesquiteException e){
			MesquiteMessage.warnProgrammer("MesquiteException thrown");
		}
	}
	/*
	public static boolean inScriptingCommand(){
		Thread thisThread = Thread.currentThread();

		if (!(thisThread instanceof CommandRecordHolder))
			return false;
		else {
			CommandRecord attachedToThread = ((CommandRecordHolder) thisThread).getCommandRecord();
			if (attachedToThread != null)
				return attachedToThread.inScriptingCommand();			
		}
		return false;
	}
	 */
	/*----------------*/
	int duringNotification = 0;
	public static boolean isDuringNotification(){
		Thread t = Thread.currentThread();
		if (!(t instanceof MesquiteThread))
			return false;
		MesquiteThread mt = (MesquiteThread)t;
		return mt.duringNotification >0;
	}
	public static void incrementDuringNotification(){
		Thread t = Thread.currentThread();
		if (!(t instanceof MesquiteThread))
			return;
		MesquiteThread mt = (MesquiteThread)t;
		mt.duringNotification++;
	}
	public static void decrementDuringNotification(){
		Thread t = Thread.currentThread();
		if (!(t instanceof MesquiteThread))
			return;
		MesquiteThread mt = (MesquiteThread)t;
		mt.duringNotification--;
		if (mt.duringNotification < 0)
			mt.duringNotification = 0;
	}
	/*----------------*/
	public static boolean isScripting(){
		return isScripting(false);
	}
	public static boolean isScripting(boolean diagnose){
		if (suppressInteractionAsLibrary){
			if (diagnose) MesquiteMessage.println("isScripting:actingAsLibrary");
			return true;
		}
		Thread thisThread = Thread.currentThread();

		/*	default behaviour in thread-based system will be:
		 * If not MesquiteThread, treat as scripting (i.e. suppress user interaction)
		 * 		except if Mesquite is starting up (MesquiteTrunk.mesquiteTrunk.isStartupThread(mt)) then treat as nonscripting
		 * If MesquiteThread but no CommandRecord is associatd with thread, treat as nonscripting (i.e. be safe and verbose)
		 * */
		//===  The following is temporary, to check how consistenly a CommandRecord is associated with the current thread
		boolean stackTrace = false;
		int situation = 0;
		boolean shouldBeScripting = unknownThreadIsScripting;

		//First check to see if this is MesquiteThread.  If not, then use default rules
		if (thisThread instanceof mesquite.trunk.PhoneHomeThread){
			if (diagnose) MesquiteMessage.println("isScripting:instanceof mesquite.trunk.PhoneHomeThread");
			return false;
		}
		else if (thisThread instanceof mesquite.lib.ConsoleThread){
			if (diagnose) MesquiteMessage.println("isScripting:instanceof mesquite.lib.ConsoleThread");
			return MesquiteTrunk.consoleListenSuppressed;  //treat as scripting if backgrounded
		}
		else if (!(thisThread instanceof CommandRecordHolder)){ //not a MesquiteThread
			if (MesquiteTrunk.mesquiteTrunk.isStartupShutdownThread(thisThread) || (MesquiteTrunk.isMacOSX() && mesquite.trunk.EAWTHandler.openFileThreads.indexOf(thisThread)>=0)) {
				shouldBeScripting = false;  //startup, shutdown; should be treated as nonscripting, but if scripting then OK
				situation = 1;
				if (diagnose) MesquiteMessage.println("isScripting:!CommandRecordHolder, 1");
				return false;
			}
			else {  //otherwise an unknown thread; treat as scripting
				shouldBeScripting = unknownThreadIsScripting;
				situation = 2;
				if (diagnose) MesquiteMessage.println("isScripting:!CommandRecordHolder, 2");
		}

		}
		//Second see if there is a CommandRecord attached to the current thread
		else {
			CommandRecord attachedToThread = ((CommandRecordHolder) thisThread).getCommandRecord();
			//No CommandRecord attached to current thread, and yet this is a MesquiteThread.  Should be treated as nonscripting
			if (attachedToThread == null){
				shouldBeScripting = false;  
				situation = 3;
				if (diagnose) MesquiteMessage.println("isScripting:CommandRecordHolder, 3");
			}
			//That's OK as long as they agree about scripting
			else {
				shouldBeScripting = attachedToThread.recordIsScripting();  //Note: problem if attached is different command rec and disgrees on scripting!
				if (attachedToThread.recordIsScripting())
					situation = 4;
				else
					situation = 5;
				if (diagnose) MesquiteMessage.println("isScripting:CommandRecordHolder, " + situation);
			}
		}
		/*
		if (shouldBeScripting != isScripting || stackTrace){
			MesquiteMessage.println("@@@@@@@@@@@@@@@@@@@@");
			MesquiteMessage.printStackTrace("UNEXPECTED SCRIPTING STATUS:  " + situation + " (" + isScripting + "; thread: " + thisThread + ")");
		}*/
		return shouldBeScripting;
	}
	public static void setShowWaitWindow(boolean show){
		if (show)
			suppressWaitWindow--;
		else
			suppressWaitWindow++;
	}
	public static void incrementSuppressWaitWindow(){
		suppressWaitWindow++;
	}
	public static void decrementSuppressWaitWindow(){
		suppressWaitWindow--;
		if (suppressWaitWindow < 0){
			MesquiteMessage.printStackTrace("Oops - negative suppressWaitWindow ");
			suppressWaitWindow = 0;
		}
	}

	public static boolean getShowWaitWindow(){
		return suppressWaitWindow<1;
	}

	//not yet used
	public static void setSuppressProgressIndicatorCurrentThread(boolean suppress){
		Thread c = Thread.currentThread();
		if (c instanceof MesquiteThread){
			MesquiteThread mt = (MesquiteThread)c;
			mt.setSpontaneousIndicator(!suppress);
		}
	}
	//not yet used
	public static boolean getSuppressProgressIndicatorCurrentThread(){
		Thread c = Thread.currentThread();
		if (c instanceof MesquiteThread){
			MesquiteThread mt = (MesquiteThread)c;
			return !mt.getSpontaneousIndicator();
		}
		return false;
	}

	//not yet used
	public static void setSuppressAllProgressIndicatorsCurrentThread(boolean suppress){
		Thread c = Thread.currentThread();
		if (c instanceof MesquiteThread){
			MesquiteThread mt = (MesquiteThread)c;
			mt.suppressAllProgressIndicators = suppress;
		}
	}
	public static boolean getSuppressAllProgressIndicators(Thread c){
		if (c !=null && c instanceof MesquiteThread){
			MesquiteThread mt = (MesquiteThread)c;
			return mt.suppressAllProgressIndicators;
		}
		return false;
	}
	public static void doomIndicator(Component indicator){
		if (indicator == null)
			return;
		Frame f = MesquiteWindow.frameOfComponent(indicator);
		if (f != null && f instanceof ProgressWindow){
			f.hide();
			if (MesquiteTrunk.isMacOSX() && MesquiteTrunk.getJavaVersionAsDouble()<1.4)
				doomedIndicators.addElement((ProgressWindow)f, false);
			else
				f.dispose();
		}
		else {
			indicator.setVisible(false);
		}
	}
	public static void doomIndicator(ProgressWindow indicator){
		if (indicator == null)
			return;
		indicator.hide();
		if (MesquiteTrunk.isMacOSX() && MesquiteTrunk.getJavaVersionAsDouble()<1.4)
			doomedIndicators.addElement(indicator, false);
		else
			indicator.dispose();
	}
	public static void surveyDoomedIndicators(){
		if (doomedIndicators==null)
			return;
		try {

			Listable[] dIndicators = doomedIndicators.getElementArray();
			for (int i=0; i<dIndicators.length; i++) {
				ProgressWindow doomed = (ProgressWindow) dIndicators[i];
				doomed.doomTicks++;
				if (doomed.doomTicks >5){
					doomedIndicators.removeElement(doomed, false);
					doomed.dispose();
				}
			}
		}
		catch (Exception e){
			MesquiteMessage.warnProgrammer("***Exception surveying doomed indicators" + e);
			MesquiteFile.throwableToLog(null, e);
		}
	}

	/* Used to be called only for OS X jaguar, whose paint bugs with new window are worked around if resize forced, but resurrected for El Capitan*/
	public static void surveyNewWindows(){
		try {
			if (MesquiteModule.mesquiteTrunk.windowVector.size() == 0)
				return;
			else {
				Enumeration e = MesquiteModule.mesquiteTrunk.windowVector.elements();
				while (e.hasMoreElements()) {
					MesquiteWindow win = (MesquiteWindow)e.nextElement();
					if (win.isVisible()){
						win.tickled++;
						if (win.tickled==1) {
							Toolkit.getDefaultToolkit().sync();
							win.setWindowSize(win.getWindowWidth(), win.getWindowHeight());
						}
					}
				}
			}
			if (MesquiteModule.mesquiteTrunk.dialogVector.size() == 0)
				return;
			else {
				Enumeration e = MesquiteModule.mesquiteTrunk.dialogVector.elements();
				while (e.hasMoreElements()) {
					MesquiteDialog dlog = (MesquiteDialog)e.nextElement();
					if (dlog.isVisible()){
						dlog.tickled++;
						if (dlog.tickled==2) {
							Toolkit.getDefaultToolkit().sync();
							if (!dlog.alreadyDisposed && dlog instanceof ExtensibleDialog)
								dlog.setSize(dlog.getSize().width+1, dlog.getSize().height+1);
							//else
							//	dlog.pack();
						}
					}
				}
			}
		}
		catch (Exception e){
			MesquiteMessage.warnProgrammer("***Exception surveying new windows " + e);
			MesquiteFile.throwableToLog(null, e);
		}
	}
	/**/
	public long getID(){
		return id;
	}
	public int getPatience(){
		return patience;
	}
	protected void setCurrent(long c){
		currentlyExecutingID = c;
	}

	public long getCurrent(){
		return currentlyExecutingID;
	}
	public void setPrevious(long c){
		previouslyExecutingID = c;
	}

	public long getPrevious(){
		return previouslyExecutingID;
	}
	public CommandRecord getCommandRecord(){
		return comRec;
	}
	public void setCommandRecord(CommandRecord c){
		comRec = c;
	}
	public String getCurrentCommandName(){
		return null;
	}
	public String getCurrentCommandExplanation(){
		return null;
	}

	/*.................................................................................................................*/
	public static void pauseForSeconds(double seconds){   
		try {
			Thread.sleep((int)(1000*seconds)); 
		}
		catch (InterruptedException e) {
			return;
		}
	}
	public static CommandRecord getCurrentCommandRecord(){
		Thread mt = Thread.currentThread();
		if (mt instanceof CommandRecordHolder)
			return ((MesquiteThread)mt).getCommandRecord();
		else
			return null;
	}
	public static void setCurrentCommandRecord(CommandRecord c){
		Thread mt = Thread.currentThread();
		if (mt instanceof CommandRecordHolder)
			((MesquiteThread)mt).setCommandRecord(c);
	}
	public static CommandRecord getCurrentCommandRecordDefIfNull(CommandRecord defaultIfNull){
		Thread mt = Thread.currentThread();
		CommandRecord cr = null;
		if (mt instanceof CommandRecordHolder)
			cr = ((CommandRecordHolder)mt).getCommandRecord(); // 
		if (cr == null) {
			if (MesquiteTrunk.debugMode && mt != MesquiteTrunk.startupShutdownThread && (MesquiteTrunk.isMacOSX() &&mesquite.trunk.EAWTHandler.openFileThreads.indexOf(mt)<0) && defaultIfNull == CommandRecord.nonscriptingRecord)
				MesquiteMessage.printStackTrace("@@@@@@@@@@@@@@@@\nNS CommandRecord used because none is attached to thread");
			return defaultIfNull;
		}
		return cr;

	}
	public boolean getSpontaneousIndicator(){
		return spontaneousIndicator;
	}
	public void setSpontaneousIndicator(boolean sp){
		spontaneousIndicator = sp;
	}
	public static boolean setProgressIndicatorCurrentThread(ProgressIndicator progressIndicator){
		Thread thread = Thread.currentThread();
		if (!(thread instanceof MesquiteThread))
			return false;
		MesquiteThread mt = (MesquiteThread)thread;
		if (mt.progressIndicator!=null && progressIndicator != mt.progressIndicator) {
			mt.progressIndicator.goAway();
		}
		CommandRecord cr = mt.getCommandRecord();
		if (cr !=null)
			cr.setProgressIndicator(progressIndicator);
		mt.progressIndicator = progressIndicator;
		if (progressIndicator !=null)
			progressIndicator.setOwnerThread(mt);
		return true;
	}
	public void setProgressIndicator(ProgressIndicator progressIndicator){

		if (this.progressIndicator!=null && progressIndicator != this.progressIndicator) {
			this.progressIndicator.goAway();
		}
		CommandRecord cr = getCommandRecord();
		if (cr !=null)
			cr.setProgressIndicator(progressIndicator);
		this.progressIndicator = progressIndicator;
		if (progressIndicator !=null)
			progressIndicator.setOwnerThread(this);
	}

	public ProgressIndicator getProgressIndicator(){
		return progressIndicator;
	}
	public void threadGoodbye(){
		if (progressIndicator!=null)
			progressIndicator.goAway();
		threads.removeElement(this);
		if (MesquiteTrunk.checkMemory)
			MesquiteTrunk.mesquiteTrunk.logln("Thread disposed (id " + id + ")");
		CommandRecord rc = getCurrentCommandRecord();
		if (rc != null && rc.getScriptingFile()!=null)
			rc.getScriptingFile().closeReading();
		dead = true;
	}
	public boolean dead(){
		return dead;
	}
	public void interrupt(){
		threadGoodbye();
		setPriority(MIN_PRIORITY);
		super.interrupt();
	}


}

