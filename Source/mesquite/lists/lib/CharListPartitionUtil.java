/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison. 
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lists.lib;
/*~~  */

import mesquite.lists.lib.*;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class CharListPartitionUtil {

	/*.................................................................................................................*/
	public static Object editGroup(MesquiteModule ownerModule, CharacterData data, MesquiteWindow cont, String name, String num) {
		int i = MesquiteInteger.fromString(num);
		CharactersGroupVector groups = (CharactersGroupVector)(data.getProject()).getFileElement(CharactersGroupVector.class, 0);
		Object obj;
		if (MesquiteInteger.isCombinable(i) && i< groups.size())
			obj = groups.elementAt(i);
		else
			obj = groups.getElement(name);
		if (obj != null) {
			CharactersGroup group = (CharactersGroup)obj;
			GroupDialog d = new GroupDialog(ownerModule.getProject(),MesquiteWindow.windowOfItem(cont), "Edit Character Group", group.getName(), group.getColor(), group.getSymbol(), group.supportsSymbols());
    	 		d.completeAndShowDialog();
			name = d.getName();
			boolean ok = d.query()==0;
			Color c = d.getColor();
			d.dispose();
			if (!ok)
				return null;


			if (!StringUtil.blank(name)) {
				group.setName(name);
				group.setColor(c);
    	 			data.notifyListeners(ownerModule, new Notification(MesquiteListener.NAMES_CHANGED)); //TODO: bogus! should notify via specs not data???
					group.notifyListeners(group, new Notification(MesquiteListener.DATA_CHANGED));
			}
		}
		return obj;
	}
	public static boolean deleteGroup(MesquiteModule ownerModule, CharacterData data, MesquiteWindow cont, String name, String num) {
		int i = MesquiteInteger.fromString(num);
		CharactersGroupVector groups = (CharactersGroupVector)(data.getProject()).getFileElement(CharactersGroupVector.class, 0);
		Object obj;
		if (MesquiteInteger.isCombinable(i) && i< groups.size())
			obj = groups.elementAt(i);
		else
			obj = groups.getElement(name);
		if (obj != null) {
			CharactersGroup group = (CharactersGroup)obj;
			groups.removeElement(group, true);
			group.doCommand("deleteMe", null, CommandChecker.defaultChecker);
			return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	public static CharactersGroup makeGroup(MesquiteModule ownerModule, CharacterData data, MesquiteWindow cont, MesquiteString ms) {
 		String n = "Untitled Group";
 		if (data.getFile()!=null)
 			n = data.getFile().getFileElements().getUniqueName(n);
 		GroupDialog d = new GroupDialog(ownerModule.getProject(),MesquiteWindow.windowOfItem(cont), "New Character Group", n, Color.white, null,CharactersGroup.supportsSymbols());
 		d.completeAndShowDialog();
		String name = d.getName();
		ms.setValue(name);
		boolean ok = d.query()==0;
		Color c = d.getColor();
		d.dispose();
		if (!ok)
			return null;
		//String name = MesquiteString.queryString(containerOfModule(), "New character group", "New character group label", "Untitled Group");
		if (StringUtil.blank(name))
			return null;
		CharactersGroup group = new CharactersGroup();
		group.setName(name);
		group.addToFile(data.getFile(), data.getProject(), null);
		if (c!=null) {
			group.setColor(c);
		}
		return group;
	}
	/*.................................................................................................................*/
	public static CharactersGroup makeGroup(MesquiteModule ownerModule, MesquiteFile file, MesquiteWindow cont, MesquiteString ms) {
 		String n = "Untitled Group";
 		if (file==null)
 			return null;
 		n = file.getFileElements().getUniqueName(n);
 		GroupDialog d = new GroupDialog(ownerModule.getProject(),MesquiteWindow.windowOfItem(cont), "New Character Group", n, Color.white, null,CharactersGroup.supportsSymbols());
 		d.completeAndShowDialog();
		String name = d.getName();
		ms.setValue(name);
		boolean ok = d.query()==0;
		Color c = d.getColor();
		d.dispose();
		if (!ok)
			return null;
		//String name = MesquiteString.queryString(containerOfModule(), "New character group", "New character group label", "Untitled Group");
		if (StringUtil.blank(name))
			return null;
		CharactersGroup group = new CharactersGroup();
		group.setName(name);
		group.addToFile(file, file.getProject(), null);
		if (c!=null) {
			group.setColor(c);
		}
		return group;
	}
}


