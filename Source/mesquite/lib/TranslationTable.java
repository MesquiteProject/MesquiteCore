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

/* ��������������������������� tree stuff ������������������������������� */
/* ======================================================================== */
/** A translation table linking taxa with alternative tokens */
public class TranslationTable {
	Taxa taxa;
	String[] labels;

	public TranslationTable (Taxa taxa){
		if (taxa!=null) {
			labels = new String[taxa.getNumTaxa()];
			this.taxa = taxa;
		}
	}
	public Taxon getTaxon(String label){
		if (labels==null || taxa == null || label == null)
			return null;
		for (int i=0; i<labels.length; i++){
			if (label.equalsIgnoreCase(labels[i])) {
				return taxa.getTaxon(i);
			}
		}
		return null;
	}
	public String getLabel(Taxon taxon){
		int i = taxa.whichTaxonNumber(taxon);
		if (i>=0 && i<taxa.getNumTaxa()) {
			if (i>=labels.length)
				resetSizes();
			return labels[i];
		}
		return null;

	}
	public String getLabel(int i){
		if (taxa==null || labels == null)
			return null;
		if (i>=0 && i<taxa.getNumTaxa()) {
			if (i>=labels.length)
				resetSizes();
			return labels[i];
		}
		return null;

	}
	public void setLabel(Taxon taxon, String label, boolean checkDuplicates){
		if (taxa==null)
			return;
		int i = taxa.whichTaxonNumber(taxon);
		if (i>=0 && i<taxa.getNumTaxa()) {
			if (i>=labels.length)
				resetSizes();
			labels[i] = label;
		}
		if (checkDuplicates)
			checkDuplicates();
	}

	public void taxaModified(int category, int starting, int num){
		int numTaxa = taxa.getNumTaxa();
		/*
		String[] newLabels = new String[numTaxa];

		if (category==MesquiteListener.PARTS_ADDED) {
			for (int i=0; i<=starting; i++)
				newLabels[i] = labels[i];
			for (int i=starting+num; i<numTaxa; i++)
				newLabels[i] = labels[i-num];
			nullify();
		}
		else if (category==MesquiteListener.PARTS_DELETED){
			for (int i=0; i<starting; i++)
				newLabels[i] = labels[i];
			for (int i=starting+num; i<labels.length; i++) {
				newLabels[i-num] = labels[i];
			}
			nullify();
		}
		else if (category==MesquiteListener.PARTS_MOVED){
			MesquiteMessage.warnProgrammer("taxa moved not working for translation table update");
			MesquiteMessage.printStackTrace();
		}
		labels = newLabels;
		checkDuplicates();
		 */
		if (category==MesquiteListener.PARTS_ADDED) {
			labels = new String[numTaxa];
		}
		else if (category==MesquiteListener.PARTS_DELETED){
			labels = new String[numTaxa];
		}
		else if (category==MesquiteListener.PARTS_MOVED){
			labels = new String[numTaxa];
		}
	}
	public void resetSizes(){
		int numTaxa = taxa.getNumTaxa();
		if (labels == null) {
			labels = new String[numTaxa];
		}
		else if (labels.length!=taxa.getNumTaxa()) {
			labels = new String[numTaxa];
			/*
			String[] newLabels = new String[numTaxa];
			for (int i=0; i<numTaxa && i<labels.length; i++)
				newLabels[i] = labels[i];
			labels = newLabels;
			 */
		}
	}

	public void cleanUpTable(){  //adds labels where needed then checks for duplicates
		if (labels == null) //TODO: construct new labels
			return;
		int numTaxa = taxa.getNumTaxa();
		if (numTaxa>labels.length){
			String[] newLabels = new String[numTaxa];
			for (int i=0; i<numTaxa && i<labels.length; i++)
				newLabels[i] = labels[i];
			labels = newLabels;
		}
		for (int i=0; i<labels.length; i++) {
			if (labels[i] ==null){
				labels[i] = "n" + i;
			}
		}
		checkDuplicates();

	}
	public void checkDuplicates(){
		if (labels == null) //TODO: construct new labels
			return;
		for (int i=0; i<labels.length; i++) {
			if (labels[i] !=null){
				while (StringArray.indexOfIgnoreCase(labels, labels[i]) !=StringArray.lastIndexOfIgnoreCase(labels, labels[i]))
					labels[i] = "t" + labels[i] + "t";
			}
		}
		for (int i=0; i<labels.length; i++) {
			if (labels[i] ==null){
				labels[i] = Integer.toString(Taxon.toExternal(i));
				while (StringArray.indexOfIgnoreCase(labels, labels[i]) !=StringArray.lastIndexOfIgnoreCase(labels, labels[i]))
					labels[i] = "t" + labels[i] + "t";
			}
		}
	}
}



