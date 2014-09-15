/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 



This class originally by P. Midford March 2005, based on iText example files and MesquitePrintJob.

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)

Modified May 02 especially for annotations*/
package mesquite.lib;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.awt.print.*;
import java.io.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;


/* ======================================================================== */

/**
@author Peter Midford
 */
public class MesquitePDFFile {

	int fitToPage;
	PrintJob job1;
	PrinterJob job2;
	Document document = null;
	String name;
	MesquiteWindow frame;
	Dimension dimension;
	PageFormat pf;
	String pdfPathString = "";
	com.lowagie.text.Rectangle pageRectangle;
	PdfWriter writer;
	PdfContentByte cb;
	PdfTemplate tp;

	/*.......................................................................*/
	protected MesquitePDFFile(MesquiteWindow w, String name) {
		this.frame = w;
		if (name == null)
			this.name = "Print to PDF";
		else
			this.name = name;
	}	

	/**
	@param frame the MesquiteWindow to copy to a PDF file
	@param name string holding the title of the *file choice menu*
	@param fitToPage integer controlling whether the image is fit to the page
	 */
	public static MesquitePDFFile getPDFFile(MesquiteWindow w, String name) {
		MesquitePDFFile f = new MesquitePDFFile(w,name);
		if (f.prepareDocument())
			return f;
		else
			return null;
	}

	/**
	@return boolean
	 */
	public boolean prepareDocument() {

		// This is different because a file is being created
		String suggestedFileName;
		MainThread.incrementSuppressWaitWindow();
		MesquiteFileDialog fdlg= new MesquiteFileDialog(this.frame, this.name, FileDialog.SAVE);   // Save File dialog box
		suggestedFileName = "untitled.pdf";
		fdlg.setFile(suggestedFileName);

		fdlg.setBackground(ColorTheme.getInterfaceBackground());
		fdlg.setVisible(true);
		if (fdlg.getFile() == null) {
			// fdlg.dispose();
			MainThread.decrementSuppressWaitWindow();
			return false;
		}
		pdfPathString = fdlg.getDirectory()+fdlg.getFile();
		// fdlg.dispose();
		// Back to getting page settings.

		job2 = PrinterJob.getPrinterJob();
		if (job2 == null) {
			MesquiteTrunk.mesquiteTrunk.alert("Error: no printer job returned in prepareDocument");
			MainThread.decrementSuppressWaitWindow();
			return false;
		}
		pf = job2.defaultPage();
		if (pf == null) {
			MesquiteTrunk.mesquiteTrunk.alert("Error: no default page returned in prepareDocument");
			MainThread.decrementSuppressWaitWindow();
			return false;

		}
		MainThread.decrementSuppressWaitWindow();
		return true;		
	}


	/**
	@param fitToPage
	 */
	public void setSizeOrientation(int fitToPage) {
		this.fitToPage = fitToPage;
		if (job2 == null) 
			return;
		else {
			switch (fitToPage) {
			case MesquitePrintJob.AUTOFIT: {   //fits landscape or portrait depending on what requires least shrinkage
				break;
			}
			case MesquitePrintJob.FIT_LANDSCAPE: { //fits into landscape
				pf.setOrientation(PageFormat.LANDSCAPE);
				break;
			}
			case MesquitePrintJob.FIT_PORTRAIT: {  //fits into portrait
				pf.setOrientation(PageFormat.PORTRAIT);
				break;
			}
			default: {    //prints at current size using page setup (thus, possibly over multiple pages)
				PageFormat tmp = job2.pageDialog(pf);
				if (tmp == null) 
					MesquiteTrunk.mesquiteTrunk.alert("Warning: no page dialog returned in prepareDocument - will use default");
				else
					pf = tmp;	
			}
			}
			return;
		}
	}			

	/** Utility
	 *@return Array
	 */
	private float[] zeroPageMatrix() {
		float zero[] = new float[6];
		for (int i=0; i<6; i++)
			zero[i] = 0.0f;
		return zero;
	}

	/**
	 *@arg document Document object representing the output file
	 * Add metadata to the PDF fle
	 */
	private void addMetaData(Document document) {
		document.addCreator("Mesquite " + MesquiteModule.getMesquiteVersion() + " using portions of " + document.getVersion());
		try {
			String uname = System.getProperty("user.name");
			document.addAuthor(uname);
		}
		catch (SecurityException e) {
			document.addAuthor("Unknown");
		}
		document.addKeywords("Mesquite");   // more later
		//document.addTitle(this.component.getParent().getName());  // ToDo: get the window's actual title 
	}

	/**
	@arg s String holds the text that the pdf file will contain
	@arg font java.awt.Font the font is specified this way for compatibility with the similar method in MesquitePrintJob
	 */
	public void printText(String s, java.awt.Font font) {
		final String exceptionMessage = "Error, an exception occurred while creating the pdf text document: ";
		if (s == null || font == null)
			return;

		//do the translation from logical to physical font here		
		//currently, the only font this method ever gets called with is "Monospaced",PLAIN,10.  So the
		//translation effort here will be minimal		
		int desiredFontFamily;   // "Monospaced" isn't defined in com.lowagie.text.Font
		if (font.getFamily().equals("Monospaced")) 
			desiredFontFamily = com.lowagie.text.Font.COURIER;
		else desiredFontFamily = com.lowagie.text.Font.TIMES_ROMAN;
		com.lowagie.text.Font textFont;
		switch (font.getStyle()) {
		case java.awt.Font.BOLD: {
			textFont = new com.lowagie.text.Font(desiredFontFamily,font.getSize(),com.lowagie.text.Font.BOLD);
			break;
		}
		case java.awt.Font.ITALIC: {
			textFont = new com.lowagie.text.Font(desiredFontFamily,font.getSize(),com.lowagie.text.Font.ITALIC);
			break;
		}
		case java.awt.Font.PLAIN: {
			textFont = new com.lowagie.text.Font(desiredFontFamily,font.getSize(),com.lowagie.text.Font.NORMAL);
			break;
		}
		default: {
			textFont = new com.lowagie.text.Font(desiredFontFamily,font.getSize(),com.lowagie.text.Font.BOLDITALIC);
		}
		}  
		document = new Document();
		try{
			PdfWriter.getInstance(document,new FileOutputStream(pdfPathString));
			addMetaData(document);
			document.open();
			document.add(new Paragraph(s,textFont));
		}
		catch (DocumentException de) {
			MesquiteTrunk.mesquiteTrunk.alert(exceptionMessage + de.getMessage());
		}
		catch (IOException ioe) {
			MesquiteTrunk.mesquiteTrunk.alert(exceptionMessage  + ioe.getMessage());
		}
		this.end();
	}




	/**
	@arg component Component to display
	@arg dim
	@return Graphics for pdf output
	 */
	public Graphics getPDFGraphicsForComponent(Component component, Dimension dim) {
		final String exceptionMessage = "Error, an exception occurred while creating the PDF document: ";
		float pageMatrix[] = zeroPageMatrix();
		int pageOrientation = PageFormat.LANDSCAPE;
		float imageableHeight;
		float imageableWidth;
		float pageHeight;   //dimensions of the virtual page, not the imageable area
		float pageWidth;    // actually these are only different for Java2D pages
		Graphics g = null;

		if ((component == null)) { 
			return null;
		}
		if (dim == null)
			dimension = component.getSize();
		else
			dimension = dim;

		if (fitToPage >= 0) {
			if (dimension == null || dimension.width <= 0 || dimension.height <= 0) {
				return null;
			}
			double shrinkWidth;
			double shrinkHeight;
			double shrinkRatioLANDSCAPE = 0.0;
			double shrinkRatioPORTRAIT = 0.0;
			double shrink;
			//Java2Davailable == true
			if ((job2 == null) || pf == null)
				return null;
			//pf = job2.defaultPage();

			pf.setOrientation(PageFormat.LANDSCAPE);
			shrinkWidth = pf.getImageableWidth()*1.0/dimension.width;
			shrinkHeight = pf.getImageableHeight()*1.0/dimension.height;
			if (shrinkWidth < shrinkHeight)
				shrinkRatioLANDSCAPE = shrinkWidth;
			else
				shrinkRatioLANDSCAPE = shrinkHeight;	
			pf.setOrientation(PageFormat.PORTRAIT);
			shrinkWidth = pf.getImageableWidth()*1.0/dimension.width;
			shrinkHeight = pf.getImageableHeight()*1.0/dimension.height;
			if (shrinkWidth < shrinkHeight)
				shrinkRatioPORTRAIT = shrinkWidth;
			else
				shrinkRatioPORTRAIT = shrinkHeight;
			if (shrinkRatioPORTRAIT < shrinkRatioLANDSCAPE){
				pf.setOrientation(PageFormat.LANDSCAPE);
				shrink = shrinkRatioLANDSCAPE;
			}
			else {
				pf.setOrientation(PageFormat.PORTRAIT);
				shrink = shrinkRatioPORTRAIT;
			}	
			pageOrientation = pf.getOrientation();

			imageableWidth = (float)pf.getImageableWidth();
			imageableHeight = (float)pf.getImageableHeight();
			pageWidth = (float)pf.getWidth();
			pageHeight = (float)pf.getHeight();
			//pageMatrix = zeroPageMatrix();
			pageMatrix[0] = (float)shrink;
			pageMatrix[3] = (float)shrink;  

		}
		else {    // not fit to page
			// Java2Davailable == true
			if (job2 == null) 
				return null;
			imageableWidth = (float)pf.getImageableWidth();
			imageableHeight = (float)pf.getImageableHeight();
			pageHeight = (float)pf.getHeight();
			pageWidth = (float)pf.getWidth(); 
			//pageMatrix = pf.getMatrix();
			//pageMatrix = zeroPageMatrix();
			pageOrientation = pf.getOrientation();
			pageMatrix[0] = pageMatrix[3] = 1f;

		}
		pageRectangle = new com.lowagie.text.Rectangle(0.0f,imageableHeight,imageableWidth,0.0f);
		try {
			document = new Document(pageRectangle);
			writer = PdfWriter.getInstance(document,new FileOutputStream(pdfPathString)); 
			addMetaData(document);
			document.open();
			cb = writer.getDirectContent();
			tp = cb.createTemplate((int)pageWidth, (int)pageHeight);  //dump this??

			cb.concatCTM(pageMatrix[0],pageMatrix[1],pageMatrix[2],pageMatrix[3],pageMatrix[4],pageMatrix[5]);
			g = cb.createGraphics((float)dimension.getWidth(),(float)dimension.getHeight()); //HEADLESS :  comment this line out for headless mode

		}
		catch (java.io.IOException e) {
			MesquiteTrunk.mesquiteTrunk.alert(exceptionMessage + e);
		}
		catch (com.lowagie.text.BadElementException e) {
			MesquiteTrunk.mesquiteTrunk.alert(exceptionMessage + e);
		}
		catch (com.lowagie.text.DocumentException e) {
			MesquiteTrunk.mesquiteTrunk.alert(exceptionMessage + e);
		}
		return g;
	}

	/**
	@arg component Component to display
	@arg dim
	@arg font
	 */
	public void printComponent(Component component, Dimension dim, java.awt.Font font) {
		final String exceptionMessage = "Error, an exception occurred while creating the PDF document: ";
		float pageMatrix[] = zeroPageMatrix();
		int pageOrientation = PageFormat.LANDSCAPE;
		float imageableHeight;
		float imageableWidth;
		float pageHeight;   //dimensions of the virtual page, not the imageable area
		float pageWidth;    // actually these are only different for Java2D pages


		if ((component == null)) { 
			return;
		}
		if (dim == null)
			dimension = component.getSize();
		else
			dimension = dim;

		if (fitToPage >= 0) {
			if (dimension == null || dimension.width <= 0 || dimension.height <= 0) {
				return;
			}
			double shrinkWidth;
			double shrinkHeight;
			double shrinkRatioLANDSCAPE = 0.0;
			double shrinkRatioPORTRAIT = 0.0;
			double shrink;
			   //Java2Davailable == true
				if ((job2 == null) || pf == null)
					return;
				//pf = job2.defaultPage();

				pf.setOrientation(PageFormat.LANDSCAPE);
				shrinkWidth = pf.getImageableWidth()*1.0/dimension.width;
				shrinkHeight = pf.getImageableHeight()*1.0/dimension.height;
				if (shrinkWidth < shrinkHeight)
					shrinkRatioLANDSCAPE = shrinkWidth;
				else
					shrinkRatioLANDSCAPE = shrinkHeight;	
				pf.setOrientation(PageFormat.PORTRAIT);
				shrinkWidth = pf.getImageableWidth()*1.0/dimension.width;
				shrinkHeight = pf.getImageableHeight()*1.0/dimension.height;
				if (shrinkWidth < shrinkHeight)
					shrinkRatioPORTRAIT = shrinkWidth;
				else
					shrinkRatioPORTRAIT = shrinkHeight;
				if (shrinkRatioPORTRAIT < shrinkRatioLANDSCAPE){
					pf.setOrientation(PageFormat.LANDSCAPE);
					shrink = shrinkRatioLANDSCAPE;
				}
				else {
					pf.setOrientation(PageFormat.PORTRAIT);
					shrink = shrinkRatioPORTRAIT;
				}	
				pageOrientation = pf.getOrientation();

				imageableWidth = (float)pf.getImageableWidth();
				imageableHeight = (float)pf.getImageableHeight();
				pageWidth = (float)pf.getWidth();
				pageHeight = (float)pf.getHeight();
				//pageMatrix = zeroPageMatrix();
				pageMatrix[0] = (float)shrink;
				pageMatrix[3] = (float)shrink;  
			}
		
		else {    // not fit to page
			    // Java2Davailable == true
				if (job2 == null) 
					return;
				imageableWidth = (float)pf.getImageableWidth();
				imageableHeight = (float)pf.getImageableHeight();
				pageHeight = (float)pf.getHeight();
				pageWidth = (float)pf.getWidth(); 
				//pageMatrix = zeroPageMatrix();
				pageOrientation = pf.getOrientation();
				pageMatrix[0] = pageMatrix[3] = 1f;
			
		}
		pageRectangle = new com.lowagie.text.Rectangle(0.0f,imageableHeight,imageableWidth,0.0f);
		try {
			document = new Document(pageRectangle);
			writer = PdfWriter.getInstance(document,new FileOutputStream(pdfPathString)); 
			addMetaData(document);
			document.open();
			cb = writer.getDirectContent();
			tp = cb.createTemplate((int)pageWidth, (int)pageHeight);  //dump this??

			java.awt.Image jImage= component.createImage((int)dimension.getWidth(),(int)dimension.getHeight());

			Graphics j2 = jImage.getGraphics();
			component.printAll(j2);
			//com.lowagie.text.Image outImage = null; //HEADLESS  Use this line for headless mode
			com.lowagie.text.Image outImage = com.lowagie.text.Image.getInstance(jImage,null);  //HEADLESS  Comment this line out for headless mode
			float verticalIncrement = imageableHeight/pageMatrix[0];
			float horizontalIncrement = imageableWidth/pageMatrix[3];
			float heightLimit = (imageableHeight/pageMatrix[0]);
			float widthLimit = -1*outImage.width();
			float verticalStart = (imageableHeight/pageMatrix[0])-outImage.height();
			for (float vertical = verticalStart; vertical < heightLimit ; vertical += verticalIncrement) {
				for (float horizontal = 0; horizontal > widthLimit; horizontal -= horizontalIncrement) {
					document.newPage();
					cb.concatCTM(pageMatrix[0],pageMatrix[1],pageMatrix[2],pageMatrix[3],pageMatrix[4],pageMatrix[5]);
					switch (pageOrientation) {
					case PageFormat.LANDSCAPE: {
						cb.addImage(outImage,outImage.width(),0f,0f,outImage.height(),horizontal,vertical);
						break;
					}
					case PageFormat.PORTRAIT: 
					default: {
						cb.addImage(outImage,outImage.width(),0.0f,0.0f,outImage.height(),horizontal,vertical);
						break;
					}
					}
				}	
			}
		}
		catch (java.io.IOException e) {
			MesquiteTrunk.mesquiteTrunk.alert(exceptionMessage + e);
		}
		catch (com.lowagie.text.BadElementException e) {
			MesquiteTrunk.mesquiteTrunk.alert(exceptionMessage + e);
		}
		catch (com.lowagie.text.DocumentException e) {
			MesquiteTrunk.mesquiteTrunk.alert(exceptionMessage + e);
		}
		end();
	}


	/**
	 */
	public void end() {
		// step 5: we close the document
		if (document != null)
			document.close();
	}

}
