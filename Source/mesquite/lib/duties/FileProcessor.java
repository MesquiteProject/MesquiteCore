/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


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
import mesquite.molec.lib.SequenceTrimmer;
import mesquite.lib.characters.*;


/* ======================================================================== */
/**This is superclass of modules to alter a file (e.g. to process matrices within the file).*/

public abstract class FileProcessor extends MesquiteModule  {
	boolean sequester = false;

   	 public Class getDutyClass() {
   	 	return FileProcessor.class;
   	 }
  	 public String[] getDefaultModule() {
 	 	return new String[] {"#AlterAllMatrices", "#CompileProcessedMatrices", "#FAFillTreeBlock", "#CopyTreesToNexusBlock", "#CopyTreesToSimpleFile"};
 	 }
	public String getDutyName() {
 		return "File Processor";
   	}
   	
 	public String getNameForProcessorList() {
 		return getName();
   	}
   	/** if returns true, then requests to remain on even after alterFile is called.  Default is false*/
   	public boolean pleaseLeaveMeOn(){
   		return false;
   	}
	/*.................................................................................................................*/
   	/** Called to inform module what is base directory of files.*/
   	public void setBaseDirectory(String path){
   	}
   	
   	protected String previousProcessorLabel = null;
   	public void setPreviousProcessorLabel(String prevLabel) {
   		previousProcessorLabel = prevLabel;
   	}
	/*.................................................................................................................*/
   	/** Called before processing a series of files.*/
   	public boolean beforeProcessingSeriesOfFiles(){
   		return true;
   	}
	/*.................................................................................................................*/
   	/** Called to process file. Override this or the next*/
   	public int processFile(MesquiteFile file){
   		return 0;
   	}
  	
	/*.................................................................................................................*/
   	/** Called after processing a series of files.*/
   	public  boolean afterProcessingSeriesOfFiles(){
   		return true;
   	}
	/*.................................................................................................................*/
   	/** If a processor wants, it can request to have the file sequestered once all processing is done*/
   	public  boolean pleaseSequester(){
   		return sequester;
   	}
	/*.................................................................................................................*/
   	public  void setPleaseSequester(boolean sequester){
   		this.sequester= sequester;
   	}
  	
	/*.................................................................................................................*/
	/** Called to process file. */
	public int processFile(MesquiteFile file, MesquiteString notice){
		return processFile(file);
	}

}





