/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.FlagByTrimAl;
/*~~  */




import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import mesquite.categ.lib.MolecularData;
import mesquite.categ.lib.RequiresAnyMolecularData;
import mesquite.lib.Bits;
import mesquite.lib.CommandChecker;
import mesquite.lib.CompatibilityTest;
import mesquite.lib.Debugg;
import mesquite.lib.ExtensibleDialog;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteThread;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.OutputFileProcessor;
import mesquite.lib.ShellScriptRunner;
import mesquite.lib.ShellScriptUtil;
import mesquite.lib.ShellScriptWatcher;
import mesquite.lib.SingleLineTextField;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.Taxa;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.characters.MatrixFlags;
import mesquite.lib.duties.CharMatrixManager;
import mesquite.lib.duties.CharactersManager;
import mesquite.lib.duties.FileCoordinator;
import mesquite.lib.duties.FileInterpreterI;
import mesquite.lib.duties.MatrixFlagger;
import mesquite.lib.duties.MatrixFlaggerForTrimming;
import mesquite.lib.duties.TaxaManager;

/* ======================================================================== */
public class FlagByTrimAl extends MatrixFlaggerForTrimming implements ShellScriptWatcher, ActionListener {

	static final String[] autoOptionNames = new String[]{"gappyout", "strict", "strictplus", "automated1"};
	static final int autoOptionDEFAULT = 3; //automated1

	int autoOption = autoOptionDEFAULT;
	static String trimAlPath = ""; 
	FileInterpreterI exporter;	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		if (!MesquiteThread.isScripting()) {
			if (!queryOptions())
				return false;
		}
		addMenuItem("trimAl Options...",  makeCommand("queryOptions",  this));
		exporter = (FileInterpreterI)hireNamedEmployee(FileInterpreterI.class, "#InterpretFastaDNA");

		return true;
	}
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyMolecularData();
	}
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("autoOption".equalsIgnoreCase(tag)) {
			autoOption = MesquiteInteger.fromString(content);
		}
		else if ("trimAlPath".equalsIgnoreCase(tag)) {
			trimAlPath = content;
		}
		super.processSingleXMLPreference(tag, content);
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "autoOption", autoOption);  
		if (!StringUtil.blank(trimAlPath))
			StringUtil.appendXMLTag(buffer, 2, "trimAlPath", trimAlPath);  
		return super.preparePreferencesForXML()+buffer.toString();
	}
	/*.................................................................................................................*/
	public void queryLocalOptions () {
		if (queryOptions())
			storePreferences();
	}
	/*.................................................................................................................*
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Presents options dialog box.", "", commandName, "queryOptions")) {
			boolean q = queryOptions();
			if (q)
				parametersChanged();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	SingleLineTextField programPathField =  null;

	public boolean queryOptions() {
		if (!okToInteractWithUser(CAN_PROCEED_ANYWAY, "Querying Options")) 
			return true;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(),  "Options for trimAl",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		programPathField = dialog.addTextField("Path to trimAl:", trimAlPath, 40);
		Button programBrowseButton = dialog.addAListenedButton("Browse...",null, this);
		programBrowseButton.setActionCommand("programBrowse");
		dialog.addBlankLine();
		Choice alignmentMethodChoice = dialog.addPopUpMenu("trimAl Option", autoOptionNames, autoOption);
		dialog.addBlankLine();
		dialog.addHorizontalLine(1);
		dialog.addLargeOrSmallTextLabel("If you use this in a publication, please cite the version of trimAl you used. See (?) help button for details.");
		String s = "The webpage of trimAl is here: <a href=\"https://trimal.readthedocs.io\">https://trimal.readthedocs.io</a>";
		s += "<p>The citation for trimAl is: Capella-Gutiérrez, S., Silla-Martínez, J. M., & Gabaldón, T. (2009). trimAl: a tool for automated alignment trimming in large-scale phylogenetic analyses. Bioinformatics (Oxford, England), 25(15), 1972–1973."
				+ "<a href = \"https://doi.org/10.1093/bioinformatics/btp348\">https://doi.org/10.1093/bioinformatics/btp348</a>";
		dialog.appendToHelpString(s);
		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			autoOption = alignmentMethodChoice.getSelectedIndex();
			storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}

	/*.................................................................................................................*/
	public  void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("programBrowse")) {
			trimAlPath = MesquiteFile.openFileDialog("Choose trimAl: ", null, null);
			if (!StringUtil.blank(trimAlPath)) {
				programPathField.setText(trimAlPath);
			}
		}
	}

	public boolean continueShellProcess(Process proc){
		return proc.isAlive();
	}

	public boolean userAborted(){
		return false;
	}

	public boolean fatalErrorDetected(){
		return false;

	}
	String[] columns;

	/*.................................................................................................................*/

	boolean saveExportFile(CharacterData data, String path, String fileName){
		getProject().incrementProjectWindowSuppression();
		FileCoordinator coord = getFileCoordinator();
		MesquiteFile tempDataFile = (MesquiteFile)coord.doCommand("newLinkedFile", StringUtil.tokenize(fileName+".nex"), CommandChecker.defaultChecker); //TODO: never scripting???
		TaxaManager taxaManager = (TaxaManager)findElementManager(Taxa.class);
		CharacterData newMatrix=null;
		Taxa newTaxa =data.getTaxa().cloneTaxa(); 
		newTaxa.addToFile(tempDataFile, null, taxaManager);
		CharactersManager manageCharacters = (CharactersManager)findElementManager(CharacterData.class);
		MCharactersDistribution matrix = data.getMCharactersDistribution();
		CharMatrixManager manager = manageCharacters.getMatrixManager(matrix.getCharacterDataClass());
		newMatrix = matrix.makeCharacterData(manager, newTaxa);
		newMatrix.setName(data.getName());

		newMatrix.addToFile(tempDataFile, getProject(), null);

		exporter.doCommand("includeGaps","true", CommandChecker.defaultChecker);
		exporter.doCommand("simplifyTaxonName","true", CommandChecker.defaultChecker);
		exporter.doCommand("writeExcludedCharacters","false", CommandChecker.defaultChecker);

		if (exporter!=null) {
			String ext = exporter.preferredDataFileExtension();
			String s = "file = " + StringUtil.tokenize(fileName) + " directory = " + StringUtil.tokenize(path) + " usePrevious ";
			coord.export(exporter, tempDataFile, s);
		}
		newMatrix.deleteMe(false);
		newTaxa.deleteMe(false);
		coord.closeFile(tempDataFile, true);
		getProject().decrementProjectWindowSuppression();
		return true;
	}
	/*======================================================*/
	public MatrixFlags flagMatrix(CharacterData data, MatrixFlags flags) {
		if (data!=null && data.getNumChars()>0 && data instanceof MolecularData){
			if (flags == null)
				flags = new MatrixFlags(data);
			else 
				flags.reset(data);
			String rootDir = createSupportDirectory() + MesquiteFile.fileSeparator;  
			boolean success = saveExportFile(data, rootDir, "input.fas");
			String unique = MesquiteTrunk.getUniqueIDBase() + Math.abs((new Random(System.currentTimeMillis())).nextInt());
			String scriptPath = rootDir + "trimAlScript" + MesquiteFile.massageStringToFilePathSafe(unique) + ".bat";
			
			
			String script = ShellScriptUtil.getChangeDirectoryCommand(rootDir) + "\n";
			script += trimAlPath + "  -in input.fas -out output.fas -" + autoOptionNames[autoOption] + " -colnumbering > columns.txt";
			MesquiteFile.putFileContents(scriptPath, script, false);
			success = ShellScriptUtil.executeAndWaitForShell( scriptPath,  null, null, false, "trimAl", null, null, this, false);

			if (success){
				String columnsText = MesquiteFile.getFileContentsAsString(rootDir + "columns.txt");
				columns = columnsText.split(", ");
				columns[0] = columns[0].substring(12, columns[0].length());
				Bits charFlags = flags.getCharacterFlags();
				int lastKeep = -1;
				int count = 0;
				for (int k = 0; k<columns.length; k++) {
					int keep = MesquiteInteger.fromString(StringUtil.stripWhitespace(columns[k]));
					if (keep < data.getNumChars())
						for (int d = lastKeep+1; d<keep; d++) {
							boolean wasSet = charFlags.isBitOn(d);
							charFlags.setBit(d);
							if (!wasSet)
								count++;
						}
					lastKeep = keep;
				}
				for (int d = lastKeep+1; d<data.getNumChars(); d++) {
					boolean wasSet = charFlags.isBitOn(d);
					charFlags.setBit(d);
					if (!wasSet)
						count++;
				}
				
				//logln("" + count + " character(s) flagged in " + data.getName());
			}

		}

		return flags;

	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return true;
	}

	/*.................................................................................................................*/
	public boolean showCitation(){
		return false;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "trimAl";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Flags sites using trimAl" ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}


}



