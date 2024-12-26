/* Mesquite source code (Genesis package).  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.genesis.lib;

import mesquite.lib.*;
import java.awt.*;
import java.awt.event.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.cont.lib.*;


/** A class that provides a General Time Reversible rate matrix. */
/* ======================================================================== */
public abstract class StateFreqUserModel extends StateFreqModel implements ActionListener {
	boolean hasDefaultValues = true;
	double[] stateFrequenciesTemp;
	boolean values = false;
	DoubleField[] stateFreqField;
	String errorMessage="";
	
//	DoubleSqMatrixFields rateMatrixField;

	public StateFreqUserModel (CompositProbCategModel probabilityModel, int numstates, double[] stateFrequencies) {
		super(probabilityModel, numstates);
		stateFrequenciesTemp = new double[getNumStates()];
		for (int i=0; i<getNumStates(); i++) {
			if (stateFrequencies==null)
				setStateFreq(i,MesquiteDouble.unassigned);
			else
				setStateFreq(i,stateFrequencies[i]);
		}
 	}

 	/*.................................................................................................................*/
	/** Sets the state frequencies. */
	public void setStateFrequencies (){
	}
 	/*.................................................................................................................*/
	/** Sets the state frequencies. */
	public void setStateFrequencies (double[] freq){
		if (freq.length>= getNumStates())
			for (int i=0; i<getNumStates(); i++)  {
				setStateFreq(i,freq[i]);
				stateFreqField[i].setValue(freq[i]);
			}
	}
	/*.................................................................................................................*/
	public boolean isFullySpecified(){
		for (int i=0; i<getNumStates(); i++) 
			if (getStateFreq(i)== MesquiteDouble.unassigned)
				return false;
		return true;
	}
	/*.................................................................................................................*/
	public abstract Class dataClass();
  	/*.................................................................................................................*/
	public void addOptions(ExtensibleDialog dialog) {
		stateFreqField = new DoubleField[getNumStates()];
		for (int i=0; i<getNumStates(); i++) {
			stateFreqField[i] = dialog.addDoubleField(""+CategoricalData.getDefaultStateSymbol(dataClass(),i)+":",getStateFreq(i), 10);
		}
		Panel moreButtons = new Panel();
		dialog.addAListenedButton("Equal",moreButtons,this);
		dialog.addAListenedButton("Norm+-",moreButtons,this);
		dialog.addAListenedButton("Norm*/",moreButtons,this);
		dialog.addNewDialogPanel(moreButtons);			
	}
	/*.................................................................................................................*/
	public void initAvailableStates() {
		availableStates = new int[numStates];
		for (int i=0; i<getNumStates(); i++)
			availableStates[i]=i;
	}
 	/*.................................................................................................................*/
	public boolean recoverOptions() {
		for (int i = 0; i<getNumStates(); i++)
			stateFrequenciesTemp[i] =stateFreqField[i].getValue();
		return checkNormality(stateFrequenciesTemp);
	}
	/*.................................................................................................................*/
	public boolean checkOptions() {
		for (int i = 0; i<getNumStates(); i++)
			stateFrequenciesTemp[i] =stateFreqField[i].getValue();
		return checkNormality(stateFrequenciesTemp) && checkValueRange(stateFrequenciesTemp);
	}
	/*.................................................................................................................*/
	public String checkOptionsReport() {
		return errorMessage; 
	}
 	/*.................................................................................................................*/
	public void setOptions() {
		setStateFrequencies(stateFrequenciesTemp);
		notifyListeners(this, new Notification(MesquiteListener.UNKNOWN));
	}
 	/*.................................................................................................................*/
	public boolean checkNormality(double[] frequencies) {
		double total = 0.0;
		for (int i = 0; i<frequencies.length; i++)
			total += frequencies[i];
		double tolerance = 0.001;
		if (((total-1.0) > tolerance)|| ((1.0- total) > tolerance)) {
			errorMessage = "Frequencies do not add up to 1.0!";
			return false;
		}
		return true;
	}
 	/*.................................................................................................................*/
	public boolean checkValueRange(double[] frequencies) {
		for (int i = 0; i<frequencies.length; i++) {
			if (frequencies[i] < 0.0) {
				errorMessage = "All frequencies must have positive values";
				return false;
			}
			else if (frequencies[i]>1.0) {
				errorMessage = "All frequencies must be 1.0 or less";
				return false;
			}
		}
		return true;
	}
	/*.................................................................................................................*/
	/** returns name of model class (e.g. "stepmatrix")*/
	public String getNEXUSClassName() {
		return "StateFrequencyUserSpecified";
	}
 	/*.................................................................................................................*/
	public String toStateFreqString() {
		String s = "";		
		boolean firstElement = true;
		for (int i = 0; i<getNumStates(); i++) {
			if (!firstElement)
				s+= " ";
			s += MesquiteDouble.toString(getStateFreq(i));
			firstElement = false;
		}
		return s;
	}
 	/*.................................................................................................................*/
	public String getNexusSpecification() {
		String s = "StateFreq = (" + toStateFreqString() +  ") ";
		return s;
	}
 	/*.................................................................................................................*/
	public void fromString (String description, MesquiteInteger stringPos, int format) {
		hasDefaultValues = false;
		ParseUtil.getToken(description, stringPos);   // RMatrix
		ParseUtil.getToken(description, stringPos);  // =
		ParseUtil.getToken(description, stringPos);  // (
		for (int i = 0; i<getNumStates(); i++) {
				String s = ParseUtil.getToken(description, stringPos);
				if (s.equalsIgnoreCase(")") || StringUtil.blank(s)) 
					return;
				setStateFreq(i,MesquiteDouble.fromString(s));
			}
		checkNormality(getStateFrequencies());
	}
 	/*.................................................................................................................*/
	final static boolean allowArchives = true;
 
	/*.................................................................................................................*/
	 public  void actionPerformed(ActionEvent e) {
	 	if   ("Equal".equals(e.getActionCommand())) {
			double probEach =1.0/getNumStates();
			for (int i=0; i<getNumStates(); i++) 
				stateFrequenciesTemp[i] = probEach;
	 		setOptions();
	 		//setStateFrequencies(stateFrequenciesTemp);
	 	}
	 	else if   ("Norm+-".equals(e.getActionCommand())) {
			double total =0;
			for (int i=0; i<getNumStates(); i++) 
				total += stateFreqField[i].getValue();
			double difference = (total - 1.0)/getNumStates();
			for (int i=0; i<getNumStates(); i++) 
				stateFrequenciesTemp[i] = stateFreqField[i].getValue() - difference;
	 		setOptions();
	 	}
	 	else if   ("Norm*/".equals(e.getActionCommand())) {
			double total =0;
			for (int i=0; i<getNumStates(); i++) 
				total += stateFreqField[i].getValue();
			for (int i=0; i<getNumStates(); i++) 
				stateFrequenciesTemp[i] = stateFreqField[i].getValue()/total;
	 		setOptions();
	 	}
	 }
 	/*.................................................................................................................*/
	/** returns parameters of the model. */
	public String getParameters (){
		return "User-specified state frequency model, with values (A C G T): " + toStateFreqString();
	}

}



