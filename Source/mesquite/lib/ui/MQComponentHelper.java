/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.TextComponent;

import javax.swing.text.JTextComponent;

import mesquite.lib.MesquiteTrunk;

/* �������������������� */
/*  [Search for MQLINUX] -- Intermediary class for workaround of StackOverflowError in Linux JDK 11 - 23 (at least!). 
 * These classes intercept validate and resize components on another thread in hopes of avoiding stack overflow error */
/* ======================================================================== */
public class MQComponentHelper {

	MQComponent component;
	static boolean verboseTW = false;
	static boolean linuxSendToOtherThread = false;
	public static boolean protectGraphics = true;

	public MQComponentHelper(MQComponent component){
		this.component = component;
	}

	void report(String s) {
		if (verboseTW && MesquiteTrunk.developmentMode)
			System.err.println(s);
	}
	
	public boolean touchingAnything(){
		return (touchingFont != null || touchingSize != null|| touchingLocation != null || touchingDimension!= null || touchingLayout!= null ||touchingBounds!= null);
	}

	/*
	 * ################################ This was built to avoid the frequent
	 * StackOverflowErrors on Linux Java post-1.8, but were extended in part to
	 * other OSs
	 */
	
	Thread touchingFont = null;

	public void setFont(Font f) {
		if (!protectGraphics) {
			((MQComponent) component).superSetFont(f);
			return;
		}
			
		if (touchingFont != null && touchingFont != Thread.currentThread()) {
			report("Warning: thread clash in " + component.getClass() + "  avoided (setFont). This thread: "
					+ Thread.currentThread() + "; also touching " + touchingFont);
			return;
		}
		touchingFont = Thread.currentThread();
		((MQComponent) component).superSetFont(f);
		touchingFont = null;
	}

	Thread touchingSize = null;

	public void setSize(int w, int h) {
		if (!protectGraphics) {
			((MQComponent) component).superSetSize(w, h);
			return;
		}
		if (touchingSize != null && touchingSize != Thread.currentThread()) {
			report("Warning: thread clash in " + component.getClass() + " avoided (setSize). This thread: "
					+ Thread.currentThread() + "; also touching " + touchingSize);
			return;
		}
		touchingSize = Thread.currentThread();
		((MQComponent) component).superSetSize(w, h);
		touchingSize = null;
	}

	Thread touchingLocation = null;

	public void setLocation(int x, int y) {
		if (!protectGraphics) {
			((MQComponent) component).superSetLocation(x, y);
			return;
		}
		if (touchingLocation != null && touchingLocation != Thread.currentThread()) {
			report("Warning: thread clash in " + component.getClass() + "  avoided (setLocation). This thread: " + Thread.currentThread() + "; also touching " + touchingLocation);
			return;
		}
		touchingLocation = Thread.currentThread();
		((MQComponent) component).superSetLocation(x, y);
		touchingLocation = null;
	}

	/* getPreferredSize ------------------------- */
	Thread touchingDimension = null;

	public Dimension getPreferredSize() {
		if (!protectGraphics) {
			return ((MQComponent) component).superGetPreferredSize();

		}
		if (touchingDimension != null && touchingDimension != Thread.currentThread()) {
			report("Warning: thread clash in " + component.getClass() + "  avoided (getPreferredSize). This thread: "
					+ Thread.currentThread() + "; also touching " + touchingDimension);
			return new Dimension(400, 400);
		}
		touchingDimension = Thread.currentThread();
		Dimension d = ((MQComponent) component).superGetPreferredSize();
		touchingDimension = null;
		return d;
	}

	/* layout ------------------------- */
	Thread touchingLayout = null;

	public void layout() {
		if (!protectGraphics) {
			((MQComponent) component).superLayout();
			return;
		}
		if (touchingLayout != null && touchingLayout != Thread.currentThread()) {
			report("Warning: thread clash in " + component.getClass() + "  avoided (layout). This thread: "
					+ Thread.currentThread() + "; also touching " + touchingLayout);
			return;
		}
		touchingLayout = Thread.currentThread();
		((MQComponent) component).superLayout();
		touchingLayout = null;
	}

	/* invalidate -------------------- */
	public void invalidate() {
		component.superInvalidate();

	}

	
	static boolean anyChildrenValidating(Component c){
		if (c instanceof MQComponent){
			MQComponentHelper helper = ((MQComponent)c).getHelper();
			if (helper != null && helper.touchingValidate != null)
				return true;
		}
		if (c instanceof Container){
			Component[] cc = ((Container)c).getComponents();
			for (int i=0; i<cc.length; i++) {
				if (anyChildrenValidating(cc[i]))
					return true;
			}
		}
		return false;
	}

	/* validate ------------------------- */
	boolean validating = false;
	Thread touchingValidate = null;

	public void validate() {
		if (!protectGraphics) {
			((MQComponent) component).superValidate();
			return;
		}
		/*if (linuxSendToOtherThread && MesquiteTrunk.isLinux()) { // seems to help on linux to put on separate thread
			if (MesquiteTrunk.linuxGWAThread != null)
				MesquiteTrunk.linuxGWAThread.actionRequested(component, 0, null);
		} else {
		*/

			if (touchingValidate != null) {
				if (touchingValidate != Thread.currentThread())
					report("Warning: thread clash in " + component.getClass() + "  avoided (validate). This thread: "
							+ Thread.currentThread() + "; also touching " + touchingValidate);
				else
					report("Warning: self-call " + component.getClass() + "  avoided (validate). This thread: "
							+ Thread.currentThread() + "; also touching " + touchingValidate);

				return;
			}
			Container container = ((Component) component).getParent();
			if (container != null) {
				if (!(container instanceof MQComponent))
					report("Container not an MQComponent " + component.getClass() + " container "
							+ container.getClass());
				else {
					MQComponentHelper containerHelper = ((MQComponent) container).getHelper();
					if (containerHelper != null) {
						Thread containerTouchingValidate = containerHelper.touchingValidate;
						if (containerTouchingValidate != null && containerTouchingValidate != Thread.currentThread()) {
							report("Warning: this and container have thread clash; in " + component.getClass()
									+ "  avoided (validate). This thread: " + Thread.currentThread()
									+ "; touching container " + containerTouchingValidate);
							return;
						}
					}
				}
			}
			if (validating) {
				report("Warning: double validation in " + component.getClass() + "  avoided (layout). This thread: "
						+ Thread.currentThread());
				return;
			}

			try {
				touchingValidate = Thread.currentThread();
				validating = true;
				((MQComponent) component).superValidate();
				validating = false;
				touchingValidate = null;

			} catch (Exception e) {
				report("Exception in " + component.getClass() + " (" + e.getClass() + ") (validate)");
			} catch (Error e) {
				report("Error in " + component.getClass() + " (" + e.getClass() + ") (validate)");
			}
		
	}

	/* setBounds ------------------------- */
	Thread touchingBounds = null;

	public void setBounds(int x, int y, int w, int h) {
		if (!protectGraphics) {
			((MQComponent) component).superSetBounds(x, y, w, h);
			return;
		}
		
			try {
				if (touchingBounds != null && touchingBounds != Thread.currentThread()) {
					report("Warning: thread clash in " + component.getClass() + "  avoided (validate). This thread: "
							+ Thread.currentThread() + "; also touching " + touchingBounds);
					return;
				}
				touchingBounds = Thread.currentThread();
				((MQComponent) component).superSetBounds(x, y, w, h);
				touchingBounds = null;
			} catch (Exception e) {
				report("Exception in " + component.getClass() + " (" + e.getClass() + ") (setBounds)");
			} catch (Error e) {
				report("Error in " + component.getClass() + " (" + e.getClass() + ") (setBounds)");
			}
		
	}

}