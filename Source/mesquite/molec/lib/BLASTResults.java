package mesquite.molec.lib;

import java.util.Iterator;
import java.util.List;

import org.apache.hivemind.util.PropertyUtils;
import org.dom4j.*;

import mesquite.categ.lib.DNAData;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteString;
import mesquite.lib.Parser;
import mesquite.lib.PropertyNamesProvider;
import mesquite.lib.StringArray;
import mesquite.lib.StringUtil;
import mesquite.lib.XMLUtil;

public class BLASTResults {
	protected double[] eValue;
	protected double[] bitScore ;
	protected String[] taxonomy;
	protected String[] definition;
	protected String[] accession;
	protected String[] ID;
	protected String[] sequence;
	int maxHits = 1;

	public BLASTResults (int maxHits) {
		this.maxHits = maxHits;
		initialize();
	}
	public void initialize() {
		eValue = new double[maxHits];
		bitScore = new double[maxHits];
		taxonomy = new String[maxHits];
		definition = new String[maxHits];
		accession = new String[maxHits];
		ID = new String[maxHits];
		zeroArrays();
	}
	public void zeroArrays() {
		for (int i=0; i<maxHits; i++) {
			eValue[i]= -1.0;
			bitScore[i] = 0.0;
			taxonomy[i] = "";
			definition[i] = "";
			accession[i] = "";
			ID[i] = "";
		}
	}
	public double geteValue(int index) {
		return eValue[index];
	}
	public void seteValue(double eValue, int index) {
		this.eValue[index] = eValue;
	}
	public double getBitScore(int index) {
		return bitScore[index];
	}
	public void setBitScore(double bitScore, int index) {
		this.bitScore[index] = bitScore;
	}
	public String getTaxonomy(int index) {
		return taxonomy[index];
	}
	public void setTaxonomy(String taxonomy, int index) {
		this.taxonomy[index] = taxonomy;
	}
	public String getDefinition(int index) {
		return definition[index];
	}
	public void setDefinition(String definition, int index) {
		this.definition[index] = definition;
	}
	public String getAccession(int index) {
		return accession[index];
	}
	public void setAccession(String accession, int index) {
		this.accession[index] = accession;
	}
	public String[] getAccessions() {
		return accession;
	}
	public String getID(int index) {
		return ID[index];
	}
	public void setID(String ID, int index) {
		this.ID[index] = ID;
	}
	public String[] getIDs() {
		return ID;
	}


	public String getSequence(int index) {
		return sequence[index];
	}
	public void setSequence(String sequence, int index) {
		this.sequence[index] = sequence;
	}

	public String toString(int numHits) {
		StringBuffer sb = new StringBuffer();
		sb.append("   Top hits\n\tAccession [eValue] Definition): \n");
		for (int i=0; i<maxHits && i<numHits && i<accession.length; i++) {
			if (StringUtil.notEmpty(accession[i]))
				sb.append("\t"+ accession[i] + "\t[" + eValue[i]+ "]\t" + definition[i]+"\n");
		}
		return sb.toString();
	}


	/*.................................................................................................................*/
	public boolean acceptableHit(int hitCount, double bitScore, double eValue) {
		return hitCount<=maxHits;
	}

	/*.................................................................................................................*/
	public  void setIDFromDefinition(String separator, int index){
		String s="";
		for (int i=0; i<maxHits && i<ID.length; i++) {
			if (StringUtil.notEmpty(definition[i])){
				s=StringUtil.getItem(definition[i],"|", index);
				if (StringUtil.notEmpty(s))
					ID[i] = s;
			}
		}

	}
	/*.................................................................................................................*/
	public  void setAccessionFromDefinition(String separator, int index){
		String s="";
		for (int i=0; i<maxHits && i<accession.length; i++) {
			if (StringUtil.notEmpty(definition[i])){
				s=StringUtil.getItem(definition[i],"|", index);
				if (StringUtil.notEmpty(s))
					accession[i] = s;
			}
		}

	}
	/*.................................................................................................................*/
	public  void setAccessionsFromIDs(boolean nucleotides){
		accession = NCBIUtil.getGenBankAccessionFromID(ID, nucleotides, null, false);
	}

	/*.................................................................................................................*/
	public  boolean processResultsFromBLAST(String response, boolean storeSequences, double eValueCutoff){
		if (accession==null)
			return false;
		zeroArrays();
		Element blastOutputElement = XMLUtil.getRootXMLElementFromString("BlastOutput",response);
		if (blastOutputElement==null)
			return false;
		int hitCount = 0;
		Element blastIterationsElement = blastOutputElement.element("BlastOutput_iterations");
		if (blastIterationsElement!=null) {
			Element IterationsElement = blastIterationsElement.element("Iteration");
			if (IterationsElement!=null) {
				Element IterationHitElement = IterationsElement.element("Iteration_hits");
				if (IterationHitElement!=null) {
					List hitList = IterationHitElement.elements("Hit");
					for (Iterator iter = hitList.iterator(); iter.hasNext() && hitCount<maxHits;) {   // this is going through all of the hits
						Element hitElement = (Element) iter.next();
						if (hitElement!=null) {

							Element hitID = hitElement.element("Hit_id");

							setDefinition(hitElement.elementText("Hit_def"), hitCount);
							String s = hitElement.elementText("Hit_accession");
							setAccession(s, hitCount);

							s = hitElement.elementText("Hit_id");
							s=StringUtil.getItem(s,"|", 2);
							if (StringUtil.notEmpty(s))
								setID(s, hitCount);

							Element hithsps = hitElement.element("Hit_hsps");
							if (hithsps!=null) {
								Element Hsp = hithsps.element("Hsp");
								if (Hsp!=null) {
									String eValue = Hsp.elementText("Hsp_evalue");
									double eValueDouble = MesquiteDouble.fromString(eValue);
									if (eValueCutoff< 0.0 || eValueDouble<=eValueCutoff) {
										seteValue(eValueDouble, hitCount);
										setBitScore(MesquiteDouble.fromString(Hsp.elementText("Hsp_bit-score")), hitCount);

										if (storeSequences)
											setSequence(Hsp.elementText("Hsp_hseq"), hitCount);
									} else if (eValueCutoff>=0.0 && eValueDouble>eValueCutoff) {
										setDefinition("", hitCount);
										setAccession("", hitCount);
										setID("", hitCount);
										hitCount--;
									}
								}
							}
							hitCount++;
						}

					}
					return true;

				}
			}
		}
		return false;
	}


}
