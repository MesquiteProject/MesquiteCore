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



/*.................................................................................................................*/
/** A class that notifies listeners if it changes.  Superclass for Associable and MesquiteString*/
public class Listened implements Listenable {
	protected Vector listeners;
	public static int listenersRemaining=0;
	private int notifySuppress = 0;
	private Notification lastNotification = null;
	private boolean notifyPending = false;
	private boolean dumpNotified = false;
	private static boolean checkMemory = MesquiteTrunk.checkMemory;
	public static long notifications = 0;
	public static ClassVector classes, classesNotified; //for detecting efficiency problems
	public static Vector allListeners; //for detecting memory leaks
	static {
		allListeners = new Vector();//for detecting memory leaks
	}
	public Listened (){
	}

	public boolean isUserVisible(){ //here as a courtesy so that as many classes as possible can support this
		return true;
	}
	/*.................................................................................................................*/
	/** lists listeners of element*/
	public void listListeners() {
		if (listeners!=null) {
			MesquiteTrunk.mesquiteTrunk.logln("Listeners of  " +this);
			Enumeration e = listeners.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				MesquiteListener listener = (MesquiteListener)obj;
				if (listener instanceof Listable && listener instanceof Identifiable)
					MesquiteTrunk.mesquiteTrunk.logln("      listener: " + ((Listable)listener).getName()+ " id " + ((Identifiable)listener).getID() + " class " + listener.getClass());
			}
			MesquiteTrunk.mesquiteTrunk.logln("- - - - - - -  " +this);
		}
	}
	public String listenersToString() {
		if (listeners == null)
			return "";
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i< listeners.size(); i++){
			Object obj = listeners.elementAt(i);
			sb.append(MesquiteModule.getShortClassName(obj.getClass()) + " ");
			if (obj instanceof Listable)
				sb.append(((Listable)obj).getName());
			sb.append("\n");
		}
		return sb.toString();
	}
	public int getNumListeners() {
		if (listeners == null)
			return 0;
		return listeners.size();
	}
	/*.................................................................................................................*/
	/** adds a listener to notify if the element changes*/
	public void addListener(MesquiteListener listener) {
		if (listener==this)
			MesquiteMessage.warnProgrammer("********* Listener is listening to itself!");
		if (listeners==null) 
			listeners = new Vector();
		if (listener!=null && listeners.indexOf(listener)<0) {
			listeners.addElement(listener);
			listenersRemaining++;
			if (checkMemory)
				allListeners.addElement(new ObjectR(listener, this));
		}
	}
	/*.................................................................................................................*/
	/** adds a listener to notify if the element changes*/
	public void addListenerHighPriority(MesquiteListener listener) {
		if (listeners==null) 
			listeners = new Vector();
		if (listener!=null && listeners.indexOf(listener)<0) {
			listeners.insertElementAt(listener, 0);
			listenersRemaining++;
			if (checkMemory)
				allListeners.addElement(new ObjectR(listener, this));
		}
	}
	/*.................................................................................................................*/
	/** removes all listeners*/
	public void removeAllListeners() {
		if (listeners!=null) {
			while( listeners.size()>0)
				removeListener((MesquiteListener)listeners.elementAt(0));
		}
	}
	/*.................................................................................................................*/
	/** removes a listener*/
	public void removeListener(MesquiteListener listener) {
		if (listeners!=null && listener !=null) {
			listeners.removeElement(listener);
			listenersRemaining--;
			if (checkMemory)
				ObjectR.removeMe(allListeners, listener, this);
		}
	}
	public static String reportListeners() {
		if (!checkMemory)
			return "checkMemory is false; listeners cannot be reported";
		ClassVector classesListened = new ClassVector();
		ClassVector classesListener = new ClassVector();
		for (int i=0; i<allListeners.size(); i++){
			ObjectR rec = (ObjectR)allListeners.elementAt(i);
			classesListened.record(rec.listened.getClass());
			classesListener.record(rec.listener.getClass());
		}
		String s = "classes listened=====\n" + classesListened.recordsToString();
		s += "\nclasses listener=====\n" + classesListener.recordsToString();
		return s;
	}
	public void incrementNotifySuppress(){
		notifySuppress ++;
	}
	public void decrementNotifySuppress(){
		notifySuppress --;
		if (notifySuppress<=0){
			notifySuppress = 0;
			if (notifyPending){
				notifyListeners(this, lastNotification);
			}
		}
	}
	/*.................................................................................................................*/
	/** Returns whether the listener is in the list of listeners*/
	public boolean amIListening(MesquiteListener me) {
		if (listeners!=null) {
			Enumeration e = listeners.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				MesquiteListener listener = (MesquiteListener)obj;
				if (me==listener)
					return true;
			}
		}
		return false;
	}
	/*.................................................................................................................*/
	/** When true, causes list of notifications made to be echoed to the console*/
	public void setDumpNotified(boolean dn){
		dumpNotified = dn;
	}
	MesquiteTimer[] timers = {new MesquiteTimer(),new MesquiteTimer(),new MesquiteTimer(),new MesquiteTimer(),new MesquiteTimer(),
			new MesquiteTimer(),new MesquiteTimer(),new MesquiteTimer(),new MesquiteTimer(),new MesquiteTimer(),
			new MesquiteTimer(),new MesquiteTimer(),new MesquiteTimer(),new MesquiteTimer(),new MesquiteTimer(),
			new MesquiteTimer(),new MesquiteTimer(),new MesquiteTimer(),new MesquiteTimer(),new MesquiteTimer(),
			new MesquiteTimer(),new MesquiteTimer(),new MesquiteTimer(),new MesquiteTimer(),new MesquiteTimer(),
			new MesquiteTimer(),new MesquiteTimer(),new MesquiteTimer(),new MesquiteTimer(),new MesquiteTimer(),
			new MesquiteTimer(),new MesquiteTimer()};

	/*.................................................................................................................*/
	/** notifies listeners that the element, and in particular part i, has changed.  The ith part may
 	be the ith character (if element is a CharacterData), or the ith tree (if a TreeVector), etc..
 	Notifies only those elements of class clss if boolean is true; notifies only those elements not of class
 	clss if boolean is false*/
	public  void notifyListeners(Object caller, Notification notification, Class clss, boolean classOnly) { 
		if (MesquiteThread.getListenerSuppressionLevel()>1)
			return;
		if (listeners!=null) {
			if (notifySuppress==0){
				MesquiteThread.incrementDuringNotification();
				boolean timeListeners = true;
				MesquiteTimer timer = new MesquiteTimer();
				timer.start();

				notifications++;
				if (classes !=null)
					classes.record(getClass());
				notifyPending = false;
				lastNotification = null;
				/*Listeners notified by first building array, so as to avoid notifying 
				listeners newly added to this list during this notification process */
				MesquiteListener[] ls = new MesquiteListener[listeners.size()];
				for (int m=0; m<ls.length; m++)
					ls[m]= (MesquiteListener)listeners.elementAt(m);
				for (int m=0; m<ls.length; m++) {
					if (clss == null || (classOnly && clss.isAssignableFrom(ls[m].getClass())) || (!classOnly && !clss.isAssignableFrom(ls[m].getClass())))
						if (!(ls[m] instanceof Doomable) || !((Doomable)ls[m]).isDoomed()) {
							if (dumpNotified)
								MesquiteTrunk.mesquiteTrunk.logln("(" + m + ") notifying " + ls[m] + " of change in " + this + ": " + notification.getCode() + " (caller: " + caller + ")");
							if (classesNotified !=null)
								classesNotified.record(ls[m].getClass());
							if (dumpNotified && m<timers.length)
								timers[m].start();
							if (MesquiteTrunk.debugMode) {
								timer.timeSinceLast();
								ls[m].changed(caller, this, notification);
								long time = timer.timeSinceLast();
								if (time>20)
									MesquiteMessage.println("@Time: " +  time + " ms. " + "Object: " + this + ".   Listener: " + ls[m]);
							} else {
								try {
									ls[m].changed(caller, this, notification);
								}
								catch (Throwable e){  //added 2. 72 to avoid crash in changed from stopping all other listeners from hearing
									try {
										String warning = "Crash when notifying " + ls[m] + " of change in " + this + " {Notification code " + Notification.getCode(notification) + " params " + IntegerArray.toString(Notification.getParameters(notification)) + "} ";
										MesquiteMessage.println("Crash when notifying " + ls[m] + " of change in " + this);
										MesquiteFile.throwableToLog(this, e);
										MesquiteTrunk.mesquiteTrunk.exceptionAlert(e, warning);
									}
									catch (Throwable f){
									}
								}
							}
							if (dumpNotified && m<timers.length){
								timers[m].end();
							}
						}
				}
				if (dumpNotified){
					System.out.print("timers " );
					for (int m = 0; m<timers.length; m++)
						System.out.print(" (" + m + ") " + timers[m].getAccumulatedTime());
					System.out.println();
				}
				MesquiteThread.decrementDuringNotification();
			}
			else {
				notifyPending = true;
				lastNotification =notification;
			}
		}
		doAfterNotify(notification);
	}
	/*.................................................................................................................*/
	//can be overrided as needed (see CharacterData for example)
	public void doAfterNotify(Notification notification){

	}

	/*.................................................................................................................*/
	/** notifies listeners that the element, and in particular part i, has changed.  The ith part may
 	be the ith character (if element is a CharacterData), or the ith tree (if a TreeVector), etc. */
	public void notifyListeners(Object caller, Notification notification) { 
		notifyListeners(caller, notification, null, true);
	}
	/*.................................................................................................................*/
	/** Returns the number of listeners remaining that are still registered with this element.
 	Useful to see that listeners are removing themselves as needed. */
	public static int getRemainingListeners(){
		return listenersRemaining;
	}

	/*-------------------------------------------------------*/
	/** Deletes the object. Should typically be called via close()  to make sure that the file element is not in use etc. */
	public void dispose() {
	
		/*  tell all listeners that this is being dleeted*/
		if (listeners!=null) {
			if (notifySuppress==0){
				boolean sizeChanged = false;
				Vector notified = new Vector();
				
				boolean stillGoing = true;
				while (stillGoing) {
					stillGoing = false;
					sizeChanged = false;
					for (int i=0; i<listeners.size(); i++) {
						Object obj = listeners.elementAt(i);
						MesquiteListener listener = (MesquiteListener)obj;
						if ((notified.indexOf(listener)<0) && (!(listener instanceof Doomable) || !((Doomable)listener).isDoomed())){
							if (dumpNotified)
								MesquiteTrunk.mesquiteTrunk.logln("notifying " + listener + " of disposal of " + this);
							int oldSize = listeners.size();
							notified.addElement(listener);
							listener.disposing(this); //in this the listeners could be changed, thus
							if (!sizeChanged)
								sizeChanged = listeners.size() != oldSize;
						}
						listenersRemaining--;
						if (checkMemory)
							ObjectR.removeMe(allListeners, listener, this);
					}
					if (sizeChanged)
						stillGoing = true;
				}
			}
			removeAllListeners();
			//listeners.removeAllElements();
		}
	}

	/*-------------------------------------------------------*/
	/** Notifies all the listeners that the passed object is being disposed.  Added June 02*/
	public void notifyDisposing(Object doomedObject) {
		/*  tell all listeners that this object is being dleeted*/
		if (listeners!=null) {
			if (notifySuppress==0){
				for (int i=0; i<listeners.size(); i++) {
					Object obj = listeners.elementAt(i);
					MesquiteListener listener = (MesquiteListener)obj;
					if (!(listener instanceof Doomable) || !((Doomable)listener).isDoomed())
						listener.disposing(doomedObject); 
				}

			}
		}
	}
}

class ObjectR extends ObjectContainer {
	Object listener;
	Listened listened;
	public ObjectR(Object listener, Listened listened){
		this.listener = listener;
		this.listened = listened;
		setObject(listener);
	}
	static void removeMe(Vector allListeners, Object listener, Listened listened){
		for (int i = 0; i<allListeners.size(); i++){
			ObjectR r = (ObjectR)allListeners.elementAt(i);
			if (r.listener == listener && r.listened == listened) {
				allListeners.removeElement(r);
				return;
			}
		}
	}
	public String toString() {
		String s = "Listener " + listener.getClass();
		if (listener instanceof Listable)
			s+= " (name " + ((Listable)listener).getName() + ")";
		s += ", listening to " + listened.getClass();
		if (listener instanceof FileElement)
			s+= " (disposed " + ((FileElement)listener).isDisposed() + ")";
		if (listened instanceof Listable)
			s+= " (name " + ((Listable)listened).getName() + ")";
		if (listened instanceof FileElement)
			s+= " (disposed " + ((FileElement)listened).isDisposed() + ")";
		return s;
	}
}


