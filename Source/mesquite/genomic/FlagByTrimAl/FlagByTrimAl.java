/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.genomic.FlagByTrimAl;
/*~~  */




import java.awt.Choice;
import java.util.Random;


import mesquite.categ.lib.MolecularData;
import mesquite.categ.lib.RequiresAnyMolecularData;
import mesquite.externalCommunication.lib.AppChooser;
import mesquite.lib.Bits;
import mesquite.lib.CommandChecker;
import mesquite.lib.CompatibilityTest;
import mesquite.lib.Debugg;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteThread;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.ParseUtil;
import mesquite.lib.ShellScriptUtil;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.MatrixFlags;
import mesquite.lib.duties.MatrixFlaggerForTrimming;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.SingleLineTextField;

/* ======================================================================== */
public class FlagByTrimAl extends MatrixFlaggerForTrimming { 

	static final String[] autoOptionNames = new String[]{"none", "gappyout", "strict", "strictplus", "automated1"};
	static final int autoOptionDEFAULT = 4; //automated1
	
	boolean useBuiltInIfAvailable = false;
	String builtinVersion;
	String trimAlPath = ""; 
	String alternativeManualPath ="";

	int autoOption = autoOptionDEFAULT;
	static String manualOptions = "";
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		if (!MesquiteThread.isScripting()) {
			if (!queryOptions())
				return false;
		}
		addMenuItem("trimAl Options...",  makeCommand("queryOptions",  this));

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
		else if ("alternativeManualPath".equalsIgnoreCase(tag)) {
			alternativeManualPath = content;
		}
		else if ("useBuiltInIfAvailable".equalsIgnoreCase(tag)) {
			useBuiltInIfAvailable = MesquiteBoolean.fromTrueFalseString(content);
		}
		else if ("manualOptions".equalsIgnoreCase(tag)) {
			manualOptions = content;
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
		if (!StringUtil.blank(alternativeManualPath))
			StringUtil.appendXMLTag(buffer, 2, "alternativeManualPath", alternativeManualPath);  
		StringUtil.appendXMLTag(buffer, 2, "useBuiltInIfAvailable", useBuiltInIfAvailable);  
		if (!StringUtil.blank(manualOptions))
			StringUtil.appendXMLTag(buffer, 2, "manualOptions", manualOptions);  
		
		if (!StringUtil.blank(trimAlPath))
			StringUtil.appendXMLTag(buffer, 2, "trimAlPath", trimAlPath);  
		return super.preparePreferencesForXML()+buffer.toString();
	}

	/*.................................................................................................................*/
	public void queryLocalOptions () {
		if (queryOptions())
			storePreferences();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = super.getSnapshot(file);
		temp.addLine("autoOption " + autoOption);
		temp.addLine("manualOptions " + ParseUtil.tokenize(manualOptions));
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the option.", "[integer]", commandName, "autoOption")) {
			int s = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (MesquiteInteger.isCombinable(s) && s>=0 && s<=4){
				autoOption = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 
			}
		}
		else if (checker.compare(this.getClass(), "Sets the string for manually-specified options.", "[string]", commandName, "manualOptions")) {
			String s = parser.getFirstToken(arguments);
			if (!StringUtil.blank(s)){
				manualOptions = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 
			}
		}
		else if (checker.compare(this.getClass(), "Presents options dialog box.", "", commandName, "queryOptions")) {
			boolean q = queryOptions();
			if (q)
				parametersChanged();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	
	/*.................................................................................................................*/
	SingleLineTextField programPathField =  null;
	SingleLineTextField manualOptionsField =  null;

	public boolean queryOptions() {
		if (!okToInteractWithUser(CAN_PROCEED_ANYWAY, "Querying Options")) 
			return true;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(),  "Options for trimAl",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		
		
		AppChooser appChooser = new AppChooser("trimAl", "trimAl", useBuiltInIfAvailable, alternativeManualPath);
		appChooser.addToDialog(dialog);
		dialog.addHorizontalLine(1);

		/*programPathField = dialog.addTextField("Path to trimAl:", trimAlPath, 40);
		Button programBrowseButton = dialog.addAListenedButton("Browse...",null, this);
		programBrowseButton.setActionCommand("programBrowse");
		*/
		dialog.addBlankLine();
		Choice alignmentMethodChoice = dialog.addPopUpMenu("Automated option", autoOptionNames, autoOption);
		manualOptionsField = dialog.addTextField("Additional options (do NOT set -in or -out):", manualOptions, 40);
		dialog.addBlankLine();
		dialog.addHorizontalLine(1);
		dialog.addLargeOrSmallTextLabel("If you use this in a publication, please cite the version of trimAl you used. See (?) help button for details.");

		String s = "This function in Mesquite requires that you have already installed trimAl in your computer. The webpage of trimAl is here: <a href=\"https://trimal.readthedocs.io\">https://trimal.readthedocs.io</a>";
		s += "<p><b>Reference for trimAl</b>: Capella-Gutiérrez, S., Silla-Martínez, J. M., & Gabaldón, T. (2009). trimAl: a tool for automated alignment trimming in large-scale phylogenetic analyses. Bioinformatics (Oxford, England), 25(15), 1972–1973."
				+ "<a href = \"https://doi.org/10.1093/bioinformatics/btp348\">https://doi.org/10.1093/bioinformatics/btp348</a>";
		dialog.appendToHelpString(s);
		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			autoOption = alignmentMethodChoice.getSelectedIndex();
			manualOptions = manualOptionsField.getText();
			trimAlPath = appChooser.getPathToUse();
 			alternativeManualPath = appChooser.getManualPath(); //for preference writing
			useBuiltInIfAvailable = appChooser.useBuiltInExecutable(); //for preference writing
			builtinVersion = appChooser.getVersion(); //for informing user; only if built-in
		storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}

	/*.................................................................................................................*
	public  void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("programBrowse")) {
			trimAlPath = MesquiteFile.openFileDialog("Choose trimAl: ", null, null);
			if (!StringUtil.blank(alternativeManualPath)) {
				programPathField.setText(alternativeManualPath);
			}
		}
	}
*/
	String[] columns;

	/*.................................................................................................................*/
	public String reportStatus(){
		return "@" + status;
	}
	/*======================================================*/
	public MatrixFlags flagMatrix(CharacterData data, MatrixFlags flags) {
		if (data!=null && data.getNumChars()>0 && data instanceof MolecularData){
			if (flags == null)
				flags = new MatrixFlags(data);
			else 
				flags.reset(data);
			
			String rootDir = createSupportDirectory() + MesquiteFile.fileSeparator;  
			String unique = MesquiteFile.massageStringToFilePathSafe(MesquiteTrunk.getUniqueIDBase() + Math.abs((new Random(System.currentTimeMillis())).nextInt()));
			status = "savingFasta";
			boolean successSaving = saveFastaFile(data, rootDir, unique + "input.fas");
			status = "fastaSaved";
			String scriptPath = rootDir + "trimAlScript" + unique + ".bat";


			String script = ShellScriptUtil.getChangeDirectoryCommand(rootDir) + "\n";
			String options = "";
			if (autoOption>0)
				options = "-" + autoOptionNames[autoOption];
			if (!StringUtil.blank(manualOptions))
				options = options + " " + manualOptions;
			script += StringUtil.protectFilePath(trimAlPath) + "  -in " + unique + "input.fas -out " + unique + "output.fas " + options + " -colnumbering > " + unique + "columns.txt";
			status = "savingScript";
			MesquiteFile.putFileContents(scriptPath, script, false);
			status = "scriptSaved";
			status = "executing";
			int resultStatus = ShellScriptUtil.executeAndWaitForShell(scriptPath);
			status = "done";

			if (successSaving && resultStatus== ShellScriptUtil.shellScriptNoError){
				String columnsPath = rootDir + unique + "columns.txt";
				if (!MesquiteFile.fileExists(columnsPath)) {
					MesquiteMessage.warnUser(" No trimming results file for matrix " + data.getName() + "; columns.txt file not found");
					deleteSupportDirectory();
					return flags;
			}
				String columnsText = MesquiteFile.getFileContentsAsString(columnsPath);
				if (columnsText != null) {
					columnsText = StringUtil.stripLeadingWhitespace(columnsText);
					columns = columnsText.split(", ");

					// the columns.txt file seems to have two versions, one with a word in front, the other with just column numbers.
					if (columns.length < 1 || (columns[0].length()<12 && !MesquiteInteger.isCombinable(MesquiteInteger.fromString(StringUtil.stripWhitespace(columns[0]))))){
						MesquiteMessage.warnUser("  WARNING: No trimming results for matrix " + data.getName() + " file: " + unique + "columns.txt; contents: " + columnsText);
					}
					else {
						if (!MesquiteInteger.isCombinable(MesquiteInteger.fromString(StringUtil.stripWhitespace(columns[0]))))
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
					}
				}
				else
					MesquiteMessage.warnUser(" No trimming results file for matrix " + data.getName());

				//logln("" + count + " character(s) flagged in " + data.getName());
			}
			else MesquiteMessage.warnUser(" Error status returned from attempt to run trimAl: " + resultStatus);
			deleteSupportDirectory(); 

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





