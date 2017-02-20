/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.stochchar.lib;

import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;


/* ======================================================================== */
/**
This class of modules supplies character models for use in calculation routines.
These methods must be passed a Data or a CharacterDistribution object because which models are appropriate will depend on these*/

public abstract class ProbModelSource extends CharacterModelSource  {

   	 public Class getDutyClass() {
   	 	return ProbModelSource.class;
   	 }
 	public String getDutyName() {
 		return "Probability Model Source";
   	 }
   	 public String[] getDefaultModule() {
   	 	return new String[] {"#CurrentProbModels"};
   	 }

}


