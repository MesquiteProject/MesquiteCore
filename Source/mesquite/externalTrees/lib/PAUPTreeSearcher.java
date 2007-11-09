package mesquite.externalTrees.lib;

import java.awt.event.*;
import java.awt.*;
import java.util.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.externalTrees.lib.*;

public abstract class PAUPTreeSearcher extends TreeSearcher implements ActionListener, PAUPCommander {
	PAUPRunner paupRunner;
	Taxa taxa;
	private MatrixSourceCoord matrixSourceTask;
	protected MCharactersDistribution observedStates;
	String PAUPPath;
	SingleLineTextField PAUPPathField =  null;
	boolean preferencesSet = false;

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		matrixSourceTask = (MatrixSourceCoord)hireCompatibleEmployee(MatrixSourceCoord.class, getCharacterClass(), "Source of matrix (for " + getName() + ")");
		if (matrixSourceTask == null)
			return sorry(getName() + " couldn't start because no source of matrix (for " + getName() + ") was obtained");

		loadPreferences();

		paupRunner = new PAUPRunner(this, PAUPPath, null);
		if (paupRunner ==null)
			return false;
		return true;
	}

	 public String getExtraTreeWindowCommands (){
   		 
   		String commands = "setSize 400 500; getTreeDrawCoordinator #mesquite.trees.BasicTreeDrawCoordinator.BasicTreeDrawCoordinator;\ntell It; ";
   		commands += "setTreeDrawer  #mesquite.trees.SquareTree.SquareTree; tell It; orientRight; ";
   		commands += "setNodeLocs #mesquite.trees.NodeLocsStandard.NodeLocsStandard;";
   		commands += " tell It; branchLengthsToggle on; endTell; ";
   		commands += " setEdgeWidth 3; endTell; endTell;";
   		return commands;
   	 }

	/*.................................................................................................................*/
	public Class getCharacterClass() {
		return null;
	}

	public void initialize(Taxa taxa) {
		this.taxa = taxa;
		if (matrixSourceTask!=null) {
			matrixSourceTask.initialize(taxa);
			if (observedStates ==null)
				observedStates = matrixSourceTask.getCurrentMatrix(taxa);
		}
		if (paupRunner ==null)
			paupRunner = new PAUPRunner(this, PAUPPath, null);
	}

	public boolean getPreferencesSet() {
		return preferencesSet;
	}
	public void setPreferencesSet(boolean b) {
		preferencesSet = b;
	}
	/*.................................................................................................................*/
	public void processMorePreferences (String tag, String content) {
	}
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("PAUPPath".equalsIgnoreCase(tag)) 
			PAUPPath = StringUtil.cleanXMLEscapeCharacters(content);
		processMorePreferences(tag, content);
		preferencesSet = true;
	}
	/*.................................................................................................................*/
	public String prepareMorePreferencesForXML () {
		return "";
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "PAUPPath", PAUPPath);  
		buffer.append(prepareMorePreferencesForXML());

		preferencesSet = true;
		return buffer.toString();
	}

	public void queryOptionsSetup(ExtensibleDialog dialog) {
	}
	/*.................................................................................................................*/
	public void queryOptionsProcess(ExtensibleDialog dialog) {
	}

	/*.................................................................................................................*/
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), getName() + " Options",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		dialog.addLabel(getName() + " Options and Location");
		String helpString = "This module will prepare a matrix for PAUP, and ask PAUP do to an analysis.  A command-line version of PAUP must be installed. ";

		dialog.appendToHelpString(helpString);

		queryOptionsSetup(dialog);

		dialog.addHorizontalLine(1);
		PAUPPathField = dialog.addTextField("Path to PAUP:", PAUPPath, 40);
		Button PAUPBrowseButton = dialog.addAListenedButton("Browse...",null, this);
		PAUPBrowseButton.setActionCommand("PAUPBrowse");

		//TextArea PAUPOptionsField = queryFilesDialog.addTextArea(PAUPOptions, 20);

		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			PAUPPath = PAUPPathField.getText();
			queryOptionsProcess(dialog);
			storePreferences();



		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	public  void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("PAUPBrowse")) {
			MesquiteString directoryName = new MesquiteString();
			MesquiteString fileName = new MesquiteString();
			PAUPPath = MesquiteFile.openFileDialog("Choose PAUP", directoryName, fileName);
			PAUPPathField.setText(PAUPPath);
		}
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	private TreeVector getTrees(Taxa taxa) {
		TreeVector trees = new TreeVector(taxa);

		CommandRecord.tick("PAUP Tree Search in progress " );

		Random rng = new Random(System.currentTimeMillis());
		paupRunner.setPAUPPath(PAUPPath);

		MesquiteDouble finalScore = new MesquiteDouble();

		paupRunner.getTrees(trees, taxa, observedStates, rng.nextInt(), finalScore, this);

		return trees;
	}

	/*.................................................................................................................*/
	public void fillTreeBlock(TreeVector treeList){
		if (treeList==null || paupRunner==null)
			return;
		taxa = treeList.getTaxa();
		initialize(taxa);


		if (!queryOptions())
			return;

		boolean pleaseStorePref = false;
		if (!preferencesSet)
			pleaseStorePref = true;
		if (StringUtil.blank(PAUPPath)) {
			MesquiteString directoryName = new MesquiteString();
			MesquiteString fileName = new MesquiteString();
			PAUPPath = MesquiteFile.openFileDialog("Choose PAUP", directoryName, fileName);
			if (StringUtil.blank(PAUPPath))
				return;
			PAUPPath= directoryName.getValue();
			if (!PAUPPath.endsWith(MesquiteFile.fileSeparator))
				PAUPPath+=MesquiteFile.fileSeparator;
			PAUPPath+=MesquiteFile.fileSeparator+fileName.getValue();
			pleaseStorePref = true;
		}
		mesquiteTrunk.incrementProjectBrowserRefreshSuppression();
		if (pleaseStorePref)
			storePreferences();

		TreeVector trees = getTrees(taxa);
		treeList.setName("Trees from PAUP search");
		treeList.setAnnotation ("Parameters: "  + getParameters(), false);
		if (trees!=null)
			treeList.addElements(trees, false);
		paupRunner=null;
	}



}
