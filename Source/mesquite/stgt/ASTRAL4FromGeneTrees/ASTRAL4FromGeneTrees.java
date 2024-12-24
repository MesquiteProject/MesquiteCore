/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.stgt.ASTRAL4FromGeneTrees;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.ImageObserver;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.stgt.SpeciesTreeGeneTreeSearcher.SpeciesTreeGeneTreeSearcher;
import mesquite.stgt.lib.ASTRALFromGeneTrees;
import mesquite.stgt.lib.ASTRALLiaison;
import mesquite.stgt.lib.SpeciesTreeGeneTreeAnalysis;
import mesquite.distance.lib.*;
import mesquite.externalCommunication.lib.AppChooser;
import mesquite.io.lib.IOUtil;

/* ======================================================================== */
public class ASTRAL4FromGeneTrees extends ASTRALFromGeneTrees {  
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return super.startJob(arguments, condition, hiredByName);
	}

	protected  String getOfficialAppNameInAppInfo() {
		return "ASTRAL4";
	}
	protected  String getProgramNameForDisplay() {
		return "ASTRAL-IV";
	}
	protected String executionCommand(String ASTRALPath, String geneTreesPath, String outputPath, String logPath) {
		String command = StringUtil.protectFilePath(ASTRALPath) + "  -u 2 -o " + outputPath + " " + geneTreesPath  + " 2> " +logPath + "\n";
		return command;
	}


}


