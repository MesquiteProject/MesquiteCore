/* Mesquite.  Copyright 1997-2005 W. Maddison, D. Maddison & Peter Midford. 
Version 1.06, August 2005.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.scm.SCMappingsSummary;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.categ.lib.CategInternodeEvent;
import mesquite.categ.lib.CategoricalHistory;
import mesquite.categ.lib.CategoricalState;
import mesquite.categ.lib.UniversalCategStateTest;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.stochchar.lib.MargLikeAncStCLForModel;
import mesquite.stochchar.lib.ProbModelSourceLike;
import mesquite.stochchar.lib.ProbPhenCategCharModel;
import mesquite.stochchar.lib.ProbabilityCategCharModel;

/* ======================================================================== */
public class SCMappingsSummary extends NumbersForNodesWithChar {
	CharMapper mapper;
	int numMappings = 100;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
 		
 		mapper = (CharMapper)hireNamedEmployee(commandRec, CharMapper.class, "#StochCharMapper");
 		if (mapper == null)
 			return sorry(commandRec, getName() + " couldn't start because no mapping module was obtained.");
 		return true;
 	}

   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public  void initialize(Tree tree, CommandRecord commandRec){
   	}

	public void calculateNumbers(Tree tree, CharacterDistribution charDistribution, NumberArray result, MesquiteString resultString, CommandRecord commandRec){
		mapper.setObservedStates( tree,  charDistribution,  commandRec);
		int numMaps = mapper.getNumberOfMappings(commandRec);
		result.zeroArray();
		if (!MesquiteInteger.isCombinable(numMaps))
			numMaps = 100;  //TODO: have settable value
		CategoricalHistory history =(CategoricalHistory)charDistribution.adjustHistorySize(tree, null);
		for (int i=0; i<numMaps; i++){
			mapper.getMapping( i,  history, null,  commandRec); 
			countInternodeChanges(tree, tree.getRoot(), result, history);
		}
	}
	/*.................................................................................................................*/
	MesquiteNumber temp = new MesquiteNumber();
    private void countInternodeChanges(Tree tree,int node,NumberArray result, CategoricalHistory sampledHistory) {

        if (node != tree.getRoot()) {
        		temp.setValue(sampledHistory.getNumberOfEvents(node));
        		result.addValue(node, temp);
        }
        if (tree.nodeIsInternal(node)){
            for(int d=tree.firstDaughterOfNode(node);tree.nodeExists(d);d=tree.nextSisterOfNode(d)) 
                countInternodeChanges(tree,d,result, sampledHistory);
        }
      
    }	
     private void averageInternodeChanges(Tree tree,int node,NumberArray result, int numMaps) {

        if (node != tree.getRoot()) {
        		result.placeValue(node, temp);
        		temp.divideBy(numMaps);
        		result.setValue(node, temp);
        }
        if (tree.nodeIsInternal(node)){
            for(int d=tree.firstDaughterOfNode(node);tree.nodeExists(d);d=tree.nextSisterOfNode(d)) 
                averageInternodeChanges(tree,d,result, numMaps);
        }
      
    }	
    
  
    /*.................................................................................................................*/
	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
	public CompatibilityTest getCompatibilityTest(){
		return new UniversalCategStateTest();
	}
   
	/*.................................................................................................................*/
    	 public String getName() {
		return "Summary of Stoch. Char Mappings";
   	 }
	/*.................................................................................................................*/
    	 public boolean showCitation() {
		return true;
   	 }
	/*.................................................................................................................*/
  	 public boolean isPrerelease() { //need to build algorithm!  Add control for replicate samples!
		return true;
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Summarizes a sample of stochastic character mappings. " ;
   	 }
   	 
}

