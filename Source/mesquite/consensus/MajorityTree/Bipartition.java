package mesquite.consensus.MajorityTree;

import java.util.Vector;
import mesquite.lib.Tree;

/**
 * A <code>Bipartition</code> represents an internal node along with is immediate children.
 *
 *@author frederick Clarke
 *@version $Revision: 1.1.6.1 $
 */
public class Bipartition
{
    public final static int INVALID = -1;

    private int index = INVALID;

    private int internalIndex = INVALID;

    /** The number of trees this Bipartition appears in. */
    private int count;

    /** The number of leaves below this Bipartition. */
    private int numOfLeaves;

    /** Second hashcode. */  
    private int secondHash;

    /** Used to determine if the Bipartition exist. */
    private boolean exists;

    /** Parent of the Bipartition.*/
    private Bipartition parent;

    /** List of taxon Children. */
    private Vector childAsTaxons;

    /** List of internal children. */
    private Vector childAsInternal;

    /** Pointer to the next Bipartition in the list. */
    private Bipartition next;


   
   
    /**
     * Creates a <code>Bipartition</code>
     */
    public Bipartition( int count, int numOfLeaves, int secondHash )
    {       
	exists = false;
	this.count = count;
	this.numOfLeaves = numOfLeaves;
	this.secondHash = secondHash;
	next = null;
	childAsTaxons = new Vector( 20 );
	childAsInternal = new Vector(20);

    }

    /**
     * Points to the next Bipartition in the list
     */
    protected void setNext( Bipartition next )
    {

	this.next = next;
    }

    /**
     * Gets the next Bipartition which this one points to.
     */
    public Bipartition getNext()
    {

	return next;


    }

    /**
     * Used to increment the count for the Bipartition
     */
    protected void incrementCount()
    {
	++count;
    }

    /**
     * Gets the count for the Bipartition
     *@return The count
     */
    public int getCount()
    {
	return count;
    }

    /**
     * Gets the number of leaves below the Bipartition
     *@return The number of leaves
     */
    public int getNumOfLeaves()
    {
	return numOfLeaves;
    }

     /**
     * Gets the second hashcode
     *@return second hashcode
     */
    public int getSecondHash()
    {
	return secondHash;
    }

    /**
     * Sets the exist status for the Bipartition
     *@param b The exist status
     */
    protected void setExist( boolean b )
    {
	exists = b;
    }

    /**
     * Gets the exist status for the Bipartition
     *@return The exist status
     */
    public boolean doesExist()
    {
	return exists;
    }

    /**
     * Used to set the parent node of this Bipartition.
     *@param b The parent
     */
    protected void setParent( Bipartition b )
    {
	parent = b;
    }

    /**
     * Gets the parent of the Bipartition
     *@return The parent
     */
    public Bipartition getParent()
    {
	return parent;
    }


    /**
     * Adds the specified taxon as a child of this Bipartition
     *@param c The taxon
     */
    protected void addTaxonAsChild( int c )
    {
	childAsTaxons.addElement( new Integer(c) );
    }

    /**
     * Removes the specified taxon from being a child of the Bipartition
     *@param c The taxon
     */
    protected void removeTaxonAsChild( int c )
    {
	childAsTaxons.removeElement( new Integer(c) );
    }

    /**
     * Adds the specified Bipartition as a child of this Bipartition
     *@param b The specified Bipartition
     */
    protected void addInternalAsChild( Bipartition b )
    {
	childAsInternal.addElement(b);
    }   


    /**
     * Removes the specified Bipartition from being a child of the Bipartition
     *@param b The specified Bipartition
     */
    protected void removeInternalAsChild( Bipartition b )
    {
	childAsInternal.removeElement(b);
    }

    /**
     * Gets the first child taxon from the list if any
     *@return The taxon number or INVALID
     */
    protected int firstTaxonAsChild()
    {
	if ( childAsTaxons.size() > 0 ) {
	    index = 0;

	    return ((Integer)childAsTaxons.elementAt(index)).intValue();

	} else {
	    return INVALID;
	}
    }

    /**
     * Gets the next child taxon if any from the list of taxon children
     *@return the next taxon number or INVALID
     */
    protected int nextTaxonAsChild()
    {
	index++;
	    
	if( index < childAsTaxons.size() ){
	    return ((Integer)childAsTaxons.elementAt(index)).intValue();
		
	} else {
	    return INVALID;
	}
    }

    /**
     * Gets the first child Bipartition from the list if any
     *@return The first Bipartition or null if list is empty
     */
    protected Bipartition firstInternalAsChild()
    {
	if ( childAsInternal.size() > 0 ) {
	    internalIndex = 0;

	    return ((Bipartition)childAsInternal.elementAt(internalIndex));

	} else {
	    return null;
	}
    }

    /**
     * Gets the next child Bipartition if any from the list.
     *@return The next Bipartition or null if we have reach the end of the list
     */
    protected Bipartition nextInternalAsChild()
    {
	internalIndex++;

	if( internalIndex < childAsInternal.size() ){
	    return ((Bipartition)childAsInternal.elementAt(internalIndex));
	    
	} else {
	    return null;
	}
    }

    /**
     * Checks to see if the list have taxon children
     *@return true if there are taxon children, false otherwise
     */
    public boolean hasTaxonChildren()
    {
	if ( childAsTaxons.size() == 0 )
	    return false;
	else
	    return true;
    }

    /**
     * Checks to see if the list have internal children
     *@return true if there are internal children, false otherwise
     */
    public boolean hasInternalChildren()
    {
	if ( childAsInternal.size() == 0 )
	    return false;
	else
	    return true;
    }

    /**
     * Checks to see if the specified Bipartition is a child.
     *@param b The specified Bipartition
     *@return true if the Bipartiton is in the list, false otherwise
     */
    public boolean containsInternal( Bipartition b )
    {
	return ( childAsInternal.contains(b) );
    }

    /**
     * Checks to see if the specified taxon is a child
     *@param taxNum The specified taxon
     *@return true if the taxon is in the list, false otherwise
     */
    public boolean containsTaxon( int taxonNum )
    {
	return ( childAsTaxons.contains(new Integer(taxonNum)) );
    }
	

}
