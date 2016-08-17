package mesquite.externalCommunication.lib;

import mesquite.lib.*;

public abstract class RESTCommunicator implements XMLPreferencesProcessor {
	protected static String username = "";
	protected static String password = ""; 
	protected String xmlPrefsString = null;
	protected String[] outputFilePaths; //local copies of files
	protected MesquiteModule ownerModule;

	public RESTCommunicator () {
	}
	
	public RESTCommunicator (MesquiteModule mb, String xmlPrefsString,String[] outputFilePaths) {
		if (xmlPrefsString != null)
			XMLUtil.readXMLPreferences(mb, this, xmlPrefsString);
		this.outputFilePaths = outputFilePaths;
		ownerModule = mb;
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setUsername " + ParseUtil.tokenize(username));
		return temp;
	}
	Parser parser = new Parser();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the username", "[username]", commandName, "setUsername")) {
			username = parser.getFirstToken(arguments);
		}
		return null;
	}	
	public void processSingleXMLPreference (String tag, String content) {
		processSingleXMLPreference(tag, null, content);

	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String flavor, String content) {
		if ("userName".equalsIgnoreCase(tag))
			username = StringUtil.cleanXMLEscapeCharacters(content);
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "username", username);  
		return buffer.toString();
	}
	/*.................................................................................................................*/
	public void forgetPassword() {
		password="";
	}

	/*.................................................................................................................*/
	protected boolean checkUsernamePassword(boolean tellUserAboutSystem){
		if (StringUtil.blank(username) || StringUtil.blank(password)){
			MesquiteBoolean answer = new MesquiteBoolean(false);
			MesquiteString usernameString = new MesquiteString();
			if (username!=null)
				usernameString.setValue(username);
			MesquiteString passwordString = new MesquiteString();
			if (password!=null)
				passwordString.setValue(password);
			String help = "You need an account on the CIPRes REST system to use this service.  To register, go to https://www.phylo.org/restusers/register.action";
			new UserNamePasswordDialog(ownerModule.containerOfModule(), "Sign in to CIPRes", help, "", "Username", "Password", answer, usernameString, passwordString);
			if (answer.getValue()){
				username=usernameString.getValue();
				password=passwordString.getValue();
			}
			ownerModule.storePreferences();
		}
		boolean success = StringUtil.notEmpty(username) && StringUtil.notEmpty(password);
		if (!success && tellUserAboutSystem) {
			MesquiteMessage.discreetNotifyUser("Use of the CIPRes service requires an account with CIPRes's REST service.  Go to https://www.phylo.org/restusers/register.action to register for an account");
		}
		return success;

	}

}
