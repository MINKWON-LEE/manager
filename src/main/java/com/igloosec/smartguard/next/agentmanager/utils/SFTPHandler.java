/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.utils.SFTPHandler.java
 * company : Mobigen
 * @author : Je-Joong Lee
 * created at : 2016. 1. 8.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.utils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;
import com.igloosec.smartguard.next.agentmanager.entity.AgentInfo;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.sshtools.net.SocketTransport;
import com.sshtools.sftp.SftpClient;
import com.sshtools.sftp.SftpFile;
import com.sshtools.sftp.SftpStatusException;
import com.sshtools.sftp.TransferCancelledException;
import com.sshtools.ssh.ChannelOpenException;
import com.sshtools.ssh.PasswordAuthentication;
import com.sshtools.ssh.SshAuthentication;
import com.sshtools.ssh.SshClient;
import com.sshtools.ssh.SshConnector;
import com.sshtools.ssh.SshException;
import com.sshtools.ssh.SshTunnel;
import com.sshtools.ssh2.Ssh2Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketException;
import java.util.Vector;


public class SFTPHandler {

	private Logger logger = LoggerFactory.getLogger(getClass());
//	Logger agentCommlogger = LoggerFactory.getLogger("agentCommLog");
	private JSch jsch = null;
	private SshClient sshProxy = null;
	private SshClient clientProxy = null;
	private SftpClient sftpProxy = null;
	private SftpClient sftpSshTools = null;
	private Session session = null;
	private Channel channel = null;
	private ChannelSftp channelSftp = null;
	 
	
	private String strKnownHostsFileName = INMEMORYDB.KNOWN_HOSTS;
	private String server= "";
	private int port = 22;
	private String username = "";
	private String password = "";
	@SuppressWarnings("unused")
	private String localDir = "";
	@SuppressWarnings("unused")
	private String remoteDir = "";
	private AgentInfo agentInfo;
	
	 static final long RemoteFileCheckInterval = 1000; // 1 = 1 millisecond
	 	 
	 public SFTPHandler(){
	 }
	 
	 public SFTPHandler(AgentInfo agentInfo){
		this.server = agentInfo.getConnectIpAddress();
		this.port = agentInfo.getPortSftp();
		this.username = agentInfo.getUserIdOs();
		this.password = agentInfo.getPasswordOs();
		this.agentInfo = agentInfo;
	}
	 
	 public SFTPHandler(String rURL, int rPort, String username, String password){
		 this.server = rURL;
	     this.port = rPort;
	     this.username = username;
	     this.password = password;
	 }
	 
	 public void setServer(String server){
	  this.server = server;
	 }
	 
	 public void setPort(int port){
	  this.port = port;
	 }

	 public void setUser(String username , String password){
	  this.username = username;
	  this.password = password;
	 }

	 public void setRemoteDir(String remoteDir){
	  this.remoteDir = remoteDir;
	 }
	 
   	 public void setLocalDir(String localDir){
    	  this.localDir = localDir;
    	 }

   
   	public String login(){
		return login("yes", false);
	}

	public String login_SshTools(){
		return login_SshTools("yes");
	}

   	private int HOSTKEY_REJECTED_CNT=0;
   	private int HOSTKEY_UNKNOWN_CNT=0;
   	
   	/**
	 *    loginType; 
	 *    CR[0] : conID&RootID
	 *    RO[1] : RootID only
	 *    RSA[2]: use RSA 
	 *    LC[3] : Line Command
	 *    PEM[4]: use PEM //개발요건 없음
	 **/
	 public String login(String useStringHostChecking , boolean useKeyEx){
		 int loginType = this.agentInfo.getLoginTypeInt();
		 String errorMessage = null;
		 int sshStep = 0;

	  jsch = new JSch();
	  
	  try{
		java.util.Properties config = new java.util.Properties();
	    jsch.setKnownHosts(strKnownHostsFileName);
	   
	   sshStep = 1;	   
	   if(loginType == 2){
		   username = INMEMORYDB.RSAaccount;  //HardCoding? or read UI-input userID? 결정 
		   logger.debug("SFTP LOGIN WITH RSA KEY FILE." +username+"/" + server+"/"+ port);
			jsch.addIdentity(INMEMORYDB.RSAFilePath,INMEMORYDB.RSAFilePhrase);
//			jsch.addIdentity(INMEMORYDB.RSAFilePath);
			session = jsch.getSession(username, server, port);
			UserInfo ui = new MySFTPUserInfo();
			session.setUserInfo(ui);
			session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");	
		}else{
			session = jsch.getSession(username, server, port);
			session.setPassword(password);	
		}	   
	   sshStep = 2;
	  
	   
	   config.put("StrictHostKeyChecking", useStringHostChecking);
	   /*************
	    * 
	    * solaris sftp error 발생시 : Session.connect: java.security.InvalidAlgorithmParameterException: Prime size must be multiple of 64, and can only range from 512 to 2048
     	when error above occurs, try with following option.
     	cause: 1. the target OS return back key string such as 2047
     	 	   2. the target OS uses Too old algorithm that is deprecated in JVM 1.6 above     	 	    	
	    * 
	    *************/
	   /************************************************************	    	   
	   config.put("kex", "diffie-hellman-group1-sha1");
	    ********************************************************/
	   if(useKeyEx){
		   config.put("kex", "diffie-hellman-group1-sha1");   
	   }
	   
	   session.setConfig(config);
	   sshStep = 3;
	   session.connect();
	   sshStep = 4;
	  }catch(JSchException e) {
		  logger.error("SFTP LOGIN ERROR FOR ID>"+username + " TO>"+server+":"+port +" sftpStep>" + sshStep+"  ERRMSG="+e.getMessage());
		  if (sshStep == 3 && e.getMessage().contains("HostKey") && HOSTKEY_UNKNOWN_CNT < 1) {
				errorMessage = "ERROR UNKNOWNHOST";
				HOSTKEY_UNKNOWN_CNT++;
				addHost(server,session.getHostKey().getKey());
		  } else if (sshStep == 3 && e.getMessage().contains("HostKey") && HOSTKEY_UNKNOWN_CNT >= 1 && HOSTKEY_REJECTED_CNT < 1) {
			  errorMessage = "ERROR UNKNOWNHOST. CHANGED SSH ALGORITHM";
			  addHost2(server,session.getHostKey().getKey());
			  HOSTKEY_REJECTED_CNT++;
		  } else if (sshStep == 3 && e.getMessage().contains("reject") && e.getMessage().contains("HostKey") && HOSTKEY_REJECTED_CNT >= 1){
			  logger.info("HOST KEY REJECTED. TRY WITH no StrictHostKeyChecking");
				HOSTKEY_REJECTED_CNT++;
				errorMessage = login("no",false);
		  }
//		  else if(sshStep == 3 && e.getMessage().contains("InvalidAlgorithmParameterException") && ALGORITHM_RETRY_CNT < 2){
//			    ALGORITHM_RETRY_CNT++;
//			   if(ALGORITHM_RETRY_CNT <= 1){
//				   logger.info("InvalidAlgorithmParameterException RETRIAL.[HOSTKEYCHK YES,DH-USE TRUE]");
//				   errorMessage = login("yes",true);   
//			   }else{
//				   logger.info("InvalidAlgorithmParameterException RETRIAL.[HOSTKEYCHK NO,DH-USE TRUE]");
//				   errorMessage = login("no",true);
//			   }
//		  }
//		  else if (sshStep == 3 && e.getMessage().contains("HostKey") && HOSTKEY_UNKNOWN_CNT < 2) {
//				errorMessage = "ERROR UNKNOWNHOST";
//				HOSTKEY_UNKNOWN_CNT++;
//				addHost(server,session.getHostKey().getKey());
//			}
		  else if(e.getMessage().toUpperCase().contains("REFUSED")){
			  errorMessage = "[SFTP-REFUSED ERROR] WHILE LOGIN FOR : ["+username+"] "+port+"포트 로의 접근이 거절 되었습니다.방화벽 허용여부 또는 사용가능 포트인지 확인해 주세요. ERR_MSG:" +e.getMessage();
		  }
		  else if(e.getMessage().toUpperCase().contains("AUTH") ||
				  e.getMessage().toUpperCase().contains("PASSWORD") ||
				  e.getMessage().toUpperCase().contains("INCORRECT")
				  ){
			  errorMessage = "[SFTP-AUTH ERROR] WHILE LOGIN FOR : ["+username+"] ID/PW 를 확인해 주세요. ERR_MSG:" +e.getMessage();
		  }
		  else{
			  errorMessage = "[SFTP-ERROR] WHILE LOGIN FOR : ["+username+"] SSL 통신 실패. ERR_MSG:" +e.getMessage();
		  }
		  return errorMessage;

			}

	  try{
	   channel = session.openChannel("sftp");
	   channel.connect();
	   channelSftp = (ChannelSftp)channel;
			logger.info("SFTP LOGGED IN SUCCESSFULLY FOR ID>"+username + " TO>"+server+":"+port);

	  }catch(JSchException e) {
		  logger.error("SFTP OPEN CHANNEL ERROR WHILE LOGIN FOR ID>"+username + " TO>"+server+":"+port +"  ERRMSG="+e.getMessage());
		  
		  if(e.getMessage().toUpperCase().contains("INPUTSTREAM IS CLOSED")){
			  errorMessage = "[SFTP-STREAM ERROR] CHANNEL OPEN ERROR : SFTP 서비스가 원할하지 않습니다. 서버운영자에게 "+username +" 계정으로 SFTP 파일 송수신이 가능한지 문의 바랍니다. ERR_MSG:" +e.getMessage();  
		  }else if(e.getMessage().toUpperCase().contains("TOO LONG")){
			  errorMessage = "[SFTP-TOO LONG MESSAGE ERROR] CHANNEL OPEN ERROR : SFTP 접속 RETURN 값이 너무 큽니다. 서버운영자에게 "+username +" 계정으로 SFTP 파일 송수신이 가능한지 문의 바랍니다. ERR_MSG:" +e.getMessage();
		  }else{
			  errorMessage = "[SFTP-ERROR] CHANNEL OPEN ERROR FOR : ["+username+"] " +e.getMessage();  
		  }
		  
	  }
	  return errorMessage;

	 }
	 
	 
	 public static class MySFTPUserInfo implements UserInfo{
		 private Logger innerlogger = LoggerFactory.getLogger(getClass());
		@Override
		public String getPassphrase() {
			return INMEMORYDB.RSAFilePhrase;
		}

		@Override
		public String getPassword() {
			return null;
		}

		@Override
		public boolean promptPassword(String message) {
			return false;
		}

		@Override
		public boolean promptPassphrase(String message) {
			innerlogger.info("passPhrase:"+message);
			return true;
		}

		@Override
		public boolean promptYesNo(String message) {
			innerlogger.info("promptYN:"+message);
			return false;
		}

		@Override
		public void showMessage(String message) {
		}
		 
		 
	 }
	 

	public String login_SshTools(String useStringHostChecking){
		String errorMessage = null;
		int sshStep = 0;

		SshConnector con = null;
		try {
			con = SshConnector.createInstance();

			/**
			 * Connect to the host
			 */
			SocketTransport t = new SocketTransport(this.server, this.port);
			t.setTcpNoDelay(true);
			
			SshClient ssh = con.connect(t, username, true);

			Ssh2Client ssh2 = (Ssh2Client) ssh;
			sshStep = 1;
			/**
			 * Authenticate the user using password authentication
			 */
			PasswordAuthentication pwd = new PasswordAuthentication();

			do {
				pwd.setPassword(password);
			} while (ssh2.authenticate(pwd) != SshAuthentication.COMPLETE
					&& ssh.isConnected());
			sshStep = 2;
			if (ssh.isAuthenticated()) {
				sftpSshTools = new SftpClient(ssh2);
			}

		} catch (SshException e) {
			logger.error(CommonUtils.printError(e));
			errorMessage = e.getMessage();
		} catch (SftpStatusException e) {
			logger.error(CommonUtils.printError(e));
			errorMessage = e.getMessage();
		} catch (SocketException e) {
			logger.error(CommonUtils.printError(e));
			errorMessage = e.getMessage();
		} catch (IOException e) {
			logger.error(CommonUtils.printError(e));
			errorMessage = e.getMessage();
		} catch (ChannelOpenException e) {
			logger.error(CommonUtils.printError(e));
			errorMessage = e.getMessage();
		}
		return errorMessage;
	}

	public String login_Proxy(String relayIpAddr, String relayPort, String relayId, String relayPw)  {
		String errorMessage = null;
		String relayServer = relayIpAddr;
		int relayServerPort = Integer.parseInt(relayPort);
		String relayServerId = relayId;
		String relayServerPw = relayPw;

		try{
			/**
			 * Create an SshConnector instance
			 */
			SshConnector con = SshConnector.createInstance();

			/**
			 * Connect to the host
			 */
			sshProxy = con.connect(new SocketTransport(relayServer, relayServerPort), relayServerId, true);

			/**
			 * Authenticate the user using password authentication
			 */
			PasswordAuthentication pwd = new PasswordAuthentication();

			do {
				pwd.setPassword(relayServerPw);
			} while (sshProxy.authenticate(pwd) != SshAuthentication.COMPLETE
					&& sshProxy.isConnected());

			if (sshProxy.isAuthenticated()) {

				SshTunnel tunnel = sshProxy.openForwardingChannel(server, port,
						"127.0.0.1", 22, "127.0.0.1", 22, null, null);

				clientProxy = con.connect(tunnel, username);


				/**
				 * Authenticate the user using password authentication
				 */
				PasswordAuthentication pwd_target = new PasswordAuthentication();
				pwd_target.setUsername(username);
				pwd_target.setPassword(password);

				clientProxy.authenticate(pwd_target);

				sftpProxy = new SftpClient(clientProxy);
			}

		} catch(SshException e) {
			errorMessage = "[SFTP-ERROR] WHILE SshException FOR : "+username+" PLEASE CHECK ID/PW. " +e.getMessage();
			return errorMessage;
		} catch (IOException e) {
			errorMessage = "[SFTP-ERROR] WHILE IOException FOR : "+username+" PLEASE CHECK ID/PW. " +e.getMessage();
			return errorMessage;
		} catch (ChannelOpenException e) {
			errorMessage = "[SFTP-ERROR] WHILE ChannelOpenException FOR : "+username+" PLEASE CHECK ID/PW. " +e.getMessage();
			return errorMessage;
		} catch (SftpStatusException e) {
			errorMessage = "[SFTP-ERROR] WHILE SftpStatusException FOR : "+username+" PLEASE CHECK ID/PW. " +e.getMessage();

		}

		return errorMessage;

	 }
	 
	 private void addHost(String ip, String key){
		  logger.info("HOST : "+ ip + " IS ADDED.!!");
//		  agentCommlogger.info("HOST : "+ ip + " IS ADDED.!!");
		    try {
		        FileWriter tmpwriter=new FileWriter(strKnownHostsFileName,true);

		            tmpwriter.append(ip + " ssh-rsa " + key+"\n");
		            logger.info(ip + " ssh-rsa " + key);

		        tmpwriter.flush();
		        tmpwriter.close();

		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}

	private void addHost2(String ip, String key){
		logger.info("HOST : "+ ip + " IS ADDED AGAIN.!!");
		try {
			FileWriter tmpwriter=new FileWriter(strKnownHostsFileName,true);

			tmpwriter.append(ip + " ecdsa-sha2-nistp256 " + key+"\n");
			logger.info(ip + " ecdsa-sha2-nistp256 " + key);

			tmpwriter.flush();
			tmpwriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	 
	 public boolean logout(){
	  try{
	   channel.disconnect();
	   session.disconnect();
	   logger.info("SFTP Logged Out.!");
//	   agentCommlogger.info("SFTP Logged Out.!");
	   return true;
	  }catch(Exception e) {
		  this.channel = null;
		  this.session = null;
		  logger.error("SFTP LOGOUT ERROR = "+e.getMessage());
//		  agentCommlogger.error("SFTP LOGOUT ERROR = "+e.getMessage());
	            return false;
	        }
	 }

	public boolean logout_Proxy(){
		try{

			sftpProxy.quit();
			sshProxy.disconnect();
			logger.info("SFTP Logged Out.!");
//	   agentCommlogger.info("SFTP Logged Out.!");
			return true;
		}catch(Exception e) {
			this.channel = null;
			this.session = null;
			logger.error("SFTP LOGOUT ERROR = "+e.getMessage());
//		  agentCommlogger.error("SFTP LOGOUT ERROR = "+e.getMessage());
			return false;
		}
	}
	 
	 public boolean fileCheck(String serverDir, String fileName){
	  boolean result = false;
	  logger.debug("CHECK FILE EXISTS @ "+ serverDir + " for "+ fileName);
	  try{
		   @SuppressWarnings("unchecked")
           Vector<ChannelSftp.LsEntry> list = channelSftp.ls(serverDir);
		   for(ChannelSftp.LsEntry entry : list) {
			if(entry.getFilename().equals(fileName)){
			 result = true;
			 break;
			}
		   }
	   
	  }catch(SftpException e){
	   logger.error("Get file list ERROR = "+e.getMessage());
	  }

	  return result;
	 }

	public boolean fileCheck_Proxy(String serverDir, String fileName){
		boolean result = false;
		logger.debug("CHECK FILE EXISTS @ "+ serverDir + " for "+ fileName);
		try{
			@SuppressWarnings("unchecked")
			SftpFile[] list = sftpProxy.ls(serverDir);
			for(SftpFile entry : list) {
				if(entry.getFilename().equals(fileName)){
					result = true;
					break;
				}
			}

		}catch (SshException e) {
			logger.error("Get file list ERROR = "+e.getMessage());
		} catch (SftpStatusException e) {
			logger.error("Get file list ERROR = "+e.getMessage());
		}

		return result;
	}

	public boolean fileCheck_SshTools(String serverDir, String fileName){
		boolean result = false;
		logger.debug("CHECK FILE EXISTS @ "+ serverDir + " for "+ fileName);
		try{
			@SuppressWarnings("unchecked")
			SftpFile[] list = sftpSshTools.ls(serverDir);
			for(SftpFile entry : list) {
				if(entry.getFilename().equals(fileName)){
					result = true;
					break;
				}
			}

		}catch (SshException e) {
			logger.error("Get file list ERROR = "+e.getMessage());
		} catch (SftpStatusException e) {
			logger.error("Get file list ERROR = "+e.getMessage());
		}

		return result;
	}

	 public String getList(String serverDir){
	  StringBuffer sb = new StringBuffer();
	  try{
	   
	   @SuppressWarnings("unchecked")
       Vector<ChannelSftp.LsEntry> list = channelSftp.ls(serverDir);
	   for(ChannelSftp.LsEntry entry : list) {
	    
	    if(!entry.getFilename().equals(".") && !entry.getFilename().equals("..") && entry.getFilename().indexOf(".") != 0 ){
	     sb.append("FILE NAME = "+entry.getFilename()+"\t\n");
	    }
	   }
	   
	  }catch(SftpException e){
		  logger.error("Get file list ERROR = "+e.getMessage());
	  }
	  return sb.toString();
	 }
	 
	 public boolean changeDirectory(String serverDir){
	  boolean result = false;
	  try{
	   channelSftp.cd(serverDir);
	   result = true;
	  }catch(SftpException e){
		  logger.error("Change Directory ERROR = "+e.getMessage());
	  }
	  return result;
	 }
	 
	 public boolean downloadFile (String remoteFileWPath, String localFileWPath){
	  boolean result = false;
	  
	  try{
	   FileOutputStream out = new FileOutputStream(localFileWPath);
	   channelSftp.get(remoteFileWPath, out);
	   out.close();
	   logger.info(remoteFileWPath + " has been downloaded successfully.");
	   result = true;
	  }catch(IOException e){
		  logger.error("SFTP File Download IO ERROR = "+e.getMessage());
		  result = false;
	  }catch(SftpException e){
		  logger.error("SFTP File Download ERROR = "+e.getMessage());
		  result = false;
	  }finally{
		  logout();
	  }
	  
	  return result;
	 }
	 
	 public String uploadFile(String serverDir, File file){
	  
	  String startPath = "";
	  String currentPath = "";
	  FileInputStream in = null;
	  String fileName = "";
	  
	  try{
		  
	   in = new FileInputStream(file);

			try {
				fileName = file.getName();
				startPath = pwd();
				currentPath = startPath;
				serverDir = startPath+"/"+serverDir;
				channelSftp.cd(serverDir);
				channelSftp.chmod(Integer.parseInt("744",8), serverDir);
			} catch (SftpException e) {
		      String errMsg = e.getMessage();
				if (errMsg.toUpperCase().contains("NO SUCH FILE")) {
					channelSftp.mkdir(serverDir);
					logger.debug(serverDir +" NEWLY CREATED.!");			
					channelSftp.cd(serverDir);
					channelSftp.chmod(Integer.parseInt("744",8), serverDir);
				} else {
					throw new SftpException(e.id, errMsg);
				}
			}   
			
			if(fileCheck(pwd(),fileName)){
//				channelSftp.rm(fileName); 
				logger.debug("OLD "+fileName + " FILE REMOVED.");
			}
	   logger.debug("SENDING "+fileName+" FILE .....");
 
	   channelSftp.put(in, serverDir+"/"+fileName, 0);
	   logger.debug(fileName+ "SFTP FILE UPLOAD COMPLETED.!");

	   channelSftp.chmod(Integer.parseInt("744",8), fileName);
	   currentPath = pwd();

	   //MOVE BACK TO HOME DIR
		channelSftp.cd(startPath);

	  }catch(SftpException e){
		  if(e.getMessage().toUpperCase().contains("PERMISSION")){
		  logger.error("[SFTP-ERROR][CONN] UPLOAD ERROR. 사용자의 HOME 디렉터리에  쓰기/실행 권한 유효여부를 확인해 주세요. "+fileName+" WRITING TO PATH="+ currentPath +" "+ e.getMessage());
		  currentPath = "[SFTP-ERROR][CONN]UPLOAD ERROR. 사용자의 HOME 디렉터리에  쓰기/실행 권한 유효여부를 확인해 주세요. "+fileName+" WRITING TO PATH="+ currentPath +" "+ e.getMessage();
		  }
		  logger.error("[SFTP-ERROR][CONN] ERROR WHILE SFTP FILE UPLOAD. "+fileName+" AT " + currentPath +" "+ e.getMessage());
		  currentPath = "[SFTP-ERROR][CONN] ERROR WHILE SFTP FILE UPLOAD. " + e.getMessage();
	  }catch(FileNotFoundException e){
		  logger.error("[SFTP-ERROR][FILE] ERROR WHILE SFTP FILE UPLOAD (File Not Found) "+fileName+" AT " + currentPath +" "+ e.getMessage());
		  currentPath = "[SFTP-ERROR][FILE] ERROR WHILE SFTP FILE UPLOAD(File Not Found) " + e.getMessage();
	  }finally{
	   try{
	    in.close();
	    logger.info(fileName +" CLOSED FILE SENDING SESSION.");
	   }catch(IOException e){
		   logger.error("[SFTP-ERROR][IO] ERROR SFTP File Upload IO ERROR[CLOSING IN STREAM] "+fileName+" = "+e.getMessage());
	   }
	  }
	  
	  return currentPath;
	 }

	public String uploadFile_SshTools(String serverDir, File file){

		String startPath = "";
		String currentPath = "";
		FileInputStream in = null;
		String fileName = "";

		try{

			in = new FileInputStream(file);

			fileName = file.getName();
			startPath = pwd_SshTools();
			currentPath = startPath;
			serverDir = startPath+"/"+serverDir;
			try {
				sftpSshTools.cd(serverDir);
				sftpSshTools.chmod(Integer.parseInt("744",8), serverDir);
			}catch (SftpStatusException e){
				String errMsg = e.getMessage();
				if (errMsg.toUpperCase().contains("NO SUCH FILE")) {
					sftpSshTools.mkdir(serverDir);
					logger.debug(serverDir +" NEWLY CREATED.!");
					sftpSshTools.cd(serverDir);
					sftpSshTools.chmod(Integer.parseInt("744",8), serverDir);
				}
			}

			if(fileCheck_SshTools(pwd_SshTools(),fileName)){
				logger.debug("OLD "+fileName + " FILE Find..");
			}

			logger.debug("SENDING "+fileName+" FILE .....");
			sftpSshTools.put(in, serverDir+"/"+fileName, 0);
			logger.debug(fileName+ "SFTP FILE UPLOAD COMPLETED.!");
			sftpSshTools.chmod(Integer.parseInt("744",8), fileName);
			currentPath = pwd_SshTools();

			//MOVE BACK TO HOME DIR
			sftpSshTools.cd(startPath);

		}catch(SftpException e){
			logger.error("[SFTP-ERROR][CONN] ERROR WHILE SFTP FILE UPLOAD "+fileName+" AT " + currentPath +" "+ e.getMessage());
			currentPath = "[SFTP-ERROR][CONN] ERROR WHILE SFTP FILE UPLOAD " + e.getMessage();
//		  agentCommlogger.error("**ERROR WHILE SFTP FILE UPLOAD "+fileName+" AT " + currentPath + e.getMessage());
		}catch(FileNotFoundException e){
			logger.error("[SFTP-ERROR][FILE] ERROR WHILE SFTP FILE UPLOAD (File Not Found) "+fileName+" AT " + currentPath +" "+ e.getMessage());
			currentPath = "[SFTP-ERROR][FILE] ERROR WHILE SFTP FILE UPLOAD(File Not Found) " + e.getMessage();
//		  agentCommlogger.error("**ERROR WHILE SFTP FILE UPLOAD "+fileName+" AT " + currentPath + e.getMessage());
		} catch (SftpStatusException e) {
			logger.error("[SFTP-ERROR][CONN] ERROR WHILE SFTP FILE UPLOAD "+fileName+" AT " + currentPath +" "+ e.getMessage());
			currentPath = "[SFTP-ERROR][CONN] ERROR WHILE SFTP FILE UPLOAD " + e.getMessage();
		} catch (SshException e) {
			logger.error("[SFTP-ERROR][CONN] ERROR WHILE SFTP FILE UPLOAD "+fileName+" AT " + currentPath +" "+ e.getMessage());
			currentPath = "[SFTP-ERROR][CONN] ERROR WHILE SFTP FILE UPLOAD " + e.getMessage();
		} catch (TransferCancelledException e) {
			logger.error("[SFTP-ERROR][CONN] ERROR WHILE SFTP FILE UPLOAD "+fileName+" AT " + currentPath +" "+ e.getMessage());
			currentPath = "[SFTP-ERROR][CONN] ERROR WHILE SFTP FILE UPLOAD " + e.getMessage();
		} finally{
			try{
				in.close();
				logger.info(fileName +" CLOSED FILE SENDING SESSION.");
			}catch(IOException e){
				logger.error("[SFTP-ERROR][IO] ERROR SFTP File Upload IO ERROR[CLOSING IN STREAM] "+fileName+" = "+e.getMessage());
			}
		}

		return currentPath;
	}

	public String uploadFile_Proxy(String serverDir, File file){

	  String startPath = "";
	  String currentPath = "";
	  FileInputStream in = null;
	  String fileName = "";

	  try{

		  in = new FileInputStream(file);

		  fileName = file.getName();
		startPath = pwd_Proxy();
		currentPath = startPath;
		serverDir = startPath+"/"+serverDir;
		try {
			sftpProxy.cd(serverDir);
		}catch (SftpStatusException e){
			String errMsg = e.getMessage();
			if (errMsg.toUpperCase().contains("NO SUCH FILE")) {
				sftpProxy.mkdir(serverDir);
				logger.debug(serverDir +" NEWLY CREATED.!");
				sftpProxy.cd(serverDir);

			}
		}

		if(fileCheck_Proxy(pwd_Proxy(),fileName)){
			logger.debug("OLD "+fileName + " FILE Find..");
		}

	   logger.debug("SENDING "+fileName+" FILE .....");
	   sftpProxy.put(in, serverDir+"/"+fileName, 0);
	   logger.debug(fileName+ "SFTP FILE UPLOAD COMPLETED.!");
		  sftpProxy.chmod(Integer.parseInt("744",8), fileName);
	   currentPath = pwd_Proxy();

	   //MOVE BACK TO HOME DIR
		  sftpProxy.cd(startPath);

	  }catch(SftpException e){
		  logger.error("[SFTP-ERROR][CONN] ERROR WHILE SFTP FILE UPLOAD "+fileName+" AT " + currentPath +" "+ e.getMessage());
		  currentPath = "[SFTP-ERROR][CONN] ERROR WHILE SFTP FILE UPLOAD " + e.getMessage();
//		  agentCommlogger.error("**ERROR WHILE SFTP FILE UPLOAD "+fileName+" AT " + currentPath + e.getMessage());
	  }catch(FileNotFoundException e){
		  logger.error("[SFTP-ERROR][FILE] ERROR WHILE SFTP FILE UPLOAD (File Not Found) "+fileName+" AT " + currentPath +" "+ e.getMessage());
		  currentPath = "[SFTP-ERROR][FILE] ERROR WHILE SFTP FILE UPLOAD(File Not Found) " + e.getMessage();
//		  agentCommlogger.error("**ERROR WHILE SFTP FILE UPLOAD "+fileName+" AT " + currentPath + e.getMessage());
	  } catch (SftpStatusException e) {
		  logger.error("[SFTP-ERROR][CONN] ERROR WHILE SFTP FILE UPLOAD "+fileName+" AT " + currentPath +" "+ e.getMessage());
		  currentPath = "[SFTP-ERROR][CONN] ERROR WHILE SFTP FILE UPLOAD " + e.getMessage();
	  } catch (SshException e) {
		  logger.error("[SFTP-ERROR][CONN] ERROR WHILE SFTP FILE UPLOAD "+fileName+" AT " + currentPath +" "+ e.getMessage());
		  currentPath = "[SFTP-ERROR][CONN] ERROR WHILE SFTP FILE UPLOAD " + e.getMessage();
	  } catch (TransferCancelledException e) {
		  logger.error("[SFTP-ERROR][CONN] ERROR WHILE SFTP FILE UPLOAD "+fileName+" AT " + currentPath +" "+ e.getMessage());
		  currentPath = "[SFTP-ERROR][CONN] ERROR WHILE SFTP FILE UPLOAD " + e.getMessage();
	  } finally{
	   try{
	    in.close();
	    logger.info(fileName +" CLOSED FILE SENDING SESSION.");
	   }catch(IOException e){
		   logger.error("[SFTP-ERROR][IO] ERROR SFTP File Upload IO ERROR[CLOSING IN STREAM] "+fileName+" = "+e.getMessage());
	   }
	  }

	  return currentPath;
	 }
	 
	 public String pwd() throws SftpException{
		 return channelSftp.pwd();
	 }

	public String pwd_SshTools() throws SftpException{
		return sftpSshTools.pwd();
	}

	public String pwd_Proxy() throws SftpException{
		return sftpProxy.pwd();
	}
	 
	 public static void main(String args[]){
		 System.out.println(
				 "java -cp AgentManager.jar com.mobigen.snet.agentmanager.utils.SFTPHandler IP PORT ID PW FILENAME REMOTEDIR");
		


		 if(args != null && args.length >0){
			 
			 System.out.println("args[0] IP :"+args[0]);
			 System.out.println("args[1] PORT :"+args[1]);
			 System.out.println("args[2] ID:"+args[2]);
			 System.out.println("args[3] PW:"+args[3]);
			 System.out.println("args[4] FILENM:"+args[4]);
			 System.out.println("args[5] REMOTE DIR:"+args[5]);
		 }
		 SFTPHandler sftp = new SFTPHandler();
		 sftp.setServer(args[0]);
		 sftp.setUser(args[2], args[3]);
		 
		 
		 String errorMsg = sftp.login();
		 System.out.println("LOGIN MSG : "+errorMsg);
		 
		 sftp.uploadFile(args[5],new File(args[4]));
		 
	 }
	 
}
