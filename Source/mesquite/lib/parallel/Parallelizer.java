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
import mesquite.lib.IntegerArray;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Puppeteer;
import mesquite.lib.Snapshot;

/* ======================================================================== */
/** */
public class Parallelizer {
	Parallelizable owner;
	int nThreads;
	PThread[] threads;
	IntegerArray calcStatus;
	boolean verbose = false;

	public Parallelizer(Parallelizable owner, int nThreads){
		this.owner = owner;
		this.nThreads = nThreads;
	}
	public synchronized void go(){ //this should be called on thread of owner, which should hold until done
		if (verbose) System.err.println("Parallelizer: INITIALIZE ##################");
		//Initialize
		int count = owner.getTotalPossibleParallelItemCount();
		if (calcStatus == null) 
			calcStatus = new IntegerArray(count);
		else 
			calcStatus.resetSize(count);
		calcStatus.zeroArray();

		if (verbose) System.err.println("Parallelizer: Calculation for first item ##################");
		//first step, do one calculation, and use its snapshot to build others
		int firstItem = owner.getNextParallelItem();
		ParallelParams ppFirst = owner.doFirstParallelCalculation();

		//Next, build others threads
		if (threads == null || !owner.pleaseReuseParallelThreads()){
			if (verbose) System.err.println("Parallelizer: Building threads ##################");
			threads = new PThread[nThreads];
			for (int i = 0; i < nThreads; i++) {
				ParallelParams ppT = owner.cloneForParallel(ppFirst);
				threads[i] = new PThread(ppT, i);
			}
		}

		if (verbose) System.err.println("Parallelizer: Running threads ##################");
		try {
			for (int i = 0; i < nThreads; i++) {
				threads[i].running = true;
				if (!threads[i].started)
					threads[i].start();
			}
		}
		catch (Exception e){
			return;
		}

		while (!completed()){
			try {
				Thread.sleep(10);
			}
			catch (Exception e){
			}
		}
	}

	public boolean itemBeingCalculated(int item){
		if (threads == null)
			return false;
		for (int i = 0; i < nThreads; i++) {
			if (threads[i].running){
				if (threads[i].itemBeingCalculated == item)
					return true;
			}
		}
		return false;
	}
	public void shutDown(){
		if (threads == null)
			return;
		for (int i = 0; i < nThreads; i++) {
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
	public boolean itemUncalculated(int item){
		return calcStatus.getValue(item)==0;
	}
	public void setItemStatus(int item, int status){
		calcStatus.setValue(item, status);
	}

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
			Puppeteer p = new Puppeteer(clone);
			Object obj = p.sendCommands(clone, snapshot, pos, "", false, null, CommandChecker.defaultChecker);
		}
		
		MesquiteModule.decrementMenuResetSuppression();
		MesquiteThread.setCurrentCommandRecord(previous);
		return clone;
	}

	/* ############################# */
	class PThread extends MesquiteThread {
		ParallelParams pp;
		int whichThread;
		boolean done = false;
		boolean started = false;
		boolean onCall = true;
		boolean running = false;
		int itemBeingCalculated = -1;

		public PThread(ParallelParams pp, int whichThread){
			this.pp = pp;
			this.whichThread = whichThread;
		}
		public void start(){
			started = true;
			onCall = true;
			super.start();
		}

		public void doJob () {
			done = false;
			int item = -1;
			while ((item = owner.getNextParallelItem())>=0){
				setItemStatus(item, 1);
				itemBeingCalculated = item;
				int result = owner.doParallelCalculation(item, pp);
				itemBeingCalculated = -1;
				//System.err.println("### finished item " + item + " on thread " + whichThread);
				if (result > 0)
					setItemStatus(item, 2);
				else
					setItemStatus(item, 3);
			}
			done = true;
		}

		public void run(){
			while (onCall){
				try {
					Thread.sleep(10); 
					if (running){
						doJob();
						running = false;
					}
				}
				catch (Exception e){
				}
			}		
		}
		public void shutDown(){ // to be called on owner's thread
			onCall = false;
			while (running){
				try {
					Thread.sleep(10); 
				}
				catch (Exception e){
				}
			}		
			MesquiteModule employer = pp.responsibleEmployer;
			for (int i = 0; i<pp.employees.length; i++)
				employer.fireEmployee(pp.employees[i]);
		}

	}
}
