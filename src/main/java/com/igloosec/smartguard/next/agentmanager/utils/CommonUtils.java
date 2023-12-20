/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.utils.DateUtil.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2015. 12. 4.
 * description : 
 */
package com.igloosec.smartguard.next.agentmanager.utils;

import com.igloosec.smartguard.next.agentmanager.api.model.SmartGuardToken;
import com.igloosec.smartguard.next.agentmanager.crypto.AESCryptography;
import com.igloosec.smartguard.next.agentmanager.entity.JobEntity;
import com.igloosec.smartguard.next.agentmanager.exception.SnetCommonErrCode;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.memory.ManagerJobFactory;

import com.sk.snet.manipulates.PatternMaker;
import jodd.util.StringUtil;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;

/**
 * Created by osujin12 on 2016. 2. 18..
 */
public class CommonUtils {

	private static Logger logger = LoggerFactory.getLogger(CommonUtils.class);
			
    public static String makeFileName(){
        Random random = new Random();
        long rand = random.nextLong();

        if(rand < 0) rand = rand*-1;

        return String.valueOf(rand);
    }

    public static String printError(Exception e){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        e.printStackTrace (new PrintStream(os));

        return new String(os.toByteArray ());
    }

    public static boolean fileCopy(String inFileName, String outFileName) {
        try {
            FileInputStream fis = new FileInputStream(inFileName);
            FileOutputStream fos = new FileOutputStream(outFileName);

            int data = 0;
            while((data=fis.read())!=-1) {
                fos.write(data);
            }
            fis.close();
            fos.close();
            return true;
        } catch (IOException e) {
            logger.error(CommonUtils.printError(e));
            return false;
        }
    }


    /**
     * return HHmm
     **/
    public static boolean compareDiagnosisDate(String start, String end ){

        @SuppressWarnings("unused")
		DateUtil dateUtil = new DateUtil();
        String nowYYYYmmdd = DateUtil.getCurrDate();

        start = nowYYYYmmdd + start;
        end = nowYYYYmmdd + end;

        long nowT = DateUtil.getCurTimeInMilis();
        long startT = 0;
        long endT = 0;


        startT = DateUtil.StringToMili(start);
        endT = DateUtil.StringToMili(end);


        if(startT >= endT){
            endT += 60*60*1000*24;
        }

        return startT <= nowT && nowT <= endT;
    }

    public static boolean moveFile(String orgiFile, String movePath){

        File uploadFile = new File(orgiFile);
        File moveFile = new File(movePath);


        return uploadFile.renameTo(moveFile);
    }
    
    public static boolean isOpenPort(String host, int port) {
		boolean result = false;
		Socket socket = new Socket();
		try {
			logger.debug("Test connection Ip :: {} :: {}", host, port);
			socket.connect(new InetSocketAddress(host, port), 1000);
			result = true;
			logger.debug("open Ip :: {} :: open port :: {}", host, port);
			socket.close();
		} catch (Exception e) {
			logger.debug("Exception Ip :: {} :: open port :: {}  Exception :: [ {} ]", host, port, e.getMessage());
			return false;
		} finally {
			if(socket !=null){
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}




    @SuppressWarnings("resource")
	public static String findFileEncoding(String fileFullPath) throws IOException {
        byte[] buf = new byte[4096];

        java.io.FileInputStream fis = new java.io.FileInputStream(fileFullPath);

//         (1)
        UniversalDetector detector = new UniversalDetector(null);

//         (2)
        int nread;
        while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
            detector.handleData(buf, 0, nread);
        }
//         (3)
        detector.dataEnd();

//         (4)
        String encoding = detector.getDetectedCharset();
        if (encoding != null) {
//            System.out.println("Detected encoding = " + encoding);
        } else {
//            System.out.println("No encoding detected.");
        }

//         (5)
        detector.reset();

        if (encoding == null) encoding = "";

        if (fis != null) fis.close();

        return encoding;
    }

    @SuppressWarnings("unused")
	public static void convertUTF8(JobEntity jobEntity) throws IOException, InterruptedException {

        String path = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.RECV, jobEntity.getJobType());
        String fileName = jobEntity.getFileName();
        String fileFullPath = path + fileName + jobEntity.getFileType();

        String charSet = findFileEncoding(fileFullPath);

        if (jobEntity == null)
            jobEntity = new JobEntity();

        logger.debug("### Character Set :: "+charSet);
        try {
            if (charSet.equalsIgnoreCase(INMEMORYDB.EUC_KR)) {
                logger.debug("Receive EUC-KR file.. Start Converting to UTF-8");
                String backupFile = path + fileName + INMEMORYDB.BAK;

                //euc-kr backup
                if (moveFile(fileFullPath,backupFile)){
                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(backupFile), "euc-kr"));
                    BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileFullPath), "UTF-8"));
                    String s;

                    while ((s = br.readLine()) != null) {
                        wr.write(s);
                        wr.newLine();
                    }

                    if (br != null && wr != null) br.close(); wr.close();

                    //remove backup file
                    removeFile(backupFile);
                }
            } else if(charSet.contains(INMEMORYDB.ISO_8859_1) || charSet.contains("ISO") || jobEntity.getSwNm().toUpperCase().contains("MSSQL") || jobEntity.getSwNm().toUpperCase().contains("WINDOWS") ){
                if(!charSet.contains("UTF")){
                    logger.debug("Receive iso-8859-1 file.. Start Converting to UTF-8");
                    String backupFile = path + fileName + INMEMORYDB.BAK;

                    //euc-kr backup
                    if (moveFile(fileFullPath,backupFile)){
                        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(backupFile), "ISO-8859-1"));
                        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileFullPath), "UTF-8"));
                        String s;

                        while ((s = br.readLine()) != null) {
                            wr.write(new String(s.getBytes("ISO_8859_1"),"EUC-KR"));
                            wr.newLine();
                        }

                        if (br != null && wr != null) br.close(); wr.close();

                        //remove backup file
                        removeFile(backupFile);
                    }
                }
            } else {
                if (!jobEntity.getJobType().equals(ManagerJobFactory.SRVMANUALDGFIN)) {

                    // 스크립트로 한글깨짐 처리할때 문제가 발생하여 자바로 한글깨짐 처리 변경
                    String backupFile = path + fileName + INMEMORYDB.BAK;
                    if (moveFile(fileFullPath,backupFile)){
                        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(backupFile), "UTF-8"));
                        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileFullPath), "UTF-8"));
                        String s;

                        while ((s = br.readLine()) != null) {
                            wr.write(s);
                            wr.newLine();
                        }

                        if (br != null && wr != null) br.close(); wr.close();

                        //remove backup file
                        removeFile(backupFile);
                    }

                }
            }
        }catch (NullPointerException e){}

    }

    @SuppressWarnings("static-access")
	public static void makeZipFile(JobEntity jobEntity) throws Exception {
        ArrayList<File> files = new ArrayList<>();
        String sendPath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.SEND, jobEntity.getJobType());

        new File(sendPath + jobEntity.getFileName() + INMEMORYDB.ZIP).delete();
        Thread.sleep(5);

        String sendFile = sendPath + jobEntity.getFileName() + jobEntity.getFileType();

        // log4j checker 넣을지
        // Log4J CVE 취약점 수집 사용 여부 확인
        if (INMEMORYDB.useLog4JChecker) {
            if (jobEntity.getAgentInfo().getOsType().toUpperCase().contains(INMEMORYDB.WIN_ID)) {
                String l4jPath = sendPath + jobEntity.getSOTP() + File.separator;
                String scan = l4jPath + INMEMORYDB.Log4JScan;
                String bat = l4jPath + INMEMORYDB.Log4JChker;
                String parVbs = l4jPath + INMEMORYDB.Lgg4JParse;
                String findVbs = l4jPath + INMEMORYDB.Log4Jfind;
                String conf = l4jPath + INMEMORYDB.useLog4JConfig;

                files.add(new File(scan));
                files.add(new File(bat));
                files.add(new File(parVbs));
                files.add(new File(findVbs));
                files.add(new File(conf));
            } else {
                String l4jPath = sendPath + jobEntity.getSOTP() + File.separator;
                String scan = l4jPath + INMEMORYDB.Log4JScanLinux;
                String scanjava = l4jPath + INMEMORYDB.Log4JScanUnix;
                String bat = l4jPath + INMEMORYDB.Log4JChkerUnix;
                String conf = l4jPath + INMEMORYDB.useLog4JConfig;

                if (jobEntity.getAgentInfo().getOsType().toUpperCase().contains("LINUX")) {
                    files.add(new File(scan));
                } else {
                    files.add(new File(scanjava));
                }
                files.add(new File(bat));
                files.add(new File(conf));
            }
        }

        files.add(new File(sendFile));

        if ((INMEMORYDB.GetOptUse == 1) && (!StringUtil.isEmpty(INMEMORYDB.GetDisableFunc))) {
            BufferedWriter dsd = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sendPath + File.separator + INMEMORYDB.DgScriptDiasbleFile), "utf-8"));
            dsd.write(INMEMORYDB.GetDisableFunc);
            dsd.close();

          //  String dsdMove = sendPath + File.separator + jobEntity.getFileName() + INMEMORYDB.DgScriptDisableExt;
          //  CommonUtils.moveFile(sendPath + File.separator + INMEMORYDB.DgScriptDiasbleFile, dsdMove);
            files.add(new File(sendPath + File.separator + INMEMORYDB.DgScriptDiasbleFile));
        }

        new ZipUtil().makeZip(files, jobEntity);
        jobEntity.setFileType(INMEMORYDB.ZIP);
    }

    public static String makeZipFileForAES256(JobEntity jobEntity) throws Exception {

        ArrayList<File> files = new ArrayList<>();
        String sendPath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.SEND, jobEntity.getJobType());

        new File(sendPath + jobEntity.getFileName() + jobEntity.getFileType()).delete();
        Thread.sleep(5);


        String sendFile1 = sendPath + jobEntity.getFileName() + INMEMORYDB.DES;
        //--phil 해쉬파일 넣을지 뺄지 추후 결정
        String sendFile2 = sendPath + jobEntity.getFileName() + INMEMORYDB.HASH;
        String sendFile3 = sendPath + jobEntity.getFileName() + INMEMORYDB.IV;
        String sendFile4 = sendPath + jobEntity.getFileName() + INMEMORYDB.SALT;

        files.add(new File(sendFile1));
        files.add(new File(sendFile2));
        files.add(new File(sendFile3));
        files.add(new File(sendFile4));

        new ZipUtil().makeZip(files, jobEntity);
        jobEntity.setFileType(INMEMORYDB.ZIP);

        return sendPath + jobEntity.getFileName() + INMEMORYDB.ZIP;
    }

    public static void unZipFile(JobEntity jobEntity) {

        String recvPath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.RECV,jobEntity.getJobType());

        if (jobEntity.isUploadLogOnly()) {
            recvPath = jobEntity.getUploadLogOnlyPath();
        }

        String recvZipFile = recvPath + jobEntity.getFileName() + INMEMORYDB.ZIP;

        try {
            new ZipUtil(recvZipFile,recvPath).unzip();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static void unSrvZipFile(JobEntity jobEntity) {

        String recvPath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.RECV,jobEntity.getJobType()) + jobEntity.getFileName() + INMEMORYDB.ZIP;

        try {
            new ZipUtil(recvPath,jobEntity.getDiagDir()).unzip();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private static void removeFile(String deleteFile){
        //remove backup file
        File file = new File(deleteFile);
        file.delete();
    }
    
   
	
	public static String toMux(String o){
		int total = o.length();    	
    	String r = "";
    	String[] temparr = new String[total];
    	for(int i=0; i < total;i++){
    		temparr[i] = String.valueOf(o.charAt(i));
    		r = PatternMaker.toCS(temparr[i])+r;
    	}
		return r; 
	}
	
	public static boolean serverListening(String host, int port)
	{
	     Socket s = null;
	     try
	     {
	         s = new Socket(host, port);
	         return true;
	     }
	     catch (Exception e)
	     {
	         return false;
	     }
	     finally
	     {
	         if(s != null)
	             try {s.close();}
	             catch(Exception e){}
	     }
	}

	public static void mkdir(String path) {
		try {
			File file = new File(path);
			if (!file.exists() && !file.isDirectory()) {
				logger.debug("mkdir path:: {}",path);
				file.mkdirs();
                String osType = System.getProperty("os.name").toLowerCase();
                if (!osType.contains("win")) {
                    Runtime.getRuntime().exec("chown -R sgweb:sgweb " + path);
                }
			}
		} catch (Exception e) {
			logger.error("Mkdir Exception :: " + e.getMessage(), e);
		}
	}

	public static void deleteFile(String path){
        try {
            File f = new File(path);
            if(f.isFile() && f.exists()){
                f.delete();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void deleteDirectory(File path){
        try {
            if(!path.exists()){
                return;
            }

            File[] files = path.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else{
                    file.delete();
                }
            }

            path.delete();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

	
	public static void close(Closeable s){
		if(s!=null){
			try {
				s.close();
			} catch (Throwable e) {
				logger.error("[ignore] close : " + e.getMessage(), e);
			}
		}
	}
	
	
	public static void sleep(int second){
		try {
			Thread.sleep(second * 1000);
		} catch (Throwable e) {
			logger.error("[ignore] sleep : " + e.toString());
		}
	}
	
  	
  	public static interface SafeRunnable{
  		public void run() throws Exception;
  	}
  	
  	
  	public static void runSafely(SafeRunnable r){
  		try {
  			r.run();
		} catch (Throwable e) {
			logger.error("== ignore", e);
		}
  	}

  	public static void makeScriptHashList(JobEntity jobEntity) throws Exception {
        logger.debug("start to get cheksum value. - " + jobEntity.getJobType());
        BufferedOutputStream bs = null;
        try {
            String section = "[" + jobEntity.getAuditFileName() + "]";
            String hashPath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.SEND, jobEntity.getJobType()) + jobEntity.getSOTP()+INMEMORYDB.HASH;
            bs = new BufferedOutputStream(new FileOutputStream(hashPath, true));
            bs.write(section.getBytes());
            bs.write(System.lineSeparator().getBytes());
            bs.write(jobEntity.getInfoChecksumHash().getBytes());
            jobEntity.setHashTxt(hashPath);
        } catch (Exception e) {
            logger.error("program Decryption Fail.");
            throw new Exception(SnetCommonErrCode.ERR_0042.getMessage());
        }finally {
            bs.close();
        }
    }

    public static void deleteZipFile(JobEntity jobEntity) throws Exception {
        String recvPath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.RECV,jobEntity.getJobType());
        String recvZipFile = recvPath + jobEntity.getFileName() + INMEMORYDB.ZIP;
        new File(recvZipFile).delete();
        Thread.sleep(5);
    }

    public static void isValidHash(JobEntity jobEntity) throws Exception {
        String recvPath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.RECV,jobEntity.getJobType());

        AESCryptography aesCryptography = new AESCryptography();
        String hash = aesCryptography.encryption(Paths.get(recvPath + jobEntity.getFileName() + INMEMORYDB.DES));
        logger.debug(jobEntity.getJobType() + " script checksum hash : " + hash);
        FileInputStream fis = new FileInputStream(recvPath + jobEntity.getFileName() + INMEMORYDB.HASH);
        BufferedReader br = new BufferedReader( new InputStreamReader( fis ) );
        String line = "";
        while ((line = br.readLine()) != null) {
            if (!line.contains(jobEntity.getFileName() + INMEMORYDB.DES)) {
                break;
            }
        }

        if (line.equals(hash)) {
            logger.debug("valid hash check.");
        } else {
            throw new Exception(SnetCommonErrCode.ERR_0018.getMessage());
        }
    }

    public static void deleteFiles(String path, String... special) {

        File file = new File(path);
        if (file.exists()) {
            for (File f : file.listFiles()) {
                if (f.isDirectory()) {
                    continue;
                }
                if (special != null && special.length > 0) {
                    for (String s : special) {
                        if (f.getName().contains(s)) {
                            f.delete();
                        }
                    }
                } else {
                    f.delete();
                }
            }
        }
    }

    public static String replacePathSeperator(String path) {
        String convert = "";
        if (INMEMORYDB.MANAGER_OS_TYPE.toLowerCase().contains("win")) {
            convert = path.replaceAll("/", Matcher.quoteReplacement(File.separator));
            if (!path.contains("c:")) {
                convert = "c:" + convert;
            }
        }

        return convert;
    }

    public static String getManagerCdFromAuth(HttpServletRequest request) throws Exception {

        SmartGuardToken sgToken = new SmartGuardToken();
        sgToken.isValid(request.getHeader("Authorization"));

        return sgToken.getManagerCd();
    }

    public static String getWinPath(String orgPath) {
        String winPath = orgPath;

        String osType = System.getProperty("os.name").toLowerCase();
        if (osType.contains("win")) {
            String orgDir2 = StringUtil.replace(winPath, "/usr/local/snetManager", "c:\\usr\\local\\snetManager");
            winPath = StringUtil.replace(orgDir2, INMEMORYDB.PATH_SLASH_UNIX, INMEMORYDB.PATH_SLASH_WIN);
        }

        return winPath;
    }

    public static boolean hasAndKey(String value, String... keys) {
        for (String key : keys) {
            if (!value.contains(key)) {
                return false;
            }
        }
        return true;
    }

    public static String replaceOneOfAll(String src, String wants, String... destS) {
        String replaced = src;

        for (String dest : destS) {
            if (src.contains(dest)) {
                replaced = StringUtil.replace(src, dest, wants);
                break;
            }
        }

        return replaced;
    }
	
    public static void main(String args[]) {

   	 System.out.println("IS SERVICE ALIVE :"+serverListening("218.233.105.58", 10226));
    	
    }
	
}
