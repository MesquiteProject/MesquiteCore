package mesquite.lib;

public interface LogFileProcessor {
	
	public void processLogFile(String logFilePath);
	
	public void processCompletedLogFile(String logFilePath);

}
