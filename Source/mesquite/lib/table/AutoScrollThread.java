package mesquite.lib.table;

/* Mesquite source code.  Copyright 1997-2007 W. Maddison and D. Maddison. 
	Version 2.01, December 2007.
	Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
	The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
	Perhaps with your help we can be more than a few, and make Mesquite better.

	Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
	Mesquite's web site is http://mesquiteproject.org

	This source code and its compiled class files are free and modifiable under the terms of 
	GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

import java.awt.*;
import java.net.*;
import java.util.*;
import java.io.*;
import mesquite.lib.*;
import mesquite.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class AutoScrollThread extends Thread {
	MousePanel panel;
	MesquiteTable table;
	boolean abort = false;
	
	public AutoScrollThread (MesquiteTable table, MousePanel panel) {
		super();
		setPriority(Thread.MIN_PRIORITY);
		this.panel = panel;
		this.table = table;
	}
	public void abortThread() {
		abort = true;
		interrupt();
	}
	public void start() {
		abort = false;
		super.start();
	}
	public void run() {
		while (!abort) {
			try {
				Thread.sleep(200);
				table.checkForAutoScroll(panel, panel.getMouseX(), panel.getMouseY());
			}
			catch (InterruptedException e){
				Thread.currentThread().interrupt();
			}
		}
	}


}
