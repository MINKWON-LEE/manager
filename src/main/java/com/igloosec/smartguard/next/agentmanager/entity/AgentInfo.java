/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.entity.AgentInfo.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 2. 4.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.entity;

import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.sk.snet.manipulates.EncryptUtil;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

public class AgentInfo {

	private String agentCd;

	private String assetCd;
	private String relayAssetCd;
	private String relay2AssetCd;
	private String relay3AssetCd;
	private String osType; // WINDOWS,LINUX,SOLARIS,UNIX,HP-UX,CISCO,FREEBSD,SOLARIS_SPARC
	private Integer osBit = 64; // 1:32bit / 2:64bit
	private String connectIpAddress;
	private String relayIpAddress;
	private String relay2IpAddress;
	private String relay3IpAddress;
	private String relaySubIpAddress;
	private String relayPort;
	private String userId = "-";
	private String password = "-";
	private String socketFilePath = "";
	private String userIdOs;
	private String userIdRoot;
	private String passwordOs;
	private String passwordRoot;
	private Integer accountChkOs;
	private Integer accountChkDba;
	private Integer accountChkUrl;
	private Integer accountChkRoot;
	private Integer accountChkWeb;
	private Integer accountChkWas;
	private Integer portSsh;
	private Integer portSftp;
	private Integer portTelnet;
	private Integer portFtp;
	private String connectLog = "";
	private String connectShellOs = "";
	private String connectShellRoot = "";
	private String promptUserIdOs = "";
	private String promptUserIdRoot = "";
	private String agentInstallPath = "";

	// Agent_Master
	private Integer agentType; // 1:Command , 2:Daemon , 3:Onetime
	private String agentInstallDate;
	private String agentStartUser;
	private String agentStartDate;
	private String agentUseSTime; // Available Time(From)
	private String agentUseETime; // Available Time(To)

	private int useFtpPort;
	private int usePort;

	//NW config
	private String nwGetConfigCmd;
	
	private String loginType; //CR:conID&RootID, RO:RootID only, RSA: use RSA , LC: Line Command, PEM: use PEM
	private int loginTypeInt = 0; //CR[0]:conID&RootID, RO[1]:RootID only, RSA[2]: use RSA , LC[3]: Line Command, PEM[4]: Use Pem File
	private String useSudo = "N"; //N:'sudo' not supported. Y:use 'sudo'


	private String pemFileLoc;
	private String cmdSu = "sv_enable";  //Login: sv_admin + Password:xxxxx + sv_enable sv_admin + su -
	private String[] cmdSuArr;

	private String channelType = ""; // 'S' = 'SSH & SFTP','TF' = 'TELNET & FTP'
										// ,'T' = 'TELNET' , 'F' = 'FTP' ,'N' =
										// 'NO PORT OPEN'
	private String[] commands;
	private String setupShellFile = ""; // setup Shell file Name
	private String setupStatus = "";
	private int agentRegiFlag = 1;// 1:SETUP REQ 2:SUCESS , 3:FAIL

	private String lastUploadedPath;
	private String bfaShellFile = ""; // before agent Shell file Name

	/*
	 * 2016.03.22 HealthCheck port_alive 추
	 */
	private int portSshAlive;
	private int portSftpAlive;
	private int portTelnetAlive;
	private int portFtpAlive;


	private String agentVersion;
	private String agentOldVersion;
	
	public static String slash = "/";
	
	private String isRelayManager = "N"; //N:No, Y:Yes

	public String getIsRelayManager() {
		return isRelayManager;
	}

	private String useDiagSudo;

	private String managerCd;

	// 3단계 페이지에서 추가되는 3개의 정보
	private String param1;

	private String param2;

	private String param3;

	private String branchId;
	private String branchNm;
	private String teamId;
	private String teamNm;

	private String dgUserId;

	private String dgUserNm;

	private String hostNm;

	private String jobType;

	public void setIsRelayManager(String isRelayManager) {
		this.isRelayManager = isRelayManager;
	}

	public void completeAgentInfo() {
		setUsePorts_channelType();
		setDecryptedPass();
		chkConnIdPw();
		chkUseSudo();
		chkParams();

		if (osType.toUpperCase().contains(INMEMORYDB.WIN_ID)) {
			slash = INMEMORYDB.PATH_SLASH_WIN;
		}

		this.socketFilePath = (socketFilePath.equalsIgnoreCase("null") || socketFilePath.equalsIgnoreCase("") || socketFilePath == null) ? "-" : socketFilePath;
	}

	public void chkParams() {
		if (this.param1 == null || this.param1.isEmpty()) {
			this.param1 = "-";
		}
		if (this.param2 == null || this.param2.isEmpty()) {
			this.param2 = "-";
		}
		if (this.param3 == null || this.param3.isEmpty()) {
			this.param3 = "-";
		}
	}
		
	public void chkUseSudo(){
		//현재 RSA 인증서 로그인시 sudo 로 su 하도록 설정됨.
//		if(this.loginTypeInt == 2){
//			this.useSudo = "Y";
//		}
	} 

		public void chkConnIdPw(){
//			this.loginType = "RSA";
			 //로그인 타입이 지정되지 않았을때, 기본타입 os ID & root ID 접속으로 시도(CR) 로 접속
			  if(this.loginType == null || "".equals(this.loginType)|| "CR".equalsIgnoreCase(this.loginType)){
				  this.loginType = "CR";
				  this.loginTypeInt = 0;
				  return;
			  }

			  if("RO".equalsIgnoreCase(this.loginType)){					
					this.loginTypeInt = 1;
				 return;	
			  }
			  if("RSA".equalsIgnoreCase(this.loginType)){					
					this.loginTypeInt = 2;
				 return;
			  }
			  if("LC".equalsIgnoreCase(this.loginType)){					
					this.loginTypeInt = 3;
				 return;	
			  }
			  if("PEM".equalsIgnoreCase(this.loginType)){					
					this.loginTypeInt = 4;
				 return;
			  }
				// when ID/Pw for Connection is null then use Root's 
			  if((userIdOs == null || passwordOs == null) && ( !"".equals(userIdRoot) && !"".equals(passwordRoot))){
					this.userIdOs  = userIdRoot;
					this.passwordOs = passwordRoot;
					this.loginType = "RO";
					this.loginTypeInt = 1;
				 return;	
			  }
			  this.loginTypeInt = 0;
			  return;
			
		}
		
		
		
		public void setDecryptedPass(){
			
			try {
				EncryptUtil eda = new EncryptUtil();
				
				if(passwordOs != null && !"".equals(passwordOs) && passwordOs.length() > 15){					
					this.passwordOs = eda.decrypt(passwordOs);					
				  }
				if(password != null && !"".equals(password) && password.length() > 15){
					this.password = eda.decrypt(password);
				  }
//				if(passwordUrl != null && !"".equals(passwordUrl) && passwordUrl.length() > 15){
//					this.passwordUrl = eda.decrypt(passwordUrl);
//				  }
				if(passwordRoot != null && !"".equals(passwordRoot) && passwordRoot.length() > 15){
					this.passwordRoot = eda.decrypt(passwordRoot);	
				  }
//				if(passwordWeb != null && !"".equals(passwordWeb) && passwordWeb.length() > 15){
//					this.passwordWeb = eda.decrypt(passwordWeb);
//				  }
//				if(passwordWas != null && !"".equals(passwordWas) && passwordWas.length() > 15){
//					this.passwordWas = eda.decrypt(passwordWas);
//				  }
			} catch (UnsupportedEncodingException | GeneralSecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}

	public String getRelayAssetCd() {
		return relayAssetCd;
	}

	public void setRelayAssetCd(String relayAssetCd) {
		this.relayAssetCd = relayAssetCd;
	}
	
	public String getRelay2AssetCd() {
		return relay2AssetCd;
	}

	public void setRelay2AssetCd(String relayAssetCd) {
		this.relay2AssetCd = relayAssetCd;
	}
	
	public String getRelay3AssetCd() {
		return relay3AssetCd;
	}

	public void setRelay3AssetCd(String relayAssetCd) {
		this.relay3AssetCd = relayAssetCd;
	}

	/**
	 * @return the agentCd
	 */
	public String getAgentCd() {
		return agentCd;
	}

	/**
	 * @param agentCd
	 *            the agentCd to set
	 */
	public void setAgentCd(String agentCd) {
		this.agentCd = agentCd;
	}

	public void setUsePorts_channelType() {
		if (this.portSftp == null) {
			this.portSftp = 0;
		}
		if (this.portFtp == null) {
			this.portFtp = 0;
		}
		if (this.portSsh == null) {
			this.portSsh = 0;
		}
		if (this.portTelnet == null) {
			this.portTelnet = 0;
		}

		String chT = "";
		String chF = "";

		chT = (this.portSsh != 0) ? "S" : ((this.portTelnet != 0) ? "T" : "");
		chF = (this.portSftp != 0) ? "H" : ((this.portFtp != 0) ? "F" : "");
		if (chT.equals("S"))
			this.usePort = this.portSsh;
		if (chT.equals("T"))
			this.usePort = this.portTelnet;
		if (chF.equals("H"))
			this.useFtpPort = this.portSftp;
		if (chF.equals("F"))
			this.useFtpPort = this.portFtp;

		this.channelType = chT + chF;

		if ("".equals(this.channelType)) {
			this.channelType = "N";
			this.useFtpPort = 0;
			this.usePort = 0;
		}

		if(relayIpAddress != null)
			this.channelType = "PR";

	}

	public String getNwGetConfigCmd() {
		return nwGetConfigCmd;
	}

	public void setNwGetConfigCmd(String nwGetConfigCmd) {
		this.nwGetConfigCmd = nwGetConfigCmd;
	}

	public String getLoginType() {
		return loginType;
	}

	public void setLoginType(String loginType) {
		this.loginType = loginType;
	}

	
	public int getLoginTypeInt() {
		return loginTypeInt;
	}
	public void setLoginTypeInt(int loginTypeInt) {
		this.loginTypeInt = loginTypeInt;
	}
	
	public String getUseSudo() {
		//N: 'sudo' not supported.     Y: use 'sudo'
		return this.useSudo;
	}

	public void setUseSudo(String useSudo) {
		//N: 'sudo' not supported.     Y: use 'sudo'
		this.useSudo = useSudo;
	}

	
	public String getCmdSu() {
		return cmdSu;
	}

	public void setCmdSu(String cmdSu) {
		this.cmdSu = cmdSu;
	}

	public String[] getCmdSuArr() {
		return cmdSuArr;
	}
	public void setCmdSuArr(String[] cmdSuArr) {
		this.cmdSuArr = cmdSuArr;
	}


	public String getPemFileLoc() {
		return pemFileLoc;
	}

	public void setPemFileLoc(String pemFileLoc) {
		this.pemFileLoc = pemFileLoc;
	}

	/**
	 * @return the assetCd
	 */
	public String getAssetCd() {
		return assetCd;
	}

	/**
	 * @param assetCd
	 *            the assetCd to set
	 */
	public void setAssetCd(String assetCd) {
		this.assetCd = assetCd;
	}

	/**
	 * @return the osType
	 */
	public String getOsType() {
		return osType;
	}

	/**
	 * @param osType
	 *            the osType to set
	 */
	public void setOsType(String osType) {
		this.osType = osType;
	}

	/**
	 * @return the osBit
	 */
	public Integer getOsBit() {
		return osBit;
	}

	/**
	 * @param osBit
	 *            the osBit to set
	 */
	public void setOsBit(Integer osBit) {
		this.osBit = osBit;
	}

	public String getRelayPort() {
		return relayPort;
	}

	public void setRelayPort(String relayPort) {
		this.relayPort = relayPort;
	}

	public String getRelayIpAddress() {
		return relayIpAddress;
	}

	public void setRelayIpAddress(String relayIpAddress) {
		this.relayIpAddress = relayIpAddress;
	}
	
	public String getRelay2IpAddress() {
		return relay2IpAddress;
	}

	public void setRelay2IpAddress(String relayIpAddress) {
		this.relay2IpAddress = relayIpAddress;
	}
	
	public String getRelay3IpAddress() {
		return relay3IpAddress;
	}

	public void setRelay3IpAddress(String relayIpAddress) {
		this.relay3IpAddress = relayIpAddress;
	}
	
	public String getRelaySubIpAddress() {
		return relaySubIpAddress;
	}

	public void setRelaySubIpAddress(String relaySubIpAddress) {
		this.relaySubIpAddress = relaySubIpAddress;
	}


	/**
	 * @return the connectIpAddress
	 */
	public String getConnectIpAddress() {
		return connectIpAddress;
	}

	/**
	 * @param connectIpAddress
	 *            the connectIpAddress to set
	 */
	public void setConnectIpAddress(String connectIpAddress) {
		this.connectIpAddress = connectIpAddress;
	}

	/**
	 * @return the userIdOs
	 */
	public String getUserIdOs() {
		return userIdOs;
	}

	/**
	 * @param userIdOs
	 *            the userIdOs to set
	 */
	public void setUserIdOs(String userIdOs) {
		this.userIdOs = userIdOs;
	}

	/**
	 * @return the userIdRoot
	 */
	public String getUserIdRoot() {
		return userIdRoot;
	}

	/**
	 * @param userIdRoot
	 *            the userIdRoot to set
	 */
	public void setUserIdRoot(String userIdRoot) {
		this.userIdRoot = userIdRoot;
	}


	/**
	 * @return the passwordOs
	 */
	public String getPasswordOs() {
		return passwordOs;
	}

	/**
	 * @param passwordOs
	 *            the passwordOs to set
	 */
	public void setPasswordOs(String passwordOs) {
		this.passwordOs = passwordOs;
	}

	/**
	 * @return the passwordRoot
	 */
	public String getPasswordRoot() {
		return passwordRoot;
	}

	/**
	 * @param passwordRoot
	 *            the passwordRoot to set
	 */
	public void setPasswordRoot(String passwordRoot) {
		this.passwordRoot = passwordRoot;
	}

	/**
	 * @return the accountChkOs
	 */
	public Integer getAccountChkOs() {
		return accountChkOs;
	}

	/**
	 * @param accountChkOs
	 *            the accountChkOs to set
	 */
	public void setAccountChkOs(Integer accountChkOs) {
		this.accountChkOs = accountChkOs;
	}

	/**
	 * @return the accountChkDba
	 */
	public Integer getAccountChkDba() {
		return accountChkDba;
	}

	/**
	 * @param accountChkDba
	 *            the accountChkDba to set
	 */
	public void setAccountChkDba(Integer accountChkDba) {
		this.accountChkDba = accountChkDba;
	}

	/**
	 * @return the accountChkUrl
	 */
	public Integer getAccountChkUrl() {
		return accountChkUrl;
	}

	/**
	 * @param accountChkUrl
	 *            the accountChkUrl to set
	 */
	public void setAccountChkUrl(Integer accountChkUrl) {
		this.accountChkUrl = accountChkUrl;
	}

	/**
	 * @return the accountChkRoot
	 */
	public Integer getAccountChkRoot() {
		return accountChkRoot;
	}

	/**
	 * @param accountChkRoot
	 *            the accountChkRoot to set
	 */
	public void setAccountChkRoot(Integer accountChkRoot) {
		this.accountChkRoot = accountChkRoot;
	}

	/**
	 * @return the accountChkWeb
	 */
	public Integer getAccountChkWeb() {
		return accountChkWeb;
	}

	/**
	 * @param accountChkWeb
	 *            the accountChkWeb to set
	 */
	public void setAccountChkWeb(Integer accountChkWeb) {
		this.accountChkWeb = accountChkWeb;
	}

	/**
	 * @return the accountChkWas
	 */
	public Integer getAccountChkWas() {
		return accountChkWas;
	}

	/**
	 * @param accountChkWas
	 *            the accountChkWas to set
	 */
	public void setAccountChkWas(Integer accountChkWas) {
		this.accountChkWas = accountChkWas;
	}

	/**
	 * @return the portSsh
	 */
	public Integer getPortSsh() {
		return portSsh;
	}

	/**
	 * @param portSsh
	 *            the portSsh to set
	 */
	public void setPortSsh(Integer portSsh) {
		this.portSsh = portSsh;
	}

	/**
	 * @return the portSftp
	 */
	public Integer getPortSftp() {
		return portSftp;
	}

	/**
	 * @param portSftp
	 *            the portSftp to set
	 */
	public void setPortSftp(Integer portSftp) {
		this.portSftp = portSftp;
	}

	/**
	 * @return the portTelnet
	 */
	public Integer getPortTelnet() {
		return portTelnet;
	}

	/**
	 * @param portTelnet
	 *            the portTelnet to set
	 */
	public void setPortTelnet(Integer portTelnet) {
		this.portTelnet = portTelnet;
	}

	/**
	 * @return the portFtp
	 */
	public Integer getPortFtp() {
		return portFtp;
	}

	/**
	 * @param portFtp
	 *            the portFtp to set
	 */
	public void setPortFtp(Integer portFtp) {
		this.portFtp = portFtp;
	}

	/**
	 * @return the connectLog
	 */
	public String getConnectLog() {
		return connectLog;
	}

	/**
	 * @param connectLog
	 *            the connectLog to set
	 */
	public void setConnectLog(String connectLog) {
		this.connectLog = connectLog;
	}

	/**
	 * @return the connectShellOs
	 */
	public String getConnectShellOs() {
		return connectShellOs;
	}

	/**
	 * @param connectShellOs
	 *            the connectShellOs to set
	 */
	public void setConnectShellOs(String connectShellOs) {
		this.connectShellOs = connectShellOs;
	}

	/**
	 * @return the connectShellRoot
	 */
	public String getConnectShellRoot() {
		return connectShellRoot;
	}

	/**
	 * @param connectShellRoot
	 *            the connectShellRoot to set
	 */
	public void setConnectShellRoot(String connectShellRoot) {
		this.connectShellRoot = connectShellRoot;
	}

	/**
	 * @return the promptUserIdOs
	 */
	public String getPromptUserIdOs() {
		return promptUserIdOs;
	}

	/**
	 * @param promptUserIdOs
	 *            the promptUserIdOs to set
	 */
	public void setPromptUserIdOs(String promptUserIdOs) {
		this.promptUserIdOs = promptUserIdOs;
	}

	/**
	 * @return the promptUserIdRoot
	 */
	public String getPromptUserIdRoot() {
		return promptUserIdRoot;
	}

	/**
	 * @param promptUserIdRoot
	 *            the promptUserIdRoot to set
	 */
	public void setPromptUserIdRoot(String promptUserIdRoot) {
		this.promptUserIdRoot = promptUserIdRoot;
	}

	/**
	 * @return the useFtpPort
	 */
	public int getUseFtpPort() {
		return useFtpPort;
	}

	/**
	 * @param useFtpPort
	 *            the useFtpPort to set
	 */
	public void setUseFtpPort(int useFtpPort) {
		this.useFtpPort = useFtpPort;
	}

	/**
	 * @return the usePort
	 */
	public int getUsePort() {
		return usePort;
	}

	/**
	 * @param usePort
	 *            the usePort to set
	 */
	public void setUsePort(int usePort) {
		this.usePort = usePort;
	}

	/**
	 * @return the channelType
	 */
	public String getChannelType() {
		return channelType;
	}

	/**
	 * @param channelType
	 *            the channelType to set
	 */
	public void setChannelType(String channelType) {
		this.channelType = channelType;
	}

	/**
	 * @return the commands
	 */
	public String[] getCommands() {
		return commands;
	}

	/**
	 * @param commands
	 *            the commands to set
	 */
	public void setCommands(String[] commands) {
		this.commands = commands;
	}

	/**
	 * @return the agentType
	 */
	public Integer getAgentType() {
		return agentType;
	}

	/**
	 * @param agentType
	 *            the agentType to set
	 */
	public void setAgentType(Integer agentType) {
		this.agentType = agentType;
	}

	/**
	 * @return the agentInstallDate
	 */
	public String getAgentInstallDate() {
		return agentInstallDate;
	}

	/**
	 * @param agentInstallDate
	 *            the agentInstallDate to set
	 */
	public void setAgentInstallDate(String agentInstallDate) {
		this.agentInstallDate = agentInstallDate;
	}

	/**
	 * @return the agentStartUser
	 */
	public String getAgentStartUser() {
		return agentStartUser;
	}

	/**
	 * @param agentStartUser
	 *            the agentStartUser to set
	 */
	public void setAgentStartUser(String agentStartUser) {
		this.agentStartUser = agentStartUser;
	}

	/**
	 * @return the agentStartDate
	 */
	public String getAgentStartDate() {
		return agentStartDate;
	}

	/**
	 * @param agentStartDate
	 *            the agentStartDate to set
	 */
	public void setAgentStartDate(String agentStartDate) {
		this.agentStartDate = agentStartDate;
	}

	/**
	 * @return the agentUseSTime
	 */
	public String getAgentUseSTime() {
		return agentUseSTime;
	}

	/**
	 * @param agentUseSTime
	 *            the agentUseSTime to set
	 */
	public void setAgentUseSTime(String agentUseSTime) {
		this.agentUseSTime = agentUseSTime;
	}

	/**
	 * @return the agentUseETime
	 */
	public String getAgentUseETime() {
		return agentUseETime;
	}

	/**
	 * @param agentUseETime
	 *            the agentUseETime to set
	 */
	public void setAgentUseETime(String agentUseETime) {
		this.agentUseETime = agentUseETime;
	}

	/**
	 * @return the lastUploadedPath
	 */
	public String getLastUploadedPath() {
		return lastUploadedPath;
	}

	/**
	 * @param lastUploadedPath
	 *            the lastUploadedPath to set
	 */
	public void setLastUploadedPath(String lastUploadedPath) {
		this.lastUploadedPath = lastUploadedPath;
	}

	/**
	 * @return the setupShellFile
	 */
	public String getSetupShellFile() {
		return setupShellFile;
	}

	/**
	 * @param setupShellFile
	 *            the setupShellFile to set
	 */
	public void setSetupShellFile(String setupShellFile) {
		this.setupShellFile = setupShellFile;
	}

	/**
	 * @return the setupStatus
	 */
	public String getSetupStatus() {
		return setupStatus;
	}

	/**
	 * @param setupStatus
	 *            the setupStatus to set
	 */
	public void setSetupStatus(String setupStatus) {
		this.setupStatus = setupStatus;
	}

	/**
	 * @return the agentRegiFlag
	 */
	public int getAgentRegiFlag() {
		return agentRegiFlag;
	}

	/**
	 * @param agentRegiFlag
	 *            the agentRegiFlag to set
	 */
	public void setAgentRegiFlag(int agentRegiFlag) {
		this.agentRegiFlag = agentRegiFlag;
	}

	/**
	 * @return the bfaShellFile
	 */
	public String getBfaShellFile() {
		return bfaShellFile;
	}

	/**
	 * @param bfaShellFile
	 *            the bfaShellFile to set
	 */
	public void setBfaShellFile(String bfaShellFile) {
		this.bfaShellFile = bfaShellFile;
	}

	/**
	 * @return the portSshAlive
	 */
	public int getPortSshAlive() {
		return portSshAlive;
	}

	/**
	 * @param portSshAlive
	 *            the portSshAlive to set
	 */
	public void setPortSshAlive(int portSshAlive) {
		this.portSshAlive = portSshAlive;
	}

	/**
	 * @return the portSftpAlive
	 */
	public int getPortSftpAlive() {
		return portSftpAlive;
	}

	/**
	 * @param portSftpAlive
	 *            the portSftpAlive to set
	 */
	public void setPortSftpAlive(int portSftpAlive) {
		this.portSftpAlive = portSftpAlive;
	}

	/**
	 * @return the portTelnetAlive
	 */
	public int getPortTelnetAlive() {
		return portTelnetAlive;
	}

	/**
	 * @param portTelnetAlive
	 *            the portTelnetAlive to set
	 */
	public void setPortTelnetAlive(int portTelnetAlive) {
		this.portTelnetAlive = portTelnetAlive;
	}

	/**
	 * @return the portFtpAlive
	 */
	public int getPortFtpAlive() {
		return portFtpAlive;
	}

	/**
	 * @param portFtpAlive
	 *            the portFtpAlive to set
	 */
	public void setPortFtpAlive(int portFtpAlive) {
		this.portFtpAlive = portFtpAlive;
	}

	public String getAgentVersion() {
		return agentVersion;
	}

	public void setAgentVersion(String agentVersion) {
		this.agentVersion = agentVersion;
	}

	public String getAgentOldVersion() {
		return agentOldVersion;
	}

	public void setAgentOldVersion(String agentOldVersion) {
		this.agentOldVersion = agentOldVersion;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = (userId.equalsIgnoreCase("null") || userId.equalsIgnoreCase("") || userId == null) ? "-" : userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = (password.equalsIgnoreCase("null") || password.equalsIgnoreCase("") || password == null) ? "-" : password;
	}

	@Override
	public String toString() {
		return "AgentInfo [agentCd=" + agentCd + ", assetCd=" + assetCd + ", osType=" + osType + ", osBit=" + osBit
				+ ", connectIpAddress=" + connectIpAddress + ", userIdOs=" + userIdOs + ", userIdRoot=" + userIdRoot +
				", passwordOs=" + passwordOs + ", passwordRoot=" + passwordRoot + ", userId=" + userId + ", password=" + password +", accountChkOs=" + accountChkOs +
				", accountChkDba=" + accountChkDba + ", accountChkUrl=" + accountChkUrl + ", accountChkRoot="
				+ accountChkRoot + ", accountChkWeb=" + accountChkWeb + ", accountChkWas=" + accountChkWas + ", portSsh=" + portSsh + ", portSftp=" + portSftp
				+ ", portTelnet=" + portTelnet + ", portFtp=" + portFtp + ", connectLog=" + connectLog
				+ ", connectShellOs=" + connectShellOs + ", connectShellRoot=" + connectShellRoot + ", promptUserIdOs="
				+ promptUserIdOs + ", promptUserIdRoot=" + promptUserIdRoot + ", agentType=" + agentType
				+ ", agentInstallDate=" + agentInstallDate + ", agentStartUser=" + agentStartUser + ", agentStartDate="
				+ agentStartDate + ", agentUseSTime=" + agentUseSTime + ", agentUseETime=" + agentUseETime
				+ ", useFtpPort=" + useFtpPort + ", usePort=" + usePort + ", channelType=" + channelType
				+ ", commands=" + Arrays.toString(commands) + ", setupShellFile=" + setupShellFile + ", setupStatus="
				+ setupStatus + ", agentRegiFlag=" + agentRegiFlag + ", lastUploadedPath=" + lastUploadedPath
				+ ", bfaShellFile=" + bfaShellFile + ", portSshAlive=" + portSshAlive + ", portSftpAlive="
				+ portSftpAlive + ", portTelnetAlive=" + portTelnetAlive + ", portFtpAlive=" + portFtpAlive + "]";
	}

	public String getSocketFilePath() {
		return socketFilePath;
	}

	public void setSocketFilePath(String socketFilePath) {
		this.socketFilePath = socketFilePath;
	}

	public String getAgentInstallPath() {
		return agentInstallPath;
	}

	public void setAgentInstallPath(String agentInstallPath) {
		this.agentInstallPath = agentInstallPath;
	}

    public void setUseDiagSudo(String useDiagSudo) { this.useDiagSudo = useDiagSudo; }

    public String getUseDiagSudo() { return useDiagSudo; }

    public void setManagerCd(String managerCd) { this.managerCd = managerCd; }

    public String getManagerCd() { return managerCd; }

	public String getParam1() { return param1; }

	public void setParam1(String param1) { this.param1 = param1; }

	public String getParam2() { return param2; }

	public void setParam2(String param2) { this.param2 = param2; }

	public String getParam3() { return param3; }

	public void setParam3(String param3) { this.param3 = param3; }

	public String getBranchId() { return branchId; }

	public void setBranchId(String branchId) { this.branchId = branchId; }

	public String getBranchNm() { return branchNm; }

	public void setBranchNm(String branchNm) { this.branchNm = branchNm; }

	public String getTeamId() { return teamId; }

	public void setTeamId(String teamId) { this.teamId = teamId; }

	public String getTeamNm() { return teamNm; }

	public void setTeamNm(String teamNm) { this.teamNm = teamNm; }

	public String getHostNm() { return hostNm; }

	public void setHostNm(String hostNm) { this.hostNm = hostNm; }

	public String getDgUserNm() { return dgUserNm; }

	public void setDgUserNm(String dgUserNm) { this.dgUserNm = dgUserNm; }

	public String getDgUserId() { return dgUserId; }

	public void setDgUserId(String dgUserId) { this.dgUserId = dgUserId; }

	public String getJobType() { return jobType; }

	public void setJobType(String jobType) { this.jobType = jobType; }
}
