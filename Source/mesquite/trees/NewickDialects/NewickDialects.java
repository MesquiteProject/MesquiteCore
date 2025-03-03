package mesquite.trees.NewickDialects;

import mesquite.lib.CommandChecker;
import mesquite.lib.Debugg;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.StringUtil;
import mesquite.lib.duties.MesquiteInit;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.NewickDialect;

import java.io.File;
import java.util.Vector;

import mesquite.externalCommunication.lib.*;

public class NewickDialects extends MesquiteInit {

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		harvestDialects();
		addMenuItem(MesquiteTrunk.helpMenu, "Newick Dialects", makeCommand("showDetails", this));
		return true;
	}
	StringBuffer details = new StringBuffer();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Shows Newick dialects known for tree file reading", null, commandName, "showDetails")) {
			logln("\n" + details.toString());
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	public void harvestDialects(){
		String dialectsDirPath = getInstallationSettingsPath();
		File dialectsDir = new File(dialectsDirPath);
		StringBuffer sb = new StringBuffer();
		if (dialectsDir.exists() && dialectsDir.isDirectory()) {
			MesquiteTree.dialects = new ListableVector();
			String[] dialectsFiles = dialectsDir.list();
			ListableVector.sort(dialectsFiles);
			for (int i=0; i<dialectsFiles.length; i++) {
				if (dialectsFiles[i]!=null && !dialectsFiles[i].equalsIgnoreCase("example.xml") && dialectsFiles[i].endsWith("xml")&& !dialectsFiles[i].startsWith(".")) {
					String dialectFilePath = dialectsDirPath + MesquiteFile.fileSeparator + dialectsFiles[i];
					NewickDialect dialect = new NewickDialect(dialectFilePath);
					boolean success = dialect.isReady();
					if (success) 
						MesquiteTree.dialects.addElement(dialect, false);
				}
			}
		}
	}

	public String getName() {
		return "Newick Dialects Harvester";
	}

}
