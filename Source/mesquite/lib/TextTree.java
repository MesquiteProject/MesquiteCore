/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib;

import java.awt.*;
import java.math.*;


/* ======================================================================== */
/**This class fills a StringBuffer with text that displays a tree.  For example:

     ,===== taxon 1
=====|
     |  ,== taxon 2
     `==|
        `== taxon 3

(Viewable in monospaced font).  The vertical axis is top to bottom (that is, taxon 1 is at vertical position 0).
The horizontal axis is left to right (so, the root starts at horizontal position 0).
*/

public class TextTree {
 	static final int textTreeTaxonSpace = 1;
 	static final int baseTextTreeBranchHeight = 3;
 	int textTreeBranchHeight = 3;
 	int[] vertical;  //for node locations
 	int[] horizontal; //for node locations
 	int maxh;
 	int rightmostvert, hpos;
 	int maxStringLength=0;
	public TextTree(Tree tree){
		vertical = new int[tree.getNumNodeSpaces()];
		horizontal = new int[tree.getNumNodeSpaces()];
	}
	/*-------------------------------------*/
	/** This method finds the longest string at a node.*/
	int maxStringSize (Tree tree, int N, String[] nodeStrings){ //fixed 21 mar 02
		if (nodeStrings==null)
			return 0;
		int max = 0;
		for (int i=0; i<nodeStrings.length; i++){
			if (tree.nodeInTree(i) && nodeStrings[i]!=null) {
				int t = nodeStrings[i].length();
				if (t>max)
					max = t;
			}
		}	
		return max;
			
	}
	/*-------------------------------------*/
	/** This assigns vertical and horizontal positions to nodes. Vertical = up and down on page (increasing with down); horizontal = left and right
	Each vertical position corresponds to a line of output.*/
	void assignLocs (Tree tree, int N, int baseV, int baseCladeSize){
		int thisCladeSize = tree.numberOfTerminalsInClade(N);
		if (N != tree.getRoot()){ //{first, calculate node's location in window based on ancestor's}
			int mother = tree.motherOfNode(N);
			if (N != tree.lastDaughterOfNode(N))
				vertical[N] = baseV- textTreeTaxonSpace * (baseCladeSize - thisCladeSize);
			else
				vertical[N] = baseV + textTreeTaxonSpace * (baseCladeSize - thisCladeSize);

			horizontal[N] = horizontal[mother]+ textTreeBranchHeight;
			if (tree.nodeIsPolytomous(mother))
				horizontal[N] += textTreeBranchHeight;  //{but add one if part of polytomy so not too short}
			
			if (horizontal[N] > maxh)
				maxh = horizontal[N];
		}
		if (tree.nodeIsInternal(N)) {
			int numTerminalsSoFar=0;
			int newBaseV = vertical[N];
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				assignLocs(tree, d, newBaseV, thisCladeSize-numTerminalsSoFar);//
				numTerminalsSoFar +=tree.numberOfTerminalsInClade(d);
				newBaseV = (vertical[N] + textTreeTaxonSpace * (numTerminalsSoFar));
			}
		}
	}
	/*-------------------------------------*/
	/** These two methods adjust the vertical positions relative to the leftmost terminal taxon.*/
	void doadjust (Tree tree, int N, int leftmostvert){
		vertical[N] = vertical[N] + (1 - leftmostvert);
		if (tree.nodeIsInternal(N)){
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				doadjust(tree, d, leftmostvert);
		}
	}
	void adjustLocs(Tree tree, int baseN){
		int leftmostvert =vertical[tree.leftmostTerminalOfNode(baseN)];
		if (leftmostvert != 1)
			doadjust(tree, baseN, leftmostvert);
		rightmostvert = vertical[tree.rightmostTerminalOfNode(baseN)];
	}
	
	/*-------------------------------------*/
	/** This adjusts vertical positions so mother nodes are centered under their clades.*/
	void centerTextBranches(Tree tree, int N){
		if (tree.nodeIsInternal(N)){
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				centerTextBranches(tree, d);
			vertical[N] = vertical[tree.firstDaughterOfNode(N)]+(vertical[tree.lastDaughterOfNode(N)]-vertical[tree.firstDaughterOfNode(N)]) /  2;
		}
	}

	/*-------------------------------------*/
	int nextInLine;
	int posnext;
	/** In a particular vertical position, this finds the next node whose line intersects and needs to be drawn.*/
	void findNextInLine (Tree tree, int N, int vpos){
		if (N == tree.getRoot()){
			if ((vpos == vertical[N]) && (hpos < horizontal[N])){
				nextInLine = N;
				posnext = 1;
			}
		}
		else {
			int mother = tree.motherOfNode(N);
			if (hpos <= horizontal[mother]){
				if (horizontal[mother] <= posnext){
					if (vpos == vertical[N]){
						nextInLine = N;
						posnext = horizontal[mother];
					}
					else if (((vpos > vertical[N]) && (vpos < vertical[mother])) || ((vpos < vertical[N]) && (vpos > vertical[mother]))){
						if ((nextInLine <0) || !((vpos == vertical[nextInLine]) && (horizontal[mother] == posnext))){//{can't override a direct line up if equal}
							nextInLine = N;
							posnext = horizontal[mother];
						}
					}
				}
			}
			else if ((hpos <= horizontal[N]) && (vpos == vertical[N])){ //{just in case one was missed}
				nextInLine = N;
				posnext = horizontal[mother];
			}
		}
		if (tree.nodeIsInternal(N)){
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				findNextInLine(tree, d, vpos);
		}
	} //{findNextInLine}
	
	/*-------------------------------------*/
	/** Continues writing from left to right along a vertical position, to write whatever is needed for a node N that intersects
	the line*/
	void writeNodeInLine (Tree tree, int N, StringBuffer buff, int vpos, String nodeString){
		int mother = tree.motherOfNode(N);
		//{=======  root  =======}
		if (N == tree.getRoot()){ //   {we are writing root of page}
			if (vpos == vertical[N]) {
				int fill = 0;
				if (nodeString==null)
					fill = maxStringLength;
				else
					fill = maxStringLength-nodeString.length();
				for (int jj = hpos; jj<  horizontal[N] - maxStringLength + fill; jj++)
					buff.append('=');
				if (nodeString!=null)
					buff.append(nodeString);
				buff.append('|');
				hpos = horizontal[N] + 1;
			}
		}
		//{=======  not root  =======}
		else if (vpos == vertical[N]){   //node is directly at this vertical position; thus will need to draw "====" for branch
			for (int jj = hpos; jj< horizontal[mother]; jj++)
				buff.append(' ');
			if (hpos < horizontal[mother])
				hpos = horizontal[mother];
			int textBranchRight;
			if (tree.nodeIsTerminal(N))   //{it is terminal on page, horizontal position of terminal is maxh}
				textBranchRight = maxh;
			else
				textBranchRight = horizontal[N];			//{it is internal, length of branch is horizontal, starting position of branch on left is hpos}
			int nodeStringLength = 0;
			if (nodeString==null)
				nodeStringLength = 0;
			else
				nodeStringLength = nodeString.length();
			int branchPieces = textBranchRight - hpos - nodeStringLength;
			hpos = textBranchRight + 1;

			//{==== Draw Branch ====}
			char firstBranchChar = '|';
			if (N == tree.firstDaughterOfNode(mother))
				firstBranchChar = ',';
			else if (N == tree.lastDaughterOfNode(mother))
				firstBranchChar = '`';
			else if (vertical[N] <= vertical[mother])
				firstBranchChar = '=';
			buff.append(firstBranchChar);
			branchPieces--;
			for (int jj = 1; jj<= branchPieces; jj++)
				buff.append('=');
			if (nodeString!=null)
				buff.append(nodeString);

			//{==== Draw Terminal Taxon name ====}
			if (tree.nodeIsTerminal(N)){
					buff.append(' ');  //{this puts a blank space in front of every terminal taxon name}
					buff.append(tree.getTaxa().getTaxonName(tree.taxonNumberOfNode(N)));
			}
			else
				buff.append('|');
		}
		else { //{node is not directly at this vertical position, thus its branch base might only cross this vertical position
			if (((vpos > vertical[N]) && (vpos < vertical[mother])) || ((vpos < vertical[N]) && (vpos > vertical[mother]))){
				for (int jj = hpos; jj<horizontal[mother]; jj++)
					buff.append(' ');
				buff.append('|');
				hpos = horizontal[mother] + 1;
			}
		}
	}//{writeNodeInLine}
		
	/*-------------------------------------*/
	/* Write a line of output (corresponding to one vertical position)*/
	void doLine (Tree tree, int vpos, StringBuffer buff, String[] nodeStrings){
		hpos = 1;
		do {
			posnext = 30000;
			nextInLine = -1;
			findNextInLine(tree, tree.getRoot(), vpos);
			if (nextInLine >=0) {
				if (nodeStrings!=null && nextInLine<nodeStrings.length)
					writeNodeInLine(tree, nextInLine, buff, vpos, nodeStrings[nextInLine]);
				else
					writeNodeInLine(tree, nextInLine, buff, vpos, null);
			}
		}
		while (nextInLine >=0);
		buff.append('\n');
	} //{DoLine}
	/*-------------------------------------*/
	public void drawTreeAsText(Tree tree, StringBuffer buff){
		drawTreeAsText(tree, buff, null);
	}
	/*-------------------------------------*/
	public void drawTreeAsText(Tree tree, StringBuffer buff, String[] nodeStrings){
		int root = tree.getRoot();
		maxStringLength = maxStringSize(tree, root, nodeStrings) ;  //rearranged 21 mar 02
		textTreeBranchHeight = baseTextTreeBranchHeight +maxStringLength;
		horizontal[root] = textTreeBranchHeight + 3;
		vertical[root] = (tree.numberOfTerminalsInClade(root) * textTreeTaxonSpace) / 2 + textTreeTaxonSpace + 1;
		maxh = 0;
		assignLocs(tree, root, vertical[root], tree.numberOfTerminalsInClade(tree.getRoot()));
		adjustLocs(tree, root);
		centerTextBranches(tree, root);
		for (int j = 1; j<= rightmostvert; j++)
				doLine(tree, j, buff, nodeStrings);
	}
}



