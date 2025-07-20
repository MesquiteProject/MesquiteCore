/* Mesquite (package mesquite.io).  Copyright 2000 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.io.InterpretFlippedFastaDNA;
/*~~  */

import java.awt.Checkbox;

import mesquite.categ.lib.CategoricalData;
import mesquite.categ.lib.CategoricalState;
import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.DNAState;
import mesquite.io.InterpretFastaDNA.InterpretFastaDNA;
import mesquite.io.lib.InterpretFasta;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.CharactersManager;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.RadioButtons;


/* ============  a file interpreter for DNA/RNA  Fasta files ============*/

public class InterpretFlippedFastaDNA extends InterpretFastaDNA {
	//Debugg.println: Rewording incomplete, and options should be different, e.g. should allow longest sequence
	protected int queryOptionsDuplicate() {
		String helpString = "If you choose Don't Add, then any incoming sequence with the same name as an existing sequence will be ignored. ";
		helpString += "If you choose Replace Data, then the incoming sequence will replace any existing sequence for that locus.  ";
		helpString += "If you choose Replace If Empty, Otherwise Add, then the incoming sequence will be put into the existing spot for that locus ONLY if that locus has no previous data there; if there is already a sequence there, then the incoming sequence will be added as a new locus. ";
		helpString += "If you choose Replace If Empty, Otherwise Ignore, then the incoming sequence will be put into the existing spot for that locus ONLY if that locus has no previous data there; if there is already a sequence there, then the incoming sequence with the same name as an existing locus will be ignored. ";
		helpString+= "If you choose Add As New Locus then all incoming sequences will be added as new loci, even if there already exist taxa in the matrix with identical names.";
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog id = new ExtensibleDialog(containerOfModule(), "Incoming loci match existing loci",buttonPressed);
		id.addLargeTextLabel("Some of the loci already found have the same name as incoming loci.  Please choose how incoming sequences with the same name as existing loci will be treated.");
		if (StringUtil.blank(helpString) && id.isInWizard())
			helpString = "<h3>" + StringUtil.protectForXML("Incoming loci match existing loci") + "</h3>Please choose.";
		id.appendToHelpString(helpString);



		RadioButtons radio = id.addRadioButtons(new String[] {"Ignore", "Replace Data","Replace If Empty, Otherwise Add","Replace If Empty, Otherwise Ignore","Add As New Loci"},treatmentOfIncomingDuplicates);

		Checkbox selectIncomingBox = id.addCheckBox("select imported sequences", selectIncoming);
		
		id.completeAndShowDialog(true);

		int value = -1;
		if (buttonPressed.getValue()==0)  {
			value = radio.getValue();
			treatmentOfIncomingDuplicates = value;
			selectIncoming = selectIncomingBox.getState();
		}
		id.dispose();
		return value;
	}
	
	/*.................................................................................................................*/
	 public boolean canExportEver() {
	return false;
	 }
	 /*.................................................................................................................*/
		public boolean canExportProject(MesquiteProject project) {  
			 return false;  //
		}
	/*.................................................................................................................*/
		public boolean canExportData(Class dataClass) {  
			return false;
		}
		/*.................................................................................................................*/
	/*.................................................................................................................*/
	 public boolean getUserChooseable() {
	return false;
	 }
/*.................................................................................................................*/
    	 public String getName() {
		return "Taxonwise FASTA (DNA/RNA)";
   	 }
/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Imports and exports taxonwise FASTA files; for use by Combine Taxonwise Fastas." ;
   	 }
   	 
}
	

