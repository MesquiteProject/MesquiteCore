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
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;



/*===============================================*/
/** a collection of fields for doubles */
public class DoubleSqMatrixFields  {
	ExtensibleDialog dialog;
	SingleLineTextField [][] textFields;
	double[][] matrix;
	String[] labels;
	boolean onlyUpperRight;
	boolean editDiagonal;
	boolean valid=false;
	boolean lastValueEditable = false;
	SingleLineTextField lastField = null;
	/*.................................................................................................................*/
	public DoubleSqMatrixFields (ExtensibleDialog dialog, double[][] matrix, String[] labels, boolean onlyUpperRight, boolean editDiagonal, int fieldLength) {
		super();
		this.dialog = dialog;
		this.labels =labels;
		this.matrix = matrix;
		initSqMatrixFields(matrix,onlyUpperRight,editDiagonal, fieldLength);
	}
	/*.................................................................................................................*/
	public DoubleSqMatrixFields (ExtensibleDialog dialog, int matrixSize, boolean onlyUpperRight, int fieldLength) {
		super();
		this.dialog = dialog;
		matrix = new double[matrixSize][matrixSize];
		for (int i = 0; i<matrixSize; i++)
			for (int j = 0; j<matrixSize; j++) 
				matrix[i][j] = MesquiteDouble.unassigned;
		initSqMatrixFields(matrix,onlyUpperRight,editDiagonal, fieldLength);
	}
	/*.................................................................................................................*/
	public void initSqMatrixFields (double[][]  matrix, boolean onlyUpperRight, boolean editDiagonal, int fieldLength) {
		this.onlyUpperRight = onlyUpperRight;
		this.editDiagonal = editDiagonal;
		textFields = new SingleLineTextField [matrix.length][matrix.length]; //protect against non-square or null matrices!
		
		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridwidth=1;
		constraints.gridheight=1;
		constraints.fill=GridBagConstraints.BOTH;
		int adj = 0;
		if (!onlyUpperRight)
			adj = 1;
	        
		Panel newPanel = new Panel();
		newPanel.setLayout(gridBag);
		gridBag.setConstraints(newPanel,constraints);
		constraints.gridy = 1;
		constraints.gridx=3;
		newPanel.add(new Label("To:"),constraints);
		
		constraints.gridy = 2;
		if (!onlyUpperRight) {
			constraints.gridx=3;
			if (labels!=null)
				newPanel.add(new Label("" +labels[0]+ "  "),constraints);
		}
			
		for (int i = 1; i<matrix.length; i++) {
			constraints.gridx=i+2+adj;
			if (labels!=null)
				newPanel.add(new Label("" +labels[i]+ "  "),constraints);
		}
		
		constraints.gridx=1;
		constraints.gridy = 3;
		newPanel.add(new Label("From: "),constraints);
		
		int numRows = matrix.length;
		if (onlyUpperRight)
			numRows --;

		for (int i = 0; i<numRows; i++)  { //cycle through rows 
			constraints.gridy=i+3;
			constraints.gridx=2;
			if (labels!=null)
				newPanel.add(new Label("  " + labels[i] + "  "),constraints);
			for (int j = 0; j<matrix[0].length; j++) {
				constraints.gridx=j+2+adj;
				if (j>i || i==j && editDiagonal || i>j && !onlyUpperRight) {   
					if (matrix[i][j]==MesquiteDouble.unassigned)
						textFields[i][j] = new SingleLineTextField("",fieldLength);
					else
						textFields[i][j] = new SingleLineTextField(MesquiteDouble.toString(matrix[i][j]),fieldLength);
					textFields[i][j].setBackground(Color.white);
					lastField = textFields[i][j];
					newPanel.add(textFields[i][j],constraints);					
				}
			}
		}
		if (!lastValueEditable && lastField!=null) {
			lastField.setEditable(false);
			lastField.setBackground(dialog.getBackground());
		}
		dialog.addNewDialogPanel(newPanel);
	}
	/*.................................................................................................................*/
	public boolean getValidDouble () {
		return valid;
	}
	/*.................................................................................................................*/
	public void setLastValueEditable (boolean editable) {
		lastValueEditable = editable;
		if (lastField!=null) {
			if (lastValueEditable) {
				lastField.setEditable(true);
				lastField.setBackground(Color.white);
		}
			else {
				lastField.setEditable(false);
				lastField.setBackground(dialog.getBackground());
			}
		}
	}
	/*.................................................................................................................*/
	public boolean getLastValueEditable () {
		return lastValueEditable;
	}
	/*.................................................................................................................*/
	public double getValue (int row, int column) {
		if (row>=textFields.length)
			return MesquiteDouble.unassigned;
		if (column>=textFields[0].length)
			return MesquiteDouble.unassigned;
		SingleLineTextField SLTF = textFields[row][column];
		if (SLTF==null)
			return MesquiteDouble.unassigned;
		String s = SLTF.getText();
		double value = MesquiteDouble.fromString(s);
		valid=true;
		if (!MesquiteDouble.isCombinable(value)) {
			valid = false;
			value=matrix[row][column];
		}
		return value;
	}
	
	/*.................................................................................................................*/
	public Double2DArray getDouble2DArray () {
		Double2DArray newArray = new Double2DArray(matrix);
		int numRows = matrix.length;
		for (int row= 0; row<numRows; row++)  { //cycle through rows 
			for (int column= 0; column<matrix[0].length; column++) {
				newArray.setValue(row, column, getValue(row,column));
			}
		}
		return  newArray;
	}

}

