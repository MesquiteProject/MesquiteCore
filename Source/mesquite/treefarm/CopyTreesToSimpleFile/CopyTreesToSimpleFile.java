/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 



Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.treefarm.CopyTreesToSimpleFile; 

import java.awt.FileDialog;

import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class CopyTreesToSimpleFile extends FileProcessor {
	String saveFile = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false; 
	}

	/** if returns true, then requests to remain on even after alterFile is called.  Default is false*/
	public boolean pleaseLeaveMeOn(){
		return false;
	}
	/*.................................................................................................................*/
	/** Called to alter file. */
	public int processFile(MesquiteFile file){
		
		if (saveFile == null || okToInteractWithUser(CAN_PROCEED_ANYWAY, "Asking for file to save")){ //need to check if can proceed
			
			MesquiteFileDialog fdlg= new MesquiteFileDialog(containerOfModule(), "Output File for Tree(s)", FileDialog.SAVE);
			fdlg.setBackground(ColorTheme.getInterfaceBackground());
			fdlg.setVisible(true);
			String fileName=fdlg.getFile();
			String directory=fdlg.getDirectory();
			if (StringUtil.blank(fileName) || StringUtil.blank(directory))
				return 2;
			saveFile = MesquiteFile.composePath(directory, fileName);
		}
		if (saveFile == null)
			return 2;
		Listable[] treeVectors = proj.getFileElements(TreeVector.class);	
   		if (treeVectors == null)
   			return 1;
		MesquiteFile.putFileContents(saveFile, "[trees from Process Data Files]  " , true);
		for (int im = 0; im < treeVectors.length; im++){
   			TreeVector trees = (TreeVector)treeVectors[im];
   			if (trees.getFile() == file){
   				for (int itree = 0; itree < trees.size(); itree++){
   					Tree t = trees.getTree(itree);
   					String description = t.writeTree(Tree.BY_NAMES);
   					MesquiteFile.appendFileContents(saveFile, "[tree " + (itree+1) + " from file " + file.getFileName() + "]  " , true);
  					
   					MesquiteFile.appendFileContents(saveFile, description + StringUtil.lineEnding(), true);
  				}
   			}
   		}
		return 0;

	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 310;  
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Compile Trees into Simple File";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Compiles trees from this file into a simple text file." ;
	}

}


