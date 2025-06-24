/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.genomic.FlagByGblocks;
/*~~  */




import java.awt.Button;
import java.awt.Choice;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.util.Random;

import mesquite.categ.lib.MolecularData;
import mesquite.categ.lib.RequiresAnyMolecularData;
import mesquite.externalCommunication.lib.AppChooser;
import mesquite.lib.Bits;
import mesquite.lib.CommandChecker;
import mesquite.lib.CompatibilityTest;
import mesquite.lib.IntegerField;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteThread;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.Parser;
import mesquite.lib.ShellScriptUtil;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.MatrixFlags;
import mesquite.lib.duties.MatrixFlaggerForTrimming;
import mesquite.lib.ui.ColorTheme;
import mesquite.lib.ui.DoubleField;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.SingleLineTextField;

/* ======================================================================== */
public class FlagByGblocks extends MatrixFlaggerForTrimming implements ActionListener, TextListener, ItemListener {

	static final double b1DEFAULT = 0.5; //Minimum Number Of Sequences For A Conserved Position (50% of the number of sequences + 1)
	static final double b2DEFAULT = 0.85; //Minimum Number Of Sequences For A Flank Position (85% of the number of sequences)
	static final int b3DEFAULT = 8; //Maximum Number Of Contiguous Nonconserved Positions (8)
	static final int b4DEFAULT = 10; //Minimum Length Of A Block	(10)
	static final int b5DEFAULT = 0; //Allowed Gap Positions (0=None, 1=With Half, 2=All)

	double b1 = b1DEFAULT; //Any integer bigger than half the number of sequences and smaller or equal than the total number of sequences; Minimum Number Of Sequences For A Conserved Position (50% of the number of sequences + 1)
	double b2 = b2DEFAULT; //Any integer equal or bigger than Minimum Number Of Sequences For A Conserved Position; Minimum Number Of Sequences For A Flank Position (85% of the number of sequences)
	int b3 = b3DEFAULT; //Any integer; Maximum Number Of Contiguous Nonconserved Positions (8)
	int b4 = b4DEFAULT; //Any integer equal or bigger than 2; Minimum Length Of A Block	(10)
	int b5 = b5DEFAULT; //0, 1, 2; Allowed Gap Positions (0=None, 1=With Half, 2=All)

	static String gblocksPath = ""; 
	boolean useBuiltInIfAvailable = false;
	String builtinVersion;
	String alternativeManualPath ="";
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		if (!MesquiteThread.isScripting()) {
			if (!queryOptions())
				return false;
		}
		addMenuItem("Gblocks Options...",  makeCommand("queryOptions",  this));

		return true;
	}
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyMolecularData();
	}
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("b1".equalsIgnoreCase(tag)) {
			b1 = MesquiteDouble.fromString(content);
		}
		if ("b2".equalsIgnoreCase(tag)) {
			b2 = MesquiteDouble.fromString(content);
		}
		if ("b3".equalsIgnoreCase(tag)) {
			b3 = MesquiteInteger.fromString(content);
		}
		if ("b4".equalsIgnoreCase(tag)) {
			b4 = MesquiteInteger.fromString(content);
		}
		if ("b5".equalsIgnoreCase(tag)) {
			b5 = MesquiteInteger.fromString(content);
		}
		else if ("gblocksPath".equalsIgnoreCase(tag)) {
			gblocksPath = content;
		}
		else if ("alternativeManualPath".equalsIgnoreCase(tag)) {
			alternativeManualPath = content;
		}
		else if ("useBuiltInIfAvailable".equalsIgnoreCase(tag)) {
			useBuiltInIfAvailable = MesquiteBoolean.fromTrueFalseString(content);
		}
		super.processSingleXMLPreference(tag, content);
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "b1", b1);  
		StringUtil.appendXMLTag(buffer, 2, "b2", b2);  
		StringUtil.appendXMLTag(buffer, 2, "b3", b3);  
		StringUtil.appendXMLTag(buffer, 2, "b4", b4);  
		StringUtil.appendXMLTag(buffer, 2, "b5", b5);  
		if (!StringUtil.blank(gblocksPath))
			StringUtil.appendXMLTag(buffer, 2, "gblocksPath", gblocksPath);  
		if (!StringUtil.blank(alternativeManualPath))
			StringUtil.appendXMLTag(buffer, 2, "alternativeManualPath", alternativeManualPath);  
		StringUtil.appendXMLTag(buffer, 2, "useBuiltInIfAvailable", useBuiltInIfAvailable);  
		return super.preparePreferencesForXML()+buffer.toString();
	}
	/*.................................................................................................................*/
	public void queryLocalOptions () {
		if (queryOptions())
			storePreferences();
	}
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = super.getSnapshot(file);
		temp.addLine("b1 " + b1);
		temp.addLine("b2 " + b2);
		temp.addLine("b3 " + b3);
		temp.addLine("b4 " + b4);
		temp.addLine("b5 " + b5);
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets b1.", "[integer]", commandName, "b1")) {
			double s = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (MesquiteDouble.isCombinable(s) && s>=0 && s<=3){
				b1 = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 
			}
		}
		else if (checker.compare(this.getClass(), "Sets b2.", "[integer]", commandName, "b2")) {
			double s = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (MesquiteDouble.isCombinable(s) && s>=0 && s<=3){
				b2 = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 
			}
		}
		else if (checker.compare(this.getClass(), "Sets b3.", "[integer]", commandName, "b3")) {
			int s = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (MesquiteInteger.isCombinable(s) && s>=0 && s<=3){
				b3 = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 
			}
		}		else if (checker.compare(this.getClass(), "Sets b4.", "[integer]", commandName, "b4")) {
			int s = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (MesquiteInteger.isCombinable(s) && s>=0 && s<=3){
				b4 = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 
			}
		}		else if (checker.compare(this.getClass(), "Sets b5.", "[integer]", commandName, "b5")) {
			int s = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (MesquiteInteger.isCombinable(s) && s>=0 && s<=3){
				b5 = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 
			}
		}		else if (checker.compare(this.getClass(), "Presents options dialog box.", "", commandName, "queryOptions")) {
			boolean q = queryOptions();
			if (q)
				parametersChanged();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public void itemStateChanged(ItemEvent e) {
		resetParamsInfo();
	}

	void resetParamsInfo(){
		String info = "b1=" + b1F.getValueAsString() + " b2=" + b2F.getValueAsString() + " b3=" + b3F.getValueAsString() + " b4=" + b4F.getValueAsString();
		if (b5F.getSelectedIndex()==0)
			info += " b5=n";
		else if (b5F.getSelectedIndex()==1)
			info += " b5=h";
		else
			info += " b5=a";

		paramsInfo.setText(info);
	}
	/*.................................................................................................................*/
	SingleLineTextField programPathField =  null;

	DoubleField b1F, b2F;
	IntegerField b3F, b4F;
	Choice b5F;
	double b1Prev, b2Prev;
	SingleLineTextField paramsInfo;

	public boolean queryOptions() {
		if (!okToInteractWithUser(CAN_PROCEED_ANYWAY, "Querying Options")) 
			return true;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(),  "Options for Gblocks",buttonPressed);  
		AppChooser appChooser = new AppChooser("Gblocks", "Gblocks", useBuiltInIfAvailable, alternativeManualPath);
		appChooser.addToDialog(dialog);
		dialog.addHorizontalLine(1);
		dialog.addBlankLine();
		if (b1<0.5)
			b1 = 0.5;
		else if (b1>1)
			b1 = 1;
		if (b2 < b1)
			b2 = b1;
		if (b3<2)
			b3 = 2;
		b1Prev = b1;
		b2Prev = b2;
		b1F = dialog.addDoubleField("Minimum proportion of sequences for a conserved position", b1, 4);
		b1F.getTextField().addTextListener(this); // to check b2 is >= b1
		b2F = dialog.addDoubleField("Minimum proportion of sequences for a flank position", b2, 4);
		b2F.getTextField().addTextListener(this); // to check b2 is >= b1
		b3F = dialog.addIntegerField("Maximum number of contiguous nonconserved positions (any integer)", b3, 4, 0, MesquiteInteger.infinite);
		b4F = dialog.addIntegerField("Minimum length of a block (at least 2)", b4, 4, 2, MesquiteInteger.infinite);
		b2F.getTextField().addTextListener(this); // to check b2 is >= b1
		b5F = dialog.addPopUpMenu("Allowed Gap Positions (b5)", new String[]{"None", "With Half", "All"}, b5);
		b1F.getTextField().addTextListener(this);
		b2F.getTextField().addTextListener(this);
		b3F.getTextField().addTextListener(this);
		b4F.getTextField().addTextListener(this);
		b5F.addItemListener(this);

		dialog.addBlankLine();
		dialog.addHorizontalLine(1);
		paramsInfo = dialog.addTextField("Report parameters as:", "", 30);
		paramsInfo.setEditable(false);
		paramsInfo.setBackground(ColorTheme.getInterfaceBackgroundPale());
		resetParamsInfo();
		dialog.addHorizontalLine(1);
		dialog.addBlankLine();
		Button useDefaultsButton = null;
		useDefaultsButton = dialog.addAListenedButton("Set to Defaults", null, this);
		useDefaultsButton.setActionCommand("setToDefaults");
		dialog.addBlankLine();
		dialog.addLargeOrSmallTextLabel("If you use this in a publication, please cite the version of Gblocks you used. See (?) help button for details.");

		String s = "This function in Mesquite requires that you have already installed Gblocks in your computer. The webpage of Gblocks is here: <a href = \"https://www.biologiaevolutiva.org/jcastresana/Gblocks.html\">https://www.biologiaevolutiva.org/jcastresana/Gblocks.html</a>"
				+ "<p>If you use this in a publication, please cite it as Gblocks (Castresana 2000) run via Mesquite."
				+ "<p><b>Reference for Gblocks</b>; Castresana J. 2000. Selection of conserved blocks from multiple alignments for their use in phylogenetic analysis. Molecular Biology and Evolution 17: 540–552"
				+ " <a href = \"http://doi.org/10.1093/oxfordjournals.molbev.a026334\">doi:10.1093/oxfordjournals.molbev.a026334</a>";
		dialog.appendToHelpString(s);
		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			b1 = b1F.getValue();
			b2 = b2F.getValue();
			b3 = b3F.getValue();
			b4 = b4F.getValue();
			b5 = b5F.getSelectedIndex();
			gblocksPath = appChooser.getPathToUse();
			alternativeManualPath = appChooser.getManualPath(); //for preference writing
			useBuiltInIfAvailable = appChooser.useBuiltInExecutable(); //for preference writing
			builtinVersion = appChooser.getVersion(); //for informing user; only if built-in
			storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}

	/*.................................................................................................................*/
	public  void actionPerformed(ActionEvent e) {
		/*if (e.getActionCommand().equalsIgnoreCase("programBrowse")) {
			gblocksPath = MesquiteFile.openFileDialog("Choose Gblocks: ", null, null);
			if (!StringUtil.blank(gblocksPath)) {
				programPathField.setText(gblocksPath);
			}
		}
		else */
		if (e.getActionCommand().equalsIgnoreCase("setToDefaults")) {
			b1F.setValue(b1DEFAULT);
			b2F.setValue(b2DEFAULT);
			b3F.setValue(b3DEFAULT);
			b4F.setValue(b4DEFAULT);
			b5F.select(b5DEFAULT);
		}
	}
	/*.................................................................................................................*/
	public  void textValueChanged(TextEvent e) {
		double b1Temp = b1F.getValue();
		double b2Temp = b2F.getValue();
		if (e.getSource() == b1F.getTextField()){
			if (b1Temp < 0.5 || b1Temp >1){
				alert("The minimum proportion of sequences for a conserved position must be between 0.5 and 1.0 inclusive.");
				b1F.setValue(b1Prev);
			}
			else if (b2Temp<b1Temp){
				alert("The minimum proportion of sequences for a flank position must be at least as great as that for a conserved position. The value for flank positions will be reset");
				b2F.setValue(b1F.getValue());
			}
			b1Prev = b1F.getValue();
			b2Prev = b2F.getValue();
		}
		else if (e.getSource() == b2F.getTextField()){
			if (b2Temp < 0.5 || b2Temp >1){
				alert("The minimum proportion of sequences for a flank position must be between 0.5 and 1.0 inclusive.");
				b2F.setValue(b2Prev);
			}
			else if (b2Temp<b1Temp){
				alert("The minimum proportion of sequences for a flank position must be at least as great as that for a conserved position.");
				b2F.setValue(b1F.getValue());
			}
			b1Prev = b1F.getValue();
			b2Prev = b2F.getValue();
		}
		resetParamsInfo();
	}

	String[] columns;

	/*.................................................................................................................*/

	/*======================================================*/
	public MatrixFlags flagMatrix(CharacterData data, MatrixFlags flags) {
		if (data!=null && data.getNumChars()>0 && data instanceof MolecularData){
			if (StringUtil.blank(gblocksPath)) {
				discreetAlert( "No path to Gblocks has been specified.");
				return null;
			}	
			if (flags == null)
				flags = new MatrixFlags(data);
			else 
				flags.reset(data);
			String rootDir = createSupportDirectory() + MesquiteFile.fileSeparator;  
			String unique = MesquiteFile.massageStringToFilePathSafe(MesquiteTrunk.getUniqueIDBase() + Math.abs((new Random(System.currentTimeMillis())).nextInt()));
			boolean successSaving = saveFastaFile(data, rootDir, unique + "alignment.fas");
			String scriptPath = rootDir + "GblocksScript" + unique + ".bat";


			String script = ShellScriptUtil.getChangeDirectoryCommand(rootDir) + "\n";
			int b1Count = (int)b1*data.getNumTaxa();
			if (b1Count <= data.getNumTaxa()/2)
				b1Count = data.getNumTaxa()/2 + 1;
			int b2Count = (int)b2*data.getNumTaxa();
			if (b2Count < b1Count)
				b2Count = b1Count;
			String gapsOption = "n";
			if (b5 == 1)
				gapsOption = "h";
			else if (b5 == 2)
				gapsOption = "a";
			script += StringUtil.protectFilePath(gblocksPath) + "  " + unique + "alignment.fas -b1=" + b1Count + " -b2=" + b2Count + " -b3=" + b3 + " -b4=" + b4 + " -b5=" + gapsOption + " -s=n -p=s";
			MesquiteFile.putFileContents(scriptPath, script, false);
			int resultStatus = ShellScriptUtil.executeAndWaitForShell(scriptPath);

			if (successSaving && resultStatus== ShellScriptUtil.shellScriptNoError){
				String[] resultText = MesquiteFile.getFileContentsAsStrings(rootDir + unique + "alignment.fas-gb.txts");
				if (resultText != null) {
					boolean done = false;
					Parser parser = new Parser();
					Bits charFlags = flags.getCharacterFlags();
					int count = 0;
					for (int i = 0; i<resultText.length && !done; i++){
						String line = resultText[i];
						if (!StringUtil.blank(line)){
							if (line.startsWith("Flanks:")){

								String[] flanks = line.split("\\[");
								int previousEnd = -1;
								for (int k=1; k<flanks.length;k++){
									if (!StringUtil.blank(flanks[k])){
										parser.setString(flanks[k]);
										int start = MesquiteInteger.fromString(parser.getFirstToken());
										int end = MesquiteInteger.fromString(parser.getNextToken());
										for (int f = previousEnd; f< start && f<data.getNumChars(); f++){
											boolean wasSet = charFlags.isBitOn(f);
											charFlags.setBit(f);
											if (!wasSet)
												count++;
										}
										previousEnd = end;
									}
								}
								for (int f = previousEnd; f< data.getNumChars(); f++){
									boolean wasSet = charFlags.isBitOn(f);
									charFlags.setBit(f);
									if (!wasSet)
										count++;
								}
								done = true;
								/*					columns = resultText.split(", ");
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
								 */
							}
						}
					}
				}
				else
					MesquiteMessage.warnProgrammer("No results from Gblocks found!");

				//logln("" + count + " character(s) flagged in " + data.getName());
			}
			else MesquiteMessage.warnUser(" Error status returned from attempt to run Gblocs: " + resultStatus);
		deleteSupportDirectory();

		}

		return flags;

	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
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
		return "Gblocks";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Flags sites using Gblocks" ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}


}



