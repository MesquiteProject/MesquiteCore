/* Mesquite source code.  Copyright 1997-2005 W. Maddison and D. Maddison. 
Version 1.06, August 2005.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.cartographer.GreatCircleDistance;
/*~~  */


import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.distance.lib.*;
import mesquite.cont.lib.*;

/* ======================================================================== */
public class GreatCircleDistance extends GeoTaxaDistFromMatrix {
	MesquiteBoolean calcMiles, calcKilometers;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		calcKilometers= new MesquiteBoolean(true);
		calcMiles= new MesquiteBoolean(false);
		MesquiteSubmenuSpec mss = addSubmenu(null, "Distance Units");
		addCheckMenuItemToSubmenu( null, mss,"Kilometers", makeCommand("setCalcKilometers",  this), calcKilometers);
		addCheckMenuItemToSubmenu( null, mss,"Miles", makeCommand("setCalcMiles",  this), calcMiles);
		return true;
  	 }	 
	public TaxaDistance getTaxaDistance(Taxa taxa, MCharactersDistribution observedStates, CommandRecord commandRec){
		if (observedStates==null) {
			MesquiteMessage.warnProgrammer("Observed states null in "+ getName());
			return null;
		}
		GreatCircleTaxDist TD = new GreatCircleTaxDist( this,taxa, observedStates);
		return TD;
	}
	/*.................................................................................................................*/
	 public boolean useKilometers() {
		 return calcKilometers.getValue();
	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Great Circle Distance";  
   	 }
   	 
    		/*.................................................................................................................*/
      	 public Snapshot getSnapshot(MesquiteFile file) {
       	 	Snapshot temp = new Snapshot();
      	 	temp.addLine("setCalcKilometers " + calcKilometers.toOffOnString());
     	 	temp.addLine("setCalcMiles " + calcMiles.toOffOnString());
     	 	return temp;
      	 }
      	 MesquiteInteger pos = new MesquiteInteger();
      	 /*.................................................................................................................*/
      	 public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {
      		 if (checker.compare(this.getClass(), "Sets whether or not kilometers should be used for Great Circle Distance calculations.", "[on or off]", commandName, "setCalcKilometers")) {
      			 boolean current = calcKilometers.getValue();
      			 calcKilometers.toggleValue(parser.getFirstToken(arguments));
      			 if (current!=calcKilometers.getValue()) {
      				 if (calcKilometers.getValue())
      					 calcMiles.setValue(false);
      				 parametersChanged(null, commandRec);
      			 }
      		 }
      		 else if (checker.compare(this.getClass(), "Sets whether or not miles should be used for Great Circle Distance calculations.", "[on or off]", commandName, "setCalcMiles")) {
      			 boolean current = calcMiles.getValue();
      			 calcMiles.toggleValue(parser.getFirstToken(arguments));
      			 if (current!=calcMiles.getValue()) {
      				 if (calcMiles.getValue())
      					 calcKilometers.setValue(false);
      				 parametersChanged(null, commandRec);
      			 }
      			 
      		 }
      		 else
      			 return super.doCommand(commandName, arguments, commandRec, checker);
      		 return null;
      	 }
      	 /*.................................................................................................................*/
      	 
      	 /** returns an explanation of what the module does.*/
      	 public String getExplanation() {
 		return "Geographic distance from a character matrix." ;
   	 }
   	 
	 public boolean requestPrimaryChoice(){
	   	 	return true;
	   	 }
 	 public boolean isPrerelease(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
}



class GreatCircleTaxDist extends GeoTaxaDistance {
	GeographicData gData;
	GreatCircleDistance gcdModule;
	
	public GreatCircleTaxDist(MesquiteModule ownerModule, Taxa taxa, MCharactersDistribution observedStates){
		super(ownerModule, taxa, observedStates);
		gData = (GeographicData)observedStates.getParentData();
		gcdModule = (GreatCircleDistance)ownerModule;

	}

/** Great Circle Distance as calculated using the formulae from 			
 * http://mathworld.wolfram.com/GreatCircle.html
			
*/
	public double getDistance(int taxon1,int  taxon2){
		if (taxon1>=0 && taxon1<getNumTaxa() && taxon2>=0 && taxon2<getNumTaxa() && gData!=null) {
			double radius;
			if (gcdModule.useKilometers())
				radius = 6371.0; //mean radius in km
			else 
				radius = 3958.76; //mean radius in miles
			double dist =MesquiteDouble.unassigned;
			
			double lambda1 = GeographicData.getPolarLongitude(gData.getState(GeographicData.getLongitudeCharacter(),taxon1,0));
			double lambda2 = GeographicData.getPolarLongitude(gData.getState(GeographicData.getLongitudeCharacter(),taxon2,0));
			double delta1 = GeographicData.getPolarLatitude(gData.getState(GeographicData.getLatitudeCharacter(),taxon1,0));
			double delta2 = GeographicData.getPolarLatitude(gData.getState(GeographicData.getLatitudeCharacter(),taxon2,0));

			if (MesquiteDouble.isCombinable(lambda1) && MesquiteDouble.isCombinable(lambda2) && MesquiteDouble.isCombinable(delta1) && MesquiteDouble.isCombinable(delta2)) {
				dist = Math.cos(delta1)*Math.cos(delta2)*Math.cos(lambda1-lambda2) + Math.sin(delta1)*Math.sin(delta2);
				dist = radius*Math.acos(dist);
			}
			return dist ;	
		}		
		else
			return MesquiteDouble.unassigned;
		}	


	public boolean isSymmetrical() {
		return true;
	}




}




