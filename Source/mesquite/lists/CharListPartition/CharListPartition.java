/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.CharListPartition;
/*~~  */

import mesquite.lists.lib.*;

import java.util.*;
import java.awt.*;

import mesquite.basic.ManageTaxaPartitions.ManageTaxaPartitions;
import mesquite.categ.lib.DNAData;
import mesquite.charMatrices.ManageCharPartitions.ManageCharPartitions;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.lib.taxa.TaxaPartition;
import mesquite.lib.ui.ListDialog;
import mesquite.lib.ui.MesquiteMenuItemSpec;
import mesquite.lib.ui.MesquiteSubmenuSpec;

/* ======================================================================== */
public class CharListPartition extends CharListAssistant implements SelectionInformer{
	/*.................................................................................................................*/
	CharacterData data=null;
	MesquiteTable table=null;
	CharactersGroupVector groups;
	CharactersSelectedUtility helperTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		groups = (CharactersGroupVector)getProject().getFileElement(CharactersGroupVector.class, 0);
		groups.addListener(this);
		helperTask = (CharactersSelectedUtility)hireNamedEmployee(CharactersSelectedUtility.class, "#CharPartitionHelper");
		return true;
	}
	/*.................................................................................................................*/
	public void endJob(){
		if (data != null)
			data.removeListener(this);
		if (groups != null)
			groups.removeListener(this);
		super.endJob();
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Group Membership (characters)";
	}
	public String getExplanation() {
		return "Lists and allows changes to group membership in the current partition of characters, for List of Characters window." ;
	}
	
	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		if (data != this.data){
			if (this.data != null)
				this.data.removeListener(this);
			data.addListener(this);
		}
		this.data = data;
		this.table = table;
		helperTask.setDataAndSelectionInformer(data, this);
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
	public void changed(Object caller, Object obj, Notification notification){
		if (caller == this)
			return;
		outputInvalid();
		parametersChanged(notification);
	}
	public String getTitle() {
		return "Group";
	}
	public String getStringForCharacter(int ic){
		if (data!=null) {
			CharacterPartition partition = (CharacterPartition)data.getCurrentSpecsSet(CharacterPartition.class);
			if (partition != null) {
				CharactersGroup group = (CharactersGroup)partition.getProperty(ic);
				if (group!=null) {
					return group.getName();
				}
			}
		}
		return "?";
	}
	public boolean useString(int ic){
		return false;
	}
	public void drawInCell(int ic, Graphics g, int x, int y,  int w, int h, boolean selected){
		if (data==null || g==null)
			return;
		boolean colored = false;
		Color c = g.getColor();
		CharacterPartition part = (CharacterPartition)data.getCurrentSpecsSet(CharacterPartition.class);
		if (part!=null) {
			CharactersGroup tg = part.getCharactersGroup(ic);
			if (tg!=null){
				Color cT = tg.getColor();
				if (cT!=null){
					g.setColor(cT);
					g.fillRect(x+1,y+1,w-1,h-1);
					colored = true;
				}
			}
		}
		if (!colored){ 
			if (selected)
				g.setColor(Color.black);
			else
				g.setColor(Color.white);
			g.fillRect(x+1,y+1,w-1,h-1);
		}

		String s = getStringForRow(ic);
		if (s!=null){
			FontMetrics fm = g.getFontMetrics(g.getFont());
			if (fm==null)
				return;
			int sw = fm.stringWidth(s);
			int sh = fm.getMaxAscent()+ fm.getMaxDescent();
			if (selected)
				g.setColor(Color.white);
			else
				g.setColor(Color.black);
			g.drawString(s, x+(w-sw)/2, y+h-(h-sh)/2);
			if (c!=null) g.setColor(c);
		}
	}
	public String getWidestString(){
		if (data!=null) {
			int length = 20;
			String longest = null;
			CharacterPartition partition = (CharacterPartition)data.getCurrentSpecsSet(CharacterPartition.class);
			if (partition != null) {
				for (int ic= 0; ic< data.getNumChars(); ic++){
					CharactersGroup group = (CharactersGroup)partition.getProperty(ic);
					if (group!=null) {
						String s = group.getName();
						if (s != null)
							if (s.length()> length) {  //just counting string length to avoid font metrics calculations
								length = s.length();
								longest = s;
							}
					}
				}
				if (longest !=null)
					return longest;
			}
		}
		return "Partition     ";
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}

	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}
}


