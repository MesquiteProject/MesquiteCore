/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.minimal.OpenURL;
/*~~  */

import java.awt.*;
import java.net.*;
import java.util.*;
import java.io.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

public class OpenURL extends GeneralFileMaker  {
	public String getNameForMenuItem() {
		return "URL...";
	}
	public String getName() {
		return "Open URL";
	}
	public String getExplanation() {
		return "Opens a file on the web as if it were a local data file";
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	/** make a new    MesquiteProject.*/
	/*.................................................................................................................*/
	public MesquiteProject establishProject(String arguments){
		FileCoordinator fileCoord = getFileCoordinator();
		MesquiteFile thisFile = new MesquiteFile();
		if (arguments ==null)
			arguments = MesquiteString.queryString(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Open URL", "URL to open:", "");
		URL url=null;

		String urlString = null;
		if (!StringUtil.blank(arguments)) {
			try {
				url = new URL(arguments);
				urlString = url.toString();
				String fileName = StringUtil.getLastItem(urlString, MesquiteFile.fileSeparator, "/");
				String dirName = StringUtil.getAllButLastItem(urlString, MesquiteFile.fileSeparator, "/") + MesquiteFile.fileSeparator;
				thisFile.setLocs(false, url, fileName, dirName);

			}
			catch (MalformedURLException e) {MesquiteModule.mesquiteTrunk.discreetAlert( MesquiteThread.isScripting(),"Bad URL specified for data file: \"" + arguments + "\"");}
		}
		else return null;
		MesquiteProject p = null;



		if (thisFile!=null && !StringUtil.blank(thisFile.getFileName())) {
			FileInterpreter fileInterp;
			logln("Location: " + urlString);
			logln("");
			boolean imp = false; //was it imported???
			//first try nexus.  If can't be read, then make list and query user...
			NexusFileInterpreter nfi = (NexusFileInterpreter)fileCoord.findImmediateEmployeeWithDuty(NexusFileInterpreter.class);
			if (nfi!=null && nfi.canReadFile(thisFile))
				fileInterp = nfi;
			else {
				imp = true;
				fileInterp = fileCoord.findImporter(thisFile, 0, arguments);
			}
			if (fileInterp !=null) {
				p = fileCoord.initiateProject(thisFile.getFileName(), thisFile);
				
				MesquiteFile sf = CommandRecord.getScriptingFileS();
				if (MesquiteThread.isScripting())
					CommandRecord.setScriptingFileS(thisFile);
				fileInterp.readFile(getProject(), thisFile, arguments);
				CommandRecord.setScriptingFileS(sf);

				if (thisFile.isClosed()){
					if (p !=null)
						p.developing = false;
					return null;
				}
				else {
					p.fileSaved(thisFile);
				}
			}
			else {
				alert("Sorry, an interpreter was not found for this file");
			}
		}
		return p;
	}
}

