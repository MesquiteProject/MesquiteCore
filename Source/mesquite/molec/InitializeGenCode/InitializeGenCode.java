/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.molec.InitializeGenCode;
/*~~  */


import mesquite.categ.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.molec.lib.*;


/* ======================================================================== */
public class InitializeGenCode extends FileInit {
	GenCodeModel standardModel, vertMitoModel, invertMitoModel, moldMitoModel, yeastMitoModel, ciliateModel;
	GenCodeModel echinoModel,euplotidModel;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		standardModel = new GenCodeModelStandard();
		vertMitoModel = new GenCodeModelVertMito();
		invertMitoModel = new GenCodeModelInvertMito();
		yeastMitoModel = new GenCodeModelYeastMito();
		moldMitoModel =  new GenCodeModelMoldProtMito();
		ciliateModel = new GenCodeModelCiliate();
		echinoModel = new GenCodeModelEchino();
		euplotidModel = new GenCodeModelEuplotid();
		CategoricalData.registerDefaultModel("GeneticCode", "Standard");
		DNAData.registerDefaultModel("GeneticCode", "Standard");
		ProteinData.registerDefaultModel("GeneticCode", "Standard");
		return true;
	}
	
	/*.................................................................................................................*/
 	/** A method called immediately after the file has been established but not yet read in.*/
 	public void projectEstablished() {
 		standardModel.addToFile(null, getProject(), null);
		vertMitoModel.addToFile(null, getProject(), null);
		invertMitoModel.addToFile(null, getProject(), null);
		yeastMitoModel.addToFile(null, getProject(), null);
		moldMitoModel.addToFile(null, getProject(), null);
		ciliateModel.addToFile(null, getProject(), null);
		echinoModel.addToFile(null, getProject(), null);
		euplotidModel.addToFile(null, getProject(), null);
//		xxx.addToFile(null, getProject(), null);
		super.projectEstablished();
 	}
	/*.................................................................................................................*/
 	public void fileElementAdded(FileElement element) {
 		if (element == null || getProject()==null)
 			return;
		if (element instanceof MolecularData) {
	 		CharacterModel defaultModel=null;
			GenCodeModelSet currentGenCodeModels;
	 		CharacterData data = (CharacterData)element;
	 		if (data.getCurrentSpecsSet(GenCodeModelSet.class) == null) {
		 		defaultModel =  data.getDefaultModel("GeneticCode");
		 		currentGenCodeModels= new GenCodeModelSet("UNTITLED", data.getNumChars(), defaultModel, data);
		 		currentGenCodeModels.addToFile(element.getFile(), getProject(), null);
		 		data.setCurrentSpecsSet(currentGenCodeModels, GenCodeModelSet.class);
	 		}
	  		if (getProject().getCharacterModels()==null)
	 			MesquiteMessage.warnProgrammer("charModels null in iMS Init GenCode");
		}
	}
   
	/*.................................................................................................................*/
    	 public boolean isSubstantive() {
		return false;
   	 }
    		/*.................................................................................................................*/
    	 	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
    	 	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
    	 	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
    	    	public int getVersionOfFirstRelease(){
    	    		return 110;  
    	    	}
    	    	/*.................................................................................................................*/
    	    	public boolean isPrerelease(){
    	    		return true;
    	    	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Initialize Genetic Code";
   	 }
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Initializes default genetic codes." ;
   	 }
	/*.................................................................................................................*/
   	 
}

