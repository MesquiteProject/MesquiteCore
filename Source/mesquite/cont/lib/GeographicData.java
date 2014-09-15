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

import java.awt.*;
import java.util.*;
import java.util.zip.*;
import mesquite.lib.duties.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
/* ======================================================================== */
/** A subclass of CharacterData for Geographic data.*/
public class GeographicData extends ContinuousData {
	static final int DECIMAL_DEGREES = 0;
	int valueFormat = DECIMAL_DEGREES;
	static final int LATITUDE = 0;
	static final int LONGITUDE=1;
	
	public GeographicData(CharMatrixManager manager, int numTaxa, int numChars, Taxa taxa){
		super(manager, numTaxa, 2,taxa);
		setCharacterName(LATITUDE, "Latitude");
		setCharacterName(LONGITUDE, "Longitude");
	}
	/*..........................................GeographicData................*/
	/** Indicates which character is latitude */
	public static int getLatitudeCharacter(){
		return LATITUDE;
	}
	/*..........................................GeographicData................*/
	/** Indicates which character is longitude */
	public static int getLongitudeCharacter(){
		return LONGITUDE;
	}
	/*..........................................GeographicData................*/
	/** Indicates the type of character stored */
	public Class getStateClass(){
		return GeographicState.class;
	}
	public int getMaxNumChars(){
		return 2;
	}
	public boolean canMoveChars(){
		return false;
	}
	/** returns the name of the type of data stored */
	public String getDataTypeName(){
		return "Geographic Data";
	}
	
	public boolean canAddCharacters() {
		return false;
	}

	/*..........................................GeographicData................*/
	public CharacterData makeCharacterData() {
		GeographicData data = new GeographicData(getMatrixManager(), getNumTaxa(), getNumChars(), getTaxa());
		data.setItemsAs(this);
		return data;
	}
	/*..........................................GeographicData................*/
	public CharacterData makeCharacterData(int ntaxa, int nchars) {
		GeographicData data = new GeographicData(getMatrixManager(), ntaxa, nchars,  getTaxa());
		data.setItemsAs(this);
		return data;
	}
	public CharacterData makeCharacterData(CharMatrixManager manager, Taxa taxa){ 
		GeographicData data =  new GeographicData(getMatrixManager(), taxa.getNumTaxa(), 0, taxa);
		data.setItemsAs(this);
		return data;
	}
	/*..........................................  GeographicData  ..................................................*/
	/**clone a portion of CharacterData and return new copy.  Does not clone the associated specs sets etc.*/ //TODO: here should use super.setToClone(data) to handle specssets etc.???
	public CharacterData cloneDataBlock(int icStart, int icEnd, int itStart, int itEnd){
		int blockChars = icEnd-icStart+1;
		int blockTaxa = itEnd-itStart+1;
		boolean[] taxaToClone = new boolean[getNumTaxa()];
		for (int it=0; it<getNumTaxa(); it++) {
			taxaToClone[it] = it>=itStart && it<=itEnd;
		}
		Taxa taxa = getTaxa().cloneTaxa(taxaToClone);

		ContinuousData cD = new GeographicData(getMatrixManager(), blockTaxa, blockChars, taxa);
		int nItems = getNumItems();
		cD.setItemsAs(this);
		for (int i=0; i<nItems; i++){
			Double2DArray imatrix = null;
				imatrix = cD.getItem(i);
			Double2DArray oldMatrix= getItem(i);
			for (int ic=icStart; ic<=icEnd; ic++)
				for (int it = itStart; it<=itEnd; it++)
					imatrix.setValue(ic-icStart,it-itStart,oldMatrix.getValue(ic,it));
		}
		for (int ic = 0; ic< numChars; ic++)
			if (getSelected(ic))
				cD.setSelected(ic-icStart, true);

		return cD;
	}
/*..........................................................*/
   	public CharacterDistribution getCharacterDistribution(int ic){
   		ContinuousEmbedded states =new ContinuousEmbedded(this, ic);
		return states;
   	}
	/*..........................................    ..................................................*/
	/**get copy of matrix and return as MCharactersDistribution */
   	public MCharactersDistribution getMCharactersDistribution(){
   		MContinuousEmbedded states = new MContinuousEmbedded(this);
		return states;
   	}
	/*..........................................    ..................................................*/
   	public boolean legalValue(int ic, double states){
   		return !MesquiteDouble.isCombinable(states) || ((ic==LATITUDE && states>=-90.0 && states<=90.0) || (ic==LONGITUDE && states>=-180.0 && states<=180.0));
   	}
	/*..........................................    ..................................................*/
   	public void setState(int ic, int it, int item, double states){
   		if (!legalValue(ic,states)) {
   			return;
   		}
   		super.setState(ic,it,item,states);
   	}
	/*..........................................................*/
   	/** returns the state of character ic in taxon it*/
   	public  CharacterState getCharacterState(CharacterState cs, int ic, int it){
   		if (cs==null || cs.getClass()!=GeographicState.class) {
   			cs =new GeographicState();
		}
   		((GeographicState)cs).setItemsAs(this);
   		for (int i=0; i< getNumItems(); i++) {
   			((GeographicState)cs).setValue(i, getState(ic, it, i));
   		}
   		return cs; 
   	}
	/*..........................................GeographicData................*/
   	public String toString(){
   		return "Geographic data matrix id: " + getID() + "; chars: " + numChars + "; taxa: " + numTaxa + "; items " + getNumItems() + "; name: " + getName();
   	}




	/*.................................................................................................................*/
	public static double getPolarLatitude(double latitude){
			return (latitude/90)*Math.PI/2;
	}
	/*.................................................................................................................*/
	public static double getPolarColatitude(double latitude){   
			return Math.PI/2 - getPolarLatitude(latitude);
	}
	/*.................................................................................................................*/
	public static double getRegularLatitude(double polarLatitude){
		double latitude = (polarLatitude/Math.PI)*2*90;
		if (latitude<-90.0)
			latitude = 90+latitude;
		else if (latitude>90.0)
			latitude = latitude-180;
		return latitude;
	}
	/*.................................................................................................................*/
	public static double getPolarLongitude(double longitude){
		if (longitude>0)
			return (longitude/180)*Math.PI;
		else
			return ((360+longitude)/180)*Math.PI;
	}
	/*.................................................................................................................*/
	public static double getRegularLongitude(double polarLongitude){
		double longitude;
		if (polarLongitude< Math.PI)  // eastern hemisphere
			longitude= (polarLongitude/Math.PI)*180;
		else   // western hemisphere
			longitude= (polarLongitude/Math.PI)*180-360;
		if (longitude<-180.0)
			longitude = 360+longitude;
		else if (longitude>180.0)
			longitude = longitude-360;
		return longitude;
	}
	

}


