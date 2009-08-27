/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison.Version 2.7, August 2009.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html) */package mesquite.molec.ColorByAA; import java.util.*;import java.awt.*;import mesquite.lib.*;import mesquite.lib.characters.*;import mesquite.lib.duties.*;import mesquite.lib.table.*;import mesquite.categ.lib.*;/* ======================================================================== */public class ColorByAA extends DataWindowAssistantI implements CellColorer, CellColorerMatrix {	MesquiteTable table;	protected DNAData data;	MesquiteBoolean emphasizeDegeneracy;	/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, boolean hiredByName){		emphasizeDegeneracy = new MesquiteBoolean(false);		addCheckMenuItem(null, "Emphasize Less Degenerate Amino Acids", makeCommand("emphasizeDegeneracy", this), emphasizeDegeneracy);		return true;	}	public boolean setActiveColors(boolean active){		setActive(true);		return true; //TODO: check success	}	/*.................................................................................................................*/	public boolean isSubstantive(){		return false;	}	/*.................................................................................................................*/	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/	public int getVersionOfFirstRelease(){		return 110;  	}	/*.................................................................................................................*/	public Snapshot getSnapshot(MesquiteFile file) { 		Snapshot temp = new Snapshot();		temp.addLine("emphasizeDegeneracy " + emphasizeDegeneracy.toOffOnString());		return temp;	}	/*.................................................................................................................*/	public Object doCommand(String commandName, String arguments, CommandChecker checker) {		if (checker.compare(this.getClass(), "Turns on or off the emphasizing of less degenerate codons.", null, commandName, "emphasizeDegeneracy")) {			emphasizeDegeneracy.toggleValue(parser.getFirstToken(arguments));			parametersChanged();		}		else			return  super.doCommand(commandName, arguments, checker);		return null;	}	/*.................................................................................................................*/	public boolean isPrerelease(){		return false;	}	/*.................................................................................................................*/	public void setTableAndData(MesquiteTable table, CharacterData data){		this.table = table;		if (data instanceof DNAData)			this.data = (DNAData)data;	}	/*.................................................................................................................*/	public String getName() {		return "Color By Amino Acid";	}	public String getNameForMenuItem() {		return "Color Nucleotide by Amino Acid";	}	/*.................................................................................................................*/	public String getExplanation() {		return "Colors the cells of a DNA matrix by the amino acids for which they code.";	}	/*.................................................................................................................*/	public void viewChanged(){	}	public String getCellString(int ic, int it){		if (!isActive())			return null;		return "Colored to show translated amino acid";	}	ColorRecord[] legend;	/*.................................................................................................................*/	public ColorRecord[] getLegendColors(){		if (data == null)			return null;		legend = new ColorRecord[ProteinState.maxProteinState+1];		for (int is = 0; is<=ProteinState.maxProteinState; is++) {			legend[is] = new ColorRecord(ProteinData.getProteinColorOfState(is), ProteinData.getStateLongName(is));		}		return legend;	}	/*.................................................................................................................*/	public String getColorsExplanation(){		if (data == null)			return null;		/*  		if (data.getClass() == CategoricalData.class){   			return "Colors of states may vary from character to character";   		}		 */		return null;	}	public Color alterColor(int ic, int it, long s, Color color){		if (emphasizeDegeneracy.getValue()) {			if (!CategoricalState.hasMultipleStates(s)) {				int aa = CategoricalState.minimum(s);				int degeneracy = data.getAminoAcidDegeneracy(ic,aa);				if (degeneracy==1)					color = ColorDistribution.darker(color, 0.4);				else if (degeneracy==2)					color = ColorDistribution.brighter(color, 0.4);				else if (degeneracy>2)					color = ColorDistribution.brighter(color, 0.1);			}		}		return color;	}	public Color getCellColor(int ic, int it){		if (ic<0 || it<0)			return null;		if (data == null)			return null;		else if (!data.isCoding(ic)) {			Color color = data.getColorOfStates(ic, it);			//return color;			return ColorDistribution.brighter(color, 0.6);		}		else {			long s = data.getAminoAcid(ic,it,true);			if (!CategoricalState.isImpossible(s)) {				return alterColor(ic,it,s,ProteinData.getAminoAcidColor(s));			}			else {				return data.getColorOfStates(ic, it);			}		}	}	public CompatibilityTest getCompatibilityTest(){		return new RequiresAnyDNAData();	}	public String getParameters(){		if (isActive())			return getName();		return null;	}}