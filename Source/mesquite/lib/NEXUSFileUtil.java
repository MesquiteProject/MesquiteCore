package mesquite.lib;

import java.util.Vector;

import mesquite.categ.lib.CategoricalData;
import mesquite.categ.lib.DNAData;
import mesquite.charMatrices.ManageCharInclusion.ManageCharInclusion;
import mesquite.charMatrices.ManageCharWeights.ManageCharWeights;
import mesquite.lib.characters.CharInclusionSet;
import mesquite.lib.characters.CharSpecsSet;
import mesquite.lib.characters.CharWeightSet;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.CharacterModel;
import mesquite.lib.characters.CharacterPartition;
import mesquite.lib.characters.CharacterStates;
import mesquite.lib.characters.CharactersGroup;
import mesquite.lib.characters.CodonPositionsSet;
import mesquite.lib.characters.ModelSet;
import mesquite.lib.taxa.Taxa;
import mesquite.parsimony.ManageTypesets.ManageTypesets;
import mesquite.parsimony.lib.ParsimonyModelSet;

public class NEXUSFileUtil {

	/*.................................................................................................................*/

	public static String nexusStringForInclusionSet(CharSpecsSet specsSet, CharacterData data){
		if (specsSet ==null || !(specsSet instanceof CharInclusionSet))
			return null;
		CharInclusionSet inclusionSet = (CharInclusionSet)specsSet;
		String s= "";
		if (inclusionSet !=null) {
			String sT = ManageCharInclusion.nexusCoreStringForSpecsSet(specsSet, data);
			if (!StringUtil.blank(sT)) {
				s+= "\tEXSET " ;
				s += "* ";
				s+= StringUtil.tokenize(inclusionSet.getName()) + " ";
				s+= "  = "+  sT + ";" + StringUtil.lineEnding();
			}
		}
		return s;
	}
	/*.................................................................................................................*/

	public static String nexusStringForTypeSet(CharSpecsSet specsSet, CharacterData data){
		if (specsSet ==null || !(specsSet instanceof ParsimonyModelSet))
			return null;
		ModelSet modelSet = (ModelSet)specsSet;
		String s= "";
		if (modelSet !=null) {
			String sT = ManageTypesets.nexusCoreStringForSpecsSet(specsSet, data);
			if (!StringUtil.blank(sT)) {
				s+= "\tTYPESET " ;
				s += "* ";
				s+= StringUtil.tokenize(modelSet.getName()) + " ";
				s+= "  = "+  sT + ";" + StringUtil.lineEnding();
			}
		}
		return s;
	}
	/*.................................................................................................................*/

	public static String nexusStringForWeightSet(CharSpecsSet specsSet, CharacterData data){
		if (specsSet ==null || !(specsSet instanceof CharWeightSet))
			return null;
		CharWeightSet modelSet = (CharWeightSet)specsSet;
		String s= "";
		if (modelSet !=null) {
			String sT = ManageCharWeights.nexusCoreStringForSpecsSet(specsSet, data);
			if (!StringUtil.blank(sT)) {
				s+= "\tWTSET " ;
				s += "* ";
				s+= StringUtil.tokenize(modelSet.getName()) + " ";
				s+= "  = "+  sT + ";" + StringUtil.lineEnding();
			}
		}
		return s;
	}


	/*.................................................................................................................*/
	public static String getStandardPartitionNEXUSCommands(CharacterData data, boolean writeExcludedCharacters, boolean includeAsterisk, MesquiteInteger numParts){
		String standardPartitionSection = "";
		String standardPartition = "";
		String partitionCommand = "";
		CharactersGroup[] parts =null;
		CharacterPartition characterPartition = (CharacterPartition)data.getCurrentSpecsSet(CharacterPartition.class);
		if (characterPartition!=null) {
			parts = characterPartition.getGroups();
		}
		if (parts==null)
			return null;
		int numCharSets = 0;
		Listable[] partition = (Listable[])characterPartition.getProperties();
		if (!writeExcludedCharacters) 
			partition = data.removeExcludedFromListable(partition);
		String q;
		for (int i=0; i<parts.length; i++) {
			q = ListableVector.getListOfMatches(partition, parts[i], CharacterStates.toExternal(0));
			if (q != null) {
				standardPartition +=  "\n\tcharset " + StringUtil.simplifyIfNeededForOutput(parts[i].getName(),true) + " = " + q + ";";
				numCharSets++;
				if (!StringUtil.blank(partitionCommand))
					partitionCommand += ", ";
				partitionCommand += " part"+i + ":" + StringUtil.simplifyIfNeededForOutput(parts[i].getName(),true) ;
			}
		}
		q = ListableVector.getListOfMatches(partition, null, CharacterStates.toExternal(0));
		if (q != null) {
			standardPartition +=  "\n\tcharset unassigned = " + q + ";";
			numCharSets++;
			if (!StringUtil.blank(partitionCommand))
				partitionCommand += ", ";
			partitionCommand += " part" + parts.length + ": unassigned" ;
		}
		if (numParts!=null) 
			numParts.setValue(numCharSets);
		if (!StringUtil.blank(standardPartition)) {
			standardPartitionSection +=standardPartition;
			standardPartitionSection += "\n\tcharpartition ";
			if (includeAsterisk)
			standardPartitionSection += "* ";
			standardPartitionSection += "currentPartition = " + partitionCommand + ";\n";
		}	
		return standardPartitionSection;
	}
	/*.................................................................................................................*/
	public static String getCodPosPartitionNEXUSCommands(CharacterData data, boolean writeExcludedCharacters, boolean includeAsterisk, MesquiteInteger numParts){
		//codon positions if nucleotide
		String codPosPartitionSection = "";
		int numCharSets = 0;
		String charSetCommands = "";
		String partitionCommand = "";
		CodonPositionsSet codSet = (CodonPositionsSet)data.getCurrentSpecsSet(CodonPositionsSet.class);
		boolean[] include = null;
		if (!writeExcludedCharacters)
			include = data.getBooleanArrayOfIncluded();
		for (int iw = 0; iw<4; iw++){
			String locs = codSet.getListOfMatches(iw,0,include);
			if (!StringUtil.blank(locs)) {
				String charSetName = "";
				if (iw==0) 
					charSetName = StringUtil.tokenize("nonCoding");
				else 
					charSetName = StringUtil.tokenize("codonPos" + iw);			
				numCharSets++;
				charSetCommands += "\n\tcharset " + charSetName + " = " +  locs + ";";
				if (!StringUtil.blank(partitionCommand))
					partitionCommand += ", ";
				partitionCommand += " part"+iw + ":" + charSetName;
			}
		}
		//	String codPos = ((DNAData)data).getCodonsAsNexusCharSets(numberCharSets, charSetList); // equivalent to list
		if (numParts!=null) 
			numParts.setValue(numCharSets);
		if (!StringUtil.blank(charSetCommands)) {
			codPosPartitionSection +=charSetCommands;
			codPosPartitionSection += "\n\tcharpartition ";
			if (includeAsterisk)
				codPosPartitionSection += "* ";
			codPosPartitionSection += "currentPartition = " + partitionCommand + ";\n";
		}	
		return codPosPartitionSection;
	}
	/*.................................................................................................................*/
	public static boolean writeNEXUSSetsBlock(Taxa taxa, String dir, String fileName, String path, CategoricalData data, boolean useCodPosIfAvailable, boolean writeExcludedCharacters, boolean includeAsterisk, MesquiteInteger numParts) {
		if (path != null) {
			MesquiteFile f = MesquiteFile.newFile(dir, fileName);
			f.suppressReadWriteLogging = true;
			f.openWriting(true);
			f.writeLine("#NEXUS" + StringUtil.lineEnding());
			String setsBlock = getNEXUSSetsBlock(data,useCodPosIfAvailable, writeExcludedCharacters, includeAsterisk, numParts);
			if (StringUtil.notEmpty(setsBlock))
				f.writeLine(setsBlock + StringUtil.lineEnding());

			f.closeWriting();
			return true;
		}
		return false;
	}

	/*.................................................................................................................*/

	public static  String getNEXUSAssumptionBlock(CategoricalData data){
		StringBuffer sb = new StringBuffer();

		ParsimonyModelSet typeSet = (ParsimonyModelSet)data.getCurrentSpecsSet(ParsimonyModelSet.class);
		String typeSetString = nexusStringForTypeSet(typeSet,data);
		CharWeightSet wtSet = (CharWeightSet)data.getCurrentSpecsSet(CharWeightSet.class);
		String wtSetString = nexusStringForWeightSet(wtSet,data);
		CharInclusionSet inclusionSet = (CharInclusionSet)data.getCurrentSpecsSet(CharInclusionSet.class);
		String inclusionSetString = nexusStringForInclusionSet(inclusionSet,data);

		if (StringUtil.notEmpty(typeSetString) || StringUtil.notEmpty(wtSetString)|| StringUtil.notEmpty(inclusionSetString)) {
			sb.append("\nBEGIN assumptions;\n");
			if (StringUtil.notEmpty(typeSetString)){
				Vector modelsUsed = new Vector();
				for (int ic = 0; ic<typeSet.getNumChars(); ic++){ //checking to see if stepmatrices etc. are being used, and if so, include them
					CharacterModel cm = typeSet.getModel(ic);
					if (!cm.isBuiltIn() && modelsUsed.indexOf(cm)<0){
						modelsUsed.addElement(cm);
						String s = "\t"+ cm.getNEXUSCommand() + " ";  
						s += StringUtil.tokenize(cm.getName()) + " (" ;  
						s += StringUtil.tokenize(cm.getNEXUSClassName()) + ") = " + StringUtil.lineEnding();
						s += "\t\t"+ cm.getNexusSpecification()+";" + StringUtil.lineEnding(); 
						sb.append(s);
					}
				}
				sb.append(typeSetString);
			}
			if (StringUtil.notEmpty(wtSetString))
				sb.append(wtSetString);
			if (StringUtil.notEmpty(inclusionSetString))
				sb.append(inclusionSetString);
			sb.append("END;\n");
		}
		return sb.toString();
	}
	/*.................................................................................................................*/

	public static  String getNEXUSSetsBlock(CategoricalData data, boolean useCodPosIfAvailable, boolean writeExcludedCharacters, boolean includeAsterisk, MesquiteInteger numParts){
		StringBuffer sb = new StringBuffer();

		String partitions = "";
		if (useCodPosIfAvailable && data instanceof DNAData && ((DNAData)data).someCoding())
			partitions = getCodPosPartitionNEXUSCommands(data, writeExcludedCharacters, includeAsterisk, numParts);
		else
			partitions = getStandardPartitionNEXUSCommands(data, writeExcludedCharacters, includeAsterisk, numParts);

		if (StringUtil.notEmpty(partitions)) {
			sb.append("\nBEGIN sets;\n");
			sb.append(partitions);
			sb.append("END;\n");
		}
		return sb.toString();
	}	/*.................................................................................................................*/
	public static boolean writeNEXUSFile(Taxa taxa, String dir, String fileName, String path, CategoricalData data, boolean writeSimplifiedNEXUS, boolean useStandardizedTaxonNames, boolean writeOnlySelectedTaxa, boolean writeSetsBlock, boolean writeAssumptionsBlock, boolean useCodPosIfAvailable, boolean writeExcludedCharacters) {
		if (path != null) {
			MesquiteFile f = MesquiteFile.newFile(dir, fileName);
			f.suppressReadWriteLogging = true; //DAVIDCHECK: Debugg.println Moved away from static. Was only used by PAUPRunner. Find other way to suppress?
			f.openWriting(true);
			f.interleaveAllowed=false;
			f.useSimplifiedNexus=writeSimplifiedNEXUS;
			f.useDataBlocks=true;
			f.useStandardizedTaxonNames=useStandardizedTaxonNames;
			f.writeExcludedCharacters=writeExcludedCharacters;
			f.writeTaxaWithAllMissing = false;
			if (taxa.anySelected())
				f.writeOnlySelectedTaxa = writeOnlySelectedTaxa;
			f.writeLine("#NEXUS" + StringUtil.lineEnding());
			boolean nexusIDs = NexusBlock.suppressNEXUSIDS;
			NexusBlock.suppressNEXUSIDS = true;
			data.getMatrixManager().writeCharactersBlock(data, null, f, null);
			if (writeSetsBlock) {
				String setsBlock = getNEXUSSetsBlock(data,useCodPosIfAvailable, writeExcludedCharacters, true, null);
				if (StringUtil.notEmpty(setsBlock))
					f.writeLine(setsBlock + StringUtil.lineEnding());
			}
			if (writeAssumptionsBlock) {
				String assumptionsBlock = getNEXUSAssumptionBlock(data);
				if (StringUtil.notEmpty(assumptionsBlock))
					f.writeLine(assumptionsBlock + StringUtil.lineEnding());
			}
			NexusBlock.suppressNEXUSIDS = nexusIDs;

			//data.getMatrixManager().writeCharactersBlock(data, cB, file, progIndicator)
			f.closeWriting();
			return true;
		}
		return false;
	}

}
