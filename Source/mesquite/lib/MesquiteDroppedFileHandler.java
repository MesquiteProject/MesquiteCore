package mesquite.lib;

import mesquite.lib.duties.FileInterpreter;

public interface MesquiteDroppedFileHandler {
	
	public FileInterpreter findFileInterpreter(String droppedContents, String fileName);
	
	public void actUponDroppedFileContents(FileInterpreter fileInterpreter, String droppedContents);


}
