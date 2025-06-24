package mesquite.lib;

import java.util.Enumeration;
import java.util.Vector;



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

