/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.concurrents.NoSleepThread.java
 * @author : Je Joong Lee
 * created at : 2016. 1. 5.
 * description :
 */
package com.igloosec.smartguard.next.agentmanager.crypto;


import com.igloosec.smartguard.next.agentmanager.entity.JobEntity;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.utils.CommonUtils;
import com.igloosec.smartguard.next.agentmanager.utils.PatterBuilder;
import com.sk.snet.manipulates.PatternMaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// ECB MODE
public class AESCryptography {

	private Logger logger = LoggerFactory.getLogger(getClass());
	

	static PatterBuilder patterBuilder = new PatterBuilder();
	public static byte[] key = PatternMaker.ENCRPTION_PMANNER.getBytes();
	
	@SuppressWarnings("finally")
	public File[]  encryptionFile(JobEntity jobEntity) throws Exception {
		String jobType = jobEntity.getJobType();
		String path =  INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.SEND, jobType);
		String fileName = jobEntity.getFileName();

		File[] files = new File[3];
		FileInputStream inFile = null;
		FileOutputStream outFile = null;
		try {
			// file to be encrypted
			inFile = new FileInputStream(path+ File.separator+ fileName+jobEntity.getFileType());

			// encrypted file
			files[0] = new File(path + fileName + ".des");
			outFile = new FileOutputStream(path + fileName + ".des");

			SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			
			//file encryption
			byte[] input = new byte[64];
			int bytesRead;

			while ((bytesRead = inFile.read(input)) != -1) {
				byte[] output = cipher.update(input, 0, bytesRead);
				if (output != null)
					outFile.write(output);
			}

			byte[] output = cipher.doFinal();
			if (output != null)
				outFile.write(output);

			outFile.flush();

		}catch (Exception e){
			logger.error("encryptionFile Exception :: {}", e.getMessage(), e.fillInStackTrace());
		}finally {
			if (inFile != null) {
				inFile.close();
			}
			if (outFile != null) {
				outFile.close();
			}

			return files;
		}
	}

	public void decryptionFile(JobEntity jobEntity) throws Exception {

		String path = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.RECV, jobEntity.getJobType());

		if (jobEntity.isUploadLogOnly()) {
			path = jobEntity.getUploadLogOnlyPath();
		}

		String fileName = jobEntity.getFileName();
		
		FileInputStream fis		= null;
		FileOutputStream fos	= null;
		try {
			fis = new FileInputStream(path+fileName+".des");

			SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
			
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);

			fos = new FileOutputStream(path+ fileName+jobEntity.getFileType());
			byte[] in = new byte[64];
			int read;
			while ((read = fis.read(in)) != -1) {
				byte[] output = cipher.update(in, 0, read);
				if (output != null)
					fos.write(output);
			}

			byte[] output = cipher.doFinal();
			if (output != null)
				fos.write(output);
			fos.flush();
		}catch (Exception e){
			logger.error("decryptionFile Exception :: {}", e.getMessage(), e.fillInStackTrace());
		}finally{
            if (fis != null) {
                fis.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
	}

	public void encryptionShFile(String srcDir) throws Exception {

		makeDir(srcDir);
		scannDir(srcDir);

		System.exit(0);
	}

	public void encryptionShFile2(String srcDir) throws Exception {

		makeDir(srcDir);
		scannDir(srcDir);
	}

	private void makeDir(String srcPath) throws IOException {
		File mk = new File(srcPath);
		for(File f : mk.listFiles()){
			if(f.getAbsolutePath().contains("tmp") || f.getAbsolutePath().contains("svn")) {
			}else if(f.isDirectory() ){
				makeDir(f.getAbsolutePath());
			}else{
				File tmp = new File(srcPath + File.separator + "tmp");
				tmp.mkdir();
			}
		}
	}

	void e_program(String srcFile, String dstDir) throws Exception {
		FileInputStream inFile = null;
		FileOutputStream outFile = null;

		File f = new File(srcFile);
		logger.debug("@@@@ dstDir : "+dstDir+f.getName());
		try {
			String fileNmae = CommonUtils.toMux(f.getName());
			// file to be encrypted
			inFile = new FileInputStream(f.getAbsolutePath());

			// encrypted file
			outFile = new FileOutputStream(dstDir+fileNmae);

			SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

			//file encryption
			byte[] input = new byte[64];
			int bytesRead;

			while ((bytesRead = inFile.read(input)) != -1) {
				byte[] output = cipher.update(input, 0, bytesRead);
				if (output != null)
					outFile.write(output);
			}

			byte[] output = cipher.doFinal();
			if (output != null)
				outFile.write(output);

			outFile.flush();

		}catch (Exception e){
			logger.debug(e.getMessage());
		}finally {
			if(inFile != null)inFile.close();
			if(outFile != null)outFile.close();
		}
	}

	public void scannDir(String path) throws Exception {
		String dstPath ;
		String srcFile ;

		File file = new File(path);
		for(File f:file.listFiles()){
			if (f.getAbsolutePath().contains(".svn") || f.getAbsolutePath().contains(".DS") || f.getAbsolutePath().contains("tmp")){

			} else if (f.isDirectory()) {
				scannDir(f.getAbsolutePath());
			} else {
				if(f.getAbsolutePath().contains("tmp")){
					f.delete();
				}
				dstPath = f.getParent()+ File.separator +"tmp" + File.separator;
				srcFile = f.getAbsolutePath();

				e_program(srcFile,dstPath);
			}
		}
	}

	@SuppressWarnings("finally")
	public boolean decryptionShFile(String srcDir , String dstDir) throws Exception {
		boolean isDec = false;
		FileInputStream fis		= null;
		FileOutputStream fos	= null;
		try {
			fis = new FileInputStream(srcDir);

			SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			fos = new FileOutputStream(dstDir);
			byte[] in = new byte[64];
			int read;
			while ((read = fis.read(in)) != -1) {
				byte[] output = cipher.update(in, 0, read);
				if (output != null)
					fos.write(output);
			}

			byte[] output = cipher.doFinal();
			if (output != null)
				fos.write(output);
			fos.flush();

			isDec = true;
		}catch (Exception e){
			if (fis != null) {
				fis.close();
			}
			if (fos != null) {
				fos.close();
			}
			logger.error("decryptionFile Exception :: {}", e.getMessage(), e.fillInStackTrace());
		}finally{
            if (fis != null) {
                fis.close();
            }
            if (fos != null) {
                fos.close();
            }

            return isDec;
		}
	}

	public void encryptionShFileSingle(String srcDir) throws Exception {

		FileInputStream inFile = null;
		FileOutputStream outFile = null;

		String dstDir = srcDir+"/tmp/";

		File file = new File(srcDir);

		File mk = new File(dstDir);
		if(!mk.exists()){
			//없다면 생성
			mk.mkdirs();
		}else{
			//있다면 현재 디렉토리 파일을 삭제
			File[] destroy = mk.listFiles();
			for(File des : destroy){
				des.delete();
			}
		}


		for (File f : file.listFiles()){
			System.out.println(f.getAbsolutePath());
			if (!f.isDirectory()){
				try {
					String fileNmae = CommonUtils.toMux(f.getName());
					// file to be encrypted
					inFile = new FileInputStream(f.getAbsolutePath());

					// encrypted file
					outFile = new FileOutputStream(dstDir+fileNmae);

					SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
					Cipher cipher = Cipher.getInstance("AES");
					cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

					//file encryption
					byte[] input = new byte[64];
					int bytesRead;

					while ((bytesRead = inFile.read(input)) != -1) {
						byte[] output = cipher.update(input, 0, bytesRead);
						if (output != null)
							outFile.write(output);
					}

					byte[] output = cipher.doFinal();
					if (output != null)
						outFile.write(output);

					outFile.flush();

				}catch (Exception e){
					logger.error("encryptionFile Exception :: {}", e.getMessage(), e.fillInStackTrace());
				}finally {
					inFile.close();
					outFile.close();
				}
			}

		}

		System.exit(0);
	}

	public void decScannDir(String srcDir, String destDir) throws Exception {
		CommonUtils.mkdir(destDir);

		String dstPath ;
		String srcFile ;

		File file = new File(srcDir);
		for(File f:file.listFiles()){
			if (f.getAbsolutePath().contains(".svn") || f.getAbsolutePath().contains(".DS") || f.getAbsolutePath().contains("tmp") || f.getAbsolutePath().contains("class")){

			} else if (f.isDirectory()) {
				scannDir(f.getAbsolutePath());
			} else {
				if(f.getAbsolutePath().contains("tmp")){
					f.delete();
				}
				srcFile = f.getAbsolutePath();
				String fileNmae = CommonUtils.toMux(f.getName());
				System.out.println(fileNmae);
				dstPath = destDir + File.separator + fileNmae;

				System.out.println("srcFile [" + srcFile + "] dstPath [" + dstPath + "]");

				decryptionShFile(srcFile,dstPath);
			}
		}
	}

	public String encryption2(Path filePath) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-512");
			byte[] mb = md.digest(Files.readAllBytes(filePath));
			return getString(mb);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return "ERROR";
		} catch (IOException e) {
			e.printStackTrace();
			return "ERROR";
		}
	}

	public String encryption(Path filePath) {
		MessageDigest md;
		try {
			byte[] mb = readFile(filePath);
			return getString(mb);
		} catch (IOException e) {
			e.printStackTrace();
			return "ERROR";
		}  catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return "ERROR";
		}
	}

	/**
	 * 사용자 패스워드를 SHA512로 HASH 한다.
	 * @param userPassword
	 *            사용자 패스워드
	 * @return Hash 값
	 *         hash : SHA-512
	 */
	public String encryption(String userPassword) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-512");

			md.update(userPassword.getBytes());
			byte[] mb = md.digest();
			return getString(mb);
		} catch (NoSuchAlgorithmException e) {
			return "ERROR";
		}
	}

	private String getString(byte[] bytes){
		String result = "";
		for (int i = 0; i < bytes.length; i++) {
			byte temp = bytes[i];
			String s = Integer.toHexString(new Byte(temp));
			while (s.length() < 2) {
				s = "0" + s;
			}
			s = s.substring(s.length() - 2);
			result += s;
		}
		return result;
	}

	private byte[] readFile(Path filePath) throws NoSuchAlgorithmException, IOException {
		MessageDigest md = MessageDigest.getInstance("SHA-512");
		FileInputStream is = null;
		try {
			is = new FileInputStream(filePath.toString());
			byte[] buffer = new byte[1024];
			int readBytes = 0;

			while ((readBytes = is.read(buffer)) > -1) {
				md.update(buffer, 0, readBytes);
			}

			return md.digest();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
