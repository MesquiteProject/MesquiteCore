/* Mesquite (package mesquite.io).  Copyright 2000-2006 D. Maddison and W. Maddison. 
Version 1.11, June 2006.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.io.ExportDistanceMatrix;

import mesquite.lib.characters.CharacterData;
import mesquite.distance.lib.IncTaxaDistanceSource;
import mesquite.distance.lib.TaxaDistance;
import mesquite.lib.*;
import mesquite.lib.duties.FileInterpreterI;
import mesquite.lib.duties.OneTreeSource;
import mesquite.io.lib.DistanceMatrixExporterDialog;

public class ExportDistanceMatrix extends FileInterpreterI{
	public static final int TABDELIMITER=0;
	public static final int COMMADELIMITER=1;
	public static final int SPACEDELIMITER=2;
	public static final int NEWLINEDELIMITER=3;
	
	public static final int COLUMNWIDTH=15;     //maybe this should be user modifiable, within limits (>=8?)

	IncTaxaDistanceSource distSource;
	public boolean addRowAndColumnHeaders; 
	public int columnDelimiterChoice;
	public int lineDelimiterChoice;

	
	
	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		return true;
	}

	public boolean canImport(){
		return false;
	}
	
	public boolean canExport(){
		return true;
	}
	
	public boolean getExportOptions(boolean dataSelected, boolean taxaSelected){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		DistanceMatrixExporterDialog exportDialog = new DistanceMatrixExporterDialog(this,containerOfModule(), "Export Distance Matrix Options", buttonPressed);
		
		exportDialog.completeAndShowDialog(dataSelected, taxaSelected);
			
		boolean ok = (exportDialog.query(dataSelected, taxaSelected)==0);

		exportDialog.dispose();
		return ok;
	}	
	
	/*.................................................................................................................*/
	public String getColumnDelimiter() {
 		if (columnDelimiterChoice == TABDELIMITER) 
 			return "\t";
 		else if (columnDelimiterChoice == COMMADELIMITER) 
 			return ",";
 		else if (columnDelimiterChoice == SPACEDELIMITER) 
 			return " ";
 		else if (columnDelimiterChoice == NEWLINEDELIMITER) 
 			return getLineEnding();
 		return " ";   // right default?
   	 }

	public void exportFile(MesquiteFile file, String arguments, CommandRecord commandRec) { //if file is null, consider whole project open to export
		Arguments args = new Arguments(new Parser(arguments), true);
		boolean usePrevious = args.parameterExists("usePrevious");  //Wayne: should I be using this? how?
 		distSource =  (IncTaxaDistanceSource)hireEmployee(commandRec, IncTaxaDistanceSource.class,"Source of distances to dump to file");
 		if (distSource == null) {
 			alert(getName() + " couldn't set up the Distance File generator because no distance calculator was obtained.");
 		}
		CharacterData data = getProject().chooseData(containerOfModule(), file, null, null, "Select data to export", commandRec);
		if (data ==null) {
			showLogWindow(true);
			logln("WARNING: No suitable data available for export to a file of format \"" + getName() + "\".  The file will not be written.\n");
			return;
		}
		Taxa taxa = data.getTaxa();
		OneTreeSource treeTask = (OneTreeSource)hireEmployee(commandRec, OneTreeSource.class, "Source of tree for calculating tree-based distance measures");
		Tree tree = null;
		if (!commandRec.scripting() && !usePrevious)
			if (!getExportOptions(data.anySelected(), taxa.anySelected()))
				return;
		TaxaDistance distanceSource =  distSource.getTaxaDistance(taxa, commandRec);
		double[][] distances = distanceSource.getMatrix();
		if (distances.length != taxa.getNumTaxa())
			MesquiteMessage.warnProgrammer("Distances dimension was " + distances.length + "; numTaxa was " + taxa.getNumTaxa());
		int numRows = Double2DArray.numFullRows(distances);
		int numColumns = Double2DArray.numFullColumns(distances);
		String columnDelimiter = getColumnDelimiter(); 
		String lineDelimiter = getLineEnding();

		StringBuffer reportString = new StringBuffer(2*numRows*numColumns);
		if (addRowAndColumnHeaders){
			for (int i=0;i<COLUMNWIDTH;i++)
				reportString.append(' ');
			for (int i=0; i<numColumns;i++){
				String cheader = taxa.getTaxon(i).getName();
				if (cheader.length() > COLUMNWIDTH)
					reportString.append(cheader.substring(0,COLUMNWIDTH));
				else{
					for(int j=cheader.length()-1;j<COLUMNWIDTH;j++)
						reportString.append(' ');
					reportString.append(cheader);
				}
				reportString.append(columnDelimiter);
			}
			reportString.append(lineDelimiter);
		}
		for (int j=0; j<numRows; j++) {
			if (addRowAndColumnHeaders){
				String rheader = taxa.getTaxon(j).getName();
				if (rheader.length() > COLUMNWIDTH)
					reportString.append(rheader.substring(0,COLUMNWIDTH));
				else{
					for(int k=rheader.length();k<COLUMNWIDTH;k++)
						reportString.append(' ');
					reportString.append(rheader);
				}
				reportString.append(columnDelimiter);
			}
			for (int i=0; i<numColumns; i++) {
				double tmp = distances[i][j];
				if (!MesquiteDouble.isCombinable(tmp))
					tmp = -9.999999999E-99;
				String myDist = MesquiteDouble.toFixedWidthString(tmp, COLUMNWIDTH);
				String tmp1 = myDist.substring(0,4);
				String tmp2 = myDist.substring(5);
				if (myDist.startsWith("10.0"))
					myDist = tmp1+tmp2;
				reportString.append(myDist);
				reportString.append(columnDelimiter);
			}
			reportString.append(lineDelimiter);
			}
		if (reportString.length()>0)
			saveExportedFileWithExtension(reportString, arguments, "dst", commandRec);
 		fireEmployee(distSource);
 		fireEmployee(treeTask);

	}
			
	
    /*.................................................................................................................*/
    public String getAuthors() {
        return "Peter Midford and Wayne Maddison";
    }

    /*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
    public String getExplanation() {
        return "Generates distance matrices in the manner of PDDIST (Garland, T., Jr., and A. R. Ives. 2000)" ;
    }

  	public boolean isPrerelease() {
	    return true;
   	}

  	public boolean isSubstantive(){
  		return true;
  	}
  	
	public String getName() {
		return "Export Distance Matrix";
	}
	


	public void readFile(MesquiteProject mf, MesquiteFile mNF, String arguments, CommandRecord commandRec) {
		// TODO Auto-generated method stub
		
	}

}
