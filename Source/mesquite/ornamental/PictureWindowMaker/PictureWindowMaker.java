/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison. 
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.ornamental.PictureWindowMaker;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class PictureWindowMaker extends FileAssistantN {
	PictureWindow pictureWindow;
	String pathToPicture;
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) { //todo: should behave differently depending on whether home file was url or not!!!
		pictureWindow = new PictureWindow(this);
		if (!MesquiteThread.isScripting()){ //file dialog to choose picture
	 		setModuleWindow(pictureWindow);
			MesquiteString dir = new MesquiteString();
			MesquiteString f = new MesquiteString();
			
   	 		String path = MesquiteFile.openFileDialog("Picture to show", dir, f);
   	 		String d = dir.getValue();
   	 		if (!StringUtil.blank(d) && !StringUtil.blank(f.getValue())) {
   	 			if (!d.endsWith("/")) 
   	 				d += "/";
   	 			boolean success;
   	 			if (getProject().getHomeDirectoryName().equalsIgnoreCase(d)){
   	 				pathToPicture = f.getValue();
   	 				String p = MesquiteFile.composePath(getProject().getHomeDirectoryName(), pathToPicture);
   	 				if (!MesquiteFile.fileExists(p))
   	 					return sorry(getName() + " couldn't start because picture file not found.");
   	 				success = pictureWindow.setPath(p);
   	 			}
   	 			else {
   	 				pathToPicture = path;
   	 				if (!MesquiteFile.fileExists(path))
   	 					return sorry(getName() + " couldn't start because picture file not found.");
					success = pictureWindow.setPath(pathToPicture);
				}
				if (!success)
					return sorry(getName() + " couldn't start because of a problem obtaining the picture.");
				else {
			 		resetContainingMenuBar();
			 		resetAllWindowsMenus();
					pictureWindow.setVisible(true);
				}
   	 		}
   	 		else 
   	 			return sorry(getName() + " couldn't start because no picture was specified.");
		}
		else {
	 		setModuleWindow(pictureWindow);
	 		resetContainingMenuBar();
	 		resetAllWindowsMenus();
 		}
		return true;
  	 }
	 
  	 public Snapshot getSnapshot(MesquiteFile file) {
  	 	if (pictureWindow ==null || !pictureWindow.isVisible())
  	 		return null;
  	 	Snapshot fromWindow = pictureWindow.getSnapshot(file);
    	 	Snapshot temp = new Snapshot();
  	 	
		temp.addLine("setPicture " + StringUtil.tokenize(MesquiteFile.decomposePath(getProject().getHomeDirectoryName(), pathToPicture))); //TODO: this should do relative to home file, not absolute
		temp.addLine("getWindow");
		temp.addLine("tell It");
		temp.incorporate(fromWindow, true);
		temp.addLine("endTell");
		temp.addLine("showWindow");
  	 	return temp;
  	 }
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
     	 	 if (checker.compare(this.getClass(), "Sets the path to the picture to be shown", "[path to file; if relative, should be relative to home file of project]", commandName, "setPicture")){
   	 		pathToPicture = ParseUtil.getFirstToken(arguments, pos);
  			boolean success = pictureWindow.setPath(MesquiteFile.composePath(getProject().getHomeDirectoryName(), pathToPicture));
  			if (!success)
  				iQuit();
  			else if (!MesquiteThread.isScripting())
				pictureWindow.setVisible(true);
    	 	 } 
      	 	 
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
    	 	
	return null;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Picture Window";
   	 }
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "New Picture Window";
   	 }
	/*.................................................................................................................*/
 	public void windowGoAway(MesquiteWindow whichWindow) {
			whichWindow.hide();
			whichWindow.dispose();
			iQuit();
	}
	 public boolean isSubstantive(){
	 	return false;
	 }
	 
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Displays a picture in a window." ;
   	 }
}
	
/* ======================================================================== */
class PictureWindow extends MesquiteWindow implements Commandable  {
	String path;
	Image image;
	ImagePanel imagePanel;
	MediaTracker mt = null;
		boolean errored = true;
		int count =0;
	
	public PictureWindow (PictureWindowMaker ownerModule){
		super(ownerModule, true); //infobar
      		setWindowSize(64,64);
      		setMinimalMenus(true);
		//getGraphicsArea().setLayout(new BorderLayout());
		addToWindow(imagePanel = new ImagePanel(this));
		imagePanel.setSize(64, 64);
		setLocation(0,0);
		imagePanel.setVisible(true);
		resetTitle();
      		setWindowSize(64,64);
	}
	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree blocks, etc.)
	they can reset their titles properly*/
	public void resetTitle(){
		setTitle("Picture: " + path); //TODO: what tree?
	}
	public void checkSize(){
		
	}
	public boolean setPath(String path){
		this.path = path;
		image = MesquiteImage.getImage(path);
		if (MesquiteImage.waitForImageToLoad(image, this.getOuterContentsArea())){
			imagePanel.setImage(image);
			imagePanel.repaint();
			setResizable(true);
	      	if (image!=null) {
	      		setWindowSize(image.getWidth(imagePanel),image.getHeight(imagePanel));
	      		imagePanel.setSize(image.getWidth(imagePanel),image.getHeight(imagePanel));
	      	}
			setResizable(false);
			resetTitle();
			return true;
		}
		return false;
	}
}
/* ======================================================================== */
/** The Panel containing the Mesquite logo on the startup window */
class ImagePanel extends Panel {
	Image pic;
	PictureWindow pw;
	public ImagePanel (PictureWindow pw) {
		setBackground(Color.white);
		this.pw = pw;
	}
	/*.................................................................................................................*/
	public void paint(Graphics g) {
	   	if (MesquiteWindow.checkDoomed(this))
	   		return;
			g.drawImage(pic,0,0,(ImageObserver)this);
		MesquiteWindow.uncheckDoomed(this);
	}
	public void setImage(Image i){
		pic = i;
	}
}


