/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.utils.SocketClient.java
 * company : Mobigen
 * @author : Oh su jin
 * created at : 2016. 5. 3.
 * description :
 */

package com.igloosec.smartguard.next.agentmanager.utils;

import com.igloosec.smartguard.next.agentmanager.entity.AgentInfo;
import com.igloosec.smartguard.next.agentmanager.entity.JobEntity;
import com.igloosec.smartguard.next.agentmanager.entity.SocketJobEntity;
import com.igloosec.smartguard.next.agentmanager.exception.SnetCommonErrCode;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;

import com.igloosec.smartguard.next.agentmanager.memory.ManagerJobFactory;
import com.igloosec.smartguard.next.agentmanager.services.AgentVersionManager;
import com.igloosec.smartguard.next.agentmanager.services.ConfigGlobalManager;
import com.sk.snet.manipulates.PatternMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SocketClient {

    Logger logger = LoggerFactory.getLogger(getClass());
    Logger healthCheckLog = LoggerFactory.getLogger("HealthCheckLog");
    
    String ip;
    String port;
    String agentKey;
    Socket socket = null;
    SSLSocket sslSocket = null;
    DataOutputStream dos = null;
    BufferedOutputStream bos = null;
    DataInputStream dis = null;
    BufferedInputStream bis = null;
    boolean useSSL = INMEMORYDB.useSSL;
//    Integer timeOut=null;
//    Integer connectionRetry=null;
    PatterBuilder patterBuilder = new PatterBuilder();
    public String keyStorePath = INMEMORYDB.KEYSTOREPATH;
    public String kspmanner = PatternMaker.KS_PMANNER;
    public String hkspmanner = PatternMaker.KS_PMANNER;

    JobEntity jobEntity;

    public SocketClient(){
    	initPMan();
    }
    
    public SocketClient(SocketJobEntity jobEntity) throws Exception {

        if (jobEntity.getAgentInfo().getRelayIpAddress() != null ){
            this.ip = jobEntity.getAgentInfo().getRelayIpAddress();
            this.port = INMEMORYDB.RELAY_PORT;
        }else if(jobEntity.getJobType().equalsIgnoreCase(ManagerJobFactory.RELAYHEALTHCHECK)) {
        	
        	if (jobEntity.getAgentInfo().getRelayIpAddress() != null)
        		this.ip = jobEntity.getAgentInfo().getRelayIpAddress();
        	else 
        		this.ip = jobEntity.getAgentInfo().getConnectIpAddress();
        	
            this.port = INMEMORYDB.RELAY_PORT;
        }else{
            this.ip = jobEntity.getAgentInfo().getConnectIpAddress();
            this.port = INMEMORYDB.AGENT_PORT;
        }

        this.agentKey = jobEntity.getAgentInfo().getAssetCd();
        this.jobEntity = jobEntity;
        initSocket(jobEntity.getConnectionRetry(), jobEntity.getConnectionTimeOut());
    }

    public SocketClient(JobEntity jobEntity) throws Exception {
    	if (jobEntity.getAgentInfo().getRelayIpAddress() != null){
            this.ip = jobEntity.getAgentInfo().getRelayIpAddress();
            this.port = INMEMORYDB.RELAY_PORT;
        }else{
            this.ip = jobEntity.getAgentInfo().getConnectIpAddress();
            this.port = INMEMORYDB.AGENT_PORT;
        }

    	this.agentKey = jobEntity.getAgentInfo().getAssetCd();
    	this.jobEntity = jobEntity;        
    	initSocket();
    }
    
    public SocketClient(String ip , String port) throws Exception {
        this.ip = ip;
        this.port = port;
        this.agentKey = "AC_TEST";
//        this.keyStorePath = "C:\\workspaces\\SNETPOC\\AgentManager\\manager\\secure\\snetsecure";
        this.keyStorePath = "/usr/local/snetManager/manager/libs/secure/snetsecure";
        initSocket();
    }

    //HealthChk 전용 소켓
    public SocketClient(AgentInfo ai) throws Exception {
        if (ai.getRelayIpAddress() != null){
            this.ip = ai.getRelayIpAddress();
            this.port = INMEMORYDB.RELAY_PORT;
        }else{
            this.ip = ai.getConnectIpAddress();
            this.port = INMEMORYDB.AGENT_PORT;
        }

        this.agentKey = ai.getAssetCd();
        logger.debug("MAKE SOCKET CLIENT : this.ip:"+this.ip +" this.port:"+this.port + " this.agentKey:"+this.agentKey);
        jobEntity = new JobEntity();
        jobEntity.setAgentInfo(ai);
        initSocket();
    }
    
    private void initPMan(){
    	hkspmanner = PatterBuilder.getKSPatterns(PatterBuilder.getTypeVal());
    }
    
    private void initSocket(){
    	try {
          Thread.currentThread();
          Thread.sleep(500);
      } catch (InterruptedException e) {
          logger.error(CommonUtils.printError(e));
      }
    	//CHECK AGENT'S SOCKET TYPE
    	boolean isSSL = true;
    	this.useSSL = isSSL;

    	try {
            if(jobEntity.getAgentInfo().getRelayIpAddress() != null) {
                createSSLSocketRelay();
            } else
            	if(isSSL){
    			createSSLSocket();
            } else{
    			createNoSSLSocket();
    		}
    		healthCheckLog.debug("connection Successes " + agentKey+" Server SSL=" + isSSL);
    		
    	} catch (IOException e) {
    		if(isSSL){
    			try {
					this.sslSocket.close();
				} catch (IOException e1) {}
    		}else{
    			try {
					this.socket.close();
				} catch (IOException e1) {}
    		}
    		healthCheckLog.error("socket connection Fail. " + agentKey + " Server SSL="+useSSL);
    	}
    	
    	
    }
    
    private void initSocket(int retryCnt, int timeout) throws Exception {

    	//CHECK AGENT'S SOCKET TYPE
    	boolean isSSL = true;
      	this.useSSL = isSSL;

    	try {
            if(jobEntity.getAgentInfo().getRelayIpAddress() != null) {
                createSSLSocketRelay();
            }else if(isSSL){
    			createSSLSocket(timeout);
    		}else{    			
    			createNoSSLSocket(timeout);
    		}
    		useHealthChkLog(true, "connection Successes " + agentKey+" Server SSL=" + isSSL);
    		
    	} catch (IOException e) {
    		if(isSSL){
    			try {
					this.sslSocket.close();
				} catch (IOException e1) {
					healthCheckLog.error("SSL socket close Exception :: {}", e1.getMessage(), e1.fillInStackTrace());
				}
    		}else{
    			try {
					this.socket.close();
				} catch (IOException e1) {
					healthCheckLog.error("socket close Exception :: {}", e1.getMessage(), e1.fillInStackTrace());
				}
    		}
    		healthCheckLog.error("socket connection Fail. " + agentKey + " Server SSL="+isSSL);
    		throw new Exception(e);
    	}
    	
    	
    	
    }
    

    
    public void createSSLSocket() throws IOException, NumberFormatException {
    	try {

            logger.debug("@@@@ KEYSTOREPATH : "+keyStorePath);
    	System.setProperty("javax.net.ssl.trustStore",keyStorePath);
		System.setProperty("javax.net.ssl.trustStorePassword",kspmanner);
		SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

			this.sslSocket = (SSLSocket)sslsocketfactory.createSocket(ip, Integer.parseInt(port));
			this.dis = new DataInputStream(this.sslSocket.getInputStream());
			this.dos = new DataOutputStream(this.sslSocket.getOutputStream());
    	}catch (NumberFormatException e1) {
			throw new NumberFormatException(e1.getMessage());
		}catch (IOException e2) {
			throw new IOException(e2.getMessage());
		}
		
    }

    public void createSSLSocketRelay() throws IOException, NumberFormatException {
        try {

            System.setProperty("javax.net.ssl.trustStore",keyStorePath);
            System.setProperty("javax.net.ssl.trustStorePassword",kspmanner);
            SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

            this.sslSocket = (SSLSocket)sslsocketfactory.createSocket(ip, Integer.parseInt(port));
            this.dis = new DataInputStream(this.sslSocket.getInputStream());
            this.dos = new DataOutputStream(this.sslSocket.getOutputStream());
        }catch (NumberFormatException e1) {
            throw new NumberFormatException(e1.getMessage());
        }catch (IOException e2) {
            throw new IOException(e2.getMessage());
        }

    }
    
    public void createSSLSocket(int timeOut) throws IOException, NumberFormatException {
    	try {
    		System.setProperty("javax.net.ssl.trustStore",keyStorePath);
    		System.setProperty("javax.net.ssl.trustStorePassword",kspmanner);
    		SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
    		
    		this.sslSocket = (SSLSocket)sslsocketfactory.createSocket();
    		sslSocket.connect(new InetSocketAddress(ip, Integer.parseInt(port)), timeOut);
    		
    		this.dis = new DataInputStream(this.sslSocket.getInputStream());
    		this.dos = new DataOutputStream(this.sslSocket.getOutputStream());
    	}catch (NumberFormatException e1) {
    		throw new NumberFormatException(e1.getMessage());
    	}catch (IOException e2) {
    		throw new IOException(e2.getMessage());
    	}
    	
    }
    public void createNoSSLSocket() throws IOException, NumberFormatException {
    	try {
			this.socket = new Socket(ip, Integer.parseInt(port));
			this.dis = new DataInputStream(socket.getInputStream());
			this.dos = new DataOutputStream(socket.getOutputStream());
		}catch (NumberFormatException e1) {
			throw new NumberFormatException(e1.getMessage());
		}catch (IOException e2) {
			throw new IOException(e2.getMessage());
		}
    	 
    }

    public void createNoSSLSocket(int timeOut) throws IOException, NumberFormatException {
//    	try {
    		this.socket = new Socket();
    		socket.connect(new InetSocketAddress(ip, Integer.parseInt(port)), timeOut);
    		this.dis = new DataInputStream(socket.getInputStream());
    		this.dos = new DataOutputStream(socket.getOutputStream());
//    	}catch (NumberFormatException e1) {			
//    		throw new NumberFormatException(e1.getMessage());
//    	}catch (IOException e2) {			
//    		throw new IOException(e2.getMessage());
//    	}
    	
    }
    
    
    
    public String[] reciveHeader(JobEntity jobEntity) throws Exception {
        String[] header=null;

        /**
         * Header 정보 수신
         * */
        try {
            String rcvHeader;
            rcvHeader = dis.readUTF();
            header = rcvHeader.split("\\|");
            jobEntity.setJobType(header[0]);
            jobEntity.setFileName(header[2]);
            logger.debug("Receive header :: "+rcvHeader);
            logger.debug("Job Type : " + header[0]);

            return header;
        }catch (EOFException e){
            logger.error(CommonUtils.printError(e));
            throw new EOFException(SnetCommonErrCode.ERR_0004.getMessage());
        }catch (Exception e){
            logger.error(CommonUtils.printError(e));
            throw new Exception(e.getMessage());
        }
    }


    public void receiveFile(JobEntity jobEntity, String[] header) throws Exception {

        try {

            int fileTransferCount = 0;
            long fileTransferSize = 0;
            String recvPath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.RECV,jobEntity.getJobType());
            File[] files;
            logger.debug("파일 수신 작업을 시작합니다. =>" + recvPath);

            //파일개수
            fileTransferCount = Integer.parseInt(header[1]);

            files = new File[fileTransferCount];

            for(int i = 0; i < fileTransferCount ; i++){
                files[i] = new File(recvPath + jobEntity.getFileName()+"."+header[3+i*2]);
                fileTransferSize = Integer.parseInt(header[4+i*2]);

                /***/
                if (!files[i].exists()) files[i].createNewFile();

                this.bos =
                        new BufferedOutputStream(
                                new FileOutputStream(files[i],false));


                int bufSize = 4096;
                byte[] buffer = new byte[bufSize];

                long total = 0;
                int count = 0;
                while (total < fileTransferSize && (count = dis.read(buffer, 0, fileTransferSize-total > buffer.length ? buffer.length : (int)(fileTransferSize-total))) > 0)
                {
                    bos.write(buffer, 0, count);
                    total += count;
                }
                bos.close();
                /***/
            }

            logger.debug("파일 수신 작업을 완료하였습니다.");
        }catch (EOFException e){
            logger.error(CommonUtils.printError(e));
            throw new EOFException(SnetCommonErrCode.ERR_0004.getMessage());
        }

    }


    public void sendHeader(File[] files, JobEntity jobEntity) throws Exception {
        try {	
            logger.debug("Send [ JobType :: {}, Asset_CD :: {}  ]", jobEntity.getJobType(), jobEntity.getAgentInfo().getAssetCd());
            String header="";

            /**
             * Header 정보 전송
             * */
            //header = makeHeader(files,jobEntity);
            header = "삭제예정";
            if(dos!=null)
            	dos.writeUTF(header);            
        }catch (Exception e){
            logger.error("SendHeader Exception :: {} ", e.getMessage(), e.fillInStackTrace());
            throw new Exception(SnetCommonErrCode.ERR_0004.getMessage());
        }
    }
    
    public void sendCMD(String msg, JobEntity jobEntity) throws Exception {
        try {	
            logger.debug("Send [ JobType :: {}, Asset_CD :: {}  ]", jobEntity.getJobType(), jobEntity.getAgentInfo().getAssetCd());
            String header="";
            String split = "|";
            /**
             * Header 정보 전송
             * */

            if (jobEntity.getAgentInfo().getRelayIpAddress() != null){
                header = jobEntity.getJobType()+split+"R"+split+msg + "[R]" + jobEntity.getAgentInfo().getConnectIpAddress();
            }else {
                header = jobEntity.getJobType()+split+"R"+split+msg;
            }

            dos.writeUTF(header);
        }catch (Exception e){
            logger.error(CommonUtils.printError(e));
            throw new Exception("Send CMD MSG Fail.");
        }

    }
    
    public void sendUpdIp(String msg, JobEntity jobEntity) throws Exception {
        try {	
            logger.debug("Send [ JobType :: {}, Asset_CD :: {}  ]", jobEntity.getJobType(), jobEntity.getAgentInfo().getAssetCd());
            String header="";
            String split = "|";
            /**
             * Header 정보 전송
             * */
            header = jobEntity.getJobType()+split+msg+ "[R]" + jobEntity.getAgentInfo().getConnectIpAddress();
            logger.debug("SEND UPDATED IP LIST TO: ASSET_CD:"+jobEntity.getAgentInfo().getAssetCd() + ", IP:"+jobEntity.getAgentInfo().getConnectIpAddress() +",header="+header);
            
            dos.writeUTF(header);
        }catch (Exception e){
            logger.error(CommonUtils.printError(e));
            throw new Exception("Send CMD MSG Fail.");
        }

    }
    
    public String receiveStream(String cmd){
    	String ret = "";
    	try {
			ret = dis.readUTF();
			
			logger.info("For ["+cmd+"] RECEIVED MSG:"+ret);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return ret;
    }
   
    
   

    public void sendFile(File[] files) throws Exception {

        try {	
            for(File file :files){
                
                int bufSize = (int)file.length();
                byte[] buf = new byte[bufSize];
                int bytesRead = 0;
                //send file

                FileInputStream fis = new FileInputStream(file);

                while ((bytesRead = fis.read(buf)) != -1) {
                    dos.write(buf, 0, bytesRead);
                }
                fis.close();
                dos.flush();
                logger.info(file.getName() + " SENT."+bufSize);
            }
        }catch (Exception e){
            logger.error(CommonUtils.printError(e));
            throw new Exception("Send File Fail." + e.getMessage());
        }


    }

    public void closeSocket(){
        try {

            if(this.dos != null) this.dos.close();
        	if(this.dis != null) this.dis.close();
            if(this.sslSocket != null)this.sslSocket.close();
            if(this.socket != null){this.socket.close();}
            logger.debug("connection Close " + agentKey + " Server");
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
        }
    }
    
    public boolean isAgentSSLCon(){
    	return isAgentSSLCon(this.ip, this.port, false);
    }

    public boolean isAgentSSLCon(boolean useHealthChkLog){
    	return isAgentSSLCon(this.ip, this.port, useHealthChkLog);
    }
    
    public boolean isAgentSSLCon(String agentip, String agentport, boolean useHealthChkLog){
    	boolean retbool = false;
    	
    	
    	String command = "openssl s_client -connect "+agentip+":"+agentport;
    	
//    	logger.info("[CHK AGENT IS SSL] cmd = " + command);
    	useHealthChkLog(useHealthChkLog, "[CHK AGENT IS SSL] cmd = " + command);
	 	Process p;
	 	StringBuffer output = new StringBuffer();
	 	String line = "";
	 	int lineCnt = 0;
	 	boolean noReply = false;
		try {
//			ExecutorService executor = Executors.newFixedThreadPool(2);
			ExecutorService executor = INMEMORYDB.executorThreadPool;
//			logger.debug("IS executor shutdown?"+executor.isShutdown() +" IS executor terminated?"+executor.isTerminated());
			useHealthChkLog(useHealthChkLog, "IS executor shutdown?"+executor.isShutdown() +" IS executor terminated?"+executor.isTerminated());
			Future<String> future;
			
			
			 p = Runtime.getRuntime().exec(command);
			 					
				try {
					BufferedReader br =new BufferedReader(new InputStreamReader(p.getInputStream()));
					

			    	Callable<String> readTask = new Callable<String>() {
						@Override
						public String call() throws Exception {
							String line=br.readLine();
							return line;
						}
					};
					int waitFor = 3000;
					while(lineCnt < 10){
						if(lineCnt >= 1){waitFor=500;}
						future = executor.submit(readTask);
						line = future.get(waitFor, TimeUnit.MILLISECONDS);
						output.append(line);
						lineCnt++;
					}
					
					br.close();
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				} catch (TimeoutException e) {
					
				}
				finally{
					if(lineCnt < 1){noReply = true;}
					p.destroy();
//					executor.shutdown();
					
				}
				String conLog = output.toString();
//				logger.debug("THE SSL HANDSHAKE LOG : "+conLog);
				useHealthChkLog(useHealthChkLog, "THE SSL HANDSHAKE LOG : "+conLog);
				
				if(conLog.toUpperCase().contains("CONNECTED") && conLog.toUpperCase().contains("CERTIFICATE")){
//					logger.debug(agentip+":"+agentport+" !! USING SSL !!");
					useHealthChkLog(useHealthChkLog, agentip+":"+agentport+" !! USING SSL !!");
					retbool = true;
			
				}
				else if(conLog.toUpperCase().contains("CONNECTION") && conLog.toUpperCase().contains("REFUSED")){
//					logger.debug(agentip+":"+agentport+" !! CANNOT CONNECT !!");
					useHealthChkLog(useHealthChkLog, agentip+":"+agentport+" !! CANNOT CONNECT !!");
					retbool = false;
				}else{
//					logger.debug(agentip+":"+agentport+" !! NO SSL !!");
					useHealthChkLog(useHealthChkLog, agentip+":"+agentport+" !! NO SSL !!");
					retbool = false;
				}
				

		} catch (IOException e) {
			e.printStackTrace();
		}
		if(!noReply){
			try {
				//FOR CONNECTION CLOSE-WAIT, TIME-WAIT
				Thread.currentThread().sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
    	return retbool;
    }
    
    /**
     * true  :: 헬스체크용 로그만 -agentManager_healthchk.log
     * false :: 기본 로그 사용 - agentManager.log
     * @param useLog
     * @param message
     */
    private void useHealthChkLog(boolean useLog, String message){
    	if(useLog){
    		healthCheckLog.debug(message);
    	}else{
    		logger.debug(message);
    	}
    }
    

    public static void main(String args[]) throws Exception {
    	
    }
}
