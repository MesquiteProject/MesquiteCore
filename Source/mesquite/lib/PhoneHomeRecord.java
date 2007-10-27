package mesquite.lib;

import mesquite.lib.*;

public class PhoneHomeRecord implements Listable {
	
	private int lastNotice = 0;
	private int lastNoticeForMyVersion = 0;
	private int lastVersionNoticed = 0;
	String message = null;
	String moduleShortName;

	
	public PhoneHomeRecord(String moduleShortName, int lastNotice, int lastNoticeForMyVersion, int lastVersionNoticed) {
		this.lastNotice = lastNotice;
		this.lastNoticeForMyVersion = lastNoticeForMyVersion;
		this.lastVersionNoticed = lastVersionNoticed;
		this.moduleShortName = moduleShortName;
	}

	public PhoneHomeRecord(String moduleShortName) {
		this.moduleShortName = moduleShortName;
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


	public String getModuleShortName() {
		return moduleShortName;
	}


	public void setModuleShortName(String moduleShortName) {
		this.moduleShortName = moduleShortName;
	}

	public String getName(){
		return getModuleShortName();
	}


}
