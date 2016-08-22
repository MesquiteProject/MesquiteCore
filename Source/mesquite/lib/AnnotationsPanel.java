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

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import java.awt.image.*;

/*
/** ======================================================================== */
public class AnnotationsPanel extends MesquitePanel implements Commandable, TextListener {
	AttachedNotesVector aim;
	HeaderPanel infoHeader;
	InfoPanel infoPanel;
	HeaderPanel commentHeader;
	TextArea commentPanel;
	HeaderPanel refHeader;
	TextArea refPanel;
	HeaderPanel imageHeader;
	AImagePanel imagePanel;
	boolean attachable = true;
	String where = "";
	String location = "";
	int currentNoteNumber = 0;
	MiniScroll scroll;
	ControlP controls;
	static AttachedNotesVector clipboardVector;
	static AttachedNote clipboardNote;
	MesquiteMenuItemSpec[]  m = new MesquiteMenuItemSpec[5];
	int controlsWidth = 100;
	int controlsHeight = 32;
	int textHeight = 60;
	int infoHeight = 50;
	int refHeight = 40;
	int imageHeight = 40;
	int windowWidth = 400;
	int currentColumn = -1;
	int currentRow = -1;
	int headerHeight = 20;
	int numHeaders = 4;
	TableTool pointerTool;
	AnnotPanelOwner annotOwner;
	public AnnotationsPanel (AnnotPanelOwner annotOwner){
		//setMinimalMenus(true);
		this.annotOwner =annotOwner;
		setLayout(null);
		pointerTool = new TableTool(this, "pointer", MesquiteModule.getRootImageDirectoryPath(), "pointer.gif", 4, 2, "Adjust Pointer", "Sets the destination of the pointer in an Image label.  Click on the label, then drag to put the pointer in the desired position.", null, null, null);
		pointerTool.setWorksOnMatrixPanel(false);
		setBackground(ColorTheme.getContentBackground());
		MesquiteModule mb = annotOwner.getModule();
		if (mb != null){
			m[0] = mb.addMenuSeparator();
			m[1] = mb.addMenuItem("Copy One Note", new MesquiteCommand("copyOne", this));
			m[2] = mb.addMenuItem("Copy All Notes", new MesquiteCommand("copyAll", this));
			m[3] = mb.addMenuItem("Paste (Add) Note(s)", new MesquiteCommand("paste", this));
			m[4] = mb.addMenuSeparator();
			mb.resetContainingMenuBar();
		}
		add(controls = new ControlP(this));
		scroll = controls.getScroll();
		controls.setVisible(true);
		//	add(titleHeader = new HeaderPanel(this, "Title"));
		//	titleHeader.setVisible(true);

		add(infoHeader = new HeaderPanel(this, "Author & Modification Dates"));
		infoHeader.setVisible(true);

		add(infoPanel = new InfoPanel(this));
		infoPanel.setVisible(true);


		add(commentHeader = new HeaderPanel(this, "Comment"));
		commentHeader.setVisible(true);

		add(commentPanel = new TextArea("",50,50, TextArea.SCROLLBARS_VERTICAL_ONLY));
		commentPanel.setEditable(true);
		commentPanel.setBackground(Color.white);
		commentPanel.setVisible(true);


		add(refHeader = new HeaderPanel(this, "Reference"));
		refHeader.setVisible(true);

		add(refPanel = new TextArea("",50,50, TextArea.SCROLLBARS_VERTICAL_ONLY));
		refPanel.setEditable(true);
		refPanel.setBackground(Color.white);
		refPanel.setVisible(true);

		add(imageHeader = new ImageHeaderPanel(this, "Image"));
		imageHeader.setVisible(true);

		add(imagePanel = new AImagePanel(this));
		imagePanel.setVisible(true);

		setSize(windowWidth, controlsHeight +  numHeaders*headerHeight + imageHeight + refHeight +textHeight + infoHeight);
		setLocation(100,100);
		commentPanel.addTextListener(this);
		refPanel.addTextListener(this);

		imagePanel.setVisible(aim != null);
		infoPanel.setVisible(aim != null);
		refPanel.setVisible(aim != null);
		commentPanel.setVisible(aim != null);
		
		
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	/** Gets the minimum size of the window */
	public Dimension getMinimumSize(){
		Dimension dim = super.getMinimumSize();
		if (dim.width< ControlP.spacing*2 + 200)
			dim.width = ControlP.spacing*2 + 200;
		return dim;
	}
	public void setAttachable(boolean a){
		if (a == attachable)
			return;
		attachable = a;
		controls.setAttachable(a);
	}
	public boolean isAttachable(){
		return attachable;
	}

	public void textValueChanged(TextEvent e){
		if (e.getSource() == commentPanel){
			if (commentPanel !=null) {
				String s = commentPanel.getText();
				if ("".equals(s))
					s = null;
				if (aim != null) {
					AttachedNote note = aim.getAttachedNote(currentNoteNumber);
					if (note !=null) {
						note.setComment(s, true);
						infoPanel.updateInfo();
					}
				}

				commentHeader.repaint();
			}
		}
		else if (e.getSource() == refPanel){
			if (refPanel !=null) {
				String s = refPanel.getText();
				if ("".equals(s))
					s = null;
				if (aim != null) {
					AttachedNote note = aim.getAttachedNote(currentNoteNumber);
					if (note !=null) {
						note.setReference(s, true);
						infoPanel.updateInfo();
					}
				}
				refHeader.repaint();

			}
		}
	}
	boolean active = false;
	public void setActive(boolean a){
		active = a;
		controls.repaint();
		//windowMessage.repaint();
	}
	/*.................................................................................................................*/
	public void toggleImagePresence( HeaderPanel panel, int modifiers, boolean local){
		if (panel == imageHeader && aim != null){
			AttachedNote an = aim.getAttachedNote(currentNoteNumber);
			if (an == null)
				return;
			if (an.getImagePath()!=null){ //image is present
				if (!MesquiteEvent.optionKeyDown(modifiers) && !AlertDialog.query(MesquiteWindow.windowOfItem(this), "Remove Image?", "Are you sure you want to remove the image from this note? (This will not delete the image file itself)", "OK", "Cancel", -1))
					return;
				an.deleteImage(true);
			}
			else
				annotOwner.chooseAndAttachImage(an, local);
			imagePanel.setImage(an.getImage());
			infoPanel.updateInfo();
			imageHeader.repaint();
			imagePanel.repaint();
		}
	}
	public AttachedNote getCurrentNote(){
		if (aim == null || aim.getAttachedNote(currentNoteNumber)== null) 
			return null;
		else 
			return aim.getAttachedNote(currentNoteNumber);
	}

	public String getTextContents() {
		AttachedNote note = getCurrentNote();
		if (note != null) {
			String s = "";
			if (note.getComment() != null)
				s += "Comment: " + note.getComment() + '\n';
			if (note.getReference() != null)
				s += "Reference: " + note.getReference() + '\n';
			if (note.getAuthorName() != null)
				s += "Created by Author(s): " + note.getAuthorName() + '\n';
			if (note.getDateCreated() != null)
				s += "Time Created: " + note.getDateCreated() + '\n';
			if (note.getAuthorModifiedName() != null)
				s += "Modified by Author(s): " + note.getAuthorModifiedName() + '\n';
			if (note.getDateModified() != null)
				s += "Time Modified: " + note.getDateModified() + '\n';
			return s;
		}
		return null;

	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		if (temp !=null)  {
			if (aim != null) 
				temp.addLine("goToNoteNumber " + (currentNoteNumber));
			//temp.addLine("resetAll");
			temp.incorporate(super.getSnapshot(file), false);
		}
		return temp;
	}

	/*.................................................................................................................*/
	MesquiteInteger pos = new MesquiteInteger();
	NameReference imageNameRef = NameReference.getNameReference("notes");
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(),  "Makes a label for current item", null, commandName, "makeItemLabel")) {
			AttachedNote note =	getCurrentNote();
			if (note == null)
				return null;
			if (note.getImage() == null)
				return null;
			ImageLabel label = new ImageLabel();

			label.setText("label");
			label.setShowPointer(false);
			note.addLabel(label);
			repaintImage();


		}
		else if (checker.compare(this.getClass(),  "Copies one note to notes clipboard", null, commandName, "copyOne")) {
			if (aim != null){
				if (currentNoteNumber >=0 && currentNoteNumber< aim.getNumNotes()){
					AttachedNote note = aim.getAttachedNote(currentNoteNumber);
					clipboardNote = note.cloneNote();
					clipboardVector = null;
				}
			}
			
		}
		else if (checker.compare(this.getClass(),  "Copies notes to notes clipboard", null, commandName, "copyAll")) {
			if (aim != null){
					clipboardVector = aim.cloneVector();
					clipboardNote = null;
			}
			
		}
		else if (checker.compare(this.getClass(),  "Paste notes here", null, commandName, "paste")) {
			if (clipboardNote == null && (clipboardVector == null || clipboardVector.getNumNotes()==0))return null;
			if (aim ==null){
				aim = annotOwner.makeNotesVector(this);
				if (aim == null)
					return null;
			}
			if (clipboardNote == null){
				aim.concatenate(clipboardVector);
				aim.notifyOwner(MesquiteListener.ANNOTATION_ADDED);
			}
			else {
				aim.addNote(clipboardNote, true);
			}
			setNote(aim.getNumNotes()-1);
			
		}
		else if (checker.compare(this.getClass(),  "Indicates which in a series of notes to show", "[note number (1 based)]", commandName, "goToNoteNumber")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int i= MesquiteInteger.fromString(arguments, io);
			if (MesquiteInteger.isCombinable(i)) {
				if (aim !=null) {
					i = i-1;
					setNote(i);
				}
			}
		}
		else if (checker.compare(this.getClass(),  "Recovers labels lost offscreen", null, commandName, "recoverLostLabels")) {
			imagePanel.recoverLostLabels();
		}

		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*_________________________________________________*/
	void requestAttachNote(){
		if (!attachable)
			return;
		if (aim ==null){
			aim = annotOwner.makeNotesVector(this);
			if (aim == null)
				return;
		}
		AttachedNote hL = new AttachedNote();
		hL.setAuthor(MesquiteModule.author);
		aim.addNote(hL, true);
		setNotes(aim, aim.getNumNotes()-1);

	}
	void panelGoAway(Panel p){
		annotOwner.panelGoAway(p);
	}
	public int getCurrentNoteNumber(){
		return currentNoteNumber;
	}
	public void setExplanation(String e){
		MesquiteWindow  pw = MesquiteWindow.windowOfItem(this);
		if (pw != null)
			pw.setExplanation(e);
	}
	public void setDefaultExplanation(){
		String s = "";
		//	if (where !=null)
		//		s = where + ": ";
		if (aim == null || aim.getNumNotes()<1) {
			if (where == null)
				setExplanation(s += "There are no notes");
			else
				setExplanation(s += "No notes are currently attached to "  + where + ". To attach a note, use the (+) at the top of the window.");
		}
		else {

			s += "Note " + (currentNoteNumber+1) + " of " + aim.getNumNotes();
			if (where != null)
				s += " attached to " + where;
			AttachedNote note = aim.getAttachedNote(currentNoteNumber);
			if (note != null){
				if (!StringUtil.blank(note.getAbsoluteImagePath()))
					s += " (Image at " + note.getAbsoluteImagePath() + ")";
			}
			setExplanation(s);
		}
	}
	public boolean doIHaveContent( HeaderPanel panel){
		if (panel == commentHeader){
			String s = commentPanel.getText();
			if (s==null || s.equals(""))
				return false;
			return true;
		}
		else if (panel == refHeader){
			String s = refPanel.getText();
			if (s==null || s.equals(""))
				return false;
			return true;
		}
		else if (panel == imageHeader){
			if (aim == null || aim.getAttachedNote(currentNoteNumber)== null) {
				return false;
			}
			else {
				AttachedNote note = aim.getAttachedNote(currentNoteNumber);
				if (note == null || note.getImage() == null)
					return false;
				return true;
			}
		}
		else if (panel == infoHeader){
			return true;
		}
		return false;
	}
	public void setNotes(AttachedNotesVector aim, String where, String location, int column, int row, int noteNumber){
		currentColumn = column;
		currentRow = row;
		this.aim = aim;
		this.where = where;
		this.location = location;
		//	if (where != null) {
		setDefaultExplanation();
		//	titleHeader.setTitle("Notes for " + where);
		//	titleHeader.repaint();
		//	}
		setNotes(aim, noteNumber);
	}	
	public void setNotes(AttachedNotesVector aim, int noteNumber){
		if (aim == null) {
			commentPanel.setText("");
			controls.setMaximumValue(0);
			imagePanel.setVisible(aim != null);
			infoPanel.setVisible(aim != null);
			refPanel.setVisible(aim != null);
			commentPanel.setVisible(aim != null);
			imagePanel.setImage(null);
			currentNoteNumber = 0;
			refPanel.setText("");
			controls.repaint();
			refHeader.repaint();
			imageHeader.repaint();
			commentHeader.repaint();
			infoHeader.repaint();
	//	controls.setBackground(null);
			return;
		}
		else {
			controls.setMaximumValue(aim.getNumNotes());
			setNote(noteNumber);
		}
	}
	public void setNote(int noteNumber){
		if (MesquiteInteger.isCombinable(noteNumber) && noteNumber>=0) {
			currentNoteNumber = noteNumber;
		}
		else 
			currentNoteNumber = 0;
		AttachedNote hL = null;
		if (aim.getNumNotes() < 1) { //automatically make one note if there is none
			hL = new AttachedNote();
			aim.addNote(hL, true);
			infoPanel.setNote(hL);
			currentNoteNumber = 0;
		}
		else {
			hL = aim.getAttachedNote(currentNoteNumber);
			if (hL == null)
				hL = aim.getAttachedNote(currentNoteNumber = 0);
		}

		hL.incrementStampSuppress();
		Image image = hL.getImage();
		String comment = hL.getComment();

		if (comment == null)
			comment = "";
		commentPanel.setText(comment);

		if (aim ==null || aim.getNumNotes()<1){
			controls.setMaximumValue(0);
		}
		else {
			controls.setMaximumValue(aim.getNumNotes());
		}

		String ref = hL.getReference();
		if (ref == null)
			ref = "";
		refPanel.setText(ref);

		controls.setCurrentValue(currentNoteNumber+1);
		imagePanel.setVisible(aim != null);
		infoPanel.setVisible(aim != null);
		refPanel.setVisible(aim != null);
		commentPanel.setVisible(aim != null);
		imagePanel.setImage(image);
		imageHeader.repaint();
		imagePanel.repaint();
		infoPanel.setNote(hL);
	//	controls.setBackground(null);
		controls.repaint();
		setDefaultExplanation();
		hL.decrementStampSuppress();
		//windowMessage.repaint();
	}
	public void repaintImage(){
		imagePanel.repaint();
	}
	public void deleteCurrentNote(int modifiers){
		if (!MesquiteEvent.optionKeyDown(modifiers) &&!AlertDialog.query(MesquiteWindow.windowOfItem(this), "Remove Note?", "Are you sure you want to remove this note and the file's reference to the current image (if any)? (This will not delete the image file itself)", "OK", "Cancel", -1))
			return;
		if (aim!=null) {
			aim.deleteNote(currentNoteNumber);
			if (aim.getNumNotes()==0)
			aim = null;
		}
		setNotes(aim, -1);
	}

	public void setSize(int width, int height){
		int hmi = controlsHeight +  numHeaders*headerHeight + infoHeight+ textHeight+ refHeight;
		imageHeight = height - hmi;
		if (imageHeight<40)
			imageHeight = 40;
		int vertical = 0;
		controls.setSize(width, controlsHeight);
		commentHeader.setSize(width, headerHeight);
		commentPanel.setSize(width, textHeight);
		refHeader.setSize(width, headerHeight);
		refPanel.setSize(width, refHeight);
		imageHeader.setSize(width, headerHeight);
		imagePanel.setSize(width, imageHeight);
		infoHeader.setSize(width, headerHeight);
		infoPanel.setSize(width, infoHeight);
		super.setSize(width, height);
	}
	public void setBounds(int x, int y, int width, int height){
		int hmi = controlsHeight +  numHeaders*headerHeight + infoHeight+ textHeight+ refHeight;
		imageHeight = height - hmi;
		if (imageHeight<40)
			imageHeight = 40;
		int vertical = 0;
		controls.setBounds(0, 0, width, controlsHeight);
		vertical += controlsHeight;
		commentHeader.setBounds(0, vertical,width, headerHeight);
		vertical += headerHeight;
		commentPanel.setBounds(0, vertical,width, textHeight);
		vertical += textHeight;
		refHeader.setBounds(0, vertical,width, headerHeight);
		vertical += headerHeight;
		refPanel.setBounds(0, vertical,width, refHeight);
		vertical += refHeight;
		imageHeader.setBounds(0, vertical,width, headerHeight);
		vertical += headerHeight;
		imagePanel.setBounds(0, vertical,width, imageHeight);
		vertical += imageHeight;
		infoHeader.setBounds(0, vertical,width, headerHeight);
		vertical += headerHeight;
		infoPanel.setBounds(0, vertical,width, infoHeight);
		super.setBounds(x, y, width, height);
	}
	boolean toolsPresent = false;
	public void setVisible(boolean vis){
		//pointer tool to redo pointer
		MesquiteWindow w = MesquiteWindow.windowOfItem(this);
		if (w != null){
			if (vis && !toolsPresent) {
				w.addTool(pointerTool);
				toolsPresent = true;
			}
			else if (!vis && toolsPresent) {
				w.removeTool(pointerTool);
				toolsPresent = false;
			}
		}
		if (m[0] != null){
			for (int i = 0; i< 5; i++)
				m[i].setEnabled(vis);
			MesquiteTrunk.resetMenuItemEnabling();
		}
		
		super.setVisible(vis);
	}
}
/* ======================================================================== */
class AImagePanel extends MesquitePanel {
	Image pic;
	AnnotationsPanel pw;
	int w =0;
	int h = 0;
	static int growboxSize = 8;
	public AImagePanel (AnnotationsPanel pw) {
		this.pw = pw;
	}
	int picX = 0;
	int picY = 0;
	int picWidth = 0;
	int picHeight = 0;
	double scale = 1.0;
	/*.................................................................................................................*/
	public void paint(Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		picX = 0;
		picY = 0;
		if (pic != null){
			g.setColor(Color.white);
			g.fillRect(0,0,w, h);
			g.setColor(Color.black);
			picWidth = pic.getWidth(this);
			picHeight = pic.getHeight(this);
			scale = MesquiteImage.getScaleToReduce(picWidth, picHeight, w, h);
			if (scale<1.0){
				picWidth *= scale;
				picHeight *= scale;
			}
			else
				scale = 1.0;
			picX = (w-picWidth)/2;
			picY = (h-picHeight)/2;
			g.drawImage(pic,picX, picY, picWidth, picHeight, (ImageObserver)this);
		}
		AttachedNote note = pw.getCurrentNote();
		if (note != null){
			Color c = g.getColor();
			for (int i=0; i<note.getNumLabels(); i++){
				Font f = g.getFont();
				ImageLabel label = note.getLabel(i);
				int labelX = label.getX();
				int labelY = label.getY();
				if (label.getFixedToImage()) {
					labelX += picX;
					labelY += picY;
				}
				if (label.getShowShadow()){
					g.setColor(Color.white);
					drawLabel(g, label, labelX, labelY, 1, 0);
					drawLabel(g, label, labelX, labelY, 1, 1);
					drawLabel(g, label, labelX, labelY, 0, 1);
					drawLabel(g, label, labelX, labelY, -1, 0);
					drawLabel(g, label, labelX, labelY, -1, -1);
					drawLabel(g, label, labelX, labelY, 0, -1);
					drawLabel(g, label, labelX, labelY, 1, -1);
					drawLabel(g, label, labelX, labelY, -1, 1);
				}

				Color cL = ColorDistribution.getStandardColor(label.getFontColor());
				if (cL != null)
					g.setColor(cL);
				else
					g.setColor(Color.black);
				drawLabel(g, label, labelX, labelY, 0, 0);

				g.setColor(c);
				g.setFont(f);
				if (highlightedLabel == label && filled && GraphicsUtil.useXORMode(g, false)){
					g.setXORMode(Color.white);
					g.fillRect(labelX, labelY, label.getWidth(), label.getHeight());
					if (squared) g.fillRect(labelX+label.getWidth()-growboxSize -2, labelY + label.getHeight()-growboxSize -2, growboxSize, growboxSize);
					g.setPaintMode();
				}
			}
		}
		MesquiteWindow.uncheckDoomed(this);
	}

	void drawLabel(Graphics g, ImageLabel label, int labelX, int labelY, int offX, int offY){
		StringInABox textBox = label.getTextBox();
		textBox.draw(g,labelX+offX+4, labelY+offY-4);
		if (label.getShowPointer()){
			drawPointerToClosestCorner(g, offX, offY, label);
		}
		g.drawRect(labelX+offX, labelY+offY, label.getWidth(), label.getHeight());
	}

	void drawPointerToClosestCorner(Graphics g, int offX, int offY, ImageLabel label){
		int pointerX = (int)(picX + scale*label.getPointerX()); //inverse recordedPointerX = (pointerX-picX)/scale;
		int pointerY = (int)(picY + scale*label.getPointerY());
		drawPointerToClosestCorner(g, pointerX, pointerY, offX, offY, label);
	}

	void drawPointerToClosestCorner(Graphics g, int pointerX, int pointerY, int offX, int offY, ImageLabel label){
		//first, get top left of label
		int labelX = label.getX();
		int labelY = label.getY();
		if (label.getFixedToImage()) {
			labelX += picX;
			labelY += picY;
		}
		pointerX += offX;
		pointerY += offY;
		labelX += offX;
		labelY+= offY;

		int sqDist =sqDistance(pointerX, pointerY, labelX, labelY); //top left
		int cornerX = labelX;
		int cornerY = labelY;

		int sqDist2 = sqDistance(pointerX, pointerY, labelX+label.getWidth(), labelY + label.getHeight()); //lower right
		if (sqDist>sqDist2) {
			sqDist = sqDist2;
			cornerX = labelX+label.getWidth();
			cornerY = labelY + label.getHeight();
		}
		sqDist2 = sqDistance(pointerX, pointerY, labelX+label.getWidth(), labelY); //top right
		if (sqDist>sqDist2) {
			sqDist = sqDist2;
			cornerX = labelX+label.getWidth();
			cornerY = labelY;
		}
		sqDist2 = sqDistance(pointerX, pointerY, labelX, labelY + label.getHeight()); //lower left
		if (sqDist>sqDist2) {
			sqDist = sqDist2;
			cornerX = labelX;
			cornerY = labelY + label.getHeight();
		}


		//picX + label.getPointerX()
		//int dTopLeft = 

		g.drawLine(pointerX, pointerY, cornerX, cornerY);
		g.fillOval(pointerX-1, pointerY-1, 2, 2);
	}
	int sqDistance(int x, int y, int x2, int y2){
		return (x-x2)*(x-x2) + (y-y2)*(y-y2);
	}
	/*.................................................................................................................*/
	public void recoverLostLabels() {
		AttachedNote note = pw.getCurrentNote();
		if (note != null){
			for (int i=0; i<note.getNumLabels(); i++){
				ImageLabel label = note.getLabel(i);
				recoverLostLabel(label);

			}
		}
		repaint();
	}
	/*.................................................................................................................*/
	void recoverLostLabel(ImageLabel label) {
		int labelX = label.getX();
		int labelY = label.getY();
		if (label.getFixedToImage()) {
			labelX += picX;
			labelY += picY;
			if (labelX < 0)
				label.setX(-picX);
			else if (labelX + label.getWidth()>w)
				label.setX(w-label.getWidth()-picX);

			if (labelY < 0)
				label.setY(-picY);
			else if (labelY + label.getHeight()>h)
				label.setY(h-label.getHeight()-picY);
		} 
		else {
			if (labelX < 0)
				label.setX(0);
			else if (labelX + label.getWidth()>w)
				label.setX(w-label.getWidth());

			if (labelY < 0)
				label.setY(0);
			else if (labelY + label.getHeight()>h)
				label.setY(h-label.getHeight());
		}
	}
	/*.................................................................................................................*/
	public void setBounds(int x, int y, int width, int height){
		w = width;
		h = height;
		super.setBounds(x, y, width, height);
	}
	/*.................................................................................................................*/
	public void setSize(int width, int height){
		w = width;
		h = height;
		super.setSize(width, height);
	}
	/*.................................................................................................................*/
	public void setImage(Image i){
		pic = i;
	}
	/*.................................................................................................................*/
	ImageLabel findLabel(int x, int y){
		AttachedNote note = pw.getCurrentNote();
		if (note == null)
			return null;

		for (int i=0; i<note.getNumLabels(); i++){
			ImageLabel label = note.getLabel(i);
			int labelX = label.getX();
			int labelY = label.getY();
			if (label.getFixedToImage()) {
				labelX += picX;
				labelY += picY;
			}
			if (x>=labelX && y >= labelY && x<= labelX+label.getWidth() && y<= labelY + label.getHeight())
				return label;
		}
		return null;
	}
	/*.................................................................................................................*/
	boolean inLabelCorner(ImageLabel label, int x, int y){
		int labelX = label.getX();
		int labelY = label.getY();
		if (label.getFixedToImage()) {
			labelX += picX;
			labelY += picY;
		}
		return (x>=labelX+label.getWidth()-growboxSize && y >= labelY + label.getHeight()-growboxSize && x<= labelX+label.getWidth() && y<= labelY + label.getHeight()) ;
	}
	boolean editLabel(ImageLabel label){
		String s = label.getText();
		if (s == null)
			s = "";

		String edited = MesquiteString.queryMultiLineString(MesquiteWindow.windowOfItem(pw), "Label", "Label:", s, 8, false, false);
		if (edited!=null)
			label.setText(edited);
		return (edited!=null && edited.length() > 0);
	}
	/*.................................................................................................................*/
	int prevPointX = MesquiteInteger.unassigned;
	int prevPointY = MesquiteInteger.unassigned;

	/*.................................................................................................................*/
	void drawPointerXOR(ImageLabel label, int pointX, int pointY, boolean undoOld, boolean drawNew){
		if (!GraphicsUtil.useXORMode(null, false))
			return;
		Graphics g=getGraphics();
		if (g==null)
			return;
		g.setXORMode(Color.white);
		g.setColor(Color.black);
		if (undoOld && MesquiteInteger.isCombinable(prevPointX)){
			//undo previous
			drawPointerToClosestCorner(g, prevPointX, prevPointY, 0, 0, label);
		}
		if (drawNew) {
			drawPointerToClosestCorner(g,  pointX, pointY, 0, 0, label);
			prevPointX = pointX;
			prevPointY = pointY;
		}
		else {
			prevPointX = MesquiteInteger.unassigned;
			prevPointY = MesquiteInteger.unassigned;
		}
		g.setPaintMode();
		g.dispose();
	}
	/*.................................................................................................................*/
	int touchedXImage = -1;
	int touchedYImage = -1;
	int prevX = MesquiteInteger.unassigned;
	int prevY = MesquiteInteger.unassigned;
	ImageLabel touchedLabel = null;
	boolean adjustingSize = false;
	boolean filled = false;
	boolean squared = false;
	/*.................................................................................................................*/
	void drawXOR(ImageLabel label, int offX, int offY, boolean undoOld, boolean drawNew){
		if (!GraphicsUtil.useXORMode(null, false))
			return;
		Graphics g=getGraphics();
		if (g==null)
			return;
		g.setXORMode(Color.white);
		g.setColor(Color.black);
		int labelX = label.getX();
		int labelY = label.getY();
		if (label.getFixedToImage()) {
			labelX += picX;
			labelY += picY;
		}
		if (adjustingSize){
			if (undoOld && MesquiteInteger.isCombinable(prevX)){
				//undo previous
				g.drawRect(labelX, labelY, prevX, prevY);
			}
			if (drawNew) {
				g.drawRect(labelX, labelY, label.getWidth()+offX, label.getHeight()+offY);
				prevX = label.getWidth()+offX;
				prevY = label.getHeight()+offY;
			}
			else {
				prevX = MesquiteInteger.unassigned;
				prevY = MesquiteInteger.unassigned;
			}
		}
		else {
			if (undoOld && MesquiteInteger.isCombinable(prevX)){
				//undo previous
				g.drawRect(prevX, prevY, label.getWidth(), label.getHeight());
			}
			if (drawNew) {
				g.drawRect(labelX+offX, labelY + offY, label.getWidth(), label.getHeight());
				prevX = labelX+offX;
				prevY = labelY+offY;
			}
			else {
				prevX = MesquiteInteger.unassigned;
				prevY = MesquiteInteger.unassigned;
			}
		}
		g.setPaintMode();
		g.dispose();
	}
	/*.................................................................................................................*/
	void fillXOR(ImageLabel label, boolean undo, boolean showSquare){
		if (!GraphicsUtil.useXORMode(null, false))
			return;
		Graphics g=getGraphics();
		if (g==null)
			return;
		g.setXORMode(Color.white);
		g.setColor(Color.black);
		int labelX = label.getX();
		int labelY = label.getY();
		if (label.getFixedToImage()) {
			labelX += picX;
			labelY += picY;
		}
		if (adjustingSize){
			if (undo){
				if (MesquiteInteger.isCombinable(prevX)){
					//undo previous
					g.fillRect(labelX, labelY, prevX, prevY);
					filled = !filled;
					if (squared) {
						g.fillRect(labelX + prevX -growboxSize - 2, labelY+ prevY -growboxSize - 2, growboxSize, growboxSize);
						squared = false;
					}
					prevX = MesquiteInteger.unassigned;
					prevY = MesquiteInteger.unassigned;
				}
			}
			else {
				g.fillRect(labelX, labelY, label.getWidth(), label.getHeight());
				filled = !filled;
				if (showSquare) {
					g.fillRect(labelX + label.getWidth() - growboxSize - 2, labelY + label.getHeight() - growboxSize - 2, growboxSize, growboxSize);
					squared = true;
				}
				else
					squared = false;
				prevX = label.getWidth();
				prevY = label.getHeight();
			}
		}
		else {
			if (undo) {
				if (MesquiteInteger.isCombinable(prevX)){
					//undo previous
					g.fillRect(prevX, prevY, label.getWidth(), label.getHeight());
					filled = !filled;
					if (squared) g.fillRect(prevX+label.getWidth()-growboxSize - 2, prevY+label.getHeight()-growboxSize - 2, growboxSize, growboxSize);
					squared = false;
				}
				prevX = MesquiteInteger.unassigned;
				prevY = MesquiteInteger.unassigned;
			}
			else {
				g.fillRect(labelX, labelY, label.getWidth(), label.getHeight());
				filled = !filled;
				if (showSquare) {
					g.fillRect(labelX + label.getWidth() - growboxSize - 2, labelY + label.getHeight() - growboxSize - 2, growboxSize, growboxSize);
					squared = true;
				}
				else
					squared = false;
				prevX = labelX;
				prevY = labelY;
			}
		}
		g.setPaintMode();
		g.dispose();
	}
	/*_________________________________________________*/
	public void mouseDown(int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		/*
		if click is with arrow tool:

		 */
		if (tool.getName().endsWith(".arrow")){
			//if in current label, then prepare to drag or drop down menu if 
			//otherwise ignore
			touchedLabel = findLabel(x,y);
			if (touchedLabel !=null) {
				if (MesquiteEvent.controlKeyDown(modifiers)) {
					redoTheMenu();
					popup.show(this, x+8,y+8);
				}
				else {
					touchedXImage = x;
					touchedYImage = y;
					adjustingSize = inLabelCorner(touchedLabel, x, y);
					drawXOR(touchedLabel, 0, 0, false, true);
				}
			}
		}
		else if (tool == pw.pointerTool){
			touchedLabel = findLabel(x,y);
			if (touchedLabel !=null) {
				drawPointerXOR(touchedLabel, x, y, false, true);
			}
		}
		else if (tool.getName().endsWith(".ibeam")){
			//if in current label, then edit label
			//if not in current label, then make new label
			touchedLabel = findLabel(x,y);
			if (touchedLabel !=null) {
				editLabel(touchedLabel);
			}
			else {
				AttachedNote note = pw.getCurrentNote();
				if (note == null || StringUtil.blank(note.getImagePath()))
					return;
				ImageLabel label = new ImageLabel();
				if (scale != 0)
					label.setLocation(x-picX, y-picY);
				else
					label.setLocation(x,y);
				label.setPointerX((int)((picWidth/2.0)/scale));
				label.setPointerY((int)((picHeight/2.0)/scale));
				if (editLabel(label))
					note.addLabel(label);
			}
			touchedLabel = null;
			repaint();

		}
	}

	/*_________________________________________________*/
	public void mouseDrag(int modifiers, int x, int y, MesquiteTool tool) {
		//pw.mouseDown(modifiers, x, y, tool);
		if (tool.getName().endsWith(".arrow")){
			//if in current label, then prepare to drag or drop down menu if 
			//otherwise ignore
			if (touchedLabel !=null) {
				drawXOR(touchedLabel, x-touchedXImage, y-touchedYImage, true, true);
			}
		}
		else if (tool == pw.pointerTool){
			if (touchedLabel !=null) {
				drawPointerXOR(touchedLabel, x, y, true, true);
			}
		}
	}
	/*_________________________________________________*/
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		//if arrow and not moved, then select label
		if (tool.getName().endsWith(".arrow")){
			//if in current label, then prepare to drag or drop down menu if 
			//otherwise ignore
			if (touchedLabel !=null) {
				if (adjustingSize) {
					//drawXOR(touchedLabel,  x-touchedXImage, y-touchedYImage, true, false);
					int w = (x-touchedXImage+touchedLabel.getWidth());
					if (w <32)
						w = 32;
					touchedLabel.setWidth(w);
					fillXOR(touchedLabel,  true, true); //dehighlight

					adjustingSize = false;
					if (findLabel(x,y)==touchedLabel){ //need to rehighlight
						fillXOR(touchedLabel,  false, true);
					}
					else
						highlightedLabel = null;
				}
				else {
					//drawXOR(touchedLabel,  x-touchedXImage, y-touchedYImage, true, false);
					touchedLabel.setX(x-touchedXImage+touchedLabel.getX());
					touchedLabel.setY(y-touchedYImage+touchedLabel.getY());
					highlightedLabel = touchedLabel; //should sstill be highlighted
					filled = true;
				}
				touchedLabel = null;
				touchedXImage = -1;
				touchedYImage = -1;

				repaint();
			}
		}
		else if (tool == pw.pointerTool){
			if (touchedLabel !=null) {
				drawPointerXOR(touchedLabel, x, y, true, false);
				if (scale != 0){
					touchedLabel.setPointerX((int)((x-picX)*1.0/scale));
					touchedLabel.setPointerY((int)((y-picY)*1.0/scale));
				}
				//if x, y are in label, then leave highlight on
				if (findLabel(x,y)!=touchedLabel){
					fillXOR(touchedLabel,  true, false);
					highlightedLabel = null;
				}
				touchedLabel = null;
				repaint();
			}
		}
	}
	/*_________________________________________________*/
	ImageLabel highlightedLabel = null;
	public void mouseMoved(int modifiers, int x, int y, MesquiteTool tool) {
		//if (tool == pw.arrowTool){
		ImageLabel current = findLabel(x,y);
		if (current != highlightedLabel){
			if (highlightedLabel!= null)
				fillXOR(highlightedLabel,  true, tool.getName().endsWith(".arrow"));
			if (current!= null)
				fillXOR(current,  false, tool.getName().endsWith(".arrow"));
			highlightedLabel = current;
		}
		//}
	}
	/*_________________________________________________*/
	public void mouseEntered(int modifiers, int x, int y, MesquiteTool tool) {
		//if (tool == pw.arrowTool){
		ImageLabel current = findLabel(x,y);
		if (current != highlightedLabel){
			if (highlightedLabel!= null)
				fillXOR(highlightedLabel,  true, tool.getName().endsWith(".arrow"));
			if (current!= null)
				fillXOR(current,  false, tool.getName().endsWith(".arrow"));
			highlightedLabel = current;
		}
		//}
	}
	/*_________________________________________________*/
	public void mouseExited(int modifiers, int x, int y, MesquiteTool tool) {
		//if (tool == pw.arrowTool){
		if (highlightedLabel!= null)
			fillXOR(highlightedLabel,  true, tool.getName().endsWith(".arrow"));
		highlightedLabel = null;
		//}
	}
	/*_________________________________________________*/
	MesquitePopup popup=null;
	MesquiteCommand setFontCommand =MesquiteModule.makeCommand("setFont",  this);
	MesquiteCommand setFontSizeCommand =MesquiteModule.makeCommand("setFontSize",  this);
	MesquiteCommand setColorCommand =MesquiteModule.makeCommand("setColor",  this);
	MesquiteCommand fixToWindowCommand=MesquiteModule.makeCommand("fixToWindow",  this);
	MesquiteCommand fixToImageCommand=MesquiteModule.makeCommand("fixToImage",  this);
	MesquiteCommand hidePointerCommand=MesquiteModule.makeCommand("hidePointer",  this);
	MesquiteCommand showPointerCommand=MesquiteModule.makeCommand("showPointer",  this);
	MesquiteCommand hideShadowCommand=MesquiteModule.makeCommand("hideShadow",  this);
	MesquiteCommand showShadowCommand=MesquiteModule.makeCommand("showShadow",  this);
	MesquiteCommand bringToFrontCommand=MesquiteModule.makeCommand("bringToFront",  this);
	MesquiteCommand deleteLabelCommand=MesquiteModule.makeCommand("deleteLabel",  this);

	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (touchedLabel == null)
			return  super.doCommand(commandName, arguments, checker);
		if (checker.compare(this.getClass(), "Sets the font", "[font name]", commandName, "setFont")) {
			touchedLabel.setFontName(ParseUtil.getFirstToken(arguments, pos));
			repaint();

		}
		else if (checker.compare(this.getClass(), "Sets the font size", "[font size]", commandName, "setFontSize")) {
			int siz = MesquiteInteger.fromString(ParseUtil.getFirstToken(arguments, pos));
			touchedLabel.setFontSize(siz);
			repaint();
		}
		else if (checker.compare(this.getClass(), "Sets the color", "[color]", commandName, "setColor")) {
			touchedLabel.setFontColor(ParseUtil.getFirstToken(arguments, pos));
			repaint();
		}
		else if (checker.compare(this.getClass(), "Fixes label to window", null, commandName, "fixToWindow")) {
			touchedLabel.setFixedToImage(false);
			recoverLostLabel(touchedLabel);
			repaint();
		}
		else if (checker.compare(this.getClass(), "Fixes label to image", null, commandName, "fixToImage")) {
			touchedLabel.setFixedToImage(true);
			repaint();
		}
		else if (checker.compare(this.getClass(), "Hides the pointer", null, commandName, "hidePointer")) {
			touchedLabel.setShowPointer(false);
			repaint();
		}
		else if (checker.compare(this.getClass(), "Shows the pointer", null, commandName, "showPointer")) {
			touchedLabel.setShowPointer(true);
			repaint();
		}
		else if (checker.compare(this.getClass(), "Hides the shadow", null, commandName, "hideShadow")) {
			touchedLabel.setShowShadow(false);
			repaint();
		}
		else if (checker.compare(this.getClass(), "Shows the shadow", null, commandName, "showShadow")) {
			touchedLabel.setShowShadow(true);
			repaint();
		}
		else if (checker.compare(this.getClass(), "Deletes the label", null, commandName, "deleteLabel")) {
			AttachedNote note = pw.getCurrentNote();
			note.deleteLabel(touchedLabel);
			touchedLabel = null;
			highlightedLabel = null;
			repaint();
		}
		else if (checker.compare(this.getClass(), "Brings label to front", null, commandName, "bringToFront")) {
			AttachedNote note = pw.getCurrentNote();
			note.bringToFront(touchedLabel);
			repaint();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	void redoTheMenu() {
		if (popup==null)
			popup = new MesquitePopup(this);
		else
			popup.removeAll();
		MesquiteSubmenu submenuFont=MesquiteSubmenu.getFontSubmenu("Font", popup, null, setFontCommand);
		popup.add(submenuFont);
		MesquiteSubmenu submenuSize=MesquiteSubmenu.getFontSizeSubmenu("Font Size", popup, null, setFontSizeCommand);
		popup.add(submenuSize);
		MesquiteSubmenu submenuColor=MesquiteSubmenu.getSubmenu("Color", popup, null);
		for (int i=0; i<ColorDistribution.standardColorNames.getSize(); i++) {
			String s =ColorDistribution.standardColorNames.getValue(i);
			if (s!=null)
				submenuColor.add(new MesquiteMenuItem(s,  null, setColorCommand, StringUtil.tokenize(s)));
		}
		popup.add(submenuColor);
		if (touchedLabel!= null){
			if (touchedLabel.getFixedToImage())
				popup.add(new MesquiteMenuItem("Fix Label to Window",  null, fixToWindowCommand));
			else
				popup.add(new MesquiteMenuItem("Fix Label to Image",  null, fixToImageCommand));
			if (touchedLabel.getShowPointer())
				popup.add(new MesquiteMenuItem("Hide Pointer",  null, hidePointerCommand));
			else
				popup.add(new MesquiteMenuItem("Show Pointer",  null, showPointerCommand));
			if (touchedLabel.getShowShadow())
				popup.add(new MesquiteMenuItem("Hide Shadow",  null, hideShadowCommand));
			else
				popup.add(new MesquiteMenuItem("Use White Shadow",  null, showShadowCommand));
			popup.add(new MesquiteMenuItem("Bring to Front",  null, bringToFrontCommand));
			popup.add(new MesquiteMenuItem("Delete Label",  null, deleteLabelCommand));

		}

		add(popup);
	}
}
/*drop down menu:
      		bring to front
      		shadow
      			white shadow
      		use note style as default
 */
/* ======================================================================== */
class ControlP extends MesquitePanel {
	AnnotationsPanel pw;
	MiniScroll scroll;
	Image trash, addPic, goaway;
	int maxValue;
	boolean allowNewWindow;
	int numicons = 3;
	boolean attachable = true;
	Font veryBigFont = new Font("SanSerif", Font.BOLD, 18);
	Font bigFont = new Font("SanSerif", Font.PLAIN, 14);
	Font smallFont = new Font("SanSerif", Font.PLAIN, 10);

	public ControlP (AnnotationsPanel pw) {
		this.pw = pw;
		setFont(smallFont);
		setBackground(ColorDistribution.brown);
		add(scroll = new MiniScroll(MesquiteModule.makeCommand("goToNoteNumber", pw), false, 1, 1, 1,"image"));
		scroll.setLocation(spacing*(numicons+1) + closeLeft, 6);
		scroll.setSize(pw.controlsWidth, pw.textHeight);
		scroll.setVisible(true);
		trash = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "trashcan.gif");
		addPic = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "add.gif");
		goaway = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "goaway.gif");
		/*
		newPic = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "new.gif");
		spotPic = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "spot.gif");
		spotGreenPic = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "spotgreen.gif");
		 */
	}
	void setAttachable(boolean a){
		attachable = a;
		if (!attachable)
			pw.setExplanation("There is no item selected to which annotations can be attached");
		scroll.setVisible(a);
	}
	MiniScroll getScroll(){
		return scroll;
	}

	int addLeft = 8;
	int closeLeft = 2;
	int iconTop = -1;
	static int spacing = 22;
	public void setCurrentValue(int i){
		scroll.setCurrentValue(i);
	}
	public void setMaximumValue(int i){
		maxValue = i;
		if (i == 0)
			scroll.setMinimumValue(0);
		else
			scroll.setMinimumValue(1);
		if (scroll.getCurrentValue()>i)
			scroll.setCurrentValue(i);
		scroll.setMaximumValue(i);
		scroll.setEnableEnter(i>1);
	}
	/*
	public void setBackground(Color c){

		Color q = ColorDistribution.medium[pw.getColorScheme()];
		if (q == null)
			super.setBackground(c);
		else
			super.setBackground(q);
	}
	/*.................................................................................................................*/
	public void paint(Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		int width = getBounds().width;
		int height = getBounds().height;
		g.setColor(Color.black);
		g.drawRect(0, 0, width,height-1);
		int miniscrollRight = scroll.getBounds().x + scroll.getBounds().width;
		addLeft = closeLeft + spacing;
		iconTop = height - 24;
		int num = maxValue;
		if (!attachable){
			//g.drawString(s, miniscrollRight+4, height-12);
			if (goaway != null)
				g.drawImage(goaway, closeLeft, iconTop, this);
		}
		else {
			g.setFont(veryBigFont);
			g.setColor(Color.white);
			int offset = 6;
			if (pw.location != null) {
				g.drawString(pw.location, miniscrollRight+4, height-12);
				offset += StringUtil.getStringDrawLength(g, pw.location);
			}
			g.setFont(smallFont);
			g.setColor(Color.black);
			if (pw.where != null)
				g.drawString(pw.where, miniscrollRight+4 + offset, height-12);

			g.setColor(Color.black);
			g.drawLine(spacing*numicons, 0, spacing*numicons,height);
			g.setFont(bigFont);
			g.drawString(Integer.toString(num), spacing*numicons+4, height-12);
			g.setFont(smallFont);
			if (goaway != null)
				g.drawImage(goaway, closeLeft, iconTop, this);
			if (addPic != null)
				g.drawImage(addPic, addLeft, iconTop, this);
			if (num >0 && trash != null)
				g.drawImage(trash, addLeft+spacing, iconTop, this);
		}
		MesquiteWindow.uncheckDoomed(this);
	}

	static final int TRASH = 0;
	static final int ADD = 1;
	static final int CLOSE = 3;

	int buttonFound(int x, int y){
		if (x >= closeLeft && x< closeLeft + 16 && y >= iconTop && y< iconTop+16) 
			return CLOSE;
		else if (x >= addLeft && x< addLeft + 16 && y >= iconTop && y< iconTop+16) 
			return ADD;
		else if (x >= addLeft + spacing && x< addLeft + 16 + spacing && y >= iconTop && y< iconTop+16) 
			return TRASH;
		else
			return -1;
	}

	private void explainButton(int b){
		if (b==TRASH) 
			pw.setExplanation("Trash can: delete the current note");
		else if (b==ADD)
			pw.setExplanation("(+) button: add a new note at this location");
		else if (b==CLOSE) {
			pw.setExplanation("Close: closes the annotations panel");
		}
		else
			pw.setDefaultExplanation();
	}
	public void mouseMoved(int modifiers, int x, int y, MesquiteTool tool) {
		int b = buttonFound(x,y);
		if (!attachable && b != CLOSE)
			return;
		explainButton(b);

	}
	public void mouseEntered(int modifiers, int x, int y, MesquiteTool tool) {
		int b = buttonFound(x,y);
		if (!attachable && b != CLOSE)
			return;
		explainButton(b);
	}
	public void mouseExited(int modifiers, int x, int y, MesquiteTool tool) {
		if (!attachable)
			return;
		pw.setDefaultExplanation();
	}

	/* to be used by subclasses to tell that panel touched */
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		int b = buttonFound(x,y);
		if (!attachable && b != CLOSE)
			return;
		if (b==TRASH) 
			pw.deleteCurrentNote(modifiers);
		else if (b==ADD)
			pw.requestAttachNote();
		else if (b==CLOSE)
			pw.panelGoAway(this);

	}
}
/* ======================================================================== */
class HeaderPanel extends MesquitePanel {
	protected AnnotationsPanel pw;
	String title;
	Image triangle, triangleDown;
	Image triangleRightOn, triangleDownOn;
	static Image checkMark;

	static {
		checkMark = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "checkMark.gif");
	}
	public HeaderPanel (AnnotationsPanel pw, String title) {
		this.pw = pw;
		this.title = title;
	}

	int buttonFound(int x, int y){
		return -1;
	}
	public void setTitle(String s){
		title = s;
	}
	/*.................................................................................................................*/
	public void paint(Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		//if (pw.aim != null){
		int width = getBounds().width;
		int height = getBounds().height;
		g.setColor(Color.black);
		g.drawString(title, 4, height-6);
		//}
		MesquiteWindow.uncheckDoomed(this);
	}

}
/* ======================================================================== */
class ImageHeaderPanel extends HeaderPanel {
	String title;
	Image trash, addPic, addRemotePic;
	boolean hasPic = false;
	int spacing = 22;

	public ImageHeaderPanel (AnnotationsPanel pw, String title) {
		super(pw, title);
		trash = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "trashcan.gif");
		addPic = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "add.gif");
		addRemotePic = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "addRemote.gif");
	}
	/*.................................................................................................................*/
	public void paint(Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		super.paint(g);
		if (pw.aim!=null){
			if (pw.doIHaveContent(this))
				g.drawImage(trash, addLeft, 2, this); 
			else {
				g.drawImage(addPic, addLeft, 2, this); 
				g.drawImage(addRemotePic, addLeft + spacing, 2, this);
			}

		}
		MesquiteWindow.uncheckDoomed(this);
	}
	static final int ASIMAGE = 0;
	static final int ASIMAGEREMOTE = 1;
	static final int addLeft = 64;
	int buttonFound(int x, int y){
		if (x>addLeft && x< addLeft+16)
			return ASIMAGE;
		else if (!pw.doIHaveContent(this) && (x>addLeft + spacing && x< addLeft + spacing +16))
			return ASIMAGEREMOTE;
		else 
			return super.buttonFound(x,y);
	}

	public void mouseMoved(int modifiers, int x, int y, MesquiteTool tool) {
		int b = buttonFound(x,y);
		if (b==ASIMAGE) {
			if (pw.doIHaveContent(this))
				pw.setExplanation("Trash can: delete the image from this note.  This will not delete the image from its original location; it will delete only this note's reference to the image");
			else
				pw.setExplanation("(+) button: add a local image to this note. ");
		}
		else if (b==ASIMAGEREMOTE && !pw.doIHaveContent(this))
			pw.setExplanation("(+') button: add a remote image (at a URL) to this note.");
		else
			pw.setDefaultExplanation();

	}
	public void mouseEntered(int modifiers, int x, int y, MesquiteTool tool) {
		int b = buttonFound(x,y);
		if (b==ASIMAGE) {
			if (pw.doIHaveContent(this))
				pw.setExplanation("Trash can: delete the image from this note.  This will not delete the image from its original location; it will delete only this note's reference to the image");
			else
				pw.setExplanation("(+) button: add a local image to this note. ");
		}
		else if (b==ASIMAGEREMOTE && !pw.doIHaveContent(this))
			pw.setExplanation("(+') button: add a remote image (at a URL) to this note.");
		else
			pw.setDefaultExplanation();
	}
	public void mouseExited(int modifiers, int x, int y, MesquiteTool tool) {
		pw.setDefaultExplanation();
	}
	/* to be used by subclasses to tell that panel touched */
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		int b = buttonFound(x,y);
		if (b==ASIMAGE)
			pw.toggleImagePresence(this, modifiers, true);
		else if (b==ASIMAGEREMOTE)
			pw.toggleImagePresence(this, modifiers, false);
		else
			super.mouseUp(modifiers, x, y, tool);
	}
}

/* ======================================================================== */
class InfoPanel extends MesquitePanel implements TextListener {
	AttachedNote note;
	AnnotationsPanel pw;
	int w =0;
	int h = 0;
	TextField authorLabel;
	Label createdLabel, modLabel;
	int labelWidth = 4;
	String previousAuthor = null;
	public InfoPanel (AnnotationsPanel pw) {
		this.pw = pw;

		//If there is a user's default author name, use it.  Otherwise copy from previous
		Author auth = MesquiteModule.author;
		String aName = null;

		if (auth != null)
			aName = auth.getName();
		if (aName == null)
			aName = previousAuthor;
		add(authorLabel = new TextField(aName));

		authorLabel.setEditable(true);
		authorLabel.setBackground(Color.white);
		authorLabel.addTextListener(this);
		add(createdLabel = new Label());
		add(modLabel =new Label());
		//	modLabel.setBackground(Color.pink);
		//	setBackground(Color.yellow);
		setNote(null);
	}
	public void textValueChanged(TextEvent e){ //ONLY if 

		if (e.getSource() == authorLabel){
			String s = authorLabel.getText();
			if ("".equals(s))
				s = null;
			if (note != null) {
				if (s != null && note.getAuthor()!= null && s.equals(note.getAuthor().getName()))
					return;
				note.setAuthor(s);
				previousAuthor = s;
				modLabel.setText(note.getDateModified());
			}
		}
	}
	public void setBounds(int x, int y, int width, int height){
		w = width;
		h = height;
		authorLabel.setBounds(0,0,width-0, height/2);
		createdLabel.setBounds(labelWidth,height/2 +4,width/2-labelWidth, height/2-2);
		modLabel.setBounds(labelWidth + width/2,height/2 +4,width/2-labelWidth, height/2-2);
		super.setBounds(x, y, width, height);
	}
	public void setSize(int width, int height){
		w = width;
		h = height;
		authorLabel.setBounds(4,0,width-labelWidth, height/2);
		createdLabel.setBounds(4,height/2 +4,width/2-labelWidth, height/2-2);
		modLabel.setBounds(labelWidth + width/2,height/2 +4,width/2-labelWidth, height/2-2);
		super.setSize(width, height);
	}
	public void setNote(AttachedNote i){
		note = i;
		updateInfo();
	}
	public void updateInfo(){
		if (note != null){
			note.incrementStampSuppress();
			authorLabel.setText(note.getAuthorName());
			createdLabel.setText("Created " + note.getDateCreated());
			modLabel.setText("Modified " + note.getDateModified());
			note.decrementStampSuppress();
		}
	}
	/*.................................................................................................................*/
	public void paint(Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		g.drawLine(0,0,w, 0);

		MesquiteWindow.uncheckDoomed(this);
	}
}

