package mesquite.externalCommunication.lib;

import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.Element;

import mesquite.lib.Listable;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.StringUtil;
import mesquite.lib.XMLUtil;

public class AppInformationFile implements Listable {
	String appName;
	String appVariant;
	String compiledAs;
	String path;
	String version;
	String URL;
	String license;
	String licenseURL;
	String citationURL;
	String citation;
	String otherProperties;
	String appsFilePath;
	boolean primary = true;
	static final int infoFileVersion = 1;
	String appNameWithinAppsDirectory;

	static String externalCommunicationXMLTag = "externalCommunication";


	public AppInformationFile(String appNameWithinAppsDirectory) {
		this.appNameWithinAppsDirectory = appNameWithinAppsDirectory;
	}
	
	public String toString(){
		return "AppInformationFile " + appName + " / " + compiledAs + " / " + path + " / " + version 
				+ " / " + appNameWithinAppsDirectory;
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
			if (!MesquiteFile.fileExists(infoFilePath))
				MesquiteMessage.discreetNotifyUser("WARNING: appInfo.xml file could not be found at " + appsFilePath);
			else if (StringUtil.blank(s))
				MesquiteMessage.discreetNotifyUser("WARNING: appInfo.xml file is empty, at " + appsFilePath);
			else {
				MesquiteMessage.discreetNotifyUser("WARNING: appInfo.xml file is improperly formatted, at " + appsFilePath);
				if (MesquiteTrunk.debugMode) {
					MesquiteMessage.println("Contents of appInfo.xml file: \n");
					MesquiteMessage.println(s);
				}
			}
			return false;
		}
		Element rootElement = doc.getRootElement();
		Element externalCommunicationElement = rootElement.element(externalCommunicationXMLTag);
		if (externalCommunicationElement != null) {
			String versionString = externalCommunicationElement.attributeValue("version");
			int versionInXml = MesquiteInteger.fromString(versionString);
			if (versionInXml==infoFileVersion) {
				Element appInfoElement = externalCommunicationElement.element("appinfo");
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
				element = appInfoElement.element("otherproperties");
				if (element!=null)
					otherProperties = element.getStringValue();
				element = appInfoElement.element("license");
				if (element!=null)
					licenseURL = element.getStringValue();
				element = appInfoElement.element("citationURL");
				if (element!=null)
					citationURL = element.getStringValue();
				element = appInfoElement.element("citation");
				if (element!=null)
					citation = element.getStringValue();
			}

		}
		return true;
	}
	public String getName() {
		String s = appName;
		if (!StringUtil.blank(appVariant))
			s+= " (" + appVariant + ")";
		if (!StringUtil.blank(version) | StringUtil.blank(compiledAs)){
			s += " (";
			if (!StringUtil.blank(version))
				s+= "v. " + version;
			if (!StringUtil.blank(compiledAs)){
				String arch = StringUtil.getLastItem(compiledAs, ".");
				if (arch.equalsIgnoreCase("univ"))
						arch = "x86 or ARM64";
				else if (arch.equalsIgnoreCase("aarch64"))
					arch = "ARM64";
				s+= " for " + arch;
			}
			s += ")";
		}
		return s;
	}
	public String getAppName() {
		return appName;
	}
	public String getAppNameWithinAppsDirectory() {
		return appNameWithinAppsDirectory;
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
	public String getOtherProperties() {
		return otherProperties;
	}

	//* if there is a choice, is this one primary?
	public boolean isPrimary() {
		return primary;
	}
	public void setPrimary(boolean primary) {
		this.primary = primary;
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
	public String getCitation() {
		return citation;
	}

}
