/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib.parallel;

import mesquite.lib.CommandChecker;
import mesquite.lib.CommandRecord;
import mesquite.lib.Debugg;
import mesquite.lib.IntegerArray;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteThread;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.Puppeteer;
import mesquite.lib.ResultCodes;
import mesquite.lib.Snapshot;
import mesquite.lib.ui.ProgressIndicator;

/* ======================================================================== */
/** */
public class Parallelizer {
	Parallelizable owner;
	int nThreads;
	PThread[] threads;
	IntegerArray calcStatus;
	boolean verbose = false;
	int totalCalculated = 0;
	boolean stopWithFirstItemFailure = false;
	ProgressIndicator progressIndicator;
	ParallelParams ppFirst = null;
	ParallelParams[] threadParams;
	long pingbackInterval = -1;

	public Parallelizer(Parallelizable owner, int nThreads){
		this.owner = owner;
		setNumThreads(nThreads);
	}


	public void shutDown(){
		if (threads == null)
			return;
		for (int i = 0; i < nThreads  && i<threads.length; i++) {
			threads[i].shutDown();
		}
	}
	public boolean completed (){
		for (int i = 0; i < nThreads; i++) {
			if (!threads[i].done)
				return false;
		}
		return true;
	}
	public boolean stopped (){
		for (int i = 0; i < nThreads; i++) {
			if (!threads[i].stopped)
				return false;
		}
		return true;
	}
	public void setPingbackInterval(long intervalInMillis){
		pingbackInterval = intervalInMillis;
	}
	public void setStopWithFirstItemFailure(boolean stop){
		stopWithFirstItemFailure = stop;
	}
	public void setProgressIndicator(ProgressIndicator progressIndicator){
		this.progressIndicator = progressIndicator;
	}

	public void setNumThreads(int n){
		nThreads = n;
		threadParams = new ParallelParams[nThreads];
	}
	/* ===================================================== */

	public final static int FAILURE = -1;
	public final static int UNCALCULATED = 0;
	public final static int BEINGCALCULATED = 1;
	public final static int SUCCESS = 2;
	public final static int INAPPLICABLE = 3;

	public void setItemStatus(int item, int status){
		if (calcStatus == null)
			return;
		calcStatus.setValue(item, status);
	}

	/* ===================================================== */
	public String summarizeCalcStatus(){
		int numUNCALCULATED = 0;
		int numBEINGCALCULATED = 0;
		int numSUCCESS = 0;
		int numINAPPLICABLE = 0;
		int numOTHER = 0;
		for (int i = 0; i<calcStatus.getSize(); i++){
			if (calcStatus.getValue(i) == UNCALCULATED)
				numUNCALCULATED++;
			else if (calcStatus.getValue(i) == BEINGCALCULATED)
				numBEINGCALCULATED++;
			else if (calcStatus.getValue(i) == SUCCESS)
				numSUCCESS++;
			else if (calcStatus.getValue(i) == INAPPLICABLE)
				numINAPPLICABLE++;
			else {
				numOTHER++;
				System.err.println("@ other " + calcStatus.getValue(i) + " for " + i);
			}
		}
		String s = "#";
		s += " UNCALCULATED " + numUNCALCULATED;
		s += " BEINGCALCULATED " + numBEINGCALCULATED;
		s += " SUCCESS " + numSUCCESS;
		s += " INAPPLICABLE " + numINAPPLICABLE;
		s += " other " + numOTHER;
		return s;
	}
	/* ===================================================== */
	public boolean itemUncalculated(int item){
		if (calcStatus == null)
			return true;
		return calcStatus.getValue(item)==UNCALCULATED;
	}
	/* ===================================================== */
	public boolean itemBeingCalculated(int item){
		if (calcStatus == null)
			return false;
		return calcStatus.getValue(item) == BEINGCALCULATED;
		/*
		if (threads == null)
			return false;
		for (int i = 0; i < nThreads; i++) {
			if (threads[i].running){
				if (threads[i].itemBeingCalculated == item)
					return true;
			}
		}
		if (calcStatus.getValue(item) == BEINGCALCULATED){
			MesquiteMessage.sys_err_println(BEINGCALCULATED but no thread claims " + item);
			return true;
		}
		return false; */
	}

	/* ===================================================== */
	public int getTotalCalculated(){
		if (calcStatus == null)
			return 0;
		int count = 0;
		int wasTotalCalculated = totalCalculated;
		for (int i= 0; i<calcStatus.getSize(); i++){
			if (calcStatus.getValue(i) != INAPPLICABLE && (calcStatus.getValue(i)<0 || calcStatus.getValue(i)>1))
				count++;
		}
		if (count != wasTotalCalculated && MesquiteTrunk.developmentMode)
			MesquiteMessage.sys_err_println("Difference between count " + count + " and totalCalculated " + wasTotalCalculated +" in Parallelizer");
		return totalCalculated;
	}

	/* ===================================================== */
	//This is an option service for cloning employees for each thread. See ParallelAlterDataMatrices for example with need to make ghost project
	public MesquiteModule cloneEmployee(MesquiteModule employer, MesquiteModule employee, Class hiredAs) {
		String snapshot = Snapshot.getSnapshotCommands(employee, null, "");
		MesquiteInteger pos = new MesquiteInteger(0);
		CommandRecord previous = MesquiteThread.getCurrentCommandRecord();
		CommandRecord record = new CommandRecord(true);
		MesquiteThread.setCurrentCommandRecord(record);
		MesquiteModule.incrementMenuResetSuppression();

		MesquiteModule clone = (MesquiteModule) employer.hireNamedEmployee(hiredAs, "#" + MesquiteModule.getShortClassName(employee.getClass()));
		if (clone != null) {
			clone.noUIForEmployeeBranch();
			clone.setAutoSnapshotAsEmployee(false);
			Puppeteer p = new Puppeteer(clone);
			Object obj = p.sendCommands(clone, snapshot, pos, "", false, null, CommandChecker.defaultChecker);
		}

		MesquiteModule.decrementMenuResetSuppression();
		MesquiteThread.setCurrentCommandRecord(previous);
		return clone;
	}
	/* ===================================================== */
	public synchronized int go(){ //this should be called on thread of owner, which should hold until done
		// ################## INITIALIZE ##################
		System.out.println("Parallelizer: calculations initializing");
		int count = owner.getTotalPossibleParallelItemCount();

		if (calcStatus == null) 
			calcStatus = new IntegerArray(count);
		else 
			calcStatus.resetSize(count);
		calcStatus.zeroArray();
		totalCalculated = 0;

		// ################## Calculation for first item ##################
		System.out.println("Parallelizer: first calculation");
		owner.markInappropriateItems(this);		
		//first step, do one calculation, and use its snapshot to build others
		int firstItem = owner.getNextParallelItemAndReserve(null, this);
		MesquiteInteger firstResult = new MesquiteInteger();
		setItemStatus(firstItem, BEINGCALCULATED);  //should be redundant, given the AndReserve
		ppFirst = owner.doFirstCalculation_Parallel(firstItem, this, firstResult); 
		if (firstResult.getValue() != ResultCodes.NO_ERROR) {
			Debugg.errln("Error in first result " + firstResult.getValue());
			setItemStatus(firstItem, FAILURE);
			if (stopWithFirstItemFailure  || firstResult.getValue() == ResultCodes.USERCANCELONINITIALIZE){
				return firstResult.getValue();
			}
		}
		else
			setItemStatus(firstItem, SUCCESS);

		totalCalculated++;

		//Next, build others threads
		if (threads == null || threads.length != nThreads || !owner.pleaseReuseParallelThreads()){
			// ################## Building threads ##################
			System.out.println("Parallelizer: building " + nThreads + " threads");
			threads = new PThread[nThreads];
			for (int i = 0; i < nThreads; i++) {
				ParallelParams ppT = owner.cloneForParallel(ppFirst, this);
				threads[i] = new PThread(ppT, i, this);
			}
		}
		
		// ################## Running threads ##################

		System.out.println("Parallelizer: starting threads");
		for (int i = 0; i < nThreads; i++) {
			threads[i].done = false;
			threads[i].running = true;  // this is how they get re-going if second time around
			threadParams[i] = threads[i].pp;
			if (!threads[i].started)
				threads[i].start();
		}
		long lastPing = System.currentTimeMillis();
		while (!completed() && !stopped()){
			try {
				Thread.sleep(10);
				long now = System.currentTimeMillis();
				if (pingbackInterval>0 && now-lastPing>pingbackInterval){
					lastPing = now;
					owner.ping(this);
				}
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
		boolean completedYay = completed();
		if (completedYay)
			System.out.println("Parallelizer: finished calculations");
		else 
			System.out.println("Parallelizer: calculations stopped");

		System.out.print("Parallelizer: Calculations finished in each thread: ");
		for (int i = 0; i < nThreads; i++) {
			
			threads[i].running = false;
			System.out.print(" " + threads[i].calculatedOnThread);
		}
		System.out.println("");
		
		System.out.println("Parallelizer: " + summarizeCalcStatus());
		if (completedYay)
			return ResultCodes.NO_ERROR;
		return ResultCodes.USER_STOPPED;
	}
	
	public ParallelParams getFirstParams(){
		return ppFirst;
	}
	
	public ParallelParams[] getThreadParams(){
		return threadParams;
	}
	public void reset(){ // to be called on owner's thread
		if (threads == null)
			return;
		for (int i = 0; i < nThreads; i++) {
			if (threads[i] !=null)
				threads[i].fireParallelEmployees();
		}
		threads = null;
	}
	/* ===================================================== */

	/* ############################# */
	class PThread extends MesquiteThread {
		ParallelParams pp;
		int whichThread;
		boolean done = false;
		boolean started = false;
		boolean stopped = false;
		boolean onCall = true;
		boolean running = false;
	
		int itemBeingCalculated = -1;
		Parallelizer parallelizer;
		int calculatedOnThread = 0;

		public PThread(ParallelParams pp, int whichThread, Parallelizer parallelizer){
			this.pp = pp;
			this.whichThread = whichThread;
			this.parallelizer = parallelizer;
		}
		public void start(){
			started = true;
			onCall = true;
			calculatedOnThread = 0;
			super.start();
		}

		public void doJob () {
			done = false;
			int item = -1;
			calculatedOnThread = 0;
			while (!stopped && (item = owner.getNextParallelItemAndReserve(pp, parallelizer))>=0){
				itemBeingCalculated = item;
				setItemStatus(item, BEINGCALCULATED);
				int result = owner.doItemCalculation_Parallel(item, pp, parallelizer);
				totalCalculated++;
				calculatedOnThread++;
				
				if (result == ResultCodes.NO_ERROR)
					setItemStatus(item, SUCCESS);
				else
					setItemStatus(item, FAILURE);
				itemBeingCalculated = -1;
				if (progressIndicator != null && progressIndicator.isAborted())
					stopped = true;
			}
			owner.threadCompletedAssignedTasks(pp, parallelizer);
			done = true;
		}

		public void run(){
			while (onCall && !stopped){
				try {
					Thread.sleep(10); 
					if (running){
						doJob();
						if (progressIndicator != null && progressIndicator.isAborted())
							stopped = true;
						running = false;
					}
				}
				catch (Exception e){
					e.printStackTrace();
				}
			}		
		}

		public void fireParallelEmployees(){ // to be called on owner's thread
			if (pp == null)
				return;
			MesquiteModule employer = pp.responsibleEmployer;
			for (int i = 0; i<pp.employees.length; i++)
				employer.fireEmployee(pp.employees[i]);
		}
		public void shutDown(){ // to be called on owner's thread
			onCall = false;
			while (running){
				try {
					Thread.sleep(10); 
				}
				catch (Exception e){
					e.printStackTrace();
				}
			}		
			fireParallelEmployees();
		}
		public String toString(){
			String details = "";
			if (parallelizer!= null){
				details = "owner: " + owner.getClass().getName();
			}
			return "ParallelizerThread " + details + " " + super.toString();
		}

	}
}
