/* Mesquite (package mesquite.io).  Copyright 2000 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)

Modifications:
28 July 01 (WPM): checked for treeVector == null on export & used getCompatibleFileElements
*/
package mesquite.io.lib;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;

//TODO:   have option to not write empty taxa?

/* ============  a file interpreter for Phylip files ============*/

public abstract class InterpretPhylip extends FileInterpreterITree {
	Class[] acceptedClasses;
	TreeVector treeVector = null;
/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		acceptedClasses = new Class[] {ProteinState.class, DNAState.class, CategoricalState.class};
 		return true;  //make this depend on taxa reader being found?)
  	 }
  	 
	/*.................................................................................................................*/
	public boolean initializeExport(Taxa taxa) {  
		 return true;  
	}
	/*.................................................................................................................*/
	public String preferredDataFileExtension() {  
		return "phy";
	}
	/*.................................................................................................................*/
	public boolean initializeTreeImport(MesquiteFile file, Taxa taxa) {  
		 return true;  
	}
	/*.................................................................................................................*/
	public boolean canExportEver() {  
		 return true;  //
	}
/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		 return project.getNumberCharMatrices(acceptedClasses) > 0;  //
	}
/*.................................................................................................................*/
	public boolean canExportData(Class dataClass) {  
		 for (int i = 0; i<acceptedClasses.length; i++)
		 	if (dataClass==acceptedClasses[i])
		 		return true;
		 return false; 
	}
/*.................................................................................................................*/
	public boolean canImport() {  
		 return true;
	}
/*.................................................................................................................*/
	public abstract CharacterData createData(CharactersManager charTask, Taxa taxa);
/*.................................................................................................................*/
	boolean previousInterleaved = false;
	public boolean getInterleaved(){
		if (okToInteractWithUser(CAN_PROCEED_ANYWAY, "Asking about interleave")){
			String helpString = "Sequential matrices have all of the data for each taxon on one row.  Interleaved matrices are organized in blocks, with the first block having data for, say, characters 1-100 for all taxa, then "
					+ "a second block with data for characters 101-200 for all taxa, and so on.";
			previousInterleaved = AlertDialog.query(module.containerOfModule(), "Interleaved or sequential?", "Is the matrix interleaved or sequential?", "Interleaved", "Sequential", 1, helpString);
		}
		return previousInterleaved;
	}	
/*.................................................................................................................*/
	public abstract void setPhylipState(CharacterData data, int ic, int it, char c);
/*...............................................  read tree ....................................................*
	/** Continues reading a tree description, starting at node "node" and the given location on the string*
	private boolean readClade(MesquiteTree tree, int node) {
			
		String c = treeParser.getNextToken();
		c = c.trim();
		if (",".equals(c)) {
			c = treeParser.getNextToken();
			c = c.trim();
		}
		if ("(".equals(c)){  //internal node
			int sprouted = tree.sproutDaughter(node, false);
			readClade(tree, sprouted);
			boolean keepGoing = true;
			while (keepGoing) {
				int loc = treeParser.getPosition();
				String next = treeParser.getNextToken();
				if (",".equals(next)) 
					next = treeParser.getNextToken();
				if (")".equals(next))
					keepGoing = false;
				else {
					treeParser.setPosition(loc);
					sprouted = tree.sproutDaughter(node, false);
					keepGoing = readClade(tree, sprouted);
				}
			}
			return true;
		}
		else if (")".equals(c)) {
			return false;
		}
	  	else {
			int taxonNumber = tree.getTaxa().whichTaxonNumber(c);
			if (taxonNumber >=0){ //taxon successfully found
				if (tree.nodeOfTaxonNumber(taxonNumber)<=0){  // first time taxon encountered
					tree.setTaxonNumber(node, taxonNumber, false);
				 	return true;
				 }
			 }
			return false;
	  	}
	  	
	}
	
	
	
/*.................................................................................................................*/
	public TreeVector readPhylipTrees (MesquiteProject mf, MesquiteFile file, String line, ProgressIndicator progIndicator, Taxa taxa) {
		return IOUtil.readPhylipTrees(this,mf, file, line, progIndicator, taxa, false, null, getTreeNameBase(), true);
	}	
	
	public void setTaxonNameLength(int value) {
		taxonNameLength = value;
	}
	
	public String getTreeNameBase () {
		return "Imported tree ";
	}
/*.................................................................................................................*/
	public void readTreeFile(MesquiteProject mf, MesquiteFile file, String arguments) {
		Taxa taxa = getProject().chooseTaxa(containerOfModule(), "Of what taxa are these trees composed?");
		if (taxa== null) 
			return;
/*
		treeVector = new TreeVector(taxa);
		String name;
		for (int it=0; it<taxa.getNumTaxa(); it++) {
			name = taxa.getTaxonName(it);
			if (name.length()>10)
				treeVector.setTranslationLabel(name.substring(0,9), name, false);
			else
				treeVector.setTranslationLabel(name, name, false);
		}
*/
		incrementMenuResetSuppression();
		//file.linkProgressIndicator(progIndicator);
		if (file.openReading()) {
			String line = file.readNextDarkLine();		
			readPhylipTrees(mf, file, line, null, taxa);
			finishImport(null, file, false );
		}
		decrementMenuResetSuppression();
	}
/*.................................................................................................................*/
	public void readFile(MesquiteProject mf, MesquiteFile file, String arguments) {
		boolean interleaved = getInterleaved();
		
		incrementMenuResetSuppression();
		ProgressIndicator progIndicator = new ProgressIndicator(mf,"Importing File "+ file.getName(), file.existingLength());
		progIndicator.start();
		file.linkProgressIndicator(progIndicator);
		if (file.openReading()) {
			TaxaManager taxaTask = (TaxaManager)findElementManager(Taxa.class);
			 CharactersManager charTask = (CharactersManager)findElementManager(CharacterData.class);
			
			Taxa taxa = taxaTask.makeNewTaxa("Taxa", 0, false);
			taxa.addToFile(file, getProject(), taxaTask);
			CategoricalData data = (CategoricalData)createData(charTask,taxa);
			data.addToFile(file, getProject(), null);
			
			String token;
			char c;
			int numTaxa = 0;
			int numChars = 0;

			StringBuffer sb = new StringBuffer(1000);
			file.readLine(sb);
			String line = sb.toString();
			parser.setString(line); 

			token = parser.getNextToken();  // numTaxa
			numTaxa = MesquiteInteger.fromString(token, "Mesquite does not currently accept PHYLIP files with options included at the top of the file", false,true);
			if (numTaxa==MesquiteInteger.impossible) {
				progIndicator.goAway();
				file.close();
				decrementMenuResetSuppression();
				return;
			}

			token = parser.getNextToken();  // numchars
			numChars = MesquiteInteger.fromString(token, "Mesquite does not currently accept PHYLIP files with options included at the top of the file", false,true);
			if (numChars==MesquiteInteger.impossible) {
				progIndicator.goAway();
				file.close();
				decrementMenuResetSuppression();
				return;
			}

			boolean wassave = data.saveChangeHistory;
			data.saveChangeHistory = false;
			taxa.addTaxa(-1, numTaxa, true);
			data.addCharacters(-1, numChars, false);
			line = file.readNextDarkLine();   // reads first data line
			
			boolean abort = false;
			int block = 1;
			int it=0;
			int nextCharToRead = 0;
	// first block
			while (!StringUtil.blank(line) && !abort && (it<numTaxa)) {
				parser.setString(line); //sets the string to be used by the parser to "line" and sets the pos to 0
				token = "";
				for (int taxonNameChar=0; taxonNameChar<120; taxonNameChar++) {  //no longer assumes standard 10-character taxon names; uses whitespace to find name
					c=parser.getNextChar();
					if (c=='\0' || StringUtil.whitespace(c, null))
						break;
					token+=c;
				}
				Taxon t = taxa.getTaxon(it);
				if (t!=null) {
					if (interleaved)
						progIndicator.setText("Reading block " + block + ", taxon "+it );
					else
						progIndicator.setText("Reading taxon "+it );
					t.setName(StringUtil.deTokenize(token).trim());
					for (int ic=0; ic<numChars; ic++) {
						c=parser.nextDarkChar();
						if (c=='\0') {
							if ((!interleaved) && (ic<numChars)){
								line = file.readNextDarkLine();	
								parser.setString(line);
							}
							else
								break;
						}
						setPhylipState(data, ic, it, c);
						if (it==0)
							nextCharToRead++;
					}
				}
				it++;
				line = file.readNextDarkLine();		
				if (file.getFileAborted())
					abort = true;
			}

// later blocks
			if ((interleaved) && (!abort)) {
				int startChar;
				while (!StringUtil.blank(line) && !abort && (nextCharToRead<=numChars)) {
					block++;
					startChar=nextCharToRead;
					it=0;
					while (!StringUtil.blank(line) && !abort && (it<numTaxa)) {
						parser.setString(line); //sets the string to be used by the parser to "line" and sets the pos to 0
						Taxon t = taxa.getTaxon(it);
						if (t!=null) {
							progIndicator.setText("Reading block " + block + ", taxon "+it );
							for (int ic=startChar; ic<numChars; ic++) {
								c=parser.nextDarkChar();
								if (c=='\0')
									break;
								setPhylipState(data,ic, it, c);
								if (it==0)
									nextCharToRead++;
							}
						}
						it++;
						line = file.readNextDarkLine();		
						if (file.getFileAborted())
							abort = true;
					}
				}
			}
			
			if (!StringUtil.blank(line)) // then we have trees
				readPhylipTrees(mf, file, line, progIndicator, taxa);
			data.saveChangeHistory = wassave;
			data.resetCellMetadata();
			finishImport(progIndicator, file, abort);
		}
		decrementMenuResetSuppression();
	}
	


/* ============================  exporting ============================*/
	public boolean exportInterleaved=false;
	public boolean exportTrees=true;
	public boolean localWriteExcludedChars = true;
	public boolean userSpecifiedWriteExcludedChars = false;
	
	/*.................................................................................................................*/
	protected int taxonNameLength = 10;
	protected TaxonNamer taxonNamer = null;
	
	public void setTaxonNamer(TaxonNamer namer) {
		this.taxonNamer = namer;
	}

	public boolean getExportOptions(boolean dataSelected, boolean taxaSelected){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		PhylipExporterDialog exportDialog = new PhylipExporterDialog(this,containerOfModule(), "Export Phylip Options", buttonPressed);
		
		
		exportDialog.completeAndShowDialog(dataSelected, taxaSelected);
			
		boolean ok = (exportDialog.query(dataSelected, taxaSelected)==0);
		if (ok)
			taxonNameLength = exportDialog.getTaxonNamesLength();
		exportDialog.dispose();
		return ok;
	}	
	TreeVector previousVector;
	/*.................................................................................................................*/
	public abstract CharacterData findDataToExport(MesquiteFile file, String arguments);
	/*.................................................................................................................*/
	public TreeVector findTreesToExport(MesquiteFile file, Taxa taxa, String arguments, boolean usePrevious){
		if (usePrevious)
			return previousVector;
		Listable[] treeVectors = getProject().getCompatibleFileElements(TreeVector.class, taxa);
		TreeVector treeVector;
		if (treeVectors.length==0)
			return null;
		if (treeVectors.length==1)
			treeVector = (TreeVector)treeVectors[0];
		else
			treeVector = (TreeVector)ListDialog.queryList(containerOfModule(), "Include trees in file?", "Include trees in file?", MesquiteString.helpString, treeVectors, 0);
		previousVector = treeVector;
		return treeVector;
	}
	/*.................................................................................................................*/
	public abstract void appendPhylipStateToBuffer(CharacterData data, int ic, int it, StringBuffer outputBuffer) ;
	/*.................................................................................................................*/
	protected void exportTrees(Taxa taxa, TreeVector treeVector, StringBuffer outputBuffer) { 
		Tree tree;
		if (treeVector !=null && treeVector.size()>0) {
			outputBuffer.append(""+treeVector.size() + getLineEnding());
			for (int iTree = 0; iTree < treeVector.size(); iTree++) {
				tree = (Tree)treeVector.elementAt(iTree);
				outputBuffer.append(tree.writeTree(Tree.BY_NUMBERS));  //or Tree.BY_NUMBERS  or Tree.BY_NAMES
				// if do it BY_NAMES, make sure you truncate the taxon names to 10 characters!!
				outputBuffer.append(getLineEnding());
			}
		}
	}
	int charWritten = 0;
	/*.................................................................................................................*/
	public void exportBlock(Taxa taxa, CharacterData data, StringBuffer outputBuffer, int startChar, int blockSize, boolean writeTaxonNames) { 
		int numTaxa = taxa.getNumTaxa();
		int numChars = data.getNumChars();
		int counter;
		String pad = "          ";
		while (pad.length() < taxonNameLength)
			pad += "  ";
		
		for (int it = 0; it<numTaxa; it++){
			if ((!writeOnlySelectedTaxa || taxa.getSelected(it)) && (writeTaxaWithAllMissing || data.hasDataForTaxon(it, writeExcludedCharacters))){
				if (writeTaxonNames) {   // first block
					String name = "";
					if (taxonNamer!=null)
						name = taxonNamer.getNameToUse(taxa,it)+pad;
					else
						name = (taxa.getTaxonName(it)+ pad);
					name = name.substring(0,taxonNameLength);
					name = StringUtil.blanksToUnderline(StringUtil.stripTrailingWhitespace(name));
					
					outputBuffer.append(name);
				//	if (taxonNameLength>name.length())
						for (int i=0;i<taxonNameLength-name.length()+1; i++)
						outputBuffer.append(" ");
				}
				//outputBuffer.append(" ");
				counter = 1;
				for (int ic = startChar; ic<numChars; ic++) {
					if ((!writeOnlySelectedData || (data.getSelected(ic))) && (writeExcludedCharacters || data.isCurrentlyIncluded(ic))){
						int currentSize = outputBuffer.length();
						appendPhylipStateToBuffer(data, ic, it, outputBuffer);
						if (it==0)
							charWritten++;
						if (outputBuffer.length()-currentSize>1) {
							alert("Sorry, this data matrix can't be exported to this format (some character states aren't represented by a single symbol [char. " + CharacterStates.toExternal(ic) + ", taxon " + Taxon.toExternal(it) + "])");
							return;
						}
						if (counter>=blockSize)
							break;
						counter++;
					}
				}
				outputBuffer.append(getLineEnding());
			}
		}
	}
	/*.................................................................................................................*/
 	public  StringBuffer getDataAsFileText(MesquiteFile file, CharacterData data) {
		if (data==null)
			return null;
		Taxa taxa = data.getTaxa();
		initializeExport(taxa);
		int numTaxa = taxa.getNumTaxa();
		int numChars = data.getNumChars();
		if (file != null){
			writeTaxaWithAllMissing = file.writeTaxaWithAllMissing;
			writeExcludedCharacters = file.writeExcludedCharacters;
		}
		int countTaxa = 0;
		for (int it = 0; it<numTaxa; it++)
			if ((!writeOnlySelectedTaxa || taxa.getSelected(it)) && (writeTaxaWithAllMissing || data.hasDataForTaxon(it, writeExcludedCharacters)))
				countTaxa++;
		numTaxa = countTaxa;
		StringBuffer outputBuffer = new StringBuffer(numTaxa*(20 + numChars));

		outputBuffer.append(Integer.toString(numTaxa)+" ");

		if (!writeExcludedCharacters)
			outputBuffer.append(Integer.toString(data.getNumCharsIncluded())+this.getLineEnding());		
		else
			outputBuffer.append(Integer.toString(numChars)+this.getLineEnding());		

		exportBlock(taxa, data, outputBuffer, 0, numChars, true);
		return outputBuffer;
	}
	/*.................................................................................................................*/
 	public boolean exportMultipleMatrices(){
 		return false;
 	}
	/*.................................................................................................................*/
 	public void setExportTrees(boolean exportTrees){
 		this.exportTrees = exportTrees;
 	}
	/*.................................................................................................................*/
 	public boolean getExportTrees(){
 		return exportTrees;
 	}
	/*.................................................................................................................*/
	public boolean exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export
		Arguments args = new Arguments(new Parser(arguments), true);
		boolean usePrevious = args.parameterExists("usePrevious");

		CharacterData data = null;
		int numMatrices =1;
		
		Taxa t = null;
		Taxa taxa = null;
		if (exportMultipleMatrices()) {
			//data = findDataToExport(file, arguments);
			t = getProject().chooseTaxa(containerOfModule(), "Select taxa to export", false);
			numMatrices = getProject().getNumberCharMatricesVisible(CategoricalState.class);
			data = getProject().getCharacterMatrixVisible(t, 0, CategoricalState.class);
			taxa = data.getTaxa();
		} else {
			data = findDataToExport(file, arguments);
			if (data != null){
				t = data.getTaxa();
				taxa = data.getTaxa();
			}

		}
		boolean dataAnySelected = false;
		if (data != null)
			dataAnySelected =data.anySelected();
		boolean taxaAnySelected = false;
		if (taxa !=null)
			taxaAnySelected= taxa.anySelected();
		if (!MesquiteThread.isScripting() && !usePrevious)
			if (!getExportOptions(dataAnySelected, taxaAnySelected))  // querying about options
				return false;

		if (userSpecifiedWriteExcludedChars) {
			file.writeExcludedCharacters= localWriteExcludedChars;
			writeExcludedCharacters = localWriteExcludedChars;
		}
		userSpecifiedWriteExcludedChars= false;

		TreeVector trees = null;
		if (getExportTrees()) {
			trees = findTreesToExport(file, t, arguments, usePrevious);
		}
		if (data ==null && trees == null) {
			showLogWindow(true);
			logln("WARNING: No suitable data or trees available for export to a file of format \"" + getName() + "\".  The file will not be written.\n");
			return false;
		}
		StringBuffer outputBuffer = new StringBuffer(100);
		int numCharWrite=0;
		boolean firstTimeThrough = true;
		initializeExport(taxa);

		if (data!=null) {
			for (int im = 0; im<numMatrices; im++) {
				if (exportMultipleMatrices()) {
					data = getProject().getCharacterMatrixVisible(t, im, CategoricalState.class);
					if (data==null) continue;
				} 
				if (!writeExcludedCharacters){
					numCharWrite +=  data.numCharsCurrentlyIncluded(this.writeOnlySelectedData);
				} else {
					numCharWrite +=  data.numberSelected(this.writeOnlySelectedData);
				}
			}
		}
		charWritten=0;

		for (int im = 0; im<numMatrices; im++) {
			if (exportMultipleMatrices()) {
				data = getProject().getCharacterMatrixVisible(t, im, CategoricalState.class);
				if (data==null) continue;
			}
			if (data == null && getExportTrees())
				taxa = trees.getTaxa();
			int numTaxa = taxa.getNumTaxa();

			int numTaxaWrite;
			int countTaxa = 0;
			for (int it = 0; it<numTaxa; it++)
				if ((!writeOnlySelectedTaxa || taxa.getSelected(it)) && (writeTaxaWithAllMissing || (data!=null && data.hasDataForTaxon(it))))
					countTaxa++;
			numTaxaWrite = countTaxa;

			int numChars = 0;

			
			if (data != null){
				numChars = data.getNumChars();
				if (firstTimeThrough) {
					outputBuffer.append(Integer.toString(numTaxaWrite)+" ");
					outputBuffer.append(Integer.toString(numCharWrite)+this.getLineEnding());
				}
				int blockSize=50;

				if (exportInterleaved || exportMultipleMatrices()){
					int ic = 0;
					while (ic<numChars) {
						int endChar = ic+blockSize;
						int count=0;
						boolean blockWritten = false;
						for (int ic2=ic; ic2<numChars && !blockWritten; ic2++) {
							if ((this.writeExcludedCharacters || data.isCurrentlyIncluded(ic2)) && (!this.writeOnlySelectedData || data.getSelected(ic2))){  // this character needs to be written
								count++;
								if (count==blockSize || ic2==numChars-1){
									endChar = ic2;
									exportBlock(taxa, data, outputBuffer, ic, blockSize, firstTimeThrough);
									outputBuffer.append(getLineEnding());
									ic=endChar+1;
									blockWritten=true;
									firstTimeThrough = false;
								}
							} else {
								if (ic2==numChars-1 && !blockWritten){  //at end
									endChar = ic2;
									if (count>0){  //only write if there is something to write
										exportBlock(taxa, data, outputBuffer, ic, blockSize, firstTimeThrough);
										outputBuffer.append(getLineEnding());
										firstTimeThrough = false;
									}
									ic=endChar+1;
									blockWritten=true;
								}
							}
						}
					}
				}
				else
					exportBlock(taxa, data, outputBuffer, 0, numChars, true);
			}
		}
		if (charWritten!=numCharWrite)
			MesquiteMessage.warnProgrammer("Warning: the number of characters written does not match expectation.");
		

		
		if (trees!=null)
			exportTrees(taxa, trees, outputBuffer);
		
		saveExportedFileWithExtension(outputBuffer, arguments, "phy");
		writeExtraFiles(taxa);
		return true;
	}

	/*.................................................................................................................*/
	public void writeExtraFiles(Taxa taxa){
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Phylip file";
   	 }
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Imports and exports Phylip files." ;
   	 }
	/*.................................................................................................................*/
   	 
   	 
}

