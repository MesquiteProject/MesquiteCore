/* Mesquite (package mesquite.io).  Copyright 2000 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.io.ExportCharStateInfo;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;


/* ============  an exporter for POY files ============*/

public class ExportCharStateInfo extends FileInterpreterI {
	String[][] delimiters;
	int A = 1;
	int B = 2;
	int C = 3;
	int D = 4;
	int E = 5;
	int F = 6;
	int G = 7;
	int H = 8;
	int I = 9;
	int J = 10;
	int K = 11;
	int L = 12;
	int M = 13;
	int P = 14;
	int Q = 15;
	int R = 16;
	int S = 17;
	int T = 18;
	int U = 19;
	int V = 20;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		delimiters = new String[3][];
		delimiters[0] = new String[]{"html", "</strong>", " (", " ", " (", ") ", "<li>", "</li>", "<ul>", "J", "</ul>", "<li>", "</li>", "<ul>", "\n", "</ul>", "<li><strong>", "</li>", "<ul>", "</ul>", ") ", "W", "X"};
		delimiters[1] = new String[]{"txt", " ", " (", " ", " (", ") ", "\n\t\t", "\n", "", "J", "", "\t", "\n", "", "\n", "", "", "\n", "", "", ") ", " ", " "};
		delimiters[2] = new String[]{"txt", "A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X"};
		return true;  //make this depend on taxa reader being found?)
	}

	/*.................................................................................................................*/
	public String preferredDataFileExtension() {
		return "";
	}
	/*.................................................................................................................*/
	public boolean canExportEver() { 
		return true;  //
	}
	/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		return (project.getNumberCharMatrices( CategoricalState.class) - project.getNumberCharMatrices( MolecularState.class) > 0) ;
	}

	/*.................................................................................................................*/
	public boolean canExportData(Class dataClass) {  
		return (dataClass==CategoricalState.class);
	}
	/*.................................................................................................................*/
	public boolean canImport() {  
		return false;
	}

	/*.................................................................................................................*/
	public void readFile(MesquiteProject mf, MesquiteFile file, String arguments) {
	}


	/* ============================  exporting ============================*/
	int style = 0;
	/*.................................................................................................................*/

	public boolean getExportOptions(boolean dataSelected){
		setLineDelimiter(UNIXDELIMITER);
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExporterDialog exportDialog = new ExporterDialog(this,containerOfModule(), "Export Character/State Info Options", buttonPressed);
		Choice choice = exportDialog.addPopUpMenu("Output format", new String[]{"Web Page (HTML)", "Plain Text"}, 0);
		exportDialog.completeAndShowDialog(dataSelected, false);

		boolean ok = (exportDialog.query(dataSelected, false)==0);
		style = choice.getSelectedIndex();
		exportDialog.dispose();
		return ok;
	}	

	NameReference notesNameRef = NameReference.getNameReference("notes");

	/*.................................................................................................................*/
	public boolean exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export
		Arguments args = new Arguments(new Parser(arguments), true);
		boolean usePrevious = args.parameterExists("usePrevious");
		CategoricalData data = (CategoricalData)getProject().chooseData(containerOfModule(), file, null, CategoricalState.class, "Select data to export");
		if (data ==null) {
			showLogWindow(true);
			logln("WARNING: No suitable data available for export to a file of format \"" + getName() + "\".  The file will not be written.\n");
			return false;
		}

		if (!MesquiteThread.isScripting() && !usePrevious)
			if (!getExportOptions(data.anySelected()))
				return false;


		int numChars = data.getNumChars();
		StringBuffer outputBuffer = new StringBuffer(20* numChars);
		String allChars = "";
		for (int ic = 0; ic<numChars; ic++) {
			if (!writeOnlySelectedData || (data.getSelected(ic))){
				String thisChar = "";
				if (data.characterHasName(ic)) {
					thisChar += data.getCharacterName(ic) + " " + delimiters[style][A];
				}
				if (!StringUtil.blank(data.getAnnotation(ic)))
					thisChar +=   delimiters[style][B]+ data.getAnnotation(ic) + delimiters[style][V];
				AttachedNotesVector hL = (AttachedNotesVector)data.getAssociatedObject(notesNameRef, ic);
				if (hL != null){
					String theseAnnotations = fromAnnotations(hL, style);
					
					if (!StringUtil.blank(theseAnnotations))
						thisChar += delimiters[style][H] +delimiters[style][H] + theseAnnotations + delimiters[style][J] + delimiters[style][J];
				}
				boolean firstState = true;
				String allStates = "";
				for (int ik =0; ik<= data.maxStateWithName(ic); ik++){
					String thisState = "";
					if (data.hasStateName(ic, ik)){
						thisState += data.getStateName(ic, ik) + " " + delimiters[style][C];
					}
					if (data.hasStateFootnote(ic, ik)){
						thisState += delimiters[style][D] + data.getStateNote(ic, ik) + delimiters[style][E];
					}
					if (data.hasStateAnnotations(ic, ik)){
						String theseAnnotations = fromAnnotations(data.getStateAnnotationsVector(ic, ik), style);
						
						if (!StringUtil.blank(theseAnnotations))
							thisState += delimiters[style][H] + theseAnnotations + delimiters[style][J];
					}
					if (!StringUtil.blank(thisState)){
						thisState =  delimiters[style][K] + Integer.toString(ik) + " " + thisState + delimiters[style][L];
						if (firstState)
							thisState = delimiters[style][M] + thisState;
						firstState = false;
						allStates += " " + thisState;
					}

				}
				if (!StringUtil.blank(allStates)){
					thisChar += delimiters[style][P]+  allStates + delimiters[style][Q];
				}
				if (!StringUtil.blank(thisChar)){
					allChars +=  delimiters[style][R]+ Integer.toString(ic+1) + ". " + thisChar + delimiters[style][S];
					
				}
			}
		}
		if (!StringUtil.blank(allChars))
			outputBuffer.append(delimiters[style][T] + allChars + delimiters[style][U]);
		else
			return false;
		outputBuffer.append(getLineEnding()+getLineEnding());

		saveExportedFileWithExtension(outputBuffer, arguments, delimiters[style][0]);
		return true;
	}

	String fromAnnotations(AttachedNotesVector v, int style){
		String theseAnnotations = "";
		for (int ink = 0; ink < v.getNumNotes(); ink++){
			String thisAnnotation = "";
			AttachedNote note = v.getAttachedNote(ink);
			if (!StringUtil.blank(note.getComment()))
				thisAnnotation += note.getComment() + " ";
			if (!StringUtil.blank(note.getReference()))
				thisAnnotation += delimiters[style][D] + note.getReference() + delimiters[style][E] ;
			if (!StringUtil.blank(thisAnnotation))
				theseAnnotations += delimiters[style][F] + thisAnnotation + delimiters[style][G];
		}
		return theseAnnotations;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}
/*.................................................................................................................*/
	public String getName() {
		return "Character/State Information";
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Exports character and state information for categorical matrix." ;
	}
	/*.................................................................................................................*/


}


