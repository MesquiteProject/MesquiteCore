/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import com.apple.mrj.*;
import javax.swing.*;

/* ======================================================================== */
/**This MesquiteFile is the file on disk, not the project.*/
public class MesquiteFile extends Listened implements HNode, Commandable, Listable, Explainable { 
	public static final int QUERY_NONE = 0;
	public static final int QUERY_CLOSE = 1;
	public static final int QUERY_DELETE = 2;
	public static final int QUERY_QUIT = 3;
	private InputStream inStream; //DataInputStream //BufferedInputStream
	private FileOutputStream outStream;
	private boolean writing = false;
	private int readCategory = -1; //0, linked; 1, include; -1 home file
	public static final int LINKED = 0;
	public static final int INCLUDED = 1;
	public static final int HOME = -1;

	public static boolean suppressReadWriteLogging = false;

	private boolean local = true;
	private URL url;
	//private InputStream streamFromHeaven;
	private String fileName,  directoryName;
	private MesquiteInteger startChar;
	//private String bufferString;
	boolean bufferRead = false;
	private MesquiteInteger pendingBrackets;
	private MesquiteProject project=null;
	private ListableVector fileElements;
	private long id=0;
	private static PrintWriter logStream = null;
	public static int totalCreated = 0;
	public static int totalDisposed = 0;
	public static int totalFinalized = 0;
	private boolean beingSaved = false;
	private boolean closed = false;
	private StringBuffer sB = new StringBuffer(100);
	private boolean writeProtected = false;
	private String comment = null;
	boolean dirtiedByCommand = false;
	private boolean openAsUntitled = false;
	private boolean closeAfterReading = false;
	private String untitledReasons ="";
	public Vector foreignElements; //keeps list of foreign elements to report to user after file read; may be disposed immediately after report
	public boolean useDataBlocks = false;  //todo: this is temporary until general format options system built
	public boolean useSimplifiedNexus = false;  //todo: this is temporary until general format options system built
	public boolean useConservativeNexus = false;  //todo: this is temporary until general format options system built
	public boolean readMesquiteBlock = true;  //todo: this is temporary until general format options system built
	public boolean useStandardizedTaxonNames = false;  //todo: this is temporary until general format options system built
	public boolean interleaveAllowed = true; //todo: this is temporary until general format options system built
	public boolean simplifyNames = false;  //todo: this is temporary until general format options system built
	public boolean ambiguityToMissing = false;  //todo: this is temporary until general format options system built
	public boolean writeCharLabels = false;  //todo: this is temporary until general format options system built
	public boolean writeExcludedCharacters=true;
	public boolean writeTaxaWithAllMissing = true;
	public boolean writeOnlySelectedTaxa = false;
	public double fractionApplicable =1.0;
	public boolean mrBayesReadingMode = false;  //todo: this is temporary until general format options system built
	public String fileReadingArguments = null;
	public int exporting = 0;  //todo: temporary.  0 = not exporting;  1 = first export; 2 = subsequent exports
	public boolean notesBugWarn = false;
	public Vector notesBugVector;
	StringBuffer remnantString;
	long filePos = 0;
	ProgressIndicator progIndicator;
	StringBuffer betweenCommandComments;
	private boolean isNexus = false;
	boolean projectClosing = false;
	MesquiteTimer nextBlockTimer, readLineTimer, getNextCommandStatusTimer;
	MesquiteCommand saveCommand = null;
	//private boolean dirty = false;
	public static String fileSeparator;
	public static boolean appendToLog = false;
	private byte[] lineEndingBytes;
	TaxonFilterer taxonFilterer = null;

	public ListableVector taxaNameTranslationTable = new ListableVector();
	public ListableVector characterDataNameTranslationTable = new ListableVector();

	private Author previousSaver;

	/*NEXUS files by PAUP convention have "current" taxa and characters blocks, namely last ones read.  
	The following are to enable this to be supported in Mesquite.  The reference should be nulled when reading is finished to prevent memory leaks! */
	private mesquite.lib.characters.CharacterData currentData;
	private Taxa currentTaxa;

	Parser parser;
	static {
		fileSeparator = System.getProperty("file.separator");
	}
	public MesquiteFile() {
		id = totalCreated;
		totalCreated++;
		parser = new Parser();
		remnantString = new StringBuffer(10);
		betweenCommandComments = new StringBuffer(10);
		pendingBrackets = new MesquiteInteger(0);
		startChar = new MesquiteInteger(0);
		nextBlockTimer = new MesquiteTimer();
		readLineTimer = new MesquiteTimer();
		try {
			lineEndingBytes = StringUtil.lineEnding().getBytes("ISO-8859-1");
		}
		catch (UnsupportedEncodingException e){
			lineEndingBytes = StringUtil.lineEnding().getBytes();
		}
		getNextCommandStatusTimer = new MesquiteTimer();
	}
	/*-------------------------------------------------------*/
	public long getID(){
		return id;
	}
	public void finalize() throws Throwable{
		totalFinalized++;
		super.finalize();
	}

	public void setReadCategory(int r){
		readCategory = r;
	}
	//0 = linked; 1 = include; -1 = home file
	public int getReadCategory(){
		return readCategory;
	}

	public void setPreviousSaver(Author a){
		previousSaver = a;
	}
	public Author getPreviousSaver(){
		return previousSaver;
	}
	public TaxonFilterer getTaxonFilterer() {
		return taxonFilterer;
	}
	public void setTaxonFilterer(TaxonFilterer taxonFilterer) {
		this.taxonFilterer = taxonFilterer;
	}
	public boolean filterTaxon(mesquite.lib.characters.CharacterData data, int it) {
		if (taxonFilterer!=null)
			return taxonFilterer.filterTaxon(data, it);
		return true;
	}

	/*-------------------------------------------------------*/
	public void setCurrentData(mesquite.lib.characters.CharacterData d){
		currentData = d;
	}
	public mesquite.lib.characters.CharacterData getCurrentData(){
		return currentData;
	}
	public void setCurrentTaxa(Taxa d){
		currentTaxa = d;
	}
	public Taxa getCurrentTaxa(){
		return currentTaxa;
	}
	public static boolean okToWriteTitleOfNEXUSBlock (MesquiteFile file, Listable obj) {
		if (StringUtil.blank(obj.getName()))
			return false;
		if (NexusBlock.suppressNEXUSTITLESANDLINKS)
			return false;
		if (file==null)
			return true;
		return !file.useSimplifiedNexus && !file.useConservativeNexus;
	}

	/*-------------------------------------------------------*/
	public static int getNumberOfFiles(){
		return totalCreated;
	}

	public String getOpenAsUntitled(){
		if (openAsUntitled)
			return untitledReasons;
		return null;
	}
	public void setOpenAsUntitled(String reason){
		openAsUntitled = true;
		untitledReasons += "\n" + reason;
	}
	/*-------------------------------------------------------*/
	public boolean getCloseAfterReading(){
		return closeAfterReading;
	}
	public void setCloseAfterReading(boolean c){
		closeAfterReading = c;
	}
	/*-------------------------------------------------------*/
	public void setBeingSaved(boolean s){
		beingSaved = s;
	}
	/*-------------------------------------------------------*/
	public boolean getBeingSaved(){
		return beingSaved;
	}
	/*-------------------------------------------------------*/
	public void setIsNexus(boolean s){
		isNexus = s;
	}
	/*-------------------------------------------------------*/
	public boolean isClosed(){
		return closed;
	}
	/*-------------------------------------------------------*/
	/** for development purposes; to time file reading //TODO: get rid of this in final*/
	public void reportTimes() {
		MesquiteMessage.println("Time in getNextBlock: " + nextBlockTimer.getAccumulatedTime());
		MesquiteMessage.println("Time for readLine: " + readLineTimer.getAccumulatedTime());
		MesquiteMessage.println("Time for getNextCommand(status): " + getNextCommandStatusTimer.getAccumulatedTime());
	}

	/*.................................................................................................................*/
	/** The method by which commands are sent to the file using text-based scripting.*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 
		if (checker.compare(this.getClass(), "Returns identification number of this file", null, commandName, "getID")) {
			return new MesquiteInteger((int)getID());
		}
		else if (checker.compare(this.getClass(), "Returns coordinator module of project", null, commandName, "getCoordinatorModule")) {
			if (project!=null)
				return project.getCoordinatorModule();
		}
		else  if (checker.compare(this.getClass(), "Closes this file", null, commandName, "close")) {
			if (project!=null && project.getCoordinatorModule()!=null)
				project.getCoordinatorModule().closeFile(this);
		}
		else  if (checker.compare(this.getClass(), "Saves this file", null, commandName, "save")) {
			if (project!=null && project.getCoordinatorModule()!=null)
				project.getCoordinatorModule().saveFile(this);
		}
		else  if (checker.compare(this.getClass(), "Saves this file as", null, commandName, "saveAs")) {
			if (project!=null && project.getCoordinatorModule()!=null)
				project.getCoordinatorModule().saveFileAs(this);
		}
		return null;
	}
	/*.................................................................................................................*/
	/** Returns size of existing file on disk corresponding to this MesquiteFile*/
	public long existingLength() {
		if (local) {
			String path =directoryName+fileName;
			File testing = new File(path);
			return testing.length();
		}
		return 0;
	}	

	public String toString() {
		return "File \"" + fileName + "\" in directory \"" + directoryName + "\", id " + getID();
	}

	/*-------------------------------------------------------*/
	/** if not applet, opens new file on disk with given name */
	public static MesquiteFile newFile(String directoryName, String fileName) {
		if (!MesquiteTrunk.isApplet()) {
			//test to see if directory exists
			MesquiteFile mF = new MesquiteFile();
			mF.local = true;
			mF.fileName = fileName;
			mF.directoryName = directoryName;
			return mF;
		}
		return null;
	}
	/*.................................................................................................................*/
	public static boolean createDirectory(String path){
		if (!MesquiteFile.fileOrDirectoryExists(path)) {
			String containing = StringUtil.getAllButLastItem(path, "/");
			String containing2 = StringUtil.getAllButLastItem(path, fileSeparator);
			// use longest string that is not the same size as the original path
			if (containing.length()>=  path.length()){  //can't use containing
				if (containing2.length()< path.length()) //only containing2 is shorter; use it
					containing = containing2;
				else return false;  //can't use either 
			}
			else if (containing2.length()>= path.length())//both are too long
				return false;
			else if (containing2.length()>containing.length()) //neither too long; therefore take longest
				containing = containing2;

			createDirectory(containing);
			File f = new File(path);
			boolean b = f.mkdir();
			return b;
		} 
		return true;
	}

	/*.................................................................................................................*/
	/** A basic method for copying one File to another; if the destination file does not exist, it will be created */
	public static void copy(File src, File dst) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}


	/*.................................................................................................................*/
	/** A basic method for copying one File to another; if the destination file does not exist, it will be created */
	public static void copyFileFromPaths(String originalPath, String newPath, boolean warn) {
		if (StringUtil.blank(originalPath) || StringUtil.blank(newPath))
			return;
		try { 
			File originalFile = new File(originalPath); //
			File newFile = new File(newPath); //
			MesquiteFile.copy(originalFile, newFile);
		}
		catch (IOException e) {
			if (warn)
				MesquiteMessage.println("Can't copy file \n    " + originalPath + "\nto\n    " + newPath);
		}
	}


	/*-------------------------------------------------------*/
	/** if not applet, allows user to choose, then opens existing file on disk */
	public static MesquiteFile open(boolean local, FilenameFilter fileFilter, String message, String suggestedDirectory) {
		if (local) {

			if (!MesquiteTrunk.isApplet()) {
				MainThread.incrementSuppressWaitWindow();
				MesquiteFileDialog fdlg= new MesquiteFileDialog(MesquiteTrunk.mesquiteTrunk.containerOfModule(), message, FileDialog.LOAD);
				fdlg.setResizable(true);
				if (suggestedDirectory !=null)
					fdlg.setDirectory(suggestedDirectory);
				fdlg.setBackground(ColorTheme.getInterfaceBackground());
				if (fileFilter!=null)
					fdlg.setFilenameFilter(fileFilter);

				fdlg.setVisible(true);

				String fName=fdlg.getFile();
				String dName =fdlg.getDirectory();
				String pathName=dName + fName;
				//fdlg.dispose();
				MainThread.decrementSuppressWaitWindow();
				if (!StringUtil.blank(fName) && !StringUtil.blank(dName)) {
					MesquiteFile mF = new MesquiteFile();
					mF.local = true;
					mF.fileName = fName;
					mF.directoryName = dName;
					return mF;
				}
				if (StringUtil.blank(dName) &&  StringUtil.blank(fName))
					MesquiteTrunk.mesquiteTrunk.logln("Open File cancelled by user");
				else if ( StringUtil.blank(fName))
					MesquiteTrunk.mesquiteTrunk.logln("Open File not completed because file name not supplied.");
				else if ( StringUtil.blank(dName))
					MesquiteTrunk.mesquiteTrunk.logln("Open File not completed because directory name not supplied.");

			}
		}
		else {
			String s= MesquiteString.queryString(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Open URL", "URL to open:", "");
			URL url=null;
			if (!StringUtil.blank(s)) {
				try {
					url = new URL(s);
					MesquiteFile mF = new MesquiteFile();
					mF.local = false;
					mF.url= url;
					String urlString = url.toString();
					mF.fileName = StringUtil.getLastItem(urlString, fileSeparator, "/"); 
					mF.directoryName = StringUtil.getAllButLastItem(urlString, fileSeparator, "/") + fileSeparator; 

					return mF;
				}
				catch (MalformedURLException e) {MesquiteModule.mesquiteTrunk.discreetAlert( MesquiteThread.isScripting(),"Bad URL specified for data file: \"" + s + "\"");}
			}
		}
		return null;
	}
	/*-------------------------------------------------------*/
	/** opens existing file on disk or URL*/
	public static MesquiteFile open(boolean local, String location) {
		if (StringUtil.blank(location))
			return open(local, (FilenameFilter)null, "Open file:", null);
		else if (local) {
			if (!MesquiteTrunk.isApplet()) {
				String fName = StringUtil.getLastItem(location, fileSeparator, "/"); 
				String dName = StringUtil.getAllButLastItem(location, fileSeparator, "/") + fileSeparator; 
				return open(dName, fName);
			}
		}
		else {
			URL url=null;
			if (!StringUtil.blank(location)) {
				try {
					url = new URL(location);
					return open(url);
				}
				catch (MalformedURLException e) {MesquiteModule.mesquiteTrunk.discreetAlert( MesquiteThread.isScripting(),"Bad URL specified for data file: \"" + location + "\"");}
			}
		}
		return null;
	}
	/*-------------------------------------------------------*/
	/** opens existing file on disk or URL*/
	public static MesquiteFile open(boolean local, String location, String suggestedDirectory) {
		return open(local, location, "Open file:", suggestedDirectory);
	}

	/*-------------------------------------------------------*/
	/** opens existing file on disk or URL*/
	public static MesquiteFile open(boolean local, String location, String message, String suggestedDirectory) {
		if (StringUtil.blank(location))
			return open(local, (FilenameFilter)null, message, suggestedDirectory);
		else if (local) {
			if (!MesquiteTrunk.isApplet()) {
				String fName = ""; 
				String dName = ""; 
				if (location.indexOf(fileSeparator) <0 && location.indexOf("/")<0)
					fName = location;
				else {
					fName = StringUtil.getLastItem(location, fileSeparator, "/"); 
					dName = StringUtil.getAllButLastItem(location, fileSeparator, "/") + fileSeparator; 
				}
				return open(dName, fName);
			}
		}
		else {
			URL url=null;
			if (!StringUtil.blank(location)) {
				try {
					url = new URL(location);
					return open(url);
				}
				catch (MalformedURLException e) {MesquiteModule.mesquiteTrunk.discreetAlert( MesquiteThread.isScripting(),"Bad URL specified for data file: \"" + location + "\"");}
			}
		}
		return null;
	}
	/*-------------------------------------------------------*/
	/** if not applet, opens existing file on disk with given name */
	public static MesquiteFile open(String directoryName, String fileName) {
		if (!fileExists(directoryName+ fileName))  {
			if (StringUtil.blank(directoryName))
				directoryName = MesquiteTrunk.suggestedDirectory;
			else if (directoryName.endsWith(fileSeparator) || directoryName.endsWith("/"))
				directoryName = MesquiteTrunk.suggestedDirectory + directoryName;
			else
				directoryName = MesquiteTrunk.suggestedDirectory + directoryName + fileSeparator;  //Dec 2013 this was backwards!



		}
		if ((directoryName.indexOf("//")<0) && fileExists(directoryName+ fileName)) { //Dec 2013 added check on double //
			MesquiteFile mF = new MesquiteFile();
			mF.local = true;
			mF.fileName = fileName;
			mF.directoryName = directoryName;
			return mF;
		}
		MesquiteModule.mesquiteTrunk.discreetAlert(MesquiteThread.isScripting(), "File Busy or Not Found  (0): \ndirectory <" + directoryName + "> \nfile <" + fileName + ">");
		return null;
	}
	public void setLocation(String fileName, String dir, boolean local){
		this.local = local;
		this.fileName = fileName;
		this.directoryName = dir;
	}
	/*-------------------------------------------------------*/
	/** opens existing file at given URL */
	public static MesquiteFile open(URL url) {
		MesquiteFile mF = new MesquiteFile();
		mF.local = false;
		mF.url= url;
		String urlString = url.toString();
		mF.fileName = StringUtil.getLastItem(urlString, fileSeparator, "/"); 
		mF.directoryName = StringUtil.getAllButLastItem(urlString, fileSeparator, "/") + fileSeparator; 
		return mF;
	}
	/*-------------------------------------------------------*/
	/** opens an input stream, e.g. if Mesquite is acting as a servlet *
	public static MesquiteFile open(InputStream streamFromHeaven) {
		MesquiteFile mF = new MesquiteFile();
		mF.local = false;
		mF.url= null;
		mF.streamFromHeaven = streamFromHeaven;
		mF.fileName = null;
		mF.directoryName = null;
		return mF;
	}
	/*-------------------------------------------------------*/
	public boolean isDirty(){
		if (dirtiedByCommand) {
			return true;
		}
		if (fileElements!=null){
			Enumeration e = fileElements.elements();
			while (e.hasMoreElements()) {
				FileElement elem = (FileElement)e.nextElement();
				if (elem.getDirty()) {
					return true;
				}
			}
		}
		return false;
	}
	/*-------------------------------------------------------*/
	/** Closes the file and removes from the project all file elements belonging to it.*/
	public boolean close() {
		if (MesquiteTrunk.checkMemory)
			MesquiteTrunk.mesquiteTrunk.logln(">= Closing file " + getName() + " =<");
		try {
			while (beingSaved) {
				Thread.sleep(50);
			}
		}
		catch(InterruptedException e){}
		boolean dirty = false;
		try {
			if (fileElements!=null){
				project.incrementProjectWindowSuppression();
				int numElements = fileElements.size();
				Enumeration eD = fileElements.elements();
				int numDisposed=0;
				int numToDispose = calcNumToDispose();
				boolean didOne;
				while (numToDispose>0 /*&& lastNumToDispose != numToDispose*/) {
					didOne=false;
					numToDispose = calcNumToDispose();
					if (numToDispose>0){
						Enumeration eDd = fileElements.elements();
						while (eDd.hasMoreElements()) {
							FileElement elem = (FileElement)eDd.nextElement();
							elem.projectClosing = projectClosing;
							if (projectClosing)
								elem.incrementNotifySuppress();
							elem.dispose();
							if (!elem.isDoomed())
								MesquiteMessage.warnProgrammer("oops, deleted element not marked as doomed");
							numDisposed++;
							project.removeFileElement(elem);
							numToDispose--;
							didOne = true;
						}
						if (!didOne)
							MesquiteMessage.warnProgrammer("oops, cycle none disposed");
					}
				}
				if (numElements!= numDisposed && fileElements.size()>0) {
					MesquiteMessage.warnProgrammer("Number elements disposed (" + numDisposed + ") not same as number reference (" + numElements + ") in file " + getName());
					Enumeration eDe = fileElements.elements();
					while (eDe.hasMoreElements()) {
						FileElement elem = (FileElement)eDe.nextElement();
						if (!elem.isDisposed())
							MesquiteMessage.warnProgrammer("    Not disposed: " + elem.getName() + " of class " + elem.getClass().getName());
					}
				}
				fileElements.removeAllElements(false);
				fileElements.dispose();
				fileElements = null;
				project.decrementProjectWindowSuppression();
			}
		}
		catch (NullPointerException e){
		}
		project.removeFile(this);
		closed = true;
		project = null;
		MesquiteTrunk.mesquiteTrunk.resetAllMenuBars();
		totalDisposed++;
		dispose();
		return true;
	}

	int calcNumToDispose(){
		int numToDispose = 0;
		for (int i=0; i<fileElements.size(); i++){
			FileElement elem = (FileElement)fileElements.elementAt(i);
			if (elem != null && !elem.isDoomed())
				numToDispose++;
		}
		return numToDispose;
	}
	boolean justCleaned = false;
	/*-------------------------------------------------------*/
	public void setDirtiedByCommand(boolean dirtied){
		dirtiedByCommand = dirtied;
	}
	/*-------------------------------------------------------*/
	/** Closes the file, removes from the project all file elements belonging to it, and deletes it.*/
	public boolean closeAndDelete(){
		close();
		return deleteFile(directoryName + fileName);
	}
	/*.................................................................................................................*/
	/** Downloads the file present at the ftp URL urlString, saves the file into the local directory at localSaveDirectoryPath,
	in the file localSaveName.  If localSaveName is blank, then it saves the file name given in the URL.*/
	public static boolean downloadFTPorHTTPFile(String urlString, String localSaveDirectoryPath, String localSaveName){
		//    first, acquire the name of the remote file 
		if (StringUtil.blank(urlString))
			return false;
		if (!(urlString.toLowerCase().startsWith("ftp://") || urlString.toLowerCase().startsWith("http://") || urlString.toLowerCase().startsWith("https://"))) {
			return false;
		}
		String fileName = "downloadedFile";
		if (!StringUtil.blank(localSaveName)) {
			fileName = localSaveName;
		} else {
			fileName = urlString;
			while (fileName.indexOf("/")>=0)
				fileName = fileName.substring(fileName.indexOf("/")+1, fileName.length());
			if (StringUtil.blank(fileName))
				fileName = "downloadedFile";
		}


		try { 
			//    now, down load the remote file 
			URL ftp = new URL(urlString); 
			URLConnection conn = ftp.openConnection (); 
			final int BUF_SIZE = 4096; 
			byte[] buf = new byte[BUF_SIZE]; 
			DataInputStream inStream = new DataInputStream(conn.getInputStream()); //, BUF_SIZE); 

			//    write the file locally 
			FileOutputStream outStream = new FileOutputStream(localSaveDirectoryPath + MesquiteFile.fileSeparator + fileName); 
			int bytesRead = -1; 
			int count=0;
			while ((bytesRead = inStream.read(buf, 0, BUF_SIZE)) > -1) { 

				outStream.write(buf, 0, bytesRead); 
			} 
			outStream.close(); 
			inStream.close(); 
			MesquiteModule.mesquiteTrunk.logln(" ");
			MesquiteModule.mesquiteTrunk.logln("File " + fileName + " downloaded");
		} 
		catch(Exception e) { 
			MesquiteModule.mesquiteTrunk.logln("File " + fileName + " could not be downloaded");
			MesquiteModule.mesquiteTrunk.logln("Exception: " + e.toString());
			e.printStackTrace(); 
			return false;
		} 
		return true;
	}
	/*.................................................................................................................*/
	/** Downloads the file present at the ftp URL urlString, saves the file into the local directory at localSaveDirectoryPath,
	using the file name given in the URL.*/
	public static boolean downloadFTPorHTTPFile(String urlString, String localSaveDirectoryPath){
		return downloadFTPorHTTPFile(urlString,localSaveDirectoryPath,null);
	}
	/*-------------------------------------------------------*/
	/** A low-level call to change various location parameters.  Shouldn't be used too liberally; changeLocation does more housekeeping.*/
	public void setLocs(boolean local, URL url, String f, String d){
		this.local = local;
		this.url = url;
		this.fileName = f;
		this.directoryName = d;
	}
	/*-------------------------------------------------------*/
	/** A low-level call to change fileName.  Shouldn't be used too liberally; changeLocation does more housekeeping.*/
	public void setPath(String path) {
		if (path==null)
			return;
		if (path.indexOf(MesquiteFile.fileSeparator)<0) {
			fileName =path;
		}
		else {
			directoryName = StringUtil.getAllButLastItem(path, MesquiteFile.fileSeparator, "/") + MesquiteFile.fileSeparator;
			fileName = StringUtil.getLastItem(path, MesquiteFile.fileSeparator, "/");
		}
	}
	/*-------------------------------------------------------*/
	/** Returns path to file on disk  19 Jan 02*/
	public String getPath() {
		if (local) {
			return directoryName+fileName;
		}
		return null;
	}	
	public void setFileName(String fileName){
		this.fileName = fileName;
	}
	/*-------------------------------------------------------*/
	/** redirects place at which file will be rewritten; for use by save as */
	public void changeLocation(String directoryName, String fileName) {
		this.fileName =fileName;
		this.directoryName=directoryName;
		MesquiteWindow.resetAllTitles();
		MesquiteModule.resetAllMenuBars();
		if (getHShow()) {
			MesquiteTrunk.mesquiteTrunk.refreshBrowser(MesquiteFile.class);
			if (getProject() != null)
				getProject().refreshProjectWindow();
		}
	}
	/*-------------------------------------------------------*/
	/** for use by save as; user puts file.  Returns true if new location successfuly found */
	public boolean changeLocation(String message) {
		MainThread.incrementSuppressWaitWindow();
		MesquiteFileDialog fdlg= new MesquiteFileDialog(MesquiteTrunk.mesquiteTrunk.containerOfModule(), message, FileDialog.SAVE);
		fdlg.setResizable(true);
		fdlg.setBackground(ColorTheme.getInterfaceBackground());
		fdlg.setFile(fileName);
		fdlg.setVisible(true);
		String tempFileName=fdlg.getFile();
		String tempDirectoryName=fdlg.getDirectory();
		// fdlg.dispose();
		MainThread.decrementSuppressWaitWindow();
		if (!StringUtil.blank(tempFileName) && !StringUtil.blank(tempDirectoryName)) {
			fileName=tempFileName;
			directoryName=tempDirectoryName;
			MesquiteWindow.resetAllTitles(); //TODO: should also send out message to all modules thatfilename changed
			MesquiteModule.resetAllMenuBars();
			if (getHShow()) {
				MesquiteTrunk.mesquiteTrunk.refreshBrowser(MesquiteFile.class);
				if (getProject() != null)
					getProject().refreshProjectWindow();
			}
			return true;
		}
		else {
			if (StringUtil.blank(tempDirectoryName) &&  StringUtil.blank(tempFileName))
				MesquiteTrunk.mesquiteTrunk.logln("Save As cancelled by user");
			else if ( StringUtil.blank(tempFileName))
				MesquiteTrunk.mesquiteTrunk.logln("Save As not completed because file name not supplied.");
			else if ( StringUtil.blank(tempDirectoryName))
				MesquiteTrunk.mesquiteTrunk.logln("Save As not completed because directory name not supplied.");
			return false;
		}
	}
	/*-------------------------------------------------------*/
	/** for use by save as; user puts file.  Returns new location */
	public static String saveFileAsDialog(String message) {
		return saveFileAsDialog(message, null);
	}

	/*-------------------------------------------------------*/
	/** for use by save as; user puts file.  Returns new location */
	public static String saveFileAsDialog(String message, StringBuffer fileNameBuffer) {
		MainThread.incrementSuppressWaitWindow();
		MesquiteFileDialog fdlg= new MesquiteFileDialog(MesquiteTrunk.mesquiteTrunk.containerOfModule(), message, FileDialog.SAVE);
		fdlg.setResizable(true);
		fdlg.setBackground(ColorTheme.getInterfaceBackground());
		fdlg.setVisible(true);
		String tempFileName=fdlg.getFile();
		if (fileNameBuffer != null)
			fileNameBuffer.append(tempFileName);
		String tempDirectoryName=fdlg.getDirectory();
		// fdlg.dispose();
		MainThread.decrementSuppressWaitWindow();
		if (!StringUtil.blank(tempFileName) && !StringUtil.blank(tempDirectoryName)) {
			return tempDirectoryName + tempFileName;
		}
		else {
			if (StringUtil.blank(tempDirectoryName) &&  StringUtil.blank(tempFileName))
				MesquiteTrunk.mesquiteTrunk.logln("Save As cancelled by user");
			else if ( StringUtil.blank(tempFileName))
				MesquiteTrunk.mesquiteTrunk.logln("Save As not completed because file name not supplied.");
			else if ( StringUtil.blank(tempDirectoryName))
				MesquiteTrunk.mesquiteTrunk.logln("Save As not completed because directory name not supplied.");
			return null;
		}
	}
	/*-------------------------------------------------------*/
	/** To choose a directory; returns path */
	public static String chooseDirectory(String message) {
		return chooseDirectory(message, null);
	}
	/*-------------------------------------------------------*/
	/** To choose a directory; returns path */
	public static String chooseDirectory(String message, String suggestedDir) {
		MainThread.incrementSuppressWaitWindow();
		String tempFileName=null;
		String tempDirectoryName=null;
		if (!MesquiteWindow.GUIavailable ||  MesquiteWindow.suppressAllWindows || (MesquiteTrunk.isMacOS() && MesquiteTrunk.isJavaVersionLessThan(1.4))) {
			MesquiteFileDialog fdlg= new MesquiteFileDialog(MesquiteTrunk.mesquiteTrunk.containerOfModule(), message, 3);
			fdlg.setResizable(true);
			if (suggestedDir != null)
				fdlg.setDirectory(suggestedDir);
			fdlg.setBackground(ColorTheme.getInterfaceBackground());
			fdlg.setVisible(true);
			tempFileName=fdlg.getFile();
			tempDirectoryName=fdlg.getDirectory();
			// fdlg.dispose();
		}
		else	if (MesquiteTrunk.isMacOS() || MesquiteTrunk.isMacOSX()) {  
			MesquiteFileDialog fdlg= new MesquiteFileDialog(MesquiteTrunk.mesquiteTrunk.containerOfModule(), message, FileDialog.LOAD);
			System.setProperty("apple.awt.fileDialogForDirectories", "true");
			fdlg.setResizable(true);
			if (suggestedDir != null)
				fdlg.setDirectory(suggestedDir);
			fdlg.setBackground(ColorTheme.getInterfaceBackground());
			fdlg.setVisible(true);
			tempFileName=fdlg.getFile();
			if (tempFileName==null)
				tempDirectoryName=null;
			else
				tempDirectoryName=fdlg.getDirectory();

			// fdlg.dispose();
			System.setProperty("apple.awt.fileDialogForDirectories", "false");
		}
		else {
			JFileChooser fdlg;

			if (suggestedDir != null) {
				fdlg= new JFileChooser(new File(suggestedDir));
			}
			else
				fdlg= new JFileChooser();
			fdlg.setBackground(ColorTheme.getInterfaceBackground());
			fdlg.setDialogTitle(message);
			fdlg.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnValue = fdlg.showOpenDialog(MesquiteTrunk.mesquiteTrunk.containerOfModule().getParentFrame());
			if (returnValue == JFileChooser.APPROVE_OPTION){
				tempDirectoryName = fdlg.getSelectedFile().getAbsolutePath();
				tempFileName = null;
			}
		}
		MainThread.decrementSuppressWaitWindow();
		if (!StringUtil.blank(tempFileName) && !StringUtil.blank(tempDirectoryName))
			return tempDirectoryName + tempFileName;
		if (!StringUtil.blank(tempDirectoryName))  
			return tempDirectoryName;
		return null;
	}
	/*-------------------------------------------------------*/
	/** for use by open; chooses file.  Returns full path and also separate directory and file names in MesquiteStrings */
	public static String openFileDialog(String message, MesquiteString directoryName, MesquiteString fileName) {


		if (message == null)
			message = "Select file";
		MainThread.incrementSuppressWaitWindow();
		MesquiteFileDialog fdlg= new MesquiteFileDialog(MesquiteTrunk.mesquiteTrunk.containerOfModule(), message, FileDialog.LOAD);
		fdlg.setResizable(true);
		fdlg.setBackground(ColorTheme.getInterfaceBackground());
		fdlg.setVisible(true);
		String tempFileName=fdlg.getFile();
		String tempDirectoryName=fdlg.getDirectory();
		// fdlg.dispose();
		MainThread.decrementSuppressWaitWindow();
		if (!StringUtil.blank(tempFileName) && !StringUtil.blank(tempDirectoryName)) {
			if (directoryName!=null)
				directoryName.setValue(tempDirectoryName);
			if (fileName!=null)
				fileName.setValue(tempFileName);
			return tempDirectoryName + tempFileName;
		}
		else {
			if (StringUtil.blank(tempDirectoryName) &&  StringUtil.blank(tempFileName))
				MesquiteTrunk.mesquiteTrunk.logln("Open File cancelled by user");
			else if ( StringUtil.blank(tempFileName))
				MesquiteTrunk.mesquiteTrunk.logln("Open File not completed because file name not supplied.");
			else if ( StringUtil.blank(tempDirectoryName))
				MesquiteTrunk.mesquiteTrunk.logln("Open File not completed because directory name not supplied.");
			return null;
		}

	}
	/*-------------------------------------------------------*/
	/** returns true if file is local (on disk), false if remote (via URL).*/
	public boolean isLocal(){
		return local;
	}
	/*-------------------------------------------------------*/
	/** sets the project to which the MesquiteFile belongs.*/
	public void setProject(MesquiteProject proj) {
		project = proj;
	}
	/*-------------------------------------------------------*/
	/** gets the project to which the MesquiteFile belongs.*/
	public MesquiteProject getProject(){
		return project;
	}
	/*-------------------------------------------------------*/
	/** Opens the file for reading.*/
	public boolean openReading(boolean warn) {
		remnantString.setLength(0);
		if (local) {
			try {
				inStream = new DataInputStream(new FileInputStream(directoryName + fileName)); //this had been BufferedInputStream with buffer 1024 but this caused OutOfMemoryErrors on large files, at least on OS X
				filePos = 0;
				currentByte = 0;
				bytesAvailable = 0;
				return true;
			}
			catch( FileNotFoundException e ) {
				if (warn) {
					MesquiteMessage.printStackTrace();
					MesquiteModule.mesquiteTrunk.discreetAlert( MesquiteThread.isScripting(),"File Busy or Not Found (1): \ndirectory <" + directoryName + "> \nfile <" + fileName + ">");
				}
			} 
			catch( IOException e ) {
				if (warn){
					MesquiteMessage.printStackTrace();
					MesquiteModule.mesquiteTrunk.discreetAlert( MesquiteThread.isScripting(),"IO exception in openReading (local) for <" + directoryName + "> \nfile <" + fileName + "> " + e.getMessage());
				}
			}
		}
		else {
			if (url!=null) {
				try {
					inStream = new DataInputStream(url.openStream()); //, 1024);
					filePos = 0;
					currentByte = 0;
					bytesAvailable = 0;
					return true;
				}
				catch( IOException e ) {
					if (warn){
						MesquiteMessage.printStackTrace();
						MesquiteModule.mesquiteTrunk.discreetAlert( MesquiteThread.isScripting(),"IO exception in openReading (url) for <" + directoryName + "> \nfile <" + fileName + "> " + e.getMessage() );
					}
				}
			}
			/*else if (streamFromHeaven !=null) {

					inStream = new DataInputStream(streamFromHeaven); //, 1024);
					filePos = 0;
					currentByte = 0;
					bytesAvailable = 0;
					return true;

			}*/

		}
		return false;
	}
	/*-------------------------------------------------------*/
	/** Opens the file for reading.*/
	public boolean openReading() {
		return openReading(true);
	}
	/*-------------------------------------------------------*/
	/** Closes the file for reading.*/
	public void closeReading() {
		try {
			currentData = null;
			currentTaxa = null;
			if (progIndicator!=null)
				progIndicator.goAway();
			progIndicator = null;
			if (inStream!=null){
				inStream.close();
			}
			inStream = null;
			filePos = 0;
			currentByte = 0;
			bytesAvailable = 0;
		}
		catch( IOException e ) {MesquiteModule.mesquiteTrunk.discreetAlert( MesquiteThread.isScripting(), "IO exception in closeReading for <" + directoryName + "> \nfile <" + fileName + "> " + e.getMessage() );}
	}

	/*-------------------------------------------------------*/
	/*if autoCopySuffix is non null, then subsequent writing saves as special temporary copy with that sring embedded in its name, 
	 * which closeWriting does not erase or rename into old name, and the saved name is available immediately after within autoCopyName
	 * */
	String autoCopySuffix = null; 
	String autoCopyName = null;
	public void setWriteAsAutoCopy(String autoCopySuffix){
		autoCopyName = null;
		this.autoCopySuffix = autoCopySuffix;
	}
	public String getAutoCopySuffix(){
		return autoCopySuffix;
	}
	public String getTempAutoCopyName(){
		return autoCopyName;
	}

	/*-------------------------------------------------------*/
	String tempFileName;
	String writingFileName, backupDirPath, backupFileName;

	/** Open file for writing. */
	public boolean openWriting(boolean ascii) {
		if (writeProtected) {
			MesquiteModule.mesquiteTrunk.discreetAlert( MesquiteThread.isScripting(), "File is write protected <" + directoryName + "> \nfile <" + fileName + ">"  );
			return false;
		}
		writing = true;
		if (!suppressReadWriteLogging)
			MesquiteModule.mesquiteTrunk.logln( "Saving File: " + fileName);
		if (directoryName !=null) {
			writingFileName = directoryName + fileName;
			backupDirPath = directoryName + "backups" + fileSeparator;
			backupFileName = backupDirPath + fileName;
			if (!suppressReadWriteLogging)
				MesquiteModule.mesquiteTrunk.logln( "In Directory: " + directoryName);
		}
		else {
			writingFileName = fileName;
			backupDirPath = "backups" + fileSeparator;
			backupFileName = "backups" + fileSeparator + fileName;
		}
		if (fileExists(writingFileName) && !canWrite(writingFileName)){
			MesquiteModule.mesquiteTrunk.discreetAlert( MesquiteThread.isScripting(),"File cannot be written.  It may be locked or open in another application. (1; Path: " + writingFileName + ")"); 

			return false;
		}
		int tempNumber = 1;
		String ac = "T";
		if (autoCopySuffix != null)
			ac = autoCopySuffix;
		autoCopyName = null;
		while (new File(tempFileName= (writingFileName + ac + (tempNumber++))).exists())
			;

		try {
			/*
			if (ascii && System.getProperty("os.name").startsWith("Mac"))
				outStream = new OutputStreamWriter(new FileOutputStream(tempFileName), "ASCII");
			else
			 */
			outStream = new FileOutputStream(new File(tempFileName));  //PrintWriter
			return true;
		}
		catch( FileNotFoundException e ) {
			MesquiteMessage.printStackTrace();
			MesquiteModule.mesquiteTrunk.discreetAlert( MesquiteThread.isScripting(),"File Busy or Not Found  (2): \ndirectory <" + directoryName + "> \nfile <" + fileName + "> "  + tempFileName);
		} 
		catch( IOException e ) {
			MesquiteMessage.printStackTrace();
			MesquiteModule.mesquiteTrunk.discreetAlert( MesquiteThread.isScripting(),"IO exception in openWriting for <" + directoryName + "> \nfile <" + fileName + "> " + e.getMessage());
		}
		return false;
	}

	/*-------------------------------------------------------*/
	/** Write string to file and add newline character. */
	public void writeLine(String s)
	{	
		if (outStream!=null){
			try {

				byte[] sBytes = s.getBytes("ISO-8859-1");

				outStream.write(sBytes); 
				outStream.write(lineEndingBytes); //was '\n'
				outStream.flush();
			}
			catch (IOException ioe){}
		}
		else
			MesquiteMessage.printStackTrace("ERROR: attempt to write to null stream");
	}
	/*-------------------------------------------------------*/
	/** Write string to file */
	public void write(String s)
	{	
		if (outStream!=null){
			try {
				byte[] sBytes = s.getBytes("ISO-8859-1");

				outStream.write(sBytes); 
				outStream.flush();

			}
			catch (IOException ioe){}
		}
		else
			MesquiteMessage.printStackTrace("ERROR: attempt to write to null stream");
	}
	/*-------------------------------------------------------*/
	/** Close file for writing */
	public void closeWriting() {
		closeWriting(0);
	}
	/*-------------------------------------------------------*/
	/** Close file for writing */
	public void closeWriting(int numBackups) {
		if (outStream!=null){
			try {
				outStream.close();
				if (writingFileName == null)
					return;
				File writingFile = new File(writingFileName);
				String setFileType = "MESQ";
				if (isNexus && writingFile.exists()){
					try {
						MRJOSType type = MRJFileUtils.getFileCreator(writingFile);
						String ts = null;
						if (type !=null)
							ts = type.toString();

						if  (type != null && !StringUtil.blank(ts)) {
							setFileType = ts;
						}
					}
					catch (Throwable t){
					}
				}
				if (autoCopySuffix == null && numBackups>0){
					File bkpDir = new File(backupDirPath);
					bkpDir.mkdirs();
					for (int i = numBackups-1; i>0; i--) {
						MesquiteFile.rename(backupFileName + "BKP"  + (i), backupFileName + "BKP"  + (i+1));
					}
					MesquiteFile.rename(writingFileName, backupFileName + "BKP1");


				}
				else if (autoCopySuffix == null)
					writingFile.delete();
				if (autoCopySuffix != null){
					autoCopyName = tempFileName;
				}
				else {
					writingFile = new File(writingFileName); //not sure if needed
					if (!MesquiteFile.rename(tempFileName, writingFileName)){

						MesquiteMessage.warnProgrammer("The temporary file " + tempFileName + " could not be renamed to " + writingFileName + ". The operating system claims you do not have permission to modify one or the other file.");
						if (MesquiteTrunk.isWindows())
							MesquiteMessage.warnProgrammer("If you are working on a Windows machine, you may have modified the original file with another program, which may mysteriously prevent you from modifying it in Mesquite.  This appears to be a bug in Windows.   Try duplicating the file, or using Save As in Mesquite to make a new copy of the file which should be free of the problem, or using another operating system.");
					}
					try {MRJFileUtils.setFileTypeAndCreator(writingFile, new MRJOSType("TEXT"), new MRJOSType(setFileType));}
					catch (Throwable t){}
				}
			}
			catch (IOException ioe){}
		}
		else
			MesquiteMessage.printStackTrace("ERROR: attempt to close null stream");
		writing = false;
	}

	public boolean isWriting(){
		return writing;
	}

	/*-------------------------------------------------------*/
	/** Get directory in which file resides (actually, entire path) */
	public String getDirectoryName() {
		return directoryName;
	}

	/*-------------------------------------------------------*/
	/** Get name of file. */
	public String getFileName() {
		return fileName;
	}

	/*-------------------------------------------------------*/
	/** Get URL of file (null if a local file). */
	public URL getURL() {
		return url;
	}
	/*-------------------------------------------------------*/
	/** Return string for directory path from full file path*/
	public static String getDirectoryPathFromFilePath(String path) { 
		return StringUtil.getAllButLastItem(path, fileSeparator, "/") + fileSeparator; 
	}
	/*-------------------------------------------------------*/
	/** Return string for directory path from full file path*/
	public static String getFileNameFromFilePath(String path) { 
		return StringUtil.getLastItem(path, fileSeparator, "/"); 
	}
	/*-------------------------------------------------------*/
	/** Return string for path, accommodating ../ at start of filename. If second string passed begins with "/", this 
	assumes that the string is an entire path to a local volume (e.g. "/HardDrive/MyDirectory/picture.gif") and ignores
	the first string.  In some cases, d is directory; f is file*/
	public static String composePath(String d, String f) { //TODO: this isn't all correct.  May work reasonably well for local, but for web URL's won't work
		if (d==null) 
			return f;
		else if (f==null)
			return d;
		int count=0;
		if (f.startsWith("http://")) //NOTE: Assumes if first is / then is /hardDrive/directory...etc.
			return f;
		if (f.startsWith("https://")) //NOTE: Assumes if first is / then is /hardDrive/directory...etc.
			return f;
		if (f.startsWith("file://")) //NOTE: Assumes if first is / then is /hardDrive/directory...etc.
			return f;
		if (f.startsWith("/")) //NOTE: Assumes if first is / then is /hardDrive/directory...etc.
			return f;
		if (f.indexOf(":\\") == 1) //NOTE: Assumes if second is : and third is \ that it's a windows full path
			return f;
		while (d.endsWith("../")){
			count++;
			d = d.substring(0, d.length()-3);
		}
		while (f.startsWith("../")){
			count++;
			f = f.substring(3, f.length());
		}
		if (d.endsWith(fileSeparator)) {//count>0 && 
			d = d.substring(0, d.length()-1);
		}
		while (count>0 && d.length()>0) {
			count--;
			d = StringUtil.getAllButLastItem(d, fileSeparator);  
		}
		return d+fileSeparator + f;
	}
	/*-------------------------------------------------------*/
	/** Converts absolute to relative string for path*/
	public static String decomposePath(String relativeTo, String path) {
		if (relativeTo==null) 
			return path;
		else if (path==null)
			return relativeTo;
		int count;
		int lastSlashCount=0;

		for (count = 0; count< relativeTo.length() && count<path.length() && relativeTo.charAt(count)==path.charAt(count); count++){
			if (relativeTo.charAt(count)=='/' ||containedAt(relativeTo, fileSeparator, count))
				lastSlashCount = count+1;  // we will want to take the part from just past the slash onward
		}
		if (lastSlashCount==0)
			return path;  
		//count is now at first character at which they don't match.  Eat up first part of both and set relative.
		path = path.substring(lastSlashCount, path.length());
		relativeTo = relativeTo.substring(lastSlashCount, relativeTo.length());
		//count number of "/" in relativeTo
		count = 0;
		for (int i = 0; i<relativeTo.length(); i++)
			if (relativeTo.charAt(i)== '/' ||containedAt(relativeTo, fileSeparator, i))
				count++;
		for (int i=0; i<count; i++)
			path = "../" + path;

		return StringUtil.replace(path, fileSeparator, "/");
	}
	/*-------------------------------------------------------*/
	static boolean containedAt(String container, String contained, int where){
		if (container == null || contained == null || where>= container.length())
			return false;
		int i;  //counter in container
		int j;  //counter in contained
		for (i = where, j=0; j<contained.length() && i<container.length(); i++, j++)
			if (contained.charAt(j) != container.charAt(i))
				return false;
		return j == contained.length();  //reached the end of contained
	}
	/*-------------------------------------------------------*/
	/** converts an existing local file path to a URL string.  See also StringUtil.encodeForURL*/
	public static String massageStringToFilePathSafe(String path) {
		if (StringUtil.blank(path))
			return path;
		path = StringUtil.replace(path, ':', '|');
		path = StringUtil.replace(path, '/', '|');
		path = StringUtil.replace(path, '\\', '|');
		path = StringUtil.replace(path, '\'', '|');
		path = StringUtil.replace(path, '"', '|');
		return path;

	}
	/*-------------------------------------------------------*/
	/** converts an existing local file path to a URL string.  See also StringUtil.encodeForURL*/
	public static String massageFilePathToURL(String path) {
		if (StringUtil.blank(path))
			return path;
		try {
			File f = new File(path);
			if (f.exists()) {
				String s = f.toURL().toString();  //errors caught in case this isn't a 1.4 VM
				return s;
			}
		}
		catch (Throwable t){
		}
		path = StringUtil.replaceFirst(path, "://", "|/"); //a kludge for problems under Windows
		path = StringUtil.replaceFirst(path, ":\\\\", "|\\"); //a kludge for problems under Windows
		path = StringUtil.replace(path, ':', '|');//a kludge for problems under Windows
		if (path.charAt(0)!='/')
			return "file:///" + path;
		else
			return "file://" + path;
		/*	try {
			URL url = new URL("file", "", path);
			q = url.toString();
		}
		catch (MalformedURLException e){
		}
		return q;
		 */
	}
	/*.................................................................................................................*/
	/** Links progress indicator*/
	public void linkProgressIndicator(ProgressIndicator pi){
		if (progIndicator!=null)
			progIndicator.goAway();
		progIndicator = pi;
		if (progIndicator!=null)
			progIndicator.setTotalValue(existingLength());
	}
	/*.................................................................................................................*/
	/** Returns true if user has requested file reading or writing stop.*/
	public boolean getFileAborted(){
		if (progIndicator !=null) {
			return progIndicator.isAborted();
		}
		return false;
	}
	/*.................................................................................................................*/
	/** Update the progress window to current filePos.*/
	private void updateProgress(){
		if (progIndicator !=null) {
			progIndicator.setCurrentValue(filePos);
		}

	}
	/*.................................................................................................................*/
	byte[] bytes = new byte[4096];
	int currentByte = 0;
	int bytesAvailable = 0;
	byte[] oneByte = new byte[1];
	String bytesString = null;
	private int readNextChar() throws IOException {
		int b = 0;
		byte by = 0;

		if (bytesAvailable == -1) {
			currentByte = 0;
			return -1;
		}
		else if (currentByte>=bytesAvailable){
			currentByte = 0;
			bytesAvailable = inStream.read(bytes);

			if (bytesAvailable <=0)
				return -1;
			filePos++;
			by = bytes[currentByte++];
			b = (int) by;

		}
		else  {
			filePos++;
			by = bytes[currentByte++];
			b = (int) by;
		}
		if (b<0){  
			// high ASCII; must fiddle to get it into default encoding.
			// needs to be manipulated because bytes are unsigned in java
			// this masks the right most 8 bits
			b = (b & 0xff);
		}
		return b;

	}
	private int ALTreadNextChar() throws IOException {
		int c = inStream.read();
		filePos++;
		return c;
	}
	/*.................................................................................................................*/
	/** read next line from file, return as String. */
	public String readLine(String endLine) {
		if (endLine == null || endLine.length() ==0)  {
			if ( readLine(sB))
				return sB.toString();
			else
				return null;
		}
		sB.setLength(0);
		if (inStream==null)
		{return null;
		}
		else	{
			try {
				if (remnantString.length() >0) {
					sB.append(remnantString.toString());
				}
				remnantString.setLength(0);
				boolean done = false;
				readLineTimer.start();
				int c=0;
				while (c!= -1) {
					c= readNextChar();

					if (c == endLine.charAt(0)){// || oldPos == pos)
						remnantString.append((char)c);
						//appendToStringBuffer(remnantString, c);
						c = -1;
						int k;
						int index = 0;
						do{ 
							index++;
							k= readNextChar();
							if (k== -1){
								done = true;
							}
							else 
								remnantString.append((char)k);
							//appendToStringBuffer(remnantString, k);
						}
						while  (index<endLine.length() && k == endLine.charAt(index) && !done);
						if (index>= endLine.length() && !done) {
							remnantString.setLength(0);
							//	appendToStringBuffer(remnantString, k);
							remnantString.append((char)k);
						}
					}
					else if (c!= -1)
						//	appendToStringBuffer(sB, c);
						sB.append((char)c);
					else { // end of stream had been reached
						done = true;
						remnantString.setLength(0);
					}

				}
				readLineTimer.end();
				String s = sB.toString();
				if (done && StringUtil.blank(s)) {
					updateProgress();
					return null;
				}
				updateProgress();
				return s;
			}
			catch( FileNotFoundException e ) {
				MesquiteModule.mesquiteTrunk.discreetAlert( MesquiteThread.isScripting(), "File Busy or Not Found  (3): \ndirectory <" + directoryName + "> \nfile <" + fileName + ">" );
				//MesquiteMessage.printStackTrace();
			} 
			catch( IOException e ) {
				MesquiteModule.mesquiteTrunk.discreetAlert( MesquiteThread.isScripting(), "IO exception in readLine for <" + directoryName + "> \nfile <" + fileName + "> " + e.getMessage() );
				//MesquiteMessage.printStackTrace();
			}
			updateProgress();
			return null;
		}
	}
	/*.................................................................................................................*/
	int totalWaitingAppend = 100;
	char[] toAppend = new char[totalWaitingAppend];
	int readyToAppend = 0;

	/** read next line from file, return as String. */
	public boolean readLine(StringBuffer buffer) {	
		buffer.setLength(0);
		if (inStream==null) {
			return false;
		}
		else	{
			try {
				readyToAppend = 0;

				int c=0;
				if (remnantString.length() >0) {
					int k= remnantString.charAt(0);
					if (k == '\n' || k == '\r') {//last remnant was a new line; thus done
						c = -1;
					}
					else {
						StringUtil.append(buffer, remnantString);
					}
				}
				remnantString.setLength(0);
				boolean done = false;
				readLineTimer.start();
				while (c!= -1) {
					c= readNextChar();
					if (c == '\n' || c == '\r'){  //end of line character
						int k= readNextChar(); //get to next character to see if a different end of line character (e.g. DOS file)
						if (k== -1){ //end of file
							done = true;
							remnantString.setLength(0);
						}
						else if (k == '\n' || k == '\r') { //next is same end of line character
							if (k==c) {
								remnantString.setLength(0);
								//	appendToStringBuffer(remnantString, k);
								remnantString.append((char)k);
								c = -1; //done line
							}
							//c=k;
						}
						else { //next is not end of line character
							remnantString.setLength(0);
							//appendToStringBuffer(remnantString, k);
							remnantString.append((char)k);
							//c=k;
						}
						c = -1;
					}
					else if (c!= -1) {
						toAppend[readyToAppend++] = (char)c;
						if (readyToAppend>= totalWaitingAppend) {
							buffer.append(toAppend, 0, (readyToAppend));
							readyToAppend = 0;
						}
					}
					else { // end of stream had been reached
						done = true;
						remnantString.setLength(0);
					}
				}
				readLineTimer.end();
				if (readyToAppend>0){
					buffer.append(toAppend, 0, (readyToAppend));
					readyToAppend = 0;
				}
				if (done && StringUtil.blank(buffer)) {
					updateProgress();
					return false;
				}
				updateProgress();
				return true;
			}
			catch( FileNotFoundException e ) {
				MesquiteModule.mesquiteTrunk.discreetAlert( MesquiteThread.isScripting(), "File Busy or Not Found  (3): \ndirectory <" + directoryName + "> \nfile <" + fileName + ">" );
				//MesquiteMessage.printStackTrace();
			} 
			catch( IOException e ) {
				MesquiteModule.mesquiteTrunk.discreetAlert( MesquiteThread.isScripting(), "IO exception in readLine for <" + directoryName + "> \nfile <" + fileName + "> " + e.getMessage() );
				//MesquiteMessage.printStackTrace();
			}
			catch( OutOfMemoryError e ) {
				MesquiteModule.mesquiteTrunk.discreetAlert( MesquiteThread.isScripting(), "OutOfMemoryError in readLine (sB.length()  " + sB.length() + " remnantString.length() " + remnantString.length()+ ") .  See file memory.txt in the Mesquite_Folder." );
				//MesquiteMessage.printStackTrace();
			}
			updateProgress();
		}
		return false;
	}
	/** read next line from file, return as String. */
	public String readLine() {	
		sB.setLength(0);
		if (inStream==null) {
			return null;
		}
		else	{
			try {
				readyToAppend = 0;

				int c=0;
				if (remnantString.length() >0) {
					int k= remnantString.charAt(0);
					if (k == '\n' || k == '\r') {//last remnant was a new line; thus done
						c = -1;
					}
					else {
						StringUtil.append(sB, remnantString);
					}
				}
				remnantString.setLength(0);
				boolean done = false;
				readLineTimer.start();
				while (c!= -1) {
					c= readNextChar();
					if (c == '\n' || c == '\r'){  //end of line character
						int k= readNextChar(); //get to next character to see if a different end of line character (e.g. DOS file)
						if (k== -1){ //end of file
							done = true;
							remnantString.setLength(0);
						}
						else if (k == '\n' || k == '\r') { //next is same end of line character
							if (k==c) {
								remnantString.setLength(0);
								//appendToStringBuffer(remnantString, k);
								remnantString.append((char)k);
								c = -1; //done line
							}
							//c=k;
						}
						else { //next is not end of line character
							remnantString.setLength(0);
							//appendToStringBuffer(remnantString, k);
							remnantString.append((char)k);
							//c=k;
						}
						c = -1;
					}
					else if (c!= -1) {
						toAppend[readyToAppend++] = (char)c;
						if (readyToAppend>= totalWaitingAppend) {
							sB.append(toAppend, 0, (readyToAppend));
							readyToAppend = 0;
						}
					}
					else { // end of stream had been reached
						done = true;
						remnantString.setLength(0);
					}
				}
				readLineTimer.end();
				if (readyToAppend>0){
					sB.append(toAppend, 0, (readyToAppend));
					readyToAppend = 0;
				}
				if (done && StringUtil.blank(sB)) {
					updateProgress();
					return null;
				}
				updateProgress();
				return sB.toString();
			}
			catch( FileNotFoundException e ) {
				MesquiteModule.mesquiteTrunk.discreetAlert( MesquiteThread.isScripting(), "File Busy or Not Found  (3): \ndirectory <" + directoryName + "> \nfile <" + fileName + ">" );
				//MesquiteMessage.printStackTrace();
			} 
			catch( IOException e ) {
				MesquiteModule.mesquiteTrunk.discreetAlert( MesquiteThread.isScripting(), "IO exception in readLine for <" + directoryName + "> \nfile <" + fileName + "> " + e.getMessage() );
				//MesquiteMessage.printStackTrace();
			}
			catch( OutOfMemoryError e ) {
				MesquiteModule.mesquiteTrunk.discreetAlert( MesquiteThread.isScripting(), "OutOfMemoryError in readLine (sB.length()  " + sB.length() + " remnantString.length() " + remnantString.length()+ ") " );
				//MesquiteMessage.printStackTrace();
			}
			updateProgress();
		}
		return null;
	}
	/*.................................................................................................................*/
	/** read next line from file, return as String. */
	public static String readLine(InputStream inStream, StringBuffer sBuff, MesquiteInteger remnantChar) {	
		return readLine(inStream, sBuff, remnantChar, null);
	}
	/*.................................................................................................................*/
	/** read next line from file, return as String. */
	public static String readLine(InputStream inStream, StringBuffer sBuff, MesquiteInteger remnantChar, MesquiteLong fPOS) {	
		sBuff.setLength(0);
		if (inStream==null)
		{return null;
		}
		else	{
			try {
				if (remnantChar.getValue()>=0)
					sBuff.append((char)remnantChar.getValue());
				remnantChar.setValue(-1);
				boolean done = false;
				int c=0;
				while (c!= -1) {
					c= inStream.read();
					if (fPOS != null)
						fPOS.increment();
					if (c == '\n' || c == '\r'){// || oldPos == pos)
						int k= inStream.read();
						if (fPOS != null)
							fPOS.increment();
						if (!((k == '\n' || k == '\r') && k!=c)) {
							remnantChar.setValue(k);
							//c=k;
						}
						c = -1;
					}
					else if (c!= -1)
						sBuff.append((char)c);
					else // end of stream had been reached
						done = true;

				}
				String s = sBuff.toString();
				if (done && StringUtil.blank(s)) { 
					//updateProgress();
					return null;
				}
				//updateProgress();
				return s;
			}
			catch( IOException e ) {MesquiteModule.mesquiteTrunk.discreetAlert( MesquiteThread.isScripting(),"IO exception in readLine(2) " + e.getMessage() );}
			//updateProgress();
			return null;
		}
	}
	/*.................................................................................................................*/
	/** read next dark line from file, return as String. */
	public String readNextDarkLine() {	
		boolean line = readLine(sB);
		while (StringUtil.blank(sB)) {
			if (!line)
				break;
			line = readLine(sB); 
		}
		return sB.toString();
	}
	MesquiteBoolean suppressComments = new MesquiteBoolean(false);
	/*.................................................................................................................*/

	/** Returns next token from file.  Note that firstToken is used for the first token in a command *
	public String nextToken(StringBuffer commentBuffer){	
			pendingBrackets.setValue(0);  //level of nesting of incompleteness of comment square brackets
			suppressComments.setValue(false);
			String token="";
			if (!bufferRead) {
				bufferRead =readLine(parser.getBuffer());
				parser.setPosition(0);
				if (!StringUtil.blank(parser.getBuffer())) 
					token = parser.getUnalteredToken(true, pendingBrackets, commentBuffer, suppressComments);
			}
			else
				token = parser.getUnalteredToken(true, pendingBrackets, commentBuffer, suppressComments); //was false
			boolean bracketEnd = false;
				//if [ found, and not closed, or if no tokens in remaining part of bufferstring, then need to continue reading lines
			while (bufferRead && (StringUtil.blank(token) || pendingBrackets.getValue()>0)) {
				boolean wasPending = (pendingBrackets.getValue()>0);
				if (StringUtil.blank(token)) {// no tokens in remaining part of bufferString
					bufferRead=readLine(parser.getBuffer());
					parser.setPosition(0);
					token = StringUtil.lineEnding();
				}
				else {
					parser.setPosition(0);
					if (!StringUtil.blank(parser.getBuffer())) 
						token = parser.getUnalteredToken(true, pendingBrackets, commentBuffer, suppressComments); //was false
				}
				bracketEnd = (wasPending && pendingBrackets.getValue()==0);
			}
			if (token==null)
				token = "";
			if (parser.getBuffer().length()==0)
				bufferRead = false;
			return token;
	}
	 */

	/*.................................................................................................................*/
	public void setTranslatedCharacter(char fromChar, char toChar){
		if (parser != null)
			parser.setTranslatedCharacter(fromChar, toChar);
	}
	public void clearTranslatedCharacter(char fromChar){
		if (parser != null)
			parser.clearTranslatedCharacter(fromChar);
	}

	/*.................................................................................................................*/
	/** Returns next token from file. Used for first token of command, to preserves leading whitespace (used to preserve
	leading tabs/spaces for blocks such as the MesquiteBlock).*/
	public String firstToken(StringBuffer commentBuffer){	
		//TODO: getUnalteredToken stops at the end of a line.  Should have pendingQuote to indicate it must keep going until quote used up
		pendingBrackets.setValue(0);  //level of nesting of incompleteness of comment square brackets
		suppressComments.setValue(false);
		String token="";

		if (!bufferRead) {
			bufferRead=readLine(parser.getBuffer());
			parser.setPosition(0);
			if (!StringUtil.blank(parser.getBuffer())) 
				token = parser.getUnalteredToken(true, pendingBrackets, commentBuffer, suppressComments);
		}
		else
			token = parser.getUnalteredToken(true, pendingBrackets, commentBuffer, suppressComments);



		//if [ found, and not closed, or if no tokens in remaining part of bufferstring, then need to continue reading lines
		while (bufferRead && (StringUtil.blank(token) || pendingBrackets.getValue()>0)) {
			//TODO: why did automatically start reading new bufferString instead of continuing to use old one?
			if (StringUtil.blank(token)) {// no tokens in remaining part of bufferString
				bufferRead=readLine(parser.getBuffer());
				parser.setPosition(0);
			}
			if (!StringUtil.blank(parser.getBuffer())) 
				token = parser.getUnalteredToken( true, pendingBrackets, commentBuffer, suppressComments);
		}
		if (token==null)
			token = "";
		return token;
	}
	/*.................................................................................................................*/
	private boolean emptyToken(String token) {
		return token == null || token.length() ==0;
		//return StringUtil.blank(token);   
	}
	/*.................................................................................................................*/

	/** Returns next token from file.  Note that firstToken is used for the first token in a command */
	public String nextToken(StringBuffer commentBuffer){	
		pendingBrackets.setValue(0);  //level of nesting of incompleteness of comment square brackets
		String token=null;
		suppressComments.setValue(false);
		if (!bufferRead) {
			bufferRead =readLine(parser.getBuffer());
			parser.setPosition(0);
			if (bufferRead) 
				token = parser.getUnalteredToken(true, pendingBrackets, commentBuffer, suppressComments); //was false
			else
				token = "";
		}
		else
			token = parser.getUnalteredToken(true, pendingBrackets, commentBuffer, suppressComments); //was false

		boolean bracketEnd = false;
		//if [ found, and not closed, or if no tokens in remaining part of bufferstring, then need to continue reading lines
		while (bufferRead && (emptyToken(token) || pendingBrackets.getValue()>0)) {
			boolean wasPending = (pendingBrackets.getValue()>0);
			if (emptyToken(token)) {// no tokens in remaining part of bufferString
				bufferRead=readLine(parser.getBuffer());

				parser.setPosition(0);
				if (pendingBrackets.getValue()>0) //in this case, would reenter loop and wipe out the current line because token will be blank
					token = parser.getUnalteredToken(true, pendingBrackets, commentBuffer, suppressComments); //was false
				else			
					token = StringUtil.lineEnding();
				//token ="";
			}
			else if (StringUtil.blank(token)) {   // added this section 22 Oct 07 to deal with bracketed comments split over multiple lines
				bufferRead =readLine(parser.getBuffer());
				parser.setPosition(0);
				if (bufferRead) 
					token = parser.getUnalteredToken(true, pendingBrackets, commentBuffer, suppressComments); //was false
				else
					token = "";
			}
			else { 
				parser.setPosition(0);
				if (!StringUtil.blank(parser.getBuffer())) 
					token = parser.getUnalteredToken(true, pendingBrackets, commentBuffer, suppressComments); //was false
			}
			bracketEnd = (wasPending && pendingBrackets.getValue()==0);
		}
		if (token==null)
			token = "";
		return token;
	}


	/*.................................................................................................................*/
	/** returns the next command in the file.  Extra information about the command is
	returned in the MesquiteInteger status.  If the command is the "begin", 1 is returned; if "end;", 2 is returned.
	Otherwise 0 is returned.   
	If includeEntireCommand is true, then the entire command is returned; if false, the only the first token (command name) is returned.
	(Returning only part of the command can save time if the commands are long (e.g., a TREE) and not needed. */
	public String getNextCommand(MesquiteInteger status, StringBuffer commandComments, boolean includeEntireCommand) {
		getNextCommandStatusTimer.start();
		StringBuffer command= new StringBuffer(300);
		betweenCommandComments.setLength(0);
		if (commandComments !=null)
			commandComments.setLength(0);
		String token = firstToken(betweenCommandComments);


		if (StringUtil.blank(token)) {
			return token;
		}
		if (ParseUtil.darkBeginsWithIgnoreCase(token, "Begin"))
			status.setValue(1);
		else
			status.setValue(0);

		if (!emptyToken(token)) {
			command.append(token);  // this is the command name
			while (!emptyToken(token) && !token.equals(";")) {
				token = nextToken(commandComments);
				if (includeEntireCommand && !emptyToken(token)) {
					if (token.charAt(0)!=';' && !parser.whitespace(token.charAt(0))) 
						command.append(' ');
					command.append(token);
				}
			}
		}
		String result = command.toString();
		if (ParseUtil.darkBeginsWithIgnoreCase(result, "end;") || ParseUtil.darkBeginsWithIgnoreCase(result, "endblock;"))
			status.setValue(2);
		getNextCommandStatusTimer.end();
		return result;
	}

	/*.................................................................................................................*/
	/** returns the next command in the file.  Extra information about the command is
	returned in the MesquiteInteger status.  If the command is the "begin", 1 is returned; if "end;", 2 is returned.
	Otherwise 0 is returned. */
	public String getNextCommand(MesquiteInteger status, StringBuffer commandComments) {
		return getNextCommand(status,commandComments, true);
	}

	/*.................................................................................................................*/
	/** skips the next command in the file.  Extra information is
	returned in the MesquiteInteger status.  If the command is the "begin", 1 is returned; if "end;", 2 is returned.
	Otherwise 0 is returned. */
	public void skipNextCommand(MesquiteInteger status) {
		String token =firstToken(null);
		if (StringUtil.blank(token)) 
			return;

		if (ParseUtil.darkBeginsWithIgnoreCase(token, "Begin"))
			status.setValue(1);
		else
			status.setValue(0);
		String lastT = null;
		if (!emptyToken(token)) {
			while (token!=null && !token.equals(";")) {
				lastT = token;
				token = nextToken(null);
			}
		}
		if (ParseUtil.darkBeginsWithIgnoreCase(lastT, "end;") || ParseUtil.darkBeginsWithIgnoreCase(lastT, "endblock;"))
			status.setValue(2);
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	MesquiteInteger pos = new MesquiteInteger(0);
	/** returns the next block; blockName is passed back via the MesquiteString object */
	public FileBlock getNextBlockOLD(MesquiteString blockName, StringBuffer fileComments, StringBuffer blockComments) {
		nextBlockTimer.start();
		FileBlock block = new FileBlock();
		if (blockComments!=null)
			blockComments.setLength(0);
		MesquiteInteger status = new MesquiteInteger(0);
		StringBuffer withinCommandComments = new StringBuffer(10);
		StringBuffer command = new StringBuffer(getNextCommand(status, withinCommandComments));
		if (betweenCommandComments.length()>0 && fileComments!=null) {
			String bcc = betweenCommandComments.toString();
			if (ParseUtil.darkBeginsWithIgnoreCase(bcc, "!"))
				fileComments.append(bcc.substring(bcc.indexOf('!')+1, bcc.length())+ StringUtil.lineEnding());
		}
		if (!StringUtil.blank(command)) {
			if (status.getValue()==0) {
				MesquiteMessage.println("last command " + Integer.toString(command.length()) + ": " + command);
			}
			else {
				Parser nameParser = new Parser();
				String bName =nameParser.getTokenNumber(command.toString(), 2); //resets string!
				blockName.setValue(bName);
				block.addCommand(command.toString(), withinCommandComments.toString());
				//block.append(StringUtil.lineEnding());
				command.setLength(0);
				String temp = getNextCommand(status, withinCommandComments);
				if (betweenCommandComments.length()>0 && blockComments!=null) {
					String bcc = betweenCommandComments.toString();
					if (ParseUtil.darkBeginsWithIgnoreCase(bcc, "!"))
						blockComments.append(bcc.substring(bcc.indexOf('!')+1, bcc.length())+ StringUtil.lineEnding());
				}
				//here withinCommandComments would contain the command comments, but there's no place to put them
				command.append(temp);
				if (!StringUtil.blank(command)) {
					block.addCommand(command.toString(), withinCommandComments.toString());
					long numCommands = 0; //added 21 Sept 01
					while (!StringUtil.blank(command) && status.getValue()!=2) {
						numCommands ++;
						if (numCommands % 1000 == 0) {
							Runtime.getRuntime().gc(); //added 21 Sept 01
						}
						command.setLength(0);
						String c = getNextCommand(status, withinCommandComments);
						if (betweenCommandComments.length()>0 && blockComments!=null) {
							String bcc = betweenCommandComments.toString();
							if (ParseUtil.darkBeginsWithIgnoreCase(bcc, "!"))
								blockComments.append(bcc.substring(bcc.indexOf('!')+1, bcc.length())+ StringUtil.lineEnding());
						}
						//here withinCommandComments would contain the command comments, but there's no place to put them
						if (!StringUtil.blank(c)) {
							command.append(c);
							block.addCommand(command.toString(), withinCommandComments.toString());
						}

					}
				}
			}
		}
		nextBlockTimer.end();
		return block;
	}
	/*.................................................................................................................*/
	/** returns the next block; blockName is passed back via the MesquiteString object */
	public FileBlock getNextBlock(MesquiteString blockName, StringBuffer fileComments, StringBuffer blockComments) {
		nextBlockTimer.start();
		return new FileBlock(this,  blockName,  fileComments, blockComments);
	}

	/*.................................................................................................................*/
	public long getFilePosition(){

		return filePos;
	}

	/*.................................................................................................................*/
	public boolean atEOF(){
		return filePos >= existingLength();
	}
	/*.................................................................................................................*/
	public void goToFilePosition(long pos){
		if (!MesquiteLong.isCombinable(pos)) {
			MesquiteMessage.println("Uncombinable goToFilePosition (" + pos + ")");
			return;
		}
		bufferRead = false;
		currentByte = 0;
		bytesAvailable = 0;
		if (remnantString != null)
			remnantString.setLength(0);
		if (parser != null)
			parser.setString("");
		try {
			if (inStream == null)
				return;
			if (pos!=filePos) {
				closeReading();
				openReading();
				inStream.skip(pos);
			}
			else if (pos == filePos){
			}
			filePos = pos;
		}
		catch (IOException e){
			MesquiteMessage.println("ioe goToFilePosition (" + pos + ")");
			filePos = -1;
			MesquiteFile.throwableToLog(this, e);
		}
		catch (Throwable e){
			filePos = -1;
		}

	}
	/*.................................................................................................................*/
	/** returns the file position of the start of the next block; returns blockName */
	public String goToNextBlockStart(MesquiteLong startPos) {
		MesquiteInteger status = new MesquiteInteger(0);
		StringBuffer command = new StringBuffer(getNextCommand(status, null)); //������������

		if (!StringUtil.blank(command)) {
			if (status.getValue()==0) {  //in middle of block; need to continue reading until get to end of block
				while (!StringUtil.blank(command) && status.getValue()!=2) {
					command.setLength(0);
					String c = getNextCommand(status, null);
					if (!StringUtil.blank(c)) 
						command.append(c);
				}
				//reached end of this block, hence mark position for resetting
				if (startPos !=null)
					startPos.setValue(getFilePosition()+1);

				//at end of block; now get next to find first part of next block
				command.setLength(0);
				String c = getNextCommand(status, null);
				if (!StringUtil.blank(c)) 
					command.append(c);
			} else {
				if (!startPos.isCombinable())
					startPos.setValue(getFilePosition()+1);
			}

			Parser nameParser = new Parser();
			return nameParser.getTokenNumber(command.toString(), 2); 
		}
		return null;
	}
	/*.................................................................................................................*/
	/** Adds the passed FileElement to the file. */
	public void addFileElement(FileElement element) {
		if (fileElements == null)
			fileElements = new ListableVector( 1);
		fileElements.addElement(element, true);
	}
	/*.................................................................................................................*/
	/** Removes the passed FileElement from the file. */
	public void removeFileElement(FileElement element) {
		if (fileElements != null) {
			if (element !=null)
				fileElements.removeElement(element, true);
		}
	}
	/*.................................................................................................................*/
	/** Gets list of file elements. */
	public ListableVector getFileElements() {
		return fileElements;
	}
	/*.................................................................................................................*/
	/** appends a number to the end of the path until it finds that no file or directory does not exist at that path */
	public static String getUniqueNumberedPath(String path) {
		if (!fileOrDirectoryExists(path+"1"))
			return path+"1";

		int count = 1;
		while (fileOrDirectoryExists(path+count)) {
			count++;

		}
		return path + count;
	}	
	/*.................................................................................................................*/
	/** appends a number to the end of the path until it finds that no file or directory does not exist at that path */
	public static String getUniqueModifiedPath(String path) {
		if (!fileOrDirectoryExists(path))
			return path;
		int count = 0;
		while (fileOrDirectoryExists(path)) {
			path+=""+(count++);
		}
		return path;
	}	
	/*.................................................................................................................*/
	/** appends a number to the end of the path until it finds that no file or directory does not exist at that path */
	public static String getUniqueModifiedFileName(String path, String extension) {
		if (!fileOrDirectoryExists(path + "." + extension))
			return path + "." + extension;
		int count = 0;
		while (fileOrDirectoryExists(path + count + "." + extension)) {
			count++;
		}
		return path + count + "." + extension;
	}	
	/*.................................................................................................................*/
	/** deletes file at path */
	public static boolean deleteFile(String path) {
		if (fileExists(path) && canWrite(path)) {
			File testing = new File(path);
			testing.delete();
			return true;
		}
		return false;
	}	

	/*.................................................................................................................*/
	public static boolean deleteDirectory(String directoryPath){
		if (StringUtil.blank(directoryPath))
			return false;
		try {
			File directory = new File(directoryPath);
			String sep = "";
			if (!directoryPath.endsWith(MesquiteFile.fileSeparator))
				sep=MesquiteFile.fileSeparator;
			if (directory!=null && directory.isDirectory()) {
				String[] files = directory.list();
				for (int i=0; i<files.length; i++) {
					String path = directoryPath + sep + files[i];
					File f = new File(path);
					if (f.isDirectory()) {
						boolean success = deleteDirectory(path);
						if (!success)
							return false;
					}
					else
						if (MesquiteFile.fileExists(path)) {
							f.delete();
						}
				}
				directory.delete();
			}
		} catch (SecurityException e){
			MesquiteMessage.println("Directory could not be deleted: " + directoryPath);
			return false;
		}
		return true;
	}
	/*.................................................................................................................*/
	/** Returns last modified time of file.*/
	public static long fileOrDirectoryLastModified(String path) {
		if (path != null) {
			if (path.indexOf("//")>=0)
				MesquiteMessage.printStackTrace("double // in path " + path); 
			File testing = new File(path);
			return testing.lastModified();
		}
		return 0;
	}	
	/*.................................................................................................................*/
	/** Checks to see if path leads to a directory OR file*/
	public static boolean fileOrDirectoryExists(String path) {
		if (path != null) {
			if (path.indexOf("//")>=0)
				MesquiteMessage.printStackTrace("double // in path " + path);
			File testing = new File(path);
			if (testing.exists())
				return true;
		}
		return false;
	}	

	public static boolean fileExists(String directoryName, String fileName){ //Dec 2013.  Why was this appending existing directory to suggested?  Was generating double // errors
		if (fileExists(directoryName+ fileName))  
			return true;
		else if (StringUtil.blank(directoryName))
			return fileExists(MesquiteTrunk.suggestedDirectory +fileName);
		else if (!(directoryName.endsWith(fileSeparator) || directoryName.endsWith("/"))) 
			return fileExists(directoryName + fileSeparator +fileName);

		return false;
	}

	/*.................................................................................................................*/
	/** Checks to see if path leads to a file that is not a directory*/
	public static boolean fileExists(String path) {
		if (path != null) {
			if (path.indexOf("//")>=0)
				MesquiteMessage.printStackTrace("double // in path " + path);  
			File testing = new File(path);
			if (testing.exists() && !testing.isDirectory())
				return true;
		}
		return false;
	}	
	/*.................................................................................................................*/
	/** Checks to see if path leads to a file that is not a directory*/
	public static String getAvailableFileName(String directoryName, String fileNameBase) {
		if (!fileExists(directoryName,fileNameBase))
			return fileNameBase;
		int counter=1;
		while (fileExists(directoryName,fileNameBase+counter))
			counter++;
		return fileNameBase+counter;
	}	
	/*.................................................................................................................*/
	/** Checks to see if path leads to a file that is not a directory*/
	public static String getAvailableFileName(String directoryName, String fileNameBase, String fileNameExtension) {
		if (!fileExists(directoryName,fileNameBase+fileNameExtension))
			return fileNameBase+fileNameExtension;
		int counter=1;
		while (fileExists(directoryName,fileNameBase+counter+fileNameExtension))
			counter++;
		return fileNameBase+counter+fileNameExtension;
	}	
	/*.................................................................................................................*/
	/** Checks to see if can write into the requested directory*/
	public boolean canWriteToDirectory() {
		if (!MesquiteTrunk.isApplet()) {
			File testing = new File(getDirectoryName());
			if (testing.canWrite())
				return true;
		}
		return false;
	}	
	/*.................................................................................................................*/
	/** Checks to see if can write into the requested directory*/
	public boolean canCreateOrRewrite() {
		if (!MesquiteTrunk.isApplet()) {
			File testing = new File(getPath());
			if (!testing.exists()){
				if (!canWriteToDirectory())
					return false;
				return true;
			}
			if (testing.canWrite())
				return true;
		}
		return false;
	}	
	/*.................................................................................................................*/
	/** Diagnoses why */
	public String diagnosePathIssues() {
		String s = "";
		if (MesquiteTrunk.isApplet()) 
			return "Mesquite is running as an Applet, and therefore is not permitted to save files";
		File file = new File(getPath());

		if (file.exists() && !file.canWrite())
			s += "The file exists but appears to be locked against writing.\n";
		File directory = new File(getDirectoryName());
		if (directory == null || !directory.exists())
			s += "The folder \"" + getDirectoryName() + "\" does not exist.\n";
		while (directory != null){
			if (directory.exists()){
				s += "The folder \"" + directory.getName() + "\" exists";
				if (directory.canWrite())
					s += ".\n";
				else
					s += " but appears to be locked against writing.\n";
			}
			else
				s += "The folder \"" + directory.getName() + "\" does not exist.\n";

			directory = directory.getParentFile();
		}
		return s;
	}	
	
	/*.................................................................................................................*/
	/** Checks to see if can write to a file*/
	public static boolean canWrite(String path) {
		if (path != null && !MesquiteTrunk.isApplet()) {
			File testing = new File(path);
			if (testing.canWrite())
				return true;
		}
		return false;
	}	
	/*.................................................................................................................*/
	/** renames file.  Used instead of File.renameTo because of bug in Windows*/
	public static boolean rename(String path, String newPath) {
		if (path != null && newPath != null) {
			File oldFile = new File(path);
			if (!oldFile.exists())
				return false;
			File newFile = new File(newPath);
			if (newFile.exists()){
				newFile.delete();
				int count = 0;
				while ((new File(newPath)).exists() && count++<10){ //wait for file system to clean up
					try{
						Thread.sleep(20);
					}
					catch (InterruptedException e){
						return false;
					}
				}
				if (count>= 10)
					return false;
			}
			int count = 0;
			File file = new File(path);
			while (!file.renameTo(new File(newPath)) && count++<10){ //wait for file system to clean up
				try{
					Thread.sleep(20);
				}
				catch (InterruptedException e){
					return false;
				}
			}
			if (count>= 10)
				return false;
			return true;
		}
		return false;
	}	
	/*.................................................................................................................*/
	/** Checks to see if path leads to a file; if so, path returned; if not, user is asked to choose new file.*/
	public static String checkFilePath(String path, String selectMessage) {
		String resultPath = path;
		boolean request=false;
		if (path == null)
			request = true;
		else {
			File testing = new File(path);
			if (!testing.exists())
				request = true;
		}

		if (request && !MesquiteThread.suppressInteractionAsLibrary) {
			MesquiteTrunk.mesquiteTrunk.alert(selectMessage);
			MainThread.incrementSuppressWaitWindow();
			MesquiteFileDialog fdlg= new MesquiteFileDialog(MesquiteTrunk.mesquiteTrunk.containerOfModule(), selectMessage, FileDialog.LOAD);
			fdlg.setResizable(true);
			fdlg.setBackground(ColorTheme.getInterfaceBackground());
			fdlg.setVisible(true);
			if (StringUtil.blank(fdlg.getDirectory()) || StringUtil.blank(fdlg.getFile())) {
				// fdlg.dispose();
				MainThread.decrementSuppressWaitWindow();
				return null;
			}
			resultPath=fdlg.getDirectory() + fdlg.getFile();
			// fdlg.dispose();
			MainThread.decrementSuppressWaitWindow();
		}
		return resultPath;
	}
	/*.................................................................................................................*/
	/** Returns the first line of the file.  path is relative to the root of the package heirarchy; i.e. for file in
	a module's folder, indicate "mesquite/modules/moduleFolderName/fileName" */
	public static String getFileFirstContents(String relativePath) {
		DataInputStream stream;
		StringBuffer sBb= new StringBuffer(100);
		MesquiteInteger remnant = new MesquiteInteger(-1);
		if (!MesquiteTrunk.isApplet()) {
			try {
				stream = new DataInputStream(new FileInputStream(relativePath));
				String newS = " ";
				while (newS != null) {
					newS =readLine(stream, sBb, remnant);
					if (!StringUtil.blank(newS))
						return newS;
				}
			}
			catch( FileNotFoundException e ) {
			} 
			catch( IOException e ) {
			}
		}
		else {/*
			if (url!=null) {
				try {
					stream = new DataInputStream(url.openStream());
					return stream.readEverything();
					}
				catch( IOException e ) {MesquiteModule.mesquiteTrunk.discreetAlert(MesquiteThread.isScripting(),"IO exception" );}
			}
		 */
		}

		return null;
	}
	/*.................................................................................................................*/
	private static String[] addStringToEnd(String[] strings, String s) {
		boolean stringAdded = false;
		for (int i = 0; i<strings.length; i++) {  //go through strings until you get to the first null one
			if (strings[i]==null) {
				strings[i]=s;
				stringAdded = true;
				return strings;
			}
		}
		if (!stringAdded) {  //need to move them down and add it at the end
			for (int i = 0; i<strings.length-1;i++) {
				strings[i]=strings[i+1];
			}
			strings[strings.length-1]=s;
		}
		return strings;
	}
	/*.................................................................................................................*/
	private static boolean someStrings(String[] strings) {
		for (int i = 0; i<strings.length-1;i++) {  //go through strings until you get to the first null one
			if (strings[i]!=null) {
				return true;
			}
		}
		return false;
	}
	/*.................................................................................................................*/
	private static String concatStrings(String[] strings) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i<strings.length;i++) {  //go through strings until you get to the first null one
			if (strings[i]!=null) {
				sb.append(strings[i]+StringUtil.lineEnding());
			}
		}
		return sb.toString();
	}
	/*.................................................................................................................*/
	/** Returns the last line of the file.  path is relative to the root of the package heirarchy; i.e. for file in
	a module's folder, indicate "mesquite/modules/moduleFolderName/fileName" */
	public static String getFileLastContents(String relativePath, int numLines) {
		DataInputStream stream;
		StringBuffer sBb= new StringBuffer(100);
		String lastS = null;
		String[] lastLines = new String[numLines];
		MesquiteInteger remnant = new MesquiteInteger(-1);
		if (!MesquiteTrunk.isApplet()) {
			try {
				stream = new DataInputStream(new FileInputStream(relativePath));
				String newS = " ";
				while (newS != null) {
					lastLines = addStringToEnd(lastLines, newS);
					newS =readLine(stream, sBb, remnant);
				}

				if (someStrings(lastLines)) {
					return concatStrings(lastLines);
				}
			}
			catch( FileNotFoundException e ) {
			} 
			catch( IOException e ) {
			}
		}
		else {
		}

		return null;
	}
	/*.................................................................................................................*/
	/** Returns the last line of the file.  path is relative to the root of the package heirarchy; i.e. for file in
	a module's folder, indicate "mesquite/modules/moduleFolderName/fileName" */
	public static String getFileLastContents(String relativePath) {
		DataInputStream stream;
		StringBuffer sBb= new StringBuffer(100);
		String lastS = null;
		MesquiteInteger remnant = new MesquiteInteger(-1);
		if (!MesquiteTrunk.isApplet()) {
			try {
				stream = new DataInputStream(new FileInputStream(relativePath));
				String newS = " ";
				while (newS != null) {
					lastS = newS;
					newS =readLine(stream, sBb, remnant);
				}
				if (!StringUtil.blank(lastS))
					return lastS;
			}
			catch( FileNotFoundException e ) {
			} 
			catch( IOException e ) {
			}
		}
		else {
		}

		return null;
	}
	/*.................................................................................................................*/
	/** Returns the last dark line of the file.  path is relative to the root of the package heirarchy; i.e. for file in
	a module's folder, indicate "mesquite/modules/moduleFolderName/fileName" */
	public static String getFileLastDarkLine(String relativePath) {
		DataInputStream stream;
		StringBuffer sBb= new StringBuffer(100);
		String lastS = null;
		MesquiteInteger remnant = new MesquiteInteger(-1);
		if (!MesquiteTrunk.isApplet()) {
			try {
				stream = new DataInputStream(new FileInputStream(relativePath));
				String lastDarkLine = null;
				String newS = " ";
				while (newS != null) {
					lastS = newS;
					newS =readLine(stream, sBb, remnant);
					if (!StringUtil.blank(newS)) 
						lastDarkLine=newS;
				}
				if (!StringUtil.blank(lastDarkLine))
					return lastDarkLine;
			}
			catch( FileNotFoundException e ) {
			} 
			catch( IOException e ) {
			}
		}
		else {
		}

		return null;
	}
	/*.................................................................................................................*/
	/** Returns the contents of the file.  path is relative to the root of the package heirarchy; i.e. for file in
	a module's folder, indicate "mesquite/modules/moduleFolderName/fileName" */
	public static String[] getFileContentsAsStrings(String relativePath) {

		return getFileContentsAsStrings(relativePath, true);
	}
	/*.................................................................................................................*/
	/** Returns the contents of the file.  path is relative to the root of the package heirarchy; i.e. for file in
	a module's folder, indicate "mesquite/modules/moduleFolderName/fileName" */
	public static String[] getFileContentsAsStrings(String relativePath, boolean notifyIfNotFound) {
		DataInputStream stream;
		Vector v = new Vector();
		String[] s = null;
		StringBuffer sBb= new StringBuffer(100);
		MesquiteInteger remnant = new MesquiteInteger(-1);
		if (!MesquiteTrunk.isApplet()) {
			try {
				stream = new DataInputStream(new FileInputStream(relativePath));
				String newS = " ";

				while (newS != null) {
					newS =readLine(stream, sBb, remnant);
					if (newS != null)
						v.addElement(newS);
				}
				if (v.size()!=0) {
					s = new String[v.size()];
					int count = 0;
					Enumeration e = v.elements();
					while (e.hasMoreElements()) {
						Object obj = e.nextElement();
						s[count]= (String)obj;
						count++;
					}
				}
			}
			catch( FileNotFoundException e ) {
				if (notifyIfNotFound){
					MesquiteMessage.warnProgrammer("File Busy or Not Found (5) : " + relativePath);
					MesquiteFile.throwableToLog(null, e);
				}
				return null;
			} 
			catch( IOException e ) {
				if (notifyIfNotFound){
					MesquiteMessage.warnProgrammer("IO Exception found (5): " + relativePath + "   " + e.getMessage());
					MesquiteFile.throwableToLog(null, e);
				}
				return null;
			}
			return s;
		}
		else {/*
			if (url!=null) {
				try {
					stream = new DataInputStream(url.openStream());
					return stream.readEverything();
					}
				catch( IOException e ) {MesquiteModule.mesquiteTrunk.discreetAlert(MesquiteThread.isScripting(), "IO exception" );}
			}
		 */
		}

		return null;
	}
	/*.................................................................................................................*/
	/** Returns the contents of the file.  path is relative to the root of the package heirarchy; i.e. for file in
	a module's folder, indicate "mesquite/modules/moduleFolderName/fileName" */
	public static String[][] getTabDelimitedTextFile(String relativePath, boolean warn) {
		DataInputStream stream;
		Vector v = new Vector();
		String[][] s = null;
		StringBuffer sBb= new StringBuffer(100);
		MesquiteInteger remnant = new MesquiteInteger(-1);
		if (!MesquiteTrunk.isApplet()) {
			try {
				stream = new DataInputStream(new FileInputStream(relativePath));
				String newS = " ";

				while (newS != null) {
					newS =readLine(stream, sBb, remnant);
					if (newS != null)
						v.addElement(newS);
				}
				if (v.size()!=0) {
					s = new String[v.size()][];
					int count = 0;
					Enumeration e = v.elements();
					while (e.hasMoreElements()) {
						Object obj = e.nextElement();
						s[count]= StringUtil.tabDelimitedTokensToStrings((String)obj);
						count++;
					}
				}
				stream.close();
			}
			catch( FileNotFoundException e ) {
				if (warn) MesquiteMessage.warnProgrammer("File Busy or Not Found (z5) : " + relativePath);
				return null;
			} 
			catch( IOException e ) {
				if (warn) MesquiteMessage.warnProgrammer("IO Exception found (z5): " + relativePath + "   " + e.getMessage());
				//MesquiteMessage.printStackTrace();
				return null;
			}
			return s;
		}
		else {		}

		return null;
	}
	/*.................................................................................................................*/
	/** Returns the contents of the file.  path is relative to the root of the package heirarchy; i.e. for file in
	a module's folder, indicate "mesquite/modules/moduleFolderName/fileName" */
	public static String[][] getTabDelimitedTextFile(String relativePath) {
		return getTabDelimitedTextFile(relativePath, true);
	}

	/*.................................................................................................................*/
	/** Returns the contents of the file.  path is relative to the root of the package heirarchy; i.e. for file in
	a module's folder, indicate "mesquite/modules/moduleFolderName/fileName".*/
	public static String getFileContentsAsStringNoWarn(String relativePath) {
		return getFileContentsAsString(relativePath, -1,100, false);
	}
	/*.................................................................................................................*/
	/** Returns the contents of the file.  path is relative to the root of the package heirarchy; i.e. for file in
	a module's folder, indicate "mesquite/modules/moduleFolderName/fileName".*/
	public static String getFileContentsAsString(String relativePath) {
		return getFileContentsAsString(relativePath, -1,100);
	}
	/*.................................................................................................................*/
	/** Returns the contents of the file, local or remote.  The parameter "maxCharacters"
	sets an upper limit on how many characters are read (if <0, then all characters read in)*/
	public String getFileContentsAsString(int maxCharacters) {
		StringBuffer sb = new StringBuffer(100);
		StringBuffer line = new StringBuffer(100);
		openReading();
		while (readLine(line) && (maxCharacters<0 ||  sb.length() <maxCharacters)){
			sb.append(line.toString());
			sb.append(StringUtil.lineSeparator);			
		}
		closeReading();
		return sb.toString();
	}
	/*.................................................................................................................*/
	/** Returns the contents of the file.  path is relative to the root of the package heirarchy; i.e. for file in
	a module's folder, indicate "mesquite/modules/moduleFolderName/fileName".  The parameter "maxCharacters"
	sets an upper limit on how many characters are read (if <0, then all characters read in)*/
	public static String getFileContentsAsString(String relativePath, int maxCharacters) {
		return getFileContentsAsString(relativePath,maxCharacters,100);
	}
	/*.................................................................................................................*/
	/** Returns the contents of the file.  path is relative to the root of the package heirarchy; i.e. for file in
	a module's folder, indicate "mesquite/modules/moduleFolderName/fileName".  The parameter "maxCharacters"
	sets an upper limit on how many characters are read (if <0, then all characters read in)*/
	public static String getFileContentsAsString(String relativePath, int maxCharacters, int startBufferSize) {
		return getFileContentsAsString(relativePath, maxCharacters, startBufferSize, true);
	}

	/*.................................................................................................................*/
	/** Returns the contents of the file.  path is relative to the root of the package heirarchy; i.e. for file in
	a module's folder, indicate "mesquite/modules/moduleFolderName/fileName".  The parameter "maxCharacters"
	sets an upper limit on how many characters are read (if <0, then all characters read in)*/
	public static String getFileContentsAsString(String relativePath, int maxCharacters, int startBufferSize, boolean warnIfProblem) {
		if (StringUtil.blank(relativePath))
			return "";
		try {
			File fin = new File(relativePath);
			FileInputStream fis = new FileInputStream(fin);
			BufferedReader in = new BufferedReader(new InputStreamReader(fis, "UTF-8"));  // why not ISO-8859-1? or something newer???
			int length = (int)fin.length();  //2. 71 restricting to maxCharacters
			if (maxCharacters>=0 && length>maxCharacters)
				length = maxCharacters;
			char[] chrArr = new char[length];
			int count=0;
			MesquiteTimer timer = new MesquiteTimer();
			timer.start();
			while(in.ready()==false) {
				if (timer.timeSinceVeryStart()>2000) {
					if (warnIfProblem)
						MesquiteMessage.warnProgrammer("File could not be read (6) : " + relativePath);
					return null;
				}
			}
			in.read(chrArr);
			in.close();
			return new String(chrArr);
		}
		catch( FileNotFoundException e ) {
			if (warnIfProblem)
				MesquiteMessage.warnProgrammer("File Busy or Not Found (6) : " + relativePath);
			//MesquiteMessage.printStackTrace();
			return null;
		} 
		catch( IOException e ) {
			if (warnIfProblem)
				MesquiteMessage.warnProgrammer("IO Exception found (6) : " + relativePath + "   " + e.getMessage());
			//MesquiteMessage.printStackTrace();
			return null;
		}
		catch( Exception e ) {
			if (warnIfProblem)
				MesquiteMessage.warnProgrammer("Other Exception found (6) : " + relativePath + "   " + e.getMessage());
			//MesquiteMessage.printStackTrace();
			return null;
		}
	}
	/*.................................................................................................................*/
	/** Returns the contents of the file.  path is relative to the root of the package heirarchy; i.e. for file in
	a module's folder, indicate "mesquite/modules/moduleFolderName/fileName".  The parameter "maxCharacters"
	sets an upper limit on how many characters are read (if <0, then all characters read in)*/
	public static String getFileContentsAsStringOld(String relativePath, int maxCharacters, int startBufferSize) {
		if (StringUtil.blank(relativePath))
			return "";
		DataInputStream stream;
		StringBuffer sBb= new StringBuffer(startBufferSize);
		StringBuffer s= new StringBuffer(startBufferSize);
		MesquiteInteger remnant = new MesquiteInteger(-1);
		if (!MesquiteTrunk.isApplet()) {
			try {
				stream = new DataInputStream(new FileInputStream(relativePath));
				String newS = " ";
				int count = 0;

				while (newS != null && (maxCharacters<0 || count<maxCharacters)) {
					newS =readLine(stream, sBb, remnant);  //TODO: this won't stop if very long lines
					if (newS!=null) {
						count += newS.length();
						s.append(newS + StringUtil.lineEnding());
					}
				}
			}
			catch( FileNotFoundException e ) {
				MesquiteMessage.warnProgrammer("File Busy or Not Found (6) : " + relativePath);
				//MesquiteMessage.printStackTrace();
				return null;
			} 
			catch( IOException e ) {
				MesquiteMessage.warnProgrammer("IO Exception found (6) : " + relativePath + "   " + e.getMessage());
				//MesquiteMessage.printStackTrace();
				return null;
			}
			return s.toString();
		}
		else {/*
			if (url!=null) {
				try {
					stream = new DataInputStream(url.openStream());
					return stream.readEverything();
					}
				catch( IOException e ) {MesquiteModule.mesquiteTrunk.discreetAlert(MesquiteThread.isScripting(), "IO exception" );}
			}
		 */
		}

		return null;
	}
	/*.................................................................................................................*/
	/** Returns the contents of the file at the url*/
	public static String getURLContentsAsString(String path, int maxCharacters) {
		return getURLContentsAsString(path, maxCharacters, true);
	}
	/*.................................................................................................................*/
	/** Returns the contents of the file at the url*/
	public static String getURLContentsAsString(String path, int maxCharacters, boolean warnIfProblem) {
		DataInputStream stream;
		StringBuffer sBb= new StringBuffer(100);
		StringBuffer s= new StringBuffer(100);
		MesquiteInteger remnant = new MesquiteInteger(-1);
		String firstFour = path.substring(0,4);
		boolean isHTTP = firstFour.equalsIgnoreCase("HTTP");
		URL url = null;
		try {
			url = new URL(path);
		}
		catch (MalformedURLException e){
			return null;
		}

		try {
			URLConnection urlConnection = url.openConnection();
			//		if (MesquiteTrunk.getJavaVersionAsDouble()>=1.5)
			//			url.setConnectTimeout(2000);
			InputStream inputStream = urlConnection.getInputStream();
			stream = new DataInputStream(inputStream);
			String newS = " ";
			int count = 0;

			while (newS != null && (maxCharacters<0 || count<maxCharacters)) {
				newS =readLine(stream, sBb, remnant);  //TODO: this won't stop if very long lines
				if (newS!=null) {
					count += newS.length();
					s.append(newS + StringUtil.lineEnding());
				}
			}
		}
		catch( IOException e ) {
			if (warnIfProblem) {
				MesquiteMessage.warnProgrammer("IO Exception getting URL contents (6a) : " + path + "\n   " + e.getMessage());
				if (MesquiteTrunk.debugMode){
					MesquiteMessage.warnProgrammer("  " + e);
					e.printStackTrace();
				}
			}
			return null;
		}
		return s.toString();

	}
	/*.................................................................................................................*/
	public static void download(String address, String localFileName) {
		OutputStream out = null;
		URLConnection conn = null;
		InputStream  in = null;
		try {
			URL url = new URL(address);
			out = new BufferedOutputStream(
					new FileOutputStream(localFileName));
			conn = url.openConnection();
			in = conn.getInputStream();
			byte[] buffer = new byte[1024];
			int numRead;
			long numWritten = 0;
			while ((numRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, numRead);
				numWritten += numRead;
			}
			System.out.println(localFileName + "\t" + numWritten);
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (IOException ioe) {
			}
		}
	}

	/** Downloads the contents of the file at the url to a local file*/
	// 0 if unknown problem in downloading/writing
	// -1 if problem reading from server
	// -2 if problem writing download
	public static int downloadURLContents(String urlPath, String writePath, boolean warnIfProblem, boolean showProgressIndicator) {
		URLConnection uC;
		OutputStream outFile = null;
		InputStream  input = null;
		ProgressIndicator progIndicator=null;
		int doing = 0;
		try {
			byte[] buf = new byte[1024];
			doing = 1; //
			URL url = new URL(urlPath);
			//outFile = new BufferedOutputStream(new FileOutputStream(writePath));
			uC = url.openConnection();
			int length = uC.getContentLength();
			if (showProgressIndicator) {
				progIndicator = new ProgressIndicator(null,"File Download", length, false);
				if (progIndicator!=null){
					progIndicator.start();
					progIndicator.toFront();
				}
			}
			input = uC.getInputStream();
			int numRead;
			long numWritten = 0;
			while ((numRead = input.read(buf)) != -1) {
				doing = 2;
				if (outFile == null)
					outFile = new BufferedOutputStream(new FileOutputStream(writePath));

				outFile.write(buf, 0, numRead);
				doing = 1;
				numWritten += numRead;
				CommandRecord.tick("" + numWritten + " bytes downloaded");
				if (progIndicator!=null){
					progIndicator.setText("" + numWritten + " bytes downloaded");
					progIndicator.setCurrentValue(numWritten);
				}
			}
			doing = 1;
			input.close();
			doing = 2;
			outFile.close();
			doing = 0;
			if (progIndicator!=null)
				progIndicator.goAway();
		} 
		catch (Exception e) {
			if (warnIfProblem){
				MesquiteMessage.warnProgrammer("IO Exception found (6q) : " + writePath + "   " + e.getMessage() );
				e.printStackTrace();
			}
			if (progIndicator!=null)
				progIndicator.goAway();
			return -doing;
		}
		return 1;

	}
	/** Returns the contents of the file at the url*/
	public static int downloadURLContents(String urlPath, String writePath, boolean warnIfProblem) {
		return downloadURLContents(urlPath, writePath, warnIfProblem, false);
	}
	/*.................................................................................................................*/
	/** Returns the contents of the file.  path is relative to the root of the package heirarchy; i.e. for file in
	a module's folder, indicate "mesquite/modules/moduleFolderName/fileName" */
	public static String[] getURLContentsAsStrings(String path) {
		return getURLContentsAsStrings(path, true);
	}
	/*.................................................................................................................*/
	/** Returns the contents of the file.  path is relative to the root of the package heirarchy; i.e. for file in
	a module's folder, indicate "mesquite/modules/moduleFolderName/fileName" */
	public static String[] getURLContentsAsStrings(String path, boolean notifyIfNotFound) {
		DataInputStream stream;
		Vector v = new Vector();
		String[] s = null;
		StringBuffer sBb= new StringBuffer(100);
		MesquiteInteger remnant = new MesquiteInteger(-1);
		URL url = null;

		try {
			url = new URL(path);
		}
		catch (MalformedURLException e){
			return null;
		}

		try {

			stream = new DataInputStream(url.openStream());
			String newS = " ";

			while (newS != null) {
				newS =readLine(stream, sBb, remnant);  //TODO: this won't stop if very long lines
				if (newS!=null) {
					v.addElement(newS);
				}
			}
			if (v.size()!=0) {
				s = new String[v.size()];
				int count = 0;
				Enumeration e = v.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					s[count]= (String)obj;
					count++;
				}
				return s;
			}
		}
		catch( IOException e ) {
			if (notifyIfNotFound)
				MesquiteMessage.warnProgrammer("IO Exception found (6a1) : " + path + "   " + e.getMessage());
			//MesquiteMessage.printStackTrace(e);
			return null;
		}

		return null;
	}
	/*.................................................................................................................*/
	public static void putFileContentsQuery(String message, String output, boolean ascii){
		MainThread.incrementSuppressWaitWindow();
		MesquiteFileDialog fdlg= new MesquiteFileDialog(MesquiteTrunk.mesquiteTrunk.containerOfModule(), message, FileDialog.SAVE);   // Save File dialog box
		fdlg.setVisible(true);
		String tempFileName=fdlg.getFile();
		String tempDirectoryName=fdlg.getDirectory();
		// fdlg.dispose();
		if (!StringUtil.blank(tempFileName) && !StringUtil.blank(tempDirectoryName)) {
			MesquiteFile.putFileContents(tempDirectoryName+tempFileName, output,ascii);
		}
		MainThread.decrementSuppressWaitWindow();
	}
	/*.................................................................................................................*/
	public static String putFileContentsQueryReturnDirectory(String message, String output){
		MainThread.incrementSuppressWaitWindow();
		MesquiteFileDialog fdlg= new MesquiteFileDialog(MesquiteTrunk.mesquiteTrunk.containerOfModule(), message, FileDialog.SAVE);   // Save File dialog box
		fdlg.setVisible(true);
		String tempFileName=fdlg.getFile();
		String tempDirectoryName=fdlg.getDirectory();
		// fdlg.dispose();
		if (!StringUtil.blank(tempFileName) && !StringUtil.blank(tempDirectoryName)) {
			MesquiteFile.putFileContents(tempDirectoryName+tempFileName, output,false);
		}
		MainThread.decrementSuppressWaitWindow();
		return tempDirectoryName;
	}
	/*.................................................................................................................*/
	static boolean w = false;
	/** Places to a file the contents.  Path is relative to the root of the package heirarchy; i.e. for file in
	a module's folder, indicate "mesquite/modules/moduleFolderName/fileName" */
	public synchronized static void putFileContents(String relativePath, String[] contents, boolean ascii) {
		if (contents==null)
			return;
		if (w)
			MesquiteMessage.warnProgrammer("writing simultaneously ");

		w = true;
		if (fileExists(relativePath) && !canWrite(relativePath)) {
			MesquiteModule.mesquiteTrunk.discreetAlert( MesquiteThread.isScripting(),"File cannot be written.  It may be locked or open in another application. (2; Path: " + relativePath + ")"); 
			//MesquiteMessage.warnProgrammer("can't write file " + relativePath);
			//MesquiteMessage.printStackTrace();
			return;
		}
		Writer stream;
		if (!MesquiteTrunk.isApplet()) {
			try {
				if (ascii && System.getProperty("os.name").startsWith("Mac"))
					stream = new OutputStreamWriter(new FileOutputStream(relativePath), "ASCII");
				else
					stream = new OutputStreamWriter(new FileOutputStream(relativePath));
				for (int i=0; i< contents.length; i++) {
					if (contents[i]!=null) {
						stream.write(contents[i] + StringUtil.lineEnding());
						stream.flush();
					}
				}
				stream.close();
				try {MRJFileUtils.setFileTypeAndCreator(new File(relativePath), new MRJOSType("TEXT"), new MRJOSType("R*ch"));}
				catch (Throwable t){}
			}
			catch( FileNotFoundException e ) {
				MesquiteMessage.warnProgrammer( "File Busy or Not Found:  put file contents (0)");
				//MesquiteMessage.printStackTrace();
			} 
			catch( IOException e ) {
				MesquiteMessage.warnProgrammer( "IO exception put file contents  (0) " + e.getMessage());
				//MesquiteMessage.printStackTrace();
			}
		}
		else {
			//files cannot be written with applets
		}
		w = false;
	}
	/*.................................................................................................................*/
	/** Places to a file the contents.  Path is relative to the root of the package heirarchy; i.e. for file in
	a module's folder, indicate "mesquite/modules/moduleFolderName/fileName" */
	public synchronized static void putFileContents(String relativePath, Vector contents, boolean ascii) {
		if (contents==null)
			return;
		if (w)
			MesquiteMessage.warnProgrammer("writing simultaneously ");

		w = true;
		if (fileExists(relativePath) && !canWrite(relativePath)) {
			MesquiteModule.mesquiteTrunk.discreetAlert( MesquiteThread.isScripting(),"File cannot be written.  It may be locked or open in another application. (3; Path: " + relativePath + ")"); 
			return;
		}
		Writer stream;
		if (!MesquiteTrunk.isApplet()) {
			try {
				if (ascii && System.getProperty("os.name").startsWith("Mac"))
					stream = new OutputStreamWriter(new FileOutputStream(relativePath), "ASCII");
				else
					stream = new OutputStreamWriter(new FileOutputStream(relativePath));
				for (int i=0; i< contents.size(); i++) {
					if (contents.elementAt(i)!=null) {
						stream.write(contents.elementAt(i).toString() + StringUtil.lineEnding());
						stream.flush();
					}
				}
				stream.close();
				try {MRJFileUtils.setFileTypeAndCreator(new File(relativePath), new MRJOSType("TEXT"), new MRJOSType("R*ch"));}
				catch (Throwable t){}
			}
			catch( FileNotFoundException e ) {
				MesquiteMessage.warnProgrammer( "File Busy or Not Found: put file contents  (1) [" + relativePath + "]");
				//MesquiteMessage.printStackTrace();
			} 
			catch( IOException e ) {
				MesquiteMessage.warnProgrammer( "IO exception put file contents  (1)  [" + relativePath + "] " + e.getMessage());
				//MesquiteMessage.printStackTrace();
			}
		}
		else {
			//files cannot be written with applets
		}
		w = false;
	}
	/*.................................................................................................................*/
	/** Places to a file the contents.  Path is relative to the root of the package heirarchy; i.e. for file in
	a module's folder, indicate "mesquite/modules/moduleFolderName/fileName" */
	public synchronized static void putFileContents(String relativePath, String contents, boolean ascii) {
		putFileContents(relativePath, contents, ascii, true);
	}
	/*.................................................................................................................*/
	/** Places to a file the contents.  Path is relative to the root of the package heirarchy; i.e. for file in
	a module's folder, indicate "mesquite/modules/moduleFolderName/fileName" */
	public synchronized static void putFileContents(String relativePath, String contents, boolean ascii, boolean warn) {
		if (contents==null || relativePath==null)
			return;
		if (w)
			MesquiteMessage.warnProgrammer("writing simultaneously ");

		w = true;
		if (fileExists(relativePath) && !canWrite(relativePath)) {
			MesquiteModule.mesquiteTrunk.discreetAlert( MesquiteThread.isScripting(),"File cannot be written.  It may be locked or open in another application. (4; Path: " + relativePath + ")"); 
			return;
		}
		Writer stream;
		if (!MesquiteTrunk.isApplet()) {
			try {
				if (ascii && System.getProperty("os.name").startsWith("Mac"))
					stream = new OutputStreamWriter(new FileOutputStream(relativePath), "ASCII");
				else
					stream = new OutputStreamWriter(new FileOutputStream(relativePath));
				if (contents!=null) {
					stream.write(contents);
					stream.flush();
					stream.close();
					try {MRJFileUtils.setFileTypeAndCreator(new File(relativePath), new MRJOSType("TEXT"), new MRJOSType("R*ch"));}
					catch (Throwable t){}
				}
			}
			catch( FileNotFoundException e ) {
				MesquiteMessage.warnProgrammer( "File Busy or Not Found:  put file contents  (2) [" + relativePath + "]");
				MesquiteMessage.printStackTrace();
			} 
			catch( IOException e ) {
				MesquiteMessage.warnProgrammer( "IO exception put file contents  (2)  [" + relativePath + "] " + e.getMessage());
				//MesquiteMessage.printStackTrace();
			}
		}
		else {
			//files cannot be written with applets
		}
		w = false;
	}
	/*.................................................................................................................*/
	/** Appends to a file the contents.  Path is relative to the root of the package heirarchy; i.e. for file in
	a module's folder, indicate "mesquite/modules/moduleFolderName/fileName" */
	public synchronized static void appendFileContents(String relativePath, String contents, boolean ascii) {
		if (w)
			MesquiteMessage.warnProgrammer("writing simultaneously ");

		w = true;
		if (fileExists(relativePath) && !canWrite(relativePath)) {
			MesquiteModule.mesquiteTrunk.discreetAlert( MesquiteThread.isScripting(),"File cannot be written.  It may be locked or open in another application. (5; Path: " + relativePath + ")"); 
			return;
		}
		Writer stream;
		if (!MesquiteTrunk.isApplet()) {
			try {
				if (ascii && System.getProperty("os.name").startsWith("Mac"))
					stream = new OutputStreamWriter(new FileOutputStream(relativePath, true), "ASCII");
				else
					stream = new OutputStreamWriter(new FileOutputStream(relativePath, true));
				if (contents!=null) {
					stream.write(contents);
					stream.flush();
					stream.close();
				}
			}
			catch( FileNotFoundException e ) {
				MesquiteMessage.warnProgrammer( "File Busy or Not Found:  append file contents  (3) [" + relativePath + "]");
				//MesquiteMessage.printStackTrace();
			} 
			catch( IOException e ) {
				MesquiteMessage.warnProgrammer( "IO exception append file contents  (3)  [" + relativePath + "] " + e.getMessage());
				//MesquiteMessage.printStackTrace();
			}
		}
		else {
			//files cannot be written with applets
		}
		w = false;
	}

	/*.................................................................................................................*/
	/** Returns whether a directory can be shown in the OS's file browser." */
	public static boolean canShowDirectory() {
		return (MesquiteTrunk.isMacOSX() || MesquiteTrunk.isWindows());
	}
	/*.................................................................................................................*/
	/** Shows the directory specified in path in the Finder or Windows Explorer" */
	public static void showDirectory(String path) {
		if (path == null)
			return;
		File file = new File(path);
		if (!file.exists()|| !file.isDirectory())
			return;
		if (MesquiteTrunk.isMacOSX()) {

			try {
				Runtime.getRuntime().exec(
						new String[] { "open", file.getAbsolutePath() });
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (MesquiteTrunk.isWindows()){
			try {
				Runtime.getRuntime().exec(
						new String[] { "explorer", file.getAbsolutePath() });
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("Sorry, can't show folder under this operating system");
		}
	}

	/*.................................................................................................................*/
	public static int numFilesEndingWith(String directoryPath, String[] files, String ending){
		int count=0;
		for (int i=0; i<files.length; i++) { // going through the folders and finding the ace files
			if (files[i]!=null ) {
				String filePath = directoryPath + MesquiteFile.fileSeparator + files[i];
				File cFile = new File(filePath);
				if (cFile.exists()) {
					if (!cFile.isDirectory()) {
						if (files[i].endsWith(ending)) {
							count++;
						}
					}
				}
			}
		}
		return count;
	}


	static boolean warnedLogNoWrite = false;
	/*.................................................................................................................*/
	/** Writes to the log file*/
	public static void writeToLog(String s) {
		if (MesquiteModule.userDirectory == null)
			return;
		String logPath = null;
		if (!MesquiteTrunk.isApplet() && s!=null) {
			try {
				if (logStream==null) {
					logPath = MesquiteModule.supportFilesDirectory + fileSeparator + MesquiteTrunk.logFileName; //TODO: should have user settable in future
					logStream = new PrintWriter(new FileOutputStream(logPath, appendToLog));
					appendToLog = true;  //subsequent calls append
				}
				logStream.write(s);
				logStream.flush();
			}
			catch( FileNotFoundException e ) {
				System.out.println("Error writing log file: FileNotFoundException. (User dir should be at " + MesquiteModule.userDirectory + "; log path " + logPath + ")"); 
			}
			catch( IOException e ) {
				System.out.println("Error writing log file: IOException. (User dir should be at " + MesquiteModule.userDirectory + "; log path \"" + logPath + "\")"); 
			}
			catch (Throwable e){
			}
		}
	}
	/*.................................................................................................................*/
	public static PrintWriter getLogWriter(){
		return logStream;
	}
	/*.................................................................................................................*/
	public static void throwableToLog(Object obj, Throwable e){
		try {
			if (e == null)
				return;
			ByteArrayOutputStream throwableStream= new ByteArrayOutputStream();
			PrintWriter throwableWriter= new PrintWriter(throwableStream);
			e.printStackTrace(throwableWriter);
			throwableWriter.flush();
			if (MesquiteTrunk.mesquiteTrunk == null)
				return;
			if (obj != null)
				MesquiteTrunk.mesquiteTrunk.logln("(Following stack trace from object of class: " + obj.getClass().toString() + ")");
			MesquiteTrunk.mesquiteTrunk.logln(throwableStream.toString());
		}
		catch (Throwable t){
		}
	}
	/*.................................................................................................................*/
	/** Closes to the log file*/
	public static void closeLog() {
		if (logStream!=null) {
			logStream.close();
			logStream = null;
			try {MRJFileUtils.setFileTypeAndCreator(new File(MesquiteModule.supportFilesDirectory + fileSeparator + MesquiteTrunk.logFileName), new MRJOSType("TEXT"), new MRJOSType("R*ch"));}
			catch (Throwable t){}
		}
	}
	/*.................................................................................................................*/
	public boolean getWriteProtected(){
		return writeProtected;
	}
	public void setWriteProtected(boolean prot){
		writeProtected = prot;
	}
	/** Sets the annotation (e.g., footnote) of this element */
	public void setAnnotation(String e, boolean notify){
		comment = e;
	}
	/** Returns the annotation (e.g., footnote) of this element */
	public String getAnnotation(){
		return comment;
	}
	/** Returns the explanation (e.g., footnote plus additional information) of this file */
	public String getExplanation(){
		if (project == null)
			return "No project";
		String extra = "";
		extra += "This file has " + project.getNumberTaxas(this)+ " block(s) of taxa and  " + project.getNumberCharMatrices(this)+ " character matrices.\n";
		if (project.getNumberLinkedFiles()>1)
			extra += "It is part of the project with home file \"" + project.getHomeFileName()+ "\"\n";
		return extra;
	}
	/* ---------------- for HNode interface ----------------------*/
	public HNode[] getHDaughters(){
		return null;
		/* post 2. 01, the projects & files window no longer shows file elements
		if (fileElements == null || fileElements.size()== 0)
			return null;
		HNode[] daughters = new HNode[fileElements.size()];
		for (int i = 0; i < fileElements.size(); i++)
			daughters[i] = (HNode)fileElements.elementAt(i);
		return daughters;
		 */
	}
	/* ---------------- for HNode interface ----------------------*/
	public HNode getHMother(){
		return project;
	}
	/* ---------------- for HNode interface ----------------------*/
	public String getName(){
		return getFileName();
	}
	public String getTypeName(){
		return "File";
	}
	/* ---------------- for HNode interface ----------------------*/
	public int getNumSupplements(){
		return 0;
	}
	/* ---------------- for HNode interface ----------------------*/
	public String getSupplementName(int index){
		return null;
	}
	/* ---------------- for HNode interface ----------------------*/
	/** for HNode interface */
	public void hNodeAction(Container c, int x, int y, int action){
		if (c==null || project == null)
			return;
		if (action == HNode.MOUSEDOWN){
			MesquitePopup popup = new MesquitePopup(c);
			popup.addItem(getName(), project.getCoordinatorModule(), null);
			popup.addItem("Close File", project.getCoordinatorModule(), project.getCloseCommand(), Long.toString(getID())); //22 Dec 01 changed from project.getFileNumber(this)
			popup.showPopup(x,y);
		}
		else if (action == HNode.MOUSEMOVE){
			String e = getExplanation();
			if (!StringUtil.blank(e)){
				MesquiteWindow f = MesquiteWindow.windowOfItem(c);
				if (f!=null && f instanceof MesquiteWindow){
					((MesquiteWindow)f).setExplanation(e);
				}
			}
			e = getAnnotation();
			if (!StringUtil.blank(e)){
				MesquiteWindow f = MesquiteWindow.windowOfItem(c);
				if (f!=null && f instanceof MesquiteWindow){
					((MesquiteWindow)f).setAnnotation(e, "Footnote above refers to file " + getName());
				}
			}
		}
	}
	/* ---------------- for HNode interface ----------------------*/
	public void hSupplementTouched(int index){}
	/* ---------------- for HNode interface ----------------------*/
	public Image getHImage(){
		return null;
	}
	/* ---------------- for HNode interface ----------------------*/
	public Color getHColor(){
		return ColorTheme.getInterfaceBackgroundPale();  //project color
	}
	/* ---------------- for HNode interface ----------------------*/
	public boolean getHShow(){
		return true; 
	}
}

class DirFilenameFilter implements FilenameFilter {
	public boolean accept(File dir, String name){
		File f = new File(dir + MesquiteFile.fileSeparator + name);
		if (f.exists() && f.isDirectory())
			return true;
		return false;		
	}
}
class MqFilterInputStream extends FilterInputStream {
	public MqFilterInputStream (InputStream s){
		super(s);
	}
}
