/* Mesquite (package mesquite.io).  Copyright 2000-2010 D. Maddison and W. Maddison. 
Version 2.73, July 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.mb.lib;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;



public abstract class ExportForMrBayesLib extends FileInterpreterI {


	/*.................................................................................................................*/
	public String preferredDataFileExtension() {
		return "nex";
	}
	/*.................................................................................................................*/
	public boolean canExportEver() {  
		return true;  //
	}
	/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		return (project.getNumberCharMatrices( CategoricalState.class) > 0) ;
	}

	/*.................................................................................................................*/
	public boolean canExportData(Class dataClass) {  
		return (dataClass==CategoricalState.class);
	}
	/*.................................................................................................................*/
	public boolean canImport() {  
		return false;
	}

	/*.................................................................................................................*/
	public void readFile(MesquiteProject mf, MesquiteFile file, String arguments) {
	}


	/* ============================  exporting ============================*/
	/*.................................................................................................................*/
	protected boolean convertAmbiguities = false;
	protected boolean simplifyNames = true;
	protected boolean useData = true;
	protected String addendum = "";
	protected String fileName = "untitled.nex";

	/*.................................................................................................................*/
	public abstract String getProgramName();
	/*.................................................................................................................*/

	public boolean getExportOptions(CharacterData data, boolean dataSelected, boolean taxaSelected){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExporterDialog exportDialog = new ExporterDialog(this,containerOfModule(), getName(), buttonPressed);
		exportDialog.setSuppressLineEndQuery(true);
		exportDialog.setDefaultButton(null);
		String helpString = "The MrBayes block shown will be added to the bottom of the exported file; you may wish to edit it before using the file. \n\n";
		helpString += "If you check 'simplify names', then Mesquite will remove all characters from the taxon names ";
		helpString += "that " + getProgramName() + " can not read, such as quotes, parentheses, etc.";
		exportDialog.appendToHelpString(helpString);
//		Checkbox convertToMissing = exportDialog.addCheckBox("convert partial ambiguities to missing", convertAmbiguities);
		Checkbox simplifyNamesCheckBox = exportDialog.addCheckBox("simplify names as required for " + getProgramName(), simplifyNames);
		exportDialog.addLabel("MrBayes block: ");

		addendum = getMrBayesBlock(data);

		TextArea fsText =exportDialog.addTextAreaSmallFont(addendum,16);

		exportDialog.completeAndShowDialog(dataSelected, taxaSelected);

		boolean ok = (exportDialog.query(dataSelected, taxaSelected)==0);

//		convertAmbiguities = convertToMissing.getState();
		simplifyNames = simplifyNamesCheckBox.getState();

		addendum = fsText.getText();
		exportDialog.dispose();
		return ok;
	}	

	private String basicBlock(){
		String sT = "begin mrbayes;\n\tset autoclose=yes nowarn=yes;";
		sT +="\n\tlset nst=6 rates=invgamma;\n\tunlink statefreq=(all) revmat=(all) shape=(all) pinvar=(all); \n\tprset applyto=(all) ratepr=variable;\n\tmcmcp ngen= 10000000 relburnin=yes burninfrac=0.25  printfreq=1000  samplefreq=1000 nchains=4 savebrlens=yes;\n\tmcmc;\n\tsumt;\nend;";

		return sT;
	}
	private String getMrBayesBlock(CharacterData data){
		boolean writeCodPosPartition = false;
		boolean writeStandardPartition = false;
		CharactersGroup[] parts =null;
		if (data instanceof DNAData)
			writeCodPosPartition = ((DNAData)data).someCoding();
		CharacterPartition characterPartition = (CharacterPartition)data.getCurrentSpecsSet(CharacterPartition.class);
		if (characterPartition == null && !writeCodPosPartition)
			return basicBlock();
		if (characterPartition!=null) {
			parts = characterPartition.getGroups();
			writeStandardPartition = parts!=null;
		}

		if (!writeStandardPartition && !writeCodPosPartition) {
			return basicBlock();
		}
		String sT = "begin mrbayes;\n\tset autoclose=no nowarn=yes;  ";
		CharInclusionSet incl = null;
		if (data !=null) {
			incl = (CharInclusionSet)data.getCurrentSpecsSet(CharInclusionSet.class);
			String exc = data.getExcludedCharactersList(incl);
			exc = StringUtil.stripBoundingWhitespace(exc);
			if (!StringUtil.blank(exc))
				sT += "\n\n\texclude " + exc + ";";
		}


		String codPosPart = "";
		boolean molecular = (data instanceof MolecularData);
		boolean nucleotides = (data instanceof DNAData);
		if (writeCodPosPartition) {
			//codon positions if nucleotide
			int numberCharSets = 0;
			String list = "";
			String names = "";
			CodonPositionsSet codSet = (CodonPositionsSet)data.getCurrentSpecsSet(CodonPositionsSet.class);
			for (int iw = 0; iw<4; iw++){
				String locs = codSet.getListOfMatches(iw);
				if (!StringUtil.blank(locs)) {
					String charSetName = "";
					if (iw==0) 
						charSetName = StringUtil.tokenize("nonCoding");
					else 
						charSetName = StringUtil.tokenize("codonPos" + iw);			
					numberCharSets++;
					list += "\n\tcharset " + charSetName + " = " +  locs + ";";
					if (!StringUtil.blank(names))
						names += ", ";
					names += charSetName;
				}
			}
		//	String codPos = ((DNAData)data).getCodonsAsNexusCharSets(numberCharSets, charSetList); // equivalent to list
			if (!StringUtil.blank(list)) {
				codPosPart += "\n\n [codon positions if you wish to use these]";
				codPosPart +=list;
				codPosPart += "\n\tpartition currentPartition = " + numberCharSets + ": " + names + ";";
				codPosPart +="\n\tset partition = currentPartition;\n\tlset applyto=(";
				for (int i = 1; i<=numberCharSets; i++) {
					codPosPart += i;
					if (i<numberCharSets) 
						codPosPart += ", ";
				}
				codPosPart += ");\n";
			}			
		}
		String standardPart = "";
		if (writeStandardPartition) {
			int numCharSets = 0;
			standardPart += "\n\n [currently specified groups if you wish to use these]";
			for (int i=0; i<parts.length; i++) {
				String q = ListableVector.getListOfMatches((Listable[])characterPartition.getProperties(), parts[i], CharacterStates.toExternal(0));
				if (q != null) {
					standardPart +=  "\n\tcharset " + StringUtil.simplifyIfNeededForOutput(parts[i].getName(),true) + " = " + q + ";";
					numCharSets++;
				}
			}
			if (numCharSets <=1)
				standardPart = "";
			else {
				standardPart += "\n\tpartition currentPartition = " + numCharSets + ": ";
				boolean firstTime = true;
				String nums = "";
				int num = 0;
				for (int i=0; i<parts.length; i++) {
					String q = ListableVector.getListOfMatches((Listable[])characterPartition.getProperties(), parts[i], CharacterStates.toExternal(0));
					if (q != null) {
						if (!firstTime){
							standardPart += ", ";
							nums += ", ";
						}
						firstTime = false;
						standardPart += StringUtil.simplifyIfNeededForOutput(parts[i].getName(), true);
						nums += Integer.toString(num+1);
						num++;
					}
				}
				standardPart +=";\n\tset partition = currentPartition;\n";  
				String s=getPostPartitionCommands(data.getTaxa());  
				if (StringUtil.notEmpty(s))
					standardPart +=s + "\n";
				standardPart +="\tlset applyto=(" + nums + ");\n";  
			}
		}
		sT += codPosPart + standardPart;
		if (nucleotides)
			sT += "\n\tlset nst=6 " + "rates=invgamma;";
		else if (!molecular)
			sT += "\n\tlset nst=1 " + "rates=gamma coding=variable;";
		if (data instanceof ProteinData)
			sT += "\n\tprset aamodelpr=mixed;";
		sT += "\n\tunlink statefreq=(all) revmat=(all) shape=(all) pinvar=(all); \n";
		String s=getPostUNLINKCommands();  
		if (StringUtil.notEmpty(s))
			sT +=s + "\n";
		sT += "\tprset applyto=(all) ratepr=variable;\n";
		s=getPostPRSETCommands();  
		if (StringUtil.notEmpty(s))
			sT +=s + "\n";
		sT += "\tmcmcp ngen= 10000000 relburnin=yes burninfrac=0.25 printfreq=1000  samplefreq=1000 nchains=4 savebrlens=yes;\n\tmcmc;\n\tsumt;\nend;";

		return sT;
	}
	/*.................................................................................................................*/
	public String getPostPartitionCommands(Taxa taxa) {
		return "";
	}
	/*.................................................................................................................*/
	public String getPostUNLINKCommands() {
		return "";
	}
	/*.................................................................................................................*/
	public String getPostPRSETCommands() {
		return "";
	}
	/*.................................................................................................................*/
	public boolean exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export
		Arguments args = new Arguments(new Parser(arguments), true);
		boolean usePrevious = args.parameterExists("usePrevious");
		CategoricalData data = (CategoricalData)getProject().chooseData(containerOfModule(), file, null, CategoricalState.class, "Select data to export");
		if (data ==null) {
			showLogWindow(true);
			logln("WARNING: No suitable data available for export to a file of format \"" + getName() + "\".  The file will not be written.\n");
			return false;
		}
		MesquiteString dir = new MesquiteString();
		MesquiteString fn = new MesquiteString();
		String suggested = fileName;
		if (file !=null)
			suggested = file.getFileName();
		MesquiteFile f;
		if (!usePrevious){
			if (!getExportOptions(data, data.anySelected(), data.getTaxa().anySelected()))
				return false;
		}
		String path = getPathForExport(arguments, suggested, dir, fn);
		if (path != null) {
			f = MesquiteFile.newFile(dir.getValue(), fn.getValue());
			if (f !=null){
				f.useSimplifiedNexus = true;
				f.useDataBlocks = useData;
				f.ambiguityToMissing = convertAmbiguities;
				f.simplifyNames =simplifyNames;
				f.openWriting(true);
				f.writeLine("#NEXUS" + StringUtil.lineEnding());
				if (!useData)
					f.writeLine(((TaxaManager)findElementManager(Taxa.class)).getTaxaBlock(data.getTaxa(), null, file));
				data.getMatrixManager().writeCharactersBlock(data, null, f, null);
				if (addendum != null)
					f.writeLine(addendum);
				f.closeWriting();
				return true;
			}
		}
		return false;
	}



}

