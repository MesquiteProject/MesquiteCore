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
	/*.................................................................................................................*/
	public static String createDirectoryForFiles(MesquiteModule module, int location, String name, String suffix) {
		MesquiteBoolean directoryCreated = new MesquiteBoolean(false);
		String rootDir = null;
		if (location == IN_SUPPORT_DIR)
			rootDir = module.createEmptySupportDirectory(directoryCreated) + MesquiteFile.fileSeparator;  //replace this with current directory of file
		else if (location == BESIDE_HOME_FILE) {
			String dir = module.getProject().getHomeFile().getDirectoryName();

			String path = dir + name + "-" + StringUtil.getDateDayOnly() + suffix;
			path = MesquiteFile.getUniqueNumberedPath(path);
			File f = new File(path);
			boolean b = f.mkdir();
			directoryCreated.setValue(b);
			if (b)
				rootDir = path + MesquiteFile.fileSeparator;
		}
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


}
