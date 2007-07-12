package mesquite.assoc.lib;

import java.awt.*;
import java.awt.event.*;
import mesquite.lib.*;


public class SimpleTaxaList extends MesquitePanel implements AdjustmentListener {
	Taxa taxa;
	MousePanel homePanel;
	boolean[] selected;
	boolean[] assigned;
	Scrollbar scrollBar;
	int scrollWidth=16;
	int topRow = 0;
	
	
	public SimpleTaxaList(Taxa taxa, MousePanel homePanel) {
		super();
		this.homePanel = homePanel;
		setBackground(Color.white);
		scrollBar=new Scrollbar(Scrollbar.VERTICAL);
		scrollBar.setVisible(true);
		scrollBar.setValues(0,getNumRowsVisible(),0,0);
		scrollBar.addAdjustmentListener(this);
		scrollBar.setUnitIncrement(1);
		scrollBar.setBlockIncrement(1);
		add(scrollBar);
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
			return MesquiteInteger.minimum(taxa.getNumTaxa(), Math.round(getBounds().height/getRowHeight()));
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
	public void setTaxa(Taxa taxa) {
		if (taxa!=null){//&& taxa!=this.taxa) {
			this.taxa = taxa;
			selected = new boolean[taxa.getNumTaxa()];
			assigned = new boolean[taxa.getNumTaxa()];
			for (int i=0; i<selected.length; i++){
				selected[i]=false;
				assigned[i]=false;
			}
			scrollBar.setMaximum(taxa.getNumTaxa());
		}
	}
	
	public void paint(Graphics g) {
		if (taxa==null || g==null)
			return;
		FontMetrics fm=g.getFontMetrics(g.getFont());
		int numParts =  taxa.getNumTaxa();
		for (int i=0; i<numParts && i<selected.length; i++) {
			drawRow(g, i, fm.getMaxDescent(), false);
		}
	}
	
	public void drawRow(Graphics g, int row, int maxDescent, boolean erase) {
		if (taxa==null || row<0 || row>=selected.length || row>=taxa.getNumTaxa())
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
		g.drawString(taxa.getTaxonName(row),5+offset,getRowBottom(row)-maxDescent);
	}
	public void redrawRow(int row, boolean erase) {
		if (taxa==null || row<0)
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
		int rowHeight = (fm.getMaxAscent() + fm.getMaxDescent() + 6);
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
		int rowHeight = (fm.getMaxAscent() + fm.getMaxDescent() + 6);
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
		int rowHeight = (fm.getMaxAscent() + fm.getMaxDescent() + 6);
		g.dispose();
		return ((row-topRow)*rowHeight+rowHeight);
	}
	public boolean getRowVisible(int row) {
		return (getRowTop(row)<getBounds().height && getRowBottom(row)>=0);
	}
	
	public int findRow(int y) {
		if (taxa==null)
			return -1;
		int numParts =  taxa.getNumTaxa();
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
		int  row = findRow(y);
		if (row>=0 && row<=selected.length && row<=taxa.getNumTaxa()) {
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
					for (int i=taxa.getNumTaxa()-1; i>row && i<selected.length; i--)
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
