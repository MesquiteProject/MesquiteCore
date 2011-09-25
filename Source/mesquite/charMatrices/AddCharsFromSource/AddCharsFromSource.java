package mesquite.charMatrices.AddCharsFromSource;

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
/*~~  */

import java.util.*;
import java.lang.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class AddCharsFromSource extends CategDataAlterer {
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false; 
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Add Characters from Source...";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Add Characters from Source";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Adds characters from a source of characters." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 275;  
	}

	CharSourceCoordObed characterSourceTask;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		//character source %%%%%%%%
		characterSourceTask = (CharSourceCoordObed)hireEmployee(CharSourceCoordObed.class, "Source of characters (for " + getName() + ")");
		if (characterSourceTask == null)
			return sorry(getName() + " couldn't start because no source of characters was obtained.");
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}
	/*.................................................................................................................*/
	/** Called to alter data in those cells selected in table*/
	public boolean alterData(CharacterData data, MesquiteTable table, UndoReference undoReference){
		
		characterSourceTask.initialize(data.getTaxa());
		int maxNum = characterSourceTask.getNumberOfCharacters(data.getTaxa());
		String s = "";
		if (!MesquiteInteger.isCombinable(maxNum))
			maxNum = 10000;
		else 
			s = " (" + maxNum + " available)";
		int numChars = MesquiteInteger.queryInteger(containerOfModule(), "Number of characters", "Number of characters to add from " + characterSourceTask.getName() + s, 1, 1, maxNum);
		if (!MesquiteInteger.isCombinable(numChars))
			return false;
		CharacterState cs = null;
		for (int i = 0; i<numChars; i++){
			CharacterDistribution dist = characterSourceTask.getCharacter(data.getTaxa(), i);
			if (data.getStateClass() != dist.getStateClass()){
				discreetAlert("Sorry, the source is supplying characters of a different type, and so can't be used");
				return false;
			}
			data.addCharacters(data.getNumChars(), 1, false);
			int ic = data.getNumChars()-1;
			for (int it = 0; it<data.getNumTaxa(); it++){
				cs = dist.getCharacterState(cs, it);
				data.setState(ic, it, cs);
			}
		}
		return true;
	}

	//	Double d = new Double(value);

	/*.................................................................................................................*/
	public boolean showCitation() {
		return false;
	}
}


