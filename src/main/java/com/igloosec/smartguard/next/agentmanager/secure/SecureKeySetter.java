/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.secure.SecureKeySetter.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 2. 3.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.secure;

import java.io.IOException;

public class SecureKeySetter {	  

      Runtime runtime = Runtime.getRuntime();
	  Process process = null;

	 @SuppressWarnings("unused")
	private void createKey(){
		  String cmd = "";
	      try {
	          process = runtime.exec(cmd);
	          process.waitFor(); // 0 = 성공 , 1 = 실패
	      } catch (IOException e) {
	          e.printStackTrace();
	      } catch (InterruptedException e) {
	          e.printStackTrace();
	      }
	  
	  }
	
	  
}
