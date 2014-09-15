/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.TaxonGCBias;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.cont.lib.*;
import mesquite.categ.lib.*;

/* Modified  9 July 07 to add GC Skew -  DRM */
/* ======================================================================== */
public class TaxonGCBias extends CharacterSource {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(MatrixSourceCoord.class, getName() + "  needs a source of sequences.",
		"The source of sequences is arranged initially");
	}
	Taxa taxa=null;
	MCategoricalDistribution matrix;
	MatrixSourceCoord matrixSourceTask;
	GCBiasCharacter states;
	String[] items;
	long A = CategoricalState.makeSet(0);
	long C = CategoricalState.makeSet(1);
	long G = CategoricalState.makeSet(2);
	long T = CategoricalState.makeSet(3);
	long AT = A | T;
	long CG =  C | G;
	long ACGT = A | C | G | T;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (condition!=null && condition!= ContinuousData.class && condition!=ContinuousState.class) {
			return sorry(getName() + " could not be used because it supplies only continuous-valued matrices");
		}
		matrixSourceTask = (MatrixSourceCoord)hireCompatibleEmployee(MatrixSourceCoord.class, DNAState.class, "DNA Matrix on which to calculate compositional bias"); //TODO: allow resetting of source (e.g., to simulation)
		if (matrixSourceTask==null){
			return sorry(getName() + " could not start because a suitable source of matrices was not obtained");
		}
		items = new String[1];
		items[0] = "bias";
		states = new GCBiasCharacter(null, 3);
		states.setItems(items);
		return true;
	}
	/*.................................................................................................................*/
	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
	/*.................................................................................................................*/
	public CompatibilityTest getCompatibilityTest() {
		return new ContinuousStateTest();
	}
	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa){
		this.taxa = taxa;
		matrixSourceTask.initialize(taxa);
	}
	/*.................................................................................................................*/
	public void initialize(Tree tree){
		if (tree!=null)
			taxa = tree.getTaxa();
		matrixSourceTask.initialize(tree);
	}

	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (states!=null)
			for (int it = 0; it<states.getNumNodes(); it++)
				states.setState(it, 0, MesquiteDouble.unassigned);
		parametersChanged(notification);
	}
	public boolean usesTree(){
		if (matrixSourceTask==null)
			return false;
		else
			return matrixSourceTask.usesTree();
	}
	/*.................................................................................................................*/
	public CharacterDistribution getCharacter(Tree tree, int icn) {
		if (tree!=null)
			taxa = tree.getTaxa();
		MCategoricalDistribution matrix = (MCategoricalDistribution)matrixSourceTask.getCurrentMatrix(tree);
		return getCharacter(taxa, matrix, icn);
	}
	/*.................................................................................................................*/
	public CharacterDistribution getCharacter(Taxa taxa, int icn) {
		MCategoricalDistribution matrix = (MCategoricalDistribution)matrixSourceTask.getCurrentMatrix(taxa);
		return getCharacter(taxa, matrix, icn);
	}
	/*.................................................................................................................*/
	private CharacterDistribution getCharacter(Taxa taxa, MCategoricalDistribution matrix, int icn) {
		if (matrix == null)
			return null;
		CharacterData data = matrix.getParentData();
		CharInclusionSet inclusion = null;
		if (data!=null)
			inclusion = (CharInclusionSet) data.getCurrentSpecsSet(CharInclusionSet.class);
		int numTaxa = taxa.getNumTaxa();
		states = (GCBiasCharacter)states.adjustSize( taxa);
		for (int it = 0; it<numTaxa; it++) {
			int tot = 0;
			int count = 0;
			double value = MesquiteDouble.unassigned;
			if (icn==5) {
				int cCount = 0;
				int gCount = 0;
				for (int ic = 0; ic<matrix.getNumChars(); ic++) {
					if (inclusion == null || inclusion.isBitOn(ic)){
						long s = matrix.getState(ic,it);
						if (!CategoricalState.isUnassigned(s) && !CategoricalState.isInapplicable(s)) {
							if (s == C) {
								cCount++;
								tot++;
							}
							if (s == G) {
								gCount++;
								tot++;
							}

						}
					}
				}
				if (tot == 0)
					value = MesquiteDouble.unassigned;  //changed from 0,  26 jan '14
				else
					value = (double)(gCount-cCount)/(gCount+cCount);

			}
			else {
				for (int ic = 0; ic<matrix.getNumChars(); ic++) {
					if (inclusion == null || inclusion.isBitOn(ic)){
						long s = matrix.getState(ic,it);
						if (!CategoricalState.isUnassigned(s) && !CategoricalState.isInapplicable(s)) {
							if (icn == 0){ //CG bias; counts only if clearly a C/G versus A/T;  polymorphisms mixing A & G, C & G, A & T, C & T are not counted
								if (s == A || s == T || s == AT) //monomorphic A or T or A&T or uncertain A or T
									tot++;
								else if (s == C || s == G || s == CG) { //monomorphic C or G or C&G or uncertain C or G
									tot++;
									count++;
								}
							}
							else if (!CategoricalState.isUncertain(s) && (s & ACGT) != 0L){ //individual base frequency; counts only if monomorphic or polymorphic, not uncertain
								tot++;
								if (icn == 1 && ((A & s) != 0L)){ //A
									count++;
								}
								else if (icn == 2  && ((C & s) != 0L)){ //C
									count++;
								}
								else if (icn == 3  && ((G & s) != 0L)){ //G
									count++;
								}
								else if (icn == 4 && ((T & s) != 0L)){ //T
									count++;
								}
							}
						}
					}
				}
				if (tot == 0)
					value = MesquiteDouble.unassigned; //changed from 0,  26 jan '14
				else
					value = ((double)count)/tot;
			}


			states.setState(it, 0, value);
		}
		states.setName( getCharacterName(taxa, icn) +  " in " + matrix.getName());  
		return states;
	}
	/*.................................................................................................................*/
	public int getNumberOfCharacters(Taxa taxa) {
		this.taxa = taxa;
		return 6;
	}
	/*.................................................................................................................*/
	/** returns the name of character ic*/
	public String getCharacterName(Taxa taxa, int ic){
		if (ic==0)
			return "Proportion G+C";
		else if (ic == 1)
			return "Proportion A";
		else if (ic == 2)
			return "Proportion C";
		else if (ic == 3)
			return "Proportion G";
		else if (ic == 4)
			return "Proportion T";
		else if (ic == 5)
			return "GC Skew";
		else
			return "?";
	}
	/*.................................................................................................................*/
	/** returns current parameters, for logging etc..*/
	public String getParameters() {
		if (matrix == null || matrix.getParentData()==null)
			return "";
		return "Compositional Bias in " + matrix.getParentData().getName();  
	}
	/*.................................................................................................................*/
	public String getName() {
		return "ACGT Compositional Bias";  
	}

	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Returns continuous characters which are the proportion G+C, and separately A, C, G, T, for each taxon in a DNA data set." ;
	}

}

class GCBiasCharacter extends ContinuousAdjustable {
	public GCBiasCharacter (Taxa taxa, int num) {
		super(taxa, num);
	}
	public CharacterModel getDefaultModel(MesquiteProject file, String paradigm){
		if (paradigm.equalsIgnoreCase("Parsimony")) {
			CharacterModel cm = file.getCharacterModel("Squared");
			if (cm==null) 
				System.out.println("Default model not found /Squared");
			return cm;
		}
		else if (paradigm.equalsIgnoreCase("Likelihood")) {
			CharacterModel cm = file.getCharacterModel("Brownian");
			if (cm==null) 
				System.out.println("Default model not found /brownian");
			return cm;
		}
		else
			return super.getDefaultModel(file, paradigm);
	}
	/*..........................................................*/
	//TODO: need one to make CharacterHistory also??????
	/** This readjust procedure can be called to readjust the size of storage of
	states of a character for nodes. */
	public AdjustableDistribution adjustSize(Taxa taxa) {
		if (taxa.getNumTaxa()!=getNumTaxa()) 
			return new GCBiasCharacter(taxa, taxa.getNumTaxa()); 
		else
			return this;
	}
}



