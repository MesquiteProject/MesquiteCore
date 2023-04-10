/* Mesquite  source code.  Copyright 2005 and onward, David Maddison and Wayne Maddison.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html) */package mesquite.lists.TaxonListVoucherCode;/*~~  */import mesquite.lists.lib.*;import mesquite.lib.characters.*;import java.awt.Label;import java.util.Vector;import mesquite.lib.*;import mesquite.lib.table.*;/* ======================================================================== */public class TaxonListVoucherCode extends TaxonListAssistant {	Taxa taxa;	MesquiteTable table=null;	OTUIDCodeInfoCoord voucherInfoTask;	MesquiteMenuItemSpec msSetID, fd, wpmMI;	/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, boolean hiredByName) {		//temporary		voucherInfoTask = (OTUIDCodeInfoCoord)hireEmployee(OTUIDCodeInfoCoord.class, null);		return true;	}	//temporary	public String getExplanationForRow(int ic){		if (taxa!=null && voucherInfoTask != null) {			VoucherInfoFromOTUIDDB vi= voucherInfoTask.getVoucherInfo((String)taxa.getAssociatedObject(VoucherInfoFromOTUIDDB.voucherDBRef, ic), (String)taxa.getAssociatedObject(VoucherInfoFromOTUIDDB.voucherCodeRef, ic));			if (vi != null)				return vi.toGenBankString();		}		return null;	}			/*.................................................................................................................*/	public void setTableAndTaxa(MesquiteTable table, Taxa taxa){		if (this.taxa != null)			this.taxa.removeListener(this);		this.taxa = taxa;		if (this.taxa != null)			this.taxa.addListener(this);		this.table = table;		deleteMenuItem(msSetID);		msSetID = addMenuItem("Get OTU ID code from first token of taxon name", makeCommand("setIDFromNameFirstToken", this));		msSetID = addMenuItem("Get OTU ID code from last token of taxon name", makeCommand("setIDFromName", this));		msSetID = addMenuItem("Prefix OTU ID code...", makeCommand("prefixIDCode", this));		msSetID = addMenuItem("Search and Replace in OTU ID code...", makeCommand("searchReplaceIDCode", this));		msSetID = addMenuItem("Remove Redundant IDs within Taxon", makeCommand("removeRedundantIDs", this));		fd = addMenuItem("Select taxa with duplicate IDs", makeCommand("selectDuplicates", this));//		wpmMI = addMenuItem("Find Voucher ID in taxon name (WPM Lab Only)", makeCommand("findWPMID", this));}	/*.................................................................................................................*/	public void dispose() {		super.dispose();		if (taxa!=null)			taxa.removeListener(this);	}	/*.................................................................................................................*/	private void prefixIDCode(){		if (table !=null && taxa!=null) {			MesquiteString prefix = new MesquiteString("");			if (!QueryDialogs.queryShortString(containerOfModule(), "Prefix", "Prefix", prefix))				return;			if (StringUtil.blank(prefix.toString()))				return;			String id = "";			boolean changed=false;			boolean anySelected = table.anyCellSelectedAnyWay();			if (employer!=null && employer instanceof ListModule) {				int c = ((ListModule)employer).getMyColumn(this);				for (int i=0; i<taxa.getNumTaxa(); i++) {					if (!anySelected || table.isCellSelectedAnyWay(c, i)) {						String current = (String)taxa.getAssociatedObject(VoucherInfoFromOTUIDDB.voucherCodeRef, i);						if (StringUtil.notEmpty(current)) {							taxa.setAssociatedObject(VoucherInfoFromOTUIDDB.voucherCodeRef, i, prefix.toString()+current);							if (!changed)								outputInvalid();							changed = true;						}					}				}			}//			if (changed)//				data.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED)); //TODO: bogus! should notify via specs not data???			outputInvalid();			parametersChanged();		}	}			String searchText ="";	String replaceText = "";	/*.................................................................................................................*/   	public boolean getOptions(){   		if (MesquiteThread.isScripting())   			return true;		MesquiteInteger buttonPressed = new MesquiteInteger(1);		ExtensibleDialog queryDialog = new ExtensibleDialog(containerOfModule(), "Replace in OTU ID Code",  buttonPressed);		queryDialog.addLabel("Replace in OTU ID Code", Label.CENTER);		SingleLineTextField searchField = queryDialog.addTextField("Search for:", searchText, 30, true);		SingleLineTextField replaceField = queryDialog.addTextField("Replace with:", replaceText, 20, true);	//	Checkbox addToEndBox = queryDialog.addCheckBox("add to end of names", addToEnd.getValue());		queryDialog.completeAndShowDialog(true);					boolean ok = (queryDialog.query()==0);				if (ok) {			searchText = searchField.getText();			replaceText = replaceField.getText();		}				queryDialog.dispose();		return ok;   	}   	/*.................................................................................................................*/	private void searchReplaceIDCode(){		if (table !=null && taxa!=null) {			if (!getOptions())				return;			if (StringUtil.blank(searchText))				return;			String id = "";			boolean changed=false;			boolean anySelected = table.anyCellSelectedAnyWay();			if (employer!=null && employer instanceof ListModule) {				int c = ((ListModule)employer).getMyColumn(this);				for (int i=0; i<taxa.getNumTaxa(); i++) {					if (!anySelected || table.isCellSelectedAnyWay(c, i)) {						String current = (String)taxa.getAssociatedObject(VoucherInfoFromOTUIDDB.voucherCodeRef, i);						if (StringUtil.notEmpty(current)) {							String newCode=StringUtil.replace(current,searchText,replaceText);							taxa.setAssociatedObject(VoucherInfoFromOTUIDDB.voucherCodeRef, i, newCode);							if (!changed)								outputInvalid();							changed = true;						}					}				}			}//			if (changed)//				data.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED)); //TODO: bogus! should notify via specs not data???			outputInvalid();			parametersChanged();		}	}	/*.................................................................................................................*/	private void setIDFromTaxonName(boolean lastToken){		if (table !=null && taxa!=null) {			String id = "";			boolean changed=false;			Parser parser = new Parser();			boolean anySelected = table.anyCellSelectedAnyWay();			parser.addToDefaultPunctuationString(".");			if (employer!=null && employer instanceof ListModule) {				int c = ((ListModule)employer).getMyColumn(this);				for (int i=0; i<taxa.getNumTaxa(); i++) {					if (!anySelected || table.isCellSelectedAnyWay(c, i)) {						id = taxa.getName(i);												parser.setString(id);						if (lastToken)							id = parser.getLastToken();						else							id = parser.getFirstToken();						if (!StringUtil.blank(id)){							taxa.setAssociatedObject(VoucherInfoFromOTUIDDB.voucherCodeRef, i, id);							if (!changed)								outputInvalid();							changed = true;						}					}				}			}//			if (changed)//				data.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED)); //TODO: bogus! should notify via specs not data???			outputInvalid();			parametersChanged();		}	}	/*.................................................................................................................*/	private void removeRedundantIDs(){		if (table !=null && taxa!=null) {			String codes = "";			boolean changed=false;			Parser parser = new Parser();			boolean anySelected = table.anyCellSelectedAnyWay();			parser.addToDefaultPunctuationString(".");			if (employer!=null && employer instanceof ListModule) {				int c = ((ListModule)employer).getMyColumn(this);				for (int i=0; i<taxa.getNumTaxa(); i++) {					if (!anySelected || table.isCellSelectedAnyWay(c, i)) {						codes = (String)taxa.getAssociatedObject(VoucherInfoFromOTUIDDB.voucherCodeRef, i);						parser.setString(codes);						parser.setWhitespaceString("/");						String singleCode = parser.getFirstToken();						Vector codeVector = new Vector();						while (StringUtil.notEmpty(singleCode)) {							if (StringUtil.notEmpty(singleCode))								codeVector.add(singleCode);							singleCode = parser.getNextToken();						}												if (codeVector.size()>1) {   // there is more than one code							String newCodes = "";							for (int j=0; j<codeVector.size(); j++) {  // these loops look for any that are duplicates and sets them to null								String code1 = (String)codeVector.elementAt(j);								if (StringUtil.notEmpty(code1)) {									for (int k=j+1; k<codeVector.size();k++) {										String code2 = (String)codeVector.elementAt(k);										if (code1.equalsIgnoreCase(code2))											codeVector.set(k, null);									}								}							}							for (int j=0; j<codeVector.size(); j++) {  // these loops look for any that are duplicates and sets them to null								String code1 = (String)codeVector.elementAt(j);								if (StringUtil.notEmpty(code1)) {									if (StringUtil.notEmpty(newCodes))										newCodes = newCodes+"/";									newCodes = newCodes + code1;								}							}							if (!StringUtil.blank(newCodes) && !newCodes.equalsIgnoreCase(codes)){								taxa.setAssociatedObject(VoucherInfoFromOTUIDDB.voucherCodeRef, i, newCodes);								if (!changed)									outputInvalid();								changed = true;							}						}					}				}			}			outputInvalid();			parametersChanged();		}	}	/*.................................................................................................................*/	private void selectDuplicates(){		if (table !=null && taxa!=null) {			boolean changed=false;			if (employer!=null && employer instanceof ListModule) {				int c = ((ListModule)employer).getMyColumn(this);				for (int i=0; i<taxa.getNumTaxa(); i++) {					String target = (String)taxa.getAssociatedObject(VoucherInfoFromOTUIDDB.voucherCodeRef, i);										if (!StringUtil.blank(target))						for (int k=i+1; k<taxa.getNumTaxa(); k++) {						String id = (String)taxa.getAssociatedObject(VoucherInfoFromOTUIDDB.voucherCodeRef, k);												if (!StringUtil.blank(id)) {							if (id.equalsIgnoreCase(target)){								if (!taxa.getSelected(i))									taxa.setSelected(i, true);								if (!taxa.getSelected(k))									taxa.setSelected(k, true);								changed = true;							}													}					}				}			}		if (changed)				taxa.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));			outputInvalid();			parametersChanged();		}	}	/*.................................................................................................................*/	private void findIDInTaxonNameWPM(){		if (table !=null && taxa!=null) {			boolean changed=false;			if (employer!=null && employer instanceof ListModule) {				int c = ((ListModule)employer).getMyColumn(this);				for (int i=0; i<taxa.getNumTaxa(); i++) {					if (table.isCellSelectedAnyWay(c, i)) {						String id = "";						String name = taxa.getName(i);						if (name.indexOf("JXZ") >=0)							id = name.substring(name.indexOf("JXZ"), name.indexOf("JXZ")+6);						else if (name.indexOf("MRB") >=0)							id = name.substring(name.indexOf("MRB"), name.indexOf("MRB")+6);						else if (name.indexOf(".DNA") >=0)							id = name.substring(name.indexOf(".DNA")+1, name.indexOf(".DNA")+8);						else if (name.indexOf(".d") >=0)							id = name.substring(name.indexOf(".d")+1, name.indexOf(".d")+5);						else if (name.indexOf(".s") >=0)							id = name.substring(name.indexOf(".s")+1, name.indexOf(".s")+5);						else if (name.indexOf(".S") >=0){							int st = name.indexOf(".S");							int lg = name.length();							if (st+5<= lg)								id = name.substring(name.indexOf(".S")+1, name.indexOf(".S")+5);							else if (st+4<= lg)								id = name.substring(name.indexOf(".S")+1, name.indexOf(".S")+4);							else if (st+3<= lg)								id = name.substring(name.indexOf(".S")+1, name.indexOf(".S")+3);							else if (st+2<= lg)								id = name.substring(name.indexOf(".S")+1, name.indexOf(".S")+2);						}						else if (name.indexOf(".GR") >=0)							id = name.substring(name.indexOf(".GR")+1, name.indexOf(".GR")+6);						else if (name.indexOf("d0") >=0)							id = name.substring(name.indexOf("d0"), name.indexOf("d0")+4);						else if (name.indexOf("d1") >=0)							id = name.substring(name.indexOf("d1"), name.indexOf("d1")+4);						else if (name.indexOf("d2") >=0)							id = name.substring(name.indexOf("d2"), name.indexOf("d2")+4);						else if (name.indexOf("d3") >=0)							id = name.substring(name.indexOf("d3"), name.indexOf("d3")+4);						else if (name.indexOf("d4") >=0)							id = name.substring(name.indexOf("d4"), name.indexOf("d4")+4);												if (!StringUtil.blank(id)){							taxa.setAssociatedObject(VoucherInfoFromOTUIDDB.voucherCodeRef, i, id);							if (!changed)								outputInvalid();							changed = true;						}					}				}			}//			if (changed)//				data.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED)); //TODO: bogus! should notify via specs not data???			outputInvalid();			parametersChanged();		}	}	/*.................................................................................................................*/	public Object doCommand(String commandName, String arguments, CommandChecker checker) {		if (checker.compare(this.getClass(), "Sets the OTU ID code to be the last token of the taxon name", null, commandName, "setIDFromName")) {			setIDFromTaxonName(true);			}		else if (checker.compare(this.getClass(), "Sets the OTU ID code to be the first token of the taxon name", null, commandName, "setIDFromNameFirstToken")) {			setIDFromTaxonName(false);			}		else if (checker.compare(this.getClass(), "Within each taxon's OTU ID code, if a code is listed more then once, extras are removed.", null, commandName, "removeRedundantIDs")) {			removeRedundantIDs();			}		else if (checker.compare(this.getClass(), "Prefixes the OTU ID code with a string", null, commandName, "prefixIDCode")) {			prefixIDCode();			}		else if (checker.compare(this.getClass(), "Searches and replaces text in the OTU ID code", null, commandName, "searchReplaceIDCode")) {			searchReplaceIDCode();			}		else if (checker.compare(this.getClass(), "Finds the OTU ID code in the taxon name (WPM lab only)", null, commandName, "findWPMID")) {			findIDInTaxonNameWPM();			}		else if (checker.compare(this.getClass(), "Selects taxa with duplicate Names", null, commandName, "selectDuplicates")) {			selectDuplicates();			}		else			return  super.doCommand(commandName, arguments, checker);		return null;	}	public void changed(Object caller, Object obj, Notification notification){		outputInvalid();		parametersChanged(notification);	}	public String getTitle() {		return "OTU ID code";	}	public String getStringForTaxon(int ic){				if (taxa!=null) {			Object n = (String)taxa.getAssociatedObject(VoucherInfoFromOTUIDDB.voucherCodeRef, ic);			if (n !=null)				return ((String)n);					}		return "-";	}	/*...............................................................................................................*/	/** returns whether or not a cell of table is editable.*/	public boolean isCellEditable(int row){		return true;	}	/*...............................................................................................................*/	/** for those permitting editing, indicates user has edited to incoming string.*/	public void setString(int row, String s){		if (taxa!=null) {			taxa.setAssociatedObject(VoucherInfoFromOTUIDDB.voucherCodeRef, row, s);		}			}	public boolean useString(int ic){		return true;	}		public String getWidestString(){		return "88888888888888888  ";	}	/*.................................................................................................................*/	public String getName() {		return "OTU ID Code";	}	/*.................................................................................................................*/	public boolean isPrerelease(){		return false;  	}	/*.................................................................................................................*/	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/	public int getVersionOfFirstRelease(){		return -1;  	}	/*.................................................................................................................*/	/** returns whether this module is requesting to appear as a primary choice */	public boolean requestPrimaryChoice(){		return true;  	}		/*.................................................................................................................*/	/** returns an explanation of what the module does.*/	public String getExplanation() {		return "Lists the OTU ID code for a taxon." ;	}}