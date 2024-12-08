package mesquite.externalCommunication.lib;


public interface AppUser {
		
	public String getAppOfficialName() ;   //official name of app as stored in the appInfo.xml file
	
	public String getProgramName() ;   // name of program for user display
	
	public void setHasApp(boolean hasApp) ;   //stores that built-in app is present
	
	public void setUsingBuiltinApp(boolean usingBuiltinApp) ;   //stores that built-in app is being used

}
