/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.ornamental.Projector;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class Projector extends TreeDisplayAssistantI {
	public Vector extras;
	public String getFunctionIconPath(){
		return getPath() + "projector.gif";
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		extras = new Vector();
		return true;
	} 
   	public String getExpectedPath(){
		return getPath() + "projector.gif";
  	 }
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		ProjectorToolExtra newPj = new ProjectorToolExtra(this, treeDisplay);
		extras.addElement(newPj);
		return newPj;
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
 	 	int tot = 0;
  	 	if (extras ==null || extras.size() == 0)
  	 		return null;
  	 	for (int i = 0; i<extras.size(); i++) {
  	 		ProjectorToolExtra pte = (ProjectorToolExtra)extras.elementAt(i);
  	 		if (pte != null)
  	 			tot += 3 +pte.getNumPicturesShowing();
  	 	}
  	 	if (tot == 0)
  	 		return null;
    	 	Snapshot temp = new Snapshot();
  	 	for (int i = 0; i<extras.size(); i++) {
  	 		ProjectorToolExtra pte = (ProjectorToolExtra)extras.elementAt(i);
    	 		if (pte != null) { 
	  	 		Snapshot q = pte.getSnapshot(file);
	  	 		if (q !=null) {
			 		temp.addLine("getExtra " + i );
		  	 		temp.addLine("tell It");
		  	 		temp.incorporate(q, true);
		  	 		temp.addLine("endTell");
	  	 		}
  	 		}
  	 	}
  	 	return temp;
  	 }
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Returns a projector object", "[number of object]", commandName, "getExtra")) {  
	  	 	int which = MesquiteInteger.fromFirstToken(arguments, pos);
	  	 	if (MesquiteInteger.isCombinable(which) && which>=0 && which<extras.size()) 
	  	 		return extras.elementAt(which);
	  	 	else
	  	 		return null;
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Projector";
   	 }
	public boolean isSubstantive(){
		return false;
	}   	 
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Supplies a projector tool for tree windows that allows pictures attached to taxa to be displayed." ;
   	 }
}

/* ======================================================================== */
class ProjectorToolExtra extends TreeDisplayDrawnExtra implements Commandable  {
	TreeTool projectorTool;
	MesquiteMenuItemSpec hideMenuItem = null;
	Projector projectorModule;
	Image[] taxonImages=null;
	 ProjectorPanel[] panels=null;
	boolean usePanels = false;
	boolean picturesOn = false;
	
	public ProjectorToolExtra (Projector ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		projectorModule = ownerModule;
		projectorTool = new TreeTool(this, "projector", ownerModule.getPath(),"projector.gif", 8,1,"Show picture", "This tool displays a picture attached to a taxon.  By clicking again, the picture is hidden.  A terminal taxon has an available picture if there is a \"(i)\" after its name.");
		projectorTool.setTouchedTaxonCommand(MesquiteModule.makeCommand("showTaxonPicture",  this));
		if (ownerModule.containerOfModule() instanceof MesquiteWindow) {
			((MesquiteWindow)ownerModule.containerOfModule()).addTool(projectorTool);
		}
	}
	/*.................................................................................................................*/
	public   void drawNode(Tree tree, int node, Graphics g) {
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			drawNode(tree, d, g);
		if (tree.nodeIsTerminal(node)) {
			int taxonNumber =tree.taxonNumberOfNode(node);
			Image im = taxonImages[taxonNumber];
			if (im!=null) {
				int imWidth = im.getWidth((ImageObserver)treeDisplay);
				int imHeight = im.getHeight((ImageObserver)treeDisplay);
				int taxonSpacing = treeDisplay.getTaxonSpacing();
				if (taxonSpacing <80)
					taxonSpacing = 80;
				double scale = MesquiteImage.getScaleToReduce(imWidth, imHeight, taxonSpacing, taxonSpacing);
		      		if (scale<1.0){
		      			imWidth *= scale;
		      			imHeight *= scale;
		      		}
				if (usePanels && panels!=null && panels[taxonNumber] != null) 
					panels[taxonNumber].setLocation((int) treeDisplay.getTreeDrawing().x[node]-imWidth/2, (int)treeDisplay.getTreeDrawing().y[node]-imHeight/2);
				else
					g.drawImage(im, (int)treeDisplay.getTreeDrawing().x[node]-imWidth/2, (int)treeDisplay.getTreeDrawing().y[node]-imHeight/2, imWidth, imHeight, (ImageObserver)treeDisplay);
			}
		}
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		if (tree==null)
			return;
		//TODO: SHOUDLN"T DO THIS IF NO IMAGES SHOWN
		if (picturesOn) {
			checkNumTaxa(tree);
			drawNode(tree, drawnRoot, g);
		}
	}
	
	private void checkNumTaxa(Tree tree) {
		if (tree==null)
			return;
		if (taxonImages==null) {
			taxonImages = new Image[tree.getNumTaxa()];
		}
		else if (taxonImages.length!= tree.getNumTaxa()) {
			Image[] newTaxonImages = new Image[tree.getNumTaxa()];
			for (int i=0; i< taxonImages.length && i<tree.getNumTaxa(); i++) {
				newTaxonImages[i]=taxonImages[i];
			}
			taxonImages=newTaxonImages;
		}
		if (usePanels) {
			if (panels==null) {
				panels = new ProjectorPanel[tree.getNumTaxa()];
				for (int i=0; i< panels.length; i++) {
					panels[i] = new ProjectorPanel(treeDisplay);
				}
			}
			else if (panels.length!= tree.getNumTaxa()) {
				ProjectorPanel[] newPanels = new ProjectorPanel[tree.getNumTaxa()];
				for (int i=0; i< panels.length && i<tree.getNumTaxa(); i++) {
					newPanels[i] = panels[i];
				}
				for (int i=panels.length;i<tree.getNumTaxa(); i++) {
					newPanels[i] = new ProjectorPanel(treeDisplay);
				}
				panels = newPanels;
			}
		}
	}
	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
	}
	/** Returns any strings to be appended to taxon name.*/
	public String getTaxonStringAddition(Taxon taxon){
		if (taxon == null)
			return null;
		Taxa taxa = taxon.getTaxa();
		int M = taxa.whichTaxonNumber(taxon);
		if (M<0  || M> taxa.getNumTaxa())
			return null;
		if (taxonImages != null && M<taxonImages.length && taxonImages[M] !=null)
			return "(i)";
		else {
			Image image = null;
			AttachedNotesVector aim = (AttachedNotesVector)taxa.getAssociatedObject(imageNameRef, M);
			if (aim == null || aim.getNumNotes()==0)
				return null;
			AttachedNote hL = aim.getAttachedNote(0);
			
			
			if (hL == null)
				return null;
			image = hL. getImage();
			if (image != null) 
				 return "(i)";
			return null;
		}
	}

	/*.................................................................................................................*/
	private   void checkMenuStatus(boolean added) {
		if (added) { //image shown; make sure menu says "turn off pictures"
			if (hideMenuItem==null) {
				hideMenuItem = ownerModule.addMenuItem("Hide pictures", MesquiteModule.makeCommand("hidePictures", this));
				ownerModule.resetContainingMenuBar();
				picturesOn = true;
			}
		}
		else {
    	 		boolean someLeft = false;
    	 		for (int M = 0; M<taxonImages.length; M++)
				if (taxonImages[M]!= null)
					someLeft=true;
			if (!someLeft) {
				ownerModule.deleteMenuItem(hideMenuItem);
				hideMenuItem = null;
				ownerModule.resetContainingMenuBar();
				picturesOn = false;
			}
		}
		
	}
	/*.................................................................................................................*/
  	 public int getNumPicturesShowing() {
  	 	if (taxonImages== null)
  	 		return 0;
  	 	int n = 0;
 		for (int j=0; j<taxonImages.length; j++) {
 			if (taxonImages[j] !=null)
 				n++;
 		}
  	 	return n;
  	 }
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
  	 	if (getNumPicturesShowing() <=0 || taxonImages == null) 
  	 		return null;
    	 	Snapshot temp = new Snapshot();
		for (int j=0; j<taxonImages.length; j++) {
 			if (taxonImages[j] !=null) {
 				temp.addLine("showTaxonPicture " + j );
 			}
 		}
  	 	return temp;
  	 }
	/*.................................................................................................................*/
	public String chooseLink(){
			MesquiteString dir = new MesquiteString();
			MesquiteString f = new MesquiteString();
			
   	 		String path = MesquiteFile.openFileDialog("Choose Picture", dir, f);
   	 		String d = dir.getValue();
   	 		if (!StringUtil.blank(d) && !StringUtil.blank(f.getValue())) {
   	 			if (!d.endsWith("/")) 
   	 				d += "/";
   	 			boolean success;
   	 			if (ownerModule.getProject().getHomeDirectoryName().equalsIgnoreCase(d)){
   	 				return f.getValue();
   	 			}
   	 			else {
   	 				return path;
				}
			}
			return null;
	}
	/*.................................................................................................................*/
  	 MesquiteInteger pos = new MesquiteInteger();
	NameReference imageNameRef = NameReference.getNameReference("notes");
	/*.................................................................................................................*/
 	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 

    	 	if (checker.compare(this.getClass(), "Hides pictures of taxa", null, commandName, "hidePictures")) {
    	 		for (int M = 0; M<taxonImages.length; M++) {
				taxonImages[M]= null;
				if (usePanels && M<panels.length && panels[M]!=null) {
					panels[M].setVisible(false);
					removePanelPlease(panels[M]);
				}
			}
			checkMenuStatus(false);
			treeDisplay.pleaseUpdate(false);
   	 	}
    	 	else if (checker.compare(this.getClass(), "Shows picture of taxon", "[taxon number][modifiers]", commandName, "showTaxonPicture")) {
    	 		Tree tree = treeDisplay.getTree();
    	 		Taxa taxa = tree.getTaxa();
   	 		int M = MesquiteInteger.fromFirstToken(arguments, pos);
   	 		if (M<0 || !MesquiteInteger.isCombinable(M))
   	 			return null;

			Image image = null;

			AttachedNotesVector aim = (AttachedNotesVector)taxa.getAssociatedObject(imageNameRef, M);
			if (aim == null) {
				ownerModule.discreetAlert( "There are no images attached to that taxon to show.  To attach an image, use the annotations panel of a Character Matrix Editor to make an annotation and link in an image");
				return null;
			}
			AttachedNote hL = aim.getAttachedNote(0);
			
			
			if (hL != null){
				image = hL. getImage();
			}
			else {
				ownerModule.discreetAlert( "There are no images attached to that taxon to show.  To attach an image, use the annotations panel of a Character Matrix Editor to make an annotation and link in an image");
				return null;
			}
			/*else {
				image = tree.getTaxa().getTaxon(M).getIllustration();
			}*/
			if (image != null) {
				checkNumTaxa(tree);
				if (taxonImages[M] == null) {
					checkMenuStatus(true);
					taxonImages[M]=  image;
					if (usePanels) {
						panels[M].setImage(taxonImages[M]);
						panels[M].setLocation((int) treeDisplay.getTreeDrawing().x[M]-taxonImages[M].getWidth((ImageObserver)treeDisplay)/2, (int)treeDisplay.getTreeDrawing().y[M]-taxonImages[M].getHeight((ImageObserver)treeDisplay)/2);
						panels[M].setSize(taxonImages[M].getWidth((ImageObserver)treeDisplay), taxonImages[M].getHeight((ImageObserver)treeDisplay));
						addPanelPlease(panels[M]);

						panels[M].setVisible(true);
						panels[M].repaint();
						//treeDisplay.fillTaxon();
					}
				}
				else {
					taxonImages[M]= null;
					checkMenuStatus(false);
					if (usePanels) {
						panels[M].setVisible(false);
						removePanelPlease(panels[M]);
					}
				}
				treeDisplay.pleaseUpdate(false);
			}
			else {
				ownerModule.discreetAlert( "There are no images attached to that taxon to show.  To attach an image, use the annotations panel of a Character Matrix Editor to make an annotation and link in an image");
				return null;
			}
    	 	}
 		return null;
 	}
	public void turnOff() {
		projectorModule.extras.removeElement(this);
		super.turnOff();
	}
}

class ProjectorPanel extends Panel {
	Image im=null;
	TreeDisplay treeDisplay;
	
	public ProjectorPanel (TreeDisplay treeDisplay) {
		this.treeDisplay = treeDisplay;
		setSize(10,10);
		setBackground(Color.red);
	}
	public void paint(Graphics g) {
	   	if (MesquiteWindow.checkDoomed(this))
	   		return;
		
		if (im!= null )
			g.drawImage(im, 0, 0, this);
		MesquiteWindow.uncheckDoomed(this);
	}
	
	public void setImage(Image im) {
		this.im = im;
		//setSize(im.getWidth(this), im.getHeight(this));
	}
}
	


