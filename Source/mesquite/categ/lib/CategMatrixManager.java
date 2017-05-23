/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 

 
 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
 Perhaps with your help we can be more than a few, and make Mesquite better.
 
 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
 Mesquite's web site is http://mesquiteproject.org
 
 This source code and its compiled class files are free and modifiable under the terms of 
 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.categ.lib;

import java.awt.*;
import java.net.*;
import java.util.*;
import java.io.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;



public abstract class CategMatrixManager extends CharMatrixManager   {
	
	public Class getDutyClass() {
		return CategMatrixManager.class;
	}
	public String getDutyName() {
		return "Manager of general categorical data matrix, including read/write CHARACTERS block";
	}
	
	public Class getDataClass(){
		return CategoricalData.class;
	}
	/*.................................................................................................................*/
	private int writeCharactersBlockPart(CharacterData data, CharactersBlock cB, StringBuffer blocks, int startChar, int endChar, int tot, int numTotal, MesquiteFile file, ProgressIndicator progIndicator) {
		if (startChar >= data.getNumChars()) {
			file.writeLine(blocks.toString());
			blocks.setLength(0);
			return 0;
		}
		if (endChar> data.getNumChars())
			endChar = data.getNumChars();

		String taxonName="";
		String taxonNameToWrite = "";
		int maxNameLength = data.getTaxa().getLongestTaxonNameLength();
		int invalidIC = -1;
		int invalidIT = -1;
		for (int it=0; it<data.getTaxa().getNumTaxa(); it++) {
			if ((file.writeTaxaWithAllMissing || data.someApplicableInTaxon(it, false)) && (!file.writeOnlySelectedTaxa || data.getTaxa().getSelected(it))&& file.filterTaxon(data, it)){
				taxonName = data.getTaxa().getTaxon(it).getName();
				if (taxonName!=null) {
					if (file.useStandardizedTaxonNames)
						taxonNameToWrite = "t" + it;
					else
						taxonNameToWrite = StringUtil.simplifyIfNeededForOutput(taxonName,file.simplifyNames);
					blocks.append("\t"+taxonNameToWrite);
					boolean spaceWritten = false;
					for (int i = 0; i<(maxNameLength-taxonNameToWrite.length()+2); i++){
						blocks.append(" ");
						spaceWritten = true;
					}
					if (!spaceWritten)
						blocks.append(" ");
				}
				if (progIndicator != null)
					progIndicator.setText("Writing data for taxon " + taxonName);
				int totInTax = 0;

				//USE SYMBOLS
				for (int ic=startChar;  ic<endChar; ic++) {
					if ((data.isCurrentlyIncluded(ic) || file.writeExcludedCharacters) ) {
						if (!data.isValid(ic, it) && invalidIC <0){
							invalidIC = ic;
							invalidIT = it;
						}
						data.statesIntoNEXUSStringBuffer(ic, it, blocks);
						tot++;
						totInTax++;
						if (numTotal>100000000) {
							if (tot % 10000000 == 0  && isLogVerbose())
								logln("\nComposing matrix: " + tot + " of " + numTotal);
							else if (tot % 10000 == 0  && isLogVerbose())
								log(".");
						}
						else if (numTotal>10000000) {
							if (tot % 1000000 == 0  && isLogVerbose())
								logln("Composing matrix: " + tot + " of " + numTotal);
						} else if (tot % 100000 == 0  && isLogVerbose())
							logln("Composing matrix: " + tot + " of " + numTotal);
					//	27 July 08:  DRM commented out the following two lines.  These cannot be included, as NEXUS files are sensitive to line breaks within the MATRIX command.
					
						// if (totInTax % 2000 == 0)
					//		blocks.append(StringUtil.lineEnding());//this is here because of current problem (at least on mrj 2.2) of long line slow writing
					}
				}
				file.writeLine(blocks.toString());
				blocks.setLength(0);
			}
		}
		if (invalidIC >=0){
			String s = "The matrix " + data.getName() + " has invalid characters states (e.g., character " + (invalidIC +1) + " in taxon " + (invalidIT + 1) + ")";
			if (warnedInvalid)
				logln(s);
			else
				alert(s);
			warnedInvalid = true;
		}
		file.writeLine(blocks.toString());
		blocks.setLength(0);
		return tot;
	}
	boolean warnedInvalid = false;
	/*.................................................................................................................*/
	public void writeNexusMatrix(CharacterData data, CharactersBlock cB, StringBuffer blocks, MesquiteFile file, ProgressIndicator progIndicator){
		warnedInvalid = false;
		blocks.append("\tMATRIX" + StringUtil.lineEnding());
		int numCharsToWrite;
		if (file.writeExcludedCharacters)
			numCharsToWrite = data.getNumChars();
		else
			numCharsToWrite = data.getNumCharsIncluded();
		int numTotal = data.getNumTaxa() * numCharsToWrite;
		//MesquiteModule.mesquiteTrunk.mesquiteMessage("Composing DNA matrix ", numTotal, 0);
		if (data.interleaved && data.interleavedLength>0 && file.interleaveAllowed) {
			int numBlocks = (int) (data.getNumChars() / data.interleavedLength);
			int tot = 0;
			for (int ib = 0; ib<=numBlocks; ib++) {
				if (ib>0)
					blocks.append(StringUtil.lineEnding());
				tot = writeCharactersBlockPart(data, cB, blocks, ib*data.interleavedLength,(ib+1)*data.interleavedLength, tot, numTotal, file, progIndicator);
			}
		}
		else
			writeCharactersBlockPart(data, cB, blocks, 0,data.getNumChars(), 0, numTotal, file, progIndicator);
		if (progIndicator !=null)
			progIndicator.setText("Finished writing matrix");
		blocks.append(";" + StringUtil.lineEnding());
		file.writeLine(blocks.toString());
		blocks.setLength(0);
	}
	
}


