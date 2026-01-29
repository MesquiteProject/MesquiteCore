/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.CharMatricesListPartition;
/*~~  */

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.Vector;

import mesquite.lib.Debugg;
import mesquite.lib.ListableVector;
import mesquite.lib.Notification;
import mesquite.lib.Pausable;
import mesquite.lib.SelectionInformer;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.MatrixPartition;
import mesquite.lib.characters.MatricesGroup;
import mesquite.lib.characters.MatricesGroupVector;
import mesquite.lib.duties.CharactersSelectedUtility;
import mesquite.lib.duties.MatricesSelectedUtility;
import mesquite.lib.table.MesquiteTable;
import mesquite.lists.lib.CharListAssistant;
import mesquite.lists.lib.CharMatricesListAssistant;
import mesquite.lists.lib.ListModule;

/* ======================================================================== */
public class CharMatricesListPartition extends CharMatricesListAssistant implements SelectionInformer, Pausable{
	//"@MATRIXGROUP
	/*.................................................................................................................*/
	MesquiteTable table=null;
	MatricesGroupVector groups;
	MatricesSelectedUtility helperTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		groups = (MatricesGroupVector)getProject().getFileElement(MatricesGroupVector.class, 0);
		groups.addListener(this);
		helperTask = (MatricesSelectedUtility)hireNamedEmployee(MatricesSelectedUtility.class, "#MatrixPartitionHelper");
		return true;
	}
	/*.................................................................................................................*/
	public void endJob(){
		if (datas != null)
			datas.removeListener(this);
		if (groups != null)
			groups.removeListener(this);
		super.endJob();
	}
	public void setTableAndObject(MesquiteTable table, Object obj){
		if (datas !=null)
			datas.removeListener(this);
		if (obj instanceof ListableVector)
			this.datas = (ListableVector)obj;
		datas.addListener(this);
		this.table = table;
		helperTask.setSelectionInformer(this);
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Group Membership (matrices)";
	}
	public String getExplanation() {
		return "Lists and allows changes to group membership in the current partition of matrices, for List of Character Matrices window." ;
	}
	
	/** Indicate what could be paused */
	public void addPausables(Vector pausables) {
		if (pausables != null)
			pausables.addElement(this);
	}
	/** to ask Pausable to pause*/
	public void pause() {
		paused = true;
	}
	/** to ask a Pausable to unpause (i.e. to resume regular activity)*/
	public void unpause() {
		paused = false;
		outputInvalid();
		parametersChanged(null);
	}
	/*.................................................................................................................*/
	boolean paused = false;
	public boolean isPaused(){
		return paused;
	}
	/*.................................................................................................................*/
	
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
		if (!paused){
			outputInvalid();
			parametersChanged(notification);
		}
	}
	public String getTitle() {
		return "Group";
	}
	public String getStringForRow(int ic){
		if (datas!=null) {
			MatrixPartition partition = (MatrixPartition)datas.getCurrentSpecsSet(MatrixPartition.class);
			if (partition != null) {
				MatricesGroup group = (MatricesGroup)partition.getProperty(ic);
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
		if (datas==null || g==null)
			return;
		boolean colored = false;
		Color c = g.getColor();
		MatrixPartition part = (MatrixPartition)datas.getCurrentSpecsSet(MatrixPartition.class);
		if (part!=null) {
			MatricesGroup tg = part.getMatricesGroup(ic);
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
		if (datas!=null) {
			int length = 20;
			String longest = null;
			MatrixPartition partition = (MatrixPartition)datas.getCurrentSpecsSet(MatrixPartition.class);
			if (partition != null) {
				for (int ic= 0; ic< datas.size(); ic++){
					MatricesGroup group = (MatricesGroup)partition.getProperty(ic);
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
		return true;  
	}
	
}


