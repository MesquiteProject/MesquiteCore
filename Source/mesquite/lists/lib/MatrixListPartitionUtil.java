/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


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

import java.awt.Color;

import mesquite.lib.CommandChecker;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteString;
import mesquite.lib.Notification;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.CharactersGroup;
import mesquite.lib.characters.CharactersGroupVector;
import mesquite.lib.characters.MatricesGroup;
import mesquite.lib.characters.MatricesGroupVector;
import mesquite.lib.ui.MesquiteWindow;

/* ======================================================================== */
public class MatrixListPartitionUtil {
	//"@MATRIXGROUP

	/*.................................................................................................................*/
	public static Object editGroup(MesquiteModule ownerModule, MesquiteWindow cont, String name, String num) {
		int i = MesquiteInteger.fromString(num);
		ListableVector datas = ownerModule.getProject().getCharacterMatrices();
		MatricesGroupVector groups = (MatricesGroupVector)(ownerModule.getProject()).getFileElement(MatricesGroupVector.class, 0);
		Object obj;
		if (MesquiteInteger.isCombinable(i) && i< groups.size())
			obj = groups.elementAt(i);
		else
			obj = groups.getElement(name);
		if (obj != null) {
			MatricesGroup group = (MatricesGroup)obj;
			GroupDialog d = new GroupDialog(ownerModule.getProject(),MesquiteWindow.windowOfItem(cont), "Edit Matrix Group", group.getName(), group.getColor(), group.getSymbol(), group.supportsSymbols());
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
    	 			datas.notifyListeners(ownerModule, new Notification(MesquiteListener.NAMES_CHANGED)); //TODO: bogus! should notify via specs not data???
					group.notifyListeners(group, new Notification(MesquiteListener.DATA_CHANGED));
			}
		}
		return obj;
	}
	public static boolean deleteGroup(MesquiteModule ownerModule, MesquiteWindow cont, String name, String num) {
		int i = MesquiteInteger.fromString(num);
		MatricesGroupVector groups = (MatricesGroupVector)(ownerModule.getProject()).getFileElement(MatricesGroupVector.class, 0);
		Object obj;
		if (MesquiteInteger.isCombinable(i) && i< groups.size())
			obj = groups.elementAt(i);
		else
			obj = groups.getElement(name);
		if (obj != null) {
			MatricesGroup group = (MatricesGroup)obj;
			groups.removeElement(group, true);
			group.doCommand("deleteMe", null, CommandChecker.defaultChecker);
			return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	public static MatricesGroup makeGroup(MesquiteModule ownerModule, MesquiteWindow cont, MesquiteString ms) {
 		String n = "Untitled Group";
		ListableVector datas = ownerModule.getProject().getCharacterMatrices();
		if (datas.getFile()!=null)
 			n = datas.getFile().getFileElements().getUniqueName(n);
 		GroupDialog d = new GroupDialog(ownerModule.getProject(),MesquiteWindow.windowOfItem(cont), "New Matrix Group", n, Color.white, null,MatricesGroup.supportsSymbols());
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
		MatricesGroup group = new MatricesGroup();
		group.setName(name);
		group.addToFile(datas.getFile(), datas.getProject(), null);
		if (c!=null) {
			group.setColor(c);
		}
		return group;
	}
	/*.................................................................................................................*/
	public static MatricesGroup makeGroup(MesquiteModule ownerModule, MesquiteFile file, MesquiteWindow cont, MesquiteString ms) {
 		String n = "Untitled Group";
 		if (file==null)
 			return null;
 		n = file.getFileElements().getUniqueName(n);
 		GroupDialog d = new GroupDialog(ownerModule.getProject(),MesquiteWindow.windowOfItem(cont), "New Matrix Group", n, Color.white, null,MatricesGroup.supportsSymbols());
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
		MatricesGroup group = new MatricesGroup();
		group.setName(name);
		group.addToFile(file, file.getProject(), null);
		if (c!=null) {
			group.setColor(c);
		}
		return group;
	}}

