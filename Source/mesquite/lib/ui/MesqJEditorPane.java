/* Mesquite source code.  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib.ui;

import javax.swing.JEditorPane;

import mesquite.lib.Debugg;
import mesquite.lib.MesquiteTrunk;

import java.awt.*;

//workaround for crashes on OS X && Linux  [Search for MQLINUX]
public class MesqJEditorPane extends JEditorPane implements MQComponent{
	public MesqJEditorPane(String a, String b){
		super(a, b);
	}
	public boolean getScrollableTracksViewportWidth() {
		try {
			return super.getScrollableTracksViewportWidth();
		}
		catch(Exception e){
		}
		return true;
	}
	public boolean getScrollableTracksViewportHeight() {
		try {
			return super.getScrollableTracksViewportHeight();
		}
		catch(Exception e){
		}

		return true;
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
			System.err.println("Exception in " + getClass() + " (" + e.getClass() + ") (getPreferredSize)"); 
		}
		catch (Error e) {
			if (MesquiteTrunk.developmentMode)
			System.err.println("Error in " + getClass() + " (" + e.getClass() + ") (getPreferredSize)"); 
		}
		return new Dimension(400, 400);
	}
	/*validate -------------------------*/
	boolean validating = false;
	public void validate(){
		if (MesquiteTrunk.isLinux() && MesquiteTrunk.linuxGWAThread!=null)
			MesquiteTrunk.linuxGWAThread.validateRequested(this);
		else {
			if (validating)
				Debugg.println("Double validating " + this);
			validating = true;
			super.validate();
			validating = false;
		}
	}
	public void pleaseValidate(){
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


