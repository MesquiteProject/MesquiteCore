/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


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
import mesquite.lib.duties.*;



/* ��������������������������� taxa ������������������������������� */
/* ======================================================================== */
/**This is the base class for the drawing of taxa.  It contains the information about
the number of nodes, and the x and y positions of the nodes.  It should be subclassed by taxa drawing modules, 
so that they can add their own needed field.  TaxaDrawings are typically used within TaxaDisplays.*/

public abstract class TaxaDrawing  {
	public double[] x; //x positions of taxa
	public double[] y; //y positions of taxa
	public double[] z; //z positions of taxa (closeness to viewer, smaller numbers closer)
	public double[] lineBaseX; //base of line on which to draw labels etc.
	public double[] lineBaseY; 
	public double[] lineTipX; //tip of line on which to draw labels etc.
	public double[] lineTipY; 
	/**labelOrientation indicates where label is to be drawn, in degrees. 0 = normal horizontal 
	writing to right of node, as would be done for a tree with orientation RIGHT.
	This does not represent simple rotation, i.e. 180 is on left side, but the writing is not upside down.  Thus
	0 would be appropriate for tree oriented RIGHT, 90 for tree DOWN, 180 for tree LEFT, 270 for tree UP */
	public int[] labelOrientation; 
	
	/** namesAngle indicates rotation of names from default in radians  (useful only for Java 1.2 or higher)*/
	public double namesAngle = MesquiteDouble.unassigned;

	public int numTaxa;
	public TaxaDisplay taxaDisplay;//TODO: many of these should be private fields!!!
	private int enableTerminalBoxesRequests = 0;
	public static long totalCreated = 0;
	public Polygon[] namePolys;
	
	public TaxaDrawing (TaxaDisplay taxaDisplay, int numTaxa) {
		this.taxaDisplay = taxaDisplay;
		totalCreated++;
		resetNumTaxa(numTaxa);
	}
	public void addOneTaxon(){
		if (x != null)
			return;
		numTaxa++;
		totalCreated++;
		x = DoubleArray.copyIntoDifferentSize(x,numTaxa,0);
		y = DoubleArray.copyIntoDifferentSize(y,numTaxa,0);
		z = DoubleArray.copyIntoDifferentSize(z,numTaxa,MesquiteDouble.unassigned);
		labelOrientation = IntegerArray.copyIntoDifferentSize(labelOrientation,numTaxa,MesquiteInteger.unassigned);
		lineBaseX = DoubleArray.copyIntoDifferentSize(lineBaseX,numTaxa,0);
		lineBaseY = DoubleArray.copyIntoDifferentSize(lineBaseY,numTaxa,0);
		lineTipX = DoubleArray.copyIntoDifferentSize(lineTipX,numTaxa,0);
		lineTipY = DoubleArray.copyIntoDifferentSize(lineTipY,numTaxa,0);
	}
	public void resetNumTaxa(int numTaxa){
		if (this.numTaxa == numTaxa && x != null)
			return;
		this.numTaxa=numTaxa;
		totalCreated++;
		x = new double[numTaxa];
		y = new double[numTaxa];
		z = new double[numTaxa];
		labelOrientation = new int[numTaxa];
		lineBaseX = new double[numTaxa];
		lineBaseY = new double[numTaxa];
		lineTipX = new double[numTaxa];
		lineTipY = new double[numTaxa];
		for (int i=0; i<numTaxa; i++) {
			x[i]=0;
			y[i]=0;
			z[i]=MesquiteInteger.unassigned;
			lineBaseX[i]=0;
			lineBaseY[i]=0;
			lineTipX[i]=0;
			lineTipY[i]=0;
			labelOrientation[i] = MesquiteInteger.unassigned;
		}
	}


	/** Sets the taxa.  This is done outside of a paint() call, and is the place that any complex calculations should be performed! */
	public abstract void recalculatePositions(Taxa taxa) ;

	/** asks if the point is within "distance" of the location of the taxon */
	public boolean locationNearTaxon(int it, int distance, int x, int y){
		if (it<0 || it>=numTaxa)
			return false;
		return (Math.abs(this.x[it]-x)<=distance && Math.abs(this.y[it]-y)<=distance );
	}

	/** Draw taxa in graphics context */
	public abstract void drawTaxa(Taxa taxa, Graphics g) ;
	
	/** Put in a request to enable terminal boxes (this allows taxon names to be drawn to leave room)*/
	public void incrementEnableTerminalBoxes(){
		enableTerminalBoxesRequests++;
	}
	/** Withdraw request to enable terminal boxes (this allows taxon names to be drawn to leave room)*/
	public void decrementEnableTerminalBoxes(){
		enableTerminalBoxesRequests--;
		if (enableTerminalBoxesRequests<0)
			enableTerminalBoxesRequests=0;
	}
	/** Returns if terminal boxes requested*/
	public boolean terminalBoxesRequested(){
		return (enableTerminalBoxesRequests>0);
	}
	/** Fill terminal box with current color. */
	public abstract void fillTerminalBox(Taxa taxa, int node, Graphics g);
	
	/** Fill terminal box of node "node" with indicated set of colors */
	public abstract void fillTerminalBoxWithColors(Taxa taxa, int node, ColorDistribution colors, Graphics g);
	
	/** Find which terminal box is at x,y */
	public abstract int findTerminalBox(Taxa taxa, int x, int y);
	
	

	
	public void setHighlightsOn(boolean on){
		/*
		if (on) {
			if (highlightThread==null) 
				highlightThread = new HighlightThread(taxaDisplay);
			if (!highlightThread.isOn())
				highlightThread.start();
		}
		else {
			if (highlightThread!=null) {
				highlightThread.stop();
				highlightThread.dispose();
			}
			highlightThread = null;
		}
		*/
		
	}
	public boolean getHighlightsOn(){
		return false;
		//return (highlightThread!=null);
	}
	public void dispose(){
		taxaDisplay = null;
		setHighlightsOn(false);
	}
	public void finalize() throws Throwable {
		setHighlightsOn(false);
		super.finalize();
	}
}


