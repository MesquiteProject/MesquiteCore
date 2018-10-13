package mesquite.externalCommunication.lib;

import mesquite.lib.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;


import com.jcraft.jsch.*;



public abstract class SSHCommunicator extends RemoteCommunicator {
	
	protected String remoteWorkingDirectoryPath = "";


	public SSHCommunicator (MesquiteModule mb, String xmlPrefsString,String[] outputFilePaths) {
		if (xmlPrefsString != null)
			XMLUtil.readXMLPreferences(mb, this, xmlPrefsString);
		this.outputFilePaths = outputFilePaths;
		ownerModule = mb;
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
	public String getRemoteWorkingDirectoryPath() {
		return remoteWorkingDirectoryPath;
	}

	public void setRemoteWorkingDirectoryPath(String workingDirectoryPath) {
		this.remoteWorkingDirectoryPath = workingDirectoryPath;
	}


	public  boolean remoteFileExists (String remoteFileName) {
		try {
			Session session=createSession();
			session.connect();
			
			ChannelSftp channel=(ChannelSftp)session.openChannel("sftp");
			channel.connect();
			channel.cd(remoteWorkingDirectoryPath);
			
			SftpATTRS sftpATTRS = channel.stat(remoteFileName);
			
			channel.disconnect();
			session.disconnect();
			return true;
			
		}  catch (Exception e) {
			return false;
		}
	}


	
	public  void sendSSHCommands (String[] commands, boolean waitForRunning) {
		if (commands==null || commands.length==0)
			return;
		try{
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

		     String filename = remoteWorkingDirectoryPath + "/errorStream.txt";
		     File fstream = new File(filename);
	         PrintStream errorStream = new PrintStream(new FileOutputStream(fstream));
		    channel.setErrStream(errorStream);
			channel.setCommand(concatenated);
			InputStream in=channel.getInputStream();
			channel.connect();

			byte[] tmp=new byte[1024];
			while(true){
				while(in.available()>0){
					int i=in.read(tmp, 0, 1024);
					if(i<0)break;
					ownerModule.logln(new String(tmp, 0, i));
				}

				if (channel.isClosed() && waitForRunning && !remoteFileExists("running")) {
					ownerModule.logln("exit-status: "+channel.getExitStatus());
					break;
				} else if (channel.isClosed())
						ownerModule.logln("exit-status: "+channel.getExitStatus());

				try{Thread.sleep(1000);}catch(Exception ee){}
			}


			channel.disconnect();
			session.disconnect();
		}catch(Exception e){
			e.printStackTrace();
		}

	}
	


	/*.................................................................................................................*/
	public String getSystemTypeName() {
		return "";
	}


}
