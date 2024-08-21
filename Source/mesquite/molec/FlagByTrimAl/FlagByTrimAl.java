/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.FlagByTrimAl;
/*~~  */




import mesquite.categ.lib.MolecularData;
import mesquite.categ.lib.RequiresAnyMolecularData;
import mesquite.lib.Bits;
import mesquite.lib.CommandChecker;
import mesquite.lib.CompatibilityTest;
import mesquite.lib.Debugg;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.Snapshot;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.MatrixFlags;
import mesquite.lib.duties.MatrixFlagger;

/* ======================================================================== */
public class FlagByTrimAl extends MatrixFlagger {

	static final int GAPPYOUT = 0;
	static final int STRICT = 1;
	static final int STRICTPLUS = 2;
	static final int AUTOMATED1 = 3;
	static final int autoOptionDEFAULT = AUTOMATED1;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		discreetAlert("This module does not yet run trimAl. You must run trimAl first, and then Mesquite will ask for the -colnumbering file from trimAl");
		addMenuItem("Read trimAl -colnumbering file...",  makeCommand("readColFile",  this));

		return true;
	}
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyMolecularData();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Presents options dialog box.", "[on or off]", commandName, "readColFile")) {
			readColsFile();
			parametersChanged();

		}


		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	String[] columns;

	void readColsFile(){
		String path = MesquiteFile.openFileDialog("Choose trimAl -colnumbering file", null, null);
		if (path == null)
			return;
		String columnsText = MesquiteFile.getFileContentsAsString(path);
		columns = columnsText.split(", ");
		columns[0] = columns[0].substring(12, columns[0].length());
		fileRead = true;
	}
	boolean fileRead = false;
	/*======================================================*/
	public MatrixFlags flagMatrix(CharacterData data, MatrixFlags flags) {
		if (data!=null && data.getNumChars()>0 && data instanceof MolecularData){
			if (flags == null)
				flags = new MatrixFlags(data);
			else 
				flags.reset(data);
			if (!fileRead)
				readColsFile();
			if (fileRead){
				Bits charFlags = flags.getCharacterFlags();
				int lastKeep = -1;
				for (int k = 0; k<columns.length; k++) {
					int keep = MesquiteInteger.fromString(columns[k]);
					for (int d = lastKeep+1; d<keep; d++)
						charFlags.setBit(d);
					lastKeep = keep;
				}
			}

		}

		return flags;

	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return true;
	}

	/*.................................................................................................................*/
	public boolean showCitation(){
		return false;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "trimAl";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Flags sites using trimAl" ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}


}



