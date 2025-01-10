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



/**  [Search for MQLINUX] -- A thread attempting to sidestep bug in Linux JDKs 11 to 23. See MQ graphics components and ExplTextArea in ExplanationArea */
public class LinuxGWAThread extends Thread {
	Vector validateGreenRoom = new Vector();
	Vector setBoundsGreenRoom = new Vector();
	Vector validateQueue = new Vector();
	Vector setBoundsQueue = new Vector();
	public LinuxGWAThread () {
		super();
	}
	public LinuxGWAThread (Runnable r) {
		super(r);
	}

	/**------------------------------------------------------------------------*/
	boolean putValidatesOnThisThread = true;
	boolean putSetBoundsOnThisThread = false;

	public void validateRequested(Component component){
		if (putValidatesOnThisThread)  //to short circuit or not?
			addOrNotToValidate(validateGreenRoom, component, null);
		else
			((MQComponent)component).pleaseValidate();
	}
	public void setBoundsRequested(Component component, int x, int y, int w, int h){
		if (putSetBoundsOnThisThread)  //to short circuit or not?
			addOrUpdateToSetBounds(setBoundsGreenRoom, new SBRecord(component, x, y, w, h), null);
		else
			((MQComponent)component).pleaseSetBounds(x, y, w, h);
	}

	/**------------------------------------------------------------------------*/
	void addOrNotToValidate(Vector v, Component component, Vector toDeleteElement){
		if (v.indexOf(component)<0) {
			v.addElement(component);
		}
		if (toDeleteElement != null)
			toDeleteElement.remove(component);
	}

	void addOrUpdateToSetBounds(Vector v, SBRecord sbr, Vector toDeleteElement){
		for (int i = 0; i<v.size(); i++){
			try {
				SBRecord sbr0 = (SBRecord)v.elementAt(i);
				if (sbr0.component == sbr.component){
					sbr0.x = sbr.x;
					sbr0.y = sbr.y;
					sbr0.w = sbr.w;
					sbr0.h = sbr.h;
					if (toDeleteElement != null)
						toDeleteElement.remove(sbr);
					return;
				}
			}
			catch (ArrayIndexOutOfBoundsException e) {

			}
		}
		v.addElement(sbr);
		if (toDeleteElement != null)
			toDeleteElement.remove(sbr);
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
		String doingString = "";
		int count = 0;
		while (!MesquiteTrunk.mesquiteTrunk.mesquiteExiting) { 
			count++;
			try {
				//	if (count % 200 == 0)
				//		Debugg.println(MesquiteFrame.listComponentsAllWindows());
				Thread.sleep(50);
				//first transfer the green room things on my thread
				if (validateQueue.size()== 0 && setBoundsQueue.size() == 0) { //swap only if was successful last time
					Vector temp = validateGreenRoom;
					validateGreenRoom = validateQueue;
					validateQueue = temp;
					temp = setBoundsGreenRoom;
					setBoundsGreenRoom = setBoundsQueue;
					setBoundsQueue = temp;
				}
				/*while (validateGreenRoom.size()>0)
					addOrNotToValidate(validateQueue, (Component)validateGreenRoom.elementAt(0), validateGreenRoom);

				while (setBoundsGreenRoom.size()>0)
					addOrUpdateToSetBounds(setBoundsQueue, (SBRecord)setBoundsGreenRoom.elementAt(0), setBoundsGreenRoom);
				 */
				//	Debugg.println("$$$$$$$$$$$$$$$$$$validateQueue " +validateQueue.size());
				for (int i = 0; i<validateQueue.size(); i++){
					Component component = (Component)validateQueue.elementAt(i);
					doingString = "SOE on validate " + getContainersList(component);
					((MQComponent)component).pleaseValidate();
				}

				for (int i = 0; i<setBoundsQueue.size(); i++){
					SBRecord sbr = (SBRecord)setBoundsQueue.elementAt(i);
					doingString = "SOE on setBounds " + getContainersList(sbr.component);
					((MQComponent)sbr.component).pleaseSetBounds(sbr.x, sbr.y, sbr.w, sbr.h);
				}
				validateQueue.removeAllElements();
				setBoundsQueue.removeAllElements();



			}
			catch (InterruptedException e){
			}
			catch (StackOverflowError e){
				System.out.println(doingString);
				Debugg.println(doingString);
			}
		}
	}



}

class SBRecord {
	int x, y, w, h;
	Component component;
	public SBRecord(Component component, int x, int y, int w, int h){
		this.component = component;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}
}
