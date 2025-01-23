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
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.Taxon;


/* ======================================================================== */
class TaxonWithTaxa{
	Taxon taxonContaining = null;
	Taxa taxaContained;
	Taxon[] associates;
	int numA = 0;
	public TaxonWithTaxa(Taxon tContaining, Taxa taxaContained){
		this.taxonContaining = tContaining;
		associates= new Taxon[taxaContained.getNumTaxa()];
		this.taxaContained = taxaContained;
	}
	Taxon getTaxon(){
		return taxonContaining;
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
	void deleteDefunctContained(){
		for (int where = associates.length-1; where>=0; where--){
			Taxon tD = associates[where];
			if (tD!=null && taxaContained.whichTaxonNumber(tD)<0)
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
		String s = "With " + taxonContaining.getName() + ":";
		for (int i=0; i<associates.length; i++){
			if (associates[i] != null) {
				s += " \"" + associates[i].getName() + "\"";
			}
		}
		return s;
	}
}
/**========================================================*/
public class TaxaAssociation extends FileElement  {
	//TODO: needs to listen to changes in taxa and respond to change numbers etc.
	Taxa taxaContaining, taxaContained;
	TaxonWithTaxa[] associations;
	public TaxaAssociation(){
	}
	/*.................................................................................................................*/
	public void setTaxa(Taxa taxa, int index){
		if (index==0)
			taxaContaining = taxa;
		else if (index==1)
			taxaContained = taxa;
		else
			MesquiteMessage.warnProgrammer("Error: Taxa association allows only two Taxa objects");
		if (taxaContaining!=null && taxaContained!=null) {
			associations= new TaxonWithTaxa[taxaContaining.getNumTaxa()];
			for (int i=0; i<taxaContaining.getNumTaxa(); i++) {
				associations[i] = new TaxonWithTaxa(taxaContaining.getTaxon(i), taxaContained);
				for (int j = 0; j<taxaContained.getNumTaxa(); j++){
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
		if (checker.compare(this.getClass(), "Sets taxon N in block Containing as NOT associated with taxon M in block Contained", "[N][M]", commandName, "deassociate")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int a = MesquiteInteger.fromString(arguments, io);
			int b = MesquiteInteger.fromString(arguments, io);
			setAssociated(a, b, false);
		}
		else if (checker.compare(this.getClass(), "Sets taxon N in block Containing as associated with taxon M in block Contained", "[N][M]", commandName, "associate")) {
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
		for (int it=0; it<taxaContaining.getNumTaxa(); it++)
			setAssociated(it, b, false);
		setAssociated(a, b, true);
	}
	/*.................................................................................................................*/
	public void moveAssociation(Taxon taxonA, Taxon taxonB){
		if (taxaContaining==null || taxaContained == null)
			return;
		int a = taxaContaining.whichTaxonNumber(taxonA);
		int b;
		//enlarge matrices!
		if (a>-1) {  // taxonA belongs to taxaA
			b = taxaContained.whichTaxonNumber(taxonB);
			if (b!=-1)
				moveAssociation(a, b);
		} else
		{ //either illegal or second taxon belongs to first Taxa
			a = taxaContained.whichTaxonNumber(taxonA);
			b = taxaContaining.whichTaxonNumber(taxonB);
			if (a!=-1 && b!=-1)
				moveAssociation(b,a);
		}
	}

	public void transposeAssociation(){
		TaxaAssociation tempAssociation = cloneAssociation(); 
		setTaxa(tempAssociation.getContainedTaxa(), 0); //notice flip
		setTaxa(tempAssociation.getContainingTaxa(), 1); //notice flip
		tempAssociation.setName("Transposed (" + getName() + ")");
		for (int a=0;a<taxaContaining.getNumTaxa() ; a++){
			for (int b=0;b<taxaContained.getNumTaxa(); b++){
				if (tempAssociation.areAssociated(taxaContaining.getTaxon(a),taxaContained.getTaxon(b)))
					setAssociated(taxaContaining.getTaxon(a),taxaContained.getTaxon(b),true);
			}
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
		if (taxaContaining==null || taxaContained == null)
			return false;
		int a = taxaContaining.whichTaxonNumber(taxon);
		if (a>-1) { //taxon belongs to taxaA
			for (int it=0; it<taxaContained.getNumTaxa(); it++) {
				if (areAssociated(a,it)) {
					Taxon associate = taxaContained.getTaxon(it);
					if (getNumAssociates(associate)==1)
						return true;
				}
			}
		}
		else {
			int b = taxaContained.whichTaxonNumber(taxon);
			for (int it=0; it<taxaContaining.getNumTaxa(); it++) {
				if (areAssociated(it,b)) {  //  this taxon is associated with it in taxonA
					Taxon associate = taxaContaining.getTaxon(it);
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
		if (taxaContaining==null || taxaContained == null)
			return false;
		int a = taxaContaining.whichTaxonNumber(taxon);
		if (a>-1) { //taxon belongs to taxaA
			for (int it=0; it<taxaContained.getNumTaxa(); it++) {
				if (areAssociated(a,it)) {
					Taxon associate = taxaContained.getTaxon(it);
					int numAssoc = getNumAssociates(associate);
					if (numAssoc>0 && numAssoc<minimum)
						return true;
				}
			}
		}
		else {
			int b = taxaContained.whichTaxonNumber(taxon);
			for (int it=0; it<taxaContaining.getNumTaxa(); it++) {
				if (areAssociated(it,b)) {  //  this taxon is associated with it in taxonA
					Taxon associate = taxaContaining.getTaxon(it);
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
		Taxa sourceTaxaContaining = association.getTaxa(0);
		Taxa sourceTaxaContained = association.getTaxa(1);
		if (taxaContaining.getNumTaxa()!= sourceTaxaContaining.getNumTaxa() || taxaContained.getNumTaxa()!= sourceTaxaContained.getNumTaxa()) {
			return;
		}
		if (zeroAll) {
			zeroAllAssociations();
		}
		for (int a=0; a<taxaContaining.getNumTaxa(); a++) {
			for (int b=0; b<taxaContained.getNumTaxa(); b++) {
				setAssociated(a,b,association.getAssociation(sourceTaxaContaining.getTaxon(a), sourceTaxaContained.getTaxon(b)));
			}
		}
	}
	/*.................................................................................................................*/
	private boolean areAssociated(int a, int b){
		if (a >=0 && a < taxaContaining.getNumTaxa() && b >= 0 && b < taxaContained.getNumTaxa()){
			Taxon taxonA = taxaContaining.getTaxon(a);
			if (taxonA == null)
				return false;
			for (int i= 0; i<associations.length; i++)
				if (associations[i].taxonContaining == taxonA)
					return associations[i].findAssociate(taxaContained.getTaxon(b))>=0;
		}
		return false;
	}
	/*.................................................................................................................*/
	private boolean areAssociated(Taxon taxonA, Taxon taxonB){
		if (taxonA == null)
			return false;
		for (int i= 0; i<associations.length; i++){
			if (associations[i].taxonContaining == taxonA)
				return associations[i].findAssociate(taxonB)>=0;
		}
		for (int i= 0; i<associations.length; i++){
			if (associations[i].taxonContaining == taxonB)
				return associations[i].findAssociate(taxonA)>=0;
		}
		return false;
	}
	/*.................................................................................................................*/
	public boolean equals(TaxaAssociation association){
		if (association==null)
			return false;
		Taxa otherA = association.getTaxa(0);
		Taxa otherB = association.getTaxa(1);
		if (taxaContaining.getNumTaxa() != otherA.getNumTaxa() || taxaContained.getNumTaxa() != otherB.getNumTaxa())
			return false;
		for (int a=0;a<taxaContaining.getNumTaxa() ; a++){
			for (int b=0;b<taxaContained.getNumTaxa(); b++){
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

		if (taxaContaining.getNumTaxa() != otherA.getNumTaxa() || taxaContained.getNumTaxa() != otherB.getNumTaxa())
			return false;


		int[] otherEquivalentToLocal = new int[otherA.getNumTaxa()];
		for (int a=0; a<taxaContaining.getNumTaxa(); a++) {
			boolean foundOne = false;
			otherEquivalentToLocal[a]=a;  // default set it to be the same
			for (int b=0;b<taxaContained.getNumTaxa() && !foundOne; b++){
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

		for (int a=0;a<taxaContaining.getNumTaxa() ; a++){
			for (int b=0;b<taxaContained.getNumTaxa(); b++){
				if (areAssociated(a,b) !=association.areAssociated(otherEquivalentToLocal[a],b))
					return false;
			}
		}
		return true;
	}

	/*.................................................................................................................*/
	public void mergeTaxa(Taxa taxa, boolean[] selected, int destinationTaxonI){
		Taxon destinationTaxon = taxa.getTaxon(destinationTaxonI);
		if (taxa == taxaContaining){
			//move all contained within it into destinationTaxon
			for (int it = 0; it<selected.length; it++){
				if (it != destinationTaxonI && selected[it]){
					Taxon mergingTaxon = taxa.getTaxon(it);
					Taxon[] contained = getAssociates(mergingTaxon);
					for (int itB=0; itB<contained.length; itB++){
						setAssociation(mergingTaxon, contained[itB], false); //disconnected from one merging
						setAssociation(destinationTaxon, contained[itB], true); //put into merging destination
					}
				}
			}
		}
		else if (taxa == taxaContained){
			//if the set merging are all within one containing, then simply delete all but the destination from associations
			//if the set merging are in various containing, delete all but one copy from each of the containing, and convert
			//  the non-destination ones to the destination taxon
			for (int itA = 0; itA<taxaContaining.getNumTaxa(); itA++){ //for each of the containing
				Taxon containingTaxon = taxaContaining.getTaxon(itA);
				Taxon[] assocOfITA = getAssociates(containingTaxon);

				int firstFound = -1;
				boolean destFound = false;
				for (int itAssoc = 0; itAssoc<assocOfITA.length; itAssoc++){
					int itContained = taxaContained.whichTaxonNumber(assocOfITA[itAssoc]);
					if (areAssociated(itA, itContained) && selected[itContained]){ //this one is contained, but is it the destinationt taxon?
						if (firstFound <0)
							firstFound = itContained;
						if (itContained == destinationTaxonI)
							destFound = true;
					}
				}

				//OK, we've found whether any are included (firstFound) and whether the destination taxon is included (destFound)
				if (firstFound>=0){ //there is a merging one among the contained
					// If destination taxon is included; everyone but it is deleted
					// if destination taxon is NOT included; everyone is deleted & destination is added
					for (int it = 0; it<selected.length; it++)
						if (selected[it] && it != destinationTaxonI)
							setAssociation(containingTaxon, taxaContained.getTaxon(it), false); //disconnected from one merging

					if (!destFound)
						setAssociation(containingTaxon, taxaContained.getTaxon(destinationTaxonI), true);
				}

			}
		}
	}

	static boolean warnedDuplicate = false;
	/*.................................................................................................................*/
	private void setAssociated(int containingTaxon, int containedTaxon, boolean assoc){
		if (containingTaxon >=0 && containingTaxon < taxaContaining.getNumTaxa() && containedTaxon >= 0 && containedTaxon < taxaContained.getNumTaxa()) {
			Taxon taxonA = taxaContaining.getTaxon(containingTaxon);
			if (taxonA == null)
				return;
			for (int i= 0; i<associations.length; i++){
				if (associations[i] != null && associations[i].taxonContaining == taxonA){
					Taxon taxonContained = taxaContained.getTaxon(containedTaxon);
					if (assoc){
						if (getAssociates(taxonContained)!=null){ //oops, already has this as an associate; don't add
							String warning = "WARNING: Contained taxon (" + taxonContained.getName() + ") is already placed within another containing taxon. This may cause issues for some calculations. This warning will not be given again.";
							if (!warnedDuplicate)
								MesquiteTrunk.mesquiteTrunk.logln(warning);
							warnedDuplicate = true;
						}
						associations[i].addAssociate(taxonContained);

					}
					else {
						associations[i].deleteAssociate(taxonContained);
					}
				}
			}
		}
	}
	private void setAssociated(Taxon taxonA, Taxon taxonB, boolean assoc){ //this allows for the two taxa to be in either block
		if (taxonA == null)
			return;
		for (int i= 0; i<associations.length; i++){
			if (associations[i] != null && associations[i].taxonContaining == taxonA){
				if (assoc){
					if (getAssociates(taxonB)!=null){ //oops, already has this as an associate; don't add
						String warning = "WARNING: Contained taxon (" + taxonB.getName() + ") is already placed within another containing taxon. This may cause issues for some calculations. This warning will not be given again.";
						if (!warnedDuplicate)
							MesquiteTrunk.mesquiteTrunk.logln(warning);
						warnedDuplicate = true;
					}
					associations[i].addAssociate(taxonB);

				}
				else {
					associations[i].deleteAssociate(taxonB);
				}
			}
		}
		for (int i= 0; i<associations.length; i++){
			if (associations[i] != null && associations[i].taxonContaining == taxonB){
				if (assoc)
					associations[i].addAssociate(taxonB);
				else 
					associations[i].deleteAssociate(taxonB);
			}
		}
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public void replaceWith(TaxaAssociation source){

	}
	/*.................................................................................................................*/
	public void zeroAllAssociations(){
		for (int it=0; it<taxaContaining.getNumTaxa(); it++)
			zeroAllAssociations(taxaContaining.getTaxon(it));
	}

	/*.................................................................................................................*/
	public void zeroAllAssociations(Taxon taxon){
		if (taxaContaining==null || taxaContained == null)
			return;

		int a = taxaContaining.whichTaxonNumber(taxon);
		if (a>-1) { //taxon belongs to taxaA
			associations[a].deleteAllAssociates();
		}
		else {
			//taxon belongs to taxaB; remove all references to it
			for (int i = 0; i<taxaContaining.getNumTaxa(); i++)
				associations[i].deleteAssociate(taxon);
		}
	}
	/*.................................................................................................................*/
	public void resetTaxaAfterChange(){
		//go through deleting defunct taxa
		for (int a = 0; a < associations.length; a++) {
			if (taxaContaining.whichTaxonNumber(associations[a].taxonContaining)<0) //containing taxon of association has been deleted
				associations[a] = null; //deleted record of associates for that containing taxon
			else {
				associations[a].deleteDefunctContained(); //delete contained that have been deleted
			}	
		}
		//change size of arrays as needed
		TaxonWithTaxa[] newAssociations= new TaxonWithTaxa[taxaContaining.getNumTaxa()];

		int count = 0;
		for (int i=0; i<associations.length; i++) {
			if (associations[i]!=null){
				TaxonWithTaxa oldT = associations[i];
				newAssociations[count] = new TaxonWithTaxa(oldT.getTaxon(), taxaContained);
				for (int j = 0; j<oldT.length(); j++){
					newAssociations[count].addAssociate(oldT.getAssociate(j));
				}
				count++;
			}
		}
		for (int i=count; i<taxaContaining.getNumTaxa(); i++)
			newAssociations[i] = new TaxonWithTaxa(taxaContaining.getTaxon(i), taxaContained);
		associations = newAssociations;
	}
	/*-----------------------------------------*/
	public String toString(){
		String names = "";
		if (taxaContaining!=null && taxaContained!=null)
			names =  " involving " + taxaContaining.getName() + " and " + taxaContained.getName();
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
		if (taxaContaining==null || taxaContained == null)
			s+= "two blocks";
		else 
			s+=  "the block \"" + taxaContaining.getName() + "\", having " + taxaContaining.getNumTaxa() + " taxa, with taxa of the block \"" + taxaContained.getName() + "\", having " + taxaContained.getNumTaxa() + " taxa";
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
			return taxaContaining;
		else if (index==1)
			return taxaContained;
		else
			MesquiteMessage.warnProgrammer("Error: Taxa association allows only two Taxa objects *");
		return null;
	}
	/*.................................................................................................................*/
	public Taxa getContainingTaxa(){
		return taxaContaining;
	}
	public Taxa getContainedTaxa(){
		return taxaContained;
	}
	/*.................................................................................................................*/
	public Taxa getOtherTaxa(Taxa taxa){
		if (taxa==taxaContained)
			return taxaContaining;
		else if (taxa == taxaContaining)
			return taxaContained;
		else
			MesquiteMessage.warnProgrammer("Error: taxa passed to getOtherTaxa not present *");
		return null;
	}
	/*.................................................................................................................*/

	/*.................................................................................................................*/

	public void setAssociation(Taxon taxonA, Taxon taxonB, boolean associated){
		if (taxaContaining==null || taxaContained == null)
			return;
		int a = taxaContaining.whichTaxonNumber(taxonA);
		int b;
		//enlarge matrices!
		if (a==-1) { //either illegal or second taxon belongs to first Taxa
			a = taxaContained.whichTaxonNumber(taxonA);
			b = taxaContaining.whichTaxonNumber(taxonB);
			if (a!=-1 && b!=-1)
				setAssociated(b,a, associated);
		}
		else  {
			b = taxaContained.whichTaxonNumber(taxonB);
			if (b!=-1)
				setAssociated(a, b, associated);
		}
	}

	/*.................................................................................................................*/
	public boolean getAssociation(Taxon taxonA, Taxon taxonB){
		if (!includedTaxon(taxonA) || !includedTaxon(taxonA))
			return false;
		int a = taxaContaining.whichTaxonNumber(taxonA);
		int b;
		if (a==-1) { //either illegal or second taxon belongs to first Taxa
			a = taxaContained.whichTaxonNumber(taxonA);
			b = taxaContaining.whichTaxonNumber(taxonB);
			if (a!=-1 && b!=-1)
				return areAssociated(b,a);
		}
		else  {
			b = taxaContained.whichTaxonNumber(taxonB);
			if (b!=-1)
				return areAssociated(a, b);
		}
		return false;
	}
	/*.................................................................................................................*/
	public int getNumAssociates(Taxon taxon){
		if (!includedTaxon(taxon))
			return 0;
		int which = taxaContaining.whichTaxonNumber(taxon);
		if (which==-1) { //taxon belongs to B; need to look down matrix
			which = taxaContained.whichTaxonNumber(taxon);
			if (which==-1)
				return 0;
			int num = 0;
			for (int i=0; i<taxaContaining.getNumTaxa(); i++) //counting how many associates
				if (areAssociated(i, which))//bug pre-1.1build62: used associates index instead of taxaA index
					num++;
			return num;
		}
		else   { //taxon belongs to A; need to look across matrix
			int num = 0;
			for (int i=0; i<taxaContained.getNumTaxa(); i++) //counting how many associates
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
		int which = taxaContaining.whichTaxonNumber(taxon);
		if (which==-1) { //taxon belongs to B; need to look down matrix
			which = taxaContained.whichTaxonNumber(taxon);
			if (which==-1)
				return null;
			int num = 0;
			for (int i=0; i<taxaContaining.getNumTaxa(); i++) //counting how many associates //bug pre-1.1build62: used associates index instead of taxaA index
				if (areAssociated(i, which))
					num++;
			if (num==0)
				return null;
			if (result==null || result.length < num)
				result = new Taxon[num];
			for (int i=0; i<result.length; i++)
				result[i] = null;
			int count=0;
			for (int i=0; i<taxaContaining.getNumTaxa(); i++) //accumulating associates //bug pre-1.1build62: used associates index instead of taxaA index
				if (areAssociated(i, which)) {
					if (count<result.length)
						result[count] = taxaContaining.getTaxon(i);
					count++;
				}
			return result;
		}
		else   { //taxon belongs to A; need to look across matrix
			int num = 0;
			for (int i=0; i<taxaContained.getNumTaxa(); i++) //counting how many associates
				if (areAssociated(which,i))//bug pre-1.1build62: used associates index instead of taxaB index
					num++;
			if (num==0)
				return null;
			if (result==null || result.length < num)
				result = new Taxon[num];
			for (int i=0; i<result.length; i++)
				result[i] = null;
			int count=0;
			for (int i=0; i<taxaContained.getNumTaxa(); i++)  //accumulating associates
				if (areAssociated(which, i)) {//bug pre-1.1build62: used associates index instead of taxaB index
					if (count<result.length)
						result[count] = taxaContained.getTaxon(i);
					count++;
				}
			return result;
		}
	}
	/*.................................................................................................................*/
	public Taxon[] getAssociates(Taxon taxon){
		if (!includedTaxon(taxon))
			return null;
		int which = taxaContaining.whichTaxonNumber(taxon);
		if (which==-1) { //taxon belongs to B; need to look down matrix
			which = taxaContained.whichTaxonNumber(taxon);
			if (which==-1)
				return null;
			int num = 0;
			for (int i=0; i<taxaContaining.getNumTaxa(); i++) //counting how many associates
				if (areAssociated(i, which))//bug pre-1.1build62: used associates index instead of taxaA index
					num++;
			if (num==0)
				return null;
			Taxon[] result = new Taxon[num];
			int count=0;
			for (int i=0; i<taxaContaining.getNumTaxa(); i++) //accumulating associates
				if (areAssociated(i, which))//bug pre-1.1build62: used associates index instead of taxaA index
					result[count++] = taxaContaining.getTaxon(i);
			return result;
		}
		else   { //taxon belongs to A; need to look across matrix
			int num = 0;
			for (int i=0; i<taxaContained.getNumTaxa(); i++) //counting how many associates
				if (areAssociated(which,i))//bug pre-1.1build62: used associates index instead of taxaB index
					num++;
			if (num==0)
				return null;
			Taxon[] result = new Taxon[num];
			int count=0;
			for (int i=0; i<taxaContained.getNumTaxa(); i++)  //accumulating associates
				if (areAssociated(which,i))//bug pre-1.1build62: used associates index instead of taxaB index
					result[count++] = taxaContained.getTaxon(i);
			return result;
		}
	}
	/*.................................................................................................................*/
	public Bits getAssociatesAsBits(Taxa taxa, int taxonNumber){
		Taxon taxon = taxa.getTaxon(taxonNumber);
		if (!includedTaxon(taxon))
			return null;
		int which = taxaContaining.whichTaxonNumber(taxon);
		if (which==-1) { //taxon belongs to B; need to look down matrix
			which = taxaContained.whichTaxonNumber(taxon);
			if (which==-1)
				return null;
			int num = 0;
			for (int i=0; i<taxaContaining.getNumTaxa(); i++) //counting how many associates
				if (areAssociated(i, which))//bug pre-1.1build62: used associates index instead of taxaA index
					num++;
			if (num==0)
				return null;
			Bits result = new Bits(taxaContaining.getNumTaxa());
			result.clearAllBits();
			int count=0;
			for (int i=0; i<taxaContaining.getNumTaxa(); i++) //accumulating associates
				if (areAssociated(i, which)){//bug pre-1.1build62: used associates index instead of taxaA index
					result.setBit(i);
				}
			return result;
		}
		else   { //taxon belongs to A; need to look across matrix
			int num = 0;
			for (int i=0; i<taxaContained.getNumTaxa(); i++) //counting how many associates
				if (areAssociated(which,i))//bug pre-1.1build62: used associates index instead of taxaB index
					num++;
			if (num==0)
				return null;
			Bits result = new Bits(taxaContained.getNumTaxa());
			result.clearAllBits();
			int count=0;
			for (int i=0; i<taxaContained.getNumTaxa(); i++)  //accumulating associates
				if (areAssociated(which,i))//bug pre-1.1build62: used associates index instead of taxaB index
					result.setBit(i);
			return result;
		}
	}
	/*.................................................................................................................*/
	public Bits getAssociatesAsBits(Taxa taxa, Bits taxaBits){
		Bits accumulatedAssociates = null;
		for (int i=0; i<taxaBits.getSize(); i++) {
			if (taxaBits.isBitOn(i)) {
				Bits associatedBits = getAssociatesAsBits(taxa, i);
				if (accumulatedAssociates==null)
					accumulatedAssociates = associatedBits;
				else {
					accumulatedAssociates.orBits(associatedBits);
				}
			}
		}

		return accumulatedAssociates;
	}	
	/*.................................................................................................................*/

	public boolean includedTaxon(Taxon taxon){
		if (taxaContaining==null || taxaContained == null)
			return false;
		int which = taxaContaining.whichTaxonNumber(taxon);
		if (which==-1) { //taxon belongs to B; need to look down matrix
			which = taxaContained.whichTaxonNumber(taxon);
			if (which==-1 || which >= taxaContained.getNumTaxa())
				return false;
		}
		else   { //taxon belongs to A; need to look across matrix
			if (which >= taxaContaining.getNumTaxa())
				return false;
		}
		return true;
	}
}

