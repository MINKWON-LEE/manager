/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.services.ConnectionManager.java
 * company : Mobigen
 * @author : Je-Joong Lee
 * created at : 2016. 2. 3.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.services;

import com.igloosec.smartguard.next.agentmanager.dao.Dao;
import com.igloosec.smartguard.next.agentmanager.entity.AgentInfo;
import com.igloosec.smartguard.next.agentmanager.entity.JobEntity;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.utils.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;

@Service("connectionManager")
public class ConnectionManager extends AbstractManager {

	/**
	 * RETURN: HashMap<String,String> returnHash returnHash.get("RESULT") =
	 * "true"/"false" returnHash.get("UPLOADPATH") = "Uploaded Path"/""
	 * returnHash.get("ERROR") = "Error message"/""
	 * **/
	@Autowired
	private Dao dao;

	@SuppressWarnings({ "rawtypes", "unused", "unchecked" })
	public HashMap<String, String> uploadOneFileViaSFTP(AgentInfo agentInfo,
														String remotePath, File f) {
		HashMap hm = new HashMap();

		HashMap<String, String> returnHash = new HashMap<String, String>();
		boolean isLogin = false;
		String errMsg = "";
		String lastUploadedPath = "";

		SFTPHandler sftp = new SFTPHandler(agentInfo);

		// ///// LOGIN //////////

		errMsg = sftp.login();

		if (errMsg != null && errMsg.contains("UNKNOWNHOST")) {
			logger.debug("RECONNECT BY UNKNOWNHOST EXCEPTION.");
			errMsg = sftp.login();
			if (errMsg == null || "".equals(errMsg)) {
				isLogin = true;
			}
		} else if (errMsg == null || "".equals(errMsg)) {
			isLogin = true;
		}

		if (isLogin) {
			lastUploadedPath = sftp.uploadFile(remotePath, f);
		}
		sftp.logout();

		return hm;
	}

	public HashMap<String, String> uploadViaSFTP(AgentInfo agentInfo,
                                                 String remotePath, File[] files) throws Exception {

		HashMap<String, String> returnHash = new HashMap<String, String>();
		boolean isLogin = false;
		String errMsg = "";
		String lastUploadedPath = "";

		SFTPHandler sftp = new SFTPHandler(agentInfo);

		// ///// LOGIN //////////

		errMsg = sftp.login();

		if (errMsg != null && errMsg.contains("UNKNOWNHOST")) {
			logger.debug("RECONNECT BY UNKNOWNHOST EXCEPTION.");
			// agentCommlogger.info("RECONNECT BY UNKNOWNHOST EXCEPTION.");
			// sftp = new SFTPHandler(agentInfo);
			errMsg = sftp.login();
			if (errMsg != null && errMsg.contains("UNKNOWNHOST") && errMsg.contains("CHANGED SSH ALGORITHM")) {
				errMsg = sftp.login();
			}

			if (errMsg == null || "".equals(errMsg)) {
				isLogin = true;
			}
		} else if (errMsg == null || "".equals(errMsg)) {
			isLogin = true;
		}

		// //// FILE TRANSFER //////////
		logger.info("FILE COUNT :" + files.length);
		// agentCommlogger.info("FILE COUNT :" + files.length);

		if (isLogin) {
			for (File f : files) {
				lastUploadedPath = sftp.uploadFile(remotePath, f);
				if (!handleUploadStatus(lastUploadedPath)) {
					returnHash.put("RESULT", "false");
					returnHash.put("UPLOADPATH", "");
					returnHash.put("ERROR", lastUploadedPath);
					sftp.logout();
					return returnHash;
				}

				agentInfo.setBfaShellFile(f.getName());
				agentInfo.setLastUploadedPath(lastUploadedPath + "/");
			}
			if (lastUploadedPath.endsWith(INMEMORYDB.PATH_SLASH_UNIX)
					|| lastUploadedPath.endsWith(INMEMORYDB.PATH_SLASH_WIN)) {
			} else {
				if (agentInfo.getOsType().toUpperCase()
						.contains(INMEMORYDB.WIN_ID)) {
					lastUploadedPath = lastUploadedPath
							+ INMEMORYDB.PATH_SLASH_WIN;
				} else {
					lastUploadedPath = lastUploadedPath
							+ INMEMORYDB.PATH_SLASH_UNIX;
				}
			}
			returnHash.put("RESULT", "true");
			returnHash.put("UPLOADPATH", lastUploadedPath);
			returnHash.put("ERROR", "");
			sftp.logout();

			return returnHash;
		} else {
			logger.debug("SFTP LOGIN FAILED.!");
			// agentCommlogger.debug("SFTP LOGIN FAILED.!");
			returnHash.put("RESULT", "false");
			returnHash.put("UPLOADPATH", "");
			returnHash.put("ERROR", errMsg);
			JobEntity je = new JobEntity();
			agentInfo.setConnectLog("SFTP LOGIN FAILED." + " ["
					+ DateUtil.getCurrDateBySecondFmt() + "]");
			je.setAgentInfo(agentInfo);
//			dao.updateConnectMaster(je); Update ConnectMastare After trying @SSH_TOOLS  
			sftp.logout();
			return returnHash;
		}
	}

	public HashMap<String, String> uploadViaSFTP_SshTools(AgentInfo agentInfo,
                                                          String remotePath, File[] files) throws Exception {

		HashMap<String, String> returnHash = new HashMap<String, String>();
		boolean isLogin = false;
		String errMsg = "";
		String lastUploadedPath = "";

		SFTPHandler sftp = new SFTPHandler(agentInfo);

		// ///// LOGIN //////////

		errMsg = sftp.login_SshTools();

		// //// FILE TRANSFER //////////
		logger.info("FILE COUNT :" + files.length);

		if (errMsg != null && errMsg.contains("UNKNOWNHOST")) {
			logger.debug("RECONNECT BY UNKNOWNHOST EXCEPTION.");
			// agentCommlogger.info("RECONNECT BY UNKNOWNHOST EXCEPTION.");
			// sftp = new SFTPHandler(agentInfo);
			errMsg = sftp.login();
			if (errMsg == null || "".equals(errMsg)) {
				isLogin = true;
			}
		} else if (errMsg == null || "".equals(errMsg)) {
			isLogin = true;
		}

		if (isLogin) {
			for (File f : files) {
				lastUploadedPath = sftp.uploadFile_SshTools(remotePath, f);
				if (!handleUploadStatus(lastUploadedPath)) {
					returnHash.put("RESULT", "false");
					returnHash.put("UPLOADPATH", "");
					returnHash.put("ERROR", lastUploadedPath);
					sftp.logout();
					return returnHash;
				}

				agentInfo.setBfaShellFile(f.getName());
				agentInfo.setLastUploadedPath(lastUploadedPath + "/");
			}
			if (lastUploadedPath.endsWith(INMEMORYDB.PATH_SLASH_UNIX)
					|| lastUploadedPath.endsWith(INMEMORYDB.PATH_SLASH_WIN)) {
			} else {
				if (agentInfo.getOsType().toUpperCase()
						.contains(INMEMORYDB.WIN_ID)) {
					lastUploadedPath = lastUploadedPath
							+ INMEMORYDB.PATH_SLASH_WIN;
				} else {
					lastUploadedPath = lastUploadedPath
							+ INMEMORYDB.PATH_SLASH_UNIX;
				}
			}
			returnHash.put("RESULT", "true");
			returnHash.put("UPLOADPATH", lastUploadedPath);
			returnHash.put("ERROR", "");
			sftp.logout();

			return returnHash;
		} else {
			logger.debug("SFTP LOGIN FAILED.!");
			// agentCommlogger.debug("SFTP LOGIN FAILED.!");
			returnHash.put("RESULT", "false");
			returnHash.put("UPLOADPATH", "");
			returnHash.put("ERROR", errMsg);
			JobEntity je = new JobEntity();
			agentInfo.setConnectLog("SFTP LOGIN FAILED." + " ["
					+ DateUtil.getCurrDateBySecondFmt() + "]");
			je.setAgentInfo(agentInfo);
			dao.updateConnectMaster(je);
			sftp.logout();
			return returnHash;
		}
	}

	public HashMap<String, String> uploadViaSFTP_Proxy(AgentInfo agentInfo, AgentInfo relayAgentInfo,
                                                       String remotePath, File[] files) throws Exception {

		HashMap<String, String> returnHash = new HashMap<String, String>();
		boolean isLogin = false;
		String errMsg = "";
		String lastUploadedPath = "";

		SFTPHandler sftp = new SFTPHandler(agentInfo);

		// ///// LOGIN //////////

		errMsg = sftp.login_Proxy(relayAgentInfo.getConnectIpAddress(),relayAgentInfo.getPortSftp().toString(),relayAgentInfo.getUserIdOs(),relayAgentInfo.getPasswordOs());

		if (errMsg != null && errMsg.contains("UNKNOWNHOST")) {
			logger.debug("RECONNECT BY UNKNOWNHOST EXCEPTION.");
			errMsg = sftp.login_Proxy(relayAgentInfo.getConnectIpAddress(),relayAgentInfo.getPortSftp().toString(),relayAgentInfo.getUserIdOs(),relayAgentInfo.getPasswordOs());
			if (errMsg == null || "".equals(errMsg)) {
				isLogin = true;
			}
		} else if (errMsg == null || "".equals(errMsg)) {
			isLogin = true;
		}

		// //// FILE TRANSFER //////////
		logger.info("FILE COUNT :" + files.length);

		if (isLogin) {
			for (File f : files) {
				lastUploadedPath = sftp.uploadFile_Proxy(remotePath, f);
				if (!handleUploadStatus(lastUploadedPath)) {
					returnHash.put("RESULT", "false");
					returnHash.put("UPLOADPATH", "");
					returnHash.put("ERROR", lastUploadedPath);
					sftp.logout_Proxy();
					return returnHash;
				}

				agentInfo.setBfaShellFile(f.getName());
				agentInfo.setLastUploadedPath(lastUploadedPath + "/");
			}
			if (lastUploadedPath.endsWith(INMEMORYDB.PATH_SLASH_UNIX)
					|| lastUploadedPath.endsWith(INMEMORYDB.PATH_SLASH_WIN)) {
			} else {
				if (agentInfo.getOsType().toUpperCase()
						.contains(INMEMORYDB.WIN_ID)) {
					lastUploadedPath = lastUploadedPath
							+ INMEMORYDB.PATH_SLASH_WIN;
				} else {
					lastUploadedPath = lastUploadedPath
							+ INMEMORYDB.PATH_SLASH_UNIX;
				}
			}
			returnHash.put("RESULT", "true");
			returnHash.put("UPLOADPATH", lastUploadedPath);
			returnHash.put("ERROR", "");
			sftp.logout();

			return returnHash;
		} else {
			logger.debug("SFTP LOGIN FAILED.!");
			returnHash.put("RESULT", "false");
			returnHash.put("UPLOADPATH", "");
			returnHash.put("ERROR", errMsg);
			JobEntity je = new JobEntity();
			agentInfo.setConnectLog("SFTP LOGIN FAILED." + " ["
					+ DateUtil.getCurrDateBySecondFmt() + "]");
			je.setAgentInfo(agentInfo);
			dao.updateConnectMaster(je);
			sftp.logout();
			return returnHash;
		}
	}

	/**
	 * RETURN: HashMap<String,String> returnHash returnHash.get("RESULT") =
	 * "true"/"false" returnHash.get("UPLOADPATH") = "Uploaded Path"/""
	 * returnHash.get("ERROR") = "Error message"/""
	 * **/

	public HashMap<String, String> uploadViaFTP(AgentInfo agentInfo,
                                                String path, File[] files) {
		return uploadViaFTP(agentInfo, path, files, true);
	}

	public HashMap<String, String> uploadViaFTP(AgentInfo agentInfo, String path, File[] files, boolean isPassiveMode) {

		HashMap<String, String> returnHash = new HashMap<String, String>();
		boolean isLogin = false;

		String connId = agentInfo.getUserIdOs();
		String connPw = agentInfo.getPasswordOs();

		if (connId == null || connPw == null || "".equals(connPw)
				|| "".equals(connId)) {
			connId = agentInfo.getUserIdRoot();
			connPw = agentInfo.getPasswordRoot();
		}

		FTPHandler ftp = new FTPHandler(agentInfo.getConnectIpAddress(),
				agentInfo.getPortFtp(), agentInfo.getUserIdOs(),
				agentInfo.getPasswordOs());

		try {
			try {
				isLogin = ftp.login(isPassiveMode);
				
			} catch (Throwable e) {
				logger.error("== ftp login " , e);
				returnHash.put("RESULT", "false");
				returnHash.put("UPLOADPATH", "");
				returnHash.put("ERROR", e.getMessage());
				return returnHash;
			}
			
			logger.info("== uploadViaFtp agent:{} isLogin:{}  isPassive:{}", new Object[]{agentInfo.getConnectIpAddress(), isLogin, isPassiveMode});
			
			if (isLogin) {
				returnHash = doLoadFiles(agentInfo, path, files, ftp);
				if ("false".equals(returnHash.get("RESULT")) && isPassiveMode) {
					logger.info("UPLOAD FAILED VIA PASSIVE MODE & RETRY WITH ACITVE MODE.");
					returnHash = uploadViaFTP(agentInfo, path, files, false);
				}
				return returnHash;
				
			} else {
				logger.debug("FTP LOGIN FAILED.");
				returnHash.put("RESULT", "false");
				returnHash.put("UPLOADPATH", "");
				returnHash.put("ERROR", "FTP LOGIN FAILED.");
				
				if (isPassiveMode) {
					logger.info("UPLOAD FAILED VIA PASSIVE MODE & RETRY WITH ACITVE MODE.");
					returnHash = uploadViaFTP(agentInfo, path, files, false);
				}
				
				return returnHash;
			}
			
		} finally {
			ftp.logout();
		}
		
		

	}

	private HashMap<String, String> doLoadFiles(AgentInfo agentInfo,
                                                String path, File[] files, FTPHandler ftp) {
		String lastUploadedPath = "";
		HashMap<String, String> returnHash = new HashMap<String, String>();
		for (File f : files) {
			lastUploadedPath = ftp.uploadFile(path, f);

			if (!handleUploadStatus(lastUploadedPath)) {
				returnHash.put("RESULT", "false");
				returnHash.put("UPLOADPATH", "");
				returnHash.put("ERROR", lastUploadedPath);
				agentInfo.setAgentRegiFlag(3);
				agentInfo.setSetupStatus(lastUploadedPath);
				ftp.logout();
				return returnHash;
			}

			if (lastUploadedPath.endsWith(INMEMORYDB.PATH_SLASH_UNIX)
					|| lastUploadedPath.endsWith(INMEMORYDB.PATH_SLASH_WIN)) {
			} else {
				if (agentInfo.getOsType().toUpperCase()
						.contains(INMEMORYDB.WIN_ID)) {
					lastUploadedPath = lastUploadedPath
							+ INMEMORYDB.PATH_SLASH_WIN;
				} else {
					lastUploadedPath = lastUploadedPath
							+ INMEMORYDB.PATH_SLASH_UNIX;
				}
			}

			returnHash.put("RESULT", "true");
			returnHash.put("UPLOADPATH", lastUploadedPath);
			returnHash.put("ERROR", "");

			agentInfo.setBfaShellFile(f.getName());
			if (lastUploadedPath.equals("/" + INMEMORYDB.GETSCRIPT_RECV_FILE_BF_AGENT_DIR + "/") && agentInfo.getUserIdOs().equals("root")) {
				lastUploadedPath = "/" + agentInfo.getUserIdOs() + "/" + INMEMORYDB.GETSCRIPT_RECV_FILE_BF_AGENT_DIR;
			}
			if (!lastUploadedPath.endsWith("/")) {
				agentInfo.setLastUploadedPath(lastUploadedPath + "/");
			} else {
				agentInfo.setLastUploadedPath(lastUploadedPath);
			}
		}
		ftp.logout();
		logger.debug("FTP SESSION LOGGED OUT After Successful Upload!");
		return returnHash;

	}

	private boolean handleUploadStatus(String lastUploadedPath) {

		if (!lastUploadedPath.contains("ERROR")) {
			logger.info("SUCCESSFULLY UPLOADED AT " + lastUploadedPath);
			return true;
		} else {
			logger.error(lastUploadedPath);
			return false;
		}
	}

	public String runRemoteScriptViaTelnet(AgentInfo ai, String cmd)
			throws Exception {

		TelnetHandler instance = new TelnetHandler(ai);
		boolean conn = false;

		try {
			conn = instance.connectWlogin();
			conn = true;
		} catch (Exception e) {
			e.printStackTrace();
			instance.close();
			return "TELNET LOGIN FAILURE: " + e.getMessage();
		}

		if (conn) {

			logger.info("RUN TELNET CMD :" + cmd);
			String result = instance.exeCommand(cmd);
			logger.info("SUCCESSFULLY RUN TELNET CMD:" + cmd
					+ " COMMAND RESULT:" + result);
			instance.close();
			return result;
		} else {
			instance.close();
			throw new Exception("ERROR CAUSED BY TELNET LOGIN FAILURE");
		}

	}

	public String verifyPromptViaTelnet(AgentInfo agentInfo) throws Exception {

		String cmds = "";

		String prmpt = runRemoteScriptViaTelnet(agentInfo, cmds);

		return prmpt;
	}

	@SuppressWarnings("unused")
	public String verifyRootPromptViaTelnet(AgentInfo ai) throws Exception {

		String cmds = "";

		String prmpt = runRemoteScriptViaTelnet_Su(ai, ai.getUserIdRoot(),
				ai.getPasswordRoot(), "");

		return prmpt;
	}

	public String getHomeDirViaTelnet(AgentInfo agentInfo) throws Exception {

		String cmds = "cd ~;pwd\n";
		String slash = INMEMORYDB.PATH_SLASH_UNIX;
		if (agentInfo.getOsType().toUpperCase().contains(INMEMORYDB.WIN_ID)) {
			slash = INMEMORYDB.PATH_SLASH_WIN;
			cmds = "cd\n";
		}
		String result = runRemoteScriptViaTelnet(agentInfo, cmds);

		if (result.toUpperCase().contains("ERROR")
				|| result.toUpperCase().contains("FAILURE") || result == null
				|| "".equals(result)) {
			return "";
		} else {
			if (result.endsWith("/") || result.endsWith("\\")) {
			} else {
				result = result + slash;
			}
			return result;
		}

	}

	public String[] verifyRootPromptViaSSH(AgentInfo ai) throws Exception {
		String result[] = new String[2];
		result[0] = "";
		result[1] = "";
		boolean isConnected = false;
		SSHHandler instance = new SSHHandler(ai);
		String errMsg = instance.connect();
		if (errMsg != null && errMsg.contains("UNKNOWNHOST")) {
			logger.debug("RECONNECT BY UNKNOWNHOST EXCEPTION.");
			// instance = new SSHHandler(ai);
			errMsg = instance.connect();
			if (errMsg == null) {
				isConnected = true;
			}
		} else if (errMsg == null) {
			isConnected = true;
		}

		if (isConnected) {
			logger.info("RUN REMOTE SSH CMD :");
			result = instance.findPromptPhrase(ai.getUserIdRoot(),
					ai.getPasswordRoot());

			instance.close();
			return result;
		} else {
			instance.close();
			logger.error("ERROR CAUSED BY SSH LOGIN FAILURE " + errMsg);
		}
		return result;

	}

	public String runRemoteScriptViaTelnet_Su(AgentInfo ai, String suId,
                                              String suPw, String cmd) throws Exception {

		TelnetHandler instance = new TelnetHandler(ai);
		boolean conn = false;

		try {
			conn = instance.connectWlogin();
			conn = true;
		} catch (Exception e) {
			instance.close();
			return "TELNET(SU) LOGIN FAILURE: " + e.getMessage();
		}

		if (conn) {
			logger.debug("----------------------- START DOSU -------------------------");
			boolean isSUDone = instance.doSu(suId, suPw);
			logger.debug("----------------------- END DOSU :" + isSUDone
					+ " -------------------------");
			if (!isSUDone) {
				instance.close();
				return "TELNET(SU) FAILURE: PLEASE CHECK ROOT ID="
						+ ai.getUserIdRoot() + " AND PROFER PASSWORD.";
			} else {
				logger.info("@@@ RUN TELNET (SU)CMD :" + cmd);
				String result = instance.exeCommand(cmd);
				logger.info("SUCCESSFULLY RUN TELNET (SU)CMD:" + cmd
						+ " COMMAND RESULT:" + result);
				instance.close();
				return result;
			}

		} else {
			instance.close();
			throw new Exception("ERROR CAUSED BY TELNET(SU) LOGIN FAILURE");
		}

	}

	public String runRemoteScripNWTelnet_Su(AgentInfo ai, String cmd)
			throws Exception {

		TelnetHandler instance = new TelnetHandler(ai);
		boolean conn = false;

		try {
			conn = instance.connectWlogin();
			conn = true;
		} catch (Exception e) {
			instance.close();
			return "TELNET LOGIN FAILURE: " + e.getMessage();
		}

		if (conn) {
			String result = instance.exeCommand("\n");
			logger.info("SUCCESSFULLY RUN TELNET (SU)CMD:" + cmd
					+ " COMMAND RESULT:" + result);
			instance.close();
			return result;
		} else {
			instance.close();
			throw new Exception("ERROR CAUSED BY TELNET(SU) LOGIN FAILURE");
		}
	}

	// public String runRemoteScriptViaSSH(AgentInfo ai, String cmd) throws
	// Exception {
	public String runRemoteScriptViaSSH(AgentInfo ai, String cmd) {
		String result = "";
		boolean isConnected = false;
		SSHHandler instance = new SSHHandler(ai);
		String errMsg = instance.connect();
		if (errMsg != null && errMsg.contains("UNKNOWNHOST")) {
			instance.close();
			logger.debug("RECONNECT BY UNKNOWNHOST EXCEPTION.");
			instance = new SSHHandler(ai);
			errMsg = instance.connect();
			if (errMsg == null) {
				isConnected = true;
			}
		} else if (errMsg == null) {
			isConnected = true;
		}

		if (isConnected) {

			result = instance.sendCommand(cmd);

			if (result != null
					&& result.toUpperCase().contains("[COMMAND_FAILURE]")) {
				logger.info("FAILED RUN SSH(NO-SU) CMD [RUN-CMD]:" + cmd
						+ " CMD RESULT:" + result);
			} else {
				logger.info("SUCCESSFULLY RUN SSH(NO-SU) CMD:" + cmd
						+ " CMD RESULT:" + result);
			}

			instance.close();
			return result;
		} else {
			instance.close();
			return "[CONNECTION_FAILURE] CHECK ID/PASSWORD FOR CONNECT_IP:"
					+ ai.getConnectIpAddress() + " CONNECT_ID:"
					+ ai.getUserIdOs();

		}

	}


	/**
	 * loginType; 
	 *  CR[0] : conID&RootID 
	 *  RO[1] : RootID only 
	 *  RSA[2]: use RSA 
	 *  LC[3]: Line Command 
	 *  PEM[4]: use PEM //개발요건 없음
	 **/
	public String runRemoteScriptViaSSH_Su(AgentInfo ai, String cmd) {
		boolean isConnected = false;
		String result = null;
		SSHHandler instance = null;

		logger.debug("AGENT LOGIN INFO : LOGIN TYPE:" + ai.getLoginType() + "  LOGIN TYPE INT:" + ai.getLoginTypeInt()
				+"SU TYPE :"+ai.getUseSudo() +" IP:"+ai.getConnectIpAddress());
				

		if (ai.getLoginTypeInt() == 0) {
			instance = new SSHHandler(ai);
		} else {
			instance = new SSHHandler(ai, ai.getLoginTypeInt());
		}

		/**************  user id connect ****************/ 
		
		String errMsg = null;
		
		switch(ai.getLoginTypeInt()) {
		case 0: //CR[0] : conID&RootID
			errMsg = instance.connect(ai.getLoginTypeInt());
			if (errMsg != null && errMsg.contains("UNKNOWNHOST")) {
				logger.debug("RECONNECT BY UNKNOWNHOST EXCEPTION.");
				instance.close();
				instance = new SSHHandler(ai);
				errMsg = instance.connect();
				if (errMsg == null) {
					isConnected = true;
				}
			} else if (errMsg == null) {
				isConnected = true;
			}
			break;
		case 1: //RO[1] : RootID only
			errMsg = instance.connect(ai.getLoginTypeInt());
			if (errMsg != null && errMsg.contains("UNKNOWNHOST")) {
				logger.debug("RECONNECT BY UNKNOWNHOST EXCEPTION.");
				instance.close();
				instance = new SSHHandler(ai);
				errMsg = instance.connect();
				if (errMsg == null) {
					isConnected = true;
				}
			} else if (errMsg == null) {
				isConnected = true;
			}
			break;
		case 3: //LC[3]: Line Command 
			// GENERAL CASE CONNECT
						errMsg = instance.connect(ai.getLoginTypeInt());
						if (errMsg != null && errMsg.contains("UNKNOWNHOST")) {
							logger.debug("RECONNECT BY UNKNOWNHOST EXCEPTION.");
							instance.close();
							instance = new SSHHandler(ai);
							errMsg = instance.connect();
							if (errMsg == null) {
								isConnected = true;
							}
						} else if (errMsg == null) {
							isConnected = true;
						}
						break;
		case 2:	
			// RSA TYPE CONNECT RSA인증서 로 로그인 !
						errMsg = instance.connect(ai.getLoginTypeInt());
						if (errMsg != null && errMsg.contains("UNKNOWNHOST")) {
							logger.debug("RECONNECT BY UNKNOWNHOST EXCEPTION.");
							instance.close();
							instance = new SSHHandler(ai, ai.getLoginTypeInt());
							errMsg = instance.connect(ai.getLoginTypeInt());
							if (errMsg == null) {
								isConnected = true;
							}
						} else if (errMsg == null) {
							isConnected = true;
						}
						break;
		case 4:
			//PEM 로그인은 요구사항 없음.
						result = "[CONNECTION_FAILURE] PEM Login IS NOT Availble.-NOT SUPPORTED.";
						logger.error(result);
						
						return result;
		default:
						result = "[CONNECTION_FAILURE] LOGIN TYPE IS NOT UNDIFINED.!!";
						logger.error(" 로그인 타입이 지정되지 않았습니다. ai.getLoginTypeInt()=" +ai.getLoginTypeInt() );
						return result;
		}
		
		
		/**************  su & exe command  ****************/

			//
			String suId = ai.getUserIdRoot();
			String suPw = ai.getPasswordRoot();
    		//0: 'sudo' not supported.  1: use 'sudo'   ||    LC[3]: Line Command
			if("Y".equalsIgnoreCase(ai.getUseSudo()) || ai.getLoginTypeInt() == 3)
			{
		    	 suPw = ai.getPasswordOs();
		    }
			if(ai.getLoginTypeInt() == 3)
			{
				//Login: sv_admin + Password:xxxxx + sv_enable sv_admin + su -
				suId = ai.getUserIdOs();
		    }
		
		if(!isConnected){			

			if (errMsg != null && (errMsg.toUpperCase().contains("FAILURE") || errMsg.toUpperCase().contains("ERROR")|| errMsg.toUpperCase().contains("EXCEPTION"))
					&& ai.getLoginTypeInt() ==0) {
				logger.debug("Retry WITH SshTools DUE TO ' "+ errMsg + " '");
				result = runRemoteScriptViaSSH_Su_sshTools(ai, cmd);
				return result;
				
			}
			else{
				instance.close();	
				result = "[CONNECTION_FAILURE] CHECK ID/PASSWORD FOR CONNECT_IP:"
						+ ai.getConnectIpAddress() + " CONNECT_ID:"
						+ ai.getUserIdOs(); 
				return result;
			}
				
		}else{
			    	
		    		//LC[3]: Line Command
			    	if(ai.getLoginTypeInt() == 3){		    					    		
			    		suId = ai.getUserIdOs();			    		
			    		logger.info("[" + ai.getConnectIpAddress()
								+ "] RUN REMOTE SSH(SU) CMD :" + cmd +" with logintype:"+ai.getLoginTypeInt()
								+"ai.getUseSudo():"+ai.getUseSudo()+" ai.getCmdSu():"+ ai.getCmdSu() + " suId:"+suId);
			    		result = instance.suSendCommand(suId,suPw, cmd, ai.getLoginTypeInt(), ai.getUseSudo(),ai.getCmdSu());
				    	
				    }else{
				    	logger.info("[" + ai.getConnectIpAddress()
								+ "] RUN REMOTE SSH(SU) CMD :" + cmd +" with logintype:"+ai.getLoginTypeInt()
								+"ai.getUseSudo():"+ai.getUseSudo()+" ai.getCmdSu():"+ ai.getCmdSu() + " suId:"+suId);
				    	result = instance.suSendCommand(suId,suPw, cmd, ai.getLoginTypeInt(), ai.getUseSudo());			    	
				    }
			    
						
			if (result != null
					&& result.toUpperCase().contains("[CONNECTION_FAILURE]")) {
				logger.info("FAILED RUN SSH(SU) CMD [CON]:" + cmd
						+ " CMD RESULT:" + result);

				logger.debug("Retry SshTools");
				result = runRemoteScriptViaSSH_Su_sshTools(ai, cmd);

			} else if (result != null
					&& result.toUpperCase().contains("[COMMAND_FAILURE]")) {
				logger.info("FAILED RUN SSH(SU) CMD [RUN-CMD]:" + cmd
						+ " CMD RESULT:" + result);
			} else {
				logger.info("SUCCESSFULLY RUN SSH(SU) CMD:" + cmd
						+ " CMD RESULT:" + result);
			}

			instance.close();

			return result;
		}
	}


	@SuppressWarnings("unused")
	public String runRemoteScriptViaSSH_Su_sshTools(AgentInfo ai, String cmd) {
		boolean isConnected = false;
		String result = "";

		SSHHandler instance;
		boolean isCon = false;
		try {
			instance = new SSHHandler(ai, "USE_SSHTOOLS");
			isCon = instance.isSshToolsCon;
		} catch (Exception e) {
			logger.error("[CONNECTION_FAILURE][SSHT] CHECK ID/PASSWORD FOR "
					+ ai.getUserIdOs() + " ERRMSG:" + e.getMessage());
			return "[CONNECTION_FAILURE][SSHT] CHECK ID/PASSWORD FOR "
					+ ai.getUserIdOs() + " ERRMSG:" + e.getMessage();
		}

		logger.debug("***** Retry sshTools Connections *****");
		if (isCon) {

			logger.info("[" + ai.getConnectIpAddress()
					+ "] RUN REMOTE SSH(SU) CMD :" + cmd);
			result = instance.suSendCommand_sshTools(ai.getUserIdRoot(),
					ai.getPasswordRoot(), cmd, ai.getUseSudo());
		} else {
			result = "[CONNECTION_FAILURE][SSHT] CHECK ID/PASSWORD FOR "
					+ ai.getUserIdOs();
			return result;
		}

		if (result != null
				&& result.toUpperCase().contains("[CONNECTION_FAILURE]")) {
			logger.info("FAILED RUN SSH(SU) CMD:" + cmd + " CMD RESULT:"
					+ result);

		} else {
			logger.info("SUCCESSFULLY RUN SSH(SU) CMD:" + cmd + " CMD RESULT:"
					+ result);

		}

		return result;
	}

	public String runRemoteScriptViaSSH_Su_sshToolsProxy(AgentInfo ai, AgentInfo relayInfo, String cmd) throws Exception {
		
		String result = "";

		SSHHandler instance;
		boolean isCon = false;
		try {
			instance = new SSHHandler(ai, relayInfo,"USE_SSHTOOLS", "USE_PROXY");
			isCon = instance.isSshToolsCon;
		} catch (Exception e) {
			logger.error("[CONNECTION_FAILURE][SSHT] CHECK ID/PASSWORD FOR "
					+ ai.getUserIdOs() + " ERRMSG:" + e.getMessage());
			return "[CONNECTION_FAILURE][SSHT] CHECK ID/PASSWORD FOR "
					+ ai.getUserIdOs() + " ERRMSG:" + e.getMessage();
		}

		logger.debug("***** proxy server sshTools Connections *****");
		if (isCon) {

			logger.info("[" + ai.getConnectIpAddress()
					+ "] RUN REMOTE SSH(SU) CMD :" + cmd);
			result = instance.suSendCommand_sshToolsProxy(ai.getUserIdRoot(),
					ai.getPasswordRoot(), cmd);
		} else {
			result = "[CONNECTION_FAILURE][SSHT] CHECK ID/PASSWORD FOR "
					+ ai.getUserIdOs();
			return result;
		}

		if (result != null
				&& result.toUpperCase().contains("[CONNECTION_FAILURE]")) {
			logger.info("FAILED RUN SSH(SU) CMD:" + cmd + " CMD RESULT:"
					+ result);

		} else {
			logger.info("SUCCESSFULLY RUN SSH(SU) CMD:" + cmd + " CMD RESULT:"
					+ result);

		}

		return result;
	}

	@SuppressWarnings("unused")
	public AgentInfo setupTestAI() {
		String IP = "";
		String CON_ID = "";
		String CON_PW = "";
		String ROOT_ID = "";
		String ROOT_PW = "";
		String OS = "";
		String PORT_SSH = "";
		String PORT_SFTP = "";
		String PORT_TEL = "";
		String PORT_FTP = "";

		AgentInfo ai = new AgentInfo();

		ai.setConnectIpAddress(IP);
		ai.setUserIdOs(CON_ID);
		ai.setPasswordOs(CON_PW);
		ai.setOsType(OS);
		ai.setUserIdRoot(ROOT_ID);
		ai.setPasswordRoot(ROOT_PW);
		ai.setPortSsh(Integer.parseInt(PORT_SSH));
		ai.setPortSftp(Integer.parseInt(PORT_SFTP));
		ai.setLoginType("RSA"); // CR[0]:conID&RootID, RO[1]:RootID only,
								// RSA[2]: use RSA , LC[3]: Line Command,
								// PEM[4]: Use Pem File
		ai.completeAgentInfo();
		return ai;
	}

	@SuppressWarnings("unused")
	public static void main(String args[]) {

		ConnectionManager cm = new ConnectionManager();
		String cmd = "pwd";
		try {
			AgentInfo ai = cm.setupTestAI();
			File f = new File("C:\\keytest\\mykey");
			cm.uploadOneFileViaSFTP(ai, "smUpload", f);

		} catch (Exception e) {
			System.out.println("ERRRRR ::::" + e.getMessage());
		}
	}
}
