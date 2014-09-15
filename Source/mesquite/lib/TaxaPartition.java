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
import java.math.*;

/* ======================================================================== */
/**A TaxaPartition is a specification of which property applies to each taxon (TAXAPARTITION in NEXUS file format).
  */

public class TaxaPartition  extends ObjectSpecsSet {
	Taxa taxa;
	public TaxaPartition (String name, int numTaxa, Object defaultProperty, Taxa taxa) {
		super(name, numTaxa, defaultProperty);
		this.taxa = taxa;
	}
	public SpecsSet cloneSpecsSet(){
		if (taxa==null)
			return null;
		TaxaPartition ms = new TaxaPartition(new String(name), taxa.getNumTaxa(), getDefaultProperty(), taxa);
		for (int i=0; i<getNumberOfParts(); i++)
			ms.setProperty(getProperty(i), i);
		return ms;
	}
	public SpecsSet makeSpecsSet(AssociableWithSpecs parent, int numParts){
		if (!(parent instanceof Taxa))
			return null;
		return new TaxaPartition("Inclusion Set", numParts, getDefaultProperty(), (Taxa)parent);
	}
	public Taxa getTaxa(){
		return taxa;
	}
	public String getTypeName(){
		return "Taxa partition";
	}
	/*
	public int getMaxPartitionNumber(){//NOT USED (Jan 01)
		int max = MesquiteInteger.unassigned;
		for (int i=0; i<getNumberOfParts(); i++) {
			TaxaGroup mq = getTaxaGroup(i);
			if (mq!=null)
				max = MesquiteInteger.maximum(max, mq.getGroupNumber());
		}
		return max;
	}
	/**Returns an array of all the partition numbers*
	public int[] getGroupNumbers(){//NOT USED (Jan 01)
		int[] temp = new int[getNumberOfParts()];
		int next = 0;
		for (int i=0; i<getNumberOfParts(); i++) {
			TaxaGroup mq = getTaxaGroup(i);
			if (mq!=null) {
				int q = mq.getGroupNumber();
				if (IntegerArray.indexOf(temp, q)<0){
					temp[next++]=q;
				}
			}
		}
		if (next==0)
			return null;
		int[] result = new int[next];
		for (int i=0; i<result.length; i++)
			result[i]=temp[i];
		
		return result;
	}
	/**Returns an array of all the partition numbers*/
	public int getNumberOfGroups(){
		TaxaGroup[] temp = new TaxaGroup[getNumberOfParts()];
		int next = 0;
		for (int i=0; i<getNumberOfParts(); i++) {
			TaxaGroup mq = getTaxaGroup(i);
			if (mq!=null) {
				if (ObjectArray.indexOf(temp, mq)<0){
					temp[next++]=mq;
				}
			}
		}
		return next;
	}
	/**Returns an array of all the partition numbers*/
	public TaxaGroup[] getGroups(){
		TaxaGroup[] temp = new TaxaGroup[getNumberOfParts()];
		int next = 0;
		for (int i=0; i<getNumberOfParts(); i++) {
			TaxaGroup mq = getTaxaGroup(i);
			if (mq!=null) {
				if (ObjectArray.indexOf(temp, mq)<0){
					temp[next++]=mq;
				}
			}
		}
		if (next==0)
			return null;
		TaxaGroup[] result = new TaxaGroup[next];
		for (int i=0; i<result.length; i++)
			result[i]=temp[i];
		
		return result;
	}
	/*
	public TaxaGroup getGroup(int groupNumber){
		for (int i=0; i<getNumberOfParts(); i++) {
			TaxaGroup mq = getTaxaGroup(i);
			if (mq!=null && mq.getGroupNumber()==groupNumber)
				return mq;
		}
		return null;
	}
	
	public TaxaGroup getGroup(String name){
		if (name==null)
			return null;
		for (int i=0; i<getNumberOfParts(); i++) {
			TaxaGroup mq = getTaxaGroup(i);
			if (mq!=null && name.equalsIgnoreCase(mq.getName()))
				return mq;
		}
		return null;
	}*/
  	/*.................................................................................................................*/
	/**/
	public int getNumberInGroup(TaxaGroup target){
		int num =0;
		for (int i=0; i<getNumberOfParts(); i++) {
			TaxaGroup mq = getTaxaGroup(i);
			if (mq==target)
				num++;
		}
		return num;
	}
 	/*.................................................................................................................*/
 	/** gets storage for set of properties*/
	public Object[] getNewPropertyStorage(int numParts){
		return new TaxaGroup[numParts];
	}
	
	
	public TaxaGroup getTaxaGroup(int part){
		return (TaxaGroup)getProperty(part);
	}
	public TaxaGroup getTaxaGroup(Taxon taxon){
		int it = taxa.whichTaxonNumber(taxon);
		if (it>=0)
			 return (TaxaGroup)getProperty(it);
		else
			return null;
	}

}



