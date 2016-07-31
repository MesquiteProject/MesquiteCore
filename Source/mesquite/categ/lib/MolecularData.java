/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.categ.lib;


import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.CharacterModel;
import mesquite.lib.characters.CharacterState;
import mesquite.lib.characters.CharacterStates;
import mesquite.lib.characters.DefaultReference;
import mesquite.lib.characters.ModelSet;
import mesquite.lib.duties.*;
import mesquite.molec.lib.*;

import java.util.*;

/* ======================================================================== */
/** A subclass of CharacterData for molecular data */
public class MolecularData extends CategoricalData {
	public static String DATATYPENAME="Molecular Data";
	GenCodeModelSet genCodeModelSet =null;
	public static final String genBankNumberName="genBankNumber";
	public static final NameReference reversedRef = NameReference.getNameReference("reversed"); //long: tInfo, data(ch); MesquiteInteger: data(cells)
	public static final NameReference genBankNumberRef = NameReference.getNameReference(genBankNumberName);//String: tInfo

	/*vectors, one for each taxon, of 3D Points indicating inversions in sequence
	x = site marking left boundary of inverted region
	y = site marking right boundary of inverted region
	z = not yet used
	 */
	protected Vector[] inversions;  
	boolean trackInversions = false;

	public MolecularData(CharMatrixManager manager, int numTaxa, int numChars, Taxa taxa){
		super(manager, numTaxa, numChars, taxa);
		saveChangeHistory = false;//turned off for now
		inventUniqueIDs = false; //turned off for now
		rememberDefaultOrder = false;
		//	nullifyBooleanArrays();
	}
	public String getDefaultIconFileName(){ //for small 16 pixel icon at left of main bar
		return "matrixMolecSmall.gif";
	}
	/*.................................................................................................................*/
	public String searchData(String s, MesquiteString commandResult) {
		if (commandResult != null)
			commandResult.setValue((String)null);
		if (StringUtil.blank(s))
			return null;
		String list = "";
		String fc =""; //to receive the direct command
		int numFound = 0;
		for (int ic=0; ic< getNumChars(); ic++){
			String name = getCharacterName(ic);
			if (name != null && StringUtil.foundIgnoreCase(name, s)){
				list += "<li>Site " + (ic+1) + ": <strong>" + StringUtil.protectForXML(name) + "</strong>. <a href=\"selectCharacter:" + ic + " " + getID()  + "\">Touch character</a></li>";
				numFound++;
				fc = "selectCharacter:" + ic+ " " + getID();
			}
		}

		if (commandResult != null && numFound == 1)
			commandResult.setValue(fc);
		if (StringUtil.blank(list))
			return list;
		return "<h2>Characters of matrix <strong>" + StringUtil.protectForXML(getName()) + "</strong></h2><ul>" + list + "</ul>";
	}
	public void setInventUniqueIDs(boolean invent){
		//inventUniqueIDs = invent; to prevent this from turning on for molecular
	}
	public boolean getInventUniqueIDs(){
		return inventUniqueIDs;
	}
	public String getUniqueID(int ic){
		return null;
	}
	public void setTrackInversions(boolean dt){
		trackInversions = dt;
	}
	public boolean getTrackInversions(){
		return trackInversions;
	}
	/** returns default column width in editors.  Default is 16.*/
	public int getNarrowDefaultColumnWidth() {
		return 12;
	}

	/*..........................................    ..................................................*/
	public CharacterState makeCharacterState(){
		return new MolecularState();
	}
	public Vector getInversions(int it){
		if (inversions == null || it >= inversions.length)
			return null;
		return inversions[it];
	}
	//true if inverted
	public boolean isReverseDirectionAtSite(int ic, int it){
		if (inversions == null || it >= inversions.length)
			return false;
		boolean inverted = false;
		//look for inversions containing this site, starting at last one
		int siteEquiv = ic;
		for (int ip = inversions[it].size()-1; ip>=0; ip--){
			Mesquite3DIntPoint p = (Mesquite3DIntPoint)inversions[it].elementAt(ip);
			if (siteEquiv >= p.x && siteEquiv <= p.y){ //contains this site
				inverted = !inverted;
				siteEquiv = p.y - (siteEquiv-p.x);
			}
		}
		return inverted;
	}
	//returns original (pre-inversion) position of current site ic, which may be within an inversion
	public int findOriginalPositionOfInvertedSite(int ic, int it){
		if (inversions == null || it >= inversions.length)
			return ic;
		//look for inversions containing this site, starting at last one
		int siteEquiv = ic;
		for (int ip = inversions[it].size()-1; ip>=0; ip--){
			Mesquite3DIntPoint p = (Mesquite3DIntPoint)inversions[it].elementAt(ip);
			if (siteEquiv >= p.x && siteEquiv <= p.y){ //contains this site
				siteEquiv = p.y - (siteEquiv-p.x);
			}
		}
		return siteEquiv;
	}
	//returns current (post-inversion) position of an original site ic
	public int findInvertedPositionOfOriginalSite(int ic, int it){
		if (inversions == null || it >= inversions.length)
			return ic;
		//look for inversions containing this site, starting at last one
		MesquiteMessage.warnProgrammer("findInvertedPositionOfOriginalSite THIS SHOULD NOT APPEAR");
		int siteEquiv = ic;
		for (int ip = 0; ip< inversions[it].size(); ip++){
			Mesquite3DIntPoint p = (Mesquite3DIntPoint)inversions[it].elementAt(ip);
			if (siteEquiv >= p.x && siteEquiv <= p.y){ //contains this site
				siteEquiv = p.y - (siteEquiv-p.x);
			}
		}
		return siteEquiv;
	}
	/*-----------------------------------------------------------*/
	/**Adds num characters after position "starting".  If "starting" = -1, then inserts at start.  If "starting" >
	number of characters, adds to end.  Any linked CharacterDatas are
	to be adjusted separately.  Returns true iff successful.*/
	public boolean addParts(int starting, int num){
		//adding characters; deal with inversions
		if (inversions != null){
			for (int it = 0; it<inversions.length && it< getNumTaxa(); it++){
				for (int ip = 0; ip< inversions[it].size(); ip++){
					Mesquite3DIntPoint p = (Mesquite3DIntPoint)inversions[it].elementAt(ip);
					if (starting < p.x) //added before x; need to push to right
						p.x += num;
					if (starting < p.y)//added before y; need to push to right
						p.y += num;
				}
			}
		}
		return super.addParts(starting, num);
	}
	/*-----------------------------------------------------------*/
	/** deletes num characters from (and including) position "starting"; returns true iff successful.  Should be overridden by particular subclasses, but this called via super so it can clean up.*/
	public boolean deleteParts(int starting, int num){
		//deleting characters; deal with inversions

		if (inversions != null){
			for (int it = 0; it<inversions.length && it< getNumTaxa(); it++){
				for (int ip = 0; ip< inversions[it].size(); ip++){
					Mesquite3DIntPoint p = (Mesquite3DIntPoint)inversions[it].elementAt(ip);
					if (starting <= p.x){//deletion will affect point
						if (p.x < starting+num) //point is being deleted also
							p.x = starting;  //snap left edge of flip to right margin of deleted area
						else
							p.x -= num; //push to left
					}
					if (starting <= p.y){//deletion will affect point
						if (p.y < starting+num) //point is being deleted also
							p.y = starting-1;   //snap right edge of flip to left margin of deleted area-1
						else
							p.y -= num; //push to left
					}
				}
			}
		}

		return super.deleteParts(starting, num);
	}
	private int newPosAfterMove(int orig, int starting, int num, int justAfter){
		if (starting < justAfter){ //moving forward
			if (orig > starting){
				if (orig <= starting + num) //will be moved forward
					orig = orig + (justAfter-starting) - num;
				else if (orig < justAfter) //in chunk shifted left because of move
					orig -= num;
			}
		}
		else if (orig > justAfter){
			if (orig < starting + num) //will be moved backward
				orig = orig + (starting-justAfter);
			else if (orig < starting)
				orig+= num;
		}
		return orig;
	}
	/*-----------------------------------------------------------*/
	/**Moves num characters from position "first" to just after position "justAfter"; returns true iff successful.*/
	public boolean moveParts(int starting, int num, int justAfter){
		//moving characters; deal with inversions
		if (inversions != null){
			for (int it = 0; it<inversions.length && it< getNumTaxa(); it++){
				for (int ip = 0; ip< inversions[it].size(); ip++){
					Mesquite3DIntPoint p = (Mesquite3DIntPoint)inversions[it].elementAt(ip);
					p.x = newPosAfterMove(p.x, starting, num, justAfter);
					p.y = newPosAfterMove(p.y, starting, num, justAfter);
				}
			}
		}
		return super.moveParts(starting, num, justAfter);
	}
	/*-----------------------------------------------------------*/
	/**Swaps characters first and second.*/
	public boolean swapParts(int first, int second){
		//swapping characters; deal with inversions
		if (inversions != null){
			MesquiteMessage.warnProgrammer("Swapping characters and strand inversions not yet fully supported");
		}
		return super.swapParts(first, second);
	}
	/*.................................................................................................................*/
	protected CharacterState moveOne(int i, int distance, int it, CharacterState cs,  MesquiteBoolean dataChanged){
		//moving contents of cells; deal with inversions
		if (inversions != null && it < inversions.length){
			for (int ip = 0; ip< inversions[it].size(); ip++){
				Mesquite3DIntPoint p = (Mesquite3DIntPoint)inversions[it].elementAt(ip);
				if (p.x == i) //this point being moved
					p.x += distance;
				if (p.y == i) //sister point being moved
					p.y += distance;
			}
		}
		return super.moveOne(i, distance, it, cs, dataChanged);
	}
	/*-----------------------------------------------------------*/
	/**Adds num taxa after position "starting"; returns true iff successful.  Assumes details already handled in subclasses, and numTaxa reset there.*/
	public boolean addTaxa(int starting, int num){
		//adding taxa; deal with inversions
		if (inversions != null && num >0){
			if (starting<0) 
				starting = -1;
			if (starting>inversions.length) 
				starting = inversions.length-1;
			int newNumParts = inversions.length+num;
			Vector[] newValues = new Vector[newNumParts];
			for (int i=0; i<= starting; i++) 
				newValues[i] = inversions[i];
			for (int i=0; i<num ; i++) 
				newValues[starting + i + 1] = null;
			for (int i=0; i<inversions.length-starting-1; i++)
				newValues[i +starting+num+1] = inversions[starting + i+1];
			inversions = newValues;
		}
		return super.addTaxa(starting, num);
	}
	/*-----------------------------------------------------------*/
	/**Deletes num taxa from position "starting"; returns true iff successful.  Assumes details already handled in subclasses, and numTaxa reset there.*/
	public boolean deleteTaxa(int starting, int num){
		//deleting taxa; deal with inversions
		if (inversions != null && num> 0){
			if (num+starting>inversions.length)
				num = inversions.length-starting;
			int newNumParts = inversions.length-num;
			Vector[] newValues = new Vector[newNumParts];
			for (int i=0; i<starting; i++)
				newValues[i] = inversions[i];
			for (int i=starting+num; i<inversions.length; i++)
				newValues[i-num ] = inversions[i];
			inversions = newValues;
		}
		return super.deleteTaxa(starting, num);
	}
	/**moves num taxa from position "starting" to just after position "justAfter"; returns true iff successful.*/
	public boolean moveTaxa(int starting, int num, int justAfter){
		//moving taxa; deal with inversions
		if (inversions != null && num>0 && starting >=0 && starting <= inversions.length){
			Vector[] newValues = new Vector [inversions.length];

			if (starting>justAfter){
				int count =0;
				for (int i=0; i<=justAfter; i++)
					newValues[count++]=inversions[i];

				for (int i=starting; i<=starting+num-1; i++)
					newValues[count++]=inversions[i];
				for (int i=justAfter+1; i<=starting-1; i++)
					newValues[count++]=inversions[i];
				for (int i=starting+num; i<inversions.length; i++)
					newValues[count++]=inversions[i];
			}
			else {
				int count =0;
				for (int i=0; i<=starting-1; i++)
					newValues[count++]=inversions[i];

				for (int i=starting+num; i<=justAfter; i++)
					newValues[count++]=inversions[i];
				for (int i=starting; i<=starting+num-1; i++)
					newValues[count++]=inversions[i];
				for (int i=justAfter+1; i<inversions.length; i++)
					newValues[count++]=inversions[i];
			}
			for (int i=0; i<inversions.length; i++)
				inversions[i]=newValues[i];
			inversions = newValues;
		}
		return super.moveTaxa(starting, num, justAfter);
	}
	/** trades the states of character ic and ic2 in taxon it.  Used for reversing sequences (for example).*/
	protected void tradeStatesBetweenCharactersInternal(int ic, int ic2, int it, boolean adjustCellLinked, boolean adjustDirections){
		//trading states; deal with inversions 
		super.tradeStatesBetweenCharactersInternal(ic, ic2, it, adjustCellLinked, adjustDirections);
		if (adjustDirections && trackInversions)
			MesquiteMessage.warnProgrammer("tradeStatesBetweenCharactersInternal does not yet handle direction tracking!"); //deal with inversions
	}

	/* .......................................... DNAData .................................................. */
	public boolean isReversed( int it) {
		Associable tInfo = getTaxaInfo(false);
		if (tInfo!=null) {
			return tInfo.getAssociatedBit(reversedRef,it);
		}
		return false;
	}
	/* .......................................... MolecularData .................................................. */
	public void setReversed( int it, boolean value) {
		Associable tInfo = getTaxaInfo(false);
		if (tInfo!=null) {
			tInfo.setAssociatedBit(reversedRef, it, value);
		}
	}

	/*..........................................  MolecularData  ..................................................*/
	/**Reverses the associated bits for all characters. */
	public void reverseAssociated(int icStart, int icEnd){
		for (int ic=0; ic <= (icEnd-icStart)/2 && icStart+ic< icEnd-ic; ic++)
			swapAssociated(icStart+ic, icEnd-ic);
		//	int numChars = getNumChars();
		//	for (int ic = 0; ic<numChars/2; ic++)
		//		swapAssociated(ic, numChars-ic-1);
	}
	/*..........................................  MolecularData  ..................................................*/
	/**Reverses the data from character icStart to icEnd in taxon it. */  
	public void reverse(int icStart, int icEnd, int it, boolean reverseTerminalGaps, boolean adjustCellLinked){
		//reversing; deal with inversions
		if (trackInversions){
			if (inversions == null){
				inversions = new Vector[numTaxa];
				for (int itt = 0; itt<numTaxa; itt++)
					inversions[itt] = new Vector();
			}
			//Wayne: should see if there is a matching element and simply flip it!
			inversions[it].addElement(new Mesquite3DIntPoint(icStart, icEnd, 0));
		}
		if ((icStart==0 && icEnd==getNumChars()-1) || (!anyApplicableBefore(icStart, it)&& !anyApplicableAfter(icEnd,it))) {
			Associable tInfo = getTaxaInfo(true);
			if (tInfo!=null) {
				boolean prevValue = tInfo.getAssociatedBit(reversedRef,it);
				tInfo.setAssociatedBit(reversedRef, it, !prevValue);
			}
		}
		super.reverse(icStart, icEnd, it, reverseTerminalGaps, adjustCellLinked);
	}
	/*..........................................  MolecularData  ..................................................*/
	/**Reverses the data from character icStart to icEnd in taxon it. NOTE: this version reverses character metadata (codon positions, etc.)*/  
	public void reverse(int icStart, int icEnd, boolean adjustCellLinked){
		for (int it= 0; it< getNumTaxa(); it++)
			reverse(icStart, icEnd, it, true, adjustCellLinked);
		int numChars = icEnd-icStart+1;
		int halfWay = numChars/2;
		for (int ic= 0; ic<halfWay; ic++)
			swapCharacterMetadata(icStart+ic,icEnd-ic);
		if (adjustCellLinked && linkedDatas.size()>0){
			for (int i=0; i<linkedDatas.size(); i++){
				CharacterData d = (CharacterData)linkedDatas.elementAt(i);
				for (int ic= 0; ic<halfWay; ic++)
					d.swapCharacterMetadata(icStart+ic,icEnd-ic);
			}
		}

	}
	/* ..........................................MolecularData................ */
	/** returns the appropriate genetic code models to be assigned as default ones to characters
	 *  that might be added immediately after character ic */
	public GenCodeModel getDefaultGenCodeModel(int ic) {
		GenCodeModel endGenCodeModel = null;
		if (ic< getNumChars())  
			endGenCodeModel = getGenCodeModel(ic+1);
		GenCodeModel startGenCodeModel = null;
		if (ic>=0)  
			startGenCodeModel = getGenCodeModel(ic);
		if (endGenCodeModel !=null && startGenCodeModel ==null)  //only the end one is non-null
			return endGenCodeModel;
		if (endGenCodeModel ==null && startGenCodeModel !=null)  //only the start one is non-null
			return startGenCodeModel;
		if (endGenCodeModel ==null && startGenCodeModel ==null)  //
			return null;
		if (startGenCodeModel.getGeneticCode().equals(startGenCodeModel.getGeneticCode()))  
			return startGenCodeModel;  //same, at least in terms of code, assign first one
		return startGenCodeModel;  // here they both exist but differ; should give warning?
	}
	/*..........................................    ..................................................*/
	/** returns the genetic code model of character ic in taxon it*/
	public GenCodeModel getGenCodeModel(int ic){
		if (genCodeModelSet==null)
			genCodeModelSet = (GenCodeModelSet)getCurrentSpecsSet(GenCodeModelSet.class);
		if (genCodeModelSet != null && ic<genCodeModelSet.getNumberOfParts() && ic>=0)
			return (GenCodeModel)genCodeModelSet.getModel(ic);
		return null;
	}
	/*..........................................    ..................................................*/
	/** returns the genetic code of character ic in taxon it*/
	public GeneticCode getGeneticCode(int ic){
		if (genCodeModelSet==null)
			genCodeModelSet = (GenCodeModelSet)getCurrentSpecsSet(GenCodeModelSet.class);
		if (genCodeModelSet != null && ic<genCodeModelSet.getNumberOfParts() && ic>=0) {
			GenCodeModel genCodeModel=  (GenCodeModel)genCodeModelSet.getModel(ic);
			if (genCodeModel != null)
				return genCodeModel.getGeneticCode();
		}
		return null;
	}
	/* ..........................................  .................................................. *
	public void assignGeneticCodeToTerminalChars(int whichTermChars){
		if (whichTermChars<0)
			assignGeneticCodeToCharsBasedOnAdjacent(0,-whichTermChars-1);
		else
			assignGeneticCodeToCharsBasedOnAdjacent(getNumChars()-whichTermChars-1, getNumChars()-1);
	}
	/* ..........................................  .................................................. *
	public void assignGeneticCodeToCharsBasedOnAdjacent(int startChar, int endChar){
		ModelSet modelSet = (ModelSet) getCurrentSpecsSet(GenCodeModelSet.class);
		if (modelSet == null) 
			return;
		int existingChar = 0;
		if (startChar<=0) {  //added to front
			existingChar = endChar+1;
			if (existingChar>=getNumChars())
				return;
		} else existingChar = startChar-1;

		GenCodeModel genCodeModel = getGenCodeModel(existingChar);
		if (genCodeModel!=null) { // the immediately adjacent character (the previous first character) has an assigned codon position
				for (int ic=startChar; ic<getNumChars(); ic++) {
					modelSet.setModel(genCodeModel, ic);
				}
			}

		}
		
/*...............................................................................................................*/
	/** Sets the GenBank number of a particular taxon in this data object. */
	public void setGenBankNumber(int it, String s){
		Taxon taxon = getTaxa().getTaxon(it);
		Associable tInfo = getTaxaInfo(true);
		if (tInfo != null && taxon != null) {
			tInfo.setAssociatedObject(MolecularData.genBankNumberRef, it, s);
		}
	}

	/* ..........................................  .................................................. */

	public boolean stripRightTerminalGaps(boolean notify){
		int ic = getNumChars();
		for (ic = getNumChars()-1; ic>=0; ic--){
			if (!entirelyInapplicable(ic))
				break;
		}
		if (ic< getNumChars()) {
			int ncic = getNumChars()-ic;
			deleteCharacters(ic+1, ncic, notify);
			deleteInLinked(ic+1, ncic, notify);
			return true;
		}
		return false;
	}
	public boolean stripLeftTerminalGaps(boolean notify){
		int ic = -1;
		for (ic = 0; ic<getNumChars(); ic++){
			if (!entirelyInapplicable(ic))
				break;
		}
		if (ic>=0) {
			deleteCharacters(0, ic, notify);
			deleteInLinked(0, ic, notify);
			return true;
		}
		return false;
	}



	public void collapseGapsInCellBlock(int it, int icStart, int icEnd, boolean notify) {
		int lastFilled = icStart-1;
		for (int ic = icStart; ic < getNumChars() && ic<=icEnd; ic++) {
			if (!isInapplicable(ic, it)){
				lastFilled++;
				if (ic!=lastFilled)
					tradeStatesBetweenCharacters(ic, lastFilled, it,true);
			}
		}
	}
	public void collapseGapsInCellBlockRight(int it, int icStart, int icEnd, boolean notify) {
		int lastFilled = icEnd+1;
		for (int ic = icEnd; ic >=0 && ic>= icStart; ic--) {
			if (!isInapplicable(ic, it)){
				lastFilled--;
				if (ic!=lastFilled)
					tradeStatesBetweenCharacters(ic, lastFilled, it,true);
			}
		}
	}

	public  StringBuffer getSequenceAsFasta(boolean includeGaps,boolean convertMultStateToMissing, int it) {
		return getSequenceAsFasta(includeGaps, convertMultStateToMissing, it, getTaxa().getTaxonName(it));
	}
	
	public  StringBuffer getSequenceAsFasta(boolean includeGaps,boolean convertMultStateToMissing, int it, String sequenceName) {
		Taxa taxa = getTaxa();

		int numTaxa = taxa.getNumTaxa();
		int numChars = getNumChars();
		StringBuffer outputBuffer = new StringBuffer(numTaxa*(20 + numChars));
		boolean isProtein = this instanceof ProteinData;

		int counter = 1;
		if (hasDataForTaxon(it)){
			counter = 1;
			outputBuffer.append(">");
			outputBuffer.append(sequenceName);
			outputBuffer.append(StringUtil.lineEnding());
			for (int ic = 0; ic<numChars; ic++) {
				int currentSize = outputBuffer.length();
				boolean wroteMoreThanOneSymbol = false;
				if (isUnassigned(ic, it) || (convertMultStateToMissing && isProtein && isMultistateOrUncertainty(ic, it)))
					outputBuffer.append(getUnassignedSymbol());
				else if (includeGaps || (!isInapplicable(ic,it))) {
					statesIntoStringBuffer(ic, it, outputBuffer, false);
					wroteMoreThanOneSymbol = outputBuffer.length()-currentSize>1;
					counter ++;
					if ((counter % 50 == 1) && (counter > 1)) {    // modulo
						outputBuffer.append(StringUtil.lineEnding());
					}
				}
				if (wroteMoreThanOneSymbol) {
					alert("Sorry, this data matrix can't be exported to this format (some character states aren't represented by a single symbol [char. " + CharacterStates.toExternal(ic) + ", taxon " + Taxon.toExternal(it) + "])");
					return null;
				}

			}
			outputBuffer.append(StringUtil.lineEnding());
		}
		return outputBuffer;
	}
}


