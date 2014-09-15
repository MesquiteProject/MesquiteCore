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
import java.awt.event.*;
import java.io.*;


/*===============================================*/
/** A dialog box*/
public class MesquiteFileDialog extends FileDialog implements Commandable, Listable {
	String path = MesquiteTrunk.getRootPath(); //TODO: use default directory
	String message = null;
	String fileName = null;
	boolean holdsConsoleFocus = false;
	boolean doneByConsole = false;
	int type;
	//MFDThread mfdThread = null;
	public static MesquiteFileDialog currentFileDialog = null;
	public MesquiteFileDialog (MesquiteWindow f, String message, int type) {
		super(getFrame(f), message, type);
		this.message = message;
		this.type = type;
		currentFileDialog = this;
		//mfdThread = new MFDThread(this);
		//mfdThread.start();
		MainThread.incrementSuppressWaitWindow();
	}
	static Frame getFrame(MesquiteWindow f){
		if (f == null)
			return null;
		return f.getParentFrame();
	}
	public void setDirectory(String path){
		super.setDirectory(path);
		this.path = path;
		if (path != null && !path.endsWith(MesquiteFile.fileSeparator))
			path += MesquiteFile.fileSeparator;
	}
	public String getFile(){
		if (doneByConsole)
			return fileName;
		return super.getFile();
	}
	public String getDirectory(){
		if (doneByConsole) {
			if (path != null && !path.endsWith(MesquiteFile.fileSeparator))
				path += MesquiteFile.fileSeparator;
			return path;
		}
		return super.getDirectory();
	}
	public String getName(){
		String s = "";
		if (getTitle() == null)
			s = "FileDialog";
		else
			s =  "File Dialog: " + getTitle();
		if (getDirectory() != null)
			s += " (" + getDirectory() + ")";
		return s;
	}
	boolean waiting = false;
	public void setVisible(boolean vis){


		if (type == 3){  //choosing a directory
			if (vis) {
				System.out.println("Choose Directory Dialog box shown.  Message: " + message);
				System.out.println("");

				showFiles();
				System.out.println("Enter \"chooseThis\" to choose current directory; number to open directory");
				ConsoleThread.setConsoleObjectCommanded(this, false, true);
				holdsConsoleFocus = true;
				if (!MesquiteWindow.GUIavailable ||  MesquiteWindow.suppressAllWindows) {
					try {
						waiting = true;
						while (waiting)
							Thread.sleep(20);
					}
					catch (InterruptedException e){
					}
					return;
				}
			}
			else {
				if (holdsConsoleFocus)
					ConsoleThread.releaseConsoleObjectCommanded(this, true);
				holdsConsoleFocus = false;

			}
		}
		else if (type == FileDialog.LOAD){
			if (vis) {
				System.out.println("Open File Dialog box shown.  Message: " + message);
				System.out.println("");

				showFiles();

				ConsoleThread.setConsoleObjectCommanded(this, false, true);
				holdsConsoleFocus = true;
				if (!MesquiteWindow.GUIavailable ||  MesquiteWindow.suppressAllWindows) {
					try {
						waiting = true;
						while (waiting)
							Thread.sleep(20);
					}
					catch (InterruptedException e){
					}
					return;
				}
			}
			else {
				if (holdsConsoleFocus)
					ConsoleThread.releaseConsoleObjectCommanded(this, true);
				holdsConsoleFocus = false;

			}
		}
		else {
				if (vis) {
					System.out.println("Save File Dialog box shown.  Message: " + message);
					System.out.println("");

					showFiles();
					System.out.println("Enter name of file to be saved as \"name '<filename>'\"");
					ConsoleThread.setConsoleObjectCommanded(this, false, true);
					holdsConsoleFocus = true;
					if (!MesquiteWindow.GUIavailable ||  MesquiteWindow.suppressAllWindows) {
						try {
							waiting = true;
							while (waiting)
								Thread.sleep(20);
						}
						catch (InterruptedException e){
						}
						return;
					}
				}
				else {
					if (holdsConsoleFocus)
						ConsoleThread.releaseConsoleObjectCommanded(this, true);
					holdsConsoleFocus = false;

				}
			}
		if (!vis){
			if (currentFileDialog == this)
				currentFileDialog = null;
			super.setVisible(false);
		}
		else {
			super.setVisible(true);
			/*
			mfdThread.pleaseShow = true;
			mfdThread.start();
			try {
				while (mfdThread.go)
					Thread.sleep(20);
			}
			catch (InterruptedException e){
			}*/
		}
		

	}
	void sv(){
		super.setVisible(true);
	}
	public void dispose(){

		if (currentFileDialog == this)
			currentFileDialog = null;
		if (alreadyDisposed)
			return;
		alreadyDisposed = true;
		super.dispose();
		if (holdsConsoleFocus)
			ConsoleThread.releaseConsoleObjectCommanded(this, true);
		holdsConsoleFocus = false;
		MainThread.decrementSuppressWaitWindow();

	}
	boolean alreadyDisposed = false;
	/*.................................................................................................................*/
	/** A request for the object to perform a command.  It is passed two strings, the name of the command and the arguments.*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 
		if (checker.compare(getClass(), null, null, commandName, "show")) {
			showFiles();
		}
		else if (checker.compare(getClass(), null, null, commandName, "up")) {
			if (path == null)
				return null;
			if (path.endsWith(MesquiteFile.fileSeparator))
				path = path.substring(0, path.length()-1);
			path = StringUtil.getAllButLastItem(path, MesquiteFile.fileSeparator) + MesquiteFile.fileSeparator;  

			showFiles();
			if (type == FileDialog.SAVE)
				System.out.println("Enter name of file to be saved as \"name '<filename>'\"");
		}
		else if (checker.compare(getClass(), null, null, commandName, "name")) {
			if (path == null)
				return null;
			MesquiteInteger pos = new MesquiteInteger();
			String name = ParseUtil.getFirstToken(arguments, pos);
			if (!StringUtil.blank(name)){
				File ff = new File(path + name);
				if (ff.isDirectory()) {
					path = path + name + MesquiteFile.fileSeparator;
					showFiles();
					return null;
				}
				if (type != 3){
					fileName = name;
				doneByConsole = true;
				waiting = false;
				setVisible(false);
				dispose();
				}
			}
		}
		else if (checker.compare(getClass(), null, null, commandName, "chooseThis")) {
			if (path == null)
				return null;
			
					fileName = null;
				doneByConsole = true;
				waiting = false;
				setVisible(false);
				dispose();
			
		}
		else if (checker.compare(getClass(), null, null, commandName, "cancel")) {

				fileName = null;
				path = null;
				doneByConsole = true;
				waiting = false;
				setVisible(false);
				dispose();

		}
		else {
			if (path == null)
				return null;
			MesquiteInteger pos = new MesquiteInteger();
			int im = MesquiteInteger.fromFirstToken(commandName, pos);
			if (MesquiteInteger.isCombinable(im)){
				im--;
				if (im >=0 && im<getNumFiles()){
					String file = getFileName(im);
					File ff = new File(path + file);
					if (ff.isDirectory()) {
						path = path + file + MesquiteFile.fileSeparator;
						showFiles();
					}
					else if (type == FileDialog.LOAD) {
						doneByConsole = true;
						fileName = file;
						waiting = false;
						setVisible(false);
						dispose();
					}
				}
			}
			
		}
		/*
    	 	else 
    	 		return  super.doCommand(commandName, arguments, checker);
		 */
		return null;
	}
	public int getNumFiles(){
		if (path == null)
			return 0;
		File f = new File(path);
		if (f.isDirectory()){
			String[] list = f.list();
			return list.length;
		}
		return 1;
	}
	public String getFileName(int i){
		if (path == null)
			return null;
		File f = new File(path);
		if (f.isDirectory()){
			String[] list = f.list();
			if (i>=0 && i<list.length)
				return list[i];
		}
		return null;
	}
	public void showFiles(){
		if (path == null)
			return;
		File f = new File(path);
		if (f.isDirectory()){
			System.out.println("Directory : " + path);
			String[] list = f.list();
			if (list==null)
				return;
			for (int i = 0; i<list.length; i++){
				File ff = new File(path + list[i]);
				if (ff.isDirectory())
					System.out.println("  " + (i+1) + " - " + list[i] + "/");
				else
					System.out.println("  " + (i+1) + " - " + list[i]);
			}
		}
		System.out.println("Enter number to select file");
	}

}

class MFDThread extends Thread {
	MesquiteFileDialog parent;
	boolean pleaseShow = false;
	int count = 0;
	boolean go= true;
	public MFDThread(MesquiteFileDialog parent){
		this.parent = parent;
	}
	public void run(){
		go = true;
		try{
			while (go && !MesquiteTrunk.mesquiteTrunk.mesquiteExiting){
				Thread.sleep(50);
				if (pleaseShow){
					parent.sv();
					go = false;
				}
			}
		}
		catch (InterruptedException e){
			MesquiteFile.throwableToLog(null, e);
		}
	}
}

