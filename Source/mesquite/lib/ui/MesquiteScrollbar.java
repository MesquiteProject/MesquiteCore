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

import java.awt.*;
import java.awt.event.*;

import mesquite.lib.Debugg;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.system.*;

/* ��������������������������� commands ������������������������������� */
/* includes commands,  buttons, miniscrolls
/* ======================================================================== */
/* scrollbar for tree */
public abstract class MesquiteScrollbar extends MQScrollbar implements MQComponent, AdjustmentListener {
	public MesquiteScrollbar ( int orientation, int value, int visible, int min, int max){
		super(orientation, value, visible, min, max);
		addAdjustmentListener(this);
		SystemUtil.setFocusable(this, false);
		setCursor(Cursor.getDefaultCursor());
	}
	public void adjustmentValueChanged(AdjustmentEvent e) {
		//Event queue
		if (processDuringAdjustment() || !e.getValueIsAdjusting())
			scrollTouched();
	}
	public abstract void scrollTouched();
	
	public boolean processDuringAdjustment() {
		return true;
	}
	/*################################
	 *  The following overrides were built to handle (hide) the frequent StackOverflowErrors on Linux Java post-1.8, 
	 *  but were extended in part to other OSs
	 */

	/*getPreferredSize -------------------------*/
	public Dimension getPreferredSize() {
		try {
			return super.getPreferredSize();
		}
		catch (Exception e) {
			if (MesquiteTrunk.developmentMode)
				System.err.println("Exception in " + getClass() + " (" + e.getClass() + ") (getPreferredSize)"); 
		}
		catch (Error e) {
			if (MesquiteTrunk.developmentMode)
				System.err.println("Error in " + getClass() + " (" + e.getClass() + ") (getPreferredSize)"); 
		}
		return new Dimension(400, 400);
	}
	/*layout -------------------------*/
	public void layout(){
		try {
			super.layout();
		}
		catch (Exception e) {
			if (MesquiteTrunk.developmentMode)
				System.err.println("Exception in " + getClass() + " (" + e.getClass() + ") (layout)"); 
		}
		catch (Error e) {
			if (MesquiteTrunk.developmentMode)
				System.err.println("Error in " + getClass() + " (" + e.getClass() + ") (layout)"); 
		}
	}
	/*validate -------------------------*/
	boolean validating = false;
	public void validate(){
		if (MesquiteTrunk.isLinux()) { //seems to help on linux to put on separate thread
			if (MesquiteTrunk.linuxGWAThread!=null)
				MesquiteTrunk.linuxGWAThread.validateRequested(this);
		}
		else {
			try {
				super.validate();
			}
			catch (Exception e) {
				if (MesquiteTrunk.developmentMode)
					System.err.println("Exception in " + getClass() + " (" + e.getClass() + ") (validate)"); 
			}
			catch (Error e) {
				if (MesquiteTrunk.developmentMode)
					System.err.println("Error in " + getClass() + " (" + e.getClass() + ") (validate)"); 
			}
		}
	}

	public void pleaseValidate(){ //this will only be called on linux
		if (validating && MesquiteTrunk.developmentMode)
			System.err.println("Double validating " + this);
		validating = true;
		try {
			super.validate();
		}
		catch (Exception e) {
			if (MesquiteTrunk.developmentMode)
				System.err.println("Exception in " + getClass() + " (" + e.getClass() + ") (pleaseValidate)"); 
		}
		catch (Error e) {
			if (MesquiteTrunk.developmentMode)
				System.err.println("Error in " + getClass() + " (" + e.getClass() + ") (pleaseValidate)"); 
		}
		validating = false;
	}


	/*setBounds -------------------------*/
	public void setBounds(int x, int y, int w, int h){
		//This is currently bypassed (see linxuGWAThread) and may not be needed; 
		if (MesquiteTrunk.isLinux() && MesquiteTrunk.linuxGWAThread!=null)
			MesquiteTrunk.linuxGWAThread.setBoundsRequested(this, x, y, w, h);
		else {
			try {
				super.setBounds(x, y, w, h);
			}
			catch (Exception e) {
				if (MesquiteTrunk.developmentMode)
					System.err.println("Exception in " + getClass() + " (" + e.getClass() + ") (setBounds)"); 
			}
			catch (Error e) {
				if (MesquiteTrunk.developmentMode)
					System.err.println("Error in " + getClass() + " (" + e.getClass() + ") (setBounds)"); 
			}
		}
	}
	public void pleaseSetBounds(int x, int y, int w, int h){ //this will only be called on linux
		try {
			super.setBounds(x, y, w, h);
		}
		catch (Exception e) {
			if (MesquiteTrunk.developmentMode)
				System.err.println("Exception in " + getClass() + " (" + e.getClass() + ") (pleaseSetBounds)"); 
		}
		catch (Error e) {
			if (MesquiteTrunk.developmentMode)
				System.err.println("Error in " + getClass() + " (" + e.getClass() + ") (pleaseSetBounds)"); 
		}
	}
	/*################################*/

}


