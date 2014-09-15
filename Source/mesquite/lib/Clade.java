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
import java.math.*;


/* ======================================================================== */
/** A clade.  This doesn't mean a set of terminal taxa in tree so much as simply a named entity.  
Used by modules to tie information such as hypertext links and images to labels at nodes in tree.*/
public class Clade {
	private String name;
	private Image illustration;
	private String illustrationPath;
	public static int totalCreated = 0;
	private String link;
	//need footnotes etc.
	
	public Clade(String name){
		this.name = name;
		totalCreated++;
	}
	public String getName() {
		return name;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getLink() {
		return link;
	}
	public void setIllustration(Image illustration, String path) {
		this.illustration = illustration;
		this.illustrationPath = path;
	}
	public String getIllustrationPath() {
		return illustrationPath;
	}
	public Image getIllustration() {
		return illustration;
	}
}

