package mesquite.io.lib;

import mesquite.lib.AlertDialog;
import mesquite.lib.MesquiteException;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteLong;
import mesquite.lib.MesquiteThread;
import mesquite.lib.ProgressIndicator;

public class FastaFileRecord {
	protected String sequenceName = null;
	protected int startPos = 0;
	protected int endPos = 0;
	protected String filePath = null;

	public FastaFileRecord(String sequenceName, int startPos, int endPos, String filePath) {
		this.sequenceName = sequenceName;
		this.startPos = startPos;
		this.endPos = endPos;
		this.filePath = filePath;
	}
	
	public String getSequenceName() {
		return sequenceName;
	}
	public void setSequenceName(String sequenceName) {
		this.sequenceName = sequenceName;
	}
	public int getStartPos() {
		return startPos;
	}
	public void setStartPos(int startPos) {
		this.startPos = startPos;
	}
	public int getEndPos() {
		return endPos;
	}
	public void setEndPos(int endPos) {
		this.endPos = endPos;
	}
	
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	public String getSequence() {
		return null;
	}

	/*.................................................................................................................*
	private boolean goToTreeBlock(MesquiteFile mNF){
		ProgressIndicator progIndicator = null;
		if (!quietOperation){
			progIndicator =  new ProgressIndicator(getProject(),"Processing File "+ mNF.getName() + " to find tree block", mNF.existingLength());
			progIndicator.start();
		}
		boolean found = false;
		if (mNF.openReading()) {
			try {
				//	long blockStart = 0;
				if (!quietOperation)
					logln("Processing File "+ mNF.getName() + " to find tree block");
				String token= mNF.firstToken(null);
				MesquiteLong startPos = new MesquiteLong();
				if (token!=null) {
					if (!token.equalsIgnoreCase("#NEXUS")) {
						discreetAlert("Not a valid NEXUS file (first token is \"" + token + "\"");
					}
					else {
						String name = null;
						//blockStart = -1;
						//blockStart = mNF.getFilePosition();
						while (!found && (name = mNF.goToNextBlockStart(startPos ))!=null) {
							if ("TREES".equalsIgnoreCase(name)){
								found = true;

								mNF.goToFilePosition(startPos.getValue()-1); //go back to start of trees block
							}
							else if ("TAXA".equalsIgnoreCase(name) || "DATA".equalsIgnoreCase(name)){
								if (progIndicator!=null)
									progIndicator.goAway();
								if (!MesquiteThread.isScripting() && !AlertDialog.query(containerOfModule(), "Use File?",  "Tree file contains a TAXA or DATA block.  " + 
										"If the TREES block has no translation table, and if the order of taxa is different in this file than in your current project in Mesquite, " +
										"then the trees may be misread.  Do you want to open this file anyway?"))
								return false;
							}
							//else
							//	blockStart = mNF.getFilePosition()+1;
						}
						if (progIndicator!=null)
							progIndicator.goAway();
						if (found && !quietOperation)
							logln("Tree block found");
					}
				}
			}
			catch (MesquiteException e){
				if (progIndicator!=null)
					progIndicator.goAway();
				return false;
			}
		}
		else {
			if (progIndicator!=null)
				progIndicator.goAway();
			return false;
		}
		if (progIndicator!=null)
			progIndicator.goAway();
		return found;
	}
	/*.................................................................................................................*/



}
