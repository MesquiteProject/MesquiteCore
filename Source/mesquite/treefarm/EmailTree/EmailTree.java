/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison. 
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.EmailTree;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import java.net.*;

/** ======================================================================== */

public class EmailTree extends TreeUtility {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;  
 	}
 	
	public  void useTree(Tree tree) {
		TextTree tt = new TextTree(tree);
		StringBuffer buff = new StringBuffer(50);
		tt.drawTreeAsText(tree, buff);
		String origTree = buff.toString();
		buff.insert(0, "\nTree: " + tree.getName() + "\n\n\n");
		replaceSpacesByTabs(buff);
		String treeString = URLEncoder.encode(buff.toString());
		buff.setLength(0);
		buff.append(treeString);
		replaceURLTabsByURLSpaces(buff);
		String file = MesquiteModule.prefsDirectory + MesquiteFile.fileSeparator + "emailTree.html";
		buff.insert(0, "<html><body><h3>Mesquite tree e-mailer</h3><a href= \"mailto:Recipient?Subject=tree&Body=");
		buff.append("\">E-mail tree</a><pre>"+ origTree +"</pre></body></html>" );
		MesquiteFile.putFileContents(file, buff.toString(), true);
		
		showWebPage(file, false);
	}
	
	void replaceSpacesByTabs(StringBuffer buff){
		String ns = buff.toString().replace(' ', '\t');
		buff.setLength(0);
		buff.append(ns);
	}
	void replaceURLTabsByURLSpaces(StringBuffer buff){
	 //09 by 20
	 	String s = buff.toString();
	 	int i = 0;
	 	int loc;
	 	while ((loc = s.indexOf("%09", i))>=0){
	 		buff.setCharAt(loc+1, '2');
	 		buff.setCharAt(loc+2, '0');
	 		i = loc+3;
	 	}
	}
	
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "E-mail Tree";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Calls mailto: to email a text image of the tree.";
   	 }
   	 
}


