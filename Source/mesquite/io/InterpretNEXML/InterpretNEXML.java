/* Mesquite source code.  Copyright 1997-2006 W. Maddison and D. Maddison.
Version 1.11, June 2006.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.io.InterpretNEXML;
/*~~  */

import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/** A file interpreter for a NEXML file format.  */
public class InterpretNEXML extends FileInterpreterI {	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, CommandRecord commandrec, boolean hiredByName) {
		return true;
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}	
	/*.................................................................................................................*/
	public boolean canExportEver() {  
		return true;
	}
	/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		return true;
	}
	/*.................................................................................................................*/
	public boolean canExportData(Class dataClass) {  
		return true;
	}
	/*.................................................................................................................*/
	public boolean canImport() {  
		return true;
	}
	/*.................................................................................................................*/
	public boolean canImport(String arguments){
		return true;
	}
	/*.................................................................................................................*/
	public void readFile(MesquiteProject mf, MesquiteFile file, String arguments) {
		incrementMenuResetSuppression();
		ProgressIndicator progIndicator = new ProgressIndicator(mf,"Importing File "+ file.getName(), file.existingLength());
		progIndicator.start();
		file.linkProgressIndicator(progIndicator);
		if (file.openReading()) {
			boolean abort = false;
			
			// XXX actual parsing happens in the class at the bottom
			LocalDomParser ldp = new LocalDomParser();
			ldp.parse(file, mf);
			
			finishImport(progIndicator, file, abort);
		}
		decrementMenuResetSuppression();
	}
	/* ============================  exporting ============================*/
	boolean compact = false;
	/*.................................................................................................................*/
	public boolean getExportOptions(boolean dataSelected, boolean taxaSelected){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExporterDialog exportDialog = new ExporterDialog(this,containerOfModule(), "Export NEXML Options", buttonPressed);

		Checkbox compactCheckBox = exportDialog.addCheckBox("compact representation", compact);

		exportDialog.completeAndShowDialog(dataSelected, taxaSelected);

		boolean ok = (exportDialog.query(dataSelected, taxaSelected)==0);

		compact = compactCheckBox.getState();
		exportDialog.dispose();
		return ok;
	}
	/*.................................................................................................................*/
	public boolean getExportOptionsSimple(boolean dataSelected, boolean taxaSelected){   // an example of a simple query, that only proved line delimiter choice; not used here
		return (ExporterDialog.query(this,containerOfModule(), "Export NEXML Options")==0);
	}
	/*.................................................................................................................*/	
	protected String getSupplementForTaxon(Taxa taxa, int it){
		return "";
	}
	/*.................................................................................................................*/
	public void exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export
		// TODO xml writing goes here?
	}
	/*.................................................................................................................*/
	public String getName() {
		return "NEXML file";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Imports and exports NEXML files." ;
	}
	/*.................................................................................................................*/

}

class LocalDomParser {

	Document dom;
	
	public LocalDomParser(){}

	public void parse( MesquiteFile file, MesquiteProject project ) {
		parseXmlFile( file );     // creates DOM
		parseDocument( project ); // traverses and instantiates objects in project
	}
		
	private void parseXmlFile( MesquiteFile file ) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); // get factory
		try {
			DocumentBuilder db = dbf.newDocumentBuilder(); // get doc builder from factory			
			dom = db.parse( file.getPath() );              // parse file to create DOM
		} catch ( ParserConfigurationException pce ) {
			pce.printStackTrace();
		} catch ( SAXException se ) {
			se.printStackTrace();
		} catch ( IOException ioe ) {
			ioe.printStackTrace();
		}
	}
	
	private void parseDocument( MesquiteProject project ){
		Element nexml = dom.getDocumentElement();           // get root
		
		/* fetch taxa elements */
		NodeList list = nexml.getElementsByTagName("taxon"); // get all otus elements
		if ( list != null && list.getLength() > 0 ) {
			for ( int i = 0; i < list.getLength(); i++ ) {								
				Element taxa = (Element)list.item(i);       //get the otus element								
				getTaxa( taxa, project );                   //create the Taxa object			
			}
		}
		
		/* fetch characters elements */
		/*
		list = nexml.getElementsByTagName("characters");
		if ( list != null && list.getLength() > 0 ) {
			for ( int i = 0; i < list.getLength(); i++ ) {								
				Element charactersElement = (Element)list.item(i);      //get the characters element								
				getCharacters( charactersElement, project );            //create the characters object			
			}
		}
		*/		
	}

	/* process taxa blocks */
	private void getTaxa(Element taxaElement, MesquiteProject project) {
		NodeList list = taxaElement.getElementsByTagName("taxon");
		Taxa taxa = project.createTaxaBlock( list.getLength() );
		taxa.setName( taxaElement.getAttribute("label") );
		taxa.setUniqueID( taxaElement.getAttribute("id") );
		if ( list != null && list.getLength() > 0 ) {
			for ( int i = 0; i < list.getLength(); i++ ) {
				Element taxonElement = (Element)list.item(i);
				Taxon taxon = taxa.getTaxon(i);
				taxon.setName( taxonElement.getAttribute("label") );
				taxon.setUniqueID( taxonElement.getAttribute("id") );
			}
		}		
	}
	
	/* process characters blocks */
	/*
	private void getCharacters(Element charactersElement, MesquiteProject project) {
		NodeList list = charactersElement.getElementsByTagName("otu");
		Taxa taxa = project.createTaxaBlock( list.getLength() );
		taxa.setName( charactersElement.getAttribute("label") );
		taxa.setUniqueID(charactersElement.getAttribute("id"));
		if ( list != null && list.getLength() > 0 ) {
			for ( int i = 0; i < list.getLength(); i++ ) {
				Element otu = (Element)list.item(i);
				Taxon taxon = taxa.getTaxon(i);
				taxon.setName( otu.getAttribute("label") );
				taxon.setUniqueID( otu.getAttribute("id") );
			}
		}		
	}	
	*/

}