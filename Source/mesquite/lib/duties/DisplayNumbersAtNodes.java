/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.duties;

import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.tree.TreeDecorator;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDisplayExtra;


/* ======================================================================== */
/**Displays numbers at nodes on tree.  Example modules: LabelNumbersOnTree.*/

public abstract class DisplayNumbersAtNodes extends MesquiteModule  {
	protected MesquiteBoolean showLabels= new MesquiteBoolean(false);
	protected MesquiteBoolean shadeBranches= new MesquiteBoolean(true);   // added DRM 07.iv.14
	protected MesquiteBoolean shadeInColor= new MesquiteBoolean(true);
	protected MesquiteBoolean labelTerminals= new MesquiteBoolean(true);  // added DRM 07.iv.14
	protected MesquiteBoolean usePercentages= new MesquiteBoolean(false);  // added DRM 1 October 2014

	public Class getDutyClass() {
   	 	return DisplayNumbersAtNodes.class;
   	 }
 	public String getDutyName() {
 		return "Display Numbers At Nodes";
   	 }
   	 
   	 public String[] getDefaultModule() {
   	 	return new String[] {"#LabelNumbersOnTree"};
   	 }
   	 
   	 /** create tree decorator which will do the drawing.  The tree decorator's 
   	 method drawOnTree(Tree tree, Object obj, Graphics g) will be called to draw the states*/
	public TreeDecorator createTreeDecorator(TreeDisplay treeDisplay, TreeDisplayExtra ownerExtra) {
		return null;
	}
	public boolean getShowLabels() {
		return showLabels.getValue();
	}
	public void setShowLabels(boolean showLabels) {
		this.showLabels.setValue(showLabels);
	}
	public boolean getShadeBranches() {
		return shadeBranches.getValue();
	}
	public void setShadeBranches(boolean shadeBranches) {
		this.shadeBranches.setValue(shadeBranches);
	}
	public boolean getShadeInColor() {
		return shadeInColor.getValue();
	}
	public void setShadeInColor(boolean shadeInColor) {
		this.shadeInColor.setValue(shadeInColor);
	}
	public boolean getLabelTerminals() {
		return labelTerminals.getValue();
	}
	public void setLabelTerminals(boolean labelTerminals) {
		this.labelTerminals.setValue(labelTerminals);
	}
   	 public boolean getUsePercentages() {
		return usePercentages.getValue();
	}
	public void setUsePercentages(boolean usePercentages) {
		this.usePercentages.setValue(usePercentages);
	}

}


