/* NINJA source code.  Copyright 2010 Travis Wheeler and David Maddison.

NINJA is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.NINJA.NeighborJoining;
/*~~  */

import java.io.File;
import java.io.FileNotFoundException;

import com.traviswheeler.ninja.TreeBuilder;
import com.traviswheeler.ninja.TreeBuilderManager;

import mesquite.distance.lib.MolecularTaxaDistance;
import mesquite.distance.lib.TaxaDistance;
import mesquite.distance.lib.TaxaDistanceSource;
import mesquite.lib.CommandChecker;
import mesquite.lib.Debugg;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.Incrementable;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteTimer;
import mesquite.lib.ResultCodes;
import mesquite.lib.Snapshot;
import mesquite.lib.analysis.DistanceAnalysis;
import mesquite.lib.duties.TreeInferer;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.TreeVector;

/* ======================================================================== */

public class NeighborJoining extends TreeInferer implements Incrementable, com.traviswheeler.libs.Logger, DistanceAnalysis {  //Incrementable just in case distance task is
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(TaxaDistanceSource.class, getName() + "  needs a source of distances.",
		"The source of distances can be selected initially");
	}
	TaxaDistanceSource distanceTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		distanceTask = (TaxaDistanceSource)hireEmployee(TaxaDistanceSource.class, "Source of distance for Cluster Analysis");
 		if (distanceTask == null) {
 			return sorry(getName() + " couldn't start because no source of distances was obtained.");
 		}
  		return true;
  	 }
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
 	 public void employeeQuit(MesquiteModule m){
  	 	iQuit();
  	 }
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot();
		temp.addLine("setDistanceSource ", distanceTask);
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	 if (checker.compare(this.getClass(), "Sets the source of distances for use in cluster analysis", "[name of module]", commandName, "setDistanceSource")) { 
    	 		TaxaDistanceSource temp=  (TaxaDistanceSource)replaceEmployee(TaxaDistanceSource.class, arguments, "Source of distance for cluster analysis", distanceTask);
 			if (temp!=null) {
 				distanceTask= temp;
 			}
 			return distanceTask;
 		}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
	}
    	 
    	/*.................................................................................................................*/
    	 public boolean isReconnectable(){
    	 	return false;
    	 }

	/*.................................................................................................................*/
 	public void setCurrent(long i){  //SHOULD NOT notify (e.g., parametersChanged)
 		if (distanceTask instanceof Incrementable)
 			((Incrementable)distanceTask).setCurrent(i);
 	}
 	public long getCurrent(){
 		if (distanceTask instanceof Incrementable)
 			return ((Incrementable)distanceTask).getCurrent();
 		return 0;
 	}
 	public String getItemTypeName(){
 		if (distanceTask instanceof Incrementable)
 			return ((Incrementable)distanceTask).getItemTypeName();
 		return "";
 	}
 	public long getMin(){
 		if (distanceTask instanceof Incrementable)
 			return ((Incrementable)distanceTask).getMin();
		return 0;
 	}
 	public long getMax(){
 		if (distanceTask instanceof Incrementable)
 			return ((Incrementable)distanceTask).getMax();
		return 0;
 	}
 	public long toInternal(long i){
 		if (distanceTask instanceof Incrementable)
 			return ((Incrementable)distanceTask).toInternal(i);
 		return i-1;
 	}
 	public long toExternal(long i){ //return whether 0 based or 1 based counting
 		if (distanceTask instanceof Incrementable)
 			return ((Incrementable)distanceTask).toExternal(i);
 		return i+1;
 	}
 	
	/*.................................................................................................................*/
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(Taxa taxa){
   		distanceTask.initialize(taxa);
   	}
   	
 	 public String getExtraTreeWindowCommands (boolean finalTree, long treeBlockID){
		String commands = "getTreeDrawCoordinator #mesquite.trees.BasicTreeDrawCoordinator.BasicTreeDrawCoordinator;\ntell It; ";
		commands += "setTreeDrawer  #mesquite.trees.SquareLineTree.SquareLineTree; tell It; showEdgeLines off; ";
		
		
		commands += "setNodeLocs #mesquite.trees.NodeLocsStandard.NodeLocsStandard;  tell It; orientRight; ";
		commands += " branchLengthsDisplay 1; ";
		commands += " endTell; setEdgeWidth 3; endTell; ";  // endTell is for SquareLineTree
		commands += "labelBranchLengths off;";
		
		commands += "getEmployee #mesquite.trees.BasicDrawTaxonNames.BasicDrawTaxonNames; tell It; setTaxonNameStyler  #mesquite.trees.ColorTaxonByPartition.ColorTaxonByPartition; setFontSize 12; endTell; ";		

		commands += " endTell; resetTitle;"; //endTell for BasicTreeDrawCoordinator
		commands += "getOwnerModule; tell It; getEmployee #mesquite.ornamental.ColorTreeByPartition.ColorTreeByPartition; tell It; colorByPartition on; endTell; endTell; ";
		return commands;
	}
/*
	public String getExtraTreeWindowCommands (boolean finalTree){

		String commands = "setSize 400 600; ";
		commands += " getTreeDrawCoordinator #mesquite.trees.BasicTreeDrawCoordinator.BasicTreeDrawCoordinator;\ntell It; ";
		commands += "setTreeDrawer  #mesquite.trees.SquareTree.SquareTree; tell It; orientRight; ";
		commands += "setNodeLocs #mesquite.trees.NodeLocsStandard.NodeLocsStandard;";
		commands += " tell It; branchLengthsDisplay 1; endTell; ";
		commands += " setEdgeWidth 3; endTell; ";
		commands += " endTell; ladderize root; ";
		return commands;
	}
	*/
	/*.................................................................................................................*/
	public static String createDirectoryForFiles(MesquiteModule module, String name) {
		MesquiteBoolean directoryCreated = new MesquiteBoolean(false);
		String rootDir = module.createEmptySupportDirectory(directoryCreated) + MesquiteFile.fileSeparator;  //replace this with current directory of file
		if (!directoryCreated.getValue()) {
			rootDir = MesquiteFile.chooseDirectory("Choose folder for storing "+name+" files");
			if (rootDir==null) {
				MesquiteMessage.discreetNotifyUser("Sorry, directory for storing "+name+" files could not be created.");
				return null;
			} else
				rootDir += MesquiteFile.fileSeparator;
		}
		return rootDir;
	}

  	
	/*.................................................................................................................*/
  	public String getNJTree(Taxa taxa, float[][] distanceMatrixFloat){
  		
  		
		String method = "default";
		File njTmpDir =  new File(createDirectoryForFiles(this, "ninja_temp_files"));
		
		//Added by TW 10/7/09 - simple method of giving numeric names to input seqs
 		String[] names = new String[distanceMatrixFloat.length];
/* 		for (int i=0; i<distanceMatrixFloat.length; i++)
 			names[i] = "" + (i+1); */
 		for (int i=0; i<distanceMatrixFloat.length; i++)
 			names[i] = "" + taxa.getTaxonName(i);  // 2025.July.05:  DRM added to ignore taxa with no data
 		
 		
 		TreeBuilder.verbose = 2; 		
		TreeBuilderManager manager = new TreeBuilderManager( method, njTmpDir, distanceMatrixFloat, names, this) ;
		String treeString = null; 
		try { 
			treeString = manager.doJob();
		} catch (FileNotFoundException e) {
			logln("Surprising error running NINJA tree manager\n");
			return null;
		}

  		return treeString;
  	}
  	
  	
	/*.................................................................................................................*/
  	public int fillTreeBlock(TreeVector treeList, int numberIfUnlimited){
 		if (treeList==null)
 			return ResultCodes.INPUT_NULL;
   		Taxa taxa = treeList.getTaxa();
   		distanceTask.initialize(taxa);
   		MesquiteTimer timer = new MesquiteTimer();
   		timer.start();
   		log("\n---------------\nCalculating distance matrix.  ");
 		TaxaDistance dist = distanceTask.getTaxaDistance(taxa);
 		double[][] distanceMatrix = dist.getReducedMatrix(true);  // 2025.July.05:  DRM added to ignore taxa with no data
 		Taxa reducedTaxa = dist.getReducedTaxa(true, true);  // 2025.July.05:  DRM added to ignore taxa with no data
		float[][] distanceMatrixFloat = new float[distanceMatrix.length][distanceMatrix.length];
		for (int i = 0; i<distanceMatrix.length; i++)
 	 		for (int j= 0; j<distanceMatrix.length; j++) {
 	 			distanceMatrixFloat[i][j] = (float) distanceMatrix[i][j];
 	 		}
				
 		
   		logln("["+ timer.timeSinceLastInSeconds()+" seconds]");
   		logln("NINJA: now calculating neighbor-joining tree.");
		String NJTree = getNJTree(reducedTaxa, distanceMatrixFloat);
   		logln("\nNINJA: completed neighbor-joining tree. ["+ timer.timeSinceLastInSeconds()+" seconds]");
   		logln("---------------\n");
		MesquiteTree tree = new MesquiteTree(taxa,NJTree, true);
		tree.setName(getName() + " tree");
		treeList.addElement(tree, false);
  		
		treeList.setName("Trees from NINJA Neighbor-Joining analysis (Distance: " + distanceTask.getName() + ")");
		treeList.setAnnotation ("Parameters: "  + getParameters() + ";   Distance: " + distanceTask.getParameters(), false);
		
		return ResultCodes.NO_ERROR;
		
  	}
	/*.................................................................................................................*/
   	public String getParameters() {
		return "Neighbor Joining via from " +  distanceTask.getName();
   	}
	/*.................................................................................................................*/
   	 public boolean hasLimitedTrees(Taxa taxa){
   	 	return true;
   	 }
 	/*.................................................................................................................*/
	 public String getName() {
	return "Neighbor Joining (NINJA)";
	 }
		/*.................................................................................................................*/
	 public String getNameForMenuItem() {
	return "Neighbor Joining (NINJA)...";
	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Supplies trees obtained from neighbor-joining analysis on distance matrices.";
   	 }
   	 
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
 	/*.................................................................................................................*/
   	 public boolean loadModule(){
   	 	return true;
   	 }
 	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
}

