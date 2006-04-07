package mesquite.consensus.MajorityTree;

import java.applet.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.TreeVector;
import mesquite.treecomp.TreeSetVisualizationV2.TreeSetViz;

import java.util.*;

/**
 * A Majority tree is a tree that contains all the edges that appear in more
 * than half the number of input trees.
 *
 *@author Frederick Clarke
 *@version $Revision: 1.1.6.1 $
 * modified by Paul Ivanov
 */
public class MajorityTree extends Consenser
{
    /** Initial capacity of PSWs trees. */
    final static int INITIAL_CAPACITY = 2500;

    /** Map of trees with their corresponding PSWTree. */
    private HashMap rememberedPSWs;

    /** Representation of our random Hashtable. */
    private RandomHashtable table;

    /** The number of taxa. */
    private int numLeaves;

    /** The number of trees selected. */
    private int numTrees;

    public double majorityPercentage;

    /** Mesquite tree object. */
    Tree t;

    /** Post order traversal representation of a tree. */
    MajPSWTree tempPSW;

    /** first hash function. */
    private int []h1;

    /** Second hash function. */
    private int []h2;

    /** BitSet use to choose unique random numbers. */
    private BitSet bitSet;

    /** List of nodes that belong to the Majority tree. */
    Vector majorityNodes;

    /** List of selected trees. */
    TreeVector list;

    /** Mapping of leaves and their parents. */
    HashMap parentOfLeaf;

    /** Represents the parent of a node. */
    Bipartition parent;

    /** The majority tree. */
    MesquiteTree tempTree;

    /** A mapping of internal to external nodes. */
    Hashtable encoding;

	/** Majority submenu */
	MesquiteSubmenuSpec majorityMenu;

	/** Item in submenu currently selected */
	MesquiteString itemName;
	/** Menu item names */
	ListableVector v = new ListableVector();
	
	/**	WPM Oct05: it would be helpful to biologists if the trees contained an indication of the frequency 
	 *  with which each branch appears among the input trees.
	 *  
	 * 	The frequencies can be attached to the branches and later accessed, as follows.
	 * 	First, insert the following line to define a NameReference (dont' ask why these exist...), up here 
	 *  above startJob:

		static final NameReference frequencyNameRef = NameReference.getNameReference("branchFrequency");
	
		Then, once you have the consensus tree (in insertAndRearrange???) you can do a call like this:
		
	 	tree.setAssociatedDouble(frequencyNameRef, node, frequencyValue);
	 	
	 	to attach to the node the double "frequencyValue" representing the percentage 
	 	of input trees with that branch.  I couldn't figure out the association betweeen the bipartitions and the
	 	branches of the consensus tree, otherwise I would have done this for you...
 	 */
   /**
     * Mesquite's constructor
     */
    public boolean startJob( String arguments, Object condition, CommandRecord commandRec,
			     boolean hiredbyName )
    {
	System.out.println("argument in MajorirtTree startJob" + arguments);
    //WPM Oct05:  UI for this module's settings moved into this module
	itemName = new MesquiteString("> 50 % (default)");
	v = new ListableVector();
	v.addElement(new MesquiteString(itemName.getValue()), false);
	v.addElement(new MesquiteString("> 55 %"), false);
	v.addElement(new MesquiteString("> 60 %"), false);
	v.addElement(new MesquiteString("> 65 %"), false);
	v.addElement(new MesquiteString("> 70 %"), false);
	v.addElement(new MesquiteString("> 75 %"), false);
	v.addElement(new MesquiteString("> 80 %"), false);
	v.addElement(new MesquiteString("> 85 %"), false);
	v.addElement(new MesquiteString("> 90 %"), false);
	v.addElement(new MesquiteString("> 95 %"), false);

	majorityMenu = addSubmenu(null, "Set Majority Percentage", makeCommand("setMajorityPercentage", (Commandable)this), v);
	majorityMenu.setList(v);
	majorityMenu.setSelected(itemName);
	//WPM Oct05 BUG?: does this have the same problem as RFDifference, i.e. (1) memory overflows if many trees because ALL remembered; (2) uses wrong tree data if tree changed 
	rememberedPSWs = new HashMap( INITIAL_CAPACITY );
	majorityPercentage = 0.50;

	return true;
    }
    //WPM Oct05:  handling of this module's settings moved into this module
	public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {
		//--- W. Maddison
		if(checker.compare(this.getClass(), "Sets the percentage majority desired by the user", null, commandName, "setMajorityPercentage")) {
			int i = MesquiteInteger.fromString(arguments);
			majorityPercentage = i * .05 + .50;
			itemName.setValue((MesquiteString)v.elementAt(i));
			parametersChanged(null, commandRec);
		}
		else { // I don't recognize this command. Pass it on to the command handler in the MesquiteModule superclass.
			return super.doCommand(commandName, arguments, commandRec, checker);
		}
		// Neither I nor my superclass knows what to do
		return null;
	}//doCommand


    /**
     * Gets the name for the module
     *@return The name of the module
     */
    public String getName()
    {
	return "Majority Rules Consensus"; //WPM Oct05  renamed to match field's expectation
    }

    /**
     * Gets the version of the module
     *@return The version number
     */
    public String getVersion()
    {
	return "1.1";
    }

    /**
     * Gets the year the module was released
     *@return The year
     */
    public String getYearReleased()
    {
	return "2002";
    }

    /**
     * Indicate whether or not the module does substantive calculations and should be cited
     *@return true if the module is cited, false otherwise
     */
    public boolean showCitation()
    {
	return true;
    }
    

    /**
     * Get the name of the package the module resides in.
     *@return The package name
     */
    public String getPackageName()
    {
	return "Tree Comparison Package";
    }

    /**
     * Indicates if the module resides in a sub-menu where the user can choose directly.
     *@return true if the module can be selected directly from a sub-menu, false otherwise
     */
    public boolean getUserChoosable() //WPM 06 set to true
    {
	return true;
    }
	public boolean requestPrimaryChoice() { return true; } //WPM 06 set to true

    /**
     * Indicates if the module is a pre-release version of the module.
     *@return true if the module is a pre-release version, false otherwise
     */
    public boolean isPrerelease()
    {
	return true;
    }

    /**
     * Indicates whether the module does substantive calculations affecting analysis
     *@return true if the module is substantive, false otherwise
     *
     */
    public boolean isSubstantive()
    {
	return true;
    }

    /**
     * Get the citation for the module
     *@return The citation
     */
    public String getCitation()
    {
	return "\n" + getYearReleased() + ". " + getAuthors() + "\n";
    }

    /**
     * Get the authors of the module
     *@return The authors
     */
    public String getAuthors()
    {
	return "Frederick Clarke, City University of New York";
    }

    /**
     * Gives an explanation of what the module does
     *@return An explanation of what the module does
     */
    public String getExplanation()
    {
	return "The Majority Tree contains those edges that\n" +
	    "appears in more than half the total number of input trees";
    }

    /**
     * This method is called to compute the Majority from a set of imput trees
     *@param list The list of selected trees.
     *@return The Majority tree.
     */
    public Tree consense( TreeVector list, CommandRecord commandRec)
    {
	this.list = list;
	numLeaves = list.getTaxa().getNumTaxa();
	numTrees = list.size();

	//System.out.println("Consense being called on tree");

	tempTree = new MesquiteTree( list.getTaxa() );
       	tempTree.setAsDefined(true);

	int tableSize = getPrime( (numLeaves+numLeaves) * numTrees );

	h1 = new int[numLeaves+1];
	h2 = new int[numLeaves+1];

	for (int i = 0; i < h1.length; i++) {
	    h1[i] = -1;
	    h2[i] = -1;
	}

	majorityNodes = new Vector( numLeaves );
	parentOfLeaf = new HashMap( numLeaves );
	encoding = new Hashtable( numLeaves );
	bitSet = new BitSet( 10 * tableSize ); // use to indicate unique random hash values

	/* pi - majority is .5 by default */
	//majorityPercentage = .5;

	if ( majorityPercentage == 0)
	{
		majorityPercentage = .5;
		System.out.println(" majorityPercentage not set, setting to default of 50%" );
	}
	table = new RandomHashtable( tableSize, numTrees, majorityPercentage );


	boolean success = true;

       do
	{
	//	System.out.println("Consense: inside do-while");
	    bitSet.clear();
	    selectRandomHashes( tableSize ); // populate h1 and h2 with unique hashCodes for each taxon
	    rememberedPSWs.clear();
	    table.clear();

	    for ( int i = 0; i < list.size(); ++i ) {
	    	commandRec.tick("Majority Rules Consensus: putting tree " + (i+1));
		t = list.getTree(i);

		if (! rememberedPSWs.containsKey( t ) ) {
		    tempPSW = new MajPSWTree( t, tableSize, h1, h2 );
		    rememberedPSWs.put( t, tempPSW );
		    success = table.traverseTree( tempPSW );

		    if ( !success )
			break;

		} else {
		    System.out.println("Tree already exists:");
		}// end if
	    }// end for
	  } while ( !success );


	int firstNode = 0;

	for ( int j = 0; j < list.size(); ++j ) {
	//   System.out.println("Consense: inside forloop");
	   t = list.getTree(j);
	    tempPSW = (MajPSWTree)rememberedPSWs.get(t);

	    if ( tempPSW != null ) {
		firstNode = t.nodeOfTaxonNumber(0);
	    	commandRec.tick("Majority Rules Consensus: getting majority nodes for tree " + (j+1));
		getMajorityNodes( firstNode, t.motherOfNode(firstNode), null);

	    }// end if tempPSW
	}// end for
	//System.out.println("before majority nodes:" +tempTree.writeTree(0, true, true, true, true,":"));


	insertMajorityNodes();//insert and rearrange majority nodes
	//System.out.println("before last:" +tempTree.writeTree(0, true, true, true, true,":"));

	//System.out.println("Consense: after Majority Nodes");

	int last = tempTree.sproutDaughter( tempTree.getRoot(), true );
	//System.out.println("before setTaxonNumber:" +tempTree.writeTree(0, true, true, true, true,":"));

	tempTree.setTaxonNumber( last, 0, true );
	
	//WPM: added this
	storeScores(tempTree, tempTree.getRoot(),list.size());

	//System.out.println("before reroot:" +tempTree.writeTree(0, true, true, true, true,":"));
	tempTree.reroot(tempTree.nodeOfTaxonNumber(0), tempTree.getRoot(), true);
	//System.out.println("Consense: after reroot");
	tempTree.standardize(tempTree.getRoot(), true);
	//System.out.println("Consense: after standardize returning tree");
	return tempTree;
    }

    /**
     * by WPM.  This method is used to assign majority scores to nodes. Not sure if it works, because I don't know
     * if the bipartitions continue to be associated with the correct nodes as these manipulations happen
     */
    private void storeScores(Tree tree, int node, int total){
	    	for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
	    		storeScores(tree, d, total);
	    	if (tree.nodeIsInternal(node)){
	    		Bipartition b = findBipartition(node);
	    		if (b != null){
	    			int c = b.getCount();
	    			((MesquiteTree)tree).setNodeLabel("" + c*100.0/total, node);
	    		}
	    	}
    }
    
    /**
     * This method is used to get the Majority nodes of each tree. It does
     * a pre-order traversal of each input tree and at each node checks to see if the node is
     * a Majority node, if it is marks it as a majority node.
     *
     *@param a The ancestor of the current node during the traversal
     *@param v The current Node
     *@param last The last node majority node seen during the traversal
     */
    private void getMajorityNodes( int a, int v, Bipartition last )
    {
	Bipartition b = null;

	if ( t.nodeIsTerminal(v) ) {
	    int taxonNum = t.taxonNumberOfNode( v );

	    if (! parentOfLeaf.containsKey( new Integer(taxonNum) ) ) {

		if ( last != null ) {

		    last.addTaxonAsChild( taxonNum );
		    parentOfLeaf.put( new Integer(taxonNum), last );

		} else {
		    parentOfLeaf.put( new Integer(taxonNum), null );
		}

	    } else {
		Bipartition currentPar = (Bipartition)parentOfLeaf.get(new Integer(taxonNum));

		if ( currentPar != null && last != null ) {
		    if (! currentPar.equals(last) ) {

			if ( currentPar.getNumOfLeaves() > last.getNumOfLeaves() ) { //why switch?
			    parentOfLeaf.remove( new Integer(taxonNum) );
			    last.addTaxonAsChild( taxonNum );
			    parentOfLeaf.put( new Integer(taxonNum), last );
			    currentPar.removeTaxonAsChild( taxonNum );

			}
		    }
		} else if ( currentPar == null && last != null ) {
		    parentOfLeaf.remove( new Integer(taxonNum) );
		    last.addTaxonAsChild( taxonNum );
		    parentOfLeaf.put( new Integer(taxonNum), last );
		}
	    }
	} else {

	    int h1 = tempPSW.getFirstHash(v);
	    int h2 = tempPSW.getSecondHash(v);

	    if ( table.isMajorityNode(h1, h2) ) { // change the majority percentage

		if ( ! table.nodeExist(h1, h2) ) {

		    table.setExist( h1, h2 );
		    b = table.getBipartition( h1, h2 );
		    parent = last;

		    if ( parent != null ) {

			parent.addInternalAsChild(b);
			b.setParent( parent );

		    } else {
			b.setParent( null );
		    }
		    
		    majorityNodes.addElement(b);

		} else {
		    b = table.getBipartition( h1, h2 );

		    if ( last != null ) {
			Bipartition currentPar = b.getParent(); //hand waving

			if ( currentPar != null && !currentPar.equals(last) ) {
			    if ( currentPar.getNumOfLeaves() > last.getNumOfLeaves() ) {
				currentPar.removeInternalAsChild(b);
				last.addInternalAsChild(b);
				b.setParent( last );

			    }
			} else if ( currentPar == null && last != null ) {

			    last.addInternalAsChild(b);
			    b.setParent(last);

			}// end currentPar != null
		    }// end last != null
		}// node Exist
		last = b;
	    } // end isMajorityNode
	}// end node is internal

	for( int d = t.firstDaughterOfNodeUR(a,v); d > 0; d = t.nextSisterOfNodeUR(a,v,d) ) {
	    getMajorityNodes(v, d, last);
	}// end for
    }// end getMajorityNodes()


    /**
     * Inserts the majority nodes into the majority tree
     */
    private void insertMajorityNodes()
    {
	Bipartition b;

	for ( int i = 0; i < majorityNodes.size(); i++ ) {
	    b = (Bipartition)majorityNodes.elementAt(i);

	    int internal = tempTree.sproutDaughter( tempTree.getRoot(), false );
	    tempTree.setTaxonNumber(internal, -1, false);
	    encoding.put(b, new Integer(internal));

	}

	insertAndRearrange();
    }



    /**
     * Rearranges the majority nodes in the tree, that is to assign nodes to their respective
     * parents.
     */
     //
     // ABANDON ALL HOPE, YE WHO ENTER HERE!
     //
     //
    private void insertAndRearrange() //double check this?
    //this is where the bug is
    {
	Bipartition b;
	int nodeInTree, motherInTree, parentNode, numDaughters = 0, sister = 0;

	for ( int i = 0; i < majorityNodes.size(); i++ ) {
	    b = (Bipartition)majorityNodes.elementAt(i);

	    if ( encoding.containsKey(b) ) {
		Bipartition parent = b.getParent();

		if ( parent != null ) { // if we have a parent
		    parentNode = ((Integer)encoding.get(parent)).intValue();
		    nodeInTree = ((Integer)encoding.get(b)).intValue();
		    motherInTree = tempTree.motherOfNode( nodeInTree );
		    numDaughters = tempTree.numberOfDaughtersOfNode( motherInTree );

		    if ( motherInTree != parentNode ) { // if we don't already have the right parent set
			if ( numDaughters == 2 ) // exactly 2 daughters
			    sister = getInternalSister( nodeInTree );

			if ( numDaughters == 1 ) { // exactly one daughter
			    int newNode = tempTree.sproutDaughter( motherInTree, false ); // create a new daughter
			    tempTree.setTaxonNumber( newNode, -1, false );
			}

			if ( tempTree.nodeIsTerminal(parentNode) ) {
			    if ( tempTree.moveBranch(nodeInTree, parentNode, false) //move branch does a lot of things internally - hairy
			    //if ( tempTree.interchangeBranches(nodeInTree, parentNode, false)  // makes things kind of work

			    ||
			    	numDaughters == 2 && tempTree.nodesAreSisters(nodeInTree, parentNode) // added this since moveBranch will fail on it
			    ) { // returns false when nodes are sisters

				int newMother = tempTree.motherOfNode( nodeInTree );

				if ( sister != 0 )
				    moveSisterUp( motherInTree, sister );

				if ( numDaughters == 2 && sister == 0 ) { //we never get here if nodes are sisters

				    int newNode = tempTree.insertNode( motherInTree, false );
				    tempTree.setTaxonNumber( newNode, -1, false );
				    Bipartition m = findBipartition( motherInTree );

				    if ( m != null ) {
					encoding.remove(m);
					encoding.put(m, new Integer(newNode));
				    }

				}
				 //tempTree.collapseBranch( tempTree.motherOfNode(nodeInTree), false ); //pi horsing around


				//
//				if ( tempTree.deleteClade(parentNode, false) ) { // didn't used to work, would StackOverflow on the paper's majority example
				if ( tempTree.snipClade(parentNode,false) ) { // pi added this -
				    encoding.remove(b);
				    encoding.put(b, new Integer(newMother));

				    int newNode = tempTree.insertNode( newMother, false );
				    tempTree.setTaxonNumber( newNode, -1, false );
				    encoding.remove( parent );
				    encoding.put(parent, new Integer(newNode));

				} else {
				    System.err.println("DELETE FAILED");
				}


			    } else {
					int branchFrom =nodeInTree ;
					int branchTo = parentNode;

							if (branchFrom==branchTo)
								System.err.println(" (branchFrom==branchTo)");
							else if (!tempTree.nodeExists(branchFrom) || !tempTree.nodeExists(branchTo))
								System.err.println(" (!nodeExists(branchFrom) || !nodeExists(branchTo))");
							else if  (tempTree.descendantOf(branchTo,branchFrom))
								System.err.println(" (descendantOf(branchTo,branchFrom))");
							else if  (branchTo == tempTree.motherOfNode(branchFrom) && !tempTree.nodeIsPolytomous(branchTo))
								System.err.println(" (branchTo == motherOfNode(branchFrom) && !nodeIsPolytomous(branchTo))");
							else if (tempTree.nodesAreSisters(branchTo, branchFrom) && (tempTree.numberOfDaughtersOfNode(tempTree.motherOfNode(branchFrom))==2))
								System.err.println(" (nodesAreSisters(branchTo, branchFrom) && (numberOfDaughtersOfNode(motherOfNode(branchFrom))==2))");
							else if (tempTree.numberOfDaughtersOfNode(tempTree.motherOfNode(branchFrom))==1) //TODO: NOTE that you can't move a branch with
							System.err.println(" (numberOfDaughtersOfNode(motherOfNode(branchFrom))==1)");
				System.err.println("MOVED FAILED: numDaughters="+numDaughters+ " nodeInTree="+nodeInTree +" parentNode="+parentNode);
				//System.exit(1);

				// print out more info
				// maybe stop doing everything?
			    }
			} else {
			    if (tempTree.moveBranch(nodeInTree, tempTree.firstDaughterOfNode(parentNode), false) ){

				if ( sister != 0 )
				    moveSisterUp( motherInTree, sister );

				if ( numDaughters == 2 && sister == 0 ) {

				    int newNode = tempTree.insertNode( motherInTree, false );
				    tempTree.setTaxonNumber( newNode, -1, false );
				    Bipartition m = findBipartition( motherInTree );

				    if ( m != null ) {
					encoding.remove(m);
					encoding.put(m, new Integer(newNode));
				    }
				}

				tempTree.collapseBranch( tempTree.motherOfNode(nodeInTree), false );

			    } else {
				System.err.println("MOVE FAILED");
			    }
			}

		    }//end motherInTree != parentNode

		}//end parent != null

	    }//end containsKey(b)

	}// end for
		//System.out.println("before insertTaxons:" +tempTree.writeTree(0, true, true, true, true,":"));

	insertTaxons();
		//System.out.println("after insertTaxons:" +tempTree.writeTree(0, true, true, true, true,":"));

    }


    /**
     * Inserts the taxon nodes into the tree
     */
    private void insertTaxons()
    {
	Bipartition b = null;
	int nodeInTree, parentNode = 0, motherInTree;

	for( int i = 0; i < majorityNodes.size(); i++ ) {
		//System.out.println("iteration " + i + ":" +tempTree.writeTree(0, true, true, true, true,":"));

	    b = (Bipartition)majorityNodes.elementAt(i);

	    if ( encoding.containsKey(b) ) {
		nodeInTree = ((Integer)encoding.get(b)).intValue();

		if ( b.hasTaxonChildren() ) {
			int j = 0;
		    for ( int t = b.firstTaxonAsChild(); t != b.INVALID; t = b.nextTaxonAsChild() ) {
			//System.out.println("b.hasTaxonChildren: iteration " + j + ":" +tempTree.writeTree(0, true, true, true, true,":"));

			int taxon = tempTree.sproutDaughter(nodeInTree, false);
			tempTree.setTaxonNumber(taxon, t, false);
			//System.out.println("b.hasTaxonChildren: iteration " + j + ":" +tempTree.writeTree(0, true, true, true, true,":"));
			j++;
		    }//end for
		}//end hasTaxonChildren()
	    }// end containsKey(b)

	}//end for
    }


    /**
     * This is used to move a node's sister up in the tree. This is done because moving a
     * branch can remove majority nodes from the tree by re-assigning the node's children
     * to the node's parent
     *@param motherInTree The node which will replace the node being removed
     *@param siste The node that is being removed
     */
    private void moveSisterUp( int motherInTree, int sister )
    {
	Bipartition sis = findBipartition(sister);

	if ( sis != null ) {
	    int newNode = tempTree.insertNode(motherInTree, false);
	    tempTree.setTaxonNumber(newNode, -1, false);

	    Bipartition mother = findBipartition(motherInTree);

	    if( mother != null ) {
		encoding.remove(mother);
		encoding.put(mother, new Integer(newNode));

	    }
	    encoding.remove(sis);
	    encoding.put(sis, new Integer(motherInTree));

	}
    }


    /**
     * This is used to get the sister of a node if the sister  is internal and if the parent of the node
     * has only two children
     *@return the number of the node or 0
     */
    private int getInternalSister( int nodeInTree )
    {
	int sister = 0;

	if ( tempTree.nodeIsFirstDaughter(nodeInTree) ) {
	    if ( tempTree.nodeIsInternal(tempTree.nextSisterOfNode(nodeInTree)) )
		sister = tempTree.nextSisterOfNode(nodeInTree);

	} else if ( tempTree.nodeIsInternal(tempTree.previousSisterOfNode(nodeInTree)) ) {
	    sister = tempTree.previousSisterOfNode(nodeInTree);
	}
	return sister;
    }

    /**
     * Used to finds the Bipartition that corresponds to this node
     *@param node The specified node
     *@return The Bipartition or null
     */
    private Bipartition findBipartition( int node )
    {
	Bipartition b = null;
	Map.Entry m;

	for ( Iterator i = encoding.entrySet().iterator(); i.hasNext(); ) {
	    m = (Map.Entry)i.next();

	    if ( ((Integer)m.getValue()).intValue() == node ) {
		b = (Bipartition)m.getKey();
		break;
	    } else {
		continue;
	    }
	}

	return b;
    }



    /**
     * Used to select unique random hashCodes for each taxon.
     *@param tableSize The size of our Hashtable
     */
    private void selectRandomHashes( int tableSize )
    {
	Random r = new Random();
	int rn = 0;
        int newTableSize = 10 * tableSize;

	for ( int i = 0;  i < h1.length; i++ ) {
	    rn =  r.nextInt( tableSize );

	    while ( bitSet.get(rn) ) {
		rn = r.nextInt( tableSize );
	    }
	    bitSet.set(rn);
	    h1[i] = rn;
	}

        bitSet.clear();

	for ( int k = 0; k < h2.length; k++ ) {
	    int nr = r.nextInt( newTableSize );

	    while ( bitSet.get(nr) ) {
		nr = r.nextInt( newTableSize );
	    }
	    bitSet.set(nr);
	    h2[k] = nr;
	}
    }


    /**
     * This method is used to get the next biggest prime number relative to the given
     * number
     *@param num The number in which we are trying to find the next biggest prime
     *@return The next biggest prime.
     */
    public int getPrime( int num )
    {
	int number = num;

	while( ! isPrime( number ) ){
	    number++;
	}
	return number;
    }


    /**
     * This method checks to see if a given number is a prime number
     *@param num The number that we are checking
     *@return true if the number is a prime, false otherwise.
     */
    private boolean isPrime( int num )
    {
	if ( num <= 2 ){
	    return (num == 2);
	}

	if ( num % 2 == 0 ) {
	    return false;
	}

	for ( int i = 3; i <= (int)Math.sqrt( num ); i += 2 ) {

	    if ( num % i == 0 )
		return false;
	}
	return true;
    }


}






