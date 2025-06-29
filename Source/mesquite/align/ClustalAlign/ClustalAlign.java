/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.align.ClustalAlign;
/*~~  */

import mesquite.align.lib.ExternalSequenceAligner;
import mesquite.categ.lib.MolecularData;
import mesquite.categ.lib.ProteinData;
import mesquite.lib.CommandChecker;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;

/* ======================================================================== */
public class ClustalAlign extends ExternalSequenceAligner{
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}	
	/*.................................................................................................................*/
	public boolean showCitation() {
		return true;
	}
	/*.................................................................................................................*/
	public String getCitation()  {
		return "Please remember to cite the version of ClustalW you used.";
	}

	/*.................................................................................................................*/
	public String getHelpString() {
		String s = "<i>You must use the command line version of Clustal, i.e., ClustalW.</i><br><br>"
			+ " \nIn the Clustal Options field, place any Clustal options you wish to use.  For example, if you wished to change the"
			+ " gap opening cost to 8 and the gap extension cost to 3, the options would be \"-gapopen=8 -gapext=3\".";
		s+= "<br><br>Note that ClustalAlign has changed; you now need to specify the full path to Clustal, including the name of the program, not just the "
			+ "directory in which the program resides.";
		return s;
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
	public String getName() {
		return "ClustalW Align";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "ClustalW Align...";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Sends the selected sequence to ClustalW to align." ;
	}
	
	public String getProgramName(){
		 return "ClustalW";
	 }
	/*.................................................................................................................*/
	public boolean useDefaultStdOutFileName() {
		return true;
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = super.getSnapshot(file);
		if (temp == null)
			temp = new Snapshot();
		temp.addLine("optionsSet");
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Records that options set", "", commandName, "optionsSet")) {
			optionsAlreadySet = true;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}	
	
	/*.................................................................................................................*/
	public String getProgramCommand(){
		if (MesquiteTrunk.isWindows())
			return "call " + StringUtil.protectFilePathForWindows(getProgramPath());
		else
			return StringUtil.protectFilePathForUnix(getProgramPath());
	}
	/*.................................................................................................................*/
	public String getDefaultProgramOptions(){
		return "";
	}
	
	public void appendDefaultOptions(StringBuffer shellScript, String inFilePath, String outFilePath, MolecularData data) {
		shellScript.append("  -infile=" + StringUtil.protectFilePathForUnix(inFilePath) + " -align -output=pir -outfile="+ StringUtil.protectFilePathForUnix(outFilePath) );
		if (data instanceof ProteinData)
			shellScript.append(" -type=protein ");
		else
			shellScript.append(" -type=dna ");

	}

	
	public String getDNAExportInterpreter () {
		return "#InterpretNBRFDNA";
	}
	public String getProteinExportInterpreter () {
		return "#InterpretNBRFProtein";
	}
	public String getDNAImportInterpreter () {
		return "#InterpretNBRFDNA";
	}
	public String getProteinImportInterpreter () {
		return "#InterpretNBRFProtein";
	}
	public  String getExportExtension() {
		return ".nbrf";
	}
	public  String getImportExtension() {
		return ".nbrf";
	}


	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return -100;  
	}

}

