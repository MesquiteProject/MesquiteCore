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
import java.awt.image.*;
import java.net.*;
import java.util.*;


/* ======================================================================== */
/*===============================================*/
/** This class provides utilities to recover images from local file or URL, automatically choosing
depending whether Mesquite is being run as applet or application.*/
public abstract class MesquiteImage extends Image {
	static int noFileErrorCount = 0;
	/** Returns an image at a given path relative to the code base of Mesquite*/
	public static Image getImageNullIfDoesntExist(String path) {
		if (!MesquiteFile.fileExists(path)) {
			if (noFileErrorCount == 10)
				MesquiteMessage.printStackTrace("Image file doesn't exist: " + path + "; THIS MESSAGE WILL NOT BE REPEATED DURING THIS RUN OF MESQUITE");
			else if (noFileErrorCount<10)
				MesquiteMessage.println("Image file doesn't exist: " + path);
			noFileErrorCount++; 
			return null;
		}
		return Toolkit.getDefaultToolkit().getImage(path);
	}
	/** Returns an image at a given path relative to the code base of Mesquite*/
	public static Image getImage(String path) {
		return getImage(path, true);
	}
	/** Returns an image at a given path relative to the code base of Mesquite*/
	public static Image getImage(String path, boolean warn) {
		if (path == null)
			return null;
		if (path.indexOf("://")>=0){
			try {
				URL imageURL = new URL(path);
				return Toolkit.getDefaultToolkit().getImage(imageURL);
			}
			catch (MalformedURLException e) {
				MesquiteMessage.println("malformed URL in get image(1) " + path);
				return null;
			}
		}
		if (!MesquiteFile.fileExists(path)) {
			if (warn){
				if (noFileErrorCount == 10)
					MesquiteMessage.printStackTrace("Image file doesn't exist: " + path + "; THIS MESSAGE WILL NOT BE REPEATED DURING THIS RUN OF MESQUITE");
				else if (noFileErrorCount<10)
					MesquiteMessage.println("Image file doesn't exist: " + path);
				noFileErrorCount++; 
			}
		return null;
		}
		return Toolkit.getDefaultToolkit().getImage(path);
	}

	/** Returns an image at a given path relative to the location of a file (URL or local)*/
	public static Image getImage(String path, MesquiteProject project) {
		if (project.getHomeURL()==null) {
			return Toolkit.getDefaultToolkit().getImage(MesquiteFile.composePath(project.getHomeDirectoryName(), path));
		}
		else {
			URL url = project.getHomeURL();
			try {
				String directoryName = url.getFile().substring(0, url.getFile().lastIndexOf(MesquiteFile.fileSeparator)+1); 
				URL imageURL = new URL(url.getProtocol() + "://" + directoryName + path);
				return Toolkit.getDefaultToolkit().getImage(imageURL);
			}
			catch (MalformedURLException e) {
				MesquiteMessage.println("malformed URL in get image (2) " + path);
				return null;
			}

		}
	}
	/*...........................................................*/
	public static void swapParts(Image[] d, int first, int second) {
		if (d == null || first<0 || first>=d.length || second<0 || second>=d.length) 
			return;
		Image temp = d[first];
		d[first] = d[second];
		d[second] = temp;
	}
	/*...........................................................*/
	public static void moveParts(Image[] d, int starting, int num, int justAfter) {
		if (num<=0 || d==null || starting>=d.length || (justAfter>=starting && justAfter<=starting+num-1)) //starting???
			return;
		if (justAfter>=d.length)
			justAfter = d.length-1;
		if (justAfter<0)
			justAfter = -1;
		Image[] newValues = new Image[d.length];
		if (starting>justAfter){
			int count =0;
			for (int i=0; i<=justAfter; i++)
				newValues[count++]=d[i];

			for (int i=starting; i<=starting+num-1; i++)
				newValues[count++]=d[i];
			for (int i=justAfter+1; i<=starting-1; i++)
				newValues[count++]=d[i];
			for (int i=starting+num; i<d.length; i++)
				newValues[count++]=d[i];
		}
		else {
			int count =0;
			for (int i=0; i<=starting-1; i++)
				newValues[count++]=d[i];

			for (int i=starting+num; i<=justAfter; i++)
				newValues[count++]=d[i];
			for (int i=starting; i<=starting+num-1; i++)
				newValues[count++]=d[i];
			for (int i=justAfter+1; i<d.length; i++)
				newValues[count++]=d[i];
		}
		for (int i=0; i<d.length; i++)
			d[i]=newValues[i];
	}
	/*.................................................................................................................*/
	public static boolean waitForImageToLoad(Image image){
		return waitForImageToLoad(image, MesquiteModule.logWindow.getParentFrame());
	}
	/*.................................................................................................................*/
	public static boolean waitForImageToLoad(Image image, Component component){
		MediaTracker mt = new MediaTracker(component);
		mt.addImage(image, 0);
		try {
			mt.waitForID(0);
			} catch (Exception e) {
			MesquiteFile.throwableToLog(component, e);
		}
		while (mt.statusID(0, true) == MediaTracker.LOADING) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				MesquiteFile.throwableToLog(component, e);
			break;
			}
		}

		boolean complete =  (mt.statusID(0, true) == MediaTracker.COMPLETE);
		mt.removeImage(image, 0);
		return complete;
}
	/*.................................................................................................................*/
	public static Image loadImage(String message, String path, Component component,  MesquiteString imagePath) {
		String imagePathLocal = path;
		if (StringUtil.blank(path)) {
			if (MesquiteThread.isScripting())
				return null;
			MesquiteString directoryName= new MesquiteString();
			MesquiteString fileName= new MesquiteString();
			imagePathLocal = MesquiteFile.openFileDialog(message, directoryName, fileName);
		}
		if (StringUtil.blank(imagePathLocal))
			return null;

		Image image = getImageNullIfDoesntExist(imagePathLocal);

		if (imagePath!=null)
			imagePath.setValue(imagePathLocal);

		if (image!=null && (component == null || waitForImageToLoad(image,component)))
			return image;
		return null;
	}
	/*.................................................................................................................*/
	/* Returns reduction needed to reduce into constraints; if width and height already smaller, then returns 1.0*/
	public static double getScaleToReduce(int width, int height, int constrainedWidth, int constrainedHeight){
		if (constrainedWidth == 0 || constrainedHeight == 0)
			return 1.0;
		if (width > constrainedWidth || height > constrainedHeight){
			double scale = 1.0;
			if (width*1.0/constrainedWidth > height*1.0/constrainedHeight) //width proportionately way too big
				scale = constrainedWidth*1.0/width;
			else  //height proportionately way too big
				scale =constrainedHeight*1.0/height;
			width *= scale;
			height *= scale;
			return scale;
		}
		else if (width < constrainedWidth || height < constrainedHeight){
			double scale = 1.0;
			if (width*1.0/constrainedWidth > height*1.0/constrainedHeight) //width proportionately way too big
				scale = constrainedWidth*1.0/width;
			else  //height proportionately way too big
				scale =constrainedHeight*1.0/height;
			width *= scale;
			height *= scale;
			return scale;
		}
		return 1.0;
	}

	public static Rectangle drawImageWithinRect(Graphics g, Image pic, int w, int h, ImageObserver io){
		return drawImageWithinRect(g, pic, 0, 0, w, h, io);
	}
	public static Rectangle drawImageWithinRect(Graphics g, Image pic, int x, int y,  int w, int h, ImageObserver io){
		return drawImageWithinRect(g, pic, x, y, w, h, true, io);
	}
	public static Rectangle drawImageWithinRect(Graphics g, Image pic, int x, int y,  int w, int h, boolean center, ImageObserver io){
		if (pic == null)
			return null;
		int picWidth = pic.getWidth(io);
		int picHeight = pic.getHeight(io);
		double scale = getScaleToReduce(picWidth, picHeight, w, h);
	//	if (scale<1.0){
			picWidth *= scale;
			picHeight *= scale;
	//	}
	//	else
	//		scale = 1.0;
		if (center){
			int picX = x + (w-picWidth)/2;
			int picY = y + (h-picHeight)/2;
			if (g != null)
				g.drawImage(pic,picX, picY, picWidth, picHeight, io);
			return new Rectangle(picX, picY, picWidth, picHeight);
		}
		else {
			if (g != null)
				g.drawImage(pic,x, y, picWidth, picHeight, io);
			return new Rectangle(x, y, picWidth, picHeight);
		}
	}
}


