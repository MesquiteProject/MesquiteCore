package mesquite.cipres;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteTree;
import mesquite.lib.Taxa;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.characters.CharacterState;
import mesquite.categ.lib.DNAState;
import mesquite.categ.lib.ProteinState;

import org.cipres.CipresIDL.DataMatrix;
import org.cipres.CipresIDL.DiscreteDatatypes;
import org.cipres.CipresIDL.Tree;
import org.cipres.CipresIDL.TreeScore;
import org.cipres.datatypes.DataMatrixWrapper;
import org.cipres.datatypes.DNAMatrixWrapper;
import org.cipres.datatypes.ProteinMatrixWrapper;
import org.cipres.datatypes.PhyloDataset;
import org.cipres.util.Config;
 
public class MesquiteCipresTypeConverter {
	private static JFileChooser fileChooser;
	private static final boolean debugging = false;

	//@ todo:  The following static block is only needed for debugging.  Remove.
	static 
	{
		if (debugging)
		{
			fileChooser = new JFileChooser(new File(Config.getInstance().getDefaultNexusFileDir()));
			fileChooser.setDialogTitle("Choose nexus file for matrix verification.");
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		}
	}

	public static org.cipres.CipresIDL.Tree toCipresTree(MesquiteTree inTree) {
			// get the newick as 1-based string with edge lengths.
		String newick = inTree.writeTree(MesquiteTree.BY_NUMBERS, false, true, true, false, ",") + ";";
			// @ todo: remove, debug only!
		//System.out.println("toCipresTree newick is: " + newick);

			//mth is assuming that accumulateTerminals will fill the array with a zero-based 
			//	list of terminals.
			//cipres needs a 1 based list.
		int [] leafSet = new int[inTree.getNumTaxa()];
		MesquiteInteger posInArray = new MesquiteInteger(0);
		inTree.accumulateTerminals(inTree.getRoot(), leafSet, posInArray);
		int endInt = posInArray.getValue();
		for (int i = 0; i < endInt; ++i) 
			leafSet[i] += 1;

			//@ we should have a second optional arg for the score
		org.cipres.CipresIDL.TreeScore ts = new org.cipres.CipresIDL.TreeScore();
		ts.noScore(0.0);
		
		return new Tree(newick, ts, leafSet, inTree.getName());
	}
	
	public static MesquiteTree toMesquiteTree(org.cipres.CipresIDL.Tree inTree, Taxa taxa) {
		return new MesquiteTree(taxa, inTree.m_newick);
	}
	
	/**
		@todo: 
		1. Mesquite seems to treat 'N's in the character data the same as '?', that
		is, instead of being {ACGT} they're {-ACGT}.  You can see Ns changed to ? in
		Mesquite's character data display.  When we do the debugging test against
		what read_nexus_server returns, the test fails if the input file has Ns.

		Note however, that Mesquite converts a literal "{ACGT}" to N and doesn't 
		seem to handle a literal "{-ACGT}" at all.

		2. Mesquite seems to treat dna data the same as rna, setting Matrix.typeName
		to "DNA type", changing Us to Ts.  We don't pass the debugging comparison
		with read_nexus_server since it sets the matrix type to RNA for rna data. 

	*/
	private static byte[][] dnaToCipresBitEncoding(MCharactersDistribution matrix) {
			
		// low order nibble - should equal first four bits set = 2^4 - 1 = 0xF 
		long databitsMask = DNAState.fullSet();

		// will convert mesquite unassigned -> cipres bit order encoding of missing, 2^5 - 1
		byte cipresBitOrderMissing = 0x1F; 

		// tl - I don't think we need to do this because if inapplicable is true
		// no other bits will be set and cipres bit encoding also treats 0 as a gap. 
		//
		// will convert mesquite inapplicable -> cipres bit order encoding of gap 
		//byte cipresBitOrderGap = 0x10;

		int nTax = matrix.getNumTaxa();
		int nChars = matrix.getNumChars();
		byte [][] bitMatrix = new byte[nTax][nChars];
		DNAState state = new DNAState();
		byte cipresBitEncoding;	
		for (int t = 0; t < nTax; ++t) {
			for (int c = 0; c < nChars; ++c) {
				matrix.getCharacterState(state, c, t);
				if (state.isUnassigned())
				{
					cipresBitEncoding = cipresBitOrderMissing;	
				} else
				{
					cipresBitEncoding= (byte)(state.getValue() & databitsMask);
				}
				bitMatrix[t][c] =  cipresBitEncoding;
			}
		}
		return bitMatrix;
	}

	// @todo: there's a lot of commonality between this function and dnaToCipresBitEncoding
	// but the datatypes differ.  Could refactor.  
	private static int[][] proteinToCipresBitEncoding(MCharactersDistribution matrix) {
			
		// Mesquite uses the usual 21 symbols, "A" - "*", followed by four extra symbols
		// "1" - "4".  I'm not sure if the CipresIDL.DataMatrix should handle the extra
		// symbols so I'll flag an error if the incoming data has any of those bits set.
		// has bits 0 thru 24 set. 
		long mesquite_databitsMask = ProteinState.fullSet();
		long cipres_databitsMask = (1 << 21) - 1; 
		long extraSymbolsMask = mesquite_databitsMask ^ cipres_databitsMask;

		// will convert mesquite unassigned -> cipres bit order encoding of missing
		int cipresBitOrderMissing = (1 << 22) - 1; 

		// tl - I don't think we need to do this because if inapplicable is true
		// no other bits will be set and cipres bit encoding also treats 0 as a gap. 
		//
		// will convert mesquite inapplicable -> cipres bit order encoding of gap 
		//byte cipresBitOrderGap = 0x10;

		int nTax = matrix.getNumTaxa();
		int nChars = matrix.getNumChars();
		int [][] bitMatrix = new int[nTax][nChars];
		ProteinState state = new ProteinState();
		int cipresBitEncoding;	
		for (int t = 0; t < nTax; ++t) {
			for (int c = 0; c < nChars; ++c) {
				matrix.getCharacterState(state, c, t);
				if (state.isUnassigned())
				{
					cipresBitEncoding = cipresBitOrderMissing;	
				} else 
				{
					if ((state.getValue() & extraSymbolsMask) != 0)
					{
						throw new UnsupportedOperationException("Extra (numerical) protein states are not suppported" +
							" by this operation.");
					}
					cipresBitEncoding= (int)(state.getValue() & cipres_databitsMask);
				}
				bitMatrix[t][c] =  cipresBitEncoding;
			}
		}
		return bitMatrix;
	}
		
		
	public static org.cipres.CipresIDL.DataMatrix toCipresMatrix(MCharactersDistribution matrix) {
		org.cipres.CipresIDL.DataMatrix cipresMatrix;
		String typeName = matrix.getDataTypeName();
		if (typeName.equals("DNA Data")) //@@ There has got to be a better way
		{
			byte[][] bitMatrix = dnaToCipresBitEncoding(matrix);
			cipresMatrix =  DNAMatrixWrapper.dnaMatrixFromBitMatrix(bitMatrix);
		} else if (typeName.equals("RNA Data"))
		{
			byte[][] bitMatrix = dnaToCipresBitEncoding(matrix);
			cipresMatrix =  DNAMatrixWrapper.rnaMatrixFromBitMatrix(bitMatrix);
		} else if (typeName.equals("Protein Data"))
		{
			int[][] bitMatrix = proteinToCipresBitEncoding(matrix);
			cipresMatrix =  ProteinMatrixWrapper.proteinMatrixFromBitMatrix(bitMatrix);
		} else
		{
			throw new UnsupportedOperationException("Matrices of data type " + typeName + 
				" are not supported yet.");
		}
		/* 
			For debugging, compare the converted matrix to what we get 
			by using the phycas read_nexus_server to create the matrix from the
			nexus file.
		*/
		//@todo: remove this call when debugging is completed.
		if (debugging)
		{
			debugVerifyMatrix(cipresMatrix);
		}
		return cipresMatrix;
	}

	/*
		When finished debugging comment out this fn, call to it, and 
		decl and init of fileChooser above.
	*/
	//@todo: comment out this function when debugging is completed.
	private static void debugVerifyMatrix(org.cipres.CipresIDL.DataMatrix cipresMatrix)
	{
		if (fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
		{
			return;
		}
		String nexusFile = fileChooser.getSelectedFile().getAbsolutePath();
		PhyloDataset phyloDataset = new PhyloDataset();
		
		try
		{
			phyloDataset.initialize(new File(nexusFile));
			org.cipres.CipresIDL.DataMatrix readNexusMatrix = phyloDataset.getDataMatrix();
			if (DataMatrixWrapper.equals(readNexusMatrix, cipresMatrix))
			{
				JOptionPane.showMessageDialog(null, "Converted matrix is same as read_nexus_matrix");
			} else
			{
				JOptionPane.showMessageDialog(null, "Converted matrix doesn't match!  See cipres log for details.");
			}
			
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, "Exception trying to verify converted matrix: " + e.getMessage());
			e.printStackTrace();
		}
		
	}
}


