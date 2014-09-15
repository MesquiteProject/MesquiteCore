/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.ListedTreeBlocks;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class ListedTreeBlocks extends TreeBlockSource {
	int currentTreeBlockIndex=0;
	TreeVector currentTreeBlock = null;
	TreeVector lastUsedTreeBlock = null;
	Taxa preferredTaxa =null;
	Taxa currentTaxa = null;
	MesquiteInteger pos = new MesquiteInteger(0);
	String pathToList = null;
	String directoryOfList = "";
	Vector fileList = null;
	String exporter = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
	    	currentTreeBlockIndex = 0;
		if (!MesquiteThread.isScripting()){ //file dialog to choose picture
			MesquiteString dir = new MesquiteString();
			MesquiteString f = new MesquiteString();
			
   	 		String path = MesquiteFile.openFileDialog("File with list of tree files", dir, f);
   	 		String d = dir.getValue();
   	 		if (!StringUtil.blank(d) && !StringUtil.blank(f.getValue())) {
   	 			if (!d.endsWith("/")) 
   	 				d += "/";
   	 			directoryOfList = d;
   	 			boolean success;
   	 			if (getProject().getHomeDirectoryName().equalsIgnoreCase(d)){
   	 				pathToList = f.getValue();
   	 				String fullPathToList = MesquiteFile.composePath(getProject().getHomeDirectoryName(), pathToList);
   	 				if (!MesquiteFile.fileExists(fullPathToList))
   	 					return sorry(getName() + " couldn't start because file listing tree files not found.");
   	 				success = readListFile(fullPathToList);
   	 			}
   	 			else {
   	 				pathToList = path;
   	 				if (!MesquiteFile.fileExists(path))
   	 					return sorry(getName() + " couldn't start because file listing tree files not found.");
					success = readListFile(pathToList);
				}
				if (!success)
					return sorry(getName() + " couldn't start because of a problem with the file listing tree files.");
   	 		}
   	 		else 
   	 			return sorry(getName() + " couldn't start because no file listing tree files was specified.");
   	 	}
		return true;
  	 }
	private boolean readListFile(String path){
		String list = MesquiteFile.getFileContentsAsString(path);
		if (list==null)
			return false;
		exporter = parser.getFirstToken(list);
		fileList = new Vector();
		String token;
		while (!StringUtil.blank(token = parser.getNextToken())) {
			fileList.addElement(token);
		}
		return fileList.size()>0;
	}
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
    	 	Snapshot temp = new Snapshot();
  	 	
		temp.addLine("setList " + StringUtil.tokenize(directoryOfList + pathToList)); //TODO: this should do relative to home file, not absolute
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
     	 	 if (checker.compare(this.getClass(), "Sets the path to the file listing tree files", "[path to file; if relative, should be relative to home file of project]", commandName, "setList")){
   	 		pathToList = ParseUtil.getFirstToken(arguments, pos);
  			boolean success = readListFile(MesquiteFile.composePath(getProject().getHomeDirectoryName(), pathToList));
  			if (!success)
  				iQuit();
    	 	 } 
      	 	 
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
    	 	
	return null;
   	 }
	/*.................................................................................................................*/
  	public void setPreferredTaxa(Taxa taxa){
   		if (taxa !=currentTaxa) {
	  		currentTaxa = taxa;
  		}
  		
  	}
   	/*.................................................................................................................*/
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(Taxa taxa){
   		setPreferredTaxa(taxa);
   	}
	/*.................................................................................................................*/
   	public TreeVector getFirstBlock(Taxa taxa) {
   		setPreferredTaxa(taxa);
   		currentTreeBlockIndex=0;
   		return getCurrentBlock(taxa);
   	}
	/*.................................................................................................................*/
   	public TreeVector getBlock(Taxa taxa, int ic) {
   		setPreferredTaxa(taxa);
   		currentTreeBlockIndex=ic;
   		return getCurrentBlock(taxa);
   	}
   	private String fileName(int ic){
   		if (fileList == null || ic >= fileList.size())
   			return "";
   		else
   			return (String)fileList.elementAt(ic);
   	}
	/*.................................................................................................................*/
   	public TreeVector getCurrentBlock(Taxa taxa) {
   		setPreferredTaxa(taxa);
   		if (taxa == null)
   			return null;
 		incrementMenuResetSuppression();
		TreeVector newTrees = new TreeVector(taxa);
 		MesquiteModule fCoord = getFileCoordinator();
 		CommandRecord cr = new CommandRecord(true);
 		CommandRecord prevR = MesquiteThread.getCurrentCommandRecord();
 		MesquiteThread.setCurrentCommandRecord(cr);
 		MesquiteFile file = (MesquiteFile)fCoord.doCommand("linkTreeFile", StringUtil.tokenize(directoryOfList+fileName(currentTreeBlockIndex)) + "  " + StringUtil.tokenize(exporter), CommandChecker.defaultChecker);
		MesquiteThread.setCurrentCommandRecord(prevR);
		if (file == null) {
			MesquiteMessage.warnProgrammer("file not found, directory (" + directoryOfList + ")  file (" + fileName(currentTreeBlockIndex) + ") currentTreeBlockIndex " + currentTreeBlockIndex);
	 		decrementMenuResetSuppression();
			return newTrees;
		}
		ListableVector vectors = file.getFileElements();
		if (vectors !=null && vectors.size()>0) {
	   		for (int i = 0; i<vectors.size(); i++){
	   			if (vectors.elementAt(i) instanceof TreeVector){
		   			TreeVector trees =  (TreeVector)vectors.elementAt(i);
		   			if (trees.getTaxa().equals(taxa)) {
				 		for (int t=0; t<trees.size(); t++){
				 			newTrees.addElement(trees.elementAt(t), false);
				 		}
				 		getProject().removeFile(file);
				 		decrementMenuResetSuppression();
		   				return newTrees;
		   			}
	   			}
			}
		}
		
 		decrementMenuResetSuppression();
		return newTrees;
		
   	}
	/*.................................................................................................................*/
   	public TreeVector getNextBlock(Taxa taxa) {
   		setPreferredTaxa(taxa);
   		currentTreeBlockIndex++;
   		return getCurrentBlock(taxa);
   	}
	/*.................................................................................................................*/
   	public int getNumberOfTreeBlocks(Taxa taxa) {
   		setPreferredTaxa(taxa);
   		if (fileList == null)
   			return 0;
   		return fileList.size();
   	}
   
	/*.................................................................................................................*/
   	public String getTreeBlockNameString(Taxa taxa, int index) {
   		setPreferredTaxa(taxa);
		return "Tree block from " + fileName(index);
   	}
		/*.................................................................................................................*/
    	 public String getName() {
		return "Tree Blocks from Files Listed In File";
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Supplies tree blocks in files listed in a file." ;
   	 }
	/*.................................................................................................................*/
   	public String getParameters() {
			return "File containing list of tree files: " + pathToList;
   	}
	/*.................................................................................................................*/
}

