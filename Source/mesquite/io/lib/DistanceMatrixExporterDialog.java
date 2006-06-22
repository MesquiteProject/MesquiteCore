/* Mesquite source code.  Copyright 2001-2006 D. Maddison and W. Maddison. 
Version 1.11, June 2006.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.io.lib;

import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Frame;

import mesquite.io.ExportDistanceMatrix.ExportDistanceMatrix;
import mesquite.lib.ExporterDialog;
import mesquite.lib.MesquiteInteger;

public class DistanceMatrixExporterDialog extends ExporterDialog {
	boolean showHeaders;
	Checkbox writeHeadersCheckBox;
	Choice columnDelimiterDropDown;
	ExportDistanceMatrix exporter;
	
	public DistanceMatrixExporterDialog (ExportDistanceMatrix module, Frame parent, String title, MesquiteInteger buttonPressed) {
		super(module, parent, title, buttonPressed);
		this.exporter = module;
	}
	
	/*.................................................................................................................*/
	public void completeAndShowDialog (boolean dataSelected, boolean taxaSelected) {
		addColumnDelimiterPopUpPanel();
		writeHeadersCheckBox = this.addCheckBox("add headers to columns and rows", exporter.addRowAndColumnHeaders);
		super.completeAndShowDialog(dataSelected, taxaSelected);
	}
	
	public int query(boolean dataSelected, boolean taxaSelected) {
		super.query(dataSelected,taxaSelected);
		exporter.columnDelimiterChoice = columnDelimiterDropDown.getSelectedIndex();
		exporter.addRowAndColumnHeaders = writeHeadersCheckBox.getState();
		return buttonPressed.getValue();
	}

	
	public boolean getRowAndColumnHeadersCheckBox(){
		return writeHeadersCheckBox.getState();
	}
	
	public void addColumnDelimiterPopUpPanel () {
		columnDelimiterDropDown = addPopUpMenu("Table entry delimiters:", "Tab", "Comma", "Space", "NewLine",exporter.lineDelimiterChoice);
	}
	



}
