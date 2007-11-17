package mesquite.lib;

import mesquite.lib.*;

public class PhoneHomeRecord implements Listable {
	
	private int lastVersionUsed = 0;
	private int lastNotice = 0;
	private int lastNoticeForMyVersion = 0;
	private int lastVersionNoticed = 0;
	private int lastNewerVersionReported = 0;
	String message = null;
	String moduleName;

	
	public PhoneHomeRecord(String moduleName,int lastVersionUsed, int lastNotice, int lastNoticeForMyVersion, int lastVersionNoticed, int lastNewerVersionReported) {
		this.lastVersionUsed = lastVersionUsed;
		this.lastNotice = lastNotice;
		this.lastNoticeForMyVersion = lastNoticeForMyVersion;
		this.lastVersionNoticed = lastVersionNoticed;
		this.lastNewerVersionReported = lastNewerVersionReported;
		this.moduleName = moduleName;
	}

	public PhoneHomeRecord(String moduleShortName) {
		this.moduleName = moduleShortName;
	}

	public void setCurrentValues(MesquiteModuleInfo mmi) {
		if (mmi!=null) {
			if (mmi.getIsPackageIntro()) {
				lastVersionUsed = mmi.getPackageVersionInt();
			}
			else 
				lastVersionUsed = mmi.getVersionInt();
		}
	}

	public int getLastNotice() {
		return lastNotice;
	}


	public void setLastNotice(int lastNotice) {
		this.lastNotice = lastNotice;
	}


	public int getLastNoticeForMyVersion() {
		return lastNoticeForMyVersion;
	}


	public void setLastNoticeForMyVersion(int lastNoticeForMyVersion) {
		this.lastNoticeForMyVersion = lastNoticeForMyVersion;
	}


	public int getLastVersionNoticed() {
		return lastVersionNoticed;
	}


	public void setLastVersionNoticed(int lastVersionNoticed) {
		this.lastVersionNoticed = lastVersionNoticed;
	}


	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	public String getModuleName() {
		return moduleName;
	}


	public void setModuleName(String moduleShortName) {
		this.moduleName = moduleShortName;
	}

	public String getName(){
		return getModuleName();
	}

	public int getLastVersionUsed() {
		return lastVersionUsed;
	}

	public void setLastVersionUsed(int lastVersionUsed) {
		this.lastVersionUsed = lastVersionUsed;
	}

	public int getLastNewerVersionReported() {
		return lastNewerVersionReported;
	}

	public void setLastNewerVersionReported(int lastNewerVersionReported) {
		this.lastNewerVersionReported = lastNewerVersionReported;
	}



}
