package mesquite.lib.table;

/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison. 
	Version 2.72, December 2009.
	Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
	The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
	Perhaps with your help we can be more than a few, and make Mesquite better.

	Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
	Mesquite's web site is http://mesquiteproject.org

	This source code and its compiled class files are free and modifiable under the terms of 
	GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.*;
import java.util.*;
import java.io.*;
import mesquite.lib.*;
import mesquite.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class AutoScrollThread extends Thread implements MouseListener {
	MousePanel panel;
	MesquiteTable table;
	boolean abort = false;
	boolean suppressed = false;
	
	public AutoScrollThread (MesquiteTable table, MousePanel panel) {
		super();
		setPriority(Thread.MIN_PRIORITY);
		this.panel = panel;
		this.table = table;
		panel.addMouseListener(this);

	}
	public void abortThread() {
		abort = true;
		interrupt();
	}
	
	/*.................................................................................................................*/
	public boolean canAutoscrollHorizontally() {
		return panel.canAutoscrollHorizontally();
	}
	/*.................................................................................................................*/
	public boolean canAutoscrollVertically() {
		return panel.canAutoscrollVertically();
	}

	public boolean active() {
		return !abort && !suppressed;
	}
	public void start() {
		abort = false;
		super.start();
	}
	public void run() {
		while (!abort && !suppressed && !MesquiteTrunk.mesquiteTrunk.mesquiteExiting) {
			try {
				Thread.sleep(200);
				if (MesquiteInteger.isCombinable(panel.getMouseX()) && MesquiteInteger.isCombinable(panel.getMouseY()) )
					table.checkForAutoScroll(panel, panel.getMouseX(), panel.getMouseY());
			}
			catch (InterruptedException e){
				Thread.currentThread().interrupt();
			}
		}
	}
	public void mouseClicked(MouseEvent arg0) {
	}
	public void mouseEntered(MouseEvent arg0) {
	}
	public void mouseExited(MouseEvent arg0) {
	}
	public void mousePressed(MouseEvent arg0) {
		
	}
	public void mouseReleased(MouseEvent arg0) {
		abortThread();
	}
	public boolean isSuppressed() {
		return suppressed;
	}
	public void setSuppressed(boolean suppressed) {
		this.suppressed = suppressed;
	}


}
