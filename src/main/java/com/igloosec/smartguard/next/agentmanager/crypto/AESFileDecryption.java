/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.concurrents.NoSleepThread.java
 * @author : Je Joong Lee
 * created at : 2016. 1. 5.
 * description :
 */
package com.igloosec.smartguard.next.agentmanager.crypto;

import com.igloosec.smartguard.next.agentmanager.entity.JobEntity;

import com.igloosec.smartguard.next.agentmanager.exception.SnetCommonErrCode;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.utils.CommonUtils;
import com.igloosec.smartguard.next.agentmanager.utils.PatterBuilder;
import com.sk.snet.manipulates.PatternMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

// CBC MODE
public class AESFileDecryption {
	
	  private Logger logger = LoggerFactory.getLogger(getClass());

	  @SuppressWarnings("unused")
	  public void decryptionService(JobEntity jobEntity) throws Exception {
		  String path = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.RECV, jobEntity.getJobType());
		  String fileName = jobEntity.getFileName();
//		  String type = jobEntity.getJobType();

		  try {
			  
			  PatterBuilder patterBuilder = new PatterBuilder();
			  String PMAN = PatternMaker.ENCRPTION_PMANNER;

			  // reading the salt
			  // user should have secure mechanism to transfer the
			  // salt, iv and password to the recipient
			  FileInputStream saltFis = new FileInputStream(path+fileName+".salt");
			  byte[] salt = new byte[8];
			  saltFis.read(salt);
			  saltFis.close();

			  // reading the iv
			  FileInputStream ivFis = new FileInputStream(path+fileName+".iv");
			  byte[] iv = new byte[16];
			  ivFis.read(iv);
			  ivFis.close();

			  SecretKeyFactory factory = SecretKeyFactory
					  .getInstance("PBKDF2WithHmacSHA1");
			  KeySpec keySpec = new PBEKeySpec(PMAN.toCharArray(), salt, 65536,
					  256);
			  SecretKey tmp = factory.generateSecret(keySpec);
			  SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

			  // file decryption
			  Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			  cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
			  FileInputStream fis = new FileInputStream(path+fileName+".des");

			  FileOutputStream fos = new FileOutputStream(path+fileName+jobEntity.getFileType());
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
			  fis.close();
			  fos.flush();
			  fos.close();
		  }catch (InvalidKeySpecException ie) {
			  logger.error(CommonUtils.printError(ie));
			  throw new Exception(SnetCommonErrCode.ERR_0007.getMessage());
		  }
		  catch (FileNotFoundException fe) {
			  logger.error(CommonUtils.printError(fe));
			  throw new Exception(SnetCommonErrCode.ERR_0032.getMessage());
		  }
		  catch (NoSuchAlgorithmException fe) {
			  logger.error(CommonUtils.printError(fe));
			  throw new Exception(SnetCommonErrCode.ERR_0007.getMessage());
		  }
		  catch (IOException ioe) {
			  logger.error(CommonUtils.printError(ioe));
			  throw new Exception(SnetCommonErrCode.ERR_0009.getMessage());
		  }
		 
	  }

	public static void main(String[] args) throws Exception {

//		new AESFileDecryption().decryptionService("/usr/local/snet/txFiles/outbound/otp/","6967590355428302875", INMEMORYDB.OTP);

//		new AESFileEncryption().encryptionService("/usr/local/snet/txFiles/outbound/otp/","6967590355428302875", INMEMORYDB.OTP);
	}
}