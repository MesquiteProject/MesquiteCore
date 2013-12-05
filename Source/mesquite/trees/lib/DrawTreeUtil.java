/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.trees.lib;
import mesquite.lib.*;

import java.awt.*;
import java.awt.geom.*;


public class DrawTreeUtil {

	private static int getOffset(int width, int edgeWidth) {
		return (width-edgeWidth)/2;
	}
	/*_________________________________________________*/
	public static void UPdefineDiagonalPoly(TreeDrawing treeDrawing, Polygon poly, int width, boolean internalNode, int Nx, int Ny, int mNx, int mNy) {
		if (poly!=null) {
			Nx -= getOffset(width,treeDrawing.getEdgeWidth());
			mNx -= getOffset(width,treeDrawing.getEdgeWidth());
			if (internalNode)  {
				poly.npoints=0;
				poly.addPoint(Nx, Ny);
				poly.addPoint(Nx+width/2, Ny-width/2);//Ny+width/2 for down
				poly.addPoint(Nx+width, Ny);
				poly.addPoint(mNx+width, mNy);
				poly.addPoint(mNx, mNy);
				poly.addPoint(Nx, Ny);
				poly.npoints=6;
			}
			else {
				poly.npoints=0;
				poly.addPoint(Nx, Ny);
				poly.addPoint(Nx+width, Ny);
				poly.addPoint(mNx+width, mNy);
				poly.addPoint(mNx, mNy);
				poly.addPoint(Nx, Ny);
				poly.npoints=5;
			}
		}
	}
	/*_________________________________________________*/
	public static void DOWNdefineDiagonalPoly(TreeDrawing treeDrawing, Polygon poly, int width, boolean internalNode, int Nx, int Ny, int mNx, int mNy) {
		if (poly!=null){
			Nx -= getOffset(width,treeDrawing.getEdgeWidth());
			mNx -= getOffset(width,treeDrawing.getEdgeWidth());
			if (internalNode) 
			{
				poly.npoints=0;
				poly.addPoint(Nx, Ny);
				poly.addPoint(Nx+width/2, Ny+width/2);
				poly.addPoint(Nx+width, Ny);
				poly.addPoint(mNx+width, mNy);
				poly.addPoint(mNx, mNy);
				poly.addPoint(Nx, Ny);
				poly.npoints=6;
			}
			else
			{
				poly.npoints=0;
				poly.addPoint(Nx, Ny);
				poly.addPoint(Nx+width, Ny);
				poly.addPoint(mNx+width, mNy);
				poly.addPoint(mNx, mNy);
				poly.addPoint(Nx, Ny);
				poly.npoints=5;
			}
		}
	}
	/*_________________________________________________*/
	public static void RIGHTdefineDiagonalPoly(TreeDrawing treeDrawing, Polygon poly,int width, boolean internalNode, int Nx, int Ny, int mNx, int mNy) {
		if (poly!=null) {
			Ny -= getOffset(width,treeDrawing.getEdgeWidth());
			mNy -= getOffset(width,treeDrawing.getEdgeWidth());
			if (internalNode) 
			{
				poly.npoints=0;
				poly.addPoint(Nx, Ny);
				poly.addPoint(Nx+width/2, Ny+width/2);
				poly.addPoint(Nx, Ny+width);
				poly.addPoint(mNx, mNy+width);
				poly.addPoint(mNx, mNy);
				poly.addPoint(Nx, Ny);
				poly.npoints=6;
			}
			else
			{
				poly.npoints=0;
				poly.addPoint(Nx, Ny);
				poly.addPoint(Nx, Ny+width);
				poly.addPoint(mNx, mNy+width);
				poly.addPoint(mNx, mNy);
				poly.addPoint(Nx, Ny);
				poly.npoints=5;
			}
		}
	}
	/*_________________________________________________*/
	public static void LEFTdefineDiagonalPoly(TreeDrawing treeDrawing, Polygon poly, int width, boolean internalNode, int Nx, int Ny, int mNx, int mNy) {
		if (poly!=null) {
			Ny -= getOffset(width,treeDrawing.getEdgeWidth());
			mNy -= getOffset(width,treeDrawing.getEdgeWidth());
			if (internalNode) 
			{
				poly.npoints=0;
				poly.addPoint(Nx, Ny);
				poly.addPoint(Nx-width/2, Ny+width/2);
				poly.addPoint(Nx, Ny+width);
				poly.addPoint(mNx, mNy+width);
				poly.addPoint(mNx, mNy);
				poly.addPoint(Nx, Ny);
				poly.npoints=6;
			}
			else
			{
				poly.npoints=0;
				poly.addPoint(Nx, Ny);
				poly.addPoint(Nx, Ny+width);
				poly.addPoint(mNx, mNy+width);
				poly.addPoint(mNx, mNy);
				poly.addPoint(Nx, Ny);
				poly.npoints=5;
			}
		}
	}
	/*_________________________________________________*/
	public static void UPdefineSquarePoly(TreeDrawing treeDrawing, Polygon poly, int width, boolean isRoot, int Nx, int Ny, int mNx, int mNy, int nShortcut) {
		Nx -= getOffset(width, treeDrawing.getEdgeWidth());
		mNx -= getOffset(width, treeDrawing.getEdgeWidth());
		mNy -= getOffset(width, treeDrawing.getEdgeWidth());
		if (isRoot) {
			poly.npoints=0;
			poly.addPoint(Nx, Ny); //root left
			poly.addPoint(Nx+width, Ny);	 //root right
			poly.addPoint(Nx+width, mNy); //subroot right
			poly.addPoint(Nx, mNy); //subroot let
			poly.addPoint(Nx, Ny); //return to root left
			poly.npoints=4;
		}
		else if (Nx<mNx) //left leaning (*)
		{
			poly.npoints=0;
			poly.addPoint(Nx, Ny); // daughter left
			poly.addPoint(Nx+width, Ny);	//daughter right 
			poly.addPoint(Nx+width, mNy -  nShortcut); //corner right
			poly.addPoint(mNx+width, mNy); //mother up
			poly.addPoint(mNx, mNy+width); //mother down
			poly.addPoint(Nx, mNy+width -  nShortcut); //corner left
			poly.addPoint(Nx, Ny); //return to daughter left
			poly.npoints=7;
		}
		else //right leaning (*)
		{
			poly.npoints=0;
			poly.addPoint(Nx, Ny);// daughter left
			poly.addPoint(Nx+width, Ny);// daughter right
			poly.addPoint(Nx+width, mNy+width -  nShortcut);// corner right
			poly.addPoint(mNx+width, mNy+width); //mother down
			poly.addPoint(mNx, mNy); //mother up
			poly.addPoint(Nx, mNy -  nShortcut); //corner left
			poly.addPoint(Nx, Ny); //return to daughter left
			poly.npoints=7;
		}
	}
	/*_________________________________________________*/
	//makes polygon counterclockwise
	public static void DOWNdefineSquarePoly(TreeDrawing treeDrawing, Polygon poly, int width, boolean isRoot, int Nx, int Ny, int mNx, int mNy, int nShortcut) {
		Nx -= getOffset(width, treeDrawing.getEdgeWidth());
		mNx -= getOffset(width, treeDrawing.getEdgeWidth());
		mNy += getOffset(width, treeDrawing.getEdgeWidth());
		if (isRoot) {
			poly.npoints=0;
			poly.addPoint(Nx, Ny); // root right
			poly.addPoint(Nx+width, Ny);	//root left
			poly.addPoint(Nx+width, mNy);//subroot left
			poly.addPoint(Nx, mNy); //subroot right
			poly.addPoint(Nx, Ny); //return to root right
			poly.npoints=4;
		}
		else if (Nx>mNx) //left leaning
		{
			poly.npoints=0;
			poly.addPoint(Nx, Ny); //daughter right
			poly.addPoint(Nx+width, Ny);//daughter left
			poly.addPoint(Nx+width, mNy-width + nShortcut);//corner left
			poly.addPoint(mNx+width, mNy-width); //mother down * on x
			poly.addPoint(mNx, mNy); //mother up 
			poly.addPoint(Nx, mNy + nShortcut); //corner right
			poly.addPoint(Nx, Ny); //return to daughter right
			poly.npoints=7;
		}
		else //right leaning
		{
			poly.npoints=0;
			poly.addPoint(Nx, Ny); // daughter right
			poly.addPoint(Nx+width, Ny);	//daughter left
			poly.addPoint(Nx+width, mNy + nShortcut);//corner left
			poly.addPoint(mNx+width, mNy); //mother up * on x
			poly.addPoint(mNx, mNy-width); //mother down 
			poly.addPoint(Nx, mNy-width + nShortcut); //corner right
			poly.addPoint(Nx, Ny); //return to daughter right
			poly.npoints=7;
		}
	}
	/*_________________________________________________*/
	//makes polygon clockwise
	public static void RIGHTdefineSquarePoly(TreeDrawing treeDrawing, Polygon poly, int width, boolean isRoot, int Nx, int Ny, int mNx, int mNy, int nShortcut) {
		Ny -= getOffset(width, treeDrawing.getEdgeWidth());
		mNy -= getOffset(width, treeDrawing.getEdgeWidth());
		mNx += getOffset(width, treeDrawing.getEdgeWidth());
		if (isRoot) {
			poly.npoints=0;
			poly.addPoint(Nx, Ny); // root left
			poly.addPoint(Nx, Ny+width);	//root right
			poly.addPoint(mNx, Ny+width);//subroot right
			poly.addPoint(mNx, Ny); //subroot left
			poly.addPoint(Nx, Ny); //return to root left
			poly.npoints=4;
		}
		else if (Ny<mNy) //leans left
		{
			poly.npoints=0;
			poly.addPoint(Nx, Ny); // daughter left
			poly.addPoint(Nx, Ny+width);	//daughter right
			poly.addPoint(mNx + nShortcut, Ny+width);//corner right
			poly.addPoint(mNx, mNy+width); //mother up * on y
			poly.addPoint(mNx-width, mNy); //mother down
			poly.addPoint(mNx-width + nShortcut, Ny); //corner left
			poly.addPoint(Nx, Ny); //return to daughter left
			poly.npoints=7;
		}
		else
		{
			poly.npoints=0;
			poly.addPoint(Nx, Ny);// daughter left
			poly.addPoint(Nx, Ny+width);//daughter right
			poly.addPoint(mNx-width + nShortcut, Ny+width);//corner right
			poly.addPoint(mNx-width, mNy+width); //mother down * on y
			poly.addPoint(mNx, mNy); //mother up 
			poly.addPoint(mNx + nShortcut, Ny); //corner left
			poly.addPoint(Nx, Ny); //return to daughter left
			poly.npoints=7;
		}

	}
	/*_________________________________________________*/
	//makes polygon counterclockwise
	public static void LEFTdefineSquarePoly(TreeDrawing treeDrawing,Polygon poly, int width, boolean isRoot, int Nx, int Ny, int mNx, int mNy, int nShortcut) {
		Ny -= getOffset(width, treeDrawing.getEdgeWidth());
		mNy -= getOffset(width, treeDrawing.getEdgeWidth());
		mNx -= getOffset(width, treeDrawing.getEdgeWidth());
		if (isRoot) {
			poly.npoints=0;
			poly.addPoint(Nx, Ny); // root right
			poly.addPoint(Nx, Ny+width);	//root left
			poly.addPoint(mNx, Ny+width);//subroot left
			poly.addPoint(mNx, Ny); //subroot right
			poly.addPoint(Nx, Ny); //return to root right
			poly.npoints=5;
		}
		else if (Ny>mNy) //left leaning
		{
			poly.npoints=0;
			poly.addPoint(Nx, Ny); // daughter right
			poly.addPoint(Nx, Ny+width);	//daughter left
			poly.addPoint(mNx+width -  nShortcut, Ny+width);//corner left  
			poly.addPoint(mNx+width, mNy+width); //mother down
			poly.addPoint(mNx, mNy); //mother up
			poly.addPoint(mNx -  nShortcut, Ny); //corner right
			poly.addPoint(Nx, Ny); //return to daughter right
			poly.npoints=7;
		}
		else
		{
			poly.npoints=0;
			poly.addPoint(Nx, Ny);// daughter right
			poly.addPoint(Nx, Ny+width); //daughter left
			poly.addPoint(mNx -  nShortcut, Ny+width);//corner left
			poly.addPoint(mNx, mNy+width); //mother up
			poly.addPoint(mNx+width, mNy);//mother down
			poly.addPoint(mNx+width -  nShortcut, Ny); //corner right
			poly.addPoint(Nx, Ny); //return to daughter right
			poly.npoints=7;
		}
	}

	/*_________________________________________________*/
	public static void drawOneCurvedBranch(TreeDisplay treeDisplay, int[] x, int[] y, int edgewidth, Tree tree, Graphics g, int node, int start, int width, int adj, boolean emphasizeNodes, Polygon nodePoly, BasicStroke defaultStroke) {
		if (tree.nodeExists(node)) {
			int nM = tree.motherOfNode(node);
			int xN=x[node];
			int xnM = x[nM];
			int yN =y[node];
			int ynM = y[nM];
			boolean done = false;
			try{
				if ( g instanceof Graphics2D) {
					if (treeDisplay.getOrientation()==TreeDisplay.UP) {
						if (xnM>xN){ //leans left
							xN += width/2+start;
							xnM += width/2;
							ynM += edgewidth - width/2 -start;
							yN += width/2;
						}
						else {
							if (start>1)
								start++;
							xN += width/2+start;
							xnM += width/2;
							ynM += width/2 +start;
							yN += width/2;
						}

					}
					else if (treeDisplay.getOrientation()==TreeDisplay.DOWN){ //����
						if (xnM>xN){ //leans left
							xN += width/2+start;
							xnM += width/2;
							ynM -= edgewidth - width/2 -start;
							yN -= width/2;
						}
						else {
							if (start>1)
								start++;
							xN += width/2+start;
							xnM += width/2;
							ynM -= width/2 +start;
							yN -= width/2;
						}
					}
					else  if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
						if (ynM>yN){ //leans left
							yN += width/2+start;
							ynM += width/2;
							xnM -= edgewidth - width/2 -start;
							xN -= width/2;
						}
						else {
							if (start>1)
								start++;
							yN += width/2+start;
							ynM += width/2;
							xnM -= width/2 +start;
							xN -= width/2;
						}

					}
					else  if (treeDisplay.getOrientation()==TreeDisplay.LEFT){  //����
						if (ynM>yN){ //leans right
							yN += width/2+start;
							ynM += width/2;
							xnM += edgewidth - width/2 -start;
							xN += width/2;
						}
						else {
							if (start>1)
								start++;
							yN += width/2+start;
							ynM += width/2;
							xnM += width/2 +start;
							xN += width/2;
						}
					}
					Arc2D.Double arc = null;
					if (treeDisplay.getOrientation()==TreeDisplay.UP) {
						if (xnM>xN) {  //leans left
							//g.setColor(Color.blue);
							arc = new Arc2D.Double(xN, yN-(ynM-yN), (xnM-xN)*2,  (ynM - yN)*2, 180, 90, Arc2D.OPEN); // left
							//g.drawRect(xN, yN-(ynM-yN), (xnM-xN)*2,  (ynM - yN)*2);
						}
						else {
							//g.setColor(Color.green);
							arc = new Arc2D.Double(xnM-(xN-xnM), yN - (ynM - yN), (xN-xnM)*2,  (ynM - yN)*2, 0, -90, Arc2D.OPEN); //right
							//g.drawRect(xnM-(xN-xnM), yN - (ynM - yN), (xN-xnM)*2,  (ynM - yN)*2);
						}
					}

					else if (treeDisplay.getOrientation()==TreeDisplay.DOWN){//����
						if (xnM>xN) {  //leans right
							//g.setColor(Color.blue);
							arc = new Arc2D.Double(xN, ynM, (xnM-xN)*2,  -(ynM - yN)*2, 90, 90, Arc2D.OPEN); // left
							//g.drawRect(xN, yN-(ynM-yN), (xnM-xN)*2,  (ynM - yN)*2);
						}
						else {
							//g.setColor(Color.green);
							arc = new Arc2D.Double(xnM-(xN-xnM), ynM, (xN-xnM)*2,  -(ynM - yN)*2, 0, 90, Arc2D.OPEN); //right
							//g.drawRect(xnM-(xN-xnM), yN - (ynM - yN), (xN-xnM)*2,  (ynM - yN)*2);
						}
					}
					else  if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
						if (ynM>yN) { //leans left
							//g.setColor(Color.blue);
							arc = new Arc2D.Double(xnM, yN, (xN-xnM)*2,  (ynM - yN)*2, 90, 90, Arc2D.OPEN); // left
							//g.drawRect(xN, yN-(ynM-yN), (xnM-xN)*2,  (ynM - yN)*2);
						}
						else {
							//g.setColor(Color.green);
							arc = new Arc2D.Double(xnM, ynM + (ynM - yN), (xN-xnM)*2,  -(ynM - yN)*2, 180,90, Arc2D.OPEN); //right
							//g.drawRect(xnM-(xN-xnM), yN - (ynM - yN), (xN-xnM)*2,  (ynM - yN)*2);
						}
					}
					else  if (treeDisplay.getOrientation()==TreeDisplay.LEFT){ //����
						if (ynM>yN) { //leans right
							//g.setColor(Color.blue);
							arc = new Arc2D.Double(xN - (xnM-xN), yN, -(xN-xnM)*2,  (ynM - yN)*2, 0, 90, Arc2D.OPEN); 
							//g.drawRect(xN, yN-(ynM-yN), (xnM-xN)*2,  (ynM - yN)*2);
						}
						else {
							//g.setColor(Color.green);
							arc = new Arc2D.Double(xN - (xnM-xN), ynM + (ynM - yN), -(xN-xnM)*2, - (ynM - yN)*2, 0,-90, Arc2D.OPEN); 
							//g.drawRect(xnM-(xN-xnM), yN - (ynM - yN), (xN-xnM)*2,  (ynM - yN)*2);
						}
					}
					if (arc!=null) {
						BasicStroke wideStroke = new BasicStroke(width);
						Graphics2D g2 = (Graphics2D)g;
						g2.setStroke(wideStroke);
						g2.draw(arc);
						done  = true;
						g2.setStroke(defaultStroke);
					}
				}

			}
			catch (Throwable t){
			}
			if (!done){
				if (treeDisplay.getOrientation()==TreeDisplay.UP) {
					if (xnM > xN)  ynM += edgewidth-1-start;
					else ynM+=start;
				}
				else if (treeDisplay.getOrientation()==TreeDisplay.DOWN){ //����
					if (xnM > xN)  ynM -= edgewidth-1-start;
					else ynM-=start;
					xnM +=adj; //why this adj is needed, I don't know.  But it seems to work.
					xN += adj;
				}
				else  if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
					if (ynM > yN)  xnM -= edgewidth-1-start;
					else xnM-=start;
				}
				else  if (treeDisplay.getOrientation()==TreeDisplay.LEFT){  //����
					if (ynM > yN) xnM += edgewidth-1-start;
					else xnM+=start;
					ynM +=adj;//why this adj is needed, I don't know.  But it seems to work.
					yN += adj;
				}
				else
					System.out.println("Error: wrong tree orientation in Arc Tree");
				for (int i=0; i<width; i++) {
					if (treeDisplay.getOrientation()==TreeDisplay.UP) {
						if (xnM>xN) {
							g.drawArc(xN + start, yN - (ynM - yN), (xnM-xN)*2,  (ynM - yN)*2, 180, 90); // left
							ynM--;
						}
						else {
							g.drawArc(xnM-(xN-xnM) + start, yN - (ynM - yN), (xN-xnM)*2,  (ynM - yN)*2, 0, -90); //right
							ynM++; //** start off -

						}
						xN++;
					}

					else if (treeDisplay.getOrientation()==TreeDisplay.DOWN){//����
						if (xnM>xN) {
							g.drawArc(xN - start,ynM, (xnM-xN)*2,  (yN -ynM)*2, 90, 90); //right
							ynM++;
						}
						else {
							g.drawArc(xnM-(xN-xnM) - start,ynM, (xN-xnM)*2,   (yN -ynM)*2, 0, 90); //left 
							ynM--;  //**start off + edgewidth
						}
						xN++;
					}
					else  if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
						if (ynM>yN) {
							g.drawArc(xnM, yN + start, (xN-xnM)*2,  (ynM - yN)*2, 90, 90);  //left
							xnM++;
						}
						else {
							g.drawArc(xnM,ynM - (yN -ynM) + start, (xN-xnM)*2,  (yN -ynM)*2, 180, 90);  //right 
							xnM--;  //start off + edgewidth
						}
						yN++;
					}
					else  if (treeDisplay.getOrientation()==TreeDisplay.LEFT){ //����
						if (ynM>yN) {
							g.drawArc(xN - (xnM-xN), yN - start, (xnM-xN)*2,  (ynM - yN)*2, 0, 90);  //right
							xnM--;
						}
						else {
							g.drawArc(xN - (xnM-xN),ynM - (yN -ynM) - start, (xnM-xN)*2,  (yN -ynM)*2, 0, -90);  //left 
							xnM++;  //start off - edgewidth
						}
						yN++;
					}

				}
			}

			if (emphasizeNodes && nodePoly!=null) {
				Color prev = g.getColor();
				g.setColor(Color.red);//for testing
				g.fillPolygon(nodePoly);
				g.setColor(prev);
			}
		}
	}

	/*_________________________________________________*/
	public static void drawOneSquareLineBranch(TreeDisplay treeDisplay, int[] x, int[] y, int edgewidth, Tree tree, Graphics g, int node, float start, float width, int adj, boolean emphasizeNodes, Polygon nodePoly, BasicStroke defaultStroke) {
		if (tree.nodeExists(node)) {
			int nM = tree.motherOfNode(node);
			int xN=x[node];
			int xnM = x[nM];
			int yN =y[node];
			int ynM = y[nM];
			int halfEdge = edgewidth/2;
			if ( g instanceof Graphics2D) {
				BasicStroke wideStroke = new BasicStroke(width);
				Graphics2D g2 = (Graphics2D)g;
				g2.setStroke(wideStroke);
				if (treeDisplay.getOrientation()==TreeDisplay.UP) {
					g2.drawLine(xN+halfEdge,yN,xN+halfEdge,ynM);
					g2.drawLine(xN+halfEdge,ynM,xnM,ynM);
				}
				else if (treeDisplay.getOrientation()==TreeDisplay.DOWN){ //����
					g2.drawLine(xN+halfEdge,yN,xN+halfEdge,ynM);
					g2.drawLine(xN+halfEdge,ynM,xnM,ynM);
				}
				else  if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
					g2.drawLine(xN,yN+halfEdge,xnM,yN+halfEdge);
					g2.drawLine(xnM,yN+halfEdge,xnM,ynM);

				}
				else  if (treeDisplay.getOrientation()==TreeDisplay.LEFT){  //����
					g2.drawLine(xN,yN+halfEdge,xnM,yN+halfEdge);
					g2.drawLine(xnM,yN+halfEdge,xnM,ynM);
				}
				g2.setStroke(defaultStroke);
			}

			if (emphasizeNodes && nodePoly!=null) {
				Color prev = g.getColor();
				g.setColor(Color.red);//for testing
				g.fillPolygon(nodePoly);
				g.setColor(prev);
			}
		}
	}


	/*_________________________________________________*/
	public static boolean inSquareLineBranch(TreeDisplay treeDisplay, int[] x, int[] y, int edgewidth, Tree tree, int node, int h, int v) {
		if (tree.nodeExists(node)) {
			int nM = tree.motherOfNode(node);
			int xN=x[node];
			int xnM = x[nM];
			int yN =y[node];
			int ynM = y[nM];
			int halfEdgewidth = edgewidth/2;

			if (treeDisplay.getOrientation()==TreeDisplay.UP) {
				if ((h>=xN) && (h<=xN+edgewidth) && (v>=yN) && (v<=ynM))  //with vertical part of branch
					return true;
				if (xnM>xN) {  // mother is to the right of node
					if ((h>=xN) && (h<=xnM) && (v>=ynM-halfEdgewidth) && (v<=ynM+halfEdgewidth))  //with horizontal part of branch
						return true;
				}
				else {
					if ((h>=xnM) && (h<=xN) && (v>=ynM-halfEdgewidth) && (v<=ynM+halfEdgewidth))  //with horizontal part of branch
						return true;
				}
			}

			else if (treeDisplay.getOrientation()==TreeDisplay.DOWN){
				if ((h>=xN) && (h<=xN+edgewidth) && (v>=ynM) && (v<=yN))  //with vertical part of branch
					return true;
				if (xnM>xN) {  // mother is to the right of node
					if ((h>=xN) && (h<=xnM) && (v>=ynM-halfEdgewidth) && (v<=ynM+halfEdgewidth))  //with horizontal part of branch
						return true;
				}
				else {
					if ((h>=xnM) && (h<=xN) && (v>=ynM-halfEdgewidth) && (v<=ynM+halfEdgewidth))  //with horizontal part of branch
						return true;
				}
			}
			else  if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
				if ((v>=yN) && (v<=yN+edgewidth) && (h>=xnM) && (h<=xN))  //with horizontal part of branch
					return true;
				if (ynM>yN) {  // mother is below node
					if ((v>=yN) && (v<=ynM) && (h>=xnM-halfEdgewidth) && (h<=xnM+halfEdgewidth))  //with vertical part of branch
						return true;
				}
				else {
					if ((v>=ynM) && (v<=yN) && (h>=xnM-halfEdgewidth) && (h<=xnM+halfEdgewidth))  //with vertical part of branch
						return true;
				}
			}
			else  if (treeDisplay.getOrientation()==TreeDisplay.LEFT){ 
				if ((v>=yN) && (v<=yN+edgewidth) && (h>=xN) && (h<=xnM))  //with horizontal part of branch
					return true;
				if (ynM>yN) { // mother is below node
					if ((v>=yN) && (v<=ynM) && (h>=xnM-halfEdgewidth) && (h<=xnM+halfEdgewidth))  //with vertical part of branch
						return true;
				}
				else {
					if ((v>=ynM) && (v<=yN) && (h>=xnM-halfEdgewidth) && (h<=xnM+halfEdgewidth))  //with vertical part of branch
						return true;
				}
			}

		}
		return false;
	}




	/*_________________________________________________*/
	public static boolean inBranch(TreeDisplay treeDisplay, int[] x, int[] y, int edgewidth, Tree tree, int node, int h, int v) {
		if (tree.nodeExists(node)) {
			int nM = tree.motherOfNode(node);
			int xN=x[node];
			int xnM = x[nM];
			int yN =y[node];
			int ynM = y[nM];
			double centerX, centerY,axisX, axisY;
			centerX =  centerY =  axisX =   axisY =0;

			if (treeDisplay.getOrientation()==TreeDisplay.UP) {
				if (xnM>xN) {
					if (h< xN || h>xnM)
						return false;
					centerX = xnM;
					centerY = yN;
					axisX =  xnM-xN;
					axisY =ynM + edgewidth - yN;
				}
				else {
					if (h< xnM || h>xN+ edgewidth)
						return false;
					centerX = xnM;
					centerY = yN;
					axisX =  xN-xnM+ edgewidth;
					axisY =ynM + edgewidth - yN;
				}
				if (v < yN || v> ynM + edgewidth)
					return false;
			}

			else if (treeDisplay.getOrientation()==TreeDisplay.DOWN){
				if (xnM>xN) {
					if (h< xN || h>xnM)
						return false;
					centerX = xnM;
					centerY = yN;
					axisX =  xnM-xN;
					axisY =yN - ynM + edgewidth;
				}
				else {
					if (h< xnM || h>xN+ edgewidth)
						return false;
					centerX = xnM;
					centerY = yN;
					axisX =  xN-xnM+ edgewidth;
					axisY =yN - ynM + edgewidth;
				}
				if (v < ynM || v> yN)
					return false;
			}
			else  if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
				if (ynM>yN) {
					if (v< yN || v>ynM)
						return false;
					centerX = xN;
					centerY = ynM;
					axisX =  xN-xnM+ edgewidth;
					axisY =ynM - yN;
				}
				else {
					if (v< ynM || v>yN+ edgewidth)
						return false;
					centerX = xN;
					centerY = ynM;
					axisX =  xN-xnM+ edgewidth;
					axisY =yN - ynM+ edgewidth;
				}
				if (h < xnM-edgewidth || h> xN)
					return false;
			}
			else  if (treeDisplay.getOrientation()==TreeDisplay.LEFT){ 
				if (ynM>yN) {
					if (v< yN- edgewidth || v>ynM)
						return false;
					centerX = xN;
					centerY = ynM;
					axisX =  xnM-xN+ edgewidth;
					axisY =ynM - yN+ edgewidth;
				}
				else {
					if (v< ynM || v>yN)
						return false;
					centerX = xN;
					centerY = ynM;
					axisX =  xnM-xN+ edgewidth;
					axisY =yN - ynM;
				}
				if (h < xN || h> xnM+ edgewidth)
					return false;
			}

			if ((h-centerX)*(h-centerX)/(axisX*axisX) + (v-centerY)*(v-centerY)/(axisY*axisY) <= 1.0)  //inside outer edge
				if ((h-centerX)*(h-centerX)/((axisX-edgewidth)*(axisX-edgewidth)) + (v-centerY)*(v-centerY)/((axisY-edgewidth)*(axisY-edgewidth)) > 1.0){ //outside inner edge
					return true;
				}
		}
		return false;
	}



}
