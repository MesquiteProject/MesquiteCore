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
import java.util.Vector;

/*==========================  Mesquite Basic Class Library    ==========================*/
/*===  the basic classes used by the trunk of Mesquite and available to the modules
/* ======================================================================== */
/** Set of sets, e.g. all of the model sets or weight sets associated with a data matrix.  Thus, all of the model sets are housed in
one SpecsSetVector.  All of the weight sets are housed in another, and so on.  Each of these SpecsSetVectors is housed
in the Vector specsVectors of CharacterData.  The different vectors are distinguished by a name associated with them.*/
public class SpecsSetVector extends ListableVector  {
	SpecsSet current;
	Class type;
	String typeName;
	public SpecsSetVector  (String typeName) {
		super();
		this.typeName = typeName;
	}
	public String getTypeName(){
		return typeName;
	}
	public int getNumSpecsSets() {  
		return size();
	}
	public SpecsSet getSpecsSet(int i){
		if (i>=0 && i<size())
			return (SpecsSet)elementAt(i);
		return null;
	}
	public void doom(){
		doomed = true;
	  		for (int i=0; i<size(); i++) {
	  			SpecsSet s = (SpecsSet)elementAt(i);
	  			if (s!=null)
	  				s.doom();
	  		}
  		
  		super.doom();
	}
	
	public SpecsSet getCurrentSpecsSet(){
		return current;
	}
	public void loadSpecsSetToCurrent(int index){
		if (index>=0 && index<size())
			setCurrentSpecsSet((SpecsSet)elementAt(index));
	}
	public void setCurrentSpecsSet(SpecsSet specsSet){
		current = specsSet;
	}
	public SpecsSet storeCurrentSpecsSet(){
		if (current == null)
			return null;
		SpecsSet c = current.cloneSpecsSet();
		c.setParent(current.getParent());
		c.setProject(current.getProject());
		addSpecSet(c);
		return c;
	}
	public void replaceStoredSpecsSet(SpecsSet oldOne, SpecsSet replacement){
		if (replacement ==null)
			return;
		SpecsSet clone = replacement.cloneSpecsSet();
		String name = replacement.getName();
		clone.setName(oldOne.getName());
		replaceElement(oldOne, clone, true);
	}
	/**returns name of item stored; e.g. "Parsimony Model" ,  "Weight", "Inclusion", "Selection"
	public String getName() {  
		return name;
	}
	public void setName(String name){
		this.name = name;
	}
	*/
	/**returns type of item stored; e.g. ParsimonyModelSet.class, WeightSet.class*/
	public Class getType() {  
		return type;
	}
	public void setType(Class type){
		this.type = type;
	}
	
	/** Add specs set to vector */
	public void addSpecSet(Listable specsSet){
		addElement(specsSet, true);
	}
	/** remove specs set from vector */
	public void removeSpecSet(Listable specsSet){ //if current, change current
		removeElement(specsSet, true);
	}
}


