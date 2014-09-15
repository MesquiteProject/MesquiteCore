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
import java.text.*;



public interface Listenable {
 	/*.................................................................................................................*/
	/** notifies listeners that element has changed*/
	public void notifyListeners(Object caller, Notification notification);
	/** adds a listener to notify if the element changes*/
	public void addListener(MesquiteListener listener);
	/** adds a listener to notify if the element changes; add to start of listener vector so it will be notified early*/
	public void addListenerHighPriority(MesquiteListener listener);
	/** removes a listener*/
	public void removeListener(MesquiteListener listener);
	/** Increments the suppression of listener notification*/
	public void incrementNotifySuppress();
	/** Decrements the suppression of listener notification*/
	public void decrementNotifySuppress();
	/** Dump to log a list of listeners*/
	public void listListeners();
	/** Returns whether the listener is in the list of listeners*/
	public boolean amIListening(MesquiteListener me);
}

