package mesquite.lib;

public interface OutputFileProcessor {
	
	public void processOutputFile(String[] outputFilePaths, int fileNum);
	
	public void processCompletedOutputFiles(String[] outputFilePaths);

}
