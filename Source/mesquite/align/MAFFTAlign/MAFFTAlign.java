package mesquite.align.MAFFTAlign;

/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
/*~~  */

import java.util.*;
import java.lang.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.lib.table.*;
import mesquite.align.lib.*;

/* ======================================================================== */
public class MAFFTAlign extends ExternalSequenceAligner{
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}	
	/*.................................................................................................................*/
	public boolean showCitation() {
		return true;
	}
	/*.................................................................................................................*/
	public String getCitation()  {
		return "Please remember to cite the version of MAFFT you used.";
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
		return "MAFFT Align";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "MAFFT Align...";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Sends the selected sequence to MAFFT to align." ;
	}
	
	public String getProgramName(){
		 return "mafft.bat";
	 }
	
	/*.................................................................................................................*/
	public String getHelpString() {
	  String s =  " In the MAFFT Options field, place any MAFFT options you wish to use.  For example, if you wished to change the"
		  + " gap opening cost to 3 and the gap extension cost to 8, the options would be \"-gapopen -8.0  -gapextend -3.0\".";
	  return s;
	}

	/*.................................................................................................................*/
	public String getProgramCommand(){
		if (MesquiteTrunk.isWindows())
			return StringUtil.protectForWindows(getProgramPath());
		else
			return StringUtil.protectForUnix(getProgramPath());
	}
	public boolean programOptionsComeFirst(){
		return true;  
	}
	/*.................................................................................................................*/
	public String getDefaultProgramOptions(){
		return "";
	}
	
	public void appendDefaultOptions(StringBuffer shellScript, String inFilePath, String outFilePath, MolecularData data) {
	if (!MesquiteTrunk.isWindows())
		shellScript.append("  " + StringUtil.protectForUnix(inFilePath) + " > " + StringUtil.protectForUnix(outFilePath));
	else
		shellScript.append("  " + StringUtil.protectForWindows(inFilePath) + " > " + StringUtil.protectForWindows(outFilePath));  //Debugg.println need to check MAFFT windows format
	}

	
	public String getDNAExportInterpreter () {
		return "#InterpretFastaDNA";
	}
	public String getProteinExportInterpreter () {
		return "#InterpretFastaProtein";
	}
	public String getDNAImportInterpreter () {
		return "#InterpretFastaDNA";
	}
	public String getProteinImportInterpreter () {
		return "#InterpretFastaProtein";
	}
	public  String getExportExtension() {
		return ".fas";
	}
	public  String getImportExtension() {
		return ".fas";
	}


	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 276;  
	}

}
