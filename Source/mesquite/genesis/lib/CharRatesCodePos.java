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

import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import java.lang.Math.*;
import java.awt.*;

/** A class that provides for a site-to-site rate variation model. */
/* ======================================================================== */
public class CharRatesCodePos extends CharRatesModel {
	double[] codePosRates;
	double[] codePosRatesTemp;
	String errorMessage="";
	Random randomNumber;
	DoubleField[] codePosRatesField;
	int numRates = 5;
	CodonPositionsSet modelSet = null;
	boolean baseOnMatrix = false;
	boolean baseOnMatrixTemp = false;
	Checkbox baseOnMatrixCheckBox;
	int codonPosition = 1;

	public CharRatesCodePos (double[] codePosRates) {
		super();
		codePosRates = new double[numRates];
		codePosRatesTemp = new double[numRates];
		this.codePosRates = codePosRates;
		randomNumber = new Random();
		//setNewRate();
 	}
	public CharRatesCodePos () {
		super();
		codePosRates = new double[numRates];
		codePosRatesTemp = new double[numRates];
		for (int i=0; i<numRates; i++) {
			codePosRates[i] = MesquiteDouble.unassigned;
			codePosRatesTemp[i] = MesquiteDouble.unassigned;
		}
		codePosRates[0] = 1.0;
		codePosRatesTemp[0] = 1.0;
		codePosRates[4] = 1.0;
		codePosRatesTemp[4] = 1.0;
		randomNumber = new Random();
		codonPosition = 1;
		//setNewRate();
 	}

 	/*.................................................................................................................*/
 	/** Should be overridden to return true if submodel needs an empirical matrix*/
	public boolean needsEmpirical(){
		return baseOnMatrix;
	}
 	/*.................................................................................................................*/
	public void initialize() {
		codonPosition = 1;
	}
 	/*.................................................................................................................*/
	public void setSeed(long seed){
		randomNumber.setSeed(seed);
	}
 	/*.................................................................................................................*/
	public CharacterModel cloneModelWithMotherLink(CharacterModel formerClone){
		CharRatesCodePos model = new CharRatesCodePos();
		completeDaughterClone(formerClone, model);
		return model;
	}
 	/* copy information from this to model passed (used in cloneModelWithMotherLink to ensure that superclass info is copied); should call super.copyToClone(pm) */
	public void copyToClone(CharacterModel md){
		if (md == null || !(md instanceof CharRatesCodePos))
			return;
		CharRatesCodePos model = (CharRatesCodePos)md;
		model.baseOnMatrixTemp = baseOnMatrixTemp;
		model.setCodePosRates(codePosRates);
		model.baseOnMatrix = baseOnMatrix;
		super.copyToClone(md);
	}
	/*.................................................................................................................*/
	public void setNewRate(int ic) {
		if (baseOnMatrix) {
			if (modelSet != null && ic<modelSet.getNumberOfParts() && ic>=0) {
				int position = modelSet.getInt(ic);
				if (position==MesquiteInteger.unassigned)
					position=numRates-1;
				if (position>=0 && position<=numRates-1 && codePosRates[position] != MesquiteDouble.unassigned) {
					setRate(codePosRates[position]);
				}
				else
					setRate(1.0);
			}
			else
				setRate(1.0);
		}
		else {
			setRate(codePosRates[ic]);
		}
/*		if (codePosRates[0] != MesquiteDouble.unassigned)
			if (randomNumber.nextDouble()<=codePosRates)
				setRate(0.0);
			else
				setRate(1.0);
*/
	}
	/*.................................................................................................................*/
	public boolean checkValidityCharRates (){  
		MCharactersStatesHolder d = probabilityModel.getMCharactersStatesHolder();
		if (!(DNAData.class.isAssignableFrom(d.getCharacterDataClass()))){
			MesquiteMessage.warnProgrammer("Empirical data in model null or not DNA data; will use equal freq. (CodonRatesCodPos)");
			return false;
		}
		MCategoricalStates data = (MCategoricalStates)d;
		if (data!=null){
			DNAData dnaData= (DNAData)data.getParentData();
			if (dnaData==null)
				return false;
			modelSet = (CodonPositionsSet)dnaData.getCurrentSpecsSet(CodonPositionsSet.class);
			return (modelSet!=null);
		}
		return false;
	}
	/*.................................................................................................................*/
	public void addOptions(ExtensibleDialog dialog) {
		codePosRatesField = new DoubleField[numRates];
	
		for (int i=1; i<numRates-1; i++) {
			codePosRatesField[i] = dialog.addDoubleField(""+i+":",codePosRates[i], 10);
		}
		codePosRatesField[0] = dialog.addDoubleField("N:",codePosRates[0], 10);   // this is for non-coding positions
		codePosRatesField[numRates-1] = dialog.addDoubleField("?:",codePosRates[numRates-1], 10);  // this is for unspecified positions
		baseOnMatrixCheckBox = dialog.addCheckBox("use positions in existing matrix", baseOnMatrix);
	}
 	/*.................................................................................................................*/
	public boolean recoverOptions() {
		for (int i=0; i<numRates; i++) {
			codePosRatesTemp[i] = codePosRatesField[i].getValue();
		}
		baseOnMatrixTemp = baseOnMatrixCheckBox.getState();
		codonPosition = 1;
		return true;
	}
 	/*.................................................................................................................*/
	public boolean checkOptions() {
		errorMessage = "";
		for (int i=0; i<numRates; i++) {
			codePosRatesTemp[i] = codePosRatesField[i].getValue();
			if (!MesquiteDouble.isCombinable(codePosRatesTemp[i])) {
				errorMessage = "Some fields do not have numbers entered into them.";
				return false;
			}
			else if (codePosRatesTemp[i]<0.0) {
				errorMessage = "Numbers must be greater than or equal to 0.0";
				return false;
			}
				
		}
		return true;
	}
	/*.................................................................................................................*/
	public String checkOptionsReport() {
		if (!checkOptions())
			return errorMessage;
		return "";
	}
 	/*.................................................................................................................*/
	public void setOptions() {
		baseOnMatrix = baseOnMatrixTemp;
		setCodePosRates(codePosRatesTemp);
		codonPosition = 1;
		notifyListeners(this, new Notification(MesquiteListener.UNKNOWN));
	}
 	/*.................................................................................................................*/
	public boolean isFullySpecified(){
		for (int i=0; i<numRates; i++)
			if (codePosRates[i]== MesquiteDouble.unassigned)
				return false;
		return true;
	}
 	/*.................................................................................................................*/
	public void setCodePosRates (double[] codePosRates) {
		this.codePosRates = codePosRates;
		codonPosition = 1;
//		setNewRate();
 	}
 	/*.................................................................................................................*/
	public double[] getCodePosRates () {
		return codePosRates;
 	}

 	/*.................................................................................................................*/
	public void initForNextCharacter(){
		if (!baseOnMatrix) {
			if (probabilityModel.isFirstCharacter()) {
				codonPosition = 1;
			}
			setNewRate(codonPosition);
			codonPosition++;
			if (codonPosition>3)
				codonPosition = 1;
		}
		else if (checkValidityCharRates()) {
			if (probabilityModel !=null) {
				if (probabilityModel.getCharacterDistribution()!=null) {
						setNewRate(probabilityModel.getCharacterDistribution().getParentCharacter());
				}
			}
		}
	}

	/*.................................................................................................................*/
	/** returns speficiations*/
	public String getNexusSpecification() {
		String s = "codePosRates = (";
		for (int i=0; i<numRates; i++) {
			if (i>0)
				s += " ";
			s += MesquiteDouble.toString(codePosRates[i]);
		}
		if (baseOnMatrix)
			s += " baseOnMatrix)";
		else
			s += " noBaseOnMatrix)";
		return s;
		
	}
	/*.................................................................................................................*/
	/** reads parameters from string (same format as written by "toSTring"*/
	public void fromString(String description, MesquiteInteger stringPos, int format) {
		String s;
		ParseUtil.getToken(description, stringPos); //codePo
		ParseUtil.getToken(description, stringPos); // =
		ParseUtil.getToken(description, stringPos); // (
		for (int i=0; i<numRates; i++) {
			s = ParseUtil.getToken(description, stringPos);
   			codePosRates[i] = MesquiteDouble.fromString(s);
   		}
		s = ParseUtil.getToken(description, stringPos);
		if ("baseOnMatrix".equalsIgnoreCase(s)) {
			baseOnMatrix = true;
			baseOnMatrixTemp = true;
		}
		else {
			baseOnMatrix = false;
			baseOnMatrixTemp = false;
		}
	}
	/*.................................................................................................................*/
	/** returns name of model class (e.g. "stepmatrix")*/
	public String getNEXUSClassName() {
		return "codePosRates";
	}
 	/*.................................................................................................................*/
	/** returns parameters of the model. */
	public String getParameters (){
		String s = "Codon Position Rates = ";
		for (int i=1; i<=3; i++) {
			if (i>1)
				s += " ";
			s += "Position "+i+": ";
			s += MesquiteDouble.toString(codePosRates[i]);
		}
		s += " Non-coding: ";
		s += MesquiteDouble.toString(codePosRates[0]);
		return s;
	}
}

