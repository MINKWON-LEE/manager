/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.utils.SocketStreamReader.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 1. 8.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.utils;

import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SocketStreamReader {
	PtyAnalyzer ptyAnalyzer;
	Logger logger = LoggerFactory.getLogger(getClass());
//	Logger agentCommlogger = LoggerFactory.getLogger("agentCommLog");

	private int readTimeout = 10*1000; // wait 59 sec 

	private int readPtyTimeout = 15*1000; // wait 4 sec

	private int timeOutMulMax = 1;
	

	private ThreadPoolTaskExecutor taskExecutor;

	public SocketStreamReader(){
		this.ptyAnalyzer = new PtyAnalyzer();

	}


	/**
	 * readIs(InputStream is, String mode)
	 *
	 * PARAM : mode = "CMD","LOGIN","VERIFYPROMPT"
	 *
	 * RESULT : HashMap<String, Object>
	 * <"PTY", String:[null/ptyType]>
	 * <"STR", String:lastReadString>
	 * <"INT", String:lastReadInt(byte)>
	 * <"ARRSTR" , ArrayList<String> read String by nine>
	 *
	 * **/
	public HashMap<String, Object> readIs(InputStream is, String mode) throws Exception {
		
		readTimeout = INMEMORYDB.readTimeout;
		readPtyTimeout = INMEMORYDB.readPtyTimeout;
		int timeOutMulMax = INMEMORYDB.timeOutMulMax; 
		int timeOutMul = 1;
		logger.debug("readTimeout:"+ readTimeout+ " readPtyTimeout:"+readPtyTimeout);
//		agentCommlogger.debug("readTimeout:"+ readTimeout+ " readPtyTimeout:"+readPtyTimeout);
		String longProcStart="+SLWAITPROC+";
		String longProcStart1="SLWAITPROC+";
		String longRelayProcStart="+SMWAITPROC+";
		String longRelayProcStart1="SMWAITPROC+";
		String longProcEnd="+ELWAITPROC+";
		String longProcEnd1="ELWAITPROC+";
		String longRelayProcEnd="+EMWAITPROC+";
		String longRelayProcEnd1="EMWAITPROC+";
		String ignoredRec = "";
		String ignoredRecInt = "";

		
		int recvedClzCnt = 0;
		int limitClzCnt = 100;
		boolean nomorePrintLog = false;
		boolean sessionDiscon = false;


		Callable<Integer> readTask = new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				int i = is.read();
				return i;
			}
		};

		String whichPty = "";
		HashMap<String, Object> readResult = new HashMap<String, Object>();

		int readHangTimeOut = (mode.equals(INMEMORYDB.ptyMode.CMD.toString())) ? readTimeout : readPtyTimeout;
		boolean promptChk = (mode.equals(INMEMORYDB.ptyMode.CMD.toString()));


		ArrayList<String> lastInCommingStringArr = new ArrayList<String>();
		String lastInComingString = "";
		String lastInComingString4ChkPty = "";
		String lastInComingInt = "";
		StringBuffer appendBuffStr = new StringBuffer();
		StringBuffer appendBuffStr4ChkPty = new StringBuffer();
		StringBuffer appendBuffStrByLine = new StringBuffer();
		StringBuffer appendBuffInt = new StringBuffer();
		

		String sbLineTemp = "";

		int readInt = 0;
		char readChar = (char)readInt;
		boolean passRead = false;
		ExecutorService executor = Executors.newFixedThreadPool(2);
		Future<Integer> future;
		boolean ignoreChar = false;
		int ignoreLeng = 0;
		try {


//			while (readInt != -1) {
			while (true) {
				if(!passRead){
					if(lastInComingString.endsWith(longProcStart)
							|| lastInComingString.endsWith(longProcStart1) 
							|| lastInComingString.endsWith(longRelayProcStart)
							|| lastInComingString.endsWith(longRelayProcStart1)){
						timeOutMul = timeOutMulMax;
						logger.debug("[INC:"+mode+"]Read-Timeout set to "+ readHangTimeOut*timeOutMul);
//						agentCommlogger.debug("[INC:"+mode+"]Read-Timeout set to "+ readHangTimeOut*timeOutMul);
					}else if(lastInComingString.endsWith(longProcEnd)
							||lastInComingString.endsWith(longProcEnd1)
							||lastInComingString.endsWith(longRelayProcEnd)
							||lastInComingString.endsWith(longRelayProcEnd1)){
						timeOutMul = 1;
						logger.debug("[DEC:"+mode+"]Read-Timeout set to "+ readHangTimeOut*timeOutMul);
//						agentCommlogger.debug("[DEC:"+mode+"]Read-Timeout set to "+ readHangTimeOut*timeOutMul);
					}
					future = executor.submit(readTask);
					readInt = future
							.get(readHangTimeOut*timeOutMul, TimeUnit.MILLISECONDS);

				}

				if(readInt == -1){
					recvedClzCnt++;
					if(recvedClzCnt >= limitClzCnt)
					{
						logger.debug("RECEIVED -1 SIGNAL X 100");
//						break;
						readResult.put(INMEMORYDB.ptyKey.ARRSTR.toString(),lastInCommingStringArr);
						sessionDiscon = true;
						throw new InterruptedException("[CONNECTION_FAILURE] DISCONNECTED WITH SIGNAL -1");
					}
				}

				if(readInt == 27) {
					ignoreChar = true; //ESCAPE CHAR [START] HANDLING
					ignoredRec = "";
					ignoredRecInt = "";
					ignoreLeng = 0;
				}


				if(ignoreChar){
					ignoredRec = ignoredRec +(char) readInt;
					ignoredRecInt = " "+readInt;
					logger.debug("ignored: "+ignoredRec+" : "+ignoredRecInt );
					ignoreLeng++;
//					agentCommlogger.debug("ignored: "+ignoredRec+" : "+ignoredRecInt );
					if(readInt == 27){
						readChar = (char) -1;
					}else{
						readChar = (char) readInt;
					}

				}else{
					readChar = (char) readInt;
				}


				passRead = false;
				
				if(readInt != 10 && readInt != 13 && readInt !=27)appendBuffStr4ChkPty.append(readChar);
//				if(!ignoreChar)appendBuffStr.append(readChar);
//				if(!ignoreChar)appendBuffStrByLine.append(readChar);
//				if(!ignoreChar)appendBuffInt.append(readInt + " ");
//				if(!ignoreChar)lastInComingString = appendBuffStr.toString();
//				if(!ignoreChar)sbLineTemp = appendBuffStrByLine.toString();
				appendBuffStr.append(readChar);
				appendBuffStrByLine.append(readChar);
				appendBuffInt.append(readInt + " ");
				lastInComingString = appendBuffStr.toString();
				lastInComingString4ChkPty= appendBuffStr4ChkPty.toString();
				sbLineTemp = appendBuffStrByLine.toString();

				lastInComingInt = appendBuffInt.toString();
				if (readInt == 10 || readInt == 13) {
					if (appendBuffStrByLine.length() == 1) {
					} else {
						lastInCommingStringArr.add(sbLineTemp.replaceAll(
								"[\\r\\n]", ""));
					}
					appendBuffStrByLine = new StringBuffer();
				}
				//ESCAPE CHAR [END] HANDLING : m
				if(ignoreChar && readInt == 109){
					ignoreChar= false;
//					logger.debug("ignored ESCAPE.! for "+ ignoredRec+" : "+ignoredRecInt);
//					agentCommlogger.debug("ignored ESCAPE.! for "+ ignoredRec+" : "+ignoredRecInt);
					ignoredRec = "";
					ignoredRecInt = "";
				}

//				if(ignoreLeng > 0){
//					String appendBuffStrByLineStr = appendBuffStrByLine.toString();
//					appendBuffStrByLineStr = appendBuffStrByLineStr.substring(0, appendBuffStrByLineStr.length() - ignoreLeng);
//					whichPty = ptyAnalyzer.checkPty(lastInComingString4ChkPty,appendBuffStrByLineStr,lastInComingInt, promptChk);
//				}else{
					whichPty = ptyAnalyzer.checkPty(lastInComingString4ChkPty,appendBuffStrByLine.toString(),lastInComingInt, promptChk);	
//				}

				//PTY TYPE
				readResult.put(INMEMORYDB.ptyKey.PTY.toString(),whichPty);
				//PTY STRING
				readResult.put(INMEMORYDB.ptyKey.STR.toString(),lastInComingString);
				//PTY INT BYTES
				readResult.put(INMEMORYDB.ptyKey.INT.toString(),lastInComingInt);
				//ARRAYED LINE BY STRING
				readResult.put(INMEMORYDB.ptyKey.ARRSTR.toString(),lastInCommingStringArr);

				if(!("PROMPT".equals(whichPty) || whichPty == null)){
					logger.debug("FOUND NON-PROMPT MODE["+mode+"] : "+ whichPty +"'"+lastInComingString+"'");
//					agentCommlogger.debug("FOUND NON-PROMPT MODE["+mode+"]: "+ whichPty +"'"+lastInComingString+"'");

					break;
				}else if("PROMPT".equals(whichPty)){
					logger.debug("FOUND PROMPT MODE["+mode+"]: "+ whichPty +"'"+lastInComingString+"'");
//					agentCommlogger.debug("FOUND PROMPT MODE["+mode+"]: "+ whichPty +"'"+lastInComingString+"'");
					try{
						future = executor.submit(readTask);
						readInt = future
								.get(readHangTimeOut*timeOutMul, TimeUnit.MILLISECONDS);
						readChar = (char) readInt;
//						if(readInt > 0){
						passRead = true;						
						logger.debug("FOUND PROMPT BUT HAS MORE TO READ. MODE["+mode+"] NEXT READ readInt='"+readInt+"' , readChar='"+readChar+"'");
//						agentCommlogger.debug("FOUND PROMPT BUT HAS MORE TO READ. MODE["+mode+"] NEXT READ readInt='"+readInt+"' , readChar='"+readChar+"'");
//						}
					}catch(TimeoutException te){
						lastInCommingStringArr.add(sbLineTemp.replaceAll(
								"[\\r\\n]", ""));
						//ARRAYED LINE BY STRING
						readResult.put(INMEMORYDB.ptyKey.ARRSTR.toString(),lastInCommingStringArr);
						logger.info("FOUND PROMPT MODE["+mode+"] && NO MORE BYTES TO READ. EXIT READ.");
//						agentCommlogger.info("FOUND PROMPT MODE["+mode+"] && NO MORE BYTES TO READ. EXIT READ.");
						break;
					}

				}
				
			}

		}catch (TimeoutException te){
			//커맨드를 실행 했을때, 프롬프트를 찾지 못하고 Timeout 되었을때.
			if(readResult.get(INMEMORYDB.ptyKey.PTY.toString()) == null && promptChk){
				
				logger.debug("\n [PTY == NULL] MODE["+mode+"] for \n STR:\n '"+ lastInComingString +"'\n INT:'" + lastInComingInt+"' \n\n");
				
				lastInCommingStringArr.add(sbLineTemp.replaceAll(
						"[\\r\\n]", ""));
				//ARRAYED LINE BY STRING
				readResult.put(INMEMORYDB.ptyKey.ARRSTR.toString(),lastInCommingStringArr);
				
//				agentCommlogger.debug("\n [NO PTY FOUND] MODE["+mode+"] for \n STR:\n '"+ lastInComingString +"'\n INT:'" + lastInComingInt+"' \n\n");
//				agentCommlogger.error("[NO PTY FOUND] MODE["+mode+"]: " + e.getMessage());
			}

			
		}catch (InterruptedException | ExecutionException e) {

			if(readResult.get(INMEMORYDB.ptyKey.PTY.toString()) == null){
				logger.debug("\n [NO PTY FOUND] MODE["+mode+"] for \n STR:\n '"+ lastInComingString +"'\n INT:'" + lastInComingInt+"' \n\n");
//				agentCommlogger.debug("\n [NO PTY FOUND] MODE["+mode+"] for \n STR:\n '"+ lastInComingString +"'\n INT:'" + lastInComingInt+"' \n\n");
//				agentCommlogger.error("[NO PTY FOUND] MODE["+mode+"]: " + e.getMessage());
			}


		}finally{
			if(executor != null)
				executor.shutdown();
		}
		if(sessionDiscon){
			throw new InterruptedException("[CONNECTION_FAILURE] DISCONNECTED WITH SIGNAL -1");
		}

		logger.debug("\n**********************************************"
				+"\nREAD RESULT PTY: \n'"+ readResult.get(INMEMORYDB.ptyKey.PTY.toString()) +"'"
				+"\nREAD RESULT STR: \n'"+ readResult.get(INMEMORYDB.ptyKey.STR.toString()) +"'"
				+"\nREAD RESULT INT: \n'"+ readResult.get(INMEMORYDB.ptyKey.INT.toString()) +"'"
				+"\n**********************************************\n");

		return readResult;

	}


	boolean isFirstLineCmdPhraseIncluded(String firstLine, String cmd) {

		String cmd1 = cmd;
		String cmd2 = cmd.substring(1);
		if (cmd != null && cmd.length() > 1) {
			cmd = cmd.replaceAll("[\\r\\n]", "");
		}
		return firstLine.contains(cmd1) || firstLine.contains(cmd2);
	}

	public String trimExeResult(ArrayList<String> lastInCommingStringArr, String cmd, String prompt) {
		String rsltStr = "";
		for(String s :lastInCommingStringArr){
			System.out.println("##########################  :'"+s+"'");
		}
		System.out.println("+++++++++++    TRIMMING RESULT START +++++++++++");
		int lineCount = lastInCommingStringArr.size();
		if (lineCount > 1) {
			System.out.println("lineCount:" + lineCount);
			System.out.println("lastInCommingStringArr.get(0):"
					+ lastInCommingStringArr.get(0));
			if (isFirstLineCmdPhraseIncluded(lastInCommingStringArr.get(0), cmd)) {
				lastInCommingStringArr.remove(0);
			}
			lineCount = lastInCommingStringArr.size();
			System.out.println("lastInCommingStringArr.get(lastIdx-1):"
					+ lastInCommingStringArr.get(lineCount - 1));
//			if (ptyAnalyzer.isPrompt(lastInCommingStringArr.get(lineCount - 1),
//					0)) {
//				lastInCommingStringArr.remove(lineCount - 1);
//			}

			String result = "";
			int idx = 0;
			for (String s : lastInCommingStringArr) {
				if(idx > 0){
					result = result +"\r\n" +s;
				}else{
					result = s;
				}
				idx++;
			}
			rsltStr = result;
		} else if(lineCount == 1){
			System.out.println("lineCount="+lineCount);
			rsltStr = lastInCommingStringArr.get(0).replaceAll("[\\r\\n]", "");
		}
		else{
			System.out.println("lineCount=0");
			rsltStr = "";
		}
		//
		System.out.println("WILL RETURN: '" + rsltStr + "'");
		System.out.println("+++++++++++    TRIMMING RESULT END +++++++++++\n");
		return rsltStr;
	}



	private static class StreamGobbler extends Thread {
		private InputStream mInputStrea;
		@SuppressWarnings("rawtypes")
		private List mDataResult;

		@SuppressWarnings("rawtypes")
		public StreamGobbler(InputStream is, List DataResult) {
			this.mInputStrea = is;
			this.mDataResult = DataResult;
		}

		@SuppressWarnings("unchecked")
		public void run() {
			super.run();

			this.mDataResult.clear();
			BufferedReader br = null;
			try{
				br = new BufferedReader( new InputStreamReader( this.mInputStrea ) );

				String line = null;
				while( (line = br.readLine()) != null ){
					this.mDataResult.add( line );
				}
			}catch( IOException e ){
				e.printStackTrace();
			}finally{
				try {
					if( br != null )
						br.close();
					this.mInputStrea.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
