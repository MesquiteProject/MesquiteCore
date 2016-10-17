/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 

 
 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
 Perhaps with your help we can be more than a few, and make Mesquite better.

 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
 Mesquite's web site is http://mesquiteproject.org

 This source code and its compiled class files are free and modifiable under the terms of 
 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.assoc.lib;

import java.awt.*;
import java.util.*;
import mesquite.lib.*;


/* ======================================================================== */
class TaxonWithTaxa{
	Taxon t = null;
	Taxa taxaB;
	Taxon[] associates;
	int numA = 0;
	public TaxonWithTaxa(Taxon t, Taxa taxaB){
		this.t = t;
		associates= new Taxon[taxaB.getNumTaxa()];
		this.taxaB = taxaB;
	}
	Taxon getTaxon(){
		return t;
	}
	Taxon getAssociate(int i){
		if (i<0 || i>=associates.length)
			return null;
		return associates[i];
	}
	void addAssociate(Taxon tB){
		if (tB==null)
			return;
		for (int i=0; i<associates.length; i++){
			if (tB == associates[i])
				return;
			if (associates[i] == null) {
				associates[i] = tB;
				return;
			}
		}
	}
	void deleteDefunct(){
		for (int where = associates.length-1; where>=0; where--){
			Taxon tD = associates[where];
			if (tD!=null && taxaB.whichTaxonNumber(tD)<0)
				deleteAssociate(tD);
		}
	}
	void deleteAssociate(Taxon tB){
		if (tB==null)
			return;
		int where = findAssociate(tB);
		int last = findLastAssociate();
		if (where>-1) { 
			associates[where] = null;
			if (last>-1) {
				associates[where] = associates[last];
				associates[last] = null;
			}
		}
	}

	void deleteAllAssociates(){
		for (int i=0; i<associates.length; i++)
			associates[i] = null;
	}

	int findAssociate(Taxon tB){
		for (int i=0; i<associates.length; i++){
			if (tB == associates[i])
				return i;
		}
		return -1;
	}
	int findLastAssociate(){
		for (int i=0; i<associates.length; i++){
			if (associates[i] == null) {
				return i-1;
			}
		}
		return -1;
	}
	int length(){
		return associates.length;
	}
	public String toString(){
		String s = "With " + t.getName() + ":";
		for (int i=0; i<associates.length; i++){
			if (associates[i] != null) {
				s += " \"" + associates[i].getName() + "\"";
			}
		}
		return s;
	}
}
/** .*/
public class TaxaAssociation extends FileElement  {
	//TODO: needs to listen to changes in taxa and respond to change numbers etc.
	Taxa taxaA, taxaB;
	TaxonWithTaxa[] associations;
	public TaxaAssociation(){
	}
	/*.................................................................................................................*/
	public void setTaxa(Taxa taxa, int index){
		if (index==0)
			taxaA = taxa;
		else if (index==1)
			taxaB = taxa;
		else
			MesquiteMessage.warnProgrammer("Error: Taxa association allows only two Taxa objects");
		if (taxaA!=null && taxaB!=null) {
			associations= new TaxonWithTaxa[taxaA.getNumTaxa()];
			for (int i=0; i<taxaA.getNumTaxa(); i++) {
				associations[i] = new TaxonWithTaxa(taxaA.getTaxon(i), taxaB);
				for (int j = 0; j<taxaB.getNumTaxa(); j++){
					setAssociated(i, j, false);
				}
			}
		}
		else
			associations = null;
	}
	public String getDefaultIconFileName(){ //for small 16 pixel icon at left of main bar
		return "taxaAssocSmall.gif";
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets taxon N in block A as NOT associated with taxon M in block B", "[N][M]", commandName, "deassociate")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int a = MesquiteInteger.fromString(arguments, io);
			int b = MesquiteInteger.fromString(arguments, io);
			setAssociated(a, b, false);
		}
		else if (checker.compare(this.getClass(), "Sets taxon N in block A as associated with taxon M in block B", "[N][M]", commandName, "associate")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int a = MesquiteInteger.fromString(arguments, io);
			int b = MesquiteInteger.fromString(arguments, io);
			setAssociated(a, b, true);
		}
		else
			return  super.doCommand(commandName, arguments, checker);
			return null;
	}
	/*.................................................................................................................*/
	public Taxon getRandomAssociate(Taxon taxon, RandomBetween rng) {
		if (taxon==null) return null;
		Taxon[] associates = getAssociates(taxon);
		if (associates==null) return null;
		Taxon associate=null;
		if (associates.length==1 || rng==null)
			associate = associates[0];
		else
			associate = associates[rng.randomIntBetween(0, associates.length-1)];
		return associate;
	}

	/*.................................................................................................................*/
	private void moveAssociation(int a , int b) {
		for (int it=0; it<taxaA.getNumTaxa(); it++)
			setAssociated(it, b, false);
		setAssociated(a, b, true);
	}
	/*.................................................................................................................*/
	public void moveAssociation(Taxon taxonA, Taxon taxonB){
		if (taxaA==null || taxaB == null)
			return;
		int a = taxaA.whichTaxonNumber(taxonA);
		int b;
		//enlarge matrices!
		if (a>-1) {  // taxonA belongs to taxaA
			b = taxaB.whichTaxonNumber(taxonB);
			if (b!=-1)
				moveAssociation(a, b);
		} else
		{ //either illegal or second taxon belongs to first Taxa
			a = taxaB.whichTaxonNumber(taxonA);
			b = taxaA.whichTaxonNumber(taxonB);
			if (a!=-1 && b!=-1)
				moveAssociation(b,a);
		}
	}

	/*.................................................................................................................*/
	public TaxaAssociation cloneAssociation(){
		TaxaAssociation newAssociation = new TaxaAssociation();
		newAssociation.setTaxa(getTaxa(0),0);
		newAssociation.setTaxa(getTaxa(1),1);
		for (int a=0;a<getTaxa(0).getNumTaxa() ; a++){
			for (int b=0;b<getTaxa(1).getNumTaxa(); b++){
				if (areAssociated(a,b))
					newAssociation.setAssociated(a,b,true);
			}
		}
		newAssociation.setName(getName());
		return newAssociation;
	}
	/*.................................................................................................................*/
	public int getNumWithNoAssociates(Taxa taxa) {
		int count=0;
		for (int it=0; it<taxa.getNumTaxa(); it++)
			if (getNumAssociates(taxa.getTaxon(it))<1) {
				count++;
			}
		return count;
	}
	/*.................................................................................................................*/
	/** true iff Taxon taxon is the only associate of the taxon to which it is associated */
	public boolean onlyAssociate(Taxon taxon) {
		if (taxaA==null || taxaB == null)
			return false;
		int a = taxaA.whichTaxonNumber(taxon);
		if (a>-1) { //taxon belongs to taxaA
			for (int it=0; it<taxaB.getNumTaxa(); it++) {
				if (areAssociated(a,it)) {
					Taxon associate = taxaB.getTaxon(it);
					if (getNumAssociates(associate)==1)
						return true;
				}
			}
		}
		else {
			int b = taxaB.whichTaxonNumber(taxon);
			for (int it=0; it<taxaA.getNumTaxa(); it++) {
				if (areAssociated(it,b)) {  //  this taxon is associated with it in taxonA
					Taxon associate = taxaA.getTaxon(it);
					if (getNumAssociates(associate)==1)
						return true;
				}
			}
		}
		return false;
	}
	/*.................................................................................................................*/
	/** true iff Taxon taxon is the only associate of the taxon to which it is associated */
	public boolean lessThanNumberOfAssociates(Taxon taxon, int minimum) {
		if (taxaA==null || taxaB == null)
			return false;
		int a = taxaA.whichTaxonNumber(taxon);
		if (a>-1) { //taxon belongs to taxaA
			for (int it=0; it<taxaB.getNumTaxa(); it++) {
				if (areAssociated(a,it)) {
					Taxon associate = taxaB.getTaxon(it);
					int numAssoc = getNumAssociates(associate);
					if (numAssoc>0 && numAssoc<minimum)
						return true;
				}
			}
		}
		else {
			int b = taxaB.whichTaxonNumber(taxon);
			for (int it=0; it<taxaA.getNumTaxa(); it++) {
				if (areAssociated(it,b)) {  //  this taxon is associated with it in taxonA
					Taxon associate = taxaA.getTaxon(it);
					int numAssoc = getNumAssociates(associate);
					if (numAssoc>0 && numAssoc<minimum)
						return true;
				}
			}
		}
		return false;
	}
	/*.................................................................................................................*/
	/** Copies associations in {@code association} to this instance of {@code TaxaAssociation}.
	 * 
	 * <p>May lead to contained (gene) taxa being associated with multiple containing (species) 
	 * taxons.  To avoid this, see {@link #copyAssociates(TaxaAssociation, boolean)}.</p>
	 * 
	 * @param	association	the association to copy.*/
	public void copyAssociates(TaxaAssociation association){
		copyAssociates(association, false);
	}
	/*.................................................................................................................*/
	/** Copies associations in {@code association} to this instance of {@code TaxaAssociation}.
	 * 
	 * @param	association	the association to copy.
	 * 
	 * @param	zeroAll	boolean indicating whether or not to zero all current associations before copying.  If set to 
	 * {@code false}, copying may result in contained (gene) taxa being associated with multiple containing (species) 
	 * taxons.*/
	public void copyAssociates (TaxaAssociation association, boolean zeroAll) {
		if (association == null) {
			return;
		}
		Taxa sourceTaxaA = association.getTaxa(0);
		Taxa sourceTaxaB = association.getTaxa(1);
		if (taxaA.getNumTaxa()!= sourceTaxaA.getNumTaxa() || taxaB.getNumTaxa()!= sourceTaxaB.getNumTaxa()) {
			return;
		}
		if (zeroAll) {
			zeroAllAssociations();
		}
		for (int a=0; a<taxaA.getNumTaxa(); a++) {
			for (int b=0; b<taxaB.getNumTaxa(); b++) {
				setAssociated(a,b,association.getAssociation(sourceTaxaA.getTaxon(a), sourceTaxaB.getTaxon(b)));
			}
		}
	}
	/*.................................................................................................................*/
	private boolean areAssociated(int a, int b){
		if (a >=0 && a < taxaA.getNumTaxa() && b >= 0 && b < taxaB.getNumTaxa()){
			Taxon taxonA = taxaA.getTaxon(a);
			if (taxonA == null)
				return false;
			for (int i= 0; i<associations.length; i++)
				if (associations[i].t == taxonA)
					return associations[i].findAssociate(taxaB.getTaxon(b))>=0;
		}
		return false;
	}
	/*.................................................................................................................*/
	public boolean equals(TaxaAssociation association){
		if (association==null)
			return false;
		Taxa otherA = association.getTaxa(0);
		Taxa otherB = association.getTaxa(1);
		if (taxaA.getNumTaxa() != otherA.getNumTaxa() || taxaB.getNumTaxa() != otherB.getNumTaxa())
			return false;
		for (int a=0;a<taxaA.getNumTaxa() ; a++){
			for (int b=0;b<taxaB.getNumTaxa(); b++){
				if (areAssociated(a,b) !=association.areAssociated(a,b))
					return false;
			}
		}
		return true;
	}
	/*.................................................................................................................*/
	public boolean hasSamePattern(TaxaAssociation association){   //taxonA can differ
		if (association==null)
			return false;
		Taxa otherA = association.getTaxa(0);
		Taxa otherB = association.getTaxa(1);
		
		if (taxaA.getNumTaxa() != otherA.getNumTaxa() || taxaB.getNumTaxa() != otherB.getNumTaxa())
			return false;
		

		int[] otherEquivalentToLocal = new int[otherA.getNumTaxa()];
		for (int a=0; a<taxaA.getNumTaxa(); a++) {
			boolean foundOne = false;
			otherEquivalentToLocal[a]=a;  // default set it to be the same
			for (int b=0;b<taxaB.getNumTaxa() && !foundOne; b++){
				if (areAssociated(a,b)){ // find something that is in this bin in taxonA
					//now see where it is in otherA
					for (int c=0; c<otherA.getNumTaxa()&& !foundOne; c++) {
						if (association.areAssociated(c, b)){  // here it is
							otherEquivalentToLocal[a]=c;
							foundOne=true;
						}
					}

				}
			}
		}
		
		for (int a=0;a<taxaA.getNumTaxa() ; a++){
			for (int b=0;b<taxaB.getNumTaxa(); b++){
				if (areAssociated(a,b) !=association.areAssociated(otherEquivalentToLocal[a],b))
					return false;
			}
		}
		return true;
	}
	/*.................................................................................................................*/
	private void setAssociated(int a, int b, boolean assoc){
		if (a >=0 && a < taxaA.getNumTaxa() && b >= 0 && b < taxaB.getNumTaxa()) {
			Taxon taxonA = taxaA.getTaxon(a);
			if (taxonA == null)
				return;
			for (int i= 0; i<associations.length; i++)
				if (associations[i] != null && associations[i].t == taxonA){

					if (assoc){
						associations[i].addAssociate(taxaB.getTaxon(b));
					}
					else {
						associations[i].deleteAssociate(taxaB.getTaxon(b));
					}
				}
		}
	}
	/*.................................................................................................................*/
	public void replaceWith(TaxaAssociation source){

	}
	/*.................................................................................................................*/
	public void zeroAllAssociations(){
		for (int it=0; it<taxaA.getNumTaxa(); it++)
			zeroAllAssociations(taxaA.getTaxon(it));
	}

	/*.................................................................................................................*/
	public void zeroAllAssociations(Taxon taxon){
		if (taxaA==null || taxaB == null)
			return;

		int a = taxaA.whichTaxonNumber(taxon);
		if (a>-1) { //taxon belongs to taxaA
			associations[a].deleteAllAssociates();
		}
		else {
			//taxon belongs to taxaB; remove all references to it
			for (int i = 0; i<taxaA.getNumTaxa(); i++)
				associations[i].deleteAssociate(taxon);
		}
	}
	/*.................................................................................................................*/
	public void resetTaxaAfterChange(){
		//go through deleting defunct taxa
		for (int a = 0; a < associations.length; a++) {
			if (taxaA.whichTaxonNumber(associations[a].t)<0)
				associations[a] = null;
			else {
				associations[a].deleteDefunct();
			}	
		}
		//change size of arrays as needed
		TaxonWithTaxa[] newAssociations= new TaxonWithTaxa[taxaA.getNumTaxa()];

		int count = 0;
		for (int i=0; i<associations.length; i++) {
			if (associations[i]!=null){
				TaxonWithTaxa oldT = associations[i];
				newAssociations[count] = new TaxonWithTaxa(oldT.getTaxon(), taxaB);
				for (int j = 0; j<oldT.length(); j++){
					newAssociations[count].addAssociate(oldT.getAssociate(j));
				}
				count++;
			}
		}
		for (int i=count; i<taxaA.getNumTaxa(); i++)
			newAssociations[i] = new TaxonWithTaxa(taxaA.getTaxon(i), taxaB);
		associations = newAssociations;
	}
	/*-----------------------------------------*/
	public String toString(){
		String names = "";
		if (taxaA!=null && taxaB!=null)
			names =  " involving " + taxaA.getName() + " and " + taxaB.getName();
		String s = "Taxa Association " + getName() + names+  "\n";
		for (int i=0; i<associations.length; i++) {
			if (associations[i]!=null){
				s += "  " + associations[i] + "\n";
			}
		}
		return s;

	}
	/*-----------------------------------------*/
	/** Returns explanation (what taxa involved)*/
	public String getExplanation(){
		String s ="This stores the association between taxa of ";
		if (taxaA==null || taxaB == null)
			s+= "two blocks";
		else 
			s+=  "the block \"" + taxaA.getName() + "\", having " + taxaA.getNumTaxa() + " taxa, with taxa of the block \"" + taxaB.getName() + "\", having " + taxaB.getNumTaxa() + " taxa";
		return s;
	}
	/*-----------------------------------------*/
	/** For HNode interface.  todo: this should be renamed getHNodeShow*/
	public boolean getHShow(){
		return true;
	}

	/*-----------------------------------------*/
	/** For HNode interface.*/
	public String getTypeName(){
		return "Taxa Association";
	}
	/*.................................................................................................................*/
	public Taxa getTaxa(int index){
		if (index==0)
			return taxaA;
		else if (index==1)
			return taxaB;
		else
			MesquiteMessage.warnProgrammer("Error: Taxa association allows only two Taxa objects *");
		return null;
	}
	/*.................................................................................................................*/
	public Taxa getOtherTaxa(Taxa taxa){
		if (taxa==taxaB)
			return taxaA;
		else if (taxa == taxaA)
			return taxaB;
		else
			MesquiteMessage.warnProgrammer("Error: taxa passed to getOtherTaxa not present *");
		return null;
	}
	/*.................................................................................................................*/
	public void setAssociation(Taxon taxonA, Taxon taxonB, boolean associated){
		if (taxaA==null || taxaB == null)
			return;
		int a = taxaA.whichTaxonNumber(taxonA);
		int b;
		//enlarge matrices!
		if (a==-1) { //either illegal or second taxon belongs to first Taxa
			a = taxaB.whichTaxonNumber(taxonA);
			b = taxaA.whichTaxonNumber(taxonB);
			if (a!=-1 && b!=-1)
				setAssociated(b,a, associated);
		}
		else  {
			b = taxaB.whichTaxonNumber(taxonB);
			if (b!=-1)
				setAssociated(a, b, associated);
		}
	}

	/*.................................................................................................................*/
	public boolean getAssociation(Taxon taxonA, Taxon taxonB){
		if (!includedTaxon(taxonA) || !includedTaxon(taxonA))
			return false;
		int a = taxaA.whichTaxonNumber(taxonA);
		int b;
		if (a==-1) { //either illegal or second taxon belongs to first Taxa
			a = taxaB.whichTaxonNumber(taxonA);
			b = taxaA.whichTaxonNumber(taxonB);
			if (a!=-1 && b!=-1)
				return areAssociated(b,a);
		}
		else  {
			b = taxaB.whichTaxonNumber(taxonB);
			if (b!=-1)
				return areAssociated(a, b);
		}
		return false;
	}
	/*.................................................................................................................*/
	public int getNumAssociates(Taxon taxon){
		if (!includedTaxon(taxon))
			return 0;
		int which = taxaA.whichTaxonNumber(taxon);
		if (which==-1) { //taxon belongs to B; need to look down matrix
			which = taxaB.whichTaxonNumber(taxon);
			if (which==-1)
				return 0;
			int num = 0;
			for (int i=0; i<taxaA.getNumTaxa(); i++) //counting how many associates
				if (areAssociated(i, which))//bug pre-1.1build62: used associates index instead of taxaA index
					num++;
			return num;
		}
		else   { //taxon belongs to A; need to look across matrix
			int num = 0;
			for (int i=0; i<taxaB.getNumTaxa(); i++) //counting how many associates
				if (areAssociated(which,i))//bug pre-1.1build62: used associates index instead of taxaB index
					num++;
			return num;
		}
	}
	/*.................................................................................................................*/
	/* this version avoids instantiation of Taxon array*/
	public Taxon[] getAssociates(Taxon taxon, Taxon[] result){

		if (!includedTaxon(taxon))
			return null;
		int which = taxaA.whichTaxonNumber(taxon);
		if (which==-1) { //taxon belongs to B; need to look down matrix
			which = taxaB.whichTaxonNumber(taxon);
			if (which==-1)
				return null;
			int num = 0;
			for (int i=0; i<taxaA.getNumTaxa(); i++) //counting how many associates //bug pre-1.1build62: used associates index instead of taxaA index
				if (areAssociated(i, which))
					num++;
			if (num==0)
				return null;
			if (result==null || result.length < num)
				result = new Taxon[num];
			for (int i=0; i<result.length; i++)
				result[i] = null;
			int count=0;
			for (int i=0; i<taxaA.getNumTaxa(); i++) //accumulating associates //bug pre-1.1build62: used associates index instead of taxaA index
				if (areAssociated(i, which)) {
					if (count<result.length)
						result[count] = taxaA.getTaxon(i);
					count++;
				}
			return result;
		}
		else   { //taxon belongs to A; need to look across matrix
			int num = 0;
			for (int i=0; i<taxaB.getNumTaxa(); i++) //counting how many associates
				if (areAssociated(which,i))//bug pre-1.1build62: used associates index instead of taxaB index
					num++;
			if (num==0)
				return null;
			if (result==null || result.length < num)
				result = new Taxon[num];
			for (int i=0; i<result.length; i++)
				result[i] = null;
			int count=0;
			for (int i=0; i<taxaB.getNumTaxa(); i++)  //accumulating associates
				if (areAssociated(which, i)) {//bug pre-1.1build62: used associates index instead of taxaB index
					if (count<result.length)
						result[count] = taxaB.getTaxon(i);
					count++;
				}
			return result;
		}
	}
	/*.................................................................................................................*/
	public Taxon[] getAssociates(Taxon taxon){
		if (!includedTaxon(taxon))
			return null;
		int which = taxaA.whichTaxonNumber(taxon);
		if (which==-1) { //taxon belongs to B; need to look down matrix
			which = taxaB.whichTaxonNumber(taxon);
			if (which==-1)
				return null;
			int num = 0;
			for (int i=0; i<taxaA.getNumTaxa(); i++) //counting how many associates
				if (areAssociated(i, which))//bug pre-1.1build62: used associates index instead of taxaA index
					num++;
			if (num==0)
				return null;
			Taxon[] result = new Taxon[num];
			int count=0;
			for (int i=0; i<taxaA.getNumTaxa(); i++) //accumulating associates
				if (areAssociated(i, which))//bug pre-1.1build62: used associates index instead of taxaA index
					result[count++] = taxaA.getTaxon(i);
			return result;
		}
		else   { //taxon belongs to A; need to look across matrix
			int num = 0;
			for (int i=0; i<taxaB.getNumTaxa(); i++) //counting how many associates
				if (areAssociated(which,i))//bug pre-1.1build62: used associates index instead of taxaB index
					num++;
			if (num==0)
				return null;
			Taxon[] result = new Taxon[num];
			int count=0;
			for (int i=0; i<taxaB.getNumTaxa(); i++)  //accumulating associates
				if (areAssociated(which,i))//bug pre-1.1build62: used associates index instead of taxaB index
					result[count++] = taxaB.getTaxon(i);
			return result;
		}
	}
	/*.................................................................................................................*/
	public Bits getAssociatesAsBits(Taxa taxa, int taxonNumber){
		Taxon taxon = taxa.getTaxon(taxonNumber);
		if (!includedTaxon(taxon))
			return null;
		int which = taxaA.whichTaxonNumber(taxon);
		if (which==-1) { //taxon belongs to B; need to look down matrix
			which = taxaB.whichTaxonNumber(taxon);
			if (which==-1)
				return null;
			int num = 0;
			for (int i=0; i<taxaA.getNumTaxa(); i++) //counting how many associates
				if (areAssociated(i, which))//bug pre-1.1build62: used associates index instead of taxaA index
					num++;
			if (num==0)
				return null;
			Bits result = new Bits(taxaA.getNumTaxa());
			result.clearAllBits();
			int count=0;
			for (int i=0; i<taxaA.getNumTaxa(); i++) //accumulating associates
				if (areAssociated(i, which)){//bug pre-1.1build62: used associates index instead of taxaA index
					result.setBit(i);
				}
			return result;
		}
		else   { //taxon belongs to A; need to look across matrix
			int num = 0;
			for (int i=0; i<taxaB.getNumTaxa(); i++) //counting how many associates
				if (areAssociated(which,i))//bug pre-1.1build62: used associates index instead of taxaB index
					num++;
			if (num==0)
				return null;
			Bits result = new Bits(taxaB.getNumTaxa());
			result.clearAllBits();
			int count=0;
			for (int i=0; i<taxaB.getNumTaxa(); i++)  //accumulating associates
				if (areAssociated(which,i))//bug pre-1.1build62: used associates index instead of taxaB index
					result.setBit(i);
			return result;
		}
	}
	public boolean includedTaxon(Taxon taxon){
		if (taxaA==null || taxaB == null)
			return false;
		int which = taxaA.whichTaxonNumber(taxon);
		if (which==-1) { //taxon belongs to B; need to look down matrix
			which = taxaB.whichTaxonNumber(taxon);
			if (which==-1 || which >= taxaB.getNumTaxa())
				return false;
		}
		else   { //taxon belongs to A; need to look across matrix
			if (which >= taxaA.getNumTaxa())
				return false;
		}
		return true;
	}
}

