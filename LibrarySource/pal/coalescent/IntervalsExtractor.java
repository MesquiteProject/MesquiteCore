// IntervalsExtractor.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

 
package pal.coalescent;

import pal.misc.*;
import pal.tree.*;
import pal.util.*;
import pal.mep.*;
import java.util.Vector;

/**
 * A series of coalescent intervals representing the time
 * order information contained in a (serial) clock-constrained
 * tree.
 *
 * @version $Id: IntervalsExtractor.java,v 1.12 2001/07/12 12:17:43 korbinian Exp $
 *
 * @author Alexei Drummond
 * @author Korbinian Strimmer
 */
public class IntervalsExtractor implements Units
{

	/**
	 * extracts intervals from clock tree.
	 */
	public static CoalescentIntervals extractFromClockTree(Tree tree)
	{	
		return extractFromClockTree(tree, -1);
	}
	
	/**
	 * extracts intervals from clock tree. Leafs are assumed to have
	 * height zero.  Starting at time zero, small (<= minSize) intervals are pooled
	 * with the next non-small interval (if this does not exist then
	 * with the previous non-small interval) 
	 */
	public static CoalescentIntervals extractFromClockTree(Tree tree, double minSize)
	{	
		tree.createNodeList(); //make consistent
		NodeUtils.lengths2Heights(tree.getRoot());
		//NodeUtils.lengths2HeightsKeepTips(tree.getRoot(),true);
		
		// Set heights of all external nodes to zero
		// we need a proper clock-tree
		//for (int i = 0; i < tree.getExternalNodeCount(); i++)
		//{
		//	tree.getExternalNode(i).setNodeHeight(0.0);
		//}

		
		Vector times = new Vector();
		Vector childs = new Vector();
		collectInternalNodeHeights(tree.getRoot(), times, childs);
		int[] indices = new int[times.size()];

		HeapSort.sort(times, indices);

		int uniqueIntervals = 0;
		
		double currentTime = 0.0;
		for (int i = 0; i < times.size(); i++) {
			double time = ((ComparableDouble)times.elementAt(indices[i])).doubleValue();
			if (Math.abs(time - currentTime) > minSize)
			{
				uniqueIntervals += 1;
			}
			currentTime = time;	
		}
		if (uniqueIntervals == 0) uniqueIntervals = 1;

		CoalescentIntervals ci = new CoalescentIntervals(uniqueIntervals);	
		ci.setUnits(tree.getUnits());
		
		double start = 0.0;
		int numLines = tree.getExternalNodeCount();
		
		int count = 0;
		int coalescences = 0;
		for (int i = 0; i < times.size(); i++)
		{
			double finish = ((ComparableDouble)times.elementAt(indices[i])).doubleValue();
			int childCount = ((Integer)childs.elementAt(indices[i])).intValue();
			
			double length = Math.abs(finish - start);
			coalescences += childCount-1;
						
			ci.setInterval(count, length + ci.getInterval(count) );
			ci.setNumLineages(count, numLines);
			
			if (length > minSize)
			{
				count++;
				if (count == uniqueIntervals) count--;
				numLines = numLines - coalescences;
		
				coalescences = 0;
			}
			
			start = finish;	
		}
		
		return ci;
	}

	/**
	 * extracts intervals in generation times from serial clock tree (in mutation times) 
	 * after taking into account mutation rate model.
	 */
	public static CoalescentIntervals extractFromTree(Tree tree, MutationRateModel muModel) {
		
		Tree newTree = TreeUtils.mutationsToGenerations(tree, muModel);
		return extractFromTree(newTree);
	}

	/**
	 * extracts intervals from serial clock tree.
	 */
	public static CoalescentIntervals extractFromTree(Tree tree)
	{
		double MULTIFURCATION_LIMIT = BranchLimits.MINARC;
		
		// get heights if it looks necessary
		if (tree.getRoot().getNodeHeight() == 0.0) {
			NodeUtils.lengths2Heights(tree.getRoot());
		}
		
		Vector times = new Vector();
		Vector childs = new Vector();
		collectAllTimes(tree.getRoot(), times, childs);
		int[] indices = new int[times.size()];
		Vector lineages = new Vector();
		Vector intervals = new Vector();

		HeapSort.sort(times, indices);

		double start = 0.0;
		int numLines = 0;
		int i = 0;
		while (i < times.size())
		{
			
			int lineagesRemoved = 0;
			int lineagesAdded = 0;
			
			double finish = ((ComparableDouble)times.elementAt(indices[i])).doubleValue();
			double next = finish;
			
			while (Math.abs(next - finish) < MULTIFURCATION_LIMIT) {
				int children = ((Integer)childs.elementAt(indices[i])).intValue();
				if (children == 0) {
					lineagesAdded += 1;
				} else {
					lineagesRemoved += (children - 1);
				}
				i += 1;
				if (i < times.size()) {
					next = ((ComparableDouble)times.elementAt(indices[i])).doubleValue();
				} else break;
			}
			//System.out.println("time = " + finish + " removed = " + lineagesRemoved + " added = " + lineagesAdded);			
			if (lineagesAdded > 0) {
			
				if ((intervals.size() > 0) || ((finish - start) > MULTIFURCATION_LIMIT)) {
					intervals.addElement(new Double(finish - start));
					lineages.addElement(new Integer(numLines));
				}
			
				start = finish;
			}
			// add sample event
			numLines += lineagesAdded;
			
			if (lineagesRemoved > 0) {
			
				intervals.addElement(new Double(finish - start));
				lineages.addElement(new Integer(numLines));			
				start = finish;
			}
			// coalescent event
			numLines -= lineagesRemoved;
			
			
				
		}

		CoalescentIntervals ci = new CoalescentIntervals(intervals.size());
		for (i = 0; i < intervals.size(); i++) {
			ci.setInterval(i, ((Double)intervals.elementAt(i)).doubleValue());
			ci.setNumLineages(i, ((Integer)lineages.elementAt(i)).intValue());
		}
			
		// Same Units as tree	
		ci.setUnits(tree.getUnits());
		

		return ci;
	}

	// PRIVATE STUFF

	/**
	 * extract coalescent times and tip information into Vector times from tree.
	 */
	private static void collectAllTimes(Node node, Vector times, Vector childs) {

		times.addElement(new ComparableDouble(node.getNodeHeight()));
		childs.addElement(new Integer(node.getChildCount()));
		
		for (int i = 0; i < node.getChildCount(); i++) {
			collectAllTimes(node.getChild(i), times, childs);
		}
	}
	
	/**
	 * extract internal node heights Vector times from tree.
	 */
	private static void collectInternalNodeHeights(Node node, Vector times, Vector childs)
	{
		if (!node.isLeaf())
		{
			times.addElement(new ComparableDouble(node.getNodeHeight()));
			childs.addElement(new Integer(node.getChildCount()));
		
			for (int i = 0; i < node.getChildCount(); i++) {
				collectInternalNodeHeights(node.getChild(i), times, childs);
			}
		}
	}
}

