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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Scrollbar;

import mesquite.lib.MesquiteTrunk;

/* �������������������� */
/*  [Search for MQLINUX] -- Intermediary class for workaround of StackOverflowError in Linux JDK 11 - 23 (at least!). 
 * These classes intercept validate and resize components on another thread in hopes of avoiding stack overflow error */
/* ======================================================================== */
public class MQScrollbar extends Scrollbar implements MQComponent {

	public MQScrollbar () {
		super();
		helper = new MQComponentHelper(this);
	}
	public MQScrollbar (int policy) {
		super(policy);
		helper = new MQComponentHelper(this);
	}
	public MQScrollbar ( int orientation, int value, int visible, int min, int max){
		super(orientation, value, visible, min, max);
		helper = new MQComponentHelper(this);
	}

	Thread touchingSVA = null;
	public void setVisibleAmount (int a){
		if (touchingSVA != null && touchingSVA != Thread.currentThread()){
			if (verboseTW && MesquiteTrunk.developmentMode)
				System.err.println("Warning: thread clash in MQScrollbar avoided (setVisibleAmount). This thread: " + Thread.currentThread() + "; also touching " + touchingSVA);
			return;
		}
		touchingSVA = Thread.currentThread();
		super.setVisibleAmount(a);
		touchingSVA = null;

	}
	Thread touchingSBO = null;
	public void setBlockIncrement (int a){
		if (touchingSBO != null && touchingSBO != Thread.currentThread()){
			if (verboseTW && MesquiteTrunk.developmentMode)
				System.err.println("Warning: thread clash in MQScrollbar avoided (setBlockIncrement). This thread: " + Thread.currentThread() + "; also touching " + touchingSBO);
			return;
		}
		touchingSBO = Thread.currentThread();
		super.setBlockIncrement(a);
		touchingSBO = null;
	}

	//###########################################################
	/*################################################################
	 *  The following overrides were built to avoid the frequent StackOverflowErrors on Linux Java post-1.8, 
	 *  but were extended in part to other OSs. See also others satisfying MQComponent interface.
	 */		
	MQComponentHelper helper = new MQComponentHelper(this);
	public MQComponentHelper getHelper(){
		return helper;
	}
	public void superValidate(){
		super.validate();
	}
	public void superSetBounds(int x, int y, int w, int h){
		super.setBounds(x,y,w,h);
	}
	public void superSetFont (Font f){
	super.setFont(f);
	}
	public void superSetSize (int w, int h){
		super.setSize(w,h);
	}
	public void superSetLocation (int x, int y){
		super.setLocation(x,y);
	}
	public Dimension superGetPreferredSize(){
		return super.getPreferredSize();
	}
	public void superLayout(){
		super.layout();
	}
	public void superInvalidate(){
		super.invalidate();
	}
	/* - - - - - - */
	public void invalidate (){
		if (helper == null)
			superInvalidate();
		else
			helper.invalidate();
	}

	public void setFont (Font f){
		if (helper == null)
			superSetFont(f);
		else
			helper.setFont(f);
	}
	public void setSize (int w, int h){
		if (helper == null)
			superSetSize(w,h);
		else
			helper.setSize(w, h);
	}
	public void setLocation (int x, int y){
		if (helper == null)
			superSetLocation(x, y);
		else
			helper.setLocation(x,y);
	}
	public Dimension getPreferredSize() {
		if (helper == null)
			return superGetPreferredSize();
		else
			return helper.getPreferredSize();
	}
	public void layout(){
		if (helper == null)
			superLayout();
		else
			helper.layout();
	}
	public void validate(){
		if (helper == null)
			superValidate();
		else
			helper.validate();
	}
	public void setBounds(int x, int y, int w, int h){
		if (helper == null)
			superSetBounds(x,y,w,h);
		else
			helper.setBounds(x,y,w,h);
	}
	/*###########################################################*/
	//###########################################################



}
