/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.TaxonListCurrPartition;
/*~~  */

import mesquite.lists.lib.*;

import java.util.*;
import java.awt.*;

import mesquite.basic.ManageTaxaPartitions.ManageTaxaPartitions;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.TaxaGroup;
import mesquite.lib.taxa.TaxaGroupVector;
import mesquite.lib.taxa.TaxaPartition;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.ListDialog;
import mesquite.lib.ui.MesquiteMenuItemSpec;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.lib.ui.MesquiteSymbol;

/* ======================================================================== */
public class TaxonListCurrPartition extends TaxonListAssistant implements SelectionInformer {
	/*.................................................................................................................*/
	Taxa taxa;
	MesquiteTable table=null;
	TaxaGroupVector groups;
	TaxaSelectedUtility helperTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		groups = (TaxaGroupVector)getProject().getFileElement(TaxaGroupVector.class, 0);
		groups.addListener(this);
		helperTask = (TaxaSelectedUtility)hireNamedEmployee(TaxaSelectedUtility.class, "#TaxonPartitionHelper");
		return true;
	}
	public void endJob(){
		if (taxa != null)
			taxa.removeListener(this);
		if (groups != null)
			groups.removeListener(this);
		super.endJob();
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Group Membership (taxa)";
	}
	public String getExplanation() {
		return "Lists and allows changes to group membership in the current partition of taxa, for List of Taxa window." ;
	}
	/*.................................................................................................................*/
	public void setTableAndTaxa(MesquiteTable table, Taxa taxa){

		if (taxa != this.taxa){
			if (this.taxa != null)
				this.taxa.removeListener(this);
			taxa.addListener(this);
		}
		this.taxa = taxa;
		this.table = table;
		helperTask.setTaxaAndSelectionInformer(taxa, this);
	}
	public void changed(Object caller, Object obj, Notification notification){
		if (caller == this)
			return;
		outputInvalid();
		parametersChanged(notification);
	}
	public boolean isItemSelected (int item, Object caller){
		if (table != null && employer!=null && employer instanceof ListModule) {
			int c = ((ListModule)employer).getMyColumn(this);
			return table.isCellSelectedAnyWay(c, item);
		}
		return false;
	}
	public boolean anyItemsSelected (Object caller){
		if (table != null && employer!=null && employer instanceof ListModule) {
			int c = ((ListModule)employer).getMyColumn(this);
			return table.anyCellsInColumnSelectedAnyWay(c);
		}
		return false;
	}
	public String getTitle() {
		return "Group";
	}
	public String getStringForTaxon(int ic){
		if (taxa!=null) {
			TaxaPartition part = (TaxaPartition)taxa.getCurrentSpecsSet(TaxaPartition.class);
			if (part==null)
				return "?";
			TaxaGroup tg = part.getTaxaGroup(ic);
			if (tg==null)
				return "?";
			return tg.getName();
		}
		return "?";
	}
	public boolean useString(int ic){
		return false;
	}
	public void drawInCell(int ic, Graphics g, int x, int y,  int w, int h, boolean selected){
		if (taxa==null || g==null)
			return;
		TaxaPartition part = (TaxaPartition)taxa.getCurrentSpecsSet(TaxaPartition.class);
		Color c = g.getColor();
		MesquiteSymbol symbol = null;
		boolean colored = false;
		Color backgroundColor = null;
		if (part!=null) {
			TaxaGroup tg = part.getTaxaGroup(ic);
			if (tg!=null){
				backgroundColor= tg.getColor();
				if (backgroundColor!=null){
					g.setColor(backgroundColor);
					g.fillRect(x+1,y+1,w-1,h-1);
					colored = true;
				}
				symbol = tg.getSymbol();
			}
		}
		if (!colored){ 
			if (selected)
				g.setColor(Color.black);
			else
				g.setColor(Color.white);
			g.fillRect(x+1,y+1,w-1,h-1);
		}
		if (symbol!=null) {
			symbol.drawSymbol(g,x+w-h/2,y+h/2,w-3,h/2-3,true);
		}

		String s = getStringForRow(ic);
		if (s!=null){
			FontMetrics fm = g.getFontMetrics(g.getFont());
			if (fm==null)
				return;
			int sw = fm.stringWidth(s);
			int sh = fm.getMaxAscent()+ fm.getMaxDescent();
			if (backgroundColor==null) {
				if (selected)
					g.setColor(Color.white);
				else
					g.setColor(Color.black);
			} else {  // background is color; choose contrasting color
				Color contrast = ColorDistribution.getContrastingTextColor(backgroundColor);
				g.setColor(contrast);
			}
			g.drawString(s, x+(w-sw)/2, y+h-(h-sh)/2);
			if (c!=null) g.setColor(c);
		}

	}

	public String getWidestString(){
		if (taxa != null) {
			TaxaPartition part = (TaxaPartition)taxa.getCurrentSpecsSet(TaxaPartition.class);
			if (part != null) {
				int max = 12;
				for (int it = 0; it<taxa.getNumTaxa(); it++) {
					TaxaGroup tg = part.getTaxaGroup(it);
					if (tg != null) {
						String name = tg.getName();
						if (StringUtil.notEmpty(name)) {
							if (name.length()>max)
								max = name.length();
						}
					}
				}
				if (max>50)
					max = 60;
				return "888888888 888888888 888888888 888888888 888888888 888888888 ".substring(0, max);
			}
		}
		return "88888888888  ";
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}

	/*.................................................................................................................*/
}

