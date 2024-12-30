/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib;

import java.awt.*;
import java.util.*;

import mesquite.lib.duties.*;

import java.io.*;

/** A thread for executing commands */
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
	
	public void validateRequested(Component component){
		addOrNotToValidate(validateGreenRoom, component);
	}
	public void setBoundsRequested(Component component, int x, int y, int w, int h){
		addOrUpdateToSetBounds(setBoundsGreenRoom, new SBRecord(component, x, y, w, h));
	}
	
	void addOrNotToValidate(Vector v, Component component){
		if (v.indexOf(component)<0)
			v.addElement(component);
	}
	void addOrUpdateToSetBounds(Vector v, SBRecord sbr){
		for (int i = 0; i<v.size(); i++){
			SBRecord sbr0 = (SBRecord)v.elementAt(i);
			if (sbr0.component == sbr.component){
				sbr0.x = sbr.x;
				sbr0.y = sbr.y;
				sbr0.w = sbr.w;
				sbr0.h = sbr.h;
				return;
			}
		}
		v.addElement(sbr);
	}
	
	String getContainersList(Component c){
		if (c == null)
			return "";
		String list = " : " + c.getClass();
		if (c instanceof Listable)
			list += " [" + ((Listable)c).getName() + "] ";
		return list + getContainersList(c.getParent());

	}
	public void run(){
		String doingString = "";
		while (!MesquiteTrunk.mesquiteTrunk.mesquiteExiting) { 
			try {
				//first transfer the green room things on my thread
				for (int i = 0; i<validateGreenRoom.size(); i++)
					addOrNotToValidate(validateQueue, (Component)validateGreenRoom.elementAt(i));
					
				for (int i = 0; i<setBoundsGreenRoom.size(); i++)
					addOrUpdateToSetBounds(setBoundsQueue, (SBRecord)setBoundsGreenRoom.elementAt(i));

				for (int i = 0; i<validateQueue.size(); i++){
					Component component = (Component)validateGreenRoom.elementAt(i);
					doingString = "SOE on validate " + getContainersList(component);
					component.validate();
				}
				
				for (int i = 0; i<setBoundsQueue.size(); i++){
					SBRecord sbr = (SBRecord)setBoundsQueue.elementAt(i);
					doingString = "SOE on validate " + getContainersList(sbr.component);
					sbr.component.setBounds(sbr.x, sbr.y, sbr.w, sbr.h);
				}
					Thread.sleep(20);
					
					
			}
			catch (InterruptedException e){
			}
			catch (StackOverflowError e){
				System.out.println(doingString);
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
