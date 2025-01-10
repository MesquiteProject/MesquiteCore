/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.tree;

import java.awt.*;
import java.math.*;
import java.util.*;

import mesquite.lib.StringUtil;

/* ======================================================================== */
/** A set of clades.  This doesn't mean a set of actual clades in tree so much as simply a series of named entities.  
Used by modules to tie information such as hypertext links and images to labels at nodes in tree.*/
public class Clades {
	private Vector clades;
	public static int totalCreated = 0;
	
	public Clades(){
		clades = new Vector();
		totalCreated++;
	}
	public Clade findClade(String name){
		if (StringUtil.blank(name))
			return null;
		Enumeration e = clades.elements();
		while (e.hasMoreElements()) {
			Clade c = (Clade)e.nextElement();
			if (name.equalsIgnoreCase(c.getName()))
				return c;
	 	}
		return null;
	}
	public Clade getClade(int ic){
		if (ic<0 || ic>=getNumClades())
			return null;
		return (Clade)clades.elementAt(ic);
	}
	public int getNumClades(){
		return clades.size();
	}
	public Clade addClade(String name) {
		Clade c = findClade(name);
		if (c==null) {
			c = new Clade(name);
			clades.addElement(c);
		}
		return c;
	}
	public void removeClade(String name) {
		Clade clade =findClade(name);
		if (clade!=null) {
			clades.removeElement(clade);
		}
	}
	public void setLink(String name, String link) {
		Clade clade = findClade(name);
		if (clade!=null)
			clade.setLink(link);
	}
	public String getLink(String name) {
		Clade clade = findClade(name);
		if (clade!=null)
			return clade.getLink();
		return null;
	}
	public void setIllustration(String name, Image illustration, String path) {
		Clade clade = findClade(name);
		if (clade!=null)
			clade.setIllustration(illustration, path);
	}
	public String getIllustrationPath(String name) {
		Clade clade = findClade(name);
		if (clade!=null)
			return clade.getIllustrationPath();
		return null;
	}
	public Image getIllustration(String name) {
		Clade clade = findClade(name);
		if (clade!=null)
			return clade.getIllustration();
		return null;
	}
}

