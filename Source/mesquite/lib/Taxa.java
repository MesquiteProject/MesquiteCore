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
import java.math.*;
import java.util.Vector;

import mesquite.categ.lib.CategoricalData;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.CharacterState;
import mesquite.lib.duties.StringMatcher;

/* ======================================================================== */
/**
 * An object of this class represents a single set of taxa (a TAXA block in a
 * NEXUS file).
 */
public class Taxa extends FileElement {
	private int numTaxa;
	private Taxon[] taxon;
	private MesquiteTree defaultTree;
	private Clades clades;
	String equalTaxaMessage = null;
	boolean writeToEqualTaxaMessage = false;

	public static boolean inventUniqueIDs = false;
	private String uniqueID; // id's of the taxa block


	public static final int MAXNUMTAXA = 100000;
	public static int totalCreated = 0;
	int id = 0;

	private boolean inFlux = false;
	private boolean duplicate = false;
	

	public Taxa(int numTaxa) {
		super(numTaxa); // For associable
		clades = new Clades();
		this.numTaxa = numTaxa;
		totalCreated++;
		name = "Taxa";
		taxon = new Taxon[numTaxa];
		for (int it = 0; it < numTaxa; it++) {
			taxon[it] = new Taxon(this);
			taxon[it].setIndex(it);
			taxon[it].setName("taxon " + Integer.toString(it + 1));
			taxon[it].setNameIsDefault(true);
		}
		recordDefaultOrder();
		if (Taxa.inventUniqueIDs)
			setUniqueID(MesquiteTrunk.getUniqueIDBase() + totalCreated);
	}
	/*.................................................................................................................*/
	Vector llListeners = new Vector();
	public void addLowLevelListener(LowLevelListener listener){
		llListeners.addElement(listener);
	}
	public void removeLowLevelListener(LowLevelListener listener){
		llListeners.removeElement(listener);
	}
	void notifyOfChangeLowLevel(int code, int i1, int i2, int i3){
			for (int i = 0; i< llListeners.size(); i++){
				LowLevelListener lll = (LowLevelListener)llListeners.elementAt(i);
				try {
					lll.llChange(this, code, i1, i2, i3);
				}
				catch (Throwable e){ //don't want problem in one of these to stop notifications
				}
			}
	}
	/* ................................................................................................................. */
	public String searchData(String s, MesquiteString commandResult) {
		if (commandResult != null)
			commandResult.setValue((String) null);
		if (StringUtil.blank(s))
			return null;
		String list = "";
		String fc = ""; // to receive the direct command
		int numFound = 0;
		for (int it = 0; it < getNumTaxa(); it++) {
			String name = getTaxonName(it);
			if (name != null && StringUtil.foundIgnoreCase(name, s)) {
				list += "<li>Taxon " + (it + 1) + ": <strong>"
				+ StringUtil.protectForXML(name)
				+ "</strong>. <a href=\"touchTaxon:" + it + " "
				+ getID() + "\">Touch taxon</a></li>";
				numFound++;
				fc = "touchTaxon:" + it + " " + getID();
			}
		}
		if (commandResult != null && numFound == 1)
			commandResult.setValue(fc);
		if (StringUtil.blank(list))
			return list;
		return "<h2>Taxa \"" + StringUtil.protectForXML(getName())
		+ "\"</h2><ul>" + list + "</ul>";
	}

	/* ................................................................................................................. */
	/** gets the explanation (footnote) of this block of taxa */
	public String getExplanation() {
		String extra = "This block of taxa has " + getNumTaxa() + " taxa.\n";
		return extra;
	}

	/* ................................................................................................................. */
	public void dispose() {
		for (int it = 0; it < numTaxa; it++) {
			taxon[it] = null;
		}
		numTaxa = 0;
		taxon = null;
		clades = null;
		super.dispose();
	}

	/* ................................................................................................................. */
	public String getTypeName() {
		return "Taxa";
	}

	/* ................................................................................................................. */
	/**
	 * Call this before you might call the equals method if you wish the equals
	 * method to store a list of reasons for differences between taxa.
	 */
	public void prepareEqualTaxaMessage() {
		equalTaxaMessage = "";
		writeToEqualTaxaMessage = true;
	}

	/* ................................................................................................................. */
	/**
	 * Call this before you might call the equals method if you wish the equals
	 * method to store a list of reasons for differences between taxa.
	 */
	public void turnOffEqualTaxaMessage() {
		equalTaxaMessage = "";
		writeToEqualTaxaMessage = false;
	}

	/* ................................................................................................................. */
	public String getEqualTaxaMessage() {
		return equalTaxaMessage;
	}

	/* ................................................................................................................. */
	public void appendToEqualTaxaMessage(String s) {
		if (writeToEqualTaxaMessage)
			if (StringUtil.blank(equalTaxaMessage))
				equalTaxaMessage += s;
			else
				equalTaxaMessage += "; " + s;
	}

	/* ................................................................................................................. */
	/**
	 * Returns true if passed Taxa is the exact same, or if all of its taxon
	 * names are the same
	 */
	public boolean equals(Taxa taxa, boolean ignoreName) {
		return equals(taxa, ignoreName, true);
	}

	/* ................................................................................................................. */
	/**
	 * Returns true if passed Taxa is the exact same, or if all of its taxon
	 * names are the same
	 */
	public boolean equals(Taxa taxa, boolean ignoreName, boolean ignoreOrder) {
		if (isDoomed()) {
			return false;
		}
		// check name of Taxa???
		if (taxa == this) {
			// appendToEqualTaxaMessage("Taxon block compared with itself");
			// writeToEqualTaxaMessage = false;
			return true;
		} else if (taxa == null) {
			appendToEqualTaxaMessage("Taxon block null");
			return false;
		} else if (taxa.getNumTaxa() != numTaxa) {
			appendToEqualTaxaMessage("Different numbers of taxa");
			return false;
		}
		if (!ignoreName && name != null && taxa.name != null
				&& !name.equalsIgnoreCase(taxa.name)) {
			appendToEqualTaxaMessage("Taxa blocks have different names");
			return false;
		}
		for (int it = 0; it < numTaxa; it++)
			if (taxa.getTaxon(taxon[it].getName()) == null) {
				appendToEqualTaxaMessage("Taxon " + taxon[it].getName()
						+ " not found in second matrix");
				return false;
			}
		for (int it = 0; it < numTaxa; it++)
			if (getTaxon(taxa.getTaxonName(it)) == null) {
				appendToEqualTaxaMessage("Taxon " + taxa.getTaxonName(it)
						+ " not found in original matrix");
				return false;
			}
		if (!ignoreOrder) {
			for (int it = 0; it < numTaxa; it++)
				if (taxa.whichTaxonNumber(taxon[it].getName()) != it) {
					appendToEqualTaxaMessage("Taxon " + taxon[it].getName()
							+ " not found in same place in second matrix");
					return false;
				}
			for (int it = 0; it < numTaxa; it++)
				if (whichTaxonNumber(taxa.getTaxonName(it)) != it) {
					appendToEqualTaxaMessage("Taxon " + taxa.getTaxonName(it)
							+ " not found in same order in original matrix");
					return false;
				}
		}
		return true;
	}

	/* ................................................................................................................. */
	/**
	 * Returns true if passed Taxa is the exact same, or if all of its taxon
	 * names are the same, or if the passed Taxa is a subset of this one
	 */
	public boolean contains(Taxa taxa, boolean ignoreName, boolean ignoreOrder) {
		if (isDoomed()) {
			return false;
		}
		// check name of Taxa???
		if (taxa == this) {
			// appendToEqualTaxaMessage("Taxon block compared with itself");
			// writeToEqualTaxaMessage = false;
			return true;
		} else if (taxa == null) {
			appendToEqualTaxaMessage("Taxon block null");
			return false;
		} else if (taxa.getNumTaxa() > numTaxa) {
			appendToEqualTaxaMessage("Taxon block has more taxa");
			return false;
		}
		if (!ignoreName && name != null && taxa.name != null
				&& !name.equalsIgnoreCase(taxa.name)) {
			appendToEqualTaxaMessage("Taxa blocks have different names");
			return false;
		}
		int inputNumTaxa = taxa.getNumTaxa();
		for (int it = 0; it < inputNumTaxa; it++)
			if (getTaxon(taxa.getTaxonName(it)) == null) {
				appendToEqualTaxaMessage("Taxon " + taxa.getTaxonName(it)
						+ " not found in original matrix");
				return false;
			}
		if (!ignoreOrder) {
			for (int it = 0; it < inputNumTaxa; it++)
				if (whichTaxonNumber(taxa.getTaxonName(it)) != it) {
					appendToEqualTaxaMessage("Taxon " + taxa.getTaxonName(it)
							+ " not found in same order in original matrix");
					return false;
				}
		}
		return true;
	}

	public int findEquivalentTaxon(Taxa oTaxa, int oit) {
		if (oTaxa == null)
			return -1;
		if (oTaxa == this)
			return oit;
		int idt = findByUniqueID(oTaxa.getUniqueID());
		if (idt >= 0)
			return idt;
		idt = whichTaxonNumber(oTaxa.getTaxonName(oit));
		if (idt >= 0)
			return idt;
		return -1;
	}

	private NameReference notesNameRef = NameReference
	.getNameReference("notes");

	/*-----------------------------------------------------------*/
	public void equalizeTaxon(Taxa oTaxa, int oit, int it) {
		if (oTaxa == null || oit < 0 || it < 0)
			return;
		Taxon taxon = getTaxon(it);
		Taxon oTaxon = oTaxa.getTaxon(oit);
		taxon.setName(oTaxon.getName());
		setAnnotation(it, oTaxa.getAnnotation(oit));
		taxon.setUniqueID(oTaxon.getUniqueID());
		AttachedNotesVector notes = (AttachedNotesVector) oTaxa
		.getAssociatedObject(notesNameRef, oit);
		if (notes != null)
			setAssociatedObject(notesNameRef, it, notes.cloneVector(this));
	}

	/* ................................................................................................................. */
	/**
	 * Note this does not yield a full clone of the taxa, as Associable and
	 * Illustration info is not cloned
	 */
	public Taxa cloneTaxa(boolean[] taxaToClone) {
		int numTax = numTaxa;
		if (taxaToClone != null) {
			int count = 0;
			for (int i = 0; i < taxaToClone.length; i++)
				if (taxaToClone[i])
					count++;
			numTax = count;
		}
		if (isDoomed())
			return null;
		Taxa temp = new Taxa(numTax);
		temp.name = name;
		if (taxaToClone != null) {
			int count = 0;
			for (int i = 0; i < taxaToClone.length; i++)
				if (taxaToClone[i]) {
					if (count<numTax)
						temp.taxon[count].setName(taxon[i].getName());
					count++;
				}
		} else
			for (int it = 0; it < numTaxa; it++) {
				temp.taxon[it].setName(taxon[it].getName());
			}
		return temp;
	}

	/* ................................................................................................................. */
	/**
	 * Note this does not yield a full clone of the taxa, as Associable and
	 * Illustration info is not cloned
	 */
	public Taxa cloneTaxa() {
		if (isDoomed())
			return null;
		Taxa temp = new Taxa(numTaxa);
		temp.name = name;
		for (int it = 0; it < numTaxa; it++) {
			temp.taxon[it].setName(taxon[it].getName());
		}
		return temp;
	}

	/***************************************************************************
	 * .................................................................................................................
	 * public Snapshot getSnapshot(MesquiteFile file) { return
	 * super.getSnapshot(file); //here to remind to call super's version if
	 * anything added here }
	 * /*.................................................................................................................
	 */
	public Object doCommand(String commandName, String arguments,
			CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the name of the taxon",
				"[number of taxon, 0 based]", commandName, "setTaxonName")) {
			MesquiteInteger pos = new MesquiteInteger(0);
			int c = MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(c) && c >= 0 && c < numTaxa) {
				String n = ParseUtil.getToken(arguments, pos);
				setTaxonName(c, n);
			}
		} else {
			return super.doCommand(commandName, arguments, checker);
		}
		return null;
	}

	/* ................................................................................................................. */
	/** returns which taxon (i.e., its number) has the given name */
	public int whichTaxonNumber(String taxonName) {
		return whichTaxonNumber(taxonName, false);
	}

	/* ................................................................................................................. */
	/** returns which taxon (i.e., its number) has the given name */
	public int whichTaxonNumber(String taxonName, boolean caseSensitive) {
		return whichTaxonNumber(taxonName, caseSensitive, false);
	}

	/* ................................................................................................................. */
	/** returns which taxon (i.e., its number) has the given name */
	public int whichTaxonNumberUsingMatcher(StringMatcher nameMatcher, String taxonName) {
		if (StringUtil.blank(taxonName))
			return -1;
		for (int i = 0; i < numTaxa; i++){  //check UniqueID's
			String uniqueID = taxon[i].getUniqueID();
			if (uniqueID != null && taxonName.equals(uniqueID))
				return i;
		}
		int tN0 = MesquiteInteger.fromString(taxonName, false);
		if (MesquiteInteger.isCombinable(tN0) && tN0 >= 1 && tN0 <= numTaxa) {
			return Taxon.toInternal(tN0);
		}

		int match = -1;
		int numMatches = 0;
		for (int i = 0; i < numTaxa; i++) {
			String ti = taxon[i].getName();
			if (ti != null  && nameMatcher.stringsMatch(ti, taxonName)) {
				match = i;
				numMatches++;
			}
		}
		if (numMatches < 2 && match >= 0)
			return match;

		// System.out.println("ERROR: bad taxon name: "+ taxonName);
		return -1;
	}
	/* ................................................................................................................. */
	/** returns which taxon (i.e., its number) has the given name */
	public int whichTaxonNumber(StringMatcher nameMatcher, String taxonName, boolean caseSensitive, boolean forgivingOfTruncation) {
		if (nameMatcher==null || nameMatcher.useDefaultMatching())
			return whichTaxonNumber(taxonName, caseSensitive, forgivingOfTruncation);
		return whichTaxonNumberUsingMatcher(nameMatcher, taxonName);
	}
	/* ................................................................................................................. */
	/** returns which taxon (i.e., its number) has the given name */
	public int whichTaxonNumber(String taxonName, boolean caseSensitive, boolean forgivingOfTruncation) {
		if (StringUtil.blank(taxonName))
			return -1;
		// second, see if there is an exact match
		if (caseSensitive) {
			for (int i = 0; i < numTaxa; i++)
				if (taxonName.equals(taxon[i].getName()))
					return i;
		} else {
			for (int i = 0; i < numTaxa; i++) { // first try exact match
				if (taxonName.equals(taxon[i].getName()))
					return i;
			}
			for (int i = 0; i < numTaxa; i++) {
				if (taxonName.equalsIgnoreCase(taxon[i].getName()))
					return i;
			}
		}
		for (int i = 0; i < numTaxa; i++){  //check UniqueID's
			String uniqueID = taxon[i].getUniqueID();
			if (uniqueID != null && taxonName.equals(uniqueID))
				return i;
		}
		// first see if integer. If integer, interpret first as
		// taxon number in case forgivingOfTrunctation permits taxon name
		// beginning with integer
		int tN0 = MesquiteInteger.fromString(taxonName, false);
		if (MesquiteInteger.isCombinable(tN0) && tN0 >= 1 && tN0 <= numTaxa) {
			return Taxon.toInternal(tN0);
		}
		if (forgivingOfTruncation) {
			// third, see if there is an unambiguous match of truncation
			// NOTE: currently we permit only truncation in the incoming name to
			// be considered a match against the Taxa block
			if (caseSensitive) {
				int matchTrunc = -1;
				int numMatches = 0;
				for (int i = 0; i < numTaxa; i++) {
					String ti = taxon[i].getName();
					if (ti != null && ti.startsWith(taxonName)) {
						matchTrunc = i;
						numMatches++;
					}
				}
				if (numMatches < 2 && matchTrunc >= 0)
					return matchTrunc;

				/*
				 * this code would permit match if name is extended beyond that
				 * in Taxa block if (numMatches <2){ int matchExtend = -1;
				 * numMatches = 0; for (int i=0; i<numTaxa; i++){ String ti =
				 * taxon[i].getName(); if (taxonName.startsWith(ti)) {
				 * matchExtend = i; numMatches++; } } if (numMatches <2){ if
				 * (matchTrunc >=0){ if (matchExtend <0 || matchTrunc ==
				 * matchExtend) return matchTrunc; } else if (matchExtend >=0)
				 * return matchExtend; } }
				 */
			} else {
				int matchTrunc = -1;
				int matchExtend = -1;
				int numMatches = 0;
				for (int i = 0; i < numTaxa; i++) {
					String ti = taxon[i].getName();
					if (ti != null
							&& StringUtil.startsWithIgnoreCase(ti, taxonName)) {
						matchTrunc = i;
						numMatches++;
					}
				}
				if (numMatches < 2 && matchTrunc >= 0)
					return matchTrunc;
				/*
				 * this code would permit match if name is extended beyond that
				 * in Taxa block if (numMatches <2){ numMatches = 0; for (int
				 * i=0; i<numTaxa; i++){ String ti = taxon[i].getName(); if
				 * (StringUtil.startsWithIgnoreCase(taxonName, ti)) {
				 * matchExtend = i; numMatches++; } } if (numMatches <2){ if
				 * (matchTrunc >=0){ if (matchExtend <0 || matchTrunc ==
				 * matchExtend) return matchTrunc; } else if (matchExtend >=0)
				 * return matchExtend; } }
				 */
			}
		}

		// System.out.println("ERROR: bad taxon name: "+ taxonName);
		return -1;
	}

	/* ................................................................................................................. */
	/**
	 * returns which taxon (i.e., its number) has the given name, doing reverse
	 * search from last to first
	 */
	public int whichTaxonNumberRev(String taxonName, boolean caseSensitive) {
		if (StringUtil.blank(taxonName))
			return -1;
		if (caseSensitive) {
			for (int i = numTaxa - 1; i >= 0; i--)
				if (taxonName.equals(taxon[i].getName()))
					return i;
		} else {
			for (int i = numTaxa - 1; i >= 0; i--)
				// first try exact match
				if (taxonName.equals(taxon[i].getName()))
					return i;
			for (int i = numTaxa - 1; i >= 0; i--)
				if (taxonName.equalsIgnoreCase(taxon[i].getName()))
					return i;
		}
		try {
			int tNum = Taxon.toInternal(MesquiteInteger.fromString(taxonName,
					false));
			if ((tNum < numTaxa) && (tNum >= 0))
				return tNum;
		} catch (NumberFormatException e) {
			System.out.println("ERROR: bad taxon number/taxon name: "
					+ taxonName);
			return -1;
		}
		// System.out.println("ERROR: bad taxon name: "+ taxonName);
		return -1;
	}
	/* ................................................................................................................. */
	/**
	 * returns which taxon (i.e., its number) has the given name, doing reverse
	 * search from last to first
	 */
	public int whichTaxonNumberUsingMatcherRev(StringMatcher nameMatcher, String taxonName) {
		if (StringUtil.blank(taxonName))
			return -1;

		for (int i = numTaxa - 1; i >= 0; i--)
			if (nameMatcher.stringsMatch(taxonName, taxon[i].getName()))
				return i;
		try {
			int tNum = Taxon.toInternal(MesquiteInteger.fromString(taxonName, false));
			if ((tNum < numTaxa) && (tNum >= 0))
				return tNum;
		} catch (NumberFormatException e) {
			System.out.println("ERROR: bad taxon number/taxon name: "
					+ taxonName);
			return -1;
		}
		// System.out.println("ERROR: bad taxon name: "+ taxonName);
		return -1;
	}
	
	/* ................................................................................................................. */
	/**
	 * returns which taxon (i.e., its number) has the given name, doing reverse
	 * search from last to first
	 */
	public int whichTaxonNumberRev(StringMatcher nameMatcher, String taxonName, boolean caseSensitive) {
		if (nameMatcher==null)
			return whichTaxonNumberRev(taxonName, caseSensitive);
		return whichTaxonNumberUsingMatcherRev(nameMatcher, taxonName);
	}


	/* ................................................................................................................. */
	/** returns which number has the given taxon. */
	public int whichTaxonNumber(Taxon t) {
		if (t == null || t.getTaxa() != this)
			return -1;
		int ind =  t.getIndex();
		if (ind < 0 || !MesquiteInteger.isCombinable(ind))
			return whichTaxonNumberCheck(t);
		else
			return ind;
	}
	/* ................................................................................................................. */
	/** returns which number has the given taxon. */
	public int whichTaxonNumberCheck(Taxon t) {
		for (int i = 0; i < numTaxa; i++)
			if (t == taxon[i])
				return i;
		return -1;
	}

	/* ................................................................................................................. */
	public String getUniqueName(String base) {
		int count = 1;
		while (true) {

			String candidate;
			if (count == 1)
				candidate = base;
			else
				candidate = base + "." + count;  
			if (whichTaxonNumber(candidate) < 0)
				return candidate;
			count++;
		}

	}

	/* ................................................................................................................. */
	public void setName(String name) {
		setDirty(true);
		this.name = name;
		notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED));
		if (getHShow()) {
			if (getProject() != null)
				getProject().refreshProjectWindow();
		}
	}

	/* ................................................................................................................. */
	public String getName() {
		if (name == null)
			return "Taxa";
		return name;
	}
	/** returns true if this has name equivalent to default name*/
	public boolean hasDefaultName() {
		return  (name==null) || name.equals("Taxa");
	}

	/* ................................................................................................................. */
	/** Gets the default tree for these taxa (default default is bush) */
	public MesquiteTree getDefaultTree() {
		if (isDoomed())
			return null;
		if (defaultTree == null) {
			MesquiteTree tree = new MesquiteTree(this);
			tree.setToDefaultBush(getNumTaxa(), false);
			return tree;
		}
		return defaultTree;
	}

	/* ................................................................................................................. */
	/** Gets the default tree for these taxa (default default is bush) */
	public MesquiteTree getDefaultDichotomousTree(MesquiteTree tree) {
		if (isDoomed())
			return null;
		if (tree == null || tree.getTaxa() != this) {
			tree = new MesquiteTree(this);
		}
		tree.setToDefaultSymmetricalTree(getNumTaxa(), false);
		return tree;
	}

	/* ................................................................................................................. */
	/** sets the default tree for these taxa */
	public void setDefaultTree(MesquiteTree tree) {
		defaultTree = tree;
		notifyListeners(this, new Notification(MesquiteListener.UNKNOWN));
	}

	/* ................................................................................................................. */
	public int getNumTaxa() {
		if (isDoomed())
			return 0;
		return numTaxa;
	}

	String findName(int num, Taxon[] t) {
		while (true) {
			String s = "taxon " + Integer.toString(num);
			if (getTaxon(s) == null && !nameInTaxonArray(s, t))
				return s;
			num++;
		}
	}

	boolean nameInTaxonArray(String name, Taxon[] t) {
		if (t == null || name == null)
			return false;
		for (int i = 0; i < t.length; i++)
			if (t[i] != null && name.equals(t[i].getName()))
				return true;
		return false;
	}
	public Taxon addTaxon(boolean notify){
		if (addTaxa(getNumTaxa(), 1, notify))
			return getTaxon(getNumTaxa()-1);
		return null;
	}
	public void deleteTaxon(Taxon taxon, boolean notify){
		int it = whichTaxonNumber(taxon);
		if (it>=0)
			deleteTaxa(it, 1, notify);
	}
	/*.................................................................................................................*/
	public void deleteTaxaWithDuplicateNames (){
		IntegerArray originalIndices = (IntegerArray) getAttachment("originalIndicesDupRead", IntegerArray.class);
		if (originalIndices != null)
			for (int i=0; i<getNumTaxa(); i++)
				originalIndices.setValue(i, i);
		boolean[] toDelete = new boolean[getNumTaxa()];
		for (int i=0; i<getNumTaxa(); i++){
			if (!toDelete[i]){
				String name = getTaxonName(i);
				boolean dup = false;
				toDelete[i] = true;
				for (int j=i+1; j<getNumTaxa(); j++){
					String name2 = getTaxonName(j);
					if (name!=null && name.equalsIgnoreCase(name2)) {
						toDelete[j] = true;
						if (originalIndices != null){
							originalIndices.setValue(j, i);  //indicate to what this deleted is equivalent
							// mark all subsequent nondeleted to one less
							for (int k = j+1;  k<getNumTaxa(); k++){
								if (!toDelete[k])
									originalIndices.setValue(k, originalIndices.getValue(k)-1);
							}
						}
					}
				}
				toDelete[i] = false;
			}
		}
		for (int i=getNumTaxa()-1; i>=0; i--){
			if (toDelete[i]){
				deleteTaxon(getTaxon(i), false);
			}
		}
		notifyListeners(this, new Notification(MesquiteListener.PARTS_DELETED));
	}
	/*.................................................................................................................*/
	public String hasDuplicateNames (){
		return hasDuplicateNames(false);
	}
	/*.................................................................................................................*/
	public String hasDuplicateNames (boolean report){
		StringBuffer list = new StringBuffer();
		int numTaxa = getNumTaxa();
		if (report && numTaxa>1000)  MesquiteMessage.println("");
		for (int i=0; i<numTaxa; i++){
			String name = getTaxonName(i);
			long iChecksum = taxon[i].getNameChecksum();
			if (report && numTaxa>1000 && i % 500 == 0) 
				MesquiteMessage.print(".");
			for (int j=i+1; j<numTaxa; j++){
				long jChecksum = taxon[j].getNameChecksum();
				if (iChecksum == jChecksum){
					String name2 = getTaxonName(j);
					if (name!=null && name.equalsIgnoreCase(name2)) {
						list.append(" [" + i + "-" + j + "] " + name);
						MesquiteMessage.println(name);
					}
				}
			}
		}
		if (list.length() == 0)
			return null;
		return list.toString();
	}
	/* ................................................................................................................. */
	/**
	 * An equivalent to addParts but with notification added. Final because
	 * overriding should be done of the Parts method instead
	 */
	public final boolean addTaxa(int first, int num, boolean notify) {
		UndoReference undoReference = null;
		if (notify) {
			UndoInstructions undoInstructions = new UndoInstructions(UndoInstructions.PARTS_ADDED, this);
			resetJustAdded();
			undoReference = new UndoReference(undoInstructions, null);
		}

		boolean added = addParts(first, num);
		if (added && notify)
			notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED, new int[] { first, num },undoReference));
		return added;

	}

	/* ................................................................................................................. */
	/** Adds new taxa starting just after taxon "starting" */
	public boolean addParts(int starting, int num) {
		if (isDoomed())
			return false;
		if (num <= 0)
			return false;
		if (!checkThread(false))
			return false;
		inFlux = true;
		if (starting < 0)
			starting = -1;
		else if (starting >= numTaxa)
			starting = numTaxa - 1;
		int newNumTaxa = numTaxa + num;
		Taxon[] newTaxonArray = new Taxon[newNumTaxa];

		for (int it = 0; it <= starting; it++)
			newTaxonArray[it] = taxon[it];
		for (int it = 0; it < num; it++) {
			newTaxonArray[starting + it + 1] = new Taxon(this);
			newTaxonArray[starting + it + 1].setName(findName(
					starting + it + 2, newTaxonArray));
		}
		for (int it = 0; it < numTaxa - starting - 1; it++){
			newTaxonArray[it + starting + num + 1] = taxon[starting + it + 1];
		}
		taxon = newTaxonArray;
		numTaxa = newNumTaxa;
		for (int it = 0; it < numTaxa; it++)
			taxon[it].setIndex(it);
		super.addParts(starting, num);
		inFlux = false;
		uncheckThread();
		notifyOfChangeLowLevel(MesquiteListener.PARTS_ADDED, starting, num, 0);  
		return true;
	}

	/* ................................................................................................................. */
	/**
	 * An equivalent to deleteParts but with notification added. Final because
	 * overriding should be done of the Parts method instead
	 */
	public final boolean deleteTaxa(int first, int num, boolean notify) {
		boolean deleted = deleteParts(first, num);
		if (deleted && notify)
			notifyListeners(this, new Notification(
					MesquiteListener.PARTS_DELETED, new int[] { first, num }));
		return deleted;

	}

	/* ................................................................................................................. */
	/** Deletes num taxa beginning at and including "starting" */
	public boolean deleteParts(int starting, int num) {
		
		if (num <= 0 || starting < 0 || starting >= numTaxa)
			return false;
		
		if (!checkThread(false))
			return false;
		inFlux = true;
		if (num + starting > numTaxa)
			num = numTaxa - starting;

		int newNumTaxa = numTaxa - num;
		Taxon[] newTaxonArray = new Taxon[newNumTaxa];

		for (int it = 0; it < starting; it++)
			newTaxonArray[it] = taxon[it];
		for (int it = starting + num; it < numTaxa; it++)
			newTaxonArray[it - num] = taxon[it];

		taxon = newTaxonArray;
		numTaxa = newNumTaxa;
		for (int it = 0; it < numTaxa; it++)
			taxon[it].setIndex(it);
		super.deleteParts(starting, num);
		inFlux = false;
		uncheckThread();
		notifyOfChangeLowLevel(MesquiteListener.PARTS_DELETED, starting, num, 0);  
		return true;
	}

	/* ................................................................................................................. */
	/** Swaps taxa first and second */
	public boolean swapTaxa(int first, int second, boolean notify) {
		if (isDoomed())
			return false;
		if (first >= taxon.length || second >= taxon.length || first < 0
				|| second < 0 || first == second)
			return false;
		swapParts(first, second);
		notifyOfChangeLowLevel(MesquiteListener.PARTS_SWAPPED, first, second, 0);  
		inFlux = true;
		/*
		 * Taxon temp = taxon[first]; taxon[first] = taxon[second];
		 * taxon[second] = temp; if (first<second) { moveParts(first, 1,
		 * second); moveParts(second-1, 1, first-1); } else { moveParts(second,
		 * 1, first); moveParts(first-1, 1, second-1); }
		 */
		if (notify)
			notifyListeners(this,
					new Notification(MesquiteListener.PARTS_MOVED));
		inFlux = false;
		return false;
	}

	/* ................................................................................................................. */
	/**
	 * An equivalent to moveParts but with notification added. Final because
	 * overriding should be done of the Parts method instead
	 */
	public final boolean moveTaxa(int first, int num, int justAfter,
			boolean notify) {
		boolean moved = moveParts(first, num, justAfter);
		if (moved && notify)
			notifyListeners(this, new Notification(MesquiteListener.PARTS_MOVED, new int[] { first, num, justAfter }));
		return moved;

	}

	/* ........................................................... */
	public boolean swapParts(int first, int second) {
		if (first < 0 || first >= taxon.length || second < 0
				|| second >= taxon.length)
			return false;
		if (!checkThread(false))
			return false;
		inFlux = true;
		Taxon temp = taxon[first];
		taxon[first] = taxon[second];
		taxon[second] = temp;
		boolean success = super.swapParts(first, second);
		taxon[first].setIndex(first);
		taxon[second].setIndex(second);
		inFlux = false;
		uncheckThread();
		return success;
	}

	/* ................................................................................................................. */
	/** Moves num taxa starting at first to be just after justAfter */
	public boolean moveParts(int first, int num, int justAfter) {
		if (isDoomed())
			return false;
		if (!checkThread(false))
			return false;
		inFlux = true;

		if (justAfter >= taxon.length)
			justAfter = taxon.length - 1;
		if (justAfter < 0)
			justAfter = -1;
		Taxon[] newTaxa = new Taxon[taxon.length];
		if (first > justAfter) {
			int count = 0;
			for (int i = 0; i <= justAfter; i++)
				newTaxa[count++] = taxon[i];
			for (int i = first; i <= first + num - 1; i++)
				newTaxa[count++] = taxon[i];
			for (int i = justAfter + 1; i <= first - 1; i++)
				newTaxa[count++] = taxon[i];
			for (int i = first + num; i < taxon.length; i++)
				newTaxa[count++] = taxon[i];
		} else { // (first<=justAfter)
			int count = 0;
			for (int i = 0; i <= first - 1; i++)
				newTaxa[count++] = taxon[i];
			for (int i = first + num; i <= justAfter; i++)
				newTaxa[count++] = taxon[i];
			for (int i = first; i <= first + num - 1; i++)
				newTaxa[count++] = taxon[i];
			for (int i = justAfter + 1; i < taxon.length; i++)
				newTaxa[count++] = taxon[i];
		}
		taxon = newTaxa;
		super.moveParts(first, num, justAfter);
		for (int it = 0; it < numTaxa; it++)
			taxon[it].setIndex(it);
		inFlux = false;
		uncheckThread();
		notifyOfChangeLowLevel(MesquiteListener.PARTS_MOVED, first, num, justAfter);  
		return true;
	}

	public boolean isInFlux() {
		return inFlux;
	}

	/* ................................................................................................................. */
	public Taxon getTaxon(String name) {
		return getTaxon(name, false);
	}

	/* ................................................................................................................. */
	public Taxon getTaxon(String name, boolean caseSensitive) {
		return getTaxon(name, caseSensitive, false);
	}

	/* ................................................................................................................. */
	public Taxon getTaxon(String name, boolean caseSensitive,
			boolean forgivingOfTruncation) {
		if (isDoomed())
			return null;
		if (name != null) {
			int it = whichTaxonNumber(name, caseSensitive,
					forgivingOfTruncation);
			if (it >= 0)
				return taxon[it];
		}
		return null;
	}

	/* ................................................................................................................. */
	public Taxon getTaxonAllowingSynonyms(String name, boolean caseSensitive) {
		if (isDoomed())
			return null;
		if (name != null) {
			int it = whichTaxonNumber(name, caseSensitive);
			if (it >= 0)
				return taxon[it];
			it = whichTaxonNumberAllowingSynonyms(name, caseSensitive);
			if (it >= 0)
				return taxon[it];
		}
		return null;
	}

	/* ................................................................................................................. */
	/** returns which taxon (i.e., its number) has the given name */
	public int whichTaxonNumberAllowingSynonyms(String taxonName,
			boolean caseSensitive) {
		if (StringUtil.blank(taxonName))
			return -1;
		if (caseSensitive) {
			for (int i = 0; i < numTaxa; i++)
				if (taxon[i].getSynonym() != null
						&& taxonName.equals(taxon[i].getSynonym()))
					return i;
		} else {
			for (int i = 0; i < numTaxa; i++)
				// first try exact match
				if (taxon[i].getSynonym() != null
						&& taxonName.equals(taxon[i].getSynonym()))
					return i;
			for (int i = 0; i < numTaxa; i++)
				if (taxon[i].getSynonym() != null
						&& taxonName.equalsIgnoreCase(taxon[i].getSynonym()))
					return i;
		}
		return -1;
	}

	/* ................................................................................................................. */
	public Taxon getTaxonByID(long id) {
		if (isDoomed())
			return null;
		for (int i = 0; i < numTaxa; i++)
			if (taxon[i].getID() == id)
				return taxon[i];
		return null;
	}

	public long[] getTaxaIDs() {
		long[] taxaIDs = new long[numTaxa];
		for (int i = 0; i < numTaxa; i++) {
			Taxon t = getTaxon(i);
			if (t != null)
				taxaIDs[i] = t.getID();
		}
		return taxaIDs;
	}

	// setting uniqueID for block
	public void setUniqueID(String id) {
		uniqueID = id;
	}

	// getting uniqueID for block
	public String getUniqueID() {
		return uniqueID;
	}

	public void deleteUniqueIDs() {
		for (int i = 0; i < numTaxa; i++) {
			taxon[i].setUniqueID(null);
		}
	}

	public int findByUniqueID(String target) {
		if (target == null)
			return -1;
		String base = MesquiteTrunk.getUniqueIDBase();
		for (int i = 0; i < numTaxa; i++) {
			String id = getUniqueID(i);
			if (id != null && id.equals(target))
				return i;
		}
		return -1;
	}

	public void stampUniqueIDs(boolean replaceExisting) {
		String base = MesquiteTrunk.getUniqueIDBase();
		for (int i = 0; i < numTaxa; i++) {
			if (replaceExisting || StringUtil.blank(taxon[i].getUniqueID()))
				taxon[i].setUniqueID(base + Taxon.totalCreated++);
		}
	}

	public void stampUniqueID(int i, boolean replaceExisting) {
		String base = MesquiteTrunk.getUniqueIDBase();
		if (replaceExisting || StringUtil.blank(taxon[i].getUniqueID()))
			taxon[i].setUniqueID(base + Taxon.totalCreated++);
	}

	public void setUniqueID(int it, String id) {
		Taxon t = getTaxon(it);
		if (t != null)
			t.setUniqueID(id);
	}

	public String getUniqueID(int it) {
		Taxon t = getTaxon(it);
		if (t != null)
			return t.getUniqueID();
		return null;
	}

	/* ................................................................................................................. */
	public Taxon getTaxon(int it) {
		if (isDoomed())
			return null;
		if (it >= 0 && it < numTaxa)
			return taxon[it];
		else
			return null;
	}

	/* ................................................................................................................. */
	public void setTaxon(int it, Taxon t) {
		setDirty(true);
		taxon[it] = t;
		notifyListeners(this, new Notification(MesquiteListener.PARTS_CHANGED));
	}

	/* ................................................................................................................. */
	public static String getStandardizedTaxonName(int it) {
		return "t"+it;
	}

	/* ................................................................................................................. */
	public String getTaxonName(int it) {
		if (it >= 0 && it < numTaxa)
			return taxon[it].getName();
		else
			return "";
	}

	/* ................................................................................................................. */
	public void setTaxonName(int it, String s) {
		setTaxonName(it, s, true);
	}

	/* ................................................................................................................. */
	public void setTaxonNameNoWarnNoNotify(int it, String s) {
		if (it >= 0 && it < numTaxa) {
			setDirty(true);
			taxon[it].setName(s);
		}
	}
	/* ................................................................................................................. */
	public void setTaxonName(int it, String s, boolean notify) {
		if (it >= 0 && it < numTaxa) {
			setDirty(true);
			taxon[it].setName(s);
			warnIfNameIllegal(it, s);
			notifyOfChangeLowLevel(MesquiteListener.NAMES_CHANGED, it, 0, 0);  
			if (notify)
				notifyListeners(this, new Notification(
						MesquiteListener.NAMES_CHANGED, new int[] { it }));
		}
	}

	/* ................................................................................................................. */
	private void warnIfNameIllegal(int it, String s) {
		String st = checkNameLegality(it, s);
		if (st != null)
			MesquiteMessage.warnProgrammer(st);
	}

	java.util.zip.CRC32 crc = new java.util.zip.CRC32();
	

	/* ................................................................................................................. */
	public String checkNameLegality(int it, String s) {
		return checkNameLegality(it, numTaxa, s);
	}
	/* ................................................................................................................. */
	public String checkNameLegality(int it, int checkUntil, String s) {
		if (StringUtil.blank(s)) {
			return "Illegal taxon name (null or blank)";
		}
		try {
			int i = Integer.parseInt(s);
			return "The taxon name \""
			+ s
			+ "\" is illegal because it consists only of numbers.  This may cause various problems and should be fixed.";
		} catch (NumberFormatException e) {
		}
		crc.reset();
		for (int i = 0; i< s.length(); i++)
			crc.update(s.charAt(i));
		long sChecksum = crc.getValue();
		for (int i = 0; i < checkUntil; i++) {
			Taxon otherTaxon = taxon[i];
			if (i != it 
					&&  sChecksum == otherTaxon.getNameChecksum() 
					&&  !otherTaxon.isNameNull() //(i < it || !otherTaxon.isNameDefault()) &&
					&&  s.equals(otherTaxon.getName())
				) { 
				return "The taxon name \""
				+ s
				+ "\" for taxon "
				+ (it + 1)
				+ " is illegal because another taxon (#"
				+ (i + 1)
				+ ") already has it.  This may cause various problems and should be fixed.";
			}
		}
		return null;
	}

	/* ................................................................................................................. */
	public String getName(int it) {
		if (it >= 0 && it < numTaxa) {
			String s = taxon[it].getName();
			if (s == null)
				return "";
			return s;
		} else
			return "";
	}

	/* ................................................................................................................. */
	public int getLongestTaxonNameLength() {
		int maxLength = 0;
		int itLength;
		for (int it = 0; it < numTaxa; it++) {
			itLength = getName(it).length();
			if (itLength > maxLength)
				maxLength = itLength;
		}
		return maxLength;
	}

	/* ................................................................................................................. */
	public Taxon userChooseTaxon(MesquiteWindow parent, String message) {
		Listable[] names = new Listable[numTaxa];
		for (int i = 0; i < numTaxa; i++)
			names[i] = taxon[i];
		Listable chosen = ListDialog.queryList(parent, "Choose taxon", message,
				MesquiteString.helpString, names, -1);
		return (Taxon) chosen;
	}

	/* ................................................................................................................. */
	public String toString() {
		return "Taxa block (name: " + getName() + " number of taxa: " + numTaxa
		+ " id: " + getID() + ")";
	}

	public String toHTMLStringDescription() {
		//	String s = "<li>Taxa block: <strong>" + getName() + "</strong>.  Number of taxa: " + numTaxa + ".  (<a href =\"showTaxa:" + getID() + "\">List &amp; Manage</a>)  (<a href =\"chartTaxa:" + getID() + "\">Chart</a>)(<a href =\"renameTaxa:" + getID() + "\">Rename</a>)  (<a href =\"editCommentTaxa:" + getID() + "\">Edit Comment</a>)  (<a href =\"deleteTaxa:" + getID() + "\">Delete</a>)";
		String s = "<li>Taxa block: <strong>" + getName() + "</strong>.  Number of taxa: " + numTaxa + ". ";
		String comment = getAnnotation();
		if (!StringUtil.blank(comment))
			s+= "<br><font color=\"#777777\">" + comment + "</font>";
		if (HTMLDescribable.verbose)
			s += "<ul>" + super.toHTMLStringDescription() + "</ul>";
		s += "</li>";
		return s;
	}

	public Clades getClades() {
		if (isDoomed())
			return null;
		return clades;
	}

	/**
	 * returns whether this taxa is marked as a duplicate of another. This is
	 * used for merging taxa and subsequently saving the file. The system is
	 * kludgey and may change
	 */
	public boolean isDuplicate() {
		return duplicate;
	}

	
	/**
	 * Returns whether this there is any data matrix containing data for taxa itStart through itEnd inclusive.
	 */
	public boolean taxaHaveAnyData( int itStart, int itEnd){
		int numMatrices = getProject().getNumberCharMatrices(null, this, CharacterData.class, true);
		for (int iM = 0; iM < numMatrices; iM++){
			CharacterData data = getProject().getCharacterMatrixVisible( this, iM, CharacterData.class);
			if (data.hasDataForTaxa(itStart, itEnd))
				return true;
		}
		return false;
	}

	/**
	 * sets whether this taxa is marked as a duplicate of another. This is used
	 * for merging taxa and subsequently saving the file. The system is kludgey
	 * and may change
	 */
	public void setDuplicate(boolean dup) {
		this.duplicate = dup;
	}

	/* ---------------- for HNode interface ---------------------- */
	public boolean getHShow() {
		return true;
	}

}

