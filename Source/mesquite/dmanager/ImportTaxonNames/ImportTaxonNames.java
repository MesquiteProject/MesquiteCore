package mesquite.dmanager.ImportTaxonNames;

import java.util.StringTokenizer;

import mesquite.lib.characters.*;
import mesquite.lib.*;
import mesquite.lib.duties.TaxonUtility;

public class ImportTaxonNames extends TaxonUtility {

	/* ................................................................................................................. */
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {

		return true;
	}

	/* ................................................................................................................. */
	public String getName() {
		return "Import Taxon Names";
	}

	/* ................................................................................................................. */
	public String getExplanation() {
		return "Reads a tab-delimited text files of voucher codes (column heading \"ID\") and taxon names (column heading \"name\"), matching taxa by voucher code and renaming taxa accordingly";
	}

	/* ................................................................................................................. */
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice() {
		return false;
	}

	/* ................................................................................................................. */
	public boolean isPrerelease() {
		return true;
	}

	/* ................................................................................................................. */
	public boolean isSubstantive() {
		return true;
	}

	/* ................................................................................................................. */
	/** if returns true, then requests to remain on even after operateOnTaxa is called. Default is false */
	public boolean pleaseLeaveMeOn() {
		return false;
	}

	/** Called to operate on the taxa in the block. Returns true if taxa altered */
	public boolean operateOnTaxa(Taxa taxa) {
		MesquiteFile file = MesquiteFile.open(true, null);
		if (file == null) {
			MesquiteMessage.println("No file for importing taxon names");
			return false;
		}
		String path = file.getPath();
		String[][] table = MesquiteFile.getTabDelimitedTextFile(path);
		if (table == null || table.length == 0) {
			MesquiteMessage.println("No appropriate data found in file for importing taxon names");
			return false;
		}
		int id = -1;
		int iName = -1;
		for (int i = 0; i < table[0].length; i++) {
			String columnName = table[0][i];
			if (columnName != null) {
				if (columnName.equalsIgnoreCase("ID"))
					id = i;
				else if (columnName.equalsIgnoreCase("name"))
					iName = i;
				else {
				}
			}
		}
		if (id < 0) {
			MesquiteMessage.println("ID column not found in file for importing taxon names");
			return false;
		}
		int count = 0;
		NameReference colorNameRef = NameReference.getNameReference("color");
		boolean noneSelected = !taxa.anySelected();
		for (int row = 0; row < table.length; row++) {
			int it = findTaxon(taxa, table[row][id]);
			if (it >= 0) {
				if (taxa.getSelected(it) || noneSelected) {
					String s = table[row][iName];
					if (!StringUtil.blank(s) && s.charAt(0) == '\"' && s.charAt(s.length() - 1) == '\"') {
						s = s.substring(1, s.length() - 1);
					}
					if (!StringUtil.blank(s) && !s.equals(taxa.getTaxonName(it))){
					taxa.setTaxonNameNoWarnNoNotify(it, s);
					taxa.setAssociatedLong(colorNameRef, it, 5);
					count++;
					}
				}
			}
			// else
			// logln("Taxon not found in taxa block: " + table[row][id]);
		}
		logln("Taxon names imported.  Number of matches found: " + count);
		taxa.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED));
		return true;
	}

	public NameReference voucherCodeRef = NameReference.getNameReference("VoucherCode"); //String: taxa
	private int findTaxon(Taxa taxa, String id) {
		if (StringUtil.blank(id))
			return -1;
		for (int it = 0; it < taxa.getNumTaxa(); it++) {
			String code =  (String)taxa.getAssociatedObject(voucherCodeRef, it);

			if (!StringUtil.blank(code)) {
				if (code.equalsIgnoreCase(id))
					return it;
				StringTokenizer tokenizer = new StringTokenizer(code, "/");
				while (tokenizer.hasMoreElements()) {
					String token = tokenizer.nextToken();
					if (!StringUtil.blank(token) && token.equalsIgnoreCase(id))
						return it;
				}
				if (id.indexOf("/") >= 0) {
					if (StringUtil.indexOfIgnoreCase(code, id) >= 0)
						return it;
				}
			}
			String name = taxa.getTaxonName(it);
			if (!StringUtil.blank(name) && name.equalsIgnoreCase(id))
				return it;

		}
		return -1;
	}

}
