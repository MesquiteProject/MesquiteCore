/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.molec.lib;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.categ.lib.*;
import mesquite.molec.lib.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
/* ======================================================================== *

*new in 1. 06*

/* ======================================================================== */
public abstract class MaintainSequenceMatch extends FindSequenceCriterion {
	String sequence = null;
	int it = -1;
	SeqCheckThread thread;
	CharacterData data;
	MesquiteTable table;
	
	Ledge panel;
	protected SeqLedge ledge;
	/*.................................................................................................................*/
 	public void endJob() {
 		if (thread != null)
 			thread.abort = true;
 		thread = null;
 		MesquiteWindow f = containerOfModule();
		if (f instanceof MesquiteWindow){
			((MesquiteWindow)f).removeLedgePanel(panel);
		}
 		super.endJob();
   	 }
   	 public void goAway(){
   	 	iQuit();
   	 }
   	 
   	 public void chooseTaxon(){
   	 	if (data == null)
   	 		return;
		Taxa taxa = data.getTaxa();
		Taxon taxon = taxa.userChooseTaxon(containerOfModule(), "Search within which taxon?");
		int empit = taxa.whichTaxonNumber(taxon);
		if (empit>=0)
			it = empit;
   	 }
   	 
	public boolean showOptions(CharacterData data, MesquiteTable table){
		this.table = table;
		if (data == null)
			return false;
		this.data = data;
		sequence = null;
		it = -1;
		chooseTaxon();
		MesquiteWindow f = containerOfModule();
		if (f instanceof MesquiteWindow){
			panel = new Ledge(this);
			ledge = panel;
			((MesquiteWindow)f).addLedgePanel(panel, 22);
			panel.setVisible(true);
		}
		if (thread == null){
			thread = new SeqCheckThread(this, panel);
			thread.start();
		}
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
   	/*
   	public boolean findNext(CharacterData data, MesquiteTable table, MesquiteInteger charFound, MesquiteInteger length, MesquiteInteger taxonFound) {
   		this.table = table;
		findSeq(data, table, charFound, length, taxonFound);
		return true;
   	}
   	*/
   	public String checkSeq() {
   		if (data != null && table != null)
			findSeq(data, table, null, null, null);
		return sequence;
   	}
   	
   	public abstract String getSequence();
   	
   	boolean findSeq(CharacterData data, MesquiteTable table, MesquiteInteger charFound, MesquiteInteger length, MesquiteInteger taxonFound) {
 		String temp = sequence;
 		sequence = getSequence();
		if (sequence == null)
			return false;
		sequence = StringUtil.stripWhitespace(sequence);
		if (temp != null && sequence != null && temp.equals(sequence))
			return true;

		return  (findFirst(data, table, sequence, charFound, length, taxonFound));
   	}
	int firstChar = 0;
	boolean sequenceFound = false;
   	boolean findFirst(CharacterData data, MesquiteTable table, String sequence, MesquiteInteger charFound, MesquiteInteger length, MesquiteInteger taxonFound) {
			sequenceFound = false;
  			if (it < 0)
  				return false;
  			if (sequence == null)
  				return false;
  			if (length != null)
  				length.setValue(0);
  			firstChar = 0;
  			table.deselectAllCells(true, false);
			StringBuffer sb = new StringBuffer(data.getNumChars());
   			for (int ic = firstChar; ic< data.getNumChars(); ic++) {
   				String cell = table.getMatrixText(ic, it);
   				if (cell!= null && cell.length()>0)
   					sb.append(cell.charAt(0));
   			}
   			t3.start();
   			for (int ic = firstChar; ic< data.getNumChars(); ic++) {
   				t4.start();
   				int extra = sequencesMatch(data, sb,it, ic, sequence);
				t4.end();
				if (extra>=0) {
					if (charFound != null)
						charFound.setValue(ic);
					if (taxonFound != null)
						taxonFound.setValue(it);
			   		for (int i = 0; i<extra + sequence.length(); i++) {
			   			table.selectCell(ic + i, it);
			   			table.redrawCell(ic + i, it);
			   		}
					table.setFocusedSequence(ic, ic + extra + sequence.length(), it);
					
					if (length != null)
						length.setValue(extra + sequence.length());
					t3.end();
					sequenceFound = true;
					return true;
				}
			}
   			t3.end();
   			return false;
   		
   	}
   	MesquiteTimer t1 = new MesquiteTimer();
   	MesquiteTimer t2 = new MesquiteTimer();
   	MesquiteTimer t3 = new MesquiteTimer();
   	MesquiteTimer t4 = new MesquiteTimer();
   	
	/*.................................................................................................................*/
   	//-1 if not match, 0 or positive for number of extras if match
   	int sequencesMatch(CharacterData data, StringBuffer matrixSequence, int it, int checkChar, String sequence) {
   		int numChars = data.getNumChars();
   		int numTaxa = data.getNumTaxa();
   		int length = sequence.length();
   		if (it< 0 || it >= numTaxa)
   			return -1;
   		if (checkChar + (length)>=numChars){ //would extend past end of sequence; but can't go to next taxon
			return -1;
   		}
   		int mismatches = 0;
   		int extra = 0;
   		
   		for (int site= 0; site < length; site++){
   			String cell = null;
   			t1.start();
   			while (data.isInapplicable(site+checkChar+extra, it) && site+checkChar+extra<numChars)
   				extra++;
   			t1.end();
   			if (length+checkChar+extra>=numChars){//would extend past end of sequence
   				return -1;
   			}
   			//cell = table.getMatrixText(site+checkChar+extra, it);
 			
 			t2.start();
   			if (site+checkChar+extra>= matrixSequence.length() || !siteMatch(matrixSequence.charAt(site+checkChar+extra), sequence, site)) {
	 			t2.end();
   				mismatches++;
   				if (mismatches>0){
	   				firstChar++;
   					return -1;
   				}
   			}
	 		else
	 			t2.end();
   		}
	   	firstChar++;
   		return extra;
   		
   	}
	boolean siteMatch(char c, String sequence, int site){
		char s = sequence.charAt(site);
		if (c == s)
			return true;
		return matchchar(c, s) || matchchar(s,c);
	}
	boolean matchchar(char c, char s){
		
		if (s == 'a' && c == 'A')
			return true;
		if (s == 'c' && c == 'C')
			return true;
		if (s == 'g' && c == 'G')
			return true;
		if (s == 't' && c == 'T')
			return true;
		if (s == 'u' && c == 'U')
			return true;
		if (s == 'b' && c == 'B')
			return true;
		if (s == 'd' && c == 'D')
			return true;
		if (s == 'e' && c == 'E')
			return true;
		if (s == 'f' && c == 'F')
			return true;
		if (s == 'h' && c == 'H')
			return true;
		if (s == 'i' && c == 'I')
			return true;
		if (s == 'j' && c == 'J')
			return true;
		if (s == 'k' && c == 'K')
			return true;
		if (s == 'l' && c == 'L')
			return true;
		if (s == 'm' && c == 'M')
			return true;
		if (s == 'n' && c == 'N')
			return true;
		if (s == 'o' && c == 'O')
			return true;
		if (s == 'p' && c == 'P')
			return true;
		if (s == 'q' && c == 'Q')
			return true;
		if (s == 'r' && c == 'R')
			return true;
		if (s == 's' && c == 'C')
			return true;
		if (s == 'v' && c == 'V')
			return true;
		if (s == 'w' && c == 'W')
			return true;
		if (s == 'x' && c == 'X')
			return true;
		if (s == 'y' && c == 'Y')
			return true;
		if (s == 'z' && c == 'Z')
			return true;
		return false;
	}
	boolean playing = true;
	public void togglePlay(){
		playing = !playing;
	}
	public boolean isPlaying(){
		return playing;
	}
	public String getTaxonName(){
		if (data == null || it <0)
			return null;
		return data.getTaxa().getTaxonName(it);
	}
	public abstract String getMessage();
	
	protected int getTaxonNumber(){
		return it;
	}
	protected boolean getSequenceFound(){
		return sequenceFound;
	}
	public String getSearchSequence(){
		if (sequence == null || it < 0)
			return "";
		else
			return sequence;
	}
   	 
}

class SeqCheckThread  extends Thread {
	MaintainSequenceMatch ownerModule;
	boolean abort = false;
	Ledge text;
	public SeqCheckThread(MaintainSequenceMatch ownerModule, Ledge text){
		this.ownerModule = ownerModule;
		this.text = text;
	}
	public void run() {
		while (!abort) { 
			try {
				Thread.sleep(200);
				if (ownerModule.isPlaying()){
					ownerModule.checkSeq();
					if (text != null){
						text.setMessage(ownerModule.getMessage());
						text.setText(ownerModule.getSearchSequence());
					}
				}
				else {
					text.setMessage("Search is paused");
				}
			}
			catch (InterruptedException e){
			}
		}
	}
}

class Ledge extends MousePanel implements SeqLedge{
	TextField text;
	Label message;
	int labelWidth = 200;
	int messageLeng = 0;
	MaintainSequenceMatch ownerModule;
	Image goaway, play, pause, taxonButton;
	int controlWidth = 52;
	public Ledge(MaintainSequenceMatch ownerModule){
		setLayout(null);
		this.ownerModule = ownerModule;
		text = new TextField(500);
		add(text);
		text.setBounds(labelWidth+controlWidth,0, 10,10);
		text.setVisible(true);
		text.setEditable(true);
		text.setBackground(Color.white);
		message = new Label("", Label.RIGHT);
		message.setVisible(true);
		add(message);
		message.setBounds(controlWidth,1,labelWidth, 10);
		message.setBackground(ColorDistribution.paleGoldenRod);
		goaway = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "goaway.gif");
		play = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "play.gif");
		pause = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "pause.gif");
		taxonButton = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "taxon.gif");
	}
	public void setText(String s){
		if (!StringUtil.stringsEqual(s, text.getText()))
			text.setText(s);
	}
	public String getText(){
		return text.getText();
	}
	public void setMessage(String s){
		if (StringUtil.stringsEqual(s, message.getText()))
			return;
		messageLeng = 0;
		if (s != null)
			messageLeng = StringUtil.getStringDrawLength(message, s) + 10;
		resetSizes(getBounds().width, getBounds().height);
		message.setText(s);
		
	}
	void resetSizes(int w, int h){
		message.setBounds(controlWidth, 1, messageLeng, h-1);
		text.setBounds(messageLeng+controlWidth,0, w-messageLeng-controlWidth, h);
	}
	public void setSize(int w, int h){
		resetSizes(w, h);
		super.setSize(w,h);
	}
	public void setBounds(int x, int y, int w, int h){
		resetSizes(w, h);
		super.setBounds(x, y, w,h);
	}
	public void paint(Graphics g){
		g.setColor(ColorDistribution.paleGoldenRod); //paleGoldenRod);
		g.fillRect(0,0, getBounds().width, getBounds().height);

		g.setColor(Color.black);
		g.drawRect(0,0, controlWidth-1, getBounds().height-1);
	   	g.drawImage(goaway, 1,2, this);

	   	if (ownerModule.isPlaying())
	   		g.drawImage(pause, 17,2, this);
	   	else
	   		g.drawImage(play, 17,2, this);
	   		
	   	g.drawImage(taxonButton, 33,2, this);


		
	}
	/* to be used by subclasses to tell that panel touched */
   	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
   		if (x< 16)
			ownerModule.goAway();
   		else if (x< 32) {
			ownerModule.togglePlay();
			repaint();
		}
   		else if (x< 48)
			ownerModule.chooseTaxon();
		
	}
}


