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
import java.util.*;


/* ======================================================================== */
/** A class used for additional graphical and calculations elements to be drawn and calculated within 
TaxaDisplays -- The TaxaDisplayExtra is notified when the cursor 
is moved over a branch and so on, and when the window is drawn the TaxaDisplayExtra is notified via drawOnTaxa so that
it can add its items to the taxa.*/
public abstract class TaxaDisplayExtra implements Listable, OwnedByModule {  
	public TaxaDisplay taxaDisplay;
	public MesquiteModule ownerModule;
	public static long totalCreated = 0;
	public static int BELOW = 1;
	public static int NORMAL = 2;
	public static int ABOVE = 3;
	int placement=NORMAL;
	
	double rescaleValue=1.0;
	
	private Vector panels = new Vector();
	public TaxaDisplayExtra (MesquiteModule ownerModule, TaxaDisplay taxaDisplay) {
		this.taxaDisplay = taxaDisplay;
		this.ownerModule=ownerModule;
		totalCreated++;
	}
	
	public MesquiteModule getOwnerModule(){
		return ownerModule;
	}
	
	public String getName(){
		if (ownerModule !=null)
			return ownerModule.getName();
		else
			return getClass().getName();
	}

	public int getPlacement(){
		return placement;
	}

	public void setPlacement(int placement){
		this.placement = placement;
	}
	
	public TaxaDisplay getTaxaDisplay(){
		return taxaDisplay;
	}
	
	public double getRescaleValue() {
		return rescaleValue;
	}

	public void setRescaleValue(double rescaleValue) {
		this.rescaleValue = rescaleValue;
	}


	public void dispose(){
		ownerModule =null;
		taxaDisplay=null;
	}
	/**notifies the TaxaDisplayExtra that the taxa have changed, so it knows to redo calculations, and so on*/
	public abstract void setTaxa(Taxa taxa);
	/**draw on the taxa passed*/
	public abstract void drawOnTaxa(Taxa taxa, Graphics g);
	/**print on the taxa passed*/
	public abstract void printOnTaxa(Taxa taxa, Graphics g);

	/**simply fills the graphics used for the taxon with the current color; used for inverting*/
	public void fillTaxon(Taxa taxa, Graphics g, int it){}
	
	/**returns whether or not the point x, y is in within this extra's graphics for this taxon*/
	public boolean pointInTaxon(Taxa taxa, int it, int x, int y){
		return false;
	}

	/**returns whether or not the taxon is in the bounds specified*/
	public boolean taxonInRectangle(Taxa taxa, int it, int x1, int y1, int x2, int y2){
		return false;
	}
	
	/**return a text version of information on taxa*/
	private String textOnTaxon(Taxa taxa, int it){
		String s="";
		s+="[" + textAtNode(it) + "]";
		return s;
	}
	/**return a text version of information on taxa*/
	private void textOnTaxon(Taxa taxa, int it, String[] nodeStrings){
		nodeStrings[it]= textAtNode(it);
	}
	/**return a text version of information on taxa, displayed as list of nodes with information at each*/
	public String infoAtNodes(Taxa taxa){
		String legend = textForLegend();
		String notes = additionalText(taxa);
		if (StringUtil.blank(notes))
			notes = legend;
		else
			notes = legend + "\n" + notes;
		if (!StringUtil.blank(notes))
			return notes;
		
		return "";
	}
	/** Returns true if this extra wants the taxon to have its name underlined */
	public boolean getTaxonUnderlined(Taxon taxon){
		return false;
	}
	/** Returns the color the extra wants the taxon name colored.*/
	public Color getTaxonColor(Taxon taxon){
		return null;
	}
	/** Returns any strings to be appended to taxon name.*/
	public String getTaxonStringAddition(Taxon taxon){
		return null;
	}
	/**return a text version of information at node*/
	public String textAtNode(int it){
		return "";
	}
	/**return text to be placed in legends*/
	public String textForLegend(){
		return "";
	}
	/**return any additional explanatory text, e.g. if there is extensive information too verbose for a legend but which should be output to text view*/
	public String additionalText(Taxa taxa){
		return "";
	}

	public void cursorEnterTaxon(int M, Graphics g){}
	/**to inform TaxaDisplayExtra that cursor has just exited name of terminal taxon M*/
	public void cursorExitTaxon(int M, Graphics g){}
	/**to inform nDisplayExtra that cursor has just touched name of terminal taxon M*/
	public void cursorTouchTaxon(int M, Graphics g){}
	
	public void addPanelPlease(Panel p){
		panels.addElement(p);
		taxaDisplay.addPanelPlease(p);
	}
	public void removePanelPlease(Panel p){
		if (panels !=null)
			panels.removeElement(p);
		if (taxaDisplay != null)
			taxaDisplay.removePanelPlease(p);
	}
	protected Vector getPanels(){
		return panels;
	}

	public void turnOff() {
		if (taxaDisplay==null)
			return;
		taxaDisplay.removeExtra(this);
		taxaDisplay.repaint();
	}
	
}

