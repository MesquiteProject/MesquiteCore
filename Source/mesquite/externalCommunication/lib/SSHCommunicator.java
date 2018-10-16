package mesquite.externalCommunication.lib;

import mesquite.lib.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Vector;

import com.jcraft.jsch.*;


public abstract class SSHCommunicator extends RemoteCommunicator {
	
//	protected String remoteWorkingDirectoryPath = "";
	protected String remoteWorkingDirectoryName = "";
	protected String remoteServerDirectoryPath = "";
	public static final String runningFileName = "running";
	protected ProgressIndicator progressIndicator;


	public SSHCommunicator (MesquiteModule mb, String xmlPrefsString,String[] outputFilePaths) {
		if (xmlPrefsString != null)
			XMLUtil.readXMLPreferences(mb, this, xmlPrefsString);
		this.outputFilePaths = outputFilePaths;
		ownerModule = mb;
	}
	public void setProgressIndicator(ProgressIndicator progressIndicator) {
		this.progressIndicator= progressIndicator;
	}

	public Session createSession() {
		try {
			java.util.Properties config = new java.util.Properties(); 
			config.put("StrictHostKeyChecking", "no");
			JSch jsch = new JSch();
			Session session=jsch.getSession(username, host, 22);
			session.setPassword(password);
			session.setConfig(config);
			return session;
		} catch (Exception e) {
			return null;
		}
	}

	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getRemoteWorkingDirectoryName() {
		return remoteWorkingDirectoryName;
	}
	public void setRemoteWorkingDirectoryName(String workingDirectoryName) {
		this.remoteWorkingDirectoryName = workingDirectoryName;
	}
	public String getRemoteWorkingDirectoryPath() {
		return getRemoteServerDirectoryPath()+getRemoteWorkingDirectoryName();
	}
	/*public void setRemoteWorkingDirectoryPath(String workingDirectoryPath) {
		this.remoteWorkingDirectoryPath = workingDirectoryPath;
	}
*/
	public String getRemoteServerDirectoryPath() {
		return remoteServerDirectoryPath;
	}
	public void setRemoteServerDirectoryPath(String remoteServerDirectoryPath) {
		this.remoteServerDirectoryPath = remoteServerDirectoryPath;
	}


	public  String lastModified (String remoteFileName) {
		try {
			Session session=createSession();
			session.connect();
			
			ChannelSftp channel=(ChannelSftp)session.openChannel("sftp");
			channel.connect();
			channel.cd(getRemoteWorkingDirectoryPath());
			
			SftpATTRS sftpATTRS = channel.stat(remoteFileName);
			
			channel.disconnect();
			session.disconnect();
			return sftpATTRS.getMtimeString();
			
		}  catch (Exception e) {
			return "";
		}
	}
	public  boolean remoteFileExists (String remoteFileName) {
		try {
			Session session=createSession();
			session.connect();
			
			ChannelSftp channel=(ChannelSftp)session.openChannel("sftp");
			channel.connect();
			channel.cd(getRemoteWorkingDirectoryPath());
			
			SftpATTRS sftpATTRS = channel.stat(remoteFileName);
			
			channel.disconnect();
			session.disconnect();
			return !sftpATTRS.isDir() && !sftpATTRS.isLink();
			
		}  catch (Exception e) {
			return false;
		}
	}

	public  boolean remoteFileExists (ChannelSftp channel, String remoteFileName) {  // faster version if channel provided as doesn't need to establish a session
		try {
			if (channel==null)
				return false;
			 if (!channel.isConnected())
				channel.connect();
			
			channel.cd(getRemoteWorkingDirectoryPath());
			
			SftpATTRS sftpATTRS = channel.stat(remoteFileName);
			
			return !sftpATTRS.isDir() && !sftpATTRS.isLink();
			
		}  catch (Exception e) {
			return false;
		}
	}




	
	public  boolean sendCommands (String[] commands, boolean waitForRunning, boolean cdIntoWorking, boolean captureErrorStream) {
		if (commands==null || commands.length==0)
			return false;
		try{
			if (cdIntoWorking)
				commands = StringArray.addToStart(commands, "cd " + getRemoteWorkingDirectoryPath());
			Session session=createSession();
			session.connect();
			ChannelExec channel=(ChannelExec)session.openChannel("exec");
			String concatenated = "";
			for (int i=0; i<commands.length; i++)
				if (StringUtil.notEmpty(commands[i]))
					if (i==0)
						concatenated += commands[i];
					else
						concatenated += " && " + commands[i];

	        if (captureErrorStream) {
	        	String filename = getRemoteWorkingDirectoryPath() + "/errorStream.txt";
	        	File fstream = new File(filename);
	        	PrintStream errorStream = new PrintStream(new FileOutputStream(fstream));
	        	channel.setErrStream(errorStream);
	        }
			channel.setCommand(concatenated);
			InputStream in=channel.getInputStream();
			channel.connect();
			boolean success = false;

			byte[] tmp=new byte[1024];
			while(true){
				while(in.available()>0){
					int i=in.read(tmp, 0, 1024);
					if(i<0)break;
					ownerModule.logln(new String(tmp, 0, i));
				}

				if (channel.isClosed() && (!waitForRunning || !remoteFileExists(runningFileName))) {
					ownerModule.logln("exit-status: "+channel.getExitStatus());
					success=channel.getExitStatus()==0;
					break;
				} else if (channel.isClosed()) {
							ownerModule.logln("exit-status: "+channel.getExitStatus());
	
				}
				success=channel.getExitStatus()==0;
				monitorAndCleanUpShell(null,progressIndicator);
				
				try{Thread.sleep(1000);}catch(Exception ee){}
			}


			channel.disconnect();
			session.disconnect();
			return success;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}

	}
	
	public  boolean sendFilesToWorkingDirectory (String[] localFilePaths, String[] remoteFileNames) {
		if (localFilePaths==null || remoteFileNames==null)
			return false;
		try{
			Session session=createSession();
			if (session==null)
				return false;  // TODO: feedback
			session.connect();
			ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
			channel.connect();

			channel.cd(getRemoteWorkingDirectoryPath());
			for (int i=0; i<localFilePaths.length && i<remoteFileNames.length; i++)
				channel.put(localFilePaths[i], remoteFileNames[i]);

			channel.disconnect();
			session.disconnect();
			return true;
		} catch(Exception e){
			ownerModule.logln("Could not SFTP files to working directory: " + e.getMessage());
			e.printStackTrace();
			return false;
		}

	}
	


	public boolean createRemoteWorkingDirectory() {
		String[] commands = new String[] { "cd " + getRemoteServerDirectoryPath(), "mkdir " + getRemoteWorkingDirectoryName()};
		return sendCommands(commands,false, false, false);
	}
	public boolean transferFilesToServer(String[] localFilePaths, String[] remoteFileNames) {
		if (createRemoteWorkingDirectory()) {
			return sendFilesToWorkingDirectory (localFilePaths, remoteFileNames);
		}
		return false;
	}

	public  boolean jobCompleted (Object location) {
		return !remoteFileExists(runningFileName);
	}
	
	public String getJobStatus(Object location) {
		if (remoteFileExists(runningFileName)) 
			return submitted;
		return "Job completed or not found.";
	}
	
	public  boolean downloadFilesToLocalWorkingDirectory (boolean onlyNewOrModified) {
		try{
			Session session=createSession();
			if (session==null)
				return false;  // TODO: feedback
			session.connect();
			ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
			channel.connect();

			channel.cd(getRemoteWorkingDirectoryPath());
			Vector remoteFiles = channel.ls(getRemoteWorkingDirectoryPath());
			
			RemoteJobFile[] remoteJobFiles = new RemoteJobFile[remoteFiles.size()];  // now acquire the last modified dates
			for (int i=0; i<remoteFiles.size(); i++) {
				ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry)remoteFiles.elementAt(i);
				String fileName = entry.getFilename();
				if (remoteFileExists(channel,fileName)) {
					remoteJobFiles[i] = new RemoteJobFile();
					remoteJobFiles[i].setLastModified(lastModified(fileName));
					remoteJobFiles[i].setFileName(fileName);
				}
			}
			for (int i=0; i<remoteFiles.size(); i++) {
				ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry)remoteFiles.elementAt(i);
				String fileName = entry.getFilename();
				if (remoteFileExists(channel,fileName)) {
					if (!onlyNewOrModified || fileNewOrModified(previousRemoteJobFiles, remoteJobFiles, i))
						channel.get(fileName, rootDir+fileName);
				}
			}
			previousRemoteJobFiles = remoteJobFiles.clone();

			channel.disconnect();
			session.disconnect();
			return true;
		} catch(Exception e){
			ownerModule.logln("Could not SFTP files to working directory: " + e.getMessage());
			e.printStackTrace();
			return false;
		}

	}

	public boolean downloadResults(Object location, String rootDir, boolean onlyNewOrModified) {
		return downloadFilesToLocalWorkingDirectory(onlyNewOrModified);
	}
	
	/*.................................................................................................................*/
	public int getDefaultMinPollIntervalSeconds(){
		return 10;
	}

	/*.................................................................................................................*/
	public boolean downloadWorkingResults(Object location, String rootDir, boolean onlyNewOrModified) {
		if (checkUsernamePassword(false)) {
			return downloadFilesToLocalWorkingDirectory(onlyNewOrModified);
		}
		return false;
	}

	public void deleteJob(Object location) {
	}

	/*.................................................................................................................*/
	public String getServiceName() {
		return "SSH Server";
	}

	/*.................................................................................................................*/
	public String getSystemTypeName() {
		return "";
	}


}
