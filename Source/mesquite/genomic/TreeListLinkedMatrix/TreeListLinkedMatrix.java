/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.genomic.TreeListLinkedMatrix;
/*~~  */

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import mesquite.lib.Debugg;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteEvent;
import mesquite.lib.MesquiteModule;
import mesquite.lib.Notification;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeVector;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.MesquiteImage;
import mesquite.lib.ui.MesquitePopup;
import mesquite.lists.lib.TreeListAssistant;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.ShowLinkedMatrixMachine;
/* ======================================================================== */
public class TreeListLinkedMatrix extends TreeListAssistant {
	Image matrixIcon;
	ShowLinkedMatrixMachine showMachine = new ShowLinkedMatrixMachine(this);
	/*.................................................................................................................*/
	public String getName() {
		return "Linked Matrix";
	}
	public String getExplanation() {
		return "Shows whether there is a matrix linked to this tree, e.g. the gene locus from which a gene tree was inferred." ;
	}
	/*.................................................................................................................*/
	TreeVector treesBlock;
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		matrixIcon = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "windowIcons/matrixSequence.gif");
		
		return true;
	}
	MesquiteTable table;
	public void setTableAndTreeBlock(MesquiteTable table, TreeVector trees){
		treesBlock = trees;
		this.table = table;
	}
	public String getTitle() {
		return "Linked Matrix";
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		if (obj == treesBlock)
			treesBlock=null;
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;  //TODO: respond
	}
	public void changed(Object caller, Object obj, Notification notification){
		if (Notification.appearsCosmetic(notification))
			return;
		parametersChanged(notification);
	}
	/*.................................................................................................................*/
	public String getStringForTree(int ic){
		if (treesBlock==null)
			return "";
		if (linkedMatrix(ic) == null)
			return "~";
		return null;
	}
	
	CharacterData linkedMatrix(int ic){
		if (treesBlock == null)
			return null;
		MesquiteTree tree = (MesquiteTree)treesBlock.getTree(ic);
		CharacterData data = tree.findLinkedMatrix(getProject());
		return data;
	}
	public void drawInCell(int ic, Graphics g, int x, int y,  int w, int h, boolean selected){
		CharacterData data = linkedMatrix(ic);
		Color oldColor = g.getColor();
		if (selected)
			g.setColor(ColorDistribution.veryLightGray);
		else
			g.setColor(ColorDistribution.uneditable);
		g.fillRect(x, y, w, h);
		g.setColor(Color.lightGray);
		g.drawRect(x, y, w, h);
		g.setColor(oldColor);
		if (table != null && data != null){
			
			g.drawImage(matrixIcon, x + (w-16)/2, y+(h-12)/2, table.getMatrixPanel());
		}
	}
	public boolean arrowTouchInRow(Graphics g, int ic, int x, int y, boolean doubleClick, int modifiers){ //so assistant can do something in response to arrow touch; return true if the event is to stop there, i.e. be intercepted
		if (MesquiteEvent.rightClick(modifiers) && linkedMatrix(ic) != null && table!= null){
			MesquitePopup popup = new MesquitePopup(table.getMatrixPanel());
			MesquiteTree tree = (MesquiteTree)treesBlock.getTree(ic);
			showMachine.addPopupChoices(popup, tree);
			popup.showPopup(x, y);						
		return true;
		}
		return false;
	}

	public String getWidestString(){
		return " Linked Matrix ";
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;    //release number
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}


}

