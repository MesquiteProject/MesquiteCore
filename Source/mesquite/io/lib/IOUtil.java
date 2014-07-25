package mesquite.io.lib;

import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.MolecularData;
import mesquite.categ.lib.ProteinData;
import mesquite.lib.*;
import mesquite.lib.characters.CharInclusionSet;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.CharacterPartition;
import mesquite.lib.characters.CharacterStates;
import mesquite.lib.characters.CharactersGroup;
import mesquite.lib.characters.CodonPositionsSet;

public class IOUtil {


	public static String[] getRAxMLRateModels(MesquiteModule mb, CharactersGroup[] parts){
		if (parts==null || parts.length==0 || parts.length>20)
			return null;
		String[] rateModels = new String[parts.length];
		for (int i=0; i<rateModels.length; i++)
			rateModels[i] = "JTT";

		if (!MesquiteThread.isScripting()) {
			MesquiteInteger buttonPressed = new MesquiteInteger(1);
			ExtensibleDialog dialog = new ExtensibleDialog(mb.containerOfModule(), "Protein Rate Models",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
			dialog.addLabel("Protein Rate Models");

			SingleLineTextField[] modelFields = new SingleLineTextField[parts.length];
			for (int i=0; i<rateModels.length; i++)
				modelFields[i] = dialog.addTextField(parts[i].getName()+":", rateModels[i], 20);

			dialog.completeAndShowDialog(true);
			if (buttonPressed.getValue()==0)  {
				for (int i=0; i<rateModels.length; i++)
					rateModels[i] = modelFields[i].getText();
			}
			dialog.dispose();
			if (buttonPressed.getValue()==0)
				return rateModels;
			return null;
		}
		return rateModels;
	}


	public static String getMultipleModelRAxMLString(MesquiteModule mb, CharacterData data, boolean partByCodPos){
		boolean writeCodPosPartition = false;
		boolean writeStandardPartition = false;
		CharactersGroup[] parts =null;
		if (data instanceof DNAData)
			writeCodPosPartition = ((DNAData)data).someCoding();
		CharacterPartition characterPartition = (CharacterPartition)data.getCurrentSpecsSet(CharacterPartition.class);
		if (characterPartition == null && !writeCodPosPartition)
			return null;
		if (characterPartition!=null) {
			parts = characterPartition.getGroups();
			writeStandardPartition = parts!=null;
		}

		if (!writeStandardPartition && !writeCodPosPartition) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		CharInclusionSet incl = null;

		String codPosPart = "";
		boolean molecular = (data instanceof MolecularData);
		boolean nucleotides = (data instanceof DNAData);
		boolean protein = (data instanceof ProteinData);
		String standardPart = "";
		if (writeStandardPartition) {
			if (nucleotides) {
				for (int i=0; i<parts.length; i++) {
					String q = ListableVector.getListOfMatches((Listable[])characterPartition.getProperties(), parts[i], CharacterStates.toExternal(0), true, ",");
					if (q != null) {
						if (nucleotides)
							sb.append("DNA, " + StringUtil.simplifyIfNeededForOutput(data.getName()+"_"+parts[i].getName(), true) + " = " +  q + "\n");
					}
				}
			} else if (protein) {
				String[] rateModels = getRAxMLRateModels(mb, parts);
				if (rateModels!=null) {
					for (int i=0; i<parts.length; i++) {
						String q = ListableVector.getListOfMatches((Listable[])characterPartition.getProperties(), parts[i], CharacterStates.toExternal(0), true, ",");
						if (q != null && i<rateModels.length) {
							sb.append(rateModels[i]+", " + StringUtil.simplifyIfNeededForOutput(data.getName()+"_"+parts[i].getName(), true) + " = " +  q + "\n");
						}
					}
				}
			} else {  // non molecular
				for (int i=0; i<parts.length; i++) {
					String q = ListableVector.getListOfMatches((Listable[])characterPartition.getProperties(), parts[i], CharacterStates.toExternal(0), true, ",");
					if (q != null) {
						if (nucleotides)
							sb.append("MULTI, " + StringUtil.simplifyIfNeededForOutput(data.getName()+"_"+parts[i].getName(), true) + " = " +  q + "\n");
					}
				}
			} 
		} else if (writeCodPosPartition && partByCodPos) {//TODO: never accessed because in the only call of this method, partByCodPos is false.
			//codon positions if nucleotide
			int numberCharSets = 0;
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
					sb.append( "DNA, " + StringUtil.simplifyIfNeededForOutput(data.getName()+"_"+charSetName,true) + " = " +  locs + "\n");
				}
			}
			//	String codPos = ((DNAData)data).getCodonsAsNexusCharSets(numberCharSets, charSetList); // equivalent to list
		}

		return sb.toString();
	}


}
