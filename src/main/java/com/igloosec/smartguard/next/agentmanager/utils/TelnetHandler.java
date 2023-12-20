/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.utils.SSHStreamReaderNative.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 1. 8.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.utils;

import com.igloosec.smartguard.next.agentmanager.entity.AgentInfo;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import jodd.util.StringUtil;
import org.apache.commons.net.telnet.TelnetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class TelnetHandler {

	Logger logger = LoggerFactory.getLogger(getClass());

	private String ip;
	private int port;
	private String id;
	private String pw;

	TelnetClient tc;
	PtyAnalyzer ptyAnalyzer;
	Socket cmdSocket = null;
	PrintStream outStream = null;
	BufferedReader inBuffStream = null;
	DataInputStream inDataStream = null;
	InputStream inStream = null;
	String charSet = "utf-8";// "EUC-KR";
	String lastInComingString = "";
	String lastInComingInt = "";
	ArrayList<String> lastInCommingStringArr = new ArrayList<String>();

	private String sessionPrompt = "";
	private String sessionRootPrompt = "";

	static int readTimeout = 59000; // wait 59 sec
	static int readPtyTimeout = 4000; // wait 4 sec
	private boolean isPasswordStep = false;
	private boolean isPromptVerifyStep = false;
	private boolean detectedLoginFail = false;
	
	private SocketStreamReader socketStreamReader;

	/**
	 * @return the sessionPrompt
	 */
	public String getSessionPrompt() {
		return sessionPrompt;
	}

	/**
	 * @param sessionPrompt
	 *            the sessionPrompt to set
	 */
	public void setSessionPrompt(String sessionPrompt) {
		this.sessionPrompt = sessionPrompt;
	}

	/**
	 * @return the sessionRootPrompt
	 */
	public String getSessionRootPrompt() {
		return sessionRootPrompt;
	}

	/**
	 * @param sessionRootPrompt
	 *            the sessionRootPrompt to set
	 */
	public void setSessionRootPrompt(String sessionRootPrompt) {
		this.sessionRootPrompt = sessionRootPrompt;
	}

	/**
	 * USAGE: TelnetHandler th = new
	 * TelnetHandler(IP,Integer.parseInt(PORT),ID,PW); boolean conn =
	 * th.connectWlogin(); if(conn){ String result = th.exeCommand("ls-al"); }
	 * 
	 * ****/

	public void close() {
		try {
			outStream.close();
			inStream.close();
			tc.disconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public TelnetHandler() {
		this.ip = "127.0.0.1";
		this.port = 23;
		this.id = "";
		this.pw = "";
		this.socketStreamReader = new SocketStreamReader();
		
	}

	public TelnetHandler(AgentInfo agentInfo) {
		this.ip = agentInfo.getConnectIpAddress();
		this.port = agentInfo.getUsePort();
		this.id = agentInfo.getUserIdOs();
		this.pw = agentInfo.getPasswordOs();
		this.tc = new TelnetClient();
		this.socketStreamReader = new SocketStreamReader();
		
	}

	public TelnetHandler(String ip, int port, String id, String pw) {
		this.ip = ip;
		this.port = port;
		this.id = id;
		this.pw = pw;
		this.tc = new TelnetClient();
		this.socketStreamReader = new SocketStreamReader();
		
	}

	public boolean connectWlogin() throws Exception {
		boolean isConnected = false;

		this.tc.connect(this.ip, this.port);
		this.tc.setTcpNoDelay(true);
		outStream = new PrintStream(this.tc.getOutputStream());
		inStream = this.tc.getInputStream();
		String mode = INMEMORYDB.ptyMode.LOGIN.toString();
		String rslt = (String)readIs(inStream, INMEMORYDB.ptyMode.LOGIN.toString()).get(INMEMORYDB.ptyKey.PTY.toString());
		handleRslt(rslt, mode);
		if (detectedLoginFail) {
			isConnected = false;
		} else {
			isConnected = true;
			// Session Prompt Set
//			setSessionPrompt(verifyPrompt(this.id));
		}

		return isConnected;

	}

	public String verifyPrompt(String id) {
		isPromptVerifyStep = true;
		String mode = INMEMORYDB.ptyMode.VERIFYPROMPT.toString();
		String prpt = exeCommand("", mode);

		if (prpt != null) {
			prpt = StringUtil.replace(prpt, "\r\n", "");
			prpt = StringUtil.replace(prpt, "\n", "");
			prpt = StringUtil.replace(prpt, "\r", "");
		}

		logger.info("VERIFIED PROMPT '" + prpt + "' [" + this.ip + ":"
				+ this.port + ", id:" + id + "]");
		return prpt;
	}

	public boolean connect() {
		boolean isConnected = false;

		try {
			this.tc.connect(this.ip, this.port);
			this.tc.setTcpNoDelay(true);
			outStream = new PrintStream(this.tc.getOutputStream());
			inStream = this.tc.getInputStream();
			String mode = INMEMORYDB.ptyMode.LOGIN.toString();
			readIs(inStream, mode);
			isConnected = true;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		return isConnected;
	}

	void handleRslt(String rslt, String mode) {
		
		boolean promptChk = (mode.equals(INMEMORYDB.ptyMode.CMD.toString()));
		
		if (rslt != null) {
			if (!promptChk && rslt.toUpperCase().equals("LOGIN")) {
				logger.debug("HANDLING  " + rslt);
				exeCommand(this.id, INMEMORYDB.ptyMode.LOGIN.toString());
			} else if (!promptChk && rslt.toUpperCase().equals("PASSWORD")) {
				logger.debug("HANDLING  " + rslt);
				isPasswordStep = true;
				exeCommand(this.pw, INMEMORYDB.ptyMode.LOGIN.toString());
			} else if (rslt.toUpperCase().equals("PROMPT")) {
				logger.debug("NO FURTHER HANDLING  " + rslt
						+ " & GOTO TRIMMING\n");
				detectedLoginFail = false;
			} else if (rslt.toUpperCase().equals("INCORRECT") ||rslt.toUpperCase().equals("INCORRECTPASS")) {
				logger.debug("NO FURTHER HANDLING  " + rslt
						+ " & GOTO TRIMMING\n");
				detectedLoginFail = true;

			} else {
				logger.debug("NO FURTHER HANDLING  " + rslt
						+ " & GOTO TRIMMING\n");
			}
		}

	}

	public boolean doSu(String rid, String rpw) {
		String suCmd = "su - " + rid;
		try {
			logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>\n " + "SENDING " + suCmd);
			outStream.println(suCmd);
			outStream.flush();
			this.readIs(inStream, "LOGIN");
			isPasswordStep = true;
			exeCommand(rpw, INMEMORYDB.ptyMode.LOGIN.toString());

		} catch (Exception e) {
			logger.error("ERROR WHILE TELNET SU :" + e.getMessage());
			return false;
		}
		return !detectedLoginFail;

	}

	public String exeCommand(String runCmd) {
		return exeCommand(runCmd, INMEMORYDB.ptyMode.CMD.toString());
	}

	@SuppressWarnings("unchecked")
	public String exeCommand(String runCmd, String mode) {

		String rsltStr = "";
		HashMap<String, Object> result = null;
		try {

			String cmmd = (runCmd.equals("")) ? "PROMPT VERIFICATION ''" : runCmd;
			System.out.println("SENDING " + cmmd);
			outStream.println(runCmd);
			outStream.flush();
			result = this.readIs(inStream, mode);
			rsltStr = (String)result.get(INMEMORYDB.ptyKey.PTY.toString());
			handleRslt(rsltStr, mode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(result != null){
			return this.socketStreamReader.trimExeResult((ArrayList<String>)result.get(INMEMORYDB.ptyKey.ARRSTR.toString()), runCmd, "");
		}
		else{
			return null;
		}
		

	}	

	HashMap<String, Object> readIs(InputStream is, String mode) throws Exception {
		return this.socketStreamReader.readIs(is, mode);
	}

	public static void main(String args[]) {
		System.out.println("java -cp AgentManager.jar com.mobigen.snet.agentmanager.utils.TelnetHandler IP PORT ID PW cmd");

		int argL = 0;
		if (args != null && args.length > 0) {
			argL = args.length;
			System.out.println("args[0] IP :" + args[0]);
			System.out.println("args[1] PORT :" + args[1]);
			System.out.println("args[2] ID:" + args[2]);
			System.out.println("args[3] PW:" + args[3]);
			System.out.println("args[4] CMD:" + args[4]);
		}

		// System.out.println("TELNET TEST :: INPUT MUST BE 'IP PORT ID PW COMMAND' ");
		int argLen = args.length;
		String IP, PORT, ID, PW, CMD;

		if (argLen != 4) {
			System.out.println("Put Proper Arguments argCnt=" + argLen);
			int argIdx = 0;
			while (argIdx < argLen) {
				System.out.println("args[" + argIdx + "]:" + args[argIdx]);
				argIdx++;
			}

		} else {
			IP = args[0];
			PORT = args[1];
			ID = args[2];
			PW = args[3];
			// CMD = args[4];

			if (PORT == null) {
				PORT = "23";
			}

			TelnetHandler th = new TelnetHandler(IP, Integer.parseInt(PORT),
					ID, PW);
			boolean conn;
			try {
				conn = th.connectWlogin();
				if (conn) {
					th.exeCommand("\n");

					// th.exeCommand("ls -al");
					//
					// th.doSu("root", "sddevhub2oll3");
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
}
