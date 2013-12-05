/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
Version 2.75, September 2011.
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


/* еееееееееееееееееееееееееее taxa еееееееееееееееееееееееееееееее */
/* ======================================================================== */
/** A taxon, including the taxon name, its number, an other information (e.g., notes and pictures attached to it).*/
public class Taxon  implements Listable, Illustratable, Identifiable {
	private String name;
	private int index = MesquiteInteger.unassigned;
	public int number = 0; //not used
	private Image illustration;
	private String illustrationPath;
	public static long totalCreated = 0;
	public static long totalFinalized = 0;
	private long id;
	private String uniqueID;
	private String link = null;
	private Taxa taxa = null;
	private boolean isDefaultName = true;
	private String synonym = null;
	//need footnotes etc.

	public Taxon(Taxa taxa){
		this.taxa = taxa;
		totalCreated++;
		id = totalCreated;
		if (Taxa.inventUniqueIDs)
			setUniqueID(MesquiteTrunk.getUniqueIDBase() + id);
	}
	public Taxa getTaxa() {
		return taxa;
	}
	public void finalize() throws Throwable {
		totalFinalized++;
		super.finalize();
	}
	public void setIndex(int index){
		this.index = index;
	}
	public int getNumber(){
		return getIndex();
	}

	public int getIndex(){
		if (index != MesquiteInteger.unassigned && taxa.getTaxon(index) == this)
			return index;
		return taxa.whichTaxonNumberCheck(this);
	}
	public long getID() {
		return id;
	}
	public String getUniqueID() {
		return uniqueID;
	}
	public void setUniqueID(String s) {
		uniqueID = s;
	}
	long nameChecksum = 0;
	java.util.zip.CRC32 crc = new java.util.zip.CRC32();
	
	boolean nameIsNull = true;
	public void setName(String name) {
		this.name = name;
		if (name == null){
			nameChecksum = 0;
			nameIsNull = true;
		}
		else {
			nameIsNull = false;
			crc.reset();
			for (int i = 0; i< name.length(); i++)
				crc.update(name.charAt(i));
			nameChecksum = crc.getValue();
		}
		isDefaultName = false;
	}
	public long getNameChecksum(){
		return nameChecksum;

	}
	public String getName() {
		return name;
	}
	public void setNameIsDefault(boolean def){
		isDefaultName = def;
	}
	public boolean isNameNull(){
		return nameIsNull;
	}
	public boolean isNameDefault(){
		return isDefaultName;
	}
	public void setSynonym(String name) {
		this.synonym = name;
	}
	public String getSynonym() {
		return synonym;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getLink() {
		return link;
	}
	/**Translates internal numbering system to external (currently, 0 based to 1 based*/
	public static int toExternal(int i){
		if (!MesquiteInteger.isCombinable(i))
			return i;
		else
			return i+1;
	}
	/**Translates external numbering system to internal (currently, 1 based to 0 based*/
	public static int toInternal(int i){
		if (!MesquiteInteger.isCombinable(i))
			return i;
		else
			return i-1;
	}
	public void setIllustration(Image illustration, String path) {
		this.illustration = illustration;
		this.illustrationPath = path;
	}
	public String getIllustrationPath() {
		return illustrationPath;
	}
	public Image getIllustration() {
		return illustration;
	}
}


