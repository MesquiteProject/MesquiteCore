package mesquite.externalCommunication.lib;

import mesquite.lib.*;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.SingleLineTextField;

public class PythonUtil {
	public static String python2Path = "";
	public static String python3Path = "";
	public static int pythonPreference = 3;
	public static int NOPYTHONPREFERENCE = 0;

	public static boolean pythonSettings(MesquiteModule ownerModule) {
		MesquiteInteger buttonPressed = new MesquiteInteger(ExtensibleDialog.defaultCANCEL);
		ExtensibleDialog dialog = new ExtensibleDialog(ownerModule.containerOfModule(), "Python Settings",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		String s = "To find the path to Python 2, go in to the Terminal or Command Line, and type \"python2 -h\" and hit return. "
				+ "The response should include a line that begins with \"usage: \", then shows the full path to Python 2, then "
				+ "has \"[options]\"; the path is the text between \"usage\" and \"[options]\". Copy that text and paste it into the "
				+ "Python 2 path field.  Then do the same for Python 3, this time using the command \"python3 -h\". ";
		dialog.appendToHelpString(s);

		dialog.addHorizontalLine(1);

		SingleLineTextField python2PathField = dialog.addTextField("Python 2 path", python2Path, 50);
		SingleLineTextField python3PathField = dialog.addTextField("Python 3 path", python3Path, 50);


		dialog.completeAndShowDialog(true);
		boolean success=(buttonPressed.getValue()== dialog.defaultOK);
		if (success)  {
			python2Path = StringUtil.stripBoundingWhitespace(python2PathField.getText());
			python3Path = StringUtil.stripBoundingWhitespace(python3PathField.getText());
			MesquiteTrunk.mesquiteTrunk.storePreferences();
		}
		dialog.dispose();
		return success;

	}

	public static String pythonVersion(MesquiteModule ownerModule, int version) {
		if (version==2 || (version==0 && pythonPreference==2))
			return ExternalProcessManager.executeAndGetStandardErr(ownerModule, getPythonPath(2), "-V");
		else 
			return ExternalProcessManager.executeAndGetStandardOut(ownerModule, getPythonPath(3), "-V");
		//		String pythonVersionStOut= ExternalProcessManager.executeAndGetStandardErr(this, "python", "-V");
		/*		String raxmlng= ExternalProcessManager.executeAndGetStandardOut(this, "/usr/local/bin/raxml-ng", "-v");
		logln("\nraxml-ng version: " + raxmlng);
		String iqTreeV= ExternalProcessManager.executeAndGetStandardOut(this, "/usr/local/bin/iqtree2", "-V");
		logln("\niq-tree version: " + iqTreeV);
		 */



		/*		scriptRunner = new ShellScriptRunner(scriptPath, runningFilePath, null, true, getName(), outputFilePaths, this, this, true);  //scriptPath, runningFilePath, null, true, name, outputFilePaths, outputFileProcessor, watcher, true
		success = scriptRunner.executeInShell();
		if (success)
			success = scriptRunner.monitorAndCleanUpShell(progressIndicator);
		 */

	}

	public static boolean python2Available() {
		return StringUtil.notEmpty(python2Path);
	}


	public static boolean python3Available() {
		return StringUtil.notEmpty(python3Path);
	}

	public static boolean pythonAvailable() {
		return python2Available() || python3Available();
	}

	public static String getPythonPath(int version) {
		if (version==3 || (version==0 && pythonPreference==3))
			return python3Path;
		else if (version==2 || (version==0 && pythonPreference==2))
			return python2Path;
		else
			return python3Path;
	}

}
