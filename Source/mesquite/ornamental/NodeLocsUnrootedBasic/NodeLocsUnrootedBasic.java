/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
 Perhaps with your help we can be more than a few, and make Mesquite better.

 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
 Mesquite's web site is http://mesquiteproject.org

 This source code and its compiled class files are free and modifiable under the terms of 
 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.ornamental.NodeLocsUnrootedBasic;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.geom.Point2D;

import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class NodeLocsUnrootedBasic extends NodeLocsUnrooted {
	public TreeDrawing treeDrawing;
	public Tree tree;
	public TreeDisplay treeDisplay;
	MesquiteBoolean showScale;
	MesquiteBoolean showBranchLengths;
	boolean resetShowBranchLengths = false;
	int rootHeight=30;
	Vector extras;
	double anglePerTerminalTaxon;
	double angleOfFirstLeftDaughterOfDrawnRoot=0.0;
	double fractionCoverage = 0.96;

	double firsttx = 0.02;
	double centerx, centery;
	double circleSlice;
	double radius;
	Point2D.Double[] location;
	//DoublePt[] sLoc;
	double lasttx;
	Rectangle treeRectangle;
	int emptyRootSlices;
	int oldNumTaxa=0;
	private MesquiteMenuItemSpec showScaleItem;

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		showBranchLengths = new MesquiteBoolean(false);
		extras = new Vector();
		showScale = new MesquiteBoolean(true);
		addMenuItem("Fraction of Circle...", makeCommand("circleFraction", this));
		addMenuItem("Start of Circle...", makeCommand("circleStart", this));
		addCheckMenuItem(null, "Branches Proportional to Lengths", makeCommand("branchLengthsToggle", this), showBranchLengths);
		if (showBranchLengths.getValue()) {
			showScaleItem = addCheckMenuItem(null, "Show scale", makeCommand("toggleScale", this), showScale);
			resetShowBranchLengths=true;
		}
		return true;
	}

	/*.................................................................................................................*/

	public void endJob(){
		if (extras!=null) {
			for (int i=0; i<extras.size(); i++){
				TreeDisplayExtra extra = (TreeDisplayExtra)extras.elementAt(i);
				if (extra!=null){
					TreeDisplay td = extra.getTreeDisplay();
					extra.turnOff();
					if (td!=null)
						td.removeExtra(extra);
				}
			}
			extras.removeAllElements();
		}
		super.endJob();
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("branchLengthsToggle " + showBranchLengths.toOffOnString());
		temp.addLine("toggleScale " + showScale.toOffOnString());
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets whether or not the branches are to be shown proportional to their lengths", "[on = proportional; off]", commandName, "branchLengthsToggle")) {
			showBranchLengths.toggleValue(parser.getFirstToken(arguments));
			if (showBranchLengths.getValue()) 
				showScaleItem = addCheckMenuItem(null, "Show scale", makeCommand("toggleScale", this), showScale);
			else
				deleteMenuItem(showScaleItem);
			resetContainingMenuBar();
			resetShowBranchLengths=true;
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to draw the scale for branch lengths", "[on or off]", commandName, "toggleScale")) {
			showScale.toggleValue(parser.getFirstToken(arguments));
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets the fraction of the circle covered by the tree", "[number between 0 and 1]", commandName, "circleFraction")) {
			double d = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (!MesquiteDouble.isCombinable(d))
				d = MesquiteDouble.queryDouble(containerOfModule(), "Fraction of circle", "Enter a number between 0 and 1 to indicate the fraction of the circle covered by the tree", fractionCoverage);
			if (MesquiteDouble.isCombinable(d))
				fractionCoverage =d;
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets the position of the start of the circle (0 = bottom; 0.5 = top)", "[number between 0 and 1]", commandName, "circleStart")) {
			double d = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (!MesquiteDouble.isCombinable(d))
				d = MesquiteDouble.queryDouble(containerOfModule(), "Start position of circle", "Enter a number between 0 and 1 to indicate the start position of the circle", firsttx);
			if (MesquiteDouble.isCombinable(d))
				firsttx =d;
			parametersChanged();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	public String getName() {
		return "Node Locations (unrooted)";
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Calculates the node locations for a tree drawn in unrooted fashion." ;
	}

	/*_________________________________________________*/
	public boolean compatibleWithOrientation(int orientation) {
		return orientation==TreeDisplay.UNROOTED;
	}
	public void setDefaultOrientation(TreeDisplay treeDisplay) {
		treeDisplay.setOrientation(TreeDisplay.UNROOTED);
	}
	/*_________________________________________________*/
	double scaling;

	private double getBranchLength (int N) {
		if (tree.branchLengthUnassigned(N))
			return 1;
		else
			return tree.getBranchLength(N);
	}

	private double getUnscaledBranchLength (int N) {
		if (showBranchLengths.getValue())
			return getBranchLength(N);
		else
			return 1;
	}

	int numNodes;
	/*{-----------------------------------------------------------------------------}
	private int findTaxa (int node){
		if (tree.nodeIsInternal(node)){
			int maxAbove = 0;
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				int aboveThis = findTaxa(d);
				if (aboveThis>maxAbove)
					maxAbove = aboveThis;
			}
			return maxAbove+1;
		}
		else //  {terminal node; see if Daughter of N}
			return 1;
	}

	/*-----------------------------------------------------------------------------*
	private double nodeAngle (int left, int right){
		double theAngle;
		if (angle[left]<= angle[right])
			theAngle = (angle[left] + angle[right]) / 2.0;
		else {
			theAngle = angle[left] + (2 * Math.PI - Math.abs(angle[left] - angle[right])) / 2.0;
			if (theAngle > 2 * Math.PI)
				theAngle = theAngle - 2 * Math.PI;
		}
		return theAngle;
	}
	/*-----------------------------------------------------------------------------*
//	angle 0 = vertical up
	private void calcterminalPosition (int node){
		double firstangle;
		massageLoc = false;
//		firstangle = Math.PI + angleBetweenTaxa/2; //TODO: {here need to use stored value}

		firstangle = anglePerTerminalTaxon;
		angle[node] =firstangle + lasttx; // {angle in radians horizontal from vertical}
		polarLength[node] =radius;

		nodePolarToLoc(polarLength[node], angle[node], location[node]);
		if (massageLoc){
			nodeLocToPolar(location[node], treeCenter, angle[node], polar);
			polarLength[node] = polar.length;
			angle[node] = polar.angle;
			nodePolarToLoc(polarLength[node], angle[node], location[node]);  //convert back and forth to deal with roundoff
		}

//		double degrees = (angle[node]/Math.PI/2*360) % 360;
		double degrees = (angle[node]/Math.PI/2*360);
		if (degrees < 45)
			treeDrawing.labelOrientation[node] = 270;
		else if (degrees < 135)
			treeDrawing.labelOrientation[node] = 0;
		else if (degrees < 225)
			treeDrawing.labelOrientation[node] = 90;
		else if (degrees < 315)
			treeDrawing.labelOrientation[node] = 180;
		else 
			treeDrawing.labelOrientation[node] = 270;

		//nodePolarToSingleLoc(polarLength[node], angle[node], sLoc[node]);
		lasttx = (anglePerTerminalTaxon + lasttx);

	}
/*-----------------------------------------------------------------------------*

	private void termTaxaRec (int node){		//{tree traversal to find locations}
		if (tree.nodeIsTerminal(node)) 
			calcterminalPosition(node);
		else {
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				termTaxaRec(d);
		}
	}

	private void terminalTaxaLocs (int node){
		lasttx = firsttx*Math.PI * 2.0;
		termTaxaRec(node);
	}
	/*-----------------------------------------------------------------------------*
	private void calcNodeLocs (int node){
		if (tree.nodeIsInternal(node)){
			double min = MesquiteDouble.unassigned;
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
				calcNodeLocs(d);
				min = MesquiteDouble.minimum(min, polarLength[d]);
			}
			int left = tree.firstDaughterOfNode(node);
			int right = tree.lastDaughterOfNode(node);
			angle[node] = nodeAngle(left, right);
			polarLength[node] = min - circleSlice;
			if (polarLength[node] < 0) 
				polarLength[node] = 0;
			//nodePolarToSingleLoc(polarLength[node], angle[node], sLoc[node]);
			nodePolarToLoc(polarLength[node], angle[node], location[node]);
			if (massageLoc){
				nodeLocToPolar(location[node], treeCenter, angle[node], polar);
				polarLength[node] = polar.length;
				angle[node] = polar.angle;
				nodePolarToLoc(polarLength[node], angle[node], location[node]);  //convert back and forth for 
			}
		}
	}
	/*-----------------------------------------------------------------------------*
	private void calculateDaylightAngles (int node){   //node begins as the drawnRoot
		if (tree.nodeIsInternal(node)){
			double min = MesquiteDouble.unassigned;
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
				calculateEqualAreaAngleSectors(d);
				min = MesquiteDouble.minimum(min, polarLength[d]);
			}
			int left = tree.firstDaughterOfNode(node);
			int right = tree.lastDaughterOfNode(node);
			angle[node] = nodeAngle(left, right);
			polarLength[node] = min - circleSlice;
			if (polarLength[node] < 0) 
				polarLength[node] = 0;
			//nodePolarToSingleLoc(polarLength[node], angle[node], sLoc[node]);
		}
	}
	/*-----------------------------------------------------------------------------*/

	/** This calculates the angular area devoted to each node, based upon the number of descendents. 
	 * This is the Equal-Area algorithm of Meacham, as outlined by Felsenstein. */
	private void calculateEqualAreaAngleSectors (int node){   //node begins as the drawnRoot
		if (tree.nodeIsInternal(node)){
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
				calculateEqualAreaAngleSectors(d);
			}
			int numInGroup = tree.numberOfTerminalsInClade(node);
			angleOfSector[node] = anglePerTerminalTaxon*numInGroup;		
		} else {
			angleOfSector[node] = anglePerTerminalTaxon;
		}
	}

	/** This calculates the node locs of each node entirely using the angular sector devoted to the descendents. */
	private void calculateEqualAreaAngles (int node){   //node begins as the drawnRoot
		if (tree.nodeIsInternal(node)){
			double startOfSector = angle[node] - angleOfSector[node]/2.0;   // this is how much angle is devoted to this node's descendents
			double startOfDaughterSector = startOfSector;
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
				angle[d]=startOfDaughterSector + angleOfSector[d]/2.0;
				startOfDaughterSector += angleOfSector[d];
			}
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
				calculateEqualAreaAngles(d);
			}
		} 
	}

	/** This calculates the node locs of each node entirely using the angular sector devoted to the descendents. */
	private void convertAnglesToNodeLocs (int node){   //node begins as the drawnRoot
		if (tree.nodeIsInternal(node)){
			double length = scaling;
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
				if (showBranchLengths.getValue()){
					length = getBranchLength(d)*scaling;
				}
				location[d].setLocation(location[node].getX()+length*Math.cos(angle[d]), location[node].getY()-length*Math.sin(angle[d]));
			}
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
				convertAnglesToNodeLocs(d);
			}
		} 
	}

	private synchronized void rotateNode (int node, int rotateNode, double rotateAngle){   //node begins as the drawnRoot
		double nx = location[node].getX();
		double ny = location[node].getY();
		double rx = location[rotateNode].getX();
		double ry = location[rotateNode].getY();
			
		double newX = rx + (nx-rx)*Math.cos(rotateAngle) - (ny-ry)*Math.sin(rotateAngle);
		double newY = ry + (nx-rx)*Math.sin(rotateAngle) + (ny-ry)*Math.cos(rotateAngle);
		location[node].setLocation(newX, newY);
	}



	private synchronized void rotateBranch (int node, int anc, int rotateNode, double rotateAngle){   //node begins as the drawnRoot
		if (tree.nodeIsInternal(node)){
			int lastDaughter=tree.lastDaughterOfNodeUR(anc, node);
			for (int d = tree.firstDaughterOfNodeUR(anc, node); tree.nodeExists(d); d = tree.nextAroundUR(node,d)){
				rotateBranch(d, node, rotateNode, rotateAngle);
				if (d==lastDaughter)
					break;
			}
		} 
		rotateNode(node, rotateNode, rotateAngle);
	}

	private synchronized void rotateTreeRec (int node, int rotateNode, double rotateAngle){  
		if (tree.nodeIsInternal(node)){
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
				rotateTreeRec(d,  rotateNode, rotateAngle);
			}
		} 
		rotateNode(node, rotateNode, rotateAngle);
	}

	private synchronized void rotateTree (int drawnRoot, int standardNode, double standardAngle){  
		double currentAngle = angleToNode(drawnRoot, standardNode) ;
		double rotateAngle = currentAngle-standardAngle;
		rotateTreeRec(drawnRoot, drawnRoot, rotateAngle);
	}


	double daylightTolerancePerNode = 0.0001;

	/*
	private void equalizeDaylightForNodeOld (int drawnRoot, int node){   //node begins as the drawnRoot
		if (tree.nodeIsInternal(node)){
			double daylight = calculateDaylightForNode(drawnRoot, node);
			int numDaughters = tree.numberOfDaughtersOfNode(node);
			if (node!=drawnRoot)
				numDaughters ++;
			double daylightPerDaughter = daylight/(numDaughters);
			int mN = tree.motherOfNode(node);
			double difference = 0.0;
			double daylightInThisPair = 0.0;
			if (node!=drawnRoot) {
				daylightInThisPair = daylightBetweenNodes (drawnRoot,mN,tree.firstDaughterOfNode(node), node);  // start out with daylight between nother node and first daughter
				difference = daylightInThisPair-daylightPerDaughter;
				if (Math.abs(difference)>daylightTolerancePerNode) {
					rotateBranch(tree.firstDaughterOfNode(node), node, -difference);
				}
			}
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
				int nextD = tree.nextSisterOfNode(d);  // get comparison sister
				if (tree.nodeExists(nextD)){
					daylightInThisPair =  daylightBetweenNodes(drawnRoot,d, nextD, node); 
					difference = daylightInThisPair-daylightPerDaughter;
					if (Math.abs(difference)>daylightTolerancePerNode) {
						rotateBranch(nextD, node, -difference);
					}
				}

			}
		} 
	}

	private void equalizeDaylightOld (int drawnRoot, int node){   //node begins as the drawnRoot
		if (tree.nodeIsInternal(node)){
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
				equalizeDaylightOld(drawnRoot, d);
			}
			equalizeDaylightForNodeOld(drawnRoot, node);
		} 
	}

	 */


	private synchronized void reportDaughters (int drawnRoot, int node){   //node begins as the drawnRoot
		if (tree.nodeIsInternal(node)){
			int[] daughtersUR = tree.daughtersOfNodeUR(drawnRoot, node);
			String s = " centerNode: " + node + " daughters: ";
			if (daughtersUR!=null) {
				for (int i=0; i<daughtersUR.length; i++)
					s+= " " + daughtersUR[i];
			} else s+= "none";
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
				reportDaughters(drawnRoot, d);
			}
		} 
	}

	boolean nodeRotatedDuringEqualization = false;

	private synchronized void equalizeDaylightForNode (int drawnRoot, int node){   //node begins as the drawnRoot
		if (tree.nodeIsInternal(node)){
			double daylight = calculateDaylightForNode(drawnRoot, node);
			int numDaughters = tree.numberOfDaughtersOfNode(node);
			if (node!=drawnRoot)
				numDaughters ++;
			double daylightPerDaughter = daylight/(numDaughters);
			double[] shiftAmount = new double[numDaughters];
			double[] originalShiftAmount = new double[numDaughters];
			double[] cumulativeShift = new double[numDaughters];
			for (int i=0; i<cumulativeShift.length; i++)
				cumulativeShift[i]=0.0;
			double[] originalDaylightInDaughter = new double[numDaughters];
			int[] daughtersUR = tree.daughtersOfNodeUR(drawnRoot, node);
			if (daughtersUR==null) return;

			//double daylightInThisPair = 0.0;
			//	//Debug.println("\n\n\n centerNode: " + node);
			//	//Debug.println("    daylightPerDaughter: " + daylightPerDaughter);
			double totalDaylight = 0.0;

			//int daughterUR = tree.firstDaughterOfNode(node); // use firstDaughter as the first daughter for comparison
			//int firstDaughterUR = daughterUR;
			//int count = 0;
			double branchLengthTolerance=0.00000001;

			for (int i =0; i<daughtersUR.length; i++) {
				int leftDaughter = i;
				int rightDaughter = i+1;
				if (rightDaughter>=daughtersUR.length)  // we are at the end, need to go back to the start
					rightDaughter = 0;
				originalDaylightInDaughter[rightDaughter] = daylightBetweenNodes (drawnRoot, daughtersUR[leftDaughter],daughtersUR[rightDaughter], node); 
				if (getUnscaledBranchLength(daughtersUR[leftDaughter])>branchLengthTolerance && getUnscaledBranchLength(daughtersUR[rightDaughter])>branchLengthTolerance)  // shift only if branches long enough
					shiftAmount[rightDaughter]=daylightPerDaughter-originalDaylightInDaughter[rightDaughter];
				else
					shiftAmount[rightDaughter]=0.0;				
				originalShiftAmount[rightDaughter] = shiftAmount[rightDaughter];
				totalDaylight+= originalDaylightInDaughter[rightDaughter];
			}

			for (int i =1; i<daughtersUR.length; i++) {
				cumulativeShift[i] = cumulativeShift[i-1]+shiftAmount[i];
			}

			for (int i =0; i<daughtersUR.length; i++) {
				int leftDaughter = i;
				int rightDaughter = i+1;
			//	int prevRightDaughter = i;
				if (rightDaughter>=daughtersUR.length)  // we are at the end, need to go back to the start
					rightDaughter = 0;
				if (leftDaughter!=0)
					shiftAmount[rightDaughter] += shiftAmount[leftDaughter];
			//	if (rightDaughter==0)
			//		shiftAmount[rightDaughter] -= shiftAmount[leftDaughter];

		}
			//shiftAmount[0] += cumulativeShift[daughtersUR.length-1];

	/*		//Debug.println("\n    centerNode: " + node);
			for (int i =0; i<daughtersUR.length; i++) {
				int leftDaughter = i;
				int rightDaughter = i+1;
			//	int prevRightDaughter = i;
				if (rightDaughter>=daughtersUR.length)  // we are at the end, need to go back to the start
					rightDaughter = 0;
				//Debug.println("         pair: " + daughtersUR[leftDaughter] + "   " + daughtersUR[rightDaughter] + "   (" + leftDaughter + "  " + rightDaughter+")");
				//Debug.println("         original shift: " + originalShiftAmount[i]);
				//Debug.println("         shift: " + shiftAmount[i]);
			//	//Debug.println("         cumulativeShift: " + cumulativeShift[i]);
		}

*/
			boolean doShift=false;
			//	shiftAmount[shiftAmount.length-1] +=shiftAmount[0];

			for (int i =0; i<daughtersUR.length; i++) {
				if (Math.abs(shiftAmount[i])>daylightTolerancePerNode) {
					doShift = true;
					break;
				}
			}


			if (doShift) {


				for (int i =0; i<daughtersUR.length; i++) {
					int leftDaughter = i;
					int rightDaughter = i+1;
					if (rightDaughter>=daughtersUR.length)  // we are at the end, need to go back to the start
						rightDaughter = 0;
					//double angle = angleToNode(node,daughters[rightDaughter]);
					//if (Math.abs(shiftAmount[leftDaughter])>daylightTolerancePerNode && getUnscaledBranchLength(daughtersUR[rightDaughter])>branchLengthTolerance && getUnscaledBranchLength(daughtersUR[leftDaughter])>branchLengthTolerance) {
					double angle = angleToNode(node,daughtersUR[rightDaughter]);
					rotateBranch(daughtersUR[rightDaughter], node, node, shiftAmount[rightDaughter]);
					double angle2 = angleToNode(node,daughtersUR[rightDaughter]);
					//double shiftError = (Math.abs(angle-angle2) - Math.abs(shiftAmount[leftDaughter]));
					nodeRotatedDuringEqualization= true;
					//angle = angleToNode(node,daughtersUR[rightDaughter]);
					//}
					//if (rightDaughter<shiftAmount.length-1 && rightDaughter!=0)
					//	shiftAmount[rightDaughter+1] +=shiftAmount[rightDaughter];

				}
				boolean equalized = true;

				for (int i =0; i<daughtersUR.length; i++) {
					int leftDaughter = i;
					int rightDaughter = i+1;
					if (rightDaughter>=daughtersUR.length)  // we are at the end, need to go back to the start
						rightDaughter = 0;
					double daylightInThisPair = daylightBetweenNodes (drawnRoot, daughtersUR[leftDaughter],daughtersUR[rightDaughter], node);  // start out with daylight between nother node and first daughter
					if (Math.abs(daylightInThisPair-daylightPerDaughter)>	daylightTolerancePerNode) {
						equalized=false;
					}
				}


				if (!equalized && false) {
					//if (!equalized)
						//Debug.println("\n\n  •|•|•|•|•|•|•|•|•|  Not Equalized •|•|•|•|•|•|");
					//Debug.println("  centerNode: " + node);
					double cumulative = 0.0;
					for (int i =0; i<daughtersUR.length; i++) {
						int leftDaughter = i;
						int rightDaughter = i+1;
						if (rightDaughter>=daughtersUR.length)  // we are at the end, need to go back to the start
							rightDaughter = 0;
						double daylightInThisPair = daylightBetweenNodes (drawnRoot, daughtersUR[leftDaughter],daughtersUR[rightDaughter], node);  // start out with daylight between nother node and first daughter
						//Debug.println("\n    pair: " + daughtersUR[leftDaughter] + "   " + daughtersUR[rightDaughter] + "   (" + leftDaughter + "  " + rightDaughter+")");
						//Debug.println("       daylightPerDaughter: " + daylightPerDaughter);
						//Debug.println("       beforeRotateDaylight " + originalDaylightInDaughter[rightDaughter]);
						//Debug.println("         original shift: " + originalShiftAmount[rightDaughter] + "            expected difference: " + (daylightPerDaughter-(originalDaylightInDaughter[rightDaughter]+originalShiftAmount[rightDaughter])));
						//Debug.println("         shift: " + shiftAmount[rightDaughter]+"            expected difference: " + (daylightPerDaughter-(originalDaylightInDaughter[rightDaughter]+shiftAmount[rightDaughter])));
						//Debug.println("       afterRotateDaylight " + daylightInThisPair + "            observed difference: " + (daylightPerDaughter-daylightInThisPair));

						//	cumulative +=shiftAmount[rightDaughter];
					}
					double newDaylight = calculateDaylightForNode(drawnRoot, node);
					//Debug.println("       old total daylight " + daylight);
					//Debug.println("       new total daylight " + newDaylight);

					//Debug.println("");
				}

				/*
				while (tree.nodeExists(nextDaughterUR)) {
					nextDaughterUR = tree.nextAroundUR(node, nextDaughterUR);
					double angle = angleToNode(node,nextDaughterUR);
					if (Math.abs(shiftAmount[count])>daylightTolerancePerNode && getUnscaledBranchLength(nextDaughterUR)>branchLengthTolerance && getUnscaledBranchLength(daughterUR)>branchLengthTolerance) {
						rotateBranch(nextDaughterUR, node, node, shiftAmount[count]);
						angle = angleToNode(node,nextDaughterUR);
					}
					if (count<shiftAmount.length-1)
						shiftAmount[count+1] +=shiftAmount[count];
					daylightInThisPair = daylightBetweenNodes (drawnRoot, daughterUR,nextDaughterUR, node);  // start out with daylight between nother node and first daughter
					if (daylightInThisPair-daylightPerDaughter>	daylightTolerancePerNode) {
							//Debug.println("\n  •|•|•|•|•|•|•|•|•|  Not Equalized •|•|•|•|•|•|");
							//Debug.println("  centerNode: " + node);
							//Debug.println("    pair: " + daughterUR + " " + nextDaughterUR);
							//Debug.println("    daylightPerDaughter: " + daylightPerDaughter + " afterRotateDaylight " + daylightInThisPair);
							//Debug.println("    shift: " + shiftAmount[count]);
							//Debug.println("    firstDaughterUR: "+firstDaughterUR);
					}

		//	//Debug.println("    daylightPerDaughter: " + daylightPerDaughter);
	//				//Debug.println("  after rotate: "+"    pair: " + daughterUR + " " + nextDaughterUR + "      shift: " + shiftAmount[count] + " daylight " + daylightInThisPair);
					double daylightNEW = calculateDaylightForNode(drawnRoot, node);
					daughterUR = nextDaughterUR;
					count++;
					if (daughterUR==firstDaughterUR)  // we are back where we started
						break;
				}	
				 */	
				//		if (count!= numDaughters)
				//			//Debug.println("  •|•|•|•|•|•|•|•|•|  NumDaughters different node " + node +" •|•|•|•|•|•|");

			}

		} 
	}

	private synchronized void equalizeDaylight (int drawnRoot, int node){   //node begins as the drawnRoot
		if (tree.nodeIsInternal(node)){
			if (node<20)
				//if (node>=11 && node<=18)
				//  	if ((node==53) ||(node==54) || (node==64))
				//	if (node==53)
		// 	if ((node==46) || (node==53) ||(node==54) || (node==64))
				//    if ((node == 58)  || (node>60&& node<=76))
				//  	if (node==58 || node==52|| node==51|| node==46|| node==53|| node==54|| node==68)
				equalizeDaylightForNode(drawnRoot, node);
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
				equalizeDaylight(drawnRoot, d);
			}
		} 
	}

	/**  */
	private double daylightBetweenNodesOLD (int drawnRoot, int node1, int node2, int centerNode){   //node begins as the drawnRoot
		int rightD = 0;
		int leftD = 0;
		int nodeToRight=0;
		int nodeToLeft=0;
		if (tree.isAncestor(node1, node2)) { // node 1 is an ancestor of node 2
			// we've got the right hand side, which is node2
			leftD = tree.leftmostTerminalOfNode(node2);
			// but we need to get the left hand one
			nodeToLeft = tree.previousSisterOfNode(centerNode);  // we want previous sister 
			if (!tree.nodeExists(nodeToLeft)) {
				int aN = centerNode;
				while(!tree.nodeExists(nodeToLeft)) {
					aN = tree.motherOfNode(aN);
					if (aN==drawnRoot) {
						nodeToLeft = tree.lastDaughterOfNode(aN);
						break;
					}
					nodeToLeft = tree.previousSisterOfNode(aN);
				}
			}
			rightD = tree.rightmostTerminalOfNode(nodeToLeft);
		} else if (tree.isAncestor(node2, node1)) {  // node 2 is an ancestor of node 1
			// we've got the left hand side, which is node1
			rightD = tree.rightmostTerminalOfNode(node1);
			// but we need to get the right hand one
			nodeToRight = tree.nextSisterOfNode(centerNode);  // we want next sister 
			if (!tree.nodeExists(nodeToRight)) {
				int aN = centerNode;
				while(!tree.nodeExists(nodeToRight)) {
					aN = tree.motherOfNode(aN);
					if (aN==drawnRoot) {
						nodeToRight = tree.firstDaughterOfNode(aN);
						break;
					}
					nodeToRight = tree.nextSisterOfNode(aN);
				}
			}
			//so we go back to the ancestor, node 2, and see if there is a descendent to the left of node 1
			leftD = tree.leftmostTerminalOfNode(nodeToRight);
		} else {
			rightD = tree.rightmostTerminalOfNode(node1);
			leftD = tree.leftmostTerminalOfNode(node2);
		}
		double dx = location[centerNode].getX();
		double dy = location[centerNode].getY();
		double ldx = location[leftD].getX()-dx;
		double ldy = location[leftD].getY()-dy;
		double rdx = location[rightD].getX()-dx;
		double rdy = location[rightD].getY()-dy;
		double angle1 = Math.atan2(ldy,ldx);
		double angle2 = Math.atan2(rdy,rdx);

		double value = Math.abs(angle1 - angle2);
		if (value>Math.PI) {
			//Debug.println("    daylight between nodes " + node1 + " and " + node2 + ": " + value + " (centernode: " + centerNode+")");
			value =2*Math.PI-value;
		}
		if (centerNode==52) {
			//Debug.println("    daylight between nodes " + node1 + " and " + node2 + ": " + value + " (centernode: " + centerNode+") LEFT: " + leftD + " RIGHT " + rightD);
		}
		return value;
	}

	/** Calculates the angle of node Node, relative to vertext centerNode */
	private synchronized double angleToNode(int centerNode, int node) {
		double dx = location[centerNode].getX();
		double dy = location[centerNode].getY();
		double nodedx = location[node].getX()-dx;
		double nodedy = location[node].getY()-dy;
		double value = Math.atan2(nodedy,nodedx);
		if (nodedy>0) // in lower left quadrant, need to subtract value from 2pi
			value = Math.PI*2-value;
		else value = -value;

		//double angle2 = Math.atan2(enddy,enddx);
		//double value = Math.abs(angle1 - angle2);
		//		if (value>Math.PI) {
		//			value =2*Math.PI-value;
		//		}
		return value;
	}

	/** Calculates angle between nodes leftNode and rightNode relative to vertex centerNode */
	private synchronized double angleBetweenNodes(int centerNode, int leftNode, int rightNode) {
		double dx = location[centerNode].getX();
		double dy = location[centerNode].getY();
		double ldx = location[leftNode].getX()-dx;
		double ldy = location[leftNode].getY()-dy;
		double rdx = location[rightNode].getX()-dx;
		double rdy = location[rightNode].getY()-dy;
		double angle1 = Math.atan2(ldy,ldx);
		double angle2 = Math.atan2(rdy,rdx);
		double diff = angle1 - angle2;
		if (diff<0) {
			//			//Debug.println(" centernode: " + centerNode + ", leftNode: " +leftNode + ", rightNode: " + rightNode);
		}
		double value = Math.abs(angle1 - angle2);
		if (value>Math.PI) {
			value =2*Math.PI-value;
		}
		return value;
	}

	private boolean rangeCrossesZero (double leftMaxAngle, double rightMaxAngle){
		return  leftMaxAngle < rightMaxAngle;
	}

	private boolean valueJustBefore3pm (double value, double leftMaxAngle){
		return  value <= leftMaxAngle;
	}

	private boolean valueJustAfter3pm (double value, double rightMaxAngle){
		return  value >= rightMaxAngle;
	}

	/** Among the nodes that are descendent from node "node", calculates
	 *  the node that is furthest counterclockwise relative to node "node" */
	private synchronized void leftmostNodeOfCladeUR(int node, int anc, int centerNode, MesquiteDouble maxAngle, MesquiteInteger maxNode, double leftMaxAngle, double rightMaxAngle) {
		if (tree.nodeIsInternal(node)){
			int startNode = tree.firstDaughterOfNodeUR(anc, node);
			int count = 0;
			for (int d = tree.firstDaughterOfNodeUR(anc, node); tree.nodeExists(d); d = tree.nextAroundUR(node,d)){
				leftmostNodeOfCladeUR(d, node, centerNode, maxAngle, maxNode, leftMaxAngle, rightMaxAngle);
				count++;
				if (d==tree.lastDaughterOfNodeUR(anc, node))
					break;
			}
		} 
		if (tree.nodeExists(node)) {
			double value = angleToNode(centerNode,node);
			if ((node==26|| node==9) && centerNode==0 && false) {
				//Debug.println("||| LEFTMOST    node " + node + " centerNode " + centerNode+ " angleToNode " + value + " maxAngle " + maxAngle.getValue());
				//Debug.println("       leftMaxAngle " + leftMaxAngle + " rightMaxAngle " + rightMaxAngle);
				//Debug.println("");
			}
			boolean record = false;
			if (!rangeCrossesZero(leftMaxAngle,rightMaxAngle)) {
				if (value<=leftMaxAngle && value>=rightMaxAngle){  // we are within bounds
					if (maxAngle.isUnassigned())
						record=true;
					else if (value>maxAngle.getValue()) {  // we have found one further to the left than previously or we've never recorded one before
						record = true;
					} else if ((value==leftMaxAngle || value==rightMaxAngle) && (maxNode.getValue()==0)){  // we are at the boundary
						record = true;
					}
				}
			} else {  // the left angle is to the left of 3 pm, the right angle to the right
				if (valueJustBefore3pm(value,leftMaxAngle)) {  // we are just CCW of 3pm, and we know we are in bounds
					if (maxAngle.isUnassigned())
						record=true;
					else if (value>maxAngle.getValue())  //
						record = true;
					else if (maxAngle.getValue()>=rightMaxAngle)   // previous value is in the slice CW of 3 pm
						record = true;
					//else if ((value==leftMaxAngle || value==rightMaxAngle) && maxNode.getValue()==0) {
					//	record = true;
					//}
				} else if (valueJustAfter3pm(value,rightMaxAngle)){ // we are CW of 3 pm, and we are in bounds
					if (maxAngle.isUnassigned())
						record=true;
					else if ((maxAngle.getValue()>=rightMaxAngle)) { // we already found one that is in this sector
						if (value>maxAngle.getValue()){   // we are to the left of the previous max
							record = true;
						}
					} else if (maxAngle.getValue()<leftMaxAngle) {  // current best is CCW of 3pm, so don't record this new one CW of 3pm
						record = false;
					} 
					//else if ((value==leftMaxAngle || value==rightMaxAngle) && maxNode.getValue()==0) {
					//	record = true;
					//}
				}
			}
			if (record) {
				if (node==0) {
					//Debug.println("••••••••  NODE 0 ••••••••••");
					//Debug.println("        LEFTMOST    node " + node + " centerNode " + centerNode+ " angleToNode " + value + " maxAngle " + maxAngle.getValue());
					//Debug.println("        leftMaxAngle " + leftMaxAngle + " rightMaxAngle " + rightMaxAngle);
				}
				maxNode.setValue(node);
				maxAngle.setValue(value);
			}		
		}
	}

	/** Among the nodes that are descendent from node "node", calculates
	 *  the node that is furthest clockwise relative to node "node" */
	private synchronized void rightmostNodeOfCladeUR(int node, int anc, int centerNode, MesquiteDouble maxAngle, MesquiteInteger maxNode, double leftMaxAngle, double rightMaxAngle) {
		if (tree.nodeIsInternal(node)){
			int startNode = tree.firstDaughterOfNodeUR(anc, node);
			int count = 0;
			for (int d = tree.firstDaughterOfNodeUR(anc, node); tree.nodeExists(d); d = tree.nextAroundUR(node,d)){
				rightmostNodeOfCladeUR(d, node, centerNode, maxAngle, maxNode, leftMaxAngle, rightMaxAngle);
				if (d==tree.lastDaughterOfNodeUR(anc, node))
					break;
			}
		} 
		if (tree.nodeExists(node)) {
			double value = angleToNode(centerNode,node);
			if ((node==2 || node == 51) && centerNode==3 && false) {
				//		//Debug.println("||| RIGHTMOST    node " + node + " centerNode " + centerNode+ " angleToNode " + value + " maxAngle " + maxAngle.getValue());
				//		//Debug.println("       leftMaxAngle " + leftMaxAngle + " rightMaxAngle " + rightMaxAngle);
				//Debug.println("");
			}
			boolean record = false;
			if (!rangeCrossesZero(leftMaxAngle,rightMaxAngle)) {
				if (value<=leftMaxAngle && value>=rightMaxAngle){  // we are within bounds
					if (maxAngle.isUnassigned())
						record=true;
					else if (value<maxAngle.getValue()) {  // we have found one further to the right than previously
						record = true;
					} else if ((value==leftMaxAngle || value==rightMaxAngle) && (maxNode.getValue()==0)){  // we are at the boundary
						record = true;
					}
				}
			} else {  // the left angle is to the left of 3 pm, the right angle to the right.  I.e., the maxes span 0
				if (valueJustBefore3pm(value,leftMaxAngle)) {  // new values is just CCW of 3pm, and we know we are in bounds
					if (maxAngle.isUnassigned())
						record=true;
					else if (valueJustAfter3pm(maxAngle.getValue(),rightMaxAngle)) // the old value was CW of 3pm, so we already have a value that is better than this one
						record=false;
					//else if (value==leftMaxAngle || value==rightMaxAngle){   // we are at boundary
					//		if (maxNode.getValue()==0) {  // only record if first time through
					//		record = true;
					//	}
					//	} 
					else if ((value<maxAngle.getValue())) {  // given where we are, lower values are further right
						record = true;
					}
				} else if (valueJustAfter3pm(value,rightMaxAngle)){ // we are CW of 3 pm, and we are in bounds
					if (maxAngle.isUnassigned())
						record=true;
					else if (value<maxAngle.getValue()) { // we've found one a little bit further to the right
						record = true;
					} else if (maxAngle.getValue()<=leftMaxAngle) {
						record = true;
					} 
					//else if ((value==leftMaxAngle || value==rightMaxAngle) && maxNode.getValue()==0) {
					//	record = true;
					//}
				}

			}
			if (record) {
				if (node==0) {
					//Debug.println("••••••••  NODE 0 ••••••••••");
					//Debug.println("        RIGHTMOST    node " + node + " centerNode " + centerNode+ " angleToNode " + value + " maxAngle " + maxAngle.getValue());
					//Debug.println("        leftMaxAngle " + leftMaxAngle + " rightMaxAngle " + rightMaxAngle);
				}
				maxNode.setValue(node);
				maxAngle.setValue(value);
			}
		}
	}


	/** Among the nodes that are "descendent" from node "node", calculates
	 *  the node that is furthest counterclockwise relative to node "node" */
	private synchronized int leftmostNode(int node, int centerNode, double leftMaxAngle, double rightMaxAngle) {
		MesquiteDouble maxAngle = new MesquiteDouble();
		MesquiteInteger maxNode = new MesquiteInteger(0);
		if (tree.nodeIsTerminal(node))
			return node;
		else
			leftmostNodeOfCladeUR(node, centerNode, centerNode, maxAngle, maxNode, leftMaxAngle, rightMaxAngle);
		return maxNode.getValue();
	}

	/** Among the nodes that are "descendent" from node "node", calculates
	 *  the node that is furthest clockwise relative to node "node" */
	private synchronized int rightmostNode(int node, int centerNode, double leftMaxAngle, double rightMaxAngle) {
		MesquiteDouble maxAngle = new MesquiteDouble();
		MesquiteInteger maxNode = new MesquiteInteger(0);
		if (tree.nodeIsTerminal(node))
			return node;
		else
			rightmostNodeOfCladeUR(node, centerNode, centerNode, maxAngle, maxNode, leftMaxAngle, rightMaxAngle);
		return maxNode.getValue();
	}


	/*
	private double daylightBetweenNodesPrevious (int drawnRoot, int node1, int node2, int centerNode){   //node begins as the drawnRoot
		int rightD = 0;
		int leftD = 0;
		int nodeToRight=0;
		int nodeToLeft=0;

		if (tree.isAncestor(node1, node2)) { // node 1 is an ancestor of node 2
			// we've got the right hand side, which is node2
			leftD = leftmostNode(node2, centerNode);
			// but we need to get the left hand one
			nodeToLeft = tree.previousSisterOfNode(centerNode);  // we want previous sister 
			if (!tree.nodeExists(nodeToLeft)) {
				int aN = centerNode;
				while(!tree.nodeExists(nodeToLeft)) {
					aN = tree.motherOfNode(aN);
					if (aN==drawnRoot) {
						nodeToLeft = tree.lastDaughterOfNode(aN);
						break;
					}
					nodeToLeft = tree.previousSisterOfNode(aN);
				}
			}
			rightD = rightmostNode(nodeToLeft, centerNode);
		} else if (tree.isAncestor(node2, node1)) {  // node 2 is an ancestor of node 1
			// we've got the left hand side, which is node1
			rightD = rightmostNode(node1, centerNode);
			// but we need to get the right hand one
			nodeToRight = tree.nextSisterOfNode(centerNode);  // we want next sister 
			if (!tree.nodeExists(nodeToRight)) {
				int aN = centerNode;
				while(!tree.nodeExists(nodeToRight)) {
					aN = tree.motherOfNode(aN);
					if (aN==drawnRoot) {
						nodeToRight = tree.firstDaughterOfNode(aN);
						break;
					}
					nodeToRight = tree.nextSisterOfNode(aN);
				}
			}
			//so we go back to the ancestor, node 2, and see if there is a descendent to the left of node 1
			leftD = leftmostNode(nodeToRight, centerNode);
		} else {
			rightD = rightmostNode(node1, centerNode);
			leftD = leftmostNode(node2, centerNode);
		}

		double value = angleBetweenNodes(centerNode, leftD, rightD);
	 	if (centerNode==52) {
		 	//Debug.println("    daylight between nodes " + node1 + " and " + node2 + ": " + value + " (centernode: " + centerNode+") LEFT: " + leftD + " RIGHT " + rightD);
	 	}
		return value;
	}


	/**  */
	private synchronized double daylightBetweenNodes (int drawnRoot, int leftDaughter, int rightDaughter, int centerNode){   //node begins as the drawnRoot
		boolean centerNodeToCheck = ((centerNode==46) || (centerNode==53) ||(centerNode==54) || (centerNode==64));
		centerNodeToCheck = false;
		if (centerNodeToCheck) {
			//	if ((centerNode==3 && leftDaughter==2 && rightDaughter==4) && false) {
			//Debug.println("\n CENTERNODE: " + centerNode);
			//Debug.println("      leftDaughter: " + leftDaughter + "  rightDaughter " + rightDaughter);
		}
		double leftMaxAngle = angleToNode(centerNode,leftDaughter);
		double rightMaxAngle = angleToNode(centerNode,rightDaughter);
		if (centerNode==23 && leftDaughter==25 && rightDaughter==19) {
			//Debug.println("      leftMaxAngle " + leftMaxAngle + " rightMaxAngle " + rightMaxAngle +"\n");
		}
		int nodeToLeft = rightmostNode(leftDaughter,centerNode, leftMaxAngle, rightMaxAngle);
		int nodeToRight = leftmostNode(rightDaughter,centerNode, leftMaxAngle, rightMaxAngle);
		if (nodeToLeft==0 && true) {
			//Debug.println("••••••••  NODE TO LEFT 0 ••••••••••");
			//Debug.println("        centerNode " + centerNode+ " leftDaughter " + leftDaughter + " rightDaughter " + rightDaughter);
		}
		if (nodeToRight==0 && true) {
			//Debug.println("••••••••  NODE TO RIGHT 0 ••••••••••");
			//Debug.println("        centerNode " + centerNode+ " leftDaughter " + leftDaughter + " rightDaughter " + rightDaughter);
		}


		double daylight = angleBetweenNodes(centerNode, nodeToLeft, nodeToRight);
		if (centerNodeToCheck) {
			//Debug.println("      nodeToLeft: " + nodeToLeft + "  nodeToRight " + nodeToRight);
			//Debug.println("      daylight angle: " + daylight);
			//Debug.println("      leftMaxAngle: " + leftMaxAngle + "  rightMaxAngle " + rightMaxAngle);
		}
		if (daylight<0) {
			//Debug.println("••••••••  DAYLIGHT<0  ••••••••••");
			//Debug.println("   CENTERNODE: " + centerNode);
			//Debug.println("      leftDaughter: " + leftDaughter + "  rightDaughter " + rightDaughter);
		}
			
		return daylight;
	}



	private synchronized double calculateDaylightForNode (int drawnRoot, int node){   //node begins as the drawnRoot
		if (tree.nodeIsInternal(node)){
			double daylight = 0.0;
			int[] daughtersUR = tree.daughtersOfNodeUR(drawnRoot, node);
			if (daughtersUR==null)
				return 0.0;
			
			
			for (int i =0; i<daughtersUR.length; i++) {
				int leftDaughter = i;
				int rightDaughter = i+1;
				if (rightDaughter>=daughtersUR.length)  // we are at the end, need to go back to the start
					rightDaughter = 0;
				
				daylight+= daylightBetweenNodes (drawnRoot, daughtersUR[leftDaughter],daughtersUR[rightDaughter], node); 
			}

			/*			

			int daughterUR = tree.firstDaughterOfNode(node); // use firstDaughter as the first daughter for comparison
			int firstDaughterUR = daughterUR;
			int nextDaughterUR = daughterUR;
			while (tree.nodeExists(nextDaughterUR)) {
				nextDaughterUR = tree.nextAroundUR(node, nextDaughterUR);
				daylight += daylightBetweenNodes (drawnRoot, daughterUR,nextDaughterUR, node);  // start out with daylight between nother node and first daughter
				daughterUR = nextDaughterUR;
				if (daughterUR==firstDaughterUR)  // we are back where we started
					break;
			}
			 */
			return daylight;
		} 
		return 0;
	}

	/* -----
	private double calculateDaylightForNodeOld (int drawnRoot, int node){   //node begins as the drawnRoot
		if (tree.nodeIsInternal(node)){
			int mN = tree.motherOfNode(node);
			double daylight = 0.0;
			if (node!=drawnRoot)
				daylight = daylightBetweenNodesPrevious (drawnRoot, mN,tree.firstDaughterOfNode(node), node);  // start out with daylight between nother node and first daughter
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
				int nextD = tree.nextSisterOfNode(d);  // get comparison sister
				if (!tree.nodeExists(nextD))  // oops, must be at end, so let's get motherNode again
					if (node==drawnRoot)
						nextD = tree.firstDaughterOfNode(node);
					else
						nextD = mN;
				daylight += daylightBetweenNodesPrevious(drawnRoot, d, nextD, node); 
			}
			return daylight;
		} 
		return 0;
	}
	 */

	private synchronized void calculateDaylight (int drawnRoot, int node, MesquiteDouble daylight){   //node begins as the drawnRoot
		if (tree.nodeIsInternal(node)){
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
				calculateDaylight(drawnRoot, d, daylight);
			}
			double dl = calculateDaylightForNode(drawnRoot, node);
			daylight.add(dl);
		} 
	}
	/*
	private void calculateDaylightOld(int drawnRoot, int node, MesquiteDouble daylight){   //node begins as the drawnRoot
		if (tree.nodeIsInternal(node)){
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
				calculateDaylightOld(drawnRoot, d, daylight);
			}
			double dl = calculateDaylightForNodeOld(drawnRoot, node);
			daylight.add(dl);
		} 
	}

	 */

	/** This calculates the node locs of each node entirely using the angular sector devoted to the descendents. */
	private synchronized void adjustDaylight (int drawnRoot){   //node begins as the drawnRoot

		double daylightInTree = 0.0;
		double lastDaylightInTree = 0.0;

		MesquiteDouble daylight = new MesquiteDouble(0.0);
		//Debug.println(" |||||||||||||  \n\n ");

		calculateDaylight(drawnRoot, drawnRoot, daylight);
		//Debug.println(" Daylight initial: " + daylight.getValue());
		lastDaylightInTree = daylight.getValue();

		int count = 0;

		//reportDaughters(drawnRoot, drawnRoot);

		while (count < 15) {
			daylight.setValue(0.0);
			nodeRotatedDuringEqualization= false;
			equalizeDaylight(drawnRoot, drawnRoot);
			//Debug.println("\n (AFTER EQUALIZE)\n");
			daylight.setValue(0.0);

			calculateDaylight(drawnRoot, drawnRoot, daylight);
			//Debug.println("       Daylight " + (count+1) + ": " +daylight.getValue());
			daylightInTree = daylight.getValue();
			if (!nodeRotatedDuringEqualization)
				break;
			//		if (daylightInTree-lastDaylightInTree<0.000001)
			//			break;
			lastDaylightInTree = daylightInTree;
			count++;
		}

		rotateTree (drawnRoot, tree.firstDaughterOfNode(drawnRoot), Math.PI/2);


	}

	double leftMost = MesquiteDouble.unassigned;
	double rightMost = MesquiteDouble.unassigned;
	double topMost = MesquiteDouble.unassigned;
	double bottomMost = MesquiteDouble.unassigned;

	/** Calculates the vertical postion of the topmost node */
	private void topMostNode (int node){   //node begins as the drawnRoot
		if (tree.nodeIsInternal(node)){
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
				topMostNode(d);
			}
		} 
		if (!MesquiteDouble.isCombinable(topMost))
			topMost= location[node].getY();
		else
			if (topMost>location[node].getY())
				topMost = location[node].getY();
	}

	/** Calculates the vertical postion of the bottomMost node */
	private void bottomMostNode (int node){   //node begins as the drawnRoot
		if (tree.nodeIsInternal(node)){
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
				bottomMostNode(d);
			}
		} 
		if (!MesquiteDouble.isCombinable(bottomMost))
			bottomMost= location[node].getY();
		else
			if (bottomMost<location[node].getY())
				bottomMost= location[node].getY();
	}

	/** Calculates the horizontal postion of the leftmost node */
	private void leftMostNode (int node){   //node begins as the drawnRoot
		if (tree.nodeIsInternal(node)){
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
				leftMostNode(d);
			}
		} 
		if (!MesquiteDouble.isCombinable(leftMost))
			leftMost= location[node].getX();
		else
			if (leftMost>location[node].getX())
				leftMost = location[node].getX();
	}

	/** Calculates the horizontal postion of the rightmost node */
	private void rightMostNode (int node){   //node begins as the drawnRoot
		if (tree.nodeIsInternal(node)){
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
				rightMostNode(d);
			}
		} 
		if (!MesquiteDouble.isCombinable(rightMost))
			rightMost= location[node].getX();
		else
			if (rightMost<location[node].getX())
				rightMost= location[node].getX();
	}


	/** Shrinks the tree the specified amount */
	private void shrink (int node, double shrinkRatioH, double shrinkRatioV){   //node begins as the drawnRoot
		if (tree.nodeIsInternal(node)){
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
				shrink(d, shrinkRatioH, shrinkRatioV);
			}
		} 
		double x = location[node].getX();
		double y = location[node].getY();
		location[node].setLocation(x*shrinkRatioH, y*shrinkRatioV);

	}

	/** Shifts the tree the specified amount */
	private void shift (int node, double shiftH, double shiftV){   //node begins as the drawnRoot
		if (tree.nodeIsInternal(node)){
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
				shift(d, shiftH, shiftV);
			}
		} 
		double x = location[node].getX();
		double y = location[node].getY();
		location[node].setLocation(x+shiftH, y+shiftV);

	}


	/** Condenses or expands the tree so that it fits within treeRectangle, and centers it*/
	private void condenseTree (int drawnRoot){  
		leftMost = MesquiteDouble.unassigned;
		rightMost = MesquiteDouble.unassigned;
		topMost = MesquiteDouble.unassigned;
		bottomMost = MesquiteDouble.unassigned;
		leftMostNode(drawnRoot);
		rightMostNode(drawnRoot);
		topMostNode(drawnRoot);
		bottomMostNode(drawnRoot);
		double horiz = rightMost-leftMost;
		double vert = bottomMost-topMost;
		double shrinkRatioH = 1.0;
		double shrinkRatioV = 1.0;
		double extraShrink = 0.8;
		//	if (horiz>treeRectangle.getWidth()) {
		shrinkRatioH = extraShrink*treeRectangle.getWidth()*1.0/horiz;
		//	}
		//	if (vert>treeRectangle.getHeight()) {
		shrinkRatioV = extraShrink* treeRectangle.getHeight()*1.0/vert;
		//	}
		if (shrinkRatioH!=1.0 || shrinkRatioV != 1.0)
			shrink(drawnRoot,shrinkRatioH, shrinkRatioV);

		leftMost = MesquiteDouble.unassigned;
		rightMost = MesquiteDouble.unassigned;
		topMost = MesquiteDouble.unassigned;
		bottomMost = MesquiteDouble.unassigned;
		leftMostNode(drawnRoot);
		rightMostNode(drawnRoot);
		topMostNode(drawnRoot);
		bottomMostNode(drawnRoot);
		double leftIdeal = (treeRectangle.getWidth()-(rightMost-leftMost))/2;
		double topIdeal = (treeRectangle.getHeight()-(bottomMost-topMost))/2;

		double shiftH = 0;
		double shiftV = 0;
		shiftH = leftIdeal-leftMost;
		shiftV = topIdeal-topMost;

			//shiftV+=300;
			//shiftH-=300;

		if (shiftH!=0 || shiftV != 0)
			shift(drawnRoot,shiftH, shiftV);

		//		//Debug.println(" node 46: " +  location[46].getX() + " " + location[46].getY());
		//		//Debug.println(" node 47: " +  location[47].getX() + " " + location[47].getY());


	}





	/*_________________________________________________*/
	public synchronized void calculateNodeLocs(TreeDisplay treeDisplay, Tree tree, int drawnRoot, Rectangle rect) { 
		if (MesquiteTree.OK(tree)) {
			this.tree = tree;
			this.treeDisplay = treeDisplay;
			if (treeDisplay.getExtras() !=null) {
				if (treeDisplay.getExtras().myElements(this)==null) {  //todo: need to do one for each treeDisplay!
					NodeLocsUnrootedExtra extra = new NodeLocsUnrootedExtra(this, treeDisplay);
					treeDisplay.addExtra(extra); 
					extras.addElement(extra);
				}
			}


			int subRoot = tree.motherOfNode(drawnRoot);
			treeCenter = new Point();
			int numNodes =tree.getNumNodeSpaces();
			if (oldNumTaxa != tree.getNumTaxa() || location == null || location.length != numNodes) {
				location = new Point2D.Double[numNodes];
				for (int i=0; i<numNodes; i++) {
					location[i] = new Point2D.Double(0.0,0.0);
				}
				angle = new double[numNodes];
				angleOfSector = new double[numNodes];
				polarLength  = new double[numNodes];
				oldNumTaxa=tree.getNumTaxa();
			}
			else {
				for (int i=0; i<location.length && location[i]!=null; i++) {
					if (location[i]!=null){
						location[i].setLocation(0, 0);
					}
					polarLength[i] = 0;
					angle[i] = 0;
					angleOfSector[i] = 0;
				}
			}

			if (resetShowBranchLengths)
				treeDisplay.showBranchLengths=showBranchLengths.getValue();
			else {
				if (treeDisplay.showBranchLengths != showBranchLengths.getValue()) {
					showBranchLengths.setValue(treeDisplay.showBranchLengths);
					if (showBranchLengths.getValue()) 
						showScaleItem = addCheckMenuItem(null, "Show scale", makeCommand("toggleScale", this), showScale);
					else
						deleteMenuItem(showScaleItem);
					resetContainingMenuBar();
				}
			}
			this.treeDrawing = treeDisplay.getTreeDrawing();


			emptyRootSlices=1;
			anglePerTerminalTaxon=(2 * Math.PI) / tree.numberOfTerminalsInClade(drawnRoot);  // the angle allotted to each terminal

			treeRectangle = rect;
			scaling = 100;


			treeCenter.setLocation(treeRectangle.width / 2,treeRectangle.height / 2);
			location[drawnRoot].setLocation(treeCenter.getX(), treeCenter.getY());
			location[subRoot].setLocation(treeCenter.getX(), treeCenter.getY());
			polarLength[subRoot] = 0;
			angle[subRoot] = angleOfFirstLeftDaughterOfDrawnRoot;
			angle[drawnRoot] = angleOfFirstLeftDaughterOfDrawnRoot;

			angleOfSector[drawnRoot] = 2*Math.PI;

			calculateEqualAreaAngleSectors (drawnRoot);

			//		for (int i = 1; i<tree.getNumNodeSpaces(); i++) 
			//			//Debug.println("node " + i + ": " + angleOfSector[i]);
			//int fdN = tree.firstDaughterOfNode(drawnRoot);
			//angle[fdN] = angleOfFirstLeftDaughterOfDrawnRoot;
			calculateEqualAreaAngles(drawnRoot);
			for (int i=0; i<angle.length; i++)
				angle[i]= -angle[i];
			convertAnglesToNodeLocs(drawnRoot);

			//Debug.println("\n ==========");

			adjustDaylight(drawnRoot);

			condenseTree(drawnRoot);

			//Debug.println("-------- After Condense ");

			//terminalTaxaLocs(drawnRoot);
			//calcNodeLocs (drawnRoot);
			//if (showBranchLengths.getValue()) {
			//	adjustForLengths(drawnRoot);
			//}
			for (int i=0; i<numNodes && i<treeDrawing.y.length; i++) {
				treeDrawing.y[i] = location[i].getY();
				treeDrawing.x[i] = location[i].getX();
			}
		}
	}



	/*.................................................................................................................*
	public void drawGrid (Graphics g, double totalHeight, double scaling,  Point2D treeCenter) {
		if (g == null)
			return;
		Color c=g.getColor();
		double log10 = Math.log(10.0);
		double hundredthHeight = Math.exp(log10* ((int) (Math.log(totalHeight)/log10)-1));
		int countTenths = 0;
		double thisHeight = totalHeight + hundredthHeight;
		while ( thisHeight>=0) {
			if (countTenths % 10 == 0)
				g.setColor(Color.blue);
			else
				g.setColor(Color.cyan);
			g.setColor(Color.red);
			thisHeight -= hundredthHeight;
			//GraphicsUtil.drawOval(g,treeCenter.getX()- (int)(thisHeight*scaling) - rootHeight,treeCenter.getY()- (int)(thisHeight*scaling) - rootHeight, 2*((int)(thisHeight*scaling) + rootHeight),  2*((int)(thisHeight*scaling) + rootHeight));
			//if (countTenths % 10 == 0)
			//	g.drawString(MesquiteDouble.toString(totalScaleHeight - thisHeight), rightEdge + buffer, (int)(base- (thisHeight*scaling)));
			countTenths ++;
		}

		if (c!=null) g.setColor(c);
	}

	 */
	FontMetrics fm;
	private void drawString(Graphics g, String s, double x, double y){
		if (g == null || StringUtil.blank(s))
			return;
		try {
			Graphics2D g2 = (Graphics2D)g;
			g2.drawString(s,(float) x, (float)y);
		}
		catch (Exception e){
		}
	}

	/*.................................................................................................................*/
	public void drawGrid(double totalTreeHeight, double totalScaleHeight, double scaling, Tree tree, int drawnRoot, TreeDisplay treeDisplay, Graphics g) {
		if (g == null)
			return;
		boolean rulerOnly = false;
		int rulerWidth = 8;
		Color c=g.getColor();
		g.setColor(Color.cyan);
		int scaleBuffer = 28;
		TreeDrawing treeDrawing = treeDisplay.getTreeDrawing();
		int buffer = 8;
		double log10 = Math.log(10.0);
		double hundredthHeight = Math.exp(log10* ((int) (Math.log(totalScaleHeight)/log10)-1));
		if (totalScaleHeight/hundredthHeight <20.0)
			hundredthHeight /= 10.0;
		int countTenths = 0;
		double thisHeight = totalScaleHeight + hundredthHeight;
		fm=g.getFontMetrics(g.getFont());
		int textHeight = fm.getHeight();
		double leftEdge = treeDisplay.getTreeDrawing().y[tree.leftmostTerminalOfNode(drawnRoot)];
		double rightEdge = treeDisplay.getTreeDrawing().y[tree.rightmostTerminalOfNode(drawnRoot)] + scaleBuffer;
		double base = treeDrawing.x[drawnRoot];
		//		if (fixedScale)
		//			base += (totalTreeHeight - fixedDepth)*scaling;
		while ( thisHeight>=0) {
			if (countTenths % 10 == 0)
				g.setColor(Color.blue);
			else
				g.setColor(Color.cyan);
			thisHeight -= hundredthHeight;
			if (rulerOnly)
				GraphicsUtil.drawLine(g,(int)(base+ (thisHeight*scaling)), rightEdge-rulerWidth,  (int)(base+ (thisHeight*scaling)),  rightEdge);
			else
				GraphicsUtil.drawLine(g,(int)(base+ (thisHeight*scaling)), leftEdge,  (int)(base+ (thisHeight*scaling)),  rightEdge);
			if (countTenths % 10 == 0)
				drawString(g, MesquiteDouble.toStringInRange(totalScaleHeight - thisHeight, totalScaleHeight), (int)(base+ (thisHeight*scaling)), rightEdge + buffer + textHeight);
			countTenths ++;
		}
		if (rulerOnly)
			GraphicsUtil.drawLine(g,(int)(base), rightEdge, (int)(base+ (totalScaleHeight*scaling)),rightEdge);

		if (c !=null)
			g.setColor(c);
		g.setPaintMode();
	}
}


class NodeLocsUnrootedExtra extends TreeDisplayBkgdExtra {
	NodeLocsUnrootedBasic locsModule;

	public NodeLocsUnrootedExtra (NodeLocsUnrootedBasic ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		locsModule = ownerModule;
	}
	/*.................................................................................................................*/
	public   String writeOnTree(Tree tree, int drawnRoot) {
		return null;
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		if (locsModule.showScale.getValue() && locsModule.showBranchLengths.getValue())
			locsModule.drawGrid(tree.tallestPathAboveNode(drawnRoot, 1.0), treeDisplay.fixedDepthScale, locsModule.scaling, tree, drawnRoot, treeDisplay, g);
		//g.setColor(Color.green);
		//g.fillOval(locsModule.centerx-8, locsModule.centery-8, 16, 16);
	}
	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
	}

}


