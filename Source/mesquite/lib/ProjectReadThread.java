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
public class ProjectReadThread extends MesquiteThread {
	public static ProjectReadThread ongoingThread = null;
	ProjectReadThread subsequent;
	String tempID;
	public void settempID(String id){
		tempID = id;
	}
	public ProjectReadThread () {
		super();
		sayGoodbye = false;
	}
	public ProjectReadThread (Runnable r) {
		super(r);
		sayGoodbye = false;
	}
	public synchronized void addSubsequentThread(ProjectReadThread thread){ //chaining threads
		if (subsequent != null){
			subsequent.addSubsequentThread(thread);
		}
		else
			subsequent = thread;
	}
	public void start(){
		try {
			ongoingThread.addSubsequentThread(this);
		}
		catch (Exception e)
		{
			ongoingThread = this;
			super.start();
		}
	}
	public void run(){
		try {
			setIsReading(true);
			super.run();
			ongoingThread = null;
			if (subsequent != null) 
				
				subsequent.start();

			setIsReading(false);
			MesquiteTrunk.resetAllMenuBars();
			threadGoodbye();
		}
		catch (MesquiteException e){
			MesquiteMessage.warnProgrammer("MesquiteException thrown");
		}
	}

}

