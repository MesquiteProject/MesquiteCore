/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)

Modified May 02 especially for annotations*/
package mesquite.lib.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.PrintJob;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteThread;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.tree.TreeDisplay;


public class MesquitePrintJob {
	public static final int AUTOFIT = 3;
	public static final int FIT_LANDSCAPE = 1;
	public static final int FIT_PORTRAIT = 0;
	public static final int NATURAL = -1;

	int fitToPage = AUTOFIT; ;
	Component component;
	Font font;
	PrintJob job1;
	PrinterJob job2;
	String name;
	MesquiteFrame frame;
	Dimension dimension;
	P2 p2;
	PageFormat pf = null;

	protected MesquitePrintJob(MesquiteFrame frame, String name){
		this.frame = frame;
		if (name == null)
			name = "Print";
		this.name = name;
		try {
			p2 = new P2();
		}
		catch (Throwable e){
			MesquiteFile.throwableToLog(this, e);
			MesquiteTrunk.mesquiteTrunk.alert("Exception or Error in making P2; details in Mesquite log file");
		}

	}
	public static MesquitePrintJob getPrintJob(MesquiteFrame frame, String name, int fitToPage){
		MesquiteTrunk.mesquiteTrunk.logln("Getting print job");
		MesquitePrintJob job = new MesquitePrintJob(frame, name);
		MesquiteTrunk.mesquiteTrunk.logln("Preparing to print");
		if (job == null)
			return null;
		if (job.preparePrint(fitToPage))
			return job;
		else {
			return null;
		}
	}
	public boolean preparePrint(int fitToPage) {
		this.fitToPage = fitToPage;


		job2 = PrinterJob.getPrinterJob();
		if (job2 ==null) {
			MesquiteTrunk.mesquiteTrunk.alert("Error: no printer job returned in preparePrint");
			return false;
		}
		job2.setPrintable(p2);

		if (fitToPage == AUTOFIT){  //fits landscape or portrait depending on what requires least shrinkage
			pf = job2.defaultPage();
			if (pf ==null) {
				MesquiteTrunk.mesquiteTrunk.alert("Error: no defaultPage returned in preparePrint (autofit)");
				return false;
			}
		}
		else if (fitToPage == FIT_LANDSCAPE){ //fits into landscape
			pf = job2.defaultPage();
			if (pf ==null) {
				MesquiteTrunk.mesquiteTrunk.alert("Error: no defaultPage returned in preparePrint (landscape)");
				return false;
			}
			pf.setOrientation(PageFormat.LANDSCAPE);
		}
		else if (fitToPage == FIT_PORTRAIT){ //fits into portrait
			pf = job2.defaultPage();
			if (pf ==null) {
				MesquiteTrunk.mesquiteTrunk.alert("Error: no defaultPage returned in preparePrint (portrait)");
				return false;
			}
			pf.setOrientation(PageFormat.PORTRAIT);
		}
		else { //prints at current size using page setup (thus, possibly over multiple pages)
			pf = job2.pageDialog(job2.defaultPage());
			if (pf == null) {
				MesquiteTrunk.mesquiteTrunk.alert("Error: no page dialog returned in preparePrint");
				return false;
			}
		}
		job2.validatePage(pf);
		job2.setPrintable(p2, pf);   
		if (MesquiteThread.isScripting())
			return true;
		else
			return job2.printDialog();
	}

	public void printComponent(Component component, Dimension dim, Font font) {
		this.font = font;
		this.component = component;
		if (dim == null){
			if (component instanceof TreeDisplay){
				
				dimension = new Dimension(((TreeDisplay)component).getFieldWidth(), ((TreeDisplay)component).getFieldHeight());
			}
			else
				dimension = component.getSize();
		}
		else
			dimension = dim;


		if (job2==null)
			return;
		Dimension oldTreeDisplaySize = null;
		Rectangle oldTreeVisRect = null;
		if (component instanceof TreeDisplay){
			oldTreeDisplaySize = component.getSize();
			oldTreeVisRect = ((TreeDisplay)component).getVisRect();
			((TreeDisplay)component).setVisRect(null);
			((TreeDisplay)component).setPrintingInProcess(true);
			component.setSize(((TreeDisplay)component).getFieldWidth(), ((TreeDisplay)component).getFieldHeight());
		}
		if (fitToPage == AUTOFIT){  //fits landscape or portrait depending on what requires least shrinkage
			if (dimension.width<=0 || dimension.height <=0)
				return;
			PageFormat pf = job2.defaultPage();
			pf.setOrientation(PageFormat.LANDSCAPE);
			double shrinkWidth = pf.getImageableWidth()*1.0/dimension.width;
			double shrinkHeight = pf.getImageableHeight()*1.0/dimension.height;
			double shrinkRatioLANDSCAPE;
			if (shrinkWidth< shrinkHeight)
				shrinkRatioLANDSCAPE = shrinkHeight/shrinkWidth;
			else
				shrinkRatioLANDSCAPE = shrinkWidth/shrinkHeight;
			pf.setOrientation(PageFormat.PORTRAIT);
			shrinkWidth = pf.getImageableWidth()*1.0/dimension.width;
			shrinkHeight = pf.getImageableHeight()*1.0/dimension.height;
			double shrinkRatioPORTRAIT;
			if (shrinkWidth< shrinkHeight)
				shrinkRatioPORTRAIT = shrinkHeight/shrinkWidth;
			else
				shrinkRatioPORTRAIT = shrinkWidth/shrinkHeight;

			if (shrinkRatioPORTRAIT>shrinkRatioLANDSCAPE)
				pf.setOrientation(PageFormat.LANDSCAPE);
			job2.validatePage(pf);
			job2.setPrintable(p2, pf);  
		}

		try {
			job2.print();  
		} 
		catch (Exception ex) {
			  //ex.printStackTrace();
		}
		if (component instanceof TreeDisplay){
			component.setSize(oldTreeDisplaySize.width, oldTreeDisplaySize.height);
			((TreeDisplay)component).setVisRect(oldTreeVisRect);
			((TreeDisplay)component).setPrintingInProcess(false);
			((TreeDisplay)component).repaintAll();
		}

	}

	public void printText(String s, Font font) {
		if (s == null || font==null)
			return;


		if (job2==null)
			return;

		if (pf == null){
			pf = job2.defaultPage(); //currently disallow page setup because at least on OS X cropping prevents the 
			//pf = job2.pageDialog(job2.defaultPage());
		}
		job2.validatePage(pf);
		job2.setPrintable(new P2Text(s, font), pf);  

		try {
			job2.print();  
		} 
		catch (Exception ex) {
			//  ex.printStackTrace();
		}

	}

	public void end(){

	}
	public class P2Text implements Printable {
		StringBuffer sB;
		Font font;
		StringInABox sBox;
		public P2Text(String s, Font font){
			sB = new StringBuffer(s);
			this.font = font;
			sBox = new StringInABox(sB, font, 1000);  
		}
		public int print(Graphics g, PageFormat pf, int pi) throws PrinterException {
			if (font !=null)
				g.setFont(font);
			Graphics2D g2 = (Graphics2D)g;
			AffineTransform at = g2.getTransform();
			double scale = at.getScaleX();
			int effectivePageWidth = (int)(pf.getImageableWidth()/scale);
			sBox.setWidth(effectivePageWidth);

			double dPagesHigh =  (sBox.getHeight() * scale)/pf.getImageableHeight(); //need to add pixels for number of pages in case lines cut
			int pagesHigh = (int)dPagesHigh;
			if ((double)pagesHigh != dPagesHigh)
				pagesHigh++;

			if (pi >= pagesHigh) {
				return Printable.NO_SUCH_PAGE;
			}

			double dEffectivePageHeight = pf.getImageableHeight()/scale;

			g2.scale(1/scale, 1/at.getScaleY());  //why this is & following line are needed isn't clear, but without it OS X 10.2 crops instead of scales
			g2.scale(scale, at.getScaleY());//why this is & preceding line are needed isn't clear, but without it OS X 10.2 crops instead of scales
			g2.translate((int)(pf.getImageableX() + 0.5),(int)(-dEffectivePageHeight*pi + pf.getImageableY() + 0.5));
			sBox.draw(g2, 0, 0, 0, 99999999); 
			return Printable.PAGE_EXISTS;

		}
	}
	public class P2 implements Printable {
		public int print(Graphics g, PageFormat pf, int pi) throws PrinterException {
			if (font !=null)
				g.setFont(font);
			Graphics2D g2 = (Graphics2D)g;
			AffineTransform at = g2.getTransform();
			double scale = at.getScaleX();
			if (fitToPage >= 0){ // not using natural size; fit into page
				if (pi >= 1 || dimension == null || dimension.width <= 0 || dimension.height <= 0) {
					return Printable.NO_SUCH_PAGE;
				}
				double shrinkWidth = pf.getImageableWidth()*1.0/dimension.width;
				double shrinkHeight = pf.getImageableHeight()*1.0/dimension.height;
				double shrink;
				if (shrinkWidth< shrinkHeight)
					shrink = shrinkWidth;
				else
					shrink = shrinkHeight;
				g2.translate((int)(pf.getImageableX() + 0.5),(int)(pf.getImageableY() + 0.5));
				g2.scale(shrink, shrink);
			}
			else {  //print at current size using page setup information
				if (dimension == null) {
					dimension = component.getSize();
				}
				double dPagesWide =  (dimension.width * scale)/pf.getImageableWidth();
				double dPagesHigh =  (dimension.height * scale)/pf.getImageableHeight();
				int pagesWide = (int)dPagesWide;
				int pagesHigh = (int)dPagesHigh;

				if ((double)pagesWide != dPagesWide)
					pagesWide++;
				if ((double)pagesHigh != dPagesHigh)
					pagesHigh++;

				if (pi >= pagesWide*pagesHigh) {
					return Printable.NO_SUCH_PAGE;
				}

				double dEffectivePageWidth = pf.getImageableWidth()/scale;
				double dEffectivePageHeight = pf.getImageableHeight()/scale;
				int piW = pi % pagesWide;
				int piH = pi / pagesWide;

				g2.scale(1/scale, 1/at.getScaleY());  //why this is & following line are needed isn't clear, but without it OS X 10.2 crops instead of scales
				g2.scale(scale, at.getScaleY());//why this is & preceding line are needed isn't clear, but without it OS X 10.2 crops instead of scales
				g2.translate((int)(-dEffectivePageWidth*piW + pf.getImageableX() + 0.5),(int)(-dEffectivePageHeight*piH + pf.getImageableY() + 0.5));
			}


			component.printAll((Graphics2D) g);
			return Printable.PAGE_EXISTS;
		}
	}
}


