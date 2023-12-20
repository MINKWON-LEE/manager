package com.igloosec.smartguard.next.agentmanager.main;


import com.igloosec.smartguard.next.agentmanager.crypto.JasyptUtility;

public class DBinfoMain {

	public static void main(String[] args) throws Exception {

//		System.out.println(new JasyptUtility().encrypt("jdbc:log4jdbc:mariadb://192.168.80.120:13306/snet"));
//		System.out.println(new JasyptUtility().encrypt("sgdbadm"));
//		System.out.println(new JasyptUtility().encrypt("sgdbadm$2015"));
//
//		System.out.println(new JasyptUtility().encrypt("jdbc:log4jdbc:mariadb://127.0.0.1:3306/snet"));
//		System.out.println(new JasyptUtility().encrypt("root"));
//		System.out.println(new JasyptUtility().encrypt("1234"));

		System.out.println("!!!!!!!!!!!!!!");
		if (args[0].equals("dec")) {
			System.out.println("Decrypt  [" + args[1] + "] ");
			System.out.println(new JasyptUtility().decrypt(args[1]));
		} else if (args[0].equals("enc")) {
			System.out.println("Encrypt : " + args[1]);
			System.out.println(new JasyptUtility().encrypt(args[1]));


		} else {
			System.out.println("Not Parameter!!" );
			System.out.println("example) dec Input OR enc Input" );
		}
	}
}

