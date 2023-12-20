package com.igloosec.smartguard.next.agentmanager.crypto;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

public class JasyptUtility {


	public String decrypt(String beforeStr) {
		StandardPBEStringEncryptor standardPBEStringEncryptor = new StandardPBEStringEncryptor();
		standardPBEStringEncryptor.setAlgorithm("PBEWITHMD5ANDDES");
		standardPBEStringEncryptor.setPassword("igloosec@2019");
		String des = standardPBEStringEncryptor.decrypt(beforeStr);
		return des;
	}

	public String encrypt(String beforeStr) {
		StandardPBEStringEncryptor standardPBEStringEncryptor = new StandardPBEStringEncryptor();
		standardPBEStringEncryptor.setAlgorithm("PBEWITHMD5ANDDES");
		standardPBEStringEncryptor.setPassword("igloosec@2019");
		String des = standardPBEStringEncryptor.encrypt(beforeStr);
		return des;
	}
}
