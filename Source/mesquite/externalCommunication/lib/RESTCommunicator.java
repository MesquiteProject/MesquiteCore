package mesquite.externalCommunication.lib;

import mesquite.lib.*;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.*;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;



public abstract class RESTCommunicator extends RemoteCommunicator {

	public RESTCommunicator (MesquiteModule mb, String xmlPrefsString,String[] outputFilePaths) {
//		if (xmlPrefsString != null)
//			XMLUtil.readXMLPreferences(mb, this, xmlPrefsString);
		this.outputFilePaths = outputFilePaths;
		ownerModule = mb;
	}

	/*.................................................................................................................*/
	public abstract String getRESTURL();

	/*.................................................................................................................*/
	public String getSystemTypeName() {
		return " REST";
	}


}
