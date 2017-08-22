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



/* ======================================================================== */
/**Represents a raw block of command strings from a NEXUS file.  Temporarily created in file reading.
 */
public class FileBlock {
	Vector commands;
	Vector commandComments;
	MesquiteFile f;
	long countRequested; //count of number of instances of target command
	int currentCommand;
	boolean directFromFile;
	boolean empty = false;
	String firstCommand = null;
	boolean readOnce = false;
	static int total = 0;
	long id;
	public FileBlock(){
		id = total++;
		commands = new Vector();
		commandComments = new Vector();
		directFromFile = false;
		reset();
	}
	
	//##########################
	MesquiteFile file;
	MesquiteString blockName;
	StringBuffer fileComments;
	StringBuffer blockComments;
	MesquiteInteger status;
	StringBuffer withinCommandComments;
	StringBuffer betweenCommandComments;

	public FileBlock(MesquiteFile file, MesquiteString blockName, StringBuffer fileComments, StringBuffer blockComments){
		this();
		this.file = file;
		directFromFile = true;
		this.blockName = blockName;
		this.fileComments = fileComments;
		this.blockComments = blockComments;
		if (blockComments!=null)
			blockComments.setLength(0);
		betweenCommandComments = new StringBuffer(10);
		status = new MesquiteInteger(0);
		withinCommandComments = new StringBuffer(10);
		reset();
		
	
		String command = file.getNextCommand(status, withinCommandComments);
		String wcc = withinCommandComments.toString();
		if (!StringUtil.blank(wcc) && wcc.length()>2){
			wcc = wcc.substring(1, wcc.length()-2);
			wcc = new Parser().getFirstToken(wcc);
			blockComments.append(wcc);
		}
		if (StringUtil.blank(command))
			empty = true;
		if (betweenCommandComments.length()>0 && fileComments!=null) {
			String bcc = betweenCommandComments.toString();
			if (ParseUtil.darkBeginsWithIgnoreCase(bcc, "!"))
				fileComments.append(bcc.substring(bcc.indexOf('!')+1, bcc.length())+ StringUtil.lineEnding());
		}
		Parser nameParser = new Parser();
		String bName =nameParser.getTokenNumber(command.toString(), 2); //resets string!
		blockName.setValue(bName);
		firstCommand = command;
	}
	
	public void setFile(MesquiteFile f){
		this.f = f;
	}
	public MesquiteFile getFile(){
		return f;
	}
	public void addCommand(String c, String comment){
		commands.addElement(c);
		if (comment ==null)
			commandComments.addElement(new String(""));
		else
			commandComments.addElement(comment);
	}
	public void reset(){
		currentCommand = 0;
	}
	public int getCurrentCommandNumber(){
		return currentCommand;
		
	}
	public String getNextFileCommand(MesquiteString comment){
		if (!directFromFile)
			return getNextFileCommandStored(comment);
		if (empty)
			return null;
		if (firstCommand!=null) {
			String q = firstCommand;
			firstCommand = null;
			return q;
		}
		withinCommandComments.setLength(0);
		betweenCommandComments.setLength(0);
		//##########################
		String command = file.getNextCommand(status, withinCommandComments, true);

		if (betweenCommandComments.length()>0 && fileComments!=null) {
			String bcc = betweenCommandComments.toString();
			if (ParseUtil.darkBeginsWithIgnoreCase(bcc, "!"))
				fileComments.append(bcc.substring(bcc.indexOf('!')+1, bcc.length())+ StringUtil.lineEnding());
		}
		//##########################
		currentCommand++;
		if (StringUtil.blank(command) || status.getValue() == 2) {
			readOnce = true;
			return null;
		}

		return command;
	}
	public boolean isEmpty(){
		if (directFromFile){
			return empty;
		}
		else
			return commands.size()==0;
	}
	public String getNextFileCommandStored(MesquiteString comment){
		if (currentCommand>=0 && currentCommand< commands.size()){
			if (comment !=null)
				comment.setValue((String)commandComments.elementAt(currentCommand));
			return (String)commands.elementAt(currentCommand++);
		}
		return null;
	}
	/*
	public String getCommand(int ic, MesquiteString comment){
		if (ic>=0 && ic< commands.size()){
			if (comment !=null)
				comment.setValue((String)commandComments.elementAt(ic));
			return (String)commands.elementAt(ic);
		}
		return null;
	}
	*/
	public int getNumCommands(){
		if (directFromFile)
			return MesquiteInteger.unassigned;
		return commands.size();
	}
	public String toString(){
		if (readOnce && directFromFile)
			MesquiteMessage.warnUser("FileBlock toString() used even though directFromFile and already read: " + blockName);
		StringBuffer sb = new StringBuffer(1000);
		MesquiteString comment = new MesquiteString();
		reset();
		String command = null;
		while ((command = getNextFileCommand(comment)) != null){
			sb.append(command + StringUtil.lineEnding());
		}
		readOnce = true;
		return sb.toString();
	}

}


