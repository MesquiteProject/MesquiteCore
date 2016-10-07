/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


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
	public static void UPdefineDiagonalPoly(TreeDrawing treeDrawing, Path2D poly, int width, boolean internalNode, double Nx, double Ny, double mNx, double mNy) {
		if (poly!=null) {
			Nx -= getOffset(width,treeDrawing.getEdgeWidth());
			mNx -= getOffset(width,treeDrawing.getEdgeWidth());
			poly.reset();
			if (internalNode)  {
				poly.moveTo(Nx, Ny);
				poly.lineTo(Nx+width/2, Ny-width/2);//Ny+width/2 for down
				poly.lineTo(Nx+width, Ny);
				poly.lineTo(mNx+width, mNy);
				poly.lineTo(mNx, mNy);
				poly.lineTo(Nx, Ny);
			}
			else {
				poly.moveTo(Nx, Ny);
				poly.lineTo(Nx+width, Ny);
				poly.lineTo(mNx+width, mNy);
				poly.lineTo(mNx, mNy);
				poly.lineTo(Nx, Ny);
			}
		}
	}
	/*_________________________________________________*/
	public static void DOWNdefineDiagonalPoly(TreeDrawing treeDrawing, Path2D poly, int width, boolean internalNode, double Nx, double Ny, double mNx, double mNy) {
		if (poly!=null){
			Nx -= getOffset(width,treeDrawing.getEdgeWidth());
			mNx -= getOffset(width,treeDrawing.getEdgeWidth());
			poly.reset();
			if (internalNode) 
			{
				poly.moveTo(Nx, Ny);
				poly.lineTo(Nx+width/2, Ny+width/2);
				poly.lineTo(Nx+width, Ny);
				poly.lineTo(mNx+width, mNy);
				poly.lineTo(mNx, mNy);
				poly.lineTo(Nx, Ny);
			}
			else
			{
				poly.moveTo(Nx, Ny);
				poly.lineTo(Nx+width, Ny);
				poly.lineTo(mNx+width, mNy);
				poly.lineTo(mNx, mNy);
				poly.lineTo(Nx, Ny);
			}
		}
	}
	/*_________________________________________________*/
	public static void RIGHTdefineDiagonalPoly(TreeDrawing treeDrawing, Path2D poly,int width, boolean internalNode, double Nx, double Ny, double mNx, double mNy) {
		if (poly!=null) {
			Ny -= getOffset(width,treeDrawing.getEdgeWidth());
			mNy -= getOffset(width,treeDrawing.getEdgeWidth());
			poly.reset();
			if (internalNode) 
			{
				poly.moveTo(Nx, Ny);
				poly.lineTo(Nx+width/2, Ny+width/2);
				poly.lineTo(Nx, Ny+width);
				poly.lineTo(mNx, mNy+width);
				poly.lineTo(mNx, mNy);
				poly.lineTo(Nx, Ny);
			}
			else
			{
				poly.moveTo(Nx, Ny);
				poly.lineTo(Nx, Ny+width);
				poly.lineTo(mNx, mNy+width);
				poly.lineTo(mNx, mNy);
				poly.lineTo(Nx, Ny);
			}
		}
	}
	/*_________________________________________________*/
	public static void LEFTdefineDiagonalPoly(TreeDrawing treeDrawing, Path2D poly, int width, boolean internalNode, double Nx, double Ny, double mNx, double mNy) {
		if (poly!=null) {
			Ny -= getOffset(width,treeDrawing.getEdgeWidth());
			mNy -= getOffset(width,treeDrawing.getEdgeWidth());
			poly.reset();
			if (internalNode) 
			{
				poly.moveTo(Nx, Ny);
				poly.lineTo(Nx-width/2, Ny+width/2);
				poly.lineTo(Nx, Ny+width);
				poly.lineTo(mNx, mNy+width);
				poly.lineTo(mNx, mNy);
				poly.lineTo(Nx, Ny);
			}
			else
			{
				poly.moveTo(Nx, Ny);
				poly.lineTo(Nx, Ny+width);
				poly.lineTo(mNx, mNy+width);
				poly.lineTo(mNx, mNy);
				poly.lineTo(Nx, Ny);
			}
		}
	}
	/*_________________________________________________*/
	public static void UPdefineSquarePoly(TreeDrawing treeDrawing, Path2D poly, int width, boolean isRoot, double Nx, double Ny, double mNx, double mNy, double nShortcut) {
		Nx -= getOffset(width, treeDrawing.getEdgeWidth());
		mNx -= getOffset(width, treeDrawing.getEdgeWidth());
		mNy -= getOffset(width, treeDrawing.getEdgeWidth());
		poly.reset();
		if (isRoot) {
			poly.moveTo(Nx, Ny); //root left
			poly.lineTo(Nx+width, Ny);	 //root right
			poly.lineTo(Nx+width, mNy); //subroot right
			poly.lineTo(Nx, mNy); //subroot let
			poly.lineTo(Nx, Ny); //return to root left
		}
		else if (Nx<mNx) //left leaning (*)
		{
			poly.moveTo(Nx, Ny); // daughter left
			poly.lineTo(Nx+width, Ny);	//daughter right 
			poly.lineTo(Nx+width, mNy -  nShortcut); //corner right
			poly.lineTo(mNx+width, mNy); //mother up
			poly.lineTo(mNx, mNy+width); //mother down
			poly.lineTo(Nx, mNy+width -  nShortcut); //corner left
			poly.lineTo(Nx, Ny); //return to daughter left
		}
		else //right leaning (*)
		{
			poly.moveTo(Nx, Ny);// daughter left
			poly.moveTo(Nx+width, Ny);// daughter right
			poly.lineTo(Nx+width, mNy+width -  nShortcut);// corner right
			poly.lineTo(mNx+width, mNy+width); //mother down
			poly.lineTo(mNx, mNy); //mother up
			poly.lineTo(Nx, mNy -  nShortcut); //corner left
			poly.lineTo(Nx, Ny); //return to daughter left
		}
	}
	/*_________________________________________________*/
	//makes polygon counterclockwise
	public static void DOWNdefineSquarePoly(TreeDrawing treeDrawing, Path2D poly, int width, boolean isRoot, double Nx, double Ny, double mNx, double mNy, double nShortcut) {
		Nx -= getOffset(width, treeDrawing.getEdgeWidth());
		mNx -= getOffset(width, treeDrawing.getEdgeWidth());
		mNy += getOffset(width, treeDrawing.getEdgeWidth());
		poly.reset();
		if (isRoot) {
			poly.moveTo(Nx, Ny); // root right
			poly.lineTo(Nx+width, Ny);	//root left
			poly.lineTo(Nx+width, mNy);//subroot left
			poly.lineTo(Nx, mNy); //subroot right
			poly.lineTo(Nx, Ny); //return to root right
		}
		else if (Nx>mNx) //left leaning
		{
			poly.moveTo(Nx, Ny); //daughter right
			poly.lineTo(Nx+width, Ny);//daughter left
			poly.lineTo(Nx+width, mNy-width + nShortcut);//corner left
			poly.lineTo(mNx+width, mNy-width); //mother down * on x
			poly.lineTo(mNx, mNy); //mother up 
			poly.lineTo(Nx, mNy + nShortcut); //corner right
			poly.lineTo(Nx, Ny); //return to daughter right
		}
		else //right leaning
		{
			poly.moveTo(Nx, Ny); // daughter right
			poly.lineTo(Nx+width, Ny);	//daughter left
			poly.lineTo(Nx+width, mNy + nShortcut);//corner left
			poly.lineTo(mNx+width, mNy); //mother up * on x
			poly.lineTo(mNx, mNy-width); //mother down 
			poly.lineTo(Nx, mNy-width + nShortcut); //corner right
			poly.lineTo(Nx, Ny); //return to daughter right
		}
	}
	/*_________________________________________________*/
	//makes polygon clockwise
	public static void RIGHTdefineSquarePoly(TreeDrawing treeDrawing, Path2D poly, int width, boolean isRoot, double Nx, double Ny, double mNx, double mNy, double nShortcut) {
		Ny -= getOffset(width, treeDrawing.getEdgeWidth());
		mNy -= getOffset(width, treeDrawing.getEdgeWidth());
		mNx += getOffset(width, treeDrawing.getEdgeWidth());
		poly.reset();
		if (isRoot) {
			poly.moveTo(Nx, Ny); // root left
			poly.lineTo(Nx, Ny+width);	//root right
			poly.lineTo(mNx, Ny+width);//subroot right
			poly.lineTo(mNx, Ny); //subroot left
			poly.lineTo(Nx, Ny); //return to root left
		}
		else if (Ny<mNy) //leans left
		{
			poly.moveTo(Nx, Ny); // daughter left
			poly.lineTo(Nx, Ny+width);	//daughter right
			poly.lineTo(mNx + nShortcut, Ny+width);//corner right
			poly.lineTo(mNx, mNy+width); //mother up * on y
			poly.lineTo(mNx-width, mNy); //mother down
			poly.lineTo(mNx-width + nShortcut, Ny); //corner left
			poly.lineTo(Nx, Ny); //return to daughter left
		}
		else
		{
			poly.moveTo(Nx, Ny);// daughter left
			poly.lineTo(Nx, Ny+width);//daughter right
			poly.lineTo(mNx-width + nShortcut, Ny+width);//corner right
			poly.lineTo(mNx-width, mNy+width); //mother down * on y
			poly.lineTo(mNx, mNy); //mother up 
			poly.lineTo(mNx + nShortcut, Ny); //corner left
			poly.lineTo(Nx, Ny); //return to daughter left
		}

	}
	/*_________________________________________________*/
	//makes polygon counterclockwise
	public static void LEFTdefineSquarePoly(TreeDrawing treeDrawing,Path2D poly, int width, boolean isRoot, double Nx, double Ny, double mNx, double mNy, double nShortcut) {
		Ny -= getOffset(width, treeDrawing.getEdgeWidth());
		mNy -= getOffset(width, treeDrawing.getEdgeWidth());
		mNx -= getOffset(width, treeDrawing.getEdgeWidth());
		poly.reset();
		if (isRoot) {
			poly.moveTo(Nx, Ny); // root right
			poly.lineTo(Nx, Ny+width);	//root left
			poly.lineTo(mNx, Ny+width);//subroot left
			poly.lineTo(mNx, Ny); //subroot right
			poly.lineTo(Nx, Ny); //return to root right
		}
		else if (Ny>mNy) //left leaning
		{
			poly.moveTo(Nx, Ny); // daughter right
			poly.lineTo(Nx, Ny+width);	//daughter left
			poly.lineTo(mNx+width -  nShortcut, Ny+width);//corner left  
			poly.lineTo(mNx+width, mNy+width); //mother down
			poly.lineTo(mNx, mNy); //mother up
			poly.lineTo(mNx -  nShortcut, Ny); //corner right
			poly.lineTo(Nx, Ny); //return to daughter right
		}
		else
		{
			poly.moveTo(Nx, Ny);// daughter right
			poly.lineTo(Nx, Ny+width); //daughter left
			poly.lineTo(mNx -  nShortcut, Ny+width);//corner left
			poly.lineTo(mNx, mNy+width); //mother up
			poly.lineTo(mNx+width, mNy);//mother down
			poly.lineTo(mNx+width -  nShortcut, Ny); //corner right
			poly.lineTo(Nx, Ny); //return to daughter right
		}
	}

	/*_________________________________________________*/
	public static void drawOneCurvedBranch(TreeDisplay treeDisplay, double[] x, double[] y, int edgewidth, Tree tree, Graphics g, int node, int start, int width, int adj, boolean emphasizeNodes, Path2D nodePoly, BasicStroke defaultStroke) {
		if (tree.nodeExists(node)) {
			int nM = tree.motherOfNode(node);
			double xN=x[node];
			double xnM = x[nM];
			double yN =y[node];
			double ynM = y[nM];
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
						g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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
							GraphicsUtil.drawArc(g,xN + start, yN - (ynM - yN), (xnM-xN)*2,  (ynM - yN)*2, 180, 90); // left
							ynM--;
						}
						else {
							GraphicsUtil.drawArc(g,xnM-(xN-xnM) + start, yN - (ynM - yN), (xN-xnM)*2,  (ynM - yN)*2, 0, -90); //right
							ynM++; //** start off -

						}
						xN++;
					}

					else if (treeDisplay.getOrientation()==TreeDisplay.DOWN){//����
						if (xnM>xN) {
							GraphicsUtil.drawArc(g,xN - start,ynM, (xnM-xN)*2,  (yN -ynM)*2, 90, 90); //right
							ynM++;
						}
						else {
							GraphicsUtil.drawArc(g,xnM-(xN-xnM) - start,ynM, (xN-xnM)*2,   (yN -ynM)*2, 0, 90); //left 
							ynM--;  //**start off + edgewidth
						}
						xN++;
					}
					else  if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
						if (ynM>yN) {
							GraphicsUtil.drawArc(g,xnM, yN + start, (xN-xnM)*2,  (ynM - yN)*2, 90, 90);  //left
							xnM++;
						}
						else {
							GraphicsUtil.drawArc(g,xnM,ynM - (yN -ynM) + start, (xN-xnM)*2,  (yN -ynM)*2, 180, 90);  //right 
							xnM--;  //start off + edgewidth
						}
						yN++;
					}
					else  if (treeDisplay.getOrientation()==TreeDisplay.LEFT){ //����
						if (ynM>yN) {
							GraphicsUtil.drawArc(g,xN - (xnM-xN), yN - start, (xnM-xN)*2,  (ynM - yN)*2, 0, 90);  //right
							xnM--;
						}
						else {
							GraphicsUtil.drawArc(g,xN - (xnM-xN),ynM - (yN -ynM) - start, (xnM-xN)*2,  (yN -ynM)*2, 0, -90);  //left 
							xnM++;  //start off - edgewidth
						}
						yN++;
					}

				}
			}

			if (emphasizeNodes && nodePoly!=null) {
				Color prev = g.getColor();
				g.setColor(Color.red);//for testing
				if (g instanceof Graphics2D)
					((Graphics2D)g).fill(nodePoly);
				g.setColor(prev);
			}
		}
	}

	/*_________________________________________________*/
	public static void drawOneSquareLineBranch(TreeDisplay treeDisplay, double[] x, double[] y, int edgewidth, Tree tree, Graphics g, ColorDistribution colors, int node, float start, float width, int adj, boolean emphasizeNodes, Path2D nodePoly, BasicStroke defaultStroke) {
		if (width< 0)
			width = 0;
		if (tree.nodeExists(node)) {
			
			int nM = tree.motherOfNode(node);
			double xN=x[node];
			double xnM = x[nM];
			double yN =y[node];
			double ynM = y[nM];  // y position of mother of node
			float halfEdge = edgewidth/2;
			if ( g instanceof Graphics2D) {
				BasicStroke wideStroke = new BasicStroke(width);
				Graphics2D g2 = (Graphics2D)g;
				Stroke stroke = g2.getStroke();
				g2.setStroke(wideStroke);
				Shape line;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				if (treeDisplay.getOrientation()==TreeDisplay.UP) {
					if (yN!=ynM){
						line = new Line2D.Double(xN+halfEdge,yN+halfEdge,xN+halfEdge,ynM+halfEdge);
						g2.draw(line);
					}
					line = new Line2D.Double(xN+halfEdge,ynM+halfEdge,xnM+halfEdge,ynM+halfEdge);
					if (node!=tree.getRoot())
						g2.draw(line);
				}
				else if (treeDisplay.getOrientation()==TreeDisplay.DOWN){ 
					if (yN!=ynM){
						line = new Line2D.Double(xN+halfEdge,yN-halfEdge,xN+halfEdge,ynM-halfEdge);
						g2.draw(line);
					}
					line = new Line2D.Double(xN+halfEdge,ynM-halfEdge,xnM+halfEdge,ynM-halfEdge);
					if (node!=tree.getRoot())
						g2.draw(line);
				}
				else  if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
					if (xN!=xnM){
						line = new Line2D.Double(xN-halfEdge,yN+halfEdge,xnM-halfEdge,yN+halfEdge);  // draws the horizontal lines
						g2.draw(line);
					}
					line = new Line2D.Double(xnM-halfEdge,yN+halfEdge,xnM-halfEdge,ynM+halfEdge);  // draws the vertical lines
					if (node!=tree.getRoot())
						g2.draw(line);
				}
				else  if (treeDisplay.getOrientation()==TreeDisplay.LEFT){  
					if (xN!=xnM){
						line = new Line2D.Double(xN+halfEdge,yN+halfEdge,xnM+halfEdge,yN+halfEdge);
						g2.draw(line);
					}
					line = new Line2D.Double(xnM+halfEdge,yN+halfEdge,xnM+halfEdge,ynM+halfEdge);
					if (node!=tree.getRoot())
						g2.draw(line);
				}
				g2.setStroke(stroke);
			}

			if (emphasizeNodes && nodePoly!=null) {
				Color prev = g.getColor();
				g.setColor(Color.green);//for testing
				//	g.fillPolygon(nodePoly);
				GraphicsUtil.fillRect(g, x[node]-1, y[node]-1,2,2);
				g.setColor(prev);
			}
		}
	}

	/*_________________________________________________*/
	public static void fillOneSquareLineBranch(TreeDisplay treeDisplay, double[] x, double[] y, int edgewidth, Tree tree, Graphics g, ColorDistribution colors, int node, float start, float lineWidth, float inset, boolean emphasizeNodes, Path2D nodePoly, BasicStroke defaultStroke) {
		if (tree.nodeExists(node)) {
			int nM = tree.motherOfNode(node);
			double xN=x[node];
			double xnM = x[nM];
			double yN =y[node];
			double ynM = y[nM];  // y position of mother of node
			double xNHor, xnMHor, yNVert;
			double ynMVert;
			double halfLine = lineWidth/2;
			Shape line;
			if (g instanceof Graphics2D && lineWidth >=0) {
				BasicStroke wideStroke = new BasicStroke(lineWidth);
				Graphics2D g2 = (Graphics2D)g;
				Stroke stroke = g2.getStroke();
				g2.setStroke(wideStroke);
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			if (treeDisplay.getOrientation()==TreeDisplay.UP) {
					xNHor = xN+start+halfLine;
					if (xN>xnM){ // branch going to right
						ynMVert = ynM+start+halfLine;
						if (yN!=ynM){
							line = new Line2D.Double(xNHor,yN+halfLine+inset,xNHor,ynM-halfLine+start);  // draws the vertical lines
							g2.draw(line);
						}
						line = new Line2D.Double(xNHor,ynMVert,xnM+edgewidth,ynMVert);  // draws the horizontal lines
						if (node!=tree.getRoot())
							g2.draw(line);
					}
					else { // branch going to left
						ynMVert = ynM+edgewidth-start-halfLine;
						if (yN!=ynM){
							line = new Line2D.Double(xNHor,yN+halfLine+inset,xNHor,ynMVert);  // draws the vertical lines
							g2.draw(line);
						}
						line = new Line2D.Double(xNHor,ynMVert,xnM+halfLine,ynMVert);  // draws the horizontal lines
						if (node!=tree.getRoot())
							g2.draw(line);
					}
				}
				else if (treeDisplay.getOrientation()==TreeDisplay.DOWN){ //����
					xNHor = xN+start+halfLine;
					if (xN>xnM){ // branch going to right
						ynMVert = ynM-start-halfLine;
						if (yN!=ynM){
							line = new Line2D.Double(xNHor,yN-halfLine-inset,xNHor,ynMVert);  // draws the vertical lines
							g2.draw(line);
						}
						line = new Line2D.Double(xNHor,ynMVert,xnM+edgewidth,ynMVert);  // draws the horizontal lines
						if (node!=tree.getRoot())
							g2.draw(line);
					}
					else { // branch going to left
						ynMVert = ynM-edgewidth+start+halfLine;
						if (yN!=ynM){
							line = new Line2D.Double(xNHor,yN-halfLine-inset,xNHor,ynMVert);  // draws the vertical lines
							g2.draw(line);
						}
						line = new Line2D.Double(xNHor,ynMVert,xnM+halfLine,ynMVert);  // draws the horizontal lines
						if (node!=tree.getRoot())
							g2.draw(line);
					}
				}
				else  if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
					yNVert = yN+start+halfLine;
					if (yN>ynM){ // branch going down
						xnMHor = xnM-start-halfLine;
						if (xN!=xnM){
							line = new Line2D.Double(xN-halfLine-inset,yNVert,xnMHor,yNVert);  // draws the horizontal lines
							g2.draw(line);
						}
						line = new Line2D.Double(xnMHor,yNVert,xnMHor,ynM+edgewidth+halfLine-inset);  // draws the vertical lines
						if (node!=tree.getRoot())
							g2.draw(line);
					}
					else { // branch going up
						xnMHor = xnM+start-edgewidth+halfLine;
						if (xN!=xnM){
							line = new Line2D.Double(xN-halfLine-inset,yNVert,xnMHor,yNVert);  // draws the horizontal lines
							g2.draw(line);
						}
						line = new Line2D.Double(xnMHor,yNVert,xnMHor,ynM-halfLine+inset);  // draws the vertical lines
						if (node!=tree.getRoot())
							g2.draw(line);
					}
				}
				else  if (treeDisplay.getOrientation()==TreeDisplay.LEFT){  //����
					yNVert = yN+start+halfLine;
					if (yN>ynM){ // branch going down
						xnMHor = xnM+start+halfLine;
						if (xN!=xnM){
							line = new Line2D.Double(xN+halfLine+1,yNVert,xnMHor,yNVert);  // draws the horizontal lines
							g2.draw(line);
						}
						line = new Line2D.Double(xnMHor,yNVert,xnMHor,ynM+edgewidth+halfLine-1);  // draws the vertical lines
						g2.draw(line);
					}
					else { // branch going up
						xnMHor = xnM+edgewidth-start-halfLine;
						if (xN!=xnM){
							line = new Line2D.Double(xN+halfLine+inset,yNVert,xnMHor,yNVert);  // draws the horizontal lines
							g2.draw(line);
						}
						line = new Line2D.Double(xnMHor,yNVert,xnMHor,ynM-halfLine+inset);  // draws the vertical lines
						g2.draw(line);
					}
				}
				g2.setStroke(stroke);
			}

			if (emphasizeNodes && nodePoly!=null) {
				Color prev = g.getColor();
				g.setColor(Color.green);//for testing
				//	g.fillPolygon(nodePoly);
				GraphicsUtil.fillRect(g, x[node]-1, y[node]-1,2,2);
				g.setColor(prev);
			}
		}
	}


	/*_________________________________________________*/
	public static boolean inSquareLineBranch(TreeDisplay treeDisplay, double[] x, double[] y, int edgewidth, int taxonSpacing, Tree tree, int node, int h, int v) {
		if (tree.nodeExists(node)) {
			int nM = tree.motherOfNode(node);
			double xN=x[node];
			double xnM = x[nM];
			double yN =y[node];
			double ynM = y[nM];
			int halfEdgewidth = edgewidth/2;
			int nearby = 4;
			int halfTaxonSpacing = taxonSpacing/2 -4;
			if (nearby> halfTaxonSpacing) nearby = halfTaxonSpacing;
			if (nearby< 1) nearby = 1;

			if (treeDisplay.getOrientation()==TreeDisplay.UP) {
				if ((h>=xN-nearby) && (h<=xN+edgewidth+nearby) && (v>=yN) && (v<=ynM))  //with vertical part of branch
					return true;
				if (xnM>xN) {  // mother is to the right of node
					if ((h>=xN) && (h<=xnM) && (v>=ynM-halfEdgewidth-nearby) && (v<=ynM+halfEdgewidth+nearby))  //with horizontal part of branch
						return true;
				}
				else {
					if ((h>=xnM) && (h<=xN) && (v>=ynM-halfEdgewidth-nearby) && (v<=ynM+halfEdgewidth+nearby))  //with horizontal part of branch
						return true;
				}
			}

			else if (treeDisplay.getOrientation()==TreeDisplay.DOWN){
				if ((h>=xN-nearby) && (h<=xN+edgewidth+nearby) && (v>=ynM) && (v<=yN))  //with vertical part of branch
					return true;
				if (xnM>xN) {  // mother is to the right of node
					if ((h>=xN) && (h<=xnM) && (v>=ynM-halfEdgewidth-nearby) && (v<=ynM+halfEdgewidth+nearby))  //with horizontal part of branch
						return true;
				}
				else {
					if ((h>=xnM) && (h<=xN) && (v>=ynM-halfEdgewidth-nearby) && (v<=ynM+halfEdgewidth+nearby))  //with horizontal part of branch
						return true;
				}
			}
			else  if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
				if ((v>=yN-nearby) && (v<=yN+edgewidth+nearby) && (h>=xnM) && (h<=xN))  //with horizontal part of branch
					return true;
				if (ynM>yN) {  // mother is below node
					if ((v>=yN) && (v<=ynM) && (h>=xnM-halfEdgewidth-nearby) && (h<=xnM+halfEdgewidth+nearby))  //with vertical part of branch
						return true;
				}
				else {
					if ((v>=ynM) && (v<=yN) && (h>=xnM-halfEdgewidth-nearby) && (h<=xnM+halfEdgewidth+nearby))  //with vertical part of branch
						return true;
				}
			}
			else  if (treeDisplay.getOrientation()==TreeDisplay.LEFT){ 
				if ((v>=yN-nearby) && (v<=yN+edgewidth+nearby) && (h>=xN) && (h<=xnM))  //with horizontal part of branch
					return true;
				if (ynM>yN) { // mother is below node
					if ((v>=yN) && (v<=ynM) && (h>=xnM-halfEdgewidth-nearby) && (h<=xnM+halfEdgewidth+nearby))  //with vertical part of branch
						return true;
				}
				else {
					if ((v>=ynM) && (v<=yN) && (h>=xnM-halfEdgewidth-nearby) && (h<=xnM+halfEdgewidth+nearby))  //with vertical part of branch
						return true;
				}
			}

		}
		return false;
	}




	/*_________________________________________________*/
	public static boolean inBranch(TreeDisplay treeDisplay, double[] x, double[] y, int edgewidth, Tree tree, int node, int h, int v) {
		if (tree.nodeExists(node)) {
			int nM = tree.motherOfNode(node);
			double xN=x[node];
			double xnM = x[nM];
			double yN =y[node];
			double ynM = y[nM];
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
