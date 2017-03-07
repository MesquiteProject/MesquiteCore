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
import java.util.*;
import java.text.*;

import mesquite.lib.duties.*;
import mesquite.lib.characters.*;



/* ======================================================================== */
/**Represents a block in a NEXUS file.  Often contains a reference to the object to be written (e.g., a CharacterData
or TreeVector.
 */
public abstract class NexusBlock implements Listable, Identifiable, Disposable {
	public static int  numBackups =0; //number of backups of NEXUS files
	MesquiteModule manager;
	MesquiteFile file;
	String name;
	Vector unrec; //to store unrecognized commands
	public static Vector classesCreated, classesFinalized, countsOfClasses, countsOfClassesFinalized; //to detect memory leaks
	public static long totalCreated =0;
	public static long totalDisposed =0;
	public static long totalFinalized =0;

	private String defaultTaxa = null;
	private String defaultCharacters = null;

	long id;
	public static boolean suppressNEXUSTITLESANDLINKS = false;
	public static boolean suppressNEXUSIDS = false;
	static {
		if (MesquiteTrunk.checkMemory) {
			classesCreated = new Vector();
			classesFinalized = new Vector();
			countsOfClassesFinalized = new Vector();
			countsOfClasses = new Vector();
		}
	}

	public NexusBlock(MesquiteFile f, MesquiteModule manager){
		totalCreated++;
		if (MesquiteTrunk.checkMemory)
			countCreated();
		id = totalCreated;
		file = f;
		this.manager = manager;
		/*
		if (f==null)
			MesquiteMessage.warnProgrammer("Warning: file null in NexusBlock " + this.getClass());
		if (manager==null)
			MesquiteMessage.warnProgrammer("Warning: manager null in NexusBlock" + this.getClass());
		 */
	}
	public boolean getWritable(){
		return true;
	}
	public static int findCorrespondingBlock(ListableVector nexusBlocks, FileElement fileElement){
		for (int i = 0; i<nexusBlocks.size(); i++){
			NexusBlock e = (NexusBlock)nexusBlocks.elementAt(i);
			if (e.contains(fileElement))
				return i;

		}
		return -1;
	}
	public static void equalizeOrdering(ListableVector v, ListableVector nexusBlocks){ 
		// start at back of v
		// put each one in place in nexus blocks just after last nexusBlock it must be after
		for (int i = v.size()-1; i>=0; i--){
			Object e = v.elementAt(i);
			int blockNum = findCorrespondingBlock(nexusBlocks, (FileElement)e);
			if (blockNum >=0){
				nexusBlocks.moveParts(blockNum, 1, 0);
				bubbleBlockDown(nexusBlocks, (NexusBlock)nexusBlocks.elementAt(0));
			}
		}
	}
	public static boolean bubbleBlockDown(ListableVector blocks, NexusBlock nb){
		if (nb==null)
			return false;
		
		int index = blocks.indexOf(nb); //find current height
		if (index >=0){
			int height = index;
			//make sure block is in OK place
			for (int i = index+1; i<blocks.size(); i++) { //now look to all higher to see if nb should jump above them
				NexusBlock nR = (NexusBlock)blocks.elementAt(i);
				if (nb.mustBeAfter(nR) && (nb.getFile()==nR.getFile())) { //nb needs to fall below nR
					height = i;  //remember height of nR
				}
			}
			if (height > index){ //nb needs to fall to height; move it
				blocks.removeElement(nb, false);
				blocks.insertElementAt(nb, height, false);
				return true;
			}
		}
		return false;

	}
	public void setDefaultTaxa(Taxa t){
		if (t==null)
			return;
		if (file == null || file.getProject() == null)
			return;
		defaultTaxa = file.getProject().getTaxaReferenceInternal(t); //remembered as id's to prevent memory leaks
	}
	public Taxa getDefaultTaxa(){
		if (file == null || file.getProject() == null)
			return null;
		if (defaultTaxa == null){
			Taxa dc = file.getCurrentTaxa();
			if (dc!=null) {
				setDefaultTaxa(dc);
				return dc;
			}
		}
		if (defaultTaxa == null){
			Taxa t = file.getProject().getTaxa(0);
			setDefaultTaxa(t);
			return t;
		}
		return file.getProject().getTaxa(null, defaultTaxa);
	}

	public void setDefaultCharacters(mesquite.lib.characters.CharacterData t){
		if (t==null)
			return;
		if (file == null || file.getProject() == null)
			return;
		defaultCharacters = file.getProject().getCharMatrixReferenceInternal(t); //remembered as id's to prevent memory leaks
	}
	public mesquite.lib.characters.CharacterData getDefaultCharacters(){
		if (file == null || file.getProject() == null)
			return null;
		if (defaultCharacters == null){
			mesquite.lib.characters.CharacterData dc = file.getCurrentData();
			if (dc!=null) {
				setDefaultCharacters(dc);
				return dc;
			}
		}

		if (defaultCharacters == null){
			mesquite.lib.characters.CharacterData t = file.getProject().getCharacterMatrix(getDefaultTaxa(), 0);
			setDefaultCharacters(t);
			return t;
		}
		return file.getProject().getCharacterMatrixByReference(null, defaultCharacters);
	}

	public void processLinkCTCommand(String s, MesquiteProject project, Parser parser){
		MesquiteInteger stringPos = new MesquiteInteger();
		stringPos.setValue(parser.getPosition());
		String[][] subcommands  = ParseUtil.getSubcommands(s, stringPos);
		if (!(subcommands == null || subcommands.length == 0 || subcommands[0] == null || subcommands[0].length == 0)){
			String ttoken = null;
			String ctoken = null;
			for (int i=0; i<subcommands[0].length; i++){
				String subC = subcommands[0][i];
				if  (subC.equalsIgnoreCase("CHARACTERS")) {
					ctoken  = subcommands[1][i];
				}
				else if  (subC.equalsIgnoreCase("TAXA")) {
					ttoken  = subcommands[1][i];
				}

			}
			if (!StringUtil.blank(ttoken)){
				Taxa t = project.getTaxaLastFirst(ttoken);

				if (t==null){
					int wt = MesquiteInteger.fromString(ttoken);
					if (MesquiteInteger.isCombinable(wt))
						t = project.getTaxa(wt-1);
				}
				if (t == null && project.getNumberTaxas(file)==1){
					t = project.getTaxa(file, 0);
				}
				if (t!=null) {
					setDefaultTaxa(t);
				}
			}
			CharacterData d = project.getCharacterMatrixReverseOrder(ctoken);
			if (d==null){
				int wt = MesquiteInteger.fromString(ctoken);
				if (MesquiteInteger.isCombinable(wt))
					d = project.getCharacterMatrix(getDefaultTaxa(), wt-1);
			}
			if (d == null && project.getNumberCharMatrices(file)==1){
				d = project.getCharacterMatrix(file, 0);
			}
			if (d!=null) {
				setDefaultCharacters(d);
			}

		}
	}
	void countCreated(){
		if (classesCreated.indexOf(getClass())<0) {
			classesCreated.addElement(getClass());
			countsOfClasses.addElement(new MesquiteInteger(1));
			countsOfClassesFinalized.addElement(new MesquiteInteger(0));
		}
		else {
			MesquiteInteger c = (MesquiteInteger)countsOfClasses.elementAt(classesCreated.indexOf(getClass()));
			if (c!=null)
				c.increment();
		}
	}
	boolean disposedAlready = false;
	public void dispose() {
		if (disposedAlready)
			return;
		manager = null;
		file = null;
		if (unrec != null)
			unrec.removeAllElements();
		unrec = null;
		disposedAlready = true;
		totalDisposed++;
	}
	public void finalize() throws Throwable {
		totalFinalized++;
		if (MesquiteTrunk.checkMemory) {
			if (classesFinalized.indexOf(getClass())<0) {
				classesFinalized.addElement(getClass());
			}
			MesquiteInteger c = (MesquiteInteger)countsOfClassesFinalized.elementAt(classesCreated.indexOf(getClass()));
			if (c!=null)
				c.increment();
		}
		super.finalize();
	}
	public long getID(){
		return id;
	}

	/** Returns the offical NEXUS block name (e.g. "TAXA")*/
	public abstract String getBlockName();

	/** DOCUMENT */
	public MesquiteFile getFile() { 
		return file;
	}
	/** DOCUMENT */
	public void setFile(MesquiteFile f) { 
		file = f;
	}
	/** DOCUMENT */
	public String getName() { 
		if (name == null)
			return "Untitled";
		else
			return name;
	}
	/** DOCUMENT */
	public void setName(String n) { 
		name = n;
	}
	/** DOCUMENT */
	public MesquiteModule getManager(){
		return manager;
	}
	/** DOCUMENT */
	public void setManager(MesquiteModule manager){
		this.manager = manager;
	}
	/** DOCUMENT */
	public abstract boolean contains(FileElement e);

	/** Returns true if this block must occur after the given block*/
	public abstract boolean mustBeAfter(NexusBlock block);

	/** DOCUMENT */
	public String getUnrecognizedCommands(){
		if (unrec==null)
			return "";
		String result = "";
		Enumeration enumeration=unrec.elements();
		while (enumeration.hasMoreElements()){
			Object obj = enumeration.nextElement();
			result += (String)obj + StringUtil.lineEnding();
		}
		return result;
	}
	/** DOCUMENT */
	void storeUnrecognizedCommand(String s){
		if (StringUtil.blank(s))
			return;

		if (unrec ==null)
			unrec = new Vector();
		unrec.addElement(s);
		if (file !=null && file.foreignElements!=null)
			file.foreignElements.addElement("Command: " + s + " in " + getBlockName() + " block ");
	}
	/** Returns the NEXUS block as a string for writing into the file.  Block must override either this or writeNEXUSBlock to write to file*/
	protected String getNEXUSBlock(){
		return null;
	}
	/** Writes the NEXUS block into the file  Block must override either this or getNEXUSBlock to write to file*/
	public void writeNEXUSBlock(MesquiteFile file, ProgressIndicator progIndicator){
		String s = getNEXUSBlock();
		if (!StringUtil.blank(s)){
			MesquiteTrunk.mesquiteTrunk.logln("      Writing " + getName());
			file.writeLine(s);
		}
	}
	/*   */

	public static String getNEXUSTime(long millis){
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeZone(TimeZone.getDefault());
		calendar.setTime(new Date(millis));
		return Integer.toString(calendar.get(Calendar.YEAR)) + "." + (calendar.get(Calendar.MONTH) +1) + "." + calendar.get(Calendar.DATE) + "." + calendar.get(Calendar.HOUR_OF_DAY) + "." + calendar.get(Calendar.MINUTE) + "." + calendar.get(Calendar.SECOND);
	}
	public static long getTimeFromNEXUS(String s){
		if (StringUtil.blank(s))
			return 0;
		try {
			DateFormat df = new SimpleDateFormat();
			Date ds = df.parse(s); 
			return ds.getTime();
		}
		catch (ParseException e){
		}
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeZone(TimeZone.getDefault());
		calendar.clear();
		StringTokenizer st = new StringTokenizer(s, ".");
		int year = MesquiteInteger.unassigned;
		int month = MesquiteInteger.unassigned;
		int date = MesquiteInteger.unassigned;
		int hour = MesquiteInteger.unassigned;
		int minute = MesquiteInteger.unassigned;
		int seconds = MesquiteInteger.unassigned;

		try{
			year = MesquiteInteger.fromString(st.nextToken());
			month = MesquiteInteger.fromString(st.nextToken())-1;
			date = MesquiteInteger.fromString(st.nextToken());
			hour = MesquiteInteger.fromString(st.nextToken());
			minute = MesquiteInteger.fromString(st.nextToken());
			seconds = MesquiteInteger.fromString(st.nextToken());
		}
		catch (Exception e){
		}
		if (MesquiteInteger.isCombinable(year) && MesquiteInteger.isCombinable(month) && MesquiteInteger.isCombinable(date)) {
			if (MesquiteInteger.isCombinable(minute) && MesquiteInteger.isCombinable(hour)) {
				if (MesquiteInteger.isCombinable(seconds)){
					calendar.set(year, month, date, hour, minute, seconds);
				}
				else 
					calendar.set(year, month, date);
			}
			else
				calendar.set(year, month, date, hour, minute);
		}
		else
			return 0;
		Date d = calendar.getTime();
		return d.getTime();
	}
}


