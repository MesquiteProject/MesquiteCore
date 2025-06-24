/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html) */package mesquite.lists.TaxonListShowAllHasData;/*~~  */import java.util.Vector;

import mesquite.lib.CommandChecker;
import mesquite.lib.CommandRecord;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Puppeteer;import mesquite.lib.characters.CharacterData;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.taxa.Taxa;
import mesquite.lists.lib.ListAssistant;
import mesquite.lists.lib.ListModule;
import mesquite.lists.lib.TaxaListAssistantI;/* ======================================================================== */public class TaxonListShowAllHasData extends TaxaListAssistantI  {	Taxa taxa;	MesquiteTable table;	public String getName() {		return "Show Columns for All Matrices";	}	public String getExplanation() {		return "Shows the Has Data column for all matrices.";	}	/*.................................................................................................................*/	public int getVersionOfFirstRelease(){		return 304;  	}	/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		addMenuItem("Show Columns for All Matrices", new MesquiteCommand("showAll", this));		return true;	}
	
	void makeColumn(Puppeteer puppeteer, ListModule listModule, CharacterData data){
		String commands = "newAssistant #TaxaListHasData; " +
				"tell It; getMatrixSource #mesquite.charMatrices.CharMatrixCoordIndep.CharMatrixCoordIndep; " +
				"tell It; setCharacterSource #mesquite.charMatrices.StoredMatrices.StoredMatrices; tell It; setDataSet " + getProject().getCharMatrixReferenceExternal(data) + "; " +
				"endTell; endTell; endTell;";
		puppeteer.execute(listModule,  commands, new MesquiteInteger(0), null, false);
		
	}	/*.................................................................................................................*/	/** A request for the MesquiteModule to perform a command.  It is passed two strings, the name of the command and the arguments.	This should be overridden by any module that wants to respond to a command.*/	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 		if (checker.compare(MesquiteModule.class, null, null, commandName, "showAll")) {
			if (taxa == null)
				return null;
			int numMatrices = getProject().getNumberCharMatrices(taxa);
			if (numMatrices<1)
				return null;
			Vector datas = new Vector();
			for (int i = 0; i<numMatrices; i++){
				CharacterData data = getProject().getCharacterMatrix(taxa, i);
				if (data.isUserVisible())
					datas.addElement(data);
			}			if (getEmployer() instanceof ListModule){
				ListModule listModule = (ListModule)getEmployer();
				Vector v = listModule.getAssistants();
				for (int k = 0; k< v.size(); k++){
					ListAssistant a = (ListAssistant)v.elementAt(k);
					if (a instanceof mesquite.molec.TaxaListHasData.TaxaListHasData){
						mesquite.molec.TaxaListHasData.TaxaListHasData tLHD = (mesquite.molec.TaxaListHasData.TaxaListHasData)a;
						CharacterData data = tLHD.getCharacterData();
						if (datas.indexOf(data)>=0)
							datas.removeElement(data);
					}
				}
				Puppeteer puppeteer = new Puppeteer(this);
				CommandRecord prevR = MesquiteThread.getCurrentCommandRecord();
				CommandRecord cRecord = new CommandRecord(true);
				MesquiteThread.setCurrentCommandRecord(cRecord);
				//at this point the vector should include only the ones not being shown.
				for (int i = 0; i<datas.size(); i++)
					makeColumn(puppeteer, listModule, (CharacterData)datas.elementAt(i));
			
				MesquiteThread.setCurrentCommandRecord(prevR);
			}		}		else			return  super.doCommand(commandName, arguments, checker);		return null;	}	/*.................................................................................................................*/	public boolean isSubstantive(){		return false;	}	/*.................................................................................................................*/	public void setTableAndTaxa(MesquiteTable table, Taxa taxa){		this.table = table;		this.taxa = taxa;	}}