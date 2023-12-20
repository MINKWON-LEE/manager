/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.utils.ShellCommandHandler.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 5. 3.
 * description :
 */
package com.igloosec.smartguard.next.agentmanager.utils;

import jodd.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ShellCommandHandler {

private Process p;
private Runtime r;
StreamGobbler errorPrc;
StreamGobbler inputPrc;
public OutputStream outputPrc;

Logger logger = LoggerFactory.getLogger(getClass());
	
public ShellCommandHandler(){
	this.r = Runtime.getRuntime();
}


public String execShCommandSimple(String command) {
	
	StringBuffer output = new StringBuffer();
	
	String line;
	try {
		p = r.exec(command);		
		BufferedReader br =new BufferedReader(new InputStreamReader(p.getInputStream()));
		while((line=br.readLine()) != null){
			output.append(line);
		}        
		p.waitFor();		
	} catch (Exception e) {
		output.append(e.getMessage());
	}
	 
	return output.toString();
	
}


public String execShCommand(String command) {
	
		StringBuffer output = new StringBuffer();
		try {
			logger.debug("Calling shellCommand="+ command);
			p = r.exec(command);
			
			
			final List<String> outList = new ArrayList<String>();
			final List<String> errorList = new ArrayList<String>();

			errorPrc = new StreamGobbler( p.getErrorStream(), errorList); 
			inputPrc = new StreamGobbler( p.getInputStream(), outList);			
			outputPrc = p.getOutputStream();			
			inputPrc.setOutputStream(outputPrc);
			
	        errorPrc.start();
	        inputPrc.start();
	        
			p.waitFor();
			
			if (errorList.size() > 0) {
				String errMsg = "Error : " + errorList.get(0);
				output.append("errMsg : " + errMsg);
				
			} else {
				int outLen = outList.size();
				System.out.println("Total Line:"+outLen);
				for(String str : outList){
					System.out.println(str);
				}				
			}
			 
		} catch (Exception e) {
			output.append(e.getMessage());
		}
		finally{
			
		} 
		return output.toString();
	}

private static class StreamGobbler extends Thread {
    private InputStream mInputStream;
	private List<String> mDataResult;
    private OutputStream mOutputStream;
    
    public StreamGobbler(InputStream is, List<String> DataResult) {
        this.mInputStream = is;
        this.mDataResult = DataResult; 
    }
    public void setOutputStream(OutputStream out){
    	mOutputStream = out;
    }
    
	public void run() {
        super.run();

        this.mDataResult.clear();
        BufferedReader br = null;
        try{
            br = new BufferedReader( new InputStreamReader( this.mInputStream ) );

            String line = null;
            while( (line = br.readLine()) != null ){	            
                this.mDataResult.add( line ); 
                System.out.println("#################################"+line);
                if(line.contains("password:")){
                	mOutputStream.write("snetdb!2016".getBytes());
                	mOutputStream.flush();
                }
            } 
        }catch( IOException e ){
            e.printStackTrace(); 
        }finally{ 
            try { 
                if( br != null ) 
                    br.close(); 
                this.mInputStream.close(); 
            } catch (IOException e) {
                e.printStackTrace(); 
            } 
        } 
    }
}



public static void main(String args[]){
	String aa = "1010156672|^|70719|^|100245005|^|아포송천S_DU3|^|52|^|0056|^|1200|^|1202|^|1200|^|1223|^|1205|^|0|^|132|^|0|^|0|^|0|^|132|^|0|^|0|^|0|^|0|^|0|^|0|^|0";
	String[] ls =  StringUtil.split(aa, "|^|");
	System.out.println(ls.length);
	System.out.println(ls[0]);
	System.out.println(ls[1]);
}

	
	
}
