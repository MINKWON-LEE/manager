/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.utils.SslSocketHandler.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 1. 28.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.utils;


import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;


public class SslSocketHandler {


    public String keyStorePath = "C:\\keytest\\snetsecure";
    public String keyStorePw = "s**t*01*!s**u**";
    public int listeningPort = 1115;
      
	public void doSSL(){
		
        try{
            // 만들어놨던 server keyStore 파일을 임의의 폴더에 위치시키고 아래와 같이 설정해준다.
            System.setProperty("javax.net.ssl.keyStore",keyStorePath);
             
            // password는 keyStore를 만들 때 입력했던 것.
            System.setProperty("javax.net.ssl.keyStorePassword",keyStorePw);
             
            // 디버깅을 위해 아래와 같이 설정.
            System.setProperty("javax.net.debug","ssl");
             
            System.out.println("***********keyStore : " + System.getProperty("javax.net.ssl.keyStore"));
            System.out.println("***********trustStore : " + System.getProperty("javax.net.ssl.trustStore"));
             
            // 서버 소켓 팩토리 생성.
            SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
             
            // 서버 소켓 생성. 1115는 포트 번호.
            SSLServerSocket sslserversocket = (SSLServerSocket)sslserversocketfactory.createServerSocket(listeningPort);
            System.out.println("Wating Connection");
             
            // 클라이언트가 언제 접속할지 모르니 항상 대기.
            while(true){
                SSLSocket socket = (SSLSocket)sslserversocket.accept();
                // 데이터 읽는 부분은 쓰레드로 처리.
                ThreadServer thread = new ThreadServer(socket);
                thread.start();
            }
             
        }catch(Exception ex){
            System.out.println(ex);
        }
         
    }
    

class ThreadServer extends Thread {
 
    private SSLSocket socket;
    private InputStream input;
    private InputStreamReader reader;
    private BufferedReader br;
 
    public ThreadServer(SSLSocket socket){
        this.socket = socket;
    }
 
    @Override
    public void run() {
         
        try{
            String fromClient = null;
            input = socket.getInputStream();
            reader = new InputStreamReader(input);
            br = new BufferedReader(reader);
             
            while((fromClient = br.readLine())!=null){
                System.out.println(fromClient);
                System.out.flush();
            }
             
        }catch(Exception e){
        }
         
    }
     
}


class SyncPipe implements Runnable
{
public SyncPipe(InputStream istrm, OutputStream ostrm) {
      istrm_ = istrm;
      ostrm_ = ostrm;
  }
  public void run() {
      try
      {
          final byte[] buffer = new byte[1024];
          for (int length = 0; (length = istrm_.read(buffer)) != -1; )
          {
              ostrm_.write(buffer, 0, length);
          }
      }
      catch (Exception e)
      {
          e.printStackTrace();
      }
  }
  private final OutputStream ostrm_;
  private final InputStream istrm_;
}


String keyName = "key2";
String keyStoreGenCmd = "C:\\Java\\JAVA7\\bin\\keytool -genkey -v -keystore "+keyName+" -alias "+keyName+"alias -keyalg RSA -keysize 2048 -validity 10000";
String pubKeyExportCmd = "C:\\Java\\JAVA7\\bin\\keytool -export -alias smykey -keystore mykey -rfc -file myKey.cer";
InputStream cmdIn;
PrintStream cmdOut;

public String readUntil(String pattern) {
	 try {
		 
		 char lastChar = pattern.charAt(pattern.length()-1);
		 System.out.println("lastChar="+lastChar + " pattern='"+pattern+"'");
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


public void prepareKeyStore() throws IOException, InterruptedException {
	String commands[] = {"cmd", };
	
	 
	StringBuilder sb = new StringBuilder();
	
	Process process = Runtime.getRuntime().exec(commands);
	
	InputStream inps = process.getInputStream();
	InputStream einps = process.getErrorStream();
	OutputStream outps = process.getOutputStream();
	
	 new Thread(new SyncPipe(einps, System.err)).start();
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

public static void main(String args[]){
	
	SslSocketHandler sslh = new SslSocketHandler();
	//sslh.doSSL();
	try {
		sslh.prepareKeyStore();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	
}
    
}







     
     
