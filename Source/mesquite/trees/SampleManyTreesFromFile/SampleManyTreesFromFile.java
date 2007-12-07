package mesquite.trees.SampleManyTreesFromFile;

import mesquite.lib.*;
import mesquite.trees.lib.*;

public class SampleManyTreesFromFile extends ManyTreesFromFileLib {

	/*.................................................................................................................*/
	protected boolean additionStartJobItems(){
		addMenuItem("File for Sample Trees From Separate...", makeCommand("setFilePath",  this));
		addMenuItem("Number of Trees to Sample...", makeCommand("setNumTreesToSample",  this));
		if (!MesquiteThread.isScripting()){
			if (numTreesInTreeBlock>0 && MesquiteInteger.isCombinable(numTreesInTreeBlock)) {
				numTreesToSample =MesquiteInteger.queryInteger(containerOfModule(), "Number of Trees to Sample", "Number of Trees to Sample (out of " + numTreesInTreeBlock + " total trees) from file:", numTreesToSample, 0, numTreesInTreeBlock, true);
			}
			else {
				numTreesToSample =MesquiteInteger.queryInteger(containerOfModule(), "Number of Trees to Sample", "Number of trees to sample from file:", numTreesToSample, 0, MesquiteInteger.infinite, true);
			}
			if (!MesquiteInteger.isCombinable(numTreesToSample) || numTreesToSample==0)
					return false;
			else
				setTreesToSample(numTreesToSample);
		}
		addMenuItem("Number of Trees to Ignore...", makeCommand("setStartTreesToIgnore",  this));
		if (!MesquiteThread.isScripting()){
			if (numTreesInTreeBlock>0 && MesquiteInteger.isCombinable(numTreesInTreeBlock)) {
				numStartTreesToIgnore =MesquiteInteger.queryInteger(containerOfModule(), "Number of Trees to Ignore", "Number of trees to ignore (out of " + numTreesInTreeBlock + " total trees) from start of file:", numStartTreesToIgnore, 0, numTreesInTreeBlock, true);
				}
				else {
					numStartTreesToIgnore =MesquiteInteger.queryInteger(containerOfModule(), "Number of Trees to Ignore", "Number of trees to ignore from start of  file:", numStartTreesToIgnore, 0, MesquiteInteger.infinite, true);
				}
			if (!MesquiteInteger.isCombinable(numStartTreesToIgnore))
					return false;
			else
				setTreesToSample(numTreesToSample);
		}
		return true;
	}
	/*.................................................................................................................*/
 	 public void additionalSnapshot(Snapshot snapshot) {
   	 	snapshot.addLine("setNumTreesToSample " + numTreesToSample);  
  	 	snapshot.addLine("setStartTreesToIgnore " + numStartTreesToIgnore);  
 	 }	
 	 MesquiteInteger pos = new MesquiteInteger(0);
 	 /*.................................................................................................................*/
 	 public boolean additionalDoCommands(String commandName, String arguments, CommandChecker checker) {
		 if (checker.compare(this.getClass(), "Specifies the number of trees to sample", "[number of trees]", commandName, "setNumTreesToSample")) {
 			 pos.setValue(0);
 			 int num = MesquiteInteger.fromString(arguments, pos);
 			 if (!MesquiteInteger.isCombinable(num)&& !MesquiteThread.isScripting()){
 				if (numTreesInTreeBlock>0 && MesquiteInteger.isCombinable(numTreesInTreeBlock)) {
 					num =MesquiteInteger.queryInteger(containerOfModule(), "Number of Trees to Sample", "Number of Trees to Sample (out of " + numTreesInTreeBlock + " total trees) from file:", numTreesToSample, 0, numTreesInTreeBlock, true);
 				}
 				else {
 					num =MesquiteInteger.queryInteger(containerOfModule(), "Number of Trees to Sample", "Number of trees to sample from file:", numTreesToSample, 0, MesquiteInteger.infinite, true);
 				}
 			 }
 			 if (MesquiteInteger.isCombinable(num)) {
 				 numTreesToSample = num;
 				 setTreesToSample(numTreesToSample);
 				 parametersChanged();
 			 }
 			 return true;
 		 }
		 else  if (checker.compare(this.getClass(), "Specifies the number of trees to ignore from the start of the file", "[number of trees]", commandName, "setStartTreesToIgnore")) {
 			 pos.setValue(0);
 			 int num = MesquiteInteger.fromString(arguments, pos);
 			 if (!MesquiteInteger.isCombinable(num)&& !MesquiteThread.isScripting()){
 				if (numTreesInTreeBlock>0 && MesquiteInteger.isCombinable(numTreesInTreeBlock)) {
 					num =MesquiteInteger.queryInteger(containerOfModule(), "Number of Trees to Ignore", "Number of trees to ignore (out of " + numTreesInTreeBlock + " total trees) from start of file:", numStartTreesToIgnore, 0, numTreesInTreeBlock, true);
				}
 				else {
 					num =MesquiteInteger.queryInteger(containerOfModule(), "Number of Trees to Ignore", "Number of trees to ignore from start of  file:", numStartTreesToIgnore, 0, MesquiteInteger.infinite, true);
 				}
 			 }
 			 if (MesquiteInteger.isCombinable(num)) {
 				numStartTreesToIgnore = num;
 				 setTreesToSample(numTreesToSample);
 				 parametersChanged();
 			 }
 			 return true;
 		 }
 		 return false;
 	 }
 	/*.................................................................................................................*/
 	protected boolean canIgnoreStartTrees(){
 		return true;
 	}
 	 /*.................................................................................................................*/
 	 protected boolean canDoLiveUpdate(){
 		 return false;
 	 }
 	 /*.................................................................................................................*/
 		 protected boolean getSampleTrees(){
 			 return true;
	}
	/*.................................................................................................................*/
	 public String getName() {
	return "Sample Trees from Separate File";
	 }
		/*.................................................................................................................*/
		/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
		 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
		 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
		public int getVersionOfFirstRelease(){
			return 200;  
		}
/*.................................................................................................................*/
	 public String getNameForMenuItem() {
	return "Sample Trees from Separate File...";
	 }
	 
/*.................................................................................................................*/
	 public String getExplanation() {
	return "Supplies randomly-sampled trees directly from a file, without bringing the contained tree block entirely into memory.  This allows much larger blocks of trees to be used within constraints of memory, but will make some calculations slower.  This module does not know how many trees are in the file, and hence may attempt to read files beyond the number in the file.";
	 }

}
