/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.utils.DoMain.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 3. 31.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.utils;


import com.igloosec.smartguard.next.agentmanager.config.DBConfig;
import com.igloosec.smartguard.next.agentmanager.dao.Dao;
import com.igloosec.smartguard.next.agentmanager.entity.AgentInfo;
import com.igloosec.smartguard.next.agentmanager.services.ConnectionManager;
import com.igloosec.smartguard.next.agentmanager.services.DeployManager;
import com.igloosec.smartguard.next.agentmanager.services.SSHConnectTest;
import jodd.util.StringUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class DoMain {


	static String readTypeIn(){
		BufferedReader br = null;
		String strIn = "";
		br = new BufferedReader(new InputStreamReader(System.in));
		try {
			strIn= br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return strIn;
	}
	static String[] paramHandle(String[] params){
		int leng = params.length;
		int idx = 0;
		String[] inputs = new String[params.length];
		System.out.println("type 1 : arg1=arg2=arg3=arg4 , 2: communication mode");
		String type =readTypeIn();
		if("1".equals(type)){
			System.out.println("type args : ");
			while(idx < leng){
				System.out.print(params[idx]+" ");
				idx++;
				}
			System.out.println("");
			idx = 0;
			String args = readTypeIn();
			inputs = StringUtil.split(args, "=");
			System.out.println(inputs.length);
		}else{
		
			while(idx < leng){
				System.out.println(params[idx]+":");
				inputs[idx] = readTypeIn();
				idx++;
				}
			idx = 0;
		}
		
		while(idx < leng){
			System.out.println(params[idx]+":"+inputs[idx]);
			idx++;
		}
		
		return inputs;
	}
	
	public static void doTelnet() throws Exception {
		
		String[] params = {"ip","port","id","password","command"};
		String[] in = paramHandle(params);
		if(in[1] == null || "".equals(in[1]) ){ in[1] = "23";}
		TelnetHandler t = new TelnetHandler(in[0], Integer.parseInt(in[1]),in[2],in[3]);
		boolean islogin= t.connectWlogin();
		if(islogin){
			String cmd = "";
			String result = t.exeCommand(in[4]);
			System.out.println("RESULT >>>>"+result+"<<<<");
			while(true){
				cmd = readTypeIn();
				if("quit".equals(cmd)){
					System.out.println("EXIT CMD !!");
					break;
				}
				 result = t.exeCommand(cmd);
			}
			t.close();
			
		}else{
			System.out.println("Login Failed.");
		}
		return;
		
	}
	
	public static void doSuTelnet() throws Exception {
		
		String[] params = {"0:ip","1:port","2:id","3:password","4:rootId","5:rootPass","6:command"};
		String[] in = paramHandle(params);
		if(in[1] == null || "".equals(in[1]) ){ in[1] = "23";}
		TelnetHandler t = new TelnetHandler(in[0], Integer.parseInt(in[1]),in[2],in[3]);
		boolean islogin= t.connectWlogin();
		if(islogin){
			String cmd = "";
			boolean sued = t.doSu(in[4], in[5]);
			String result = "xxxx";
			if(sued)result = t.exeCommand(in[6]);
			
			System.out.println("RESULT >>>>"+result+"<<<<<");
			while(true){
				cmd = readTypeIn();
				if("quit".equals(cmd)){
					System.out.println("EXIT CMD !!");
					break;
				}
				 result = t.exeCommand(cmd);
			}
		}else{
			System.out.println("Login Failed.");
		}
		return;
		
	}
	
	
	static void doDeploy(DeployManager dm) throws Exception {
		String[] params = {"0:assecCd","1:ip","2:id","3:password","4:os{LINUS,WINDOW}","5:bit[64/32]","6:rootId","7:rootPass","8:SFTP-PORT(22/0/)","9:FTP-PORT(21/0/)","10:SSH-PORT(22/0/)","11:TELNET-PORT(23/0/)"};
		String[] in = paramHandle(params);
		AgentInfo ai = new AgentInfo();
		
			ai.setSetupShellFile("agentsetup.sh");
			ai.setLastUploadedPath("/usr/local/tomcat/setup/");
					
			ai.setAssetCd(in[0]);
			ai.setConnectIpAddress(in[1]);
			ai.setUserIdOs(in[2]);
			ai.setPasswordOs(in[3]);
			ai.setOsType(in[4]);
			ai.setOsBit(Integer.parseInt(in[5]));
			ai.setUserIdRoot(in[6]);
			ai.setPasswordRoot(in[7]);
			ai.setPortSftp(Integer.parseInt(in[8]));
			ai.setPortFtp(Integer.parseInt(in[9]));
			ai.setPortSsh(Integer.parseInt(in[10]));
			ai.setPortTelnet(Integer.parseInt(in[11]));
		
			ai.completeAgentInfo();		
		 
			dm.installAgent(ai);
		
	}
	
	static void doOpenPort(){
		String[] params = {"ip","port"};
		String[] in;
//		CommonUtils c = new CommonUtils();
		while(true){
			in = paramHandle(params);
			if("".equals(in[0]) || "quit".equals(in[0]))break;
			
			if(CommonUtils.isOpenPort(in[0], Integer.parseInt(in[1]))){
				System.out.println("PORT OPEN.!!!");
			}
			else{
				System.out.println("CANNOT ACCESS PORT.!!!"+ in[0] + ", "+ in[1]);
			}
		}
		return;
		
	}
static void doSFtp(ConnectionManager c) throws Exception {
		
		String[] params = {"0:ip","1:port","2:id","3:password","4:file,file,file..","5:remotepath"};
		String[] in = paramHandle(params);
		
		if(in[1] == null || "".equals(in[1]) ){ in[1] = "22";}
		
		AgentInfo ai = new AgentInfo();
		
		ai.setSetupShellFile("agentsetup.sh");
		ai.setLastUploadedPath("/usr/local/tomcat/setup/");
				
		ai.setConnectIpAddress("172.20.76.161");
		ai.setPortSsh(Integer.parseInt("22"));
		ai.setUserIdOs("n1140382");
		ai.setPasswordOs("nps!!1234");
		ai.setPortSftp(Integer.parseInt("22"));
		ai.setUserIdRoot("root");
		ai.setPasswordRoot("tnfus^&161");
		ai.setOsType("LINUX");
		
		
//		ai.setAssetCd(params[0]);
//		ai.setConnectIpAddress(in[0]);
//		ai.setUserIdOs(in[2]);
//		ai.setPasswordOs(in[3]);
//		ai.setOsType("LINUX");
//		ai.setPortFtp(Integer.parseInt(in[1]));
		ai.completeAgentInfo();		
		
		File[] files;
		String[] fns = StringUtil.split(in[4], ",");
		files = new File[fns.length];
		int idx = 0;
		for(String fn : fns){
			files[idx] = new File(fn);
			idx++;
		}
		
		c.uploadViaSFTP(ai, in[5], files);
		
		return;
	}
	
	static void doFtp(ConnectionManager  c){
		
		String[] params = {"0:ip","1:port","2:id","3:password","4:file,file,file..","5:remotepath"};
		String[] in = paramHandle(params);
		
		if(in[1] == null || "".equals(in[1]) ){ in[1] = "21";}
		
		AgentInfo ai = new AgentInfo();
		
		ai.setSetupShellFile("agentsetup.sh");
		ai.setLastUploadedPath("/usr/local/tomcat/setup/");
				
//		ai.setAssetCd(params[0]);
		ai.setConnectIpAddress(in[0]);
		ai.setUserIdOs(in[2]);
		ai.setPasswordOs(in[3]);
		ai.setOsType("WIN");
		ai.setPortFtp(Integer.parseInt(in[1]));
		ai.completeAgentInfo();		
		
		File[] files;
		String[] fns = StringUtil.split(in[4], ",");
		files = new File[fns.length];
		int idx = 0;
		for(String fn : fns){
			files[idx] = new File(fn);
			idx++;
		}
		
		c.uploadViaFTP(ai, in[5], files);
		
		return;
	}
	
	static void doSsh(ConnectionManager c) throws Exception {
		String[] params = {"0:ip","1:port", "2:id", "3:password" , "4:cmd", "5:os(WIN/LINUX)"};
		
		String[] in = paramHandle(params);
		AgentInfo ai = new AgentInfo();
		
		if(in[0] == null || "".equals(in[1])){in[1] = "22";}
		ai.setConnectIpAddress(in[0]);
		ai.setPortSsh(Integer.parseInt(in[1]));
		ai.setUserIdOs(in[2]);
		ai.setPasswordOs(in[3]);
		ai.setPortSftp(Integer.parseInt(in[1]));
		ai.setOsType(in[5]);
		
//		ai.setUserIdRoot(params[6]);
//		ai.setPasswordRoot(params[7]);
//		ai.setPortSftp(Integer.parseInt(params[8]));
//		ai.setPortFtp(Integer.parseInt(params[9]));		
//		ai.setPortSsh(Integer.parseInt(params[10]));
//		ai.setPortTelnet(Integer.parseInt(params[11]));		
	
		ai.completeAgentInfo();		
	
		c.runRemoteScriptViaSSH(ai, in[4]);
		return;
	}
	
	
	static void doSshSU(ConnectionManager c) throws Exception {
		String[] params = {"0:ip","1:port", "2:id", "3:password" , "4:rootid","5:rootpw", "6:cmd", "7:os(WIN/LINUX)"};
		
		String[] in = paramHandle(params);
		AgentInfo ai = new AgentInfo();
		
		if(in[0] == null || "".equals(in[1])){in[1] = "22";}
		
//		ai.setConnectIpAddress("218.233.105.58");
//		ai.setPortSsh(Integer.parseInt("22"));
//		ai.setUserIdOs("snet");
//		ai.setPasswordOs("mobigen123");
//		ai.setPortSftp(Integer.parseInt("22"));
//		ai.setUserIdRoot("root");
//		ai.setPasswordRoot("mobigen123");
//		ai.setOsType("LINUX");
				 
		ai.setConnectIpAddress("172.20.76.161");
		ai.setPortSsh(Integer.parseInt("22"));
		ai.setUserIdOs("n1140382");
		ai.setPasswordOs("nps!!1234");
		ai.setPortSftp(Integer.parseInt("22"));
		ai.setUserIdRoot("root");
		ai.setPasswordRoot("tnfus^&161");
		ai.setOsType("LINUX");
		
		
//		ai.setConnectIpAddress(in[0]);
//		ai.setPortSsh(Integer.parseInt(in[1]));
//		ai.setUserIdOs(in[2]);
//		ai.setPasswordOs(in[3]);
//		ai.setPortSftp(Integer.parseInt(in[1]));
//		ai.setUserIdRoot(in[4]);
//		ai.setPasswordRoot(in[5]);
//		ai.setOsType(in[7]);

//		ai.setUserIdRoot(params[6]);
//		ai.setPasswordRoot(params[7]);
//		ai.setPortSftp(Integer.parseInt(params[8]));
//		ai.setPortFtp(Integer.parseInt(params[9]));		
//		ai.setPortSsh(Integer.parseInt(params[10]));
//		ai.setPortTelnet(Integer.parseInt(params[11]));		
	
		ai.completeAgentInfo();		
	
		c.runRemoteScriptViaSSH_Su(ai, "pwd");
	}
	
static void doGetHome(ConnectionManager  c) throws Exception {
		
		String[] params = {"0:ip","1:port","2:id","3:password","4:os(WIN/LINUX)"};
		String[] in = paramHandle(params);
		
		if(in[1] == null || "".equals(in[1]) ){ in[1] = "23";}
		
		AgentInfo ai = new AgentInfo();
				
//		ai.setAssetCd(params[0]);
		ai.setConnectIpAddress(in[0]);
		ai.setUserIdOs(in[2]);
		ai.setPasswordOs(in[3]);
		ai.setOsType(in[4]);
		ai.setPortTelnet(Integer.parseInt(in[1]));
		ai.completeAgentInfo();		
		
		System.out.println("RREESSUULLTT:'"+c.getHomeDirViaTelnet(ai)+"'");
		
		
		return;
	}
	
static void verifyPromptViaTelnet(ConnectionManager  c) throws Exception {
	
	String[] params = {"0:ip","1:port","2:id","3:password","4:os(WIN/LINUX)"};
	String[] in = paramHandle(params);
	
	if(in[1] == null || "".equals(in[1]) ){ in[1] = "23";}
	
	AgentInfo ai = new AgentInfo();
			
//	ai.setAssetCd(params[0]);
	ai.setConnectIpAddress(in[0]);
	ai.setUserIdOs(in[2]);
	ai.setPasswordOs(in[3]);
	ai.setOsType(in[4]);
	ai.setPortTelnet(Integer.parseInt(in[1]));
	ai.completeAgentInfo();		
	
	System.out.println("RREESSUULLTT:'"+c.verifyPromptViaTelnet(ai)+"'");
	
	
	return;
}


public static void verifyRootPromptViaTelnet(ConnectionManager  c) throws Exception {
	
	String[] params = {"0:ip","1:port","2:id","3:password","4:rootId","5:rootPass","6:os(WIN/LINUX)"};
	String[] in = paramHandle(params);
	if(in[1] == null || "".equals(in[1]) ){ in[1] = "23";}
	AgentInfo ai = new AgentInfo();
	
	ai.setConnectIpAddress(in[0]);
	ai.setUserIdOs(in[2]);
	ai.setPasswordOs(in[3]);
	ai.setOsType(in[6]);
	ai.setUserIdRoot(in[4]);
	ai.setPasswordRoot(in[5]);
	ai.setPortTelnet(Integer.parseInt(in[1]));
	ai.completeAgentInfo();		
		
	System.out.println("RREESSUULLTT:'"+c.verifyRootPromptViaTelnet(ai)+"'");
	
	return;
	
}

static void verifyRootPromptViaSSH(ConnectionManager c){
	String[] params = {"0:ip","1:port", "2:id", "3:password" , "4:rootid","5:rootpw", "6:os(WIN/LINUX)"};
	
	String[] in = paramHandle(params);
	AgentInfo ai = new AgentInfo();
	
	ai.setConnectIpAddress(in[0]);
	ai.setPortSsh(Integer.parseInt(in[1]));
	ai.setUserIdOs(in[2]);
	ai.setPasswordOs(in[3]);
	ai.setPortSftp(Integer.parseInt(in[1]));
	ai.setUserIdRoot(in[4]);
	ai.setPasswordRoot(in[5]);
	ai.setOsType(in[6]);

//	ai.setUserIdRoot(params[6]);
//	ai.setPasswordRoot(params[7]);
//	ai.setPortSftp(Integer.parseInt(params[8]));
//	ai.setPortFtp(Integer.parseInt(params[9]));		
//	ai.setPortSsh(Integer.parseInt(params[10]));
//	ai.setPortTelnet(Integer.parseInt(params[11]));		

	ai.completeAgentInfo();		

	String[] result = null;
	try{
		result = c.verifyRootPromptViaSSH(ai);	
	}catch(Exception e){
		e.getStackTrace();
	}
	
	System.out.println("root Prompt : '"+result[0]+"'");
	System.out.println("conn Prompt : '"+result[1]+"'");
}



		static void doCheckSShMode(){
			SSHConnectTest st = new SSHConnectTest();
		
		   String[] params = {"0:ip","1:port", "2:id", "3:password"};
			
			String[] in = paramHandle(params);
			if(in[1] == null || "".equals(in[1]) ){ in[1] = "22";}
			
			AgentInfo ai = new AgentInfo();
			
			ai.setConnectIpAddress(in[0]);
			ai.setPortSsh(Integer.parseInt(in[1]));
			ai.setUserIdOs(in[2]);
			ai.setPasswordOs(in[3]);
			ai.setPortSftp(Integer.parseInt(in[1]));
			ai.setOsType("LINUX");
			
			ai.completeAgentInfo();
			st.sshPasswordModeCheck(ai, "");	
		}

	public static void main(String args[]) throws Exception {
//		@SuppressWarnings("resource")
//        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
//				"applicationContext.xml");
//		System.out.println("\r\n\r\n\r\n");

		ApplicationContext applicationContext = new AnnotationConfigApplicationContext(DBConfig.class, Dao.class, ConnectionManager.class);

		ConnectionManager c = (ConnectionManager)applicationContext.getBean("connectionManager");
		System.out.println("INPUT COMMAND TYPE: ");
		System.out.println(" i.e. install | openport | telnet | telnetsu | ssh \n| sshsu | sftp | ftp | homedir | telProm \n| telsuProm | pwmodechk");
		String type = readTypeIn();
		System.out.println("'"+type+"'");
		
		switch(type){
		case "telnet" : doTelnet(); break;
		case "telnetsu" : doSuTelnet(); break;
		case "install" : {
			DeployManager dm = (DeployManager)applicationContext.getBean("deployManager"); 
			doDeploy(dm); 
			break;	
						}
		
		case "openport" : doOpenPort(); break; 
		case "ssh" : {			
			doSsh(c); 
			break;	
		}
		case "sshsu" : {			
			doSshSU(c); 
			break;	
			}
		case "sftp" : {		
			doSFtp(c);
			break;
		}
		case "ftp" : {		
			doFtp(c);
			break;
		}
		case "homedir" :{
			doGetHome(c);
			break;
		}
		case "telProm" :{
			verifyPromptViaTelnet(c);
			break;
		}
		case "telsuProm" :{
			verifyRootPromptViaTelnet(c);
			break;
		}
		case "sshProm" :{
			verifyRootPromptViaSSH(c);
			break;
		}
		case "pwmodechk" :{
			doCheckSShMode();
			break;
		}
		
		
		
		
		}
		
		System.out.println(">>> END OF PROGRAM. <<<");
		
		 
		
	}

}
