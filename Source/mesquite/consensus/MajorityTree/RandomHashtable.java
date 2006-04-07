package mesquite.consensus.MajorityTree;


import java.util.Vector;


/**
 * This class is used to represent a random hashtable. It it used to do
 * a post-order-traversal of an input tree while computing the hashCodes of
 * each internal nodes and storing them in a table. It handles collisions
 * by chaining.
 *
 *@author Frederick Clarke
 *@version $Revision: 1.1.6.1 $
 */
public class RandomHashtable
{
    /** The hash table of Bipartitions or Vector objects. */
    private Bipartition []hashTable;

    /** The number of tree input trees. */
    private int numTrees;

    /* The percentage of trees that constitutes majority (pi)*/
    private double majorityPercentage;



    /**
     * Creates a <code>RandomHashtable</code>
     * pi- added a majority as a parameter
     */
    public RandomHashtable( int tableSize, int numTrees, double majority  )
    {
	this.numTrees = numTrees;
	this.majorityPercentage = majority;
	hashTable   = new Bipartition[ tableSize ];

	for (int i = 0; i < hashTable.length; i++)
	    hashTable[i] = null;

    }

    /**
     * Iterates through the nodes of a tree and computes the hashcode of each
     * internal node. If the index that represents the hashCode in the table is null it creates
     * a <code>Bipartion</code>'s to represent the node if not it stores a Vector of
     * <code>Bipartition<code>'s at that index
     *
     *@param t A post-order representation of a tree.
     */
    public boolean traverseTree( MajPSWTree t )
    {
	boolean success = true;

	t.prepareForEnumeration();
	int []vw = t.nextVertex();


	while( vw != null ) {

	    if ( vw[1] == 0 ) {		//vw[1] is
		vw = t.nextVertex();

	    } else {
		int hashCode  = t.getFirstHash( vw[0] );
		Bipartition b = null;

		if ( hashTable[ hashCode ] == null ) {
		    b = new Bipartition(1, vw[1], t.getSecondHash(vw[0]) );
		    hashTable[ hashCode ] = b;

		} else {
		    Bipartition next = hashTable[ hashCode ];
		    Bipartition last = next;
		    boolean found    = false;

		    while (next != null) {
			if ( next.getSecondHash() == t.getSecondHash( vw[0] ) ) {
			    if ( next.getNumOfLeaves() == vw[1] ) {
				next.incrementCount();
				found = true;
				break;

			    } else {
				System.err.println("DOUBLE COLLISION at Index: " + hashCode);
				return false;
			    }

			} else {// end if
			    last = next;
			    next = next.getNext();

			}
		    }//end while

		    if ( !found ) {
			Bipartition b1 = new Bipartition(1, vw[1], t.getSecondHash(vw[0]));
			last.setNext(b1);
		    }

		}// end if
		vw = t.nextVertex();
	    }
	}
	return success;
    }


    public void clear()
    {

	for ( int i = 0; i < hashTable.length; i++ )
	    hashTable[i] = null;

    }

    /**
     * This is used to check if a particular node is a Majority node.
     *
     *@param h1 The first hashCode which is used to specify the index of the table.
     *@param h2 The second hashCode used to find the Bipartion we are looking for.
     *@return true if Node is a Majority node, false otherwise
     */
    public boolean isMajorityNode( int h1, int h2 )
    {
	boolean majority = false;

	Bipartition b = hashTable[h1];

        do {

	    //if ( b.getSecondHash() == h2 && b.getCount() > (numTrees / 2) ) { // pi- change majority percentage here
		if ( b.getSecondHash() == h2 && b.getCount() > (numTrees * majorityPercentage) ) { // pi- change majority percentage here
		majority = true;
		return majority;
	    }
//	    System.out.println("in isMajorityNode: numTrees / 2" + numTrees / 2);

	    b = b.getNext();


	} while (b != null );

	return majority;
    }

    /**
     * Used to get the Bipartion at index h1 or the Bipartition on the list where its
     * second Hashcode is equal to h2.
     *
     *@param h1 The index of the Bipartition that we are looking for.
     *@param h2 Used to find the Bipartition in the list with second HashCode equals to h2.
     *@return The Bipartition
     */
    public Bipartition getBipartition( int h1, int h2 )
    {
	Bipartition b = hashTable[h1];

	do {

	    if ( b.getSecondHash() == h2 )
		return b;


	    b = b.getNext();


	} while (b != null );

	return b;
    }

    /**
     * Used to check if we have already seen this Bipartition during the retrieval of the
     * Majority nodes.
     *
     *@param h1 Index for the Bipartition
     *@param h2 Used to find the Bipartition from the list
     *@return true if we have already seen this node, false otherwise
     */
    public boolean nodeExist( int h1, int h2 )
    {
	boolean exists = false;
	Bipartition b = hashTable[h1];


	do {

	    if ( b.getSecondHash() == h2 ) {
		exists = b.doesExist();
		return exists;
	    }


	    b = b.getNext();


	} while (b != null );

	return exists;
    }


    /**
     * Used to set the exist status of a Majority node to true. This is done during
     * the retrieval of the Majority nodes, aftering getting the node we set its exist
     * status to true.
     *@param h1 Index for the Bipartition
     *@param h2 Used to find the Bipartition from the list
     */
    protected void setExist( int h1, int h2 )
    {
	Bipartition b = hashTable[h1];


	do {

	    if ( b.getSecondHash() == h2 ) {
		b.setExist(true);
		return;
	    }


	    b = b.getNext();


	} while (b != null );

    }

}






