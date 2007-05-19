/* Mesquite source code.  Copyright 1997-2006 W. Maddison and D. Maddison.Version 1.11, June 2006.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.charMatrices.NoColor; import java.util.*;import java.awt.*;import mesquite.lib.*;import mesquite.lib.characters.*;import mesquite.lib.duties.*;import mesquite.lib.table.*;/* ======================================================================== */public class NoColor extends DataWindowAssistantI implements CellColorer {	/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName){		return true;	}	/*.................................................................................................................*/   	 public boolean isSubstantive(){   	 	return false;   	 }	/*.................................................................................................................*/	public void setTableAndData(MesquiteTable table, CharacterData data, CommandRecord commandRec){		this.data = data;		taxa = data.getTaxa();	}	CharacterData data;	Taxa taxa;	/*.................................................................................................................*/    	 public String getName() {		return "No Color";   	 }	/*.................................................................................................................*/  	 public String getExplanation() {		return "Turns off cell coloring.";   	 }   	 	/*.................................................................................................................*/   	public void viewChanged(CommandRecord commandRec){   	}   	 public boolean setActiveColors(boolean active, CommandRecord commandRec){   	 	setActive(true);		return true;   	 }   	public ColorRecord[] getLegendColors(CommandRecord commandRec){   		return null;   	}   	public String getColorsExplanation(CommandRecord commandRec){  		return null;   	}	public Color getCellColor(int ic, int it){		if (ic < 0 && it>=0)  {			TaxaPartition part = (TaxaPartition)taxa.getCurrentSpecsSet(TaxaPartition.class);			if (part!=null){				TaxaGroup mi = (TaxaGroup)part.getProperty(it);				if (mi!=null)					return mi.getColor();			}			return null;		}		else if (ic>=0 && it<0) {  			CharacterPartition part = (CharacterPartition)data.getCurrentSpecsSet(CharacterPartition.class);			if (part!=null){				CharactersGroup mi = (CharactersGroup)part.getProperty(ic);				if (mi!=null)					return mi.getColor();			}return null;		}		return null;	}   	public String getCellString(int ic, int it){		if (ic < 0 && it>=0)  {			TaxaPartition part = (TaxaPartition)taxa.getCurrentSpecsSet(TaxaPartition.class);			if (part!=null){				TaxaGroup mi = (TaxaGroup)part.getProperty(it);				if (mi!=null)					return "This taxon belongs to the group " + mi.getName();			}		}		else if (ic>=0 && it<0) {  			CharacterPartition part = (CharacterPartition)data.getCurrentSpecsSet(CharacterPartition.class);			if (part!=null){				CharactersGroup mi = (CharactersGroup)part.getProperty(ic);				if (mi!=null)					return "This character belongs to the group " + mi.getName();			}		}		return "";   	}	public String getParameters(){		return null;	}}	