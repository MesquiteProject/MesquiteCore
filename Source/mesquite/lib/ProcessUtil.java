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
	public static void addEnvironmentVariableToProcessBuilder(ProcessBuilder processBuilder, String variable, String value) {
		processBuilder.environment().put(variable, value);
	}

	/*.................................................................................................................*/
	public static Process startProcess(MesquiteInteger errorCode, String workingDirectoryPath, String outputFilePath, String errorFilePath, String envVariableName, String envVariableValue, String...command){
		try {
			
			if (command==null || command.length==0 || StringUtil.blank(command[0])) {
				MesquiteMessage.printLogln("Error in attempting to start external program: commands empty. \n");
				MesquiteMessage.printStackTrace();
				return null;
			}
			ProcessBuilder pb = new ProcessBuilder(command);
			if (StringUtil.notEmpty(envVariableName) && StringUtil.notEmpty(envVariableValue))
				addEnvironmentVariableToProcessBuilder(pb, envVariableName,envVariableValue);
			
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
			String message = e.getMessage();
			MesquiteMessage.printLogln("IOException in attempting to start external program. \n" + message + "\n");
			if (e.getMessage().indexOf("error=13")>0) {
				message+= "\n\nCheck to see if the external program is executable.";
				if (errorCode!=null)
					errorCode.setValue(PERMISSIONDENIED);
			}
			MesquiteMessage.discreetNotifyUser(message);
		}
		return null;
	}

	/*.................................................................................................................*/
	public static Process startProcess(MesquiteInteger errorCode, String workingDirectoryPath, String outputFilePath, String errorFilePath, String...command){
		return startProcess(errorCode,  workingDirectoryPath,  outputFilePath,  errorFilePath, null, null, command);
	}

}
