/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.utils.SSHHandlerUtils.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 3. 22.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.utils;

import com.igloosec.smartguard.next.agentmanager.entity.AgentInfo;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.jcraft.jsch.*;
import com.sshtools.net.SocketTransport;
import com.sshtools.publickey.SshPrivateKeyFile;
import com.sshtools.publickey.SshPrivateKeyFileFactory;
import com.sshtools.ssh.PasswordAuthentication;
import com.sshtools.ssh.PseudoTerminalModes;
import com.sshtools.ssh.PublicKeyAuthentication;
import com.sshtools.ssh.SshAuthentication;
import com.sshtools.ssh.SshClient;
import com.sshtools.ssh.SshConnector;
import com.sshtools.ssh.SshSession;
import com.sshtools.ssh.SshTunnel;
import com.sshtools.ssh.components.SshKeyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SSHCommon {

	JSch jsch;
	Session session;
	public SshSession session_sshtools = null;
	SshSession forwardedSession = null;
	SshClient ssh = null;
	SshClient forwardedConnection = null;
	PtyAnalyzer ptyAnalyzer;
    ChannelExec channel;
//	ChannelShell channel;
	String strKnownHostsFileName;
	InputStream inStream = null;
	InputStream errStream = null;
	OutputStream outStream = null;

	
	String server = "";
	int port=22;
	String username;
	String password;
	String rootId;
	String rootPw;
		
	String rootPrompt = "";
	String connPrompt = "";
		
	int intTimeOut;
	String ptyReturnKey[] = {"PTY","STR","INT"};
	static int readTimeout = 10*1000;
	static int readPtyTimeout = 4000; // wait 4 sec
    boolean isPasswordStep = false;
    private SocketStreamReader socketStreamReader;

	public boolean isSshToolsCon = false;

	public Logger logger = LoggerFactory.getLogger(getClass());

	public SSHCommon(AgentInfo agentInfo){
		createInstance(agentInfo.getUserIdOs(), agentInfo.getPasswordOs(), agentInfo.getConnectIpAddress(), INMEMORYDB.KNOWN_HOSTS);
		this.port = agentInfo.getPortSsh();
		this.intTimeOut = 60000;
	}
	
	public SSHCommon(AgentInfo agentInfo, int loginType){
//		loginType; 
		if(loginType == 0 || loginType == 2 || loginType == 3){
			createInstance(agentInfo.getUserIdOs(), agentInfo.getPasswordOs(), agentInfo.getConnectIpAddress(), INMEMORYDB.KNOWN_HOSTS);	
		}else if(loginType == 1){
			createInstance(agentInfo.getUserIdRoot(), agentInfo.getPasswordRoot(), agentInfo.getConnectIpAddress(), INMEMORYDB.KNOWN_HOSTS);	
		}
		this.port = agentInfo.getPortSsh();
		this.intTimeOut = 60000;
	}

	public SSHCommon(AgentInfo agentInfo, String sshTools) throws Exception {
		this.port = agentInfo.getPortSsh();
		this.intTimeOut = 60000;
		if(agentInfo.getLoginTypeInt() ==2){
			isSshToolsCon = createInstance_sshTools_RSA(agentInfo.getUserIdOs(), agentInfo.getPasswordOs(), agentInfo.getConnectIpAddress(), INMEMORYDB.KNOWN_HOSTS);	
		}else{
			isSshToolsCon = createInstance_sshTools(agentInfo.getUserIdOs(), agentInfo.getPasswordOs(), agentInfo.getConnectIpAddress(), INMEMORYDB.KNOWN_HOSTS);	
		}
			
		
		
	}

	public SSHCommon(AgentInfo agentInfo, AgentInfo relayInfo, String sshTools, String proxy) throws Exception {
		this.port = agentInfo.getPortSsh();
		this.intTimeOut = 120000;
		isSshToolsCon = createInstance_sshToolsProxy(agentInfo.getUserIdOs(), agentInfo.getPasswordOs(), agentInfo.getConnectIpAddress(), agentInfo.getRelayIpAddress(), relayInfo);
	}

	public SSHCommon(String id, String pass, String ip) {
		createInstance(id, pass, ip, INMEMORYDB.KNOWN_HOSTS);
		this.port = 22;
		this.intTimeOut = 60000;
	}
	
	public SSHCommon(String id, String pass, String ip, int port) {
		createInstance(id, pass, ip,INMEMORYDB.KNOWN_HOSTS);
		this.port = port;
		this.intTimeOut = 60000;
	}
	
	public SSHCommon(String id, String pass, String ip, String knownHostsFN) {
		createInstance(id, pass, ip, knownHostsFN);
		this.port = 22;
		this.intTimeOut = 60000;
	}
	
	public SSHCommon(String id, String pass, String ip, int port, String knownHostsFN) throws Exception {
		this.port = port;
		this.intTimeOut = 60000;
		createInstance_sshTools(id, pass, ip, knownHostsFN);
//		createInstance_sshToolsProxy(id, pass, ip, knownHostsFN);
	}

	public SSHCommon(String id, String pass, String ip, int port, String knownHostsFN, String nwSsh) throws Exception {
		this.port = port;
		this.intTimeOut = 60000;
		createInstance_sshTools(id, pass, ip, knownHostsFN,nwSsh);
	}

	public SSHCommon(String id, String pass, String ip, int port, String knownHostsFN, int timeOut) {
		createInstance(id, pass, ip, knownHostsFN);
		this.port = port;
		this.intTimeOut = timeOut;
	}

	private void createInstance(String id, String pass,
                                String ip, String knownHostsFN) {
		ptyAnalyzer = new PtyAnalyzer();
		this.socketStreamReader = new SocketStreamReader();
		jsch = new JSch();
		strKnownHostsFileName = knownHostsFN;

		try {
			jsch.setKnownHosts(knownHostsFN);
		} catch (JSchException jschX) {
			logger.error(jschX.getMessage());
		}

		this.username = id;
		this.password = pass;
		this.server = ip;
	}

	private boolean createInstance_sshTools(String id, String pass, String ip, String knownHostsFN) throws Exception {

		this.username = id;
		this.password = pass;
		this.server = ip;

		this.socketStreamReader = new SocketStreamReader();		
		return openSessionAndChannel_sshTools();
	}

	private boolean createInstance_sshTools(String id, String pass, String ip, String knownHostsFN, String nwSsh) throws Exception {

		this.username = id;
		this.password = pass;
		this.server = ip;

		this.socketStreamReader = new SocketStreamReader();
		return openSessionAndChannel_sshTools(nwSsh);
	}
	
	private boolean createInstance_sshTools_RSA(String id, String pass, String ip, String knownHostsFN) throws Exception {

		this.username = id;
		this.password = pass;
		this.server = ip;

		this.socketStreamReader = new SocketStreamReader();		
		return openSessionAndChannel_sshTools_RSA();
		
		
	}
	
	

	private boolean createInstance_sshToolsProxy(String id, String pass, String ip, String relayServer, AgentInfo relayInfo) throws Exception {

		this.username = id;
		this.password = pass;
		this.server = ip;



		this.socketStreamReader = new SocketStreamReader();
		return openSessionAndChannel_sshToolsProxy(relayInfo);
	}
	
	
	/**
	 * loginType; 
	 *    CR[0] : conID&RootID
	 *    RO[1] : RootID only
	 *    RSA[2]: use RSA 
	 *    LC[3] : Line Command
	 *    PEM[4]: use PEM //개발요건 없음
	 **/
 	public String connect(){
   		return connect("yes" ,0,false);
   	}
 	
	public String connect(int loginTypeInt){
   		return connect("yes" ,loginTypeInt,false);
   	}

	
	private int HOSTKEY_REJECTED_CNT=0;
   	private int HOSTKEY_UNKNOWN_CNT=0;
   	private int ALGORITHM_RETRY_CNT=0;
   	
   	
    public static class MySSHUserInfo implements UserInfo{
		 private Logger innerlogger = LoggerFactory.getLogger(getClass());
		@Override
		public String getPassphrase() {
			// TODO Auto-generated method stub
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
   	
	public String connect(String useStringHostChecking, int loginType, boolean useKeyEx) {
		String errorMessage = null;
		int sshStep = 0;
		
		try {
		
			Properties prop = new Properties();
			jsch = new JSch();
			jsch.setKnownHosts(strKnownHostsFileName);

			sshStep = 1;
			
			if(loginType == 2){
				username = INMEMORYDB.RSAaccount; //HardCoding? or read UI-input userID? 결정 
				jsch.addIdentity(INMEMORYDB.RSAFilePath,INMEMORYDB.RSAFilePhrase);
				session = jsch.getSession(username,server, port);
				UserInfo ui = new MySSHUserInfo();
				session.setUserInfo(ui);
				this.session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
				logger.debug("RSA AUTH TYPE HAS BEEN SET.!!!!! ");
			}else{
				session = jsch.getSession(username,server, port);
				this.session.setPassword(password);	
			}
			
			sshStep = 2;
			prop.put("StrictHostKeyChecking", useStringHostChecking);
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
				prop.put("kex", "diffie-hellman-group1-sha1");   
			   }
			
			this.session.setConfig(prop);
			
			sshStep = 3;
			
			logger.info("SSH CONNECTING FOR ID/pw>"+username + " TO>"+server+":"+port +" useHostCheck:"+useStringHostChecking +" AUTH TYPE:"+loginType);
			this.session.connect(intTimeOut);
			sshStep = 4;
			this.channel = (ChannelExec)session.openChannel("exec");
//			this.channel = (ChannelShell)session.openChannel("shell");	
		
			logger.info("SSH CONNECT SUCCESSFULLY FOR ID>"+username + " TO>"+server+":"+port);
			
			
		} catch (JSchException jschX) {
			 logger.error("SSH CONNECT ERROR FOR ID>"+username + " TO>"+server+":"+port +" sshStep>" + sshStep+"  ERRMSG="+jschX.getMessage());

			 if(sshStep == 3 && jschX.getMessage().toLowerCase().contains("reject") && jschX.getMessage().toLowerCase().contains("hostkey") && HOSTKEY_REJECTED_CNT < 2){
				  logger.info("HOST KEY REJECTED. TRY WITH no StrictHostKeyChecking : "+jschX.getMessage());
				  HOSTKEY_REJECTED_CNT++;
				  errorMessage = connect("no",loginType,false);
			  }
//			 else if(sshStep == 3 && jschX.getMessage().contains("InvalidAlgorithmParameterException") && ALGORITHM_RETRY_CNT < 2){
//				    ALGORITHM_RETRY_CNT++;
//				   if(ALGORITHM_RETRY_CNT <= 1){
//					   logger.info("InvalidAlgorithmParameterException RETRIAL.[HOSTKEYCHK YES,DH-USE TRUE]");
//					   errorMessage = connect("yes",loginType,true);   
//				   }else{
//					   logger.info("InvalidAlgorithmParameterException RETRIAL.[HOSTKEYCHK NO,DH-USE TRUE]");
//					   errorMessage = connect("no",loginType,true);
//				   }
//			  }
			  else if (sshStep == 3 && jschX.getMessage().contains("HostKey")&& HOSTKEY_UNKNOWN_CNT < 2) {
				errorMessage = "ERROR UNKNOWNHOST";
				HOSTKEY_UNKNOWN_CNT++;
				addHost(server, session.getHostKey().getKey());				
			}else{
				 errorMessage = "ERROR WHILE SSH CONNECT : "+ jschX.getMessage();
			}			
			
		}
		
		//Error Message Decoration
		if(errorMessage != null && !errorMessage.contains("[CONNECTION_FAILURE]")){
			errorMessage = "[CONNECTION_FAILURE] FOR CONNECTION_ID:"+username + "CONNECTION_IP:"+server+":"+port +" ERROR MESSAGE:"+ errorMessage;
		}
		
		return errorMessage;
	}
	
	
	
	
 	
 	
	/******************************************************************************************************************
	public String connect_bak(String useStringHostChecking) {
		String errorMessage = null;
		int sshStep = 0;

		try {
		
			Properties prop = new Properties();
		
			this.session = jsch.getSession(username,server, port);
			sshStep = 1;			
			this.session.setPassword(password);
			sshStep = 2;
			prop.put("StrictHostKeyChecking", useStringHostChecking);			
//			prop.put("PreferredAuthentications","password,gssapi-with-mic,publickey,keyboard-interactive");
			this.session.setConfig(prop);
			
			sshStep = 3;
			
			logger.info("SSH CONNECTING FOR ID/pw>"+username + " TO>"+server+":"+port +" useHostCheck:"+useStringHostChecking);
			this.session.connect(intTimeOut);
			sshStep = 4;
			this.channel = (ChannelExec)session.openChannel("exec");
//			this.channel = (ChannelShell)session.openChannel("shell");	
		
			logger.info("SSH CONNECT SUCCESSFULLY FOR ID>"+username + " TO>"+server+":"+port);
			
			
		} catch (JSchException jschX) {
			 logger.error("SSH CONNECT ERROR FOR ID>"+username + " TO>"+server+":"+port +" sshStep>" + sshStep+"  ERRMSG="+jschX.getMessage());
			 
			 if(sshStep == 3 && jschX.getMessage().toLowerCase().contains("reject") && jschX.getMessage().toLowerCase().contains("hostkey") && HOSTKEY_REJECTED_CNT < 2){
				  logger.info("HOST KEY REJECTED. TRY WITH no StrictHostKeyChecking : "+jschX.getMessage());
				  HOSTKEY_REJECTED_CNT++;
				  errorMessage = connect("no");
			  }
			  else if (sshStep == 3 && jschX.getMessage().contains("HostKey")&& HOSTKEY_UNKNOWN_CNT < 2) {
				errorMessage = "ERROR UNKNOWNHOST";
				HOSTKEY_UNKNOWN_CNT++;
				addHost(server, session.getHostKey().getKey());				
			}else{
				 errorMessage = "ERROR WHILE SSH CONNECT : "+ jschX.getMessage();
			}			
			
		}
		
		//Error Message Decoration
		if(errorMessage != null && !errorMessage.contains("[CONNECTION_FAILURE]")){
			errorMessage = "[CONNECTION_FAILURE] FOR CONNECTION_ID:"+username + "CONNECTION_IP:"+server+":"+port +" ERROR MESSAGE:"+ errorMessage;
		}
		
		return errorMessage;
	}
	******************************************************************************************************************/
	

	public void connectChannel(String runCmd) throws IOException, JSchException{

		try{
			this.inStream = this.channel.getInputStream();
			this.errStream = this.channel.getErrStream();
			this.outStream  = this.channel.getOutputStream();
			this.channel.setCommand(runCmd); 
	    	this.channel.setPty(true);
			this.channel.connect(60000);
			logger.debug("SENT@CONNECT_CHANNEL : "+runCmd);	
		}catch(IOException | JSchException e){
			e.getStackTrace();
		}
		
		
	}
	
	public void connectChannel_sshTools(String runCmd) throws IOException, JSchException{

		try{
			session_sshtools.getOutputStream().write((runCmd+"\n").getBytes() );
			session_sshtools.getOutputStream().flush();
			logger.debug("SENT CMD WITH SSHTOOLS : "+runCmd);
		}catch(IOException e){
			e.getStackTrace();
		}
	}

	public void connectChannel_sshToolsProxy(String runCmd) throws IOException, JSchException{

		try{
			forwardedSession.getOutputStream().write((runCmd+"\n").getBytes() );
			forwardedSession.getOutputStream().flush();
			logger.debug("SENT CMD WITH SSHTOOLS : "+runCmd);
		}catch(IOException e){
			e.getStackTrace();
		}
	}

	void addHost(String ip, String key) {
		logger.info("HOST : " + ip + " is added.!!");
		try {
			FileWriter tmpwriter = new FileWriter(strKnownHostsFileName, true);

			tmpwriter.append(ip + " ssh-rsa " + key + "\n");
			logger.info(ip + " ssh-rsa " + key);

			tmpwriter.flush();
			tmpwriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String exeCommand(String runCmd, String mode) throws Exception {
		String rslt = "";
    	//SEND COMMAND
    	logger.debug("SEND SSH COMMAND "+ runCmd);
    	openSessionAndChannel();
    	if(this.channel.isConnected()){
    		logger.debug("channel connected. DO via EXISTING Stream!");
    		this.outStream.write((runCmd+"\n").getBytes() );
    		this.outStream.flush();
    		@SuppressWarnings("unchecked")
            ArrayList<String> resultByline = (ArrayList<String>) readPtyIs(mode).get(INMEMORYDB.ptyKey.ARRSTR.toString());
    		rslt = this.socketStreamReader.trimExeResult(resultByline, runCmd, "");
    	}else{
    		logger.debug("channel NOT connected. DO via NEW Channel Connect!");    		
    		connectChannel(runCmd+"\n");
    		rslt = readIs();  
    	}    		
    	  	
    	return rslt;
    }
	
	public HashMap<String, Object> exeCommand2(String runCmd, String mode) throws Exception {
		String rslt = "";
    	//SEND COMMAND
    	logger.debug("SEND SSH COMMAND "+ runCmd);
    	openSessionAndChannel();
    	if(this.channel.isConnected()){
    		logger.debug("channel connected. DO via EXISTING Stream!");
    		this.outStream.write((runCmd+"\n").getBytes() );
    		this.outStream.flush();    		
			return readPtyIs(mode);
    
    	}else{
    		logger.debug("channel NOT connected. DO via NEW Channel Connect!");    		
    		connectChannel(runCmd+"\n");
    		return readPtyIs(mode);
    	}
    }

	public String exeCommand_sshTools(String runCmd, String mode) throws Exception {
		String rslt = "";
		//SEND COMMAND
		logger.debug("SEND SSH COMMAND "+ runCmd);

		if (!session_sshtools.isClosed()) {
			logger.debug("channel connected. DO via EXISTING Stream!");
			session_sshtools.getOutputStream().write((runCmd+"\n").getBytes() );
			session_sshtools.getOutputStream().flush();
			@SuppressWarnings("unchecked")
            ArrayList<String> resultByline = (ArrayList<String>) readPtyIs_sshTools(mode).get(INMEMORYDB.ptyKey.ARRSTR.toString());
			rslt = this.socketStreamReader.trimExeResult(resultByline, runCmd, "");
		}else{
			logger.debug("channel NOT connected. DO via NEW Channel Connect!");
			connectChannel(runCmd+"\n");
			rslt = readIs();
		}

		return rslt;
	}

	public String exeCommand_sshToolsProxy(String runCmd, String mode) throws Exception {
		String rslt = "";
		//SEND COMMAND
		logger.debug("SEND SSH COMMAND "+ runCmd);

		if (!forwardedSession.isClosed()) {
			logger.debug("channel connected. DO via EXISTING Stream!");
			forwardedSession.getOutputStream().write((runCmd+"\n").getBytes() );
			forwardedSession.getOutputStream().flush();
			@SuppressWarnings("unchecked")
            ArrayList<String> resultByline = (ArrayList<String>) readPtyIs_sshToolsProxy(mode).get(INMEMORYDB.ptyKey.ARRSTR.toString());
			rslt = this.socketStreamReader.trimExeResult(resultByline, runCmd, "");
		}else{
			logger.debug("channel NOT connected. DO via NEW Channel Connect!");
			connectChannel(runCmd+"\n");
			rslt = readIs();
		}

		return rslt;
	}

	String readIs() throws IOException {
		readTimeout = INMEMORYDB.readTimeout;
		readPtyTimeout = INMEMORYDB.readPtyTimeout;
		int timeOutMulMax = INMEMORYDB.timeOutMulMax; 
		int timeOutMul = 1;
		ExecutorService executor = Executors.newFixedThreadPool(2);
		String retStr = "";
		String bufferInt = "";
		int bufferArrayLength = 2097152; //4096;
		String longProcStart="+SLWAITPROC+";
		String longProcEnd="+ELWAITPROC+";
		
		
		byte[] bufferArray = new byte[bufferArrayLength];
		
		Callable<Integer> readTask = new Callable<Integer>() {
	        @Override
	        public Integer call() throws Exception {
	            return inStream.read();
	        }
	    };
		
		//for 1byte
		int byteIdx = 0;			
		int readInt = 0;  
		byte readbyte; 
				
		try {
				    do
				    {
				    	if(retStr.endsWith(longProcStart)){
							timeOutMul = timeOutMulMax;
							logger.debug("[INC]Read-Timeout set to "+ readTimeout*timeOutMul);
						}else if(retStr.endsWith(longProcEnd)){
							timeOutMul = 1;
							logger.debug("[DEC]Read-Timeout set to "+ readTimeout*timeOutMul);
						}
				    	
					    Future<Integer> future = executor.submit(readTask);
						readInt = future.get(readTimeout*timeOutMul, TimeUnit.MILLISECONDS);
											
				    	readbyte = (byte)readInt;
				    	
				    	bufferArray[byteIdx] = readbyte;
				    	bufferInt = bufferInt+ readInt+" ";		    	
				    	if(readInt == -1){
				    		retStr = new String(bufferArray,0,byteIdx, StandardCharsets.UTF_8);
				    	}else{
				    		retStr = new String(bufferArray,0,byteIdx+1, StandardCharsets.UTF_8);
				    	}	
				
				    		byteIdx++;				    		
				    		
				    }while(readInt != -1);		    
		    //logger.debug("returnSTR = '"+retStr+"' bufferInt= '" + bufferInt +"'");
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			//logger.debug("retStr="+retStr +" last readByte="+(byte)readInt +" byteIdx="+byteIdx);
			logger.error("CANNOT FIND PROMPT PHRASE AND HANGING.. PLEASE INVESTIGATE INPUTSTREAM: {{"+retStr + "}} AND BYTES: {{"+bufferInt+"}}" +" last readByte="+(byte)readInt +" byteIdx="+byteIdx);
		}
		finally{
			if(executor != null)
			executor.shutdown();
		}
	    return retStr;
		
	}
	
	void openSessionAndChannel() throws JSchException{
		if(this.channel == null ){
			String errorMessage = null;
			if((errorMessage = connect()) != null){				
						if(errorMessage.contains("UNKNOWNHOST")){
							logger.info("Reconnect Session ~!!!");
							this.connect();							
						}else{
							logger.error("Session Connection ERROR By "+ errorMessage);							
							return ;
						}
				}else{
					logger.info("Session Connected.");
				}
		}else{			
			if(this.channel.isClosed()){
//				this.channel.disconnect();
//			logger.info("OLD CHANNEL DISCONNECTED.");
//				this.channel = null;
//				this.channel = (ChannelShell)session.openChannel("shell");
				this.channel = (ChannelExec)session.openChannel("exec");
			logger.info("Session Already exist- OPENED CHANNEL AGAIN.");
			}else{
			logger.info("Session Already exist- USE EXISTING CHANNEL.");
			}
		}		
	}

	boolean openSessionAndChannel_sshTools_RSA() throws Exception {
		/**
		 * Create an SshConnector instance
		 */
		SshConnector con = SshConnector.createInstance();
		
		boolean isConnected = false;
		/**
		 * Connect to the host
		 */

		System.out.println("Connecting to " + server);

		SocketTransport transport = new SocketTransport(server, port);


		System.out.println("Creating SSH client");
		
		ssh = con.connect(transport, username, true);

		/**
		 * Authenticate the user using password authentication
		 */
		PublicKeyAuthentication pk = new PublicKeyAuthentication();
		do {
			String fileNm = INMEMORYDB.RSAFilePath;
			String passPh = INMEMORYDB.RSAFilePhrase;
			
			
			System.out.print("Private key file: "+fileNm);
			 				SshPrivateKeyFile pkfile = SshPrivateKeyFileFactory 
			 						.parse(new FileInputStream(fileNm));
			 
			 
			 				SshKeyPair pair; 
			 				if (pkfile.isPassphraseProtected()) { 
			 					System.out.print("Passphrase: ");
			 					pair = pkfile.toKeyPair(passPh); 
			 				} else 
			 					pair = pkfile.toKeyPair(null); 
			 
			 
			 				pk.setPrivateKey(pair.getPrivateKey()); 
			 				pk.setPublicKey(pair.getPublicKey()); 
			
		} while (ssh.authenticate(pk) != SshAuthentication.COMPLETE
				&& ssh.isConnected());

		/**
		 * Start a session and do basic IO
		 */
		if (ssh.isAuthenticated()) {
			System.out.println("login Suceesce.!");
			// Some old SSH2 servers kill the connection after the first
			// session has closed and there are no other sessions started;
			// so to avoid this we create the first session and dont ever
			// use it
			session_sshtools = ssh.openSessionChannel();

			// Use the newly added PseudoTerminalModes class to
			// turn off echo on the remote shell
			PseudoTerminalModes pty = new PseudoTerminalModes(ssh);
			pty.setTerminalMode(PseudoTerminalModes.ECHO, false);

			session_sshtools.requestPseudoTerminal("vt100", 80, 24, 0, 0, pty);
			session_sshtools.startShell();

			readPtyIs_sshTools("");

			isConnected = true;
		}

		return isConnected;
	}
	
	
	boolean openSessionAndChannel_sshTools() throws Exception {
		/**
		 * Create an SshConnector instance
		 */
		SshConnector con = SshConnector.createInstance();

//		con.getContext().setHostKeyVerification(new ConsoleKnownHostsKeyVerification(INMEMORYDB.KNOWN_HOSTS));		
		boolean isConnected = false;
		/**
		 * Connect to the host
		 */

		System.out.println("Connecting to " + server);

		SocketTransport transport = new SocketTransport(server, port);


		System.out.println("Creating SSH client");
		
		ssh = con.connect(transport, username, true);
		
		/**
		 * Authenticate the user using password authentication
		 */
		PasswordAuthentication pwd = new PasswordAuthentication();
		do {
			pwd.setPassword(password);
		} while (ssh.authenticate(pwd) != SshAuthentication.COMPLETE
				&& ssh.isConnected());

		/**
		 * Start a session and do basic IO
		 */
		if (ssh.isAuthenticated()) {
			System.out.println("login Suceesce.!");
			// Some old SSH2 servers kill the connection after the first
			// session has closed and there are no other sessions started;
			// so to avoid this we create the first session and dont ever
			// use it
			session_sshtools = ssh.openSessionChannel();

			// Use the newly added PseudoTerminalModes class to
			// turn off echo on the remote shell
			PseudoTerminalModes pty = new PseudoTerminalModes(ssh);
			pty.setTerminalMode(PseudoTerminalModes.ECHO, false);

			session_sshtools.requestPseudoTerminal("vt100", 80, 24, 0, 0, pty);
			session_sshtools.startShell();

			readPtyIs_sshTools("");

			isConnected = true;
		}

		return isConnected;
	}


	boolean openSessionAndChannel_sshTools(String nwSsh) throws Exception {
		/**
		 * Create an SshConnector instance
		 */
		SshConnector con = SshConnector.createInstance();

//		con.getContext().setHostKeyVerification(new ConsoleKnownHostsKeyVerification(INMEMORYDB.KNOWN_HOSTS));
		boolean isConnected = false;
		/**
		 * Connect to the host
		 */

		System.out.println("Connecting to " + server);

		SocketTransport transport = new SocketTransport(server, port);


		System.out.println("Creating SSH client");

		ssh = con.connect(transport, username, true);

		/**
		 * Authenticate the user using password authentication
		 */
		PasswordAuthentication pwd = new PasswordAuthentication();
		do {
			pwd.setPassword(password);
		} while (ssh.authenticate(pwd) != SshAuthentication.COMPLETE
				&& ssh.isConnected());

		/**
		 * Start a session and do basic IO
		 */
		if (ssh.isAuthenticated()) {
			System.out.println("login Suceesce.!");
			// Some old SSH2 servers kill the connection after the first
			// session has closed and there are no other sessions started;
			// so to avoid this we create the first session and dont ever
			// use it
			session_sshtools = ssh.openSessionChannel();

			// Use the newly added PseudoTerminalModes class to
			// turn off echo on the remote shell
			PseudoTerminalModes pty = new PseudoTerminalModes(ssh);
			pty.setTerminalMode(PseudoTerminalModes.ECHO, false);

			session_sshtools.requestPseudoTerminal("vt100", 80, 24, 0, 0, pty);
			session_sshtools.startShell();


			isConnected = true;
		}

		return isConnected;
	}

	boolean openSessionAndChannel_sshToolsProxy(AgentInfo relayInfo) throws Exception {

		String relayServer = relayInfo.getConnectIpAddress();
		int relayServerPort = relayInfo.getPortSftp();
		String relayServerId = relayInfo.getUserIdOs();
		String relayServerPw = relayInfo.getPasswordOs();


		/**
		 * Create an SshConnector instance
		 */
		SshConnector con = SshConnector.createInstance();

		boolean isConnected = false;
		/**
		 * Connect to the host
		 */

		logger.debug("Connecting to " + server);

		/**
		 * Connect to the host
		 */
		final SshClient ssh = con.connect(new SocketTransport(relayServer, relayServerPort), relayServerId, true);


		/**
		 * Authenticate the user using password authentication
		 */
		PasswordAuthentication pwd = new PasswordAuthentication();
		do {
			pwd.setPassword(relayServerPw);
		} while (ssh.authenticate(pwd) != SshAuthentication.COMPLETE
				&& ssh.isConnected());

		/**
		 * Start a session and do basic IO
		 */
		if (ssh.isAuthenticated()) {

			SshTunnel tunnel = ssh.openForwardingChannel(server, port,
					"127.0.0.1", 22, "127.0.0.1", 22, null, null);


			forwardedConnection = con.connect(tunnel,username);


			/**
			 * Authenticate the user using password authentication
			 */
			PasswordAuthentication pwd_target = new PasswordAuthentication();
			pwd_target.setUsername(username);
			pwd_target.setPassword(password);

			forwardedConnection.authenticate(pwd_target);



			forwardedSession = forwardedConnection.openSessionChannel();

			// Use the newly added PseudoTerminalModes class to
			// turn off echo on the remote shell
			PseudoTerminalModes pty = new PseudoTerminalModes(forwardedConnection);
			pty.setTerminalMode(PseudoTerminalModes.ECHO, false);

			forwardedSession.requestPseudoTerminal("vt100", 80, 24, 0, 0, pty);
			forwardedSession.startShell();

			readPtyIs_sshToolsProxy("");

			isConnected = true;
		}

		return isConnected;
	}
	
	

public boolean exePtyCommand(String runCmd , String id, String pw, String mode) throws Exception {

	//SEND COMMAND
	logger.debug("SEND SSH COMMAND "+ runCmd);
	//agentCommlogger.debug("SEND SSH COMMAND "+ runCmd +" mode:"+mode);
	try{
		openSessionAndChannel();	
	}catch(JSchException je){
		//agentCommlogger.error("ERROR ON OPEN SESSION & CHANNEL. MSG:" +je.getMessage());
		throw je;
	}
	
	try{
    connectChannel(runCmd);
	}catch(JSchException je2){
		//agentCommlogger.error("ERROR ON CONNECT CHANNEL. MSG:" +je2.getMessage());
		throw je2;
	}catch(IOException ioe){
		//agentCommlogger.error("ERROR ON CONNECT CHANNEL. MSG:" +ioe.getMessage());
		throw ioe;
	}

	String ptyType = (String)readPtyIs(mode).get(INMEMORYDB.ptyKey.PTY.toString());
	
	return handlePtyPhrase(ptyType, id, pw , mode);
}

	public boolean exePtyCommand_sshTools(String runCmd , String id, String pw, String mode) throws Exception {

		//SEND COMMAND
		logger.debug("SEND SSH COMMAND "+ runCmd);
		//agentCommlogger.debug("SEND SSH COMMAND "+ runCmd +" mode:"+mode);


		try{
			connectChannel_sshTools(runCmd);
		}catch(JSchException je2){
			//agentCommlogger.error("ERROR ON CONNECT CHANNEL. MSG:" +je2.getMessage());
			throw je2;
		}catch(IOException ioe){
			//agentCommlogger.error("ERROR ON CONNECT CHANNEL. MSG:" +ioe.getMessage());
			throw ioe;
		}

		String ptyType = (String)readPtyIs_sshTools(mode).get(INMEMORYDB.ptyKey.PTY.toString());

		return handlePtyPhrase_sshTools(ptyType, id, pw , mode);
	}

	public boolean exePtyCommand_sshToolsProxy(String runCmd , String id, String pw, String mode) throws Exception {

		//SEND COMMAND
		logger.debug("SEND SSH COMMAND "+ runCmd);
		//agentCommlogger.debug("SEND SSH COMMAND "+ runCmd +" mode:"+mode);


		try{
			connectChannel_sshToolsProxy(runCmd);
		}catch(JSchException je2){
			//agentCommlogger.error("ERROR ON CONNECT CHANNEL. MSG:" +je2.getMessage());
			throw je2;
		}catch(IOException ioe){
			//agentCommlogger.error("ERROR ON CONNECT CHANNEL. MSG:" +ioe.getMessage());
			throw ioe;
		}

		String ptyType = (String)readPtyIs_sshToolsProxy(mode).get(INMEMORYDB.ptyKey.PTY.toString());

		return handlePtyPhrase_sshToolsProxy(ptyType, id, pw , mode);
	}

	public boolean handlePtyPhrase(String TYPE , String intputId, String inputPw, String mode) throws Exception {
		String ptyType = "";
		String inputPhrase = "";
		boolean callAgain = true;
		boolean promptChk = false;
		if(TYPE == null){
			logger.debug("CANNOT UNDERSTAND PTY TYPE : null" );
			//agentCommlogger.debug("CANNOT UNDERSTAND PTY TYPE : null");
			return false;
		}

		switch(TYPE){
		case "LOGIN":
			inputPhrase = intputId+"\n";
			logger.debug("SEND LOGIN ID "+ inputPhrase);
			//agentCommlogger.debug("SEND LOGIN ID "+ inputPhrase);
			callAgain = true;
			mode = INMEMORYDB.ptyMode.LOGIN.toString();
			break;
		case "PASSWORD":
			inputPhrase = inputPw+"\n";
	//		inputPhrase = inputPw;
			logger.debug("SENDP");
			//agentCommlogger.debug("SENDP");
			callAgain = true;
			mode = INMEMORYDB.ptyMode.LOGIN.toString();
			break;
		case "PROMPT":
			logger.debug("REACHED PROMPT [PTY PROCESS END].");
			//agentCommlogger.debug("REACHED PROMPT [PTY PROCESS END].");
			callAgain = false;
			break;
		case "SSLCONFRM" :
			logger.debug("SEND SSLCONFIRM yes");
			//agentCommlogger.debug("SEND SSLCONFIRM yes");
			inputPhrase = "yes\n";
			mode = INMEMORYDB.ptyMode.LOGIN.toString();
			callAgain = true;
			break;
		case "INCORRECTPASS" :
			logger.debug("INCORRECT PASSWORD");
			//agentCommlogger.debug("INCORRECT PASSWORD");
			callAgain = false;
			break;
		case "LOGINFIN" :
			logger.debug("LOGIN FINISHED.");
			//agentCommlogger.debug("LOGIN FINISHED.");
			callAgain = false;
			break;
		default: logger.debug("UNKNOWN PTY TYPE ["+TYPE+"]");
			//agentCommlogger.debug("UNKNOWN PTY TYPE ["+TYPE+"]");
			callAgain = false;
			return false;
		}
	
		if(callAgain){
		//agentCommlogger.debug("[START]STREAM WRITE IN handlePtyPhrase for TYPE "+TYPE);
		this.outStream.write(inputPhrase.getBytes());
		this.outStream.flush();
		if("PASSWORD".equalsIgnoreCase(TYPE)){
	//		logger.debug("SENT IN handlePtyPhrase :"+PatternMaker.toMuxENC(inputPhrase));
	//		//agentCommlogger.debug("SENT IN handlePtyPhrase :"+PatternMaker.toMuxENC(inputPhrase));
			}else{
				logger.debug("SENT IN handlePtyPhrase :"+inputPhrase);
				//agentCommlogger.debug("SENT IN handlePtyPhrase :"+inputPhrase);
			}

		//agentCommlogger.debug("[END]STREAM WRITE IN handlePtyPhrase for TYPE "+TYPE);

		ptyType = (String) readPtyIs(mode).get(ptyReturnKey[0]);
		logger.debug("WILL CALL handlePtyPhrase with ptyType:"+ptyType +" AFTER TYPE "+TYPE);
		//agentCommlogger.debug("WILL CALL handlePtyPhrase with ptyType:"+ptyType+" AFTER TYPE "+TYPE);

		return handlePtyPhrase(ptyType, intputId, inputPw,mode);
		}else{
			return true;
		}

	}

	public boolean handlePtyPhrase_sshTools(String TYPE , String intputId, String inputPw, String mode) throws Exception {
		String ptyType = "";
		String inputPhrase = "";
		boolean callAgain = true;
		boolean promptChk = false;
		if(TYPE == null){
			logger.debug("CANNOT UNDERSTAND PTY TYPE : null" );
			//agentCommlogger.debug("CANNOT UNDERSTAND PTY TYPE : null");
			return false;
		}

		switch(TYPE){
			case "LOGIN":
				inputPhrase = intputId+"\n";
				logger.debug("SEND LOGIN ID "+ inputPhrase);
				//agentCommlogger.debug("SEND LOGIN ID "+ inputPhrase);
				callAgain = true;
				mode = INMEMORYDB.ptyMode.LOGIN.toString();
				break;
			case "PASSWORD":
				inputPhrase = inputPw+"\n";
//		inputPhrase = inputPw;
				logger.debug("SENDP");
				//agentCommlogger.debug("SENDP");
				callAgain = true;
				mode = INMEMORYDB.ptyMode.LOGIN.toString();
				break;
			case "PROMPT":
				logger.debug("REACHED PROMPT [PTY PROCESS END].");
				//agentCommlogger.debug("REACHED PROMPT [PTY PROCESS END].");
				callAgain = false;
				break;
			case "SSLCONFRM" :
				logger.debug("SEND SSLCONFIRM yes");
				//agentCommlogger.debug("SEND SSLCONFIRM yes");
				inputPhrase = "yes\n";
				mode = INMEMORYDB.ptyMode.LOGIN.toString();
				callAgain = true;
				break;
			case "INCORRECTPASS" :
				logger.debug("INCORRECT PASSWORD");
				//agentCommlogger.debug("INCORRECT PASSWORD");
				callAgain = false;
				break;
			case "LOGINFIN" :
				logger.debug("LOGIN FINISHED.");
				//agentCommlogger.debug("LOGIN FINISHED.");
				callAgain = false;
				break;
			default: logger.debug("UNKNOWN PTY TYPE ["+TYPE+"]");
				//agentCommlogger.debug("UNKNOWN PTY TYPE ["+TYPE+"]");
				callAgain = false;
				return false;
		}

		if(callAgain){
			//agentCommlogger.debug("[START]STREAM WRITE IN handlePtyPhrase for TYPE "+TYPE);
			session_sshtools.getOutputStream().write(inputPhrase.getBytes());
			session_sshtools.getOutputStream().flush();
			if("PASSWORD".equalsIgnoreCase(TYPE)){
			}else{
				logger.debug("SENT IN handlePtyPhrase :"+inputPhrase);
				//agentCommlogger.debug("SENT IN handlePtyPhrase :"+inputPhrase);
			}

			//agentCommlogger.debug("[END]STREAM WRITE IN handlePtyPhrase for TYPE "+TYPE);

			ptyType = (String) readPtyIs_sshTools(mode).get(ptyReturnKey[0]);
			logger.debug("WILL CALL handlePtyPhrase with ptyType:"+ptyType +" AFTER TYPE "+TYPE);
			//agentCommlogger.debug("WILL CALL handlePtyPhrase with ptyType:"+ptyType+" AFTER TYPE "+TYPE);

			return handlePtyPhrase_sshTools(ptyType, intputId, inputPw,mode);
		}else{
			return true;
		}

	}

	public boolean handlePtyPhrase_sshToolsProxy(String TYPE , String intputId, String inputPw, String mode) throws Exception {
		String ptyType = "";
		String inputPhrase = "";
		boolean callAgain = true;
		boolean promptChk = false;
		if(TYPE == null){
			logger.debug("CANNOT UNDERSTAND PTY TYPE : null" );
			//agentCommlogger.debug("CANNOT UNDERSTAND PTY TYPE : null");
			return false;
		}

		switch(TYPE){
			case "LOGIN":
				inputPhrase = intputId+"\n";
				logger.debug("SEND LOGIN ID "+ inputPhrase);
				//agentCommlogger.debug("SEND LOGIN ID "+ inputPhrase);
				callAgain = true;
				mode = INMEMORYDB.ptyMode.LOGIN.toString();
				break;
			case "PASSWORD":
				inputPhrase = inputPw+"\n";
//		inputPhrase = inputPw;
				logger.debug("SENDP");
				//agentCommlogger.debug("SENDP");
				callAgain = true;
				mode = INMEMORYDB.ptyMode.LOGIN.toString();
				break;
			case "PROMPT":
				logger.debug("REACHED PROMPT [PTY PROCESS END].");
				//agentCommlogger.debug("REACHED PROMPT [PTY PROCESS END].");
				callAgain = false;
				break;
			case "SSLCONFRM" :
				logger.debug("SEND SSLCONFIRM yes");
				//agentCommlogger.debug("SEND SSLCONFIRM yes");
				inputPhrase = "yes\n";
				mode = INMEMORYDB.ptyMode.LOGIN.toString();
				callAgain = true;
				break;
			case "INCORRECTPASS" :
				logger.debug("INCORRECT PASSWORD");
				//agentCommlogger.debug("INCORRECT PASSWORD");
				callAgain = false;
				break;
			case "LOGINFIN" :
				logger.debug("LOGIN FINISHED.");
				//agentCommlogger.debug("LOGIN FINISHED.");
				callAgain = false;
				break;
			default: logger.debug("UNKNOWN PTY TYPE ["+TYPE+"]");
				//agentCommlogger.debug("UNKNOWN PTY TYPE ["+TYPE+"]");
				callAgain = false;
				return false;
		}

		if(callAgain){
			//agentCommlogger.debug("[START]STREAM WRITE IN handlePtyPhrase for TYPE "+TYPE);
			forwardedSession.getOutputStream().write(inputPhrase.getBytes());
			forwardedSession.getOutputStream().flush();
			if("PASSWORD".equalsIgnoreCase(TYPE)){
			}else{
				logger.debug("SENT IN handlePtyPhrase :"+inputPhrase);
				//agentCommlogger.debug("SENT IN handlePtyPhrase :"+inputPhrase);
			}

			//agentCommlogger.debug("[END]STREAM WRITE IN handlePtyPhrase for TYPE "+TYPE);

			ptyType = (String) readPtyIs_sshToolsProxy(mode).get(ptyReturnKey[0]);
			logger.debug("WILL CALL handlePtyPhrase with ptyType:"+ptyType +" AFTER TYPE "+TYPE);
			//agentCommlogger.debug("WILL CALL handlePtyPhrase with ptyType:"+ptyType+" AFTER TYPE "+TYPE);

			return handlePtyPhrase_sshToolsProxy(ptyType, intputId, inputPw,mode);
		}else{
			return true;
		}

	}


	private HashMap<String, Object> readPtyIs(String mode) throws Exception
	{
		return this.socketStreamReader.readIs(this.inStream, mode);
	}


	private HashMap<String, Object> readPtyIs_sshTools(String mode) throws Exception
	{
		return this.socketStreamReader.readIs(session_sshtools.getInputStream(), mode);
	}

	private HashMap<String, Object> readPtyIs_sshToolsProxy(String mode) throws Exception
	{
		return this.socketStreamReader.readIs(forwardedSession.getInputStream(), mode);
	}


 String removeFirstAndLastLineFeed(String src){
	 String rslt = "";
	 try{
//		 logger.debug("Before Remove LineFeed '"+src +"'");

		    byte[] bytes = src.getBytes();  
		    
		    int sIdx = 0;
		    int eIdx = bytes.length;
//			logger.debug("S1 : "+bytes[0] + " E1:"+ bytes[eIdx-1] + " eIdx:"+eIdx);
		    //REMOVE LINE FEED FOR FIRST 2 BYTES
		    if(bytes[sIdx] == 10 || bytes[sIdx] == 13){
//		    	logger.debug("S1 : "+bytes[sIdx]);
		    	sIdx++;
		    	if(bytes[sIdx] == 10 || bytes[sIdx] == 13){
//		    		logger.debug("S2 : "+bytes[sIdx]);
		    		sIdx++;
		    	}
		    }
		    //REMOVE LINE FEED FOR LAST 3 BYTES    
		    if(bytes[eIdx-1] == 10 || bytes[eIdx-1] == 13 || bytes[eIdx-1] == -1){
//		    	logger.debug("E1 : "+bytes[eIdx-1]);
		    	eIdx--;
		    	if(bytes[eIdx-1] == 10 || bytes[eIdx-1] == 13){
//		    		logger.debug("E2 : "+bytes[eIdx-1]);
		    		eIdx--;
		    		if(bytes[eIdx-1] == 10 || bytes[eIdx-1] == 13){
//		    			logger.debug("E3 : "+bytes[eIdx-1]);
		        		eIdx--;
		        	}
		    	}
		    }
		    rslt = new String(bytes,sIdx,eIdx-sIdx, StandardCharsets.UTF_8);
		    logger.debug("After Remove LineFeed '"+rslt +"'");
			
	 }
	 catch(Exception e){
		 logger.error("ERROR WHILE removeFirstAndLastLineFeed " + e.getMessage());
		 rslt = src;
	 }
   
	return rslt;
	
}

	
	public void close() {
		if( this.channel != null ) {
			this.channel.disconnect();
		}

		if (this.session != null) {
			this.session.disconnect();
		}
		
	}

	
}
