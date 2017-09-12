package mesquite.lib;

import java.io.File;
import java.io.IOException;

public class ProcessUtil {
	
	public final static int NOERROR = 0;
	public final static int PERMISSIONDENIED=13;

	/*.................................................................................................................*/
	public static Process startProcess(String...command){
		try {
			if (command==null || command.length==0 || StringUtil.blank(command[0]))
				return null;
			ProcessBuilder pb = new ProcessBuilder(command);
			Process p = pb.start();
			return p;
		}
		catch (IOException e) {
		}
		return null;
	}

	/*.................................................................................................................*/
	public static Process startProcess(MesquiteInteger errorCode, String workingDirectoryPath, String outputFilePath, String errorFilePath, String...command){
		try {
			
			if (command==null || command.length==0 || StringUtil.blank(command[0])) {
				MesquiteMessage.printLogln("Error in attempting to start external program: commands empty. \n");
				
				return null;
			}
			ProcessBuilder pb = new ProcessBuilder(command);
			
		   if (StringUtil.notEmpty(workingDirectoryPath)) {
				pb.directory(new File(workingDirectoryPath));
		   }

			File errorLog=null;
			if (errorFilePath!=null) {
				errorLog = new File(errorFilePath);				
				pb.redirectError(ProcessBuilder.Redirect.appendTo(errorLog));
			}

			File log=null;
			if (outputFilePath!=null) {
				log = new File(outputFilePath);				
				pb.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
			}
			Process p = pb.start();
			if (log!=null)
				assert pb.redirectOutput().file() == log;
			if (errorLog!=null)
				assert pb.redirectError().file() == errorLog;
			if (errorCode!=null)
				errorCode.setValue(NOERROR);
		
			return p;
		}
		catch (IOException e) {
			MesquiteMessage.printLogln("IOException in attempting to start external program. \n");
			String message = e.getMessage();
			if (e.getMessage().indexOf("error=13")>0) {
				message+= "\n\nCheck to see if the external program is executable.";
				if (errorCode!=null)
					errorCode.setValue(PERMISSIONDENIED);
			}
			MesquiteMessage.discreetNotifyUser(message);
		}
		return null;
	}

}
