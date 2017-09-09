package mesquite.lib;

import java.io.File;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;

public class OutputFileTailer {
	File fileToTail;
	StringBuffer fileContents;

	OutputFileTailer(File fileToTail) {
		this.fileToTail = fileToTail;
		fileContents = new StringBuffer();
	}
	
	public void start ()  {
		OutputFileListener listener = new OutputFileListener(this);
		Tailer tailer = Tailer.create(fileToTail, listener, 500);
		Debugg.println("^^^^^^^^^^  Starting StandardOutputsStreamReader");
	}

	public void appendToFileContents(String s) {
		fileContents.append(s);
	}
	public String getFileContents() {
		return fileContents.toString();
	}
}

class OutputFileListener extends TailerListenerAdapter {
	OutputFileTailer tailer;


	OutputFileListener(OutputFileTailer tailer) {
		this.tailer = tailer;
	}

	public void handle(String line) {
			if (tailer != null) {
				tailer.appendToFileContents(line+StringUtil.lineEnding());
				Debugg.println("||| " + line);
			}
	}
}

