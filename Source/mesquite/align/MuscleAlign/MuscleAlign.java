package mesquite.align.MuscleAlign;

import mesquite.align.lib.ExternalSequenceAligner;
import mesquite.categ.lib.MolecularData;
import mesquite.lib.CommandChecker;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;

/* ======================================================================== */
public class MuscleAlign extends ExternalSequenceAligner{
	protected static String V3="V3";
	protected static String V5="V5";
	protected String appVariant = V3;
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
		return "Please remember to cite the version of MUSCLE you used.";
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
		return "MUSCLE Align";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "MUSCLE Align...";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Sends the selected sequence to Muscle to align." ;
	}
	
	public String getProgramName(){
		 return "MUSCLE";
	 }
	/*.................................................................................................................*/
	public String getHelpURL(){
		if (appInfoFile!=null) {
			if (StringUtil.notEmpty(appInfoFile.getURL()))
					return appInfoFile.getURL();
		}
		return "http://www.drive5.com/muscle/manual/";
	}
	public String getAppOfficialName() {
		return "muscle";
	}
	/*.................................................................................................................*/
	public String getHelpString() {
	  String s =  " In the Muscle Options field, place any Muscle options you wish to use.  For example, if you wished to change the"
		  + " gap opening cost to 3 and the gap extension cost to 8, the options would be \"-gapopen -8.0  -gapextend -3.0\".";
	  return s;
	}

	/*.................................................................................................................*/
	public String getProgramCommand(){
		if (MesquiteTrunk.isWindows())
			return "call " + StringUtil.protectFilePathForWindows(getProgramPath());
		else
			return StringUtil.protectFilePathForUnix(getProgramPath());
	}
	/*.................................................................................................................*/
	public String getDefaultProgramOptions(){
		return "";
	}
	
	/*.................................................................................................................*/
	public boolean appVariantMatches(String variant){
		String s = getAppVariant();
		if (StringUtil.notEmpty(s)) {
			return s.equalsIgnoreCase(variant);
		}
		return false;
	}
	/*.................................................................................................................*/
	public String getInputFileOption(){
		if (appVariantMatches(V5))
			return " -align ";
		else 
			return " -in ";
		
	}
	/*.................................................................................................................*/
	public String geOutputFileOption(){
		if (appVariantMatches(V5))
			return " -output ";
		else
			return " -out ";
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = super.getSnapshot(file);
		if (temp == null)
			temp = new Snapshot();
		temp.addLine("optionsSet");
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Records that options set", "", commandName, "optionsSet")) {
			optionsAlreadySet = true;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}	
	

	public void appendDefaultOptions(StringBuffer shellScript, String inFilePath, String outFilePath, MolecularData data) {
		if (scriptBased || appVariantMatches(V5))
			shellScript.append(getInputFileOption() + StringUtil.protectFilePathForUnix(inFilePath)+geOutputFileOption() + StringUtil.protectFilePathForUnix(outFilePath));
		else
			shellScript.append(getInputFileOption() + StringUtil.protectFilePathForUnix(inFilePath));
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
		return -111;  
	}

}
