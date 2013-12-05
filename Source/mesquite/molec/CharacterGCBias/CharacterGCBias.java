/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.molec.CharacterGCBias;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.cont.lib.*;
import mesquite.categ.lib.*;

/* ======================================================================== */
public class CharacterGCBias extends NumberForCharacter implements NumForCharTreeIndep {
	double resultNum;
	boolean wrongType = false;
	boolean first = true;
	long A = CategoricalState.makeSet(0);
	long C = CategoricalState.makeSet(1);
	long G = CategoricalState.makeSet(2);
	long T = CategoricalState.makeSet(3);
	long AT = A | T;
	long CG =  C | G;
	long ACGT = AT | CG;
	int whatFreq = 0;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		ListableVector f  = new ListableVector();
		f.addElement(new MesquiteString("GC Bias"), false);
		f.addElement(new MesquiteString("A Frequency"), false);
		f.addElement(new MesquiteString("C Frequency"), false);
		f.addElement(new MesquiteString("G Frequency"), false);
		f.addElement(new MesquiteString("T Frequency"), false);
		addSubmenu(null, "Frequency Calculated", makeCommand("setMode", this), f);
		return true;
  	 }
  	 
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot();
 	 	temp.addLine("setMode " + whatFreq);  //TODO: note not snapshotting tree task
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
   	 	if (checker.compare(this.getClass(), "Sets what frequency is calculated", "[GC = 0, A = 1, C = 2, G = 3, T = 4]", commandName, "setMode")) {
    	 		int w = MesquiteInteger.fromString(parser.getFirstToken(arguments));
    	 		if (w>=0 && w<=4) {
    	 			whatFreq = w;
    	 			if (!MesquiteThread.isScripting())
    	 				parametersChanged();
    	 		}
    	 	}
      	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
    	 	return null;
    	 }
   
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(CharacterDistribution charStates){

   	}
	/*.................................................................................................................*/
  	 public CompatibilityTest getCompatibilityTest() {
  	 	return new RequiresAnyDNAData();
  	 }
	/*.................................................................................................................*/
	public  void calculateNumber(CharacterDistribution charStates, MesquiteNumber result, MesquiteString resultString) {
    	 	if (result==null)
    	 		return;
    	clearResultAndLastResult(result);
		int numT = charStates.getNumTaxa();
		wrongType = !(DNAState.class.isAssignableFrom(charStates.getStateClass()));
		if (wrongType) {
			if (first)
				alert("Error: non DNA-character in CharacterGCBias.  Unable to calculate the Compositional Bias.");
			first = false;
			return;
		}
		int total=0;
		int count=0;
		CategoricalDistribution dna = (CategoricalDistribution)charStates;
		for (int it = 0; it<numT; it++) {
			if (!charStates.isUnassigned(it) && !charStates.isInapplicable(it)) {
				long s = dna.getState(it);
				if (!CategoricalState.isUnassigned(s) && !CategoricalState.isInapplicable(s)) {
					if (whatFreq == 0){ //CG bias; counts only if clearly a C/G versus A/T;  polymorphisms mixing A & G, C & G, A & T, C & T are not counted
						if (s == A || s == T || s == AT) //monomorphic A or T or A&T or uncertain A or T
							total++;
						else if (s == C || s == G || s == CG) { //monomorphic C or G or C&G or uncertain C or G
							total++;
							count++;
						}
					}
					else if (!CategoricalState.isUncertain(s) && (s & ACGT) != 0L){ //individual base frequency; counts only if monomorphic or polymorphic, not uncertain
						total++;
						if (whatFreq == 1 && ((A & s) != 0L)){ //A
							count++;
						}
						else if (whatFreq == 2  && ((C & s) != 0L)){ //C
							count++;
						}
						else if (whatFreq == 3  && ((G & s) != 0L)){ //G
							count++;
						}
						else if (whatFreq == 4 && ((T & s) != 0L)){ //T
							count++;
						}
					}
				}
			}
		}
		if (total == 0)
			resultNum = 0;
		else
			resultNum = 100.000*count/total;
		if (wrongType)
			result.setToUnassigned();
		else
			result.setValue(resultNum);
			
		if (resultString!=null)
			resultString.setValue(getParameters() + ": "+ result.toString());
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
 	/** returns current parameters, for logging etc..*/
 	public String getParameters() {
 		if (whatFreq == 0)
 			return "GC Bias";
 		else if (whatFreq == 1)
 			return "Frequency of A";
 		else if (whatFreq == 2)
 			return "Frequency of C";
 		else if (whatFreq == 3)
 			return "Frequency of G";
 		else if (whatFreq == 4)
 			return "Frequency of T";
   		return null;  
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Character Compositional Bias";
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Calculates the percent of taxa with particular nucleotides (GC bias, or individual frequency of A, C, G or T) for a character." ;
   	 }
}

