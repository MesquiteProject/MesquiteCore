/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.duties;

import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.table.*;
import mesquite.lib.characters.*;


/* ======================================================================== */

public abstract class DataMatcher extends MesquiteModule  {
	protected MesquiteTable table;
	protected mesquite.lib.characters.CharacterData data;
	protected mesquite.lib.characters.CharacterState state;


   	 public Class getDutyClass() {
   	 	return DataMatcher.class;
   	 }
 	public String getDutyName() {
 		return "Data Matcher";
   	}
	/*.................................................................................................................*/
	public void setTableAndData( MesquiteTable table, mesquite.lib.characters.CharacterData data) {
		this.table = table;
		this.data =data;
		state = data.makeCharacterState();
	}
   	/*.................................................................................................................*/
   	/** this method holds whatever preparation calls are needed just before use of this method */
	public void prepare() {
	}
   	/*.................................................................................................................*/
   	/** specifies whether or not the match module can function if the master piece is entirely inapplicable data */
	public boolean requiresApplicable() {
		return true;
	}   	
 	/*.................................................................................................................*/
   	/** Returns the value of the best possible match.  Should be overridden by subclasses if they can calculate the value.*/
	public abstract double getBestMatchValue(CharacterState[] csOriginalArray) ;
	/*.................................................................................................................*/
   	/** Returns the approximate value of the worst possible match.  Should be overridden by subclasses if they can calculate the value.*/
	public abstract double getApproximateWorstMatchValue(CharacterState[] csOriginalArray) ;
  	/*.................................................................................................................*/
   	/** Returns the value of the best possible match.  Should be overridden by subclasses if they can calculate the value.*/
	public double getBestMatchValue() {
		return MesquiteDouble.unassigned;
	}   	
 	/*.................................................................................................................*/
   	/** Returns the value of the worst possible match.  Should be overridden by subclasses if they can calculate the value.*/
	public double getWorstMatchValue() {
		return MesquiteDouble.unassigned;
	}   	
	/*.................................................................................................................*/
   	/** Returns whether or not better matches have higher values or not.*/
	public boolean getHigherIsBetter() {
		return true;
	}   	

  	/** Returns whether candidate sequence beginning at candidateStartChar matches a stretch of the matrix (originalTaxon from originalStartChar to originalEndChar) .  
   	candidateEndChar might be null or not; if it is not null, then this method should return in it the end character of any match. */
   	public double sequenceMatch(mesquite.lib.characters.CharacterData data, MesquiteTable table, int originalTaxon, int originalStartChar, int originalEndChar, int candidateTaxon, int candidateStartChar, MesquiteInteger candidateEndChar){
   		return sequenceMatch(data,table,data.getCharacterStateArray(originalTaxon,originalStartChar,originalEndChar),candidateTaxon, candidateStartChar, candidateEndChar);
   	}

   	/** Returns whether candidate stretch of matrix matches the data passed in the CharacterState array csOriginalArray*/
   	public double sequenceMatch(mesquite.lib.characters.CharacterData data, MesquiteTable table, CharacterState[] csOriginalArray, int candidateTaxon, int candidateStartChar, MesquiteInteger candidateEndChar){
   		setTableAndData(table,data);
   		return sequenceMatch(csOriginalArray,  candidateTaxon,candidateStartChar,candidateEndChar);
   	}

   	/** Returns whether candidate stretch of matrix, in taxon candidateTaxon, starting at candidateStartChar, ending at candidateEndChar,
   	 *  matches the data passed in the CharacterState array csOriginalArray.   Note that while candidateStartChar is fixed, candidateEndChar is not, and 
   	 *  the length of the potential matching region iis determined by the module, and the chosen end character is to be returned in candidateEndChar.*/
   	public abstract double sequenceMatch(CharacterState[] csOriginalArray, int candidateTaxon, int candidateStartChar, MesquiteInteger candidateEndChar);

  	/** Returns whether the match for the two given CharacterState arrays.*/
   	public abstract double sequenceMatch(CharacterState[] csOriginalArray, CharacterState[] csCandidateArray);

}


