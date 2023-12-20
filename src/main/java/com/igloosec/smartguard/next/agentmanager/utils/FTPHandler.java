/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.utils.FTPHandler.java
 * company : Mobigen
 * @author : Je-Joong Lee
 * created at : 2016. 1. 8.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.utils;


import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

public class FTPHandler {

	static Logger logger = LoggerFactory.getLogger(FTPHandler.class);
	Logger agentCommlogger = LoggerFactory.getLogger("agentCommLog");
	 FTPClient ftpClient;
	 
	 private String server ;
	 private int port = 21;
	 private String username = "";
	 private String password = "";
	 
     
     static final long RemoteFileCheckInterval = 1000; // 1 = 1 millisecond
    
     public  FTPHandler(){
    	 this.ftpClient = new FTPClient();
    	 this.ftpClient.addProtocolCommandListener(new PrintCommandListener(new
                 PrintWriter(System.out)));
     }
     
     public  FTPHandler(String rURL, int rPort, String username, String password){
    	this.ftpClient = new FTPClient();
    	this.ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
    	this.server = rURL;
    	this.port = rPort;
    	this.username = username;
    	this.password = password;

//		logger.debug(this.username+ " : "+this.password);
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

   	    	
  	public boolean login(boolean isPassiveMode) throws IOException {

		 logger.debug("FTP Connect. ip = "+this.server+" port = "+this.port);
		 ftpClient.connect(this.server, this.port);
		 showServerReply(ftpClient);
		 logger.debug("FTP login.");
		 boolean isLogin = ftpClient.login(this.username, this.password);
		 showServerReply(ftpClient);
		 if(isPassiveMode){
			 ftpClient.enterLocalPassiveMode();
		 }else{
			 ftpClient.enterLocalActiveMode();
		 }
		 ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

		 return isLogin;
   }
  	public boolean logout(){
  		if(ftpClient==null){
  			return  true;
  		}
  		if(ftpClient.isConnected()){
  			CommonUtils.runSafely(()->ftpClient.logout());
  			CommonUtils.runSafely(()->ftpClient.disconnect());
  		}
  		return true;
  	}

  	
    
 	public boolean fileCheck(String serverDir, String fileName){
 		boolean result = false;
    	try {
			String[] rFileNames = ftpClient.listNames(serverDir);
			for(String fname : rFileNames){
			if(fileName.equalsIgnoreCase(fname)){
				result = true;
				break;
			}	
			}			
		} catch (IOException e) {
			logger.error(e.getMessage());			
		}
    	return result;
    	
    	
    }
 	
 	
 	public boolean uploadFile_test(String remoteFileWPath, String localFileWPath){
 		logger.debug("Upload file to remoteFilePath:"+remoteFileWPath +" , localFileWPath:"+localFileWPath);
 		File uploadFile = new File(localFileWPath);
 		InputStream inputStream;
		try {
			inputStream = new FileInputStream(uploadFile);
			ftpClient.mkd(remoteFileWPath);
			showServerReply(ftpClient);
			boolean isUploaded =ftpClient.storeFile(remoteFileWPath, inputStream);
			showServerReply(ftpClient);
	 		return isUploaded;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
 		
 	}

 	
 	public String uploadFile(String remoteFileWPath, File localFile){
 		
		InputStream inputStream;
		try {
			String scriptFullPath = "";
			String homeDir = ftpClient.printWorkingDirectory();
			if("".equals(homeDir)){
				homeDir = "./";
			}
			
			showServerReply(ftpClient);
			if(homeDir != null && homeDir.contains(remoteFileWPath)){
				if (!homeDir.endsWith("/")) {
					scriptFullPath = homeDir + "/";
				}
			}else{
				ftpClient.mkd(remoteFileWPath);
				String temp = "";
				if (!homeDir.endsWith("/")) {
					temp = homeDir + "/";
				} else {
					temp = homeDir;
				}
				if (!remoteFileWPath.endsWith("/")) {
					scriptFullPath = temp + remoteFileWPath + "/";
				} else {
					scriptFullPath = temp + remoteFileWPath;
				}
			}
			logger.debug("printWorking Dir : "+homeDir);
			logger.debug("Upload file to remoteFilePath:"+scriptFullPath+localFile.getName() +" , localFile:"+localFile.getAbsolutePath());
			agentCommlogger.debug("printWorking Dir : "+homeDir);
			agentCommlogger.debug("Upload file to remoteFilePath:"+scriptFullPath+localFile.getName() +" , localFile:"+localFile.getAbsolutePath());

			inputStream = new FileInputStream(localFile);

			logger.debug("SENDING "+localFile.getName()+" FILE .....");
			agentCommlogger.debug("SENDING "+localFile.getName()+" FILE .....");	

			if(!ftpClient.changeWorkingDirectory(scriptFullPath)){
				logger.warn("== ftp cwd failed");
				CommonUtils.close(inputStream);
				throw new IOException("ftp cwd failed");
			}

			if(!ftpClient.storeFile(scriptFullPath+localFile.getName(), inputStream)){
				logger.warn("== ftp cwd failed");
				CommonUtils.close(inputStream);
				throw new IOException("ftp cwd failed");
			}
			
//			ftpClient.completePendingCommand();
			logger.debug(localFile.getName()+ " FTP FILE UPLOAD COMPLETED.!");
			agentCommlogger.debug(localFile.getName()+ " FTP FILE UPLOAD COMPLETED.!");
			ftpClient.sendSiteCommand("chmod " + "755 " + scriptFullPath+localFile.getName());
			showServerReply(ftpClient);
			
			
			String workingDir = ftpClient.printWorkingDirectory();
			logger.debug("returning workingDir : "+ workingDir);
			agentCommlogger.debug("returning workingDir : "+ workingDir);
//	 		return workingDir;
			CommonUtils.close(inputStream);
			return scriptFullPath;
	 		
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
			agentCommlogger.error("ERROR WHILE FTP FILE UPLOAD :" + e.getMessage());
			return "**ERROR WHILE FTP FILE UPLOAD - FILE NOT FOUND" + e.getMessage(); 
		} catch (IOException e2) {
			logger.error(e2.getMessage(), e2);
			agentCommlogger.error("ERROR WHILE FTP FILE UPLOAD :" + e2.getMessage());
			return "**ERROR WHILE FTP FILE UPLOAD - IO EXCEPTION" + e2.getMessage();
		}
		
 		
 	}
	
 	private static void showServerReply(FTPClient ftpClient) {
        String[] replies = ftpClient.getReplyStrings();
        if (replies != null && replies.length > 0) {
            for (String aReply : replies) {
                logger.debug("SERVER: " + aReply);
            }
        }
    }

	/**
	 * using retrieveFile(String, OutputStream)
	 * **/	
	public boolean downloadFile(String remoteFileWPath, String localFileWPath){
		  
          File downloadFile = new File(localFileWPath);

          boolean success = false;
		try {
			OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
			success = ftpClient.retrieveFile(remoteFileWPath, outputStream);
			outputStream.close();
			if (success) {
	              logger.info(remoteFileWPath + " has been downloaded successfully.");
	          }
		} catch (IOException e) {
			success = false;
			logger.error("FTP File Download IO ERROR = "+e.getMessage());
			e.printStackTrace();
		}
		finally {
//			       logout();
		}
		
		return success;
		
	}
	
	/**
	 * using InputStream retrieveFileStream(String)
	 * **/
	public boolean getRemoteFile2(String remoteFilePath, String localFilePath, String batchFileName, String exeTime){

		String remoteFile = remoteFilePath+"/"+batchFileName;
        File downloadFile = new File(localFilePath+"/"+batchFileName);
        
        boolean success = false;
        try{
        
        OutputStream outputStream2 = new BufferedOutputStream(new FileOutputStream(downloadFile));
        InputStream inputStream = ftpClient.retrieveFileStream(remoteFile);
        byte[] bytesArray = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(bytesArray)) != -1) {
            outputStream2.write(bytesArray, 0, bytesRead);
        }

        success = ftpClient.completePendingCommand();
        if (success) {
        	 logger.info(batchFileName + " has been downloaded successfully.");
        }
        outputStream2.close();
        inputStream.close();

    } catch (IOException ex) {
    	success = false;
        logger.error("Error: " + ex.getMessage());
        ex.printStackTrace();
    } finally {
			        try {
			            if (ftpClient.isConnected()) {
			                ftpClient.logout();
			                ftpClient.disconnect();
			            }
			        } catch (IOException ex) {
			            ex.printStackTrace();
			        }
    			}
        
        return success;
	}

	// FTP의 ls 명령, 모든 파일 리스트를 가져온다
	public FTPFile[] list() {

		FTPFile[] files = null;
		try {
			files = this.ftpClient.listFiles();
			return files;
		} catch (IOException e) {
			logger.error(CommonUtils.printError(e));
		}
		return null;
	}

	public String pwd() {

		String path ="";
		try {
				path = this.ftpClient.printWorkingDirectory();
			return path;
		} catch (IOException e) {
			logger.error(CommonUtils.printError(e));
		}
		return null;
	}

	public void cd(String path) {
		try {
			ftpClient.changeWorkingDirectory(path);
		} catch (IOException e) {
			logger.error(CommonUtils.printError(e));
		}
	}
	

public static void main(String args[]) throws IOException {
	
	System.out.println("UPLOAD TEST :: INPUT MUST BE 'IP PORT ID PW RemoteFile(Full Path) LocalFile(Full Path)' ");
	int argLen = args.length;	
	String IP,PORT,ID,PW,RFile,LFile;
	
	if(argLen != 6){
		System.out.println("Put Proper Arguments argCnt="+ argLen);
		System.out.println("args[0]:"+args[0]);
		System.out.println("args[1]:"+args[1]);
		System.out.println("args[2]:"+args[2]);
		System.out.println("args[3]:"+args[3]);
		System.out.println("args[4]:"+args[4]);
		System.out.println("args[5]:"+args[5]);
		
	}else{
		IP = args[0];
		PORT = args[1];
		ID = args[2];
		PW = args[3];
		RFile = args[4];
		LFile = args[5];		
		
		FTPHandler ftp = new FTPHandler(IP, Integer.parseInt(PORT),ID,PW);

//		ftp.login();

		ftp.uploadFile(RFile, new File(LFile));
	}
		
	
	
}

}