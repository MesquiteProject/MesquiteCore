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
import java.util.*;

import mesquite.lib.duties.*;

/* ======================================================================== */
/** The panel in which taxa or trees are drawn.  Superclass to TreeDisplay and TaxaDisplay
*/
public abstract class TaxaTreeDisplay extends MesquitePanel  {
	/** The taxa being drawn */
	protected Taxa taxa;
	
	/**  TaxaDisplayExtras attached to this TaxaDisplay*/
	protected ListableVector extras;
	/**  This is part of system to record whether the taxa need redrawing, used so as to prevent multiple updates*/
	protected int repaintsPending =0;
	/**  Records whether or not the taxon names should be centered, if drawn in one plane, about axis of longest name*/
	public boolean centerNames = false;
	/**  Records whether or not the taxon names are to be drawn*/
	public boolean suppressNames = false;
	
	/**  Records whether drawing is to be suppressed*/
	protected boolean suppress = true;
	
	protected boolean paintedOnce = false;
	/**  A notice that is drawn in the display.*/
	protected String notice;
	/**  Is a frame to be drawn in the TaxaDisplay?*/
	protected boolean drawFrame=false;
	/**  The area in which the taxa are drawn*/
	Rectangle field;
	/**  */
	protected int taxonNameBuffer = 30;
	/**  */
	protected boolean inProgress = false;
	/**  */
	protected MesquiteModule ownerModule;
	/**  */
	public static long totalCreated;
	/**  */
	protected boolean isInvalid = false;
	/**  */
	protected boolean crossDrawn = false;
	
	public double[] nodeLocsParameters = new double[8]; //parameters for use by node locs module
	public double[] drawParameters = new double[8];//parameters for use by draw taxon module
	
	static {
		totalCreated = 0;
	}
	
	public TaxaTreeDisplay (MesquiteModule ownerModule, Taxa taxa) { 
		super();
		setDontDuplicateCommands(true);
		if (MesquiteTrunk.isMacOSX())
			setMoveFrequency(4);
		else
			setMoveFrequency(0);
		totalCreated++;
		this.ownerModule=ownerModule;
		field = new Rectangle();
		extras = new ListableVector();
		this.taxa = taxa;
		setLayout(null);
	}
	
	public MesquiteModule getOwnerModule(){
		return ownerModule;
	}
	
	public int getNumTaxa() {
		return taxa.getNumTaxa();
	}
	public boolean getDrawingInProcess(){
		return inProgress;
	}
	   
	public void setDrawingInProcess(boolean inProgress){
		this.inProgress= inProgress;
	}
	private boolean printing = false;
	public boolean getPrintingInProcess(){
		return printing;
	}
	public void setPrintingInProcess(boolean inProgress){
		this.printing= inProgress;
	}
	public void suppressDrawing(boolean toSuppress) {
		suppress=toSuppress;
	}
	public Taxa getTaxa() {
		return taxa;
	}
	public void setTaxa(Taxa taxa) {
		this.taxa = taxa;
	}
	public boolean isCrossDrawn() {
		return crossDrawn;
	}

	public void setCrossDrawn(boolean crossDrawn) {
		this.crossDrawn = crossDrawn;
	}

	protected boolean redoCalculationsWaiting = false;
	public void recalculatePositions() {
		if (!inProgress){
			try{
				redoCalculations(12);

				redoCalculationsWaiting = false;
			}
			catch (Exception e){
			
			}
		}
		else
			redoCalculationsWaiting = true;
	}
	Rectangle visRect;

	public Rectangle getVisRect(){
		return visRect;
	}
	public void setVisRect(Rectangle r){
		visRect = r;
	}

	public void setTaxonNameBuffer(int b) {
		taxonNameBuffer = b;
	}
	public int getTaxonNameBuffer() {
		return taxonNameBuffer;
	}
	public void setInvalid(boolean iv) {  //this does not cause a redraw; simply posting a note.  Flag gets should be set to false in  drawing
		this.isInvalid = iv;
	}
	public boolean getInvalid() { 
		return isInvalid;
	}
	private void updateComponents(Container c){
		Component[] cc = c.getComponents();
		for (int i=0; i<cc.length; i++) {
			cc[i].repaint();
			if (cc[i] instanceof Container)
				updateComponents((Container)cc[i]);
		}
	}
	
	public void repaintAll() {  //TODO: this whole system needs revamping.  
		if (isVisible()) {
			repaint();
			updateComponents(this);
		}
	}
	public void pleaseUpdate() {  //TODO: this whole system needs revamping.  
		repaint();
	}
	public void pleaseUpdate(boolean resetElements) {  //TODO: this whole system needs revamping.  
		repaint(resetElements);

		MesquiteWindow mw = MesquiteWindow.windowOfItem(this);
		if (mw !=null && mw.getMode()>0)
			mw.updateTextPage();
	}
	public void repaint(boolean resetElements){
		repaint();
	}
	public void repaint() {  //TODO: this whole system needs revamping.  
		if (!isVisible())
			return;
		if (redoCalculationsWaiting){
			try{
			recalculatePositions();
			}
			catch (Exception e){
			}
			catch (Throwable e){
			}
		}
		repaintsPending++;
		if (repaintsPending<=1){
			super.repaint();
			crossDrawn=false;
		}
	}
	public void setFrame(boolean doFrame) {
		drawFrame = doFrame;
	}
	public void setSize(){
		
	}
	public abstract void redoCalculations(int code);
	
	public void setFieldSize(int x, int y) {
		boolean ch = (field.width!=x || field.height !=y);
		field.width=x;
		field.height=y;
		if (ch)
			redoCalculations(4); 
	}
	public void setFieldWidth(int x) {
		boolean ch = (field.width!=x);
		field.width=x;
		if (ch)
			redoCalculations(5); 
	}
	public void setFieldHeight(int y) {
		boolean ch = (field.height !=y);
		field.height=y;
		if (ch)
			redoCalculations(6); 
	}
	public int getFieldWidth() {
		//return getBounds().width;
		return field.width;
	}
	public int getFieldHeight() {
		//return getBounds().width;
		return field.height;
	}
	public Rectangle getField() { 
		//return getBounds();
		return field;
	}

	public void setNotice(String s) {
		notice = s;
	}
	public ListableVector getExtras() {
		return extras;
	}
	
	public void paint(Graphics g) {
	   	if (MesquiteWindow.checkDoomed(this))
	   		return;
	   	paintedOnce = true;
		MesquiteWindow.uncheckDoomed(this);
	}

	public void fillTaxon(Graphics g, int m){}
	public void redrawTaxa(Graphics g, int M) {}

	public boolean pointInTaxon(int m, int x, int y){return false;}
	public boolean taxonInRectangle(Taxa taxa, int it, int x1, int y1, int x2, int y2){
		return false;}

	public void addPanelPlease(Panel p){
		if (p==null)
			return;
		add(p);
		MesquiteWindow window = MesquiteWindow.windowOfItem(this);
		if (window !=null)
			window.componentAdded(this, p);
	}
	public void removePanelPlease(Panel p){
		if (p==null)
			return;
		remove(p);
	}
	public void addComponentPlease(Component p){
		if (p==null)
			return;
		add(p);
	}
	public void removeComponentPlease(Component p){
		if (p==null)
			return;
		remove(p);
	}
	public void destroyExtras(){
		if (extras!=null)
			extras.removeAllElements(true);
	}

}


