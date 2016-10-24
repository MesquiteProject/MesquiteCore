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
/** The panel in which taxa are drawn.  This is used within a taxon window, and can be used by other
modules.  A TaxaDisplay is created
and supervised by a taxon draw coordinator module.  A reference to a TaxaDisplay is needed 
for shading and decorating the taxa. <P>

Note that this is parallel to TreeDisplay
*/
public class TaxaDisplay extends TaxaTreeDisplay  {
	boolean scaleToFit = false;
	double rescaleValue = 1.0;
	DrawNamesTaxaDisplay namesTask;
	/**  The panel in which the taxa are actually drawn.*/
	private TaxaDrawing taxaDrawing;
	
	public TaxaDisplay (MesquiteModule ownerModule, Taxa taxa) { 
		super(ownerModule,taxa);
	}
	
	public void setTaxaDrawing(TaxaDrawing td) {
		taxaDrawing = td;
//		if (taxaDrawing!=null && taxa!=null)
//			setTaxa(taxa);
	}
	public TaxaDrawing getTaxaDrawing() {
		return taxaDrawing;
	}
	   
	public DrawNamesTaxaDisplay getDrawTaxonNames(){
		return namesTask;
	}
	public void setDrawTaxonNames(DrawNamesTaxaDisplay dtn){
		namesTask = dtn;
	}
	public void setDrawingInProcess(boolean inProgress){
		this.inProgress= inProgress;
//		if (!inProgress && holdingTaxa != null) {
//			setTaxa(holdingTaxa);
			//repaint();
//		}
	}
   	public void setScaleToFit(boolean scaleToFit){
   		this.scaleToFit = scaleToFit;
   	}
   	
   	public boolean getScaleToFit(){
   		return scaleToFit;
   	}
   	
   	public double getRescaleValue(){
   		return rescaleValue;
   	}
   	

   	public void setRescaleValue(double rescaleValue){
   		this.rescaleValue = rescaleValue;
   	}
   	

	public void redoCalculations(int code){
		if (taxaDrawing!=null)
			taxaDrawing.recalculatePositions(taxa); //to force node locs recalc
		
	}
	public void addExtra(TaxaDisplayExtra extra) {
		if (extras != null)
			extras.addElement(extra, false);
	}
	public void removeExtra(TaxaDisplayExtra extra) {
		if (extras != null)
			extras.removeElement(extra, false);
	}
	public boolean findExtra(TaxaDisplayExtra extra) {
		if (extras == null)
			return false;
		return (extras.indexOf(extra) >= 0);
	}
	public TaxaDisplayExtra[] getMyExtras(MesquiteModule mb) {
		if (extras != null) {
			int count = 0;
			Enumeration e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				TaxaDisplayExtra ex = (TaxaDisplayExtra)obj;
			   	if (ex.getOwnerModule() == mb) 
			   		 count++;
		 	}
		 	if (count == 0)
		 		return null;
		 	TaxaDisplayExtra[] ee = new TaxaDisplayExtra[count];
			e = extras.elements();
			count = 0;
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				TaxaDisplayExtra ex = (TaxaDisplayExtra)obj;
			   	if (ex.getOwnerModule() == mb) 
			   		ee[count++] = ex;
		 	}
		 	return ee;
		}
		return null;
	}
	public void setTaxaAllExtras(Taxa taxa) {
		if (extras != null) {
			Enumeration e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				TaxaDisplayExtra ex = (TaxaDisplayExtra)obj;
			   	if (ownerModule==null || ownerModule.isDoomed()) 
			   		return;
	 			ex.setTaxa(taxa);
		 	}
		}
	}
	public void drawAllBackgroundExtrasOfPlacement(Taxa taxa, Graphics g, int placement) {
		if (taxa.isDoomed())
			return;
		if (extras != null) {
			Enumeration e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				TaxaDisplayExtra ex = (TaxaDisplayExtra)obj;
	 			if (ex instanceof TaxaDisplayBkgdExtra && ex.getPlacement()==placement) {
				   	if (ownerModule==null || ownerModule.isDoomed()) 
				   		return;
	 				ex.drawOnTaxa(taxa, g);
	 			}
	 		}
		}
	}
	public void drawAllBackgroundExtras(Taxa taxa, Graphics g) {
		drawAllBackgroundExtrasOfPlacement(taxa,g,TaxaDisplayExtra.BELOW);
		drawAllBackgroundExtrasOfPlacement(taxa,g,TaxaDisplayExtra.NORMAL);
		drawAllBackgroundExtrasOfPlacement(taxa,g,TaxaDisplayExtra.ABOVE);
	}
	public void drawAllExtras(Taxa taxa, Graphics g) {
		if (taxa.isDoomed())
			return;
		if (extras != null) {
			Enumeration e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				TaxaDisplayExtra ex = (TaxaDisplayExtra)obj;
	 			if (!(ex instanceof TaxaDisplayBkgdExtra)) {
				   	if (ownerModule==null || ownerModule.isDoomed()) 
				   		return;
	 				ex.drawOnTaxa(taxa, g);
	 			}
	 		}
		}
		if (notice!=null)
			g.drawString(notice, 6, getBounds().height-6);
		if (drawFrame)
			g.drawRoundRect(0, 0, getBounds().width, getBounds().height, 10, 10);
	}
	public void printAllBackgroundExtras(Taxa taxa, Graphics g) {
		if (taxa.isDoomed())
			return;
		if (extras != null) {
			Enumeration e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				TaxaDisplayExtra ex = (TaxaDisplayExtra)obj;
	 			if (ex instanceof TaxaDisplayBkgdExtra) {
				   	if (ownerModule==null || ownerModule.isDoomed()) 
				   		return;
	 				ex.printOnTaxa(taxa, g);
	 			}
	 		}
		}
	}
	public void printAllExtras(Taxa taxa, Graphics g) {
		if (taxa.isDoomed())
			return;
		if (extras != null) {
			Enumeration e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				TaxaDisplayExtra ex = (TaxaDisplayExtra)obj;
	 			if (!(ex instanceof TaxaDisplayBkgdExtra)) {
				   	if (ownerModule==null || ownerModule.isDoomed()) 
				   		return;
	 				ex.printOnTaxa(taxa, g);
	 			}
	 		}
		}
	}
	public void moveExtraToFront(TaxaDisplayExtra extra){
		if (extra == null)
			return;
		Vector panels = extra.getPanels();
		for (int i=0; panels !=null && i<panels.size(); i++) {
			Panel p = (Panel)panels.elementAt(i);
			remove(p);
			add(p);
		}
		getExtras().removeElement(extra, false);
		getExtras().addElement(extra, false);
		pleaseUpdate(false);
	}
	
	public String getTextVersion() {
		if (taxaDrawing == null)
			return "";
		String s="";
		if (extras != null) {
			Enumeration e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				TaxaDisplayExtra ex = (TaxaDisplayExtra)obj;
				if (ex!=null){
					String sEx =null;
					sEx = ex.infoAtNodes(taxa);
		 			if (!StringUtil.blank(sEx)) {
						String owner = "";
						if (ex.getOwnerModule()!=null)
							owner = ex.getOwnerModule().getName();
						s+= "\n\n--------------- " + owner + " ---------------";
		 				s+= "\n\n"+ sEx + "\n";
		 			}
	 			}
	 		}
		}
		return s;
	}
	public void dispose(){
		MesquiteWindow w = MesquiteWindow.windowOfItem(this);
		if (w!=null)
			w.waitUntilDisposable();
		
		ownerModule =null;
		if (taxaDrawing !=null)
			taxaDrawing.dispose();
		taxaDrawing = null;
		if (extras != null) {
			Enumeration e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				TaxaDisplayExtra ex = (TaxaDisplayExtra)obj;
	 			ex.dispose();
	 		}
		}
		destroyExtras();
		extras = null;
		super.dispose();
	}

}


