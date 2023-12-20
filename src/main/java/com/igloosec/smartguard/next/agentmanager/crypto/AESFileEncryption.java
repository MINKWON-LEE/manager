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
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.AlgorithmParameters;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

/** package com.javapapers.java.security; **/

public class AESFileEncryption {

	Logger logger = LoggerFactory.getLogger(getClass());
	PatterBuilder patterBuilder = new PatterBuilder();
	

//	public void encryptionService(JobEntity jobEntity){
//
//	}
	@SuppressWarnings("finally")
	public File[] encryptionService(JobEntity jobEntity){

		String jobType = jobEntity.getJobType();
		String path = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.SEND,jobType);
		String fileName = jobEntity.getFileName();
		File[] files = new File[3];

		try {
			// file to be encrypted
			FileInputStream inFile = new FileInputStream(path+fileName+jobEntity.getFileType());

			// encrypted file
			FileOutputStream outFile = new FileOutputStream(path + fileName + ".des");
			files[0] = new File(path + fileName + ".des");

			// password to encrypt the file
			String manner = PatternMaker.ENCRPTION_PMANNER;

			// password, iv and salt should be transferred to the other end
			// in a secure manner

			// salt is used for encoding
			// writing it to a file
			// salt should be transferred to the recipient securely
			// for decryption
			byte[] salt = new byte[8];
			SecureRandom secureRandom = new SecureRandom();
			secureRandom.nextBytes(salt);
			FileOutputStream saltOutFile = new FileOutputStream(path + fileName + ".salt");
			files[1] = new File(path + fileName + ".salt");
			saltOutFile.write(salt);
			saltOutFile.close();

			SecretKeyFactory factory = SecretKeyFactory
					.getInstance("PBKDF2WithHmacSHA1");
			KeySpec keySpec = new PBEKeySpec(manner.toCharArray(), salt, 65536,
					256);
			SecretKey secretKey = factory.generateSecret(keySpec);
			SecretKey secret = new SecretKeySpec(secretKey.getEncoded(), "AES");

			//
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secret);
			AlgorithmParameters params = cipher.getParameters();

			// iv adds randomness to the text and just makes the mechanism more
			// secure
			// used while initializing the cipher
			// file to store the iv
			FileOutputStream ivOutFile = new FileOutputStream(path + fileName + ".iv");
			files[2] = new File(path + fileName + ".iv");
			byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
			ivOutFile.write(iv);
			ivOutFile.close();

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

			inFile.close();
			outFile.flush();
			outFile.close();
		}catch (Exception e){
			logger.error(CommonUtils.printError(e));
		}finally {
			return files;
		}
	}

	public static void main(String[] args) throws Exception {

//		get script
//		new AESFileEncryption().encryptionService("./script/","Example-1.0-SNAPSHOT.jar","script");

//		new SocketClient("127.0.0.1","1978",INMEMORYDB.agentKey).connectionSocket("./script/","script");



	}

}
