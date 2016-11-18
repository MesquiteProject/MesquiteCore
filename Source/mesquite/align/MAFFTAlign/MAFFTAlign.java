package mesquite.align.MAFFTAlign;

/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
/*~~  */

import java.util.*;
import java.lang.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.JLabel;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.lib.table.*;
import mesquite.align.lib.*;

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
		useMaxThreadsCheckBox = dialog.addCheckBox("use maximum number of computer cores", useMaxCores);
		
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
		if (!MesquiteTrunk.isWindows())
			shellScript.append("  " + StringUtil.protectFilePathForUnix(inFilePath) + " > " + StringUtil.protectFilePathForUnix(outFilePath));
		else
			shellScript.append(" --out " + StringUtil.protectFilePathForUnix(outFilePath) + " " + StringUtil.protectFilePathForUnix(inFilePath));  
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
