/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.CharListGenCodeModels;
/*~~  */

import mesquite.lists.lib.*;
import mesquite.molec.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.table.*;
import mesquite.categ.lib.*;

/* ======================================================================== */
public class CharListGenCodeModels extends CharListAssistant {
	CharacterData data=null;
	MesquiteTable table=null;
	MesquiteSubmenuSpec mss;
	MesquiteMenuItemSpec mScs, mStc, mRssc, mLine, mLine2;
	MesquiteSubmenuSpec webPageSubmenu, toStringSubmenu;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		getProject().getCentralModelListener().addListener(this);
		return true;
	}
	/*.................................................................................................................*/
	public void endJob() {
		getProject().getCentralModelListener().removeListener(this);
		super.endJob();
	}
	/*.................................................................................................................*/
	public void disposing(Object obj){
		if (obj instanceof GenCodeModel) {
			parametersChanged();
		}
	}
	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the genetic code of the selected characters", "[number of model]", commandName, "setModel")) {
			if (table !=null && data!=null) {
				boolean changed=false;
				int whichModel = MesquiteInteger.fromFirstToken(arguments, pos);
				if (!MesquiteInteger.isCombinable(whichModel))
					return null;
				CharacterModel model = getProject().getCharacterModel(new ModelCompatibilityInfo(GenCodeModel.class, data.getStateClass()), whichModel);
				/*
				 *  				String genCodeName = parser.getFirstToken(arguments);
  				genCodeName = parser.getNextToken();
  				 CharacterModel model = getProject().getCharacterModel(new ModelCompatibilityInfo(GenCodeModel.class, data.getStateClass()), genCodeName);
				 */
				if (model != null) {
					ModelSet modelSet = (ModelSet) data.getCurrentSpecsSet(GenCodeModelSet.class);
					if (modelSet == null) {
						CharacterModel defaultModel =  data.getDefaultModel("GeneticCode");
						modelSet= new GenCodeModelSet("Genetic Code Model Set", data.getNumChars(), defaultModel, data);
						modelSet.addToFile(data.getFile(), getProject(), findElementManager(GenCodeModelSet.class)); 
						data.setCurrentSpecsSet(modelSet, GenCodeModelSet.class);
					}
					if (modelSet != null) {
						if (employer!=null && employer instanceof ListModule) {
							int c = ((ListModule)employer).getMyColumn(this);
							for (int i=0; i<data.getNumChars(); i++) {
								if (table.isCellSelectedAnyWay(c, i)) {
									modelSet.setModel(model, i);
									if (!changed)
										outputInvalid();

									changed = true;
								}
							}
						}
					}
				}

				if (changed)
					data.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  
				//TODO: not quite kosher; HOW TO HAVE MODEL SET LISTENERS??? -- modelSource
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Stores current genetic code model set (CODESET)", null, commandName, "storeCurrent")) {
			if (data!=null){
				SpecsSetVector ssv = data.getSpecSetsVector(GenCodeModelSet.class);
				if (ssv == null || ssv.getCurrentSpecsSet() == null) {
					CharacterModel defaultModel =  data.getDefaultModel("GeneticCode");
					ModelSet modelSet= new GenCodeModelSet("Genetic Code Set", data.getNumChars(), defaultModel, data);
					modelSet.addToFile(data.getFile(), getProject(), findElementManager(GenCodeModelSet.class)); 
					data.setCurrentSpecsSet(modelSet, GenCodeModelSet.class);
					ssv = data.getSpecSetsVector(GenCodeModelSet.class);
				}
				if (ssv!=null) {
					SpecsSet s = ssv.storeCurrentSpecsSet();
					if (s.getFile() == null)
						s.addToFile(data.getFile(), getProject(), findElementManager(GenCodeModelSet.class));
					s.setName(ssv.getUniqueName("Genetic Code Set"));
					String name = MesquiteString.queryString(containerOfModule(), "Name", "Name of genetic code set to be stored", "Genetic Code Set");
					if (!StringUtil.blank(name))
						s.setName(name);
					ssv.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED));  
				}
				else MesquiteMessage.warnProgrammer("sorry, can't store because no specssetvector");
			}
		}
		else if (checker.compare(this.getClass(), "Replace stored genetic code set (CODESET) by the current one", null, commandName, "replaceWithCurrent")) {
			if (data!=null){
				SpecsSetVector ssv = data.getSpecSetsVector(GenCodeModelSet.class);
				if (ssv!=null) {
					SpecsSet chosen = (SpecsSet)ListDialog.queryList(containerOfModule(), "Replace stored set", "Choose stored genetic code set to replace by current set", MesquiteString.helpString, ssv, 0);
					if (chosen!=null){
						SpecsSet current = ssv.getCurrentSpecsSet();
						ssv.replaceStoredSpecsSet(chosen, current);
					}
				}

			}
		}
		else if (checker.compare(this.getClass(), "Loads the stored genetic code set to be the current one", "[number of genetic code set to load]", commandName, "loadToCurrent")) {
			if (data !=null) {
				int which = MesquiteInteger.fromFirstToken(arguments, stringPos);
				if (MesquiteInteger.isCombinable(which)){
					SpecsSetVector ssv = data.getSpecSetsVector(GenCodeModelSet.class);
					if (ssv!=null) {
						SpecsSet chosen = ssv.getSpecsSet(which);
						if (chosen!=null){
							ssv.setCurrentSpecsSet(chosen.cloneSpecsSet()); 
							data.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED)); //should notify via specs not data???
							return chosen;
						}
					}
				}
			}
		}
		else if (checker.compare(this.getClass(), "Displays the NCBI page for the genetic code", "", commandName, "showNCBIPage")) {
			boolean changed=false;
			int whichModel = MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(whichModel))
				return null;
			CharacterModel model = getProject().getCharacterModel(new ModelCompatibilityInfo(GenCodeModel.class, data.getStateClass()), whichModel);
			if (model != null) {
				GeneticCode theCode = ((GenCodeModel)model).getGeneticCode();
				if (theCode!=null)
					theCode.showNCBIPage();
			}

		}
		else if (checker.compare(this.getClass(), "Displays the genetic code in the log window", "", commandName, "showCodeInLog")) {
			boolean changed=false;
			int whichModel = MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(whichModel))
				return null;
			CharacterModel model = getProject().getCharacterModel(new ModelCompatibilityInfo(GenCodeModel.class, data.getStateClass()), whichModel);
			if (model != null) {
				GeneticCode theCode = ((GenCodeModel)model).getGeneticCode();

				if (theCode!=null) {
					String s = theCode.toNCBIString();
					log(s);
				}
			}

		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}


	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		/* hire employees here */
		deleteMenuItem(mss);
		deleteMenuItem(mScs);
		deleteMenuItem(mRssc);
		deleteMenuItem(mLine);
		deleteMenuItem(mLine2);
		deleteMenuItem(mStc);
		deleteMenuItem(webPageSubmenu);
		deleteMenuItem(toStringSubmenu);
		mss = addSubmenu(null, "Genetic Code", makeCommand("setModel", this), getProject().getCharacterModels());
		mLine = addMenuSeparator();
		mScs = addMenuItem("Store current set...", makeCommand("storeCurrent",  this));
		mRssc = addMenuItem("Replace stored set by current...", makeCommand("replaceWithCurrent",  this));
		mss.setCompatibilityCheck(new ModelCompatibilityInfo(GenCodeModel.class, data.getStateClass()));
		if (data !=null)
			mStc = addSubmenu(null, "Load genetic code set", makeCommand("loadToCurrent",  this), data.getSpecSetsVector(GenCodeModelSet.class));
		mLine2 = addMenuSeparator();
		webPageSubmenu = addSubmenu(null, "Show NCBI Web Page", makeCommand("showNCBIPage", this), getProject().getCharacterModels());
		webPageSubmenu.setCompatibilityCheck(new ModelCompatibilityInfo(GenCodeModel.class, data.getStateClass()));
		toStringSubmenu = addSubmenu(null, "List Code in Log Window", makeCommand("showCodeInLog", this), getProject().getCharacterModels());
		toStringSubmenu.setCompatibilityCheck(new ModelCompatibilityInfo(GenCodeModel.class, data.getStateClass()));
		this.data = data;
		this.table = table;
	}
	public void changed(Object caller, Object obj, Notification notification){
		if (Notification.appearsCosmetic(notification))
			return;
		outputInvalid();
		parametersChanged(notification);
	}
	public String getTitle() {
		return "Genetic Code";
	}
	public String getStringForCharacter(int ic){
		if (data!=null) {
			ModelSet modelSet = (ModelSet)data.getCurrentSpecsSet(GenCodeModelSet.class);
			if (modelSet != null) {
				CharacterModel model = modelSet.getModel(ic);
				if (model!=null) {
					return model.getName();
				}
			}
			else
				MesquiteMessage.warnProgrammer("model set null in gsfc");
		}
		return "?";
	}
	public String getWidestString(){
		return "Invertebrate Mitochondrial ";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Current Genetic Codes ";
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
		return false;
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Shows current genetic codes applied to characters in character list window." ;
	}
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyMolecularData();
	}
}

