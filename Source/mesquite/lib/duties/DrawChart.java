/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
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


/* ======================================================================== */
/**A class of modules that supplies Charters, to draw charts.  Example modules: Histogram, LineGraph,
Scattergram, TableChart.*/

public abstract class DrawChart extends MesquiteModule  {
	 public String getFunctionIconPath(){
   		 return getRootImageDirectoryPath() + "functionIcons/chart.gif";
   	 }

   	 public Class getDutyClass() {
   	 	return DrawChart.class;
   	 }
 	public String getDutyName() {
 		return "Draw Chart";
   	 }
 	/** Supplies a Charter object.*/
 	public abstract Charter createCharter(ChartListener listener);
	
 	/** Indicates to the chart module what Associable might be associated with points of the chart. 
 	The module need not respond to it.  If called, the module can safely assume each point refers 
 	to a part of the associable.*/
	public abstract void pointsAreSelectable(boolean areParts, Selectionable a, boolean allowSequenceOptions);
   	public boolean isSubstantive(){
   		return false;  
   	}
}


