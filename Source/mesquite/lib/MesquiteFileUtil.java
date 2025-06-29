/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib;

import java.io.File;

public class MesquiteFileUtil {

	/*.................................................................................................................*/
	public static String createDirectoryForFiles(MesquiteModule module, boolean askForLocation, String name) {
		MesquiteBoolean directoryCreated = new MesquiteBoolean(false);
		String rootDir = null;
		if (!askForLocation)
			rootDir = module.createEmptySupportDirectory(directoryCreated) + MesquiteFile.fileSeparator;  //replace this with current directory of file
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
	
	public static final int IN_SUPPORT_DIR = 0;
	public static final int BESIDE_HOME_FILE = 1;
	public static final int ASK_FOR_LOCATION = 2;
	public static final int IN_SUBDIRECTORY_BESIDE_HOME_FILE = 3;  // as of 4.0, not yet used

	/*.................................................................................................................*/
	public static String pathForFiles(String enclosingDirectoryPath, String name, String suffix, boolean createUniqueDatedName) {
		String path = enclosingDirectoryPath + StringUtil.cleanseStringOfFancyChars(name, false, true);
		if (createUniqueDatedName) {
			path+= "-" + StringUtil.getDateDayOnly() + suffix;
			path = MesquiteFile.getUniqueNumberedPath(path);
		}
		return path;
	}
	/*.................................................................................................................*/
	public static String createDirectoryForFiles(MesquiteModule module, int location, String subDirectoryName, String name, String suffix, boolean createUniqueDatedName) {
		MesquiteBoolean directoryCreated = new MesquiteBoolean(false);
		String rootDir = null;
		if (location == IN_SUPPORT_DIR)
			rootDir = module.createEmptySupportDirectory(directoryCreated) + MesquiteFile.fileSeparator;  //replace this with current directory of file
		else if (location == BESIDE_HOME_FILE) {
			String dir = module.getProject().getHomeFile().getDirectoryName();
			String path = pathForFiles(dir, name, suffix, createUniqueDatedName);
			File f = new File(path);
			boolean b = f.mkdir();
			directoryCreated.setValue(b);
			if (b)
				rootDir = path + MesquiteFile.fileSeparator;
		}
		else if (location == IN_SUBDIRECTORY_BESIDE_HOME_FILE) {
			String dir = module.getProject().getHomeFile().getDirectoryName();
			if (StringUtil.notEmpty(subDirectoryName))
				dir=dir+subDirectoryName +MesquiteFile.fileSeparator;
			File f;
			boolean b;
			if (!MesquiteFile.fileExists(dir)) {
				f = new File(dir);
				b = f.mkdir();
			}
			String path = pathForFiles(dir, name, suffix, createUniqueDatedName);
			f = new File(path);
			b = f.mkdir();
			directoryCreated.setValue(b);
			if (b)
				rootDir = path + MesquiteFile.fileSeparator;
		}
		if (!directoryCreated.getValue()) {
			String path = MesquiteFile.chooseDirectory("Choose folder for storing "+name+" files");
			if (path==null) {
				MesquiteMessage.discreetNotifyUser("Sorry, directory for storing "+name+" files could not be created.");
				return null;
			} else {
				path += MesquiteFile.fileSeparator;
				path = pathForFiles(path, name, suffix, createUniqueDatedName);
				File f = new File(path);
				boolean b = f.mkdir();
				directoryCreated.setValue(b);
				if (b)
					rootDir = path + MesquiteFile.fileSeparator;
			}
		}
		return rootDir;
	}
	/*.................................................................................................................*/
	public static String createDirectoryForFiles(MesquiteModule module, int location, String name, String suffix) {
		return createDirectoryForFiles( module,  location, null, name,  suffix, true);
	}
	/*.................................................................................................................*/
	public static String createDirectoryForFiles(MesquiteModule module, int location, String subDirectoryName, String name, String suffix) {
		return createDirectoryForFiles( module,  location, subDirectoryName, name,  suffix, true);
	}


}
