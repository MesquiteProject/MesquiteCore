/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.cont.lib;

import mesquite.lib.NameReference;
/* ======================================================================== */
/** Contains an array of  continuous character states for one character, at each of the taxa or nodes 
 See notes under <a href = "ContinuousData.html">ContinuousData</a> regarding items */
public interface ItemContainer  {
	
	/*..........................................ItemContainer................*/
	public int getNumItems();
	/*..........................................ItemContainer................*/
	public String getItemName(int index);
	/*..........................................ItemContainer................*/
	public NameReference getItemReference(String name);
	/*..........................................ItemContainer................*/
	public NameReference getItemReference(int index);
	/*..........................................ItemContainer................*/
	//public void setItemReference(int index, NameReference nr);

	/*..........................................ItemContainer................*/
	public int getItemNumber(NameReference nr);
	/*..........................................ItemContainer................*/
	//public Object addItem(String nr);
	/*..........................................ItemContainer................*/
	//public void removeItem(int nr);

}


