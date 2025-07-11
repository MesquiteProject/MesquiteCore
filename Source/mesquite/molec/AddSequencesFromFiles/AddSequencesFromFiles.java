/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
 Perhaps with your help we can be more than a few, and make Mesquite better.

 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
 Mesquite's web site is http://mesquiteproject.org

 This source code and its compiled class files are free and modifiable under the terms of 
 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.AddSequencesFromFiles;
/*~~  */

import java.io.File;
import java.util.Vector;

import mesquite.categ.lib.DNAData;
import mesquite.lib.MainThread;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteModule;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.DataWindowMaker;
import mesquite.lib.duties.FileAssistantFM;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.ui.MesquiteWindow;

/* ======================================================================== */
public class AddSequencesFromFiles extends FileAssistantFM {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (Thread.currentThread() instanceof MainThread){
			MesquiteWindow w = ((MainThread)Thread.currentThread()).getWindowContext();
			MesquiteModule mb = w.getOwnerModule();
			if (mb instanceof DataWindowMaker){
				DataWindowMaker dwm = (DataWindowMaker)mb;
				CharacterData data = dwm.getCharacterData();
				if (data instanceof DNAData){
					getAndAddSequences(dwm.getTable());
					return true;
				}
			}
		}
		alert("To add sequences, you must have a character matrix editor window at the front");
		iQuit();
		return false;
	}

	void getAndAddSequences(MesquiteTable table){
		File[] files = MesquiteFile.openFilesDialog("Choose one or more FASTA, GenBank, or NBRF files with sequences to add to this matrix");
		if (files == null)
			return;
		Vector fileList = new Vector();
		for (int i = 0; i<files.length; i++)
			fileList.addElement(files[i]);
		table.processFilesDroppedOnPanel(fileList);
		
	}
	/*...............................................................................................................	*/
	public boolean requestPrimaryChoice(){
		return false;
	}

	/*.................................................................................................................*/
	public boolean isPrerelease() { 
		return false;
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Add Sequences from FASTA/NBRF/GenBank Files to Matrix...";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Add Sequences from FASTA/NBRF/GenBank Files to Matrix";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 400;  
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "If a DNA data matrix window is in front, gives user choice of files containing sequences to add to matrix.";  
	}

}


