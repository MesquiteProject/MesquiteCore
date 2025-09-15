/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 



Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.ParallelAlterMatrixAsUtility;

import java.util.Date;
import java.util.Vector;

import mesquite.lib.Bits;
import mesquite.lib.CommandChecker;
import mesquite.lib.CommandRecord;
import mesquite.lib.CompatibilityTest;
import mesquite.lib.Debugg;
import mesquite.lib.FileElement;
import mesquite.lib.IntegerField;
import mesquite.lib.ListableVector;
import mesquite.lib.Listened;
import mesquite.lib.MenuOwner;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteLong;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteProject;
import mesquite.lib.NexusBlock;
import mesquite.lib.MesquiteThread;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.Notification;
import mesquite.lib.ProjPanelPanel;
import mesquite.lib.Puppeteer;
import mesquite.lib.ResultCodes;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.AlteredDataParameters;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.DataAlterer;
import mesquite.lib.duties.DataAltererParallelizable;
import mesquite.lib.duties.FileCoordinator;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.MQPanel;
import mesquite.lib.ui.ProgressIndicator;
import mesquite.lists.lib.CharMatricesListProcessorUtility;
import mesquite.minimal.DrawHierarchy.DrawHierarchy;

/* ======================================================================== */
public class ParallelAlterMatrixAsUtility extends CharMatricesListProcessorUtility {
	static int numThreads = 2;

	/* ................................................................................................................. */
	public String getName() {
		return "Parallelized Alter Matrices";
	}

	public String getNameForMenuItem() {
		return "Parallel Alter Matrices...";
	}

	public String getExplanation() {
		return "Alters selected matrices in List of Character Matrices window, with the option of using parallel processing (multithreading) to speed the completion.";
	}

	DataAlterer firstAlterTask = null;
	static boolean beaned = false;
	/* ................................................................................................................. */
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		if (arguments != null) {
			firstAlterTask = (DataAlterer) hireNamedEmployee(DataAltererParallelizable.class, arguments);
			if (firstAlterTask == null)
				return sorry(getName() + " couldn't start because the requested data alterer wasn't successfully hired.");
		}
		else if (!MesquiteThread.isScripting()) {
			firstAlterTask = (DataAlterer) hireEmployee(DataAltererParallelizable.class, "Alterer of matrices");
			if (firstAlterTask == null)
				return sorry(getName() + " couldn't start because no matrix alterer module obtained.");
		}
		if (!queryOptions()) {
			fireEmployee(firstAlterTask);
			return false;
		}
		if (!beaned){
			postBean("ParallelAlterMatrixAsUtility-started");
			beaned = true;
		}
		return true;
	}

	public String getNameForProcessorList() {
		if (firstAlterTask != null)
			return getName() + "(" + firstAlterTask.getName() + ")";
		return getName();
	}

	/* ................................................................................................................. */
	public String getNameAndParameters() {
		if (firstAlterTask == null)
			return "Alter Matrices";
		else
			return firstAlterTask.getNameAndParameters();
	}

	/* ................................................................................................................. */
	/* ................................................................................................................. */
	public String preparePreferencesForXML() {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "numThreads", numThreads);
		return buffer.toString();
	}

	public void processSingleXMLPreference(String tag, String flavor, String content) {
		processSingleXMLPreference(tag, null, content);
	}

	/* ................................................................................................................. */
	public void processSingleXMLPreference(String tag, String content) {
		if ("numThreads".equalsIgnoreCase(tag))
			numThreads = MesquiteInteger.fromString(content);
	}

	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setDataAlterer ", firstAlterTask);
		return temp;
	}

	/* ................................................................................................................. */
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the module that alters data", "[name of module]", commandName, "setDataAlterer")) {
			DataAlterer temp = (DataAlterer) replaceEmployee(DataAltererParallelizable.class, arguments, "Data alterer", firstAlterTask);
			if (temp != null) {
				firstAlterTask = temp;
				return firstAlterTask;
			}

		}
		else
			return super.doCommand(commandName, arguments, checker);
		return null;
	}

	/* ................................................................................................................. */
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(containerOfModule(), "Number of Parallel Calculations", buttonPressed);
		queryDialog.addLargeOrSmallTextLabel("The calculations will be performed in parallel, on several threads. Choose the number of parallel threads according to your computer's multiprocessing capabilities.");
		IntegerField integerField = queryDialog.addIntegerField("Number of threads", numThreads, 20, 1, 255);
		queryDialog.addLargeOrSmallTextLabel("(Note: the first matrix will be processed alone, and then the others in parallel.)");
		queryDialog.addLargeOrSmallTextLabel("The calculations tend to slow down after they have completed 1000 to 2000 matrices, for reasons mysterious to the Mesquite developers. "
				+"If you are processing many matrices, you may need to alter the first 1000 matrices by selecting those rows, then save the file, then reopening it for the next 1000, and so on. Alternatively, you may need to run it overnight.");

		queryDialog.setDefaultTextComponent(integerField.getTextField());
		queryDialog.setDefaultComponent(integerField.getTextField());

		queryDialog.completeAndShowDialog(true);

		boolean OK = buttonPressed.getValue() == 0;
		if (OK) {
			if (!integerField.isValidInteger()) {
				alert("The number of threads must be a valid integer.");
				OK = false;
			}
			else {
				int temp = integerField.getValue();
				if (MesquiteInteger.isCombinable(temp) && temp > 0 && temp < 256) {
					numThreads = temp;
					storePreferences();
				}
				else {
					alert("The number of threads must be between 1 and 255.");
					OK = false;
				}
			}
		}
		queryDialog.dispose();
		return (OK);
	}
	/* ................................................................................................................. */

	boolean firstTime = true;

	/** if returns true, then requests to remain on even after operateOnTaxas is called. Default is false */
	public boolean pleaseLeaveMeOn() {
		return false;
	}

	/* ................................................................................................................. */
	static int PATIENCE = 3; // How much longer than first matrix is subsequent matrix considered stalled
	static int SLEEPTIME = 50; // How much sleep between each check of threads
	static long REPORTDELAY = 10000; // If there is a stalled calculation (determined by PATIENCE), how often to report (in milliseconds)
	Bits matricesDone;

	/* ................................................................................................................. */
	/** Called to operate on the CharacterData blocks. Returns true if taxa altered */
	public boolean operateOnDatas(ListableVector datas, MesquiteTable table) {

		incrementMenuResetSuppression(numThreads + 1);
		DrawHierarchy.suppressNodeRepaints = true;
		CompatibilityTest test = firstAlterTask.getCompatibilityTest();
		MesquiteProject project = getProject();
		if (project != null) {
			project.getCoordinatorModule().setWhomToAskIfOKToInteractWithUser(this);
			project.incrementProjectWindowSuppression();
		}
		Vector v = pauseAllPausables();
		matricesDone = new Bits(datas.size());
		// Do the first matrix separately to set up the parameters of the alteration
		boolean doneFirstMatrix = false;
		int successFirstMatrix = -1;
		long startTime = System.currentTimeMillis();
		logln("Parallel Alter Matrices started at " + StringUtil.getDateTime(new Date(startTime)));
		for (int im = 0; im < datas.size() && !doneFirstMatrix; im++) {
			CharacterData data = (CharacterData) datas.elementAt(im);
			if (test.isCompatible(data, project, this)) {
				if (datas.size() > 1)
					logln("Altering first matrix \"" + data.getName() + "\"");
				AlteredDataParameters alteredDataParameters = new AlteredDataParameters();
				successFirstMatrix = firstAlterTask.alterData(data, null, null, alteredDataParameters);
				doneFirstMatrix = true;
				if (successFirstMatrix == ResultCodes.SUCCEEDED) {
					matricesDone.setBit(im);
					Notification notification = new Notification(MesquiteListener.DATA_CHANGED, alteredDataParameters.getParameters(), null);
					if (alteredDataParameters.getSubcodes() != null)
						notification.setSubcodes(alteredDataParameters.getSubcodes());
					data.notifyListeners(this, notification);
					if (datas.size() > 1)
						logln("First matrix altered. About to alter others in parallel on " + numThreads + " threads.");
				}
			}
		}

		if (successFirstMatrix < 0) {
			unpauseAllPausables(v);
			if (project != null)
				project.decrementProjectWindowSuppression();

			decrementMenuResetSuppression(numThreads + 1);
			return false;
		}

		firstTime = false;
		ListableVector sectionTimings = new ListableVector();
		long startParallel = System.currentTimeMillis();
		long longWait = (startParallel - startTime) * PATIENCE;
		ProgressIndicator progIndicator = new ProgressIndicator(project, "Altering matrices", "", datas.size(), true);
		progIndicator.start();
		progIndicator.setText("Setting up " + numThreads + " threads");

		//Whether to notify within the threads. If not, a cruder notification is thereby given, but this isn't editing by hand
		notifyAsYouGo = false;
		//Whether to use a chunked system (see below)
		boolean chunked = false;
		//Pause time every 1000 to garbage collect, in case this helps. A good pause might be 5000 (5 seconds);
		long pause = 0;

		if (!notifyAsYouGo)
			for (int im = 0; im<datas.size(); im++){
				CharacterData data = (CharacterData) datas.elementAt(im);
				data.setNotificationsOnOff(false);
			}
		long ListenednotificationsMadeTotal = Listened.notificationsMadeTotal;
		long MQPanelrepaintRequestsTotal = MQPanel.repaintRequestsTotal;
		long ProjPanelPanelrefreshRequestsTotal = ProjPanelPanel.refreshRequestsTotal;
		long MenuOwnerallMenuBarResetsTotal = MenuOwner.allMenuBarResetsTotal;
		long MesquiteFilefilesOpenTotal = MesquiteFile.filesOpenTotal;
		long MesquiteProjecttotalCreated = MesquiteProject.totalCreated;
		long MesquiteProjecttotalDisposed = MesquiteProject.totalDisposed;
		long FileElementtotalDisposed = FileElement.totalDisposed;
		long FileElementtotalTrueFileElementCreated = FileElement.totalTrueFileElementCreated;
		long NexusBlocktotalCreated = NexusBlock.totalCreated;
		long NexusBlocktotalDisposed = NexusBlock.totalDisposed;

		if (MesquiteTrunk.developmentMode){
			logln("");
			logln(">>>>>BEFORE>> Element report " + getProject().elementsReport());
			logln("");
		}


		//A system to divide the run into chunks of 500 or 1000 to see if that avoids the slowdown that starts at about 1000 matrices. 
		//Didn't seem to help much, so disabled by chunked == false, in which case just make the chunk size as big as the total number.
		int numMatricesInChunk = 1500;
		int numMatricesTotal = datas.size();
		if (!chunked ||numMatricesTotal<numMatricesInChunk*1.5) 
			numMatricesInChunk = numMatricesTotal;


		aborted = false;
		int reportAtMatrix = -1;
		long lastReportTime = System.currentTimeMillis();
		long startTimeThisSection  =  System.currentTimeMillis();
		int sectionStart = 1; //last section recorded in listableVector
		boolean thisIsFirstChunk = true;
		int millennium = 0;
		for (int firstMatrixInChunk = 1; firstMatrixInChunk<numMatricesTotal; firstMatrixInChunk += numMatricesInChunk){

			//=================================================
			// making threads and getting them started
			AlterThread[] threads = new AlterThread[numThreads];
			int blockSize = (numMatricesInChunk - 1) / numThreads + 1; // first one already done
			if (blockSize == 0)
				blockSize = 1;
			for (int i = 0; i < numThreads; i++) {
				int firstMatrix = i * blockSize + firstMatrixInChunk; // shifted over firstMatrixInChunk to account for matrix already done
				int lastMatrix = firstMatrix + blockSize - 1; // shifted over 1 to account for matrix already done
				if (lastMatrix > numMatricesTotal - 1)
					lastMatrix = numMatricesTotal - 1;
				threads[i] = new AlterThread(this, datas, firstMatrix, lastMatrix, test, longWait);
			}
			if (thisIsFirstChunk){
				progIndicator.setText("Starting threads");
				logln("About to start " + numThreads + " threads to alter the matrices");
			}
			else {
				progIndicator.setText("Restarting threads after first chunk of " + numMatricesInChunk + " matrices");
				logln("\nResetting to do next chunk of matrices");
			}
			for (int i = 0; i < numThreads; i++)
				threads[i].start();
			thisIsFirstChunk = false;
			boolean allDone = false;

			// checking on all of the threads
			while (!allDone && !aborted) {
				try {
					Thread.sleep(SLEEPTIME);
					allDone = true;
					boolean waiting = false;
					for (int i = 0; i < numThreads; i++) {
						if (threads[i] != null && !threads[i].done)
							allDone = false;
						waiting = waiting || threads[i].longWait();
					}

					if (waiting && System.currentTimeMillis() - lastReportTime > REPORTDELAY) {
						String report = "\n... still waiting on threads (matrix number)";
						for (int i = 0; i < numThreads; i++) {
							if (threads[i].longWait()) {
								report += " " + (i + 1) + " (im: " + (threads[i].im + 1);
								String sta = threads[i].alterTask.reportStatus();
								if (sta != null)
									report += " status: " + sta;
								report += ")";
							}
						}
						int numDone = matricesDone.numBitsOn();
						double parallelTimePerMatrix = (1.0 * System.currentTimeMillis() - startParallel) / numDone;
						long timeAtCompletion = System.currentTimeMillis() + (long) (parallelTimePerMatrix * (numMatricesTotal - numDone));
						report += ".  Matrices completed: " + numDone + " of " + numMatricesTotal + ". Expected completion of all matrices: " + StringUtil.getDateTime(new Date(timeAtCompletion));
						logln(report);
						lastReportTime = System.currentTimeMillis();
					}

					progIndicator.setText("Number of matrices altered " + matricesDone.numBitsOn());
					progIndicator.setCurrentValue(matricesDone.numBitsOn());
					if (progIndicator.isAborted())
						aborted = true;
					int numDone = matricesDone.numBitsOn();

					if (numDone - sectionStart >=100){  //Recording section timing;
						MesquiteLong sectTime = new MesquiteLong("to " + numDone, System.currentTimeMillis()-startTimeThisSection);
						sectionStart = numDone;
						startTimeThisSection = System.currentTimeMillis();
						sectionTimings.addElement(sectTime, false);
					}

					if (pause > 0 && numDone - millennium > 1000){
						millennium += 1000;
						System.gc();
						logln("\n\nTaking a deep breath before continuing...\n");
						try {
							Thread.sleep(pause); //take a breather before next chunk! (maybe help with garbage collection?)
						}
						catch (Exception e){
						}
					}

					if ((numMatricesTotal <= 1000 && numDone % 10 == 0) || (numMatricesTotal > 1000 && numDone % (numMatricesTotal / 100) == 0)) { // do every 1% of matrices
						CommandRecord.tick("Finished altering " + numDone + " of " + numMatricesTotal + " matrices.");
						if (numDone > reportAtMatrix && (System.currentTimeMillis() - startParallel > 100000)) { // run has been longer than 100 seconds; worth reporting every so often what timing will be
							reportAtMatrix = numDone;
							double parallelTimePerMatrix = (1.0 * System.currentTimeMillis() - startParallel) / numDone;
							long timeAtCompletion = System.currentTimeMillis() + (long) (parallelTimePerMatrix * (numMatricesTotal - numDone));
							logln("\nFinished altering " + numDone + " of " + numMatricesTotal + " matrices. Expected completion of all matrices: " + StringUtil.getDateTime(new Date(timeAtCompletion)));
						}
					}

				} catch (Exception e) {
				}

			}

			for (int i = 0; i < numThreads; i++) {
				threads[i].shutDown();
				//threads[i].fileCoordinator.fireEmployee(threads[i].alterTask);
				//fireEmployee(threads[i].fileCoordinator);
			}

			if (chunked){
				try {
					Thread.sleep(1000); //take a breather before next chunk! (maybe help with garbage collection?)
				}
				catch (Exception e){
				}
			}
		}
		if (MesquiteTrunk.developmentMode){
			logln("");
			logln(">>>>>AFTER>> Element report " + getProject().elementsReport());
			logln(">>>>>CHANGE>>  Listened.notificationsMadeTotal " + -(ListenednotificationsMadeTotal - Listened.notificationsMadeTotal) );
			logln(">>>>>CHANGE>>  MQPanel.repaintRequestsTotal " + -(MQPanelrepaintRequestsTotal-MQPanel.repaintRequestsTotal));
			logln(">>>>>CHANGE>>  ProjPanelPanel.refreshRequestsTotal " + -(ProjPanelPanelrefreshRequestsTotal-ProjPanelPanel.refreshRequestsTotal));
			logln(">>>>>CHANGE>>  MenuOwner.allMenuBarResetsTotal " + -(MenuOwnerallMenuBarResetsTotal-MenuOwner.allMenuBarResetsTotal ));
			logln(">>>>>CHANGE>>  MesquiteFile.filesOpenTotal " + -(MesquiteFilefilesOpenTotal-MesquiteFile.filesOpenTotal));
			logln(">>>>>CHANGE>>  MesquiteProject.totalCreated " + -(MesquiteProjecttotalCreated-MesquiteProject.totalCreated ));
			logln(">>>>>CHANGE>>  MesquiteProject.totalDisposed " + -(MesquiteProjecttotalDisposed-MesquiteProject.totalDisposed));
			logln(">>>>>CHANGE>>  NexusBlock.totalCreated " + -(NexusBlocktotalCreated-NexusBlock.totalCreated));
			logln(">>>>>CHANGE>>  NexusBlock.totalDisposed " + -(NexusBlocktotalDisposed-NexusBlock.totalDisposed));
			logln(">>>>>CHANGE>>  FileElement.totalTrueFileElementCreated " + -(FileElementtotalTrueFileElementCreated-FileElement.totalTrueFileElementCreated));
			logln(">>>>>CHANGE>>  FileElement.totalDisposed " + -(FileElementtotalDisposed-FileElement.totalDisposed));
			logln("");
		}
		DrawHierarchy.suppressNodeRepaints = false;

		if (!notifyAsYouGo){ //threads weren't notifying on completion, so do it all now
			Notification notification = new Notification(MesquiteListener.DATA_CHANGED, null, null);
			for (int im = 0; im<datas.size(); im++){
				CharacterData data = (CharacterData) datas.elementAt(im);
				data.setNotificationsOnOff(true);
				if (matricesDone.isBitOn(im))
					data.notifyListeners(this, notification);
			}
		}
		progIndicator.goAway();
		long finishTime = System.currentTimeMillis();
		logln("Altered: " + (matricesDone.numBitsOn()) + " matrices. (Finished " + StringUtil.getDateTime(new Date(finishTime)) + ", after " + ((finishTime- startTime)/1000.0) + " seconds)");
		if (matricesDone.numBitsOn()>100 && ((System.currentTimeMillis()- startTime)/1000.0)>20){
			logln("Timings, milliseconds: ");
			for (int k = 0; k<sectionTimings.size(); k++){
				MesquiteLong sT = (MesquiteLong)sectionTimings.elementAt(k);
				logln("\t" + (sT.getValue()/1000.0) + "\t" + sT.getName());
			}
			logln("\t" +  ((finishTime- startTime)/1000.0) + "\tTOTAL seconds");
		}
		unpauseAllPausables(v);
		if (project != null) {
			project.zeroProjectWindowSuppression();
			project.getCoordinatorModule().setWhomToAskIfOKToInteractWithUser(null);
		}
		zeroMenuResetSuppression(); // set menu and project suppression to zero, just in case of threading issues?
		resetAllMenuBars();
		if (System.currentTimeMillis()- startTime>100000)
			MesquiteMessage.beep();
		return true;
	}
	/* ................................................................................................................. */

	boolean aborted;
	boolean notifyAsYouGo = false;

	public boolean okToInteractWithUser(int howImportant, String messageToUser) {
		return firstTime;
	}

	/* ................................................................................................................. */
	DataAlterer cloneFirstAlterTask(AlterThread thread) {
		String snapshot = Snapshot.getSnapshotCommands(firstAlterTask, null, "");
		MesquiteInteger pos = new MesquiteInteger(0);
		CommandRecord previous = MesquiteThread.getCurrentCommandRecord();
		CommandRecord record = new CommandRecord(true);
		MesquiteThread.setCurrentCommandRecord(record);
		MesquiteModule.incrementMenuResetSuppression();
		thread.fileCoordinator = (FileCoordinator) hireNamedEmployee(FileCoordinator.class, "#BasicFileCoordinator");
		MesquiteFile home = thread.fileCoordinator.createBlankProject();
		if (home != null){
			MesquiteProject hProj = home.getProject();
			if (hProj!= null) {
				hProj.incrementProjectWindowSuppression();
				hProj.notifyFileElementsAdded = false;
			}
			thread.fileCoordinator.setProject(home.getProject());
		}
		DataAlterer alterer = (DataAlterer) thread.fileCoordinator.hireNamedEmployee(DataAlterer.class, "#" + getShortClassName(firstAlterTask.getClass()));
		if (alterer != null) {
			Puppeteer p = new Puppeteer(thread.fileCoordinator);
			Object obj = p.sendCommands(alterer, snapshot, pos, "", false, null, CommandChecker.defaultChecker);
		}
		MesquiteModule.decrementMenuResetSuppression();
		MesquiteThread.setCurrentCommandRecord(previous);
		return alterer;
	}

	/* ................................................................................................................. */
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice() {
		return true;
	}

	/* ................................................................................................................. */
	/**
	 * returns the version number at which this module was first released. If 0, then no version number is claimed. If a POSITIVE integer then the number refers to the Mesquite version. This should be used only by modules part of the core release of Mesquite. If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package
	 */
	public int getVersionOfFirstRelease() {
		return 400;
	}

	/* ................................................................................................................. */
	public boolean isPrerelease() {
		return false;
	}

}

class AlterThread extends MesquiteThread {
	ParallelAlterMatrixAsUtility ownerModule;
	ListableVector datas;
	int firstMatrix;
	int lastMatrix;
	boolean done = false;
	CompatibilityTest test;
	DataAlterer alterTask;
	FileCoordinator fileCoordinator;
	int imPreviousReportedAsSlow = 0;
	long lastTimeChanged = -1;
	long longWait = 10000;

	public AlterThread(ParallelAlterMatrixAsUtility ownerModule, ListableVector datas, int startWindow, int endWindow, CompatibilityTest test, long longWait) {
		this.datas = datas;
		this.ownerModule = ownerModule;
		this.firstMatrix = startWindow;
		this.lastMatrix = endWindow;
		this.test = test;
		alterTask = ownerModule.cloneFirstAlterTask(this);
		this.longWait = longWait;
	}
	public void shutDown(){
		fileCoordinator.fireEmployee(alterTask);
		ownerModule.fireEmployee(fileCoordinator);
	}
	String report() {
		if (done)
			return "done";
		return "On matrix " + im;
	}

	boolean longWait() {
		return !done && (System.currentTimeMillis() - lastTimeChanged > longWait); // Yes, it's a long wait
	}

	int im;

	public void run() {
		if (alterTask != null) {
			setThreadMaxLogLevel(0);
			for (im = firstMatrix; im <= lastMatrix && !ownerModule.aborted; im++) {
				lastTimeChanged = System.currentTimeMillis() / 1000 * 1000; // truncating it to the second
				CharacterData data = (CharacterData) datas.elementAt(im);
				if (test.isCompatible(data, ownerModule.getProject(), ownerModule)) {
					AlteredDataParameters alteredDataParameters = new AlteredDataParameters();
					MesquiteThread.setHintToSuppressProgressIndicatorCurrentThread(true);
					if (ownerModule.matricesDone.isBitOn(im))
						MesquiteMessage.printStackTrace("ERROR: Matrix appears to have already been done! " + im);
					int result = -10;
					try {
						result = alterTask.alterData(data, null, null, alteredDataParameters);
					} catch (Exception e) {
						ownerModule.logln("Exception in Parallel Alter Matrices -- " + e);
					}
					MesquiteThread.setHintToSuppressProgressIndicatorCurrentThread(false);
					if (result == ResultCodes.SUCCEEDED) {
						if (ownerModule.notifyAsYouGo){ 
							Notification notification = new Notification(MesquiteListener.DATA_CHANGED, alteredDataParameters.getParameters(), null);
							if (alteredDataParameters.getSubcodes() != null)
								notification.setSubcodes(alteredDataParameters.getSubcodes());
							data.notifyListeners(this, notification);
						}
						ownerModule.matricesDone.setBit(im);
					}
				}
			}
			releaseThreadMaxLogLevel();
		}
		done = true;
	}

}
