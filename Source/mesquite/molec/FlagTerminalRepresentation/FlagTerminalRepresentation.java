/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.FlagTerminalRepresentation;
/*~~  */



import java.awt.Checkbox;

import mesquite.categ.lib.CategoricalData;
import mesquite.lib.Bits;
import mesquite.lib.CommandChecker;
import mesquite.lib.Debugg;
import mesquite.lib.DoubleField;
import mesquite.lib.IntegerField;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.molec.lib.SiteFlagger;

/* ======================================================================== */
public class FlagTerminalRepresentation extends SiteFlagger {

	int threshold = MesquiteInteger.unassigned;
	boolean queried = false;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		if (!MesquiteThread.isScripting()) {
			if (!queryOptions())
				return false;
		}
		addMenuItem(null, "Set terminal representation threshold...", makeCommand("setThreshold", this));
		return true;
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "threshold", threshold);  
		return buffer.toString();
	}
	public void processSingleXMLPreference (String tag, String flavor, String content){
		processSingleXMLPreference(tag, null, content);
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("threshold".equalsIgnoreCase(tag))
			threshold = MesquiteInteger.fromString(content);
	}
	/*.................................................................................................................*/
 	 public Snapshot getSnapshot(MesquiteFile file) { 
  	 	Snapshot temp = new Snapshot();
	 	temp.addLine("setThreshold " + threshold); 
 	 	return temp;
 	 }
 	 private boolean queryOptions() {
			int offer;
			if (threshold == MesquiteInteger.unassigned)
				offer = 5;
			else
				offer = threshold;
			int w = MesquiteInteger.queryInteger(containerOfModule(), "Threshold for terminal representation", "Enter a number to indicate the minimum number of sequences represented by data for sites toward the edges to be considered well-represented", offer);
			if (MesquiteInteger.isCombinable(w)) {
				threshold = w;
	   	 		storePreferences();
				return true;
			}
			else
				return false;
			
	 }
	/*.................................................................................................................*/
   	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
  	 	if (checker.compare(this.getClass(), "Sets threshold for terminal representation", "", commandName, "setThreshold")) {
   	 		int w = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (!MesquiteInteger.isCombinable(w))
				w = MesquiteInteger.queryInteger(containerOfModule(), "Threshold for terminal representation", "Enter a number to indicate the minimum number of sequences represented by data for sites toward the edges to be considered well-represented", threshold);
 	 		if (w>=0 && w<=1) {
   	 			threshold = w;
   	 			storePreferences();
   	 			queried = true;
   	 			if (!MesquiteThread.isScripting())
   	 				parametersChanged();
   	 		}
   	 	}
     	 	else
   	 		return  super.doCommand(commandName, arguments, checker);
   	 	return null;
   	 }
 	int[] taxonSequenceStart, taxonSequenceEnd;
 	int getFirstInSequence(CharacterData data, int it) {
		for (int ic=0; ic<data.getNumChars(); ic++)
			if (!data.isInapplicable(ic, it))
				return ic;
		return -1;
	}
	int getLastInSequence(CharacterData data, int it) {
		for (int ic=data.getNumChars()-1; ic>=0; ic--)
			if (!data.isInapplicable(ic, it))
				return ic;
		return -1;
	}
	/*.................................................................................................................*/
	public Bits flagSites(CharacterData data, Bits flags) {
		if (flags == null)
			flags = new Bits(data.getNumChars());
		else {
			if (flags.getSize()< data.getNumChars())
				flags.resetSize(data.getNumChars());
			flags.clearAllBits();
		}
		
		if (taxonSequenceStart == null || taxonSequenceStart.length != data.getNumTaxa()) {
			taxonSequenceStart = new int[data.getNumTaxa()];
			taxonSequenceEnd = new int[data.getNumTaxa()];
		}
		
		for (int it=0; it<data.getNumTaxa(); it++) {
			taxonSequenceStart[it] = getFirstInSequence(data, it);
			taxonSequenceEnd[it] = getLastInSequence(data, it);
		}
		// looking from front
		boolean allRepresented = false;
		for (int ic = 0; ic < data.getNumChars() && !allRepresented; ic++) {
			int numRepresented = 0;
			for (int it = 0; it < data.getNumTaxa(); it++) {
				if (taxonSequenceStart[it] >=0) {
					if (ic>=taxonSequenceStart[it])
						numRepresented ++;
				}
			}
			if (numRepresented<threshold)
				flags.setBit(ic, true);
			allRepresented = (numRepresented == data.getNumTaxa());
		}
		// looking from behind
		allRepresented = false;
		for (int ic = data.getNumChars()-1; ic >=0; ic--) {
			int numRepresented = 0;
			for (int it = 0; it < data.getNumTaxa(); it++) {
				if (taxonSequenceEnd[it] >=0) {
					if (ic<=taxonSequenceEnd[it])
						numRepresented ++;
				}
			}
			if (numRepresented<threshold)
				flags.setBit(ic, true);
			allRepresented = (numRepresented == data.getNumTaxa());
		}
		return flags;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return true;
	}

	/*.................................................................................................................*/
	public boolean showCitation(){
		return false;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Terminal Representation";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Flags sites at alignment edges represented by less than a certain number of sequences." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}


}


