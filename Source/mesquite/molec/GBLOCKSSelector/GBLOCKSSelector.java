/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.GBLOCKSSelector;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.molec.lib.*;

/* ======================================================================== */
public class GBLOCKSSelector extends MesquiteInit {

/*This module has been replaced by a new system. However, this remains merely to transfer the old preferences to  FlagByGLBOCKS */

	public static boolean prefsRead = false;   
	public static double IS = MesquiteDouble.inapplicable;   // fraction of identical residues that is upper boundary for non-conserved sequences
	public static double FS = MesquiteDouble.inapplicable;  // fraction of identical residues that is upper boundary for conserved sequences
	public static int CP = MesquiteInteger.inapplicable;  //block size limit for non-conserved blocks
	public static int BL = MesquiteInteger.inapplicable;  //  small region block size limit 
	public static double gapThreshold = MesquiteDouble.inapplicable;   // the fraction of gaps allowed at a site
	public static boolean chooseAmbiguousSites;
	public static boolean countWithinApplicable;   // count fractions of identical residues only within those taxa without gaps at a site
	public static boolean chooseAmbiguousSitesRead = false;
	public static boolean countWithinApplicableRead = false;   
	public static GBLOCKSSelector instanceOfMe;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		instanceOfMe = this;  //so that new version can ask this to delete its old preferences.
		return true;
	}
	/*.................................................................................................................*/
	public void prefsCaptured() {
		storePreferences();
		prefsRead = false;
	}
	/*.................................................................................................................*
		/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		prefsRead = true;
		if ("IS".equalsIgnoreCase(tag)) 
			IS = MesquiteDouble.fromString(content);
		if ("FS".equalsIgnoreCase(tag)) 
			FS = MesquiteDouble.fromString(content);
		if ("CP".equalsIgnoreCase(tag)) 
			CP = MesquiteInteger.fromString(content);
		if ("BL".equalsIgnoreCase(tag)) 
			BL = MesquiteInteger.fromString(content);
		if ("gapThreshold".equalsIgnoreCase(tag)) 
			gapThreshold = MesquiteDouble.fromString(content);

		if ("chooseAmbiguousSites".equalsIgnoreCase(tag)) {
			chooseAmbiguousSites = MesquiteBoolean.fromTrueFalseString(content);
			chooseAmbiguousSitesRead= true;
		}
		if ("countWithinApplicable".equalsIgnoreCase(tag)) {
			countWithinApplicable = MesquiteBoolean.fromTrueFalseString(content);
			countWithinApplicableRead=true;
		}
	}

	public String preparePreferencesForXML () {
		
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "PrefsCapturedByNewerVersion", "");  

		return buffer.toString();
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Old GBLOCKS Selector preference capturer";
	}
	/*.................................................................................................................*/


}

