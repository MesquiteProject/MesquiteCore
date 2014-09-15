/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.aTreeFarmIntro;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/*
To do:
o If only single tree window, these modifying tree sources should not be available, since they will recursively look internally

======== Random adjustments of current tree ========
++ Randomly rearrange tree (makes n randomly chosen branch moves)

++ Augment tree randomly

++ Rarefy tree randomly

++ Random resolutions of polytomy

++ Add noise to branch lengths (uniform normal; binary noise; biased normal, e.g. increase variance deeper)

-- Reshuffling terminals

======== Determinate adjustments ========

++ All rerootings

++ Rearrangements (using tree search rearrangments)

-- Adjust branch lengths of trees (uses branch lengths adjusters available)

-- Partition tree depending on taxa partition

======== Simulations of evolution/ Fully random trees ========
++ All dichotomous trees

++ Equiprobable speciation (pure birth process -- conditioned on what?  terminal branch lengths?)

++ Equiprobable trees

-- Constant birth/death process

-- Randomly varying birth/death parameters

-- b/d depends of simulation of evolution of character affecting diversification rates

-- b/d depends on age of lineage

*/
/* ======================================================================== */
public class aTreeFarmIntro extends PackageIntro {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;
  	 }
  	 public Class getDutyClass(){
  	 	return aTreeFarmIntro.class;
  	 }
	/*.................................................................................................................*/
    	 public String getExplanation() {
		return "Extra utilities for trees, including comparisons, randomizations and manipulations.";
   	 }
   
	/*.................................................................................................................*/
    	 public String getName() {
		return "Tree Farm Package Introduction";
   	 }
	/*.................................................................................................................*/
	/** Returns the name of the package of modules (e.g., "Basic Mesquite Package", "Rhetenor")*/
 	public String getPackageName(){
 		return "Tree Farm";
 	}
	/*.................................................................................................................*/
	/** Returns whether package is built-in (comes with default install of Mesquite)*/
	public boolean isBuiltInPackage(){
		return true;
	}
	/*.................................................................................................................*/
	/** Returns citation for a package of modules*/
 	public String getPackageCitation(){
 		return "Maddison, W.P.,  D.R. Maddison and P. Midford, 2014.  Tree Farm package for Mesquite, version 3.0.";
 	}
	/*.................................................................................................................*/
  	 public String getPackageVersion() {
		return "3.00";
   	 }
	/*.................................................................................................................*/
  	 public String getPackageAuthors() {
		return "W. Maddison, D. Maddison and P. Midford.";
   	 }
	/*.................................................................................................................*/
	/** Returns whether there is a splash banner*/
	public boolean hasSplash(){
 		return true; 
	}
}

