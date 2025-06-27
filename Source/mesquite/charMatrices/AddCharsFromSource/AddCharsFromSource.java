package mesquite.charMatrices.AddCharsFromSource;

import mesquite.categ.lib.CategDataAlterer;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.ResultCodes;
import mesquite.lib.UndoReference;
import mesquite.lib.characters.AltererWholeCharacterAddRemove;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.CharacterDistribution;
import mesquite.lib.characters.CharacterState;
import mesquite.lib.duties.CharSourceCoordObed;
import mesquite.lib.table.MesquiteTable;

/* ======================================================================== */
public class AddCharsFromSource extends CategDataAlterer implements AltererWholeCharacterAddRemove {
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false; 
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Add Characters from Source...";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Add Characters from Source";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Adds characters from a source of characters." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 275;  
	}

	CharSourceCoordObed characterSourceTask;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		//character source %%%%%%%%
		characterSourceTask = (CharSourceCoordObed)hireEmployee(CharSourceCoordObed.class, "Source of characters (for " + getName() + ")");
		if (characterSourceTask == null)
			return sorry(getName() + " couldn't start because no source of characters was obtained.");
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}
	/*.................................................................................................................*/
	/** Called to alter data in those cells selected in table*/
	public int alterData(CharacterData data, MesquiteTable table, UndoReference undoReference){
		
		characterSourceTask.initialize(data.getTaxa());
		int maxNum = characterSourceTask.getNumberOfCharacters(data.getTaxa());
		String s = "";
		if (!MesquiteInteger.isCombinable(maxNum))
			maxNum = 10000;
		else 
			s = " (" + maxNum + " available)";
		int numChars = MesquiteInteger.queryInteger(containerOfModule(), "Number of characters", "Number of characters to add from " + characterSourceTask.getName() + s, 1, 1, maxNum);
		if (!MesquiteInteger.isCombinable(numChars))
			return ResultCodes.USER_STOPPED;
		CharacterState cs = null;
		for (int i = 0; i<numChars; i++){
			CharacterDistribution dist = characterSourceTask.getCharacter(data.getTaxa(), i);
			if (data.getStateClass() != dist.getStateClass()){
				discreetAlert("Sorry, the source is supplying characters of a different type, and so can't be used");
				return -20;
			}
			data.addCharacters(data.getNumChars(), 1, false);
			int ic = data.getNumChars()-1;
			for (int it = 0; it<data.getNumTaxa(); it++){
				cs = dist.getCharacterState(cs, it);
				data.setState(ic, it, cs);
			}
		}
		return ResultCodes.SUCCEEDED;
	}

	//	Double d = new Double(value);

	/*.................................................................................................................*/
	public boolean showCitation() {
		return false;
	}
}


