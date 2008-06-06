package mesquite.lib;

/* Mesquite source code.  Copyright 1997-2008 W. Maddison and D. Maddison.
Version 2.5, June 2008.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/

import java.awt.*;
import java.util.*;



/*.................................................................................................................*/
/** A class that looks after the droplisteners for a particular target*/
public class MesquiteDropManager {
	protected Vector listeners;
	public static int listenersRemaining=0;
	int listenerRemoved = -1;
	private static boolean checkMemory = MesquiteTrunk.checkMemory;
	public static Vector allListeners; //for detecting memory leaks
	static {
		allListeners = new Vector();//for detecting memory leaks
	}
	public MesquiteDropManager (){
	}
 	/*.................................................................................................................*/
	/** adds a listener to notify if the element changes*/
	public void addListener(MesquiteDropListener listener) {
		if (listeners==null) 
			listeners = new Vector();
		if (listener!=null && listeners.indexOf(listener)<0) {
			listeners.addElement(listener);
			listenersRemaining++;
			if (checkMemory)
				allListeners.addElement(new DropObjectR(listener, this));
		}
	}
 	/*.................................................................................................................*/
	/** adds a listener to notify if the element changes*/
	public void addListenerHighPriority(MesquiteDropListener listener) {
		if (listeners==null) 
			listeners = new Vector();
		if (listener!=null && listeners.indexOf(listener)<0) {
			listeners.insertElementAt(listener, 0);
			listenersRemaining++;
			if (checkMemory)
				allListeners.addElement(new DropObjectR(listener, this));
		}
	}
 	/*.................................................................................................................*/
	/** removes a listener*/
	public void removeListener(MesquiteDropListener listener) {
		if (listeners!=null && listener !=null) {
			listenerRemoved = listeners.indexOf(listener);
			listeners.removeElement(listener);
			listenersRemaining--;
			if (checkMemory)
				DropObjectR.removeMe(allListeners, listener, this);
		}
	}
	/*.................................................................................................................*/
	/** Returns whether the listener is in the list of listeners*/
	public boolean amIListening(MesquiteDropListener me) {
		if (listeners!=null) {
			Enumeration e = listeners.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				MesquiteDropListener listener = (MesquiteDropListener)obj;
	 			if (me==listener)
	 				return true;
			}
		}
		return false;
	}
	/*.................................................................................................................*/
 	/** notifies listeners that the element, and in particular part i, has changed.  The ith part may
 	be the ith character (if element is a CharacterData), or the ith tree (if a TreeVector), etc..
 	Notifies only those elements of class clss if boolean is true; notifies only those elements not of class
 	clss if boolean is false*/
	public  boolean askListenersToProcess(Object obj, boolean onlyOneProcessor) { 
		boolean success=false;
		if (listeners!=null) {
				MesquiteDropListener[] ls = new MesquiteDropListener[listeners.size()];
				for (int m=0; m<ls.length; m++)
					ls[m]= (MesquiteDropListener)listeners.elementAt(m);
				for (int m=0; m<ls.length; m++) {
		 			if ( ls[m].processDroppedObject(obj))
		 				success=true;
		 			if (success && onlyOneProcessor)
		 				return true;
		 		
	 		}
		}
		return success;
	}
	/*.................................................................................................................*/
 	/** Returns the number of listeners remaining that are still registered with this element.
 	Useful to see that listeners are removing themselves as needed. */
	public static int getRemainingListeners(){
		return listenersRemaining;
	}
	
	
}

class DropObjectR extends ObjectContainer {
	Object listener;
	MesquiteDropManager listened;
	public DropObjectR(Object listener, MesquiteDropManager listened){
		this.listener = listener;
		this.listened = listened;
		setObject(listener);
	}
	static void removeMe(Vector allListeners, Object listener, MesquiteDropManager listened){
		for (int i = 0; i<allListeners.size(); i++){
			DropObjectR r = (DropObjectR)allListeners.elementAt(i);
			if (r.listener == listener && r.listened == listened) {
				allListeners.removeElement(r);
				return;
			}
		}
	}
}

