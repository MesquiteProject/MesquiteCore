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
import mesquite.lib.MesquiteTimer;
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
		//		queryDialog.addLargeOrSmallTextLabel("The calculations sometimes slow down after they have completed 1000 to 3000 matrices, for reasons mysterious to the Mesquite developers. "
		//				+"If you are processing many matrices, and it slows down too much, then you may need to alter the first 1000 matrices by selecting those rows, then save the file, then reopen it for the next 1000, and so on. Alternatively, you may need to run it overnight.");

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
	static long REPORTDELAY = 60000; // If there is a stalled calculation (determined by PATIENCE), how often to report (in milliseconds)


	int[] matricesDone; //0 not started; 1 in progress; 2 done successfully; 3 or 5 failed; 4 or 6 incompatible
	int numMatricesDone(){
		if (matricesDone == null)
			return 0;
		int count = 0;
		for (int i = 0; i<matricesDone.length; i++)
			if (matricesDone[i] >1)  //note: counts also those that failed
				count++;
		return count;
	}
	int numMatricesFailed(){
		if (matricesDone == null)
			return 0;
		int count = 0;
		for (int i = 0; i<matricesDone.length; i++)
			if (matricesDone[i] >2) 
				count++;
		return count;
	}
	void reportFailures(){
		if (matricesDone == null)
			return;
		int count = 0;
		String sF = "";
		String sI = "";
		for (int i = 0; i<matricesDone.length; i++){
			if (matricesDone[i] ==3){
				if (StringUtil.notEmpty(sF))
					sF += ", ";
				sF +=Integer.toString(i+1);
				matricesDone[i] = 5;
			}
			else if (matricesDone[i] ==4) {
				if (StringUtil.notEmpty(sI))
					sI += ", ";
				sI +=Integer.toString(i+1);
				matricesDone[i] = 6;
			}
		}
		if (StringUtil.notEmpty(sF) ||StringUtil.notEmpty(sI))
			logln("");
		if (StringUtil.notEmpty(sF))
			logln("Failed to alter these matrices: #" + sF + ".");
		if (StringUtil.notEmpty(sI))
			logln("Failed to alter these matrices: #" + sI + ", because they were incompatible with the method.");
	}
	int numMatricesSuccessful(){
		if (matricesDone == null)
			return 0;
		int count = 0;
		for (int i = 0; i<matricesDone.length; i++)
			if (matricesDone[i] == 2) 
				count++;
		return count;
	}

	AlterThread[] threads;
	/* ................................................................................................................. */
	/** Called to operate on the CharacterData blocks. Returns true if taxa altered */
	public boolean operateOnDatas(ListableVector datas, MesquiteTable table) {

		incrementMenuResetSuppression(numThreads + 1);
		DrawHierarchy.suppressNodeRepaints = true;
		CompatibilityTest test = firstAlterTask.getCompatibilityTest();
		Debugg.println("test " + test);
		MesquiteProject project = getProject();
		if (project != null) {
			project.getCoordinatorModule().setWhomToAskIfOKToInteractWithUser(this);
			project.incrementProjectWindowSuppression();
		}
		Vector v = pauseAllPausables();
		matricesDone = new int[datas.size()];
		// Do the first matrix separately to set up the parameters of the alteration
		//boolean doneFirstMatrix = false;
		int successFirstMatrix = -1;
		long startTime = System.currentTimeMillis();
		logln("Parallel Alter Matrices started at " + StringUtil.getDateTime(new Date(startTime)));
		//	for (int im = 0; im < datas.size() && !doneFirstMatrix; im++) {
		CharacterData data = (CharacterData) datas.elementAt(0);
		if (test.isCompatible(data, project, this)) {
			if (datas.size() > 1)
				logln("\nAltering first matrix \"" + data.getName() + "\"");
			AlteredDataParameters alteredDataParameters = new AlteredDataParameters();
			successFirstMatrix = firstAlterTask.alterData(data, null, null, alteredDataParameters);
			//doneFirstMatrix = true;
			if (successFirstMatrix == ResultCodes.SUCCEEDED) {
				matricesDone[0]=2;
				Notification notification = new Notification(MesquiteListener.DATA_CHANGED, alteredDataParameters.getParameters(), null);
				if (alteredDataParameters.getSubcodes() != null)
					notification.setSubcodes(alteredDataParameters.getSubcodes());
				data.notifyListeners(this, notification);
				if (datas.size() > 1)
					logln("First matrix altered. About to alter others in parallel on " + numThreads + " threads.");
			}
			else {
				MesquiteMessage.warnProgrammer("   Failed to alter matrix #1 " +data.getName() + " (code " + successFirstMatrix + ").");
				MesquiteMessage.warnProgrammer("\nBecause this occurred with the first matrix, which is critical for setting up the calculations, Parallel Alter Matrices will be stopped.\n");
			}
		}
		else {
			MesquiteMessage.warnProgrammer("\nFailed to alter matrix #1 " +data.getName() + " because it is incompatible with the method.");
			MesquiteMessage.warnProgrammer("\nBecause this occurred with the first matrix, which is critical for setting up the calculations, Parallel Alter Matrices will be stopped.\n");
		}
		//	}

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

		//Whether to notify within the threads. If not, a cruder notification is thereby given, but that's OK because this isn't like editing the matrices by hand, which needs fine scale notifications
		notifyAsYouGo = false;

		if (!notifyAsYouGo)
			for (int im = 0; im<datas.size(); im++){
				CharacterData ddata = (CharacterData) datas.elementAt(im);
				ddata.setNotificationsOnOff(false);
			}
		/* This had part of effort to diagnose slow down Sept 2025
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
		 */


		//Pause time every 1000 to garbage collect, in case this helps. A good pause might be 5000 (5 seconds);
		//long pause = 0;

		//Chunking: This was a system to divide the run into chunks of 500 or 1000 to see if that avoids the slowdown that starts at about 1000 matrices. 
		//Didn't seem to help much, so disabled by chunked == false, in which case just make the chunk size as big as the total number.
		//Cut out in future once this module stabilizes fully.
		/*boolean chunked = false;
		int numMatricesInChunk = 1500;
		if (!chunked ||numMatricesTotal<numMatricesInChunk*1.5) 
			numMatricesInChunk = numMatricesTotal;
		boolean thisIsFirstChunk = true;
		 */
		int numMatricesTotal = datas.size();

		aborted = false;
		int reportAtMatrix = -1;
		long lastReportTime = System.currentTimeMillis();

		//These were used to time sections to diagnose slowdown fixed in Sept 2025
		//long startTimeThisSection  =  System.currentTimeMillis();
		//int sectionStart = 1; //last section recorded in listableVector
		//int millennium = 0;


		//for (int firstMatrixInChunk = 1; firstMatrixInChunk<numMatricesTotal; firstMatrixInChunk += numMatricesInChunk){

		//=================================================
		// making threads and getting them started
		threads = new AlterThread[numThreads];
		int blockSize = (numMatricesTotal - 1) / numThreads + 1; // first one already done
		if (blockSize == 0)
			blockSize = 1;
		for (int i = 0; i < numThreads; i++) {
			int firstMatrix = i * blockSize + 1; // shifted over 1 to account for matrix already done
			int lastMatrix = firstMatrix + blockSize - 1; // shifted over 1 to account for matrix already done
			if (lastMatrix == numMatricesTotal -2)
				lastMatrix = numMatricesTotal -1;
			if (lastMatrix > numMatricesTotal - 1)
				lastMatrix = numMatricesTotal - 1;
			threads[i] = new AlterThread(this, datas, firstMatrix, lastMatrix, test, longWait, i);
			//logln("Thread #" + i + " will be assigned matrices # " + (firstMatrix+1) + " to " + (lastMatrix+1));
		}
		logln("");
		//if (thisIsFirstChunk){
		progIndicator.setText("Starting threads");
		logln("Starting " + numThreads + " threads to alter the matrices");
		/*	}
			else {
				progIndicator.setText("Restarting threads after first chunk of " + numMatricesInChunk + " matrices");
				logln("\nResetting to do next chunk of matrices");
			}
		 */
		//startTimeThisSection  =  System.currentTimeMillis();
		for (int i = 0; i < numThreads; i++)
			threads[i].start();
		//	thisIsFirstChunk = false;
		boolean allDone = false;
		int numSections = 8;
		MesquiteTimer[] sectionTimers = MesquiteTimer.makeTimers(numSections);
		int currentTimer = 0;
		int lastTimer = 0;
		
		int numDone = 0;
		// checking on all of the threads
		while (!allDone && !aborted) {
			try {
				sectionTimers[currentTimer].start();
				lastTimer = currentTimer;
				Thread.sleep(SLEEPTIME);
				allDone = true;
				boolean waiting = false;
				for (int i = 0; i < numThreads; i++) {
					if (threads[i] != null && !threads[i].done)
						allDone = false;
					waiting = waiting || threads[i].longWait();
				}

				sectionTimers[currentTimer].end();
				
				
				numDone = numMatricesDone();
				currentTimer = numDone*numSections/numMatricesTotal;

				if (System.currentTimeMillis() - lastReportTime > REPORTDELAY) {
					reportFailures();
					double parallelTimePerMatrix = (1.0 * System.currentTimeMillis() - startParallel) / numDone;
					long timeAtCompletion = System.currentTimeMillis() + (long) (parallelTimePerMatrix * (numMatricesTotal - numDone));
					String report = "\nMatrices completed: " + numDone + " of " + numMatricesTotal + ". Expected completion of all matrices: " + StringUtil.getDateTime(new Date(timeAtCompletion));
					lastReportTime = System.currentTimeMillis();
					if (waiting && MesquiteTrunk.developmentMode){
						report += "\n";
						for (int i = 0; i < numThreads; i++) {
							if (threads[i].longWait()) {
								CharacterData ddata = (CharacterData)datas.elementAt(threads[i].im);
								report += "...Still waiting on Thread #" + (i ) + " (matrix # " + (threads[i].im + 1) + ", name: " + ddata.getName() + ", with " + ddata.getNumChars() + " characters.";
								String sta = threads[i].alterTask.reportStatus();
								if (sta != null)
									report += " Status: " + sta;
								report += ")";
							}
						}
					}
					logln(report);
					logln("");
				}
				

				progIndicator.setText("Number of matrices completed " + numMatricesDone());
				progIndicator.setCurrentValue(numMatricesDone());
				if (progIndicator.isAborted())
					aborted = true;
				numDone = numMatricesDone();
				currentTimer = numDone*numSections/numMatricesTotal;

				
				if ((numMatricesTotal <= 1000 && numDone % 10 == 0) || (numMatricesTotal > 1000 && numDone % (numMatricesTotal / 100) == 0)) { // do every 1% of matrices
					CommandRecord.tick("Completed " + numDone + " of " + numMatricesTotal + " matrices.");
					if (numDone > reportAtMatrix && (System.currentTimeMillis() - startParallel > 100000)) { // run has been longer than 100 seconds; worth reporting every so often what timing will be
						reportAtMatrix = numDone;
						double parallelTimePerMatrix = (1.0 * System.currentTimeMillis() - startParallel) / numDone;
						long timeAtCompletion = System.currentTimeMillis() + (long) (parallelTimePerMatrix * (numMatricesTotal - numDone));
						logln("\nCompleted " + numDone + " of " + numMatricesTotal + " matrices. Expected completion of all matrices: " + StringUtil.getDateTime(new Date(timeAtCompletion)));
					}
				}

			} 
			
			catch (Exception e) {
				System.out.println("Exception in Parallel Alter Matrices");
				e.printStackTrace();
			}

		}
		logln("\nCompleted Parallel Alter Matrices.");
		//harvesting timings of matrices
		double[] sectionMatrixTimings = new double[numMatricesTotal/100+1];
		int[] sectionMatrixCount = new int[numMatricesTotal/100+1];
		boolean done = false;
		int countS = 0;
		while (!done){
			int earliestThread = -1;
			long earliestTime = -1;
			for (int i = 0; i<numThreads; i++){
				if (threads[i].matrixTimings.size()>0){
					long[] mt = (long[])threads[i].matrixTimings.elementAt(0);
					if (earliestTime <0 ||  mt[0]< earliestTime){
						earliestTime = mt[0];
						earliestThread = i;
					}
				}
			}
			if (earliestThread<0)
				done = true;
			else {
				long[] mt = (long[])threads[earliestThread].matrixTimings.elementAt(0);
				threads[earliestThread].matrixTimings.removeElementAt(0);
				sectionMatrixTimings[countS/100]+= mt[1];
				sectionMatrixCount[countS/100]++;
				countS++;
			}
		}

		for (int i = 0; i < numThreads; i++) {
			threads[i].shutDown();
			//threads[i].fileCoordinator.fireEmployee(threads[i].alterTask);
			//fireEmployee(threads[i].fileCoordinator);
		}
		reportFailures();

		/*if (chunked){
				try {
					Thread.sleep(1000); //take a breather before next chunk! (maybe help with garbage collection?)
				}
				catch (Exception e){
				}
			}
		 */
		//	} //@@@

		/* This was to diagnose slowdown Sept 2025
		if (false && MesquiteTrunk.developmentMode){
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
		 */
		DrawHierarchy.suppressNodeRepaints = false;
		/*To do: 
		 * -- Report on matrices that didnt' succeed
		 * -- Have stop that nixes the threads
		 */
		if (!notifyAsYouGo){ //threads weren't notifying on completion, so do it all now
			Notification notification = new Notification(MesquiteListener.DATA_CHANGED, null, null);
			for (int im = 0; im<datas.size(); im++){
				CharacterData ddata = (CharacterData) datas.elementAt(im);
				ddata.setNotificationsOnOff(true);
				if (matricesDone[im] == 2)
					ddata.notifyListeners(this, notification);
			}
		}
		progIndicator.goAway();
		long finishTime = System.currentTimeMillis();
		String failed = "";
		int numFailed = numMatricesFailed();
		if (numFailed>0)
			failed = " Failed or incompatible: " + numFailed + " matrices.";
		String sectionSummary = MesquiteTimer.summarize(sectionTimers);
		logln("Altered: " + (numMatricesSuccessful()) + " matrices." + failed + " (Finished " + StringUtil.getDateTime(new Date(finishTime)) + ", after " + ((finishTime- startTime)/1000.0) + " seconds. Sections: " + sectionSummary + ")");
		
		if (MesquiteTrunk.developmentMode){
			String mTimings = "Average times of blocks of 100 matrices in sequence of starting: ";
			for (int i= 0; i< sectionMatrixTimings.length; i++){
				if (sectionMatrixCount[i]>0)
				mTimings += "   " + (1.0*sectionMatrixTimings[i]/sectionMatrixCount[i]);
			}
			logln(mTimings);
	}
		
		/*
			if (numMatricesDone()>100 && ((System.currentTimeMillis()- startTime)/1000.0)>20){
				logln("Timings, milliseconds: ");
				for (int k = 0; k<sectionTimings.size(); k++){
					MesquiteLong sT = (MesquiteLong)sectionTimings.elementAt(k);
					logln("\t" + (sT.getValue()/1000.0) + "\t" + sT.getName());
				}
				logln("\t" +  ((finishTime- startTime)/1000.0) + "\tTOTAL seconds");
			}
		 */
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
	int giveMeAMatrixToDo(int whichThreadAmI){
		if (threads == null)
			return -1;
		int maxRemaining = 0;
		int whichIsSlowest = -1;
		for (int i = 0; i<threads.length; i++){
			int nR = threads[i].getNumRemaining();
			if (nR>maxRemaining){
				maxRemaining = nR;
				whichIsSlowest = i;
			}
		}
		if (maxRemaining == 0 || whichIsSlowest<0)
			return -1;
		for (int k = threads[whichIsSlowest].lastMatrix; k>= threads[whichIsSlowest].firstMatrix; k--){
			if (matricesDone[k] == 0)
				return k;
		}
		return -1;
	}
	/* ................................................................................................................. */

	boolean aborted;
	boolean notifyAsYouGo = false;

	public boolean okToInteractWithUser(int howImportant, String messageToUser) {
		return firstTime;
	}
	/* ................................................................................................................. */
	/**Override to limit employee core use */
	public int getMaxCoresForEmployee() {
		return 1;
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
				hProj.setNotificationsOnOff(false);
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

/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
/* add vector of timings. Time each alteration, and make long[2] with timestamp of start and timestamp of duration, and add to vector
 * Later, after all done, harvest timings in order (removing element each time) from all threads, and build average per 100 */

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
	int whichThreadAmI = 0;
	int numAssignedFinished = 0;
	Vector matrixTimings;

	public AlterThread(ParallelAlterMatrixAsUtility ownerModule, ListableVector datas, int startWindow, int endWindow, CompatibilityTest test, long longWait, int whichThreadAmI) {
		this.datas = datas;
		this.ownerModule = ownerModule;
		this.firstMatrix = startWindow;
		this.lastMatrix = endWindow;
		this.test = test;
		alterTask = ownerModule.cloneFirstAlterTask(this);
		this.longWait = longWait;
		this.whichThreadAmI =  whichThreadAmI;
		matrixTimings = new Vector();
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
	int getNumRemaining(){
		return lastMatrix - firstMatrix + 1 -numAssignedFinished;
	}
	public String getThreadName(){
		return "Thread #" + whichThreadAmI + " for Parallel Alter Matrices, assigned matrices #s " + (firstMatrix+1) + " to " + (lastMatrix+1);
	}
	boolean longWait() {
		return !done && (System.currentTimeMillis() - lastTimeChanged > longWait); // Yes, it's a long wait
	}

	int im;

	void alterData(int im){
		CharacterData data = (CharacterData) datas.elementAt(im);
		if (test == null || test.isCompatible(data, ownerModule.getProject(), ownerModule)) {
			AlteredDataParameters alteredDataParameters = new AlteredDataParameters();
			if (ownerModule.matricesDone[im] ==0) /*
				MesquiteMessage.println("Matrix already done " + (im + 1));
			else if (ownerModule.matricesDone[im] !=1)
				MesquiteMessage.println("Matrix being worked on " + (im + 1));
			else */{
				MesquiteThread.setHintToSuppressProgressIndicatorCurrentThread(true);
				ownerModule.matricesDone[im] =1; //in progress
				int result = -10;
				try {
					long[] matrixTime = new long[2];
					matrixTime[0] = System.currentTimeMillis();
					result = alterTask.alterData(data, null, null, alteredDataParameters);
					ownerModule.log(".");

					matrixTime[1] = System.currentTimeMillis() - matrixTime[0];
					matrixTimings.addElement(matrixTime);
				} catch (Exception e) {
					ownerModule.logln("Exception in Parallel Alter Matrices -- " + e);
					e.printStackTrace();
				}
				MesquiteThread.setHintToSuppressProgressIndicatorCurrentThread(false);
				if (result == ResultCodes.SUCCEEDED) {
					if (ownerModule.notifyAsYouGo){ 
						Notification notification = new Notification(MesquiteListener.DATA_CHANGED, alteredDataParameters.getParameters(), null);
						if (alteredDataParameters.getSubcodes() != null)
							notification.setSubcodes(alteredDataParameters.getSubcodes());
						data.notifyListeners(this, notification);
					}
					ownerModule.matricesDone[im] =2; //done
				}
				else
					ownerModule.matricesDone[im] =3; //failed
			}
		} 
		else {
			ownerModule.matricesDone[im] =4; //incompatible
		}
	}
	public void run() {
		if (alterTask != null) {
			setThreadMaxLogLevel(MesquiteMessage.HIGH_PRIORITY);
			for (im = firstMatrix; im <= lastMatrix && !ownerModule.aborted; im++) {
				lastTimeChanged = System.currentTimeMillis() / 1000 * 1000; // truncating it to the second
				alterData(im);
				numAssignedFinished++;
			}
			//if (MesquiteTrunk.developmentMode)
			//	ownerModule.logln("~~~ " + getThreadName() + ", has completed its assigned tasks. ~~~");
			int imOthers = -1;
			while ((imOthers = ownerModule.giveMeAMatrixToDo(whichThreadAmI))>=0){
				//if (MesquiteTrunk.developmentMode)
				//	ownerModule.logln("~~> Thread #" + whichThreadAmI + " working to help with matrix " + (imOthers+1));
				lastTimeChanged = System.currentTimeMillis() / 1000 * 1000; // truncating it to the second
				alterData(imOthers);
			}
			releaseThreadMaxLogLevel();
		}
		done = true;
	}

}
