/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)

Modified:
25 Aug 01 (WPM): added getTerminalNode
 */
package mesquite.lib;

import java.awt.*;
import java.math.*;
import java.util.*;

import mesquite.lib.duties.ElementManager;
import mesquite.lib.duties.FileCoordinator;
import mesquite.lib.duties.NumberForTree;
import mesquite.lib.duties.TreesManager;

/* ======================================================================== */
/** The basic Tree class of Mesquite.  Nodes are represented by integers (Object representation of nodes is too
memory-costly). Node relationships use the first daughter/next sister system to accommodate polytomies easily.
Thus a recursive method can take the following form and be called initially by recurse(tree, tree.getRoot()); :
<pre><dl><dd>void recurse (Tree tree, int N) {
<dl><dd>for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
<dl><dd>recurse(tree, d);</dl></dl><dd>}</dl></pre>
Daughters, sisters and parents of a node can be found using public methods.<p>
Information can be stored at the nodes of the tree in the following ways.  The first two storage sites are directly embedded within the Tree.  Modules
cannot store their own private information directly in the Tree, but they can use the other storage methods.
<ul>
<li>Arrays embedded within the Tree object storing which nodes are mother, sister, daughter, etc.  
These arrays are always instantiated and are private; they are accessed via methods such as motherOfNode(node).
<li>Arrays storing branch lengths and any String label attached to the node.  
These arrays are instantiated as needed and are private, but their information can be set and gotten using methods provided.
<li>Because Tree extends Associable, arrays (LongArray, DoubleArray, ObjectArray or Bits) can be requested that will be attached to the tree and maintained (i.e. the array sizes 
will be automatically adjusted if the tree's size changes).   This is useful place to store information that is to follow the tree around, to be stuck\
to the tree like lichen. When the tree is written, this information will be written within the tree description String in special comments
associated with each node.  When such a tree description is read, the information will be read without assistance from modules.  Thus, the information
is of restricted forms, but is public and persistent.  Some things that currently use this associable storage are: whether the branch is selected, 
the color of the branch, the width of the lineage.  Branch selection is given special status because it is used in various calculations, and thus has 
streamlined methods to access the information.  The information is stored just as is the other Associable information.
<li>There is a special vector that allows objects to be associated with the tree, called the "sensitiveAttachments".  It is private, but objects can be attached, removed
or retrieved from it using the methods provided.  Such information is "sensitive" in the sense that it it to be detached as soon as the tree is 
altered.  This system is still immature; it was invented for information that was to be attached to the tree and used by other modules, but
the information would become untrustworthy as soon as the tree changed.  Since the module attaching it to the tree would find it difficult to follow the tree 
as it is subsequently used, it attaches the information with the understanding that the information is to be destroyed as soon as the tree changes 
(this is done in incrementVersion()).
<li>All of the above information is referenced directly or indirectly from the Tree object, and thus follows the tree object around.  Most modules will
find it useful to have private information that is associated with the tree.  They can do this by maintaining arrays that have as many elements
as numNodeSpaces in the tree, with the ith element of the array being associated with the ith node.  The size of the array can be checked, and readjusted 
if needed, each time a tree is passed.
</ul>
Support for special sorts of trees:
<ul>
<li>Reticulate trees:  Support  is incomplete. For each node, only one mother node is designated, but if additional parents are specified, they are added to the set
of parents of the node.  Thus, calculations needing non-reticulate trees can simply use the mother node and pretend
like the additional parents don't exist.  Calculations needing reticulate trees can ask for the parents.  (The set of parents
should be built to include the mother node.)  Tree reading of reticulate nodes is incomplete. 
<li>Observed taxa fixed as ancestors: not yet supported.
<li>Truly unrooted trees, with node-rings storage and recursion used by PHYLIP:  not yet supported.  There are some special methods whose names end in
UR that allow access to the tree as if unrooted.
</ul>*/
public class MesquiteTree extends Associable implements AdjustableTree, Listable, Renamable, Commandable, MesquiteListener, CompatibilityChecker, Identifiable {
	/** The set of taxa to which terminal nodes refer. */
	protected Taxa taxa;
	/** The tree vector to which this Tree belongs.  The tree does not need to belong to a TreeVector, but if it is, then it is stored here
	so that the TreeVector's translation table can be used when the Tree is written */
	private TreeVector treeVector = null;
	/** The name of the tree.*/
	private String name;
	/** the nodes that are the root and subRoot*/
	protected int root, subRoot;
	/** The number of taxa in the Taxa at last check. */
	private int oldNumTaxa;
	/** The size of various arrays within the tree object; that is, the number of spaces there are for individual nodes in the tree.  This is automatically increased as needed.*/
	private int numNodeSpaces;
	/** true if nodes other than root and subRoot are assigned*/
	private boolean exists;
	/** The array of first daughter nodes.  The ith element indicates what is the number of the first daughter of the ith node.*/
	private int[] firstDaughter;  
	/** The array of next sister nodes.  The ith element indicates what is the number of the next sister of the ith node.*/
	private int[] nextSister;  
	/** The array of mother nodes.  The ith element indicates what is the number of the mother of the ith node.*/
	private int[] mother;  
	/** The array of taxon numbers.  If the node is internal, this number should be negative.  Otherwise, the taxon number is the the number of the taxon within the Taxa*/
	private int[] taxonNumber;  
	/** The array of node numbers of taxa; the inverse of taxonNumber.  New to 2. 7, for efficiency with large trees.  If the taxon is not in the tree this number should be negative.*/
	private int[] nodeOfTaxon;  
	/** A temporary array of booleans, for use currently to reset taxonNumbers (see resetNodeOfTaxonNumbers).*/
	private boolean[] flags;  
	/** The branch lengths of the branches.  This array is instantiated only if needed.*/
	private double[] branchLength;
	/** The clade name labels at the nodes.   This array is instantiated only if needed.*/
	private String[] label;  
	/** The array of parents arrays (used for reticulate trees).  This array is always instantiated, but at each node the array of parents is instantiated only if needed.*/
	private int[][] parents;
	/** An array of id's of taxa; this is obtained from the Taxa initially and is updated when taxa are deleted, added or moved; it serves as a reference for what this Tree thinks
	are the current taxa id's.  If taxa are added, deleted or moved, it is used to compare to the current id array returned by the Taxa to decide how taxon numbers (which
	are not the same as taxa id's, and instead are linear 1... within the Taxa) in the tree should be updated.  At any other time, if there is a mismatch between the stored
	taxa id's and those returned by the Taxa, then a warning can be given.  In future it may be best to have the Taxa store an oldID's array that is reset after every notifyListeners so as
	not to burden every MesquiteTree (and CharacterData, and...) to store an array, but that would remove the error checking ability.*/
	private long[] taxaIDs;
	/** A vector to store objects that are to be cut off if the tree changes, though to be kept if it's cloned*/
	private Vector sensitiveAttachments; 
	/** A number recording the topology version number.  With changes of topology of the tree, the version number is incremented.  This way you know if you're dealing with the exact same tree or not.*/
	private long topologyVersion = 0;
	/** A number recording the branch lengths version number.  With changes of branch lengths or topology of the tree, the version number is incremented.  This way you know if you're dealing with the exact same tree or not.*/
	private long branchLengthsVersion = 0;
	/** a flag to tell the system the tree description being read is from a MrBayes contree file.*/
	protected boolean readingMrBayesConTree = false;
	/** A number recording the last taxa version with which synchronized.*/
	private long taxaVersion = 0;
	/** Locks name to provoke error messages if name change attempted.*/
	private boolean nameLock = false;
	/** Currently not used internally, though a query to isLocked currently does indicate true if the tree is at that moment undergoing modification and thus shouldn't be touched.*/
	private boolean locked = false;
	/** A vector to store the MesquiteListeners currently listening for changes in this tree.
	Vector listeners;
	/** A count of the listeners remaining, used to try to catch memory leaks.
	private static int listenersRemaining=0;
	 */
	/** True if internal node names are treated as cosmetic */
	public static boolean cosmeticInternalNames = true;
	/** True if internal node names should be converted to annotations */
	public static boolean convertInternalNames = false;
	/** True if should warn if reticulations found */
	//public static boolean warnReticulations = true;
	/** True if polytomies in tree treated as hard by default */
	public static boolean polytomyDefaultHard = true;
	/** True if tree reading permits truncated taxon names */
	public static boolean permitTruncTaxNames = true;
	/** True if tree reading permits taxon names to be expressed as t0, t1, etc.*/
	public static boolean permitT0Names = false;
	/** If true, then taxa block is enlarged when unfamiliar taxon name encountered */
	private boolean permitTaxaBlockEnlargement = false;
	/** 0 if polytomies in tree treated as hard, 1 if soft, 2 if not yet assigned */
	private int polytomiesHard = 2;
	/** A boolean that stores whether the tree is rooted or not */
	private boolean rooted = true;
	/** A counter of the total number of Trees created (for debugging/memory leaks)*/
	public static long totalCreated = 0;
	/** A counter of the total number of Trees disposed (for debugging/memory leaks)*/
	public static long totalDisposed = 0;
	/** A counter of the total number of Trees finalized (for debugging/memory leaks)*/
	public static long totalFinalized = 0;
	/** The id number of this Tree (each tree during a run receives its own number */
	private long id=0;

	/** If this tree is read in from a file, this is the number of this tree within the file */
	private int fileIndex = MesquiteInteger.unassigned;

	/** If modified since named, "*" is returned with name. */
	private boolean modifiedSinceNamed = false;
	/** The assigned number of the tree (used by Stored Trees to record the index of the original tree in a TreeVector from which this was cloned) */ 
	private int sequenceNumber = -1;

	/** If true then after branch moves, etc., tree integrity is checked.*/
	public static boolean checkIntegrity = false; //Todo: have settable preference

	private static MesquiteTimer[] timers;

	static {
		timers = new MesquiteTimer[10];
		for (int i= 0; i<10; i++)
			timers[i] = new MesquiteTimer();
	}
	/** The constructor, passed the Taxa on which the tree is based */
	public MesquiteTree (Taxa taxa) {
		super(standardNumNodeSpaces(taxa));
		numericalLabelInterpretationSet = numericalLabelInterpretationSetRUN;
		interpretNumericalLabelsAsOnBranches = interpretNumericalLabelsAsOnBranchesRUN;
		interpretLabelsAsNumerical = interpretLabelsAsNumericalRUN;
		defaultValueCode = defaultValueCodeRUN;
		totalCreated++;
		id = totalCreated;
		this.taxa = taxa;
		numNodeSpaces = standardNumNodeSpaces(taxa); //extra in case bad taxa OR INTERNAL UNBRANCHED NODES
		firstDaughter= new int[numNodeSpaces];  
		nextSister= new int[numNodeSpaces];  
		mother= new int[numNodeSpaces];  
		taxonNumber= new int[numNodeSpaces];  
		flags = new boolean[numNodeSpaces];
		parents = new int[numNodeSpaces][];
		if (taxa==null)
			MesquiteMessage.warnProgrammer(" Taxa in constructor for Tree is null ");
		else {
			nodeOfTaxon = new int[taxa.getNumTaxa()];
			oldNumTaxa = taxa.getNumTaxa();
			taxaVersion = taxa.getVersionNumber();
			taxaIDs = taxa.getTaxaIDs();
			//	taxa.addListener(this);//modified 19 Nov 01; trees no longer responsible for listening, to prevent memory leaks if not disposed; TreeVector in charge of notifying
		}
		intializeTree();
	}
	/** The constructor, passed the Taxa on which the tree is based.  This initializer is used only for cloning,
	as it prepares for an exact copy of the tree even if it's currently out of date*/
	public MesquiteTree (Taxa taxa, int numTaxa, int numNodeSpaces, long taxaVersion) {
		super(numNodeSpaces);
		numericalLabelInterpretationSet = numericalLabelInterpretationSetRUN;
		interpretNumericalLabelsAsOnBranches = interpretNumericalLabelsAsOnBranchesRUN;
		interpretLabelsAsNumerical = interpretLabelsAsNumericalRUN;
		defaultValueCode = defaultValueCodeRUN;
		totalCreated++;
		id = totalCreated;
		this.taxa = taxa;
		this.numNodeSpaces = numNodeSpaces; //extra in case bad taxa OR INTERNAL UNBRANCHED NODES
		firstDaughter= new int[numNodeSpaces];  
		nextSister= new int[numNodeSpaces];  
		mother= new int[numNodeSpaces];  
		taxonNumber= new int[numNodeSpaces];  
		nodeOfTaxon = new int[taxa.getNumTaxa()];
		flags = new boolean[numNodeSpaces];
		parents = new int[numNodeSpaces][];
		this.taxaVersion = taxaVersion;
		if (taxa==null)
			MesquiteMessage.warnProgrammer(" Taxa in constructor for Tree is null ");
		else {
			oldNumTaxa = numTaxa;
			taxaIDs = taxa.getTaxaIDs();
			//	taxa.addListener(this);//modified 19 Nov 01; trees no longer responsible for listening, to prevent memory leaks if not disposed; TreeVector in charge of notifying
		}
		intializeTree();
	}
	/** The constructor, passed the Taxa on which the tree is based, and a string description of the tree */
	public MesquiteTree (Taxa taxa, String description) {
		this(taxa);
		numericalLabelInterpretationSet = numericalLabelInterpretationSetRUN;
		interpretNumericalLabelsAsOnBranches = interpretNumericalLabelsAsOnBranchesRUN;
		readTree(description);
	}
	public String toHTMLStringDescription(){
		String sT = super.toHTMLStringDescription();
		if (!StringUtil.blank(sT))
			return "<li>Tree: " + getName() + "<ul>" + sT + "</ul></li>";
		return "<li>Tree: " + getName() + "</li>";
	}
	/*-----------------------------------------*/
	/** This sets arrays to default values, including zapping the labels and parents array elements.  Sensitive attachments also removed.
	It does not change Associateds nor does it reset numNodeSpaces */
	private void intializeTree() {
		exists = false;
		for (int i=0; i<numNodeSpaces; i++){	//not extras, not just 2t+3
			firstDaughter[i]=0;
			nextSister[i]=0;
			mother[i]=0;
			setTaxonNumber(i, -1);
			flags[i] = false;
		}
		firstDaughter[0]=-1;
		nextSister[0]=-1;
		mother[0]=-1;
		setTaxonNumber(0, -1);
		for (int i=0; i< nodeOfTaxon.length; i++)
			nodeOfTaxon[i] = -1;
		subRoot = 1;
		mother[subRoot] = -1;
		root = 2;
		mother[root] = subRoot;
		firstDaughter[subRoot]=root;
		if (branchLength!=null) 
			for (int i=0; i<numNodeSpaces; i++)
				branchLength[i] = MesquiteDouble.unassigned;
		if (label!=null) 
			for (int i=0; i<numNodeSpaces; i++)
				label[i] = null;
		for (int i=0; i<numNodeSpaces; i++)
			parents[i] = null;
		if (sensitiveAttachments!=null)
			sensitiveAttachments.removeAllElements();

		rooted = true;
		locked = false;
	}
	/*-----------------------------------------*/
	int lastOpenFound = -1;
	/** Returns an open (unused) node space in the arrays.  If none available, automatically expands storage. */
	private int openNode() {
		if (lastOpenFound>=-1 && lastOpenFound+1<numNodeSpaces){
			if (mother[lastOpenFound +1] == 0){
				lastOpenFound++;
				return lastOpenFound;
			}
		}
		for (int i=1; i<numNodeSpaces; i++)
			if (mother[i]==0){
				lastOpenFound = i;
				return i;
			}
		setNumNodeSpaces(numNodeSpaces+20); //one chance to expand
		for (int i=1; i<numNodeSpaces; i++)
			if (mother[i]==0){
				lastOpenFound = i;
				return i;
			}
		MesquiteMessage.warnProgrammer("no open nodes in Tree");//for some reason didn't work
		lastOpenFound = -1;
		return 0;
	}
	/*-----------------------------------------*/
	long cloneID = 0;
	/** Returns a clone of the tree.  Care is required:  as MesquiteTree on its own does not establish a listener
	to the Taxa to respond to changes in the Taxa.  It is not needed if the clone is to be placed in a TreeVector, nor if the tree will be
	remade by a Tree Source in response to taxa changes.  Otherwise, a listener should be established.*/
	public MesquiteTree cloneTree() {
		MesquiteTree T = null;
		boolean success = false;
		cloneID++;
		int gcCount = 0;
		while (!success){
			try {
				T = new MesquiteTree(taxa, oldNumTaxa, numNodeSpaces, taxaVersion);
				T.setToClone(this);
				success = true;
			}
			catch (OutOfMemoryError e){
				System.out.println("Out of memory error while cloning tree " + getID() + " clone ID " + cloneID + " gc " + gcCount + "; Mesquite will attempt to continue..  See file memory.txt in the Mesquite_Folder.");
				MesquiteTrunk.mesquiteTrunk.logln("Out of memory error while cloning tree " + getID() + " clone ID " + cloneID + " gc " + gcCount + "; Mesquite will attempt to continue..  See file memory.txt in the Mesquite_Folder.");
				try {
					Thread.sleep(50);
					Runtime rtm = Runtime.getRuntime();
					rtm.gc();
					gcCount++;
				}
				catch (InterruptedException ie){
					Thread.currentThread().interrupt();
				}
				catch (OutOfMemoryError ee){
				}
			}
		}
		return T;
	}
	/*-----------------------------------------*/
	/** Sets this tree to be a clone of that passed.*/
	public void setToClone(MesquiteTree tree) {
		if(!taxa.equals(tree.getTaxa(), false, true))
			this.taxa = tree.getTaxa();
		if (!tree.hasNodeLabels())
			label = null;
		if (!tree.hasBranchLengths())
			branchLength = null;
		//tree.checkTaxaIDs(null);
		oldNumTaxa = tree.oldNumTaxa;
		taxaVersion = tree.taxaVersion;
		sequenceNumber = tree.sequenceNumber;
		nodeOfTaxon = new int[taxa.getNumTaxa()];
		if (tree.getNumNodeSpaces()!=numNodeSpaces){
			numNodeSpaces = tree.getNumNodeSpaces(); //extra in case bad taxa OR INTERNAL UNBRANCHED NODES
			setNumberOfParts(numNodeSpaces);
			flags = new boolean[numNodeSpaces];
			firstDaughter= new int[numNodeSpaces];  
			nextSister= new int[numNodeSpaces];  
			mother= new int[numNodeSpaces];  
			taxonNumber= new int[numNodeSpaces];  
			parents = new int[numNodeSpaces][];
			if (tree.hasNodeLabels())
				label = new String[numNodeSpaces];
			if (tree.hasBranchLengths())
				branchLength = new double[numNodeSpaces];
		}
		else {
			if (tree.hasNodeLabels() && label == null)
				label = new String[numNodeSpaces];
			if (tree.hasBranchLengths() && branchLength == null)
				branchLength = new double[numNodeSpaces];
			flags = new boolean[numNodeSpaces];

		}

		intializeTree();

		for (int i=0; i<numNodeSpaces; i++){	
			if (branchLength!=null && tree.branchLength!=null)
				branchLength[i]=tree.branchLength[i];
			firstDaughter[i]=tree.firstDaughter[i];
			nextSister[i]=tree.nextSister[i];
			mother[i]=tree.mother[i];
			flags[i] = false;
			setTaxonNumber(i, tree.taxonNumber[i]);
			if (tree.parents[i]!=null) { //TODO: leave parents null until needed?
				parents[i] = new int[tree.parents[i].length];
				for (int j=0; j<tree.parents[i].length; j++)
					parents[i][j]=tree.parents[i][j];
			}
			if (label!=null && tree.label!=null)
				if (tree.label[i]!=null) 
					label[i] = new String(tree.label[i]);
		}
		for (int i=0; i<nodeOfTaxon.length; i++)
			nodeOfTaxon[i] = -1;
		for (int i=0; i<taxa.getNumTaxa() && i < tree.nodeOfTaxon.length; i++)
			nodeOfTaxon[i] = tree.nodeOfTaxon[i];
		setAssociateds(tree);
		setAttachments(tree);
		if (tree.sensitiveAttachments!=null) {
			if (sensitiveAttachments==null)
				sensitiveAttachments = new Vector();
			else
				sensitiveAttachments.removeAllElements();
			int s = tree.sensitiveAttachments.size();
			for (int i=0; i<s; i++)
				sensitiveAttachments.addElement(tree.sensitiveAttachments.elementAt(i));
		}
		else if (sensitiveAttachments!=null)
			sensitiveAttachments.removeAllElements();

		if (tree.anySelectedInClade(tree.getRoot())){
			NameReference sN = makeAssociatedBits("selected"); //this won't make new Bits if not needed, just return reference
			selected = getWhichAssociatedBits(sN);
		}
		else
			selected = null;
		checkAssociated();
		setAnnotation(tree.getAnnotation(), false); 
		root=tree.root;
		subRoot=tree.subRoot;
		//treeVector = tree.treeVector;
		numNodeSpaces = tree.numNodeSpaces;
		name = tree.name;
		modifiedSinceNamed = tree.modifiedSinceNamed;
		if (nameLock)
			MesquiteMessage.printStackTrace("name changed in locked tree");
		rooted = tree.rooted;
		polytomiesHard = tree.polytomiesHard;
		exists = true;
	}
	/*-----------------------------------------*/
	/** Sets this tree to be a clone of that passed.*/
	public void setToCloneFormOnly(Tree tree) {
		if(!taxa.equals(tree.getTaxa(), false, true))
			this.taxa = tree.getTaxa();
		if (!tree.hasNodeLabels())
			label = null;
		if (!tree.hasBranchLengths())
			branchLength = null;
		nodeOfTaxon = new int[taxa.getNumTaxa()];
		if (tree.getNumNodeSpaces()!=numNodeSpaces){
			numNodeSpaces = tree.getNumNodeSpaces(); //extra in case bad taxa OR INTERNAL UNBRANCHED NODES
			setNumberOfParts(numNodeSpaces);
			firstDaughter= new int[numNodeSpaces];  
			nextSister= new int[numNodeSpaces];  
			mother= new int[numNodeSpaces];  
			taxonNumber= new int[numNodeSpaces];  
			flags = new boolean[numNodeSpaces];
			parents = new int[numNodeSpaces][];
			if (tree.hasBranchLengths())
				branchLength = new double[numNodeSpaces];
			if (tree.hasNodeLabels())
				label = new String[numNodeSpaces];
		}
		else {
			if (tree.hasBranchLengths() && branchLength == null)
				branchLength = new double[numNodeSpaces];
		}


		intializeTree();

		for (int i=0; i<numNodeSpaces; i++){	
			if (branchLength!=null && tree.hasBranchLengths())
				branchLength[i]=tree.getBranchLength(i);
			flags[i] = false;
			firstDaughter[i]=tree.firstDaughterOfNode(i);
			nextSister[i]=tree.nextSisterOfNode(i);
			mother[i]=tree.motherOfNode(i);
			setTaxonNumber(i, tree.taxonNumberOfNode(i));
		}
		root=tree.getRoot();
		subRoot=tree.getSubRoot();
		//treeVector = tree.treeVector;
		numNodeSpaces = tree.getNumNodeSpaces();
		name = tree.getName();
		rooted = tree.getRooted();
		polytomiesHard = tree.getPolytomiesAssumption();
		exists = true;
	}
	public boolean upToDateWithTaxa(){
		if (getTaxa() == null)
			return false;
		if (getTaxa().getVersionNumber()==taxaVersion)
			return true;
		if (taxaIDs.length != taxa.getNumTaxa())
			return false;
		for (int i = 0; i<taxaIDs.length; i++)
			if (taxa.getTaxon(i).getID() != taxaIDs[i])
				return false;
		taxaVersion = getTaxa().getVersionNumber();
		return true;

	}
	public long getTaxaVersion(){
		return taxaVersion;
	}
	/** sets whether or not this is a MrBayes consensus tree.*/
	public void setReadingMrBayesConTree(boolean value) {
		readingMrBayesConTree = true;
	}

	/** sets whether or not this is a MrBayes consensus tree.*/
	public boolean getReadingMrBayesConTree() {
		return readingMrBayesConTree;
	}


	/*----------------------------------------*/
	private int recordMinTerms(Tree tree, int node, int[] minTerms){
		if (tree.nodeIsTerminal(node)) {
			minTerms[node]= tree.taxonNumberOfNode(node);
		}
		else{
			int min = 999999999;
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				int m = recordMinTerms(tree, d, minTerms);
				if (m<min)
					min = m;
			}
			minTerms[node] = min;
		}
		return minTerms[node] ;
	}
	private boolean coTraverse(Tree tree, int node, int[] minTerms, Tree tree2, int node2, int[] minTerms2, boolean checkBranchLengths){
		if (checkBranchLengths && (tree.getBranchLength(node) != tree2.getBranchLength(node2)))
			return false;
		if (tree.nodeIsTerminal(node)){
			if (!tree2.nodeIsTerminal(node2))
				return false;
			if (tree.taxonNumberOfNode(node) != tree2.taxonNumberOfNode(node2))
				return false;
			return true;
		}
		int numD = tree.numberOfDaughtersOfNode(node);
		int numD2 = tree2.numberOfDaughtersOfNode(node2);
		if (numD != numD2)
			return false;

		int currentMin = -1;
		for (int i=0; i<numD; i++){  //going through daughters by order of min terminal
			int dMin =0;
			int dMin2 = 0;
			int min = 9999999;
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				int m = minTerms[d];
				if (m<min && m > currentMin){
					min = m;
					dMin = d;
				}
			}
			int min2 = 9999999;
			for (int d = tree2.firstDaughterOfNode(node2); tree2.nodeExists(d); d = tree2.nextSisterOfNode(d)) {
				int m = minTerms2[d];
				if (m<min2 && m > currentMin){
					min2 = m;
					dMin2 = d;
				}
			}
			if (min != min2)
				return false;
			if (!coTraverse(tree, dMin, minTerms, tree2, dMin2, minTerms2, checkBranchLengths))
				return false;
			currentMin = min;
		}
		return true;
	}

	/** Returns true if trees have same topology. */
	public boolean equalsTopology(Tree tree, boolean checkBranchLengths){
		if (tree==null)
			return false;
		int[] minTermsThis = new int[getNumNodeSpaces()];
		int[] minTermsOther = new int[tree.getNumNodeSpaces()];
		recordMinTerms(this, getRoot(), minTermsThis);
		recordMinTerms(tree, tree.getRoot(), minTermsOther);
		return coTraverse(this, getRoot(), minTermsThis, tree, tree.getRoot(), minTermsOther, checkBranchLengths);
	}
	/*----------------------------------------*/
	/** Returns true if passed tree has same core array storage (same root, mothers, firstDaughter, nextSister, taxonNumber). */
	public boolean equalsCoreArrays(MesquiteTree tree){
		if (tree ==null || taxa != tree.taxa || numNodeSpaces!=tree.numNodeSpaces || root != tree.root)
			return false;
		if ((parents==null) != (tree.parents == null))
			return false;
		if (parents != null ){
			if (parents.length != tree.parents.length)
				return false;
			if (parents.length !=0 && parents[0]!=null && tree.parents[0] !=null) {
				if (parents[0].length!= tree.parents[0].length)
					return false;
			}
		}
		for (int i=0; i< numNodeSpaces; i++){
			if (firstDaughter[i] != tree.firstDaughter[i])
				return false;  
			if (nextSister[i] != tree.nextSister[i])
				return false;  
			if (mother[i] != tree.mother[i])
				return false;  
			if (taxonNumber[i] != tree.taxonNumber[i])
				return false;  
			if (parents != null && parents.length !=0  && parents[i] !=null && tree.parents[i] !=null) {
				for (int j=0; j<parents[i].length; j++)
					if (parents[i][j] != tree.parents[i][j])
						return false;
			}
		}
		return true;
	}
	/** Returns true if passed tree has same core array storage (same root, mothers, firstDaughter, nextSister, taxonNumber). */
	public boolean equalsCoreArraysPlusBranchLengths(MesquiteTree tree){
		if (!equalsCoreArrays(tree))
			return false;
		if (tree ==null || taxa != tree.taxa || numNodeSpaces!=tree.numNodeSpaces || root != tree.root)
			return false;
		if ((branchLength==null) != (tree.branchLength == null))
			return false;
		if (branchLength == null)
			return true;
		for (int i=0; i< numNodeSpaces; i++){
			if (branchLength[i] != tree.branchLength[i])
				return false;  
		}
		return true;
	}
	/*-----------------------------------------*/
	/** Returns the base numNodeSpaces used for a tree of numTaxa taxa; a convenient starting point for modules that need to allocate space
	for storage for nodes but which don't yet have a tree in hand. */
	public static int standardNumNodeSpaces(int numTaxa){
		return numTaxa*3+5;
	}
	/*-----------------------------------------*/
	/** Returns the base numNodeSpaces used for a tree for the passed taxa; a convenient starting point for modules that need to allocate space
	for storage for nodes but which don't yet have a tree in hand. */
	public static int standardNumNodeSpaces(Taxa taxa){
		if (taxa==null)
			return 0;
		else
			return taxa.getNumTaxa()*3+5;
	}
	/*-----------------------------------------*/
	/** Resets the number of node spaces to that passed.  arrays are expanded or shrunk as needed. */
	private void setNumNodeSpaces(int newNumNodeSpaces){
		if (newNumNodeSpaces==numNodeSpaces)
			return;
		int[] newFirstDaughter= new int[newNumNodeSpaces];  
		int[] newNextSister= new int[newNumNodeSpaces];  
		int[] newMother= new int[newNumNodeSpaces];  
		int[] newTaxonNumber= new int[newNumNodeSpaces];  
		int[][] newParents = new int[newNumNodeSpaces][];
		boolean[] newFlags = new boolean[newNumNodeSpaces];
		for (int i=0; i<numNodeSpaces && i<newNumNodeSpaces; i++) {
			newFlags[i] = flags[i];
			newFirstDaughter[i]=firstDaughter[i];
			newNextSister[i]=nextSister[i];
			newMother[i]=mother[i];
			newTaxonNumber[i] = taxonNumber[i]; //nodeOfTaxon should remain unchanged
			newParents[i] = parents[i]; 
		}
		for (int i=numNodeSpaces; i<newNumNodeSpaces; i++) {
			newFlags[i] = false;
			newFirstDaughter[i]=0;
			newNextSister[i]=0;
			newMother[i]=0;
			newTaxonNumber[i] = -1; //nodeOfTaxon should remain unchanged
			newParents[i] = null; 
		}
		firstDaughter = newFirstDaughter;
		nextSister = newNextSister;
		mother = newMother ;
		taxonNumber = newTaxonNumber;
		parents=newParents;
		flags = newFlags;
		if (branchLength!=null) {
			double[] newBranchLength = new double[newNumNodeSpaces];
			for (int i=0; i<numNodeSpaces && i<newNumNodeSpaces; i++)
				newBranchLength[i] = branchLength[i];
			for (int i=numNodeSpaces; i<newNumNodeSpaces; i++) 
				newBranchLength[i] = MesquiteDouble.unassigned;
			branchLength = newBranchLength;
		}
		if (label!=null) {
			String[] newLabel = new String[newNumNodeSpaces];
			for (int i=0; i<numNodeSpaces && i<newNumNodeSpaces; i++)
				newLabel[i] = label[i];
			label = newLabel;
		}


		numNodeSpaces = newNumNodeSpaces;
		resetNodeOfTaxonNumbers();
		setNumberOfParts(numNodeSpaces);
	}
	/*-----------------------------------------*/
	/**Returns the unique id number of this tree*/
	public long getID(){
		return id;
	}
	/*-----------------------------------------*/
	/** Returns whether the tree has a name.*/
	public boolean hasName() {
		return (name!=null);
	}
	/*-----------------------------------------*/
	/** Returns the name of the tree, or "Untitled" if no name has been set.*/
	public String getName() {
		if (name==null)
			return "Untitled Tree";
		if (modifiedSinceNamed)
			return name + '+';
		return name;
	}
	/*-----------------------------------------*/
	/** Sets the name of the tree.*/
	public void setName(String name) {
		if (nameLock)
			MesquiteMessage.printStackTrace("Error: locked name in tree changed");
		else {
			this.name = name;
			modifiedSinceNamed = false;
		}
	}
	/*-----------------------------------------*/
	/** Sets whether name is locked*/
	public void setNameLock(boolean lock) {
		nameLock = lock;
	}
	/*-----------------------------------------*/
	/** Returns topology version number of tree.  Version number should be incremented with each change in topology.*/
	public long getTopologyVersion(){
		return topologyVersion;
	}
	/*-----------------------------------------*/
	/** Returns branch lengths version number of tree.  Version number should be incremented with each change in branch lengths.*/
	public long getBranchLengthsVersion(){
		return branchLengthsVersion;
	}
	/*-----------------------------------------*/
	/** Used to keep track of version numbers of tree.  Each time tree is changed, call this.  Also removes sensitive attachments. */
	private void incrementVersion(int code, boolean notify){
		versionNumber++;
		if (code == BRANCHES_REARRANGED) {
			topologyVersion++;
			branchLengthsVersion++;
		}
		if (code == BRANCHLENGTHS_CHANGED)
			branchLengthsVersion++;
		if (sensitiveAttachments!=null)
			sensitiveAttachments.removeAllElements();
		modifiedSinceNamed = true; 
		setDirty(true);
		if (nameLock)
			MesquiteMessage.printStackTrace("Error: locked name in tree changed 2");
		if (notify)
			notifyListeners(this, new Notification (code));

	}
	/*-----------------------------------------*/
	/* Returns whether tree is the same id, topology, branch lengths and or full version */
	public boolean sameTreeVersions(TreeReference tr, boolean checkBranchLengths, boolean exact){
		if (tr==null)
			return false;
		if (tr.getID() != getID())
			return false;
		if (tr.getTopologyVersion() != getTopologyVersion())
			return false;
		if (checkBranchLengths && (tr.getBranchLengthsVersion() != getBranchLengthsVersion()))
			return false;
		if (exact && (tr.getVersionNumber() != getVersionNumber()))
			return false;
		return true;
	}
	/*-----------------------------------------*/
	/* Returns whether tree is the same id, topology, branch lengths and or full version */
	public boolean sameTreeVersions(MesquiteTree tr, boolean checkBranchLengths, boolean exact){
		if (tr==null)
			return false;
		if (tr.getID() != getID())
			return false;
		if (tr.getTopologyVersion() != getTopologyVersion())
			return false;
		if (checkBranchLengths && (tr.getBranchLengthsVersion() != getBranchLengthsVersion()))
			return false;
		if (exact && (tr.getVersionNumber() != getVersionNumber()))
			return false;
		return true;
	}
	/*-----------------------------------------*/
	/* Returns whether tree is the same id, topology, branch lengths and or full version */
	public TreeReference getTreeReference(TreeReference tr){
		if (tr==null)
			tr = new TreeReference();
		tr.setID(getID());
		tr.setTopologyVersion(getTopologyVersion());
		tr.setBranchLengthsVersion(getBranchLengthsVersion());
		tr.setVersionNumber(getVersionNumber());
		return tr;
	}
	/*-----------------------------------------*
 	public boolean equals(Object obj){
 		if (!(obj instanceof MesquiteTree))
 			return false;
 		MesquiteTree tree = (MesquiteTree)obj;
 		return equalsCoreArraysPlusBranchLengths(tree);
 	}
	/*-----------------------------------------*/
	/** Returns the Taxa to which the tree refers.*/
	public Taxa getTaxa() {
		return taxa;
	}
	/*-----------------------------------------*/
	/** returns true if object is set of taxa on which tree is based.  For CompatibilityChecker interface*/
	public boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer, MesquiteString report){
		if (obj instanceof Taxa) {
			if (obj != taxa && report != null)
				report.setValue("tree applies to a different block of taxa.");
			return (obj == taxa);
		}
		return true;
	}
	/** Returns whether module is compatible with given object*/
	public boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer) {
		return isCompatible(obj, project, prospectiveEmployer, null);
	}
	/*-----------------------------------------*/
	boolean inProgressAddingTaxa = false;
	public boolean isEmpty(){
		if (inProgressAddingTaxa)
			return false;
		if (!isDefined())
			return true;
		if (!inBounds(getRoot()))
			return true;
		if (nodeIsTerminal(getRoot()))
			return true;
		if (numberOfTerminalsInClade(getRoot())<1)
			return true;
		if (numberOfDaughtersOfNode(getRoot())<1)
			return true;
		return false;
	}
	/*-----------------------------------------*/
	/** Returns number of terminal taxa in clade.*/
	public  int numberOfNegativeTerminalsInClade(int node) {
		if (!inBounds(node))
			return 0;
		if (nodeIsTerminal(node) && taxonNumberOfNode(node)<0)
			return 1; //count node itself
		else{
			int count = 0;
			for (int d = firstDaughterOfNode(node); nodeExists(d); d = nextSisterOfNode(d))
				count += numberOfNegativeTerminalsInClade(d);
			return count;
		}
	}
	/*-----------------------------------------*/
	public boolean isValid(){
		return !isEmpty() && (numberOfNegativeTerminalsInClade(getRoot())<=0);
	}
	/*-----------------------------------------*/
	public boolean isDefined(){
		return exists;
	}
	/*-----------------------------------------*/
	public void setAsDefined(boolean defined){
		exists = defined;
	}
	/*-----------------------------------------*/
	/** Returns false if tree is null, has no nodes, or is locked.*/
	public static boolean OK(Tree tree) {
		if (tree==null)
			return false;
		else if ((tree.isDefined()) && (!tree.isLocked())) {
			return true;
		}
		else 
			return false;
	}
	/*-----------------------------------------*/
	/** Sets the  tree vector to which the tree belongs.*/
	public void setTreeVector(TreeVector treeVector) {
		this.treeVector = treeVector;
	}
	/*-----------------------------------------*/
	/** Gets the  tree vector to which the tree belongs.*/
	public TreeVector getTreeVector() {
		return treeVector;
	}
	/*-----------------------------------------*/
	/** Disconnects listeners */
	public void dispose(){
		/*if (listeners!=null) {
			Enumeration e = listeners.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				MesquiteListener listener = (MesquiteListener)obj;
	 				listener.disposing(this);

				}
			listeners.removeAllElements();
			listenersRemaining=0;
			listeners = null;
		}
		 */
		if (taxa!=null)
			taxa.removeListener(this);
		removeAllListeners();
		totalDisposed++;

	}

	public void finalize() throws Throwable {
		//	if (taxa!=null)
		//		taxa.removeListener(this);
		totalFinalized++;
		super.finalize();
	}
	/*-----------------------------------------*/
	/** For Commandable interface; currently responds to set branch length messages and tree description writing */
	public Object doCommand(String commandName, String arguments, CommandChecker checker){
		if (checker.compare(this.getClass(), "Sets all branch lengths of the tree", "[length]", commandName, "setAllBranchLengths")) {
			double bL = MesquiteDouble.fromString(arguments);
			if (MesquiteDouble.isCombinable(bL)|| MesquiteDouble.isUnassigned(bL)){
				setAllBranchLengths(bL, true);
				return MesquiteBoolean.TRUE;
			}
		}
		else if (checker.compare(this.getClass(), "Sets branch lengths of all branches with unassigned lengths of the tree", "[length]", commandName, "setAllUnassignedBranchLengths")) {
			double bL = MesquiteDouble.fromString(arguments);
			if (MesquiteDouble.isCombinable(bL)|| MesquiteDouble.isUnassigned(bL)) {
				for (int i=0; i<numNodeSpaces; i++)
					if (nodeExists(i)) {
						double d = getBranchLength(i);
						if (MesquiteDouble.isUnassigned(d))
							setBranchLength(i, bL, false);
					}
				incrementVersion(BRANCHLENGTHS_CHANGED, true);
				return MesquiteBoolean.TRUE;
			}
		}
		else if (checker.compare(this.getClass(), "Multiplies all branch lengths of the tree by a factor", "[factor]", commandName, "scaleAllBranchLengths")) {
			double bL = MesquiteDouble.fromString(arguments);
			if (MesquiteDouble.isCombinable(bL)|| MesquiteDouble.isUnassigned(bL)) {
				for (int i=0; i<numNodeSpaces; i++)
					if (nodeExists(i)) {
						double d = getBranchLength(i);
						if (MesquiteDouble.isCombinable(d))
							setBranchLength(i, d*bL, false);
						//else if (MesquiteDouble.isUnassigned(d))
						//	setBranchLength(i, bL, false);   // as it was unassigned, presume 1 before scale
					}
				incrementVersion(BRANCHLENGTHS_CHANGED, true);
				return MesquiteBoolean.TRUE;
			}
		}
		else if (checker.compare(this.getClass(), "Multiplies lengths of all selected branches by a factor", "[factor]", commandName, "scaleLengthSelectedBranches")) {
			double bL = MesquiteDouble.fromString(arguments);
			if (MesquiteDouble.isCombinable(bL)|| MesquiteDouble.isUnassigned(bL)) {
				for (int i=0; i<numNodeSpaces; i++)
					if (nodeExists(i) && getSelected(i)) {
						double d = getBranchLength(i);
						if (MesquiteDouble.isCombinable(d))
							setBranchLength(i, d*bL, false);
					}
				incrementVersion(BRANCHLENGTHS_CHANGED, true);
				return MesquiteBoolean.TRUE;
			}
		}
		else if (checker.compare(this.getClass(), "Sets lengths of all selected branches", "[length]", commandName, "setLengthSelectedBranches")) {
			double bL = MesquiteDouble.fromString(arguments);
			if (MesquiteDouble.isCombinable(bL) || MesquiteDouble.isUnassigned(bL)) {
				for (int i=0; i<numNodeSpaces; i++)
					if (nodeExists(i) && getSelected(i))
						setBranchLength(i, bL, false);
				incrementVersion(BRANCHLENGTHS_CHANGED, true);
				return MesquiteBoolean.TRUE;
			}
		}
		else if (checker.compare(this.getClass(), "Set length of indicated branch", "[length][node number]", commandName, "setBranchLength")) {
			pos.setValue(0);
			double L = MesquiteDouble.fromString(arguments, pos);
			int node = MesquiteInteger.fromString(arguments, pos);
			if (MesquiteDouble.isCombinable(L) && MesquiteInteger.isCombinable(node) && nodeExists(node)) {
				setBranchLength(node, L, false);
				return MesquiteBoolean.TRUE;
			}
		}
		else if (checker.compare(this.getClass(), "Set label of indicated branch", "[label][node number]", commandName, "setBranchLabel")) {
			String name = ParseUtil.getFirstToken(arguments, pos);
			int node = MesquiteInteger.fromString(arguments, pos);
			if (MesquiteInteger.isCombinable(node) && nodeExists(node)) {
				setNodeLabel(name, node);
				return MesquiteBoolean.TRUE;
			}
		}
		else if (checker.compare(this.getClass(), "Gets node number that corresponds to a taxon", "[taxon]", commandName, "getNodeOfTaxonNumber")) {
			pos.setValue(0);
			int tax1 = MesquiteInteger.fromString(arguments, pos);

			if (MesquiteInteger.isCombinable(tax1)) {
				int node1 = nodeOfTaxonNumber(tax1);
				return new MesquiteInteger(node1);
			}
		}
		else if (checker.compare(this.getClass(), "Gets node number that is MRCA of two taxa", "[taxon1][taxon2]", commandName, "getMRCAofTaxa")) {
			pos.setValue(0);
			int tax1 = MesquiteInteger.fromString(arguments, pos);
			int tax2 = MesquiteInteger.fromString(arguments, pos);

			if (MesquiteInteger.isCombinable(tax1) && MesquiteInteger.isCombinable(tax2)) {
				int node1 = nodeOfTaxonNumber(tax1);
				int node2 = nodeOfTaxonNumber(tax2);

				return new MesquiteInteger(mrca(node1, node2));
			}
		}
		else if (checker.compare(this.getClass(), "Sets all branch lengths of the tree to unassigned", null, commandName, "deassignAllBranchLengths")) {
			for (int i=0; i<numNodeSpaces; i++)
				if (nodeExists(i))
					setBranchLength(i, MesquiteDouble.unassigned, false);
			incrementVersion(BRANCHLENGTHS_CHANGED, true);
		}
		else if (checker.compare(this.getClass(), "Sets lengths of selected branches to unassigned", null, commandName, "deassignLengthSelectedBranches")) {
			for (int i=0; i<numNodeSpaces; i++)
				if (nodeExists(i) && getSelected(i))
					setBranchLength(i, MesquiteDouble.unassigned, false);
			incrementVersion(BRANCHLENGTHS_CHANGED, true);
		}
		else if (checker.compare(this.getClass(), "Writes the tree as a string to the log window", null, commandName, "writeTree")) {
			MesquiteTrunk.mesquiteTrunk.logln("Tree \"" + getName() + "\"    " + writeTree(BY_NAMES));
		}
		else if (checker.compare(this.getClass(), "Returns a string description of the tree (with taxa indicated by their numbers)", null, commandName, "getDescriptionByNumbers")) {
			return writeTree(BY_NUMBERS);
		}
		else if (checker.compare(this.getClass(), "Returns a string description of the tree (with taxa indicated by their names)", null, commandName, "getDescriptionByNames")) {
			return writeTree(BY_NAMES);
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*-----------------------------------------*/
	/*.......................................basic node utilities.............................................*/
	/*-----------------------------------------*/
	/** Returns the root of the tree (i.e., the most recent common ancestor of the terminal taxa in the tree.*/
	public int getRoot() {
		return root;
	}
	/*-----------------------------------------*/
	/** Returns the subRoot of the tree (the mother of the most recent common ancestral node of the tree).  
	The subroot exists so that some traversals (including for drawing the root branch)
	don't choke when they come to the root and look for an ancestor.*/
	public int getSubRoot() {
		return subRoot;
	}
	/*-----------------------------------------*/
	/** Sets whether tree is rooted.*/
	public void setRooted(boolean rooted, boolean notify) {
		if (rooted != this.rooted) {
			this.rooted = rooted;
			incrementVersion(MesquiteListener.BRANCHES_REARRANGED, notify);
		}
	}
	/*-----------------------------------------*/
	/** Returns whether tree is rooted.*/
	public boolean getRooted() {
		return rooted;
	}
	/*-----------------------------------------*/
	/** Returns whether root is real.  It isn't real if the tree is unrooted.*/
	public boolean rootIsReal() {
		return rooted;
	}
	/*-----------------------------------------*/
	/** Returns the number of terminal taxa in the Taxa to which the tree refers.  This is not necessarily the 
	number of terminal taxa in the tree itself (for that, call numberOfTerminalsInClade(root)).*/
	public int getNumTaxa() {
		return taxa.getNumTaxa();
	}
	/*-----------------------------------------*/
	/** Returns the number of node spaces available in storage arrays within tree object.*/
	public int getNumNodeSpaces() {
		return numNodeSpaces;
	}
	/*-----------------------------------------*/
	/** Returns true if N is a valid node designation in tree.*/
	public  boolean nodeExists(int node) {
		return inBounds(node); 
	}
	//TODO: nodeExists should be renamed nodeInRange and most occurences in program should use it; should be separate nodeInTree (see below)
	/** Overriding method of Associable.*/
	protected boolean inBounds(int node) {
		return ((node>0)&&(node<numNodeSpaces)); 
	}
	/*-----------------------------------------*/
	/** Returns true if node is an internal node.*/
	public  boolean nodeIsInternal(int node) {
		if (!inBounds(node))
			return false;
		return (firstDaughter[node]!=0); }
	/*-----------------------------------------*/
	/** Returns true if node is a terminal node.*/
	public  boolean nodeIsTerminal(int node) {
		if (!inBounds(node))
			return false;
		return (firstDaughter[node]==0); }
	/*-----------------------------------------*/
	/** Returns the taxon number of the node.  This is -1 if the node is internal,
	the taxon number if terminal.*/
	public  int taxonNumberOfNode(int node) {  //todo: should only return if actually in tree!!!!!!
		if (!inBounds(node))
			return -1;
		return taxonNumber[node]; }
	/*-----------------------------------------*/
	private void flagNodesInTree(int node){
		flags[node] = true;
		for (int d = firstDaughterOfNode(node); nodeExists(d) && !nodeWasFound; d = nextSisterOfNode(d))
			flagNodesInTree(d);
	}
	/*-----------------------------------------*/
	private void zeroUnflaggedNodes(int node){
		if (!flags[node]){
			firstDaughter[node]=0;
			nextSister[node]=0;
			mother[node]=0;
			taxonNumber[node] = -1; 
			parents[node] = null; 
		}
		for (int d = firstDaughterOfNode(node); nodeExists(d) && !nodeWasFound; d = nextSisterOfNode(d))
			zeroUnflaggedNodes(d);
	}
	/*-----------------------------------------*/
	/** Refills the nodeOfTaxon array. Also cleans up node spaces that used to have nodes in them.*/
	private void resetNodeOfTaxonNumbers() {
		if (nodeOfTaxon == null || taxa.getNumTaxa() != nodeOfTaxon.length){
			nodeOfTaxon = new int[taxa.getNumTaxa()];
		}
		for (int i=1; i<nodeOfTaxon.length; i++)// Nov 2013 initialize
			nodeOfTaxon[i] = -1;

		for (int i=1; i<flags.length; i++)// Sept 2014
			flags[i] = false;
		flagNodesInTree(getRoot());
		zeroUnflaggedNodes(getRoot());
		for (int i=1; i<flags.length; i++)// Sept 2014
			flags[i] = false;

		for (int i=1; i<numNodeSpaces; i++)
			if (taxonNumber[i]>=0 && taxonNumber[i]<taxa.getNumTaxa() ){  // Nov 2013 check if in tree
				nodeOfTaxon[taxonNumber[i]] = i;
			}
	}
	/*-----------------------------------------*/
	/** Returns the terminal node corresponding to the given taxon number; 0 if the taxon  is not part of the tree.*/
	public int nodeOfTaxonNumber(int taxonNum) {
		if (taxonNum<0)
			return 0;
		if (taxonNum < nodeOfTaxon.length){
			int n = nodeOfTaxon[taxonNum];
			if (n >=0 && n< taxonNumber.length && taxonNumber[n] == taxonNum)
				return n;
		}

		for (int i=1; i<numNodeSpaces; i++)
			if (taxonNumber[i]==taxonNum && nodeInTree(i)){  //todo: should only return if actually in tree!!!!!!
				/*if (taxonNum> nodeOfTaxon.length)
					---("    Temporary debugging code in MesquiteTree (nodeOfTaxonNumber) " + taxonNum + " i  " + i + " taxonNum too large for nodeOfTaxon array; length: " + nodeOfTaxon.length); 

				else
					---("    Temporary debugging code in MesquiteTree (nodeOfTaxonNumber) " + taxonNum + " i  " + i + " nodeOfTaxon[taxonNum] " + nodeOfTaxon[taxonNum]); 
				 */
				return i;
			}
		return 0;
	}
	/*-----------------------------------------*/
	/** Sets the taxon number for the node.  If the node is internal, the number should
	be set to -1.  Otherwise it should be set to the taxon corresponding to the node.
	NOTE: this should be used with caution, since it doesn't check for tree integrity.  Intended primarily
	for internal use (with MesquiteTree).*/
	public void setTaxonNumber(int node, int num, boolean notify) { 
		if (!inBounds(node))
			return;
		setTaxonNumber(node, num); 
		incrementVersion(MesquiteListener.BRANCHES_REARRANGED, notify);
	}
	/*-----------------------------------------*/
	/** Returns the immediate ancestor of node (mother) if node is non-reticulate.
	If node is reticulate, returns the first (primary) parent.*/
	public  int motherOfNode(int node) {
		if (!inBounds(node))
			return 0;
		return mother[node]; }
	/*-----------------------------------------*/
	/** Returns the node's mother's mother.*/
	public  int grandmotherOfNode(int node) {
		return motherOfNode(motherOfNode(node)); 
	}
	/*-----------------------------------------*/
	/** Returns the number of parents of node.*/
	public int numberOfParentsOfNode(int node) {
		if (!inBounds(node))
			return 0;
		if (parents[node] == null)
			return 1;
		else
			return parents[node].length;
	}
	/*-----------------------------------------*/
	/** Returns the indexTH parent of node.*/
	public int parentOfNode(int node, int index) {
		if (!inBounds(node))
			return 0;
		if (parents[node] == null && index ==1) {
			return motherOfNode(node);
		}
		else if (index <= parents[node].length) {
			return parents[node][index - 1];
		}	
		return 0;
	}
	/*-----------------------------------------*/
	/** Returns the array of parents of node.  If node is not reticulate, the array returned contains
	only the mother node.*/
	public int[] parentsOfNode(int node) {
		if (!inBounds(node))
			return null;
		if (parents[node] == null) {
			int[] anc = new int[1];
			anc[0] = motherOfNode(node);
			return anc;
		}
		else {
			int[] anc = new int[parents[node].length];
			for (int i=0; i<parents[node].length; i++)
				anc[i] = parents[node][i];
			return anc;
		}	
	}
	/*-----------------------------------------*/
	/** An indelicate quick way to delete all reticulations from a node. Added 4 Feb 02*/
	public void snipReticulations(int node){
		if (!inBounds(node))
			return;
		parents[node] = null;
	}
	/*-----------------------------------------*/
	/** Sets par to be an additional parent of node.  Used to make reticulations.  Currently there
	is no way to subtract parents individually, nor to rearrange trees that contain reticulations.*/
	public void setParentOfNode(int node, int par, boolean notify) {
		if (!inBounds(node))
			return;
		int numParents;
		if (parents[node] == null)
			numParents=1;
		else
			numParents = parents[node].length+1;
		int[] newParents = new int[numParents];
		for (int i=0; i<numParents; i++) {
			if (parents[node] == null || i>= parents[node].length)
				newParents[i] = 0;
			else
				newParents[i]=parents[node][i];
		}
		newParents[numParents-1] = par;
		parents[node] = newParents;
		incrementVersion(MesquiteListener.BRANCHES_REARRANGED, notify);
	}
	/*-----------------------------------------*/
	/** Returns true iff first branch is a descendant of second branch.*/
	public boolean descendantOf(int branchD, int branchA) { 
		if (branchD == branchA)
			return false;
		else if (!nodeExists(branchD)|!nodeExists(branchA))
			return false;
		else {
			int b = branchD;
			while (b!=subRoot && nodeExists(b)) {
				b = motherOfNode(b);
				if (b==branchA)
					return true;
			}
			return false;
		}
	}
	/*-----------------------------------------*/
	/** Returns depth of second branch below first; -1 if not ancestral.*/
	public int depthToAncestor(int branchD, int branchA) { 
		if (branchD == branchA)
			return 0;
		else if (!nodeExists(branchD)|!nodeExists(branchA))
			return -1;
		else {
			int b = branchD;
			int depth = 0;
			while (b!=subRoot && nodeExists(b)) {
				depth++;
				b = motherOfNode(b);
				if (b==branchA)
					return depth;
			}
			return -1;
		}
	}

	/*-----------------------------------------*/
	/** Returns the deepest path in a clade, in terms of numbers of nodes*/
	public int deepestPath( int node) { 
		int deepest = 0;
		for (int it = 0; it< getTaxa().getNumTaxa(); it++){
			if (taxonInTree(it)){  // taxon in tree (ideally do taxon in clade)
				int current = depthToAncestor(nodeOfTaxonNumber(it),node); 
				if (current>deepest)
					deepest = current;
			}
		}
		return deepest;
	}

	/*-----------------------------------------*/
	/** Traversal in which nodes are ignored as illegal.
	 * Codes for nodes:
	 * ILLEGAL if illegal and nothing in subclade is legal; 
	 * SEMILEGAL: node itself is illegal but subclade includes legal one;
     * LEGAL if legal (i.e. either more than one descendant is legal, or this is a legal terminal
	 * The permits tree traversal where illegal lineages are ignored as if they aren't there
	 * */
	public static final int ILLEGAL = 2;
	public static final int SEMILEGAL = 1;
	public static final int LEGAL = 0;
	/*-----------------------------------------*/
	/** Returns the first (left-most) legal daughter of node, where legality is implied by integer array with values 
	 * */
	public  int firstLegalDaughterOfNode(int node, int[] legality) {
		if (legality == null || node>=legality.length)
			return firstDaughterOfNode(node);
		if (!inBounds(node))
			return 0;
		for (int candidate = firstDaughterOfNode(node); nodeExists(candidate); candidate= nextSisterOfNode(candidate)){
			if (candidate>=legality.length)
				return candidate;
			if (legality[candidate] == LEGAL) //candidate itself is legal; return it
				return candidate;
			if (legality[candidate] == SEMILEGAL) //candidate itself is illegal, but its subclade contains something legal
				return firstLegalDaughterOfNode(candidate, legality);
		}
		return 0;
	}
	/*-----------------------------------------*/
	/** Returns the next legal sister of node, where legality is implied by integer array with values 
	 * */
	public  int nextLegalSisterOfNode(int node, int[] legality) {
		if (legality == null || node>=legality.length)
			return nextSisterOfNode(node);
		if (!inBounds(node))
			return 0;

		//check through sisters until find one legal
		for (int candidate = nextSisterOfNode(node); nodeExists(candidate); candidate= nextSisterOfNode(candidate)){
			if (candidate>=legality.length)
				return candidate;
			if (legality[candidate] == LEGAL) //candidate itself is legal; return it
				return candidate;
			if (legality[candidate] == SEMILEGAL) //candidate itself is illegal, but its subclade contains something legal
				return firstLegalDaughterOfNode(candidate, legality);
		}
		int mom = motherOfNode(node);  //if this is legal, then you've gone as far as you need to go; next sister not found
		if (!nodeExists(mom) || mom>=legality.length || legality[mom] == LEGAL)
			return 0;
		
		//all sisters next are entirely illegal; go down to mother and the next aunt
		for (int ancestor = motherOfNode(node); nodeExists(ancestor); ancestor= motherOfNode(ancestor)){
			
			//look to all this ancestor's sisters
			for (int candidate = nextSisterOfNode(ancestor); nodeExists(candidate); candidate= nextSisterOfNode(candidate)){
				if (candidate>=legality.length)
					return candidate;
				if (legality[candidate] == LEGAL) //candidate itself is legal; return it
					return candidate;
				if (legality[candidate] == SEMILEGAL) //candidate itself is illegal, but its subclade contains something legal
					return firstLegalDaughterOfNode(candidate, legality);
			}
			// have looked to ancestor's sisters, and not found.  The next ancestor deeper is legal, then you're done.
			int ancAnc = motherOfNode(ancestor);  
			
			if (!nodeExists(ancAnc) || ancAnc>=legality.length || legality[ancAnc] == LEGAL)//if this is ancestor legal, then you've gone as far as you need to go; next sister not found
				return 0;
			
		}
		
		//nothing found!!!	
		return 0;
	}
	/*-----------------------------------------*/
	/** Returns the right-most daughter of node.*/
	public int lastLegalDaughterOfNode(int node, int[] legality) {
		if (!inBounds(node))
			return 0;

		int thisSister = firstLegalDaughterOfNode(node, legality);
		while (nodeExists(nextLegalSisterOfNode(thisSister, legality)))
			thisSister = nextLegalSisterOfNode(thisSister, legality);
		return thisSister;
	}
	/*-----------------------------------------*/
	/** Returns the legal root 
	 * */
	public  int getLegalRoot(int[] legality) {
		int r = getRoot();
		if (legality == null)
			return r;
		// if first and last the same, there is only one path, so follow it until there are two, or you hit the tips
		while ((firstLegalDaughterOfNode(r, legality) == lastLegalDaughterOfNode(r, legality)) && (inBounds(firstLegalDaughterOfNode(r, legality))))
			r = firstLegalDaughterOfNode(r, legality);

		if (firstLegalDaughterOfNode(r, legality) == lastLegalDaughterOfNode(r, legality))
			return 0;
		return r;
	}
	/*-----------------------------------------*/
	/** Returns the first (left-most) daughter of node.*/
	public  int firstDaughterOfNode(int node) {
		if (!inBounds(node))
			return 0;
		return firstDaughter[node]; }
	/*-----------------------------------------*/
	/** Returns the right-most daughter of node.*/
	public int lastDaughterOfNode(int node) {
		if (!inBounds(node))
			return 0;

		int thisSister = firstDaughterOfNode(node);
		while (nodeExists(nextSisterOfNode(thisSister)))
			thisSister = nextSisterOfNode(thisSister);
		return thisSister;
	}
	/*-----------------------------------------*/
	/** Returns the array of daughters of a node.  Normally it will be best to cycle through the
	daughters as shown in the recursion example in the documentation for the Tree class.*/
	public int[] daughtersOfNode(int node) {
		if (!inBounds(node))
			return null;
		int thisSister = firstDaughterOfNode(node);
		int count = 0;
		while (nodeExists(thisSister)) {
			count ++;
			thisSister = nextSisterOfNode(thisSister);
		}
		int[] desc = new int[count];
		count=0;
		thisSister = firstDaughterOfNode(node);
		while (nodeExists(thisSister)) {
			desc[count] = thisSister;
			count ++;
			thisSister = nextSisterOfNode(thisSister);
		}
		return desc;
	}
	/*-----------------------------------------*/
	/** Returns true if branchD is an immediate daughter of branchA */
	public boolean daughterOf(int branchD, int branchA) { 
		if (branchD == branchA)
			return false;
		else if (!nodeExists(branchD)|!nodeExists(branchA))
			return false;
		else if (nodeIsInternal(branchA)) {
			int thisSister = firstDaughterOfNode(branchA);
			while (nodeExists(thisSister)) {
				if (thisSister==branchD)
					return true;
				thisSister = nextSisterOfNode(thisSister);
			}
			return false;
		}
		return false;
	}
	/*-----------------------------------------*/
	/** Returns true if node is the first (leftmost) daughter of its mother.*/
	public boolean nodeIsFirstDaughter(int node) {
		if (!inBounds(node))
			return false;
		if (firstDaughterOfNode(motherOfNode(node))==node) 
			return true;
		else
			return false;
	}
	/*-----------------------------------------*/
	/** Returns the index of the node among its sisters (i.e., if it is daughter number 0 of
	its mother, 1, 2, etc. (zero based).*/
	public int whichDaughter(int node) {
		if (!nodeExists(node) || !nodeExists(motherOfNode(node)))
			return -1;
		int m = motherOfNode(node);


		int thisSister = firstDaughterOfNode(m);
		if (thisSister==node)
			return 0;
		int count =0;
		while (thisSister!=node && nodeExists(thisSister)) {
			thisSister = nextSisterOfNode(thisSister);
			count++;
		}
		if (count==0)
			return -1;
		return count;
	}
	/*-----------------------------------------*/
	/** Returns the indexTH daughter of node (zero based).*/
	public int indexedDaughterOfNode(int node, int index) {
		if (!inBounds(node))
			return 0;
		int thisSister = firstDaughterOfNode(node);
		int count = 0;
		while (nodeExists(thisSister)) {
			if (count == index)
				return thisSister;
			count ++;

			thisSister = nextSisterOfNode(thisSister);
		}
		return 0;
	}
	/*-----------------------------------------*/
	/** Returns the number of daughters of the node.*/
	public int numberOfDaughtersOfNode(int node) {
		if (!inBounds(node))
			return 0;
		int thisSister = firstDaughterOfNode(node);
		int count = 0;
		while (nodeExists(thisSister)) {
			count ++;
			thisSister = nextSisterOfNode(thisSister);
		}
		return count;
	}
	/*-----------------------------------------*/
	/** Returns if branchD is descendant of branchA, then which daughter of branchA does it descend from?
		Returns 0 if not descendant.*/
	public int whichDaughterDescendantOf(int branchD, int branchA) { 
		if (branchD == branchA)
			return 0;
		else if (!nodeExists(branchD)|!nodeExists(branchA))
			return 0;
		else {
			int lastB;
			int b = branchD;
			while (b!=subRoot) {
				lastB = b;
				b = motherOfNode(b);
				if (b==branchA)
					return lastB;
			}
			return 0;
		}
	}
	/*-----------------------------------------*/
	/** Returns the node's sister immediately to the right.  If the node has no
	sister to the right, returns 0 (which is not a valid node designation).*/
	public  int nextSisterOfNode(int node) {
		if (!inBounds(node))
			return 0;
		return nextSister[node]; }
	/*-----------------------------------------*/
	/** Returns the node's sister immediately to the left.  If the node has no 
	sister to the right, returns 0 (which is not a valid node designation).*/
	public int previousSisterOfNode(int node) {
		if (!inBounds(node))
			return 0;
		int thisSister = firstDaughterOfNode(motherOfNode(node));
		if (node==thisSister)
			return 0;
		int lastSister;
		while (nodeExists(thisSister)) {
			lastSister=thisSister;
			thisSister = nextSisterOfNode(thisSister);
			if (thisSister==node)
				return lastSister;
		}
		return 0;
	}
	/*-----------------------------------------*/
	/** Returns true if branch1 and branch2 are sisters by their mother (i.e., primary parent).
	There is currently no method to return whether two nodes share at least one parent.*/
	public  boolean nodesAreSisters(int branch1, int branch2) { 
		if (branch1 == branch2)
			return false;
		else if (branch1== root || branch2 == root)
			return false;
		else if (!nodeExists(branch1)|!nodeExists(branch2))
			return false;
		else 
			return (motherOfNode(branch1)==motherOfNode(branch2));
	}
	/*-----------------------------------------*/
	/** Returns whether clade has nodes with more than one parent.*/
	private  boolean hasReticulations(int node) {
		if (!inBounds(node))
			return false;
		if (numberOfParentsOfNode(node)>1) 
			return true;
		if (nodeIsTerminal(node))
			return false;
		for (int d = firstDaughterOfNode(node); nodeExists(d); d = nextSisterOfNode(d))
			if (hasReticulations(d))
				return true;
		return false;
	}
	/*-----------------------------------------*/
	/** Returns whether tree has nodes with more than one parent.*/
	public  boolean hasReticulations() {
		return hasReticulations(getRoot());
	}

	/*-----------------------------------------*/
	/** Deletes any unbranched internal nodes.*/
	public  void deleteUnbranchedInternals(int node, boolean notify) {
		if (!inBounds(node))
			return;
		if (nodeIsTerminal(node))
			return;
		for (int d = firstDaughterOfNode(node); nodeExists(d); d = nextSisterOfNode(d)){
			deleteUnbranchedInternals(d, notify);
		}
		int d =  firstDaughterOfNode(node);
		while (nodeExists(d)){
			if (nodeIsUnbranchedInternal(d)) {
				int nextd = nextSisterOfNode(d);
				collapseBranch(d, false);
				d = nextd;
			}
			else 
				d =  nextSisterOfNode(d);
		}
	}
	/*-----------------------------------------*/
	/** Returns whether clade has unbranched internal nodes.*/
	public  boolean hasUnbranchedInternals(int node) {
		if (!inBounds(node))
			return false;
		if (nodeIsTerminal(node))
			return false;
		if (nodeIsUnbranchedInternal(node))
			return true;
		for (int d = firstDaughterOfNode(node); nodeExists(d); d = nextSisterOfNode(d))
			if ( hasUnbranchedInternals(d))
				return true;
		return false;
	}
	/*-----------------------------------------*/
	/** Returns true if the node is an internal node with only a single daughter.*/
	public boolean nodeIsUnbranchedInternal(int node) {
		if (!inBounds(node))
			return false;
		if (nodeIsTerminal(node))
			return false;
		int thisSister = firstDaughterOfNode(node);
		int count = 0;
		while (nodeExists(thisSister)) {
			count ++;
			if (count>1)
				return false;
			thisSister = nextSisterOfNode(thisSister);
		}
		return true;
	}
	/*-----------------------------------------*/
	/** Returns whether clade has polytomies.*/
	public  boolean hasPolytomies(int node) {
		if (!inBounds(node))
			return false;
		if (nodeIsTerminal(node))
			return false;
		if (nodeIsPolytomous(node))
			return true;
		for (int d = firstDaughterOfNode(node); nodeExists(d); d = nextSisterOfNode(d))
			if ( hasPolytomies(d))
				return true;
		return false;
	}
	/*-----------------------------------------*/
	/** Returns true if the node is polytomous (has more than two daughters).*/
	public boolean nodeIsPolytomous(int node) {
		if (!inBounds(node))
			return false;
		if (nodeIsTerminal(node))
			return false;
		int thisSister = firstDaughterOfNode(node);
		int count = 0;
		while (nodeExists(thisSister)) {
			count ++;
			if (count>2)
				return true;
			thisSister = nextSisterOfNode(thisSister);
		}
		return false;
	}
	/*-----------------------------------------*/
	/** Returns true if the node has one descendant, two, or is a hard polytomy.*/
	public boolean nodeIsHard(int node) { //TODO: in future allow individual trees or nodes to store whether polytomy is hard or soft
		if (!inBounds(node))
			return false;
		if (!nodeIsPolytomous(node))
			return true;
		if (polytomiesHard==2)
			return polytomyDefaultHard;
		return polytomiesHard == 0;
	}
	/*-----------------------------------------*/
	/** Returns true if the node is a soft polytomy.*/
	public boolean nodeIsSoft(int node) {
		if (!inBounds(node))
			return false;
		if (!nodeIsPolytomous(node))
			return false;
		if (polytomiesHard==2)
			return !polytomyDefaultHard;
		return polytomiesHard == 1;
	}
	/*-----------------------------------------*/
	/** Sets the polytomies assumption for this tree; 0= hard; 1 = soft; 2 = unassigned.*/
	public void setPolytomiesAssumption(int assumption, boolean notify){
		if (polytomiesHard != assumption){
			polytomiesHard = assumption;
			incrementVersion(MesquiteListener.BRANCHES_REARRANGED, notify);
		}
	}
	/*-----------------------------------------*/
	/** Returns the polytomies assumption for this tree; 0= hard; 1 = soft; 2 = unassigned.*/
	public int getPolytomiesAssumption(){
		return polytomiesHard;
	}
	/*-----------------------------------------*/
	/** Returns whether clade has soft polytomies (uncertain resolution).*/
	public  boolean hasSoftPolytomies(int node) {
		if (nodeIsSoft(node))
			return true;
		for (int d = firstDaughterOfNode(node); nodeExists(d); d = nextSisterOfNode(d))
			if (hasSoftPolytomies(d))
				return true;
		return false;
	}
	/*-----------------------------------------*/
	/** Returns number of total nodes (internal and external) in clade.*/
	public  int numberOfNodesInClade(int node) {
		if (!inBounds(node))
			return 0;
		int count = 0;
		for (int d = firstDaughterOfNode(node); nodeExists(d); d = nextSisterOfNode(d))
			count += numberOfNodesInClade(d);
		count++; //count node itself
		return count;
	}
	/*-----------------------------------------*/
	/** Returns number of terminal taxa in clade.*/
	public  int numberOfTerminalsInClade(int node) {
		if (!inBounds(node))
			return 0;
		if (nodeIsTerminal(node))
			return 1; //count node itself
		else{
			int count = 0;
			for (int d = firstDaughterOfNode(node); nodeExists(d); d = nextSisterOfNode(d))
				count += numberOfTerminalsInClade(d);
			return count;
		}
	}
	/*-----------------------------------------*/
	/** Returns number of internal nodes in clade.*/
	public  int numberOfInternalsInClade(int node) {
		if (!inBounds(node))
			return 0;
		int count = 0;
		if (nodeIsInternal(node)) {
			count++; //count node itself
			for (int d = firstDaughterOfNode(node); nodeExists(d); d = nextSisterOfNode(d))
				count += numberOfInternalsInClade(d);
		}
		return count;
	}
	/*-----------------------------------------*/
	/** Returns the left-most terminal that is descendant from node.*/
	public  int leftmostTerminalOfNode(int node) {
		if (!inBounds(node))
			return 0;
		if (nodeIsTerminal(node))
			return node;
		else {
			int d = firstDaughter[node];
			while (nodeIsInternal(d))
				d = firstDaughter[d];
			return d; 
		}
	}
	/*-----------------------------------------*/
	/** Returns the right-most terminal that is descendant from node.*/
	public  int rightmostTerminalOfNode(int node) {
		if (!inBounds(node))
			return 0;
		if (nodeIsTerminal(node))
			return node;
		else
			return rightmostTerminalOfNode(lastDaughterOfNode(node)); 
	}
	/*-----------------------------------------*/
	private void fillBits(int node, Bits b) {
		if (nodeIsInternal(node)) {
			for (int d = firstDaughterOfNode(node); nodeExists(d); d = nextSisterOfNode(d))
				fillBits(d, b);
		}
		else {
			int n = taxonNumberOfNode(node);
			b.setBit(n);
		}
	}
	/*-----------------------------------------*/
	/** Returns list of terminal taxa of clade of node.*/
	public Bits getTerminalTaxaAsBits(int node){
		if (!inBounds(node))
			return null;
		Bits b = new Bits(numNodeSpaces);
		fillBits(node, b);
		return b;
	}
	/*-----------------------------------------*/
	private void offBits(int node, Bits b) {
		if (nodeIsInternal(node)) {
			for (int d = firstDaughterOfNode(node); nodeExists(d); d = nextSisterOfNode(d))
				offBits(d, b);
		}
		else {
			int n = taxonNumberOfNode(node);
			b.setBit(n, false);
		}
	}
	/*-----------------------------------------*/
	/** Returns list of terminal taxa in a tree that are NOT of clade of node.*/
	public Bits getTerminalTaxaComplementAsBits(int node){
		if (!inBounds(node))
			return null;
		Bits b = new Bits(numNodeSpaces);
		fillBits(getRoot(), b);  // turn on all bits
		offBits(node, b);  // turn off all bits that are in clade
		return b;
	}
	/*-----------------------------------------*/
	/** Returns list of terminal taxa of clade of node.*/
	public int[] getTerminalTaxa(int node){
		if (!inBounds(node))
			return null;
		int numTerms = numberOfTerminalsInClade(node);
		int[] result = new int[numTerms];
		MesquiteInteger count = new MesquiteInteger(0);
		if (numTerms>0)
			fillTermAr(node, result, count);
		return result;
	}
	/*-----------------------------------------*/
	private void gNAtHeight(int node, double target, int[] nodes, double heightToAncestor, double lengthIfUnassigned, MesquiteInteger count){
		if (target>heightToAncestor){
			double bL = getBranchLength(node, lengthIfUnassigned);
			if (node != getRoot() && heightToAncestor + bL >= target) { //this is a branch that crosses the target height
				nodes[count.getValue()] = node;
				count.increment();
			}
			else if (nodeIsInternal(node)) {
				if (node != getRoot())
					heightToAncestor += bL;
				for (int d = firstDaughterOfNode(node); nodeExists(d); d = nextSisterOfNode(d))
					gNAtHeight(d, target, nodes, heightToAncestor, lengthIfUnassigned, count);
			}
		}
	}
	/*-----------------------------------------*/
	/** Returns list of nodes at a particular height above the root.  Array may have some elements filled with -1 meaning no node*/
	public int[] getNodesAtHeight(double height, double lengthIfUnassigned, int[] nodes){
		if (nodes == null || nodes.length < taxa.getNumTaxa())
			nodes = new int[taxa.getNumTaxa()];
		for (int i = 0; i< nodes.length; i++)
			nodes[i] = -1;
		MesquiteInteger count = new MesquiteInteger(0);
		gNAtHeight(getRoot(), height, nodes, 0.0, lengthIfUnassigned, count);

		return nodes;
	}
	/*-----------------------------------------*/
	private void fillTermAr(int node, int[] ar, MesquiteInteger count){
		if (nodeIsTerminal(node)) {
			ar[count.getValue()] = taxonNumberOfNode(node);
			count.increment();
		}
		else
			for (int d = firstDaughterOfNode(node); nodeExists(d); d = nextSisterOfNode(d))
				fillTermAr(d, ar, count);
	}
	/*-----------------------------------------*/
	/** Returns list of terminal taxa NOT in clade of node.*/
	public int[] getTerminalTaxaComplement(int node){
		return getTerminalTaxaComplement(getTerminalTaxa(getRoot()),getTerminalTaxa(node));
	}
	/*-----------------------------------------*/
	/** Returns list of terminal taxa NOT in clade of node.  Into this version you pass the int[] of the terminal taxa in the tree, for speed's sake. */
	public int[] getTerminalTaxaComplement(int[] allTerminals, int node){
		return getTerminalTaxaComplement(allTerminals,getTerminalTaxa(node));
	}
	/*-----------------------------------------*/
	/** Returns list of terminal taxa NOT in clade of node.  Into this version you pass the int[] of the terminal taxa in the tree, for speed's sake. */
	public int[] getTerminalTaxaComplement(int[] allTerminals, int[] nodeTerminals){
		return IntegerArray.subtractArrays(allTerminals,nodeTerminals);
	}
	/*-----------------------------------------*/
	private synchronized void downMarkPathsFromTerminals(Bits terminals, Bits nodes, int node){
		for (int d = firstDaughterOfNode(node); nodeExists(d); d = nextSisterOfNode(d)) {
			downMarkPathsFromTerminals(terminals, nodes, d);
			if (nodeIsTerminal(d) && terminals.isBitOn(taxonNumberOfNode(d))){   // this is a terminal
				nodes.setBit(d);
			}
			if (nodes.isBitOn(d)){   // there is terminal above this point
				nodes.setBit(node);
			}
		}
	}
	/*-----------------------------------------*/
	private synchronized void upMarkPathsFromTerminals(Bits nodes, int node){
		int numLines = 0;
		int line =-1;
		for (int d = firstDaughterOfNode(node); nodeExists(d); d = nextSisterOfNode(d)) {
			if (nodes.isBitOn(d)){   // there is terminal above this point
				numLines++;
				line = d;
			}
		}
		if (numLines<=1)
			nodes.setBit(node,false);
		if (numLines==1)
			upMarkPathsFromTerminals(nodes, line);
	}
	/*-----------------------------------------*/
	private synchronized void markPathsFromTerminals(Bits terminals, Bits nodes, int node) { 
		if (terminals==null || nodes==null)
			return;
		downMarkPathsFromTerminals(terminals, nodes, node);
		upMarkPathsFromTerminals(nodes,  node);
	}
	/*-----------------------------------------*/
	private synchronized boolean scanForUnmarkedDescendants(Bits terminals, Bits nodes, int node){
		for (int d = firstDaughterOfNode(node); nodeExists(d); d = nextSisterOfNode(d)) {
			if (scanForUnmarkedDescendants(terminals, nodes, d))
				return true;
			if (!nodes.isBitOn(d) && nodes.isBitOn(node)){   // an ancestor is marked but not a descendant
				return true;
			}
		}
		return false;
	}
	/*-----------------------------------------*/
	/** Returns true if the terminals form a clade.*/
	public boolean isClade(Bits terminals) { 
		Bits nodes = new Bits(numNodeSpaces);
		markPathsFromTerminals(terminals, nodes, getRoot());
		return !scanForUnmarkedDescendants(terminals, nodes, getRoot());
	}
	/*-----------------------------------------*/
	/** Returns true if the terminals form a clade.*/
	public int makeClade(Bits terminals) { 
		int mrca = mrca(terminals);
		Bits nodes = new Bits(numNodeSpaces);
		markPathsFromTerminals(terminals, nodes,mrca);
		int count = 0;
		int firstDaughter = 0;
		for (int d = firstDaughterOfNode(mrca); nodeExists(d); ) {
			int nextDaughter = nextSisterOfNode(d);
			if (nodes.isBitOn(d)){
				if (count==0)
					firstDaughter=d;
				moveBranch(d,firstDaughter,false);
				if (count>1)
					collapseBranch(motherOfNode(firstDaughter),false);
				count++;
			}
			d= nextDaughter;
		}
		return motherOfNode(firstDaughter);
	}
	/*-----------------------------------------*/
	private synchronized void scanForMultipleTransitions(Bits terminals, Bits nodes, int node, MesquiteInteger numTransitions, MesquiteInteger descendantBoundary){
		for (int d = firstDaughterOfNode(node); nodeExists(d); d = nextSisterOfNode(d)) {
			scanForMultipleTransitions(terminals, nodes, d, numTransitions, descendantBoundary);
			if (numTransitions.getValue()>1)
				return;
			if (nodes.isBitOn(d) != nodes.isBitOn(node)){   // an ancestor is marked but not a descendant
				numTransitions.add(1);
				descendantBoundary.setValue(d);
			}
		}
		if (numTransitions.getValue()>1)
			return;
	}
	/*-----------------------------------------*/
	/** Returns true iff the terminal taxa listed in "terminals" form a convex part of the tree.  Also returns the node that forms the boundary*/
	public boolean isConvex(Bits terminals, MesquiteInteger descendantBoundary) {
		if (terminals==null) {
			return false;
		}
		MesquiteInteger single = new MesquiteInteger();
		if (terminals.oneBitOn(single)) {
			descendantBoundary.setValue(nodeOfTaxonNumber(single.getValue()));
			return true;
		}
		else {
			Bits nodes = new Bits(numNodeSpaces);
			markPathsFromTerminals(terminals, nodes, getRoot());
			MesquiteInteger numTransitions = new MesquiteInteger(0);
			scanForMultipleTransitions(terminals, nodes, getRoot(), numTransitions, descendantBoundary);
			return numTransitions.getValue()<=1;
		}
	}
	/*-----------------------------------------*/
	/** Returns true iff the terminal taxa listed in "terminals" form a convex part of the tree. */
	public boolean isConvex(Bits terminals) {
		MesquiteInteger boundary = new MesquiteInteger(0);
		return isConvex(terminals, boundary);
	}

	public  void convertBranchLengthToNodeValue(int node, NameReference nameRef){
		if (nodeIsInternal(node)) {
			double bl = getBranchLength(node); 
			if (MesquiteDouble.isCombinable(bl)){
				setAssociatedDouble(nameRef, node, 0.01*bl, true);  // value will be a percentage
			}
			for (int d = firstDaughterOfNode(node); nodeExists(d) && !nodeWasFound; d = nextSisterOfNode(d))
				convertBranchLengthToNodeValue(d, nameRef);
		}
	}

	public  void convertBranchLengthsToNodeValue(String nameRefString){
		NameReference nameRef = NameReference.getNameReference("consensusFrequency");
		convertBranchLengthToNodeValue(getRoot(), nameRef);
	}


	/*-----------------------------------------*/
	/** Returns the nth terminal node in the clade (0 based), from left to right.  If is too low or high,
	 returns 0.*/
	public int getTerminalNode(int node, int n){
		if (!inBounds(node))
			return 0;
		int numTerms = numberOfTerminalsInClade(node);
		if (numTerms==0 || n>=numTerms || n<0)
			return 0;
		MesquiteInteger count = new MesquiteInteger(0);
		MesquiteInteger result = new MesquiteInteger(0);
		findTerm(node, n, result, count);
		return result.getValue();
	}
	private void findTerm(int node, int n, MesquiteInteger ar, MesquiteInteger count){
		if (nodeIsTerminal(node)) {
			if (count.getValue()== n)
				ar.setValue(node);
			count.increment();
		}
		else
			for (int d = firstDaughterOfNode(node); nodeExists(d) && ar.getValue()==0; d = nextSisterOfNode(d))
				findTerm(d, n, ar, count);
	}
	/*-----------------------------------------*/
	public void cleanTree(){
		for (int i=0; i<firstDaughter.length; i++){	
			if (!nodeInTree(i)){
				firstDaughter[i]=0;
				nextSister[i]=0;
				mother[i]=0;
				setTaxonNumber(i, -1);
			}
		}
		firstDaughter[0]=-1;
		nextSister[0]=-1;
		mother[0]=-1;
		setTaxonNumber(0, -1);
		mother[subRoot] = -1;
		mother[root] = subRoot;
		firstDaughter[subRoot]=root;
	}
	/*-----------------------------------------*/
	private boolean nodeWasFound;
	private void findNodeInTree(int node, int sought){
		if (node==sought)
			nodeWasFound=true;
		for (int d = firstDaughterOfNode(node); nodeExists(d) && !nodeWasFound; d = nextSisterOfNode(d))
			findNodeInTree(d, sought);
	}
	/** Returns whether node is part of tree. Differs from nodeExists by doing full recursion (more reliable, slower).*/
	public boolean nodeInTree(int sought){
		if (!inBounds(sought))
			return false;
		nodeWasFound=false;
		if ((sought>1) && (sought<=numNodeSpaces))
			findNodeInTree(root, sought);
		return nodeWasFound;
	}
	/** Returns whether taxon is part of tree. Does a full recursion (more reliable, slower).*/
	private boolean taxonWasFound = false;
	public void findTaxonInTree(int node, int sought){
		if (nodeIsTerminal(node) && taxonNumber[node]==sought)
			taxonWasFound=true;
		for (int d = firstDaughterOfNode(node); nodeExists(d) && !taxonWasFound; d = nextSisterOfNode(d))
			findTaxonInTree(d, sought);
	}
	/** Returns whether taxon is part of tree. */
	public boolean taxonInTree(int taxonNum){
		taxonWasFound=false;
		if ((taxonNum>=0) && (taxonNum<taxa.getNumTaxa()))
			findTaxonInTree(root, taxonNum);
		return taxonWasFound;
	}
	/*-----------------------------------------*/
	public int nextInPreorder(int node){
		if (nodeIsInternal(node))
			return firstDaughterOfNode(node);
		if (nodeExists(nextSisterOfNode(node)))
			return nextSisterOfNode(node);
		int mother = motherOfNode(node);
		while (mother != root && !nodeExists(nextSisterOfNode(mother))) {
			mother = motherOfNode(mother);
		}
		if (mother == root)
			return 0;
		return nextSisterOfNode(mother);
	}
	/*-----------------------------------------*/
	public int nextInPostorder(int node){
		if (node == root)
			return 0;
		if (nodeExists(nextSisterOfNode(node)))
			return leftmostTerminalOfNode(nextSisterOfNode(node));
		return motherOfNode(node);


	}
	public int firstInPostorder(){
		return leftmostTerminalOfNode(root);
	}
	/*-----------------------------------------*/
	private int countNodes;
	private int nodeFound;
	/** goes through the tree returning which node is the nodeNumberTH found in the traversal */
	private synchronized void findNodeInTraversal(int node, int nodeNumber){
		countNodes++;
		if ((countNodes==nodeNumber) && (nodeFound==0))
			nodeFound=node;
		for (int d = firstDaughterOfNode(node); nodeExists(d) && nodeFound==0; d = nextSisterOfNode(d))
			findNodeInTraversal(d, nodeNumber);
	}
	/*-----------------------------------------*/
	/**Returns indexTH node in tree traversal*/
	public int nodeInTraversal(int index){
		countNodes=-1;
		nodeFound=0;
		findNodeInTraversal(root, index);
		return nodeFound;
	}
	/*-----------------------------------------*/
	/**Returns indexTH node in traversal through clade*/
	public int nodeInTraversal(int index, int cladeRoot){
		countNodes=-1;
		nodeFound=0;
		findNodeInTraversal(cladeRoot, index);
		return nodeFound;
	}
	/*-----------------------------------------*/
	/** Returns most recent common ancestor of two branches.*/
	public int mrca(int branchA, int branchB) { 
		if (branchB == branchA)
			return branchB;
		else if (!nodeExists(branchB)|!nodeExists(branchA))
			return 0;
		else if (descendantOf(branchA, branchB) )
			return branchB;
		else if (descendantOf(branchB, branchA) )
			return branchA;
		else {
			int a = branchA;
			while (a!=subRoot) {
				a = motherOfNode(a);
				if (descendantOf(branchB, a)) {
					return a;
				}
			}
			return 0;
		}
	}
	/*-----------------------------------------*/
	/** Returns most recent common ancestor for an array of nodes.*/
	public int mrca(int[] nodes){
		for (int nodeCheck = 0; nodeCheck < nodes.length; nodeCheck++){
			if(!nodeExists(nodes[nodeCheck])){
				return 0;
			}
		}
		return mrca(nodes, false);
	}
	/*-----------------------------------------*/
	/** Returns most recent common ancestor for the selected taxa.*/
	public int mrcaSelected(){
		return mrca(taxa.getSelectedBits());
	}
	/*-----------------------------------------*/
	/** Returns most recent common ancestor for an array of nodes; if boolean ignoreMissing = false, it 
	 * will only return a non-zero (existing) node if all the nodes in the array are present in the 
	 * tree.  If ignoreMissing = true, it will return the mrca of those nodes passed in the node array
	 * that are actually present in the tree; nodes in the node array that are not present in the tree 
	 * are ignored.*/
	public int mrca(int[] nodes, boolean ignoreMissing){
		boolean allIncluded = false;
		int a = nodes[0];
		if(!ignoreMissing){
			while (a!=subRoot){
				a = motherOfNode(a);
				descendantCheckLoop: for (int descendantCheck = 0; descendantCheck < nodes.length; descendantCheck++){
					if(!descendantOf(nodes[descendantCheck], a)){
						allIncluded = false;
						break descendantCheckLoop;
					} else {
						allIncluded = true;
					}
				}
				if(allIncluded){
					return a;
				}
			}
			return 0;
		}
		else{
			int nodeCheck = 1;
			while(!nodeExists(a) && nodeCheck < (nodes.length - 1)){
				a = nodes[nodeCheck];
				nodeCheck++;
			}
			if(!nodeExists(a) && nodeCheck==(nodes.length-1)) //at least two nodes of passed array MUST exist in the tree for a mrca to be defined.
				return 0;
			while (a!=subRoot){
				a = motherOfNode(a);
				descendantCheckLoop: for (int descendantCheck = 0; descendantCheck < nodes.length; descendantCheck++){
					/*The following conditional makes sure that only those nodes that exist in the tree are used
					 * to determine mrca.  If node does not exist in the tree, it is ignored.*/
					if(nodeExists(nodes[descendantCheck])){
						if(!descendantOf(nodes[descendantCheck], a)){
							allIncluded = false;
							break descendantCheckLoop;
						}
						else {
							allIncluded = true;
						}
					}
				}
				if(allIncluded){
					return a;
				}
			}
			return 0;
		}
	}
	/*-----------------------------------------*/
	/** Returns most recent common ancestor for an array of taxon numbers; if boolean ignoreMissing = false, it 
	 * will only return a non-zero (existing) node if all the taxons in the array are present in the 
	 * tree.  If ignoreMissing = true, it will return the mrca of those taxons passed in the taxon array
	 * that are actually present in the tree; taxons in the taxon array that are not present in the tree 
	 * are ignored.*/
	public int mrcaTaxons(int[] taxons, boolean ignoreMissing){
		int[] nodes = new int[taxons.length];
		for(int it = 0; it < nodes.length; it++){
			nodes[it] = nodeOfTaxonNumber(taxons[it]);
		}
		return mrca(nodes, ignoreMissing);
	}
	/*-----------------------------------------*/
	private synchronized int getDeepest(int aTerminal, Bits nodes){
		int d = aTerminal;
		for (int p = aTerminal; nodeExists(p); p = motherOfNode(p)) {
			if (!nodes.isBitOn(p))
				return d;
			d = p;
		}
		return getRoot();
	}
	/** Returns true if the first node is an ancestor of the second node.*/
	public boolean isAncestor(int potentialAncestor, int node){
		int mN = motherOfNode(node);
		while (nodeExists(mN)){
			if (mN==potentialAncestor)
				return true;
			if (mN==subRoot)
				return false;
			mN = motherOfNode(mN);
		}
		return false;
	}

	/*-----------------------------------------*/
	/** Returns most recent common ancestor of the terminals designated in terminals.*/
	public int mrca(Bits terminals) { 
		if (terminals == null)
			return getRoot();
		Bits nodes = new Bits(numNodeSpaces);
		markPathsFromTerminals(terminals, nodes, getRoot());
		int firstTerminal = terminals.firstBitOn();
		return getDeepest(nodeOfTaxonNumber(firstTerminal), nodes);
	}
	/*-----------------------------------------*/
	/** Returns the closest ancestor that has more than one daughter.*/
	public  int branchingAncestor(int node) {
		if (!inBounds(node))
			return 0;
		if (node==root)
			return node;
		int anc = motherOfNode(node);
		while(numberOfDaughtersOfNode(anc)==1 && anc!=subRoot) //or root?
			anc = motherOfNode(anc);
		return anc;
	}
	/*-----------------------------------------*/
	/** Returns the closest descendant that has more than one daughter, or is terminal.*/
	public  int branchingDescendant(int node) {
		if (!inBounds(node))
			return 0;
		if (nodeIsTerminal(node))
			return node;
		int desc = node;
		while(numberOfDaughtersOfNode(desc)==1 && nodeIsInternal(desc))
			desc = firstDaughterOfNode(desc);
		return desc;
	}
	/*-----------------------------------------*/
	/** Returns the next (clockwise) to node connected to anc.  This is one of the UR procedures, designed
	to allow unrooted style traversal through the tree*/
	public int nextAroundUR(int anc, int node){
		if (!nodeExists(node) && !nodeExists(anc))
			return 0;
		if (node== mother[anc])
			return firstDaughter[anc];
		int candidate = nextSister[node];
		if (!nodeExists(candidate)) {
			if (anc==root)
				candidate = firstDaughter[root];
			else
				candidate = mother[anc];
		}
		return candidate;
	}
	/*-----------------------------------------*/
	/** Returns the first (left-most) daughter of node in an UNROOTED sense where the node
	is treated as descendant from anc. This is one of the UR procedures, designed
	to allow unrooted style traversal through the tree*/
	public  int firstDaughterOfNodeUR(int anc, int node) {
		if (!nodeExists(node) && !nodeExists(anc))
			return 0;
		if (anc==mother[node] || anc == node) //heading up tree or starting here
			return firstDaughter[node]; 
		else
			return nextAroundUR(node, anc);
	}
	/*-----------------------------------------*/
	/** Returns the node's sister immediately to the right in an UNROOTED sense where the node
	is treated as descendant from anc, which is descendant from ancAnc.  If the node has no
	sister to the right, returns 0 (which is not a valid node designation). This is one of the UR procedures, designed
	to allow unrooted style traversal through the tree*/
	public  int nextSisterOfNodeUR(int ancAnc, int anc, int node) {
		if (!nodeExists(node) && !nodeExists(anc))
			return 0;
		int candidate = nextAroundUR(anc, node);
		if (candidate == ancAnc)
			return 0;
		else if (ancAnc==anc && candidate == firstDaughter[anc])
			return 0;
		else
			return candidate;
	}
	/*-----------------------------------------* WAYNECHECK

	public  int lastDaughterOfNodeUR(int anc, int node) {
		if (!nodeExists(node) && !nodeExists(anc))
			return 0;
		int first = firstDaughterOfNodeUR(anc, node);
		int prev = first;
		while (nodeExists(first)) {
			prev = first;
			first = nextSisterOfNodeUR(anc, node, first);
		}
		return prev;
	}
	*/
	
	/** Returns the first (left-most) daughter of node in an UNROOTED sense where the node
	is treated as descendant from anc. This is one of the UR procedures, designed
	to allow unrooted style traversal through the tree*/

	public  int lastDaughterOfNodeUR(int anc, int node) {
		if (!nodeExists(node) && !nodeExists(anc))
			return 0;
		int first = firstDaughterOfNodeUR(anc, node);
		int prev = first;
		
		int current = first;
		while (nodeExists(current)) {
			prev = current;
			current = nextSisterOfNodeUR(anc, node, current);
			if (current==first)
				return prev;
		}
		return prev;
	}

	/*-----------------------------------------*/
	/** Returns what node number in Mesquite's standard rooted sense corresponds to the anc-node branch.*/
	public  int nodeOfBranchUR(int anc, int node) {
		if (anc==mother[node])
			return node; 
		else if (node == mother[anc])
			return anc;
		else
			return 0;
	}
	
	/** Returns all of the "daughters" of a node, treating the tree as unrooted.  That is, it returns as
	 * one of the daughters the mother of the node (which should be the the last entry in the array). 
	 * If you pass into root the MRCA of a subtree containing "node", then it will treat
	 * that subtree as unrooted.  Note:  if node is not the root or a descendant of root, then this will return null */
	public int[] daughtersOfNodeUR (int root, int node){  
		if (nodeIsInternal(node) && (!getRooted() || isAncestor(root, node) || root==node)){
			int numDaughters = numberOfDaughtersOfNode(node);
			if (node!=root)
				numDaughters ++;
			int[] daughters = new int[numDaughters];
			int firstDaughterUR = firstDaughterOfNode(node); // use firstDaughter as the first daughter 
			int nextDaughterUR = firstDaughterUR;
			int count=0;
			while (nodeExists(nextDaughterUR)) {
				daughters[count]=nextDaughterUR;
				nextDaughterUR = nextAroundUR(node, nextDaughterUR);
				if (nextDaughterUR==firstDaughterUR)  // we are back where we started
					break;
				count++;
			}		
			return daughters;
		}
		return null;
	}

	/*-----------------------------------------*/
	/** Returns whether any branchLengths are assigned.*/
	private boolean lengthsAssigned(int node) { 
		if (branchLength ==null)
			return false;
		if (!MesquiteDouble.isUnassigned(branchLength[node]))
			return true;
		for (int daughter = firstDaughterOfNode(node); nodeExists(daughter); daughter = nextSisterOfNode(daughter))
			if (lengthsAssigned(daughter))
				return true;

		return false;
	}
	/*-----------------------------------------*/
	/** Returns true if tree has branch lengths.*/
	public boolean hasBranchLengths() { 
		if (branchLength ==null)
			return false;
		return lengthsAssigned(root);
	}
	/*-----------------------------------------*/
	/** Returns whether all branchLengths are assigned.*/
	private boolean allLengthsAssigned(int node, boolean includeRoot) { 
		if (branchLength ==null)
			return false;
		if ((node != root || includeRoot) && MesquiteDouble.isUnassigned(branchLength[node]))
			return false;
		for (int daughter = firstDaughterOfNode(node); nodeExists(daughter); daughter = nextSisterOfNode(daughter))
			if (!allLengthsAssigned(daughter, includeRoot))
				return false;

		return true;
	}
	/*-----------------------------------------*/
	/** Returns true if tree has all nodes with branch lengths assigned.*/
	public boolean allLengthsAssigned(boolean includeRoot) { 
		return allLengthsAssigned(root, includeRoot);
	}
	/*-----------------------------------------*/
	/** Returns true if tree has all nodes with branch lengths assigned.*/
	public boolean allLengthsAssigned() { 
		return allLengthsAssigned(true);
	}
	/*-----------------------------------------*/
	/** Returns the branch length of the node.*/
	public  double getBranchLength(int node) { 
		if (!inBounds(node))
			return MesquiteDouble.unassigned;
		if (branchLength==null)
			return MesquiteDouble.unassigned;
		else 
			return branchLength[node];
	}
	/*-----------------------------------------*/
	/** Returns the branch length of the node.  If the branch length is unassigned, pass back the double passed in*/
	public  double getBranchLength(int node, double ifUnassigned) { 
		if (!inBounds(node))
			return ifUnassigned;
		if (branchLength==null)
			return ifUnassigned;
		else if (MesquiteDouble.isUnassigned(branchLength[node]))
			return ifUnassigned;
		else
			return branchLength[node];
	}
	/*-----------------------------------------*/
	/** Sets the branch length of nodes (stored as a double internally).*/
	private void setLengths(int node, double length) { 
		for (int daughter = firstDaughterOfNode(node); nodeExists(daughter); daughter = nextSisterOfNode(daughter))
			setLengths(daughter, length);

		branchLength[node]= length; 
	}
	/*-----------------------------------------*/
	/** Sets the branch length of all nodes whose length is currently unassigned.*/
	private  void setAllUnassignedBranchLengthsRec(int node, double length) { 
		for (int daughter = firstDaughterOfNode(node); nodeExists(daughter); daughter = nextSisterOfNode(daughter))
			setAllUnassignedBranchLengthsRec(daughter, length);
		if (getBranchLength(node) == MesquiteDouble.unassigned)
			setBranchLength(node, length, false); 
	}
	/*-----------------------------------------*/
	/** Sets the branch length of all nodes whose length is currently unassigned.*/
	public  void setAllUnassignedBranchLengths(double length, boolean notify) { 
		if (branchLength==null) {
			branchLength = new double[numNodeSpaces];
			for (int i=0; i<numNodeSpaces; i++) {
				branchLength[i]= MesquiteDouble.unassigned;
			}
		}
		setAllUnassignedBranchLengthsRec(getRoot(), length);
		incrementVersion(BRANCHLENGTHS_CHANGED, notify);
	}
	/*-----------------------------------------*/
	/** Sets the branch length of node.*/
	public  void setAllBranchLengths(double length, boolean notify) { 

		if (branchLength==null) {
			branchLength = new double[numNodeSpaces];
			for (int i=0; i<numNodeSpaces; i++) {
				branchLength[i]= MesquiteDouble.unassigned;
			}
		}
		setLengths(root, length); 
		incrementVersion(BRANCHLENGTHS_CHANGED, notify);
	}
	/*-----------------------------------------*/
	/** Sets the branch length of node.*/
	public  void scaleAllBranchLengths(double factor, boolean notify) { 

		if (branchLength==null) {
			return;
		}
		for (int i=0; i<numNodeSpaces; i++)
			if (nodeExists(i) && !branchLengthUnassigned(i))
				setBranchLength(i, getBranchLength(i)*factor, false);
		incrementVersion(BRANCHLENGTHS_CHANGED, notify);
	}
	/*-----------------------------------------*/
	/** Sets the branch length of node (stored as a double internally).*/
	public  void setBranchLength(int node, int length, boolean notify) { 
		if (!inBounds(node))
			return;
		if (branchLength==null) {
			branchLength = new double[numNodeSpaces];
			for (int i=0; i<numNodeSpaces; i++) {
				branchLength[i]= MesquiteDouble.unassigned;
			}
		}
		if (MesquiteInteger.unassigned ==length)
			branchLength[node]= MesquiteDouble.unassigned; 
		else 
			branchLength[node]= length; 
		if (MesquiteInteger.isCombinable(length) && length<0)
			;//MesquiteMessage.notifyProgrammer("Warning: branch length being set to negative number (" + length + ")");
		incrementVersion(BRANCHLENGTHS_CHANGED, notify);
	}
	/*-----------------------------------------*/
	/** Sets the branch length of node.*/
	public  void setBranchLength(int node, double length, boolean notify) { 
		if (!inBounds(node))
			return;
		if (branchLength==null) {
			branchLength = new double[numNodeSpaces];
			for (int i=0; i<numNodeSpaces; i++) {
				branchLength[i]= MesquiteDouble.unassigned;
			}
		}
		branchLength[node] = length; 
		if (MesquiteDouble.isCombinable(length) && length<0)
			;//MesquiteMessage.notifyProgrammer("Warning: branch length being set to negative number (" + length + ")");
		incrementVersion(BRANCHLENGTHS_CHANGED, notify);
	}
	/*-----------------------------------------*/
	/** Returns whether branch length of node is unassigned.*/
	public  boolean branchLengthUnassigned(int node) { 
		if (!inBounds(node))
			return true;
		return (branchLength==null || MesquiteDouble.isUnassigned(branchLength[node]));
	}
	/*-----------------------------------------*/
	/** returns tallest path in number of nodes, not branch length, above a node.  Added 3 Nov 01 WPM */
	public int mostStepsAboveNode (int node) {
		if (!inBounds(node))
			return 0;
		if (nodeIsTerminal(node))
			return 0;
		int maximum = 0;
		for (int daughter=firstDaughterOfNode(node); nodeExists(daughter); daughter = nextSisterOfNode(daughter) ) {
			int thisWay = mostStepsAboveNode(daughter) + 1;
			if (thisWay>maximum)
				maximum = thisWay;
		}
		return maximum;
	}
	/*-----------------------------------------*/
	/** returns the node whose distance from the root in branch length is the tallest.  Added 3 Nov 01 WPM*/
	public int tallestNode (int node) {
		if (!inBounds(node))
			return node;
		if (nodeIsTerminal(node))
			return node;
		double maximum = 0;
		int tallest = 0;
		for (int daughter=firstDaughterOfNode(node); nodeExists(daughter); daughter = nextSisterOfNode(daughter) ) {
			double thisWay = tallestPathAboveNode(daughter);
			if (!branchLengthUnassigned(daughter))
				thisWay += getBranchLength(daughter);
			if (thisWay>maximum) {
				maximum = thisWay;
				tallest = tallestNode(daughter);
			}
		}
		return tallest;
	}
	/*-----------------------------------------*/
	/** returns the node whose distance from the root in branch length is the tallest.  Added 3 Nov 01 WPM*/
	public int tallestNode (int node, double perUnassignedLength) {
		if (!inBounds(node))
			return node;
		if (nodeIsTerminal(node))
			return node;
		double maximum = 0;
		int tallest = 0;
		for (int daughter=firstDaughterOfNode(node); nodeExists(daughter); daughter = nextSisterOfNode(daughter) ) {
			double thisWay = tallestPathAboveNode(daughter);
			thisWay += getBranchLength(daughter, perUnassignedLength);
			if (thisWay>maximum) {
				maximum = thisWay;
				tallest = tallestNode(daughter);
			}
		}
		return tallest;
	}
	/*-----------------------------------------*/
	/** returns total of branchlengths from node up to tallest terminal */
	public double tallestPathAboveNode (int node) {
		if (!inBounds(node))
			return 0;
		if (nodeIsTerminal(node))
			return 0;
		double maximum = 0;
		for (int daughter=firstDaughterOfNode(node); nodeExists(daughter); daughter = nextSisterOfNode(daughter) ) {
			double thisWay = tallestPathAboveNode(daughter);
			if (!branchLengthUnassigned(daughter))
				thisWay += getBranchLength(daughter);
			if (thisWay>maximum)
				maximum = thisWay;
		}
		return maximum;
	}
	/*-----------------------------------------*/
	/** returns total of branchlengths from node up to tallest terminal */
	public double tallestPathAboveNodeUR (int anc, int node) {
		if (!inBounds(node) || !inBounds(anc))
			return 0;
		if (nodeIsTerminal(node))
			return 0;
		double maximum = 0;
		for (int daughter=firstDaughterOfNodeUR(anc, node); nodeExists(daughter); daughter = nextSisterOfNodeUR(anc, node, daughter) ) {
			double thisWay = tallestPathAboveNodeUR(node, daughter);
			if (!branchLengthUnassigned(nodeOfBranchUR(node,daughter)))
				thisWay += getBranchLength(nodeOfBranchUR(node,daughter));
			if (thisWay>maximum)
				maximum = thisWay;
		}
		return maximum;
	}
	/*-----------------------------------------*/
	/** returns total of branchlengths from node up to tallest terminal */
	public double tallestPathAboveNodeUR (int anc, int node, double perUnassignedLength) {
		if (!inBounds(node) || !inBounds(anc))
			return 0;
		if (nodeIsTerminal(node))
			return 0;
		double maximum = 0;
		for (int daughter=firstDaughterOfNodeUR(anc, node); nodeExists(daughter); daughter = nextSisterOfNodeUR(anc, node, daughter) ) {
			double bL =getBranchLength(nodeOfBranchUR(node,daughter));
			if (MesquiteDouble.isUnassigned(bL))
				bL = perUnassignedLength;
			double thisWay = tallestPathAboveNodeUR(node, daughter, perUnassignedLength) +bL;
			if (thisWay>maximum)
				maximum = thisWay;
		}
		return maximum;
	}
	/*-----------------------------------------*/
	/** returns total of branchlengths from node up to tallest terminal, with unassigned lengths given value "perUnassignedLength" */
	public double tallestPathAboveNode (int node, double perUnassignedLength) {
		if (!inBounds(node))
			return 0;
		if (nodeIsTerminal(node))
			return 0;
		double maximum = 0;
		for (int daughter=firstDaughterOfNode(node); nodeExists(daughter); daughter = nextSisterOfNode(daughter) ) {
			double bL =getBranchLength(daughter);
			if (MesquiteDouble.isUnassigned(bL))
				bL = perUnassignedLength;
			double thisWay = tallestPathAboveNode(daughter, perUnassignedLength) +bL;
			if (thisWay>maximum)
				maximum = thisWay;
		}
		return maximum;
	}
	/*-----------------------------------------*/
	/** returns total of branchlengths from node up to shortest terminal, with unassigned lengths given value "perUnassignedLength" */
	public double shortestPathAboveNode (int node, double perUnassignedLength) {
		if (!inBounds(node))
			return 0;
		if (nodeIsTerminal(node))
			return 0;
		double minimum = MesquiteDouble.unassigned;
		for (int daughter=firstDaughterOfNode(node); nodeExists(daughter); daughter = nextSisterOfNode(daughter) ) {
			double bL =getBranchLength(daughter);
			if (MesquiteDouble.isUnassigned(bL))
				bL = perUnassignedLength;
			double thisWay = shortestPathAboveNode(daughter, perUnassignedLength) +bL;
			if (minimum == MesquiteDouble.unassigned || thisWay<minimum)
				minimum = thisWay;
		}
		return minimum;
	}
	/*-----------------------------------------*/
	/** returns total of branchlengths from node down to root, with unassigned lengths given value "perUnassignedLength" */
	public double distanceToRoot (int node, boolean countUnassigned, double perUnassignedLength) {
		if (!inBounds(node))
			return 0;
		if (node == root || node == subRoot)
			return 0;
		int current = node;
		double total = 0.0;
		boolean found = false;
		int count = 0;
		while (current != root && (count++ < getNumNodeSpaces())){
			double bL = getBranchLength(current);
			if (!MesquiteDouble.isCombinable(bL)){
				if (countUnassigned && bL == MesquiteDouble.unassigned){
					total += perUnassignedLength;
					found = true;
				}
			}
			else {
				total += bL;
				found = true;
			}
			current = motherOfNode(current);
		}
		if (!found)
			return MesquiteDouble.unassigned;
		return total;
	}
	/*-----------------------------------------*/

	static boolean numericalLabelInterpretationSetRUN = false;
	boolean numericalLabelInterpretationSet = false;
	static boolean interpretNumericalLabelsAsOnBranchesRUN = false;
	boolean interpretNumericalLabelsAsOnBranches = false;
	static boolean interpretLabelsAsNumericalRUN = false;
	boolean interpretLabelsAsNumerical = false;
	static String defaultValueCodeRUN = "";
	String defaultValueCode = "";
	NameReference defaultValueCodeRef = null;
	
	boolean checkNumericalLabelInterpretation(String c){
		if (numericalLabelInterpretationSet){ //user has answered, therefore follow guidance
			if (interpretLabelsAsNumerical)
				return true;
			return false;
		}

		if (taxa != null){
			MesquiteProject project = taxa.getProject();
			FileCoordinator fc = project.getCoordinatorModule();
			TreesManager em = (TreesManager)fc.findManager(fc, TreeVector.class);
			boolean[] interps = new boolean[4]; //0 interpret as number (vs. text); 1 interpret as on branch (vs. node); 2 remember
			MesquiteString n = new MesquiteString(); //the code name of the value, e.g. "consensusFrequency"
			numericalLabelInterpretationSet = em.queryAboutNumericalLabelIntepretation(interps, c, n);
			if (numericalLabelInterpretationSet){
				if (interps[0]){ // treat as number
					interpretLabelsAsNumerical = true;
					defaultValueCode = StringUtil.tokenize(n.getValue());
					defaultValueCodeRef = NameReference.getNameReference(defaultValueCode);
					interpretNumericalLabelsAsOnBranches = interps[1];
					if (interps[2]){
						defaultValueCodeRUN = defaultValueCode;
						interpretNumericalLabelsAsOnBranchesRUN = interpretNumericalLabelsAsOnBranches;
						numericalLabelInterpretationSetRUN = true;
					}
					return true;
				}
			}
		}
		return false;
	}
	/*-----------------------------------------*/
	NameReference branchNotesRef = NameReference.getNameReference("note");
	/** Reads a token referring to a named internal node. */
	private String readNamedInternal(String TreeDescription, String c, int sN, MesquiteInteger stringLoc){

		if (convertInternalNames){
			setAssociatedObject(branchNotesRef, sN, c);
			return ParseUtil.getToken(TreeDescription, stringLoc);  //skip parens or next comma
		}
		else {
			int taxon = taxa.whichTaxonNumber(c, false, permitTruncTaxNames && !permitTaxaBlockEnlargement);
			if (taxon>=0){
				System.out.println("Observed taxon " + c + " in ancestral position; not yet allowed by Mesquite.  Tree will not be read in properly");
			}
			if (cosmeticInternalNames){
				if (MesquiteNumber.isNumber(c) && checkNumericalLabelInterpretation(c)){
					double d = MesquiteDouble.fromString(c);
					setAssociatedDouble(defaultValueCodeRef, sN, d, interpretNumericalLabelsAsOnBranches);
				}
				else 
					setNodeLabel(c, sN); 
				
				if (taxa!=null && taxa.getClades()!=null && taxa.getClades().findClade(c) == null)
					taxa.getClades().addClade(c);
				return ParseUtil.getToken(TreeDescription, stringLoc);  //skip parens or next comma
			}
			else if (MesquiteNumber.isNumber(c) && checkNumericalLabelInterpretation(c)){
				double d = MesquiteDouble.fromString(c);
				setAssociatedDouble(defaultValueCodeRef, sN, d, interpretNumericalLabelsAsOnBranches);
				return ParseUtil.getToken(TreeDescription, stringLoc);  //skip parens or next comma
			}
			else {
				int labNode = nodeOfLabel(c);
				if (labNode==-1) {
					setNodeLabel(c, sN); 
					if (taxa!=null && taxa.getClades()!=null && taxa.getClades().findClade(c) == null)
						taxa.getClades().addClade(c);
					return ParseUtil.getToken(TreeDescription, stringLoc);  //skip parens or next comma
				}
				else {//IF LABEL already exists, then attach new ancestor
					String s = "";
					if (permitTruncTaxNames)
						s =" (This may have occured because of a corrupted file, or because tree reading is set to permit truncated taxon names (see Defaults menu to turn this off), leading to ambiguities.)"; 
					if (numReticWarnings++ < 5)
						MesquiteMessage.warnProgrammer("Apparent reticulation found (two taxon names or clade names interpreted as the same)." + s  + " " + TreeDescription);
					else if (numReticWarnings == 5)
						MesquiteTrunk.mesquiteTrunk.discreetAlert("Five warnings about apparent reticulations have been given. " + s + "  If there are further problems in this run of Mesquite, only short warnings will be given");
					else if (numReticWarnings <100)
						MesquiteMessage.println("Another tree with apparent reticulations found.");
					else if (numReticWarnings == 100)
						MesquiteMessage.println("NO MORE WARNINGS ABOUT RETICULATIONS WILL BE GIVEN IN THIS RUN OF MESQUITE.");


					setParentOfNode(labNode, motherOfNode(labNode), false);
					setParentOfNode(labNode, motherOfNode(sN), false);
					return ParseUtil.getToken(TreeDescription, stringLoc);  //skip parens or next comma
				}
			}
		}
	}
	static int numReticWarnings = 0;
	static boolean dWarn = true;
	private boolean expectedPunctuation(String c){
		return (")".equals(c) || ",".equals(c) || ";".equals(c) || "<".equals(c));
	}
	public void setPermitTaxaBlockEnlargement(boolean permit){
		this.permitTaxaBlockEnlargement = permit;
	}
	public boolean getPermitTaxaBlockEnlargement(){
		return permitTaxaBlockEnlargement;
	}
	/** Takes the node information in a file created by a recent version of MrBayes, and retokenizes it as MrBayes does not use standard NEXUS tokenization rules for this. */
	protected String retokenizeMrBayesConTreeNodeInfo(String nodeInfo) {
		if (StringUtil.blank(nodeInfo))
			return nodeInfo;
		nodeInfo = StringUtil.replace(nodeInfo, " ", "");
		if (nodeInfo.indexOf('&')<=1)
			nodeInfo=nodeInfo.replaceFirst("&", " ");
		nodeInfo= nodeInfo.replace("\"", "\'");  // replace double quotes with single quotes
		return nodeInfo;
	}
	
	private void readAssociatedInTree (String TreeDescription, int node, MesquiteInteger stringLoc) {
		if (readingMrBayesConTree) {
			String c = ParseUtil.getToken(TreeDescription, stringLoc, "", ">", false) + ">";  //get next token
			c = retokenizeMrBayesConTreeNodeInfo(c);
			readAssociated(c, node, new MesquiteInteger(0), null, ",=>{}");
			ParseUtil.getToken(TreeDescription, stringLoc, "", ">"); //skip ">"
		} else
			readAssociated(TreeDescription, node, stringLoc);

	}
	/*...............................................  read tree ....................................................*/
	/** Continues reading a tree description, starting at node "node" and the given location on the string*/
	static final int CONTINUE = 0;
	static final int DONT_SPROUT = 1;
	static final int FAILED = 2;
	private int readClade(String TreeDescription, int node, MesquiteInteger stringLoc, TaxonNamer namer,  String whitespaceString, String punctuationString) {
		if (StringUtil.blank(TreeDescription))
			return FAILED;

		String c = ParseUtil.getToken(TreeDescription, stringLoc, whitespaceString, punctuationString);
		if ("<".equals(c)) {
			while (!">".equals(c)  && c != null) 
				c=ParseUtil.getToken(TreeDescription, stringLoc, whitespaceString, punctuationString);
			c=ParseUtil.getToken(TreeDescription, stringLoc, whitespaceString, punctuationString);
		}
		if ("(".equals(c)){  //internal node
			int sprouted = sproutDaughter(node, false);
			int result = readClade(TreeDescription, sprouted,stringLoc, namer, whitespaceString, punctuationString);
			if (result == FAILED)//������������������������
				return FAILED;
			c = ParseUtil.getToken(TreeDescription, stringLoc, whitespaceString, punctuationString);  //skip comma
			if (!((",".equals(c))||(")".equals(c)) || (":".equals(c)) || "<".equals(c) || "%".equals(c) || "#".equals(c))){ // name of internal node!!!!
				c = readNamedInternal(TreeDescription, c, sprouted, stringLoc);
			}
			while (":".equals(c) || "<".equals(c)|| "%".equals(c) || "#".equals(c)) {
				if (":".equals(c)) {
					readLength(TreeDescription, sprouted, stringLoc);
					c = ParseUtil.getToken(TreeDescription, stringLoc, whitespaceString, punctuationString);  //get next token
					if (!expectedPunctuation(c)) {
						MesquiteMessage.warnProgrammer("bad token in tree where ,  ) ; expected (" + c + ") 1");
						return FAILED;
					}
				}
				else if ("%".equals(c) || "#".equals(c)) {
					skipValue(TreeDescription, sprouted, stringLoc);
					c = ParseUtil.getToken(TreeDescription, stringLoc, whitespaceString, punctuationString);  //get next token
					if (!expectedPunctuation(c)) {
						MesquiteMessage.warnProgrammer("bad token in tree where ,  ) ; expected (" + c + ") 2");
						return FAILED;
					}
				}
				else if ("<".equals(c)) {
					readAssociatedInTree(TreeDescription, sprouted, stringLoc);
					c = ParseUtil.getToken(TreeDescription, stringLoc, whitespaceString, punctuationString);  //get next token
					if (!(c!=null && ":".equals(c)) && !expectedPunctuation(c)) {
						MesquiteMessage.warnProgrammer("bad token in tree where ,  ) ; expected (" + c + ") 3");
						return FAILED;
					}
				}
			}
			while (",".equals(c)) {
				if (result == CONTINUE)
					sprouted = sproutDaughter(node, false);
				result = readClade(TreeDescription, sprouted,stringLoc, namer, whitespaceString, punctuationString);
				if (result == FAILED) //������������������������
					return FAILED;
				c = ParseUtil.getToken(TreeDescription, stringLoc, whitespaceString, punctuationString); //skip parens or next comma
				if (!((",".equals(c))||(")".equals(c)) || (":".equals(c)) || "<".equals(c)|| "%".equals(c) || "#".equals(c))){ // name of internal node!!!!
					c = readNamedInternal(TreeDescription, c, sprouted, stringLoc);
				}
				while (":".equals(c) || "<".equals(c)|| "%".equals(c) || "#".equals(c)) {
					if (":".equals(c)) {
						readLength(TreeDescription, sprouted, stringLoc);
						c = ParseUtil.getToken(TreeDescription, stringLoc, whitespaceString, punctuationString);  //get next token
						if (!expectedPunctuation(c)) {
							MesquiteMessage.warnProgrammer("bad token in tree where ,  ) ; expected (" + c + ") 4");
							return FAILED;
						}
					}
					else if ("%".equals(c) || "#".equals(c)) {
						skipValue(TreeDescription, sprouted, stringLoc);
						c = ParseUtil.getToken(TreeDescription, stringLoc, whitespaceString, punctuationString);  //get next token
						if (!expectedPunctuation(c)) {
							MesquiteMessage.warnProgrammer("bad token in tree where ,  ) ; expected (" + c + ") 5");
							return FAILED;
						}
					}
					else if ("<".equals(c)) {
						readAssociatedInTree(TreeDescription, sprouted, stringLoc);
						c = ParseUtil.getToken(TreeDescription, stringLoc, whitespaceString, punctuationString);  //get next token
						if (!(c!=null && ":".equals(c)) && !expectedPunctuation(c)) {
							MesquiteMessage.warnProgrammer("bad token in tree where ,  ) ; expected (" + c + ") 6");
							return FAILED;
						}
					}
				}
			}
			return CONTINUE;
		}
		else {
			int path = 0;
			int taxonNumber = -1;
			int fromWhichNamer = -1;
			if (namer != null){
				taxonNumber = namer.whichTaxonNumber(taxa, c);
				fromWhichNamer = 1;
			}
			if (taxonNumber < 0){
				if (treeVector !=null) {
					taxonNumber = treeVector.whichTaxonNumber(c, permitTruncTaxNames && !permitTaxaBlockEnlargement); //use treeVector's translation table if one is available
					fromWhichNamer = 2;
					if (taxonNumber <0) {
						taxonNumber = taxa.whichTaxonNumber(c, false, permitTruncTaxNames && !permitTaxaBlockEnlargement);
						fromWhichNamer = 3;
					}
					path += 1;
				}
				else {  //if (taxonNumber == -1) {
					taxonNumber = taxa.whichTaxonNumber(c, false, permitTruncTaxNames && !permitTaxaBlockEnlargement);
					fromWhichNamer = 4;
				}
				if (taxonNumber<0){
					/* Debugg.  WAYNECHECK: DAVIDCHECK: there is a problem with this.  If you read in a Zephyr produced tree file by itself, not after
					 * opening the file with the taxa in order, then you will likely get reticulations.  In particular, permitTONames not only
					 * interprets "tx" as a taxon name, it presumes that the taxon number of this taxon number x.  The problem with this
					 * is that in reading the treefile, it creates the taxa in the orders it encounters them.  If t88 is the first taxon in the first
					 * treedescription, this will be taxon 0.  So if it reads t0, it will interpret it as taxon 0, even if it is the 99th taxon read in. 
					 * */
					if (MesquiteTree.permitT0Names && c != null && c.startsWith("t")){  //not found in taxon names, but as permits t0, t1 style names, look for it there 
						String number = c.substring(1, c.length());
						int num = MesquiteInteger.fromString(number);
						if (MesquiteInteger.isCombinable(num) && num>=0 && num<taxa.getNumTaxa())
							taxonNumber = num;
					}
				}
			}
			if ( taxonNumber >=0){ //taxon successfully found
				if (taxonNumber>= nodeOfTaxon.length)
					resetNodeOfTaxonNumbers();
				if (taxonNumber>= nodeOfTaxon.length){
					MesquiteMessage.warnProgrammer("taxon number too high found (" + c + "); number: "+taxonNumber);
					taxonNumber = namer.whichTaxonNumber(taxa, c);
					return FAILED;
				}

				if (nodeOfTaxon[taxonNumber]<=0){  // first time taxon encountered
					//if (nodeOfTaxonNumber(taxonNumber)<=0){  // first time taxon encountered
					setTaxonNumber(node, taxonNumber, false);
					return CONTINUE;
				}
				else {
					int termN = nodeOfTaxonNumber(taxonNumber);
					if (motherOfNode(termN) != motherOfNode(node)) { //protect against redundant references; NOTE: may not protect if more than two parents
						//apparent reticulation found!
						String s = "";
						if (permitTruncTaxNames)
							s =" (This may have occured because of a corrupted file, or because tree reading is set to permit truncated taxon names (see Defaults menu to turn this off), leading to ambiguities.)"; 
						if (numReticWarnings++ < 5)
							MesquiteMessage.warnProgrammer("Apparent reticulation found (two taxon names or clade names interpreted as the same). [" + c + "] " + s  + " " + TreeDescription);
						else if (numReticWarnings == 5)
							MesquiteTrunk.mesquiteTrunk.discreetAlert("Five warnings about apparent reticulations have been given. " + s + "  If there are further problems in this run of Mesquite, only short warnings will be given");
						else if (numReticWarnings <100){
							MesquiteMessage.println("Another tree with apparent reticulations found.");
						}
						else if (numReticWarnings == 100)
							MesquiteMessage.println("NO MORE WARNINGS ABOUT RETICULATIONS WILL BE GIVEN IN THIS RUN OF MESQUITE.");
						setParentOfNode(termN, motherOfNode(termN), false);
						setParentOfNode(termN, motherOfNode(node), false);
						return DONT_SPROUT; //don't continue up tree
					}
					else {//redundant
						MesquiteMessage.warnUser("Redundant taxon or node name in tree  (" + c + ", taxon number " + taxonNumber + "; number from " + fromWhichNamer + " permitTruncTaxNames " + permitTruncTaxNames + "); attempt will be made to read tree, but some clades or taxa may be missing. " + TreeDescription);
						//snip last daughter added, as redundant
						int prev = previousSisterOfNode(node);
						if (prev>=0 && prev<nextSister.length)
							nextSister[prev] = 0;
						return DONT_SPROUT;
					}
				}
			}
			else { //see if apparent taxon is actually name of previously labelled internal node
				int labNode = nodeOfLabel(c);
				if (labNode!=-1) {//IF LABEL already exists, then attach new ancestor
					if (motherOfNode(labNode) != motherOfNode(node)) { //protect against redundant references; NOTE: may not protect if more than two parents
						String s = "";
						if (permitTruncTaxNames)
							s =" (This may have occured because of a corrupted file, or because tree reading is set to permit truncated taxon names (see Defaults menu to turn this off), leading to ambiguities.)"; 
						if (numReticWarnings++ < 5)
							MesquiteMessage.warnProgrammer("Apparent reticulation found (two taxon names or clade names interpreted as the same). [" + c + "] " + s  + " " + TreeDescription);
						else if (numReticWarnings == 5)
							MesquiteTrunk.mesquiteTrunk.discreetAlert("Five warnings about apparent reticulations have been given. " + s + "  If there are further problems in this run of Mesquite, only short warnings will be given");
						else if (numReticWarnings <100)
							MesquiteMessage.println("Another tree with apparent reticulations found.");
						else if (numReticWarnings == 100)
							MesquiteMessage.println("NO MORE WARNINGS ABOUT RETICULATIONS WILL BE GIVEN IN THIS RUN OF MESQUITE.");
						setParentOfNode(labNode, motherOfNode(labNode), false);
						setParentOfNode(labNode, motherOfNode(node), false);
						//c = ParseUtil.getToken(TreeDescription, stringLoc);  //skip internal node name
						return DONT_SPROUT; //don't continue up tree
					}
					else {  //redundant
						MesquiteMessage.warnUser("Redundant taxon or node name in tree description  (" + c + ", internal node); attempt will be made to read tree, but some clades or taxa may be missing. " + TreeDescription);
						//snip last daughter added, as redundant
						int prev = previousSisterOfNode(node);
						if (prev>=0 && prev<nextSister.length)
							nextSister[prev] = 0;
						return DONT_SPROUT;
					}
				}
				else {
					if (permitTaxaBlockEnlargement){
						inProgressAddingTaxa = true;
						boolean success = taxa.addTaxa(taxa.getNumTaxa(), 1, true);
						if (success){
							taxa.setTaxonName(taxa.getNumTaxa()-1, c);
							oldNumTaxa = taxa.getNumTaxa();
							taxaIDs = taxa.getTaxaIDs();
							setTaxonNumber(node, taxa.getNumTaxa()-1, false);
							inProgressAddingTaxa = false;
							return CONTINUE;

						}
						inProgressAddingTaxa = false;
					}

					if (dWarn) { //modified 13 Nov 01
						if (AlertDialog.query(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Unrecognized taxon name", "Unrecognized name (\"" + c + "\") of terminal taxon in tree", "Continue", "Don't warn again", 0)) {
							dWarn = false;
						}
						MesquiteMessage.warnUser("Unrecognized name (\"" + c + "\") of terminal taxon in tree " + getName() + " for taxa " + getTaxa().getName() + " (search for \"ERROR>\" in output in log file) " + path);
						StringBuffer sb = new StringBuffer(TreeDescription);
						sb.insert(stringLoc.getValue()-1, "ERROR>");
						MesquiteTrunk.mesquiteTrunk.logln(sb.toString());
					}
					else {
						MesquiteMessage.warnUser("Unrecognized name (\"" + c + "\") of terminal taxon in tree " + getName() + " for taxa " + getTaxa().getName() + " (search for \"ERROR>\" in output in log file) " + path);
						lastUnrecognizedName = c;
					}
					return FAILED;
				}
			}
		}
	}
	public boolean graftCladeFromDescription(String TreeDescription, int node, MesquiteInteger stringLoc, TaxonNamer namer) {
		setTaxonNumber(node, -1);
		try {
			if (readClade(TreeDescription, node, stringLoc, namer, null, null) == FAILED){
				if (lastUnrecognizedName != null)
					MesquiteMessage.warnProgrammer("graft clade failed; taxon name unrecognized: " + lastUnrecognizedName);
				else
					MesquiteMessage.printStackTrace("graft clade failed");


				return false;
			}
			if (!checkTreeIntegrity(root)) {
				MesquiteMessage.printStackTrace("graft clade failed (integrity check)");
				return false;
			}
		}
		catch (Throwable e){
			MesquiteTrunk.mesquiteTrunk.logln("Problem grafting clade " + TreeDescription);

			MesquiteTrunk.mesquiteTrunk.exceptionAlert(e, "Problem reading tree");
			return false;
		}
		return true;
	}
	/*-----------------------------------------*/
	/* reads the branch length; allows exponentials and negative numbers*/
	private void skipValue (String TreeDescription, int node, MesquiteInteger stringLoc) {
		double d = MesquiteDouble.fromString(TreeDescription, stringLoc);
	}
	/*-----------------------------------------*/
	/* reads the branch length; allows exponentials and negative numbers*/
	private void readLength (String TreeDescription, int node, MesquiteInteger stringLoc) {
		double d = MesquiteDouble.fromString(TreeDescription, stringLoc);
		if (d!=MesquiteDouble.impossible)
			setBranchLength(node, d, false);
	}
	/*-----------------------------------------*/
	String lastUnrecognizedName = null;
	/** Reads the tree description string and sets the tree object to store the tree described.*/
	public boolean readTree(String TreeDescription) {
		return readTree(TreeDescription, null);
	}

	/** Reads the tree description string and sets the tree object to store the tree described.*/
	public boolean readTree(String TreeDescription, TaxonNamer namer) {
		return readTree(TreeDescription, null, null, null);
	}

	/** Reads the tree description string and sets the tree object to store the tree described.*/
	public boolean readTree(String TreeDescription, TaxonNamer namer, String whitespaceString, String punctuationString) {
		deassignAssociated();
		MesquiteInteger stringLoc = new MesquiteInteger(0);

		intializeTree();
		lastUnrecognizedName = null;
		try {
			if (readClade(TreeDescription, root, stringLoc, namer, whitespaceString, punctuationString) == FAILED){
				if (lastUnrecognizedName != null)
					MesquiteMessage.warnProgrammer("read clade failed; taxon name unrecognized: " + lastUnrecognizedName);
				else
					MesquiteMessage.printStackTrace("read clade failed");
				if (MesquiteTrunk.debugMode)
					MesquiteMessage.discreetNotifyUser("\nTree description: \n"+TreeDescription +"\n");

				intializeTree();

				return false;
			}
		}
		catch (Throwable e){
			MesquiteTrunk.mesquiteTrunk.logln("Problem reading tree " + TreeDescription);

			MesquiteTrunk.mesquiteTrunk.exceptionAlert(e, "Problem reading tree");
			return false;
		}
		String c = ParseUtil.getToken(TreeDescription, stringLoc, whitespaceString, punctuationString);  //skip comma or parens
		if (!StringUtil.blank(c) && !(";".equals(c))){  //TODO: all these "equals" should be replaced by StringUtil static methods
			if (!((",".equals(c))||(")".equals(c)) || (":".equals(c)) || "<".equals(c) || "%".equals(c) || "#".equals(c)))// name of internal node!!!!
				c = readNamedInternal(TreeDescription, c, root, stringLoc);
			while (":".equals(c) || "<".equals(c)|| "%".equals(c) || "#".equals(c)) {
				if (":".equals(c)) {
					readLength(TreeDescription, root, stringLoc);
					c = ParseUtil.getToken(TreeDescription, stringLoc, whitespaceString, punctuationString);  //skip comma or parens
					if (!expectedPunctuation(c)) {
						intializeTree();
						MesquiteMessage.warnProgrammer("bad token in tree where ,  ) ; expected (" + c + ") 7");
						return false;
					}
				}
				else if ("%".equals(c)||"#".equals(c)) {
					skipValue(TreeDescription, root, stringLoc);
					c = ParseUtil.getToken(TreeDescription, stringLoc, whitespaceString, punctuationString);  //skip comma or parens
					if (!expectedPunctuation(c)) {
						intializeTree();
						MesquiteMessage.warnProgrammer("bad token in tree where ,  ) ; expected (" + c + ") 8");
						return false;
					}
				}
				else if ("<".equals(c)) {
					readAssociatedInTree(TreeDescription, root, stringLoc);
					c = ParseUtil.getToken(TreeDescription, stringLoc, whitespaceString, punctuationString);  //skip comma or parens
					if (!(c!=null && ":".equals(c)) && !expectedPunctuation(c)) {
						intializeTree();
						MesquiteMessage.warnProgrammer("bad token in tree where ,  ) ; expected (" + c + ") 9");
						return false;
					}
					if ("<".equals(c)){
						readAttachedProperties(TreeDescription, stringLoc);
						c = ParseUtil.getToken(TreeDescription, stringLoc, whitespaceString, punctuationString);  //skip comma or parens
					}
					if ("<".equals(c)){
						readExtras(TreeDescription, stringLoc);
						c = ParseUtil.getToken(TreeDescription, stringLoc, whitespaceString, punctuationString);  //skip comma or parens
					}
				}
			}
		}
		selected = getWhichAssociatedBits(NameReference.getNameReference("selected"));
		checkAssociated();
		exists=true;
		if (!checkTreeIntegrity(root)) {
			intializeTree();
			MesquiteMessage.warnProgrammer("tree failed integrity check");
			return false;
		}
		incrementVersion(MesquiteListener.BRANCHES_REARRANGED,true);
		return true;
	}
	/*-----------------------------------------*/
	/** Sets the tree to be a default bush.*/
	public void setToDefaultBush(Bits whichTaxa, boolean notify) {
		int numTaxa = whichTaxa.numBitsOn();
		if (numTaxa == 0)
			return;
		String temp = "(";
		boolean first=true;
		for (int it=0; it<whichTaxa.getSize(); it++) {
			if (whichTaxa.isBitOn(it)){
				if (first)
					first=false;
				else
					temp+=",";
				temp+=Integer.toString(Taxon.toExternal(it));
			}
		}
		temp+=");";
		readTree(temp);
		incrementVersion(MesquiteListener.BRANCHES_REARRANGED,notify);
		setName("Default Bush");
	}
	/*-----------------------------------------*/
	/** Sets the tree to be a default bush.*/
	public void setToDefaultBush(int numTaxa, boolean notify) {
		if (numTaxa == 0)
			return;
		String temp = "(";
		boolean first=true;
		for (int it=0; it<numTaxa; it++) {
			if (first)
				first=false;
			else
				temp+=",";
			temp+=Integer.toString(Taxon.toExternal(it));
		}
		temp+=");";
		readTree(temp);
		incrementVersion(MesquiteListener.BRANCHES_REARRANGED,notify);
		setName("Default Bush");
	}
	/*-----------------------------------------*/
	/** Sets the tree to be a default ladder.*/
	public void setToDefaultLadder(int numTaxa, boolean notify) {
		String temp = "";
		for (int it=0; it<numTaxa-1; it++) 
			temp+="("+ Taxon.toExternal(it) +",";
		temp+=Taxon.toExternal(numTaxa-1);
		for (int it=0; it<numTaxa-1; it++)
			temp+=")";
		temp+=";";
		readTree(temp);
		incrementVersion(MesquiteListener.BRANCHES_REARRANGED,notify);
		setName("Default Ladder");
	}
	/*.................................................................................................................*/
	void formSymmetricalClade( int minTaxon, int maxTaxon){
		int range = maxTaxon-minTaxon + 1;
		if (range > 1) {
			int newRight = minTaxon + range/2;
			splitTerminal(minTaxon, newRight, false);
			formSymmetricalClade(minTaxon, newRight -1);
			formSymmetricalClade(newRight, maxTaxon);
		}
	}
	public void setToDefaultSymmetricalTree(int numTaxa, boolean notify) {
		if (numTaxa == 1){
			setToDefaultBush(1, false);
			return;
		}
		setToDefaultBush(2, false);
		int secondHalf = numTaxa/2;
		int rightNode = nextSisterOfNode(firstDaughterOfNode(getRoot()));
		setTaxonNumber(rightNode, secondHalf, false);
		formSymmetricalClade(0, secondHalf-1);
		formSymmetricalClade(secondHalf, numTaxa-1);
		incrementVersion(MesquiteListener.BRANCHES_REARRANGED,notify);
		setName("Default Symmetrical Tree");
	}
	/*-----------------------------------------*/
	/*.........................................Write tree.................................................*/
	/** Writes a tree description into the StringBuffer using taxon numbers instead of labels or taxon names */
	private void writeTreeByNumbersGeneral(int node, StringBuffer treeDescription, boolean includeAssociated, boolean includeBranchLengths, boolean includeNodeLabels, boolean zeroBased, String delimiter) {
		if (nodeIsInternal(node)) {
			treeDescription.append('(');
			int thisSister = firstDaughterOfNode(node);
			writeTreeByNumbersGeneral(thisSister, treeDescription, includeAssociated, includeBranchLengths, includeNodeLabels,  zeroBased, delimiter);
			while (nodeExists(thisSister = nextSisterOfNode(thisSister))) {
				if (delimiter!=null)
					treeDescription.append(delimiter);
				else
					treeDescription.append(' ');
				writeTreeByNumbersGeneral(thisSister, treeDescription, includeAssociated, includeBranchLengths, includeNodeLabels,  zeroBased,  delimiter);
			}
			treeDescription.append(')');
			if (includeNodeLabels && nodeHasLabel(node))
				treeDescription.append(StringUtil.tokenize(getNodeLabel(node)));
		}
		else if (zeroBased)
			treeDescription.append(Integer.toString(taxonNumberOfNode(node)));
		else
			treeDescription.append(Integer.toString(Taxon.toExternal(taxonNumberOfNode(node))));
		if (includeBranchLengths && !branchLengthUnassigned(node)) {
			treeDescription.append(':');
			treeDescription.append(MesquiteDouble.toStringDigitsSpecified(getBranchLength(node), -1)); //add -1 to signal full accuracy 17 Dec 01
		}
		if (includeAssociated){
			String a = writeAssociated(node, true);
			treeDescription.append(a);
		}
	}
	/*-----------------------------------------*/
	/** Writes a tree description into the StringBuffer using taxon names */
	private void writeTreeByNamesGeneral(int node, StringBuffer treeDescription, boolean includeAssociated, boolean includeBranchLengths,  boolean includeNodeLabels, String delimiter) {
		if (nodeIsInternal(node)) {
			treeDescription.append('(');
			int thisSister = firstDaughterOfNode(node);
			writeTreeByNamesGeneral(thisSister, treeDescription, includeAssociated, includeBranchLengths, includeNodeLabels,  delimiter);
			while (nodeExists(thisSister = nextSisterOfNode(thisSister))) {
				if (delimiter!=null)
					treeDescription.append(delimiter);
				else
					treeDescription.append(' ');
				writeTreeByNamesGeneral(thisSister, treeDescription, includeAssociated, includeBranchLengths, includeNodeLabels,  delimiter);
			}
			treeDescription.append(')');
			if (includeNodeLabels && nodeHasLabel(node))
				treeDescription.append(StringUtil.tokenize(getNodeLabel(node)));
		}
		else {
			treeDescription.append(StringUtil.tokenize(taxa.getTaxonName(taxonNumberOfNode(node))));
		}
		if (includeBranchLengths && !branchLengthUnassigned(node)) {
			treeDescription.append(':');
			treeDescription.append(MesquiteDouble.toStringDigitsSpecified(getBranchLength(node), -1)); //add -1 to signal full accuracy 17 Dec 01
		}
		if (includeAssociated){
			String a = writeAssociated(node, true);
			treeDescription.append(a);
		}
	}
	/*-----------------------------------------*/
	/** Returns a string describing the tree in standard parenthesis notation (Newick standard), using taxon
	names or numbers to refer to the taxa, depending on the boolean parameter byNames.*/
	public String writeTreeSimple(int byWhat, boolean includeAssociated, boolean includeBranchLengths, boolean includeNodeLabels, boolean zeroBased, String delimiter) {
		StringBuffer s = new StringBuffer(numberOfNodesInClade(root)*40);
		if (byWhat == BY_NAMES)
			writeTreeByNamesGeneral(root, s, includeAssociated, includeBranchLengths,  includeNodeLabels, delimiter);
		else if (byWhat == BY_NUMBERS)
			writeTreeByNumbersGeneral(root, s, includeAssociated, includeBranchLengths,  includeNodeLabels, zeroBased, delimiter);
		return s.toString();
	}
	/*-----------------------------------------*/
	/** Writes a tree into the string buffer, placing node numbers in comments after the nodes */
	private void writeTreeByNumbersWithNodeNumbers(int node, StringBuffer treeDescription, boolean suppressAssociated) {
		if (nodeIsInternal(node)) {
			treeDescription.append('(');
			int thisSister = firstDaughterOfNode(node);
			writeTreeByNumbersWithNodeNumbers(thisSister, treeDescription, suppressAssociated);
			while (nodeExists(thisSister = nextSisterOfNode(thisSister))) {
				treeDescription.append(',');
				writeTreeByNumbersWithNodeNumbers(thisSister, treeDescription, suppressAssociated);
			}
			treeDescription.append(')');
			if (nodeHasLabel(node))
				treeDescription.append(StringUtil.tokenize(getNodeLabel(node)));
			treeDescription.append("[" + node + "]");
		}
		else {
			treeDescription.append(Integer.toString(Taxon.toExternal(taxonNumberOfNode(node))));
			treeDescription.append("[" + node + "]");
		}
		if (!branchLengthUnassigned(node)) {
			treeDescription.append(':');
			treeDescription.append(MesquiteDouble.toStringDigitsSpecified(getBranchLength(node), -1)); //add -1 to signal full accuracy 17 Dec 01
		}
		if (!suppressAssociated){
			String a = writeAssociated(node, false);
			treeDescription.append(a);
		}
	}
	/*-----------------------------------------*/
	/** Writes a tree description into the StringBuffer using taxon numbers instead of labels or taxon names */
	private void writeTreeByNumbers(int node, StringBuffer treeDescription, boolean includeAssociated, boolean associatedUseComments) {
		if (nodeIsInternal(node)) {
			treeDescription.append('(');
			int thisSister = firstDaughterOfNode(node);
			writeTreeByNumbers(thisSister, treeDescription, includeAssociated, associatedUseComments);
			while (nodeExists(thisSister = nextSisterOfNode(thisSister))) {
				treeDescription.append(',');
				writeTreeByNumbers(thisSister, treeDescription,includeAssociated, associatedUseComments);
			}
			treeDescription.append(')');
			if (nodeHasLabel(node))
				treeDescription.append(StringUtil.tokenize(getNodeLabel(node)));
		}
		else {
			treeDescription.append(Integer.toString(Taxon.toExternal(taxonNumberOfNode(node))));
		}
		if (!branchLengthUnassigned(node)) {
			treeDescription.append(':');
			treeDescription.append(MesquiteDouble.toStringDigitsSpecified(getBranchLength(node), -1)); //add -1 to signal full accuracy 17 Dec 01
		}
		if (includeAssociated){
			String a = writeAssociated(node, associatedUseComments);
			treeDescription.append(a);
		}
	}
	/*-----------------------------------------*/
	/** Writes a tree description into the StringBuffer using taxon names in simple style: t0, t1, t2, etc. */
	private void writeTreeByT0Names(int node, StringBuffer treeDescription, boolean includeBranchLengths) {
		if (nodeIsInternal(node)) {
			treeDescription.append('(');
			int thisSister = firstDaughterOfNode(node);
			writeTreeByT0Names(thisSister, treeDescription, includeBranchLengths);
			while (nodeExists(thisSister = nextSisterOfNode(thisSister))) {
				treeDescription.append(',');
				writeTreeByT0Names(thisSister, treeDescription,includeBranchLengths);
			}
			treeDescription.append(')');
		}
		else {
			treeDescription.append("t" + taxonNumberOfNode(node));
		}
		if ( includeBranchLengths && !branchLengthUnassigned(node)) {
			treeDescription.append(':');
			treeDescription.append(MesquiteDouble.toStringDigitsSpecified(getBranchLength(node), -1)); //add -1 to signal full accuracy 17 Dec 01
		}
	}
	/*-----------------------------------------*/
	/** Writes a tree description into the StringBuffer using taxon names in simple style: t0, t1, t2, etc. */
	public String writeTreeByT0Names(boolean includeBranchLengths) {
		StringBuffer tD = new StringBuffer(10);
		writeTreeByT0Names(getRoot(), tD, includeBranchLengths);
		return tD.toString();
			
	}
	/*-----------------------------------------*/
	/** Writes a tree description into the StringBuffer using taxon names */
	private void writeTreeByNames(int node, StringBuffer treeDescription, boolean includeBranchLengths, boolean includeAssociated, boolean associatedUseComments) {
		if (nodeIsInternal(node)) {
			treeDescription.append('(');
			int thisSister = firstDaughterOfNode(node);
			writeTreeByNames(thisSister, treeDescription, includeBranchLengths, includeAssociated, associatedUseComments);
			while (nodeExists(thisSister = nextSisterOfNode(thisSister))) {
				treeDescription.append(',');
				writeTreeByNames(thisSister, treeDescription,includeBranchLengths, includeAssociated, associatedUseComments);
			}
			treeDescription.append(')');
			if (nodeHasLabel(node))
				treeDescription.append(StringUtil.tokenize(getNodeLabel(node)));
		}
		else {
			treeDescription.append(StringUtil.tokenize(taxa.getTaxonName(taxonNumberOfNode(node))));
		}
		if ( includeBranchLengths && !branchLengthUnassigned(node)) {
			treeDescription.append(':');
			treeDescription.append(MesquiteDouble.toStringDigitsSpecified(getBranchLength(node), -1)); //add -1 to signal full accuracy 17 Dec 01
		}
		if (includeAssociated){
			String a = writeAssociated(node, associatedUseComments);
			treeDescription.append(a);
		}
	}
	/*-----------------------------------------*/
	/** Writes a tree description into the StringBuffer using translation table labels if available */
	private void writeTreeByLabels(int node, StringBuffer treeDescription, boolean associatedUseComments) {
		if (nodeIsInternal(node)) {
			treeDescription.append('(');
			int thisSister = firstDaughterOfNode(node);
			writeTreeByLabels(thisSister, treeDescription, associatedUseComments);
			while (nodeExists(thisSister = nextSisterOfNode(thisSister))) {
				treeDescription.append(',');
				writeTreeByLabels(thisSister, treeDescription, associatedUseComments);
			}
			treeDescription.append(')');
			if (nodeHasLabel(node))
				treeDescription.append(StringUtil.tokenize(getNodeLabel(node)));
		}
		else {
			if (treeVector !=null){//use treeVector's translation table if one is available
				treeDescription.append(treeVector.getTranslationLabel(taxonNumberOfNode(node))); 
			}
			else
				treeDescription.append(Integer.toString(Taxon.toExternal(taxonNumberOfNode(node))));
		}
		if (!branchLengthUnassigned(node)) {
			treeDescription.append(':');
			String bL = MesquiteDouble.toStringDigitsSpecified(getBranchLength(node), -1);
			treeDescription.append(bL); //add -1 to signal full accuracy 17 Dec 01
		}
		String a = writeAssociated(node, associatedUseComments);
		treeDescription.append(a);
	}
	/*-----------------------------------------*/
	/** Outputs as string; currently returns name if the tree exists, otherwise calls super.toString() */
	public String toString(){
		if (!exists)
			return super.toString();
		return getName() + " (id " + getID() + ", version " + getVersionNumber() + ")"; //writeTree(BY_NAMES);
	}
	/*-----------------------------------------*/
	/** Returns a string describing the tree in standard parenthesis notation (Newick standard), using taxon
	numbers to refer to the taxa.*/
	public String writeTreeWithNodeNumbers() {
		StringBuffer s = new StringBuffer(numberOfNodesInClade(root)*20);
		writeTreeByNumbersWithNodeNumbers(root, s, true);
		writeTreeProperties(s, true);

		s.append(';');
		return s.toString();
	}
	/*-----------------------------------------*/
	/** Returns a string describing the tree in standard parenthesis notation (Newick standard), using taxon
	numbers to refer to the taxa.*/
	public String writeTree() {
		StringBuffer s = new StringBuffer(numberOfNodesInClade(root)*40);
		writeTreeByNumbers(root, s, true, true);
		writeTreeProperties(s, true);
		s.append(';');
		return s.toString();
	}
	/*-----------------------------------------*/
	/** Returns a simple string describing the tree in standard parenthesis notation (Newick standard), excluding associated information except branch lengths.
	To be used for output to user, not internally (as it may lose information).*/
	public String writeTreeSimpleByNames() {
		StringBuffer s = new StringBuffer(numberOfNodesInClade(root)*40);
		writeTreeByNames(root, s, true, false, false);
		//writeTreeProperties(s, true);
		s.append(';');
		return s.toString();
	}
	/*-----------------------------------------*/
	/** Returns a simple string describing the tree in standard parenthesis notation (Newick standard), excluding associated information except branch lengths.
	To be used for output to user, not internally (as it may lose information).*/
	public String writeTreeSimpleByNumbers() {
		StringBuffer s = new StringBuffer(numberOfNodesInClade(root)*40);
		writeTreeByNumbers(root, s, false, false);
		//writeTreeProperties(s, true);
		s.append(';');
		return s.toString();
	}
	/*-----------------------------------------*/
	/** Writes a tree description into the StringBuffer using taxon names */
	private void writeTreeWithNamer(int node, StringBuffer treeDescription, TaxonNamer namer, boolean includeBranchLengths, boolean includeAssociated, boolean associatedUseComments) {
		if (nodeIsInternal(node)) {
			treeDescription.append('(');
			int thisSister = firstDaughterOfNode(node);
			writeTreeWithNamer(thisSister, treeDescription, namer, includeBranchLengths, includeAssociated, associatedUseComments);
			while (nodeExists(thisSister = nextSisterOfNode(thisSister))) {
				treeDescription.append(',');
				writeTreeWithNamer(thisSister, treeDescription,  namer, includeBranchLengths, includeAssociated, associatedUseComments);
			}
			treeDescription.append(')');
			if (nodeHasLabel(node))
				treeDescription.append(StringUtil.tokenize(getNodeLabel(node)));
		}
		else {
			treeDescription.append(StringUtil.tokenize(namer.getNameToUse(taxa, taxonNumberOfNode(node))));
		}
		if ( includeBranchLengths && !branchLengthUnassigned(node)) {
			treeDescription.append(':');
			treeDescription.append(MesquiteDouble.toStringDigitsSpecified(getBranchLength(node), -1)); //add -1 to signal full accuracy 17 Dec 01
		}
		if (includeAssociated){
			String a = writeAssociated(node, associatedUseComments);
			treeDescription.append(a);
		}
	}
	/*-----------------------------------------*/
	/** Returns a Newick tree with taxon names supplied by TaxonNamer, e.g. so that modules may use their own codes for taxa.*/
	public String writeTreeWithNamer(TaxonNamer namer, boolean includeBranchLengths, boolean includeAssocAndProperties) {
		StringBuffer s = new StringBuffer(numberOfNodesInClade(root)*40);
		writeTreeWithNamer(root, s, namer, includeBranchLengths, includeAssocAndProperties, true);
		if (includeAssocAndProperties)
			writeTreeProperties(s, true);
		s.append(';');
		return s.toString();
	}
	/*-----------------------------------------*/
	/** Returns a string describing the tree in standard parenthesis notation (Newick standard), using taxon
	names or numbers to refer to the taxa, depending on the boolean parameter byNames.*/
	public String writeTree(int byWhat) {
		return writeTree(byWhat, true);
	}
	/*-----------------------------------------*/
	/** Returns a string describing the clade in standard parenthesis notation (Newick standard), using taxon
	names or numbers to refer to the taxa, depending on the boolean parameter byNames.*/
	public String writeClade(int node, int byWhat, boolean associatedUseComments) {
		StringBuffer s = new StringBuffer(numberOfNodesInClade(root)*40);
		if (byWhat == BY_NAMES)
			writeTreeByNames(node, s, true, true, associatedUseComments);
		else if (byWhat == BY_NUMBERS)
			writeTreeByNumbers(node, s, true, associatedUseComments);
		else if (byWhat == BY_TABLE)
			writeTreeByLabels(node, s, associatedUseComments);
		writeTreeProperties(s, associatedUseComments);
		s.append(';');
		return s.toString();
	}
	/*-----------------------------------------*/
	/** Returns a string describing the tree in standard parenthesis notation (Newick standard), using taxon
	names or numbers to refer to the taxa, depending on the boolean parameter byNames.*/
	public String writeTree(int byWhat, boolean associatedUseComments) {
		return writeClade(root, byWhat, associatedUseComments);
	}
	/*-----------------------------------------*/
	private void writeTreeProperties(StringBuffer sb, boolean useComments){
		String spref = "";
		if (StringUtil.blank(writeAssociated(root, useComments))){ //if nothing at root put dummy so second will be tree properties
			if (useComments)
				spref = "[% ]"; 
			else
				spref = "<>";
		}
		MesquiteBoolean mb = null;
		MesquiteBoolean mr = null;
		if (!getRooted()) {
			mr = new MesquiteBoolean(!getRooted());
			mr.setName("unrooted");
			attachIfUniqueName(mr);
		}
		if (polytomiesHard == 0 || polytomiesHard == 1) {
			mb = new MesquiteBoolean(polytomiesHard==0);
			mb.setName("polytomiesHard");
			attachIfUniqueName(mb);
		}
		String s = writeAttachments(useComments);
		String sprefpref = "";
		if (StringUtil.blank(s)){
			if (useComments)
				sprefpref = "[% ]"; 
			else
				sprefpref = "<>";
		}
		else 
			sprefpref = s;
		if (mb !=null) {
			detach(mb);
		}
		if (mr !=null) {
			detach(mr);
		}
		if (s ==null || s.length()>0) {
			sb.append(spref + " " + s);
			spref = "";
		}
		String b = writeAssociatedBetweenness();
		if (!StringUtil.blank(b)){
			sb.append(spref + " " + sprefpref + " ");
			if (useComments)
				sb.append("[% " + b + " ]"); 
			else
				sb.append("< " + b + " >"); 
		}

	}
	/*-----------------------------------------*/
	private void readAttachedProperties(String TreeDescription, MesquiteInteger stringLoc){
		readAttachments(TreeDescription, stringLoc);
		Object or = getAttachment("unrooted");
		Object ob = getAttachment("polytomiesHard");

		MesquiteBoolean mr = null;
		MesquiteBoolean mb = null;
		if (or instanceof MesquiteBoolean)
			mr = (MesquiteBoolean)or;
		if (ob instanceof MesquiteBoolean)
			mb = (MesquiteBoolean)ob;
		if (mr !=null){
			setRooted(!mr.getValue(), false);
			detach(mr);
		}
		if (mb !=null){
			if (mb.getValue())
				setPolytomiesAssumption(0, false);
			else
				setPolytomiesAssumption(1, false);
			detach(mb);
		}
	}
	public void readExtras(String assocString, MesquiteInteger pos){
		//assumes already past "<";
		String key=ParseUtil.getToken(assocString, pos);
		while (!">".equals(key)) {
			if (StringUtil.blank(key))
				return;
			String tok = ParseUtil.getToken(assocString, pos); //eating up equals
			int oldPos = pos.getValue();
			String value = ParseUtil.getToken(assocString, pos); //finding value
			if (StringUtil.blank(value))
				return;
			if (key.equalsIgnoreCase("setBetweenBits")) {
				NameReference nRef = NameReference.getNameReference(value);
				Bits b = getWhichAssociatedBits(nRef);
				if (b != null)
					b.setBetweenness(true);
			}
			else if (key.equalsIgnoreCase("setBetweenLong")) {
				NameReference nRef = NameReference.getNameReference(value);
				LongArray b = getWhichAssociatedLong(nRef);
				if (b != null)
					b.setBetweenness(true);
			}
			else if (key.equalsIgnoreCase("setBetweenDouble")) {
				NameReference nRef = NameReference.getNameReference(value);
				DoubleArray b = getWhichAssociatedDouble(nRef);
				if (b != null)
					b.setBetweenness(true);
			}
			else if (key.equalsIgnoreCase("setBetweenObject")) {
				NameReference nRef = NameReference.getNameReference(value);
				ObjectArray b = getWhichAssociatedObject(nRef);
				if (b != null)
					b.setBetweenness(true);
			}
			key=ParseUtil.getToken(assocString, pos);
			if (",".equals(key)) //eating up "," separating subcommands
				key=ParseUtil.getToken(assocString, pos);
		}
	}
	/*-----------------------------------------*/
	/** Branches a terminal node off taxon number taxonNum, and assigns it taxon newNumber.  If newNumber < 0, then assign next available taxon not in tree  */
	public synchronized void splitTerminal(int taxonNum, int newNumber, boolean notify) {
		int node = nodeOfTaxonNumber(taxonNum);
		if (nodeExists(node)) {
			int N1= sproutDaughter(node, notify);
			setTaxonNumber(N1, taxonNum, false);
			int N2= sproutDaughter(node, notify);
			if (newNumber<0){
				for (int it = 0; it< taxa.getNumTaxa(); it++){
					if (!taxonInTree(it)){  // found taxon not in tree
						newNumber = it;
						break;
					}
				}
			}
			setTaxonNumber(N2, newNumber, false);
			setTaxonNumber(node, -1, false);
			incrementVersion(MesquiteListener.BRANCHES_REARRANGED,notify);
		}

	}
	/*-----------------------------------------*/
	/** Sprouts a new daughter from node and returns it. */
	public synchronized int sproutDaughter(int node, boolean notify) {
		if (!nodeExists(node))
			return 0;
		int op = openNode();
		if (op == 0)
			return 0;
		mother[op] = node;

		if (!nodeExists(firstDaughterOfNode(node)))
			firstDaughter[node] = op;
		else
			nextSister[lastDaughterOfNode(node)] = op;
		if (branchLength!=null)
			branchLength[op] = MesquiteDouble.unassigned; //add 1. 1 just in case
		incrementVersion(MesquiteListener.BRANCHES_REARRANGED,notify);
		return op;
	}

	/*-----------------------------------------*/
	//VIRTUAL DELETION. See also LEGAL system as an alternative
	/** Marks taxon (and any nodes required by it) as deleted virtually in the boolean array.  Used in conjunction with subsequent
	 * traversals that ignore the deleted area, e.g. for ignoring taxa with missing data.*/
	public void virtualDeleteTaxon(int it, boolean[] deleted){
		if (deleted == null)
			return;
		int node = nodeOfTaxonNumber(it);
		int mother = motherOfNode(node);
		if (!nodeExists(node) || node >= deleted.length || mother >= deleted.length)
			return;
		deleted[node] = true;
		virtualDeleteReviewClades(getRoot(), deleted);
		virtualDeleteReviewAttachPoints(getRoot(), deleted);

	}
	/*-----------------------------------------*/
	/* Ensures that virtual deletion doesn't leave orphaned internal nodes*/
	private void virtualDeleteReviewClades(int node, boolean[] deleted){
		boolean allDescDeleted = true;
		if (nodeIsTerminal(node))
			return;
		if (node < deleted.length)
			deleted[node] = false;
		for (int d = firstDaughterOfNode(node); nodeExists(d); d = nextSisterOfNode(d)){
			virtualDeleteReviewClades(d, deleted);
			if (d < deleted.length && !deleted[d])
				allDescDeleted = false;
		}
		if (allDescDeleted && node < deleted.length)
			deleted[node] = true;

	}
	/*-----------------------------------------*/
	/* Ensures that virtual deletion doesn't leave orphaned internal nodes*/
	private void virtualDeleteReviewAttachPoints(int node, boolean[] deleted){
		if (nodeIsTerminal(node))
			return;
		int numNotDeleted = 0;
		for (int d = firstDaughterOfNode(node); nodeExists(d); d = nextSisterOfNode(d)){
			if (d < deleted.length && !deleted[d])
				numNotDeleted++;
			virtualDeleteReviewAttachPoints(d, deleted);
		}
		if (numNotDeleted<2)
			deleted[node] = true;

	}
	/*-----------------------------------------*/
	/** Writes a tree description into the StringBuffer, filtering virtually deleted nodes */
	public void writeTree(int node, StringBuffer treeDescription, boolean[] deleted) {
		if (nodeIsInternal(node)) {
			treeDescription.append('(');
			int thisSister = firstDaughterOfNode(node);
			writeTree(thisSister, treeDescription, deleted);
			while (nodeExists(thisSister = nextSisterOfNode(thisSister))) {
				treeDescription.append(',');
				writeTree(thisSister, treeDescription, deleted);
			}
			treeDescription.append(')');
		}
		else {
			treeDescription.append(Integer.toString(Taxon.toExternal(taxonNumberOfNode(node))));
		}
		if (deleted != null && node < deleted.length && deleted[node])
			treeDescription.append("*");
		//if (!branchLengthUnassigned(node, deleted)) {
		treeDescription.append(':');
		treeDescription.append(MesquiteDouble.toStringDigitsSpecified(getBranchLength(node, 1.0, deleted), -1)); //add -1 to signal full accuracy 17 Dec 01
		//}
	}
	/*-----------------------------------------*/
	/** Returns the root of the tree (i.e., the most recent common ancestor of the terminal taxa in the tree.*/
	public int getRoot(boolean[] deleted) {
		if (deleted == null)
			return root;
		return findFirstNonDeleted(root, -1, deleted);
	}
	/*-----------------------------------------*/
	/** Returns whether root is a real node, considering virtually deleted nodes.*/
	public boolean rootIsReal(boolean[] deleted) {
		if (deleted == null)
			return rootIsReal();
		if (root < deleted.length && deleted[root])
			return true;
		return rootIsReal();
	}
	/*-----------------------------------------*/
	/** Returns the first (left-most) daughter of node filtering virtuallyDeleted nodes.*/
	public  int firstDaughterOfNode(int node, boolean[] deleted) {
		if (deleted == null)
			return firstDaughterOfNode(node);
		if (!inBounds(node) || node >= deleted.length || deleted[node])
			return 0;
		return findFirstNonDeleted(node, node, deleted);
		/*int d = node;
		while ((d = firstDaughter[d])!= 0){
			 if (d < deleted.length && !deleted[d])
				return d;
		}
		return 0;*/
	}
	/*-----------------------------------------*/
	/** Returns the right-most daughter of node, filtering virtually deleted nodes.*/
	public int lastDaughterOfNode(int node, boolean[] deleted) {
		if (deleted == null)
			return lastDaughterOfNode(node);
		if (!inBounds(node))
			return 0;
		int thisSister = firstDaughterOfNode(node, deleted);
		while (nodeExists(nextSisterOfNode(thisSister, deleted)))
			thisSister = nextSisterOfNode(thisSister, deleted);
		return thisSister;
	}
	/*-----------------------------------------*/
	/* finding first non deleted in traversal*/
	private int findFirstNonDeleted(int node, int exceptThisNode, boolean[] deleted){
		if (node != exceptThisNode && node < deleted.length && !deleted[node])
			return node;
		for (int d = firstDaughterOfNode(node); nodeExists(d); d = nextSisterOfNode(d)){
			int n = findFirstNonDeleted(d, exceptThisNode, deleted);
			if (n != 0)
				return n;
		}
		return 0;
	}
	/*-----------------------------------------*/
	/** Returns the node's sister immediately to the right, filtering virtually deleted nodes.*/
	public  int nextSisterOfNode(int node, boolean[] deleted) {
		if (deleted == null)
			return nextSisterOfNode(node);
		if (!inBounds(node) || node >= deleted.length || deleted[node])
			return 0;
		int mn = node;
		while (mn!=0){ 
			int d = mn;

			while ((d = nextSisterOfNode(d))!= 0){
				int n = findFirstNonDeleted(d, -1, deleted);
				if (n != 0)
					return n; 
			}
			//unsuccessful; go to next level of cousin
			mn = motherOfNode(mn);
			if (mn< deleted.length && !deleted[mn])//mn exists, thus ran out of chances
				return 0;
		}
		return 0;
	}
	/*-----------------------------------------*/
	/** Returns the node's sister immediately to the left, filtering virtually deleted nodes.*/
	public int previousSisterOfNode(int node, boolean[] deleted) {
		if (deleted == null)
			return previousSisterOfNode(node);
		if (!inBounds(node))
			return 0;
		int thisSister = firstDaughterOfNode(motherOfNode(node, deleted),  deleted);
		if (node==thisSister)
			return 0;
		int lastSister;
		while (nodeExists(thisSister)) {
			lastSister=thisSister;
			thisSister = nextSisterOfNode(thisSister,  deleted);
			if (thisSister==node)
				return lastSister;
		}
		return 0;
	}
	/*-----------------------------------------*/
	/** Returns the immediate ancestor of node (mother) if node is non-reticulate, filtering virtually deleted nodes*/
	public  int motherOfNode(int node, boolean[] deleted) {
		if (deleted == null)
			return motherOfNode(node);
		if (!inBounds(node) || node >= deleted.length || deleted[node])
			return 0;
		int d = node;
		while ((d = mother[d])!= 0){
			if (d == getRoot(deleted))
				return d;
			if (d < deleted.length && !deleted[d])
				return d;
		}
		return 0;
	}
	/*-----------------------------------------*/
	/** Returns the branch length of the node, filtered by virtual deletion of nodes.*/
	public  double getBranchLength(int node, boolean[] deleted) {
		return getBranchLength(node, MesquiteDouble.unassigned, deleted);
	}
	/*-----------------------------------------*/
	/** Returns the branch length of the node.  If the branch length is unassigned, pass back the double passed in, filtered by virtual deletion of nodes*/
	public  double getBranchLength(int node, double ifUnassigned, boolean[] deleted) {
		if (deleted == null)
			return getBranchLength(node, ifUnassigned);
		if (!inBounds(node) || node >= deleted.length || deleted[node])
			return ifUnassigned;
		if (branchLength==null)
			return ifUnassigned;
		int d = node;
		double sum = MesquiteDouble.unassigned;
		int mom = motherOfNode(node, deleted);
		if (mom == 0)
			return ifUnassigned;
		while ( d != mom ) {  //bug fixed June 08 by R. FitzJohn
			sum = MesquiteDouble.add(sum, branchLength[d]);
			d = mother[d];
		}
		if (MesquiteDouble.isUnassigned(sum))
			return ifUnassigned;
		else
			return sum;
	}
	/*-----------------------------------------*/
	/** Returns whether branch length of node is unassigned, filtering virtually deleted nodes.*/
	public  boolean branchLengthUnassigned(int node, boolean[] deleted) { 
		if (deleted == null)
			return branchLengthUnassigned(node);
		if (!inBounds(node))
			return true;
		double b = getBranchLength(node, deleted);
		return MesquiteDouble.isUnassigned(b);
	}
	/*-----------------------------------------*/
	private int lowestValuedTerminal(int node){
		if (nodeIsTerminal(node))
			return taxonNumberOfNode(node);
		int min = lowestValuedTerminal(firstDaughterOfNode(node));
		for (int d = nextSisterOfNode(firstDaughterOfNode(node)); nodeExists(d); d = nextSisterOfNode(d)) {
			int dT = lowestValuedTerminal(d);
			if (dT<min)
				min = dT;
		}
		return min;
	}
	private boolean firstSortsAfter(int firstNode, int secondNode, boolean leftToRight){
		int fN = numberOfTerminalsInClade(firstNode);
		int sN = numberOfTerminalsInClade(secondNode);
		if (leftToRight){
			if (fN>sN)
				return true;
			else if (sN>fN)
				return false;
			else 
				return lowestValuedTerminal(firstNode) > lowestValuedTerminal(secondNode);
		}
		else {
			if (fN<sN)
				return true;
			else if (sN<fN)
				return false;
			else 
				return lowestValuedTerminal(firstNode) < lowestValuedTerminal(secondNode);
		}
	}

	private synchronized void sortDescendants(int node, boolean leftToRight){
		if (nodeIsTerminal(node))
			return;
		for (int d = firstDaughterOfNode(node); nodeExists(d); d = nextSisterOfNode(d))
			sortDescendants(d, leftToRight);
		int numDaughters = numberOfDaughtersOfNode(node);
		for (int i=1; i<numDaughters; i++) {
			for (int j= i-1; j>=0 && firstSortsAfter(indexedDaughterOfNode(node, j), indexedDaughterOfNode(node, j+1), leftToRight); j--) {
				interchangeBranches(indexedDaughterOfNode(node, j), indexedDaughterOfNode(node, j+1), false);
			}
		}
	}
	/*-----------------------------------------*/
	/** Returns the number of a randomly chosen node.*/
	public int randomNode(RandomBetween rng, boolean allowRoot){
		int candidate = -1;
		while (!(nodeExists(candidate) && getSubRoot()!=candidate && (getRoot()!=candidate || allowRoot))) {
			candidate = rng.randomIntBetween(0, getNumNodeSpaces());
		}
		return candidate;
	}

	/*-----------------------------------------*/
	private synchronized void randomizeDescendants(int node, Random rng){
		if (nodeIsTerminal(node))
			return;
		for (int d = firstDaughterOfNode(node); nodeExists(d); d = nextSisterOfNode(d))
			randomizeDescendants(d, rng);

		int numDaughters = numberOfDaughtersOfNode(node);
		int[] order = new int[numDaughters];
		IntegerArray.fillWithRandomOrderValues(order, rng);
		for (int i=0; i<numDaughters; i++) {
			int positionOfDaughter = -1;
			for (int j=i; j<numDaughters && positionOfDaughter<0; j++) {
				if (order[j]==i) // we've found the next one, and it is at j
					positionOfDaughter=j;
			}
			if (i!=positionOfDaughter)
				interchangeBranches(indexedDaughterOfNode(node, i), indexedDaughterOfNode(node, positionOfDaughter), false);
		}
	}

	/*-----------------------------------------*/
	/** Puts clade in random arrangement.*/
	public synchronized boolean randomlyRotateDescendants(int node, Random rng, boolean notify){
		randomizeDescendants(node, rng);
		checkTreeIntegrity(getRoot());
		incrementVersion(MesquiteListener.BRANCHES_REARRANGED,notify);
		return true;
	}
	/*-----------------------------------------*/
	/** Puts clade in standard arrangement, such that smaller clades to left, 
	and if sister clades have same number of terminals, then with one with lowest numbered terminal to left.*/
	public synchronized boolean standardize(int node, boolean notify){
		return standardize(node, true, notify);
	}
	/*-----------------------------------------*/
	/** Puts clade in standard arrangement, such that smaller clades to left (or right, depending on boolean passed), 
	and if sister clades have same number of terminals, then with one with lowest numbered terminal to left (or right).*/
	public synchronized boolean standardize(int node, boolean leftToRight, boolean notify){
		sortDescendants(node, leftToRight);
		checkTreeIntegrity(root);
		incrementVersion(MesquiteListener.BRANCHES_REARRANGED,notify);
		return true;
	}
	/*-----------------------------------------*/
	/** Puts clade in standard arrangement, such that smaller clades to left (or right, depending on boolean passed), 
	and if sister clades have same number of terminals, then with one with lowest numbered terminal to left (or right).*/
	public synchronized boolean standardize(TaxaSelectionSet taxonSet, boolean notify){
		MesquiteInteger descendantBoundary = new MesquiteInteger();
		if (taxonSet==null) {
			for (int i=0; i<getNumTaxa(); i++) { // find lowest number taxon that is in the tree
				if (taxonInTree(i)) {
					int atBranch = nodeOfTaxonNumber(i);   
					reroot(atBranch, getRoot(), false);
					break;
				}
			}
		}
		else if (isConvex(taxonSet.getBits(), descendantBoundary)) {
			if (descendantBoundary.isCombinable())
				reroot(descendantBoundary.getValue(), getRoot(), false);
			else 
				for (int i=0; i<getNumTaxa(); i++) { // find lowest number taxon that is in the tree
					if (taxonInTree(i)) {
						int atBranch = nodeOfTaxonNumber(i);
						reroot(atBranch, getRoot(), false);
						break;
					}
				}
		}
		else

			for (int i=0; i<getNumTaxa(); i++) { // find lowest number taxon that is in the set
				if (taxonInTree(i) && taxonSet.isBitOn(i)) {
					int atBranch = nodeOfTaxonNumber(i);
					reroot(atBranch, getRoot(), false);
					break;
				}
			}
		return standardize(getRoot(), true, notify);
	}

	private synchronized void focalSortDescendants(int node, int focalNode, boolean leftToRight, boolean extremeAlreadySorted){
		if (nodeIsTerminal(node))
			return;
		for (int d = firstDaughterOfNode(node); nodeExists(d); d = nextSisterOfNode(d))
			focalSortDescendants(d, focalNode, leftToRight, extremeAlreadySorted);
		int numDaughters = numberOfDaughtersOfNode(node);
		int startDaughter = 1;
		int endDaughter = numDaughters;
		if (extremeAlreadySorted && descendantOf(focalNode,node)) {
			if (leftToRight)
				endDaughter--;
			else if (!leftToRight )
				startDaughter++;
		}
		for (int i=startDaughter; i<endDaughter; i++) {
			for (int j= i-1; j>=startDaughter-1&& firstSortsAfter(indexedDaughterOfNode(node, j), indexedDaughterOfNode(node, j+1), leftToRight); j--) {
				interchangeBranches(indexedDaughterOfNode(node, j), indexedDaughterOfNode(node, j+1), false);
			}
		}
	}
	/*-----------------------------------------*/
	/** Puts clade in standard arrangement, such that focalTaxon is to left (or right, depending on boolean passed). */
	public synchronized boolean focalStandardize(int focalTaxon, boolean leftToRight, boolean notify){
		int focalNode = nodeOfTaxonNumber(focalTaxon);
		int node = focalNode;
		while (node!=getSubRoot() && nodeExists(node)) {
			int motherNode = motherOfNode(node);
			if (motherNode>0)
				if (!leftToRight &&  node!= firstDaughterOfNode(motherNode))
					interchangeBranches(firstDaughterOfNode(motherNode), node, false);
				else if (leftToRight &&  node!= lastDaughterOfNode(motherNode))
					interchangeBranches(lastDaughterOfNode(motherNode), node, false);
			node = motherOfNode(node);
		}


		focalSortDescendants(root, focalNode, leftToRight, true);
		checkTreeIntegrity(root);
		incrementVersion(MesquiteListener.BRANCHES_REARRANGED,notify);
		return true;
	}
	/*-----------------------------------------*/
	/** Interchanges two branches of tree.*/
	public synchronized boolean interchangeBranches(int node, int otherNode, boolean notify) {   
		return interchangeBranches(node, otherNode, false, notify);
	}
	/*-----------------------------------------*/
	/** Interchanges two branches of tree.*/
	public synchronized boolean interchangeBranches(int node, int otherNode, boolean preserveHeights, boolean notify) {   
		if (!nodeExists(node) || !nodeExists(otherNode))
			return false;
		if (node==otherNode)
			return false;
		else if  (descendantOf(node,otherNode) || descendantOf(otherNode,node))
			return false;
		double oldNodeHeight = 0;
		double oldOtherNodeHeight = 0;
		double oldNodeMotherHeight = 0;
		double oldOtherNodeMotherHeight = 0;
		if (preserveHeights){
			oldNodeHeight = distanceToRoot(node, false, 0);
			oldOtherNodeHeight = distanceToRoot(otherNode, false, 0);
			oldNodeMotherHeight = distanceToRoot(motherOfNode(node), false, 0);
			oldOtherNodeMotherHeight = distanceToRoot(motherOfNode(otherNode), false, 0);
		}
		if (nodeIsInternal(node) || nodeIsInternal(otherNode)) {
			int nFD = firstDaughterOfNode(node);
			int mFD = firstDaughterOfNode(otherNode);

			for (int d = firstDaughterOfNode(node); nodeExists(d); d=nextSisterOfNode(d))
				mother[d]=otherNode;  // trade first all the daughters
			for (int d = firstDaughterOfNode(otherNode); nodeExists(d); d=nextSisterOfNode(d))
				mother[d]=node;  // trade first all the daughters

			firstDaughter[node]=mFD; 
			firstDaughter[otherNode]=nFD;
		}

		if (branchLength!=null) {
			double nL = branchLength[node];  // trade lengths
			branchLength[node]=branchLength[otherNode];
			branchLength[otherNode]=nL;
		}
		if (label!=null) {
			String nL = label[node];  // trade lengths
			label[node]=label[otherNode];
			label[otherNode]=nL;
		}
		exchangeAssociated(node,otherNode);
		boolean mIs = getSelected(otherNode);
		setSelected(otherNode, getSelected(node));
		setSelected(node, mIs);
		int nN = taxonNumber[node];  // trade NUMBERS
		taxonNumber[node]=taxonNumber[otherNode]; 
		taxonNumber[otherNode]=nN; 
		if (taxonNumber[otherNode]>=0)
			nodeOfTaxon[taxonNumber[otherNode]] = otherNode;
		if (taxonNumber[node]>=0)
			nodeOfTaxon[taxonNumber[node]] = node;
		if (preserveHeights){ 
			if (MesquiteDouble.isCombinable(oldNodeHeight) && MesquiteDouble.isCombinable(oldOtherNodeHeight) && MesquiteDouble.isCombinable(oldNodeMotherHeight) && MesquiteDouble.isCombinable(oldOtherNodeMotherHeight)){

				setBranchLength(otherNode, oldNodeHeight-oldOtherNodeMotherHeight, false);
				setBranchLength(node, oldOtherNodeHeight-oldNodeMotherHeight, false);

			}
		}
		checkTreeIntegrity(root);
		incrementVersion(MesquiteListener.BRANCHES_REARRANGED,notify);
		return true;
	}
	/*-----------------------------------------*/
	/** Collapses branch to yield polytomy.*/
	public synchronized boolean collapseBranch(int node, boolean notify) {   
		if (!nodeExists(node))
			return false;
		if (nodeIsInternal(node) && (node!=root)) {
			int pS = previousSisterOfNode(node);
			if (MesquiteTrunk.trackActivity) System.out.println("collapse branch");
			int sis = nextSisterOfNode(node);
			int d = firstDaughterOfNode(node);

			while (nodeExists(d)) {	// connecting all Daughters to their grandmother
				if (!branchLengthUnassigned(node)){ //added post 1. 12 to preserve ultrametricity
					if (branchLengthUnassigned(d))
						branchLength[d] = branchLength[node];
					else
						branchLength[d] += branchLength[node];
				}
				mother[d] = motherOfNode(node);
				d = nextSisterOfNode(d);

			}
			nextSister[lastDaughterOfNode(node)] = sis;
			if (nodeIsFirstDaughter(node))
				firstDaughter[motherOfNode(node)] = firstDaughterOfNode(node);
			else 
				nextSister[pS] = firstDaughterOfNode(node);
			firstDaughter[node]=0;
			nextSister[node]=0;
			mother[node]=0;
			setTaxonNumber(node, -1);
			if (branchLength!=null)
				branchLength[node] = MesquiteDouble.unassigned;
			if (label !=null)
				label[node] = null;
			incrementVersion(MesquiteListener.BRANCHES_REARRANGED,notify);
			return true;

		}
		else {
			if (MesquiteTrunk.trackActivity) System.out.println("collapse branch failed");
			return false;
		}
	}
	/*-----------------------------------------*/
	/** Collapses all internal branches within clade above node, to yield bush.*/
	private synchronized void inCollapseAllBranches(int node) {   
		int d=firstDaughterOfNode(node);
		while (nodeExists(d)) {
			int nNS = nextSisterOfNode(d);
			inCollapseAllBranches(d);
			if (nodeIsInternal(d))
				collapseBranch(d, false);
			d = nNS;
		}
	}
	/*-----------------------------------------*/
	/** Collapses all internal branches within tree BELOW node, to yield bush.*/
	private synchronized void inCollapseAllBranchesUntilNode(int node, int endNode) {   
		int d=firstDaughterOfNode(node);
		while (nodeExists(d)) {
			int nNS = nextSisterOfNode(d);
			if (d!=endNode)
				inCollapseAllBranchesUntilNode(d, endNode);
			if (nodeIsInternal(d) && d!=endNode)
				collapseBranch(d, false);
			d = nNS;
		}
	}
	/*-----------------------------------------*/
	/** Collapses all internal branches within clade above node, to yield bush.*/
	public synchronized boolean collapseAllBranches(int node, boolean below, boolean notify) {   
		if (!nodeExists(node))
			return false;
		if (below)
			inCollapseAllBranchesUntilNode(root, node);
		else
			inCollapseAllBranches(node);
		incrementVersion(MesquiteListener.BRANCHES_REARRANGED,notify);
		return true;
	}
	/*.................................................................................................................*/
	public void reshuffleTerminals(RandomBetween rng){ 

		int[] terminals = getTerminalTaxa(getRoot());
		int numTerminals =numberOfTerminalsInClade(getRoot());
		for (int fT = 0; fT < numTerminals-1; fT++) {
			int firstTerminal = terminals[fT];
			int secondTerminal = terminals[rng.randomIntBetween(fT,numTerminals-1)];

			int firstTaxonNode = nodeOfTaxonNumber(firstTerminal);
			int secondTaxonNode = nodeOfTaxonNumber(secondTerminal);
			setTaxonNumber(secondTaxonNode,firstTerminal,false);
			setTaxonNumber(firstTaxonNode,secondTerminal,false);

		}
		terminals = getTerminalTaxa(getRoot());
	}
	/*-----------------------------------------*/
	private void zeroClade(int node){
		for (int d = firstDaughterOfNode(node); nodeExists(d); d=nextSisterOfNode(d)) {
			zeroClade(d);
		}
		firstDaughter[node]=0;
		nextSister[node] = 0;
		mother[node]=0;
		setTaxonNumber(node, -1);
		if (label !=null)
			label[node] = null;
		deassignAssociated(node);
	}
	/** Excise node and clade above it from tree, zeroing information at each node in clade.*/
	public synchronized boolean deleteClade(int node, boolean notify) {   
		if (snipClade(node, notify)){
			zeroClade(node);
			return true;
		}
		return false;
	}
	/*-----------------------------------------*/
	/** Excise node and clade above it from tree but leave the clade intact, in case it is to be attached elsewhere.*/
	public synchronized boolean snipClade(int node, boolean notify) {   
		if (node==root)
			return false;
		else {  // also prohibit if will be left fewer than three??
			if (!nodeExists(node))
				return false;
			locked = true;
			//int numSnipped = numberOfTerminalsInClade(node);
			int mom = motherOfNode(node);
			if (!nodeExists(mom))
				return false;
			if (numberOfDaughtersOfNode(mom)>2) {     //easy case; just pluck out
				int sisterRight=nextSisterOfNode(node);
				if (nodeIsFirstDaughter(node)) {
					firstDaughter[mom] = sisterRight;
				}
				else {
					int sisterLeft=previousSisterOfNode(node);
					if (!nodeExists(sisterLeft))
						return false;
					nextSister[sisterLeft] = sisterRight;
				}

			}
			else {// moving sister into mother's place
				if (numberOfDaughtersOfNode(mom)==1) {     //cut down to 
					while (mom != root && numberOfDaughtersOfNode(mom) == 1 && mom != 0){
						node = mom;  //cut one deeper
						mom = motherOfNode(mom);
					}
					if (!nodeExists(mom))
						return false;
					if (mom == root && numberOfDaughtersOfNode(mom)==1){
						locked = false;
						exists = false;
						root = 0;
						incrementVersion(MesquiteListener.BRANCHES_REARRANGED,notify);
						return true;
					}
				}
				int sister;
				if (nodeIsFirstDaughter(node)) 
					sister = nextSisterOfNode(node);
				else  
					sister = previousSisterOfNode(node);
				if (!nodeExists(sister))
					return false;
				if (hasBranchLengths()) { //remember length of branches to adjust afterward
					MesquiteDouble lengthOfRemoved = new MesquiteDouble();
					if (!nodeIsPolytomous(mom)){ // && mom != root) {  post-1.12: adds also for root
						lengthOfRemoved.setValue(branchLength[mom]); //add length of mom;
						lengthOfRemoved.add(branchLength[sister]); // add length of sister of branchFrom
						setBranchLength(sister, lengthOfRemoved.getValue(), false);
					}
				}
				int neice = firstDaughterOfNode(sister);
				firstDaughter[mom] = firstDaughter[sister];
				while (nodeExists(neice)) {
					mother[neice] = mom;
					neice = nextSisterOfNode(neice);
				}

				if (branchLength!=null)
					branchLength[mom] = branchLength[sister];
				taxonNumber[mom] = taxonNumber[sister]; 
				if (taxonNumber[mom]>=0 && taxonNumber[mom]<nodeOfTaxon.length)
					nodeOfTaxon[taxonNumber[mom]] = mom;
				if (label!=null)
					label[mom] = label[sister];
				transferAssociated(sister, mom);

				deassignAssociated(sister);
				setSelected(mom, getSelected(sister));
				setSelected(sister, false);
				firstDaughter[sister]=0;
				nextSister[sister]=0;
				mother[sister]=0;
				setTaxonNumber(sister, -1);
				if (label!=null)
					label[sister] = null;
				if (branchLength!=null)
					branchLength[sister] = MesquiteDouble.unassigned;
			}
			locked = false;

			resetNodeOfTaxonNumbers();  //added Nov 2013
			incrementVersion(MesquiteListener.BRANCHES_REARRANGED,notify);

			return true;
		}
	}
	/*................................................................................................................*/
	/** Attach terminal taxon to tree along given branch.*/
	public synchronized boolean graftTaxon(int taxon, int toN, boolean notify) {   
		if (!nodeInTree(toN)) {
			MesquiteMessage.warnProgrammer("ATTEMPT TO GRAFT ONTO NODE NOT IN TREE");
			return false;
		}
		int fromN = openNode();
		if (fromN==0)
			return false;
		mother[fromN] = -1;  //done some next openNode thinks it's not open
		setTaxonNumber(fromN, taxon);
		if (label!=null)
			label[fromN] = null;
		firstDaughter[fromN] = 0;
		nextSister[fromN] = 0;
		deassignAssociated(fromN);


		boolean success = (nodeExists(graftClade(fromN, toN, notify)));


		return success;
	}
	/*-----------------------------------------*/
	/** Attach node fromN (and any clade to attached to it) along node toN.  Returns the new node created to attach fromN*/
	public synchronized int graftClade(int fromN, int toN, boolean notify) {   
		if (!nodeExists(fromN) || !nodeExists(toN))
			return 0;
		locked = true;
		int newMother = openNode();
		if (newMother==0)
			return 0;
		setTaxonNumber(newMother, -1);
		if (label!=null)
			label[newMother] = null;
		deassignAssociated(newMother);

		int toMother = motherOfNode(toN);

		if (nodeIsFirstDaughter(toN)) {
			int nextSis = nextSisterOfNode(toN);
			firstDaughter[toMother]=newMother;
			firstDaughter[newMother]=toN;
			nextSister[toN]=fromN;
			mother[newMother]=toMother;
			mother[toN]=newMother;
			mother[fromN]=newMother;
			nextSister[fromN]=0;
			nextSister[newMother]=nextSis;
		}
		else {
			int prevSister = previousSisterOfNode(toN);
			mother[newMother]=toMother;
			mother[toN]=newMother;
			mother[fromN]=newMother;
			nextSister[prevSister]=newMother;
			firstDaughter[newMother]=fromN;
			nextSister[fromN]=toN;
			nextSister[newMother]=nextSister[toN];
			nextSister[toN]=0;
		}
		if (toN==root)
			root = newMother;
		locked = false;

		incrementVersion(MesquiteListener.BRANCHES_REARRANGED,notify);
		return newMother;
	}
	/*-----------------------------------------*/
	/** Move branch so as to excise branchFrom from its current position, and attach it to branch beneath
	node branchTo.  If successful, rename as given (to ensure renamed before notified).*/
	public synchronized boolean moveBranch(int branchFrom, int branchTo, boolean notify) {
		return moveBranch(branchFrom, branchTo, notify, false, 0.0);
	}
	/*-----------------------------------------*/
	/** Move branch so as to excise branchFrom from its current position, and attach it to branch beneath
	node branchTo.  If successful, rename as given (to ensure renamed before notified).
	Preserve heights indicates that height of descendants from ancestor is preserved.
	Preserve proportion indicates how the branch lengths are distributed; if 0 then new branches are 0 length; 0.5 means the new branch is 
	half the length of the shortest of the two branches from/to.  preserveProportion is <strong>only</strong> considered when {@code preserveHeights=true};
	if {@code preserveHeights=false} the branch receiving the new branch is effectively split in half by the newly grafted branch.*/
	public synchronized boolean moveBranch(int branchFrom, int branchTo, boolean notify, boolean preserveHeights, double preserveProportion) {
		if (branchFrom==branchTo)
			return false;
		else if (!nodeExists(branchFrom) || !nodeExists(branchTo))
			return false;
		else if  (descendantOf(branchTo,branchFrom))
			return false;
		else if  (branchTo == motherOfNode(branchFrom) && !nodeIsPolytomous(branchTo))
			return false;
		else if (nodesAreSisters(branchTo, branchFrom) && (numberOfDaughtersOfNode(motherOfNode(branchFrom))==2))
			return false;
		else if (numberOfDaughtersOfNode(motherOfNode(branchFrom))==1) //TODO: NOTE that you can't move a branch with 
			return false;

		checkTreeIntegrity(root);

		double  toLength = getBranchLength(branchTo);
		double  fromLength = getBranchLength(branchFrom);

		//first, pluck out "branchFrom"
		//next, attach branchFrom clade onto branchTo
		if (snipClade(branchFrom, false)) {
			int newMother = graftClade(branchFrom, branchTo, false);
			if (hasBranchLengths() && MesquiteDouble.isCombinable(toLength)) { //remember length of branches to adjust afterward
				if (preserveHeights){
					if (preserveProportion< 0 || !MesquiteDouble.isCombinable(fromLength))
						preserveProportion = 0;
					if (preserveProportion>1)
						preserveProportion = 1;
					if (preserveProportion == 0)
						setBranchLength(newMother, 0, false);
					else {
						double min = 0;
						if (toLength > fromLength)
							min = fromLength*preserveProportion;
						else
							min = toLength*preserveProportion;

						setBranchLength(newMother, min, false);
						setBranchLength(branchTo, toLength-min, false);
						setBranchLength(branchFrom, fromLength-min, false);
					}
				}
				else {
					setBranchLength(newMother, toLength/2.0, false);
					setBranchLength(branchTo, toLength/2.0, false);
				}
			}
		}
		if (!checkTreeIntegrity(root)) {
			locked = true;
			//what to do here?  notify?
		}
		incrementVersion(MesquiteListener.BRANCHES_REARRANGED,notify);
		return true;

	}
	/*-----------------------------------------*/
	/** Inserts a new node on the branch represented by "node", and returns the number of the inserted node */
	public synchronized int insertNode(int node, boolean notify){
		if (!nodeExists(node))
			return -1;
		int mom = motherOfNode(node);
		int wD = whichDaughter(node);
		int newN= openNode();
		if (newN>0 && wD>=0) {
			int youngerSister = nextSister[node];
			if (wD==0){ //first daughter
				firstDaughter[mom] = newN;
			}
			else {
				int olderSister = indexedDaughterOfNode(mom, wD-1);
				nextSister[olderSister] = newN;
			}
			nextSister[node] = 0;
			nextSister[newN] = youngerSister;
			firstDaughter[newN]= node;
			taxonNumber[newN] = -1;  //Nov 2013
			mother[node]=newN;
			mother[newN]=mom;
			if (node==root)
				root = newN;
			incrementVersion(MesquiteListener.BRANCHES_REARRANGED,notify);
			return newN;
		}
		return -1;

	}
	/*-----------------------------------------*/
	/** Adds d to be a new daughter of node. */
	private synchronized void insertDaughter(int node, int d) {
		if (node!= 0){
			if (!nodeExists(firstDaughterOfNode(node)))
				firstDaughter[node] = d;
			else
				nextSister[lastDaughterOfNode(node)] =d;
			nextSister[d]=0;
		}
	}
	/*-----------------------------------------*/
	/** Adds d to be a new daughter of node. */
	private synchronized void flipFirstTwoDaughters(int node) {
		if (node!= 0){
			int first = firstDaughter[node];
			int second = nextSister[first];
			firstDaughter[node]=second;
			nextSister[second] =first;
			nextSister[first]=0;
		}
	}
	/*------------------- SUBTLE REROOTING OF AUXILIARY INFORMATION ----------------------*/
	/* This messy section up until floatNode is new as of 2. 72.  It permits information associated with the parts of the tree to be associated
	 * either with the nodes or branches.  This is relevant in rerooting, because branch associated information then needs to flip to another node once
	 * the branch is moved to the other side of the root.  Any associated arrays that have betweenness == true will behave as branch associated; others are node associated.
	 * Modules setting these associated can indicate they are branch associated by adding the setAsBetween boolean = true to the setAssociatedXXXXX methods or
	 * by the direct methods setAssociatedXXXXXBetweenness.  These write to the file as a special comment (see readExtras) at the end of the tree.
	 *   For old files the standard branch associated info are forced
	 * as such by checkAssociated() that looks to see if the name reference is recognized as branch associated */

	public boolean rerootAssocBetweenOnlyFloatPoly(int first, int second){
		if (first>numParts || first<0) 
			return false;
		if (second>numParts || second<0) 
			return false;
		if (bits!=null) {
			for (int i=0; i< bits.size(); i++) {
				Bits b = (Bits)bits.elementAt(i);
				if (b.isBetween())
					b.swapParts(first, second);
			}
		}
		if (longs!=null) {
			for (int i=0; i< longs.size(); i++) {
				LongArray b = (LongArray)longs.elementAt(i);
				if (b.isBetween())
					b.swapParts(first, second);
			}
		}
		if (doubles!=null)
			for (int i=0; i< doubles.size(); i++) {
				DoubleArray b = (DoubleArray)doubles.elementAt(i);
				if (b.isBetween())
					b.swapParts(first, second);
			}
		if (objects!=null)
			for (int i=0; i< objects.size(); i++) {
				ObjectArray b = (ObjectArray)objects.elementAt(i);
				if (b.isBetween())
					b.swapParts(first, second); //comments handled here
			}

		setDirty(true);
		return true;
	}
	/*-----------------------------------------*/
	public boolean rerootAssocBetweenOnlyFloatDichot(int previousFromAtNode, int sis, int targetNode){
		if (previousFromAtNode>numParts || previousFromAtNode<0) 
			return false;
		if (sis>numParts || sis<0) 
			return false;
		if (targetNode>numParts || targetNode<0) 
			return false;

		if (bits!=null) {
			for (int i=0; i< bits.size(); i++) {
				Bits b = (Bits)bits.elementAt(i);
				if (b.isBetween()) {
					boolean newValue;
					if (b.isBitOn(previousFromAtNode) || b.isBitOn(sis))
						newValue = true;
					else 
						newValue = false;
					boolean bT = b.isBitOn(targetNode);

					b.setBit(sis, newValue);
					b.setBit(previousFromAtNode, bT);
					b.setBit(targetNode, false);
				}
			}
		}
		if (longs!=null) {
			for (int i=0; i< longs.size(); i++) {
				LongArray b = (LongArray)longs.elementAt(i);
				if (b.isBetween()) {
					long newValue = MesquiteLong.unassigned;
					if (MesquiteLong.isUnassigned(b.getValue(previousFromAtNode)))
						newValue = b.getValue(sis);
					else if (MesquiteLong.isUnassigned(b.getValue(sis)))
						newValue = b.getValue(previousFromAtNode);
					else if (b.getValue(sis) == b.getValue(previousFromAtNode))
						newValue = b.getValue(sis);
					else
						newValue = MesquiteLong.unassigned;
					long bT = b.getValue(targetNode);
					b.setValue(sis, newValue);
					b.setValue(previousFromAtNode, bT);
					b.setValue(targetNode, MesquiteLong.unassigned);
				}
			}
		}
		if (doubles!=null)
			for (int i=0; i< doubles.size(); i++) {
				DoubleArray b = (DoubleArray)doubles.elementAt(i);
				if (b.isBetween()) {
					double newValue = MesquiteLong.unassigned;
					if (MesquiteDouble.isUnassigned(b.getValue(previousFromAtNode)))
						newValue = b.getValue(sis);
					else if (MesquiteDouble.isUnassigned(b.getValue(sis)))
						newValue = b.getValue(previousFromAtNode);
					else if (b.getValue(sis) == b.getValue(previousFromAtNode))
						newValue = b.getValue(sis);
					else
						newValue = MesquiteDouble.unassigned;
					double bT = b.getValue(targetNode);

					b.setValue(sis, newValue);
					b.setValue(previousFromAtNode, bT);
					b.setValue(targetNode, MesquiteDouble.unassigned);
				}
			}
		if (objects!=null)
			for (int i=0; i< objects.size(); i++) {
				ObjectArray b = (ObjectArray)objects.elementAt(i);
				if (b.isBetween()) {
					Object newValue = null;
					if (b.getValue(previousFromAtNode) == null)
						newValue = b.getValue(sis);
					else if (b.getValue(sis) == null)
						newValue = b.getValue(previousFromAtNode);
					else if (b.getValue(sis).equals(b.getValue(previousFromAtNode)))
						newValue = b.getValue(sis);
					else
						newValue = null;
					Object bT = b.getValue(targetNode);

					b.setValue(sis, newValue);
					b.setValue(previousFromAtNode, bT);
					b.setValue(targetNode, null);
				}
			}

		setDirty(true);
		return true;
	}

	/*-----------------------------------------*/
	public boolean rerootAssocBetweenOnlyCladeRoot(int oldMother, int atNode, int cladeRoot){
		if (oldMother>numParts || oldMother<0) 
			return false;
		if (atNode>numParts || atNode<0) 
			return false;
		if (cladeRoot>numParts || cladeRoot<0) 
			return false;


		if (bits!=null) {
			for (int i=0; i< bits.size(); i++) {
				Bits b = (Bits)bits.elementAt(i);
				if (b.isBetween()) {
					boolean bT = b.isBitOn(atNode);
					boolean bSpan = b.isBitOn(oldMother);

					b.setBit(oldMother, false);
					b.setBit(atNode, bSpan);

					b.setBit(cladeRoot, bT);
				}
			}
		}
		if (longs!=null) {
			for (int i=0; i< longs.size(); i++) {
				LongArray b = (LongArray)longs.elementAt(i);
				if (b.isBetween()) {
					long bT = b.getValue(atNode);
					long bSpan = b.getValue(oldMother);

					b.setValue(atNode, bSpan);
					b.setValue(oldMother, MesquiteLong.unassigned);

					b.setValue(cladeRoot, bT);
				}
			}
		}
		if (doubles!=null)
			for (int i=0; i< doubles.size(); i++) {
				DoubleArray b = (DoubleArray)doubles.elementAt(i);
				if (b.isBetween()) {
					double bT = b.getValue(atNode);
					double bSpan = b.getValue(oldMother);

					b.setValue(atNode, bSpan);
					b.setValue(oldMother, MesquiteDouble.unassigned);

					b.setValue(cladeRoot, bT);
				}
			}
		if (objects!=null)
			for (int i=0; i< objects.size(); i++) {
				ObjectArray b = (ObjectArray)objects.elementAt(i);
				if (b.isBetween()) {
					Object bT = b.getValue(atNode);
					Object bSpan = b.getValue(oldMother);

					//b.setValue(oldMother, MesquiteDouble.unassigned);
					b.setValue(atNode, bSpan);
					b.setValue(oldMother, null);
					b.setValue(cladeRoot, bT);
				}
			}

		setDirty(true);
		return true;
	}
	static final String[] betweenLongs = new String[]{"color"};
	static final String[] betweenDoubles = new String[]{"width", "consensusFrequency", "posteriorProbability"};
	static final String[] betweenObjects = new String[]{};
	static final String[] betweenBits = new String[]{};
	/*
	public void setAssociatedBit(NameReference nRef, int index, boolean value){
		setAssociatedBit(nRef, index, value, StringArray.indexOfIgnoreCase(betweenBits, nRef.getValue())>=0);
	}
	public void setAssociatedLong(NameReference nRef, int index, long value){
		setAssociatedLong(nRef, index, value, StringArray.indexOfIgnoreCase(betweenLongs, nRef.getValue())>=0);
	}
	public void setAssociatedDouble(NameReference nRef, int index, double value){
		setAssociatedDouble(nRef, index, value, StringArray.indexOfIgnoreCase(betweenDoubles, nRef.getValue())>=0);
	}
	public void setAssociatedObject(NameReference nRef, int index, Object value){
		setAssociatedObject(nRef, index, value, StringArray.indexOfIgnoreCase(betweenObjects, nRef.getValue())>=0);
	}
	 */
	private void checkAssociated(){   //for old data files that don't have betweenness recorded
		if (bits!=null) {
			for (int i=0; i< bits.size(); i++) {
				Bits b = (Bits)bits.elementAt(i);
				NameReference nRef = b.getNameReference();
				if (StringArray.indexOfIgnoreCase(betweenBits, nRef.getValue())>=0)
					b.setBetweenness(true);
			}
		}
		if (longs!=null) {
			for (int i=0; i< longs.size(); i++) {
				LongArray b = (LongArray)longs.elementAt(i);
				NameReference nRef = b.getNameReference();
				if (StringArray.indexOfIgnoreCase(betweenLongs, nRef.getValue())>=0)
					b.setBetweenness(true);
			}
		}
		if (doubles!=null)
			for (int i=0; i< doubles.size(); i++) {
				DoubleArray b = (DoubleArray)doubles.elementAt(i);
				NameReference nRef = b.getNameReference();
				if (StringArray.indexOfIgnoreCase(betweenDoubles, nRef.getValue())>=0)
					b.setBetweenness(true);
			}
		if (objects!=null)
			for (int i=0; i< objects.size(); i++) {
				ObjectArray b = (ObjectArray)objects.elementAt(i);
				NameReference nRef = b.getNameReference();
				if (StringArray.indexOfIgnoreCase(betweenObjects, nRef.getValue())>=0)
					b.setBetweenness(true);
			}


	}

	private String writeAssociatedBetweenness(){
		String s = "";
		if (bits!=null) {
			for (int i=0; i< bits.size(); i++) {
				Bits b = (Bits)bits.elementAt(i);
				if (b.isBetween())
					s += " setBetweenBits = " + StringUtil.tokenize(b.getNameReference().getValue());
			}
		}
		if (longs!=null) {
			for (int i=0; i< longs.size(); i++) {
				LongArray b = (LongArray)longs.elementAt(i);
				if (b.isBetween())
					s += " setBetweenLong = " + StringUtil.tokenize(b.getNameReference().getValue());
			}
		}
		if (doubles!=null)
			for (int i=0; i< doubles.size(); i++) {
				DoubleArray b = (DoubleArray)doubles.elementAt(i);
				if (b.isBetween())
					s += " setBetweenDouble = " + StringUtil.tokenize(b.getNameReference().getValue());
			}
		if (objects!=null)
			for (int i=0; i< objects.size(); i++) {
				ObjectArray b = (ObjectArray)objects.elementAt(i);
				if (b.isBetween())
					s += " setBetweenObject = " + StringUtil.tokenize(b.getNameReference().getValue());
			}
		return s;
	}
	/*-----------------------------------------*/
	//floatNode and reroot corrected to handle branch lengths 29 Sept 2001

	/** For rerooting.  This starts from the node chosen for the new root, and proceeds down to the clade root.
	It then unhooks the clade root and lets it float upward, temporarily making the new clade root to be the previous
	node on the path from the chosen node.  It then back up to that node, and lets it float upward, making the new clade
	root to be the previous node on the path.  And so on, letting the clade float upward piece by piece from its root
	back toward the chosen node. */
	private synchronized void floatNode(int targetNode, int previousFromAtNode, int cladeRoot){
		if (targetNode!=cladeRoot) {
			floatNode(motherOfNode(targetNode), targetNode, cladeRoot);  //go down until at the clade root
		}
		/*Either at the original clade root, or coming back up from it.  If coming back up, then previous floating will have left
		the targetNode to be the current (possibly temporary) root of the clade.  The goal here will be to snip this target
		from its mother and let it float upward relative to the previous node up the path from the chosen node*/
		//About to snip the current targe
		if (nodeIsPolytomous(targetNode)) { // target polytomous
			//branch length that had belonged to previousFromAtNode needs to be assigned to targetNode
			double bP = 0;
			double bT = 0;
			if (branchLength!=null) {
				bP = branchLength[previousFromAtNode];
				bT = branchLength[targetNode];
			}
			if (firstDaughter[targetNode]==previousFromAtNode) {
				firstDaughter[targetNode]= nextSister[previousFromAtNode];
			}
			else {
				int pSis = previousSisterOfNode(previousFromAtNode);
				nextSister[pSis]= nextSister[previousFromAtNode];
			}
			mother[targetNode]=previousFromAtNode;
			nextSister[previousFromAtNode] = 0;
			insertDaughter(previousFromAtNode, targetNode);
			if (branchLength!=null) {
				branchLength[targetNode] = bP;
				branchLength[previousFromAtNode] = bT;
			}
			rerootAssocBetweenOnlyFloatPoly(targetNode, previousFromAtNode);
		}
		else { //target dichotomous 
			int sis = nextSisterOfNode(previousFromAtNode);
			if (!nodeExists(sis))
				sis = previousSisterOfNode(previousFromAtNode);
			//branch lengths that had belonged to previousFromAtNode and sis need to be summed to be assigned to sis
			double bSum = 0;
			double bT = 0;
			if (branchLength!=null) {
				if (MesquiteDouble.isUnassigned(branchLength[previousFromAtNode]))
					bSum = branchLength[sis];
				else if (MesquiteDouble.isUnassigned(branchLength[sis]))
					bSum = branchLength[previousFromAtNode];
				else
					bSum = branchLength[previousFromAtNode] + branchLength[sis];
				bT = branchLength[targetNode];
			}
			mother[sis]=previousFromAtNode;
			nextSister[previousFromAtNode] = 0;
			insertDaughter(previousFromAtNode, sis);
			mother[cladeRoot]=0; // zeroing root so it will be open
			setTaxonNumber(cladeRoot,-1);
			if (label!=null)
				label[cladeRoot] = null;
			if (branchLength!=null) {
				branchLength[sis] = bSum;
				branchLength[previousFromAtNode] = bT;
				branchLength[targetNode] = MesquiteDouble.unassigned;
			}
			rerootAssocBetweenOnlyFloatDichot(previousFromAtNode, sis, targetNode);
		}
	}
	/*-----------------------------------------*/
	/** Returns sum of all branchLengths.*/
	private double totalBranchLength(int node) { 
		if (branchLength ==null)
			return MesquiteDouble.unassigned;
		double total = branchLength[node];
		if (nodeIsInternal(node))
			for (int daughter = firstDaughterOfNode(node); nodeExists(daughter); daughter = nextSisterOfNode(daughter))
				total = MesquiteDouble.add(total, totalBranchLength(daughter));
		return total;
	}
	/*-----------------------------------------*/
	boolean nodeIsUnbranchedDescendantOfRoot(int node){
		if (node == root)
			return true;
		int anc = motherOfNode(node);
		while (anc != root && nodeExists(anc)){
			if (!nodeIsUnbranchedInternal(anc))
				return false;
			anc = motherOfNode(anc);
		}
		if (anc == root)
			return true;
		return false;

	}

	/*-----------------------------------------*/
	void collapseRootwardUnbranched(int node){

		if (nodeIsUnbranchedInternal(node)){
			collapseRootwardUnbranched(firstDaughterOfNode(node));
		}
		if (node != root)
			collapseBranch(node, false);
	}
	/*-----------------------------------------*/
	static boolean warnedUnbranchedReroot = false;
	/** reroot the clade below node atNode.*/
	public synchronized boolean reroot(int atNode, int cladeRoot, boolean notify) {
		if (!nodeExists(atNode) || !nodeExists(cladeRoot) ||cladeRoot==atNode || (!nodeIsPolytomous(cladeRoot) && cladeRoot == motherOfNode(atNode)))
			return false;
		checkTreeIntegrity(root);
		if (hasUnbranchedInternals(root)){
			//deleteUnbranchedInternals(root, false);
			if (nodeIsUnbranchedInternal(atNode)){
				taxa.discreetAlert("Sorry, you can't reroot at an unbranched internal node.");
				return false;
			}
			if (nodeIsUnbranchedDescendantOfRoot(atNode)){
				taxa.discreetAlert("Sorry, you can't reroot at an unbranched descendant of root.");
				return false;
			}
			//delete all unbranched descendants of root
			collapseRootwardUnbranched(root);
			if (!nodeExists(atNode) || !nodeExists(cladeRoot) ||cladeRoot==atNode || (!nodeIsPolytomous(cladeRoot) && cladeRoot == motherOfNode(atNode)))
				return true;


			if (descendantOf(atNode, cladeRoot)) { 
				int anc = atNode;
				while (anc != cladeRoot && nodeExists(anc) && anc != root){
					int ta = motherOfNode(anc);
					if (nodeIsUnbranchedInternal(anc)) {
						collapseBranch(anc, false);
						if (!warnedUnbranchedReroot){
							taxa.discreetAlert("Note: Unbranched internal nodes involved in rerooting were deleted.");
							warnedUnbranchedReroot = true;
						}
					}
					anc = ta;
				}
			}
		}
		double branchLengthBefore = totalBranchLength(root);
		int oldMother = motherOfNode(atNode);
		int oldMotherOfClade = motherOfNode(cladeRoot);
		int oldPrevSisterOfRoot = previousSisterOfNode(cladeRoot);
		int oldNextSisterOfRoot = nextSisterOfNode(cladeRoot);
		boolean rootWasFirstDaughter = firstDaughter[oldMotherOfClade]==cladeRoot;
		boolean atNodeWasFirstDaughter = firstDaughter[oldMother]==atNode;

		//Do the major part of the work for the rerooting
		floatNode(motherOfNode(atNode), atNode, cladeRoot);

		//Clean up: floating will have rerooted it directly at atNode; must shift to be just below atNode
		//At new root of clade:  create new node for new root of clade
		if (cladeRoot==root) {
			//establish tree's root at new location if needed
			cladeRoot = openNode();
			root = cladeRoot;
		}
		else
			cladeRoot = openNode();
		if (cladeRoot==0)
			return false;

		double bSpan = 0;
		double bT = 0;
		if (branchLength!=null) {
			bT = branchLength[atNode];
			bSpan = branchLength[oldMother];
		}

		setTaxonNumber(cladeRoot,-1);  //set clade root to be not terminal
		if (label!=null)
			label[cladeRoot] = null;

		//had clade root previously been the first daughter of its mother?
		if (rootWasFirstDaughter)		//if so, connect this mother to its new first daughter
			firstDaughter[oldMotherOfClade]=cladeRoot; 
		else { //otherwise, connect the sister that had been the link to the clade root to its new next sister
			nextSister[oldPrevSisterOfRoot]=cladeRoot;
			nextSister[cladeRoot]=oldNextSisterOfRoot;
		}

		//shift connections so that new root of clade is just below atNode
		if (oldMother== firstDaughter[atNode]) { //if so, it must be connected as first daughter of cladeRoot
			firstDaughter[atNode]= nextSister[oldMother];
			firstDaughter[cladeRoot]=oldMother;
			insertDaughter(cladeRoot, atNode);
		}
		else {
			int pSis = previousSisterOfNode(oldMother);
			nextSister[pSis]= nextSister[oldMother];
			nextSister[oldMother] = 0;
			firstDaughter[cladeRoot]=atNode;
			insertDaughter(cladeRoot, oldMother);
			if (!atNodeWasFirstDaughter) {   //added 2. 01 to prevent odd flipping
				flipFirstTwoDaughters(cladeRoot);
			}
		}
		if (branchLength!=null) {
			branchLength[cladeRoot] = bT;
			if (MesquiteDouble.isCombinable(bSpan)){
				branchLength[oldMother] = bSpan/2.0;
				branchLength[atNode] = bSpan/2.0;
			}
		}
		rerootAssocBetweenOnlyCladeRoot(oldMother, atNode, cladeRoot);
		mother[atNode]= cladeRoot;
		mother[oldMother] = cladeRoot;
		mother[cladeRoot]=oldMotherOfClade;
		double branchLengthAfter = totalBranchLength(root);
		if (!MesquiteDouble.closeEnough(branchLengthAfter, branchLengthBefore, 0.000001))
			MesquiteMessage.warnProgrammer("Error: sum of branch lengths not preserved on reroot (before " + branchLengthBefore + " after " + branchLengthAfter + ")");
		checkTreeIntegrity(root);
		incrementVersion(MesquiteListener.BRANCHES_REARRANGED,notify);
		return true;

	}
	/*-----------------------------------------*/
	/** makes trees for all rootings of clade above node given, and places them into TreeVector*/
	public void makeAllRootings(int cladeRoot, TreeVector trees) {
		if (trees!= null && nodeExists(cladeRoot)) {
			MesquiteTree baseTree = cloneTree();
			baseTree.setName("original");
			trees.addElement(baseTree, false); //save first unrooted version
			int numReroots = baseTree.numberOfNodesInClade(cladeRoot)-1;
			int count = 1;
			for (int i = 1; i<=numReroots; i++) {
				int atNode = baseTree.nodeInTraversal(i, cladeRoot);
				if (baseTree.nodeIsPolytomous(cladeRoot) || baseTree.motherOfNode(atNode)!= cladeRoot) {
					count++;
					MesquiteTree newTree = baseTree.cloneTree();
					newTree.setName("Rooting " + count);
					newTree.reroot(newTree.nodeInTraversal(i, cladeRoot), cladeRoot, false);
					trees.addElement(newTree, false);
				}
			}
		}
	}
	/*-----------------------------------------*/
	/** Outputs to console the mother, firstDaughter and nextSister storage */
	public void dump (){
		System.out.println("------------------------------------->");
		System.out.println("tree: " +writeTree());
		System.out.println("root: " + root);
		System.out.println("numNodeSpaces: " + numNodeSpaces);
		System.out.println("nodes not indicated have label[i] == null && mother[i] == 0 && firstDaughter[i] != 0 && nextSister[i] == 0 && taxonNumber[i] == -1");
		for (int i=0; i<numNodeSpaces; i++) {
			if (label != null){
				if (label[i] != null && mother[i] != 0 || firstDaughter[i] != 0 || nextSister[i] != 0 || taxonNumber[i] != -1)
					System.out.println("node " + i + " (label "+ label[i] + " ) (mother "+ mother[i] + ") (firstDaughter "+ firstDaughter[i] + ") (nextSister " + nextSister[i] + ") (taxon " + taxonNumber[i] + ")");
			}
			else
				if (mother[i] != 0 || firstDaughter[i] != 0 || nextSister[i] != 0 || taxonNumber[i] != -1)
					System.out.println("node " + i + " (mother "+ mother[i] + ") (firstDaughter "+ firstDaughter[i] + ") (nextSister " + nextSister[i] + ") (taxon " + taxonNumber[i] + ")");

		}
		System.out.println("<-------------------------------------");
	}
	/*-----------------------------------------*/
	/** Returns a string with error message concerning the tree's integrity.  Does it recursively and hence may suffer when integrity damaged. (see dump as alternative) */
	private String outputcheckTreeIntegrity(int node) {
		String s = "[Error in tree "  + getName() + " (id " + getID() + ", version " + getVersionNumber() + ")";
		if (nodeIsTerminal(node) && (taxonNumberOfNode(node)<0)) 
			s += ":  terminal node with number 0;  node = " + Integer.toString(node) + " (taxa: " + taxa.getName()+ ")]";
		if (!nodeIsTerminal(node) && (taxonNumberOfNode(node)>=0)) 
			s += ":  internal node with number non zero;  node = " + Integer.toString(node) + " (taxa: " + taxa.getName()+ ")]";
		if (!daughterOf(node, motherOfNode(node))) 
			s += ":  node is not mother's daughter;  node = " + Integer.toString(node) + " (taxa: " + taxa.getName()+ ")]";
		if (node!=root)
			if (!daughterOf(motherOfNode(node), motherOfNode(motherOfNode(node))))
				s += ":  mother is not grandmother's daughter;  node = " + Integer.toString(node) + " (taxa: " + taxa.getName()+ ")]";
		int thisSister = firstDaughterOfNode(node);

		if (nodeIsTerminal(node)) 
			return Integer.toString(taxonNumberOfNode(node));
		else {
			s += "(";
			boolean first = true;
			while (nodeExists(thisSister)) {
				if (!first)
					s += ",";
				first = false;
				s += outputcheckTreeIntegrity(thisSister);
				thisSister = nextSisterOfNode(thisSister);
			}
			s += ")";
			return s;
		}
	}
	/*-----------------------------------------*/
	/** Checks the tree integrity and outputs errors to system console if there are problems */
	private void recursecheckTreeIntegrity(int node) {
		if (nodeIsTerminal(node) && (taxonNumberOfNode(node)<0)) {
			System.out.println("*Error in tree " + getName() + ":  terminal node with number 0; node= " + Integer.toString(node) + " (taxa: " + taxa.getName()+ ")]");
			dump();
			System.out.println(outputcheckTreeIntegrity(root));
		}
		if (!nodeIsTerminal(node) && (taxonNumberOfNode(node)>=0)) {
			System.out.println("*Error in tree " + getName() + ":  internal node with number non zero; node= " + Integer.toString(node) + " (taxa: " + taxa.getName()+ ")]");
			dump();
			System.out.println(outputcheckTreeIntegrity(root));
		}
		if (!daughterOf(node, motherOfNode(node))) {
			System.out.println("*Error in tree " + getName() + ":  node is not mother's daughter; node= " + Integer.toString(node) + " (taxa: " + taxa.getName()+ ")]");
			dump();
			System.out.println(outputcheckTreeIntegrity(root));
		}
		if (node!=root)
			if (!daughterOf(motherOfNode(node), motherOfNode(motherOfNode(node)))) {
				System.out.println("*Error in tree " + getName() + ":  mother is not grandmother's daughter; node= " + Integer.toString(node) + " (taxa: " + taxa.getName()+ ")]");
				dump();
				System.out.println(outputcheckTreeIntegrity(root));
			}

		int thisSister = firstDaughterOfNode(node);

		while (nodeExists(thisSister)) {
			recursecheckTreeIntegrity(thisSister);
			thisSister = nextSisterOfNode(thisSister);
		}
	}
	/*-----------------------------------------*/
	/** Outputs warning */
	private boolean doWarn(String t) {
		MesquiteTrunk.mesquiteTrunk.logln(t);
		return false;
	}
	/*-----------------------------------------*/
	/** Checks integrity of tree and outputs messages for any errors found. */
	private boolean checkTreeIntegrity(int node) {
		if (!checkIntegrity)
			return true;
		checkTaxaIDs();
		boolean toReturn=true;

		for (int i=1; i<numNodeSpaces; i++) {
			if (motherOfNode(i)!=0) {
				if (motherOfNode(i)==firstDaughterOfNode(i))
					toReturn = doWarn("node " + Integer.toString(i) + " mother=first ");
				if (motherOfNode(i)==nextSisterOfNode(i))
					toReturn = doWarn("node " + Integer.toString(i) + " mother=next ");
				if (motherOfNode(i)==i)
					toReturn = doWarn("node " + Integer.toString(i) + " mother=node ");
				if (firstDaughterOfNode(i)==i)
					toReturn = doWarn("node " + Integer.toString(i) + " first=node ");
				if (nextSisterOfNode(i)==i)
					toReturn = doWarn("node " + Integer.toString(i) + " next=node ");
			}
		}

		/**/
		recursecheckTreeIntegrity(root);
		if (false){ //This checking has never or rarely generated an error, and is expensive in time
			int[] countsReferencesAsMother = new int[numNodeSpaces];
			int[] countsReferencesAsDaughter = new int[numNodeSpaces];
			for (int i=1; i<numNodeSpaces; i++) {
				countsReferencesAsMother[i]=0;
				countsReferencesAsDaughter[i]=0;
			}
			for (int i=1; i<numNodeSpaces; i++) {
				if (nodeInTree(i)) {
					if (inBounds(mother[i]))
						countsReferencesAsMother[mother[i]]++;
					if (inBounds(firstDaughter[i]))
						countsReferencesAsDaughter[firstDaughter[i]]++;
					if (inBounds(nextSister[i]))
						countsReferencesAsDaughter[nextSister[i]]++;
				}
			}
			for (int i=2; i<numNodeSpaces; i++) {
				if (nodeInTree(i) && countsReferencesAsDaughter[i]>1)
					toReturn = doWarn("node " + Integer.toString(i) + " claimed as daughter more than once ");
			}
			for (int i=1; i<numNodeSpaces; i++) {
				if (nodeInTree(i) && countsReferencesAsMother[i]!=numberOfDaughtersOfNode(i))
					toReturn = doWarn("node " + Integer.toString(i) + " claimed as mother not same as Daughter count ");
			}
		}
		return toReturn;
	}
	/*
	static boolean noMoreWarnRet = false;
	public void warnRetIfNeeded(){
		if (noMoreWarnRet || !warnReticulations)
			return;
		if (hasReticulations()){
			String w = "Tree has reticulations.  This may be unintentional, resulting from internal nodes with duplicate names; to avoid this request Convert Internal Names in the Defaults menu.  This warning can be turned off in the Defaults menu.";
			if (MesquiteThread.isScripting()) {
				MesquiteTrunk.mesquiteTrunk.logln(w);
			}
			else if (!AlertDialog.query(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Reticulations!", w, "OK", "Don't warn again"))
				noMoreWarnRet = true;

		}
	}
	/*-----------------------------------------*/
	/** Finds the highest taxon number in the given clade */
	private int findHighestTaxonNumber (int node) {
		if (!inBounds(node))
			return -1;
		if (nodeIsTerminal(node))
			return taxonNumberOfNode(node);
		int maximum = -1;
		for (int daughter=firstDaughterOfNode(node); nodeExists(daughter); daughter = nextSisterOfNode(daughter) ) {
			int highestInDaughter = findHighestTaxonNumber(daughter);
			if (highestInDaughter>maximum)
				maximum = highestInDaughter;
		}
		return maximum;
	}
	/*-----------------------------------------*/
	/** Finds the node of the highest taxon number in the given clade */
	private int findTerminalOfHighestTaxonNumber (int node) {
		return nodeOfTaxonNumber(findHighestTaxonNumber(node));
	}
	/*-----------------------------------------*/
	/** Finds the lowest taxon number in the given clade */
	private int findLowestTaxonNumber (int node) {
		if (!inBounds(node))
			return -1;
		if (nodeIsTerminal(node))
			return taxonNumberOfNode(node);
		int minimum = getNumTaxa();
		for (int daughter=firstDaughterOfNode(node); nodeExists(daughter); daughter = nextSisterOfNode(daughter) ) {
			int lowestInDaughter = findLowestTaxonNumber(daughter);
			if (lowestInDaughter<minimum)
				minimum = lowestInDaughter;
		}
		return minimum;
	}
	/*-----------------------------------------*/
	/** Finds the node of the lowest taxon number in the given clade */
	private int findTerminalOfLowestTaxonNumber (int node) {
		return nodeOfTaxonNumber(findLowestTaxonNumber(node));
	}
	/*-----------------------------------------*/
	/** Places into the passed array (which must have been sized correctly in advance) the taxon numbers in the clade */
	public void accumulateTerminals (int node, int[] terminals, MesquiteInteger next) {
		if (!inBounds(node))
			return;
		if (terminals==null || next==null || next.getValue()>= terminals.length)
			return;
		if (nodeIsTerminal(node))  {
			terminals[next.getValue()] = taxonNumberOfNode(node);
			next.increment();
		}
		else
			for (int daughter=firstDaughterOfNode(node); nodeExists(daughter); daughter = nextSisterOfNode(daughter) )
				accumulateTerminals(daughter, terminals, next);
	}
	/*-----------------------------------------*/
	/** For enumerating all trees: returns whether the clade is set to the last form in the sequence *
   	private boolean cladeIsLastInSequence(int node){
   		if (nodeIsTerminal(node))
   			return true;
   		else {
	   		int leftDaughter = firstDaughterOfNode(node);
	   		int rightDaughter =nextSisterOfNode( firstDaughterOfNode(node));
	   		if (nodeIsTerminal(leftDaughter)) {
	   			return (findTerminalOfHighestTaxonNumber(node) == leftDaughter)&&cladeIsLastInSequence(rightDaughter);
	   		}
   			return (nodeIsTerminal(leftDaughter) && (findTerminalOfHighestTaxonNumber(node) == leftDaughter) &&cladeIsLastInSequence(rightDaughter));  //todo: leftDaughter also needs to have some value
   		}
   	}
	/*-----------------------------------------*/
	/* First in sequence is left ladder with terminal taxa in order from lowest numbered to highest numbered*
   	private void setCladeToFirstInSequence(int node){
		if (!inBounds(node))
			return;
   		if (nodeIsTerminal(node))
   			return;
   		if (nodeIsPolytomous(node))
   			MesquiteMessage.warnProgrammer("Oops -- setCladeToFirstInSequence doesn't yet handle polytomies");//TODO: needs to change polytomies to 
   		int leftDaughter = firstDaughterOfNode(node);
   		int rightDaughter =nextSisterOfNode(leftDaughter);
   		if (nodeIsTerminal(rightDaughter) && nodeIsTerminal(leftDaughter)) {
   			if (taxonNumberOfNode(leftDaughter)>taxonNumberOfNode(rightDaughter))
   				interchangeBranches(rightDaughter, leftDaughter, false);
   		}
   		else {
    			//find lowest valued terminal and move it to be sister to rest
   			int nodeOfLowestTaxon = findTerminalOfLowestTaxonNumber(node);
   			int base = node;
   			if (nodeOfLowestTaxon == rightDaughter)
   				interchangeBranches(rightDaughter, leftDaughter, false);
   			else {
	   			moveBranch(nodeOfLowestTaxon, node, false);
	   			base = motherOfNode(node);
	   			int lef = firstDaughterOfNode(base);
	   			if (nodeOfLowestTaxon == nextSisterOfNode(lef))
   					interchangeBranches(nodeOfLowestTaxon, lef, false);
	   		}
   			int ontoNode = firstDaughterOfNode(base);
   			//now go through terminals on right side, moving the lowest onto left side one by one
   			while (nodeIsInternal(rightDaughter =nextSisterOfNode(firstDaughterOfNode(base)))) {
   				nodeOfLowestTaxon = findTerminalOfLowestTaxonNumber(rightDaughter);
	   			moveBranch(nodeOfLowestTaxon, ontoNode, false);
	   			ontoNode = firstDaughterOfNode(base);
   			}

   		}	
   	}
	/*-----------------------------------------*/
	/** Represent s as bitfield (actually a simple long) with bit cleared where element is in s but not in subS, bit set where in subS *
   	private long representSubset(int[] s, int[] subS){
   		if (s== null || subS==null)
   			return 0L;
   		long result=0L;
   		for(int i=0; i<subS.length; i++){
   			int loc = IntegerArray.indexOf(s, subS[i]);
   			if (loc>=0)
   				result = MesquiteSet.addToSet(result, loc);
   		}
   		return result;
   	}
	/*-----------------------------------------*/
	/** Sets this the clade to have the next form in the sequence of all possible topologies (e.g., for exhaustive searches). *
   	private boolean setCladeToNextInSequence(int node, int whoseCalling){ 
   		if (nodeIsTerminal(node)) {
   			return false;
   		}
   		else if (nodeIsPolytomous(node))
   			setCladeToFirstInSequence(node);
   		int leftDaughter = firstDaughterOfNode(node);
   		int rightDaughter =nextSisterOfNode( firstDaughterOfNode(node));
   		if (!setCladeToNextInSequence(rightDaughter, ++whoseCalling)) {
   			if (setCladeToNextInSequence(leftDaughter, ++whoseCalling)) {
   				setCladeToFirstInSequence(rightDaughter);
   				return true;
   			}
   			else {
   				/*left and right are individually last and so go to next partition into left and right
   				First, check to see if this is last partition with this left-right balance.  If not, then
   				interchange taxa to make it the next partition with this left-right balance. If last, then
   				go to the next left-right balance as long as the current balance is not even or n-(n-1)*
   				int numOnLeft = numberOfTerminalsInClade(leftDaughter);
   				int numOnRight = numberOfTerminalsInClade(rightDaughter);
   				if (numOnLeft==numOnRight){ //even balance
	    				/*This is last partition with this left-right balance for even balances (e.g. 2-2 or 3-3)
	   				if all of the highest number taxa are on the left (they started on the right)*
	   				if (numOnLeft==1) {
	   					return false;
	   				}	
	   				else {
						int[] allTaxa = new int[numOnLeft+numOnRight];
						int[] leftTaxa = new int[numOnLeft];
						int[] rightTaxa = new int[numOnRight];
						MesquiteInteger next = new MesquiteInteger(0);
						accumulateTerminals(leftDaughter, leftTaxa, next);
						next.setValue(0);
						accumulateTerminals(rightDaughter, rightTaxa, next);
						next.setValue(0);
						accumulateTerminals(node, allTaxa, next);
						IntegerArray.sort(leftTaxa);
						IntegerArray.sort(rightTaxa);
						IntegerArray.sort(allTaxa);
	   					long thisSet = representSubset(allTaxa, leftTaxa); // represent allTaxa as bitfield with 0's where taxon is in right, 1 where in left
	   					boolean done=false;
	   					int times = 0;
						while (!done){
	   						long nextSet = MesquiteSet.nextSet(thisSet, allTaxa.length-1);
	   						times++;
	   						if (nextSet==0L) {
	   							return times<=1;
	   						}
	   						else {
	   							long changedTo0 = (thisSet ^ nextSet) & (~nextSet);
	   							long changedTo1 = (thisSet ^ nextSet) & (~thisSet);
	   							while (changedTo0!=0L && changedTo1!=0L){
			   						int elementChangedTo0 = MesquiteSet.minimum(changedTo0);
			   						int elementChangedTo1 = MesquiteSet.minimum(changedTo1);
			   						int whichTaxonMovedLeftToRight = nodeOfTaxonNumber(allTaxa[elementChangedTo0]);
			   						int whichTaxonMovedRightToLeft = nodeOfTaxonNumber(allTaxa[elementChangedTo1]);
			   						interchangeBranches(whichTaxonMovedLeftToRight,whichTaxonMovedRightToLeft,false);
			   						changedTo0 = MesquiteSet.clearFromSet(changedTo0, elementChangedTo0);
			   						changedTo1 = MesquiteSet.clearFromSet(changedTo1, elementChangedTo1);
	   							}

		   						if (MesquiteSet.isElement(nextSet, 0))
		   							done=true;
		   						thisSet = nextSet;
	   						}
						}
						setCladeToFirstInSequence(rightDaughter);
						setCladeToFirstInSequence(leftDaughter);
						return true;
					}
  				}
   				else { //unbalanced
	   				/*This is last partition with this left-right balance
	   				if all of the highest number taxa are on the left (they started on the right)*
	   				int highestTaxonRight = taxonNumberOfNode(findTerminalOfHighestTaxonNumber(rightDaughter));
	   				int lowestTaxonLeft = taxonNumberOfNode(findTerminalOfLowestTaxonNumber(leftDaughter));
   					if (highestTaxonRight<lowestTaxonLeft){ //is last partition with this balance; go to next balance
   						if (numOnLeft-numOnRight>1){ //not yet last balance
   							//move any terminal from left to right to change balance
   							int nodeOfHighestTaxonLeft = findTerminalOfHighestTaxonNumber(leftDaughter);
   							moveBranch(nodeOfHighestTaxonLeft, rightDaughter, false);
							rightDaughter = motherOfNode(rightDaughter);
     							//then cycle, interchanging lowest and highest until all highest on left
     							int nodeOfLowestTaxonRight;
	   						while ((taxonNumberOfNode(nodeOfLowestTaxonRight = findTerminalOfLowestTaxonNumber(rightDaughter)))<(taxonNumberOfNode(nodeOfHighestTaxonLeft=findTerminalOfHighestTaxonNumber(leftDaughter))))
	   							interchangeBranches(nodeOfLowestTaxonRight, nodeOfHighestTaxonLeft, false);

   							setCladeToFirstInSequence(rightDaughter);
   							setCladeToFirstInSequence(leftDaughter);
   							return true;
   						}
   						else 
   							return false;
   					}
   					else { //is not yet last partition with this balance; go to next partition
   						int[] allTaxa = new int[numOnLeft+numOnRight];
   						int[] leftTaxa = new int[numOnLeft];
   						int[] rightTaxa = new int[numOnRight];
   						MesquiteInteger next = new MesquiteInteger(0);
   						accumulateTerminals(leftDaughter, leftTaxa, next);
   						next.setValue(0);
   						accumulateTerminals(rightDaughter, rightTaxa, next);
   						next.setValue(0);
   						accumulateTerminals(node, allTaxa, next);
   						IntegerArray.sort(leftTaxa);
   						IntegerArray.sort(rightTaxa);
   						IntegerArray.sort(allTaxa);
   						long thisSet = representSubset(allTaxa, leftTaxa); // represent allTaxa as bitfield with 0's where taxon is in right, 1 where in left
   						long nextSet = MesquiteSet.nextSet(thisSet, allTaxa.length-1);
   						if (nextSet==0L)
   							return false;
    						else {
   							long changedTo0 = (thisSet ^ nextSet) & (~nextSet);
   							long changedTo1 = (thisSet ^ nextSet) & (~thisSet);
   							while (changedTo0!=0L && changedTo1!=0L){
		   						int elementChangedTo0 = MesquiteSet.minimum(changedTo0);
		   						int elementChangedTo1 = MesquiteSet.minimum(changedTo1);
		   						int whichTaxonMovedLeftToRight = nodeOfTaxonNumber(allTaxa[elementChangedTo0]);
		   						int whichTaxonMovedRightToLeft = nodeOfTaxonNumber(allTaxa[elementChangedTo1]);
		   						interchangeBranches(whichTaxonMovedLeftToRight,whichTaxonMovedRightToLeft,false);
		   						changedTo0 = MesquiteSet.clearFromSet(changedTo0, elementChangedTo0);
		   						changedTo1 = MesquiteSet.clearFromSet(changedTo1, elementChangedTo1);
   							}
   							setCladeToFirstInSequence(rightDaughter);
   							setCladeToFirstInSequence(leftDaughter);
	   						return true;
   						}
   					}
   				}
   			}
   		}
   		else  //rightClade has reached end
   			return true;
   	}

	/*-----------------------------------------*/
	/** Returns the first tree in the sequence of all possible trees (polytomies are set to first resolution)*
	public void setToFirstInSequence(){
		setCladeToFirstInSequence(getRoot());
	}
	/*-----------------------------------------*/
	/** Returns the next tree in the sequence of all possible trees (polytomies are set to first resolution)*
	public boolean setToNextInSequence(){
		if (numberOfTerminalsInClade(root)>60) {
			MesquiteMessage.warnProgrammer("Cannot find next tree in sequence if tree has more than 60 terminal taxa");
			return false;
		}
		else
			return setCladeToNextInSequence(getRoot(), 0);
	}

	/*-----------------------------------------*/
	/** Returns whether the tree has node labels */
	public boolean hasNodeLabels(){
		return (label!=null);
	}
	/*-----------------------------------------*/
	/** Sets the node label to the passed string (doesn't copy string; just uses reference) */
	public void setNodeLabel(String s, int node){
		if (!nodeExists(node))
			return;
		if (label==null) {
			label = new String[numNodeSpaces];
			for (int i=0; i<numNodeSpaces; i++) {
				label[i] = null;
			}
		}
		label[node] = s;
	}
	/*-----------------------------------------*/
	/** Gets the node label of the node */
	public String getNodeLabel(int node){
		if (!inBounds(node))
			return  null;
		if (nodeIsTerminal(node)) {
			int it = taxonNumberOfNode(node);
			if (it>=0)
				return taxa.getTaxonName(it);
		}
		if (label==null) {
			return null;
		}
		return label[node];
	}
	/*-----------------------------------------*/
	/** Returns whether the node has a label */
	public boolean nodeHasLabel(int node){
		return (label!=null && nodeExists(node) && label[node]!=null) || (nodeIsTerminal(node));
	}
	/*-----------------------------------------*/
	/** Finds the node with the given label */
	public int nodeOfLabel(String s){
		return nodeOfLabel(s, false);
	}
	/*-----------------------------------------*/
	/** Finds the node with the given label */
	public int nodeOfLabel(String s, boolean caseSensitive){
		if (label==null || StringUtil.blank(s))
			return -1;
		else if (caseSensitive) {
			for (int i=0; i<numNodeSpaces; i++) {
				if (s.equals(label[i]))
					return i;
			}
			int it = taxa.whichTaxonNumber(s, true, permitTruncTaxNames && !permitTaxaBlockEnlargement);
			if (it>=0)
				return nodeOfTaxonNumber(it);
			return -1;
		}
		else  {
			for (int i=0; i<numNodeSpaces; i++) {
				if (s.equalsIgnoreCase(label[i]))
					return i;
			}
			int it = taxa.whichTaxonNumber(s, false, permitTruncTaxNames && !permitTaxaBlockEnlargement);
			if (it>=0)
				return nodeOfTaxonNumber(it);
			return -1;
		}
	}
	/*-----------------------------------------*/
	/** Returns whether tree is locked.*/
	public boolean isLocked() {
		return locked;
	}
	/*-----------------------------------------*/
	/** Locks the tree (Currently, not used properly!!!!).*/
	public void setLocked(boolean locked) {
		this.locked= locked;
	}
	/*-----------------------------------------*/
	/** Returns whether there are any selected nodes in the clade */
	private boolean selectedInClade( int node) {
		if (!inBounds(node))
			return false;
		if (selected==null)
			return false;
		if (getSelected(node))
			return true;
		for (int d = firstDaughterOfNode(node); nodeExists(d); d = nextSisterOfNode(d)) {
			if (selectedInClade(d))
				return true;
		}
		return false;
	}
	/*-----------------------------------------*/
	/** Returns whether there are any selected nodes in the clade */
	public boolean anySelectedInClade(int node) {
		if (!inBounds(node))
			return false;
		if (selected==null)
			return false;
		return selectedInClade(node);
	}
	/*-----------------------------------------*/
	/** Returns the number of nodes selected in the clade */
	public int numberSelectedInClade( int node) {
		if (!inBounds(node))
			return 0;
		if (selected==null)
			return 0;
		int numInClade=0;
		if (getSelected(node))
			numInClade++;
		for (int d = firstDaughterOfNode(node); nodeExists(d); d = nextSisterOfNode(d)) {
			numInClade+= numberSelectedInClade(d);
		}
		return numInClade;
	}
	/*-----------------------------------------*/
	/** Gets the first node selected in clade of node node */
	public int getFirstSelected(int node) {
		if (!inBounds(node))
			return 0;
		if (getSelected(node))
			return node;
		for (int d = firstDaughterOfNode(node); nodeExists(d); d = nextSisterOfNode(d)) {
			int s = getFirstSelected(d);
			if (s!=-1)
				return s;
		}
		return -1;
	}
	/*-----------------------------------------*/
	/** SelectsAllNodes in the clade */
	public void selectAllInClade( int node) {
		if (!inBounds(node))
			return;
		if (selected==null)
			return;
		setSelected(node, true);
		for (int d = firstDaughterOfNode(node); nodeExists(d); d = nextSisterOfNode(d)) {
			selectAllInClade(d);
		}
	}
	/*-----------------------------------------*/
	/** Attach the object to the sensitive attachments */
	public void attachToSensitives(Object obj){
		if (obj ==null)
			return;
		if (sensitiveAttachments ==null)
			sensitiveAttachments = new Vector();
		sensitiveAttachments.addElement(obj);
	}
	/*-----------------------------------------*/
	/** Remove the object from the sensitive attachments */
	public void removeFromSensitives(Object obj){
		if (obj ==null || sensitiveAttachments ==null)
			return;
		sensitiveAttachments.removeElement(obj);
	}
	/*-----------------------------------------*/
	/** Remove all objects from the sensitive attachments */
	public void removeAllSensitives(){
		if (sensitiveAttachments ==null)
			return;
		sensitiveAttachments.removeAllElements();
	}
	/*-----------------------------------------*/
	/** Returns whether there are any sensitive attachments of the given class or a subclass */
	public boolean anyAttachedSensitive(Class c){
		if (c ==null ||sensitiveAttachments == null ||sensitiveAttachments.size() == 0)
			return false;
		int s = sensitiveAttachments.size();
		for (int i= 0; i< s; i++)
			if (c.isAssignableFrom(sensitiveAttachments.elementAt(i).getClass()))
				return true;
		return false;
	}
	/*-----------------------------------------*/
	/** Returns the number of sensitive attachments of the given class or a subclass */
	public int numberAttachedSensitive(Class c){
		if (c ==null ||sensitiveAttachments == null ||sensitiveAttachments.size() == 0)
			return 0;
		int s = sensitiveAttachments.size();
		int count =0;
		for (int i= 0; i< s; i++)
			if (c.isAssignableFrom(sensitiveAttachments.elementAt(i).getClass()))
				count++;
		return count;
	}
	/*-----------------------------------------*/
	/** Returns the indexTH sensitive attachment of the given class or a subclass */
	public Object getAttachedSensitive(Class c, int index){
		if (c ==null ||sensitiveAttachments == null ||sensitiveAttachments.size() == 0)
			return null;
		int s = sensitiveAttachments.size();
		int count =0;
		for (int i= 0; i< s; i++)
			if (c.isAssignableFrom(sensitiveAttachments.elementAt(i).getClass())) {
				if (count==index)
					return sensitiveAttachments.elementAt(i);
				count++;
			}
		return null;
	}
	/*-----------------------------------------*/
	/** Lists attached sensitives */
	public String getListAttachedSensitives(){
		if (sensitiveAttachments == null ||sensitiveAttachments.size() == 0)
			return null;
		String s = "";
		for (int i= 0; i< sensitiveAttachments.size(); i++)
			s += sensitiveAttachments.elementAt(i) + "\n";
		return s;
	}
	private boolean doChecks = false; //turned off because of problems with mismatch of notifications causing frequent messages
	private boolean checkTaxaIDs(){
		if (!doChecks)
			return true;
		String warning = null;
		if (oldNumTaxa != taxa.getNumTaxa()) 
			warning = "Error in tree "  + getName() + " (id " + getID() + ", version " + getVersionNumber() + ", treeVector: " + treeVector + "): oldNumTaxa != taxa.getNumTaxa()";
		if (taxaIDs.length !=oldNumTaxa) 
			warning = "Error in tree "  + getName() + " (id " + getID() + ", version " + getVersionNumber() + ", treeVector: " + treeVector +"): oldNumTaxa != taxaIDs.length";
		for (int i = 0; i<taxa.getNumTaxa() && warning == null; i++)
			if (i>= taxaIDs.length || taxa.getTaxon(i).getID() != taxaIDs[i])
				warning = "Error in tree "  + getName() + " (id " + getID() + ", version " + getVersionNumber() + ", treeVector: " + treeVector +"): id of taxon " + i +" in Taxa doesn't match id recorded in MesquiteTree";
		if (warning == null)
			return true;
		MesquiteMessage.warnProgrammer(warning);
		return false;
	}
	public void reconcileTaxa(int code, Notification notification){
		reconcileTaxa(code, notification, true);
	}
	public void reconcileTaxa(int code, Notification notification, boolean notify){
		//check id list of taxa to see that it matches; otherwise add or subtract taxa;  ASSUMES TAXA DELETED OR ADDED BUT NOT MOVED!!!!!!
		int newNumTaxa = taxa.getNumTaxa();
		if (newNumTaxa == oldNumTaxa) {
			if (code== MesquiteListener.PARTS_MOVED || code== MesquiteListener.PARTS_CHANGED) {
				/*go through list of taxa.  If any taxon is not in sequence expected from Taxa then find where it is in the list of taxaID's
				and move it into place*/
				for (int i = 0; i<taxa.getNumTaxa(); i++){ //go through list of taxa
					if (taxa.getTaxon(i).getID() != taxaIDs[i]){ //taxon i is not in sequence expected from Taxa
						int loc = LongArray.indexOf(taxaIDs, taxa.getTaxon(i).getID());
						if (loc <0) {
							MesquiteTrunk.mesquiteTrunk.discreetAlert( "Error in MesquiteTree: taxaID's cannot be reconciled with current Taxa");
							return;
						}
						else {
							//taxon in place loc in the taxaID's has been renumbered to i.  Must adjust taxonNumber array accordingly
							//if loc>i, then any taxonNumber>loc or <i stay the same; taxonNumbers <loc and >=i get bumped up by one; taxonNumber loc becomes i
							//if loc<i, then any taxonNumber<loc or >i stay the same; taxonNumbers >loc and <=i go down by one; taxonNumber loc becomes i
							if (loc>i){
								for (int j=0; j<numNodeSpaces; j++) {
									int t = taxonNumber[j];
									if (t < loc && t>=i)
										incrementTaxonNumber(j, 1);
									else if (t == loc)
										setTaxonNumber(j, i);
								}
							}
							else {
								for (int j=0; j<numNodeSpaces; j++) {
									int t = taxonNumber[j];
									if (t > loc && t<=i)
										decrementTaxonNumber(j, 1);
									else if (t == loc)
										setTaxonNumber(j, i);
								}
							}
							LongArray.moveParts(taxaIDs, loc, 1, i-1);

						}
					}
				}
				//MesquiteTrunk.mesquiteTrunk.discreetAlert( "ERROR in MesquiteTree: MesquiteListener.PARTS_MOVED not yet handled");
				if (notify)
					notifyListeners(this, notification);
			}
			else
				checkTaxaIDs();
			resetNodeOfTaxonNumbers();
		}
		else {
			long[] oldTaxaIDs = taxaIDs;
			boolean chgd = false;
			if (code== MesquiteListener.PARTS_ADDED) {
				//cycle through finding which taxa in Taxa are not in tree, and adding them
				for (int i=0; i<newNumTaxa; i++) {
					Taxon t = taxa.getTaxon(i);
					long tid = t.getID();
					if (LongArray.indexOf(oldTaxaIDs, tid)<0){
						int tN = taxa.whichTaxonNumber(t);
						addTaxa(tN-1, 1, false, false);
						//should instead find contiguous block and add all at once!
					}
				}

			}
			else if (code== MesquiteListener.PARTS_DELETED) {
				//cycle through finding which taxa in tree have been deleted && deleting them from tree
				for (int i=oldNumTaxa-1; i>=0; i--) {
					Taxon t = taxa.getTaxonByID(oldTaxaIDs[i]);
					if (t==null) {
						deleteTaxa(i, 1, false, false);
						chgd = true;
						//should instead find contiguous block and delete all at once!
					}
				}
			}
			else {
				//cycle through finding which taxa deleted && deleting them from tree
				for (int i=oldNumTaxa-1; i>=0 && i<oldTaxaIDs.length; i--) {
					Taxon t = taxa.getTaxonByID(oldTaxaIDs[i]);
					if (t==null) {
						deleteTaxa(i, 1, false, false);
						chgd = true;
						//should instead find contiguous block and delete all at once!
					}
				}
				//cycle through finding which taxa in Taxa are not in tree, and adding them
				for (int i=0; i<newNumTaxa; i++) {
					Taxon t = taxa.getTaxon(i);
					long tid = t.getID();
					if (LongArray.indexOf(oldTaxaIDs, tid)<0){
						int tN = taxa.whichTaxonNumber(t);
						addTaxa(tN-1, 1, false, false);
						chgd = true;
						//should instead find contiguous block and add all at once!
					}
				}
			}

			oldNumTaxa = taxa.getNumTaxa();

			taxaIDs = taxa.getTaxaIDs();
			checkTaxaIDs();
			if (chgd)
				incrementVersion(MesquiteListener.BRANCHES_REARRANGED,true);
			resetNodeOfTaxonNumbers();
		}
	}
	private void addTaxa(int starting, int num, boolean resetOldNumTaxa, boolean notify){
		//TODO: make more efficient. This leaves the arrays big
		int newNumTaxa = oldNumTaxa+num;
		setNumNodeSpaces(numNodeSpaces + standardNumNodeSpaces(num));
		boolean added = false;
		if (starting<0)
			starting = -1;
		else if (starting>=oldNumTaxa)
			starting = oldNumTaxa-1;
		for (int i=0; i<numNodeSpaces; i++) {
			if (taxonNumber[i] > starting) {
				incrementTaxonNumber(i,  num);
				added = true;
			}
		}
		nodeOfTaxon =  IntegerArray.addParts(nodeOfTaxon, starting, num);
		taxaIDs = taxa.getTaxaIDs();
		if (resetOldNumTaxa)
			oldNumTaxa = taxa.getNumTaxa();
		taxaVersion = taxa.getVersionNumber();
		if (added && notify)
			incrementVersion(MesquiteListener.BRANCHES_REARRANGED,true);
	}
	private void setTaxonNumber(int node, int taxNumber){
		if (node<0 || node>=taxonNumber.length || taxNumber>= taxa.getNumTaxa())
			return;
		taxonNumber[node] = taxNumber;
		if (taxNumber>=0 && taxNumber<nodeOfTaxon.length){
			nodeOfTaxon[taxNumber]=node;
		}
	}
	private void decrementTaxonNumber(int node, int  num){
		if (node<0 || node>=taxonNumber.length)
			return;
		taxonNumber[node] -= num;
		if (taxonNumber[node]>=0 && taxonNumber[node]<nodeOfTaxon.length)
			nodeOfTaxon[taxonNumber[node]]=node;
	}
	private void incrementTaxonNumber(int node, int  num){
		if (node<0 || node>=taxonNumber.length)
			return;
		taxonNumber[node] += num;
		if (taxonNumber[node]>=0 && taxonNumber[node]<nodeOfTaxon.length)
			nodeOfTaxon[taxonNumber[node]]=node;
	}
	private void deleteTaxa(int starting, int num, boolean resetOldNumTaxa, boolean notify){
		//TODO: make more efficient. This leaves the arrays big
		int deleted = 0; //added 14 Feb 02
		for (int i=0; i<num; i++) {
			if (starting+i>=0 && starting+i<oldNumTaxa)
				snipClade(nodeOfTaxonNumber(starting+i), false);
		}
		for (int i=0; i<numNodeSpaces; i++) {
			if (taxonNumber[i] >= starting) {
				if (taxonNumber[i]-starting < num){ //among deleted
					setTaxonNumber(i, -1);
					deleted++; //added 14 Feb 02
				}
				else {
					decrementTaxonNumber(i, num);
				}
			}
		}
		nodeOfTaxon =  IntegerArray.deleteParts(nodeOfTaxon, starting, num);
		taxaIDs = taxa.getTaxaIDs();
		if (resetOldNumTaxa)
			oldNumTaxa = taxa.getNumTaxa();
		taxaVersion = taxa.getVersionNumber();
		if (deleted>0 && notify)
			incrementVersion(MesquiteListener.BRANCHES_REARRANGED,true);
	}
	/*-----------------------------------------*/
	private long[] lastNotifications = new long[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1}; //a partial protection against responding to the same notification twice, e.g. coming via two different pathways.
	private boolean notificationFound(Notification notification){
		if (notification ==null)
			return false;
		long id = notification.getNotificationNumber();
		if (id <0)
			return false;
		if (LongArray.indexOf(lastNotifications, id)>=0)
			return true;
		return false;
	}
	private void rememberNotification(Notification notification){
		if (notification ==null)
			return;
		long id = notification.getNotificationNumber();
		if (id <0)
			return;
		for (int i = 0; i< lastNotifications.length-1; i++)
			lastNotifications[i+1] = lastNotifications[i];
		lastNotifications[0] = id;
	}
	/** For MesquiteListener interface.  Passes which object changed, along with optional integer (e.g. for character)*/
	public void changed(Object caller, Object obj, Notification notification){
		if (notificationFound(notification))
			return;
		rememberNotification(notification);
		if (obj == taxa){
			if (Notification.appearsCosmetic(notification))
				return;
			int code = Notification.getCode(notification);
			if  (code==MesquiteListener.SELECTION_CHANGED)  
				return;
			int[] parameters = Notification.getParameters(notification);
			if ((parameters == null) || (code != MesquiteListener.PARTS_ADDED && code != MesquiteListener.PARTS_DELETED && code != MesquiteListener.PARTS_MOVED)) {
				reconcileTaxa(code, notification);
				return;
			}
			int starting = 0;
			int num =  0;
			try{
				starting = parameters[0];
				num = parameters[1];
			}
			catch (ArrayIndexOutOfBoundsException e){
				MesquiteMessage.warnProgrammer("Error: insufficient parameters in changed in MesquiteTree ");
				return;
			}
			if (code==MesquiteListener.PARTS_ADDED) {
				addTaxa(starting, num, true, true);
				taxaIDs = taxa.getTaxaIDs();
				checkTaxaIDs();
			}
			else if (code==MesquiteListener.PARTS_DELETED){
				deleteTaxa(starting, num, true, true);
				taxaIDs = taxa.getTaxaIDs();
				checkTaxaIDs();
			}
			else if (code==MesquiteListener.PARTS_MOVED){
				reconcileTaxa(code, notification);
				taxaIDs = taxa.getTaxaIDs();
				incrementVersion(MesquiteListener.BRANCHES_REARRANGED,true);
			}
			taxaVersion = taxa.getVersionNumber();
		}
	}
	
	public void resetTaxaInfo(){
		taxaIDs = taxa.getTaxaIDs();
		oldNumTaxa = taxa.getNumTaxa();
	}
	/*-----------------------------------------*/
	/** For MesquiteListener interface.  Passes which object was disposed*/
	public void disposing(Object obj){
	}
	/*-----------------------------------------*/
	/** For MesquiteListener interface.  Asks whether it's ok to delete the object as far as the listener is concerned (e.g., is it in use?)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;
	}

	/*-----------------------------------------*/
	/** Returns the number assigned to this tree (used by Stored Trees, see comment at field declaration)*/
	public int getAssignedNumber(){
		return sequenceNumber;
	}
	/*-----------------------------------------*/
	/** Sets the number assigned to this tree (used by Stored Trees, see comment at field declaration)*/
	public void setAssignedNumber(int num){
		sequenceNumber = num;
	}
	/*-----------------------------------------*/
	/**Translates internal numbering system to external (currently, 0 based to 1 based)*/
	public static int toExternal(int i){
		if (!MesquiteInteger.isCombinable(i))
			return i;
		else
			return i+1;
	}
	/*-----------------------------------------*/
	/**Translates external numbering system to internal (currently, 1 based to 0 based)*/
	public static int toInternal(int i){
		if (!MesquiteInteger.isCombinable(i))
			return i;
		else
			return i-1;
	}
	/*-----------------------------------------*/
	/** returns the number of dichotomous trees with the passed number of terminal taxa.  At 12 this is greater than the maximum int. */
	public static BigInteger numberOfDichotomousTrees(int numTaxa){
		if (numTaxa<=0)
			return null;
		BigInteger product = new BigInteger("1");
		if (numTaxa<3)
			return product;

		for (int i=3; i<=numTaxa; i++)
			product = product.multiply(new BigInteger(Integer.toString(2*i - 3)));
		return product;
	}

	public static String getProfileReport(){
		String s = "MesquiteTree profiling: ";
		for (int i=0; i<timers.length; i++){
			if (timers[i] != null)
				s += " (" + i + ") " + timers[i].getAccumulatedTime();
		}
		return s;
	}



	/** Ultrametricizes the tree, in simple fashion.*/
	/*.................................................................................................................*/
	public  void arbitrarilyUltrametricize(){
		setUnassignedToOnes(getRoot());
		ut(getRoot(), tallestPathAboveNode(getRoot(), 0));
	}
	/*.................................................................................................................*/
	private void setUnassignedToOnes(int node){
		if (!MesquiteDouble.isCombinable(getBranchLength(node))) {
			setBranchLength(node, 1, false);
		}
		for (int daughter=firstDaughterOfNode(node); nodeExists(daughter); daughter =  nextSisterOfNode(daughter) ) {
			setUnassignedToOnes(daughter);
		}
	}
	/*.................................................................................................................*/
	private void ut( int node, double targetHeight){
		if (nodeIsTerminal(node)) {
			setBranchLength(node, targetHeight, false);
		}
		else {
			double heightAbove = tallestPathAboveNode(node, 0);
			double nodeLength;
			if (heightAbove==0) 
				nodeLength = targetHeight/2;
			else
				nodeLength = targetHeight-heightAbove;
			if (getRoot()!=node)
				setBranchLength(node, nodeLength, false);
			for (int daughter=firstDaughterOfNode(node); nodeExists(daughter); daughter = nextSisterOfNode(daughter) ) {
				ut(daughter, targetHeight - nodeLength);
			}
		}

	}

	public int getFileIndex(){
		return fileIndex;
	}
	public void setFileIndex(int index){
		fileIndex =index;
	}
	/*_________________________________________________*/
	public boolean ancestorHasNameReference(NameReference nameRef, int node) {
		if (!nodeExists(node))
			return false;
		if (getAssociatedBit(nameRef, motherOfNode(node)))
			return true;
		if (getRoot() == node || getSubRoot() == node)
			return false;
		return ancestorHasNameReference(nameRef, motherOfNode(node));
	}
	/*_________________________________________________*/
	public int ancestorWithNameReference(NameReference nameRef, int node) {
		if (!nodeExists(node))
			return 0;
		if (getAssociatedBit(nameRef, motherOfNode(node)))
			return motherOfNode(node);
		if (getRoot() == node || getSubRoot() == node)
			return 0;
		return ancestorWithNameReference(nameRef, motherOfNode(node));
	}

}



