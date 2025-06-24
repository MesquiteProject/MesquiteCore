package mesquite.lib.table;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.ui.MousePanel;

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
