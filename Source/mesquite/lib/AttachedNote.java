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

import mesquite.lib.misc.AttachedNotesVector;
import mesquite.lib.ui.ImageLabel;
import mesquite.lib.ui.MesquiteImage;

/*
time mod and created should not be allowed to be 0!
/* ======================================================================== */
public class AttachedNote  implements NexusWritable {
	private String loc, comment, imPath, reference, extras;
	private long timeModified;
	private long timeCreated;
	private String timeCreatedString = null;
	Author author, authorModified;
	
	String authorLiteral, authorModifiedLiteral;
	String authorCodeLiteral, authorModifiedCodeLiteral;
	private String IDString; // possibly uniqueID string
	private Image im;
	AttachedNotesVector vector;
	Vector imageLabelsVector;
	Vector textFieldsVector;
	static int numCreated = 0;
	
	public AttachedNote(){
		stampDateCreated();
		imageLabelsVector = new Vector();
		textFieldsVector = new Vector();
		extras = "";
		numCreated++;
		IDString = MesquiteTrunk.getUniqueIDBase() + numCreated;
	}
	public String toString(){
		String s = "Comment: " + comment;
		s += "\nImage: " + imPath;
		s += "\nAuthor: " + getAuthorName();
		s += "\nCreated: " + getDateCreated();
		s += "\nModified by: " + getAuthorModifiedName();
		s += "\nModified: " + getDateModified();
		s += "\nReference: " + reference;
		return s;		
	}
	public void describeDifferences(AttachedNote other, StringBuffer sb){
		if (other == null || sb == null)
			return;
		if (!StringUtil.stringsEqual(comment, other.comment))
			sb.append("\nComment different: " + comment + " \nversus other\n " + other.comment);
		if (!StringUtil.stringsEqual(imPath, other.imPath))
			sb.append("\nImage paths different: " + imPath + " \nversus other\n " + other.imPath);
		if (!StringUtil.stringsEqual(getAuthorName(), other.getAuthorName()))
			sb.append("\nAuthors different: " + getAuthorName() + " \nversus other\n " + other.getAuthorName());
		if (timeCreated != other.timeCreated)
			sb.append("\nTime created different: " + getDateCreated() + " \nversus other\n " + other.getDateCreated());
		if (!StringUtil.stringsEqual(getAuthorModifiedName(), other.getAuthorModifiedName()))
			sb.append("\nLast Modified Authors different: " + getAuthorModifiedName() + " \nversus other\n " + other.getAuthorModifiedName());
		if (timeModified != other.timeModified)
			sb.append("\nTime modified different: " + getDateModified() + " \nversus other\n " + other.getDateModified());
		if (!StringUtil.stringsEqual(reference, other.reference))
			sb.append("\nReference different: " + reference + " \nversus other\n " + other.reference);
	}
	public boolean sameContents(AttachedNote other, boolean excludeDateCreated, boolean excludeDateModified){
		if (other == null)
			return false;
		if (!excludeDateCreated && timeCreated != other.timeCreated)
			return false;
		if (!excludeDateModified && timeModified != other.timeModified)
			return false;
		if (!excludeDateCreated && !StringUtil.stringsEqual(timeCreatedString, other.timeCreatedString))
			return false;
		if (!StringUtil.stringsEqual(loc, other.loc) || !StringUtil.stringsEqual(comment, other.comment) || !StringUtil.stringsEqual(extras, other.extras) || !StringUtil.stringsEqual(imPath, other.imPath) || !StringUtil.stringsEqual(reference, other.reference))
			return false;
		if (!labelsSame(imageLabelsVector, other.imageLabelsVector) || !labelsSame(other.imageLabelsVector, imageLabelsVector))
			return false;
		if (!textFieldsSame(textFieldsVector, other.textFieldsVector) || !textFieldsSame(other.textFieldsVector, textFieldsVector))
			return false;

		return true;
	}
	public boolean descendant(AttachedNote other){
		if (other == null)
			return false;
		return StringUtil.stringsEqual(other.IDString, IDString);
	}
	public AttachedNote cloneNote(){
		AttachedNote a = new AttachedNote();
		a.loc = loc;
		a.comment = (comment);
		a.imPath = (imPath);
		a.reference = (reference);
		a.extras = (extras);
		a.timeModified = timeModified;
		a.timeCreated = timeCreated;
		a.timeCreatedString = timeCreatedString;
		a.authorModified = authorModified;
		a.author = author;
		a.authorLiteral = authorLiteral;
		a.authorModifiedLiteral = authorModifiedLiteral;
		a.authorCodeLiteral = authorCodeLiteral;
		a.authorModifiedCodeLiteral = authorModifiedCodeLiteral;
		a.IDString = IDString;
		for (int i=0; i<getNumTextFields(); i++)
			a.textFieldsVector.addElement(new ANTextField(getTextField(i).getName(), getTextField(i).getString()));
		for (int i=0; i<getNumLabels(); i++)
			a.addLabel(getLabel(i).cloneLabel());
		return a;
	}
	public void setToClone(AttachedNote an){
		loc = an.loc;
		comment = an.comment;
		imPath = (an.imPath);
		reference = (an.reference);
		extras = (an.extras);
		timeModified = an.timeModified;
		timeCreated = an.timeCreated;
		timeCreatedString = an.timeCreatedString;
		authorModified = an.authorModified;
		author = an.author;
		authorLiteral = an.authorLiteral;
		authorModifiedLiteral = an.authorModifiedLiteral;
		authorCodeLiteral = an.authorCodeLiteral;
		authorModifiedCodeLiteral = an.authorModifiedCodeLiteral;
		IDString = an.IDString;
		for (int i=0; i<getNumTextFields(); i++)
			textFieldsVector.addElement(new ANTextField(an.getTextField(i).getName(), an.getTextField(i).getString()));
		for (int i=0; i<getNumLabels(); i++)
			addLabel(an.getLabel(i).cloneLabel());
	}
	public void setID(String s){
		IDString = s;
	}
	public String getID(){
		return IDString;
	}
	
	private boolean labelsSame(Vector v, Vector u){  
		if (v.size() !=u.size())
			 return false;
		for (int i= 0; i< u.size(); i++){
			ImageLabel label = (ImageLabel)u.elementAt(i);
			boolean matchFound = false;
			for (int j= 0; j< v.size();j++){
				ImageLabel otherLabel = (ImageLabel)v.elementAt(j);
				if (label.equals(otherLabel))
					matchFound = true;
			}
			if (!matchFound)
				return false;
		}
		return true;
	}
	private boolean textFieldsSame(Vector v, Vector u){  
		if (v.size() !=u.size())
			 return false;
		for (int i= 0; i< u.size(); i++){
			ANTextField label = (ANTextField)u.elementAt(i);
			boolean matchFound = false;
			for (int j= 0; j< v.size();j++){
				ANTextField otherLabel = (ANTextField)v.elementAt(j);
				if (label.equals(otherLabel))
					matchFound = true;
			}
			if (!matchFound)
				return false;
		}
		return true;
	}
	
	public boolean containsString(String s){
		if (StringUtil.foundIgnoreCase(reference, s))
			return true;
		else if (StringUtil.foundIgnoreCase(comment, s))
			return true;
		else if (author != null && StringUtil.foundIgnoreCase(author.getName(), s))
			return true;
		else if (authorModified != null && StringUtil.foundIgnoreCase(authorModified.getName(), s))
			return true;
		else if (StringUtil.foundIgnoreCase(authorLiteral, s))
			return true;
		else if (StringUtil.foundIgnoreCase(authorModifiedLiteral, s))
			return true;
		for (int i=0; i< getNumTextFields(); i++) {
			ANTextField tf = getTextField(i);
			if (tf != null) {
				if (StringUtil.foundIgnoreCase(tf.getString(), s))
					return true;
			}
		}
		for (int i=0; i< getNumLabels(); i++)
			if (StringUtil.foundIgnoreCase(getLabel(i).getText(), s))
				return true;
		return false;
	}
	
	public void setVector(AttachedNotesVector v){
		vector = v;
	}
	public String getComment(){
		return comment;
	}
	public void addExtraSubcommand(String s, boolean notify){
		extras += s;
		stampDateModified(notify);
	}
	public void setComment(String s, boolean notify){
		if (s == comment || (s!=null && comment!=null && s.equals(comment)))
			return;
		comment = s;
		stampDateModified(notify);
	}
	private ANTextField findTextField(String name){
		for (int i=0; i< textFieldsVector.size(); i++){
			ANTextField tf = (ANTextField)textFieldsVector.elementAt(i);
			if (tf.getName().equalsIgnoreCase(name))
				return tf;
		}
		return null;
		
	}
	
	public void setTextField(String name, String s, boolean notify){
		if (StringUtil.blank(name) || StringUtil.blank(s) || ";".equals(name)){
			MesquiteMessage.warnProgrammer("Warning: text field illegal in attached note (" + name + ") [" + s + "]");
		}
		ANTextField tf = findTextField(name);
		if (tf == null)
			textFieldsVector.addElement(new ANTextField(name, s));
		else 
			tf.setString(s);
		stampDateModified(notify);
	}
	public ANTextField getTextField(int i){
		if (i >=0 && i< textFieldsVector.size())
			return (ANTextField)textFieldsVector.elementAt(i);
		return null;
	}
	public int getNumTextFields(){
		return textFieldsVector.size();
	}
	//returns relative path of image
	public String getImagePath(){
		return loc;
	}
	//returns absolute path of image
	public String getAbsoluteImagePath(){
		return imPath;
	}
	public void setImage(Image s, boolean notify){
		im = s;
	}
	public void setImagePath(String path, String fullPath, boolean notify){
		loc = path;
		imPath = fullPath;
		if (StringUtil.blank(loc))
			loc = null;
		if (StringUtil.blank(imPath))
			imPath = null;
		stampDateModified(notify);
	}
	
   	public void attachImage(MesquiteModule module, boolean local){
   			if (module == null || module.getProject() == null)
   				return;
   			MesquiteProject proj = module.getProject();
			String relPath = null;
			String loc = null;
			Image im = null;
			if (local){
				MesquiteString dir = new MesquiteString();
				MesquiteString f = new MesquiteString();
				loc = MesquiteFile.openFileDialog("Image to attach", dir, f);
	   	 		if (StringUtil.blank(loc))
	   	 			return;

				relPath = MesquiteFile.decomposePath(proj.getHomeDirectoryName(), loc);
				module.logln("Attaching image at " + loc + " (path " + relPath + "  relative to " + proj.getHomeDirectoryName() + ")");
	   	 		im = MesquiteImage.getImage(relPath, proj);
				if (!MesquiteImage.waitForImageToLoad(im)) {
					String ext = StringUtil.getLastItem(loc, ".");
					if (ext == null || !(ext.equalsIgnoreCase("gif") || ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg")))
						module.alert("The image failed to load.  Currently, only gif and jpeg images can be attached to files");
					else
						MesquiteMessage.warnProgrammer("Image failed to load!");
					return;
				}
			}
			else {
				relPath = MesquiteString.queryString(module.containerOfModule(), "URL of image", "URL of the image to attach to this note", "");
				loc = "";
	   	 		if (StringUtil.blank(relPath))
					return;
				im = MesquiteImage.getImage(relPath);

			}
   	 		setImagePath(relPath, loc, false);
   	 		setImage(im, true);
   	 }
	public ImageLabel getLabel(int i){
		if (i>=0 && i<imageLabelsVector.size())
			return (ImageLabel)imageLabelsVector.elementAt(i);
		return null;
	}
	public int getNumLabels(){
		return imageLabelsVector.size();
	}
	public int addLabel(ImageLabel label){
		imageLabelsVector.addElement(label);
		return imageLabelsVector.size()-1;
	}
	public void deleteLabel(ImageLabel label){
		imageLabelsVector.remove(label);
	}
	public void bringToFront(ImageLabel label){
		if (imageLabelsVector.indexOf(label)>=0){
			imageLabelsVector.remove(label);
			imageLabelsVector.addElement(label);
		}
	}
	
	public String getReference(){
		return reference;
	}
	public void setReference(String s, boolean notify){
		if (s == reference || (s!=null && reference!=null && s.equals(reference)))
			return;
		reference = s;
		stampDateModified(notify);
	}
	public Author getAuthor(){
		return author;
	}
	public String getAuthorName(){
		if (author != null)
			return author.getName();
		return authorLiteral;
	}
	public void setAuthorCode(String s){
		authorCodeLiteral =s ;
	}
	public void setAuthorModifiedCode(String s){
		authorModifiedCodeLiteral =s ;
	}
	public void setAuthor(String s){
		authorLiteral = s;
		author = null;
	}
	public void setAuthor(Author s){
		authorLiteral = null;
		author = s;
	}
	public Author getAuthorModified(){
		return authorModified;
	}
	public String getAuthorModifiedName(){
		if (authorModified != null)
			return authorModified.getName();
		if (authorModifiedLiteral != null)
			return authorModifiedLiteral;
		return authorModifiedCodeLiteral;
	}
	public void setAuthorModified(String s){
		authorModifiedLiteral = s;
		authorModified = null;
	}
	public void setAuthorModified(Author s){
		authorModifiedLiteral = null;
		authorModified = s;
	}
	public void setDateCreated(long s){
		timeCreated = s;
		if (!MesquiteLong.isCombinable(s))
			MesquiteMessage.warnProgrammer("Warning: Date of note at uncombinable time " + this);
	}
	public void setDateCreatedString(String s){
		timeCreatedString = s;
	}
	public void stampDateCreated(){
		timeCreated = System.currentTimeMillis();
		timeModified = timeCreated;
		author = MesquiteModule.author;
	}
	public long getDateCreatedInMillis(){
		return timeCreated;
	}
	public String getDateCreated(){
		if (timeCreated == 0) {
			if (StringUtil.blank(timeCreatedString))
				return "No Time Recorded";
			return timeCreatedString;
		}
		Date dnow = new Date(timeCreated);
		return dnow.toString();
	}
	public void setDateModified(String s){
		timeModified = NexusBlock.getTimeFromNEXUS(s);
	}
	public void setDateModified(long s){
		timeModified = s;
		if (!MesquiteLong.isCombinable(s))
			MesquiteMessage.warnProgrammer("Warning: Date mod. of note at uncombinable time " + this);
	}
	int stampSuppress=0;
	public void incrementStampSuppress(){
		stampSuppress++;
	}
	public void decrementStampSuppress(){
		stampSuppress--;
	}
	public void stampDateModified(boolean notify){
		if (stampSuppress<=0){
			timeModified = System.currentTimeMillis();
			if (notify && vector != null)
				vector.notifyOwner(MesquiteListener.ANNOTATION_CHANGED);
		}
	}
	public String getDateModified(){
		if (timeModified == 0)
			return "No Time Recorded";
		Date dnow = new Date(timeModified);
		return dnow.toString();
	}
	public long getDateModifiedInMillis(){
		return timeModified;
	}
	public void deleteImage(boolean notify){
		imPath = null;
		loc = null;
		im = null;
		stampDateModified(notify);
	}
	public Image getImage(){
 		if (im == null && imPath !=null) {
	 		im = MesquiteImage.getImage(imPath);
			if (!MesquiteImage.waitForImageToLoad(im)) {
				MesquiteTrunk.mesquiteTrunk.logln("Error in loading image; location: " + imPath);
			}
		}

		return im;
	}
	String prepareString(String s, boolean useUnderscore){
		if (s == null || s.length() == 0) {
			if (!useUnderscore)
				return "";
			s = " ";
		}
		return StringUtil.tokenize(s);
	}
	public static final String ANNOTATION = "AN";
	public static final String TAXON = "T";
	public static final String TAXA = "TS";
	public static final String CHARACTERS = "CS";
	public static final String CHARACTER = "C";
	public static final String STATE = "ST";
	public static final String COMMENT = "CM"; 
	public static final String REFERENCE = "R"; 
	public static final String TEXTFIELD = "TF"; 
	public static final String AUTHOR = "AU"; 
	public static final String AUTHORCODE = "A"; 
	public static final String DATEMOD = "DM"; 
	public static final String DATECREATED = "DC"; 
	public static final String IMAGELOC = "I"; 
	public static final String IMAGELABEL = "L"; 
	public static final String WIDTH = "W"; 
	public static final String FIXEDTOIMAGE = "F"; 
	public static final String SHOWPOINTER = "SP"; 
	public static final String POINTERY = "PY"; 
	public static final String POINTERX = "PX"; 
	public static final String SIZE = "S"; 
	public static final String COLOR = "CO"; 
	public static final String COLORNUMBER = "CN"; 
	public static final String FONT = "F"; 
	public static final String TEXT = "TX"; 
	public static final String ID = "ID"; 
	
	public boolean anythingNEXUStoWrite(){
		if (!StringUtil.blank(getImagePath()))
				return true;
		if (!StringUtil.blank(getComment()))
				return true;
		if (!StringUtil.blank(getReference()))
				return true;
		for (int i=0; i< getNumTextFields(); i++) {
			ANTextField tf = getTextField(i);
			if (tf != null) {
				if (!StringUtil.blank(tf.getString()))
					return true;
			}
		}
		if (getNumLabels()>0)
			return true;
		if (extras != null)
			return true;
		return false;
	}
	public void getNexusString(StringBuffer s){
		s.append(" ");
		if (author ==null){
			if (authorLiteral != null){
				s.append(AUTHOR);
				s.append(" = ");
				s.append(prepareString(authorLiteral, true));
			}
			else if (authorCodeLiteral != null){
				s.append(AUTHORCODE);
				s.append(" = ");
				s.append(prepareString(authorCodeLiteral, true));
			}
		}
		else {
			s.append(AUTHORCODE);
			s.append(" = ");
			s.append(prepareString(author.getCode(), true));
		}
		s.append(" ");
		if (timeCreated != 0){
				s.append(DATECREATED);
				s.append(" = ");
				s.append(prepareString(NexusBlock.getNEXUSTime(timeCreated), true));
				s.append(" ");
		}
		else if (!StringUtil.blank(timeCreatedString)){ //only explicit string
				s.append("DATECREATED");
				s.append(" = ");
				s.append(prepareString(timeCreatedString, true));
				s.append(" ");
		}
		if (timeModified != 0){
			s.append(DATEMOD);
			s.append(" = ");
			s.append(prepareString(NexusBlock.getNEXUSTime(timeModified), true));
			s.append(" ");
		}
		s.append(ID);
		s.append(" = ");
		s.append(prepareString(getID(), true));
		s.append(" ");
		s.append(IMAGELOC);
		s.append(" = ");
		s.append(prepareString(getImagePath(), true));
		s.append(" ");
		s.append(TEXTFIELD);
		s.append(" = (");
		s.append(COMMENT);
		s.append(" ");
		s.append(prepareString(getComment(), false));
		s.append(")");
		s.append(" ");
		if (!StringUtil.blank(getReference())){
			s.append(TEXTFIELD);
			s.append(" = (");
			s.append(REFERENCE);
			s.append(" ");
			s.append(prepareString(getReference(), false));
			s.append(")");
		}
		for (int i=0; i< getNumTextFields(); i++) {
			ANTextField tf = getTextField(i);
			if (tf != null) {
				s.append(" ");
				s.append(TEXTFIELD);
				s.append(" = (");
				s.append(tf.getName());
				s.append(" ");
				s.append(prepareString(tf.getString(), false));
				s.append(")");
			}
		}
		for (int i=0; i< getNumLabels(); i++) {
			s.append(" ");
			s.append(IMAGELABEL);
			s.append(" = ");
			s.append(getLabel(i).getNexusString());
		}
		if (extras != null)
			s.append(extras);
	}
	public String getNexusString(){
		StringBuffer s = new StringBuffer(50);
		getNexusString(s);
		return s.toString();
	}
	
}
	
class ANTextField implements Listable {
	String name, s;
	public ANTextField(String name, String s){
		this.name = name;
		this.s = s;
	}
	
	public boolean equals(ANTextField other){
		return (StringUtil.stringsEqual(name, other.name) && StringUtil.stringsEqual(s, other.s));
	}
	public String getName(){
		return name;
	}
	public String getString(){
		return s;
	}
	public void setString(String s){
		this.s = s;
	}
}


