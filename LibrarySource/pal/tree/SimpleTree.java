// SimpleTree.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.tree;

import pal.misc.*;
import pal.io.*;
import pal.alignment.*;

import java.io.*;
import java.util.*;


/**
 * data structure for a binary/non-binary rooted/unrooted trees
 *
 * @version $Id: SimpleTree.java,v 1.11 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Alexei Drummond
 * @author Korbinian Strimmer
 *
 */
public class SimpleTree implements Tree, Report, Units, Serializable
{
	//
	// Public stuff
	//

	/** root node */
	private Node root;

	/** list of internal nodes (including root) */
	private Node[] internalNode;
	
	/** number of internal nodes (including root) */
	private int numInternalNodes;

	/** list of external nodes */
	private Node[] externalNode;
	
	/** number of external nodes */
	private int numExternalNodes;

	
	/** holds the units of the trees branches. */
	private int units = EXPECTED_SUBSTITUTIONS;
	
	/** constructor tree consisting solely of root node */
	public SimpleTree() {
		
		// Default configuration
		root = new SimpleNode();
		root.setIdentifier(new Identifier("ROOT"));
		root.setBranchLength(0.0);
		root.setBranchLengthSE(0.0);
	}

	/** constructor taking a root node */
	public SimpleTree(Node r) {
		
		root = r;
		createNodeList();
	}

	/** clone constructor */
	public SimpleTree(Tree tree)
	{
		root = new SimpleNode(tree.getRoot());
		setUnits(tree.getUnits());
		createNodeList();
	}

	/** clone constructor */
	public SimpleTree(Tree tree, boolean keepIdentifiers)
	{
		root = new SimpleNode(tree.getRoot(), keepIdentifiers);
		setUnits(tree.getUnits());
		createNodeList();
	}

	/**
	 * Return the units that this tree is expressed in.
	 */
	public final int getUnits() {
		return units;
	}

	/**
	 * Sets the units that this tree is expressed in.
	 */
	public final void setUnits(int units) {
		this.units = units;
	}


	/**
	 * Returns the number of external nodes.
	 */
	public final int getExternalNodeCount() {
		return numExternalNodes;
	}

	/**
	 * Returns the ith external node.
	 */
	public final Node getExternalNode(int i) {
		return externalNode[i];
	}
	
	/**
	 * Returns the number of internal nodes.
	 */
	public final int getInternalNodeCount() {
		return numInternalNodes;
	}

	/**
	 * Returns the ith internal node.
	 */
	public final Node getInternalNode(int i) {
		return internalNode[i];
	}

	/**
	 * Returns the root node of this tree.
	 */
	public final Node getRoot() {
		return root;
	}

	/**
	 * Set a new node as root node.
	 */
	public final void setRoot(Node r) {
		root = r;
		createNodeList();
	}

	/** count and list external and internal nodes and
		compute heights of each node */
	public void createNodeList()
	{
		numInternalNodes = 0;
		numExternalNodes = 0;
		Node node = root;
		do
		{
			node = NodeUtils.postorderSuccessor(node);
			if (node.isLeaf())
			{
				node.setNumber(numExternalNodes);
				numExternalNodes++;
			}
			else
			{
				node.setNumber(numInternalNodes);
				numInternalNodes++;
			}
		}
		while(node != root);
		
		internalNode = new Node[numInternalNodes];
		externalNode = new Node[numExternalNodes];
		node = root;
		do
		{
			node = NodeUtils.postorderSuccessor(node);
			if (node.isLeaf())
			{
				externalNode[node.getNumber()] = node;
			}
			else
			{
				internalNode[node.getNumber()] = node;
			}	
		}
		while(node != root);
				
		// compute heights if it seems necessary
		if (root.getNodeHeight() == 0.0) {
			NodeUtils.lengths2Heights(root);
		}
	}

	public String toString() {
		StringWriter sw = new StringWriter();
		TreeUtils.printNH(this, new PrintWriter(sw), true, false);

		return sw.toString();
	}


	/**
	 * return node with number num (as displayed in ASCII tree)
	 *
	 * @param num number of node
	 *
	 * @return node
	 */
	public Node findNode(int num)
	{
		createNodeList();
	
		if (num <= numExternalNodes)
		{
			return externalNode[num-1];
		}
		else
		{
			return internalNode[num-1-numExternalNodes];
		}
	}

	/**
	 * make node with number num to root node
	 *
	 * @param num number of node
	 */
	public void reroot(int num)
	{
		reroot(findNode(num));
	}

	/**
	 * make given node to root node
	 *
	 * @param node new root node
	 */
	public void reroot(Node node)
	{
		if (node.isRoot() || node.isLeaf())
		{
			return;
		}
		
		if (!node.getParent().isRoot())
		{
			reroot(node.getParent());
		}
		
		// Now the parent of node is root
		
		if (node.getParent().getChildCount() < 3)
		{
			// Rerooting not possible
			return;
		}
		
		// Exchange branch label, length et cetera
		NodeUtils.exchangeInfo(node.getParent(), node);
		
		// Rearange topology
		Node parent = node.getParent();
		NodeUtils.removeChild(parent, node);
		node.addChild(parent);
		root = node;
	}

	// interface Report
	
	public void report(PrintWriter out)
	{
		TreeUtils.report(this, out);
	}
	
	

}

