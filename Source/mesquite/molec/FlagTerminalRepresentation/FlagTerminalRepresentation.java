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



import java.awt.Button;
import java.awt.Checkbox;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import mesquite.categ.lib.CategoricalData;
import mesquite.lib.Bits;
import mesquite.lib.CommandChecker;
import mesquite.lib.Debugg;
import mesquite.lib.DoubleField;
import mesquite.lib.ExtensibleDialog;
import mesquite.lib.IntegerField;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteThread;
import mesquite.lib.RadioButtons;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.MatrixFlags;
import mesquite.lib.duties.MatrixFlagger;
import mesquite.lib.duties.MatrixFlaggerForTrimming;
import mesquite.lib.duties.MatrixFlaggerForTrimmingSites;

/* ======================================================================== */
public class FlagTerminalRepresentation extends MatrixFlaggerForTrimmingSites implements ItemListener {

	int absThreshold = MesquiteInteger.unassigned;
	double propThreshold = MesquiteDouble.unassigned;
	boolean useProportion = true;
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
		StringUtil.appendXMLTag(buffer, 2, "absThreshold", absThreshold);  
		StringUtil.appendXMLTag(buffer, 2, "propThreshold", propThreshold);  
		StringUtil.appendXMLTag(buffer, 2, "useProportion", useProportion);  
		return buffer.toString();
	}
	public void processSingleXMLPreference (String tag, String flavor, String content){
		processSingleXMLPreference(tag, null, content);
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("absThreshold".equalsIgnoreCase(tag))
			absThreshold = MesquiteInteger.fromString(content);
		if ("propThreshold".equalsIgnoreCase(tag))
			propThreshold = MesquiteInteger.fromString(content);
		if ("useProportion".equalsIgnoreCase(tag))
			useProportion = MesquiteBoolean.fromTrueFalseString(content);
	}
	/*.................................................................................................................*/
 	 public Snapshot getSnapshot(MesquiteFile file) { 
  	 	Snapshot temp = new Snapshot();
	 	temp.addLine("absThreshold " + absThreshold); 
	 	temp.addLine("propThreshold " + propThreshold); 
	 	temp.addLine("useProportion " + useProportion); 
 	 	return temp;
 	 }
 	 
  	Checkbox usePropCB;
 	DoubleField propField;
 	Checkbox useAbsCB;
 	IntegerField absField;

 	private boolean queryOptions() {
 		MesquiteInteger buttonPressed = new MesquiteInteger(1);
 		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(),  "Threshold for Terminal Representation",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()

 		usePropCB = dialog.addCheckBox("Use proportion of taxa", useProportion);
 		propField = dialog.addDoubleField("    Proportion of taxa defining low representation", propThreshold, 4);
 		useAbsCB = dialog.addCheckBox("Use absolute number of taxa", !useProportion);
 		absField = dialog.addIntegerField("    Number of taxa defining low representation", absThreshold, 4);
 		usePropCB.addItemListener(this);
		useAbsCB.addItemListener(this);
		propField.setEnabled(usePropCB.getState());
		absField.setEnabled(useAbsCB.getState());

 		dialog.addHorizontalLine(1);
 		dialog.addBlankLine();


 		dialog.completeAndShowDialog(true);
 		if (buttonPressed.getValue()==0)  {
 			useProportion = usePropCB.getState();

 			propThreshold = propField.getValue();
 			absThreshold = absField.getValue();

 			queried = true;
			storePreferences();
 		}
 		dialog.dispose();
		return (buttonPressed.getValue()==0);
 	}
	public void itemStateChanged(ItemEvent e) {
		if (e.getItemSelectable() == usePropCB){
			useAbsCB.setState(!usePropCB.getState());
			propField.setEnabled(usePropCB.getState());
			absField.setEnabled(useAbsCB.getState());
		}				
		else if (e.getItemSelectable() == useAbsCB){
			usePropCB.setState(!useAbsCB.getState());
			propField.setEnabled(usePropCB.getState());
			absField.setEnabled(useAbsCB.getState());
		}				
}
	/*.................................................................................................................*/
   	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
  	 	if (checker.compare(this.getClass(), "Sets absolute threshold for terminal representation", "", commandName, "absThreshold")) {
   	 		int w = MesquiteInteger.fromString(parser.getFirstToken(arguments));
 	 		if (MesquiteInteger.isCombinable(w) && w>=0)
   	 			absThreshold = w;
   	 	}
  	 	else if (checker.compare(this.getClass(), "Sets proportion threshold for terminal representation", "", commandName, "propThreshold")) {
   	 		double w = MesquiteDouble.fromString(parser.getFirstToken(arguments));
 	 		if (MesquiteDouble.isCombinable(w) && w>=0 && w<=1)
 	 			propThreshold = w;
   	 	}
  	 	else if (checker.compare(this.getClass(), "Sets whether to use absolute or proportional threshold", "", commandName, "useProportion")) {
 	 		useProportion = MesquiteBoolean.fromTrueFalseString(parser.getFirstToken(arguments));
   	 			queried = true;
   	 	}
    	 	else
   	 		return  super.doCommand(commandName, arguments, checker);
   	 	return null;
   	 }
 	/*.................................................................................................................*/
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
	public MatrixFlags flagMatrix(CharacterData data, MatrixFlags flags) {
			if (flags == null)
				flags = new MatrixFlags(data);
			else 
				flags.reset(data);


			Bits charFlags = flags.getCharacterFlags();
		int numTaxa = data.getNumTaxa();
		if (taxonSequenceStart == null || taxonSequenceStart.length != numTaxa) {
			taxonSequenceStart = new int[data.getNumTaxa()];
			taxonSequenceEnd = new int[data.getNumTaxa()];
		}
		
		for (int it=0; it<numTaxa; it++) {
			taxonSequenceStart[it] = getFirstInSequence(data, it);
			taxonSequenceEnd[it] = getLastInSequence(data, it);
		}
		// looking from front
		boolean allRepresented = false;
		for (int ic = 0; ic < data.getNumChars() && !allRepresented; ic++) {
			int numRepresented = 0;
			for (int it = 0; it < numTaxa; it++) {
				if (taxonSequenceStart[it] >=0) {
					if (ic>=taxonSequenceStart[it])
						numRepresented ++;
				}
			}
			if (!useProportion && numRepresented<absThreshold)
				charFlags.setBit(ic, true);
			else if (useProportion && numRepresented*1.0/numTaxa<propThreshold)
				charFlags.setBit(ic, true);
			allRepresented = (numRepresented == data.getNumTaxa());
		}
		// looking from behind
		allRepresented = false;
		for (int ic = data.getNumChars()-1; ic >=0; ic--) {
			int numRepresented = 0;
			for (int it = 0; it < numTaxa; it++) {
				if (taxonSequenceEnd[it] >=0) {
					if (ic<=taxonSequenceEnd[it])
						numRepresented ++;
				}
			}
			if (!useProportion && numRepresented<absThreshold)
				charFlags.setBit(ic, true);
			else if (useProportion && numRepresented*1.0/numTaxa<propThreshold)
				charFlags.setBit(ic, true);
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
		return "Flags sites at alignment edges represented by less than a certain number or proportion of sequences." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}


}


