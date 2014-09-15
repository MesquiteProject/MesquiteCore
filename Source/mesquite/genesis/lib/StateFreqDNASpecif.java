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
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;


/* ======================================================================== */
public class StateFreqDNASpecif extends StateFreqDNAModel {
	boolean specified = false;
	double[] frequencies;
	DoubleField AField, CField, GField, TField;
	double AFreq = 0.25, CFreq =0.25, GFreq=0.25, TFreq=0.25;
	double AFreqTemp, CFreqTemp, GFreqTemp, TFreqTemp;
	
	public StateFreqDNASpecif (CompositProbCategModel probabilityModel) {
		super(probabilityModel);
		
	}
	/*.................................................................................................................*/
	public String getNexusSpecification(){ //needs to return freqs
		return "";
	}
	public boolean isFullySpecified(){
		return specified;
	}
 	/*.................................................................................................................*/
	public CharacterModel cloneModelWithMotherLink(CharacterModel formerClone){
		StateFreqDNASpecif model = new StateFreqDNASpecif(probabilityModel);
		completeDaughterClone(formerClone, model);
		return model;
	}
 	/* copy information from this to model passed (used in cloneModelWithMotherLink to ensure that superclass info is copied); should call super.copyToClone(pm) */
	public void copyToClone(CharacterModel md){
		if (md == null || !(md instanceof StateFreqDNASpecif))
			return;
		StateFreqDNASpecif model = (StateFreqDNASpecif)md;
		model.setStateFrequencies(frequencies);
		super.copyToClone(md);
	}
 	/*.................................................................................................................*/
	/** Sets the equilibrium state frequencies to be all equal. */
	public void setStateFrequencies (){
	}
 	/*.................................................................................................................*/
	/** Sets the equilibrium state frequencies to be all equal. */
	public void setStateFrequencies (double[] frequencies){
		for (int i=0; i<frequencies.length; i++) {
			setStateFreq(i,frequencies[i]);
			this.frequencies[i] = frequencies[i];
		}
		specified = true;
	}
	/*.................................................................................................................*/
	public void addOptions(ExtensibleDialog dialog) {
		AField = dialog.addDoubleField("A:",AFreq, 20);
		CField = dialog.addDoubleField("C:",CFreq, 20);
		GField = dialog.addDoubleField("G:",GFreq, 20);
		TField = dialog.addDoubleField("T:",TFreq, 20);
	}
 	/*.................................................................................................................*/
	public boolean recoverOptions() {
		AFreqTemp = AField.getValue();
		CFreqTemp = CField.getValue();
		GFreqTemp = GField.getValue();
		TFreqTemp = TField.getValue();
		return true;
	}
	/*.................................................................................................................*/
	public boolean checkOptions() {
		return true;
	}
	/*.................................................................................................................*/
	public String checkOptionsReport() {
		return "";
	}
 	/*.................................................................................................................*/
	public void setOptions() {
		setStateFreq(0,AFreqTemp);
		setStateFreq(1,CFreqTemp);
		setStateFreq(2,GFreqTemp);
		setStateFreq(3,TFreqTemp);
		specified = true;
		notifyListeners(this, new Notification(MesquiteListener.UNKNOWN));
	}
}

