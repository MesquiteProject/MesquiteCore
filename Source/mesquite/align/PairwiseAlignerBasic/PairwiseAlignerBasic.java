package mesquite.align.PairwiseAlignerBasic;

import mesquite.align.lib.*;
import mesquite.categ.lib.*;
import mesquite.lib.CommandRecord;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteTrunk;

public class PairwiseAlignerBasic extends TwoSequenceAligner {
	
	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		// TODO load prefs?  (see PhredPrap for example)
		//put in loadPreferences and storePreferences
		// processSinglePreferenceForXML ... 
        // preparePreferencesForXML
		return true;
	}

	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}

	/**
	 * Override method in superclass
	 */
	
   	public long[][] alignSequences(long[] A_withGaps, long[] B_withGaps, boolean returnAlignment, MesquiteNumber score, CategoricalState state, CommandRecord commandRec) {
   		MesquiteInteger gapOpen = new MesquiteInteger();
   		MesquiteInteger gapExtend = new MesquiteInteger();
  		int alphabetLength = state.getMaxPossibleState()+1;
  		int subs[][] = AlignUtil.getDefaultCosts(gapOpen, gapExtend, alphabetLength);  
  		  		
   		PairwiseAligner pa = new PairwiseAligner(false,subs,gapOpen.getValue(), gapExtend.getValue(), alphabetLength);
		return pa.alignSequences(A_withGaps,B_withGaps,returnAlignment, score);
	}
	
	public String getName() {
		return "Basic Pairwise Aligner";
	}

 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Performs a basic pairwise alignment." ;
   	 }

   	/*.................................................................................................................*/
	/*	public boolean queryOptions() {
			MesquiteInteger buttonPressed = new MesquiteInteger(1);
			ExtensibleDialog queryFilesDialog = new ExtensibleDialog(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Pairwise Alignmenr File Location & Options",buttonPressed);  
			queryFilesDialog.addLabel("Pairwise Alignment - File Location & Options");

			
			subMatrixPathField = queryFilesDialog.addTextField("Substitution Matrix path:", null, 40);
			Button clustalBrowseButton = queryFilesDialog.addAListenedButton("Browse...",null, this);
			clustalBrowseButton.setActionCommand("subMatrixBrowse");

			RadioButtons rb = queryFilesDialog.addRadioButtons(new String[]{"Cost (minimization)","Similarity (maximization)"},0);
			SingleLineTextField gapOpenField = queryFilesDialog.addTextField("Gap opening penalty:",""+gapOpenDefault, 3);
			SingleLineTextField gapExtendField = queryFilesDialog.addTextField("Gap extension penalty:",""+gapExtendDefault, 3);
			
			queryFilesDialog.completeAndShowDialog(true);
			if (buttonPressed.getValue()==0)  {
		
				gapOpen = Integer.parseInt(gapOpenField.getText());
				gapExtend = Integer.parseInt(gapExtendField.getText());
				gapOpen -= gapExtend; // internal representation of gap startup cost is that first gap char costs gapOpen + gapExtend
				maximize = rb.getValue(); 
				
				String filePath = subMatrixPathField.getText();
				
				MesquiteFile dataFile =MesquiteFile.open(true, filePath);
				if (dataFile==null || StringUtil.blank(dataFile.getFileName()))
					return false;
					
				String contents = MesquiteFile.getFileContentsAsString(filePath);
				if (StringUtil.blank(contents)) 
					return false;
				
				if (!readMatrix(contents))
					return false;
				
			}
			queryFilesDialog.dispose();
							
			return (buttonPressed.getValue()==0);
		}*/
	
/*	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().equalsIgnoreCase("subMatrixBrowse")) {
 			
 			MesquiteString directoryName= new MesquiteString();
 			MesquiteString fileName= new MesquiteString();
 			String filePath = MesquiteFile.openFileDialog("Choose file containing the substitution matrix...", directoryName, fileName);
 			if (filePath==null) 
 				return;
 			
 			subMatrixPathField.setText(filePath);
 			
 		}		
	}
*/

/*	private boolean readMatrix (String contents) {
		String lines[] = contents.split("(\\r|\\n)+");
		
		if (lines.length < 3 )
			return false;
		
		//read in the alphabet
		lines[0] = lines[0].replaceAll("\\t","");
		char alphabet[] = new char[lines[0].length()];
		lines[0].getChars(0, lines[0].length(), alphabet, 0);

		if ( lines.length != alphabet.length +1 )
				return false;
		
		//translate letters to the correct bit in Mesquite's long representation
		int alphaconv[] = new int[alphabet.length];
		for (int i=0; i< alphabet.length; i++) {
			int id;
			if (alphabet.length == 4) 
				 id = DNAState.minimum(DNAState.fromCharStatic(alphabet[i]));
			else if (alphabet.length == 20) 
				id = ProteinState.minimum(ProteinState.fromCharStatic(alphabet[i])); //???
			else
				return false;
			
			if (id <0 || id > alphabet.length) 
				return false;
			
			alphaconv[i] = id;
		}
		
		
		//fill in the matrix
		subs =  new int[alphabet.length][alphabet.length];
		for (int i = 1; i < lines.length; i++) {
			String costs[] = lines[i].split("\\s+");
			int from = alphaconv[i-1];
			for (int j = 0; j < costs.length; j++) {
				int to = alphaconv[j];
				subs[from][to] = subs[to][from] = Integer.parseInt(costs[j]);
			}	
		}
		return true;
	}
*/
}
