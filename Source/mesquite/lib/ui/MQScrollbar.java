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

import mesquite.lib.MesquiteTrunk;

/* �������������������� */
/*  [Search for MQLINUX] -- Intermediary class for workaround of StackOverflowError in Linux JDK 11 - 23 (at least!). 
 * These classes intercept validate and resize components on another thread in hopes of avoiding stack overflow error */
/* ======================================================================== */
public class MQScrollbar extends Scrollbar {

	public MQScrollbar () {
		super();
	}
	public MQScrollbar (int policy) {
		super(policy);
	}
	public MQScrollbar ( int orientation, int value, int visible, int min, int max){
		super(orientation, value, visible, min, max);
	}


	Thread touching = null;
	public void setSize (int w, int h){
		if (touching != null && touching != Thread.currentThread()){
			if (MesquiteTrunk.developmentMode)
				System.err.println("Warning: thread clash in MQScrollbar avoided (setSize). This thread: " + Thread.currentThread() + "; also touching " + touching);
			return;
		}
		touching = Thread.currentThread();
		super.setSize(w, h);
		touching = null;
	}
	public void setBounds (int x, int y, int w, int h){
		if (touching != null && touching != Thread.currentThread()){
			if (MesquiteTrunk.developmentMode)
				System.err.println("Warning: thread clash in MQScrollbar avoided (setBounds). This thread: " + Thread.currentThread() + "; also touching " + touching);
			return;
		}
		touching = Thread.currentThread();
		super.setBounds(x, y, w, h);
		touching = null;
	}
	public void setLocation (int x, int y){
		if (touching != null && touching != Thread.currentThread()){
			if (MesquiteTrunk.developmentMode)
				System.err.println("Warning: thread clash in MQScrollbar avoided (setLocation). This thread: " + Thread.currentThread() + "; also touching " + touching);
			return;
		}
		touching = Thread.currentThread();
		super.setLocation(x, y);
		touching = null;
	}
	public void setVisibleAmount (int a){
		if (touching != null && touching != Thread.currentThread()){
			if (MesquiteTrunk.developmentMode)
				System.err.println("Warning: thread clash in MQScrollbar avoided (setVisibleAmount). This thread: " + Thread.currentThread() + "; also touching " + touching);
			return;
		}
		touching = Thread.currentThread();
		super.setVisibleAmount(a);
		touching = null;

	}
	public void setBlockIncrement (int a){
		if (touching != null && touching != Thread.currentThread()){
			if (MesquiteTrunk.developmentMode)
				System.err.println("Warning: thread clash in MQScrollbar avoided (setBlockIncrement). This thread: " + Thread.currentThread() + "; also touching " + touching);
			return;
		}
		touching = Thread.currentThread();
		super.setBlockIncrement(a);
		touching = null;
	}

	/*################################*/

}
