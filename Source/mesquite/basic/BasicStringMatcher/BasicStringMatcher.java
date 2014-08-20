/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.basic.BasicStringMatcher;

import mesquite.lib.*;
import mesquite.lib.duties.*;

public class BasicStringMatcher extends StringMatcher {
	 

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	public boolean stringsMatch(String s1, String s2) {
		MesquiteMessage.warnProgrammer("WARNING: Within BasicStringMatcher.stringsMatch(): shouldn't be here!!!!");
		return false;
	}

	public Class getDutyClass() {
		return StringMatcher.class;
	}

	public String getName() {
		return "Default String Matcher";
	}

	public String getExplanation() {
		return "Uses the default string-matching mechanism of the module that is doing the calling.";
	}

	public boolean useDefaultMatching() {
		return true;
	}

}
