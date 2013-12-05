package mesquite.lib;

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

}
