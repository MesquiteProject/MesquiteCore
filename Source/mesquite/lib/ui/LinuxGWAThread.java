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
import java.util.*;

import mesquite.lib.Debugg;
import mesquite.lib.Listable;
import mesquite.lib.MesquiteTrunk;

//** TEMPORARY Debugg.println(

/**  [Search for MQLINUX] -- A thread attempting to sidestep bug in Linux JDKs 11 to 23. See MQ graphics components and ExplTextArea in ExplanationArea */
public class LinuxGWAThread extends Thread {
	Vector greenRoom = new Vector();
	Vector queue = new Vector();
	public LinuxGWAThread () {
		super();
	}
	public LinuxGWAThread (Runnable r) {
		super(r);
	}

	/**------------------------------------------------------------------------*/
	boolean putOnThisThread = false;

	public void actionRequested(MQComponent component, int action, String text){
		if (putOnThisThread)  //to short circuit or not?
			addOrNotToQueue(greenRoom, new ARecord(component, action, text));
		else 
			doAction(component, action, text);
	}
	

	/**------------------------------------------------------------------------*/
	void addOrNotToQueue(Vector v, ARecord aRec){
		if (v.indexOf(aRec)<0) {
			v.addElement(aRec);
		}
	}

	void doAction(MQComponent component, int action, String text){
		if (action == 0)
			((MQComponent)component).superValidate();
	/*	else if (action == 1 && component instanceof MQTextComponent)
			((MQTextComponent)component).superSetEditable("true".equalsIgnoreCase(text));
		else if (action == 2 && component instanceof MQTextComponent)
			((MQTextComponent)component).superSetText(text);*/
			
	}
	/**------------------------------------------------------------------------*/


	String getContainersList(Component c){
		if (c == null)
			return "";
		String list = " : " + c.getClass();
		if (c instanceof Listable)
			list += " [" + ((Listable)c).getName() + "] ";
		return list + getContainersList(c.getParent());

	}


	public void run(){
		if (!MesquiteTrunk.isLinux())  //Debugg.println don't do if not Linux
			return;
		while (!MesquiteTrunk.mesquiteTrunk.mesquiteExiting) { 
			try {

				Thread.sleep(50);
				//first transfer the green room things on my thread
				if (queue.size()== 0 ) { //swap only if was successful last time
					Vector temp = greenRoom;
					greenRoom = queue;
					queue = temp;
				}


				for (int i = 0; i<queue.size(); i++){
					ARecord aRec = (ARecord)queue.elementAt(i);
					doAction(aRec.component, aRec.action, aRec.text);

				}

				
				queue.removeAllElements();



			}
			catch (InterruptedException e){
			}
			
		}
	}



}

class ARecord {
	int action;
	String text;
	MQComponent component;
	public ARecord(MQComponent component, int action, String text){
		this.component = component;
		this.action = action;
		this.text = text;
	}
}
