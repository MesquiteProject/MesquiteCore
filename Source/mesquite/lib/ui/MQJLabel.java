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

import javax.swing.JLabel;
import javax.swing.JPanel;

import mesquite.lib.MesquiteTrunk;

/* �������������������� */
/*  [Search for MQLINUX] -- Intermediary class for workaround of StackOverflowError in Linux JDK 11 - 23 (at least!). 
 * These classes intercept validate and resize components on another thread in hopes of avoiding stack overflow error */
/* ======================================================================== */
public class MQJLabel extends JLabel implements MQComponent {

	public MQJLabel () {
		super();
	}
	public MQJLabel (String s) {
		super(s);
	}

	/*getPreferredSize -------------------------*/
    public Dimension getPreferredSize() {
		if (MesquiteTrunk.isLinux()) {
			try {
				return super.getPreferredSize();
			}
			catch (StackOverflowError e) {
				if (MesquiteTrunk.developmentMode)
				System.err.println("Yet another StackOverflowError on  linux");
			}
		}
		try {
			return super.getPreferredSize();
		}
		catch (Exception e) {
			if (MesquiteTrunk.developmentMode)
			System.err.println("Exception in " + getClass() + " (" + e.getClass() + ")"); 
		}
		return new Dimension(400, 400);
	}
	/*layout -------------------------*/
	public void layout(){
		if (MesquiteTrunk.isLinux()) {
			try {
				super.layout();
			}
			catch (StackOverflowError e) {
				System.out.println("Yet another StackOverflowError on  linux");
			}
		}
		else {
			super.layout();
		}
	}
	/*validate -------------------------*/
	boolean validating = false;
	public void validate(){
		if (MesquiteTrunk.isLinux()) {
			if (MesquiteTrunk.linuxGWAThread!=null)
				MesquiteTrunk.linuxGWAThread.validateRequested(this);
		}
		else {
			super.validate();
		}
	}
	public void pleaseValidate(){
		if (!validating) {
			validating = true;
			try {
				super.validate();
			}
			catch (StackOverflowError e) {
				if (MesquiteTrunk.developmentMode)
			System.out.println("Yet another StackOverflowError on  linux");
				
			}
			validating = false;
		}
	}


	/*setBounds -------------------------*/
	//This is currently bypassed (see linxuGWAThread) and may not be needed; left here in case further testing shows this protection is needed also. See ExplTextArea also
	public void setBounds(int x, int y, int w, int h){
		if (MesquiteTrunk.isLinux() && MesquiteTrunk.linuxGWAThread!=null)
			MesquiteTrunk.linuxGWAThread.setBoundsRequested(this, x, y, w, h);
		else
			super.setBounds(x, y, w, h);
	}
	public void pleaseSetBounds(int x, int y, int w, int h){
		super.setBounds(x, y, w, h);
	}
	/*s----- -------------------------*/

}
