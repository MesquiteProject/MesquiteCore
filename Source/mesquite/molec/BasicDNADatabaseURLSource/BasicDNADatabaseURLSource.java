package mesquite.molec.BasicDNADatabaseURLSource;

import mesquite.molec.lib.DNADatabaseURLSource;

public class BasicDNADatabaseURLSource extends DNADatabaseURLSource {

	public String getURL(int urlType) {
		return null;
	}

	public String getName() {
		return "Basic DNA Database URL Source";
	}

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return false;
	}

}
