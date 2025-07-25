package mesquite.align.MAFFTAlign;

import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import mesquite.align.lib.ExternalSequenceAligner;
import mesquite.categ.lib.MolecularData;
import mesquite.lib.CommandChecker;
import mesquite.lib.Debugg;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.ParseUtil;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.SingleLineTextField;

/* ======================================================================== */
public class MAFFTAlign extends ExternalSequenceAligner implements ItemListener{
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}	
	/*.................................................................................................................*/
	public boolean showCitation() {
		return true;
	}
	/*.................................................................................................................*/
	public String getCitation()  {
		return "Please remember to cite the version of MAFFT you used.";
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}
	/*.................................................................................................................*/
	public String getName() {
		return "MAFFT Align";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "MAFFT Align...";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Sends the selected sequences to MAFFT to align." ;
	}

	public String getProgramName(){
		return "MAFFT";
	}

	public String getAppOfficialName() {
		return "mafft";
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = super.getSnapshot(file);
		if (temp == null)
			temp = new Snapshot();
		temp.addLine("setAlignmentMethod " + alignmentMethod);  
		temp.addLine("setAlignmentMethodText " + ParseUtil.tokenize(alignmentMethodText));  
		temp.addLine("setUseMaxCores " + useMaxCores);
		temp.addLine("optionsSet");
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the alignment method", "[number]", commandName, "setAlignmentMethod")) {
			alignmentMethod = MesquiteInteger.fromString(parser.getFirstToken(arguments));
		}
		else if (checker.compare(this.getClass(), "Sets the text of the alignment method", "[text]", commandName, "setAlignmentMethodText")) {
			String temp = parser.getFirstToken(arguments);
			if (temp != null)
				alignmentMethodText = temp;
		}
		else if (checker.compare(this.getClass(), "Sets whether to use maximum number of cores", "[true or false]", commandName, "setUseMaxCores")) {
			useMaxCores = MesquiteBoolean.fromTrueFalseString(parser.getFirstToken(arguments));
		}
		else if (checker.compare(this.getClass(), "Records that options set", "", commandName, "optionsSet")) {
			optionsAlreadySet = true;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}	
	
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("alignmentMethod".equalsIgnoreCase(tag)) {
			alignmentMethod = MesquiteInteger.fromString(content);
		}
		else if ("alignmentMethodText".equalsIgnoreCase(tag)) {
			alignmentMethodText = StringUtil.cleanXMLEscapeCharacters(content);
		}
		else if ("useMaxCores".equalsIgnoreCase(tag))
			useMaxCores = MesquiteBoolean.fromTrueFalseString(content);

		super.processSingleXMLPreference(tag, content);
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "alignmentMethod", alignmentMethod);  
		StringUtil.appendXMLTag(buffer, 2, "alignmentMethodText", alignmentMethodText);  
		StringUtil.appendXMLTag(buffer, 2, "useMaxCores", useMaxCores);  

		return super.preparePreferencesForXML()+buffer.toString();
	}

	/*.................................................................................................................*/
	public String getHelpString() {
		String s =  " In the MAFFT Options field, place any MAFFT options you wish to use.  For example, if you wished to "
				+ " invoke the linsi option (for more thorough alignment search), you would use \"--localpair --maxiterate 1000\".";
		return s;   //linsi option:  --localpair --maxiterate 1000 --thread -1
	}
	/*.................................................................................................................*/
	public String getHelpURL(){
		if (appInfoFile!=null) {
			if (StringUtil.notEmpty(appInfoFile.getURL()))
					return appInfoFile.getURL();
		}
			
		return "http://mafft.cbrc.jp/alignment/software/manual/manual.html";
	}

	static int DEFAULTSUGGESTEDMETHOD=0;
	static final int LINSI = 1;
	static final int GINSI = 2;
	static final int EINSI = 3; 
	static final int FFTNSI2 = 4; 
	static final int FFTNSI1000 = 5; 
	static final int FFTNS2 =6; 
	static final int FFTNS1 = 7; 
	static final int NWNSI = 8; 
	static final int NWNS2 = 9; 
	static final int NWNSPT1 = 10; 
	boolean useMaxCores = true;
	int alignmentMethod = DEFAULTSUGGESTEDMETHOD;
	String alignmentMethodText = "";
	Checkbox useMaxThreadsCheckBox;
	Choice alignmentMethodChoice;
	SingleLineTextField alignmentTextField;

	/*.................................................................................................................*/
	public String getAlignmentText(int value) {
		String alignmentText="";
		switch (value) {
		case LINSI:
			alignmentText=" --localpair --maxiterate 1000 ";
			break;
		case GINSI:
			alignmentText=" --globalpair --maxiterate 1000 ";
			break;
		case EINSI:
			alignmentText=" --ep 0 --genafpair --maxiterate 1000 ";
			break;
		case FFTNSI2:
			alignmentText=" --retree 2 --maxiterate 2 ";
			break;
		case FFTNSI1000:
			alignmentText=" --retree 2 --maxiterate 1000 ";
			break;
		case FFTNS2:
			alignmentText=" --retree 2 --maxiterate 0 ";
			break;
		case FFTNS1:
			alignmentText=" --retree 1 --maxiterate 0 ";
			break;
		case NWNSI:
			alignmentText=" --retree 2 --maxiterate 2 --nofft ";
			break;
		case NWNS2:
			alignmentText=" --retree 2 --maxiterate 0 --nofft ";
			break;
		case NWNSPT1:
			alignmentText=" --retree 1 --maxiterate 0 --nofft --parttree ";
			break;
		default: 
			alignmentText="";
		}
		return alignmentText;
	}
/*.................................................................................................................*/
	public void setAlignmentTextFromChoice() {
		String alignmentText=getAlignmentText(alignmentMethodChoice.getSelectedIndex());
		if (alignmentTextField!=null)
			alignmentTextField.setText(alignmentText);
	}
	/*.................................................................................................................*/
	public void itemStateChanged(ItemEvent e) {
		if (e.getItemSelectable() == alignmentMethodChoice){
			setAlignmentTextFromChoice();
		}				
	}
/*.................................................................................................................*/
	public void queryProgramOptions(ExtensibleDialog dialog) {
		useMaxThreadsCheckBox = dialog.addCheckBox("let MAFFT choose number of computer cores to use", useMaxCores);
		
		alignmentMethodChoice = dialog.addPopUpMenu("Suggested Methods", new String[] {"Default",  "L-INS-i", "G-INSI-i", "E-INS-i", "FFT-NS-i 2", "FFT-NS-i 1000", "FFT-NS-2", "FFT-NS-1", "NW-NS-i", "NW-NS-2", "NW-NS-PartTree-1"}, alignmentMethod);
		alignmentTextField = dialog.addTextField("Basic alignment method", alignmentMethodText, 40);
		alignmentMethodChoice.addItemListener(this);
		//linsi --localpair --maxiterate 1000
		//ginsi --globalpair --maxiterate 1000
		//einsi --ep 0 --genafpair --maxiterate 1000

		//fftnsi2 --retree 2 --maxiterate 2
		//fftnsi1000 --retree 2 --maxiterate 1000
		//fftns2 --retree 2 --maxiterate 0
		//fftns1--retree 1 --maxiterate 0
		//nwnsi --retree 2 --maxiterate 2 --nofft
		//nwns2 --retree 2 --maxiterate 0 --nofft
		//nwnsPT1 --retree 1 --maxiterate 0 --nofft --parttree

	}
	/*.................................................................................................................*/
	public void processQueryProgramOptions(ExtensibleDialog dialog) {
		useMaxCores = useMaxThreadsCheckBox.getState();
		int temp = alignmentMethodChoice.getSelectedIndex();
		if (temp>=0)
			alignmentMethod = temp;
		alignmentMethodText = alignmentTextField.getText();
		storePreferences();

	}

	/*.................................................................................................................*/
	public String getQueryProgramOptions() {
		String options = "";
		if (useMaxCores)
			options+=" --thread -1 ";
		if (alignmentMethodText == null)
			options += " ";
		else
			options += " " + alignmentMethodText + " ";
		return options;
	}

	/*.................................................................................................................*/
	public String getProgramCommand(){
		if (MesquiteTrunk.isWindows())
			return "call " + StringUtil.protectFilePathForWindows(getProgramPath());
		else
			return StringUtil.protectFilePathForUnix(getProgramPath());
	}
	public boolean programOptionsComeFirst(){
		return true;  
	}
	/*.................................................................................................................*/
	public String getDefaultProgramOptions(){
		return "";
	}

	public void appendDefaultOptions(StringBuffer shellScript, String inFilePath, String outFilePath, MolecularData data) {
			if (scriptBased)
				shellScript.append("  " + StringUtil.protectFilePathForUnix(inFilePath) + " > " + outFilePath);
			else
				shellScript.append("  " + StringUtil.protectFilePathForUnix(inFilePath));
	}
	
	public String getDNAExportInterpreter () {
		return "#InterpretFastaDNA";
	}
	public String getProteinExportInterpreter () {
		return "#InterpretFastaProtein";
	}
	public String getDNAImportInterpreter () {
		return "#InterpretFastaDNA";
	}
	public String getProteinImportInterpreter () {
		return "#InterpretFastaProtein";
	}
	public  String getExportExtension() {
		return ".fas";
	}
	public  String getImportExtension() {
		return ".fas";
	}


	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 300;  
	}

}
