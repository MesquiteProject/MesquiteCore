/* Mesquite source code.  Copyright 1997-2006 W. Maddison and D. Maddison. 
 This module copyright 2006 P. Midford and W. Maddison

Version 1.11, June 2006.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.correl.lib;


import mesquite.lib.*;
import mesquite.lib.characters.CharacterDistribution;
import mesquite.lib.duties.*;

public abstract class ContrastsForCharAndTree extends MesquiteModule {
 	 public Class getDutyClass() {
    	 	return ContrastsForCharAndTree.class;
    	 }
  	public String getDutyName() {
  		return "Contrasts for Character and Tree";
    	 }

 	public  abstract void calculateContrasts(Tree tree, CharacterDistribution observedStates, NumberArray result, MesquiteString resultString, CommandRecord commandRec);


}

