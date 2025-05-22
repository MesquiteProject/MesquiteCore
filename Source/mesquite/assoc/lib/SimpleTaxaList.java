/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.assoc.lib;

import java.awt.*;
import java.awt.event.*;
import mesquite.lib.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.MQScrollbar;
import mesquite.lib.ui.MesquitePanel;
import mesquite.lib.ui.MesquiteTool;
import mesquite.lib.ui.MousePanel;


public class SimpleTaxaList extends MesquitePanel implements AdjustmentListener {
	Taxa taxa;
	MousePanel homePanel;
	boolean[] selected;
	boolean[] assigned;
	MQScrollbar scrollBar;
	int scrollWidth=16;
	int topRow = 0;
	int rowSpacing = 6;
	boolean selectable = true;

	public SimpleTaxaList(Taxa taxa, MousePanel homePanel, boolean showScroll, int rowSpacing, boolean selectable) {
		super();
		this.homePanel = homePanel;
		setBackground(Color.white);
		this.rowSpacing = rowSpacing;
		this.selectable = selectable;
		if (showScroll){
			scrollBar=new MQScrollbar(Scrollbar.VERTICAL);
			scrollBar.setVisible(true);
			scrollBar.setValues(0,getNumRowsVisible(),0,0);
			scrollBar.addAdjustmentListener(this);
			scrollBar.setUnitIncrement(1);
			scrollBar.setBlockIncrement(1);
			add(scrollBar);
		}
		setTaxa(taxa);
	}

	public void setAssigned(int i, boolean b) {
		if (assigned!=null && i>=0 && i<assigned.length)
			assigned[i] = b;
	}
	public boolean[] getSelectedList() {
		return selected;
	}

	public boolean getSelected(int i){
		if (i>=0 && i<selected.length)
			return selected[i];
		return false;
	}


	public void setSize(int width, int height) {
		super.setSize(width,height);
		if (scrollBar!=null) {
			scrollBar.setSize(scrollWidth,height);
			scrollBar.setLocation(getBounds().width-scrollWidth,0);
			scrollBar.setVisibleAmount(getNumRowsVisible());
			scrollBar.setBlockIncrement(getNumRowsVisible()-1);
		}

	}

	public void setLocation(int x, int y) {
		super.setLocation(x,y);
		if (scrollBar!=null)
			scrollBar.setLocation(getBounds().width-scrollWidth,0);
	}

	public int getNumRowsVisible(){
		if (taxa!=null)
			return MesquiteInteger.minimum(getNumTaxa(taxa), Math.round(getBounds().height/getRowHeight()));
		return Math.round(getBounds().height/getRowHeight());
	}
	public void deselectAll() {
		if (selected==null)
			return;
		for (int i=0; i<selected.length; i++) {
			boolean wasSelected = selected[i];
			selected[i]=false;
			if (wasSelected) 
				redrawRow(i, true);
		}
	}
	public void selectRows(int row1, int row2) {
		for (int i=row1; i<=row2; i++) {
			if (i>=0 && i<selected.length) {
				boolean wasSelected = selected[i];
				selected[i]=true;
				if (!wasSelected) 
					redrawRow(i, true);
			}
		}
	}
	public void selectRow(int row,boolean b) {
		if (row>=0 && row<selected.length) {
			boolean wasSelected = selected[row];
			selected[row]=b;
			if (wasSelected!=b) 
				redrawRow(row, true);
		}
	}
	int getNumTaxa(Taxa taxa){
		int numTaxa = 0;
		if (taxa != null)
			numTaxa = taxa.getNumTaxa();
		return numTaxa;
	}
	public void setTaxa(Taxa taxa) {
			this.taxa = taxa;
			int numTaxa = getNumTaxa(taxa);
				
			selected = new boolean[numTaxa];
			assigned = new boolean[numTaxa];
			for (int i=0; i<selected.length; i++){
				selected[i]=false;
				assigned[i]=false;
			}
			if (scrollBar != null)
				scrollBar.setMaximum(numTaxa);
			repaint();
	}

	public void paint(Graphics g) {
		if (g==null)
			return;
		FontMetrics fm=g.getFontMetrics(g.getFont());
		int numParts =  getNumTaxa(taxa);
		for (int i=0; i<numParts && i<selected.length; i++) {
			drawRow(g, i, fm.getMaxDescent(), false);
		}
		g.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);  //bottom line
	}

	public void drawRow(Graphics g, int row, int maxDescent, boolean erase) {
		if (row<0 || row>=selected.length || row>=getNumTaxa(taxa))
			return;
		if (!getRowVisible(row))
			return;
		if (selected[row])  {
			g.setColor(Color.lightGray);
			g.fillRect(0,getRowTop(row),getBounds().width, getRowHeight());		
		}
		else if (erase) {
			g.setColor(Color.white);
			g.fillRect(0,getRowTop(row),getBounds().width, getRowHeight());
		}
		int offset = 15;
		if (assigned[row]) {
			g.setColor(Color.gray);
			offset=0;
		}
		else {
			g.setColor(Color.black);
		}
		if (taxa.getTaxonName(row) != null)
			g.drawString(taxa.getTaxonName(row),5+offset,getRowBottom(row)-maxDescent);
	}
	public void redrawRow(int row, boolean erase) {
		if (row<0)
			return;
		Graphics g = getGraphics();
		if (g==null)
			return;
		FontMetrics fm=g.getFontMetrics(g.getFont());
		drawRow(g,row,fm.getMaxDescent(), erase);
		g.dispose();

	}
	public int getRowHeight() {
		Graphics g = getGraphics();
		if (g==null)
			return -1;
		FontMetrics fm=g.getFontMetrics(g.getFont());
		int rowHeight = (fm.getMaxAscent() + fm.getMaxDescent() + rowSpacing);
		g.dispose();
		return (rowHeight);
	}
	public int getRowTop(int row) {
		if (taxa==null)
			return -1;
		Graphics g = getGraphics();
		if (g==null)
			return -1;
		FontMetrics fm=g.getFontMetrics(g.getFont());
		int rowHeight = (fm.getMaxAscent() + fm.getMaxDescent() + rowSpacing);
		g.dispose();
		return ((row-topRow)*rowHeight);
	}
	public int getRowBottom(int row) {
		if (taxa==null)
			return -1;
		Graphics g = getGraphics();
		if (g==null)
			return -1;
		FontMetrics fm=g.getFontMetrics(g.getFont());
		int rowHeight = (fm.getMaxAscent() + fm.getMaxDescent() + rowSpacing);
		g.dispose();
		return ((row-topRow)*rowHeight+rowHeight);
	}
	public boolean getRowVisible(int row) {
		return (getRowTop(row)<getBounds().height && getRowBottom(row)>=0);
	}

	public int findRow(int y) {
		if (taxa==null)
			return -1;
		int numParts =  getNumTaxa(taxa);
		int row = -1;
		for (int i=0; i<numParts; i++) {
			if (y>getRowTop(i) && y<= getRowBottom(i)) {
				row = i;
				break;
			}
		}
		return row;
	}


	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		if (!selectable)
			return;
		int  row = findRow(y);
		if (row>=0 && row<=selected.length && row<=getNumTaxa(taxa)) {
			if (!selected[row] && !MesquiteEvent.shiftKeyDown(modifiers) && !MesquiteEvent.commandOrControlKeyDown(modifiers))
				deselectAll();
			if (MesquiteEvent.shiftKeyDown(modifiers)) {
				boolean selectionExtended = false;
				for (int i=0; i<row; i++)
					if (selected[i]){
						selectRows(i,row);
						selectionExtended=true;
						break;
					}
				if (!selectionExtended)
					for (int i=getNumTaxa(taxa)-1; i>row && i<selected.length; i--)
						if (selected[i]){
							selectRows(row,i);
							selectionExtended=true;
							break;
						}
				if (!selectionExtended) {
					selected[row]= !selected[row];
					redrawRow(row, false);
				}
			}
			else {
				if (MesquiteEvent.commandOrControlKeyDown(modifiers))
					selected[row]= !selected[row];
				else 
					selected[row]= true;
				redrawRow(row, false);
			}
		} else
			deselectAll();
	}
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
	}
	public void mouseDrag(int modifiers, int x, int y, MesquiteTool tool) {
	}

	public void adjustmentValueChanged(AdjustmentEvent e) {
		if(e.getAdjustable() == scrollBar) {
			switch(e.getAdjustmentType()) {
			case AdjustmentEvent.UNIT_DECREMENT:
			case AdjustmentEvent.UNIT_INCREMENT:
			case AdjustmentEvent.BLOCK_INCREMENT:
			case AdjustmentEvent.BLOCK_DECREMENT:
			case AdjustmentEvent.TRACK:
				topRow = e.getValue();
				repaint();
				break;
			}
		}
	}

}
