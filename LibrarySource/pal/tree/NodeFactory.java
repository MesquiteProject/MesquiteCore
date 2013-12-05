// NodeFactory.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.tree;


/**
 * Creates nodes
 * <b>
 * The purpose of this class is to decouple the creation of
 * a class of type "Node" from its actual implementation.  This
 * class should be used instead of calling the constructor
 * of an implementation of "Node"
 * (at the moment "SimpleNode") as it may change in the future.</b><p>
 *
 * Other plans: add features here to recyle old nodes rather than
 * leaving them to the Java garbage collector
 *
 * @author Korbinian Strimmer
 */
public class NodeFactory
{
	/** create a node */
	public static Node createNode()
	{
		return new SimpleNode();
	}
	
	/** constructor used to clone a node and all children */
	public static Node createNode(Node node)
	{
		return new SimpleNode(node);
	}
}

