/* Mesquite source code.  Copyright 1997-2008 W. Maddison and D. Maddison. 
Version 2.5, June 2008.
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
 * installation process, but some may be acquired within packages installed by hand.  The file receipts.xml is compiled by Mesquite and stored in Mesquite_Folder (it must be
 * there as opposed to Mesquite_Support_Files because it belongs to the installation, not the user.
 * 
 * The main global variable are stored as statics in PhoneHomeUtil; the module managing the system is mesquite.minimal..Installer.
 * 
 * Update notices:  An update notice has messagetype "update".  See updates.xml in this package for examples.  A single update may make reference to multiple installation events.
 * Key tags are:
 * -- location: the folder (in Mesquite_Folder) within which the new file or folder is to be installed.
 * -- file: the folder or file to be installed.  This may be a package folder, e.g. "assoc", or a file, e.g. "rt.jar".  Mesquite uses the name of this file or folder for a hint as to whether
 * a previous version is installed.  Also, this file or folder is renamed temporarily during installation to be able to recover it if the installation fails.
 * -- url: the file to be downloaded for installation, e.g. a zip file (if a package with directory structure is to be installed) or a jar file.
 * -- treatment: asis if the file is simply downloaded in place; unzip if the file is to be unzipped
 * 
 * Zip files must be prepared properly such that when unzipped they yield directly the directory stucture desired.  Zip files made by ZipIt on OS X do not work.  Installer can perform
 * the zipping to ensure the zip files will work.  To enable this, select Debug Mode in File>Defaults and restart Mesquite.  Then, select File>Zip.
 * 
 * 
 * todo:
 * -- check to see that receipts point to files that still exist; if not, then treat receipt as if it hadn't existed
 * -- check all referred files to see that installation is present
 * -- make PhoneHomeUtil ignore updates already included in html
 * -- permit writing outside Mesquite
 * -- permit OS-specific installs
 * -- add tag "critical" to updates; these would get installed only if receipt and folders indicate installation already had been done
 * */
public class Installer extends MesquiteInit {
	public String getName() {
		return "Installer";
	}
	public String getExplanation() {
		return "Installs packages." ;
	}
	/*.................................................................................................................*/
	/** returns the URL of the notices file for this module so that it can phone home and check for messages */
	public String  getHomePhoneNumber(){ 
		return "http://mesquiteproject.org/mesquite/updates/updates.xmlDELETETOTURNON";
	}
	/*.................................................................................................................*/

	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		if (MesquiteTrunk.debugMode)
			mesquiteTrunk.addMenuItem(MesquiteTrunk.fileMenu, "Zip Folder for Installer", makeCommand("zip", this));
		readReceipts();
		return true;
	}
	/*.................................................................................................................*/
	void readReceipts(){
		readReceipts(getRootPath() + "receipts.xml");
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
		for (Iterator iter = receipts.iterator(); iter.hasNext();) {   // this is going through all of the notices
			Element messageElement = (Element) iter.next();
			ListableVector v = new ListableVector();
			v.addElement(new MesquiteString("identity", messageElement.elementText("identity")), false);
			v.addElement(new MesquiteString("updateVersion", messageElement.elementText("updateVersion")), false);
			v.addElement(new MesquiteString("uniqueLocation", messageElement.elementText("uniqueLocation")), false);
			v.addElement(new MesquiteString("explanation", messageElement.elementText("explanation")), false);
			v.addElement(new MesquiteString("packageName", messageElement.elementText("packageName")), false);
			if (!PhoneHomeUtil.alreadyInReceipts(v))
				PhoneHomeUtil.installedReceipts.addElement(v);
		}
		writeReceipts();  //rewrite into central
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
			buff.append("\t\t<uniqueLocation>" + ((MesquiteString)rec.getElement("uniqueLocation")).getValue() + "</uniqueLocation>\n");
			buff.append("\t\t<explanation><![CDATA[" + ((MesquiteString)rec.getElement("explanation")).getValue() + "]]></explanation>\n");
			buff.append("\t\t<packageName>" + ((MesquiteString)rec.getElement("packageName")).getValue() + "</packageName>\n");
			buff.append("\t</installationReceipt>\n");
		}
		buff.append("\n</mesquite>");
		MesquiteFile.putFileContents(getRootPath() + "receipts.xml", buff.toString(), true);
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

	boolean install(Element installElement, ListableVector receipt){
		String url = installElement.elementText("url");
		String pathInMesquiteFolder = installElement.elementText("location");
		String fileName = installElement.elementText("file");
		int version = MesquiteInteger.fromString(installElement.elementText("updateVersion"));  
		String treatment = installElement.elementText("treatment");
		String downloadAs = "installerDownload";
		if (treatment == null || treatment.equalsIgnoreCase("asis"))
			downloadAs = fileName;
		File prevPackage = new File(getRootPath() + pathInMesquiteFolder + "/" + fileName);
		File tempPackage = new File(getRootPath() + pathInMesquiteFolder + "/"  + fileName+ "PREVIOUSVERSION");
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
		logln("Downloading installation file from " + url);
		if (MesquiteFile.downloadURLContents(url, getRootPath() + pathInMesquiteFolder + "/" + downloadAs, true)){
			if (hadExisted){
				logln("Renaming old version of " + fileName + " to " + fileName + "PREVIOUSVERSION");

				prevPackage.renameTo(tempPackage);
			}
			logln("Unzipping installation file to " + getRootPath() + pathInMesquiteFolder+ "/" + downloadAs);
			boolean fileReady = true;
			if (treatment != null && treatment.equalsIgnoreCase("unzip")){
				fileReady = unzip(getRootPath() + pathInMesquiteFolder + "/", downloadAs);
				if (fileReady)
					MesquiteFile.deleteFile(getRootPath() + pathInMesquiteFolder + "/" + downloadAs);
			}
			if (fileReady){
				logln("Installation of " + fileName + " was successful.");
				File f = new File(getRootPath() + pathInMesquiteFolder + "/" + fileName);
				if (f.isDirectory() && receipt.indexOfByName("uniqueLocation")<0)
					receipt.addElement(new MesquiteString("uniqueLocation", pathInMesquiteFolder + "/" + fileName), false);
			}
			else if (hadExisted){
				logln("Installation unsuccessful; attempting to recover old version of " + fileName);
				tempPackage.renameTo(prevPackage);
				return false;
			}
		}	
		else {
			MesquiteFile.deleteFile(getRootPath() + pathInMesquiteFolder + "/" + downloadAs);
			return false;
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
			MesquiteFile.deleteDirectory(getRootPath() + pathInMesquiteFolder + "/" + fileName+ "PREVIOUSVERSION");
		}
	}
	void reverse(Element installElement){
		String pathInMesquiteFolder = installElement.elementText("location");
		String fileName = installElement.elementText("file");
		File prevPackage = new File(getRootPath() + pathInMesquiteFolder + "/" + fileName);
		File tempPackage = new File(getRootPath() + pathInMesquiteFolder + "/"  + fileName+ "PREVIOUSVERSION");
		boolean hadExisted = tempPackage.exists();
		if (hadExisted){
			logln("Installation unsuccessful; attempting to recover old version of " + fileName);
			MesquiteFile.deleteDirectory(getRootPath() + pathInMesquiteFolder + "/" + fileName);
			tempPackage.renameTo(prevPackage);
		}
	}


	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "phoneHomeLinkTouched", null, commandName, "phoneHomeLinkTouched")) {
			if (!MesquiteFile.canWrite(getRootPath())){
				discreetAlert("Sorry, you do not have the permissions required to modify the Mesquite_Folder.  Installation cannot proceed.");
				return null;
			}
			String source = parser.getFirstToken(arguments);
			ListableVector updateRecord = PhoneHomeUtil.getUpdateRecord(source.substring(source.indexOf(":")+1, source.length()));
			if (updateRecord == null)
				return null;
			MesquiteString identity = (MesquiteString)updateRecord.getElement("identity");
			MesquiteString versionString = (MesquiteString)updateRecord.getElement("updateVersion");
			MesquiteString packageName = (MesquiteString)updateRecord.getElement("packageName");
			MesquiteString explanation = (MesquiteString)updateRecord.getElement("explanation");
			ListableVector receipt = new ListableVector();
			receipt.addElement(identity, false);
			receipt.addElement(versionString, false);
			receipt.addElement(explanation, false);
			receipt.addElement(packageName, false);
			boolean asked = false;
			if (PhoneHomeUtil.alreadyInReceipts(identity.getValue(), versionString.getValue())){
				if (!MesquiteThread.isScripting() && !AlertDialog.query(containerOfModule(), "Install?", "The package " + packageName + " of the same version already appears to be installed.  Do you want to reinstall?"))
					return null;
				else asked = true;
			}
			if (!asked && !MesquiteThread.isScripting() && !AlertDialog.query(containerOfModule(), "Install?", "You have requested to install the package " + packageName + ".  Do you want to install it now?"))
				return null;
			parser.setString(versionString.getValue());
			int version = MesquiteInteger.fromString(parser);
			ObjectContainer io = (ObjectContainer)updateRecord.getElement("install");
			List installation = (List)io.getObject();
			int count = 0;
			boolean failed = false;
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
			if (!failed){
				//Here mesquite should store a receipt that this update was installed.  Should store package reference and version reference.  
				if (!PhoneHomeUtil.alreadyInReceipts(receipt))
					PhoneHomeUtil.installedReceipts.addElement(receipt);
				writeReceipts();
				PhoneHomeUtil.refreshUpdateMenuItems();
				discreetAlert("Installation was successful.  You will need to restart Mesquite to make use of the new installation.");
			}
		}
		else if (checker.compare(this.getClass(), "zip", null, commandName, "zip")) {
			String source = MesquiteFile.chooseDirectory("Directory to Zip", null);
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
		return true;
	}

}

