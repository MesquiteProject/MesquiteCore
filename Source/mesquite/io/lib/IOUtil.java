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
import mesquite.lib.duties.TreesManager;

public class IOUtil {

	public static String translationTableFileName = "taxonNamesTranslationTable.txt";
	public static final String RAXMLSCORENAME = "RAxMLScore";
	public static final String RAXMLFINALSCORENAME = "RAxMLScore (Final Gamma-based)";
	/*.................................................................................................................*/

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

	/*.................................................................................................................*/
	/*.................................................................................................................*/

	public static void copyCurrentSpecSets(CharacterData sourceData, CharacterData destinationData){
		CharactersGroup[] parts =null;
		CharacterPartition characterPartition = (CharacterPartition)sourceData.getCurrentSpecsSet(CharacterPartition.class);
		if (characterPartition != null) {
			SpecsSet partitionCopy = characterPartition.cloneSpecsSet();
			destinationData.setCurrentSpecsSet(partitionCopy, CharacterPartition.class);
		}
		CharInclusionSet incl = (CharInclusionSet)sourceData.getCurrentSpecsSet(CharInclusionSet.class);
		if (incl != null) {
			destinationData.setCurrentSpecsSet(incl, CharInclusionSet.class);
		} 

	}
	/*.................................................................................................................*/

	public static String getMultipleModelRAxMLString(MesquiteModule mb, CharacterData data, boolean partByCodPos){
		boolean writeCodPosPartition = false;
		boolean writeStandardPartition = false;
		CharactersGroup[] parts =null;
		if (data instanceof DNAData)
			writeCodPosPartition = ((DNAData)data).someCoding();
		CharacterPartition characterPartition = (CharacterPartition)data.getCurrentSpecsSet(CharacterPartition.class);
		if (characterPartition == null && !writeCodPosPartition) {
			return null;
		} 
		if (characterPartition!=null) {
			parts = characterPartition.getGroups();
			writeStandardPartition = parts!=null;
		}

		if (!writeStandardPartition && !writeCodPosPartition) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		CharInclusionSet incl = (CharInclusionSet)data.getCurrentSpecsSet(CharInclusionSet.class);

		String codPosPart = "";
		boolean molecular = (data instanceof MolecularData);
		boolean nucleotides = (data instanceof DNAData);
		boolean protein = (data instanceof ProteinData);
		String standardPart = "";
		if (writeStandardPartition) {
			Listable[] partition = (Listable[])characterPartition.getProperties();
			partition = data.removeExcludedFromListable(partition);
			if (nucleotides) {
				String q;
				for (int i=0; i<parts.length; i++) {
					q = ListableVector.getListOfMatches(partition, parts[i], CharacterStates.toExternal(0), true, ",");
					if (q != null) {
						if (nucleotides)
							sb.append("DNA, " + StringUtil.simplifyIfNeededForOutput(data.getName()+"_"+parts[i].getName(), true) + " = " +  q + "\n");
					}
				}
				q = ListableVector.getListOfMatches(partition, null, CharacterStates.toExternal(0), true, ",");
				if (q != null) {
					if (nucleotides)
						sb.append("DNA, " + StringUtil.simplifyIfNeededForOutput(data.getName()+"_unassigned", true) + " = " +  q + "\n");
				}
			} else if (protein) {
				String[] rateModels = getRAxMLRateModels(mb, parts);
				String q;
				if (rateModels!=null) {
					for (int i=0; i<parts.length; i++) {
						q = ListableVector.getListOfMatches(partition, parts[i], CharacterStates.toExternal(0), true, ",");
						if (q != null && i<rateModels.length) {
							sb.append(rateModels[i]+", " + StringUtil.simplifyIfNeededForOutput(data.getName()+"_"+parts[i].getName(), true) + " = " +  q + "\n");
						}
					}
				}
			} else {  // non molecular
				for (int i=0; i<parts.length; i++) {
					String q = ListableVector.getListOfMatches(partition, parts[i], CharacterStates.toExternal(0), true, ",");
					if (q != null) {
						if (nucleotides)
							sb.append("MULTI, " + StringUtil.simplifyIfNeededForOutput(data.getName()+"_"+parts[i].getName(), true) + " = " +  q + "\n");
					}
				}
			} 
		} else if (writeCodPosPartition && partByCodPos) {//TODO: never accessed by Zephyr because in the only Zephyr call of this method, partByCodPos is false.
			//codon positions if nucleotide
			int numberCharSets = 0;
			boolean[] include = data.getBooleanArrayOfIncluded();
			CodonPositionsSet codSet = (CodonPositionsSet)data.getCurrentSpecsSet(CodonPositionsSet.class);
			for (int iw = 0; iw<4; iw++){
				String locs = codSet.getListOfMatches(iw,0, include, true);
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
	/*.................................................................................................................*/
	public static TreeVector readPhylipTrees (MesquiteModule mb, MesquiteProject mf, MesquiteFile file, String line, ProgressIndicator progIndicator, Taxa taxa, boolean permitTaxaBlockEnlarge, TaxonNamer namer, String treeNameBase, boolean addTreesToFile) {
		Parser treeParser = new Parser();
		treeParser.setQuoteCharacter((char)0);
		int numTrees = MesquiteInteger.infinite;
		if (line != null){
			treeParser.setString(line); 
			String token = treeParser.getNextToken();  // numTaxa
			numTrees = MesquiteInteger.fromString(token);
		}
		int iTree = 0;
		TreeVector trees = null;
		boolean abort = false;
		line = file.readNextDarkLine();		
		while (!StringUtil.blank(line) && !abort && (iTree<numTrees)) {
			treeParser.setString(line); //sets the string to be used by the parser to "line" and sets the pos to 0

			if (trees == null) {
				trees = new TreeVector(taxa);
				trees.setName("Imported trees");
			}
			MesquiteTree t = new MesquiteTree(taxa);
			t.setPermitTaxaBlockEnlargement(permitTaxaBlockEnlarge);
			//t.setTreeVector(treeVector);
			t.readTree(line,namer, null, "():;,[]\'");  //tree reading adjusted to use Newick punctuation rather than NEXUS
			/*MesquiteInteger pos = new MesquiteInteger(0);
			treeParser.setString(line);
			readClade(t, t.getRoot());
			t.setAsDefined(true);*/
			t.setName(treeNameBase + (iTree+1));
			trees.addElement(t, false);


			iTree++;
			line = file.readNextDarkLine();		
			if (file.getFileAborted())
				abort = true;
		}
		if (trees != null && addTreesToFile)
			trees.addToFile(file,mf,(TreesManager)mb.findElementManager(TreeVector.class));
		return trees;
		
	}

	/*.................................................................................................................*/

	public static void readRAxMLInfoFile(MesquiteModule mb, String fileContents, boolean verbose, TreeVector trees, DoubleArray finalValues, DoubleArray optimizedValues) {
		if (finalValues==null) return;
		Parser parser = new Parser(fileContents);
		parser.setAllowComments(false);
		parser.allowComments = false;
		int count =0;

		String line = parser.getRawNextDarkLine();
		if (verbose)
			mb.logln("\nSummary of RAxML Search");

		while (!StringUtil.blank(line) && count < finalValues.getSize()) {
			if (line.startsWith("Inference[")) {
				Parser subParser = new Parser();
				subParser.setString(line);
				String token = subParser.getFirstToken();
				while (!StringUtil.blank(token) && ! subParser.atEnd()){
					if (token.indexOf("likelihood")>=0) {
						token = subParser.getNextToken();
						finalValues.setValue(count,-MesquiteDouble.fromString(token));
						//	finalScore[count].setValue(finalValues[count]);
						mb.logln("RAxML Run " + (count+1) + " ln L = -" + finalValues.getValue(count));
					}
					token = subParser.getNextToken();
				}
				count++;
			}
			parser.setAllowComments(false);
			line = parser.getRawNextDarkLine();
		}
		
		count =0;

		if (optimizedValues!=null) {
			while (!StringUtil.blank(line) && count < optimizedValues.getSize()) {
				if (line.startsWith("Inference[")) {
					Parser subParser = new Parser();
					subParser.setString(line);
					String token = subParser.getFirstToken();   // should be "Inference"
					while (!StringUtil.blank(token) && ! subParser.atEnd()){
						if (token.indexOf("Likelihood")>=0) {
							token = subParser.getNextToken(); // :
							token = subParser.getNextToken(); // -
							optimizedValues.setValue(count,-MesquiteDouble.fromString(token));
						}
						token = subParser.getNextToken();
					}
					count++;
				}
				parser.setAllowComments(false);
				line = parser.getRawNextDarkLine();
			}
		}


		double bestScore =MesquiteDouble.unassigned;
		int bestRun = MesquiteInteger.unassigned;
		for (int i=0; i<trees.getNumberOfTrees(); i++) {
			Tree newTree = trees.getTree(i);
			if (MesquiteDouble.isCombinable(finalValues.getValue(i))){
				MesquiteDouble s = new MesquiteDouble(-finalValues.getValue(i));
				s.setName(RAXMLSCORENAME);
				((Attachable)newTree).attachIfUniqueName(s);
			}
			if (MesquiteDouble.isCombinable(optimizedValues.getValue(i))){
				MesquiteDouble s = new MesquiteDouble(-optimizedValues.getValue(i));
				s.setName(IOUtil.RAXMLFINALSCORENAME);
				((Attachable)newTree).attachIfUniqueName(s);
			}

			if (MesquiteDouble.isCombinable(finalValues.getValue(i)))
				if (MesquiteDouble.isUnassigned(bestScore)) {
					bestScore = finalValues.getValue(i);
					bestRun = i;
				}
				else if (bestScore>finalValues.getValue(i)) {
					bestScore = finalValues.getValue(i);
					bestRun = i;
				}
		}
		if (MesquiteInteger.isCombinable(bestRun)) {
			Tree t = trees.getTree(bestRun);

			String newName = t.getName() + " BEST";
			if (t instanceof AdjustableTree )
				((AdjustableTree)t).setName(newName);
		}

	}
}
