package mesquite.externalCommunication.lib;

import mesquite.lib.*;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.*;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;



public abstract class RESTCommunicator implements XMLPreferencesProcessor {
	protected static String username = "";
	protected static String password = ""; 
	protected String xmlPrefsString = null;
	protected String[] outputFilePaths; //local copies of files
	protected MesquiteModule ownerModule;
	protected boolean verbose = MesquiteTrunk.debugMode;
	protected boolean aborted = false;

	protected OutputFileProcessor outputFileProcessor; // for reconnection
	protected ShellScriptWatcher watcher; // for reconnection


	public RESTCommunicator () {
	}

	public RESTCommunicator (MesquiteModule mb, String xmlPrefsString,String[] outputFilePaths) {
		if (xmlPrefsString != null)
			XMLUtil.readXMLPreferences(mb, this, xmlPrefsString);
		this.outputFilePaths = outputFilePaths;
		ownerModule = mb;
	}
	
	
	public String getAPITestUserName(){
		return "";
	}
	public String getAPITestPassword(){
		return "";
	}
	public boolean useAPITestUser() {
		return false;
	}
	public boolean isAborted() {
		return aborted;
	}

	public void setAborted(boolean aborted) {
		this.aborted = aborted;
	}

	/*.................................................................................................................*/
	public String getUserName(){
		if (useAPITestUser()) {
			return getAPITestUserName();
		} else 
			return username;
	}
	/*.................................................................................................................*/
	public void setUserName(String newName){
		if (!useAPITestUser()) 
			username=newName;
	}
	/*.................................................................................................................*/
	public String getPassword(){
		if (useAPITestUser()) {
			return getAPITestPassword();
		} else 
			return password;
	}
	/*.................................................................................................................*/
	public void setPassword(String newPassword){
		if (!useAPITestUser()) 
			password=newPassword;
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
	public abstract String getBaseURL();
	/*.................................................................................................................*/
	public abstract String getAPIURL();
	/*.................................................................................................................*/
	public abstract String getRESTURL();
	/*.................................................................................................................*/
	public abstract String getRegistrationURL();
	/*.................................................................................................................*/
	public abstract String getSystemName();
	/*.................................................................................................................*/
	protected boolean checkUsernamePassword(boolean tellUserAboutSystem){
		if (StringUtil.blank(getUserName()) || StringUtil.blank(password)){
			MesquiteBoolean answer = new MesquiteBoolean(false);
			MesquiteString usernameString = new MesquiteString();
			if (getUserName()!=null)
				usernameString.setValue(getUserName());
			MesquiteString passwordString = new MesquiteString();
			if (getPassword()!=null)
				passwordString.setValue(getPassword());
			String help = "You need an account on the "+getSystemName()+" REST system to use this service.  To register, go to " + getRegistrationURL();
			String registrationHint = "Touch on the web link icon on the left to register for this service.";
			new UserNamePasswordDialog(ownerModule.containerOfModule(), "Sign in to "+getSystemName(), help, getRegistrationURL(), registrationHint, "Username", "Password", answer, usernameString, passwordString);
			if (answer.getValue()){
				setUserName(usernameString.getValue());
				setPassword(passwordString.getValue());
			}
			ownerModule.storePreferences();
		}
		boolean success = StringUtil.notEmpty(getUserName()) && StringUtil.notEmpty(getPassword());
		if (!success && tellUserAboutSystem) {
			MesquiteMessage.discreetNotifyUser("Use of the "+getSystemName()+" service requires an account with the service.  Go to "+ getRegistrationURL()+" to register for an account");
		}
		return success;

	}

	/*.................................................................................................................*/
	public HttpClient getHttpClient(){
		// from http://www.artima.com/forums/flat.jsp?forum=121&thread=357685
		CredentialsProvider provider = new BasicCredentialsProvider();
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
		provider.setCredentials(AuthScope.ANY, credentials);
		return HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
	}


}
