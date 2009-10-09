/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison. 
Version 2.71, September 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.minimal.Installer;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.duties.*;
import java.util.zip.*;
import java.util.*;
import java.io.*;

import org.dom4j.Element;

/*....+++++++++++++++Installer.  See also PhoneHomeUtil  ++++++++++++++++++++++++++..........*/
/*
 * The update/installation system relies on two sources of information: 
 * (1) the notices of updates compiled by the Phone Home system, using getHomePhoneNumber() of modules to know where to call.  Packages can list these phone numbers
 * for their own updating, but in addition we will maintain a central updates.xml in Mesquite so that newly released packages can be announced there.
 * 
 * (2) the local receipts for packages installed.  Most of these receipts may be acquired through the online
 * installation process, but some may be acquired within packages installed by hand.  The file receipts.xml is compiled by Mesquite and stored in Mesquite_Folder/extras (it must be
 * there as opposed to Mesquite_Support_Files because it belongs to the installation, not the user.
 * 
 * The main global variable are stored as statics in PhoneHomeUtil; the module managing the system is mesquite.minimal..Installer.
 * 
 * Update notices:  An update notice has messagetype "update".  See updates.xml in this package for examples.  A single update may make reference to multiple installation events.
 * Key tags are:
 * -- the standard tags of notices from home (forVersion, noticeNumber, messageType=update, explanation
 * -- identity: name that will uniquely identify your package; don't include version number , e.g. "PDAP"
 * -- uniqueLocation: a location that should be unique to this package, to help mesquite know if a previous version is installed even if receipt is missing
 * 			Must be within Mesquite_Folder, and relative to it.
 * -- updateVersion: the version of the update, to determine if this or later update is already installed
 * -- packageName: Human readable name of package or update
 * -- explanation: note (if html, as CDATA) to display in notices dialog
 * -- java: java version required
 * -- requires:  human readable name of package this one needs to have already been installed.  MUST be paired with a requiredPath tag
 * -- requiredPath: file or directory that is a litmus test as to whether the package on which this depends is already installed.  Mesquite merely check to see if this file or directory exists.  
 * 			Must be within Mesquite_Folder, and relative to it.
 * -- install:  each install element indicates one item to be installed.  There may be multiple (e.g. one for the package and one for a jar it needs)
 *		within install:
 *			-- location: the directory into which this item is to be put. Must be within Mesquite_Folder, and relative to it.
 *			-- file: the file or directory that will be created within the above directory.  Used in part to retain prior copy in case something goes wrong in installation
 *			-- url: the url from which to download the item.
 *			-- treatment: either asis or unzip.  Note: zip files to be downloaded and unzipped must have been prepared by Mesquite itself as noted below
 *			-- updateVersion: version of this particular item.  Not used, but might be in future
 *			-- forOS: OS for which this item applies.  Accepts match if value occurs within "os.name" java System propery.  
 *						There can be multiple forOS elements.  If none, it is assumed to apply to all OS's.
 *					-- os: os name, e.g. "Windows" or "Mac OS X"
 *					-- osVersion: optional.  If present must be an exact match.  Thus if you want an item to apply to Mac OS X 10.4 or 10.5, you must include two forOS elements
 *	The above tags apply to all downloads.  In addition, if a download is an update only, e.g. a patch, then the following tag should be included:
 * -- updateOnly, with values "false" (default), "true" a non critical update, and "critical" a critical update, e.g. a bug fix.
 * 			If updateOnly is true or critical, then Mesquite will give notification of it only if the original package being updated appears to be installed.  This is judged by receipts or by the uniqueLocation tag
 * 			If updateOnly is true, then Mesquite will give notification only once.
 * 			If updateOnly is critical, then Mesquite will continue to give notification on each startup as long as original package being updated remains installed and critical update is not yet installed
 * 
 * Zip files must be prepared properly such that when unzipped they yield directly the directory stucture desired.  Zip files made by ZipIt on OS X do not work.  
 * Installer can perform the zipping to ensure the zip files will work.  To enable this, select Debug Mode in File>Defaults and restart Mesquite.  
 * Then, select File>Zip Folder or Zip File.
 * 
 * 
 * todo:
 * GENERAL
 * 
 * INSTALL PIECE
 * -- FUTURE (or in control of modules after they start): permit writing outside Mesquite
 * -- FUTURE (or in control of modules after they start): permit scripts to run, e.g. to install outside of Mesquite_Folder
 * 
 * Ã-- Same protection in menu formation re java/dependencies
 * Ã-- have dependency tag that indicates dependencies
 * Ã-- make directories needed
 * Ã-- permit OS-specific installs
 * Ã-- what if dir not writable???
 * Ã-- have require tag that indicates minimal Java version
 * Ã-- receipt should contain more info
 * Ã-- add tag "critical" to updates; these would get installed only if receipt and folders indicate installation already had been done
 * Ã-- distinguish install and update, i.e. some do not give messages unless you already have older pakcage installed
 * Ã-- check to see that receipts point to files that still exist; if not, then treat receipt as if it hadn't existed
 * */
public class Installer extends MesquiteInit {
	public String getName() {
		return "Installer";
	}
	public String getExplanation() {
		return "Installs packages." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 260;  
	}
	/*.................................................................................................................*/
	/** returns the URL of the notices file for this module so that it can phone home and check for messages */
	public String  getHomePhoneNumber(){ 
		if (!MesquiteTrunk.mesquiteTrunk.isPrerelease() && !MesquiteTrunk.debugMode)
			return "http://mesquiteproject.org/mesquite/updates/updates.xml";
		else
			return "http://mesquiteproject.org/mesquite/prereleasenotices/updates.xml";   
		//NOTE mesquite.minimal.Defaults.Defaults has own home phone number for server to record build numbers in use
	}
	/*.................................................................................................................*/

	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		if (MesquiteTrunk.debugMode){
			mesquiteTrunk.addMenuItem(MesquiteTrunk.fileMenu, "Zip Folder for Installer", makeCommand("zipDir", this));
			mesquiteTrunk.addMenuItem(MesquiteTrunk.fileMenu, "Zip File for Installer", makeCommand("zipFile", this));
		}
		readReceipts();
		return true;
	}
	/*.................................................................................................................*/
	void readReceipts(){
		readReceipts(getInstallationSettingsPath() + "receipts.xml");
		File mesquite = new File(getRootPath()+ "mesquite"); 
		String[] folders = mesquite.list(); 
		for (int i=0; i<folders.length; i++){
			readReceipts(getRootPath() + "mesquite/" + folders[i] + "/receipts.xml");
		}

	}
	void readReceipts(String path){
		String receiptsContents = null;
		try{
			receiptsContents = MesquiteFile.getFileContentsAsString(path, 2000000, 100, false);
		} catch (Exception e) {
			return;
		}
		if (StringUtil.blank(receiptsContents))
			return;
		Element root = XMLUtil.getRootXMLElementFromString("mesquite",receiptsContents);
		if (root==null)
			return;
		List receipts = root.elements("installationReceipt");
		boolean stillInstalled = true;
		for (Iterator iter = receipts.iterator(); iter.hasNext();) {   // this is going through all of the notices
			Element messageElement = (Element) iter.next();
			ListableVector v = new ListableVector();
			v.addElement(new MesquiteString("identity", messageElement.elementText("identity")), false);
			v.addElement(new MesquiteString("updateVersion", messageElement.elementText("updateVersion")), false);
			List locs = messageElement.elements("location");
			for (Iterator locsIter = locs.iterator();  locsIter.hasNext();) {   // this is going through all of the notices
				Element locElement = (Element) locsIter.next();
				String p = locElement.elementText("path");
				if (!MesquiteFile.fileOrDirectoryExists(MesquiteTrunk.getRootPath() + p))
					stillInstalled = false;
				v.addElement(new MesquiteString("location", locElement.elementText("path")), false);

			}
			v.addElement(new MesquiteString("explanation", messageElement.elementText("explanation")), false);
			v.addElement(new MesquiteString("packageName", messageElement.elementText("packageName")), false);
			v.addElement(new MesquiteString("uniqueLocation", messageElement.elementText("uniqueLocation")), false);
			Element elup = messageElement.element("updateXML");
			if (elup != null)
				v.addElement(new MesquiteString("updateXML",  clean(XMLUtil.getElementAsXMLString(elup, "UTF-8", true)) ), false);
			//	v.addElement(new MesquiteString("updateXML", "<updateXML>\n" + XMLUtil.getElementAsXMLString(elup, "UTF-8", true) + "</updateXML>" ), false);

			if (!PhoneHomeUtil.alreadyInReceipts(v)){
				if (stillInstalled)
					PhoneHomeUtil.installedReceipts.addElement(v);
			}
		}
		writeReceipts();  //rewrite into central
	}

	String clean(String ux){
		ux = StringUtil.replace(ux, "\n", null);
		ux = StringUtil.replace(ux, "\r", null);
		ux = StringUtil.replace(ux, "\t", null);
		ux = StringUtil.stripStuttered(ux, "  ");
		return ux;
	}
	/*.................................................................................................................*/
	void writeReceipts(){

		StringBuffer buff = new StringBuffer();
		buff.append("<?xml version=\"1.0\"?>\n<mesquite>\n");
		for (int i=0; i< PhoneHomeUtil.installedReceipts.size(); i++){
			ListableVector rec = (ListableVector)PhoneHomeUtil.installedReceipts.elementAt(i);
			buff.append("\t<installationReceipt>\n");
			buff.append("\t\t<identity>" + ((MesquiteString)rec.getElement("identity")).getValue() + "</identity>\n");
			buff.append("\t\t<updateVersion>" + ((MesquiteString)rec.getElement("updateVersion")).getValue() + "</updateVersion>\n");
			List locs = rec.getElements("location");
			for (Iterator iter = locs.iterator();  iter.hasNext();) {   // this is going through all of the notices
				MesquiteString loc = (MesquiteString) iter.next();
				buff.append("\t\t<location><path>" + loc.getValue() + "</path></location>\n");

			}

			buff.append("\t\t<explanation><![CDATA[" + ((MesquiteString)rec.getElement("explanation")).getValue() + "]]></explanation>\n");
			buff.append("\t\t<packageName>" + ((MesquiteString)rec.getElement("packageName")).getValue() + "</packageName>\n");
			buff.append("\t\t<uniqueLocation>" + ((MesquiteString)rec.getElement("uniqueLocation")).getValue() + "</uniqueLocation>\n");
			MesquiteString ux = (MesquiteString)rec.getElement("updateXML");
			if (ux != null){
				buff.append("\t\t" + clean(ux.getValue()) + "\n");
			}
			buff.append("\t</installationReceipt>\n");
		}
		buff.append("\n</mesquite>");
		MesquiteFile.createDirectory(getInstallationSettingsPath() );
		MesquiteFile.putFileContents(getInstallationSettingsPath() + "receipts.xml", buff.toString(), true);
	}

	/*.................................................................................................................*/
	void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int len;
		while((len = in.read(buffer)) >= 0)
			out.write(buffer, 0, len);
		in.close();
		out.close();
	}

	/*.................................................................................................................*/
	boolean zipDirectory(String sourceBase, String source, String destination){
		try { 
			ZipOutputStream zipOutput = new ZipOutputStream(new FileOutputStream(destination)); 
			String p = source;
			String subst = p.substring(sourceBase.length(), p.length());
			if (!subst.endsWith("/"))
				subst += "/";
			ZipEntry zentry = new ZipEntry(subst); 
			zipOutput.putNextEntry(zentry); 
			zipDir(sourceBase, source, zipOutput); 
			zipOutput.close(); 
			return true;
		} 
		catch(Exception e) { 
			MesquiteMessage.warnProgrammer("ZIP FAILED");
		} 
		return false;
	}
	public void zipDir(String sourceBase, String directoryPath, ZipOutputStream zipOutput) { 
		File zipDirec;
		try { 
			CommandRecord.tick("Zipping " + directoryPath);
			logln("Zipping " + directoryPath);

			zipDirec = new File(directoryPath); 

			String[] files = zipDirec.list(); 
			byte[] buff = new byte[2156]; 
			int numbyes = 0; 

			for(int i=0; i<files.length; i++)  { 
				if (!files[i].startsWith(".")){
					File f = new File(zipDirec, files[i]); 
					if(f.isDirectory())  { 
						String p = f.getPath();
						String subst = p.substring(sourceBase.length(), p.length());
						if (!subst.endsWith("/"))
							subst += "/";
						ZipEntry zEntry = new ZipEntry(subst); 
						zipOutput.putNextEntry(zEntry); 
						String filePath = f.getPath(); 
						zipDir( sourceBase, filePath, zipOutput); 
						continue; 
					} 
					FileInputStream instream = new FileInputStream(f); 
					String p = f.getPath();
					String subst = p.substring(sourceBase.length(), p.length());
					if (!subst.endsWith("/") && f.isDirectory())
						subst += "/";
					ZipEntry zentry = new ZipEntry(subst); 
					zipOutput.putNextEntry(zentry); 
					while((numbyes = instream.read(buff)) != -1) { 
						zipOutput.write(buff, 0, numbyes); 
					} 
					instream.close(); 
				}
			} 
		} 
		catch(Exception e) { 
		} 
	}
	boolean unzip(String loc, String file){
		ZipFile zipFile;
		Enumeration entries;
		try {
			zipFile = new ZipFile(loc + file);

			entries = zipFile.entries();

			while(entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry)entries.nextElement();
				String name = entry.getName();
				if (!name.startsWith(".")){
					if(entry.isDirectory()) {					
						(new File(loc + entry.getName())).mkdir();
						continue;
					}
					copy(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(loc +entry.getName())));
				}
			}

			zipFile.close();
			return true;
		} 
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return false;
	}

	boolean applicableOS(Element installElement){
		List osList = installElement.elements("forOS");
		if (osList == null)
			return true;
		if (osList.size() == 0)
			return true;
		for (Iterator i = osList.iterator(); i.hasNext();) {   // this is going through all of the notices
			Element osElement = (Element) i.next();
			String os = osElement.elementText("os");
			if (os == null)
				return true;
			String osVersion = osElement.elementText("osVersion");
			String osArch = osElement.elementText("osArch");
			boolean osArchMatches =(StringUtil.blank(osArch)|| System.getProperty("os.arch").indexOf(os)>=0);
			boolean osMatches =(StringUtil.blank(os)|| System.getProperty("os.name").indexOf(os)>=0);
			boolean osVersionMatches =(StringUtil.blank(osVersion)|| System.getProperty("os.version").startsWith(osVersion));
			if (osMatches && osVersionMatches && osArchMatches)
				return true;

		}
		return false;
	}
	/*.................................................................................................................*/
	public boolean executeScriptString(String script, boolean precedeWithCDToMesquiteFolder){
		MesquiteBoolean b = new MesquiteBoolean();
		String scriptPath  = createSupportDirectory(b) + MesquiteFile.fileSeparator + "script";
		if (precedeWithCDToMesquiteFolder)
			script = ShellScriptUtil.getChangeDirectoryCommand(getRootPath()) + script;
		MesquiteFile.putFileContents(scriptPath, script, true);
		return ShellScriptUtil.executeAndWaitForShell(scriptPath, "installScript");
	}
	boolean install(Element installElement, ListableVector receipt){
		if (!applicableOS(installElement))
			return true;  //return true because not considered failure if inapplicable OS
		String url = installElement.elementText("url");
		String pathInMesquiteFolder = installElement.elementText("location");
		String fileName = installElement.elementText("file");
		int version = MesquiteInteger.fromString(installElement.elementText("updateVersion"));  
		String treatment = installElement.elementText("treatment");
		String execute = installElement.elementText("execute");
		String executeInMesquiteFolder = installElement.elementText("executeInMesquiteFolder");
		String downloadAs = "installerDownload";
		if (treatment == null || treatment.equalsIgnoreCase("asis"))
			downloadAs = fileName;
		String prevPackagePath = getRootPath() + pathInMesquiteFolder + "/" + fileName;
		String tempPackagePath = getRootPath() + pathInMesquiteFolder + "/"  + fileName+ "PREVIOUSVERSION";
		File prevPackage = new File(prevPackagePath);
		boolean hadExisted = prevPackage.exists();
		if (false && hadExisted){
			String prevVString = MesquiteFile.getFileContentsAsString(getRootPath() +  pathInMesquiteFolder + "/" + fileName+ "/version.txt");
			int prevVersion  = MesquiteInteger.fromString(prevVString);
			if (false && prevVersion >= version){
				if (!AlertDialog.query(containerOfModule(), "Replace?", "The version of the package " + fileName + " installed (" + prevVersion + ") is the same as or newer than the one to be downloaded (" + version + ") .  Do you want to replace it with the one to be downloaded?")){
					return false;
				}
			}
		}
		if (url != null){
			logln("Downloading installation file from " + url);
			if (!MesquiteFile.fileOrDirectoryExists(getRootPath() + pathInMesquiteFolder)){
				MesquiteFile.createDirectory(getRootPath() + pathInMesquiteFolder);
			}
			if (hadExisted){
				logln("Renaming old version of " + fileName + " to " + fileName + "PREVIOUSVERSION");
				MesquiteFile.rename(prevPackagePath, tempPackagePath);
			}
			logln("Downloading installation file to " + getRootPath() + pathInMesquiteFolder+ "/" + downloadAs);
			if (MesquiteFile.downloadURLContents(url, getRootPath() + pathInMesquiteFolder + "/" + downloadAs, true)){
				boolean fileReady = true;
				if (treatment != null && treatment.equalsIgnoreCase("unzip")){
					logln("Unzipping installation file");
					fileReady = unzip(getRootPath() + pathInMesquiteFolder + "/", downloadAs);
					if (fileReady)
						MesquiteFile.deleteFile(getRootPath() + pathInMesquiteFolder + "/" + downloadAs);
				}
				if (fileReady){
					logln("Installation of " + fileName + " was successful.");
					receipt.addElement(new MesquiteString("location", pathInMesquiteFolder + "/" + fileName), false);
				}
				else if (hadExisted){
					logln("Installation unsuccessful; attempting to recover old version of " + fileName);
					MesquiteFile.rename(tempPackagePath, prevPackagePath);
					return false;
				}
			}	
			else {
				MesquiteFile.deleteFile(getRootPath() + pathInMesquiteFolder + "/" + downloadAs);
				return false;
			}
		}
		if (execute != null){
			String shortEx = execute;
			if (execute.length() > 500)
				shortEx = execute.substring(0, 500);
			if (AlertDialog.query(containerOfModule(), "Execute Script?", "The installer has downloaded the following script to execute.  Is it OK to execute it?\n"+ shortEx)){
				logln("Executing script");
				if (!executeScriptString(execute, false)){
					logln("Script execution unsuccessful");
					return false;
				}
			}
		}
		if (executeInMesquiteFolder != null){
			String shortEx = executeInMesquiteFolder;
			if (executeInMesquiteFolder.length() > 500)
				shortEx = executeInMesquiteFolder.substring(0, 500);
			shortEx = "<first, change directories into Mesquite_Folder>\n" + shortEx;
			if (AlertDialog.query(containerOfModule(), "Execute Script?", "The installer has downloaded the following script to execute.  Is it OK to execute it?\n"+ shortEx)){
				logln("Executing script");
				if (!executeScriptString(executeInMesquiteFolder, true)){
					logln("Script execution unsuccessful");
					return false;
				}
			}
		}
		return true;
	}
	void cleanUp(Element installElement){
		String pathInMesquiteFolder = installElement.elementText("location");
		String fileName = installElement.elementText("file");
		File tempPackage = new File(getRootPath() + pathInMesquiteFolder + "/"  + fileName+ "PREVIOUSVERSION");
		boolean hadExisted = tempPackage.exists();
		if (hadExisted){
			logln("Deleting old version of " + fileName);
			if (tempPackage.isDirectory())
				MesquiteFile.deleteDirectory(getRootPath() + pathInMesquiteFolder + "/" + fileName+ "PREVIOUSVERSION");
			else 
				MesquiteFile.deleteFile(getRootPath() + pathInMesquiteFolder + "/" + fileName+ "PREVIOUSVERSION");
		}
	}
	void reverse(Element installElement){
		String pathInMesquiteFolder = installElement.elementText("location");
		String fileName = installElement.elementText("file");
		String prevPackagePath = getRootPath() + pathInMesquiteFolder + "/" + fileName;
		String tempPackagePath = getRootPath() + pathInMesquiteFolder + "/"  + fileName+ "PREVIOUSVERSION";
		File prevPackage = new File(prevPackagePath);

		File tempPackage = new File(tempPackagePath);
		boolean hadExisted = tempPackage.exists();
		if (hadExisted){
			logln("Installation unsuccessful; attempting to recover old version of " + fileName);
			if (prevPackage.isDirectory())
				MesquiteFile.deleteDirectory(getRootPath() + pathInMesquiteFolder + "/" + fileName);
			else 
				MesquiteFile.deleteFile(getRootPath() + pathInMesquiteFolder + "/" + fileName);
			MesquiteFile.rename(tempPackagePath, prevPackagePath);
		}
	}


	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "phoneHomeLinkTouched", null, commandName, "phoneHomeLinkTouched") || checker.compare(this.getClass(), "phoneHomeLinkTouchedAdHoc", null, commandName, "phoneHomeLinkTouchedAdHoc")) {
			boolean adHoc =  (commandName.equalsIgnoreCase("phoneHomeLinkTouchedAdHoc"));
			if (!MesquiteFile.canWrite(getRootPath())){
				discreetAlert("Sorry, you do not have the permissions required to modify the Mesquite_Folder.  Installation cannot proceed.");
				return null;
			}
			String source = parser.getFirstToken(arguments);
			ListableVector updateRecord = null;
			if (adHoc)
				updateRecord = PhoneHomeUtil.adHocRecord;
			else
				updateRecord = PhoneHomeUtil.getUpdateRecord(source.substring(source.indexOf(":")+1, source.length()));
			if (updateRecord == null)
				return null;
			MesquiteString identity = (MesquiteString)updateRecord.getElement("identity");
			MesquiteString versionString = (MesquiteString)updateRecord.getElement("updateVersion");
			MesquiteString packageName = (MesquiteString)updateRecord.getElement("packageName");
			MesquiteString explanation = (MesquiteString)updateRecord.getElement("explanation");
			MesquiteString uniqueLocation = (MesquiteString)updateRecord.getElement("uniqueLocation");
			MesquiteString updateXML = (MesquiteString)updateRecord.getElement("updateXML");
			ListableVector receipt = new ListableVector();
			receipt.addElement(identity, false);
			receipt.addElement(versionString, false);
			receipt.addElement(explanation, false);
			receipt.addElement(packageName, false);
			receipt.addElement(uniqueLocation, false);
			if (updateXML != null)
				receipt.addElement(updateXML, false);
			boolean asked = false;
			if (!adHoc && PhoneHomeUtil.alreadyInReceipts(identity.getValue(), versionString.getValue())){
				if (!MesquiteThread.isScripting() && !AlertDialog.query(containerOfModule(), "Install?", "The package " + packageName + " of the same version already appears to be installed.  Do you want to reinstall?"))
					return null;
				else asked = true;
			}
			if ((adHoc || !asked) && !MesquiteThread.isScripting() && !AlertDialog.query(containerOfModule(), "Install?", "You have requested to install " + packageName + ".  Do you want to install it now?"))
				return null;
			parser.setString(versionString.getValue());
			int version = MesquiteInteger.fromString(parser);
			ObjectContainer io = (ObjectContainer)updateRecord.getElement("install");
			List installation = (List)io.getObject();
			int count = 0;
			boolean failed = false;
			
			//hide all project windows
			Enumeration e = MesquiteTrunk.mesquiteTrunk.windowVector.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				MesquiteWindow mw = (MesquiteWindow)obj;
				if (!(mw instanceof SystemWindow))
					mw.getParentFrame().setVisible(false);
			}
			Projects projects = MesquiteTrunk.mesquiteTrunk.getProjectList();
			for (int ip = 0; ip< projects.getNumProjects(); ip++){
				MesquiteProject proj = projects.getProject(ip);
				
			}
			for (Iterator iter = installation.iterator(); !failed && iter.hasNext();) {   // this is going through all of the notices
				Element installElement = (Element) iter.next();
				if (install(installElement, receipt))
					count++;
				else
					failed = true;
			}
			int i = 0;
			if (failed)
				discreetAlert("Installation was UNSUCCESSFUL; Mesquite will attempt to restore any previous versions");
			for (Iterator iter = installation.iterator(); iter.hasNext();) {   // this is going through all of the notices
				Element installElement = (Element) iter.next();
				if (failed){
					if (i<=count)
						reverse(installElement);
				}
				else 
					cleanUp(installElement);
				i++;
			}
			//show all project windows
			Enumeration e2 = MesquiteTrunk.mesquiteTrunk.windowVector.elements();
			while (e2.hasMoreElements()) {
				Object obj = e2.nextElement();
				MesquiteWindow mw = (MesquiteWindow)obj;
				if (!(mw instanceof SystemWindow))
					mw.getParentFrame().setVisible(true);
			}
			
			
			if (!failed){
				//Here mesquite should store a receipt that this update was installed.  Should store package reference and version reference.  
				if (!PhoneHomeUtil.alreadyInReceipts(receipt))
					PhoneHomeUtil.installedReceipts.addElement(receipt);
				writeReceipts();
				PhoneHomeUtil.refreshUpdateMenuItems();
				discreetAlert("Installation was successful.  You will need to restart Mesquite to make use of the new installation.");
				resetAllMenuBars();
			}
		}
		else if (checker.compare(this.getClass(), "zip directory", null, commandName, "zipDir")) {
			String source = MesquiteFile.chooseDirectory("Directory to Zip", null);
			String destination = MesquiteFile.saveFileAsDialog("Save zip file", null);
			if (zipDirectory(MesquiteFile.getDirectoryPathFromFilePath(source), source, destination))
				alert("Zip successful");
			else
				alert("Zip unsuccessful!!!!");
		}
		else if (checker.compare(this.getClass(), "zip file", null, commandName, "zipFile")) {
			String source = MesquiteFile.openFileDialog("File to Zip",  null,  null);
			String destination = MesquiteFile.saveFileAsDialog("Save zip file", null);
			if (zipDirectory(MesquiteFile.getDirectoryPathFromFilePath(source), source, destination))
				alert("Zip successful");
			else
				alert("Zip unsuccessful!!!!");
		}
		else 		if (checker.compare(this.getClass(), "Unzips", null, commandName, "unzip")) {
			//unzip();
		}

		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive() {
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}

}

