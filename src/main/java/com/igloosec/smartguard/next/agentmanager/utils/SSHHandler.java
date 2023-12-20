/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.utils.SSHHandler.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 1. 11.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.utils;

import com.igloosec.smartguard.next.agentmanager.entity.AgentInfo;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.jcraft.jsch.JSchException;

import jodd.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class SSHHandler extends SSHCommon{

	public SSHHandler(AgentInfo agentInfo) {
		super(agentInfo);
	}
	
	public SSHHandler(AgentInfo agentInfo,int loginType) {
		super(agentInfo,loginType);
	}

	public SSHHandler(AgentInfo agentInfo, String sshtools) throws Exception {
		super(agentInfo,sshtools);
	}

	public SSHHandler(AgentInfo agentInfo, AgentInfo relayInfo, String sshtools, String proxy) throws Exception {
		super(agentInfo,relayInfo,sshtools,proxy);
	}

	public SSHHandler(String userName, String password, String connectionIP, int connectionPort, String knownHostFile) throws Exception {
		super(userName, password, connectionIP,connectionPort, knownHostFile);
	}

	private Logger logger = LoggerFactory.getLogger(getClass());


    static byte[] str2byte(String str, String encoding){
		if(str==null)
		  return null;
		try{ return str.getBytes(encoding); }
		catch(java.io.UnsupportedEncodingException e){
		  return str.getBytes();
		}
	}

	static byte[] str2byte(String str){
		    return str2byte(str, "UTF-8");
		  }
	
	static String cleanStr(String org){
		if(org != null){
			org = StringUtil.replace(org, "\r\n", "");
			org = StringUtil.replace(org, "\n", "");
			org = StringUtil.replace(org, "\r", "");	
		}
		return org;
		
	}

	public boolean doSu(String rootId, String rootPass) throws Exception {
//		String whoami = sendCommand("whoami");
//		logger.debug("WHOAMI:"+whoami);
//		if(whoami != null && whoami.contains(rootId)){
//			logger.info("Already Logged in As "+ rootId);
//			return false;
//		}
		String mode = INMEMORYDB.ptyMode.LOGIN.toString();
		return exePtyCommand("su - "+rootId, rootId, rootPass,mode);
	}
	
	public boolean doSu(String rootId, String rootPass, boolean useSudo) throws Exception {

		String suCmd = "su - "+rootId;
		
		if(useSudo){
			suCmd =  "sudo su - "+rootId;
		}
		
		String mode = INMEMORYDB.ptyMode.LOGIN.toString();
		return exePtyCommand(suCmd, rootId, rootPass,mode);
	}
	
	public boolean doCmdSu(String id, String pass, String cmdSu) throws Exception {
		////Login: sv_admin + Password:xxxxx + sv_enable sv_admin + su -
		String mode = INMEMORYDB.ptyMode.LOGIN.toString();
		boolean isCmdDone = exePtyCommand(cmdSu+ " "+id, id, pass ,mode);
		if(isCmdDone){
			String PTY = (String)exeCommand2("su -",mode).get(INMEMORYDB.ptyKey.PTY.toString());
			if("PROMPT".equals(PTY)){
				logger.debug("[INLINE-SU]PROMPT = "+PTY);
				return true;
			}
			else{
				logger.debug("[INLINE-SU] FAILED PROMPT = "+PTY);
				return false;
			}
		}else{
			logger.debug("[INLINE-SU] FAILED AT SENDING "+cmdSu+ " "+id);
			return false;
		}
		
	}
	
	
	

	public boolean doSu_sshTools(String rootId, String rootPass) throws Exception {

		String mode = INMEMORYDB.ptyMode.LOGIN.toString();
		return exePtyCommand_sshTools("su - "+rootId, rootId, rootPass,mode);
	}

	public boolean doSu_sshToolsProxy(String rootId, String rootPass) throws Exception {

		String mode = INMEMORYDB.ptyMode.LOGIN.toString();
		return exePtyCommand_sshToolsProxy("su - "+rootId, rootId, rootPass,mode);
	}

	public String sendCommand(String runCmd){
		String mode = INMEMORYDB.ptyMode.CMD.toString();
		String result ;
		try{
			result = exeCommand(runCmd , mode);
		}catch(Exception e){
			result = "[COMMAND_FAILURE] ERROR OCCURRED WHILE EXEC CMD (NO-SU) . ERRMSG: "+runCmd+" : "+e.getMessage();
		}
//		result = cleanUpResultPhrase(result,runCmd);
	 	 return result;
	}

	public String sendCommand_sshTools(String runCmd) throws Exception {
		String mode = INMEMORYDB.ptyMode.CMD.toString();
		String result = exeCommand_sshTools(runCmd,mode);
		result = cleanUpResultPhrase(result,runCmd);
		return result;
	}

	public String sendCommand_sshToolsProxy(String runCmd) throws Exception {
		String mode = INMEMORYDB.ptyMode.CMD.toString();
		String result = exeCommand_sshToolsProxy(runCmd,mode);
		result = cleanUpResultPhrase(result,runCmd);
		return result;
	}

	
	public String suSendCommand(String rootId , String rootPass, String command){
		//suSendCommand(rootId,rootPass,command,접속계정&루트계정으로 접속,sudo 사용안함)
		return suSendCommand(rootId,rootPass,command,0,"N");
	}
	
	

	/**
	 * loginType; 
	 *    CR[0] : conID&RootID
	 *    RO[1] : RootID only
	 *    RSA[2]: use RSA 
	 *    LC[3] : Line Command
	 *    PEM[4]: use PEM 
	 **/
 	public String suSendCommand(String rootId , String rootPass, String command , int loginType , String useSudo){

	 	String result = "";
	 	boolean switched =  false;
	 	
	 	switch(loginType){
	 	case 0 : 
	 		    logger.debug("[GENERAL]************ SU START ******************************");			
			 	try{
			 		if("Y".equalsIgnoreCase(useSudo)){
			 			switched = doSu(rootId,rootPass,true);			 			
		 			}else{
		 				switched = doSu(rootId,rootPass);			 				
		 			}
					
				}catch(Exception e){
					result = "[GENERAL][CONNECTION_FAILURE] ERROR OCCURRED WHILE SWITCHING USER TO ROOT. \\n CHECK ID/PW FOR "+rootId +" ERRMSG:"+e.getMessage();
					logger.info(result);
				}
				logger.debug("[GENERAL]************* SU DONE:"+switched +" ****************");
				
				break;
	 	case 1 : 
	 			logger.debug("[ROOT LOGIN] ************  SU ALREADY DONE. ****************"); 
	 			 break;
	 	case 2 : 
	 		    logger.debug("[SUDO] ************* SU START ******************************");
		 		try{
		 			if("N".equalsIgnoreCase(useSudo)){
		 				switched = doSu(rootId,rootPass,false);	
		 			}else{
						switched=true;
		 				logger.debug("do not need to switch user. (loginType : 2, rootID : " + rootId + ", rootPass : " + rootPass);
//		 				switched = doSu(rootId,rootPass,true);
		 			}
					
				}catch(Exception e){
					result = "[SUDO][CONNECTION_FAILURE] ERROR OCCURRED WHILE SWITCHING USER TO ROOT. \\n CHECK ID/PW FOR "+rootId +" ERRMSG:"+e.getMessage();
					logger.info(result);
				}
				logger.debug("[SUDO] ************** SU DONE:"+switched +" ****************");
	 			 break;
	 	case 3 : 
//	 		    logger.debug("[INLINE-SU]************ SU START ***************************");			
//			 	try{
//					switched = doCmdSu(rootId,rootPass,);
//				}catch(Exception e){
//					result = "[INLINE-SU][CONNECTION_FAILURE] ERROR OCCURRED WHILE SWITCHING USER TO ROOT. \\n CHECK ID/PW FOR "+rootId +" ERRMSG:"+e.getMessage();
//					logger.info(result);
//				}
//				logger.debug("[INLINE-SU]************* SU DONE:"+switched +" *************");
//				
				break;
	 	
	 	default : logger.debug("SU TYPE IS UNDEFINED.["+loginType+"]"); break;
	 	}
	 	
		String mode = INMEMORYDB.ptyMode.CMD.toString();
		if(switched) {
			try{
				result = exeCommand(command , mode);
			}catch(Exception e){
				result = "[COMMAND_FAILURE] ERROR OCCURRED WHILE EXEC CMD (POST-SU) . ERRMSG: "+command+" : "+e.getMessage();
			}
		}else {
			result = "[CONNECTION_FAILURE] ERROR OCCURRED WHILE SWITCHING USER TO ROOT. \n CHECK ID/PW FOR "+rootId;
		}


//		result = cleanUpResultPhrase(result,command);
		if(this.channel != null){this.channel.disconnect(); logger.debug("channel disconnected after execommand.");}
		if(this.session != null){this.session.disconnect(); logger.debug("session disconnected after execommand.");}
		
		return result;
	}

 	
 	
 	public String suSendCommand(String rootId , String rootPass, String command , int loginType , String useSudo, String cmdSu){

//		public String suSendCommand(String rootId , String rootPass, String command) throws Exception{
	 	String result = "";
	 	boolean switched =  false;
	 	

	 		    logger.debug("[INLINE-SU]************ SU START ***************************");			
			 	try{
					switched = doCmdSu(rootId,rootPass,cmdSu);
				}catch(Exception e){
					result = "[INLINE-SU][CONNECTION_FAILURE] ERROR OCCURRED WHILE SWITCHING USER TO ROOT. \\n CHECK ID/PW FOR "+rootId +" ERRMSG:"+e.getMessage();
					logger.info(result);
				}
				logger.debug("[INLINE-SU]************* SU DONE:"+switched +" *************");
				

	 	
	 	
		String mode = INMEMORYDB.ptyMode.CMD.toString();
		if(switched) {
			try{
				result = exeCommand(command , mode);
			}catch(Exception e){
				result = "[COMMAND_FAILURE] ERROR OCCURRED WHILE EXEC CMD (POST-SU) . ERRMSG: "+command+" : "+e.getMessage();
			}
		}else {
			result = "[CONNECTION_FAILURE] ERROR OCCURRED WHILE SWITCHING USER TO ROOT. \n CHECK ID/PW FOR "+rootId;
		}

		if(this.channel != null){this.channel.disconnect(); logger.debug("channel disconnected after execommand.");}
		if(this.session != null){this.session.disconnect(); logger.debug("session disconnected after execommand.");}
		
		return result;
	}

 	
 	
//	public String suSendCommand_sshTools(String rootId , String rootPass, String command) throws Exception{
   public String suSendCommand_sshTools(String rootId , String rootPass, String command, String useSudo){
	   String result = "";
	   boolean switched = false;
	   if (useSudo.toUpperCase().equals("N")) {
		   logger.debug("*********************  SU START[SSHT] *********************");
		   try {
			   switched = doSu_sshTools(rootId, rootPass);
		   } catch (Exception e) {
			   result = "[CONNECTION_FAILURE][SSHT] ERROR OCCURRED WHILE SWITCHING USER TO ROOT. CHECK ID/PW FOR " + rootId + " ERRMSG:" + e.getMessage();
		   }
		   logger.debug("*********************SU DONE[SSHT]:" + switched + " *********************");
	   } else {
		   switched = true;
		   logger.debug("don`t need to switch use.");
	   }

		String mode = INMEMORYDB.ptyMode.CMD.toString();
		if(switched) {
			try{
				result = exeCommand_sshTools(command , mode);
			}catch(Exception e){
				result = "[COMMAND_FAILURE][SSHT] ERROR OCCURRED WHILE EXEC CMD (POST-SU) . ERRMSG: "+command+" : "+e.getMessage();
			}
		}else {
			result = "[CONNECTION_FAILURE] ERROR OCCURRED WHILE SWITCHING USER TO ROOT. CHECK ID/PW FOR "+rootId;
		}

		if(this.session_sshtools != null){this.session_sshtools.close(); logger.debug("channel disconnected after execommand.");}
		if(this.ssh != null){this.ssh.disconnect(); logger.debug("session disconnected after execommand.");}

		return result;
	}

	public String suSendCommand_sshToolsProxy(String rootId , String rootPass, String command) throws Exception {
		String result = "";

		logger.debug("********************* SU START *********************");
		//agentCommlogger.debug("*********************  SU START *********************");
		boolean switched =  doSu_sshToolsProxy(rootId,rootPass);
		logger.debug("*********************SU DONE:"+switched +" *********************");
		//agentCommlogger.debug("*********************SU DONE:"+switched +" *********************");
		String mode = INMEMORYDB.ptyMode.CMD.toString();
		if(switched) {
			result = exeCommand_sshToolsProxy(command , mode);
		}else{

			result = " FAILED SWITCHING USER TO ROOT.";
		}

//		result = cleanUpResultPhrase(result,command);
		if(this.forwardedSession != null){this.forwardedSession.close(); logger.debug("channel disconnected after execommand.");}
		if(this.forwardedConnection != null){this.forwardedConnection.disconnect(); logger.debug("session disconnected after execommand.");}

		return result;
	}


	public String[] findPromptPhrase(String rId, String rPw) throws Exception {
		String rootP = "";
		String connP = "";
		String[] prompts = new String[2];
		try {
			if(doSu(rId, rPw)){
				String mode = INMEMORYDB.ptyMode.CMD.toString();
				rootP = exeCommand("",mode);
				doSu(this.username, this.password);
				connP = exeCommand("",mode);
			}else{
				logger.error("Please Enter ID/PW of Root.");
			}

			this.rootPrompt = removeFirstAndLastLineFeed(rootP);
			this.connPrompt = removeFirstAndLastLineFeed(connP);

			prompts[0] = this.rootPrompt;
			prompts[1] = this.connPrompt;
	//		rootP = exeCommand("");
	//		rootP = removeFirstAndLastLineFeed(rootP);
	//		exePtyCommand("su -" , this.username, this.password);
	//		connP = exeCommand("");
	//		connP = removeFirstAndLastLineFeed(connP);

		} catch (IOException | JSchException e1) {
			e1.printStackTrace();

			logger.error("ERROR WHILE FIND PROMPT PHRASE.!" + e1.getMessage());
			prompts[0] = "";
			prompts[1] = "";

		}

		logger.info("rootP : '"+ this.rootPrompt + "' connP : '"+ this.connPrompt+"'");
		return prompts;
	}

	private String cleanUpResultPhrase(String src, String cmd){
		String returnStr = src;
		try {
			//remove command Phrase
			returnStr = returnStr.replaceFirst(cmd, "");
			//remove prompt Phrase
	//	logger.debug("clean: "+ this.rootPrompt +", "+ this.connPrompt);
			returnStr = returnStr.replace(this.rootPrompt, "");
			returnStr = returnStr.replace(this.connPrompt, "");
			//remove line feed
			returnStr = removeFirstAndLastLineFeed(returnStr);
		}catch (java.util.regex.PatternSyntaxException e){}

		return returnStr;

	}

    
	public void mkdir(AgentInfo ai, String absolutePath) throws Exception {
		String command = "mkdir -p "+absolutePath+" \n";
		
		logger.debug("mkdir:"+command);
		String reStr = suSendCommand(ai.getUserIdRoot(),
				ai.getPasswordRoot(), command);
		
		logger.info(" MKDIR result STR:" + reStr );
	}
	
	/**
	 * ONLY FOR ROOT account
	 * **/
	public boolean isDirExist(AgentInfo ai, String absolutePath) throws Exception {
		boolean exist = false;
		String lsStr = getList(ai,absolutePath);
		logger.debug("isDirExist LIST STRING :"+lsStr);
		if(!lsStr.contains(absolutePath)){
			exist = true;
			logger.debug(absolutePath + "EXIST.");
		}else{
			logger.debug(absolutePath+ "NOT EXIST.");
		}
		return exist;
	}
	
	public void mvFile(AgentInfo agentInfo, String from, String to) throws Exception {
	
		String commands = "mv -f "+from+" "+to +" \n";
		String result = suSendCommand(agentInfo.getUserIdRoot(),
				agentInfo.getPasswordRoot(), commands);	
	
	}
	
	
	/**
	 * ONLY FOR ROOT account
	 * @throws JSchException 
	 * @throws IOException
	 * **/
	public String getList(AgentInfo agentInfo, String absolutePath) throws Exception {
		String reStr = "";
		String command = "ls "+absolutePath +" \n";
		logger.debug("entered getList : "+command);
		
		
			reStr = suSendCommand(agentInfo.getUserIdRoot(),
					agentInfo.getPasswordRoot(), command);
		
		logger.info(" GET LIST result STR:" + reStr );
		
		return reStr;
	}
	
	public void stringToFile(String ctn, String fileName)
			throws FileNotFoundException {
		PrintWriter out = new PrintWriter(fileName);
		out.println(ctn);
		out.close();
	}
	
	public static void main(String args[]) throws Exception {
 		String command = "ls -l";
		String userName = "";
		String password = "";
		String connectionIP = "";
		String rootPw = "";

		if (args.length > 1){
			userName = args[0];
			password = args[1];
			connectionIP = args[2];
			rootPw = args[3];
		}

		System.out.println("userName : "+userName);
		System.out.println("password : "+password);
		System.out.println("connectionIP : "+connectionIP);
		System.out.println("rootPw : "+rootPw);


		int connectionPort = 22;
				String knownHostFile = "knownHosts.txt";
		
		SSHHandler instance = new SSHHandler(userName, password, connectionIP, connectionPort, knownHostFile);
		

		System.out.println("!!!! :: "+instance.sendCommand_sshToolsProxy("ls -al"));
//		instance.findPromptPhrase("root","");
		String result = instance.suSendCommand_sshToolsProxy("root",rootPw,"ls -l");
		System.out.println("'1111>>>"+result +"<<<'");
		instance.close();
		
	}

}

