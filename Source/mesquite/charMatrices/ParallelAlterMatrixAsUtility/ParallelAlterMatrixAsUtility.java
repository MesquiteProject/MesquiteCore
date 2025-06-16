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

import mesquite.lists.lib.*;

import java.util.Date;
import java.util.Vector;

import mesquite.categ.lib.CategoricalState;
import mesquite.genomic.FlagBySpruceup.FlagBySpruceup;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.ProgressIndicator;

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

	public boolean loadModule() {
		return true; // too flaky
	}

	public String getExplanation() {
		return "Alters selected matrices in List of Character Matrices window, with the option of using parallel processing (multithreading) to speed the completion.";
	}

	DataAlterer firstAlterTask = null;

	/* ................................................................................................................. */
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		if (arguments != null) {
			firstAlterTask = (DataAlterer) hireNamedEmployee(DataAlterer.class, arguments);
			if (firstAlterTask == null)
				return sorry(getName() + " couldn't start because the requested data alterer wasn't successfully hired.");
		}
		else if (!MesquiteThread.isScripting()) {
			firstAlterTask = (DataAlterer) hireEmployee(DataAlterer.class, "Alterer of matrices");
			if (firstAlterTask == null)
				return sorry(getName() + " couldn't start because no matrix alterer module obtained.");
		}
		if (!queryOptions()) {
			fireEmployee(firstAlterTask);
			return false;
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
			DataAlterer temp = (DataAlterer) replaceEmployee(DataAlterer.class, arguments, "Data alterer", firstAlterTask);
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
		/*
		 * if (StringUtil.blank(help) && queryDialog.isInWizard()) help = "<h3>" + StringUtil.protectForXML(title) + "</h3>Please enter a whole number (integer).  <p>The initial value is " + value; queryDialog.appendToHelpString(help);
		 */
		IntegerField integerField = queryDialog.addIntegerField("Number of threads", numThreads, 20, 1, 255);
		queryDialog.addLargeOrSmallTextLabel("(Note: the first matrix will be processed alone, and then the others in parallel.)");

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
		CompatibilityTest test = firstAlterTask.getCompatibilityTest();
		if (getProject() != null) {
			getProject().getCoordinatorModule().setWhomToAskIfOKToInteractWithUser(this);
			getProject().incrementProjectWindowSuppression();
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
			if (test.isCompatible(data, getProject(), this)) {
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
			if (getProject() != null)
				getProject().decrementProjectWindowSuppression();
			decrementMenuResetSuppression(numThreads + 1);
			return false;
		}

		firstTime = false;

		long startParallel = System.currentTimeMillis();
		long longWait = (startParallel - startTime) * PATIENCE;
		ProgressIndicator progIndicator = new ProgressIndicator(getProject(), "Altering matrices", "", datas.size(), true);
		progIndicator.start();
		progIndicator.setText("Setting up " + numThreads + " threads");
		// making threads and getting them started
		int numMatrices = datas.size();
		AlterThread[] threads = new AlterThread[numThreads];
		int blockSize = (numMatrices - 1) / numThreads + 1; // first one already done
		if (blockSize == 0)
			blockSize = 1;
		for (int i = 0; i < numThreads; i++) {
			int firstMatrix = i * blockSize + 1; // shifted over 1 to account for matrix already done
			int lastMatrix = firstMatrix + blockSize - 1; // shifted over 1 to account for matrix already done
			if (lastMatrix > numMatrices - 1)
				lastMatrix = numMatrices - 1;
			threads[i] = new AlterThread(this, datas, firstMatrix, lastMatrix, test, longWait);
		}
		progIndicator.setText("Starting threads");
		logln("About to start " + numThreads + " threads to alter the matrices");
		for (int i = 0; i < numThreads; i++)
			threads[i].start();

		int reportAtMatrix = -1;
		aborted = false;
		// checking on all of the threads
		boolean allDone = false;
		long lastReportTime = System.currentTimeMillis();
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
					long timeAtCompletion = System.currentTimeMillis() + (long) (parallelTimePerMatrix * (numMatrices - numDone));
					report += ".  Matrices completed: " + numDone + " of " + numMatrices + ". Expected completion of all matrices: " + StringUtil.getDateTime(new Date(timeAtCompletion));
					logln(report);
					lastReportTime = System.currentTimeMillis();
				}

				progIndicator.setText("Number of matrices altered " + matricesDone.numBitsOn());
				progIndicator.setCurrentValue(matricesDone.numBitsOn());
				if (progIndicator.isAborted())
					aborted = true;
				int numDone = matricesDone.numBitsOn();
				if ((numMatrices <= 1000 && numDone % 10 == 0) || (numMatrices > 1000 && numDone % (numMatrices / 100) == 0)) { // do every 1% of matrices
					CommandRecord.tick("Finished altering " + numDone + " of " + numMatrices + " matrices.");
					if (numDone > reportAtMatrix && (System.currentTimeMillis() - startParallel > 100000)) { // run has been longer than 100 seconds; worth reporting every so often what timing will be
						reportAtMatrix = numDone;
						double parallelTimePerMatrix = (1.0 * System.currentTimeMillis() - startParallel) / numDone;
						long timeAtCompletion = System.currentTimeMillis() + (long) (parallelTimePerMatrix * (numMatrices - numDone));
						logln("\nFinished altering " + numDone + " of " + numMatrices + " matrices. Expected completion of all matrices: " + StringUtil.getDateTime(new Date(timeAtCompletion)));
					}
				}

			} catch (Exception e) {
			}

		}

		for (int i = 0; i < numThreads; i++) {
			threads[i].fileCoordinator.fireEmployee(threads[i].alterTask);
			fireEmployee(threads[i].fileCoordinator);
		}
		progIndicator.goAway();
		logln("Altered: " + (matricesDone.numBitsOn()) + " matrices. (Finished " + StringUtil.getDateTime(new Date(System.currentTimeMillis())) + ")");
		unpauseAllPausables(v);
		if (getProject() != null) {
			getProject().zeroProjectWindowSuppression();
			getProject().getCoordinatorModule().setWhomToAskIfOKToInteractWithUser(null);
		}
		zeroMenuResetSuppression(); // set menu and project suppression to zero, just in case of threading issues?
		// MainThread.zeroSuppressWaitWindow();
		resetAllMenuBars();

		return true;
	}
	/* ................................................................................................................. */

	boolean aborted;

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
		if (home != null)
			thread.fileCoordinator.setProject(home.getProject());
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
		return NEXTRELEASE;
	}

	/* ................................................................................................................. */
	public boolean isPrerelease() {
		return true;
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
			for (im = firstMatrix; im <= lastMatrix && !ownerModule.aborted; im++) {
				lastTimeChanged = System.currentTimeMillis() / 1000 * 1000; // truncating it to the second
				CharacterData data = (CharacterData) datas.elementAt(im);
				if (test.isCompatible(data, ownerModule.getProject(), ownerModule)) {
					AlteredDataParameters alteredDataParameters = new AlteredDataParameters();
					MesquiteThread.setHintToSuppressProgressIndicatorCurrentThread(true);
					if (ownerModule.matricesDone.isBitOn(im))
						MesquiteMessage.printStackTrace("ERROR: doing matrix " + im);
					int result = -10;
					try {
						result = alterTask.alterData(data, null, null, alteredDataParameters);
					} catch (Exception e) {
						ownerModule.logln("Exception in Parallel Alter Matrices -- " + e);
					}
					MesquiteThread.setHintToSuppressProgressIndicatorCurrentThread(false);
					if (result == ResultCodes.SUCCEEDED) {
						Notification notification = new Notification(MesquiteListener.DATA_CHANGED, alteredDataParameters.getParameters(), null);
						if (alteredDataParameters.getSubcodes() != null)
							notification.setSubcodes(alteredDataParameters.getSubcodes());
						data.notifyListeners(this, notification);
						ownerModule.matricesDone.setBit(im);
					}
				}
			}
		}
		done = true;
	}

}
