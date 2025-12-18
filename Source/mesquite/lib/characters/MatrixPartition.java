/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.characters; 

import mesquite.lib.AssociableWithSpecs;
import mesquite.lib.ListableVector;
import mesquite.lib.ObjectArray;
import mesquite.lib.ObjectSpecsSet;
import mesquite.lib.SpecsSet;

/* ======================================================================== */
/**A CharacterPartition is a specification of which property applies to each matrix (MATRIXPARTITION in NEXUS file format).
  */

public class MatrixPartition  extends ObjectSpecsSet {
	ListableVector datas;
	public MatrixPartition (String name, int numChars, Object defaultProperty, ListableVector datas) {
		super(name, numChars, defaultProperty);
		this.datas = datas;
	}
	
	public SpecsSet cloneSpecsSet(){
		MatrixPartition ms = new MatrixPartition(new String(name), getNumberOfParts(), (MatricesGroup)getDefaultProperty(), datas);
		for (int i=0; i<getNumberOfParts(); i++)
			ms.setProperty(getProperty(i), i);
		return ms;
	}
	public SpecsSet makeSpecsSet(AssociableWithSpecs parent, int numParts){
		if (!(parent instanceof ListableVector))
			return null;
		return new MatrixPartition("Partition", numParts, getDefaultProperty(), (ListableVector)parent);
	}
	public String getTypeName(){
		return "Matrix partition";
	}

	public MatricesGroup getMatricesGroup(int part){
		return (MatricesGroup)getProperty(part);
	}
	/**Returns whether there are groups*/
	public boolean anyGroups(){
		int next = 0;
		for (int i=0; i<getNumberOfParts(); i++) {
			MatricesGroup mq = getMatricesGroup(i);
			if (mq!=null) {
				return true;
			}
		}
		return false;
	}
	/**Returns an array of all the partitions for all the matrices*/
	public MatricesGroup[] getGroups(){
		MatricesGroup[] temp = new MatricesGroup[getNumberOfParts()];
		int next = 0;
		for (int i=0; i<getNumberOfParts(); i++) {
			MatricesGroup mq = getMatricesGroup(i);
			if (mq!=null) {
				if (ObjectArray.indexOf(temp, mq)<0){
					temp[next++]=mq;
				}
			}
		}
		if (next==0)
			return null;
		MatricesGroup[] result = new MatricesGroup[next];
		for (int i=0; i<result.length; i++)
			result[i]=temp[i];
		
		return result;
	}
 	/*.................................................................................................................*/
	/**/
	public int getNumberInGroup(MatricesGroup target){
		int num =0;
		for (int i=0; i<getNumberOfParts(); i++) {
			MatricesGroup mq = getMatricesGroup(i);
			if (mq==target)
				num++;
		}
		return num;
	}
 	/*.................................................................................................................*/
	/**/
	public boolean getAnyCurrentlyUnassigned(){
		for (int i=0; i<getNumberOfParts(); i++) {
			MatricesGroup mq = getMatricesGroup(i);
			if (mq==null)
				return true;
		}
		return false;
	}
	/*.................................................................................................................*/
 	/** gets storage for set of properties*/
	public Object[] getNewPropertyStorage(int numParts){
		return new MatricesGroup[numParts];
	}
 	/*.................................................................................................................*/
 	/** return array of matrix properties (can also get indivitually using getProperty)*/
	public MatricesGroup[] getPartitions() {
		MatricesGroup[] p = new MatricesGroup[getNumberOfParts()];
		for (int i=0; i<getNumberOfParts(); i++) {
			p[i] =(MatricesGroup)getProperty(i);
		}
		return p;
	}
	

}


