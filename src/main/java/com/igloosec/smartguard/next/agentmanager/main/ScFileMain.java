package com.igloosec.smartguard.next.agentmanager.main;


import com.igloosec.smartguard.next.agentmanager.crypto.AESCryptography;
import com.igloosec.smartguard.next.agentmanager.utils.CommonUtils;
import com.igloosec.smartguard.next.agentmanager.utils.ZipUtil;

public class ScFileMain {

	public static void main(String[] args) throws Exception {

		if(args[0].equals("dec")) {
			System.out.println("Decrypt files ["+args[1] + "] to [" + args[2] + "]");
			new AESCryptography().decScannDir(args[1], args[2]);
		} else if (args[0].equals("backup")) {
			System.out.println("Decrypt files ["+args[1] + "] to [" + args[2] + "]");
			new AESCryptography().decryptionShFile(args[1], args[2] + ".zip");
			CommonUtils.mkdir(args[2]);

			new ZipUtil(args[2] + ".zip", "./" + args[2] + "/").unzip();
		} else {
			System.out.println("BUILD : " + args[0]);
			new AESCryptography().encryptionShFile(args[0]);
		}
	}
}
