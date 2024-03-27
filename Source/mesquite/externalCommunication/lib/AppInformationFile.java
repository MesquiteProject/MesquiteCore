package mesquite.externalCommunication.lib;

import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.Element;

import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.StringUtil;
import mesquite.lib.XMLUtil;

public class AppInformationFile {
	String appName;
	String appVariant;
	String compiledAs;
	String path;
	String version;
	String URL;
	String license;
	String licenseURL;
	String citationURL;
	String appsFilePath;
	static final int infoFileVersion = 1;
	String appNameWithinAppsDirectory;

	
	public AppInformationFile(String appNameWithinAppsDirectory) {
		this.appNameWithinAppsDirectory = appNameWithinAppsDirectory;
	}
	/*.................................................................................................................*/
	public String getAppInfoFilePath(){
		if (MesquiteTrunk.isMacOSX())
			return appsFilePath + "Contents" + MesquiteFile.fileSeparator + "Resources" + MesquiteFile.fileSeparator + "appinfo.xml";
		else
			return appsFilePath + MesquiteFile.fileSeparator + "appinfo.xml";
	}

	/*.................................................................................................................*/
	public boolean processAppInfoFile(){
		appsFilePath = MesquiteFile.getPathWithSingleSeparatorAtEnd(MesquiteTrunk.appsDirectory) +appNameWithinAppsDirectory +MesquiteFile.fileSeparator;
		String infoFilePath = getAppInfoFilePath();
		String s = MesquiteFile.getFileContentsAsString(infoFilePath); 
		Document doc = XMLUtil.getDocumentFromString(s);
		if (doc==null) {
			MesquiteMessage.discreetNotifyUser("WARNING: properly formatted appInfo.xml file could not be found at " + appsFilePath);
			return false;
		}
		Element rootElement = doc.getRootElement();
		Element zephyrElement = rootElement.element("zephyr");
		if (zephyrElement != null) {
			String versionString = zephyrElement.attributeValue("version");
			int versionInXml = MesquiteInteger.fromString(versionString);
			if (versionInXml==infoFileVersion) {
				Element appInfoElement = zephyrElement.element("appinfo");
				Element element = appInfoElement.element("appname");
				if (element!=null)
					appName = element.getStringValue();
				element = appInfoElement.element("appvariant");
				if (element!=null)
					appVariant = element.getStringValue();
				element = appInfoElement.element("compiledas");
				if (element!=null)
					compiledAs = element.getStringValue();
				element = appInfoElement.element("path");
				if (element!=null)
					path = element.getStringValue();
				element = appInfoElement.element("version");
				if (element!=null)
					version = element.getStringValue();
				element = appInfoElement.element("URL");
				if (element!=null)
					URL = element.getStringValue();
				element = appInfoElement.element("licenseURL");
				if (element!=null)
					license = element.getStringValue();
				element = appInfoElement.element("license");
				if (element!=null)
					licenseURL = element.getStringValue();
				element = appInfoElement.element("citationURL");
				if (element!=null)
					citationURL = element.getStringValue();
			}

		}
		return true;
	}
	public String getAppName() {
		return appName;
	}
	public String getAppVariant() {
		return appVariant;
	}
	public String getCompiledAs() {
		return compiledAs;
	}
	public String getPath() {
		return path;
	}
	public String getFullPath() {
		return appsFilePath+path;
	}
	public String getVersion() {
		return version;
	}
	public String getURL() {
		return URL;
	}
	public String getLicense() {
		return license;
	}
	public String getLicenseURL() {
		return licenseURL;
	}
	public String getCitationURL() {
		return citationURL;
	}
	
}
