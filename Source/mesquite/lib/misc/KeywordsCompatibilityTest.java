/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib.misc;

import mesquite.lib.CompatibilityTest;
import mesquite.lib.EmployerEmployee;
import mesquite.lib.MesquiteProject;
import mesquite.lib.StringArray;

/* ======================================================================== */
/** A compatibility test for a condition that is a list of keywords that must be matched. Condition in hiring can be either a single String, or a String[] of required keywords.
 * See use by TreesFromSelMatrices and TreesFromSelMatricesParallel.*/
public class KeywordsCompatibilityTest extends CompatibilityTest {
	String[] keywords;
	public KeywordsCompatibilityTest(String[] words){
		keywords = words;
	}
	public boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer){
		if (obj == null)  
			return true;
		if (keywords == null || keywords.length == 0)
			return true;
		String[] requestedWords = null;
		if (obj instanceof KeywordsCompatibilityTest) 
			requestedWords = ((KeywordsCompatibilityTest)obj).keywords;
		if (obj instanceof String[])
			requestedWords = (String[])obj;
		if (obj instanceof String)
			requestedWords = new String[]{(String)obj};
		if (requestedWords != null) {
			for (int i = 0; i<requestedWords.length; i++)
				if (StringArray.indexOfIgnoreCase(keywords, requestedWords[i])<0){
					return false;
				}
			return true;
		}
		return false;

	}
}


