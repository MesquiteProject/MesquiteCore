package mesquite.externalTrees.PAUPParsimony;

import java.awt.*;

import mesquite.lib.*;
import mesquite.externalTrees.lib.*;

public class PAUPParsimony extends PAUPTreeSearcher {
	int bootStrapReps = 500;
	boolean doBootstrap = false;
	boolean getConsensus = false;
	String hsOptions = "";
	protected String paupCommands = "";

	public String getExtraTreeWindowCommands (){

		String commands = "setSize 400 600; getTreeDrawCoordinator #mesquite.trees.BasicTreeDrawCoordinator.BasicTreeDrawCoordinator;\ntell It; ";
		commands += "setTreeDrawer  #mesquite.trees.SquareTree.SquareTree; tell It; orientRight; ";
		commands += "setNodeLocs #mesquite.trees.NodeLocsStandard.NodeLocsStandard;";
		commands += " setEdgeWidth 3; endTell; ";
		if (doBootstrap)
			commands += "labelBranchLengths on; setNumBrLenDecimals 0; showBrLenLabelsOnTerminals off; showBrLensUnspecified off; setBrLenLabelColor 0 0 0;";
		commands += " endTell; ladderize root; ";
		return commands;
	}


	/*.................................................................................................................*/
	public void processMorePreferences (String tag, String content) {
		if ("bootStrapReps".equalsIgnoreCase(tag))
			bootStrapReps = MesquiteInteger.fromString(content);
		if ("bootstrap".equalsIgnoreCase(tag))
			doBootstrap = MesquiteBoolean.fromTrueFalseString(content);
		if ("getConsensus".equalsIgnoreCase(tag))
			getConsensus = MesquiteBoolean.fromTrueFalseString(content);
		if ("hsOptions".equalsIgnoreCase(tag))
			hsOptions = StringUtil.cleanXMLEscapeCharacters(content);
		if ("paupCommands".equalsIgnoreCase(tag))
			paupCommands = StringUtil.cleanXMLEscapeCharacters(content);
}
	/*.................................................................................................................*/
	public String prepareMorePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "bootStrapReps", bootStrapReps);  
		StringUtil.appendXMLTag(buffer, 2, "bootstrap", doBootstrap);  
		StringUtil.appendXMLTag(buffer, 2, "getConsensus", getConsensus);  
		StringUtil.appendXMLTag(buffer, 2, "hsOptions", hsOptions);  
		StringUtil.appendXMLTag(buffer, 2, "paupCommands", paupCommands);  
	return buffer.toString();
	}

	Checkbox bootstrapBox;
	Checkbox getConsensusBox;
	IntegerField bootStrapRepsField;
	SingleLineTextField hsOptionsField;
	TextArea paupCommandsField;

	/*.................................................................................................................*/
	public void queryOptionsSetup(ExtensibleDialog dialog) {
		String helpString = "\nIf \"bootstrap\" is on, the PAUP will do a parsimony bootstrap of the number of replicates specified; otherwise, it will do a parsimony heuristic search.";
		helpString+= "\nAny PAUP commands entered in the Additional Commands field will be executed in PAUP immediately before the bootstrap or hs command.";
		dialog.appendToHelpString(helpString);

		hsOptionsField = dialog.addTextField("Heuristic search options:", hsOptions, 24);
		getConsensusBox = dialog.addCheckBox("only read in consensus", getConsensus);

		bootstrapBox = dialog.addCheckBox("bootstrap", doBootstrap);
		bootStrapRepsField = dialog.addIntegerField("Bootstrap Reps", bootStrapReps, 8, 1, MesquiteInteger.infinite);
		
		dialog.addLabel("Additional commands before hs or bootstrap command: ");
		paupCommandsField =dialog.addTextAreaSmallFont(paupCommands,4);

	}

	/*.................................................................................................................*/
	public void queryOptionsProcess(ExtensibleDialog dialog) {
		bootStrapReps = bootStrapRepsField.getValue();
		doBootstrap = bootstrapBox.getState();
		getConsensus = getConsensusBox.getState();
		hsOptions = hsOptionsField.getText();
		paupCommands = paupCommandsField.getText();
	}

	/*.................................................................................................................*/
	public String getPAUPCommandFileMiddle(String dataFileName, String outputTreeFileName){
		StringBuffer sb = new StringBuffer();
		sb.append("\texec " + StringUtil.tokenize(dataFileName) + ";\n");
		sb.append("\tset criterion=parsimony;\n");
		if (doBootstrap && bootStrapReps>0) {
			sb.append("\tdefaults hs " + hsOptions + ";\n");
			sb.append(paupCommands+"\n");
			sb.append("\tboot nreps = " + bootStrapReps + " search=heuristic;\n");
			sb.append("\tsavetrees from=1 to=1 SaveBootP=brlens file=" + StringUtil.tokenize(outputTreeFileName) + ";\n");
		}
		else {
			sb.append(paupCommands+"\n");
			sb.append("\ths " + hsOptions + ";\n");
			if (getConsensus)
				sb.append("\tcontree all/strict=yes treefile=" + StringUtil.tokenize(outputTreeFileName) + ";\n");
			else
				sb.append("\tsavetrees file=" + StringUtil.tokenize(outputTreeFileName) + ";\n");
		}
		return sb.toString();
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean requestPrimaryChoice(){
		return true;
	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}


	public String getExplanation() {
		return "If PAUP is installed, will save a copy of a character matrix and script PAUP to conduct a parsimony search, and harvest the resulting trees.";
	}
	public String getName() {
		return "Parsimony Search (PAUP)";
	}
	public String getNameForMenuItem() {
		return "Parsimony Search (PAUP)...";
	}


}
