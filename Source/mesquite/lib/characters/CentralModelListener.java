/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.characters; 

import java.awt.*;
import java.util.*;
import mesquite.lib.duties.*;
import mesquite.lib.*;

/*Last documented:  17 August 1998; updated May 2000 */
/* ======================================================================== */
/** A class for the object belonging to the project that serves as central location for listening to 
models and reporting to those interested if one changed.  This avoids modules having to sets themselves as listeners for all
of various models that come and go.  Each character model registers itself with this and the modules set themselves
as listeners of this CentralModelListener*/
public class CentralModelListener extends FileElement implements MesquiteListener  {

	public static void staticChanged(Object caller, Class c, Notification notification){
		Projects ps = MesquiteTrunk.mesquiteTrunk.getProjectList();
		for (int i=0; i<ps.getNumProjects(); i++){
			MesquiteProject p = ps.getProject(i);
			CentralModelListener cml = p.getCentralModelListener();
			cml.notifyListeners(caller, c, notification);
			//cml.allModelsOfClassChanged(p, caller, c, code, parameters);
		}
	}
	/*
	private void allModelsOfClassChanged(MesquiteProject p, Object caller, Class c, int code, int[] parameters){
		ModelVector mv = p.getCharacterModels();
		for (int i=0; i<mv.size(); i++){
			CharacterModel cm = (CharacterModel)mv.elementAt(i);
			if (c.isAssignableFrom(cm.getClass())) {
				notifyListeners(caller, cm, code, parameters);
			}
		}
	}
	*/
	public void changed(Object caller, Object obj, Notification notification){
		if (obj instanceof CharacterModel){
			notifyListeners(caller, obj, notification);
		}
	}
	public boolean okToDispose(Object obj, int queryUser){
		return true;
	}
	public void disposing(Object obj){
		if (obj instanceof CharacterModel){
			notifyDisposing(obj);
		}
	}
	/**/
	public void addModel(CharacterModel model){
		model.addListener(this);
	}
	public void removeModel(CharacterModel model){
		model.removeListener(this);
	}
 	/*.................................................................................................................*/
	/** notifies listeners that model has changed*/
	public void notifyListeners(Object caller, Object model, Notification notification) {
		if (MesquiteThread.getListenerSuppressionLevel()>1)
			return;
		CommandRecord.tick("Model changed");
		
		if (listeners!=null) {
			Enumeration e = listeners.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				MesquiteListener listener = (MesquiteListener)obj;
	 			listener.changed(caller, model, notification);
			}
		}
	}
	/**/
}

