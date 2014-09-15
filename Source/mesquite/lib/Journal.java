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
import mesquite.lib.duties.*;

/* ��������������������������� commands ������������������������������� */
/* includes commands,  buttons, miniscrolls

/** 
*/
public class Journal {
	private String buttons  = "";
	private String[] journalTarget = null;
	private String targetArgs = "";
	static Journal j;
	
	public Journal() {
	}
	public void initialize(){
		buttons  = "";
		journalTarget = null;
		targetArgs = "";
	}
	public void  setJournalTarget(MenuItem item){
		if (item==null)
			return;
		String s = item.getLabel();
		MenuContainer cont = item.getParent();
		int count = 1;
		while (cont!= null && !(cont instanceof MenuBar) && !(cont instanceof MesquitePopup)&& cont instanceof MenuComponent) {
			if (cont instanceof MenuItem)
				count++;
			cont =  ((MenuComponent)cont).getParent();
		}
		journalTarget = new String[count];
		cont =item.getParent();
		journalTarget[0] = item.getLabel();
		count = 1;
		while (cont!= null && !(cont instanceof MenuBar) && !(cont instanceof MesquitePopup)&& cont instanceof MenuComponent) {
			if (cont instanceof MenuItem)
				journalTarget[count++]= ((MenuItem)cont).getLabel();
			cont =  ((MenuComponent)cont).getParent();
		}
	}
	public void  setJournalTarget(MenuItem item, String arg){
		setJournalTarget(item);
		targetArgs = arg;
	}
	public void  addToJournal(String s){
			buttons += s + '\n';
	}
	public String toString(){
		return "Target: " + StringArray.toString(journalTarget) + '\n' + "Args: " + targetArgs + '\n' + buttons;
	}
	public static void setStandardJournal(Journal jo){
		j = jo;
	}
	public void runJournal(MesquiteWindow window){
		//find menu item
		//set journal in dialog default
		//choose it, sending args
	}
	/*public static void startJournalling(){
		journalingOn = true;
	}
	public static void  addToJournal(MenuItem item){
		if (journalingOn)
			journal.setJournalTarget(item);
	}
	public static void  addToJournal(String s){
		if (journalingOn)
			journal.addStringToJournal(s);
	}
	*/
	public void stopJournalling(){
		MesquiteMessage.warnProgrammer("====JOURNAL====\n" + this);
		j = null;
	}
	
}

