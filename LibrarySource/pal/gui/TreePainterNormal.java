// TreePainterNormal.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.gui;

import pal.tree.*;
import pal.io.*;
import pal.misc.*;

import headless.awt.*;

/**
 * A class that can paint a tree into a Graphics object.  
 *
 * @version $Id: TreePainterNormal.java,v 1.6 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Alexei Drummond
 */
public class TreePainterNormal extends TreePainter {

	public static final int RIGHTBORDER = 75;
	public static final int LEFTBORDER = 10;
	public static final int TOPBORDER = 20;
	public static final int BOTTOMBORDER = 30;

	public static final int FONT_SIZE = 9;
	public static final int YSPACER = 20;
	public static final int XSPACER = 4;

	private double xScale = 1.0;
	private double yScale = 1.0;


  public TreePainterNormal(Tree toDisplay, String title, boolean showTitle) {
		this(toDisplay,title,showTitle,null);
	}
	public TreePainterNormal(Tree toDisplay, String title, boolean showTitle, Node highlightNode) {
		super(toDisplay,title,showTitle);
		standardTreePrep(highlightNode);
		System.out.println("HLNODE:"+highlightNode);
	}


	/** 
	 * Returns the preferred size for drawing 
	 * (that is the size that will show everything nicely)
	 */
	public Dimension getPreferredSize() {
		return new Dimension(100 + LEFTBORDER + RIGHTBORDER, 
			(int)Math.round(width * FONT_SIZE) + TOPBORDER + BOTTOMBORDER);
	}

	protected void paint(PositionedNode node, Graphics g,
		int displayWidth, int displayHeight) {

		Point p = getPoint(node,displayWidth, displayHeight);
		g.setColor(FOREGROUND);

		if (node.hasChildren()) {
			for (int i = 0; i < node.getChildCount(); i++) {
				paintLeafBranch(p, getPoint((PositionedNode)node.getChild(i), 
					displayWidth,displayHeight), node, g);
			}

			for (int i = 0; i < node.getChildCount(); i++) {
				paint((PositionedNode)node.getChild(i), g,displayWidth, displayHeight);
			}
		} else {

			if ((maxLeafTime > 0.0) && isUsingColor()) {
				g.setColor(Color.getHSBColor((float)(maxLeafTime - node.getNodeHeight())/(float)maxLeafTime, 1.0f, 1.0f));
			} else {
				g.setColor(NORMAL_LABEL_COLOR);
			}

			if (isUsingColor()) {
				int halfWidth = getPenWidth() / 2;
				g.fillRect(p.x - halfWidth, p.y - halfWidth, getPenWidth(), getPenWidth());
			}
			if (isUsingSymbols()&&getTimeOrderCharacterData()!=null) {

				drawSymbol(g, p.x + XSPACER, p.y - (FONT_SIZE / 2), FONT_SIZE,
					getTimeOrderCharacterData().getTimeOrdinal(getTimeOrderCharacterData().whichIdNumber(node.getIdentifier().getName())));
			} else {
				String name = node.getIdentifier().getName();
				int width = g.getFontMetrics().stringWidth(name);
				if(isUsingColor()) {
					g.drawString(name, p.x + XSPACER,
							p.y + (FONT_SIZE / 2));
					if(node.isHighlighted()) {
						g.setColor(Color.red);
						g.drawOval(p.x - 4+XSPACER, p.y-FONT_SIZE/2-5, width +10, FONT_SIZE+8 );
					}
				} else {
					if(node.isHighlighted()) {
						g.setColor(Color.red);
					}
					g.drawString(name, p.x + XSPACER,
						p.y + (FONT_SIZE / 2));
				}
			}
		}

	}

	public Point getPoint(PositionedNode node, int displayWidth, int displayHeight) {

		return new Point(displayWidth -
			(int)Math.round(node.getNodeHeight() * xScale) - RIGHTBORDER,
			(int)Math.round(node.x * yScale) + TOPBORDER);
	}

	private void paintLeafBranch(Point p, Point lp, PositionedNode node, Graphics g) {

		int halfWidth = getPenWidth() / 2;

		// paint join to parent
		g.fillRect(p.x - halfWidth, Math.min(p.y, lp.y) - halfWidth,
				getPenWidth(), Math.abs(lp.y - p.y) + getPenWidth());

		// paint branch
		g.fillRect(Math.min(p.x, lp.x) - halfWidth, lp.y - halfWidth,
				 Math.abs(lp.x - p.x) + getPenWidth(), getPenWidth());

		if (isShowingNodeHeights()) {

			String label = FormattedOutput.getInstance().getDecimalString(node.getNodeHeight(), 4);
			int width = g.getFontMetrics().stringWidth(label);

			int x = Math.min(p.x, lp.x) - (width / 2);

			g.drawString(label, x, p.y - halfWidth - 1);

		}

		if (isShowingInternalLabels()) {
			String label = node.getIdentifier().getName();
			int width = g.getFontMetrics().stringWidth(label);

			int x = Math.min(p.x, lp.x) - (width / 2);

			g.drawString(label, x, p.y - halfWidth - 1);
		}
	}


	public void paint(Graphics g, int displayWidth, int displayHeight) {

		g.setFont(new Font("Times", Font.PLAIN, FONT_SIZE));

		xScale = (double)(displayWidth - LEFTBORDER - RIGHTBORDER) / height;
		yScale = (double)(displayHeight - TOPBORDER - BOTTOMBORDER) / width;

		g.setColor(BACKGROUND);
		g.fillRect(0, 0, displayWidth, displayHeight);
		paint(treeNode, g, displayWidth, displayHeight);

		doTitle(g,LEFTBORDER, TOPBORDER - 8);
		doScale(g,xScale,LEFTBORDER,displayHeight - BOTTOMBORDER + 12);
	}

}

