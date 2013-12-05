// Tree.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.tree;

import pal.misc.*;
import pal.io.*;

import java.io.*;
import java.util.*;


/**
 * Interface for a phylogenetic or genealogical tree.
 *
 * @version $Id: Tree.java,v 1.15 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Alexei Drummond
 */
public interface Tree extends Units, Serializable {

	/**
	 * Returns the root node of this tree.
	 */
	Node getRoot();

	/**
	 * returns a count of the number of external nodes (tips) in this
	 * tree.
	 */
	int getExternalNodeCount();
	
	/**
	 * returns a count of the number of internal nodes (and hence clades)
	 * in this tree.
	 */
	int getInternalNodeCount();

	/**
	 * returns the ith external node in the tree.
	 */
	Node getExternalNode(int i);
	
	/**
	 * returns the ith internal node in the tree.
	 */
	Node getInternalNode(int i);

	/**
	 * This method is called to ensure that the calls to other methods
	 * in this interface are valid.
	 */
	void createNodeList();

	/**
	 * Gets the units that this tree's branch lengths and node
	 * heights are expressed in.
	 */
	int getUnits();

	/**
	 * Sets the units that this tree's branch lengths and node
	 * heights are expressed in.
	 */
	void setUnits(int units);
}

