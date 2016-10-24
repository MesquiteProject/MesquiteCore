/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.consensus.lib;

import java.util.Vector;

import mesquite.consensus.lib.Bipartition;
import mesquite.lib.*;

public class BipartitionVector extends Vector {
	boolean rooted = true;
	boolean useWeights = false;

	int numTaxa =  0;
	int numTreesTotal = 0;
	double weightedTreesTotal = 0.0;
	Bits[] nodes;
	double[] branchLengths;
	int princess = 0;
	Taxa taxa;
	Bits allTaxa;
	Bits partitionPresent;
	Bits bits1;
	Bits bits2;
	public static final int MAJRULEMODE = 0;
	public static final int STRICTMODE = 1;
	public static final int SEMISTRICTMODE = 2;
	public static final int MATCHMODE = 3;
	int mode = STRICTMODE;
	double weight = 1.0;

	int numBranchLengthArraySizeWarnings=0;
	int numNodeArraySizeWarnings=0;


	public void setTaxa(Taxa taxa){
		numTreesTotal = 0;
		this.taxa = taxa;
		numTaxa = taxa.getNumTaxa();
		nodes = new Bits[MesquiteTree.standardNumNodeSpaces(taxa)*2];  //some slack in case nonstandard tree
		partitionPresent = new Bits(MesquiteTree.standardNumNodeSpaces(taxa)*2);
		for (int i= 0; i<nodes.length; i++)
			nodes[i] = new Bits(numTaxa);
		allTaxa = new Bits(numTaxa);
		bits1 = new Bits(numTaxa);
		bits2 = new Bits(numTaxa);
		branchLengths = new double[MesquiteTree.standardNumNodeSpaces(taxa)];
		for (int i=0; i<branchLengths.length; i++){
			branchLengths[i] = MesquiteDouble.unassigned;
		}

	}
	public void zeroFrequencies(){
		numTreesTotal = 0;
		weight=1.0;
		weightedTreesTotal=0.0;
		if (allTaxa!=null)
			allTaxa.clearAllBits();
		if (partitionPresent!=null)
			partitionPresent.clearAllBits();
		for (int i=0; i<size(); i++){
			Bipartition b = getBipart(i);
			if (b!=null)
				b.reset();
		}
		for (int i=0; i<branchLengths.length; i++){
			branchLengths[i] = MesquiteDouble.unassigned;
		}

	}
	
	public void initialize() {
		numBranchLengthArraySizeWarnings=0;
		numNodeArraySizeWarnings=0;
	}
	
	public String toString(){
		String s = "BipartitionVector\n";
		for (int i=0; i<size(); i++){
			Bipartition b = getBipart(i);
			s += "   " + b + "\n";
		}
		return s;
	}
	public Bipartition getBipart(int i){
		if (i<size() && i>=0)
			return (Bipartition)elementAt(i);
		return null;
	}

	public double getDecimalFrequency (Bipartition bp) {
		if (useWeights)
			return bp.freqDouble*1.0/weightedTreesTotal;
		else
			return bp.getFreq()*1.0/numTreesTotal;
	}

	public void dump(){
		sort(true);
		MesquiteMessage.println("\nBipartition frequencies");
		MesquiteInteger.logHorizIntegerList(numTaxa);
		for (int i=0; i<size(); i++){
			Bipartition stored = getBipart(i);
			double freq = getDecimalFrequency(stored);
			if (freq>0.05) {
				String s = MesquiteDouble.toPrecisionSpecifiedString(freq,3);
				MesquiteMessage.println(stored.bits.toAsteriskString() + "   " +s);
			}
		}

	}
	public void tradeValues(int i, int j){
		long freq1 = ((Bipartition)elementAt(i)).getFreq();
		double freqDouble1 = ((Bipartition)elementAt(i)).getFreqDouble();
		Bits bits1 = ((Bipartition)elementAt(i)).getBits();
		long freq2 = ((Bipartition)elementAt(j)).getFreq();
		double freqDouble2 = ((Bipartition)elementAt(j)).getFreqDouble();
		Bits bits2 = ((Bipartition)elementAt(j)).getBits();
		((Bipartition)elementAt(i)).setFreq(freq2);
		((Bipartition)elementAt(j)).setFreq(freq1);
		((Bipartition)elementAt(i)).setFreqDouble(freqDouble2);
		((Bipartition)elementAt(j)).setFreqDouble(freqDouble1);
		((Bipartition)elementAt(i)).setBits(bits2);
		((Bipartition)elementAt(j)).setBits(bits1);
	}

	public void sort(boolean descending){
		for (int i=0; i<size(); i++) {
			for (int j= i+1; j<size(); j++) {
				if (useWeights) {
					double freq1 = ((Bipartition)elementAt(i)).getFreqDouble();
					double freq2 = ((Bipartition)elementAt(j)).getFreqDouble();
					if ((freq1<freq2 && descending) || (freq1>freq2 && !descending))
						tradeValues(i,j);
				} else {
					long freq1 = ((Bipartition)elementAt(i)).getFreq();
					long freq2 = ((Bipartition)elementAt(j)).getFreq();
					if ((freq1<freq2 && descending) || (freq1>freq2 && !descending))
						tradeValues(i,j);
				}
			}
		}
	}

	/** removes all bipartitions with frequencies lower that that specified */
	public void removeBipartitions(double lowerFrequencyLimit) {
		for (int i=size()-1; i>=0; i--) {
			double freq = getDecimalFrequency((Bipartition)elementAt(i));
			if (freq< lowerFrequencyLimit)
				remove(i);
		}

	}

	/** Returns a new BipartitionVector that contains only those Bipartitions that are in a frequency greater than or equal to the lowerFrequencyLimit */
	public BipartitionVector getFilteredBipartitions (double lowerFrequencyLimit) {
		BipartitionVector bpv = new BipartitionVector();
		bpv.setTaxa(taxa);
		for (int i=0; i<size(); i++){
			Bipartition bp = ((Bipartition)elementAt(i));
			double freq = getDecimalFrequency(bp);
			if (freq >= lowerFrequencyLimit) {
				Bipartition newbp = new Bipartition(numTaxa);
				newbp.copyIntoBits(bp.getBits());
				newbp.setFreq(bp.getFreq());
				newbp.setFreqDouble(bp.getFreqDouble());
				bpv.addElement(newbp);
			}
		}
		return bpv;
	}
	
	public double getDecimalFrequencyOfNode(Tree tree, int node, boolean rooted){
		Bits bits = tree.getTerminalTaxaAsBits(node);
		for (int i=0; i<size(); i++){
			Bipartition bp = ((Bipartition)elementAt(i));
			if (bp.equals(bits,rooted))
				return getDecimalFrequency(bp);
		}
		return 0.0;
	}
	
	/** Examines all contradictory clades and returnes the frequency value of the most frequent one. */
	public double getMaximumDecimalFrequencyOfContradictoryNode(Tree tree, int node, boolean rooted){
		Bits nodeBits = tree.getTerminalTaxaAsBits(node);
		Bits treeBits = tree.getTerminalTaxaAsBits(tree.getRoot());
		nodeBits.andBits(treeBits);  // this shouldn't be needed, but just in case...

		double max=0.0;
		for (int i=0; i<size(); i++){
			Bipartition bp = ((Bipartition)elementAt(i));
			Bits bpBits =bp.getBits();
			bpBits.andBits(treeBits);  //again, this shouldn't be needed, but just in case...
			if (!compatible(nodeBits, bpBits)) {
					double freq = getDecimalFrequency(bp);
					if (freq>max) max=freq;
			}
		}
		return max;
	}

	public boolean compatible(Bits a, Bits b){
		bits1.setBits(a);
		bits2.setBits(b);
		bits1.andBits(bits2);   // intersection of bits1 and bits2
		if (bits1.anyBitsOn()){   // intersection has something in it
			bits1.setBits(a);
			bits1.invertAllBits();
			bits1.andBits(bits2);  // intersection of bits1 complement and bits2
			if (bits1.anyBitsOn()){   
				bits1.setBits(a);
				bits2.invertAllBits();
				bits1.andBits(bits2);  // intersection of bits2 complement and bits1
				if (bits1.anyBitsOn()){   // intersection has something in it
					if (rooted)   // if rooted we don't need to check for the last pattern
						return false;
					bits1.setBits(a);
					bits1.invertAllBits();
					bits1.andBits(bits2);  // intersection of bits2 complement and bits1 complement
					if (bits1.anyBitsOn())  
						return false;   // all 4 pattern exist, must be incompatible
				}
			}
		}
		return true;

	}

	/** creates and adds to the vector a Bipartition from the Bits */
	private Bipartition simpleAddBipartition(Bits bits){
		Bipartition bipart = new Bipartition(numTaxa);
		bipart.copyIntoBits(bits);
		addElement(bipart);
		return bipart;
	}
	/** Goes through the tree and adds all a Bipartition to the vector for each split found */
	private void simpleGetPartitions(Tree tree, int node){
		if (tree.nodeIsTerminal(node)){
			nodes[node].setBit(tree.taxonNumberOfNode(node));
			double length = tree.getBranchLength(node);
			if (MesquiteDouble.isCombinable(length)) {
				if (!MesquiteDouble.isCombinable(branchLengths[tree.taxonNumberOfNode(node)]))
					branchLengths[tree.taxonNumberOfNode(node)] =0.0;
				branchLengths[tree.taxonNumberOfNode(node)] += length;
			}
			return;
		}
		nodes[node].clearAllBits();
		branchLengthArrayWarningForThisTree = false;
		nodeArraySizeWarningForThisTree=false;
		for (int daughter=tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter) ) {
			if (!getPartitions(tree, daughter))
				return;
			nodes[node].orBits(nodes[daughter]);
		}
		if (node != princess || rooted) {
			Bipartition bp = simpleAddBipartition(nodes[node]);
			double length = tree.getBranchLength(node);
			if (MesquiteDouble.isCombinable(length))
				bp.addToSplitLength(length);
		}
	}

	private Bipartition addBipart(Bits bits){
		if (bits == null)
			return null;
		switch (mode) {
		case STRICTMODE: 
			if (numTreesTotal==0) {  // first tree; just add it
				Bipartition bipart = new Bipartition(numTaxa);
				bipart.copyIntoBits(bits);
				addElement(bipart);
				return bipart;
			} else {
				boolean foundConflict = false;
				int identical = -1;
				for (int i=size()-1; i>=0; i--){  // going through stored ones and see whether it matches the new bits
					Bipartition stored = getBipart(i);
					if (stored != null){
						if (compatible(stored.getBits(),bits)){ //the incoming is compatible with the ith stored bipartition
						if (stored.equals(bits, rooted)){  // it's not only compatible, but they are the same
							identical=i;
							stored.setPresent(true);
						}
					} else { // 
						foundConflict=true;
						remove(i);
					}
					}
				}
				if (foundConflict && identical>0)
					remove(identical);
			}
			return null;

		case MAJRULEMODE:
		case SEMISTRICTMODE:
			for (int i=0; i<size(); i++){
				Bipartition stored = getBipart(i);

				if (stored != null && stored.equals(bits, rooted)){  //stored.potentialMatch(numBits, rooted)&&
					if (useWeights && mode==MAJRULEMODE)
						stored.weightedIncrement(weight);
					else
						stored.increment();
					return stored;
				}
			}
			Bipartition bipart = new Bipartition(numTaxa);
			bipart.copyIntoBits(bits);
			if (useWeights&& mode==MAJRULEMODE)
				bipart.weightedIncrement(weight);
			else
				bipart.increment();
			addElement(bipart);
			return bipart;
			
		case MATCHMODE:
			for (int i=0; i<size(); i++){
				Bipartition stored = getBipart(i);

				if (stored != null && stored.equals(bits, rooted)){  //stored.potentialMatch(numBits, rooted)&&
					if (useWeights)
						stored.weightedIncrement(weight);
					else
						stored.increment();
					return stored;
				}
			}
		default:
			return null;
		}
	}
	
	boolean branchLengthArrayWarningForThisTree = false;
	boolean nodeArraySizeWarningForThisTree = false;
	int biggestNode =0;
	
	/*.................................................................................................................*/
	private void printDiagnostics (Tree tree, int node) {
		MesquiteMessage.println("  Diagnostics:");
		MesquiteMessage.println("    tree name = "+ tree.getName());
		MesquiteMessage.println("    number of taxa in taxa block = "+ tree.getTaxa().getNumTaxa());
		MesquiteMessage.println("    number of terminal taxa in tree = "+tree.numberOfTerminalsInClade(tree.getRoot()));
		MesquiteMessage.println("    number of nodes in tree = "+tree.numberOfNodesInClade(tree.getRoot()));
		MesquiteMessage.println("    node = "+node);
		if (tree.nodeIsTerminal(node)){
			MesquiteMessage.println("    Node is terminal");
			MesquiteMessage.println("      taxon number = "+tree.taxonNumberOfNode(node));
		}
		else
			MesquiteMessage.println("    Node is internal");

	}

	/*.................................................................................................................*/
	private boolean getPartitions(Tree tree, int node){
		if (node >= nodes.length) {

			if (((!nodeArraySizeWarningForThisTree && numNodeArraySizeWarnings<10)||node>biggestNode)){
				MesquiteMessage.println("\nProblem with getPartitions: node number is larger than nodes array allocation");
				printDiagnostics(tree,node);
				MesquiteMessage.println("    nodes.length = "+ nodes.length);
				numNodeArraySizeWarnings++;
				nodeArraySizeWarningForThisTree=true;
				if (biggestNode<node)
					biggestNode=node;
			}
			return false;
		}
		if (tree.nodeIsTerminal(node)){     
			int it = tree.taxonNumberOfNode(node);
			if ((it< 0 || it >= branchLengths.length) && numBranchLengthArraySizeWarnings<10 && !branchLengthArrayWarningForThisTree){
				MesquiteMessage.println("\nProblem with getPartitions: branchlengths array too small.");
				printDiagnostics(tree,node);
				MesquiteMessage.println("    branchLengths.length = "+branchLengths.length);
				numBranchLengthArraySizeWarnings++;
				branchLengthArrayWarningForThisTree=true;
			}
			
			nodes[node].setBit(it);
			double length = tree.getBranchLength(node);
			if (MesquiteDouble.isCombinable(length)) {
				if (!MesquiteDouble.isCombinable(branchLengths[it]))
					branchLengths[it] =0.0;
				branchLengths[it] += length;
			}
			return true;
		}
		nodes[node].clearAllBits();
		for (int daughter=tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter) ) {
			if (!getPartitions(tree, daughter))
				return false;
			nodes[node].orBits(nodes[daughter]);
		}
		if (node != princess || rooted) {
			Bipartition bp = addBipart(nodes[node]);
			double length = tree.getBranchLength(node);
			if (bp!=null && MesquiteDouble.isCombinable(length))
				bp.addToSplitLength(length);

		}
		return true;
	}


	public void checkArraySizes(Tree tree){
		//upgrading storage if not big enough
		if (nodes.length < tree.getNumNodeSpaces()){
			partitionPresent.resetSize(tree.getNumNodeSpaces());
			Bits[] newNodes = new Bits[tree.getNumNodeSpaces()];
			for (int i=0; i<newNodes.length; i++)
				if (i<nodes.length)
					newNodes[i] =  nodes[i];
				else
					newNodes[i] = new Bits(numTaxa);
			nodes = newNodes;
		}
		if (branchLengths.length < tree.getNumNodeSpaces()){
			double[] newBranchLengths = new double[tree.getNumNodeSpaces()];
			for (int i=0; i<newBranchLengths.length; i++)
				if (i<branchLengths.length)
					newBranchLengths[i] =  branchLengths[i];
				else
					newBranchLengths[i] = MesquiteDouble.unassigned;
			branchLengths = newBranchLengths;
		}
	}

	
	/*.................................................................................................................*/
	
	/** adds tree to existing */
	public void addTree(Tree tree){
		branchLengthArrayWarningForThisTree=false;
		nodeArraySizeWarningForThisTree=false;
		princess = tree.firstDaughterOfNode(tree.getRoot());
		
		checkArraySizes(tree);
		if (tree.nodeIsPolytomous(tree.getRoot()))
			princess=-1;  // don't worry about princess if root is a polytomy
		for (int i= 0; i<nodes.length; i++)
			nodes[i].clearAllBits();
		if (numTreesTotal>0 && mode==STRICTMODE) 
			for (int i=size()-1; i>=0; i--){ 
				Bipartition stored = getBipart(i);
				if (stored!=null)
					stored.setPresent(false);
			}

		if (!getPartitions(tree, tree.getRoot()))
			return;

		if (numTreesTotal>0 && mode==STRICTMODE) 
			for (int i=size()-1; i>=0; i--){ 
				Bipartition stored = getBipart(i);
				if (stored!=null && !stored.isPresent())
					remove(i);
			}

		numTreesTotal++;
		if (useWeights)
			weightedTreesTotal += getWeight();
		allTaxa.orBits(nodes[tree.getRoot()]);
	}


	/** returns a new bipartitionVector for just for the tree passed. */
	public static BipartitionVector getBipartitionVector(Tree tree){
		if (tree==null)
			return null;
		BipartitionVector bpv = new BipartitionVector();
		bpv.setTaxa(tree.getTaxa());
		bpv.setRooted(tree.getRooted());
		int princess = tree.firstDaughterOfNode(tree.getRoot());
		if (tree.nodeIsPolytomous(tree.getRoot()))
			princess=-1;  // don't worry about princess if root is a polytomy
		bpv.setPrincess(princess);
		bpv.checkArraySizes(tree);
		bpv.simpleGetPartitions(tree, tree.getRoot());
		return bpv;
	}


	NameReference freqRef = NameReference.getNameReference("consensusFrequency");
	private void resolveByBipartition(MesquiteTree tree, Bipartition stored){
		int newNode = tree.makeClade(stored.bits);
		if (mode==MAJRULEMODE) {
			double prop = getDecimalFrequency(stored);
			tree.setAssociatedDouble(freqRef, newNode, prop, true);
			//	tree.setNodeLabel(MesquiteDouble.toStringDigitsSpecified(prop, 3), newNode);
			if (tree.nodeIsInternal(newNode)){
				double length = stored.getSplitLength();
				if (MesquiteDouble.isCombinable(length))
					tree.setBranchLength(newNode, length, false);
			}
		}
	}
	public Tree makeTree(double minFreq){
		MesquiteTree tree = new MesquiteTree(taxa);
		tree.setToDefaultBush(allTaxa, false);  //bug fixed 2. 75; had used the first taxa, not the set in the bipartition vector
		boolean setLengths = false;
		for (int it=0; it<numTaxa; it++)
			if (MesquiteDouble.isCombinable(branchLengths[it])) {
				if (useWeights)
					branchLengths[it]=branchLengths[it]/weightedTreesTotal;
				else
					branchLengths[it]=branchLengths[it]/numTreesTotal;
				setLengths = true;
			}

		if (mode==SEMISTRICTMODE){ // for this, need to go through and wipe out incompatible ones.
			for (int i=0; i<size(); i++){
				Bipartition stored = getBipart(i);
				stored.setPresent(true); 
				if (stored.getFreq()!=numTreesTotal) {
					for (int j=0; j<size(); j++){
						Bipartition comparison = getBipart(j);
						if (!compatible(comparison.getBits(), stored.getBits()))
							stored.setPresent(false);
					}

				}
			}
			for (int j=size()-1; j>=0; j--){
				if (!getBipart(j).isPresent())
					remove (j);
			}

		}

		for (int i=0; i<size(); i++){
			Bipartition stored = getBipart(i);
			stored.avgSplitLength(useWeights);
			//	if (!rooted)
			//		stored.bits.standardizeComplement(0);
			if (mode==STRICTMODE || mode==SEMISTRICTMODE) {
				resolveByBipartition(tree, stored);
			} else {
				double prop = getDecimalFrequency(stored);
				if (minFreq==0.5) {
					if (prop>minFreq)
						resolveByBipartition(tree, stored);
				}
				else	if (prop>=minFreq){
					resolveByBipartition(tree, stored);
				}
			}
		}
		if (setLengths) {
			for (int it=0; it<numTaxa; it++)
				if (MesquiteDouble.isCombinable(branchLengths[it])) {
					tree.setBranchLength(tree.nodeOfTaxonNumber(it), branchLengths[it], false);
				}
			tree.setBranchLength(tree.getRoot(), 0.0, false);
		}
		tree.standardize(tree.getRoot(), false);
		return tree;
	}
	public Tree makeTree(){
		return makeTree(0.5);
	}

	public int getMode() {
		return mode;
	}
	public void setMode(int mode) {
		this.mode = mode;
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	public boolean isUseWeights() {
		return useWeights;
	}
	public void setUseWeights(boolean useWeights) {
		this.useWeights = useWeights;
	}
	public boolean isRooted() {
		return rooted;
	}
	public void setRooted(boolean rooted) {
		this.rooted = rooted;
	}
	public int getPrincess() {
		return princess;
	}
	public void setPrincess(int princess) {
		this.princess = princess;
	}
}
