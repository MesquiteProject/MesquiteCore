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
import java.util.*;

import mesquite.lib.taxa.TaxaPartition;

/*==========================  Mesquite Basic Class Library    ==========================*/
/*===  the basic classes used by the trunk of Mesquite and available to the modules

/* ======================================================================== */
/*.................................................................................................................*/
/** A class that contains serially repeated parts, to each of which may be attached information.
For example, a Tree contains many nodes, and information might be attached to each node.  A Taxa contains taxons,
a CharacterData contains characters, and so on.  The purpose of this class is to coordinate
this attached ("associated") information.  Subclasses include FileElement (and thus CharacterData
and Taxa) and Tree.  */
public abstract class AssociableWithSpecs extends Associable {
	/**Vector of vectors of sets specifying values for elements*/
	private Vector specsVectors = new Vector();
	public static final int SPECSSET_CHANGED = 200;

	public AssociableWithSpecs (int numParts){
		super(numParts);
	}
	public AssociableWithSpecs (){
		super();
	}
	public String toHTMLStringDescription(){
		String s = super.toHTMLStringDescription();
			String sT = "";
		if (specsVectors!=null){ //update size of specification sets
	  		for (int i=0; i<specsVectors.size(); i++) {
	  			SpecsSetVector sv = (SpecsSetVector)specsVectors.elementAt(i);
	  			sT += sv.toHTMLStringDescription();
	  		}
	  		
  		}
 		if (!StringUtil.blank(sT))
			s += "<li>Specification Sets<ul>" +sT + "</ul></li>";
		
		return s;
	}
	public String listAssociates(){
		String s = super.listAssociates();
  		if (specsVectors!=null){ //update size of specification sets
	  		for (int i=0; i<specsVectors.size(); i++) {
	  			SpecsSetVector sv = (SpecsSetVector)specsVectors.elementAt(i);
	  			s += "Specs sets of class " + sv.getClass() + "\n";
	  			for (int j=0; j<sv.size(); j++) {
	  				SpecsSet css = (SpecsSet)sv.elementAt(j);
	  				s+= "   " + css.getName() + "\n";
	  			}
	  		}
  		}
  		return s;
	}
	public void disposing(Object obj){
  		if (specsVectors!=null){ //update size of specification sets
	  		for (int i=0; i<specsVectors.size(); i++) {
	  			SpecsSetVector sv = (SpecsSetVector)specsVectors.elementAt(i);
	  			SpecsSet s = sv.getCurrentSpecsSet();
	  			for (int j=0; j<sv.size(); j++) {
	  				SpecsSet css = (SpecsSet)sv.elementAt(j);
	  				css.disposing(obj);
	  			}
	  			SpecsSet currentSS = sv.getCurrentSpecsSet();
	  			if (currentSS!=null) {
	  				currentSS.disposing(obj);
				}
	  		}
  		}
	}
	public void dispose(){
  		if (specsVectors!=null){ //update size of specification sets
  			try {
	  		for (int i=0; i<specsVectors.size(); i++) {
	  			SpecsSetVector sv = (SpecsSetVector)specsVectors.elementAt(i);
	  			SpecsSet s = sv.getCurrentSpecsSet();
	  			if (s!=null)
	  				s.dispose();
	  			sv.dispose();
	  		}
	  		if (specsVectors!=null)
	  			specsVectors.removeAllElements();
  			}
  			catch (Exception e){
  			}
  		}
  		specsVectors = null;
 		super.dispose();
	}
	public void equalizeParts(AssociableWithSpecs other, int otherPart, int part){
  		if (otherPart>=other.getNumberOfParts() && part >= getNumberOfParts())
  			return;
  		if (other.specsVectors!=null){ 
  		//should do reverse; come from other into one to force creation as needed
	  		for (int i=0; i<other.specsVectors.size(); i++) { 
	  			SpecsSetVector otherSV = (SpecsSetVector)other.specsVectors.elementAt(i);
	  			SpecsSet otherCurrentSS = otherSV.getCurrentSpecsSet();
	  			if (otherCurrentSS!=null) {
	  				Class specsClass = otherSV.getType();
	  				SpecsSet thisSS = getCurrentSpecsSet(specsClass);
	  				if (thisSS == null){
	  					thisSS = otherCurrentSS.makeSpecsSet(this, getNumberOfParts());
				 		if (thisSS != null){
				 			if (this instanceof FileElement){
				 				thisSS.addToFile(((FileElement)this).getFile(), ((FileElement)this).getProject(), ((FileElement)this).getProject().getCoordinatorModule().findElementManager(specsClass)); 
							}
							setCurrentSpecsSet(thisSS, specsClass);
						}
	  				}
	  				if (thisSS != null)
	  					thisSS.equalizeSpecs(otherCurrentSS, otherPart, part);
				}
	  		}
  		}
  		super.equalizeParts(other, otherPart, part);
	}
	public boolean addParts(int starting, int num){
		if (!super.addParts(starting, num))
			return false;
  		if (specsVectors!=null){ //update size of specification sets
	  		for (int i=0; i<specsVectors.size(); i++) {
	  			SpecsSetVector sv = (SpecsSetVector)specsVectors.elementAt(i);
	  			for (int j=0; j<sv.size(); j++) {
	  				SpecsSet css = (SpecsSet)sv.elementAt(j);
	  				css.addParts(starting, num);
  			}
	  			SpecsSet currentSS = sv.getCurrentSpecsSet();
	  			if (currentSS!=null) {
	  				currentSS.addParts(starting,num);
				}
	  		}
  		}
  		return true;
	}
	public boolean deleteParts(int starting, int num){
		if (!super.deleteParts(starting, num))
			return false;
  		if (specsVectors!=null){ //update size of specification sets
	  		for (int i=0; i<specsVectors.size(); i++) {
	  			SpecsSetVector sv = (SpecsSetVector)specsVectors.elementAt(i);
	  			for (int j=0; j<sv.size(); j++) {
	  				SpecsSet css = (SpecsSet)sv.elementAt(j);
	  				css.deleteParts(starting, num);
	  			}
	  			SpecsSet currentSS = sv.getCurrentSpecsSet();
	  			if (currentSS!=null)
	  				currentSS.deleteParts(starting,num);
	  		}
  		}
  		return true;
	}
	
	/** Deletes parts flagged for deletion in Bits*/
	protected boolean deletePartsFlagged(Bits toDelete){ 
		
		if (!super.deletePartsFlagged(toDelete))
			return false;
  		if (specsVectors!=null){ //update size of specification sets
	  		for (int i=0; i<specsVectors.size(); i++) {
	  			SpecsSetVector sv = (SpecsSetVector)specsVectors.elementAt(i);
	  			for (int j=0; j<sv.size(); j++) {
	  				SpecsSet css = (SpecsSet)sv.elementAt(j);
	  				css.deletePartsFlagged(toDelete);
	  			}
	  			SpecsSet currentSS = sv.getCurrentSpecsSet();
	  			if (currentSS!=null)
	  				currentSS.deletePartsFlagged(toDelete);
	  		}
  		}
		return true;
	}	
	
	
	/** Deletes parts by blocks.
	 * blocks[i][0] is start of block; blocks[i][1] is end of block
	 * Assumes that these blocks are in sequence, non-overlapping, etc!!! *
	protected boolean deletePartsBy Blocks(int[][] blocks){ 
		
		if (!super.deletePartsBy Blocks(blocks))
			return false;
  		if (specsVectors!=null){ //update size of specification sets
	  		for (int i=0; i<specsVectors.size(); i++) {
	  			SpecsSetVector sv = (SpecsSetVector)specsVectors.elementAt(i);
	  			for (int j=0; j<sv.size(); j++) {
	  				SpecsSet css = (SpecsSet)sv.elementAt(j);
	  				css.deletePartsBy Blocks(blocks);
	  			}
	  			SpecsSet currentSS = sv.getCurrentSpecsSet();
	  			if (currentSS!=null)
	  				currentSS.deletePartsBy Blocks(blocks);
	  		}
  		}
		return true;
	}
	
	/**/
	public boolean moveParts(int starting, int num, int justAfter){
		if (!super.moveParts(starting, num, justAfter))
			return false;
  		if (specsVectors!=null){ //update size of specification sets
	  		for (int i=0; i<specsVectors.size(); i++) {
	  			SpecsSetVector sv = (SpecsSetVector)specsVectors.elementAt(i);
	  			for (int j=0; j<sv.size(); j++) {
	  				SpecsSet css = (SpecsSet)sv.elementAt(j);
	  				css.moveParts(starting, num, justAfter);
	  			}
	  			SpecsSet currentSS = sv.getCurrentSpecsSet();
	  			if (currentSS!=null)
	  				currentSS.moveParts(starting,num,justAfter);
	  		}
  		}
		return true;
	}
	public boolean swapParts(int first, int second, boolean notify){
		if (!super.swapParts(first, second, notify))
			return false;
  		if (specsVectors!=null){ //update size of specification sets
	  		for (int i=0; i<specsVectors.size(); i++) {
	  			SpecsSetVector sv = (SpecsSetVector)specsVectors.elementAt(i);
	  			for (int j=0; j<sv.size(); j++) {
	  				SpecsSet css = (SpecsSet)sv.elementAt(j);
	  				css.swapParts(first, second, notify);
	  			}
	  			SpecsSet currentSS = sv.getCurrentSpecsSet();
	  			if (currentSS!=null)
	  				currentSS.swapParts(first, second, notify);
	  		}
  		}
		return true;
	}
   	public Vector getSpecSetsVectorVector() {
   		return specsVectors;
   	}
   	public SpecsSetVector getSpecSetsVector(Class type) {
  		if (type==null || specsVectors == null) 
  			return null;

  		for (int i=0; specsVectors != null && i<specsVectors.size(); i++) {
  			SpecsSetVector sv = (SpecsSetVector)specsVectors.elementAt(i);
  			if (sv!=null && rightType(type, sv)) {
  				return sv;
  			}
  		}
		//MesquiteMessage.warnUser("specs set vector not found; type= " + type);
		return null;
   	}
   	private boolean rightType(Class type, SpecsSetVector sv){
	  	return (sv!=null && type == sv.getType());
   	}
 	/*.................................................................................................................*/
 	/** */
  	public void prepareSpecsSetVector(Class type, String typeName){  
  		//go through specsVectors finding the right one, add it to it
  		if (type!=null) {
	  		for (int i=0; i<specsVectors.size(); i++) {
	  			SpecsSetVector sv = (SpecsSetVector)specsVectors.elementAt(i);
	  			if (rightType(type, sv))
	  				return;
	  		}
	  		//not found; need to make new one
  			SpecsSetVector sv = new SpecsSetVector(typeName); 
  			sv.setObjectCharacterized(this);
  			sv.setType(type);
  			specsVectors.addElement(sv);
  		}
  		else MesquiteMessage.warnUser("Attempt to add specs set with no type specified");
	}
	/*.................................................................................................................*/
 	/** stores the given specifications set in the list of specs sets.  If an appropriate specs set vector is not found,
 	a new one is made.*/
  	public void storeSpecsSet(SpecsSet specsSet, Class type){  
 		setDirty(true);
  		//go through specsVectors finding the right one, add it to it
  		if (type!=null && specsSet!=null) {
	  		for (int i=0; i<specsVectors.size(); i++) {
	  			SpecsSetVector sv = (SpecsSetVector)specsVectors.elementAt(i);
	  			if (rightType(type, sv)) {
	  					
	  				sv.addSpecSet(specsSet);  
	  				specsSet.setParent(this);
	  				return;
	  			}
	  		}
	  		//not found; need to make new one
			String typeName = specsSet.getTypeName() + "s";
			SpecsSetVector sv = new SpecsSetVector(typeName); 
  			sv.setObjectCharacterized(this);
  			sv.setType(type);
  			specsVectors.addElement(sv);
  			sv.addSpecSet(specsSet);
	  		specsSet.setParent(this);
  			
  		}
  		else MesquiteMessage.warnUser("Attempt to add specs set with no type specified");
	}
 	/*.................................................................................................................*/
 	/** removes the given specifications set from the list of specs sets*/
  	public void removeSpecsSet(SpecsSet specsSet, Class type){  
 		setDirty(true);
  		//go through specsVectors finding the right one, add it to it
  		if (type!=null && specsSet!=null && specsVectors!=null) {
	  		for (int i=0; i<specsVectors.size(); i++) {
	  			SpecsSetVector sv = (SpecsSetVector)specsVectors.elementAt(i);
	  			if (rightType(type, sv)) {
	  				sv.removeSpecSet(specsSet);  
	  				specsSet.setParent(null);
	  				
	  				return;
	  			}
	  		}
  		}
	}
 	/*.................................................................................................................*/
 	/** returns the number of specs sets*/
  	public int getNumSpecsSets(Class type){  
  		if (type==null || specsVectors==null )
  			return 0;
  		for (int i=0; i<specsVectors.size(); i++) {
  			SpecsSetVector sv = (SpecsSetVector)specsVectors.elementAt(i);
  			if (rightType(type, sv)) {
  				return sv.getNumSpecsSets();  
  			}
  		}
  		return 0;
  	}

	/*.................................................................................................................*/
 	/** returns the given specs set from the list of specs sets*/
  	public SpecsSet getCurrentSpecsSet(Class type){  
  		if (type!=null && specsVectors!=null) {
	  		for (int i=0; i<specsVectors.size(); i++) {
	  			SpecsSetVector sv = (SpecsSetVector)specsVectors.elementAt(i);
	  			if (sv!=null && rightType(type, sv)) {
	  				return sv.getCurrentSpecsSet();
	  			}
	  		}
  		}
  		return null;
	}
	public void setCurrentSpecsSet(SpecsSet specsSet, Class type){
  		if (type!=null && specsVectors!=null &&specsSet!=null ) {
	 		setDirty(true);
	  		for (int i=0; i<specsVectors.size(); i++) {
	  			SpecsSetVector sv = (SpecsSetVector)specsVectors.elementAt(i);
	  			if (sv!=null && rightType(type, sv)) {
	  				sv.setCurrentSpecsSet(specsSet);
			  		specsSet.setParent(this);
	  				return;
	  			}
	  		}
	  		//not found; need to make new one
			String typeName = specsSet.getTypeName() + "s";
 			SpecsSetVector sv = new SpecsSetVector(typeName); 
  			sv.setObjectCharacterized(this);
  			sv.setType(type);
  			specsVectors.addElement(sv);
	  		sv.setCurrentSpecsSet(specsSet);
	  		specsSet.setParent(this);
			notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_CHANGED));
 		}
	}
 	/*.................................................................................................................*/
 	/** returns the given specs set from the list of specs sets*/
  	public SpecsSet getSpecsSet(String name, Class type){  
  		if (type!=null && specsVectors!=null && name !=null) {
	  		for (int i=0; i<specsVectors.size(); i++) {
	  			SpecsSetVector sv = (SpecsSetVector)specsVectors.elementAt(i);
	  			if (rightType(type, sv)) {
			  		for (int j=0; j<sv.getNumSpecsSets(); j++) {
			  			SpecsSet temp = (SpecsSet)sv.elementAt(j);
			  			if (name.equalsIgnoreCase(temp.getName()))
			  				return temp;
			  		}
	  			}
	  		}
  		}
  		return null;
	}
 	/*.................................................................................................................*/
 	/** returns the given specs set from the list of specs sets*/
  	public SpecsSet getSpecsSet(int index, Class type){  
 		if (type!=null && specsVectors!=null ) {
	  		for (int i=0; i<specsVectors.size(); i++) {
	  			SpecsSetVector sv = (SpecsSetVector)specsVectors.elementAt(i);
	  			if (index < sv.size() && rightType(type, sv)) {
			  		return (SpecsSet)sv.elementAt(index);
	  			}
	  		}
  		}
  		return null;
	}

}

