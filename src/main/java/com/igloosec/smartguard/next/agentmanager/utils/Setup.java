/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.utils.Setup.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 1. 29.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class Setup {


String keyName = "key2";
String keyStoreGenCmd = "C:\\Java\\JAVA7\\bin\\keytool -genkey -v -keystore "+keyName+" -alias "+keyName+"alias -keyalg RSA -keysize 2048 -validity 10000";
String pubKeyExportCmd = "C:\\Java\\JAVA7\\bin\\keytool -export -alias smykey -keystore mykey -rfc -file myKey.cer";
InputStream cmdIn;
PrintStream cmdOut;

public String readUntil(String pattern) {
	 try {
		 
		 char lastChar = pattern.charAt(pattern.length()-1);
		 //System.out.println("lastChar="+lastChar + " pattern='"+pattern+"'");
	 StringBuffer sb = new StringBuffer();
	 boolean found = false;
	 int asci = cmdIn.read(); 
	 char ch = (char) asci; 
	 
	 while (true) {
		 //System.out.println(sb.toString());
	 sb.append(ch);
			 if (ch == lastChar) {
			 if (sb.toString().endsWith(pattern)) {
			//	 System.out.println(sb.toString());
			 return sb.toString();
			 }
			 }
			 asci = cmdIn.read();
			 ch = (char) asci;
	 }
	 }
	 catch (Exception e) {
	 e.printStackTrace();
	 }
	 return null;
	 }


public void write(String value) {
	 try {
		 cmdOut.println(value);
		 cmdOut.flush();	 
	 }
	 catch (Exception e) {
	 e.printStackTrace();
	 }
	 }


public void doRuntimeCommand(String CMDTYPE, String OSTYPE) throws IOException {
	
	StringBuilder sb = new StringBuilder();
	Process process;
	InputStream cmdIn;
	PrintStream cmdOut;
	
	if(OSTYPE.equals("WIN")){
		String commands[] =  {"cmd", };
		process = Runtime.getRuntime().exec(commands);
	}else {
		String commands[] =  {"pwd", };
		process = Runtime.getRuntime().exec(commands);
	}
	
	
	InputStream einps = process.getErrorStream();
	cmdIn = process.getInputStream(); 
	cmdOut = new PrintStream(process.getOutputStream());
	
	
	
}


public void prepareKeyStore() throws IOException, InterruptedException {
	String commands[] = {"cmd", };
	
	 
	StringBuilder sb = new StringBuilder();
	
	Process process = Runtime.getRuntime().exec(commands);
	
	InputStream inps = process.getInputStream();
	InputStream einps = process.getErrorStream();
	OutputStream outps = process.getOutputStream();
	
	 //new Thread(new SyncPipe(einps, System.err)).start();
	 //new Thread(new SyncPipe(inps, System.out)).start();
	 cmdIn = inps;
	 cmdOut = new PrintStream(outps);
	 
	String pr = readUntil("r>");
	System.out.println(pr);
	write("java -version");
	pr = readUntil("r>");
	System.out.println("------"+pr);
//	BufferedReader br = new BufferedReader( new InputStreamReader( inps ) );

	
//	while( (line = br.readLine()) != null ){	            
//		System.out.println("line: "+line);
//		stdin.println("java -version");
//        sb.append(line);
//    }
    
//    System.out.println(sb.toString());
	
    
//    stdin.close();
	cmdOut.close();
    int returncd = process.waitFor();
    
	
	
      
      
}

	
}
