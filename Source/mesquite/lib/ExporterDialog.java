/* Mesquite source code.  Copyright 2001 and onward, D. Maddison and W. Maddison. 


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
import mesquite.lib.duties.*;



/*===============================================*/
/** Export Options dialog box class*/
public class ExporterDialog extends ExtensibleDialog {
	FileInterpreter fileInterpreter;
	static final String exportString = "Export";
	static final String cancelString = "Cancel";
	Choice exportDelimiter;
	Checkbox writeOnlySelectedDataCheckBox;
	Checkbox writeOnlySelectedTaxaCheckBox;
	boolean suppressLineEndQuery = false;
	public ExporterDialog (FileInterpreterI fileInterpreter, MesquiteWindow parent, String title, MesquiteInteger buttonPressed) {
		this((FileInterpreter)fileInterpreter, parent, title, buttonPressed);
	}
	public ExporterDialog (FileInterpreter fileInterpreter, MesquiteWindow parent, String title, MesquiteInteger buttonPressed) {
		super(parent, title, buttonPressed);
		this.fileInterpreter = fileInterpreter;
		setDefaultButton("Export");
	}
	/*.................................................................................................................*/
	public void addLineEndPopUpPanel () {
		exportDelimiter = addPopUpMenu("End of line character:", "Current System Default","MacOS (CR)","Windows (CR+LF)","UNIX (LF)",fileInterpreter.lineDelimiter);
	}
	/*.................................................................................................................*/
	public void setSuppressLineEndQuery(boolean su){
		suppressLineEndQuery = su;
	}
	
	/*.................................................................................................................*/
	public void completeAndShowDialog (boolean dataSelected, boolean taxaSelected) {
		fileInterpreter.writeOnlySelectedTaxa = false;
		fileInterpreter.writeOnlySelectedData = false;
		if (dataSelected) 
			 writeOnlySelectedDataCheckBox = this.addCheckBox("write only selected data", fileInterpreter.writeOnlySelectedData);
		else
			writeOnlySelectedDataCheckBox = new Checkbox();
			
		if (taxaSelected) 
			 writeOnlySelectedTaxaCheckBox = this.addCheckBox("write only selected taxa", fileInterpreter.writeOnlySelectedTaxa);
		else
			writeOnlySelectedTaxaCheckBox = new Checkbox();
		if (!suppressLineEndQuery)
			addLineEndPopUpPanel();
		addPrimaryButtonRow(exportString, cancelString);
		prepareAndDisplayDialog();
	}
	/*.................................................................................................................*/
	public void completeAndShowDialog () {
		if (!suppressLineEndQuery)
			addLineEndPopUpPanel();
		addPrimaryButtonRow(exportString, cancelString);
		prepareAndDisplayDialog();
	}
	/*.................................................................................................................*/
	public void buttonHit(String buttonLabel, Button button) {
		super.buttonHit(buttonLabel, button);
		if (buttonLabel.equalsIgnoreCase(exportString) && exportDelimiter != null) {
			fileInterpreter.lineDelimiter = exportDelimiter.getSelectedIndex();
		}
	}
	/*.................................................................................................................*/
	public static int query(FileInterpreterI fileInterpreter, MesquiteWindow parent, String title) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExporterDialog exportDialog = new ExporterDialog(fileInterpreter, parent, title,buttonPressed);

		exportDialog.completeAndShowDialog();
		exportDialog.dispose();
		return buttonPressed.getValue();
	}
	/*.................................................................................................................*/
	public int query() {
		return buttonPressed.getValue();
	}
	/*.................................................................................................................*/
	public int query(boolean dataSelected, boolean taxaSelected) {
		if (dataSelected) 
			fileInterpreter.writeOnlySelectedData = writeOnlySelectedDataCheckBox.getState();
		if (taxaSelected) 
			fileInterpreter.writeOnlySelectedTaxa = writeOnlySelectedTaxaCheckBox.getState();
		
		return buttonPressed.getValue();
	}
}

