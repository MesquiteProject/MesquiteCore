package mesquite.molec.lib;

import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;

import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.Parser;
import mesquite.lib.StringUtil;
import mesquite.lib.XMLUtil;

public class BLASTResults {
	protected double[] eValue;
	protected double[] bitScore ;
	protected String[] taxonomy;
	protected String[] definition;
	protected String[] accession;
	protected String[] ID;
	protected int[] frame;
	protected String[] sequence;
	protected boolean[] reversed;
	
	protected int queryStartMatch[];
	protected int queryEndMatch[];
	protected int hitStartMatch[];
	protected int hitEndMatch[];
	protected boolean hitReversed[];
	
	protected String databaseName;

	//the following specify whether to read in the whole sequences or only part of them, and the size of the flanking regions to read in
	protected boolean readPartialContig= true;

	protected int flankingRegionSizeToRead = 2000;

	protected int numHits = 0;
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
		frame = new int[maxHits];
		ID = new String[maxHits];
		reversed = new boolean[maxHits];
		
		
		queryStartMatch = new int[maxHits];
		queryEndMatch= new int[maxHits];
		hitStartMatch= new int[maxHits];
		hitEndMatch= new int[maxHits];
		hitReversed= new boolean[maxHits];

		zeroArrays();
	}
	public void zeroArrays() {
		for (int i=0; i<maxHits; i++) {
			eValue[i]= -1.0;
			bitScore[i] = 0.0;
			taxonomy[i] = "";
			definition[i] = "";
			accession[i] = "";
			frame[i] = 0;
			reversed[i] = false;
			ID[i] = "";
			queryStartMatch[i]=0;
			queryEndMatch[i]=0;
			hitStartMatch[i]=0;
			hitEndMatch[i]=0;
			hitReversed[i]=false;
		}
	}
	public String getDatabaseName() {
		return databaseName;
	}
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public int getQueryStartMatch(int index) {
		return queryStartMatch[index];
	}
	public void setQueryStartMatch(int value, int index) {
		this.queryStartMatch[index] = value;
	}
	public int getQueryEndMatch(int index) {
		return queryEndMatch[index];
	}
	public void setQueryEndMatch(int value, int index) {
		this.queryEndMatch[index] = value;
	}
	public int getHitStartMatch(int index) {
		return hitStartMatch[index];
	}
	public void setHitStartMatch(int value, int index) {
		this.hitStartMatch[index] = value;
	}
	public int getHitEndMatch(int index) {
		return hitEndMatch[index];
	}
	public void setHitEndMatch(int value, int index) {
		this.hitEndMatch[index] = value;
	}
	public boolean getHitReversed(int index) {
		return hitReversed[index];
	}
	public void setHitReversed(boolean value, int index) {
		this.hitReversed[index] = value;
	}

	public double geteValue(int index) {
		return eValue[index];
	}
	public void seteValue(double value, int index) {
		this.eValue[index] = value;
	}

	public double getBitScore(int index) {
		return bitScore[index];
	}
	public void setBitScore(double bitScore, int index) {
		this.bitScore[index] = bitScore;
	}
	public boolean getReversed(int index) {
		return reversed[index];
	}
	public void setReversed(boolean reversed, int index) {
		this.reversed[index] = reversed;
	}
	public String getTaxonomy(int index) {
		return taxonomy[index];
	}
	public void setTaxonomy(String taxonomy, int index) {
		this.taxonomy[index] = taxonomy;
	}
	public int getFrame(int index) {
		return frame[index];
	}
	public void setFrame(int frame, int index) {
		this.frame[index] = frame;
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
	public void setNumHits(int numHits) {
		this.numHits = numHits;
	}
	public int getNumHits() {
		return numHits;
	}
	public boolean getReadPartialContig() {
		return readPartialContig;
	}
	public void setReadPartialContig(boolean readPartialContig) {
		this.readPartialContig = readPartialContig;
	}
	public int getFlankingRegionSizeToRead() {
		return flankingRegionSizeToRead;
	}
	public void setFlankingRegionSizeToRead(int flankingRegionSizeToRead) {
		this.flankingRegionSizeToRead = flankingRegionSizeToRead;
	}


	public String reversedToString() {
		String s = "";
		for (int i=0; i<maxHits && i<reversed.length; i++) {
			if (reversed[i]) 
				s+= " - ";
			else 
				s+= " + ";
		}
		return s;
	}


	public String getSequence(int index) {
		return sequence[index];
	}
	public void setSequence(String sequence, int index) {
		this.sequence[index] = sequence;
	}

	public String toString(int numHits) {
		StringBuffer sb = new StringBuffer();
		sb.append("   Top hits\n\tAccession\t[eValue]\tDefinition: \n");
		for (int i=0; i<maxHits && i<numHits && i<accession.length; i++) {
			if (StringUtil.notEmpty(accession[i])){
				/*				if (reversed[i])
					sb.append("\t-");
				else 
					sb.append("\t+");
				 */				sb.append("\t"+ accession[i] + "\t[" + eValue[i]+ "]\t" + definition[i]+"\n");
			}
		}
		return sb.toString();
	}


	/*.................................................................................................................*/
	public boolean acceptableHit(int hitCount, double bitScore, double eValue) {
		return hitCount<=maxHits;
	}
	/*.................................................................................................................*/
	public boolean someHits() {
		for (int i=0; i<maxHits; i++) {
			if (StringUtil.notEmpty(getAccession(i))) {
				return true;
			}
		}
		return false;
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
	public int hitsSatisfyMatches (String[] matchInDefinitions, int minNumToMatch, int maxNumToMatch, boolean noOthers, boolean onlyMatchOnce) {  
		if (minNumToMatch==0 || matchInDefinitions==null)
			return 0;
		boolean[] alreadyMatched = new boolean[matchInDefinitions.length];
		for (int j=0; j<alreadyMatched.length; j++){
			alreadyMatched[j]=false;
		}

		int count = 0;
		for (int i=0; i<maxHits && i<definition.length; i++) {  // go through the hits
			if (StringUtil.notEmpty(definition[i])) {
				boolean foundMatch = false;
				for (int j=0; j<matchInDefinitions.length && !foundMatch; j++){ 
					if (StringUtil.notEmpty(definition[i])&&StringUtil.notEmpty(matchInDefinitions[j])) {
						int index = StringUtil.indexOfIgnoreCase(definition[i], matchInDefinitions[j]);
						if (index>=0){
							if (onlyMatchOnce && alreadyMatched[j])
								return 0;
							alreadyMatched[j]=true;
							count++;
							foundMatch=true;
						}
					}
				}
				if (!foundMatch) {  // here is something that is not in the match list
					return 0;
				}
			}
		}
		if (count>=minNumToMatch && count <= maxNumToMatch)
			return count;
		return 0;
	}

	/*.................................................................................................................*/
	public  void setIDFromDefinition(){
		String s="";
		for (int i=0; i<maxHits && i<ID.length; i++) {
			if (StringUtil.notEmpty(definition[i])){
				ID[i] = definition[i];
			}
		}

	}
	/*.................................................................................................................*/
	public  void setIDFromElement(String separator, int index){
		String s="";
		for (int i=0; i<maxHits && i<ID.length; i++) {
			if (StringUtil.notEmpty(ID[i])){
				s=StringUtil.getItem(ID[i],"|", index);
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

	/*
	 *       
	 	<Hsp_query-from>67</Hsp_query-from>  first base in query sequence matching the hit
      <Hsp_query-to>655</Hsp_query-to>  last base in query sequence matching the hit
      <Hsp_hit-from>1</Hsp_hit-from>  first base in hit that matches up with Hsp_query-from
      <Hsp_hit-to>589</Hsp_hit-to>  last base in hit that matches up with Hsp_query-to.  NOTE: larger, therefore don't reverse complement


    <Hsp_query-from>8</Hsp_query-from>
    <Hsp_query-to>160</Hsp_query-to>
    <Hsp_hit-from>158</Hsp_hit-from>
    <Hsp_hit-to>6</Hsp_hit-to>   NOTE:  less than Hsp_hit-from therefore need to reverse complement!
*/
	/*.................................................................................................................*/
	public  boolean processResultsFromBLAST(String response, boolean storeSequences, double eValueCutoff){
		if (accession==null)
			return false;
		zeroArrays();
		Element blastOutputElement = XMLUtil.getRootXMLElementFromString("BlastOutput",response);
		if (blastOutputElement==null)
			return false;
		numHits = 0;
		String db = blastOutputElement.elementText("BlastOutput_db");
		if (StringUtil.notEmpty(db)) {
			db=StringUtil.replace(db, "\"", "");
			db = StringUtil.getLastItem(db, MesquiteFile.fileSeparator);
			setDatabaseName(db);
		}
		

		Element blastIterationsElement = blastOutputElement.element("BlastOutput_iterations");
		if (blastIterationsElement!=null) {
			Element IterationsElement = blastIterationsElement.element("Iteration");
			if (IterationsElement!=null) {
				Element IterationHitElement = IterationsElement.element("Iteration_hits");
				if (IterationHitElement!=null) {
					List hitList = IterationHitElement.elements("Hit");
					for (Iterator iter = hitList.iterator(); iter.hasNext() && numHits<maxHits;) {   // this is going through all of the hits
						Element hitElement = (Element) iter.next();
						if (hitElement!=null) {

							String s = hitElement.elementText("Hit_def");
							setDefinition(s, numHits);

							s = hitElement.elementText("Hit_accession");
							setAccession(s, numHits);

							s = hitElement.elementText("Hit_id");
							//s=StringUtil.getItem(s,"|", 2);
							if (StringUtil.notEmpty(s))
								setID(s, numHits);

							Element hithsps = hitElement.element("Hit_hsps");
							if (hithsps!=null) {
								Element Hsp = hithsps.element("Hsp");
								if (Hsp!=null) {
									String eValue = Hsp.elementText("Hsp_evalue");
									double eValueDouble = MesquiteDouble.fromString(eValue);
									if (eValueCutoff< 0.0 || eValueDouble<=eValueCutoff) {
										seteValue(eValueDouble, numHits);
										setBitScore(MesquiteDouble.fromString(Hsp.elementText("Hsp_bit-score")), numHits);
										setFrame(MesquiteInteger.fromString(Hsp.elementText("Hsp_hit-frame")), numHits);

										int queryFrame = MesquiteInteger.fromString(Hsp.elementText("Hsp_query-frame"));
										setReversed(queryFrame<0, numHits);
										setQueryStartMatch(MesquiteInteger.fromString(Hsp.elementText("Hsp_query-from")),numHits);
										setQueryEndMatch(MesquiteInteger.fromString(Hsp.elementText("Hsp_query-to")),numHits);
										setHitStartMatch(MesquiteInteger.fromString(Hsp.elementText("Hsp_hit-from")),numHits);
										setHitEndMatch(MesquiteInteger.fromString(Hsp.elementText("Hsp_hit-to")),numHits);
										setHitReversed(hitEndMatch[numHits]<hitStartMatch[numHits],numHits);
											
										

										if (storeSequences)
											setSequence(Hsp.elementText("Hsp_hseq"), numHits);
									} else if (eValueCutoff>=0.0 && eValueDouble>eValueCutoff) {
										setDefinition("", numHits);
										setAccession("", numHits);
										setID("", numHits);
										setFrame(0, numHits);
										numHits--;
									}
								}
							}
							numHits++;
						}

					}
					return true;

				}
			}
		}
		return false;
	}

	//	Alternatively, You can look at the query start/end and subject start/end coordinates. If your query aligns to reverse of subject, then the subject end 
	//coordinate will be smaller than the start coordinate. For example, if SeqA aligns to reverse of SeqB, you might see position 50-100 of SeqA 
	// aligning to position 200-150 of SeqB.

}
